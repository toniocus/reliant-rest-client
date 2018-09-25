package rest.server;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import rest.client.basic.ReliantRestClientBodyInterceptor;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@SpringBootTest
public class TestRestClient {

    @BeforeClass
    public static void start() {
        DemoApplication.main("");
    }

    @AfterClass
    public static void end() {
        DemoApplication.shutdown();
    }

    @Test
    public void testOK() {

        RestTemplate rt = createRestTemplate(60_000);

        ResponseEntity<String> response = rt.getForEntity("http://localhost:9090/ok", String.class);

        System.out.println(response.getBody());

    }

    @Test
    public void testEmpty() {

        RestTemplate rt = createRestTemplate(60_000);

        ResponseEntity<String> response = rt.getForEntity("http://localhost:9090/empty", String.class);

        System.out.println(response.getBody());

    }

    @Test
    public void testConnect() throws IOException, RestClientException, URISyntaxException {

        try {

            System.out.println("Calling service....");

            RestTemplate rt = createRestTemplate(5000);
            rt.getForEntity("http://localhost:9090/timeout/20", String.class);

        }
        catch (ResourceAccessException stx) {
            System.out.println("Read timeout Timeout correctly received...." + stx.toString()
                + " bytes: " + ((SocketTimeoutException) stx.getCause()).bytesTransferred);

            Assert.assertTrue("Read timed out", stx.toString().toLowerCase().contains("read timed out"));
        }
        catch(Exception ex) {
            Assert.fail("Wrong Exception is thrown: " + ex.toString());
        }


    }

    @Test
    public void testNoJson() throws Exception {

        RestTemplate rt = createRestTemplate(10_000);

        try {
            ResponseEntity<JsonNode> response = rt.getForEntity("http://localhost:9090/noJson", JsonNode.class);

            System.out.println(response.getBody());
        }
        catch (HttpMessageNotReadableException ex) {

            // No way to get the original message from this exception.
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail("Wrong Exception: " + ex.toString());
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
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail("Wrong Exception: " + ex.toString());
        }

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
