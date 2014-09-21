/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rallocloud.main;

/**
 *
 * @author Atakan
 */
public class Statistician {

    private static int RJR;
    private static int size;

    public static void setSize(int size) {
        Statistician.size = size;
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
}
