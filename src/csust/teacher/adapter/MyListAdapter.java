package csust.teacher.adapter;

import java.io.File;
import java.util.List;







import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import csust.teacher.activity.CourseDetailActivity;
import csust.teacher.activity.DownloadActivity;
import csust.teacher.activity.R;
import csust.teacher.download.MyFileDownLoader;
import csust.teacher.info.CourseInfo;
import csust.teacher.model.Model;
import csust.teacher.net.ThreadPoolUtils;
import csust.teacher.thread.HttpGetThread;
import csust.teacher.utils.LoadImg;
import csust.teacher.utils.LoadImg.ImageDownloadCallBack;

/**
 * mylistview的适配器
 * @author U-anLA
 *
 */
public class MyListAdapter extends BaseAdapter{
	public static final String ACTION_DOWNLOAD_PROGRESS = "my_download_progress";
	public static final String ACTION_DOWNLOAD_SUCCESS = "my_download_success";
	public static final String ACTION_DOWNLOAD_FAIL = "my_download_fail";
	
	private List<CourseInfo> list;
	private Context ctx;
	private LoadImg loadImgHeadImg;
	private boolean upFlag = false;
	private boolean downFlag = false;
	private ProgressDialog mProDialog;
	
	/** Notification管理 */
	public NotificationManager mNotificationManager;
	

	

	public MyListAdapter(NotificationManager mNotificationManager,Context ctx, List<CourseInfo> list) {
		mProDialog = new ProgressDialog(ctx);
		mProDialog.setCancelable(true);
		this.list = list;
		this.ctx = ctx;
		//加载图像
		loadImgHeadImg = new LoadImg(ctx);
		this.mNotificationManager = mNotificationManager;
	}
	public MyListAdapter(Context ctx, List<CourseInfo> list) {
		mProDialog = new ProgressDialog(ctx);
		mProDialog.setCancelable(true);
		this.list = list;
		this.ctx = ctx;
		//加载图像
		loadImgHeadImg = new LoadImg(ctx);
		this.mNotificationManager = mNotificationManager;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final Holder hold;
		
		
		if(convertView == null){
			hold = new Holder();
			convertView = View.inflate(ctx, R.layout.mylistview_item, null);
			hold.teacherPic = (ImageView) convertView.findViewById(R.id.itemTeacherPic);
			hold.teacherNum = (TextView) convertView.findViewById(R.id.itemTeacherNum);
			hold.courseName = (TextView) convertView.findViewById(R.id.itemCourseName);
			hold.teacherName = (TextView) convertView.findViewById(R.id.itemTeacherName);
			hold.btnDownload = (Button) convertView.findViewById(R.id.list_downloadData);
			convertView.setTag(hold);
		}else{
			hold = (Holder) convertView.getTag();
		}
		Object b = convertView.getTag();

		hold.teacherNum.setText(list.get(position).getTeacherNum());
		
		hold.teacherName.setText(list.get(position).getTeacherName());
		hold.courseName.setText(list.get(position).getCourseName());
		//设置监听
		hold.teacherPic.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//用于和教师会话~，后期实现。
				if(Model.MYUSERINFO != null){
					//已登录
				}else{
					Toast.makeText(ctx, "请先登录才能发送消息哦", 1).show();
				}
			}
		});
		
		hold.btnDownload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//首先用于生成excel表格，生成好后，既可以下载。
				String myUrl = Model.GETSIGNEDREPORT + "course_id="+list.get(position).getCourseName();
				ThreadPoolUtils.execute(new HttpGetThread(hand, myUrl));
				
				mProDialog.setTitle("正在生成签到数据报表，请勿取消！");
				mProDialog.show();
			}
		});
		
		
		hold.teacherPic.setImageResource(R.drawable.default_users_avatar);
		if(list.get(position).getTeacherNum().equalsIgnoreCase("")){
			hold.teacherPic.setImageResource(R.drawable.default_users_avatar);
		}else{
			hold.teacherPic.setTag(Model.USERHEADURL + list.get(position).getTeacherNum());
			Bitmap bitTeacher = loadImgHeadImg.loadImage(hold.teacherPic, Model.USERHEADURL+list.get(position).getTeacherNum(), new ImageDownloadCallBack() {
				@Override
				public void onImageDownload(ImageView imageView, Bitmap bitmap) {
					if(position >= list.size()){
						if(hold.teacherPic.getTag().equals(Model.USERHEADURL+list.get(position-1).getTeacherNum())){
							hold.teacherPic.setImageBitmap(bitmap);
						}
					}else{
						if(hold.teacherPic.getTag().equals(Model.USERHEADURL+list.get(position).getTeacherNum())){
							hold.teacherPic.setImageBitmap(bitmap);
						}
					}
				}
			});
			if(bitTeacher != null){
				hold.teacherPic.setImageBitmap(bitTeacher);
			}
		}
		
		return convertView;
		
	}
	
	static class Holder{
		ImageView teacherPic;
		TextView teacherNum;
		TextView courseName;
		TextView teacherName;
		Button btnDownload;
	}
	
	/**
	 * 用于让服务器生成报表的handler
	 */
	Handler hand = new Handler(){
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			if (msg.what == 404) {
				Toast.makeText(ctx, "找不到服务器地址", 1).show();
			} else if (msg.what == 100) {
				Toast.makeText(ctx, "传输失败", 1).show();
			} else if (msg.what == 200) {
				mProDialog.dismiss();
				// 正确的处理逻辑
				String result = (String) msg.obj;
				if(result.equals("[0]")){
					Toast.makeText(MyListAdapter.this.ctx, "抱歉，生成失败！", 1).show();
				}else{
					Toast.makeText(MyListAdapter.this.ctx, "生成数据报表文件成功！", 1).show();
					//进入下载页面，直接进行下载，利用notification
					beginDownload(Model.USERREPORTURL+result);
					
				}
				
				
				
				
			}
		};
	};
	
	/**
	 * 用于开始下载数据
	 * 
	 * @param url
	 */
	private void beginDownload(String url){
		
		Intent intent = new Intent(ctx, DownloadActivity.class);
		Bundle bund = new Bundle();
		bund.putSerializable("url", url);
		// intent.putExtra("value", bund);
		intent.putExtras(bund);
		ctx.startActivity(intent);
		
		
//		MyFileDownLoader myDownloader = new MyFileDownLoader(ctx, mNotificationManager, url);
//		myDownloader.startDownloadService();
		
	}

	
}













