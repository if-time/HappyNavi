package com.trackersurvey.happynavi;

import java.util.Locale;

import com.trackersurvey.helper.AppManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

//继承PreferenceActivity，并实现OnPreferenceChangeListener和OnPreferenceClickListener监听接口
public class SetParameter extends PreferenceActivity implements OnPreferenceClickListener,
OnPreferenceChangeListener{
	//定义相关变量
	String updateSwitchKey;   
	String updateFrequencyKey;
	String uploadFrequencyKey;
	String rec_loc_FrequencyKey;
	String norec_loc_FrequencyKey;
//	String languageKey;
	CheckBoxPreference updateSwitchCheckPref; 
	ListPreference updateFrequencyListPref;
//	ListPreference languageListPref;
//	SharedPreferences languageSharePref;
	Preference uploadFrequencyPref;
	Preference rec_loc_FrequencyPref;
	//Preference norec_loc_FrequencyPref;
	//顶部标题栏
	private LinearLayout back;
	private ImageView titleBackOff; // 顶部回退按钮
	private TextView titleText; // 顶部文本
	private Button titleButton; // 顶部确认按钮
	public static SetParameter instance = null;
//	public static String languageSummaryTxt = "";
	private SharedPreferences sp;
	private int l;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	final boolean isCustom=requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        super.onCreate(savedInstanceState);
        
        // 切换中英文
 		//-----------------------------------------------------//
 		Resources resources = getResources();
 		Configuration configure = resources.getConfiguration();
 		DisplayMetrics dm = resources.getDisplayMetrics();
 		if(TabHost_Main.l==0){
 			configure.locale = Locale.CHINESE;
 		}
 		if(TabHost_Main.l==1){
 			configure.locale = Locale.ENGLISH;
 		}
 		if(TabHost_Main.l==2) {
			configure.locale = new Locale("cs", "CZ");
		}
 		resources.updateConfiguration(configure, dm);
 		//-----------------------------------------------------//
        
        instance = this;
        if(isCustom){
        //自定义顶部标题栏
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        }
       //从xml文件中添加Preference项
        
        addPreferencesFromResource(R.xml.preferencesii);
        
        AppManager.getAppManager().addActivity(this);
        back = (LinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}

		});
		
		// 顶部标题
		titleText = (TextView) findViewById(R.id.header_text);
		titleText.setText(getResources().getString(R.string.item_setparameter));
		// 右部按钮不可见
		titleButton = (Button) findViewById(R.id.header_right_btn);
		titleButton.setVisibility(View.INVISIBLE);
		
        //获取各个Preference
        updateSwitchKey = getResources().getString(R.string.auto_update_switch_key);
        updateFrequencyKey = getResources().getString(R.string.auto_update_frequency_key);
        uploadFrequencyKey = getResources().getString(R.string.auto_upload_frequency_key);
        rec_loc_FrequencyKey = getResources().getString(R.string.auto_record_loc_frequency_key);
        norec_loc_FrequencyKey = getResources().getString(R.string.auto_norecord_loc_frequency_key);
//        languageKey = getResources().getString(R.string.language_key);
        updateSwitchCheckPref = (CheckBoxPreference)findPreference(updateSwitchKey);
        updateFrequencyListPref = (ListPreference)findPreference(updateFrequencyKey);
//        languageListPref = (ListPreference)findPreference(languageKey);
//        languageSharePref = (SharedPreferences) findPreference(languageKey);
        uploadFrequencyPref = (Preference)findPreference(uploadFrequencyKey);
        rec_loc_FrequencyPref = (Preference)findPreference(rec_loc_FrequencyKey);
        //norec_loc_FrequencyPref = (Preference)findPreference(norec_loc_FrequencyKey);
        //为各个Preference注册监听接口    
        updateSwitchCheckPref.setOnPreferenceClickListener(this);
        updateFrequencyListPref.setOnPreferenceClickListener(this); 
