package com.example.eric.lbstest;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.eric.lbstest.base.BaseMapActivity;
import com.example.eric.lbstest.maplistener.MyOrientationListener;
import com.example.eric.lbstest.utils.ToastUtils;

import java.util.List;

import butterknife.BindView;

public class PickLocationOfEyeActivity extends BaseMapActivity implements View.OnClickListener, OnGetGeoCoderResultListener, BaiduMap.OnMapStatusChangeListener {

    //界面控件
    @BindView(R.id.bmapView)
    MapView mapView; //地图控件

    @BindView(R.id.img_location_back_origin)
    ImageView img_location_back_origin;

    @BindView(R.id.btn_back)
    Button btn_back;

    @BindView(R.id.tv_address)
    TextView tv_address;

    @BindView(R.id.tv_location)
    TextView tv_location;

    @BindView(R.id.tv_locating)
    TextView tv_locating;

    @BindView(R.id.tv_offline_result)
    TextView tv_offline_result;

    @BindView(R.id.img_select_point)
    ImageView img_select_point;

    BaiduMap baiduMap = null;

    //定位相关
    private boolean isFirstLocate = true;
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;

    //自定义定位图标
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;

    //当前经纬度
    private LatLng mLocationLatLng;

    //获取的位置
    private String mLocationValue;

    // MapView 中央对于的屏幕坐标
    private android.graphics.Point mCenterPoint = null;
    private LatLng mCenterLatLng;

    //地理编码
    private GeoCoder mSearch;

    //模式变量
    private MyLocationConfiguration.LocationMode mLocationMode;

    //电子眼经纬度信息
    LatLng eyeLatLng;

    PoiInfo eyeInfo = new PoiInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location_of_eye);

        requestLocation();
        initView();

    }

    private void initView() {
        // 地图初始化
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);

        //去掉百度logo
        View child = mapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)){
            child.setVisibility(View.INVISIBLE);
        }

        //指南针控件
        baiduMap.getUiSettings().setCompassEnabled(true);

        //调整比例尺位置
        baiduMap.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Point point_scale = new Point();
                point_scale.x = 160;
                point_scale.y = 1380;
                mapView.setScaleControlPosition(point_scale);

                Point point_zoom = new Point();
                point_zoom.x = 930;
                point_zoom.y = 800;
                mapView.setZoomControlsPosition(point_zoom);
            }
        });

        // 初始化当前 MapView 中心屏幕坐标
        mCenterPoint = baiduMap.getMapStatus().targetScreen;//这里为-1，-1！！！！需要后面再获取
        //ToastUtils.showShort(MainActivity.this, mCenterPoint.x + "\n" + mCenterPoint.y);
        mLocationLatLng = baiduMap.getMapStatus().target;

        // 地理编码
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        // 地图状态监听
        baiduMap.setOnMapStatusChangeListener(this);

        baiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;

                    case MotionEvent.ACTION_UP:
                        break;
                }
            }
        });

        img_location_back_origin.setOnClickListener(this);
        btn_back.setOnClickListener(this);
    }

    private void requestLocation() {
        initLocation();

        // 开启定位
        mLocationClient.start();

        // 开启方向传感器
        myOrientationListener.start();
    }

    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);//使用GPS定位
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");//bd09ll：百度经纬度坐标
        //option.setCoorType("bd09");//bd09：百度墨卡托坐标
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);

        //初始化图标
        //mIconLocation = BitmapDescriptorFactory.fromResource(R.mipmap.icon_landing_arrow);

        myOrientationListener = new MyOrientationListener(this);

        myOrientationListener.setmOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_location_back_origin:
                if (mLocationLatLng != null) {
                    // 实现动画跳转
                    MapStatusUpdate u = MapStatusUpdateFactory
                            .newLatLng(mLocationLatLng);
                    baiduMap.animateMapStatus(u);
                }
                break;

            case R.id.btn_back:
                finish();
                break;

            case R.id.tv_ok:
                ToastUtils.showShort(PickLocationOfEyeActivity.this, "Click Button OK!");
                Intent intent = new Intent();
                intent.putExtra("EyeLatLng", eyeLatLng);
                intent.putExtra("EyeAddress", eyeInfo.address);
                setResult(RESULT_OK, intent);
                finish();
                break;

            default:
                break;
        }
    }

    private void navigateTo(BDLocation location) {
        //searchEditView.setText(isFirstLocate ? "true" : "false");
        mLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(isFirstLocate) {
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(mLocationLatLng);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);

            /*判断baiduMap是已经移动到指定位置666666666*/
            if (baiduMap.getLocationData()!=null)
                if (baiduMap.getLocationData().latitude==location.getLatitude()
                        &&baiduMap.getLocationData().longitude==location.getLongitude()){
                    isFirstLocate = false;
                }
        }
        MyLocationData locationData = new MyLocationData.Builder()
                .direction(mCurrentX)
                .accuracy(location.getRadius())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
        baiduMap.setMyLocationData(locationData);
        mCenterPoint = baiduMap.getMapStatus().targetScreen;//真正的初始化赋值！！！！！！！！
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        // 正向地理编码指的是由地址信息转换为坐标点的过程
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }
        //ToastUtils.showLong(MainActivity.this, result.getAddress());
        // 获取反向地理编码结果
        final PoiInfo mCurrentInfo = new PoiInfo();
        mCurrentInfo.address = result.getAddress();
        mCurrentInfo.location = result.getLocation();

        mLocationValue = result.getAddress();
        eyeLatLng = result.getLocation();//得到当前设置电子眼的经纬度
        eyeInfo.address = result.getAddress();//得到当前设置电子眼的地址信息

        List<PoiInfo> poiInfoList = result.getPoiList();

        tv_locating.setVisibility(View.GONE);
        tv_address.setVisibility(View.VISIBLE);
        tv_location.setVisibility(View.VISIBLE);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_address.setText(mCurrentInfo.address);
                tv_location.setText(mCurrentInfo.address);
            }
        });

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {
        /*
        mCenterLatLng = baiduMap.getProjection().fromScreenLocation(
                mCenterPoint);
        baiduMap.clear();
        BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.mipmap.icon_focus_marker);
        MarkerOptions markerOptions = new MarkerOptions().icon(desc).position(mCenterLatLng);
        baiduMap.addOverlay(markerOptions);*/
    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        tv_address.setVisibility(View.GONE);
        tv_location.setVisibility(View.GONE);
        tv_locating.setVisibility(View.VISIBLE);
        // 获取当前 MapView 中心屏幕坐标对应的地理坐标
        mCenterLatLng = baiduMap.getProjection().fromScreenLocation(
                mCenterPoint);
        // 发起反地理编码检索
        mSearch.reverseGeoCode((new ReverseGeoCodeOption())
                .location(mCenterLatLng));
    }

    //定位监听器
    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null) {
                return;
            }

            if(location.getLocType() == BDLocation.TypeGpsLocation ||
                    location.getLocType() == BDLocation.TypeNetWorkLocation) {
                navigateTo(location);
            }

            // 设置自定义图标
            MyLocationConfiguration config = new MyLocationConfiguration(mLocationMode,
                    true, mIconLocation);
            baiduMap.setMyLocationConfigeration(config);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止定位
        if(mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
        //停止方向传感器
        myOrientationListener.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
