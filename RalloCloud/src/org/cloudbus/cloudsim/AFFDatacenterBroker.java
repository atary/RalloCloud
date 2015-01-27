/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * Arbitrary first fit (cloudsim default, overridden only for consistency)
 *
 * @author Atakan
 */
public class AFFDatacenterBroker extends BrokerStrategy {

    protected Map<Integer, List<Integer>> datacenterRequestedIdsMap;

    public AFFDatacenterBroker(String name) throws Exception {
        super(name);
        datacenterRequestedIdsMap = new HashMap<>();
    }

    @Override
    protected void createSingleVm(int vmId) {
        ArrayList<Integer> requestedDCs = new ArrayList<>();
        if (datacenterRequestedIdsMap.get(vmId) != null) {
            requestedDCs.addAll(datacenterRequestedIdsMap.get(vmId));
        }
        Vm vm = VmList.getById(getVmList(), vmId);
        for (int dcId : datacenterIdsList) {
            if (!requestedDCs.contains(dcId)) {
                setVmsRequested(getVmsRequested() + 1);
                Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vmId + " in " + CloudSim.getEntityName(dcId) + " (" + dcId + ")");
                sendAt(dcId, getVmTime(vmId), CloudSimTags.VM_CREATE_ACK, vm);
                requestedDCs.add(dcId);
                datacenterRequestedIdsMap.put(vmId, requestedDCs);
                return;
            }
        }
        Log.printLine(CloudSim.clock() + ": " + getName() + ": None of the datacenters were available for VM #" + vmId + ". Retrying...");

        datacenterRequestedIdsMap.remove(vmId);
        createSingleVm(vmId);
    }

    @Override
    protected void createGroupVm(List<Integer> g, Double[][] t) {
        for (int vmId : g) {
            createSingleVm(vmId);
        }
    }
}
