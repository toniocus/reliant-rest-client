package web.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;
import rest.client.ReliantDemoApplication;

/**
 * DOCUMENT .
 *
 * @author tonioc
 *
 */
public class WebClientTest extends Assertions {

    private static final Logger log = LoggerFactory.getLogger(WebClientTest.class);

    private static WebTestClient testClient;
    private WebClient client;
    private HttpClient httpClient;

    @BeforeAll
    public static void start() {
        ReliantDemoApplication.main("");
//        testClient = WebTestClient
//                .bindToApplicationContext(ReliantDemoApplication.getContext())
//                .build();
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
    protected WebClient getClient() {

        if (this.client == null) {
            this.httpClient = HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .responseTimeout(Duration.ofMillis(5000))
                    .wiretap(true)
                    .wiretap("webclient.log", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                    .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

            this.client = WebClient.builder()
                    .baseUrl("http://localhost:9090")
                    .defaultCookie("cookieKey", "cookieValue")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultUriVariables(Collections.singletonMap("url", "http://localhost:9090"))
                    .clientConnector(new ReactorClientHttpConnector(this.httpClient))
                    .build();
        }

        return this.client;
    }

    @Test
    void testGetPerson() throws Exception {

        System.out.println("Running getPerson... ");
//        Mono<ResponseEntity<JsonNode>> mono =
//        Mono<JsonNode> mono =
            getClient()
            .get()
            .uri("person/{name}", "name", "Andres")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .toEntity(JsonNode.class)
//            .exchangeToMono(r -> {
//                if (r.statusCode().value() / 200 == 1) {
//                    return r.bodyToMono(JsonNode.class);
//                }
//                else {
//                    return r.createException().flatMap(Mono::error);
//                }
//            })
            .doOnNext(System.out::println)
            .doOnError(System.err::println)
            .block();



    }

    @Test
    void testGetPersons() throws Exception {

        System.out.println("Running getPerson... ");

        List<Mono<JsonNode>> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Mono<JsonNode> mono = getClient()
                .get()
                .uri("person/{name}", "Andres-" + (i+1))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(r -> {
                    if (r.statusCode().value() / 200 == 1) {
                        return r.bodyToMono(JsonNode.class);
                    }
                    else {
                        return r.createException().flatMap(Mono::error);
                    }
                })
                .doOnNext(n -> log.info("{}", n));

            list.add(mono);
        }

        Mono.when(list).block();
    }

}
