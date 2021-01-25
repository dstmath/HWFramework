package com.android.server.mtm;

import android.app.mtm.IMultiTaskProcessObserver;
import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.app.mtm.iaware.SceneData;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.rms.iaware.RPolicyData;
import com.android.server.am.HwActivityManagerService;
import com.android.server.mtm.dump.DumpCase;
import com.android.server.mtm.iaware.PolicyManager;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.AwareProcessBaseInfo;
import com.android.server.mtm.iaware.appmng.appclean.AppCleaner;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.algorithm.ActivityTopManagerRt;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import com.huawei.android.app.ActivityManagerNativeEx;
import com.huawei.android.app.IProcessObserverEx;
import com.huawei.android.content.pm.IPackageManagerExt;
import com.huawei.android.internal.os.SomeArgsEx;
import com.huawei.android.os.ProcessExt;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.ServiceThreadExt;
import com.huawei.util.LogEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class MultiTaskManagerService extends DefaultMultiTaskManagerService {
    private static final boolean DEBUG = false;
    private static final int DELAY_TIME_MSG_APP_CLEAN = 3000;
    private static final int MSG_APP_CLEAN = 301;
    private static final int MSG_APP_CLEAN_WITH_CALLBACK = 300;
    private static final int MSG_GROUP_CHANGE_NOTIFY = 1;
    public static final int MSG_IMPLICIT_BR = 400;
    private static final int MSG_INIT_MTM_PROCESS = 500;
    public static final int MSG_POLICY_BR = 200;
    public static final int MSG_PROCESS_BR = 100;
    private static final int MSG_PROCESS_CHANGE_NOTIFY = 2;
    private static final int MSG_PROCESS_DIE_NOTIFY = 3;
    private static final int MSG_RESOURCE_NOTIFY = 4;
    public static final int MSG_TRACK_BR = 5;
    public static final int MY_PID = Process.myPid();
    private static final int PERSISTENT_PROC_ADJ = -800;
    private static final String TAG = "MultiTaskManagerService";
    private static MultiTaskManagerService sSelf;
    private HandlerThread handlerThread = this.thread.getHandlerThread();
    private AwareBroadcastPolicy mAwareBrPolicy = null;
    private AwareBroadcastDumpRadar mAwareBrRadar = null;
    private AwareJobSchedulerService mAwareJobSchedulerService = null;
    private final Context mContext;
    private final Handler mHandler;
    private Handler.Callback mHandlerCallback = new Handler.Callback() {
        /* class com.android.server.mtm.MultiTaskManagerService.AnonymousClass1 */

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1 || i == 2) {
                MultiTaskManagerService.this.doProcessChangeNotify(msg);
                return true;
            } else if (i == 3) {
                if (MultiTaskManagerService.this.mProcInfo != null) {
                    MultiTaskManagerService.this.mProcInfo.removeKilledProcess(msg.arg1);
                    if (MultiTaskManagerService.this.mAwareBrPolicy != null) {
                        MultiTaskManagerService.this.mAwareBrPolicy.clearCacheBr(msg.arg1);
                    }
                }
                return true;
            } else if (i == 300) {
                if (msg.obj instanceof SomeArgsEx) {
                    MultiTaskManagerService.this.doCleanWithCallback(null, (SomeArgsEx) msg.obj);
                }
                return true;
            } else if (i == 301) {
                if (msg.obj instanceof SomeArgsEx) {
                    MultiTaskManagerService.this.doAppCleanAction(null, (SomeArgsEx) msg.obj);
                }
                return true;
            } else if (i != MultiTaskManagerService.MSG_INIT_MTM_PROCESS) {
                return false;
            } else {
                MultiTaskManagerService.this.addAllProcessToMtm();
                return true;
            }
        }
    };
    private long mLastCheckSmartClean = 0;
    private final Object mLock = new Object();
    private PolicyManager mPolicyManager = null;
    private ProcessCleaner mProcCleaner = null;
    private ProcessInfoCollector mProcInfo = null;
    private IProcessObserverEx mProcessObserver = new IProcessObserverEx() {
        /* class com.android.server.mtm.MultiTaskManagerService.AnonymousClass2 */

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (LogEx.getLogHWInfo()) {
                SlogEx.i(MultiTaskManagerService.TAG, "onForegroundActivitiesChanged pid = " + pid + ", uid = " + uid);
            }
            MultiTaskManagerService.this.notifyProcessStatusChange(pid, uid);
            MultiTaskManagerService.this.dispatchFgActivitiesChanged(pid, uid, foregroundActivities);
            if (MultiTaskManagerService.this.mAwareBrPolicy != null) {
                MultiTaskManagerService.this.mAwareBrPolicy.notifyAwareUnproxyBr(pid, uid);
            }
        }

        public void onProcessDied(int pid, int uid) {
            if (LogEx.getLogHWInfo()) {
                SlogEx.i(MultiTaskManagerService.TAG, "onProcessDied pid = " + pid + ", uid = " + uid);
            }
            MultiTaskManagerService.this.notifyProcessDiedChange(pid, uid);
            MultiTaskManagerService.this.dispatchProcessDied(pid, uid);
        }
    };
    final RemoteCallbackList<IMultiTaskProcessObserver> mProcessObserverList = new RemoteCallbackList<>();
    private ServiceThreadExt thread = new ServiceThreadExt(TAG, -2, false);

    public MultiTaskManagerService(Context context) {
        this.mContext = context;
        setSelf(this);
        systemReady();
        this.handlerThread.start();
        this.mHandler = new Handler(this.handlerThread.getLooper(), this.mHandlerCallback);
        this.mProcInfo = ProcessInfoCollector.getInstance();
        this.mProcCleaner = ProcessCleaner.getInstance(this.mContext);
        AwareAppMngSort.getInstance(context);
        AwareUserHabit.getInstance(context);
        ActivityTopManagerRt.getInstance(context);
        this.mPolicyManager = new PolicyManager(this.mContext, this.handlerThread);
        AwareAppStartupPolicy.getInstance(context, this.handlerThread);
        this.mAwareBrPolicy = new AwareBroadcastPolicy(this.mHandler, context);
        this.mAwareBrPolicy.init();
        this.mAwareBrRadar = new AwareBroadcastDumpRadar(this.mHandler);
        this.mAwareJobSchedulerService = new AwareJobSchedulerService(context, this.handlerThread);
        AppCleanupDumpRadar.getInstance().setHandler(this.mHandler);
        this.mHandler.sendEmptyMessage(MSG_INIT_MTM_PROCESS);
    }

    public static MultiTaskManagerService self() {
        return sSelf;
    }

    private static void setSelf(MultiTaskManagerService curInstance) {
        sSelf = curInstance;
    }

    public Context context() {
        return this.mContext;
    }

    private void systemReady() {
        try {
            ActivityManagerNativeEx.registerProcessObserver(this.mProcessObserver);
            if (LogEx.getLogHWInfo()) {
                SlogEx.i(TAG, "MultiTaskManagerService systemReady success.");
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "MultiTaskManagerService systemReady failed.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doProcessChangeNotify(Message msg) {
        if (this.mProcInfo != null) {
            boolean isFgChange = false;
            if (msg.what == 2 && this.mProcInfo.getProcessInfo(msg.arg1) != null) {
                isFgChange = true;
            }
            this.mProcInfo.recordProcessInfo(msg.arg1, msg.arg2);
            if (isFgChange) {
                this.mProcInfo.setAwareProcessState(msg.arg1, msg.arg2, -1);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doCleanWithCallback(AppCleaner cleaner, SomeArgsEx args) {
        AppCleanParam param = (AppCleanParam) args.arg1();
        IAppCleanCallback callback = (IAppCleanCallback) args.arg2();
        AppCleaner cleaner2 = AppCleaner.getInstance(this.mContext);
        if (cleaner2 != null) {
            cleaner2.requestAppCleanWithCallback(param, callback);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doAppCleanAction(AppCleaner cleaner, SomeArgsEx arg) {
        AppMngConstant.AppCleanSource source = (AppMngConstant.AppCleanSource) arg.arg1();
        AppCleaner cleaner2 = AppCleaner.getInstance(this.mContext);
        if (cleaner2 != null) {
            cleaner2.requestAppClean(source);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchFgActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        int count = this.mProcessObserverList.beginBroadcast();
        while (count > 0) {
            count--;
            IMultiTaskProcessObserver observer = this.mProcessObserverList.getBroadcastItem(count);
            if (observer != null) {
                try {
                    observer.onForegroundActivitiesChanged(pid, uid, foregroundActivities);
                } catch (RemoteException e) {
                    AwareLog.w(TAG, "Call observer.onForegroundActivitiesChanged may fail.");
                }
            }
        }
        this.mProcessObserverList.finishBroadcast();
    }

    private void dispatchProcessStateChanged(int pid, int uid, int procState) {
        int count = this.mProcessObserverList.beginBroadcast();
        while (count > 0) {
            count--;
            IMultiTaskProcessObserver observer = this.mProcessObserverList.getBroadcastItem(count);
            if (observer != null) {
                try {
                    observer.onProcessStateChanged(pid, uid, procState);
                } catch (RemoteException e) {
                    AwareLog.w(TAG, "Call observer.onProcessStateChanged may fail.");
                }
            }
        }
        this.mProcessObserverList.finishBroadcast();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchProcessDied(int pid, int uid) {
        int count = this.mProcessObserverList.beginBroadcast();
        while (count > 0) {
            count--;
            IMultiTaskProcessObserver observer = this.mProcessObserverList.getBroadcastItem(count);
            if (observer != null) {
                try {
                    observer.onProcessDied(pid, uid);
                } catch (RemoteException e) {
                    AwareLog.w(TAG, "Call observer.onProcessDied may fail.");
                }
            }
        }
        this.mProcessObserverList.finishBroadcast();
    }

    public void notifyResourceStatusOverload(int resourceType, String resourceExtend, int state, Bundle args) {
        enforceCallingPermission();
        Message msg = this.mHandler.obtainMessage();
        msg.what = 4;
        msg.arg1 = resourceType;
        msg.arg2 = state;
        SomeArgsEx othereargs = SomeArgsEx.obtain();
        othereargs.setArg1(resourceExtend);
        othereargs.setArg2(args);
        msg.obj = othereargs;
        this.mHandler.sendMessage(msg);
    }

    public void registerObserver(IMultiTaskProcessObserver observer) {
        enforceCallingPermission();
        synchronized (this.mLock) {
            this.mProcessObserverList.register(observer);
        }
    }

    public void unregisterObserver(IMultiTaskProcessObserver observer) {
        enforceCallingPermission();
        synchronized (this.mLock) {
            this.mProcessObserverList.unregister(observer);
        }
    }

    private void enforceCallingPermission() {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        if (pid != Process.myPid() && uid != 0 && uid != 1000) {
            String msg = "Permission Denial: can not access MultiTaskManagerService! pid = " + pid + ", uid = " + uid;
            SlogEx.e(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    /* access modifiers changed from: package-private */
    public int checkCallingPermission(String permission) {
        return checkPermission(permission, Binder.getCallingPid(), UserHandleEx.getAppId(Binder.getCallingUid()));
    }

    public int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return -1;
        }
        if (pid == MY_PID || uid == 0 || uid == 1000) {
            return 0;
        }
        if (UserHandleEx.isIsolated(uid)) {
            return -1;
        }
        try {
            return IPackageManagerExt.checkUidPermission(permission, uid);
        } catch (RemoteException e) {
            SlogEx.e(TAG, "PackageManager is dead?!?");
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump Multi task service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission android.permission.DUMP");
        } else if (Binder.getCallingUid() <= 1000) {
            DumpCase.dump(this.mContext, pw, args);
        }
    }

    public boolean killProcess(int pid, boolean restartService) {
        enforceCallingPermission();
        return this.mProcCleaner.killProcess(pid, restartService);
    }

    public boolean forcestopApps(int pid) {
        enforceCallingPermission();
        return this.mProcCleaner.forceStopApps(pid);
    }

    public void notifyProcessGroupChange(int pid, int uid) {
        enforceCallingPermission();
        sendProcessChangeMessage(1, pid, uid);
    }

    public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) {
        enforceCallingPermission();
        sendProcessChangeMessage(2, pid, uid);
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            policy.notifyProcessStart(pkg, process, hostingType, pid, uid);
        }
    }

    public void notifyProcessDiedChange(int pid, int uid) {
        enforceCallingPermission();
        sendProcessChangeMessage(3, pid, uid);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyProcessStatusChange(int pid, int uid) {
        enforceCallingPermission();
        sendProcessChangeMessage(2, pid, uid);
    }

    private void sendProcessChangeMessage(int event, int pid, int uid) {
        Handler handler = this.mHandler;
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.what = event;
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mHandler.sendMessage(msg);
        }
    }

    public boolean reportScene(int featureId, SceneData scene) {
        enforceCallingPermission();
        return this.mPolicyManager.reportScene(featureId, scene);
    }

    public RPolicyData acquirePolicyData(int featureId, SceneData scene) {
        enforceCallingPermission();
        return this.mPolicyManager.acquirePolicyData(featureId, scene);
    }

    private void enforceCallerSystemUid() {
        if (UserHandleEx.getAppId(Binder.getCallingUid()) != 1000) {
            throw new SecurityException("Only system uid apps have mtm calling permission");
        }
    }

    public List<HwAppStartupSetting> retrieveAppStartupSettings(List<String> pkgList, HwAppStartupSettingFilter filter) throws RemoteException {
        enforceCallerSystemUid();
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            return policy.retrieveAppStartupSettings(pkgList, filter);
        }
        AwareLog.w(TAG, "retrieveAppStartupSettings policy not ready!");
        return null;
    }

    public List<String> retrieveAppStartupPackages(List<String> pkgList, int[] policy, int[] modifier, int[] show) throws RemoteException {
        enforceCallerSystemUid();
        AwareAppStartupPolicy appStartupPolicy = AwareAppStartupPolicy.self();
        if (appStartupPolicy != null) {
            return appStartupPolicy.retrieveAppStartupPackages(pkgList, policy, modifier, show);
        }
        AwareLog.w(TAG, "retrieveAppStartupPackages policy not ready!");
        return null;
    }

    public boolean updateAppStartupSettings(List<HwAppStartupSetting> settingList, boolean clearFirst) throws RemoteException {
        enforceCallerSystemUid();
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            return policy.updateAppStartupSettings(settingList, clearFirst);
        }
        AwareLog.w(TAG, "updateAppStartupSettings policy not ready!");
        return false;
    }

    public boolean removeAppStartupSetting(String pkgName) throws RemoteException {
        enforceCallerSystemUid();
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            return policy.removeAppStartupSetting(pkgName);
        }
        AwareLog.w(TAG, "removeAppStartupSetting policy not ready!");
        return false;
    }

    public boolean updateCloudPolicy(String filePath) throws RemoteException {
        enforceCallerSystemUid();
        AwareLog.w(TAG, "old cloud update, please use new cloud update!");
        return false;
    }

    private void enforceCallerRootOrSys() {
        int uid = Binder.getCallingUid();
        if (uid != 0 && 1000 != UserHandleEx.getAppId(uid)) {
            throw new SecurityException("Permission Denial: can not access MultiTaskManagerService! uid = " + uid);
        }
    }

    public void requestAppCleanWithCallback(AppCleanParam param, IAppCleanCallback callback) {
        enforceCallerRootOrSys();
        Message msg = this.mHandler.obtainMessage();
        msg.what = 300;
        SomeArgsEx args = SomeArgsEx.obtain();
        args.setArg1(param);
        args.setArg2(callback);
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    public AwareBroadcastPolicy getAwareBrPolicy() {
        enforceCallingPermission();
        return this.mAwareBrPolicy;
    }

    public void requestAppClean(AppMngConstant.AppCleanSource source) {
        if (source == null) {
            AwareLog.e(TAG, "requestAppClean source = null");
            return;
        }
        Message msg = this.mHandler.obtainMessage();
        msg.what = 301;
        SomeArgsEx args = SomeArgsEx.obtain();
        args.setArg1(source);
        msg.obj = args;
        if (AppMngConstant.AppCleanSource.SMART_CLEAN.equals(source)) {
            long currTime = SystemClock.elapsedRealtime();
            if (((long) AppMngConfig.getSmartCleanInterval()) <= currTime - this.mLastCheckSmartClean) {
                this.mLastCheckSmartClean = currTime;
                this.mHandler.removeMessages(301);
                this.mHandler.sendMessageDelayed(msg, 3000);
                return;
            }
            return;
        }
        this.mHandler.sendMessage(msg);
    }

    public void cancelAppClean() {
        this.mHandler.removeMessages(301);
    }

    public AwareBroadcastDumpRadar getAwareBrRadar() {
        enforceCallingPermission();
        return this.mAwareBrRadar;
    }

    public AwareJobSchedulerService getAwareJobSchedulerService() {
        enforceCallingPermission();
        return this.mAwareJobSchedulerService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addAllProcessToMtm() {
        if (this.mProcInfo == null) {
            AwareLog.e(TAG, "procInfo is null, process start before MTM could missing.");
            return;
        }
        HwActivityManagerService ams = HwActivityManagerService.self();
        if (ams == null) {
            AwareLog.e(TAG, "ams is null!");
            return;
        }
        Map<Integer, AwareProcessBaseInfo> allProcInfo = ams.getAllProcessBaseInfo();
        if (allProcInfo.isEmpty()) {
            AwareLog.e(TAG, "there is no process in ams?");
            return;
        }
        for (Map.Entry<Integer, AwareProcessBaseInfo> info : allProcInfo.entrySet()) {
            this.mProcInfo.recordProcessInfo(info.getKey().intValue(), info.getValue().uid);
            if (info.getValue().curAdj == PERSISTENT_PROC_ADJ) {
                ProcessExt.setProcessGroup(info.getKey().intValue(), -1);
            }
        }
    }
}
