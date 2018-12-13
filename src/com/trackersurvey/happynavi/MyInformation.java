package com.trackersurvey.happynavi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.Des;
import com.trackersurvey.helper.MD5Util;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.httpconnection.updateInformation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MyInformation extends Activity {
	private boolean isSelectIcon = false;
	private LinearLayout back;
	private ProgressDialog proDialog=null;
	LinearLayout ll;
	TextView user_id;
	EditText name;
	EditText usertel;
	EditText pswStr;// 密保提示
	DatePicker birthday;
	EditText birth;
	// 所有控件
	ArrayList<View> views = new ArrayList<View>();
	int select;// 判断哪种获得照片方式
	// 以下7行 图片处理用
	private static final int GET_IMAGE_VIA_CAMERA = 1;
	private static final int REQUEST_PICTURE = 2;

	private int imageWidth;
	private int imageHeight;
	ImageView mImageView;

	File mediaStorageDir = new File(
            Common.PHOTO_PATH,
             "happynavi_nick"+Common.currentTime()+".jpg");
	
	Uri uri;
	// 设置本地文件夹和要保存的图片文件
	String  fileName = "new_picture.jpg";

	ArrayList<String> commit = new ArrayList<String>();
	// 以下4行处理个人消息
	int index = 0;
	int count = 1;// 记录有几个下拉框
	ArrayList<Integer> order = new ArrayList<Integer>();
	ArrayList<Integer> order2 = new ArrayList<Integer>();

	String result;
	String s_birthday;
	// 存储填空的选项 为了匹配
	private String[] itemss = new String[500];
	// 更新URL
	//String update_url = Common.url + "userChgInfo.aspx";
	// 存储修改后图片
	String save_newpic;
	String save_nickname;
	String version = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_information);
		AppManager.getAppManager().addActivity(this);
		back = (LinearLayout) findViewById(R.id.personalinfo_back);
		ll = (LinearLayout) findViewById(R.id.layout);

		birthday = (DatePicker) findViewById(R.id.dpPicker);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}

		});
		if (proDialog == null){
			proDialog = new ProgressDialog(this);
		}
		// user_id.setText(Common.userId);

		// 接收到的登陆界面显示信息
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();

		String register_information = bundle.getString("register_information");
		String information = bundle.getString("information");
		
		//Log.i("register_information", register_information);
		//Log.i("information", information);

		// 初始化拍照图片
		initImageView();
		// 初始化账号
		initId();

		/*
		 * 从这开始需要从服务器拿信息给予赋值的控件均要加入View
		 * 
		 * 以 昵称(0) 生日(1) 密码保护信息 (2) + XXX的顺序加入
		 */

		// 初始化昵称
		initName();
		// 初始化密码提示信息
		initPswStr();

		// 初始化生日
		initBirthday();

		// 初始化密码保护信息

		// views加入昵称
		views.add(name);

	
		//3.2决定暂时去除密码提示信息（即备用密码）
		// views加入密码提示信息
		views.add(pswStr);
		//pswStr.setVisibility(View.GONE);
		// views加入生日
		views.add(birthday);

		// 初始化密码保护信息

		// ------------------------------------------------------------

		/*
		 * 不要动 处理register_information 拿到信息字符串 用?分隔开 * 均为输入框
		 * 
		 * #为选择题 均为选择框 用!分割选项
		 * 
		 * 初始化各个控件
		 */
		
		ArrayList<Integer> select_num=new ArrayList<Integer>( );
		if(register_information!=null &&!register_information.equals("")){
			String[] title = register_information.split("[?]");
	
			for (int i = 0; i < title.length; i++) {
	
				if (title[i].startsWith("*")) {
	
					String str = title[i].substring(1, title[i].length());
	
					initTextView(str);
	
				} else if (title[i].startsWith("#")) {
					order.add(i);// order 好像没用了
					order2.add(i + 3);// +3 因为有 昵称 生日 密码保护信息
										// order2记录spinner在View中的位置
					String[] items = title[i].split("!");
					String[] show = new String[items.length - 1];// -1是因为第一项是Textview要显示的文本
					select_num.add(items.length - 1);
					for (int l = 1; l < items.length; l++) {
						show[l - 1] = items[l];
						// itemss.set(index++, items[l]);
						itemss[index++] = items[l];
					}
					index = 17 * count;// 17看心情 随便设置一个数 数要大于最大的选项数目
					count++;
					String str = items[0].substring(1, items[0].length());
	
					initSpinner(str, show);// 给下拉框赋值
	
				}
	
			}
		}
		// ------------------------------------------------------------
		/*
		 * 给上面声明的控件附上个人信息 赋值 昵称 生日 密保信息 + 若干
		 * 
		 * 显示： 账号 昵称 生日 密保信息 +若干
		 */

		try {
			information=Des.decrypt(information);
		} catch (Exception e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		String informations[] = information.split("[?]");
		
		
		//判断是否进行DES
				//若版本号小于"1.3.3"则不解密
		
		if(Common.version == null ||Common.version.equals("")){
			version = Common.getAppVersionName(getApplicationContext());
		}else{
			version = Common.version;
		}
		boolean jiemi = false;
		if(version != null && !version.equals("")){
			jiemi=( version.compareTo("1.3.3")>=0);
		}
				
				if(jiemi)
				{  
					for(int i=0;i<informations.length;i++)
					{
						if(i!=1)//备用密码不解密
						{
						try {
							informations[i]=Des.decrypt(informations[i]);
							//Log.i("information"+i, informations[i]);
						} catch (Exception e) {
							// TODO 自动生成的 catch 块
							e.printStackTrace();
						}
						
					}
						
					}
					
				}
		
		
		
		
		
		
		
		
		
		
		if (informations.length <= 1) {
			return;
		}
		// save 用来保存选择框里 个人信息所选择的项
		int save[] = new int[count - 1];

		
//	for(int i=0;i<itemss.length;i++)
//		{
//			Log.i("itemss_i", i+"  "+itemss[i]);
//			
//		}
		
	for(int i=0;i<select_num.size();i++)
	{
		Log.i("num", ""+select_num.get(i));
		
	}
		int index = 0;
        int in_index=0;
      //  Log.i("infor.len",informations.length +"");
        int select=0;
		for (int i = 3; i < informations.length; i++) {
			for (int m = in_index; m < itemss.length; m++) {

		
				if (informations[i].equals(itemss[m])) {
					Log.i("item",informations[i]+"  "+ itemss[m]+" " +m);
					Log.i("in_index",in_index+"");
				
				Log.i("index",index+"");
					save[index] = m % 17;//
				Log.i("save[i]",i+"  "+m+ "   "+   (m%17)+"   "+itemss[m]);
					
				Log.i("select_num.indexOf(select)-m%17",select_num.get(select)+" "+m%17+"");
				in_index=m+1+(select_num.get(select)-m%17);
					index++;
					select++;
					Log.i("i",i+"");
					 break;
				}
				
			}

		}

		// views 里面顺序 昵称0 密码提示信息1 生日2 +xxxx
		int count2 = 0;
		for (int i = 0; i < views.size(); i++) {

			if (order2.contains(i))// xuanze
			{
				((AdapterView<SpinnerAdapter>) views.get(i))
						.setSelection(save[count2++]);

			}

			else if (i == 2) {

				// 处理生日
				String time[] = informations[i].split(" ");

				birth.setText(time[0]);
				// s_birthday=time[0];
				birth.setTextColor(Color.BLACK);

			} else 
			
			 
			
			{

				((EditText) views.get(i)).setText(informations[i]);
			}

		}
		// ---------------------------------------------------------
	}

	// 图片按钮拍照
	public void initImageView() {

		mImageView = (ImageView) findViewById(R.id.info_photo);

		Bitmap bmp;
		// final Bitmap img=BitmapFactory.decodeResource(this.getResources(),
		// R.drawable.headicon);
		String headphoto=Common.getHeadPhoto(getApplicationContext());
		if (headphoto == null || headphoto.equals("")
				|| headphoto.equals("null")) {
			bmp = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.icon_register);
		} else {

			byte[] decode1 = Base64.decode(headphoto, Base64.DEFAULT);
			bmp = BitmapFactory.decodeByteArray(decode1, 0, decode1.length);

		}

		imageWidth = bmp.getWidth();
		imageHeight = bmp.getHeight();

		mImageView.setImageBitmap(bmp);

		// mImageView.setImageResource(R.drawable.icon_register);
		// Bitmap bmp = BitmapFactory.decodeResource(getResources(),
		// R.drawable.icon_register);

		// params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		// params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

		mImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				// photo();

				CharSequence[] items = getResources().getStringArray(R.array.way_selectpic);
				select = 0;
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MyInformation.this);

				builder.setTitle(getResources().getString(R.string.tips_dlgtle_selectpic));

				builder.setSingleChoiceItems(items, 0,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								// TODO 自动生成的方法存根
								select = which;

							}

						});
				builder.setPositiveButton(getResources().getString(R.string.confirm),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								if (select == 0) {
									photo();
								} else {
									Album();

								}
							}
						});
				builder.create().show();

			}
		});
	}

	// 启动照相机
	public void photo() {
		Intent intent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		uri = Uri.fromFile(mediaStorageDir);

		intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, GET_IMAGE_VIA_CAMERA);

	}

	// 打开图库
	public void Album() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		startActivityForResult(intent, REQUEST_PICTURE);

	}

	// 图片处理
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1:
				/**
				Bitmap bmp = BitmapFactory.decodeFile(mediaStorageDir.getAbsolutePath());
				*/
				Bitmap bmp = Common.scaleBitmap(mediaStorageDir.getAbsolutePath(), 
						Common.winWidth, Common.winHeight);
				bmp = ThumbnailUtils.extractThumbnail(bmp, imageWidth,
						imageHeight);
				FileOutputStream os;
				try {
					os = openFileOutput(fileName,Context.MODE_PRIVATE);
					bmp.compress(Bitmap.CompressFormat.JPEG, 90, os);
					isSelectIcon = true;
					try {
						os.flush();
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}

				mImageView.setImageBitmap(bmp);
				Log.i("我拍的照片", mediaStorageDir.getAbsolutePath());
				// caozuo
				break;
			case 2:
				Uri uri = data.getData();
				if (!TextUtils.isEmpty(uri.getAuthority())||!TextUtils.isEmpty(uri.getScheme())) {
					// 查询选择图片
					Cursor cursor = getContentResolver().query(uri,
							new String[] { MediaStore.Images.Media.DATA },
							null, null, null);
					// 返回 没找到选择图片
					String selectImage =null;
					if (null != cursor) {
						// 光标移动至开头 获取图片路径
						cursor.moveToFirst();
						selectImage = cursor.getString(cursor
								.getColumnIndex(MediaStore.Images.Media.DATA));
						cursor.close();
					}else{
						selectImage = Uri.decode(uri.getEncodedPath());
					}
					/** OOM
					Bitmap bmp2 = BitmapFactory.decodeFile(selectImage);
					*/
					Bitmap bmp2 = Common.scaleBitmap(selectImage,
							Common.winWidth, Common.winHeight);
					bmp2 = ThumbnailUtils.extractThumbnail(bmp2, imageWidth,
							imageHeight);
					mImageView.setImageBitmap(bmp2);
					FileOutputStream os2;
					try {
						os2 =openFileOutput(fileName, Context.MODE_PRIVATE);
						bmp2.compress(Bitmap.CompressFormat.JPEG, 90, os2);
						isSelectIcon = true;
						try {
							os2.flush();
							os2.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} catch (FileNotFoundException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				}
				break;
			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 上传图片 字节流
	public String testUpload(String path) {
		try {

			String srcUrl = path; // "/mnt/sdcard/"; //路径
			// String fileName = PhotoName+".jpg"; //文件名
			FileInputStream fis = openFileInput(srcUrl);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[65536];
			int count = 0;
			while ((count = fis.read(buffer)) >= 0) {
				baos.write(buffer, 0, count);
			}
			String uploadBuffer = new String(Base64.encode(baos.toByteArray(),
					Base64.DEFAULT)); // 进行Base64编码

			fis.close();// 这两行原来没有
			baos.flush();
			return uploadBuffer;

		} catch (Exception e) {
			e.printStackTrace();
		}
		// return soapObject;
		return null;

	}

	// 初始化name id psw psww
	public void initName() {

		LinearLayout l = new LinearLayout(this);

		TextView tv = new TextView(this);
		tv.setId(1);

		tv.setText(getResources().getString(R.string.nickname_div));
		
		tv.setTextSize(18);
		tv.setTextColor(Color.rgb(35, 35, 35));
		tv.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// lp1.addRule(RelativeLayout.CENTER_VERTICAL);
		tv.setLayoutParams(lp1);
		lp1.weight = 1;
		l.addView(tv);

		name = new EditText(this);

		name.setId(2);// 猜猜看为啥？
		// edittexts.add(et);
		name.setSingleLine(true);
		name.setTextSize(18);
		name.setPadding(360, 0, 0, 0);
		name.setBackgroundResource(R.drawable.edittext_login);
		// name.setHint("例如：足迹");
		name.setHighlightColor(Color.rgb(169, 169, 169));
		name.setHighlightColor(Color.BLUE);
		name.setHint(getResources().getString(R.string.nickname_hint));

		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// lp2.addRule(RelativeLayout.RIGHT_OF,1);
		// 
		// lp2.addRule(RelativeLayout.ALIGN_BOTTOM,1);
		lp2.weight = 2;
		name.setLayoutParams(lp2);
		l.addView(name);

		ll.addView(l);

	}

	public void initId() {

		LinearLayout l = new LinearLayout(this);

		TextView tv = new TextView(this);
		tv.setId(1);
		tv.setText(getResources().getString(R.string.id_div));

		tv.setTextSize(18);
		tv.setTextColor(Color.rgb(35, 35, 35));
		tv.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// lp1.addRule(RelativeLayout.CENTER_VERTICAL);
		tv.setLayoutParams(lp1);
		lp1.weight = 1;
		l.addView(tv);

		usertel = new EditText(this);
		usertel.setText(Common.getUserId(this));
		usertel.setEnabled(false);
		usertel.setId(2);// 猜猜看为啥？
		// edittexts.add(et);
		usertel.setSingleLine(true);
		usertel.setTextSize(18);
		usertel.setPadding(360, 0, 0, 0);
		usertel.setBackgroundResource(R.drawable.edittext_login);
		usertel.setInputType(InputType.TYPE_CLASS_NUMBER);
		usertel.setHighlightColor(Color.rgb(169, 169, 169));
		usertel.setHighlightColor(Color.BLUE);

		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		lp2.weight = 2;
		usertel.setLayoutParams(lp2);
		l.addView(usertel);

		ll.addView(l);

	}

	// 密码提示信息
	public void initPswStr() {

		LinearLayout l = new LinearLayout(this);
		l.setOrientation(LinearLayout.HORIZONTAL);
		TextView tv = new TextView(this);
		tv.setId(1);

		tv.setText(getResources().getString(R.string.secondpwd_div));
		tv.setTextSize(18);
		tv.setTextColor(Color.rgb(35, 35, 35));
		tv.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// lp1.addRule(RelativeLayout.CENTER_VERTICAL);
		lp1.gravity = Gravity.CENTER;
		tv.setLayoutParams(lp1);
		lp1.weight = 1;
		l.addView(tv);

		
		pswStr = new EditText(this);

		pswStr.setId(2);// 猜猜看为啥？
		// edittexts.add(et);
		// pswStr.setSingleLine(true);
		pswStr.setTextSize(18);
		// pswStr.setPadding(360, 0, 0, 0);
		pswStr.setBackgroundResource(R.drawable.edittext_login);
		// pswStr.setHint("例如：足迹");
		pswStr.setHighlightColor(Color.rgb(169, 169, 169));
		pswStr.setHighlightColor(Color.BLUE);
		// pswStr.setHint("用于密码丢失时候找回密码");

		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// lp2.addRule(RelativeLayout.RIGHT_OF,1);
		//
		// lp2.addRule(RelativeLayout.ALIGN_BOTTOM,1);
		lp2.gravity = Gravity.CENTER;
		lp2.weight = 2;
		pswStr.setLayoutParams(lp2);
		l.addView(pswStr);
        ll.addView(l);
		
        
        tv.setVisibility(View.GONE);
		pswStr.setVisibility(View.GONE);
		
		
		
	}

	// 生日
	public void initBirthday() {

		LinearLayout l = new LinearLayout(this);

		TextView tv = new TextView(this);
		tv.setId(1);
		tv.setText(getResources().getString(R.string.birthday_div));

		tv.setTextSize(18);
		tv.setTextColor(Color.rgb(35, 35, 35));
		tv.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		// lp1.addRule(RelativeLayout.CENTER_VERTICAL);
		tv.setLayoutParams(lp1);
		lp1.weight = 1;
		l.addView(tv);

		birth = new EditText(this);

		// birth.setText(Common.userId);

		// birth.setEnabled(false);
		birth.setId(2);// 猜猜看为啥？
		// edittexts.add(et);
		birth.setSingleLine(true);
		birth.setTextSize(18);

		birth.setPadding(360, 0, 0, 0);
		birth.setBackgroundResource(R.drawable.edittext_login);
		// birth.setInputType(InputType.TYPE_CLASS_NUMBER);
		// birth.setHighlightColor(Color.rgb(169, 169, 169));
		// birth.setHighlightColor(Color.BLUE);

		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		lp2.weight = 2;
		birth.setLayoutParams(lp2);
		l.addView(birth);

		ll.addView(l);

		final Calendar c = Calendar.getInstance();
		birth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toast.makeText(MyInformation.this, "birth",
				// Toast.LENGTH_SHORT).show();
				DatePickerDialog dialog = new DatePickerDialog(
						MyInformation.this,
						new DatePickerDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePicker view, int year,
									int monthOfYear, int dayOfMonth) {
								c.set(year, monthOfYear, dayOfMonth);
								birth.setText(DateFormat.format("yyy-MM-dd", c));
							}
						}, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
								.get(Calendar.DAY_OF_MONTH));
				dialog.show();
				// s_birthday= ""+c.get(Calendar.YEAR)+ "/" +
				// (c.get(Calendar.MONTH)+1)+ "/" +
				// c.get(Calendar.DAY_OF_MONTH);
			}
		});

	}

	// 生成textview +editview
	public void initTextView(String title) {

		for (int i = 0; i < 1; i++) {

			LinearLayout l = new LinearLayout(this);

			TextView tv = new TextView(this);
			tv.setId(i + 100);// 猜猜看为啥？
			tv.setText(title);
			tv.setTextSize(18);
			tv.setTextColor(Color.rgb(35, 35, 35));
			tv.setPadding(0, 0, 0, 0);
			LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			// lp1.addRule(RelativeLayout.CENTER_VERTICAL);

			lp1.weight = 1;
			tv.setLayoutParams(lp1);

			l.addView(tv);

			EditText et = new EditText(this);
			et.setId(i + 110);// 猜猜看为啥？
			// edittexts.add(et);
			views.add(et);
			et.setTextSize(18);
			et.setPadding(360, 0, 0, 0);
			et.setBackgroundResource(R.drawable.edittext_login);

			LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			// lp2.addRule(RelativeLayout.RIGHT_OF, i + 100);
			//
			// lp2.addRule(RelativeLayout.ALIGN_BOTTOM, i + 100);
			lp2.weight = 2;
			et.setLayoutParams(lp2);
			l.addView(et);

			ll.addView(l);

		}

	}

	// 生成textview+spinner
	public void initSpinner(String title, String[] items) {// 下拉块

		for (int i = 0; i < 1; i++) {

			LinearLayout l = new LinearLayout(this);

			TextView tv = new TextView(this);
			tv.setId(i + 1000);// 猜猜看 why+1000
			tv.setText(title);
			tv.setTextSize(18);
			tv.setTextColor(Color.rgb(35, 35, 35));
			tv.setPadding(0, 0, 0, 0);
			LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(0,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			// lp1.addRule(RelativeLayout.CENTER_VERTICAL);
			lp1.weight = 1;
			tv.setLayoutParams(lp1);

			l.addView(tv);

			// Log.i("spin", "2");
			Spinner s = new Spinner(this);

			// spinners.add(s);
			views.add(s);
			s.setId(i + 1100);
			// ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
			// s.getId(), education_arr) ;
			// adapter.setDropDownViewResource(s.getId());
			// s.setAdapter(adapter);
			// 把数组导入到ArrayList中
			// ArrayAdapter array_adapter = new ArrayAdapter<String>(this,
			// android.R.layout.simple_spinner_item, items);
			my_SpinnerAdapter array_adapter = new my_SpinnerAdapter(this,
					android.R.layout.simple_spinner_item, items);
			array_adapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			s.setAdapter(array_adapter);

			s.setPadding(360, 0, 0, 0);
			s.setBackgroundResource(R.drawable.edittext_login);
			// Log.i("spin", "3");
			LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			// lp2.addRule(RelativeLayout.RIGHT_OF, i + 1000);
			//
			// lp2.addRule(RelativeLayout.ALIGN_BOTTOM, i + 1000);
			lp2.weight = 2;
			s.setLayoutParams(lp2);

			// 监听
			s.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO 自动生成的方法存根

					// dispToast("选择的是" +(String) s.getSelectedItem()+arg2);

					arg0.setVisibility(View.VISIBLE);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO 自动生成的方法存根

				}
			});

			l.addView(s);
			// Log.i("spin", "4");

			ll.addView(l);
			// Log.i("spin", "5");
		}

	}

	// 测试显示
	public void dispToast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	// 更新按钮
	public void update(View v) {
		if(Common.url != null && !Common.url.equals("")){
			
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		String update_url = Common.url + "userChgInfoEncrypt.aspx";
		
		// Toast.makeText(this, "更新", Toast.LENGTH_SHORT).show();
		StringBuffer sb = new StringBuffer();

		String s_name = name.getText().toString();
		String s_pswStr = pswStr.getText().toString();
		String s_pic;
		if (isSelectIcon) {
			s_pic = testUpload(fileName);
		} else{
			s_pic = Common.getHeadPhoto(getApplicationContext());
		}
		if (s_name.equals(""))// 判断不能为空
		{
			
			Toast.makeText(this,getResources().getString(R.string.tips_infonotwhole), Toast.LENGTH_SHORT).show();
		}

		else {

			// sb.append(s_pic);
			// sb.append("!");

			for (int i = 0; i < views.size(); i++) {

				if (order2.contains(i)) {
					sb.append((String) ((AdapterView<SpinnerAdapter>) views
							.get(i)).getSelectedItem());

					sb.append("!");
				}

				else if (i == 2) {
					s_birthday = birth.getText().toString();
				//Log.i("bitrh",s_birthday);
					if(!Common.isLegalBirth(s_birthday))
					{
						Toast.makeText(this,getResources().getString(R.string.tips_birthillegal), Toast.LENGTH_SHORT).show();
						return ;
					}
					
					sb.append(s_birthday);
					sb.append("!");
					commit.add(s_birthday);
					sb.append(s_pic);
					sb.append("!");
				} else {

					if (i == 1)// 备用密码用MD5
					{

						commit.add(MD5Util.string2MD5(((EditText) views.get(i))
								.getText().toString()));
						sb.append(MD5Util.string2MD5(((EditText) views.get(i))
								.getText().toString()));
						sb.append("!");
					} else {
						commit.add(((EditText) views.get(i)).getText()
								.toString());
						sb.append(((EditText) views.get(i)).getText()
								.toString());
						sb.append("!");
					}
				}
			}

			//Log.i("phonelog", "MyInformation---\n"+sb.toString());
			String[] ssbb = sb.toString().split("!");
			// Common.pic=ssbb[2];
			//Log.i("ssbb-2", ssbb[5]);
			// 昵称 密保 生日 图片
			save_nickname = ssbb[0];
			save_newpic = ssbb[3];
			// Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();

			boolean canCommit = true;

			for (String s : commit) {
				if (s == null)
					continue;
				if (s.contains("!") || s.contains("?") || s.contains("#"))

				{
					canCommit = false;
					commit.clear();
					break;
				}
			}

			if (canCommit) {
				if(version !=null && !version.equals("")){
					Common.showDialog(proDialog,getResources().getString(R.string.tip), getResources().getString(R.string.tips_changing));
					updateInformation ui = new updateInformation(handler,
							update_url, sb.toString(),Common.getDeviceId(getApplicationContext()),version,Common.getUserId(getApplicationContext()));
					ui.start();
				}else{
					ToastUtil.show(MyInformation.this, getResources().getString(R.string.tips_errorversion));
				}
			}

			else {

				Toast.makeText(this, getResources().getString(R.string.tips_inputillegal),
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	// 接受更改信息
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0://
				Common.dismissDialog(proDialog);
				// Toast.makeText(ManagerLogin.this,
				// "登录成功"+msg.obj,Toast.LENGTH_SHORT).show();
				result = (msg.obj.toString().trim());
				//Common.pic = save_newpic;
				Common.writeHeadphoto(getApplicationContext(), save_newpic);
				Common.NickName = save_nickname;
				Toast.makeText(MyInformation.this,getResources().getString(R.string.tips_changesuccess), Toast.LENGTH_SHORT)
						.show();
				finish();
				break;

			case 1:// 注册不成功但连接了
				Common.dismissDialog(proDialog);
				// Toast.makeText(ManagerLogin.this,
				// "登录成功"+msg.obj,Toast.LENGTH_SHORT).show();
				result = (msg.obj.toString().trim());

				Toast.makeText(MyInformation.this,getResources().getString(R.string.tips_postfail),
						Toast.LENGTH_SHORT).show();
				break;
			case 10:// 连接失败
				Common.dismissDialog(proDialog);
				// Toast.makeText(ManagerLogin.this,
				// "登录成功"+msg.obj,Toast.LENGTH_SHORT).show();
				result = (msg.obj.toString().trim());

				Toast.makeText(MyInformation.this, getResources().getString(R.string.tips_netdisconnect),
						Toast.LENGTH_SHORT).show();
				break;

			}
		}

	};

	class my_SpinnerAdapter extends ArrayAdapter<String> {
		Context context;
		String[] objects;

		public my_SpinnerAdapter(Context context, int textViewResourceId,
				String[] items) {
			super(context, textViewResourceId, items);
			this.context = context;
			this.objects = items;
		}

		@Override
		public View getDropDownView(int position, View convertView,
				ViewGroup parent) {
			// 这个函数修改的是spinner点击之后出来的选择的部分的字体大小和方式
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(
						android.R.layout.simple_spinner_dropdown_item, parent,
						false);
			}

			// 这里使用的text1 直接复制过来就行 不用重新起名 否则可能找不到这个文本框 是系统默认的
			TextView tv = (TextView) convertView
					.findViewById(android.R.id.text1);
			tv.setText(objects[position]);
			tv.setTextSize(18);
			return convertView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// 这个函数修改的选择完spinner中的东西后 显示在屏幕上的字体的大小
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(
						android.R.layout.simple_spinner_item, parent, false);
			}

			// 这里使用的text1 直接复制过来就行 不用重新起名 否则可能找不到这个文本框 是系统默认的
			TextView tv = (TextView) convertView
					.findViewById(android.R.id.text1);
			tv.setText(objects[position]);
			tv.setTextSize(18);
			return convertView;
		}

	}
}
