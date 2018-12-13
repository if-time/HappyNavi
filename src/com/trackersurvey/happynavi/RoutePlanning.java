package com.trackersurvey.happynavi;

import java.util.List;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
//import com.amap.api.maps.overlay.BusRouteOverlay;
//import com.amap.api.maps.overlay.DrivingRouteOverlay;
//import com.amap.api.maps.overlay.WalkRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;

import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;
import com.trackersurvey.helper.AMapUtil;
import com.trackersurvey.helper.AppManager;
import com.trackersurvey.helper.RouteSearchPoiDialog;
import com.trackersurvey.helper.ToastUtil;
import com.trackersurvey.helper.RouteSearchPoiDialog.OnListItemClick;
import com.amap.api.services.route.WalkRouteResult;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class RoutePlanning extends Activity implements OnMarkerClickListener,
			OnMapClickListener, OnInfoWindowClickListener, InfoWindowAdapter,
			OnPoiSearchListener, OnRouteSearchListener, OnClickListener{
	private AMap aMap;
	private MapView mapView;
	private Button drivingButton;
	private Button busButton;
	private Button walkButton;

	private ImageButton startImageButton;
	private ImageButton endImageButton;
	private ImageButton routeSearchImagebtn;

	private EditText startTextView;
	private EditText endTextView;
	private ProgressDialog progDialog = null;// ����ʱ������
	
	private int busMode = RouteSearch.BusDefault;// ����Ĭ��ģʽ
	private int drivingMode = RouteSearch.DrivingDefault;// �ݳ�Ĭ��ģʽ
	private int walkMode = RouteSearch.WalkDefault;// ����Ĭ��ģʽ
	
	private BusRouteResult busRouteResult;// ����ģʽ��ѯ���
	private DriveRouteResult driveRouteResult;// �ݳ�ģʽ��ѯ���
	private WalkRouteResult walkRouteResult;// ����ģʽ��ѯ���
	private int routeType = 1;// 1������ģʽ��2����ݳ�ģʽ��3������ģʽ
	private String strStart;
	private String strEnd;
	private LatLonPoint startPoint = null;
	private LatLonPoint endPoint = null;
	private PoiSearch.Query startSearchQuery;
	private PoiSearch.Query endSearchQuery;
	
	private boolean isClickStart = false;
	private boolean isClickTarget = false;
	private Marker startMk, targetMk;
	private RouteSearch routeSearch;
	public ArrayAdapter<String> aAdapter;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.route_activity);
		AppManager.getAppManager().addActivity(this);
		mapView = (MapView) findViewById(R.id.map_routeplan);
		mapView.onCreate(bundle);// �˷���������д
		init();
	}
	/**
	 * ��ʼ��AMap����
	 */
	private void init() {
		if (aMap == null) {
			aMap = mapView.getMap();
			registerListener();
		}
		routeSearch = new RouteSearch(this);
		routeSearch.setRouteSearchListener(this);
		startTextView = (EditText) findViewById(R.id.autotextview_roadsearch_start);
		endTextView = (EditText) findViewById(R.id.autotextview_roadsearch_goals);
		busButton = (Button) findViewById(R.id.imagebtn_roadsearch_tab_transit);
		busButton.setOnClickListener(this);
		drivingButton = (Button) findViewById(R.id.imagebtn_roadsearch_tab_driving);
		drivingButton.setOnClickListener(this);
		walkButton = (Button) findViewById(R.id.imagebtn_roadsearch_tab_walk);
		walkButton.setOnClickListener(this);
		startImageButton = (ImageButton) findViewById(R.id.imagebtn_roadsearch_startoption);
		startImageButton.setOnClickListener(this);
		endImageButton = (ImageButton) findViewById(R.id.imagebtn_roadsearch_endoption);
		endImageButton.setOnClickListener(this);
		routeSearchImagebtn = (ImageButton) findViewById(R.id.imagebtn_roadsearch_search);
		routeSearchImagebtn.setOnClickListener(this);
	}

	/**
	 * ע�����
	 */
	private void registerListener() {
		aMap.setOnMapClickListener(RoutePlanning.this);
		aMap.setOnMarkerClickListener(RoutePlanning.this);
		aMap.setOnInfoWindowClickListener(RoutePlanning.this);
		aMap.setInfoWindowAdapter(RoutePlanning.this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.imagebtn_roadsearch_startoption:
			startImagePoint();
			break;
		case R.id.imagebtn_roadsearch_endoption:
			endImagePoint();
			break;
		case R.id.imagebtn_roadsearch_tab_transit:
			busRoute();
			break;
		case R.id.imagebtn_roadsearch_tab_driving:
			drivingRoute();
			break;
		case R.id.imagebtn_roadsearch_tab_walk:
			walkRoute();
			break;
		case R.id.imagebtn_roadsearch_search:
			searchRoute();
			break;
		default:
			break;
		}
	}
	/**
	 * �ڵ�ͼ��ѡȡ���
	 */
	private void startImagePoint() {
		ToastUtil.show(RoutePlanning.this, "�ڵ�ͼ�ϵ���������");
		isClickStart = true;
		isClickTarget = false;
		registerListener();
	}
	/**
	 * �ڵ�ͼ��ѡȡ�յ�
	 */
	private void endImagePoint() {
		ToastUtil.show(RoutePlanning.this, "�ڵ�ͼ�ϵ�������յ�");
		isClickTarget = true;
		isClickStart = false;
		registerListener();
	}
	/**
	 * ѡ�񹫽�ģʽ
	 */
	private void busRoute() {
		routeType = 1;// ��ʶΪ����ģʽ
		busMode = RouteSearch.BusDefault;
		drivingButton.setBackgroundResource(R.drawable.mode_driving_off);//���°�ťͼ���л�
		busButton.setBackgroundResource(R.drawable.mode_transit_on);
		walkButton.setBackgroundResource(R.drawable.mode_walk_off);

	}

	/**
	 * ѡ��ݳ�ģʽ
	 */
	private void drivingRoute() {
		routeType = 2;// ��ʶΪ�ݳ�ģʽ
		drivingButton.setBackgroundResource(R.drawable.mode_driving_on);
		busButton.setBackgroundResource(R.drawable.mode_transit_off);
		walkButton.setBackgroundResource(R.drawable.mode_walk_off);
	}

	/**
	 * ѡ����ģʽ
	 */
	private void walkRoute() {
		routeType = 3;// ��ʶΪ����ģʽ
		walkMode = RouteSearch.WalkMultipath;
		drivingButton.setBackgroundResource(R.drawable.mode_driving_off);
		busButton.setBackgroundResource(R.drawable.mode_transit_off);
		walkButton.setBackgroundResource(R.drawable.mode_walk_on);
	}
	/**
	 * ���������ť��ʼRoute����
	 */
	public void searchRoute() {
		strStart = startTextView.getText().toString().trim();
		strEnd = endTextView.getText().toString().trim();
		if (strStart == null || strStart.length() == 0) {
			ToastUtil.show(RoutePlanning.this, "��ѡ�����");
			return;
		}
		if (strEnd == null || strEnd.length() == 0) {
			ToastUtil.show(RoutePlanning.this, "��ѡ���յ�");
			return;
		}
		if (strStart.equals(strEnd)) {
			ToastUtil.show(RoutePlanning.this, "������յ����ܽ��������Բ���ǰ��");
			return;
		}

		startSearchResult();// ��ʼ���յ�
	}
	/**
	 * ��ѯ·���滮���
	 */
	public void startSearchResult() {
		strStart = startTextView.getText().toString().trim();
		if (startPoint != null && strStart.equals("��ͼ�ϵ����")) {
			endSearchResult();
		} else {
			showProgressDialog();
			startSearchQuery = new PoiSearch.Query(strStart, "", "010"); // ��һ��������ʾ��ѯ�ؼ��֣��ڶ�������ʾpoi�������ͣ�������������ʾ�������Ż��߳�����
			startSearchQuery.setPageNum(0);// ���ò�ѯ�ڼ�ҳ����һҳ��0��ʼ
			startSearchQuery.setPageSize(20);// ����ÿҳ���ض���������
			PoiSearch poiSearch = new PoiSearch(RoutePlanning.this,
					startSearchQuery);
			poiSearch.setOnPoiSearchListener(this);
			poiSearch.searchPOIAsyn();// �첽poi��ѯ
		}
	}
	/**
	 * ��ѯ·���滮�յ�
	 */
	public void endSearchResult() {
		strEnd = endTextView.getText().toString().trim();
		if (endPoint != null && strEnd.equals("��ͼ�ϵ��յ�")) {
			searchRouteResult(startPoint, endPoint);
		} else {
			showProgressDialog();
			endSearchQuery = new PoiSearch.Query(strEnd, "", "010"); // ��һ��������ʾ��ѯ�ؼ��֣��ڶ�������ʾpoi�������ͣ�������������ʾ�������Ż��߳�����
			endSearchQuery.setPageNum(0);// ���ò�ѯ�ڼ�ҳ����һҳ��0��ʼ
			endSearchQuery.setPageSize(20);// ����ÿҳ���ض���������

			PoiSearch poiSearch = new PoiSearch(RoutePlanning.this,
					endSearchQuery);
			poiSearch.setOnPoiSearchListener(this);
			poiSearch.searchPOIAsyn(); // �첽poi��ѯ
		}
	}
	/**
	 * ��ʼ����·���滮����
	 */
	public void searchRouteResult(LatLonPoint startPoint, LatLonPoint endPoint) {
		showProgressDialog();
		final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
				startPoint, endPoint);
		if (routeType == 1) {// ����·���滮
			BusRouteQuery query = new BusRouteQuery(fromAndTo, busMode, "����", 0);// ��һ��������ʾ·���滮�������յ㣬�ڶ���������ʾ������ѯģʽ��������������ʾ������ѯ�������ţ����ĸ�������ʾ�Ƿ����ҹ�೵��0��ʾ������
			routeSearch.calculateBusRouteAsyn(query);// �첽·���滮����ģʽ��ѯ
		} else if (routeType == 2) {// �ݳ�·���滮
			DriveRouteQuery query = new DriveRouteQuery(fromAndTo, drivingMode,
					null, null, "");// ��һ��������ʾ·���滮�������յ㣬�ڶ���������ʾ�ݳ�ģʽ��������������ʾ;���㣬���ĸ�������ʾ�������򣬵����������ʾ���õ�·
			routeSearch.calculateDriveRouteAsyn(query);// �첽·���滮�ݳ�ģʽ��ѯ
		} else if (routeType == 3) {// ����·���滮
			WalkRouteQuery query = new WalkRouteQuery(fromAndTo, walkMode);
			routeSearch.calculateWalkRouteAsyn(query);// �첽·���滮����ģʽ��ѯ
		}
	}
	/**
	 * POI��������ص�
	 */
	@Override
	public void onPoiSearched(PoiResult result, int rCode) {
		dissmissProgressDialog();
		if (rCode == 0) {// ���سɹ�
			if (result != null && result.getQuery() != null
					&& result.getPois() != null && result.getPois().size() > 0) {// ����poi�Ľ��
				if (result.getQuery().equals(startSearchQuery)) {
					List<PoiItem> poiItems = result.getPois();// ȡ��poiitem����
					RouteSearchPoiDialog dialog = new RouteSearchPoiDialog(
							RoutePlanning.this, poiItems);
					dialog.setTitle("��Ҫ�ҵ������:");
					dialog.show();
					dialog.setOnListClickListener(new OnListItemClick() {
						@Override
						public void onListItemClick(
								RouteSearchPoiDialog dialog,
								PoiItem startpoiItem) {
							startPoint = startpoiItem.getLatLonPoint();
							strStart = startpoiItem.getTitle();
							startTextView.setText(strStart);
							endSearchResult();// ��ʼ���յ�,�������ҵ���--��
						}

					});
				} else if (result.getQuery().equals(endSearchQuery)) {
					List<PoiItem> poiItems = result.getPois();// ȡ��poiitem����
					RouteSearchPoiDialog dialog = new RouteSearchPoiDialog(
							RoutePlanning.this, poiItems);
					dialog.setTitle("��Ҫ�ҵ��յ���:");
					dialog.show();
					dialog.setOnListClickListener(new OnListItemClick() {
						@Override
						public void onListItemClick(
								RouteSearchPoiDialog dialog, PoiItem endpoiItem) {
							endPoint = endpoiItem.getLatLonPoint();
							strEnd = endpoiItem.getTitle();
							endTextView.setText(strEnd);
							searchRouteResult(startPoint, endPoint);// ����·���滮����
						}

					});
				}
			} else {
				ToastUtil.show(RoutePlanning.this, R.string.no_result);
			}
		} else if (rCode == 27) {
			ToastUtil.show(RoutePlanning.this, R.string.error_network);
		} else if (rCode == 32) {
			ToastUtil.show(RoutePlanning.this, R.string.error_key);
		} else {
			ToastUtil.show(RoutePlanning.this, getString(R.string.error_other)
					+ rCode);
		}
	}
	/**
	 * ����·�߲�ѯ�ص�
	 */
	@Override
	public void onBusRouteSearched(BusRouteResult result, int rCode) {
		dissmissProgressDialog();
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				busRouteResult = result;
				BusPath busPath = busRouteResult.getPaths().get(0);
				aMap.clear();// �����ͼ�ϵ����и�����
//				BusRouteOverlay routeOverlay = new BusRouteOverlay(this, aMap,
//						busPath, 
//						
// 						busRouteResult.getStartPos(),
// 					busRouteResult.getTargetPos()
//						 
//						
//						);
//				routeOverlay.removeFromMap();
//				routeOverlay.addToMap();
//				routeOverlay.zoomToSpan();
			} else {
				ToastUtil.show(RoutePlanning.this, R.string.no_result);
			}
		} else if (rCode == 27) {
			ToastUtil.show(RoutePlanning.this, R.string.error_network);
		} else if (rCode == 32) {
			ToastUtil.show(RoutePlanning.this, R.string.error_key);
		} else {
			ToastUtil.show(RoutePlanning.this, getString(R.string.error_other)
					+ rCode);
		}
	}

	/**
	 * �ݳ�����ص�
	 */
	@Override
	public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
		dissmissProgressDialog();
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				driveRouteResult = result;
				DrivePath drivePath = driveRouteResult.getPaths().get(0);
				aMap.clear();// �����ͼ�ϵ����и�����
