package com.android.server.mtm;

import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.app.mtm.IMultiTaskManagerService.Stub;
import android.app.mtm.IMultiTaskProcessObserver;
import android.app.mtm.iaware.HwAppStartupSetting;
import android.app.mtm.iaware.HwAppStartupSettingFilter;
import android.app.mtm.iaware.RSceneData;
import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.AppMngConstant.AppCleanSource;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.rms.iaware.AwareLog;
import android.rms.iaware.RPolicyData;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.SomeArgs;
import com.android.server.ServiceThread;
import com.android.server.mtm.dump.DumpCase;
import com.android.server.mtm.iaware.RPolicyManager;
import com.android.server.mtm.iaware.appmng.AwareAppMngSort;
import com.android.server.mtm.iaware.appmng.appclean.AppCleaner;
import com.android.server.mtm.iaware.appmng.appstart.AwareAppStartupPolicy;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.srms.AwareBroadcastDumpRadar;
import com.android.server.mtm.iaware.srms.AwareBroadcastPolicy;
import com.android.server.mtm.taskstatus.ProcessCleaner;
import com.android.server.mtm.taskstatus.ProcessInfoCollector;
import com.android.server.rms.algorithm.AwareUserHabit;
import com.android.server.rms.iaware.appmng.AppMngConfig;
import com.android.server.rms.iaware.srms.AppCleanupDumpRadar;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

