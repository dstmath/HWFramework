package com.android.server.wifi.HwQoE;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver.Stub;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AppTypeRecoManager;
import android.text.TextUtils;
import com.huawei.android.app.ActivityManagerEx;
import java.util.List;

public class HwQoEContentAware {
    private static final String[] DEFAULT_GAME_NAMES = new String[]{"com.huawei.gamebox"};
    private static final int MSG_APP_UDP_MONITOR = 1;
    private static final int MSG_FOREGROUND_APP_CHANGED = 2;
    private static final String TAG = "HwQoEContentAware";
    private static final long UDP_ACCESS_MONITOR_INTERVAL = 3000;
    private static HwQoEContentAware mHwQoEContentAware;
    private boolean isAppStateMonitorEnabled;
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
    private HwQoEUdpNetWorkInfo mCurrUdpInfoForMonitor;
    private String mForegroundAppPackageName;
    private String mForegroundName;
    private HwProcessObserver mHwProcessObserver;
    private Handler mHwQoEContentAwareHandler;
    private HwQoEJNIAdapter mHwQoEJNIAdapter = HwQoEJNIAdapter.getInstance();
    private HwQoEWifiPolicyConfigManager mHwQoEWifiPolicyConfigManager = HwQoEWifiPolicyConfigManager.getInstance(this.mContext);
    private HwQoEUdpNetWorkInfo mLastUdpInfoForMonitor;
    private PackageManager mPackageManager = this.mContext.getPackageManager();
    private String mPackageName;

    private class HwProcessObserver extends Stub {
        /* synthetic */ HwProcessObserver(HwQoEContentAware this$0, HwProcessObserver -this1) {
            this();
        }

