package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.app.WaitResult;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.AuxiliaryResolveInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.freeform.HwFreeFormManager;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Pools;
import android.util.Slog;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.Toast;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.HeavyWeightSwitcherActivity;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.am.ActivityStack;
import com.android.server.am.ActivityStackSupervisor;
import com.android.server.am.LaunchParamsController;
import com.android.server.os.HwBootCheck;
import com.android.server.pm.DumpState;
import com.android.server.pm.InstantAppResolver;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.huawei.server.am.IHwActivityStarterEx;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityStarter extends AbsActivityStarter {
    private static final String ACTION_HWCHOOSER = "com.huawei.intent.action.hwCHOOSER";
    static final int DEFAULT_DISPLAY_STARTED_APP = 0;
    private static final String EXTRA_ALWAYS_USE_OPTION = "alwaysUseOption";
    private static final String HWPCEXPLORER_PACKAGE_NAME = "com.huawei.desktop.explorer";
    private static final String INCALLUI_ACTIVITY_CLASS_NAME = "com.android.incallui/.InCallActivity";
    private static final int INVALID_LAUNCH_MODE = -1;
    static final int OTHER_DISPLAY_NOT_STARTED_APP = -1;
    static final int OTHER_DISPLAY_SPECIAL_APP = 2;
    static final int OTHER_DISPLAY_STARTED_APP = 1;
    protected static final String SUW_FRP_STATE = "hw_suw_frp_state";
    private static final String TAG = "ActivityManager";
    private static final String TAG_CONFIGURATION = (ActivityManagerService.TAG + ActivityManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_FOCUS = "ActivityManager";
    private static final String TAG_RESULTS = "ActivityManager";
    private static final String TAG_USER_LEAVING = "ActivityManager";
    static Map<Integer, Boolean> mLauncherStartState = new HashMap();
    private boolean mAddingToTask;
    private boolean mAvoidMoveToFront;
    private int mCallingUid;
    private final ActivityStartController mController;
    private boolean mDoResume;
    private IHwActivityStarterEx mHwActivityStarterEx;
    private TaskRecord mInTask;
    private Intent mIntent;
    private boolean mIntentDelivered;
    private final ActivityStartInterceptor mInterceptor;
    private boolean mKeepCurTransition;
    private final ActivityRecord[] mLastStartActivityRecord = new ActivityRecord[1];
    private int mLastStartActivityResult;
    private long mLastStartActivityTimeMs;
    private String mLastStartReason;
    private int mLaunchFlags;
    private int mLaunchMode;
    private LaunchParamsController.LaunchParams mLaunchParams = new LaunchParamsController.LaunchParams();
    private boolean mLaunchTaskBehind;
    private boolean mMovedToFront;
    private ActivityInfo mNewTaskInfo;
    private Intent mNewTaskIntent;
    private boolean mNoAnimation;
    private ActivityRecord mNotTop;
    protected ActivityOptions mOptions;
    private int mPreferredDisplayId;
    private Request mRequest = new Request();
    private TaskRecord mReuseTask;
    final ActivityManagerService mService;
    private boolean mShouldSkipStartingWindow;
    private ActivityRecord mSourceRecord;
    private ActivityStack mSourceStack;
    private ActivityRecord mStartActivity;
    private int mStartFlags;
    final ActivityStackSupervisor mSupervisor;
    private ActivityStack mTargetStack;
    private IVoiceInteractor mVoiceInteractor;
    private IVoiceInteractionSession mVoiceSession;

    static class DefaultFactory implements Factory {
        private final int MAX_STARTER_COUNT = 3;
        private ActivityStartController mController;
        private ActivityStartInterceptor mInterceptor;
        private ActivityManagerService mService;
        private Pools.SynchronizedPool<ActivityStarter> mStarterPool = new Pools.SynchronizedPool<>(3);
        private ActivityStackSupervisor mSupervisor;

        DefaultFactory(ActivityManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
            this.mService = service;
            this.mSupervisor = supervisor;
            this.mInterceptor = interceptor;
        }

        public void setController(ActivityStartController controller) {
            this.mController = controller;
        }

        public ActivityStarter obtain() {
            ActivityStarter starter = (ActivityStarter) this.mStarterPool.acquire();
            if (starter != null) {
                return starter;
            }
            HwServiceFactory.IHwActivityStarter iActivitySt = HwServiceFactory.getHwActivityStarter();
            if (iActivitySt != null) {
                return iActivitySt.getInstance(this.mController, this.mService, this.mSupervisor, this.mInterceptor);
            }
            return new ActivityStarter(this.mController, this.mService, this.mSupervisor, this.mInterceptor);
        }

        public void recycle(ActivityStarter starter) {
            starter.reset(true);
            this.mStarterPool.release(starter);
        }
    }

    @VisibleForTesting
    interface Factory {
        ActivityStarter obtain();

        void recycle(ActivityStarter activityStarter);

        void setController(ActivityStartController activityStartController);
    }

    private static class Request {
        private static final int DEFAULT_CALLING_PID = 0;
        private static final int DEFAULT_CALLING_UID = -1;
        ActivityInfo activityInfo;
        SafeActivityOptions activityOptions;
        boolean allowPendingRemoteAnimationRegistryLookup;
        boolean avoidMoveToFront;
        IApplicationThread caller;
        String callingPackage;
        int callingPid = -1;
        int callingUid = 0;
        boolean componentSpecified;
        Intent ephemeralIntent;
        int filterCallingUid;
        Configuration globalConfig;
        boolean ignoreTargetSecurity;
        TaskRecord inTask;
        Intent intent;
        boolean mayWait;
        ActivityRecord[] outActivity;
        ProfilerInfo profilerInfo;
        int realCallingPid;
        int realCallingUid;
        String reason;
        int requestCode;
        ResolveInfo resolveInfo;
        String resolvedType;
        IBinder resultTo;
        String resultWho;
        int startFlags;
        int userId;
        IVoiceInteractor voiceInteractor;
        IVoiceInteractionSession voiceSession;
        WaitResult waitResult;

        Request() {
            reset();
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.caller = null;
            this.intent = null;
            this.ephemeralIntent = null;
            this.resolvedType = null;
            this.activityInfo = null;
            this.resolveInfo = null;
            this.voiceSession = null;
            this.voiceInteractor = null;
            this.resultTo = null;
            this.resultWho = null;
            this.requestCode = 0;
            this.callingPid = 0;
            this.callingUid = -1;
            this.callingPackage = null;
            this.realCallingPid = 0;
            this.realCallingUid = 0;
            this.startFlags = 0;
            this.activityOptions = null;
            this.ignoreTargetSecurity = false;
            this.componentSpecified = false;
            this.outActivity = null;
            this.inTask = null;
            this.reason = null;
            this.profilerInfo = null;
            this.globalConfig = null;
            this.userId = 0;
            this.waitResult = null;
            this.mayWait = false;
            this.avoidMoveToFront = false;
            this.allowPendingRemoteAnimationRegistryLookup = true;
            this.filterCallingUid = -10000;
        }

        /* access modifiers changed from: package-private */
        public void set(Request request) {
            this.caller = request.caller;
            this.intent = request.intent;
            this.ephemeralIntent = request.ephemeralIntent;
            this.resolvedType = request.resolvedType;
            this.activityInfo = request.activityInfo;
            this.resolveInfo = request.resolveInfo;
            this.voiceSession = request.voiceSession;
            this.voiceInteractor = request.voiceInteractor;
            this.resultTo = request.resultTo;
            this.resultWho = request.resultWho;
            this.requestCode = request.requestCode;
            this.callingPid = request.callingPid;
            this.callingUid = request.callingUid;
            this.callingPackage = request.callingPackage;
            this.realCallingPid = request.realCallingPid;
            this.realCallingUid = request.realCallingUid;
            this.startFlags = request.startFlags;
            this.activityOptions = request.activityOptions;
            this.ignoreTargetSecurity = request.ignoreTargetSecurity;
            this.componentSpecified = request.componentSpecified;
            this.outActivity = request.outActivity;
            this.inTask = request.inTask;
            this.reason = request.reason;
            this.profilerInfo = request.profilerInfo;
            this.globalConfig = request.globalConfig;
            this.userId = request.userId;
            this.waitResult = request.waitResult;
            this.mayWait = request.mayWait;
            this.avoidMoveToFront = request.avoidMoveToFront;
            this.allowPendingRemoteAnimationRegistryLookup = request.allowPendingRemoteAnimationRegistryLookup;
            this.filterCallingUid = request.filterCallingUid;
        }
    }

    ActivityStarter(ActivityStartController controller, ActivityManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
        this.mController = controller;
        this.mService = service;
        this.mSupervisor = supervisor;
        this.mInterceptor = interceptor;
        reset(true);
        this.mHwActivityStarterEx = HwServiceExFactory.getHwActivityStarterEx(service);
    }

    /* access modifiers changed from: package-private */
    public void set(ActivityStarter starter) {
        this.mStartActivity = starter.mStartActivity;
        this.mIntent = starter.mIntent;
        this.mCallingUid = starter.mCallingUid;
        this.mOptions = starter.mOptions;
        this.mLaunchTaskBehind = starter.mLaunchTaskBehind;
        this.mLaunchFlags = starter.mLaunchFlags;
        this.mLaunchMode = starter.mLaunchMode;
        this.mLaunchParams.set(starter.mLaunchParams);
        this.mNotTop = starter.mNotTop;
        this.mDoResume = starter.mDoResume;
        this.mStartFlags = starter.mStartFlags;
        this.mSourceRecord = starter.mSourceRecord;
        this.mPreferredDisplayId = starter.mPreferredDisplayId;
        this.mInTask = starter.mInTask;
        this.mAddingToTask = starter.mAddingToTask;
        this.mReuseTask = starter.mReuseTask;
        this.mNewTaskInfo = starter.mNewTaskInfo;
        this.mNewTaskIntent = starter.mNewTaskIntent;
        this.mSourceStack = starter.mSourceStack;
        this.mTargetStack = starter.mTargetStack;
        this.mMovedToFront = starter.mMovedToFront;
        this.mNoAnimation = starter.mNoAnimation;
        this.mKeepCurTransition = starter.mKeepCurTransition;
        this.mAvoidMoveToFront = starter.mAvoidMoveToFront;
        this.mVoiceSession = starter.mVoiceSession;
        this.mVoiceInteractor = starter.mVoiceInteractor;
        this.mIntentDelivered = starter.mIntentDelivered;
        this.mRequest.set(starter.mRequest);
    }

    /* access modifiers changed from: package-private */
    public ActivityRecord getStartActivity() {
        return this.mStartActivity;
    }

    /* access modifiers changed from: package-private */
    public boolean relatedToPackage(String packageName) {
        if ((this.mLastStartActivityRecord[0] == null || !packageName.equals(this.mLastStartActivityRecord[0].packageName)) && (this.mStartActivity == null || !packageName.equals(this.mStartActivity.packageName))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int execute() {
        try {
            if (this.mRequest.mayWait) {
                IApplicationThread iApplicationThread = this.mRequest.caller;
                int i = this.mRequest.callingUid;
                String str = this.mRequest.callingPackage;
                Intent intent = this.mRequest.intent;
                String str2 = this.mRequest.resolvedType;
                IVoiceInteractionSession iVoiceInteractionSession = this.mRequest.voiceSession;
                IVoiceInteractor iVoiceInteractor = this.mRequest.voiceInteractor;
                IBinder iBinder = this.mRequest.resultTo;
                String str3 = this.mRequest.resultWho;
                int i2 = this.mRequest.requestCode;
                int i3 = this.mRequest.startFlags;
                ProfilerInfo profilerInfo = this.mRequest.profilerInfo;
                WaitResult waitResult = this.mRequest.waitResult;
                Configuration configuration = this.mRequest.globalConfig;
                SafeActivityOptions safeActivityOptions = this.mRequest.activityOptions;
                boolean z = this.mRequest.ignoreTargetSecurity;
                int i4 = this.mRequest.userId;
                TaskRecord taskRecord = this.mRequest.inTask;
                String str4 = this.mRequest.reason;
                boolean z2 = this.mRequest.allowPendingRemoteAnimationRegistryLookup;
                return startActivityMayWait(iApplicationThread, i, str, intent, str2, iVoiceInteractionSession, iVoiceInteractor, iBinder, str3, i2, i3, profilerInfo, waitResult, configuration, safeActivityOptions, z, i4, taskRecord, str4, z2);
            }
            IApplicationThread iApplicationThread2 = this.mRequest.caller;
            Intent intent2 = this.mRequest.intent;
            Intent intent3 = this.mRequest.ephemeralIntent;
            String str5 = this.mRequest.resolvedType;
            ActivityInfo activityInfo = this.mRequest.activityInfo;
            ResolveInfo resolveInfo = this.mRequest.resolveInfo;
            IVoiceInteractionSession iVoiceInteractionSession2 = this.mRequest.voiceSession;
            IVoiceInteractor iVoiceInteractor2 = this.mRequest.voiceInteractor;
            IBinder iBinder2 = this.mRequest.resultTo;
            String str6 = this.mRequest.resultWho;
            int i5 = this.mRequest.requestCode;
            int i6 = this.mRequest.callingPid;
            int i7 = this.mRequest.callingUid;
            String str7 = this.mRequest.callingPackage;
            int i8 = this.mRequest.realCallingPid;
            int i9 = this.mRequest.realCallingUid;
            int i10 = this.mRequest.startFlags;
            SafeActivityOptions safeActivityOptions2 = this.mRequest.activityOptions;
            boolean z3 = this.mRequest.ignoreTargetSecurity;
            boolean z4 = this.mRequest.componentSpecified;
            ActivityRecord[] activityRecordArr = this.mRequest.outActivity;
            TaskRecord taskRecord2 = this.mRequest.inTask;
            String str8 = this.mRequest.reason;
            boolean z5 = this.mRequest.allowPendingRemoteAnimationRegistryLookup;
            int startActivity = startActivity(iApplicationThread2, intent2, intent3, str5, activityInfo, resolveInfo, iVoiceInteractionSession2, iVoiceInteractor2, iBinder2, str6, i5, i6, i7, str7, i8, i9, i10, safeActivityOptions2, z3, z4, activityRecordArr, taskRecord2, str8, z5);
            onExecutionComplete();
            return startActivity;
        } finally {
            onExecutionComplete();
        }
    }

    /* access modifiers changed from: package-private */
    public int startResolvedActivity(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity) {
        try {
            return startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags, doResume, options, inTask, outActivity);
        } finally {
            onExecutionComplete();
        }
    }

    /* access modifiers changed from: package-private */
    public int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, SafeActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, TaskRecord inTask, String reason, boolean allowPendingRemoteAnimationRegistryLookup) {
        if (!TextUtils.isEmpty(reason)) {
            this.mLastStartReason = reason;
            this.mLastStartActivityTimeMs = System.currentTimeMillis();
            this.mLastStartActivityRecord[0] = null;
            this.mLastStartActivityResult = startActivity(caller, intent, ephemeralIntent, resolvedType, aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, this.mLastStartActivityRecord, inTask, allowPendingRemoteAnimationRegistryLookup);
            if (outActivity != null) {
                outActivity[0] = this.mLastStartActivityRecord[0];
            }
            return getExternalResult(this.mLastStartActivityResult);
        }
        throw new IllegalArgumentException("Need to specify a reason.");
    }

    static int getExternalResult(int result) {
        if (result != 102) {
            return result;
        }
        return 0;
    }

    private void onExecutionComplete() {
        this.mController.onExecutionComplete(this);
    }

    /* JADX WARNING: Removed duplicated region for block: B:194:0x04a1  */
    /* JADX WARNING: Removed duplicated region for block: B:195:0x04c5  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x04cf A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x04ea  */
    /* JADX WARNING: Removed duplicated region for block: B:233:0x065b  */
    /* JADX WARNING: Removed duplicated region for block: B:234:0x065e  */
    /* JADX WARNING: Removed duplicated region for block: B:237:0x069c  */
    /* JADX WARNING: Removed duplicated region for block: B:239:0x06b6  */
    private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, SafeActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, TaskRecord inTask, boolean allowPendingRemoteAnimationRegistryLookup) {
        int callingPid2;
        Bundle verificationBundle;
        boolean z;
        int callingUid2;
        ProcessRecord callerApp;
        int i;
        String str;
        ActivityRecord resultRecord;
        ActivityRecord sourceRecord;
        int requestCode2;
        String resultWho2;
        ActivityRecord resultRecord2;
        String callingPackage2;
        ProcessRecord callerApp2;
        ActivityOptions checkedOptions;
        ProcessRecord callerApp3;
        ActivityOptions checkedOptions2;
        int callingUid3;
        int i2;
        int i3;
        int callingUid4;
        TaskRecord inTask2;
        int callingUid5;
        int callingPid3;
        ResolveInfo rInfo2;
        String callingPackage3;
        ActivityInfo aInfo2;
        String resolvedType2;
        int callingPid4;
        int userId;
        Intent intent2;
        int callingPid5;
        ResolveInfo rInfo3;
        String callingPackage4;
        int i4;
        int callingUid6;
        Intent intent3;
        ActivityInfo aInfo3;
        ActivityRecord r;
        ResolveInfo rInfo4;
        int callingPid6;
        String callingPackage5;
        String startActivityInfo;
        long restoreCurId;
        int callingPid7;
        ProcessRecord callerApp4;
        IApplicationThread iApplicationThread = caller;
        Intent intent4 = intent;
        String resolvedType3 = resolvedType;
        ActivityInfo aInfo4 = aInfo;
        IBinder iBinder = resultTo;
        int i5 = realCallingUid;
        int i6 = startFlags;
        int err = 0;
        Bundle verificationBundle2 = options != null ? options.popAppVerificationBundle() : null;
        if (aInfo4 != null) {
            this.mService.mHwAMSEx.noteActivityStart(aInfo4.applicationInfo.packageName, aInfo4.processName, intent.getComponent() != null ? intent.getComponent().getClassName() : BluetoothManagerService.DEFAULT_PACKAGE_NAME, 0, aInfo4.applicationInfo.uid, true);
        }
        if (aInfo4 != null && !this.mHwActivityStarterEx.isAbleToLaunchInVR(this.mService.mContext, aInfo4.packageName)) {
            return 0;
        }
        if (iApplicationThread != null) {
            ProcessRecord callerApp5 = this.mService.getRecordForAppLocked(iApplicationThread);
            if (callerApp5 != null) {
                if (intent4.hasCategory("android.intent.category.HOME")) {
                    this.mService.checkIfScreenStatusRequestAndSendBroadcast();
                }
                int callingPid8 = callerApp5.pid;
                verificationBundle = verificationBundle2;
                int callingUid7 = callerApp5.info.uid;
                long restoreCurId2 = Binder.clearCallingIdentity();
                try {
                    int callingUid8 = callingUid7;
                    try {
                        int callingPid9 = callingPid8;
                        try {
                            callingPid7 = callingPid9;
                            callerApp4 = callerApp5;
                            z = false;
                            i = i6;
                        } catch (Throwable th) {
                            th = th;
                            ProcessRecord processRecord = callerApp5;
                            int i7 = i6;
                            restoreCurId = restoreCurId2;
                            int i8 = callingPid9;
                            Binder.restoreCallingIdentity(restoreCurId);
                            throw th;
                        }
                        try {
                            if (!this.mService.mHwAMSEx.isAllowToStartActivity(this.mService.mContext, callerApp5.info.packageName, aInfo4, this.mService.isSleepingLocked(), ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).getLastResumedActivity())) {
                                Binder.restoreCallingIdentity(restoreCurId2);
                                return 0;
                            }
                            Binder.restoreCallingIdentity(restoreCurId2);
                            callingPid2 = callingPid7;
                            callerApp = callerApp4;
                            callingUid2 = callingUid8;
                        } catch (Throwable th2) {
                            th = th2;
                            restoreCurId = restoreCurId2;
                            Binder.restoreCallingIdentity(restoreCurId);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        int i9 = callingPid8;
                        ProcessRecord processRecord2 = callerApp5;
                        int i10 = i6;
                        restoreCurId = restoreCurId2;
                        Binder.restoreCallingIdentity(restoreCurId);
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    int i11 = callingUid7;
                    int i12 = callingPid8;
                    ProcessRecord processRecord3 = callerApp5;
                    int callingUid9 = i6;
                    restoreCurId = restoreCurId2;
                    Binder.restoreCallingIdentity(restoreCurId);
                    throw th;
                }
            } else {
                verificationBundle = verificationBundle2;
                ProcessRecord callerApp6 = callerApp5;
                i = i6;
                z = false;
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to find app for caller ");
                sb.append(iApplicationThread);
                sb.append(" (pid=");
                int i13 = callingPid;
                sb.append(i13);
                sb.append(") when starting: ");
                sb.append(intent.toString());
                Slog.w(ActivityManagerService.TAG, sb.toString());
                err = -94;
                callingUid2 = callingUid;
                callingPid2 = i13;
                callerApp = callerApp6;
            }
        } else {
            verificationBundle = verificationBundle2;
            i = i6;
            z = false;
            callingUid2 = callingUid;
            callerApp = null;
            callingPid2 = callingPid;
        }
        HwFreeFormManager.getInstance(this.mService.mContext).removeFloatListView();
        int userId2 = (aInfo4 == null || aInfo4.applicationInfo == null) ? z : UserHandle.getUserId(aInfo4.applicationInfo.uid);
        if ((!mLauncherStartState.containsKey(Integer.valueOf(userId2)) || !mLauncherStartState.get(Integer.valueOf(userId2)).booleanValue()) && this.mService.isStartLauncherActivity(intent4, userId2)) {
            Slog.w(ActivityManagerService.TAG, "check the USER_SETUP_COMPLETE is set 1 in first start launcher!");
            this.mService.forceValidateHomeButton(userId2);
            mLauncherStartState.put(Integer.valueOf(userId2), true);
        }
        if (err == 0) {
            Slog.i(ActivityManagerService.TAG, "START u" + userId2 + " {" + intent4.toShortStringWithoutClip(true, true, true) + "} from uid " + callingUid2);
            if (!this.mService.mActivityIdle) {
                if (!ActivityManagerService.IS_DEBUG_VERSION || intent.getComponent() == null) {
                    startActivityInfo = "start activity";
                } else {
                    startActivityInfo = "START u" + userId2 + " " + intent.getComponent().toShortString() + " from uid " + callingUid2;
                }
                HwBootCheck.addBootInfo(startActivityInfo);
            }
            this.mSupervisor.recognitionMaliciousApp(iApplicationThread, intent4, userId2);
            ComponentName cmp = intent.getComponent();
            if (cmp != null) {
                String strPkg = cmp.getPackageName();
            }
            if (!ACTION_HWCHOOSER.equals(intent.getAction()) || !intent4.getBooleanExtra(EXTRA_ALWAYS_USE_OPTION, z)) {
                str = callingPackage;
            } else {
                str = callingPackage;
                if (!HWPCEXPLORER_PACKAGE_NAME.equals(str)) {
                    intent4.putExtra(EXTRA_ALWAYS_USE_OPTION, z);
                }
            }
            this.mHwActivityStarterEx.effectiveIawareToLaunchApp(intent4, aInfo4, this.mService.getActivityStartController().mCurActivityPkName);
        } else {
            str = callingPackage;
        }
        if (iBinder != null) {
            ActivityRecord sourceRecord2 = this.mSupervisor.isInAnyStackLocked(iBinder);
            if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                StringBuilder sb2 = new StringBuilder();
                resultRecord = null;
                sb2.append("Will send result to ");
                sb2.append(iBinder);
                sb2.append(" ");
                sb2.append(sourceRecord2);
                Slog.v(ActivityManagerService.TAG, sb2.toString());
            } else {
                resultRecord = null;
            }
            if (sourceRecord2 == null || requestCode < 0 || sourceRecord2.finishing) {
                sourceRecord = sourceRecord2;
            } else {
                sourceRecord = sourceRecord2;
                resultRecord = sourceRecord2;
            }
        } else {
            resultRecord = null;
            sourceRecord = null;
        }
        int launchFlags = intent.getFlags();
        if ((launchFlags & DumpState.DUMP_HANDLE) == 0 || sourceRecord == null) {
            resultWho2 = resultWho;
            requestCode2 = requestCode;
            callingPackage2 = str;
            resultRecord2 = resultRecord;
        } else if (requestCode >= 0) {
            SafeActivityOptions.abort(options);
            return -93;
        } else {
            ActivityRecord resultRecord3 = sourceRecord.resultTo;
            if (resultRecord3 != null && !resultRecord3.isInStackLocked()) {
                resultRecord3 = null;
            }
            String resultWho3 = sourceRecord.resultWho;
            int requestCode3 = sourceRecord.requestCode;
            sourceRecord.resultTo = null;
            if (resultRecord3 != null) {
                resultRecord3.removeResultsLocked(sourceRecord, resultWho3, requestCode3);
            }
            if (sourceRecord.launchedFromUid == callingUid2) {
                resultRecord2 = resultRecord3;
                requestCode2 = requestCode3;
                resultWho2 = resultWho3;
                callingPackage2 = sourceRecord.launchedFromPackage;
            } else {
                requestCode2 = requestCode3;
                resultWho2 = resultWho3;
                callingPackage2 = str;
                resultRecord2 = resultRecord3;
            }
        }
        if (err == 0 && intent.getComponent() == null) {
            err = -91;
        }
        if (err == 0 && aInfo4 == null) {
            err = -92;
        }
        if (!(err != 0 || sourceRecord == null || sourceRecord.getTask().voiceSession == null || (launchFlags & 268435456) != 0 || sourceRecord.info.applicationInfo.uid == aInfo4.applicationInfo.uid)) {
            try {
                intent4.addCategory("android.intent.category.VOICE");
                if (!this.mService.getPackageManager().activitySupportsIntent(intent.getComponent(), intent4, resolvedType3)) {
                    Slog.w(ActivityManagerService.TAG, "Activity being started in current voice task does not support voice: " + intent4);
                    err = -97;
                }
            } catch (RemoteException e) {
                Slog.w(ActivityManagerService.TAG, "Failure checking voice capabilities", e);
                err = -97;
            }
        }
        if (err == 0 && voiceSession != null) {
            try {
                if (!this.mService.getPackageManager().activitySupportsIntent(intent.getComponent(), intent4, resolvedType3)) {
                    Slog.w(ActivityManagerService.TAG, "Activity being started in new voice task does not support: " + intent4);
                    err = -97;
                }
            } catch (RemoteException e2) {
                Slog.w(ActivityManagerService.TAG, "Failure checking voice capabilities", e2);
                err = -97;
            }
        }
        int err2 = err;
        ActivityStack resultStack = resultRecord2 == null ? null : resultRecord2.getStack();
        if (err2 != 0) {
            if (resultRecord2 != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord2, resultWho2, requestCode2, 0, null);
            }
            SafeActivityOptions.abort(options);
            return err2;
        }
        ActivityRecord resultRecord4 = resultRecord2;
        int userId3 = userId2;
        ProcessRecord callerApp7 = callerApp;
        int callingUid10 = callingUid2;
        boolean abort = (!this.mSupervisor.checkStartAnyActivityPermission(intent4, aInfo4, resultWho2, requestCode2, callingPid2, callingUid2, callingPackage2, ignoreTargetSecurity, inTask != null, callerApp, resultRecord2, resultStack)) | (!this.mService.mIntentFirewall.checkStartActivity(intent4, callingUid2, callingPid2, resolvedType3, aInfo4.applicationInfo));
        SafeActivityOptions safeActivityOptions = options;
        if (safeActivityOptions != null) {
            callerApp2 = callerApp7;
            checkedOptions = safeActivityOptions.getOptions(intent4, aInfo4, callerApp2, this.mSupervisor);
        } else {
            callerApp2 = callerApp7;
            checkedOptions = null;
        }
        if (!abort) {
            checkedOptions2 = checkedOptions;
            callerApp3 = callerApp2;
            if (this.mService.mHwAMSEx.shouldPreventStartActivity(aInfo4, callingUid10, callingPid2, callingPackage2, userId3)) {
                abort = true;
            }
        } else {
            checkedOptions2 = checkedOptions;
            callerApp3 = callerApp2;
        }
        boolean abort2 = abort;
        if (allowPendingRemoteAnimationRegistryLookup) {
            checkedOptions2 = this.mService.getActivityStartController().getPendingRemoteAnimationRegistry().overrideOptionsIfNeeded(callingPackage2, checkedOptions2);
        }
        if (this.mService.mController != null) {
            try {
                abort2 |= !this.mService.mController.activityStarting(intent.cloneFilter(), aInfo4.applicationInfo.packageName);
            } catch (RemoteException e3) {
                this.mService.mController = null;
            }
        }
        boolean abort3 = startingCustomActivity(abort2, intent4, aInfo4) | abort2;
        if ("startActivityAsCaller".equals(this.mLastStartReason)) {
            callingUid3 = callingUid10;
            intent4.setCallingUid(callingUid3);
        } else {
            callingUid3 = callingUid10;
            if (!"PendingIntentRecord".equals(this.mLastStartReason) || intent.getCallingUid() == 0) {
                i3 = 0;
                i2 = realCallingUid;
                intent4.setCallingUid(i2);
                int i14 = i3;
                callingUid4 = callingUid3;
                this.mInterceptor.setStates(userId3, realCallingPid, i2, i, callingPackage2);
                this.mInterceptor.setSourceRecord(sourceRecord);
                if (!this.mInterceptor.intercept(intent4, rInfo, aInfo4, resolvedType3, inTask, callingPid2, callingUid4, checkedOptions2)) {
                    intent4 = this.mInterceptor.mIntent;
                    rInfo2 = this.mInterceptor.mRInfo;
                    aInfo4 = this.mInterceptor.mAInfo;
                    resolvedType3 = this.mInterceptor.mResolvedType;
                    TaskRecord inTask3 = this.mInterceptor.mInTask;
                    callingPid3 = this.mInterceptor.mCallingPid;
                    callingUid5 = this.mInterceptor.mCallingUid;
                    inTask2 = inTask3;
                    checkedOptions2 = this.mInterceptor.mActivityOptions;
                } else {
                    rInfo2 = rInfo;
                    inTask2 = inTask;
                    callingUid5 = callingUid4;
                    callingPid3 = callingPid2;
                }
                if (!abort3) {
                    if (!(resultRecord4 == null || resultStack == null)) {
                        resultStack.sendActivityResultLocked(-1, resultRecord4, resultWho2, requestCode2, 0, null);
                    }
                    ActivityOptions.abort(checkedOptions2);
                    return 102;
                } else if (!this.mHwActivityStarterEx.isAbleToLaunchVideoActivity(this.mService.mContext, intent4)) {
                    SafeActivityOptions.abort(options);
                    return i14;
                } else {
                    if (!this.mService.mPermissionReviewRequired || aInfo4 == null) {
                        callingPackage5 = callingPackage2;
                        rInfo4 = rInfo2;
                        callingPid6 = callingPid3;
                        userId = userId3;
                        callingPid5 = realCallingUid;
                    } else {
                        userId = userId3;
                        if (this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(aInfo4.packageName, userId)) {
                            ActivityManagerService activityManagerService = this.mService;
                            boolean z2 = abort3;
                            Intent[] intentArr = new Intent[1];
                            intentArr[i14] = intent4;
                            ResolveInfo resolveInfo = rInfo2;
                            String[] strArr = new String[1];
                            strArr[i14] = resolvedType3;
                            IIntentSender target = activityManagerService.getIntentSenderLocked(2, callingPackage2, callingUid5, userId, null, null, 0, intentArr, strArr, 1342177280, null);
                            int flags = intent4.getFlags();
                            Intent newIntent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                            newIntent.setFlags(8388608 | flags);
                            newIntent.putExtra("android.intent.extra.PACKAGE_NAME", aInfo4.packageName);
                            newIntent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
                            if (resultRecord4 != null) {
                                newIntent.putExtra("android.intent.extra.RESULT_NEEDED", true);
                            }
                            Intent intent5 = newIntent;
                            callingUid5 = realCallingUid;
                            callingPid4 = realCallingPid;
                            callingPid5 = realCallingUid;
                            ResolveInfo rInfo5 = this.mSupervisor.resolveIntent(intent5, null, userId, 0, computeResolveFilterUid(callingUid5, callingPid5, this.mRequest.filterCallingUid));
                            resolvedType2 = null;
                            ActivityInfo aInfo5 = this.mSupervisor.resolveActivity(intent5, rInfo5, i, null);
                            if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                                StringBuilder sb3 = new StringBuilder();
                                IIntentSender iIntentSender = target;
                                sb3.append("START u");
                                sb3.append(userId);
                                sb3.append(" {");
                                aInfo2 = aInfo5;
                                callingPackage3 = callingPackage2;
                                sb3.append(intent5.toShortString(true, true, true, false));
                                sb3.append("} from uid ");
                                sb3.append(callingUid5);
                                sb3.append(" on display ");
                                sb3.append(this.mSupervisor.mFocusedStack == null ? 0 : this.mSupervisor.mFocusedStack.mDisplayId);
                                Slog.i(ActivityManagerService.TAG, sb3.toString());
                            } else {
                                aInfo2 = aInfo5;
                                callingPackage3 = callingPackage2;
                            }
                            rInfo3 = rInfo5;
                            intent2 = intent5;
                            if (rInfo3 != null || rInfo3.auxiliaryInfo == null) {
                                i4 = i;
                                Bundle bundle = verificationBundle;
                                callingPackage4 = callingPackage3;
                                intent3 = intent2;
                                callingUid6 = callingUid5;
                                aInfo3 = aInfo2;
                            } else {
                                callingPackage4 = callingPackage3;
                                i4 = i;
                                Intent intent6 = createLaunchIntent(rInfo3.auxiliaryInfo, ephemeralIntent, callingPackage4, verificationBundle, resolvedType2, userId);
                                callingUid6 = callingPid5;
                                resolvedType2 = null;
                                callingPid4 = realCallingPid;
                                intent3 = intent6;
                                aInfo3 = this.mSupervisor.resolveActivity(intent6, rInfo3, i4, null);
                            }
                            Slog.i(ActivityManagerService.TAG, "ActivityRecord info: " + aInfo3);
                            r = HwServiceFactory.createActivityRecord(this.mService, callerApp3, callingPid4, callingUid6, callingPackage4, intent3, resolvedType2, aInfo3, this.mService.getGlobalConfiguration(), resultRecord4, resultWho2, requestCode2, componentSpecified, voiceSession == null, this.mSupervisor, checkedOptions2, sourceRecord);
                            if (!this.mService.shouldPreventActivity(intent3, aInfo3, r, callingPid4, callingUid6, callerApp3)) {
                                Slog.w(ActivityManagerService.TAG, "forbiden launch for activity: " + r);
                                SafeActivityOptions.abort(options);
                                return 100;
                            }
                            if (outActivity != null) {
                                outActivity[0] = r;
                            }
                            if (r.appTimeTracker == null && sourceRecord != null) {
                                r.appTimeTracker = sourceRecord.appTimeTracker;
                            }
                            ActivityStack stack = this.mSupervisor.mFocusedStack;
                            if (this.mSupervisor.getResumedActivityLocked() == null || !INCALLUI_ACTIVITY_CLASS_NAME.equals(this.mSupervisor.getResumedActivityLocked().shortComponentName) || voiceSession != null || stack.mResumedActivity != null || r.shortComponentName == null || r.shortComponentName.equals(this.mSupervisor.getResumedActivityLocked().shortComponentName) || !this.mService.isStartLauncherActivity(intent3, userId) || ((stack.mLastPausedActivity != null && INCALLUI_ACTIVITY_CLASS_NAME.equals(stack.mLastPausedActivity.shortComponentName)) || this.mService.isSleepingLocked())) {
                                if (voiceSession != null) {
                                    ActivityInfo activityInfo = aInfo3;
                                    int i15 = userId;
                                    ActivityStack activityStack = stack;
                                } else if (stack.getResumedActivity() == null || stack.getResumedActivity().info.applicationInfo.uid != callingPid5) {
                                    int i16 = userId;
                                    ResolveInfo resolveInfo2 = rInfo3;
                                    ActivityStack stack2 = stack;
                                    if (!this.mService.checkAppSwitchAllowedLocked(callingPid4, callingUid6, realCallingPid, realCallingUid, "Activity start")) {
                                        if (launchFlags != 0 || !stack2.isActivityTypeHome()) {
                                            Slog.d(ActivityManagerService.TAG, "allow pending activitys to launch");
                                            ActivityInfo activityInfo2 = aInfo3;
                                            ActivityStackSupervisor.PendingActivityLaunch pendingActivityLaunch = r6;
                                            ActivityStack activityStack2 = stack2;
                                            ActivityStack activityStack3 = stack2;
                                            ActivityStartController activityStartController = this.mController;
                                            ActivityStackSupervisor.PendingActivityLaunch pendingActivityLaunch2 = new ActivityStackSupervisor.PendingActivityLaunch(r, sourceRecord, i4, activityStack2, callerApp3);
                                            activityStartController.addPendingActivityLaunch(pendingActivityLaunch);
                                        } else {
                                            Slog.d(ActivityManagerService.TAG, "not allow pending activitys to launch");
                                            ActivityStack activityStack4 = stack2;
                                            ActivityInfo activityInfo3 = aInfo3;
                                        }
                                        ActivityOptions.abort(checkedOptions2);
                                        return 100;
                                    }
                                    ActivityInfo activityInfo4 = aInfo3;
                                } else {
                                    ResolveInfo resolveInfo3 = rInfo3;
                                    ActivityInfo activityInfo5 = aInfo3;
                                    int i17 = userId;
                                    ActivityStack activityStack5 = stack;
                                }
                                if (this.mService.mDidAppSwitch) {
                                    this.mService.mAppSwitchesAllowedTime = 0;
                                } else {
                                    this.mService.mDidAppSwitch = true;
                                }
                                this.mController.doPendingActivityLaunches(false);
                                ActivityRecord activityRecord = sourceRecord;
                                ActivityOptions activityOptions = checkedOptions2;
                                int err3 = startActivity(r, sourceRecord, voiceSession, voiceInteractor, i4, true, checkedOptions2, inTask2, outActivity);
                                if (Jlog.isUBMEnable() && err3 >= 0 && intent3.getComponent() != null) {
                                    Jlog.d(272, "AL#" + intent3.getComponent().flattenToShortString() + "(" + intent3.getAction() + "," + intent3.getCategories() + ")");
                                }
                                return err3;
                            }
                            Slog.d(ActivityManagerService.TAG, "abort launch for activity: " + r);
                            SafeActivityOptions.abort(options);
                            return 100;
                        }
                        callingPackage5 = callingPackage2;
                        rInfo4 = rInfo2;
                        callingPid6 = callingPid3;
                        callingPid5 = realCallingUid;
                    }
                    intent2 = intent4;
                    resolvedType2 = resolvedType3;
                    aInfo2 = aInfo4;
                    callingPid4 = callingPid6;
                    rInfo3 = rInfo4;
                    if (rInfo3 != null) {
                    }
                    i4 = i;
                    Bundle bundle2 = verificationBundle;
                    callingPackage4 = callingPackage3;
                    intent3 = intent2;
                    callingUid6 = callingUid5;
                    aInfo3 = aInfo2;
                    Slog.i(ActivityManagerService.TAG, "ActivityRecord info: " + aInfo3);
                    r = HwServiceFactory.createActivityRecord(this.mService, callerApp3, callingPid4, callingUid6, callingPackage4, intent3, resolvedType2, aInfo3, this.mService.getGlobalConfiguration(), resultRecord4, resultWho2, requestCode2, componentSpecified, voiceSession == null, this.mSupervisor, checkedOptions2, sourceRecord);
                    if (!this.mService.shouldPreventActivity(intent3, aInfo3, r, callingPid4, callingUid6, callerApp3)) {
                    }
                }
            }
        }
        i3 = 0;
        i2 = realCallingUid;
        int i142 = i3;
        callingUid4 = callingUid3;
        this.mInterceptor.setStates(userId3, realCallingPid, i2, i, callingPackage2);
        this.mInterceptor.setSourceRecord(sourceRecord);
        if (!this.mInterceptor.intercept(intent4, rInfo, aInfo4, resolvedType3, inTask, callingPid2, callingUid4, checkedOptions2)) {
        }
        if (!abort3) {
        }
    }

    private Intent createLaunchIntent(AuxiliaryResolveInfo auxiliaryResponse, Intent originalIntent, String callingPackage, Bundle verificationBundle, String resolvedType, int userId) {
        Intent intent;
        ComponentName componentName;
        AuxiliaryResolveInfo auxiliaryResolveInfo = auxiliaryResponse;
        if (auxiliaryResolveInfo != null && auxiliaryResolveInfo.needsPhaseTwo) {
            this.mService.getPackageManagerInternalLocked().requestInstantAppResolutionPhaseTwo(auxiliaryResolveInfo, originalIntent, resolvedType, callingPackage, verificationBundle, userId);
        }
        Intent sanitizeIntent = InstantAppResolver.sanitizeIntent(originalIntent);
        List list = null;
        if (auxiliaryResolveInfo == null) {
            intent = null;
        } else {
            intent = auxiliaryResolveInfo.failureIntent;
        }
        if (auxiliaryResolveInfo == null) {
            componentName = null;
        } else {
            componentName = auxiliaryResolveInfo.installFailureActivity;
        }
        String str = auxiliaryResolveInfo == null ? null : auxiliaryResolveInfo.token;
        boolean z = auxiliaryResolveInfo != null && auxiliaryResolveInfo.needsPhaseTwo;
        if (auxiliaryResolveInfo != null) {
            list = auxiliaryResolveInfo.filters;
        }
        return InstantAppResolver.buildEphemeralInstallerIntent(originalIntent, sanitizeIntent, intent, callingPackage, verificationBundle, resolvedType, userId, componentName, str, z, list);
    }

    /* access modifiers changed from: package-private */
    public void postStartActivityProcessing(ActivityRecord r, int result, ActivityStack targetStack) {
        if (!ActivityManager.isStartResultFatalError(result)) {
            this.mSupervisor.reportWaitingActivityLaunchedIfNeeded(r, result);
            ActivityStack startedActivityStack = null;
            ActivityStack currentStack = r.getStack();
            if (currentStack != null) {
                startedActivityStack = currentStack;
            } else if (this.mTargetStack != null) {
                startedActivityStack = targetStack;
            }
            if (startedActivityStack != null) {
                boolean clearedTask = (this.mLaunchFlags & 268468224) == 268468224 && this.mReuseTask != null;
                if (result == 2 || result == 3 || clearedTask) {
                    switch (startedActivityStack.getWindowingMode()) {
                        case 2:
                            this.mService.mTaskChangeNotificationController.notifyPinnedActivityRestartAttempt(clearedTask);
                            break;
                        case 3:
                            ActivityStack homeStack = this.mSupervisor.mHomeStack;
                            if (homeStack != null && homeStack.shouldBeVisible(null)) {
                                this.mService.mWindowManager.showRecentApps();
                                break;
                            }
                    }
                }
            }
        }
    }

    /* JADX WARNING: type inference failed for: r7v8, types: [boolean] */
    /* JADX WARNING: type inference failed for: r7v11 */
    /* JADX WARNING: type inference failed for: r7v14 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:105:0x024b A[SYNTHETIC, Splitter:B:105:0x024b] */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x056c  */
    /* JADX WARNING: Removed duplicated region for block: B:235:0x0593  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x011d  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x0183  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x018c  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x01f2 A[SYNTHETIC, Splitter:B:83:0x01f2] */
    /* JADX WARNING: Removed duplicated region for block: B:99:0x022a A[SYNTHETIC, Splitter:B:99:0x022a] */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:291:0x0683=Splitter:B:291:0x0683, B:281:0x066c=Splitter:B:281:0x066c} */
    public int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration globalConfig, SafeActivityOptions options, boolean ignoreTargetSecurity, int userId, TaskRecord inTask, String reason, boolean allowPendingRemoteAnimationRegistryLookup) {
        int callingUid2;
        int callingPid;
        int realCallingUid;
        int callingPid2;
        Intent intent2;
        ResolveInfo rInfo;
        ActivityInfo aInfo;
        ActivityManagerService activityManagerService;
        ActivityManagerService activityManagerService2;
        boolean z;
        ActivityStack stack;
        long origId;
        int callingUid3;
        ResolveInfo rInfo2;
        boolean componentSpecified;
        int callingPid3;
        ActivityInfo aInfo2;
        String resolvedType2;
        int realCallingUid2;
        IApplicationThread caller2;
        ? r7;
        ActivityStarter activityStarter;
        ActivityInfo aInfo3;
        int callingPid4;
        ActivityManagerService activityManagerService3;
        long origId2;
        int appCallingUid;
        int callingPid5;
        ActivityInfo activityInfo;
        ActivityManagerService activityManagerService4;
        Intent newIntent;
        Intent intent3;
        int callingUid4;
        int callingPid6;
        ActivityInfo aInfo4;
        int realCallingUid3;
        ActivityInfo activityInfo2;
        ResolveInfo rInfo3;
        long token;
        boolean z2;
        boolean profileLockedAndParentUnlockingOrUnlocked;
        int callingUid5;
        IApplicationThread iApplicationThread = caller;
        String str = callingPackage;
        Intent intent4 = intent;
        WaitResult waitResult = outResult;
        Configuration configuration = globalConfig;
        int callingUid6 = userId;
        if (intent4 == null || !intent.hasFileDescriptors()) {
            this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunching();
            boolean componentSpecified2 = intent.getComponent() != null;
            int realCallingPid = Binder.getCallingPid();
            int realCallingUid4 = Binder.getCallingUid();
            if (callingUid >= 0) {
                callingPid = -1;
                callingUid2 = callingUid;
            } else {
                if (iApplicationThread == null) {
                    callingPid = realCallingPid;
                    callingUid5 = realCallingUid4;
                } else {
                    callingPid = -1;
                    callingUid5 = -1;
                }
                callingUid2 = callingUid5;
            }
            int callingPid7 = callingPid;
            Intent ephemeralIntent = new Intent(intent4);
            Intent intent5 = new Intent(intent4);
            if (componentSpecified2 && ((!"android.intent.action.VIEW".equals(intent5.getAction()) || intent5.getData() != null) && !"android.intent.action.INSTALL_INSTANT_APP_PACKAGE".equals(intent5.getAction()) && !"android.intent.action.RESOLVE_INSTANT_APP_PACKAGE".equals(intent5.getAction()) && this.mService.getPackageManagerInternalLocked().isInstantAppInstallerComponent(intent5.getComponent()))) {
                intent5.setComponent(null);
                componentSpecified2 = false;
            }
            int callingPid8 = callingPid7;
            boolean componentSpecified3 = componentSpecified2;
            int callingUid7 = callingUid2;
            Intent ephemeralIntent2 = ephemeralIntent;
            int realCallingUid5 = realCallingUid4;
            ResolveInfo rInfo4 = this.mSupervisor.resolveIntent(intent5, resolvedType, callingUid6, 0, computeResolveFilterUid(callingUid2, realCallingUid4, this.mRequest.filterCallingUid));
            Intent intent6 = intent5;
            if (standardizeHomeIntent(rInfo4, intent6)) {
                componentSpecified3 = false;
            }
            if (rInfo4 == null) {
                UserInfo userInfo = this.mSupervisor.getUserInfo(callingUid6);
                if (userInfo != null) {
                    if (userInfo.isManagedProfile() || userInfo.isClonedProfile()) {
                        UserManager userManager = UserManager.get(this.mService.mContext);
                        long token2 = Binder.clearCallingIdentity();
                        try {
                            UserInfo parent = userManager.getProfileParent(callingUid6);
                            if (parent != null) {
                                try {
                                    if (userManager.isUserUnlockingOrUnlocked(parent.id) && !userManager.isUserUnlockingOrUnlocked(callingUid6)) {
                                        z2 = true;
                                        profileLockedAndParentUnlockingOrUnlocked = z2;
                                        long token3 = token2;
                                        Binder.restoreCallingIdentity(token3);
                                        if (profileLockedAndParentUnlockingOrUnlocked) {
                                            boolean z3 = profileLockedAndParentUnlockingOrUnlocked;
                                            callingPid2 = callingPid8;
                                            realCallingUid = realCallingUid5;
                                            long j = token3;
                                            UserInfo userInfo2 = userInfo;
                                            UserManager userManager2 = userManager;
                                            intent2 = intent6;
                                            ResolveInfo resolveInfo = rInfo4;
                                            rInfo = this.mSupervisor.resolveIntent(intent6, resolvedType, callingUid6, 786432, computeResolveFilterUid(callingUid7, realCallingUid5, this.mRequest.filterCallingUid));
                                            int i = startFlags;
                                            aInfo = this.mSupervisor.resolveActivity(intent2, rInfo, i, profilerInfo);
                                            if (!(aInfo == null || aInfo.applicationInfo == null || str == null || !str.equals(aInfo.applicationInfo.packageName))) {
                                                String activityName = intent2.getComponent() != null ? intent2.getComponent().getClassName() : BluetoothManagerService.DEFAULT_PACKAGE_NAME;
                                                Jlog.d(335, aInfo.applicationInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + activityName, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                                            }
                                            if (!(!Jlog.isPerfTest() || aInfo == null || aInfo.applicationInfo == null)) {
                                                Jlog.i(3023, Jlog.getMessage("ActivityStarter", "startActivityMayWait", "whopkg=" + str + "&pkg=" + aInfo.applicationInfo.packageName + "&cls=" + aInfo.name));
                                            }
                                            activityManagerService = this.mService;
                                            synchronized (activityManagerService) {
                                                ActivityManagerService.boostPriorityForLockedSection();
                                                ActivityStack stack2 = this.mSupervisor.mFocusedStack;
                                                if (configuration != null) {
                                                    try {
                                                        if (this.mService.getGlobalConfiguration().diff(configuration) != 0) {
                                                            z = true;
                                                            stack2.mConfigWillChange = z;
                                                            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                                                Slog.v(TAG_CONFIGURATION, "Starting activity when config will change = " + stack2.mConfigWillChange);
                                                            }
                                                            long origId3 = Binder.clearCallingIdentity();
                                                            ActivityStack stack3 = stack2;
                                                            if (aInfo != null) {
                                                                try {
                                                                    if ((aInfo.applicationInfo.privateFlags & 2) != 0 && this.mService.mHasHeavyWeightFeature && aInfo.processName.equals(aInfo.applicationInfo.packageName)) {
                                                                        ProcessRecord heavy = this.mService.mHeavyWeightProcess;
                                                                        if (heavy != null) {
                                                                            rInfo2 = rInfo;
                                                                            try {
                                                                                if (heavy.info.uid == aInfo.applicationInfo.uid) {
                                                                                    try {
                                                                                        if (heavy.processName.equals(aInfo.processName)) {
                                                                                            callingUid3 = callingUid7;
                                                                                            origId = origId3;
                                                                                            activityManagerService3 = activityManagerService;
                                                                                            aInfo3 = aInfo;
                                                                                            callingPid4 = callingPid2;
                                                                                            realCallingUid2 = realCallingUid;
                                                                                            stack = stack3;
                                                                                            resolvedType2 = resolvedType;
                                                                                            callingPid3 = callingPid4;
                                                                                            componentSpecified = componentSpecified3;
                                                                                            caller2 = caller;
                                                                                            aInfo2 = aInfo3;
                                                                                            this.mService.addCallerToIntent(intent2, caller2);
                                                                                            if (!HwDeviceManager.disallowOp(intent2)) {
                                                                                                try {
                                                                                                    Slog.i(ActivityManagerService.TAG, "due to disallow op launching activity aborted");
                                                                                                    UiThread.getHandler().post(new Runnable() {
                                                                                                        public void run() {
                                                                                                            Context context = ActivityStarter.this.mService.mUiContext;
                                                                                                            if (context != null) {
                                                                                                                Toast toast = Toast.makeText(context, context.getString(33686140), 0);
                                                                                                                toast.getWindowParams().privateFlags |= 16;
                                                                                                                toast.show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                                    return -96;
                                                                                                } catch (Throwable th) {
                                                                                                    th = th;
                                                                                                    IApplicationThread iApplicationThread2 = caller2;
                                                                                                    Configuration configuration2 = configuration;
                                                                                                    Intent intent7 = ephemeralIntent2;
                                                                                                    int i2 = callingPid3;
                                                                                                    ResolveInfo resolveInfo2 = rInfo2;
                                                                                                    WaitResult waitResult2 = outResult;
                                                                                                    while (true) {
                                                                                                        try {
                                                                                                            break;
                                                                                                        } catch (Throwable th2) {
                                                                                                            th = th2;
                                                                                                        }
                                                                                                    }
                                                                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                                    throw th;
                                                                                                }
                                                                                            } else {
                                                                                                ActivityRecord[] outRecord = new ActivityRecord[1];
                                                                                                IApplicationThread iApplicationThread3 = caller2;
                                                                                                Intent intent8 = intent2;
                                                                                                try {
                                                                                                    int res = startActivity(caller2, intent2, ephemeralIntent2, resolvedType2, aInfo2, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid3, callingUid3, callingPackage, realCallingPid, realCallingUid2, i, options, ignoreTargetSecurity, componentSpecified, outRecord, inTask, reason, allowPendingRemoteAnimationRegistryLookup);
                                                                                                    Binder.restoreCallingIdentity(origId);
                                                                                                    ActivityStack stack4 = stack;
                                                                                                    if (stack4.mConfigWillChange) {
                                                                                                        activityStarter = this;
                                                                                                        try {
                                                                                                            activityStarter.mService.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
                                                                                                            r7 = 0;
                                                                                                            stack4.mConfigWillChange = false;
                                                                                                            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                                                                                                try {
                                                                                                                    Slog.v(TAG_CONFIGURATION, "Updating to new configuration after starting activity.");
                                                                                                                } catch (Throwable th3) {
                                                                                                                    th = th3;
                                                                                                                    int i3 = callingPid3;
                                                                                                                    ResolveInfo resolveInfo3 = rInfo2;
                                                                                                                    Intent intent9 = intent8;
                                                                                                                    Configuration configuration3 = globalConfig;
                                                                                                                }
                                                                                                            }
                                                                                                            try {
                                                                                                                activityStarter.mService.updateConfigurationLocked(globalConfig, null, false);
                                                                                                            } catch (Throwable th4) {
                                                                                                                th = th4;
                                                                                                            }
                                                                                                        } catch (Throwable th5) {
                                                                                                            th = th5;
                                                                                                            Configuration configuration4 = globalConfig;
                                                                                                            WaitResult waitResult22 = outResult;
                                                                                                            while (true) {
                                                                                                                break;
                                                                                                            }
                                                                                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                                            throw th;
                                                                                                        }
                                                                                                    } else {
                                                                                                        activityStarter = this;
                                                                                                        r7 = 0;
                                                                                                        Configuration configuration5 = globalConfig;
                                                                                                    }
                                                                                                    WaitResult waitResult3 = outResult;
                                                                                                    if (waitResult3 != null) {
                                                                                                        try {
                                                                                                            waitResult3.result = res;
                                                                                                            waitResult3.origin = intent8.getComponent();
                                                                                                            ActivityRecord r = outRecord[r7];
                                                                                                            if (res != 0) {
                                                                                                                switch (res) {
                                                                                                                    case 2:
                                                                                                                        if (r.nowVisible && r.isState(ActivityStack.ActivityState.RESUMED)) {
                                                                                                                            waitResult3.timeout = r7;
                                                                                                                            waitResult3.who = r.realActivity;
                                                                                                                            waitResult3.totalTime = 0;
                                                                                                                            waitResult3.thisTime = 0;
                                                                                                                            break;
                                                                                                                        } else {
                                                                                                                            waitResult3.thisTime = SystemClock.uptimeMillis();
                                                                                                                            activityStarter.mSupervisor.waitActivityVisible(r.realActivity, waitResult3);
                                                                                                                            while (true) {
                                                                                                                                try {
                                                                                                                                    activityStarter.mService.wait();
                                                                                                                                } catch (InterruptedException e) {
                                                                                                                                }
                                                                                                                                if (!waitResult3.timeout) {
                                                                                                                                    if (waitResult3.who != null) {
                                                                                                                                        break;
                                                                                                                                    }
                                                                                                                                } else {
                                                                                                                                    break;
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }
                                                                                                                        break;
                                                                                                                    case 3:
                                                                                                                        waitResult3.timeout = r7;
                                                                                                                        waitResult3.who = r.realActivity;
                                                                                                                        waitResult3.totalTime = 0;
                                                                                                                        waitResult3.thisTime = 0;
                                                                                                                        break;
                                                                                                                }
                                                                                                            } else {
                                                                                                                activityStarter.mSupervisor.mWaitingActivityLaunched.add(waitResult3);
                                                                                                                do {
                                                                                                                    try {
                                                                                                                        activityStarter.mService.wait();
                                                                                                                    } catch (InterruptedException e2) {
                                                                                                                    }
                                                                                                                    if (waitResult3.result == 2 || waitResult3.timeout) {
                                                                                                                    }
                                                                                                                } while (waitResult3.who == null);
                                                                                                                if (waitResult3.result == 2) {
                                                                                                                    res = 2;
                                                                                                                }
                                                                                                            }
                                                                                                        } catch (Throwable th6) {
                                                                                                            th = th6;
                                                                                                            int i4 = callingPid3;
                                                                                                            ResolveInfo resolveInfo4 = rInfo2;
                                                                                                            while (true) {
                                                                                                                break;
                                                                                                            }
                                                                                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                                            throw th;
                                                                                                        }
                                                                                                    } else {
                                                                                                        Intent intent10 = intent8;
                                                                                                    }
                                                                                                    activityStarter.mSupervisor.getActivityMetricsLogger().notifyActivityLaunched(res, outRecord[r7]);
                                                                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                                    return res;
                                                                                                } catch (Throwable th7) {
                                                                                                    th = th7;
                                                                                                    Intent intent11 = intent8;
                                                                                                    Configuration configuration6 = globalConfig;
                                                                                                    WaitResult waitResult4 = outResult;
                                                                                                    while (true) {
                                                                                                        break;
                                                                                                    }
                                                                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                                    throw th;
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    } catch (Throwable th8) {
                                                                                        th = th8;
                                                                                        String str2 = resolvedType;
                                                                                        IApplicationThread iApplicationThread4 = iApplicationThread;
                                                                                        int i5 = callingUid7;
                                                                                        activityManagerService2 = activityManagerService;
                                                                                        Configuration configuration7 = configuration;
                                                                                        WaitResult waitResult222 = outResult;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                        throw th;
                                                                                    }
                                                                                }
                                                                                int appCallingUid2 = callingUid7;
                                                                                if (iApplicationThread != null) {
                                                                                    try {
                                                                                        ProcessRecord callerApp = this.mService.getRecordForAppLocked(iApplicationThread);
                                                                                        if (callerApp != null) {
                                                                                            int i6 = callingUid7;
                                                                                            try {
                                                                                                appCallingUid = callerApp.info.uid;
                                                                                                origId2 = origId3;
                                                                                                callingPid5 = callingPid2;
                                                                                            } catch (Throwable th9) {
                                                                                                th = th9;
                                                                                                String str3 = resolvedType;
                                                                                                IApplicationThread iApplicationThread5 = iApplicationThread;
                                                                                                activityManagerService2 = activityManagerService;
                                                                                                Configuration configuration72 = configuration;
                                                                                                WaitResult waitResult2222 = outResult;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                                throw th;
                                                                                            }
                                                                                        } else {
                                                                                            int i7 = appCallingUid2;
                                                                                            try {
                                                                                                StringBuilder sb = new StringBuilder();
                                                                                                long j2 = origId3;
                                                                                                sb.append("Unable to find app for caller ");
                                                                                                sb.append(iApplicationThread);
                                                                                                sb.append(" (pid=");
                                                                                                int callingPid9 = callingPid2;
                                                                                                try {
                                                                                                    sb.append(callingPid9);
                                                                                                    sb.append(") when starting: ");
                                                                                                    sb.append(intent2.toString());
                                                                                                    Slog.w(ActivityManagerService.TAG, sb.toString());
                                                                                                    SafeActivityOptions.abort(options);
                                                                                                } catch (Throwable th10) {
                                                                                                    th = th10;
                                                                                                    String str4 = resolvedType;
                                                                                                    IApplicationThread iApplicationThread6 = iApplicationThread;
                                                                                                    int i8 = callingPid9;
                                                                                                    activityManagerService2 = activityManagerService;
                                                                                                    Configuration configuration8 = configuration;
                                                                                                    boolean z4 = componentSpecified3;
                                                                                                    Intent intent12 = ephemeralIntent2;
                                                                                                    WaitResult waitResult22222 = outResult;
                                                                                                    while (true) {
                                                                                                        break;
                                                                                                    }
                                                                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                                    throw th;
                                                                                                }
                                                                                            } catch (Throwable th11) {
                                                                                                th = th11;
                                                                                                String str5 = resolvedType;
                                                                                                IApplicationThread iApplicationThread7 = iApplicationThread;
                                                                                                int i9 = callingPid2;
                                                                                                activityManagerService2 = activityManagerService;
                                                                                                Configuration configuration9 = configuration;
                                                                                                boolean z5 = componentSpecified3;
                                                                                                Intent intent13 = ephemeralIntent2;
                                                                                                int i10 = realCallingUid;
                                                                                                ResolveInfo resolveInfo5 = rInfo2;
                                                                                                ActivityInfo activityInfo3 = aInfo;
                                                                                                WaitResult waitResult5 = outResult;
                                                                                                while (true) {
                                                                                                    break;
                                                                                                }
                                                                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                                throw th;
                                                                                            }
                                                                                        }
                                                                                    } catch (Throwable th12) {
                                                                                        th = th12;
                                                                                        int i11 = callingUid7;
                                                                                        String str6 = resolvedType;
                                                                                        IApplicationThread iApplicationThread8 = iApplicationThread;
                                                                                        int i12 = callingPid2;
                                                                                        activityManagerService2 = activityManagerService;
                                                                                        Configuration configuration10 = configuration;
                                                                                        boolean z6 = componentSpecified3;
                                                                                        Intent intent14 = ephemeralIntent2;
                                                                                        int i13 = realCallingUid;
                                                                                        ResolveInfo resolveInfo6 = rInfo2;
                                                                                        ActivityInfo activityInfo4 = aInfo;
                                                                                        WaitResult waitResult6 = outResult;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                        throw th;
                                                                                    }
                                                                                } else {
                                                                                    appCallingUid = appCallingUid2;
                                                                                    origId2 = origId3;
                                                                                    callingPid5 = callingPid2;
                                                                                }
                                                                            } catch (Throwable th13) {
                                                                                th = th13;
                                                                                int i14 = callingUid7;
                                                                                activityManagerService2 = activityManagerService;
                                                                                ActivityInfo activityInfo5 = aInfo;
                                                                                String str7 = resolvedType;
                                                                                int i15 = callingPid2;
                                                                                Configuration configuration11 = configuration;
                                                                                boolean z7 = componentSpecified3;
                                                                                Intent intent15 = ephemeralIntent2;
                                                                                int i16 = realCallingUid;
                                                                                ResolveInfo resolveInfo7 = rInfo2;
                                                                                WaitResult waitResult7 = outResult;
                                                                                IApplicationThread iApplicationThread9 = caller;
                                                                                ActivityInfo activityInfo6 = activityInfo5;
                                                                                while (true) {
                                                                                    break;
                                                                                }
                                                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                throw th;
                                                                            }
                                                                            try {
                                                                                IIntentSender target = this.mService.getIntentSenderLocked(2, PackageManagerService.PLATFORM_PACKAGE_NAME, appCallingUid, callingUid6, null, null, 0, new Intent[]{intent2}, new String[]{resolvedType}, 1342177280, null);
                                                                                newIntent = new Intent();
                                                                                if (requestCode >= 0) {
                                                                                    try {
                                                                                        newIntent.putExtra("has_result", true);
                                                                                    } catch (Throwable th14) {
                                                                                        th = th14;
                                                                                        String str8 = resolvedType;
                                                                                        int i17 = callingPid5;
                                                                                        activityManagerService2 = activityManagerService;
                                                                                        Configuration configuration12 = configuration;
                                                                                        boolean z8 = componentSpecified3;
                                                                                        Intent intent16 = ephemeralIntent2;
                                                                                        int i18 = realCallingUid;
                                                                                        ResolveInfo resolveInfo8 = rInfo2;
                                                                                        IApplicationThread iApplicationThread10 = caller;
                                                                                    }
                                                                                }
                                                                                newIntent.putExtra(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT, new IntentSender(target));
                                                                                if (heavy.activities.size() > 0) {
                                                                                    ActivityRecord hist = heavy.activities.get(0);
                                                                                    ProcessRecord processRecord = heavy;
                                                                                    newIntent.putExtra("cur_app", hist.packageName);
                                                                                    newIntent.putExtra("cur_task", hist.getTask().taskId);
                                                                                }
                                                                                newIntent.putExtra("new_app", aInfo.packageName);
                                                                                newIntent.setFlags(intent2.getFlags());
                                                                                newIntent.setClassName(PackageManagerService.PLATFORM_PACKAGE_NAME, HeavyWeightSwitcherActivity.class.getName());
                                                                                origId = origId2;
                                                                                intent3 = newIntent;
                                                                                try {
                                                                                    callingUid4 = Binder.getCallingUid();
                                                                                    try {
                                                                                        callingPid6 = Binder.getCallingPid();
                                                                                        componentSpecified3 = true;
                                                                                        try {
                                                                                            aInfo4 = aInfo;
                                                                                            realCallingUid3 = realCallingUid;
                                                                                        } catch (Throwable th15) {
                                                                                            th = th15;
                                                                                            activityManagerService4 = activityManagerService;
                                                                                            activityInfo = aInfo;
                                                                                            Configuration configuration13 = configuration;
                                                                                            int i19 = callingUid4;
                                                                                            int i20 = callingPid6;
                                                                                            int i21 = realCallingUid;
                                                                                            WaitResult waitResult8 = outResult;
                                                                                            ActivityInfo aInfo5 = activityInfo;
                                                                                            while (true) {
                                                                                                break;
                                                                                            }
                                                                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                            throw th;
                                                                                        }
                                                                                    } catch (Throwable th16) {
                                                                                        th = th16;
                                                                                        activityManagerService4 = activityManagerService;
                                                                                        activityInfo = aInfo;
                                                                                        int i22 = callingPid5;
                                                                                        Configuration configuration14 = configuration;
                                                                                        int i23 = callingUid4;
                                                                                        int i212 = realCallingUid;
                                                                                        WaitResult waitResult82 = outResult;
                                                                                        ActivityInfo aInfo52 = activityInfo;
                                                                                        while (true) {
                                                                                            break;
                                                                                        }
                                                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                        throw th;
                                                                                    }
                                                                                } catch (Throwable th17) {
                                                                                    th = th17;
                                                                                    activityManagerService4 = activityManagerService;
                                                                                    activityInfo = aInfo;
                                                                                    int i24 = callingPid5;
                                                                                    Intent intent17 = intent3;
                                                                                    Configuration configuration15 = configuration;
                                                                                    boolean z9 = componentSpecified3;
                                                                                    Intent intent18 = ephemeralIntent2;
                                                                                    int i25 = realCallingUid;
                                                                                    ResolveInfo resolveInfo9 = rInfo2;
                                                                                    WaitResult waitResult9 = outResult;
                                                                                    ActivityInfo aInfo522 = activityInfo;
                                                                                    while (true) {
                                                                                        break;
                                                                                    }
                                                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                    throw th;
                                                                                }
                                                                            } catch (Throwable th18) {
                                                                                th = th18;
                                                                                activityManagerService4 = activityManagerService;
                                                                                activityInfo = aInfo;
                                                                                String str9 = resolvedType;
                                                                                int i26 = callingPid5;
                                                                                Configuration configuration16 = configuration;
                                                                                boolean z10 = componentSpecified3;
                                                                                Intent intent19 = ephemeralIntent2;
                                                                                int i27 = realCallingUid;
                                                                                ResolveInfo resolveInfo10 = rInfo2;
                                                                                WaitResult waitResult10 = outResult;
                                                                                IApplicationThread iApplicationThread11 = caller;
                                                                                ActivityInfo aInfo5222 = activityInfo;
                                                                                while (true) {
                                                                                    break;
                                                                                }
                                                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                throw th;
                                                                            }
                                                                            try {
                                                                                Intent intent20 = newIntent;
                                                                                stack = stack3;
                                                                                activityManagerService2 = activityManagerService;
                                                                                realCallingUid2 = realCallingUid3;
                                                                                ActivityInfo activityInfo7 = aInfo4;
                                                                                try {
                                                                                    ResolveInfo rInfo5 = this.mSupervisor.resolveIntent(intent3, null, callingUid6, 0, computeResolveFilterUid(callingUid4, realCallingUid3, this.mRequest.filterCallingUid));
                                                                                    if (rInfo5 != null) {
                                                                                        try {
                                                                                            activityInfo2 = rInfo5.activityInfo;
                                                                                        } catch (Throwable th19) {
                                                                                            th = th19;
                                                                                            ResolveInfo resolveInfo11 = rInfo5;
                                                                                            Configuration configuration17 = configuration;
                                                                                            int i28 = callingUid4;
                                                                                            int i29 = callingPid6;
                                                                                            Intent intent21 = ephemeralIntent2;
                                                                                        }
                                                                                    } else {
                                                                                        activityInfo2 = null;
                                                                                    }
                                                                                    ActivityInfo aInfo6 = activityInfo2;
                                                                                    if (aInfo6 != null) {
                                                                                        try {
                                                                                            resolvedType2 = null;
                                                                                            caller2 = null;
                                                                                            rInfo2 = rInfo5;
                                                                                            callingUid3 = callingUid4;
                                                                                            callingPid3 = callingPid6;
                                                                                            componentSpecified = true;
                                                                                            aInfo2 = this.mService.getActivityInfoForUser(aInfo6, callingUid6);
                                                                                            intent2 = intent3;
                                                                                        } catch (Throwable th20) {
                                                                                            th = th20;
                                                                                            ResolveInfo resolveInfo12 = rInfo5;
                                                                                            Configuration configuration18 = configuration;
                                                                                            int i30 = callingUid4;
                                                                                            int i31 = callingPid6;
                                                                                            Intent intent22 = ephemeralIntent2;
                                                                                            Intent intent23 = intent3;
                                                                                            ActivityInfo activityInfo8 = aInfo6;
                                                                                            WaitResult waitResult222222 = outResult;
                                                                                            while (true) {
                                                                                                break;
                                                                                            }
                                                                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                            throw th;
                                                                                        }
                                                                                    } else {
                                                                                        resolvedType2 = null;
                                                                                        caller2 = null;
                                                                                        rInfo2 = rInfo5;
                                                                                        callingUid3 = callingUid4;
                                                                                        callingPid3 = callingPid6;
                                                                                        componentSpecified = true;
                                                                                        intent2 = intent3;
                                                                                        aInfo2 = aInfo6;
                                                                                    }
                                                                                    this.mService.addCallerToIntent(intent2, caller2);
                                                                                    if (!HwDeviceManager.disallowOp(intent2)) {
                                                                                    }
                                                                                } catch (Throwable th21) {
                                                                                    th = th21;
                                                                                    Configuration configuration19 = configuration;
                                                                                    int i32 = callingUid4;
                                                                                    int i33 = callingPid6;
                                                                                    Intent intent24 = ephemeralIntent2;
                                                                                    ResolveInfo resolveInfo13 = rInfo2;
                                                                                    WaitResult waitResult11 = outResult;
                                                                                    ActivityInfo aInfo7 = activityInfo7;
                                                                                    while (true) {
                                                                                        break;
                                                                                    }
                                                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                    throw th;
                                                                                }
                                                                            } catch (Throwable th22) {
                                                                                th = th22;
                                                                                activityManagerService2 = activityManagerService;
                                                                                int i34 = realCallingUid3;
                                                                                Configuration configuration20 = configuration;
                                                                                int i35 = callingUid4;
                                                                                int i36 = callingPid6;
                                                                                Intent intent25 = ephemeralIntent2;
                                                                                ResolveInfo resolveInfo14 = rInfo2;
                                                                                WaitResult waitResult12 = outResult;
                                                                                Intent intent26 = intent3;
                                                                                ActivityInfo activityInfo9 = aInfo4;
                                                                                while (true) {
                                                                                    break;
                                                                                }
                                                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                                                throw th;
                                                                            }
                                                                        }
                                                                    }
                                                                } catch (Throwable th23) {
                                                                    th = th23;
                                                                    int i37 = callingUid7;
                                                                    ResolveInfo resolveInfo15 = rInfo;
                                                                    activityManagerService2 = activityManagerService;
                                                                    ActivityInfo activityInfo10 = aInfo;
                                                                    String str10 = resolvedType;
                                                                    int i38 = callingPid2;
                                                                    Configuration configuration21 = configuration;
                                                                    boolean z11 = componentSpecified3;
                                                                    Intent intent27 = ephemeralIntent2;
                                                                    int i39 = realCallingUid;
                                                                    WaitResult waitResult13 = outResult;
                                                                    IApplicationThread iApplicationThread12 = caller;
                                                                    ActivityInfo activityInfo11 = activityInfo10;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                                    throw th;
                                                                }
                                                            }
                                                            callingUid3 = callingUid7;
                                                            rInfo2 = rInfo;
                                                            origId = origId3;
                                                            activityManagerService3 = activityManagerService;
                                                            aInfo3 = aInfo;
                                                            callingPid4 = callingPid2;
                                                            realCallingUid2 = realCallingUid;
                                                            stack = stack3;
                                                            resolvedType2 = resolvedType;
                                                            callingPid3 = callingPid4;
                                                            componentSpecified = componentSpecified3;
                                                            caller2 = caller;
                                                            aInfo2 = aInfo3;
                                                            this.mService.addCallerToIntent(intent2, caller2);
                                                            if (!HwDeviceManager.disallowOp(intent2)) {
                                                            }
                                                        }
                                                    } catch (Throwable th24) {
                                                        th = th24;
                                                        String str11 = resolvedType;
                                                        IApplicationThread iApplicationThread13 = iApplicationThread;
                                                        int i40 = callingUid7;
                                                        activityManagerService2 = activityManagerService;
                                                        Configuration configuration22 = configuration;
                                                        boolean z12 = componentSpecified3;
                                                        Intent intent28 = ephemeralIntent2;
                                                        int i41 = callingPid2;
                                                        int i42 = realCallingUid;
                                                        WaitResult waitResult2222222 = outResult;
                                                        while (true) {
                                                            break;
                                                        }
                                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                                        throw th;
                                                    }
                                                }
                                                z = false;
                                                try {
                                                    stack2.mConfigWillChange = z;
                                                    if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                                    }
                                                    long origId32 = Binder.clearCallingIdentity();
                                                    ActivityStack stack32 = stack2;
                                                    if (aInfo != null) {
                                                    }
                                                    callingUid3 = callingUid7;
                                                    rInfo2 = rInfo;
                                                    origId = origId32;
                                                    activityManagerService3 = activityManagerService;
                                                    aInfo3 = aInfo;
                                                    callingPid4 = callingPid2;
                                                    realCallingUid2 = realCallingUid;
                                                    stack = stack32;
                                                    resolvedType2 = resolvedType;
                                                    callingPid3 = callingPid4;
                                                    componentSpecified = componentSpecified3;
                                                    caller2 = caller;
                                                    aInfo2 = aInfo3;
                                                } catch (Throwable th25) {
                                                    th = th25;
                                                    int i43 = callingUid7;
                                                    ResolveInfo resolveInfo16 = rInfo;
                                                    activityManagerService2 = activityManagerService;
                                                    ActivityInfo activityInfo12 = aInfo;
                                                    Configuration configuration23 = configuration;
                                                    Intent intent29 = ephemeralIntent2;
                                                    int i44 = realCallingUid;
                                                    WaitResult waitResult14 = outResult;
                                                    String str12 = resolvedType;
                                                    int i45 = callingPid2;
                                                    boolean z13 = componentSpecified3;
                                                    IApplicationThread iApplicationThread14 = caller;
                                                    ActivityInfo activityInfo13 = activityInfo12;
                                                    while (true) {
                                                        break;
                                                    }
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    throw th;
                                                }
                                                try {
                                                    this.mService.addCallerToIntent(intent2, caller2);
                                                    if (!HwDeviceManager.disallowOp(intent2)) {
                                                    }
                                                } catch (Throwable th26) {
                                                    th = th26;
                                                    IApplicationThread iApplicationThread15 = caller2;
                                                    Configuration configuration24 = configuration;
                                                    Intent intent30 = ephemeralIntent2;
                                                    WaitResult waitResult15 = outResult;
                                                    int i46 = callingPid3;
                                                    ResolveInfo resolveInfo17 = rInfo2;
                                                    while (true) {
                                                        break;
                                                    }
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    throw th;
                                                }
                                            }
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return -94;
                                        }
                                    }
                                } catch (Throwable th27) {
                                    parent = th27;
                                    int i47 = callingPid8;
                                    int i48 = realCallingUid5;
                                    UserManager userManager3 = userManager;
                                    UserInfo userInfo3 = userInfo;
                                    Intent intent31 = intent6;
                                    ResolveInfo resolveInfo18 = rInfo4;
                                    token = token2;
                                    Binder.restoreCallingIdentity(token);
                                    throw parent;
                                }
                            }
                            z2 = false;
                            profileLockedAndParentUnlockingOrUnlocked = z2;
                            long token32 = token2;
                            Binder.restoreCallingIdentity(token32);
                            if (profileLockedAndParentUnlockingOrUnlocked) {
                            }
                        } catch (Throwable th28) {
                            parent = th28;
                            int i49 = callingPid8;
                            int i50 = realCallingUid5;
                            UserManager userManager4 = userManager;
                            UserInfo userInfo4 = userInfo;
                            Intent intent32 = intent6;
                            ResolveInfo resolveInfo19 = rInfo4;
                            token = token2;
                            Binder.restoreCallingIdentity(token);
                            throw parent;
                        }
                    } else {
                        callingPid2 = callingPid8;
                        realCallingUid = realCallingUid5;
                        intent2 = intent6;
                        rInfo3 = rInfo4;
                        rInfo = rInfo3;
                        int i51 = startFlags;
                        aInfo = this.mSupervisor.resolveActivity(intent2, rInfo, i51, profilerInfo);
                        if (intent2.getComponent() != null) {
                        }
                        Jlog.d(335, aInfo.applicationInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + activityName, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                        Jlog.i(3023, Jlog.getMessage("ActivityStarter", "startActivityMayWait", "whopkg=" + str + "&pkg=" + aInfo.applicationInfo.packageName + "&cls=" + aInfo.name));
                        activityManagerService = this.mService;
                        synchronized (activityManagerService) {
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return -94;
                    }
                }
            }
            callingPid2 = callingPid8;
            realCallingUid = realCallingUid5;
            intent2 = intent6;
            rInfo3 = rInfo4;
            rInfo = rInfo3;
            int i512 = startFlags;
            aInfo = this.mSupervisor.resolveActivity(intent2, rInfo, i512, profilerInfo);
            if (intent2.getComponent() != null) {
            }
            Jlog.d(335, aInfo.applicationInfo.packageName + SliceClientPermissions.SliceAuthority.DELIMITER + activityName, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            Jlog.i(3023, Jlog.getMessage("ActivityStarter", "startActivityMayWait", "whopkg=" + str + "&pkg=" + aInfo.applicationInfo.packageName + "&cls=" + aInfo.name));
            activityManagerService = this.mService;
            synchronized (activityManagerService) {
            }
            ActivityManagerService.resetPriorityAfterLockedSection();
            return -94;
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    static int computeResolveFilterUid(int customCallingUid, int actualCallingUid, int filterCallingUid) {
        if (filterCallingUid != -10000) {
            return filterCallingUid;
        }
        return customCallingUid >= 0 ? customCallingUid : actualCallingUid;
    }

    private int startActivity(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity) {
        try {
            this.mService.mWindowManager.deferSurfaceLayout();
            int result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor, startFlags, doResume, options, inTask, outActivity);
            try {
                ActivityStack stack = this.mStartActivity.getStack();
                if (!ActivityManager.isStartResultSuccessful(result) && stack != null) {
                    stack.finishActivityLocked(this.mStartActivity, 0, null, "startActivity", true);
                }
                this.mService.mWindowManager.continueSurfaceLayout();
                if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v(ActivityManagerService.TAG, "startActivity result is " + result);
                }
                postStartActivityProcessing(r, result, this.mTargetStack);
                return result;
            } catch (Throwable th) {
                ActivityRecord activityRecord = r;
                throw th;
            }
        } catch (Throwable th2) {
            ActivityRecord activityRecord2 = r;
            ActivityStack stack2 = this.mStartActivity.getStack();
            if (!ActivityManager.isStartResultSuccessful(-96) && stack2 != null) {
                stack2.finishActivityLocked(this.mStartActivity, 0, null, "startActivity", true);
            }
            throw th2;
        } finally {
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    private int startActivityUnchecked(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity) {
        ActivityRecord[] activityRecordArr = outActivity;
        setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor);
        computeLaunchingTaskFlags();
        computeSourceStack();
        this.mIntent.setFlags(this.mLaunchFlags);
        ActivityRecord reusedActivity = getReusableIntentActivity();
        int preferredWindowingMode = 0;
        int preferredLaunchDisplayId = 0;
        if (this.mOptions != null) {
            preferredWindowingMode = this.mOptions.getLaunchWindowingMode();
            preferredLaunchDisplayId = this.mOptions.getLaunchDisplayId();
        }
        if (preferredWindowingMode == 12) {
            reusedActivity = null;
        }
        Flog.i(101, "ReusedActivity is " + reusedActivity);
        if (!this.mLaunchParams.isEmpty()) {
            if (this.mLaunchParams.hasPreferredDisplay()) {
                preferredLaunchDisplayId = this.mLaunchParams.mPreferredDisplayId;
            }
            if (this.mLaunchParams.hasWindowingMode()) {
                preferredWindowingMode = this.mLaunchParams.mWindowingMode;
            }
        }
        ActivityStack activityStack = null;
        if (HwPCUtils.isPcCastModeInServer()) {
            if (!this.mHwActivityStarterEx.isAbleToLaunchInPCCastMode(this.mStartActivity.shortComponentName, this.mPreferredDisplayId, reusedActivity)) {
                return 102;
            }
            int startedResult = hasStartedOnOtherDisplay(this.mStartActivity, this.mPreferredDisplayId);
            if (startedResult != -1) {
                ActivityOptions.abort(this.mOptions);
                if (this.mStartActivity.resultTo != null) {
                    activityStack = this.mStartActivity.resultTo.getStack();
                }
                ActivityStack sourceStack = activityStack;
                if (sourceStack != null) {
                    sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
                }
                if (startedResult == 1) {
                    return 99;
                }
                if (startedResult == 0) {
                    return 98;
                }
                return 0;
            }
            if (killProcessOnOtherDisplay(this.mStartActivity, this.mPreferredDisplayId)) {
                reusedActivity = null;
            }
            if (HwPCUtils.enabledInPad() && reusedActivity != null && (("com.android.systemui/.settings.BrightnessDialog".equals(reusedActivity.shortComponentName) || "com.android.incallui".equals(reusedActivity.packageName) || "com.huawei.android.wfdft".equals(reusedActivity.packageName)) && !HwPCUtils.isValidExtDisplayId(reusedActivity.getDisplayId()))) {
                Slog.i(ActivityManagerService.TAG, "startActivityUnchecked reusedActivity :" + reusedActivity);
                reusedActivity = null;
            }
        } else if (killProcessOnDefaultDisplay(this.mStartActivity)) {
            reusedActivity = null;
        }
        int i = 2;
        if (!(reusedActivity == null || reusedActivity.getTask() == null)) {
            if (this.mService.getLockTaskController().isLockTaskModeViolation(reusedActivity.getTask(), (this.mLaunchFlags & 268468224) == 268468224)) {
                Slog.e(ActivityManagerService.TAG, "startActivityUnchecked: Attempt to violate Lock Task Mode");
                return 101;
            }
            boolean clearTopAndResetStandardLaunchMode = (this.mLaunchFlags & 69206016) == 69206016 && this.mLaunchMode == 0;
            if (this.mStartActivity.getTask() == null && !clearTopAndResetStandardLaunchMode) {
                this.mStartActivity.setTask(reusedActivity.getTask());
            }
            if (reusedActivity.getTask().intent == null) {
                reusedActivity.getTask().setIntent(this.mStartActivity);
            }
            if ((this.mLaunchFlags & 67108864) != 0 || isDocumentLaunchesIntoExisting(this.mLaunchFlags) || isLaunchModeOneOf(3, 2)) {
                TaskRecord task = reusedActivity.getTask();
                ActivityRecord top = task.performClearTaskForReuseLocked(this.mStartActivity, this.mLaunchFlags);
                if (reusedActivity.getTask() == null) {
                    reusedActivity.setTask(task);
                }
                if (top != null) {
                    if (top.frontOfTask) {
                        top.getTask().setIntent(this.mStartActivity);
                    }
                    deliverNewIntent(top);
                }
            }
            this.mSupervisor.sendPowerHintForLaunchStartIfNeeded(false, reusedActivity);
            ActivityRecord reusedActivity2 = setTargetStackAndMoveToFrontIfNeeded(reusedActivity);
            ActivityRecord outResult = (activityRecordArr == null || activityRecordArr.length <= 0) ? null : activityRecordArr[0];
            if (outResult != null && (outResult.finishing || outResult.noDisplay)) {
                activityRecordArr[0] = reusedActivity2;
            }
            if ((this.mStartFlags & 1) != 0) {
                resumeTargetStackIfNeeded();
                return 1;
            } else if (reusedActivity2 != null) {
                setTaskFromIntentActivity(reusedActivity2);
                if (!this.mAddingToTask && this.mReuseTask == null) {
                    resumeTargetStackIfNeeded();
                    if (activityRecordArr != null && activityRecordArr.length > 0) {
                        activityRecordArr[0] = reusedActivity2;
                    }
                    if (!this.mMovedToFront) {
                        i = 3;
                    }
                    return i;
                }
            }
        }
        if (this.mStartActivity.packageName == null) {
            if (this.mStartActivity.resultTo != null) {
                activityStack = this.mStartActivity.resultTo.getStack();
            }
            ActivityStack sourceStack2 = activityStack;
            if (sourceStack2 != null) {
                sourceStack2.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            }
            ActivityOptions.abort(this.mOptions);
            return -92;
        }
        ActivityStack topStack = this.mSupervisor.mFocusedStack;
        ActivityRecord topFocused = topStack.getTopActivity();
        ActivityRecord top2 = topStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        if (top2 != null && this.mStartActivity.resultTo == null && top2.realActivity.equals(this.mStartActivity.realActivity) && top2.userId == this.mStartActivity.userId && top2.app != null && top2.app.thread != null && ((this.mLaunchFlags & 536870912) != 0 || isLaunchModeOneOf(1, 2))) {
            topStack.mLastPausedActivity = null;
            if (this.mDoResume) {
                this.mSupervisor.resumeFocusedStackTopActivityLocked();
            }
            ActivityOptions.abort(this.mOptions);
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            deliverNewIntent(top2);
            this.mSupervisor.handleNonResizableTaskIfNeeded(top2.getTask(), preferredWindowingMode, preferredLaunchDisplayId, topStack);
            return 3;
        }
        boolean newTask = false;
        TaskRecord taskToAffiliate = (!this.mLaunchTaskBehind || this.mSourceRecord == null) ? null : this.mSourceRecord.getTask();
        int result = 0;
        if (this.mStartActivity.resultTo == null && this.mInTask == null && !this.mAddingToTask && (this.mLaunchFlags & 268435456) != 0) {
            newTask = true;
            result = setTaskFromReuseOrCreateNewTask(taskToAffiliate, topStack);
        } else if (this.mSourceRecord != null) {
            if (HwPCUtils.isPcCastModeInServer() && this.mSourceRecord.getTask() == null && this.mSourceRecord.getStack() == null) {
                Slog.i(ActivityManagerService.TAG, "ActivityStarter startActivityUnchecked task and stack null");
                setTaskToCurrentTopOrCreateNewTask();
            } else {
                result = setTaskFromSourceRecord();
            }
        } else if (this.mInTask != null) {
            result = setTaskFromInTask();
        } else {
            setTaskToCurrentTopOrCreateNewTask();
        }
        boolean newTask2 = newTask;
        int result2 = result;
        this.mHwActivityStarterEx.handleFreeFormStackIfNeed(this.mStartActivity);
        if (result2 != 0) {
            return result2;
        }
        if (this.mService != null && this.mService.mHwAMSEx != null && !this.mService.mHwAMSEx.checkActivityStartForPCMode(this, this.mOptions, this.mStartActivity, this.mTargetStack)) {
            return -96;
        }
        if (this.mCallingUid == 1000 && this.mIntent.getCallingUid() != 0) {
            this.mCallingUid = this.mIntent.getCallingUid();
            this.mIntent.setCallingUid(0);
        }
        this.mService.grantUriPermissionFromIntentLocked(this.mCallingUid, this.mStartActivity.packageName, this.mIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.userId);
        this.mService.grantEphemeralAccessLocked(this.mStartActivity.userId, this.mIntent, this.mStartActivity.appInfo.uid, UserHandle.getAppId(this.mCallingUid));
        if (newTask2) {
            EventLog.writeEvent(EventLogTags.AM_CREATE_TASK, new Object[]{Integer.valueOf(this.mStartActivity.userId), Integer.valueOf(this.mStartActivity.getTask().taskId)});
        }
        ActivityStack.logStartActivity(EventLogTags.AM_CREATE_ACTIVITY, this.mStartActivity, this.mStartActivity.getTask());
        this.mTargetStack.mLastPausedActivity = null;
        this.mSupervisor.sendPowerHintForLaunchStartIfNeeded(false, this.mStartActivity);
        TaskRecord taskRecord = taskToAffiliate;
        ActivityRecord activityRecord = top2;
        this.mTargetStack.startActivityLocked(this.mStartActivity, topFocused, newTask2, this.mKeepCurTransition, this.mOptions);
        if (this.mDoResume) {
            ActivityRecord topTaskActivity = this.mStartActivity.getTask().topRunningActivityLocked();
            if (!this.mTargetStack.isFocusable() || !(topTaskActivity == null || !topTaskActivity.mTaskOverlay || this.mStartActivity == topTaskActivity)) {
                this.mTargetStack.ensureActivitiesVisibleLocked(null, 0, false);
                this.mService.mWindowManager.executeAppTransition();
            } else {
                if (this.mTargetStack.isFocusable() && !this.mSupervisor.isFocusedStack(this.mTargetStack)) {
                    this.mTargetStack.moveToFront("startActivityUnchecked");
                }
                this.mSupervisor.resumeFocusedStackTopActivityLocked(this.mTargetStack, this.mStartActivity, this.mOptions);
            }
        } else if (this.mStartActivity != null) {
            this.mSupervisor.mRecentTasks.add(this.mStartActivity.getTask());
        }
        this.mSupervisor.updateUserStackLocked(this.mStartActivity.userId, this.mTargetStack);
        this.mSupervisor.handleNonResizableTaskIfNeeded(this.mStartActivity.getTask(), preferredWindowingMode, preferredLaunchDisplayId, this.mTargetStack);
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void reset(boolean clearRequest) {
        this.mStartActivity = null;
        this.mIntent = null;
        this.mCallingUid = -1;
        this.mOptions = null;
        this.mLaunchTaskBehind = false;
        this.mLaunchFlags = 0;
        this.mLaunchMode = -1;
        this.mLaunchParams.reset();
        this.mNotTop = null;
        this.mDoResume = false;
        this.mStartFlags = 0;
        this.mSourceRecord = null;
        this.mPreferredDisplayId = -1;
        this.mInTask = null;
        this.mAddingToTask = false;
        this.mReuseTask = null;
        this.mNewTaskInfo = null;
        this.mNewTaskIntent = null;
        this.mSourceStack = null;
        this.mTargetStack = null;
        this.mMovedToFront = false;
        this.mNoAnimation = false;
        this.mKeepCurTransition = false;
        this.mAvoidMoveToFront = false;
        this.mVoiceSession = null;
        this.mVoiceInteractor = null;
        this.mIntentDelivered = false;
        if (clearRequest) {
            this.mRequest.reset();
        }
    }

    /* access modifiers changed from: protected */
    public int hasStartedOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        return -1;
    }

    /* access modifiers changed from: protected */
    public boolean killProcessOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean killProcessOnDefaultDisplay(ActivityRecord startActivity) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void setInitialState(ActivityRecord r, ActivityOptions options, TaskRecord inTask, boolean doResume, int startFlags, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        ActivityRecord activityRecord = r;
        ActivityOptions activityOptions = options;
        TaskRecord taskRecord = inTask;
        boolean z = doResume;
        int i = startFlags;
        boolean z2 = false;
        reset(false);
        this.mStartActivity = activityRecord;
        this.mIntent = activityRecord.intent;
        this.mOptions = activityOptions;
        this.mCallingUid = activityRecord.launchedFromUid;
        ActivityRecord activityRecord2 = sourceRecord;
        this.mSourceRecord = activityRecord2;
        this.mVoiceSession = voiceSession;
        this.mVoiceInteractor = voiceInteractor;
        this.mPreferredDisplayId = getPreferedDisplayId(this.mSourceRecord, this.mStartActivity, activityOptions);
        this.mLaunchParams.reset();
        this.mSupervisor.getLaunchParamsController().calculate(taskRecord, null, activityRecord, activityRecord2, activityOptions, this.mLaunchParams);
        this.mLaunchMode = activityRecord.launchMode;
        this.mLaunchFlags = adjustLaunchFlagsToDocumentMode(activityRecord, 3 == this.mLaunchMode, 2 == this.mLaunchMode, this.mIntent.getFlags());
        this.mLaunchTaskBehind = activityRecord.mLaunchTaskBehind && !isLaunchModeOneOf(2, 3) && (this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0;
        sendNewTaskResultRequestIfNeeded();
        if ((this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0 && activityRecord.resultTo == null) {
            this.mLaunchFlags |= 268435456;
        }
        if ((this.mLaunchFlags & 268435456) != 0 && (this.mLaunchTaskBehind || activityRecord.info.documentLaunchMode == 2)) {
            this.mLaunchFlags |= 134217728;
        }
        this.mSupervisor.mUserLeaving = (this.mLaunchFlags & 262144) == 0;
        if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
            Slog.v(ActivityManagerService.TAG, "startActivity() => mUserLeaving=" + this.mSupervisor.mUserLeaving);
        }
        this.mDoResume = z;
        if (!z || !r.okToShowLocked()) {
            activityRecord.delayedResume = true;
            this.mDoResume = false;
        }
        if (this.mOptions != null) {
            if (this.mOptions.getLaunchTaskId() != -1 && this.mOptions.getTaskOverlay()) {
                activityRecord.mTaskOverlay = true;
                if (!this.mOptions.canTaskOverlayResume()) {
                    TaskRecord task = this.mSupervisor.anyTaskForIdLocked(this.mOptions.getLaunchTaskId());
                    ActivityRecord top = task != null ? task.getTopActivity() : null;
                    if (top != null && !top.isState(ActivityStack.ActivityState.RESUMED)) {
                        this.mDoResume = false;
                        this.mAvoidMoveToFront = true;
                    }
                }
            } else if (this.mOptions.getAvoidMoveToFront()) {
                this.mDoResume = false;
                this.mAvoidMoveToFront = true;
            }
        }
        this.mNotTop = (this.mLaunchFlags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 ? activityRecord : null;
        this.mInTask = taskRecord;
        if (taskRecord != null && !taskRecord.inRecents) {
            Slog.w(ActivityManagerService.TAG, "Starting activity in task not in recents: " + taskRecord);
            this.mInTask = null;
        }
        this.mStartFlags = i;
        if ((i & 1) != 0) {
            ActivityRecord checkedCaller = activityRecord2;
            if (checkedCaller == null) {
                checkedCaller = this.mSupervisor.mFocusedStack.topRunningNonDelayedActivityLocked(this.mNotTop);
            }
            if (!checkedCaller.realActivity.equals(activityRecord.realActivity)) {
                this.mStartFlags &= -2;
            }
        }
        if ((this.mLaunchFlags & 65536) != 0) {
            z2 = true;
        }
        this.mNoAnimation = z2;
    }

    private void sendNewTaskResultRequestIfNeeded() {
        ActivityStack sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getStack() : null;
        if (!(sourceStack == null || (this.mLaunchFlags & 268435456) == 0)) {
            if (isInSkipCancelResultList(this.mStartActivity.shortComponentName)) {
                Slog.w(ActivityManagerService.TAG, "we skip cancelling activity result from activity " + this.mStartActivity.shortComponentName);
                return;
            }
            Slog.w(ActivityManagerService.TAG, "Activity is launching as a new task, so cancelling activity result.");
            sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            this.mStartActivity.resultTo = null;
        }
    }

    private void computeLaunchingTaskFlags() {
        if (this.mSourceRecord != null || this.mInTask == null || this.mInTask.getStack() == null) {
            this.mInTask = null;
            if ((this.mStartActivity.isResolverActivity() || this.mStartActivity.noDisplay) && this.mSourceRecord != null && this.mSourceRecord.inFreeformWindowingMode()) {
                this.mAddingToTask = true;
            }
        } else {
            Intent baseIntent = this.mInTask.getBaseIntent();
            ActivityRecord root = this.mInTask.getRootActivity();
            if (baseIntent != null) {
                if (isLaunchModeOneOf(3, 2)) {
                    if (!baseIntent.getComponent().equals(this.mStartActivity.intent.getComponent())) {
                        ActivityOptions.abort(this.mOptions);
                        throw new IllegalArgumentException("Trying to launch singleInstance/Task " + this.mStartActivity + " into different task " + this.mInTask);
                    } else if (root != null) {
                        ActivityOptions.abort(this.mOptions);
                        throw new IllegalArgumentException("Caller with mInTask " + this.mInTask + " has root " + root + " but target is singleInstance/Task");
                    }
                }
                if (root == null) {
                    this.mLaunchFlags = (this.mLaunchFlags & -403185665) | (baseIntent.getFlags() & 403185664);
                    this.mIntent.setFlags(this.mLaunchFlags);
                    this.mInTask.setIntent(this.mStartActivity);
                    this.mAddingToTask = true;
                } else if ((this.mLaunchFlags & 268435456) != 0) {
                    this.mAddingToTask = false;
                } else {
                    this.mAddingToTask = true;
                }
                this.mReuseTask = this.mInTask;
            } else {
                ActivityOptions.abort(this.mOptions);
                throw new IllegalArgumentException("Launching into task without base intent: " + this.mInTask);
            }
        }
        if (this.mInTask != null) {
            return;
        }
        if (this.mSourceRecord == null) {
            if ((this.mLaunchFlags & 268435456) == 0 && this.mInTask == null) {
                Slog.w(ActivityManagerService.TAG, "startActivity called from non-Activity context; forcing Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent.toShortStringWithoutClip(true, true, true));
                this.mLaunchFlags = this.mLaunchFlags | 268435456;
            }
        } else if (this.mSourceRecord.launchMode == 3) {
            this.mLaunchFlags |= 268435456;
        } else if (isLaunchModeOneOf(3, 2)) {
            this.mLaunchFlags |= 268435456;
        }
    }

    private void computeSourceStack() {
        if (this.mSourceRecord == null) {
            this.mSourceStack = null;
        } else if (!this.mSourceRecord.finishing) {
            this.mSourceStack = this.mSourceRecord.getStack();
        } else {
            if ((this.mLaunchFlags & 268435456) == 0) {
                Slog.w(ActivityManagerService.TAG, "startActivity called from finishing " + this.mSourceRecord + "; forcing Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                this.mLaunchFlags = this.mLaunchFlags | 268435456;
                this.mNewTaskInfo = this.mSourceRecord.info;
                TaskRecord sourceTask = this.mSourceRecord.getTask();
                this.mNewTaskIntent = sourceTask != null ? sourceTask.intent : null;
            }
            this.mSourceRecord = null;
            this.mSourceStack = null;
        }
    }

    private ActivityRecord getReusableIntentActivity() {
        boolean z = false;
        boolean putIntoExistingTask = (((this.mLaunchFlags & 268435456) != 0 && (this.mLaunchFlags & 134217728) == 0) || isLaunchModeOneOf(3, 2)) & (this.mInTask == null && this.mStartActivity.resultTo == null);
        if (this.mOptions != null && this.mOptions.getLaunchTaskId() != -1) {
            TaskRecord task = this.mSupervisor.anyTaskForIdLocked(this.mOptions.getLaunchTaskId());
            return task != null ? task.getTopActivity() : null;
        } else if (!putIntoExistingTask) {
            return null;
        } else {
            if (3 == this.mLaunchMode) {
                return this.mSupervisor.findActivityLocked(this.mIntent, this.mStartActivity.info, this.mStartActivity.isActivityTypeHome());
            }
            if ((this.mLaunchFlags & 4096) == 0) {
                return this.mSupervisor.findTaskLocked(this.mStartActivity, this.mPreferredDisplayId);
            }
            ActivityStackSupervisor activityStackSupervisor = this.mSupervisor;
            Intent intent = this.mIntent;
            ActivityInfo activityInfo = this.mStartActivity.info;
            if (2 != this.mLaunchMode) {
                z = true;
            }
            return activityStackSupervisor.findActivityLocked(intent, activityInfo, z);
        }
    }

    /* access modifiers changed from: protected */
    public int getPreferedDisplayId(ActivityRecord sourceRecord, ActivityRecord startingActivity, ActivityOptions options) {
        if (startingActivity != null && startingActivity.requestedVrComponent != null) {
            return 0;
        }
        int displayId = this.mService.mVr2dDisplayId;
        if (displayId != -1) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityManagerService.TAG, "getSourceDisplayId :" + displayId);
            }
            return displayId;
        }
        int launchDisplayId = options != null ? options.getLaunchDisplayId() : -1;
        if (launchDisplayId != -1) {
            return launchDisplayId;
        }
        int displayId2 = sourceRecord != null ? sourceRecord.getDisplayId() : -1;
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && ("com.android.incallui".equals(startingActivity.packageName) || "com.android.systemui/.settings.BrightnessDialog".equals(startingActivity.shortComponentName))) {
            Slog.d(ActivityManagerService.TAG, "getSourceDisplayId set displayId :" + HwPCUtils.getPCDisplayID() + ", packageName :" + startingActivity.packageName + ", shortComponentName :" + startingActivity.shortComponentName);
            displayId2 = HwPCUtils.getPCDisplayID();
        }
        IVRSystemServiceManager vrMananger = HwFrameworkFactory.getVRSystemServiceManager();
        if (vrMananger != null ? vrMananger.isVirtualScreenMode() : false) {
            displayId2 = -1;
        }
        if (displayId2 != -1) {
            return displayId2;
        }
        return 0;
    }

    private ActivityRecord setTargetStackAndMoveToFrontIfNeeded(ActivityRecord intentActivity) {
        ActivityRecord activityRecord = intentActivity;
        this.mTargetStack = intentActivity.getStack();
        this.mTargetStack.mLastPausedActivity = null;
        ActivityStack focusStack = this.mSupervisor.getFocusedStack();
        ActivityRecord curTop = focusStack == null ? null : focusStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        TaskRecord topTask = curTop != null ? curTop.getTask() : null;
        if (topTask != null && (!(topTask == intentActivity.getTask() && topTask == focusStack.topTask()) && !this.mAvoidMoveToFront)) {
            this.mStartActivity.intent.addFlags(DumpState.DUMP_CHANGES);
            if (this.mSourceRecord == null || (this.mSourceStack.getTopActivity() != null && this.mSourceStack.getTopActivity().getTask() == this.mSourceRecord.getTask())) {
                if (this.mLaunchTaskBehind && this.mSourceRecord != null) {
                    activityRecord.setTaskToAffiliateWith(this.mSourceRecord.getTask());
                }
                if (!((this.mLaunchFlags & 268468224) == 268468224)) {
                    ActivityStack launchStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.getTask(), this.mOptions);
                    TaskRecord intentTask = intentActivity.getTask();
                    if (launchStack == null || launchStack == this.mTargetStack) {
                        if (this.mTargetStack.getWindowingMode() == 12) {
                            this.mService.mHwAMSEx.exitCoordinationModeInner(false, true);
                        }
                        this.mTargetStack.moveTaskToFrontLocked(intentTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringingFoundTaskToFront");
                        this.mMovedToFront = true;
                    } else if (launchStack.inSplitScreenWindowingMode()) {
                        if ((this.mLaunchFlags & 4096) != 0) {
                            intentTask.reparent(launchStack, true, 0, true, true, "launchToSide");
                        } else {
                            this.mTargetStack.moveTaskToFrontLocked(intentTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringToFrontInsteadOfAdjacentLaunch");
                        }
                        this.mMovedToFront = launchStack != launchStack.getDisplay().getTopStackInWindowingMode(launchStack.getWindowingMode());
                    } else if (launchStack.mDisplayId != this.mTargetStack.mDisplayId) {
                        if (HwPCUtils.isPcCastModeInServer()) {
                            HwPCUtils.log(ActivityManagerService.TAG, " the activity will reparentToDisplay because computer stack is:" + launchStack.mStackId + "#" + launchStack.mDisplayId + " target stack is " + this.mTargetStack.mStackId + "#" + this.mTargetStack.mDisplayId);
                        }
                        intentActivity.getTask().reparent(launchStack, true, 0, true, true, "reparentToDisplay");
                        this.mMovedToFront = true;
                    } else if (launchStack.isActivityTypeHome() && !this.mTargetStack.isActivityTypeHome()) {
                        intentActivity.getTask().reparent(launchStack, true, 0, true, true, "reparentingHome");
                        this.mMovedToFront = true;
                    }
                    this.mOptions = null;
                    if (launchStack != null && launchStack.getAllTasks().isEmpty() && HwPCUtils.isExtDynamicStack(launchStack.mStackId)) {
                        launchStack.remove();
                    }
                    if (HwFreeFormUtils.isFreeFormEnable() && launchStack != null && launchStack.inFreeformWindowingMode() && !this.mTargetStack.inFreeformWindowingMode()) {
                        HwFreeFormUtils.log("ams", "move reused activity to freeform from fullscreen");
                        intentActivity.getTask().mHwTaskRecordEx.forceNewConfigWhenReuseActivity(intentActivity.getTask().mActivities);
                        intentActivity.getTask().reparent(launchStack, true, 0, true, true, "reparentToFreeForm");
                        this.mShouldSkipStartingWindow = true;
                        this.mMovedToFront = true;
                    }
                    if (!INCALLUI_ACTIVITY_CLASS_NAME.equals(activityRecord.shortComponentName) && !this.mShouldSkipStartingWindow) {
                        activityRecord.showStartingWindow(null, false, true);
                        this.mShouldSkipStartingWindow = false;
                    }
                }
            }
        }
        this.mTargetStack = intentActivity.getStack();
        if (!this.mMovedToFront && this.mDoResume) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityManagerService.TAG, "Bring to front target: " + this.mTargetStack + " from " + activityRecord);
            }
            this.mTargetStack.moveToFront("intentActivityFound");
        }
        this.mSupervisor.handleNonResizableTaskIfNeeded(intentActivity.getTask(), 0, 0, this.mTargetStack);
        if ((this.mLaunchFlags & DumpState.DUMP_COMPILER_STATS) != 0) {
            return this.mTargetStack.resetTaskIfNeededLocked(activityRecord, this.mStartActivity);
        }
        return activityRecord;
    }

    private void setTaskFromIntentActivity(ActivityRecord intentActivity) {
        if ((this.mLaunchFlags & 268468224) == 268468224) {
            TaskRecord task = intentActivity.getTask();
            task.performClearTaskLocked();
            this.mReuseTask = task;
            this.mReuseTask.setIntent(this.mStartActivity);
        } else if ((this.mLaunchFlags & 67108864) != 0 || isLaunchModeOneOf(3, 2)) {
            if (intentActivity.getTask().performClearTaskLocked(this.mStartActivity, this.mLaunchFlags) == null) {
                this.mAddingToTask = true;
                if (!isInSkipCancelResultList(this.mStartActivity.shortComponentName)) {
                    this.mStartActivity.setTask(null);
                }
                this.mSourceRecord = intentActivity;
                TaskRecord task2 = this.mSourceRecord.getTask();
                if (task2 != null && task2.getStack() == null) {
                    this.mTargetStack = computeStackFocus(this.mSourceRecord, false, this.mLaunchFlags, this.mOptions);
                    this.mTargetStack.addTask(task2, true ^ this.mLaunchTaskBehind, "startActivityUnchecked");
                }
            }
        } else if (this.mStartActivity.realActivity.equals(intentActivity.getTask().realActivity)) {
            if (((this.mLaunchFlags & 536870912) != 0 || 1 == this.mLaunchMode) && intentActivity.realActivity.equals(this.mStartActivity.realActivity)) {
                if (intentActivity.frontOfTask) {
                    intentActivity.getTask().setIntent(this.mStartActivity);
                }
                deliverNewIntent(intentActivity);
            } else if (intentActivity.getTask().isSameIntentFilter(this.mStartActivity)) {
            } else {
                if (!this.mStartActivity.intent.filterEquals(intentActivity.intent) || !"android.intent.action.MAIN".equals(this.mStartActivity.intent.getAction())) {
                    this.mAddingToTask = true;
                    this.mSourceRecord = intentActivity;
                }
            }
        } else if ((this.mLaunchFlags & DumpState.DUMP_COMPILER_STATS) == 0) {
            this.mAddingToTask = true;
            this.mSourceRecord = intentActivity;
        } else if (!intentActivity.getTask().rootWasReset) {
            intentActivity.getTask().setIntent(this.mStartActivity);
        }
    }

    private void resumeTargetStackIfNeeded() {
        if (this.mDoResume) {
            this.mSupervisor.resumeFocusedStackTopActivityLocked(this.mTargetStack, null, this.mOptions);
        } else {
            ActivityOptions.abort(this.mOptions);
        }
        this.mSupervisor.updateUserStackLocked(this.mStartActivity.userId, this.mTargetStack);
    }

    private int setTaskFromReuseOrCreateNewTask(TaskRecord taskToAffiliate, ActivityStack topStack) {
        TaskRecord taskRecord = taskToAffiliate;
        this.mTargetStack = computeStackFocus(this.mStartActivity, true, this.mLaunchFlags, this.mOptions);
        this.mHwActivityStarterEx.moveFreeFormToFullScreenStackIfNeed(this.mStartActivity, this.mTargetStack.inFreeformWindowingMode() || this.mLaunchParams.mWindowingMode == 5);
        if (this.mReuseTask == null) {
            addOrReparentStartingActivity(this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId), this.mNewTaskInfo != null ? this.mNewTaskInfo : this.mStartActivity.info, this.mNewTaskIntent != null ? this.mNewTaskIntent : this.mIntent, this.mVoiceSession, this.mVoiceInteractor, !this.mLaunchTaskBehind, this.mStartActivity, this.mSourceRecord, this.mOptions), "setTaskFromReuseOrCreateNewTask - mReuseTask");
            updateBounds(this.mStartActivity.getTask(), this.mLaunchParams.mBounds);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityManagerService.TAG, "Starting new activity " + this.mStartActivity + " in new task " + this.mStartActivity.getTask());
            }
        } else {
            addOrReparentStartingActivity(this.mReuseTask, "setTaskFromReuseOrCreateNewTask");
        }
        if (taskRecord != null) {
            this.mStartActivity.setTaskToAffiliateWith(taskRecord);
        }
        if (this.mService.getLockTaskController().isLockTaskModeViolation(this.mStartActivity.getTask())) {
            Slog.e(ActivityManagerService.TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("reuseOrNewTask");
        }
        return 0;
    }

    private void deliverNewIntent(ActivityRecord activity) {
        if (!this.mIntentDelivered) {
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, activity, activity.getTask());
            activity.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            this.mIntentDelivered = true;
        }
    }

    private int setTaskFromSourceRecord() {
        int i;
        if (this.mService.getLockTaskController().isLockTaskModeViolation(this.mSourceRecord.getTask())) {
            Slog.e(ActivityManagerService.TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        TaskRecord sourceTask = this.mSourceRecord.getTask();
        ActivityStack sourceStack = this.mSourceRecord.getStack();
        if (this.mTargetStack != null) {
            i = this.mTargetStack.mDisplayId;
        } else {
            i = sourceStack.mDisplayId;
        }
        int targetDisplayId = i;
        if ((sourceStack.topTask() == sourceTask && this.mStartActivity.canBeLaunchedOnDisplay(targetDisplayId) && (this.mTargetStack == null || this.mTargetStack.getWindowingMode() == sourceStack.getWindowingMode() || !sourceStack.inFreeformWindowingMode())) ? false : true) {
            this.mTargetStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.getTask(), this.mOptions);
            if (this.mTargetStack == null && targetDisplayId != sourceStack.mDisplayId) {
                this.mTargetStack = this.mService.mStackSupervisor.getValidLaunchStackOnDisplay(sourceStack.mDisplayId, this.mStartActivity);
            }
            if (this.mTargetStack == null) {
                this.mTargetStack = this.mService.mStackSupervisor.getNextValidLaunchStackLocked(this.mStartActivity, -1);
            }
        }
        if (this.mTargetStack == null) {
            this.mTargetStack = sourceStack;
        } else if (this.mTargetStack != sourceStack) {
            sourceTask.reparent(this.mTargetStack, true, 0, false, true, "launchToSide");
        }
        if (HwFreeFormUtils.isFreeFormEnable() && sourceStack.inFreeformWindowingMode() && this.mTargetStack == sourceStack) {
            this.mHwActivityStarterEx.moveFreeFormToFullScreenStackIfNeed(this.mStartActivity, this.mTargetStack.inFreeformWindowingMode());
            this.mTargetStack = sourceStack;
        }
        if (this.mTargetStack.topTask() != sourceTask && !this.mAvoidMoveToFront) {
            this.mTargetStack.moveTaskToFrontLocked(sourceTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "sourceTaskToFront");
        } else if (this.mDoResume) {
            this.mTargetStack.moveToFront("sourceStackToFront");
        }
        if (!this.mAddingToTask && (this.mLaunchFlags & 67108864) != 0) {
            ActivityRecord top = sourceTask.performClearTaskLocked(this.mStartActivity, this.mLaunchFlags);
            this.mKeepCurTransition = true;
            if (top != null) {
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, top.getTask());
                deliverNewIntent(top);
                this.mTargetStack.mLastPausedActivity = null;
                if (this.mDoResume) {
                    this.mSupervisor.resumeFocusedStackTopActivityLocked();
                }
                ActivityOptions.abort(this.mOptions);
                return 3;
            }
        } else if (!this.mAddingToTask && (this.mLaunchFlags & 131072) != 0) {
            ActivityRecord top2 = sourceTask.findActivityInHistoryLocked(this.mStartActivity);
            if (top2 != null) {
                TaskRecord task = top2.getTask();
                task.moveActivityToFrontLocked(top2);
                top2.updateOptionsLocked(this.mOptions);
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, task);
                deliverNewIntent(top2);
                this.mTargetStack.mLastPausedActivity = null;
                if (this.mDoResume) {
                    this.mSupervisor.resumeFocusedStackTopActivityLocked();
                }
                return 3;
            }
        }
        addOrReparentStartingActivity(sourceTask, "setTaskFromSourceRecord");
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityManagerService.TAG, "Starting new activity " + this.mStartActivity + " in existing task " + this.mStartActivity.getTask() + " from source " + this.mSourceRecord);
        }
        return 0;
    }

    private int setTaskFromInTask() {
        if (this.mService.getLockTaskController().isLockTaskModeViolation(this.mInTask)) {
            Slog.e(ActivityManagerService.TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        this.mTargetStack = this.mInTask.getStack();
        ActivityRecord top = this.mInTask.getTopActivity();
        if (top != null && top.realActivity.equals(this.mStartActivity.realActivity) && top.userId == this.mStartActivity.userId && ((this.mLaunchFlags & 536870912) != 0 || isLaunchModeOneOf(1, 2))) {
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            deliverNewIntent(top);
            return 3;
        } else if (!this.mAddingToTask) {
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            ActivityOptions.abort(this.mOptions);
            return 2;
        } else {
            if (!this.mLaunchParams.mBounds.isEmpty()) {
                ActivityStack stack = this.mSupervisor.getLaunchStack(null, null, this.mInTask, true);
                if (stack != this.mInTask.getStack()) {
                    this.mInTask.reparent(stack, true, 1, false, true, "inTaskToFront");
                    this.mTargetStack = this.mInTask.getStack();
                }
                updateBounds(this.mInTask, this.mLaunchParams.mBounds);
            }
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            addOrReparentStartingActivity(this.mInTask, "setTaskFromInTask");
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityManagerService.TAG, "Starting new activity " + this.mStartActivity + " in explicit task " + this.mStartActivity.getTask());
            }
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void updateBounds(TaskRecord task, Rect bounds) {
        if (!bounds.isEmpty()) {
            ActivityStack stack = task.getStack();
            if (stack == null || !stack.resizeStackWithLaunchBounds()) {
                task.updateOverrideConfiguration(bounds);
            } else {
                this.mService.resizeStack(stack.mStackId, bounds, true, false, true, -1);
            }
        }
    }

    private void setTaskToCurrentTopOrCreateNewTask() {
        this.mTargetStack = computeStackFocus(this.mStartActivity, false, this.mLaunchFlags, this.mOptions);
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("addingToTopTask");
        }
        ActivityRecord prev = this.mTargetStack.getTopActivity();
        TaskRecord task = prev != null ? prev.getTask() : this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId), this.mStartActivity.info, this.mIntent, null, null, true, this.mStartActivity, this.mSourceRecord, this.mOptions);
        addOrReparentStartingActivity(task, "setTaskToCurrentTopOrCreateNewTask");
        this.mTargetStack.positionChildWindowContainerAtTop(task);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityManagerService.TAG, "Starting new activity " + this.mStartActivity + " in new guessed " + this.mStartActivity.getTask());
        }
    }

    private void addOrReparentStartingActivity(TaskRecord parent, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityManagerService.TAG, "addOrReparentStartingActivity reason: " + reason);
        }
        if (this.mStartActivity.getTask() == null || this.mStartActivity.getTask() == parent) {
            parent.addActivityToTop(this.mStartActivity);
        } else {
            this.mStartActivity.reparent(parent, parent.mActivities.size(), reason);
        }
    }

    private int adjustLaunchFlagsToDocumentMode(ActivityRecord r, boolean launchSingleInstance, boolean launchSingleTask, int launchFlags) {
        if ((launchFlags & DumpState.DUMP_FROZEN) == 0 || (!launchSingleInstance && !launchSingleTask)) {
            switch (r.info.documentLaunchMode) {
                case 1:
                    return launchFlags | DumpState.DUMP_FROZEN;
                case 2:
                    return launchFlags | DumpState.DUMP_FROZEN;
                case 3:
                    return launchFlags & -134217729;
                default:
                    return launchFlags;
            }
        } else {
            Slog.i(ActivityManagerService.TAG, "Ignoring FLAG_ACTIVITY_NEW_DOCUMENT, launchMode is \"singleInstance\" or \"singleTask\"");
            return launchFlags & -134742017;
        }
    }

    private ActivityStack computeStackFocus(ActivityRecord r, boolean newTask, int launchFlags, ActivityOptions aOptions) {
        TaskRecord task = r.getTask();
        ActivityStack stack = getLaunchStack(r, launchFlags, task, aOptions);
        if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
            Slog.d(ActivityManagerService.TAG, "getLaunchStack stack:" + stack);
        }
        if (stack != null) {
            return stack;
        }
        ActivityStack currentStack = task != null ? task.getStack() : null;
        if (currentStack != null) {
            if (this.mSupervisor.mFocusedStack != currentStack) {
                if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(ActivityManagerService.TAG, "computeStackFocus: Setting focused stack to r=" + r + " task=" + task);
                }
            } else if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityManagerService.TAG, "computeStackFocus: Focused stack already=" + this.mSupervisor.mFocusedStack);
            }
            return currentStack;
        } else if (canLaunchIntoFocusedStack(r, newTask)) {
            if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityManagerService.TAG, "computeStackFocus: Have a focused stack=" + this.mSupervisor.mFocusedStack);
            }
            return this.mSupervisor.mFocusedStack;
        } else {
            if (this.mPreferredDisplayId != 0) {
                stack = this.mSupervisor.getValidLaunchStackOnDisplay(this.mPreferredDisplayId, r);
                if (stack == null) {
                    if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.d(ActivityManagerService.TAG, "computeStackFocus: Can't launch on mPreferredDisplayId=" + this.mPreferredDisplayId + ", looking on all displays.");
                    }
                    stack = this.mSupervisor.getNextValidLaunchStackLocked(r, this.mPreferredDisplayId);
                }
            }
            if (stack == null) {
                ActivityDisplay display = this.mSupervisor.getDefaultDisplay();
                for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStack stack2 = display.getChildAt(stackNdx);
                    if (!stack2.isOnHomeDisplay()) {
                        if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.d(ActivityManagerService.TAG, "computeStackFocus: Setting focused stack=" + stack2);
                        }
                        return stack2;
                    }
                }
                stack = this.mSupervisor.getLaunchStack(r, aOptions, task, true);
            }
            if (ActivityManagerDebugConfig.DEBUG_FOCUS || ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(ActivityManagerService.TAG, "computeStackFocus: New stack r=" + r + " stackId=" + stack.mStackId);
            }
            return stack;
        }
    }

    private boolean canLaunchIntoFocusedStack(ActivityRecord r, boolean newTask) {
        boolean canUseFocusedStack;
        ActivityStack focusedStack = this.mSupervisor.mFocusedStack;
        boolean z = false;
        if (focusedStack.isActivityTypeAssistant()) {
            canUseFocusedStack = r.isActivityTypeAssistant();
        } else {
            int windowingMode = focusedStack.getWindowingMode();
            if (windowingMode != 1) {
                switch (windowingMode) {
                    case 3:
                    case 4:
                        canUseFocusedStack = r.supportsSplitScreenWindowingMode();
                        break;
                    case 5:
                        canUseFocusedStack = r.supportsFreeform();
                        break;
                    default:
                        if (!HwPCUtils.isExtDynamicStack(focusedStack.mStackId)) {
                            if (!focusedStack.isOnHomeDisplay() && r.canBeLaunchedOnDisplay(focusedStack.mDisplayId)) {
                                canUseFocusedStack = true;
                                break;
                            } else {
                                canUseFocusedStack = false;
                                break;
                            }
                        } else {
                            return false;
                        }
                }
            } else {
                canUseFocusedStack = true;
            }
        }
        if (canUseFocusedStack && !newTask && this.mPreferredDisplayId == focusedStack.mDisplayId) {
            z = true;
        }
        return z;
    }

    private ActivityStack getLaunchStack(ActivityRecord r, int launchFlags, TaskRecord task, ActivityOptions aOptions) {
        if (this.mReuseTask != null) {
            return this.mReuseTask.getStack();
        }
        if ((launchFlags & 4096) == 0 || this.mPreferredDisplayId != 0) {
            return this.mSupervisor.getLaunchStack(r, aOptions, task, true, this.mPreferredDisplayId != 0 ? this.mPreferredDisplayId : -1);
        }
        ActivityStack parentStack = task != null ? task.getStack() : this.mSupervisor.mFocusedStack;
        if (parentStack != this.mSupervisor.mFocusedStack) {
            return parentStack;
        }
        if (this.mSupervisor.mFocusedStack != null && task == this.mSupervisor.mFocusedStack.topTask()) {
            return this.mSupervisor.mFocusedStack;
        }
        if (parentStack == null || !parentStack.inSplitScreenPrimaryWindowingMode()) {
            ActivityStack dockedStack = this.mSupervisor.getDefaultDisplay().getSplitScreenPrimaryStack();
            if (dockedStack == null || dockedStack.shouldBeVisible(r)) {
                return dockedStack;
            }
            return this.mSupervisor.getLaunchStack(r, aOptions, task, true);
        }
        return parentStack.getDisplay().getOrCreateStack(4, this.mSupervisor.resolveActivityType(r, this.mOptions, task), true);
    }

    private boolean isLaunchModeOneOf(int mode1, int mode2) {
        return mode1 == this.mLaunchMode || mode2 == this.mLaunchMode;
    }

    static boolean isDocumentLaunchesIntoExisting(int flags) {
        return (524288 & flags) != 0 && (134217728 & flags) == 0;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setIntent(Intent intent) {
        this.mRequest.intent = intent;
        return this;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Intent getIntent() {
        return this.mRequest.intent;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setReason(String reason) {
        this.mRequest.reason = reason;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setCaller(IApplicationThread caller) {
        this.mRequest.caller = caller;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setEphemeralIntent(Intent intent) {
        this.mRequest.ephemeralIntent = intent;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setResolvedType(String type) {
        this.mRequest.resolvedType = type;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setActivityInfo(ActivityInfo info) {
        this.mRequest.activityInfo = info;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setResolveInfo(ResolveInfo info) {
        this.mRequest.resolveInfo = info;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setVoiceSession(IVoiceInteractionSession voiceSession) {
        this.mRequest.voiceSession = voiceSession;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setVoiceInteractor(IVoiceInteractor voiceInteractor) {
        this.mRequest.voiceInteractor = voiceInteractor;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setResultTo(IBinder resultTo) {
        this.mRequest.resultTo = resultTo;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setResultWho(String resultWho) {
        this.mRequest.resultWho = resultWho;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setRequestCode(int requestCode) {
        this.mRequest.requestCode = requestCode;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setCallingPid(int pid) {
        this.mRequest.callingPid = pid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setCallingUid(int uid) {
        this.mRequest.callingUid = uid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setCallingPackage(String callingPackage) {
        this.mRequest.callingPackage = callingPackage;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setRealCallingPid(int pid) {
        this.mRequest.realCallingPid = pid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setRealCallingUid(int uid) {
        this.mRequest.realCallingUid = uid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setStartFlags(int startFlags) {
        this.mRequest.startFlags = startFlags;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setActivityOptions(SafeActivityOptions options) {
        this.mRequest.activityOptions = options;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setActivityOptions(Bundle bOptions) {
        return setActivityOptions(SafeActivityOptions.fromBundle(bOptions));
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setIgnoreTargetSecurity(boolean ignoreTargetSecurity) {
        this.mRequest.ignoreTargetSecurity = ignoreTargetSecurity;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setFilterCallingUid(int filterCallingUid) {
        this.mRequest.filterCallingUid = filterCallingUid;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setComponentSpecified(boolean componentSpecified) {
        this.mRequest.componentSpecified = componentSpecified;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setOutActivity(ActivityRecord[] outActivity) {
        this.mRequest.outActivity = outActivity;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setInTask(TaskRecord inTask) {
        this.mRequest.inTask = inTask;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setWaitResult(WaitResult result) {
        this.mRequest.waitResult = result;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setProfilerInfo(ProfilerInfo info) {
        this.mRequest.profilerInfo = info;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setGlobalConfiguration(Configuration config) {
        this.mRequest.globalConfig = config;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setUserId(int userId) {
        this.mRequest.userId = userId;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setMayWait(int userId) {
        this.mRequest.mayWait = true;
        this.mRequest.userId = userId;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setAllowPendingRemoteAnimationRegistryLookup(boolean allowLookup) {
        this.mRequest.allowPendingRemoteAnimationRegistryLookup = allowLookup;
        return this;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        String prefix2 = prefix + "  ";
        pw.print(prefix2);
        pw.print("mCurrentUser=");
        pw.println(this.mSupervisor.mCurrentUser);
        pw.print(prefix2);
        pw.print("mLastStartReason=");
        pw.println(this.mLastStartReason);
        pw.print(prefix2);
        pw.print("mLastStartActivityTimeMs=");
        pw.println(DateFormat.getDateTimeInstance().format(new Date(this.mLastStartActivityTimeMs)));
        pw.print(prefix2);
        pw.print("mLastStartActivityResult=");
        pw.println(this.mLastStartActivityResult);
        boolean z = false;
        ActivityRecord r = this.mLastStartActivityRecord[0];
        if (r != null) {
            pw.print(prefix2);
            pw.println("mLastStartActivityRecord:");
            r.dump(pw, prefix2 + "  ");
        }
        if (this.mStartActivity != null) {
            pw.print(prefix2);
            pw.println("mStartActivity:");
            this.mStartActivity.dump(pw, prefix2 + "  ");
        }
        if (this.mIntent != null) {
            pw.print(prefix2);
            pw.print("mIntent=");
            pw.println(this.mIntent);
        }
        if (this.mOptions != null) {
            pw.print(prefix2);
            pw.print("mOptions=");
            pw.println(this.mOptions);
        }
        pw.print(prefix2);
        pw.print("mLaunchSingleTop=");
        pw.print(1 == this.mLaunchMode);
        pw.print(" mLaunchSingleInstance=");
        pw.print(3 == this.mLaunchMode);
        pw.print(" mLaunchSingleTask=");
        if (2 == this.mLaunchMode) {
            z = true;
        }
        pw.println(z);
        pw.print(prefix2);
        pw.print("mLaunchFlags=0x");
        pw.print(Integer.toHexString(this.mLaunchFlags));
        pw.print(" mDoResume=");
        pw.print(this.mDoResume);
        pw.print(" mAddingToTask=");
        pw.println(this.mAddingToTask);
    }

    protected static boolean clearFrpRestricted(Context context, int userId) {
        return Settings.Secure.putIntForUser(context.getContentResolver(), SUW_FRP_STATE, 0, userId);
    }

    protected static boolean isFrpRestricted(Context context, int userId) {
        return Settings.Secure.getIntForUser(context.getContentResolver(), SUW_FRP_STATE, 0, userId) == 1;
    }
}
