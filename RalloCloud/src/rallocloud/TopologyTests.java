/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rallocloud;

import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.NetworkTopology;
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

/**
 *
 * @author Atakan
 */
public class TopologyTests {

    private static List<Cloudlet> cloudletList;

    private static List<Vm> vmlist;

    public static void main(String[] args) {

            //System.out.println("START");

            try {
                int num_user = 1; 
                Calendar calendar = Calendar.getInstance();
                boolean trace_flag = false;

                CloudSim.init(num_user, calendar, trace_flag);

                Datacenter datacenterA = createDatacenter("A",150,16384,1000000,1000);
                Datacenter datacenterB = createDatacenter("B",1500,16384,1000000,1000);

                DatacenterBroker broker = createBroker();
                int brokerId = broker.getId();

                vmlist = new ArrayList<Vm>();

                int vmid = 0;
                int mips = 1500;
                long size = 10000; //image size (MB)
                int ram = 512; //vm memory (MB)
                long bw = 1000;
                int pesNumber = 1; //number of cpus
                String vmm = "Xen"; //VMM name

                Vm vm1 = new Vm(vmid, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

                vmlist.add(vm1);

                broker.submitVmList(vmlist);

                cloudletList = new ArrayList<Cloudlet>();

                int id = 0;
                long length = 30000;
                long fileSize = 300;
                long outputSize = 300;
                UtilizationModel utilizationModel = new UtilizationModelFull();

                Cloudlet cloudlet1 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
                cloudlet1.setUserId(brokerId);

                cloudletList.add(cloudlet1);
                
                broker.submitCloudletList(cloudletList);
                
                //broker.bindCloudletToVm(cloudlet2.getCloudletId(),vm2.getId());
                
                NetworkTopology.buildNetworkTopology("C:\\Users\\Atakan\\Documents\\NetBeansProjects\\RalloCloud\\RalloCloud\\data\\federica.brite");
                NetworkTopology.mapNode(broker.getId(), 15);
                NetworkTopology.mapNode(datacenterA.getId(), 1);
                NetworkTopology.mapNode(datacenterB.getId(), 12);

                //NetworkTopology.addLink(datacenterA.getId(), broker.getId(), 0.0, 0.0);
                //NetworkTopology.addLink(datacenterB.getId(), broker.getId(), 0.0, 1.1);
                //NetworkTopology.addLink(datacenterA.getId(), datacenterB.getId(), 0.0, 0.0);
                
                //System.out.println(Arrays.deepToString(NetworkTopology.getBwMatrix())); 
                
                CloudSim.startSimulation();

                List<Cloudlet> clList = broker.getCloudletReceivedList();
                
                List<Vm> vmList = broker.getVmList();
                
                CloudSim.stopSimulation();
                
                printCloudletList(clList);
                
                printVmList(vmList);
               
                //System.out.println("FINISH");
                //DecimalFormat dft = new DecimalFormat("###.##");
                //System.out.println("Delay: " + dft.format(NetworkTopology.getDelay(broker.getId(), datacenterA.getId()))); 

            }
            catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("The simulation has been terminated due to an unexpected error");
            }
    }

    private static Datacenter createDatacenter(String name, int mips, int ram, long storage, int bw){

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
        int hostId=0;

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

    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static DatacenterBroker createBroker(){

        DatacenterBroker broker = null;
        try {
                broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
                e.printStackTrace();
                return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects
     * @param list  list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "\t\t";
        System.out.println();
        System.out.println("========== CLOUDLETS ==========");
        System.out.println("CL ID" + indent + "STATUS" + indent +
                        "DC Name" + indent + "VM ID" + indent + "Time" + indent + "Start" + indent + "Finish" + indent + "User ID");

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            System.out.print(cloudlet.getCloudletId() + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
                    System.out.print("SUCCESS");
                    DecimalFormat dft = new DecimalFormat("###.##");
                    System.out.println( indent + cloudlet.getResourceName(cloudlet.getResourceId()) + indent + cloudlet.getVmId() +
                                    indent + dft.format(cloudlet.getActualCPUTime()) + indent + dft.format(cloudlet.getExecStartTime())+
                                    indent + dft.format(cloudlet.getFinishTime()) + indent + cloudlet.getUserId());
            }
        }
    }
    
    private static void printVmList(List<Vm> list){
        String indent = "\t\t";
        System.out.println();
        System.out.println("========== VMs ==========");
        System.out.println("VM ID" + indent + "User ID" + indent +
                        "Host ID" + indent + "DC ID" + indent + "DC Name" + indent + "Start" + indent + "Finish" + indent + "User ID");
        for(Vm v : list){
            System.out.println(v.getCurrentAllocatedRam());
            //System.out.println(v.getId() + indent + v.getUserId() + indent + v.getHost().getId() + indent + v.getHost().getDatacenter().getId() + indent + v.getHost().getDatacenter().getName());
        }
    }
}
