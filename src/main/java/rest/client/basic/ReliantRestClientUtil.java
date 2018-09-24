package rest.client.basic;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * The Class ReliantRestClientUtil.
 */
public final class ReliantRestClientUtil {

    /**
     * Constructor.
     */
    private ReliantRestClientUtil() {
    }


    /**
     * Create retry policy.
     *
     * @return the retry policy
     */
    public static RetryPolicy createRetryPolicy() {

        RetryPolicy simple3 = new SimpleRetryPolicy(3);
        RetryPolicy simple0 = new NeverRetryPolicy();

        Map<Class<? extends Throwable>, RetryPolicy> map = new HashMap<>();
        map.put(ResourceAccessException.class, simple3);
        map.put(RestClientResponseException.class, simple0);
        map.put(HttpMessageNotReadableException.class, simple0);

        ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();
        policy.setPolicyMap(map);

        return policy;
    }


}
