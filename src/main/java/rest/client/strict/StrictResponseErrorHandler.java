package rest.client.strict;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;


/**
 * The Class StrictResponseErrorHandler.
 */
public class StrictResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected boolean hasError(final HttpStatus statusCode) {
        return (statusCode.series() != HttpStatus.Series.SUCCESSFUL);
    }

    @Override
    public void handleError(final ClientHttpResponse response) throws IOException {

        // TODO Manage Http-Status 503, and handle retry in a better way ?
        int statusCode = response.getRawStatusCode();

        if ((statusCode >= 100  &&  statusCode <= 199)
                || (statusCode >= 300  &&  statusCode <= 399)) {

            throw new HttpNot2xxStatusCodeException(
                        response.getStatusCode()
                        , "Status code: " + statusCode
                        , response.getHeaders()
                        , getResponseBody(response)
                        , getCharset(response)
                        );
        }
        else {
            super.handleError(response);
        }
    }
}
