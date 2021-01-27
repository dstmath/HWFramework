package com.android.server.am;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.HwRecentTaskInfo;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.database.IContentObserver;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.HwSysResource;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.CollectData;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Slog;
import android.util.TimingsTraceLog;
import android.view.WindowManagerPolicyConstants;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.server.SMCSAMSHelper;
import com.android.server.ServiceThread;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.pm.PackageManagerServiceEx;
import com.android.server.pm.auth.HwCertification;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.server.wm.ActivityRecord;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.ActivityTaskManagerServiceEx;
import com.android.server.wm.HwActivityTaskManagerServiceEx;
import com.android.server.wm.HwWindowManagerService;
import com.android.server.wm.WindowProcessController;
import com.huawei.android.app.TaskStackListenerEx;
import com.huawei.aod.AodThemeConst;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.wm.WindowProcessControllerEx;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsWCGModeID;

public final class HwActivityManagerService extends ActivityManagerService {
    public static final int BACKUP_APP_ADJ = 300;
    public static final int CACHED_APP_MAX_ADJ = 999;
    public static final int CACHED_APP_MIN_ADJ = 900;
    private static final String DESCRIPTOR = "android.app.IActivityManager";
    public static final int FOREGROUND_APP_ADJ = 0;
    public static final int HEAVY_WEIGHT_APP_ADJ = 400;
    public static final int HOME_APP_ADJ = 600;
    private static final boolean IS_DEBUG_HWTRIM = SystemProperties.getBoolean("ro.enable.st_debug", false);
    private static final boolean IS_ENABLE_RMS = SystemProperties.getBoolean("ro.config.enable_rms", false);
    private static final boolean IS_SMCSHWSYSM_ENABLED = SystemProperties.getBoolean("ro.enable.hwsysm_smcs", true);
    public static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", AppActConstant.VALUE_DEFAULT));
    public static final int NATIVE_ADJ = -1000;
    public static final int PERCEPTIBLE_APP_ADJ = 200;
    public static final int PERSISTENT_PROC_ADJ = -800;
    public static final int PERSISTENT_SERVICE_ADJ = -700;
    public static final int PREVIOUS_APP_ADJ = 700;
    private static final int QUEUE_NUM_DEFAULT = 3;
    private static final int QUEUE_NUM_IAWARE = 7;
    private static final int QUEUE_NUM_RMS = 5;
    public static final int SERVICE_ADJ = 500;
    public static final int SERVICE_B_ADJ = 800;
    public static final int SYSTEM_ADJ = -900;
    private static final String SYSTEM_DEBUGGABLE = "ro.debuggable";
    static final String TAG = "HwActivityManagerService";
    public static final int VISIBLE_APP_ADJ = 100;
    private static boolean isEnableIaware = SystemProperties.getBoolean("persist.sys.enable_iaware", false);
    private static HwActivityManagerService mSelf;
    private static Set<String> sAllowedCrossUserForCloneArrays = new HashSet();
    private HwSysResource mAppResource;
    private HwSysResource mAppServiceResource;
    private HwSysResource mBroadcastResource;
    private Map<Integer, Intent> mCurrentSplitIntent = new HashMap();
    Handler mHandler = new Handler();
    private Intent mLastSplitIntent;
    private HwSysResource mOrderedBroadcastResource;
    private boolean[] mScreenStatusRequests = {false, false};
    private Map<Integer, Stack<IBinder>> mSplitActivityEntryStack;
    private Bundle mSplitExtras;

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
        sAllowedCrossUserForCloneArrays.add("com.huawei.android.launcher");
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
    }

    public HwActivityManagerService(Context context, ActivityTaskManagerService atm) {
        super(context, atm);
        mSelf = this;
    }

    public static HwActivityManagerService self() {
        return mSelf;
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (code == 502) {
            data.enforceInterface(DESCRIPTOR);
            boolean handleANRFilterFIFO = handleANRFilterFIFO(data.readInt(), data.readInt());
            reply.writeNoException();
            reply.writeInt(handleANRFilterFIFO ? 1 : 0);
            return true;
        } else if (code == 504) {
            data.enforceInterface(DESCRIPTOR);
            String packageName = getPackageNameForPid(data.readInt());
            reply.writeNoException();
            reply.writeString(packageName);
            return true;
        } else if (code == 1599294787) {
            return handHwMemClean(code, data, reply, flags);
        } else {
            boolean isForLast = false;
            switch (code) {
                case WifiProCommonUtils.RESP_CODE_UNSTABLE /* 601 */:
                    data.enforceInterface(DESCRIPTOR);
                    Intent intent = (Intent) data.readParcelable(null);
                    int pid = data.readInt();
                    Bundle bundle = data.readBundle();
                    if (data.readInt() > 0) {
                        isForLast = true;
                    }
                    setIntentInfo(intent, pid, bundle, isForLast);
                    reply.writeNoException();
                    return true;
                case WifiProCommonUtils.RESP_CODE_GATEWAY /* 602 */:
                    data.enforceInterface(DESCRIPTOR);
                    Parcelable[] parcelables = getIntentInfo(data.readInt(), data.readInt() > 0);
                    reply.writeNoException();
                    reply.writeParcelableArray(parcelables, 0);
                    return true;
                case WifiProCommonUtils.RESP_CODE_INVALID_URL /* 603 */:
                    data.enforceInterface(DESCRIPTOR);
                    addToEntryStack(data.readInt(), data.readStrongBinder(), data.readInt(), (Intent) Intent.CREATOR.createFromParcel(data));
                    reply.writeNoException();
                    return true;
                case WifiProCommonUtils.RESP_CODE_ABNORMAL_SERVER /* 604 */:
                    data.enforceInterface(DESCRIPTOR);
                    clearEntryStack(data.readInt(), data.readStrongBinder());
                    return true;
                case WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED /* 605 */:
                    data.enforceInterface(DESCRIPTOR);
                    removeFromEntryStack(data.readInt(), data.readStrongBinder());
                    return true;
                case WifiProCommonUtils.RESP_CODE_CONN_RESET /* 606 */:
                    data.enforceInterface(DESCRIPTOR);
                    boolean isTopSplitActivity = isTopSplitActivity(data.readInt(), data.readStrongBinder());
                    reply.writeNoException();
                    reply.writeInt(isTopSplitActivity ? 1 : 0);
                    return true;
                default:
                    return HwActivityManagerService.super.onTransact(code, data, reply, flags);
            }
        }
    }

    private boolean handHwMemClean(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        if (IS_DEBUG_HWTRIM) {
            Log.v(TAG, "AMS.onTransact: got HWMEMCLEAN_TRANSACTION");
        }
        if (IS_SMCSHWSYSM_ENABLED) {
            if (this.mContext == null) {
                return false;
            }
            if (this.mContext.checkCallingPermission("huawei.permission.HSM_SMCS") != 0) {
                if (IS_DEBUG_HWTRIM) {
                    Log.e(TAG, "SMCSAMSHelper.handleTransact permission deny");
                }
                return false;
            } else if (SMCSAMSHelper.getInstance().handleTransact(data, reply, flags)) {
                return true;
            }
        } else if (IS_DEBUG_HWTRIM) {
            Log.v(TAG, "AMS.onTransact: HWSysM SMCS is disabled.");
        }
        return HwActivityManagerService.super.onTransact(code, data, reply, flags);
    }

    public Configuration getCurNaviConfiguration() {
        return this.mWindowManager.getCurNaviConfiguration();
    }

    public boolean serviceIsRunning(ComponentName serviceCmpName, int curUser) {
        boolean z;
        synchronized (this) {
            Slog.d(TAG, "serviceIsRunning, for user " + curUser + ", serviceCmpName " + serviceCmpName);
            z = this.mServices.getServicesLocked(curUser).get(serviceCmpName) != null;
        }
        return z;
    }

    public void systemReady(Runnable goingCallback, TimingsTraceLog traceLog) {
        HwActivityManagerService.super.systemReady(goingCallback, traceLog);
        this.mContext.registerReceiver(new ScreenStatusReceiver(), new IntentFilter("android.intent.action.stk.check_screen_idle"), "com.huawei.permission.STK_CHECK_SCREEN_IDLE", null);
    }

    public ArrayList<Integer> getIawareDumpData() {
        ArrayList<Integer> queueSizes;
        synchronized (this) {
            queueSizes = new ArrayList<>();
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                List<Integer> queueSizesTemps = queue.getIawareDumpData();
                if (queueSizesTemps != null) {
                    queueSizes.addAll(queueSizesTemps);
                }
            }
        }
        return queueSizes;
    }

    public void updateSRMSStatisticsData(int subTypeCode) {
        HwPartIawareUtil.updateStatisticsData(subTypeCode);
    }

    public boolean getIawareResourceFeature(int type) {
        if (type >= 1 && type <= 2) {
            return HwPartIawareUtil.getIawareResourceFeature(type);
        }
        if (type < 10 || type > 11) {
            return false;
        }
        return HwPartIawareUtil.isFeatureEnabled(type);
    }

    public long proxyBroadcast(List<String> pkgs, boolean isProxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                delay = Math.max(queue.proxyBroadcast(pkgs, isProxy), delay);
            }
        }
        return delay;
    }

    public long proxyBroadcastByPid(List<Integer> pids, boolean isProxy) {
        long delay;
        synchronized (this) {
            delay = 0;
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                delay = Math.max(queue.proxyBroadcastByPid(pids, isProxy), delay);
            }
        }
        return delay;
    }

    public void setProxyBroadcastActions(List<String> actions) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.setProxyBroadcastActions(actions);
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

    public void proxyBroadcastConfig(int type, String key, List<String> value) {
        synchronized (this) {
            for (BroadcastQueue queue : this.mBroadcastQueues) {
                queue.proxyBroadcastConfig(type, key, value);
            }
        }
    }

    class ScreenStatusReceiver extends BroadcastReceiver {
        ScreenStatusReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.stk.check_screen_idle".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("slot_id", 0);
                if (slotId < 0 || slotId >= HwActivityManagerService.this.mScreenStatusRequests.length) {
                    Slog.w(HwActivityManagerService.TAG, "ScreenStatusReceiver, slotId " + slotId + " Invalid");
                    return;
                }
                HwActivityManagerService.this.mScreenStatusRequests[slotId] = intent.getBooleanExtra("SCREEN_STATUS_REQUEST", false);
                if (!HwActivityManagerService.this.mScreenStatusRequests[slotId] && ActivityManagerDebugConfig.DEBUG_ALL) {
                    Slog.v(HwActivityManagerService.TAG, "Screen Status request is OFF, slot: " + slotId);
                }
            }
        }
    }

    public void checkIfScreenStatusRequestAndSendBroadcast() {
        int slotId = 0;
        while (true) {
            boolean[] zArr = this.mScreenStatusRequests;
            if (slotId < zArr.length) {
                if (zArr[slotId]) {
                    Intent stkIntent = new Intent("com.huawei.intent.action.stk.idle_screen");
                    stkIntent.addFlags(16777216);
                    stkIntent.putExtra("SCREEN_IDLE", true);
                    stkIntent.putExtra("slot_id", slotId);
                    this.mContext.sendBroadcast(stkIntent, "com.huawei.permission.CAT_IDLE_SCREEN");
                }
                slotId++;
            } else {
                return;
            }
        }
    }

    public boolean isProcessExistLocked(String processName, int uid) {
        return getProcessRecordLocked(processName, uid, true) != null;
    }

    public ProcessRecord getProcessRecordLocked(int pid) {
        ProcessRecord proc = null;
        if (pid >= 0) {
            synchronized (this.mPidsSelfLocked) {
                proc = this.mPidsSelfLocked.get(pid);
            }
        }
        return proc;
    }

    public ProcessRecordEx getProcessRecordLockedEx(int pid) {
        return ProcessRecordExUtils.createProcessRecordEx(getProcessRecordLocked(pid));
    }

    public ProcessRecordEx getProcessRecordEx(String processName, int uid, boolean keepIfLarge) {
        return ProcessRecordExUtils.createProcessRecordEx(getProcessRecord(processName, uid, keepIfLarge));
    }

    public boolean shouldPreventRestartService(ServiceInfo serviceInfo, boolean isRealStart) {
        return HwPartIawareUtil.shouldPreventRestartService(serviceInfo, isRealStart);
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, ProcessRecord callerApp) {
        return HwPartIawareUtil.shouldPreventStartProvider(cpi, callerPid, callerUid, new WindowProcessControllerEx(callerApp.getWindowProcessController()));
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
            } finally {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue[] initialBroadcastQueue() {
        int queueNum;
        if (isEnableIaware) {
            queueNum = 7;
        } else if (IS_ENABLE_RMS) {
            queueNum = 5;
        } else {
            queueNum = 3;
        }
        return new BroadcastQueue[queueNum];
    }

    /* access modifiers changed from: protected */
    public void setThirdPartyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (IS_ENABLE_RMS || isEnableIaware) {
            ServiceThread thirdAppHandlerThread = new ServiceThread("ThirdAppHandlerThread", 10, false);
            thirdAppHandlerThread.start();
            Handler thirdAppHandler = new Handler(thirdAppHandlerThread.getLooper());
            BroadcastConstants foreConstants = new BroadcastConstants("bcast_fg_constants");
            foreConstants.TIMEOUT = 10000;
            BroadcastConstants backConstants = new BroadcastConstants("bcast_bg_constants");
            backConstants.TIMEOUT = 60000;
            this.mFgThirdAppBroadcastQueue = new HwBroadcastQueue(this, thirdAppHandler, "fgthirdapp", foreConstants, false);
            this.mBgThirdAppBroadcastQueue = new HwBroadcastQueue(this, thirdAppHandler, "bgthirdapp", backConstants, false);
            broadcastQueues[3] = this.mFgThirdAppBroadcastQueue;
            broadcastQueues[4] = this.mBgThirdAppBroadcastQueue;
        }
    }

    /* access modifiers changed from: protected */
    public void setKeyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
        if (isEnableIaware) {
            ServiceThread keyAppHandlerThread = new ServiceThread("keyAppHanderThread", 0, false);
            keyAppHandlerThread.start();
            Handler keyAppHandler = new Handler(keyAppHandlerThread.getLooper());
            BroadcastConstants foreConstants = new BroadcastConstants("bcast_fg_constants");
            foreConstants.TIMEOUT = 10000;
            BroadcastConstants backConstants = new BroadcastConstants("bcast_bg_constants");
            backConstants.TIMEOUT = 60000;
            this.mFgKeyAppBroadcastQueue = new HwBroadcastQueue(this, keyAppHandler, "fgkeyapp", foreConstants, false);
            this.mBgKeyAppBroadcastQueue = new HwBroadcastQueue(this, keyAppHandler, "bgkeyapp", backConstants, false);
            broadcastQueues[5] = this.mFgKeyAppBroadcastQueue;
            broadcastQueues[6] = this.mBgKeyAppBroadcastQueue;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isThirdPartyAppBroadcastQueue(ProcessRecord callerApp) {
        if (callerApp == null) {
            return false;
        }
        if (!IS_ENABLE_RMS && !getIawareResourceFeature(1)) {
            return false;
        }
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND) {
            Log.i(TAG, "Split enqueueing broadcast [callerApp]:" + callerApp);
        }
        if (callerApp.mInstr != null) {
            return false;
        }
        int flags = callerApp.info.flags & 1;
        int hwFlags = callerApp.info.hwFlags & 33554432;
        if (flags == 0 || hwFlags != 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isKeyAppBroadcastQueue(int type, String name) {
        return getIawareResourceFeature(1) && name != null && isKeyApp(type, 0, name);
    }

    /* access modifiers changed from: protected */
    public boolean isThirdPartyAppPendingBroadcastProcessLocked(int pid) {
        if (IS_ENABLE_RMS || getIawareResourceFeature(1)) {
            return this.mFgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid) || this.mBgThirdAppBroadcastQueue.isPendingBroadcastProcessLocked(pid);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isKeyAppPendingBroadcastProcessLocked(int pid) {
        if (!getIawareResourceFeature(1) || this.mFgKeyAppBroadcastQueue == null || this.mBgKeyAppBroadcastQueue == null) {
            return false;
        }
        return this.mFgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid) || this.mBgKeyAppBroadcastQueue.isPendingBroadcastProcessLocked(pid);
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue thirdPartyAppBroadcastQueueForIntent(Intent intent) {
        boolean isFg = (intent.getFlags() & 268435456) != 0;
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND) {
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
        if (ActivityManagerDebugConfig.DEBUG_BROADCAST_BACKGROUND) {
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
            if (IS_DEBUG_HWTRIM || Log.HWINFO) {
                Log.d(TAG, "init BroadcastResource");
            }
            this.mBroadcastResource = HwFrameworkFactory.getHwResource(11);
        }
    }

    public void checkOrderedBroadcastTimeoutLocked(String actionOrPkg, int timeCost, boolean isInToOut) {
        if (getIawareResourceFeature(2)) {
            if (this.mOrderedBroadcastResource == null) {
                if (IS_DEBUG_HWTRIM || Log.HWINFO) {
                    Log.d(TAG, "init OrderedBroadcastResource");
                }
                this.mOrderedBroadcastResource = HwFrameworkFactory.getHwResource(31);
            }
            HwSysResource hwSysResource = this.mOrderedBroadcastResource;
            if (hwSysResource != null && !isInToOut) {
                hwSysResource.acquire(0, actionOrPkg, 0);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void checkBroadcastRecordSpeed(int callingUid, String callerPackage, ProcessRecord callerApp) {
        if (this.mBroadcastResource != null && callerApp != null) {
            int processType = getProcessType(callerApp);
            if (("1".equals(SystemProperties.get(SYSTEM_DEBUGGABLE, "0")) || processType == 0) && this.mBroadcastResource.acquire(callingUid, callerPackage, processType) == 2 && ActivityManagerDebugConfig.DEBUG_BROADCAST) {
                Log.i(TAG, "This App send broadcast speed is overload! uid = " + callingUid);
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
        if ((app.info.flags & 1) != 0) {
            return 2;
        }
        return 0;
    }

    public boolean isKeyApp(int type, int value, String key) {
        HwSysResource hwSysResource = this.mBroadcastResource;
        if (hwSysResource == null || key == null || hwSysResource.queryPkgPolicy(type, value, key) != 1) {
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
        if (this.mAppServiceResource == null || sr == null || sr.appInfo.packageName == null || sr.serviceInfo.name == null || sr.appInfo.uid <= 0 || this.mAppServiceResource.acquire(sr.appInfo.uid, sr.appInfo.packageName, getProcessType(app)) != 2) {
            return true;
        }
        Log.i(TAG, "Failed to acquire AppServiceResource:" + sr.serviceInfo.name + " of " + sr.appInfo.packageName + AodThemeConst.SPLASH + sr.appInfo.uid);
        return false;
    }

    public boolean isAcquireAppResourceLocked(ProcessRecord app) {
        int processType;
        if (!(this.mAppResource == null || app == null || app.info == null || app.processName == null || app.uid <= 0 || app.startTime <= 0)) {
            if ((app.info.flags & 1) != 0 && (app.info.hwFlags & 33554432) == 0 && (app.info.hwFlags & HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW) == 0) {
                processType = 2;
            } else {
                processType = 0;
            }
            Bundle args = new Bundle();
            args.putInt("callingUid", app.uid);
            args.putString("pkg", app.processName);
            args.putLong("startTime", app.startTime);
            args.putInt("processType", processType);
            args.putBoolean("launchfromActivity", app.launchfromActivity);
            args.putBoolean("topProcess", app.hasForegroundActivities());
            if (this.mAppResource.acquire((Uri) null, (IContentObserver) null, args) == 2) {
                Log.i(TAG, "Failed to acquire AppResource:" + app.info.packageName + AodThemeConst.SPLASH + app.uid);
                return false;
            }
        }
        return true;
    }

    private void clearAppServiceResource(ProcessRecord app) {
        HwSysResource hwSysResource = this.mAppServiceResource;
        if (hwSysResource != null && app != null) {
            hwSysResource.clear(app.uid, app.info.packageName, getProcessType(app));
            Log.i(TAG, "clear AppServiceResource of " + app.info.packageName + AodThemeConst.SPLASH + app.uid);
        }
    }

    private void clearAppResource(ProcessRecord app) {
        if (this.mAppResource != null && app != null && app.uid > 0 && app.info != null && app.info.packageName != null) {
            boolean isSystemFlag = true;
            int processType = 0;
            if (!((app.info.flags & 1) != 0 && (app.info.hwFlags & 33554432) == 0 && (app.info.hwFlags & HwActivityTaskManagerServiceEx.APP_ATTRIBUTE_FORCE_NOT_ROTATE_WINDOW) == 0)) {
                isSystemFlag = false;
            }
            if (isSystemFlag) {
                processType = 2;
            }
            this.mAppResource.clear(app.uid, app.info.packageName, processType);
            Log.i(TAG, "clear Appresource of " + app.info.packageName + AodThemeConst.SPLASH + app.uid);
        }
    }

    public void clearAppAndAppServiceResource(ProcessRecord app) {
        clearAppServiceResource(app);
        clearAppResource(app);
    }

    public Map<Integer, AwareProcessBaseInfo> getAllProcessBaseInfo() {
        Map<Integer, AwareProcessBaseInfo> list;
        synchronized (this.mPidsSelfLocked) {
            int size = this.mPidsSelfLocked.size();
            list = new ArrayMap<>(size);
            for (int i = 0; i < size; i++) {
                ProcessRecord p = this.mPidsSelfLocked.valueAt(i);
                AwareProcessBaseInfo baseInfo = new AwareProcessBaseInfo();
                baseInfo.curAdj = p.curAdj;
                baseInfo.foregroundActivities = p.mHasForegroundActivities;
                baseInfo.adjType = p.adjType;
                baseInfo.hasShownUi = p.hasShownUi;
                baseInfo.uid = p.uid;
                baseInfo.appUid = p.info.uid;
                baseInfo.targetSdkVersion = p.info.targetSdkVersion;
                baseInfo.setProcState = p.setProcState;
                list.put(Integer.valueOf(p.pid), baseInfo);
            }
        }
        return list;
    }

    public AwareProcessBaseInfo getProcessBaseInfo(int pid) {
        AwareProcessBaseInfo baseInfo;
        synchronized (this.mPidsSelfLocked) {
            baseInfo = new AwareProcessBaseInfo();
            baseInfo.curAdj = 1001;
            ProcessRecord p = this.mPidsSelfLocked.get(pid);
            if (p != null) {
                baseInfo.foregroundActivities = p.mHasForegroundActivities;
                baseInfo.uid = p.uid;
                baseInfo.appUid = p.info.uid;
                baseInfo.targetSdkVersion = p.info.targetSdkVersion;
                baseInfo.setProcState = p.setProcState;
                baseInfo.curAdj = p.curAdj;
                baseInfo.adjType = p.adjType;
            }
        }
        return baseInfo;
    }

    public void reportAssocEnable(ArrayMap<Integer, Integer> forePids) {
        HwSysResManager resManager = HwSysResManager.getInstance();
        if (resManager != null && forePids != null && resManager.isResourceNeeded(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC))) {
            synchronized (this) {
                Iterator it = this.mProcessList.mLruProcesses.iterator();
                while (it.hasNext()) {
                    ProcessRecord proc = (ProcessRecord) it.next();
                    if (proc != null) {
                        if (proc.mHasForegroundActivities) {
                            forePids.put(Integer.valueOf(proc.pid), Integer.valueOf(proc.uid));
                        }
                        ArrayList<String> pkgs = getProcessPkgList(proc);
                        Bundle args = new Bundle();
                        args.putInt("callPid", proc.pid);
                        args.putInt("callUid", proc.uid);
                        args.putString("callProcName", proc.processName);
                        args.putInt("userid", proc.userId);
                        args.putStringArrayList("pkgname", pkgs);
                        args.putInt("relationType", 4);
                        HwSysResManager.getInstance().reportData(new CollectData(AwareConstant.ResourceType.getReousrceId(AwareConstant.ResourceType.RESOURCE_APPASSOC), System.currentTimeMillis(), args));
                        reportServiceRelationIAware(proc);
                    }
                }
                this.mActivityTaskManager.mHwATMSEx.reportHomeProcess(this.mActivityTaskManager.mHomeProcess);
            }
        }
    }

    private void reportServiceRelationIAware(ProcessRecord proc) {
        Iterator it = proc.connections.iterator();
        while (it.hasNext()) {
            ConnectionRecord cr = (ConnectionRecord) it.next();
            if (!(cr == null || cr.binding == null)) {
                this.mHwAMSEx.reportServiceRelationIAware(1, cr.binding.service, proc, (Intent) null, cr.binding);
            }
        }
        Iterator it2 = proc.conProviders.iterator();
        while (it2.hasNext()) {
            ContentProviderConnection cpc = (ContentProviderConnection) it2.next();
            if (cpc != null) {
                this.mHwAMSEx.reportServiceRelationIAware(2, cpc.provider, proc, false);
            }
        }
    }

    private ArrayList<String> getProcessPkgList(ProcessRecord proc) {
        ArrayList<String> pkgs = new ArrayList<>();
        int size = proc.pkgList.size();
        for (int i = 0; i < size; i++) {
            String pkg = proc.pkgList.keyAt(i);
            if (!pkgs.contains(pkg)) {
                pkgs.add(pkg);
            }
        }
        return pkgs;
    }

    public void setPackageStoppedState(List<String> packageList, boolean isStopped, int targetUid) {
        if (packageList != null) {
            int userId = UserHandle.getUserId(targetUid);
            IPackageManager pm = AppGlobals.getPackageManager();
            try {
                synchronized (this) {
                    for (String packageName : packageList) {
                        pm.setPackageStoppedState(packageName, isStopped, userId);
                    }
                }
            } catch (RemoteException e) {
                Slog.w(TAG, "Failed trying to unstop package RemoteException");
            } catch (IllegalArgumentException e2) {
                Slog.w(TAG, "Failed trying to unstop package " + packageList.toString() + ": " + e2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean cleanUpApplicationRecordLocked(ProcessRecord app, boolean isRestarting, boolean isAllowRestart, int index, boolean isReplacingPid) {
        if (IS_TABLET) {
            Map<Integer, Stack<IBinder>> map = this.mSplitActivityEntryStack;
            if (map != null && map.containsKey(Integer.valueOf(app.pid))) {
                Slog.w(TAG, "Split main entrance killed, clear sub activities for " + app.info.packageName + ", pid " + app.pid);
                clearEntryStack(app.pid, null);
                this.mSplitActivityEntryStack.remove(Integer.valueOf(app.pid));
            }
            Map<Integer, Intent> map2 = this.mCurrentSplitIntent;
            if (map2 != null && map2.containsKey(Integer.valueOf(app.pid))) {
                this.mCurrentSplitIntent.remove(Integer.valueOf(app.pid));
            }
        }
        return HwActivityManagerService.super.cleanUpApplicationRecordLocked(app, isRestarting, isAllowRestart, index, isReplacingPid);
    }

    public void cleanActivityByUid(List<String> packageList, int targetUid) {
        synchronized (this) {
            int userId = UserHandle.getUserId(targetUid);
            for (String packageName : packageList) {
                if (canCleanTaskRecord(packageName)) {
                    finishDisabledPackageActivitiesLocked(packageName, true, false, userId);
                }
            }
        }
    }

    public int numOfPidWithActivity(int uid) {
        int count = 0;
        synchronized (this.mPidsSelfLocked) {
            int listSize = this.mPidsSelfLocked.size();
            for (int i = 0; i < listSize; i++) {
                ProcessRecord p = this.mPidsSelfLocked.valueAt(i);
                if (p.uid == uid && p.hasShownUi) {
                    count++;
                }
            }
        }
        return count;
    }

    private String getPackageNameForPid(int pid) {
        synchronized (this.mPidsSelfLocked) {
            ProcessRecord proc = this.mPidsSelfLocked.get(pid);
            if (proc != null) {
                return proc.info != null ? proc.info.packageName : PackageManagerServiceEx.PLATFORM_PACKAGE_NAME;
            }
            Flog.i(100, "ProcessRecord for pid " + pid + " does not exist");
            return null;
        }
    }

    public boolean isLauncher(String packageName) {
        if (Process.myUid() != 1000 || packageName == null || packageName.trim().isEmpty()) {
            return false;
        }
        if ("com.huawei.android.launcher".equals(packageName)) {
            return true;
        }
        if (this.mContext != null) {
            return isHomeActivitie(packageName);
        }
        return false;
    }

    private boolean isHomeActivitie(String packageName) {
        List<ResolveInfo> outActivities = new ArrayList<>();
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return false;
        }
        ComponentName componentName = pm.getHomeActivities(outActivities);
        if (!(componentName == null || componentName.getPackageName() == null)) {
            return packageName.equals(componentName.getPackageName());
        }
        for (ResolveInfo info : outActivities) {
            String homePkg = info.activityInfo.packageName;
            if (packageName.equals(homePkg)) {
                Slog.d(TAG, "homePkg is " + homePkg + " ,isLauncher");
                return true;
            }
        }
        return false;
    }

    private void setIntentInfo(Intent intent, int pid, Bundle bundle, boolean isForLast) {
        if (isForLast) {
            this.mLastSplitIntent = intent;
            this.mSplitExtras = bundle;
            return;
        }
        if (!this.mCurrentSplitIntent.containsKey(Integer.valueOf(pid))) {
            Log.e(TAG, "CRITICAL_LOG add intent info.");
        }
        this.mCurrentSplitIntent.put(Integer.valueOf(pid), intent);
    }

    private Parcelable[] getIntentInfo(int pid, boolean isForLast) {
        return isForLast ? new Parcelable[]{this.mLastSplitIntent, this.mSplitExtras} : new Parcelable[]{this.mCurrentSplitIntent.get(Integer.valueOf(pid)), null};
    }

    public void addToEntryStack(int pid, IBinder token, int resultCode, Intent resultData) {
        if (this.mSplitActivityEntryStack == null) {
            this.mSplitActivityEntryStack = new HashMap();
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
        Stack<IBinder> pkgStack;
        Map<Integer, Stack<IBinder>> map = this.mSplitActivityEntryStack;
        if (map != null && !map.isEmpty() && (pkgStack = this.mSplitActivityEntryStack.get(Integer.valueOf(pid))) != null && !pkgStack.empty()) {
            if (selfToken == null || selfToken.equals(pkgStack.peek())) {
                long ident = Binder.clearCallingIdentity();
                while (!pkgStack.empty()) {
                    IBinder token = pkgStack.pop();
                    if (token != null && !token.equals(selfToken)) {
                        Flog.i(100, "Clearing entry " + token);
                        finishActivity(token, 0, null, 0);
                        if (selfToken == null) {
                            String packageName = (String) Optional.ofNullable(ActivityRecord.forToken(token)).map($$Lambda$HwActivityManagerService$uBaDHvl0cuGOkV3gu_gSRJcBDAo.INSTANCE).map($$Lambda$HwActivityManagerService$l6iD6YqBhKovhe9BjjDtPRxFZLo.INSTANCE).map($$Lambda$HwActivityManagerService$bVqRtPHJyoeaCNQUI6C__Pr4fJs.INSTANCE).orElse(null);
                            if (!TextUtils.isEmpty(packageName)) {
                                this.mActivityTaskManager.overridePendingTransition(token, packageName, 0, 0);
                            }
                        }
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
        Stack<IBinder> pkgStack;
        Map<Integer, Stack<IBinder>> map = this.mSplitActivityEntryStack;
        if (map == null || map.isEmpty() || token == null || (pkgStack = this.mSplitActivityEntryStack.get(Integer.valueOf(pid))) == null || pkgStack.empty()) {
            return false;
        }
        return token.equals(pkgStack.peek());
    }

    public void removeFromEntryStack(int pid, IBinder token) {
        Map<Integer, Stack<IBinder>> map;
        Stack<IBinder> pkgStack;
        if (token != null && (map = this.mSplitActivityEntryStack) != null && (pkgStack = map.get(Integer.valueOf(pid))) != null && pkgStack.empty()) {
            pkgStack.remove(token);
        }
    }

    public boolean isPkgHasAlarm(List<String> packageList, int targetUid) {
        if (packageList == null) {
            return false;
        }
        synchronized (this.mPendingIntentController.mLock) {
            for (String packageName : packageList) {
                if (isAlarmPkg(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean isAlarmPkg(String packageName) {
        PendingIntentRecord pir;
        if (this.mPendingIntentController.mIntentSenderRecords.size() <= 0) {
            return false;
        }
        for (WeakReference<PendingIntentRecord> wpir : this.mPendingIntentController.mIntentSenderRecords.values()) {
            if (!(wpir == null || (pir = wpir.get()) == null || pir.key == null || pir.key.packageName == null || !pir.key.packageName.equals(packageName))) {
                return true;
            }
        }
        return false;
    }

    public int iawareGetUidState(int uid) {
        UidRecord uidRec = this.mProcessList.mActiveUids.get(uid);
        if (uidRec == null) {
            return 21;
        }
        return uidRec.mCurProcState;
    }

    public boolean iawareGetPreloadState(int uid) {
        UidRecord uidRec = this.mProcessList.mActiveUids.get(uid);
        if (uidRec == null) {
            return false;
        }
        return uidRec.isPreload;
    }

    /* access modifiers changed from: protected */
    public int handleUserForCloneOrAfw(String name, int userId) {
        int i;
        if (userId == 0 || name == null) {
            return userId;
        }
        int newUserId = userId;
        if (userId != this.mUserController.getCurrentUserIdLU()) {
            long ident = Binder.clearCallingIdentity();
            try {
                UserInfo ui = this.mUserController.getUserInfo(userId);
                if (ui != null) {
                    boolean isManagedProfileSpecial = true;
                    boolean isCloneProfileAllowed = ui.isClonedProfile() && sAllowedCrossUserForCloneArrays.contains(name);
                    if (!ui.isManagedProfile() || !"com.huawei.android.launcher.settings".equals(name)) {
                        isManagedProfileSpecial = false;
                    }
                    if (!isCloneProfileAllowed) {
                        if (!isManagedProfileSpecial) {
                            i = userId;
                            newUserId = i;
                        }
                    }
                    i = ui.profileGroupId;
                    newUserId = i;
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        return newUserId;
    }

    /* access modifiers changed from: package-private */
    public int broadcastIntentLocked(ProcessRecord callerApp, String callerPackage, Intent intent, String resolvedType, IIntentReceiver resultTo, int resultCode, String resultData, Bundle resultExtras, String[] requiredPermissions, int appOp, Bundle bOptions, boolean isOrdered, boolean isSticky, int callingPid, int callingUid, int realCallingUid, int realCallingPid, int userId) {
        String action = intent.getAction();
        if ("com.android.launcher.action.INSTALL_SHORTCUT".equals(action)) {
            intent.putExtra("android.intent.extra.USER_ID", userId);
        }
        boolean isIawareNeedSkip = true;
        if (getBgBroadcastQueue().getMtmBRManager() == null || !getBgBroadcastQueue().getMtmBRManager().iawareNeedSkipBroadcastSend(action, new Object[]{intent})) {
            isIawareNeedSkip = false;
        }
        if (!SmartDualCardConsts.SYSTEM_STATE_NAME_WIFI_NETWORK_STATE_CHANGED.equals(action) || !isIawareNeedSkip) {
            return HwActivityManagerService.super.broadcastIntentLocked(callerApp, callerPackage, intent, resolvedType, resultTo, resultCode, resultData, resultExtras, requiredPermissions, appOp, bOptions, isOrdered, isSticky, callingPid, callingUid, realCallingUid, realCallingPid, userId);
        }
        return 0;
    }

    public int iawareGetUidProcNum(int uid) {
        UidRecord uidRec = this.mProcessList.mActiveUids.get(uid);
        if (uidRec == null) {
            return 0;
        }
        return uidRec.numProcs;
    }

    public void updateFingerprintSlideSwitch() {
        if (HwPCUtils.enabled() && (this.mWindowManager instanceof HwWindowManagerService)) {
            this.mWindowManager.updateFingerprintSlideSwitch();
        }
    }

    public void freezeOrThawRotationInPcMode() {
        if (HwPCUtils.enabledInPad()) {
            synchronized (this) {
                this.mWindowManager.freezeDisplayRotation(HwPCUtils.getPCDisplayID(), 1);
            }
        }
    }

    public void relaunchIMEIfNecessary() {
        InputMethodManager imm;
        if (checkCallingPermission("android.permission.KILL_BACKGROUND_PROCESSES") == 0 || checkCallingPermission("android.permission.RESTART_PACKAGES") == 0) {
            Slog.i(TAG, "relaunchIMEIfNecessary: enter.");
            long callingId = Binder.clearCallingIdentity();
            try {
                String packageName = Settings.Secure.getString(this.mContext.getContentResolver(), "default_input_method");
                List<InputMethodInfo> methodList = null;
                if (HwPCUtils.enabledInPad() && (imm = (InputMethodManager) this.mContext.getSystemService("input_method")) != null) {
                    methodList = imm.getInputMethodList();
                }
                if (packageName == null) {
                    Slog.w(TAG, "relaunchIMEIfNecessary: IME packageName is null, return.");
                    return;
                }
                int index = packageName.indexOf(47);
                if (index == -1) {
                    Slog.w(TAG, "relaunchIMEIfNecessary: IME packageName is invalid, return.");
                    Binder.restoreCallingIdentity(callingId);
                    return;
                }
                String packageName2 = packageName.substring(0, index);
                int userId = this.mUserController.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), UserHandle.myUserId(), true, 2, "killBackgroundProcesses", (String) null);
                IPackageManager pm = AppGlobals.getPackageManager();
                synchronized (this) {
                    int appId = -1;
                    try {
                        appId = UserHandle.getAppId(pm.getPackageUid(packageName2, 268435456, userId));
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Unable to get Package Uid, userId = " + userId);
                    }
                    if (appId == -1) {
                        Slog.w(TAG, "Invalid packageName: " + packageName2);
                        Binder.restoreCallingIdentity(callingId);
                        return;
                    }
                    killPackageProcesses(packageName2, methodList, userId, appId);
                    Binder.restoreCallingIdentity(callingId);
                }
            } finally {
                Binder.restoreCallingIdentity(callingId);
            }
        } else {
            String msg = "Permission Denial: killBackgroundProcesses() from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires android.permission.KILL_BACKGROUND_PROCESSES";
            Slog.w(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    private void killPackageProcesses(String packageName, List<InputMethodInfo> methodList, int userId, int appId) {
        if (!HwPCUtils.enabledInPad() || methodList == null) {
            Slog.i(TAG, "killPackageProcesses: call killPackageProcessesLocked.");
            this.mProcessList.killPackageProcessesLocked(packageName, appId, userId, 0, false, false, true, false, false, "relaunchIME");
            return;
        }
        for (InputMethodInfo mi : methodList) {
            this.mProcessList.killPackageProcessesLocked(mi.getPackageName(), appId, userId, 100, false, false, true, false, false, "relaunchIME");
        }
    }

    public void registerExternalPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mWindowManager.registerPointerEventListener(listener, HwPCUtils.getPCDisplayID());
    }

    public void registerExternalPointerEventListener(PointerEventListenerEx listenerEx) {
        this.mWindowManager.registerPointerEventListener(listenerEx.getPointerEventListenerBridge(), HwPCUtils.getPCDisplayID());
    }

    public void unregisterExternalPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mWindowManager.unregisterPointerEventListener(listener, HwPCUtils.getPCDisplayID());
    }

    public void unregisterExternalPointerEventListener(PointerEventListenerEx listenerEx) {
        this.mWindowManager.unregisterPointerEventListener(listenerEx.getPointerEventListenerBridge(), HwPCUtils.getPCDisplayID());
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
        ProcessRecord pr = (ProcessRecord) this.mProcessList.mProcessNames.get(packageName, uid);
        if (pr != null) {
            Slog.d(TAG, "pr.mDisplayId:" + pr.mDisplayId);
            return HwPCUtils.isValidExtDisplayId(pr.mDisplayId);
        }
        Slog.d(TAG, "no pr, packageName:" + packageName);
        return false;
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue getProcessBroadcastQueue(ProcessRecord callerApp, String callerPackage, Intent intent) {
        intent.removeFlags(HighBitsWCGModeID.MODE_SUPERGAMUT);
        if (isKeyAppBroadcastQueue(1, callerPackage) || isKeyAppBroadcastQueue(2, intent.getAction())) {
            BroadcastQueue queue = keyAppBroadcastQueueForIntent(intent);
            intent.addFlags(4096);
            return queue;
        } else if (!isThirdPartyAppBroadcastQueue(callerApp)) {
            return broadcastQueueForIntent(intent);
        } else {
            BroadcastQueue queue2 = thirdPartyAppBroadcastQueueForIntent(intent);
            intent.addFlags(8192);
            return queue2;
        }
    }

    public WindowProcessController getWindowProcessController(int callerPid) {
        ProcessRecord app = getProcessRecordLocked(callerPid);
        if (app == null) {
            return null;
        }
        return app.getWindowProcessController();
    }

    public WindowProcessControllerEx getWindowProcessControllerEx(int callerPid) {
        return new WindowProcessControllerEx(getWindowProcessController(callerPid));
    }

    public int getStableProviderProcStatus(int procPid) {
        if (this.mHwAMSEx != null) {
            return this.mHwAMSEx.getStableProviderProcStatus(procPid);
        }
        return 0;
    }

    public boolean isCurrentUserEmpty() {
        return getCurrentUser() == null;
    }

    public int getCurrentUserId() {
        if (getCurrentUser() != null) {
            return getCurrentUser().id;
        }
        return 0;
    }

    public boolean updateDisplayOverrideConfiguration(Configuration values, int displayId) {
        return this.mActivityTaskManager.updateDisplayOverrideConfiguration(values, displayId);
    }

    public ActivityTaskManagerServiceEx getActivityTaskManagerServiceEx() {
        ActivityTaskManagerServiceEx atmsEx = new ActivityTaskManagerServiceEx();
        atmsEx.setActivityTaskManagerService(this.mActivityTaskManager);
        return atmsEx;
    }

    public List<ActivityManager.RecentTaskInfo> getRecentTasksList(int maxNum, int flags, int userId) {
        if (getRecentTasks(maxNum, flags, userId) == null) {
            return null;
        }
        return getRecentTasks(maxNum, flags, userId).getList();
    }

    public HandlerThread getAmsHandlerThread() {
        return this.mHandlerThread;
    }

    public void setLocalThreadReason(String reason) {
        this.mLocalStopReason.set(reason);
    }

    public void togglePCMode(boolean pcMode, int displayId) {
        this.mActivityTaskManager.mHwATMSEx.togglePCMode(pcMode, displayId);
    }

    public void toggleHome() {
        this.mActivityTaskManager.mHwATMSEx.toggleHome();
    }

    public void hwRestoreTask(int taskId, float xPos, float yPos) {
        this.mActivityTaskManager.mHwATMSEx.hwRestoreTask(taskId, xPos, yPos);
    }

    public void hwResizeTask(int taskId, Rect bounds) {
        this.mActivityTaskManager.mHwATMSEx.hwResizeTask(taskId, bounds);
    }

    public void moveTaskBackwards(int taskId) {
        this.mActivityTaskManager.mHwATMSEx.moveTaskBackwards(taskId);
    }

    public Bitmap getTaskThumbnailOnPCMode(int taskId) {
        return this.mActivityTaskManager.mHwATMSEx.getTaskThumbnailOnPCMode(taskId);
    }

    public HwRecentTaskInfo getHwRecentTaskInfo(int taskId) {
        return this.mActivityTaskManager.mHwATMSEx.getHwRecentTaskInfo(taskId);
    }

    public void registerHwTaskStackListener(TaskStackListenerEx listener) {
        this.mActivityTaskManager.mHwATMSEx.registerHwTaskStackListener(listener.getTaskStackListener());
    }

    public void unRegisterHwTaskStackListener(TaskStackListenerEx listener) {
        this.mActivityTaskManager.mHwATMSEx.unRegisterHwTaskStackListener(listener.getTaskStackListener());
    }

    public Bitmap getDisplayBitmap(int displayId, int width, int height) {
        if (this.mWindowManager instanceof HwWindowManagerService) {
            return this.mWindowManager.getDisplayBitmap(displayId, width, height);
        }
        return null;
    }
}
