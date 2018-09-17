package com.huawei.android.app;

import android.graphics.Point;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import android.view.IWindowLayoutObserver;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.WindowManagerGlobal;
import com.huawei.android.view.IDockedStackListenerEx;
import com.huawei.facerecognition.FaceCamera;

public class WindowManagerEx {
    public static final int GET_DISPLAY_SIZE_TYPE_BASE = 1;
    public static final int GET_DISPLAY_SIZE_TYPE_INITIAL = 0;
    public static final int GET_DOCKED_TYPE_BOTTOM = 4;
    public static final int GET_DOCKED_TYPE_INVALID = -1;
    public static final int GET_DOCKED_TYPE_LEFT = 1;
    public static final int GET_DOCKED_TYPE_RIGHT = 3;
    public static final int GET_DOCKED_TYPE_TOP = 2;
    private static final char[] HEXDIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final int IS_INPUT_METHOD_VISIBLE_TOKEN = 1004;
    private static final int IS_TOP_FULL_SCREEN_TOKEN = 206;
    private static final String LOG_TAG = "WindowManagerEx";
    private static final Singleton<IWindowManager> gDefault = new Singleton<IWindowManager>() {
        protected IWindowManager create() {
            return Stub.asInterface(ServiceManager.getService("window"));
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
        switch (type) {
            case -1:
                return -1;
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
        return AppOpsManagerEx.TYPE_NET;
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00ac  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ba  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00c0  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String getWindowCKInfo(String defaultValue) {
        RemoteException e;
        Throwable th;
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        if (windowManagerBinder != null) {
            Parcel parcel = null;
            Parcel parcel2 = null;
            try {
                parcel = Parcel.obtain();
                parcel2 = Parcel.obtain();
                parcel.writeInterfaceToken("android.view.IWindowManager");
                windowManagerBinder.transact(FaceCamera.RET_REPEAT_REQUEST_FAILED, parcel, parcel2, 0);
                int[] value = new int[168];
                parcel2.readIntArray(value);
                if (value.length == 0) {
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    if (parcel2 != null) {
                        parcel2.recycle();
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
                try {
                    Log.d(LOG_TAG, "getWindowCKInfo value is " + windowCKInfo);
                    if (parcel != null) {
                        parcel.recycle();
                    }
                    if (parcel2 != null) {
                        parcel2.recycle();
                    }
                    return windowCKInfo;
                } catch (RemoteException e2) {
                    e = e2;
                    String str = windowCKInfo;
                    try {
                        Log.e(LOG_TAG, "getWindowCKInfo exception is " + e.getMessage());
                        if (parcel != null) {
                        }
                        if (parcel2 != null) {
                        }
                        return defaultValue;
                    } catch (Throwable th2) {
                        th = th2;
                        if (parcel != null) {
                            parcel.recycle();
                        }
                        if (parcel2 != null) {
                            parcel2.recycle();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    if (parcel != null) {
                    }
                    if (parcel2 != null) {
                    }
                    throw th;
                }
            } catch (RemoteException e3) {
                e = e3;
                Log.e(LOG_TAG, "getWindowCKInfo exception is " + e.getMessage());
                if (parcel != null) {
                    parcel.recycle();
                }
                if (parcel2 != null) {
                    parcel2.recycle();
                }
                return defaultValue;
            }
        }
        Log.i(LOG_TAG, "getWindowCKInfo windowManagerBinder is null");
        return defaultValue;
    }

    public static void registerWindowObserver(IWindowLayoutObserver observer, long period) {
        IBinder iBinder = null;
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        if (windowManagerBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.view.IWindowManager");
                if (observer != null) {
                    iBinder = observer.asBinder();
                }
                data.writeStrongBinder(iBinder);
                data.writeLong(period);
                windowManagerBinder.transact(1009, data, reply, 0);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "registerWindowObserver exception is " + e.getMessage());
            } finally {
                data.recycle();
                reply.recycle();
            }
            return;
        }
        Log.w(LOG_TAG, "registerWindowObserver windowManagerBinder is null");
    }

    public static void unRegisterWindowObserver(IWindowLayoutObserver observer) {
        IBinder iBinder = null;
        IBinder windowManagerBinder = WindowManagerGlobal.getWindowManagerService().asBinder();
        if (windowManagerBinder != null) {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken("android.view.IWindowManager");
                if (observer != null) {
                    iBinder = observer.asBinder();
                }
                data.writeStrongBinder(iBinder);
                windowManagerBinder.transact(PackageManagerEx.TRANSACTION_CODE_SET_HDB_KEY, data, reply, 0);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "unRegisterWindowObserver exception is " + e.getMessage());
            } finally {
                data.recycle();
                reply.recycle();
            }
            return;
        }
        Log.w(LOG_TAG, "unRegisterWindowObserver windowManagerBinder is null");
    }
}
