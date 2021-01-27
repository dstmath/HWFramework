package com.android.server.wm;

import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import com.huawei.android.view.DisplayInfoEx;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.server.HwPartIawareUtil;

public class HwWindowStateAnimator extends WindowStateAnimatorBridgeEx {
    private static final int SCENE_POS_EXIT = -1;
    private static final int SCENE_POS_START = 1;
    private static final String TAG = "HwWindowStateAnimator";
    private static final int TYPE_LEFT = 1;
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_RIGHT = 2;
    private DisplayEngineManager mDisplayEngineManager;
    private boolean mIsDefaultDisplay = true;
    private boolean mIsLeftLazyMode;
    private boolean mIsRightLazyMode;
    private int mLastLazyMode;
    private float mLazyScale;
    private int mScreenHeight;
    private int mScreenWidth;
    private final boolean mSkipScalingDownSurface;
    private final WindowManager mWindowManager;

    public HwWindowStateAnimator(WindowStateEx win) {
        super(win);
        if (win == null) {
            this.mSkipScalingDownSurface = false;
            this.mWindowManager = null;
            return;
        }
        this.mSkipScalingDownSurface = (win.getAttrs() == null || win.getAttrs().getTitle() == null) ? false : win.getAttrs().getTitle().toString().contains("hwSingleMode_window");
        int displayId = this.mWin.getDisplayId();
        if (!(displayId == -1 || displayId == 0)) {
            this.mIsDefaultDisplay = false;
        }
        this.mLastLazyMode = this.mService.getLazyMode();
        if (!this.mIsDefaultDisplay) {
            this.mLastLazyMode = 0;
        }
        this.mLazyScale = 1.0f;
        setLazyIsExiting(false);
        setLazyIsEntering(false);
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mDisplayEngineManager = new DisplayEngineManager();
    }

    private void updatedisplayinfo() {
        Display defaultDisplay = this.mWindowManager.getDefaultDisplay();
        DisplayInfoEx defaultDisplayInfo = new DisplayInfoEx();
        DisplayInfoEx.getDisplayInfo(defaultDisplay, defaultDisplayInfo);
        boolean isPortrait = defaultDisplayInfo.getLogicalHeight() > defaultDisplayInfo.getLogicalWidth();
        this.mScreenWidth = isPortrait ? defaultDisplayInfo.getLogicalWidth() : defaultDisplayInfo.getLogicalHeight();
        this.mScreenHeight = isPortrait ? defaultDisplayInfo.getLogicalHeight() : defaultDisplayInfo.getLogicalWidth();
    }

    public int adjustAnimLayerIfCoverclosed(int type, int animLayer) {
        if (type != 2000 || animLayer >= TOP_LAYER || (!isCoverClosed())) {
            return animLayer;
        }
        return TOP_LAYER;
    }

    private boolean floatEqualCompare(float f) {
        return ((double) Math.abs(this.mLazyScale - f)) < 1.0E-6d;
    }

    private boolean isOrientationLandscape(int requestedOrientation) {
        return requestedOrientation == 0 || requestedOrientation == 6 || requestedOrientation == 8 || requestedOrientation == 11;
    }

    private boolean isMultiWindowInSingleHandMode() {
        return (this.mWin.getAttrs().type == 2034 && isLazyIsEntering() && floatEqualCompare(0.8f)) || (isLazyIsExiting() && floatEqualCompare(0.95f));
    }

    /* access modifiers changed from: protected */
    public WindowSurfaceControllerEx createSurfaceLocked(int windowType, int ownerUid) {
        WindowSurfaceControllerEx surfaceController = HwWindowStateAnimator.super.aospCreateSurfaceLocked(windowType, ownerUid);
        sendMessageToDeSceneHandler(1);
        return surfaceController;
    }

    /* access modifiers changed from: protected */
    public void destroySurfaceLocked() {
        sendMessageToDeSceneHandler(-1);
        HwWindowStateAnimator.super.aospDestroySurfaceLocked();
    }

    private void sendMessageToDeSceneHandler(int pos) {
        WindowStateEx windowStateEx = this.mWin;
        WindowManagerServiceEx service = windowStateEx.getWmServicesEx();
        String surName = windowStateEx.getAttrs().getTitle().toString();
        DisplayContentEx displayContentEx = service.getDefaultDisplayContentLocked();
        int initScreenWidth = displayContentEx.getInitialDisplayWidth();
        int initScreenHeight = displayContentEx.getInitialDisplayHeight();
        Bundle data = new Bundle();
        data.putInt("Position", pos);
        data.putString("SurfaceName", surName);
        data.putInt("FrameLeft", windowStateEx.getFrameLw().left);
        data.putInt("FrameRight", windowStateEx.getFrameLw().right);
        data.putInt("FrameTop", windowStateEx.getFrameLw().top);
        data.putInt("FrameBottom", windowStateEx.getFrameLw().bottom);
        data.putInt("SourceWidth", windowStateEx.getRequestedWidth());
        data.putInt("SourceHeight", windowStateEx.getRequestedHeight());
        data.putInt("DisplayWidth", initScreenWidth);
        data.putInt("DisplayHeight", initScreenHeight);
        data.putInt("Layer", windowStateEx.getLayer());
        data.putInt("BaseLayer", windowStateEx.getBaseLayer());
        data.putInt("SubLayer", windowStateEx.getSubLayer());
        data.putInt("SurfaceFormat", getSurfaceFormat());
        data.putString("AttachWinName", null);
        this.mDisplayEngineManager.sendMessage(DE_MESSAGE_ID_CUSTOM, data);
    }

    private int checkWindowType(WindowStateEx win) {
        if (win.getAppOp() == 24) {
            return 1;
        }
        if (win.getAttrs().type == 2005) {
            return 2;
        }
        return -1;
    }

    public boolean isEvilWindow(WindowStateEx win) {
        if (win == null || win.isWinStateNull()) {
            return false;
        }
        return HwPartIawareUtil.isEvilWindow(win.getSessionEx().getPid(), win.identityHashCode(), checkWindowType(win));
    }
}
