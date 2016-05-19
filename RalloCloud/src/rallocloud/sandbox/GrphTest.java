/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.sandbox;

import grph.*;
import grph.in_memory.InMemoryGrph;
import java.io.OutputStream;
import java.io.PrintStream;
import toools.StopWatch;

/**
 *
 * @author Atakan
 */
public class GrphTest {

    private enum topologyType {

        LINEAR, CIRCULAR, COMPLETE, STAR
    }

    public static void main(String[] args) {
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
                //NO-OP
            }
        });
        PrintStream originalStream = System.out;
        for (int i = 10; i <= 500; i += 20) {
            System.setOut(dummyStream);

            Grph g = new InMemoryGrph();

            g.addNVertices(i);
            g.rnws(3, 0.5);

            SubgraphSearch sgs = new SubgraphSearch();

            StopWatch sw = new StopWatch(StopWatch.UNIT.ms);

            Grph sub = generateGraph(5, topologyType.STAR);
            sgs.findAllMatches(g, sub);

            sub = generateGraph(5, topologyType.CIRCULAR);
            sgs.findAllMatches(g, sub);

            sub = generateGraph(5, topologyType.COMPLETE);
            sgs.findAllMatches(g, sub);

            sub = generateGraph(5, topologyType.LINEAR);
            sgs.findAllMatches(g, sub);
            
            System.setOut(originalStream);
            System.out.println(i + "\t" + sw.getElapsedTime() / 4);
            sw.reset();
        }
    }

    private static Grph generateGraph(int count, topologyType type) {
        return createGraph(createMatrix(count, type));
    }

    private static Grph createGraph(Double[][] m) {
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

    private static Double[][] createMatrix(int count, topologyType type) {
        Double[][] topology = new Double[count][count];

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                topology[i][j] = 0.0;
            }
        }

        switch (type) {
            case LINEAR:
                for (int i = 0; i < count - 1; i++) {
                    topology[i][i + 1] = 1.0;
                    topology[i + 1][i] = 1.0;
                }
                break;
            case COMPLETE:
                for (int i = 0; i < count; i++) {
                    for (int j = 0; j < count; j++) {
                        if (i != j) {
                            topology[i][j] = 1.0;
                        }
                    }
                }
                break;
            case CIRCULAR:
                for (int i = 0; i < count - 1; i++) {
                    topology[i][i + 1] = 1.0;
                    topology[i + 1][i] = 1.0;
                }
                topology[count - 1][0] = 1.0;
                topology[0][count - 1] = 1.0;
                break;
            case STAR:
                for (int i = 1; i < count; i++) {
                    topology[i][0] = 1.0;
                    topology[0][i] = 1.0;
                }
                break;
        }
        return topology;
    }
}
