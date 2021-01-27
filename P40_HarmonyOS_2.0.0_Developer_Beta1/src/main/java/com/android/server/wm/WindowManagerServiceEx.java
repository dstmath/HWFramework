package com.android.server.wm;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import com.android.server.am.PointerEventListenerEx;
import com.android.server.display.FoldPolicy;
import com.android.server.input.InputManagerServiceAospEx;
import com.android.server.policy.WindowManagerPolicyEx;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class WindowManagerServiceEx {
    public static final String IS_FOLD_KEY = "isFold";
    public static final int LAZY_MODE_LEFT = 1;
    public static final int LAZY_MODE_NOMAL = 0;
    public static final int LAZY_MODE_RIGHT = 2;
    public static final float LAZY_MODE_SCALE = 0.75f;
    public static final String USE_CUSTOM_FOLD_ANIM_KEY = "useCustomFoldAnim";
    private ActivityTaskManagerServiceEx mAtmsEx;
    private RootWindowContainerEx mRootWindowContainerEx;
    private WindowManagerService mWms;

    public WindowManagerServiceEx() {
    }

    public WindowManagerServiceEx(WindowManagerService windowManagerService) {
        this.mWms = windowManagerService;
    }

    public void setWindowManagerService(WindowManagerService wms) {
        this.mWms = wms;
    }

    public WindowManagerService getWindowManagerService() {
        return this.mWms;
    }

    public WindowManagerPolicyEx getWindowManagerPolicyEx() {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService == null || windowManagerService.getPolicy() == null) {
            return null;
        }
        WindowManagerPolicyEx windowManagerPolicyEx = new WindowManagerPolicyEx();
        windowManagerPolicyEx.setWindowManagerPolicy(this.mWms.getPolicy());
        return windowManagerPolicyEx;
    }

    public void registerPointerEventListener(PointerEventListenerEx listener, int displayId) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null && listener != null) {
            windowManagerService.registerPointerEventListener(listener.getPointerEventListenerBridge(), displayId);
        }
    }

    public void unregisterPointerEventListener(PointerEventListenerEx listener, int displayId) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null && listener != null) {
            windowManagerService.unregisterPointerEventListener(listener.getPointerEventListenerBridge(), displayId);
        }
    }

    public int getDefaultDisplayRotation() {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            return windowManagerService.getDefaultDisplayRotation();
        }
        return 0;
    }

    public boolean isUpsideDownRotation(int rotation) {
        DisplayRotation displayRotation;
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService == null || windowManagerService.getDefaultDisplayContentLocked() == null || (displayRotation = this.mWms.getDefaultDisplayContentLocked().getDisplayRotation()) == null || displayRotation.getUpsideDownRotation() != rotation) {
            return false;
        }
        return true;
    }

    public boolean isSensorPortraitRotation() {
        DisplayRotation displayRotation;
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService == null || windowManagerService.getDefaultDisplayContentLocked() == null || (displayRotation = this.mWms.getDefaultDisplayContentLocked().getDisplayRotation()) == null || displayRotation.getOrientationListener() == null || displayRotation.getOrientationListener().getProposedRotation() != displayRotation.getPortraitRotation()) {
            return false;
        }
        return true;
    }

    public void stopFreezingScreen() {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.stopFreezingScreen();
        }
    }

    public void stopFreezingScreen(FoldPolicy.ScreenUnfreezingCallback callback) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.stopFreezingScreen(callback);
        }
    }

    public Object getGlobalLock() {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService == null) {
            return new Object();
        }
        return windowManagerService.mGlobalLock;
    }

    public RootWindowContainerEx getRootWindowContainerEx() {
        WindowManagerService windowManagerService;
        if (!(this.mRootWindowContainerEx != null || (windowManagerService = this.mWms) == null || windowManagerService.getRoot() == null)) {
            this.mRootWindowContainerEx = new RootWindowContainerEx();
            this.mRootWindowContainerEx.setRootWindowContainer(this.mWms.getRoot());
        }
        return this.mRootWindowContainerEx;
    }

    public TaskStackEx getImeFocusStackLocked() {
        if (this.mWms == null) {
            return null;
        }
        TaskStackEx taskStackEx = new TaskStackEx();
        taskStackEx.setTaskStack(this.mWms.getImeFocusStackLocked());
        return taskStackEx;
    }

    public void deferSurfaceLayout() {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.deferSurfaceLayout();
        }
    }

    public void continueSurfaceLayout() {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.continueSurfaceLayout();
        }
    }

    public boolean isKeyguardLocked() {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            return windowManagerService.isKeyguardLocked();
        }
        return false;
    }

    public float getTransitionAnimationScaleLocked() {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            return windowManagerService.getTransitionAnimationScaleLocked();
        }
        return 1.0f;
    }

    public void setMagicWindowMoveInterpolator(Interpolator interpolator) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.getWindowManagerServiceEx().setMagicWindowMoveInterpolator(interpolator);
        }
    }

    public void setMagicWindowAnimation(boolean isStart, Animation enter, Animation exit) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.getWindowManagerServiceEx().setMagicWindowAnimation(isStart, enter, exit);
        }
    }

    public WindowManagerService getService() {
        return this.mWms;
    }

    public void freezeOrThawRotation(int rotation) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.freezeOrThawRotation(rotation);
        }
    }

    public void setLazyMode(int lazyMode, boolean hintShowing, String windowName) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.setLazyMode(lazyMode, hintShowing, windowName);
        }
    }

    public void setAnimatorLazyMode(boolean isLazying) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.setAnimatorLazyMode(isLazying);
        }
    }

    public boolean postEx(Runnable runnable) {
        return this.mWms.getWindowMangerServiceHandler().post(runnable);
    }

    public ActivityTaskManagerServiceEx getAtmServiceEx() {
        WindowManagerService windowManagerService;
        if (!(this.mAtmsEx != null || (windowManagerService = this.mWms) == null || windowManagerService.mAtmService == null)) {
            this.mAtmsEx = new ActivityTaskManagerServiceEx();
            this.mAtmsEx.setActivityTaskManagerService(this.mWms.mAtmService);
        }
        return this.mAtmsEx;
    }

    public int dipToPixel(int dip, DisplayMetrics displayMetrics) {
        WindowManagerService windowManagerService = this.mWms;
        return WindowManagerService.dipToPixel(dip, displayMetrics);
    }

    public int getBaseDisplayDensity(int displayId) {
        return this.mWms.getBaseDisplayDensity(displayId);
    }

    public void getBaseDisplaySize(int displayId, Point size) {
        this.mWms.getBaseDisplaySize(displayId, size);
    }

    public InputManagerServiceAospEx getInputManagerServiceEx() {
        InputManagerServiceAospEx inputManagerServiceAospEx = new InputManagerServiceAospEx();
        inputManagerServiceAospEx.setInputManagerService(this.mWms.mInputManager);
        return inputManagerServiceAospEx;
    }

    public Context getContext() {
        return this.mWms.mContext;
    }

    public int getCurrentUserId() {
        return this.mWms.mCurrentUserId;
    }

    public void updateRotation(boolean isAlwaysSendConfiguration, boolean isForceRelayout) {
        this.mWms.updateRotation(isAlwaysSendConfiguration, isForceRelayout);
    }

    public void updateRotationUnchecked(boolean isAlwaysSendConfiguration, boolean isForceRelayout) {
        this.mWms.updateRotationUnchecked(isAlwaysSendConfiguration, isForceRelayout);
    }

    public WindowManagerPolicyEx getPolicyEx() {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService == null || windowManagerService.getPolicy() == null) {
            return null;
        }
        WindowManagerPolicyEx wmPolicyEx = new WindowManagerPolicyEx();
        wmPolicyEx.setWindowManagerPolicy(this.mWms.getPolicy());
        return wmPolicyEx;
    }

    public void wakeDisplayModeChange(boolean isChange) {
        this.mWms.wakeDisplayModeChange(isChange);
    }

    public int getFoldDisplayMode() {
        return this.mWms.getFoldDisplayMode();
    }

    public IBinder getDisplayToken(int displayId) {
        return this.mWms.mDisplayManagerInternal.getDisplayToken(displayId);
    }

    public int getLazyMode() {
        return this.mWms.getLazyMode();
    }

    public boolean isInSubFoldScaleMode() {
        return this.mWms.isInSubFoldScaleMode();
    }

    public float getSubFoldModeScale() {
        return this.mWms.mSubFoldModeScale;
    }

    public float[] getTmpFloats() {
        return this.mWms.mTmpFloats;
    }

    public boolean isLimitedAlphaCompositing() {
        return this.mWms.mLimitedAlphaCompositing;
    }

    public boolean isKeyguardLockedAndOccluded() {
        return this.mWms.mPolicy.isKeyguardLockedAndOccluded();
    }

    public WindowAnimator getWindowAnimator() {
        return this.mWms.mAnimator;
    }

    public static boolean getLocalLogV() {
        return false;
    }

    public DisplayContentEx getDefaultDisplayContentLocked() {
        DisplayContent displayContent = this.mWms.getDefaultDisplayContentLocked();
        if (displayContent == null) {
            return null;
        }
        return new DisplayContentEx(displayContent);
    }

    public boolean isPendingLock() {
        return this.mWms.isPendingLock();
    }

    public Point getOriginPointForLazyMode(float scale, int lazyModeType) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            return windowManagerService.getOriginPointForLazyMode(scale, lazyModeType);
        }
        return null;
    }

    public Handler getHwHandler() {
        return this.mWms.getWindowManagerServiceEx().getHwHandler();
    }

    public static WindowManagerServiceEx getInstance() {
        WindowManagerService service = ServiceManager.getService("window");
        if (service == null) {
            return null;
        }
        WindowManagerServiceEx serviceEx = new WindowManagerServiceEx();
        serviceEx.setWindowManagerService(service);
        return serviceEx;
    }

    public void startFreezingScreen(int exitAnim, int enterAnim) {
        WindowManagerService windowManagerService = this.mWms;
        if (windowManagerService != null) {
            windowManagerService.startFreezingScreen(exitAnim, enterAnim);
        }
    }

    public Handler getAnimationHandler() {
        return this.mWms.getAnimationHandler();
    }

    public boolean isInDisplayFrozen() {
        return this.mWms.isInDisplayFrozen();
    }

    public boolean isSuperWallpaper() {
        return this.mWms.isInWallpaperEffect();
    }
}
