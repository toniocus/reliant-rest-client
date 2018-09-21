package rest.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * The Class ReliantRestClientUtil.
 */
final class ReliantRestClientUtil {

    /**
     * Constructor.
     */
    private ReliantRestClientUtil() {
    }

    /**
     * The Class RestTemplateContext.
     */
    protected static class RestTemplateContext {
        RestTemplate restTemplate;
        ReliantRestClientInterceptor interceptor;
        int connectTimeoutInMillis;
        int readTimeoutInMillis;

    }

    /**
     * Create rest template context
     *
     * @param connectTimeoutInMillis the connect timeout in millis
     * @param readTimeoutInMillis the read timeout in millis
     * @return the rest template
     */
    public static RestTemplateContext createRestTemplateContext(
            final int connectTimeoutInMillis
            , final int readTimeoutInMillis)  {

        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(connectTimeoutInMillis);
        simpleClientHttpRequestFactory.setReadTimeout(readTimeoutInMillis);

        BufferingClientHttpRequestFactory buffReqFactory =
                new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory);

        RestTemplate rt = new RestTemplate(buffReqFactory);

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
        ctx.readTimeoutInMillis = readTimeoutInMillis;
        ctx.connectTimeoutInMillis = connectTimeoutInMillis;

        return ctx;
    }


    /**
     * Create retry policy.
     *
     * @return the retry policy
     */
    public static RetryPolicy createRetryPolicy() {

        SimpleRetryPolicy simple3 = new SimpleRetryPolicy(3);
        SimpleRetryPolicy simple0 = new SimpleRetryPolicy(0);

        Map<Class<? extends Throwable>, RetryPolicy> map = new HashMap<>();
        map.put(ResourceAccessException.class, simple3);
        map.put(HttpMessageNotReadableException.class, simple0);

        ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();
        policy.setPolicyMap(map);

        return policy;
    }


}
