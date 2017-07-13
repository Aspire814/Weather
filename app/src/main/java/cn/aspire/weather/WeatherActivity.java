package cn.aspire.weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.aspire.weather.gson.Forecast;
import cn.aspire.weather.gson.Weather;
import cn.aspire.weather.util.Utility;

public class  WeatherActivity extends AppCompatActivity {
    private TextView tv_cityName;//当前城市名称
    private TextView tv_now_tmp;//当前温度
    private TextView tv_now_info;//当前天气描述
    private TextView tv_aqi_text;//当前aqi指数
    private TextView tv_pm25_text;//当前的pm2.5指数
    private TextView tv_suggession_comfort;//舒适度信息
    private TextView tv_suggession_carWash;//洗车建议
    private TextView tv_suggession_sport;//运动建议
    private TextView tv_forecast;
    private ImageView iv_home;
    private LinearLayout forecast_layout;
    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                Weather weather = (Weather) msg.obj;
                tv_cityName.setText(weather.basic.cityName);
                tv_now_tmp.setText(weather.now.temperature + "℃");
                tv_now_info.setText(weather.now.more.info);
                if (weather.aqi != null) {
                    tv_aqi_text.setText(weather.aqi.city.aqi);
                    tv_pm25_text.setText(weather.aqi.city.pm25);
                }
                tv_suggession_sport.setText(weather.suggestion.sport.info);
                tv_suggession_carWash.setText(weather.suggestion.carWash.info);
                tv_suggession_comfort.setText(weather.suggestion.comfort.info);
                for (Forecast forecast : weather.forecastList) {
                    View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.listitem, forecast_layout, false);
                    TextView tv_date_text = (TextView) view.findViewById(R.id.tv_date_text);
                    TextView tv_info = (TextView) view.findViewById(R.id.tv_info);
                    TextView tv_tmp_max = (TextView) view.findViewById(R.id.tv_tmp_max);
                    TextView tv_tmp_min = (TextView) view.findViewById(R.id.tv_tmp_min);
                    tv_date_text.setText(forecast.date);
                    tv_info.setText(forecast.more.info);
                    tv_tmp_max.setText(forecast.temperature.max);
                    tv_tmp_min.setText(forecast.temperature.min);
                    forecast_layout.addView(view);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        setView();
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String weatherString = sp.getString("weather", null);
        if (!TextUtils.isEmpty(weatherString)) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            Message msg = new Message();
            msg.obj = weather;
            msg.what = 0;
            myHandler.sendMessage(msg);
        } else {
            final String weatherId = getIntent().getStringExtra("weatherId");
            getWeatherInfo(weatherId);
        }
    }

    private void getWeatherInfo(final String weatherId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.getDoOutput();
                    urlConnection.setRequestProperty("Charset", "UTF-8");
                    urlConnection.setConnectTimeout(5000);
                    int code = urlConnection.getResponseCode();
                    if (code == 200) {
                        InputStream is = urlConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        StringBuffer stringBuffer = new StringBuffer();
                        String line = null;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuffer.append(line);
                        }
                        String responseString = stringBuffer.toString();
                        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("weather", responseString);
                        editor.commit();
                        Weather weather = Utility.handleWeatherResponse(responseString);
                        Message msg = new Message();
                        msg.obj = weather;
                        msg.what = 0;
                        myHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setView() {
        forecast_layout = (LinearLayout) findViewById(R.id.forecast_layout);
        tv_cityName = (TextView) findViewById(R.id.tv_cityName);
        tv_now_tmp = (TextView) findViewById(R.id.tv_now_tmp);
        tv_now_info = (TextView) findViewById(R.id.tv_now_info);
        tv_aqi_text = (TextView) findViewById(R.id.tv_aqi_text);
        tv_pm25_text = (TextView) findViewById(R.id.tv_pm25_text);
        tv_suggession_comfort = (TextView) findViewById(R.id.tv_suggession_comfort);
        tv_suggession_carWash = (TextView) findViewById(R.id.tv_suggession_carWash);
        tv_suggession_sport = (TextView) findViewById(R.id.tv_suggession_sport);
        tv_forecast = (TextView) findViewById(R.id.tv_forecast);
        iv_home = (ImageView) findViewById(R.id.iv_home);
        iv_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp=getSharedPreferences("config",Context.MODE_PRIVATE);
                sp.edit().clear().commit();
                Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
