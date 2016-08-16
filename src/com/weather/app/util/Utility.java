package com.weather.app.util;

import com.weather.app.activity.ChooseAreaActivity;
import com.weather.app.model.City;
import com.weather.app.model.County;
import com.weather.app.model.Province;
import com.weather.app.model.WeatherDB;

import android.text.TextUtils;
import android.util.Log;

public class Utility {
	/**
	 * �����ʹ������������ص�ʡ������
	 */
	public synchronized static boolean handleProvincesResponse(WeatherDB weatherDB,String response){
		if(!TextUtils.isEmpty(response)){
			String [] allProvinces=response.split(",");
			if(allProvinces!=null&&allProvinces.length>0){
				for(String p:allProvinces){
					String[] array=p.split("\\|");
					Province province=new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					//���������������ݴ洢��Province����
					weatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	

	/**
	 * �����ʹ������������ص��м�����
	*/


	public synchronized static boolean handleCitiesResponse(WeatherDB weatherDB,String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities=response.split(",");
			if(allCities!=null&&allCities.length>0){
				for(String c:allCities){
					String[] array=c.split("\\|");
					City city=new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					//���������������ݴ洢��City����
					weatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * �����ʹ������������ص��ؼ�����
	 */
	public synchronized static boolean handleCountiesResponse(WeatherDB weatherDB,String response,int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounties=response.split(",");
			if(allCounties!=null&&allCounties.length>0){
				for(String c:allCounties){
					String[] array=c.split("\\|");
					County county=new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					//���������������ݴ洢��County����
					LogUtil.d(ChooseAreaActivity.TAG, "In Utility Saving county:"+county.getCountyName());
					weatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}













