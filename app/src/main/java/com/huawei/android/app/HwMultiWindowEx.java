package com.huawei.android.app;

import android.app.ActivityManagerNative;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.os.IMWThirdpartyCallback.Stub;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.BuildEx.VERSION;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HwMultiWindowEx {
    private static final int IS_IN_MULTIWINDOW_MODE_TRANSACTION = 3103;
    private static final int REGISTER_THIRD_PARTY_CALLBACK_TRANSACTION = 3101;
    private static final String TAG = null;
    private static final int UNREGISTER_THIRD_PARTY_CALLBACK_TRANSACTION = 3102;
    private static final int VERSION_NO = 1;
    private static Method sGetMultiWinFrameMethod;
    private static Method sGetServiceMethod;
    private static Method sIsInMultiWindowModeMethod;
    private static boolean sIsMWSupported;
    private static Method sIsMultiWinMethod;
    private static final Object sLock = null;
    private static ThirdpartyCallBackHandler sMWCallBackHandler;
    private static Object sMultiWinService;
    private static Method sRegisterThirdPartyCallBackMethod;
    private static boolean sRegistered;
    private static Class<?> sServiceManagerClazz;
    private static List<StateChangeListener> sStateChangeListeners;
    private static Method sUnregisterThirdPartyCallBackMethod;

    public interface StateChangeListener {
        void onModeChanged(boolean z);

        void onSizeChanged();

        void onZoneChanged();
    }

    private static class ThirdpartyCallBackHandler extends Stub {
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
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.app.HwMultiWindowEx.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.app.HwMultiWindowEx.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.app.HwMultiWindowEx.<clinit>():void");
    }

    private static void initDeclaredMethods() {
        Class<?>[] isMultiWinArgs = new Class[VERSION_NO];
        isMultiWinArgs[0] = Integer.TYPE;
        Class<?>[] getMultiWinFrameArgs = new Class[]{Integer.TYPE, Rect.class};
        Class<?>[] mMWCallBackArgs = new Class[VERSION_NO];
        mMWCallBackArgs[0] = IMWThirdpartyCallback.class;
        try {
            Class[] clsArr = new Class[VERSION_NO];
            clsArr[0] = IBinder.class;
            Method asInterface = Class.forName("android.os.IMultiWinService$Stub").getMethod("asInterface", clsArr);
            sServiceManagerClazz = Class.forName("android.os.ServiceManager");
            Class[] clsArr2 = new Class[VERSION_NO];
            clsArr2[0] = String.class;
            sGetServiceMethod = sServiceManagerClazz.getMethod("getService", clsArr2);
            Object[] objArr = new Object[VERSION_NO];
            Method method = sGetServiceMethod;
            Class cls = sServiceManagerClazz;
            Object[] objArr2 = new Object[VERSION_NO];
            objArr2[0] = "multiwin";
            objArr[0] = method.invoke(cls, objArr2);
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
                    Object obj = sMultiWinService;
                    Object[] objArr = new Object[VERSION_NO];
                    objArr[0] = Integer.valueOf(aTaskID);
                    return ((Boolean) method.invoke(obj, objArr)).booleanValue();
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
        boolean z = false;
        if (isMultiWindowSupported()) {
            synchronized (sLock) {
                if (sStateChangeListeners == null) {
                    sStateChangeListeners = new ArrayList();
                }
                if (!sStateChangeListeners.contains(aStateChangeListener)) {
                    z = sStateChangeListeners.add(aStateChangeListener);
                }
            }
            if (!sRegistered) {
                sMWCallBackHandler = new ThirdpartyCallBackHandler();
                sRegistered = registerThirdPartyCallBack(sMWCallBackHandler);
            }
        }
        return z;
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
                Object obj = sMultiWinService;
                Object[] objArr = new Object[VERSION_NO];
                objArr[0] = aCallBackReference;
                return ((Boolean) method.invoke(obj, objArr)).booleanValue();
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
                Object obj = sMultiWinService;
                Object[] objArr = new Object[VERSION_NO];
                objArr[0] = aCallBackReference;
                return ((Boolean) method.invoke(obj, objArr)).booleanValue();
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
            parcel.writeInterfaceToken("android.app.IActivityManager");
            ActivityManagerNative.getDefault().asBinder().transact(IS_IN_MULTIWINDOW_MODE_TRANSACTION, parcel, parcel2, 0);
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
            parcel.writeInterfaceToken("android.app.IActivityManager");
            if (aCallBackReference != null) {
                iBinder = aCallBackReference.asBinder();
            }
            parcel.writeStrongBinder(iBinder);
            ActivityManagerNative.getDefault().asBinder().transact(REGISTER_THIRD_PARTY_CALLBACK_TRANSACTION, parcel, parcel2, 0);
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
            parcel.writeInterfaceToken("android.app.IActivityManager");
            if (aCallBackReference != null) {
                iBinder = aCallBackReference.asBinder();
            }
            parcel.writeStrongBinder(iBinder);
            ActivityManagerNative.getDefault().asBinder().transact(UNREGISTER_THIRD_PARTY_CALLBACK_TRANSACTION, parcel, parcel2, 0);
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
