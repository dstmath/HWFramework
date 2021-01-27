package com.android.server.wm;

import android.app.ActivityOptions;
import android.graphics.Rect;
import android.view.MotionEvent;
import com.android.server.am.PointerEventListenerEx;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.util.SlogEx;
import com.huawei.server.camera.CameraRotationBase;
import com.huawei.server.magicwin.DeviceAttribute;
import com.huawei.server.magicwin.HwMagicWinAnimation;
import com.huawei.server.magicwin.HwMagicWindowConfig;
import com.huawei.server.utils.SharedParameters;
import com.huawei.server.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class HwMagicContainer implements DeviceAttribute {
    private static final String TAG = "HWMW_HwMagicContainer";
    protected ActivityOptions mActivityOptions;
    protected HwMagicWinAnimation mAnimation = null;
    protected CameraRotationBase mCameraRotation = null;
    protected HwMagicWindowConfig mConfig = null;
    protected int mDisplayId = -1;
    protected SharedParameters mParameters;

    public HwMagicWindowConfig getConfig() {
        return this.mConfig;
    }

    @Override // com.huawei.server.magicwin.DeviceAttribute
    public boolean isLocalContainer() {
        return false;
    }

    @Override // com.huawei.server.magicwin.DeviceAttribute
    public boolean isVirtualContainer() {
        return false;
    }

    public int getType() {
        return -1;
    }

    public ActivityDisplayEx getActivityDisplay() {
        return this.mParameters.getAms().getActivityTaskManagerEx().getRootActivityContainer().getDefaultDisplay();
    }

    public boolean isScaled(String pkgName) {
        return this.mConfig.isScaled(pkgName);
    }

    public float getRatio(String pkgName) {
        return this.mConfig.getRatio(pkgName);
    }

    public CameraRotationBase getCameraRotation() {
        return this.mCameraRotation;
    }

    public Rect getBounds(int position, String pkgName) {
        return this.mConfig.getBounds(position, pkgName);
    }

    public Rect getBounds(int position, boolean isScaled) {
        return this.mConfig.getBounds(position, isScaled);
    }

    public boolean checkPosition(Rect bound, int pos) {
        return getBoundsPosition(bound) == pos;
    }

    public int getBoundsPosition(Rect bounds) {
        if (bounds == null || this.mConfig.getBounds(5, false).equals(bounds) || this.mParameters.getMwWinManager().getAmsPolicy().mDefaultFullScreenBounds.equals(bounds)) {
            String pkgName = this.mParameters.getMwWinManager().getAmsPolicy().getFocusedStackPackageName(this);
            if (bounds == null || !this.mConfig.isSupportAppTaskSplitScreen(pkgName) || !isFoldableDevice()) {
                return 5;
            }
            return 3;
        }
        int posSplite = this.mConfig.getBoundPosition(bounds, 0);
        if (posSplite != 0) {
            return posSplite;
        }
        return 0;
    }

    public int getAppSupportMode(String packageName) {
        return this.mConfig.getWindowMode(packageName);
    }

    public boolean isAppSupportMagicWin(String packageName) {
        return getAppSupportMode(packageName) >= 0;
    }

    public boolean isSupportAnAnMode(String pkg) {
        return this.mConfig.getWindowMode(pkg) == 2;
    }

    public boolean isReLaunchWhenResize(String pkg) {
        return this.mConfig.isReLaunchWhenResize(pkg);
    }

    public boolean isSupportOpenMode(String pkg) {
        return this.mConfig.getWindowMode(pkg) == 3;
    }

    public boolean isSupportFullScreenVideo(String packageName) {
        return this.mConfig.isVideoFullscreen(packageName);
    }

    public boolean getHwMagicWinEnabled(String pkg) {
        return this.mConfig.getHwMagicWinEnabled(pkg);
    }

    public boolean isHomePage(String pkg, String component) {
        String[] homes;
        if (component == null || component.isEmpty() || (homes = this.mConfig.getHomes(pkg)) == null) {
            return false;
        }
        for (String home : homes) {
            if (!(home == null || home.isEmpty() || !home.equals(component))) {
                return true;
            }
        }
        return false;
    }

    public boolean isNeedDetect(String pkg) {
        return this.mConfig.getHwMagicWinEnabled(pkg) && !isSupportOpenMode(pkg) && this.mConfig.isNeedDetect(pkg);
    }

    public HwMagicWinAnimation getAnimation() {
        return this.mAnimation;
    }

    public void updateDisplayMetrics(int height, int width) {
    }

    @Override // com.huawei.server.magicwin.DeviceAttribute
    public boolean isPadDevice() {
        return false;
    }

    @Override // com.huawei.server.magicwin.DeviceAttribute
    public boolean isFoldableDevice() {
        return false;
    }

    @Override // com.huawei.server.magicwin.DeviceAttribute
    public boolean isInFoldedStatus() {
        return isFoldableDevice() && HwFoldScreenManagerEx.getDisplayMode() != 1;
    }

    public int getOrientation() {
        return this.mParameters.getContext().getResources().getConfiguration().orientation;
    }

    public boolean isInMagicWinOrientation() {
        return isFoldableDevice() ? !isInFoldedStatus() && getOrientation() == 1 : getOrientation() == 2;
    }

    public void calcHwSplitStackBounds() {
        Map<Integer, List<Rect>> modeBounds = new HashMap<>();
        modeBounds.put(0, calcHwSplitStackBounds(0));
        modeBounds.put(1, calcHwSplitStackBounds(1));
        modeBounds.put(2, calcHwSplitStackBounds(2));
        calcHwSplitStackBounds(5);
        calcHwSplitStackBounds(6);
        Rect leftRect = modeBounds.get(0).get(0);
        Rect rightRect = modeBounds.get(0).get(1);
        if (leftRect == null || rightRect == null || leftRect.top != rightRect.top) {
            SlogEx.e(TAG, "calc split bounds error.");
        } else {
            getConfig().updateAppBoundsFromMode(modeBounds);
        }
    }

    private List<Rect> calcHwSplitStackBounds(int splitRatio) {
        List<Rect> modeBounds = new ArrayList<>();
        Rect leftBound = new Rect();
        Rect rightBound = new Rect();
        getConfig().calcHwSplitStackBounds(isFoldableDevice(), splitRatio, leftBound, rightBound);
        Rect[] newBounds = getConfig().adjustBoundsForResize(leftBound, rightBound);
        if (newBounds == null || newBounds.length <= 1) {
            modeBounds.add(leftBound);
            modeBounds.add(rightBound);
        } else {
            modeBounds.add(newBounds[0]);
            modeBounds.add(newBounds[1]);
        }
        return modeBounds;
    }

    public void registerEventListener() {
        this.mParameters.getWms().registerPointerEventListener(new PointerEventListenerEx() {
            /* class com.android.server.wm.HwMagicContainer.AnonymousClass1 */

            public void onPointerEvent(MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == 0) {
                    synchronized (HwMagicContainer.this.mParameters.getWms().getGlobalLock()) {
                        Optional.ofNullable(HwMagicContainer.this.getActivityDisplay()).map($$Lambda$HwMagicContainer$1$yBxzhEpuwJ2IQ5aOCt7nz4QLT4.INSTANCE).map($$Lambda$HwMagicContainer$1$4J_3K6vxK9lrI6Zox0X_jkLKrA.INSTANCE).ifPresent($$Lambda$HwMagicContainer$1$zTkm1QmS4NNGJrlEvyZE9jls0E.INSTANCE);
                    }
                }
            }
        }, getDisplayId());
    }

    public int getDisplayId() {
        return this.mDisplayId;
    }

    public void attachDisplayId(int displayId) {
        this.mDisplayId = displayId;
    }

    public void updateActivityOptions(ActivityRecordEx focus, ActivityRecordEx next, ActivityOptions options) {
        ActivityOptions activityOptions;
        if (getConfig().isSupportAppTaskSplitScreen(Utils.getPackageName(next)) && options != null) {
            HwMagicWinAmsPolicy amsPolicy = this.mParameters.getMwWinManager().getAmsPolicy();
            if (focus.isActivityTypeHome() && amsPolicy.isMainActivity(this, next)) {
                this.mActivityOptions = options;
            }
            if (focus.inHwMagicWindowingMode() && amsPolicy.isMainActivity(this, focus) && amsPolicy.isRelatedActivity(this, next) && (activityOptions = this.mActivityOptions) != null) {
                options.update(activityOptions);
                this.mActivityOptions = null;
            }
        }
    }
}
