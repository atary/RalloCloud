/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;

/**
 *
 * @author Atakan
 */
public class Statistician {

    private static int RJR = 0;
    private static int size = 0;
    private static double endTime;
    private static final ArrayList<Double> delays = new ArrayList<>();

    public static void addDelay(double d) {
        if(d>0) delays.add(d);
    }

    public static void setEndTime(double endTime) {
        Statistician.endTime = endTime;
    }

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

    public static double getLDB(List<Cloudlet> clList, ArrayList<Datacenter> dcList) {
        HashMap<Integer, Double> dcUtil = new HashMap<>();
        for (Cloudlet c : clList) {
            int d = c.getResourceId();
            double newValue = c.getCloudletLength();
            for (Datacenter dc : dcList) {
                if (dc.getId() == d) {
                    newValue = newValue / dc.getHostList().get(0).getTotalMips();
                }
            }
            if (dcUtil.containsKey(d)) {
                newValue += dcUtil.get(d);
            }
            dcUtil.put(d, newValue);
        }
        double LDB = 0;
        double mean = 0;
        for (double util : dcUtil.values()) {
            mean += util;
        }
        mean /= dcUtil.size();
        for (double util : dcUtil.values()) {
            LDB += Math.pow(util - mean, 2);
        }
        return Math.sqrt(LDB / dcUtil.size());
    }

    static double getTRP(List<Cloudlet> clList) {
        double mips = 0;
        for (Cloudlet c : clList) {
            mips += c.getCloudletLength();
        }
        return mips / endTime;
    }

    static double getADL() {
        double total = 0;
        for (double d : delays) {
            total += d;
        }
        return total / delays.size();
    }

    static double getMDL() {
        double max = 0;
        for (double d : delays) {
            if (d > max) {
                max = d;
            }
        }
        return max;
    }
}
