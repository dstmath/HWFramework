package com.android.server.wm;

import android.content.ContentResolver;
import android.content.pm.ActivityInfoEx;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.HwMwUtils;
import android.view.animation.Animation;
import com.android.server.am.ActivityManagerServiceEx;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.content.res.ConfigurationAdapter;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.utils.Utils;
import java.util.List;

public class OrientationPolicy {
    private static final int FULLSCREEN_VISIBILITY_DELAY = 50;
    private static final int PARAM_INDEX_ONE = 1;
    private static final int PARAM_INDEX_TWO = 2;
    private static final int PARAM_INDEX_ZERO = 0;
    private static final String TAG = "HWMW_OrientationPolicy";
    private ActivityTaskManagerServiceEx mActivityTaskManager;
    private ActivityManagerServiceEx mAms;
    private HwMagicWinAmsPolicy mHwMagicWinAmsPolicy;
    private HwMagicWinManager mMwManager;
    private SettingsObserver mSettingsObserver;
    private int mUserRotation = 0;
    private int mUserRotationMode = WindowManagerPolicyEx.USER_ROTATION_FREE;

    public OrientationPolicy(ActivityManagerServiceEx ams, HwMagicWinManager manager, HwMagicWinAmsPolicy policy) {
        this.mAms = ams;
        this.mMwManager = manager;
        this.mHwMagicWinAmsPolicy = policy;
        this.mActivityTaskManager = ams.getActivityTaskManagerEx();
        this.mSettingsObserver = new SettingsObserver(new Handler());
        this.mSettingsObserver.observe();
    }

    public void resizeSpecialVideoInMagicWindowMode(IBinder token, int requestedOrientation, Bundle result) {
        ActivityRecordEx activityRecord = ActivityRecordEx.forToken(token);
        HwMagicContainer container = this.mMwManager.getContainer(activityRecord);
        String packageName = Utils.getPackageName(activityRecord);
        if (container != null && container.getHwMagicWinEnabled(packageName)) {
            if (ActivityInfoEx.isFixedOrientationPortrait(requestedOrientation) && container.isLocalContainer() && !isUserRotationLocked() && this.mAms.getWindowManagerServiceEx().isSensorPortraitRotation()) {
                SlogEx.i(TAG, "resizeSpecialVideoInMagicWindowMode already change to PORT");
            } else if (this.mHwMagicWinAmsPolicy.mMagicWinSplitMng.isPkgSpliteScreenMode(activityRecord, true)) {
                SlogEx.i(TAG, "resizeSpecialVideoInMagicWindowMode in PkgSpliteScreenMode, do nothing");
            } else if (!activityRecord.equalsActivityRecord(this.mHwMagicWinAmsPolicy.getTopActivity(container))) {
                SlogEx.i(TAG, "resizeSpecialVideoInMagicWindowMode, activity not top running");
            } else if (this.mHwMagicWinAmsPolicy.isShowDragBar(activityRecord)) {
                SlogEx.i(TAG, "resizeSpecialVideoInMagicWindowMode, activity is drag full mode");
            } else {
                SlogEx.i(TAG, "resizeSpecialVideoInMagicWindowMode,requested = " + requestedOrientation);
                if (ActivityInfoEx.isFixedOrientationLandscape(requestedOrientation)) {
                    activityRecord.setIsFullScreenVideoInLandscape(true);
                    SlogEx.i(TAG, "set mIsFullScreenVideoInLandscape to true, activity = " + activityRecord);
                }
                if (container.isFoldableDevice() && activityRecord.inHwMagicWindowingMode() && !activityRecord.isTopRunningActivity()) {
                    result.putBoolean("RESULT_REJECT_ORIENTATION", true);
                }
                if (container.isSupportFullScreenVideo(packageName) && activityRecord.inHwMagicWindowingMode()) {
                    resizeActivityInMagicWindowMode(activityRecord, requestedOrientation);
                }
                if (activityRecord.getWindowingMode() == 1 && !container.isFoldableDevice() && !this.mMwManager.getAmsPolicy().isPkgInLogoffStatus(activityRecord)) {
                    processVideoFullScreenToMw(activityRecord, requestedOrientation);
                }
            }
        }
    }

