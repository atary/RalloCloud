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
import java.util.Random;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * Random
 *
 * @author Atakan
 */
public class SNWDatacenterBroker extends DatacenterBrokerStrategy {

    protected Map<Integer, List<Integer>> datacenterRequestedIdsMap;

    public SNWDatacenterBroker(String name) throws Exception {
        super(name);
        datacenterRequestedIdsMap = new HashMap<>();
    }

    @Override
    protected void createSingleVm(int vmId) {
        ArrayList<Integer> requestedDCs = new ArrayList<>();
        if (datacenterRequestedIdsMap.get(vmId) != null) {
            requestedDCs.addAll(datacenterRequestedIdsMap.get(vmId));
        }
        List<Integer> group = new ArrayList<>();
        for (List<Integer> g : VmGroups.keySet()) {
            if (g.contains(vmId)) {
                group.addAll(g);
                break;
            }
        }
        double minDelay = Double.MAX_VALUE;
        int dcId = -1;
        for (int d : datacenterIdsList) {
            double totalDelay = 0;
            for (int i : group) {
                Vm vm = VmList.getById(getVmList(), i);
                if (vm.getHost() != null) {
                    int dc = vm.getHost().getDatacenter().getId();
                    double delay = NetworkTopologyPublic.getDelay(dc, d);
                    if (delay == 0) {
                        delay = 100;
                    }
                    totalDelay += delay;
                }
            }
            if (!requestedDCs.contains(d)) {
                if (totalDelay < minDelay) {
                    minDelay = totalDelay;
                    dcId = d;
                }
            }
        }
        if (dcId != -1) {
            Vm vm = VmList.getById(getVmList(), vmId);
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId() + " in " + CloudSim.getEntityName(dcId) + " (" + dcId + ")");
            sendNow(dcId, CloudSimTags.VM_CREATE_ACK, vm);
            requestedDCs.add(dcId);
            datacenterRequestedIdsMap.put(vmId, requestedDCs);
        } else {
            Log.printLine(CloudSim.clock() + ": " + getName() + ": None of the datacenters were available for VM #" + vmId + ". Retrying...");
            datacenterRequestedIdsMap.remove(vmId);
            createSingleVm(vmId);
        }
    }

    @Override
    protected void createGroupVm(List<Integer> g, Double[][] t) {
        ArrayList<Integer> sentVM = new ArrayList<>();
        ArrayList<Integer> requestedDC = new ArrayList<>();
        int vmId = g.get(0);
        Vm vm = VmList.getById(getVmList(), vmId);
        Random randomGenerator = new Random();
        int dcId = datacenterIdsList.get(randomGenerator.nextInt(datacenterIdsList.size()));
        sentVM.add(0);
        requestedDC.add(dcId);
        setVmsRequested(getVmsRequested() + 1);
        Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vmId + " in " + CloudSim.getEntityName(dcId) + " (" + dcId + ")");
        sendAt(dcId, getVmTime(vmId), CloudSimTags.VM_CREATE_ACK, vm);
        recursiveCreateVm(vmId, g, t, sentVM, requestedDC);
    }

    private void recursiveCreateVm(int vmId, List<Integer> g, Double[][] t, ArrayList<Integer> sent, ArrayList<Integer> requested) {
        Double[][] bwMatrix = NetworkTopologyPublic.getBwMatrix();
        for (int i = 0; i < t.length; i++) {
            if (t[vmId][i] > 0) {
                if (sent.contains(i)) {
                    continue;
                }
                for (int j = 0; j < bwMatrix.length; j++) {
                    if(requested.contains(j+2)){
                        continue;
                    }
                    if (bwMatrix[vmId][j] > 0) {
                        int datacenterId = j + 2;
                        sent.add(i);
                        requested.add(datacenterId);
                        Vm vm = VmList.getById(getVmList(), g.get(i));
                        setVmsRequested(getVmsRequested() + 1);
                        Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId() + " in " + CloudSim.getEntityName(datacenterId) + " (" + datacenterId + ")");
                        sendAt(datacenterId, GroupTimes.get(g), CloudSimTags.VM_CREATE_ACK, vm);
                        recursiveCreateVm(vmId, g, t, sent, requested);
                        break;
                    }
                }
            }
        }
    }

}
