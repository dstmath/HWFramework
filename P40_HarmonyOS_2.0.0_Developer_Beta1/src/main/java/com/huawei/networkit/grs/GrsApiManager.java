package com.huawei.networkit.grs;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.networkit.grs.cache.CacheManager;
import com.huawei.networkit.grs.cache.CacheState;
import com.huawei.networkit.grs.common.Logger;
import com.huawei.networkit.grs.local.LocalManagerV1;
import com.huawei.networkit.grs.local.LocalManagerV2;
import com.huawei.networkit.grs.local.model.CountryCodeBean;
import com.huawei.networkit.grs.requestremote.GrsResponse;
import com.huawei.networkit.grs.requestremote.RequestController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class GrsApiManager {
    private static final String TAG = GrsApiManager.class.getSimpleName();
    private GrsBaseInfo grsBaseInfo;

    public GrsApiManager(GrsBaseInfo grsBaseInfo2) {
        this.grsBaseInfo = grsBaseInfo2;
    }

    public String synGetGrsUrl(String serviceName, String key) {
        CacheState cacheState = new CacheState();
        String localUrl = getUrlLocal(serviceName, key, cacheState);
        if (cacheState.isUnexpired()) {
            Logger.v(TAG, "get unexpired cache localUrl{%s}", localUrl);
            return localUrl;
        }
        String remoteUrl = getServiceNameUrl(synGetUrlsFromServer(), serviceName, key);
        if (TextUtils.isEmpty(remoteUrl)) {
            return localUrl;
        }
        Logger.v(TAG, "get from remote server's remoteUrl {%s}", remoteUrl);
        return remoteUrl;
    }

    public Map<String, String> synGetGrsUrls(String serviceName) {
        Map<String, String> remoteUrls;
        CacheState cacheState = new CacheState();
        Map<String, String> localUrls = getUrlsLocal(serviceName, cacheState);
        if (!cacheState.isUnexpired() && (remoteUrls = getServiceNameUrls(synGetUrlsFromServer(), serviceName)) != null && !remoteUrls.isEmpty()) {
            return remoteUrls;
        }
        return localUrls;
    }

    private Map<String, String> getUrlsLocal(String serviceName, CacheState cacheState) {
        Map<String, String> result = CacheManager.getServiceUrls(this.grsBaseInfo, serviceName, cacheState);
        if (result != null && !result.isEmpty()) {
            return result;
        }
        Map<String, String> result2 = LocalManagerV2.getLocalManagerV2().getServicesUrlsFromLocal(this.grsBaseInfo, serviceName);
        if (result2 != null && !result2.isEmpty()) {
            return result2;
        }
        Map<String, String> result3 = LocalManagerV1.getLocalManager().getServicesUrlsFromLocal(this.grsBaseInfo, serviceName);
        if (result3 == null || result3.isEmpty()) {
            return null;
        }
        return result3;
    }

    private String getUrlLocal(String serviceName, String key, CacheState cacheState) {
        String result = CacheManager.getServiceUrl(this.grsBaseInfo, serviceName, key, cacheState);
        if (!TextUtils.isEmpty(result)) {
            return result;
        }
        String result2 = LocalManagerV2.getLocalManagerV2().getUrlFromLocal(this.grsBaseInfo, serviceName, key);
        if (!TextUtils.isEmpty(result2)) {
            return result2;
        }
        String result3 = LocalManagerV1.getLocalManager().getUrlFromLocal(this.grsBaseInfo, serviceName, key);
        if (!TextUtils.isEmpty(result3)) {
            return result3;
        }
        return null;
    }

    public void ayncGetGrsUrl(final String serviceName, final String key, final IQueryUrlCallBack callBack) {
        CacheState cacheState = new CacheState();
        final String localUrl = getUrlLocal(serviceName, key, cacheState);
        if (!cacheState.isUnexpired()) {
            RequestController.getInstance().getAyncServicesUrls(this.grsBaseInfo, new GrsCallBack() {
                /* class com.huawei.networkit.grs.GrsApiManager.AnonymousClass1 */

                @Override // com.huawei.networkit.grs.GrsCallBack
                public void onResponse(GrsResponse grsResponse) {
                    String url = GrsApiManager.getServiceNameUrl(grsResponse.getResult(), serviceName, key);
                    if (!TextUtils.isEmpty(url)) {
                        callBack.onCallBackSuccess(url);
                    } else if (!TextUtils.isEmpty(localUrl)) {
                        callBack.onCallBackSuccess(localUrl);
                    } else {
                        callBack.onCallBackFail(-5);
                    }
                }

                @Override // com.huawei.networkit.grs.GrsCallBack
                public void onFailure() {
                    if (!TextUtils.isEmpty(localUrl)) {
                        callBack.onCallBackSuccess(localUrl);
                    } else {
                        callBack.onCallBackFail(-3);
                    }
                }
            });
        } else if (TextUtils.isEmpty(localUrl)) {
            callBack.onCallBackFail(-5);
        } else {
            callBack.onCallBackSuccess(localUrl);
        }
    }

    public void ayncGetGrsUrls(String serviceName, IQueryUrlsCallBack callBack) {
        CacheState cacheState = new CacheState();
        Map<String, String> localUrls = getUrlsLocal(serviceName, cacheState);
        if (!cacheState.isUnexpired()) {
            ayncGetUrlsFromServer(serviceName, localUrls, callBack);
        } else if (localUrls == null || localUrls.isEmpty()) {
            callBack.onCallBackFail(-5);
        } else {
            callBack.onCallBackSuccess(localUrls);
        }
    }

    private void ayncGetUrlsFromServer(final String serviceName, final Map<String, String> localUrls, final IQueryUrlsCallBack callBack) {
        RequestController.getInstance().getAyncServicesUrls(this.grsBaseInfo, new GrsCallBack() {
            /* class com.huawei.networkit.grs.GrsApiManager.AnonymousClass2 */

            @Override // com.huawei.networkit.grs.GrsCallBack
            public void onResponse(GrsResponse grsResponse) {
                Map<String, String> urls = GrsApiManager.getServiceNameUrls(grsResponse.getResult(), serviceName);
                if (urls == null || urls.isEmpty()) {
                    Map map = localUrls;
                    if (map == null || map.isEmpty()) {
                        callBack.onCallBackFail(-5);
                    } else {
                        callBack.onCallBackSuccess(localUrls);
                    }
                } else {
                    callBack.onCallBackSuccess(urls);
                }
            }

            @Override // com.huawei.networkit.grs.GrsCallBack
            public void onFailure() {
                Map map = localUrls;
                if (map == null || map.isEmpty()) {
                    callBack.onCallBackFail(-3);
                } else {
                    callBack.onCallBackSuccess(localUrls);
                }
            }
        });
    }

    public String synGetUrlsFromServer() {
        GrsResponse response = RequestController.getInstance().getSyncServicesUrls(this.grsBaseInfo);
        return response == null ? "" : response.getResult();
    }

    public static Map<String, Map<String, String>> getServicesUrlsMap(String spValue) {
        Map<String, Map<String, String>> cacheMap = new HashMap<>(16);
        if (TextUtils.isEmpty(spValue)) {
            Logger.v(TAG, "isSpExpire jsonValue is null.");
            return cacheMap;
        }
        try {
            JSONObject jsonObject = new JSONObject(spValue);
            Iterator ite = jsonObject.keys();
            while (ite.hasNext()) {
                String key = ite.next().toString();
                cacheMap.put(key, getServiceUrls(jsonObject.getJSONObject(key)));
            }
            return cacheMap;
        } catch (JSONException e) {
            Logger.w(TAG, "getServicesUrlsMap occur a JSONException", e);
            return cacheMap;
        }
    }

    public static Map<String, String> getServiceUrls(JSONObject urls) {
        Map<String, String> urlsMap = new HashMap<>(16);
        try {
            Iterator ite = urls.keys();
            while (ite.hasNext()) {
                String key = ite.next().toString();
                urlsMap.put(key, urls.get(key).toString());
            }
            return urlsMap;
        } catch (JSONException e) {
            Logger.w(TAG, "getServiceUrls occur a JSONException", e);
            return urlsMap;
        }
    }

    public static Map<String, String> getServiceNameUrls(String spValue, String serviceName) {
        Map<String, String> urlsMap = new HashMap<>();
        if (TextUtils.isEmpty(spValue)) {
            Logger.v(TAG, "isSpExpire jsonValue is null.");
            return urlsMap;
        }
        try {
            JSONObject jsObject = new JSONObject(spValue).getJSONObject(serviceName);
            if (jsObject == null) {
                Logger.v(TAG, "getServiceNameUrls jsObject null.");
                return urlsMap;
            }
            Iterator ite = jsObject.keys();
            while (ite.hasNext()) {
                String key = ite.next().toString();
                urlsMap.put(key, jsObject.get(key).toString());
            }
            return urlsMap;
        } catch (JSONException e) {
            Logger.w(TAG, "Method{getServiceNameUrls} query url from SP occur an JSONException", e);
            return urlsMap;
        }
    }

    public static String getServiceNameUrl(String spValue, String serviceName, String key) {
        if (TextUtils.isEmpty(spValue)) {
            return "";
        }
        try {
            return new JSONObject(spValue).getJSONObject(serviceName).getString(key);
        } catch (JSONException e) {
            Logger.w(TAG, "Method{getServiceNameUrl} query url from SP occur an JSONException", e);
            return "";
        }
    }

    public static CountryCodeBean getCountryCode(Context context, boolean enableNetwork) {
        return new CountryCodeBean(context, enableNetwork);
    }
}
