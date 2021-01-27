package com.huawei.server.rme.hyperhold;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.swing.HwSwingMotionGestureConstant;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.server.rme.collector.ResourceCollector;
import com.huawei.server.rme.hyperhold.ParaConfig;
import com.huawei.server.rme.hyperhold.SceneConst;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class SceneProcessing {
    private static final String AOD_NAME = "com.huawei.aod";
    private static final long APP_REQ_MEM_MIN_INTERVAL = 100;
    private static final String APP_SWITCH_REASON = "appSwitch";
    private static final String BACK_APP_PKG = "fromPackage";
    private static final String BACK_APP_UID = "fromUid";
    private static final int DEL_TYPE = 2;
    private static final long KEEP_ALIVE_TIME = 0;
    private static final int KILL_TYPE = 3;
    private static final String LAUNCHER_NAME = "com.huawei.android.launcher";
    private static final int MAX_COST_TIME = 5000;
    private static final int NORMAL_TYPE = 1;
    private static final int POOL_SIZE = 1;
    private static final int SCREEN_OFF = 5;
    private static final int SCREEN_OFF_DELAY = 180000;
    private static final int SCREEN_ON = 4;
    private static final String TAG = "SWAP_SCENE";
    private static final String TOP_APP_INTENT_RESON = "android.intent.extra.REASON";
    private static final String TOP_APP_PKG = "toPackage";
    private static final String TOP_APP_UID = "toUid";
    private static volatile boolean isInDeepSleep = false;
    private static volatile SceneProcessing sceneHandler;
    private Set<String> activeAppSet;
    private AppModel appModel;
    private int appReqKillMemMax;
    private int appReqKillThreshold;
    private AppScore appScore;
    private BufferProc bufferProc;
    private Context context;
    private String curFrontAppName = "";
    private int curFrontAppUid;
    private String fromPkgName;
    private int fromUid;
    private Handler handler = null;
    private boolean inCamera;
    private KernelInterface kernelInterface;
    private KillDecision killDecision;
    private long lastAppReqMemTime = 0;
    private boolean logEnable;
    private AppSwitchCallBack mAppSwitchCallBack = null;
    private SwapProcessObserver mProcessObserver = new SwapProcessObserver();
    private SwapScreenStateReceiver mReceiver = null;
    private PowerKit.Sink mStateRecognitionListener = new PowerKit.Sink() {
        /* class com.huawei.server.rme.hyperhold.SceneProcessing.AnonymousClass1 */

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            if (eventType == 1) {
                SceneProcessing.this.notifySceneData("FREEZE", new SceneConst.ScenePara(pkg));
            }
            if (eventType == 2) {
                SceneProcessing.this.notifySceneData("UNFREEZE", new SceneConst.ScenePara(pkg));
            }
        }
    };
    private int maxApplication = -1;
    private Set<String> mkdirAppSet = Collections.synchronizedSet(new HashSet());
    private ParaConfig paraConfig;
    private Set<String> pkgNameSet = new HashSet();
    private PowerKit powerKitPgSdk = null;
    private int quickKillBuffer;
    private ThreadPoolExecutor sceneParseExecutor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
    private int screenOffQuickBuffer;
    private HashMap<String, ParaConfig.WhiteListParam> whiteListMap;

    private SceneProcessing() {
    }

    public void init(Context context2) {
        this.appModel = AppModel.getInstance();
        this.paraConfig = ParaConfig.getInstance();
        this.kernelInterface = KernelInterface.getInstance();
        this.appScore = AppScore.getInstance();
        this.bufferProc = BufferProc.getInstance();
        this.killDecision = KillDecision.getInstance();
        initHandler();
        this.context = context2;
        this.whiteListMap = this.paraConfig.getWhiteList();
        this.activeAppSet = this.paraConfig.getActiveAppList();
        this.logEnable = this.paraConfig.getOtherParam().getLogEnable();
        this.inCamera = false;
        getAllRunningAppBeforeServiceByAms();
        this.quickKillBuffer = ParaConfig.getInstance().getKillParam().getBigKillMem();
        this.screenOffQuickBuffer = this.paraConfig.getKillParamOpt().getScreenOffQuickBuffer();
        this.maxApplication = this.paraConfig.getKillParam().getMaxApplication();
        this.appReqKillThreshold = this.paraConfig.getKillParam().getAppReqKillThreshold();
        this.appReqKillMemMax = this.paraConfig.getKillParam().getAppReqKillMemMax();
        if (this.activeAppSet.contains("system_server")) {
            addSystemServerPid();
        }
        this.powerKitPgSdk = PowerKit.getInstance();
        setActAppScore();
        registerAppSwitchCallBack();
        registerProcessObserver();
        callFreezeRegisterListener();
        this.mReceiver = new SwapScreenStateReceiver();
        registerScreenBroadCastReceiver();
        resetZswapdCpuSet();
        Slog.i(TAG, "SceneProcessing init called");
    }

    private void initHandler() {
        Looper looper = HyperHoldServiceThread.getInstance().getLooper();
        if (looper == null) {
            looper = BackgroundThread.get().getLooper();
            Slog.e(TAG, "HyperHold Service Thread get failed, use background looper instead");
        }
        this.handler = new Handler(looper) {
            /* class com.huawei.server.rme.hyperhold.SceneProcessing.AnonymousClass2 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == 1 && (msg.obj instanceof SceneConst.ScenePara)) {
                    SceneProcessing.this.sceneParseExecutor.execute(new SceneParse(1, (SceneConst.ScenePara) msg.obj));
                } else if (msg.what == 2 && (msg.obj instanceof String)) {
                    SceneProcessing.this.sceneParseExecutor.execute(new SceneParse(2, (String) msg.obj));
                } else if (msg.what == 4) {
                    SceneProcessing.this.resetZswapdCpuSet();
                    SceneProcessing.this.resetQuickKillBuffer();
                } else if (msg.what == 5) {
                    boolean unused = SceneProcessing.isInDeepSleep = true;
                    SceneProcessing.this.setZswapdCpuSetToSmall();
                } else {
                    Slog.e(SceneProcessing.TAG, "handle error message");
                }
            }
        };
    }

    public void notifySceneData(String eventType, SceneConst.ScenePara para) {
        if (para == null || para.getAppName() == null || eventType == null) {
            Slog.e(TAG, "notifySceneData: app name is null");
        } else if (this.handler != null) {
            Slog.i(TAG, "Entering notifySceneData notifySceneData. EventType: " + eventType + ", App: " + para.getAppName() + ", uid: " + para.getUid() + ", pid: " + para.getPid());
            Message msg = this.handler.obtainMessage();
            try {
                para.setEventType(SceneConst.SceneEvent.valueOf(eventType));
                msg.obj = para;
                msg.what = 1;
                this.handler.sendMessage(msg);
            } catch (IllegalArgumentException e) {
                Slog.e(TAG, "notifySceneData: Illegal event type.");
            }
        }
    }

    public void disableRegister() {
        unregisterAppSwitchCallBack();
        callFreezeUnregisterListener();
        unregisterProcessObserver();
        unregisterScreenBroadCastReceiver();
    }

    public static SceneProcessing getInstance() {
        if (sceneHandler == null) {
            synchronized (SceneProcessing.class) {
                if (sceneHandler == null) {
                    sceneHandler = new SceneProcessing();
                }
            }
        }
        return sceneHandler;
    }

    public Set<String> getPkgNameSet() {
        return this.pkgNameSet;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifySceneData(SceneConst.ScenePara para) {
        long beforeTime = System.currentTimeMillis();
        if (this.logEnable) {
            Slog.i(TAG, "handleNotifySceneData::Entering notifySceneData. EventType:" + para.getEventType() + " para:" + para.getAppName());
        }
        switch (para.getEventType()) {
            case SWITCH_TO_FOREGROUND:
                appColdCheck(para.getAppName());
                activityKillCheck(para.getActivityName());
                break;
            case APP_START:
                notifyAppStart(para.getAppName());
                break;
            case FREEZE:
                notifyAppFreeze(para.getAppName());
                break;
            case UNFREEZE:
                notifyAppUnFreeze(para.getAppName());
                break;
            case SCREEN_ON:
                notifyScreenOn();
                break;
            case SCREEN_OFF:
                notifyScreenOff();
                break;
            case PROCESS_CREATE:
                notifyProcessCreate(para.getAppName(), para.getUid(), para.getPid());
                break;
            case PROCESS_DIED:
                notifyProcessDied(para.getAppName(), para.getUid(), para.getPid());
                break;
            case APP_SWITCH_TO:
                checkAppSwitchTo(para.getAppName(), para.getUid(), para.getIsVisable(), para.getTestTime());
                this.curFrontAppUid = para.getUid();
                break;
            case APP_SWITCH_FROM:
                notifyAppChangeToBackground(para.getAppName());
                break;
            default:
                Slog.i(TAG, "EventType:" + para.getEventType());
                break;
        }
        long totalCost = System.currentTimeMillis() - beforeTime;
        if (totalCost > HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD) {
            Slog.i(TAG, "handleNotifySceneData::total cost time = " + totalCost + "ms. Time out! >5s");
        }
    }

    /* access modifiers changed from: private */
    public class AppSwitchCallBack extends IHwActivityNotifierEx {
        private AppSwitchCallBack() {
        }

        public void call(Bundle extras) {
            if (extras != null && SceneProcessing.APP_SWITCH_REASON.equals(extras.getString(SceneProcessing.TOP_APP_INTENT_RESON))) {
                int toUid = extras.getInt(SceneProcessing.TOP_APP_UID, -1);
                String toPkgName = extras.getString(SceneProcessing.TOP_APP_PKG);
                if (toUid <= 0 || toPkgName == null) {
                    Slog.e(SceneProcessing.TAG, "AppStartToCallBack pid or uid or pkgName invalid!");
                    return;
                }
                SceneProcessing.this.notifySceneData("APP_SWITCH_TO", new SceneConst.ScenePara(toPkgName, toUid));
                if (!toPkgName.equals(SceneProcessing.this.fromPkgName)) {
                    if (SceneProcessing.this.fromPkgName != null) {
                        SceneProcessing.this.notifySceneData("APP_SWITCH_FROM", new SceneConst.ScenePara(SceneProcessing.this.fromPkgName, SceneProcessing.this.fromUid));
                    }
                    SceneProcessing.this.fromPkgName = toPkgName;
                    SceneProcessing.this.fromUid = toUid;
                }
            }
        }
    }

    private void registerAppSwitchCallBack() {
        if (this.mAppSwitchCallBack == null) {
            this.mAppSwitchCallBack = new AppSwitchCallBack();
            ActivityManagerEx.registerHwActivityNotifier(this.mAppSwitchCallBack, APP_SWITCH_REASON);
        }
    }

    private void unregisterAppSwitchCallBack() {
        AppSwitchCallBack appSwitchCallBack = this.mAppSwitchCallBack;
        if (appSwitchCallBack != null) {
            ActivityManagerEx.unregisterHwActivityNotifier(appSwitchCallBack);
            this.mAppSwitchCallBack = null;
        }
    }

    /* access modifiers changed from: private */
    public class SwapScreenStateReceiver extends BroadcastReceiver {
        private SwapScreenStateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || context == null) {
                Slog.e(SceneProcessing.TAG, "Broadcast Receiver error!");
                return;
            }
            String action = intent.getAction();
            if (action != null) {
                if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_ON.equals(action)) {
                    SceneProcessing.this.notifySceneData("SCREEN_ON", new SceneConst.ScenePara());
                } else if (SmartDualCardConsts.SYSTEM_STATE_NAME_SCREEN_OFF.equals(action)) {
                    SceneProcessing.this.notifySceneData("SCREEN_OFF", new SceneConst.ScenePara());
                } else {
                    Slog.i(SceneProcessing.TAG, "other broadcast receive, please check!");
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

    private void callFreezeRegisterListener() {
        PowerKit powerKit = this.powerKitPgSdk;
        if (powerKit != null) {
            try {
                powerKit.enableStateEvent(this.mStateRecognitionListener, 6);
            } catch (RemoteException e) {
                this.powerKitPgSdk = null;
                Slog.e(TAG, "powerKitPgSdk registerSink && enableStateEvent happend RemoteException");
            }
        }
    }

    private void callFreezeUnregisterListener() {
        PowerKit powerKit = this.powerKitPgSdk;
        if (powerKit != null) {
            try {
                powerKit.disableStateEvent(this.mStateRecognitionListener, 6);
            } catch (RemoteException e) {
                Slog.e(TAG, "powerKitPgSdk unregisterSink happend RemoteException.");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class SwapProcessObserver extends IProcessObserver.Stub {
        SwapProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        }

        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
        }

        public void onProcessDied(int pid, int uid) {
            String pkgName = SceneProcessing.this.appModel.getPkgByPid(pid);
            if (pkgName == null || "".equals(pkgName)) {
                Slog.e(SceneProcessing.TAG, "get pkg name from pid is null!");
                return;
            }
            SceneProcessing.getInstance().notifySceneData("PROCESS_DIED", new SceneConst.ScenePara(pkgName, pid, uid));
        }
    }

    private void registerProcessObserver() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            Slog.e(TAG, "Swap register process observer failed!");
        }
    }

    private void unregisterProcessObserver() {
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            Slog.e(TAG, "unregister process observer failed!");
        }
    }

    private void addSystemServerPid() {
        int pid = Process.myPid();
        if (pid != 0) {
            Slog.i(TAG, "addSystemPid:" + pid);
            this.kernelInterface.changeGroupToActivity(pid);
        }
    }

    private void setActAppScore() {
        int score = this.paraConfig.getScoreRatioParam().getScore();
        int activeReclaimRatio = this.paraConfig.getScoreRatioParam().getActiveReclaimRatio();
        int activeSwapRatio = this.paraConfig.getScoreRatioParam().getActiveSwapRatio();
        int activeReclaimRefault = this.paraConfig.getScoreRatioParam().getActiveReclaimRefault();
        this.kernelInterface.setScore("activityApp", score);
        this.kernelInterface.writeRatioNew("activityApp", activeReclaimRatio, activeSwapRatio, activeReclaimRefault);
    }

    private void getAllRunningAppBeforeServiceByAms() {
        List<ActivityManager.RunningAppProcessInfo> runningApps;
        this.kernelInterface.mkDirectory("activityApp");
        Context context2 = this.context;
        if (context2 != null) {
            Object tempObject = context2.getSystemService("activity");
            if ((tempObject instanceof ActivityManager) && (runningApps = ((ActivityManager) tempObject).getRunningAppProcesses()) != null && runningApps.size() > 0) {
                for (ActivityManager.RunningAppProcessInfo app : runningApps) {
                    if (app.pkgList == null || app.pkgList.length > 1) {
                        Slog.i(TAG, "get running pkg from AMS, pkgList is null or > 1, will not create memcg. pid: " + app.pid + ", uid: " + app.uid);
                    } else {
                        String[] strArr = app.pkgList;
                        for (String pkg : strArr) {
                            this.pkgNameSet.add(pkg);
                            if (this.kernelInterface.mkDirectory(pkg)) {
                                this.mkdirAppSet.add(pkg);
                            }
                            Slog.i(TAG, "get running pkg from AMS:" + pkg + ", pid: " + app.pid + ", uid: " + app.uid);
                            this.appModel.notifyProcessCreate(pkg, 1000, app.pid);
                            activityAppCheckAndSet(pkg, app.pid);
                        }
                    }
                }
            }
        }
    }

    private void upSwapInPri(int tid) {
        ResourceCollector.setProcessorAffinity(0, 4, 7);
        Process.setThreadPriority(tid, -20);
    }

    private void resetSwapInPri(int tid, int beforePriority) {
        Process.setThreadPriority(tid, beforePriority);
        ResourceCollector.setProcessorAffinity(0, 0, 7);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setZswapdCpuSetToSmall() {
        int pid = this.kernelInterface.getZswapdPid();
        Slog.i(TAG, "setZswapdCpuSetToSmall 0-3, get pid of zswapd:" + pid);
        ResourceCollector.setProcessorAffinity(pid, 0, 3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetZswapdCpuSet() {
        int pid = this.kernelInterface.getZswapdPid();
        Slog.i(TAG, "resetZswapdCpuSet to 0-5, get pid of zswapd:" + pid);
        ResourceCollector.setProcessorAffinity(pid, 0, 5);
    }

    private void swapIn(String appName) {
        Trace.traceBegin(8, "SceneProcessing::swapIn:" + appName);
        int tid = Process.myTid();
        int beforePriority = Process.getThreadPriority(tid);
        upSwapInPri(tid);
        this.kernelInterface.setEswap2Zram(appName, 100);
        this.kernelInterface.setForceSwapIn(appName);
        resetSwapInPri(tid, beforePriority);
        Trace.traceEnd(8);
    }

    private void checkInWhiteList(String appName) {
        HashMap<String, ParaConfig.WhiteListParam> hashMap = this.whiteListMap;
        if (hashMap != null && hashMap.containsKey(appName)) {
            this.killDecision.tryToQuickKill(this.whiteListMap.get(appName).getWhiteListKillThreshold());
        }
    }

    private void addPidByPkg(String appName) {
        List<ActivityManager.RunningAppProcessInfo> runningApps;
        Context context2 = this.context;
        if (context2 != null) {
            Object tempObject = context2.getSystemService("activity");
            if ((tempObject instanceof ActivityManager) && (runningApps = ((ActivityManager) tempObject).getRunningAppProcesses()) != null && runningApps.size() > 0) {
                for (ActivityManager.RunningAppProcessInfo app : runningApps) {
                    if (app.pkgList != null) {
                        for (String pkg : app.pkgList) {
                            if (pkg.equals(appName)) {
                                this.kernelInterface.addToProcs(appName, app.pid);
                            }
                        }
                    }
                }
            }
        }
    }

    private void appColdCheck(String appName) {
        if (!this.mkdirAppSet.contains(appName)) {
            if (!this.curFrontAppName.equals(appName) && !"com.huawei.android.launcher".equals(appName)) {
                BufferProc bufferProc2 = this.bufferProc;
                BufferProc.getInstance().notifyCurrentEvent("appStart");
                this.curFrontAppName = appName;
            }
            if (this.kernelInterface.mkDirectory(appName)) {
                this.mkdirAppSet.add(appName);
                Slog.i(TAG, "Entering appColdCheck cold! Mkdir. appName:" + appName);
            }
        } else if (this.curFrontAppName.equals(appName) && !"com.huawei.android.launcher".equals(appName)) {
            if (appName.contains("com.qiyi.video")) {
                BufferProc bufferProc3 = this.bufferProc;
                BufferProc.getInstance().notifyQiyi();
                return;
            }
            BufferProc bufferProc4 = this.bufferProc;
            BufferProc.getInstance().notifyCurrentEvent("activityStart");
        }
    }

    private void activityKillCheck(String activityName) {
        HashMap<String, ParaConfig.WhiteListParam> hashMap;
        if (activityName != null && (hashMap = this.whiteListMap) != null && hashMap.containsKey(activityName) && this.whiteListMap.get(activityName).getIsActivity()) {
            Slog.i(TAG, "ActivityKillCheck:" + activityName);
            this.killDecision.tryToQuickKill(this.whiteListMap.get(activityName).getWhiteListKillThreshold());
        }
    }

    private void notifyToTopAndSwapIn(String appName, int uid, boolean isVisable, boolean appSwitchMessage, String testTime) {
        String appNameToModel;
        if (testTime == null || testTime.isEmpty()) {
            appNameToModel = appName;
        } else {
            appNameToModel = appName + " " + testTime;
        }
        if (!appName.equals(AOD_NAME)) {
            this.killDecision.updateModel(appNameToModel);
        }
        if (this.appModel.isAppDied(appName)) {
            if (!this.mkdirAppSet.contains(appName)) {
                Slog.i(TAG, "App Switch with cold start:" + appName);
                if (this.kernelInterface.mkDirectory(appName)) {
                    this.mkdirAppSet.add(appName);
                    addPidByPkg(appName);
                }
            }
            Slog.i(TAG, "Entering notifyToTopAndSwapIn cold! appName:" + appName);
            this.appScore.setScoreAndRatioByScene(appName, SceneConst.SceneEvent.valueOf("SWITCH_TO_FOREGROUND"));
        } else {
            this.appScore.setScoreAndRatioByScene(appName, SceneConst.SceneEvent.valueOf("SWITCH_TO_FOREGROUND"));
            swapIn(appName);
        }
        this.appModel.notifyAppStart(appName, uid, isVisable);
        checkInWhiteList(appName);
    }

    private void checkAppSwitchTo(String appName, int uid, boolean isVisable, String testTime) {
        if (appName.contains("com.huawei.camera")) {
            Slog.i(TAG, "camera start, set zswapd to small cores");
            setZswapdCpuSetToSmall();
            this.inCamera = true;
        } else if (this.inCamera) {
            resetZswapdCpuSet();
            this.inCamera = false;
        }
        if (appName.equals("com.huawei.android.launcher")) {
            BufferProc bufferProc2 = this.bufferProc;
            BufferProc.getInstance().notifyCurrentEvent("goLauncher");
            return;
        }
        BufferProc bufferProc3 = this.bufferProc;
        BufferProc.getInstance().notifyCurrentEvent("appStart");
        notifyToTopAndSwapIn(appName, uid, isVisable, true, testTime);
        if (this.maxApplication > 0) {
            checkMaxApplicationNum();
        }
    }

    private void checkMaxApplicationNum() {
        int applicationNum = this.appModel.getLivedAppNum();
        int i = this.maxApplication;
        if (applicationNum >= i && i != -1) {
            this.killDecision.killOneApp();
        }
    }

    public void notifyAppReqMem(long reqMemKb) {
        long curTime = SystemClock.elapsedRealtime();
        if (curTime - this.lastAppReqMemTime < APP_REQ_MEM_MIN_INTERVAL) {
            Slog.e(TAG, "App require memory too frequent, interval= " + (curTime - this.lastAppReqMemTime) + ", for min interval= " + APP_REQ_MEM_MIN_INTERVAL);
            return;
        }
        int reqMemFinal = ((int) (reqMemKb / 1024)) + this.appReqKillThreshold;
        Slog.i(TAG, "Enter app require memoryKb= " + reqMemKb + ", final reqMemoryMb=" + reqMemFinal);
        if (reqMemFinal > this.appReqKillMemMax) {
            Slog.e(TAG, "App require memory too large, will use maxReqMem, reqMem= " + reqMemKb + ", maxReqMem= " + this.appReqKillMemMax);
            reqMemFinal = this.appReqKillMemMax;
        }
        this.lastAppReqMemTime = curTime;
        this.killDecision.tryToQuickKill(reqMemFinal);
    }

    private void notifyAppChangeToBackground(String appName) {
        if (!appName.equals("com.huawei.android.launcher") && !appName.equals("")) {
            this.appScore.setScoreAndRatioByScene(appName, SceneConst.SceneEvent.valueOf("SWITCH_TO_BACKGROUND"));
            this.appModel.notifyAppGoBackground(appName);
            KillDecision killDecision2 = this.killDecision;
            if (killDecision2 != null) {
                killDecision2.notifyAkBg(appName);
            }
        }
    }

    private void notifyAppStart(String appName) {
        if (this.logEnable) {
            Slog.i(TAG, "Entering notifyAppStart. appName:" + appName);
        }
        this.appScore.setScoreAndRatioByScene(appName, SceneConst.SceneEvent.valueOf("APP_START"));
    }

    private void notifyAppFreeze(String appName) {
        if (this.logEnable) {
            Slog.i(TAG, "Entering notifyAppFreeze. appName:" + appName);
        }
        this.appModel.notifyAppFreeze(appName);
        this.appScore.setScoreAndRatioByScene(appName, SceneConst.SceneEvent.valueOf("FREEZE"));
    }

    private void notifyAppUnFreeze(String appName) {
        if (this.logEnable) {
            Slog.i(TAG, "Entering notifyAppUnFreeze. appName:" + appName);
        }
        this.appModel.notifyAppUnFreeze(appName);
        this.appScore.setScoreAndRatioByScene(appName, SceneConst.SceneEvent.valueOf("UNFREEZE"));
    }

    private void activityAppCheckAndSet(String appName, int pid) {
        Set<String> set = this.activeAppSet;
        if (set != null && this.kernelInterface != null && appName != null) {
            if (set.contains(appName)) {
                Slog.i(TAG, "activityAppCheckAndSet. appName:" + appName + "pid" + pid);
                this.kernelInterface.changeGroupToActivity(pid);
                Message msg = this.handler.obtainMessage();
                msg.obj = appName;
                msg.what = 2;
                this.handler.sendMessageDelayed(msg, 1000);
                return;
            }
            Slog.i(TAG, "add pid to " + appName + ", pid: " + pid);
            this.kernelInterface.addToProcs(appName, pid);
        }
    }

    private void notifyProcessCreate(String appName, int uid, int pid) {
        if (this.logEnable) {
            Slog.i(TAG, "Entering notifyProcessCreate. appName:" + appName + " uid:" + uid + "pid" + pid);
        }
        if (this.appModel.isAppDied(appName)) {
            Slog.i(TAG, "Entering appColdCheck cold! appName:" + appName);
            if (!this.mkdirAppSet.contains(appName)) {
                if (this.kernelInterface.mkDirectory(appName)) {
                    this.mkdirAppSet.add(appName);
                }
                Slog.i(TAG, "Entering appColdCheck cold! Mkdir. appName:" + appName);
            }
            notifyAppStart(appName);
            if (this.maxApplication > 0) {
                checkMaxApplicationNum();
            }
        }
        activityAppCheckAndSet(appName, pid);
        this.appModel.notifyProcessCreate(appName, uid, pid);
    }

    private void notifyProcessDied(String appName, int uid, int pid) {
        if (this.logEnable) {
            Slog.i(TAG, "Entering notifyProcessDied. appName:" + appName + " uid:" + uid + "pid" + pid);
        }
        this.appModel.notifyProcessDied(appName, uid, pid);
        if (this.appModel.isAppDied(appName)) {
            Slog.i(TAG, "This app was killed: " + appName);
            Message msg = this.handler.obtainMessage();
            msg.obj = appName;
            msg.what = 2;
            this.handler.sendMessageDelayed(msg, 1000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetQuickKillBuffer() {
        this.killDecision.setQuickKillBuffer(this.quickKillBuffer);
    }

    public boolean isInDeepSleep() {
        return isInDeepSleep;
    }

    public int getCurrentAppUid() {
        return this.curFrontAppUid;
    }

    private void notifyScreenOn() {
        this.appModel.writeRunningStateLog("ScreenOn");
        this.handler.removeMessages(5);
        isInDeepSleep = false;
        this.killDecision.setQuickKillBuffer(this.screenOffQuickBuffer);
        Message msg = this.handler.obtainMessage();
        msg.what = 4;
        this.handler.sendMessageDelayed(msg, HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD);
        this.bufferProc.enableZswapd();
        this.killDecision.notifyScreenOn();
    }

    private void notifyScreenOff() {
        this.appModel.writeRunningStateLog("ScreenOff");
        this.handler.removeMessages(5);
        Message msg = this.handler.obtainMessage();
        msg.what = 5;
        this.handler.sendMessageDelayed(msg, 180000);
        this.killDecision.serializeModel();
        this.bufferProc.notifyToSetBufferScreenClose();
    }

    private class SceneParse implements Runnable {
        SceneConst.ScenePara para;
        String pkg;
        int type;

        SceneParse(int type2, SceneConst.ScenePara para2) {
            this.type = type2;
            this.para = para2;
        }

        SceneParse(int type2, String pkg2) {
            this.type = type2;
            this.pkg = pkg2;
        }

        @Override // java.lang.Runnable
        public void run() {
            SceneConst.ScenePara scenePara;
            if (this.type == 1 && (scenePara = this.para) != null) {
                SceneProcessing.this.handleNotifySceneData(scenePara);
            } else if (this.type != 2 || this.pkg == null) {
                Slog.e(SceneProcessing.TAG, "handle error message");
            } else {
                long beforeTime = System.currentTimeMillis();
                if (!SceneProcessing.this.kernelInterface.delDirectory(this.pkg)) {
                    Slog.i(SceneProcessing.TAG, "Delete memcg fail.");
                }
                SceneProcessing.this.mkdirAppSet.remove(this.pkg);
                long totalCost = System.currentTimeMillis() - beforeTime;
                if (totalCost > HwSwingMotionGestureConstant.HOVER_SCREEN_OFF_THRESHOLD) {
                    Slog.i(SceneProcessing.TAG, "handleNotifySceneData::delmemcg. total cost time = " + totalCost + "ms. Time out! >5s");
                }
            }
        }
    }
}
