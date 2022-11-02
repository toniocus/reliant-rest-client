package rest.client.retrofit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RetrofitTimeoutTest extends Assertions {

    private static ServerSocket serverSocket;

    private final OkHttpClient client = new OkHttpClient();

    private static int port;

    @BeforeAll
    public static void beforeClass() throws IOException {
        // server socket with single element backlog queue (1) and dynamicaly allocated port (0)
        serverSocket = new ServerSocket(0, 1);
        // just get the allocated port
        port = serverSocket.getLocalPort();
        // fill backlog queue by this request so consequent requests will be blocked
        new Socket().connect(serverSocket.getLocalSocketAddress());
    }

    @AfterAll
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

            ConnectTimeoutService toService = new Retrofit.Builder()
                .baseUrl("http://10.255.255.1/")
                .client(cl)
                .build()
                .create(ConnectTimeoutService.class);

            Response<ResponseBody> response = toService.timeout().execute();
            fail("No exception thrown");
        }
        catch (Exception ex) {
            stop = System.currentTimeMillis();
            ex.printStackTrace();
            System.out.println(ex.toString());
        }


    }

}