package com.huawei.android.app;

import android.app.ActivityManager;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.IMWThirdpartyCallback.Stub;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.BuildEx.VERSION;
import com.huawei.android.os.HwTransCodeEx;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HwMultiWindowEx {
    private static final String TAG = HwMultiWindowEx.class.getSimpleName();
    private static final int VERSION_NO = 1;
    private static Method sGetMultiWinFrameMethod = null;
    private static Method sGetServiceMethod;
    private static Method sIsInMultiWindowModeMethod = null;
    private static boolean sIsMWSupported = false;
    private static Method sIsMultiWinMethod = null;
    private static final Object sLock = new Object();
    private static ThirdpartyCallBackHandler sMWCallBackHandler;
    private static Object sMultiWinService;
    private static Method sRegisterThirdPartyCallBackMethod = null;
    private static boolean sRegistered = false;
    private static Class<?> sServiceManagerClazz;
    private static List<StateChangeListener> sStateChangeListeners;
    private static Method sUnregisterThirdPartyCallBackMethod = null;

    public interface StateChangeListener {
        void onModeChanged(boolean z);

        void onSizeChanged();

        void onZoneChanged();
    }

    private static class ThirdpartyCallBackHandler extends Stub {
        /* synthetic */ ThirdpartyCallBackHandler(ThirdpartyCallBackHandler -this0) {
            this();
        }

        private ThirdpartyCallBackHandler() {
        }

        public void onModeChanged(boolean aMWStatus) {
            for (StateChangeListener lListener : HwMultiWindowEx.sStateChangeListeners) {
                lListener.onModeChanged(aMWStatus);
            }
        }

        public void onZoneChanged() {
            for (StateChangeListener lListener : HwMultiWindowEx.sStateChangeListeners) {
                lListener.onZoneChanged();
            }
        }

        public void onSizeChanged() {
            for (StateChangeListener lListener : HwMultiWindowEx.sStateChangeListeners) {
                lListener.onSizeChanged();
            }
        }
    }

    static {
        initDeclaredMethods();
    }

    private static void initDeclaredMethods() {
        Class<?>[] isMultiWinArgs = new Class[]{Integer.TYPE};
        Class<?>[] getMultiWinFrameArgs = new Class[]{Integer.TYPE, Rect.class};
        Class<?>[] mMWCallBackArgs = new Class[]{IMWThirdpartyCallback.class};
        try {
            Method asInterface = Class.forName("android.os.IMultiWinService$Stub").getMethod("asInterface", new Class[]{IBinder.class});
            sServiceManagerClazz = Class.forName("android.os.ServiceManager");
            sGetServiceMethod = sServiceManagerClazz.getMethod("getService", new Class[]{String.class});
            Object[] objArr = new Object[1];
            objArr[0] = sGetServiceMethod.invoke(sServiceManagerClazz, new Object[]{"multiwin"});
            sMultiWinService = asInterface.invoke(null, objArr);
            if (sMultiWinService != null) {
                sIsMWSupported = true;
                Class<?> clazz = sMultiWinService.getClass();
                sIsMultiWinMethod = clazz.getDeclaredMethod("isPartOfMultiWindow", isMultiWinArgs);
                sIsInMultiWindowModeMethod = clazz.getDeclaredMethod("getMWMaintained", (Class[]) null);
                sGetMultiWinFrameMethod = clazz.getDeclaredMethod("getMultiWinFrameByTaskID", getMultiWinFrameArgs);
                sRegisterThirdPartyCallBackMethod = clazz.getDeclaredMethod("registerThirdPartyCallBack", mMWCallBackArgs);
                sUnregisterThirdPartyCallBackMethod = clazz.getDeclaredMethod("unregisterThirdPartyCallBack", mMWCallBackArgs);
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "initDeclaredMethods failed:" + e.toString());
        } catch (NoSuchMethodException e2) {
            Log.e(TAG, "initDeclaredMethods failed:" + e2.toString());
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "initDeclaredMethods failed:" + e3.toString());
        } catch (IllegalArgumentException e4) {
            Log.e(TAG, "initDeclaredMethods failed:" + e4.toString());
        } catch (InvocationTargetException e5) {
            Log.e(TAG, "initDeclaredMethods failed:" + e5.toString());
        } catch (NullPointerException e6) {
            Log.e(TAG, "initDeclaredMethods failed:" + e6.toString());
        }
    }

    public static boolean isMultiWin(int aTaskID) {
        if (isInMultiWindowMode()) {
            Method method = sIsMultiWinMethod;
            if (method != null) {
                try {
                    return ((Boolean) method.invoke(sMultiWinService, new Object[]{Integer.valueOf(aTaskID)})).booleanValue();
                } catch (IllegalAccessException e) {
                    Log.d(TAG, "call method " + method.getName() + " failed !!!");
                } catch (IllegalArgumentException e2) {
                    Log.d(TAG, "call method " + method.getName() + " failed !!!");
                } catch (InvocationTargetException e3) {
                    Log.d(TAG, "call method " + method.getName() + " failed !!!");
                }
            }
        }
        return false;
    }

    public static boolean isInMultiWindowMode() {
        if (VERSION.EMUI_SDK_INT >= 11) {
            return isInMultiWindowMode_N();
        }
        Method method = sIsInMultiWindowModeMethod;
        if (method != null) {
            try {
                return ((Boolean) method.invoke(sMultiWinService, (Object[]) null)).booleanValue();
            } catch (IllegalAccessException e) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            } catch (IllegalArgumentException e2) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            } catch (InvocationTargetException e3) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            }
        }
        return false;
    }

    public static Rect getMultiWinFrame(int aTaskID) {
        Rect lFrame = new Rect();
        Method method = sGetMultiWinFrameMethod;
        if (method != null) {
            try {
                method.invoke(sMultiWinService, new Object[]{Integer.valueOf(aTaskID), lFrame});
            } catch (IllegalAccessException e) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            } catch (IllegalArgumentException e2) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            } catch (InvocationTargetException e3) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            }
        }
        return lFrame;
    }

    public static boolean isMultiWindowSupported() {
        return sIsMWSupported;
    }

    public static boolean setStateChangeListener(StateChangeListener aStateChangeListener) {
        if (VERSION.EMUI_SDK_INT >= 11) {
            return setStateChangeListener_N(aStateChangeListener);
        }
        boolean lListenerAdded = false;
        if (isMultiWindowSupported()) {
            synchronized (sLock) {
                if (sStateChangeListeners == null) {
                    sStateChangeListeners = new ArrayList();
                }
                if (!sStateChangeListeners.contains(aStateChangeListener)) {
                    lListenerAdded = sStateChangeListeners.add(aStateChangeListener);
                }
            }
            if (!sRegistered) {
                sMWCallBackHandler = new ThirdpartyCallBackHandler();
                sRegistered = registerThirdPartyCallBack(sMWCallBackHandler);
            }
        }
        return lListenerAdded;
    }

    public static boolean unregisterStateChangeListener(StateChangeListener aStateChangeListener) {
        if (VERSION.EMUI_SDK_INT >= 11) {
            return unregisterStateChangeListener_N(aStateChangeListener);
        }
        boolean lListenerRemoved = false;
        if (sStateChangeListeners != null) {
            lListenerRemoved = sStateChangeListeners.remove(aStateChangeListener);
            if (sStateChangeListeners.size() == 0 && sMWCallBackHandler != null) {
                unregisterThirdPartyCallBack(sMWCallBackHandler);
                sRegistered = false;
            }
        }
        return lListenerRemoved;
    }

    private static boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackReference) {
        Method method = sRegisterThirdPartyCallBackMethod;
        if (method != null) {
            try {
                return ((Boolean) method.invoke(sMultiWinService, new Object[]{aCallBackReference})).booleanValue();
            } catch (IllegalAccessException e) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            } catch (IllegalArgumentException e2) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            } catch (InvocationTargetException e3) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            }
        }
        return false;
    }

    private static boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackReference) {
        Method method = sUnregisterThirdPartyCallBackMethod;
        if (method != null) {
            try {
                return ((Boolean) method.invoke(sMultiWinService, new Object[]{aCallBackReference})).booleanValue();
            } catch (IllegalAccessException e) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            } catch (IllegalArgumentException e2) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            } catch (InvocationTargetException e3) {
                Log.d(TAG, "call method " + method.getName() + " failed !!!");
            }
        }
        return false;
    }

    private static boolean isInMultiWindowMode_N() {
        int ret = 0;
        Parcel parcel = null;
        Parcel parcel2 = null;
        try {
            parcel = Parcel.obtain();
            parcel2 = Parcel.obtain();
            parcel.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.IS_IN_MULTIWINDOW_MODE_TRANSACTION, parcel, parcel2, 0);
            parcel2.readException();
            ret = parcel2.readInt();
            Log.d(TAG, "isInMultiWindowMode ret: " + ret);
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Remote exception happened", e);
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (Throwable th) {
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        }
        if (ret > 0) {
            return true;
        }
        return false;
    }

    private static boolean setStateChangeListener_N(StateChangeListener aStateChangeListener) {
        boolean lListenerAdded = false;
        synchronized (sLock) {
            if (sStateChangeListeners == null) {
                sStateChangeListeners = new ArrayList();
            }
            if (!sStateChangeListeners.contains(aStateChangeListener)) {
                lListenerAdded = sStateChangeListeners.add(aStateChangeListener);
            }
        }
        if (!sRegistered) {
            sMWCallBackHandler = new ThirdpartyCallBackHandler();
            sRegistered = registerThirdPartyCallBack_N(sMWCallBackHandler);
        }
        return lListenerAdded;
    }

    private static boolean unregisterStateChangeListener_N(StateChangeListener aStateChangeListener) {
        boolean lListenerRemoved = false;
        if (sStateChangeListeners != null) {
            lListenerRemoved = sStateChangeListeners.remove(aStateChangeListener);
            if (sStateChangeListeners.size() == 0 && sMWCallBackHandler != null) {
                unregisterThirdPartyCallBack_N(sMWCallBackHandler);
                sRegistered = false;
            }
        }
        return lListenerRemoved;
    }

    private static boolean registerThirdPartyCallBack_N(IMWThirdpartyCallback aCallBackReference) {
        IBinder iBinder = null;
        int ret = 0;
        Parcel parcel = null;
        Parcel parcel2 = null;
        try {
            parcel = Parcel.obtain();
            parcel2 = Parcel.obtain();
            parcel.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            if (aCallBackReference != null) {
                iBinder = aCallBackReference.asBinder();
            }
            parcel.writeStrongBinder(iBinder);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.REGISTER_THIRD_PARTY_CALLBACK_TRANSACTION, parcel, parcel2, 0);
            parcel2.readException();
            ret = parcel2.readInt();
            Log.d(TAG, "registerThirdPartyCallBack ret: " + ret);
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Remote exception happened", e);
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (Throwable th) {
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        }
        if (ret > 0) {
            return true;
        }
        return false;
    }

    private static boolean unregisterThirdPartyCallBack_N(IMWThirdpartyCallback aCallBackReference) {
        IBinder iBinder = null;
        int ret = 0;
        Parcel parcel = null;
        Parcel parcel2 = null;
        try {
            parcel = Parcel.obtain();
            parcel2 = Parcel.obtain();
            parcel.writeInterfaceToken(HwTransCodeEx.ACTIVITYMANAGER_DESCRIPTOR);
            if (aCallBackReference != null) {
                iBinder = aCallBackReference.asBinder();
            }
            parcel.writeStrongBinder(iBinder);
            ActivityManager.getService().asBinder().transact(HwTransCodeEx.UNREGISTER_THIRD_PARTY_CALLBACK_TRANSACTION, parcel, parcel2, 0);
            parcel2.readException();
            ret = parcel2.readInt();
            Log.d(TAG, "unregisterThirdPartyCallBack ret: " + ret);
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Remote exception happened", e);
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        } catch (Throwable th) {
            if (parcel != null) {
                parcel.recycle();
            }
            if (parcel2 != null) {
                parcel2.recycle();
            }
        }
        if (ret > 0) {
            return true;
        }
        return false;
    }
}
