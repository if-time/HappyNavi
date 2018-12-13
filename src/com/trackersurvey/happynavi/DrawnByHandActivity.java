package com.trackersurvey.happynavi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
//import com.amap.api.maps.overlay.DrivingRouteOverlay;
//import com.amap.api.maps.overlay.WalkRouteOverlay;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
//import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
//import com.autonavi.ae.gmap.gloverlay.BaseRouteOverlay;
import com.trackersurvey.entity.CityData;
import com.trackersurvey.helper.Common;
import com.trackersurvey.helper.CustomDialog;
import com.trackersurvey.helper.MyLinearLayout;
import com.trackersurvey.helper.ToastUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class DrawnByHandActivity extends Activity implements OnClickListener, OnMapClickListener, OnMarkerClickListener, 
LocationSource, AMapLocationListener, OnRouteSearchListener, OnInfoWindowClickListener, InfoWindowAdapter, 
OnGeocodeSearchListener, TextWatcher, InputtipsListener{
	
	private AMap aMap;
	private MapView mapView;
	private MyLinearLayout back;
	private TextView title;			//�����ı�
	private Button titleRightBtn; 	// ����ȷ�ϰ�ť
	private OnLocationChangedListener mListener;
	private AMapLocationClient mlocationClient;
	private AMapLocationClientOption mLocationOption;
	private RouteSearch mRouteSearch;
	private WalkRouteResult mWalkRouteResult;
	private DriveRouteResult mDriveRouteResult;
	private BusRouteResult mBusRouteResult;
	private final int ROUTE_TYPE_DRIVE = 1;
	private final int ROUTE_TYPE_BUS = 2;
	private final int ROUTE_TYPE_WALK = 3;
	
	private MarkerOptions markerOption;
	private Marker marker;					//�ֶ������ͼ�����ı��
	private Marker startMarker;				//�����
	private Marker endMarker;				//�յ���
	private List<Marker> markerList;		//������б�
	private List<Marker> startMarkerList;	//�յ����б�
	private List<Marker> endMarkerList;
	private List<LatLng> markerLatLngList;		//�滮·���ؼ���ľ�γ�ȣ�����·���滮ǰ��marker��ע
	private List<LatLng> startMarkerLatLngList;
	private List<LatLng> endMarkerLatLngList;
	private LatLonPoint latlonPoint;	//�滮·��ʱ�õ��ı�������
	private LatLonPoint startLatlonPoint;
	private LatLonPoint endLatlonPoint;
	private List<LatLonPoint> latlonPointList;
	private List<LatLonPoint> startLatlonPointList;
	private List<LatLonPoint> endLatlonPointList;
	private boolean confirmed = false;
	private int count;
	private int i;
	//private ImageView deletePoint;
	//private ImageView search;
	private RelativeLayout deletePointLayout;
	private RelativeLayout searchLayout;
	private ImageButton routeMode;
	
	private GeocodeSearch geocodeSearch;
	private String addressName;
	
	private boolean walkMode = true;
	private boolean busMode = false;
	private boolean driveMode = false;
	private boolean searchMode = false;
	
	private String addressLatlonStr;
	private double searchLat;
	private double searchLng;
	private LatLng searchLatLng;
	
	//private ImageView mDrive;
	//private ImageView mBus;
	//private ImageView mWalk;
//	private MyWalkRouteOverlay walkRouteOverlay;			//����·���滮�켣����
//	private MyDrivingRouteOverlay drivingRouteOverlay;		//��ʻ·���滮�켣����
//	private List<MyWalkRouteOverlay> walkRouteOverlayList;
//	private List<MyDrivingRouteOverlay> drivingRouteOverlayList;
//	private List<MyWalkRouteOverlay> searchWalkRouteOverlayList;
//	private List<MyDrivingRouteOverlay> searchDrivingRouteOverlayList;
	private String mCurrentCityName = "����";//���ڹ���·���滮
	private RelativeLayout searchAddr;
	private boolean isShown = false;
	private Button confirmBegin;
	private Button confirmEnd;
	
	private String city = "�й�";
	private String cityCode = "0086";//�й�������(ĳЩ�ص���������)
	private AutoCompleteTextView inputBegin;
	private AutoCompleteTextView inputEnd;
	private ListView mInputlist;
	private Dialog bottomDialog;
	private LinearLayout dialogLayout;
	private RelativeLayout walkingLayout;
	private RelativeLayout drivingLayout;
	private SharedPreferences sp;
	//private int listCount;
	//private List<CityData> listProvince;
	//private List<CityData> listCity;
	//private Spinner spinnerProvince, spinnerCity;
	//private RelativeLayout routeTips;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.drawnbyhand);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
		
		mapView = (MapView) findViewById(R.id.draw_map);
		mapView.onCreate(savedInstanceState);//��ʾһ�ŵ�ͼ
		
		init();
		initView();
		
		mLocationOption.setOnceLocation(true);					//���ڶ�λ
		mlocationClient.setLocationOption(mLocationOption);
		mlocationClient.startLocation();
		
		markerList = new ArrayList<Marker>();					//�ֵ��ͼ���ɵı�ע����󼯺�
		startMarkerList = new ArrayList<Marker>();
		startMarkerLatLngList = new ArrayList<LatLng>();
		endMarkerLatLngList = new ArrayList<LatLng>();
		endMarkerList = new ArrayList<Marker>();
		markerLatLngList = new ArrayList<LatLng>();						//�ֵ��ͼ���ɵı�ע��ľ�γ�ȶ��󼯺�
		latlonPointList = new ArrayList<LatLonPoint>();			//·���滮��γ�Ȳ������󼯺�
		startLatlonPointList = new ArrayList<LatLonPoint>();
		endLatlonPointList = new ArrayList<LatLonPoint>();
//		walkRouteOverlayList = new ArrayList<MyWalkRouteOverlay>();		//����·���滮�߶��󼯺�
//		drivingRouteOverlayList = new ArrayList<MyDrivingRouteOverlay>();
//		searchWalkRouteOverlayList = new ArrayList<MyWalkRouteOverlay>();
//		searchDrivingRouteOverlayList = new ArrayList<MyDrivingRouteOverlay>();
		
		geocodeSearch = new GeocodeSearch(this);				//����������������ڸ��ݵ�ַ����������γ��
		walkMode = true;//Ĭ��Ϊ����ģʽ
		sp = getSharedPreferences("config", 0);//��ʼ��sp��ʵ��
		boolean isShowTips = sp.getBoolean("isShowTips", true);
		if(isShowTips){
			showTipsDialog();
		}
		
		//geocodeSearch.setOnGeocodeSearchListener(this);
		
//		if(points.size() > 1){
//			confirmed = true;
//			if(latlonPointList.size()>=2 && confirmed){	
//				searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
//				confirmed = false;
//			}
//		}
		
	}
	public void initView(){
		back = (MyLinearLayout) findViewById(R.id.title_back);
		back.setOnClickListener(this);
		title = (TextView) findViewById(R.id.header_text);
		title.setText(getResources().getText(R.string.pathplaning));
		titleRightBtn = (Button) findViewById(R.id.header_right_btn);
		//deletePoint = (ImageView) findViewById(R.id.delete_point);
		deletePointLayout = (RelativeLayout) findViewById(R.id.delete_point_layout);
		titleRightBtn.setVisibility(View.INVISIBLE);
		titleRightBtn.setOnClickListener(this);
		//deletePoint.setOnClickListener(this);
		deletePointLayout.setOnClickListener(this);
		
		routeMode = (ImageButton) findViewById(R.id.route_mode_select);
		routeMode.setOnClickListener(this);
		//search = (ImageView) findViewById(R.id.search_dest);
		searchLayout = (RelativeLayout) findViewById(R.id.search_dest_layout);
		//search.setOnClickListener(this);
		searchLayout.setOnClickListener(this);
		
		//mDrive = (ImageView) findViewById(R.id.route_drive);
		//mBus = (ImageView) findViewById(R.id.route_bus);
		//mWalk = (ImageView) findViewById(R.id.route_walk);
		
		searchAddr = (RelativeLayout) findViewById(R.id.search_addr);
		searchAddr.setVisibility(View.INVISIBLE);
		
		inputBegin = (AutoCompleteTextView) findViewById(R.id.input_begin);
		inputEnd = (AutoCompleteTextView) findViewById(R.id.input_end);
		
		confirmBegin = (Button) findViewById(R.id.confirm_begin);
		confirmEnd = (Button) findViewById(R.id.confirm_end);
		
		mInputlist = (ListView) findViewById(R.id.inputlist);
		
		//mDrive.setOnClickListener(this);
		//mWalk.setOnClickListener(this);
		confirmBegin.setOnClickListener(this);
		confirmEnd.setOnClickListener(this);
		//��������¼����Զ���ʾ��
		inputBegin.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				String newText = s.toString().trim();
		        InputtipsQuery inputquery = new InputtipsQuery(newText, city);
		        inputquery.setCityLimit(true);
		        Inputtips inputTips = new Inputtips(getApplicationContext(), inputquery);
		        inputTips.setInputtipsListener(new InputtipsListener() {
					
					@Override
					public void onGetInputtips(List<Tip> tipList, int rCode) {
						// TODO Auto-generated method stub
						if (rCode == 1000) {
				            final List<HashMap<String, String>> listString = new ArrayList<HashMap<String, String>>();
				            for (int i = 0; i < tipList.size(); i++) {
				            	HashMap<String, String> map = new HashMap<String, String>();
				            	map.put("name", tipList.get(i).getName());
				            	map.put("address", tipList.get(i).getDistrict());
				                listString.add(map);
				            }
				            SimpleAdapter aAdapter = new SimpleAdapter(getApplicationContext(), listString, R.layout.item_layout, 
				            		new String[] {"name","address"}, new int[] {R.id.poi_field_id, R.id.poi_value_id});

				            mInputlist.setAdapter(aAdapter);
				            aAdapter.notifyDataSetChanged();
				            
				            mInputlist.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
									// TODO Auto-generated method stub
									//ToastUtil.show(getApplicationContext(), listString.get(position).get("name"));
									inputBegin.setText(listString.get(position).get("name"));
									addressName = inputBegin.getText().toString().trim();
									
									searchMode = true;
									getBeginLatlon(addressName);//��������ַ����������γ��
									
									searchAddr.setVisibility(View.INVISIBLE);
									InputMethodManager imm1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									if(imm1.isActive()){
										//�ر�����̣�����������ͬ������������л�������ر�״̬��
										imm1.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_NOT_ALWAYS);
									}
								}
							});

				        } else {
							ToastUtil.show(getApplicationContext(), rCode);
						}
					}
				});
		        inputTips.requestInputtipsAsyn();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		inputEnd.addTextChangedListener(this);
		
	}
	/**
	 * ��ʼ��
	 */
	public void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			setUpMap();
		}
		registerListener();
		mRouteSearch = new RouteSearch(this);
		mRouteSearch.setRouteSearchListener(this);
		
	}
	/**
	 * ����һЩamap������
	 */
	public void setUpMap() {
		// �Զ���ϵͳ��λС����
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle.myLocationIcon(BitmapDescriptorFactory
				.fromResource(R.drawable.location_marker));// ����С�����ͼ��
		myLocationStyle.strokeColor(Color.BLACK);// ����Բ�εı߿���ɫ
		myLocationStyle.radiusFillColor(Color.argb(100, 255, 255, 255));// ����Բ�ε������ɫ
		// myLocationStyle.anchor(int,int)//����С�����ê��
		myLocationStyle.strokeWidth(1.0f);// ����Բ�εı߿��ϸ
		aMap.setMyLocationStyle(myLocationStyle);
		aMap.setLocationSource(this);// ���ö�λ����
		aMap.getUiSettings().setMyLocationButtonEnabled(true);// ����Ĭ�϶�λ��ť�Ƿ���ʾ
		aMap.setMyLocationEnabled(true);// ����Ϊtrue��ʾ��ʾ��λ�㲢�ɴ�����λ��false��ʾ���ض�λ�㲢���ɴ�����λ��Ĭ����false
		aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		
		aMap.getUiSettings().setCompassEnabled(true);  //��������
		aMap.getUiSettings().setScaleControlsEnabled(true);//���ñ�����
		aMap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);//�������Ű�ť���Ҳ��м�λ��
		aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
	}
	/**
	 * ע�����
	 */
	private void registerListener(){
		aMap.setOnMapClickListener(DrawnByHandActivity.this);
		aMap.setOnMarkerClickListener(DrawnByHandActivity.this);
		aMap.setOnInfoWindowClickListener(DrawnByHandActivity.this);
		aMap.setInfoWindowAdapter(DrawnByHandActivity.this);
	}

	/**
	 * ��ʼ����·���滮����(��ʼ����)
	 * @param routeType
	 * @param mode
	 */
	public void searchRouteResult(int routeType, int mode){	
		/*if(markerLatLngList.size() == 0){
			ToastUtil.show(getApplicationContext(), "��������");
		}
		if(markerLatLngList.size() == 1){
			ToastUtil.show(getApplicationContext(), "������յ�");
		}
		if(endMarkerList.size()>0 && startMarkerList.size()==0){
			ToastUtil.show(getApplicationContext(), "��������");
		}
		if(startMarkerList.size()>0 && endMarkerList.size()<0){
			ToastUtil.show(getApplicationContext(), "������յ�");
		}*/
		//����������õ��ĵ�
		if(endMarkerList.size()>0 && startMarkerList.size()>0 && searchMode){
			//���ֻ��һ�����յ�Ҳֻ��һ��
			final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(startLatlonPointList.get(0), endLatlonPointList.get(0));
			if (routeType == ROUTE_TYPE_DRIVE){ //��ʻ·���滮
				// ��һ��������ʾ·���滮�������յ㣬�ڶ���������ʾ�ݳ�ģʽ��������������ʾ;���㣬
				// ���ĸ�������ʾ�������򣬵����������ʾ���õ�·
				DriveRouteQuery query = new DriveRouteQuery(fromAndTo, mode, null,null, "");
				mRouteSearch.calculateDriveRouteAsyn(query);// �첽·���滮�ݳ�ģʽ��ѯ
			}
			if (routeType == ROUTE_TYPE_WALK) {	//����·���滮
				WalkRouteQuery query = new WalkRouteQuery(fromAndTo, mode);
				mRouteSearch.calculateWalkRouteAsyn(query);// �첽·���滮����ģʽ��ѯ
			}
		}
		if(markerLatLngList.size()>1 && !searchMode){			
			Log.i("1003", "aaa"+count);
				final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(latlonPointList.get(count-2), latlonPointList.get(count-1));
				//final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(latlonPointList.get(i), latlonPointList.get(i+1));
				if (routeType == ROUTE_TYPE_DRIVE){ //��ʻ·���滮
					// ��һ��������ʾ·���滮�������յ㣬�ڶ���������ʾ�ݳ�ģʽ��������������ʾ;���㣬
					// ���ĸ�������ʾ�������򣬵����������ʾ���õ�·
					DriveRouteQuery query = new DriveRouteQuery(fromAndTo, mode, null,null, "");
					mRouteSearch.calculateDriveRouteAsyn(query);// �첽·���滮�ݳ�ģʽ��ѯ
				}
				if (routeType == ROUTE_TYPE_WALK) {	//����·���滮
					WalkRouteQuery query = new WalkRouteQuery(fromAndTo, mode);
					mRouteSearch.calculateWalkRouteAsyn(query);// �첽·���滮����ģʽ��ѯ
				}
			i = 0;
		}
		
	}
	/**
	 * ��Ӧ��ʼ��ַ�������(�������)
	 * @param name
	 */
	public void getBeginLatlon(final String name){
		// ��һ��������ʾ��ַ���ڶ���������ʾ��ѯ���У����Ļ�������ȫƴ��citycode��adcode��
		GeocodeQuery query = new GeocodeQuery(name, cityCode);
		
		geocodeSearch.getFromLocationNameAsyn(query);// ����ͬ�������������
		geocodeSearch.setOnGeocodeSearchListener(new OnGeocodeSearchListener() {
			@Override
			public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
			}
			@Override
			public void onGeocodeSearched(GeocodeResult result, int rCode) {
				// TODO Auto-generated method stub
//				if (rCode == 1000) {
//					if (result != null && result.getGeocodeAddressList() != null
//							&& result.getGeocodeAddressList().size() > 0) {
//						GeocodeAddress address = result.getGeocodeAddressList().get(0);
//						addressLatlonStr = ""+address.getLatLonPoint();
//						
//						searchLat = Double.parseDouble(addressLatlonStr.split(",")[0].toString());//�õ�����
//						searchLng = Double.parseDouble(addressLatlonStr.split(",")[1].toString());//�õ�γ��
//						searchLatLng = new LatLng(searchLat, searchLng);//�ճɿ����ڱ�ע�ľ�γ����������
//						
//						markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//								.position(searchLatLng);
//						startMarker = aMap.addMarker(markerOption);//���һ����ע
//						aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, 16));
//						startMarkerList.add(startMarker);
//						Log.i("1003", ""+startMarkerList.size()+"begin");
//						
//						startMarkerLatLngList.add(searchLatLng);
//						startLatlonPoint = new LatLonPoint(searchLat, searchLng);//����·���滮�ľ�γ����������
//						startLatlonPointList.add(startLatlonPoint);
//						
//						//����Ѿ���ע��һ�����
//						if(startMarkerList.size()>1){
//							Log.i("1003", "����ִ����begin");
//							startMarkerList.get(0).remove();//ɾ���ѱ�ע�����
//							startMarkerList.remove(0);		//ɾ���ѱ�ע��������
//							//markerLatLngList.remove(0);		//ɾ���ѱ�ע����㾭γ��
//							startLatlonPointList.remove(0);	//ɾ������·���滮����㾭γ��
//							//����Ѿ�����һ������·��
//							if(searchWalkRouteOverlayList.size()!=0){
//								searchWalkRouteOverlayList.get(0).removeFromMap();
//								searchWalkRouteOverlayList.remove(0);
//							}
//							if(searchDrivingRouteOverlayList.size()!=0){
//								searchDrivingRouteOverlayList.get(0).removeFromMap();
//								searchDrivingRouteOverlayList.remove(0);
//							}
//							//count--;
//						}
//						
//						//����յ���ȷ��
//						if(endMarkerList.size()>0){
//							if(walkMode){									
//								searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
//							}
//							if(driveMode){
//								searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
//							}
//						}
//					} else {
//						ToastUtil.show(getApplicationContext(), "�Բ���û��������������ݣ�");
//					}
//				} else {
//					ToastUtil.show(getApplicationContext(), rCode);
//				}
			}
		});
	}
	/**
	 * ��Ӧ�յ��ַ�������(�����յ�)
	 * @param name
	 */
	public void getEndLatlon(final String name){
		// ��һ��������ʾ��ַ���ڶ���������ʾ��ѯ���У����Ļ�������ȫƴ��citycode��adcode��
			GeocodeQuery query = new GeocodeQuery(name, cityCode);
			geocodeSearch.getFromLocationNameAsyn(query);// ����ͬ�������������
			geocodeSearch.setOnGeocodeSearchListener(new OnGeocodeSearchListener() {
				@Override
				public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
				}
				@Override
				public void onGeocodeSearched(GeocodeResult result, int rCode) {
					// TODO Auto-generated method stub
//					if (rCode == 1000) {
//						if (result != null && result.getGeocodeAddressList() != null
//								&& result.getGeocodeAddressList().size() > 0) {
//							GeocodeAddress address = result.getGeocodeAddressList().get(0);
//							addressLatlonStr = ""+address.getLatLonPoint();
//							Log.i("geo", ""+addressLatlonStr);
//							searchLat = Double.parseDouble(addressLatlonStr.split(",")[0].toString());//�õ�����
//							searchLng = Double.parseDouble(addressLatlonStr.split(",")[1].toString());//�õ�γ��
//							searchLatLng = new LatLng(searchLat, searchLng);//�ճɿ����ڱ�ע�ľ�γ����������
//							
//							markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
//									.position(searchLatLng);
//							endMarker = aMap.addMarker(markerOption);//���һ����ע
//							aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, 16));
//							endMarkerList.add(endMarker);
//							//Log.i("1003", ""+endMarkerList.size()+"end");
//							Log.i("1003", "�յ�"+endMarkerList.size());
//							Log.i("1003", "���"+startMarkerList.size());
//							endMarkerLatLngList.add(searchLatLng);
//							endLatlonPoint = new LatLonPoint(searchLat, searchLng);
//							endLatlonPointList.add(endLatlonPoint);
//							
//							//����Ѿ���ע��һ���յ�
//							if(endMarkerList.size()>1){
//								Log.i("1003", "�յ�"+endMarkerList.size());
//								Log.i("1003", "���"+startMarkerList.size());
//								endMarkerList.get(0).remove();	//ɾ���ѱ�ע�����
//								endMarkerList.remove(0);		//ɾ���ѱ�ע��������
//								endLatlonPointList.remove(0);	//ɾ������·���滮����㾭γ��
//								if(searchWalkRouteOverlayList.size()!=0){
//									searchWalkRouteOverlayList.get(0).removeFromMap();
//									searchWalkRouteOverlayList.remove(0);
//								}
//								if(searchDrivingRouteOverlayList.size()!=0){
//									searchDrivingRouteOverlayList.get(0).removeFromMap();
//									searchDrivingRouteOverlayList.remove(0);
//								}
//							}
//							
//							//�յ�ȷ����������ʼ�滮��Ĭ��Ϊ����
//							if(walkMode){									
//								searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
//							}
//							if(driveMode){
//								searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
//							}
//						} else {
//							ToastUtil.show(getApplicationContext(), "�Բ���û��������������ݣ�");
//						}
//					} else {
//						ToastUtil.show(getApplicationContext(), rCode);
//					}
				}
			});
	}
	//�ײ������Ի���·���滮ģʽ(���С���ʻ)
	public void showDialog(){
		bottomDialog = new Dialog(this, R.style.BottomDialog);
		dialogLayout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.dialog_bottom, null);
		walkingLayout = (RelativeLayout) dialogLayout.findViewById(R.id.layout_walking);
		drivingLayout = (RelativeLayout) dialogLayout.findViewById(R.id.layout_driving);
		walkingLayout.setOnClickListener(this);
		drivingLayout.setOnClickListener(this);
		bottomDialog.setContentView(dialogLayout);
		Window dialogWindow = bottomDialog.getWindow();
		dialogWindow.setGravity(Gravity.BOTTOM);
		//��Ӷ���
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();//��ȡ�Ի���ǰ�Ĳ���
		lp.x = 0;//��λ��x����
		lp.y = 0;//��λ��y����
		lp.width = (int)getResources().getDisplayMetrics().widthPixels;//���
		dialogLayout.measure(0, 0);
		lp.height = dialogLayout.getMeasuredHeight();
		lp.alpha = 9f;//͸����
		dialogWindow.setAttributes(lp);
		bottomDialog.show();
	}
	//��ʾ�Ի���
	public void showTipsDialog(){
		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.tip));
		builder.setMessage(getResources().getString(R.string.route_tips));
		builder.setIsShowChebox(true);
		builder.setCheckBox(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				Editor editor = sp.edit();
				editor.putBoolean("isShowTips", !isChecked);
				editor.commit();
			}
		});
		builder.setPositiveButton(getResources().getString(R.string.close), new android.content.DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.title_back:	//�������˰�ť
			finish();
			break;
		/*case R.id.header_right_btn:		//����ȷ����ť
			confirmed = true;
			if(latlonPointList.size()>=2 && confirmed){	
				searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
				confirmed = false;
			}
			break;*/
		case R.id.delete_point_layout:
			if(markerList.size()!=0 && latlonPointList.size()!=0){				
				markerList.get(markerList.size()-1).remove();
				markerList.remove(markerList.size()-1);
				markerLatLngList.remove(markerLatLngList.size()-1);
				latlonPointList.remove(latlonPointList.size()-1);
				count--;
//				if(!walkRouteOverlayList.isEmpty()){					
//					walkRouteOverlayList.get(walkRouteOverlayList.size()-1).removeFromMap();
//					walkRouteOverlayList.remove(walkRouteOverlayList.size()-1);
//				}
//				if(!drivingRouteOverlayList.isEmpty()){					
//					drivingRouteOverlayList.get(drivingRouteOverlayList.size()-1).removeFromMap();
//					drivingRouteOverlayList.remove(drivingRouteOverlayList.size()-1);
//				}
			}
			break;
		case R.id.search_dest_layout:
			if(!isShown){
				searchAddr.setVisibility(View.VISIBLE);
				isShown = true;
			}else{
				searchAddr.setVisibility(View.INVISIBLE);
				isShown = false;
			}
			
			break;
		/*case R.id.route_drive:
			searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
			//mDrive.setImageResource(R.drawable.route_drive_select);
			//mBus.setImageResource(R.drawable.route_bus_normal);
			//mWalk.setImageResource(R.drawable.route_walk_normal);
			break;*/
		/*case R.id.route_walk:
			searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
			//mDrive.setImageResource(R.drawable.route_drive_normal);
			//mBus.setImageResource(R.drawable.route_bus_normal);
			//mWalk.setImageResource(R.drawable.route_walk_select);
			break;*/
		case R.id.confirm_begin://������㰴ť
			addressName = inputBegin.getText().toString().trim();
			searchMode = true;
			getBeginLatlon(addressName);
			searchAddr.setVisibility(View.INVISIBLE);
			InputMethodManager imm1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if(imm1.isActive()){
				//�ر�����̣�����������ͬ������������л�������ر�״̬��
				imm1.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_NOT_ALWAYS);
			}
			break;
		case R.id.confirm_end://�����յ㰴ť
			addressName = inputEnd.getText().toString().trim();
			searchMode = true;
			getEndLatlon(addressName);
			/*if(walkMode){				
				searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
			}
			if(driveMode){
				searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingDefault);
			}*/
			searchAddr.setVisibility(View.INVISIBLE);
			InputMethodManager imm2 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			if(imm2.isActive()){
				imm2.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_NOT_ALWAYS);
			}
			break;
		case R.id.route_mode_select://ģʽѡ��ť
			showDialog();
			break;
		case R.id.layout_walking:
			//ToastUtil.show(this, "��ѡ���˲���·�߹滮ģʽ");
			walkMode = true;
			if(markerLatLngList.size()!=0 && markerList.size()!=0 &&
					latlonPointList.size()!=0 && driveMode){
				for(int i = count-1;i>=0;i--){
					markerList.get(i).remove();
					markerList.remove(i);
					markerLatLngList.remove(i);
					latlonPointList.remove(i);
				}
			}
			if(startMarkerList.size()!=0 && startMarkerLatLngList.size()!=0 &&
					startLatlonPointList.size()!=0 && driveMode){
				startMarkerList.get(0).remove();
				startMarkerList.remove(0);
				startMarkerLatLngList.remove(0);
				startLatlonPointList.remove(0);
			}
			if(endMarkerList.size()!=0 && endMarkerLatLngList.size()!=0 &&
					endLatlonPointList.size()!=0 && driveMode){
				endMarkerList.get(0).remove();
				endMarkerList.remove(0);
				endMarkerLatLngList.remove(0);
				endLatlonPointList.remove(0);
			}
