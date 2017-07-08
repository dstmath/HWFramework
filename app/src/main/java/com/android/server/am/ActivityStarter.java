package com.android.server.am;

import android.app.ActivityManager.StackId;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityContainer;
import android.app.IActivityManager.WaitResult;
import android.app.IApplicationThread;
import android.app.KeyguardManager;
import android.app.ProfilerInfo;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.service.voice.IVoiceInteractionSession;
import android.util.EventLog;
import android.util.Jlog;
import android.util.Slog;
import com.android.internal.app.HeavyWeightSwitcherActivity;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.HwServiceFactory;
import com.android.server.SystemService;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowManagerService.H;
import com.android.server.wm.WindowState;
import com.hisi.perfhub.PerfHub;
import com.huawei.pgmng.log.LogPower;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.Set;

public class ActivityStarter extends AbsActivityStarter {
    private static final String TAG = null;
    private static final String TAG_CONFIGURATION = null;
    private static final String TAG_FOCUS = null;
    private static final String TAG_RESULTS = null;
    private static final String TAG_USER_LEAVING = null;
    private boolean mAddingToTask;
    private boolean mAvoidMoveToFront;
    private int mCallingUid;
    String mCurActivityPkName;
    private HwCustActivityStackSupervisor mCustAss;
    private boolean mDoResume;
    private boolean mHasStartLauncher;
    private TaskRecord mInTask;
    private Intent mIntent;
    private ActivityStartInterceptor mInterceptor;
    private boolean mKeepCurTransition;
    private Rect mLaunchBounds;
    private int mLaunchFlags;
    private boolean mLaunchSingleInstance;
    private boolean mLaunchSingleTask;
    private boolean mLaunchSingleTop;
    private boolean mLaunchTaskBehind;
    private boolean mMovedOtherTask;
    private boolean mMovedToFront;
    private ActivityInfo mNewTaskInfo;
    private Intent mNewTaskIntent;
    private boolean mNoAnimation;
    private ActivityRecord mNotTop;
    private ActivityOptions mOptions;
    final ArrayList<PendingActivityLaunch> mPendingActivityLaunches;
    private PerfHub mPerfHub;
    private TaskRecord mReuseTask;
    private ActivityRecord mReusedActivity;
    final ActivityManagerService mService;
    private ActivityRecord mSourceRecord;
    private ActivityStack mSourceStack;
    private ActivityRecord mStartActivity;
    private int mStartFlags;
    private final ActivityStackSupervisor mSupervisor;
    private ActivityStack mTargetStack;
    private IVoiceInteractor mVoiceInteractor;
    private IVoiceInteractionSession mVoiceSession;
    private WindowManagerService mWindowManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ActivityStarter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.ActivityStarter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityStarter.<clinit>():void");
    }

    private void reset() {
        this.mStartActivity = null;
        this.mIntent = null;
        this.mCallingUid = -1;
        this.mOptions = null;
        this.mLaunchSingleTop = false;
        this.mLaunchSingleInstance = false;
        this.mLaunchSingleTask = false;
        this.mLaunchTaskBehind = false;
        this.mLaunchFlags = 0;
        this.mLaunchBounds = null;
        this.mNotTop = null;
        this.mDoResume = false;
        this.mStartFlags = 0;
        this.mSourceRecord = null;
        this.mInTask = null;
        this.mAddingToTask = false;
        this.mReuseTask = null;
        this.mNewTaskInfo = null;
        this.mNewTaskIntent = null;
        this.mSourceStack = null;
        this.mTargetStack = null;
        this.mMovedOtherTask = false;
        this.mMovedToFront = false;
        this.mNoAnimation = false;
        this.mKeepCurTransition = false;
        this.mAvoidMoveToFront = false;
        this.mVoiceSession = null;
        this.mVoiceInteractor = null;
    }

    ActivityStarter(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        this.mPendingActivityLaunches = new ArrayList();
        this.mCurActivityPkName = null;
        this.mHasStartLauncher = false;
        this.mCustAss = (HwCustActivityStackSupervisor) HwCustUtils.createObj(HwCustActivityStackSupervisor.class, new Object[0]);
        this.mService = service;
        this.mSupervisor = supervisor;
        this.mInterceptor = new ActivityStartInterceptor(this.mService, this.mSupervisor);
        this.mCurActivityPkName = "";
    }

    final int startActivityLocked(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, ActivityContainer container, TaskRecord inTask) {
        ProcessRecord processRecord = null;
        if (caller != null) {
            processRecord = this.mService.getRecordForAppLocked(caller);
            if (processRecord != null) {
                if (intent.hasCategory("android.intent.category.HOME")) {
                    this.mService.checkIfScreenStatusRequestAndSendBroadcast();
                }
            }
        }
        if (aInfo != null) {
            this.mService.noteActivityStart(aInfo.applicationInfo.packageName, aInfo.processName, 0, aInfo.applicationInfo.uid, true);
        }
        if (intent.getComponent() == null || !HwDeviceManager.disallowOp(4, intent.getComponent().getPackageName())) {
            int err = 0;
            if (HwFrameworkFactory.getVRSystemServiceManager().isVRMode() && aInfo != null && !HwFrameworkFactory.getVRSystemServiceManager().isVRApplication(this.mService.mContext, aInfo.packageName)) {
                return 0;
            }
            String str;
            StringBuilder append;
            int i;
            int err2;
            if (caller != null) {
                processRecord = this.mService.getRecordForAppLocked(caller);
                if (processRecord != null) {
                    callingPid = processRecord.pid;
                    callingUid = processRecord.info.uid;
                } else {
                    Slog.w(TAG, "Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting: " + intent.toString());
                    err = -4;
                }
            }
            int userId = aInfo != null ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0;
            if (!this.mHasStartLauncher && this.mService.isStartLauncherActivity(intent)) {
                Slog.w(TAG, "check the USER_SETUP_COMPLETE is set 1 in first start launcher!");
                this.mService.forceValidateHomeButton();
                this.mHasStartLauncher = true;
            }
            if (err == 0) {
                str = TAG;
                append = new StringBuilder().append("START u").append(userId).append(" {").append(intent.toShortString(true, true, true, false)).append("} from uid ").append(callingUid).append(" on display ");
                i = container == null ? this.mSupervisor.mFocusedStack == null ? 0 : this.mSupervisor.mFocusedStack.mDisplayId : container.mActivityDisplay == null ? 0 : container.mActivityDisplay.mDisplayId;
                Slog.i(str, append.append(i).toString());
                this.mSupervisor.recognitionMaliciousApp(caller, intent);
                ComponentName cmp = intent.getComponent();
                String strPkg = "";
                if (cmp != null) {
                    strPkg = cmp.getPackageName();
                }
                if (intent.getCategories() != null && intent.getCategories().toString().contains("android.intent.category.LAUNCHER")) {
                    this.mService.getRecordCust().appEnterRecord(strPkg);
                }
                Jlog.perfEvent(5, "", new int[0]);
                if (this.mPerfHub == null) {
                    this.mPerfHub = new PerfHub();
                }
                if (this.mPerfHub != null) {
                    this.mPerfHub.perfEvent(5, "", new int[0]);
                }
                if (this.mCurActivityPkName == null || !this.mCurActivityPkName.equals(strPkg)) {
                    if (this.mPerfHub == null) {
                        this.mPerfHub = new PerfHub();
                    }
                    if (this.mPerfHub != null) {
                        this.mPerfHub.perfEvent(4, "", new int[0]);
                    }
                    this.mService.notifyAppEventToIaware(1500, strPkg);
                    LogPower.push(139, strPkg);
                } else {
                    this.mService.notifyAppEventToIaware(SystemService.PHASE_SYSTEM_SERVICES_READY, strPkg);
                }
            }
            ActivityRecord activityRecord = null;
            ActivityRecord resultRecord = null;
            if (resultTo != null) {
                activityRecord = this.mSupervisor.isInAnyStackLocked(resultTo);
                if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                    Slog.v(TAG_RESULTS, "Will send result to " + resultTo + " " + activityRecord);
                }
                if (!(activityRecord == null || requestCode < 0 || activityRecord.finishing)) {
                    resultRecord = activityRecord;
                }
            }
            int launchFlags = intent.getFlags();
            if (!((33554432 & launchFlags) == 0 || activityRecord == null)) {
                if (requestCode >= 0) {
                    ActivityOptions.abort(options);
                    return -3;
                }
                resultRecord = activityRecord.resultTo;
                if (!(resultRecord == null || resultRecord.isInStackLocked())) {
                    resultRecord = null;
                }
                resultWho = activityRecord.resultWho;
                requestCode = activityRecord.requestCode;
                activityRecord.resultTo = null;
                if (resultRecord != null) {
                    resultRecord.removeResultsLocked(activityRecord, resultWho, requestCode);
                }
                if (activityRecord.launchedFromUid == callingUid) {
                    callingPackage = activityRecord.launchedFromPackage;
                }
            }
            if (err == 0 && intent.getComponent() == null) {
                err = -1;
            }
            if (err == 0 && aInfo == null) {
                err = -2;
            }
            if (!(err != 0 || activityRecord == null || activityRecord.task.voiceSession == null || (268435456 & launchFlags) != 0 || (activityRecord.info.applicationInfo.uid == aInfo.applicationInfo.uid && activityRecord.info.applicationInfo.euid == aInfo.applicationInfo.euid))) {
                try {
                    intent.addCategory("android.intent.category.VOICE");
                    if (!AppGlobals.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                        Slog.w(TAG, "Activity being started in current voice task does not support voice: " + intent);
                        err = -7;
                    }
                } catch (Throwable e) {
                    Slog.w(TAG, "Failure checking voice capabilities", e);
                    err = -7;
                }
            }
            if (err != 0 || voiceSession == null) {
                err2 = err;
            } else {
                try {
                    if (!AppGlobals.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                        Slog.w(TAG, "Activity being started in new voice task does not support: " + intent);
                        err = -7;
                    }
                    err2 = err;
                } catch (Throwable e2) {
                    Slog.w(TAG, "Failure checking voice capabilities", e2);
                    err2 = -7;
                }
            }
            ActivityStack activityStack = resultRecord == null ? null : resultRecord.task.stack;
            if (err2 != 0) {
                if (resultRecord != null) {
                    activityStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
                }
                ActivityOptions.abort(options);
                return err2;
            }
            int abort = (!this.mSupervisor.checkStartAnyActivityPermission(intent, aInfo, resultWho, requestCode, callingPid, callingUid, callingPackage, ignoreTargetSecurity, processRecord, resultRecord, activityStack, options)) | (this.mService.mIntentFirewall.checkStartActivity(intent, callingUid, callingPid, resolvedType, aInfo.applicationInfo) ? 0 : 1);
            if (abort == 0 && this.mService.shouldPreventStartActivity(aInfo, callingUid, callingPid, callingPackage, userId)) {
                abort = 1;
            }
            if (this.mService.mController != null) {
                try {
                    abort |= this.mService.mController.activityStarting(intent.cloneFilter(), aInfo.applicationInfo.packageName) ? 0 : 1;
                } catch (RemoteException e3) {
                    this.mService.mController = null;
                }
            }
            if (this.mService.mCustomController != null) {
                abort |= this.mService.customActivityStarting(intent, aInfo.applicationInfo.packageName);
            }
            this.mInterceptor.setStates(userId, realCallingPid, realCallingUid, startFlags, callingPackage);
            err = rInfo;
            this.mInterceptor.intercept(intent, err, aInfo, resolvedType, inTask, callingPid, callingUid, options);
            intent = this.mInterceptor.mIntent;
            rInfo = this.mInterceptor.mRInfo;
            aInfo = this.mInterceptor.mAInfo;
            resolvedType = this.mInterceptor.mResolvedType;
            inTask = this.mInterceptor.mInTask;
            callingPid = this.mInterceptor.mCallingPid;
            callingUid = this.mInterceptor.mCallingUid;
            options = this.mInterceptor.mActivityOptions;
            if (abort != 0) {
                if (!(resultRecord == null || activityStack == null)) {
                    activityStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
                }
                ActivityOptions.abort(options);
                return 0;
            }
            int flags;
            if (Build.PERMISSIONS_REVIEW_REQUIRED && aInfo != null && this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(aInfo.packageName, userId)) {
                IIntentSender target = this.mService.getIntentSenderLocked(2, callingPackage, callingUid, userId, null, null, 0, new Intent[]{intent}, new String[]{resolvedType}, 1342177280, null);
                flags = intent.getFlags();
                Intent intent2 = new Intent("android.intent.action.REVIEW_PERMISSIONS");
                intent2.setFlags(8388608 | flags);
                intent2.putExtra("android.intent.extra.PACKAGE_NAME", aInfo.packageName);
                intent2.putExtra("android.intent.extra.INTENT", new IntentSender(target));
                if (resultRecord != null) {
                    intent2.putExtra("android.intent.extra.RESULT_NEEDED", true);
                }
                intent = intent2;
                resolvedType = null;
                callingUid = realCallingUid;
                callingPid = realCallingPid;
                rInfo = this.mSupervisor.resolveIntent(intent2, null, userId);
                aInfo = this.mSupervisor.resolveActivity(intent2, rInfo, startFlags, null);
                if (ActivityManagerDebugConfig.DEBUG_PERMISSIONS_REVIEW) {
                    str = TAG;
                    append = new StringBuilder().append("START u").append(userId).append(" {").append(intent2.toShortString(true, true, true, false)).append("} from uid ").append(realCallingUid).append(" on display ");
                    i = container == null ? this.mSupervisor.mFocusedStack == null ? 0 : this.mSupervisor.mFocusedStack.mDisplayId : container.mActivityDisplay == null ? 0 : container.mActivityDisplay.mDisplayId;
                    Slog.i(str, append.append(i).toString());
                }
            }
            if (!(rInfo == null || rInfo.ephemeralResolveInfo == null)) {
                IIntentSender failureTarget = this.mService.getIntentSenderLocked(2, callingPackage, Binder.getCallingUid(), userId, null, null, 0, new Intent[]{intent}, new String[]{resolvedType}, 1409286144, null);
                ephemeralIntent.setPackage(rInfo.ephemeralResolveInfo.getPackageName());
                IIntentSender ephemeralTarget = this.mService.getIntentSenderLocked(2, callingPackage, Binder.getCallingUid(), userId, null, null, 0, new Intent[]{ephemeralIntent}, new String[]{resolvedType}, 1409286144, null);
                flags = intent.getFlags();
                intent = new Intent();
                intent.setFlags((268435456 | flags) | 8388608);
                intent.putExtra("android.intent.extra.PACKAGE_NAME", rInfo.ephemeralResolveInfo.getPackageName());
                intent.putExtra("android.intent.extra.EPHEMERAL_FAILURE", new IntentSender(failureTarget));
                intent.putExtra("android.intent.extra.EPHEMERAL_SUCCESS", new IntentSender(ephemeralTarget));
                resolvedType = null;
                callingUid = realCallingUid;
                callingPid = realCallingPid;
                aInfo = this.mSupervisor.resolveActivity(intent, rInfo.ephemeralInstaller, startFlags, null);
            }
            Slog.i(TAG, "ActivityRecord info: " + aInfo + ", euid: " + (aInfo != null ? aInfo.applicationInfo.euid : 0));
            ActivityRecord r = HwServiceFactory.createActivityRecord(this.mService, processRecord, callingUid, callingPackage, intent, resolvedType, aInfo, this.mService.mConfiguration, resultRecord, resultWho, requestCode, componentSpecified, voiceSession != null, this.mSupervisor, container, options, activityRecord);
            if (this.mService.shouldPreventActivity(intent, aInfo, r)) {
                Slog.w(TAG, "forbiden launch for activity: " + r);
                ActivityOptions.abort(options);
                return 4;
            }
            if (outActivity != null) {
                outActivity[0] = r;
            }
            if (r.appTimeTracker == null && activityRecord != null) {
                r.appTimeTracker = activityRecord.appTimeTracker;
            }
            ActivityStack stack = this.mSupervisor.mFocusedStack;
            if (voiceSession == null && stack.mResumedActivity == null && this.mService.mFocusedActivity != null && r.shortComponentName != null && !r.shortComponentName.equals(this.mService.mFocusedActivity.shortComponentName) && this.mService.isStartLauncherActivity(intent) && "com.android.incallui/.InCallActivity".equals(this.mService.mFocusedActivity.shortComponentName) && ((stack.mLastPausedActivity == null || !"com.android.incallui/.InCallActivity".equals(stack.mLastPausedActivity.shortComponentName)) && !this.mService.isSleepingLocked())) {
                Slog.d(TAG, "abort launch for activity: " + r);
                ActivityOptions.abort(options);
                return 4;
            } else if (this.mService.isSleepingLocked() && r.shortComponentName != null && "com.tencent.news/.push.alive.offactivity.OffActivity".equals(r.shortComponentName)) {
                Slog.i(TAG, "abort launch for activity: " + r);
                ActivityOptions.abort(options);
                return 4;
            } else {
                if (voiceSession == null && (stack.mResumedActivity == null || stack.mResumedActivity.info.applicationInfo.uid != callingUid)) {
                    err = callingUid;
                    if (!this.mService.checkAppSwitchAllowedLocked(callingPid, err, realCallingPid, realCallingUid, "Activity start")) {
                        this.mPendingActivityLaunches.add(new PendingActivityLaunch(r, activityRecord, startFlags, stack, processRecord));
                        ActivityOptions.abort(options);
                        return 4;
                    }
                }
                if (this.mService.mDidAppSwitch) {
                    this.mService.mAppSwitchesAllowedTime = 0;
                } else {
                    this.mService.mDidAppSwitch = true;
                }
                doPendingActivityLaunchesLocked(false);
                try {
                    this.mService.mWindowManager.deferSurfaceLayout();
                    err = startActivityUnchecked(r, activityRecord, voiceSession, voiceInteractor, startFlags, true, options, inTask);
                    postStartActivityUncheckedProcessing(r, err, stack.mStackId, this.mSourceRecord, this.mTargetStack);
                    return err;
                } finally {
                    this.mService.mWindowManager.continueSurfaceLayout();
                }
            }
        } else {
            Slog.i(TAG, "[" + intent.getComponent().getPackageName() + "] is disallowed running by MDM apk");
            return -6;
        }
    }

    void postStartActivityUncheckedProcessing(ActivityRecord r, int result, int prevFocusedStackId, ActivityRecord sourceRecord, ActivityStack targetStack) {
        ActivityRecord topActivityHomeStack = null;
        if (result < 0) {
            this.mSupervisor.notifyActivityDrawnForKeyguard();
            return;
        }
        if (result == 2 && !this.mSupervisor.mWaitingActivityLaunched.isEmpty()) {
            this.mSupervisor.reportTaskToFrontNoLaunch(this.mStartActivity);
        }
        int startedActivityStackId = -1;
        if (r.task != null && r.task.stack != null) {
            startedActivityStackId = r.task.stack.mStackId;
        } else if (this.mTargetStack != null) {
            startedActivityStackId = targetStack.mStackId;
        }
        boolean noDisplayActivityOverHome = (sourceRecord == null || !sourceRecord.noDisplay) ? false : sourceRecord.task.getTaskToReturnTo() == 1;
        if (startedActivityStackId == 3 && (prevFocusedStackId == 0 || noDisplayActivityOverHome)) {
            ActivityStack homeStack = this.mSupervisor.getStack(0);
            if (homeStack != null) {
                topActivityHomeStack = homeStack.topRunningActivityLocked();
            }
            if (topActivityHomeStack == null || topActivityHomeStack.mActivityType != 2) {
                Slog.d(TAG, "Scheduling recents launch.");
                this.mWindowManager.showRecentApps(true);
                return;
            }
        }
        if (startedActivityStackId == 4 && (result == 2 || result == 3)) {
            this.mService.notifyPinnedActivityRestartAttemptLocked();
        }
    }

    void startHomeActivityLocked(Intent intent, ActivityInfo aInfo, String reason) {
        this.mSupervisor.moveHomeStackTaskToTop(1, reason);
        startActivityLocked(null, intent, null, null, aInfo, null, null, null, null, null, 0, 0, 0, null, 0, 0, 0, null, false, false, null, null, null);
        if (this.mSupervisor.inResumeTopActivity) {
            this.mSupervisor.scheduleResumeTopActivities();
        }
    }

    void showConfirmDeviceCredential(int userId) {
        ActivityStack targetStack;
        ActivityStack fullscreenStack = this.mSupervisor.getStack(1);
        ActivityStack freeformStack = this.mSupervisor.getStack(2);
        if (fullscreenStack != null && fullscreenStack.getStackVisibilityLocked(null) != 0) {
            targetStack = fullscreenStack;
        } else if (freeformStack == null || freeformStack.getStackVisibilityLocked(null) == 0) {
            targetStack = this.mSupervisor.getStack(0);
        } else {
            targetStack = freeformStack;
        }
        if (targetStack != null) {
            Intent credential = ((KeyguardManager) this.mService.mContext.getSystemService("keyguard")).createConfirmDeviceCredentialIntent(null, null, userId);
            if (credential != null) {
                ActivityRecord activityRecord = targetStack.topRunningActivityLocked();
                if (activityRecord != null) {
                    credential.putExtra("android.intent.extra.INTENT", new IntentSender(this.mService.getIntentSenderLocked(2, activityRecord.launchedFromPackage, activityRecord.launchedFromUid, activityRecord.userId, null, null, 0, new Intent[]{activityRecord.intent}, new String[]{activityRecord.resolvedType}, 1409286144, null)));
                    startConfirmCredentialIntent(credential);
                }
            }
        }
    }

    void startConfirmCredentialIntent(Intent intent) {
        intent.addFlags(276840448);
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchTaskId(this.mSupervisor.getHomeActivity().task.taskId);
        this.mService.mContext.startActivityAsUser(intent, options.toBundle(), UserHandle.CURRENT);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration config, Bundle bOptions, boolean ignoreTargetSecurity, int userId, IActivityContainer iContainer, TaskRecord inTask) {
        ActivityInfo aInfo;
        Throwable th;
        if (intent == null || !intent.hasFileDescriptors()) {
            ResolveInfo rInfo;
            ActivityOptions options;
            ActivityContainer container;
            int realCallingPid;
            int realCallingUid;
            int callingPid;
            ActivityStack stack;
            boolean z;
            long origId;
            ActivityInfo aInfo2;
            ActivityRecord[] outRecord;
            int res;
            ActivityRecord r;
            this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunching();
            boolean z2 = intent.getComponent() != null;
            Intent intent2 = new Intent(intent);
            intent2 = new Intent(intent);
            if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                Set<String> categories = intent2.getCategories();
                if (categories != null) {
                    if (categories.contains("android.intent.category.HOME")) {
                        intent2.removeCategory("android.intent.category.HOME");
                        intent2.addFlags(4194304);
                        intent2.setClassName("com.huawei.android.launcher", "com.huawei.android.launcher.powersavemode.PowerSaveModeLauncher");
                        if (this.mCustAss != null) {
                            this.mCustAss.modifyIntentForLauncher3(intent2);
                        }
                    }
                }
            }
            ResolveInfo rInfo2 = this.mSupervisor.resolveIntent(intent2, resolvedType, userId);
            if (standardizeHomeIntent(rInfo2, intent2)) {
                z2 = false;
            }
            if (rInfo2 == null) {
                UserInfo userInfo = this.mSupervisor.getUserInfo(userId);
                if (userInfo == null) {
                    rInfo = rInfo2;
                } else if (userInfo.isManagedProfile()) {
                    UserManager userManager = UserManager.get(this.mService.mContext);
                    long token = Binder.clearCallingIdentity();
                    try {
                        boolean profileLockedAndParentUnlockingOrUnlocked;
                        UserInfo parent = userManager.getProfileParent(userId);
                        if (parent != null) {
                            if (userManager.isUserUnlockingOrUnlocked(parent.id)) {
                                profileLockedAndParentUnlockingOrUnlocked = !userManager.isUserUnlockingOrUnlocked(userId);
                                Binder.restoreCallingIdentity(token);
                                if (profileLockedAndParentUnlockingOrUnlocked) {
                                    rInfo = this.mSupervisor.resolveIntent(intent2, resolvedType, userId, 786432);
                                }
                            }
                        }
                        profileLockedAndParentUnlockingOrUnlocked = false;
                        Binder.restoreCallingIdentity(token);
                        if (profileLockedAndParentUnlockingOrUnlocked) {
                            rInfo = this.mSupervisor.resolveIntent(intent2, resolvedType, userId, 786432);
                        }
                    } catch (Throwable th2) {
                        Binder.restoreCallingIdentity(token);
                    }
                }
                aInfo = this.mSupervisor.resolveActivity(intent2, rInfo, startFlags, profilerInfo);
                if (!(aInfo == null || aInfo.applicationInfo == null || callingPackage == null)) {
                    if (callingPackage.equals(aInfo.applicationInfo.packageName)) {
                        Jlog.d(335, aInfo.applicationInfo.packageName, "");
                    }
                }
                if (!(!Jlog.isPerfTest() || aInfo == null || aInfo.applicationInfo == null)) {
                    Jlog.i(2023, "whopkg=" + callingPackage + "&pkg=" + aInfo.applicationInfo.packageName + "&cls=" + aInfo.name);
                }
                options = ActivityOptions.fromBundle(bOptions);
                container = (ActivityContainer) iContainer;
                synchronized (this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        if (container != null || container.mParentActivity == null || container.mParentActivity.state == ActivityState.RESUMED) {
                            realCallingPid = Binder.getCallingPid();
                            realCallingUid = Binder.getCallingUid();
                            if (callingUid >= 0) {
                                callingPid = -1;
                            } else if (caller != null) {
                                callingPid = realCallingPid;
                                callingUid = realCallingUid;
                            } else {
                                callingUid = -1;
                                callingPid = -1;
                            }
                            if (container != null || container.mStack.isOnHomeDisplay()) {
                                stack = this.mSupervisor.mFocusedStack;
                            } else {
                                stack = container.mStack;
                            }
                            z = config == null && this.mService.mConfiguration.diff(config) != 0;
                            stack.mConfigWillChange = z;
                            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.v(TAG_CONFIGURATION, "Starting activity when config will change = " + stack.mConfigWillChange);
                            }
                            origId = Binder.clearCallingIdentity();
                            if (aInfo != null || (aInfo.applicationInfo.privateFlags & 2) == 0) {
                                aInfo2 = aInfo;
                                rInfo2 = rInfo;
                                intent = intent2;
                            } else if (aInfo.processName.equals(aInfo.applicationInfo.packageName)) {
                                ProcessRecord heavy = this.mService.mHeavyWeightProcess;
                                if (heavy == null || (heavy.info.uid == aInfo.applicationInfo.uid && this.mService.mHeavyWeightProcess.info.euid == aInfo.applicationInfo.euid && heavy.processName.equals(aInfo.processName))) {
                                    aInfo2 = aInfo;
                                    rInfo2 = rInfo;
                                    intent = intent2;
                                } else {
                                    int appCallingUid = callingUid;
                                    if (caller != null) {
                                        ProcessRecord callerApp = this.mService.getRecordForAppLocked(caller);
                                        if (callerApp != null) {
                                            appCallingUid = callerApp.info.uid;
                                        } else {
                                            Slog.w(TAG, "Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting: " + intent2.toString());
                                            ActivityOptions.abort(options);
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            return -4;
                                        }
                                    }
                                    IIntentSender target = this.mService.getIntentSenderLocked(2, "android", appCallingUid, userId, null, null, 0, new Intent[]{intent2}, new String[]{resolvedType}, 1342177280, null);
                                    Intent newIntent = new Intent();
                                    if (requestCode >= 0) {
                                        newIntent.putExtra("has_result", true);
                                    }
                                    newIntent.putExtra(HwBroadcastRadarUtil.KEY_BROADCAST_INTENT, new IntentSender(target));
                                    if (heavy.activities.size() > 0) {
                                        ActivityRecord hist = (ActivityRecord) heavy.activities.get(0);
                                        newIntent.putExtra("cur_app", hist.packageName);
                                        newIntent.putExtra("cur_task", hist.task.taskId);
                                    }
                                    newIntent.putExtra("new_app", aInfo.packageName);
                                    newIntent.setFlags(intent2.getFlags());
                                    newIntent.setClassName("android", HeavyWeightSwitcherActivity.class.getName());
                                    intent = newIntent;
                                    resolvedType = null;
                                    caller = null;
                                    try {
                                        callingUid = Binder.getCallingUid();
                                        callingPid = Binder.getCallingPid();
                                        z2 = true;
                                        rInfo2 = this.mSupervisor.resolveIntent(newIntent, null, userId);
                                        if (rInfo2 != null) {
                                            try {
                                                aInfo2 = rInfo2.activityInfo;
                                            } catch (Throwable th3) {
                                                th = th3;
                                                aInfo2 = aInfo;
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                throw th;
                                            }
                                        }
                                        aInfo2 = null;
                                        if (aInfo2 != null) {
                                            aInfo2 = this.mService.getActivityInfoForUser(aInfo2, userId);
                                        }
                                    } catch (Throwable th4) {
                                        th = th4;
                                        aInfo2 = aInfo;
                                        rInfo2 = rInfo;
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                }
                            } else {
                                aInfo2 = aInfo;
                                rInfo2 = rInfo;
                                intent = intent2;
                            }
                            this.mService.addCallerToIntent(intent, caller);
                            if (HwDeviceManager.disallowOp(intent)) {
                                outRecord = new ActivityRecord[1];
                                res = startActivityLocked(caller, intent, intent2, resolvedType, aInfo2, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, z2, outRecord, container, inTask);
                                Binder.restoreCallingIdentity(origId);
                                if (stack.mConfigWillChange) {
                                    this.mService.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
                                    stack.mConfigWillChange = false;
                                    if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                        Slog.v(TAG_CONFIGURATION, "Updating to new configuration after starting activity.");
                                    }
                                    this.mService.updateConfigurationLocked(config, null, false);
                                }
                                if (outResult != null) {
                                    outResult.result = res;
                                    if (res == 0) {
                                        this.mSupervisor.mWaitingActivityLaunched.add(outResult);
                                        while (true) {
                                            try {
                                                this.mService.wait();
                                            } catch (InterruptedException e) {
                                            }
                                            try {
                                                if (!(outResult.result == 2 || outResult.timeout)) {
                                                    if (outResult.who == null) {
                                                        break;
                                                    }
                                                }
                                            } catch (Throwable th5) {
                                                th = th5;
                                            }
                                        }
                                        if (outResult.result == 2) {
                                            res = 2;
                                        }
                                    }
                                    if (res == 2) {
                                        r = stack.topRunningActivityLocked();
                                        if (r.nowVisible || r.state != ActivityState.RESUMED) {
                                            outResult.thisTime = SystemClock.uptimeMillis();
                                            this.mSupervisor.mWaitingActivityVisible.add(outResult);
                                            while (true) {
                                                try {
                                                    this.mService.wait();
                                                } catch (InterruptedException e2) {
                                                }
                                                if (!outResult.timeout && outResult.who == null) {
                                                }
                                            }
                                        } else {
                                            outResult.timeout = false;
                                            outResult.who = new ComponentName(r.info.packageName, r.info.name);
                                            outResult.totalTime = 0;
                                            outResult.thisTime = 0;
                                        }
                                    }
                                }
                                this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunched(res, this.mReusedActivity == null ? this.mReusedActivity : outRecord[0]);
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return res;
                            }
                            Slog.i(TAG, "due to disallow op launching activity aborted");
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return -6;
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return -6;
                    } catch (Throwable th6) {
                        th = th6;
                        aInfo2 = aInfo;
                        rInfo2 = rInfo;
                        intent = intent2;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            }
            rInfo = rInfo2;
            aInfo = this.mSupervisor.resolveActivity(intent2, rInfo, startFlags, profilerInfo);
            if (callingPackage.equals(aInfo.applicationInfo.packageName)) {
                Jlog.d(335, aInfo.applicationInfo.packageName, "");
            }
            Jlog.i(2023, "whopkg=" + callingPackage + "&pkg=" + aInfo.applicationInfo.packageName + "&cls=" + aInfo.name);
            options = ActivityOptions.fromBundle(bOptions);
            container = (ActivityContainer) iContainer;
            synchronized (this.mService) {
                ActivityManagerService.boostPriorityForLockedSection();
                if (container != null) {
                }
                realCallingPid = Binder.getCallingPid();
                realCallingUid = Binder.getCallingUid();
                if (callingUid >= 0) {
                    callingPid = -1;
                } else if (caller != null) {
                    callingUid = -1;
                    callingPid = -1;
                } else {
                    callingPid = realCallingPid;
                    callingUid = realCallingUid;
                }
                if (container != null) {
                }
                stack = this.mSupervisor.mFocusedStack;
                if (config == null) {
                }
                stack.mConfigWillChange = z;
                if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Starting activity when config will change = " + stack.mConfigWillChange);
                }
                origId = Binder.clearCallingIdentity();
                if (aInfo != null) {
                }
                aInfo2 = aInfo;
                rInfo2 = rInfo;
                intent = intent2;
                this.mService.addCallerToIntent(intent, caller);
                if (HwDeviceManager.disallowOp(intent)) {
                    outRecord = new ActivityRecord[1];
                    res = startActivityLocked(caller, intent, intent2, resolvedType, aInfo2, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, z2, outRecord, container, inTask);
                    Binder.restoreCallingIdentity(origId);
                    if (stack.mConfigWillChange) {
                        this.mService.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
                        stack.mConfigWillChange = false;
                        if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Updating to new configuration after starting activity.");
                        }
                        this.mService.updateConfigurationLocked(config, null, false);
                    }
                    if (outResult != null) {
                        outResult.result = res;
                        if (res == 0) {
                            this.mSupervisor.mWaitingActivityLaunched.add(outResult);
                            while (true) {
                                this.mService.wait();
                                if (outResult.who == null) {
                                    break;
                                    if (outResult.result == 2) {
                                        res = 2;
                                    }
                                }
                            }
                        }
                        if (res == 2) {
                            r = stack.topRunningActivityLocked();
                            if (r.nowVisible) {
                            }
                            outResult.thisTime = SystemClock.uptimeMillis();
                            this.mSupervisor.mWaitingActivityVisible.add(outResult);
                            while (true) {
                                this.mService.wait();
                            }
                        }
                    }
                    if (this.mReusedActivity == null) {
                    }
                    this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunched(res, this.mReusedActivity == null ? this.mReusedActivity : outRecord[0]);
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return res;
                }
                Slog.i(TAG, "due to disallow op launching activity aborted");
                ActivityManagerService.resetPriorityAfterLockedSection();
                return -6;
            }
        }
        throw new IllegalArgumentException("File descriptors passed in Intent");
    }

    final int startActivities(IApplicationThread caller, int callingUid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle bOptions, int userId) {
        if (intents == null) {
            throw new NullPointerException("intents is null");
        } else if (resolvedTypes == null) {
            throw new NullPointerException("resolvedTypes is null");
        } else if (intents.length != resolvedTypes.length) {
            throw new IllegalArgumentException("intents are length different than resolvedTypes");
        } else {
            int callingPid;
            if (callingUid >= 0) {
                callingPid = -1;
            } else if (caller == null) {
                callingPid = Binder.getCallingPid();
                callingUid = Binder.getCallingUid();
            } else {
                callingUid = -1;
                callingPid = -1;
            }
            long origId = Binder.clearCallingIdentity();
            try {
                synchronized (this.mService) {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord[] outActivity = new ActivityRecord[1];
                    int i = 0;
                    while (i < intents.length) {
                        Intent intent = intents[i];
                        if (intent == null) {
                        } else {
                            if (intent != null) {
                                if (intent.hasFileDescriptors()) {
                                    throw new IllegalArgumentException("File descriptors passed in Intent");
                                }
                            }
                            boolean componentSpecified = intent.getComponent() != null;
                            Intent intent2 = new Intent(intent);
                            this.mService.addCallerToIntent(intent2, caller);
                            ActivityInfo aInfo = this.mService.getActivityInfoForUser(this.mSupervisor.resolveActivity(intent2, resolvedTypes[i], 0, null, userId), userId);
                            if (aInfo == null || (aInfo.applicationInfo.privateFlags & 2) == 0) {
                                int res = startActivityLocked(caller, intent2, null, resolvedTypes[i], aInfo, null, null, null, resultTo, null, -1, callingPid, callingUid, callingPackage, callingPid, callingUid, 0, ActivityOptions.fromBundle(i == intents.length + -1 ? bOptions : null), false, componentSpecified, outActivity, null, null);
                                if (res < 0) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    Binder.restoreCallingIdentity(origId);
                                    return res;
                                } else if (outActivity[0] != null) {
                                    Object obj = outActivity[0].appToken;
                                } else {
                                    resultTo = null;
                                }
                            } else {
                                throw new IllegalArgumentException("FLAG_CANT_SAVE_STATE not supported here");
                            }
                        }
                        i++;
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    Binder.restoreCallingIdentity(origId);
                    return 0;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(origId);
            }
        }
    }

    private int startActivityUnchecked(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask) {
        ActivityRecord top;
        setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor);
        computeLaunchingTaskFlags();
        computeSourceStack();
        this.mIntent.setFlags(this.mLaunchFlags);
        this.mReusedActivity = getReusableIntentActivity();
        int preferredLaunchStackId = this.mOptions != null ? this.mOptions.getLaunchStackId() : -1;
        if (this.mReusedActivity != null) {
            if (this.mSupervisor.isLockTaskModeViolation(this.mReusedActivity.task, (this.mLaunchFlags & 268468224) == 268468224)) {
                this.mSupervisor.showLockTaskToast();
                Slog.e(TAG, "startActivityUnchecked: Attempt to violate Lock Task Mode");
                return 5;
            }
            if (this.mStartActivity.task == null) {
                this.mStartActivity.task = this.mReusedActivity.task;
            }
            if (this.mReusedActivity.task.intent == null) {
                this.mReusedActivity.task.setIntent(this.mStartActivity);
            }
            if ((this.mLaunchFlags & 67108864) != 0 || this.mLaunchSingleInstance || this.mLaunchSingleTask) {
                top = this.mReusedActivity.task.performClearTaskForReuseLocked(this.mStartActivity, this.mLaunchFlags);
                if (top != null) {
                    if (top.frontOfTask) {
                        top.task.setIntent(this.mStartActivity);
                    }
                    ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, top.task);
                    top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
                }
            }
            this.mReusedActivity = setTargetStackAndMoveToFrontIfNeeded(this.mReusedActivity);
            if ((this.mStartFlags & 1) != 0) {
                resumeTargetStackIfNeeded();
                return 1;
            }
            setTaskFromIntentActivity(this.mReusedActivity);
            if (!this.mAddingToTask && this.mReuseTask == null) {
                resumeTargetStackIfNeeded();
                return 2;
            }
        }
        if (this.mStartActivity.packageName == null) {
            if (!(this.mStartActivity.resultTo == null || this.mStartActivity.resultTo.task.stack == null)) {
                this.mStartActivity.resultTo.task.stack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            }
            ActivityOptions.abort(this.mOptions);
            return -2;
        }
        boolean dontStart;
        ActivityStack topStack = this.mSupervisor.mFocusedStack;
        top = topStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        if (top != null && this.mStartActivity.resultTo == null && top.realActivity.equals(this.mStartActivity.realActivity) && top.multiLaunchId == this.mStartActivity.multiLaunchId && top.userId == this.mStartActivity.userId && top.app != null && top.app.thread != null) {
            boolean z;
            if ((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop) {
                z = true;
            } else {
                z = this.mLaunchSingleTask;
            }
            dontStart = z;
        } else {
            dontStart = false;
        }
        if (dontStart) {
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, top, top.task);
            topStack.mLastPausedActivity = null;
            if (this.mDoResume) {
                this.mSupervisor.resumeFocusedStackTopActivityLocked();
            }
            ActivityOptions.abort(this.mOptions);
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            this.mSupervisor.handleNonResizableTaskIfNeeded(top.task, preferredLaunchStackId, topStack.mStackId);
            return 3;
        }
        boolean newTask = false;
        TaskRecord taskRecord = (!this.mLaunchTaskBehind || this.mSourceRecord == null) ? null : this.mSourceRecord.task;
        if (this.mStartActivity.resultTo == null && this.mInTask == null && !this.mAddingToTask && (this.mLaunchFlags & 268435456) != 0) {
            newTask = true;
            setTaskFromReuseOrCreateNewTask(taskRecord);
            if (this.mSupervisor.isLockTaskModeViolation(this.mStartActivity.task)) {
                Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
                return 5;
            } else if (!this.mMovedOtherTask) {
                updateTaskReturnToType(this.mStartActivity.task, this.mLaunchFlags, topStack);
            }
        } else if (this.mSourceRecord != null) {
            if (this.mSupervisor.isLockTaskModeViolation(this.mSourceRecord.task)) {
                Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
                return 5;
            }
            result = setTaskFromSourceRecord();
            if (result != 0) {
                return result;
            }
        } else if (this.mInTask == null) {
            setTaskToCurrentTopOrCreateNewTask();
        } else if (this.mSupervisor.isLockTaskModeViolation(this.mInTask)) {
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 5;
        } else {
            result = setTaskFromInTask();
            if (result != 0) {
                return result;
            }
        }
        this.mService.grantUriPermissionFromIntentLocked(this.mCallingUid, this.mStartActivity.packageName, this.mIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.userId);
        if (this.mSourceRecord != null && this.mSourceRecord.isRecentsActivity()) {
            this.mStartActivity.task.setTaskToReturnTo(2);
        }
        if (newTask) {
            EventLog.writeEvent(EventLogTags.AM_CREATE_TASK, new Object[]{Integer.valueOf(this.mStartActivity.userId), Integer.valueOf(this.mStartActivity.task.taskId)});
        }
        ActivityStack.logStartActivity(EventLogTags.AM_CREATE_ACTIVITY, this.mStartActivity, this.mStartActivity.task);
        this.mTargetStack.mLastPausedActivity = null;
        this.mTargetStack.startActivityLocked(this.mStartActivity, newTask, this.mKeepCurTransition, this.mOptions);
        if (this.mDoResume) {
            if (!this.mLaunchTaskBehind) {
                this.mService.setFocusedActivityLocked(this.mStartActivity, "startedActivity");
            }
            ActivityRecord topTaskActivity = this.mStartActivity.task.topRunningActivityLocked();
            if (this.mTargetStack.isFocusable() && (topTaskActivity == null || !topTaskActivity.mTaskOverlay || this.mStartActivity == topTaskActivity)) {
                this.mSupervisor.resumeFocusedStackTopActivityLocked(this.mTargetStack, this.mStartActivity, this.mOptions);
            } else {
                this.mTargetStack.ensureActivitiesVisibleLocked(null, 0, false);
                this.mWindowManager.executeAppTransition();
            }
        } else {
            this.mTargetStack.addRecentActivityLocked(this.mStartActivity);
        }
        this.mSupervisor.updateUserStackLocked(this.mStartActivity.userId, this.mTargetStack);
        this.mSupervisor.handleNonResizableTaskIfNeeded(this.mStartActivity.task, preferredLaunchStackId, this.mTargetStack.mStackId);
        return 0;
    }

    private void setInitialState(ActivityRecord r, ActivityOptions options, TaskRecord inTask, boolean doResume, int startFlags, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        reset();
        this.mStartActivity = r;
        this.mIntent = r.intent;
        this.mOptions = options;
        this.mCallingUid = r.launchedFromUid;
        this.mSourceRecord = sourceRecord;
        this.mVoiceSession = voiceSession;
        this.mVoiceInteractor = voiceInteractor;
        this.mLaunchBounds = getOverrideBounds(r, options, inTask);
        this.mLaunchSingleTop = r.launchMode == 1;
        this.mLaunchSingleInstance = r.launchMode == 3;
        this.mLaunchSingleTask = r.launchMode == 2;
        this.mLaunchFlags = adjustLaunchFlagsToDocumentMode(r, this.mLaunchSingleInstance, this.mLaunchSingleTask, this.mIntent.getFlags());
        boolean z = (!r.mLaunchTaskBehind || this.mLaunchSingleTask || this.mLaunchSingleInstance) ? false : (this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0;
        this.mLaunchTaskBehind = z;
        sendNewTaskResultRequestIfNeeded();
        if ((this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0 && r.resultTo == null) {
            this.mLaunchFlags |= 268435456;
        }
        if ((this.mLaunchFlags & 268435456) != 0 && (this.mLaunchTaskBehind || r.info.documentLaunchMode == 2)) {
            this.mLaunchFlags |= 134217728;
        }
        this.mSupervisor.mUserLeaving = (this.mLaunchFlags & DumpState.DUMP_DOMAIN_PREFERRED) == 0;
        if (ActivityManagerDebugConfig.DEBUG_USER_LEAVING) {
            Slog.v(TAG_USER_LEAVING, "startActivity() => mUserLeaving=" + this.mSupervisor.mUserLeaving);
        }
        this.mDoResume = doResume;
        if (!(doResume && this.mSupervisor.okToShowLocked(r))) {
            r.delayedResume = true;
            this.mDoResume = false;
        }
        if (!(this.mOptions == null || this.mOptions.getLaunchTaskId() == -1 || !this.mOptions.getTaskOverlay())) {
            r.mTaskOverlay = true;
            TaskRecord task = this.mSupervisor.anyTaskForIdLocked(this.mOptions.getLaunchTaskId());
            ActivityRecord top = task != null ? task.getTopActivity() : null;
            if (!(top == null || top.visible)) {
                this.mDoResume = false;
                this.mAvoidMoveToFront = true;
            }
        }
        this.mNotTop = (this.mLaunchFlags & 16777216) != 0 ? r : null;
        this.mInTask = inTask;
        if (!(inTask == null || inTask.inRecents)) {
            Slog.w(TAG, "Starting activity in task not in recents: " + inTask);
            this.mInTask = null;
        }
        this.mStartFlags = startFlags;
        if ((startFlags & 1) != 0) {
            ActivityRecord checkedCaller = sourceRecord;
            if (sourceRecord == null) {
                checkedCaller = this.mSupervisor.mFocusedStack.topRunningNonDelayedActivityLocked(this.mNotTop);
            }
            if (!checkedCaller.realActivity.equals(r.realActivity)) {
                this.mStartFlags &= -2;
            }
        }
        if ((this.mLaunchFlags & DumpState.DUMP_INSTALLS) != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mNoAnimation = z;
    }

    private void sendNewTaskResultRequestIfNeeded() {
        if (this.mStartActivity.resultTo != null && (this.mLaunchFlags & 268435456) != 0 && this.mStartActivity.resultTo.task.stack != null) {
            Slog.w(TAG, "Activity is launching as a new task, so cancelling activity result.");
            this.mStartActivity.resultTo.task.stack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            this.mStartActivity.resultTo = null;
        }
    }

    private void computeLaunchingTaskFlags() {
        if (this.mSourceRecord != null || this.mInTask == null || this.mInTask.stack == null) {
            this.mInTask = null;
            if ((this.mStartActivity.isResolverActivity() || this.mStartActivity.noDisplay) && this.mSourceRecord != null && this.mSourceRecord.isFreeform()) {
                this.mAddingToTask = true;
            }
        } else {
            Intent baseIntent = this.mInTask.getBaseIntent();
            ActivityRecord root = this.mInTask.getRootActivity();
            if (baseIntent == null) {
                ActivityOptions.abort(this.mOptions);
                throw new IllegalArgumentException("Launching into task without base intent: " + this.mInTask);
            }
            if (this.mLaunchSingleInstance || this.mLaunchSingleTask) {
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
        }
        if (this.mInTask != null) {
            return;
        }
        if (this.mSourceRecord == null) {
            if ((this.mLaunchFlags & 268435456) == 0 && this.mInTask == null) {
                Slog.w(TAG, "startActivity called from non-Activity context; forcing Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                this.mLaunchFlags |= 268435456;
            }
        } else if (this.mSourceRecord.launchMode == 3) {
            this.mLaunchFlags |= 268435456;
        } else if (this.mLaunchSingleInstance || this.mLaunchSingleTask) {
            this.mLaunchFlags |= 268435456;
        }
    }

    private void computeSourceStack() {
        if (this.mSourceRecord == null) {
            this.mSourceStack = null;
        } else if (this.mSourceRecord.finishing) {
            if ((this.mLaunchFlags & 268435456) == 0) {
                Slog.w(TAG, "startActivity called from finishing " + this.mSourceRecord + "; forcing " + "Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                this.mLaunchFlags |= 268435456;
                this.mNewTaskInfo = this.mSourceRecord.info;
                this.mNewTaskIntent = this.mSourceRecord.task.intent;
            }
            this.mSourceRecord = null;
            this.mSourceStack = null;
        } else {
            this.mSourceStack = this.mSourceRecord.task.stack;
        }
    }

    private ActivityRecord getReusableIntentActivity() {
        boolean putIntoExistingTask;
        int i;
        boolean z = false;
        if (((this.mLaunchFlags & 268435456) == 0 || (this.mLaunchFlags & 134217728) != 0) && !this.mLaunchSingleInstance) {
            putIntoExistingTask = this.mLaunchSingleTask;
        } else {
            putIntoExistingTask = true;
        }
        if (this.mInTask == null && this.mStartActivity.resultTo == null) {
            i = 1;
        } else {
            i = 0;
        }
        putIntoExistingTask &= i;
        if (this.mOptions != null && this.mOptions.getLaunchTaskId() != -1) {
            TaskRecord task = this.mSupervisor.anyTaskForIdLocked(this.mOptions.getLaunchTaskId());
            if (task != null) {
                return task.getTopActivity();
            }
            return null;
        } else if (!putIntoExistingTask) {
            return null;
        } else {
            if (this.mLaunchSingleInstance) {
                return this.mSupervisor.findActivityLocked(this.mIntent, this.mStartActivity.info, false);
            }
            if ((this.mLaunchFlags & DumpState.DUMP_PREFERRED) == 0) {
                return this.mSupervisor.findTaskLocked(this.mStartActivity);
            }
            ActivityStackSupervisor activityStackSupervisor = this.mSupervisor;
            Intent intent = this.mIntent;
            ActivityInfo activityInfo = this.mStartActivity.info;
            if (!this.mLaunchSingleTask) {
                z = true;
            }
            return activityStackSupervisor.findActivityLocked(intent, activityInfo, z);
        }
    }

    private ActivityRecord setTargetStackAndMoveToFrontIfNeeded(ActivityRecord intentActivity) {
        this.mTargetStack = intentActivity.task.stack;
        this.mTargetStack.mLastPausedActivity = null;
        ActivityStack focusStack = this.mSupervisor.getFocusedStack();
        ActivityRecord curTop = focusStack == null ? null : focusStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        if (!(curTop == null || ((curTop.task == intentActivity.task && curTop.task == focusStack.topTask()) || this.mAvoidMoveToFront))) {
            this.mStartActivity.intent.addFlags(4194304);
            if (this.mSourceRecord == null || (this.mSourceStack.topActivity() != null && this.mSourceStack.topActivity().task == this.mSourceRecord.task)) {
                if (intentActivity.task.getTaskToReturnTo() == 1 && intentActivity.task != this.mTargetStack.topTask()) {
                    TaskRecord tr = (TaskRecord) this.mTargetStack.getAllTasks().get(this.mTargetStack.getAllTasks().indexOf(intentActivity.task) + 1);
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG, "The intentActivity is " + intentActivity + ",task is " + intentActivity.task + " then set the TaskToReturnTo=HOME of this task " + tr + " above it.");
                        Slog.d(TAG, "Bring to front target: " + this.mTargetStack + " from " + intentActivity);
                    }
                    if (tr != null) {
                        tr.setTaskToReturnTo(1);
                    }
                }
                if (this.mLaunchTaskBehind && this.mSourceRecord != null) {
                    intentActivity.setTaskToAffiliateWith(this.mSourceRecord.task);
                }
                this.mMovedOtherTask = true;
                if (!((this.mLaunchFlags & 268468224) == 268468224)) {
                    ActivityStack launchStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.task, this.mOptions);
                    if (launchStack == null || launchStack == this.mTargetStack) {
                        this.mTargetStack.moveTaskToFrontLocked(intentActivity.task, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringingFoundTaskToFront");
                        this.mMovedToFront = true;
                    } else if (launchStack.mStackId == 3 || launchStack.mStackId == 1) {
                        if ((this.mLaunchFlags & DumpState.DUMP_PREFERRED) != 0) {
                            this.mSupervisor.moveTaskToStackLocked(intentActivity.task.taskId, launchStack.mStackId, true, true, "launchToSide", true);
                        } else {
                            this.mTargetStack.moveTaskToFrontLocked(intentActivity.task, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringToFrontInsteadOfAdjacentLaunch");
                        }
                        this.mMovedToFront = true;
                    }
                    this.mOptions = null;
                }
                updateTaskReturnToType(intentActivity.task, this.mLaunchFlags, focusStack);
            }
        }
        if (!this.mMovedToFront && this.mDoResume) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityStackSupervisor.TAG_TASKS, "Bring to front target: " + this.mTargetStack + " from " + intentActivity);
            }
            this.mTargetStack.moveToFront("intentActivityFound");
        }
        this.mSupervisor.handleNonResizableTaskIfNeeded(intentActivity.task, -1, this.mTargetStack.mStackId);
        if ((this.mLaunchFlags & 2097152) != 0) {
            return this.mTargetStack.resetTaskIfNeededLocked(intentActivity, this.mStartActivity);
        }
        return intentActivity;
    }

    private void updateTaskReturnToType(TaskRecord task, int launchFlags, ActivityStack focusedStack) {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            task.setTaskToReturnTo(0);
        } else if ((launchFlags & 268451840) == 268451840) {
            task.setTaskToReturnTo(1);
        } else if (focusedStack == null || focusedStack.mStackId == 0) {
            task.setTaskToReturnTo(1);
        } else {
            task.setTaskToReturnTo(0);
        }
    }

    private void setTaskFromIntentActivity(ActivityRecord intentActivity) {
        boolean z = false;
        if ((this.mLaunchFlags & 268468224) == 268468224) {
            this.mReuseTask = intentActivity.task;
            this.mReuseTask.performClearTaskLocked();
            this.mReuseTask.setIntent(this.mStartActivity);
            this.mMovedOtherTask = true;
        } else if ((this.mLaunchFlags & 67108864) != 0 || this.mLaunchSingleInstance || this.mLaunchSingleTask) {
            if (intentActivity.task.performClearTaskLocked(this.mStartActivity, this.mLaunchFlags) == null) {
                this.mAddingToTask = true;
                this.mSourceRecord = intentActivity;
                TaskRecord task = this.mSourceRecord.task;
                if (task != null && task.stack == null) {
                    this.mTargetStack = computeStackFocus(this.mSourceRecord, false, null, this.mLaunchFlags, this.mOptions);
                    ActivityStack activityStack = this.mTargetStack;
                    if (!this.mLaunchTaskBehind) {
                        z = true;
                    }
                    activityStack.addTask(task, z, "startActivityUnchecked");
                }
            }
        } else if (this.mStartActivity.realActivity.equals(intentActivity.task.realActivity)) {
            if (((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop) && intentActivity.realActivity.equals(this.mStartActivity.realActivity)) {
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, intentActivity.task);
                if (intentActivity.frontOfTask) {
                    intentActivity.task.setIntent(this.mStartActivity);
                }
                intentActivity.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            } else if (!intentActivity.task.isSameIntentFilter(this.mStartActivity)) {
                if (!this.mStartActivity.intent.filterEquals(intentActivity.intent) || !"android.intent.action.MAIN".equals(this.mStartActivity.intent.getAction())) {
                    this.mAddingToTask = true;
                    this.mSourceRecord = intentActivity;
                }
            }
        } else if ((this.mLaunchFlags & 2097152) == 0) {
            this.mAddingToTask = true;
            this.mSourceRecord = intentActivity;
        } else if (!intentActivity.task.rootWasReset) {
            intentActivity.task.setIntent(this.mStartActivity);
        }
    }

    private void resumeTargetStackIfNeeded() {
        if (this.mDoResume) {
            this.mSupervisor.resumeFocusedStackTopActivityLocked(this.mTargetStack, null, this.mOptions);
            if (!this.mMovedToFront) {
                this.mSupervisor.notifyActivityDrawnForKeyguard();
            }
        } else {
            ActivityOptions.abort(this.mOptions);
        }
        this.mSupervisor.updateUserStackLocked(this.mStartActivity.userId, this.mTargetStack);
    }

    private void setTaskFromReuseOrCreateNewTask(TaskRecord taskToAffiliate) {
        this.mTargetStack = computeStackFocus(this.mStartActivity, true, this.mLaunchBounds, this.mLaunchFlags, this.mOptions);
        if (this.mReuseTask == null) {
            boolean z;
            ActivityStack activityStack = this.mTargetStack;
            int nextTaskIdForUserLocked = this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId);
            ActivityInfo activityInfo = this.mNewTaskInfo != null ? this.mNewTaskInfo : this.mStartActivity.info;
            Intent intent = this.mNewTaskIntent != null ? this.mNewTaskIntent : this.mIntent;
            IVoiceInteractionSession iVoiceInteractionSession = this.mVoiceSession;
            IVoiceInteractor iVoiceInteractor = this.mVoiceInteractor;
            if (this.mLaunchTaskBehind) {
                z = false;
            } else {
                z = true;
            }
            this.mStartActivity.setTask(activityStack.createTaskRecord(nextTaskIdForUserLocked, activityInfo, intent, iVoiceInteractionSession, iVoiceInteractor, z), taskToAffiliate);
            if (this.mLaunchBounds != null) {
                nextTaskIdForUserLocked = this.mTargetStack.mStackId;
                if (StackId.resizeStackWithLaunchBounds(nextTaskIdForUserLocked)) {
                    this.mService.resizeStack(nextTaskIdForUserLocked, this.mLaunchBounds, true, false, true, -1);
                } else {
                    this.mStartActivity.task.updateOverrideConfiguration(this.mLaunchBounds);
                }
            }
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in new task " + this.mStartActivity.task);
                return;
            }
            return;
        }
        this.mStartActivity.setTask(this.mReuseTask, taskToAffiliate);
    }

    private int setTaskFromSourceRecord() {
        TaskRecord sourceTask = this.mSourceRecord.task;
        if (sourceTask.stack.topTask() != sourceTask) {
            this.mTargetStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.task, this.mOptions);
        }
        if (this.mTargetStack == null) {
            this.mTargetStack = sourceTask.stack;
        } else if (this.mTargetStack != sourceTask.stack) {
            this.mSupervisor.moveTaskToStackLocked(sourceTask.taskId, this.mTargetStack.mStackId, true, true, "launchToSide", false);
        }
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("sourceStackToFront");
        }
        if (!(this.mTargetStack.topTask() == sourceTask || this.mAvoidMoveToFront)) {
            this.mTargetStack.moveTaskToFrontLocked(sourceTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "sourceTaskToFront");
        }
        ActivityRecord top;
        if (!this.mAddingToTask && (this.mLaunchFlags & 67108864) != 0) {
            top = sourceTask.performClearTaskLocked(this.mStartActivity, this.mLaunchFlags);
            this.mKeepCurTransition = true;
            if (top != null) {
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, top.task);
                top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
                this.mTargetStack.mLastPausedActivity = null;
                if (this.mDoResume) {
                    this.mSupervisor.resumeFocusedStackTopActivityLocked();
                }
                ActivityOptions.abort(this.mOptions);
                return 3;
            }
        } else if (!(this.mAddingToTask || (this.mLaunchFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) == 0)) {
            top = sourceTask.findActivityInHistoryLocked(this.mStartActivity);
            if (top != null) {
                TaskRecord task = top.task;
                task.moveActivityToFrontLocked(top);
                top.updateOptionsLocked(this.mOptions);
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, task);
                top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
                this.mTargetStack.mLastPausedActivity = null;
                if (this.mDoResume) {
                    this.mSupervisor.resumeFocusedStackTopActivityLocked();
                }
                return 3;
            }
        }
        this.mStartActivity.setTask(sourceTask, null);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in existing task " + this.mStartActivity.task + " from source " + this.mSourceRecord);
        }
        return 0;
    }

    private int setTaskFromInTask() {
        if (this.mLaunchBounds != null) {
            this.mInTask.updateOverrideConfiguration(this.mLaunchBounds);
            int stackId = this.mInTask.getLaunchStackId();
            if (stackId != this.mInTask.stack.mStackId) {
                stackId = this.mSupervisor.moveTaskToStackUncheckedLocked(this.mInTask, stackId, true, false, "inTaskToFront").mStackId;
            }
            if (StackId.resizeStackWithLaunchBounds(stackId)) {
                this.mService.resizeStack(stackId, this.mLaunchBounds, true, false, true, -1);
            }
        }
        this.mTargetStack = this.mInTask.stack;
        this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
        ActivityRecord top = this.mInTask.getTopActivity();
        if (top != null && top.realActivity.equals(this.mStartActivity.realActivity) && top.userId == this.mStartActivity.userId && top.multiLaunchId == this.mStartActivity.multiLaunchId && ((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop || this.mLaunchSingleTask)) {
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, top, top.task);
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            return 3;
        } else if (this.mAddingToTask) {
            this.mStartActivity.setTask(this.mInTask, null);
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in explicit task " + this.mStartActivity.task);
            }
            return 0;
        } else {
            ActivityOptions.abort(this.mOptions);
            return 2;
        }
    }

    private void setTaskToCurrentTopOrCreateNewTask() {
        this.mTargetStack = computeStackFocus(this.mStartActivity, false, null, this.mLaunchFlags, this.mOptions);
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("addingToTopTask");
        }
        ActivityRecord prev = this.mTargetStack.topActivity();
        this.mStartActivity.setTask(prev != null ? prev.task : this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId), this.mStartActivity.info, this.mIntent, null, null, true), null);
        this.mWindowManager.moveTaskToTop(this.mStartActivity.task.taskId);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in new guessed " + this.mStartActivity.task);
        }
    }

    private int adjustLaunchFlagsToDocumentMode(ActivityRecord r, boolean launchSingleInstance, boolean launchSingleTask, int launchFlags) {
        if ((launchFlags & DumpState.DUMP_FROZEN) == 0 || !(launchSingleInstance || launchSingleTask)) {
            switch (r.info.documentLaunchMode) {
                case WindowState.LOW_RESOLUTION_FEATURE_OFF /*0*/:
                    return launchFlags;
                case WindowState.LOW_RESOLUTION_COMPOSITION_OFF /*1*/:
                    return launchFlags | DumpState.DUMP_FROZEN;
                case WindowState.LOW_RESOLUTION_COMPOSITION_ON /*2*/:
                    return launchFlags | DumpState.DUMP_FROZEN;
                case H.REPORT_LOSING_FOCUS /*3*/:
                    return launchFlags & -134217729;
                default:
                    return launchFlags;
            }
        }
        Slog.i(TAG, "Ignoring FLAG_ACTIVITY_NEW_DOCUMENT, launchMode is \"singleInstance\" or \"singleTask\"");
        return launchFlags & -134742017;
    }

    final void doPendingActivityLaunchesLocked(boolean doResume) {
        while (!this.mPendingActivityLaunches.isEmpty()) {
            PendingActivityLaunch pal = (PendingActivityLaunch) this.mPendingActivityLaunches.remove(0);
            try {
                postStartActivityUncheckedProcessing(pal.r, startActivityUnchecked(pal.r, pal.sourceRecord, null, null, pal.startFlags, doResume ? this.mPendingActivityLaunches.isEmpty() : false, null, null), this.mSupervisor.mFocusedStack.mStackId, this.mSourceRecord, this.mTargetStack);
            } catch (Exception e) {
                Slog.e(TAG, "Exception during pending activity launch pal=" + pal, e);
                pal.sendErrorResult(e.getMessage());
            }
        }
    }

    private ActivityStack computeStackFocus(ActivityRecord r, boolean newTask, Rect bounds, int launchFlags, ActivityOptions aOptions) {
        TaskRecord task = r.task;
        boolean isApplicationTask = !r.isApplicationActivity() ? task != null ? task.isApplicationTask() : false : true;
        if (!isApplicationTask) {
            return this.mSupervisor.mHomeStack;
        }
        ActivityStack stack = getLaunchStack(r, launchFlags, task, aOptions);
        if (stack != null) {
            return stack;
        }
        if (task == null || task.stack == null) {
            ActivityContainer container = r.mInitialActivityContainer;
            if (container != null) {
                r.mInitialActivityContainer = null;
                return container.mStack;
            }
            int focusedStackId = this.mSupervisor.mFocusedStack.mStackId;
            boolean canUseFocusedStack = (focusedStackId == 1 || (focusedStackId == 3 && r.canGoInDockedStack())) ? true : focusedStackId == 2 ? r.isResizeableOrForced() : false;
            if (!canUseFocusedStack || (newTask && !this.mSupervisor.mFocusedStack.mActivityContainer.isEligibleForNewTasks())) {
                int stackId;
                ArrayList<ActivityStack> homeDisplayStacks = this.mSupervisor.mHomeStack.mStacks;
                int stackNdx = homeDisplayStacks.size() - 1;
                while (stackNdx >= 0) {
                    stack = (ActivityStack) homeDisplayStacks.get(stackNdx);
                    if (StackId.isStaticStack(stack.mStackId)) {
                        stackNdx--;
                    } else {
                        if (ActivityManagerDebugConfig.DEBUG_STACK) {
                            Slog.d(TAG_FOCUS, "computeStackFocus: Setting focused stack=" + stack);
                        }
                        return stack;
                    }
                }
                if (task != null) {
                    stackId = task.getLaunchStackId();
                } else if (bounds != null) {
                    stackId = 2;
                } else {
                    stackId = 1;
                }
                stack = this.mSupervisor.getStack(stackId, true, true);
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_FOCUS, "computeStackFocus: New stack r=" + r + " stackId=" + stack.mStackId);
                }
                return stack;
            }
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: Have a focused stack=" + this.mSupervisor.mFocusedStack);
            }
            return this.mSupervisor.mFocusedStack;
        }
        stack = task.stack;
        if (stack.isOnHomeDisplay()) {
            if (this.mSupervisor.mFocusedStack != stack) {
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_FOCUS, "computeStackFocus: Setting focused stack to r=" + r + " task=" + task);
                }
            } else if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: Focused stack already=" + this.mSupervisor.mFocusedStack);
            }
        }
        return stack;
    }

    private ActivityStack getLaunchStack(ActivityRecord r, int launchFlags, TaskRecord task, ActivityOptions aOptions) {
        if (this.mReuseTask != null) {
            return this.mReuseTask.stack;
        }
        int launchStackId = aOptions != null ? aOptions.getLaunchStackId() : -1;
        if (isValidLaunchStackId(launchStackId, r)) {
            return this.mSupervisor.getStack(launchStackId, true, true);
        }
        if (launchStackId == 3) {
            return this.mSupervisor.getStack(1, true, true);
        }
        if ((launchFlags & DumpState.DUMP_PREFERRED) == 0) {
            return null;
        }
        ActivityStack parentStack;
        if (task != null) {
            parentStack = task.stack;
        } else if (r.mInitialActivityContainer != null) {
            parentStack = r.mInitialActivityContainer.mStack;
        } else {
            parentStack = this.mSupervisor.mFocusedStack;
        }
        if (parentStack != this.mSupervisor.mFocusedStack) {
            return parentStack;
        }
        if (this.mSupervisor.mFocusedStack != null && task == this.mSupervisor.mFocusedStack.topTask()) {
            return this.mSupervisor.mFocusedStack;
        }
        if (parentStack != null && parentStack.mStackId == 3) {
            return this.mSupervisor.getStack(1, true, true);
        }
        ActivityStack dockedStack = this.mSupervisor.getStack(3);
        if (dockedStack == null || dockedStack.getStackVisibilityLocked(r) != 0) {
            return dockedStack;
        }
        return null;
    }

    private boolean isValidLaunchStackId(int stackId, ActivityRecord r) {
        if (stackId == -1 || stackId == 0 || !StackId.isStaticStack(stackId)) {
            return false;
        }
        if (stackId != 1 && (!this.mService.mSupportsMultiWindow || !r.isResizeableOrForced())) {
            return false;
        }
        if (stackId == 3 && r.canGoInDockedStack()) {
            return true;
        }
        if (stackId == 2 && !this.mService.mSupportsFreeformWindowManagement) {
            return false;
        }
        boolean z = this.mService.mSupportsPictureInPicture ? !r.supportsPictureInPicture() ? this.mService.mForceResizableActivities : true : false;
        return stackId != 4 || z;
    }

    Rect getOverrideBounds(ActivityRecord r, ActivityOptions options, TaskRecord inTask) {
        if (options == null) {
            return null;
        }
        if ((r.isResizeable() || (inTask != null && inTask.isResizeable())) && this.mSupervisor.canUseActivityOptionsLaunchBounds(options, options.getLaunchStackId())) {
            return TaskRecord.validateBounds(options.getLaunchBounds());
        }
        return null;
    }

    void setWindowManager(WindowManagerService wm) {
        this.mWindowManager = wm;
    }

    void removePendingActivityLaunchesLocked(ActivityStack stack) {
        for (int palNdx = this.mPendingActivityLaunches.size() - 1; palNdx >= 0; palNdx--) {
            if (((PendingActivityLaunch) this.mPendingActivityLaunches.get(palNdx)).stack == stack) {
                this.mPendingActivityLaunches.remove(palNdx);
            }
        }
    }

    protected boolean standardizeHomeIntent(ResolveInfo rInfo, Intent intent) {
        return false;
    }
}
