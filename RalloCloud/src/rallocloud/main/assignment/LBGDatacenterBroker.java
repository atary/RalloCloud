/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main.assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cloudbus.cloudsim.Datacenter;
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
 *
 * @author Atakan
 */
public class LBGDatacenterBroker extends BrokerStrategy {

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
        Vm vm = null;
        for (Vm v : vmList) {
            if (v.getId() == vmId) {
                vm = v;
            }
        }
        double minUtil = Double.MAX_VALUE;
        int dcId = -1;
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
            for (int i = 0; i < dc.getHostList().size(); i++) {
                ramCap += dc.getHostList().get(i).getRam();
            }

            double ramUse = 0;
            for (Vm v : vmList) {
                if (v.getHost() != null && v.getHost().getDatacenter().getId() == di) {
                    ramUse += v.getRam();
                }
            }

            double util = ramUse / ramCap;
            
            System.out.println("util: " + util + "\n");

            if (util < minUtil) {
                minUtil = util;
                dcId = di;
            }
        }
        if (dcId != -1) {
            setVmsRequested(getVmsRequested() + 1);
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vmId + " in " + CloudSim.getEntityName(dcId));
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
    protected void createGroupVm(Set<Integer> g) {
        for (int vmId : g) {
            createSingleVm(vmId);
        }
    }

}