    private void processVideoFullScreenToMw(ActivityRecordEx activityRecord, int requestedOrientation) {
        if (activityRecord != null && activityRecord.getAppWindowTokenEx() != null) {
            String packageName = Utils.getPackageName(activityRecord);
            Rect bound = activityRecord.getBounds();
            boolean isExitMwOrientation = false;
            boolean isCurOrientationLand = bound.width() > bound.height();
            if (ActivityInfoEx.isFixedOrientationPortrait(requestedOrientation) || requestedOrientation == -1) {
                isExitMwOrientation = true;
            }
            if (isCurOrientationLand && isExitMwOrientation) {
                ActivityStackEx stack = activityRecord.getActivityStackEx();
                if (stack != null) {
                    stack.setWindowingMode((int) HwMagicWinAnimationScene.SCENE_EXIT_SLAVE_TO_SLAVE);
                    this.mHwMagicWinAmsPolicy.mModeSwitcher.setMagicActivityBound(stack, packageName, null);
                    HwMagicContainer container = this.mMwManager.getContainer(activityRecord);
                    if (container != null && container.isVirtualContainer()) {
                        container.getCameraRotation().updateCameraRotation(-1);
                    }
                }
                this.mHwMagicWinAmsPolicy.updateStackVisibility(activityRecord, true);
            }
        }
    }

    private void resizeActivityInMagicWindowMode(ActivityRecordEx activityRecord, int requestedOrientation) {
        HwMagicContainer container = this.mMwManager.getContainer(activityRecord);
        if (container != null) {
            boolean isInFullScreen = this.mMwManager.isFull(activityRecord);
            boolean isEnterMwOrientation = ActivityInfoEx.isFixedOrientationLandscape(requestedOrientation);
            boolean isExitMwOrientation = ActivityInfoEx.isFixedOrientationPortrait(requestedOrientation) || requestedOrientation == -1;
            if ((!isInFullScreen || this.mMwManager.isDragFullMode(activityRecord)) && isEnterMwOrientation) {
                this.mHwMagicWinAmsPolicy.setWindowBoundsLocked(activityRecord, container.getBounds(5, false));
                activityRecord.getMergedOverrideConfiguration().orientation = 2;
                this.mMwManager.getUIController().updateSplitBarVisibility(false, container.getDisplayId());
                Message msg = this.mMwManager.getHandler().obtainMessage(12);
                msg.obj = activityRecord.getShadow();
                this.mMwManager.getHandler().removeMessages(12);
                this.mMwManager.getHandler().sendMessageDelayed(msg, 50);
                container.getCameraRotation().updateCameraRotation(0);
            } else if (!isInFullScreen || !isExitMwOrientation || activityRecord.getLastBound() == null) {
                SlogEx.i(TAG, "do nothing, bounds = " + activityRecord.getRequestedOverrideBounds());
                return;
            } else {
                SlogEx.i(TAG, "resizeActivityInMagicWindowMode, wallpaper visible change to true");
                this.mMwManager.getUIController().updateMwWallpaperVisibility(true, container.getDisplayId(), true);
                this.mHwMagicWinAmsPolicy.setWindowBoundsLocked(activityRecord, activityRecord.getLastBound());
                activityRecord.getMergedOverrideConfiguration().orientation = 1;
                this.mMwManager.getUIController().updateSplitBarVisibility(true, container.getDisplayId());
                this.mHwMagicWinAmsPolicy.updateStackVisibility(activityRecord, true);
                container.getCameraRotation().updateCameraRotation(-1);
            }
            int temp = activityRecord.getInfo().configChanges;
            activityRecord.getInfo().configChanges |= 3328;
            activityRecord.ensureActivityConfiguration(0, true);
            activityRecord.getInfo().configChanges = temp;
        }
    }

    public void isDisableUpsideDownRotation(int rotation, Bundle result) {
        ActivityStackEx focusedStack;
        HwMagicContainer container = this.mMwManager.getLocalContainer();
        if (container != null && HwMwUtils.isInSuitableScene(true)) {
            if ((container.isFoldableDevice() || isUpsideDownRotation(rotation)) && getCurrentRotation() != rotation && (focusedStack = this.mHwMagicWinAmsPolicy.getFocusedTopStack(container)) != null) {
                String pkgName = Utils.getRealPkgName(focusedStack.getTopActivity());
                ActivityRecordEx topActivity = focusedStack.getTopActivity();
                boolean isRightWindowingMode = focusedStack.inHwMagicWindowingMode() || focusedStack.getWindowingMode() == 1;
                if (!container.isFoldableDevice() || topActivity == null || !isRightWindowingMode) {
                    if (isRightWindowingMode && container.getHwMagicWinEnabled(pkgName)) {
                        result.putBoolean("BUNDLE_IS_UPSIDEDOWN_ROTATION", true);
                    } else if (isRightWindowingMode && isLimitReversePortrait(focusedStack, container)) {
                        result.putBoolean("BUNDLE_IS_UPSIDEDOWN_ROTATION", true);
                    }
                } else if (container.getHwMagicWinEnabled(pkgName) && !topActivity.isFullScreenVideoInLandscape() && rotation != 0) {
                    result.putBoolean("BUNDLE_IS_UPSIDEDOWN_ROTATION", true);
                }
            }
        }
    }

