package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.Intent;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;

public class HwRootActivityContainerEx extends RootActivityContainerBridgeEx {
    private static final int APP_LOCK_START_DELAY_MAXTIMES = 5;
    private static final int APP_LOCK_START_DELAY_TIME = 500;
    private static final String EXTRA_USER_HANDLE_HWEX = "android.intent.extra.user_handle_hwex";
    private static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final String KEY_LAUNCH_WINDOWING_MODE = "android.activity.windowingMode";
    private static final String OPTIONS_KEY = "options";
    private static final int SCREEN_OFF = 1;
    private static final String SEMICOLON_STR = ";";
    private static final String TAG = "HwRootActivityContainerEx";
    private static long sAppLockStartTimes = 0;
    private static long sApplockStartTimeBegin = 0;
    IHwRootActivityContainerInner mIRacInner = null;
    private final ActivityTaskManagerServiceEx mService;

    public HwRootActivityContainerEx(IHwRootActivityContainerInner rac, ActivityTaskManagerServiceEx service) {
        super(rac, service);
        this.mService = service;
        this.mIRacInner = rac;
    }

    public boolean resumeAppLockActivityIfNeeded(ActivityStackEx stack, ActivityOptions targetOptions) {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx;
        ActivityRecordEx activityRecord;
        int windowMode;
        if (isActivityStackEmpty(stack) || (activityTaskManagerServiceEx = this.mService) == null || activityTaskManagerServiceEx.getCurrentUserId() != 0 || (activityRecord = stack.topRunningActivityLocked()) == null || activityRecord.isEmpty() || isAppLockApplication(activityRecord)) {
            return false;
        }
        this.mService.notifyPopupCamera(activityRecord.getShortComponentName());
        if (!IS_HW_MULTIWINDOW_SUPPORTED && stack.inMultiWindowMode() && "outofsleep".equals(this.mService.getActivityStackSupervisorEx().getActivityLaunchTrack()) && !isAppInLockList(activityRecord.getPackageName(), activityRecord.getUserId())) {
            if (stack.inSplitScreenPrimaryWindowingMode()) {
                windowMode = 4;
            } else {
                windowMode = 3;
            }
            ActivityStackEx topOtherSplitStack = stack.getDisplay().getTopStackInWindowingMode(windowMode);
            if (topOtherSplitStack != null) {
                activityRecord = topOtherSplitStack.topRunningActivityLocked();
            }
        }
        if (activityRecord == null || activityRecord.isResumedState() || !isKeyguardDismiss() || !isAppInLockList(activityRecord.getPackageName(), activityRecord.getUserId())) {
            return false;
        }
        long realTime = SystemClock.elapsedRealtime();
        if (realTime - sApplockStartTimeBegin < 500) {
            long j = sAppLockStartTimes;
            if (j >= 5) {
                Slog.e(TAG, "start applock too often, ignored in 500ms");
                return true;
            }
            sAppLockStartTimes = j + 1;
        } else {
            sApplockStartTimeBegin = realTime;
            sAppLockStartTimes = 0;
        }
        startAppLock(stack, activityRecord);
        return true;
    }

    private boolean isActivityStackEmpty(ActivityStackEx activityStackEx) {
        return activityStackEx == null || activityStackEx.isActivityStackNull();
    }

    private void startAppLock(ActivityStackEx stack, ActivityRecordEx activityRecord) {
        if (!isActivityStackEmpty(stack)) {
            Intent newIntent = new Intent("huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER_LOCKSCREEN");
            newIntent.putExtra(EXTRA_USER_HANDLE_HWEX, activityRecord.getUserId());
            int flags = 109051904;
            if (IS_HW_MULTIWINDOW_SUPPORTED && !stack.getDisplay().isSleeping()) {
                flags = 109051904 | 1073741824;
            }
            newIntent.setFlags(flags);
            newIntent.setPackage("com.huawei.systemmanager");
            newIntent.putExtra("android.intent.extra.TASK_ID", activityRecord.getTaskRecordEx().getTaskId());
            newIntent.putExtra("android.intent.extra.PACKAGE_NAME", activityRecord.getPackageName());
            newIntent.putExtra("android.intent.extra.COMPONENT_NAME", activityRecord.getActivityComponent());
            ActivityOptions options = ActivityOptions.makeBasic();
            if (!IS_HW_MULTIWINDOW_SUPPORTED) {
                newIntent.putExtra("windowMode", stack.getWindowingMode());
            } else {
                options.setLaunchWindowingMode(activityRecord.getWindowingMode());
                newIntent.putExtra(OPTIONS_KEY, options.toBundle());
            }
            options.setLaunchTaskId(activityRecord.getTaskRecordEx().getTaskId());
            this.mService.getContext().startActivity(newIntent, options.toBundle());
            if (DEBUG_STATES) {
                Slog.i(TAG, "startAppLock packageName:" + activityRecord.getPackageName() + " userId:" + activityRecord.getUserId() + " taskId:" + activityRecord.getTaskRecordEx().getTaskId());
            }
        }
    }

