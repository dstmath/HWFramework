package ohos.dmsdp.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.dmsdp.sdk.IBinderAuthcation;
import ohos.dmsdp.sdk.IDMSDPAdapter;
import ohos.dmsdp.sdk.audio.VirtualAudioProxy;
import ohos.dmsdp.sdk.notification.NotificationData;
import ohos.dmsdp.sdk.sensor.SensorDataListener;
import ohos.dmsdp.sdk.sensor.SensorDataListenerTransport;
import ohos.dmsdp.sdk.sensor.VirtualSensor;
import ohos.dmsdp.sdk.vibrate.VirtualVibrator;

public final class DMSDPAdapterProxy extends DMSDPAdapter {
    private static final String API_NAME_GETSENSORLIST = "getSensorList";
    private static final String API_NAME_GETVIBRATELIST = "getVibrateList";
    private static final String API_NAME_SUBSCRIBESENSOR = "subscribeSensorDataListener";
    private static final String API_NAME_UNSUBSCRIBESENSOR = "unSubscribeSensorDataListener";
    private static final String API_NAME_VIBRATE = "vibrate";
    private static final String API_NAME_VIBRATECANCEL = "vibrateCancel";
    private static final String API_NAME_VIBRATEREPEAT = "vibrateRepeat";
    private static final int COLLECTION_SIZE = 8;
    public static final int DEVICE_TYPE_MIC = 2;
    public static final int DEVICE_TYPE_SPEAKER = 3;
    private static final Object DMSDP_LOCK = new Object();
    private static final String DMSDP_PACKAGE_NAME = "com.huawei.dmsdp";
    private static final String DMSDP_PERMISSION_BODY_SENSORS = "android.permission.BODY_SENSORS";
    private static final String DMSDP_PERMISSION_VIBRATE = "android.permission.VIBRATE";
    private static final String TAG = "DMSDPAdapterProxy";
    private static DMSDPServiceConnection sConnection;
    private static DMSDPAdapterProxy sDMSDPAdapter;
    private static boolean sDMSDPBound = false;
    private static Context sDMSDPContext;
    private static IDMSDPAdapter sDMSDPService;
    private static HandlerThread sThread;
    private final HashMap<SensorDataListener, SensorDataListenerTransport> mSensorDataListenerTransportMap;

