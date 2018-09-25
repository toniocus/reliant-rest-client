package rest.client.basic;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.web.client.RestTemplate;

/**
 * The Class ReliantRetryCallback.
 *
 * @param <T> the generic type
 */
 public class ReliantRetryCallback<T> implements RetryCallback<ResponseEntity<T>, RuntimeException> {

    private static Logger log = LoggerFactory.getLogger(ReliantRetryCallback.class);

    private Function<RestTemplate, ResponseEntity<T>> function;
    private RestTemplate rt;

    /**
     * Instantiates a new reliant retry callback.
     *
     * @param pRt the rt
     * @param pFunction the function
     * @param pReliantRestClient TODO
     */
    public ReliantRetryCallback(final RestTemplate pRt,
            final Function<RestTemplate, ResponseEntity<T>> pFunction) {
        this.rt = pRt;
        this.function = pFunction;
    }


    @Override
    public ResponseEntity<T> doWithRetry(final RetryContext context) throws RuntimeException {

        if (context.getRetryCount() > 0) {
            log.info("Retry #" + context.getRetryCount()
                    +  " caused by: "
                    +  (context.getLastThrowable() == null ? "Unknown Reason" : context.getLastThrowable().toString())
                    );
        }

        return this.function.apply(this.rt);
    }
 }