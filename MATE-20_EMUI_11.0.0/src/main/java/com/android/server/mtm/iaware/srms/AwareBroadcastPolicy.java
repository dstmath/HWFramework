package com.android.server.mtm.iaware.srms;

import android.content.ActionFilterEntry;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.am.HwBroadcastRecord;
import com.android.server.mtm.MultiTaskManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.iaware.appmng.policy.BrFilterPolicy;
import com.android.server.mtm.iaware.brjob.controller.KeyWordController;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup;
import com.android.server.rms.iaware.appmng.AwareSceneRecognize;
import com.android.server.rms.iaware.feature.DevSchedFeatureRt;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.BroadcastExFeature;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import com.huawei.android.app.usage.UsageStatsManagerInternalEx;
import com.huawei.android.content.IntentFilterExt;
import com.huawei.android.os.HandlerEx;
import com.huawei.server.pm.PackageManagerServiceEx;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AwareBroadcastPolicy {
    private static final int APP_STATE_IDLE = 1;
    private static final int APP_STATE_NOT_IDLE = 0;
    private static final int BATTERY_DATA_FILTER = 1;
    private static final int BATTERY_DATA_NO_FILTER = 0;
    public static final String BATTERY_LEVEL_NAME = "BatteryLevel";
    public static final String BATTERY_STATUS_CHANGE_VALUE = "CHARGING_CHANGE";
    public static final String BATTERY_STATUS_CHARGING_OFF_VALUE = "CHARGING_OFF";
    public static final String BATTERY_STATUS_CHARGING_ON_VALUE = "CHARGING_ON";
    public static final String BATTERY_STATUS_NAME = "BatteryStatus";
    private static final int BROADCAST_PROXY_SPEED_INDEX = 2;
    private static final int BROADCAST_PROXY_SPEED_INTERVAL = 100;
    private static final long BROADCAST_PROXY_SPEED_INTERVAL_LONG = 100;
    private static final int BROADCAST_PROXY_SPEED_NUMBER = 10;
    private static final int BROADCAST_PROXY_SPEED_NUMBER_INDEX_MAX = 9;
    public static final int CHARGING_OFF = 0;
    public static final int CHARGING_ON = 1;
    public static final String CONNECTIVITY_NAME = "ConnectStatus";
    public static final String CONNECT_STATUS_MOBILE_CON_VALUE = "MOBILEDATACON";
    public static final String CONNECT_STATUS_MOBILE_DSCON_VALUE = "MOBILEDATADSCON";
    public static final String CONNECT_STATUS_WIFI_CON_VALUE = "WIFIDATACON";
    public static final String CONNECT_STATUS_WIFI_DSCON_VALUE = "WIFIDATADSCON";
    public static final String DROP = "DROP";
    public static final String DROP_TYPE = "DropType";
    public static final String EXTRA_NAME = "Extra";
    private static final int GOOGLE_STATE_CONNECTED = 1;
    private static final int GOOGLE_STATE_DISCONNECTED = -1;
    private static final int IAWARE_APP_INSTALL_DELAY_TIME = 5000;
    private static final int IAWARE_DWONLOAD_DELAY_TIME = 3000;
    private static final int IAWARE_START_ACTIVITY_DELAY_TIME = 3000;
    public static final int INVALUED_DATA = -1;
    private static final int LOOPER_CHECK_TIME = 1000;
    private static final int LOOPER_CHECK_TIME_RECOUNT = 2000;
    public static final int MOBILE_CON = 1;
    public static final int MOBILE_DSCON = 2;
    private static final int MSG_APP_IDLE_STAT_CHANGE = 211;
    private static final int MSG_GOOGLE_CONN_STAT_CHANGE = 210;
    private static final int MSG_INSTALL_APP_TIMEOUT = 208;
    private static final int MSG_NOTIFY_OVERFLOW = 212;
    private static final int MSG_POLICY_DL_END = 202;
    private static final int MSG_POLICY_DL_START = 201;
    private static final int MSG_POLICY_END_CHECK = 206;
    private static final int MSG_POLICY_SCENE_ACTIVITY = 205;
    private static final int MSG_POLICY_SCENE_SLIP = 203;
    private static final int MSG_START_ACTIVITY_TIMEOUT = 207;
    private static final int MSG_UPDATE_BR_POLICY = 209;
    public static final String SCREEN_NAME = "ScreenStatus";
    public static final String SCREEN_STATUS_OFF = "SCREENOFF";
    public static final String SCREEN_STATUS_ON = "SCREENON";
    private static final String TAG = "AwareBroadcastPolicy";
    public static final int UNKNOW_CHARGING = -1;
    public static final int UNKNOW_CON = -1;
    public static final int WIFI_CON = 3;
    private static final int WIFI_DATA_DISCARD = 2;
    private static final int WIFI_DATA_FILTER = 1;
    private static final int WIFI_DATA_NO_FILTER = 0;
    public static final int WIFI_DSCON = 4;
    public static final String WIFI_NET_STATUS_NAME = "WifiNetStatus";
    public static final String WIFI_RSSI_STATUS_NAME = "WifiRssi";
    public static final String WIFI_STATUS_CONNECTING_VALUE = "WIFICONTING";
    public static final String WIFI_STATUS_CONNECT_VALUE = "WIFICON";
    public static final String WIFI_STATUS_DISABLED_VALUE = "WIFIDISABLED";
    public static final String WIFI_STATUS_DISCONNECT_VALUE = "WIFIDSCON";
    public static final String WIFI_STATUS_ENABLED_VALUE = "WIFIENABLED";
    public static final String WIFI_STATUS_NAME = "WifiStatus";
    public static final String WIFI_SUP_STATUS_COMPLET_VALUE = "WIFISUPCOMPLE";
    public static final String WIFI_SUP_STATUS_DISCONNECT_VALUE = "WIFISUPDSCON";
    public static final String WIFI_SUP_STATUS_NAME = "WifiSupStatus";
    private static ArrayMap<String, AwareBroadcastCache> sAwareBrCaches = new ArrayMap<>();
    private static boolean sGoogleConnStat = false;
    private AwareBroadcastConfig mAwareBroadcastConfig;
    private final ArraySet<Integer> mAwareDownloadingUid = new ArraySet<>();
    private ArrayMap<String, FilterStatus> mAwareFilterStatus = new ArrayMap<>();
    private boolean mAwareInstallApp = false;
    private ArraySet<String> mAwareNoProxyActions = new ArraySet<>();
    private ArraySet<String> mAwareNoProxyPkgs = new ArraySet<>();
    private boolean mAwareProxyActivitStart = false;
    private boolean mAwareProxySlip = false;
    private AwareSceneStateCallback mAwareSceneStateCallback;
    private boolean mAwareScreenOn = true;
    private AwareStateCallback mAwareStateCallback;
    private ArraySet<String> mAwareTrimActions = new ArraySet<>();
    private ArrayMap<String, ArraySet<String>> mAwareUnProxySys = new ArrayMap<>();
    private int mBatteryLevel = -1;
    private AwareBroadcastProcess mBgAwareBr = null;
    private int mCharging = -1;
    private int mConnectStatus = -1;
    private Context mContext = null;
    private long mCountCheck = 0;
    private AwareBroadcastProcess mFgAwareBr = null;
    private int mForegroundAppLevel = 2;
    private final AwareBroadcastPolicyHandler mHandler;
    private long mLastParallelBrTime = 0;
    private final Object mLockNoProxyActions = new Object();
    private final Object mLockNoProxyPkgs = new Object();
    private final Object mLockTrimActions = new Object();
    private final Object mLockUnProxySys = new Object();
    private int mNoTouchCheckCount = MultiTaskManagerService.MSG_POLICY_BR;
    private int mPlugedtype = -1;
    private int mPrePlugedtype = -1;
    private final long[][] mProxyCount = ((long[][]) Array.newInstance(long.class, 10, 2));
    private boolean mScreenOn = true;
    private boolean mSpeedParallelStartProxy = false;
    private long mStartParallelBrTime = 0;
    private int mTouchCheckCount = 60;
    private NetworkInfo.State mWifiNetStatus = NetworkInfo.State.UNKNOWN;
    private int mWifiRssi = -127;
    private int mWifiStatus = 4;
    private SupplicantState mWifiSupStatus = SupplicantState.INVALID;

    public AwareBroadcastPolicy(Handler handler, Context context) {
        this.mContext = context;
        this.mBgAwareBr = new AwareBroadcastProcess(this, handler, "iawarebackground");
        this.mFgAwareBr = new AwareBroadcastProcess(this, handler, "iawareforeground");
        this.mHandler = new AwareBroadcastPolicyHandler(handler.getLooper());
        this.mAwareBroadcastConfig = AwareBroadcastConfig.getInstance();
        this.mAwareFilterStatus.put(WIFI_NET_STATUS_NAME, new WifiNetFilterStatus(this, null));
        this.mAwareFilterStatus.put("WifiStatus", new WifiFilterStatus(this, null));
        this.mAwareFilterStatus.put(WIFI_SUP_STATUS_NAME, new WifiSupFilterStatus(this, null));
        this.mAwareFilterStatus.put(WIFI_RSSI_STATUS_NAME, new WifiRssiFilterStatus(this, null));
        this.mAwareFilterStatus.put(BATTERY_STATUS_NAME, new BatteryFilterStatus(this, null));
        this.mAwareFilterStatus.put(BATTERY_LEVEL_NAME, new BatteryLevelFilterStatus(this, null));
        this.mAwareFilterStatus.put(CONNECTIVITY_NAME, new ConnectivityFilterStatus(this, null));
        this.mAwareFilterStatus.put("Extra", new ExtraFilterStatus(this, null));
        this.mAwareFilterStatus.put(SCREEN_NAME, new ScreenFilterStatus(this, null));
        this.mAwareFilterStatus.put(DROP_TYPE, new DropFilterStatus(this, null));
        initState();
        UsageStatsManagerInternalEx.addAppIdleStateChangeListener(new AppIdleStateChangeListener(this, null));
    }

    /* access modifiers changed from: private */
    public final class AwareBroadcastPolicyHandler extends HandlerEx {
        public AwareBroadcastPolicyHandler(Looper looper) {
            super(looper, (Handler.Callback) null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AwareBroadcastPolicy.MSG_POLICY_DL_START /* 201 */:
                    synchronized (AwareBroadcastPolicy.this.mAwareDownloadingUid) {
                        AwareBroadcastPolicy.this.mAwareDownloadingUid.add(Integer.valueOf(msg.arg1));
                    }
                    return;
                case AwareBroadcastPolicy.MSG_POLICY_DL_END /* 202 */:
                    synchronized (AwareBroadcastPolicy.this.mAwareDownloadingUid) {
                        AwareBroadcastPolicy.this.mAwareDownloadingUid.remove(Integer.valueOf(msg.arg1));
                    }
                    return;
                case AwareBroadcastPolicy.MSG_POLICY_SCENE_SLIP /* 203 */:
                    AwareBroadcastPolicy.this.setAwarePolicy(2, msg.arg1);
                    return;
                case 204:
                default:
                    return;
                case AwareBroadcastPolicy.MSG_POLICY_SCENE_ACTIVITY /* 205 */:
                    AwareBroadcastPolicy.this.setAwarePolicy(4, msg.arg1);
                    return;
                case AwareBroadcastPolicy.MSG_POLICY_END_CHECK /* 206 */:
                    AwareBroadcastPolicy.this.startUnproxyBroadcast();
                    return;
                case AwareBroadcastPolicy.MSG_START_ACTIVITY_TIMEOUT /* 207 */:
                    AwareBroadcastPolicy.this.mAwareProxyActivitStart = false;
                    return;
                case AwareBroadcastPolicy.MSG_INSTALL_APP_TIMEOUT /* 208 */:
                    AwareBroadcastPolicy.this.mAwareInstallApp = false;
                    return;
                case AwareBroadcastPolicy.MSG_UPDATE_BR_POLICY /* 209 */:
                    if (msg.obj instanceof AwareProcessInfo) {
                        AwareBroadcastPolicy.this.updateBrPolicy(msg.arg1, (AwareProcessInfo) msg.obj);
                        return;
                    }
                    return;
                case AwareBroadcastPolicy.MSG_GOOGLE_CONN_STAT_CHANGE /* 210 */:
                    AwareBroadcastPolicy.this.resetGoogleConnStat(msg.arg1);
                    return;
                case AwareBroadcastPolicy.MSG_APP_IDLE_STAT_CHANGE /* 211 */:
                    if (msg.obj instanceof String) {
                        AwareBroadcastPolicy.this.updateAppIdleStat(msg.arg1, msg.arg2, (String) msg.obj);
                        return;
                    }
                    return;
                case AwareBroadcastPolicy.MSG_NOTIFY_OVERFLOW /* 212 */:
                    AwareBroadcastPolicy.this.unproxyCacheBr(msg.arg1);
                    return;
            }
        }
    }

    public void init() {
        this.mAwareStateCallback = new AwareStateCallback(this, null);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 5);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 1);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 2);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 3);
        AwareAppKeyBackgroup.getInstance().registerStateCallback(this.mAwareStateCallback, 4);
        this.mAwareSceneStateCallback = new AwareSceneStateCallback(this, null);
        AwareSceneRecognize.getInstance().registerStateCallback(this.mAwareSceneStateCallback, 1);
        this.mAwareBroadcastConfig.doinit();
    }

    public static void initBrCache(String name, Object ams) {
        if (!sAwareBrCaches.containsKey(name)) {
            sAwareBrCaches.put(name, new AwareBroadcastCache(name, ams));
        }
    }

    public boolean enqueueAwareProxyBroacast(boolean isParallel, HwBroadcastRecord record) {
        if (record == null) {
            return false;
        }
        if (record.isBackground()) {
            this.mBgAwareBr.enqueueAwareProxyBroacast(isParallel, record);
            return true;
        }
        this.mFgAwareBr.enqueueAwareProxyBroacast(isParallel, record);
        return true;
    }

    public boolean shouldAwareProxyBroadcast(String brAction, int callingPid, int receiverUid, int receiverPid, String recevierPkg) {
        synchronized (this.mAwareDownloadingUid) {
            if (this.mAwareDownloadingUid.contains(Integer.valueOf(receiverUid))) {
                if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(TAG, "iaware_br : downloading, don't proxy : " + recevierPkg + ": action : " + brAction);
                }
                return false;
            }
        }
        if (!isForbidProxy(brAction, recevierPkg) && isAwarePrepared()) {
            return true;
        }
        return false;
    }

    private boolean isAwarePrepared() {
        return (this.mFgAwareBr == null || this.mBgAwareBr == null) ? false : true;
    }

    private boolean isForbidProxy(String action, String pkg) {
        boolean proxyWithoutAction;
        boolean proxyWithoutPkg;
        synchronized (this.mLockNoProxyActions) {
            proxyWithoutAction = this.mAwareNoProxyActions.contains(action);
        }
        if (proxyWithoutAction) {
            return true;
        }
        synchronized (this.mLockNoProxyPkgs) {
            proxyWithoutPkg = this.mAwareNoProxyPkgs.contains(pkg);
        }
        return proxyWithoutPkg;
    }

    public boolean isProxyedAllowedCondition() {
        return this.mAwareScreenOn && this.mSpeedParallelStartProxy;
    }

    public boolean isNotProxySysPkg(String pkg, String action) {
        synchronized (this.mLockUnProxySys) {
            ArraySet<String> actions = this.mAwareUnProxySys.get(pkg);
            if (actions == null) {
                return false;
            }
            return actions.contains(action);
        }
    }

    public boolean isTrimAction(String action) {
        boolean contains;
        synchronized (this.mLockTrimActions) {
            contains = this.mAwareTrimActions.contains(action);
        }
        return contains;
    }

    public void updateXmlConfig() {
        if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.i(TAG, "updateXmlConfig begin");
        }
        if (!isAwarePrepared()) {
            AwareLog.e(TAG, "iaware process broacast don't prepared.");
            return;
        }
        synchronized (this.mLockNoProxyActions) {
            this.mAwareNoProxyActions = this.mAwareBroadcastConfig.getUnProxyActionList();
        }
        synchronized (this.mLockNoProxyPkgs) {
            this.mAwareNoProxyPkgs = this.mAwareBroadcastConfig.getUnProxyPkgList();
        }
        synchronized (this.mLockUnProxySys) {
            this.mAwareUnProxySys = this.mAwareBroadcastConfig.getUnProxySysList();
        }
        synchronized (this.mLockTrimActions) {
            this.mAwareTrimActions = this.mAwareBroadcastConfig.getTrimActionList();
        }
        this.mForegroundAppLevel = this.mAwareBroadcastConfig.getFgAppLevel();
        this.mNoTouchCheckCount = this.mAwareBroadcastConfig.getNoTouchCheckCount();
        this.mTouchCheckCount = this.mAwareBroadcastConfig.getTouchCheckCount();
        this.mBgAwareBr.setUnProxyMaxDuration(this.mAwareBroadcastConfig.getUnProxyMaxDuration());
        this.mBgAwareBr.setUnProxyMaxSpeed(this.mAwareBroadcastConfig.getUnProxyMaxSpeed());
        this.mBgAwareBr.setUnProxyMinSpeed(this.mAwareBroadcastConfig.getUnProxyMinSpeed());
        this.mBgAwareBr.setUnProxyMiddleSpeed(this.mAwareBroadcastConfig.getUnProxyMiddleSpeed());
        this.mBgAwareBr.setUnProxyHighSpeed(this.mAwareBroadcastConfig.getUnProxyHighSpeed());
        this.mFgAwareBr.setUnProxyMaxDuration(this.mAwareBroadcastConfig.getUnProxyMaxDuration());
        this.mFgAwareBr.setUnProxyMaxSpeed(this.mAwareBroadcastConfig.getUnProxyMaxSpeed());
        this.mFgAwareBr.setUnProxyMinSpeed(this.mAwareBroadcastConfig.getUnProxyMinSpeed());
        this.mFgAwareBr.setUnProxyMiddleSpeed(this.mAwareBroadcastConfig.getUnProxyMiddleSpeed());
        this.mFgAwareBr.setUnProxyHighSpeed(this.mAwareBroadcastConfig.getUnProxyHighSpeed());
    }

    public void iawareStartCountBroadcastSpeed(boolean isParallel, long dispatchClockTime, int size) {
        if (this.mAwareScreenOn && !this.mSpeedParallelStartProxy && isAwarePrepared() && isParallel) {
            checkParallCount(dispatchClockTime, size);
        }
    }

    public void endCheckCount() {
        if (isAwarePrepared()) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = MSG_POLICY_END_CHECK;
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startUnproxyBroadcast() {
        if (isEmptyAwareBrList()) {
            this.mSpeedParallelStartProxy = false;
            return;
        }
        this.mBgAwareBr.startUnproxyBroadcast();
        this.mFgAwareBr.startUnproxyBroadcast();
    }

    private void checkParallCount(long dispatchClockTime, int size) {
        this.mCountCheck = 0;
        long j = this.mStartParallelBrTime;
        if (j == 0) {
            long[][] jArr = this.mProxyCount;
            long[] jArr2 = jArr[0];
            this.mStartParallelBrTime = dispatchClockTime;
            jArr2[0] = dispatchClockTime;
            long[] jArr3 = jArr[0];
            long j2 = (long) size;
            this.mCountCheck = j2;
            jArr3[1] = j2;
            for (int index = 1; index < 10; index++) {
                long[][] jArr4 = this.mProxyCount;
                jArr4[index][0] = this.mStartParallelBrTime + (((long) index) * BROADCAST_PROXY_SPEED_INTERVAL_LONG);
                jArr4[index][1] = 0;
            }
            setProxyCount();
            return;
        }
        this.mLastParallelBrTime = dispatchClockTime;
        long tempPeriod = this.mLastParallelBrTime - j;
        if (tempPeriod < 0) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br checkcount < 0");
            }
            this.mStartParallelBrTime = 0;
            checkParallCount(dispatchClockTime, size);
        }
        countBrByTimePeriod(tempPeriod, dispatchClockTime, size);
    }

    private void timeOverLoopCheckTimeProcess(long tempPeriod, long dispatchClockTime, int size) {
        long j = BROADCAST_PROXY_SPEED_INTERVAL_LONG;
        int index = (int) ((tempPeriod - 1000) / BROADCAST_PROXY_SPEED_INTERVAL_LONG);
        int i = 9;
        if (index == 9) {
            this.mStartParallelBrTime = 0;
            checkParallCount(dispatchClockTime, size);
        }
        if (index < 9) {
            this.mStartParallelBrTime = this.mProxyCount[index + 1][0];
            int proxyIndex = index;
            int tempIndex = 0;
            while (tempIndex < 10) {
                proxyIndex++;
                if (proxyIndex < 10) {
                    long[][] jArr = this.mProxyCount;
                    jArr[tempIndex][0] = jArr[proxyIndex][0];
                    jArr[tempIndex][1] = jArr[proxyIndex][1];
                } else if (tempIndex < i) {
                    long[][] jArr2 = this.mProxyCount;
                    jArr2[tempIndex][0] = jArr2[tempIndex - 1][0] + j;
                    jArr2[tempIndex][1] = 0;
                } else {
                    long[][] jArr3 = this.mProxyCount;
                    jArr3[tempIndex][0] = jArr3[tempIndex - 1][0] + j;
                    jArr3[tempIndex][1] = (long) size;
                }
                tempIndex++;
                i = 9;
                j = BROADCAST_PROXY_SPEED_INTERVAL_LONG;
            }
            for (int countIndex = 0; countIndex < 10; countIndex++) {
                this.mCountCheck += this.mProxyCount[countIndex][1];
            }
            setProxyCount();
        }
    }

    private void countBrByTimePeriod(long tempPeriod, long dispatchClockTime, int size) {
        if (tempPeriod >= 2000) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br checkcount > 2000");
            }
            this.mStartParallelBrTime = 0;
            checkParallCount(dispatchClockTime, size);
        } else if (tempPeriod >= 0 && tempPeriod < 1000) {
            int index = (int) (tempPeriod / BROADCAST_PROXY_SPEED_INTERVAL_LONG);
            long[][] jArr = this.mProxyCount;
            jArr[index][1] = jArr[index][1] + ((long) size);
            for (int tempIndex = 0; tempIndex <= index; tempIndex++) {
                this.mCountCheck += this.mProxyCount[tempIndex][1];
            }
            setProxyCount();
        } else if (tempPeriod >= 1000) {
            timeOverLoopCheckTimeProcess(tempPeriod, dispatchClockTime, size);
        }
    }

    private void setProxyCount() {
        if (isStrictCondition()) {
            if (this.mCountCheck > ((long) this.mTouchCheckCount)) {
                this.mSpeedParallelStartProxy = true;
            }
        } else if (this.mCountCheck > ((long) this.mNoTouchCheckCount)) {
            this.mSpeedParallelStartProxy = true;
        }
    }

    public void reportSysEvent(int event, int eventType) {
        if (event == 15016) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.i(TAG, "iaware_br install app: " + eventType);
            }
            if (eventType == 0) {
                this.mAwareInstallApp = true;
            } else if (eventType == 1) {
                this.mAwareInstallApp = false;
            } else {
                return;
            }
            if (this.mHandler.hasMessages(MSG_INSTALL_APP_TIMEOUT)) {
                this.mHandler.removeMessages(MSG_INSTALL_APP_TIMEOUT);
            }
            if (eventType == 0) {
                this.mHandler.sendEmptyMessageDelayed(MSG_INSTALL_APP_TIMEOUT, 5000);
            }
        } else if (event == 20011) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br dev status event screen on");
            }
            this.mAwareScreenOn = true;
        } else if (event == 90011) {
            if (AwareBroadcastDebug.getDebugDetail()) {
                AwareLog.d(TAG, "iaware_br dev status event screen off");
            }
            this.mAwareScreenOn = false;
            resetUnproxySpeedScreenOff();
        }
    }

    private void resetUnproxySpeedScreenOff() {
        if (isAwarePrepared()) {
            this.mBgAwareBr.setUnProxySpeedScreenOff();
            this.mFgAwareBr.setUnProxySpeedScreenOff();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setAwarePolicy(int type, int event) {
        if (type != 2) {
            if (type == 4) {
                if (event == 1) {
                    this.mAwareProxyActivitStart = true;
                } else if (event == 0) {
                    this.mAwareProxyActivitStart = false;
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(TAG, "don't process event " + event);
                }
            }
        } else if (event == 1) {
            this.mAwareProxySlip = true;
        } else if (event == 0) {
            this.mAwareProxySlip = false;
        } else if (AwareBroadcastDebug.getDebugDetail()) {
            AwareLog.d(TAG, "don't process event " + event);
        }
    }

    public boolean isSpeedNoCtrol() {
        return !isStrictCondition();
    }

    public boolean isScreenOff() {
        return !this.mAwareScreenOn;
    }

    private boolean isStrictCondition() {
        return this.mAwareProxySlip || this.mAwareProxyActivitStart;
    }

    public boolean isEmptyAwareBrList() {
        return this.mBgAwareBr.getAwareBrSize() == 0 && this.mFgAwareBr.getAwareBrSize() == 0;
    }

    public void setStartProxy(boolean startProxy) {
        this.mSpeedParallelStartProxy = startProxy;
    }

    public boolean getStartProxy() {
        return this.mSpeedParallelStartProxy;
    }

    private void dumpAwareBrAddPmsName(ArrayList<String> iawareDownloadingPkgs, ArraySet<Integer> iawareDownloadingUid, PackageManagerServiceEx pms) {
        Iterator<Integer> it = iawareDownloadingUid.iterator();
        while (it.hasNext()) {
            String name = pms.getNameForUid(it.next().intValue());
            if (name != null) {
                iawareDownloadingPkgs.add(name);
            }
        }
    }

    public void dumpAwareBr(PrintWriter pw) {
        pw.println("    feature enable :" + BroadcastFeature.isFeatureEnabled(10));
        synchronized (this.mLockNoProxyActions) {
            pw.println("    Default no proxy actions :" + this.mAwareNoProxyActions);
        }
        synchronized (this.mLockNoProxyPkgs) {
            pw.println("    Default no proxy pkgs :" + this.mAwareNoProxyPkgs);
        }
        synchronized (this.mLockUnProxySys) {
            pw.println("    Default unproxy sys :" + this.mAwareUnProxySys);
        }
        synchronized (this.mLockTrimActions) {
            pw.println("    Default trim action :" + this.mAwareTrimActions);
        }
        pw.println("    fg app level :" + this.mForegroundAppLevel);
        pw.println("    The receiver speed :" + this.mCountCheck);
        ArraySet<Integer> iawareDownloadingUid = new ArraySet<>();
        ArrayList<String> iawareDownloadingPkgs = new ArrayList<>();
        synchronized (this.mAwareDownloadingUid) {
            iawareDownloadingUid.addAll((ArraySet<? extends Integer>) this.mAwareDownloadingUid);
        }
        if (iawareDownloadingUid.size() > 0) {
            PackageManagerServiceEx pms = new PackageManagerServiceEx();
            if (!pms.isPmsNull()) {
                dumpAwareBrAddPmsName(iawareDownloadingPkgs, iawareDownloadingUid, pms);
            }
        }
        pw.println("    App Downloading:" + iawareDownloadingPkgs);
        StringBuilder sb = new StringBuilder();
        sb.append("    Screen:");
        sb.append(this.mAwareScreenOn ? "on" : "off");
        pw.println(sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append("    Operation: [");
        sb2.append(this.mAwareProxySlip ? "slip" : "");
        sb2.append(" ");
        sb2.append(this.mAwareProxyActivitStart ? "activityStart" : "");
        sb2.append("]");
        pw.println(sb2.toString());
        pw.println("    Proxy info:");
        this.mBgAwareBr.dump(pw);
        this.mFgAwareBr.dump(pw);
    }

    /* access modifiers changed from: private */
    public class AwareStateCallback implements AwareAppKeyBackgroup.IAwareStateCallback {
        private AwareStateCallback() {
        }

        /* synthetic */ AwareStateCallback(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.rms.iaware.appmng.AwareAppKeyBackgroup.IAwareStateCallback
        public void onStateChanged(int stateType, int eventType, int pid, int uid) {
            AwareBroadcastPolicy.this.updateProcessPolicy(stateType, eventType, pid, uid);
            if (BroadcastFeature.isFeatureEnabled(10) && stateType == 5 && uid >= 0) {
                if (eventType == 1) {
                    Message msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg.what = AwareBroadcastPolicy.MSG_POLICY_DL_START;
                    msg.arg1 = uid;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
                } else if (eventType == 2) {
                    Message msg2 = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg2.what = AwareBroadcastPolicy.MSG_POLICY_DL_END;
                    msg2.arg1 = uid;
                    AwareBroadcastPolicy.this.mHandler.sendMessageDelayed(msg2, 3000);
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(AwareBroadcastPolicy.TAG, "don't process type " + eventType);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class AwareSceneStateCallback implements AwareSceneRecognize.IAwareSceneRecCallback {
        private AwareSceneStateCallback() {
        }

        /* synthetic */ AwareSceneStateCallback(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.rms.iaware.appmng.AwareSceneRecognize.IAwareSceneRecCallback
        public void onStateChanged(int sceneType, int eventType, String pkg) {
            if (BroadcastFeature.isFeatureEnabled(10)) {
                if (sceneType == 2) {
                    Message msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg.what = AwareBroadcastPolicy.MSG_POLICY_SCENE_SLIP;
                    msg.arg1 = eventType;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
                } else if (sceneType == 4) {
                    Message msg2 = AwareBroadcastPolicy.this.mHandler.obtainMessage();
                    msg2.what = AwareBroadcastPolicy.MSG_POLICY_SCENE_ACTIVITY;
                    msg2.arg1 = eventType;
                    AwareBroadcastPolicy.this.mHandler.sendMessage(msg2);
                    if (AwareBroadcastPolicy.this.mHandler.hasMessages(AwareBroadcastPolicy.MSG_START_ACTIVITY_TIMEOUT)) {
                        AwareBroadcastPolicy.this.mHandler.removeMessages(AwareBroadcastPolicy.MSG_START_ACTIVITY_TIMEOUT);
                    }
                    if (eventType == 1) {
                        AwareBroadcastPolicy.this.mHandler.sendEmptyMessageDelayed(AwareBroadcastPolicy.MSG_START_ACTIVITY_TIMEOUT, 3000);
                    }
                } else if (AwareBroadcastDebug.getDebugDetail()) {
                    AwareLog.d(AwareBroadcastPolicy.TAG, "don't process scene type " + sceneType);
                }
            }
        }
    }

    public void notifyAwareUnproxyBr(int pid, int uid) {
        if (isAwarePrepared()) {
            this.mBgAwareBr.startUnproxyFgAppBroadcast(pid, uid);
            this.mFgAwareBr.startUnproxyFgAppBroadcast(pid, uid);
        }
    }

    public int getForegroundAppLevel() {
        return this.mForegroundAppLevel;
    }

    public boolean isInstallApp() {
        return this.mAwareInstallApp;
    }

    public boolean assemFilterBr(Intent intent, IntentFilter filter) {
        FilterStatus filterStatus;
        if (intent == null || filter == null) {
            return false;
        }
        boolean realFilter = false;
        Iterator<ActionFilterEntry> it = IntentFilterExt.actionFilterIterator(filter);
        if (it == null) {
            return false;
        }
        while (it.hasNext()) {
            ActionFilterEntry actionFilter = it.next();
            if (actionFilter.getAction() != null && actionFilter.getAction().equals(intent.getAction())) {
                String filterName = actionFilter.getFilterName();
                String filterValue = actionFilter.getFilterValue();
                if (!(filterName == null || filterValue == null || (filterStatus = this.mAwareFilterStatus.get(filterName)) == null)) {
                    realFilter = filterStatus.filter(intent, filterValue);
                    if (AwareBroadcastDebug.getFilterDebug()) {
                        AwareLog.i(TAG, "iaware_br, filterValue: " + filterValue + ", realFilter : " + realFilter + ", id:" + filter);
                    }
                    if (!realFilter) {
                        return realFilter;
                    }
                }
            } else if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.w(TAG, "iaware_br, action not match. ");
            }
        }
        return realFilter;
    }

    private abstract class FilterStatus {
        public abstract boolean filter(Intent intent, String str);

        private FilterStatus() {
        }

        /* synthetic */ FilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }
    }

    private class WifiNetFilterStatus extends FilterStatus {
        private WifiNetFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ WifiNetFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            int i = AnonymousClass1.$SwitchMap$android$net$NetworkInfo$State[AwareBroadcastPolicy.this.mWifiNetStatus.ordinal()];
            if (i == 1) {
                return !"WIFICON".equals(filterValue);
            }
            if (i == 2) {
                return !"WIFIDSCON".equals(filterValue);
            }
            if (i == 3) {
                return !AwareBroadcastPolicy.WIFI_STATUS_CONNECTING_VALUE.equals(filterValue);
            }
            if (i != 4) {
                return true;
            }
            return false;
        }
    }

    private class WifiFilterStatus extends FilterStatus {
        private WifiFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ WifiFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            int i = AwareBroadcastPolicy.this.mWifiStatus;
            if (i == 1) {
                return !AwareBroadcastPolicy.WIFI_STATUS_DISABLED_VALUE.equals(filterValue);
            }
            if (i == 3) {
                return !AwareBroadcastPolicy.WIFI_STATUS_ENABLED_VALUE.equals(filterValue);
            }
            if (i != 4) {
                return true;
            }
            return false;
        }
    }

    private class WifiSupFilterStatus extends FilterStatus {
        private WifiSupFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ WifiSupFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            int i = AnonymousClass1.$SwitchMap$android$net$wifi$SupplicantState[AwareBroadcastPolicy.this.mWifiSupStatus.ordinal()];
            if (i == 1) {
                return !AwareBroadcastPolicy.WIFI_SUP_STATUS_COMPLET_VALUE.equals(filterValue);
            }
            if (i == 2) {
                return !AwareBroadcastPolicy.WIFI_SUP_STATUS_DISCONNECT_VALUE.equals(filterValue);
            }
            if (i != 3) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.mtm.iaware.srms.AwareBroadcastPolicy$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$State = new int[NetworkInfo.State.values().length];
        static final /* synthetic */ int[] $SwitchMap$android$net$wifi$SupplicantState = new int[SupplicantState.values().length];

        static {
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.COMPLETED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$net$wifi$SupplicantState[SupplicantState.INVALID.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.CONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$State[NetworkInfo.State.UNKNOWN.ordinal()] = 4;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

    private class WifiRssiFilterStatus extends FilterStatus {
        private WifiRssiFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ WifiRssiFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            try {
                String[] values = filterValue.split(":");
                if (values.length != 2) {
                    return false;
                }
                int minRssi = Integer.parseInt(values[0]);
                int maxRssi = Integer.parseInt(values[1]);
                if (minRssi >= maxRssi) {
                    return false;
                }
                if (AwareBroadcastPolicy.this.mWifiRssi > maxRssi || AwareBroadcastPolicy.this.mWifiRssi < minRssi) {
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                AwareLog.e(AwareBroadcastPolicy.TAG, "iaware_br rssi value format is error");
                return false;
            }
        }
    }

    private class BatteryFilterStatus extends FilterStatus {
        private BatteryFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ BatteryFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x0039  */
        /* JADX WARNING: Removed duplicated region for block: B:32:0x0064  */
        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            boolean z;
            int hashCode = filterValue.hashCode();
            if (hashCode != -992200083) {
                if (hashCode != -693431679) {
                    if (hashCode == 427770174 && filterValue.equals(AwareBroadcastPolicy.BATTERY_STATUS_CHANGE_VALUE)) {
                        z = false;
                        if (z) {
                            return AwareBroadcastPolicy.this.mPrePlugedtype == AwareBroadcastPolicy.this.mPlugedtype && AwareBroadcastPolicy.this.mPrePlugedtype != -1;
                        }
                        if (z) {
                            return (AwareBroadcastPolicy.this.mCharging == 1 || AwareBroadcastPolicy.this.mCharging == -1) ? false : true;
                        }
                        if (!z) {
                            return true;
                        }
                        return (AwareBroadcastPolicy.this.mCharging == 0 || AwareBroadcastPolicy.this.mCharging == -1) ? false : true;
                    }
                } else if (filterValue.equals(AwareBroadcastPolicy.BATTERY_STATUS_CHARGING_OFF_VALUE)) {
                    z = true;
                    if (z) {
                    }
                }
            } else if (filterValue.equals(AwareBroadcastPolicy.BATTERY_STATUS_CHARGING_ON_VALUE)) {
                z = true;
                if (z) {
                }
            }
            z = true;
            if (z) {
            }
        }
    }

    private class BatteryLevelFilterStatus extends FilterStatus {
        private BatteryLevelFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ BatteryLevelFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            try {
                String[] values = filterValue.split(":");
                if (values.length != 2) {
                    return false;
                }
                int minLevel = Integer.parseInt(values[0]);
                int maxLevel = Integer.parseInt(values[1]);
                if (minLevel >= maxLevel) {
                    return false;
                }
                if (AwareBroadcastPolicy.this.mBatteryLevel < minLevel || AwareBroadcastPolicy.this.mBatteryLevel > maxLevel) {
                    return true;
                }
                return false;
            } catch (NumberFormatException e) {
                AwareLog.e(AwareBroadcastPolicy.TAG, "iaware_br level value format is error");
                return false;
            }
        }
    }

    private class ConnectivityFilterStatus extends FilterStatus {
        private ConnectivityFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ ConnectivityFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            int i = AwareBroadcastPolicy.this.mConnectStatus;
            if (i == -1) {
                return false;
            }
            if (i == 1) {
                return !"MOBILEDATACON".equals(filterValue);
            }
            if (i == 2) {
                return !"MOBILEDATADSCON".equals(filterValue);
            }
            if (i == 3) {
                return !AwareBroadcastPolicy.CONNECT_STATUS_WIFI_CON_VALUE.equals(filterValue);
            }
            if (i != 4) {
                return true;
            }
            return !AwareBroadcastPolicy.CONNECT_STATUS_WIFI_DSCON_VALUE.equals(filterValue);
        }
    }

    private class ExtraFilterStatus extends FilterStatus {
        private ExtraFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ ExtraFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            String[] extras = filterValue.split("[\\[\\]]");
            for (int i = 0; i < extras.length; i++) {
                if (!(extras[i] == null || extras[i].trim().length() == 0 || ":".equals(extras[i].trim()))) {
                    if (AwareBroadcastDebug.getDebug()) {
                        AwareLog.i(AwareBroadcastPolicy.TAG, "iaware_br compare extra: " + extras[i]);
                    }
                    String[] values = extras[i].split("[:@]");
                    if (values.length != 3) {
                        AwareLog.e(AwareBroadcastPolicy.TAG, "iaware_br extra value length is wrong.");
                        return false;
                    } else if (KeyWordController.matchReg(values[0], values[1], values[2], intent)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private class ScreenFilterStatus extends FilterStatus {
        private ScreenFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ ScreenFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            if (AwareBroadcastPolicy.SCREEN_STATUS_ON.equals(filterValue)) {
                return !AwareBroadcastPolicy.this.mScreenOn;
            }
            if (AwareBroadcastPolicy.SCREEN_STATUS_OFF.equals(filterValue)) {
                return AwareBroadcastPolicy.this.mScreenOn;
            }
            return true;
        }
    }

    private class DropFilterStatus extends FilterStatus {
        private DropFilterStatus() {
            super(AwareBroadcastPolicy.this, null);
        }

        /* synthetic */ DropFilterStatus(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.server.mtm.iaware.srms.AwareBroadcastPolicy.FilterStatus
        public boolean filter(Intent intent, String filterValue) {
            return AwareBroadcastPolicy.DROP.equals(filterValue);
        }
    }

    private NetworkInfo.State getWifiNetworkStatus(Intent intent) {
        Object obj = intent.getParcelableExtra("networkInfo");
        if (obj instanceof NetworkInfo) {
            return ((NetworkInfo) obj).getState();
        }
        return NetworkInfo.State.UNKNOWN;
    }

    private int getConnectNetworkStatus(Intent intent) {
        NetworkInfo info;
        int type = intent.getIntExtra("networkType", -1);
        if ((type != 0 && type != 1) || (info = (NetworkInfo) intent.getParcelableExtra("networkInfo")) == null) {
            return -1;
        }
        if (type == 0) {
            if (info.isConnected()) {
                return 1;
            }
            return 2;
        } else if (info.isConnected()) {
            return 3;
        } else {
            return 4;
        }
    }

    private int getWifiStatus(Intent intent) {
        return intent.getIntExtra("wifi_state", 4);
    }

    private int getWifiRssi(Intent intent) {
        return intent.getIntExtra("newRssi", -127);
    }

    private SupplicantState getWifiSupStatus(Intent intent) {
        Object obj = intent.getParcelableExtra("newState");
        if (obj instanceof SupplicantState) {
            return (SupplicantState) obj;
        }
        return SupplicantState.INVALID;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public void getStateFromSendBr(Intent intent) {
        String action;
        char c;
        if (intent != null && (action = intent.getAction()) != null) {
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case -1886648615:
                    if (action.equals("android.intent.action.ACTION_POWER_DISCONNECTED")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1875733435:
                    if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -1538406691:
                    if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case -1172645946:
                    if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -385684331:
                    if (action.equals("android.net.wifi.RSSI_CHANGED")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -343630553:
                    if (action.equals("android.net.wifi.STATE_CHANGE")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 233521600:
                    if (action.equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 1019184907:
                    if (action.equals("android.intent.action.ACTION_POWER_CONNECTED")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    this.mBatteryLevel = intent.getIntExtra(MemoryConstant.MEM_FILECACHE_ITEM_LEVEL, -1);
                    this.mPrePlugedtype = this.mPlugedtype;
                    this.mPlugedtype = intent.getIntExtra("plugged", -1);
                    return;
                case 1:
                    this.mCharging = 1;
                    return;
                case 2:
                    this.mCharging = 0;
                    return;
                case 3:
                    this.mWifiNetStatus = getWifiNetworkStatus(intent);
                    return;
                case 4:
                    this.mConnectStatus = getConnectNetworkStatus(intent);
                    return;
                case 5:
                    this.mWifiStatus = getWifiStatus(intent);
                    return;
                case 6:
                    this.mWifiSupStatus = getWifiSupStatus(intent);
                    return;
                case 7:
                    this.mWifiRssi = getWifiRssi(intent);
                    return;
                case '\b':
                    this.mScreenOn = true;
                    return;
                case '\t':
                    this.mScreenOn = false;
                    return;
                default:
                    return;
            }
        }
    }

    private void initConnectInfo(ConnectivityManager connectManager) {
        NetworkInfo connectInfo = connectManager.getActiveNetworkInfo();
        if (connectInfo == null || !connectInfo.isConnected()) {
            return;
        }
        if (connectInfo.getType() == 1) {
            this.mWifiNetStatus = NetworkInfo.State.CONNECTED;
            this.mConnectStatus = 3;
        } else if (connectInfo.getType() == 0) {
            this.mConnectStatus = 1;
        }
    }

    private void initState() {
        Context context = this.mContext;
        if (context != null) {
            Object objBatteryManager = context.getSystemService("batterymanager");
            Object objWifiManager = this.mContext.getSystemService(DevSchedFeatureRt.WIFI_FEATURE);
            Object objConnectManager = this.mContext.getSystemService("connectivity");
            if (objBatteryManager instanceof BatteryManager) {
                if (((BatteryManager) objBatteryManager).isCharging()) {
                    this.mCharging = 1;
                } else {
                    this.mCharging = 0;
                }
            }
            if (objWifiManager instanceof WifiManager) {
                this.mWifiStatus = ((WifiManager) objWifiManager).getWifiState();
            }
            if (objConnectManager instanceof ConnectivityManager) {
                initConnectInfo((ConnectivityManager) objConnectManager);
            }
        }
    }

    public enum BrCtrlType {
        NONE("do-nothing"),
        CACHEBR("cachebr"),
        DISCARDBR("discardbr"),
        DATADEFAULTBR("datadefaultbr");
        
        String mDescription;

        private BrCtrlType(String description) {
            this.mDescription = description;
        }

        public String description() {
            return this.mDescription;
        }
    }

    public int filterBr(Intent intent, AwareProcessInfo pInfo) {
        if (intent == null || pInfo == null) {
            return BrCtrlType.NONE.ordinal();
        }
        String action = intent.getAction();
        if (action == null) {
            return BrCtrlType.NONE.ordinal();
        }
        BrFilterPolicy brfilterPolicy = pInfo.getBrPolicy(intent);
        if (brfilterPolicy == null) {
            return BrCtrlType.NONE.ordinal();
        }
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter brfilterPolicy.getPolicy() : " + brfilterPolicy.getPolicy());
        }
        if (brfilterPolicy.getPolicy() == BrCtrlType.DATADEFAULTBR.ordinal()) {
            char c = 65535;
            switch (action.hashCode()) {
                case -1875733435:
                    if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                        c = 1;
                        break;
                    }
                    break;
                case -1538406691:
                    if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                        c = 3;
                        break;
                    }
                    break;
                case -343630553:
                    if (action.equals("android.net.wifi.STATE_CHANGE")) {
                        c = 0;
                        break;
                    }
                    break;
                case 233521600:
                    if (action.equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                        c = 2;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                return getWifiNetPolicy(action);
            }
            if (c == 1) {
                return getWifiPolicy(action);
            }
            if (c == 2) {
                return getWifiSupPolicy(action);
            }
            if (c == 3) {
                return getBatteryPolicy(action);
            }
        }
        return brfilterPolicy.getPolicy();
    }

    private int getWifiNetPolicy(String action) {
        int policy = BrCtrlType.NONE.ordinal();
        int dataPolicy = BroadcastExFeature.getBrFilterPolicy(action);
        if (dataPolicy == 1) {
            if (this.mWifiNetStatus == NetworkInfo.State.CONNECTED || this.mWifiNetStatus == NetworkInfo.State.DISCONNECTED) {
                return policy;
            }
            return BrCtrlType.DISCARDBR.ordinal();
        } else if (dataPolicy == 2) {
            return BrCtrlType.DISCARDBR.ordinal();
        } else {
            return policy;
        }
    }

    private int getWifiPolicy(String action) {
        int policy = BrCtrlType.NONE.ordinal();
        int dataPolicy = BroadcastExFeature.getBrFilterPolicy(action);
        if (dataPolicy == 1) {
            int i = this.mWifiStatus;
            if (i == 3 || i == 1) {
                return policy;
            }
            return BrCtrlType.DISCARDBR.ordinal();
        } else if (dataPolicy == 2) {
            return BrCtrlType.DISCARDBR.ordinal();
        } else {
            return policy;
        }
    }

    private int getWifiSupPolicy(String action) {
        int policy = BrCtrlType.NONE.ordinal();
        int dataPolicy = BroadcastExFeature.getBrFilterPolicy(action);
        if (dataPolicy == 1) {
            if (this.mWifiSupStatus == SupplicantState.COMPLETED || this.mWifiSupStatus == SupplicantState.DISCONNECTED) {
                return policy;
            }
            return BrCtrlType.DISCARDBR.ordinal();
        } else if (dataPolicy == 2) {
            return BrCtrlType.DISCARDBR.ordinal();
        } else {
            return policy;
        }
    }

    private int getBatteryPolicy(String action) {
        int policy = BrCtrlType.NONE.ordinal();
        if (BroadcastExFeature.getBrFilterPolicy(action) == 1 && this.mPrePlugedtype == this.mPlugedtype) {
            return BrCtrlType.DISCARDBR.ordinal();
        }
        return policy;
    }

    public boolean awareTrimAndEnqueueBr(boolean isParallel, HwBroadcastRecord record, boolean notify, int pid, String pkgName) {
        if (record == null) {
            return false;
        }
        boolean[] broadcastInfo = {isParallel, notify};
        AwareBroadcastCache brCache = sAwareBrCaches.get(record.getBrQueueName());
        if (brCache != null) {
            return brCache.awareTrimAndEnqueueBr(broadcastInfo, record, pid, pkgName, this);
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void unproxyCacheBr(int pid) {
        for (Map.Entry<String, AwareBroadcastCache> ent : sAwareBrCaches.entrySet()) {
            ent.getValue().unproxyCacheBr(pid);
        }
    }

    public void clearCacheBr(int pid) {
        for (Map.Entry<String, AwareBroadcastCache> ent : sAwareBrCaches.entrySet()) {
            ent.getValue().clearCacheBr(pid);
        }
    }

    public void updateProcessBrPolicy(AwareProcessInfo info, int state) {
        if (info != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = MSG_UPDATE_BR_POLICY;
            msg.arg1 = state;
            msg.obj = info;
            this.mHandler.sendMessage(msg);
        }
    }

    private final class AppIdleStateChangeListener extends UsageStatsManagerInternalEx.AppIdleStateChangeListenerEx {
        private AppIdleStateChangeListener() {
        }

        /* synthetic */ AppIdleStateChangeListener(AwareBroadcastPolicy x0, AnonymousClass1 x1) {
            this();
        }

        public void onAppIdleStateChanged(String packageName, int userId, boolean idle, int bucket, int reason) {
            Message msg = AwareBroadcastPolicy.this.mHandler.obtainMessage();
            msg.what = AwareBroadcastPolicy.MSG_APP_IDLE_STAT_CHANGE;
            if (idle) {
                msg.arg1 = 1;
            } else {
                msg.arg1 = 0;
            }
            msg.arg2 = userId;
            msg.obj = packageName;
            AwareBroadcastPolicy.this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAppIdleStat(int idleStat, int userId, String packageName) {
        ArrayList<AwareProcessInfo> procList = ProcessInfoCollector.getInstance().getAwareProcessInfosFromPackage(packageName, userId);
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter appidle:" + idleStat + ", packageName:" + packageName + ", userId:" + userId);
        }
        int count = procList.size();
        for (int i = 0; i < count; i++) {
            AwareProcessInfo info = procList.get(i);
            if (idleStat == 1) {
                ProcessInfoCollector.getInstance().setAwareProcessState(info.procPid, info.procProcInfo.mUid, 3);
            } else {
                ProcessInfoCollector.getInstance().setAwareProcessState(info.procPid, info.procProcInfo.mUid, 4);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateProcessPolicy(int stateType, int eventType, int pid, int uid) {
        if (stateType != 5) {
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "iaware_brFilter process status:" + stateType + ", eventType:" + eventType + ", pid:" + pid + ", uid:" + uid);
            }
            if (pid > 0) {
                ProcessInfoCollector.getInstance().setAwareProcessState(pid, uid, -1);
            } else {
                ProcessInfoCollector.getInstance().setAwareProcessStateByUid(pid, uid, -1);
            }
        }
    }

    public void reportGoogleConn(boolean conn) {
        Message msg = this.mHandler.obtainMessage();
        msg.what = MSG_GOOGLE_CONN_STAT_CHANGE;
        if (conn) {
            msg.arg1 = 1;
        } else {
            msg.arg1 = -1;
        }
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetGoogleConnStat(int connStat) {
        boolean conn = false;
        if (connStat == 1) {
            conn = true;
        }
        if (sGoogleConnStat != conn) {
            sGoogleConnStat = conn;
            if (AwareBroadcastDebug.getFilterDebug()) {
                AwareLog.i(TAG, "iaware_brFilter google conn stat change:" + sGoogleConnStat);
            }
            ArraySet<String> googleAppPkgs = BroadcastExFeature.getBrGoogleAppList();
            int count = googleAppPkgs.size();
            for (int i = 0; i < count; i++) {
                ArrayList<AwareProcessInfo> procList = ProcessInfoCollector.getInstance().getAwareProcessInfosFromPackage(googleAppPkgs.valueAt(i), -1);
                int countProc = procList.size();
                for (int j = 0; j < countProc; j++) {
                    AwareProcessInfo info = procList.get(j);
                    ProcessInfoCollector.getInstance().setAwareProcessState(info.procPid, info.procProcInfo.mUid, -1);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateBrPolicy(int state, AwareProcessInfo info) {
        info.updateBrPolicy();
        if (state == 2 || state == 4 || state == 8 || state == 6) {
            int currentState = info.getState();
            if (currentState == 10 || currentState == 9) {
                unproxyCacheBr(info.procPid);
            }
        }
    }

    public static int getGoogleConnStat() {
        if (sGoogleConnStat) {
            return 1;
        }
        return -1;
    }

    public void notifyOverFlow(int pid) {
        if (AwareBroadcastDebug.getFilterDebug()) {
            AwareLog.i(TAG, "iaware_brFilter notifyOverFlow: " + pid);
        }
        Message msg = this.mHandler.obtainMessage();
        msg.what = MSG_NOTIFY_OVERFLOW;
        msg.arg1 = pid;
        this.mHandler.sendMessage(msg);
    }

    public void dumpAwareFilterBr(PrintWriter pw) {
        pw.println("    BrFilterfeature enable :" + BroadcastExFeature.isFeatureEnabled(1));
        pw.println("    process policy :");
        ArrayList<AwareProcessInfo> listProcess = ProcessInfoCollector.getInstance().getAwareProcessInfoList();
        int size = listProcess.size();
        for (int i = 0; i < size; i++) {
            AwareProcessInfo info = listProcess.get(i);
            pw.println("      process pid : " + info.procPid + ", process name : " + info.procProcInfo.mProcessName + ", process adj:" + info.procProcInfo.mCurAdj);
            for (Map.Entry<String, BrFilterPolicy> ent : info.getBrFilterPolicyMap().entrySet()) {
                pw.println("         policy action : " + ent.getKey() + ", value : " + ent.getValue().getPolicy() + ", state : " + ent.getValue().getProcessState());
            }
        }
        pw.println("    br before filter count :" + AwareBroadcastDumpRadar.getBrBeforeCount());
        pw.println("    br after filter count :" + AwareBroadcastDumpRadar.getBrAfterCount());
        pw.println("    br noprocess filter count :" + AwareBroadcastDumpRadar.getBrNoProcessCount());
        pw.println("    br system_server nodrop count :" + AwareBroadcastDumpRadar.getSsNoDropCount());
        pw.println("    br persistent app nodrop count :" + AwareBroadcastDumpRadar.getPerAppNoDropCount());
        pw.println("    Default white list :" + BroadcastExFeature.getBrFilterWhiteList());
        pw.println("    Default white actionAPP :" + BroadcastExFeature.getBrFilterWhiteApp());
        pw.println("    Default black actionAPP :" + BroadcastExFeature.getBrFilterBlackApp());
        pw.println("    google app list :" + BroadcastExFeature.getBrGoogleAppList());
        AwareBroadcastDumpRadar radar = MultiTaskManagerService.self().getAwareBrRadar();
        if (radar != null) {
            pw.println("    brfilter detail :");
            HashMap<String, Integer> brFilter = radar.getBrFilterDetail();
            if (brFilter != null) {
                for (Map.Entry<String, Integer> ent2 : brFilter.entrySet()) {
                    pw.println("         " + ent2.getKey() + ", count , " + ent2.getValue());
                }
            }
        }
    }
}
