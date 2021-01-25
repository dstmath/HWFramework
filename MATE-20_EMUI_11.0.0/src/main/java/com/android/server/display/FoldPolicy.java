package com.android.server.display;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayViewport;
import android.hardware.display.HwFoldScreenState;
import android.hardware.input.InputManagerInternal;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Slog;
import android.view.DisplayCutout;
import com.android.server.LocalServices;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import huawei.android.hwutil.HwFullScreenDisplay;
import java.util.Optional;

public abstract class FoldPolicy {
    public static final int NAV_BAR_HEIGHT = 122;
    public static final String TAG = "FoldPolicy";
    protected Context mContext;
    public int mDisplayMode;
    private DisplayManagerInternal mDm;
    protected volatile boolean mFoldStateChanging;
    protected HwFoldScreenManagerInternal mFsm;
    public Rect mFullDispRect = new Rect();
    private InputManagerInternal mInputManager;
    public Rect mMainDispRect = new Rect();
    private WindowManagerInternal mWm;

    public interface DisplayModeChangeCallback {
        void onDisplayModeChangeTimeout(int i, int i2, boolean z);
    }

    public interface ScreenUnfreezingCallback {
        void onScreenUnfreezing();
    }

    public abstract void adjustViewportFrame(DisplayViewport displayViewport, Rect rect, Rect rect2);

    public abstract Rect getDispRect(int i);

    public abstract Rect getScreenDispRect(int i);

    public FoldPolicy(Context context) {
        this.mContext = context;
        this.mDisplayMode = 0;
        this.mFullDispRect.set(0, 0, HwFoldScreenState.SCREEN_FOLD_FULL_WIDTH, HwFoldScreenState.SCREEN_FOLD_FULL_HEIGHT);
    }

    public int getDisplayMode() {
        return this.mDisplayMode;
    }

    public int setDisplayMode(int mode) {
        boolean isScreenOn = ((PowerManager) this.mContext.getSystemService("power")).isScreenOn();
        int oldDisplayMode = this.mDisplayMode;
        Rect dispRegion = getDispRect(mode);
        if (dispRegion != null) {
            int h = dispRegion.height();
            HwFullScreenDisplay.setFullScreenData(h, h - 122, dispRegion.width());
            setRealDisplayMode(mode);
        }
        if (this.mFsm == null) {
            this.mFsm = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        }
        HwFoldScreenManagerInternal hwFoldScreenManagerInternal = this.mFsm;
        if (hwFoldScreenManagerInternal != null) {
            hwFoldScreenManagerInternal.onSetFoldDisplayModeFinished(this.mDisplayMode, oldDisplayMode);
        }
        if (isScreenOn) {
            return 0;
        }
        if (this.mDm == null) {
            this.mDm = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
        }
        HwFoldScreenManagerInternal hwFoldScreenManagerInternal2 = this.mFsm;
        if (hwFoldScreenManagerInternal2 == null || !hwFoldScreenManagerInternal2.getInfoDrawWindow()) {
            return 0;
        }
        Slog.d(TAG, "requestScreenState");
        this.mDm.requestScreenState();
        this.mFsm.resetInfoDrawWindow();
        return 0;
    }

    private boolean setRealDisplayMode(int mode) {
        if (mode == this.mDisplayMode) {
            Slog.d(TAG, "Current mode don't change, return!");
            if (this.mDm == null) {
                this.mDm = (DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class);
            }
            this.mDm.resetDisplayDelay();
            handleResumeDispModeChange();
            return false;
        }
        Rect screenRect = getDispRect(mode);
        if (this.mWm == null) {
            this.mWm = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        }
        if (!(this.mWm == null || screenRect == null || screenRect.isEmpty())) {
            Slog.d(TAG, "setRealDisplayMode new mode:" + mode + ", old mode:" + this.mDisplayMode + " screenRect=" + screenRect);
            Bundle foldScreenInfo = new Bundle();
            foldScreenInfo.putBoolean("isFold", true);
            foldScreenInfo.putBoolean("useCustomFoldAnim", HwFoldScreenState.isOutFoldDevice());
            foldScreenInfo.putInt("fromFoldMode", this.mDisplayMode);
            foldScreenInfo.putInt("toFoldMode", mode);
            this.mDisplayMode = mode;
            this.mWm.setForcedDisplaySizeAndDensity(true, 0, screenRect.width(), screenRect.height(), 0, foldScreenInfo);
            if (this.mInputManager == null) {
                this.mInputManager = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
            }
            InputManagerInternal inputManagerInternal = this.mInputManager;
            if (inputManagerInternal != null) {
                inputManagerInternal.setDisplayMode(mode, HwFoldScreenState.SCREEN_FOLD_SUB_WIDTH, HwFoldScreenState.SCREEN_FOLD_MAIN_WIDTH, HwFoldScreenState.SCREEN_FOLD_FULL_HEIGHT);
                Slog.d(TAG, "mIM.setDisplayMode !");
            }
        }
        return true;
    }

    private void handleResumeDispModeChange() {
        if (this.mFsm == null) {
            this.mFsm = (HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class);
        }
        if (this.mFsm != null) {
            Slog.d(TAG, "handleResumeDispModeChange from FSSImpl");
            this.mFsm.resumeDispModeChange();
            WindowManagerInternal windowManagerInternal = this.mWm;
            if (windowManagerInternal != null) {
                windowManagerInternal.unFreezeFoldRotation();
            }
        }
    }

    public int getDisplayRotation() {
        return 0;
    }

    public boolean getDisplayCutoutFlag(Resources res) {
        return res.getBoolean(17891480);
    }

    public Optional<DisplayCutout> getDisplayCutoutInfo(Resources res, int width, int height) {
        return Optional.ofNullable(DisplayCutout.fromResourcesRectApproximation(res, width, height));
    }

    public int onPreDisplayModeChange(int newDisplayMode) {
        return 0;
    }

    public int onPostDisplayModeChange(int newDisplayMode) {
        return 0;
    }

    public void setDisplayStatus(IBinder token, int displayMode, int foldState, DisplayModeChangeCallback callback) {
        setDisplayMode(displayMode);
    }

    public int setCurrentBrightnessOff() {
        return 0;
    }

    public boolean isFoldStateChanging() {
        return this.mFoldStateChanging;
    }
}
