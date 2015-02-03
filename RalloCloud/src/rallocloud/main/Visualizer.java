/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.path.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.cloudbus.cloudsim.NetworkTopologyPublic;
import toools.set.IntArrayListSet;

/**
 *
 * @author Atakan
 */
public class Visualizer {

    public static void emptyTopology(Double[][] bwMatrix, ArrayList<String> labels, ArrayList<Integer>... highlight) {
        Grph g = new InMemoryGrph();

        for (int i = 0; i < bwMatrix.length; i++) {
            g.addVertex(i);
            if (labels.size() > 0) {
                g.getVertexLabelProperty().setValue(i, labels.get(i) + " (" + i + ")");
            }
            g.getVertexSizeProperty().setValue(i, 20);
        }
        for (int i = 0; i < bwMatrix.length; i++) {
            for (int j = 0; j < i; j++) {
                if (bwMatrix[i][j] > 0) {
                    int e = g.addUndirectedSimpleEdge(i, j);
                    DecimalFormat dft = new DecimalFormat("###.##");
                    g.getEdgeLabelProperty().setValue(e, dft.format(NetworkTopologyPublic.getDelay(i+2, j+2)) + "ms");// / " + String.valueOf(bwMatrix[i][j]) + "Mbit");
                }
            }
        }
        int color = 2;
        for (ArrayList<Integer> h : highlight) {
            IntArrayListSet hl = new IntArrayListSet();
            for (int v : h) {
                hl.add(v);
            }
            g.highlight(g.getSubgraphInducedByVertices(hl), color);
            color++;
        }
        g.display();
    }

    public static void assignedTopology(Double[][] bwMatrix, ArrayList<String> labels, ArrayList<Integer>... highlight) {
        Grph g = new InMemoryGrph();
        for (int i = 0; i < bwMatrix.length; i++) {
            g.addVertex(i);
            g.getVertexLabelProperty().setValue(i, labels.get(i) + " (" + i + ")");
            g.getVertexSizeProperty().setValue(i, 20);
        }
        for (int i = 0; i < bwMatrix.length; i++) {
            for (int j = 0; j < i; j++) {
                if (bwMatrix[i][j] > 0) {
                    int e = g.addUndirectedSimpleEdge(i, j);
                    DecimalFormat dft = new DecimalFormat("###.##");
                    g.getEdgeLabelProperty().setValue(e, dft.format(NetworkTopologyPublic.getDelay(i+2, j+2)) + "ms");// / " + String.valueOf(bwMatrix[i][j]) + "Mbit");
                }
            }
        }
        int color = 2;
        for (ArrayList<Integer> h : highlight) {
            for (int v1 : h) {
                g.highlightVertex(v1, color);
                g.getVertexSizeProperty().setValue(v1, 25);
                for (int v2 : h) {
                    Path p = g.getShortestPath(v1, v2);
                    for (int i = 0; i < p.getLength(); i++) {
                        g.highlightEdges(g.getEdgesConnecting(p.getVertexAt(i), p.getVertexAt(i + 1)), color);
                        g.getEdgeWidthProperty().setValue(g.getEdgesConnecting(p.getVertexAt(i), p.getVertexAt(i + 1)).getGreatest(), 2);
                    }
                }
            }
            color++;
        }
        g.display();
    }
}
