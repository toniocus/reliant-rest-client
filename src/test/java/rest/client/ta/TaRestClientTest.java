package rest.client.ta;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import rest.client.ReliantDemoApplication;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@SpringBootTest
public class TaRestClientTest extends Assertions {

    @BeforeAll
    public static void start() {
        ReliantDemoApplication.main("");
    }

    @AfterAll
    public static void end() {
        ReliantDemoApplication.shutdown();
    }

    /**
     * Test read timeout.
     *
     * @throws Exception the exception
     */
    @Test
    public void testReadTimeout() throws Exception {

        long start = 0L, stop = 0L;

        try {
            TaRestTemplate rrc = new TaRestTemplate(10, 5);

            start = System.currentTimeMillis();
            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/timeout/20", String.class));

        }
        catch (ResourceAccessException ex) {
            stop = System.currentTimeMillis();
            assertTrue(ex.getCause() instanceof SocketTimeoutException, "Timeout Exception");
        }
    }

    /**
     * Test status 100, this is a read-timeout.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStatus503() throws Exception {

        try {
            TaRestTemplate rrc = new TaRestTemplate(10, 5);

            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/status503", String.class));

            fail("No exception thrown");
        }
        catch (Exception ex) {
            assertTrue(ex instanceof HttpServerErrorException, "exception");
            assertEquals(503, ((HttpServerErrorException) ex).getRawStatusCode(), "503");
        }
    }


}
