/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rallocloud.main;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import java.util.ArrayList;
import toools.set.IntSet;

/**
 *
 * @author Atakan
 */
public class Visualizer {
    public static void display(double[][] bwMatrix, ArrayList<String> labels, ArrayList<Integer>... highlight){
        Grph g = new InMemoryGrph();
        for(int i = 0; i < bwMatrix.length; i++){
            g.addVertex(i);
            g.getVertexLabelProperty().setValue(i, labels.get(i));
            g.getVertexSizeProperty().setValue(i, 20);
        }
        for(int i = 0; i < bwMatrix.length; i++){
            for(int j = 0; j < i; j++){
                if(bwMatrix[i][j] > 0){
                    int e = g.addUndirectedSimpleEdge(i, j);
                    g.getEdgeLabelProperty().setValue(e, String.valueOf(bwMatrix[i][j]) + "Mbit");
                }
            }
        }
        int color = 2;
        for(ArrayList<Integer> h : highlight){
            for(int v : h){
                g.highlightVertex(v, color);
                g.highlightEdges(g.getEdgesIncidentTo(v), color);
            }
            color++;
        }
        g.display();
    }
    
}
