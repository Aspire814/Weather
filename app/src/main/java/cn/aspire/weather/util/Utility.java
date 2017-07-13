package cn.aspire.weather.util;

import android.text.TextUtils;

import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.aspire.weather.gson.Weather;
import cn.aspire.weather.pojo.City;
import cn.aspire.weather.pojo.Country;
import cn.aspire.weather.pojo.Province;

/**
 * 创建人： Aspire
 * 创建时间： 2017/7/10 0010.
 *
 * @版本 ：1.0
 * @描述 ：用于解析json数据
 * }
 */

public class Utility {
    public Utility() {
    }

    /**
        *作者：@Aspire
        *创建时间：2017/7/11 0011 下午 4:36
        *描述：解析封装Json数据
    */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return  new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *作者：@Aspire
     *创建时间：2017/7/10 0010 下午 3:18
     *描述：解析省份的Json数据
     */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    /**
        *作者：@Aspire
        *创建时间：2017/7/10 0010 下午 3:20
        *描述：解析处理服务器返回的市级json数据
    */
    public static boolean handCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for(int i=0;i<allCities.length();i++){
                    JSONObject cityObject =allCities.getJSONObject(i);
                    City city =new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    /**
        *作者：@Aspire
        *创建时间：2017/7/10 0010 下午 3:32
        *描述：解析处理服务器返回的县级json数据
    */
    public static boolean handleCountryResponse(String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allCountries = new JSONArray(response);
                for(int i=0;i<allCountries.length();i++){
                    JSONObject countryObject =allCountries.getJSONObject(i);
                    Country country = new Country();
                    country.setCountryName(countryObject.getString("name"));
                    country.setWeatherId(countryObject.getString("weather_id"));
                    country.setCityId(cityId);
                    country.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
