package com.android.server.wm;

import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.util.HwMwUtils;
import android.util.Slog;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.animation.Animation;

public final class HwWindowStateEx extends WindowStateBridgeEx {
    private static final int INPUT_METHOD_WINDOW_THRESHOLD_HEIGHT = 50;
    private static final String PERMISSION_CONTROLLER_PACKAGE = "com.android.permissioncontroller";
    private static final float SCALE_MODULUS = 0.5f;
    private static final String TAG = "HwWindowStateEx";
    private final Rect mDimBoundsRect = new Rect();
    private int mInputMethodWindowHeight = 0;
    private int mInputMethodWindowTop = 0;
    final WindowManagerServiceEx mService;
    final WindowStateEx mWinState;

    public HwWindowStateEx(WindowManagerServiceEx service, WindowStateEx windowState) {
        super(service, windowState);
        this.mService = service;
        this.mWinState = windowState;
    }

    public Rect adjustImePosForFreeform(Rect contentFrame, Rect containingFrame) {
        if (!HwFreeFormUtils.isFreeFormEnable() || contentFrame == null || containingFrame == null) {
            return containingFrame;
        }
        int offsetY = Math.max(contentFrame.bottom - containingFrame.bottom, contentFrame.top - containingFrame.top);
        Rect taskBounds = new Rect();
        if (offsetY < 0) {
            this.mWinState.getTaskEx().getBounds(taskBounds);
            taskBounds.offset(0, offsetY);
            this.mWinState.getTaskEx().setBounds(taskBounds);
            containingFrame.offset(0, offsetY);
        }
        return containingFrame;
    }

    public boolean isInHwFreeFormWorkspace() {
        if (!HwFreeFormUtils.isFreeFormEnable()) {
            return false;
        }
        return this.mWinState.inFreeformWindowingMode();
    }

    public boolean isInHideCaptionList() {
        if (!isInHwFreeFormWorkspace() || this.mWinState.getDisplayContentEx().getConfiguration().orientation == 2) {
            return false;
        }
        String windowTitle = this.mWinState.toString();
        for (String str : HwFreeFormUtils.sHideCaptionActivity) {
            if (windowTitle.contains(str)) {
                return true;
            }
        }
        return false;
    }

    public int adjustTopForFreeform(Rect frame, Rect limitFrame, int minVisibleHeight) {
        if (frame == null || limitFrame == null) {
            return 0;
        }
        int top = Math.min(frame.top, limitFrame.bottom - minVisibleHeight);
        if (!isInHwFreeFormWorkspace() || isInHideCaptionList()) {
            return Math.max(limitFrame.top, top);
        }
        return top;
    }

    public void createMagicWindowDimmer() {
        ActivityRecordEx actRecordEx;
        if (HwMwUtils.ENABLED && this.mWinState.isAppWindowTokenNotNull() && this.mWinState.isParentNotNull() && !PERMISSION_CONTROLLER_PACKAGE.equals(this.mWinState.getAttrs().packageName) && (actRecordEx = this.mWinState.getAppWindowTokenEx().getActivityRecordEx()) != null && !HwMwUtils.performPolicy(108, new Object[]{actRecordEx.getShadow()}).getBoolean("ACTIVITY_FULLSCREEN", false)) {
            new HwMagicWindowDimmerEx(this.mWinState).setHwWindowStateExMwDimmer();
        }
    }

    public void destoryMagicWindowDimmer() {
        if (HwMwUtils.ENABLED && this.mWinState.getMwDimmer() != null) {
            new HwMagicWindowDimmerEx(this.mWinState).destoryMagicWindowDimmer();
        }
    }

