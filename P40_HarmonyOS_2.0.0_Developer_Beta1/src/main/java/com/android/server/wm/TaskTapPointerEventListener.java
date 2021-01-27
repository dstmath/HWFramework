package com.android.server.wm;

import android.app.HwRecentTaskInfo;
import android.content.res.HwPCMultiWindowCompatibility;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.input.InputManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.WindowManagerPolicyConstants;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.inputmethod.HwInputMethodManager;

public class TaskTapPointerEventListener implements WindowManagerPolicyConstants.PointerEventListener {
    private static final int INIT_FREEFORM_TASK_ID = -2;
    public static final int INVALID_POS = -1;
    private static final int INVALID_TASK_ID = -1;
    private static final boolean IS_HW_MULTIWINDOW_SUPPORTED = SystemProperties.getBoolean("ro.config.hw_multiwindow_optimization", false);
    private final DisplayContent mDisplayContent;
    private final Region mHwPCtouchExcludeRegion = new Region();
    private int mLastfreeformTaskId = -2;
    private int mPointerIconType = 1;
    private final WindowManagerService mService;
    private final Rect mTmpRect = new Rect();
    private final Region mTouchExcludeRegion = new Region();

    public TaskTapPointerEventListener(WindowManagerService service, DisplayContent displayContent) {
        this.mService = service;
        this.mDisplayContent = displayContent;
    }

