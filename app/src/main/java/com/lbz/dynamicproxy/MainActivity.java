package com.lbz.dynamicproxy;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lbz.dynamicproxy.annotation.BindView;
import com.lbz.dynamicproxy.annotation.OnClick;
import com.lbz.dynamicproxy.net.CustomRetrofit;
import com.lbz.dynamicproxy.net.WeatherService;
import com.lbz.dynamicproxy.utils.InjectView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    public static final String KEY = "ae6c53e2186f33bbf240a12d80672d1b";

    @BindView(R.id.result)
    TextView resultView;

    private CustomRetrofit customRetrofit;
    private WeatherService weatherService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InjectView.inject(this);
        initRetrofit();
    }

    private void initRetrofit() {
        customRetrofit = new CustomRetrofit.Builder().baseUrl("https://restapi.amap.com").build();
        weatherService = customRetrofit.create(WeatherService.class);
    }


    @OnClick(R.id.http_get)
    public void getClick() {
        Call call = weatherService.getWeather("110102", KEY);
        call.enqueue(callback);
    }

    @OnClick(R.id.http_post)
    public void postClick() {
        Call call = weatherService.postWeather("110101", KEY);
        call.enqueue(callback);
    }

    private Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            runOnUiThread(() -> resultView.setText(e.getMessage()));

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body != null) {
                    String result = body.string();
                    runOnUiThread(() -> resultView.setText(result));
                }
            } else {
                runOnUiThread(() -> resultView.setText("ERROR"));
            }
        }
    };

}