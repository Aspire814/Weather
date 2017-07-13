package cn.aspire.weather.pojo;

import org.litepal.crud.DataSupport;

/**
 * 创建人： Aspire
 * 创建时间： 2017/7/10 0010.
 *
 * @版本 ：1.0
 * @描述 ：县
 * }
 */

public class Country extends DataSupport{
    private int id;
    private String countryName;
    private String weatherId;
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
