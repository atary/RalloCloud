/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.python.google.common.collect.HashBiMap;
import rallocloud.main.assignment.*;

/**
 *
 * @author Atakan
 */
public class RalloCloud {

    public static void main(String[] args) {

        try {
            Statistician.setSize(0);
            HashBiMap<Integer, Integer> simGrphMap = HashBiMap.create(); //Key: cloudsim id, Value: grph id

            int num_user = 2;
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
            labels.add("BROKER1");
            labels.add("BROKER2");

            MyNetworkTopology.buildNetworkTopology("C:\\Users\\Atakan\\Documents\\NetBeansProjects\\RalloCloud\\RalloCloud\\data\\federica.brite");

            ArrayList<Datacenter> dcList = new ArrayList<>();

            for (int i = 0; i < 14; i++) {
                Datacenter dc = createDatacenter(labels.get(i), 3000, 16384, 1000000, 1000);
                dcList.add(dc);
                MyNetworkTopology.mapNode(dc.getId(), i);
                simGrphMap.put(dc.getId(), i);
            }
            
            Datacenter dc = createDatacenter(labels.get(14), 0, 0, 0, 0); //Empty datacenter for nordunet
            dcList.add(dc);
            MyNetworkTopology.mapNode(dc.getId(), 14);
            simGrphMap.put(dc.getId(), 14);

            BrokerStrategy broker1 = createBroker(dcList);
            BrokerStrategy broker2 = createBroker(dcList);

            double[][] loadTopology1 = createLoad(broker1, 3);
            double[][] loadTopology2 = createLoad(broker2, 2);

            //Visualizer.emptyTopology(loadTopology1, new ArrayList<String>());

            MyNetworkTopology.mapNode(broker1.getId(), 15);
            simGrphMap.put(broker1.getId(), 15);
            MyNetworkTopology.mapNode(broker2.getId(), 16);
            simGrphMap.put(broker2.getId(), 16);

            ArrayList<Integer> brokers = new ArrayList<>();
            brokers.add(15);
            brokers.add(16);

            ArrayList<Integer> cores = new ArrayList<>();
            cores.add(0);
            cores.add(1);
            cores.add(2);
            cores.add(3);

            ArrayList<Integer> excluded = new ArrayList<>();
            excluded.add(14);

            //Visualizer.emptyTopology(MyNetworkTopology.getBwMatrix(), labels, brokers, cores, excluded);
            
            CloudSim.startSimulation();

            List<Cloudlet> clList1 = broker1.getCloudletSubmittedList();
            List<Cloudlet> clList2 = broker2.getCloudletSubmittedList();
            
            broker1.getVmList().get(0).getCurrentAllocatedBw();
            
            CloudSim.stopSimulation();
            
            List<Cloudlet> clList = new ArrayList<Cloudlet>(clList1);
            clList.addAll(clList2);
            
            printCloudletList(clList);
            
            //BEGIN DSF CALC
            
            HashSet<Integer> dcs1= new HashSet<Integer>();
            for(Cloudlet c : clList1){
                dcs1.add(c.getResourceId());
            }
            HashSet<Integer> dcs2= new HashSet<Integer>();
            for(Cloudlet c : clList2){
                dcs2.add(c.getResourceId());
            }
            
            DecimalFormat dft = new DecimalFormat("###.##");
            double DSF = ((double)dcs1.size() / (double)clList1.size()) + ((double)dcs2.size() / (double)clList2.size());
            System.out.println("Distribution Factor (DSF)\t: \t" + dft.format(DSF/2.0));
            
            //END DSF CALC
            
            //printVmList(vmList);

            ArrayList<Integer> dcIdList1 = new ArrayList<>();
            ArrayList<Integer> dcIdList2 = new ArrayList<>();

            for (Cloudlet c : clList1) {
                dcIdList1.add(simGrphMap.get(c.getResourceId()));
            }
            for (Cloudlet c : clList2) {
                dcIdList2.add(simGrphMap.get(c.getResourceId()));
            }

            ArrayList<Integer> b1 = new ArrayList<>();
            b1.add(simGrphMap.get(broker1.getId()));
            ArrayList<Integer> b2 = new ArrayList<>();
            b2.add(simGrphMap.get(broker2.getId()));

            Visualizer.assignedTopology(MyNetworkTopology.getBwMatrix(), labels, b1, b2, dcIdList1, dcIdList2);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("The simulation has been terminated due to an unexpected error");
        }
    }

