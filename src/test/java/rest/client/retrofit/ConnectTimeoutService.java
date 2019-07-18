package rest.client.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public interface ConnectTimeoutService {

    @GET("xxx")
    Call<ResponseBody>  timeout();
}
