package com.example.eric.lbstest;

import android.os.Bundle;

import com.example.eric.lbstest.base.BaseToolbarMapActivity;

/**
 * 公交路线地图
 * 1.首先从城市检索中查询出公交路线类型的PoiInfo
 * 2.更具PoiInfo的类型筛选出是公交路线的检索，并添加到集合中
 * 3.用BusLineSearch分别查询，集合中数据
 */
public class BusLineMapActivity extends BaseToolbarMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_line_map);
    }
}
