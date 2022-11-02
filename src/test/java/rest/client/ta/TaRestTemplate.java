package rest.client.ta;

import java.time.Duration;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * TaRestTemplate a wrapper over RestTemplate with easy configurable timeouts and a standard
 * retry policy {@link TaRestRetryPolicy}, for HttpStatus 503 and connection timeout errors.
 *
 * <h2>Simple use: (See also {@link #execute(Function)} for a nicer sample)</h2>
 * <pre>
 * {@code
 * ....
 *    // Construct with default timeouts
 *    TaRestTemplate taRest = new TaRestTemplate(15, 90);
 *
 *    // Customize underlying RestTemplate
 *    RestTemplate rt = taRest.getRestTemplate();
 *    rt.whatever .....
 *
 *    // call your restemplate method as a lamba.
 *    ResponseEntity<String> result = taRest
 *          .execute(restTemplate -> restTemplate.getForEntity("http://localhost:9090/status100", String.class));
 *
 * ....
 * }
 * </pre>
 */
public class TaRestTemplate {

    private static final Logger log = LoggerFactory.getLogger(TaRestTemplate.class);

    private final RestTemplate restTemplate;
    private final TaRestRetryPolicy retryPolicy = new TaRestRetryPolicy(-1, -1);
    private int connectTO;
    private int readTO;

    /*
     * Instantiates a new rest template factory.
     *
     * @param pConnectTimeoutSeconds the connect timeout seconds
     * @param pReadTimeoutSeconds the read timeout seconds
     */
    public TaRestTemplate(final int connectTimeoutSeconds, final int readTimeoutSeconds) {

        this.connectTO = connectTimeoutSeconds;
        this.readTO = readTimeoutSeconds;


        this.restTemplate = new RestTemplateBuilder()
                .setReadTimeout(Duration.ofSeconds(readTimeoutSeconds))
                .setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
    }

    /**
     * Instantiates a new rest template factory.
     *
     * @param pRestTemplate the rest template
     * @param pConnectTimeoutSeconds the connect timeout seconds
     * @param pReadTimeoutSeconds the read timeout seconds
     */
    public TaRestTemplate(final RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    /**
     * Gets the rest template.
     *
     * @return the rest template
     */
    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    /**
     * Execute the provided lambda function that recieves {@link RestTemplate} as an argument.
     *
     * <h2>Simple use:</h2>
     * <pre>
     * {@code
     * ....
     *    // Construct with default timeouts
     *    TaRestTemplate taRest = new TaRestTemplate(15, 90);
     *
     *    // Using UriComponentBuilder from org.springframework.web.util
     *    // to build a URL
     *    // To generate: http://conciliator-batch.uat.ta.xom.dev.com/process/conciliate/vi?msisdn=XXXXX
     *    UriComponents uriComp = UriComponentsBuilder
     *         .fromHttpUrl("http://conciliator-batch.uat.ta.xom.dev.com")
     *         .pathSegment("process", "conciliate", "v1")
     *         .queryParam("msisdn", "0023459876")
     *         .build();
     *
     *    // call your restemplate method as a lamba, using uriComp.toUri() to get the URL
     *    ResponseEntity<String> result = taRest
     *          .execute(restTemplate -> restTemplate.getForEntity(uriComp.toUri(), String.class));
     *
     * ....
     * }
     * </pre>
     *
     *
     * @param <T> the generic type
     * @param function the function receiving a {@link RestTemplate} argument and returning a
     *    ResponseEntity.
     * @return the response entity
     */
    public <T> ResponseEntity<T> execute(final Function<RestTemplate, ResponseEntity<T>> function) {

        Validate.notNull(function, "Function argument should not be null");

        // Init retry policy in case the template is used more than once.
        this.retryPolicy.initPolicy();

        do {

            try {

                log.info("Calling restTemplate with [connectTimeout(Secs.):{}, readTimeout(Secs.):{}]"
                        , this.connectTO
                        , this.readTO
                        );

                return function.apply(this.restTemplate);
            }
            catch (ResourceAccessException ex) {

                // A way to differentiate connection timeout from read timeout.
                if (ex.toString().contains("connect timed out")) {

                    if (this.retryPolicy.retryWait()) {
                        log.info("Recieved Connection Timeout, retrying...");
                        continue;
                    }
                }

                throw ex;
            }
            catch (HttpStatusCodeException ex) {

                if (ex.getRawStatusCode() == 503) {

                    if (this.retryPolicy.retryWait()) {
                        log.info("Recieved HttpStatus 503, retrying...");
                        continue;
                    }
                }

                throw ex;
            }
        }
        while(true);
    }

}
