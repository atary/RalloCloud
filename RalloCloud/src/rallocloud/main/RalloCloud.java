package rallocloud.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import org.cloudbus.cloudsim.*;
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
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.NetworkTopologyPublic;
import org.cloudbus.cloudsim.provisioners.BwProvisionerNetworked;

/**
 *
 * @author Atakan
 */
public class RalloCloud {

    private static int vmid = 0;
    private static int cloudletid = 0;
    private static final HashSet<DatacenterBrokerStrategy> brokerSet = new HashSet<>();
    public static PrintWriter out = null;
    public static String strategy;
    private static int vmRAM;
    private static int vmBW;

    private enum topologyType {

        LINEAR, CIRCULAR, COMPLETE, STAR
    }

    public static void main(String[] args) {

        try {
            boolean printList = true; //Human readable?
            vmRAM = 1;
            vmBW = 1;
            strategy = "LNF";
            if (args.length > 0) {
                printList = false;
                if (args.length > 1) {
                    vmRAM = Integer.parseInt(args[1]);
                }
                if (args.length > 2) {
                    vmBW = Integer.parseInt(args[2]);
                }
                strategy = args[0];
                out = new PrintWriter(new BufferedWriter(new FileWriter("dist/out/" + vmRAM + "-" + vmBW + ".txt", true)));
                out.println(strategy);
            }

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

            NetworkTopologyPublic.buildNetworkTopology("C:\\Users\\Atakan\\Documents\\NetBeansProjects\\RalloCloud\\RalloCloud\\data\\federica.brite");
            NetworkTopologyPublic.setNextIdx(NetworkTopologyPublic.getBwMatrix().length);

            ArrayList<Datacenter> dcList = new ArrayList<>();

            for (int i = 0; i < 14; i++) {
                Datacenter dc = createDatacenter(labels.get(i), 1538*4, 65536, 4000000, 4000);
                dcList.add(dc);
                NetworkTopologyPublic.mapNodes(dc, i);
            }

            Datacenter dc = createDatacenter(labels.get(14), 0, 0, 0, 0); //Empty datacenter for nordunet
            dcList.add(dc);
            NetworkTopologyPublic.mapNodes(dc, 14);

            int i = 0;
            for (Datacenter d : dcList) {
                String name = "BROKER" + i;
                i++;
                labels.add(name);
                createBroker(dcList, name, d.getId());
            }

            for (DatacenterBrokerStrategy bs : brokerSet) {
                for (i = 0; i < bs.getPopulation() / 5; i++) {
                    createVmGroup(bs, 3, 50, topologyType.LINEAR);
                    createVmGroup(bs, 2, 50, topologyType.COMPLETE);
                }
            }

            NetworkTopologyPublic.setBrokerSet(brokerSet);

            //Visualizer.emptyTopology(MyNetworkTopology.getBwMatrix(), labels);
            //START
            CloudSim.startSimulation();

            List<Cloudlet> clList = new ArrayList<>();
            ArrayList<List<Cloudlet>> clSepList = new ArrayList<>();
            HashMap<Integer, Integer> VmsToDatacentersMap = new HashMap<>();
            for (DatacenterBrokerStrategy bs : brokerSet) {
                if (bs.getCloudletSubmittedList().isEmpty()) {
                    continue;
                }
                clList.addAll(bs.getCloudletSubmittedList());
                clSepList.add(bs.getCloudletSubmittedList());
                VmsToDatacentersMap.putAll(bs.getVmsToDatacentersMap());
            }

            CloudSim.stopSimulation();
            //STOP            

            System.out.println("");
            printCloudletList(clList, printList);
            if (printList) {
                DecimalFormat dft = new DecimalFormat("###.##");
                System.out.println("Distribution Factor (DSF)\t: \t" + dft.format(Statistician.getDSF(clSepList)));
                System.out.println("Load Balance (LDB)\t\t: \t" + dft.format(Statistician.getLDB(clList, dcList)));
                printVmList(VmsToDatacentersMap, labels);
            } else {
                out.println(Statistician.getDSF(clSepList));
                out.println(Statistician.getLDB(clList, dcList));
                out.println("");
                out.close();
            }
            System.out.println("");

        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Double[][] createVmGroup(DatacenterBrokerStrategy broker, int count, double time, topologyType type) {
        int brokerId = broker.getId();

        PoissonDistribution pd = new PoissonDistribution(count); //for VM count
        count = pd.sample();
        UniformRealDistribution urd = new UniformRealDistribution(0, time); //For request time
        time = urd.sample();

        if (count == 0) {
            return null;
        }

        ArrayList<Integer> group = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int mips = 50;
            long size = 10000; //image size (MB)
            int ram = 1024 * vmRAM; //vm memory (MB)
            long bw = 50 * vmBW;
            int pesNumber = 1; //number of cpus
            String vmm = "Xen"; //VMM name

            Vm virtualMachine = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            group.add(vmid);
            broker.getVmList().add(virtualMachine);
            broker.getAllVmList().add(virtualMachine);

            long length = 150;
            long fileSize = 15;
            long outputSize = 15;
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
        List<Host> hostList = new ArrayList<>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<>();

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        //4. Create Host with its id and list of PEs and add them to the list of machines
        int hostId = 0;

        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerNetworked(bw, -1),
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
        double cost = 1;             // the cost of using processing in this resource
        double costPerMem = 0.05;		// the cost of using memory in this resource
        double costPerStorage = 0.001;	// the cost of using storage in this resource
        double costPerBw = 0.0;			// the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<>();	//we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        // 6. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            //e.printStackTrace();
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
                    + "DC Name" + indent + "DC ID" + indent + "VM ID" + indent + "Durat" + indent + "Time"
                    + indent + "Start" + indent + "Finish" + indent + "Broker" + indent + "Group" + indent + "Cost");
        }
        double AUL = 0;
        double MUL = 0;
        double JRT = 0;
        double JCT = 0;
        double CST = 0;
        double AVC = 0;
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = clList.get(i);

            DatacenterBrokerStrategy broker = null;

            for (DatacenterBrokerStrategy b : brokerSet) {
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
                        + indent + dft.format(cloudlet.getFinishTime()) + indent + cloudlet.getUserId() + indent + group + indent + cloudlet.getCostPerSec());
            }
            AUL += cloudlet.getExecStartTime();
            JRT += cloudlet.getActualCPUTime();
            JCT += (cloudlet.getFinishTime()-time);
            CST += cloudlet.getCostPerSec() * cloudlet.getActualCPUTime();
            AVC += cloudlet.getCostPerSec();
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
            System.out.println("Total Cost (CST)\t\t: \t" + dft.format(CST));
            System.out.println("Average Cost (AVC)\t\t: \t" + dft.format(AVC / size));
        } else {
            out.println(AUL / size);
            out.println(MUL);
            out.println(Statistician.getADL());
            out.println(Statistician.getMDL());
            out.println(JRT / size);
            out.println(JCT / size);
            out.println(Statistician.getTRP(clList));
            out.println(Statistician.getRJR() * 100);
            out.println(CST);
            out.println(AVC / size);
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

    private static DatacenterBrokerStrategy createBroker(ArrayList<Datacenter> dcList, String name, int dcId) {

        try {
            DatacenterBrokerStrategy broker = null;

            switch (strategy) {
                case "AFF":
                    broker = new AFFDatacenterBroker(name);
                    break;
                case "ANF":
                    broker = new ANFDatacenterBroker(name);
                    break;
                case "LBG":
                    broker = new LBGDatacenterBroker(name);
                    break;
                case "LFF":
                    broker = new LFFDatacenterBroker(name);
                    break;
                case "LNF":
                    broker = new LNFDatacenterBroker(name);
                    break;
                case "RAN":
                    broker = new RANDatacenterBroker(name);
                    break;
                case "TBF":
                    broker = new TBFDatacenterBroker(name);
                    break;
                default:
                    System.exit(1);
                    break;
            }

            int[] pops = {-1, -1, 61, 81, 11, 30, 10, 6, 5, 23, 5, 10, 10, 9, 23, 8, 6};

            broker.setDatacenterList(dcList);
            NetworkTopologyPublic.addLink(dcId, broker.getId(), 10.0, 0.1);

            broker.setPopulation(pops[dcId]);

            brokerSet.add(broker);

            return broker;
        } catch (Exception ex) {
            Logger.getLogger(RalloCloud.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
