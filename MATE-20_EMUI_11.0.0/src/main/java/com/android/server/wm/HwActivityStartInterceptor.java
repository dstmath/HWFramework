package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.IIntentSenderEx;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSenderEx;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import com.android.server.pm.UserManagerServiceEx;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.app.ProfilerInfoEx;
import com.huawei.hiai.awareness.AwarenessConstants;
import java.util.Set;

public final class HwActivityStartInterceptor extends ActivityStartInterceptorBridgeEx {
    private static final String EXTRA_USER_HANDLE_HWEX = "android.intent.extra.user_handle_hwex";
    private static final int HW_FLAG_START_ACTIVITIES = 1;
    private static final boolean IS_BOPD = SystemProperties.getBoolean("sys.bopd", false);
    private static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private static final String KEY_BLUR_BACKGROUND = "blurBackground";
    private static final String LAUNCHER_PKGNAME = "com.huawei.android.launcher";
    private static final String LAUNCHER_PKG_NAME = "com.huawei.android.launcher";
    private static final String LAUNCHER_POWER_SAVE = "com.huawei.android.launcher.powersavemode.PowerSaveModeLauncher";
    private static final String LAUNCHER_STREET_MODE = "com.huawei.android.launcher.streetmode.StreetModeLauncher";
    private static final String PROP_POWER_SAVE = "sys.super_power_save";
    private static final String PROP_RIDE_MODE = "sys.ride_mode";
    private static final int PROVISIONED_OFF = 0;
    private static final String TAG = "HwASInterceptor";
    private ActivityRecordEx mSourceRecord;

    public HwActivityStartInterceptor(ActivityTaskManagerServiceEx service, ActivityStackSupervisorEx supervisor) {
        super(service, supervisor);
    }

    public static boolean isAppLockAction(String action) {
        return isAppLockActionEx(action);
    }

    public static boolean isAppLockActivity(String activity) {
        return isAppLockActivityEx(activity);
    }

    public static boolean isAppLockPackageName(String packageName) {
        return isAppLockPackageNameEx(packageName);
    }

    public boolean interceptStartActivityIfNeed(Intent intent, ActivityOptions activityOptions) {
        if (intent == null) {
            return false;
        }
        if (!interceptStartActivityForAppLock(intent, activityOptions) && !changeIntentForDifferentModeIfNeed(intent)) {
            return false;
        }
        return true;
    }

    private boolean changeIntentForDifferentModeIfNeed(Intent intent) {
        boolean isChange;
        Set<String> categories = intent.getCategories();
        if (categories != null && categories.contains("android.intent.category.HOME")) {
            Intent targetIntent = new Intent(intent);
            if (SystemProperties.getBoolean("sys.super_power_save", false)) {
                targetIntent.removeCategory("android.intent.category.HOME");
                targetIntent.addFlags(4194304);
                targetIntent.setClassName("com.huawei.android.launcher", LAUNCHER_POWER_SAVE);
                isChange = true;
            } else if (SystemProperties.getBoolean(PROP_RIDE_MODE, false)) {
                targetIntent.removeCategory("android.intent.category.HOME");
                targetIntent.addFlags(4194304);
                targetIntent.setClassName("com.huawei.android.launcher", LAUNCHER_STREET_MODE);
                isChange = true;
            } else if (!IS_BOPD) {
                return false;
            } else {
                if (Settings.Global.getInt(this.mService.getContext().getContentResolver(), "device_provisioned", 0) == 0) {
                    Slog.i(TAG, "failed to set activity as EmergencyBackupActivity for bopd due to oobe not finished");
                    return false;
                }
                buildBackupIntent(targetIntent);
                Slog.i(TAG, "set activity as EmergencyBackupActivity in the mode of bopd successfully.");
                isChange = true;
            }
            if (isChange) {
                ResolveInfo tempResolveInfo = this.mSupervisor.resolveIntent(targetIntent, (String) null, getUserId(), 0, getRealCallingUid());
                ActivityInfo tempActivityInfo = this.mSupervisor.resolveActivity(targetIntent, tempResolveInfo, getStartFlags(), (ProfilerInfoEx) null);
                if (tempResolveInfo == null || tempActivityInfo == null) {
                    Slog.e(TAG, "Change intent for different mode null. mRInfo:" + tempResolveInfo + ", mAInfo:" + tempActivityInfo);
                    return false;
                }
                setIntent(targetIntent);
                setResolvedType(null);
                setResolveInfo(tempResolveInfo);
                setActivityInfo(tempActivityInfo);
                Slog.i(TAG, "Change intent for different mode not null.");
                return true;
            }
        }
        return false;
    }

    private void buildBackupIntent(Intent targetIntent) {
        targetIntent.removeCategory("android.intent.category.HOME");
        targetIntent.addFlags(4194304);
        targetIntent.setComponent(new ComponentName(SystemProperties.get("sys.bopd.package.name", "com.huawei.KoBackup"), SystemProperties.get("sys.bopd.activity.name", "com.huawei.KoBackup.EmergencyBackupActivity")));
    }