    public boolean isAppInLockList(String pgkName, int userId) {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx;
        if (pgkName == null || (activityTaskManagerServiceEx = this.mService) == null || activityTaskManagerServiceEx.isAtmsNull() || Settings.Secure.getInt(this.mService.getContext().getContentResolver(), "app_lock_func_status", 0) != 1) {
            return false;
        }
        if ((";" + Settings.Secure.getStringForUser(this.mService.getContext().getContentResolver(), "app_lock_list", userId) + ";").contains(";" + pgkName + ";")) {
            return true;
        }
        return false;
    }

    private boolean isAppLockApplication(ActivityRecordEx activityRecord) {
        return activityRecord.getIntent() != null && HwActivityStartInterceptor.isAppLockPackageName(activityRecord.getPackageName()) && HwActivityStartInterceptor.isAppLockAction(activityRecord.getIntent().getAction());
    }

    private boolean isKeyguardDismiss() {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx = this.mService;
        if (activityTaskManagerServiceEx == null || activityTaskManagerServiceEx.getActivityStackSupervisorEx().getKeyguardController().isKeyguardLocked() || this.mService.getWindowManager().isPendingLock()) {
            return false;
        }
        return true;
    }

    public void checkStartAppLockActivity() {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx;
        if (IS_HW_MULTIWINDOW_SUPPORTED && (activityTaskManagerServiceEx = this.mService) != null) {
            for (int displayNdx = activityTaskManagerServiceEx.getRootActivityContainer().getChildCount() - 1; displayNdx >= 0; displayNdx--) {
                ActivityDisplayEx display = this.mService.getRootActivityContainer().getChildAt(displayNdx);
                if (display.getDisplayId() == 0 && display.isSleeping()) {
                    for (int stackNdx = display.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                        ActivityStackEx stack = display.getChildAt(stackNdx);
                        if (stack != null && stack.shouldBeVisible((ActivityRecordEx) null)) {
                            ActivityRecordEx topActivity = stack.topRunningActivityLocked();
                            if (topActivity != null && !isAppLockApplication(topActivity)) {
                                if (!topActivity.isResumedState() && isAppHasLock(topActivity.getPackageName(), topActivity.getUserId())) {
                                    Intent newIntent = new Intent("huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER_LOCKSCREEN");
                                    newIntent.putExtra(EXTRA_USER_HANDLE_HWEX, topActivity.getUserId());
                                    newIntent.setPackage("com.huawei.systemmanager");
                                    newIntent.putExtra("android.intent.extra.PACKAGE_NAME", topActivity.getPackageName());
                                    newIntent.putExtra("android.intent.extra.COMPONENT_NAME", topActivity.getActivityComponent());
                                    newIntent.putExtra("android.intent.extra.TASK_ID", topActivity.getTaskRecordEx().getTaskId());
                                    newIntent.putExtra("android.intent.extra.REASON", 1);
                                    ActivityOptions options = ActivityOptions.makeBasic();
                                    options.setLaunchWindowingMode(topActivity.getWindowingMode());
                                    options.setLaunchTaskId(topActivity.getTaskRecordEx().getTaskId());
                                    Bundle bundle = options.toBundle();
                                    newIntent.putExtra(OPTIONS_KEY, bundle);
                                    newIntent.addFlags(268435456);
                                    this.mService.post(new Runnable(newIntent, bundle) {
                                        /* class com.android.server.wm.$$Lambda$HwRootActivityContainerEx$OTUFHSluHUFSag2ryPfnZa1fZ8M */
                                        private final /* synthetic */ Intent f$1;
                                        private final /* synthetic */ Bundle f$2;

                                        {
                                            this.f$1 = r2;
                                            this.f$2 = r3;
                                        }

                                        @Override // java.lang.Runnable
                                        public final void run() {
                                            HwRootActivityContainerEx.this.lambda$checkStartAppLockActivity$0$HwRootActivityContainerEx(this.f$1, this.f$2);
                                        }
                                    });
                                    if (DEBUG_STATES) {
                                        Slog.i(TAG, "checkStartAppLockActivity packageName:" + topActivity.getPackageName() + " userId:" + topActivity.getUserId() + " taskId:" + topActivity.getTaskRecordEx().getTaskId());
                                    }
                                }
                                if (!stack.inMultiWindowMode()) {
                                    break;
                                }
                            } else if (!(topActivity == null || topActivity.getIntent() == null)) {
                                topActivity.getIntent().removeFlags(1073741824);
                            }
                        }
                    }
                }
            }
        }
    }

