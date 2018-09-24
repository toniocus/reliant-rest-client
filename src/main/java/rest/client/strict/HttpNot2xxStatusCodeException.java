package rest.client.strict;

import java.nio.charset.Charset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * The Class HttpNot2xxStatusCodeException.
 */
public class HttpNot2xxStatusCodeException extends HttpStatusCodeException {

    private static final long serialVersionUID = 1111860013682463767L;

    /**
     * Instantiates a new http not 2 xx status code exception.
     *
     * @param pStatusCode the status code
     */
    public HttpNot2xxStatusCodeException(final HttpStatus pStatusCode) {
        super(pStatusCode);
    }

    /**
     * Instantiates a new http not 2 xx status code exception.
     *
     * @param pStatusCode the status code
     * @param pStatusText the status text
     */
    public HttpNot2xxStatusCodeException(final HttpStatus pStatusCode, final String pStatusText) {
        super(pStatusCode, pStatusText);
    }

    /**
     * Instantiates a new http not 2 xx status code exception.
     *
     * @param pStatusCode the status code
     * @param pStatusText the status text
     * @param pResponseBody the response body
     * @param pResponseCharset the response charset
     */
    public HttpNot2xxStatusCodeException(final HttpStatus pStatusCode, final String pStatusText,
            final byte[] pResponseBody, final Charset pResponseCharset) {
        super(pStatusCode, pStatusText, pResponseBody, pResponseCharset);
    }

    /**
     * Instantiates a new http not 2 xx status code exception.
     *
     * @param pStatusCode the status code
     * @param pStatusText the status text
     * @param pResponseHeaders the response headers
     * @param pResponseBody the response body
     * @param pResponseCharset the response charset
     */
    public HttpNot2xxStatusCodeException(final HttpStatus pStatusCode
            , final String pStatusText
            , final HttpHeaders pResponseHeaders
            , final byte[] pResponseBody
            , final Charset pResponseCharset) {

        super(pStatusCode, pStatusText, pResponseHeaders, pResponseBody, pResponseCharset);
    }

}
