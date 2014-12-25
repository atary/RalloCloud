/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main.assignment;

import java.util.Random;
import java.util.Set;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

/**
 * Random
 *
 * @author Atakan
 */
public class RANDatacenterBroker extends BrokerStrategy {

    public RANDatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void createSingleVm(int vmId) {
        Vm vm = null;
        for (Vm v : vmList) {
            if (v.getId() == vmId) {
                vm = v;
            }
        }
        Random randomGenerator = new Random();
        int dcId = datacenterIdsList.get(randomGenerator.nextInt(datacenterIdsList.size()));
        setVmsRequested(getVmsRequested() + 1);
        Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vmId + " in " + CloudSim.getEntityName(dcId));
        sendNow(dcId, CloudSimTags.VM_CREATE_ACK, vm);
    }

    @Override
    protected void createGroupVm(Set<Integer> g) {
        for (int vmId : g) {
            createSingleVm(vmId);
        }
    }

}
