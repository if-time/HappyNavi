package com.trackersurvey.happynavi;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
import com.trackersurvey.helper.MD5Util;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.httpconnection.PostRegisterData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
public class Register extends Activity {

	private boolean isSelectIcon = false;
	
	LinearLayout ll;
	EditText name;
	EditText pswStr;//密保提示
	EditText usertel;
	EditText password;
	EditText password2;
	DatePicker birthday;
	EditText birth;
	CheckBox cb;
	ArrayList<View> views = new ArrayList<View>();
	int select;//判断哪种获得照片方式
	ArrayList <String> commit=new ArrayList<String>();
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

	ArrayList<Integer> order = new ArrayList<Integer>();
	//String register_url = Common.url+"login.aspx";
	String result;
	String s_birthday;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.register);
		AppManager.getAppManager().addActivity(this);
		//Common.pic="";
		ll = (LinearLayout) findViewById(R.id.layout);

		
		birthday = (DatePicker) findViewById(R.id.dpPicker);

		
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		// 接收到的登陆界面显示信息
		String information = bundle.getString("information");
		if(information == null || information.equals("")){
			ToastUtil.show(this, getResources().getString(R.string.tips_errorversion));
			return;
		}
		initName();
		initId();
		initPs1();
		initPs2();
//		initPswStr();
		initBirthday();
		String[] title = information.split("[?]");

		for (int i = 0; i < title.length; i++) {

			if (title[i].startsWith("*")) {

				String str = title[i].substring(1, title[i].length());

				initTextView(str);

			} else if (title[i].startsWith("#")) {
				order.add(i);
				String[] items = title[i].split("!");
				String[] show = new String[items.length - 1];
				for (int l = 1; l < items.length; l++)
					show[l - 1] = items[l];

				String str = items[0].substring(1, items[0].length());

				initSpinner(str, show);

			}

		}
		initImageView();
		initProtocol();
	}
	
	// 图片按钮拍照
	public void initImageView() {

			mImageView = (ImageView) findViewById(R.id.iv_photo);

			Bitmap bmp;
			// final Bitmap img=BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.headicon);
/**
			if (Common.pic == null || Common.pic.equals("")
					|| Common.pic.equals("null")) {
				
			} else {

				byte[] decode1 = Base64.decode(Common.pic, Base64.DEFAULT);
				bmp = BitmapFactory.decodeByteArray(decode1, 0, decode1.length);

			}
*/		
			bmp = BitmapFactory.decodeResource(this.getResources(),R.drawable.icon_register);
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
							Register.this);

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

	
	
	//初始化name  id  psw  psww   oswStr
	public void initName()
	{
		
		LinearLayout l=new LinearLayout(this);
		l.setOrientation(LinearLayout.HORIZONTAL);
			TextView tv = new TextView(this);
			tv.setId(1);
			
			tv.setText(getResources().getString(R.string.nickname_star));
			tv.setTextSize(18);
			tv.setTextColor(Color.rgb(35, 35, 35));    
			tv.setPadding(0, 0, 0, 0);
			LinearLayout.LayoutParams  lp1=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			//lp1.addRule(RelativeLayout.CENTER_VERTICAL);
		   lp1.gravity=Gravity.CENTER;
			tv.setLayoutParams(lp1);
			 lp1.weight=1;
			l.addView(tv);

			 name = new EditText(this);
			
			name.setId(2);// 
			// edittexts.add(et);
			//name.setSingleLine(true);
			name.setTextSize(18);
		//	name.setPadding(360, 0, 0, 0);
			name.setBackgroundResource(R.drawable.edittext_login);
           //name.setHint("例如：足迹");
           name.setHighlightColor(Color.rgb(169, 169, 169));
           name.setHighlightColor(Color.BLUE);
           name.setHint(getResources().getString(R.string.nickname_hint));
          
           LinearLayout.LayoutParams  lp2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
//			lp2.addRule(RelativeLayout.RIGHT_OF,1);
//
//			lp2.addRule(RelativeLayout.ALIGN_BOTTOM,1);
           lp2.gravity=Gravity.CENTER;
           lp2.weight=2;
           name.setLayoutParams(lp2);
			l.addView(name);

			ll.addView(l);

		
		
	}
	
	
	public void initId()
	{
		
		LinearLayout l=new LinearLayout(this);
		l.setOrientation(LinearLayout.HORIZONTAL);
		TextView tv = new TextView(this);
		tv.setId(1);
		tv.setText(getResources().getString(R.string.id_star));
		
		tv.setTextSize(18);
		tv.setTextColor(Color.rgb(35, 35, 35));    
		tv.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams  lp1=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		//lp1.addRule(RelativeLayout.CENTER_VERTICAL);
		tv.setLayoutParams(lp1);
		  lp1.gravity=Gravity.CENTER;
		lp1.weight=1;
		l.addView(tv);

		usertel = new EditText(this);
		
		usertel.setId(2);// 猜猜看为啥？
		// edittexts.add(et);
		//usertel.setSingleLine(true);
		usertel.setTextSize(18);
		//usertel.setPadding(360, 0, 0, 0);
		usertel.setBackgroundResource(R.drawable.edittext_login);
        usertel.setInputType(InputType.TYPE_CLASS_NUMBER);
        usertel.setHighlightColor(Color.rgb(169, 169, 169));
        usertel.setHighlightColor(Color.BLUE);
        usertel.setHint(getResources().getString(R.string.telenum));
        usertel.setSingleLine(false);
      
        LinearLayout.LayoutParams  lp2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);

