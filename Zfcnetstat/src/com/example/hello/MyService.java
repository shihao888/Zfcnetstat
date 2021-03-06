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
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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
						//将之前的上网时间数值上传网站（在WIFI或者MOBILE情况下）
						if(info.getType() == ConnectivityManager.TYPE_MOBILE||info.getType() == ConnectivityManager.TYPE_WIFI){
							if(timer==null){
								timer = new Timer();
							}
							if(task==null){
								task = new MyTimerTask();
							}
							try{
								//Timer和TimerTask在调用cancel()取消后不能再执行 schedule语句，否则提示出错
								timer.schedule(task, 1000, 60000); // 1s后执行task,然后每隔60s连续执行 
							}catch(Exception e){
								return;
							}
						}
					}
					else{
						Toast.makeText(getApplicationContext(), "没有可用网络",Toast.LENGTH_SHORT).show();
						//停止计时
						cancelTimerandTask();
						long stoptime = System.currentTimeMillis();						
						long totaltime = profile.readTime("totaltime")+stoptime-profile.readTime("starttime"); 
						profile.writeTime(totaltime,"totaltime"); //记录总时间
						profile.writeTime(stoptime,"starttime");//记录结束时间到开始位置						
					}
					
            }  
		}
		
		
	};
    
	// http://blog.csdn.net/wuleihenbang/article/details/17126371
	// http://blog.csdn.net/aigochina/article/details/17841999
	private static class MyHandler extends Handler {
		
		private final MyService mService;
		
		public MyHandler(Looper looper, MyService s) {
			super(looper);			
			this.mService = new WeakReference<MyService>(s).get();
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
        	case ProfileUtil.MSG_UPLOAD:
				Bundle data = msg.getData();
				String val = data.getString("MyValue");// 请求结果
				Zfcnetstat zfc = ((Zfcnetstat)mService.getApplicationContext());
				if(zfc.isPrompted())
				  Toast.makeText(mService.getApplicationContext(), val, Toast.LENGTH_LONG).show();
				break;
			}
		}
	}
    private Notification mkNotification(String strNotificationText){
    	Intent intent = new Intent(this,MainActivity.class); //点击通知栏，会转到哪个activity
    	/*
		 * 注意Intent的flag设置：
		 * FLAG_ACTIVITY_CLEAR_TOP: 如果activity已在当前任务中运行，在它前端的activity都会被关闭，
		 * 它就成了最前端的activity。
		 * FLAG_ACTIVITY_SINGLE_TOP: 如果activity已经在最前端运行，则不需要再加载。
		 * 设置这两个flag，就是让一个且唯一的一个activity（服务界面）运行在最前端。
		 */
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	
	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,  
	            intent, 0);  
	    Notification notification = new Notification.Builder(this)  
	            .setContentTitle("浙江金融职业学院")  
	            .setContentText(strNotificationText)  
	            .setSmallIcon(R.drawable.ic_launcher).build();	    
	    notification.contentIntent = contentIntent;	    
	    return notification;
    }
    private void improvePriority() {
    	Notification notification = mkNotification("上网时长调查服务已启动."); 
    	//设置notification的flag，表明在点击通知后，通知并不会消失，仍在通知栏显示图标。
	    //这是确保在activity中退出后，状态栏仍有图标可提下拉、点击，再次进入activity。
	    notification.flags |= Notification.FLAG_NO_CLEAR;
	    startForeground(1, notification);  //0 将不会显示 notification
	}
    private void sendNotification() {
		//获取通知管理器
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		//新建一个通知，指定其图标和标题		 
	    Notification notification = mkNotification("上网时长调查服务已停止！！"); 
	    notification.defaults = Notification.DEFAULT_SOUND;//发出默认声音
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;//点击通知后自动清除通知
	    mNotificationManager.notify(0, notification);//第一个参数为自定义的通知唯一标识
	}
	@Override
	public void onCreate() {
		super.onCreate();
		improvePriority();
		//		
		profile = new ProfileUtil(this);
		//注册广播  
        IntentFilter mFilter = new IntentFilter();  
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); // 添加接收网络连接状态改变的Action  
        registerReceiver(mReceiver, mFilter); 
        //获取电源锁，待机后也可以运行
        acquireWakeLock();

	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Service is shutting down...", Toast.LENGTH_SHORT).show();
		super.onDestroy();
		unregisterReceiver(mReceiver); // 删除广播
		
		stopForeground(true); 
		cancelTimerandTask();
		sendNotification();
		
		//释放待机锁
		releaseWakeLock();
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

	    // Restart the service if it got killed
		Toast.makeText(getApplicationContext(), "Service is starting...", Toast.LENGTH_SHORT).show();
	    return START_STICKY;
	}
	//定时器
	Timer timer = new Timer();  
    TimerTask task = new MyTimerTask(); 
    private class MyTimerTask extends TimerTask {  
  
        @Override  
        public void run() {  
            // 需要做的事:
        	// 更新总上网时间,同时更新MembershipActivity界面
        	//定时连接后台nodejs服务器上传总上网时间  
        	long stoptime = System.currentTimeMillis();						
			long totaltime = profile.readTime("totaltime")+stoptime-profile.readTime("starttime"); 
			profile.writeTime(totaltime,"totaltime"); //记录总时间
			profile.writeTime(stoptime,"starttime");//记录结束时间到开始位置	
			//更新MembershipActivity界面
			Intent intent=new Intent();
			Bundle bundle = new Bundle();			
			bundle.putLong("totaltime", totaltime);
			intent.putExtras(bundle);
			intent.setAction("android.intent.action.OnlineTimeUpdate");//action与接收器相同
			sendBroadcast(intent); //更新本地：更新membershipActivity显示界面
        	connectNodejsServer(); //更新远程：更新服务器
        }  
    }; 
    private void cancelTimerandTask() {
		// TODO Auto-generated method stub
		if (task != null) {
			task.cancel();
			task = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
    private void connectNodejsServer() {
		
		String[] s = new String[2];
		try {
			s[0] = URLEncoder.encode(profile.readParam("mobilenum"), "UTF-8");
			long t = profile.readTime("totaltime");
			float hour = (float)t / ( 1000 * 60 * 60 ); //毫秒转小时
			DecimalFormat   fnum  =   new  DecimalFormat("##0.00");
			String str = fnum.format(hour);//小时保留2位小数
			s[1] = URLEncoder.encode(str, "UTF-8");			
			String site = ProfileUtil.mywebsite+"/upload";
			String url = site + "?mobilenum="+s[0]+"&onlinetime=" + s[1];

			// 启动线程更新网站端数据库
			//让需要sendMessage的线程知道用哪个handler往哪个Looper发消息
			MyHandler h = new MyHandler(Looper.getMainLooper(),MyService.this); //对外部类对象的引用	
			HttpGetThread httpThread = new HttpGetThread(url, h, ProfileUtil.MSG_UPLOAD);
			new Thread(httpThread).start();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//end of connectNodejsServer()
    
    private WakeLock wakeLock = null;

    /**
     * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
     */
    private void acquireWakeLock() {
     if (null == wakeLock) {
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
        | PowerManager.ON_AFTER_RELEASE, getClass()
        .getCanonicalName());
      if (null != wakeLock) {
         wakeLock.acquire();
      }
     }
    }
 // 释放设备电源锁
    private void releaseWakeLock() {
     if (null != wakeLock && wakeLock.isHeld()) {
      wakeLock.release();
      wakeLock = null;
     }
    }
}
