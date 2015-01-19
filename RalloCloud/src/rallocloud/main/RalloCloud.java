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
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
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
public class RalloCloud {

    private static int vmid = 0;
    private static int cloudletid = 0;
    private static HashSet<BrokerStrategy> brokerSet = new HashSet<>();

    private enum topologyType {

        LINEAR, CIRCULAR, COMPLETE, STAR
    }

    public static void main(String[] args) {

        try {
            int num_user = 15;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            CloudSim.init(num_user, calendar, trace_flag);

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
                createBroker(dcList, name, d.getId());
            }

            for (BrokerStrategy bs : brokerSet) {
                createVmGroup(bs, 3, 200, topologyType.LINEAR);
                createVmGroup(bs, 2, 200, topologyType.COMPLETE);
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

            boolean printList = false; //Human readable?

            System.out.println("");
            printCloudletList(clList, printList);
            if (printList) {
                DecimalFormat dft = new DecimalFormat("###.##");
                System.out.println("Distribution Factor (DSF)\t: \t" + dft.format(Statistician.getDSF(clSepList)));
                System.out.println("Load Balance (LDB)\t\t: \t" + dft.format(Statistician.getLDB(clList, dcList)));
                printVmList(VmsToDatacentersMap, labels);
            } else {
                System.out.println(Statistician.getDSF(clSepList));
                System.out.println(Statistician.getLDB(clList, dcList));
            }
            System.out.println("");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Double[][] createVmGroup(BrokerStrategy broker, int count, double time, topologyType type) {
        int brokerId = broker.getId();

        PoissonDistribution pd = new PoissonDistribution(count); //for VM count
        count = pd.sample();
        UniformRealDistribution urd = new UniformRealDistribution(0, time); //For request time
        time = urd.sample();

        //System.out.println(broker.getId() + "\t" + time + "\t" + count);
        if (count == 0) {
            return null;
        }

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
        broker.getGroupTimes().put(group, time);

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
    private static void printCloudletList(List<Cloudlet> clList, boolean list) {
        int size = clList.size();
        Cloudlet cloudlet;

        String indent = "\t\t";
        if (list) {
            System.out.println("\n========== CLOUDLETS ==========");
            System.out.println("CL ID" + indent + "STATUS" + indent
                    + "DC Name" + indent + "DC ID" + indent + "VM ID" + indent + "Durat" + indent + "Time" + indent + "Start" + indent + "Finish" + indent + "Broker" + indent + "Group");
        }
        double AUL = 0;
        double MUL = 0;
        double JRT = 0;
        double JCT = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = clList.get(i);

            BrokerStrategy broker = null;

            for (BrokerStrategy b : brokerSet) {
                if (b.getId() == cloudlet.getUserId()) {
                    broker = b;
                    break;
                }
            }

            List<Integer> group = null;

            for (List<Integer> l : broker.getVmGroups().keySet()) {
                if (l.contains(cloudlet.getVmId())) {
                    group = l;
                }
            }

            double time = broker.getGroupTimes().get(group);
            if (list) {
                System.out.print(cloudlet.getCloudletId() + indent);
                System.out.print(cloudlet.getCloudletStatus() == Cloudlet.SUCCESS ? "SUCCESS" : "OTHER");
                System.out.println(indent + cloudlet.getResourceName(cloudlet.getResourceId()) + indent + cloudlet.getResourceId() + indent + cloudlet.getVmId()
                        + indent + dft.format(cloudlet.getActualCPUTime()) + indent + dft.format(time) + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + dft.format(cloudlet.getFinishTime()) + indent + cloudlet.getUserId() + indent + group);
            }
            AUL += cloudlet.getExecStartTime();
            JRT += cloudlet.getActualCPUTime();
            JCT += cloudlet.getFinishTime();
            if (cloudlet.getExecStartTime() > MUL) {
                MUL = cloudlet.getExecStartTime();
            }
        }
        if (list) {
            System.out.println("\n=========== METRICS ===========");
            System.out.println("Average User Latency (AUL)\t: \t" + dft.format(AUL / size) + "s");
            System.out.println("Maximum User Latency (MUL)\t: \t" + dft.format(MUL) + "s");
            System.out.println("Average Inter-DC Latency (ADL)\t: \t" + dft.format(Statistician.getADL()) + "s");
            System.out.println("Maximum Inter-DC Latency (MDL)\t: \t" + dft.format(Statistician.getMDL()) + "s");
            System.out.println("Job Run Time (JRT)\t\t: \t" + dft.format(JRT / size) + "s");
            System.out.println("Job Completion Time (JCT)\t: \t" + dft.format(JCT / size) + "s");
            System.out.println("Throughput (TRP)\t\t: \t" + dft.format(Statistician.getTRP(clList)) + " MIPS");
            System.out.println("Rejection Rate (RJR)\t\t: \t" + dft.format(Statistician.getRJR() * 100) + "%");
        } else {
            System.out.println(AUL / size);
            System.out.println(MUL);
            System.out.println(Statistician.getADL());
            System.out.println(Statistician.getMDL());
            System.out.println(JRT / size);
            System.out.println(JCT / size);
            System.out.println(Statistician.getTRP(clList));
            System.out.println(Statistician.getRJR() * 100);
        }
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

    private static BrokerStrategy createBroker(ArrayList<Datacenter> dcList, String name, int dcId) {

        try {
            BrokerStrategy broker;

            broker = new ANFDatacenterBroker(name);
            broker.setDatacenterList(dcList);
            MyNetworkTopology.addLink(dcId, broker.getId(), 10.0, 0.1);

            System.out.println(broker.getClass().getSimpleName() + " is created");

            brokerSet.add(broker);

            return broker;
        } catch (Exception ex) {
            Logger.getLogger(RalloCloud.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