//		lp2.addRule(RelativeLayout.RIGHT_OF,1);
//
//		lp2.addRule(RelativeLayout.ALIGN_BOTTOM,1);
        lp2.gravity=Gravity.CENTER;
		lp2.weight=2;
		usertel.setLayoutParams(lp2);
		l.addView(usertel);

		ll.addView(l);
	}
	
	public void initPs1()
	{
		
	
		LinearLayout l=new LinearLayout(this);
		l.setOrientation(LinearLayout.HORIZONTAL);
	
		TextView tv = new TextView(this);
		tv.setId(1);
		tv.setText(getResources().getString(R.string.pwd_star));
	//	tv.setBackgroundColor(Color.GREEN);
		tv.setTextSize(18);
		tv.setTextColor(Color.rgb(35, 35, 35));    
	//	tv.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams  lp1=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		//lp1.addRule(RelativeLayout.CENTER_VERTICAL);
	
		  lp1.gravity=Gravity.CENTER;
		tv.setLayoutParams(lp1);
		 lp1.weight=1;
		l.addView(tv);

		password = new EditText(this);
		
		password.setId(2);// 猜猜看为啥？
		// edittexts.add(et);
		//password.setSingleLine(true);
		password.setTextSize(18);
		password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		//password.setPadding(360, 0, 0, 0);
		password.setBackgroundResource(R.drawable.edittext_login);
    //  password.setBackgroundColor(Color.BLUE);
       password.setHighlightColor(Color.rgb(169, 169, 169));
       password.setHighlightColor(Color.BLUE);
       password.setHint(getResources().getString(R.string.pwd_hint));
       password.setSingleLine(false);
      
       LinearLayout.LayoutParams  lp2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);

