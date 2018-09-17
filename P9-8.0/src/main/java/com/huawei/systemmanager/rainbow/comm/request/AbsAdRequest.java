package com.huawei.systemmanager.rainbow.comm.request;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.android.internal.util.Preconditions;
import com.huawei.systemmanager.rainbow.comm.request.ICommonRequest.RequestType;
import com.huawei.systemmanager.rainbow.comm.request.util.DeviceUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbsAdRequest extends AbsRequest {
    private static final int CONNECTED_TIME_OUT = 10000;
    private static final int MAX_RETRY_TIME = 1;
    protected static final String PARAM_APPS = "apps";
    private static final String PARAM_BUILDNUMBER = "buildNumber";
    protected static final String PARAM_CLIENTPACKAGE = "clientPackage";
    private static final String PARAM_EMUIVER = "emuiVer";
    private static final String PARAM_FIRMWAREVERSION = "firmwareVersion";
    private static final String PARAM_LOCALE = "locale";
    protected static final String PARAM_METHOD = "method";
    private static final String PARAM_PHONETYPE = "phoneType";
    private static final String PARAM_RESOLUTION = "resolution";
    private static final String PARAM_SYSBITS = "sysBits";
    private static final String PARAM_TS = "ts";
    protected static final String PARAM_URL = "url";
    private static final String PARAM_VERSION = "version";
    private static final int READ_TIME_OUT = 10000;
    private static final int SYSTEM_32 = 1;
    private static final int SYSTEM_64 = 2;
    private static final String TAG = "AdBlock_AbsAdRequest";
    private static final String UNKNOWN = "unknown";
    private int mConnectTimeout = 10000;
    private int mReadTimeout = 10000;

    protected abstract void addExtPostRequestParam(Context context, Map<String, String> map);

    protected abstract int checkResponseCode(Context context, int i);

    protected abstract String getRequestUrl(RequestType requestType);

    protected abstract void parseResponseAndPost(Context context, JSONObject jSONObject) throws JSONException;

    protected void setTimeout(int connectTimeout, int readTimeout) {
        this.mConnectTimeout = connectTimeout;
        this.mReadTimeout = readTimeout;
    }

    protected void doRequest(Context ctx) {
        int retryCount = 0;
        Log.d(TAG, "doRequest");
        while (retryCount < 1) {
            if (!innerConnectRequest(ctx)) {
                retryCount++;
            } else {
                return;
            }
        }
        setRequestFailed();
    }

    private boolean innerConnectRequest(Context context) {
        try {
            String response;
            ICommonRequest conn = JointFactory.getGzipRequest();
            Preconditions.checkNotNull(conn);
            conn.setTimeout(this.mConnectTimeout, this.mReadTimeout);
            RequestType type = getRequestType();
            String url = getRequestUrl(type);
            Preconditions.checkNotNull(url);
            preProcess();
            if (RequestType.REQUEST_GET == type) {
                response = conn.doGetRequest(url);
            } else if (RequestType.REQUEST_POST == type) {
                response = conn.doPostRequest(url, getPostRequestParam(context), getZipEncodingStatus(), context);
            } else {
                response = "";
                Log.w(TAG, "unreachable code");
            }
            return processResponse(context, response);
        } catch (RuntimeException ex) {
            Log.e(TAG, "innerRequest catch RuntimeException: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        } catch (Exception ex2) {
            Log.e(TAG, "innerRequest catch Exception: " + ex2.getMessage());
            ex2.printStackTrace();
            return false;
        }
    }

    private String getPostRequestParam(Context context) {
        try {
            Preconditions.checkNotNull(context, "input context is null");
            Map<String, String> map = new HashMap();
            if (isNeedDefaultParam()) {
                map.put(PARAM_FIRMWAREVERSION, VERSION.RELEASE);
                map.put(PARAM_LOCALE, Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry());
                map.put(PARAM_EMUIVER, DeviceUtil.getTelephoneEMUIVersion());
                map.put(PARAM_BUILDNUMBER, Build.DISPLAY);
                map.put(PARAM_PHONETYPE, Build.MODEL);
                map.put("version", getSelfVersion(context));
                map.put(PARAM_SYSBITS, String.valueOf(getSysteBit()));
                map.put(PARAM_TS, String.valueOf(System.currentTimeMillis()));
                map.put(PARAM_RESOLUTION, getResolution(context));
                map.put(PARAM_CLIENTPACKAGE, context.getPackageName());
            }
            addExtPostRequestParam(context, map);
            String param = convertParams(map);
            Log.d(TAG, "getPostRequestParam param: " + param);
            return param;
        } catch (NullPointerException ex) {
            Log.e(TAG, "getPostRequestParam catch NullPointerException: " + ex.getMessage());
            throw new RuntimeException("Failed to prepair JSON request parameters in [getPostRequestParam].");
        } catch (Exception ex2) {
            Log.e(TAG, "getPostRequestParam catch Exception: " + ex2.getMessage());
            throw new RuntimeException("Failed to prepair JSON request parameters in [getPostRequestParam].");
        }
    }

    private String getSelfVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 786432).versionName;
        } catch (NameNotFoundException e) {
            Log.w(TAG, "getSelfVersion catch Exception:", e);
            return UNKNOWN;
        }
    }

    private String getResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService("window");
        if (wm == null) {
            return UNKNOWN;
        }
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getRealSize(point);
        return point.x + "x" + point.y;
    }

    private static int getSysteBit() {
        if (SystemProperties.get("ro.product.cpu.abi", "").contains("arm64")) {
            return 2;
        }
        return 1;
    }

    public static String convertParams(Map<String, String> map) {
        StringBuffer params = new StringBuffer();
        ArrayList<String> keys = new ArrayList(map.keySet());
        Collections.sort(keys);
        int i = 0;
        while (i < keys.size()) {
            String key = (String) keys.get(i);
            String value = (String) map.get(key);
            if (value != null) {
                params.append((i == 0 ? "" : "&") + key + "=" + urlEncoderParams(value));
            }
            i++;
        }
        return params.toString();
    }

    public static String urlEncoderParams(String params) {
        if (params == null) {
            return null;
        }
        try {
            return URLEncoder.encode(params, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "urlEncoderParams catch Exception:", e);
            return null;
        }
    }

    protected boolean processResponse(Context ctx, String response) {
        try {
            Preconditions.checkNotNull(response, "processResponse input response should not be empty!");
            if (TextUtils.isEmpty(response)) {
                Log.w(TAG, "processResponse response is empty");
                return false;
            }
            JSONObject jsonResponse = new JSONObject(response);
            int nCheckResult = checkResponseCode(ctx, jsonResponse.getInt(getResultCodeFiled()));
            switch (nCheckResult) {
                case 0:
                    parseResponseAndPost(ctx, jsonResponse);
                    return true;
                case 1:
                    parseResponseAndPost(ctx, jsonResponse);
                    return false;
                case 2:
                    return true;
                case 3:
                    return false;
                default:
                    Log.w(TAG, "processResponse: Invalid response code check result = " + nCheckResult);
                    return false;
            }
        } catch (JSONException ex) {
            Log.e(TAG, "processResponse catch JSONException:", ex);
            return false;
        } catch (Exception ex2) {
            Log.e(TAG, "processResponse catch Exception", ex2);
            return false;
        }
    }

    protected String getResultCodeFiled() {
        return "rtnCode";
    }

    protected void preProcess() {
    }

    protected RequestType getRequestType() {
        return RequestType.REQUEST_POST;
    }

    protected boolean getZipEncodingStatus() {
        return true;
    }
}
