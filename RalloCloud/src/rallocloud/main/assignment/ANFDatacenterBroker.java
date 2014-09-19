/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main.assignment;


import java.util.List;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * Arbitrary next fit
 * @author Atakan
 */
public class ANFDatacenterBroker extends org.cloudbus.cloudsim.DatacenterBroker{
    
    public static int i;
    
    public ANFDatacenterBroker(String name) throws Exception{
        super(name);
    }
    
    @Override
    protected void processResourceCharacteristics(SimEvent ev) {
            DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
            getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

            if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
                    //setDatacenterRequestedIdsList(new ArrayList<Integer>());
                    createVmsInDatacenter(getDatacenterIdsList());
            }
    }
    
    protected void createVmsInDatacenter(List<Integer> datacenterIds) {
        int requestedVms = 0;
        for (Vm vm : getVmList()) {
            int datacenterId = datacenterIds.get(i++ % datacenterIds.size());
            String datacenterName = CloudSim.getEntityName(datacenterId);
            if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
                    Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
                                    + " in " + datacenterName);
                    sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
                    requestedVms++;
            }
            //getDatacenterRequestedIdsList().add(datacenterId);
        }

        setVmsRequested(requestedVms);
        setVmsAcks(0);
    }
}
