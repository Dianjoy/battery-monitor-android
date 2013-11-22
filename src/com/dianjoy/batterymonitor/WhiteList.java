package com.dianjoy.batterymonitor;

import java.util.ArrayList;
import java.util.List;

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

public class WhiteList extends Activity {
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
	/*存放控件*/ 
    class ViewHolder{
		 public TextView appName;
         public ImageView appIcon;
         public CheckBox select;
	}
	/*新建一个类继承BaseAdapter，实现视图与数据的绑定*/ 
	private class MyAdapter extends BaseAdapter {
	    private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局 /*构造函数*/ 
		public MyAdapter(Context context) {
		    this.mInflater = LayoutInflater.from(context);
        }

		@Override
		public int getCount() {
		    return listItem.size();//返回数组的长度        
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
		                    /*得到各个控件的对象*/                    
		          holder.appName = (TextView) convertView.findViewById(R.id.app_info_name);
		          holder.appIcon = (ImageView) convertView.findViewById(R.id.app_info_image);
		          holder.select = (CheckBox) convertView.findViewById(R.id.app_info_select);
		          convertView.setTag(holder);//绑定ViewHolder对象                   
		    } else{
		          holder = (ViewHolder)convertView.getTag();//取出ViewHolder对象                  
		    }
		            /*设置TextView显示的内容，即我们存放在动态数组中的数据*/             
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
		Bitmap oldbmp = drawableToBitmap(drawable); // drawable转换成bitmap
		Matrix matrix = new Matrix(); // 创建操作图片用的Matrix对象
		float scaleWidth = ((float) w / width); // 计算缩放比例
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // 建立新的bitmap，其内容是对原bitmap的缩放后的图
		return new BitmapDrawable(newbmp); // 把bitmap转换成drawable并返回
	}

	public Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth(); // 取drawable的长宽
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; // 取drawable的颜色格式
		Bitmap bitmap = Bitmap.createBitmap(width, height, config); // 建立对应bitmap
		Canvas canvas = new Canvas(bitmap); // 建立对应bitmap的画布
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); // 把drawable内容画到画布中
		return bitmap;
	}
	public int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}
}
