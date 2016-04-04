package com.example.hello;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;

public class RegisterActivitySMS extends Activity implements OnClickListener{
	//验证码
	ImageView vc_image; // 图标
	Button vc_shuaxin, vc_ok; // 确定和刷新验证码
	String getCode = null; // 获取验证码的值
	EditText vc_code; // 文本框的值
	//
	Button buttonOK, buttonCancel; 
	private ProfileUtil profile;
	String mobilenum,pwd1,pwd2,realname,stuid;
	//短信验证
	Button requestCodeBtn;
	EditText inputCodeEt;
	int i = 30; //倒计时
	private static String APPKEY = "113589082c5b5";    
    private static String APPSECRETE = "e42ab57e47c84a6286f630be8632570a";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_register);
		setTitle("用户注册");
		profile = new ProfileUtil(this);
				
		buttonOK = (Button) findViewById(R.id.registerOK);  
        buttonCancel = (Button) findViewById(R.id.registerCancel);  
  
        buttonOK.setOnClickListener(this);  
        buttonCancel.setOnClickListener(this); 
        
        //生成验证码
        vc_image=(ImageView)findViewById(R.id.vc_image);
        vc_image.setImageBitmap(Code.getInstance().getBitmap());
        vc_code=(EditText) findViewById(R.id.vc_code);
        
        getCode=Code.getInstance().getCode(); //获取显示的验证码
        vc_shuaxin=(Button)findViewById(R.id.vc_shuaxin);
        vc_shuaxin.setOnClickListener(this);
        
        //短信验证手机号        
        requestCodeBtn = (Button) findViewById(R.id.sms_getcode);
        requestCodeBtn.setOnClickListener(this);
        inputCodeEt = (EditText) findViewById(R.id.input_smscode);
        
	}

	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.vc_shuaxin:
			//刷新验证码
			vc_image.setImageBitmap(Code.getInstance().getBitmap());
			getCode = Code.getInstance().getCode();
			return;
		case R.id.registerOK:
			
			//判断验证码是否正确
			String v_code = vc_code.getText().toString().trim();
			if (v_code == null || v_code.equals("")) {
				Toast.makeText(RegisterActivitySMS.this, "没有填写验证码", Toast.LENGTH_LONG).show();
				return;
			} else if (!v_code.equals(getCode)) {
				Toast.makeText(RegisterActivitySMS.this, "验证码填写不正确", Toast.LENGTH_LONG).show();
				return;
			} 
			String smscode = inputCodeEt.getText().toString();
			if(mobilenum==null||mobilenum.isEmpty()){
				Toast.makeText(RegisterActivitySMS.this, "没有填写手机号", Toast.LENGTH_LONG).show();
				return;	
			}
			if(smscode==null||smscode.isEmpty()){
				Toast.makeText(RegisterActivitySMS.this, "没有填写手机验证码", Toast.LENGTH_LONG).show();
				return;	
			}
            //判断短信验证码
			//将收到的验证码和手机号提交再次核对    
            SMSSDK.submitVerificationCode("86", mobilenum, smscode);
            //createProgressBar(); 
            // 获取用户密码
            pwd1 = ((EditText)findViewById(R.id.pwd1)).getText().toString(); 
            pwd2 = ((EditText)findViewById(R.id.pwd2)).getText().toString();
            //获取其他信息
            realname = ((EditText)findViewById(R.id.realname)).getText().toString();
            stuid = ((EditText)findViewById(R.id.stuid)).getText().toString();
            
            if (TextUtils.isEmpty(mobilenum) || TextUtils.isEmpty(pwd1)|| TextUtils.isEmpty(pwd2)
            		||TextUtils.isEmpty(realname)|| TextUtils.isEmpty(stuid)) {  
                Toast.makeText(this, "所有信息都必须填写！", Toast.LENGTH_LONG).show();
                return;
            }else if(!pwd1.equals(pwd2)){
            	Toast.makeText(this, "两次密码不一致！", Toast.LENGTH_LONG).show();
                return;
            }else{
            	if(profile.isNetworkAvailable()){
            		registerNodejsServer();
            	}else{
            		Toast.makeText(this, "没有网络连接！", Toast.LENGTH_LONG).show();
                    return;
            	}
            }
			break;
		case R.id.registerCancel:
			finish();
			break;
			
		case R.id.sms_getcode://发送验证码
			
	        initSDK();
			
            							
  
            // 3. 把按钮变成不可点击，并且显示倒计时（正在获取）  