public class MultiTaskManagerService extends Stub {
    static final boolean DEBUG = false;
    private static final int DELAY_TIME_MSG_APP_CLEAN = 3000;
    private static final int MSG_APP_CLEAN = 301;
    private static final int MSG_APP_CLEAN_WITH_CALLBACK = 300;
    private static final int MSG_GROUP_CHANGE_NOTIFY = 1;
    public static final int MSG_IMPLICIT_BR = 400;
    public static final int MSG_POLICY_BR = 200;
    public static final int MSG_PROCESS_BR = 100;
    private static final int MSG_PROCESS_CHANGE_NOTIFY = 2;
    private static final int MSG_PROCESS_DIE_NOTIFY = 3;
    private static final int MSG_RESOURCE_NOTIFY = 4;
    public static final int MSG_TRACK_BR = 5;
    public static final int MY_PID = Process.myPid();
    static final String TAG = "MultiTaskManagerService";
    private static MultiTaskManagerService mSelf;
    HandlerThread handlerThread = new ServiceThread(TAG, -2, false);
    private AwareJobSchedulerService mAwareJobSchedulerService = null;
    private final Context mContext;
    private final Handler mHandler;
    private Callback mHandlerCallback = new Callback() {
        public boolean handleMessage(Message msg) {
            AppCleaner cleaner;
            switch (msg.what) {
                case 1:
                case 2:
                    if (MultiTaskManagerService.this.mProcInfo != null) {
                        MultiTaskManagerService.this.mProcInfo.recordProcessInfo(msg.arg1, msg.arg2);
                    }
                    return true;
                case 3:
                    if (MultiTaskManagerService.this.mProcInfo != null) {
                        MultiTaskManagerService.this.mProcInfo.removeKilledProcess(msg.arg1);
                    }
                    return true;
                case 300:
                    SomeArgs args = msg.obj;
                    AppCleanParam param = args.arg1;
                    IAppCleanCallback callback = args.arg2;
                    cleaner = AppCleaner.getInstance(MultiTaskManagerService.this.mContext);
                    if (cleaner != null) {
                        cleaner.requestAppCleanWithCallback(param, callback);
                    }
                    return true;
                case 301:
                    AppCleanSource source = msg.obj.arg1;
                    cleaner = AppCleaner.getInstance(MultiTaskManagerService.this.mContext);
                    if (cleaner != null) {
                        cleaner.requestAppClean(source);
                    }
                    return true;
                default:
                    return false;
            }
        }
    };
    private AwareBroadcastPolicy mIawareBrPolicy = null;
    private AwareBroadcastDumpRadar mIawareBrRadar = null;
    private long mLastCheckSmartClean = 0;
    private ProcessCleaner mProcCleaner = null;
    private ProcessInfoCollector mProcInfo = null;
    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (Log.HWINFO) {
                Slog.i(MultiTaskManagerService.TAG, "onForegroundActivitiesChanged pid = " + pid + ", uid = " + uid);
            }
            MultiTaskManagerService.this.notifyProcessStatusChange(pid, uid);
            MultiTaskManagerService.this.dispatchFgActivitiesChanged(pid, uid, foregroundActivities);
            if (MultiTaskManagerService.this.mIawareBrPolicy != null) {
                MultiTaskManagerService.this.mIawareBrPolicy.notifyIawareUnproxyBr(pid, uid);
            }
        }

        public void onProcessDied(int pid, int uid) {
            if (Log.HWINFO) {
                Slog.i(MultiTaskManagerService.TAG, "onProcessDied pid = " + pid + ", uid = " + uid);
            }
            MultiTaskManagerService.this.notifyProcessDiedChange(pid, uid);
            MultiTaskManagerService.this.dispatchProcessDied(pid, uid);
        }
    };
    final RemoteCallbackList<IMultiTaskProcessObserver> mProcessObserverList = new RemoteCallbackList();
    private RPolicyManager mRPolicyManager = null;
    IPackageManager pm = null;

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
        this.mRPolicyManager = new RPolicyManager(this.mContext, this.handlerThread);
        AwareAppStartupPolicy.getInstance(context, this.handlerThread);
        this.mIawareBrPolicy = new AwareBroadcastPolicy(this.mHandler);
        this.mIawareBrPolicy.init();
        this.mIawareBrRadar = new AwareBroadcastDumpRadar(this.mHandler);
        this.mAwareJobSchedulerService = new AwareJobSchedulerService(context, this.handlerThread);
        AppCleanupDumpRadar.getInstance().setHandler(this.mHandler);
    }

    public static MultiTaskManagerService self() {
        return mSelf;
    }

    private static void setSelf(MultiTaskManagerService curInstance) {
        mSelf = curInstance;
    }

    public Context context() {
        return this.mContext;
    }

    private void systemReady() {
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
            if (Log.HWINFO) {
                Slog.i(TAG, "MultiTaskManagerService systemReady success");
            }
            this.pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        } catch (RemoteException e) {
            Slog.e(TAG, "MultiTaskManagerService systemReady failed");
        }
    }

    private void dispatchFgActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
        int i = this.mProcessObserverList.beginBroadcast();
        while (i > 0) {
            i--;
            IMultiTaskProcessObserver observer = (IMultiTaskProcessObserver) this.mProcessObserverList.getBroadcastItem(i);
            if (observer != null) {
                try {
                    observer.onForegroundActivitiesChanged(pid, uid, foregroundActivities);
                } catch (RemoteException e) {
                }
            }
        }
        this.mProcessObserverList.finishBroadcast();
    }

    private void dispatchProcessStateChanged(int pid, int uid, int procState) {
        int i = this.mProcessObserverList.beginBroadcast();
        while (i > 0) {
            i--;
            IMultiTaskProcessObserver observer = (IMultiTaskProcessObserver) this.mProcessObserverList.getBroadcastItem(i);
            if (observer != null) {
                try {
                    observer.onProcessStateChanged(pid, uid, procState);
                } catch (RemoteException e) {
                }
            }
        }
        this.mProcessObserverList.finishBroadcast();
    }

    private void dispatchProcessDied(int pid, int uid) {
        int i = this.mProcessObserverList.beginBroadcast();
        while (i > 0) {
            i--;
            IMultiTaskProcessObserver observer = (IMultiTaskProcessObserver) this.mProcessObserverList.getBroadcastItem(i);
            if (observer != null) {
                try {
                    observer.onProcessDied(pid, uid);
                } catch (RemoteException e) {
                }
            }
        }
        this.mProcessObserverList.finishBroadcast();
    }

    public void notifyResourceStatusOverload(int resourcetype, String resourceextend, int resourcestatus, Bundle args) {
        enforceCallingPermission();
        Message msg = this.mHandler.obtainMessage();
        msg.what = 4;
        msg.arg1 = resourcetype;
        msg.arg2 = resourcestatus;
        SomeArgs othereargs = SomeArgs.obtain();
        othereargs.arg1 = resourceextend;
        othereargs.arg2 = args;
        msg.obj = othereargs;
        this.mHandler.sendMessage(msg);
    }

    public void registerObserver(IMultiTaskProcessObserver observer) {
        enforceCallingPermission();
        synchronized (this) {
            this.mProcessObserverList.register(observer);
        }
    }

    public void unregisterObserver(IMultiTaskProcessObserver observer) {
        enforceCallingPermission();
        synchronized (this) {
            this.mProcessObserverList.unregister(observer);
        }
    }

    private void enforceCallingPermission() {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        if (pid != Process.myPid() && uid != 0 && uid != 1000) {
            String msg = "Permission Denial: can not access MultiTaskManagerService! pid = " + pid + ",uid = " + uid;
            Slog.e(TAG, msg);
            throw new SecurityException(msg);
        }
    }

    int checkCallingPermission(String permission) {
        return checkPermission(permission, Binder.getCallingPid(), UserHandle.getAppId(Binder.getCallingUid()));
    }

    public int checkPermission(String permission, int pid, int uid) {
        if (permission == null) {
            return -1;
        }
        if (pid == MY_PID || uid == 0 || uid == 1000) {
            return 0;
        }
        if (UserHandle.isIsolated(uid)) {
            return -1;
        }
        try {
            if (this.pm == null) {
                this.pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            }
            if (this.pm != null) {
                return this.pm.checkUidPermission(permission, uid);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "PackageManager is dead?!?", e);
        }
        return -1;
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (checkCallingPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump Multi task service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " without permission " + "android.permission.DUMP");
            return;
        }
        if (Binder.getCallingUid() <= 1000) {
            DumpCase.dump(this.mContext, pw, args);
        }
    }

    public boolean killProcess(int pid, boolean restartservice) {
        enforceCallingPermission();
        return this.mProcCleaner.killProcess(pid, restartservice);
    }

    public boolean forcestopApps(int pid) {
        enforceCallingPermission();
        return this.mProcCleaner.forcestopApps(pid);
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

    private void notifyProcessStatusChange(int pid, int uid) {
        enforceCallingPermission();
        sendProcessChangeMessage(2, pid, uid);
    }

    private void sendProcessChangeMessage(int event, int pid, int uid) {
        if (this.mHandler != null) {
            Message msg = this.mHandler.obtainMessage();
            msg.what = event;
            msg.arg1 = pid;
            msg.arg2 = uid;
            this.mHandler.sendMessage(msg);
        }
    }

    public boolean reportScene(int featureId, RSceneData scene) {
        enforceCallingPermission();
        return this.mRPolicyManager.reportScene(featureId, scene);
    }

    public RPolicyData acquirePolicyData(int featureId, RSceneData scene) {
        enforceCallingPermission();
        return this.mRPolicyManager.acquirePolicyData(featureId, scene);
    }

    private void enforceCallerSystemUid() {
        if (1000 != UserHandle.getAppId(Binder.getCallingUid())) {
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
        AwareAppStartupPolicy policy = AwareAppStartupPolicy.self();
        if (policy != null) {
            return policy.updateCloudPolicy(filePath);
        }
        AwareLog.w(TAG, "updateCloudPolicy policy not ready!");
        return false;
    }

    private void enforceCallerRootOrSys() {
        int uid = Binder.getCallingUid();
        if (uid != 0 && 1000 != UserHandle.getAppId(uid)) {
            throw new SecurityException("Permission Denial: can not access MultiTaskManagerService! uid = " + uid);
        }
    }

    public void requestAppCleanWithCallback(AppCleanParam param, IAppCleanCallback callback) {
        enforceCallerRootOrSys();
        Message msg = this.mHandler.obtainMessage();
        msg.what = 300;
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = param;
        args.arg2 = callback;
        msg.obj = args;
        this.mHandler.sendMessage(msg);
    }

    public AwareBroadcastPolicy getIawareBrPolicy() {
        enforceCallingPermission();
        return this.mIawareBrPolicy;
    }

    public void requestAppClean(AppCleanSource source) {
        if (source == null) {
            AwareLog.e(TAG, "requestAppClean source = null");
            return;
        }
        Message msg = this.mHandler.obtainMessage();
        msg.what = 301;
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = source;
        msg.obj = args;
        if (AppCleanSource.SMART_CLEAN.equals(source)) {
            long currTime = SystemClock.elapsedRealtime();
            if (((long) AppMngConfig.getSmartCleanInterval()) <= currTime - this.mLastCheckSmartClean) {
                this.mLastCheckSmartClean = currTime;
                this.mHandler.removeMessages(301);
                this.mHandler.sendMessageDelayed(msg, 3000);
            }
        } else {
            this.mHandler.sendMessage(msg);
        }
    }

    public void cancelAppClean() {
        this.mHandler.removeMessages(301);
    }

    public AwareBroadcastDumpRadar getIawareBrRadar() {
        enforceCallingPermission();
        return this.mIawareBrRadar;
    }

    public AwareJobSchedulerService getAwareJobSchedulerService() {
        enforceCallingPermission();
        return this.mAwareJobSchedulerService;
    }
}
