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
import android.util.wifi.HwHiLog;
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
    private ContentResolver mContentResolver;
    private Context mContext;
    private String mCurrConfigKey = null;
    private int mFirstDetect = 0;
    private Handler mHandler;
    private int mHttpRespCode = 599;
    private String mRedirectedUrl = null;

    public HwPortalExceptionManager(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        init();
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

    private void init() {
        this.mHandler = new Handler() {
            /* class com.android.server.wifi.HwPortalExceptionManager.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:
                        HwPortalExceptionManager.this.LOGD("###MSG_UPDATE_PORTAL_CONNECTED_INFO", new Object[0]);
                        HwPortalExceptionManager.this.handleUpdatePortalConnectedInfo(msg.arg1, msg.arg2, (Bundle) msg.obj);
                        break;
                    case 102:
                        HwPortalExceptionManager.this.LOGD("###MSG_RESET_PORTAL_PROPERTY_FLAG", new Object[0]);
                        HwPortalExceptionManager.this.mCurrConfigKey = null;
                        HwPortalExceptionManager.this.mFirstDetect = 0;
                        HwPortalExceptionManager.this.mHttpRespCode = 599;
                        HwPortalExceptionManager.this.mRedirectedUrl = null;
                        Settings.Secure.putInt(HwPortalExceptionManager.this.mContentResolver, "HW_WIFI_PORTAL_FLAG", 0);
                        break;
                    case 103:
                        HwPortalExceptionManager.this.LOGD("###MSG_HANDLE_PORTAL_LOAD_RESULT", new Object[0]);
                        HwPortalExceptionManager.this.handleBroswerLoadPortalPageResult((Bundle) msg.obj);
                        break;
                    case 104:
                        HwPortalExceptionManager.this.LOGD("###MSG_HANDLE_QUERY_PORTAL_INFO", new Object[0]);
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
            /* class com.android.server.wifi.HwPortalExceptionManager.AnonymousClass2 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                HwPortalExceptionManager hwPortalExceptionManager = HwPortalExceptionManager.this;
                hwPortalExceptionManager.LOGD("receive broadcast from browser, action = " + intent.getAction(), new Object[0]);
                if (HwPortalExceptionManager.ACTION_PORTAL_LOADING_STATUS.equals(intent.getAction())) {
                    String transId = intent.getStringExtra(HwPortalExceptionManager.FLAG_PORTAL_LOADING_TRANSID);
                    String status = intent.getStringExtra(HwPortalExceptionManager.FLAG_PORTAL_LOADING_STATUS);
                    String errCode = intent.getStringExtra(HwPortalExceptionManager.FLAG_PORTAL_LOADING_ERRORCODE);
                    HwPortalExceptionManager.this.LOGD("transId = %{public}s, status = %{public}s, errCode = %{public}s", transId, status, errCode);
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
    /* access modifiers changed from: public */
    private void handleUpdatePortalConnectedInfo(int firstDetect, int httpRespCode, Bundle bundle) {
        if (this.mCurrConfigKey == null) {
            this.mFirstDetect = firstDetect;
            this.mHttpRespCode = httpRespCode;
            if (bundle != null) {
                this.mCurrConfigKey = bundle.getString("portal_config_key");
                this.mRedirectedUrl = bundle.getString("portal_redirected_url");
            }
            LOGD("handleUpdatePortalConnectedInfo, firstDetect = %{public}d, httpRespCode = %{public}d, redirectedUrl = %{private}s, mCurrConfigKey = %{private}s", Integer.valueOf(firstDetect), Integer.valueOf(httpRespCode), this.mRedirectedUrl, this.mCurrConfigKey);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBroswerLoadPortalPageResult(Bundle bundle) {
        if (bundle != null) {
            String transId = bundle.getString(FLAG_PORTAL_LOADING_TRANSID);
            String status = bundle.getString(FLAG_PORTAL_LOADING_STATUS);
            String errCode = bundle.getString(FLAG_PORTAL_LOADING_ERRORCODE);
            LOGD("handleBroswerLoadPortalPageResult, transId = %{public}s, status = %{public}s, errCode = %{public}s", transId, status, errCode);
            HwWifiCHRService chrInstance = HwWifiServiceFactory.getHwWifiCHRService();
            if (chrInstance != null) {
                Bundle data = new Bundle();
                String portalPageLoadStatus = transId + "|" + status + "|" + errCode;
                LOGD("handleBroswerLoadPortalPageResult, portalPageLoadStatus = %{public}s", portalPageLoadStatus);
                data.putString("portalPageLoadStatus", portalPageLoadStatus);
                chrInstance.uploadDFTEvent(909002062, data);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBroswerQueryPortalInfo() {
        Intent intent = new Intent(ACTION_RESP_PORTAL_WIFI_INFO);
        String configKey = this.mCurrConfigKey;
        if (configKey == null) {
            configKey = "";
        }
        int cellId = WifiProCommonUtils.getCurrentCellId();
        intent.putExtra(FLAG_PORTAL_CONFIG_KEY, configKey);
        intent.putExtra(FLAG_PORTAL_RESP_CODE, this.mHttpRespCode);
        intent.putExtra(FLAG_PORTAL_FIRST_DETECT, this.mFirstDetect);
        intent.putExtra(FLAG_PORTAL_LOCATION_CELLID, cellId);
        intent.putExtra(FLAG_REDIRECTED_URL, this.mRedirectedUrl);
        intent.setPackage(BROWSER_PACKET_NAME);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        LOGD("handleBroswerQueryPortalInfo, configKey = %{private}s, code = %{public}d, first = %{public}d, url = %{private}s", configKey, Integer.valueOf(this.mHttpRespCode), Integer.valueOf(this.mFirstDetect), this.mRedirectedUrl);
    }

    public void notifyPortalAuthenStatus(boolean success) {
        if (success) {
            Handler handler = this.mHandler;
            handler.sendMessage(Message.obtain(handler, 102, 0, 0, null));
        }
    }

    public void notifyNetworkDisconnected() {
        Handler handler = this.mHandler;
        handler.sendMessage(Message.obtain(handler, 102, 0, 0, null));
    }

    public synchronized void notifyPortalConnectedInfo(String configKey, boolean firstDetect, int httpRespCode, String redirectedUrl) {
        Bundle bundle = new Bundle();
        bundle.putString("portal_config_key", configKey);
        bundle.putString("portal_redirected_url", redirectedUrl);
        this.mHandler.sendMessage(Message.obtain(this.mHandler, 101, firstDetect ? 1 : 0, httpRespCode, bundle));
    }

    public void LOGD(String msg, Object... args) {
        HwHiLog.d(TAG, false, msg, args);
    }

    public void LOGW(String msg, Object... args) {
        HwHiLog.w(TAG, false, msg, args);
    }
}
