package com.lzq.sd_watcher_GUI;

import com.lzq.sd_watcher_Core.SD_ObserveService;
import com.lzq.sd_watcher_GUI.R;

import android.os.Bundle;
import android.os.IBinder;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {

	
	private boolean mIsBound=false;
	private SD_ObserveService sobs=null;
	private ServiceConnection sc=new ServiceConnection()
	{

		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			sobs=((SD_ObserveService.SD_Service_Binder)service).getService();
		}

		
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			sobs=null;
			
		}
		
	};
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        /**
         * 添加动作条
         */
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// remove the activity title to make space for tabs
		actionBar.setDisplayShowTitleEnabled(true);
		
		/**
		 * 创建、添加选项卡
		 */
		Tab sd_view = actionBar.newTab().setText("资源管理器");
		Tab config_view = actionBar.newTab().setText("系统设置");
		
		/**
		 * 创建选项卡Tab之后，每一个Tab必须注册有一个监听器，用来完成选中Tab之后进行的操作。
		 * 这里创建了内部类SD_TabListener来完成选中Tab之后的一些列操作
		 */
		SD_TabListener sd_view_listener=new SD_TabListener(new FileScanFragment());
		SD_TabListener config_view_listener=new SD_TabListener(new ConfigurationFragment());
		/**
		 * 给Tab注册监听器
		 */
		sd_view.setTabListener(sd_view_listener);
		config_view.setTabListener(config_view_listener);
		
		/**
		 * 将Tab添加到ActionBar中
		 */
		actionBar.addTab(sd_view);
		actionBar.addTab(config_view);
		


		
		
		
    }

    
	/**
	 * 绑定后台监听服务
	 */
	public void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent("com.lzq.sd_watcher_service"), sc, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	public void doUnbindService() {
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(sc);
	        mIsBound = false;
	    }
	}
	
	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }
    /**
     * 内部类，为每一个Tab需要注册的监听器
     * @author LZQ
     *
     */
	class SD_TabListener implements TabListener {
		Fragment fragment;

		public SD_TabListener(Fragment ft) {
			fragment = ft;
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
		}

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			ft.add(R.id.TabLayout, fragment, null);
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			ft.remove(fragment);
		}
	}
}
