package com.example.hello;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * 
 * @ClassName: ChkNewVersionReceiver  
 * @Description: 每天闹铃时间到了会进入这个广播，这个时候会检查是否有新版本更新。
 * @author:shihao sheraton@sina.com
 * @date: 2016
 *
 */

public class ChkNewVersionReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// 启动一个Activity       
        Intent activityIntent = new Intent(context, MainActivity.class);    
        //  要想在Service中启动Activity，必须设置如下标志（网上搜的才知道这点）    
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
        context.startActivity(activityIntent);  
	}

}
