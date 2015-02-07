package org.cloudbus.cloudsim.provisioners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.cloudbus.cloudsim.NetworkTopologyPublic;
import org.cloudbus.cloudsim.Vm;

public class BwProvisionerNetworked extends BwProvisioner {

    private final Map<String, Long> bwTable;
    private final Map<Integer, Long> linkTable;
    private final int DCid;

    public BwProvisionerNetworked(long bw, int DCid) {
        super(bw);
        bwTable = new HashMap<>();
        linkTable = new HashMap<>();
        this.DCid = DCid;
    }

    @Override
    public boolean allocateBwForVm(Vm vm, long bw) {
        //deallocateBwForVm(vm);
        NetworkTopologyPublic.getShortestPathDCs(vm, DCid);
        if (getAvailableBw() >= bw) {
            setAvailableBw(getAvailableBw() - bw);
            bwTable.put(vm.getUid(), bw);
            vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
            return true;
        }

        //vm.setCurrentAllocatedBw(getAllocatedBwForVm(vm));
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
