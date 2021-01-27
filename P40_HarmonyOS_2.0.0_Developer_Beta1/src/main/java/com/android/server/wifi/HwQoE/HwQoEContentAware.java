package com.android.server.wifi.HwQoE;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AppTypeRecoManager;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.HwQoeUdpNetworkInfo;
import com.android.server.wifi.wifipro.HwWifiProServiceManager;
import com.huawei.android.app.ActivityManagerEx;
import java.util.List;

public class HwQoEContentAware {
    private static final int APP_NAME_FILED = 2;
    private static final int BITS_PER_BYTE = 8;
    private static final int CONVERTION_UNIT = 1024;
    private static final String[] DEFAULT_GAME_NAMES = {"com.huawei.gamebox"};
    private static final int MICROSECONDS_PER_SECOND = 1000;
    private static final int MSG_APP_UDP_MONITOR = 1;
    private static final int MSG_FOREGROUND_APP_CHANGED = 2;
    private static final int SECOND_INTERVAL = 10;
    private static final int STAMP_VALID = 1;
    private static final String TAG = "HiDATA_ContentAware";
    private static final long UDP_ACCESS_MONITOR_INTERVAL = 2000;
    private static HwQoEContentAware mHwQoEContentAware;
    private boolean isBroadcastRegisted;
    private boolean isCallbackNotified;
    private boolean isSensitiveApp;
    private ActivityManager mActivityManager = ((ActivityManager) this.mContext.getSystemService("activity"));
    private int mAppSensitivityScore;
    private AppTypeRecoManager mAppTypeRecoManager = AppTypeRecoManager.getInstance();
    private IHwQoEContentAwareCallback mCallback;
    private Context mContext;
    private int mCurrMoniorUid;
    private int mCurrMoniorscore;
    private HwQoeUdpNetworkInfo mCurrUdpInfoForMonitor;
    private String mForegroundAppPackageName;
    private String mForegroundName;
    private HwProcessObserver mHwProcessObserver;
    private Handler mHwQoEContentAwareHandler;
    private HwQoEJNIAdapter mHwQoEJNIAdapter = HwQoEJNIAdapter.getInstance();
    private HwQoEWifiPolicyConfigManager mHwQoEWifiPolicyConfigManager = HwQoEWifiPolicyConfigManager.getInstance(this.mContext);
    private HwWifiProServiceManager mHwWifiProServiceManager;
    private HwQoeUdpNetworkInfo mLastUdpInfoForMonitor;
    private int mMonitorNetwork = -1;
    private PackageManager mPackageManager = this.mContext.getPackageManager();
    private String mPackageName;

    private HwQoEContentAware(Context context, IHwQoEContentAwareCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
        this.mHwWifiProServiceManager = HwWifiProServiceManager.createHwWifiProServiceManager(context);
        initHwQoEContentAwareHandler();
    }

    public static HwQoEContentAware createInstance(Context context, IHwQoEContentAwareCallback callback) {
        if (mHwQoEContentAware == null) {
            mHwQoEContentAware = new HwQoEContentAware(context, callback);
        }
        return mHwQoEContentAware;
    }

    public static HwQoEContentAware getInstance() {
        return mHwQoEContentAware;
    }

    public void setAppStateMonitorEnabled(boolean enabled, String packageName, int network) {
        logD(false, "setAppStateMonitorEnabled, enabled: %{public}s, packageName: %{public}s,network: %{public}d, mMonitorNetwork: %{public}d", String.valueOf(enabled), packageName, Integer.valueOf(network), Integer.valueOf(this.mMonitorNetwork));
        if (!enabled) {
            if (this.isCallbackNotified) {
                this.isCallbackNotified = false;
                this.mCallback.onSensitiveAppStateChange(this.mCurrMoniorUid, 0, true);
            }
            this.mPackageName = null;
            this.mCurrMoniorUid = 0;
            this.mAppSensitivityScore = 0;
            this.mMonitorNetwork = -1;
            this.mHwQoEContentAwareHandler.removeMessages(1);
        } else if (this.mMonitorNetwork != network) {
            this.mPackageName = packageName;
            this.mCurrMoniorUid = getAppUid(this.mPackageName);
            this.mAppSensitivityScore = 0;
            this.mMonitorNetwork = network;
            this.mHwQoEContentAwareHandler.removeMessages(1);
            this.mHwQoEContentAwareHandler.sendEmptyMessageDelayed(1, UDP_ACCESS_MONITOR_INTERVAL);
        } else {
            logD(false, "mMonitorNetwork != network, do nothing.", new Object[0]);
        }
    }

