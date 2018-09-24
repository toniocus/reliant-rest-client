package rest.client.strict;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import rest.client.basic.ReliantRestClientInterceptor;
import rest.client.basic.ReliantRestClientUtil;
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
     * Execute the provided function with RestTemplate as an argument.
     *
     * @param <T> the generic type
     * @param function the function
     * @return the response entity
     */
    public <T> ResponseEntity<T> execute(final Function<RestTemplate, ResponseEntity<T>> function) {

        Validate.notNull(function, "Function argument should not be null");

        RetryPolicy policy = ReliantRestClientUtil.createRetryPolicy();

        ExponentialBackOffPolicy bop = new ExponentialBackOffPolicy();
        bop.setMultiplier(3);
        bop.setMaxInterval(150_000L);
        bop.setInitialInterval((this.connectTimeout < 20_0000 ? 30_000 : 45_000));

        // Use the policy...
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(policy);
        template.setBackOffPolicy(bop);

        ReliantRetryCallback<T> rcc = new ReliantRetryCallback<>(this.rtContext.restTemplate, function);

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
        RestTemplate restTemplate;
        ReliantRestClientInterceptor interceptor;

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

        BufferingClientHttpRequestFactory buffReqFactory =
                new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory);

        RestTemplate rt = new RestTemplate(buffReqFactory);
        rt.setErrorHandler(new StrictResponseErrorHandler());

        // Add interceptor
        List<ClientHttpRequestInterceptor> interceptors = rt.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
            rt.setInterceptors(interceptors);
        }

        ReliantRestClientInterceptor interceptor = new ReliantRestClientInterceptor();
        interceptors.add(interceptor);

        RestTemplateContext ctx = new RestTemplateContext();
        ctx.interceptor = interceptor;
        ctx.restTemplate = rt;

        return ctx;
    }

}
