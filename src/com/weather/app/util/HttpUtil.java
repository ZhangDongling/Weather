package com.weather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.weather.app.activity.ChooseAreaActivity;

public class HttpUtil {
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpURLConnection connection=null;
				try{
					URL url=new URL(address);
					connection=(HttpURLConnection)url.openConnection();
					LogUtil.d(ChooseAreaActivity.TAG, "HttpUtil:"+
							LogUtil.EXTRA_INFO+
							":Got the Connection");
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					LogUtil.d(ChooseAreaActivity.TAG,"HttpUtil:"+
							LogUtil.EXTRA_INFO+
							":Getting the InputStream...");
					InputStream in = connection.getInputStream();
					LogUtil.d(ChooseAreaActivity.TAG, "HttpUtil:"+
							LogUtil.EXTRA_INFO+
							": Got the InputStream...");
					BufferedReader reader=new BufferedReader(new InputStreamReader(in));
					LogUtil.d(ChooseAreaActivity.TAG, "HttpUtil:"+
							LogUtil.EXTRA_INFO+
							":Got the BufferedReader..");
					StringBuilder response=new StringBuilder();
					String line;
					while((line=reader.readLine())!=null){
						response.append(line);
					}
					LogUtil.d(ChooseAreaActivity.TAG, "In HttpUtil:"+
							LogUtil.EXTRA_INFO+
							":The response is :"+response);
					if(listener!=null){
						//回调onFinish()方法
						listener.onFinish(response.toString());
					}
				}
				catch(Exception e){
					if(listener!=null){
						//回调onError()方法
						listener.onError(e);
					}
				}
				finally{
					if(connection!=null){
						connection.disconnect();
					}
				}
			}
			
		}).start();
	}
}
