package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifipro.WifiProCommonUtils;

public class HwPortalExceptionManager {
    public static final String ACTION_PORTAL_LOADING_STATUS = "com.huawei.wifi.ACTION_PORTAL_LOADING_STATUS";
    public static final String ACTION_QUERY_PORTAL_WIFI_INFO = "com.huawei.wifi.ACTION_QUERY_PORTAL_WIFI_INFO";
    public static final String ACTION_RESP_PORTAL_WIFI_INFO = "com.huawei.wifi.ACTION_RESP_PORTAL_WIFI_INFO";
    public static final String BROWSER_PACKET_NAME = "com.huawei.browser";
    public static final String FLAG_PORTAL_CONFIG_KEY = "portal.config.key";
    public static final String FLAG_PORTAL_FIRST_DETECT = "portal.first.detect";
    public static final String FLAG_PORTAL_LOADING_ERRORCODE = "portal.loading.errorcode";
    public static final String FLAG_PORTAL_LOADING_STATUS = "portal.loading.status";
    public static final String FLAG_PORTAL_LOADING_TRANSID = "portal.loading.transid";
    public static final String FLAG_PORTAL_LOCATION_CELLID = "portal.location.cellid";
    public static final String FLAG_PORTAL_RESP_CODE = "portal.resp.code";
    public static final String FLAG_REDIRECTED_URL = "portal.redirected.url";
    private static final int MSG_HANDLE_PORTAL_LOAD_RESULT = 103;
    private static final int MSG_HANDLE_QUERY_PORTAL_INFO = 104;
    private static final int MSG_RESET_PORTAL_PROPERTY_FLAG = 102;
    private static final int MSG_UPDATE_PORTAL_CONNECTED_INFO = 101;
    private static final String TAG = "HwPortalExceptionManager";
    private static HwPortalExceptionManager mHwPortalExceptionManager = null;
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver;
    private Context mContext;
    /* access modifiers changed from: private */
    public String mCurrConfigKey = null;
    /* access modifiers changed from: private */
    public int mFirstDetect = 0;
    /* access modifiers changed from: private */
    public Handler mHandler;
    /* access modifiers changed from: private */
    public int mHttpRespCode = 599;
    /* access modifiers changed from: private */
    public String mRedirectedUrl = null;

    public HwPortalExceptionManager(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
    }

    public static synchronized HwPortalExceptionManager getInstance(Context context) {
        HwPortalExceptionManager hwPortalExceptionManager;
        synchronized (HwPortalExceptionManager.class) {
            if (mHwPortalExceptionManager == null) {
                mHwPortalExceptionManager = new HwPortalExceptionManager(context);
            }
            hwPortalExceptionManager = mHwPortalExceptionManager;
        }
        return hwPortalExceptionManager;
    }

