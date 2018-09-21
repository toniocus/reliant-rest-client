package rest.server;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public class RestClientInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(final HttpRequest request
            , final byte[] body
            , final ClientHttpRequestExecution execution) throws IOException {

        ClientHttpResponse response = execution.execute(request, body);

        String s = IOUtils.toString(response.getBody());
        System.out.println("LOG DEBUG REST_RESPONSE: " + s);

        return response;
    }

}
