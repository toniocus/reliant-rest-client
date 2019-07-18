package rest.client.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
public interface TimeoutService {

    @GET("xxx")
    Call<ResponseBody>  timeout();
}
