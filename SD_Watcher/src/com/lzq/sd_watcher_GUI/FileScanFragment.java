package com.lzq.sd_watcher_GUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.lzq.sd_watcher_GUI.R;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class FileScanFragment extends Fragment {
	/**
	 * �����ѡ�����Ҫ������¹��ܣ�
	 * 1.�оٳ�SDcard�µ������ļ���Ϣ���û�������������ļ��м������ļ���
	 * 2.����ĳһѡ�����ɾ���ļ��л��ļ�
	 */

	final String ROOTPATH=Environment.getExternalStorageDirectory().getPath();
	TextView CurrentPathView=null;
	ListView FileListView=null;
	View FileScanView=null;
	ToggleButton ServiceToggler=null;
	FileScanFragment self=this;
	ArrayList<String> FileName=null;
	ArrayList<String> FilePath=null;
	ArrayList<Map<String,Object>> FileList=null;
	SimpleAdapter ListAdapter=null;
	
	ProgressDialog loading=null;
	
	String currentPath=null;

	boolean loadover=false;
	
	
	
	
	Handler FileScanHandler = new Handler();
	/**
	 * �����ʵ����Runnable�ӿڵ����У�����ListAdapter�б��������ȥ��ʼ��FileListView���ý�������ʾ���ļ�����Ϣ
	 */
	Runnable UpdateFileList =new Runnable()
	{
		public void run()
		{
			loading.dismiss();
			FileListView.setAdapter(ListAdapter);
			FileListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		}
	};
//	Runnable UpdateUI = new Runnable() {
//		// String path=current_path;
//		public void run() {
//			refreshMyFileList(current_path);
//		}
//	};
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		initialFileList();
		refreshFileList(ROOTPATH);
		
	}
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stubreturn super.onCreateView(inflater,
		// container, savedInstanceState);
		FileScanView = inflater.inflate(R.layout.filescan_fragment_layout, null);
		return FileScanView;
	}
	
	
	private void initialFileList()
	{
		CurrentPathView=(TextView)FileScanView.findViewById(R.id.FileScanTextView);
		FileListView=(ListView)FileScanView.findViewById(R.id.FileScanListView);
		/**
		 * ���ÿ�������ť
		 */
		ServiceToggler=(ToggleButton)FileScanView.findViewById(R.id.ToggleServiceButton);
		ServiceToggler.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(ServiceToggler.isChecked())
				{
					/**
					 * ��һ�ε��
					 */
					Toast.makeText(FileScanView.getContext(),"��̨�����Ѿ�����,����Ϊ�����SDcard�е��ļ���Ϣ��", Toast.LENGTH_SHORT).show();
				   ((MainActivity)(self.getActivity())).doBindService();
				}
				else
				{
					Toast.makeText(FileScanView.getContext(),"��̨�����Ѿ��رա�", Toast.LENGTH_SHORT).show();
					((MainActivity)(self.getActivity())).doUnbindService();
				}
			}
			
		});
		/**
		 ** ���õ���ѡ�������
		 */
		FileListView.setOnItemClickListener(new FileItemClickedListener());	
		/**
		 * ���ó���ѡ�������
		 */
		FileListView.setOnItemLongClickListener(new FileItemLongClickedListener());
	}
	
	private void refreshFileList(String path)
	{
		FileName=new ArrayList();
		FilePath=new ArrayList();
		FileList=new ArrayList();
		currentPath=path;
		CurrentPathView.setText("current path: "+path);
		loading=ProgressDialog.show(FileScanView.getContext(), "refreshing file list......", "Please waiting ......");	
		refreshThread rthread=new refreshThread(path);	
		rthread.start();
	
	}
	
	
	
	/**
	 * ���ڼ���FileList�����ļ�ѡ�����ʱ���¼��ļ�����������ѡ�����ʱ������
	 * @author LZQ
	 *
	 */
	class FileItemClickedListener implements OnItemClickListener
	{

		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long ID) {
			// TODO Auto-generated method stub
			
			String clickedFilePath=FilePath.get(position);
			//System.out.println("Item clicked!  Clicked file path:"+clickedFilePath);
			if(new File(clickedFilePath).isDirectory())
		     	refreshFileList(clickedFilePath);
		}
		
	}
	/**
	 * ���ڼ���FileList��ѡ�����ʱ���¼�
	 */
	class FileItemLongClickedListener implements OnItemLongClickListener
	{

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int position, long ID) {
			/**
			 * ����ʱ�����Ի���ѯ���Ƿ�Ҫɾ���ļ�
			 */
			// TODO Auto-generated method stub
			final String clickedFilePath=FilePath.get(position);
			
			 new AlertDialog.Builder(FileScanView.getContext()) 
             .setTitle("ɾ������").setIcon(R.drawable.android)
             .setMessage("ȷ��ɾ���ļ� "+FileName.get(position)+" ��") 
             .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() 
                 { 
                     public void onClick(DialogInterface dialog,int which) 
                     { 
                    	 final ProgressDialog pd=ProgressDialog.show(FileScanView.getContext(), "ɾ����", "����ɾ���ļ���"+clickedFilePath);
                    	 new Thread()
                    	 {
                    		 public void run()
                    		 {
                    			 deleteFile(new File(clickedFilePath));
                    			 pd.dismiss();
                    		 }
                    	 }.start();                   	
                    	 refreshFileList(currentPath);

                     } 
                 } 
             ) 
             .setNegativeButton("ȡ��",null) .show(); 
			return true;
		}
		
	}
	
    /**
     * ɾ���ļ��м������ļ�
     * @param oldPath ��ɾ���ļ���
     */
    public void deleteFile(File oldPath) {
        if (oldPath.isDirectory()) {
            File[] files = oldPath.listFiles();
            for (File file : files) {
                deleteFile(file);
            }
            oldPath.delete();
        } else {
            oldPath.delete();
        }
    }

	/**
	 * ����ִ�и���FileList��һ���߳��࣬����������run����
	 * ��run�����У���ȡpathĿ¼�µ������ļ���Ϣ������Ҫ��ʾ���ļ������ļ�ͼ����Ϣ������ListAdapter��
	 * @author LZQ
	 *
	 */
	class refreshThread extends Thread
	{
		private String path;
		public refreshThread(String path)
		{
			this.path=path;
		}
		public void run()
		{

			File rootFile =new File(path);
			File[] subFiles=rootFile.listFiles();
			
			if(!path.equals(ROOTPATH))
			{
				FileName.add("...");
				FilePath.add(rootFile.getParent());
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("file_ico", R.drawable.folder_yellow);
				map.put("file_name", "...");
				FileList.add(map);			
			}
			/**
			 * �����г��ļ�����Ϣ
			 */
			for(File file:subFiles)
			{
				if(file.isDirectory()&&!(file.getName().startsWith(".")))//�ų�.android_secure�����������ļ�
				{			
					FileName.add(file.getName());
					FilePath.add(file.getAbsolutePath());
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("file_ico", R.drawable.folder_yellow);
					map.put("file_name",file.getName());
					FileList.add(map);	
				}
			}
			/**
			 * �г��������ļ�����Ϣ
			 */
			for(File file:subFiles)
			{
				if(!file.isDirectory())
				{
					//System.err.println("add "+file.getName()+"  "+file.getAbsolutePath());
					FileName.add(file.getName());
					FilePath.add(file.getAbsolutePath());
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("file_ico", R.drawable.folder);
					map.put("file_name",file.getName());
					FileList.add(map);	
				}
			}
			/**
			 * ��Ҫ��ʾ���ļ���Ϣ���������ListAdapter�У�Ȼ�������߳��е�Handelerȥ��ɸ���UI�Ĳ���
			 */
		    ListAdapter = new SimpleAdapter(FileScanView.getContext(),
					FileList, R.layout.filelist_item, new String[] { "file_ico",
							"file_name" }, new int[] { R.id.file_ico,
							R.id.file_name });
		    //System.out.println("post");
		    FileScanHandler.post(UpdateFileList);
		}
	}
			
}
