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
import java.util.Random;
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
public abstract class DatacenterBrokerStrategy extends DatacenterBroker {

    //public static int i;
    protected ArrayList<Datacenter> datacenterList;

    protected static ArrayList<Vm> AllVmList = new ArrayList<>(); //All VMs from all brokers

    protected Map<List<Integer>, Double[][]> VmGroups;

    protected Map<List<Integer>, Double> GroupTimes;

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

    public Map<List<Integer>, Double> getGroupTimes() {
        return GroupTimes;
    }

    public DatacenterBrokerStrategy(String name) throws Exception {
        super(name);
        Log.disable();
        VmGroups = new HashMap<>();
        GroupTimes = new HashMap<>();
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
    protected void processResourceCharacteristicsRequest(SimEvent ev) {
        super.processResourceCharacteristicsRequest(ev);
        Collections.shuffle(datacenterIdsList, new Random(getId())); //Each broker has a differently ordered list.
    }

    @Override
    protected void processVmCreate(SimEvent ev) {
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        int vmId = data[1];
        int result = data[2];
        Statistician.trial();
        if (result == CloudSimTags.TRUE) {
            getVmsToDatacentersMap().put(vmId, datacenterId);
            getVmsCreatedList().add(VmList.getById(getVmList(), vmId));

            Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId + " has been created in " + CloudSim.getEntityName(datacenterId) + " (" + datacenterId + ")");

            Datacenter dc = VmList.getById(getVmList(), vmId).getHost().getDatacenter();

            double ramCap = 0;
            double ramUse = 0;

            for (int i = 0; i < dc.getHostList().size(); i++) {
                ramCap = dc.getHostList().get(i).getRam();
            }

            for (Vm v : AllVmList) {
                //if (vmsToDatacentersMap.get(v.getId()) == di) {
                if (v.getHost() != null && v.getHost().getDatacenter().getId() == datacenterId) {
                    ramUse += v.getRam();
                }
            }

            double util = ramUse / ramCap;

            dc.getCharacteristics().setCostPerSecond(util);

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
                Log.printLine(CloudSim.clock() + ": " + getName() + ": Group of VM #" + vmId + " (" + group + ") is ready.");
                Double[][] t = VmGroups.get(group);
                for (int v : group) {
                    submitCloudlets(v, group, t);
                }
            } else {
            }

        } else {
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId + " failed in " + CloudSim.getEntityName(datacenterId) + " (" + datacenterId + ")");
            Statistician.rejected();
            if (CloudSim.clock() > 1000) {
                System.exit(1);
            }
            createSingleVm(vmId);
        }
        incrementVmsAcks();
    }

    protected abstract void createSingleVm(int id);

    protected abstract void createGroupVm(List<Integer> g, Double[][] t);

    private void submitCloudlets(int vmId, List<Integer> group, Double[][] top) {
        Vm vm = VmList.getById(getVmsCreatedList(), vmId);
        for (Cloudlet cloudlet : getCloudletList()) {
            if (cloudlet.getVmId() == vmId) {
                cloudlet.setCloudletLength(cloudlet.getCloudletLength() + calculateExtraLength(cloudlet, vmId, group, top));
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

    private long calculateExtraLength(Cloudlet c, int vmId, List<Integer> group, Double[][] top) {
        int topIndex = group.indexOf(vmId);

        Datacenter dc = VmList.getById(getVmList(), vmId).getHost().getDatacenter();

        ArrayList<Integer> in = new ArrayList<>();
        ArrayList<Integer> out = new ArrayList<>();

        for (int i = 0; i < group.size(); i++) {
            if (top[i][topIndex] > 0) {
                in.add(group.get(i));
            }
            if (top[topIndex][i] > 0) {
                out.add(group.get(i));
            }
        }

        double extraIn = 0;
        double extraOut = 0;

        for (int i : in) {
            Vm vm = VmList.getById(getVmList(), i);
            int dcId = vm.getHost().getDatacenter().getId();
            double delay = MyNetworkTopology.getDelay(dcId, dc.getId());
            Statistician.addDelay(delay);
            if (delay > extraIn) {
                extraIn = delay;
            }
        }
        for (int i : out) {
            Vm vm = VmList.getById(getVmList(), i);
            int dcId = vm.getHost().getDatacenter().getId();
            double delay = MyNetworkTopology.getDelay(dc.getId(), dcId);
            Statistician.addDelay(delay);
            if (delay > extraOut) {
                extraOut = delay;
            }
        }

        return (long) (extraIn * c.getCloudletFileSize() + extraOut * c.getCloudletOutputSize());
    }

    @Override
    public void shutdownEntity() {
        Log.printLine(getName() + " is shutting down...");
        Statistician.setEndTime(CloudSim.clock());
    }

    protected void sendAt(int entityId, double time, int cloudSimTag, Object data) {
        if (entityId < 0) {
            return;
        }

        double delay = time - CloudSim.clock();

        // if delay is -ve, then it doesn't make sense. So resets to 0.0
        if (delay < 0) {
            delay = 0;
        }

        if (entityId < 0) {
            Log.printLine(getName() + ".send(): Error - " + "invalid entity id " + entityId);
            return;
        }

        int srcId = getId();
        if (entityId != srcId) {// does not delay self messages
            if (MyNetworkTopology.getDelay(srcId, entityId) > delay) {
                delay = MyNetworkTopology.getDelay(srcId, entityId);
            }
        }

        schedule(entityId, delay, cloudSimTag, data);
    }

    private List<Integer> getVmGroup(int vmId) {
        for (List<Integer> g : VmGroups.keySet()) {
            if (g.contains(vmId)) {
                return g;
            }
        }
        return null;
    }

    protected double getVmTime(int vmId) {
        List<Integer> l = getVmGroup(vmId);
        //if (GroupTimes.containsKey(l)) {
        return GroupTimes.get(l);
        /*} else {
         return 0;
         }*/
    }

}
