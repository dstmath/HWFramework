package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityContainer;
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
import android.graphics.Rect;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Slog;
import android.widget.Toast;
import com.android.internal.app.HeavyWeightSwitcherActivity;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.HwServiceExFactory;
import com.android.server.HwServiceFactory;
import com.android.server.UiThread;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.input.InputManagerService;
import com.android.server.os.HwBootCheck;
import com.android.server.pm.InstantAppResolver;
import com.android.server.wm.WindowManagerService;
import com.huawei.server.am.IHwActivityStarterEx;
import huawei.cust.HwCustUtils;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActivityStarter extends AbsActivityStarter {
    private static final String ACTION_HWCHOOSER = "com.huawei.intent.action.hwCHOOSER";
    private static final String EXTRA_ALWAYS_USE_OPTION = "alwaysUseOption";
    private static final String HWPCEXPLORER_PACKAGE_NAME = "com.huawei.desktop.explorer";
    private static final String INCALLUI_ACTIVITY_CLASS_NAME = "com.android.incallui/.InCallActivity";
    protected static final String SUW_FRP_STATE = "hw_suw_frp_state";
    private static final String TAG = "ActivityManager";
    private static final String TAG_CONFIGURATION = (TAG + ActivityManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_FOCUS = (TAG + ActivityManagerDebugConfig.POSTFIX_FOCUS);
    private static final String TAG_RESULTS = (TAG + ActivityManagerDebugConfig.POSTFIX_RESULTS);
    private static final String TAG_USER_LEAVING = (TAG + ActivityManagerDebugConfig.POSTFIX_USER_LEAVING);
    private boolean mAddingToTask;
    private boolean mAvoidMoveToFront;
    private int mCallingUid;
    String mCurActivityPkName = null;
    private HwCustActivityStackSupervisor mCustAss = ((HwCustActivityStackSupervisor) HwCustUtils.createObj(HwCustActivityStackSupervisor.class, new Object[0]));
    private boolean mDoResume;
    private IHwActivityStarterEx mHwActivityStarterEx;
    private TaskRecord mInTask;
    private Intent mIntent;
    private ActivityStartInterceptor mInterceptor;
    private boolean mKeepCurTransition;
    private final ActivityRecord[] mLastHomeActivityStartRecord = new ActivityRecord[1];
    private int mLastHomeActivityStartResult;
    private final ActivityRecord[] mLastStartActivityRecord = new ActivityRecord[1];
    private int mLastStartActivityResult;
    private long mLastStartActivityTimeMs;
    private String mLastStartReason;
    private Rect mLaunchBounds;
    private int mLaunchFlags;
    private boolean mLaunchSingleInstance;
    private boolean mLaunchSingleTask;
    private boolean mLaunchSingleTop;
    private boolean mLaunchTaskBehind;
    Map<Integer, Boolean> mLauncherStartState = new HashMap();
    private boolean mMovedOtherTask;
    private boolean mMovedToFront;
    private ActivityInfo mNewTaskInfo;
    private Intent mNewTaskIntent;
    private boolean mNoAnimation;
    private ActivityRecord mNotTop;
    protected ActivityOptions mOptions;
    final ArrayList<PendingActivityLaunch> mPendingActivityLaunches = new ArrayList();
    private boolean mPowerHintSent;
    private TaskRecord mReuseTask;
    final ActivityManagerService mService;
    private int mSourceDisplayId;
    private ActivityRecord mSourceRecord;
    private ActivityStack mSourceStack;
    private ActivityRecord mStartActivity;
    private int mStartFlags;
    final ActivityStackSupervisor mSupervisor;
    private ActivityStack mTargetStack;
    private boolean mUsingVr2dDisplay;
    private IVoiceInteractor mVoiceInteractor;
    private IVoiceInteractionSession mVoiceSession;
    private WindowManagerService mWindowManager;

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
        this.mSourceDisplayId = -1;
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
        this.mUsingVr2dDisplay = false;
    }

    ActivityStarter(ActivityManagerService service, ActivityStackSupervisor supervisor) {
        this.mService = service;
        this.mSupervisor = supervisor;
        this.mInterceptor = HwServiceFactory.createActivityStartInterceptor(this.mService, this.mSupervisor);
        this.mCurActivityPkName = "";
        this.mUsingVr2dDisplay = false;
        this.mHwActivityStarterEx = HwServiceExFactory.getHwActivityStarterEx(service);
    }

    int startActivityLocked(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, ActivityContainer container, TaskRecord inTask, String reason) {
        if (TextUtils.isEmpty(reason)) {
            throw new IllegalArgumentException("Need to specify a reason.");
        }
        this.mLastStartReason = reason;
        this.mLastStartActivityTimeMs = System.currentTimeMillis();
        this.mLastStartActivityRecord[0] = null;
        this.mLastStartActivityResult = startActivity(caller, intent, ephemeralIntent, resolvedType, aInfo, rInfo, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, this.mLastStartActivityRecord, container, inTask);
        if (outActivity != null) {
            outActivity[0] = this.mLastStartActivityRecord[0];
        }
        return this.mLastStartActivityResult;
    }

    private int startActivity(IApplicationThread caller, Intent intent, Intent ephemeralIntent, String resolvedType, ActivityInfo aInfo, ResolveInfo rInfo, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int callingPid, int callingUid, String callingPackage, int realCallingPid, int realCallingUid, int startFlags, ActivityOptions options, boolean ignoreTargetSecurity, boolean componentSpecified, ActivityRecord[] outActivity, ActivityContainer container, TaskRecord inTask) {
        int err = 0;
        Bundle verificationBundle = options != null ? options.popAppVerificationBundle() : null;
        ProcessRecord callerApp = null;
        if (caller != null) {
            callerApp = this.mService.getRecordForAppLocked(caller);
            if (callerApp != null) {
                if (intent.hasCategory("android.intent.category.HOME")) {
                    this.mService.checkIfScreenStatusRequestAndSendBroadcast();
                }
            }
        }
        if (aInfo != null) {
            this.mService.noteActivityStart(new AppInfo(aInfo.applicationInfo.packageName, aInfo.processName, intent.getComponent() != null ? intent.getComponent().getClassName() : "NULL", 0, aInfo.applicationInfo.uid), true);
        }
        if (HwFrameworkFactory.getVRSystemServiceManager().isVRMode() && aInfo != null && !HwFrameworkFactory.getVRSystemServiceManager().isVRApplication(this.mService.mContext, aInfo.packageName)) {
            return 0;
        }
        if (caller != null) {
            callerApp = this.mService.getRecordForAppLocked(caller);
            if (callerApp != null) {
                callingPid = callerApp.pid;
                callingUid = callerApp.info.uid;
            } else {
                Slog.w(TAG, "Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting: " + intent.toString());
                err = -94;
            }
        }
        int userId = aInfo != null ? UserHandle.getUserId(aInfo.applicationInfo.uid) : 0;
        if (!(this.mLauncherStartState.containsKey(Integer.valueOf(userId)) && (((Boolean) this.mLauncherStartState.get(Integer.valueOf(userId))).booleanValue() ^ 1) == 0) && this.mService.isStartLauncherActivity(intent, userId)) {
            Slog.w(TAG, "check the USER_SETUP_COMPLETE is set 1 in first start launcher!");
            this.mService.forceValidateHomeButton(userId);
            this.mLauncherStartState.put(Integer.valueOf(userId), Boolean.valueOf(true));
            clearFrpRestricted(this.mService.mContext, userId);
        }
        if (err == 0) {
            String startActivityInfo = "START u" + userId + " {" + intent.toShortString(true, true, true, false) + "} from uid " + callingUid;
            Slog.i(TAG, startActivityInfo);
            HwBootCheck.addBootInfo(startActivityInfo);
            this.mSupervisor.recognitionMaliciousApp(caller, intent);
            ComponentName cmp = intent.getComponent();
            String strPkg = "";
            if (cmp != null) {
                strPkg = cmp.getPackageName();
            }
            if (intent.getCategories() != null && intent.getCategories().toString().contains("android.intent.category.LAUNCHER")) {
                this.mService.getRecordCust().appEnterRecord(strPkg);
            }
            if (ACTION_HWCHOOSER.equals(intent.getAction())) {
                if (intent.getBooleanExtra(EXTRA_ALWAYS_USE_OPTION, false) && (HWPCEXPLORER_PACKAGE_NAME.equals(callingPackage) ^ 1) != 0) {
                    intent.putExtra(EXTRA_ALWAYS_USE_OPTION, false);
                }
            }
            this.mHwActivityStarterEx.effectiveIawareToLaunchApp(intent, aInfo, this.mCurActivityPkName);
        }
        ActivityRecord sourceRecord = null;
        ActivityRecord resultRecord = null;
        if (resultTo != null) {
            sourceRecord = this.mSupervisor.isInAnyStackLocked(resultTo);
            if (ActivityManagerDebugConfig.DEBUG_RESULTS) {
                Slog.v(TAG_RESULTS, "Will send result to " + resultTo + " " + sourceRecord);
            }
            if (!(sourceRecord == null || requestCode < 0 || (sourceRecord.finishing ^ 1) == 0)) {
                resultRecord = sourceRecord;
            }
        }
        int launchFlags = intent.getFlags();
        if (!((33554432 & launchFlags) == 0 || sourceRecord == null)) {
            if (requestCode >= 0) {
                ActivityOptions.abort(options);
                return -93;
            }
            resultRecord = sourceRecord.resultTo;
            if (!(resultRecord == null || (resultRecord.isInStackLocked() ^ 1) == 0)) {
                resultRecord = null;
            }
            resultWho = sourceRecord.resultWho;
            requestCode = sourceRecord.requestCode;
            sourceRecord.resultTo = null;
            if (resultRecord != null) {
                resultRecord.removeResultsLocked(sourceRecord, resultWho, requestCode);
            }
            if (sourceRecord.launchedFromUid == callingUid) {
                callingPackage = sourceRecord.launchedFromPackage;
            }
        }
        if (err == 0 && intent.getComponent() == null) {
            err = -91;
        }
        if (err == 0 && aInfo == null) {
            err = -92;
        }
        if (!(err != 0 || sourceRecord == null || sourceRecord.getTask().voiceSession == null || (268435456 & launchFlags) != 0 || sourceRecord.info.applicationInfo.uid == aInfo.applicationInfo.uid)) {
            try {
                intent.addCategory("android.intent.category.VOICE");
                if (!AppGlobals.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                    Slog.w(TAG, "Activity being started in current voice task does not support voice: " + intent);
                    err = -97;
                }
            } catch (Throwable e) {
                Slog.w(TAG, "Failure checking voice capabilities", e);
                err = -97;
            }
        }
        if (err == 0 && voiceSession != null) {
            try {
                if (!AppGlobals.getPackageManager().activitySupportsIntent(intent.getComponent(), intent, resolvedType)) {
                    Slog.w(TAG, "Activity being started in new voice task does not support: " + intent);
                    err = -97;
                }
            } catch (Throwable e2) {
                Slog.w(TAG, "Failure checking voice capabilities", e2);
                err = -97;
            }
        }
        ActivityStack resultStack = resultRecord == null ? null : resultRecord.getStack();
        if (err != 0) {
            if (resultRecord != null) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
            }
            ActivityOptions.abort(options);
            return err;
        }
        boolean abort = (this.mSupervisor.checkStartAnyActivityPermission(intent, aInfo, resultWho, requestCode, callingPid, callingUid, callingPackage, ignoreTargetSecurity, callerApp, resultRecord, resultStack, options) ^ 1) | (this.mService.mIntentFirewall.checkStartActivity(intent, callingUid, callingPid, resolvedType, aInfo.applicationInfo) ^ 1);
        if (!abort && this.mService.shouldPreventStartActivity(aInfo, callingUid, callingPid, callingPackage, userId)) {
            abort = true;
        }
        if (this.mService.mController != null) {
            try {
                abort |= this.mService.mController.activityStarting(intent.cloneFilter(), aInfo.applicationInfo.packageName) ^ 1;
            } catch (RemoteException e3) {
                this.mService.mController = null;
            }
        }
        abort |= startingCustomActivity(abort, intent, aInfo);
        this.mInterceptor.setStates(userId, realCallingPid, realCallingUid, startFlags, callingPackage);
        this.mInterceptor.setSourceRecord(sourceRecord);
        this.mInterceptor.intercept(intent, rInfo, aInfo, resolvedType, inTask, callingPid, callingUid, options);
        intent = this.mInterceptor.mIntent;
        rInfo = this.mInterceptor.mRInfo;
        aInfo = this.mInterceptor.mAInfo;
        resolvedType = this.mInterceptor.mResolvedType;
        inTask = this.mInterceptor.mInTask;
        callingPid = this.mInterceptor.mCallingPid;
        callingUid = this.mInterceptor.mCallingUid;
        options = this.mInterceptor.mActivityOptions;
        if (abort) {
            if (!(resultRecord == null || resultStack == null)) {
                resultStack.sendActivityResultLocked(-1, resultRecord, resultWho, requestCode, 0, null);
            }
            ActivityOptions.abort(options);
            return 0;
        }
        if (this.mService.mPermissionReviewRequired && aInfo != null && this.mService.getPackageManagerInternalLocked().isPermissionsReviewRequired(aInfo.packageName, userId)) {
            IIntentSender target = this.mService.getIntentSenderLocked(2, callingPackage, callingUid, userId, null, null, 0, new Intent[]{intent}, new String[]{resolvedType}, 1342177280, null);
            int flags = intent.getFlags();
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
                String str = TAG;
                StringBuilder append = new StringBuilder().append("START u").append(userId).append(" {").append(intent2.toShortString(true, true, true, false)).append("} from uid ").append(realCallingUid).append(" on display ");
                int i = container == null ? this.mSupervisor.mFocusedStack == null ? 0 : this.mSupervisor.mFocusedStack.mDisplayId : container.mActivityDisplay == null ? 0 : container.mActivityDisplay.mDisplayId;
                Slog.i(str, append.append(i).toString());
            }
        }
        if (!(rInfo == null || rInfo.auxiliaryInfo == null)) {
            intent = createLaunchIntent(rInfo.auxiliaryInfo, ephemeralIntent, callingPackage, verificationBundle, resolvedType, userId);
            resolvedType = null;
            callingUid = realCallingUid;
            callingPid = realCallingPid;
            aInfo = this.mSupervisor.resolveActivity(intent, rInfo, startFlags, null);
        }
        Slog.i(TAG, "ActivityRecord info: " + aInfo);
        ActivityRecord r = HwServiceFactory.createActivityRecord(this.mService, callerApp, callingPid, callingUid, callingPackage, intent, resolvedType, aInfo, this.mService.getGlobalConfiguration(), resultRecord, resultWho, requestCode, componentSpecified, voiceSession != null, this.mSupervisor, container, options, sourceRecord);
        if (this.mService.shouldPreventActivity(intent, aInfo, r, callingPid, callingUid, callerApp)) {
            Slog.w(TAG, "forbiden launch for activity: " + r);
            ActivityOptions.abort(options);
            return 100;
        }
        if (outActivity != null) {
            outActivity[0] = r;
        }
        if (r.appTimeTracker == null && sourceRecord != null) {
            r.appTimeTracker = sourceRecord.appTimeTracker;
        }
        ActivityStack stack = this.mSupervisor.mFocusedStack;
        if (voiceSession == null && stack.mResumedActivity == null && this.mSupervisor.getResumedActivityLocked() != null && r.shortComponentName != null && (r.shortComponentName.equals(this.mSupervisor.getResumedActivityLocked().shortComponentName) ^ 1) != 0 && INCALLUI_ACTIVITY_CLASS_NAME.equals(this.mSupervisor.getResumedActivityLocked().shortComponentName) && this.mService.isStartLauncherActivity(intent, userId) && ((stack.mLastPausedActivity == null || (INCALLUI_ACTIVITY_CLASS_NAME.equals(stack.mLastPausedActivity.shortComponentName) ^ 1) != 0) && (this.mService.isSleepingLocked() ^ 1) != 0)) {
            Slog.d(TAG, "abort launch for activity: " + r);
            ActivityOptions.abort(options);
            return 100;
        } else if (this.mService.isSleepingLocked() && r.shortComponentName != null && "com.tencent.news/.push.alive.offactivity.OffActivity".equals(r.shortComponentName)) {
            Slog.i(TAG, "abort launch for activity: " + r);
            ActivityOptions.abort(options);
            return 100;
        } else {
            if (voiceSession == null && (stack.mResumedActivity == null || stack.mResumedActivity.info.applicationInfo.uid != callingUid)) {
                if (!this.mService.checkAppSwitchAllowedLocked(callingPid, callingUid, realCallingPid, realCallingUid, "Activity start")) {
                    this.mPendingActivityLaunches.add(new PendingActivityLaunch(r, sourceRecord, startFlags, stack, callerApp));
                    ActivityOptions.abort(options);
                    return 100;
                }
            }
            if (this.mService.mDidAppSwitch) {
                this.mService.mAppSwitchesAllowedTime = 0;
            } else {
                this.mService.mDidAppSwitch = true;
            }
            doPendingActivityLaunchesLocked(false);
            err = startActivity(r, sourceRecord, voiceSession, voiceInteractor, startFlags, true, options, inTask, outActivity);
            if (Jlog.isUBMEnable() && err >= 0 && intent.getComponent() != null) {
                Jlog.d(InputManagerService.BTN_MOUSE, "AL#" + intent.getComponent().flattenToShortString() + "(" + intent.getAction() + "," + intent.getCategories() + ")");
            }
            return err;
        }
    }

    private Intent createLaunchIntent(AuxiliaryResolveInfo auxiliaryResponse, Intent originalIntent, String callingPackage, Bundle verificationBundle, String resolvedType, int userId) {
        if (auxiliaryResponse.needsPhaseTwo) {
            this.mService.getPackageManagerInternalLocked().requestInstantAppResolutionPhaseTwo(auxiliaryResponse, originalIntent, resolvedType, callingPackage, verificationBundle, userId);
        }
        return InstantAppResolver.buildEphemeralInstallerIntent("android.intent.action.INSTALL_INSTANT_APP_PACKAGE", originalIntent, auxiliaryResponse.failureIntent, callingPackage, verificationBundle, resolvedType, userId, auxiliaryResponse.packageName, auxiliaryResponse.splitName, auxiliaryResponse.versionCode, auxiliaryResponse.token, auxiliaryResponse.needsPhaseTwo);
    }

    void postStartActivityProcessing(ActivityRecord r, int result, int prevFocusedStackId, ActivityRecord sourceRecord, ActivityStack targetStack) {
        if (!ActivityManager.isStartResultFatalError(result)) {
            if (result == 2 && (this.mSupervisor.mWaitingActivityLaunched.isEmpty() ^ 1) != 0) {
                this.mSupervisor.reportTaskToFrontNoLaunch(this.mStartActivity);
            }
            int startedActivityStackId = -1;
            ActivityStack currentStack = r.getStack();
            if (currentStack != null) {
                startedActivityStackId = currentStack.mStackId;
            } else if (this.mTargetStack != null) {
                startedActivityStackId = targetStack.mStackId;
            }
            if (startedActivityStackId == 3) {
                ActivityStack homeStack = this.mSupervisor.getStack(0);
                if (homeStack != null ? homeStack.isVisible() : false) {
                    Slog.d(TAG, "Scheduling recents launch.");
                    this.mWindowManager.showRecentApps(true);
                }
                return;
            }
            boolean clearedTask = (this.mLaunchFlags & 268468224) == 268468224 ? this.mReuseTask != null : false;
            if (startedActivityStackId == 4 && (result == 2 || result == 3 || clearedTask)) {
                this.mService.mTaskChangeNotificationController.notifyPinnedActivityRestartAttempt(clearedTask);
            }
        }
    }

    void startHomeActivityLocked(Intent intent, ActivityInfo aInfo, String reason) {
        this.mSupervisor.moveHomeStackTaskToTop(reason);
        this.mLastHomeActivityStartResult = startActivityLocked(null, intent, null, null, aInfo, null, null, null, null, null, 0, 0, 0, null, 0, 0, 0, null, false, false, this.mLastHomeActivityStartRecord, null, null, "startHomeActivity: " + reason);
        if (this.mSupervisor.inResumeTopActivity) {
            this.mSupervisor.scheduleResumeTopActivities();
        }
    }

    void startConfirmCredentialIntent(Intent intent, Bundle optionsBundle) {
        ActivityOptions options;
        intent.addFlags(276840448);
        if (optionsBundle != null) {
            options = new ActivityOptions(optionsBundle);
        } else {
            options = ActivityOptions.makeBasic();
        }
        options.setLaunchTaskId(this.mSupervisor.getHomeActivity().getTask().taskId);
        this.mService.mContext.startActivityAsUser(intent, options.toBundle(), UserHandle.CURRENT);
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x0145  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x017f  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x01e5 A:{SYNTHETIC, Splitter: B:69:0x01e5} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int startActivityMayWait(IApplicationThread caller, int callingUid, String callingPackage, Intent intent, String resolvedType, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, IBinder resultTo, String resultWho, int requestCode, int startFlags, ProfilerInfo profilerInfo, WaitResult outResult, Configuration globalConfig, Bundle bOptions, boolean ignoreTargetSecurity, int userId, IActivityContainer iContainer, TaskRecord inTask, String reason) {
        Throwable th;
        if (intent == null || !intent.hasFileDescriptors()) {
            Set<String> categories;
            ResolveInfo rInfo;
            ActivityInfo aInfo;
            ActivityOptions options;
            ActivityContainer container;
            this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunching();
            boolean componentSpecified = intent.getComponent() != null;
            Intent intent2 = new Intent(intent);
            intent2 = new Intent(intent);
            if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                categories = intent2.getCategories();
                if (categories != null) {
                    if (categories.contains("android.intent.category.HOME")) {
                        intent2.removeCategory("android.intent.category.HOME");
                        intent2.addFlags(DumpState.DUMP_CHANGES);
                        intent2.setClassName("com.huawei.android.launcher", "com.huawei.android.launcher.powersavemode.PowerSaveModeLauncher");
                        if (this.mCustAss != null) {
                            this.mCustAss.modifyIntentForLauncher3(intent2);
                        }
                    }
                }
            }
            if (SystemProperties.getBoolean("sys.ride_mode", false)) {
                categories = intent2.getCategories();
                if (categories != null) {
                    if (categories.contains("android.intent.category.HOME")) {
                        intent2.removeCategory("android.intent.category.HOME");
                        intent2.addFlags(DumpState.DUMP_CHANGES);
                        intent2.setClassName("com.huawei.android.launcher", "com.huawei.android.launcher.streetmode.StreetModeLauncher");
                    }
                }
            }
            if (componentSpecified && intent2.getData() != null && "android.intent.action.VIEW".equals(intent2.getAction()) && this.mService.getPackageManagerInternalLocked().isInstantAppInstallerComponent(intent2.getComponent())) {
                intent2.setComponent(null);
                componentSpecified = false;
            }
            ResolveInfo rInfo2 = this.mSupervisor.resolveIntent(intent2, resolvedType, userId);
            if (standardizeHomeIntent(rInfo2, intent2)) {
                componentSpecified = false;
            }
            if (rInfo2 == null) {
                UserInfo userInfo = this.mSupervisor.getUserInfo(userId);
                if (userInfo == null) {
                    rInfo = rInfo2;
                } else if (userInfo.isManagedProfile() || userInfo.isClonedProfile()) {
                    UserManager userManager = UserManager.get(this.mService.mContext);
                    long token = Binder.clearCallingIdentity();
                    try {
                        boolean profileLockedAndParentUnlockingOrUnlocked;
                        UserInfo parent = userManager.getProfileParent(userId);
                        if (parent != null) {
                            if (userManager.isUserUnlockingOrUnlocked(parent.id)) {
                                profileLockedAndParentUnlockingOrUnlocked = userManager.isUserUnlockingOrUnlocked(userId) ^ 1;
                                Binder.restoreCallingIdentity(token);
                                if (profileLockedAndParentUnlockingOrUnlocked) {
                                    rInfo = this.mSupervisor.resolveIntent(intent2, resolvedType, userId, 786432);
                                }
                            }
                        }
                        profileLockedAndParentUnlockingOrUnlocked = false;
                        Binder.restoreCallingIdentity(token);
                        if (profileLockedAndParentUnlockingOrUnlocked) {
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
                        if (container == null || container.mParentActivity == null || container.mParentActivity.state == ActivityState.RESUMED) {
                            int callingPid;
                            ActivityStack stack;
                            ActivityInfo aInfo2;
                            int realCallingPid = Binder.getCallingPid();
                            int realCallingUid = Binder.getCallingUid();
                            if (callingUid >= 0) {
                                callingPid = -1;
                            } else if (caller == null) {
                                callingPid = realCallingPid;
                                callingUid = realCallingUid;
                            } else {
                                callingUid = -1;
                                callingPid = -1;
                            }
                            if (container == null || container.mStack.isOnHomeDisplay()) {
                                stack = this.mSupervisor.mFocusedStack;
                            } else {
                                stack = container.mStack;
                            }
                            boolean z = globalConfig != null ? this.mService.getGlobalConfiguration().diff(globalConfig) != 0 : false;
                            stack.mConfigWillChange = z;
                            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.v(TAG_CONFIGURATION, "Starting activity when config will change = " + stack.mConfigWillChange);
                            }
                            long origId = Binder.clearCallingIdentity();
                            if (aInfo == null || (aInfo.applicationInfo.privateFlags & 2) == 0) {
                                aInfo2 = aInfo;
                                rInfo2 = rInfo;
                                intent = intent2;
                            } else {
                                if (aInfo.processName.equals(aInfo.applicationInfo.packageName)) {
                                    ProcessRecord heavy = this.mService.mHeavyWeightProcess;
                                    if (heavy == null) {
                                        aInfo2 = aInfo;
                                        rInfo2 = rInfo;
                                        intent = intent2;
                                    } else if (!(heavy.info.uid == aInfo.applicationInfo.uid && (heavy.processName.equals(aInfo.processName) ^ 1) == 0)) {
                                        int appCallingUid = callingUid;
                                        if (caller != null) {
                                            ProcessRecord callerApp = this.mService.getRecordForAppLocked(caller);
                                            if (callerApp != null) {
                                                appCallingUid = callerApp.info.uid;
                                            } else {
                                                Slog.w(TAG, "Unable to find app for caller " + caller + " (pid=" + callingPid + ") when starting: " + intent2.toString());
                                                ActivityOptions.abort(options);
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                return -94;
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
                                            newIntent.putExtra("cur_task", hist.getTask().taskId);
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
                                            componentSpecified = true;
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
                                        }
                                    }
                                }
                                aInfo2 = aInfo;
                                rInfo2 = rInfo;
                                intent = intent2;
                            }
                            this.mService.addCallerToIntent(intent, caller);
                            if (HwDeviceManager.disallowOp(intent)) {
                                Slog.i(TAG, "due to disallow op launching activity aborted");
                                UiThread.getHandler().post(new Runnable() {
                                    public void run() {
                                        Context context = ActivityStarter.this.mService.mUiContext;
                                        if (context != null) {
                                            Toast.makeText(context, context.getString(33685969), 0).show();
                                        }
                                    }
                                });
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                return -96;
                            }
                            ActivityRecord[] outRecord = new ActivityRecord[1];
                            int res = startActivityLocked(caller, intent, intent2, resolvedType, aInfo2, rInfo2, voiceSession, voiceInteractor, resultTo, resultWho, requestCode, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, startFlags, options, ignoreTargetSecurity, componentSpecified, outRecord, container, inTask, reason);
                            Binder.restoreCallingIdentity(origId);
                            if (stack.mConfigWillChange) {
                                this.mService.enforceCallingPermission("android.permission.CHANGE_CONFIGURATION", "updateConfiguration()");
                                stack.mConfigWillChange = false;
                                if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                    Slog.v(TAG_CONFIGURATION, "Updating to new configuration after starting activity.");
                                }
                                this.mService.updateConfigurationLocked(globalConfig, null, false);
                            }
                            if (outResult != null) {
                                outResult.result = res;
                                outResult.origin = intent.getComponent();
                                if (res == 0) {
                                    this.mSupervisor.mWaitingActivityLaunched.add(outResult);
                                    Slog.i(TAG, "mWaitingActivityLaunched add ComponentName = " + outResult.who);
                                    do {
                                        try {
                                            this.mService.wait();
                                        } catch (InterruptedException e) {
                                        }
                                        try {
                                            if (outResult.result == 2 || (outResult.timeout ^ 1) == 0) {
                                                Slog.i(TAG, "wait Launched end, result = " + outResult.result + " who = " + outResult.who);
                                            }
                                        } catch (Throwable th5) {
                                            th = th5;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    } while (outResult.who == null);
                                    Slog.i(TAG, "wait Launched end, result = " + outResult.result + " who = " + outResult.who);
                                    if (outResult.result == 2) {
                                        res = 2;
                                    }
                                }
                                if (res == 2) {
                                    ActivityRecord r = outRecord[0];
                                    if (r.nowVisible && r.state == ActivityState.RESUMED) {
                                        outResult.timeout = false;
                                        outResult.who = r.realActivity;
                                        outResult.totalTime = 0;
                                        outResult.thisTime = 0;
                                    } else {
                                        outResult.thisTime = SystemClock.uptimeMillis();
                                        this.mSupervisor.waitActivityVisible(r.realActivity, outResult);
                                        Slog.i(TAG, "waitActivityVisible add realActivity = " + r.realActivity + " who =" + outResult.who);
                                        do {
                                            try {
                                                this.mService.wait();
                                            } catch (InterruptedException e2) {
                                            }
                                            if (outResult.timeout) {
                                                break;
                                            }
                                        } while (outResult.who == null);
                                        Slog.i(TAG, "wait Visible end, who = " + outResult.who);
                                    }
                                }
                            }
                            this.mSupervisor.mActivityMetricsLogger.notifyActivityLaunched(res, outRecord[0]);
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            return res;
                        }
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        return -96;
                    } catch (Throwable th6) {
                        th = th6;
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
            }
            Jlog.i(2023, "whopkg=" + callingPackage + "&pkg=" + aInfo.applicationInfo.packageName + "&cls=" + aInfo.name);
            options = ActivityOptions.fromBundle(bOptions);
            container = (ActivityContainer) iContainer;
            synchronized (this.mService) {
            }
        } else {
            throw new IllegalArgumentException("File descriptors passed in Intent");
        }
    }

    final int startActivities(IApplicationThread caller, int callingUid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, Bundle bOptions, int userId, String reason) {
        if (intents == null) {
            throw new NullPointerException("intents is null");
        } else if (resolvedTypes == null) {
            throw new NullPointerException("resolvedTypes is null");
        } else if (intents.length != resolvedTypes.length) {
            throw new IllegalArgumentException("intents are length different than resolvedTypes");
        } else {
            int callingPid;
            int realCallingPid = Binder.getCallingPid();
            int realCallingUid = Binder.getCallingUid();
            if (callingUid >= 0) {
                callingPid = -1;
            } else if (caller == null) {
                callingPid = realCallingPid;
                callingUid = realCallingUid;
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
                        } else if (intent == null || !intent.hasFileDescriptors()) {
                            boolean componentSpecified = intent.getComponent() != null;
                            Intent intent2 = new Intent(intent);
                            this.mService.addCallerToIntent(intent2, caller);
                            ActivityInfo aInfo = this.mService.getActivityInfoForUser(this.mSupervisor.resolveActivity(intent2, resolvedTypes[i], 0, null, userId), userId);
                            if (aInfo == null || (aInfo.applicationInfo.privateFlags & 2) == 0) {
                                int res = startActivityLocked(caller, intent2, null, resolvedTypes[i], aInfo, null, null, null, resultTo, null, -1, callingPid, callingUid, callingPackage, realCallingPid, realCallingUid, 0, ActivityOptions.fromBundle(i == intents.length + -1 ? bOptions : null), false, componentSpecified, outActivity, null, null, reason);
                                if (res < 0) {
                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                    Binder.restoreCallingIdentity(origId);
                                    return res;
                                } else if (outActivity[0] != null) {
                                    Object resultTo2 = outActivity[0].appToken;
                                } else {
                                    resultTo2 = null;
                                }
                            } else {
                                throw new IllegalArgumentException("FLAG_CANT_SAVE_STATE not supported here");
                            }
                        } else {
                            throw new IllegalArgumentException("File descriptors passed in Intent");
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

    void sendPowerHintForLaunchStartIfNeeded(boolean forceSend) {
        boolean sendHint = forceSend;
        if (!forceSend) {
            ActivityRecord resumedActivity = this.mSupervisor.getResumedActivityLocked();
            if (resumedActivity == null || resumedActivity.app == null) {
                sendHint = true;
            } else {
                sendHint = resumedActivity.app.equals(this.mStartActivity.app) ^ 1;
            }
        }
        if (sendHint && this.mService.mLocalPowerManager != null) {
            this.mService.mLocalPowerManager.powerHint(8, 1);
            this.mPowerHintSent = true;
        }
    }

    void sendPowerHintForLaunchEndIfNeeded() {
        if (this.mPowerHintSent && this.mService.mLocalPowerManager != null) {
            this.mService.mLocalPowerManager.powerHint(8, 0);
            this.mPowerHintSent = false;
        }
    }

    private int startActivity(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity) {
        int result = -96;
        try {
            this.mService.mWindowManager.deferSurfaceLayout();
            result = startActivityUnchecked(r, sourceRecord, voiceSession, voiceInteractor, startFlags, doResume, options, inTask, outActivity);
            if (!(ActivityManager.isStartResultSuccessful(result) || this.mStartActivity.getTask() == null)) {
                this.mStartActivity.getTask().removeActivity(this.mStartActivity);
            }
            this.mService.mWindowManager.continueSurfaceLayout();
            postStartActivityProcessing(r, result, this.mSupervisor.getLastStack().mStackId, this.mSourceRecord, this.mTargetStack);
            return result;
        } catch (Throwable th) {
            if (!(ActivityManager.isStartResultSuccessful(result) || this.mStartActivity.getTask() == null)) {
                this.mStartActivity.getTask().removeActivity(this.mStartActivity);
            }
            this.mService.mWindowManager.continueSurfaceLayout();
        }
    }

    private int startActivityUnchecked(ActivityRecord r, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor, int startFlags, boolean doResume, ActivityOptions options, TaskRecord inTask, ActivityRecord[] outActivity) {
        ActivityStack sourceStack;
        ActivityRecord top;
        setInitialState(r, options, inTask, doResume, startFlags, sourceRecord, voiceSession, voiceInteractor);
        computeLaunchingTaskFlags();
        computeSourceStack();
        this.mIntent.setFlags(this.mLaunchFlags);
        ActivityRecord reusedActivity = getReusableIntentActivity();
        if (HwPCUtils.isPcCastModeInServer()) {
            if (hasStartedOnOtherDisplay(this.mStartActivity, this.mSourceDisplayId)) {
                ActivityOptions.abort(this.mOptions);
                sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getStack() : null;
                if (sourceStack != null) {
                    sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
                }
                return 0;
            }
            if (killProcessOnOtherDisplay(this.mStartActivity, this.mSourceDisplayId)) {
                reusedActivity = null;
            }
            if (HwPCUtils.enabledInPad() && reusedActivity != null && (("com.android.systemui/.settings.BrightnessDialog".equals(reusedActivity.shortComponentName) || "com.android.incallui".equals(reusedActivity.packageName) || "com.huawei.android.wfdft".equals(reusedActivity.packageName)) && (HwPCUtils.isValidExtDisplayId(reusedActivity.getDisplayId()) ^ 1) != 0)) {
                Slog.i(TAG, "startActivityUnchecked reusedActivity :" + reusedActivity);
                reusedActivity = null;
            }
        } else {
            if (killProcessOnDefaultDisplay(this.mStartActivity)) {
                reusedActivity = null;
            }
        }
        int preferredLaunchStackId = this.mOptions != null ? this.mOptions.getLaunchStackId() : -1;
        int preferredLaunchDisplayId = this.mOptions != null ? this.mOptions.getLaunchDisplayId() : 0;
        if (!(reusedActivity == null || reusedActivity.getTask() == null)) {
            if (this.mSupervisor.isLockTaskModeViolation(reusedActivity.getTask(), (this.mLaunchFlags & 268468224) == 268468224)) {
                this.mSupervisor.showLockTaskToast();
                Slog.e(TAG, "startActivityUnchecked: Attempt to violate Lock Task Mode");
                return 101;
            }
            if (this.mStartActivity.getTask() == null) {
                this.mStartActivity.setTask(reusedActivity.getTask());
            }
            if (reusedActivity.getTask().intent == null) {
                reusedActivity.getTask().setIntent(this.mStartActivity);
            }
            if ((this.mLaunchFlags & 67108864) != 0 || isDocumentLaunchesIntoExisting(this.mLaunchFlags) || this.mLaunchSingleInstance || this.mLaunchSingleTask) {
                TaskRecord task = reusedActivity.getTask();
                top = task.performClearTaskForReuseLocked(this.mStartActivity, this.mLaunchFlags);
                if (reusedActivity.getTask() == null) {
                    reusedActivity.setTask(task);
                }
                if (top != null) {
                    if (top.frontOfTask) {
                        top.getTask().setIntent(this.mStartActivity);
                    }
                    ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, top.getTask());
                    top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
                }
            }
            sendPowerHintForLaunchStartIfNeeded(false);
            ActivityRecord activityRecord = setTargetStackAndMoveToFrontIfNeeded(reusedActivity);
            if (activityRecord != null) {
                reusedActivity = activityRecord;
            }
            ActivityRecord outResult = (outActivity == null || outActivity.length <= 0) ? null : outActivity[0];
            if (outResult != null && (outResult.finishing || outResult.noDisplay)) {
                outActivity[0] = reusedActivity;
            }
            if ((this.mStartFlags & 1) != 0) {
                resumeTargetStackIfNeeded();
                return 1;
            }
            setTaskFromIntentActivity(reusedActivity);
            if (!this.mAddingToTask && this.mReuseTask == null) {
                resumeTargetStackIfNeeded();
                if (outActivity != null && outActivity.length > 0) {
                    outActivity[0] = reusedActivity;
                }
                return 2;
            }
        }
        if (this.mStartActivity.packageName == null) {
            sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getStack() : null;
            if (sourceStack != null) {
                sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            }
            ActivityOptions.abort(this.mOptions);
            return -92;
        }
        boolean dontStart;
        ActivityStack topStack = this.mSupervisor.mFocusedStack;
        ActivityRecord topFocused = topStack.topActivity();
        top = topStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        if (top == null || this.mStartActivity.resultTo != null || !top.realActivity.equals(this.mStartActivity.realActivity) || top.userId != this.mStartActivity.userId || top.app == null || top.app.thread == null) {
            dontStart = false;
        } else if ((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop) {
            dontStart = true;
        } else {
            dontStart = this.mLaunchSingleTask;
        }
        if (dontStart) {
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, top, top.getTask());
            topStack.mLastPausedActivity = null;
            if (this.mDoResume) {
                this.mSupervisor.resumeFocusedStackTopActivityLocked();
            }
            ActivityOptions.abort(this.mOptions);
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            this.mSupervisor.handleNonResizableTaskIfNeeded(top.getTask(), preferredLaunchStackId, preferredLaunchDisplayId, topStack.mStackId);
            return 3;
        }
        boolean newTask = false;
        TaskRecord taskToAffiliate = (!this.mLaunchTaskBehind || this.mSourceRecord == null) ? null : this.mSourceRecord.getTask();
        int result = 0;
        if (this.mStartActivity.resultTo == null && this.mInTask == null && (this.mAddingToTask ^ 1) != 0 && (this.mLaunchFlags & 268435456) != 0) {
            newTask = true;
            result = setTaskFromReuseOrCreateNewTask(taskToAffiliate, preferredLaunchStackId, topStack);
        } else if (this.mSourceRecord != null) {
            result = setTaskFromSourceRecord();
        } else if (this.mInTask != null) {
            result = setTaskFromInTask();
        } else {
            setTaskToCurrentTopOrCreateNewTask();
        }
        if (result != 0) {
            return result;
        }
        if (this.mCallingUid == 1000 && this.mIntent.getCallingUid() != 0) {
            this.mCallingUid = this.mIntent.getCallingUid();
        }
        this.mService.grantUriPermissionFromIntentLocked(this.mCallingUid, this.mStartActivity.packageName, this.mIntent, this.mStartActivity.getUriPermissionsLocked(), this.mStartActivity.userId);
        this.mService.grantEphemeralAccessLocked(this.mStartActivity.userId, this.mIntent, this.mStartActivity.appInfo.uid, UserHandle.getAppId(this.mCallingUid));
        if (this.mSourceRecord != null) {
            this.mStartActivity.getTask().setTaskToReturnTo(this.mSourceRecord);
        }
        if (newTask) {
            EventLog.writeEvent(EventLogTags.AM_CREATE_TASK, new Object[]{Integer.valueOf(this.mStartActivity.userId), Integer.valueOf(this.mStartActivity.getTask().taskId)});
        }
        ActivityStack.logStartActivity(EventLogTags.AM_CREATE_ACTIVITY, this.mStartActivity, this.mStartActivity.getTask());
        this.mTargetStack.mLastPausedActivity = null;
        sendPowerHintForLaunchStartIfNeeded(false);
        this.mTargetStack.startActivityLocked(this.mStartActivity, topFocused, newTask, this.mKeepCurTransition, this.mOptions);
        if (this.mDoResume) {
            ActivityRecord topTaskActivity = this.mStartActivity.getTask().topRunningActivityLocked();
            if (this.mTargetStack.isFocusable() && (topTaskActivity == null || !topTaskActivity.mTaskOverlay || this.mStartActivity == topTaskActivity)) {
                if (this.mTargetStack.isFocusable() && (this.mSupervisor.isFocusedStack(this.mTargetStack) ^ 1) != 0) {
                    this.mTargetStack.moveToFront("startActivityUnchecked");
                }
                this.mSupervisor.resumeFocusedStackTopActivityLocked(this.mTargetStack, this.mStartActivity, this.mOptions);
            } else {
                this.mTargetStack.ensureActivitiesVisibleLocked(null, 0, false);
                this.mWindowManager.executeAppTransition();
            }
        } else {
            this.mTargetStack.addRecentActivityLocked(this.mStartActivity);
        }
        this.mSupervisor.updateUserStackLocked(this.mStartActivity.userId, this.mTargetStack);
        this.mSupervisor.handleNonResizableTaskIfNeeded(this.mStartActivity.getTask(), preferredLaunchStackId, preferredLaunchDisplayId, this.mTargetStack.mStackId);
        return 0;
    }

    protected boolean hasStartedOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        return false;
    }

    protected boolean killProcessOnOtherDisplay(ActivityRecord startActivity, int sourceDisplayId) {
        return false;
    }

    protected boolean killProcessOnDefaultDisplay(ActivityRecord startActivity) {
        return false;
    }

    protected void setInitialState(ActivityRecord r, ActivityOptions options, TaskRecord inTask, boolean doResume, int startFlags, ActivityRecord sourceRecord, IVoiceInteractionSession voiceSession, IVoiceInteractor voiceInteractor) {
        reset();
        this.mStartActivity = r;
        this.mIntent = r.intent;
        this.mOptions = options;
        this.mCallingUid = r.launchedFromUid;
        this.mSourceRecord = sourceRecord;
        this.mVoiceSession = voiceSession;
        this.mVoiceInteractor = voiceInteractor;
        this.mSourceDisplayId = getSourceDisplayId(this.mSourceRecord, this.mStartActivity);
        this.mLaunchBounds = getOverrideBounds(r, options, inTask);
        this.mLaunchSingleTop = r.launchMode == 1;
        this.mLaunchSingleInstance = r.launchMode == 3;
        this.mLaunchSingleTask = r.launchMode == 2;
        this.mLaunchFlags = adjustLaunchFlagsToDocumentMode(r, this.mLaunchSingleInstance, this.mLaunchSingleTask, this.mIntent.getFlags());
        boolean z = (!r.mLaunchTaskBehind || (this.mLaunchSingleTask ^ 1) == 0 || (this.mLaunchSingleInstance ^ 1) == 0) ? false : (this.mLaunchFlags & DumpState.DUMP_FROZEN) != 0;
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
        if (!(doResume && (r.okToShowLocked() ^ 1) == 0)) {
            r.delayedResume = true;
            this.mDoResume = false;
        }
        if (!(this.mOptions == null || this.mOptions.getLaunchTaskId() == -1 || !this.mOptions.getTaskOverlay())) {
            r.mTaskOverlay = true;
            if (!this.mOptions.canTaskOverlayResume()) {
                TaskRecord task = this.mSupervisor.anyTaskForIdLocked(this.mOptions.getLaunchTaskId());
                ActivityRecord top = task != null ? task.getTopActivity() : null;
                if (!(top == null || top.state == ActivityState.RESUMED)) {
                    this.mDoResume = false;
                    this.mAvoidMoveToFront = true;
                }
            }
        }
        this.mNotTop = (this.mLaunchFlags & 16777216) != 0 ? r : null;
        this.mInTask = inTask;
        if (!(inTask == null || (inTask.inRecents ^ 1) == 0)) {
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
        this.mNoAnimation = (this.mLaunchFlags & 65536) != 0;
    }

    private void sendNewTaskResultRequestIfNeeded() {
        ActivityStack sourceStack = this.mStartActivity.resultTo != null ? this.mStartActivity.resultTo.getStack() : null;
        if (!(sourceStack == null || (this.mLaunchFlags & 268435456) == 0)) {
            if (isInSkipCancelResultList(this.mStartActivity.shortComponentName)) {
                Slog.w(TAG, "we skip cancelling activity result from activity " + this.mStartActivity.shortComponentName);
                return;
            }
            Slog.w(TAG, "Activity is launching as a new task, so cancelling activity result.");
            sourceStack.sendActivityResultLocked(-1, this.mStartActivity.resultTo, this.mStartActivity.resultWho, this.mStartActivity.requestCode, 0, null);
            this.mStartActivity.resultTo = null;
        }
    }

    private void computeLaunchingTaskFlags() {
        if (this.mSourceRecord != null || this.mInTask == null || this.mInTask.getStack() == null) {
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
                Intent intent;
                Slog.w(TAG, "startActivity called from finishing " + this.mSourceRecord + "; forcing " + "Intent.FLAG_ACTIVITY_NEW_TASK for: " + this.mIntent);
                this.mLaunchFlags |= 268435456;
                this.mNewTaskInfo = this.mSourceRecord.info;
                TaskRecord sourceTask = this.mSourceRecord.getTask();
                if (sourceTask != null) {
                    intent = sourceTask.intent;
                } else {
                    intent = null;
                }
                this.mNewTaskIntent = intent;
            }
            this.mSourceRecord = null;
            this.mSourceStack = null;
        } else {
            this.mSourceStack = this.mSourceRecord.getStack();
        }
    }

    private ActivityRecord getReusableIntentActivity() {
        boolean putIntoExistingTask;
        int i;
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
            if ((this.mLaunchFlags & 4096) != 0) {
                return this.mSupervisor.findActivityLocked(this.mIntent, this.mStartActivity.info, this.mLaunchSingleTask ^ 1);
            }
            return this.mSupervisor.findTaskLocked(this.mStartActivity, this.mSourceDisplayId);
        }
    }

    protected int getSourceDisplayId(ActivityRecord sourceRecord, ActivityRecord startingActivity) {
        if (startingActivity != null && startingActivity.requestedVrComponent != null) {
            return 0;
        }
        int displayId = this.mService.mVr2dDisplayId;
        if (displayId != -1) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG, "getSourceDisplayId :" + displayId);
            }
            this.mUsingVr2dDisplay = true;
            return displayId;
        }
        displayId = sourceRecord != null ? sourceRecord.getDisplayId() : -1;
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer() && ("com.android.incallui".equals(startingActivity.packageName) || "com.android.systemui/.settings.BrightnessDialog".equals(startingActivity.shortComponentName))) {
            Slog.d(TAG, "getSourceDisplayId set displayId :" + HwPCUtils.getPCDisplayID() + ", packageName :" + startingActivity.packageName + ", shortComponentName :" + startingActivity.shortComponentName);
            displayId = HwPCUtils.getPCDisplayID();
        }
        if (displayId != -1) {
            return displayId;
        }
        return 0;
    }

    private ActivityRecord setTargetStackAndMoveToFrontIfNeeded(ActivityRecord intentActivity) {
        this.mTargetStack = intentActivity.getStack();
        this.mTargetStack.mLastPausedActivity = null;
        ActivityStack focusStack = this.mSupervisor.getFocusedStack();
        ActivityRecord curTop = focusStack == null ? null : focusStack.topRunningNonDelayedActivityLocked(this.mNotTop);
        TaskRecord topTask = curTop != null ? curTop.getTask() : null;
        if (!(topTask == null || ((topTask == intentActivity.getTask() && topTask == focusStack.topTask()) || (this.mAvoidMoveToFront ^ 1) == 0))) {
            this.mStartActivity.intent.addFlags(DumpState.DUMP_CHANGES);
            if (this.mSourceRecord == null || (this.mSourceStack.topActivity() != null && this.mSourceStack.topActivity().getTask() == this.mSourceRecord.getTask())) {
                if (intentActivity.getTask().getTaskToReturnTo() == 1 && intentActivity.getTask() != this.mTargetStack.topTask()) {
                    TaskRecord tr = (TaskRecord) this.mTargetStack.getAllTasks().get(this.mTargetStack.getAllTasks().indexOf(intentActivity.getTask()) + 1);
                    if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                        Slog.d(TAG, "The intentActivity is " + intentActivity + ",task is " + intentActivity.getTask() + " then set the TaskToReturnTo=HOME of this task " + tr + " above it.");
                        Slog.d(TAG, "Bring to front target: " + this.mTargetStack + " from " + intentActivity);
                    }
                    if (tr != null) {
                        tr.setTaskToReturnTo(1);
                    }
                }
                if (this.mLaunchTaskBehind && this.mSourceRecord != null) {
                    intentActivity.setTaskToAffiliateWith(this.mSourceRecord.getTask());
                }
                this.mMovedOtherTask = true;
                if (!((this.mLaunchFlags & 268468224) == 268468224)) {
                    ActivityStack launchStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.getTask(), this.mOptions);
                    TaskRecord intentTask = intentActivity.getTask();
                    if (launchStack == null || launchStack == this.mTargetStack) {
                        this.mTargetStack.moveTaskToFrontLocked(intentTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringingFoundTaskToFront");
                        this.mMovedToFront = true;
                    } else if (launchStack.mStackId == 3 || launchStack.mStackId == 1) {
                        if ((this.mLaunchFlags & 4096) != 0) {
                            intentTask.reparent(launchStack.mStackId, true, 0, true, true, "launchToSide");
                        } else {
                            this.mTargetStack.moveTaskToFrontLocked(intentTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "bringToFrontInsteadOfAdjacentLaunch");
                        }
                        this.mMovedToFront = true;
                    } else if (launchStack.mDisplayId != this.mTargetStack.mDisplayId) {
                        if (HwPCUtils.isPcCastModeInServer()) {
                            HwPCUtils.log(TAG, " the activity will reparentToDisplay because computer stack is:" + launchStack.mStackId + "#" + launchStack.mDisplayId + " target stack is " + this.mTargetStack.mStackId + "#" + this.mTargetStack.mDisplayId);
                        }
                        intentActivity.getTask().reparent(launchStack.mStackId, true, 0, true, true, "reparentToDisplay");
                        this.mMovedToFront = true;
                    }
                    this.mOptions = null;
                    if (launchStack != null && launchStack.mActivityContainer != null && HwPCUtils.isPcDynamicStack(launchStack.mStackId) && launchStack.getAllTasks().isEmpty()) {
                        launchStack.mActivityContainer.onTaskListEmptyLocked();
                    }
                    if (!INCALLUI_ACTIVITY_CLASS_NAME.equals(intentActivity.shortComponentName)) {
                        intentActivity.showStartingWindow(null, false, true);
                    }
                }
                updateTaskReturnToType(intentActivity.getTask(), this.mLaunchFlags, focusStack);
            }
        }
        if (!this.mMovedToFront && this.mDoResume) {
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.d(ActivityStackSupervisor.TAG_TASKS, "Bring to front target: " + this.mTargetStack + " from " + intentActivity);
            }
            this.mTargetStack.moveToFront("intentActivityFound");
        }
        this.mSupervisor.handleNonResizableTaskIfNeeded(intentActivity.getTask(), -1, 0, this.mTargetStack.mStackId);
        if ((this.mLaunchFlags & DumpState.DUMP_COMPILER_STATS) == 0) {
            return intentActivity;
        }
        return this.mTargetStack.resetTaskIfNeededLocked(intentActivity, this.mStartActivity);
    }

    private void updateTaskReturnToType(TaskRecord task, int launchFlags, ActivityStack focusedStack) {
        boolean isSuperPowerSavingMode = SystemProperties.getBoolean("sys.super_power_save", false);
        boolean isRideModeEnabled = SystemProperties.getBoolean("sys.ride_mode", false);
        if (isSuperPowerSavingMode || isRideModeEnabled) {
            task.setTaskToReturnTo(0);
        } else if ((launchFlags & 268451840) == 268451840) {
            task.setTaskToReturnTo(1);
        } else if (focusedStack == null || focusedStack.isHomeStack()) {
            task.setTaskToReturnTo(1);
        } else if (focusedStack == null || focusedStack == task.getStack() || !focusedStack.isAssistantStack()) {
            task.setTaskToReturnTo(0);
        } else {
            task.setTaskToReturnTo(3);
        }
    }

    private void setTaskFromIntentActivity(ActivityRecord intentActivity) {
        TaskRecord task;
        if ((this.mLaunchFlags & 268468224) == 268468224) {
            task = intentActivity.getTask();
            task.performClearTaskLocked();
            this.mReuseTask = task;
            this.mReuseTask.setIntent(this.mStartActivity);
            this.mMovedOtherTask = true;
        } else if ((this.mLaunchFlags & 67108864) != 0 || this.mLaunchSingleInstance || this.mLaunchSingleTask) {
            if (intentActivity.getTask().performClearTaskLocked(this.mStartActivity, this.mLaunchFlags) == null) {
                this.mAddingToTask = true;
                if (!isInSkipCancelResultList(this.mStartActivity.shortComponentName)) {
                    this.mStartActivity.setTask(null);
                }
                this.mSourceRecord = intentActivity;
                task = this.mSourceRecord.getTask();
                if (task != null && task.getStack() == null) {
                    this.mTargetStack = computeStackFocus(this.mSourceRecord, false, null, this.mLaunchFlags, this.mOptions);
                    this.mTargetStack.addTask(task, this.mLaunchTaskBehind ^ 1, "startActivityUnchecked");
                }
            }
        } else if (this.mStartActivity.realActivity.equals(intentActivity.getTask().realActivity)) {
            if (((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop) && intentActivity.realActivity.equals(this.mStartActivity.realActivity)) {
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, intentActivity.getTask());
                if (intentActivity.frontOfTask) {
                    intentActivity.getTask().setIntent(this.mStartActivity);
                }
                intentActivity.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            } else if (!intentActivity.getTask().isSameIntentFilter(this.mStartActivity)) {
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

    private int setTaskFromReuseOrCreateNewTask(TaskRecord taskToAffiliate, int preferredLaunchStackId, ActivityStack topStack) {
        this.mTargetStack = computeStackFocus(this.mStartActivity, true, this.mLaunchBounds, this.mLaunchFlags, this.mOptions);
        if (this.mReuseTask == null) {
            addOrReparentStartingActivity(this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId), this.mNewTaskInfo != null ? this.mNewTaskInfo : this.mStartActivity.info, this.mNewTaskIntent != null ? this.mNewTaskIntent : this.mIntent, this.mVoiceSession, this.mVoiceInteractor, this.mLaunchTaskBehind ^ 1, this.mStartActivity.mActivityType), "setTaskFromReuseOrCreateNewTask - mReuseTask");
            if (this.mLaunchBounds != null) {
                int stackId = this.mTargetStack.mStackId;
                if (StackId.resizeStackWithLaunchBounds(stackId)) {
                    this.mService.resizeStack(stackId, this.mLaunchBounds, true, false, true, -1);
                } else {
                    this.mStartActivity.getTask().updateOverrideConfiguration(this.mLaunchBounds);
                }
            }
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in new task " + this.mStartActivity.getTask());
            }
        } else {
            addOrReparentStartingActivity(this.mReuseTask, "setTaskFromReuseOrCreateNewTask");
        }
        if (taskToAffiliate != null) {
            this.mStartActivity.setTaskToAffiliateWith(taskToAffiliate);
        }
        if (this.mSupervisor.isLockTaskModeViolation(this.mStartActivity.getTask())) {
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        if (!this.mMovedOtherTask) {
            TaskRecord task = this.mStartActivity.getTask();
            int i = this.mLaunchFlags;
            if (preferredLaunchStackId != -1) {
                topStack = this.mTargetStack;
            }
            updateTaskReturnToType(task, i, topStack);
        }
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("reuseOrNewTask");
        }
        return 0;
    }

    private int setTaskFromSourceRecord() {
        if (this.mSupervisor.isLockTaskModeViolation(this.mSourceRecord.getTask())) {
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        TaskRecord sourceTask = this.mSourceRecord.getTask();
        ActivityStack sourceStack = this.mSourceRecord.getStack();
        if (sourceStack.topTask() != sourceTask) {
            this.mTargetStack = getLaunchStack(this.mStartActivity, this.mLaunchFlags, this.mStartActivity.getTask(), this.mOptions);
        }
        if (this.mTargetStack == null) {
            this.mTargetStack = sourceStack;
        } else if (this.mTargetStack != sourceStack) {
            sourceTask.reparent(this.mTargetStack.mStackId, true, 0, false, true, "launchToSide");
        }
        if (this.mTargetStack.topTask() != sourceTask && (this.mAvoidMoveToFront ^ 1) != 0) {
            this.mTargetStack.moveTaskToFrontLocked(sourceTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "sourceTaskToFront");
        } else if (this.mDoResume) {
            this.mTargetStack.moveToFront("sourceStackToFront");
        }
        ActivityRecord top;
        if (!this.mAddingToTask && (this.mLaunchFlags & 67108864) != 0) {
            top = sourceTask.performClearTaskLocked(this.mStartActivity, this.mLaunchFlags);
            this.mKeepCurTransition = true;
            if (top != null) {
                ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, this.mStartActivity, top.getTask());
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
                TaskRecord task = top.getTask();
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
        addOrReparentStartingActivity(sourceTask, "setTaskFromSourceRecord");
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in existing task " + this.mStartActivity.getTask() + " from source " + this.mSourceRecord);
        }
        return 0;
    }

    private int setTaskFromInTask() {
        if (this.mSupervisor.isLockTaskModeViolation(this.mInTask)) {
            Slog.e(TAG, "Attempted Lock Task Mode violation mStartActivity=" + this.mStartActivity);
            return 101;
        }
        this.mTargetStack = this.mInTask.getStack();
        ActivityRecord top = this.mInTask.getTopActivity();
        if (top != null && top.realActivity.equals(this.mStartActivity.realActivity) && top.userId == this.mStartActivity.userId && ((this.mLaunchFlags & 536870912) != 0 || this.mLaunchSingleTop || this.mLaunchSingleTask)) {
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            ActivityStack.logStartActivity(EventLogTags.AM_NEW_INTENT, top, top.getTask());
            if ((this.mStartFlags & 1) != 0) {
                return 1;
            }
            top.deliverNewIntentLocked(this.mCallingUid, this.mStartActivity.intent, this.mStartActivity.launchedFromPackage);
            return 3;
        } else if (this.mAddingToTask) {
            if (this.mLaunchBounds != null) {
                this.mInTask.updateOverrideConfiguration(this.mLaunchBounds);
                int stackId = this.mInTask.getLaunchStackId();
                if (stackId != this.mInTask.getStackId()) {
                    this.mInTask.reparent(stackId, true, 1, false, true, "inTaskToFront");
                    stackId = this.mInTask.getStackId();
                    this.mTargetStack = this.mInTask.getStack();
                }
                if (StackId.resizeStackWithLaunchBounds(stackId)) {
                    this.mService.resizeStack(stackId, this.mLaunchBounds, true, false, true, -1);
                }
            }
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            addOrReparentStartingActivity(this.mInTask, "setTaskFromInTask");
            if (ActivityManagerDebugConfig.DEBUG_TASKS) {
                Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in explicit task " + this.mStartActivity.getTask());
            }
            return 0;
        } else {
            this.mTargetStack.moveTaskToFrontLocked(this.mInTask, this.mNoAnimation, this.mOptions, this.mStartActivity.appTimeTracker, "inTaskToFront");
            ActivityOptions.abort(this.mOptions);
            return 2;
        }
    }

    private void setTaskToCurrentTopOrCreateNewTask() {
        TaskRecord task;
        this.mTargetStack = computeStackFocus(this.mStartActivity, false, null, this.mLaunchFlags, this.mOptions);
        if (this.mDoResume) {
            this.mTargetStack.moveToFront("addingToTopTask");
        }
        ActivityRecord prev = this.mTargetStack.topActivity();
        if (prev != null) {
            task = prev.getTask();
        } else {
            task = this.mTargetStack.createTaskRecord(this.mSupervisor.getNextTaskIdForUserLocked(this.mStartActivity.userId), this.mStartActivity.info, this.mIntent, null, null, true, this.mStartActivity.mActivityType);
        }
        addOrReparentStartingActivity(task, "setTaskToCurrentTopOrCreateNewTask");
        this.mTargetStack.positionChildWindowContainerAtTop(task);
        if (ActivityManagerDebugConfig.DEBUG_TASKS) {
            Slog.v(ActivityStackSupervisor.TAG_TASKS, "Starting new activity " + this.mStartActivity + " in new guessed " + this.mStartActivity.getTask());
        }
    }

    private void addOrReparentStartingActivity(TaskRecord parent, String reason) {
        if (this.mStartActivity.getTask() == null || this.mStartActivity.getTask() == parent) {
            parent.addActivityToTop(this.mStartActivity);
        } else {
            this.mStartActivity.reparent(parent, parent.mActivities.size(), reason);
        }
    }

    private int adjustLaunchFlagsToDocumentMode(ActivityRecord r, boolean launchSingleInstance, boolean launchSingleTask, int launchFlags) {
        if ((launchFlags & DumpState.DUMP_FROZEN) == 0 || !(launchSingleInstance || launchSingleTask)) {
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
        }
        Slog.i(TAG, "Ignoring FLAG_ACTIVITY_NEW_DOCUMENT, launchMode is \"singleInstance\" or \"singleTask\"");
        return launchFlags & -134742017;
    }

    final void doPendingActivityLaunchesLocked(boolean doResume) {
        while (!this.mPendingActivityLaunches.isEmpty()) {
            PendingActivityLaunch pal = (PendingActivityLaunch) this.mPendingActivityLaunches.remove(0);
            try {
                startActivity(pal.r, pal.sourceRecord, null, null, pal.startFlags, doResume ? this.mPendingActivityLaunches.isEmpty() : false, null, null, null);
            } catch (Exception e) {
                Slog.e(TAG, "Exception during pending activity launch pal=" + pal, e);
                pal.sendErrorResult(e.getMessage());
            }
        }
    }

    private ActivityStack computeStackFocus(ActivityRecord r, boolean newTask, Rect bounds, int launchFlags, ActivityOptions aOptions) {
        TaskRecord task = r.getTask();
        ActivityStack stack = getLaunchStack(r, launchFlags, task, aOptions);
        if (stack != null) {
            return stack;
        }
        ActivityStack currentStack = task != null ? task.getStack() : null;
        if (currentStack != null) {
            if (this.mSupervisor.mFocusedStack != currentStack) {
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.d(TAG_FOCUS, "computeStackFocus: Setting focused stack to r=" + r + " task=" + task);
                }
            } else if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: Focused stack already=" + this.mSupervisor.mFocusedStack);
            }
            return currentStack;
        }
        ActivityContainer container = r.mInitialActivityContainer;
        if (container != null) {
            r.mInitialActivityContainer = null;
            return container.mStack;
        } else if (canLaunchIntoFocusedStack(r, newTask)) {
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: Have a focused stack=" + this.mSupervisor.mFocusedStack);
            }
            return this.mSupervisor.mFocusedStack;
        } else {
            if (this.mSourceDisplayId != 0) {
                stack = this.mSupervisor.getValidLaunchStackOnDisplay(this.mSourceDisplayId, r);
                if (stack == null) {
                    if (ActivityManagerDebugConfig.DEBUG_STACK) {
                        Slog.d(TAG_FOCUS, "computeStackFocus: Can't launch on mSourceDisplayId=" + this.mSourceDisplayId + ", looking on all displays.");
                    }
                    stack = this.mSupervisor.getNextValidLaunchStackLocked(r, this.mSourceDisplayId);
                }
            }
            if (stack == null) {
                int stackId;
                ArrayList<ActivityStack> homeDisplayStacks = this.mSupervisor.mHomeStack.mStacks;
                for (int stackNdx = homeDisplayStacks.size() - 1; stackNdx >= 0; stackNdx--) {
                    stack = (ActivityStack) homeDisplayStacks.get(stackNdx);
                    if ((HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isPcDynamicStack(stack.mStackId)) && StackId.isDynamicStack(stack.mStackId)) {
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
            }
            if (ActivityManagerDebugConfig.DEBUG_STACK) {
                Slog.d(TAG_FOCUS, "computeStackFocus: New stack r=" + r + " stackId=" + stack.mStackId);
            }
            return stack;
        }
    }

    private boolean canLaunchIntoFocusedStack(ActivityRecord r, boolean newTask) {
        boolean canUseFocusedStack;
        boolean z = false;
        ActivityStack focusedStack = this.mSupervisor.mFocusedStack;
        int focusedStackId = this.mSupervisor.mFocusedStack.mStackId;
        switch (focusedStackId) {
            case 1:
                canUseFocusedStack = true;
                break;
            case 2:
                canUseFocusedStack = r.supportsFreeform();
                break;
            case 3:
                canUseFocusedStack = r.supportsSplitScreen();
                break;
            case 6:
                canUseFocusedStack = r.isAssistantActivity();
                break;
            default:
                if (!HwPCUtils.isPcDynamicStack(focusedStackId)) {
                    if (!StackId.isDynamicStack(focusedStackId)) {
                        canUseFocusedStack = false;
                        break;
                    }
                    canUseFocusedStack = r.canBeLaunchedOnDisplay(focusedStack.mDisplayId);
                    break;
                }
                return false;
        }
        if (canUseFocusedStack && ((!newTask || focusedStack.mActivityContainer.isEligibleForNewTasks()) && this.mSourceDisplayId == focusedStack.mDisplayId)) {
            z = true;
        }
        return z;
    }

    private ActivityStack getLaunchStack(ActivityRecord r, int launchFlags, TaskRecord task, ActivityOptions aOptions) {
        if (this.mReuseTask != null) {
            return this.mReuseTask.getStack();
        }
        if (r.isHomeActivity()) {
            return this.mSupervisor.mHomeStack;
        }
        if (r.isRecentsActivity()) {
            return this.mSupervisor.getStack(5, true, true);
        }
        if (r.isAssistantActivity()) {
            return this.mSupervisor.getStack(6, true, true);
        }
        int launchDisplayId = aOptions != null ? aOptions.getLaunchDisplayId() : -1;
        int launchStackId = aOptions != null ? aOptions.getLaunchStackId() : -1;
        if (launchStackId != -1 && launchDisplayId != -1) {
            throw new IllegalArgumentException("Stack and display id can't be set at the same time.");
        } else if (isValidLaunchStackId(launchStackId, launchDisplayId, r)) {
            return this.mSupervisor.getStack(launchStackId, true, true);
        } else {
            if (launchStackId == 3) {
                return this.mSupervisor.getStack(1, true, true);
            }
            if (launchDisplayId != -1) {
                return this.mSupervisor.getValidLaunchStackOnDisplay(launchDisplayId, r);
            }
            if (this.mUsingVr2dDisplay) {
                ActivityStack as = this.mSupervisor.getValidLaunchStackOnDisplay(this.mSourceDisplayId, r);
                if (ActivityManagerDebugConfig.DEBUG_STACK) {
                    Slog.v(TAG, "Launch stack for app: " + r.toString() + ", on virtual display stack:" + as.toString());
                }
                return as;
            } else if ((launchFlags & 4096) == 0 || this.mSourceDisplayId != 0) {
                return null;
            } else {
                ActivityStack parentStack;
                if (task != null) {
                    parentStack = task.getStack();
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
                if (parentStack != null && parentStack.isDockedStack()) {
                    return this.mSupervisor.getStack(1, true, true);
                }
                ActivityStack dockedStack = this.mSupervisor.getStack(3);
                if (dockedStack == null || dockedStack.shouldBeVisible(r) != 0) {
                    return dockedStack;
                }
                return null;
            }
        }
    }

    boolean isValidLaunchStackId(int stackId, int displayId, ActivityRecord r) {
        switch (stackId) {
            case -1:
            case 0:
                return false;
            case 1:
                return true;
            case 2:
                return r.supportsFreeform();
            case 3:
                return r.supportsSplitScreen();
            case 4:
                return r.supportsPictureInPicture();
            case 5:
                return r.isRecentsActivity();
            case 6:
                return r.isAssistantActivity();
            default:
                if (HwPCUtils.isPcDynamicStack(stackId) && this.mService.mSupportsMultiDisplay && HwPCUtils.isPcCastModeInServer()) {
                    return true;
                }
                if (StackId.isDynamicStack(stackId)) {
                    return r.canBeLaunchedOnDisplay(displayId);
                }
                Slog.e(TAG, "isValidLaunchStackId: Unexpected stackId=" + stackId);
                return false;
        }
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

    static boolean isDocumentLaunchesIntoExisting(int flags) {
        if ((DumpState.DUMP_FROZEN & flags) == 0 || (134217728 & flags) != 0) {
            return false;
        }
        return true;
    }

    boolean clearPendingActivityLaunchesLocked(String packageName) {
        boolean didSomething = false;
        for (int palNdx = this.mPendingActivityLaunches.size() - 1; palNdx >= 0; palNdx--) {
            ActivityRecord r = ((PendingActivityLaunch) this.mPendingActivityLaunches.get(palNdx)).r;
            if (r != null && r.packageName.equals(packageName)) {
                this.mPendingActivityLaunches.remove(palNdx);
                didSomething = true;
            }
        }
        return didSomething;
    }

    protected boolean standardizeHomeIntent(ResolveInfo rInfo, Intent intent) {
        return false;
    }

    void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + "ActivityStarter:");
        prefix = prefix + "  ";
        pw.println(prefix + "mCurrentUser=" + this.mSupervisor.mCurrentUser);
        pw.println(prefix + "mLastStartReason=" + this.mLastStartReason);
        pw.println(prefix + "mLastStartActivityTimeMs=" + DateFormat.getDateTimeInstance().format(new Date(this.mLastStartActivityTimeMs)));
        pw.println(prefix + "mLastStartActivityResult=" + this.mLastStartActivityResult);
        ActivityRecord r = this.mLastStartActivityRecord[0];
        if (r != null) {
            pw.println(prefix + "mLastStartActivityRecord:");
            r.dump(pw, prefix + " ");
        }
        pw.println(prefix + "mLastHomeActivityStartResult=" + this.mLastHomeActivityStartResult);
        r = this.mLastHomeActivityStartRecord[0];
        if (r != null) {
            pw.println(prefix + "mLastHomeActivityStartRecord:");
            r.dump(pw, prefix + " ");
        }
        if (this.mStartActivity != null) {
            pw.println(prefix + "mStartActivity:");
            this.mStartActivity.dump(pw, prefix + " ");
        }
        if (this.mIntent != null) {
            pw.println(prefix + "mIntent=" + this.mIntent);
        }
        if (this.mOptions != null) {
            pw.println(prefix + "mOptions=" + this.mOptions);
        }
        pw.println(prefix + "mLaunchSingleTop=" + this.mLaunchSingleTop + " mLaunchSingleInstance=" + this.mLaunchSingleInstance + " mLaunchSingleTask=" + this.mLaunchSingleTask + " mLaunchFlags=0x" + Integer.toHexString(this.mLaunchFlags) + " mDoResume=" + this.mDoResume + " mAddingToTask=" + this.mAddingToTask);
    }

    protected static boolean clearFrpRestricted(Context context, int userId) {
        return Secure.putIntForUser(context.getContentResolver(), SUW_FRP_STATE, 0, userId);
    }

    protected static boolean isFrpRestricted(Context context, int userId) {
        return Secure.getIntForUser(context.getContentResolver(), SUW_FRP_STATE, 0, userId) == 1;
    }
}
