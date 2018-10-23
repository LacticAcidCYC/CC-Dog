package com.example.eric.lbstest.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.baidu.mapapi.SDKInitializer;
import com.example.eric.lbstest.utils.ActivityCollector;

import butterknife.ButterKnife;

/**
 * Created by RSJ on 2017/4/19.
 * 地图基础类
 * 使用条件：
 * 1. 需要加入百度地图的MapView
 */

public class BaseMapActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//禁止横屏
        //避免横屏后数据丢失造成bug
        ActivityCollector.addActivity(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        //注意该方法要在setContentView之前执行
        SDKInitializer.initialize(getApplicationContext());//使用百度地图必须的初始化
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