    public boolean updateMagicWindowDimmer() {
        DimmerEx dimmer = this.mWinState.getMwDimmer();
        DimmerEx taskDimmer = this.mWinState.getDimmer();
        if (dimmer == null || taskDimmer == null) {
            return false;
        }
        boolean isDimming = false;
        dimmer.resetDimStates();
        if ((this.mWinState.getAttrs().flags & 2) != 0 && this.mWinState.isVisibleNow() && !this.mWinState.isHidden()) {
            isDimming = true;
            SurfaceControl.Transaction pendingTransaction = this.mWinState.getPendingTransaction();
            WindowStateEx windowStateEx = this.mWinState;
            dimmer.dimBelow(pendingTransaction, windowStateEx, windowStateEx.getAttrs().dimAmount);
        }
        this.mDimBoundsRect.set(this.mWinState.getWindowFrames().getDisplayFrame());
        Rect rect = this.mDimBoundsRect;
        rect.right = rect.left + ((int) ((((float) this.mDimBoundsRect.width()) * this.mWinState.getMwUsedScaleFactor() * this.mWinState.getGlobalScale()) + SCALE_MODULUS));
        Rect rect2 = this.mDimBoundsRect;
        rect2.bottom = rect2.top + ((int) ((((float) this.mDimBoundsRect.height()) * this.mWinState.getMwUsedScaleFactor() * this.mWinState.getGlobalScale()) + SCALE_MODULUS));
        this.mDimBoundsRect.offsetTo(0, 0);
        if (dimmer.updateDims(this.mWinState.getPendingTransaction(), this.mDimBoundsRect)) {
            this.mWinState.scheduleAnimation();
        }
        return isDimming;
    }

    public void stopMagicWindowDimmer() {
        WindowStateEx windowStateEx;
        if (HwMwUtils.ENABLED && (windowStateEx = this.mWinState) != null && windowStateEx.getMwDimmer() != null) {
            this.mWinState.getMwDimmer().stopDim(this.mWinState.getPendingTransaction());
        }
    }

    public boolean isNeedMoveAnimation(WindowStateEx windowState) {
        if (windowState == null || windowState.getAppWindowTokenEx() == null || windowState.getAppWindowTokenEx().isAppWindowTokenNull() || !windowState.inHwMagicWindowingMode()) {
            return true;
        }
        ActivityRecordEx activityRecordEx = windowState.getAppWindowTokenEx().getActivityRecordEx();
        if (!activityRecordEx.instanceOfHwActivityRecord() || !activityRecordEx.isFromFullscreenToMagicWin()) {
            return HwMwUtils.performPolicy(208, new Object[]{windowState.getAppWindowTokenEx()}).getBoolean("RESULT_NEED_SYSTEM_ANIMATION", true);
        }
        activityRecordEx.setIsFromFullscreenToMagicWin(false);
        return false;
    }

    public void setInputMethodWindowTop(int top) {
        this.mInputMethodWindowTop = top;
    }

    public void initializeHwAnim(Animation anim, int appWidth, int appHeight, int frameWidth) {
        if (anim == null) {
            Slog.e(TAG, "Fail to initialize animation because the parameter is null");
        } else {
            anim.initialize(frameWidth, this.mInputMethodWindowHeight, appWidth, appHeight);
        }
    }

    public int calculateInputMethodWindowHeight(int appHeight, int lazyMode, int frameBottom, int frameHeight, int rotation) {
        if (lazyMode != 0) {
            this.mInputMethodWindowHeight = appHeight - this.mInputMethodWindowTop;
        } else if (rotation == 0 || rotation == 2) {
            this.mInputMethodWindowHeight = frameBottom - this.mInputMethodWindowTop;
        } else {
            this.mInputMethodWindowHeight = frameHeight - this.mInputMethodWindowTop;
        }
        return this.mInputMethodWindowHeight;
    }

    public boolean isPopUpIme(int inputMethodWindowHeight, boolean isImeWithHwFlag, WindowManager.LayoutParams attrs) {
        if (attrs == null) {
            Slog.e(TAG, "Fail to get the type of the pop up because the parameter is null");
            return false;
        } else if ((!isImeWithHwFlag || (attrs.hwFlags & 1048576) == 0) && (isImeWithHwFlag || inputMethodWindowHeight > INPUT_METHOD_WINDOW_THRESHOLD_HEIGHT)) {
            return false;
        } else {
            return true;
        }
    }
}
