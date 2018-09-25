package rest.client.basic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
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
public class ReliantRestClientBodyInterceptor implements ClientHttpRequestInterceptor {

    private byte[] responseBody;
    private Charset charset;

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
        this.charset = getCharset(response);

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

    /**
     * Gets the response body as string, if not able to convert to String will send
     * the byte array.
     *
     * @return the response body as string
     */
    public String getResponseBodyAsString() {

        try {
            return new String(this.responseBody, this.charset);
        }
        catch (Exception ex) {
            // Not able to transform to String,  send bytes
            return "Cannot convert byte[] to String, hera are the bytes: " + Arrays.toString(this.responseBody);
        }

    }

    /**
     * Gets the charset.
     *
     * @param message the message
     * @return the charset
     */
    private static Charset getCharset(final ClientHttpResponse message)
    {
        return Optional.ofNullable(message.getHeaders().getContentType())
            .map(MediaType::getCharset)
            .orElse(Charset.defaultCharset());
    }

}
