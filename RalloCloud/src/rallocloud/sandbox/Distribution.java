package rallocloud.sandbox;

import org.apache.commons.math3.distribution.*;

/**
 *
 * @author Atakan
 */
public class Distribution {

    public static void main(String[] args) {
        PoissonDistribution p = new PoissonDistribution(5); //VM sayısı için
        for (int i = 0; i < 10; i++) {
            System.out.println(p.sample());
        }

        UniformRealDistribution u = new UniformRealDistribution(0, 200); //İstek zamanı için
        for (int i = 0; i < 10; i++) {
            System.out.println(u.sample());
        }
    }
}