    private boolean isLimitReversePortrait(ActivityStackEx focus, HwMagicContainer container) {
        synchronized (this.mActivityTaskManager.getGlobalLock()) {
            ActivityRecordEx topActivity = focus.getTopActivity();
            boolean isTopTrans = false;
            boolean z = true;
            if (topActivity != null) {
                AppWindowTokenExt token = topActivity.getAppWindowTokenEx();
                isTopTrans = token != null && !token.fillsParent() && token.getOrientation() == -1;
            }
            if (!isTopTrans) {
                return false;
            }
            ActivityDisplayEx activityDisplay = container.getActivityDisplay();
            if (activityDisplay == null) {
                return false;
            }
            ActivityStackEx nextStack = activityDisplay.getNextFocusableStack(focus, true);
            boolean isNextMagicAppVisible = false;
            if (nextStack != null) {
                ActivityRecordEx nextTop = nextStack.getTopActivity();
                if (!container.getHwMagicWinEnabled(Utils.getRealPkgName(nextTop))) {
                    return false;
                }
                if (nextTop == null || !nextTop.isVisible()) {
                    z = false;
                }
                isNextMagicAppVisible = z;
            }
            return isNextMagicAppVisible;
        }
    }

    public void changeOrientationForMultiWin(HwMagicContainer container, Configuration configuration, float density) {
        Rect bounds = ConfigurationAdapter.getBounds(configuration);
        configuration.screenHeightDp = (int) (((float) bounds.height()) / density);
        configuration.screenWidthDp = (int) (((float) bounds.width()) / density);
        ConfigurationAdapter.setAppBounds(configuration, bounds);
        ActivityRecordEx topActivity = this.mMwManager.getAmsPolicy().getTopActivity(container);
        int i = 1;
        boolean isFullScreenSetPort = this.mHwMagicWinAmsPolicy.isFullscreenWindow(container) && !this.mHwMagicWinAmsPolicy.isDefaultFullscreenActivity(container, topActivity) && !topActivity.isFullScreenVideoInLandscape();
        if ((this.mHwMagicWinAmsPolicy.isInHwDoubleWindow(container) || isFullScreenSetPort) && !container.getConfig().isSupportAppTaskSplitScreen(Utils.getPackageName(topActivity))) {
            configuration.orientation = 1;
            return;
        }
        if (configuration.screenWidthDp > configuration.screenHeightDp) {
            i = 2;
        }
        configuration.orientation = i;
    }

    public void checkMagicOrientation(ActivityRecordEx ar, Bundle result) {
        int i;
        HwMagicContainer container = this.mMwManager.getContainer(ar);
        if (ar != null && ar.getAppWindowTokenEx() != null && container != null && shouldOverrideOrientation(ar.getAppWindowTokenEx())) {
            int orientation = ar.getAppWindowTokenEx().getOrientation();
            boolean magicMode = ar.inHwMagicWindowingMode();
            String pkgName = Utils.getRealPkgName(ar);
            int magicRotation = checkMagicOrientationInner(container.getHwMagicWinEnabled(pkgName), orientation, ar, container);
            if (container.getConfig().isSupportMagicRotatingScreen(pkgName) && ActivityInfoEx.isFixedOrientationPortrait(orientation)) {
                if (this.mUserRotationMode != WindowManagerPolicyEx.USER_ROTATION_LOCKED) {
                    magicRotation = 4;
                }
                if (this.mUserRotationMode == WindowManagerPolicyEx.USER_ROTATION_LOCKED && ((i = this.mUserRotation) == 1 || i == 3)) {
                    magicRotation = 0;
                }
            }
            if (HwMwUtils.MAGICWIN_LOG_SWITCH) {
                SlogEx.i(TAG, "checkMagicRotation, pkgName = " + pkgName + ", orientation = " + orientation + ", magicMode = " + magicMode + ", mIsFullScreenVideoInLandscape = " + ar.isFullScreenVideoInLandscape() + ", return :" + magicRotation);
            }
            result.putInt("BUNDLE_RESULT_ORIENTATION", magicRotation);
        }
    }

