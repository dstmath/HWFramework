package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import com.huawei.android.app.ActivityOptionsEx;
import com.huawei.android.app.WindowConfigurationEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.SettingsExEx;
import com.huawei.android.util.SlogEx;
import java.util.HashSet;

public class HwRootActivityContainerEx extends RootActivityContainerBridgeEx {
    private static final int APP_LOCK_START_DELAY_MAXTIMES = 5;
    private static final int APP_LOCK_START_DELAY_TIME = 500;
    private static final String EXTRA_USER_HANDLE_HWEX = "android.intent.extra.user_handle_hwex";
    private static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemPropertiesEx.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final String KEY_LAUNCH_WINDOWING_MODE = "android.activity.windowingMode";
    private static final String OPTIONS_KEY = "options";
    private static final int SCREEN_OFF = 1;
    private static final String SEMICOLON_STR = ";";
    private static final long START_APP_LOCK_TIMEOUT = 1000;
    private static final String TAG = "HwRootActivityContainerEx";
    private static long sAppLockStartTimes = 0;
    private static long sApplockStartTimeBegin = 0;
    IHwRootActivityContainerInner mIRacInner = null;
    private final ActivityTaskManagerServiceEx mService;
    private HashSet<Integer> mStartingAppLockStackSet = new HashSet<>();

    public HwRootActivityContainerEx(IHwRootActivityContainerInner rac, ActivityTaskManagerServiceEx service) {
        super(rac, service);
        this.mService = service;
        this.mIRacInner = rac;
    }

