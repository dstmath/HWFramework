package com.android.server.wifi.dc;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.IWifiActionListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.rms.iaware.AppTypeRecoManager;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.HwHiDataAppStateInfo;
import com.android.server.hidata.HwHiDataManager;
import com.android.server.hidata.IHwHiDataCallback;
import com.android.server.wifi.HwQoE.HwQoEUtils;

public class DcMonitor {
    private static final int APP_ACTION_INTERNET_TEACH_MASK = 2;
    private static final int APP_ACTION_TRUE = 2;
    private static final int LATENCY_SIGNAL_THRESHOLD = 2;
    private static final String TAG = "DcMonitor";
    private static final int WIFI_SIGNAL_CHANGED_DELAY_TIME = 4000;
    private static DcMonitor sDcMonitor = null;
    private ActionReceiver mActionReceiver;
    private AppTypeRecoManager mAppTypeRecoManager;
    private Context mContext;
    private String mCurrentBssid = "";
    private DcController mDcController;
    private Handler mDcControllerHandler;
    private DcHilinkController mDcHilinkController;
    private Handler mDcHilinkHandler;
    private IHwHiDataCallback mHwHidataCallback = new IHwHiDataCallback() {
        /* class com.android.server.wifi.dc.DcMonitor.AnonymousClass1 */

        public void onAppStateChangeCallBack(HwHiDataAppStateInfo appStateInfo) {
            if (appStateInfo == null) {
                HwHiLog.d(DcMonitor.TAG, false, "gameStateInfo is null", new Object[0]);
            } else if (DcMonitor.this.isGameAppType(appStateInfo) || DcMonitor.this.isTeachingAppType(appStateInfo)) {
                int curState = appStateInfo.getCurState();
                int curScence = appStateInfo.getCurScenes();
                HwHiLog.d(DcMonitor.TAG, false, "gameStateInfo CurState:%{public}d CurScence:%{public}d", new Object[]{Integer.valueOf(curState), Integer.valueOf(curScence)});
                switch (curState) {
                    case 100:
                    case 103:
                        DcMonitor.this.handleHidataAppState(curScence, true);
                        DcMonitor.this.mDcHilinkHandler.sendEmptyMessage(6);
                        DcMonitor.this.mDcControllerHandler.sendEmptyMessage(6);
                        return;
                    case 101:
                    case HwQoEUtils.QOE_MSG_MONITOR_NO_INTERNET /* 104 */:
                        DcMonitor.this.handleHidataAppState(curScence, false);
                        DcMonitor.this.mDcHilinkHandler.sendEmptyMessage(7);
                        DcMonitor.this.mDcControllerHandler.sendEmptyMessage(7);
                        return;
                    case 102:
                    default:
                        return;
                }
            } else {
                HwHiLog.d(DcMonitor.TAG, false, "not game or teaching app type", new Object[0]);
            }
        }
    };
    private boolean mIsAudioStarted = false;
    private boolean mIsGameStarted = false;
    private boolean mIsHicallStarted = false;
    private boolean mIsHidataAppStarted = false;
    private boolean mIsVideoStarted = false;
    private boolean mIsWifiConnected = false;
    private boolean mIsWifiSignalGood = false;
    private WifiManager mWifiManager;

    private DcMonitor(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mDcController = DcController.createDcController(this.mContext);
        this.mDcControllerHandler = this.mDcController.getDcControllerHandler();
        this.mDcHilinkController = DcHilinkController.createDcHilinkController(this.mContext);
        this.mDcHilinkHandler = this.mDcHilinkController.getDcHilinkHandler();
        this.mAppTypeRecoManager = AppTypeRecoManager.getInstance();
        registerActionReceiver();
    }

    public static DcMonitor createDcMonitor(Context context) {
        if (sDcMonitor == null) {
            sDcMonitor = new DcMonitor(context);
        }
        return sDcMonitor;
    }