//			if(drivingRouteOverlayList.size()!=0){
//				for(int i = drivingRouteOverlayList.size()-1;i>=0;i--){
//					drivingRouteOverlayList.get(i).removeFromMap();
//					drivingRouteOverlayList.remove(i);
//				}
//			}
//			if(searchDrivingRouteOverlayList.size()!=0){
//				for(int i = searchDrivingRouteOverlayList.size()-1;i>=0;i--){
//					searchDrivingRouteOverlayList.get(i).removeFromMap();
//					searchDrivingRouteOverlayList.remove(i);
//				}
//			}
			count = 0;
			driveMode = false;
			routeMode.setImageResource(R.drawable.ic_walking);
			bottomDialog.dismiss();
			break;
		case R.id.layout_driving:
			//ToastUtil.show(this, "��ѡ���˼�ʻ·�߹滮ģʽ");
			driveMode = true;
			if(markerLatLngList.size()!=0 && markerList.size()!=0 &&
					latlonPointList.size()!=0 && walkMode){
				for(int i = count-1;i>=0;i--){
					markerList.get(i).remove();
					markerList.remove(i);
					markerLatLngList.remove(i);
					latlonPointList.remove(i);
				}
			}
			if(startMarkerList.size()!=0 && startMarkerLatLngList.size()!=0 &&
					startLatlonPointList.size()!=0 && walkMode){
				startMarkerList.get(0).remove();
				startMarkerList.remove(0);
				startMarkerLatLngList.remove(0);
				startLatlonPointList.remove(0);
			}
			if(endMarkerList.size()!=0 && endMarkerLatLngList.size()!=0 &&
					endLatlonPointList.size()!=0 && walkMode){
				endMarkerList.get(0).remove();
				endMarkerList.remove(0);
				endMarkerLatLngList.remove(0);
				endLatlonPointList.remove(0);
			}
