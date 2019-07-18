package rest.client.retrofit;

import com.fasterxml.jackson.databind.JsonNode;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public interface RetrofitTestService {

    @GET("timeout/{seconds}")
    Call<String> timeout(@Path("seconds") int seconds);

    @GET("noJson")
    Call<JsonNode> noJson();

    @GET("noJsonError")
    Call<JsonNode> noJsonError();

    @GET("status300")
    Call<String> status300();

}
