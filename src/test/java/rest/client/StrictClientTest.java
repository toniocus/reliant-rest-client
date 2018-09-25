package rest.client;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.fasterxml.jackson.databind.JsonNode;

import rest.client.strict.HttpNot2xxStatusCodeException;
import rest.client.strict.StrictRestClient;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@SpringBootTest
public class StrictClientTest extends Assert {

    @BeforeClass
    public static void start() {
        ReliantDemoApplication.main("");
    }

    @AfterClass
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
            StrictRestClient rrc = new StrictRestClient(5);

            start = System.currentTimeMillis();
            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/timeout/20", String.class));

            fail("No exception thrown");
        }
        catch (Exception ex) {
            stop = System.currentTimeMillis();
            assertTrue("Read timeout", ex.getMessage().toLowerCase().contains("read timed out"));
        }

        assertTrue("Read time out",  (stop - start) < 10_000);
    }

    /**
     * Test no json.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNoJson() throws Exception {


        try {

            // TODO how to check the message body is logged ????

            StrictRestClient rrc = new StrictRestClient();

            ResponseEntity<JsonNode> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/noJson", JsonNode.class));

            fail("No exception thrown");
        }
        catch (HttpMessageNotReadableException ex) {
            // OK
        }
    }

    /**
     * Test status 300.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStatus300() throws Exception {


        try {
            StrictRestClient rrc = new StrictRestClient();

            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/status300", String.class));

            fail("No Exception thrown");
        }
        catch (Exception ex) {
            assertTrue("Exception OK", ex instanceof HttpNot2xxStatusCodeException);
        }
    }

    /**
     * Test status 100, this is a read-timeout.
     *
     * @throws Exception the exception
     */
    @Test
    public void testStatus100() throws Exception {

        long start = 0L, stop = 0L;

        try {
            StrictRestClient rrc = new StrictRestClient(5_000);

            start = System.currentTimeMillis();
            ResponseEntity<String> result = rrc
                    .execute(rt -> rt.getForEntity("http://localhost:9090/status100", String.class));

            fail("No exception thrown");
        }
        catch (Exception ex) {
            stop = System.currentTimeMillis();
            assertTrue("Read timeout", ex.getMessage().toLowerCase().contains("read timed out"));
        }

        assertTrue("Read time out",  (stop - start) < 10_000);
    }
}
