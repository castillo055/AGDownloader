package loc.inhouse.agdownloader.scrapper;

import retrofit2.Call;
import okhttp3.ResponseBody;
import retrofit2.http.*;

public interface uc3mAPI {

    //@GET("/")
    //@Headers({
    //        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
    //        "Upgrade-Insecure-Requests: 1"
    //})
    //Call<ResponseBody> requestMainPage();

    @POST("/index.php/CAS/login")
    @FormUrlEncoded
    @Headers({
            "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Content-Type: application/x-www-form-urlencoded",
            "DNT: 1",
            "Origin: https://login.uc3m.es",
            "Referer: https://login.uc3m.es/index.php/CAS/login?service=https%3A%2F%2Faulaglobal.uc3m.es%2Flogin%2Findex.php",
            "Upgrade-Insecure-Requests: 1"
    })
    Call<ResponseBody> senLoginInfo(@Query("service") String service,
                                    @Field("adAS_i18n_theme") String theme,
                                    @Field("adAS_mode") String _mode,
                                    @Field("adAS_username") String username,
                                    @Field("adAS_password") String password);
}
