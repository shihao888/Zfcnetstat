package com.example.hello;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
	Button buttonLogin, buttonRegister, buttonShortcut; 	 
	EditText et_mobilenum,et_pwd;
	String mobilenum,pwd;	
	private ProfileUtil profile;
	Zfcnetstat zfc; 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			moveTaskToBack(false);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		zfc = ((Zfcnetstat)getApplicationContext());
		zfc.setPrompted(false);
		profile = new ProfileUtil(this);
		
		
				
		// 通过 findViewById(id)方法获取用户名和密码控件对象  
		et_mobilenum = (EditText) findViewById(R.id.et_mobilenum);  
        et_pwd = (EditText) findViewById(R.id.et_pwd); 
				
		//开启
		buttonLogin = (Button) findViewById(R.id.buttonlogin);  
        buttonRegister = (Button) findViewById(R.id.buttonregister);  
  
        buttonLogin.setOnClickListener(this);  
        buttonRegister.setOnClickListener(this); 
        
        //检查是否有版本更新
        checkNewVersion();
        //checkNewVersionDaily();
	}
	

	@Override
	protected void onStart() {
		// 这一句不能省
		// 在重写 onStart()、onStop()、onResume()、onPause()、onDestroy() 等等函数的时候
		// 一定要在函数中加上一句 super.onXX();否则就会报错。
		super.onStart();
		// 如果曾经成功登录过,2分钟内不用再次登录，而显示直接跳转按钮
		buttonShortcut = (Button) findViewById(R.id.buttonShortcut);
		if (isAlreadyLoggedIn()) {
			buttonShortcut.setVisibility(View.VISIBLE);// 显示按钮
			buttonShortcut.setOnClickListener(this);
		} else
			buttonShortcut.setVisibility(View.GONE);// 隐藏按钮
		
	}
	
	private Boolean isAlreadyLoggedIn() {
		final int const_time = 2;//时间间隔2分钟
		// 如果成功登录
		if((ProfileUtil.LoginSuccessFlag).equals(profile.readParam("loginSuccess"))){
			long currentTime = System.currentTimeMillis();
			long loginSuccessTime = profile.readTime("loginSuccessTime");//之前成功登录时间
			if(loginSuccessTime==0l)return false;//没有设置过loginSuccessTime，说明还未成功登录过
			
			int timeInterval = (int)((currentTime - loginSuccessTime) / ( 1000 * 60 )); //时间间隔是几分钟
			return (timeInterval<const_time)?true:false; //小于2分钟的登录返回TRUE
		}else 
			return false;
		
	}

	@Override
	public void onClick(View src) {
		// TODO Auto-generated method stub
				
		switch (src.getId()) {
		case R.id.buttonlogin:			    		
    		// 获取用户手机号  
            mobilenum = et_mobilenum.getText().toString();  
            // 获取用户密码
            pwd = et_pwd.getText().toString(); 
            if (TextUtils.isEmpty(mobilenum) || TextUtils.isEmpty(pwd)) {  
                Toast.makeText(this, "手机号和密码都不能为空,调查服务没有启动！", Toast.LENGTH_LONG).show();
                return;
            }else{
            	buttonLogin.setEnabled(false);
            	login(mobilenum,pwd);       	
            }                       
			break;
		case R.id.buttonregister:
			GotoNextActivity(this, RegisterActivity.class, "", "");
			break;
		case R.id.buttonShortcut:
			GotoNextActivity(this, MembershipActivity.class, "mobilenum", mobilenum);
			break;
		}
		return;
	}
	private void login(String mobilenum, String pwd) {
		String[] s = new String[2];		
		try {
			s[0] = URLEncoder.encode(mobilenum, "UTF-8");
			s[1] = URLEncoder.encode(profile.getMD5(pwd), "UTF-8");			
				
			String site = ProfileUtil.mywebsite+"/login";
			String url = site + "?mobilenum=" + s[0] + "&pwd=" + s[1];

			// 启动线程更新网站端数据库
			Handler h = new MyHandler(this);
			HttpGetThread httpThread = new HttpGetThread(url, h, ProfileUtil.MSG_LOGIN);
			new Thread(httpThread).start();
			
			

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static class MyHandler extends Handler {  
        private final MainActivity mActivity;  
  
        public MyHandler(MainActivity activity) {  
            mActivity = new WeakReference<MainActivity>(activity).get();  
        }  
        
        @Override  
        public void handleMessage(Message msg) {  
        	super.handleMessage(msg);
        	switch (msg.what) {
        	case ProfileUtil.MSG_LOGIN:        	
				Bundle data = msg.getData();
				String val = data.getString("MyValue");//请求结果
				Toast.makeText(mActivity.getApplicationContext(), val, Toast.LENGTH_LONG).show();
				//如果登录成功
				if(val!=null&&val.equals(ProfileUtil.LoginSuccessFlag)){
				mActivity.profile.writeParam("mobilenum",mActivity.mobilenum);
				mActivity.profile.writeParam("loginSuccess",ProfileUtil.LoginSuccessFlag);
				mActivity.profile.writeTime(System.currentTimeMillis(),"loginSuccessTime");	//记下成功登录时间
				mActivity.GotoNextActivity(mActivity,MembershipActivity.class,"mobilenum",mActivity.mobilenum);
				}
				mActivity.buttonLogin.setEnabled(true);
				break;
        	case ProfileUtil.MSG_VERSION:
        		Bundle data1 = msg.getData();
				String val1 = data1.getString("MyValue");//请求结果
				mActivity.analyseVersion(val1);
        		break;
        	default:
        		break;
        		
        	}
        }  
    }  

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			String s="当前版本："+Config.getVerCode(this)+"\n用户:"+profile.readParam("mobilenum")+"\n开发团队：周宏敏、史浩";
			AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器 
			builder.setMessage(s);
			builder.create().show();			
			return true;
		}
		if (id == R.id.closePrompt) {			
			zfc.setPrompted(false);
			invalidateOptionsMenu();
			return true;
		}
		if (id == R.id.openPrompt) {			
			zfc.setPrompted(true);
			invalidateOptionsMenu();
			return true;
		}
		if (id == R.id.checkNewversion) {			
			//检查是否有版本更新
	        checkNewVersion();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if(zfc.isPrompted()){
			menu.findItem(R.id.openPrompt).setVisible(false);
			menu.findItem(R.id.closePrompt).setVisible(true);
		}
		else{
			menu.findItem(R.id.openPrompt).setVisible(true);
			menu.findItem(R.id.closePrompt).setVisible(false);
		}
			
		return super.onPrepareOptionsMenu(menu);
	}
	private void GotoNextActivity(Activity FromAct,Class<?> ToActCls,String InfoName, String InfoValue){
		Intent intent = new Intent(); 
		intent.putExtra(InfoName, InfoValue);
		intent.setClass(FromAct,ToActCls);
		/*
		 * 注意Intent的flag设置：
		 * FLAG_ACTIVITY_CLEAR_TOP: 如果activity已在当前任务中运行，在它前端的activity都会被关闭，
		 * 它就成了最前端的activity。
		 * FLAG_ACTIVITY_SINGLE_TOP: 如果activity已经在最前端运行，则不需要再加载。
		 * 设置这两个flag，就是让一个且唯一的一个activity（服务界面）运行在最前端。
		 */
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);//http://blog.csdn.net/sxsj333/article/details/6639812
		FromAct.startActivityForResult(intent, 0);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch (resultCode) { 
		case RESULT_OK:
			Bundle b=data.getExtras(); //data为B中回传的Intent
			String str=b.getString("voice from RegisterActivity");
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	/*
	 * 检查是否为新版本
	 */
	private Handler handler = new Handler();	
	public ProgressDialog pBar;	
	private int newVerCode = 0;
	private String newVerName = "";	
	
	public void checkNewVersion() {
		
		// 启动线程获取版本信息ver.json
		Handler h = new MyHandler(this);
		HttpGetThread httpThread = new HttpGetThread(Config.UPDATE_SERVER + Config.UPDATE_VERJSON, h, ProfileUtil.MSG_VERSION);
		new Thread(httpThread).start();
	}
	private void analyseVersion(String verjson) {
		
		if (getServerVerCode(verjson)) {
			int vercode = Config.getVerCode(this);
			if (newVerCode > vercode) {
				doNewVersionUpdate();

			} else {
				notNewVersionShow();
			}
		}
		
	}
	private boolean getServerVerCode(String verjson) {
		try {						
			JSONArray array = new JSONArray(verjson);
			if (array.length() > 0) {
				JSONObject obj = array.getJSONObject(0);
				try {
					newVerCode = Integer.parseInt(obj.getString("verCode"));
					newVerName = obj.getString("verName");
				} catch (Exception e) {
					newVerCode = -1;
					newVerName = "";
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void notNewVersionShow() {
		int verCode = Config.getVerCode(this);
		String verName = Config.getVerName(this);
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本:");
		sb.append(verName);
		sb.append(" Code:");
		sb.append(verCode);
		sb.append(",\n已是最新版,无需更新!");
		Dialog dialog = new AlertDialog.Builder(this).setTitle("软件更新").setMessage(sb.toString())//设置内容
				.setPositiveButton("确定",// 设置确定按钮
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}

						})
				.create();
		dialog.show();
	}

	private void doNewVersionUpdate() {
		int verCode = Config.getVerCode(this);
		String verName = Config.getVerName(this);
		StringBuffer sb = new StringBuffer();
		sb.append("当前版本:");
		sb.append(verName);
		sb.append(" Code:");
		sb.append(verCode);
		sb.append(", 发现新版本:");
		sb.append(newVerName);
		sb.append(" Code:");
		sb.append(newVerCode);
		sb.append(", 是否更新?");
		Dialog dialog = new AlertDialog.Builder(this).setTitle("软件更新").setMessage(sb.toString())
				//设置内容
				.setPositiveButton("更新",// 设置确定按钮
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(getApplicationContext(), UpdateService.class);
								intent.putExtra("app_english_name", getResources().getString(R.string.app_english_name));
								startService(intent);//启动更新服务UpdateService！！
							}

						})
				.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						//点击"取消"按钮之后退出程序
						//finish();
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}
	void down() {
		handler.post(new Runnable() {
			public void run() {
				pBar.cancel();
				update();
			}
		});

	}

	void update() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), Config.UPDATE_SAVENAME)),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}
	/*
	 * 启动定时器，每天检查一次更新
	 */
	private void checkNewVersionDaily(){
		
		final long DAY = 1000L * 60 * 60 * 24;
		
		long firstTime = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)  
		long currTime = System.currentTimeMillis();  
		  
		Calendar calendar = Calendar.getInstance();  
		calendar.setTimeInMillis(System.currentTimeMillis());  
		// 这里时区需要设置一下，不然会有8个小时的时间差  
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));  
		calendar.set(Calendar.MINUTE, 0);  
		calendar.set(Calendar.HOUR_OF_DAY, 13); //24小时表示，13表示下午1点 
		calendar.set(Calendar.SECOND, 0);  
		calendar.set(Calendar.MILLISECOND, 0);
		// 选择的每天定时时间  
		long selectTime = calendar.getTimeInMillis();  
		// 如果当前时间大于设置的时间，那么就从第二天的设定时间开始  
		if(currTime > selectTime) {		  
			calendar.add(Calendar.DAY_OF_MONTH, 1);  
			selectTime = calendar.getTimeInMillis();  
		}  
		// 计算现在时间到设定时间的时间差
	 	long time = selectTime - currTime;
 		firstTime += time;
 		

		//这里采用定时发送广播的形式PendingIntent.getBroadcast
		Intent intent = new Intent(MainActivity.this, ChkNewVersionReceiver.class);  
		PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
	
		// 进行闹铃注册
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime, 10*1000, sender); 
	}
	// 取消检查新版本
	public void cancelChkNewVersion(){		
		Intent intent = new Intent(MainActivity.this, ChkNewVersionReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this,
                0, intent, 0);        
        
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);

	}
	
}
