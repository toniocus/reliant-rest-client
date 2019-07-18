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
    private boolean checkJsonBody = false;

    /**
     * Instantiates a new jackson converter factory for TA.
     */
    private JacksonConverterFactoryForTA() {
    }

    /**
     * Creates an instance of JacksonConverterFactoryForTA with default
     * Jackson ObjectMapper, and checkBody = true;
     *
     * @return the jackson converter factory for TA
     */
    public static JacksonConverterFactoryForTA create() {
        return create(new ObjectMapper(), false);
    }

    /**
     * Creates an instance of JacksonConverterFactoryForTA with given
     * Jackson ObjectMapper, and checkBody = true;
     *
     *
     * @param mapper the mapper
     * @return the jackson converter factory for TA
     */
    public static JacksonConverterFactoryForTA create(final ObjectMapper mapper) {
        return create(mapper, false);
    }

    /**
     * Creates an instance of JacksonConverterFactoryForTA with given
     * Jackson ObjectMapper and checkBody;
     *
     * @param mapper the mapper if null default ObjectMapper will be created
     * @param checkJsonBody the check json body, body is loaded in a String and message is
     * reported if a JsonParse happens.
     * @return the jackson converter factory for TA
     */
    @SuppressWarnings("ConstantConditions") // Guarding public API nullability.
    public static JacksonConverterFactoryForTA create(final ObjectMapper mapper, final boolean checkJsonBody) {

        JacksonConverterFactoryForTA conv = new JacksonConverterFactoryForTA();
        conv.mapper = (mapper == null ? new ObjectMapper() : mapper);
        conv.checkJsonBody = checkJsonBody;
        conv.delegate = JacksonConverterFactory.create(conv.mapper);

        return conv;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(final Type type, final Annotation[] annotations,
        final Retrofit retrofit) {
      JavaType javaType = this.mapper.getTypeFactory().constructType(type);
      ObjectReader reader = this.mapper.readerFor(javaType);
      return new JacksonResponseBodyConverterForTA<>(reader, this.checkJsonBody);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(final Type type,
        final Annotation[] parameterAnnotations, final Annotation[] methodAnnotations, final Retrofit retrofit) {

        return this.delegate.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }
}