    private static double[][] createLoad(BrokerStrategy broker, int count) {
        int brokerId = broker.getId();
        int vmid = 0;
        int cloudletid = 0;
        HashSet<Integer> group = new HashSet<>();
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

            long length = 30000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            Cloudlet application = new Cloudlet(cloudletid, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            application.setUserId(brokerId);

            broker.getCloudletList().add(application);

            broker.bindCloudletToVm(application.getCloudletId(), virtualMachine.getId());

            vmid++;
            cloudletid++;
        }
        broker.getVmGroups().add(group);
        
        double[][] topology = new double[count][count];

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                topology[i][j] = 0.0;
            }
        }

        for (int i = 0; i < count - 1; i++) {
            topology[i][i + 1] = 1.0;
            topology[i + 1][i] = 1.0;
        }

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

        String indent = "\t\t";
        System.out.println();
        System.out.println("========== CLOUDLETS ==========");
        System.out.println("CL ID" + indent + "STATUS" + indent
                + "DC Name" + indent + "VM ID" + indent + "Time" + indent + "Start" + indent + "Finish" + indent + "Broker ID");
        double AUL = 0;
        double MUL = 0;
        double JRT = 0;
        double JCT = 0;
        double UTR = 0;
        double UTRtime = 15;
        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = clList.get(i);
            System.out.print(cloudlet.getCloudletId() + indent);
            System.out.print(cloudlet.getCloudletStatus() == Cloudlet.SUCCESS ? "SUCCESS" : "OTHER");
            System.out.println(indent + cloudlet.getResourceName(cloudlet.getResourceId()) + cloudlet.getResourceId() + indent + cloudlet.getVmId()
                    + indent + dft.format(cloudlet.getActualCPUTime()) + indent + dft.format(cloudlet.getExecStartTime())
                    + indent + dft.format(cloudlet.getFinishTime()) + indent + cloudlet.getUserId());
            AUL += cloudlet.getExecStartTime();
            JRT += 1000000 * cloudlet.getActualCPUTime() / cloudlet.getCloudletLength();
            JCT += cloudlet.getFinishTime();
            if (cloudlet.getExecStartTime() > MUL) {
                MUL = cloudlet.getExecStartTime();
            }
        }

        System.out.println();
        System.out.println("=========== METRICS ===========");
        System.out.println("Average User Latency (AUL)\t: \t" + dft.format(AUL / size) + " s");
        System.out.println("Maximum User Latency (MUL)\t: \t" + dft.format(MUL) + " s");
        System.out.println("Rejection Rate (RJR)\t\t: \t" + dft.format(Statistician.getRJR() * 100) + "%");
        System.out.println("Job Run Time (JRT)\t\t: \t" + dft.format(JRT / size) + " s/1M inst.");
        System.out.println("Job Completion Time (JCT)\t: \t" + dft.format(JCT / size) + " s");
    }

    private static void printVmList(List<Vm> list) {
        String indent = "\t\t";
        System.out.println();
        System.out.println("========== VMs ==========");
        System.out.println("VM ID" + indent + "User ID" + indent
                + "Host ID" + indent + "DC ID" + indent + "DC Name" + indent + "Start" + indent + "Finish" + indent + "User ID");
        for (Vm v : list) {
            System.out.println(v.getCurrentAllocatedRam());
            //System.out.println(v.getId() + indent + v.getUserId() + indent + v.getHost().getId() + indent + v.getHost().getDatacenter().getId() + indent + v.getHost().getDatacenter().getName());
        }
    }

    private static BrokerStrategy createBroker(ArrayList<Datacenter> dcList) {

        BrokerStrategy broker;
        try {
            broker = new LBGDatacenterBroker("Broker");
            broker.setDatacenterList(dcList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }
}