//            requestCodeBtn.setClickable(false);  
//            requestCodeBtn.setText("重新发送(" + i-- + ")"); 
//            Handler h = new MyHandler(Looper.getMainLooper(),RegisterActivity.this);
//			SendSMSThread smsThread = new SendSMSThread(h);
//			new Thread(smsThread).start();
			
            // 4. 打开广播来接受读取短信  
  
            
			break;
		}
			
	}
	
	private class SendSMSThread implements Runnable {
		Handler handler;
		public SendSMSThread(Handler h) {
			this.handler = h;
		}

		@Override
		public void run() {
			for (int i = 30; i > 0; i--) { 
				Message msg = handler.obtainMessage();
				msg.what = ProfileUtil.MSG_SMSCODE_WAITTING;
	            handler.sendMessage(msg);  
                if (i <= 0) {  
                    break;  
                }  
                try {  
                    Thread.sleep(1000);  
                } catch (InterruptedException e) {  
                    e.printStackTrace();  
                }  
            }
			Message msg = handler.obtainMessage();
			msg.what = ProfileUtil.MSG_SMSCODE_TIMEOUT;
            handler.sendMessage(msg);
            
		}//end of run
	}
	
	private void registerNodejsServer() {
			String[] s = new String[4];		
			try {
				s[0] = URLEncoder.encode(mobilenum, "UTF-8");
				//产生32位小写的，类似这样的，比如2的profile.getMD5(2)得到
				//c81e728d9d4c2f636f067f89cc14862c
				s[1] = URLEncoder.encode(profile.getMD5(pwd1), "UTF-8");
				s[2] = URLEncoder.encode(realname, "UTF-8");
				s[3] = URLEncoder.encode(stuid, "UTF-8");
				
					
				String site = ProfileUtil.mywebsite+"/init";
				String url = site + "?mobilenum=" + s[0] + "&pwd=" + s[1] + "&realname=" + s[2] + "&stuid="
						+ s[3];

				// 启动线程更新网站端数据库
				Handler h = new MyHandler(Looper.getMainLooper(),this);
				HttpGetThread httpThread = new HttpGetThread(url, h , ProfileUtil.MSG_Register);
				new Thread(httpThread).start();
				
				

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}//end of registerNodejsServer()
	//http://blog.csdn.net/wuleihenbang/article/details/17126371
	//http://blog.csdn.net/aigochina/article/details/17841999
	private static class MyHandler extends Handler {  
        private final RegisterActivitySMS mActivity;  
  
        public MyHandler(Looper looper, RegisterActivitySMS activity) {
        	super(looper);
            mActivity = new WeakReference<RegisterActivitySMS>(activity).get();  
        }  
        
        @Override  
        public void handleMessage(Message msg) {  
        	super.handleMessage(msg);
        	switch(msg.what){
        	case ProfileUtil.MSG_Register:
				Bundle data1 = msg.getData();
				String val = data1.getString("MyValue");//请求结果
				Toast.makeText(mActivity.getApplicationContext(), val, Toast.LENGTH_LONG).show();
				mActivity.finish();
				break;
        	case ProfileUtil.MSG_SMSCODE:
        		int event = msg.arg1;    
                int result = msg.arg2;    
                Object data = msg.obj;    
                   
                if (result == SMSSDK.RESULT_COMPLETE) {    
                    // 短信注册成功后，返回MainActivity,然后提示    
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 提交验证码成功    
                        Toast.makeText(mActivity.getApplicationContext(), "提交验证码成功",    
                                Toast.LENGTH_SHORT).show(); 
                        Intent intent = new Intent(mActivity.getApplicationContext(), 
                        		MainActivity.class);    
                        mActivity.startActivity(intent);
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {    
                        Toast.makeText(mActivity.getApplicationContext(), "正在获取验证码",    
                                Toast.LENGTH_SHORT).show();    
                    } else {    
                        ((Throwable) data).printStackTrace();    
                    }  
                }
        		break;
        	case ProfileUtil.MSG_SMSCODE_WAITTING:    
        		mActivity.requestCodeBtn.setText("重新发送(" + mActivity.i + ")"); 
                break;
        	case ProfileUtil.MSG_SMSCODE_TIMEOUT:  
        		mActivity.requestCodeBtn.setText("获取验证码");    
        		mActivity.requestCodeBtn.setClickable(true);    
        		mActivity.i = 30;
                break;
            
        	}
        }  
    }  

	/*
	 * 短信验证 www.mob.com
	 */
	/**
	 * 初始化短信SDK
	 */
	 EventHandler eh = new EventHandler() {
	        @Override
	        public void afterEvent(int event, int result, Object data) {
	            switch (event) {
	                    case SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE:
	                        if (result == SMSSDK.RESULT_COMPLETE) {
	                            toast("验证成功");
	                        } else {
	                            toast("验证失败");
	                        }
	                        break;
	                    case SMSSDK.EVENT_GET_VERIFICATION_CODE:
	                        if (result == SMSSDK.RESULT_COMPLETE) {
	                            toast("获取验证码成功");
	                            //默认的智能验证是开启的,我已经在后台关闭
	                        } else {
	                            toast("获取验证码失败");
	                        }
	                        break;
	            }//end of switch
	        }
	    };
	private void initSDK() {
		
		cn.smssdk.SMSSDK.initSDK(getApplicationContext(), APPKEY, APPSECRETE,true);
		
//		cn.smssdk.EventHandler eventHandler = new EventHandler() {
//			/**
//			 * 在操作之后被触发
//			 * 
//			 * @param event
//			 *            参数1
//			 * @param result
//			 *            参数2 SMSSDK.RESULT_COMPLETE表示操作成功，
//			 *            为SMSSDK.RESULT_ERROR表示操作失败
//			 * @param data
//			 *            事件操作的结果
//			 */
//			final Handler handler = new MyHandler(Looper.getMainLooper(),RegisterActivity.this);
//			@Override
//			public void afterEvent(int event, int result, Object data) {
//				Message msg = new Message();
//				msg.what = ProfileUtil.MSG_SMSCODE;
//				msg.arg1 = event;
//				msg.arg2 = result;
//				msg.obj = data;
//				handler.sendMessage(msg);
//			}
//		};
		// 注册回调监听接口
		SMSSDK.registerEventHandler(eh); //注册短信回调
		getSMS();
	}
	private void getSMS() {
		// 获取用户手机号 
        mobilenum = ((EditText)findViewById(R.id.reg_mobilenum)).getText().toString();
        // 1. 通过规则判断手机号  
        if (!judgePhoneNums(mobilenum)) {  
            return;  
        } // 2. 通过sdk发送短信验证  
        SMSSDK.getVerificationCode("86", mobilenum, new OnSendMessageHandler() {
			@Override
			public boolean onSendMessage(String arg0, String arg1) {
				// TODO Auto-generated method stub
				return false;
			}});
	}

	/**  
     * 判断手机号码是否合理  
     *   
     * @param phoneNums  
     */    
    private boolean judgePhoneNums(String phoneNums) {    
        if (isMatchLength(phoneNums, 11)    
                && isMobileNO(phoneNums)) {    
            return true;    
        }    
        toast("手机号码输入有误！");    
        return false;    
    }
    private void toast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegisterActivitySMS.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**  
     * 判断一个字符串的位数  
     * @param str  
     * @param length  
     * @return  
     */    
    public static boolean isMatchLength(String str, int length) {    
        if (str.isEmpty()) {    
            return false;    
        } else {    
            return str.length() == length ? true : false;    
        }    
    }    
    
    /**  
     * 验证手机格式  
     */    
    public static boolean isMobileNO(String mobileNums) {    
        /*  
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188  
         * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）  
         * 总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9  
         */    
        String telRegex = "[1][358]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。    
        if (TextUtils.isEmpty(mobileNums))    
            return false;    
        else    
            return mobileNums.matches(telRegex);    
    }
    /**  
     * progressbar  
     */    
    private void createProgressBar() {    
        FrameLayout layout = (FrameLayout) findViewById(android.R.id.content);    
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(    
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);    
        layoutParams.gravity = Gravity.CENTER;    
        ProgressBar mProBar = new ProgressBar(this);    
        mProBar.setLayoutParams(layoutParams);    
        mProBar.setVisibility(View.VISIBLE);    
        layout.addView(mProBar);    
    } 
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		SMSSDK.unregisterAllEventHandler();    
        super.onDestroy(); 
	}    
    
}
