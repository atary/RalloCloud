/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.path.Path;
import org.cloudbus.cloudsim.provisioners.BwProvisionerNetworked;

/**
 *
 * @author Atakan
 */
public class NetworkTopologyPublic extends NetworkTopology {

    private static HashSet<DatacenterBrokerStrategy> brokerSet = new HashSet<>();
    private static Grph grph = new InMemoryGrph();

    public static void setBrokerSet(HashSet<DatacenterBrokerStrategy> brokerSet) {
        NetworkTopologyPublic.brokerSet = brokerSet;
    }

    public static Double[][] getBwMatrix() {
        int l = bwMatrix.length;
        Double[][] temp = new Double[l][l];
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < l; j++) {
                temp[i][j] = bwMatrix[i][j];
            }
        }
        return temp;
    }

    public static void setNextIdx(int nextIdx) {
        NetworkTopology.nextIdx = nextIdx;
    }

    public static int getInDegree(int id) {
        int mid = map.get(id);
        int inDegree = 0;
        int l = bwMatrix.length;
        for (int i = 0; i < l; i++) {
            if (bwMatrix[i][mid] > 0) {
                inDegree++;
            }
        }
        return inDegree;
    }

    public static int getOutDegree(int id) {
        int mid = map.get(id);
        int outDegree = 0;
        int l = bwMatrix.length;
        for (int i = 0; i < l; i++) {
            if (bwMatrix[mid][i] > 0) {
                outDegree++;
            }
        }
        return outDegree;
    }

    public static void mapNodes(Datacenter dc, int briteID) {
        int cloudSimEntityID = dc.getId();
        try {
            // this CloudSim entity was already mapped?
            if (!map.containsKey(cloudSimEntityID)) {
                if (!map.containsValue(briteID)) { // this BRITE node was already mapped?
                    map.put(cloudSimEntityID, briteID);
                } else {
                    Log.printLine("Error in network mapping. BRITE node " + briteID + " already in use.");
                }
            } else {
                Log.printLine("Error in network mapping. CloudSim entity " + cloudSimEntityID
                        + " already mapped.");
            }
        } catch (Exception e) {
            Log.printLine("Error in network mapping. CloudSim node " + cloudSimEntityID
                    + " not mapped to BRITE node " + briteID + ".");
        }

        for (Host h : dc.getHostList()) {
            long bw = h.getBw();
            bw *= getInDegree(cloudSimEntityID);
            h.setBwProvisioner(new BwProvisionerNetworked(bw, dc.getId()));
        }
    }

    public static ArrayList<Datacenter> getShortestPathDCs(Vm vm, int dcId) {
        DatacenterBrokerStrategy broker = null;
        for (DatacenterBrokerStrategy b : brokerSet) {
            if (b.getId() == vm.getUserId()) {
                broker = b;
            }
        }
        ArrayList<Integer> group = null;
        for (List<Integer> g : broker.getVmGroups().keySet()) {
            if (g.contains(vm.getId())) {
                group = (ArrayList<Integer>) g;
            }
        }
        Double[][] topology = broker.getVmGroups().get(group);
        int index = group.indexOf(vm.getId());

        ArrayList<Integer> connectedVmIds = new ArrayList<>();
        for (int i = 0; i < topology[index].length; i++) {
            if (topology[index][i] > 0) {
                connectedVmIds.add(group.get(i));
            }
        }

        ArrayList<Integer> endDCIds = new ArrayList<>();
        for (Vm v : broker.getVmList()) {
            if (connectedVmIds.contains(v.getId()) && v.getHost() != null) {
                endDCIds.add(v.getHost().getDatacenter().getId() - 2); //grph ids are -2
            }
        }

        fillGrph();

        ArrayList<Integer> DCIds = new ArrayList<>();
        for (int i : endDCIds) {
            Path p = grph.getShortestPath(dcId - 2, i);
            for (int j : p.getVertexSet().toIntArray()) {
                if (j != dcId - 2) {
                    DCIds.add(j + 2);
                }
            }
        }

        ArrayList<Datacenter> DCs = new ArrayList<>();
        for (Datacenter d : broker.getDatacenterList()) {
            if(DCIds.contains(d.getId())){
                DCs.add(d);
            }
        }
        
        return DCs;
    }

    private static void fillGrph() {
        if (grph.getVertices().isEmpty()) {
            for (int i = 0; i < bwMatrix.length; i++) {
                grph.addVertex(i);
            }
            for (int i = 0; i < bwMatrix.length; i++) {
                for (int j = 0; j < i; j++) {
                    if (bwMatrix[i][j] > 0) {
                        grph.addUndirectedSimpleEdge(i, j);
                    }
                }
            }
        }
    }
}
