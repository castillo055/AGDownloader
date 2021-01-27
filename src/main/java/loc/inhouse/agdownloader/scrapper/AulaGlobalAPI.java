package loc.inhouse.agdownloader.scrapper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface AulaGlobalAPI {
    @GET("/login/index.php")
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Referer: https://login.uc3m.es/",
            "Upgrade-Insecure-Requests: 1"
    })
    Call<ResponseBody> sendLoginTicket(@Query("ticket") String ticket);

    @GET("/login/index.php")
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Referer: https://login.uc3m.es/",
            "Upgrade-Insecure-Requests: 1"
    })
    Call<ResponseBody> sendTestsessionRq();

    @GET("/")
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
            "Upgrade-Insecure-Requests: 1",
            "Referer: https://login.uc3m.es/"
    })
    Call<ResponseBody> requestMainPage();

    @GET("/course/view.php")
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Referer: https://aulaglobal.uc3m.es",
            "Upgrade-Insecure-Requests: 1"
    })
    Call<ResponseBody> requestCoursePage(@Query("id") String id);

    @GET("/mod/resource/view.php")
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Upgrade-Insecure-Requests: 1"
    })
    Call<ResponseBody> requestResourceLoc(@Header("Referer") String ref, @Query("id") String id);
}