//		lp2.addRule(RelativeLayout.RIGHT_OF,1);
//
//		lp2.addRule(RelativeLayout.ALIGN_BOTTOM,1);
		password.setLayoutParams(lp2);
		  lp2.gravity=Gravity.CENTER;
		 lp2.weight=2;
		l.addView(password);

		ll.addView(l);
	}
	
	
	public void initPs2()
	{
		
		LinearLayout l=new LinearLayout(this);
		l.setOrientation(LinearLayout.HORIZONTAL);
	
		TextView tv = new TextView(this);
		tv.setId(1);
		tv.setText(getResources().getString(R.string.pwd_confirm));
		//tv.setBackgroundColor(Color.BLUE);
		tv.setTextSize(18);
		tv.setTextColor(Color.rgb(35, 35, 35));    
		//tv.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams  lp1=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		//lp1.addRule(RelativeLayout.CENTER_VERTICAL);
	
		  lp1.gravity=Gravity.CENTER;
		lp1.weight=1;
		tv.setLayoutParams(lp1);

		l.addView(tv);

		password2 = new EditText(this);
		
		password2.setId(2);// 猜猜看为啥？
		// edittexts.add(et);
		//password2.setSingleLine(true);
		password2.setTextSize(18);
		password2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
	//	password2.setPadding(360, 0, 0, 0);
		password2.setBackgroundResource(R.drawable.edittext_login);
      
	//	password2.setBackgroundColor(Color.GREEN);
        password2.setHint(getResources().getString(R.string.pwd_same));
        password2.setSingleLine(false);
      
   	    LinearLayout.LayoutParams  lp2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);


		password2.setLayoutParams(lp2);
		lp2.gravity=Gravity.CENTER;
		lp2.weight=2;
		l.addView(password2);

		ll.addView(l);
	}
	
	
	
	//密码提示信息
	public void initPswStr()
	{
		
		LinearLayout l=new LinearLayout(this);
		l.setOrientation(LinearLayout.HORIZONTAL);
			TextView tv = new TextView(this);
			tv.setId(1);
			
			tv.setText(getResources().getString(R.string.secondpwd_div));
			tv.setTextSize(18);
			tv.setTextColor(Color.rgb(35, 35, 35));    
			tv.setPadding(0, 0, 0, 0);
			LinearLayout.LayoutParams  lp1=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			//lp1.addRule(RelativeLayout.CENTER_VERTICAL);
		    lp1.gravity=Gravity.CENTER;
			tv.setLayoutParams(lp1);
			lp1.weight=1;
			l.addView(tv);

			pswStr = new EditText(this);
			
			pswStr.setId(2);// 猜猜看为啥？
			// edittexts.add(et);
			//pswStr.setSingleLine(true);
			pswStr.setTextSize(18);
		//	pswStr.setPadding(360, 0, 0, 0);
			pswStr.setBackgroundResource(R.drawable.edittext_login);
           //pswStr.setHint("例如：足迹");
           pswStr.setHighlightColor(Color.rgb(169, 169, 169));
           pswStr.setHighlightColor(Color.BLUE);
           pswStr.setHint(getResources().getString(R.string.tofindpwd));
          
           LinearLayout.LayoutParams  lp2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
//			lp2.addRule(RelativeLayout.RIGHT_OF,1);
//
//			lp2.addRule(RelativeLayout.ALIGN_BOTTOM,1);
           lp2.gravity=Gravity.CENTER;
           lp2.weight=2;
           pswStr.setLayoutParams(lp2);
			l.addView(pswStr);

			ll.addView(l);

		
		
	}
	
	
	
	
	//生日
			public void initBirthday()
			{
				//Log.i("???", "???");
				LinearLayout l=new LinearLayout(this);
				l.setOrientation(LinearLayout.HORIZONTAL);
				TextView tv = new TextView(this);
				tv.setId(1);
				tv.setText(getResources().getString(R.string.birthday_star));
			//	tv.setBackgroundColor(Color.GREEN);
				tv.setTextSize(18);
				tv.setTextColor(Color.rgb(35, 35, 35));    
			//	tv.setPadding(0, 0, 0, 0);
				
				LinearLayout.LayoutParams  lp1=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
				//lp1.addRule(RelativeLayout.CENTER_VERTICAL);
				// lp1.gravity = Gravity.CENTER;
				lp1.gravity=Gravity.CENTER;
				tv.setLayoutParams(lp1);
				lp1.weight=1;
				l.addView(tv);
				
				birth = new EditText(this);
				
			//	birth.setText(Common.userId);
				
				//birth.setEnabled(false);
				birth.setId(2);// 
				// edittexts.add(et);
				birth.setSingleLine(true);
				birth.setTextSize(18);
			//	birth.setTextColor(Color.BLACK);
			//	birth.setPadding(360, 0, 0, 0);
				birth.setBackgroundResource(R.drawable.edittext_login);
				//birth.setBackgroundColor(Color.rgb(255, 35, 35));
				birth.setHint(getResources().getString(R.string.singleclick));
				// birth.setInputType(InputType.TYPE_CLASS_NUMBER);
//		       birth.setHighlightColor(Color.rgb(169, 169, 169));
//		       birth.setHighlightColor(Color.BLUE);
		 //  birth.setBackgroundColor(Color.BLUE);
		       //Log.i("???", "???");
		        LinearLayout.LayoutParams  lp2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		  	 //lp2.gravity = Gravity.CENTER;
		        lp2.gravity=Gravity.CENTER;
			    lp2.weight=2;
				birth.setLayoutParams(lp2);
				l.addView(birth);
				//Log.i("???", "???");
				ll.addView(l);
				//Log.i("???", "|||");
				
				
				final Calendar c = Calendar.getInstance();
		        birth.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						if(event.getAction() == MotionEvent.ACTION_UP){
							DatePickerDialog dialog = new DatePickerDialog(Register.this, new DatePickerDialog.OnDateSetListener() {
			                    @Override
			                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			                        c.set(year, monthOfYear, dayOfMonth);
			                        birth.setText(DateFormat.format("yyy-MM-dd", c));
			                    }
			                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
			                dialog.show();
						}
						return true;
					}
				});
		        /*
		        ClickListener(new View.OnClickListener() {
		            @Override
		            public void onClick(View v) {
		                DatePickerDialog dialog = new DatePickerDialog(Register.this, new DatePickerDialog.OnDateSetListener() {
		                    @Override
		                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		                        c.set(year, monthOfYear, dayOfMonth);
		                        birth.setText(DateFormat.format("yyy-MM-dd", c));
		                    }
		                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		                dialog.show();
		              // s_birthday= ""+c.get(Calendar.YEAR)+ "/" + (c.get(Calendar.MONTH)+1)+ "/" + c.get(Calendar.DAY_OF_MONTH);
		            }
		        });
				*/
				
				
				
				
				
				
				
			}
	
	
			//协议
			public void initProtocol()
			{
				
				
				LinearLayout l=new LinearLayout(this);
				l.setOrientation(LinearLayout.HORIZONTAL);
					cb = new CheckBox(this);
					cb.setId(1);
					
				
					LinearLayout.LayoutParams  lp1=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
					//lp1.addRule(RelativeLayout.CENTER_VERTICAL);
				   lp1.gravity=Gravity.CENTER;
					cb.setLayoutParams(lp1);
					 lp1.weight=1;
					l.addView(cb);

				TextView protocol = new TextView(this);
					
					protocol.setId(2);// 猜猜看为啥？
					// edittexts.add(et);
					//protocol.setSingleLine(true);
					protocol.setTextSize(18);
				//	protocol.setPadding(360, 0, 0, 0);
				//	protocol.setBackgroundResource(R.drawable.edittext_login);
		           //protocol.setHint("例如：足迹");
		         //  protocol.setHighlightColor(Color.rgb(169, 169, 169));
		        //   protocol.setHighlightColor(Color.BLUE);
		           protocol.setText(getResources().getString(R.string.iagreeprotocol));
		            protocol.setTextColor(Color.BLUE);
		           LinearLayout.LayoutParams  lp2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
//					lp2.addRule(RelativeLayout.RIGHT_OF,1);
		//
//					lp2.addRule(RelativeLayout.ALIGN_BOTTOM,1);
		           lp2.gravity=Gravity.CENTER;
		           lp2.weight=8;
		           protocol.setLayoutParams(lp2);
					l.addView(protocol);

					ll.addView(l);
				
					
					
					
					protocol.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
						AlertDialog alert =new AlertDialog.Builder(Register.this).create();
							alert.setTitle(getResources().getString(R.string.tips_dlgtle_protocol));
							alert.setMessage(getResources().getString(R.string.tips_dlgmsg_protocol1)
									+"\n"+getResources().getString(R.string.tips_dlgmsg_protocol2)
									+"\n"+getResources().getString(R.string.tips_dlgmsg_protocol3)
									+"\n"+getResources().getString(R.string.tips_dlgmsg_protocol4)
									+"\n"+getResources().getString(R.string.tips_dlgmsg_protocol5));
							alert.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.close),  new DialogInterface.OnClickListener() {
						
					
						public void onClick(DialogInterface dialog, int which) {
													
						}
					});
							
							
							
							alert.show();
							
						}
					});
					
					
					
					
					
					
					
				
			}
			
			
 	
	// 生成textview +editview
	public void initTextView(String title) {

		for (int i = 0; i < 1; i++) {

			LinearLayout l=new LinearLayout(this);
			l.setOrientation(LinearLayout.HORIZONTAL);
			TextView tv = new TextView(this);
			tv.setId(i + 100);//
			tv.setText(title);
			tv.setTextSize(18);
			tv.setTextColor(Color.rgb(35, 35, 35));    
			tv.setPadding(0, 0, 0, 0);
			LinearLayout.LayoutParams  lp1=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			//lp1.addRule(RelativeLayout.CENTER_VERTICAL);
			  lp1.gravity=Gravity.CENTER;
			 lp1.weight=1;
			tv.setLayoutParams(lp1);

			l.addView(tv);

			EditText et = new EditText(this);
			et.setId(i + 110);//
			// edittexts.add(et);
			views.add(et);
			et.setTextSize(18);
			//et.setPadding(360, 0, 0, 0);
			et.setBackgroundResource(R.drawable.edittext_login);

			LinearLayout.LayoutParams  lp2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);

//			lp2.addRule(RelativeLayout.RIGHT_OF, i + 100);
//
//			lp2.addRule(RelativeLayout.ALIGN_BOTTOM, i + 100);
			  lp2.gravity=Gravity.CENTER;
			lp2.weight=2;
			et.setLayoutParams(lp2);
			l.addView(et);

			ll.addView(l);

		}

	}

	// 生成textview+spinner
	public void initSpinner(String title, String[] items) {// 下拉块

		for (int i = 0; i < 1; i++) {

			LinearLayout l=new LinearLayout(this);
			l.setOrientation(LinearLayout.HORIZONTAL);
		
			TextView tv = new TextView(this);
			tv.setId(i + 1000);// 
			tv.setText(title);
			tv.setTextSize(18);
			tv.setTextColor(Color.rgb(35, 35, 35));    
			tv.setPadding(0, 0, 0, 0);
			LinearLayout.LayoutParams  lp1=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			//lp1.addRule(RelativeLayout.CENTER_VERTICAL);
			
			
			  lp1.gravity=Gravity.CENTER;
			lp1.weight=1;
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
//			ArrayAdapter array_adapter = new ArrayAdapter<String>(this,
//					android.R.layout.simple_spinner_item, items);
			my_SpinnerAdapter array_adapter =new my_SpinnerAdapter(this, android.R.layout.simple_spinner_item, items);
			array_adapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			s.setAdapter(array_adapter);

			//s.setPadding(360, 0, 0, 0);
			s.setBackgroundResource(R.drawable.edittext_login);
			// Log.i("spin", "3");
			LinearLayout.LayoutParams  lp2=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);

//			lp2.addRule(RelativeLayout.RIGHT_OF, i + 1000);
//
//			lp2.addRule(RelativeLayout.ALIGN_BOTTOM, i + 1000);
			  lp2.gravity=Gravity.CENTER;
			lp2.weight=2;
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

   //提交信息
	public void submit(View v) {
		if(Common.url != null && !Common.url.equals("")){
		}else{
			Common.url = getResources().getString(R.string.url);
		}
		String register_url = Common.url+"userRegister.aspx";
		
		String version = null;
		StringBuffer sb = new StringBuffer();

		String s_name = name.getText().toString();
		String s_usertel = usertel.getText().toString();
		String s_password = password.getText().toString();
//		String s_pswStr=pswStr.getText().toString();//不再提交备用密码
		String s_pic = null;
		try{
			if (isSelectIcon) {
				s_pic = testUpload(fileName);
			} else{
				s_pic = "";
			}
		}
		catch( Exception e)
		{
		}
		s_birthday =birth.getText().toString();
		//Log.i("birth",s_birthday);
		if(!Common.isLegalBirth(s_birthday))
		{
			Toast.makeText(this,getResources().getString(R.string.tips_birthillegal), Toast.LENGTH_SHORT).show();
			return ;
		}
		String s_password2 = password2.getText().toString();
//		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8}$");
		Pattern p = Pattern.compile("^[\\d]{6,20}$");//数字，位数在6到20之间
		Matcher m = p.matcher(s_usertel);
		m.matches();
		//if (s_name.equals("") || s_usertel.equals("") || s_password.equals("")|| s_pswStr.equals(""))// 判断账号密码不能为空
		if (s_name.equals("") || s_usertel.equals("") || s_password.equals(""))// 判断账号密码不能为空
		{
			String show = getResources().getString(R.string.tips_infonotwhole);
			Toast.makeText(this, show, Toast.LENGTH_SHORT).show();
		}
		else if (m.matches()) {	
			if(s_password.equals(s_password2))
			{
			sb.append(s_usertel);
		    commit.add(s_usertel);
			sb.append("!");
			
			//判断是否进行MD5
			//若版本号小于"1.2.5"则不加密
			//boolean jiami=( Common.version.compareTo("1.2.5")>=0);
			
			if(Common.version == null ||Common.version.equals("")){
				version = Common.getAppVersionName(getApplicationContext());
			}else{
				version = Common.version;
			}
			boolean jiami = true;
			if(version != null && !version.equals("")){
				jiami=( version.compareTo("1.2.5")>=0);
			}else{
				ToastUtil.show(this, getResources().getString(R.string.tips_errorversion));
			}
			if(jiami){
				sb.append(MD5Util.string2MD5(s_password));
				commit.add(MD5Util.string2MD5(s_password));
			}else{
				sb.append((s_password));
				commit.add((s_password));
			}
			//sb.append(s_password);
			//commit.add(s_password);
			sb.append("!");
			sb.append(s_name);
			commit.add(s_name);
			sb.append("!");
			//判断是否进行MD5
			
//			if(jiami){
//				sb.append(MD5Util.string2MD5(s_pswStr));
//				commit.add(MD5Util.string2MD5(s_pswStr));
//			}else{
//				sb.append((s_pswStr));
//				commit.add((s_pswStr));
//			}
			
			//sb.append(s_pswStr);
			//commit.add(s_pswStr);
			sb.append("!");
			
			sb.append(s_birthday);
			commit.add(s_birthday);
			sb.append("!");
			
			sb.append(s_pic);
			sb.append("!");
			for (int i = 0; i < views.size(); i++) {
				if (order.contains(i)) {
					sb.append((String) ((AdapterView<SpinnerAdapter>) views
							.get(i)).getSelectedItem());
					sb.append("!");
				}else {
					sb.append(((EditText) views.get(i)).getText().toString());
					commit.add(((EditText) views.get(i)).getText().toString());
					sb.append("!");
				}
			}
		//	Toast.makeText(this, sb.toString(), Toast.LENGTH_SHORT).show();
			// 发送
			if(cb.isChecked()){
				
				boolean canCommit=true;
				
				for(String s :commit){
					if(s==null)
					continue;
					if(s.contains("!")||s.contains("?")||s.contains("#")){
						canCommit=false;
						commit.clear();
						break;
					}
				}
			if(canCommit){
				PostRegisterData prd = new PostRegisterData(handler, register_url,
						sb.toString(),Common.getDeviceId(getApplicationContext()),version);
				prd.start();
				//Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, getResources().getString(R.string.tips_inputillegal), Toast.LENGTH_SHORT).show();
			}
		}else{	
			Toast.makeText(this,getResources().getString(R.string.tips_agreeprotocol), Toast.LENGTH_SHORT).show();
		}
	}
			else{
				Toast.makeText(this,getResources().getString(R.string.tips_pwddiffer), Toast.LENGTH_SHORT).show();
			}
			
		}else {
			Toast.makeText(this,getResources().getString(R.string.id_illegal), Toast.LENGTH_SHORT).show();
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:// 注册成功

				// Toast.makeText(ManagerLogin.this,
				// "登录成功"+msg.obj,Toast.LENGTH_SHORT).show();
				result = (msg.obj.toString().trim());

				Toast.makeText(Register.this,getResources().getString(R.string.register_succ),
						Toast.LENGTH_SHORT).show();
				finish();
				break;

			case 1:// 注册不成功但连接了

				// Toast.makeText(ManagerLogin.this,
				// "登录成功"+msg.obj,Toast.LENGTH_SHORT).show();
				result = (msg.obj.toString().trim());

				Toast.makeText(Register.this,getResources().getString(R.string.register_fail_exist) ,
						Toast.LENGTH_SHORT).show();
				break;
			case 10:// 连接失败

				// Toast.makeText(ManagerLogin.this,
				// "登录成功"+msg.obj,Toast.LENGTH_SHORT).show();
				result = (msg.obj.toString().trim());

				Toast.makeText(Register.this,getResources().getString(R.string.tips_netdisconnect),
						Toast.LENGTH_SHORT).show();
				break;

			}
		}

	};
	
	
	
	
	
	
	class my_SpinnerAdapter extends ArrayAdapter<String> {
		  Context context;
		 String [] objects;
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
		    convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
		   }

		   //这里使用的text1 直接复制过来就行 不用重新起名 否则可能找不到这个文本框 是系统默认的
		   TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
		   tv.setText(objects[position]);
		   tv.setTextSize(18);
		   return convertView;
		  }
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		           //这个函数修改的选择完spinner中的东西后 显示在屏幕上的字体的大小
		           if (convertView == null) {  
		               LayoutInflater inflater = LayoutInflater.from(context);  
		               convertView = inflater.inflate(  
		                       android.R.layout.simple_spinner_item, parent, false);  
		           }  
		     
		      //这里使用的text1 直接复制过来就行 不用重新起名 否则可能找不到这个文本框 是系统默认的
		     TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
		     tv.setText(objects[position]);
		     tv.setTextSize(18);
		           return convertView; 
		  }
		  
		  
		 }
}