        private HwProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                if (HwQoEContentAware.this.mHwQoEContentAwareHandler.hasMessages(2)) {
                    HwQoEContentAware.this.mHwQoEContentAwareHandler.removeMessages(2);
                }
                HwQoEContentAware.this.mForegroundAppPackageName = HwQoEContentAware.this.getAppNameUid(uid);
                HwQoEUtils.logD("mForegroundAppPackageName: " + HwQoEContentAware.this.mForegroundAppPackageName + " ,score: " + HwQoEContentAware.this.mAppSensitivityScore);
                HwQoEContentAware.this.handleForegroundAppWifiSleepChange(HwQoEContentAware.this.mForegroundAppPackageName, true);
            }
            if (uid > 0 && !TextUtils.isEmpty(HwQoEContentAware.this.mPackageName)) {
                if (foregroundActivities && HwQoEContentAware.this.mPackageName.equals(HwQoEContentAware.this.getAppNameUid(uid))) {
                    HwQoEUtils.logD("SensitiveApp is foregroundActivities, isAppStateMonitorEnabled = " + HwQoEContentAware.this.isAppStateMonitorEnabled + ",currMoniorUid:" + HwQoEContentAware.this.mCurrMoniorUid);
                    HwQoEContentAware.this.isSensitiveApp = true;
                    HwQoEContentAware.this.mCurrMoniorUid = uid;
                    if (HwQoEContentAware.this.isAppStateMonitorEnabled && (HwQoEContentAware.this.mHwQoEContentAwareHandler.hasMessages(1) ^ 1) != 0) {
                        HwQoEContentAware.this.mHwQoEContentAwareHandler.sendEmptyMessage(1);
                    }
                } else if (HwQoEContentAware.this.isSensitiveApp && (foregroundActivities ^ 1) != 0 && HwQoEContentAware.this.mPackageName.equals(HwQoEContentAware.this.getAppNameUid(uid))) {
                    HwQoEContentAware.this.mForegroundName = HwQoEContentAware.this.getForegroundActivity();
                    HwQoEUtils.logD("SensitiveApp is BackgroundActivities,isCallbackNotified:" + HwQoEContentAware.this.isCallbackNotified + ", mForegroundName:" + HwQoEContentAware.this.mForegroundName);
                    if (TextUtils.isEmpty(HwQoEContentAware.this.mForegroundName) || !HwQoEContentAware.this.mForegroundName.equals(HwQoEContentAware.this.getAppNameUid(HwQoEContentAware.this.mCurrMoniorUid))) {
                        HwQoEContentAware.this.isSensitiveApp = false;
                        if (HwQoEContentAware.this.mHwQoEContentAwareHandler.hasMessages(1)) {
                            HwQoEContentAware.this.mHwQoEContentAwareHandler.removeMessages(1);
                        }
                        if (HwQoEContentAware.this.isCallbackNotified) {
                            HwQoEContentAware.this.isCallbackNotified = false;
                            HwQoEContentAware.this.mAppSensitivityScore = 0;
                            HwQoEContentAware.this.mCallback.onSensitiveAppStateChange(HwQoEContentAware.this.mCurrMoniorUid, 0, true);
                        }
                    } else {
                        HwQoEUtils.logD("SensitiveApp is Not BackgroundActivities");
                    }
                }
            }
        }

        public void onProcessDied(int pid, int uid) {
            if (HwQoEContentAware.this.isSensitiveApp && uid == HwQoEContentAware.this.mCurrMoniorUid) {
                HwQoEContentAware.this.mForegroundName = HwQoEContentAware.this.getForegroundActivity();
                HwQoEUtils.logD("onProcessDied,  foregroundName:" + HwQoEContentAware.this.mForegroundName);
            }
        }
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
        HwQoEUtils.logD("setAppStateMonitorEnabled, enabled: " + enabled + ", packageName: " + packageName + ",network:" + network);
        if (!enabled) {
            this.mPackageName = null;
            this.mCurrMoniorUid = 0;
            this.isCallbackNotified = false;
            this.mAppSensitivityScore = 0;
            if (this.mHwQoEContentAwareHandler.hasMessages(1)) {
                this.mHwQoEContentAwareHandler.removeMessages(1);
            }
        } else if (!TextUtils.isEmpty(packageName) && (network == 0 || network == 1)) {
            this.mPackageName = packageName;
            this.mCurrMoniorUid = getAppUid(this.mPackageName);
            if (this.mPackageName.equals(getForegroundActivity()) && (this.mHwQoEContentAwareHandler.hasMessages(1) ^ 1) != 0) {
                this.mHwQoEContentAwareHandler.sendEmptyMessage(1);
            }
        } else {
            return;
        }
        this.isAppStateMonitorEnabled = enabled;
    }

    private HwQoEContentAware(Context context, IHwQoEContentAwareCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
        initHwQoEContentAwareHandler();
    }

    public void updateWifiSleepWhiteList(int type, List<String> packageWhiteList) {
        if (7 == type) {
            this.mHwQoEWifiPolicyConfigManager.updateWifiSleepWhiteList(packageWhiteList);
        }
    }

    public boolean isLiveStreamApp(int uid) {
        int noBgLimit = this.mHwQoEWifiPolicyConfigManager.queryWifiSleepTime(getAppNameUid(uid));
        if (21 != this.mAppTypeRecoManager.getAppType(getAppNameUid(uid)) && 1000 != noBgLimit) {
            return false;
        }
        HwQoEUtils.logD("isLiveStreamApp,uid:" + uid);
        return true;
    }

    private int calculateNewUdpAccessScore(HwQoEUdpNetWorkInfo currUdpInfo, HwQoEUdpNetWorkInfo lastUdpInfo) {
        if (currUdpInfo == null || lastUdpInfo == null) {
            return 0;
        }
        if (currUdpInfo.getUid() != lastUdpInfo.getUid()) {
            HwQoEUtils.logD("uid is error,ignore calculate score");
            return 0;
        }
        long timestamp = currUdpInfo.getTimestamp() - lastUdpInfo.getTimestamp();
        if (timestamp <= 0 || timestamp > 10000) {
            HwQoEUtils.logD("[timestamp]: " + timestamp);
            return 0;
        }
        int score;
        long txUdpPackets = currUdpInfo.getTxUdpPackets() - lastUdpInfo.getTxUdpPackets();
        long rxUdpPackets = currUdpInfo.getRxUdpPackets() - lastUdpInfo.getRxUdpPackets();
        long rxUdpBytes = currUdpInfo.getRxUdpBytes() - lastUdpInfo.getRxUdpBytes();
        long txUdpBytes = currUdpInfo.getTxUdpBytes() - lastUdpInfo.getTxUdpBytes();
        long stamp = timestamp / 1000;
        if (stamp == 0) {
            stamp = 1;
        }
        long speed = ((rxUdpBytes + txUdpBytes) / stamp) / 1024;
        HwQoEUtils.logD("[txUdpPackets]: " + txUdpPackets + " , [rxUdpPackets]: " + rxUdpPackets + ",[UDP SPEED]" + speed + " KB/S");
        if ((rxUdpPackets + txUdpPackets) / stamp > 10) {
            score = 1;
        } else {
            score = -1;
        }
        if (this.mCallback != null) {
            this.mCallback.onPeriodSpeed(speed);
        }
        return score;
    }

    private void initHwQoEContentAwareHandler() {
        HandlerThread handlerThread = new HandlerThread("hw_qoe_contentaware_thread");
        handlerThread.start();
        this.mHwQoEContentAwareHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (HwQoEContentAware.this.isAppStateMonitorEnabled) {
                            HwQoEContentAware.this.mCurrUdpInfoForMonitor = HwQoEContentAware.this.mHwQoEJNIAdapter.getUdpNetworkStatsDetail(HwQoEContentAware.this.mCurrMoniorUid, 1);
                            HwQoEContentAware.this.mCurrMoniorscore = HwQoEContentAware.this.calculateNewUdpAccessScore(HwQoEContentAware.this.mCurrUdpInfoForMonitor, HwQoEContentAware.this.mLastUdpInfoForMonitor);
                            HwQoEContentAware.this.mAppSensitivityScore = HwQoEContentAware.this.mAppSensitivityScore + HwQoEContentAware.this.mCurrMoniorscore;
                            if (HwQoEContentAware.this.mAppSensitivityScore < 0 || (HwQoEContentAware.this.mAppSensitivityScore >= 2 && HwQoEContentAware.this.mCurrMoniorscore <= 0)) {
                                HwQoEContentAware.this.mAppSensitivityScore = 0;
                            }
                            if (HwQoEContentAware.this.mLastUdpInfoForMonitor == null) {
                                HwQoEContentAware.this.mLastUdpInfoForMonitor = new HwQoEUdpNetWorkInfo(HwQoEContentAware.this.mCurrUdpInfoForMonitor);
                            } else {
                                HwQoEContentAware.this.mLastUdpInfoForMonitor.setUdpNetWorkInfo(HwQoEContentAware.this.mCurrUdpInfoForMonitor);
                            }
                            HwQoEUtils.logD("mAppSensitivityScore = " + HwQoEContentAware.this.mAppSensitivityScore + " , isCallbackNotified = " + HwQoEContentAware.this.isCallbackNotified + " ,isSensitiveApp = " + HwQoEContentAware.this.isSensitiveApp + ", score: " + HwQoEContentAware.this.mCurrMoniorscore);
                            if (!HwQoEContentAware.this.isCallbackNotified && HwQoEContentAware.this.mAppSensitivityScore >= 2) {
                                HwQoEContentAware.this.isCallbackNotified = true;
                                HwQoEContentAware.this.mCallback.onSensitiveAppStateChange(HwQoEContentAware.this.mCurrMoniorUid, 1, false);
                            } else if (HwQoEContentAware.this.isCallbackNotified && HwQoEContentAware.this.mAppSensitivityScore == 0) {
                                HwQoEContentAware.this.isCallbackNotified = false;
                                HwQoEContentAware.this.mCallback.onSensitiveAppStateChange(HwQoEContentAware.this.mCurrMoniorUid, 0, false);
                            }
                            if (HwQoEContentAware.this.isSensitiveApp && (HwQoEContentAware.this.mHwQoEContentAwareHandler.hasMessages(1) ^ 1) != 0) {
                                HwQoEContentAware.this.mHwQoEContentAwareHandler.sendEmptyMessageDelayed(1, 3000);
                                return;
                            }
                            return;
                        }
                        return;
                    case 2:
                        HwQoEContentAware.this.handleForegroundAppWifiSleepChange(HwQoEContentAware.this.mForegroundAppPackageName, false);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public void registerProcessObserver() {
        if (!this.isBroadcastRegisted) {
            this.isBroadcastRegisted = true;
            this.mHwProcessObserver = new HwProcessObserver(this, null);
            HwQoEUtils.logD("registerProcessObserver");
            try {
                ActivityManagerNative.getDefault().registerProcessObserver(this.mHwProcessObserver);
            } catch (RemoteException e) {
                HwQoEUtils.logD("register process observer failed," + e.getMessage());
            }
        }
    }

    public void unregisterProcessObserver() {
        if (this.isBroadcastRegisted) {
            this.isBroadcastRegisted = false;
            try {
                ActivityManagerNative.getDefault().unregisterProcessObserver(this.mHwProcessObserver);
            } catch (RemoteException e) {
                HwQoEUtils.logD("unregister process observer failed," + e.getMessage());
            }
        }
    }

    private int getAppUid(String processName) {
        int uid = -1;
        if (TextUtils.isEmpty(processName)) {
            return -1;
        }
        try {
            ApplicationInfo ai = this.mPackageManager.getApplicationInfo(processName, 1);
            if (ai != null) {
                uid = ai.uid;
                HwQoEUtils.logD("packageName = " + processName + ", uid = " + uid);
            }
        } catch (NameNotFoundException e) {
            HwQoEUtils.logD("NameNotFoundException: " + e.getMessage());
        }
        return uid;
    }

    private String getAppNameUid(int uid) {
        String processName = "";
        List<RunningAppProcessInfo> appProcessList = this.mActivityManager.getRunningAppProcesses();
        if (appProcessList == null) {
            return null;
        }
        for (RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.uid == uid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    private synchronized String getForegroundActivity() {
        List<RunningTaskInfo> runningTaskInfos = this.mActivityManager.getRunningTasks(1);
        if (runningTaskInfos == null || runningTaskInfos.isEmpty()) {
            HwQoEUtils.logD("running task is null, ams is abnormal!!!");
            return null;
        }
        RunningTaskInfo mRunningTask = (RunningTaskInfo) runningTaskInfos.get(0);
        if (mRunningTask == null) {
            HwQoEUtils.logD("failed to get RunningTaskInfo");
            return null;
        }
        return mRunningTask.topActivity.getPackageName();
    }

    public void queryForegroundAppType() {
        handleForegroundAppWifiSleepChange(getForegroundActivity(), true);
    }

    private void handleForegroundAppWifiSleepChange(String packageName, boolean retry) {
        if (!TextUtils.isEmpty(packageName)) {
            int sleepTime = this.mHwQoEWifiPolicyConfigManager.queryWifiSleepTime(packageName);
            int type = this.mAppTypeRecoManager.getAppType(packageName);
            if (-1 != sleepTime) {
                this.mCallback.onForegroundAppWifiSleepChange(true, sleepTime, type, packageName);
            } else if (-1 != type || !retry) {
                this.mCallback.onForegroundAppWifiSleepChange(false, sleepTime, type, packageName);
            } else if (!this.mHwQoEContentAwareHandler.hasMessages(2)) {
                this.mHwQoEContentAwareHandler.sendEmptyMessageDelayed(2, 3000);
            }
            this.mCallback.onForegroundAppTypeChange(type, packageName);
        }
    }

    public boolean isGameType(int type, String packageName) {
        if (isGameTypeForRecoManager(type)) {
            return true;
        }
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        if (isDefaultGameType(packageName)) {
            return true;
        }
        if (packageName.contains(HwQoEUtils.SEPARATOR) && isGameTypeForRecoManager(this.mAppTypeRecoManager.getAppType(getRealAppName(packageName)))) {
            return true;
        }
        if (HwQoEUtils.GAME_ASSISIT_ENABLE) {
            return ActivityManagerEx.isInGameSpace(packageName);
        }
        return false;
    }

    public String getRealAppName(String appName) {
        String realName = "";
        if (!TextUtils.isEmpty(appName) && appName.contains(HwQoEUtils.SEPARATOR)) {
            String[] appNames = appName.split(HwQoEUtils.SEPARATOR, 2);
            if (appNames.length > 0) {
                realName = appNames[0];
            }
        }
        HwQoEUtils.logD("RealAppName:" + realName);
        return realName;
    }

    public boolean isGameTypeForRecoManager(int type) {
        if (305 == type || 9 == type) {
            return true;
        }
        return false;
    }

    private boolean isDefaultGameType(String appName) {
        if (!TextUtils.isEmpty(appName)) {
            for (String startsWith : DEFAULT_GAME_NAMES) {
                if (appName.startsWith(startsWith)) {
                    return true;
                }
            }
        }
        return false;
    }
}
