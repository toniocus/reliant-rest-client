package rest.client.ta;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TaRestRetryPolicy.
 *
 * <P>TODO an interface that can let us implement different retry policies,
 * and of course let TaRestTemplate accept different policies.
 *
 * @author tonioc
 */
public class TaRestRetryPolicy implements Cloneable {

    private static Logger log = LoggerFactory.getLogger(TaRestRetryPolicy.class);

    /**
     * The Constant INITIAL_RETRY_DELAY_DEFAULT, {@value #INITIAL_RETRY_DELAY_DEFAULT} milliseconds.
     */
    public static final int  INITIAL_RETRY_DELAY_DEFAULT         = 5_000;

    /**
     * The Constant MAX_RETRIES_DEFAULT, {@value #MAX_RETRIES_DEFAULT} retries.
     */
    public static final int  MAX_RETRIES_DEFAULT                 = 3;


    private int initialRetryDelay;
    private int maxRetries;
    private int retry = 0;
    private Random random = new Random(System.currentTimeMillis());

    /**
     * Constructor.
     *
     * @param maxRetries the max retries if less or equal 0 uses {@link #MAX_RETRIES_DEFAULT}
     * @param initialRetryDelay the initial retry delay in milliseconds
     *  , if less or equal 0 uses {@link #INITIAL_RETRY_DELAY_DEFAULT}
     */
    public TaRestRetryPolicy(
            final int maxRetries
            , final int initialRetryDelay) {

        this.initialRetryDelay = (initialRetryDelay <= 0 ? INITIAL_RETRY_DELAY_DEFAULT : initialRetryDelay);
        this.maxRetries = (maxRetries <= 0 ? MAX_RETRIES_DEFAULT : maxRetries);
    }

    /**
     * Gets the retry number.
     *
     * @return the retry number
     */
    public int getNextRetry() {
        return this.retry;
    }

    /**
     * Init policy, so it can start a new cycle.
     */
    public void initPolicy() {
        this.retry = 0;
    }

    /**
     * Waits a configured time before returning.
     *
     * @return true, if a retry should be performed
     */
    public boolean retryWait() {
        this.retry++;

        if (this.retry > this.maxRetries) {
            return false;
        }

        double factor = 1;
        for (int i=1; i<this.retry; i++) {
            factor *= (1 + this.random.nextDouble());
        }

        int delay = (int) (this.initialRetryDelay * factor);

        try {
            log.info("Connect timeout retry {} with delay (millisecs) {}", this.retry, delay);
            Thread.sleep(delay);
        }
        catch(InterruptedException intEx) {
            // OK nothing to do
        }

        return true;

    }
}
