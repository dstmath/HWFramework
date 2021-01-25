package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.Slog;
import android.view.Display;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.wm.DisplayContent;
import java.util.ArrayList;
import java.util.List;

public class DisplayContentBridgeEx {
    private DisplayContentBridge mDisplayContentBridge;
    private boolean mIsHasLighterViewInPcCastMode;
    private boolean mIsShouldDropMotionEventForTouchPad;
    private WindowManagerServiceEx mWindowManagerServiceEx;

    public DisplayContentBridgeEx(Display display, WindowManagerServiceEx serviceEx, ActivityDisplayEx activityDisplayEx) {
        this.mWindowManagerServiceEx = serviceEx;
        if (serviceEx != null) {
            this.mDisplayContentBridge = new DisplayContentBridge(display, serviceEx.getWindowManagerService(), activityDisplayEx == null ? null : activityDisplayEx.getActivityDisplay(), this);
            this.mDisplayContentBridge.setDisplayContentEx(this);
        }
    }

    public DisplayContentBridge getHwDisplayContent() {
        return this.mDisplayContentBridge;
    }

    /* access modifiers changed from: package-private */
    public void computeScreenConfiguration(Configuration config) {
    }

    public void aospComputeScreenConfiguration(Configuration config) {
        this.mDisplayContentBridge.aospComputeScreenConfiguration(config);
    }

    /* access modifiers changed from: package-private */
    public void updateBaseDisplayMetrics(int baseWidth, int baseHeight, int baseDensity) {
    }

    public void initDisplayRoundCorner(int baseWidth, int baseHeight, int baseDensity) {
    }

    public DisplayRoundCorner getDisplayRoundCorner() {
        WindowManagerServiceEx windowManagerServiceEx = this.mWindowManagerServiceEx;
        if (windowManagerServiceEx != null) {
            return DisplayRoundCorner.getInstance(windowManagerServiceEx);
        }
        return null;
    }

    public void aospUpdateBaseDisplayMetrics(int baseWidth, int baseHeight, int baseDensity) {
        this.mDisplayContentBridge.aospUpdateBaseDisplayMetrics(baseWidth, baseHeight, baseDensity);
    }

    /* access modifiers changed from: package-private */
    public Rect getSafeInsetsByType(int type) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public List<Rect> getBoundsByType(int type) {
        return null;
    }

    /* access modifiers changed from: package-private */
    public List taskIdFromTop() {
        return new ArrayList<>(0);
    }

    public TaskStackContainersEx getTaskStackContainers() {
        DisplayContentBridge displayContentBridge = this.mDisplayContentBridge;
        if (displayContentBridge == null || displayContentBridge.mTaskStackContainers == null) {
            return null;
        }
        TaskStackContainersEx taskStackContainersEx = new TaskStackContainersEx();
        taskStackContainersEx.setTaskStackContainers(this.mDisplayContentBridge.mTaskStackContainers);
        return taskStackContainersEx;
    }

    public void setDisplayRotationFR(int rotation) {
    }

    public void togglePCMode(boolean isPcMode) {
    }

    public Display getDisplay() {
        return this.mDisplayContentBridge.mDisplay;
    }

    public AboveAppWindowContainersEx getAboveAppWindowContainers() {
        AboveAppWindowContainersEx aboveAppWindowContainersEx = new AboveAppWindowContainersEx();
        aboveAppWindowContainersEx.setAboveAppWindowsContainers(this.mDisplayContentBridge.mAboveAppWindowsContainers);
        return aboveAppWindowContainersEx;
    }

    public int getDisplayId() {
        return this.mDisplayContentBridge.mDisplayId;
    }

    public int getRotation() {
        return this.mDisplayContentBridge.getRotation();
    }

    /* access modifiers changed from: protected */
    public boolean updateRotationUnchecked(boolean isForceUpdate) {
        return false;
    }

    public boolean aospUpdateRotationUnchecked(boolean isForceUpdate) {
        return this.mDisplayContentBridge.aospUpdateRotationUnchecked(isForceUpdate);
    }

    /* access modifiers changed from: package-private */
    public void performLayout(boolean isInitial, boolean isUpdateInputWindows) {
    }

    public void aospPerformLayout(boolean isInitial, boolean isUpdateInputWindows) {
        this.mDisplayContentBridge.aospPerformLayout(isInitial, isUpdateInputWindows);
    }

    public TaskStackEx getTopStack() {
        if (this.mDisplayContentBridge.getTopStack() == null) {
            return null;
        }
        return new TaskStackEx(this.mDisplayContentBridge.getTopStack());
    }

    public boolean isStackVisible(int windowingMode) {
        return this.mDisplayContentBridge.isStackVisible(windowingMode);
    }

