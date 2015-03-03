package org.cloudbus.cloudsim.provisioners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.NetworkTopologyPublic;
import org.cloudbus.cloudsim.Vm;

public class BwProvisionerNetworked extends BwProvisioner {

    private final Map<String, Long> bwTable;
    private final Map<String, Long> bwLinkTable;
    private final int DCid;
    private final static HashSet<BwProvisionerNetworked> provisioners = new HashSet<>();

    public BwProvisionerNetworked(long bw, int DCid) {
        super(bw);
        bwTable = new HashMap<>();
        bwLinkTable = new HashMap<>();
        this.DCid = DCid;
        provisioners.add(this);
    }

    public boolean allocateBwForVmLink(Vm vm, long bw) {
        if (getAvailableBw() >= bw) {
            setAvailableBw(getAvailableBw() - bw);
            bwLinkTable.put(vm.getUid(), bw);
            return true;
        }
        return false;
    }

    public void deallocateBwForVmLink(Vm vm) {
        if (bwLinkTable.containsKey(vm.getUid())) {
            long amountFreed = bwLinkTable.remove(vm.getUid());
            setAvailableBw(getAvailableBw() + amountFreed);
        }
    }

    @Override
    public boolean allocateBwForVm(Vm vm, long bw) {
        //deallocateBwForVm(vm);
        if (getAvailableBw() >= bw) {
            ArrayList<Datacenter> DCs = NetworkTopologyPublic.getShortestPathDCs(vm, DCid);
            for (Datacenter d : DCs) {
                long avail = d.getHostList().get(0).getBwProvisioner().getAvailableBw();
                if (avail < bw) {
                    return false;
                }
            }
            for (Datacenter d : DCs) {
                if (!((BwProvisionerNetworked) d.getHostList().get(0).getBwProvisioner()).allocateBwForVmLink(vm, bw)) {
                    for (BwProvisionerNetworked bwp : provisioners) {
                        bwp.deallocateBwForVmLink(vm);
                    }
                    return false;
                }
            }
            setAvailableBw(getAvailableBw() - bw);
            bwTable.put(vm.getUid(), bw);
            vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
            return true;
        }
        return false;
    }

    @Override
    public long getAllocatedBwForVm(Vm vm) {
        if (bwTable.containsKey(vm.getUid())) {
            return bwTable.get(vm.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateBwForVm(Vm vm) {
        if (bwTable.containsKey(vm.getUid())) {
            long amountFreed = bwTable.remove(vm.getUid());
            setAvailableBw(getAvailableBw() + amountFreed);
            vm.setCurrentAllocatedBw(0);
            for (BwProvisionerNetworked bwp : provisioners) {
                bwp.deallocateBwForVmLink(vm);
            }
        }
    }

    @Override
    public void deallocateBwForAllVms() {
        throw new UnsupportedOperationException("Not supported yet - Atakan."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSuitableForVm(Vm vm, long bw) {
        throw new UnsupportedOperationException("Not supported yet - Atakan."); //To change body of generated methods, choose Tools | Templates.
    }

}
