package rest.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import rest.client.strict.StrictRestClient;

public class ConnectTimeoutTest extends Assert {

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
    @Ignore("Ignored because the time it takes, comment out to try")
    public void testConnectTimeout() throws IOException, RestClientException, URISyntaxException {

        long start = 0L, stop = 0L;

        try {
            StrictRestClient rrc = new StrictRestClient(5_000, 0);

            start = System.currentTimeMillis();
            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://10.255.255.1", String.class));

            System.out.println("Recived message: " +  result.getBody());
        }
        catch (Exception ex) {
            stop = System.currentTimeMillis();
            assertTrue("connect time out",  ex.getMessage().contains("connect timed out"));
        }

        assertTrue("Exception thrown", stop > 0);
        assertTrue("Retries occured", (stop - start) > 5_000 * 3);

    }

}