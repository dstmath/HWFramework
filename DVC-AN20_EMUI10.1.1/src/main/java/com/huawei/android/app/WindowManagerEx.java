package com.huawei.android.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import android.view.DragEvent;
import android.view.IHwRotateObserver;
import android.view.IWindowLayoutObserver;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import android.widget.RemoteViews;
import com.huawei.android.util.HwPCUtilsEx;
import com.huawei.android.view.HwWindowManager;
import com.huawei.android.view.IDockedStackListenerEx;
import com.huawei.android.view.IHwWindowManager;
import java.util.List;

public class WindowManagerEx {
    public static final int GESTURE_NAV_MODE_DEFAULT = 0;
    public static final int GESTURE_NAV_MODE_FORCE_DISABLE = 2;
    public static final int GESTURE_NAV_MODE_FORCE_ENABLE = 1;
    public static final int GET_DISPLAY_SIZE_TYPE_BASE = 1;
    public static final int GET_DISPLAY_SIZE_TYPE_INITIAL = 0;
    public static final int GET_DOCKED_TYPE_BOTTOM = 4;
    public static final int GET_DOCKED_TYPE_INVALID = -1;
    public static final int GET_DOCKED_TYPE_LEFT = 1;
    public static final int GET_DOCKED_TYPE_RIGHT = 3;
    public static final int GET_DOCKED_TYPE_TOP = 2;
    private static final char[] HEXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final int IS_INPUT_METHOD_VISIBLE_TOKEN = 1004;
    private static final int IS_TOP_FULL_SCREEN_TOKEN = 206;
    private static final String LOG_TAG = "WindowManagerEx";
    private static final int TRANSACTION_GET_PENDING_APP_TRANSITION = 1040;
    private static final Singleton<IWindowManager> gDefault = new Singleton<IWindowManager>() {
        /* class com.huawei.android.app.WindowManagerEx.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public IWindowManager create() {
            return IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        }
    };

    public static boolean isInputMethodVisible() throws RemoteException {
        int ret = 0;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            getDefault().asBinder().transact(1004, data, reply, 0);
            reply.readException();
            ret = reply.readInt();
            Log.e("WindowManagerEx", "ret: " + ret);
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "isInputMethodVisible", e);
        }
        if (ret > 0) {
            return true;
        }
        return false;
    }

    public static boolean isTopFullscreen() {
        int ret = 0;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.view.IWindowManager");
            getDefault().asBinder().transact(IS_TOP_FULL_SCREEN_TOKEN, data, reply, 0);
            ret = reply.readInt();
            Log.d("WindowManagerEx", "isTopIsFullscreen: ret: " + ret);
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "isTopIsFullscreen", e);
        }
        if (ret > 0) {
            return true;
        }
        return false;
    }

    private static IWindowManager getDefault() {
        return (IWindowManager) gDefault.get();
    }

    public static void getDisplaySize(int type, int displayId, Point size) throws RemoteException {
        if (type == 0) {
            getDefault().getInitialDisplaySize(displayId, size);
        } else if (type == 1) {
            getDefault().getBaseDisplaySize(displayId, size);
        }
    }

    public static int getPendingAppTransition() throws RemoteException {
        int result = 0;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("android.view.IWindowManager");
            getDefault().asBinder().transact(TRANSACTION_GET_PENDING_APP_TRANSITION, data, reply, 0);
            reply.readException();
            result = reply.readInt();
            Log.i("WindowManagerEx", "getPendingAppTransition result : " + result);
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "getPendingAppTransition catch RemoteException");
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return result;
    }

    public static void executeAppTransition() throws RemoteException {
        getDefault().executeAppTransition();
    }

    public static final int getDockedStackSide() throws RemoteException {
        return getDefault().getDockedStackSide();
    }

    public static final int getDockedStackSideConstant(int type) {
        if (type == -1) {
            return -1;
        }
        if (type == 1) {
            return 1;
        }
        if (type == 2) {
            return 2;
        }
        if (type == 3) {
            return 3;
        }
        if (type != 4) {
            return -1;
        }
        return 4;
    }

    public static void setAnimationScale(int which, float scale) throws RemoteException {
        getDefault().setAnimationScale(which, scale);
    }

    public static float getAnimationScale(int which) throws RemoteException {
        return getDefault().getAnimationScale(which);
    }

    public static void registerDockedStackListener(IDockedStackListenerEx listener) {
        try {
            getDefault().registerDockedStackListener(listener.getDockedStackListener());
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "registerDockedStackListener failed", e);
        }
    }

    public static boolean isKeyguardLocked() {
        try {
            return getDefault().isKeyguardLocked();
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "isKeyguardLocked failed", e);
            return false;
        }
    }

    public static int getHideNaviFlag() {
        return Integer.MIN_VALUE;
    }

    public static String getWindowCKInfo(String defaultValue) {
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        if (windowManagerBinder != null) {
            Parcel data = null;
            Parcel reply = null;
            try {
                data = Parcel.obtain();
                reply = Parcel.obtain();
                data.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(HwPCUtilsEx.FORCED_PC_DISPLAY_SIZE_OVERSCAN_MODE, data, reply, 0);
                int[] value = new int[168];
                reply.readIntArray(value);
                if (value.length == 0) {
                    data.recycle();
                    reply.recycle();
                    return null;
                }
                char[] chars = new char[(value.length * 2)];
                for (int i = 0; i < value.length; i++) {
                    int b = value[i];
                    chars[i * 2] = HEXDIGITS[(b & 240) >> 4];
                    chars[(i * 2) + 1] = HEXDIGITS[b & 15];
                }
                String windowCKInfo = new String(chars);
                Log.d("WindowManagerEx", "getWindowCKInfo value is " + windowCKInfo);
                data.recycle();
                reply.recycle();
                return windowCKInfo;
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "getWindowCKInfo exception is " + e.getMessage());
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
            } catch (Throwable th) {
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
                throw th;
            }
        } else {
            Log.i("WindowManagerEx", "getWindowCKInfo windowManagerBinder is null");
            return defaultValue;
        }
    }

    public static void registerWindowObserver(IWindowLayoutObserver observer, long period) {
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        if (windowManagerBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.view.IWindowManager");
                data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                data.writeLong(period);
                windowManagerBinder.transact(1009, data, reply, 0);
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "registerWindowObserver exception is " + e.getMessage());
            } catch (Throwable th) {
                data.recycle();
                reply.recycle();
                throw th;
            }
            data.recycle();
            reply.recycle();
            return;
        }
        Log.w("WindowManagerEx", "registerWindowObserver windowManagerBinder is null");
    }

    public static void unRegisterWindowObserver(IWindowLayoutObserver observer) {
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        if (windowManagerBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.view.IWindowManager");
                data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                windowManagerBinder.transact(PackageManagerEx.TRANSACTION_CODE_SET_HDB_KEY, data, reply, 0);
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "unRegisterWindowObserver exception is " + e.getMessage());
            } catch (Throwable th) {
                data.recycle();
                reply.recycle();
                throw th;
            }
            data.recycle();
            reply.recycle();
            return;
        }
        Log.w("WindowManagerEx", "unRegisterWindowObserver windowManagerBinder is null");
    }

    public static void updateAppView(RemoteViews remoteViews) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.updateAppView(remoteViews);
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "updateAppView RemoteException");
            }
        }
    }

    public static void removeAppView() {
        removeAppView(true);
    }

    public static void removeAppView(boolean isNeedAddBtnView) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.removeAppView(isNeedAddBtnView);
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "removeAppView RemoteException");
            }
        }
    }

    public static boolean isFullScreenDevice() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm == null) {
            return false;
        }
        try {
            return wm.isFullScreenDevice();
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "isFullScreenDevice RemoteException");
            return false;
        }
    }

    public static float getDeviceMaxRatio() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm == null) {
            return 0.0f;
        }
        try {
            return wm.getDeviceMaxRatio();
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "getDeviceMaxRatio RemoteException");
            return 0.0f;
        }
    }

    public static Rect getTopAppDisplayBounds(float appMaxRatio, int rotation) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm == null) {
            return null;
        }
        try {
            return wm.getTopAppDisplayBounds(appMaxRatio, rotation);
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "getTopAppDisplayBounds RemoteException");
            return null;
        }
    }

    public static void registerRotateObserver(IHwRotateObserver observer) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.registerRotateObserver(observer);
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "registerRotateObserver RemoteException");
            }
        }
    }

    public static void unregisterRotateObserver(IHwRotateObserver observer) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.unregisterRotateObserver(observer);
            } catch (RemoteException e) {
                Log.w("WindowManagerEx", "unregisterRotateObserver RemoteException");
            }
        }
    }

    public static List<String> getNotchSystemApps() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm == null) {
            return null;
        }
        try {
            return wm.getNotchSystemApps();
        } catch (RemoteException e) {
            Log.w("WindowManagerEx", "getSystemApps RemoteException");
            return null;
        }
    }

    public static int getFocusWindowWidth() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm == null) {
            return 0;
        }
        try {
            return wm.getFocusWindowWidth();
        } catch (RemoteException e) {
            Log.w("WindowManagerEx", "getFocusWindowWidth RemoteException");
            return 0;
        }
    }

    public static int getRestrictedScreenHeight() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm == null) {
            return 0;
        }
        try {
            return wm.getRestrictedScreenHeight();
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "getRestrictedScreenHeight failed: catch RemoteException!");
            return 0;
        }
    }

    public static boolean isWindowSupportKnuckle() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm == null) {
            return false;
        }
        try {
            return wm.isWindowSupportKnuckle();
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "isShotEnable failed: catch RemoteException!");
            return false;
        }
    }

    public static boolean isNavigationBarVisible() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm == null) {
            return false;
        }
        try {
            return wm.isNavigationBarVisible();
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "isNavigationBarVisible failed: catch RemoteException!");
            return false;
        }
    }

    public static void dismissKeyguardLw() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.dismissKeyguardLw();
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "dismissKeyguardLw failed: catch RemoteException!");
            }
        }
    }

    public static void lockNow(Bundle options) {
        try {
            WindowManagerGlobal.getWindowManagerService().lockNow(options);
        } catch (RemoteException e) {
            Log.w("WindowManagerEx", "lockNow RemoteException");
        }
    }

    public static void setAppWindowExitInfo(Bundle bundle, Bitmap iconBitmap) {
        HwWindowManager.setAppWindowExitInfo(bundle, iconBitmap);
    }

    public static void setGestureNavMode(Context context, int leftMode, int rightMode, int bottomMode) {
        HwWindowManager.setGestureNavMode(context, leftMode, rightMode, bottomMode);
    }

    public static String getTouchedWinPackageName(float x, float y, int displayId) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm == null) {
            return "";
        }
        try {
            return wm.getTouchedWinPackageName(x, y, displayId);
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "getTouchedWinState failed: catch RemoteException!");
            return "";
        }
    }

    public static void notifyDragAndDropForMultiDisplay(float x, float y, int displayId, DragEvent evt) {
        if (evt == null) {
            Log.e("WindowManagerEx", "notifyDragAndDropForMultiDisplay evt is null.");
            return;
        }
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.notifyDragAndDropForMultiDisplay(x, y, displayId, evt);
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "notifyDragAndDropForMultiDisplay failed: catch RemoteException!");
            }
        }
    }

    public static void registerDragListenerForMultiDisplay(HwMultiDisplayDragListenerEx listener) {
        if (listener == null) {
            Log.e("WindowManagerEx", "registerDragListenerForMultiDisplay listener is null.");
            return;
        }
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.registerDragListenerForMultiDisplay(listener.getInnerListener());
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "registerDragListenerForMultiDisplay failed: catch RemoteException!");
            }
        }
    }

    public static void unregisterDragListenerForMultiDisplay() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.unregisterDragListenerForMultiDisplay();
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "unregisterDragListenerForMultiDisplay failed: catch RemoteException!");
            }
        }
    }

    public static void registerDropListenerForMultiDisplay(HwMultiDisplayDropListenerEx listener) {
        if (listener == null) {
            Log.e("WindowManagerEx", "registerDropListenerForMultiDisplay listener is null.");
            return;
        }
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.registerDropListenerForMultiDisplay(listener.getInnerListener());
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "registerDropListenerForMultiDisplay failed: catch RemoteException!");
            }
        }
    }

    public static void unregisterDropListenerForMultiDisplay() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.unregisterDropListenerForMultiDisplay();
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "unregisterDropListenerForMultiDisplay failed: catch RemoteException!");
            }
        }
    }

    public static void registerBitmapDragListenerForMultiDisplay(HwMultiDisplayBitmapDragListenerEx listener) {
        if (listener == null) {
            Log.e("WindowManagerEx", "registerDragListenerForMultiDisplay listener is null.");
            return;
        }
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.registerBitmapDragListenerForMultiDisplay(listener);
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "registerDragListenerForMultiDisplayBitmap failed: catch RemoteException!");
            }
        }
    }

    public static void unregisterBitmapDragListenerForMultiDisplay() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.unregisterBitmapDragListenerForMultiDisplay();
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "unregisterDragListenerForMultiDisplay failed: catch RemoteException!");
            }
        }
    }

    @Deprecated
    public static void isViewDroppableForMultiDisplay(float x, float y, int displayId, HwMultiDisplayDroppableListenerEx listener) {
        Log.i("WindowManagerEx", "interface is deprecated， pls refer WindowManagerEx.isViewDroppableForMultiDisplay(int displayId,DragEvent dv, HwMultiDisplayDroppableListenerEx listener)");
    }

    @Deprecated
    public static void isViewDroppableForMultiDisplay(int displayId, DragEvent dv, HwMultiDisplayDroppableListenerEx listener) {
        if (listener == null) {
            Log.e("WindowManagerEx", "isViewDroppableForMultiDisplay listener is null.");
            return;
        }
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.registerIsDroppableForMultiDisplay(listener.getInnerListener());
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "isViewDroppableForMultiDisplay failed: catch RemoteException!");
                return;
            }
        }
        notifyDragAndDropForMultiDisplay(dv.getX(), dv.getY(), displayId, dv);
    }

    public static boolean isViewDroppableForMultiDisplayWithResult(int displayId, DragEvent dv, HwMultiDisplayDroppableListenerEx listener) {
        if (listener == null) {
            Log.e("WindowManagerEx", "isViewDroppableForMultiDisplay listener is null.");
            return false;
        }
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.registerIsDroppableForMultiDisplay(listener.getInnerListener());
                try {
                    return wm.notifyDragAndDropForMultiDisplay(dv.getX(), dv.getY(), displayId, dv);
                } catch (RemoteException e) {
                    Log.e("WindowManagerEx", "notifyDragAndDropForMultiDisplay failed: catch RemoteException!");
                }
            } catch (RemoteException e2) {
                Log.e("WindowManagerEx", "isViewDroppableForMultiDisplay failed: catch RemoteException!");
                return false;
            }
        }
        return false;
    }

    public static boolean isRotationFrozen() {
        try {
            return getDefault().isRotationFrozen();
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "isRotationFrozen RemoteException");
            return true;
        }
    }

    public static void freezeRotation(int rotation) {
        try {
            getDefault().freezeRotation(rotation);
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "freezeRotation RemoteException");
        }
    }

    public static void thawRotation() {
        try {
            getDefault().thawRotation();
        } catch (RemoteException e) {
            Log.e("WindowManagerEx", "thawRotation RemoteException");
        }
    }

    public static void updateFocusWindowFreezed(Boolean isGainFocus) {
        if (HwWindowManager.getService() != null) {
            try {
                HwWindowManager.getService().updateFocusWindowFreezed(isGainFocus.booleanValue());
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "updateFocusWindowFreezed RemoteException");
            }
        }
    }

    public static void registerPhoneOperateListenerForHwMultiDisplay(HwMultiDisplayPhoneOperateListenerEx listener) {
        if (listener == null) {
            Log.e("WindowManagerEx", "registerHwMultiDisplayPhoneOperateListener, null.listener");
            return;
        }
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.registerPhoneOperateListenerForHwMultiDisplay(listener.getInnerListener());
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "registerHwMultiDisplayPhoneOperateListener failed: catch RemoteException!");
            }
        }
    }

    public static void unregisterPhoneOperateListenerForHwMultiDisplay() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.unregisterPhoneOperateListenerForHwMultiDisplay();
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "unregisterPhoneOperateListenerForHwMultiDisplay failed: catch RemoteException!");
            }
        }
    }

    public static void setAboveAppWindowsContainersVisible(boolean isVisible) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.setAboveAppWindowsContainersVisible(isVisible);
            } catch (RemoteException e) {
                Log.e("WindowManagerEx", "setAboveAppWindowsContainersVisible failed: catch RemoteException!");
            }
        }
    }
}
