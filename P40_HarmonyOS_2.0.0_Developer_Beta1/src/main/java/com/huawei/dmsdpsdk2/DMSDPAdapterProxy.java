package com.huawei.dmsdpsdk2;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdp.devicevirtualization.CameraDataCallback;
import com.huawei.dmsdpsdk2.IBinderAuthcation;
import com.huawei.dmsdpsdk2.IDMSDPAdapter;
import com.huawei.dmsdpsdk2.audio.VirtualAudioProxy;
import com.huawei.dmsdpsdk2.notification.NotificationData;
import com.huawei.dmsdpsdk2.sensor.SensorDataListener;
import com.huawei.dmsdpsdk2.sensor.SensorDataListenerTransport;
import com.huawei.dmsdpsdk2.sensor.VirtualSensor;
import com.huawei.dmsdpsdk2.util.Util;
import com.huawei.dmsdpsdk2.vibrate.VirtualVibrator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DMSDPAdapterProxy extends DMSDPAdapter {
    private static final String API_NAME_GETSENSORLIST = "getSensorList";
    private static final String API_NAME_GETVIBRATELIST = "getVibrateList";
    private static final String API_NAME_SUBSCRIBESENSOR = "subscribeSensorDataListener";
    private static final String API_NAME_UNSUBSCRIBESENSOR = "unSubscribeSensorDataListener";
    private static final String API_NAME_VIBRATE = "vibrate";
    private static final String API_NAME_VIBRATECANCEL = "vibrateCancel";
    private static final String API_NAME_VIBRATEREPEAT = "vibrateRepeat";
    public static final int DEVICE_TYPE_MIC = 2;
    public static final int DEVICE_TYPE_SPEAKER = 3;
    private static final String DMSDP_PACKAGE_NAME = "com.huawei.dmsdp";
    private static final String DMSDP_PERMISSION_BODY_SENSORS = "android.permission.BODY_SENSORS";
    private static final String DMSDP_PERMISSION_VIBRATE = "android.permission.VIBRATE";
    private static final String TAG = "DMSDPAdapterProxy";
    private static final String THREAD_PREFIX = "thread_";
    private static DMSDPServiceConnection sConnection;
    private static DMSDPAdapterProxy sDMSDPAdapter;
    private static boolean sDMSDPBound = false;
    private static Context sDMSDPContext;
    private static final Object sDMSDPLock = new Object();
    private static IDMSDPAdapter sDMSDPService;
    private static HandlerThread sThread;
    private final HashMap<String, CameraDataCallbackTransport> mCameraDataCallbackTransportMap;
    private final HashMap<SensorDataListener, SensorDataListenerTransport> mSensorDataListenerTransportMap;

    static class DMSDPServiceWrapperProxy implements DMSDPServiceWrapper {
        DMSDPServiceWrapperProxy() {
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public boolean hasNullService() {
            return DMSDPAdapterProxy.sDMSDPService == null;
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int startDiscover(int businessId, int protocol, int deviceFilter, int serviceFilter, IDiscoverListener listener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.startDiscover(businessId, protocol, deviceFilter, serviceFilter, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int stopDiscover(int businessId, int protocol, IDiscoverListener listener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.stopDiscover(businessId, protocol, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int startScan(int businessId, int protocol) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.startScan(businessId, protocol);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int stopScan(int businessId, int protocol) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.stopScan(businessId, protocol);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int connectDevice(int businessId, int channelType, DMSDPDevice device, Map params) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.connectDevice(businessId, channelType, device, params);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int disconnectDevice(int businessId, int channelType, DMSDPDevice device) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.disconnectDevice(businessId, channelType, device);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int requestDeviceService(int businessId, DMSDPDevice device, int serviceType) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.requestDeviceService(businessId, device, serviceType);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int startDeviceService(int businessId, DMSDPDeviceService serivce, int type, Map params) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.startDeviceService(businessId, serivce, type, params);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int stopDeviceService(int businessId, DMSDPDeviceService service, int type) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.stopDeviceService(businessId, service, type);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int updateDeviceService(int businessId, DMSDPDeviceService service, int action, Map params) throws RemoteException {
            if (action != 207 || DMSDPAdapterProxy.sDMSDPContext != null) {
                return DMSDPAdapterProxy.sDMSDPService.updateDeviceService(businessId, service, action, params);
            }
            HwLog.e(DMSDPAdapterProxy.TAG, "the caller context is null");
            return -1;
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int registerDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.registerDMSDPListener(businessId, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int unRegisterDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.unRegisterDMSDPListener(businessId, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int registerDataListener(int businessId, DMSDPDevice device, int dataType, IDataListener listener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.registerDataListener(businessId, device, dataType, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int unRegisterDataListener(int businessId, DMSDPDevice device, int dataType) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.unRegisterDataListener(businessId, device, dataType);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int sendData(int businessId, DMSDPDevice device, int dataType, byte[] data) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.sendData(businessId, device, dataType, data);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public boolean hasInit() throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.hasInit();
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int openVirtualCameraStream(String appId, String deviceId, String serviceId, Map<String, Object> cameraParas, ICameraDataCallback callback) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.openVirtualCameraStream(appId, deviceId, serviceId, cameraParas, callback);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int closeVirtualCameraStream(String appId, String deviceId, String serviceId) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.closeVirtualCameraStream(appId, deviceId, serviceId);
        }
    }

    private DMSDPAdapterProxy() {
        super(new DMSDPServiceWrapperProxy());
        this.mSensorDataListenerTransportMap = new HashMap<>(0);
        this.mCameraDataCallbackTransportMap = new HashMap<>(0);
        synchronized (DMSDPAdapter.class) {
            if (sThread == null) {
                sThread = new HandlerThread("DMSDPAdapter Looper");
                sThread.start();
            }
        }
        HwLog.d(TAG, "DMSDPAdapter init");
    }

    public static synchronized int getDMSDPAdapter(Context context, DMSDPAdapterCallback callback) {
        synchronized (DMSDPAdapterProxy.class) {
            HwLog.d(TAG, "getDMSDPAdapter");
            if (context == null) {
                HwLog.e(TAG, "context is null and return.");
                return -2;
            } else if (callback == null) {
                HwLog.e(TAG, "callback is null and return.");
                return -2;
            } else if (sDMSDPAdapter != null) {
                callback.onAdapterGet(sDMSDPAdapter);
                return 0;
            } else {
                return bindAidlService(context, callback);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void setDMSDPService(IDMSDPAdapter remoteService) {
        sDMSDPService = remoteService;
    }

    public static synchronized int createInstance(Context context, DMSDPAdapterCallback callback) {
        synchronized (DMSDPAdapterProxy.class) {
            HwLog.d(TAG, "createInstance start ");
            if (context == null || callback == null) {
                HwLog.e(TAG, "createInstance context or callback null");
                throw new IllegalArgumentException("createInstance context or callback null");
            } else if (sDMSDPAdapter != null) {
                sDMSDPContext = context;
                callback.onAdapterGet(sDMSDPAdapter);
                return 0;
            } else if (sDMSDPContext != null) {
                return 0;
            } else {
                sDMSDPContext = context;
                return getDMSDPAdapter(context, callback);
            }
        }
    }

    public static synchronized void releaseInstance() {
        synchronized (DMSDPAdapterProxy.class) {
            if (sDMSDPContext == null) {
                HwLog.e(TAG, "Instance of DMDSPAdapter already released or have not got yet");
                return;
            }
            unbindAidlService(sDMSDPContext);
            sDMSDPContext = null;
        }
    }

    public static void disableVirtualAudio() {
        HwLog.i(TAG, "begin disableVirtualAudio");
        VirtualAudioProxy.getInstance().stopAudioService(BuildConfig.FLAVOR, BuildConfig.FLAVOR, 2, new HashMap(1));
        VirtualAudioProxy.getInstance().stopAudioService(BuildConfig.FLAVOR, BuildConfig.FLAVOR, 3, new HashMap(1));
        HwLog.i(TAG, "end disableVirtualAudio");
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public Looper getLooper() {
        synchronized (DMSDPAdapterProxy.class) {
            if (sThread == null) {
                return null;
            }
            return sThread.getLooper();
        }
    }

    private IDMSDPAdapter getDMSDPServiceInner() {
        IDMSDPAdapter iDMSDPAdapter;
        synchronized (this) {
            iDMSDPAdapter = sDMSDPService;
        }
        return iDMSDPAdapter;
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public IInterface getDMSDPService() {
        return getDMSDPServiceInner();
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public Object getLock() {
        return sDMSDPLock;
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public DMSDPAdapter getAdapterObject() {
        DMSDPAdapterProxy dMSDPAdapterProxy;
        synchronized (sDMSDPLock) {
            dMSDPAdapterProxy = sDMSDPAdapter;
        }
        return dMSDPAdapterProxy;
    }

    public static int bindAidlService(Context context, DMSDPAdapterCallback callback) {
        HwLog.d(TAG, "bindAidlService");
        synchronized (sDMSDPLock) {
            if (sDMSDPBound) {
                return 0;
            }
            Intent intent = new Intent();
            intent.setAction("com.huawei.dmsdp.DMSDP_SERVICE");
            intent.setPackage(DMSDP_PACKAGE_NAME);
            intent.putExtra("bindMode", "modeAuthcation");
            try {
                context.bindService(intent, new DMSDPServiceConnection(context, callback), 65);
                return 0;
            } catch (SecurityException e) {
                HwLog.e(TAG, "bindAidlService bindService DMSDPServiceConnection ERROR:" + e.getLocalizedMessage());
                return -10;
            }
        }
    }

    public static void unbindAidlService(Context context) {
        try {
            synchronized (sDMSDPLock) {
                HwLog.d(TAG, "unbindAidlService mDMSDPBound = " + sDMSDPBound);
                if (sDMSDPBound) {
                    sDMSDPService = null;
                    sDMSDPAdapter = null;
                    context.unbindService(sConnection);
                    sDMSDPBound = false;
                    if (sThread != null) {
                        if (Build.VERSION.SDK_INT >= 18) {
                            sThread.quitSafely();
                        } else {
                            sThread.quit();
                        }
                        sThread = null;
                    }
                }
            }
        } catch (SecurityException e) {
            HwLog.e(TAG, "error in unbindAidlService mDMSDPService" + e.getLocalizedMessage());
            sDMSDPBound = false;
        } catch (IllegalArgumentException e2) {
            HwLog.e(TAG, "IllegalArgumentException in unbindAidlService mDMSDPService" + e2.getLocalizedMessage());
            sDMSDPBound = false;
        }
    }

    /* access modifiers changed from: private */
    public static void callBackNull(Context context, DMSDPAdapterCallback callback) {
        callback.onAdapterGet(null);
        unbindAidlService(context);
    }

    /* access modifiers changed from: private */
    public static class DMSDPServiceConnection implements ServiceConnection {
        private DMSDPAdapterCallback callback;
        private Context context;

        DMSDPServiceConnection(Context context2, DMSDPAdapterCallback callback2) {
            this.context = context2.getApplicationContext();
            this.callback = callback2;
            HwLog.d(DMSDPAdapterProxy.TAG, "DMSDPServiceConnection construct");
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (this) {
                HwLog.d(DMSDPAdapterProxy.TAG, "client onServiceConnected  || service " + service);
                IDMSDPAdapter unused = DMSDPAdapterProxy.sDMSDPService = getDMSDPService(service);
                if (DMSDPAdapterProxy.sDMSDPService == null) {
                    HwLog.e(DMSDPAdapterProxy.TAG, "DMSDPService permission denied");
                    return;
                }
                boolean hasInit = false;
                try {
                    hasInit = DMSDPAdapterProxy.sDMSDPService.hasInit();
                } catch (RemoteException e) {
                    HwLog.e(DMSDPAdapterProxy.TAG, "error in onServiceConnected" + e.getLocalizedMessage());
                }
                if (!hasInit) {
                    HwLog.d(DMSDPAdapterProxy.TAG, "DMSDPService has not init. set mDMSDPService = null");
                    IDMSDPAdapter unused2 = DMSDPAdapterProxy.sDMSDPService = null;
                }
                if (DMSDPAdapterProxy.sDMSDPAdapter == null) {
                    DMSDPAdapterProxy unused3 = DMSDPAdapterProxy.sDMSDPAdapter = new DMSDPAdapterProxy();
                }
                DMSDPAdapterProxy unused4 = DMSDPAdapterProxy.sDMSDPAdapter;
                DMSDPAdapterProxy.setDMSDPService(DMSDPAdapterProxy.sDMSDPService);
                DMSDPAdapterProxy unused5 = DMSDPAdapterProxy.sDMSDPAdapter;
                DMSDPServiceConnection unused6 = DMSDPAdapterProxy.sConnection = this;
                boolean unused7 = DMSDPAdapterProxy.sDMSDPBound = true;
                if (DMSDPAdapterProxy.sDMSDPContext == null) {
                    Context unused8 = DMSDPAdapterProxy.sDMSDPContext = this.context;
                }
                if (this.callback != null) {
                    if (DMSDPAdapterProxy.sDMSDPAdapter.hasNullService()) {
                        DMSDPAdapterProxy.callBackNull(this.context, this.callback);
                        return;
                    }
                    this.callback.onAdapterGet(DMSDPAdapterProxy.sDMSDPAdapter);
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            synchronized (this) {
                HwLog.d(DMSDPAdapterProxy.TAG, "onServiceDisconnected");
                boolean unused = DMSDPAdapterProxy.sDMSDPBound = false;
                IDMSDPAdapter unused2 = DMSDPAdapterProxy.sDMSDPService = null;
                DMSDPAdapterProxy unused3 = DMSDPAdapterProxy.sDMSDPAdapter = null;
                if (DMSDPAdapterProxy.sThread != null) {
                    if (Build.VERSION.SDK_INT >= 18) {
                        DMSDPAdapterProxy.sThread.quitSafely();
                    } else {
                        DMSDPAdapterProxy.sThread.quit();
                    }
                    HandlerThread unused4 = DMSDPAdapterProxy.sThread = null;
                }
                this.callback = null;
                this.context = null;
            }
        }

        private IDMSDPAdapter getDMSDPService(IBinder service) {
            try {
                if (service.getInterfaceDescriptor().contains("IDMSDPAdapter")) {
                    HwLog.d(DMSDPAdapterProxy.TAG, "fetch origin version");
                    return IDMSDPAdapter.Stub.asInterface(service);
                }
                IBinderAuthcation binderAuth = IBinderAuthcation.Stub.asInterface(service);
                HwLog.i(DMSDPAdapterProxy.TAG, "version = 1.0.0");
                IBinder serviceBinder = binderAuth.getAuthcation(Version.VERSION);
                if (serviceBinder != null) {
                    return IDMSDPAdapter.Stub.asInterface(serviceBinder);
                }
                HwLog.e(DMSDPAdapterProxy.TAG, "get authcation failed.Service is null");
                return null;
            } catch (RemoteException e) {
                HwLog.e(DMSDPAdapterProxy.TAG, "error in GetDMSDPService " + e.getLocalizedMessage());
                return null;
            } catch (SecurityException e2) {
                HwLog.e(DMSDPAdapterProxy.TAG, "error in GetDMSDPService " + e2.getLocalizedMessage());
                return null;
            }
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int queryAuthDevice(int businessId, List<DMSDPDevice> deviceList) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "queryAuthDevice");
            if (sDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.queryAuthDevice(businessId, deviceList);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "queryAuthDevice ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "queryAuthDevice DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int delAuthDevice(int businessId) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "queryConnectDevice");
            if (sDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.delAuthDevice(businessId);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "delAuthDevice ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "delAuthDevice DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int getVirtualCameraList(int businessId, List<String> cameraIdList) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "get virtual camera list");
            if (sDMSDPContext == null) {
                HwLog.e(TAG, "the caller context is null");
                return -1;
            }
            if (sDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.getVirtualCameraList(businessId, cameraIdList);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "getVirtualCameraList ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "getVirtualCameraList DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int setVirtualDevicePolicy(int businessId, int module, int policy) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "setVirtualDevicePolicy: policy=" + policy);
            if (sDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.setVirtualDevicePolicy(businessId, module, policy);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "setVirtualDevicePolicy ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "setVirtualDevicePolicy DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int switchModem(String deviceId, int mode, String varStr, int varInt) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "switchModem");
            if (sDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.switchModem(deviceId, mode, varStr, varInt);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "switchModem ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "switchModem DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int getModemStatus(List<DMSDPVirtualDevice> virDeviceList) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "getModemStatus");
            if (sDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.getModemStatus(virDeviceList);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "getModemStatus ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "getModemStatus DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int reportData(String apiName, long callTime, long startTime, int result) {
        IDMSDPAdapter dmsdpService;
        HwLog.i(TAG, "reportData start.");
        if (apiName == null) {
            HwLog.e(TAG, "apiName is null");
            return -2;
        }
        DMSDPAdapterProxy dMSDPAdapterProxy = sDMSDPAdapter;
        if (dMSDPAdapterProxy == null || (dmsdpService = dMSDPAdapterProxy.getDMSDPServiceInner()) == null) {
            HwLog.e(TAG, "connectDevice DMSDPService is null, createInstance ERROR");
            return -2;
        }
        try {
            dmsdpService.reportData(apiName, callTime, startTime, result);
            return 0;
        } catch (RemoteException e) {
            HwLog.e(TAG, "connectDevice ERROR:" + e.getLocalizedMessage());
            return -3;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int getSensorList(String deviceId, int sensorType, List<VirtualSensor> sensorList) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "get virtual sensor list, sensorType =" + sensorType);
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "getSensorList DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dmsdpService.getSensorList(deviceId, sensorType, sensorList);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "getSensorList ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "the caller context is null");
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int subscribeSensorDataListener(SensorDataListener listener, VirtualSensor sensor, int rate) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "subscribe sensor DataListener");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    Looper looper = sDMSDPAdapter.getLooper();
                    if (looper == null) {
                        HwLog.e(TAG, "subscribeSensorDataListener looper is null");
                        return -1;
                    }
                    IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "subscribeSensorDataListener DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    SensorDataListenerTransport transport = this.mSensorDataListenerTransportMap.get(listener);
                    if (transport == null) {
                        transport = new SensorDataListenerTransport(listener, looper);
                        this.mSensorDataListenerTransportMap.put(listener, transport);
                    }
                    try {
                        return dmsdpService.subscribeSensorDataListener(transport, sensor, rate);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "subscribeSensorDataListener ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "subscribeSensorDataListener the caller context is null");
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int unSubscribeSensorDataListener(SensorDataListener listener) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "unsubscribe sensor DataListener");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "unSubscribeSensorDataListener DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    SensorDataListenerTransport transport = this.mSensorDataListenerTransportMap.get(listener);
                    if (transport == null) {
                        HwLog.d(TAG, "unSubscribeSensorDataListener was not register");
                        return -2;
                    }
                    try {
                        this.mSensorDataListenerTransportMap.remove(listener);
                        return dmsdpService.unSubscribeSensorDataListener(transport);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "unSubscribeSensorDataListener ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "the caller context is null");
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int getVibrateList(String deviceId, List<VirtualVibrator> vibrateList) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "get vibrateList");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "getVibrateList DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dmsdpService.getVibrateList(deviceId, vibrateList);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "getVibrateList ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "the caller context is null");
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int vibrate(String deviceId, int vibrateId, long milliseconds) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, API_NAME_VIBRATE);
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "vibrate DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dmsdpService.vibrate(deviceId, vibrateId, milliseconds);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "vibrate ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "the caller context is null");
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int vibrateRepeat(String deviceId, int vibrateId, long[] pattern, int repeat) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, API_NAME_VIBRATEREPEAT);
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "vibrate DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dmsdpService.vibrateRepeat(deviceId, vibrateId, pattern, repeat);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "vibrate ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "vibrateRepeat:the caller context is null");
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int vibrateCancel(String deviceId, int vibrateId) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "vibrate cancel");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "vibrate cancel DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dmsdpService.vibrateCancel(deviceId, vibrateId);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "vibrate cancel ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "vibrateCancel the caller context is null");
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int sendNotification(String deviceId, int notificationId, NotificationData notification, int operationMode) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "send notification");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "sendNotification DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dmsdpService.sendNotification(deviceId, notificationId, notification, operationMode);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "sendNotification remote ERROR");
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "sendNotification the caller context is null");
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int keepChannelActive(String deviceId, int duration) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "keep channel active, duration: " + duration);
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "keepChannelActive: DMSDPService is null");
                        return -10;
                    }
                    try {
                        return dmsdpService.keepChannelActive(deviceId, duration);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "keepChannelActive: remote ERROR");
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "keepChannelActive: the caller context is null");
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int openVirtualCameraStream(String streamId, String deviceId, String serviceId, Map<String, Object> cameraParas, CameraDataCallback callback) {
        Throwable th;
        RemoteException e;
        synchronized (getLock()) {
            try {
                HwLog.i(TAG, "openVirtualCamera start with pid=" + Process.myPid());
                if (!validateInit()) {
                    HwLog.e(TAG, "openVirtualCamera DMSDPService is null, createInstance ERROR");
                    return -2;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(getPkgName());
                sb.append("_");
                try {
                    sb.append(streamId);
                    String appId = sb.toString();
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(appId);
                    sb2.append("_");
                    try {
                        sb2.append(deviceId);
                        sb2.append("_");
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                    try {
                        sb2.append(serviceId);
                        String transPortId = sb2.toString();
                        if (this.mCameraDataCallbackTransportMap.get(transPortId) != null) {
                            HwLog.e(TAG, "please close virtual camera first, transPortId: " + Util.anonymizeData(transPortId));
                            return -2;
                        }
                        HandlerThread handlerThread = new HandlerThread(THREAD_PREFIX + transPortId);
                        handlerThread.start();
                        try {
                            CameraDataCallbackTransport transport = new CameraDataCallbackTransport(callback, handlerThread);
                            this.mCameraDataCallbackTransportMap.put(transPortId, transport);
                            try {
                                try {
                                    return getAdapterObject().getServiceWrapper().openVirtualCameraStream(appId, deviceId, serviceId, cameraParas, transport);
                                } catch (RemoteException e2) {
                                    e = e2;
                                    HwLog.e(TAG, "openVirtualCamera ERROR:" + e.getLocalizedMessage());
                                    try {
                                        handlerThread.quit();
                                    } catch (RuntimeException re) {
                                        HwLog.e(TAG, "quit handler thread failed, ex: " + re.getLocalizedMessage());
                                    }
                                    return -3;
                                }
                            } catch (RemoteException e3) {
                                e = e3;
                                HwLog.e(TAG, "openVirtualCamera ERROR:" + e.getLocalizedMessage());
                                handlerThread.quit();
                                return -3;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        throw th;
                    }
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                throw th;
            }
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int closeVirtualCameraStream(String streamId, String deviceId, String serviceId) {
        synchronized (getLock()) {
            HwLog.i(TAG, "closeVirtualCamera start with pid=" + Process.myPid());
            if (!validateInit()) {
                HwLog.e(TAG, "closeVirtualCamera DMSDPService is null, createInstance ERROR");
                return -2;
            }
            String appId = getPkgName() + "_" + streamId;
            String transPortId = appId + "_" + deviceId + "_" + serviceId;
            CameraDataCallbackTransport transport = this.mCameraDataCallbackTransportMap.get(transPortId);
            if (transport == null) {
                HwLog.e(TAG, "Can not find CameraDataCallbackTransport, transportId: " + Util.anonymizeData(transPortId));
                return -2;
            }
            transport.stopHandlerThread();
            this.mCameraDataCallbackTransportMap.remove(transPortId);
            try {
                return getAdapterObject().getServiceWrapper().closeVirtualCameraStream(appId, deviceId, serviceId);
            } catch (RemoteException e) {
                HwLog.e(TAG, "closeVirtualCamera ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    private String getPkgName() {
        HwLog.d(TAG, "get package name from calling pid " + Binder.getCallingPid());
        ActivityManager activityManager = (ActivityManager) sDMSDPContext.getSystemService("activity");
        if (activityManager == null) {
            return BuildConfig.FLAVOR;
        }
        for (ActivityManager.RunningAppProcessInfo info : activityManager.getRunningAppProcesses()) {
            if (info.pid == Binder.getCallingPid()) {
                return info.processName;
            }
        }
        return BuildConfig.FLAVOR;
    }
}
