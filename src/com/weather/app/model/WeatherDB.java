package com.weather.app.model;

import java.util.ArrayList;
import java.util.List;

import com.weather.app.activity.ChooseAreaActivity;
import com.weather.app.db.WeatherOpenHelper;
import com.weather.app.util.LogUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WeatherDB {
	/**
	 * 数据库名
	 */
	public static final String DB_NAME="my_weather";
	
	private static final String ID="id";
	
	private static final String PROVINCE_NAME="province_name";
	private static final String PROVINCE_CODE="province_code";
	private static final String PROVINCE_ID="province_id";
	
	private static final String CITY_NAME="city_name";
	private static final String CITY_CODE="city_code";
	private static final String CITY_ID="city_id";
	
	private static final String COUNTY_NAME="county_name";
	private static final String COUNTY_CODE="county_code";
	private static final String COUNTY_ID="county_id";
	
	private static final String TABLE_PROVINCE="Province";
	private static final String TABLE_CITY="City";
	private static final String TABLE_COUNTY="County";
	
	/**
	 * 数据库版本名
	 */
	public static final int VERSION=1;
	
	private static WeatherDB weatherDB;
	
	private SQLiteDatabase db;
	
	/**
	 * 将构造方法私有化
	 */
	private WeatherDB(Context context){
		WeatherOpenHelper dbHelper=new WeatherOpenHelper(context, DB_NAME, null, VERSION);
		db=dbHelper.getWritableDatabase();
	}
	
	/**
	 * 获取WeatherDB的实例
	 */
	public synchronized static WeatherDB getInstance(Context context){
		if(weatherDB==null){
			weatherDB=new WeatherDB(context);
		}
		return weatherDB;
	}
	
	public void saveProvince(Province province){
		if(province!=null){
			ContentValues values=new ContentValues();
			values.put(PROVINCE_NAME, province.getProvinceName());
			values.put(PROVINCE_CODE, province.getProvinceCode());
			db.insert(TABLE_PROVINCE, null, values);
		}
	}
	
	/**
	 * 从数据库读取全国所有的省份的信息.
	 */
	public List<Province> loadProvinces(){
		List<Province>list=new ArrayList<Province>();
		Cursor cursor=db.query(TABLE_PROVINCE, null, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				Province province=new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex(ID)));
				province.setProvinceName(cursor.getString(cursor.
						getColumnIndex(PROVINCE_NAME)));
				province.setProvinceCode(cursor.getString(cursor.
						getColumnIndex(PROVINCE_CODE)));
				list.add(province);
			}while(cursor.moveToNext());
		}
		return list;
	}
	
	/**
	 * 将City实例存储到数据库
	 */
	public void saveCity(City city){
		if(city!=null){
			ContentValues values=new ContentValues();
			values.put(CITY_NAME, city.getCityName());
			values.put(CITY_CODE, city.getCityCode());
			values.put(PROVINCE_ID, city.getProvinceId());
			db.insert(TABLE_CITY, null, values);
		}
	}
	/**
	 * 从数据库读取某省下所有的城市信息
	 */
	public List<City> loadCities(int provinceId){
		List<City>list=new ArrayList<City>();
		Cursor cursor=db.query(TABLE_CITY, null, "province_id=?", new String[]{String.valueOf(provinceId)}, null, null, null);
		if(cursor.moveToFirst()){
			do{
				City city=new City();
				city.setId(cursor.getInt(cursor.getColumnIndex(ID)));
				city.setCityName(cursor.getString(cursor.
						getColumnIndex(CITY_NAME)));
				city.setCityCode(cursor.getString(cursor.
						getColumnIndex(CITY_CODE)));
				city.setProvinceId(provinceId);
				list.add(city);
			}while(cursor.moveToNext());
		}
		return list;
	}
	
	/**
	 * 将County实例存储到数据库
	 */
	public void saveCounty(County county){
		if(county!=null){
			ContentValues values=new ContentValues();
			values.put(COUNTY_NAME, county.getCountyName());
			values.put(COUNTY_CODE,county.getCountyCode());
			values.put(CITY_ID, county.getCityId());
			LogUtil.d(ChooseAreaActivity.TAG,"WeatherDB: inserting county:"+county.getCountyName());
			long res;
			res=db.insert(TABLE_COUNTY, null,values);
			LogUtil.d(ChooseAreaActivity.TAG, "WeatherDB: db.insert county:"+county.getCountyName()+",res="+res);
		}
	}
	
	/**
	 * 从数据库读取某城市下所有县的信息
	 */
	public List<County>loadCounties(int cityId){
		List<County>list=new ArrayList<County>();
		Cursor cursor=db.query(TABLE_COUNTY, null, "city_id=?", new String[]{String.valueOf(cityId)}, null, null, null);
		if(cursor.moveToFirst()){
			do{
				County county=new County();
				county.setId(cursor.getInt(cursor.getColumnIndex(ID)));
				county.setCountyName(cursor.getString(cursor.getColumnIndex(COUNTY_NAME)));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex(COUNTY_CODE)));
				county.setCityId(cityId);
				list.add(county);
			}while(cursor.moveToNext());
		}
		return list;
	}

}
