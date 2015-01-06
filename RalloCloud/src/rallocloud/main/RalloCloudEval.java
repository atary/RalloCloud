/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import rallocloud.main.assignment.*;

/**
 *
 * @author Atakan
 */
public class RalloCloudEval {

    private static int vmid;
    private static int cloudletid;
    private static HashSet<BrokerStrategy> brokerSet;

    private enum topologyType {

        LINEAR, CIRCULAR, COMPLETE, STAR
    }

    public static void main(String[] args) {

        ArrayList<String> labels = new ArrayList<>();
        labels.add("GARR");
        labels.add("DFN");
        labels.add("CESNET");
        labels.add("PSNC");
        labels.add("FCCN");
        labels.add("GRNET");
        labels.add("HEANET");
        labels.add("I2CAT");
        labels.add("ICCS");
        labels.add("KTH");
        labels.add("NIIF");
        labels.add("PSNC-2");
        labels.add("RedIRIS");
        labels.add("SWITCH");
        labels.add("NORDUNET");

        for (int index = 0; index < 6; index++) {
            try {
                vmid = 0;
                cloudletid = 0;
                brokerSet = new HashSet<>();

                int num_user = 15;
                Calendar calendar = Calendar.getInstance();
                boolean trace_flag = false;

                CloudSim.init(num_user, calendar, trace_flag);

                MyNetworkTopology.buildNetworkTopology("C:\\Users\\Atakan\\Documents\\NetBeansProjects\\RalloCloud\\RalloCloud\\data\\federica.brite");
                MyNetworkTopology.setNextIdx(MyNetworkTopology.getBwMatrix().length);

                ArrayList<Datacenter> dcList = new ArrayList<>();

                for (int i = 0; i < 14; i++) {
                    Datacenter dc = createDatacenter(labels.get(i), 14000, 16384, 1000000, 1000);
                    dcList.add(dc);
                    MyNetworkTopology.mapNode(dc.getId(), i);
                }

                Datacenter dc = createDatacenter(labels.get(14), 0, 0, 0, 0); //Empty datacenter for nordunet
                dcList.add(dc);
                MyNetworkTopology.mapNode(dc.getId(), 14);

                int i = 0;
                for (Datacenter d : dcList) {
                    String name = "BROKER" + i;
                    i++;
                    labels.add(name);
                    createBroker(dcList, name, d.getId(), index);
                }

                for (BrokerStrategy bs : brokerSet) {
                    createLoad(bs, 3, topologyType.COMPLETE);
                    createLoad(bs, 2, topologyType.COMPLETE);
                }

                //Visualizer.emptyTopology(MyNetworkTopology.getBwMatrix(), labels);
                //START
                CloudSim.startSimulation();

                List<Cloudlet> clList = new ArrayList<>();
                ArrayList<List<Cloudlet>> clSepList = new ArrayList<>();
                HashMap<Integer, Integer> VmsToDatacentersMap = new HashMap<>();
                for (BrokerStrategy bs : brokerSet) {
                    if (bs.getCloudletSubmittedList().isEmpty()) {
                        continue;
                    }
                    clList.addAll(bs.getCloudletSubmittedList());
                    clSepList.add(bs.getCloudletSubmittedList());
                    VmsToDatacentersMap.putAll(bs.getVmsToDatacentersMap());
                }

                CloudSim.stopSimulation();
                //STOP

                System.out.println(brokerSet.iterator().next().getClass().getSimpleName());

                printCloudletList(clList);
                System.out.println(Statistician.getDSF(clSepList));
                System.out.println(Statistician.getLDB(clList, dcList));
                //printVmList(VmsToDatacentersMap, labels);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("The simulation has been terminated due to an unexpected error");
            }
        }
    }

