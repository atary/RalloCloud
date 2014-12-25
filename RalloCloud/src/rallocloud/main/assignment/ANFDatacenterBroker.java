/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main.assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;

/**
 * Arbitrary next fit
 *
 * @author Atakan
 */
public class ANFDatacenterBroker extends BrokerStrategy {

    public static int i;

    public ANFDatacenterBroker(String name) throws Exception {
        super(name);
        i = 0;
    }

    @Override
    protected void createSingleVm(int vmId) {
        HashSet<Integer> hs = new HashSet<>();
        hs.add(vmId);
        createGroupVm(hs);
    }

    @Override
    protected void createGroupVm(Set<Integer> g) {
        for (int vmId : g) {
            Vm vm = null;
            for (Vm v : vmList) {
                if (v.getId() == vmId) {
                    vm = v;
                }
            }

            int datacenterId = datacenterIdsList.get(i++ % datacenterIdsList.size());
            String datacenterName = CloudSim.getEntityName(datacenterId);
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
                    + " in " + datacenterName);
            sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
        }
    }
}
