package com.android.server;

import android.app.job.JobService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.android.server.am.BroadcastDispatcher;
import com.android.server.am.HwBroadcastQueue;
import com.android.server.am.IHwActivityManagerInner;
import com.android.server.am.IHwActivityManagerServiceEx;
import com.android.server.am.IHwBroadcastQueueEx;
import com.android.server.audio.IHwAudioServiceEx;
import com.android.server.audio.IHwAudioServiceInner;
import com.android.server.connectivity.IHwConnectivityServiceInner;
import com.android.server.display.IHwColorFadeEx;
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
import com.android.server.notification.IHwNotificationManagerServiceEx;
import com.android.server.pm.IHwBackgroundDexOptInner;
import com.android.server.pm.IHwBackgroundDexOptServiceEx;
import com.android.server.pm.IHwPackageManagerInner;
import com.android.server.pm.IHwPackageManagerServiceEx;
import com.android.server.pm.IHwPluginPackage;
import com.android.server.pm.dex.IHwPackageDynamicCodeLoading;
import com.android.server.policy.IHwPhoneWindowManagerEx;
import com.android.server.policy.IHwPhoneWindowManagerInner;
import com.android.server.power.IHwPowerManagerInner;
import com.android.server.power.IHwPowerManagerServiceEx;
import com.android.server.statusbar.IHwStatusBarManagerServiceEx;
import com.android.server.usb.IHwUsbDeviceManagerEx;
import com.android.server.usb.IHwUsbDeviceManagerInner;
import com.android.server.usb.IHwUsbUserSettingsManagerEx;
import com.android.server.usb.IHwUsbUserSettingsManagerInner;
import com.android.server.wallpaper.IHwWallpaperManagerInner;
import com.android.server.wallpaper.IHwWallpaperManagerServiceEx;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.IHwActivityDisplayEx;
import com.android.server.wm.IHwActivityTaskManagerInner;
import com.android.server.wm.IHwActivityTaskManagerServiceEx;
import com.android.server.wm.IHwDisplayContentEx;
import com.android.server.wm.IHwRootActivityContainerInner;
import com.android.server.wm.IHwSingleHandContentEx;
import com.android.server.wm.IHwTaskPositionerEx;
import com.android.server.wm.IHwTaskSnapshotCacheEx;
import com.android.server.wm.IHwTaskStackEx;
import com.android.server.wm.IHwWindowManagerInner;
import com.android.server.wm.IHwWindowManagerServiceEx;
import com.android.server.wm.IHwWindowStateEx;
import com.android.server.wm.TaskStack;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowState;
import com.huawei.server.connectivity.IHwConnectivityServiceEx;
import com.huawei.server.media.projection.IHwMediaProjectionManagerServiceEx;
import com.huawei.server.wm.IHwActivityStackEx;
import com.huawei.server.wm.IHwActivityStackSupervisorEx;
import com.huawei.server.wm.IHwActivityStarterEx;
import com.huawei.server.wm.IHwDisplayPolicyEx;
import com.huawei.server.wm.IHwDisplayPolicyInner;
import com.huawei.server.wm.IHwDisplayRotationEx;
import com.huawei.server.wm.IHwRootActivityContainerEx;
import com.huawei.server.wm.IHwSingleHandAdapter;
import com.huawei.server.wm.IHwTaskLaunchParamsModifierEx;
import com.huawei.server.wm.IHwTaskRecordEx;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HwServiceExFactory {
    private static final Object LOCK = new Object();
    private static final String TAG = "HwServiceExFactory";
    private static Factory obj = null;

    public interface Factory {
        IHwActivityDisplayEx getHwActivityDisplayEx();

        IHwActivityManagerServiceEx getHwActivityManagerServiceEx(IHwActivityManagerInner iHwActivityManagerInner, Context context);

        IHwActivityStackEx getHwActivityStackEx(ActivityStack activityStack, ActivityTaskManagerService activityTaskManagerService);

        IHwActivityStackSupervisorEx getHwActivityStackSupervisorEx(ActivityTaskManagerService activityTaskManagerService);

        IHwActivityStarterEx getHwActivityStarterEx(ActivityTaskManagerService activityTaskManagerService);

        IHwActivityTaskManagerServiceEx getHwActivityTaskManagerServiceEx(IHwActivityTaskManagerInner iHwActivityTaskManagerInner, Context context);

        IHwAudioServiceEx getHwAudioServiceEx(IHwAudioServiceInner iHwAudioServiceInner, Context context);

        IHwBackgroundDexOptServiceEx getHwBackgroundDexOptServiceEx(IHwBackgroundDexOptInner iHwBackgroundDexOptInner, JobService jobService, Context context);

        IHwBluetoothManagerServiceEx getHwBluetoothManagerServiceEx(IHwBluetoothManagerInner iHwBluetoothManagerInner, Context context, Handler handler);

        IHwBroadcastQueueEx getHwBroadcastQueueEx(HwBroadcastQueue hwBroadcastQueue, BroadcastDispatcher broadcastDispatcher, String str);

        IHwColorFadeEx getHwColorFadeEx(Context context);

        IHwConnectivityServiceEx getHwConnectivityServiceEx(IHwConnectivityServiceInner iHwConnectivityServiceInner, Context context);

        IHwDisplayContentEx getHwDisplayContentEx();

        IHwDisplayManagerServiceEx getHwDisplayManagerServiceEx(IHwDisplayManagerInner iHwDisplayManagerInner, Context context);

        IHwDisplayPolicyEx getHwDisplayPolicyEx(WindowManagerService windowManagerService, IHwDisplayPolicyInner iHwDisplayPolicyInner, DisplayContent displayContent, Context context, boolean z);

        IHwDisplayPowerControllerEx getHwDisplayPowerControllerEx(Context context, IHwDisplayPowerControllerEx.Callbacks callbacks);

        IHwDisplayRotationEx getHwDisplayRotationEx(WindowManagerService windowManagerService, DisplayContent displayContent, boolean z);

        IHwInputManagerServiceEx getHwInputManagerServiceEx(IHwInputManagerInner iHwInputManagerInner, Context context);

        IHwInputMethodManagerServiceEx getHwInputMethodManagerServiceEx(IHwInputMethodManagerInner iHwInputMethodManagerInner, Context context);

        IHwMediaProjectionManagerServiceEx getHwMediaProjectionManagerServiceEx(IHwMediaProjectionManagerServiceInner iHwMediaProjectionManagerServiceInner, Context context);

        IHwNetworkStatsServiceEx getHwNetworkStatsServiceEx(IHwNetworkStatsInner iHwNetworkStatsInner, Context context);

        IHwNotificationManagerServiceEx getHwNotificationManagerServiceEx();

        IHwPackageDynamicCodeLoading getHwPackageDynamicCodeLoading();

        IHwPackageManagerServiceEx getHwPackageManagerServiceEx(IHwPackageManagerInner iHwPackageManagerInner, Context context);

        IHwPhoneWindowManagerEx getHwPhoneWindowManagerEx(IHwPhoneWindowManagerInner iHwPhoneWindowManagerInner, Context context);

        IHwPluginPackage getHwPluginPackage(IHwPackageManagerInner iHwPackageManagerInner, String str);

        IHwPowerManagerServiceEx getHwPowerManagerServiceEx(IHwPowerManagerInner iHwPowerManagerInner, Context context);

        IHwRootActivityContainerEx getHwRootActivityContainerEx(IHwRootActivityContainerInner iHwRootActivityContainerInner, ActivityTaskManagerService activityTaskManagerService);

        IHwSingleHandAdapter getHwSingleHandAdapter(Context context, Handler handler, Handler handler2, WindowManagerService windowManagerService);

        IHwSingleHandContentEx getHwSingleHandContentEx(WindowManagerService windowManagerService);

        IHwStatusBarManagerServiceEx getHwStatusBarManagerServiceEx();

        IHwStorageManagerServiceEx getHwStorageManagerServiceEx(IHwStorageManagerInner iHwStorageManagerInner, Context context);

        IHwTaskLaunchParamsModifierEx getHwTaskLaunchParamsModifierEx();

        IHwTaskPositionerEx getHwTaskPositionerEx(WindowManagerService windowManagerService);

        IHwTaskRecordEx getHwTaskRecordEx();

        IHwTaskSnapshotCacheEx getHwTaskSnapshotCacheEx();

        IHwTaskStackEx getHwTaskStackEx(TaskStack taskStack, WindowManagerService windowManagerService);

        IHwUsbDeviceManagerEx getHwUsbDeviceManagerEx(IHwUsbDeviceManagerInner iHwUsbDeviceManagerInner, Context context);

        IHwUsbUserSettingsManagerEx getHwUsbUserSettingsManagerEx(IHwUsbUserSettingsManagerInner iHwUsbUserSettingsManagerInner, Context context);

        IHwWallpaperManagerServiceEx getHwWallpaperManagerServiceEx(IHwWallpaperManagerInner iHwWallpaperManagerInner, Context context);

        IHwWindowManagerServiceEx getHwWindowManagerServiceEx(IHwWindowManagerInner iHwWindowManagerInner, Context context);

        IHwWindowStateEx getHwWindowStateEx(WindowManagerService windowManagerService, WindowState windowState);
    }

    private static Factory getImplObject() {
        synchronized (LOCK) {
            if (obj == null) {
                try {
                    obj = (Factory) Class.forName("com.android.server.HwServiceExFactoryImpl").newInstance();
                    Log.v(TAG, "get AllImpl object = " + obj);
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "ClassNotFoundException when getImplObject!");
                } catch (Exception e2) {
                    Log.e(TAG, ": reflection exception!");
                }
            }
        }
        return obj;
    }

    private static Object getHwInterfaceExProxy(Class<?>[] interfaces) {
        return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, new InvocationHandler() {
            /* class com.android.server.HwServiceExFactory.AnonymousClass1 */

            @Override // java.lang.reflect.InvocationHandler
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Class returnType = method.getReturnType();
                if (returnType == Integer.TYPE || returnType == Long.TYPE || returnType == Byte.TYPE || returnType == Character.TYPE || returnType == Short.TYPE || returnType == Double.TYPE) {
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
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwActivityManagerServiceEx(ams, context);
        }
        return (IHwActivityManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwActivityManagerServiceEx.class});
    }

    public static IHwWallpaperManagerServiceEx getHwWallpaperManagerServiceEx(IHwWallpaperManagerInner wms, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwWallpaperManagerServiceEx(wms, context);
        }
        return (IHwWallpaperManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwWallpaperManagerServiceEx.class});
    }

    public static IHwNotificationManagerServiceEx getHwNotificationManagerServiceEx() {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwNotificationManagerServiceEx();
        }
        return (IHwNotificationManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwNotificationManagerServiceEx.class});
    }

    public static IHwActivityTaskManagerServiceEx getHwActivityTaskManagerServiceEx(IHwActivityTaskManagerInner atms, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwActivityTaskManagerServiceEx(atms, context);
        }
        return (IHwActivityTaskManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwActivityTaskManagerServiceEx.class});
    }

    public static IHwWindowManagerServiceEx getHwWindowManagerServiceEx(IHwWindowManagerInner wms, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwWindowManagerServiceEx(wms, context);
        }
        return (IHwWindowManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwWindowManagerServiceEx.class});
    }

    public static IHwPackageManagerServiceEx getHwPackageManagerServiceEx(IHwPackageManagerInner pms, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwPackageManagerServiceEx(pms, context);
        }
        return (IHwPackageManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwPackageManagerServiceEx.class});
    }

    public static IHwPluginPackage getHwPluginPackage(IHwPackageManagerInner pms, String packageName) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwPluginPackage(pms, packageName);
        }
        return (IHwPluginPackage) getHwInterfaceExProxy(new Class[]{IHwPluginPackage.class});
    }

    public static IHwInputMethodManagerServiceEx getHwInputMethodManagerServiceEx(IHwInputMethodManagerInner ims, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwInputMethodManagerServiceEx(ims, context);
        }
        return (IHwInputMethodManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwInputMethodManagerServiceEx.class});
    }

    public static IHwBackgroundDexOptServiceEx getHwBackgroundDexOptServiceEx(IHwBackgroundDexOptInner bdos, JobService service, Context context) {
        Factory mObject = getImplObject();
        if (mObject != null) {
            return mObject.getHwBackgroundDexOptServiceEx(bdos, service, context);
        }
        return (IHwBackgroundDexOptServiceEx) getHwInterfaceExProxy(new Class[]{IHwBackgroundDexOptServiceEx.class});
    }

    public static IHwPowerManagerServiceEx getHwPowerManagerServiceEx(IHwPowerManagerInner pms, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwPowerManagerServiceEx(pms, context);
        }
        return (IHwPowerManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwPowerManagerServiceEx.class});
    }

    public static IHwActivityStarterEx getHwActivityStarterEx(ActivityTaskManagerService ams) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwActivityStarterEx(ams);
        }
        return (IHwActivityStarterEx) getHwInterfaceExProxy(new Class[]{IHwActivityStarterEx.class});
    }

    public static IHwAudioServiceEx getHwAudioServiceEx(IHwAudioServiceInner ias, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwAudioServiceEx(ias, context);
        }
        return (IHwAudioServiceEx) getHwInterfaceExProxy(new Class[]{IHwAudioServiceEx.class});
    }

    public static IHwInputManagerServiceEx getHwInputManagerServiceEx(IHwInputManagerInner ims, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwInputManagerServiceEx(ims, context);
        }
        return (IHwInputManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwInputManagerServiceEx.class});
    }

    public static IHwActivityStackSupervisorEx getHwActivityStackSupervisorEx(ActivityTaskManagerService ams) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwActivityStackSupervisorEx(ams);
        }
        return (IHwActivityStackSupervisorEx) getHwInterfaceExProxy(new Class[]{IHwActivityStackSupervisorEx.class});
    }

    public static IHwPhoneWindowManagerEx getHwPhoneWindowManagerEx(IHwPhoneWindowManagerInner pws, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwPhoneWindowManagerEx(pws, context);
        }
        return (IHwPhoneWindowManagerEx) getHwInterfaceExProxy(new Class[]{IHwPhoneWindowManagerEx.class});
    }

    public static IHwDisplayManagerServiceEx getHwDisplayManagerServiceEx(IHwDisplayManagerInner dms, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwDisplayManagerServiceEx(dms, context);
        }
        return (IHwDisplayManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwDisplayManagerServiceEx.class});
    }

    public static IHwTaskPositionerEx getHwTaskPositionerEx(WindowManagerService wms) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwTaskPositionerEx(wms);
        }
        return (IHwTaskPositionerEx) getHwInterfaceExProxy(new Class[]{IHwTaskPositionerEx.class});
    }

    public static IHwWindowStateEx getHwWindowStateEx(WindowManagerService wms, WindowState windowState) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwWindowStateEx(wms, windowState);
        }
        return (IHwWindowStateEx) getHwInterfaceExProxy(new Class[]{IHwWindowStateEx.class});
    }

    public static IHwTaskStackEx getHwTaskStackEx(TaskStack taskStack, WindowManagerService wms) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwTaskStackEx(taskStack, wms);
        }
        return (IHwTaskStackEx) getHwInterfaceExProxy(new Class[]{IHwTaskStackEx.class});
    }

    public static IHwTaskLaunchParamsModifierEx getHwTaskLaunchParamsModifierEx() {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwTaskLaunchParamsModifierEx();
        }
        return (IHwTaskLaunchParamsModifierEx) getHwInterfaceExProxy(new Class[]{IHwTaskLaunchParamsModifierEx.class});
    }

    public static IHwTaskRecordEx getHwTaskRecordEx() {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwTaskRecordEx();
        }
        return (IHwTaskRecordEx) getHwInterfaceExProxy(new Class[]{IHwTaskRecordEx.class});
    }

    public static IHwDisplayPolicyEx getHwDisplayPolicyEx(WindowManagerService service, IHwDisplayPolicyInner displayPolicy, DisplayContent displayContent, Context context, boolean isDefaultDisplay) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwDisplayPolicyEx(service, displayPolicy, displayContent, context, isDefaultDisplay);
        }
        return (IHwDisplayPolicyEx) getHwInterfaceExProxy(new Class[]{IHwDisplayPolicyEx.class});
    }

    public static IHwDisplayRotationEx getHwDisplayRotationEx(WindowManagerService service, DisplayContent displayContent, boolean isDefaultDisplay) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwDisplayRotationEx(service, displayContent, isDefaultDisplay);
        }
        return (IHwDisplayRotationEx) getHwInterfaceExProxy(new Class[]{IHwDisplayRotationEx.class});
    }

    public static IHwConnectivityServiceEx getHwConnectivityServiceEx(IHwConnectivityServiceInner csi, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwConnectivityServiceEx(csi, context);
        }
        return (IHwConnectivityServiceEx) getHwInterfaceExProxy(new Class[]{IHwConnectivityServiceEx.class});
    }

    public static IHwMediaProjectionManagerServiceEx getHwMediaProjectionManagerServiceEx(IHwMediaProjectionManagerServiceInner mpms, Context context) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwMediaProjectionManagerServiceEx(mpms, context);
        }
        return (IHwMediaProjectionManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwMediaProjectionManagerServiceEx.class});
    }

    public static IHwActivityStackEx getHwActivityStackEx(ActivityStack stack, ActivityTaskManagerService ams) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwActivityStackEx(stack, ams);
        }
        return (IHwActivityStackEx) getHwInterfaceExProxy(new Class[]{IHwActivityStarterEx.class});
    }

    public static IHwRootActivityContainerEx getHwRootActivityContainerEx(IHwRootActivityContainerInner rac, ActivityTaskManagerService service) {
        Factory factoryObj = getImplObject();
        if (factoryObj != null) {
            return factoryObj.getHwRootActivityContainerEx(rac, service);
        }
        return (IHwRootActivityContainerEx) getHwInterfaceExProxy(new Class[]{IHwRootActivityContainerEx.class});
    }

    public static IHwDisplayContentEx getHwDisplayContentEx() {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwDisplayContentEx();
        }
        return (IHwDisplayContentEx) getHwInterfaceExProxy(new Class[]{IHwDisplayContentEx.class});
    }

    public static IHwBroadcastQueueEx getHwBroadcastQueueEx(HwBroadcastQueue bq, BroadcastDispatcher bd, String queueName) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwBroadcastQueueEx(bq, bd, queueName);
        }
        return (IHwBroadcastQueueEx) getHwInterfaceExProxy(new Class[]{IHwBroadcastQueueEx.class});
    }

    public static IHwNetworkStatsServiceEx getHwNetworkStatsServiceEx(IHwNetworkStatsInner hwNetworkStatsInner, Context context) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwNetworkStatsServiceEx(hwNetworkStatsInner, context);
        }
        Object object = getHwInterfaceExProxy(new Class[]{IHwNetworkStatsServiceEx.class});
        if (object instanceof IHwNetworkStatsServiceEx) {
            return (IHwNetworkStatsServiceEx) object;
        }
        return null;
    }

    public static IHwBluetoothManagerServiceEx getHwBluetoothManagerServiceEx(IHwBluetoothManagerInner bms, Context context, Handler handler) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwBluetoothManagerServiceEx(bms, context, handler);
        }
        return (IHwBluetoothManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwBluetoothManagerServiceEx.class});
    }

    public static IHwDisplayPowerControllerEx getHwDisplayPowerControllerEx(Context context, IHwDisplayPowerControllerEx.Callbacks callbacks) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwDisplayPowerControllerEx(context, callbacks);
        }
        return (IHwDisplayPowerControllerEx) getHwInterfaceExProxy(new Class[]{IHwDisplayPowerControllerEx.class});
    }

    public static IHwTaskSnapshotCacheEx getHwTaskSnapshotCacheEx() {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwTaskSnapshotCacheEx();
        }
        return (IHwTaskSnapshotCacheEx) getHwInterfaceExProxy(new Class[]{IHwTaskSnapshotCacheEx.class});
    }

    public static IHwStorageManagerServiceEx getHwStorageManagerServiceEx(IHwStorageManagerInner sms, Context context) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwStorageManagerServiceEx(sms, context);
        }
        return (IHwStorageManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwStorageManagerServiceEx.class});
    }

    public static IHwUsbDeviceManagerEx getHwUsbDeviceManagerEx(IHwUsbDeviceManagerInner ums, Context context) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwUsbDeviceManagerEx(ums, context);
        }
        return (IHwUsbDeviceManagerEx) getHwInterfaceExProxy(new Class[]{IHwUsbDeviceManagerEx.class});
    }

    public static IHwActivityDisplayEx getHwActivityDisplayEx() {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwActivityDisplayEx();
        }
        return (IHwActivityDisplayEx) getHwInterfaceExProxy(new Class[]{IHwActivityDisplayEx.class});
    }

    public static IHwUsbUserSettingsManagerEx getHwUsbUserSettingsManagerEx(IHwUsbUserSettingsManagerInner hwUsbUserSettingsManagerInner, Context context) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwUsbUserSettingsManagerEx(hwUsbUserSettingsManagerInner, context);
        }
        return (IHwUsbUserSettingsManagerEx) getHwInterfaceExProxy(new Class[]{IHwUsbUserSettingsManagerEx.class});
    }

    public static IHwSingleHandContentEx getHwSingleHandContentEx(WindowManagerService service) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwSingleHandContentEx(service);
        }
        return (IHwSingleHandContentEx) getHwInterfaceExProxy(new Class[]{IHwSingleHandContentEx.class});
    }

    public static IHwSingleHandAdapter getHwSingleHandAdapter(Context context, Handler handler, Handler uiHandler, WindowManagerService service) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwSingleHandAdapter(context, handler, uiHandler, service);
        }
        return (IHwSingleHandAdapter) getHwInterfaceExProxy(new Class[]{IHwSingleHandAdapter.class});
    }

    public static IHwColorFadeEx getHwColorFadeEx(Context context) {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwColorFadeEx(context);
        }
        Object object = getHwInterfaceExProxy(new Class[]{IHwColorFadeEx.class});
        if (object instanceof IHwColorFadeEx) {
            return (IHwColorFadeEx) object;
        }
        return null;
    }

    public static IHwStatusBarManagerServiceEx getHwStatusBarManagerServiceEx() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwStatusBarManagerServiceEx();
        }
        return (IHwStatusBarManagerServiceEx) getHwInterfaceExProxy(new Class[]{IHwStatusBarManagerServiceEx.class});
    }

    public static IHwPackageDynamicCodeLoading getHwPackageDynamicCodeLoading() {
        Factory factory = getImplObject();
        if (factory != null) {
            return factory.getHwPackageDynamicCodeLoading();
        }
        return (IHwPackageDynamicCodeLoading) getHwInterfaceExProxy(new Class[]{IHwPackageDynamicCodeLoading.class});
    }
}
