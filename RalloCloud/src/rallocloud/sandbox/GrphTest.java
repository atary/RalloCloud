/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rallocloud.sandbox;

import grph.*;
import grph.in_memory.InMemoryGrph;

/**
 *
 * @author Atakan
 */
public class GrphTest {
    
    public static void main(String[] args) {
        
        Grph g = new InMemoryGrph();
        
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addDirectedSimpleEdge(1, 2);
        g.addUndirectedSimpleEdge(3, 2);
        g.getVertexColorProperty().setValue(2, 5);
        g.getVertexColorProperty().setValue(1, 5);
        g.getEdgeStyleProperty().setValue(1, 5);
        g.getEdgeColorProperty().setValue(0, 5);
        g.display();
    }
}
