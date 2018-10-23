package com.example.eric.lbstest.maputils;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.overlayutil.PoiOverlay;

/**
 * Created by APLEE on 2017/4/20.
 */

public class MyPoiOverlay extends PoiOverlay {
    /**
     * 构造函数
     *
     * @param baiduMap 该 PoiOverlay 引用的 BaiduMap 对象
     */
    public MyPoiOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }
}