//        languageListPref.setOnPreferenceClickListener(this);
//        languageSharePref.registerOnSharedPreferenceChangeListener(this);
        uploadFrequencyPref.setOnPreferenceClickListener(this);
        rec_loc_FrequencyPref.setOnPreferenceClickListener(this);
       // norec_loc_FrequencyPref.setOnPreferenceClickListener(this);
        
        updateSwitchCheckPref.setOnPreferenceChangeListener(this);
        updateFrequencyListPref.setOnPreferenceChangeListener(this);
//        languageListPref.setOnPreferenceChangeListener(this);
        uploadFrequencyPref.setEnabled(false);//OnPreferenceChangeListener(this);
        rec_loc_FrequencyPref.setEnabled(false);//OnPreferenceChangeListener(this);
        //norec_loc_FrequencyPref.setEnabled(false);//OnPreferenceChangeListener(this);
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        updateFrequencyListPref.setSummary(updateFrequencyListPref.getEntry());
//        languageListPref.setSummary(languageListPref.getEntry());
        
        uploadFrequencyPref.setSummary(settings.getInt(uploadFrequencyKey, 30)+getResources().getString(R.string.setbyserver));
        rec_loc_FrequencyPref.setSummary(settings.getInt(rec_loc_FrequencyKey, 5)+getResources().getString(R.string.setbyserver));
        //norec_loc_FrequencyPref.setSummary(settings.getInt(norec_loc_FrequencyKey, 10)+"秒  (后台设定)");
        sp = getSharedPreferences("com.trackersurvey.happynavi_preferences", 0);
        String language = sp.getString("language", "0");
		l = Integer.parseInt(language);
    }
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		//Log.v("mysetting", preference.getKey()+" is clicked");
		//Log.v("mysetting", preference.getKey());
		//判断是哪个Preference被点击了
		if(preference.getKey().equals(updateSwitchKey))
		{
			//Log.v("mysetting", "checkbox preference is clicked");
		}
		else if(preference.getKey().equals(updateFrequencyKey))
		{
			//Log.v("mysetting", "list preference is clicked");
		}
//		else if(preference.getKey().equals(languageKey)){
//			//Log.v("mysetting", "list preference is clicked");
//		}
		else
		{
			return false;
		}
		return true;
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		//Log.v("mysetting", preference.getKey()+" is changed");
		//Log.v("mysetting", );
		//判断是哪个Preference改变了
		if(preference.getKey().equals(updateSwitchKey))
		{
			//Log.v("mysetting", "checkbox preference is changed");
		}
		else if(preference.getKey().equals(updateFrequencyKey))
		{
			int newSummary=Integer.parseInt(newValue.toString());
			String summaryTxt="";
			if(newSummary>=60){
				newSummary=newSummary/60;
				summaryTxt=newSummary+getResources().getString(R.string.hour);
			}
			else{
				summaryTxt=newSummary+getResources().getString(R.string.minute);
			}
			//Log.v("mysetting", "list preference is changed to--->"+summaryTxt);
			updateFrequencyListPref.setSummary(summaryTxt);
		}
//		else if(preference.getKey().equals(languageKey)){
//			int languageNewSummary = Integer.parseInt(newValue.toString());
//			if(l == 0){
//				languageSummaryTxt = getResources().getString(R.string.language_chinese);
//			}else if(l == 1){
//				languageSummaryTxt = getResources().getString(R.string.language_english);
//			}
//			languageListPref.setSummary(languageSummaryTxt);
//			SetParameter.instance.finish();
//			SettingsMain.instance.finish();
//			Personal_main.instance.finish();
//			Intent intent = new Intent(SetParameter.this,TabHost_Main.class);
//			startActivity(intent);
//		}
		else
		{
			//如果返回false表示不允许被改变
			//Log.v("mysetting", preference.getKey()+" false");
			return false;
		}
		//返回true表示允许改变
		//Log.v("mysetting", preference.getKey()+" true");
		return true;
	}
//	@Override
//	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//		// TODO Auto-generated method stub
//		
//	}
	
}
