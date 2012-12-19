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
		 * ������ʱ�����Ƚ���ʱ�˿�SD��Ŀ¼�µ������ļ�����Ϣ����SD_Files��
		 */
		SD_Files = new HashSet();
		File sd = new File(ROOTPATH);
		File[] files = sd.listFiles();
		for (File f : files)
			SD_Files.add(f.getName());

		/**
		 * �ڴ˴�����Service��Ҫִ�еĲ���������SDcard�е��ļ��д������
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
	 * ��һ�����񱻰󶨵�һ��Activity��ʱ��ServiceConnection�е�OnServiceConnected�����ã�
	 * ����ͨ��Service��onBind����
	 * ���һ���ӿڶ���IBinder�����������´��������ӵ����Service�����һ���ӿڣ����ǿ����Զ�������ӿ��е�
	 * ���ݣ���Activity�п��Ի������ӿڣ���ô��������ӿ�����ʵ����ط��������Service��ʵ��������Ϳ���ͨ��Service��
	 * ʵ������Service���������Ϣ������
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return ssb;
	}

	/**
	 * ��������Service����ȫ�ֶԻ���
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
				.setTitle("��⵽��SD��Ŀ¼�Ĳ���").setCancelable(false).setMessage(SD_Info)
				.setIcon(R.drawable.android).setPositiveButton("ɾ��",  new DialogInterface.OnClickListener() 
                { 
                    public void onClick(DialogInterface dialog,int which) 
                    { 
                    
                   	 File f=new File(deletePath);
                   	 f.delete();   
                   	AlertDialog CommitDialog=new AlertDialog.Builder(sos).setCancelable(false).setIcon(R.drawable.android).setMessage("ɾ���ɹ���").setPositiveButton("ȷ��",null).create();
                   	CommitDialog.getWindow().setType(
            				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                   	CommitDialog.show();
                    } 
                } )
				.setNegativeButton("ȡ��", null).create();

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
			 * �������ļ������
			 */

			/**
			 * ����Ƕ�SD��Ŀ¼�µĲ���
			 */

			if (path != null) {
				
				File f=new File(Environment.getExternalStorageDirectory().getPath()+"/"+path);
				if(!f.exists()&&SD_Files.contains(path))
				{
					/**
					 * ����ļ��������ˣ�Ҳ����˵�ļ���ɾ���ˣ���ô��������ɾ��
					 */
					SD_Files.remove(path);
					System.out.println("delete file :"+path);
					return;
				}
				else if(!SD_Files.contains(path))
				{
					/**
					 * ԭ�����ļ�Ŀ¼�в���������ļ����ƣ�˵������һ���´������ļ�
					 */
					SD_Files.add(path);
					System.out.println("create file :"+path);
					deletePath=new String(Environment.getExternalStorageDirectory().getPath()+"/"+path);
					ResultMsg=new String("SD��Ŀ¼�������ļ�����:\n /sdcard/"+path+"\n�������ϣ��Ӧ�ó��������SD��Ŀ¼�µ��ļ����ݣ�������ͨ��ɾ����ťɾ������ļ��С�");
					handler.post(showSD_Info);
				}

			}

		}

	}

}