//			if(walkRouteOverlayList.size()!=0){
//				for(int i = walkRouteOverlayList.size()-1;i>=0;i--){
//					walkRouteOverlayList.get(i).removeFromMap();
//					walkRouteOverlayList.remove(i);
//				}
//			}
//			if(searchWalkRouteOverlayList.size()!=0){
//				for(int i = searchWalkRouteOverlayList.size()-1;i>=0;i--){
//					searchWalkRouteOverlayList.get(i).removeFromMap();
//					searchWalkRouteOverlayList.remove(i);
//				}
//			}
			count = 0;
			walkMode = false;
			routeMode.setImageResource(R.drawable.ic_driving);
			bottomDialog.dismiss();
			break;
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ��ͼ����¼��ص�����
	 */
	@Override
	public void onMapClick(LatLng latlng) {
		// TODO Auto-generated method stub
		searchMode = false;
		markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
				.position(latlng);
		marker = aMap.addMarker(markerOption);//���һ����ע
		markerList.add(marker);//��ÿһ����ӱ�ע��mark������ӵ�markList������
		markerLatLngList.add(latlng);//��ÿһ����ӱ�ע�ľ�γ�ȶ���latlng��ӵ�markerpoints������
		latlonPoint = new LatLonPoint(latlng.latitude, latlng.longitude);//ÿ���һ�ε�ͼ����һ��LatLonPoint�������ڹ滮
		latlonPointList.add(latlonPoint);//��LatLonPoint������ӵ������У����ڶ��滮
		count++;
		if(walkMode){
			searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
		}
		if(driveMode){
			searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DrivingShortDistance);
		}
	}

	/**
	 * ���λ
	 */
	@Override
	public void activate(OnLocationChangedListener listener) {
		// TODO Auto-generated method stub
		mListener = listener;
		if (mlocationClient == null) {
			mlocationClient = new AMapLocationClient(this);
			mLocationOption = new AMapLocationClientOption();
			mLocationOption.setSensorEnable(true);
			//���ö�λ����
			mlocationClient.setLocationListener(this);
			//����Ϊ�߾��ȶ�λģʽ
			mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
			//���ö�λ����
			mlocationClient.setLocationOption(mLocationOption);
			// �˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
			// ע�����ú��ʵĶ�λʱ��ļ������С���֧��Ϊ2000ms���������ں���ʱ�����stopLocation()������ȡ����λ����
			// �ڶ�λ�������ں��ʵ��������ڵ���onDestroy()����
			// �ڵ��ζ�λ����£���λ���۳ɹ���񣬶��������stopLocation()�����Ƴ����󣬶�λsdk�ڲ����Ƴ�
			mlocationClient.startLocation();
		}
	}

	/**
	 * ֹͣ��λ
	 */
	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		mListener = null;
		if (mlocationClient != null) {
			mlocationClient.stopLocation();
			mlocationClient.onDestroy();
		}
		mlocationClient = null;
	}
	/**
	 * ��λ�ɹ���ص�����
	 */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		AMapLocationClientOption option = new AMapLocationClientOption();
		option.setSensorEnable(true);
		// TODO Auto-generated method stub
		if (mListener != null && amapLocation != null) {
			if (amapLocation != null
					&& amapLocation.getErrorCode() == 0) {
				//mLocationErrText.setVisibility(View.GONE);
				mListener.onLocationChanged(amapLocation);// ��ʾϵͳС����
			} else {
				String errText = "��λʧ��," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
				Log.e("AmapErr",errText);
				//mLocationErrText.setVisibility(View.VISIBLE);
				//mLocationErrText.setText(errText);
			}
			double longitude = amapLocation.getLongitude();
			double latitude = amapLocation.getLatitude();
			double altitude = amapLocation.getAltitude();
			Log.i("location", "longitude:"+longitude+";latitude:"+latitude+";altitude:"+altitude);
		}
	}
	/**
	 * ��������·���滮����Ļص�������
	 */
	@Override
	public void onBusRouteSearched(BusRouteResult result, int errorCode) {
		// TODO Auto-generated method stub
		//mBottomLayout.setVisibility(View.GONE);
		//aMap.clear();// �����ͼ�ϵ����и�����
		if (errorCode == 1000) {
			if (result != null && result.getPaths() != null) {
				if (result.getPaths().size() > 0) {
					mBusRouteResult = result;
//					BusRouteOverlay mBusrouteOverlay = new BusRouteOverlay(this, aMap,
//							mBuspath, mBusRouteResult.getStartPos(),
//							mBusRouteResult.getTargetPos());
					//BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(mContext, mBusRouteResult);
					//mBusResultList.setAdapter(mBusResultListAdapter);		
				} else if (result != null && result.getPaths() == null) {
					ToastUtil.show(getApplicationContext(), R.string.no_result);
				}
			} else {
				ToastUtil.show(getApplicationContext(), R.string.no_result);
			}
		} else {
			ToastUtil.show(getApplicationContext(), errorCode);
		}
	}
	/**
	 * �ݳ�·���滮����Ļص�������
	 */
	@Override
	public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
		// TODO Auto-generated method stub
		if (errorCode == 1000) {
			if (result != null && result.getPaths() != null) {
				if (result.getPaths().size() > 0) {
//					mDriveRouteResult = result;
//					final DrivePath drivePath = mDriveRouteResult.getPaths()
//							.get(0);
//					drivingRouteOverlay = new MyDrivingRouteOverlay(
//							getApplicationContext(), aMap, drivePath,
//							mDriveRouteResult.getStartPos(),
//							mDriveRouteResult.getTargetPos(), null);
//					drivingRouteOverlay.setNodeIconVisibility(false);//���ýڵ�marker�Ƿ���ʾ
//					drivingRouteOverlay.removeFromMap();
//					drivingRouteOverlay.addToMap();
//					if(!searchMode){						
//						drivingRouteOverlayList.add(drivingRouteOverlay);
//					}else{
//						searchDrivingRouteOverlayList.add(drivingRouteOverlay);
//						drivingRouteOverlay.zoomToSpan();
//					}
//					//drivingRouteOverlay.zoomToSpan();
//					if(startMarkerList.size()!=0 && startLatlonPointList.size()!=0){//��������
//						if(!walkRouteOverlayList.isEmpty()){
//							for(int i = 0;i<2;i++){
//								walkRouteOverlayList.get(i).removeFromMap();								
//								walkRouteOverlayList.remove(i);
//							}
//						}
//					}
				} else if (result != null && result.getPaths() == null) {
					ToastUtil.show(getApplicationContext(), R.string.no_result);
				}

			} else {
				ToastUtil.show(getApplicationContext(), R.string.no_result);
			}
		} else {
			ToastUtil.show(getApplicationContext(), errorCode);
		}
	}
