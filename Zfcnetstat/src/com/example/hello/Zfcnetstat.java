package com.example.hello;

import android.app.Application;
/* 保存全局变量
 * http://www.cnblogs.com/xiongbo/archive/2011/05/18/2050425.html
 */
public class Zfcnetstat extends Application {

	private boolean isPrompted;

	public boolean isPrompted() {
		return isPrompted;
	}

	public void setPrompted(boolean b) {
		this.isPrompted = b;
	}
}