    private boolean interceptStartActivityForAppLock(Intent intent, ActivityOptions activityOptions) {
        if ((getUserId() != 0 && !UserManagerServiceEx.getUserInfo(getUserId()).isClonedProfile() && !UserManagerServiceEx.getUserInfo(getUserId()).isManagedProfile()) || this.mSupervisor.getKeyguardController().isKeyguardLocked() || !this.mSupervisor.getRootActivityContainerEx().isAppInLockList(intent.getComponent().getPackageName(), getUserId())) {
            return false;
        }
        setIntent(getNewIntent(intent, activityOptions));
        setCallingPid(getRealCallingPid());
        setCallingUid(getRealCallingUid());
        setResolvedType(null);
        if (getInTask() != null && !getInTask().isEmpty()) {
            getIntent().putExtra("android.intent.extra.TASK_ID", getInTask().getTaskId());
            setInTask(null);
        }
        setResolveInfo(this.mSupervisor.resolveIntent(getIntent(), getResolvedType(), getUserId(), 0, getRealCallingUid()));
        setActivityInfo(this.mSupervisor.resolveActivity(getIntent(), getResolveInfo(), getStartFlags(), (ProfilerInfoEx) null));
        return true;
    }

    public void setSourceRecord(ActivityRecordEx sourceRecord) {
        this.mSourceRecord = sourceRecord;
    }

    private Intent getNewIntent(Intent intent, ActivityOptions activityOptions) {
        Intent newIntent = new Intent("huawei.intent.action.APPLOCK_FRAMEWORK_MANAGER");
        newIntent.putExtra(EXTRA_USER_HANDLE_HWEX, getUserId());
        newIntent.setPackage("com.huawei.systemmanager");
        int flags = 41943040;
        if (IS_HW_MULTIWINDOW_SUPPORTED) {
            flags = 41943040 | 1073741824;
        }
        newIntent.setFlags(flags);
        newIntent.putExtra("android.intent.extra.PACKAGE_NAME", getActivityInfo().packageName);
        if ((intent.getHwFlags() & AwarenessConstants.MSDP_ENVIRONMENT_TYPE_WAY_OFFICE) != 0) {
            newIntent.putExtra("huawei.intent.extra.mode", 1);
            intent.setHwFlags(intent.getHwFlags() & -524289);
        }
        ActivityRecordEx activityRecordEx = this.mSourceRecord;
        boolean isLaunchFlag = false;
        boolean isSourceRecordEmpty = activityRecordEx == null || activityRecordEx.isEmpty();
        boolean isInTaskEmpty = getInTask() == null || getInTask().isEmpty();
        if (isSourceRecordEmpty && isInTaskEmpty) {
            getIntent().addFlags(268435456);
        }
        if (!isInTaskEmpty && getInTask().getChildCount() == 0 && (getIntent().getFlags() & 1048576) != 0) {
            Slog.i(TAG, "removed by app lock interceptor, taskId=" + getInTask().getTaskId());
            HwActivityTaskManager.removeTask(getInTask().getTaskId(), (IBinder) null, (String) null, true, "applock-interceptor");
        }
        getIntent().addHwFlags(2);
        IIntentSenderEx target = this.mService.getIntentSenderLocked(2, getCallingPackage(), Binder.getCallingUid(), getUserId(), (IBinder) null, (String) null, 0, new Intent[]{getIntent()}, new String[]{getResolvedType()}, 1207959552, (Bundle) null);
        setUserId(0);
        newIntent.putExtra("android.intent.extra.INTENT", new IntentSenderEx(target).getIntentSender());
        if ((isSourceRecordEmpty && isInTaskEmpty) || (!isSourceRecordEmpty && this.mSourceRecord.getActivityType() == 2)) {
            newIntent.addFlags(268435456);
        }
        if (activityOptions != null && WindowConfiguration.isHwMultiStackWindowingMode(activityOptions.getLaunchWindowingMode())) {
            newIntent.putExtra("options", activityOptions.toBundle());
            if ((intent.getFlags() & 268435456) != 0) {
                newIntent.addFlags(268435456);
            }
        }
        newIntent.putExtra("android.intent.extra.COMPONENT_NAME", intent.getComponent());
        ActivityStackEx topStack = this.mService.getRootActivityContainer().getTopDisplayFocusedStack();
        if (!IS_HW_MULTIWINDOW_SUPPORTED && topStack != null && !topStack.isActivityStackNull()) {
            newIntent.putExtra("windowMode", topStack.getWindowingMode());
        }
        if ("com.huawei.android.launcher".equals(getCallingPackage()) || (getIntent().getHwFlags() & AwarenessConstants.ANDROID_INTENT_TIME_SET_ACTION) != 0) {
            isLaunchFlag = true;
        }
        if (isLaunchFlag || !(topStack == null || topStack.getCurrentResumedActivity() == null || !"com.huawei.android.launcher".equals(topStack.getCurrentResumedActivity().getPackageName()))) {
            newIntent.putExtra(KEY_BLUR_BACKGROUND, true);
        }
        return newIntent;
    }

    /* access modifiers changed from: package-private */
    public IntentSender makeIntentSender(Intent intent) {
        return new IntentSender(this.mService.getIntentSenderLocked(2, getCallingPackage(), getCallingUid(), getUserId(), (IBinder) null, (String) null, 0, new Intent[]{intent}, new String[]{getResolvedType()}, 1409286144, (Bundle) null).getIntentSender());
    }
}
