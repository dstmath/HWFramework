package com.android.server.wm;

import android.app.HwRecentTaskInfo;
import android.content.res.HwPCMultiWindowCompatibility;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.os.RemoteException;
import android.util.HwPCUtils;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;
import com.huawei.android.app.HwActivityManager;

public class TaskTapPointerEventListener implements WindowManagerPolicyConstants.PointerEventListener {
    public static final int INVALID_POS = -1;
    private final DisplayContent mDisplayContent;
    private final Region mHwPCtouchExcludeRegion = new Region();
    private int mLastfreeformTaskId = -1;
    private int mPointerIconType = 1;
    private final WindowManagerService mService;
    private final Rect mTmpRect = new Rect();
    private final Region mTouchExcludeRegion = new Region();

    public TaskTapPointerEventListener(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
    }

    public void onPointerEvent(MotionEvent motionEvent, int displayId) {
        if (displayId == getDisplayId()) {
            try {
                onPointerEvent(motionEvent);
            } catch (IndexOutOfBoundsException e) {
                HwPCUtils.log("TaskTapPointerEventListener", "IndexOutOfBoundsException occured : " + e);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:106:?, code lost:
        return;
     */
    public void onPointerEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction() & 255;
        if (action == 0) {
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            synchronized (this) {
                if (HwPCUtils.isPcCastModeInServer() && this.mHwPCtouchExcludeRegion.contains(x, y)) {
                    return;
                }
                if ((motionEvent.getFlags() & 65536) == 0) {
                    int freeformTaskId = -1;
                    if (this.mDisplayContent != null) {
                        TaskStack freeformStack = this.mDisplayContent.getStack(5, 1);
                        if (freeformStack != null) {
                            freeformTaskId = freeformStack.taskIdFromPoint(x, y);
                            if (this.mLastfreeformTaskId * freeformTaskId < 0) {
                                if (freeformTaskId < 0) {
                                    boolean needUpdate = false;
                                    if (this.mService.getInputMethodWindow() != null) {
                                        Region region = new Region();
                                        this.mService.getInputMethodWindow().getTouchableRegion(region);
                                        if (!region.contains(x, y)) {
                                            needUpdate = true;
                                        }
                                    } else {
                                        needUpdate = true;
                                    }
                                    if (needUpdate) {
                                        HwActivityManager.updateFreeFormOutLine(1);
                                        this.mLastfreeformTaskId = freeformTaskId;
                                    }
                                } else {
                                    HwActivityManager.updateFreeFormOutLine(2);
                                    this.mLastfreeformTaskId = freeformTaskId;
                                }
                            }
                        }
                    }
                    if (!this.mTouchExcludeRegion.contains(x, y)) {
                        this.mService.mTaskPositioningController.handleTapOutsideTask(this.mDisplayContent, x, y);
                    } else {
                        if (HwPCUtils.isPcCastModeInServer()) {
                            this.mService.mTaskPositioningController.handleTapOutsideTask(this.mDisplayContent, -1, -1);
                        }
                        if (freeformTaskId > 0) {
                            this.mService.mTaskPositioningController.handleTapOutsideTask(this.mDisplayContent, x, y);
                        }
                    }
                }
            }
        } else if (action == 7) {
            int x2 = (int) motionEvent.getX();
            int y2 = (int) motionEvent.getY();
            Task task = this.mDisplayContent.findTaskForResizePoint(x2, y2);
            int iconType = 1;
            if (task != null) {
                task.getDimBounds(this.mTmpRect);
                if (!this.mTmpRect.isEmpty() && !this.mTmpRect.contains(x2, y2)) {
                    int i = 1014;
                    if (x2 < this.mTmpRect.left) {
                        if (y2 < this.mTmpRect.top) {
                            i = 1017;
                        } else if (y2 > this.mTmpRect.bottom) {
                            i = 1016;
                        }
                        iconType = i;
                    } else if (x2 > this.mTmpRect.right) {
                        if (y2 < this.mTmpRect.top) {
                            i = 1016;
                        } else if (y2 > this.mTmpRect.bottom) {
                            i = 1017;
                        }
                        iconType = i;
                    } else if (y2 < this.mTmpRect.top || y2 > this.mTmpRect.bottom) {
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
                    if (ti != null && !HwPCMultiWindowCompatibility.isResizable(ti.windowState)) {
                        iconType = 1;
                    } else if (this.mTouchExcludeRegion.contains(x2, y2)) {
                        iconType = 1;
                    }
                }
            }
            if (this.mPointerIconType != iconType) {
                this.mPointerIconType = iconType;
                if (this.mPointerIconType == 1) {
                    this.mService.mH.obtainMessage(55, x2, y2, this.mDisplayContent).sendToTarget();
                } else {
                    InputManager.getInstance().setPointerIconType(this.mPointerIconType);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setTouchExcludeRegion(Region newRegion) {
        synchronized (this) {
            this.mTouchExcludeRegion.set(newRegion);
        }
    }

    private int getDisplayId() {
        return this.mDisplayContent.getDisplayId();
    }

    /* access modifiers changed from: package-private */
    public void setHwPCTouchExcludeRegion(Region newRegion) {
        synchronized (this) {
            this.mHwPCtouchExcludeRegion.set(newRegion);
        }
    }
}
