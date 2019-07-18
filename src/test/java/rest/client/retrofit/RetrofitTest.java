package rest.client.retrofit;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;

import okhttp3.OkHttpClient;
import rest.client.ReliantDemoApplication;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@SpringBootTest
public class RetrofitTest extends Assert {

    private OkHttpClient client = new OkHttpClient();

    @BeforeClass
    public static void start() {
        ReliantDemoApplication.main("");
    }

    @AfterClass
    public static void end() {
        ReliantDemoApplication.shutdown();
    }

    /**
     * Gets the service.
     *
     * @param secondsReadTimeout the seconds read timeout
     * @return the service
     */
    protected RetrofitTestService getService(final int secondsReadTimeout) {

        OkHttpClient client = this.client.newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(secondsReadTimeout, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl("http://localhost:9090/")
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactoryForTA.create(null, true))
                .build()
                .create(RetrofitTestService.class);

    }

    /**
     * Test read timeout.
     *
     * @throws Exception the exception
     */
    @Test
    public void testReadTimeout() throws Exception {

        try {

            System.out.println("Running ReadTimeout... ");
            RetrofitTestService service = getService(10);

            Call<String> timeout = service.timeout(20);

            timeout.execute();

            fail("No exception thrown");
        }
        catch (SocketTimeoutException ex) {
            System.out.println(ex.toString());
            assertTrue("Read timeout", ex.getMessage().toLowerCase().contains("timeout"));
        }

        //assertTrue("Read time out",  (stop - start) < 500 * 2);
    }

    /**
     * Test no json.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNoJson() throws Exception {

        try {

            System.out.println("Running NoJson... ");
            RetrofitTestService service = getService(30);

            Response<JsonNode> response = service.noJson().execute();

            fail("No exception thrown");
        }
        catch (JsonParseException ex) {
            System.out.println(ex);
            // OK
        }
    }

    /**
     * Test no json.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNoJsonError() throws Exception {

            System.out.println("Running NoJsonError... ");
            RetrofitTestService service = getService(30);

            Response<JsonNode> response = service.noJsonError().execute();

            assertFalse("Ok bad request", response.isSuccessful());

            System.out.println(response.errorBody().string());

    }

    /**
     * Test status 300.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStatus300() throws Exception {


        System.out.println("Running Status300... ");
        RetrofitTestService service = getService(30);

        Response<String> execute = service.status300().execute();

        assertFalse("Ok bad request", execute.isSuccessful());
        System.out.println(execute.errorBody().string());

    }


}
