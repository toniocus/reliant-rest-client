package rest.client.basic;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * When an HttpMessageNotReadableException is thrown by RestTemplate, for example none
 * parsable Json, the body of the message is lost, need that for log info.
 *
 * <P>To be able to do this you need to use <i>BufferingClientHttpRequestFactory</i> so
 * getBody() InputStream is not traversed here, and lost during the next phase of processing.
 *
 */
public class ReliantRestClientInterceptor implements ClientHttpRequestInterceptor {

    private byte[] responseBody;

    /**
     * Intercept and store the message in this instance.
     *
     * @param request the request
     * @param body the body
     * @param execution the execution
     * @return the client http response
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public ClientHttpResponse intercept(final HttpRequest request
            , final byte[] body
            , final ClientHttpRequestExecution execution) throws IOException {

        ClientHttpResponse response = execution.execute(request, body);
        this.responseBody = IOUtils.toByteArray(response.getBody());
        return response;
    }


    /**
     * Gets the response body.
     *
     * @return the response body
     */
    public byte[] getResponseBody() {
        return this.responseBody;
    }

}
