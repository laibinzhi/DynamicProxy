package com.lbz.dynamicproxy.net;

import com.lbz.dynamicproxy.annotation.Field;
import com.lbz.dynamicproxy.annotation.GET;
import com.lbz.dynamicproxy.annotation.POST;
import com.lbz.dynamicproxy.annotation.Query;

import okhttp3.Call;

/**
 * @author: laibinzhi
 * @date: 2022-04-22 16:47
 * @desc:
 */
public interface WeatherService {
    @POST("/v3/weather/weatherInfo")
    Call postWeather(@Field("city") String city, @Field("key") String key);

    @GET("/v3/weather/weatherInfo")
    Call getWeather(@Query("city") String city, @Query("key") String key);
}
