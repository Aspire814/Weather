package cn.aspire.weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cn.aspire.weather.pojo.City;
import cn.aspire.weather.pojo.Country;
import cn.aspire.weather.pojo.Province;
import cn.aspire.weather.util.Utility;

public class MainActivity extends AppCompatActivity {
    /**
     * 级别常量
     */
    public static final int LEVEL_PROVINCE = 0;

    public static final int LEVEL_CITY = 1;

    public static final int LEVEL_COUNTY = 2;
    /**
     * 用于显示的数据
     */
    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 乡列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<Country> countyList;

    /**
     * 选中的省
     */
    private Province selectedProvince;

    /**
     * 选中的市
     */
    private City selectedCity;
    /**
     * 当前选中的级别
     */
    public int currentLevel;
    private ListView lv_itemIfo;
    private Button back_button;
    private TextView title_text;
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setView();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.
                simple_list_item_1, dataList);
        lv_itemIfo.setAdapter(adapter);
        SharedPreferences sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        String weatherString = sp.getString("weather", null);
        if (!TextUtils.isEmpty(weatherString)) {
            Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
            startActivity(intent);
        } else {
            queryProvinces();
        }
    }

    private void queryProvinces() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title_text.setText("中国");
                back_button.setVisibility(View.GONE);
                provinceList = DataSupport.findAll(Province.class);
                if (provinceList.size() > 0) {
                    dataList.clear();//清除dataList中的数据，并且加载当前等级的数据
                    for (Province province : provinceList) {
                        dataList.add(province.getProvinceName());//加载省份名称
                    }
                    adapter.notifyDataSetChanged();
                    lv_itemIfo.setSelection(0);//设置默认选中第一行
                    currentLevel = LEVEL_PROVINCE;//设置当前等级为省级
                } else {
                    //数据库中没有该省份数据，需要远程加载
                    String address = "http://guolin.tech/api/china";
                    queryFromServer(address, LEVEL_PROVINCE);
                }
            }
        });
    }

    /**
     * 查询选中省所有市，优先从数据库查询，如果没有再到服务器查询
     */
    private void queryCities() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title_text.setText(selectedProvince.getProvinceName());
                back_button.setVisibility(View.VISIBLE);
                cityList = DataSupport.where("provinceid = ?", selectedProvince.getId() + "").find(City.class);
                if (cityList.size() > 0) {
                    dataList.clear();
                    for (City city : cityList) {
                        dataList.add(city.getCityName());
                    }
                    adapter.notifyDataSetChanged();
                    lv_itemIfo.setSelection(0);//设置默认选中第一行
                    currentLevel = LEVEL_CITY;
                } else {
                    String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode();
                    queryFromServer(address, LEVEL_CITY);
                }
            }
        });
    }

    /**
     * 查询选中县所有市，优先从数据库查询，如果没有再到服务器查询
     */
    private void queryCounties() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title_text.setText(selectedCity.getCityName());
                back_button.setVisibility(View.VISIBLE);
                countyList = DataSupport.where("cityid = ?", selectedCity.getId() + "").find(Country.class);
                if (countyList.size() > 0) {
                    dataList.clear();
                    for (Country country : countyList) {
                        dataList.add(country.getCountryName());
                    }
                    adapter.notifyDataSetChanged();
                    //lv_itemIfo.setSelection(0);//设置默认选中第一行
                    currentLevel = LEVEL_COUNTY;
                } else {
                    String address = "http://guolin.tech/api/china/" + selectedProvince.getProvinceCode()
                            + "/" + selectedCity.getCityCode();
                    queryFromServer(address, LEVEL_COUNTY);
                }
            }
        });

    }

    private void queryFromServer(final String address, final int level) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestProperty("Charset", "UTF-8");
                    urlConnection.setConnectTimeout(5000);
                    int code = urlConnection.getResponseCode();
                    if (code == 200) {
                        InputStream is = urlConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "Utf-8"));
                        StringBuffer stringBuffer = new StringBuffer();
                        String line = null;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuffer.append(line);
                        }
                        String responseString = stringBuffer.toString();
                        boolean result = false;
                        switch (level) {
                            //解析json格式的字符串数据
                            case LEVEL_CITY:
                                result = Utility.handCityResponse(responseString,
                                        selectedProvince.getId());
                                if (result) {
                                    queryCities();
                                }
                                break;
                            case LEVEL_COUNTY:
                                result = Utility.handleCountryResponse(responseString,
                                        selectedCity.getId());
                                if (result) {
                                    queryCounties();
                                }
                                break;
                            case LEVEL_PROVINCE:
                                result = Utility.handleProvinceResponse(responseString);
                                if (result) {
                                    queryProvinces();
                                }
                                break;
                            default:
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void setView() {
        lv_itemIfo = (ListView) findViewById(R.id.lv_itemIfo);
        back_button = (Button) findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (currentLevel) {
                    case LEVEL_CITY:
                        currentLevel = LEVEL_PROVINCE;
                        queryProvinces();
                        break;
                    case LEVEL_COUNTY:
                        currentLevel = LEVEL_CITY;
                        queryCities();
                        break;
                    case LEVEL_PROVINCE:
                        break;
                    default:
                }
            }
        });
        title_text = (TextView) findViewById(R.id.title_text);
        lv_itemIfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                    intent.putExtra("weatherId", weatherId);
                    startActivity(intent);
                }
            }
        });
    }
}