    public boolean isMagicWindowStackVisible() {
        synchronized (getWmService().getGlobalLock()) {
            if (isStackVisible(103)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void uploadOrientation(int rotation) {
    }

    public boolean shouldDropMotionEventForTouchPad(float x, float y) {
        return false;
    }

    public boolean hasLighterViewInPCCastMode() {
        return false;
    }

    public void forAllWindowsDropMotionEventForTouchPad(float coordX, float coordY, boolean isTraverseTopToBottom) {
        this.mIsShouldDropMotionEventForTouchPad = false;
        this.mDisplayContentBridge.forAllWindows(new ToBooleanFunction(coordX, coordY) {
            /* class com.android.server.wm.$$Lambda$DisplayContentBridgeEx$tQBvaDz9bsmswZ15hxtzrllAf0 */
            private final /* synthetic */ float f$1;
            private final /* synthetic */ float f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final boolean apply(Object obj) {
                return DisplayContentBridgeEx.this.lambda$forAllWindowsDropMotionEventForTouchPad$0$DisplayContentBridgeEx(this.f$1, this.f$2, (WindowState) obj);
            }
        }, isTraverseTopToBottom);
    }

    public /* synthetic */ boolean lambda$forAllWindowsDropMotionEventForTouchPad$0$DisplayContentBridgeEx(float coordX, float coordY, WindowState winState) {
        String title = winState.getAttrs().getTitle() == null ? null : winState.getAttrs().getTitle().toString();
        if ("com.huawei.desktop.systemui/com.huawei.systemui.mk.activity.ImitateActivity".equalsIgnoreCase(title)) {
            this.mIsShouldDropMotionEventForTouchPad = false;
            return true;
        }
        if (winState.isVisible() && !"Emui:A11WaterMarkWnd".equalsIgnoreCase(title)) {
            Region outRegion = new Region();
            winState.getTouchableRegion(outRegion);
            if (outRegion.contains((int) coordX, (int) coordY)) {
                Slog.d("WindowManager", "consume event in title = " + title);
                this.mIsShouldDropMotionEventForTouchPad = true;
                return true;
            }
        }
        this.mIsShouldDropMotionEventForTouchPad = false;
        return false;
    }

    public boolean isShouldDropMotionEventForTouchPad() {
        return this.mIsShouldDropMotionEventForTouchPad;
    }

    public void forAllWindowsLighterViewInPcCastMode(boolean isTraverseTopToBottom) {
        this.mDisplayContentBridge.forAllWindows(new ToBooleanFunction() {
            /* class com.android.server.wm.$$Lambda$DisplayContentBridgeEx$xH1HztlF0ZJTH3Orl1I5eAvYKoA */

            public final boolean apply(Object obj) {
                return DisplayContentBridgeEx.this.lambda$forAllWindowsLighterViewInPcCastMode$1$DisplayContentBridgeEx((WindowState) obj);
            }
        }, isTraverseTopToBottom);
    }

    public /* synthetic */ boolean lambda$forAllWindowsLighterViewInPcCastMode$1$DisplayContentBridgeEx(WindowState winState) {
        if (!"com.huawei.systemui.mk.lighterdrawer.LighterDrawView".equalsIgnoreCase(winState.getAttrs().getTitle() == null ? null : winState.getAttrs().getTitle().toString()) || !winState.isVisible()) {
            this.mIsHasLighterViewInPcCastMode = false;
            return false;
        }
        this.mIsHasLighterViewInPcCastMode = true;
        return true;
    }

    public boolean isHasLighterViewInPcCastMode() {
        return this.mIsHasLighterViewInPcCastMode;
    }

    public WindowManagerServiceEx getWmService() {
        return this.mWindowManagerServiceEx;
    }

    public class TaskStackContainersEx {
        protected DisplayContent.TaskStackContainers mTaskStackContainers;

        public TaskStackContainersEx() {
        }

        public void setTaskStackContainers(DisplayContent.TaskStackContainers taskStackContainers) {
            this.mTaskStackContainers = taskStackContainers;
        }

        /* access modifiers changed from: protected */
        public int getChildCount() {
            return this.mTaskStackContainers.getChildCount();
        }

        /* access modifiers changed from: protected */
        public TaskStackEx getChildAt(int index) {
            return new TaskStackEx(this.mTaskStackContainers.getChildAt(index));
        }
    }

    public final class AboveAppWindowContainersEx {
        private DisplayContent.AboveAppWindowContainers mAboveAppWindowsContainers;

        public AboveAppWindowContainersEx() {
        }

        public void setAboveAppWindowsContainers(DisplayContent.AboveAppWindowContainers aboveAppWindowsContainers) {
            this.mAboveAppWindowsContainers = aboveAppWindowsContainers;
        }

        public WindowTokenEx getTopChild() {
            DisplayContent.AboveAppWindowContainers aboveAppWindowContainers = this.mAboveAppWindowsContainers;
            if (aboveAppWindowContainers == null || aboveAppWindowContainers.getTopChild() == null) {
                return null;
            }
            WindowTokenEx windowTokenEx = new WindowTokenEx();
            windowTokenEx.setWindowToken((WindowToken) this.mAboveAppWindowsContainers.getTopChild());
            return windowTokenEx;
        }
    }
}
