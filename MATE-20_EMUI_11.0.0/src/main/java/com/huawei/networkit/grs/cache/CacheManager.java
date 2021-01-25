package com.huawei.networkit.grs.cache;

import android.text.TextUtils;
import com.huawei.internal.telephony.ProxyControllerEx;
import com.huawei.networkit.grs.GrsApiManager;
import com.huawei.networkit.grs.GrsBaseInfo;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.requestremote.GrsResponse;
import com.huawei.networkit.grs.requestremote.RequestController;
import com.huawei.networkit.grs.utils.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {
    private static final long ADVANCE_MIL_TIME = 300000;
    private static final String SPKEY_UNION_SUFFIX = "time";
    private static final String TAG = CacheManager.class.getSimpleName();
    private static Map<String, Map<String, Map<String, String>>> cacheMap = new ConcurrentHashMap(16);
    private static Map<String, Long> cacheTime = new ConcurrentHashMap(16);

    public static void updateCacheFromServer(GrsBaseInfo grsBaseInfo, GrsResponse grsResponse) {
        if (grsResponse.getIsSuccess() == 2) {
            Logger.w(TAG, "update cache from server failed");
            return;
        }
        String spUrlKey = grsBaseInfo.getGrsParasKey(false, true);
        GrsPreferences.getInstance().putString(spUrlKey, grsResponse.getResult());
        GrsPreferences instance = GrsPreferences.getInstance();
        instance.putString(spUrlKey + SPKEY_UNION_SUFFIX, grsResponse.getCacheExpiretime());
        cacheMap.put(spUrlKey, GrsApiManager.getServicesUrlsMap(grsResponse.getResult()));
        cacheTime.put(spUrlKey, Long.valueOf(Long.parseLong(grsResponse.getCacheExpiretime())));
    }

    public static Map<String, String> getServiceUrls(GrsBaseInfo grsBaseInfo, String serviceName, CacheState cacheState) {
        Map<String, Map<String, String>> servicesMap = cacheMap.get(grsBaseInfo.getGrsParasKey(false, true));
        if (servicesMap == null || servicesMap.isEmpty()) {
            return new HashMap();
        }
        updateCache(grsBaseInfo, cacheState);
        return servicesMap.get(serviceName);
    }

    public static String getServiceUrl(GrsBaseInfo grsBaseInfo, String serviceName, String key, CacheState cacheState) {
        Map<String, String> serviceUrls = getServiceUrls(grsBaseInfo, serviceName, cacheState);
        if (serviceUrls == null) {
            return null;
        }
        return serviceUrls.get(key);
    }

    public static void initCache(GrsBaseInfo grsBaseInfo) {
        String spUrlKey = grsBaseInfo.getGrsParasKey(false, true);
        String spValue = GrsPreferences.getInstance().getString(spUrlKey, "");
        long time = 0;
        GrsPreferences instance = GrsPreferences.getInstance();
        String urlParamKey = instance.getString(spUrlKey + SPKEY_UNION_SUFFIX, ProxyControllerEx.MODEM_0);
        if (!TextUtils.isEmpty(urlParamKey) && urlParamKey.matches("\\d+")) {
            try {
                time = Long.parseLong(urlParamKey);
            } catch (NumberFormatException e) {
                Logger.w(TAG, "convert urlParamKey from String to Long catch NumberFormatException.", e);
                time = 0;
            }
        }
        cacheMap.put(spUrlKey, GrsApiManager.getServicesUrlsMap(spValue));
        cacheTime.put(spUrlKey, Long.valueOf(time));
        updateCache(grsBaseInfo, spUrlKey);
    }

    private static void updateCache(GrsBaseInfo grsBaseInfo, String spUrlKey) {
        String str = TAG;
        Map<String, Long> map = cacheTime;
        Logger.v(str, "cacheTime is{%s} and its size is{%s}", map, Integer.valueOf(map.size()));
        if (Time.isTimeWillExpire(cacheTime.get(spUrlKey), (long) ADVANCE_MIL_TIME)) {
            RequestController.getInstance().getAyncServicesUrls(grsBaseInfo, null);
        }
    }

    private static void updateCache(GrsBaseInfo grsBaseInfo, CacheState cacheState) {
        Long expireTime = cacheTime.get(grsBaseInfo.getGrsParasKey(false, true));
        if (Time.isTimeExpire(expireTime)) {
            cacheState.setCacheState(2);
            return;
        }
        if (Time.isTimeWillExpire(expireTime, (long) ADVANCE_MIL_TIME)) {
            RequestController.getInstance().getAyncServicesUrls(grsBaseInfo, null);
        }
        cacheState.setCacheState(1);
    }

    public static void forceExpire(GrsBaseInfo grsBaseInfo) {
        String spUrlKey = grsBaseInfo.getGrsParasKey(false, true);
        GrsPreferences instance = GrsPreferences.getInstance();
        instance.putString(spUrlKey + SPKEY_UNION_SUFFIX, ProxyControllerEx.MODEM_0);
        Map<String, Long> map = cacheTime;
        map.remove(spUrlKey + SPKEY_UNION_SUFFIX);
        cacheMap.remove(spUrlKey);
        RequestController.getInstance().removeCurrentRequest(spUrlKey);
    }
}
