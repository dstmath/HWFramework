package com.huawei.networkit.grs;

import android.content.Context;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.local.model.CountryCodeBean;
import java.util.HashMap;
import java.util.Map;

public class GrsApi {
    private static final String TAG = "GrsApi";
    private static GrsClient grsClient;

    public static int grsSdkInit(Context context, GrsBaseInfo grsBaseInfoParam) {
        grsClient = new GrsClient(context, grsBaseInfoParam);
        return 0;
    }

    public static String synGetGrsUrl(String serviceName, String key) {
        GrsClient grsClient2 = grsClient;
        if (grsClient2 != null && serviceName != null && key != null) {
            return grsClient2.synGetGrsUrl(serviceName, key);
        }
        Logger.w(TAG, "GrsApi.synGetGrsUrl method maybe grsSdkInit has not completed and grsClient is null.");
        return null;
    }

    public static Map<String, String> synGetGrsUrls(String serviceName) {
        GrsClient grsClient2 = grsClient;
        if (grsClient2 != null && serviceName != null) {
            return grsClient2.synGetGrsUrls(serviceName);
        }
        Logger.w(TAG, "GrsApi.synGetGrsUrls method maybe grsSdkInit has not completed and grsClient is null.");
        return new HashMap();
    }

    public static void ayncGetGrsUrl(String serviceName, String key, IQueryUrlCallBack callBack) {
        if (callBack == null) {
            Logger.w(TAG, "IQueryUrlCallBack is must not null for process continue.");
            return;
        }
        GrsClient grsClient2 = grsClient;
        if (grsClient2 == null || serviceName == null || key == null) {
            callBack.onCallBackFail(-6);
        } else {
            grsClient2.ayncGetGrsUrl(serviceName, key, callBack);
        }
    }

    public static void ayncGetGrsUrls(String serviceName, IQueryUrlsCallBack callBack) {
        if (callBack == null) {
            Logger.w(TAG, "IQueryUrlsCallBack is must not null for process continue.");
            return;
        }
        GrsClient grsClient2 = grsClient;
        if (grsClient2 == null || serviceName == null) {
            callBack.onCallBackFail(-6);
        } else {
            grsClient2.ayncGetGrsUrls(serviceName, callBack);
        }
    }

    public static CountryCodeBean getCountryCode(Context context, boolean enableNetwork) {
        return GrsApiManager.getCountryCode(context, enableNetwork);
    }

    public static boolean forceExpire() {
        GrsClient grsClient2 = grsClient;
        if (grsClient2 != null) {
            return grsClient2.forceExpire();
        }
        Logger.w(TAG, "GrsApi.forceExpire return false because grsClient is null.");
        return false;
    }
}
