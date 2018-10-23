package com.example.eric.lbstest;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ZoomControls;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.eric.lbstest.base.BaseMapActivity;
import com.example.eric.lbstest.utils.ToastUtils;

import butterknife.BindView;

/**
 * 电子眼位置在地图上的位置查看
 */

public class EyeLocationViewActivity extends BaseMapActivity implements View.OnClickListener {

    private static final String TAG = "EyeLocationViewActivity";

    @BindView(R.id.bmapView)
    MapView mapView; //地图控件

    @BindView(R.id.img_location_back_origin)
    ImageView img_location_back_origin;

    @BindView(R.id.btn_back)
    Button btn_back;

    BaiduMap baiduMap = null;

    // MapView 中央对于的屏幕坐标
    private android.graphics.Point mCenterPoint = null;

    //电子眼经纬度信息
    double latitude = 0.0;

    double longitude = 0.0;

    LatLng eyeLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eye_location_view);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("EyeLat", 0.0);
        longitude = intent.getDoubleExtra("EyeLong", 0.0);
        //ToastUtils.showShort(EyeLocationViewActivity.this,
          //      "经度: " + String.valueOf(longitude) + "\n" + "纬度: " + String.valueOf(latitude));
        eyeLatLng = new LatLng(latitude, longitude);

        initView();
        locateEye();
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
                point_scale.y = 1630;
                mapView.setScaleControlPosition(point_scale);

                Point point_zoom = new Point();
                point_zoom.x = 930;
                point_zoom.y = 800;
                mapView.setZoomControlsPosition(point_zoom);
            }
        });

        img_location_back_origin.setOnClickListener(this);
        btn_back.setOnClickListener(this);
    }

    private void locateEye() {
        BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.mipmap.icon_focus_marker);
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(desc)
                .position(eyeLatLng)
                .animateType(MarkerOptions.MarkerAnimateType.grow);
        baiduMap.addOverlay(markerOptions);

        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(eyeLatLng);
        baiduMap.animateMapStatus(update);
        update = MapStatusUpdateFactory.zoomTo(16f);
        baiduMap.animateMapStatus(update);
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
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_location_back_origin:
                if (eyeLatLng != null) {
                    // 实现动画跳转
                    MapStatusUpdate u = MapStatusUpdateFactory
                            .newLatLng(eyeLatLng);
                    baiduMap.animateMapStatus(u);
                }
                break;

            case R.id.btn_back:
                finish();
                break;

            default:
                break;
        }
    }
}
