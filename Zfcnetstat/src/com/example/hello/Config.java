package com.example.hello;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class Config {
	
	private static final String TAG = "Config";
	 
	public static final String UPDATE_SAVENAME = "Hello.apk"; 
	public static final String UPDATE_SERVER = "http://www.xiashaweixin.com/zfcnetstat/"; 
	public static final String UPDATE_APKNAME = "zfcnetstat.apk";
	public static final String UPDATE_VERJSON = "ver.json";
	//[{"appname":"zfcnetstat","apkname":"Hello.apk","verName":1.0.2,"verCode":2}] 
	//把这个文件ver.json放http://www.xiashaweixin.com/zfcnetstat/下
	
	public static int getVerCode(Context context) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return verCode;
	}
	
	public static String getVerName(Context context) {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return verName;	

	}
	/*return like 上网时长调查*/
	public static String getAppName(Context context) {
		String verName = context.getResources()
		.getText(R.string.app_name).toString();
		return verName;
	}
	/*return like zfcnetstat*/
	public static String getAppEnglishName(Context context) {
		String verName = context.getResources()
		.getText(R.string.app_english_name).toString();
		return verName;
	}
}
