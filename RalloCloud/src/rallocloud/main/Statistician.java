/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;

/**
 *
 * @author Atakan
 */
public class Statistician {

    private static int RJR = 0;
    private static int size = 0;

    public static double getRJR() {
        return (double) RJR / size;
    }

    public static void rejected() {
        RJR++;
    }

    public static void trial() {
        size++;
    }

    public static double getDSF(ArrayList<List<Cloudlet>> clSepList) {
        double DSF = 0;
        for (List<Cloudlet> clList : clSepList) {
            HashSet<Integer> dcs = new HashSet<>();
            for (Cloudlet c : clList) {
                dcs.add(c.getResourceId());
            }
            DSF += (double) dcs.size() / (double) clList.size();
        }
        return DSF / (double) clSepList.size();
    }
}