    public void updateWifiSleepWhiteList(int type, List<String> packageWhiteList) {
        if (type == 7) {
            this.mHwQoEWifiPolicyConfigManager.updateWifiSleepWhiteList(packageWhiteList);
        }
    }

    public boolean isLiveStreamApp(int uid) {
        int noBgLimit = this.mHwQoEWifiPolicyConfigManager.queryWifiSleepTime(getAppNameUid(uid));
        if (this.mAppTypeRecoManager.getAppType(getAppNameUid(uid)) != 21 && noBgLimit != 1000) {
            return false;
        }
        logD(false, "isLiveStreamApp,uid:%{public}d", Integer.valueOf(uid));
        return true;
    }

    public boolean isDownloadApp(int uid) {
        String appName = this.mContext.getPackageManager().getNameForUid(uid);
        int bgLimit = this.mHwQoEWifiPolicyConfigManager.queryWifiSleepTime(appName);
        if (this.mAppTypeRecoManager.getAppType(appName) != 21 && bgLimit != 2000) {
            return false;
        }
        logD(false, "isDownloadApp,uid:%{public}d", Integer.valueOf(uid));
        return true;
    }

    /* access modifiers changed from: private */
    public class HwProcessObserver extends IProcessObserver.Stub {
        private HwProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                if (HwQoEContentAware.this.mHwQoEContentAwareHandler.hasMessages(2)) {
                    HwQoEContentAware.this.mHwQoEContentAwareHandler.removeMessages(2);
                }
                HwQoEContentAware hwQoEContentAware = HwQoEContentAware.this;
                hwQoEContentAware.mForegroundAppPackageName = hwQoEContentAware.getAppNameUid(uid);
                HwQoEContentAware hwQoEContentAware2 = HwQoEContentAware.this;
                hwQoEContentAware2.logD(false, "mForegroundAppPackageName: %{public}s, score: %{public}d", hwQoEContentAware2.mForegroundAppPackageName, Integer.valueOf(HwQoEContentAware.this.mAppSensitivityScore));
                HwQoEContentAware hwQoEContentAware3 = HwQoEContentAware.this;
                hwQoEContentAware3.handleForegroundAppWifiSleepChange(hwQoEContentAware3.mForegroundAppPackageName, true);
                if (!TextUtils.isEmpty(HwQoEContentAware.this.mForegroundAppPackageName)) {
                    HwQoEContentAware.this.mHwWifiProServiceManager.notifyForegroundAppChanged(HwQoEContentAware.this.mForegroundAppPackageName);
                }
            }
            if (uid > 0 && !TextUtils.isEmpty(HwQoEContentAware.this.mPackageName)) {
                if (foregroundActivities && HwQoEContentAware.this.mPackageName.equals(HwQoEContentAware.this.getAppNameUid(uid))) {
                    HwQoEContentAware hwQoEContentAware4 = HwQoEContentAware.this;
                    hwQoEContentAware4.logD(false, "SensitiveApp is foregroundActivities, currMoniorUid:%{public}d", Integer.valueOf(hwQoEContentAware4.mCurrMoniorUid));
                    HwQoEContentAware.this.isSensitiveApp = true;
                    HwQoEContentAware.this.mCurrMoniorUid = uid;
                    if (!HwQoEContentAware.this.mHwQoEContentAwareHandler.hasMessages(1)) {
                        HwQoEContentAware.this.mHwQoEContentAwareHandler.sendEmptyMessage(1);
                    }
                    if (HwQoEContentAware.this.isCallbackNotified) {
                        HwQoEContentAware.this.mCallback.onSensitiveAppStateChange(HwQoEContentAware.this.mCurrMoniorUid, 2, false);
                    }
                } else if (!HwQoEContentAware.this.isSensitiveApp || foregroundActivities || !HwQoEContentAware.this.mPackageName.equals(HwQoEContentAware.this.getAppNameUid(uid))) {
                    HwQoEContentAware.this.logD(false, "not foreground activity and sensitive app.", new Object[0]);
                } else {
                    HwQoEContentAware hwQoEContentAware5 = HwQoEContentAware.this;
                    hwQoEContentAware5.mForegroundName = hwQoEContentAware5.getForegroundActivity();
                    HwQoEContentAware hwQoEContentAware6 = HwQoEContentAware.this;
                    hwQoEContentAware6.logD(false, "SensitiveApp is BackgroundActivities,isCallbackNotified:%{public}s, mForegroundName:%{public}s", String.valueOf(hwQoEContentAware6.isCallbackNotified), HwQoEContentAware.this.mForegroundName);
                    if (!TextUtils.isEmpty(HwQoEContentAware.this.mForegroundName)) {
                        String str = HwQoEContentAware.this.mForegroundName;
                        HwQoEContentAware hwQoEContentAware7 = HwQoEContentAware.this;
                        if (str.equals(hwQoEContentAware7.getAppNameUid(hwQoEContentAware7.mCurrMoniorUid))) {
                            HwQoEContentAware.this.logD(false, "SensitiveApp is Not BackgroundActivities", new Object[0]);
                            return;
                        }
                    }
                    HwQoEContentAware.this.isSensitiveApp = false;
                    if (HwQoEContentAware.this.isCallbackNotified) {
                        HwQoEContentAware.this.mCallback.onSensitiveAppStateChange(HwQoEContentAware.this.mCurrMoniorUid, 2, true);
                    } else if (HwQoEContentAware.this.mHwQoEContentAwareHandler.hasMessages(1)) {
                        HwQoEContentAware.this.mHwQoEContentAwareHandler.removeMessages(1);
                    }
                }
            }
        }

        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
        }

        public void onProcessDied(int pid, int uid) {
            if (HwQoEContentAware.this.isSensitiveApp && uid == HwQoEContentAware.this.mCurrMoniorUid) {
                HwQoEContentAware hwQoEContentAware = HwQoEContentAware.this;
                hwQoEContentAware.mForegroundName = hwQoEContentAware.getForegroundActivity();
            }
        }
    }

    private boolean checkUdpAccessScoreParam(HwQoeUdpNetworkInfo currUdpInfo, HwQoeUdpNetworkInfo lastUdpInfo) {
        if (currUdpInfo == null) {
            logD(false, "currUdpInfo is null", new Object[0]);
            return false;
        } else if (lastUdpInfo == null) {
            logD(false, "lastUdpInfo is null", new Object[0]);
            return false;
        } else if (currUdpInfo.getUid() != lastUdpInfo.getUid()) {
            logD(false, "uid is error,ignore calculate score", new Object[0]);
            return false;
        } else if (currUdpInfo.getNetwork() == lastUdpInfo.getNetwork()) {
            return true;
        } else {
            logD(false, "Network is error,ignore calculate score", new Object[0]);
            return false;
        }
    }

    /* JADX INFO: Multiple debug info for r4v3 long: [D('timestamp' long), D('outSpeed' long)] */
    /* JADX INFO: Multiple debug info for r4v4 long: [D('inSpeed' long), D('outSpeed' long)] */
    /* JADX INFO: Multiple debug info for r10v3 long: [D('txTcpBytes' long), D('outTcpSpeed' long)] */
    /* JADX INFO: Multiple debug info for r6v4 long: [D('inTcpSpeed' long), D('rxTcpBytes' long)] */
    private int calculateNewUdpAccessScore(HwQoeUdpNetworkInfo currUdpInfo, HwQoeUdpNetworkInfo lastUdpInfo) {
        long timestamp;
        HwQoEContentAware hwQoEContentAware;
        int score;
        HwQoEContentAware hwQoEContentAware2;
        if (!checkUdpAccessScoreParam(currUdpInfo, lastUdpInfo)) {
            return 0;
        }
        long timestamp2 = currUdpInfo.getTimestamp() - lastUdpInfo.getTimestamp();
        if (timestamp2 <= 0) {
            hwQoEContentAware = this;
            timestamp = timestamp2;
        } else if (timestamp2 > 10000) {
            hwQoEContentAware = this;
            timestamp = timestamp2;
        } else {
            long txUdpPackets = currUdpInfo.getTxUdpPackets() - lastUdpInfo.getTxUdpPackets();
            long rxUdpPackets = currUdpInfo.getRxUdpPackets() - lastUdpInfo.getRxUdpPackets();
            long rxUdpBytes = currUdpInfo.getRxUdpBytes() - lastUdpInfo.getRxUdpBytes();
            long txUdpBytes = currUdpInfo.getTxUdpBytes() - lastUdpInfo.getTxUdpBytes();
            long socketNum = (long) currUdpInfo.getUidUdpSockets();
            long rxTcpBytes = currUdpInfo.mRxTcpBytes - lastUdpInfo.mRxTcpBytes;
            long rxTcpPackets = currUdpInfo.mRxTcpPackets - lastUdpInfo.mRxTcpPackets;
            long txTcpBytes = currUdpInfo.mTxTcpBytes - lastUdpInfo.mTxTcpBytes;
            long txTcpPackets = currUdpInfo.mTxTcpPackets - lastUdpInfo.mTxTcpPackets;
            long stamp = timestamp2 / 1000;
            if (stamp == 0) {
                stamp = 1;
            }
            long outSpeed = (txUdpBytes / (stamp * 1024)) * 8;
            long outSpeed2 = (rxUdpBytes / (stamp * 1024)) * 8;
            long outTcpSpeed = (txTcpBytes / (stamp * 1024)) * 8;
            long rxTcpBytes2 = (rxTcpBytes / (1024 * stamp)) * 8;
            if ((rxUdpPackets + txUdpPackets) / stamp > 10 || (outTcpSpeed + rxTcpBytes2) / stamp > 10) {
                score = 0 + 1;
            } else {
                score = 0 - 1;
            }
            if (socketNum == 0 && rxUdpPackets + txUdpPackets == 0 && txTcpPackets + rxTcpPackets == 0) {
                hwQoEContentAware2 = this;
                score = 0 - hwQoEContentAware2.mAppSensitivityScore;
            } else {
                hwQoEContentAware2 = this;
            }
            if (hwQoEContentAware2.mCallback != null && isWechartCalling()) {
                if (outSpeed2 != 0 || rxTcpBytes2 == 0) {
                    hwQoEContentAware2.mCallback.onPeriodSpeed(outSpeed, outSpeed2);
                } else {
                    hwQoEContentAware2.mCallback.onPeriodSpeed(outTcpSpeed, rxTcpBytes2);
                }
            }
            return score;
        }
        hwQoEContentAware.logD(false, "[timestamp]: %{public}s", String.valueOf(timestamp));
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppUdpMonitor() {
        if (this.isSensitiveApp || isWechartCalling()) {
            int i = this.mMonitorNetwork;
            if (i == 1 || i == 0) {
                this.mCurrUdpInfoForMonitor = this.mHwQoEJNIAdapter.getUdpNetworkStatsDetail(this.mCurrMoniorUid, this.mMonitorNetwork);
            }
            if (this.mLastUdpInfoForMonitor == null) {
                this.mLastUdpInfoForMonitor = new HwQoeUdpNetworkInfo();
            } else {
                handleWechatUdpInfoChange();
            }
            this.mLastUdpInfoForMonitor.setUdpNetWorkInfo(this.mCurrUdpInfoForMonitor);
            if (!this.mHwQoEContentAwareHandler.hasMessages(1)) {
                this.mHwQoEContentAwareHandler.sendEmptyMessageDelayed(1, UDP_ACCESS_MONITOR_INTERVAL);
            }
        } else if (this.isCallbackNotified) {
            this.isCallbackNotified = false;
            this.mCallback.onSensitiveAppStateChange(this.mCurrMoniorUid, 0, true);
        }
    }

    private void initHwQoEContentAwareHandler() {
        HandlerThread handlerThread = new HandlerThread("hw_qoe_contentaware_thread");
        handlerThread.start();
        this.mHwQoEContentAwareHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.wifi.HwQoE.HwQoEContentAware.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 1) {
                    HwQoEContentAware.this.handleAppUdpMonitor();
                } else if (i == 2) {
                    HwQoEContentAware hwQoEContentAware = HwQoEContentAware.this;
                    hwQoEContentAware.handleForegroundAppWifiSleepChange(hwQoEContentAware.mForegroundAppPackageName, false);
                }
            }
        };
    }

    private void registerProcessObserver() {
        if (!this.isBroadcastRegisted) {
            this.isBroadcastRegisted = true;
            this.mHwProcessObserver = new HwProcessObserver();
            logD(false, "registerProcessObserver", new Object[0]);
            try {
                ActivityManagerNative.getDefault().registerProcessObserver(this.mHwProcessObserver);
            } catch (RemoteException e) {
                logD(false, "register process observer failed,%{public}s", e.getMessage());
            }
        }
    }

    public int getForegroundAppUid() {
        return getAppUid(getForegroundActivity());
    }

    public int getAppUid(String processName) {
        if (TextUtils.isEmpty(processName)) {
            return -1;
        }
        try {
            ApplicationInfo ai = this.mPackageManager.getApplicationInfo(processName, 1);
            if (ai == null) {
                return -1;
            }
            int uid = ai.uid;
            logD(false, "packageName = %{public}s, uid = %{public}d", processName, Integer.valueOf(uid));
            return uid;
        } catch (PackageManager.NameNotFoundException e) {
            logD(false, "NameNotFoundException: %{public}s", e.getMessage());
            return -1;
        }
    }

    public String getAppNameUid(int uid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.uid == uid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized String getForegroundActivity() {
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            if (!runningTaskInfos.isEmpty()) {
                ActivityManager.RunningTaskInfo mRunningTask = runningTaskInfos.get(0);
                if (mRunningTask == null) {
                    logD(false, "failed to get RunningTaskInfo", new Object[0]);
                    return "";
                }
                return mRunningTask.topActivity.getPackageName();
            }
        }
        logD(false, "running task is null, ams is abnormal!!!", new Object[0]);
        return "";
    }

    public void queryForegroundAppType() {
        handleForegroundAppWifiSleepChange(getForegroundActivity(), true);
    }

    public void onSystemBootCompled() {
        registerProcessObserver();
    }

    private void handleWechatUdpInfoChange() {
        this.mCurrMoniorscore = calculateNewUdpAccessScore(this.mCurrUdpInfoForMonitor, this.mLastUdpInfoForMonitor);
        int i = this.mAppSensitivityScore;
        int i2 = this.mCurrMoniorscore;
        this.mAppSensitivityScore = i + i2;
        int i3 = this.mAppSensitivityScore;
        if (i3 < 0 || (i3 >= 2 && i2 <= 0)) {
            this.mAppSensitivityScore = 0;
        }
        if (!this.isCallbackNotified && this.mAppSensitivityScore >= 2 && isWechartCalling()) {
            this.isCallbackNotified = true;
            this.mCallback.onSensitiveAppStateChange(this.mCurrMoniorUid, 1, false);
        } else if (this.isCallbackNotified && this.mAppSensitivityScore == 0 && !isWechartCalling()) {
            this.isCallbackNotified = false;
            this.mCallback.onSensitiveAppStateChange(this.mCurrMoniorUid, 0, false);
            if (!this.isSensitiveApp && this.mHwQoEContentAwareHandler.hasMessages(1)) {
                this.mHwQoEContentAwareHandler.removeMessages(1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleForegroundAppWifiSleepChange(String packageName, boolean retry) {
        if (!TextUtils.isEmpty(packageName)) {
            int sleepTime = this.mHwQoEWifiPolicyConfigManager.queryWifiSleepTime(packageName);
            int type = this.mAppTypeRecoManager.getAppType(packageName);
            if (sleepTime != -1) {
                this.mCallback.onForegroundAppWifiSleepChange(true, sleepTime, type, packageName);
            } else if (type != -1 || !retry) {
                this.mCallback.onForegroundAppWifiSleepChange(false, sleepTime, type, packageName);
            } else if (!this.mHwQoEContentAwareHandler.hasMessages(2)) {
                this.mHwQoEContentAwareHandler.sendEmptyMessageDelayed(2, UDP_ACCESS_MONITOR_INTERVAL);
            }
            this.mCallback.onForegroundAppTypeChange(type, packageName);
        }
    }

    public boolean isGameType(int type, String packageName) {
        if (packageName == null) {
            return false;
        }
        if (isGameTypeForRecoManager(type)) {
            return true;
        }
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (isDefaultGameType(packageName)) {
            return true;
        }
        if (packageName.contains(":") && isGameTypeForRecoManager(this.mAppTypeRecoManager.getAppType(getRealAppName(packageName)))) {
            return true;
        }
        if (HwQoEUtils.GAME_ASSISIT_ENABLE) {
            return ActivityManagerEx.isInGameSpace(packageName);
        }
        return false;
    }

    public String getRealAppName(String appName) {
        String realName = "";
        if (!TextUtils.isEmpty(appName) && appName.contains(":")) {
            String[] appNames = appName.split(":", 2);
            if (appNames.length > 0) {
                realName = appNames[0];
            }
        }
        logD(false, "RealAppName:%{public}s", realName);
        return realName;
    }

    public boolean isGameTypeForRecoManager(int type) {
        if (type == 305 || type == 9) {
            return true;
        }
        return false;
    }

    private boolean isDefaultGameType(String appName) {
        if (!TextUtils.isEmpty(appName)) {
            for (String name : DEFAULT_GAME_NAMES) {
                if (appName.startsWith(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logD(boolean isFmtStrPrivate, String log, Object... args) {
        HwHiLog.d(TAG, isFmtStrPrivate, log, args);
    }

    private boolean isWechartCalling() {
        AudioManager audioManager = (AudioManager) this.mContext.getSystemService("audio");
        if (audioManager == null) {
            logD(false, "audioManager is null", new Object[0]);
            return false;
        }
        int mode = audioManager.getMode();
        logD(false, "isWechartCalling mode = %{public}d", Integer.valueOf(mode));
        if (mode == 3) {
            return true;
        }
        return false;
    }
}
