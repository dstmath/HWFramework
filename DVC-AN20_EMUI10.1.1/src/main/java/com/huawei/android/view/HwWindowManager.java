package com.huawei.android.view;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.Singleton;
import android.view.DisplayCutout;
import android.view.DragEvent;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowInsets;
import com.huawei.android.view.IHwWindowManager;
import java.util.ArrayList;
import java.util.List;

public class HwWindowManager {
    private static final String CAST_PKG = "com.huawei.associateassistant";
    public static final int DISPLAY_AREA_TYPE_CUTOUT = 3;
    public static final int DISPLAY_AREA_TYPE_ROUND_CORNER = 1;
    public static final int DISPLAY_AREA_TYPE_SIDE = 2;
    public static final int DISPLAY_AREA_TYPE_UNION = 0;
    private static final Singleton<IHwWindowManager> IWindowManagerSingleton = new Singleton<IHwWindowManager>() {
        /* class com.huawei.android.view.HwWindowManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwWindowManager create() {
            try {
                IWindowManager wms = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
                if (wms == null) {
                    return null;
                }
                return IHwWindowManager.Stub.asInterface(wms.getHwInnerService());
            } catch (RemoteException e) {
                Log.e(HwWindowManager.TAG, "IHwWindowManager create() fail: " + e);
                return null;
            }
        }
    };
    public static final int NOTCH_MODE_ALWAYS = 1;
    private static final String TAG = "HwWindowManager";

    public static IHwWindowManager getService() {
        return IWindowManagerSingleton.get();
    }

    public static List<String> getNotchSystemApps() {
        try {
            return getService().getNotchSystemApps();
        } catch (RemoteException e) {
            Log.e(TAG, "getSystemApps failed " + e.getMessage());
            return null;
        }
    }

    public static boolean registerWMMonitorCallback(IHwWMDAMonitorCallback callback) {
        if (getService() == null) {
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            getService().registerWMMonitorCallback(callback);
            Binder.restoreCallingIdentity(token);
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "registerWMMonitorCallback catch RemoteException!");
            Binder.restoreCallingIdentity(token);
            return false;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public static List<Bundle> getVisibleWindows(int ops) {
        if (getService() == null) {
            return null;
        }
        long token = Binder.clearCallingIdentity();
        try {
            return getService().getVisibleWindows(ops);
        } catch (RemoteException e) {
            Log.e(TAG, "getVisibleWindows catch RemoteException!");
            return null;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static int getFocusWindowWidth() {
        try {
            return getService().getFocusWindowWidth();
        } catch (RemoteException e) {
            Log.e(TAG, "getFocusWindowWidth failed " + e.getMessage());
            return 0;
        }
    }

    public static void startNotifyWindowFocusChange() {
        try {
            getService().startNotifyWindowFocusChange();
        } catch (RemoteException e) {
            Log.e(TAG, "startNotifyWindowFocusChange failed " + e.getMessage());
        }
    }

    public static void stopNotifyWindowFocusChange() {
        try {
            getService().stopNotifyWindowFocusChange();
        } catch (RemoteException e) {
            Log.e(TAG, "stopNotifyWindowFocusChange failed " + e.getMessage());
        }
    }

    public static void setAppWindowExitInfo(Bundle bundle, Bitmap iconBitmap) {
        try {
            getService().setAppWindowExitInfo(bundle, iconBitmap);
        } catch (RemoteException e) {
            Log.e(TAG, "setExitInfo failed " + e.getMessage());
        }
    }

    public static void getCurrFocusedWinInExtDisplay(Bundle outBundle) {
        try {
            getService().getCurrFocusedWinInExtDisplay(outBundle);
        } catch (RemoteException e) {
            Log.e(TAG, "getCurrFocusedWinInExtDisplay failed " + e.getMessage());
        }
    }

    public static boolean hasLighterViewInPCCastMode() {
        try {
            return getService().hasLighterViewInPCCastMode();
        } catch (RemoteException e) {
            Log.e(TAG, "hasLighterViewInPCCastMode failed " + e.getMessage());
            return false;
        }
    }

    public static boolean shouldDropMotionEventForTouchPad(float x, float y) {
        try {
            return getService().shouldDropMotionEventForTouchPad(x, y);
        } catch (RemoteException e) {
            Log.e(TAG, "shouldDropMotionEventForTouchPad failed " + e.getMessage());
            return false;
        }
    }

    public static HwTaskSnapshotWrapper getForegroundTaskSnapshotWrapper(boolean refresh) {
        try {
            return getService().getForegroundTaskSnapshotWrapper(refresh);
        } catch (RemoteException e) {
            Log.e(TAG, "getForegroundTaskSnapshotWrapper", e);
            return null;
        }
    }

    public static void setCoverManagerState(boolean isCoverOpen) {
        try {
            getService().setCoverManagerState(isCoverOpen);
        } catch (RemoteException e) {
            Log.e(TAG, "setCoverManagerState failed " + e.getMessage());
        }
    }

    public static void freezeOrThawRotation(int rotation) {
        try {
            getService().freezeOrThawRotation(rotation);
        } catch (RemoteException e) {
            Log.e(TAG, "freezeOrThawRotation failed " + e.getMessage());
        }
    }

    public static Bitmap getDisplayBitmap(int displayId, int width, int height) {
        try {
            return getService().getDisplayBitmap(displayId, width, height);
        } catch (RemoteException e) {
            Log.e(TAG, "getDisplayBitmap failed " + e.getMessage());
            return null;
        }
    }

    public static void setGestureNavMode(Context context, int leftMode, int rightMode, int bottomMode) {
        try {
            getService().setGestureNavMode(context.getPackageName(), leftMode, rightMode, bottomMode);
        } catch (RemoteException e) {
            Log.e(TAG, "setGestureNavMode failed", e);
        }
    }

    public static void setForcedDisplayDensityAndSize(int displayId, int density, int width, int height) {
        try {
            getService().setForcedDisplayDensityAndSize(displayId, density, width, height);
        } catch (RemoteException e) {
            Log.e(TAG, "setForcedDisplayDensityAndSize failed", e);
        }
    }

    public static boolean dragStartForMultiDisplay(ClipData clipData) {
        try {
            return getService().dragStartForMultiDisplay(clipData);
        } catch (RemoteException e) {
            Log.e(TAG, "dragStartForMultiDisplay failed.");
            return false;
        }
    }

    public static boolean dropStartForMultiDisplay(DragEvent dragEvent, DragEvent dragEventLoc, View view) {
        if (dragEvent == null || view == null) {
            return false;
        }
        ClipDescription clpDescription = dragEvent.getClipDescription();
        if (clpDescription == null) {
            Log.e(TAG, "dropStartForMultiDisplay clipDescription null point");
            return false;
        }
        CharSequence lable = clpDescription.getLabel();
        if (lable == null) {
            Log.e(TAG, "dropStartForMultiDisplay lable null point");
            return false;
        }
        String clipDescription = lable.toString();
        if (clipDescription == null || "windowscast".equals(clipDescription)) {
            boolean result = view.dispatchDragEvent(DragEvent.obtain(2, dragEventLoc.getX(), dragEventLoc.getY(), dragEvent.getLocalState(), dragEvent.getClipDescription(), null, null, true));
            if (!result) {
                return result;
            }
            try {
                Log.d(TAG, "dropStartForMultiDisplay droppable is true, dropstart called");
                return getService().dropStartForMultiDisplay(dragEvent);
            } catch (RemoteException e) {
                Log.e(TAG, "dropStartForMultiDisplay remote failed.");
                return false;
            }
        } else {
            Log.d(TAG, "dropStartForMultiDisplay is notify drop or drag start is not associateassistant");
            return false;
        }
    }

    public static boolean setDragStartBitmap(View.DragShadowBuilder shadowBuilder, Point shadowSize) {
        if (!HwPCUtils.isInWindowsCastMode()) {
            Log.e(TAG, "setDragStartBitmap check not in window cast mode.");
            return false;
        }
        Bitmap bitmap = Bitmap.createBitmap(shadowSize.x, shadowSize.y, Bitmap.Config.ARGB_8888);
        shadowBuilder.onDrawShadow(new Canvas(bitmap));
        try {
            boolean dragStartBitmap = getService().setDragStartBitmap(bitmap);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return dragStartBitmap;
        } catch (RemoteException e) {
            Log.e(TAG, "dragStartForMultiDisplay failed.");
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return false;
        } catch (Throwable th) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            throw th;
        }
    }

    public static void setDroppableForMultiDisplay(float x, float y, boolean result) {
        try {
            getService().setDroppableForMultiDisplay(x, y, result);
        } catch (RemoteException e) {
            Log.e(TAG, "setDroppableResultForMultiDisplay failed.");
        }
    }

    public static boolean isNeedExceptDisplaySide(Rect exceptDisplayRect) {
        try {
            return getService().isNeedExceptDisplaySide(exceptDisplayRect);
        } catch (RemoteException e) {
            Log.e(TAG, "isNeedExceptDisplaySide remote exception.");
            return false;
        }
    }

    public static boolean isAppNeedExpand(String pkgName) {
        try {
            if (getService() != null) {
                return getService().isAppNeedExpand(pkgName);
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "isAppNeedExpand remote exception.");
            return true;
        }
    }

    private static boolean isSwitched() {
        try {
            if (getService() != null) {
                return getService().isSwitched();
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "isSwitched remote exception.");
            return false;
        }
    }

    private static boolean needRestore(Context context) {
        if (!isSwitched()) {
            return false;
        }
        if (!"com.huawei.associateassistant".equals(getDragSrcPkgName()) && !"com.huawei.associateassistant".equals(context.getBasePackageName())) {
            return true;
        }
        if (!"com.huawei.associateassistant".equals(getDragSrcPkgName()) || !"com.huawei.associateassistant".equals(context.getBasePackageName())) {
            return false;
        }
        return true;
    }

    public static void checkAndRestoreShadow(Context context) {
        if (needRestore(context)) {
            try {
                if (getService() != null) {
                    getService().restoreShadow();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "checkAndRestoreShadow remote exception.");
            }
        }
    }

    public static boolean shouldSwitchDrag(Context context) {
        String name = getDragSrcPkgName();
        if (name == null || !"com.huawei.associateassistant".equals(name) || "com.huawei.associateassistant".equals(context.getBasePackageName())) {
            return false;
        }
        return true;
    }

    public static String getDragSrcPkgName() {
        try {
            if (getService() != null) {
                return getService().getDragSrcPkgName();
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "getDragSrcPkgName remote exception.");
            return null;
        }
    }

    private static Rect getUnionSafeInsets(WindowInsets windowInsets) {
        Rect sideDisplayInsets;
        Rect roundCornerInsets;
        DisplayCutout displayCutout;
        Rect displayCutoutInsets = null;
        if (!(windowInsets == null || (displayCutout = windowInsets.getDisplayCutout()) == null)) {
            displayCutoutInsets = displayCutout.getSafeInsets();
        }
        try {
            roundCornerInsets = getService().getSafeInsets(1);
            sideDisplayInsets = getService().getSafeInsets(2);
        } catch (RemoteException e) {
            Log.e(TAG, "fetchNewDatafromServer error");
            roundCornerInsets = null;
            sideDisplayInsets = null;
        }
        List<Rect> tmpList = new ArrayList<>(3);
        if (displayCutoutInsets != null) {
            tmpList.add(displayCutoutInsets);
        }
        if (roundCornerInsets != null) {
            tmpList.add(roundCornerInsets);
        }
        if (sideDisplayInsets != null) {
            tmpList.add(sideDisplayInsets);
        }
        if (tmpList.isEmpty()) {
            return new Rect(0, 0, 0, 0);
        }
        int maxLeft = 0;
        int maxTop = 0;
        int maxRight = 0;
        int maxBottom = 0;
        for (Rect rect : tmpList) {
            if (maxLeft < rect.left) {
                maxLeft = rect.left;
            }
            if (maxTop < rect.top) {
                maxTop = rect.top;
            }
            if (maxRight < rect.right) {
                maxRight = rect.right;
            }
            if (maxBottom < rect.bottom) {
                maxBottom = rect.bottom;
            }
        }
        return new Rect(maxLeft, maxTop, maxRight, maxBottom);
    }

    public static Rect getSafeInsets(int type, WindowInsets windowInsets) {
        DisplayCutout displayCutout;
        if (type == 0) {
            return getUnionSafeInsets(windowInsets);
        }
        if (type == 1 || type == 2) {
            try {
                Rect safeInsets = getService().getSafeInsets(type);
                if (safeInsets == null) {
                    return new Rect(0, 0, 0, 0);
                }
                return safeInsets;
            } catch (RemoteException e) {
                Log.e(TAG, "getSafeInsetsByType error");
                return null;
            }
        } else if (type != 3) {
            return new Rect(0, 0, 0, 0);
        } else {
            if (windowInsets == null || (displayCutout = windowInsets.getDisplayCutout()) == null) {
                return new Rect(0, 0, 0, 0);
            }
            return displayCutout.getSafeInsets();
        }
    }

    public static List<Rect> getBounds(int type, WindowInsets windowInsets) {
        DisplayCutout displayCutout;
        if (type == 1 || type == 2) {
            try {
                List<Rect> unSafeBounds = getService().getBounds(type);
                if (unSafeBounds == null) {
                    return new ArrayList<>();
                }
                return unSafeBounds;
            } catch (RemoteException e) {
                Log.e(TAG, "getBoundsByType error");
                return null;
            }
        } else if (type != 3) {
            return new ArrayList();
        } else {
            if (windowInsets == null || (displayCutout = windowInsets.getDisplayCutout()) == null) {
                return new ArrayList();
            }
            return displayCutout.getBoundingRects();
        }
    }

    public static void registerHwMultiDisplayDragStateListener(IHwMultiDisplayDragStateListener listener) {
        if (getService() == null) {
            Log.e(TAG, "getService return null.");
            return;
        }
        try {
            getService().registerHwMultiDisplayDragStateListener(listener);
        } catch (RemoteException e) {
            Log.e(TAG, "registerHwMultiDisplayDragStateListener failed.");
        }
    }

    public static void unregisterHwMultiDisplayDragStateListener() {
        if (getService() == null) {
            Log.e(TAG, "getService return null.");
            return;
        }
        try {
            getService().unregisterHwMultiDisplayDragStateListener();
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterHwMultiDisplayDragStateListener failed.");
        }
    }

    public static void setOriginalDropPoint(float x, float y) {
        try {
            getService().setOriginalDropPoint(x, y);
        } catch (RemoteException e) {
            Log.e(TAG, "setOriginalDropPoint failed.");
        }
    }

    public static void updateDragState(int dragState) {
        if (getService() == null) {
            Log.e(TAG, "updateDragState getService return null.");
            return;
        }
        try {
            getService().updateDragState(dragState);
        } catch (RemoteException e) {
            Log.e(TAG, "updateDragState failed.");
        }
    }

    public static int getTopActivityAdaptNotchState(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return 0;
        }
        try {
            return getService().getTopActivityAdaptNotchState(packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "getTopActivityAdapteNotchState RemoteException");
            return 0;
        }
    }

    public static void setAboveAppWindowsContainersVisible(boolean isVisible) {
        try {
            getService().setAboveAppWindowsContainersVisible(isVisible);
        } catch (RemoteException e) {
            Log.e(TAG, "setAboveAppWindowsContainersVisible RemoteException");
        }
    }
}
