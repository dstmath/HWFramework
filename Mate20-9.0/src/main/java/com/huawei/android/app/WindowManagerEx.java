package com.huawei.android.app;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import android.view.IHwRotateObserver;
import android.view.IWindowLayoutObserver;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
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
    private static final Singleton<IWindowManager> gDefault = new Singleton<IWindowManager>() {
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
            Log.e(LOG_TAG, "ret: " + ret);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "isInputMethodVisible", e);
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
            Log.d(LOG_TAG, "isTopIsFullscreen: ret: " + ret);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "isTopIsFullscreen", e);
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
        switch (type) {
            case 0:
                getDefault().getInitialDisplaySize(displayId, size);
                return;
            case 1:
                getDefault().getBaseDisplaySize(displayId, size);
                return;
            default:
                return;
        }
    }

    public static int getPendingAppTransition() throws RemoteException {
        return getDefault().getPendingAppTransition();
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
        switch (type) {
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            default:
                return -1;
        }
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
            Log.e(LOG_TAG, "registerDockedStackListener failed", e);
        }
    }

    public static boolean isKeyguardLocked() {
        try {
            return getDefault().isKeyguardLocked();
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "isKeyguardLocked failed", e);
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
                    if (data != null) {
                        data.recycle();
                    }
                    if (reply != null) {
                        reply.recycle();
                    }
                    return null;
                }
                char[] chars = new char[(value.length * 2)];
                for (int i = 0; i < value.length; i++) {
                    int b = value[i];
                    chars[i * 2] = HEXDIGITS[(b & 240) >> 4];
                    chars[(i * 2) + 1] = HEXDIGITS[b & 15];
                }
                String windowCKInfo = new String(chars);
                Log.d(LOG_TAG, "getWindowCKInfo value is " + windowCKInfo);
                if (data != null) {
                    data.recycle();
                }
                if (reply != null) {
                    reply.recycle();
                }
                return windowCKInfo;
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "getWindowCKInfo exception is " + e.getMessage());
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
            Log.i(LOG_TAG, "getWindowCKInfo windowManagerBinder is null");
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
                Log.e(LOG_TAG, "registerWindowObserver exception is " + e.getMessage());
            } catch (Throwable th) {
                data.recycle();
                reply.recycle();
                throw th;
            }
            data.recycle();
            reply.recycle();
            return;
        }
        Log.w(LOG_TAG, "registerWindowObserver windowManagerBinder is null");
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
                Log.e(LOG_TAG, "unRegisterWindowObserver exception is " + e.getMessage());
            } catch (Throwable th) {
                data.recycle();
                reply.recycle();
                throw th;
            }
            data.recycle();
            reply.recycle();
            return;
        }
        Log.w(LOG_TAG, "unRegisterWindowObserver windowManagerBinder is null");
    }

    public static boolean isFullScreenDevice() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                return wm.isFullScreenDevice();
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "isFullScreenDevice RemoteException");
            }
        }
        return false;
    }

    public static float getDeviceMaxRatio() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                return wm.getDeviceMaxRatio();
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "getDeviceMaxRatio RemoteException");
            }
        }
        return 0.0f;
    }

    public static Rect getTopAppDisplayBounds(float appMaxRatio, int rotation) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                return wm.getTopAppDisplayBounds(appMaxRatio, rotation);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "getTopAppDisplayBounds RemoteException");
            }
        }
        return null;
    }

    public static void registerRotateObserver(IHwRotateObserver observer) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.registerRotateObserver(observer);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "registerRotateObserver RemoteException");
            }
        }
    }

    public static void unregisterRotateObserver(IHwRotateObserver observer) {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.unregisterRotateObserver(observer);
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "unregisterRotateObserver RemoteException");
            }
        }
    }

    public static List<String> getNotchSystemApps() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                return wm.getNotchSystemApps();
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "getSystemApps RemoteException");
            }
        }
        return null;
    }

    public static int getFocusWindowWidth() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                return wm.getFocusWindowWidth();
            } catch (RemoteException e) {
                Log.w(LOG_TAG, "getFocusWindowWidth RemoteException");
            }
        }
        return 0;
    }

    public static int getRestrictedScreenHeight() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                return wm.getRestrictedScreenHeight();
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "getRestrictedScreenHeight failed: catch RemoteException!");
            }
        }
        return 0;
    }

    public static boolean isWindowSupportKnuckle() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                return wm.isWindowSupportKnuckle();
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "isShotEnable failed: catch RemoteException!");
            }
        }
        return false;
    }

    public static boolean isNavigationBarVisible() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                return wm.isNavigationBarVisible();
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "isNavigationBarVisible failed: catch RemoteException!");
            }
        }
        return false;
    }

    public static void dismissKeyguardLw() {
        IHwWindowManager wm = HwWindowManager.getService();
        if (wm != null) {
            try {
                wm.dismissKeyguardLw();
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "dismissKeyguardLw failed: catch RemoteException!");
            }
        }
    }

    public static void lockNow(Bundle options) {
        try {
            WindowManagerGlobal.getWindowManagerService().lockNow(options);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "lockNow RemoteException");
        }
    }

    public static void setGestureNavMode(Context context, int leftMode, int rightMode, int bottomMode) {
        HwWindowManager.setGestureNavMode(context, leftMode, rightMode, bottomMode);
    }
}
