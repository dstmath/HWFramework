package com.android.server.hidata.histream;

import android.util.Log;

class HwHiStreamUtils {
    public static final int DAY_TIME = 86400000;
    public static final int DEFAULT_NETWORK_STRATEGY = 0;
    public static final int DEFAULT_SOCKET_STRATEGY = 3;
    public static final String DOUYIN_NAME = "DOUYIN";
    public static final int DOUYIN_NETWORK_STRATEGY = 0;
    public static final int DOUYIN_SOCKET_STRATEGY = 3;
    public static final int HOME_AP = 0;
    public static final int INVALID_VALUE = -1;
    public static final int NETWORK_CELLULAR = 801;
    public static final int NETWORK_UNKNOW = -1;
    public static final int NETWORK_WIFI = 800;
    public static final int PUBLIC_AP = 1;
    public static final int SCENE_DOUYIN = 3;
    public static final int SCENE_UNKNOWN = -1;
    public static final int SCENE_WECHAT_AUDIO_CALL = 1;
    public static final int SCENE_WECHAT_VIDEO_CALL = 2;
    public static final String TAG = "HiDATA_HiStream";
    public static final String WECHAT_NAME = "WECHAT";
    public static final int WEEK_DAYS = 7;

    HwHiStreamUtils() {
    }

    public static void logD(String info) {
        Log.d(TAG, info);
    }

    public static void logW(String info) {
        Log.w(TAG, info);
    }

    public static void logE(String info) {
        Log.e(TAG, info);
    }
}