    static class DMSDPServiceWrapperProxy implements DMSDPServiceWrapper {
        DMSDPServiceWrapperProxy() {
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public boolean hasNullService() {
            return DMSDPAdapterProxy.sDMSDPService == null;
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int startDiscover(int i, int i2, int i3, int i4, IDiscoverListener iDiscoverListener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.startDiscover(i, i2, i3, i4, iDiscoverListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int stopDiscover(int i, int i2, IDiscoverListener iDiscoverListener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.stopDiscover(i, i2, iDiscoverListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int startScan(int i, int i2) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.startScan(i, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int stopScan(int i, int i2) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.stopScan(i, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int connectDevice(int i, int i2, DMSDPDevice dMSDPDevice, Map<String, Object> map) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.connectDevice(i, i2, dMSDPDevice, map);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int disconnectDevice(int i, int i2, DMSDPDevice dMSDPDevice) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.disconnectDevice(i, i2, dMSDPDevice);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int requestDeviceService(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.requestDeviceService(i, dMSDPDevice, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int startDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map<String, Object> map) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.startDeviceService(i, dMSDPDeviceService, i2, map);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int stopDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.stopDeviceService(i, dMSDPDeviceService, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int updateDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map<String, Object> map) throws RemoteException {
            if (i2 != 207 || DMSDPAdapterProxy.sDMSDPContext != null) {
                return DMSDPAdapterProxy.sDMSDPService.updateDeviceService(i, dMSDPDeviceService, i2, map);
            }
            HwLog.e(DMSDPAdapterProxy.TAG, "the caller context is null");
            return -1;
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int registerDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.registerDMSDPListener(i, iDMSDPListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int unRegisterDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.unRegisterDMSDPListener(i, iDMSDPListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int registerDataListener(int i, DMSDPDevice dMSDPDevice, int i2, IDataListener iDataListener) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.registerDataListener(i, dMSDPDevice, i2, iDataListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int unRegisterDataListener(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.unRegisterDataListener(i, dMSDPDevice, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int sendData(int i, DMSDPDevice dMSDPDevice, int i2, byte[] bArr) throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.sendData(i, dMSDPDevice, i2, bArr);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public boolean hasInit() throws RemoteException {
            return DMSDPAdapterProxy.sDMSDPService.hasInit();
        }
    }

    private DMSDPAdapterProxy() {
        super(new DMSDPServiceWrapperProxy());
        this.mSensorDataListenerTransportMap = new HashMap<>(8);
        if (sThread == null) {
            sThread = new HandlerThread("DMSDPAdapter Looper");
            sThread.start();
        }
        HwLog.d(TAG, "DMSDPAdapter init");
    }

    public static synchronized int getDMSDPAdapter(Context context, DMSDPAdapterCallback dMSDPAdapterCallback) {
        synchronized (DMSDPAdapterProxy.class) {
            HwLog.d(TAG, "getDMSDPAdapter");
            if (context == null) {
                HwLog.e(TAG, "context is null and return.");
                return -2;
            } else if (dMSDPAdapterCallback == null) {
                HwLog.e(TAG, "callback is null and return.");
                return -2;
            } else if (sDMSDPAdapter != null) {
                dMSDPAdapterCallback.onAdapterGet(sDMSDPAdapter);
                return 0;
            } else {
                return bindAidlService(context, dMSDPAdapterCallback);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void setDMSDPService(IDMSDPAdapter iDMSDPAdapter) {
        sDMSDPService = iDMSDPAdapter;
    }

    public static synchronized int createInstance(Context context, DMSDPAdapterCallback dMSDPAdapterCallback) {
        synchronized (DMSDPAdapterProxy.class) {
            HwLog.d(TAG, "createInstance start ");
            if (context == null || dMSDPAdapterCallback == null) {
                HwLog.e(TAG, "createInstance context or callback null");
                throw new IllegalArgumentException("createInstance context or callback null");
            } else if (sDMSDPAdapter != null) {
                dMSDPAdapterCallback.onAdapterGet(sDMSDPAdapter);
                return 0;
            } else if (sDMSDPContext != null) {
                return 0;
            } else {
                sDMSDPContext = context;
                return getDMSDPAdapter(context, dMSDPAdapterCallback);
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
        VirtualAudioProxy.getInstance().stopAudioService("", "", 2, new HashMap(1));
        VirtualAudioProxy.getInstance().stopAudioService("", "", 3, new HashMap(1));
        HwLog.i(TAG, "end disableVirtualAudio");
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public Looper getLooper() {
        synchronized (DMSDP_LOCK) {
            if (sThread == null) {
                return null;
            }
            return sThread.getLooper();
        }
    }

    private IDMSDPAdapter getDMSDPServiceInner() {
        IDMSDPAdapter iDMSDPAdapter;
        synchronized (DMSDP_LOCK) {
            iDMSDPAdapter = sDMSDPService;
        }
        return iDMSDPAdapter;
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public IInterface getDMSDPService() {
        return getDMSDPServiceInner();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public Object getLock() {
        return DMSDP_LOCK;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public DMSDPAdapter getAdapterObject() {
        DMSDPAdapterProxy dMSDPAdapterProxy;
        synchronized (DMSDP_LOCK) {
            dMSDPAdapterProxy = sDMSDPAdapter;
        }
        return dMSDPAdapterProxy;
    }

    public static int bindAidlService(Context context, DMSDPAdapterCallback dMSDPAdapterCallback) {
        HwLog.d(TAG, "bindAidlService");
        synchronized (DMSDP_LOCK) {
            if (sDMSDPBound) {
                return 0;
            }
            Intent intent = new Intent();
            intent.setAction("com.huawei.dmsdp.DMSDP_SERVICE");
            intent.setPackage(DMSDP_PACKAGE_NAME);
            intent.putExtra("bindMode", "modeAuthcation");
            try {
                context.bindService(intent, new DMSDPServiceConnection(context, dMSDPAdapterCallback), 65);
                return 0;
            } catch (SecurityException e) {
                HwLog.e(TAG, "bindAidlService bindService DMSDPServiceConnection ERROR:" + e.getLocalizedMessage());
                return -10;
            }
        }
    }

    public static void unbindAidlService(Context context) {
        try {
            synchronized (DMSDP_LOCK) {
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
    public static void callBackNull(Context context, DMSDPAdapterCallback dMSDPAdapterCallback) {
        dMSDPAdapterCallback.onAdapterGet(null);
        unbindAidlService(context);
    }

    /* access modifiers changed from: private */
    public static class DMSDPServiceConnection implements ServiceConnection {
        private DMSDPAdapterCallback callback;
        private Context context;

        DMSDPServiceConnection(Context context2, DMSDPAdapterCallback dMSDPAdapterCallback) {
            this.context = context2.getApplicationContext();
            this.callback = dMSDPAdapterCallback;
            HwLog.d(DMSDPAdapterProxy.TAG, "DMSDPServiceConnection construct");
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            synchronized (DMSDPAdapterProxy.DMSDP_LOCK) {
                HwLog.d(DMSDPAdapterProxy.TAG, "client onServiceConnected  || service " + iBinder);
                IDMSDPAdapter unused = DMSDPAdapterProxy.sDMSDPService = getDMSDPService(iBinder);
                if (DMSDPAdapterProxy.sDMSDPService == null) {
                    HwLog.e(DMSDPAdapterProxy.TAG, "DMSDPService permission denied");
                    return;
                }
                boolean z = false;
                try {
                    z = DMSDPAdapterProxy.sDMSDPService.hasInit();
                } catch (RemoteException e) {
                    HwLog.e(DMSDPAdapterProxy.TAG, "error in onServiceConnected" + e.getLocalizedMessage());
                }
                if (!z) {
                    HwLog.d(DMSDPAdapterProxy.TAG, "DMSDPService has not init. set mDMSDPService = null");
                    IDMSDPAdapter unused2 = DMSDPAdapterProxy.sDMSDPService = null;
                }
                if (DMSDPAdapterProxy.sDMSDPAdapter == null) {
                    DMSDPAdapterProxy unused3 = DMSDPAdapterProxy.sDMSDPAdapter = new DMSDPAdapterProxy();
                }
                DMSDPAdapterProxy.setDMSDPService(DMSDPAdapterProxy.sDMSDPService);
                DMSDPServiceConnection unused4 = DMSDPAdapterProxy.sConnection = this;
                boolean unused5 = DMSDPAdapterProxy.sDMSDPBound = true;
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
        public void onServiceDisconnected(ComponentName componentName) {
            synchronized (DMSDPAdapterProxy.DMSDP_LOCK) {
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
            }
        }

        private IDMSDPAdapter getDMSDPService(IBinder iBinder) {
            try {
                if (iBinder.getInterfaceDescriptor().contains("IDMSDPAdapter")) {
                    HwLog.d(DMSDPAdapterProxy.TAG, "fetch origin version");
                    return IDMSDPAdapter.Stub.asInterface(iBinder);
                }
                IBinder authcation = IBinderAuthcation.Stub.asInterface(iBinder).getAuthcation(Version.VERSION);
                if (authcation != null) {
                    return IDMSDPAdapter.Stub.asInterface(authcation);
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

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int queryAuthDevice(int i, List<DMSDPDevice> list) {
        IDMSDPAdapter dMSDPServiceInner;
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "queryAuthDevice");
            if (sDMSDPAdapter == null || (dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner()) == null) {
                HwLog.e(TAG, "queryAuthDevice DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return dMSDPServiceInner.queryAuthDevice(i, list);
            } catch (RemoteException e) {
                HwLog.e(TAG, "queryAuthDevice ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int delAuthDevice(int i) {
        IDMSDPAdapter dMSDPServiceInner;
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "queryConnectDevice");
            if (sDMSDPAdapter == null || (dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner()) == null) {
                HwLog.e(TAG, "delAuthDevice DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return dMSDPServiceInner.delAuthDevice(i);
            } catch (RemoteException e) {
                HwLog.e(TAG, "delAuthDevice ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int getVirtualCameraList(int i, List<String> list) {
        IDMSDPAdapter dMSDPServiceInner;
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "get virtual camera list");
            if (sDMSDPContext == null) {
                HwLog.e(TAG, "the caller context is null");
                return -1;
            } else if (sDMSDPAdapter == null || (dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner()) == null) {
                HwLog.e(TAG, "getVirtualCameraList DMSDPService is null, createInstance ERROR");
                return -2;
            } else {
                try {
                    return dMSDPServiceInner.getVirtualCameraList(i, list);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "getVirtualCameraList ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int setVirtualDevicePolicy(int i, int i2, int i3) {
        IDMSDPAdapter dMSDPServiceInner;
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "setVirtualDevicePolicy: policy=" + i3);
            if (sDMSDPAdapter == null || (dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner()) == null) {
                HwLog.e(TAG, "setVirtualDevicePolicy DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return dMSDPServiceInner.setVirtualDevicePolicy(i, i2, i3);
            } catch (RemoteException e) {
                HwLog.e(TAG, "setVirtualDevicePolicy ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int switchModem(String str, int i, String str2, int i2) {
        IDMSDPAdapter dMSDPServiceInner;
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "switchModem");
            if (sDMSDPAdapter == null || (dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner()) == null) {
                HwLog.e(TAG, "switchModem DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return dMSDPServiceInner.switchModem(str, i, str2, i2);
            } catch (RemoteException e) {
                HwLog.e(TAG, "switchModem ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int getModemStatus(List<DMSDPVirtualDevice> list) {
        IDMSDPAdapter dMSDPServiceInner;
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "getModemStatus");
            if (sDMSDPAdapter == null || (dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner()) == null) {
                HwLog.e(TAG, "getModemStatus DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return dMSDPServiceInner.getModemStatus(list);
            } catch (RemoteException e) {
                HwLog.e(TAG, "getModemStatus ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int reportData(String str, long j, long j2, int i) {
        IDMSDPAdapter dMSDPServiceInner;
        HwLog.i(TAG, "reportData start.");
        if (str == null) {
            HwLog.e(TAG, "apiName is null");
            return -2;
        }
        DMSDPAdapterProxy dMSDPAdapterProxy = sDMSDPAdapter;
        if (dMSDPAdapterProxy == null || (dMSDPServiceInner = dMSDPAdapterProxy.getDMSDPServiceInner()) == null) {
            HwLog.e(TAG, "connectDevice DMSDPService is null, createInstance ERROR");
            return -2;
        }
        try {
            dMSDPServiceInner.reportData(str, j, j2, i);
            return 0;
        } catch (RemoteException e) {
            HwLog.e(TAG, "connectDevice ERROR:" + e.getLocalizedMessage());
            return -3;
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int getSensorList(String str, int i, List<VirtualSensor> list) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "get virtual sensor list, sensorType =" + i);
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "getSensorList DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dMSDPServiceInner.getSensorList(str, i, list);
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

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int subscribeSensorDataListener(SensorDataListener sensorDataListener, VirtualSensor virtualSensor, int i) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "subscribe sensor DataListener");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    Looper looper = sDMSDPAdapter.getLooper();
                    if (looper == null) {
                        HwLog.e(TAG, "subscribeSensorDataListener looper is null");
                        return -1;
                    }
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "subscribeSensorDataListener DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    SensorDataListenerTransport sensorDataListenerTransport = this.mSensorDataListenerTransportMap.get(sensorDataListener);
                    if (sensorDataListenerTransport == null) {
                        sensorDataListenerTransport = new SensorDataListenerTransport(sensorDataListener, looper);
                        this.mSensorDataListenerTransportMap.put(sensorDataListener, sensorDataListenerTransport);
                    }
                    try {
                        return dMSDPServiceInner.subscribeSensorDataListener(sensorDataListenerTransport, virtualSensor, i);
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

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int unSubscribeSensorDataListener(SensorDataListener sensorDataListener) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "unsubscribe sensor DataListener");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "unSubscribeSensorDataListener DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    SensorDataListenerTransport sensorDataListenerTransport = this.mSensorDataListenerTransportMap.get(sensorDataListener);
                    if (sensorDataListenerTransport == null) {
                        HwLog.d(TAG, "unSubscribeSensorDataListener was not register");
                        return -2;
                    }
                    try {
                        this.mSensorDataListenerTransportMap.remove(sensorDataListener);
                        return dMSDPServiceInner.unSubscribeSensorDataListener(sensorDataListenerTransport);
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

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int getVibrateList(String str, List<VirtualVibrator> list) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "get vibrateList");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "getVibrateList DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dMSDPServiceInner.getVibrateList(str, list);
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

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int vibrate(String str, int i, long j) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, API_NAME_VIBRATE);
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "vibrate DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dMSDPServiceInner.vibrate(str, i, j);
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

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int vibrateRepeat(String str, int i, long[] jArr, int i2) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, API_NAME_VIBRATEREPEAT);
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "vibrate DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dMSDPServiceInner.vibrateRepeat(str, i, jArr, i2);
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

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int vibrateCancel(String str, int i) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "vibrate cancel");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "vibrate cancel DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dMSDPServiceInner.vibrateCancel(str, i);
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

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int sendNotification(String str, int i, NotificationData notificationData, int i2) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "send notification");
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "sendNotification DMSDPService is null, createInstance ERROR");
                        return -10;
                    }
                    try {
                        return dMSDPServiceInner.sendNotification(str, i, notificationData, i2);
                    } catch (RemoteException unused) {
                        HwLog.e(TAG, "sendNotification remote ERROR");
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "sendNotification the caller context is null");
            return -1;
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int keepChannelActive(String str, int i) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "keep channel active, duration: " + i);
            if (sDMSDPContext != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "keepChannelActive: DMSDPService is null");
                        return -10;
                    }
                    try {
                        return dMSDPServiceInner.keepChannelActive(str, i);
                    } catch (RemoteException unused) {
                        HwLog.e(TAG, "keepChannelActive: remote ERROR");
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "keepChannelActive: the caller context is null");
            return -1;
        }
    }
}
