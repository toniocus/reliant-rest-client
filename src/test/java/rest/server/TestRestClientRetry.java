package rest.server;

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
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import rest.client.basic.ReliantRestClientBodyInterceptor;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@SpringBootTest
public class TestRestClientRetry {

    @BeforeClass
    public static void start() {
        DemoApplication.main("");
    }

    @AfterClass
    public static void end() {
        DemoApplication.shutdown();
    }

    @Test
    public void testConnect() throws Exception {

         RestTemplate rt = createRestTemplate(5000);

         RetryPolicy policy = createRetryPolicy();

         // Use the policy...
         RetryTemplate template = new RetryTemplate();
         template.setRetryPolicy(policy);

         ResponseEntity<String> result = template.execute(new RetryCallback<ResponseEntity<String>, Exception>() {

             @Override
            public ResponseEntity<String> doWithRetry(final RetryContext context) throws Exception {

                 System.out.println("******** Calling timeout service: " + context.getRetryCount());

                 if (context.getLastThrowable() != null) {
                     System.out.println(" Las Exception was: " + context.getLastThrowable().getClass().getSimpleName());
                 }

                 return rt.getForEntity("http://localhost:9090/timeout/20", String.class);
             }
         }
         , new RecoveryCallback<ResponseEntity<String>>() {

            @Override
            public ResponseEntity<String> recover(final RetryContext pContext) throws Exception {
                return ResponseEntity.badRequest().body("No pude lograrlo, sorry guys.");
            }

        });

        System.out.println("Recived message: " +  result.getBody());
    }

    @Test
    public void testNoJson() throws Exception {

         RestTemplate rt = createRestTemplate(5000);
         RetryPolicy policy = createRetryPolicy();

         // Use the policy...
         RetryTemplate template = new RetryTemplate();
         template.setRetryPolicy(policy);

         ResponseEntity<JsonNode> result = template.execute(new RetryCallback<ResponseEntity<JsonNode>, Exception>() {

             @Override
            public ResponseEntity<JsonNode> doWithRetry(final RetryContext context) throws Exception {

                 System.out.println("******** Calling timeout service: " + context.getRetryCount());

                 if (context.getLastThrowable() != null) {
                     System.out.println(" Las Exception was: " + context.getLastThrowable().getClass().getSimpleName());
                 }

                 return rt.getForEntity("http://localhost:9090/noJson", JsonNode.class);
             }
         }
         , new RecoveryCallback<ResponseEntity<JsonNode>>() {

            @Override
            public ResponseEntity<JsonNode> recover(final RetryContext pContext) throws Exception {
                return ResponseEntity.badRequest()
                        .body(JsonNodeFactory.instance.objectNode().put("error", "sorry, no lo logre"));
            }

        });

        System.out.println("Recived message: " +  result.getBody());

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
