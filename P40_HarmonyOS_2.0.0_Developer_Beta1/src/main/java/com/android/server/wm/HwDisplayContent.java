package com.android.server.wm;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Flog;
import android.util.HwMwUtils;
import android.util.HwPCUtils;
import android.view.Display;
import android.view.InputDevice;
import com.android.server.wm.utils.HwDisplaySizeUtilEx;
import com.huawei.android.util.SlogEx;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class HwDisplayContent extends DisplayContentBridgeEx {
    private static final int MRX_KEYBOARD_PID = 4253;
    private static final int MRX_KEYBOARD_VID = 4817;
    private static final String TAG_WM = WindowManagerDebugConfigEx.getWmTag();
    private static DisplayRoundCorner sDisplayRoundCorner = null;

    @FunctionalInterface
    private interface ScreenshoterForExternalDisplay<E> {
        E screenshotForExternalDisplay(IBinder iBinder, Rect rect, int i, int i2, int i3, int i4, boolean z, int i5);
    }

    public HwDisplayContent(Display display, WindowManagerServiceEx service, ActivityDisplayEx activityDisplay) {
        super(display, service, activityDisplay);
    }

    /* access modifiers changed from: package-private */
    public void computeScreenConfiguration(Configuration config) {
        HwDisplayContent.super.aospComputeScreenConfiguration(config);
        if (HwPCUtils.enabledInPad() && HwPCUtils.isPcCastModeInServer()) {
            InputDevice[] devices = getWmService().getInputManagerServiceEx().getInputDevices();
            int len = devices != null ? devices.length : 0;
            for (int i = 0; i < len; i++) {
                InputDevice device = devices[i];
                if (device.getProductId() == MRX_KEYBOARD_PID && device.getVendorId() == MRX_KEYBOARD_VID) {
                    config.keyboard = 2;
                    config.hardKeyboardHidden = 1;
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateBaseDisplayMetrics(int baseWidth, int baseHeight, int baseDensity) {
        HwDisplayContent.super.aospUpdateBaseDisplayMetrics(baseWidth, baseHeight, baseDensity);
        if (DisplayRoundCorner.isRoundCornerDisplay() || HwDisplaySizeUtilEx.hasSideInScreen()) {
            sDisplayRoundCorner = getDisplayRoundCorner();
            sDisplayRoundCorner.setScreenSize(baseWidth, baseHeight);
            String str = TAG_WM;
            SlogEx.d(str, "updateBaseDisplayMetrics " + sDisplayRoundCorner);
        }
    }

    public void initDisplayRoundCorner(int baseWidth, int baseHeight, int baseDensity) {
        if (DisplayRoundCorner.isRoundCornerDisplay() || HwDisplaySizeUtilEx.hasSideInScreen()) {
            sDisplayRoundCorner = getDisplayRoundCorner();
            sDisplayRoundCorner.setScreenSize(baseWidth, baseHeight);
            String str = TAG_WM;
            SlogEx.d(str, "initDisplayRoundCorner " + sDisplayRoundCorner);
        }
    }

    /* access modifiers changed from: package-private */
    public Rect getSafeInsetsByType(int type) {
        DisplayRoundCorner displayRoundCorner = sDisplayRoundCorner;
        if (displayRoundCorner == null) {
            SlogEx.v(TAG_WM, "sDisplayRoundCorner is null");
            return null;
        } else if (type == 1) {
            return displayRoundCorner.getRoundCornerSafeInsets(getRotation());
        } else {
            if (type != 2) {
                return null;
            }
            return displayRoundCorner.getSideDisplaySafeInsets(getRotation());
        }
    }

    /* access modifiers changed from: package-private */
    public List<Rect> getBoundsByType(int type) {
        DisplayRoundCorner displayRoundCorner = sDisplayRoundCorner;
        if (displayRoundCorner == null) {
            SlogEx.v(TAG_WM, "sDisplayRoundCorner is null");
            return null;
        } else if (type == 1) {
            return displayRoundCorner.getRoundCornerUnsafeBounds(getRotation());
        } else {
            if (type != 2) {
                return null;
            }
            return displayRoundCorner.getSideDisplayUnsafeBounds(getRotation());
        }
    }

    /* access modifiers changed from: package-private */
    public List taskIdFromTop() {
        int taskId;
        List<Integer> tasks = new ArrayList<>();
        for (int stackNdx = getTaskStackContainers().getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            TaskStackEx stack = getTaskStackContainers().getChildAt(stackNdx);
            if (stack != null) {
                for (int taskNdx = stack.getChildrenSize() - 1; taskNdx >= 0; taskNdx--) {
                    TaskEx task = stack.getChildren(taskNdx);
                    if (task != null && task.getTopVisibleAppMainWindow() != null && (taskId = task.getTaskId()) != -1) {
                        tasks.add(Integer.valueOf(taskId));
                        return tasks;
                    }
                }
                continue;
            }
        }
        return tasks;
    }

    public void setDisplayRotationFR(int rotation) {
        IntelliServiceManager.setDisplayRotation(rotation);
    }

    public void togglePCMode(boolean isPcMode) {
        if (!isPcMode && HwPCUtils.isValidExtDisplayId(getDisplay().getDisplayId())) {
            try {
                WindowTokenEx topChild = getAboveAppWindowContainers().getTopChild();
                while (topChild != null) {
                    topChild.removeImmediately();
                    topChild = getAboveAppWindowContainers().getTopChild();
                }
            } catch (Exception e) {
                HwPCUtils.log("PCManager", "togglePCMode failed!!!");
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean updateRotationUnchecked(boolean isForceUpdate) {
        if (HwPCUtils.isPcCastModeInServer()) {
            if (HwPCUtils.isValidExtDisplayId(getDisplayId())) {
                return false;
            }
            if (HwPCUtils.enabledInPad() && getRotation() == 1) {
                return false;
            }
        }
        return HwDisplayContent.super.aospUpdateRotationUnchecked(isForceUpdate);
    }

    /* access modifiers changed from: package-private */
    public void performLayout(boolean isInitial, boolean isUpdateInputWindows) {
        TaskStackEx topStack;
        HwDisplayContent.super.aospPerformLayout(isInitial, isUpdateInputWindows);
        if (HwMwUtils.ENABLED && (topStack = getTopStack()) != null && !topStack.isTaskStackEmpty()) {
            if (topStack.isVisible() || topStack.inHwFreeFormWindowingMode()) {
                HwMwUtils.performPolicy(8, new Object[]{Boolean.valueOf(isMagicWindowStackVisible()), Integer.valueOf(getDisplayId())});
            }
        }
    }

    /* access modifiers changed from: protected */
    public void uploadOrientation(int rotation) {
        if (getWmService().getContext() != null) {
            String rotationState = (rotation == 1 || rotation == 3) ? "horizontal" : "vertical";
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("ScreenOrientation", rotationState);
                jsonObject.put("rotation", rotation);
                Flog.bdReport(991310171, jsonObject);
            } catch (JSONException e) {
                SlogEx.e(TAG_WM, "Create json failed when uploadOrientation.");
            }
        }
    }

    public boolean shouldDropMotionEventForTouchPad(float coordX, float coordY) {
        forAllWindowsDropMotionEventForTouchPad(coordX, coordY, true);
        return isShouldDropMotionEventForTouchPad();
    }

    public boolean hasLighterViewInPCCastMode() {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(getDisplayId())) {
            SlogEx.d(TAG_WM, "hasLighterViewInPCCastMode not in PC cast mode");
            return false;
        }
        forAllWindowsLighterViewInPcCastMode(true);
        return isHasLighterViewInPcCastMode();
    }
}
