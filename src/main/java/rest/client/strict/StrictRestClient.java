package rest.client.strict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import rest.client.basic.ReliantRestClientBodyInterceptor;
import rest.client.basic.ReliantRestClientClassifier;
import rest.client.basic.ReliantRetryCallback;

/**
 * StrictRestClient a wrapper over RestTemplate with standard retry policies and timeout periods,
 * and throws exception if HttpStatus 3xx is received.
 *
 * <h2>Simple use:</h2>
 * <pre>
 * {@code
 * ....
 *    // Construct with default timeouts
 *    StrictRestClient strictRest = new StrictRestClient();
 *
 *    // call your restemplate method as a lamba.
 *    ResponseEntity<String> result = strictRest
 *          .execute(restTemplate -> restTemplate.getForEntity("http://localhost:9090/status100", String.class));
 *
 * ....
 * }
 * </pre>
 *
 * <h2>Handled Exceptions:</h2>
 *
 * <ol>
 * <li>ResourceAccessException - connect timeout (3 retries)
 * <li>ResourceAccessException - I/O or read timeout (0 retries)
 * <li>HttpMessageNotReadableException - ex. Json parse error, (0 retries and logs original message)
 * <li>HttpClientErrorException - (0 retries)
 * <li>HttpServerErrorException - (0 retries)
 * <li>HttpNot2xxStatusCodeException - Status not 2xx, 4xx, 5xx (0 retries)
 * <li>UnknownHttpStatusCodeException - Custom or Unknown StatusCode (0 retries)
 * <li>RestClientException - (0 retries)
 * </ol>
 */
public class StrictRestClient {

    static Logger log = LoggerFactory.getLogger(StrictRestClient.class);

    /**
     * The Constant DEFAULT_CONNECT_TIMEOUT_IN_MILLIS = {@value #DEFAULT_CONNECT_TIMEOUT_IN_MILLIS}.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT_IN_MILLIS = 5_000;

    /**
     * The Constant DEFAULT_READ_TIMEOUT_IN_MILLIS = {@value #DEFAULT_READ_TIMEOUT_IN_MILLIS}
     */
    public static final int DEFAULT_READ_TIMEOUT_IN_MILLIS = 30_000;

    private RestTemplateContext rtContext;
    private int connectTimeout;
    private int readTimeout;

    /**
     * Constructor with default timeouts.
     */
    public StrictRestClient() {
        this(0,0);
    }

    /**
     * Instantiates a new reliant rest client, with readTimeout and default connectionTimeout.
     *
     * @param readTimeout the read timeout, if less or equal 0 {@link #DEFAULT_READ_TIMEOUT_IN_MILLIS} will be used.
     */
    public StrictRestClient(final int readTimeout) {
        this(0, readTimeout);
    }

    /**
     * Instantiates a new reliant rest client, with connect and read timeout.
     *
     * @param connectTimeout the connect timeout, if less or equal 0 {@link #DEFAULT_CONNECT_TIMEOUT_IN_MILLIS} will be used.
     * @param readTimeout the read timeout, if less or equal 0 {@link #DEFAULT_READ_TIMEOUT_IN_MILLIS} will be used.
     */
    public StrictRestClient(final int connectTimeout, final int readTimeout) {
        this.connectTimeout = (connectTimeout <= 0 ? DEFAULT_CONNECT_TIMEOUT_IN_MILLIS : connectTimeout);
        this.readTimeout = (readTimeout <= 0 ? DEFAULT_READ_TIMEOUT_IN_MILLIS : readTimeout);
        this.rtContext = createRestTemplateContext(this.connectTimeout, this.readTimeout);
    }


    /**
     * Gets the rest template context.
     *
     * @return the rest template context
     */
    protected RestTemplateContext getRestTemplateContext() {
        return this.rtContext;
    }

