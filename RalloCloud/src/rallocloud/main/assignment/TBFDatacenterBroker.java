/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main.assignment;

import com.carrotsearch.hppc.IntArrayList;
import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.util.Matching;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;
import rallocloud.main.MyNetworkTopology;

/**
 * Topology based best fit
 *
 * @author Atakan
 */
public class TBFDatacenterBroker extends BrokerStrategy {

    public TBFDatacenterBroker(String name) throws Exception {
        super(name);
    }

    @Override
    protected void createSingleVm(int vmId) {
        ArrayList<Integer> g = new ArrayList<>();
        g.add(vmId);
        Double[][] t = new Double[1][1];
        t[0][0] = 0.0;
        System.out.println("Group is broken.");
        createGroupVm(g, t);
    }

    @Override
    protected void createGroupVm(List<Integer> g, Double[][] t) {
        Matching m = matchTopology(t, g);
        if (m == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < g.size(); i++) {
            Vm vm = VmList.getById(getVmList(), g.get(i));
            int datacenterId = m.pattern2graph().get(i) + 2;
            setVmsRequested(getVmsRequested() + 1);
            Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId() + " in " + CloudSim.getEntityName(datacenterId) + " (" + datacenterId + ")");
            sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
        }
    }

    private Matching matchTopology(Double[][] topology, List<Integer> group) {
        Double[][] bwMatrix = MyNetworkTopology.getBwMatrix();
        Grph g = createGraph(bwMatrix);
        Grph s = createGraph(topology);

        List<Matching> m = compute(g, s, false);
        Collections.shuffle(m);
        for (Matching mm : m) {
            boolean good = true;
            for (int i : mm.pattern2graph().values().toIntegerArrayList()) {
                if (i > 13) {
                    good = false;
                    break;
                }
            }
            if (good) {
                return mm;
            }
        }
        return null;
    }

    private Grph createGraph(Double[][] m) {
        Grph g = new InMemoryGrph();

        for (int i = 0; i < m.length; i++) {
            g.addVertex(i);
        }
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < i; j++) {
                if (m[i][j] > 0) {
                    g.addUndirectedSimpleEdge(i, j);
                }
            }
        }
        return g;
    }

    public List<Matching> compute(Grph g, Grph p, boolean induced) {
        if (g.getVertices().getDensity() < 1) {
            throw new IllegalArgumentException();
        }

        if (p.getVertices().getDensity() < 1) {
            throw new IllegalArgumentException();
        }

        IntArrayList l = new IntArrayList();
        List<IntArrayList> matchings = new ArrayList<IntArrayList>();

        compute(g, p, true, matchings, l, 0, induced);
        List<Matching> mm = new ArrayList<Matching>();

        for (IntArrayList l2 : matchings) {
            Matching m = new Matching();

            for (int i = 0; i < l2.size(); ++i) {
                m.pattern2graph(i, l2.get(i));
            }

            mm.add(m);
        }

        return mm;
    }

    public boolean compute(Grph g, Grph h, boolean all, List<IntArrayList> matchings, IntArrayList l, int hv,
            boolean induced) {
        if (!h.getVertices().contains(hv)) {
            if (all) {
                matchings.add(l.clone());
                return false;
            } else {
                return true;
            }
        }

        boolean mistake = false;

        for (int gv : g.getVertices().toIntArray()) {
            if (l.contains(gv)) {
                continue;
            }

            mistake = false;

            for (int hi = 0; hi < l.size(); ++hi) {
                int gi = l.get(hi);
                boolean match = induced ? (g.areVerticesAdjacent(gi, gv) == h.areVerticesAdjacent(hi, hv)) : (g
                        .areVerticesAdjacent(gi, gv) || !h.areVerticesAdjacent(hi, hv));

                boolean match2 = induced ? (g.areVerticesAdjacent(gv, gi) == h.areVerticesAdjacent(hv, hi)) : (g
                        .areVerticesAdjacent(gv, gi) || !h.areVerticesAdjacent(hv, hi));

                if (!match && !match2) {
                    mistake = true;
                    break;
                }
            }

            if (mistake) {
                continue;
            } else {
                l.add(gv);

                if (compute(g, h, all, matchings, l, hv + 1, induced)) {
                    return true;
                } else {
                    l.remove(l.size() - 1);
                }
            }
        }

        return false;
    }

}