    private boolean shouldOverrideOrientation(AppWindowTokenExt token) {
        return !(token.getSendingToBottom() || token.getDisplayContentEx().closingAppsContains(token)) && (token.getIsVisible() || token.getDisplayContentEx().openingAppsContains(token));
    }

    public boolean isDefaultLandOrientation(int orientation) {
        return ActivityInfoEx.isFixedOrientationLandscape(orientation);
    }

    private int checkMagicOrientationInner(boolean isAppSupportMagicWin, int orientation, ActivityRecordEx ar, HwMagicContainer container) {
        if (!isAppSupportMagicWin) {
            return -3;
        }
        if (isDefaultLandOrientation(orientation) && (!container.isFoldableDevice() || orientation != -1)) {
            return -3;
        }
        ar.setIsFullScreenVideoInLandscape(false);
        return getOrientation(container);
    }

    private boolean isUpsideDownRotation(int rotation) {
        return this.mAms.getWindowManagerServiceEx().isUpsideDownRotation(rotation);
    }

    private int getCurrentRotation() {
        return this.mAms.getWindowManagerServiceEx().getDefaultDisplayRotation();
    }

    private int getOrientation(HwMagicContainer container) {
        if (container.isFoldableDevice() || isUpsideDownRotation(getCurrentRotation())) {
            return 1;
        }
        return getOrientation();
    }

    public int getOrientation() {
        if (this.mUserRotationMode != WindowManagerPolicyEx.USER_ROTATION_LOCKED) {
            return 4;
        }
        int i = this.mUserRotation;
        if (i == 0 || i == 2) {
            return -3;
        }
        return -1;
    }

    public boolean isUserRotationLocked() {
        return this.mUserRotationMode == WindowManagerPolicyEx.USER_ROTATION_LOCKED;
    }

    public void updateSettings() {
        ContentResolver resolver = this.mActivityTaskManager.getContext().getContentResolver();
        this.mUserRotation = SettingsEx.System.getIntForUser(resolver, "user_rotation", 0, -2);
        this.mUserRotationMode = SettingsEx.System.getIntForUser(resolver, "accelerometer_rotation", 0, -2) != 0 ? WindowManagerPolicyEx.USER_ROTATION_FREE : WindowManagerPolicyEx.USER_ROTATION_LOCKED;
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void observe() {
            ContentResolver resolver = OrientationPolicy.this.mActivityTaskManager.getContext().getContentResolver();
            ContentResolverExt.registerContentObserver(resolver, Settings.System.getUriFor("accelerometer_rotation"), false, this, -1);
            ContentResolverExt.registerContentObserver(resolver, Settings.System.getUriFor("user_rotation"), false, this, -1);
            OrientationPolicy.this.updateSettings();
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            OrientationPolicy.this.updateSettings();
        }
    }

    public void getRotationAnima(List params) {
        boolean isRotation = false;
        ScreenRotationAnimationEx animation = new ScreenRotationAnimationEx();
        animation.resetScreenRotationAnimation(params.get(0));
        Animation[] results = new Animation[2];
        Integer[] param = {Integer.valueOf(animation.getOriginalRotation()), Integer.valueOf(((Integer) params.get(1)).intValue()), Integer.valueOf(animation.getOriginalHeight()), Integer.valueOf(((Integer) params.get(2)).intValue())};
        HwMagicContainer container = this.mMwManager.getContainer(0);
        if (container != null) {
            if (container.getAnimation() != null && !this.mAms.getWindowManagerServiceEx().isKeyguardLocked()) {
                isRotation = container.getAnimation().getRotationAnim(param, results);
                container.getAnimation().resetParamsForRotation();
            }
            if (isRotation) {
                animation.setHwMagicWindow(true);
                animation.setRotateExitAnimation(results[0]);
                animation.setRotateEnterAnimation(results[1]);
            }
        }
    }

    public void updateRequestedOrientation() {
        ActivityRecordEx topActivity;
        HwMagicContainer container = this.mMwManager.getVirtualContainer();
        if (container != null && (topActivity = this.mHwMagicWinAmsPolicy.getTopActivity(container)) != null && !topActivity.inHwMagicWindowingMode()) {
            container.getCameraRotation().updateCameraRotation(0);
        }
    }
}
