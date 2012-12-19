package com.lzq.sd_watcher_Core;

import java.io.File;
import java.util.HashSet;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.lzq.sd_watcher_GUI.R;

public class SD_ObserveService extends Service {

	private final IBinder ssb = new SD_Service_Binder();
	private final String ROOTPATH = Environment.getExternalStorageDirectory().getPath()+"/";

	private String ResultMsg=null;
	private String deletePath=null;
	
	private Handler handler = new Handler();
	private Runnable showSD_Info = new Runnable() {
		public void run() {
			// TODO Auto-generated method stub
			showSystemDialog(ResultMsg);
		}
	};
	
	
	private SD_Observer Observer = null;
	private SD_ObserveService sos = this;
	private HashSet<String> SD_Files = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e("Service Info", "Service Created.");
		/**
		 * 服务开启时，首先将此时此刻SD根目录下的所有文件夹信息放入SD_Files中
		 */
		SD_Files = new HashSet();
		File sd = new File(ROOTPATH);
		File[] files = sd.listFiles();
		for (File f : files)
			SD_Files.add(f.getName());

		/**
		 * 在此处进行Service需要执行的操作，监听SDcard中的文件夹创建情况
		 */
		Observer = new SD_Observer(ROOTPATH);
		Observer.startWatching();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.e("Service Info", "Service Destoried.");
		if (Observer != null)
			Observer.stopWatching();
		Observer = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 当一个服务被绑定到一个Activity上时，ServiceConnection中的OnServiceConnected被调用，
	 * 它会通过Service的onBind函数
	 * 获得一个接口对象IBinder，它代表了新创建并连接的这个Service对象的一个接口，我们可以自定义这个接口中的
	 * 内容，在Activity中可以获得这个接口，那么就在这个接口类中实现相关方法，获得Service的实例，今儿就可以通过Service的
	 * 实例来和Service对象进行信息交互了
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return ssb;
	}

	/**
	 * 尝试利用Service创建全局对话框
	 * 
	 * @author LZQ
	 * 
	 */
	private void showSystemDialog(String SD_Info) {

		// LayoutInflater factory = LayoutInflater.from(sos);
		// View ResultView=factory.inflate(R.layout.globaldialog_layout, null);
		// ImageView
		// iv=(ImageView)ResultView.findViewById(R.id.ResultImageView);

		AlertDialog ResultDialog = new AlertDialog.Builder(sos)
				.setTitle("检测到对SD根目录的操作").setCancelable(false).setMessage(SD_Info)
				.setIcon(R.drawable.android).setPositiveButton("删除",  new DialogInterface.OnClickListener() 
                { 
                    public void onClick(DialogInterface dialog,int which) 
                    { 
                    
                   	 File f=new File(deletePath);
                   	 f.delete();   
                   	AlertDialog CommitDialog=new AlertDialog.Builder(sos).setCancelable(false).setIcon(R.drawable.android).setMessage("删除成功！").setPositiveButton("确定",null).create();
                   	CommitDialog.getWindow().setType(
            				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                   	CommitDialog.show();
                    } 
                } )
				.setNegativeButton("取消", null).create();

		ResultDialog.getWindow().setType(
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		ResultDialog.show();


	}

	public class SD_Service_Binder extends Binder {
		public SD_ObserveService getService() {
			return SD_ObserveService.this;
		}
	}

	public class SD_Observer extends FileObserver {

		public SD_Observer(String path) {
			super(path);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onEvent(int event, String path) {
			// TODO Auto-generated method stub

			/**
			 * 监听到文件创建活动
			 */

			/**
			 * 如果是对SD根目录下的操作
			 */

			if (path != null) {
				
				File f=new File(Environment.getExternalStorageDirectory().getPath()+"/"+path);
				if(!f.exists()&&SD_Files.contains(path))
				{
					/**
					 * 如果文件不存在了，也就是说文件被删除了，那么从名单中删除
					 */
					SD_Files.remove(path);
					System.out.println("delete file :"+path);
					return;
				}
				else if(!SD_Files.contains(path))
				{
					/**
					 * 原来的文件目录中不包含这个文件名称，说明这是一个新创建的文件
					 */
					SD_Files.add(path);
					System.out.println("create file :"+path);
					deletePath=new String(Environment.getExternalStorageDirectory().getPath()+"/"+path);
					ResultMsg=new String("SD根目录下有新文件创建:\n /sdcard/"+path+"\n如果您不希望应用程序更改您SD根目录下的文件内容，您可以通过删除按钮删除这个文件夹。");
					handler.post(showSD_Info);
				}

			}

		}

	}

}
