/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main;

import org.cloudbus.cloudsim.NetworkTopology;

/**
 *
 * @author Atakan
 */
public class MyNetworkTopology extends org.cloudbus.cloudsim.NetworkTopology {

    protected static double[][] bwUtilityMatrix = null;

    public static double[][] getBwUtilityMatrix() {
        return bwUtilityMatrix;
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
}
