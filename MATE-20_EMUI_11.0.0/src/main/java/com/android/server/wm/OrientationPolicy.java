package com.android.server.wm;

import android.content.pm.ActivityInfoEx;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.HwMwUtils;
import com.android.server.am.ActivityManagerServiceEx;
import com.huawei.android.content.res.ConfigurationAdapter;
import com.huawei.android.util.SlogEx;
import com.huawei.server.magicwin.HwMagicWinAnimationScene;
import com.huawei.server.utils.Utils;

public class OrientationPolicy {
    private static final int FULLSCREEN_VISIBILITY_DELAY = 50;
    private static final String TAG = "HWMW_OrientationPolicy";
    private ActivityTaskManagerServiceEx mActivityTaskManager;
    private ActivityManagerServiceEx mAms;
    private HwMagicWinAmsPolicy mHwMagicWinAmsPolicy;
    private HwMagicWinManager mMwManager;

    public OrientationPolicy(ActivityManagerServiceEx ams, HwMagicWinManager manager, HwMagicWinAmsPolicy policy) {
        this.mAms = ams;
        this.mMwManager = manager;
        this.mHwMagicWinAmsPolicy = policy;
        this.mActivityTaskManager = ams.getActivityTaskManagerEx();
    }

    public void resizeSpecialVideoInMagicWindowMode(IBinder token, int requestedOrientation, Bundle result) {
        ActivityRecordEx activityRecord = ActivityRecordEx.forToken(token);
        HwMagicContainer container = this.mMwManager.getContainer(activityRecord);
        String packageName = Utils.getPackageName(activityRecord);
        if (container != null && container.getHwMagicWinEnabled(packageName)) {
            if (ActivityInfoEx.isFixedOrientationPortrait(requestedOrientation) && container.isLocalContainer() && !this.mMwManager.getWmsPolicy().isUserRotationLocked() && this.mAms.getWindowManagerServiceEx().isSensorPortraitRotation()) {
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
        HwMagicContainer container = this.mMwManager.getContainer(ar);
        if (ar != null && ar.getAppWindowTokenEx() != null && container != null && shouldOverrideOrientation(ar.getAppWindowTokenEx())) {
            int orientation = ar.getAppWindowTokenEx().getOrientation();
            boolean magicMode = ar.inHwMagicWindowingMode();
            String pkgName = Utils.getRealPkgName(ar);
            int magicRotation = checkMagicOrientationInner(container.getHwMagicWinEnabled(pkgName), orientation, ar, container);
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

    private int getOrientation(HwMagicContainer container) {
        if (container.isFoldableDevice() || isUpsideDownRotation(getCurrentRotation())) {
            return 1;
        }
        return this.mMwManager.getWmsPolicy().getOrientation();
    }

    private boolean isUpsideDownRotation(int rotation) {
        return this.mAms.getWindowManagerServiceEx().isUpsideDownRotation(rotation);
    }

    private int getCurrentRotation() {
        return this.mAms.getWindowManagerServiceEx().getDefaultDisplayRotation();
    }
}