//	public class MyDrivingRouteOverlay extends DrivingRouteOverlay{
//
//		public MyDrivingRouteOverlay(Context arg0, AMap arg1, DrivePath arg2, LatLonPoint arg3, LatLonPoint arg4,
//				List<LatLonPoint> arg5) {
//			super(arg0, arg1, arg2, arg3, arg4, arg5);
//			// TODO Auto-generated constructor stub
//		}
//		@Override
//		protected BitmapDescriptor getStartBitmapDescriptor() {
//			// TODO Auto-generated method stub
//			BitmapDescriptor startBitmapDescriptor=new BitmapDescriptorFactory().fromResource(R.drawable.start);
//			return startBitmapDescriptor;
//		}
//		@Override
//		protected BitmapDescriptor getEndBitmapDescriptor() {
//			// TODO Auto-generated method stub
//			BitmapDescriptor endBitmapDescriptor=new BitmapDescriptorFactory().fromResource(R.drawable.end);
//			return endBitmapDescriptor;
//		}
//		@Override
//		protected BitmapDescriptor getDriveBitmapDescriptor() {
//			// TODO Auto-generated method stub
//			
//			return super.getDriveBitmapDescriptor();
//		}
//		
//		
//	}
	/**
	 * ����·���滮����Ļص�������
	 * @param result ����·���滮�Ľ����
	 * @param errorCode ���ؽ���ɹ�����ʧ�ܵ���Ӧ��
	 */
	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
		// TODO Auto-generated method stub
		if (errorCode == 1000) {
			if (result != null && result.getPaths() != null) {
				if (result.getPaths().size() > 0) {
//					mWalkRouteResult = result;
//					final WalkPath walkPath = mWalkRouteResult.getPaths()
//							.get(0);
//					walkRouteOverlay = new MyWalkRouteOverlay(
//							this, aMap, walkPath,
//							mWalkRouteResult.getStartPos(),
//							mWalkRouteResult.getTargetPos());
//					walkRouteOverlay.removeFromMap();
//					walkRouteOverlay.addToMap();
//					if(!searchMode){						
//						walkRouteOverlayList.add(walkRouteOverlay);
//					}else{
//						searchWalkRouteOverlayList.add(walkRouteOverlay);
//						walkRouteOverlay.zoomToSpan();
//					}
//					//walkRouteOverlay.zoomToSpan();
//					if(startMarkerList.size()!=0 && startLatlonPointList.size()!=0){				
//						if(!drivingRouteOverlayList.isEmpty()){
//							for(int i = 0;i<2;i++){
//								drivingRouteOverlayList.get(i).removeFromMap();								
//								drivingRouteOverlayList.remove(i);
//							}
//						}
//					}
				} else if (result != null && result.getPaths() == null) {
					ToastUtil.show(getApplicationContext(), R.string.no_result);
				}
			} else {
				ToastUtil.show(getApplicationContext(), R.string.no_result);
			}
		} else {
			ToastUtil.show(getApplicationContext(), errorCode);
		}
	}
	//��һ���ڲ���̳�WalkRouteOverlay������д���еķ������Ըı�·���滮�ߺ͵����ʽ