    public /* synthetic */ void lambda$checkStartAppLockActivity$0$HwRootActivityContainerEx(Intent newIntent, Bundle bundle) {
        this.mService.getContext().startActivity(newIntent, bundle);
    }

    public boolean checkWindowModeForAppLock(ActivityRecordEx target, ActivityRecordEx activityRecord) {
        try {
            if (!HwActivityStartInterceptor.isAppLockPackageName(target.getActivityInfo().packageName) || !HwActivityStartInterceptor.isAppLockAction(target.getIntent().getAction())) {
                return false;
            }
            return isSkipReusableScenario(target, activityRecord);
        } catch (BadParcelableException e) {
            Slog.e(TAG, "Check window mode for applock fail.");
            return false;
        } catch (NullPointerException e2) {
            Slog.e(TAG, "Check window mode for applock fail: null");
            return false;
        }
    }

    private boolean isSkipReusableScenario(ActivityRecordEx target, ActivityRecordEx activityRecord) {
        if (!IS_HW_MULTIWINDOW_SUPPORTED) {
            int windowingMode = target.getIntent().getIntExtra("windowMode", 0);
            if (windowingMode != activityRecord.getWindowingMode() || WindowConfiguration.isSplitScreenWindowingMode(windowingMode)) {
                Slog.i(TAG, "Skipping " + activityRecord + ": mismatch windowMode");
                return true;
            }
        } else if (HwActivityStartInterceptor.isAppLockPackageName(activityRecord.getActivityInfo().packageName) && HwActivityStartInterceptor.isAppLockAction(activityRecord.getIntent().getAction())) {
            Bundle bundle = target.getIntent().getBundleExtra(OPTIONS_KEY);
            int windowingMode2 = bundle != null ? bundle.getInt(KEY_LAUNCH_WINDOWING_MODE) : 1;
            if (windowingMode2 != activityRecord.getWindowingMode() && (windowingMode2 != 1 || activityRecord.inMultiWindowMode())) {
                Slog.i(TAG, "Skipping " + activityRecord + ": mismatch window mode");
                return true;
            }
        }
        if (!HwActivityStartInterceptor.isAppLockPackageName(activityRecord.getActivityInfo().packageName)) {
            if (!(activityRecord.getTaskRecordEx() == null || activityRecord.getTaskRecordEx().getIntent() == null || !HwActivityStartInterceptor.isAppLockAction(activityRecord.getTaskRecordEx().getIntent().getAction()))) {
                Slog.i(TAG, "Skipping " + activityRecord + ": mismatch task");
                return true;
            }
        } else if (target.getIntent().getIntExtra(EXTRA_USER_HANDLE_HWEX, 0) != activityRecord.getIntent().getIntExtra(EXTRA_USER_HANDLE_HWEX, 0)) {
            return true;
        } else {
            String targetPackageName = target.getIntent().getStringExtra("android.intent.extra.PACKAGE_NAME");
            String recordPackageName = activityRecord.getIntent().getStringExtra("android.intent.extra.PACKAGE_NAME");
            if (targetPackageName != null && !targetPackageName.equals(recordPackageName)) {
                return true;
            }
            ComponentName targetCompName = (ComponentName) target.getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
            ComponentName recordCompName = (ComponentName) activityRecord.getIntent().getParcelableExtra("android.intent.extra.COMPONENT_NAME");
            if (targetCompName != null && !targetCompName.equals(recordCompName)) {
                Slog.i(TAG, "Skipping : mismatch component name " + targetCompName);
                return true;
            }
        }
        return false;
    }

    private boolean isAppHasLock(String pgkName, int userId) {
        if (pgkName == null || Settings.Secure.getInt(this.mService.getContext().getContentResolver(), "app_lock_func_status", 0) != 1) {
            return false;
        }
        if (!(";" + Settings.Secure.getStringForUser(this.mService.getContext().getContentResolver(), "applock_unlocked_list", userId) + ";").contains(";" + pgkName + ";")) {
            if (!(";" + Settings.Secure.getStringForUser(this.mService.getContext().getContentResolver(), "app_lock_list", userId) + ";").contains(";" + pgkName + ";")) {
                return false;
            }
        }
        return true;
    }
}
