package work;

import java.util.Random;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public class Work {

    public static void main(final String[] args) {

        Random r = new Random(System.currentTimeMillis());

        long t = 5_000L;

        System.out.println(t * 1.2);

        for (int i=0; i<5; i++) {
            double factor = 1.0 + r.nextDouble();
            System.out.format("factor: %f, t: %f,  t:%d%n", factor, t * factor, (long)(t*factor));
            t = (long) (t * factor);

        }


    }
}