//	public class MyWalkRouteOverlay extends WalkRouteOverlay{
//		
//		public MyWalkRouteOverlay(Context arg0, AMap arg1, WalkPath arg2, LatLonPoint arg3, LatLonPoint arg4) {
//			super(arg0, arg1, arg2, arg3, arg4);
//			// TODO Auto-generated constructor stub
//		}
//		//���marker
//		@Override
//		protected BitmapDescriptor getStartBitmapDescriptor() {
//			// TODO Auto-generated method stub
//			BitmapDescriptor startBitmapDescriptor=new BitmapDescriptorFactory().fromResource(R.drawable.start);
//				return startBitmapDescriptor;
//		}
//		//�յ�marker
//		@Override
//		protected BitmapDescriptor getEndBitmapDescriptor() {
//			// TODO Auto-generated method stub
//			BitmapDescriptor otherBitmapDescriptor=new BitmapDescriptorFactory().fromResource(R.drawable.icon_mark);
//			BitmapDescriptor endBitmapDescriptor=new BitmapDescriptorFactory().fromResource(R.drawable.end);
//			//if(i!=count-2){
//				//return otherBitmapDescriptor;
//			//}else{				
//				return endBitmapDescriptor;
//			//}
//		}
//		//�м��marker
//		@Override
//		protected BitmapDescriptor getWalkBitmapDescriptor() {
//			// TODO Auto-generated method stub
//			//��ԭ����С��ͼƬ��Ϊ͸��ͼƬ
//			BitmapDescriptor midBitmapDescriptor=new BitmapDescriptorFactory().fromResource(R.drawable.none);
//			return midBitmapDescriptor;
//		}
//		
//		@Override
//		public void setNodeIconVisibility(boolean arg0) {
//			// TODO Auto-generated method stub
//			super.setNodeIconVisibility(arg0);
//		}
//		
//	}
	
	@Override
	public boolean onMarkerClick(Marker arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onInfoWindowClick(Marker arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * ����������д
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mapView.onResume();
	}
	
	/**
	 * ����������д
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mapView.onResume();
	}
	
	/**
	 * ����������д
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}
	
	/**
	 * ����������д
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mapView.onDestroy();
	}
	@Override
	public void onGeocodeSearched(GeocodeResult result, int rCode) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * �����������ݱ仯�Ļص�����
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		String newText = s.toString().trim();
        InputtipsQuery inputquery = new InputtipsQuery(newText, city);
        inputquery.setCityLimit(true);
        Inputtips inputTips = new Inputtips(getApplicationContext(), inputquery);
        inputTips.setInputtipsListener(this);
        inputTips.requestInputtipsAsyn();
	}
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * ���������Զ���ʾ�Ļص�����
	 */
	@Override
	public void onGetInputtips(List<Tip> tipList, int rCode) {
		// TODO Auto-generated method stub
		if (rCode == 1000) {
            final List<HashMap<String, String>> listString = new ArrayList<HashMap<String, String>>();
            for (int i = 0; i < tipList.size(); i++) {
            	HashMap<String, String> map = new HashMap<String, String>();
            	map.put("name", tipList.get(i).getName());
            	map.put("address", tipList.get(i).getDistrict());
                listString.add(map);
            }
            SimpleAdapter aAdapter = new SimpleAdapter(getApplicationContext(), listString, R.layout.item_layout, 
            		new String[] {"name","address"}, new int[] {R.id.poi_field_id, R.id.poi_value_id});

            mInputlist.setAdapter(aAdapter);
            aAdapter.notifyDataSetChanged();
            
            mInputlist.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					//ToastUtil.show(getApplicationContext(), listString.get(position).get("name"));
					inputEnd.setText(listString.get(position).get("name"));
					addressName = inputEnd.getText().toString().trim();
					searchMode = true;
					getEndLatlon(addressName);//�����յ��ַ����������γ��
					
					searchAddr.setVisibility(View.INVISIBLE);
					InputMethodManager imm1 = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					if(imm1.isActive()){
						//�ر�����̣�����������ͬ������������л�������ر�״̬��
						imm1.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
			});

        } else {
			ToastUtil.show(this.getApplicationContext(), rCode);
		}
	}
	@Override
	public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
}
