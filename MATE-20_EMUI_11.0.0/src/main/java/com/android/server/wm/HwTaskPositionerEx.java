package com.android.server.wm;

import android.content.res.HwPCMultiWindowCompatibility;
import android.freeform.HwFreeFormUtils;
import android.graphics.Rect;
import android.os.RemoteException;
import android.pc.IHwPCManager;
import android.util.HwPCUtils;
import com.huawei.server.HwPCFactory;

public final class HwTaskPositionerEx extends HwTaskPositionerBridgeEx {
    private static final int BOTHWAY = 0;
    private static final int HIDE_SIDE_WINDOW_PREVIEW = 0;
    private static final int HOT_AREA_MARGIN = 48;
    private static final int LEFT = 1;
    private static final int PC_WINDOW_MIN_SIZE_DEFAULT = 640;
    private static final int PC_WINDOW_MIN_SIZE_DIVIDER = 2;
    private static final int RIGHT = 2;
    private static final int SHOW_SIDE_WINDOW_PREVIEW = 1;
    private static final Rect SIDELEFT_WINDOW_RECT_REF = new Rect(-2, -2, -2, -2);
    private static final Rect SIDERIGHT_WINDOW_RECT_REF = new Rect(-3, -3, -3, -3);
    private static final String TAG = "HwTaskPositionerEx";
    private static final int TYPE_HEIGHT = 2;
    private static final int TYPE_WIDTH = 1;
    private final Rect leftCursorHotArea = new Rect();
    private boolean mIsSplitWindowAnimateDisplayed = false;
    private int mPcWindowMinHeight;
    private int mPcWindowMinWidth;
    final WindowManagerServiceEx mService;
    private final Rect maximizeWindowBounds = new Rect();
    private final Rect rightCursorHotArea = new Rect();

    public HwTaskPositionerEx(WindowManagerServiceEx service) {
        super(service);
        this.mService = service;
    }

    public void updateFreeFormOutLine(int color) {
        if (HwFreeFormUtils.isFreeFormEnable()) {
            this.mService.postEx(new Runnable(color) {
                /* class com.android.server.wm.$$Lambda$HwTaskPositionerEx$Zftv5fom_A29AO7VYT4LjAET0jI */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    HwTaskPositionerEx.lambda$updateFreeFormOutLine$0(this.f$0);
                }
            });
        }
    }

    public int limitPCWindowSize(int length, int limitType) {
        getPCWindowSizeLimit();
        if (limitType == 1) {
            return Math.max(this.mPcWindowMinWidth, length);
        }
        if (limitType == 2) {
            return Math.max(this.mPcWindowMinHeight, length);
        }
        return PC_WINDOW_MIN_SIZE_DEFAULT;
    }

    public void processPCWindowFinishDragHitHotArea(TaskRecordEx taskRecord, float newX, float newY) {
        if (HwPCMultiWindowCompatibility.isResizable(taskRecord.getWindowState())) {
            try {
                IHwPCManager pcManager = HwPCUtils.getHwPCManager();
                if (pcManager != null) {
                    if (cursorHitHotArea(newX, newY, 1)) {
                        triggerSplitWindowPreviewLayer(0, 0);
                        pcManager.hwResizeTask(taskRecord.getTaskId(), SIDELEFT_WINDOW_RECT_REF);
                        pcManager.triggerRecentTaskSplitView(2, taskRecord.getTaskId());
                    }
                    if (cursorHitHotArea(newX, newY, 2)) {
                        triggerSplitWindowPreviewLayer(0, 0);
                        pcManager.hwResizeTask(taskRecord.getTaskId(), SIDERIGHT_WINDOW_RECT_REF);
                        pcManager.triggerRecentTaskSplitView(1, taskRecord.getTaskId());
                    }
                }
            } catch (RemoteException e) {
                HwPCUtils.log(TAG, "processPCWindowFinishDragHitHotArea RemoteException." + e);
            }
        }
    }

    public void processPCWindowDragHitHotArea(TaskRecordEx taskRecord, float newX, float newY) {
        if (HwPCMultiWindowCompatibility.isResizable(taskRecord.getWindowState())) {
            getPCCursorHotArea();
            if (HwPCMultiWindowCompatibility.isLayoutSplit(taskRecord.getWindowState())) {
                return;
            }
            if (cursorHitHotArea(newX, newY, 1)) {
                triggerSplitWindowPreviewLayer(1, 1);
            } else if (cursorHitHotArea(newX, newY, 2)) {
                triggerSplitWindowPreviewLayer(2, 1);
            } else {
                triggerSplitWindowPreviewLayer(0, 0);
            }
        }
    }

    private boolean cursorHitHotArea(float cursorX, float cursorY, int hitSide) {
        if (hitSide == 1) {
            return this.leftCursorHotArea.contains((int) cursorX, (int) cursorY);
        }
        if (hitSide != 2) {
            return false;
        }
        return this.rightCursorHotArea.contains((int) cursorX, (int) cursorY);
    }

    private void getPCWindowSizeLimit() {
        DefaultHwPCMultiWindowManager multiWindowManager;
        if (this.maximizeWindowBounds.isEmpty() && (multiWindowManager = getHwPCMultiWindowManager(buildAtmsEx())) != null) {
            this.maximizeWindowBounds.set(multiWindowManager.getMaximizedBounds());
            this.mPcWindowMinHeight = this.maximizeWindowBounds.height() / 2;
            this.mPcWindowMinWidth = this.maximizeWindowBounds.width() / 2;
        }
    }

    private void getPCCursorHotArea() {
        DefaultHwPCMultiWindowManager multiWindowManager;
        if (this.maximizeWindowBounds.isEmpty() && (multiWindowManager = getHwPCMultiWindowManager(buildAtmsEx())) != null) {
            this.maximizeWindowBounds.set(multiWindowManager.getMaximizedBounds());
            this.leftCursorHotArea.set(this.maximizeWindowBounds.left, this.maximizeWindowBounds.top, HOT_AREA_MARGIN, this.maximizeWindowBounds.bottom);
            this.rightCursorHotArea.set(this.maximizeWindowBounds.right - HOT_AREA_MARGIN, this.maximizeWindowBounds.top, this.maximizeWindowBounds.right, this.maximizeWindowBounds.bottom);
        }
    }

    private void triggerSplitWindowPreviewLayer(int side, int action) {
        try {
            IHwPCManager pcManager = HwPCUtils.getHwPCManager();
            if (pcManager == null) {
                return;
            }
            if (action == 1) {
                if (!this.mIsSplitWindowAnimateDisplayed) {
                    pcManager.triggerSplitWindowPreviewLayer(side, 1);
                    this.mIsSplitWindowAnimateDisplayed = true;
                }
            } else if (action == 0 && this.mIsSplitWindowAnimateDisplayed) {
                pcManager.triggerSplitWindowPreviewLayer(0, 0);
                this.mIsSplitWindowAnimateDisplayed = false;
            }
        } catch (RemoteException e) {
            HwPCUtils.log(TAG, "triggerSideWindowPreviewLayer RemoteException." + e);
        }
    }

    private ActivityTaskManagerServiceEx buildAtmsEx() {
        return this.mService.getAtmServiceEx();
    }

    private DefaultHwPCMultiWindowManager getHwPCMultiWindowManager(ActivityTaskManagerServiceEx atmsEx) {
        return HwPCFactory.getHwPCFactory().getHwPCFactoryImpl().getHwPCMultiWindowManager(atmsEx);
    }
}