    /* JADX INFO: finally extract failed */
    public void onPointerEvent(MotionEvent motionEvent) {
        DisplayContent displayContent;
        TaskStack freeformStack;
        Task task;
        Task task2;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            boolean mHasFindExcludeRegion = false;
            int x = (int) motionEvent.getX();
            int y = (int) motionEvent.getY();
            synchronized (this) {
                if (HwPCUtils.isPcCastModeInServer() && this.mHwPCtouchExcludeRegion.contains(x, y)) {
                    return;
                }
                if ((motionEvent.getFlags() & 65536) == 0) {
                    if (!this.mTouchExcludeRegion.contains(x, y)) {
                        mHasFindExcludeRegion = true;
                        this.mService.mTaskPositioningController.handleTapOutsideTask(this.mDisplayContent, x, y);
                    } else if (HwPCUtils.isPcCastModeInServer() || HwPCUtils.isInWindowsCastMode()) {
                        this.mService.mTaskPositioningController.handleTapOutsideTask(this.mDisplayContent, -1, -1);
                    }
                } else {
                    return;
                }
            }
            synchronized (this.mService.getGlobalLock()) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    if (IS_HW_MULTIWINDOW_SUPPORTED) {
                        if (!mHasFindExcludeRegion && this.mDisplayContent.isHwMultiStackVisible(WindowManagerService.H.APP_TRANSITION_GETSPECSFUTURE_TIMEOUT) && isHwFreeFormDrag(x, y)) {
                            this.mService.mTaskPositioningController.handleTapOutsideTask(this.mDisplayContent, x, y);
                        }
                    } else if (!(this.mDisplayContent == null || (freeformStack = this.mDisplayContent.getStack(5, 1)) == null)) {
                        int freeformTaskId = freeformStack.taskIdFromPoint(x, y);
                        if (freeformTaskId == -1 && (task = this.mDisplayContent.findTaskForResizePoint(x, y)) != null && task.inFreeformWindowingMode()) {
                            freeformTaskId = 1;
                        }
                        if (this.mLastfreeformTaskId * freeformTaskId < 0 || this.mLastfreeformTaskId == -2) {
                            if (freeformTaskId < 0) {
                                updateFreeForm(x, y, freeformTaskId);
                            } else {
                                this.mService.mH.post($$Lambda$TaskTapPointerEventListener$H5E5vP2wnfNbGDfOb4ylBrufjlg.INSTANCE);
                                this.mLastfreeformTaskId = freeformTaskId;
                            }
                        }
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            if (HwPCUtils.isInWindowsCastMode() && (displayContent = this.mDisplayContent) != null) {
                if (displayContent.getDisplayId() == 0 || HwPCUtils.getWindowsCastDisplayId() == this.mDisplayContent.getDisplayId()) {
                    InputDevice inputDevice = InputDevice.getDevice(motionEvent.getDeviceId());
                    if (inputDevice == null || !inputDevice.supportsSource(4098)) {
                        this.mService.setFocusedDisplay(HwPCUtils.getWindowsCastDisplayId(), false, "handleTapOutsideTaskXY");
                        HwInputMethodManager.restartInputMethodForMultiDisplay();
                        try {
                            IHwPCManager pcMgr = HwPCUtils.getHwPCManager();
                            if (HwPCUtils.isShopDemo()) {
                                if (pcMgr != null && !pcMgr.isScreenPowerOn()) {
                                    pcMgr.setScreenPower(true);
                                }
                            } else if (pcMgr != null && pcMgr.isScreenPowerOn()) {
                                pcMgr.setScreenPower(false);
                            }
                        } catch (RemoteException e) {
                            HwPCUtils.log("onPointerEvent", "getDesiredScreenPolicyLocked RemoteException");
                        }
                    } else {
                        this.mService.setFocusedDisplay(0, false, "handleTapOutsideTaskXY");
                        HwInputMethodManager.restartInputMethodForMultiDisplay();
                        HwPCUtils.log("onPointerEvent", "onOperateOnPhone.");
                        this.mService.getWindowManagerServiceEx().onOperateOnPhone();
                        try {
                            IHwPCManager pcMgr2 = HwPCUtils.getHwPCManager();
                            if (pcMgr2 != null && !pcMgr2.isScreenPowerOn()) {
                                pcMgr2.setScreenPower(true);
                            }
                        } catch (RemoteException e2) {
                            HwPCUtils.log("onPointerEvent", "getDesiredScreenPolicyLocked RemoteException");
                        }
                    }
                }
            }
        } else if (actionMasked != 1) {
            if (actionMasked == 7 || actionMasked == 9) {
                int x2 = (int) motionEvent.getX();
                int y2 = (int) motionEvent.getY();
                synchronized (this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        task2 = this.mDisplayContent.findTaskForResizePoint(x2, y2);
                        if (task2 != null) {
                            task2.getDimBounds(this.mTmpRect);
                        }
                    } catch (Throwable th2) {
                        WindowManagerService.resetPriorityAfterLockedSection();
                        throw th2;
                    }
                }
                WindowManagerService.resetPriorityAfterLockedSection();
                int iconType = 1;
                if (task2 != null) {
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
                            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
                            if (pcManager != null) {
                                ti = pcManager.getHwRecentTaskInfo(task2.mTaskId);
                            }
                        } catch (RemoteException e3) {
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
                        this.mService.mH.removeMessages(55);
                        this.mService.mH.obtainMessage(55, x2, y2, this.mDisplayContent).sendToTarget();
                        return;
                    }
                    InputManager.getInstance().setPointerIconType(this.mPointerIconType);
                }
            } else if (actionMasked == 10) {
                int x3 = (int) motionEvent.getX();
                int y3 = (int) motionEvent.getY();
                if (this.mPointerIconType != 1) {
                    this.mPointerIconType = 1;
                    this.mService.mH.removeMessages(55);
                    this.mService.mH.obtainMessage(55, x3, y3, this.mDisplayContent).sendToTarget();
                }
            }
        } else if (this.mService.mTaskPositioningController != null) {
            this.mService.mTaskPositioningController.endTaskPositioning(this.mDisplayContent);
        }
    }

    /* access modifiers changed from: package-private */
    public void setTouchExcludeRegion(Region newRegion) {
        synchronized (this) {
            this.mTouchExcludeRegion.set(newRegion);
        }
    }

    /* access modifiers changed from: package-private */
    public void setHwPCTouchExcludeRegion(Region newRegion) {
        synchronized (this) {
            this.mHwPCtouchExcludeRegion.set(newRegion);
        }
    }

    private void updateFreeForm(int x, int y, int freeformTaskId) {
        boolean needUpdate = false;
        if (this.mService.getInputMethodWindowLw() != null) {
            Region region = new Region();
            if (this.mService.getInputMethodWindowLw() instanceof WindowState) {
                this.mService.getInputMethodWindowLw().getTouchableRegion(region);
            }
            if (!region.contains(x, y)) {
                needUpdate = true;
            }
        } else {
            needUpdate = true;
        }
        if (needUpdate) {
            this.mService.mH.post($$Lambda$TaskTapPointerEventListener$uXH_olFfBoCNv8x_8EAPv6meu8E.INSTANCE);
            this.mLastfreeformTaskId = freeformTaskId;
        }
    }

    private boolean isHwFreeFormDrag(int eventX, int eventY) {
        return this.mDisplayContent.findTaskForResizePoint(eventX, eventY) != null;
    }
}
