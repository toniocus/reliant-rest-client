package rest.client.retrofit;

import java.net.SocketTimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import rest.client.ReliantDemoApplication;
import rest.client.models.ModelPerson;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@SpringBootTest
public class RetrofitTest extends Assertions {

    private static final Logger log = LoggerFactory.getLogger(RetrofitTest.class);
    private static OkHttpClient singletonHttpClient;
    private static OkHttpListener listener = new OkHttpListener();

    @BeforeAll
    public static void start() {

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(10);

        singletonHttpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .eventListener(listener)
                .build();

        ReliantDemoApplication.main("");
    }

    @AfterAll
    public static void end() {
        ReliantDemoApplication.shutdown();
    }

    /**
     * Gets the service.
     *
     * @param secondsReadTimeout the seconds read timeout
     * @return the service
     */
    protected RetrofitTestService getService(final int secondsReadTimeout, final boolean logHttp) {

        OkHttpClient client = null;

        if (logHttp) {

            HttpLoggingInterceptor interceptor =
                    new HttpLoggingInterceptor(msg -> System.out.println("RF: " + msg));
            interceptor.level(Level.BASIC);

            client = this.singletonHttpClient.newBuilder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(secondsReadTimeout, TimeUnit.SECONDS)
                    .addInterceptor(interceptor)
                    .build();
        }
        else {

            client = this.singletonHttpClient.newBuilder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(secondsReadTimeout, TimeUnit.SECONDS)
                    .build();

        }

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
            RetrofitTestService service = getService(10, false);

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
            RetrofitTestService service = getService(30, false);

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
        RetrofitTestService service = getService(30, false);

        Response<JsonNode> response = service.noJsonError().execute();

        assertFalse("Ok bad request", response.isSuccessful());
        System.out.println(response.errorBody().string());
    }

    /**
     * Test receiving json as string.
     *
     * @throws Exception the exception
     */
    @Test
    public void testReceivingJsonAsString() throws Exception {

        System.out.println("Running jsonAsString... ");
        RetrofitTestService service = getService(30, true);

        Response<String> response = service.jsonAsString().execute();

        assertTrue("Ok request", response.isSuccessful());
        System.out.println(response.body());
    }

    /**
     * Test status 300.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStatus300() throws Exception {


        System.out.println("Running Status300... ");
        RetrofitTestService service = getService(30, false);

        Response<String> execute = service.status300().execute();

        assertFalse("Ok bad request", execute.isSuccessful());
        System.out.println(execute.errorBody().string());

    }

    @Test
    public void testStatus500() throws Exception {

        System.out.println("Running Status500... ");
        RetrofitTestService service = getService(10, true);

        Response<String> response = service.status500().execute();

        assertFalse("Ok server error", response.isSuccessful());
        assertEquals(500, response.code(), "HTTP Status Code 500");
    }

	@Test
    public void testPostAck() throws Exception {


        System.out.println("Running postAck... ");
        RetrofitTestService service = getService(30, true);

        ObjectNode request = JsonNodeFactory.instance.objectNode()
            .put("code", 1001L)
            .put("name", "Alejandro Valverde");

        Response<JsonNode> execute = service.postsAck(request).execute();

        assertTrue("Ok request", execute.isSuccessful());

    }

    @Test
    public void testGetPerson() throws Exception {

        System.out.println("Running getPerson... ");
        RetrofitTestService service = getService(30, true);

        Response<JsonNode> execute = service.getPerson("Andres").execute();

        assertTrue("Ok request", execute.isSuccessful());

    }

    @Test
    public void testGetPersonModel() throws Exception {

        System.out.println("Running getPerson... ");
        RetrofitTestService service = getService(30, true);

        Response<ModelPerson> execute = service
                .getPersonModel("Andres").execute();

        assertTrue("Ok request", execute.isSuccessful());
        System.out.println(execute.body());

    }

    @Test
    public void testGetPersonModelAsync() throws Exception {

        int max=200;
        System.out.println("Running getPerson... ");
        RetrofitTestService service = getService(30, true);

        CountDownLatch countDownLatch = new CountDownLatch(200);

        for (int i = 0; i < 200; i++ ) {

            if (listener.getCounter() >= 10) {
                log.info("********* WAITING");
                i--;
                Thread.sleep(500);
                continue;
            }

            service.getPersonModel("Andres " + (i+1)).enqueue(new Callback<ModelPerson>() {

                @Override
                public void onResponse(final Call<ModelPerson> call, final Response<ModelPerson> response) {
                    if (response.isSuccessful()) {
                        String tname=Thread.currentThread().getName();
                        log.info("{} Success call to person {}", call.request()
                                , tname.substring(tname.length()-10)
                                );
                    }
                    else {
                        log.info("Error in call {}", response.code());
                    }

                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(final Call<ModelPerson> call, final Throwable t) {
                    log.error("Error", t);
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();
    }

    @Test
    public void testGetPersonModelSync() throws Exception {

        System.out.println("Running getPerson... ");
        RetrofitTestService service = getService(30, true);

        for (int i = 0; i < 200; i++ ) {
            Response<ModelPerson> execute = service.getPersonModel("Andres " + (i+1)).execute();
            log.info("Success call {}", i);
        }
    }

    @Test
    public void testStorePerson() throws Exception {

        System.out.println("Running StorePerson... ");
        RetrofitTestService service = getService(30, true);

        ModelPerson person = new ModelPerson("Ricardo")
                .addAddr("Balbin", 2019)
                .addAddr("Jaramillo", 3361);

        Response<JsonNode> execute = service.storePerson(person).execute();

        assertTrue("Ok request", execute.isSuccessful());
        System.out.println(execute.body());

    }

    void assertTrue(final String msg, final boolean b) {
        Assertions.assertTrue(b, msg);
    }

    void assertFalse(final String msg, final boolean b) {
        Assertions.assertFalse(b, msg);
    }

}
