package rest.client;

import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * The Class ReliaRestClient.
 *
 * <h2>Handled Exceptions:</h2>
 *
 * <ol>
 * <li>ResourceAccessException - connect timeout
 * <li>ResourceAccessException - read timeout
 * <li>HttpMessageNotReadableException - When transformation to the destination type fails.
 * <li>HttpClientErrorException - Status 4xx
 * <li>HttpServerErrorException - Status 5xx (special management of 503 retry after)
 * <li>UnknownHttpStatusCodeException - Custom or Unknown StatusCode ????
 * <li>RestClientException
 * </ol>
 */
public class ReliantRestClient {

    /**
     * The Constant DEFAULT_CONNECT_TIMEOUT_IN_MILLIS = {@value #DEFAULT_CONNECT_TIMEOUT_IN_MILLIS}.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_IN_MILLIS = 5_000;

    /**
     * The Constant DEFAULT_READ_TIMEOUT_IN_MILLIS = {@value #DEFAULT_READ_TIMEOUT_IN_MILLIS}
     */
    public static final int DEFAULT_READ_TIMEOUT_IN_MILLIS = 30_000;

    private ReliantRestClientUtil.RestTemplateContext rtContext;

    /**
     * Constructor with defaults.
     */
    public ReliantRestClient() {
        this.rtContext = ReliantRestClientUtil.createRestTemplateContext(
                DEFAULT_CONNECT_TIMEOUT_IN_MILLIS, DEFAULT_READ_TIMEOUT_IN_MILLIS);
    }

    /**
     * Execute.
     *
     * @param <T> the generic type
     * @param function the function
     * @return the response entity
     */
    public <T> ResponseEntity<T> execute(final Function<RestTemplate, ResponseEntity<T>> function) {

        Validate.notNull(function, "Function argument should not be null");

        RetryPolicy policy = ReliantRestClientUtil.createRetryPolicy();

        // Use the policy...
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(policy);

        ReliantRetryCallback<T> rcc = new ReliantRetryCallback<>(this.rtContext.restTemplate, function);

        try {
            return template.execute(rcc);
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }

    }

    // ============================================================================
    // ### - Helper classes
    // ============================================================================

    /**
     * The Class ReliantRetryCallback.
     *
     * @param <T> the generic type
     */
     private class ReliantRetryCallback<T> implements RetryCallback<ResponseEntity<T>, RuntimeException> {

        private Function<RestTemplate, ResponseEntity<T>> function;
        private RestTemplate rt;

        /**
         * Instantiates a new reliant retry callback.
         *
         * @param pRt the rt
         * @param pFunction the function
         */
        public ReliantRetryCallback(final RestTemplate pRt,
                final Function<RestTemplate, ResponseEntity<T>> pFunction) {
            this.rt = pRt;
            this.function = pFunction;
        }


        @Override
        public ResponseEntity<T> doWithRetry(final RetryContext context) throws RuntimeException {
            return this.function.apply(this.rt);
        }
     };
}
