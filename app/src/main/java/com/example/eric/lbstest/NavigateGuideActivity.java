package com.example.eric.lbstest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.BikingRouteOverlay;
import com.baidu.mapapi.overlayutil.BusLineOverlay;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteNode;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
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
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.eric.lbstest.base.BaseMapActivity;
import com.example.eric.lbstest.classes.ElectricEye;
import com.example.eric.lbstest.maplistener.MyOrientationListener;
import com.example.eric.lbstest.utils.LogUtils;
import com.example.eric.lbstest.utils.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.BmobUser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NavigateGuideActivity extends BaseMapActivity implements OnGetGeoCoderResultListener, BaiduMap.OnMapStatusChangeListener, View.OnClickListener, OnGetPoiSearchResultListener, OnGetBusLineSearchResultListener, OnGetRoutePlanResultListener {

    private static final String TAG = "NavigateGuideActivity";

    private Context mContext;

    //界面控件
    @BindView(R.id.btn_traffic_map)
    Button btn_traffic_map;

    @BindView(R.id.bmapView)
    MapView mapView; //地图控件

    @BindView(R.id.btn_back)
    Button btn_back;

    @BindView(R.id.tv_add_eye)
    TextView tv_add_eye;

    @BindView(R.id.img_location_back_origin)
    ImageView img_location_back_origin;

    @BindView(R.id.btn_begin_navigate_guide)
    Button btn_begin_navigate_guide;

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

    //获取的位置
    private String mLocationValue;

    //地理编码
    private GeoCoder mSearch;

    //模式变量
    private MyLocationConfiguration.LocationMode mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;

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

    //标志
    private static boolean isNavigating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate_guide);

        mContext = this;

        requestLocation();
        initViews();

        Intent intent = getIntent();
        latLng_start = intent.getParcelableExtra("StartLaTLng");
        latLng_end = intent.getParcelableExtra("EndLaTLng");
        fromPlanNode = PlanNode.withLocation(latLng_start);
        toPlanNode = PlanNode.withLocation(latLng_end);
        int nav_mode = intent.getIntExtra("NavMode", 1);
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
                point_scale.x = 160;
                point_scale.y = 1630;
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

        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 路线查询初始化，并注册监听
        busLineSearch = BusLineSearch.newInstance();
        busLineSearch.setOnGetBusLineSearchResultListener(this);

        // 路线规划初始化，并注册监听
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(this);

        // 地图状态监听
        baiduMap.setOnMapStatusChangeListener(this);

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

        //控件注册监听
        img_location_back_origin.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        btn_begin_navigate_guide.setOnClickListener(this);
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
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;

            case R.id.btn_traffic_map:
                if(FLAG_TRAFFIC_MAP == false) {
                    ToastUtils.showShort(mContext, "开启交通图");
                    baiduMap.setTrafficEnabled(true);
                    FLAG_TRAFFIC_MAP = true;
                } else {
                    ToastUtils.showShort(mContext, "关闭交通图");
                    baiduMap.setTrafficEnabled(false);
                    FLAG_TRAFFIC_MAP = false;
                }
                break;

            case R.id.tv_add_eye:
                //ToastUtils.showShort(MainActivity.this, "添加电子眼");
                if(BmobUser.getCurrentUser(mContext) != null){
                    // 允许用户使用应用
                    Intent accountIntent = new Intent(this, AccountActivity.class);
                    startActivity(accountIntent);
                }else{
                    ToastUtils.showShort(mContext, "请您登录后再进行相关操作");
                    //缓存用户对象为空时， 可打开用户登录界面…
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.img_location_back_origin://回到原点
                drivingRouteOverlays.get(0).zoomToSpan();
                break;

            case R.id.btn_begin_navigate_guide:
                if(!isNavigating) {
                    navigateBegin();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_begin_navigate_guide.setText("取消导航");
                        }
                    });
                } else {
                    navigateClose();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_begin_navigate_guide.setText("开始导航");
                        }
                    });
                }

                break;

            default:
                break;
        }
    }

    private void navigateBegin() {
        isNavigating = true;
        sendRequestForEyeMessage();
    }

    private void navigateClose() {
        isNavigating = false;
    }

    private void sendRequestForEyeMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://apis.haoservice.com/efficient/camera?lat="+mLocationLatLng.latitude+"&lon="+mLocationLatLng.longitude+"&r=1000&key=62fd2fddb7b145f4a612cbae3cf8b9d3")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseJSONWithJSONObject(responseData);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithJSONObject(String jsonData) {
        try {
            JSONObject mJsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = mJsonObject.getJSONArray("result");
            ToastUtils.showShort(mContext, "附近1000米内共发现摄像头(电子眼)：" + jsonArray.length() + "个。");
            for(int i=0; i<jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int type = jsonObject.getInt("Type");
                double longitude = jsonObject.getDouble("EndX");
                double latitude = jsonObject.getDouble("EndY");
                String address = jsonObject.getString("FormattedAddress");
                Log.i(TAG, "type is " + ElectricEye.getTypeName(type));
                Log.i(TAG, "longitude is " + longitude);
                Log.i(TAG, "latitude is " + latitude);
                Log.i(TAG, "address is " + address);

                BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.mipmap.icon_focus_marker);
                LatLng latLng = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions()
                        .icon(desc)
                        .position(latLng)
                        .animateType(MarkerOptions.MarkerAnimateType.grow);
                baiduMap.addOverlay(markerOptions);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        LogUtils.i(TAG, "获得检索回调...");
        if(poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            ToastUtils.showShort(mContext, "抱歉，未找到POI检索结果");
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
            ToastUtils.showLong(mContext, "发现" + busLines.size() + "条公交线路!!!");
            busLineSearch.searchBusLine(new BusLineSearchOption().city("西安").uid(busLines.get(uidPostion)));

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
        for(int i=0; i<1/*i<drivingRouteResult.getRouteLines().size()*/; i++) {
            MyDrivingRouteOverlay myDrivingRouteOverlay = new MyDrivingRouteOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(myDrivingRouteOverlay);
            myDrivingRouteOverlay.removeFromMap();
            myDrivingRouteOverlay.setData(routeLines.get(i));
            myDrivingRouteOverlay.addToMap();
            myDrivingRouteOverlay.zoomToSpan();

            drivingRouteOverlays.add(myDrivingRouteOverlay);
/*
            List<RouteNode> routeNodes = routeLines.get(i).getWayPoints();
            Log.i(TAG, "Title:" + routeNodes.get(i).getTitle());
            Log.i(TAG, "经度：" + routeNodes.get(i).getLocation().longitude + "\n"
                + "纬度：" + routeNodes.get(i).getLocation().latitude);
            Log.i(TAG, "DescribeContents:" + routeNodes.get(i).describeContents());
*/
            Log.i(TAG, "拥堵米数：" + routeLines.get(i).getCongestionDistance() +"米");
            Log.i(TAG, "路线途中的红绿灯个数：" + routeLines.get(i).getLightNum() +"个");

            List<DrivingRouteLine.DrivingStep> drivingSteps = routeLines.get(i).getAllStep();
            Log.i(TAG, "共有" + drivingSteps.size() + "段路");

            double total_distance = 0;
            for(DrivingRouteLine.DrivingStep step : drivingSteps) {
                Log.i(TAG, "该路段起点方向值：" + step.getDirection());

                Log.i(TAG, "起始点经度：" + step.getEntrance().getLocation().longitude
                        + "\n" + "起始点纬度：" + step.getEntrance().getLocation().latitude);
                Log.i(TAG, "起点指示信息：" + step.getEntranceInstructions());

                Log.i(TAG, "终点经度：" + step.getExit().getLocation().longitude
                        + "\n" + "终点纬度：" + step.getExit().getLocation().latitude);
                Log.i(TAG, "终点指示信息：" + step.getExitInstructions());

                Log.i(TAG, "路段总体指示信息：" + step.getInstructions());

                Log.i(TAG, "路段需要转弯数：" + step.getNumTurns());

                Log.i(TAG, "路段长度：" + step.getDistance() + "米");
                total_distance += step.getDistance();
            }
            Log.i(TAG, "路线总距离：" + total_distance/1000 + "公里");

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
        public boolean onPolylineClick(Polyline polyline) {
            return super.onPolylineClick(polyline);
        }

        @Override
        public boolean onRouteNodeClick(int i) {
            return super.onRouteNodeClick(i);
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
}
