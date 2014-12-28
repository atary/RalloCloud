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
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;
import rallocloud.main.Statistician;

/**
 * Base strategy that provide metric calculation
 *
 * @author Atakan
 */
public abstract class BrokerStrategy extends org.cloudbus.cloudsim.DatacenterBroker {

    //public static int i;
    protected ArrayList<Datacenter> datacenterList;

    protected static ArrayList<Vm> AllVmList = new ArrayList<>(); //All VMs from all brokers

    protected Map<List<Integer>, Double[][]> VmGroups;

    public ArrayList<Vm> getAllVmList() {
        return AllVmList;
    }

    public ArrayList<Datacenter> getDatacenterList() {
        return datacenterList;
    }

    public void setDatacenterList(ArrayList<Datacenter> datacenterList) {
        this.datacenterList = datacenterList;
    }

    public Map<List<Integer>, Double[][]> getVmGroups() {
        return VmGroups;
    }

    public BrokerStrategy(String name) throws Exception {
        super(name);
        VmGroups = new HashMap<>();
    }

    @Override
    public Map<Integer, Integer> getVmsToDatacentersMap() {
        return vmsToDatacentersMap;
    }

    @Override
    protected void processResourceCharacteristics(SimEvent ev) {
        DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
        getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);
        if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
            //setDatacenterRequestedIdsList(new ArrayList<Integer>());
            for (List<Integer> g : VmGroups.keySet()) {
                createGroupVm(g, VmGroups.get(g));
            }
        }
    }

    @Override
    protected void processVmCreate(SimEvent ev) { //For statistician class
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];
        Statistician.trial();
        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().put(vmId, datacenterId);
            getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
            Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId + " has been created in " + CloudSim.getEntityName(datacenterId) + " (" + datacenterId + ")");

            boolean ready = true;
            List<Integer> group = null;
            for (List<Integer> g : VmGroups.keySet()) {
                if (g.contains(vmId)) {
                    group = g;
                    for (int v : group) {
                        Vm vm = VmList.getById(getVmList(), v);
                        if (!getVmsCreatedList().contains(vm)) {
                            ready = false;
                            break;
                        }
                    }
                    break;
                }
            }
            if (ready) {
                System.out.println("GROUP IS READY!");
                for (int v : group) {
                    submitCloudlets(v);
                }
            }
            else System.out.println("GROUP IS NOT READY!");

        } else {
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId + " failed in " + CloudSim.getEntityName(datacenterId) + " (" + datacenterId + ")");
            Statistician.rejected();
            createSingleVm(vmId);
        }
        incrementVmsAcks();
    }

    protected abstract void createSingleVm(int id);

    protected abstract void createGroupVm(List<Integer> g, Double[][] t);

    private void submitCloudlets(int vmId) {
        Vm vm = VmList.getById(getVmsCreatedList(), vmId);
        for (Cloudlet cloudlet : getCloudletList()) {
            if (cloudlet.getVmId() == vmId) {
                
                //TODO: Hangi VM'lere yönlü olarak bağlı olduğu belirlenecek. Length değeri artırılacak. Artış miktarı bağlı vm'e olan delay ve input/output size ile orantılı olacak.
                
                Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet " + cloudlet.getCloudletId() + " to VM #" + vm.getId() + " in " + vm.getHost().getDatacenter().getName() + " (" + vm.getHost().getDatacenter().getId() + ")");
                sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
                //cloudletsSubmitted++;
                getCloudletSubmittedList().add(cloudlet);
            }
        }
        // remove submitted cloudlets from waiting list
        for (Cloudlet cloudlet : getCloudletSubmittedList()) {
            getCloudletList().remove(cloudlet);
        }
    }

    @Override
    protected void processCloudletReturn(SimEvent ev) {
        Cloudlet cloudlet = (Cloudlet) ev.getData();
        getCloudletReceivedList().add(cloudlet);
        Vm vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
        Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId() + " received by VM #" + vm.getId() + " in " + vm.getHost().getDatacenter().getName() + " (" + vm.getHost().getDatacenter().getId() + ")");

        destroyVm(cloudlet.getVmId());
        //cloudletsSubmitted--;
    }

    private void destroyVm(int vmId) {
        Vm vm = VmList.getById(getVmsCreatedList(), vmId);
        Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vmId + " in " + vm.getHost().getDatacenter().getName() + " (" + vm.getHost().getDatacenter().getId() + ")");
        sendNow(getVmsToDatacentersMap().get(vmId), CloudSimTags.VM_DESTROY, vm);
    }

}
