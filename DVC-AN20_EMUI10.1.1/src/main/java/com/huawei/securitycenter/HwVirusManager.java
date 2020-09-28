package com.huawei.securitycenter;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.securitycenter.IHwSecService;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwVirusManager {
    private static final int APPUID_ERROR = 803;
    private static final String GET_CURRENT_WIFI_THREAT_STATUS = "getCurrentWifiThreatStatus";
    private static final String GET_VIRUS_APP_LIST = "getVirusAppList";
    private static final int HMS_DISCONNECTED = 801;
    private static final int INITIAL_ARRAY_CAPACITY = 16;
    private static final int INVALID_PARA = 802;
    private static final int PULL_FROM_HMS_TIMEOUT = 800;
    private static final String SEC_KIT_BD_PARA_RES = "result";
    private static final String SERVICE_NAME = "com.huawei.securitycenter.mainservice.HwSecService";
    private static final int SUCCESS = 0;
    private static final String TAG = "HwVirusManager";
    private static final int WIFI_STATUS_CURRENT_SSID_IS_NOT_SECURE = 2;
    private static final int WIFI_STATUS_CURRENT_SSID_IS_SECURE = 1;
    private static final int WIFI_STATUS_DETECT_ERROR = -1;
    private static final int WIFI_STATUS_NO_SSID = 0;
    private static volatile HwVirusManager sInstance = null;
    private IHwSecService mHwSecService;

    private HwVirusManager() {
        Log.i(TAG, "create HwVirusManager");
    }

    public static HwVirusManager getInstance() {
        if (sInstance == null) {
            synchronized (HwVirusManager.class) {
                if (sInstance == null) {
                    sInstance = new HwVirusManager();
                }
            }
        }
        return sInstance;
    }

    public List<HwVirusAppInfo> getVirusAppList() throws PermissionDenyException {
        bindHwSecService();
        PermissionDenyException permissionDenyException = new PermissionDenyException();
        try {
            if (this.mHwSecService != null) {
                Bundle result = this.mHwSecService.call(GET_VIRUS_APP_LIST, (Bundle) null);
                if (result != null) {
                    int resultCode = result.getInt(SEC_KIT_BD_PARA_RES);
                    Log.d(TAG, "call getVirusAppList and result bundle code is: " + resultCode);
                    if (resultCode == PULL_FROM_HMS_TIMEOUT) {
                        permissionDenyException.setMessage("Internet connection time out");
                        throw permissionDenyException;
                    } else if (resultCode == 0) {
                        return getHwVirusDataList(result);
                    } else {
                        permissionDenyException.setMessage("Permission denied");
                        throw permissionDenyException;
                    }
                } else {
                    permissionDenyException.setMessage("system error");
                    throw permissionDenyException;
                }
            } else {
                Log.e(TAG, "getVirusAppList: mHwSecService is null!");
                permissionDenyException.setMessage("This phone does not support virus query capability.");
                throw permissionDenyException;
            }
        } catch (JSONException e) {
            Log.e(TAG, "getVirusAppList JSONException: " + e.getMessage());
            return new ArrayList(16);
        } catch (RemoteException e2) {
            Log.e(TAG, "getVirusAppList RemoteException: " + e2.getMessage());
            return new ArrayList(16);
        }
    }

    public int getWifiThreatDetectStatus() throws PermissionDenyException {
        bindHwSecService();
        PermissionDenyException permissionDenyException = new PermissionDenyException();
        try {
            if (this.mHwSecService != null) {
                Bundle result = this.mHwSecService.call(GET_CURRENT_WIFI_THREAT_STATUS, (Bundle) null);
                if (result != null) {
                    int resultCode = result.getInt(SEC_KIT_BD_PARA_RES);
                    Log.d(TAG, "call getWifiThreatDetectStatus and result bundle code is: " + resultCode);
                    if (resultCode == PULL_FROM_HMS_TIMEOUT) {
                        permissionDenyException.setMessage("Internet connection time out");
                        throw permissionDenyException;
                    } else if (resultCode == 0) {
                        Log.d(TAG, "wifi int is: " + result.getInt("wifi_threat_status"));
                        return result.getInt("wifi_threat_status");
                    } else {
                        permissionDenyException.setMessage("Permission denied");
                        throw permissionDenyException;
                    }
                } else {
                    permissionDenyException.setMessage("system error");
                    throw permissionDenyException;
                }
            } else {
                Log.e(TAG, "getWifiThreatDetectStatus: mHwSecService is null!");
                permissionDenyException.setMessage("This phone does not support wifi status query capability.");
                throw permissionDenyException;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "getWifiThreatDetectStatus RemoteException: " + e.getMessage());
            return -1;
        }
    }

    @Nullable
    private List<HwVirusAppInfo> getHwVirusDataList(Bundle result) throws JSONException {
        if (result == null) {
            return new ArrayList(16);
        }
        String resultJsonObj = result.getString("virus_list_json");
        if (!TextUtils.isEmpty(resultJsonObj)) {
            return parseJsonToList(new JSONObject(resultJsonObj).getJSONArray("listJsonArray"));
        }
        return new ArrayList(16);
    }

    private List<HwVirusAppInfo> parseJsonToList(JSONArray jsonArray) throws JSONException {
        List<HwVirusAppInfo> virusAppInfoList = new ArrayList<>(16);
        if (jsonArray.length() > 0) {
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int apkCategory = jsonObject.getInt("apkCategory");
                String apkPackageName = jsonObject.getString("apkPackageName");
                String baseApkSha256 = jsonObject.getString("apkSha256");
                if (!TextUtils.isEmpty(apkPackageName)) {
                    virusAppInfoList.add(new HwVirusAppInfo(apkCategory, apkPackageName, Base64.getDecoder().decode(baseApkSha256)));
                }
            }
        } else {
            Log.e(TAG, "parseJsonToList: jsonArray is null or size is 0.");
        }
        return virusAppInfoList;
    }

    private void bindHwSecService() {
        if (this.mHwSecService != null) {
            Log.e(TAG, "bindHwSecService: binder is not null!");
            return;
        }
        IBinder binder = ServiceManagerEx.getService(SERVICE_NAME);
        if (binder == null) {
            Log.e(TAG, "bindHwSecService fail, binder is null.");
            return;
        }
        this.mHwSecService = IHwSecService.Stub.asInterface(binder);
        if (this.mHwSecService == null) {
            Log.e(TAG, "bindHwSecService fail, mHwSecService is null.");
            return;
        }
        try {
            binder.linkToDeath(new IBinder.DeathRecipient() {
                /* class com.huawei.securitycenter.HwVirusManager.AnonymousClass1 */

                public void binderDied() {
                    Log.e(HwVirusManager.TAG, "binderDied");
                    HwVirusManager.this.mHwSecService = null;
                }
            }, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "bindAntimalService: linkToDeath error, " + e.getMessage());
        }
    }
}
