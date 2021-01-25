package com.huawei.dmsdp.devicevirtualization;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import com.huawei.dmsdpsdk2.HwLog;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DvKit {
    private static final int API_LEVEL = 1;
    private static final String DMSDP_PACKAGE_NAME = "com.huawei.dmsdp";
    public static final String LOCAL_VIRTUAL_DEVICE_CLASS = "LocalVirtualDeviceManager";
    private static final String TAG = "DvKit";
    public static final String VIRTUAL_CAMERA_SERVICE = "VirtualCameraManager";
    public static final String VIRTUAL_DEVICE_CLASS = "VirtualDeviceManager";
    public static final String VIRTUAL_NOTIFICATION_SERVICE = "VirtualNotificationService";
    public static final String VIRTUAL_SENSOR_SERVICE = "VirtualSensorService";
    public static final String VIRTUAL_VIBRATE_SERVICE = "VirtualVibrateService";
    private static DvKit mDvKit;
    private IDmsdpServiceCallback mDmsdpServiceCallback;
    private IDvKitConnectCallback mIDvKitConnectCallback;
    private Map<String, VirtualManager> mKitServices = new ConcurrentHashMap(0);
    private VirtualService mVirtualService;

    private DvKit() {
    }

    public static DvKit getInstance() {
        DvKit dvKit;
        synchronized (DvKit.class) {
            if (isDmsdpExist()) {
                if (mDvKit == null) {
                    mDvKit = new DvKit();
                }
                dvKit = mDvKit;
            } else {
                throw new NoClassDefFoundError();
            }
        }
        return dvKit;
    }

    public int connect(Context context, IDvKitConnectCallback callback) {
        HwLog.i(TAG, "start connect");
        if (context == null || callback == null) {
            HwLog.e(TAG, "param is invalid");
            return -2;
        }
        this.mIDvKitConnectCallback = callback;
        if (this.mDmsdpServiceCallback != null) {
            return 0;
        }
        this.mDmsdpServiceCallback = new IDmsdpServiceCallback() {
            /* class com.huawei.dmsdp.devicevirtualization.DvKit.AnonymousClass1 */

            @Override // com.huawei.dmsdp.devicevirtualization.IDmsdpServiceCallback
            public void onAdapterGet(VirtualService adapter) {
                HwLog.i(DvKit.TAG, "onAdapterGet:" + adapter);
                if (adapter != null) {
                    DvKit.this.mVirtualService = adapter;
                    if (DvKit.this.mIDvKitConnectCallback != null) {
                        DvKit.this.mIDvKitConnectCallback.onConnect(0);
                        return;
                    }
                    return;
                }
                HwLog.e(DvKit.TAG, "VirtualService is null");
                if (DvKit.this.mIDvKitConnectCallback != null) {
                    DvKit.this.mIDvKitConnectCallback.onConnect(1);
                    DvKit.this.mIDvKitConnectCallback = null;
                }
            }

            @Override // com.huawei.dmsdp.devicevirtualization.IDmsdpServiceCallback
            public void onBinderDied() {
                HwLog.i(DvKit.TAG, "onBinderDied");
                if (DvKit.this.mIDvKitConnectCallback != null) {
                    DvKit.this.mIDvKitConnectCallback.onDisconnect();
                    DvKit.this.mIDvKitConnectCallback = null;
                }
                DvKit.this.mDmsdpServiceCallback = null;
                if (DvKit.this.mVirtualService != null) {
                    for (Map.Entry<String, VirtualManager> entry : DvKit.this.mKitServices.entrySet()) {
                        entry.getValue().onDisConnect();
                    }
                    VirtualService unused = DvKit.this.mVirtualService;
                    VirtualService.releaseInstance();
                    HwLog.d(DvKit.TAG, "releaseInstance");
                    DvKit.this.mVirtualService = null;
                }
                DvKit.this.mKitServices.clear();
            }
        };
        VirtualService.createInstance(context, this.mDmsdpServiceCallback);
        return 0;
    }

    public void disConnect() {
        HwLog.i(TAG, "disConnect");
        this.mIDvKitConnectCallback = null;
        this.mDmsdpServiceCallback = null;
        for (Map.Entry<String, VirtualManager> entry : this.mKitServices.entrySet()) {
            entry.getValue().onDisConnect();
        }
        this.mKitServices.clear();
        if (this.mVirtualService != null) {
            VirtualService.releaseInstance();
            HwLog.d(TAG, "releaseInstance");
            this.mVirtualService = null;
        }
    }

    public VirtualManager getKitService(String serviceClass) {
        HwLog.i(TAG, "getKitService");
        if (serviceClass == null || serviceClass.length() == 0 || this.mVirtualService == null) {
            return null;
        }
        if (this.mKitServices.containsKey(serviceClass)) {
            HwLog.i(TAG, "mKitServiceManager has contains serviceClass");
            return this.mKitServices.get(serviceClass);
        } else if (LOCAL_VIRTUAL_DEVICE_CLASS.equals(serviceClass)) {
            HwLog.i(TAG, "mVirtualService is not null");
            LocalVirtualDeviceManager virtualManager = LocalVirtualDeviceManager.getInstance();
            virtualManager.onConnect(this.mVirtualService);
            this.mKitServices.put(serviceClass, virtualManager);
            return virtualManager;
        } else if (VIRTUAL_DEVICE_CLASS.equals(serviceClass)) {
            HwLog.i(TAG, "mVirtualService is not null");
            VirtualDeviceManager virtualDeviceManager = VirtualDeviceManager.getInstance();
            virtualDeviceManager.onConnect(this.mVirtualService);
            this.mKitServices.put(serviceClass, virtualDeviceManager);
            return virtualDeviceManager;
        } else if (VIRTUAL_SENSOR_SERVICE.equals(serviceClass)) {
            HwLog.i(TAG, "virtual sensor mVirtualService is not null");
            SensorAgent sensorAgent = SensorAgent.getInstance();
            sensorAgent.onConnect(this.mVirtualService);
            this.mKitServices.put(serviceClass, sensorAgent);
            return sensorAgent;
        } else if (VIRTUAL_VIBRATE_SERVICE.equals(serviceClass)) {
            HwLog.i(TAG, "virtual vibrator mVirtualService is not null");
            VibratorService vibratorService = VibratorService.getInstance();
            vibratorService.onConnect(this.mVirtualService);
            this.mKitServices.put(serviceClass, vibratorService);
            return vibratorService;
        } else if (VIRTUAL_NOTIFICATION_SERVICE.equals(serviceClass)) {
            DvNotificationService notificationService = DvNotificationService.getInstance();
            notificationService.onConnect(this.mVirtualService);
            this.mKitServices.put(serviceClass, notificationService);
            return notificationService;
        } else if (!VIRTUAL_CAMERA_SERVICE.equals(serviceClass)) {
            return null;
        } else {
            VirtualCameraManager virtualCameraManager = VirtualCameraManager.getInstance();
            virtualCameraManager.onConnect(this.mVirtualService);
            this.mKitServices.put(serviceClass, virtualCameraManager);
            return virtualCameraManager;
        }
    }

    public static String getVersion() {
        HwLog.i(TAG, "getVersion");
        if (isDmsdpExist()) {
            return DvKitVersion.getVersion();
        }
        throw new NoClassDefFoundError();
    }

    static int getAPILevel() {
        return 1;
    }

    private static boolean isDmsdpExist() {
        HwLog.d(TAG, "isDmsdpExist");
        try {
            Application app = getCurrentApplication();
            if (app == null) {
                HwLog.e(TAG, "Current application is null");
                return false;
            }
            app.getApplicationContext().getPackageManager().getApplicationInfo(DMSDP_PACKAGE_NAME, 128);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            HwLog.e(TAG, "DMSDP is not installed");
            return false;
        }
    }

    private static Application getCurrentApplication() {
        try {
            Method currentActivityThreadMethod = Class.forName("android.app.ActivityThread").getDeclaredMethod("currentApplication", new Class[0]);
            currentActivityThreadMethod.setAccessible(true);
            if (currentActivityThreadMethod.invoke(null, new Object[0]) instanceof Application) {
                return (Application) currentActivityThreadMethod.invoke(null, new Object[0]);
            }
            return null;
        } catch (IllegalAccessException e) {
            HwLog.e(TAG, "Can't get current application");
            return null;
        } catch (InvocationTargetException e2) {
            HwLog.e(TAG, "Can't get current application");
            return null;
        } catch (NoSuchMethodException e3) {
            HwLog.e(TAG, "Can't get current application");
            return null;
        } catch (ClassNotFoundException e4) {
            HwLog.e(TAG, "Can't get current application");
            return null;
        }
    }
}
