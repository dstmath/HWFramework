package com.huawei.hwwifiproservice.wifipro.networkrecommend;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class NetworkRecommendManager {
    private static final String TAG = "WiFi_PRONetworkRecommendManager";
    private static NetworkRecommendManager sInstance = null;

    private NetworkRecommendManager() {
    }

    public void init(Handler handler, Context context) {
        HumanFactorRecommend.getInstance().init(handler, context);
    }

    public static NetworkRecommendManager getInstance() {
        if (sInstance == null) {
            sInstance = new NetworkRecommendManager();
        }
        return sInstance;
    }

    public boolean isRecommendWiFi2Cell() {
        return HumanFactorRecommend.getInstance().isRecommendWiFi2Cell();
    }

    public boolean isRecommendCell2WiFi() {
        return HumanFactorRecommend.getInstance().isRecommendCell2WiFi();
    }

    public boolean isRecommendShowWifiToCellToast() {
        return HumanFactorRecommend.getInstance().isRecommendShowWifiToCellToast();
    }

    public void registerBoosterService() {
        HumanFactorRecommend.getInstance().registerBoosterService();
    }

    private void logI(String info) {
        Log.i(TAG, info);
    }

    private void logD(String info) {
        Log.d(TAG, info);
    }

    private void logE(String info) {
        Log.e(TAG, info);
    }
}
