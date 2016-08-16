package com.weather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.weather.app.R;
import com.weather.app.model.City;
import com.weather.app.model.County;
import com.weather.app.model.Province;
import com.weather.app.model.WeatherDB;
import com.weather.app.util.HttpCallbackListener;
import com.weather.app.util.HttpUtil;
import com.weather.app.util.LogUtil;
import com.weather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity implements OnItemClickListener{
	public static final String TAG="ChooseAreaActivity";
	
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String>adapter;
	private WeatherDB weatherDB;
	private List<String>dataList=new ArrayList<String>();
	
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	/**
	 * 市列表
	 */
	private List<City> cityList;
	/**
	 * 县列表
	 */
	private List<County> countyList;
	
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	
	/**
	 * 选中的城市
	 */
	private City selectedCity;
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		boolean from_weather_activity=getIntent().getBooleanExtra("from_weather_activity", false);
		if(!from_weather_activity){
			//如果不是从WeatherActivity点击了Home键来到这个页面的，就说明是刚刚启动
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			if(prefs.getBoolean("city_selected", false)){
				//如果当前存储了本地天气信息,就直接显示本地天气信息
				Intent intent=new Intent(this,WeatherActivity.class);
				startActivity(intent);
				finish();
				return;
			}
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		weatherDB=WeatherDB.getInstance(this);
		listView.setOnItemClickListener(this);
		
		queryProvinces();//加载省级数据
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
		// TODO Auto-generated method stub
		if(currentLevel==LEVEL_PROVINCE){
			selectedProvince=provinceList.get(index);
			LogUtil.d(TAG, "You clicked a province...");
			queryCities();
		}
		else if(currentLevel==LEVEL_CITY){
			selectedCity=cityList.get(index);
			LogUtil.d(TAG, "You clicked a city...");
			queryCounties();
		}
		else if(currentLevel==LEVEL_COUNTY){
			String countyCode=countyList.get(index).getCountyCode();
			Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
			intent.putExtra("county_code", countyCode);
			startActivity(intent);
			finish();
		}
	}
	
	/**
	 * 查询全国所有的省，优先从数据库查询,如果没有查询到再去服务器上查询
	 */
	private void queryProvinces(){
		provinceList=weatherDB.loadProvinces();
		if(provinceList.size()>0){
			LogUtil.d(TAG, "ChooseAreaActivity: There are provinces stored");
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}
		else{
			queryFromServer(null,"province");
		}
	}
	
	/**
	 * 查询选中省内所有的市，优先从数据库查询,如果没有查询到再去服务器上查询
	 */
	private void queryCities(){
		cityList=weatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}
		else{
			LogUtil.d(TAG, "Going to query city form server");
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	
	/**
	 * 查询选中市内所有的县,优先从数据库查询,如果没有查询到再去服务器查询
	 */
	private void queryCounties(){
		countyList=weatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}
		else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	
	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(final String code,final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+code+
					".xml";
		}
		else{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		LogUtil.d(TAG, "Querying "+type+" from server");
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){

			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				LogUtil.d(TAG, "onFinish on querying "+type+" from server");
				boolean result=false;
				if(type.equals("province")){
					result=Utility.handleProvincesResponse(weatherDB, response);
				}
				else if(type.equals("city")){
					result=Utility.handleCitiesResponse(weatherDB, response, selectedProvince.getId());
				}
				else if(type.equals("county")){
					LogUtil.d(TAG, "onFinish querying county,response="+response);
					result=Utility.handleCountiesResponse(weatherDB,response,selectedCity.getId());
				}
				if(result){
					//通过runOnUiThread()方法回到主线程处理逻辑
					LogUtil.d(TAG, "result is true");
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if(type.equals("province")){
								queryProvinces();
							}
							else if(type.equals("city")){
								queryCities();
							}
							else if(type.equals("county")){
								queryCounties();
							}
						}
						
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				//通过runOnUiThread()方法回到主线程处理
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
					
				});
			}
			
		});
	}
	
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog(){
		if(progressDialog==null){
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog(){
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
	}
	
	/**
	 * 捕获Back键，根据当前级别来判断，此时应该返回市列表、省列表还是直接退出
	 */
	
	@Override
	public void onBackPressed(){
		if(currentLevel==LEVEL_COUNTY){
			queryCities();
		}
		else if(currentLevel==LEVEL_CITY){
			queryProvinces();
		}
		else{
			finish();
		}
	}
}

















