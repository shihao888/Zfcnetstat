package com.example.hello;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MembershipActivity extends Activity implements OnClickListener{
	Button buttonStartService;
	private ProfileUtil profile;
	boolean isServiceStarted = false;
	TextView tvOnlineInfo;	
	MyReceiver receiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_membership);
		String membername = getIntent().getStringExtra("membername");
		this.setTitle("会员："+membername);
		profile = new ProfileUtil(this);
		//显示上网时间
		tvOnlineInfo = (TextView) findViewById(R.id.OnlineInfo);
		//注册接收器
		receiver=new MyReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("android.intent.action.OnlineTimeUpdate");
		this.registerReceiver(receiver,filter);
		//开启
		buttonStartService = (Button) findViewById(R.id.StartService); 	 
		buttonStartService.setOnClickListener(this);  
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.StartService:
			
			if (!isServiceStarted) {
				Intent intent = new Intent(this, MyService.class);
				if (startService(intent) == null) {
					Toast.makeText(getApplicationContext(), "无法启动！", Toast.LENGTH_SHORT).show();
					return;
				}
				isServiceStarted = true;
				//
			} else {
				Toast.makeText(getApplicationContext(), "服务已启动！", Toast.LENGTH_SHORT).show();
				return;
			}			
			
			break;
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.member, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			String s="版本version 6 \n"+"用户id:"+profile.readParam("userid");
			AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器 
			builder.setMessage(s);
			builder.create().show();			
			return true;
		}
		if (id == R.id.chkmyservice) {
			String s = "";
			if(isServiceRunning("com.example.hello.MyService"))s="后台服务正在运行......";
			else s="后台服务已经停止!!!";
			Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
			Long total = profile.readTime("totaltime");
			Long start = profile.readTime("starttime");
			Long stop = profile.readTime("stoptime");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy年-MM月dd日-HH时mm分ss秒");
			Date date1 = new Date(start);Date date2 = new Date(stop);
			String sTotal = formatDuring(total);
			String sStart = formatter.format(date1);
			String sStop= formatter.format(date2);
			s="total="+sTotal+" starttime="+sStart+" stoptime="+sStop;
			AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器 
			builder.setMessage(s);
			builder.create().show(); 
			
			return true;
		}
		if (id == R.id.cleartotal) {
			profile.writeTime(0,"totaltime");
		}
		if (id == R.id.stopservice) {
			if (isServiceStarted) {		
				stopService(new Intent(this, MyService.class));
				isServiceStarted = false;
			}else{
				Toast.makeText(getApplicationContext(), "服务已停止！" ,Toast.LENGTH_SHORT).show();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	private boolean isServiceRunning(String serviceName) {//"com.example.MyService"
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceName.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	public static String formatDuring(long mss) {  	      
	    java.text.DecimalFormat   df=new   java.text.DecimalFormat("#.##"); 		
		float f = (float)mss/(float)(1000 * 60 * 60);			     
	    return df.format(f) +" 小时 " ;  
	} 
	//自定义一个广播接收器
	public class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle bundle=intent.getExtras();
			long a=bundle.getLong("totaltime");
			tvOnlineInfo.setText(formatDuring(a));
		}
		
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(receiver);
	}
}
