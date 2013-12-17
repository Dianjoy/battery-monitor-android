package com.lobster.batterymonitor;

import java.util.ArrayList;
import java.util.List;

import com.dianjoy.batterymonitor.R;
import com.lobster.batterymonitor.tools.AppInfo;
import com.lobster.batterymonitor.tools.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class WhiteList extends UmentActivity {
	private ListView appInfo;
	private ArrayList<AppInfo> listItem;
	private Context context;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_whitelist);
		appInfo = (ListView) findViewById(R.id.white_list_view);
		listItem = new ArrayList<AppInfo>();
		context = this;
		getData();
		MyAdapter adapter = new MyAdapter(this);
		appInfo.setAdapter(adapter);
		
	}
	private ArrayList<AppInfo> getData(){
		 PackageManager pm = getPackageManager();
		 List<PackageInfo> packages = pm.getInstalledPackages(0);
		 for (PackageInfo pack : packages) {
			 if ((pack.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0) {
				 Drawable icon = zoomDrawable(pack.applicationInfo.loadIcon(pm), dip2px(context,65), dip2px(context,65));
				 String pName = pack.applicationInfo.packageName;
				 String name = pack.applicationInfo.loadLabel(getPackageManager()).toString(); 
				 listItem.add(new AppInfo(icon, pName, name));
			 }
		 }
		 return listItem;
		    
     }
	/*��ſؼ�*/ 
    class ViewHolder{
		 public TextView appName;
         public ImageView appIcon;
         public CheckBox select;
	}
	/*�½�һ����̳�BaseAdapter��ʵ����ͼ����ݵİ�*/ 
	private class MyAdapter extends BaseAdapter {
	    private LayoutInflater mInflater;//�õ�һ��LayoutInfalter�����������벼�� /*���캯��*/ 
		public MyAdapter(Context context) {
		    this.mInflater = LayoutInflater.from(context);
        }

		@Override
		public int getCount() {
		    return listItem.size();//��������ĳ���        
		}
		@Override
		public Object getItem(int position) {
		    return null;
		}

		@Override
		public long getItemId(int position) {
		    return 0;
		}
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
		    ViewHolder holder;            
		    if (convertView == null) {
		          convertView = mInflater.inflate(R.layout.app_info_layout,null);
		          holder = new ViewHolder();
		                    /*�õ������ؼ��Ķ���*/                    
		          holder.appName = (TextView) convertView.findViewById(R.id.app_info_name);
		          holder.appIcon = (ImageView) convertView.findViewById(R.id.app_info_image);
		          holder.select = (CheckBox) convertView.findViewById(R.id.app_info_select);
		          convertView.setTag(holder);//��ViewHolder����                   
		    } else{
		          holder = (ViewHolder)convertView.getTag();//ȡ��ViewHolder����                  
		    }
		            /*����TextView��ʾ�����ݣ������Ǵ���ڶ�̬�����е����*/             
		    holder.appName.setText(listItem.get(position).getName());
		    holder.appIcon.setImageDrawable(listItem.get(position).getIcon());
		    
		    holder.select.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub
					Utils.setPreferenceStr(context,listItem.get(position).getPackName(), isChecked+"");
					//Log.i("test11", isChecked + "" + listItem.get(position).getPackName());
				}
		    	
		    });
		    boolean allow = false;
		    if (Utils.getPreferenceStr(context, listItem.get(position).getPackName(), false +"").equals("true")){
		    	allow = true;
		    }
		    holder.select.setChecked(allow);
		    return convertView;
        }	    
	}
	public void backButton(View v) {
		this.finish();
	}
	public Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable); // drawableת����bitmap
		Matrix matrix = new Matrix(); // ��������ͼƬ�õ�Matrix����
		float scaleWidth = ((float) w / width); // �������ű���
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight); // �������ű���
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // �����µ�bitmap���������Ƕ�ԭbitmap�����ź��ͼ
		return new BitmapDrawable(newbmp); // ��bitmapת����drawable������
	}

	public Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth(); // ȡdrawable�ĳ���
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; // ȡdrawable����ɫ��ʽ
		Bitmap bitmap = Bitmap.createBitmap(width, height, config); // ������Ӧbitmap
		Canvas canvas = new Canvas(bitmap); // ������Ӧbitmap�Ļ���
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); // ��drawable���ݻ���������
		return bitmap;
	}
	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
}