//				DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
//						this, aMap, drivePath, driveRouteResult.getStartPos(),
//						driveRouteResult.getTargetPos());
//				drivingRouteOverlay.removeFromMap();
//				drivingRouteOverlay.addToMap();
//				drivingRouteOverlay.zoomToSpan();
			} else {
				ToastUtil.show(RoutePlanning.this, R.string.no_result);
			}
		} else if (rCode == 27) {
			ToastUtil.show(RoutePlanning.this, R.string.error_network);
		} else if (rCode == 32) {
			ToastUtil.show(RoutePlanning.this, R.string.error_key);
		} else {
			ToastUtil.show(RoutePlanning.this, getString(R.string.error_other)
					+ rCode);
		}
	}

	/**
	 * ����·�߽���ص�
	 */
	@Override
	public void onWalkRouteSearched(WalkRouteResult result, int rCode) {
		dissmissProgressDialog();
		if (rCode == 0) {
			if (result != null && result.getPaths() != null
					&& result.getPaths().size() > 0) {
				walkRouteResult = result;
				WalkPath walkPath = walkRouteResult.getPaths().get(0);
				aMap.clear();// �����ͼ�ϵ����и�����
//				WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this,
//						aMap, walkPath, walkRouteResult.getStartPos(),
//						walkRouteResult.getTargetPos());
//				walkRouteOverlay.removeFromMap();
//				walkRouteOverlay.addToMap();
//				walkRouteOverlay.zoomToSpan();
			} else {
				ToastUtil.show(RoutePlanning.this, R.string.no_result);
			}
		} else if (rCode == 27) {
			ToastUtil.show(RoutePlanning.this, R.string.error_network);
		} else if (rCode == 32) {
			ToastUtil.show(RoutePlanning.this, R.string.error_key);
		} else {
			ToastUtil.show(RoutePlanning.this, getString(R.string.error_other)
					+ rCode);
		}
	}
	
	@Override
	public void onMapClick(LatLng latng) {
		Toast.makeText(RoutePlanning.this, "onMapClick", 0).show();
		if (isClickStart) {
			startMk = aMap.addMarker(new MarkerOptions()
					.anchor(0.5f, 1)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.point)).position(latng)
					.title("���ѡ��Ϊ���"));
			startMk.showInfoWindow();
		} else if (isClickTarget) {
			targetMk = aMap.addMarker(new MarkerOptions()
					.anchor(0.5f, 1)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.point)).position(latng)
					.title("���ѡ��ΪĿ�ĵ�"));
			targetMk.showInfoWindow();
		}
	}
	@Override
	public void onInfoWindowClick(Marker marker) {
		isClickStart = false;
		isClickTarget = false;
		Toast.makeText(RoutePlanning.this, "onInfoWindowClick", 0).show();
		if (marker.equals(startMk)) {
			startTextView.setText("��ͼ�ϵ����");
			startPoint = AMapUtil.convertToLatLonPoint(startMk.getPosition());
			startMk.hideInfoWindow();
			startMk.remove();
		} else if (marker.equals(targetMk)) {
			endTextView.setText("��ͼ�ϵ��յ�");
			endPoint = AMapUtil.convertToLatLonPoint(targetMk.getPosition());
			targetMk.hideInfoWindow();
			targetMk.remove();
		}
		else{
			
		}
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		Toast.makeText(RoutePlanning.this, "onMarkerClick", 0).show();
		if (marker.isInfoWindowShown()) {
			marker.hideInfoWindow();
		} else {
			marker.showInfoWindow();
		}
		return false;
	}

	
	/**
	 * ��ʾ���ȿ�
	 */
	private void showProgressDialog() {
		if (progDialog == null)
			progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progDialog.setIndeterminate(false);
		progDialog.setCancelable(true);
		progDialog.setMessage("��������");
		progDialog.show();
	}

	/**
	 * ���ؽ��ȿ�
	 */
	private void dissmissProgressDialog() {
		if (progDialog != null) {
			progDialog.dismiss();
		}
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

	
	/**
	 * ����������д
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	/**
	 * ����������д
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	/**
	 * ����������д
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	/**
	 * ����������д
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}
	@Override
	public void onPoiItemSearched(PoiItem arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onRideRouteSearched(RideRouteResult arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
}
