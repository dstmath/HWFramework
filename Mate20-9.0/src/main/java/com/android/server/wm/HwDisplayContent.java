package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.IBinder;
import android.util.HwPCUtils;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayInfo;
import com.android.internal.util.ToBooleanFunction;
import com.android.server.security.securitydiagnose.HwSecDiagnoseConstant;
import com.huawei.android.statistical.StatisticalUtils;
import huawei.android.os.HwGeneralManager;
import java.util.ArrayList;
import java.util.List;

public class HwDisplayContent extends DisplayContent {
    private boolean mTmpshouldDropMotionEventForTouchPad;

    @FunctionalInterface
    private interface ScreenshoterForExternalDisplay<E> {
        E screenshotForExternalDisplay(IBinder iBinder, Rect rect, int i, int i2, int i3, int i4, boolean z, int i5);
    }

    public HwDisplayContent(Display display, WindowManagerService service, WallpaperController wallpaperController, DisplayWindowController controller) {
        super(display, service, wallpaperController, controller);
    }

    /* access modifiers changed from: package-private */
    public void computeScreenConfiguration(Configuration config) {
        HwDisplayContent.super.computeScreenConfiguration(config);
        if (HwGeneralManager.getInstance().isSupportForce()) {
            DisplayInfo displayInfo = this.mService.getDefaultDisplayContentLocked().getDisplayInfo();
            this.mService.mInputManager.setDisplayWidthAndHeight(displayInfo.logicalWidth, displayInfo.logicalHeight);
        }
    }

    /* access modifiers changed from: package-private */
    public List taskIdFromTop() {
        List<Integer> tasks = new ArrayList<>();
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            TaskStack stack = this.mTaskStackContainers.getChildAt(stackNdx);
            for (int taskNdx = stack.mChildren.size() - 1; taskNdx >= 0; taskNdx--) {
                Task task = (Task) stack.mChildren.get(taskNdx);
                if (task.getTopVisibleAppMainWindow() != null) {
                    int taskId = task.mTaskId;
                    if (taskId != -1) {
                        tasks.add(Integer.valueOf(taskId));
                        return tasks;
                    }
                }
            }
        }
        return tasks;
    }

    public void setDisplayRotationFR(int rotation) {
        IntelliServiceManager.setDisplayRotation(rotation);
    }

    public void togglePCMode(boolean pcMode) {
        if (!pcMode && HwPCUtils.isValidExtDisplayId(this.mDisplay.getDisplayId())) {
            try {
                WindowToken topChild = this.mAboveAppWindowsContainers.getTopChild();
                while (topChild != null) {
                    topChild.removeImmediately();
                    topChild = (WindowToken) this.mAboveAppWindowsContainers.getTopChild();
                }
            } catch (Exception e) {
                HwPCUtils.log("PCManager", "togglePCMode failed!!!");
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean updateRotationUnchecked(boolean forceUpdate) {
        if (!HwPCUtils.isPcCastModeInServer() || (!HwPCUtils.isValidExtDisplayId(this.mDisplayId) && (!HwPCUtils.enabledInPad() || getRotation() != 1))) {
            return HwDisplayContent.super.updateRotationUnchecked(forceUpdate);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void performLayout(boolean initial, boolean updateInputWindows) {
        HwDisplayContent.super.performLayout(initial, updateInputWindows);
    }

    /* access modifiers changed from: package-private */
    public void prepareSurfaces() {
        HwDisplayContent.super.prepareSurfaces();
        if (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(getDisplayId())) {
            int pcScreenDpMode = this.mService.getPCScreenDisplayMode();
            if (this.mService.mHwWMSEx != null) {
                this.mService.mHwWMSEx.computeShownFrameLockedByPCScreenDpMode(pcScreenDpMode);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void uploadOrientation(int rotation) {
        String rotationState;
        if (this.mService.mContext != null) {
            if (rotation == 1 || rotation == 3) {
                rotationState = "is horizontal screen";
            } else {
                rotationState = "is vertical screen";
            }
            StatisticalUtils.reporte(this.mService.mContext, HwSecDiagnoseConstant.OEMINFO_ID_ROOT_CHECK, "{ " + rotationState + " rotation:" + rotation + " }");
        }
    }

    public boolean shouldDropMotionEventForTouchPad(float x, float y) {
        this.mTmpshouldDropMotionEventForTouchPad = false;
        forAllWindows(new ToBooleanFunction(x, y) {
            private final /* synthetic */ float f$1;
            private final /* synthetic */ float f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final boolean apply(Object obj) {
                return HwDisplayContent.lambda$shouldDropMotionEventForTouchPad$0(HwDisplayContent.this, this.f$1, this.f$2, (WindowState) obj);
            }
        }, true);
        return this.mTmpshouldDropMotionEventForTouchPad;
    }

    public static /* synthetic */ boolean lambda$shouldDropMotionEventForTouchPad$0(HwDisplayContent hwDisplayContent, float x, float y, WindowState w) {
        String title = w.getAttrs().getTitle() == null ? null : w.getAttrs().getTitle().toString();
        if ("com.huawei.desktop.systemui/com.huawei.systemui.mk.activity.ImitateActivity".equalsIgnoreCase(title)) {
            hwDisplayContent.mTmpshouldDropMotionEventForTouchPad = false;
            return true;
        }
        if (w.isVisible()) {
            Region outRegion = new Region();
            w.getTouchableRegion(outRegion);
            if (outRegion.contains((int) x, (int) y)) {
                Slog.d("WindowManager", "consume event in title = " + title);
                hwDisplayContent.mTmpshouldDropMotionEventForTouchPad = true;
                return true;
            }
        }
        hwDisplayContent.mTmpshouldDropMotionEventForTouchPad = false;
        return false;
    }
}
