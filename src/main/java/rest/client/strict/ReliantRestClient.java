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
 * The Class ReliaRestClient.
 *
 * <h2>Handled Exceptions:</h2>
 *
 * <ol>
 * <li>ResourceAccessException - connect timeout
 * <li>ResourceAccessException - read timeout
 * <li>HttpMessageNotReadableException - When transformation to the destination type fails.
 * <li>HttpClientErrorException - Status 4xx
 * <li>HttpServerErrorException - Status 5xx (special management of 503 retry after ????)
 * <li>HttpNot2xxStatusCodeException - Status 1xx or 3xxx
 * <li>UnknownHttpStatusCodeException - Custom or Unknown StatusCode ????
 * <li>RestClientException
 * </ol>
 */
public class ReliantRestClient {

    static Logger log = LoggerFactory.getLogger(ReliantRestClient.class);

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
     * Constructor with defaults.
     */
    public ReliantRestClient() {
        this(0,0);
    }

    /**
     * Instantiates a new reliant rest client.
     *
     * @param readTimeout the read timeout
     */
    public ReliantRestClient(final int readTimeout) {
        this(0, readTimeout);
    }

    /**
     * Instantiates a new reliant rest client.
     *
     * @param connectTimeout the connect timeout
     * @param readTimeout the read timeout
     */
    public ReliantRestClient(final int connectTimeout, final int readTimeout) {
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
     * Execute the provided function with RestTemplate as an argument.
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
     * Create rest template context
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

        RetryPolicy retry3 = new SimpleRetryPolicy(3);
        RetryPolicy neverRetry = new NeverRetryPolicy();

        Map<Class<? extends Throwable>, RetryPolicy> map = new HashMap<>();
        map.put(ResourceAccessException.class, retry3);
        map.put(RestClientResponseException.class, neverRetry);
        map.put(HttpMessageNotReadableException.class, neverRetry);

        BiFunction<Throwable, RetryPolicy, RetryPolicy> classifier = ( (th, rp) ->  {

            if (th instanceof ResourceAccessException) {

                boolean connTO = th.getMessage().toLowerCase().contains("connect timed out");

                if (!connTO) {
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
        bop.setInitialInterval(20_000L);

        return bop;
    }
}
