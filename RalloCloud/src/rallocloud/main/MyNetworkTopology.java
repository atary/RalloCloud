/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main;

import java.io.IOException;
import java.util.HashMap;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.network.DelayMatrix_Float;
import org.cloudbus.cloudsim.network.GraphReaderBrite;

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
}
