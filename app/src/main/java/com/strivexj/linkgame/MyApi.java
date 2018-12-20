package com.strivexj.linkgame;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by cwj on 5/28/18 22:07
 */
public interface MyApi {
    String HOST = "https://strivexj.com/";

    @GET("linkGameRanking")
    Call<ResponseBody> getRankingList();

    @POST("linkGameRanking")
    @FormUrlEncoded
    Call<ResponseBody> uploadRecord(@Field("username") String username, @Field("type") int type, @Field("record") int record, @Field("date") String date);

}
