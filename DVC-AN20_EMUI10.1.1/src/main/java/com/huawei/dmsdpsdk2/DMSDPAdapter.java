package com.huawei.dmsdpsdk2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import com.huawei.android.hwpartdevicevirtualization.BuildConfig;
import com.huawei.dmsdpsdk2.IBinderAuthcation;
import com.huawei.dmsdpsdk2.IDMSDPAdapter;
import com.huawei.dmsdpsdk2.audio.VirtualAudioProxy;
import com.huawei.dmsdpsdk2.notification.NotificationData;
import com.huawei.dmsdpsdk2.sensor.SensorDataListener;
import com.huawei.dmsdpsdk2.sensor.SensorDataListenerTransport;
import com.huawei.dmsdpsdk2.sensor.VirtualSensor;
import com.huawei.dmsdpsdk2.vibrate.VirtualVibrator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DMSDPAdapter {
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
    private static final String TAG = "DMSDPServiceJar";
    private static DMSDPServiceConnection mConnection;
    private static DMSDPAdapter mDMSDPAdapter;
    private static boolean mDMSDPBound = false;
    private static Context mDMSDPContext;
    private static final Object mDMSDPLock = new Object();
    private static IDMSDPAdapter mDMSDPService;
    private static HandlerThread mThread;
    private final HashMap<DMSDPListener, DMSDPListenerTransport> mDMSDPListenerTransportMap;
    private final HashMap<DataListener, DataListenerTransport> mDataListenerTransportMap;
    private final HashMap<DiscoverListener, DiscoverListenerTransport> mDiscoverListenerTransportMap;
    private final HashMap<SensorDataListener, SensorDataListenerTransport> mSensorDataListenerTransportMap;

    private DMSDPAdapter() {
        this.mDiscoverListenerTransportMap = new HashMap<>(0);
        this.mDMSDPListenerTransportMap = new HashMap<>(0);
        this.mDataListenerTransportMap = new HashMap<>(0);
        this.mSensorDataListenerTransportMap = new HashMap<>(0);
        synchronized (DMSDPAdapter.class) {
            if (mThread == null) {
                mThread = new HandlerThread("DMSDPAdapter Looper");
                mThread.start();
            }
        }
        HwLog.d(TAG, "DMSDPAdapter init");
    }

    public static synchronized int getDMSDPAdapter(Context context, DMSDPAdapterCallback callback) {
        synchronized (DMSDPAdapter.class) {
            HwLog.d(TAG, "getDMSDPAdapter");
            if (context == null) {
                HwLog.e(TAG, "context is null and return.");
                return -2;
            } else if (callback == null) {
                HwLog.e(TAG, "callback is null and return.");
                return -2;
            } else if (mDMSDPAdapter != null) {
                callback.onAdapterGet(mDMSDPAdapter);
                return 0;
            } else {
                return bindAidlService(context, callback);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void setDMSDPService(IDMSDPAdapter remoteService) {
        mDMSDPService = remoteService;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasNullService() {
        return mDMSDPService == null;
    }

    public static synchronized int createInstance(Context context, DMSDPAdapterCallback callback) {
        synchronized (DMSDPAdapter.class) {
            HwLog.d(TAG, "createInstance start ");
            if (context == null || callback == null) {
                HwLog.e(TAG, "createInstance context or callback null");
                throw new IllegalArgumentException("createInstance context or callback null");
            } else if (mDMSDPAdapter != null) {
                callback.onAdapterGet(mDMSDPAdapter);
                return 0;
            } else if (mDMSDPContext != null) {
                return 0;
            } else {
                mDMSDPContext = context;
                return getDMSDPAdapter(context, callback);
            }
        }
    }

    public static synchronized void releaseInstance() {
        synchronized (DMSDPAdapter.class) {
            if (mDMSDPContext == null) {
                HwLog.e(TAG, "Instance of DMDSPAdapter already released or have not got yet");
                return;
            }
            unbindAidlService(mDMSDPContext);
            mDMSDPContext = null;
        }
    }

    public static void disableVirtualAudio() {
        HwLog.i(TAG, "begin disableVirtualAudio");
        VirtualAudioProxy.getInstance().stopAudioService(BuildConfig.FLAVOR, BuildConfig.FLAVOR, 2, new HashMap(1));
        VirtualAudioProxy.getInstance().stopAudioService(BuildConfig.FLAVOR, BuildConfig.FLAVOR, 3, new HashMap(1));
        HwLog.i(TAG, "end disableVirtualAudio");
    }

    public Looper getLooper() {
        synchronized (DMSDPAdapter.class) {
            if (mThread == null) {
                return null;
            }
            return mThread.getLooper();
        }
    }

    public IDMSDPAdapter getDMSDPService() {
        IDMSDPAdapter iDMSDPAdapter;
        synchronized (this) {
            iDMSDPAdapter = mDMSDPService;
        }
        return iDMSDPAdapter;
    }

    public static int bindAidlService(Context context, DMSDPAdapterCallback callback) {
        HwLog.d(TAG, "bindAidlService");
        synchronized (mDMSDPLock) {
            if (mDMSDPBound) {
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
        HwLog.d(TAG, "unbindAidlService mDMSDPBound = " + mDMSDPBound);
        try {
            synchronized (mDMSDPLock) {
                if (mDMSDPBound) {
                    mDMSDPService = null;
                    mDMSDPAdapter = null;
                    context.unbindService(mConnection);
                    mDMSDPBound = false;
                    if (mThread != null) {
                        if (Build.VERSION.SDK_INT >= 18) {
                            mThread.quitSafely();
                        } else {
                            mThread.quit();
                        }
                        mThread = null;
                    }
                }
            }
        } catch (SecurityException e) {
            HwLog.e(TAG, "error in unbindAidlService mDMSDPService" + e.getLocalizedMessage());
            mDMSDPBound = false;
        } catch (IllegalArgumentException e2) {
            HwLog.e(TAG, "IllegalArgumentException in unbindAidlService mDMSDPService" + e2.getLocalizedMessage());
            mDMSDPBound = false;
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
            HwLog.d(DMSDPAdapter.TAG, "DMSDPServiceConnection construct");
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (this) {
                HwLog.d(DMSDPAdapter.TAG, "client onServiceConnected  || service " + service);
                IDMSDPAdapter unused = DMSDPAdapter.mDMSDPService = getDMSDPService(service);
                if (DMSDPAdapter.mDMSDPService == null) {
                    HwLog.e(DMSDPAdapter.TAG, "DMSDPService permission denied");
                    return;
                }
                boolean hasInit = false;
                try {
                    hasInit = DMSDPAdapter.mDMSDPService.hasInit();
                } catch (RemoteException e) {
                    HwLog.e(DMSDPAdapter.TAG, "error in onServiceConnected" + e.getLocalizedMessage());
                }
                if (!hasInit) {
                    HwLog.d(DMSDPAdapter.TAG, "DMSDPService has not init. set mDMSDPService = null");
                    IDMSDPAdapter unused2 = DMSDPAdapter.mDMSDPService = null;
                }
                if (DMSDPAdapter.mDMSDPAdapter == null) {
                    DMSDPAdapter unused3 = DMSDPAdapter.mDMSDPAdapter = new DMSDPAdapter();
                }
                DMSDPAdapter unused4 = DMSDPAdapter.mDMSDPAdapter;
                DMSDPAdapter.setDMSDPService(DMSDPAdapter.mDMSDPService);
                DMSDPAdapter unused5 = DMSDPAdapter.mDMSDPAdapter;
                DMSDPServiceConnection unused6 = DMSDPAdapter.mConnection = this;
                boolean unused7 = DMSDPAdapter.mDMSDPBound = true;
                if (this.callback != null) {
                    if (DMSDPAdapter.mDMSDPAdapter.hasNullService()) {
                        DMSDPAdapter.callBackNull(this.context, this.callback);
                        return;
                    }
                    this.callback.onAdapterGet(DMSDPAdapter.mDMSDPAdapter);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            synchronized (this) {
                HwLog.d(DMSDPAdapter.TAG, "onServiceDisconnected");
                boolean unused = DMSDPAdapter.mDMSDPBound = false;
                IDMSDPAdapter unused2 = DMSDPAdapter.mDMSDPService = null;
                DMSDPAdapter unused3 = DMSDPAdapter.mDMSDPAdapter = null;
                if (DMSDPAdapter.mThread != null) {
                    if (Build.VERSION.SDK_INT >= 18) {
                        DMSDPAdapter.mThread.quitSafely();
                    } else {
                        DMSDPAdapter.mThread.quit();
                    }
                    HandlerThread unused4 = DMSDPAdapter.mThread = null;
                }
            }
        }

        private IDMSDPAdapter getDMSDPService(IBinder service) {
            try {
                if (service.getInterfaceDescriptor().contains("IDMSDPAdapter")) {
                    HwLog.d(DMSDPAdapter.TAG, "fetch origin version");
                    return IDMSDPAdapter.Stub.asInterface(service);
                }
                IBinder serviceBinder = IBinderAuthcation.Stub.asInterface(service).getAuthcation(Version.VERSION);
                if (serviceBinder != null) {
                    return IDMSDPAdapter.Stub.asInterface(serviceBinder);
                }
                HwLog.e(DMSDPAdapter.TAG, "get authcation failed.Service is null");
                return null;
            } catch (RemoteException e) {
                HwLog.e(DMSDPAdapter.TAG, "error in GetDMSDPService " + e.getLocalizedMessage());
                return null;
            } catch (SecurityException e2) {
                HwLog.e(DMSDPAdapter.TAG, "error in GetDMSDPService " + e2.getLocalizedMessage());
                return null;
            }
        }
    }

    public int registerDMSDPListener(int businessId, DMSDPListener listener) {
        IDMSDPAdapter dmsdpService;
        synchronized (mDMSDPLock) {
            if (listener == null) {
                HwLog.e(TAG, "registerDMSDPListener listener null");
                return -2;
            }
            if (!(mDMSDPAdapter == null || (dmsdpService = mDMSDPAdapter.getDMSDPService()) == null)) {
                Looper looper = mDMSDPAdapter.getLooper();
                if (looper != null) {
                    DMSDPListenerTransport transport = this.mDMSDPListenerTransportMap.get(listener);
                    if (transport == null) {
                        transport = new DMSDPListenerTransport(listener, looper);
                        this.mDMSDPListenerTransportMap.put(listener, transport);
                    }
                    try {
                        return dmsdpService.registerDMSDPListener(businessId, transport);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "registerDMSDPListener ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "registerDMSDPListener DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int unRegisterDMSDPListener(int businessId, DMSDPListener listener) {
        synchronized (mDMSDPLock) {
            if (listener == null) {
                HwLog.e(TAG, "unRegisterDMSDPListener listener null");
                return -2;
            }
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    DMSDPListenerTransport transport = this.mDMSDPListenerTransportMap.get(listener);
                    if (transport == null) {
                        HwLog.d(TAG, "DMSDPListener was not register");
                        return -4;
                    }
                    try {
                        this.mDMSDPListenerTransportMap.remove(listener);
                        return dmsdpService.unRegisterDMSDPListener(businessId, transport);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "unRegisterDMSDPListener ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "unRegisterDMSDPListener DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int startDiscover(int businessId, int protocol, int deviceFilter, int serviceFilter, DiscoverListener listener) {
        DiscoverListenerTransport transport;
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "startDiscover start");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    Looper looper = mDMSDPAdapter.getLooper();
                    if (looper != null) {
                        if (protocol >= 1) {
                            if (protocol <= 255) {
                                if (deviceFilter >= 0) {
                                    if (deviceFilter <= 255) {
                                        if (serviceFilter >= 0) {
                                            if (serviceFilter <= 511) {
                                                DiscoverListenerTransport transport2 = this.mDiscoverListenerTransportMap.get(listener);
                                                if (transport2 == null) {
                                                    DiscoverListenerTransport transport3 = new DiscoverListenerTransport(listener, looper);
                                                    this.mDiscoverListenerTransportMap.put(listener, transport3);
                                                    transport = transport3;
                                                } else {
                                                    transport = transport2;
                                                }
                                                try {
                                                    return dmsdpService.startDiscover(businessId, protocol, deviceFilter, serviceFilter, transport);
                                                } catch (RemoteException e) {
                                                    HwLog.e(TAG, "startDiscover ERROR:" + e.getLocalizedMessage());
                                                    return -3;
                                                }
                                            }
                                        }
                                        HwLog.e(TAG, "startDiscover service filter is not valid");
                                        return -2;
                                    }
                                }
                                HwLog.e(TAG, "startDiscover device filter is not valid");
                                return -2;
                            }
                        }
                        HwLog.e(TAG, "startDiscover protocol is not valid");
                        return -2;
                    }
                }
            }
            HwLog.e(TAG, "startDiscover DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int stopDiscover(int businessId, int protocol, DiscoverListener listener) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "stopDiscover start");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    if (protocol >= 1) {
                        if (protocol <= 255) {
                            DiscoverListenerTransport transport = this.mDiscoverListenerTransportMap.get(listener);
                            if (transport == null) {
                                HwLog.d(TAG, "DiscoverListener was not register");
                                return -4;
                            }
                            if (protocol == 255) {
                                try {
                                    this.mDiscoverListenerTransportMap.remove(listener);
                                } catch (RemoteException e) {
                                    HwLog.e(TAG, "stopDiscover ERROR:" + e.getLocalizedMessage());
                                    return -3;
                                }
                            }
                            return dmsdpService.stopDiscover(businessId, protocol, transport);
                        }
                    }
                    HwLog.e(TAG, "startDiscover protocol is not valid");
                    return -2;
                }
            }
            HwLog.e(TAG, "stopDiscover DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int startScan(int businessId, int protocol) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "startScan start");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    if (protocol < 1 || protocol > 255) {
                        HwLog.e(TAG, "startDiscover protocol is not valid");
                        return -2;
                    }
                    try {
                        return dmsdpService.startScan(businessId, protocol);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "startScan ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "startScan DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int stopScan(int businessId, int protocol) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "stopScan start");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    if (protocol < 1 || protocol > 255) {
                        HwLog.e(TAG, "startDiscover protocol is not valid");
                        return -2;
                    }
                    try {
                        return dmsdpService.stopScan(businessId, protocol);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "stopScan ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "stopScan DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int connectDevice(int businessId, int channelType, DMSDPDevice device, Map<String, Object> params) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "connectDevice start.");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.connectDevice(businessId, channelType, device, params);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "connectDevice ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "connectDevice DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int disconnectDevice(int businessId, int channelType, DMSDPDevice device) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "disconnectDevice start");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.disconnectDevice(businessId, channelType, device);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "disconnectDevice ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "disconnectDevice DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int requestDeviceService(int businessId, DMSDPDevice device, int serviceType) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "requestDeviceService start, serviceType:" + serviceType);
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    if (serviceType < 1 || serviceType > 16383) {
                        HwLog.e(TAG, "requestDeviceService serviceType is not valid");
                        return -2;
                    }
                    try {
                        return dmsdpService.requestDeviceService(businessId, device, serviceType);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "requestDeviceService ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "requestDeviceService DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int startDeviceService(int businessId, DMSDPDeviceService service, int type, Map<String, Object> params) {
        IDMSDPAdapter dmsdpService;
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "startDeviceService start");
            if (mDMSDPAdapter == null || (dmsdpService = mDMSDPAdapter.getDMSDPService()) == null || mDMSDPAdapter.getLooper() == null) {
                HwLog.e(TAG, "startDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return dmsdpService.startDeviceService(businessId, service, type, params);
            } catch (RemoteException e) {
                HwLog.e(TAG, "startDeviceService ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int stopDeviceService(int businessId, DMSDPDeviceService service, int type) {
        IDMSDPAdapter dmsdpService;
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "stopDeviceService start");
            if (mDMSDPAdapter == null || (dmsdpService = mDMSDPAdapter.getDMSDPService()) == null || mDMSDPAdapter.getLooper() == null) {
                HwLog.e(TAG, "stopDeviceService DMSDPService is null, createInstance ERROR");
                return -2;
            }
            try {
                return dmsdpService.stopDeviceService(businessId, service, type);
            } catch (RemoteException e) {
                HwLog.e(TAG, "stopDeviceService ERROR:" + e.getLocalizedMessage());
                return -3;
            }
        }
    }

    public int updateDeviceService(int businessId, DMSDPDeviceService service, int action, Map<String, Object> params) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "updateDeviceService");
            if (action == 207 && mDMSDPContext == null) {
                HwLog.e(TAG, "the caller context is null");
                return -1;
            }
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.updateDeviceService(businessId, service, action, params);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "updateDeviceService ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "updateDeviceService DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int registerDataListener(int businessId, DMSDPDevice device, int dataType, DataListener listener) {
        IDMSDPAdapter dmsdpService;
        synchronized (mDMSDPLock) {
            if (listener == null) {
                HwLog.e(TAG, "registerDataListener listener null");
                return -2;
            }
            if (!(mDMSDPAdapter == null || (dmsdpService = mDMSDPAdapter.getDMSDPService()) == null)) {
                Looper looper = mDMSDPAdapter.getLooper();
                if (looper != null) {
                    DataListenerTransport transport = this.mDataListenerTransportMap.get(listener);
                    if (transport == null) {
                        transport = new DataListenerTransport(listener, looper);
                        this.mDataListenerTransportMap.put(listener, transport);
                    }
                    try {
                        return dmsdpService.registerDataListener(businessId, device, dataType, transport);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "registerDMSDPListener ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "registerDataListener DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int unRegisterDataListener(int businessId, DMSDPDevice device, int dataType) {
        synchronized (mDMSDPLock) {
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.unRegisterDataListener(businessId, device, dataType);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "unRegisterDMSDPListener ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "unRegisterDMSDPListener DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int sendData(int businessId, DMSDPDevice device, int dataType, byte[] data) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "sendData");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.sendData(businessId, device, dataType, data);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "sendData ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "sendData DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    public int queryAuthDevice(int businessId, List<DMSDPDevice> deviceList) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "queryAuthDevice");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int delAuthDevice(int businessId) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "queryConnectDevice");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int getVirtualCameraList(int businessId, List<String> cameraIdList) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "get virtual camera list");
            if (mDMSDPContext == null) {
                HwLog.e(TAG, "the caller context is null");
                return -1;
            }
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int setVirtualDevicePolicy(int businessId, int module, int policy) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "setVirtualDevicePolicy: policy=" + policy);
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int switchModem(String deviceId, int mode, String varStr, int varInt) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "switchModem");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int getModemStatus(List<DMSDPVirtualDevice> virDeviceList) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "getModemStatus");
            if (mDMSDPAdapter != null) {
                IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int reportData(String apiName, long callTime, long startTime, int result) {
        IDMSDPAdapter dmsdpService;
        HwLog.i(TAG, "reportData start.");
        if (apiName == null) {
            HwLog.e(TAG, "apiName is null");
            return -2;
        }
        DMSDPAdapter dMSDPAdapter = mDMSDPAdapter;
        if (dMSDPAdapter == null || (dmsdpService = dMSDPAdapter.getDMSDPService()) == null) {
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

    public int getSensorList(String deviceId, int sensorType, List<VirtualSensor> sensorList) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "get virtual sensor list, sensorType =" + sensorType);
            if (mDMSDPContext != null) {
                if (mDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int subscribeSensorDataListener(SensorDataListener listener, VirtualSensor sensor, int rate) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "subscribe sensor DataListener");
            if (mDMSDPContext != null) {
                if (mDMSDPAdapter != null) {
                    Looper looper = mDMSDPAdapter.getLooper();
                    if (looper == null) {
                        HwLog.e(TAG, "subscribeSensorDataListener looper is null");
                        return -1;
                    }
                    IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int unSubscribeSensorDataListener(SensorDataListener listener) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "unsubscribe sensor DataListener");
            if (mDMSDPContext != null) {
                if (mDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int getVibrateList(String deviceId, List<VirtualVibrator> vibrateList) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "get vibrateList");
            if (mDMSDPContext != null) {
                if (mDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int vibrate(String deviceId, int vibrateId, long milliseconds) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, API_NAME_VIBRATE);
            if (mDMSDPContext != null) {
                if (mDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int vibrateRepeat(String deviceId, int vibrateId, long[] pattern, int repeat) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, API_NAME_VIBRATEREPEAT);
            if (mDMSDPContext != null) {
                if (mDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int vibrateCancel(String deviceId, int vibrateId) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "vibrate cancel");
            if (mDMSDPContext != null) {
                if (mDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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

    public int sendNotification(String deviceId, int notificationId, NotificationData notification, int operationMode) {
        synchronized (mDMSDPLock) {
            HwLog.i(TAG, "send notification");
            if (mDMSDPContext != null) {
                if (mDMSDPAdapter != null) {
                    IDMSDPAdapter dmsdpService = mDMSDPAdapter.getDMSDPService();
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
}
