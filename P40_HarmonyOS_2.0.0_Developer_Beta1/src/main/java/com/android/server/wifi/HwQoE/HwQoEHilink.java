package com.android.server.wifi.HwQoE;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwHilinkProxyController;
import com.android.server.wifi.IHwHilinkCallback;
import com.android.server.wifi.hwUtil.ScanResultRecords;
import com.huawei.hilink.framework.aidl.ResponseCallbackWrapper;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwQoEHilink {
    private static final int ACC_FAST_DETECT_INTVAL_MSEC = 3000;
    private static final int ACC_NORMAL_DETECT_INTVAL_MSEC = 30000;
    private static final int ACC_STATE_SWITCH_DELAY_TIME_MSEC = 2000;
    private static final String ANDROID_PACKAGE_INSTALLER = "com.android.packageinstaller";
    private static final int APP_NAME_MAX_LEN = 64;
    private static final int DISCONNECT_HILINK_DELAY_TIME_MSEC = 100000;
    private static final int DISCOVER_LIMIT_RETRY_TIMES = 3;
    private static final String EMPTY_STRING = "";
    private static final String ENTERPRISE_ROUTER = "enterprise";
    private static final int FAST_DETECT_LIMIT_TIMES = 20;
    private static final int FIVE_ELEMENT_LIMIT_NUM = 60;
    private static final int GAME_INFO_SPLIT_INDEX_PORT = 2;
    private static final int GAME_INFO_SPLIT_INDEX_UID = 8;
    private static final int GAME_INFO_SPLIT_MIN_NUM = 9;
    private static final int HILINK_DISCOVER_TIMEOUT_MS = 3000;
    private static final boolean IS_ENABLE_EN_ACC = SystemProperties.getBoolean("ro.config.wifi_enterprise_acc", true);
    private static final boolean IS_ENABLE_GAME_ACC = SystemProperties.getBoolean("ro.config.wifi_game_acc", true);
    private static final int MSG_ENTERPRISE_APPLIST_REQUEST = 3;
    private static final int MSG_HIGAME_FAST_DETECT = 1;
    private static final int MSG_HIGAME_NORMAL_DETECT = 2;
    private static final int MSG_HIGAME_SET_ACC_GAME_MODE = 0;
    private static final int MSG_TRIGGER_ACC_APP = 4;
    private static final int PROC_NET_TCP4 = 0;
    private static final int PROC_NET_TCP6 = 2;
    private static final int PROC_NET_UDP4 = 1;
    private static final int PROC_NET_UDP6 = 3;
    private static final int PROTOCOL_TYPE_TCP = 6;
    private static final int PROTOCOL_TYPE_UDP = 17;
    private static final String TAG = "HwQoEHilink";
    private static HwQoEHilink mHwQoEHilink;
    private List<String> mAccAppList = new ArrayList();
    private boolean mAccGameDataDetected = false;
    private boolean mAccGameEnabled = false;
    private AccGameHandler mAccGameHandler;
    private IAppListStateChangeCallback mAppListStateChangeCallback = null;
    private String mAppName = "";
    private int mApplistSequence = 0;
    private String mBssid = "";
    private final Context mContext;
    private int mCurrentConnectedApType = 0;
    private long mElapsedScreenOffTime = 0;
    private int mFastDetectTimes = 0;
    private boolean mGameEnabled = false;
    private int mHilinkAccRetryTimes = 0;
    private IHwHilinkCallback mHwHilinkCallback = new IHwHilinkCallback() {
        /* class com.android.server.wifi.HwQoE.HwQoEHilink.AnonymousClass1 */
        private static final int STATE_DTLS_FAILURE = 1;

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onProxyReadyStateChanged(int state) {
            HwHiLog.d(HwQoEHilink.TAG, false, "onProxyReadyStateChanged =%{public}d", new Object[]{Integer.valueOf(state)});
            if (state != 0) {
                HwQoEHilink.this.stopAccTimer();
                return;
            }
            if (HwQoEHilink.this.isEnterpriseHilinkAp() && !HwQoEHilink.this.mIsHilinkAccServerStarted && !HwQoEHilink.this.mIsDownloadAppListFinished) {
                HwQoEHilink.this.discoverHiGameServer();
            }
            if (HwQoEHilink.this.mAccGameEnabled) {
                HwQoEHilink.this.startAccTimer();
                return;
            }
            HwQoEHilink.this.stopAccTimer();
            if (HwQoEHilink.this.mIsHilinkAccServerStarted) {
                HwQoEHilink.this.sendGameAccInfo(false);
            }
        }

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onProxyLostStateChanged() {
            HwQoEHilink.this.mAccGameEnabled = false;
            HwQoEHilink.this.mIsHilinkAccServerStarted = false;
            HwQoEHilink.this.mHilinkAccRetryTimes = 0;
            HwQoEHilink.this.mIsHilinkResponsed = false;
            HwQoEHilink.this.stopAccTimer();
        }

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onConnectionStateChanged(int state) {
            if (state == 1) {
                HwQoEHilink.this.mAccGameEnabled = false;
                HwQoEHilink.this.mIsHilinkAccServerStarted = false;
                HwQoEHilink.this.mHilinkAccRetryTimes = 0;
                HwQoEHilink.this.stopAccTimer();
            }
        }

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onConnectSuccessResult(int requestId) {
            HwHiLog.d(HwQoEHilink.TAG, false, "mHilinkAccRetryTimes=%{public}d, mIsHilinkResponsed=%{public}s", new Object[]{Integer.valueOf(HwQoEHilink.this.mHilinkAccRetryTimes), String.valueOf(HwQoEHilink.this.mIsHilinkResponsed)});
            if (HwQoEHilink.this.mHilinkAccRetryTimes > 0 && !HwQoEHilink.this.mIsHilinkResponsed) {
                HwQoEHilink.this.mIsHilinkAccServerStarted = true;
                HwQoEHilink.this.mHilinkAccRetryTimes = 0;
                if (!HwQoEHilink.this.isEnterpriseHilinkAp()) {
                    HwQoEHilink.this.sendGameAccInfo(true);
                } else if (!HwQoEHilink.this.mIsDownloadAppListFinished) {
                    HwQoEHilink.this.mAccGameHandler.sendMessage(Message.obtain(HwQoEHilink.this.mAccGameHandler, 3, 0, 0));
                } else {
                    HwHiLog.d(HwQoEHilink.TAG, false, "onConnectSuccessResult and triggerAccSwitch", new Object[0]);
                    HwQoEHilink.this.triggerAccSwitch();
                }
            }
        }

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onConnectFailedResult(int requestId, int errorCode) {
            if (HwQoEHilink.this.mHilinkAccRetryTimes > 0 && !HwQoEHilink.this.mIsHilinkResponsed) {
                HwQoEHilink.this.accGameAction(true);
            }
        }
    };
    private HwHilinkProxyController mHwHilinkProxyController;
    private boolean mIsDownloadAppListFinished = false;
    private boolean mIsHilinkAccServerStarted = false;
    private boolean mIsHilinkResponsed = false;
    private final Object mLock = new Object();
    private HwHilinkProxyController.HwHilinkModuleType mModuleType = HwHilinkProxyController.HwHilinkModuleType.HIGAME;
    private HwHilinkProxyController.HwHilinkServiceType mServiceType = HwHilinkProxyController.HwHilinkServiceType.HIGAME;
    private int mTotalApplistRespNum = 0;
    private WifiManager mWifiManager = null;
    private int mWifiState = HwQoEUtils.QOE_MSG_WIFI_DISCONNECT;

    public interface IAppListStateChangeCallback {
        void notifyClearAppList();

        void notifyUpdateAppList(List<String> list);
    }

    static /* synthetic */ int access$1508(HwQoEHilink x0) {
        int i = x0.mFastDetectTimes;
        x0.mFastDetectTimes = i + 1;
        return i;
    }

    static /* synthetic */ int access$2104(HwQoEHilink x0) {
        int i = x0.mApplistSequence + 1;
        x0.mApplistSequence = i;
        return i;
    }

    private HwQoEHilink(Context ctx) {
        this.mContext = ctx;
        this.mAccGameHandler = new AccGameHandler();
        this.mHwHilinkProxyController = HwHilinkProxyController.getInstance();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (!this.mHwHilinkProxyController.isRegisterHilinkCallback(this.mHwHilinkCallback)) {
            HwHiLog.d(TAG, false, "registerHilinkCallback failed", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public class AccGameHandler extends Handler {
        private AccGameHandler() {
        }

        /* JADX INFO: Multiple debug info for r0v21 int: [D('accEnable' int), D('totalNum' int)] */
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 0) {
                HwQoEHilink.this.setAccGameMode();
            } else if (i == 1) {
                HwQoEHilink.this.accGameAction(true);
                if (HwQoEHilink.this.mAccGameEnabled) {
                    if (HwQoEHilink.this.mFastDetectTimes < 20) {
                        HwQoEHilink.access$1508(HwQoEHilink.this);
                    }
                    if (HwQoEHilink.this.mFastDetectTimes >= 20 || HwQoEHilink.this.mAccGameDataDetected) {
                        HwQoEHilink.this.mAccGameHandler.sendEmptyMessageDelayed(2, 30000);
                    } else {
                        HwQoEHilink.this.mAccGameHandler.sendEmptyMessageDelayed(1, 3000);
                    }
                }
            } else if (i == 2) {
                HwQoEHilink.this.accGameAction(true);
                if (HwQoEHilink.this.mAccGameEnabled) {
                    HwQoEHilink.this.mAccGameHandler.sendEmptyMessageDelayed(2, 30000);
                }
            } else if (i == 3) {
                int totalNum = msg.arg1;
                int sequence = msg.arg2;
                HwQoEHilink.this.requestEnterpriseAppList(totalNum, sequence);
                if (totalNum == 0 && sequence == 0) {
                    HwQoEHilink.this.triggerAccSwitch();
                }
            } else if (i == 4) {
                int accEnable = msg.arg1;
                Object tempObj = msg.obj;
                if (tempObj != null && (tempObj instanceof String)) {
                    String appName = (String) tempObj;
                    HwHiLog.d(HwQoEHilink.TAG, false, "accEnable = %{public}d, appName = %{public}s", new Object[]{Integer.valueOf(accEnable), appName});
                    HwQoEHilink.this.triggerAccApp(accEnable, appName);
                }
            }
        }
    }

    public static synchronized HwQoEHilink getInstance(Context ctx) {
        HwQoEHilink hwQoEHilink;
        synchronized (HwQoEHilink.class) {
            if (mHwQoEHilink == null) {
                mHwQoEHilink = new HwQoEHilink(ctx);
            }
            hwQoEHilink = mHwQoEHilink;
        }
        return hwQoEHilink;
    }

    public void handleAccGameStateChanged(boolean enable, String appName) {
        int accEnable = 0;
        if (enable) {
            accEnable = 1;
        }
        AccGameHandler accGameHandler = this.mAccGameHandler;
        accGameHandler.sendMessage(Message.obtain(accGameHandler, 4, accEnable, 0, appName));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void triggerAccApp(int enable, String appName) {
        if (!TextUtils.isEmpty(appName)) {
            boolean z = true;
            if (enable != 1) {
                z = false;
            }
            this.mGameEnabled = z;
            this.mAppName = appName;
            triggerAccSwitch();
        }
    }

    public void handleAccWifiStateChanged(int state) {
        this.mWifiState = state;
        if (state != 115) {
            switch (state) {
                case HwQoEUtils.QOE_MSG_WIFI_DISABLE /* 108 */:
                case HwQoEUtils.QOE_MSG_WIFI_DISCONNECT /* 110 */:
                    HwHiLog.e(TAG, false, "QOE_MSG_WIFI_DISABLE or QOE_MSG_WIFI_DISCONNECT", new Object[0]);
                    initAccGameParams();
                    this.mCurrentConnectedApType = 0;
                    this.mIsDownloadAppListFinished = false;
                    IAppListStateChangeCallback iAppListStateChangeCallback = this.mAppListStateChangeCallback;
                    if (iAppListStateChangeCallback != null) {
                        iAppListStateChangeCallback.notifyClearAppList();
                        return;
                    }
                    return;
                case HwQoEUtils.QOE_MSG_WIFI_CONNECTED /* 109 */:
                    this.mCurrentConnectedApType = getHiLinkApType();
                    HwHiLog.d(TAG, false, "QOE_MSG_WIFI_CONNECTED and apType = %{public}d", new Object[]{Integer.valueOf(this.mCurrentConnectedApType)});
                    if (!isEnterpriseHilinkAp() || this.mIsDownloadAppListFinished) {
                        triggerAccSwitch();
                        return;
                    } else {
                        openHwHilinkProxy();
                        return;
                    }
                default:
                    HwHiLog.e(TAG, false, "unknow wifi state", new Object[0]);
                    return;
            }
        } else {
            HwHiLog.e(TAG, false, "QOE_MSG_WIFI_ROAMING", new Object[0]);
            initAccGameParams();
            triggerAccSwitch();
        }
    }

    public void handleScreenStateChanged(boolean isScreenOn) {
        HwHiLog.d(TAG, false, "handleScreenStateChanged, isScreenOn :%{public}s", new Object[]{String.valueOf(isScreenOn)});
        if (!isScreenOn) {
            this.mElapsedScreenOffTime = SystemClock.elapsedRealtime();
        } else if (this.mElapsedScreenOffTime != 0 && this.mHwHilinkProxyController.isOpened() && SystemClock.elapsedRealtime() - this.mElapsedScreenOffTime > 100000) {
            HwHiLog.d(TAG, false, "screen off > 100s, reset hilink", new Object[0]);
            this.mElapsedScreenOffTime = 0;
            initAccGameParams();
            triggerAccSwitch();
        }
    }

    private void initAccGameParams() {
        HwHiLog.d(TAG, false, "init Game config", new Object[0]);
        this.mAccGameEnabled = false;
        this.mIsHilinkAccServerStarted = false;
        this.mAccGameDataDetected = false;
        this.mHilinkAccRetryTimes = 0;
        this.mFastDetectTimes = 0;
        stopAccTimer();
        if (this.mHwHilinkProxyController.isOpened()) {
            HwHiLog.d(TAG, false, "already open, now close", new Object[0]);
            this.mHwHilinkProxyController.removeReferenceModule(this.mModuleType.getValue());
            this.mHwHilinkProxyController.closeHilinkServiceProxy();
        }
    }

    private int getHiLinkApType() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            HwHiLog.d(TAG, false, "wifiManager is null!", new Object[0]);
            return 0;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            HwHiLog.d(TAG, false, "wifiInfo is null!", new Object[0]);
            return 0;
        }
        this.mBssid = wifiInfo.getBSSID();
        if (!TextUtils.isEmpty(this.mBssid)) {
            return ScanResultRecords.getDefault().getHiLinkAp(this.mBssid);
        }
        HwHiLog.d(TAG, false, "bssid is empty!", new Object[0]);
        return 0;
    }

    private boolean isNormalHiLinkAp() {
        if (IS_ENABLE_GAME_ACC && this.mCurrentConnectedApType == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isEnterpriseHilinkAp() {
        if (!IS_ENABLE_EN_ACC || !IS_ENABLE_GAME_ACC || this.mCurrentConnectedApType != 2) {
            return false;
        }
        return true;
    }

    public void registerAppListStateChangeCallback(IAppListStateChangeCallback appListStateChangeCallback) {
        HwHiLog.d(TAG, false, "registerAppListStateChangeCallback", new Object[0]);
        this.mAppListStateChangeCallback = appListStateChangeCallback;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void triggerAccSwitch() {
        boolean enable;
        if ((isNormalHiLinkAp() || isEnterpriseHilinkAp()) && this.mGameEnabled && this.mWifiState == 109) {
            enable = true;
        } else {
            enable = false;
        }
        if (this.mAccGameEnabled == enable) {
            HwHiLog.d(TAG, false, "triggerAccSwitch, enable: %{public}s, mWifiState:%{public}d,mAccGameEnabled:%{public}s", new Object[]{String.valueOf(enable), Integer.valueOf(this.mWifiState), String.valueOf(this.mAccGameEnabled)});
        } else if (this.mIsHilinkAccServerStarted || this.mHilinkAccRetryTimes <= 3) {
            HwHiLog.d(TAG, false, "enable: %{public}s, appName: %{public}s", new Object[]{String.valueOf(enable), this.mAppName});
            this.mAccGameEnabled = enable;
            openHwHilinkProxy();
        } else {
            HwHiLog.d(TAG, false, "triggerAccSwitch, router don't support game acceleration!", new Object[0]);
            this.mAccGameEnabled = false;
            stopAccTimer();
        }
    }

    private void openHwHilinkProxy() {
        if (this.mWifiState != 109) {
            HwHiLog.e(TAG, false, "only allow openHwHilinkProxy in connected state", new Object[0]);
            return;
        }
        if (this.mAccGameHandler.hasMessages(0)) {
            this.mAccGameHandler.removeMessages(0);
        }
        if (!this.mHwHilinkProxyController.isOpened()) {
            this.mAccGameHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            this.mAccGameHandler.sendEmptyMessage(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAccGameMode() {
        HwHiLog.d(TAG, false, "set acc game mode, acc=%{public}s", new Object[]{String.valueOf(this.mAccGameEnabled)});
        this.mHwHilinkProxyController.openHilinkServiceProxy(this.mModuleType.getValue(), this.mContext);
        if (!this.mHwHilinkProxyController.isOpened()) {
            return;
        }
        if (this.mAccGameEnabled) {
            startAccTimer();
            return;
        }
        stopAccTimer();
        if (this.mIsHilinkAccServerStarted) {
            sendGameAccInfo(false);
        }
        this.mHwHilinkProxyController.removeReferenceModule(this.mModuleType.getValue());
        this.mHwHilinkProxyController.closeHilinkServiceProxy();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAccTimer() {
        HwHiLog.d(TAG, false, "start acc timer", new Object[0]);
        this.mAccGameDataDetected = false;
        this.mFastDetectTimes = 0;
        this.mAccGameHandler.sendEmptyMessageDelayed(1, 3000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopAccTimer() {
        HwHiLog.d(TAG, false, "stop acc timer", new Object[0]);
        this.mAccGameDataDetected = false;
        this.mFastDetectTimes = 0;
        if (this.mAccGameHandler.hasMessages(1)) {
            this.mAccGameHandler.removeMessages(1);
        }
        if (this.mAccGameHandler.hasMessages(2)) {
            this.mAccGameHandler.removeMessages(2);
        }
        if (this.mAccGameHandler.hasMessages(0)) {
            this.mAccGameHandler.removeMessages(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void accGameAction(boolean accelerate) {
        if (this.mIsHilinkAccServerStarted || this.mHilinkAccRetryTimes <= 3) {
            if (this.mIsHilinkAccServerStarted) {
                sendGameAccInfo(accelerate);
            } else {
                discoverHiGameServer();
            }
            return;
        }
        HwHiLog.d(TAG, false, "accGameAction, router don't support game acceleration!", new Object[0]);
        this.mAccGameEnabled = false;
        stopAccTimer();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void discoverHiGameServer() {
        HwHiLog.d(TAG, false, "prepare to discover hilik ability", new Object[0]);
        this.mIsHilinkResponsed = false;
        this.mHilinkAccRetryTimes++;
        HwHilinkProxyController hwHilinkProxyController = this.mHwHilinkProxyController;
        Objects.requireNonNull(hwHilinkProxyController);
        HwHilinkProxyController.ServiceFoundCallback serviceFoundCallback = new HwHilinkProxyController.ServiceFoundCallback();
        serviceFoundCallback.bssid = this.mBssid;
        serviceFoundCallback.serviceType = this.mServiceType.getValue();
        int ret = this.mHwHilinkProxyController.discover(serviceFoundCallback);
        if (ret != 0) {
            HwHiLog.d(TAG, false, "discover Service failed! ret = %{public}d", new Object[]{Integer.valueOf(ret)});
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendGameAccInfo(boolean accelerate) {
        if (!this.mIsHilinkAccServerStarted) {
            HwHiLog.d(TAG, false, "sendGameAccInfo,router don't support game acceleration!", new Object[0]);
            return;
        }
        HwHiLog.d(TAG, false, "prepare to call hilink", new Object[0]);
        String payload = buildHilinkPayload(accelerate);
        if (payload != null) {
            HwHiLog.d(TAG, false, "payload length: %{public}d", new Object[]{Integer.valueOf(payload.length())});
            int ret = this.mHwHilinkProxyController.call(1, payload, this.mServiceType.getValue(), new ResponseCallback());
            if (ret != 0) {
                HwHiLog.d(TAG, false, "call failed! ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            }
        }
    }

    /* access modifiers changed from: private */
    public class ResponseCallback extends ResponseCallbackWrapper {
        private ResponseCallback() {
        }

        @Override // com.huawei.hilink.framework.aidl.ResponseCallbackWrapper, com.huawei.hilink.framework.aidl.IResponseCallback
        public void onRecieveError(int errorCode) throws RemoteException {
            HwQoEHilink.this.mIsHilinkResponsed = false;
            HwHiLog.d(HwQoEHilink.TAG, false, "response recieve error : %{public}d", new Object[]{Integer.valueOf(errorCode)});
        }

        @Override // com.huawei.hilink.framework.aidl.ResponseCallbackWrapper, com.huawei.hilink.framework.aidl.IResponseCallback
        public void onRecieveResponse(int callID, String payload) throws RemoteException {
            HwQoEHilink.this.mIsHilinkResponsed = true;
            HwHiLog.d(HwQoEHilink.TAG, false, "response recieve callID : %{public}d payload: %{public}s", new Object[]{Integer.valueOf(callID), payload});
        }
    }

    private String buildHilinkPayload(boolean accelerate) {
        String appName;
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (this.mAppName == null) {
            HwHiLog.d(TAG, false, "appName is null!", new Object[0]);
            return null;
        }
        HwQoEContentAware hwQoEContentAware = HwQoEContentAware.getInstance();
        if (hwQoEContentAware == null) {
            HwHiLog.d(TAG, false, "hwQoEContentAware is null", new Object[0]);
            return null;
        }
        int appUid = hwQoEContentAware.getAppUid(this.mAppName);
        if (appUid < 0) {
            HwHiLog.d(TAG, false, "the game is not exist,appName: %{public}s", new Object[]{this.mAppName});
            this.mAccGameEnabled = false;
            stopAccTimer();
            return null;
        }
        HwHiLog.d(TAG, false, "buildHilinkPayload, accelerate: %{public}s, appUid: %{public}d, appName: %{public}s", new Object[]{String.valueOf(accelerate), Integer.valueOf(appUid), this.mAppName});
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        if (wifiManager == null) {
            HwHiLog.d(TAG, false, "wifiManager is null!", new Object[0]);
            return null;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            HwHiLog.d(TAG, false, "wifiInfo is null!", new Object[0]);
            return null;
        }
        String localIP = transIPHexToStr(wifiInfo.getIpAddress());
        try {
            if (this.mAppName.length() > 64) {
                try {
                    appName = this.mAppName.substring(0, 64);
                } catch (JSONException e) {
                    HwHiLog.e(TAG, false, "Json Exception in build hilink payload!", new Object[0]);
                    return jsonObject.toString();
                }
            } else {
                appName = this.mAppName;
            }
            if (accelerate) {
                buildSocketInfo(appUid, jsonArray, localIP);
                if (jsonArray.length() == 0) {
                    HwHiLog.d(TAG, false, "do not found game 5 elements", new Object[0]);
                    return null;
                }
                this.mAccGameDataDetected = true;
                jsonObject.put("action", "create");
                jsonObject.put("pkgName", appName);
                jsonObject.put("accelMode", "");
                jsonObject.put("data", jsonArray);
            } else {
                JSONObject json5elem = new JSONObject();
                json5elem.put("clientIp", localIP);
                try {
                    json5elem.put("clientPort", 0);
                    json5elem.put("serverIp", "");
                    json5elem.put("serverPort", 0);
                    json5elem.put("proto", 0);
                    jsonArray.put(json5elem);
                    jsonObject.put("action", "delete");
                    jsonObject.put("pkgName", appName);
                    jsonObject.put("accelMode", "");
                    jsonObject.put("data", jsonArray);
                } catch (JSONException e2) {
                }
            }
        } catch (JSONException e3) {
            HwHiLog.e(TAG, false, "Json Exception in build hilink payload!", new Object[0]);
            return jsonObject.toString();
        }
        return jsonObject.toString();
    }

    private String transIPHexToStr(int ip) {
        return (ip & 255) + "." + ((ip >> 8) & 255) + "." + ((ip >> 16) & 255) + "." + ((ip >> 24) & 255);
    }

    private String getProcFilePath(int procNetType) {
        if (procNetType == 0) {
            return "/proc/net/tcp";
        }
        if (procNetType == 1) {
            return "/proc/net/udp";
        }
        if (procNetType == 2) {
            return "/proc/net/tcp6";
        }
        if (procNetType == 3) {
            return "/proc/net/udp6";
        }
        HwHiLog.d(TAG, false, "invalid proc net type: %{public}d", new Object[]{Integer.valueOf(procNetType)});
        return "";
    }

    private void buildSocketInfo(int uid, JSONArray jsonArray, String clientIp) throws JSONException {
        buildSocketInfoPerType(uid, 0, jsonArray, clientIp);
        buildSocketInfoPerType(uid, 1, jsonArray, clientIp);
        if (isEnterpriseHilinkAp()) {
            buildSocketInfoPerType(uid, 2, jsonArray, clientIp);
            buildSocketInfoPerType(uid, 3, jsonArray, clientIp);
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:124:0x0073 */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:129:0x0073 */
    /* JADX DEBUG: Multi-variable search result rejected for r8v2, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r5v22 'bufDbg'  java.lang.StringBuffer: [D('json5elem' org.json.JSONObject), D('bufDbg' java.lang.StringBuffer)] */
    /* JADX WARN: Type inference failed for: r8v0 */
    /* JADX WARN: Type inference failed for: r8v9 */
    /* JADX WARN: Type inference failed for: r8v10 */
    /* JADX WARN: Type inference failed for: r8v18 */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        android.util.wifi.HwHiLog.d(com.android.server.wifi.HwQoE.HwQoEHilink.TAG, r8, "five element reach the max limit!", new java.lang.Object[r8]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0084, code lost:
        r5 = r18;
     */
    /* JADX WARNING: Removed duplicated region for block: B:101:0x0211 A[SYNTHETIC, Splitter:B:101:0x0211] */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x0222  */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x022b A[SYNTHETIC, Splitter:B:109:0x022b] */
    /* JADX WARNING: Removed duplicated region for block: B:114:0x023c A[SYNTHETIC, Splitter:B:114:0x023c] */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x024d A[SYNTHETIC, Splitter:B:119:0x024d] */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0037  */
    /* JADX WARNING: Removed duplicated region for block: B:134:? A[RETURN, SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003f  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0192 A[Catch:{ IOException | IllegalStateException | IndexOutOfBoundsException | NumberFormatException -> 0x01ec }, ExcHandler: IOException | IllegalStateException | IndexOutOfBoundsException | NumberFormatException (e java.lang.Throwable), Splitter:B:28:0x0090] */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x01f3 A[ExcHandler: IOException | IllegalStateException | IndexOutOfBoundsException | NumberFormatException (e java.lang.Throwable), Splitter:B:24:0x007f] */
    /* JADX WARNING: Removed duplicated region for block: B:96:0x0200 A[SYNTHETIC, Splitter:B:96:0x0200] */
    private void buildSocketInfoPerType(int uid, int procNetType, JSONArray jsonArray, String clientIp) throws JSONException {
        int protocolType;
        String filePath;
        Throwable th;
        StringBuffer bufDbg;
        StringBuffer bufDbg2;
        int i = 0;
        if (procNetType != 0) {
            if (procNetType != 1) {
                if (procNetType != 2) {
                    if (procNetType != 3) {
                        HwHiLog.d(TAG, false, "invalid protocol: %{public}d", new Object[]{0});
                        return;
                    }
                }
            }
            protocolType = 17;
            filePath = getProcFilePath(procNetType);
            if (!TextUtils.isEmpty(filePath)) {
                HwHiLog.e(TAG, false, "invalid proc net file", new Object[0]);
                return;
            }
            FileInputStream fs = null;
            InputStreamReader ir = null;
            BufferedReader br = null;
            try {
                fs = new FileInputStream(filePath);
                ir = new InputStreamReader(fs, "UTF-8");
                br = new BufferedReader(ir);
                Pattern patternPort = Pattern.compile(":([0-9|A-F|a-f]{4})");
                String strLoopIp = "0100007F";
                String strUid = Integer.toString(uid);
                StringBuffer bufDbg3 = new StringBuffer();
                String lineData = br.readLine();
                while (true) {
                    if (lineData == null) {
                        bufDbg = bufDbg3;
                        break;
                    }
                    try {
                        if (jsonArray.length() >= FIVE_ELEMENT_LIMIT_NUM) {
                            break;
                        }
                        try {
                            String[] splitData = lineData.split("\\s+");
                            if (splitData.length < 9) {
                                HwHiLog.d(TAG, false, "file data is not correct!", new Object[0]);
                                try {
                                    br.close();
                                } catch (IOException e) {
                                    HwHiLog.e(TAG, false, "getFlowInfo fail br", new Object[0]);
                                }
                                try {
                                    ir.close();
                                } catch (IOException e2) {
                                    HwHiLog.e(TAG, false, "getFlowInfo fail ir", new Object[0]);
                                }
                                try {
                                    fs.close();
                                    return;
                                } catch (IOException e3) {
                                    HwHiLog.e(TAG, false, "getFlowInfo fail fs", new Object[0]);
                                    return;
                                }
                            } else {
                                if (splitData[8].equals(strUid)) {
                                    strUid = strUid;
                                    if (splitData[2].contains(strLoopIp)) {
                                        lineData = br.readLine();
                                        strLoopIp = strLoopIp;
                                        i = 0;
                                    } else {
                                        Matcher matcher = patternPort.matcher(splitData[2]);
                                        if (matcher.find()) {
                                            strLoopIp = strLoopIp;
                                            int clientPort = Integer.decode("0x" + matcher.group(1)).intValue();
                                            JSONObject json5elem = new JSONObject();
                                            try {
                                                json5elem.put("clientIp", clientIp);
                                                json5elem.put("clientPort", clientPort);
                                                json5elem.put("serverIp", "");
                                                json5elem.put("serverPort", 0);
                                                json5elem.put("proto", protocolType);
                                            } catch (IOException | IllegalStateException | IndexOutOfBoundsException | NumberFormatException e4) {
                                                try {
                                                    HwHiLog.e(TAG, false, "getFlowInfo fail", new Object[0]);
                                                    if (br != null) {
                                                    }
                                                    if (ir != null) {
                                                    }
                                                    if (fs == null) {
                                                    }
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    if (br != null) {
                                                        try {
                                                            br.close();
                                                        } catch (IOException e5) {
                                                            HwHiLog.e(TAG, false, "getFlowInfo fail br", new Object[0]);
                                                        }
                                                    }
                                                    if (ir != null) {
                                                        try {
                                                            ir.close();
                                                        } catch (IOException e6) {
                                                            HwHiLog.e(TAG, false, "getFlowInfo fail ir", new Object[0]);
                                                        }
                                                    }
                                                    if (fs != null) {
                                                        try {
                                                            fs.close();
                                                        } catch (IOException e7) {
                                                            HwHiLog.e(TAG, false, "getFlowInfo fail fs", new Object[0]);
                                                        }
                                                    }
                                                    throw th;
                                                }
                                            } catch (Throwable th3) {
                                                th = th3;
                                                if (br != null) {
                                                }
                                                if (ir != null) {
                                                }
                                                if (fs != null) {
                                                }
                                                throw th;
                                            }
                                            try {
                                                jsonArray.put(json5elem);
                                                bufDbg2 = bufDbg3;
                                                bufDbg2.append(" ");
                                                bufDbg2.append(clientPort);
                                            } catch (IOException | IllegalStateException | IndexOutOfBoundsException | NumberFormatException e8) {
                                                HwHiLog.e(TAG, false, "getFlowInfo fail", new Object[0]);
                                                if (br != null) {
                                                    try {
                                                        br.close();
                                                    } catch (IOException e9) {
                                                        HwHiLog.e(TAG, false, "getFlowInfo fail br", new Object[0]);
                                                    }
                                                }
                                                if (ir != null) {
                                                    try {
                                                        ir.close();
                                                    } catch (IOException e10) {
                                                        HwHiLog.e(TAG, false, "getFlowInfo fail ir", new Object[0]);
                                                    }
                                                }
                                                if (fs == null) {
                                                    fs.close();
                                                    return;
                                                }
                                                return;
                                            }
                                        } else {
                                            strLoopIp = strLoopIp;
                                            bufDbg2 = bufDbg3;
                                        }
                                    }
                                } else {
                                    strUid = strUid;
                                    bufDbg2 = bufDbg3;
                                }
                                lineData = br.readLine();
                                bufDbg3 = bufDbg2;
                                i = 0;
                            }
                        } catch (IOException | IllegalStateException | IndexOutOfBoundsException | NumberFormatException e11) {
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        if (br != null) {
                        }
                        if (ir != null) {
                        }
                        if (fs != null) {
                        }
                        throw th;
                    }
                }
                String strDbg = bufDbg.toString();
                if (!TextUtils.isEmpty(strDbg)) {
                    HwHiLog.d(TAG, false, "protocol: %{public}d, port: %{public}s", new Object[]{Integer.valueOf(protocolType), strDbg});
                }
                try {
                    br.close();
                } catch (IOException e12) {
                    HwHiLog.e(TAG, false, "getFlowInfo fail br", new Object[0]);
                }
                try {
                    ir.close();
                } catch (IOException e13) {
                    HwHiLog.e(TAG, false, "getFlowInfo fail ir", new Object[0]);
                }
                try {
                    fs.close();
                    return;
                } catch (IOException e14) {
                    HwHiLog.e(TAG, false, "getFlowInfo fail fs", new Object[0]);
                    return;
                }
            } catch (Throwable th5) {
                th = th5;
                if (br != null) {
                }
                if (ir != null) {
                }
                if (fs != null) {
                }
                throw th;
            }
        }
        protocolType = 6;
        filePath = getProcFilePath(procNetType);
        if (!TextUtils.isEmpty(filePath)) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestEnterpriseAppList(int totalNum, int sequence) {
        if (!isEnterpriseHilinkAp() || !this.mIsHilinkAccServerStarted) {
            HwHiLog.e(TAG, false, "Not allow to download app list!", new Object[0]);
            return;
        }
        HwHiLog.i(TAG, false, "Prepare to request applist, totalNum = %{public}d, sequence = %{public}d!", new Object[]{Integer.valueOf(totalNum), Integer.valueOf(sequence)});
        String payload = buildApplistRequestPayload(totalNum, sequence);
        if (TextUtils.isEmpty(payload)) {
            HwHiLog.i(TAG, false, "Invalid request payload", new Object[0]);
            return;
        }
        int ret = this.mHwHilinkProxyController.call(1, payload, ENTERPRISE_ROUTER, new AppListResponseCallback());
        if (ret != 0) {
            HwHiLog.e(TAG, false, "call failed! ret = %{public}d", new Object[]{Integer.valueOf(ret)});
        }
    }

    private String buildApplistRequestPayload(int totalNum, int sequence) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action", "appList");
            jsonObject.put("totalNum", totalNum);
            jsonObject.put("sequence", sequence);
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "Json Exception when build request payload.", new Object[0]);
        }
        return jsonObject.toString();
    }

    /* access modifiers changed from: private */
    public class AppListResponseCallback extends ResponseCallbackWrapper {
        private static final int MAX_TOATL_NUM = 5;

        private AppListResponseCallback() {
        }

        private boolean isApplistRespParamsValid(int totalNum, int sequence) {
            if (totalNum <= 0 || totalNum > 5 || sequence < 0 || sequence >= totalNum || HwQoEHilink.this.mApplistSequence >= totalNum) {
                return false;
            }
            return true;
        }

        private void initAppListParam() {
            HwHiLog.e(HwQoEHilink.TAG, false, "init app list param", new Object[0]);
            HwQoEHilink.this.mTotalApplistRespNum = 0;
            HwQoEHilink.this.mApplistSequence = 0;
            synchronized (HwQoEHilink.this.mLock) {
                HwQoEHilink.this.mAccAppList.clear();
            }
        }

        @Override // com.huawei.hilink.framework.aidl.ResponseCallbackWrapper, com.huawei.hilink.framework.aidl.IResponseCallback
        public void onRecieveError(int errorCode) throws RemoteException {
            initAppListParam();
        }

        @Override // com.huawei.hilink.framework.aidl.ResponseCallbackWrapper, com.huawei.hilink.framework.aidl.IResponseCallback
        public void onRecieveResponse(int callId, String payload) throws RemoteException {
            try {
                JSONObject obj = new JSONObject(payload);
                int totalNum = obj.getInt("totalNum");
                if (!isApplistRespParamsValid(totalNum, obj.getInt("sequence"))) {
                    initAppListParam();
                    return;
                }
                HwQoEHilink.this.mTotalApplistRespNum = totalNum;
                int sequence = HwQoEHilink.this.mApplistSequence;
                JSONArray appListArray = obj.getJSONArray("appList");
                synchronized (HwQoEHilink.this.mLock) {
                    for (int i = 0; i < appListArray.length(); i++) {
                        HwQoEHilink.this.mAccAppList.add(appListArray.getJSONObject(i).getString("pkgName"));
                    }
                }
                if (sequence < totalNum - 1) {
                    HwQoEHilink.this.mAccGameHandler.sendMessage(Message.obtain(HwQoEHilink.this.mAccGameHandler, 3, HwQoEHilink.this.mTotalApplistRespNum, HwQoEHilink.access$2104(HwQoEHilink.this)));
                } else if (sequence == totalNum - 1) {
                    if (HwQoEHilink.this.mAppListStateChangeCallback != null) {
                        List<String> accAppList = new ArrayList<>();
                        synchronized (HwQoEHilink.this.mLock) {
                            accAppList.addAll(HwQoEHilink.this.mAccAppList);
                        }
                        HwQoEHilink.this.mIsDownloadAppListFinished = true;
                        HwQoEHilink.this.mAppListStateChangeCallback.notifyUpdateAppList(accAppList);
                        HwHiLog.d(HwQoEHilink.TAG, false, "Update applist and size %{public}d", new Object[]{Integer.valueOf(accAppList.size())});
                    }
                    initAppListParam();
                } else {
                    HwHiLog.w(HwQoEHilink.TAG, false, "Invalid sequence", new Object[0]);
                }
            } catch (JSONException e) {
                HwHiLog.e(HwQoEHilink.TAG, false, "Json Exception in onRecieveResponse!", new Object[0]);
                initAppListParam();
            }
        }
    }
}
