package com.example.eric.lbstest.utils;

import com.example.eric.lbstest.BuildConfig;

/**
 * Created by RSJ on 2017/4/20.
 */

public class LogUtils {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void i(String tag, String msg) {
        if(DEBUG) {
            android.util.Log.i(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if(DEBUG) {
            android.util.Log.e(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if(DEBUG) {
            android.util.Log.d(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if(DEBUG) {
            android.util.Log.v(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if(DEBUG) {
            android.util.Log.w(tag, msg);
        }
    }
}
