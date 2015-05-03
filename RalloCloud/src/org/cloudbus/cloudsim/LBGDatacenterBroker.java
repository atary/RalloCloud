/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

/**
 * Latency based first fit
 *
 * @author Atakan
 */
public class LBGDatacenterBroker extends DatacenterBrokerStrategy {

    protected Map<Integer, List<Integer>> datacenterRequestedIdsMap;

    public LBGDatacenterBroker(String name) throws Exception {
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
        double minUtil = Double.MAX_VALUE;
        int dcId = -1;
        Collections.shuffle(datacenterIdsList);
        for (int di : datacenterIdsList) {
            if (requestedDCs.contains(di)) {
                continue;
            }
            Datacenter dc = null;
            for (Datacenter d : datacenterList) {
                if (d.getId() == di) {
                    dc = d;
                    break;
                }
            }
            double ramCap = 0;
            double ramUse = 0;

            for (int i = 0; i < dc.getHostList().size(); i++) {
                ramCap = dc.getHostList().get(i).getRam();
            }

            for (Vm v : AllVmList) {
                if (v.getHost() != null && v.getHost().getDatacenter().getId() == di) {
                    ramUse += v.getRam();
                }
            }

            double util = ramUse / ramCap;

            if (util < minUtil) {
                minUtil = util;
                dcId = di;
            }
        }
        if (dcId != -1) {
            setVmsRequested(getVmsRequested() + 1);
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vmId + " in " + CloudSim.getEntityName(dcId) + " (" + dcId + ")");
            sendAt(dcId, getVmTime(vmId), CloudSimTags.VM_CREATE_ACK, vm);
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
        for (int vmId : g) {
            createSingleVm(vmId);
        }
    }

}
