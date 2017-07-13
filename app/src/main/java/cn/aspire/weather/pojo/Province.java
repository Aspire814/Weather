package cn.aspire.weather.pojo;

import org.litepal.crud.DataSupport;

/**
 * 创建人： Aspire
 * 创建时间： 2017/7/10 0010.
 *
 * @版本 ：1.0
 * @描述 ：省份
 * }
 */

public class Province extends DataSupport{
    private int id;
    private String provinceName;
    private int provinceCode;

    public void setId(int id) {
        this.id = id;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public int getId() {
        return id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }
}
