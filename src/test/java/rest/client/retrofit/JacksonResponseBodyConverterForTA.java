/*
 *
 */
package rest.client.retrofit;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectReader;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * The Class JacksonResponseBodyConverterForTA.
 *
 * @author tonioc
 * @param <T> the generic type
 */
public class JacksonResponseBodyConverterForTA<T> implements Converter<ResponseBody, T> {
    private final ObjectReader adapter;
    private final boolean cacheBodyInString;

    private static final Logger log = LoggerFactory
            .getLogger(JacksonResponseBodyConverterForTA.class);

    /**
     * Instantiates a new jackson response body converter for TA.
     *
     * @param adapter the adapter
     * @param cacheBodyInString the cache body in string
     */
    JacksonResponseBodyConverterForTA(final ObjectReader adapter, final boolean cacheBodyInString) {
        this.adapter = adapter;
        this.cacheBodyInString = cacheBodyInString;
    }

    @Override
    public T convert(final ResponseBody value) throws IOException {

        String body = null;

        try {
            if (this.cacheBodyInString) {
                body = value.string();
                return this.adapter.readValue(body);
            }
            else {
                return this.adapter.readValue(value.charStream());
            }
        }
        catch (JsonProcessingException ex) {

            if (this.cacheBodyInString) {
                log.error("Error processing response body: " + body);
            }

            throw ex;
        }
        catch (IOException ex) {
            throw ex;
        }
        finally {
            value.close();
        }
    }
}
