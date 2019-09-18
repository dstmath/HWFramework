package com.android.server.wm;

import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;

public final class HwWindowStateEx implements IHwWindowStateEx {
    public static final String TAG = "HwWindowStateEx";
    final WindowManagerService mService;
    final WindowState mWinState;

    public HwWindowStateEx(WindowManagerService wms, WindowState windowState) {
        this.mService = wms;
        this.mWinState = windowState;
    }

    public Rect adjustImePosForFreeform(Rect contentFrame, Rect containingFrame) {
        if (!HwFreeFormUtils.isFreeFormEnable()) {
            return containingFrame;
        }
        int D1 = contentFrame.bottom - containingFrame.bottom;
        int D2 = contentFrame.top - containingFrame.top;
        int offsetY = D1 > D2 ? D1 : D2;
        Rect taskBounds = new Rect();
        if (offsetY < 0) {
            this.mWinState.getTask().getBounds(taskBounds);
            taskBounds.offset(0, offsetY);
            this.mWinState.getTask().setBounds(taskBounds);
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
        if (!isInHwFreeFormWorkspace() || this.mWinState.getDisplayContent().getConfiguration().orientation == 2) {
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
        int top = frame.top > limitFrame.bottom - minVisibleHeight ? limitFrame.bottom - minVisibleHeight : frame.top;
        if (isInHwFreeFormWorkspace() && !isInHideCaptionList()) {
            return top;
        }
        return limitFrame.top > top ? limitFrame.top : top;
    }
}
