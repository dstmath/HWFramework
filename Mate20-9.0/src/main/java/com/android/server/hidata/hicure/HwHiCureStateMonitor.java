package com.android.server.hidata.hicure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.util.Log;
import com.android.server.hidata.arbitration.HwArbitrationChrImpl;
import com.android.server.hidata.hicure.HwHiCureActivityObserver;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class HwHiCureStateMonitor {
    private static final String ACTION_HICURE_DETECTION = "huawei.intent.action.ACTION_HICURE_DETECTION";
    private static final String ACTION_HICURE_NOTIFY = "huawei.intent.action.HI_DATA_CHECK";
    public static final String ARBITRATION_NOTIFY_HICURE_RESULT_ACTION = "huawei.intent.action.HICURE_RESULT";
    public static final int DEFAULT_HICURE_OVERTIME = 30;
    public static final int DEFAULT_HICURE_RESULT = 3;
    private static final int EVENT_APP_STATE_BACKGROUND = 3;
    private static final int EVENT_APP_STATE_FOREGROUND = 2;
    private static final int EVENT_TRANSITION_TO_HICURE = 4;
    private static final int EVENT_WIFI_DISCONNECT_STATE_TIMEOUT = 1;
    private static final int EVENT_WIFI_NETWORK_STATE_CHANGE = 0;
    private static final int EVNET_NOT_RECEIVE_HICURE_RESULT = 5;
    private static final int EVNET_RESET_HICURE_ALLOW = 6;
    private static final String EXTRA_APPUID = "extra_uid";
    private static final String EXTRA_BLOCKING_TYPE = "extra_blocking_type";
    private static final String EXTRA_CELL_LINK_STATUS = "extra_cell_link_status";
    private static final String EXTRA_CELL_SWITCH_STATUS = "extra_cell_switch_status";
    private static final String EXTRA_CURRENT_NETWORK = "extra_current_network";
    public static final String EXTRA_HICURE_DIAGNOSE_RESULT = "extra_diagnose_result";
    public static final String EXTRA_HICURE_METHOD = "extra_method";
    public static final String EXTRA_HICURE_OVERTIME = "extra_timer_result";
    public static final String EXTRA_HICURE_RESULT = "extra_result";
    private static final String EXTRA_WIFI_LINK_STATUS = "extra_wifi_link_status";
    private static final String EXTRA_WIFI_SWITCH_STATUS = "extra_wifi_switch_status";
    private static final long HICURE_DEFAULT_OVERTIME = 1800000;
    private static final long HICURE_DEFAULT_OVERTIME_RESET = 180000;
    private static final int HICURE_DELAYTIME = 2000;
    private static final long HICURE_INFORM_BLOCKTIME = 86400000;
    private static final String HICURE_PACKAGE_NAME_HWDETECTREPAIR = "com.huawei.hwdetectrepair";
    private static final String HICURE_PACKAGE_NAME_PHONE = "com.android.phone";
    private static final String PERMISSION_HICURE_SMART_NOTIFY_FAULT = "huawei.permission.SMART_NOTIFY_FAULT";
    public static final String SETTING_HICURE_LAST_INFROM_TIME = "hicure_db_curetime_value";
    public static final String TAG = "HwHiCureStateMonitor";
    private static final int WIFI_DISCONNECTED_STATE_TIMEOUT = 10000;
    private static HwHiCureStateMonitor mHwHiCureStateMonitor = null;
    /* access modifiers changed from: private */
    public HwHiCureActivityObserver.HiCureAppInfo mCachedAppInfo = null;
    private Context mContext = null;
    private int mCurServiceState = 1;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(HwHiCureStateMonitor.TAG, "handleMessage: msg[" + msg.what + "]");
            switch (msg.what) {
                case 0:
                    HwHiCureStateMonitor.this.processWifiNetworkStateChange((NetworkInfo.State) msg.obj);
                    return;
                case 1:
                    boolean unused = HwHiCureStateMonitor.this.mWifiDisconnectedTimeout = true;
                    if (HwHiCureStateMonitor.this.mCachedAppInfo != null) {
                        HwHiCureStateMonitor.this.tryPerformHiCure(HwHiCureStateMonitor.this.mCachedAppInfo.mPackageName, HwHiCureStateMonitor.this.mCachedAppInfo.mAppUID);
                        return;
                    }
                    return;
                case 2:
                    if (msg.obj instanceof HwHiCureActivityObserver.HiCureAppInfo) {
                        HwHiCureActivityObserver.HiCureAppInfo unused2 = HwHiCureStateMonitor.this.mCachedAppInfo = (HwHiCureActivityObserver.HiCureAppInfo) msg.obj;
                        if (!hasMessages(4)) {
                            sendEmptyMessageDelayed(4, 2000);
                            return;
                        }
                        return;
                    }
                    return;
                case 3:
                    HwHiCureActivityObserver.HiCureAppInfo unused3 = HwHiCureStateMonitor.this.mCachedAppInfo = null;
                    if (hasMessages(4)) {
                        removeMessages(4);
                        return;
                    }
                    return;
                case 4:
                    if (HwHiCureStateMonitor.this.mCachedAppInfo != null) {
                        HwHiCureStateMonitor.this.tryPerformHiCure(HwHiCureStateMonitor.this.mCachedAppInfo.mPackageName, HwHiCureStateMonitor.this.mCachedAppInfo.mAppUID);
                        return;
                    }
                    return;
                case 5:
                    sendEmptyMessageDelayed(6, 1800000);
                    HwHiCureStateMonitor.this.mHwArbitrationChrImpl.updateHiCureResultChr(-1, -1, -1);
                    return;
                case 6:
                    boolean unused4 = HwHiCureStateMonitor.this.mIsAllowHiCure = true;
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public HwArbitrationChrImpl mHwArbitrationChrImpl = null;
    /* access modifiers changed from: private */
    public boolean mIsAllowHiCure = true;
    /* access modifiers changed from: private */
    public boolean mIsScreenOn = true;
    private long mLastReceiveResultTime = 0;
    private StateBroadcastReceiver mStateBroadcastReceiver = null;
    /* access modifiers changed from: private */
    public boolean mWifiDisconnectedTimeout = true;

    private class StateBroadcastReceiver extends BroadcastReceiver {
        private StateBroadcastReceiver() {
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            if (action == null) {
                Log.e(HwHiCureStateMonitor.TAG, "onReceive: action is null");
                return;
            }
            Log.d(HwHiCureStateMonitor.TAG, "onReceive: action[" + action + "]");
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = 2;
                        break;
                    }
                case -2104353374:
                    if (action.equals("android.intent.action.SERVICE_STATE")) {
                        c = 4;
                        break;
                    }
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = 3;
                        break;
                    }
                case -343630553:
                    if (action.equals("android.net.wifi.STATE_CHANGE")) {
                        c = 0;
                        break;
                    }
                case 1593989801:
                    if (action.equals(HwHiCureStateMonitor.ARBITRATION_NOTIFY_HICURE_RESULT_ACTION)) {
                        c = 1;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (netInfo != null) {
                        HwHiCureStateMonitor.this.mHandler.sendMessage(HwHiCureStateMonitor.this.mHandler.obtainMessage(0, netInfo.getState()));
                        break;
                    }
                    break;
                case 1:
                    HwHiCureStateMonitor.this.receiveCureResult(intent.getIntExtra(HwHiCureStateMonitor.EXTRA_HICURE_RESULT, 3), intent.getIntExtra(HwHiCureStateMonitor.EXTRA_HICURE_OVERTIME, 30), intent.getIntExtra(HwHiCureStateMonitor.EXTRA_HICURE_DIAGNOSE_RESULT, -1), intent.getIntExtra(HwHiCureStateMonitor.EXTRA_HICURE_METHOD, -1));
                    break;
                case 2:
                    boolean unused = HwHiCureStateMonitor.this.mIsScreenOn = false;
                    break;
                case 3:
                    boolean unused2 = HwHiCureStateMonitor.this.mIsScreenOn = true;
                    break;
                case 4:
                    HwHiCureStateMonitor.this.handleTelephonyServiceStateChanged(ServiceState.newFromBundle(intent.getExtras()), intent.getIntExtra("subscription", -1));
                    break;
            }
        }
    }

    private HwHiCureStateMonitor(Context context) {
        this.mContext = context;
        this.mStateBroadcastReceiver = new StateBroadcastReceiver();
        this.mHwArbitrationChrImpl = HwArbitrationChrImpl.createInstance();
    }

    public static synchronized HwHiCureStateMonitor createHwHiCureStateMonitor(Context context) {
        HwHiCureStateMonitor hwHiCureStateMonitor;
        synchronized (HwHiCureStateMonitor.class) {
            if (mHwHiCureStateMonitor == null) {
                mHwHiCureStateMonitor = new HwHiCureStateMonitor(context);
            }
            hwHiCureStateMonitor = mHwHiCureStateMonitor;
        }
        return hwHiCureStateMonitor;
    }

    public void startMonitor() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction(ARBITRATION_NOTIFY_HICURE_RESULT_ACTION);
        intentFilter.addAction("android.intent.action.SERVICE_STATE");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiver(this.mStateBroadcastReceiver, intentFilter);
    }

    /* access modifiers changed from: private */
    public void receiveCureResult(int result, int overTime, int diagnoseResult, int method) {
        long querytime = SystemClock.elapsedRealtime();
        if (0 == this.mLastReceiveResultTime || querytime - this.mLastReceiveResultTime >= 2000) {
            Log.d(TAG, "receiveCureResult: result[" + result + "] overTime[" + overTime + "] diagnoseResult[" + diagnoseResult + "] method[" + method + "]");
            this.mLastReceiveResultTime = querytime;
            this.mHandler.removeMessages(5);
            this.mHandler.removeMessages(6);
            if (overTime <= 0) {
                this.mIsAllowHiCure = true;
                return;
            }
            this.mHwArbitrationChrImpl.updateHiCureResultChr(result, diagnoseResult, method);
            this.mHwArbitrationChrImpl.updateIsStallAfterCure(0);
            this.mHandler.sendEmptyMessageDelayed(6, ((long) (overTime * 60)) * 1000);
            return;
        }
        this.mLastReceiveResultTime = querytime;
    }

    /* access modifiers changed from: private */
    public void handleTelephonyServiceStateChanged(ServiceState serviceState, int subId) {
        if (HwHiCureCommonUtil.getDefaultDataSubId(this.mContext) != subId || serviceState == null) {
            Log.d(TAG, "handleTelephonyServiceStateChanged: subId[" + subId + "] serviceState[" + serviceState + "]");
            return;
        }
        int newState = serviceState.getDataRegState();
        Log.d(TAG, "handleTelephonyServiceStateChanged: newState[" + newState + "] mCurServiceState[" + this.mCurServiceState + "]");
        if (newState == this.mCurServiceState) {
            Log.d(TAG, "service state is not changed.");
        } else {
            this.mCurServiceState = newState;
        }
    }

    /* access modifiers changed from: private */
    public void processWifiNetworkStateChange(NetworkInfo.State wifiNetworkState) {
        if (wifiNetworkState == NetworkInfo.State.DISCONNECTED) {
            if (!this.mHandler.hasMessages(1)) {
                this.mHandler.sendEmptyMessageDelayed(1, MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
            }
        } else if (wifiNetworkState == NetworkInfo.State.CONNECTED) {
            this.mWifiDisconnectedTimeout = false;
            if (this.mHandler.hasMessages(1)) {
                this.mHandler.removeMessages(1);
            }
        }
    }

    /* access modifiers changed from: private */
    public void tryPerformHiCure(String packegeName, int appUID) {
        if (!this.mIsAllowHiCure) {
            Log.d(TAG, "tryPerformHiCure: is not allow hicure");
        } else if (!this.mIsScreenOn) {
            Log.d(TAG, "tryPerformHiCure: Screen is off");
        } else if (this.mCurServiceState != 0) {
            Log.d(TAG, "tryPerformHiCure: network is not in service.");
        } else if (!this.mWifiDisconnectedTimeout) {
            Log.d(TAG, "tryPerformHiCure: wifi link normally");
        } else {
            boolean wifiSwitchStatus = HwHiCureCommonUtil.isWifiEnabled(this.mContext);
            boolean wifiLinkStatus = HwHiCureCommonUtil.isWifiConnected(this.mContext);
            boolean cellSwitchStatus = HwHiCureCommonUtil.isUserDataEnabled(this.mContext);
            boolean cellLinkStatus = HwHiCureCommonUtil.isDataConnected(this.mContext);
            int currentNetwork = HwHiCureCommonUtil.getActiveConnectType(this.mContext);
            boolean dataRoamingSwitch = HwHiCureCommonUtil.isDataRoamingEnabled(this.mContext);
            boolean roaming = HwHiCureCommonUtil.isRoaming(this.mContext);
            if (wifiLinkStatus) {
                boolean z = cellSwitchStatus;
                boolean z2 = cellLinkStatus;
            } else if (cellLinkStatus) {
                boolean z3 = wifiSwitchStatus;
                boolean z4 = cellSwitchStatus;
                boolean z5 = cellLinkStatus;
            } else {
                if ((!roaming || dataRoamingSwitch) && cellSwitchStatus) {
                    this.mIsAllowHiCure = false;
                    sendHicureBroadcast(HICURE_PACKAGE_NAME_PHONE);
                    this.mHwArbitrationChrImpl.updateHiCureRequestChr(packegeName, 1, currentNetwork, cellSwitchStatus, dataRoamingSwitch, cellLinkStatus, wifiSwitchStatus, wifiLinkStatus, 0);
                    this.mHandler.sendEmptyMessageDelayed(5, HICURE_DEFAULT_OVERTIME_RESET);
                } else {
                    long startCureTime = System.currentTimeMillis();
                    long lastCureTime = getLastCureTime(this.mContext);
                    long cureTimeDiff = startCureTime - lastCureTime;
                    Log.d(TAG, "tryPerformHiCure: startCureTime[" + startCureTime + "] lastCureTime[" + lastCureTime + "] cureTimeDiff[" + cureTimeDiff + "]");
                    if (cureTimeDiff < 0) {
                        Settings.System.putLong(this.mContext.getContentResolver(), "hicure_db_curetime_value", startCureTime);
                    } else if (cureTimeDiff > 86400000) {
                        this.mIsAllowHiCure = false;
                        sendHicureBroadcast(HICURE_PACKAGE_NAME_HWDETECTREPAIR);
                        long j = cureTimeDiff;
                        long j2 = lastCureTime;
                        boolean z6 = cellSwitchStatus;
                        boolean z7 = cellLinkStatus;
                        boolean z8 = wifiSwitchStatus;
                        this.mHwArbitrationChrImpl.updateHiCureRequestChr(packegeName, 1, currentNetwork, cellSwitchStatus, dataRoamingSwitch, cellLinkStatus, wifiSwitchStatus, wifiLinkStatus, 0);
                        this.mHandler.sendEmptyMessageDelayed(5, HICURE_DEFAULT_OVERTIME_RESET);
                        Settings.System.putLong(this.mContext.getContentResolver(), "hicure_db_curetime_value", startCureTime);
                    } else {
                        boolean z9 = cellSwitchStatus;
                        boolean z10 = cellLinkStatus;
                    }
                }
            }
        }
    }

    private void sendHicureBroadcast(String packageName) {
        Intent intent = new Intent(ACTION_HICURE_NOTIFY);
        intent.setFlags(67108864);
        intent.setPackage(packageName);
        intent.putExtra(EXTRA_BLOCKING_TYPE, 1);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION_HICURE_SMART_NOTIFY_FAULT);
        Log.d(TAG, "sendHicureBroadcast: intent = " + intent);
    }

    private long getLastCureTime(Context context) {
        return Settings.System.getLong(context.getContentResolver(), "hicure_db_curetime_value", -1);
    }

    public void sendActvityStateChanged(int appState, HwHiCureActivityObserver.HiCureAppInfo hiCureAppInfo) {
        if (appState == 0) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, hiCureAppInfo));
        } else if (1 == appState) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3, hiCureAppInfo));
        }
    }
}
