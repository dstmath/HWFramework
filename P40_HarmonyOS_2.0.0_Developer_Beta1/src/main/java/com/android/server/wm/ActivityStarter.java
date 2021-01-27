package com.android.server.wm;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.app.WaitResult;
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
import android.os.BadParcelableException;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Pools;
import android.util.Slog;
import android.view.IApplicationToken;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.HeavyWeightSwitcherActivity;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.am.PendingIntentRecord;
import com.android.server.os.HwBootCheck;
import com.android.server.pm.InstantAppResolver;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityStackSupervisor;
import com.android.server.wm.LaunchParamsController;
import com.android.server.wm.WindowManagerService;
import com.huawei.server.wm.IHwActivityStarterEx;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ActivityStarter extends AbsActivityStarter {
    private static final String ACTION_HWCHOOSER = "com.huawei.intent.action.hwCHOOSER";
    private static final String DOCKBAR_PACKAGE_NAME = "com.huawei.hwdockbar";
    private static final String EXTRA_ALWAYS_USE_OPTION = "alwaysUseOption";
    private static final String HWPCSYSTEMUI_PACKAGE_NAME = "com.huawei.desktop.systemui";
    private static final String INCALLUI_ACTIVITY_CLASS_NAME = "com.android.incallui/.InCallActivity";
    private static final String INTELLIGENT_APPNAME = "com.huawei.intelligent";
    private static final int INVALID_LAUNCH_MODE = -1;
    private static final int INVALID_VALUE = -1;
    private static final String LAUNCHER_APPNAME = "com.huawei.android.launcher";
    private static final String MULTI_TASK_APP_PACKAGE = "com.huawei.hwdockbar.intent.extra.MULTIPLE_TASK_APP_PACKAGE";
    protected static final String SUW_FRP_STATE = "hw_suw_frp_state";
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_CONFIGURATION = ("ActivityTaskManager" + ActivityTaskManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_FOCUS = "ActivityTaskManager";
    private static final String TAG_RESULTS = "ActivityTaskManager";
    private static final String TAG_USER_LEAVING = "ActivityTaskManager";
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
    private boolean mRestrictedBgActivity;
    private TaskRecord mReuseTask;
    private final RootActivityContainer mRootActivityContainer;
    final ActivityTaskManagerService mService;
    private boolean mShouldSkipStartingWindow;
    private ActivityRecord mSourceRecord;
    private ActivityStack mSourceStack;
    private ActivityRecord mStartActivity;
    private int mStartFlags;
    final ActivityStackSupervisor mSupervisor;
    private ActivityStack mTargetStack;
    private IVoiceInteractor mVoiceInteractor;
    private IVoiceInteractionSession mVoiceSession;

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public interface Factory {
        ActivityStarter obtain();

        void recycle(ActivityStarter activityStarter);

        void setController(ActivityStartController activityStartController);
    }

    /* access modifiers changed from: package-private */
    public static class DefaultFactory implements Factory {
        private final int MAX_STARTER_COUNT = 3;
        private ActivityStartController mController;
        private ActivityStartInterceptor mInterceptor;
        private ActivityTaskManagerService mService;
        private Pools.SynchronizedPool<ActivityStarter> mStarterPool = new Pools.SynchronizedPool<>(3);
        private ActivityStackSupervisor mSupervisor;

        DefaultFactory(ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
            this.mService = service;
            this.mSupervisor = supervisor;
            this.mInterceptor = interceptor;
        }

        @Override // com.android.server.wm.ActivityStarter.Factory
        public void setController(ActivityStartController controller) {
            this.mController = controller;
        }

        @Override // com.android.server.wm.ActivityStarter.Factory
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

        @Override // com.android.server.wm.ActivityStarter.Factory
        public void recycle(ActivityStarter starter) {
            starter.reset(true);
            this.mStarterPool.release(starter);
        }
    }

    /* access modifiers changed from: private */
    public static class Request {
        private static final int DEFAULT_CALLING_PID = 0;
        private static final int DEFAULT_CALLING_UID = -1;
        static final int DEFAULT_REAL_CALLING_PID = 0;
        static final int DEFAULT_REAL_CALLING_UID = -1;
        ActivityInfo activityInfo;
        SafeActivityOptions activityOptions;
        boolean allowBackgroundActivityStart;
        boolean allowPendingRemoteAnimationRegistryLookup;
        boolean avoidMoveToFront;
        IApplicationThread caller;
        String callingPackage;
        int callingPid = 0;
        int callingUid = -1;
        boolean componentSpecified;
        Intent ephemeralIntent;
        int filterCallingUid;
        Configuration globalConfig;
        boolean ignoreTargetSecurity;
        TaskRecord inTask;
        Intent intent;
        boolean mayWait;
        PendingIntentRecord originatingPendingIntent;
        ActivityRecord[] outActivity;
        ProfilerInfo profilerInfo;
        int realCallingPid = 0;
        int realCallingUid = -1;
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
            this.realCallingUid = -1;
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
            this.originatingPendingIntent = null;
            this.allowBackgroundActivityStart = false;
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
            this.originatingPendingIntent = request.originatingPendingIntent;
            this.allowBackgroundActivityStart = request.allowBackgroundActivityStart;
        }
    }

    ActivityStarter(ActivityStartController controller, ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStartInterceptor interceptor) {
        this.mController = controller;
        this.mService = service;
        this.mRootActivityContainer = service.mRootActivityContainer;
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
        this.mRestrictedBgActivity = starter.mRestrictedBgActivity;
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
        ActivityRecord activityRecord;
        ActivityRecord[] activityRecordArr = this.mLastStartActivityRecord;
        if ((activityRecordArr[0] == null || !packageName.equals(activityRecordArr[0].packageName)) && ((activityRecord = this.mStartActivity) == null || !packageName.equals(activityRecord.packageName))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public int execute() {
        try {
            if (this.mRequest.mayWait) {
                return startActivityMayWait(this.mRequest.caller, this.mRequest.callingUid, this.mRequest.callingPackage, this.mRequest.realCallingPid, this.mRequest.realCallingUid, this.mRequest.intent, this.mRequest.resolvedType, this.mRequest.voiceSession, this.mRequest.voiceInteractor, this.mRequest.resultTo, this.mRequest.resultWho, this.mRequest.requestCode, this.mRequest.startFlags, this.mRequest.profilerInfo, this.mRequest.waitResult, this.mRequest.globalConfig, this.mRequest.activityOptions, this.mRequest.ignoreTargetSecurity, this.mRequest.userId, this.mRequest.inTask, this.mRequest.reason, this.mRequest.allowPendingRemoteAnimationRegistryLookup, this.mRequest.originatingPendingIntent, this.mRequest.allowBackgroundActivityStart);
            }
            int startActivity = startActivity(this.mRequest.caller, this.mRequest.intent, this.mRequest.ephemeralIntent, this.mRequest.resolvedType, this.mRequest.activityInfo, this.mRequest.resolveInfo, this.mRequest.voiceSession, this.mRequest.voiceInteractor, this.mRequest.resultTo, this.mRequest.resultWho, this.mRequest.requestCode, this.mRequest.callingPid, this.mRequest.callingUid, this.mRequest.callingPackage, this.mRequest.realCallingPid, this.mRequest.realCallingUid, this.mRequest.startFlags, this.mRequest.activityOptions, this.mRequest.ignoreTargetSecurity, this.mRequest.componentSpecified, this.mRequest.outActivity, this.mRequest.inTask, this.mRequest.reason, this.mRequest.allowPendingRemoteAnimationRegistryLookup, this.mRequest.originatingPendingIntent, this.mRequest.allowBackgroundActivityStart);
            onExecutionComplete();
            return startActivity;
        } finally {
            onExecutionComplete();
        }
    }

    /* access modifiers changed from: package-private */
    public int startResolvedActivity(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask) {
        try {
            this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunching(r.intent);
            this.mLastStartReason = "startResolvedActivity";
            this.mLastStartActivityTimeMs = System.currentTimeMillis();
            this.mLastStartActivityRecord[0] = r;
            this.mLastStartActivityResult = startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags, doResume, options, inTask, this.mLastStartActivityRecord, false);
            this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunched(this.mLastStartActivityResult, this.mLastStartActivityRecord[0]);
            return this.mLastStartActivityResult;
        } finally {
            onExecutionComplete();
        }
    }

    /* access modifiers changed from: package-private */
    public int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, SafeActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, TaskRecord inTask, String reason, boolean allowPendingRemoteAnimationRegistryLookup, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        if (!TextUtils.isEmpty(reason)) {
            this.mLastStartReason = reason;
            this.mLastStartActivityTimeMs = System.currentTimeMillis();
            ActivityRecord[] activityRecordArr = this.mLastStartActivityRecord;
            activityRecordArr[0] = null;
            this.mLastStartActivityResult = startActivity(caller, intent, ephemeralIntent, resolvedType, aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, activityRecordArr, inTask, allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, allowBackgroundActivityStart);
            if (this.mLastStartActivityResult != 0) {
                if (intent != null) {
                    Slog.e("ActivityTaskManager", "START {" + intent.toShortStringWithoutClip(true, true, true) + "} result: " + this.mLastStartActivityResult);
                }
            }
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

    /* JADX INFO: Multiple debug info for r15v9 android.os.Bundle: [D('tempLaunchBounds' android.graphics.Rect), D('verificationBundle' android.os.Bundle)] */
    /* JADX INFO: Multiple debug info for r9v10 'userId'  int: [D('checkedOptions' android.app.ActivityOptions), D('userId' int)] */
    /* JADX WARNING: Removed duplicated region for block: B:287:0x07da A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x07dc  */
    private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, SafeActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, TaskRecord inTask, boolean allowPendingRemoteAnimationRegistryLookup, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        int callingPid2;
        Bundle verificationBundle;
        int i;
        String str;
        int callingUid2;
        WindowProcessController callerApp;
        String str2;
        WindowProcessController callerApp2;
        String str3;
        ActivityRecord sourceRecord;
        int requestCode2;
        ActivityRecord resultRecord;
        String resultWho2;
        ActivityStack activityStack;
        int err;
        String callingPackage2;
        boolean restrictedBgActivity;
        ActivityRecord sourceRecord2;
        String str4;
        ActivityInfo activityInfo;
        String str5;
        WindowProcessController callerApp3;
        ActivityOptions checkedOptions;
        int templaunchFlags;
        WindowProcessController callerApp4;
        String callingPackage3;
        ActivityOptions checkedOptions2;
        boolean isIntercepted;
        TaskRecord inTask2;
        boolean abort;
        ActivityOptions checkedOptions3;
        int callingUid3;
        int callingPid3;
        String resolvedType2;
        ResolveInfo rInfo2;
        ActivityInfo aInfo2;
        Intent intent2;
        String str6;
        ActivityOptions checkedOptions4;
        int callingPid4;
        int userId;
        int callingPid5;
        ActivityInfo aInfo3;
        boolean z;
        Intent intent3;
        String resolvedType3;
        ResolveInfo rInfo3;
        boolean z2;
        int callingPid6;
        String resolvedType4;
        Bundle verificationBundle2;
        int callingUid4;
        Intent intent4;
        ActivityInfo aInfo4;
        ActivityRecord sourceRecord3;
        ActivityStarter activityStarter;
        ActivityRecord sourceRecord4;
        ActivityInfo aInfo5;
        ResolveInfo rInfo4;
        Intent intent5;
        String resolvedType5;
        String resolvedType6;
        ResolveInfo rInfo5;
        ActivityInfo aInfo6;
        ResolveInfo rInfo6;
        Bundle callerAppBundle;
        long j;
        Throwable th;
        String startActivityInfo;
        Throwable th2;
        String callingPackage4 = callingPackage;
        this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunching(intent);
        int err2 = 0;
        Bundle verificationBundle3 = options != null ? options.popAppVerificationBundle() : null;
        if (aInfo != null) {
            this.mService.mHwATMSEx.noteActivityStart(aInfo.applicationInfo.packageName, aInfo.processName, intent.getComponent() != null ? intent.getComponent().getClassName() : "NULL", 0, aInfo.applicationInfo.uid, true);
        }
        if (!this.mHwActivityStarterEx.isAbleToLaunchInVr(this.mService.mContext, intent, callingPackage4, aInfo)) {
            return 0;
        }
        if (caller != null) {
            WindowProcessController callerApp5 = this.mService.getProcessController(caller);
            if (callerApp5 != null) {
                int callingPid7 = callerApp5.getPid();
                int callingUid5 = callerApp5.mInfo.uid;
                long restoreCurId = Binder.clearCallingIdentity();
                try {
                    try {
                        verificationBundle = verificationBundle3;
                        i = 0;
                    } catch (Throwable th3) {
                        th2 = th3;
                        Binder.restoreCallingIdentity(restoreCurId);
                        throw th2;
                    }
                    try {
                        if (!this.mService.mHwATMSEx.isAllowToStartActivity(this.mService.mContext, callerApp5.mInfo.packageName, aInfo, this.mService.isSleepingLocked(), ((ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class)).getLastResumedActivity())) {
                            Binder.restoreCallingIdentity(restoreCurId);
                            return 0;
                        }
                        Binder.restoreCallingIdentity(restoreCurId);
                        callingUid2 = callingUid5;
                        callerApp = callerApp5;
                        str = "ActivityTaskManager";
                        callingPid2 = callingPid7;
                    } catch (Throwable th4) {
                        th2 = th4;
                        Binder.restoreCallingIdentity(restoreCurId);
                        throw th2;
                    }
                } catch (Throwable th5) {
                    th2 = th5;
                    Binder.restoreCallingIdentity(restoreCurId);
                    throw th2;
                }
            } else {
                verificationBundle = verificationBundle3;
                i = 0;
                str = "ActivityTaskManager";
                Slog.w(str, "Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting: " + intent.toString());
                err2 = -94;
                callingUid2 = callingUid;
                callingPid2 = callingPid;
                callerApp = callerApp5;
            }
        } else {
            str = "ActivityTaskManager";
            verificationBundle = verificationBundle3;
            i = 0;
            callingUid2 = callingUid;
            callerApp = null;
            callingPid2 = callingPid;
        }
        HwFreeFormManager.getInstance(this.mService.mContext).removeFloatListView();
        int userId2 = (aInfo == null || aInfo.applicationInfo == null) ? i : UserHandle.getUserId(aInfo.applicationInfo.uid);
        if (err2 == 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("START u");
            sb.append(userId2);
            sb.append(" {");
            str2 = " {";
            callerApp2 = callerApp;
            sb.append(intent.toShortStringWithoutClip(true, true, true));
            sb.append("} from uid ");
            sb.append(callingUid2);
            Slog.i(str, sb.toString());
            if (!this.mService.mActivityIdle) {
                if (intent.getComponent() != null) {
                    startActivityInfo = "START u" + userId2 + " " + intent.getComponent().toShortString() + " from uid " + callingUid2;
                } else {
                    startActivityInfo = "start activity";
                }
                HwBootCheck.addBootInfo(startActivityInfo);
            }
            if (this.mService.mHwATMSEx.showIncompatibleAppDialog(aInfo, callingPackage4)) {
                return WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT;
            }
            this.mSupervisor.recognitionMaliciousApp(caller, intent, userId2);
            if (ACTION_HWCHOOSER.equals(intent.getAction()) && intent.getBooleanExtra(EXTRA_ALWAYS_USE_OPTION, false) && !HWPCSYSTEMUI_PACKAGE_NAME.equals(callingPackage4)) {
                intent.putExtra(EXTRA_ALWAYS_USE_OPTION, false);
            }
            this.mHwActivityStarterEx.effectiveIawareToLaunchApp(intent, aInfo, this.mService.getActivityStartController().mCurActivityPkName);
        } else {
            str2 = " {";
            callerApp2 = callerApp;
        }
        ActivityRecord resultRecord2 = null;
        if (resultTo != null) {
            ActivityRecord sourceRecord5 = this.mRootActivityContainer.isInAnyStack(resultTo);
            if (ActivityTaskManagerDebugConfig.DEBUG_RESULTS) {
                StringBuilder sb2 = new StringBuilder();
                str3 = "START u";
                sb2.append("Will send result to ");
                sb2.append(resultTo);
                sb2.append(" ");
                sb2.append(sourceRecord5);
                Slog.v(str, sb2.toString());
            } else {
                str3 = "START u";
            }
            if (sourceRecord5 == null || requestCode < 0 || sourceRecord5.finishing) {
                sourceRecord = sourceRecord5;
            } else {
                resultRecord2 = sourceRecord5;
                sourceRecord = sourceRecord5;
            }
        } else {
            str3 = "START u";
            sourceRecord = null;
        }
        int launchFlags = intent.getFlags();
        if ((launchFlags & 33554432) == 0 || sourceRecord == null) {
            activityStack = null;
            resultWho2 = resultWho;
            requestCode2 = requestCode;
            resultRecord = resultRecord2;
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
                resultRecord = resultRecord3;
                resultWho2 = resultWho3;
                requestCode2 = requestCode3;
                callingPackage4 = sourceRecord.launchedFromPackage;
                activityStack = null;
            } else {
                resultRecord = resultRecord3;
                resultWho2 = resultWho3;
                requestCode2 = requestCode3;
                activityStack = null;
            }
        }
        if (err2 == 0 && intent.getComponent() == null) {
            err2 = -91;
        }
        if (err2 == 0 && aInfo == null) {
            err2 = -92;
        }
        if (!(err2 != 0 || sourceRecord == null || sourceRecord.getTaskRecord().voiceSession == null || (launchFlags & 268435456) != 0 || sourceRecord.info.applicationInfo.uid == aInfo.applicationInfo.uid)) {
            try {
                intent.addCategory("android.intent.category.VOICE");
                if (!this.mService.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                    Slog.w(str, "Activity being started in current voice task does not support voice: " + intent);
                    err2 = -97;
                }
            } catch (RemoteException e) {
                Slog.w(str, "Failure checking voice capabilities", e);
                err2 = -97;
            }
        }
        if (err2 != 0 || voiceSession == null) {
            err = err2;
        } else {
            try {
                if (!this.mService.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                    Slog.w(str, "Activity being started in new voice task does not support: " + intent);
                    err2 = -97;
                }
                err = err2;
            } catch (RemoteException e2) {
                Slog.w(str, "Failure checking voice capabilities", e2);
                err = -97;
            }
        }
        ActivityStack resultStack = resultRecord == null ? activityStack : resultRecord.getActivityStack();
        if (err != 0) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho2, requestCode2, 0, null);
            }
            SafeActivityOptions.abort(options);
            return err;
        }
        boolean abort2 = (!this.mSupervisor.checkStartAnyActivityPermission(intent, aInfo, resultWho2, requestCode2, callingPid2, callingUid2, callingPackage4, ignoreTargetSecurity, inTask != null, callerApp2, resultRecord, resultStack)) | (!this.mService.mIntentFirewall.checkStartActivity(intent, callingUid2, callingPid2, resolvedType, aInfo.applicationInfo)) | (!this.mService.getPermissionPolicyInternal().checkStartActivity(intent, callingUid2, callingPackage4));
        if (!abort2) {
            try {
                Trace.traceBegin(64, "shouldAbortBackgroundActivityStart");
                j = 64;
                callingPackage2 = callingPackage4;
                str4 = str3;
                str5 = str;
                sourceRecord2 = sourceRecord;
                activityInfo = aInfo;
                try {
                    boolean restrictedBgActivity2 = shouldAbortBackgroundActivityStart(callingUid2, callingPid2, callingPackage4, realCallingUid, realCallingPid, callerApp2, originatingPendingIntent, allowBackgroundActivityStart, intent);
                    Trace.traceEnd(64);
                    restrictedBgActivity = restrictedBgActivity2;
                } catch (Throwable th6) {
                    th = th6;
                    Trace.traceEnd(j);
                    throw th;
                }
            } catch (Throwable th7) {
                th = th7;
                j = 64;
                Trace.traceEnd(j);
                throw th;
            }
        } else {
            callingPackage2 = callingPackage4;
            str4 = str3;
            sourceRecord2 = sourceRecord;
            str5 = str;
            activityInfo = aInfo;
            restrictedBgActivity = false;
        }
        if (options != null) {
            callerApp3 = callerApp2;
            checkedOptions = options.getOptions(intent, activityInfo, callerApp3, this.mSupervisor);
        } else {
            callerApp3 = callerApp2;
            checkedOptions = null;
        }
        if (restrictedBgActivity) {
            if (this.mService.mHwATMSEx.shouldAbortSelfLaunchWhenReturnHome(activityInfo.applicationInfo.packageName, callingUid2, realCallingUid)) {
                ActivityOptions.abort(checkedOptions);
                Slog.d(str5, "Return home just recently, abort app self-launch:" + intent);
                return 100;
            }
        }
        if (this.mInTask == null && this.mSourceRecord == null) {
            templaunchFlags = launchFlags | 268435456;
        } else {
            templaunchFlags = launchFlags;
        }
        ActivityOptions checkedOptions5 = this.mService.mHwATMSEx.updateToHwFreeFormIfNeeded(intent, aInfo, inTask, templaunchFlags, resultRecord, checkedOptions);
        if (!abort2) {
            callerApp4 = callerApp3;
            if (this.mService.mHwATMSEx.shouldPreventStartActivity(aInfo, callingUid2, callingPid2, callingPackage2, userId2, intent, callerApp3, checkedOptions5, sourceRecord2)) {
                abort2 = true;
            }
        } else {
            callerApp4 = callerApp3;
        }
        if (allowPendingRemoteAnimationRegistryLookup) {
            callingPackage3 = callingPackage2;
            checkedOptions2 = this.mService.getActivityStartController().getPendingRemoteAnimationRegistry().overrideOptionsIfNeeded(callingPackage3, checkedOptions5);
        } else {
            callingPackage3 = callingPackage2;
            checkedOptions2 = checkedOptions5;
        }
        if (this.mService.mController != null) {
            try {
                boolean stoppedByController = !this.mService.mController.activityStarting(intent.cloneFilter(), activityInfo.applicationInfo.packageName);
                if (stoppedByController) {
                    Slog.w(str5, "ActivityStart aborted by Controller, isMonkey:" + this.mService.mControllerIsAMonkey);
                }
                abort2 |= stoppedByController;
            } catch (RemoteException e3) {
                this.mService.mController = null;
            }
        }
        if ("startActivityAsCaller".equals(this.mLastStartReason)) {
            intent.setCallingUid(callingUid2);
        } else if (!"PendingIntentRecord".equals(this.mLastStartReason) || intent.getCallingUid() == 0) {
            intent.setCallingUid(realCallingUid);
        }
        this.mInterceptor.setStates(userId2, realCallingPid, realCallingUid, startFlags, callingPackage3);
        this.mInterceptor.setSourceRecord(sourceRecord2);
        boolean isTempPendingShow = checkedOptions2 != null ? checkedOptions2.isPendingShow() : false;
        Rect tempLaunchBounds = checkedOptions2 != null ? checkedOptions2.getLaunchBounds() : null;
        float tempStackScale = checkedOptions2 != null ? checkedOptions2.getStackScale() : -1.0f;
        if (checkedOptions2 != null) {
            checkedOptions2.setPendingShow(false);
            if (isTempPendingShow) {
                checkedOptions2.setLaunchBounds(null);
                checkedOptions2.setStackScale(-1.0f);
            }
        }
        if (this.mInterceptor.intercept(intent, rInfo, aInfo, resolvedType, inTask, callingPid2, callingUid2, checkedOptions2)) {
            intent2 = this.mInterceptor.mIntent;
            ResolveInfo rInfo7 = this.mInterceptor.mRInfo;
            ActivityInfo aInfo7 = this.mInterceptor.mAInfo;
            resolvedType2 = this.mInterceptor.mResolvedType;
            TaskRecord inTask3 = this.mInterceptor.mInTask;
            callingPid3 = this.mInterceptor.mCallingPid;
            callingUid3 = this.mInterceptor.mCallingUid;
            ActivityOptions checkedOptions6 = this.mInterceptor.mActivityOptions;
            if (checkedOptions6 != null) {
                rInfo6 = rInfo7;
                if (checkedOptions6.getLaunchWindowingMode() != 105) {
                    aInfo6 = aInfo7;
                } else if (intent2 != null) {
                    aInfo6 = aInfo7;
                    if (this.mService.mHwATMSEx.isStartAppLock(intent2.getPackage(), intent2.getAction())) {
                        int taskId = 0;
                        if (!(callerApp4 == null || (callerAppBundle = callerApp4.getActivityOptionFromAppProcess()) == null)) {
                            taskId = callerAppBundle.getInt("key_task_id");
                        }
                        this.mService.mWindowManager.getPolicy().sendMessageHint(taskId);
                        Slog.i(str5, "sendMessageHint taskId: " + taskId);
                        inTask2 = inTask3;
                        isIntercepted = true;
                        abort = true;
                        rInfo2 = rInfo6;
                        aInfo2 = aInfo6;
                        checkedOptions3 = checkedOptions6;
                    }
                } else {
                    aInfo6 = aInfo7;
                }
            } else {
                rInfo6 = rInfo7;
                aInfo6 = aInfo7;
            }
            inTask2 = inTask3;
            isIntercepted = true;
            abort = abort2;
            rInfo2 = rInfo6;
            aInfo2 = aInfo6;
            checkedOptions3 = checkedOptions6;
        } else {
            intent2 = intent;
            resolvedType2 = resolvedType;
            aInfo2 = aInfo;
            rInfo2 = rInfo;
            isIntercepted = false;
            inTask2 = inTask;
            callingUid3 = callingUid2;
            abort = abort2;
            checkedOptions3 = checkedOptions2;
            callingPid3 = callingPid2;
        }
        if (checkedOptions3 != null) {
            checkedOptions3.setPendingShow(isTempPendingShow);
            if (isTempPendingShow) {
                checkedOptions3.setLaunchBounds(tempLaunchBounds);
                checkedOptions3.setStackScale(tempStackScale);
            }
        }
        if (abort) {
            if (!(resultRecord == null || resultStack == null)) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho2, requestCode2, 0, null);
            }
            ActivityOptions.abort(checkedOptions3);
            return WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT;
        }
        if (!this.mHwActivityStarterEx.isAbleToLaunchVideoActivity(this.mService.mContext, intent2)) {
            str6 = str5;
            if (callingPid3 == realCallingPid) {
                Slog.i(str6, "LaunchVideoActivity abort " + intent2 + " pid " + realCallingPid);
                SafeActivityOptions.abort(options);
                return 0;
            }
        } else {
            str6 = str5;
        }
        if (aInfo2 != null) {
            checkedOptions4 = checkedOptions3;
            userId = userId2;
            if (this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(aInfo2.packageName, userId)) {
                IIntentSender target = this.mService.getIntentSenderLocked(2, callingPackage3, callingUid3, userId, null, null, 0, new Intent[]{intent2}, new String[]{resolvedType2}, 1342177280, null);
                Intent newIntent = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                int flags = intent2.getFlags() | 8388608;
                if ((268959744 & flags) != 0) {
                    flags |= 134217728;
                }
                newIntent.setFlags(flags);
                newIntent.putExtra("android.intent.extra.PACKAGE_NAME", aInfo2.packageName);
                newIntent.putExtra("android.intent.extra.INTENT", new IntentSender(target));
                if (resultRecord != null) {
                    newIntent.putExtra("android.intent.extra.RESULT_NEEDED", true);
                }
                callingUid3 = realCallingUid;
                ResolveInfo rInfo8 = this.mSupervisor.resolveIntent(newIntent, null, userId, 0, computeResolveFilterUid(callingUid3, realCallingUid, this.mRequest.filterCallingUid));
                callingPid5 = startFlags;
                ActivityInfo aInfo8 = this.mSupervisor.resolveActivity(newIntent, rInfo8, callingPid5, null);
                if (ActivityTaskManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                    ActivityStack focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
                    rInfo5 = rInfo8;
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append(str4);
                    sb3.append(userId);
                    sb3.append(str2);
                    resolvedType6 = null;
                    z = true;
                    sb3.append(newIntent.toShortString(true, true, true, false));
                    sb3.append("} from uid ");
                    sb3.append(callingUid3);
                    sb3.append(" on display ");
                    sb3.append(focusedStack == null ? 0 : focusedStack.mDisplayId);
                    Slog.i(str6, sb3.toString());
                } else {
                    rInfo5 = rInfo8;
                    resolvedType6 = null;
                    z = true;
                }
                rInfo3 = rInfo5;
                callingPid4 = realCallingPid;
                intent3 = newIntent;
                aInfo3 = aInfo8;
                resolvedType3 = resolvedType6;
                if (rInfo3 != null || rInfo3.auxiliaryInfo == null) {
                    z2 = z;
                    activityStarter = this;
                    verificationBundle2 = verificationBundle;
                    sourceRecord3 = sourceRecord2;
                    resolvedType4 = resolvedType3;
                    intent4 = intent3;
                    aInfo4 = aInfo3;
                    callingUid4 = callingUid3;
                    callingPid6 = callingPid4;
                } else {
                    activityStarter = this;
                    verificationBundle2 = verificationBundle;
                    sourceRecord3 = sourceRecord2;
                    z2 = true;
                    Intent intent6 = createLaunchIntent(rInfo3.auxiliaryInfo, ephemeralIntent, callingPackage3, verificationBundle2, resolvedType3, userId);
                    resolvedType4 = null;
                    intent4 = intent6;
                    aInfo4 = activityStarter.mSupervisor.resolveActivity(intent6, rInfo3, callingPid5, null);
                    callingUid4 = realCallingUid;
                    callingPid6 = realCallingPid;
                }
                Slog.i(str6, "ActivityRecord info: " + aInfo4);
                if (!isNeedToStartExSplash(intent4, callingPackage3, requestCode2, restrictedBgActivity, isIntercepted, aInfo4, checkedOptions4, callingUid4, userId, resolvedType4)) {
                    return 0;
                }
                ActivityTaskManagerService activityTaskManagerService = activityStarter.mService;
                ActivityRecord r = HwServiceFactory.createActivityRecord(activityTaskManagerService, callerApp4, callingPid6, callingUid4, callingPackage3, intent4, resolvedType4, aInfo4, activityTaskManagerService.getGlobalConfiguration(), resultRecord, resultWho2, requestCode2, componentSpecified, voiceSession != null ? z2 : false, activityStarter.mSupervisor, checkedOptions4, sourceRecord3);
                if (outActivity != null) {
                    outActivity[0] = r;
                }
                activityStarter.mService.mHwATMSEx.setCallingPkg(callingPackage3);
                if (r.appTimeTracker == null) {
                    sourceRecord4 = sourceRecord3;
                    if (sourceRecord4 != null) {
                        r.appTimeTracker = sourceRecord4.appTimeTracker;
                    }
                } else {
                    sourceRecord4 = sourceRecord3;
                }
                ActivityStack stack = activityStarter.mRootActivityContainer.getTopDisplayFocusedStack();
                if (voiceSession == null) {
                    if (stack.getResumedActivity() != null) {
                        if (stack.getResumedActivity().info.applicationInfo.uid == realCallingUid) {
                        }
                    }
                    if (!activityStarter.mService.checkAppSwitchAllowedLocked(callingPid6, callingUid4, realCallingPid, realCallingUid, "Activity start")) {
                        if (!restrictedBgActivity || !activityStarter.handleBackgroundActivityAbort(r)) {
                            activityStarter.mController.addPendingActivityLaunch(new ActivityStackSupervisor.PendingActivityLaunch(r, sourceRecord4, startFlags, stack, callerApp4));
                        }
                        ActivityOptions.abort(checkedOptions4);
                        return 100;
                    }
                }
                activityStarter.mService.onStartActivitySetDidAppSwitch();
                activityStarter.mController.doPendingActivityLaunches(false);
                int res = startActivity(r, sourceRecord4, voiceSession, voiceInteractor, startFlags, true, checkedOptions4, inTask2, outActivity, restrictedBgActivity);
                activityStarter.mSupervisor.getActivityMetricsLogger().notifyActivityLaunched(res, outActivity[0]);
                return res;
            }
            intent5 = intent2;
            aInfo5 = aInfo2;
            rInfo4 = rInfo2;
            resolvedType5 = resolvedType2;
            callingPid4 = callingPid3;
            z = true;
            callingPid5 = startFlags;
        } else {
            intent5 = intent2;
            aInfo5 = aInfo2;
            rInfo4 = rInfo2;
            callingPid4 = callingPid3;
            checkedOptions4 = checkedOptions3;
            z = true;
            userId = userId2;
            callingPid5 = startFlags;
            resolvedType5 = resolvedType2;
        }
        aInfo3 = aInfo5;
        resolvedType3 = resolvedType5;
        intent3 = intent5;
        rInfo3 = rInfo4;
        if (rInfo3 != null) {
        }
        z2 = z;
        activityStarter = this;
        verificationBundle2 = verificationBundle;
        sourceRecord3 = sourceRecord2;
        resolvedType4 = resolvedType3;
        intent4 = intent3;
        aInfo4 = aInfo3;
        callingUid4 = callingUid3;
        callingPid6 = callingPid4;
        Slog.i(str6, "ActivityRecord info: " + aInfo4);
        if (!isNeedToStartExSplash(intent4, callingPackage3, requestCode2, restrictedBgActivity, isIntercepted, aInfo4, checkedOptions4, callingUid4, userId, resolvedType4)) {
        }
    }

    private void updateToHwFreeFormAnimationIfNeed(ActivityRecord r, ActivityRecord reusedActivity, ActivityRecord sourceRecord, ActivityOptions options) {
        ActivityRecord intentActivity;
        if (r != null && options != null && options.getAnimationType() == 13) {
            if (sourceRecord == null || !sourceRecord.nowVisible || sourceRecord.packageName == null || !sourceRecord.packageName.equals(r.packageName)) {
                boolean isNeedToUpdate = false;
                if (reusedActivity != null) {
                    isNeedToUpdate = reusedActivity.inHwFreeFormWindowingMode();
                } else if ((3 == this.mLaunchMode || (this.mLaunchFlags & 4096) != 0) && (intentActivity = this.mRootActivityContainer.findTask(this.mStartActivity, this.mPreferredDisplayId)) != null) {
                    isNeedToUpdate = intentActivity.inHwFreeFormWindowingMode();
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                    Slog.i("ActivityTaskManager", "updateToHwFreeFormAnimation, record:" + r + " isNeedToUpdate:" + isNeedToUpdate + " reusedActivity:" + reusedActivity + " sourceRecord:" + sourceRecord + " mPreferredDisplayId:" + this.mPreferredDisplayId);
                }
                if (isNeedToUpdate) {
                    ActivityDisplay display = this.mRootActivityContainer.getActivityDisplay(this.mPreferredDisplayId);
                    if (display != null) {
                        display.mDisplayContent.mAppTransition.clear();
                    }
                    r.updateOptionsLocked(ActivityOptions.makeCustomAnimation(this.mService.mContext, 34209874, 0));
                }
            }
        }
    }

    private boolean isNeedToStartExSplash(Intent intent, String callingPackage, int requestCode, boolean isRestrictedBgActivity, boolean isIntercepted, ActivityInfo info, ActivityOptions checkedOptions, int callingUid, int userId, String resolvedType) {
        Throwable th;
        if (intent != null) {
            if (intent.getComponent() != null) {
                String packageName = intent.getComponent().getPackageName();
                Bundle checkBundle = new Bundle();
                checkBundle.putString("exsplash_callingpackage", callingPackage);
                checkBundle.putString("exsplash_package", packageName);
                checkBundle.putInt("exsplash_requestcode", requestCode);
                checkBundle.putBoolean("exsplash_isintercepted", isRestrictedBgActivity || isIntercepted);
                checkBundle.putParcelable("exsplash_info", info);
                checkBundle.putInt("exsplash_userId", userId);
                if (!this.mService.mHwATMSEx.isExSplashEnable(checkBundle)) {
                    return false;
                }
                long restoreCurId = Binder.clearCallingIdentity();
                try {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("android.intent.extra.INTENT", intent);
                    bundle.putString("exsplash_callingpackage", callingPackage);
                    try {
                        bundle.putInt("exsplash_callingUid", callingUid);
                        bundle.putInt("exsplash_userId", userId);
                        try {
                            bundle.putString("exsplash_resolvedType", resolvedType);
                            try {
                                this.mService.mHwATMSEx.startExSplash(bundle, checkedOptions);
                                Binder.restoreCallingIdentity(restoreCurId);
                                return true;
                            } catch (Throwable th2) {
                                th = th2;
                                Binder.restoreCallingIdentity(restoreCurId);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            Binder.restoreCallingIdentity(restoreCurId);
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        Binder.restoreCallingIdentity(restoreCurId);
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    Binder.restoreCallingIdentity(restoreCurId);
                    throw th;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldAbortBackgroundActivityStart(int callingUid, int callingPid, String callingPackage, int realCallingUid, int realCallingPid, WindowProcessController callerApp, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart, Intent intent) {
        boolean shouldAbortSelfLaunchWhenReturnHome;
        int realCallingUidProcState;
        boolean realCallingUidHasAnyVisibleWindow;
        boolean isRealCallingUidForeground;
        boolean isRealCallingUidPersistentSystemProcess;
        int realCallingUidProcState2;
        int callerAppUid;
        WindowProcessController callerApp2;
        int callerAppUid2;
        WindowProcessController callerApp3;
        boolean shouldAbortSelfLaunchWhenReturnHome2;
        int callingAppId = UserHandle.getAppId(callingUid);
        if (callingUid != 0 && callingAppId != 1000) {
            if (callingAppId != 1027) {
                int callingUidProcState = this.mService.getUidState(callingUid);
                boolean callingUidHasAnyVisibleWindow = this.mService.mWindowManager.mRoot.isAnyNonToastWindowVisibleForUid(callingUid);
                boolean isCallingUidForeground = callingUidHasAnyVisibleWindow || callingUidProcState == 2 || callingUidProcState == 4;
                boolean isCallingUidPersistentSystemProcess = callingUidProcState <= 1;
                if (callingUidHasAnyVisibleWindow || isCallingUidPersistentSystemProcess) {
                    boolean shouldAbortSelfLaunchWhenReturnHome3 = this.mService.mHwATMSEx.shouldAbortSelfLaunchWhenReturnHome(callingPackage, callingUid, realCallingUid);
                    if (!shouldAbortSelfLaunchWhenReturnHome3) {
                        Slog.i("ActivityTaskManager", "Background activity start: don't abort if calling uid has any visible window or is persistent system process");
                        return false;
                    }
                    shouldAbortSelfLaunchWhenReturnHome = shouldAbortSelfLaunchWhenReturnHome3;
                } else {
                    shouldAbortSelfLaunchWhenReturnHome = false;
                }
                if (callingUid == realCallingUid) {
                    realCallingUidProcState = callingUidProcState;
                } else {
                    realCallingUidProcState = this.mService.getUidState(realCallingUid);
                }
                if (callingUid == realCallingUid) {
                    realCallingUidHasAnyVisibleWindow = callingUidHasAnyVisibleWindow;
                } else {
                    realCallingUidHasAnyVisibleWindow = this.mService.mWindowManager.mRoot.isAnyNonToastWindowVisibleForUid(realCallingUid);
                }
                if (callingUid == realCallingUid) {
                    isRealCallingUidForeground = isCallingUidForeground;
                } else {
                    isRealCallingUidForeground = realCallingUidHasAnyVisibleWindow || realCallingUidProcState == 2;
                }
                int realCallingAppId = UserHandle.getAppId(realCallingUid);
                if (callingUid == realCallingUid) {
                    isRealCallingUidPersistentSystemProcess = isCallingUidPersistentSystemProcess;
                } else {
                    isRealCallingUidPersistentSystemProcess = realCallingAppId == 1000 || realCallingUidProcState <= 1;
                }
                if (realCallingUid == callingUid) {
                    realCallingUidProcState2 = realCallingUidProcState;
                } else if (realCallingUidHasAnyVisibleWindow) {
                    Slog.i("ActivityTaskManager", "Background activity start: don't abort if the realCallingUid has a visible window");
                    return false;
                } else {
                    realCallingUidProcState2 = realCallingUidProcState;
                    if (isRealCallingUidPersistentSystemProcess && allowBackgroundActivityStart) {
                        Slog.i("ActivityTaskManager", "Background activity start: don't abort if the realCallingUid is a persistent system process and the IntentSender was whitelisted to start an activity");
                        return false;
                    } else if (this.mService.isAssociatedCompanionApp(UserHandle.getUserId(realCallingUid), realCallingUid)) {
                        Slog.i("ActivityTaskManager", "Background activity start: don't abort if the realCallingUid is an associated companion app");
                        return false;
                    }
                }
                ActivityTaskManagerService activityTaskManagerService = this.mService;
                if (ActivityTaskManagerService.checkPermission("android.permission.START_ACTIVITIES_FROM_BACKGROUND", callingPid, callingUid) == 0) {
                    Slog.i("ActivityTaskManager", "Background activity start for " + callingPackage + " allowed because START_ACTIVITIES_FROM_BACKGROUND permission is granted");
                    return false;
                } else if (this.mSupervisor.mRecentTasks.isCallerRecents(callingUid)) {
                    Slog.i("ActivityTaskManager", "Background activity start: don't abort if the caller has the same uid as the recents component");
                    return false;
                } else if (this.mService.isDeviceOwner(callingUid)) {
                    Slog.i("ActivityTaskManager", "Background activity start: don't abort if the callingUid is the device owner");
                    return false;
                } else {
                    if (this.mService.isAssociatedCompanionApp(UserHandle.getUserId(callingUid), callingUid)) {
                        Slog.i("ActivityTaskManager", "Background activity start: don't abort if the callingUid has companion device");
                        return false;
                    }
                    if (callerApp == null) {
                        callerApp2 = this.mService.getProcessController(realCallingPid, realCallingUid);
                        callerAppUid = realCallingUid;
                    } else {
                        callerApp2 = callerApp;
                        callerAppUid = callingUid;
                    }
                    if (callerApp2 == null) {
                        callerApp3 = callerApp2;
                        callerAppUid2 = callerAppUid;
                        shouldAbortSelfLaunchWhenReturnHome2 = true;
                    } else if (callerApp2.areBackgroundActivityStartsAllowed(shouldAbortSelfLaunchWhenReturnHome)) {
                        Slog.i("ActivityTaskManager", "Background activity start: original " + callerApp2 + "is BackgroundActivityStartsAllowed");
                        return false;
                    } else {
                        ArraySet<WindowProcessController> uidProcesses = this.mService.mProcessMap.getProcesses(callerAppUid);
                        if (uidProcesses != null) {
                            shouldAbortSelfLaunchWhenReturnHome2 = true;
                            callerAppUid2 = callerAppUid;
                            int i = uidProcesses.size() - 1;
                            while (i >= 0) {
                                WindowProcessController proc = uidProcesses.valueAt(i);
                                if (proc == callerApp2 || !proc.areBackgroundActivityStartsAllowed(shouldAbortSelfLaunchWhenReturnHome)) {
                                    i--;
                                    callerApp2 = callerApp2;
                                    uidProcesses = uidProcesses;
                                } else {
                                    Slog.i("ActivityTaskManager", "Background activity start: " + proc + " is BackgroundActivityStartsAllowed");
                                    return false;
                                }
                            }
                            callerApp3 = callerApp2;
                        } else {
                            callerApp3 = callerApp2;
                            callerAppUid2 = callerAppUid;
                            shouldAbortSelfLaunchWhenReturnHome2 = true;
                        }
                    }
                    if (!this.mService.hasSystemAlertWindowPermission(callingUid, callingPid, callingPackage) || shouldAbortSelfLaunchWhenReturnHome) {
                        Slog.w("ActivityTaskManager", "Background activity start [callingPackage: " + callingPackage + "; callingUid: " + callingUid + "; isCallingUidForeground: " + isCallingUidForeground + "; isCallingUidPersistentSystemProcess: " + isCallingUidPersistentSystemProcess + "; realCallingUid: " + realCallingUid + "; isRealCallingUidForeground: " + isRealCallingUidForeground + "; isRealCallingUidPersistentSystemProcess: " + isRealCallingUidPersistentSystemProcess + "; originatingPendingIntent: " + originatingPendingIntent + "; isBgStartWhitelisted: " + allowBackgroundActivityStart + "; intent: " + intent + "; callerApp: " + callerApp3 + "; shouldAbortSelfLaunchWhenReturnHome: " + shouldAbortSelfLaunchWhenReturnHome + "]");
                        if (!this.mService.isActivityStartsLoggingEnabled()) {
                            return shouldAbortSelfLaunchWhenReturnHome2;
                        }
                        this.mSupervisor.getActivityMetricsLogger().logAbortedBgActivityStart(intent, callerApp3, callingUid, callingPackage, callingUidProcState, callingUidHasAnyVisibleWindow, realCallingUid, realCallingUidProcState2, realCallingUidHasAnyVisibleWindow, originatingPendingIntent != null ? shouldAbortSelfLaunchWhenReturnHome2 : false);
                        return shouldAbortSelfLaunchWhenReturnHome2;
                    }
                    Slog.w("ActivityTaskManager", "Background activity start for " + callingPackage + " allowed because SYSTEM_ALERT_WINDOW permission is granted.");
                    return false;
                }
            }
        }
        Slog.i("ActivityTaskManager", "Background activity start: don't abort for the most important UIDs");
        return false;
    }

    private Intent createLaunchIntent(AuxiliaryResolveInfo auxiliaryResponse, Intent originalIntent, String callingPackage, Bundle verificationBundle, String resolvedType, int userId) {
        if (auxiliaryResponse != null && auxiliaryResponse.needsPhaseTwo) {
            this.mService.getPackageManagerInternalLocked().requestInstantAppResolutionPhaseTwo(auxiliaryResponse, originalIntent, resolvedType, callingPackage, verificationBundle, userId);
        }
        Intent sanitizeIntent = InstantAppResolver.sanitizeIntent(originalIntent);
        List list = null;
        Intent intent = auxiliaryResponse == null ? null : auxiliaryResponse.failureIntent;
        ComponentName componentName = auxiliaryResponse == null ? null : auxiliaryResponse.installFailureActivity;
        String str = auxiliaryResponse == null ? null : auxiliaryResponse.token;
        boolean z = auxiliaryResponse != null && auxiliaryResponse.needsPhaseTwo;
        if (auxiliaryResponse != null) {
            list = auxiliaryResponse.filters;
        }
        return InstantAppResolver.buildEphemeralInstallerIntent(originalIntent, sanitizeIntent, intent, callingPackage, verificationBundle, resolvedType, userId, componentName, str, z, list);
    }

    /* access modifiers changed from: package-private */
    public void postStartActivityProcessing(ActivityRecord r, int result, ActivityStack startedActivityStack) {
        ActivityStack homeStack;
        if (!ActivityManager.isStartResultFatalError(result)) {
            this.mSupervisor.reportWaitingActivityLaunchedIfNeeded(r, result);
            if (startedActivityStack != null) {
                boolean clearedTask = (this.mLaunchFlags & 268468224) == 268468224 && this.mReuseTask != null;
                if (result == 2 || result == 3 || clearedTask) {
                    int windowingMode = startedActivityStack.getWindowingMode();
                    if (windowingMode == 2) {
                        this.mService.getTaskChangeNotificationController().notifyPinnedActivityRestartAttempt(clearedTask);
                    } else if (windowingMode == 3 && (homeStack = startedActivityStack.getDisplay().getHomeStack()) != null && homeStack.shouldBeVisible(null)) {
                        this.mService.mWindowManager.showRecentApps();
                    }
                }
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v16, resolved type: com.android.server.wm.ActivityTaskManagerService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v15, types: [boolean] */
    /* JADX WARN: Type inference failed for: r0v58 */
    /* JADX WARN: Type inference failed for: r0v89 */
    /* JADX WARN: Type inference failed for: r0v98 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:104:0x0200  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0221 A[SYNTHETIC, Splitter:B:107:0x0221] */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x04ab  */
    /* JADX WARNING: Removed duplicated region for block: B:193:0x04d7 A[Catch:{ all -> 0x04d2, all -> 0x056a }] */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x04ea A[SYNTHETIC, Splitter:B:197:0x04ea] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0124  */
    /* JADX WARNING: Removed duplicated region for block: B:64:0x0145  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x016e  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x018d  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0196  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x01c3 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:89:0x01c6  */
    /* JADX WARNING: Unknown variable types count: 1 */
    public int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, int requestRealCallingPid, int requestRealCallingUid, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration globalConfig, SafeActivityOptions options, boolean ignoreTargetSecurity, int userId, TaskRecord inTask, String reason, boolean allowPendingRemoteAnimationRegistryLookup, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        int realCallingPid;
        int realCallingUid;
        int callingUid2;
        int callingUid3;
        boolean componentSpecified;
        int realCallingUid2;
        ResolveInfo rInfo;
        ActivityInfo aInfo;
        WindowManagerGlobalLock windowManagerGlobalLock;
        Throwable th;
        ActivityStack stack;
        boolean z;
        boolean componentSpecified2;
        int callingUid4;
        int callingPid;
        ResolveInfo rInfo2;
        ActivityInfo aInfo2;
        ActivityStack stack2;
        String resolvedType2;
        Intent intent2;
        IApplicationThread caller2;
        int realCallingUid3;
        ?? r0;
        ActivityRecord[] outRecord;
        int res;
        ActivityStarter activityStarter;
        ActivityRecord r;
        ActivityInfo aInfo3;
        ResolveInfo rInfo3;
        WindowProcessController heavy;
        int appCallingUid;
        ActivityStack stack3;
        int callingUid5;
        int callingPid2;
        ActivityInfo aInfo4;
        ResolveInfo rInfo4;
        Throwable th2;
        UserInfo parent;
        boolean profileLockedAndParentUnlockingOrUnlocked;
        if (intent == null || !intent.hasFileDescriptors()) {
            this.mSupervisor.getActivityMetricsLogger().notifyActivityLaunching(intent);
            boolean componentSpecified3 = intent.getComponent() != null;
            if (requestRealCallingPid != 0) {
                realCallingPid = requestRealCallingPid;
            } else {
                realCallingPid = Binder.getCallingPid();
            }
            if (requestRealCallingUid != -1) {
                realCallingUid = requestRealCallingUid;
            } else {
                realCallingUid = Binder.getCallingUid();
            }
            if (callingUid >= 0) {
                callingUid2 = callingUid;
                callingUid3 = -1;
            } else if (caller == null) {
                callingUid2 = realCallingUid;
                callingUid3 = realCallingPid;
            } else {
                callingUid3 = -1;
                callingUid2 = -1;
            }
            Intent ephemeralIntent = new Intent(intent);
            Intent intent3 = new Intent(intent);
            if (componentSpecified3 && ((!"android.intent.action.VIEW".equals(intent3.getAction()) || intent3.getData() != null) && !"android.intent.action.INSTALL_INSTANT_APP_PACKAGE".equals(intent3.getAction()) && !"android.intent.action.RESOLVE_INSTANT_APP_PACKAGE".equals(intent3.getAction()) && this.mService.getPackageManagerInternalLocked().isInstantAppInstallerComponent(intent3.getComponent()))) {
                intent3.setComponent(null);
                componentSpecified3 = false;
            }
            ResolveInfo rInfo5 = this.mSupervisor.resolveIntent(intent3, resolvedType, userId, 0, computeResolveFilterUid(callingUid2, realCallingUid, this.mRequest.filterCallingUid));
            if (standardizeHomeIntent(rInfo5, intent3)) {
                componentSpecified = false;
            } else {
                componentSpecified = componentSpecified3;
            }
            if (rInfo5 == null) {
                UserInfo userInfo = this.mSupervisor.getUserInfo(userId);
                if (userInfo == null) {
                    rInfo4 = rInfo5;
                    realCallingUid2 = realCallingUid;
                } else if (userInfo.isManagedProfile() || userInfo.isClonedProfile()) {
                    UserManager userManager = UserManager.get(this.mService.mContext);
                    long token = Binder.clearCallingIdentity();
                    try {
                        parent = userManager.getProfileParent(userId);
                        profileLockedAndParentUnlockingOrUnlocked = false;
                        Binder.restoreCallingIdentity(token);
                        if (!profileLockedAndParentUnlockingOrUnlocked) {
                        }
                    } catch (Throwable th3) {
                        th2 = th3;
                        Binder.restoreCallingIdentity(token);
                        throw th2;
                    }
                    if (parent != null) {
                        try {
                        } catch (Throwable th4) {
                            th2 = th4;
                            Binder.restoreCallingIdentity(token);
                            throw th2;
                        }
                        if (userManager.isUserUnlockingOrUnlocked(parent.id) && !userManager.isUserUnlockingOrUnlocked(userId)) {
                            profileLockedAndParentUnlockingOrUnlocked = true;
                            Binder.restoreCallingIdentity(token);
                            if (!profileLockedAndParentUnlockingOrUnlocked) {
                                realCallingUid2 = realCallingUid;
                                rInfo = this.mSupervisor.resolveIntent(intent3, resolvedType, userId, 786432, computeResolveFilterUid(callingUid2, realCallingUid, this.mRequest.filterCallingUid));
                                aInfo = this.mSupervisor.resolveActivity(intent3, rInfo, startFlags, profilerInfo);
                                if (aInfo != null) {
                                    this.mHwActivityStarterEx.preloadApplication(aInfo.applicationInfo, callingPackage);
                                }
                                if (!(aInfo == null || aInfo.applicationInfo == null || callingPackage == null || !callingPackage.equals(aInfo.applicationInfo.packageName))) {
                                    Jlog.d(335, aInfo.applicationInfo.packageName + "/" + (intent3.getComponent() == null ? intent3.getComponent().getClassName() : "NULL"), "");
                                }
                                if (!this.mHwActivityStarterEx.isAppDisabledByMdmNoComponent(aInfo, intent3, resolvedType, this.mSupervisor)) {
                                    return -92;
                                }
                                WindowManagerGlobalLock windowManagerGlobalLock2 = this.mService.mGlobalLock;
                                synchronized (windowManagerGlobalLock2) {
                                    try {
                                        WindowManagerService.boostPriorityForLockedSection();
                                        stack = this.mRootActivityContainer.getTopDisplayFocusedStack();
                                        z = false;
                                        stack.mConfigWillChange = z;
                                        if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                        }
                                        long origId = Binder.clearCallingIdentity();
                                        if (aInfo != null) {
                                        }
                                        windowManagerGlobalLock = windowManagerGlobalLock2;
                                        stack2 = stack;
                                        aInfo3 = aInfo;
                                        rInfo3 = rInfo;
                                        realCallingUid3 = realCallingUid2;
                                        heavy = null;
                                        resolvedType2 = resolvedType;
                                        callingPid = callingUid3;
                                        intent2 = intent3;
                                        callingUid4 = callingUid2;
                                        rInfo2 = rInfo3;
                                        aInfo2 = aInfo3;
                                        componentSpecified2 = componentSpecified;
                                        caller2 = caller;
                                        r0 = heavy;
                                        try {
                                            outRecord = new ActivityRecord[1];
                                            try {
                                                res = startActivity(caller2, intent2, ephemeralIntent, resolvedType2, aInfo2, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid4, callingPackage, realCallingPid, realCallingUid3, startFlags, options, ignoreTargetSecurity, componentSpecified2, outRecord, inTask, reason, allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, allowBackgroundActivityStart);
                                                Binder.restoreCallingIdentity(origId);
                                                if (!stack2.mConfigWillChange) {
                                                }
                                                ActivityMetricsLogger activityMetricsLogger = activityStarter.mSupervisor.getActivityMetricsLogger();
                                                char c = r0 == true ? 1 : 0;
                                                char c2 = r0 == true ? 1 : 0;
                                                char c3 = r0 == true ? 1 : 0;
                                                char c4 = r0 == true ? 1 : 0;
                                                char c5 = r0 == true ? 1 : 0;
                                                char c6 = r0 == true ? 1 : 0;
                                                char c7 = r0 == true ? 1 : 0;
                                                char c8 = r0 == true ? 1 : 0;
                                                activityMetricsLogger.notifyActivityLaunched(res, outRecord[c]);
                                                if (outResult != null) {
                                                }
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                return res;
                                            } catch (Throwable th5) {
                                                th = th5;
                                                while (true) {
                                                    try {
                                                        break;
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                    }
                                                }
                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        } catch (Throwable th7) {
                                            th = th7;
                                            while (true) {
                                                break;
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    } catch (Throwable th8) {
                                        th = th8;
                                        windowManagerGlobalLock = windowManagerGlobalLock2;
                                        while (true) {
                                            break;
                                        }
                                        WindowManagerService.resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                    if (globalConfig != null) {
                                        try {
                                        } catch (Throwable th9) {
                                            th = th9;
                                            windowManagerGlobalLock = windowManagerGlobalLock2;
                                            while (true) {
                                                break;
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                        if (this.mService.getGlobalConfiguration().diff(globalConfig) != 0) {
                                            z = true;
                                            stack.mConfigWillChange = z;
                                            if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                                Slog.v(TAG_CONFIGURATION, "Starting activity when config will change = " + stack.mConfigWillChange);
                                            }
                                            long origId2 = Binder.clearCallingIdentity();
                                            if (aInfo != null) {
                                                try {
                                                } catch (Throwable th10) {
                                                    th = th10;
                                                    windowManagerGlobalLock = windowManagerGlobalLock2;
                                                    while (true) {
                                                        break;
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    throw th;
                                                }
                                                if ((aInfo.applicationInfo.privateFlags & 2) != 0 && this.mService.mHasHeavyWeightFeature) {
                                                    if (aInfo.processName.equals(aInfo.applicationInfo.packageName)) {
                                                        WindowProcessController heavy2 = this.mService.mHeavyWeightProcess;
                                                        if (heavy2 == null) {
                                                            windowManagerGlobalLock = windowManagerGlobalLock2;
                                                            stack2 = stack;
                                                            aInfo3 = aInfo;
                                                            rInfo3 = rInfo;
                                                            realCallingUid3 = realCallingUid2;
                                                            heavy = null;
                                                        } else if (heavy2.mInfo.uid != aInfo.applicationInfo.uid || !heavy2.mName.equals(aInfo.processName)) {
                                                            if (caller != null) {
                                                                WindowProcessController callerApp = this.mService.getProcessController(caller);
                                                                if (callerApp != null) {
                                                                    stack3 = stack;
                                                                    appCallingUid = callerApp.mInfo.uid;
                                                                } else {
                                                                    Slog.w("ActivityTaskManager", "Unable to find app for caller " + caller + " (pid=" + callingUid3 + ") when starting: " + intent3.toString());
                                                                    SafeActivityOptions.abort(options);
                                                                }
                                                            } else {
                                                                stack3 = stack;
                                                                appCallingUid = callingUid2;
                                                            }
                                                            IIntentSender target = this.mService.getIntentSenderLocked(2, "android", appCallingUid, userId, null, null, 0, new Intent[]{intent3}, new String[]{resolvedType}, 1342177280, null);
                                                            Intent newIntent = new Intent();
                                                            if (requestCode >= 0) {
                                                                try {
                                                                    newIntent.putExtra("has_result", true);
                                                                } catch (Throwable th11) {
                                                                    th = th11;
                                                                    windowManagerGlobalLock = windowManagerGlobalLock2;
                                                                }
                                                            }
                                                            newIntent.putExtra("intent", new IntentSender(target));
                                                            heavy2.updateIntentForHeavyWeightActivity(newIntent);
                                                            newIntent.putExtra("new_app", aInfo.packageName);
                                                            newIntent.setFlags(intent3.getFlags());
                                                            newIntent.setClassName("android", HeavyWeightSwitcherActivity.class.getName());
                                                            try {
                                                                callingUid5 = Binder.getCallingUid();
                                                                callingPid2 = Binder.getCallingPid();
                                                                componentSpecified = true;
                                                                windowManagerGlobalLock = windowManagerGlobalLock2;
                                                                stack2 = stack3;
                                                                r0 = 0;
                                                                r0 = 0;
                                                            } catch (Throwable th12) {
                                                                th = th12;
                                                                windowManagerGlobalLock = windowManagerGlobalLock2;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw th;
                                                            }
                                                            try {
                                                                ResolveInfo rInfo6 = this.mSupervisor.resolveIntent(newIntent, null, userId, 0, computeResolveFilterUid(callingUid5, realCallingUid2, this.mRequest.filterCallingUid));
                                                                if (rInfo6 != null) {
                                                                    try {
                                                                        aInfo4 = rInfo6.activityInfo;
                                                                    } catch (Throwable th13) {
                                                                        th = th13;
                                                                    }
                                                                } else {
                                                                    aInfo4 = null;
                                                                }
                                                                if (aInfo4 != null) {
                                                                    try {
                                                                        realCallingUid3 = realCallingUid2;
                                                                        try {
                                                                            aInfo2 = this.mService.mAmInternal.getActivityInfoForUser(aInfo4, userId);
                                                                            resolvedType2 = null;
                                                                            callingPid = callingPid2;
                                                                            intent2 = newIntent;
                                                                            rInfo2 = rInfo6;
                                                                            callingUid4 = callingUid5;
                                                                            componentSpecified2 = true;
                                                                            caller2 = null;
                                                                        } catch (Throwable th14) {
                                                                            th = th14;
                                                                            while (true) {
                                                                                break;
                                                                            }
                                                                            WindowManagerService.resetPriorityAfterLockedSection();
                                                                            throw th;
                                                                        }
                                                                    } catch (Throwable th15) {
                                                                        th = th15;
                                                                        while (true) {
                                                                            break;
                                                                        }
                                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                                        throw th;
                                                                    }
                                                                } else {
                                                                    realCallingUid3 = realCallingUid2;
                                                                    resolvedType2 = null;
                                                                    callingPid = callingPid2;
                                                                    intent2 = newIntent;
                                                                    aInfo2 = aInfo4;
                                                                    rInfo2 = rInfo6;
                                                                    callingUid4 = callingUid5;
                                                                    componentSpecified2 = true;
                                                                    caller2 = null;
                                                                }
                                                                outRecord = new ActivityRecord[1];
                                                                res = startActivity(caller2, intent2, ephemeralIntent, resolvedType2, aInfo2, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid4, callingPackage, realCallingPid, realCallingUid3, startFlags, options, ignoreTargetSecurity, componentSpecified2, outRecord, inTask, reason, allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, allowBackgroundActivityStart);
                                                                Binder.restoreCallingIdentity(origId2);
                                                                if (!stack2.mConfigWillChange) {
                                                                    activityStarter = this;
                                                                    try {
                                                                        activityStarter.mService.mAmInternal.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
                                                                        stack2.mConfigWillChange = r0;
                                                                        if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                                                            try {
                                                                                Slog.v(TAG_CONFIGURATION, "Updating to new configuration after starting activity.");
                                                                            } catch (Throwable th16) {
                                                                                th = th16;
                                                                            }
                                                                        }
                                                                        activityStarter.mService.updateConfigurationLocked(globalConfig, null, r0);
                                                                    } catch (Throwable th17) {
                                                                        th = th17;
                                                                        while (true) {
                                                                            break;
                                                                        }
                                                                        WindowManagerService.resetPriorityAfterLockedSection();
                                                                        throw th;
                                                                    }
                                                                } else {
                                                                    activityStarter = this;
                                                                }
                                                                ActivityMetricsLogger activityMetricsLogger2 = activityStarter.mSupervisor.getActivityMetricsLogger();
                                                                char c9 = r0 == true ? 1 : 0;
                                                                char c22 = r0 == true ? 1 : 0;
                                                                char c32 = r0 == true ? 1 : 0;
                                                                char c42 = r0 == true ? 1 : 0;
                                                                char c52 = r0 == true ? 1 : 0;
                                                                char c62 = r0 == true ? 1 : 0;
                                                                char c72 = r0 == true ? 1 : 0;
                                                                char c82 = r0 == true ? 1 : 0;
                                                                activityMetricsLogger2.notifyActivityLaunched(res, outRecord[c9]);
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                return res;
                                                            } catch (Throwable th18) {
                                                                th = th18;
                                                                while (true) {
                                                                    break;
                                                                }
                                                                WindowManagerService.resetPriorityAfterLockedSection();
                                                                throw th;
                                                            }
                                                            if (outResult != null) {
                                                                try {
                                                                    outResult.result = res;
                                                                    r = outRecord[r0];
                                                                } catch (Throwable th19) {
                                                                    th = th19;
                                                                    while (true) {
                                                                        break;
                                                                    }
                                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                                    throw th;
                                                                }
                                                                if (res != 0) {
                                                                    int i = 3;
                                                                    if (res == 2) {
                                                                        if (!r.attachedToProcess()) {
                                                                            i = 1;
                                                                        }
                                                                        outResult.launchState = i;
                                                                        if (!r.nowVisible || !r.isState(ActivityStack.ActivityState.RESUMED)) {
                                                                            activityStarter.mSupervisor.waitActivityVisible(r.mActivityComponent, outResult, SystemClock.uptimeMillis());
                                                                            do {
                                                                                try {
                                                                                    activityStarter.mService.mGlobalLock.wait();
                                                                                } catch (InterruptedException e) {
                                                                                }
                                                                                if (outResult.timeout) {
                                                                                    break;
                                                                                }
                                                                            } while (outResult.who == null);
                                                                        } else {
                                                                            outResult.timeout = r0;
                                                                            outResult.who = r.mActivityComponent;
                                                                            outResult.totalTime = 0;
                                                                        }
                                                                    } else if (res == 3) {
                                                                        outResult.timeout = r0;
                                                                        outResult.who = r.mActivityComponent;
                                                                        outResult.totalTime = 0;
                                                                    }
                                                                } else {
                                                                    activityStarter.mSupervisor.mWaitingActivityLaunched.add(outResult);
                                                                    do {
                                                                        try {
                                                                            activityStarter.mService.mGlobalLock.wait();
                                                                        } catch (InterruptedException e2) {
                                                                        }
                                                                        if (outResult.result == 2 || outResult.timeout) {
                                                                            break;
                                                                        }
                                                                    } while (outResult.who == null);
                                                                    if (outResult.result == 2) {
                                                                        res = 2;
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            windowManagerGlobalLock = windowManagerGlobalLock2;
                                                            stack2 = stack;
                                                            aInfo3 = aInfo;
                                                            rInfo3 = rInfo;
                                                            realCallingUid3 = realCallingUid2;
                                                            heavy = null;
                                                        }
                                                    } else {
                                                        windowManagerGlobalLock = windowManagerGlobalLock2;
                                                        stack2 = stack;
                                                        aInfo3 = aInfo;
                                                        rInfo3 = rInfo;
                                                        realCallingUid3 = realCallingUid2;
                                                        heavy = null;
                                                    }
                                                    resolvedType2 = resolvedType;
                                                    callingPid = callingUid3;
                                                    intent2 = intent3;
                                                    callingUid4 = callingUid2;
                                                    rInfo2 = rInfo3;
                                                    aInfo2 = aInfo3;
                                                    componentSpecified2 = componentSpecified;
                                                    caller2 = caller;
                                                    r0 = heavy;
                                                    outRecord = new ActivityRecord[1];
                                                    res = startActivity(caller2, intent2, ephemeralIntent, resolvedType2, aInfo2, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid4, callingPackage, realCallingPid, realCallingUid3, startFlags, options, ignoreTargetSecurity, componentSpecified2, outRecord, inTask, reason, allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, allowBackgroundActivityStart);
                                                    Binder.restoreCallingIdentity(origId2);
                                                    if (!stack2.mConfigWillChange) {
                                                    }
                                                    ActivityMetricsLogger activityMetricsLogger22 = activityStarter.mSupervisor.getActivityMetricsLogger();
                                                    char c92 = r0 == true ? 1 : 0;
                                                    char c222 = r0 == true ? 1 : 0;
                                                    char c322 = r0 == true ? 1 : 0;
                                                    char c422 = r0 == true ? 1 : 0;
                                                    char c522 = r0 == true ? 1 : 0;
                                                    char c622 = r0 == true ? 1 : 0;
                                                    char c722 = r0 == true ? 1 : 0;
                                                    char c822 = r0 == true ? 1 : 0;
                                                    activityMetricsLogger22.notifyActivityLaunched(res, outRecord[c92]);
                                                    if (outResult != null) {
                                                    }
                                                    WindowManagerService.resetPriorityAfterLockedSection();
                                                    return res;
                                                }
                                            }
                                            windowManagerGlobalLock = windowManagerGlobalLock2;
                                            stack2 = stack;
                                            aInfo3 = aInfo;
                                            rInfo3 = rInfo;
                                            realCallingUid3 = realCallingUid2;
                                            heavy = null;
                                            resolvedType2 = resolvedType;
                                            callingPid = callingUid3;
                                            intent2 = intent3;
                                            callingUid4 = callingUid2;
                                            rInfo2 = rInfo3;
                                            aInfo2 = aInfo3;
                                            componentSpecified2 = componentSpecified;
                                            caller2 = caller;
                                            r0 = heavy;
                                            outRecord = new ActivityRecord[1];
                                            res = startActivity(caller2, intent2, ephemeralIntent, resolvedType2, aInfo2, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid4, callingPackage, realCallingPid, realCallingUid3, startFlags, options, ignoreTargetSecurity, componentSpecified2, outRecord, inTask, reason, allowPendingRemoteAnimationRegistryLookup, originatingPendingIntent, allowBackgroundActivityStart);
                                            Binder.restoreCallingIdentity(origId2);
                                            if (!stack2.mConfigWillChange) {
                                            }
                                            ActivityMetricsLogger activityMetricsLogger222 = activityStarter.mSupervisor.getActivityMetricsLogger();
                                            char c922 = r0 == true ? 1 : 0;
                                            char c2222 = r0 == true ? 1 : 0;
                                            char c3222 = r0 == true ? 1 : 0;
                                            char c4222 = r0 == true ? 1 : 0;
                                            char c5222 = r0 == true ? 1 : 0;
                                            char c6222 = r0 == true ? 1 : 0;
                                            char c7222 = r0 == true ? 1 : 0;
                                            char c8222 = r0 == true ? 1 : 0;
                                            activityMetricsLogger222.notifyActivityLaunched(res, outRecord[c922]);
                                            if (outResult != null) {
                                            }
                                            WindowManagerService.resetPriorityAfterLockedSection();
                                            return res;
                                        }
                                    }
                                }
                                WindowManagerService.resetPriorityAfterLockedSection();
                                return -94;
                            }
                            rInfo4 = rInfo5;
                            realCallingUid2 = realCallingUid;
                        }
                    }
                } else {
                    rInfo4 = rInfo5;
                    realCallingUid2 = realCallingUid;
                }
            } else {
                rInfo4 = rInfo5;
                realCallingUid2 = realCallingUid;
            }
            rInfo = rInfo4;
            aInfo = this.mSupervisor.resolveActivity(intent3, rInfo, startFlags, profilerInfo);
            if (aInfo != null) {
            }
            if (intent3.getComponent() == null) {
            }
            Jlog.d(335, aInfo.applicationInfo.packageName + "/" + (intent3.getComponent() == null ? intent3.getComponent().getClassName() : "NULL"), "");
            if (!this.mHwActivityStarterEx.isAppDisabledByMdmNoComponent(aInfo, intent3, resolvedType, this.mSupervisor)) {
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
    }

    static int computeResolveFilterUid(int customCallingUid, int actualCallingUid, int filterCallingUid) {
        if (filterCallingUid != -10000) {
            return filterCallingUid;
        }
        return customCallingUid >= 0 ? customCallingUid : actualCallingUid;
    }

    /* JADX INFO: finally extract failed */
    private int startActivity(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity, boolean restrictedBgActivity) {
        try {
            this.mService.mWindowManager.deferSurfaceLayout();
            int result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor, startFlags, doResume, options, inTask, outActivity, restrictedBgActivity);
            try {
                ActivityStack currentStack = r.getActivityStack();
                ActivityStack startedActivityStack = currentStack != null ? currentStack : this.mTargetStack;
                if (!ActivityManager.isStartResultSuccessful(result)) {
                    ActivityStack stack = this.mStartActivity.getActivityStack();
                    if (stack != null) {
                        stack.finishActivityLocked(this.mStartActivity, 0, null, "startActivity", true);
                    }
                    if (startedActivityStack != null && startedActivityStack.isAttached() && startedActivityStack.numActivities() == 0 && !startedActivityStack.isActivityTypeHome()) {
                        startedActivityStack.remove();
                    }
                } else if (startedActivityStack != null) {
                    ActivityRecord currentTop = startedActivityStack.topRunningActivityLocked();
                    if (currentTop != null && currentTop.shouldUpdateConfigForDisplayChanged()) {
                        this.mRootActivityContainer.ensureVisibilityAndConfig(currentTop, currentTop.getDisplayId(), true, false);
                    }
                }
                this.mService.mWindowManager.continueSurfaceLayout();
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v("ActivityTaskManager", "startActivity result is " + result);
                }
                postStartActivityProcessing(r, result, startedActivityStack);
                return result;
            } catch (Throwable th) {
                throw th;
            }
        } catch (Throwable th2) {
            ActivityStack currentStack2 = r.getActivityStack();
            ActivityStack startedActivityStack2 = currentStack2 != null ? currentStack2 : this.mTargetStack;
            if (!ActivityManager.isStartResultSuccessful(-96)) {
                ActivityStack stack2 = this.mStartActivity.getActivityStack();
                if (stack2 != null) {
                    stack2.finishActivityLocked(this.mStartActivity, 0, null, "startActivity", true);
                }
                if (startedActivityStack2 != null && startedActivityStack2.isAttached() && startedActivityStack2.numActivities() == 0 && !startedActivityStack2.isActivityTypeHome()) {
                    startedActivityStack2.remove();
                }
            } else if (startedActivityStack2 != null) {
                ActivityRecord currentTop2 = startedActivityStack2.topRunningActivityLocked();
                if (currentTop2 != null && currentTop2.shouldUpdateConfigForDisplayChanged()) {
                    this.mRootActivityContainer.ensureVisibilityAndConfig(currentTop2, currentTop2.getDisplayId(), true, false);
                }
            }
            throw th2;
        } finally {
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    private boolean handleBackgroundActivityAbort(ActivityRecord r) {
        if (!(!this.mService.isBackgroundActivityStartsEnabled())) {
            return false;
        }
        ActivityRecord resultRecord = r.resultTo;
        String resultWho = r.resultWho;
        int requestCode = r.requestCode;
        if (resultRecord != null) {
            resultRecord.getActivityStack().sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
        }
        ActivityOptions.abort(r.pendingOptions);
        return true;
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x054d: APUT  (r3v19 java.lang.Object[]), (0 ??[int, short, byte, char]), (r4v17 android.view.IApplicationToken$Stub) */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x0322  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0359  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00a5  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00aa  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x00ca  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0125  */
    private int startActivityUnchecked(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity, boolean restrictedBgActivity) {
        ActivityRecord reusedActivity;
        int i;
        ActivityRecord reusedActivity2;
        ActivityStack activityStack;
        ActivityRecord activityRecord;
        ActivityRecord activityRecord2;
        ActivityRecord activityRecord3;
        ActivityRecord activityRecord4;
        this.mService.mHwATMSEx.adjustActivityOptionsForPCCast(r, options);
        setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor, restrictedBgActivity);
        int preferredWindowingMode = this.mLaunchParams.mWindowingMode;
        computeLaunchingTaskFlags();
        computeSourceStack();
        this.mIntent.setFlags(this.mLaunchFlags);
        ActivityRecord reusedActivity3 = getReusableIntentActivity();
        if (options != null && options.getLaunchWindowingMode() == 12) {
            reusedActivity3 = null;
        }
        this.mService.mHwATMSEx.noteActivityInitializing(this.mStartActivity, reusedActivity3);
        StringBuilder sb = new StringBuilder();
        sb.append("ReusedActivity is ");
        sb.append(reusedActivity3);
        sb.append(" launchmode:");
        sb.append(options != null ? options.getLaunchWindowingMode() : 0);
        Flog.i(101, sb.toString());
        Bundle ret = this.mHwActivityStarterEx.checkActivityStartedOnDisplay(this.mStartActivity, this.mPreferredDisplayId, options, reusedActivity3);
        if (ret != null) {
            if (ret.getBoolean("skipStart", false)) {
                return ret.getInt("startResult", 0);
            }
            if (ret.getBoolean("skipReuse", false)) {
                reusedActivity = null;
                this.mSupervisor.getLaunchParamsController().calculate(reusedActivity == null ? reusedActivity.getTaskRecord() : this.mInTask, r.info.windowLayout, r, sourceRecord, options, 2, this.mLaunchParams);
                if (!this.mLaunchParams.hasPreferredDisplay()) {
                    i = this.mLaunchParams.mPreferredDisplayId;
                } else {
                    i = 0;
                }
                this.mPreferredDisplayId = i;
                updateToHwFreeFormAnimationIfNeed(r, reusedActivity, sourceRecord, options);
                if (r.isActivityTypeHome() || this.mRootActivityContainer.canStartHomeOnDisplay(r.info, this.mPreferredDisplayId, true)) {
                    if (HwFreeFormUtils.isFreeFormEnable() && !HwFreeFormUtils.getFreeFormStackVisible()) {
                        if (reusedActivity == null && reusedActivity.inFreeformWindowingMode() && this.mLaunchParams.mWindowingMode != 5) {
                            this.mSupervisor.mHwActivityStackSupervisorEx.handleFreeFormWindow(reusedActivity.task);
                        }
                    }
                    if (reusedActivity != null || reusedActivity.getTaskRecord() == null) {
                        reusedActivity2 = reusedActivity;
                    } else {
                        if (this.mService.getLockTaskController().isLockTaskModeViolation(reusedActivity.getTaskRecord(), (this.mLaunchFlags & 268468224) == 268468224)) {
                            Slog.e("ActivityTaskManager", "startActivityUnchecked: Attempt to violate Lock Task Mode");
                            return 101;
                        }
                        ActivityRecord activityRecord5 = this.mSourceRecord;
                        if ((activityRecord5 != null && activityRecord5.isActivityTypeHome()) || ((options != null && (options.getActivityLaunchEventFrom() == 2 || options.getActivityLaunchEventFrom() == 3)) || ((activityRecord4 = this.mStartActivity) != null && (INTELLIGENT_APPNAME.equals(activityRecord4.launchedFromPackage) || "com.huawei.android.launcher".equals(this.mStartActivity.launchedFromPackage))))) {
                            this.mSupervisor.mHwActivityStackSupervisorEx.handlePCMultiDisplayWindow(reusedActivity.task, this.mLaunchParams.mWindowingMode);
                        }
                        boolean clearTopAndResetStandardLaunchMode = (this.mLaunchFlags & 69206016) == 69206016 && this.mLaunchMode == 0;
                        if (this.mStartActivity.getTaskRecord() == null && !clearTopAndResetStandardLaunchMode) {
                            this.mStartActivity.setTask(reusedActivity.getTaskRecord());
                        }
                        if (reusedActivity.getTaskRecord().intent == null) {
                            reusedActivity.getTaskRecord().setIntent(this.mStartActivity);
                        } else {
                            if ((this.mStartActivity.intent.getFlags() & 16384) != 0) {
                                reusedActivity.getTaskRecord().intent.addFlags(16384);
                            } else {
                                reusedActivity.getTaskRecord().intent.removeFlags(16384);
                            }
                        }
                        int i2 = this.mLaunchFlags;
                        if ((67108864 & i2) != 0 || isDocumentLaunchesIntoExisting(i2) || isLaunchModeOneOf(3, 2)) {
                            TaskRecord task = reusedActivity.getTaskRecord();
                            ActivityRecord top = task.performClearTaskForReuseLocked(this.mStartActivity, this.mLaunchFlags);
                            if (reusedActivity.getTaskRecord() == null) {
                                reusedActivity.setTask(task);
                            }
                            if (top != null) {
                                if (top.frontOfTask) {
                                    top.getTaskRecord().setIntent(this.mStartActivity);
                                }
                                deliverNewIntent(top);
                            }
                        }
                        this.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(false, reusedActivity);
                        Rect resultRect = new Rect();
                        ActivityStack startStack = null;
                        ActivityRecord activityRecord6 = this.mStartActivity;
                        if (activityRecord6 != null) {
                            startStack = activityRecord6.getActivityStack();
                        }
                        if (startStack != null) {
                            startStack.mIsTempStack = false;
                            boolean isFreeformVisible = startStack.getWindowingMode() == 102 && startStack.isAlwaysOnTop();
                            if (!((startStack.mTaskStack == null || !startStack.mTaskStack.isVisible() || startStack.getWindowingMode() == 102) ? false : true) && !isFreeformVisible) {
                                resultRect = this.mService.mHwATMSEx.handleStackFromOneStep(this.mStartActivity, startStack, this.mSourceRecord);
                            }
                        }
                        reusedActivity2 = setTargetStackAndMoveToFrontIfNeeded(reusedActivity);
                        if (startStack != null) {
                            if (!resultRect.isEmpty() && (activityRecord3 = this.mSourceRecord) != null && activityRecord3.getActivityStack() != null && startStack.getWindowingMode() == 102) {
                                startStack.mTaskStack.mHwStackScale = this.mSourceRecord.getActivityStack().mTaskStack.mHwStackScale;
                                startStack.resize(resultRect, null, null);
                                this.mLaunchParams.mBounds.set(resultRect);
                            }
                            IHwActivityTaskManagerServiceEx iHwActivityTaskManagerServiceEx = this.mService.mHwATMSEx;
                            ActivityRecord activityRecord7 = this.mStartActivity;
                            iHwActivityTaskManagerServiceEx.oneStepHwMultiWindowBdReport(activityRecord7, activityRecord7.getActivityStack().getWindowingMode(), this.mOptions);
                        }
                        ActivityRecord outResult = (outActivity == null || outActivity.length <= 0) ? null : outActivity[0];
                        if (outResult != null && (outResult.finishing || outResult.noDisplay)) {
                            outActivity[0] = reusedActivity2;
                        }
                        if ((this.mStartFlags & 1) != 0) {
                            resumeTargetStackIfNeeded();
                            return 1;
                        } else if (reusedActivity2 != null) {
                            setTaskFromIntentActivity(reusedActivity2);
                            if (!this.mAddingToTask && this.mReuseTask == null) {
                                resumeTargetStackIfNeeded();
                                if (outActivity != null && outActivity.length > 0) {
                                    outActivity[0] = (!reusedActivity2.finishing || reusedActivity2.getTaskRecord() == null) ? reusedActivity2 : reusedActivity2.getTaskRecord().getTopActivity();
                                }
                                return this.mMovedToFront ? 2 : 3;
                            }
                        }
                    }
                    if (this.mStartActivity.packageName != null) {
                        ActivityStack sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getActivityStack() : null;
                        if (sourceStack != null) {
                            sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
                        }
                        ActivityOptions.abort(this.mOptions);
                        return -92;
                    }
                    ActivityStack topStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
                    ActivityRecord topFocused = topStack.getTopActivity();
                    ActivityRecord top2 = topStack.topRunningNonDelayedActivityLocked(this.mNotTop);
                    if (top2 != null && this.mStartActivity.resultTo == null && top2.mActivityComponent.equals(this.mStartActivity.mActivityComponent) && top2.mUserId == this.mStartActivity.mUserId && top2.attachedToProcess() && ((this.mLaunchFlags & 536870912) != 0 || isLaunchModeOneOf(1, 2)) && ((!top2.isActivityTypeHome() || top2.getDisplayId() == this.mPreferredDisplayId) && (!isLaunchModeSingleTop() || !isAllowedLaunchMultiTask()))) {
                        topStack.mLastPausedActivity = null;
                        if (this.mDoResume) {
                            this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                        }
                        ActivityOptions.abort(this.mOptions);
                        if ((this.mStartFlags & 1) != 0) {
                            return 1;
                        }
                        deliverNewIntent(top2);
                        this.mSupervisor.handleNonResizableTaskIfNeeded(top2.getTaskRecord(), preferredWindowingMode, this.mPreferredDisplayId, topStack);
                        return 3;
                    }
                    boolean newTask = false;
                    TaskRecord taskToAffiliate = (!this.mLaunchTaskBehind || (activityRecord2 = this.mSourceRecord) == null) ? null : activityRecord2.getTaskRecord();
                    int result = 0;
                    if ((this.mStartActivity.resultTo == null || ((activityRecord = this.mSourceRecord) != null && activityRecord.inHwMagicWindowingMode() && this.mStartActivity.mIsMwNewTask)) && this.mInTask == null && !this.mAddingToTask && (this.mLaunchFlags & 268435456) != 0) {
                        result = setTaskFromReuseOrCreateNewTask(taskToAffiliate);
                        newTask = true;
                    } else if (this.mSourceRecord == null) {
                        result = this.mInTask != null ? setTaskFromInTask() : setTaskToCurrentTopOrCreateNewTask();
                    } else if (HwPCUtils.isPcCastModeInServer() && this.mSourceRecord.getTaskRecord() == null && this.mSourceRecord.getActivityStack() == null) {
                        Slog.i("ActivityTaskManager", "ActivityStarter startActivityUnchecked task and stack null");
                        setTaskToCurrentTopOrCreateNewTask();
                    } else if (!HwPCUtils.isHiCarCastMode() || !HwPCUtils.isValidExtDisplayId(this.mPreferredDisplayId) || (activityStack = this.mSourceStack) == null || HwPCUtils.isExtDynamicStack(activityStack.mStackId)) {
                        result = setTaskFromSourceRecord();
                    } else {
                        HwPCUtils.log("ActivityTaskManager", "ActivityStarter startActivityUnchecked in hicar mode.");
                        setTaskToCurrentTopOrCreateNewTask();
                    }
                    this.mHwActivityStarterEx.handleFreeFormStackIfNeed(this.mStartActivity);
                    if (result != 0) {
                        return result;
                    }
                    if (!this.mHwActivityStarterEx.checkActivityStartForPCMode(this.mOptions, this.mStartActivity, this.mTargetStack)) {
                        return -96;
                    }
                    if (this.mCallingUid == 1000 && this.mIntent.getCallingUid() != 0) {
                        this.mCallingUid = this.mIntent.getCallingUid();
                        this.mIntent.setCallingUid(0);
                    }
                    this.mService.mUgmInternal.grantUriPermissionFromIntent(this.mCallingUid, this.mStartActivity.packageName, this.mIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.mUserId);
                    this.mService.getPackageManagerInternalLocked().grantEphemeralAccess(this.mStartActivity.mUserId, this.mIntent, UserHandle.getAppId(this.mStartActivity.appInfo.uid), UserHandle.getAppId(this.mCallingUid));
                    if (newTask) {
                        EventLog.writeEvent(30004, Integer.valueOf(this.mStartActivity.mUserId), Integer.valueOf(this.mStartActivity.getTaskRecord().taskId));
                    }
                    ActivityRecord activityRecord8 = this.mStartActivity;
                    ActivityStack.logStartActivity(30005, activityRecord8, activityRecord8.getTaskRecord());
                    IApplicationToken.Stub stub = null;
                    this.mTargetStack.mLastPausedActivity = null;
                    this.mRootActivityContainer.sendPowerHintForLaunchStartIfNeeded(false, this.mStartActivity);
                    if (HwMwUtils.ENABLED && topFocused != null) {
                        boolean isCreateNewTask = newTask && this.mReuseTask == null;
                        Object[] objArr = new Object[5];
                        ActivityRecord activityRecord9 = this.mSourceRecord;
                        objArr[0] = activityRecord9 != null ? activityRecord9.appToken : topFocused.appToken;
                        objArr[1] = this.mStartActivity;
                        if (reusedActivity2 != null) {
                            stub = reusedActivity2.appToken;
                        }
                        objArr[2] = stub;
                        objArr[3] = Boolean.valueOf(isCreateNewTask);
                        objArr[4] = options;
                        HwMwUtils.performPolicy(0, objArr);
                    }
                    this.mTargetStack.startActivityLocked(this.mStartActivity, topFocused, newTask, this.mKeepCurTransition, this.mOptions);
                    if (this.mDoResume) {
                        ActivityRecord topTaskActivity = this.mStartActivity.getTaskRecord().topRunningActivityLocked();
                        if (!this.mTargetStack.isFocusable() || !(topTaskActivity == null || !topTaskActivity.mTaskOverlay || this.mStartActivity == topTaskActivity)) {
                            this.mTargetStack.ensureActivitiesVisibleLocked(this.mStartActivity, 0, false);
                            this.mTargetStack.getDisplay().mDisplayContent.executeAppTransition();
                        } else {
                            if (this.mTargetStack.isFocusable() && !this.mRootActivityContainer.isTopDisplayFocusedStack(this.mTargetStack)) {
                                this.mTargetStack.moveToFront("startActivityUnchecked");
                            }
                            this.mRootActivityContainer.resumeFocusedStacksTopActivities(this.mTargetStack, this.mStartActivity, this.mOptions);
                        }
                    } else if (this.mStartActivity != null) {
                        this.mSupervisor.mRecentTasks.add(this.mStartActivity.getTaskRecord());
                    }
                    this.mRootActivityContainer.updateUserStack(this.mStartActivity.mUserId, this.mTargetStack);
                    this.mSupervisor.handleNonResizableTaskIfNeeded(this.mStartActivity.getTaskRecord(), preferredWindowingMode, this.mPreferredDisplayId, this.mTargetStack);
                    return 0;
                }
                Slog.w("ActivityTaskManager", "Cannot launch home on display " + this.mPreferredDisplayId);
                return -96;
            }
        }
        reusedActivity = reusedActivity3;
        this.mSupervisor.getLaunchParamsController().calculate(reusedActivity == null ? reusedActivity.getTaskRecord() : this.mInTask, r.info.windowLayout, r, sourceRecord, options, 2, this.mLaunchParams);
        if (!this.mLaunchParams.hasPreferredDisplay()) {
        }
        this.mPreferredDisplayId = i;
        updateToHwFreeFormAnimationIfNeed(r, reusedActivity, sourceRecord, options);
        if (r.isActivityTypeHome()) {
        }
        if (reusedActivity == null && reusedActivity.inFreeformWindowingMode() && this.mLaunchParams.mWindowingMode != 5) {
        }
        if (reusedActivity != null) {
        }
        reusedActivity2 = reusedActivity;
        if (this.mStartActivity.packageName != null) {
        }
    }

    /* access modifiers changed from: package-private */
    public void reset(boolean clearRequest) {
        this.mStartActivity = null;
        this.mIntent = null;
        this.mCallingUid = -1;
        this.mOptions = null;
        this.mRestrictedBgActivity = false;
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
    public void setInitialState(ActivityRecord r, ActivityOptions options, TaskRecord inTask, boolean doResume, int startFlags, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, boolean restrictedBgActivity) {
        int i;
        if (options != null && options.getLaunchWindowingMode() == 102 && this.mService.mHwATMSEx.isInDisplaySurfaceScaled()) {
            options.setLaunchWindowingMode(0);
        }
        reset(false);
        this.mStartActivity = r;
        this.mIntent = r.intent;
        this.mOptions = options;
        this.mCallingUid = r.launchedFromUid;
        this.mSourceRecord = sourceRecord;
        this.mVoiceSession = voiceSession;
        this.mVoiceInteractor = voiceInteractor;
        this.mRestrictedBgActivity = restrictedBgActivity;
        this.mLaunchParams.reset();
        this.mSupervisor.getLaunchParamsController().calculate(inTask, r.info.windowLayout, r, sourceRecord, options, 0, this.mLaunchParams);
        if (this.mLaunchParams.hasPreferredDisplay()) {
            i = this.mLaunchParams.mPreferredDisplayId;
        } else {
            i = 0;
        }
        this.mPreferredDisplayId = i;
        this.mLaunchMode = r.launchMode;
        this.mLaunchFlags = adjustLaunchFlagsToDocumentMode(r, 3 == this.mLaunchMode, 2 == this.mLaunchMode, this.mIntent.getFlags());
        this.mLaunchTaskBehind = r.mLaunchTaskBehind && !isLaunchModeOneOf(2, 3) && (this.mLaunchFlags & 524288) != 0;
        sendNewTaskResultRequestIfNeeded();
        if ((this.mLaunchFlags & 524288) != 0 && r.resultTo == null) {
            this.mLaunchFlags |= 268435456;
        }
        if ((this.mLaunchFlags & 268435456) != 0 && (this.mLaunchTaskBehind || r.info.documentLaunchMode == 2)) {
            this.mLaunchFlags |= 134217728;
        }
        this.mSupervisor.mUserLeaving = (this.mLaunchFlags & 262144) == 0;
        if (ActivityTaskManagerDebugConfig.DEBUG_USER_LEAVING) {
            Slog.v("ActivityTaskManager", "startActivity() => mUserLeaving=" + this.mSupervisor.mUserLeaving);
        }
        this.mDoResume = doResume;
        if (!doResume || !r.okToShowLocked()) {
            r.delayedResume = true;
            this.mDoResume = false;
        }
        ActivityOptions activityOptions = this.mOptions;
        if (activityOptions != null) {
            if (activityOptions.getLaunchTaskId() != -1 && this.mOptions.getTaskOverlay()) {
                r.mTaskOverlay = true;
                if (!this.mOptions.canTaskOverlayResume()) {
                    TaskRecord task = this.mRootActivityContainer.anyTaskForId(this.mOptions.getLaunchTaskId());
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
        this.mNotTop = (this.mLaunchFlags & 16777216) != 0 ? sourceRecord : null;
        this.mInTask = inTask;
        if (inTask != null && !inTask.inRecents) {
            Slog.w("ActivityTaskManager", "Starting activity in task not in recents: " + inTask);
            this.mInTask = null;
        }
        this.mStartFlags = startFlags;
        if ((startFlags & 1) != 0) {
            ActivityRecord checkedCaller = sourceRecord;
            if (checkedCaller == null) {
                checkedCaller = this.mRootActivityContainer.getTopDisplayFocusedStack().topRunningNonDelayedActivityLocked(this.mNotTop);
            }
            if (!checkedCaller.mActivityComponent.equals(r.mActivityComponent)) {
                this.mStartFlags &= -2;
            }
        }
        this.mNoAnimation = (this.mLaunchFlags & 65536) != 0;
        if (this.mRestrictedBgActivity && !this.mService.isBackgroundActivityStartsEnabled()) {
            this.mAvoidMoveToFront = true;
            this.mDoResume = false;
        }
    }

    private void sendNewTaskResultRequestIfNeeded() {
        ActivityStack sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getActivityStack() : null;
        if (sourceStack != null && (this.mLaunchFlags & 268435456) != 0) {
            if (isInSkipCancelResultList(this.mStartActivity.shortComponentName) || (sourceStack.inHwMagicWindowingMode() && this.mStartActivity.mIsMwNewTask)) {
                Slog.w("ActivityTaskManager", "we skip cancelling activity result from activity " + this.mStartActivity.shortComponentName);
                return;
            }
            Slog.w("ActivityTaskManager", "Activity is launching as a new task, so cancelling activity result.");
            sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            this.mStartActivity.resultTo = null;
        }
    }

    private void computeLaunchingTaskFlags() {
        ActivityRecord activityRecord;
        TaskRecord taskRecord;
        if (this.mSourceRecord != null || (taskRecord = this.mInTask) == null || taskRecord.getStack() == null) {
            this.mInTask = null;
            if ((this.mStartActivity.isResolverActivity() || this.mStartActivity.noDisplay) && (activityRecord = this.mSourceRecord) != null && activityRecord.inFreeformWindowingMode()) {
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
        TaskRecord taskRecord2 = this.mInTask;
        if (taskRecord2 == null) {
            ActivityRecord activityRecord2 = this.mSourceRecord;
            if (activityRecord2 == null) {
                if ((this.mLaunchFlags & 268435456) == 0 && taskRecord2 == null) {
                    Slog.w("ActivityTaskManager", "startActivity called from non-Activity context; forcing Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent.toShortStringWithoutClip(true, true, true));
                    this.mLaunchFlags = this.mLaunchFlags | 268435456;
                }
            } else if (activityRecord2.launchMode == 3) {
                this.mLaunchFlags |= 268435456;
            } else if (isLaunchModeOneOf(3, 2)) {
                this.mLaunchFlags |= 268435456;
            }
        }
    }

    private void computeSourceStack() {
        ActivityRecord activityRecord = this.mSourceRecord;
        if (activityRecord == null) {
            this.mSourceStack = null;
        } else if (!activityRecord.finishing) {
            this.mSourceStack = this.mSourceRecord.getActivityStack();
        } else {
            if ((this.mLaunchFlags & 268435456) == 0) {
                Slog.w("ActivityTaskManager", "startActivity called from finishing " + this.mSourceRecord + "; forcing Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                this.mLaunchFlags = this.mLaunchFlags | 268435456;
                this.mNewTaskInfo = this.mSourceRecord.info;
                TaskRecord sourceTask = this.mSourceRecord.getTaskRecord();
                this.mNewTaskIntent = sourceTask != null ? sourceTask.intent : null;
            }
            this.mSourceRecord = null;
            this.mSourceStack = null;
        }
    }

    private ActivityRecord getReusableIntentActivity() {
        int i = this.mLaunchFlags;
        boolean z = false;
        boolean putIntoExistingTask = (((268435456 & i) != 0 && (i & 134217728) == 0) || isLaunchModeOneOf(3, 2)) & (this.mInTask == null && this.mStartActivity.resultTo == null);
        ActivityRecord intentActivity = null;
        ActivityOptions activityOptions = this.mOptions;
        if (activityOptions != null && activityOptions.getLaunchTaskId() != -1) {
            TaskRecord task = this.mRootActivityContainer.anyTaskForId(this.mOptions.getLaunchTaskId());
            intentActivity = task != null ? task.getTopActivity() : null;
        } else if (putIntoExistingTask) {
            if (3 == this.mLaunchMode) {
                intentActivity = this.mRootActivityContainer.findActivity(this.mIntent, this.mStartActivity.info, this.mStartActivity.isActivityTypeHome());
            } else if ((this.mLaunchFlags & 4096) != 0) {
                RootActivityContainer rootActivityContainer = this.mRootActivityContainer;
                Intent intent = this.mIntent;
                ActivityInfo activityInfo = this.mStartActivity.info;
                if (2 != this.mLaunchMode) {
                    z = true;
                }
                intentActivity = rootActivityContainer.findActivity(intent, activityInfo, z);
            } else {
                intentActivity = this.mRootActivityContainer.findTask(this.mStartActivity, this.mPreferredDisplayId);
            }
        }
        if (intentActivity == null) {
            return intentActivity;
        }
        if ((this.mStartActivity.isActivityTypeHome() || intentActivity.isActivityTypeHome()) && intentActivity.getDisplayId() != this.mPreferredDisplayId) {
            return null;
        }
        return intentActivity;
    }

    private ActivityRecord setTargetStackAndMoveToFrontIfNeeded(ActivityRecord intentActivity) {
        boolean differentTopTask;
        TaskRecord intentTask;
        ActivityRecord activityRecord;
        this.mTargetStack = intentActivity.getActivityStack();
        ActivityStack activityStack = this.mTargetStack;
        activityStack.mLastPausedActivity = null;
        if (this.mPreferredDisplayId == activityStack.mDisplayId) {
            ActivityStack focusStack = this.mTargetStack.getDisplay().getFocusedStack();
            ActivityRecord curTop = focusStack == null ? null : focusStack.topRunningNonDelayedActivityLocked(this.mNotTop);
            TaskRecord topTask = curTop != null ? curTop.getTaskRecord() : null;
            differentTopTask = (topTask == intentActivity.getTaskRecord() && (focusStack == null || topTask == focusStack.topTask())) ? false : true;
        } else {
            differentTopTask = true;
        }
        if (differentTopTask && !this.mAvoidMoveToFront) {
            this.mStartActivity.intent.addFlags(4194304);
            if (this.mSourceRecord == null || (this.mSourceStack.getTopActivity() != null && this.mSourceStack.getTopActivity().getTaskRecord() == this.mSourceRecord.getTaskRecord())) {
                if (this.mLaunchTaskBehind && (activityRecord = this.mSourceRecord) != null) {
                    intentActivity.setTaskToAffiliateWith(activityRecord.getTaskRecord());
                }
                if (!((this.mLaunchFlags & 268468224) == 268468224)) {
                    ActivityRecord activityRecord2 = this.mStartActivity;
                    ActivityStack launchStack = getLaunchStack(activityRecord2, this.mLaunchFlags, activityRecord2.getTaskRecord(), this.mOptions);
                    TaskRecord intentTask2 = intentActivity.getTaskRecord();
                    if (launchStack == null) {
                        intentTask = intentTask2;
                    } else if (launchStack == this.mTargetStack) {
                        intentTask = intentTask2;
                    } else {
                        if (launchStack.inSplitScreenWindowingMode()) {
                            if ((this.mLaunchFlags & 4096) != 0) {
                                intentTask2.reparent(launchStack, true, 0, true, true, "launchToSide");
                            } else {
                                this.mTargetStack.moveTaskToFrontLocked(intentTask2, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringToFrontInsteadOfAdjacentLaunch");
                            }
                            this.mMovedToFront = launchStack != launchStack.getDisplay().getTopStackInWindowingMode(launchStack.getWindowingMode());
                        } else if (launchStack.mDisplayId != this.mTargetStack.mDisplayId) {
                            if (HwPCUtils.isPcCastModeInServer()) {
                                HwPCUtils.log("ActivityTaskManager", " the activity will reparentToDisplay because computer stack is:" + launchStack.mStackId + "#" + launchStack.mDisplayId + " target stack is " + this.mTargetStack.mStackId + "#" + this.mTargetStack.mDisplayId);
                            }
                            this.mService.mHwATMSEx.moveStackToFrontEx(this.mOptions, this.mTargetStack, this.mStartActivity, this.mSourceRecord, this.mLaunchParams.mBounds);
                            if (launchStack.mDisplayId != this.mTargetStack.mDisplayId) {
                                intentActivity.getTaskRecord().reparent(launchStack, true, 0, true, true, "reparentToDisplay");
                            } else if (launchStack.getChildCount() == 0) {
                                launchStack.remove();
                                this.mTargetStack.moveToFront("intentActivityFoundOnOtherDisplay");
                            }
                            this.mMovedToFront = true;
                        } else if (launchStack.isActivityTypeHome() && !this.mTargetStack.isActivityTypeHome()) {
                            intentActivity.getTaskRecord().reparent(launchStack, true, 0, true, true, "reparentingHome");
                            this.mMovedToFront = true;
                        }
                        if (launchStack != null && launchStack.getAllTasks().isEmpty() && HwPCUtils.isExtDynamicStack(launchStack.mStackId)) {
                            launchStack.remove();
                        }
                        this.mOptions = null;
                        Flog.i(301, "setTargetStackAndMoveToFront--->>>showStartingWindow for r:" + intentActivity);
                        if (!INCALLUI_ACTIVITY_CLASS_NAME.equals(intentActivity.shortComponentName) && !this.mShouldSkipStartingWindow) {
                            intentActivity.showStartingWindow(null, false, true);
                            this.mShouldSkipStartingWindow = false;
                        }
                    }
                    if (this.mTargetStack.getWindowingMode() == 12) {
                        this.mService.mHwATMSEx.exitCoordinationMode(false, true);
                    }
                    moveFreeFormFromFullscreen(launchStack, intentTask);
                    this.mService.mHwATMSEx.moveStackToFrontEx(this.mOptions, this.mTargetStack, this.mStartActivity, this.mSourceRecord, this.mLaunchParams.mBounds);
                    this.mTargetStack.moveTaskToFrontLocked(intentTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringingFoundTaskToFront");
                    this.mMovedToFront = true;
                    launchStack.remove();
                    this.mOptions = null;
                    Flog.i(301, "setTargetStackAndMoveToFront--->>>showStartingWindow for r:" + intentActivity);
                    intentActivity.showStartingWindow(null, false, true);
                    this.mShouldSkipStartingWindow = false;
                }
            }
        }
        this.mTargetStack = intentActivity.getActivityStack();
        if (!this.mMovedToFront && this.mDoResume) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.d("ActivityTaskManager", "Bring to front target: " + this.mTargetStack + " from " + intentActivity);
            }
            if (differentTopTask) {
                this.mService.mHwATMSEx.moveStackToFrontEx(this.mOptions, this.mTargetStack, this.mStartActivity, this.mSourceRecord, this.mLaunchParams.mBounds);
            }
            this.mTargetStack.moveToFront("intentActivityFound");
        }
        this.mSupervisor.handleNonResizableTaskIfNeeded(intentActivity.getTaskRecord(), 0, 0, this.mTargetStack);
        if ((this.mLaunchFlags & 2097152) != 0) {
            return this.mTargetStack.resetTaskIfNeededLocked(intentActivity, this.mStartActivity);
        }
        return intentActivity;
    }

    private void moveFreeFormFromFullscreen(ActivityStack launchStack, TaskRecord intentTask) {
        ActivityOptions activityOptions;
        if (HwFreeFormUtils.isFreeFormEnable() && launchStack != null && !this.mTargetStack.inFreeformWindowingMode() && (activityOptions = this.mOptions) != null && activityOptions.getLaunchWindowingMode() == 5) {
            ActivityStack nextStack = this.mService.mRootActivityContainer.getNextFocusableStack(this.mTargetStack, true);
            String underPkgName = (nextStack == null || nextStack.getTopActivity() == null) ? "" : nextStack.getTopActivity().packageName;
            HwFreeFormUtils.log("ams", "move reused activity to freeform from fullscreen above " + underPkgName);
            ActivityStack freeformStack = this.mTargetStack;
            intentTask.mIsReparenting = true;
            freeformStack.setFreeFormStackVisible(true);
            freeformStack.setCurrentPkgUnderFreeForm(underPkgName);
            freeformStack.setWindowingMode(5, false, false, false, true, false);
            updateBounds(intentTask, this.mLaunchParams.mBounds);
            intentTask.mIsReparenting = false;
            this.mShouldSkipStartingWindow = true;
        }
    }

    private void setTaskFromIntentActivity(ActivityRecord intentActivity) {
        int i = this.mLaunchFlags;
        if ((i & 268468224) == 268468224) {
            TaskRecord task = intentActivity.getTaskRecord();
            task.performClearTaskLocked();
            this.mReuseTask = task;
            this.mReuseTask.setIntent(this.mStartActivity);
        } else if ((i & 67108864) != 0 || isLaunchModeOneOf(3, 2)) {
            if (intentActivity.getTaskRecord().performClearTaskLocked(this.mStartActivity, this.mLaunchFlags) == null) {
                this.mAddingToTask = true;
                if (!isInSkipCancelResultList(this.mStartActivity.shortComponentName)) {
                    this.mStartActivity.setTask(null);
                }
                this.mSourceRecord = intentActivity;
                TaskRecord task2 = this.mSourceRecord.getTaskRecord();
                if (task2 != null && task2.getStack() == null) {
                    this.mTargetStack = computeStackFocus(this.mSourceRecord, false, this.mLaunchFlags, this.mOptions);
                    this.mTargetStack.addTask(task2, true ^ this.mLaunchTaskBehind, "startActivityUnchecked");
                }
            }
        } else if (this.mStartActivity.mActivityComponent.equals(intentActivity.getTaskRecord().realActivity)) {
            if (((this.mLaunchFlags & 536870912) != 0 || 1 == this.mLaunchMode) && intentActivity.mActivityComponent.equals(this.mStartActivity.mActivityComponent)) {
                if (intentActivity.frontOfTask) {
                    intentActivity.getTaskRecord().setIntent(this.mStartActivity);
                }
                deliverNewIntent(intentActivity);
            } else if (intentActivity.getTaskRecord().isSameIntentFilter(this.mStartActivity)) {
            } else {
                if (!this.mStartActivity.intent.filterEquals(intentActivity.intent) || !"android.intent.action.MAIN".equals(this.mStartActivity.intent.getAction())) {
                    this.mAddingToTask = true;
                    this.mSourceRecord = intentActivity;
                }
            }
        } else if ((this.mLaunchFlags & 2097152) == 0) {
            this.mAddingToTask = true;
            this.mSourceRecord = intentActivity;
        } else if (!intentActivity.getTaskRecord().rootWasReset) {
            intentActivity.getTaskRecord().setIntent(this.mStartActivity);
        }
    }

    private void resumeTargetStackIfNeeded() {
        if (this.mDoResume) {
            this.mRootActivityContainer.resumeFocusedStacksTopActivities(this.mTargetStack, null, this.mOptions);
        } else {
            ActivityOptions.abort(this.mOptions);
        }
        this.mRootActivityContainer.updateUserStack(this.mStartActivity.mUserId, this.mTargetStack);
    }

    private int setTaskFromReuseOrCreateNewTask(TaskRecord taskToAffiliate) {
        ActivityStack activityStack;
        ActivityOptions activityOptions;
        TaskRecord taskRecord;
        if (this.mRestrictedBgActivity && (((taskRecord = this.mReuseTask) == null || !taskRecord.containsAppUid(this.mCallingUid)) && handleBackgroundActivityAbort(this.mStartActivity))) {
            return WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT;
        }
        this.mTargetStack = computeStackFocus(this.mStartActivity, true, this.mLaunchFlags, this.mOptions);
        if (this.mTargetStack != null) {
            Rect newRect = this.mService.mHwATMSEx.handleStackFromOneStep(this.mStartActivity, this.mTargetStack, this.mSourceRecord);
            if (!newRect.isEmpty()) {
                this.mLaunchParams.mBounds.set(newRect);
            }
            this.mService.mHwATMSEx.oneStepHwMultiWindowBdReport(this.mStartActivity, this.mTargetStack.getWindowingMode(), this.mOptions);
        }
        this.mHwActivityStarterEx.moveFreeFormToFullScreenStackIfNeed(this.mStartActivity, this.mTargetStack.inFreeformWindowingMode() || this.mLaunchParams.mWindowingMode == 5);
        TaskRecord taskRecord2 = this.mReuseTask;
        if (taskRecord2 == null) {
            ActivityStack activityStack2 = this.mTargetStack;
            int nextTaskIdForUserLocked = this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.mUserId);
            ActivityInfo activityInfo = this.mNewTaskInfo;
            if (activityInfo == null) {
                activityInfo = this.mStartActivity.info;
            }
            Intent intent = this.mNewTaskIntent;
            if (intent == null) {
                intent = this.mIntent;
            }
            addOrReparentStartingActivity(activityStack2.createTaskRecord(nextTaskIdForUserLocked, activityInfo, intent, this.mVoiceSession, this.mVoiceInteractor, !this.mLaunchTaskBehind, this.mStartActivity, this.mSourceRecord, this.mOptions), "setTaskFromReuseOrCreateNewTask - mReuseTask");
            boolean isBoundsReused = false;
            if (this.mTargetStack.getWindowingMode() == 102 && this.mLaunchParams.mBounds.isEmpty()) {
                String appPackage = this.mStartActivity.packageName;
                try {
                    if (this.mIntent != null && this.mService.mHwATMSEx.isStartAppLock(this.mIntent.getPackage(), this.mIntent.getAction())) {
                        appPackage = new Intent(this.mIntent).getStringExtra("android.intent.extra.PACKAGE_NAME");
                    }
                    if (this.mIntent != null && DOCKBAR_PACKAGE_NAME.equals(this.mIntent.getPackage())) {
                        appPackage = new Intent(this.mIntent).getStringExtra(MULTI_TASK_APP_PACKAGE);
                    }
                } catch (BadParcelableException e) {
                    Slog.w("ActivityTaskManager", "setTaskFromReuseOrCreateNewTask get extra data error.");
                }
                float reuseScale = this.mService.mHwATMSEx.getReusableHwFreeFormBounds(appPackage, this.mStartActivity.mUserId, this.mLaunchParams.mBounds);
                if (!this.mLaunchParams.mBounds.isEmpty()) {
                    isBoundsReused = true;
                }
                ActivityOptions activityOptions2 = this.mOptions;
                if (activityOptions2 != null && reuseScale > 0.0f) {
                    activityOptions2.setStackScale(reuseScale);
                }
            }
            if (((this.mTargetStack.inHwMultiStackWindowingMode() && !this.mTargetStack.inHwSplitScreenWindowingMode()) || this.mTargetStack.inCoordinationSecondaryWindowingMode()) && !this.mLaunchParams.mBounds.isEmpty()) {
                ActivityOptions activityOptions3 = this.mOptions;
                if (!(activityOptions3 == null || activityOptions3.getStackScale() <= 0.0f || this.mTargetStack.getTaskStack() == null)) {
                    this.mTargetStack.getTaskStack().mHwStackScale = this.mOptions.getStackScale();
                }
                this.mTargetStack.resize(this.mLaunchParams.mBounds, null, null);
                if (isBoundsReused) {
                    this.mService.mHwATMSEx.updateDragFreeFormPos(this.mTargetStack);
                }
                ActivityOptions activityOptions4 = this.mOptions;
                if (activityOptions4 != null) {
                    this.mTargetStack.setPendingShow(activityOptions4.isPendingShow());
                }
            }
            updateBounds(this.mStartActivity.getTaskRecord(), this.mLaunchParams.mBounds);
            if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                Slog.v("ActivityTaskManager", "Starting new activity " + this.mStartActivity + " in new task " + this.mStartActivity.getTaskRecord());
            }
        } else {
            addOrReparentStartingActivity(taskRecord2, "setTaskFromReuseOrCreateNewTask");
            if (HwFreeFormUtils.isFreeFormEnable() && (activityStack = this.mTargetStack) != null && !activityStack.inFreeformWindowingMode() && (activityOptions = this.mOptions) != null && activityOptions.getLaunchWindowingMode() == 5) {
                ActivityStack nextStack = this.mService.mRootActivityContainer.getNextFocusableStack(this.mTargetStack, true);
                String underPkgName = (nextStack == null || nextStack.getTopActivity() == null) ? "" : nextStack.getTopActivity().packageName;
                HwFreeFormUtils.log("ams", "move new create activity to freeform from fullscreen above " + underPkgName);
                ActivityStack freeformStack = this.mTargetStack.getDisplay().getOrCreateStack(5, this.mTargetStack.getActivityType(), true);
                this.mReuseTask.mIsReparenting = true;
                freeformStack.setFreeFormStackVisible(true);
                freeformStack.setCurrentPkgUnderFreeForm(underPkgName);
                this.mReuseTask.reparent(freeformStack, true, 1, true, true, "reparentToFreeForm");
                updateBounds(this.mReuseTask, this.mLaunchParams.mBounds);
                this.mReuseTask.mIsReparenting = false;
                this.mTargetStack = freeformStack;
            }
        }
        if (taskToAffiliate != null) {
            this.mStartActivity.setTaskToAffiliateWith(taskToAffiliate);
        }
        if (!this.mService.getLockTaskController().isLockTaskModeViolation(this.mStartActivity.getTaskRecord()) || (this.mCallingUid == 1000 && (this.mStartActivity.intent.getHwFlags() & 65536) != 0)) {
            if (this.mDoResume) {
                this.mTargetStack.moveToFront("reuseOrNewTask");
            }
            return 0;
        }
        Slog.e("ActivityTaskManager", "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
        return 101;
    }

    private void deliverNewIntent(ActivityRecord activity) {
        if (!this.mIntentDelivered) {
            ActivityStack.logStartActivity(30003, activity, activity.getTaskRecord());
            activity.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            this.mIntentDelivered = true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:92:0x01c2  */
    private int setTaskFromSourceRecord() {
        int targetDisplayId;
        Intent intent;
        ActivityOptions activityOptions;
        ActivityRecord top;
        ActivityStack activityStack;
        if (this.mService.getLockTaskController().isLockTaskModeViolation(this.mSourceRecord.getTaskRecord())) {
            Slog.e("ActivityTaskManager", "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        TaskRecord sourceTask = this.mSourceRecord.getTaskRecord();
        ActivityStack sourceStack = this.mSourceRecord.getActivityStack();
        if (this.mRestrictedBgActivity && !sourceTask.containsAppUid(this.mCallingUid) && handleBackgroundActivityAbort(this.mStartActivity)) {
            return WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT;
        }
        ActivityStack activityStack2 = this.mTargetStack;
        if (activityStack2 != null) {
            targetDisplayId = activityStack2.mDisplayId;
        } else {
            targetDisplayId = sourceStack.mDisplayId;
        }
        if (sourceStack.topTask() != sourceTask || !this.mStartActivity.canBeLaunchedOnDisplay(targetDisplayId)) {
            ActivityRecord activityRecord = this.mStartActivity;
            this.mTargetStack = getLaunchStack(activityRecord, this.mLaunchFlags, activityRecord.getTaskRecord(), this.mOptions);
            if (this.mTargetStack == null && targetDisplayId != sourceStack.mDisplayId) {
                this.mTargetStack = this.mRootActivityContainer.getValidLaunchStackOnDisplay(sourceStack.mDisplayId, this.mStartActivity, this.mOptions, this.mLaunchParams);
            }
            if (this.mTargetStack == null) {
                this.mTargetStack = this.mRootActivityContainer.getNextValidLaunchStack(this.mStartActivity, -1);
            }
        }
        ActivityStack activityStack3 = this.mTargetStack;
        if (activityStack3 == null) {
            this.mTargetStack = sourceStack;
        } else if (activityStack3 != sourceStack) {
            sourceTask.reparent(activityStack3, true, 0, false, true, "launchToSide");
        }
        if (HwFreeFormUtils.isFreeFormEnable() && sourceStack.inFreeformWindowingMode() && (activityStack = this.mTargetStack) == sourceStack) {
            this.mHwActivityStarterEx.moveFreeFormToFullScreenStackIfNeed(this.mStartActivity, activityStack.inFreeformWindowingMode());
            this.mTargetStack = sourceStack;
        }
        if (this.mTargetStack.topTask() != sourceTask && !this.mAvoidMoveToFront) {
            this.mTargetStack.moveTaskToFrontLocked(sourceTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "sourceTaskToFront");
        } else if (this.mDoResume) {
            ActivityStack stack = this.mRootActivityContainer.getStack(5, 1);
            if (stack != null && stack.isTopActivityVisible()) {
                stack.getWindowConfiguration().setAlwaysOnTop(true);
                HwFreeFormUtils.log("ActivityTaskManager", "set freeformStack always on top.");
            }
            this.mTargetStack.moveToFront("sourceStackToFront");
        }
        if (!this.mAddingToTask) {
            int i = this.mLaunchFlags;
            if ((67108864 & i) != 0) {
                ActivityRecord top2 = sourceTask.performClearTaskLocked(this.mStartActivity, i);
                this.mKeepCurTransition = true;
                if (top2 != null) {
                    ActivityStack.logStartActivity(30003, this.mStartActivity, top2.getTaskRecord());
                    deliverNewIntent(top2);
                    this.mTargetStack.mLastPausedActivity = null;
                    if (this.mDoResume) {
                        this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                    }
                    ActivityOptions.abort(this.mOptions);
                    return 3;
                }
                if (this.mTargetStack == sourceStack && (intent = this.mIntent) != null && (intent.getHwFlags() & 1048576) != 0 && (activityOptions = this.mOptions) != null && activityOptions.getAnimationType() == 1 && this.mOptions.getCustomEnterResId() == 34209874) {
                    this.mStartActivity.updateOptionsLocked(ActivityOptions.makeBasic());
                }
                addOrReparentStartingActivity(sourceTask, "setTaskFromSourceRecord");
                if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                    Slog.v("ActivityTaskManager", "Starting new activity " + this.mStartActivity + " in existing task " + this.mStartActivity.getTaskRecord() + " from source " + this.mSourceRecord);
                }
                return 0;
            }
        }
        if (!(this.mAddingToTask || (this.mLaunchFlags & 131072) == 0 || (top = sourceTask.findActivityInHistoryLocked(this.mStartActivity)) == null)) {
            TaskRecord task = top.getTaskRecord();
            task.moveActivityToFrontLocked(top);
            top.updateOptionsLocked(this.mOptions);
            ActivityStack.logStartActivity(30003, this.mStartActivity, task);
            deliverNewIntent(top);
            this.mTargetStack.mLastPausedActivity = null;
            if (this.mDoResume) {
                this.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
            return 3;
        }
        this.mStartActivity.updateOptionsLocked(ActivityOptions.makeBasic());
        addOrReparentStartingActivity(sourceTask, "setTaskFromSourceRecord");
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
        }
        return 0;
    }

    private int setTaskFromInTask() {
        if (this.mService.getLockTaskController().isLockTaskModeViolation(this.mInTask)) {
            Slog.e("ActivityTaskManager", "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        this.mTargetStack = this.mInTask.getStack();
        ActivityRecord top = this.mInTask.getTopActivity();
        if (top != null && top.mActivityComponent.equals(this.mStartActivity.mActivityComponent) && top.mUserId == this.mStartActivity.mUserId && ((this.mLaunchFlags & 536870912) != 0 || isLaunchModeOneOf(1, 2))) {
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
                ActivityStack stack = this.mRootActivityContainer.getLaunchStack(null, null, this.mInTask, true);
                if (stack != this.mInTask.getStack()) {
                    this.mInTask.reparent(stack, true, 1, false, true, "inTaskToFront");
                    this.mTargetStack = this.mInTask.getStack();
                }
                updateBounds(this.mInTask, this.mLaunchParams.mBounds);
            }
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            addOrReparentStartingActivity(this.mInTask, "setTaskFromInTask");
            if (!ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
                return 0;
            }
            Slog.v("ActivityTaskManager", "Starting new activity " + this.mStartActivity + " in explicit task " + this.mStartActivity.getTaskRecord());
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

    private int setTaskToCurrentTopOrCreateNewTask() {
        this.mTargetStack = computeStackFocus(this.mStartActivity, false, this.mLaunchFlags, this.mOptions);
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("addingToTopTask");
        }
        ActivityRecord prev = this.mTargetStack.getTopActivity();
        if (this.mRestrictedBgActivity && prev == null && handleBackgroundActivityAbort(this.mStartActivity)) {
            return WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT;
        }
        TaskRecord task = prev != null ? prev.getTaskRecord() : this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.mUserId), this.mStartActivity.info, this.mIntent, null, null, true, this.mStartActivity, this.mSourceRecord, this.mOptions);
        if (this.mRestrictedBgActivity && prev != null && !task.containsAppUid(this.mCallingUid) && handleBackgroundActivityAbort(this.mStartActivity)) {
            return WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT;
        }
        addOrReparentStartingActivity(task, "setTaskToCurrentTopOrCreateNewTask");
        this.mTargetStack.positionChildWindowContainerAtTop(task);
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.v("ActivityTaskManager", "Starting new activity " + this.mStartActivity + " in new guessed " + this.mStartActivity.getTaskRecord());
        }
        return 0;
    }

    private void addOrReparentStartingActivity(TaskRecord parent, String reason) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Slog.v("ActivityTaskManager", "addOrReparentStartingActivity reason: " + reason);
        }
        if (this.mStartActivity.getTaskRecord() == null || this.mStartActivity.getTaskRecord() == parent) {
            parent.addActivityToTop(this.mStartActivity);
        } else {
            this.mStartActivity.reparent(parent, parent.mActivities.size(), reason);
        }
    }

    private int adjustLaunchFlagsToDocumentMode(ActivityRecord r, boolean launchSingleInstance, boolean launchSingleTask, int launchFlags) {
        if ((launchFlags & 524288) == 0 || (!launchSingleInstance && !launchSingleTask)) {
            int i = r.info.documentLaunchMode;
            if (i == 0) {
                return launchFlags;
            }
            if (i == 1) {
                return launchFlags | 524288;
            }
            if (i == 2) {
                return launchFlags | 524288;
            }
            if (i != 3) {
                return launchFlags;
            }
            return launchFlags & -134217729;
        }
        Slog.i("ActivityTaskManager", "Ignoring FLAG_ACTIVITY_NEW_DOCUMENT, launchMode is \"singleInstance\" or \"singleTask\"");
        return launchFlags & -134742017;
    }

    private ActivityStack computeStackFocus(ActivityRecord r, boolean newTask, int launchFlags, ActivityOptions aOptions) {
        TaskRecord task = r.getTaskRecord();
        ActivityStack stack = getLaunchStack(r, launchFlags, task, aOptions);
        if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
            Slog.d("ActivityTaskManager", "getLaunchStack stack:" + stack);
        }
        if (stack != null) {
            return stack;
        }
        ActivityStack currentStack = task != null ? task.getStack() : null;
        ActivityStack focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        if (currentStack != null) {
            if (focusedStack != currentStack) {
                if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                    Slog.d("ActivityTaskManager", "computeStackFocus: Setting focused stack to r=" + r + " task=" + task);
                }
            } else if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.d("ActivityTaskManager", "computeStackFocus: Focused stack already=" + focusedStack);
            }
            return currentStack;
        } else if (canLaunchIntoFocusedStack(r, newTask)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.d("ActivityTaskManager", "computeStackFocus: Have a focused stack=" + focusedStack);
            }
            return focusedStack;
        } else {
            int i = this.mPreferredDisplayId;
            if (i != 0 && (stack = this.mRootActivityContainer.getValidLaunchStackOnDisplay(i, r, aOptions, this.mLaunchParams)) == null) {
                if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                    Slog.d("ActivityTaskManager", "computeStackFocus: Can't launch on mPreferredDisplayId=" + this.mPreferredDisplayId + ", looking on all displays.");
                }
                stack = this.mRootActivityContainer.getNextValidLaunchStack(r, this.mPreferredDisplayId);
            }
            if (stack == null) {
                stack = this.mRootActivityContainer.getLaunchStack(r, aOptions, task, true);
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS || ActivityTaskManagerDebugConfig.DEBUG_STACK) {
                Slog.d("ActivityTaskManager", "computeStackFocus: New stack r=" + r + " stackId=" + stack.mStackId);
            }
            return stack;
        }
    }

    private boolean canLaunchIntoFocusedStack(ActivityRecord r, boolean newTask) {
        boolean canUseFocusedStack;
        ActivityStack focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        if (focusedStack.isActivityTypeAssistant()) {
            canUseFocusedStack = r.isActivityTypeAssistant();
        } else {
            int windowingMode = focusedStack.getWindowingMode();
            if (windowingMode == 1) {
                canUseFocusedStack = true;
            } else if (windowingMode == 3 || windowingMode == 4) {
                canUseFocusedStack = r.supportsSplitScreenWindowingMode();
            } else if (windowingMode == 5) {
                canUseFocusedStack = r.supportsFreeform();
            } else if (HwPCUtils.isExtDynamicStack(focusedStack.mStackId)) {
                return false;
            } else {
                canUseFocusedStack = !focusedStack.isOnHomeDisplay() && r.canBeLaunchedOnDisplay(focusedStack.mDisplayId);
            }
        }
        return canUseFocusedStack && !newTask && this.mPreferredDisplayId == focusedStack.mDisplayId;
    }

    private ActivityStack getLaunchStack(ActivityRecord r, int launchFlags, TaskRecord task, ActivityOptions aOptions) {
        TaskRecord taskRecord = this.mReuseTask;
        if (taskRecord != null) {
            return taskRecord.getStack();
        }
        boolean onTop = true;
        if ((launchFlags & 4096) == 0 || this.mPreferredDisplayId != 0) {
            if (aOptions != null && aOptions.getAvoidMoveToFront()) {
                onTop = false;
            }
            moveFreeFormStackIfNeed(r, aOptions);
            return this.mRootActivityContainer.getLaunchStack(r, aOptions, task, onTop, this.mLaunchParams);
        }
        ActivityStack focusedStack = this.mRootActivityContainer.getTopDisplayFocusedStack();
        ActivityStack parentStack = task != null ? task.getStack() : focusedStack;
        if (parentStack != focusedStack) {
            return parentStack;
        }
        if (focusedStack != null && task == focusedStack.topTask()) {
            return focusedStack;
        }
        if (parentStack == null || !parentStack.inSplitScreenPrimaryWindowingMode()) {
            ActivityStack dockedStack = this.mRootActivityContainer.getDefaultDisplay().getSplitScreenPrimaryStack();
            if (dockedStack == null || dockedStack.shouldBeVisible(r)) {
                return dockedStack;
            }
            return this.mRootActivityContainer.getLaunchStack(r, aOptions, task, true);
        }
        return parentStack.getDisplay().getOrCreateStack(4, this.mRootActivityContainer.resolveActivityType(r, this.mOptions, task), true);
    }

    private void moveFreeFormStackIfNeed(ActivityRecord startActivity, ActivityOptions activityOptions) {
        if (startActivity != null && activityOptions != null && activityOptions.getLaunchWindowingMode() == 5) {
            boolean isFreeFormExist = false;
            ActivityStack freeFormStack = this.mService.getRootActivityContainer().getStack(5, 1);
            if (freeFormStack != null) {
                ActivityRecord topActivity = freeFormStack.topRunningActivityLocked();
                isFreeFormExist = true;
                if (topActivity != null && this.mService.isInFreeformWhiteList(startActivity.packageName) && this.mService.isInFreeformWhiteList(topActivity.packageName) && ((!startActivity.packageName.equals(topActivity.packageName) || startActivity.mUserId != topActivity.mUserId) && HwFreeFormUtils.getFreeFormStackVisible())) {
                    this.mSupervisor.mHwActivityStackSupervisorEx.removeFreeFromStackLocked();
                    isFreeFormExist = false;
                }
            }
            String activityTitle = startActivity.info.getComponentName().flattenToShortString();
            if (!isFreeFormExist && HwFreeFormUtils.sExitFreeformActivity.contains(activityTitle)) {
                activityOptions.setLaunchWindowingMode(1);
            }
        }
    }

    private boolean isLaunchModeOneOf(int mode1, int mode2) {
        int i = this.mLaunchMode;
        return mode1 == i || mode2 == i;
    }

    private boolean isLaunchModeSingleTop() {
        return (this.mLaunchFlags & 536870912) != 0 || this.mLaunchMode == 1;
    }

    private boolean isSystemLaunchMultiTask() {
        return ((this.mCallingUid != 1000 && !DOCKBAR_PACKAGE_NAME.equals(this.mRequest.callingPackage)) || this.mStartActivity.intent == null || (this.mStartActivity.intent.getHwFlags() & 131072) == 0) ? false : true;
    }

    private boolean isStartActivityInMultiTask() {
        String basePackage = null;
        Intent baseIntent = this.mStartActivity.task != null ? this.mStartActivity.task.getBaseIntent() : null;
        if (baseIntent == null || (baseIntent.getHwFlags() & 131072) == 0) {
            return false;
        }
        if (baseIntent.getComponent() != null) {
            basePackage = baseIntent.getComponent().getPackageName();
        }
        if (basePackage == null || !basePackage.equals(this.mStartActivity.packageName)) {
            return false;
        }
        return true;
    }

    private boolean isStartFromRecents() {
        return (this.mInTask == null || this.mStartActivity.intent == null || (this.mStartActivity.intent.getFlags() & 1048576) == 0) ? false : true;
    }

    private boolean isAllowedLaunchMultiTask() {
        return isSystemLaunchMultiTask() || isStartActivityInMultiTask() || isStartFromRecents();
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
    @VisibleForTesting
    public int getCallingUid() {
        return this.mRequest.callingUid;
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
        Request request = this.mRequest;
        request.mayWait = true;
        request.userId = userId;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setAllowPendingRemoteAnimationRegistryLookup(boolean allowLookup) {
        this.mRequest.allowPendingRemoteAnimationRegistryLookup = allowLookup;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setOriginatingPendingIntent(PendingIntentRecord originatingPendingIntent) {
        this.mRequest.originatingPendingIntent = originatingPendingIntent;
        return this;
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter setAllowBackgroundActivityStart(boolean allowBackgroundActivityStart) {
        this.mRequest.allowBackgroundActivityStart = allowBackgroundActivityStart;
        return this;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        String prefix2 = prefix + "  ";
        pw.print(prefix2);
        pw.print("mCurrentUser=");
        pw.println(this.mRootActivityContainer.mCurrentUser);
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
