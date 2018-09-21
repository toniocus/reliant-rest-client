package rest.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class ConnectTimeoutTest {

    private static ServerSocket serverSocket;

    private static int port;

    @BeforeClass
    public static void beforeClass() throws IOException {
        // server socket with single element backlog queue (1) and dynamicaly allocated port (0)
        serverSocket = new ServerSocket(0, 1);
        // just get the allocated port
        port = serverSocket.getLocalPort();
        // fill backlog queue by this request so consequent requests will be blocked
        new Socket().connect(serverSocket.getLocalSocketAddress());
    }

    @AfterClass
    public static void afterClass() throws IOException {
        // some cleanup
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Test
    public void testConnect() throws IOException, RestClientException, URISyntaxException {

        try {

            System.out.println("Calling service....");
            RestTemplate rt = createRestTemplate(100);
            rt.getForEntity("http://10.255.255.1", String.class);



        }
        catch (ResourceAccessException stx) {
            System.out.println("Connect Timeout correctly received...." + stx.toString()
                    + " bytes: " + ((SocketTimeoutException) stx.getCause()).bytesTransferred);
            Assert.assertTrue("Connect timed out", stx.toString().toLowerCase().contains("connect timed out"));
        }
        catch(Exception ex) {
            Assert.fail("Wrong Exception is thrown: " + ex.toString());
        }

        try {

            URL url = new URL("http://localhost:" + ConnectTimeoutTest.port); // use allocated port
            System.out.println("Calling service....");

            RestTemplate rt = createRestTemplate(100);
            rt.getForEntity(url.toURI(), String.class);

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

    private RestTemplate createRestTemplate(final long readTimeoutInMillis)  {

        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(5000);
        simpleClientHttpRequestFactory.setReadTimeout(5000);
        return new RestTemplate(simpleClientHttpRequestFactory);
    }

}