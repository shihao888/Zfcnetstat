package com.example.hello;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.RemoteViews;


/***
 * 更新版本
 * 
 * @author zhangjia
 * 
 */
public class UpdateService extends Service {
	private static final int TIMEOUT = 10 * 1000;// 超时
	private static final int DOWN_OK = 1;
	private static final int DOWN_ERROR = 0;
	private String app_english_name;
	private NotificationManager notificationManager;
	private Notification notification;	
	private int notification_id = 10;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		app_english_name = intent.getStringExtra("app_english_name");		
		// 创建文件
		FileUtil.createFile(app_english_name);	//创建文件
		createNotification();			//创建通知
		createThread();					//线程下载

		return super.onStartCommand(intent, flags, startId);

	}
	private Notification mkNotification(String strNotificationText){
    	Uri uri = Uri.fromFile(FileUtil.updateFile);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri,"application/vnd.android.package-archive");

		PendingIntent contentIntent = PendingIntent.getActivity(
				UpdateService.this, 0, intent, 0);
		notification = new Notification.Builder(this)  
	            .setContentTitle("浙江金融职业学院-"+getResources().getString(R.string.app_english_name))  
	            .setContentText("下载成功，点击安装")  
	            .setSmallIcon(R.drawable.ic_launcher).build();	    
	    notification.contentIntent = contentIntent;	
	    return notification;
    }
	private static class MyHandler extends Handler {
		
		private final UpdateService mService;
		
		public MyHandler(Looper looper, UpdateService s) {
			super(looper);			
			this.mService = new WeakReference<UpdateService>(s).get();
		}

		@Override		
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case DOWN_OK:
				// 下载完成，点击安装								
				mService.mkNotification("下载成功，点击安装");
				mService.notificationManager.notify(mService.notification_id, mService.notification);
				mService.stopSelf();// 停止服务
				break;
			case DOWN_ERROR:
				mService.mkNotification("下载失败");
				mService.notificationManager.notify(mService.notification_id, mService.notification);
				mService.stopSelf();// 停止服务
				break;
			default:
				mService.stopSelf();// 停止服务
				break;
			}
		}
	}
	/***
	 * 开线程下载
	 */
	public void createThread() {
		/***
		 * 更新UI
		 */
		final MyHandler h = new MyHandler(Looper.getMainLooper(),UpdateService.this);
	
		final Message message = new Message();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {					
					long downloadSize = downloadUpdateFile(Config.UPDATE_SERVER
							+ Config.UPDATE_APKNAME,
					FileUtil.updateFile.toString());
					if (downloadSize > 0) {
						// 下载成功
						message.what = DOWN_OK;
						h.sendMessage(message);
					}

				} catch (Exception e) {
					e.printStackTrace();
					message.what = DOWN_ERROR;
					h.sendMessage(message);
				}

			}
		}).start();
	}

	/***
	 * 创建通知栏
	 */
	RemoteViews contentView;

	public void createNotification() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification.Builder(this)
				.setContentTitle("浙江金融职业学院-"+getResources().getString(R.string.app_name))  
	            .setContentText("下载新版本") 
			    .setSmallIcon(R.drawable.ic_launcher)// 这个图标必须要设置，不然下面那个RemoteViews不起作用
			    .build();
		// 这个参数是通知提示闪出来的值.
		notification.tickerText = "开始下载"; 

		/***
		 * 在这里我们用自定的view来显示Notification
		 */
		contentView = new RemoteViews(getPackageName(),
				R.layout.notification_item);
		contentView.setTextViewText(R.id.notificationTitle, "正在下载");
		contentView.setTextViewText(R.id.notificationPercent, "0%");
		contentView.setProgressBar(R.id.notificationProgress, 100, 0, false);

		notification.contentView = contentView;

		Intent updateIntent = new Intent(this, MainActivity.class);
		updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);

		notification.contentIntent = pendingIntent;

		notificationManager.notify(notification_id, notification);

	}

	/***
	 * 下载文件
	 * 
	 * @return
	 * @throws MalformedURLException
	 */
	public long downloadUpdateFile(String down_url, String file)
			throws Exception {
		int down_step = 5;// 提示step
		int totalSize;// 文件总大小
		int downloadCount = 0;// 已经下载好的大小
		int updateCount = 0;// 已经上传的文件大小
		InputStream inputStream;
		OutputStream outputStream;

		URL url = new URL(down_url);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();
		httpURLConnection.setConnectTimeout(TIMEOUT);
		httpURLConnection.setReadTimeout(TIMEOUT);
		// 获取下载文件的size
		totalSize = httpURLConnection.getContentLength();
		if (httpURLConnection.getResponseCode() == 404) {
			throw new Exception("fail!");
		}
		inputStream = httpURLConnection.getInputStream();
		outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉
		byte buffer[] = new byte[1024];
		int readsize = 0;
		while ((readsize = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, readsize);
			downloadCount += readsize;// 时时获取下载到的大小
			/**
			 * 每次增张5%
			 */
			if (updateCount == 0
					|| (downloadCount * 100 / totalSize - down_step) >= updateCount) {
				updateCount += down_step;
				// 改变通知栏
				// notification.setLatestEventInfo(this, "正在下载...", updateCount
				// + "%" + "", pendingIntent);
				contentView.setTextViewText(R.id.notificationPercent,
						updateCount + "%");
				contentView.setProgressBar(R.id.notificationProgress, 100,
						updateCount, false);
				// show_view
				notificationManager.notify(notification_id, notification);

			}

		}
		if (httpURLConnection != null) {
			httpURLConnection.disconnect();
		}
		inputStream.close();
		outputStream.close();

		return downloadCount;

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

}