    /**
     * Execute the provided lambda function that recieves {@link RestTemplate} as an argument.
     *
     * @param <T> the generic type
     * @param function the function
     * @return the response entity
     */
    public <T> ResponseEntity<T> execute(final Function<RestTemplate, ResponseEntity<T>> function) {

        Validate.notNull(function, "Function argument should not be null");

        // Use the policy...
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(createRetryPolicy());
        template.setBackOffPolicy(createBackOffPolicy());

        ReliantRetryCallback<T> rcc =
                new ReliantRetryCallback<>(getRestTemplateContext().restTemplate, function);

        try {
            return template.execute(rcc);
        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new RuntimeException("Execute RetryTemplate error", ex);
        }

    }

    // ============================================================================
    //   ### -  Building RestTemplate
    // ============================================================================

    /**
     * The Class RestTemplateContext.
     */
    protected static class RestTemplateContext {
        public RestTemplate restTemplate;
        public ReliantRestClientBodyInterceptor bodyInterceptor;

    }

    /**
     * Create rest template context.
     *
     * @param connectTimeoutInMillis the connect timeout in millis
     * @param readTimeoutInMillis the read timeout in millis
     * @return the rest template
     */
    private RestTemplateContext createRestTemplateContext(
            final int connectTimeoutInMillis
            , final int readTimeoutInMillis)  {

        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(connectTimeoutInMillis);
        simpleClientHttpRequestFactory.setReadTimeout(readTimeoutInMillis);

        // We need this to let the body interceptor read the body request
        // without exhausting the input stream.
        BufferingClientHttpRequestFactory buffReqFactory =
                new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory);


        RestTemplate rt = new RestTemplate(buffReqFactory);
        ReliantRestClientBodyInterceptor interceptor = new ReliantRestClientBodyInterceptor();
        rt.setErrorHandler(new StrictResponseErrorHandler());

        // Add interceptor
        List<ClientHttpRequestInterceptor> interceptors = rt.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
            rt.setInterceptors(interceptors);
        }

        interceptors.add(interceptor);

        RestTemplateContext ctx = new RestTemplateContext();
        ctx.bodyInterceptor = interceptor;
        ctx.restTemplate = rt;

        return ctx;
    }

    /**
     * Create retry policy.
     *
     * @return the retry policy
     */
    public RetryPolicy createRetryPolicy() {

        RetryPolicy retry3 = new SimpleRetryPolicy(4);
        RetryPolicy neverRetry = new NeverRetryPolicy();

        Map<Class<? extends Throwable>, RetryPolicy> map = new HashMap<>();
        map.put(ResourceAccessException.class, retry3);
        map.put(RestClientResponseException.class, neverRetry);
        map.put(HttpMessageNotReadableException.class, neverRetry);

        BiFunction<Throwable, RetryPolicy, RetryPolicy> classifier = ( (th, rp) ->  {

            if (th instanceof ResourceAccessException) {

                boolean connTO = th.getMessage().toLowerCase().contains("connect timed out");

                if (!connTO) {
                    // Really not sure if this is good, although it currently works
                    // seems the state of the retry is kept in retry context.
                    return neverRetry;
                }

            }
            else if (th instanceof HttpMessageNotReadableException) {

                if (getRestTemplateContext().bodyInterceptor != null) {
                    // if we do not do this, the body of the original message is lost
                    // and we will not be able to see it.
                    log.error("Message cannot be converted to expected type: "
                            + getRestTemplateContext().bodyInterceptor.getResponseBodyAsString()
                            );
                }
            }

            return rp;
        });

        ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();
        policy.setExceptionClassifier(new ReliantRestClientClassifier(map, classifier));

        return policy;
    }

    /**
     * Create back off policy.
     *
     * @return the back off policy
     */
    public BackOffPolicy createBackOffPolicy() {

        ExponentialBackOffPolicy bop = new ExponentialBackOffPolicy();
        bop.setMultiplier(2);
        bop.setMaxInterval(150_000L);
        bop.setInitialInterval(7_500L);

        return bop;
    }
}
