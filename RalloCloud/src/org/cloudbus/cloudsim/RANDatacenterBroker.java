/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim;

import java.util.List;
import java.util.Random;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * Random
 *
 * @author Atakan
 */
public class RANDatacenterBroker extends DatacenterBrokerStrategy {

    public RANDatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void createSingleVm(int vmId) {
        Vm vm = VmList.getById(getVmList(), vmId);
        Random randomGenerator = new Random();
        int dcId = datacenterIdsList.get(randomGenerator.nextInt(datacenterIdsList.size()));
        setVmsRequested(getVmsRequested() + 1);
        Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vmId + " in " + CloudSim.getEntityName(dcId) + " (" + dcId + ")");
        sendAt(dcId, getVmTime(vmId), CloudSimTags.VM_CREATE_ACK, vm);
    }

    @Override
    protected void createGroupVm(List<Integer> g, Double[][] t) {
        for (int vmId : g) {
            createSingleVm(vmId);
        }
    }

}
