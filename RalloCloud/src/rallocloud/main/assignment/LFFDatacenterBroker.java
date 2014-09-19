/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main.assignment;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import rallocloud.main.MyNetworkTopology;
import rallocloud.main.Statistician;

/**
 * Latency based first fit
 * @author Atakan
 */
public class LFFDatacenterBroker extends org.cloudbus.cloudsim.DatacenterBroker{
    
    public static int i;

    public LFFDatacenterBroker(String name) throws Exception {
        super(name);
    }
    
    @Override
    protected void processResourceCharacteristics(SimEvent ev) {
            DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
            getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

            if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
                    setDatacenterRequestedIdsList(new ArrayList<Integer>());
                    createVmsInDatacenter(getDatacenterIdsList(),-1);
            }
    }
    
    protected void createVmsInDatacenter(List<Integer> datacenterIds, int rejectedId) {
        int requestedVms = 0;
        getDatacenterRequestedIdsList().add(rejectedId);
        for (Vm vm : getVmList()) {
            double minDelay = Double.MAX_VALUE;
            int datacenterId = -1;
            for(int dcId : datacenterIds){
                double delay = MyNetworkTopology.getDelay(dcId, vm.getUserId());
                if(delay < minDelay && !getDatacenterRequestedIdsList().contains(dcId)){
                    minDelay = delay;
                    datacenterId = dcId;
                }
            }
            String datacenterName = CloudSim.getEntityName(datacenterId);
            System.out.println(datacenterName);
            if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
                Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
                                + " in " + datacenterName);
                sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
                requestedVms++;
            } 
        }

        setVmsRequested(requestedVms);
        setVmsAcks(0);
    }
    
    @Override
    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];
        Statistician.trial();
        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().put(vmId, datacenterId);
            getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
            Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
                            + " has been created in Datacenter #" + datacenterId + ", Host #"
                            + VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
        } else {
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
                            + " failed in Datacenter #" + datacenterId);
            Statistician.rejected();
        }

        incrementVmsAcks();

        // all the requested VMs have been created
        if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
                submitCloudlets();
        } else {
            // all the acks received, but some VMs were not created
            if (getVmsRequested() == getVmsAcks()) {

                createVmsInDatacenter(getDatacenterIdsList(), datacenterId);

                // all datacenters already queried
                if (getVmsCreatedList().size() > 0) { // if some vm were created
                        submitCloudlets();
                } else { // no vms created. abort
                        Log.printLine(CloudSim.clock() + ": " + getName()
                                        + ": none of the required VMs could be created. Aborting");
                        finishExecution();
                }
            }
        }
    }
}
