package com.android.server.wifi.grs;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.grs.requestremote.GrsResponse;
import com.android.server.wifi.grs.requestremote.RequestController;
import com.android.server.wifi.grs.utils.ContextUtil;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class GrsApiManager {
    private static final String CAPTIVE_PORTAL_EXPIRED_TIME = "captive_portal_expired_time";
    private static final String CAPTIVE_PORTAL_URL_HTTPS_BACKUP = "captive_portal_probe_url_https_backup";
    private static final String CAPTIVE_PORTAL_URL_HTTPS_MAIN = "captive_portal_probe_url_https_main";
    private static final String CAPTIVE_PORTAL_URL_HTTP_BACKUP = "captive_portal_probe_url_http_backup";
    private static final String CAPTIVE_PORTAL_URL_HTTP_MAIN = "captive_portal_probe_url_http_main";
    private static final int DEFAULT_VALUE = 0;
    private static final String KEYWORD_HTTPS_BACKUP = "httpsBackup";
    private static final String KEYWORD_HTTPS_MAIN = "httpsMain";
    private static final String KEYWORD_HTTP_BACKUP = "httpBackup";
    private static final String KEYWORD_HTTP_MAIN = "httpMain";
    private static final int ONE_DAY = 86400000;
    private static final String SERVICE_NAME = "com.huawei.cloud.networkProbe";
    private static final String TAG = GrsApiManager.class.getSimpleName();

    public GrsApiManager(Context context) {
        if (context != null) {
            ContextUtil.setContext(context);
            return;
        }
        throw new NullPointerException("invalid init params for context is null.");
    }

    public void ayncGetGrsUrls() {
        if (System.currentTimeMillis() < Long.valueOf(Settings.Global.getLong(ContextUtil.getContext().getContentResolver(), CAPTIVE_PORTAL_EXPIRED_TIME, 0)).longValue()) {
            Log.d(TAG, "the values in global settings are not expired, return");
        } else {
            ayncGetUrlsFromServer(SERVICE_NAME);
        }
    }

    private void ayncGetUrlsFromServer(final String serviceName) {
        RequestController.getInstance().getAsyncServicesUrls(new GrsCallBack() {
            /* class com.android.server.wifi.grs.GrsApiManager.AnonymousClass1 */

            @Override // com.android.server.wifi.grs.GrsCallBack
            public void onResponse(GrsResponse grsResponse) {
                GrsApiManager.this.getServiceNameUrls(grsResponse.getResult(), serviceName);
            }

            @Override // com.android.server.wifi.grs.GrsCallBack
            public void onFailure() {
                Log.d(GrsApiManager.TAG, "the request is failure");
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getServiceNameUrls(String spValue, String serviceName) {
        if (TextUtils.isEmpty(spValue)) {
            Log.d(TAG, "getServiceNameUrls spValue is null.");
            return;
        }
        try {
            JSONObject jsObject = new JSONObject(spValue).getJSONObject(serviceName);
            if (jsObject == null) {
                Log.d(TAG, "getServiceNameUrls jsObject null.");
                return;
            }
            Iterator ite = jsObject.keys();
            while (ite.hasNext()) {
                String key = ite.next().toString();
                String value = jsObject.get(key).toString();
                String str = TAG;
                Log.d(str, "grs result, key = " + key + ", value = " + value);
                setValue(key, value);
            }
            Settings.Global.putLong(ContextUtil.getContext().getContentResolver(), CAPTIVE_PORTAL_EXPIRED_TIME, System.currentTimeMillis() + 86400000);
        } catch (JSONException e) {
            Log.d(TAG, "an JSONException is occurred");
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void setValue(String key, String value) {
        char c;
        switch (key.hashCode()) {
            case -133300639:
                if (key.equals(KEYWORD_HTTP_MAIN)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 155700877:
                if (key.equals(KEYWORD_HTTPS_BACKUP)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 197137732:
                if (key.equals(KEYWORD_HTTPS_MAIN)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 432006250:
                if (key.equals(KEYWORD_HTTP_BACKUP)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0) {
            Settings.Global.putString(ContextUtil.getContext().getContentResolver(), CAPTIVE_PORTAL_URL_HTTP_MAIN, value);
        } else if (c == 1) {
            Settings.Global.putString(ContextUtil.getContext().getContentResolver(), CAPTIVE_PORTAL_URL_HTTPS_MAIN, value);
        } else if (c == 2) {
            Settings.Global.putString(ContextUtil.getContext().getContentResolver(), CAPTIVE_PORTAL_URL_HTTP_BACKUP, value);
        } else if (c != 3) {
            Log.d(TAG, "unexpected key value!");
        } else {
            Settings.Global.putString(ContextUtil.getContext().getContentResolver(), CAPTIVE_PORTAL_URL_HTTPS_BACKUP, value);
        }
    }
}
