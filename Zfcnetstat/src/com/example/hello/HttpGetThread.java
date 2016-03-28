package com.example.hello;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class HttpGetThread implements Runnable {
	private String url;
	Handler handler;
	private String resultStr;
	public HttpGetThread(String urlstr,Handler h) {
		this.url = urlstr;
		this.handler = h;
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
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString("MyValue",resultStr);
        msg.setData(data);
        handler.sendMessage(msg);
	}

}
