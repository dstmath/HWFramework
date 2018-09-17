package com.android.server.wm;

import android.content.Context;
import android.content.res.HwPCMultiWindowCompatibility;
import android.graphics.Rect;
import android.util.HwPCUtils;
import com.android.server.am.ActivityRecord;
import com.android.server.am.HwActivityRecord;

public final class HwWindowManagerServiceEx implements IHwWindowManagerServiceEx {
    static final String TAG = "HwWindowManagerServiceEx";
    final Context mContext;
    IHwWindowManagerInner mIWmsInner = null;

    public HwWindowManagerServiceEx(IHwWindowManagerInner wms, Context context) {
        this.mIWmsInner = wms;
        this.mContext = context;
    }

    private boolean isInputTargetWindow(WindowState windowState, WindowState inputTargetWin) {
        boolean z = false;
        if (inputTargetWin == null) {
            return false;
        }
        Task inputMethodTask = inputTargetWin.getTask();
        Task task = windowState.getTask();
        if (inputMethodTask == null || task == null) {
            return false;
        }
        if (inputMethodTask.mTaskId == task.mTaskId) {
            z = true;
        }
        return z;
    }

    public void adjustWindowPosForPadPC(Rect containingFrame, Rect contentFrame, WindowState imeWin, WindowState inputTargetWin, WindowState win) {
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.enabledInPad() && HwPCUtils.isValidExtDisplayId(win.getDisplayId()) && imeWin != null && imeWin.isVisibleNow() && isInputTargetWindow(win, inputTargetWin)) {
            int windowState = -1;
            ActivityRecord r = ActivityRecord.forToken(win.getAttrs().token);
            if (r != null) {
                if (r instanceof HwActivityRecord) {
                    windowState = ((HwActivityRecord) r).getWindowState();
                }
                if (windowState != -1 && (HwPCMultiWindowCompatibility.isLayoutFullscreen(windowState) ^ 1) != 0 && (HwPCMultiWindowCompatibility.isLayoutMaximized(windowState) ^ 1) != 0 && !contentFrame.isEmpty() && containingFrame.bottom > contentFrame.bottom) {
                    int D1 = contentFrame.bottom - containingFrame.bottom;
                    int D2 = contentFrame.top - containingFrame.top;
                    int offsetY = D1 > D2 ? D1 : D2;
                    if (offsetY < 0) {
                        containingFrame.offset(0, offsetY);
                    }
                }
            }
        }
    }

    public void layoutWindowForPadPCMode(WindowState win, WindowState inputTargetWin, WindowState imeWin, Rect pf, Rect df, Rect cf, Rect vf, int contentBottom) {
        if (isInputTargetWindow(win, inputTargetWin)) {
            int inputMethodTop = 0;
            if (imeWin != null && imeWin.isVisibleLw()) {
                int top = (imeWin.getDisplayFrameLw().top > imeWin.getContentFrameLw().top ? imeWin.getDisplayFrameLw().top : imeWin.getContentFrameLw().top) + imeWin.getGivenContentInsetsLw().top;
                inputMethodTop = contentBottom < top ? contentBottom : top;
            }
            if (inputMethodTop > 0) {
                vf.bottom = inputMethodTop;
                cf.bottom = inputMethodTop;
                df.bottom = inputMethodTop;
                pf.bottom = inputMethodTop;
            }
        }
    }

    public int releaseSnapshots(int memLevel) {
        int releaseSnapshots;
        synchronized (this.mIWmsInner.getWindowMap()) {
            releaseSnapshots = this.mIWmsInner.getTaskSnapshotController().releaseSnapshots(memLevel);
        }
        return releaseSnapshots;
    }
}
