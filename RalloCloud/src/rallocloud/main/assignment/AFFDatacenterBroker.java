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
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;

/**
 * Arbitrary first fit (cloudsim default, overridden only for consistency)
 *
 * @author Atakan
 */
public class AFFDatacenterBroker extends BrokerStrategy {
    
    protected Map<Integer,List<Integer>> datacenterRequestedIdsMap;


    public AFFDatacenterBroker(String name) throws Exception {
        super(name);
        datacenterRequestedIdsMap = new HashMap<>();
    }

    @Override
    protected void createSingleVm(int vmId) {
        ArrayList<Integer> requestedDCs = new ArrayList<>();
        if(datacenterRequestedIdsMap.get(vmId)!=null){
            requestedDCs.addAll(datacenterRequestedIdsMap.get(vmId));
        }
        Vm vm = null;
        for(Vm v : vmList)
            if(v.getId() == vmId)
                vm=v;
        for(int dcId : datacenterIdsList){
            if(!requestedDCs.contains(dcId)){
                setVmsRequested(getVmsRequested()+1);
                Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vmId + " in " + CloudSim.getEntityName(dcId));
                sendNow(dcId, CloudSimTags.VM_CREATE_ACK, vm);
                requestedDCs.add(dcId);
                datacenterRequestedIdsMap.put(vmId, requestedDCs);
                return;
            }
        }
        Log.printLine(CloudSim.clock() + ": " + getName() + ": None of the datacenters are available for VM #" + vmId);
    }

    @Override
    protected void createGroupVm(Set<Integer> g) {
        for(int vmId : g){
            createSingleVm(vmId);
        }
    }
}
