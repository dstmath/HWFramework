package com.huawei.server.rme.hyperhold;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.IMonitor;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AliveReport {
    private static final long ALIVE_REPORT_DELAY = 7200000;
    private static final int APP_DIED = 2;
    private static final String APP_SWITCH_REASON = "appSwitch";
    private static final int COLD_START = 0;
    private static final int HOT_START = 1;
    private static final String HYPERHOLD_LOG_PATH = "/data/log/iaware/hyperhold/";
    private static final String IAWARE_LOG_PATH = "/data/log/iaware/";
    private static final int PRIVILEGE = 504;
    private static final int REPORT_APP = 2;
    private static final long REPORT_BIG_DATA_DELAY = 28800000;
    private static final long REPORT_HEARTBEAT = 1800000;
    private static final String RUNSTATE_LOG_FILE = "hyperhold_running_state";
    private static final String RUNSTATE_LOG_PATH = "/data/log/iaware/hyperhold/hyperhold_running_state.log";
    private static final int STATISTIC_LIVE_APP = 1;
    private static final String TAG_PG = "SWAP_AliveReport";
    private static final String TOP_APP_INTENT_RESON = "android.intent.extra.REASON";
    private static final String TOP_APP_PKG = "toPackage";
    private static final String TOP_APP_UID = "toUid";
    private static volatile AliveReport aliveReport = null;
    private ConcurrentHashMap<String, AliveAppInfo> aliveAppMap;
    private AppSwitchCallBack appSwitchCallBack;
    private int beginTime;
    private Context context;
    private int endTime;
    private String fromPkgName;
    private Handler handler;
    private volatile boolean isInit;
    private volatile boolean isReportEnable;
    private long lastReportAppNameTime;
    private long lastReportAppTime;
    private long lastReportLiveTime;
    private ExecutorService liveAppCountExecutor;
    private SwapScreenStateReceiver mReceiver;
    private PackageManager packageManager;
    private SwapAliveProcessObserver processObserver;
    private long recordStartTime;
    private volatile long reportAliveTime;
    private long serviceStartTime;
    private volatile int userType;

    private AliveReport() {
        this.beginTime = 0;
        this.endTime = 0;
        this.userType = 0;
        this.isReportEnable = false;
        this.isInit = false;
        this.lastReportLiveTime = 0;
        this.lastReportAppTime = 0;
        this.lastReportAppNameTime = 0;
        this.mReceiver = null;
        this.aliveAppMap = new ConcurrentHashMap<>();
        this.packageManager = null;
        this.processObserver = new SwapAliveProcessObserver();
        this.appSwitchCallBack = null;
        this.liveAppCountExecutor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(BackgroundThread.get().getLooper()) {
            /* class com.huawei.server.rme.hyperhold.AliveReport.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (AliveReport.this.isReportEnable) {
                    AliveReport.this.dealMsg(msg);
                }
            }
        };
        this.serviceStartTime = System.currentTimeMillis();
        this.lastReportAppTime = SystemClock.elapsedRealtime();
        this.recordStartTime = this.serviceStartTime;
        this.reportAliveTime = WifiProCommonUtils.RECHECK_DELAYED_MS;
    }

    /* access modifiers changed from: private */
    public class LiveAppCountMonitor implements Runnable {
        String appName;

        LiveAppCountMonitor(String appName2) {
            this.appName = appName2;
        }

        @Override // java.lang.Runnable
        public void run() {
            String str = this.appName;
            if (str != null) {
                AliveReport.this.handleAppForgroundMsg(str);
            }
        }
    }

    /* access modifiers changed from: private */
    public class SwapScreenStateReceiver extends BroadcastReceiver {
        private SwapScreenStateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || context == null) {
                Slog.e(AliveReport.TAG_PG, "Broadcast Receiver error!");
                return;
            }
            String action = intent.getAction();
            if (action != null) {
                if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                    AliveReport.this.writeRunningStateLog("ScreenOn");
                } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                    AliveReport.this.writeRunningStateLog("ScreenOff");
                } else {
                    Slog.i(AliveReport.TAG_PG, "other broadcast receive, please check!");
                }
            }
        }
    }

    private void registerScreenBroadCastReceiver() {
        IntentFilter deviceStates = new IntentFilter();
        deviceStates.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON);
        deviceStates.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF);
        deviceStates.setPriority(1000);
        this.context.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, deviceStates, null, null);
    }

    private void unregisterScreenBroadCastReceiver() {
        SwapScreenStateReceiver swapScreenStateReceiver = this.mReceiver;
        if (swapScreenStateReceiver != null) {
            this.context.unregisterReceiver(swapScreenStateReceiver);
        }
    }

    public void init(Context context2) {
        detectUserType();
        judgeReportEnable();
        if (this.isReportEnable) {
            setLogPermissions();
            beginReportTimer();
            this.context = context2;
            this.lastReportLiveTime = System.currentTimeMillis();
            registerAppSwitchCallBack();
            registerProcessObserver();
            this.isInit = true;
            this.mReceiver = new SwapScreenStateReceiver();
            registerScreenBroadCastReceiver();
            Slog.i(TAG_PG, "aliveReport initalized!");
        }
    }

    public static AliveReport getInstance() {
        if (aliveReport == null) {
            synchronized (AliveReport.class) {
                if (aliveReport == null) {
                    aliveReport = new AliveReport();
                }
            }
        }
        return aliveReport;
    }

    public boolean getReportEnable() {
        return this.isReportEnable;
    }

    public boolean getInitStatus() {
        return this.isInit;
    }

    public void reportLogFile() {
        if (!new File("/data/log/iaware/hyperhold/hyperhold_running_state.log").exists()) {
            Slog.e(TAG_PG, "running state logfile not exists.");
            return;
        }
        IMonitor.EventStream eventStream = IMonitor.openEventStream(905001001);
        this.endTime = getIntTimestamp(System.currentTimeMillis());
        eventStream.setParam("beginTime", this.beginTime);
        eventStream.setParam("endTime", this.endTime);
        if (IMonitor.sendEvent(eventStream)) {
            Slog.i(TAG_PG, "running state log send successs!");
            this.beginTime = this.endTime;
        } else {
            Slog.e(TAG_PG, "running state log send failed!");
        }
        try {
            eventStream.close();
        } catch (IOException ex) {
            Slog.e(TAG_PG, "eventStream close failed: " + ex);
        }
    }

    private void disableRegister() {
        unregisterAppSwitchCallBack();
        unregisterProcessObserver();
        unregisterScreenBroadCastReceiver();
    }

    /* access modifiers changed from: private */
    public static class AliveAppInfo {
        private boolean isAlive;
        private boolean isFronted;
        private Set<Integer> pidSet;
        private String pkgName;
        private int uid;

        private AliveAppInfo(Builder builder) {
            this.pkgName = builder.pkgName;
            this.uid = builder.uid;
            this.pidSet = builder.pidSet;
            this.isFronted = builder.isFronted;
        }

        /* access modifiers changed from: private */
        public static class Builder {
            private boolean isAlive;
            private boolean isFronted;
            private Set<Integer> pidSet;
            private String pkgName;
            private int uid;

            private Builder() {
                this.pkgName = "";
                this.uid = -1;
                this.pidSet = new HashSet();
                this.isFronted = false;
                this.isAlive = false;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder pkgName(String pkgName2) {
                this.pkgName = pkgName2;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder uid(int uid2) {
                this.uid = uid2;
                return this;
            }

            private Builder pidSet(Set<Integer> pidSet2) {
                this.pidSet = pidSet2;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder isFronted(boolean isFronted2) {
                this.isFronted = isFronted2;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private AliveAppInfo create() {
                return new AliveAppInfo(this);
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addPids(Integer pid) {
            this.pidSet.add(pid);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void delPid(Integer pid) {
            this.pidSet.remove(pid);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Set<Integer> getPids() {
            return this.pidSet;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setIsFronted(boolean isFronted2) {
            this.isFronted = isFronted2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isFronted() {
            return this.isFronted;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setIsAlive(boolean isAlive2) {
            this.isAlive = true;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean getIsAlive() {
            return this.isAlive;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private String getAppName() {
            return this.pkgName;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dealMsg(Message msg) {
        Message msgSend = this.handler.obtainMessage();
        msgSend.what = msg.what;
        int i = msg.what;
        if (i != 1) {
            if (i == 2) {
                reportAppInfo();
                this.handler.sendMessageDelayed(msgSend, REPORT_HEARTBEAT);
            }
        } else if (msg.obj instanceof String) {
            this.liveAppCountExecutor.execute(new LiveAppCountMonitor((String) msg.obj));
        }
    }

    private void reportAppInfo() {
        long curTime = SystemClock.elapsedRealtime();
        if (curTime - this.lastReportAppTime >= this.reportAliveTime) {
            if (sendAppLivingInfo()) {
                this.reportAliveTime = ALIVE_REPORT_DELAY;
                this.recordStartTime = System.currentTimeMillis();
            } else {
                Slog.e(TAG_PG, "sendAppInfo failed!");
            }
            this.lastReportAppTime = curTime;
        }
    }

    private boolean sendAppLivingInfo() {
        int appLivingNum = getLiveAppNum();
        IMonitor.EventStream eventStream = IMonitor.openEventStream(905000007);
        eventStream.setParam("start", getIntTimestamp(this.recordStartTime));
        int curTime = getIntTimestamp(System.currentTimeMillis());
        eventStream.setParam("end", curTime);
        eventStream.setParam("activity", appLivingNum);
        Slog.i(TAG_PG, "Send appLivingInfo:" + getIntTimestamp(this.recordStartTime) + "," + curTime + "," + appLivingNum);
        boolean isSendSuc = IMonitor.sendEvent(eventStream);
        if (isSendSuc) {
            Slog.i(TAG_PG, "AppLivingInfo(905000007) send successs!");
        } else {
            Slog.e(TAG_PG, "AppLivingInfo send failed!");
        }
        try {
            eventStream.close();
        } catch (IOException ex) {
            Slog.e(TAG_PG, "eventStream close failed: " + ex);
        }
        return isSendSuc;
    }

    private void detectUserType() {
        this.userType = SystemProperties.getInt("ro.logsystem.usertype", -1);
        Slog.i(TAG_PG, "Alive reporter getUserType:" + this.userType);
    }

    private void beginReportTimer() {
        Handler handler2 = this.handler;
        if (handler2 == null) {
            Slog.e(TAG_PG, "handler not initialized!");
            return;
        }
        Message msgReport = handler2.obtainMessage();
        msgReport.what = 2;
        this.handler.sendMessageDelayed(msgReport, REPORT_HEARTBEAT);
    }

    private void judgeReportEnable() {
        this.isReportEnable = false;
        if (this.userType == 3 && !Swap.getInstance().isSwapEnabled()) {
            Slog.i(TAG_PG, "userType is 3 and swap is unEnable, will report.");
            this.isReportEnable = true;
        }
    }

    private int getIntTimestamp(long timestamp) {
        return (int) (timestamp / 1000);
    }

    /* access modifiers changed from: private */
    public class AppSwitchCallBack extends IHwActivityNotifierEx {
        private AppSwitchCallBack() {
        }

        public void call(Bundle extras) {
            if (extras != null && AliveReport.APP_SWITCH_REASON.equals(extras.getString(AliveReport.TOP_APP_INTENT_RESON))) {
                int toUid = extras.getInt(AliveReport.TOP_APP_UID, -1);
                String toPkgName = extras.getString(AliveReport.TOP_APP_PKG);
                if (toUid <= 0 || toPkgName == null) {
                    Slog.e(AliveReport.TAG_PG, "AppStartToCallBack pid or uid or pkgName invalid!");
                    return;
                }
                if (!toPkgName.equals(AliveReport.this.fromPkgName)) {
                    if (AliveReport.this.fromPkgName != null && !AliveReport.this.fromPkgName.equals("com.huawei.android.launcher")) {
                        AliveReport aliveReport = AliveReport.this;
                        aliveReport.writeRunningStateLog("GoBackground, " + AliveReport.this.fromPkgName);
                    }
                    AliveReport.this.fromPkgName = toPkgName;
                }
                AliveReport.this.handleAppForground(toPkgName, toUid);
            }
        }
    }

    private void registerAppSwitchCallBack() {
        if (this.appSwitchCallBack == null) {
            this.appSwitchCallBack = new AppSwitchCallBack();
            ActivityManagerEx.registerHwActivityNotifier(this.appSwitchCallBack, APP_SWITCH_REASON);
        }
    }

    private void unregisterAppSwitchCallBack() {
        AppSwitchCallBack appSwitchCallBack2 = this.appSwitchCallBack;
        if (appSwitchCallBack2 != null) {
            ActivityManagerEx.unregisterHwActivityNotifier(appSwitchCallBack2);
            this.appSwitchCallBack = null;
        }
    }

    /* access modifiers changed from: private */
    public class SwapAliveProcessObserver extends IProcessObserver.Stub {
        private SwapAliveProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
        }

        public void onProcessDied(int pid, int uid) {
            AliveReport aliveReport = AliveReport.this;
            aliveReport.packageManager = aliveReport.context.getPackageManager();
            String pkgName = null;
            if (AliveReport.this.packageManager == null) {
                Slog.e(AliveReport.TAG_PG, "Process_died get pkgname error!");
            } else {
                pkgName = AliveReport.this.packageManager.getNameForUid(uid);
            }
            if (pkgName == null) {
                Slog.e(AliveReport.TAG_PG, "packageManager getNameForUid pkgname is null!");
            } else {
                AliveReport.this.handleProcessDied(pkgName, pid);
            }
        }
    }

    private void registerProcessObserver() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.processObserver);
        } catch (RemoteException e) {
            Slog.e(TAG_PG, "Swap register process observer failed!");
        }
    }

    private void unregisterProcessObserver() {
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.processObserver);
        } catch (RemoteException e) {
            Slog.e(TAG_PG, "unregister process observer failed!");
        }
    }

    public void notifyProcessCreate(String appName, int pid, int uid) {
        if (this.isReportEnable) {
            handleProcessCreate(appName, pid, uid);
        }
    }

    private void getAllRunningAppBeforeServiceByAms() {
        List<ActivityManager.RunningAppProcessInfo> runningApps;
        Context context2 = this.context;
        if (context2 != null) {
            Object tempObject = context2.getSystemService("activity");
            if ((tempObject instanceof ActivityManager) && (runningApps = ((ActivityManager) tempObject).getRunningAppProcesses()) != null && runningApps.size() > 0) {
                for (ActivityManager.RunningAppProcessInfo app : runningApps) {
                    if (app.pkgList != null) {
                        String[] strArr = app.pkgList;
                        for (String pkg : strArr) {
                            handleProcessCreate(pkg, 1000, app.pid);
                            Slog.i(TAG_PG, "get running pkg from AMS:" + pkg);
                        }
                    }
                }
            }
        }
    }

    private void handleProcessCreate(String appName, int pid, int uid) {
        AliveAppInfo curApp = this.aliveAppMap.get(appName);
        if (curApp == null) {
            AliveAppInfo curApp2 = new AliveAppInfo.Builder().pkgName(appName).uid(uid).isFronted(false).create();
            if (pid > 0) {
                curApp2.addPids(Integer.valueOf(pid));
            }
            this.aliveAppMap.put(appName, curApp2);
        } else if (pid > 0) {
            curApp.addPids(Integer.valueOf(pid));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppForground(String appName, int uid) {
        if (!this.aliveAppMap.containsKey(appName)) {
            handleProcessCreate(appName, uid, -1);
        }
        if (!"com.huawei.android.launcher".equals(appName)) {
            Message msg = this.handler.obtainMessage();
            msg.what = 1;
            msg.obj = appName;
            this.handler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppForgroundMsg(String appName) {
        if (this.aliveAppMap.containsKey(appName)) {
            AliveAppInfo curApp = this.aliveAppMap.get(appName);
            if (curApp == null) {
                Slog.e(TAG_PG, "handleAppForegroundMsg nullptr: " + appName);
                return;
            }
            if (!curApp.isFronted()) {
                curApp.setIsFronted(true);
            }
            if (!curApp.getIsAlive()) {
                curApp.setIsAlive(true);
                writeLiveCountToFile(curApp, 0);
            } else {
                writeLiveCountToFile(curApp, 1);
            }
            Slog.i(TAG_PG, "handleAppForground:pkg:" + appName);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleProcessDied(String appName, int pid) {
        AliveAppInfo curApp = this.aliveAppMap.get(appName);
        if (curApp == null) {
            Slog.e(TAG_PG, "handleProcessDied nullptr: " + appName);
            return;
        }
        curApp.delPid(Integer.valueOf(pid));
        if (curApp.getPids().size() == 0) {
            handleAppDied(appName);
        }
    }

    private void handleAppDied(String appName) {
        AliveAppInfo curApp = this.aliveAppMap.get(appName);
        if (curApp == null) {
            Slog.e(TAG_PG, "handleAppDied app not exist: " + appName);
            return;
        }
        this.aliveAppMap.remove(appName);
        writeLiveCountToFile(curApp, 2);
        Slog.i(TAG_PG, "handleAppDied:pkg:" + appName);
    }

    private void writeLiveCountToFile(AliveAppInfo app, int startType) {
        if (app.isFronted()) {
            if (startType == 2) {
                app.setIsAlive(false);
                app.setIsFronted(false);
            }
            writeRunningStateLog(statisticFrontedApp(app, startType));
        }
    }

    private String statisticFrontedApp(AliveAppInfo app, int startType) {
        String appStatus = "app start";
        if (startType == 0) {
            appStatus = "ColdStart";
        } else if (startType == 1) {
            appStatus = "HotStart";
        } else if (startType == 2) {
            appStatus = "Died";
        } else {
            Slog.e(TAG_PG, "statisticFrontedApp get wrong start type " + startType);
        }
        return appStatus + ", " + app.getAppName() + ", " + getLiveAppNum();
    }

    private int getLiveAppNum() {
        int liveAppNum = 0;
        for (AliveAppInfo app : this.aliveAppMap.values()) {
            if (app.isFronted()) {
                liveAppNum++;
            }
        }
        return liveAppNum;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeRunningStateLog(String logStr) {
        boolean isExist = new File("/data/log/iaware/hyperhold/hyperhold_running_state.log").exists();
        KernelInterface.getInstance().writeSwapFile("hyperhold_running_state", getNowTime() + ", " + logStr + System.lineSeparator(), false);
        long curTime = System.currentTimeMillis();
        File liveCountFile = new File("/data/log/iaware/hyperhold/hyperhold_running_state.log");
        if (curTime - this.lastReportLiveTime > REPORT_BIG_DATA_DELAY || liveCountFile.length() > 81920) {
            reportLogFile();
            this.lastReportLiveTime = curTime;
        }
        checkAdditionalLog(isExist);
    }

    private void checkAdditionalLog(boolean isExist) {
        File logFile = new File("/data/log/iaware/hyperhold/hyperhold_running_state.log");
        long curTime = SystemClock.uptimeMillis();
        if (logFile.exists() && isExist) {
            long j = this.lastReportAppNameTime;
            if (j != 0 && curTime - j < WifiProCommonUtils.RECHECK_DELAYED_MS) {
                return;
            }
        }
        this.lastReportAppNameTime = curTime;
        KernelInterface.getInstance().writeSwapFile("hyperhold_running_state", getNowTime() + ", CurrentVersion, " + SystemProperties.get("persist.sys.hiview.cust_version") + System.lineSeparator(), false);
        StringBuilder appListLog = new StringBuilder(getNowTime());
        appListLog.append(", CurrentLivedApps, ");
        int liveAppNum = 0;
        for (AliveAppInfo app : this.aliveAppMap.values()) {
            if (app.isFronted()) {
                liveAppNum++;
                if (liveAppNum > 1) {
                    appListLog.append("; ");
                }
                appListLog.append(app.getAppName());
            }
        }
        appListLog.append(", ");
        appListLog.append(liveAppNum);
        appListLog.append(System.lineSeparator());
        KernelInterface.getInstance().writeSwapFile("hyperhold_running_state", appListLog.toString(), false);
    }

    private String getNowTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private void setSingleLogPermission(String pathName) {
        if (Files.exists(Paths.get(pathName, new String[0]), new LinkOption[0])) {
            FileUtils.setPermissions(pathName, PRIVILEGE, -1, -1);
            Slog.i(TAG_PG, "reset permissions of path: " + pathName);
        }
    }

    private void setLogPermissions() {
        setSingleLogPermission(IAWARE_LOG_PATH);
        setSingleLogPermission(HYPERHOLD_LOG_PATH);
        setSingleLogPermission("/data/log/iaware/hyperhold/hyperhold_running_state.log");
    }
}
