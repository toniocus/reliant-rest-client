package rest.client.retrofit;

import com.fasterxml.jackson.databind.JsonNode;

import rest.client.models.ModelPerson;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
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

    @GET("json")
    Call<String> jsonAsString();

    @GET("noJsonError")
    Call<JsonNode> noJsonError();

    @GET("status300")
    Call<String> status300();
    
    @GET("status500")
    Call<String> status500();

    @POST("posts/ack")
    Call<JsonNode> postsAck(@Body JsonNode body);

    @GET("person/{name}")
    Call<JsonNode> getPerson(@Path("name") String name);

    @GET("person/{name}")
    Call<ModelPerson> getPersonModel(@Path("name") String name);

    @POST("person")
    Call<JsonNode> storePerson(@Body ModelPerson person);



}
