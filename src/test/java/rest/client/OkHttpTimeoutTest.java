package rest.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.web.client.RestClientException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpTimeoutTest extends Assert {

    private static ServerSocket serverSocket;

    private OkHttpClient client = new OkHttpClient();

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
    //@Ignore("Ignored because the time it takes, comment out to try")
    public void testConnectTimeout() throws IOException, RestClientException, URISyntaxException {

        long start = 0L, stop = 0L;

        try {
            OkHttpClient cl = this.client.newBuilder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder().url("http://10.255.255.1").build();

            try (Response response = cl.newCall(request).execute()) {

                if (! response.isSuccessful()) {
                    throw new RuntimeException("Error" + response);
                }
                else {
                    System.out.println(response.body().string());
                }
            }

            fail("No exception thrown");
        }
        catch (IOException ex) {
            stop = System.currentTimeMillis();
            System.out.println(ex.toString());
        }


    }

}