package rest.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import rest.client.strict.ReliantRestClient;

public class TestReliantConnectTimeout {

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
            ReliantRestClient rrc = new ReliantRestClient();

            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://10.255.255.1", String.class));

            System.out.println("Recived message: " +  result.getBody());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}