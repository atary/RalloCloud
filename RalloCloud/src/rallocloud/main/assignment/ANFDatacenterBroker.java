/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main.assignment;

import java.util.HashSet;
import java.util.Set;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

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
        createGroupVm(hs, new Double[0][0]);
    }

    @Override
    protected void createGroupVm(Set<Integer> g, Double[][] t) {
        for (int vmId : g) {
            Vm vm = VmList.getById(getVmList(), vmId);
            setVmsRequested(getVmsRequested() + 1);
            int datacenterId = datacenterIdsList.get(i++ % datacenterIdsList.size());
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vmId + " in " + CloudSim.getEntityName(datacenterId) + " (" + datacenterId + ")");
            sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
        }
    }
}