    public void init() {
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:
                        HwPortalExceptionManager.this.LOGD("###MSG_UPDATE_PORTAL_CONNECTED_INFO");
                        HwPortalExceptionManager.this.handleUpdatePortalConnectedInfo(msg.arg1, msg.arg2, (Bundle) msg.obj);
                        break;
                    case 102:
                        HwPortalExceptionManager.this.LOGD("###MSG_RESET_PORTAL_PROPERTY_FLAG");
                        String unused = HwPortalExceptionManager.this.mCurrConfigKey = null;
                        int unused2 = HwPortalExceptionManager.this.mFirstDetect = 0;
                        int unused3 = HwPortalExceptionManager.this.mHttpRespCode = 599;
                        String unused4 = HwPortalExceptionManager.this.mRedirectedUrl = null;
                        Settings.Secure.putInt(HwPortalExceptionManager.this.mContentResolver, "HW_WIFI_PORTAL_FLAG", 0);
                        break;
                    case 103:
                        HwPortalExceptionManager.this.LOGD("###MSG_HANDLE_PORTAL_LOAD_RESULT");
                        HwPortalExceptionManager.this.handleBroswerLoadPortalPageResult((Bundle) msg.obj);
                        break;
                    case 104:
                        HwPortalExceptionManager.this.LOGD("###MSG_HANDLE_QUERY_PORTAL_INFO");
                        HwPortalExceptionManager.this.handleBroswerQueryPortalInfo();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PORTAL_LOADING_STATUS);
        intentFilter.addAction(ACTION_QUERY_PORTAL_WIFI_INFO);
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                HwPortalExceptionManager hwPortalExceptionManager = HwPortalExceptionManager.this;
                hwPortalExceptionManager.LOGD("receive broadcast from browser, action = " + intent.getAction());
                if (HwPortalExceptionManager.ACTION_PORTAL_LOADING_STATUS.equals(intent.getAction())) {
                    String transId = intent.getStringExtra(HwPortalExceptionManager.FLAG_PORTAL_LOADING_TRANSID);
                    String status = intent.getStringExtra(HwPortalExceptionManager.FLAG_PORTAL_LOADING_STATUS);
                    String errCode = intent.getStringExtra(HwPortalExceptionManager.FLAG_PORTAL_LOADING_ERRORCODE);
                    HwPortalExceptionManager hwPortalExceptionManager2 = HwPortalExceptionManager.this;
                    hwPortalExceptionManager2.LOGD("transId = " + transId + ", status = " + status + ", errCode = " + errCode);
                    Bundle msg1Bundle = new Bundle();
                    msg1Bundle.putString(HwPortalExceptionManager.FLAG_PORTAL_LOADING_TRANSID, transId);
                    msg1Bundle.putString(HwPortalExceptionManager.FLAG_PORTAL_LOADING_STATUS, status);
                    msg1Bundle.putString(HwPortalExceptionManager.FLAG_PORTAL_LOADING_ERRORCODE, errCode);
                    HwPortalExceptionManager.this.mHandler.sendMessage(Message.obtain(HwPortalExceptionManager.this.mHandler, 103, msg1Bundle));
                } else if (HwPortalExceptionManager.ACTION_QUERY_PORTAL_WIFI_INFO.equals(intent.getAction())) {
                    HwPortalExceptionManager.this.mHandler.sendMessage(Message.obtain(HwPortalExceptionManager.this.mHandler, 104, 0, 0, null));
                }
            }
        };
        this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public void handleUpdatePortalConnectedInfo(int firstDetect, int httpRespCode, Bundle bundle) {
        if (this.mCurrConfigKey == null) {
            this.mFirstDetect = firstDetect;
            this.mHttpRespCode = httpRespCode;
            if (bundle != null) {
                this.mCurrConfigKey = bundle.getString("portal_config_key");
                this.mRedirectedUrl = bundle.getString("portal_redirected_url");
            }
            LOGD("handleUpdatePortalConnectedInfo, firstDetect = " + firstDetect + ", httpRespCode = " + httpRespCode + ", redirectedUrl = " + this.mRedirectedUrl + ", mCurrConfigKey = " + this.mCurrConfigKey);
        }
    }

    /* access modifiers changed from: private */
    public void handleBroswerLoadPortalPageResult(Bundle bundle) {
        if (bundle != null) {
            String transId = bundle.getString(FLAG_PORTAL_LOADING_TRANSID);
            String status = bundle.getString(FLAG_PORTAL_LOADING_STATUS);
            String errCode = bundle.getString(FLAG_PORTAL_LOADING_ERRORCODE);
            LOGD("handleBroswerLoadPortalPageResult, transId = " + transId + ", status = " + status + ", errCode = " + errCode);
            HwWifiCHRService chrInstance = HwWifiServiceFactory.getHwWifiCHRService();
            if (chrInstance != null) {
                Bundle data = new Bundle();
                LOGD("handleBroswerLoadPortalPageResult, portalPageLoadStatus = " + portalPageLoadStatus);
                data.putString("portalPageLoadStatus", transId + "|" + status + "|" + errCode);
                chrInstance.uploadDFTEvent(909002062, data);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleBroswerQueryPortalInfo() {
        Intent intent = new Intent(ACTION_RESP_PORTAL_WIFI_INFO);
        String configKey = this.mCurrConfigKey != null ? this.mCurrConfigKey : "";
        int cellId = WifiProCommonUtils.getCurrentCellId();
        intent.putExtra(FLAG_PORTAL_CONFIG_KEY, configKey);
        intent.putExtra(FLAG_PORTAL_RESP_CODE, this.mHttpRespCode);
        intent.putExtra(FLAG_PORTAL_FIRST_DETECT, this.mFirstDetect);
        intent.putExtra(FLAG_PORTAL_LOCATION_CELLID, cellId);
        intent.putExtra(FLAG_REDIRECTED_URL, this.mRedirectedUrl);
        intent.setPackage(BROWSER_PACKET_NAME);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        LOGD("handleBroswerQueryPortalInfo, configKey = " + configKey + ", code = " + this.mHttpRespCode + ", first = " + this.mFirstDetect + ", url = " + this.mRedirectedUrl);
    }

    public void notifyPortalAuthenStatus(boolean success) {
        if (success) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 102, 0, 0, null));
        }
    }

    public void notifyNetworkDisconnected() {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 102, 0, 0, null));
    }

    public synchronized void notifyPortalConnectedInfo(String configKey, boolean firstDetect, int httpRespCode, String redirectedUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("portal_config_key", configKey);
        bundle.putString("portal_redirected_url", redirectedUrl);
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 101, firstDetect, httpRespCode, bundle));
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