    private static Double[][] createLoad(BrokerStrategy broker, int count, topologyType type) {
        int brokerId = broker.getId();
        ArrayList<Integer> group = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int mips = 3000;
            long size = 10000; //image size (MB)
            int ram = 512; //vm memory (MB)
            long bw = 100;
            int pesNumber = 1; //number of cpus
            String vmm = "Xen"; //VMM name

            Vm virtualMachine = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            group.add(vmid);
            broker.getVmList().add(virtualMachine);
            broker.getAllVmList().add(virtualMachine);

            long length = 30000;
            long fileSize = 3000;
            long outputSize = 3000;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            Cloudlet application = new Cloudlet(cloudletid, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            application.setUserId(brokerId);

            broker.getCloudletList().add(application);

            broker.bindCloudletToVm(application.getCloudletId(), virtualMachine.getId());

            vmid++;
            cloudletid++;
        }

        Double[][] topology = new Double[count][count];

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                topology[i][j] = 0.0;
            }
        }

        if (type == topologyType.LINEAR) {
            for (int i = 0; i < count - 1; i++) {
                topology[i][i + 1] = 1.0;
                topology[i + 1][i] = 1.0;
            }
        } else if (type == topologyType.COMPLETE) {
            for (int i = 0; i < count; i++) {
                for (int j = 0; j < count; j++) {
                    if (i != j) {
                        topology[i][j] = 1.0;
                    }
                }
            }
        } else if (type == topologyType.CIRCULAR) {
            for (int i = 0; i < count - 1; i++) {
                topology[i][i + 1] = 1.0;
                topology[i + 1][i] = 1.0;
            }
            topology[count - 1][0] = 1.0;
            topology[0][count - 1] = 1.0;
        } else if (type == topologyType.STAR) {
            for (int i = 1; i < count; i++) {
                topology[i][0] = 1.0;
                topology[0][i] = 1.0;
            }
        }

        broker.getVmGroups().put(group, topology);

        return topology;
    }

    private static Datacenter createDatacenter(String name, int mips, int ram, long storage, int bw) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        //4. Create Host with its id and list of PEs and add them to the list of machines
        int hostId = 0;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        ); // This is our machine

        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Prints the Cloudlet objects
     *
     * @param clList list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> clList) {
        int size = clList.size();
        Cloudlet cloudlet;

        /*String indent = "\t\t";
         System.out.println("\n========== CLOUDLETS ==========");
         System.out.println("CL ID" + indent + "STATUS" + indent
         + "DC Name" + indent + "DC ID" + indent + "VM ID" + indent + "Time" + indent + "Start" + indent + "Finish" + indent + "Broker ID");*/
        double AUL = 0;
        double MUL = 0;
        double JRT = 0;
        double JCT = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = clList.get(i);
            /*System.out.print(cloudlet.getCloudletId() + indent);
             System.out.print(cloudlet.getCloudletStatus() == Cloudlet.SUCCESS ? "SUCCESS" : "OTHER");
             System.out.println(indent + cloudlet.getResourceName(cloudlet.getResourceId()) + indent + cloudlet.getResourceId() + indent + cloudlet.getVmId()
             + indent + dft.format(cloudlet.getActualCPUTime()) + indent + dft.format(cloudlet.getExecStartTime())
             + indent + dft.format(cloudlet.getFinishTime()) + indent + cloudlet.getUserId());*/
            AUL += cloudlet.getExecStartTime();
            JRT += cloudlet.getActualCPUTime();
            JCT += cloudlet.getFinishTime();
            if (cloudlet.getExecStartTime() > MUL) {
                MUL = cloudlet.getExecStartTime();
            }
        }
        //System.out.println("\n=========== METRICS ===========");
        System.out.println(AUL / size);
        System.out.println(MUL);
        System.out.println(Statistician.getADL());
        System.out.println(Statistician.getMDL());
        System.out.println(JRT / size);
        System.out.println(JCT / size);
        System.out.println(Statistician.getTRP(clList));
        System.out.println(Statistician.getRJR() * 100);
    }

    private static void printVmList(Map<Integer, Integer> m, ArrayList<String> l) {
        String indent = "\t\t";
        System.out.println();
        System.out.println("========== VMs ==========");
        System.out.println("VM ID" + indent + "DC Name" + indent + "DC ID");
        for (int vmId : m.keySet()) {
            int dcId = m.get(vmId);
            System.out.println(vmId + indent + l.get(dcId - 2) + indent + dcId);
        }
    }

    private static BrokerStrategy createBroker(ArrayList<Datacenter> dcList, String name, int dcId, int index) {

        try {
            BrokerStrategy broker;

            switch (index) {
                case 0:
                    broker = new AFFDatacenterBroker(name);
                    break;
                case 1:
                    broker = new ANFDatacenterBroker(name);
                    break;
                case 2:
                    broker = new LBGDatacenterBroker(name);
                    break;
                case 3:
                    broker = new LFFDatacenterBroker(name);
                    break;
                case 4:
                    broker = new RANDatacenterBroker(name);
                    break;
                case 5:
                    broker = new TBFDatacenterBroker(name);
                    break;
                default:
                    broker = new RANDatacenterBroker(name);
            }

            broker.setDatacenterList(dcList);
            MyNetworkTopology.addLink(dcId, broker.getId(), 10.0, 0.1);

            //System.out.println(broker.getClass().getSimpleName() + " is created");
            brokerSet.add(broker);

            return broker;
        } catch (Exception ex) {
            Logger.getLogger(RalloCloudEval.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}