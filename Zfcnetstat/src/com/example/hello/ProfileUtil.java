package com.example.hello;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import java.util.concurrent.atomic.AtomicInteger;

public class ProfileUtil {
	public static final String mywebsite = "http://zfcnetstat.duapp.com";
	private SharedPreferences sp;
	private Activity myact;
	private Service mysrv;
	private Context myctx;
	public ProfileUtil(Service srv) {
		mysrv = srv;
		sp=mysrv.getSharedPreferences(mysrv.getString(R.string.config_filename), Context.MODE_PRIVATE);
	}
	public ProfileUtil(Activity act) {
		myact = act;
		sp=act.getSharedPreferences(act.getString(R.string.config_filename), Context.MODE_PRIVATE);
	}
	public ProfileUtil(Context ctx) {
		myctx = ctx;
		sp=ctx.getSharedPreferences(ctx.getString(R.string.config_filename), Context.MODE_PRIVATE);
	}
	//自己编写的方法
	/*
	 * 用来生成唯一的id
	 * 类似这样的格式：145835041706101
	 * 如果将系统部署到集群上面，情况有会有不同了，
	 * 不同的服务器集群生成的这个数字，是有重合的概率的，因此，一般情况是，
	 * 将集群中的每个机器进行编码，然后将机器编码放在这个标识的前面以示区分。
	 */
	private static AtomicInteger counter = new AtomicInteger(0);
	
	 public static void main(String[] args) {
         
	        System.out.println(ProfileUtil.getAtomicCounter());
	 }

	public static long getAtomicCounter() {
		if (counter.get() > 999999) {
			counter.set(1);
		}
		long time = System.currentTimeMillis();
		long returnValue = time * 100 + counter.incrementAndGet();
		return returnValue;
	}
	
	//uuid，返回去掉了-的uuid
	public static String getUUID() {

		String uuidStr = UUID.randomUUID().toString();

		uuidStr = uuidStr.substring(0, 8) + uuidStr.substring(9, 13)

				+ uuidStr.substring(14, 18) + uuidStr.substring(19, 23)

				+ uuidStr.substring(24);

		return uuidStr;

	}
	//读参数：读取姓名、学号等信息
	public String readParam(String sName) {

		return sp.getString(sName, "N/A");

	}
	//写参数：写入姓名、学号等信息
	public void writeParam(String key, String value) {

		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		editor.commit();

	}
	//读写配置文件
	public void writeTime(long time, String sName) {

		SharedPreferences.Editor editor = sp.edit();
		editor.putString(sName, Long.toString(time));
		editor.commit();

	}

	public long readTime(String sName) {

		String tmp = sp.getString(sName, "0");
		if (tmp == null)
			return 0l;
		else
			return Long.parseLong(tmp);
	}
	//md5加密
	public String getMD5(String info)
	{
	  try
	  {
	    MessageDigest md5 = MessageDigest.getInstance("MD5");
	    md5.update(info.getBytes("UTF-8"));
	    byte[] encryption = md5.digest();
	      
	    StringBuffer strBuf = new StringBuffer();
	    for (int i = 0; i < encryption.length; i++)
	    {
	      if (Integer.toHexString(0xff & encryption[i]).length() == 1)
	      {
	        strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
	      }
	      else
	      {
	        strBuf.append(Integer.toHexString(0xff & encryption[i]));
	      }
	    }
	      
	    return strBuf.toString();
	  }
	  catch (NoSuchAlgorithmException e)
	  {
	    return "";
	  }
	  catch (UnsupportedEncodingException e)
	  {
	    return "";
	  }
	}
	/**
     * 检查当前网络是否可用
     * 
     * @param context
     * @return
     */
    
    public boolean isNetworkAvailable()
    {
        Context context = myact.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            
            if(info != null && info.isAvailable() && (info.getState() == NetworkInfo.State.CONNECTED)){						
				return true;
			}
			
        }
        return false;
    }
    public void showMessage(String s){
    	if(myact!=null)
    		Toast.makeText(myact.getApplicationContext(), s, Toast.LENGTH_LONG).show();
    	else if(mysrv!=null)
    		Toast.makeText(mysrv.getApplicationContext(), s, Toast.LENGTH_LONG).show();
    		
	}
  
}