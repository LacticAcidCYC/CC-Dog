package com.example.eric.lbstest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BikingRouteOverlay;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.eric.lbstest.adapter.LocationAdapter;
import com.example.eric.lbstest.base.BaseMapActivity;
import com.example.eric.lbstest.maplistener.MyOrientationListener;
import com.example.eric.lbstest.maputils.MyPoiOverlay;
import com.example.eric.lbstest.utils.AnimationUtils;
import com.example.eric.lbstest.utils.LogUtils;
import com.example.eric.lbstest.utils.ToastUtils;
import com.melnykov.fab.FloatingActionButton;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;

public class MainActivity extends BaseMapActivity implements View.OnClickListener, OnGetPoiSearchResultListener,
        OnGetGeoCoderResultListener, BaiduMap.OnMapStatusChangeListener, AdapterView.OnItemClickListener, OnGetBusLineSearchResultListener, OnGetRoutePlanResultListener {

    private static final String TAG = "MainActivity";

    //界面控件
    @BindView(R.id.position_text_view)
    TextView positionText;

    @BindView(R.id.search_linear_layout)
    LinearLayout search_linear_layout;

    @BindView(R.id.search_edit_view)
    EditText searchEditView;

    @BindView(R.id.btn_traffic_map)
    Button btn_traffic_map;

    @BindView(R.id.btn_heat_map)
    Button btn_heat_map;

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @BindView(R.id.bmapView)
    MapView mapView; //地图控件

    @BindView(R.id.nearby_relative_layout)
    RelativeLayout nearby_relative_layout;

    @BindView(R.id.tv_add_eye)
    TextView tv_add_eye;

    @BindView(R.id.img_location_back_origin)
    ImageView img_location_back_origin;

    @BindView(R.id.pb_location_load_bar)
    ProgressBar pb_location_load_bar; //进度条

    BaiduMap baiduMap = null;

    //定位相关
    private boolean isFirstLocate = true;
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;

    // MapView 中央对于的屏幕坐标
    private android.graphics.Point mCenterPoint = null;

    //自定义定位图标
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;

    //当前经纬度
    private LatLng mLocationLatLng;

    //附近地点列表
    @BindView(R.id.lv_location_position)
    ListView lv_location_position;

    //获取的位置
    private String mLocationValue;

    //列表数据
    private List<PoiInfo> datas;

    //列表适配器
    private LocationAdapter locatorAdapter;

    //地理编码
    private GeoCoder mSearch;

    //模式变量
    private MyLocationConfiguration.LocationMode mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;//有普通、跟随、罗盘模式

    //搜索相关
    private PoiSearch mPoiSearch = null;

    private RoutePlanSearch routePlanSearch;//路径规划搜索接口

    private BusLineSearch busLineSearch;//公交线路查询接口

    private List<String> busLines = new ArrayList<>();

    private int uidPostion = 0;//当前选中第几条路线

    //各种线路规划绘制对象(覆盖物)
    private BusLineOverlay busLineOverlay;//公交

    private BikingRouteOverlay bikingRouteOverlay;//自行车

    //private DrivingRouteOverlay drivingRouteOverlay;//驾车
    private MyDrivingRouteOverlay drivingRouteOverlay;//驾车

    private List<MyDrivingRouteOverlay> drivingRouteOverlays = new ArrayList<>();

    private TransitRouteOverlay transitRouteOverlay;//换乘

    private WalkingRouteOverlay walkingRouteOverlay;//步行

    //导航与路线规划位置
    LatLng latLng_start;

    LatLng latLng_end;

    //路径规划中的出行节点信息,出行节点包括：起点，终点，途经点
    PlanNode fromPlanNode;

    PlanNode toPlanNode;

    //各类地图开关
    private static boolean FLAG_TRAFFIC_MAP = false;

    private static boolean FLAG_HEAT_MAP = false;

    //请求码
    private final static int REQUEST_CODE = 0x123;
    private final static int REQUEST_NAV_CODE = 0x234;

    //标志
    private boolean isTouch = true;
    private boolean isLongClick = false;
    private boolean isClick = false;
    private boolean isNav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bmob默认初始化
        Bmob.initialize(this, "e6d034b2b3fe34777426c63cf0685f1c");

        List<String> permissionList = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
            .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
            .READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
            .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }

        initViews();//先定位后显示——解决总是开始时显示北京地图的问题
    }

    private void initViews() {
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
                point_scale.x = 250;
                point_scale.y = 2130;
                mapView.setScaleControlPosition(point_scale);

                Point point_zoom = new Point();
                point_zoom.x = 1250;
                point_zoom.y = 800;
                mapView.setZoomControlsPosition(point_zoom);
            }
        });
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(isNav) {
                    //ToastUtils.showShort(MainActivity.this, "Navigating!!!");
                    return;
                }
                if(isLongClick) {
                    isLongClick = false;
                } else if(!isLongClick) {
                    if(!isClick) {
                        search_linear_layout.setVisibility(View.GONE);
                        search_linear_layout.setAnimation(AnimationUtils.moveToViewTop());

                        btn_traffic_map.setVisibility(View.GONE);
                        btn_traffic_map.setClickable(false);
                        AnimationUtils.setHideAnimation(btn_traffic_map, 500);
                        //btn_traffic_map.setAnimation(android.view.animation.AnimationUtils
                        //        .makeOutAnimation(MainActivity.this, true));

                        btn_heat_map.setVisibility(View.GONE);
                        btn_heat_map.setClickable(false);
                        AnimationUtils.setHideAnimation(btn_heat_map, 500);
                        //btn_heat_map.setAnimation(android.view.animation.AnimationUtils
                        //        .makeOutAnimation(MainActivity.this, true));

                        floatingActionButton.setVisibility(View.GONE);
                        floatingActionButton.setClickable(false);
                        AnimationUtils.setHideAnimation(floatingActionButton, 500);
                        //floatingActionButton.setAnimation(android.view.animation.AnimationUtils
                        //        .makeOutAnimation(MainActivity.this, true));
                        isClick = true;
                    } else if(isClick) {
                        search_linear_layout.setVisibility(View.VISIBLE);
                        search_linear_layout.setAnimation(AnimationUtils.moveToViewLocationfromTop());

                        btn_traffic_map.setVisibility(View.VISIBLE);
                        btn_traffic_map.setClickable(true);
                        AnimationUtils.setShowAnimation(btn_traffic_map, 500);
                        //btn_traffic_map.setAnimation(android.view.animation.AnimationUtils
                        //        .makeInAnimation(MainActivity.this, false));

                        btn_heat_map.setVisibility(View.VISIBLE);
                        btn_heat_map.setClickable(true);
                        AnimationUtils.setShowAnimation(btn_heat_map, 500);
                        //btn_heat_map.setAnimation(android.view.animation.AnimationUtils
                        //        .makeInAnimation(MainActivity.this, false));

                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.setClickable(true);
                        AnimationUtils.setShowAnimation(floatingActionButton, 500);
                        //floatingActionButton.setAnimation(android.view.animation.AnimationUtils
                        //        .makeInAnimation(MainActivity.this, false));

                        isClick = false;
                    }
                }

                baiduMap.clear();
                nearby_relative_layout.setVisibility(View.GONE);
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                if(isNav) {
                    return false;
                }
                LatLng poiLatLng = mapPoi.getPosition();
                ToastUtils.showShort(MainActivity.this, "地址：" + mapPoi.getName());
                baiduMap.clear();
                BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.mipmap.icon_focus_marker);
                MarkerOptions markerOptions = new MarkerOptions()
                        .icon(desc)
                        .position(poiLatLng)
                        .animateType(MarkerOptions.MarkerAnimateType.grow);
                baiduMap.addOverlay(markerOptions);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(poiLatLng);
                baiduMap.animateMapStatus(u);
                return false;
            }
        });

        //长按地图效果
        baiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //ToastUtils.showLong(MainActivity.this, "11111111111");
                isLongClick = true;
                baiduMap.clear();
                BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.mipmap.icon_focus_marker);
                MarkerOptions markerOptions = new MarkerOptions()
                        .icon(desc)
                        .position(latLng)
                        .animateType(MarkerOptions.MarkerAnimateType.grow);
                baiduMap.addOverlay(markerOptions);
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
                baiduMap.animateMapStatus(u);
                //mCenterPoint = baiduMap.getMapStatus().targetScreen;
                //ToastUtils.showShort(MainActivity.this, mCenterPoint.x + "\n" + "11111"+ "\n" + mCenterPoint.y);
                searchPoi();

                nearby_relative_layout.setVisibility(View.VISIBLE);
                //isLongClick = false;
            }
        });

        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                return false;
            }
        });

        //searchEditView.setInputType(InputType.TYPE_NULL);
        searchEditView.setCursorVisible(false);//设置光标不可见
        searchEditView.setOnClickListener(this);
        //设置长按也能打开搜索页
        searchEditView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent searchPositionIntent = new Intent(MainActivity.this, SearchPositionActivity.class);
                startActivityForResult(searchPositionIntent, REQUEST_CODE);
                return true;
            }
        });

        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 路线查询初始化，并注册监听
        busLineSearch = BusLineSearch.newInstance();
        busLineSearch.setOnGetBusLineSearchResultListener(this);

        // 路线规划初始化，并注册监听
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(this);

        // 初始化当前 MapView 中心屏幕坐标
        mCenterPoint = baiduMap.getMapStatus().targetScreen;//这里为-1，-1！！！！需要后面再获取
        //ToastUtils.showShort(MainActivity.this, mCenterPoint.x + "\n" + mCenterPoint.y);
        mLocationLatLng = baiduMap.getMapStatus().target;

        // 地理编码
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        // 地图状态监听
        baiduMap.setOnMapStatusChangeListener(this);

        // 列表初始化
        datas = new ArrayList();
        locatorAdapter = new LocationAdapter(this, datas);
        lv_location_position.setAdapter(locatorAdapter);

        // 初始化各种路线搜索类覆盖物,并设置监听
        busLineOverlay = new BusLineOverlay(baiduMap);
        bikingRouteOverlay = new BikingRouteOverlay(baiduMap);

        drivingRouteOverlay = new MyDrivingRouteOverlay(baiduMap);

        transitRouteOverlay = new TransitRouteOverlay(baiduMap);
        walkingRouteOverlay = new WalkingRouteOverlay(baiduMap);

        baiduMap.setOnMarkerClickListener(busLineOverlay);
        baiduMap.setOnMarkerClickListener(bikingRouteOverlay);

        baiduMap.setOnMarkerClickListener(drivingRouteOverlay);

        baiduMap.setOnMarkerClickListener(transitRouteOverlay);
        baiduMap.setOnMarkerClickListener(walkingRouteOverlay);

        // 控件注册监听
        lv_location_position.setOnItemClickListener(this);
        img_location_back_origin.setOnClickListener(this);
        floatingActionButton.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //img_location_back_origin.setImageResource(R.mipmap.back_origin_normal);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            //地点搜索后显示
            baiduMap.clear();
            locatorAdapter.setSelectItemIndex(0);
            Log.d(TAG, "location get");

            // 获取经纬度
            LatLng latLng = data.getParcelableExtra("LatLng");

            //添加marker
            BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.mipmap.icon_focus_marker);
            MarkerOptions markerOptions = new MarkerOptions()
                    .icon(desc)
                    .position(latLng)
                    .animateType(MarkerOptions.MarkerAnimateType.grow);
            baiduMap.addOverlay(markerOptions);

            // 实现动画跳转
            MapStatusUpdate u = MapStatusUpdateFactory
                    .newLatLng(latLng);
            baiduMap.animateMapStatus(u);
            mSearch.reverseGeoCode((new ReverseGeoCodeOption())
                    .location(latLng));
            nearby_relative_layout.setVisibility(View.VISIBLE);
        } else if(requestCode == REQUEST_NAV_CODE && resultCode == RESULT_OK) {
            //获取导航起始点和终点的经纬度
            isNav = true;
            latLng_start = data.getParcelableExtra("StartLaTLng");
            latLng_end = data.getParcelableExtra("EndLaTLng");
            fromPlanNode = PlanNode.withLocation(latLng_start);
            toPlanNode = PlanNode.withLocation(latLng_end);
            int nav_mode = data.getIntExtra("NavMode", 1);
            switch (nav_mode) {
                case 1:
                    //驾车路线
                    //常规路线规划
                    routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(fromPlanNode).to(toPlanNode));
/*
                    routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(fromPlanNode).to(toPlanNode)
                        .policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_DIS_FIRST));//距离优先
*/
/*
                    routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(fromPlanNode).to(toPlanNode)
                            .policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_FEE_FIRST));//收费优先
*/
/*
                    routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(fromPlanNode).to(toPlanNode)
                            .policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_TIME_FIRST));//时间优先
*/
/*
                    routePlanSearch.drivingSearch(new DrivingRoutePlanOption().from(fromPlanNode).to(toPlanNode)
                            .policy(DrivingRoutePlanOption.DrivingPolicy.ECAR_AVOID_JAM));//避免拥堵
*/
                    break;
                case 2:
                    break;
                case 3:
                    //步行路线
                    routePlanSearch.walkingSearch(new WalkingRoutePlanOption().from(fromPlanNode).to(toPlanNode));
                    break;
                case 4:
                    //骑行路线
                    routePlanSearch.bikingSearch(new BikingRoutePlanOption().from(fromPlanNode).to(toPlanNode));
                    break;
            }

            /*
            //添加marker
            BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.mipmap.icon_focus_marker);
            MarkerOptions markerOptions = new MarkerOptions().icon(desc).position(latLng_start);
            baiduMap.addOverlay(markerOptions);

            // 实现动画跳转
            MapStatusUpdate u = MapStatusUpdateFactory
                    .newLatLng(latLng_start);
            baiduMap.animateMapStatus(u);*/
        }
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

    private void navigateTo(BDLocation location) {
        //searchEditView.setText(isFirstLocate ? "true" : "false");
        mLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(isFirstLocate) {
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(mLocationLatLng);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);

            mSearch.reverseGeoCode((new ReverseGeoCodeOption())
                    .location(mLocationLatLng));
            /*
            ToastUtils.showLong(MainActivity.this, String.valueOf(mLoactionLatLng.latitude) + "\n" +
                    String.valueOf(mLoactionLatLng.longitude));*/

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
        //ToastUtils.showShort(MainActivity.this, mCenterPoint.x + "\n" + mCenterPoint.y);
    }

    //权限申请回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0) {
                    for(int result : grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Please confirm all the permissions", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "Unknown error!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_edit_view:
                //Toast.makeText(MainActivity.this, "Click Search TextView", Toast.LENGTH_SHORT).show();
                Intent searchPositionIntent = new Intent(this, SearchPositionActivity.class);
                //startActivity(searchPositionIntent);
                startActivityForResult(searchPositionIntent, REQUEST_CODE);
                break;

            /*点击无效
            case R.id.bmapView:
                Log.d("MainActivity", "Click baidu Map");
                Toast.makeText(MainActivity.this, "Click baidu Map", Toast.LENGTH_SHORT).show();
                break;*/

            case R.id.btn_user:
                //Toast.makeText(MainActivity.this, "Click button User", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(this, LoginActivity.class);
                if(BmobUser.getCurrentUser(MainActivity.this) != null){
                    // 允许用户使用应用
                    Intent accountIntent = new Intent(this, AccountActivity.class);
                    startActivity(accountIntent);
                }else{
                    //缓存用户对象为空时， 可打开用户登录界面…
                    ToastUtils.showShort(MainActivity.this, "Please login first!");
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }

                break;

            case R.id.btn_search_location://文字检索城市中的餐厅，银行等，返回标志物
                String city = "西安";
                String info = searchEditView.getText().toString();
                if(TextUtils.isEmpty(info)) {
                    ToastUtils.showShort(MainActivity.this, "Please input your searching information");
                } else {
                    LogUtils.i(TAG, "开始查询。。。" + city + "--" + info);
                    mPoiSearch.searchInCity(new PoiCitySearchOption().city(city).keyword(info));
                }
                break;

            case R.id.btn_traffic_map:
                if(FLAG_TRAFFIC_MAP == false) {
                    ToastUtils.showShort(MainActivity.this, "Open traffic map");
                    baiduMap.setTrafficEnabled(true);
                    FLAG_TRAFFIC_MAP = true;
                } else {
                    ToastUtils.showShort(MainActivity.this, "Close traffic map");
                    baiduMap.setTrafficEnabled(false);
                    FLAG_TRAFFIC_MAP = false;
                }
                break;

            case R.id.btn_heat_map:
                if(FLAG_HEAT_MAP == false) {
                    ToastUtils.showShort(MainActivity.this, "Open heat map");
                    baiduMap.setBaiduHeatMapEnabled(true);
                    FLAG_HEAT_MAP = true;
                } else {
                    ToastUtils.showShort(MainActivity.this, "Close heat map");
                    baiduMap.setBaiduHeatMapEnabled(false);
                    FLAG_HEAT_MAP = false;
                }
                break;

            case R.id.tv_add_eye:
                //ToastUtils.showShort(MainActivity.this, "添加电子眼");
                if(BmobUser.getCurrentUser(MainActivity.this) != null){
                    // 允许用户使用应用
                    Intent accountIntent = new Intent(this, AccountActivity.class);
                    startActivity(accountIntent);
                }else{
                    ToastUtils.showShort(MainActivity.this, "Please login first");
                    //缓存用户对象为空时， 可打开用户登录界面…
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                break;
/*
            case R.id.tv_compass_mode:
                mLocationMode = MyLocationConfiguration.LocationMode.COMPASS;
                break;
*/
            case R.id.img_location_back_origin://回到原点
                //mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
                if (mLocationLatLng != null) {
                    // 实现动画跳转
                    //img_location_back_origin.setImageResource(R.mipmap.back_origin_select);
                    MapStatusUpdate u = MapStatusUpdateFactory
                            .newLatLng(mLocationLatLng);
                    baiduMap.animateMapStatus(u);
                    mSearch.reverseGeoCode((new ReverseGeoCodeOption())
                            .location(mLocationLatLng));
                }
                break;

            case R.id.fab:
                //ToastUtils.showShort(MainActivity.this, "click route_fab!");
                Intent navIntent = new Intent(this, NavigateActivity.class);
                navIntent.putExtra("CurrentLatLng", mLocationLatLng);
                startActivityForResult(navIntent, REQUEST_NAV_CODE);

            default:
                break;
        }
    }

    @Override
    public void onGetPoiResult(final PoiResult poiResult) {
        LogUtils.i(TAG, "获得检索回调...");
        if(poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            ToastUtils.showShort(this, "Sorry, no result at all!");
            return;
        }
        if(poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
            baiduMap.clear();
            busLines.clear();//清空公交线路list
            //创建PoiOverlay
            List<PoiInfo> allPoi = poiResult.getAllPoi();
            if(allPoi == null || allPoi.size() == 0) return;
            for(int i=0; i<allPoi.size(); i++) {
                PoiInfo poiInfo = allPoi.get(i);
                Log.i(TAG, "类型： " + poiInfo.type);
                //如果是公交类型的路线，就把它的UID添加到集合中
                if(poiInfo.type == PoiInfo.POITYPE.BUS_LINE) {
                    busLines.add(poiInfo.uid);
                }
            }
            if(busLines.size() == 0) return;
            uidPostion = 0;
            ToastUtils.showLong(MainActivity.this, "发现" + busLines.size() + "条公交线路!!!");
            busLineSearch.searchBusLine(new BusLineSearchOption().city("西安").uid(busLines.get(uidPostion)));

            /*
            PoiOverlay overlay = new MyPoiOverlay(baiduMap) {
                @Override
                public boolean onPoiClick(int i) {
                    if(poiResult.getAllPoi() != null
                        && poiResult.getAllPoi().get(i) != null) {
                        ToastUtils.showLong(BMapManager.getContext(), poiResult.getAllPoi().get(i).name);
                    }
                    return true;
                }
            };
            //设置overlay可以处理标注点击事件
            baiduMap.setOnMarkerClickListener(overlay);
            //设置PoiOverlay数据
            LogUtils.i(TAG, "getTotalPageNum个数：" + poiResult.getTotalPageNum()
                    + "  getTotalPoiNum：" + poiResult.getTotalPoiNum());
            overlay.setData(poiResult);
            //添加PoiOverlay到地图中
            overlay.addToMap();
            overlay.zoomToSpan();*/
            return;
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

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
        //ToastUtils.showShort(MainActivity.this, "11111111111111");
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            return;
        }
        //ToastUtils.showLong(MainActivity.this, result.getAddress());
        // 获取反向地理编码结果
        PoiInfo mCurrentInfo = new PoiInfo();
        mCurrentInfo.address = result.getAddress();
        mCurrentInfo.location = result.getLocation();
        mCurrentInfo.name = result.getAddress();
        mLocationValue = result.getAddress();
        datas.clear();
        if(!TextUtils.isEmpty(mLocationValue)) {
            datas.add(mCurrentInfo);
        }
        if(result.getPoiList() != null && result.getPoiList().size() > 0) {
            datas.addAll(result.getPoiList());
        }
        locatorAdapter.notifyDataSetChanged();
        pb_location_load_bar.setVisibility(View.GONE);
    }

    /**
     * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
     *
     * @param status 地图状态改变开始时的地图状态
     */
    @Override
    public void onMapStatusChangeStart(MapStatus status) {
        //mCenterPoint = status.targetScreen;
    }

    /**
     * 地图状态变化中
     *
     * @param status 当前地图状态
     */
    @Override
    public void onMapStatusChange(MapStatus status) {
        /*
        if (isLongClick) {
            ToastUtils.showShort(MainActivity.this, "2222");
            datas.clear();
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(status.target));
            pb_location_load_bar.setVisibility(View.VISIBLE);
            lv_location_position.setSelection(0);
            locatorAdapter.setSelectItemIndex(0);
        }*/
    }

    /**
     * 地图状态改变结束
     *
     * @param status 地图状态改变结束后的地图状态
     */
    @Override
    public void onMapStatusChangeFinish(MapStatus status) {
        /*
        if(isLongClick) {
            if (mCenterPoint == null) {
                return;
            }
            // 获取当前 MapView 中心屏幕坐标对应的地理坐标
            LatLng currentLatLng = baiduMap.getProjection().fromScreenLocation(
                    mCenterPoint);
            // 发起反地理编码检索
            mSearch.reverseGeoCode((new ReverseGeoCodeOption())
                    .location(currentLatLng));
            lv_location_position.setSelection(0);
            locatorAdapter.setSelectItemIndex(0);
            pb_location_load_bar.setVisibility(View.VISIBLE);
        }*/
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //isTouch = false;
        //isLongClick = false;
        // 设置选中项下标，并刷新
        locatorAdapter.setSelectItemIndex(position);
        locatorAdapter.notifyDataSetChanged();

        baiduMap.clear();
        PoiInfo info = (PoiInfo) locatorAdapter.getItem(position);

        LatLng la = info.location;
        // 获取位置
        mLocationValue = info.name;
        ToastUtils.showLong(MainActivity.this, mLocationValue);

        //添加marker
        BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.mipmap.icon_focus_marker);
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(desc)
                .position(la)
                .animateType(MarkerOptions.MarkerAnimateType.grow);
        baiduMap.addOverlay(markerOptions);

        // 动画跳转
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(la);
        baiduMap.animateMapStatus(u);

        //searchPoi();
    }

    /*
    //地图触摸事件监听器
    BaiduMap.OnMapTouchListener touchListener = new BaiduMap.OnMapTouchListener() {
        @Override
        public void onTouch(MotionEvent event) {
            isTouch = true;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // 显示列表，查找附近的地点
                searchPoi();
                //img_location_back_origin.setImageResource(R.mipmap.back_origin_normal);
            }
        }
    };*/

    private void searchPoi() {
        if (mCenterPoint == null) {
            return;
        }
        // 获取当前 MapView 中心屏幕坐标对应的地理坐标
        LatLng currentLatLng = baiduMap.getProjection().fromScreenLocation(
                mCenterPoint);
        // 发起反地理编码检索
        mSearch.reverseGeoCode((new ReverseGeoCodeOption())
                .location(currentLatLng));
        lv_location_position.setSelection(0);
        locatorAdapter.setSelectItemIndex(0);
        pb_location_load_bar.setVisibility(View.VISIBLE);
    }

    private void searchBusLine() {
        LogUtils.i(TAG, "当前查询的路线==" + uidPostion);
        //busLineSearch.searchBusLine(new BusLineSearchOption().city(etStationCity.getText().toString()).uid(busLines.get(uidPostion)));
    }

    /**
     * 【2】从检索的结果中筛选出公交路线
     * 并将其添加到地图上
     * @param busLineResult
     */
    @Override
    public void onGetBusLineResult(BusLineResult busLineResult) {
        if (busLineResult == null || busLineResult.error != SearchResult.ERRORNO.NO_ERROR) {
            ToastUtils.showLong(this, "抱歉，未找到结果");
            return;
        }
        baiduMap.clear();
        busLineOverlay.removeFromMap();
        busLineOverlay.setData(busLineResult);
        busLineOverlay.addToMap();
        busLineOverlay.zoomToSpan();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            ToastUtils.showLong(this, "抱歉，未找到结果");
            return;
        }
        LogUtils.i(TAG, "线路条数：" + walkingRouteResult.getRouteLines().size());
        List<WalkingRouteLine> routeLines = walkingRouteResult.getRouteLines();
        baiduMap.clear();
        walkingRouteOverlay.removeFromMap();
        walkingRouteOverlay.setData(routeLines.get(0));
        walkingRouteOverlay.addToMap();
        walkingRouteOverlay.zoomToSpan();
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
        if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            ToastUtils.showLong(this, "抱歉，未找到结果");
            return;
        }
        LogUtils.i(TAG, "线路条数：" + transitRouteResult.getRouteLines().size());
        List<TransitRouteLine> routeLines = transitRouteResult.getRouteLines();
        baiduMap.clear();
        transitRouteOverlay.removeFromMap();
        transitRouteOverlay.setData(routeLines.get(0));
        transitRouteOverlay.addToMap();
        transitRouteOverlay.zoomToSpan();
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        if (drivingRouteResult == null || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            ToastUtils.showLong(this, "抱歉，未找到结果");
            return;
        }
        LogUtils.i(TAG, "线路条数：" + drivingRouteResult.getRouteLines().size());
        List<DrivingRouteLine> routeLines = drivingRouteResult.getRouteLines();
        baiduMap.clear();
        drivingRouteOverlays.clear();
        for(int i=0; i<drivingRouteResult.getRouteLines().size(); i++) {
            MyDrivingRouteOverlay myDrivingRouteOverlay = new MyDrivingRouteOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(myDrivingRouteOverlay);
            myDrivingRouteOverlay.removeFromMap();
            myDrivingRouteOverlay.setData(routeLines.get(i));
            myDrivingRouteOverlay.addToMap();
            myDrivingRouteOverlay.zoomToSpan();

            drivingRouteOverlays.add(myDrivingRouteOverlay);
            /*
            List<DrivingRouteLine.DrivingStep> steps = routeLines.get(i).getAllStep();
            LatLng nodeLocation; //每个路段起始坐标点
            nodeLocation = steps.get(0).getEntrance().getLocation();
            String nodeTitle; //每个路段的详细信息
            nodeTitle = steps.get(0).getInstructions();*/
        }
