package rest.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import rest.client.basic.ReliantRestClientBodyInterceptor;
import rest.client.strict.ReliantRestClient;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@SpringBootTest
public class TestReliantRerstClient {

    @BeforeClass
    public static void start() {
        ReliantDemoApplication.main("");
    }

    @AfterClass
    public static void end() {
        ReliantDemoApplication.shutdown();
    }

    @Test
    public void testConnect() throws Exception {

        try {
            ReliantRestClient rrc = new ReliantRestClient(5);

            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/timeout/20", String.class));

            System.out.println("Recived message: " +  result.getBody());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testNoJson() throws Exception {


        try {
            ReliantRestClient rrc = new ReliantRestClient();

            ResponseEntity<JsonNode> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/noJson", JsonNode.class));

            System.out.println("Recived message: " +  result.getBody());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testStatus300() throws Exception {


        try {
            ReliantRestClient rrc = new ReliantRestClient(50_000);

            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/status300", String.class));

            System.out.println("Recived message: " +  result.getBody());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testStatus100() throws Exception {

        try {
            ReliantRestClient rrc = new ReliantRestClient(10_000);

            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/status100", String.class));

            System.out.println("Recived message: " +  result.getBody());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testNoJsonError() throws Exception {

        RestTemplate rt = new RestTemplate();

        try {
            ResponseEntity<JsonNode> response = rt.getForEntity("http://localhost:9090/noJsonError", JsonNode.class);

            System.out.println(response.getBody());
        }
        catch (HttpClientErrorException ex) {

            System.out.println("Error Response: " + ex.getResponseBodyAsString());

            // No way to get the original message from this exception.
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail("Wrong Exception: " + ex.toString());
        }

    }

    private RetryPolicy createRetryPolicy() {

        SimpleRetryPolicy simple3 = new SimpleRetryPolicy(3);
        SimpleRetryPolicy simple0 = new SimpleRetryPolicy(0);

        Map<Class<? extends Throwable>, RetryPolicy> map = new HashMap<>();
        map.put(ResourceAccessException.class, simple3);
        map.put(HttpMessageNotReadableException.class, simple0);

        ExceptionClassifierRetryPolicy policy = new ExceptionClassifierRetryPolicy();
        policy.setPolicyMap(map);

        return policy;
    }

    private RestTemplate createRestTemplate(final int readTimeoutInMillis)  {

        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(5000);
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

        interceptors.add(new ReliantRestClientBodyInterceptor());

        return rt;
    }

}
