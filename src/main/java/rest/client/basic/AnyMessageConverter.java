package rest.client.basic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public class AnyMessageConverter extends AbstractHttpMessageConverter<Object> {

    private static final Logger log = LoggerFactory.getLogger(AnyMessageConverter.class);

    /**
     * Constructor.
     */
    public AnyMessageConverter() {
        super(MediaType.ALL);
    }

    @Override
    protected boolean supports(final Class<?> pClazz) {
        return true;
    }

    @Override
    protected Object readInternal(final Class<?> pClazz, final HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {

        List<String> list = inputMessage.getHeaders().get(HttpHeaders.CONTENT_TYPE);
        log.error("An un-managed media type arrived {} here is the content: ", list);

        try (InputStreamReader reader = new InputStreamReader(inputMessage.getBody(), getCharset(inputMessage));
             BufferedReader br = new BufferedReader(reader)) {

            int count = 0;
            char[] chars = new char[80];

            do {

                int charsRead = reader.read(chars);
                if (charsRead == -1) {
                    break;
                }

                count += charsRead;
                if (count > 240) {
                    System.out.println("...");
                    break;
                }

                System.out.print(new String(chars, 0, charsRead));

            }
            while (true);

            System.out.println();
            System.out.flush();

        }
        catch (Exception ex) {
            log.error("Could not transform input to String, body will not be shown", ex);
        }

        throw new HttpMessageNotReadableException("AnyConverter is used when no suitable converter is"
                + " available, probably because an unexpected mediaType arrived."
                + " MediaType: " + list
                );
    }

    @Override
    protected void writeInternal(final Object pT, final HttpOutputMessage pOutputMessage)
            throws IOException, HttpMessageNotWritableException {

        throw new HttpMessageNotWritableException("AnyConverter should not be used for writing message,"
                + " if this happens something is wrong in your code"
                );

    }

    /**
     * Gets the charset.
     *
     * @param message the message
     * @return the charset
     */
    private Charset getCharset(final HttpInputMessage message)
    {
        return Optional.ofNullable(message.getHeaders().getContentType())
            .map(MediaType::getCharset)
            .orElse(Charset.defaultCharset());
    }


}
