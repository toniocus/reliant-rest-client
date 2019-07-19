package rest.client.retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public final class JacksonConverterFactoryForTA extends Converter.Factory {

    private JacksonConverterFactory delegate;
    private ObjectMapper mapper = null;
    private boolean cacheBodyInString = false;

    /**
     * Instantiates a new jackson converter factory for TA.
     */
    private JacksonConverterFactoryForTA() {
    }

    /**
     * Creates an instance of JacksonConverterFactoryForTA with default
     * Jackson ObjectMapper, and cacheBodyInString = true;
     *
     * @return the jackson converter factory for TA
     * @see #create(ObjectMapper, boolean)
     */
    public static JacksonConverterFactoryForTA create() {
        return create(new ObjectMapper(), true);
    }

    /**
     * Creates an instance of JacksonConverterFactoryForTA with default
     * Jackson ObjectMapper, and cacheBodyInString = false;

     *
     * @return the jackson converter factory for TA
     * @see #create(ObjectMapper, boolean)
     */
    public static JacksonConverterFactoryForTA createConverterForStreaming() {
        return create(new ObjectMapper(), false);
    }

    /**
     * Creates an instance of JacksonConverterFactoryForTA with given
     * Jackson ObjectMapper, and cacheBodyInString = true;
     *
     *
     * @param mapper the mapper, if null default ObjectMapper will be used.
     * @return the jackson converter factory for TA
     * @see #create(ObjectMapper, boolean)
     */
    public static JacksonConverterFactoryForTA create(final ObjectMapper mapper) {
        return create(mapper, true);
    }

    /**
     * Creates an instance of JacksonConverterFactoryForTA with given
     * Jackson ObjectMapper, and cacheBodyInString = false;
     *
     *
     * @param mapper the mapper, if null default ObjectMapper will be used.
     * @return the jackson converter factory for TA
     * @see #create(ObjectMapper, boolean)
     */
    public static JacksonConverterFactoryForTA createConverterForStreaming(final ObjectMapper mapper) {
        return create(mapper, false);
    }

    /**
     * Creates an instance of JacksonConverterFactoryForTA with given
     * Jackson ObjectMapper and cacheBodyInString.
     *
     * @param mapper the mapper if null default ObjectMapper will be created
     *
     * @param cacheBodyInString if <b>true</b> this parameter makes the converter to store first the response in an String
     * and then parse it from it, in this way in case of converter error we can show the received response in the log,
     * if <b>false</b> ObjectMapper uses the response stream to read it and <b>no</b> information about the request
     * can be fetched in case of parsing error.
     *
     * @return the jackson converter factory for TA
     */
    public static JacksonConverterFactoryForTA create(final ObjectMapper mapper, final boolean cacheBodyInString) {

        JacksonConverterFactoryForTA conv = new JacksonConverterFactoryForTA();
        conv.mapper = (mapper == null ? new ObjectMapper() : mapper);
        conv.cacheBodyInString = cacheBodyInString;
        conv.delegate = JacksonConverterFactory.create(conv.mapper);

        return conv;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(final Type type, final Annotation[] annotations,
        final Retrofit retrofit) {
      JavaType javaType = this.mapper.getTypeFactory().constructType(type);
      ObjectReader reader = this.mapper.readerFor(javaType);
      return new JacksonResponseBodyConverterForTA<>(reader, this.cacheBodyInString);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(final Type type,
        final Annotation[] parameterAnnotations, final Annotation[] methodAnnotations, final Retrofit retrofit) {

        return this.delegate.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }
}