    public boolean resumeAppLockActivityIfNeeded(ActivityStackEx stack, ActivityOptions targetOptions, boolean ignoreResumed) {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx;
        if (isActivityStackEmpty(stack) || (activityTaskManagerServiceEx = this.mService) == null || activityTaskManagerServiceEx.getCurrentUserId() != 0) {
            return false;
        }
        ActivityRecordEx activityRecord = stack.topRunningActivityLocked();
        final int stackId = stack.getStackId();
        if (activityRecord == null || activityRecord.isEmpty() || isAppLockApplication(activityRecord) || this.mStartingAppLockStackSet.contains(Integer.valueOf(stackId))) {
            return false;
        }
        this.mService.notifyPopupCamera(activityRecord.getShortComponentName());
        if (!IS_HW_MULTIWINDOW_SUPPORTED && stack.inMultiWindowMode() && "outofsleep".equals(this.mService.getActivityStackSupervisorEx().getActivityLaunchTrack()) && !isAppInLockList(activityRecord.getPackageName(), activityRecord.getUserId())) {
            ActivityStackEx topOtherSplitStack = stack.getDisplay().getTopStackInWindowingMode(stack.inSplitScreenPrimaryWindowingMode() ? 4 : 3);
            if (topOtherSplitStack != null) {
                activityRecord = topOtherSplitStack.topRunningActivityLocked();
            }
        }
        if (activityRecord == null || ((!ignoreResumed && activityRecord.isResumedState()) || !isKeyguardDismiss() || !isAppInLockList(activityRecord.getPackageName(), activityRecord.getUserId()))) {
            return false;
        }
        long realTime = SystemClock.elapsedRealtime();
        if (realTime - sApplockStartTimeBegin < 500) {
            long j = sAppLockStartTimes;
            if (j >= 5) {
                SlogEx.e(TAG, "start applock too often, ignored in 500ms");
                return true;
            }
            sAppLockStartTimes = j + 1;
        } else {
            sApplockStartTimeBegin = realTime;
            sAppLockStartTimes = 0;
        }
        this.mStartingAppLockStackSet.add(Integer.valueOf(stackId));
        startAppLock(stack, activityRecord);
        this.mService.postDelayed(new Runnable() {
            /* class com.android.server.wm.HwRootActivityContainerEx.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                HwRootActivityContainerEx.this.mStartingAppLockStackSet.remove(Integer.valueOf(stackId));
            }
        }, (long) START_APP_LOCK_TIMEOUT);
        return true;
    }

    public void removeStartingAppLock(int stackId) {
        this.mStartingAppLockStackSet.remove(Integer.valueOf(stackId));
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
                ActivityOptionsEx.setLaunchWindowingMode(options, activityRecord.getWindowingMode());
                newIntent.putExtra(OPTIONS_KEY, options.toBundle());
            }
            ActivityOptionsEx.setLaunchTaskId(options, activityRecord.getTaskRecordEx().getTaskId());
            this.mService.getContext().startActivity(newIntent, options.toBundle());
            if (DEBUG_STATES) {
                SlogEx.i(TAG, "startAppLock packageName:" + activityRecord.getPackageName() + " userId:" + activityRecord.getUserId() + " taskId:" + activityRecord.getTaskRecordEx().getTaskId());
            }
        }
    }

    public boolean isAppInLockList(String pgkName, int userId) {
        ActivityTaskManagerServiceEx activityTaskManagerServiceEx;
        if (pgkName == null || (activityTaskManagerServiceEx = this.mService) == null || activityTaskManagerServiceEx.isAtmsNull() || Settings.Secure.getInt(this.mService.getContext().getContentResolver(), "app_lock_func_status", 0) != 1) {
            return false;
        }
        if ((SEMICOLON_STR + SettingsExEx.Secure.getStringForUser(this.mService.getContext().getContentResolver(), "app_lock_list", userId) + SEMICOLON_STR).contains(SEMICOLON_STR + pgkName + SEMICOLON_STR)) {
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
        this.mStartingAppLockStackSet.clear();
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
                                    final Intent newIntent = new Intent("huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER_LOCKSCREEN");
                                    newIntent.putExtra(EXTRA_USER_HANDLE_HWEX, topActivity.getUserId());
                                    newIntent.setPackage("com.huawei.systemmanager");
                                    newIntent.putExtra("android.intent.extra.PACKAGE_NAME", topActivity.getPackageName());
                                    newIntent.putExtra("android.intent.extra.COMPONENT_NAME", topActivity.getActivityComponent());
                                    newIntent.putExtra("android.intent.extra.TASK_ID", topActivity.getTaskRecordEx().getTaskId());
                                    newIntent.putExtra("android.intent.extra.REASON", 1);
                                    ActivityOptions options = ActivityOptions.makeBasic();
                                    ActivityOptionsEx.setLaunchWindowingMode(options, topActivity.getWindowingMode());
                                    ActivityOptionsEx.setLaunchTaskId(options, topActivity.getTaskRecordEx().getTaskId());
                                    final Bundle bundle = options.toBundle();
                                    newIntent.putExtra(OPTIONS_KEY, bundle);
                                    newIntent.addFlags(268435456);
                                    this.mService.post(new Runnable() {
                                        /* class com.android.server.wm.HwRootActivityContainerEx.AnonymousClass2 */

                                        @Override // java.lang.Runnable
                                        public void run() {
                                            HwRootActivityContainerEx.this.mService.getContext().startActivity(newIntent, bundle);
                                        }
                                    });
                                    if (DEBUG_STATES) {
                                        SlogEx.i(TAG, "checkStartAppLockActivity packageName:" + topActivity.getPackageName() + " userId:" + topActivity.getUserId() + " taskId:" + topActivity.getTaskRecordEx().getTaskId());
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

    public boolean checkWindowModeForAppLock(ActivityRecordEx target, ActivityRecordEx activityRecord) {
        try {
            if (!HwActivityStartInterceptor.isAppLockPackageName(target.getActivityInfo().packageName) || !HwActivityStartInterceptor.isAppLockAction(target.getIntent().getAction())) {
                return false;
            }
            return isSkipReusableScenario(target, activityRecord);
        } catch (BadParcelableException e) {
            SlogEx.e(TAG, "Check window mode for applock fail.");
            return false;
        } catch (NullPointerException e2) {
            SlogEx.e(TAG, "Check window mode for applock fail: null");
            return false;
        }
    }

    private boolean isSkipReusableScenario(ActivityRecordEx target, ActivityRecordEx activityRecord) {
        if (!IS_HW_MULTIWINDOW_SUPPORTED) {
            int windowingMode = target.getIntent().getIntExtra("windowMode", 0);
            if (windowingMode != activityRecord.getWindowingMode() || WindowConfigurationEx.isSplitScreenWindowingMode(windowingMode)) {
                SlogEx.i(TAG, "Skipping " + activityRecord + ": mismatch windowMode");
                return true;
            }
        } else if (HwActivityStartInterceptor.isAppLockPackageName(activityRecord.getActivityInfo().packageName) && HwActivityStartInterceptor.isAppLockAction(activityRecord.getIntent().getAction())) {
            Bundle bundle = target.getIntent().getBundleExtra(OPTIONS_KEY);
            int windowingMode2 = bundle != null ? bundle.getInt(KEY_LAUNCH_WINDOWING_MODE) : 1;
            if (windowingMode2 != activityRecord.getWindowingMode() && ((windowingMode2 != 1 || activityRecord.inMultiWindowMode()) && !shouldReuseActivity(target, activityRecord))) {
                SlogEx.i(TAG, "Skipping " + activityRecord + ": mismatch window mode");
                return true;
            }
        }
        if (!HwActivityStartInterceptor.isAppLockPackageName(activityRecord.getActivityInfo().packageName)) {
            if (!(activityRecord.getTaskRecordEx() == null || activityRecord.getTaskRecordEx().getIntent() == null || !HwActivityStartInterceptor.isAppLockAction(activityRecord.getTaskRecordEx().getIntent().getAction()))) {
                SlogEx.i(TAG, "Skipping " + activityRecord + ": mismatch task");
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
                SlogEx.i(TAG, "Skipping : mismatch component name " + targetCompName);
                return true;
            }
        }
        return false;
    }

    private boolean isAppHasLock(String pgkName, int userId) {
        if (pgkName == null || Settings.Secure.getInt(this.mService.getContext().getContentResolver(), "app_lock_func_status", 0) != 1) {
            return false;
        }
        if (!(SEMICOLON_STR + SettingsExEx.Secure.getStringForUser(this.mService.getContext().getContentResolver(), "applock_unlocked_list", userId) + SEMICOLON_STR).contains(SEMICOLON_STR + pgkName + SEMICOLON_STR)) {
            if (!(SEMICOLON_STR + SettingsExEx.Secure.getStringForUser(this.mService.getContext().getContentResolver(), "app_lock_list", userId) + SEMICOLON_STR).contains(SEMICOLON_STR + pgkName + SEMICOLON_STR)) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldReuseActivity(ActivityRecordEx targetRecord, ActivityRecordEx existingRecord) {
        if (targetRecord != null && existingRecord != null && HwActivityStartInterceptor.isAppLockActivity(targetRecord.getShortComponentName()) && HwActivityStartInterceptor.isAppLockActivity(existingRecord.getShortComponentName())) {
            try {
                Intent targetIntent = targetRecord.getIntent();
                Intent existingIntent = existingRecord.getIntent();
                if (targetIntent != null) {
                    if (existingIntent != null) {
                        if (targetIntent.getIntExtra(EXTRA_USER_HANDLE_HWEX, 0) != existingIntent.getIntExtra(EXTRA_USER_HANDLE_HWEX, 0)) {
                            return false;
                        }
                        String targetPkg = targetIntent.getStringExtra("android.intent.extra.PACKAGE_NAME");
                        String existingPkg = existingIntent.getStringExtra("android.intent.extra.PACKAGE_NAME");
                        if (targetPkg != null) {
                            return targetPkg.equals(existingPkg);
                        }
                    }
                }
                return false;
            } catch (BadParcelableException e) {
                SlogEx.w(TAG, "shouldReuseActivity get extra data error.");
            }
        }
        return false;
    }
}