    public static DcMonitor getInstance() {
        return sDcMonitor;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isGameAppType(HwHiDataAppStateInfo appStateInfo) {
        Context context;
        if (appStateInfo == null || (context = this.mContext) == null || this.mAppTypeRecoManager == null) {
            return false;
        }
        String appName = context.getPackageManager().getNameForUid(appStateInfo.getCurUid());
        if (!TextUtils.isEmpty(appName) && this.mAppTypeRecoManager.getAppType(appName) == 9) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isTeachingAppType(HwHiDataAppStateInfo appStateInfo) {
        int action;
        if (appStateInfo != null && (action = appStateInfo.getAction()) >= 0 && (action & 2) == 2) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isWifiConnected() {
        return this.mIsWifiConnected;
    }

    /* access modifiers changed from: package-private */
    public boolean isGameStarted() {
        return this.mIsHidataAppStarted;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerHiDateMonitor() {
        HwHiDataManager hwHidataManager = HwHiDataManager.getInstance();
        if (hwHidataManager != null) {
            HwHiLog.d(TAG, false, "registerHiDateMonitor", new Object[0]);
            hwHidataManager.registerHiDataMonitor(this.mHwHidataCallback);
            return;
        }
        HwHiLog.e(TAG, false, "hwHidataManager is null", new Object[0]);
    }

    public void notifyNetworkRoamingCompleted(String newBssid) {
        if (this.mWifiManager == null || TextUtils.isEmpty(newBssid) || TextUtils.isEmpty(this.mCurrentBssid) || newBssid.equals(this.mCurrentBssid)) {
            HwHiLog.e(TAG, false, "notifyWifiRoamingCompleted, but bssid is unchanged, ignore it", new Object[0]);
            return;
        }
        this.mCurrentBssid = newBssid;
        HwHiLog.d(TAG, false, "NotifyNetworkRoaming completed", new Object[0]);
        Handler handler = this.mDcHilinkHandler;
        if (handler != null) {
            handler.sendEmptyMessage(29);
        }
        Handler handler2 = this.mDcControllerHandler;
        if (handler2 != null) {
            handler2.sendEmptyMessage(29);
        }
    }

    public void dcConnect(WifiConfiguration configuration, IWifiActionListener listener) {
        this.mDcController.dcConnect(configuration, listener);
    }

    public boolean dcDisconnect() {
        return this.mDcController.dcDisconnect();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleHidataAppState(int scence, boolean isStarted) {
        boolean z;
        switch (scence) {
            case 100105:
                this.mIsAudioStarted = isStarted;
                break;
            case 100106:
                this.mIsVideoStarted = isStarted;
                break;
            case 101101:
                this.mIsHicallStarted = isStarted;
                break;
            default:
                this.mIsGameStarted = isStarted;
                break;
        }
        if (!this.mIsGameStarted && !this.mIsVideoStarted && !this.mIsAudioStarted) {
            if (!this.mIsHicallStarted) {
                z = false;
                this.mIsHidataAppStarted = z;
            }
        }
        z = true;
        this.mIsHidataAppStarted = z;
    }

    private void registerActionReceiver() {
        this.mActionReceiver = new ActionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        filter.addAction("android.net.wifi.SCAN_RESULTS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mContext.registerReceiver(this.mActionReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWifiRssiChanged() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            HwHiLog.e(TAG, false, "handleWifiRssiChanged: wifiManager is null", new Object[0]);
            return;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            HwHiLog.e(TAG, false, "handleWifiRssiChanged: wifiInfo is null", new Object[0]);
        } else if (HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getFrequency(), wifiInfo.getRssi()) <= 2) {
            if (this.mDcHilinkHandler.hasMessages(33)) {
                this.mDcHilinkHandler.removeMessages(33);
            }
            if (this.mIsWifiSignalGood) {
                this.mIsWifiSignalGood = false;
                this.mDcHilinkHandler.sendEmptyMessageDelayed(34, 4000);
                this.mDcControllerHandler.sendEmptyMessageDelayed(34, 4000);
            }
        } else {
            if (this.mDcHilinkHandler.hasMessages(34)) {
                this.mDcHilinkHandler.removeMessages(34);
            }
            if (this.mDcControllerHandler.hasMessages(34)) {
                this.mDcControllerHandler.removeMessages(34);
            }
            if (!this.mIsWifiSignalGood) {
                this.mIsWifiSignalGood = true;
                this.mDcHilinkHandler.sendEmptyMessageDelayed(33, 4000);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleWifiStateChanged(NetworkInfo info) {
        if (info == null) {
            HwHiLog.w(TAG, false, "receive NETWORK_STATE_CHANGED_ACTION but network info is null", new Object[0]);
            return;
        }
        if (NetworkInfo.DetailedState.CONNECTED.equals(info.getDetailedState())) {
            this.mIsWifiConnected = true;
            if (this.mWifiManager == null) {
                HwHiLog.e(TAG, false, "receive NETWORK_STATE_CHANGED_ACTION but wifiManager is null", new Object[0]);
                return;
            }
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                String wifiBssid = wifiInfo.getBSSID();
                if (!TextUtils.isEmpty(this.mCurrentBssid)) {
                    notifyNetworkRoamingCompleted(wifiBssid);
                }
                this.mCurrentBssid = wifiBssid;
            }
            this.mDcControllerHandler.sendEmptyMessage(0);
            this.mDcHilinkHandler.sendEmptyMessage(0);
        } else if (NetworkInfo.DetailedState.DISCONNECTED.equals(info.getDetailedState())) {
            this.mIsWifiConnected = false;
            this.mIsWifiSignalGood = false;
            this.mCurrentBssid = "";
            this.mDcControllerHandler.sendEmptyMessage(1);
            this.mDcHilinkHandler.sendEmptyMessage(1);
        }
    }

    /* access modifiers changed from: private */
    public class ActionReceiver extends BroadcastReceiver {
        private ActionReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    int wifiState = intent.getIntExtra("wifi_state", 4);
                    if (wifiState == 3) {
                        DcMonitor.this.mDcControllerHandler.sendEmptyMessage(2);
                        DcMonitor.this.mDcHilinkHandler.sendEmptyMessage(2);
                    }
                    if (wifiState == 1) {
                        DcMonitor.this.mIsWifiConnected = false;
                        DcMonitor.this.mIsWifiSignalGood = false;
                        DcMonitor.this.mDcControllerHandler.sendEmptyMessage(3);
                        DcMonitor.this.mDcHilinkHandler.sendEmptyMessage(3);
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    DcMonitor.this.handleWifiStateChanged((NetworkInfo) intent.getParcelableExtra("networkInfo"));
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    NetworkInfo p2pNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (p2pNetworkInfo != null && p2pNetworkInfo.isConnected()) {
                        DcMonitor.this.mDcControllerHandler.sendEmptyMessage(4);
                        DcMonitor.this.mDcHilinkHandler.sendEmptyMessage(4);
                    }
                    if (p2pNetworkInfo != null && p2pNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        DcMonitor.this.mDcControllerHandler.sendEmptyMessage(5);
                        DcMonitor.this.mDcHilinkHandler.sendEmptyMessage(5);
                    }
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    DcMonitor.this.mDcHilinkController.handleScreenStateChanged(false);
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    DcMonitor.this.mDcHilinkController.handleScreenStateChanged(true);
                } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                    DcMonitor.this.mDcController.handleUpdateScanResults();
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    DcMonitor.this.registerHiDateMonitor();
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    DcMonitor.this.handleWifiRssiChanged();
                } else {
                    HwHiLog.d(DcMonitor.TAG, false, "receive other broadcast", new Object[0]);
                }
            }
        }
    }
}
