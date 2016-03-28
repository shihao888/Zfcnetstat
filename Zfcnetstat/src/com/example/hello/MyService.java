package com.example.hello;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MyService extends Service {
		
	private ConnectivityManager connectivityManager;  
    private NetworkInfo info;     
    
	private ProfileUtil profile;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	//得到手机唯一标识
	public String getDevId() {
		try { 
			TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);  
			if(tm!=null&&tm.getDeviceId()!=null){
				return tm.getDeviceId();
			}else
				return "N/A";
	    } catch (Exception e) { 
	    	return "N/A";
	    } 
		
	}
	
	//
	private BroadcastReceiver mReceiver = new BroadcastReceiver()  
    {  
		
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();  
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))  
            {              	            	
				connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				info = connectivityManager.getActiveNetworkInfo(); 
				
					if(info != null && info.isAvailable() && (info.getState() == NetworkInfo.State.CONNECTED)){						
						//开始计时
						Long l = System.currentTimeMillis();
						profile.writeTime(l,"starttime");
						Toast.makeText(getApplicationContext(), info.getTypeName(),Toast.LENGTH_SHORT).show();
						//将之前的上网时间数值上传网站（仅在WIFI情况下）
						if(info.getType() == ConnectivityManager.TYPE_MOBILE){
							connectNodejsServer();
						}
					}
					else{
						Toast.makeText(getApplicationContext(), "没有可用网络",Toast.LENGTH_SHORT).show();
						//停止计时
						long stoptime = System.currentTimeMillis();						
						long totaltime = profile.readTime("totaltime")+stoptime-profile.readTime("starttime"); 
						profile.writeTime(totaltime,"totaltime"); //记录总时间
						profile.writeTime(stoptime,"starttime");//记录结束时间到开始位置						
					}
					
            }  
		}
		
		private void connectNodejsServer() {
			long totaltime = profile.readTime("totaltime");
			String s1, s2;
			try {
				s1 = URLEncoder.encode(profile.readParam("username"), "UTF-8");
				s2 = URLEncoder.encode(profile.readParam("stuid"), "UTF-8");

				String site = "http://zfcnetstat.duapp.com/upload";
				String url = site + "?onlinetime=" + totaltime + "&username=" + s1 + "&stuid=" + s2 + "&devid="
						+ getDevId();

				// 启动线程更新网站端数据库
				MyHandler h = new MyHandler(MyService.this); //对外部类对象的引用
				HttpGetThread httpThread = new HttpGetThread(url, h);
				new Thread(httpThread).start();
				

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//end of connectNodejsServer()
	};
    
	// http://blog.csdn.net/wuleihenbang/article/details/17126371
	// http://blog.csdn.net/aigochina/article/details/17841999
	private static class MyHandler extends Handler {
		private final MyService mService;

		public MyHandler(MyService s) {
			mService = new WeakReference<MyService>(s).get();
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle data = msg.getData();
			String val = data.getString("MyValue");// 请求结果
			Toast.makeText(mService.getApplicationContext(), val, Toast.LENGTH_LONG).show();
		}
	}
   
    private void improvePriority() {  
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,  
	            new Intent(this, MyService.class), 0);  
	    Notification notification = new Notification.Builder(this)  
	            .setContentTitle("Foreground Service")  
	            .setContentText("Foreground Service Started.")  
	            .setSmallIcon(R.drawable.ic_launcher).build();  
	    notification.contentIntent = contentIntent;  
	    startForeground(1, notification);  //0 将不会显示 notification
	} 
	@Override
	public void onCreate() {
		improvePriority();
		//		
		profile = new ProfileUtil(this);
		//注册广播  
        IntentFilter mFilter = new IntentFilter();  
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); // 添加接收网络连接状态改变的Action  
        registerReceiver(mReceiver, mFilter); 

	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Service is shutting down...", Toast.LENGTH_SHORT).show();
		super.onDestroy();
		unregisterReceiver(mReceiver); // 删除广播
		
		stopForeground(true); 
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

	    // Restart the service if it got killed
		Toast.makeText(getApplicationContext(), "Service is starting...", Toast.LENGTH_SHORT).show();
	    return START_STICKY;
	}


	
}
