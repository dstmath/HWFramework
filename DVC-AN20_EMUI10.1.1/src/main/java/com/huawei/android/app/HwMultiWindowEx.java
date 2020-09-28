package com.huawei.android.app;

import android.graphics.Rect;
import android.os.IBinder;
import android.os.IMWThirdpartyCallback;
import android.util.Log;
import com.huawei.android.os.BuildEx;
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

    static {
        initDeclaredMethods();
    }

    private static void initDeclaredMethods() {
        Class<?>[] isMultiWinArgs = {Integer.TYPE};
        Class<?>[] getMultiWinFrameArgs = {Integer.TYPE, Rect.class};
        Class<?>[] mMWCallBackArgs = {IMWThirdpartyCallback.class};
        try {
            Method asInterface = Class.forName("android.os.IMultiWinService$Stub").getMethod("asInterface", IBinder.class);
            sServiceManagerClazz = Class.forName("android.os.ServiceManager");
            sGetServiceMethod = sServiceManagerClazz.getMethod("getService", String.class);
            sMultiWinService = asInterface.invoke(null, sGetServiceMethod.invoke(sServiceManagerClazz, "multiwin"));
            if (sMultiWinService != null) {
                sIsMWSupported = true;
                Class<?> clazz = sMultiWinService.getClass();
                sIsMultiWinMethod = clazz.getDeclaredMethod("isPartOfMultiWindow", isMultiWinArgs);
                sIsInMultiWindowModeMethod = clazz.getDeclaredMethod("getMWMaintained", null);
                sGetMultiWinFrameMethod = clazz.getDeclaredMethod("getMultiWinFrameByTaskID", getMultiWinFrameArgs);
                sRegisterThirdPartyCallBackMethod = clazz.getDeclaredMethod("registerThirdPartyCallBack", mMWCallBackArgs);
                sUnregisterThirdPartyCallBackMethod = clazz.getDeclaredMethod("unregisterThirdPartyCallBack", mMWCallBackArgs);
            }
        } catch (ClassNotFoundException e) {
            String str = TAG;
            Log.e(str, "initDeclaredMethods failed:" + e.toString());
        } catch (NoSuchMethodException e2) {
            String str2 = TAG;
            Log.e(str2, "initDeclaredMethods failed:" + e2.toString());
        } catch (IllegalAccessException e3) {
            String str3 = TAG;
            Log.e(str3, "initDeclaredMethods failed:" + e3.toString());
        } catch (IllegalArgumentException e4) {
            String str4 = TAG;
            Log.e(str4, "initDeclaredMethods failed:" + e4.toString());
        } catch (InvocationTargetException e5) {
            String str5 = TAG;
            Log.e(str5, "initDeclaredMethods failed:" + e5.toString());
        }
    }

    public static boolean isMultiWin(int aTaskID) {
        Method method;
        if (isInMultiWindowMode() && (method = sIsMultiWinMethod) != null) {
            try {
                return ((Boolean) method.invoke(sMultiWinService, Integer.valueOf(aTaskID))).booleanValue();
            } catch (IllegalAccessException e) {
                String str = TAG;
                Log.d(str, "call method " + method.getName() + " failed !!!");
            } catch (IllegalArgumentException e2) {
                String str2 = TAG;
                Log.d(str2, "call method " + method.getName() + " failed !!!");
            } catch (InvocationTargetException e3) {
                String str3 = TAG;
                Log.d(str3, "call method " + method.getName() + " failed !!!");
            }
        }
        return false;
    }

    public static boolean isInMultiWindowMode() {
        if (BuildEx.VERSION.EMUI_SDK_INT >= 11) {
            return isInMultiWindowMode_N();
        }
        Method method = sIsInMultiWindowModeMethod;
        if (method == null) {
            return false;
        }
        try {
            return ((Boolean) method.invoke(sMultiWinService, null)).booleanValue();
        } catch (IllegalAccessException e) {
            String str = TAG;
            Log.d(str, "call method " + method.getName() + " failed !!!");
            return false;
        } catch (IllegalArgumentException e2) {
            String str2 = TAG;
            Log.d(str2, "call method " + method.getName() + " failed !!!");
            return false;
        } catch (InvocationTargetException e3) {
            String str3 = TAG;
            Log.d(str3, "call method " + method.getName() + " failed !!!");
            return false;
        }
    }

    public static Rect getMultiWinFrame(int aTaskID) {
        Rect lFrame = new Rect();
        Method method = sGetMultiWinFrameMethod;
        if (method != null) {
            try {
                method.invoke(sMultiWinService, Integer.valueOf(aTaskID), lFrame);
            } catch (IllegalAccessException e) {
                String str = TAG;
                Log.d(str, "call method " + method.getName() + " failed !!!");
            } catch (IllegalArgumentException e2) {
                String str2 = TAG;
                Log.d(str2, "call method " + method.getName() + " failed !!!");
            } catch (InvocationTargetException e3) {
                String str3 = TAG;
                Log.d(str3, "call method " + method.getName() + " failed !!!");
            }
        }
        return lFrame;
    }

    public static boolean isMultiWindowSupported() {
        return sIsMWSupported;
    }

    public static boolean setStateChangeListener(StateChangeListener aStateChangeListener) {
        if (BuildEx.VERSION.EMUI_SDK_INT >= 11) {
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
        ThirdpartyCallBackHandler thirdpartyCallBackHandler;
        if (BuildEx.VERSION.EMUI_SDK_INT >= 11) {
            return unregisterStateChangeListener_N(aStateChangeListener);
        }
        boolean lListenerRemoved = false;
        List<StateChangeListener> list = sStateChangeListeners;
        if (list != null) {
            lListenerRemoved = list.remove(aStateChangeListener);
            if (sStateChangeListeners.size() == 0 && (thirdpartyCallBackHandler = sMWCallBackHandler) != null) {
                unregisterThirdPartyCallBack(thirdpartyCallBackHandler);
                sRegistered = false;
            }
        }
        return lListenerRemoved;
    }

    private static boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackReference) {
        Method method = sRegisterThirdPartyCallBackMethod;
        if (method != null) {
            try {
                return ((Boolean) method.invoke(sMultiWinService, aCallBackReference)).booleanValue();
            } catch (IllegalAccessException e) {
                String str = TAG;
                Log.d(str, "call method " + method.getName() + " failed !!!");
            } catch (IllegalArgumentException e2) {
                String str2 = TAG;
                Log.d(str2, "call method " + method.getName() + " failed !!!");
            } catch (InvocationTargetException e3) {
                String str3 = TAG;
                Log.d(str3, "call method " + method.getName() + " failed !!!");
            }
        }
        return false;
    }

    private static boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackReference) {
        Method method = sUnregisterThirdPartyCallBackMethod;
        if (method != null) {
            try {
                return ((Boolean) method.invoke(sMultiWinService, aCallBackReference)).booleanValue();
            } catch (IllegalAccessException e) {
                String str = TAG;
                Log.d(str, "call method " + method.getName() + " failed !!!");
            } catch (IllegalArgumentException e2) {
                String str2 = TAG;
                Log.d(str2, "call method " + method.getName() + " failed !!!");
            } catch (InvocationTargetException e3) {
                String str3 = TAG;
                Log.d(str3, "call method " + method.getName() + " failed !!!");
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static class ThirdpartyCallBackHandler extends IMWThirdpartyCallback.Stub {
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

    private static boolean isInMultiWindowMode_N() {
        return HwActivityTaskManager.isInMultiWindowMode();
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
        ThirdpartyCallBackHandler thirdpartyCallBackHandler;
        boolean lListenerRemoved = false;
        List<StateChangeListener> list = sStateChangeListeners;
        if (list != null) {
            lListenerRemoved = list.remove(aStateChangeListener);
            if (sStateChangeListeners.size() == 0 && (thirdpartyCallBackHandler = sMWCallBackHandler) != null) {
                unregisterThirdPartyCallBack_N(thirdpartyCallBackHandler);
                sRegistered = false;
            }
        }
        return lListenerRemoved;
    }

    private static boolean registerThirdPartyCallBack_N(IMWThirdpartyCallback aCallBackReference) {
        return HwActivityTaskManager.registerThirdPartyCallBack(aCallBackReference);
    }

    private static boolean unregisterThirdPartyCallBack_N(IMWThirdpartyCallback aCallBackReference) {
        return HwActivityTaskManager.unregisterThirdPartyCallBack(aCallBackReference);
    }
}
