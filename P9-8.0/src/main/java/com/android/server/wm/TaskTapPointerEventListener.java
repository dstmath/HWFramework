package com.android.server.wm;

import android.app.HwRecentTaskInfo;
import android.content.res.HwPCMultiWindowCompatibility;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.os.RemoteException;
import android.util.HwPCUtils;
import android.view.MotionEvent;
import android.view.WindowManagerPolicy.PointerEventListener;

public class TaskTapPointerEventListener implements PointerEventListener {
    public static final int INVALID_POS = -1;
    private final DisplayContent mDisplayContent;
    private final Region mHwPCtouchExcludeRegion = new Region();
    private int mPointerIconType = 1;
    private final WindowManagerService mService;
    private final Rect mTmpRect = new Rect();
    private final Region mTouchExcludeRegion = new Region();

    public TaskTapPointerEventListener(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
    }

    public void onPointerEvent(MotionEvent motionEvent) {
        int x;
        int y;
        switch (motionEvent.getAction() & 255) {
            case 0:
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                synchronized (this) {
                    if (!(HwPCUtils.isPcCastModeInServer() && this.mHwPCtouchExcludeRegion.contains(x, y))) {
                        if (!this.mTouchExcludeRegion.contains(x, y)) {
                            this.mService.mH.obtainMessage(31, x, y, this.mDisplayContent).sendToTarget();
                        } else if (HwPCUtils.isPcCastModeInServer()) {
                            this.mService.mH.obtainMessage(31, -1, -1, this.mDisplayContent).sendToTarget();
                        }
                    }
                }
                return;
            case 7:
                x = (int) motionEvent.getX();
                y = (int) motionEvent.getY();
                Task task = this.mDisplayContent.findTaskForResizePoint(x, y);
                int iconType = 1;
                if (task != null) {
                    task.getDimBounds(this.mTmpRect);
                    if (!(this.mTmpRect.isEmpty() || (this.mTmpRect.contains(x, y) ^ 1) == 0)) {
                        if (x < this.mTmpRect.left) {
                            iconType = y < this.mTmpRect.top ? 1017 : y > this.mTmpRect.bottom ? 1016 : 1014;
                        } else if (x > this.mTmpRect.right) {
                            iconType = y < this.mTmpRect.top ? 1016 : y > this.mTmpRect.bottom ? 1017 : 1014;
                        } else if (y < this.mTmpRect.top || y > this.mTmpRect.bottom) {
                            iconType = 1015;
                        }
                    }
                    if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(this.mDisplayContent.getDisplayId())) {
                        HwRecentTaskInfo ti = null;
                        try {
                            if (this.mService.mPCManager != null) {
                                ti = this.mService.mPCManager.getHwRecentTaskInfo(task.mTaskId);
                            }
                        } catch (RemoteException e) {
                        }
                        if (ti != null && (HwPCMultiWindowCompatibility.isResizable(ti.windowState) ^ 1) != 0) {
                            iconType = 1;
                        } else if (this.mTouchExcludeRegion.contains(x, y)) {
                            iconType = 1;
                        }
                    }
                }
                if (this.mPointerIconType != iconType) {
                    this.mPointerIconType = iconType;
                    if (this.mPointerIconType == 1) {
                        this.mService.mH.obtainMessage(55, x, y, this.mDisplayContent).sendToTarget();
                        return;
                    } else {
                        InputManager.getInstance().setPointerIconType(this.mPointerIconType);
                        return;
                    }
                }
                return;
            default:
                return;
        }
    }

    void setTouchExcludeRegion(Region newRegion) {
        synchronized (this) {
            this.mTouchExcludeRegion.set(newRegion);
        }
    }

    void setHwPCTouchExcludeRegion(Region newRegion) {
        synchronized (this) {
            this.mHwPCtouchExcludeRegion.set(newRegion);
        }
    }
}
