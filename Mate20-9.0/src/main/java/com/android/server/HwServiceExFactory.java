package com.android.server;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.IHwActivityManagerInner;
import com.android.server.am.IHwActivityManagerServiceEx;
import com.android.server.audio.IHwAudioServiceEx;
import com.android.server.audio.IHwAudioServiceInner;
import com.android.server.connectivity.IHwConnectivityServiceInner;
import com.android.server.display.IHwDisplayManagerInner;
import com.android.server.display.IHwDisplayManagerServiceEx;
import com.android.server.display.IHwDisplayPowerControllerEx;
import com.android.server.imm.IHwInputMethodManagerInner;
import com.android.server.imm.IHwInputMethodManagerServiceEx;
import com.android.server.input.IHwInputManagerInner;
import com.android.server.input.IHwInputManagerServiceEx;
import com.android.server.media.projection.IHwMediaProjectionManagerServiceInner;
import com.android.server.net.IHwNetworkStatsInner;
import com.android.server.net.IHwNetworkStatsServiceEx;
import com.android.server.pm.IHwBackgroundDexOptInner;
import com.android.server.pm.IHwBackgroundDexOptServiceEx;
import com.android.server.pm.IHwPackageManagerInner;
import com.android.server.pm.IHwPackageManagerServiceEx;
import com.android.server.policy.IHwPhoneWindowManagerEx;
import com.android.server.policy.IHwPhoneWindowManagerInner;
import com.android.server.power.IHwPowerManagerInner;
import com.android.server.power.IHwPowerManagerServiceEx;
import com.android.server.vr.IHwVrManagerServiceEx;
import com.android.server.wm.IHwTaskPositionerEx;
import com.android.server.wm.IHwTaskStackEx;
import com.android.server.wm.IHwWindowManagerInner;
import com.android.server.wm.IHwWindowManagerServiceEx;
import com.android.server.wm.IHwWindowStateEx;
import com.android.server.wm.TaskStack;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import com.huawei.server.am.IHwActivityStackSupervisorEx;
import com.huawei.server.am.IHwActivityStarterEx;
import com.huawei.server.am.IHwTaskLaunchParamsModifierEx;
import com.huawei.server.am.IHwTaskRecordEx;
import com.huawei.server.connectivity.IHwConnectivityServiceEx;
import com.huawei.server.media.projection.IHwMediaProjectionManagerServiceEx;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HwServiceExFactory {
    private static final String TAG = "HwServiceExFactory";
    private static final Object mLock = new Object();
    private static Factory obj = null;

    public interface Factory {
        IHwActivityManagerServiceEx getHwActivityManagerServiceEx(IHwActivityManagerInner iHwActivityManagerInner, Context context);

        IHwActivityStackSupervisorEx getHwActivityStackSupervisorEx(ActivityManagerService activityManagerService);

        IHwActivityStarterEx getHwActivityStarterEx(ActivityManagerService activityManagerService);

        IHwAudioServiceEx getHwAudioServiceEx(IHwAudioServiceInner iHwAudioServiceInner, Context context);

        IHwBackgroundDexOptServiceEx getHwBackgroundDexOptServiceEx(IHwBackgroundDexOptInner iHwBackgroundDexOptInner, Context context);

        IHwConnectivityServiceEx getHwConnectivityServiceEx(IHwConnectivityServiceInner iHwConnectivityServiceInner, Context context);

        IHwDisplayManagerServiceEx getHwDisplayManagerServiceEx(IHwDisplayManagerInner iHwDisplayManagerInner, Context context);

        IHwDisplayPowerControllerEx getHwDisplayPowerControllerEx(Context context, IHwDisplayPowerControllerEx.Callbacks callbacks, SensorManager sensorManager);

        IHwInputManagerServiceEx getHwInputManagerServiceEx(IHwInputManagerInner iHwInputManagerInner, Context context);

        IHwInputMethodManagerServiceEx getHwInputMethodManagerServiceEx(IHwInputMethodManagerInner iHwInputMethodManagerInner, Context context);

        IHwMediaProjectionManagerServiceEx getHwMediaProjectionManagerServiceEx(IHwMediaProjectionManagerServiceInner iHwMediaProjectionManagerServiceInner, Context context);

        IHwNetworkStatsServiceEx getHwNetworkStatsServiceEx(IHwNetworkStatsInner iHwNetworkStatsInner, Context context);

        IHwPackageManagerServiceEx getHwPackageManagerServiceEx(IHwPackageManagerInner iHwPackageManagerInner, Context context);

        IHwPhoneWindowManagerEx getHwPhoneWindowManagerEx(IHwPhoneWindowManagerInner iHwPhoneWindowManagerInner, Context context);

        IHwPowerManagerServiceEx getHwPowerManagerServiceEx(IHwPowerManagerInner iHwPowerManagerInner, Context context);

        IHwTaskLaunchParamsModifierEx getHwTaskLaunchParamsModifierEx();

        IHwTaskPositionerEx getHwTaskPositionerEx(WindowManagerService windowManagerService);

        IHwTaskRecordEx getHwTaskRecordEx();

        IHwTaskStackEx getHwTaskStackEx(TaskStack taskStack, WindowManagerService windowManagerService);

        IHwVrManagerServiceEx getHwVrManagerServiceEx();

        IHwWindowManagerServiceEx getHwWindowManagerServiceEx(IHwWindowManagerInner iHwWindowManagerInner, Context context);

        IHwWindowStateEx getHwWindowStateEx(WindowManagerService windowManagerService, WindowState windowState);
    }

    private static Factory getImplObject() {
        synchronized (mLock) {
            if (obj == null) {
                try {
                    obj = (Factory) Class.forName("com.android.server.HwServiceExFactoryImpl").newInstance();
                    Log.v(TAG, "get AllImpl object = " + obj);
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "ClassNotFoundException : " + e);
                } catch (Exception e2) {
                    Log.e(TAG, ": reflection exception is " + e2);
                }
            }
        }
        return obj;
    }

    private static Object getHwInterfaceExProxy(Class<?>[] interfaces) {
        return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Class returnType = method.getReturnType();
                if (returnType == Integer.TYPE || returnType == Long.TYPE || returnType == Byte.TYPE || returnType == Character.TYPE || returnType == Short.TYPE || returnType == Long.TYPE || returnType == Double.TYPE) {
                    return 0;
                }
                if (returnType == Float.TYPE) {
                    return Float.valueOf(0.0f);
                }
                if (returnType == Boolean.TYPE) {
                    return false;
                }
                return null;
            }
        });
    }

    public static IHwActivityManagerServiceEx getHwActivityManagerServiceEx(IHwActivityManagerInner ams, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwActivityManagerServiceEx(ams, context);
        }
        return (IHwActivityManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwActivityManagerServiceEx.class});
    }

    public static IHwWindowManagerServiceEx getHwWindowManagerServiceEx(IHwWindowManagerInner wms, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWindowManagerServiceEx(wms, context);
        }
        return (IHwWindowManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwWindowManagerServiceEx.class});
    }

    public static IHwPackageManagerServiceEx getHwPackageManagerServiceEx(IHwPackageManagerInner pms, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPackageManagerServiceEx(pms, context);
        }
        return (IHwPackageManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwPackageManagerServiceEx.class});
    }

    public static IHwInputMethodManagerServiceEx getHwInputMethodManagerServiceEx(IHwInputMethodManagerInner ims, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwInputMethodManagerServiceEx(ims, context);
        }
        return (IHwInputMethodManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwInputMethodManagerServiceEx.class});
    }

    public static IHwBackgroundDexOptServiceEx getHwBackgroundDexOptServiceEx(IHwBackgroundDexOptInner bdox, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwBackgroundDexOptServiceEx(bdox, context);
        }
        return (IHwBackgroundDexOptServiceEx) getHwInterfaceExProxy(new Class[]{IHwBackgroundDexOptServiceEx.class});
    }

    public static IHwPowerManagerServiceEx getHwPowerManagerServiceEx(IHwPowerManagerInner pms, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPowerManagerServiceEx(pms, context);
        }
        return (IHwPowerManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwPowerManagerServiceEx.class});
    }

    public static IHwActivityStarterEx getHwActivityStarterEx(ActivityManagerService ams) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwActivityStarterEx(ams);
        }
        return (IHwActivityStarterEx) getHwInterfaceExProxy(new Class[]{IHwActivityStarterEx.class});
    }

    public static IHwAudioServiceEx getHwAudioServiceEx(IHwAudioServiceInner ias, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAudioServiceEx(ias, context);
        }
        return (IHwAudioServiceEx) getHwInterfaceExProxy(new Class[]{IHwAudioServiceEx.class});
    }

    public static IHwInputManagerServiceEx getHwInputManagerServiceEx(IHwInputManagerInner ims, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwInputManagerServiceEx(ims, context);
        }
        return (IHwInputManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwInputManagerServiceEx.class});
    }

    public static IHwActivityStackSupervisorEx getHwActivityStackSupervisorEx(ActivityManagerService ams) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwActivityStackSupervisorEx(ams);
        }
        return (IHwActivityStackSupervisorEx) getHwInterfaceExProxy(new Class[]{IHwActivityStackSupervisorEx.class});
    }

    public static IHwPhoneWindowManagerEx getHwPhoneWindowManagerEx(IHwPhoneWindowManagerInner pws, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwPhoneWindowManagerEx(pws, context);
        }
        return (IHwPhoneWindowManagerEx) getHwInterfaceExProxy(new Class[]{IHwPhoneWindowManagerEx.class});
    }

    public static IHwDisplayManagerServiceEx getHwDisplayManagerServiceEx(IHwDisplayManagerInner dms, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwDisplayManagerServiceEx(dms, context);
        }
        return (IHwDisplayManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwDisplayManagerServiceEx.class});
    }

    public static IHwTaskPositionerEx getHwTaskPositionerEx(WindowManagerService wms) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwTaskPositionerEx(wms);
        }
        return (IHwTaskPositionerEx) getHwInterfaceExProxy(new Class[]{IHwTaskPositionerEx.class});
    }

    public static IHwWindowStateEx getHwWindowStateEx(WindowManagerService wms, WindowState windowState) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwWindowStateEx(wms, windowState);
        }
        return (IHwWindowStateEx) getHwInterfaceExProxy(new Class[]{IHwWindowStateEx.class});
    }

    public static IHwTaskStackEx getHwTaskStackEx(TaskStack taskStack, WindowManagerService wms) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwTaskStackEx(taskStack, wms);
        }
        return (IHwTaskStackEx) getHwInterfaceExProxy(new Class[]{IHwTaskStackEx.class});
    }

    public static IHwTaskLaunchParamsModifierEx getHwTaskLaunchParamsModifierEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwTaskLaunchParamsModifierEx();
        }
        return (IHwTaskLaunchParamsModifierEx) getHwInterfaceExProxy(new Class[]{IHwTaskLaunchParamsModifierEx.class});
    }

    public static IHwTaskRecordEx getHwTaskRecordEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwTaskRecordEx();
        }
        return (IHwTaskRecordEx) getHwInterfaceExProxy(new Class[]{IHwTaskRecordEx.class});
    }

    public static IHwVrManagerServiceEx getHwVrManagerServiceEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwVrManagerServiceEx();
        }
        return (IHwVrManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwVrManagerServiceEx.class});
    }

    public static IHwConnectivityServiceEx getHwConnectivityServiceEx(IHwConnectivityServiceInner csi, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwConnectivityServiceEx(csi, context);
        }
        return (IHwConnectivityServiceEx) getHwInterfaceExProxy(new Class[]{IHwConnectivityServiceEx.class});
    }

    public static IHwMediaProjectionManagerServiceEx getHwMediaProjectionManagerServiceEx(IHwMediaProjectionManagerServiceInner mpms, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwMediaProjectionManagerServiceEx(mpms, context);
        }
        return (IHwMediaProjectionManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwMediaProjectionManagerServiceEx.class});
    }

    public static IHwNetworkStatsServiceEx getHwNetworkStatsServiceEx(IHwNetworkStatsInner nss, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwNetworkStatsServiceEx(nss, context);
        }
        return (IHwNetworkStatsServiceEx) getHwInterfaceExProxy(new Class[]{IHwNetworkStatsServiceEx.class});
    }

    public static IHwDisplayPowerControllerEx getHwDisplayPowerControllerEx(Context context, IHwDisplayPowerControllerEx.Callbacks callbacks, SensorManager sensorManager) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwDisplayPowerControllerEx(context, callbacks, sensorManager);
        }
        return (IHwDisplayPowerControllerEx) getHwInterfaceExProxy(new Class[]{IHwDisplayPowerControllerEx.class});
    }
}
