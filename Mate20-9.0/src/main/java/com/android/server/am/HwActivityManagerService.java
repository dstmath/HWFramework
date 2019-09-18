package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.HwRecentTaskInfo;
import android.app.IActivityController;
import android.app.IApplicationThread;
import android.app.ITaskStackListener;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.HwPCMultiWindowCompatibility;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.HwSysResource;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.TimingsTraceLog;
import android.view.WindowManagerPolicyConstants;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.server.LocalServices;
import com.android.server.SMCSAMSHelper;
import com.android.server.ServiceThread;
import com.android.server.gesture.GestureNavConst;
import com.android.server.input.HwInputManagerService;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.pm.auth.HwCertification;
import com.android.server.rms.iaware.appmng.AwareFakeActivityRecg;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.srms.BroadcastFeature;
import com.android.server.rms.iaware.srms.ResourceFeature;
import com.android.server.rms.iaware.srms.SRMSDumpRadar;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.HwWindowManagerService;
import com.huawei.android.app.IGameObserver;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public final class HwActivityManagerService extends ActivityManagerService {
    public static final int BACKUP_APP_ADJ = 300;
    static final boolean DEBUG_HWTRIM = smcsLOGV;
    static final boolean DEBUG_HWTRIM_PERFORM = smcsLOGV;
    public static final int FOREGROUND_APP_ADJ = 0;
    public static final int HEAVY_WEIGHT_APP_ADJ = 400;
    public static final int HOME_APP_ADJ = 600;
    private static final String HW_TRIM_MEMORY_ACTION = "huawei.intent.action.HW_TRIM_MEMORY_ACTION";
    public static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", MemoryConstant.MEM_SCENE_DEFAULT));
    public static final int NATIVE_ADJ = -1000;
    public static final int PERCEPTIBLE_APP_ADJ = 200;
    public static final int PERSISTENT_PROC_ADJ = -800;
    public static final int PERSISTENT_SERVICE_ADJ = -700;
    public static final int PREVIOUS_APP_ADJ = 700;
    private static final int QUEUE_NUM_DEFAULT = 2;
    private static final int QUEUE_NUM_IAWARE = 6;
    private static final int QUEUE_NUM_RMS = 4;
    public static final int SERVICE_ADJ = 500;
    public static final int SERVICE_B_ADJ = 800;
    static final int SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION = 2101;
    public static final int SYSTEM_ADJ = -900;
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    static final String TAG = "HwActivityManagerService";
    public static final int VISIBLE_APP_ADJ = 100;
    private static final String descriptor = "android.app.IActivityManager";
    private static boolean enableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
    static final boolean enableRms = SystemProperties.getBoolean("ro.config.enable_rms", false);
    private static final boolean mIsSMCSHWSYSMEnabled = SystemProperties.getBoolean("ro.enable.hwsysm_smcs", true);
    private static HwActivityManagerService mSelf;
    private static Set<String> sAllowedCrossUserForCloneArrays = new HashSet();
    private static Set<String> sPreventStartWhenSleeping = new HashSet();
    static final boolean smcsLOGV = SystemProperties.getBoolean("ro.enable.st_debug", false);
    private HwSysResource mAppResource;
    public HwSysResource mAppServiceResource;
    private HwSysResource mBroadcastResource;
    private HashMap<Integer, Intent> mCurrentSplitIntent = new HashMap<>();
    IActivityController mCustomController = null;
    Handler mHandler = new Handler();
    private HwGameAssistantController mHwGameAssistantController;
    private Intent mLastSplitIntent;
    private HwSysResource mOrderedBroadcastResource;
    OverscanTimeout mOverscanTimeout = new OverscanTimeout();
    /* access modifiers changed from: private */
    public boolean[] mScreenStatusRequest = {false, false};
    private HashMap<Integer, Stack<IBinder>> mSplitActivityEntryStack;
    private Bundle mSplitExtras;
    private int mSrvFlagLocked = 0;

    class OverscanTimeout implements Runnable {
        OverscanTimeout() {
        }

        public void run() {
            Slog.i(HwActivityManagerService.TAG, "OverscanTimeout run");
            Settings.Global.putString(HwActivityManagerService.this.mContext.getContentResolver(), "single_hand_mode", "");
        }
    }

    class ScreenStatusReceiver extends BroadcastReceiver {
        ScreenStatusReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.stk.check_screen_idle".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("slot_id", 0);
                if (slotId < 0 || slotId >= HwActivityManagerService.this.mScreenStatusRequest.length) {
                    Slog.w(HwActivityManagerService.TAG, "ScreenStatusReceiver, slotId " + slotId + " Invalid");
                    return;
                }
                HwActivityManagerService.this.mScreenStatusRequest[slotId] = intent.getBooleanExtra("SCREEN_STATUS_REQUEST", false);
                if (HwActivityManagerService.this.mScreenStatusRequest[slotId]) {
                    ActivityRecord p = HwActivityManagerService.this.getFocusedStack().topRunningActivityLocked();
                    if (p != null) {
                        Intent StkIntent = new Intent("com.huawei.intent.action.stk.idle_screen");
                        if (p.intent.hasCategory("android.intent.category.HOME")) {
                            StkIntent.putExtra("SCREEN_IDLE", true);
                        } else {
                            StkIntent.putExtra("SCREEN_IDLE", false);
                        }
                        StkIntent.putExtra("slot_id", slotId);
                        StkIntent.addFlags(16777216);
                        HwActivityManagerService.this.mContext.sendBroadcast(StkIntent, "com.huawei.permission.CAT_IDLE_SCREEN");
                        if (ActivityManagerDebugConfig.DEBUG_ALL) {
                            Slog.v(HwActivityManagerService.TAG, "Broadcasting Home Idle Screen Intent ... slot: " + slotId);
                        }
                    }
                } else if (ActivityManagerDebugConfig.DEBUG_ALL) {
                    Slog.v(HwActivityManagerService.TAG, "Screen Status request is OFF, slot: " + slotId);
                }
            }
        }
    }

    class TrimMemoryReceiver extends BroadcastReceiver {
        TrimMemoryReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getAction() == null || !HwActivityManagerService.HW_TRIM_MEMORY_ACTION.equals(intent.getAction()))) {
                HwActivityManagerService.this.trimGLMemory(80);
            }
        }
    }

    static {
        sAllowedCrossUserForCloneArrays.add(HwCertification.SIGNATURE_MEDIA);
        sAllowedCrossUserForCloneArrays.add("com.android.providers.media.documents");
        sAllowedCrossUserForCloneArrays.add("com.huawei.android.launcher.settings");
        sAllowedCrossUserForCloneArrays.add("com.android.badge");
        sAllowedCrossUserForCloneArrays.add("com.android.providers.media");
        sAllowedCrossUserForCloneArrays.add("android.media.IMediaScannerService");
        sAllowedCrossUserForCloneArrays.add("com.android.contacts.files");
        sAllowedCrossUserForCloneArrays.add("com.android.contacts.app");
        sAllowedCrossUserForCloneArrays.add("com.huawei.numberlocation");
        sAllowedCrossUserForCloneArrays.add("csp-prefs-cfg");
        sAllowedCrossUserForCloneArrays.add("contacts");
        sAllowedCrossUserForCloneArrays.add("com.android.contacts");
        sAllowedCrossUserForCloneArrays.add("android.process.media");
        sAllowedCrossUserForCloneArrays.add(GestureNavConst.DEFAULT_LAUNCHER_PACKAGE);
        sAllowedCrossUserForCloneArrays.add("android.process.acore");
        sAllowedCrossUserForCloneArrays.add("call_log");
        sAllowedCrossUserForCloneArrays.add("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        sAllowedCrossUserForCloneArrays.add("android.intent.action.MEDIA_SCANNER_SCAN_FOLDER");
        sAllowedCrossUserForCloneArrays.add("com.android.launcher.action.INSTALL_SHORTCUT");
        sAllowedCrossUserForCloneArrays.add("mms");
        sAllowedCrossUserForCloneArrays.add("sms");
        sAllowedCrossUserForCloneArrays.add("mms-sms");
        sAllowedCrossUserForCloneArrays.add("com.android.providers.downloads");
        sAllowedCrossUserForCloneArrays.add("downloads");
        sAllowedCrossUserForCloneArrays.add("com.android.providers.downloads.documents");
        sPreventStartWhenSleeping.add("com.ss.android.article.news/com.ss.android.message.sswo.SswoActivity");
        sPreventStartWhenSleeping.add("com.ss.android.article.video/com.ss.android.message.sswo.SswoActivity");
        sPreventStartWhenSleeping.add("dongzheng.szkingdom.android.phone/com.dgzq.IM.ui.activity.KeepAliveActivity");
        sPreventStartWhenSleeping.add("com.tencent.news/.push.alive.offactivity.OffActivity");
    }

    public HwActivityManagerService(Context mContext) {
        super(mContext);
        mSelf = this;
        if (SystemProperties.getInt("ro.config.gameassist", 0) == 1) {
            this.mHwGameAssistantController = new HwGameAssistantController(this);
        } else {
            this.mHwGameAssistantController = null;
        }
    }

    public static HwActivityManagerService self() {
        return mSelf;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        ComponentName _arg0;
        ComponentName _arg02;
        if (code == 502) {
            data.enforceInterface(descriptor);
            boolean res = handleANRFilterFIFO(data.readInt(), data.readInt());
            reply.writeNoException();
            reply.writeInt(res);
            return true;
        } else if (code == 504) {
            data.enforceInterface(descriptor);
            String packageName = getPackageNameForPid(data.readInt());
            reply.writeNoException();
            reply.writeString(packageName);
            return true;
        } else if (code != SET_CUSTOM_ACTIVITY_CONTROLLER_TRANSACTION) {
            boolean forLast = false;
            if (code != 1599294787) {
                Bundle _arg2 = null;
                switch (code) {
                    case WifiProCommonUtils.RESP_CODE_UNSTABLE /*601*/:
                        data.enforceInterface(descriptor);
                        Intent intent = (Intent) data.readParcelable(null);
                        int pid = data.readInt();
                        Bundle bundle = data.readBundle();
                        if (data.readInt() > 0) {
                            forLast = true;
                        }
                        setIntentInfo(intent, pid, bundle, forLast);
                        reply.writeNoException();
                        return true;
                    case WifiProCommonUtils.RESP_CODE_GATEWAY /*602*/:
                        data.enforceInterface(descriptor);
                        Parcelable[] p = getIntentInfo(data.readInt(), data.readInt() > 0);
                        reply.writeNoException();
                        reply.writeParcelableArray(p, 0);
                        return true;
                    case WifiProCommonUtils.RESP_CODE_INVALID_URL /*603*/:
                        data.enforceInterface(descriptor);
                        addToEntryStack(data.readInt(), data.readStrongBinder(), data.readInt(), (Intent) Intent.CREATOR.createFromParcel(data));
                        reply.writeNoException();
                        return true;
                    case WifiProCommonUtils.RESP_CODE_ABNORMAL_SERVER /*604*/:
                        data.enforceInterface(descriptor);
                        clearEntryStack(data.readInt(), data.readStrongBinder());
                        return true;
                    case WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED /*605*/:
                        data.enforceInterface(descriptor);
                        removeFromEntryStack(data.readInt(), data.readStrongBinder());
                        return true;
                    case WifiProCommonUtils.RESP_CODE_CONN_RESET /*606*/:
                        data.enforceInterface(descriptor);
                        boolean isTop = isTopSplitActivity(data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(isTop);
                        return true;
                    default:
                        switch (code) {
                            case 3211:
                                data.enforceInterface(descriptor);
                                if (data.readInt() != 0) {
                                    _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                                } else {
                                    _arg0 = null;
                                }
                                if (data.readInt() != 0) {
                                    _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                                }
                                boolean _result = requestContentNode(_arg0, _arg2, data.readInt());
                                reply.writeNoException();
                                reply.writeInt(_result);
                                return true;
                            case 3212:
                                data.enforceInterface(descriptor);
                                if (data.readInt() != 0) {
                                    _arg02 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                                } else {
                                    _arg02 = null;
                                }
                                if (data.readInt() != 0) {
                                    _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                                }
                                boolean _result2 = requestContentOther(_arg02, _arg2, data.readInt());
                                reply.writeNoException();
                                reply.writeInt(_result2);
                                return true;
                            default:
                                switch (code) {
                                    case 3301:
                                        List<String> addList = new ArrayList<>();
                                        data.enforceInterface(descriptor);
                                        data.readStringList(addList);
                                        boolean _result3 = addGameSpacePackageList(addList);
                                        reply.writeNoException();
                                        reply.writeInt(_result3);
                                        return true;
                                    case 3302:
                                        List<String> delList = new ArrayList<>();
                                        data.enforceInterface(descriptor);
                                        data.readStringList(delList);
                                        boolean _result4 = delGameSpacePackageList(delList);
                                        reply.writeNoException();
                                        reply.writeInt(_result4);
                                        return true;
                                    case 3303:
                                        data.enforceInterface(descriptor);
                                        boolean _result5 = isInGameSpace(data.readString());
                                        reply.writeNoException();
                                        reply.writeInt(_result5);
                                        return true;
                                    case 3304:
                                        data.enforceInterface(descriptor);
                                        List<String> packageList = getGameList();
                                        reply.writeNoException();
                                        reply.writeStringList(packageList);
                                        return true;
                                    case 3305:
                                        data.enforceInterface(descriptor);
                                        registerGameObserver(IGameObserver.Stub.asInterface(data.readStrongBinder()));
                                        reply.writeNoException();
                                        return true;
                                    case 3306:
                                        data.enforceInterface(descriptor);
                                        unregisterGameObserver(IGameObserver.Stub.asInterface(data.readStrongBinder()));
                                        reply.writeNoException();
                                        return true;
                                    case 3307:
                                        data.enforceInterface(descriptor);
                                        boolean isDndOn = isGameDndOn();
                                        reply.writeNoException();
                                        reply.writeInt(isDndOn);
                                        return true;
                                    case 3308:
                                        data.enforceInterface(descriptor);
                                        boolean isKeyControlOn = isGameKeyControlOn();
                                        reply.writeNoException();
                                        reply.writeInt(isKeyControlOn);
                                        return true;
                                    case 3309:
                                        data.enforceInterface(descriptor);
                                        boolean isGestureDisabled = isGameGestureDisabled();
                                        reply.writeNoException();
                                        reply.writeInt(isGestureDisabled);
                                        return true;
                                }
                        }
                }
            } else {
                if (DEBUG_HWTRIM) {
                    Log.v(TAG, "AMS.onTransact: got HWMEMCLEAN_TRANSACTION");
                }
                if (mIsSMCSHWSYSMEnabled) {
                    if (this.mContext == null) {
                        return false;
                    }
                    if (this.mContext.checkCallingPermission("huawei.permission.HSM_SMCS") != 0) {
                        if (DEBUG_HWTRIM) {
                            Log.e(TAG, "SMCSAMSHelper.handleTransact permission deny");
                        }
                        return false;
                    } else if (SMCSAMSHelper.getInstance().handleTransact(data, reply, flags)) {
                        return true;
                    }
                } else if (DEBUG_HWTRIM) {
                    Log.v(TAG, "AMS.onTransact: HWSysM SMCS is disabled.");
                }
            }
            return HwActivityManagerService.super.onTransact(code, data, reply, flags);
        } else {
            data.enforceInterface(descriptor);
            setCustomActivityController(IActivityController.Stub.asInterface(data.readStrongBinder()));
            reply.writeNoException();
            return true;
        }
    }

    public void addCallerToIntent(Intent intent, IApplicationThread caller) {
        String callerPackage = null;
        if (caller != null) {
            ProcessRecord callerApp = getRecordForAppLocked(caller);
            if (callerApp != null) {
                callerPackage = callerApp.info.packageName;
            }
        }
        if (callerPackage != null) {
            try {
                if (isInstall(intent)) {
                    String callerIndex = intent.getStringExtra("caller_package");
                    if (callerIndex != null) {
                        callerPackage = callerIndex;
                    }
                    intent.putExtra("caller_package", callerPackage);
                }
            } catch (Exception e) {
                Log.e(TAG, "Get package info faild:" + e);
            }
        }
    }

    private boolean isInstall(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        boolean story = false;
        if ("android.intent.action.INSTALL_PACKAGE".equals(action)) {
            story = true;
        }
        if ("application/vnd.android.package-archive".equals(type)) {
            return true;
        }
        return story;
    }

    public Configuration getCurNaviConfiguration() {
        return this.mWindowManager.getCurNaviConfiguration();
    }

    public String topAppName() {
        ActivityStack focusedStack;
        synchronized (this) {
            focusedStack = getFocusedStack();
        }
        ActivityRecord r = focusedStack.topRunningActivityLocked();
        if (r != null) {
            return r.shortComponentName;
        }
        return null;
    }

    public boolean serviceIsRunning(ComponentName serviceCmpName, int curUser) {
        boolean z;
        synchronized (this) {
            Slog.d(TAG, "serviceIsRunning, for user " + curUser + ", serviceCmpName " + serviceCmpName);
            z = this.mServices.getServicesLocked(curUser).get(serviceCmpName) != null;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void setDeviceProvisioned() {
        ContentResolver cr = this.mContext.getContentResolver();
        if ((Settings.Global.getInt(cr, "device_provisioned", 0) == 0 || Settings.Secure.getInt(cr, "user_setup_complete", 0) == 0) && ServiceManager.getService("package").getHwPMSEx().isSetupDisabled()) {
            Settings.Global.putInt(cr, "device_provisioned", 1);
            Settings.Secure.putInt(cr, "user_setup_complete", 1);
        }
    }

    public void systemReady(Runnable goingCallback, TimingsTraceLog traceLog) {
        if (!this.mSystemReady) {
            setDeviceProvisioned();
        }
        HwActivityManagerService.super.systemReady(goingCallback, traceLog);
        this.mContext.registerReceiver(new ScreenStatusReceiver(), new IntentFilter("android.intent.action.stk.check_screen_idle"), "com.huawei.permission.STK_CHECK_SCREEN_IDLE", null);
        this.mContext.registerReceiver(new TrimMemoryReceiver(), new IntentFilter(HW_TRIM_MEMORY_ACTION));
        if (this.mHwGameAssistantController != null) {
            this.mHwGameAssistantController.systemReady();
        }
    }

    public ArrayList<Integer> getIawareDumpData() {
        ArrayList<Integer> queueSizes = new ArrayList<>();
        for (BroadcastQueue queue : this.mBroadcastQueues) {
            ArrayList<Integer> queueSizesTemp = queue.getIawareDumpData();
            if (queueSizesTemp != null) {
                queueSizes.addAll(queueSizesTemp);
            }
        }
        return queueSizes;
    }

    public void updateSRMSStatisticsData(int subTypeCode) {
        SRMSDumpRadar.getInstance().updateStatisticsData(subTypeCode);
    }

    public boolean getIawareResourceFeature(int type) {
        if (type >= 1 && type <= 2) {
            return ResourceFeature.getIawareResourceFeature(type);
        }
        if (type < 10 || type > 11) {
            return false;
        }
        return BroadcastFeature.isFeatureEnabled(type);
    }

    public long proxyBroadcast(List<String> pkgs, boolean proxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                long temp = queue.proxyBroadcast(pkgs, proxy);
                delay = temp > delay ? temp : delay;
            }
        }
        return delay;
    }

    public long proxyBroadcastByPid(List<Integer> pids, boolean proxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                long temp = queue.proxyBroadcastByPid(pids, proxy);
                delay = temp > delay ? temp : delay;
            }
        }
        return delay;
    }

    public void setProxyBCActions(List<String> actions) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.setProxyBCActions(actions);
            }
        }
    }

    public void setActionExcludePkg(String action, String pkg) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.setActionExcludePkg(action, pkg);
            }
        }
    }

    public void proxyBCConfig(int type, String key, List<String> value) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.proxyBCConfig(type, key, value);
            }
        }
    }

    public void checkIfScreenStatusRequestAndSendBroadcast() {
        for (int slotId = 0; slotId < this.mScreenStatusRequest.length; slotId++) {
            if (this.mScreenStatusRequest[slotId]) {
                Intent StkIntent = new Intent("com.huawei.intent.action.stk.idle_screen");
                StkIntent.addFlags(16777216);
                StkIntent.putExtra("SCREEN_IDLE", true);
                StkIntent.putExtra("slot_id", slotId);
                this.mContext.sendBroadcast(StkIntent, "com.huawei.permission.CAT_IDLE_SCREEN");
            }
        }
    }

    public void setServiceFlagLocked(int servFlag) {
        this.mSrvFlagLocked = servFlag;
    }

    public boolean isProcessExistLocked(String processName, int uid) {
        return getProcessRecordLocked(processName, uid, true) != null;
    }

    public ProcessRecord getProcessRecordLocked(int pid) {
        ProcessRecord proc = null;
        if (pid >= 0) {
            synchronized (this.mPidsSelfLocked) {
                proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            }
        }
        return proc;
    }

    public boolean shouldPreventSendReceiver(Intent intent, ResolveInfo resolveInfo, int callerPid, int callerUid, ProcessRecord targetApp, ProcessRecord callerApp) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy != null) {
            return appStartupPolicy.shouldPreventSendReceiver(intent, resolveInfo, callerPid, callerUid, targetApp, callerApp);
        }
        return false;
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid, ProcessRecord callerApp, boolean servExist, Intent service) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy == null) {
            return false;
        }
        return appStartupPolicy.shouldPreventStartService(servInfo, callerPid, callerUid, callerApp, this.mSrvFlagLocked, servExist, service);
    }

    public boolean shouldPreventActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record, int callerPid, int callerUid, ProcessRecord callerApp) {
        if (intent == null || aInfo == null || record == null) {
            return false;
        }
        if (isSleepingLocked() && sPreventStartWhenSleeping.contains(record.shortComponentName)) {
            return true;
        }
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy == null) {
            return false;
        }
        boolean shouldPrevent = appStartupPolicy.shouldPreventStartActivity(intent, aInfo, record, callerPid, callerUid, callerApp);
        if (!shouldPrevent) {
            shouldPrevent = AwareFakeActivityRecg.self().shouldPreventStartActivity(aInfo, callerPid, callerUid, this.mBatteryStatsService.getActiveStatistics().isScreenOn());
        }
        return shouldPrevent;
    }

    public boolean shouldPreventRestartService(ServiceInfo sInfo, boolean realStart) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy != null) {
            return appStartupPolicy.shouldPreventRestartService(sInfo, realStart);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void exitSingleHandMode() {
        this.mHandler.removeCallbacks(this.mOverscanTimeout);
        this.mHandler.postDelayed(this.mOverscanTimeout, 200);
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, ProcessRecord callerApp) {
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy != null) {
            return appStartupPolicy.shouldPreventStartProvider(cpi, callerPid, callerUid, callerApp);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void setCustomActivityController(IActivityController controller) {
        enforceCallingPermission("android.permission.SET_ACTIVITY_WATCHER", "setCustomActivityController()");
        synchronized (this) {
            this.mCustomController = controller;
        }
        HwInputManagerService.HwInputManagerServiceInternal inputManager = (HwInputManagerService.HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerService.HwInputManagerServiceInternal.class);
        if (inputManager != null) {
            inputManager.setCustomActivityController(controller);
        }
    }

    public void setRequestedOrientation(IBinder token, int requestedOrientation) {
        HwActivityManagerService.super.setRequestedOrientation(token, requestedOrientation);
        if (this.mWindowManager.getLazyMode() != 0) {
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this) {
                    if (requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8 || requestedOrientation == 11) {
                        Settings.Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "");
                    }
                }
                Binder.restoreCallingIdentity(origId);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean customActivityStarting(Intent intent, String packageName) {
        if (this.mCustomController != null) {
            boolean startOK = true;
            try {
                startOK = this.mCustomController.activityStarting(intent.cloneFilter(), packageName);
            } catch (RemoteException e) {
                this.mCustomController = null;
                HwInputManagerService.HwInputManagerServiceInternal inputManager = (HwInputManagerService.HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerService.HwInputManagerServiceInternal.class);
                if (inputManager != null) {
                    inputManager.setCustomActivityController(null);
                }
            }
            if (!startOK) {
                Slog.i(TAG, "Not starting activity because custom controller stop it");
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean customActivityResuming(String packageName) {
        if (this.mCustomController != null) {
            boolean resumeOK = true;
            try {
                resumeOK = this.mCustomController.activityResuming(packageName);
            } catch (RemoteException e) {
                this.mCustomController = null;
                HwInputManagerService.HwInputManagerServiceInternal inputManager = (HwInputManagerService.HwInputManagerServiceInternal) LocalServices.getService(HwInputManagerService.HwInputManagerServiceInternal.class);
                if (inputManager != null) {
                    inputManager.setCustomActivityController(null);
                }
            }
            if (!resumeOK) {
                Slog.i(TAG, "Not resuming activity because custom controller stop it");
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue[] initialBroadcastQueue() {
        int queueNum;
        if (enableIaware) {
            queueNum = 6;
        } else if (enableRms != 0) {
            queueNum = 4;
        } else {
            queueNum = 2;
        }
        return new BroadcastQueue[queueNum];
    }

    /* access modifiers changed from: protected */
    public void setThirdPartyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (enableRms || enableIaware) {
            ServiceThread thirdAppHandlerThread = new ServiceThread("ThirdAppHandlerThread", 10, false);
            thirdAppHandlerThread.start();
            Handler thirdAppHandler = new Handler(thirdAppHandlerThread.getLooper());
            HwBroadcastQueue hwBroadcastQueue = new HwBroadcastQueue(this, thirdAppHandler, "fgthirdapp", (long) BROADCAST_FG_TIMEOUT, false);
            this.mFgThirdAppBroadcastQueue = hwBroadcastQueue;
            HwBroadcastQueue hwBroadcastQueue2 = new HwBroadcastQueue(this, thirdAppHandler, "bgthirdapp", (long) BROADCAST_BG_TIMEOUT, false);
            this.mBgThirdAppBroadcastQueue = hwBroadcastQueue2;
            broadcastQueues[2] = this.mFgThirdAppBroadcastQueue;
            broadcastQueues[3] = this.mBgThirdAppBroadcastQueue;
        }
    }

    /* access modifiers changed from: protected */
    public void setKeyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (enableIaware) {
            ServiceThread keyAppHandlerThread = new ServiceThread("keyAppHanderThread", 0, false);
            keyAppHandlerThread.start();
            Handler keyAppHandler = new Handler(keyAppHandlerThread.getLooper());
            HwBroadcastQueue hwBroadcastQueue = new HwBroadcastQueue(this, keyAppHandler, "fgkeyapp", (long) BROADCAST_FG_TIMEOUT, false);
            this.mFgKeyAppBroadcastQueue = hwBroadcastQueue;
            HwBroadcastQueue hwBroadcastQueue2 = new HwBroadcastQueue(this, keyAppHandler, "bgkeyapp", (long) BROADCAST_BG_TIMEOUT, false);
            this.mBgKeyAppBroadcastQueue = hwBroadcastQueue2;
            broadcastQueues[4] = this.mFgKeyAppBroadcastQueue;
            broadcastQueues[5] = this.mBgKeyAppBroadcastQueue;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isThirdPartyAppBroadcastQueue(ProcessRecord callerApp) {
        boolean z = false;
        if ((!enableRms && !getIawareResourceFeature(1)) || callerApp == null) {
            return false;
        }
        if (DEBUG_HWTRIM || Log.HWINFO) {
            Log.i(TAG, "Split enqueueing broadcast [callerApp]:" + callerApp);
        }
        if (callerApp.instr != null) {
            return false;
        }
        if ((callerApp.info.flags & 1) == 0 || (callerApp.info.hwFlags & 33554432) != 0) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean isKeyAppBroadcastQueue(int type, String name) {
        return getIawareResourceFeature(1) && name != null && isKeyApp(type, 0, name);
    }

    /* access modifiers changed from: protected */
    public boolean isThirdPartyAppPendingBroadcastProcessLocked(int pid) {
        boolean z = false;
        if (!enableRms && !getIawareResourceFeature(1)) {
            return false;
        }
        if (this.mFgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid) || this.mBgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid)) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean isKeyAppPendingBroadcastProcessLocked(int pid) {
        boolean z = true;
        if (!getIawareResourceFeature(1) || this.mFgKeyAppBroadcastQueue == null || this.mBgKeyAppBroadcastQueue == null) {
            return false;
        }
        if (!this.mFgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid) && !this.mBgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid)) {
            z = false;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue thirdPartyAppBroadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        if (DEBUG_HWTRIM || Log.HWINFO) {
            StringBuilder sb = new StringBuilder();
            sb.append("thirdAppBroadcastQueueForIntent intent ");
            sb.append(intent);
            sb.append(" on ");
            sb.append(isFg ? "fgthirdapp" : "bgthirdapp");
            sb.append(" queue");
            Log.i(TAG, sb.toString());
        }
        return isFg ? this.mFgThirdAppBroadcastQueue : this.mBgThirdAppBroadcastQueue;
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue keyAppBroadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        if (DEBUG_HWTRIM || Log.HWINFO) {
            StringBuilder sb = new StringBuilder();
            sb.append("keyAppBroadcastQueueForIntent intent ");
            sb.append(intent);
            sb.append(" on ");
            sb.append(isFg ? "fgkeyapp" : "bgkeyapp");
            sb.append(" queue");
            Log.i(TAG, sb.toString());
        }
        if (isFg) {
            updateSRMSStatisticsData(0);
        } else {
            updateSRMSStatisticsData(1);
        }
        return isFg ? this.mFgKeyAppBroadcastQueue : this.mBgKeyAppBroadcastQueue;
    }

    /* access modifiers changed from: protected */
    public void initBroadcastResourceLocked() {
        if (this.mBroadcastResource == null) {
            if (DEBUG_HWTRIM || Log.HWINFO) {
                Log.d(TAG, "init BroadcastResource");
            }
            this.mBroadcastResource = HwFrameworkFactory.getHwResource(11);
        }
    }

    public void checkOrderedBroadcastTimeoutLocked(String actionOrPkg, int timeCost, boolean isInToOut) {
        if (getIawareResourceFeature(2)) {
            if (this.mOrderedBroadcastResource == null) {
                if (DEBUG_HWTRIM || Log.HWINFO) {
                    Log.d(TAG, "init OrderedBroadcastResource");
                }
                this.mOrderedBroadcastResource = HwFrameworkFactory.getHwResource(31);
            }
            if (this.mOrderedBroadcastResource != null && !isInToOut) {
                this.mOrderedBroadcastResource.acquire(0, actionOrPkg, 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkBroadcastRecordSpeed(int callingUid, String callerPackage, ProcessRecord callerApp) {
        if (this.mBroadcastResource != null && callerApp != null) {
            int uid = callingUid;
            String pkg = callerPackage;
            int processType = getProcessType(callerApp);
            if (("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || processType == 0) && 2 == this.mBroadcastResource.acquire(uid, pkg, processType) && ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Log.i(TAG, "This App send broadcast speed is overload! uid = " + uid);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void clearBroadcastResource(ProcessRecord app) {
        if (this.mBroadcastResource != null && app != null) {
            int uid = app.info.uid;
            String pkg = app.info.packageName;
            int processType = getProcessType(app);
            if ("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || processType == 0) {
                this.mBroadcastResource.clear(uid, pkg, processType);
            }
        }
    }

    private int getProcessType(ProcessRecord app) {
        return (app.info.flags & 1) != 0 ? 2 : 0;
    }

    public boolean isKeyApp(int type, int value, String key) {
        if (this.mBroadcastResource == null || key == null || 1 != this.mBroadcastResource.queryPkgPolicy(type, value, key)) {
            return false;
        }
        if (Log.HWLog) {
            Log.i(TAG, "isKeyApp in whiteList key:" + key + " , type is " + type);
        }
        return true;
    }

    private void initAppResourceLocked() {
        if (this.mAppResource == null) {
            Log.i(TAG, "init Appresource");
            this.mAppResource = HwFrameworkFactory.getHwResource(18);
        }
    }

    private void initAppServiceResourceLocked() {
        if (this.mAppServiceResource == null) {
            Log.i(TAG, "init AppServiceResource");
            this.mAppServiceResource = HwFrameworkFactory.getHwResource(17);
        }
    }

    public void initAppAndAppServiceResourceLocked() {
        initAppResourceLocked();
        initAppServiceResourceLocked();
    }

    public boolean isAcquireAppServiceResourceLocked(ServiceRecord sr, ProcessRecord app) {
        if (this.mAppServiceResource == null || sr == null || sr.appInfo.uid <= 0 || sr.appInfo.packageName == null || sr.serviceInfo.name == null || 2 != this.mAppServiceResource.acquire(sr.appInfo.uid, sr.appInfo.packageName, getProcessType(app))) {
            return true;
        }
        Log.i(TAG, "Failed to acquire AppServiceResource:" + sr.serviceInfo.name + " of " + sr.appInfo.packageName + "/" + sr.appInfo.uid);
        return false;
    }

    public boolean isAcquireAppResourceLocked(ProcessRecord app) {
        if (!(this.mAppResource == null || app == null || app.uid <= 0 || app.info == null || app.processName == null || app.startTime <= 0)) {
            int processType = ((app.info.flags & 1) != 0 && (app.info.hwFlags & 33554432) == 0 && (app.info.hwFlags & 67108864) == 0) ? 2 : 0;
            Bundle args = new Bundle();
            args.putInt("callingUid", app.uid);
            args.putString("pkg", app.processName);
            args.putLong("startTime", app.startTime);
            args.putInt("processType", processType);
            args.putBoolean("launchfromActivity", app.launchfromActivity);
            args.putBoolean("topProcess", isTopProcessLocked(app));
            if (2 == this.mAppResource.acquire(null, null, args)) {
                Log.i(TAG, "Failed to acquire AppResource:" + app.info.packageName + "/" + app.uid);
                return false;
            }
        }
        return true;
    }

    private void clearAppServiceResource(ProcessRecord app) {
        if (this.mAppServiceResource != null && app != null) {
            this.mAppServiceResource.clear(app.uid, app.info.packageName, getProcessType(app));
            Log.i(TAG, "clear AppServiceResource of " + app.info.packageName + "/" + app.uid);
        }
    }

    private void clearAppResource(ProcessRecord app) {
        if (this.mAppResource != null && app != null && app.uid > 0 && app.info != null && app.info.packageName != null) {
            this.mAppResource.clear(app.uid, app.info.packageName, ((app.info.flags & 1) != 0 && (app.info.hwFlags & 33554432) == 0 && (app.info.hwFlags & 67108864) == 0) ? 2 : 0);
            Log.i(TAG, "clear Appresource of " + app.info.packageName + "/" + app.uid);
        }
    }

    public void clearAppAndAppServiceResource(ProcessRecord app) {
        clearAppServiceResource(app);
        clearAppResource(app);
    }

    public void trimGLMemory(int level) {
        Slog.i(TAG, " trimGLMemory begin ");
        synchronized (this.mLruProcesses) {
            int list_size = this.mLruProcesses.size();
            for (int i = 0; i < list_size; i++) {
                ProcessRecord app = (ProcessRecord) this.mLruProcesses.get(i);
                if (app.thread != null) {
                    try {
                        app.thread.scheduleTrimMemory(level);
                    } catch (RemoteException e) {
                    }
                }
            }
        }
        Slog.i(TAG, " trimGLMemory end ");
    }

    public Map<Integer, AwareProcessBaseInfo> getAllProcessBaseInfo() {
        ArrayMap<Integer, AwareProcessBaseInfo> list;
        synchronized (this.mPidsSelfLocked) {
            int size = this.mPidsSelfLocked.size();
            list = new ArrayMap<>(size);
            for (int i = 0; i < size; i++) {
                ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                AwareProcessBaseInfo baseInfo = new AwareProcessBaseInfo();
                baseInfo.mCurAdj = p.curAdj;
                baseInfo.mForegroundActivities = p.foregroundActivities;
                baseInfo.mAdjType = p.adjType;
                baseInfo.mHasShownUi = p.hasShownUi;
                baseInfo.mUid = p.uid;
                baseInfo.mAppUid = p.info.uid;
                baseInfo.mSetProcState = p.setProcState;
                list.put(Integer.valueOf(p.pid), baseInfo);
            }
        }
        return list;
    }

    public AwareProcessBaseInfo getProcessBaseInfo(int pid) {
        AwareProcessBaseInfo baseInfo;
        synchronized (this.mPidsSelfLocked) {
            baseInfo = new AwareProcessBaseInfo();
            baseInfo.mCurAdj = 1001;
            ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            if (p != null) {
                baseInfo.mForegroundActivities = p.foregroundActivities;
                baseInfo.mUid = p.uid;
                baseInfo.mAppUid = p.info.uid;
                baseInfo.mSetProcState = p.setProcState;
                baseInfo.mCurAdj = p.curAdj;
                baseInfo.mAdjType = p.adjType;
            }
        }
        return baseInfo;
    }

    public void reportAssocEnable(ArrayMap<Integer, Integer> forePids) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC)) && forePids != null) {
            synchronized (this) {
                Iterator it = this.mLruProcesses.iterator();
                while (it.hasNext()) {
                    ProcessRecord proc = (ProcessRecord) it.next();
                    if (proc != null) {
                        if (proc.foregroundActivities) {
                            forePids.put(Integer.valueOf(proc.pid), Integer.valueOf(proc.uid));
                        }
                        ArrayList<String> pkgs = new ArrayList<>();
                        int size = proc.pkgList.size();
                        for (int i = 0; i < size; i++) {
                            String pkg = (String) proc.pkgList.keyAt(i);
                            if (!pkgs.contains(pkg)) {
                                pkgs.add(pkg);
                            }
                        }
                        Bundle args = new Bundle();
                        args.putInt("callPid", proc.pid);
                        args.putInt("callUid", proc.uid);
                        args.putString("callProcName", proc.processName);
                        args.putInt("userid", proc.userId);
                        args.putStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME, pkgs);
                        args.putInt("relationType", 4);
                        HwSysResManager.getInstance().reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args));
                        Iterator it2 = proc.connections.iterator();
                        while (it2.hasNext()) {
                            ConnectionRecord cr = (ConnectionRecord) it2.next();
                            if (cr != null) {
                                if (cr.binding != null) {
                                    this.mHwAMSEx.reportServiceRelationIAware(1, cr.binding.service, proc);
                                }
                            }
                        }
                        Iterator it3 = proc.conProviders.iterator();
                        while (it3.hasNext()) {
                            ContentProviderConnection cpc = (ContentProviderConnection) it3.next();
                            if (cpc != null) {
                                this.mHwAMSEx.reportServiceRelationIAware(2, cpc.provider, proc);
                            }
                        }
                    }
                }
                this.mHwAMSEx.reportHomeProcess(this.mHomeProcess);
            }
        }
    }

    public void setPackageStoppedState(List<String> packageList, boolean stopped, int targetUid) {
        if (packageList != null) {
            int userId = UserHandle.getUserId(targetUid);
            IPackageManager pm = AppGlobals.getPackageManager();
            try {
                synchronized (this) {
                    for (String packageName : packageList) {
                        pm.setPackageStoppedState(packageName, stopped, userId);
                    }
                }
            } catch (RemoteException e) {
            } catch (IllegalArgumentException e2) {
                Slog.w(TAG, "Failed trying to unstop package " + packageList.toString() + ": " + e2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final boolean cleanUpApplicationRecordLocked(ProcessRecord app, boolean restarting, boolean allowRestart, int index, boolean replacingPid) {
        if (IS_TABLET) {
            if (this.mSplitActivityEntryStack != null && this.mSplitActivityEntryStack.containsKey(Integer.valueOf(app.pid))) {
                Slog.w(TAG, "Split main entrance killed, clear sub activities for " + app.info.packageName + ", pid " + app.pid);
                clearEntryStack(app.pid, null);
                this.mSplitActivityEntryStack.remove(Integer.valueOf(app.pid));
            }
            if (this.mCurrentSplitIntent != null && this.mCurrentSplitIntent.containsKey(Integer.valueOf(app.pid))) {
                this.mCurrentSplitIntent.remove(Integer.valueOf(app.pid));
            }
        }
        return HwActivityManagerService.super.cleanUpApplicationRecordLocked(app, restarting, allowRestart, index, replacingPid);
    }

    public void cleanActivityByUid(List<String> packageList, int targetUid) {
        synchronized (this) {
            int userId = UserHandle.getUserId(targetUid);
            for (String packageName : packageList) {
                if (canCleanTaskRecord(packageName)) {
                    this.mStackSupervisor.finishDisabledPackageActivitiesLocked(packageName, null, true, false, userId);
                }
            }
        }
    }

    public int numOfPidWithActivity(int uid) {
        int count = 0;
        synchronized (this.mPidsSelfLocked) {
            int list_size = this.mPidsSelfLocked.size();
            for (int i = 0; i < list_size; i++) {
                ProcessRecord p = (ProcessRecord) this.mPidsSelfLocked.valueAt(i);
                if (p.uid == uid && p.hasShownUi) {
                    count++;
                }
            }
        }
        return count;
    }

    /* access modifiers changed from: protected */
    public void forceValidateHomeButton(int userId) {
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 0, userId) == 0 || Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
            Settings.Global.putInt(this.mContext.getContentResolver(), "device_provisioned", 1);
            Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "user_setup_complete", 1, userId);
            Log.w(TAG, "DEVICE_PROVISIONED or USER_SETUP_COMPLETE set 0 to 1!");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isStartLauncherActivity(Intent intent, int userId) {
        if (intent == null) {
            Log.w(TAG, "intent is null, not start launcher!");
            return false;
        }
        PackageManager pm = this.mContext.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT");
        ComponentName cmp = intent.getComponent();
        if (pm != null && intent.hasCategory("android.intent.category.HOME")) {
            long origId = Binder.clearCallingIdentity();
            try {
                ResolveInfo info = pm.resolveActivityAsUser(mainIntent, 0, userId);
                if (info == null || info.priority != 0 || cmp == null || info.activityInfo == null || !cmp.getPackageName().equals(info.activityInfo.packageName)) {
                    Binder.restoreCallingIdentity(origId);
                } else {
                    Log.d(TAG, "info priority is 0, cmp: " + cmp + ", userId: " + userId);
                    return true;
                }
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        return r2;
     */
    private String getPackageNameForPid(int pid) {
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord proc = (ProcessRecord) this.mPidsSelfLocked.get(pid);
            if (proc != null) {
                String str = proc.info != null ? proc.info.packageName : "android";
            } else {
                Flog.i(100, "ProcessRecord for pid " + pid + " does not exist");
                return null;
            }
        }
    }

    public boolean isLauncher(String packageName) {
        if (Process.myUid() != 1000 || packageName == null || packageName.trim().isEmpty()) {
            return false;
        }
        if (GestureNavConst.DEFAULT_LAUNCHER_PACKAGE.equals(packageName)) {
            return true;
        }
        if (this.mContext != null) {
            List<ResolveInfo> outActivities = new ArrayList<>();
            PackageManager pm = this.mContext.getPackageManager();
            if (pm != null) {
                ComponentName componentName = pm.getHomeActivities(outActivities);
                if (componentName == null || componentName.getPackageName() == null) {
                    for (ResolveInfo info : outActivities) {
                        String homePkg = info.activityInfo.packageName;
                        if (packageName.equals(homePkg)) {
                            Slog.d(TAG, "homePkg is " + homePkg + " ,isLauncher");
                            return true;
                        }
                    }
                } else if (packageName.equals(componentName.getPackageName())) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private void setIntentInfo(Intent intent, int pid, Bundle bundle, boolean forLast) {
        if (forLast) {
            this.mLastSplitIntent = intent;
            this.mSplitExtras = bundle;
            return;
        }
        if (!this.mCurrentSplitIntent.containsKey(Integer.valueOf(pid))) {
            Log.e(TAG, "CRITICAL_LOG add intent info.");
        }
        this.mCurrentSplitIntent.put(Integer.valueOf(pid), intent);
    }

    private Parcelable[] getIntentInfo(int pid, boolean forLast) {
        if (forLast) {
            return new Parcelable[]{this.mLastSplitIntent, this.mSplitExtras};
        }
        return new Parcelable[]{this.mCurrentSplitIntent.get(Integer.valueOf(pid)), null};
    }

    public void addToEntryStack(int pid, IBinder token, int resultCode, Intent resultData) {
        if (this.mSplitActivityEntryStack == null) {
            this.mSplitActivityEntryStack = new HashMap<>();
        }
        Flog.i(100, "addToEntryStack, activity is " + token);
        Stack<IBinder> pkgStack = this.mSplitActivityEntryStack.get(Integer.valueOf(pid));
        if (pkgStack == null) {
            pkgStack = new Stack<>();
        }
        pkgStack.push(token);
        this.mSplitActivityEntryStack.put(Integer.valueOf(pid), pkgStack);
    }

    public void clearEntryStack(int pid, IBinder selfToken) {
        if (this.mSplitActivityEntryStack != null && !this.mSplitActivityEntryStack.isEmpty()) {
            Stack<IBinder> pkgStack = this.mSplitActivityEntryStack.get(Integer.valueOf(pid));
            if (pkgStack != null && !pkgStack.empty() && (selfToken == null || selfToken.equals(pkgStack.peek()))) {
                long ident = Binder.clearCallingIdentity();
                while (!pkgStack.empty()) {
                    IBinder token = pkgStack.pop();
                    if (token != null && !token.equals(selfToken)) {
                        Flog.i(100, "Clearing entry " + token);
                        finishActivity(token, 0, null, 0);
                    }
                }
                Binder.restoreCallingIdentity(ident);
                if (selfToken != null) {
                    pkgStack.push(selfToken);
                }
            }
        }
    }

    public boolean isTopSplitActivity(int pid, IBinder token) {
        if (this.mSplitActivityEntryStack == null || this.mSplitActivityEntryStack.isEmpty() || token == null) {
            return false;
        }
        Stack<IBinder> pkgStack = this.mSplitActivityEntryStack.get(Integer.valueOf(pid));
        if (pkgStack == null || pkgStack.empty()) {
            return false;
        }
        return token.equals(pkgStack.peek());
    }

    public void removeFromEntryStack(int pid, IBinder token) {
        if (token != null && this.mSplitActivityEntryStack != null) {
            Stack<IBinder> pkgStack = this.mSplitActivityEntryStack.get(Integer.valueOf(pid));
            if (pkgStack != null && pkgStack.empty()) {
                pkgStack.remove(token);
            }
        }
    }

    public boolean isPkgHasAlarm(List<String> packageList, int targetUid) {
        if (packageList == null) {
            return false;
        }
        synchronized (this) {
            for (String packageName : packageList) {
                if (this.mIntentSenderRecords.size() > 0) {
                    for (WeakReference<PendingIntentRecord> wpir : this.mIntentSenderRecords.values()) {
                        if (wpir != null) {
                            PendingIntentRecord pir = (PendingIntentRecord) wpir.get();
                            if (!(pir == null || pir.key == null)) {
                                if (pir.key.packageName != null) {
                                    if (pir.key.packageName.equals(packageName)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    continue;
                }
            }
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004b, code lost:
        return false;
     */
    public boolean requestContentNode(ComponentName componentName, Bundle data, int token) {
        enforceCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "requestContentNode()");
        synchronized (this) {
            ActivityRecord activity = getFocusedStack().getTopActivity();
            ActivityRecord realActivity = getTopActivityAppToken(componentName, activity);
            if (realActivity != null && realActivity.app != null && realActivity.app.thread != null) {
                try {
                    realActivity.app.thread.requestContentNode(realActivity.appToken, data, token);
                    return true;
                } catch (RemoteException e) {
                    Slog.w(TAG, "requestContentNode failed: crash calling " + activity);
                    return false;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004b, code lost:
        return false;
     */
    public boolean requestContentOther(ComponentName componentName, Bundle data, int token) {
        enforceCallingPermission("com.huawei.permission.CONTENT_SENSOR_PERMISSION", "requestContentOther()");
        synchronized (this) {
            ActivityRecord activity = getFocusedStack().getTopActivity();
            ActivityRecord realActivity = getTopActivityAppToken(componentName, activity);
            if (realActivity != null && realActivity.app != null && realActivity.app.thread != null) {
                try {
                    realActivity.app.thread.requestContentOther(realActivity.appToken, data, token);
                    return true;
                } catch (RemoteException e) {
                    Slog.w(TAG, "requestContentOther failed: crash calling " + activity);
                    return false;
                }
            }
        }
    }

    private ActivityRecord getTopActivityAppToken(ComponentName componentName, ActivityRecord activity) {
        if (activity == null) {
            Slog.w(TAG, "requestContent failed: no activity");
            return null;
        } else if (componentName == null || activity.info == null) {
            return null;
        } else {
            if (componentName.equals(activity.info.getComponentName())) {
                Slog.w(TAG, "componentName = " + componentName + " realActivity = " + activity.info.getComponentName() + " isEqual = " + componentName.equals(activity.info.getComponentName()));
                if (this.mLastActivityRecord != null) {
                    Slog.w(TAG, " mLastActivityRecord = " + this.mLastActivityRecord.info.getComponentName());
                }
                return activity;
            }
            ActivityRecord lastResumedActivity = getLastResumedActivity();
            if (lastResumedActivity != null && componentName.equals(lastResumedActivity.info.getComponentName())) {
                Slog.w(TAG, "lastResumedActivity = " + lastResumedActivity.info.getComponentName());
                return lastResumedActivity;
            } else if (this.mLastActivityRecord == null || !componentName.equals(this.mLastActivityRecord.info.getComponentName())) {
                return null;
            } else {
                Slog.w(TAG, " mLastActivityRecord = " + this.mLastActivityRecord.info.getComponentName());
                return this.mLastActivityRecord;
            }
        }
    }

    public int iawareGetUidState(int uid) {
        UidRecord uidRec = (UidRecord) this.mActiveUids.get(uid);
        if (uidRec == null) {
            return 19;
        }
        return uidRec.curProcState;
    }

    /* access modifiers changed from: protected */
    public int handleUserForCloneOrAfw(String name, int userId) {
        if (userId == 0 || name == null) {
            return userId;
        }
        int newUserId = userId;
        if (userId != this.mUserController.getCurrentUserIdLU()) {
            long ident = Binder.clearCallingIdentity();
            try {
                UserInfo ui = this.mUserController.getUserInfo(userId);
                if (ui != null && ((IS_SUPPORT_CLONE_APP && ui.isClonedProfile() && sAllowedCrossUserForCloneArrays.contains(name)) || (ui.isManagedProfile() && "com.huawei.android.launcher.settings".equals(name)))) {
                    newUserId = ui.profileGroupId;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return newUserId;
    }

    /* access modifiers changed from: package-private */
    public final int broadcastIntentLocked(ProcessRecord callerApp, String callerPackage, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions, boolean ordered, boolean sticky, int callingPid, int callingUid, int userId) {
        Intent intent2 = intent;
        String action = null;
        if (intent2 != null) {
            action = intent2.getAction();
        }
        if ("com.android.launcher.action.INSTALL_SHORTCUT".equals(action)) {
            intent2.putExtra("android.intent.extra.USER_ID", userId);
        } else {
            int i = userId;
        }
        if (this.mHwGameAssistantController != null) {
            this.mHwGameAssistantController.handleInterestedBroadcast(intent2);
        }
        if ("android.net.wifi.STATE_CHANGE".equals(action) && getBgBroadcastQueue().getMtmBRManager() != null) {
            if (getBgBroadcastQueue().getMtmBRManager().iawareNeedSkipBroadcastSend(action, new Object[]{intent2})) {
                return 0;
            }
        }
        return HwActivityManagerService.super.broadcastIntentLocked(callerApp, callerPackage, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermissions, appOp, bOptions, ordered, sticky, callingPid, callingUid, userId);
    }

    public int iawareGetUidProcNum(int uid) {
        UidRecord uidRec = (UidRecord) this.mActiveUids.get(uid);
        if (uidRec == null) {
            return 0;
        }
        return uidRec.numProcs;
    }

    public void togglePCMode(boolean pcMode, int displayId) {
        synchronized (this) {
            if (!pcMode) {
                try {
                    ActivityDisplay activityDisplay = (ActivityDisplay) this.mStackSupervisor.mActivityDisplays.get(displayId);
                    if (activityDisplay != null && (this.mStackSupervisor instanceof HwActivityStackSupervisor)) {
                        int size = activityDisplay.getChildCount();
                        ArrayList<ActivityStack> stacks = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            stacks.add(activityDisplay.getChildAt(i));
                        }
                        this.mStackSupervisor.onDisplayRemoved(stacks);
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (this.mWindowManager instanceof HwWindowManagerService) {
                this.mWindowManager.togglePCMode(pcMode, displayId);
            }
        }
    }

    public void updateFingerprintSlideSwitch() {
        if (HwPCUtils.enabled() && (this.mWindowManager instanceof HwWindowManagerService)) {
            this.mWindowManager.updateFingerprintSlideSwitch();
        }
    }

    public void freezeOrThawRotationInPcMode() {
        if (HwPCUtils.enabledInPad()) {
            synchronized (this) {
                this.mWindowManager.freezeRotation(1);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0163, code lost:
        android.os.Binder.restoreCallingIdentity(r12);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0167, code lost:
        return;
     */
    public void relaunchIMEIfNecessary() {
        int appId;
        if (checkCallingPermission("android.permission.KILL_BACKGROUND_PROCESSES") == 0 || checkCallingPermission("android.permission.RESTART_PACKAGES") == 0) {
            long callingId = Binder.clearCallingIdentity();
            try {
                String packageName = Settings.Secure.getString(this.mContext.getContentResolver(), "default_input_method");
                List<InputMethodInfo> methodList = null;
                if (HwPCUtils.enabledInPad()) {
                    InputMethodManager imm = (InputMethodManager) this.mContext.getSystemService("input_method");
                    if (imm != null) {
                        methodList = imm.getInputMethodList();
                    }
                }
                List<InputMethodInfo> methodList2 = methodList;
                if (packageName != null) {
                    int index = packageName.indexOf(47);
                    if (index == -1) {
                        Binder.restoreCallingIdentity(callingId);
                        return;
                    }
                    String packageName2 = packageName.substring(0, index);
                    int userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), UserHandle.myUserId(), true, 2, "killBackgroundProcesses", null);
                    IPackageManager pm = AppGlobals.getPackageManager();
                    synchronized (this) {
                        try {
                            appId = UserHandle.getAppId(pm.getPackageUid(packageName2, 268435456, userId));
                        } catch (RemoteException e) {
                            Slog.e(TAG, "Unable to get Package Uid, userId = " + userId);
                            appId = -1;
                        } catch (Throwable th) {
                            th = th;
                            IPackageManager iPackageManager = pm;
                            int i = userId;
                            String str = packageName2;
                            throw th;
                        }
                        if (appId == -1) {
                            try {
                                Slog.w(TAG, "Invalid packageName: " + packageName2);
                                Binder.restoreCallingIdentity(callingId);
                            } catch (Throwable th2) {
                                th = th2;
                                IPackageManager iPackageManager2 = pm;
                                int i2 = userId;
                                String str2 = packageName2;
                                throw th;
                            }
                        } else if (!HwPCUtils.enabledInPad() || methodList2 == null) {
                            killPackageProcessesLocked(packageName2, appId, userId, 100, false, false, true, false, "relaunchIME");
                        } else {
                            Iterator<InputMethodInfo> it = methodList2.iterator();
                            while (it.hasNext()) {
                                InputMethodInfo mi = it.next();
                                InputMethodInfo inputMethodInfo = mi;
                                Iterator<InputMethodInfo> it2 = it;
                                IPackageManager pm2 = pm;
                                int userId2 = userId;
                                String packageName3 = packageName2;
                                try {
                                    killPackageProcessesLocked(mi.getPackageName(), appId, userId, 100, false, false, true, false, "relaunchIME");
                                    it = it2;
                                    pm = pm2;
                                    userId = userId2;
                                    packageName2 = packageName3;
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            }
                            int i3 = userId;
                            String str3 = packageName2;
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            String msg = "Permission Denial: killBackgroundProcesses() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + "android.permission.KILL_BACKGROUND_PROCESSES";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0098, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x009b, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00a2, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    public void hwRestoreTask(int taskId, float xPos, float yPos) {
        if (HwPCUtils.isPcCastModeInServer()) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this);
            if (multiWindowMgr != null) {
                long origId = Binder.clearCallingIdentity();
                try {
                    synchronized (this) {
                        TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                        if (tr == null) {
                            Binder.restoreCallingIdentity(origId);
                        } else if (!HwPCMultiWindowCompatibility.isRestorable(tr.mWindowState)) {
                            Binder.restoreCallingIdentity(origId);
                        } else {
                            if (tr.getStack() instanceof HwActivityStack) {
                                tr.getStack().resetOtherStacksVisible(true);
                            }
                            Rect rect = multiWindowMgr.getWindowBounds(tr);
                            if (rect == null) {
                                Binder.restoreCallingIdentity(origId);
                                return;
                            }
                            if (!(xPos == -1.0f || yPos == -1.0f)) {
                                Rect bounds = tr.getOverrideBounds();
                                if (bounds == null) {
                                    bounds = multiWindowMgr.getMaximizedBounds();
                                }
                                if (bounds.width() != 0) {
                                    if (bounds.height() != 0) {
                                        rect.offsetTo((int) (xPos - (((float) rect.width()) * ((xPos - ((float) bounds.left)) / ((float) bounds.width())))), (int) (yPos - (((float) rect.height()) * ((yPos - ((float) bounds.top)) / ((float) bounds.height())))));
                                    }
                                }
                            }
                            tr.resize(rect, 3, true, false);
                        }
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00d6, code lost:
        android.os.Binder.restoreCallingIdentity(r1);
     */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0036 A[Catch:{ all -> 0x001d, all -> 0x00dc }, DONT_GENERATE] */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x003b A[SYNTHETIC, Splitter:B:25:0x003b] */
    public void hwResizeTask(int taskId, Rect bounds) {
        TaskRecord task;
        String str;
        if (HwPCUtils.isPcCastModeInServer()) {
            HwPCMultiWindowManager multiWindowMgr = HwPCMultiWindowManager.getInstance(this);
            if (multiWindowMgr != null) {
                long origId = Binder.clearCallingIdentity();
                try {
                    synchronized (this) {
                        boolean isFullscreen = false;
                        boolean isMaximized = false;
                        if (bounds != null) {
                            if (bounds.top < 0) {
                                bounds = null;
                                isFullscreen = true;
                                task = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                                if (task != null) {
                                    Binder.restoreCallingIdentity(origId);
                                    return;
                                } else if (!multiWindowMgr.isSupportResize(task, isFullscreen, isMaximized)) {
                                    HwPCUtils.log("HwPCMultiWindowManager", "hwResizeTask-fail: (" + Integer.toHexString(task.mWindowState) + ")isFullscreen:" + isFullscreen + "; isMax:" + isMaximized);
                                    Binder.restoreCallingIdentity(origId);
                                    return;
                                } else {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("hwResizeTask: ");
                                    if (bounds == null) {
                                        str = "null";
                                    } else {
                                        str = bounds.toShortString() + " (" + bounds.width() + ", " + bounds.height() + ")";
                                    }
                                    sb.append(str);
                                    HwPCUtils.log("HwPCMultiWindowManager", sb.toString());
                                    task.resize(bounds, 3, true, false);
                                    if ((task.getStack() instanceof HwActivityStack) && (isFullscreen || isMaximized)) {
                                        task.getStack().resetOtherStacksVisible(false);
                                    }
                                }
                            }
                        }
                        if (bounds != null && bounds.isEmpty()) {
                            bounds = multiWindowMgr.getMaximizedBounds();
                            isMaximized = true;
                        }
                        task = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                        if (task != null) {
                        }
                    }
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            }
        }
    }

    public int getWindowState(IBinder token) {
        ActivityRecord r;
        if (!HwPCUtils.isPcCastModeInServer()) {
            return -1;
        }
        long ident = Binder.clearCallingIdentity();
        try {
            synchronized (this) {
                r = ActivityRecord.isInStackLocked(token);
            }
            if (r != null) {
                if (r.task != null) {
                    int windowState = r.task.getWindowState();
                    Binder.restoreCallingIdentity(ident);
                    return windowState;
                }
            }
            Binder.restoreCallingIdentity(ident);
            return -1;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(ident);
            throw th;
        }
    }

    public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) {
        synchronized (this) {
            long origId = Binder.clearCallingIdentity();
            try {
                TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(taskId);
                if (tr != null) {
                    HwRecentTaskInfo createHwRecentTaskInfoFromTaskRecord = createHwRecentTaskInfoFromTaskRecord(tr);
                    return createHwRecentTaskInfoFromTaskRecord;
                }
                Binder.restoreCallingIdentity(origId);
                return null;
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    /* access modifiers changed from: protected */
    public HwRecentTaskInfo createHwRecentTaskInfoFromTaskRecord(TaskRecord tr) {
        ActivityManager.RecentTaskInfo rti = createRecentTasks().createRecentTaskInfo(tr);
        HwRecentTaskInfo hwRti = new HwRecentTaskInfo();
        hwRti.translateRecentTaskinfo(rti);
        ActivityStack stack = tr.getStack();
        if (stack != null) {
            hwRti.displayId = stack.mDisplayId;
        }
        hwRti.windowState = tr.getWindowState();
        if (!tr.mActivities.isEmpty() && (this.mWindowManager instanceof HwWindowManagerService)) {
            hwRti.systemUiVisibility = this.mWindowManager.getWindowSystemUiVisibility(((ActivityRecord) tr.mActivities.get(0)).appToken);
        }
        return hwRti;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001c, code lost:
        com.android.server.am.HwActivityManagerService.super.overridePendingTransition(r3, r4, r5, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        return;
     */
    public void overridePendingTransition(IBinder token, String packageName, int enterAnim, int exitAnim) {
        synchronized (this) {
            ActivityRecord self = ActivityRecord.isInStackLocked(token);
            if (self != null) {
                if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(self.getDisplayId())) {
                }
            }
        }
    }

    public void moveTaskBackwards(int task) {
        boolean handled = false;
        if (HwPCUtils.isPcCastModeInServer()) {
            synchronized (this) {
                long origId = Binder.clearCallingIdentity();
                TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(task);
                if (tr != null && HwPCUtils.isExtDynamicStack(tr.getStackId())) {
                    handled = true;
                    tr.getStack().moveTaskToBackLocked(task);
                }
                Binder.restoreCallingIdentity(origId);
            }
        }
        if (!handled) {
            HwActivityManagerService.super.moveTaskBackwards(task);
        }
    }

    public void registerHwTaskStackListener(ITaskStackListener listener) {
        TaskChangeNotificationController taskctl = getHwTaskChangeController();
        if (taskctl != null) {
            taskctl.registerTaskStackListener(listener);
        }
    }

    public void unRegisterHwTaskStackListener(ITaskStackListener listener) {
        if (getHwTaskChangeController() != null) {
            getHwTaskChangeController().unregisterTaskStackListener(listener);
        }
    }

    public Bitmap getDisplayBitmap(int displayId, int width, int height) {
        if (this.mWindowManager instanceof HwWindowManagerService) {
            return this.mWindowManager.getDisplayBitmap(displayId, width, height);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isTaskNotResizeableEx(TaskRecord task, Rect bounds) {
        return (isTaskSizeChange(task, bounds) && !HwPCMultiWindowCompatibility.isResizable(task.getWindowState())) || (!isTaskSizeChange(task, bounds) && !HwPCMultiWindowCompatibility.isLayoutHadBounds(task.getWindowState()));
    }

    private boolean isTaskSizeChange(TaskRecord task, Rect rect) {
        return (task.getOverrideBounds().width() == rect.width() && task.getOverrideBounds().height() == rect.height()) ? false : true;
    }

    public void toggleHome() {
        if (HwPCUtils.isPcCastModeInServer()) {
            synchronized (this) {
                long origId = Binder.clearCallingIdentity();
                try {
                    int displayId = HwPCUtils.getPCDisplayID();
                    if (HwPCUtils.isValidExtDisplayId(displayId)) {
                        ActivityDisplay activityDisplay = (ActivityDisplay) this.mStackSupervisor.mActivityDisplays.get(displayId);
                        if (activityDisplay == null) {
                            Binder.restoreCallingIdentity(origId);
                            return;
                        }
                        ArrayList<ActivityStack> stacks = new ArrayList<>();
                        int size = activityDisplay.getChildCount();
                        for (int i = 0; i < size; i++) {
                            stacks.add(activityDisplay.getChildAt(i));
                        }
                        boolean moveAllToBack = true;
                        int stackNdx = stacks.size() - 1;
                        while (true) {
                            if (stackNdx < 0) {
                                break;
                            }
                            HwActivityStack hwActivityStack = stacks.get(stackNdx);
                            if ((hwActivityStack instanceof HwActivityStack) && hwActivityStack.mHiddenFromHome) {
                                moveAllToBack = false;
                                break;
                            }
                            stackNdx--;
                        }
                        for (int stackNdx2 = stacks.size() - 1; stackNdx2 >= 0; stackNdx2--) {
                            HwActivityStack hwActivityStack2 = stacks.get(stackNdx2);
                            TaskRecord task = hwActivityStack2.topTask();
                            if (task != null) {
                                if (moveAllToBack) {
                                    if (hwActivityStack2.shouldBeVisible(null) && (hwActivityStack2 instanceof HwActivityStack) && !hwActivityStack2.mHiddenFromHome) {
                                        hwActivityStack2.moveTaskToBackLocked(task.taskId);
                                        hwActivityStack2.mHiddenFromHome = true;
                                    }
                                } else if (hwActivityStack2.shouldBeVisible(null) || !(hwActivityStack2 instanceof HwActivityStack) || !hwActivityStack2.mHiddenFromHome) {
                                } else {
                                    TaskRecord taskRecord = task;
                                    hwActivityStack2.moveTaskToFrontLocked(task, true, null, null, "moveToFrontFromHomeKey.");
                                    hwActivityStack2.mHiddenFromHome = false;
                                }
                            }
                        }
                        Binder.restoreCallingIdentity(origId);
                    }
                } finally {
                    Binder.restoreCallingIdentity(origId);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isMaximizedPortraitAppOnPCMode(ActivityRecord r) {
        if (!(!HwPCUtils.isPcCastModeInServer() || r.getStack() == null || r.info == null || r.info.getComponentName() == null || !HwPCUtils.isValidExtDisplayId(r.getStack().mDisplayId))) {
            if (HwPCMultiWindowManager.getInstance(this).mPortraitMaximizedPkgList.contains(r.info.getComponentName().getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public void registerExternalPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mWindowManager.registerExternalPointerEventListener(listener);
    }

    public void unregisterExternalPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mWindowManager.unregisterExternalPointerEventListener(listener);
    }

    public void setPCScreenDpMode(int mode) {
        if (this.mWindowManager instanceof HwWindowManagerService) {
            this.mWindowManager.setPCScreenDisplayMode(mode);
        }
    }

    public int getPCScreenDisplayMode() {
        if (this.mWindowManager instanceof HwWindowManagerService) {
            return this.mWindowManager.getPCScreenDisplayMode();
        }
        return 0;
    }

    public boolean isPackageRunningOnPCMode(String packageName, int uid) {
        Slog.d(TAG, "isPackageRunningOnPCMode, packageName:" + packageName + ", uid:" + uid);
        if (packageName == null) {
            Slog.e(TAG, "isPackageRunningOnPCMode packageName == null");
            return false;
        }
        ProcessRecord pr = (ProcessRecord) this.mProcessNames.get(packageName, uid);
        if (pr != null) {
            Slog.d(TAG, "pr.mDisplayId:" + pr.mDisplayId);
            return HwPCUtils.isValidExtDisplayId(pr.mDisplayId);
        }
        Slog.d(TAG, "no pr, packageName:" + packageName);
        return false;
    }

    public Bitmap getTaskThumbnailOnPCMode(int taskId) {
        Bitmap bitmap = null;
        synchronized (this) {
            TaskRecord tr = this.mStackSupervisor.anyTaskForIdLocked(taskId, 1);
            if (!(tr == null || tr.mStack == null || !(this.mWindowManager instanceof HwWindowManagerService))) {
                ActivityRecord r = tr.topRunningActivityLocked();
                if (r != null) {
                    bitmap = this.mWindowManager.getTaskSnapshotForPc(r.getDisplayId(), r.appToken, tr.taskId, tr.userId);
                }
            }
        }
        return bitmap;
    }

    public boolean checkTaskId(int taskId) {
        ProcessRecord processRecord = (ProcessRecord) this.mPidsSelfLocked.get(Binder.getCallingPid());
        if (processRecord != null) {
            ArrayList<ActivityRecord> activityRecords = processRecord.activities;
            ArrayList taskIdContainer = new ArrayList();
            int recordsSize = activityRecords.size();
            for (int i = 0; i < recordsSize; i++) {
                TaskRecord task = activityRecords.get(i).getTask();
                if (task != null) {
                    taskIdContainer.add(Integer.valueOf(task.taskId));
                }
            }
            if (taskIdContainer.contains(Integer.valueOf(taskId))) {
                return true;
            }
        }
        return false;
    }

    public boolean addGameSpacePackageList(List<String> packageList) {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.addGameSpacePackageList(packageList);
        }
        return false;
    }

    public boolean delGameSpacePackageList(List<String> packageList) {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.delGameSpacePackageList(packageList);
        }
        return false;
    }

    public boolean isInGameSpace(String packageName) {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.isInGameSpace(packageName);
        }
        return false;
    }

    public List<String> getGameList() {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.getGameList();
        }
        return null;
    }

    public void registerGameObserver(IGameObserver observer) {
        if (this.mHwGameAssistantController != null) {
            this.mHwGameAssistantController.registerGameObserver(observer);
        }
    }

    public void unregisterGameObserver(IGameObserver observer) {
        if (this.mHwGameAssistantController != null) {
            this.mHwGameAssistantController.unregisterGameObserver(observer);
        }
    }

    public boolean isGameDndOn() {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.isGameDndOn();
        }
        return false;
    }

    public boolean isGameKeyControlOn() {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.isGameKeyControlOn();
        }
        return false;
    }

    public boolean isGameGestureDisabled() {
        if (this.mHwGameAssistantController != null) {
            return this.mHwGameAssistantController.isGameGestureDisabled();
        }
        return false;
    }
}
