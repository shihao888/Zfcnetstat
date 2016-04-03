package com.example.hello;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class HttpGetThread implements Runnable {
	private String url;
	Handler handler;
	private String resultStr;
	private int msgType;
	public HttpGetThread(String urlstr,Handler h,int messageType) {
		this.url = urlstr;
		this.handler = h;
		this.msgType = messageType;
	}

	@Override
	public void run() {
		// TODO: http request.
    	try {
			resultStr = new HttpRequestor().doGet(url);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//http://shmilyaw-hotmail-com.iteye.com/blog/1881302
		}
        Message msg = handler.obtainMessage();//http://www.2cto.com/kf/201311/255885.html
        Bundle data = new Bundle();
        data.putString("MyValue",resultStr);
        msg.setData(data);
        msg.what = msgType;
        handler.sendMessage(msg);
	}

}