/*
        drivingRouteOverlay.removeFromMap();
        drivingRouteOverlay.setData(routeLines.get(0));
        drivingRouteOverlay.addToMap();
        drivingRouteOverlay.zoomToSpan();*/
    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        if (bikingRouteResult == null || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            ToastUtils.showLong(this, "抱歉，未找到结果");
            return;
        }
        LogUtils.i(TAG, "线路条数：" + bikingRouteResult.getRouteLines().size());
        List<BikingRouteLine> routeLines = bikingRouteResult.getRouteLines();
        baiduMap.clear();
        bikingRouteOverlay.removeFromMap();
        bikingRouteOverlay.setData(routeLines.get(0));
        bikingRouteOverlay.addToMap();
        bikingRouteOverlay.zoomToSpan();
    }

    //定位监听器
    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            /*
            final StringBuilder currentPosition = new StringBuilder();
            //Log.d("LocationListener","build position");
            currentPosition.append("Latitude(纬度): ").append(location.getLatitude())
                    .append("\n");
            //Log.d("LocationListener", String.valueOf(location.getLatitude()));
            currentPosition.append("Longitude(经度): ").append(location.getLongitude())
                    .append("\n");
            currentPosition.append("Country(国家): ").append(location.getCountry())
                    .append("\n");
            currentPosition.append("Province(省): ").append(location.getProvince())
                    .append("\n");
            currentPosition.append("City(市): ").append(location.getCity())
                    .append("\n");
            currentPosition.append("District(区): ").append(location.getDistrict())
                    .append("\n");
            currentPosition.append("Street(街道): ").append(location.getStreet())
                    .append("\n");
            currentPosition.append("定位方式: ");
            if(location.getLocType() == BDLocation.TypeGpsLocation) {
                Log.d("LocationListener", "GPS");
                currentPosition.append("GPS");
            } else if(location.getLocType() == BDLocation.TypeNetWorkLocation) {
                Log.d("LocationListener", "Network");
                currentPosition.append("NETWORK");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    positionText.setText(currentPosition);
                }
            });*/
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null) {
                return;
            }

            if(location.getLocType() == BDLocation.TypeGpsLocation ||
                    location.getLocType() == BDLocation.TypeNetWorkLocation) {
                //ToastUtils.showLong(MainActivity.this, "0000000000000000");
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
        mPoiSearch.destroy();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        /**
         * 构造函数
         *
         * @param baiduMap 该DrivingRouteOvelray引用的 BaiduMap
         */
        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public int getLineColor() {
            return super.getLineColor();
            //return Color.GREEN;
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            return super.getStartMarker();
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            return super.getTerminalMarker();
        }
    }
}
















