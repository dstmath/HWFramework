package com.huawei.dmsdpsdk2;

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
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import com.huawei.dmsdpsdk2.IDMSDPAdapterAgent;
import com.huawei.dmsdpsdk2.util.Util;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class DMSDPAdapterAgent extends DMSDPAdapter {
    private static final String DMSDP_ACTION_NAME = "com.huawei.dmsdpdevice.DMSDP_DEVICE_SERVICE";
    private static final String DMSDP_PACKAGE_NAME = "com.huawei.dmsdp";
    private static final String DMSDP_SERVICE_NAME = "com.huawei.dmsdpdevice.DMSDPService";
    private static final int KEY_ACTION_MAX = 2;
    private static final int KEY_ACTION_MIN = 0;
    private static final String TAG = "DMSDPAdapterAgent";
    private static DMSDPServiceConnection sConnection;
    private static DMSDPAdapterAgent sDMSDPAdapter;
    private static boolean sDMSDPBound = false;
    private static Context sDMSDPContext;
    private static final Object sDMSDPLock = new Object();
    private static IDMSDPAdapterAgent sDMSDPService;
    private static HandlerThread sThread;

    static class DMSDPServiceWrapperAgent implements DMSDPServiceWrapper {
        DMSDPServiceWrapperAgent() {
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public boolean hasNullService() {
            return DMSDPAdapterAgent.sDMSDPService == null;
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int startDiscover(int businessId, int protocol, int deviceFilter, int serviceFilter, IDiscoverListener listener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.startDiscover(businessId, protocol, deviceFilter, serviceFilter, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int stopDiscover(int businessId, int protocol, IDiscoverListener listener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.stopDiscover(businessId, protocol, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int startScan(int businessId, int protocol) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.startScan(businessId, protocol);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int stopScan(int businessId, int protocol) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.stopScan(businessId, protocol);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int connectDevice(int businessId, int channelType, DMSDPDevice device, Map params) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.connectDevice(businessId, channelType, device, params);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int disconnectDevice(int businessId, int channelType, DMSDPDevice device) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.disconnectDevice(businessId, channelType, device);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int requestDeviceService(int businessId, DMSDPDevice device, int serviceType) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.requestDeviceService(businessId, device, serviceType);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int startDeviceService(int businessId, DMSDPDeviceService serivce, int type, Map params) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.startDeviceService(businessId, serivce, type, params);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int stopDeviceService(int businessId, DMSDPDeviceService service, int type) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.stopDeviceService(businessId, service, type);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int updateDeviceService(int businessId, DMSDPDeviceService service, int action, Map params) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.updateDeviceService(businessId, service, action, params);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int registerDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.registerDMSDPListener(businessId, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int unRegisterDMSDPListener(int businessId, IDMSDPListener listener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.unRegisterDMSDPListener(businessId, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int registerDataListener(int businessId, DMSDPDevice device, int dataType, IDataListener listener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.registerDataListener(businessId, device, dataType, listener);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int unRegisterDataListener(int businessId, DMSDPDevice device, int dataType) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.unRegisterDataListener(businessId, device, dataType);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int sendData(int businessId, DMSDPDevice device, int dataType, byte[] data) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.sendData(businessId, device, dataType, data);
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public boolean hasInit() throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.hasInit();
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int openVirtualCameraStream(String appId, String deviceId, String serviceId, Map<String, Object> map, ICameraDataCallback callback) throws RemoteException {
            return -7;
        }

        @Override // com.huawei.dmsdpsdk2.DMSDPServiceWrapper
        public int closeVirtualCameraStream(String appId, String deviceId, String serviceId) throws RemoteException {
            return -7;
        }
    }

    private DMSDPAdapterAgent() {
        super(new DMSDPServiceWrapperAgent());
        synchronized (sDMSDPLock) {
            if (sThread == null) {
                sThread = new HandlerThread("DMSDPAdapter Looper");
                sThread.start();
            }
        }
        HwLog.d(TAG, "DMSDPAdapter init");
    }

    public static int getDMSDPAdapter(Context context, DMSDPAdapterCallback callback) {
        HwLog.d(TAG, "getDMSDPAdapter");
        if (context == null) {
            HwLog.e(TAG, "context is null and return.");
            return -2;
        } else if (callback == null) {
            HwLog.e(TAG, "callback is null and return.");
            return -2;
        } else {
            synchronized (sDMSDPLock) {
                if (sDMSDPAdapter != null) {
                    callback.onAdapterGet(sDMSDPAdapter);
                    return 0;
                }
                return bindAidlService(context, callback);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void setDMSDPService(IDMSDPAdapterAgent remoteService) {
        sDMSDPService = remoteService;
    }

    public static int createInstance(Context context, DMSDPAdapterCallback callback) {
        HwLog.d(TAG, "createInstance start ");
        if (context == null || callback == null) {
            HwLog.e(TAG, "createInstance context or callback null");
            throw new IllegalArgumentException("createInstance context or callback null");
        }
        synchronized (sDMSDPLock) {
            if (sDMSDPAdapter != null) {
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

    public static void releaseInstance() {
        synchronized (sDMSDPLock) {
            if (sDMSDPContext == null) {
                HwLog.e(TAG, "Instance of DMDSPAdapter already released or have not got yet");
                return;
            }
            unbindAidlService(sDMSDPContext);
            sDMSDPContext = null;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public Looper getLooper() {
        synchronized (sDMSDPLock) {
            if (sThread == null) {
                return null;
            }
            return sThread.getLooper();
        }
    }

    private IDMSDPAdapterAgent getDMSDPServiceInner() {
        IDMSDPAdapterAgent iDMSDPAdapterAgent;
        synchronized (sDMSDPLock) {
            iDMSDPAdapterAgent = sDMSDPService;
        }
        return iDMSDPAdapterAgent;
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
        DMSDPAdapterAgent dMSDPAdapterAgent;
        synchronized (sDMSDPLock) {
            dMSDPAdapterAgent = sDMSDPAdapter;
        }
        return dMSDPAdapterAgent;
    }

    public static int bindAidlService(Context context, DMSDPAdapterCallback callback) {
        HwLog.d(TAG, "bindAidlService");
        synchronized (sDMSDPLock) {
            if (sDMSDPBound) {
                return 0;
            }
            Intent intent = new Intent();
            intent.setAction(DMSDP_ACTION_NAME);
            intent.setPackage(DMSDP_PACKAGE_NAME);
            intent.setComponent(new ComponentName(DMSDP_PACKAGE_NAME, DMSDP_SERVICE_NAME));
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
            HwLog.d(DMSDPAdapterAgent.TAG, "DMSDPServiceConnection construct");
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (DMSDPAdapterAgent.sDMSDPLock) {
                HwLog.d(DMSDPAdapterAgent.TAG, "client onServiceConnected  || service " + service);
                IDMSDPAdapterAgent unused = DMSDPAdapterAgent.sDMSDPService = IDMSDPAdapterAgent.Stub.asInterface(service);
                boolean hasInit = false;
                try {
                    hasInit = DMSDPAdapterAgent.sDMSDPService.hasInit();
                } catch (RemoteException e) {
                    HwLog.e(DMSDPAdapterAgent.TAG, "error in onServiceConnected" + e.getLocalizedMessage());
                }
                if (!hasInit) {
                    HwLog.d(DMSDPAdapterAgent.TAG, "DMSDPService has not init. set mDMSDPService = null");
                    IDMSDPAdapterAgent unused2 = DMSDPAdapterAgent.sDMSDPService = null;
                }
                if (DMSDPAdapterAgent.sDMSDPAdapter == null) {
                    DMSDPAdapterAgent unused3 = DMSDPAdapterAgent.sDMSDPAdapter = new DMSDPAdapterAgent();
                }
                DMSDPAdapterAgent unused4 = DMSDPAdapterAgent.sDMSDPAdapter;
                DMSDPAdapterAgent.setDMSDPService(DMSDPAdapterAgent.sDMSDPService);
                DMSDPAdapterAgent unused5 = DMSDPAdapterAgent.sDMSDPAdapter;
                DMSDPServiceConnection unused6 = DMSDPAdapterAgent.sConnection = this;
                boolean unused7 = DMSDPAdapterAgent.sDMSDPBound = true;
                if (DMSDPAdapterAgent.sDMSDPContext == null) {
                    Context unused8 = DMSDPAdapterAgent.sDMSDPContext = this.context;
                }
                if (this.callback != null) {
                    if (DMSDPAdapterAgent.sDMSDPAdapter.hasNullService()) {
                        DMSDPAdapterAgent.callBackNull(this.context, this.callback);
                        return;
                    }
                    this.callback.onAdapterGet(DMSDPAdapterAgent.sDMSDPAdapter);
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            synchronized (DMSDPAdapterAgent.sDMSDPLock) {
                HwLog.d(DMSDPAdapterAgent.TAG, "onServiceDisconnected");
                boolean unused = DMSDPAdapterAgent.sDMSDPBound = false;
                IDMSDPAdapterAgent unused2 = DMSDPAdapterAgent.sDMSDPService = null;
                DMSDPAdapterAgent unused3 = DMSDPAdapterAgent.sDMSDPAdapter = null;
                if (DMSDPAdapterAgent.sThread != null) {
                    if (Build.VERSION.SDK_INT >= 18) {
                        DMSDPAdapterAgent.sThread.quitSafely();
                    } else {
                        DMSDPAdapterAgent.sThread.quit();
                    }
                    HandlerThread unused4 = DMSDPAdapterAgent.sThread = null;
                }
                this.callback = null;
                this.context = null;
            }
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int sendKeyEvent(int businessId, int keyCode, int action) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "sendKeyEvent start");
            if (action >= 0) {
                if (action <= 2) {
                    if (sDMSDPAdapter != null) {
                        IDMSDPAdapterAgent dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                        if (dmsdpService != null) {
                            try {
                                byte[] tmpa = Util.intToByteArray(keyCode);
                                byte[] tmpb = Util.intToByteArray(action);
                                byte[] combined = new byte[(tmpa.length + tmpb.length)];
                                System.arraycopy(tmpa, 0, combined, 0, tmpa.length);
                                System.arraycopy(tmpb, 0, combined, tmpa.length, tmpb.length);
                                return dmsdpService.sendData(businessId, null, 3, combined);
                            } catch (RemoteException e) {
                                HwLog.e(TAG, "sendKeyEvent ERROR:" + e.getLocalizedMessage());
                                return -3;
                            }
                        }
                    }
                    HwLog.e(TAG, "sendKeyEvent DMSDPAdapter or DMSDPService is null");
                    return -2;
                }
            }
            HwLog.e(TAG, "sendKeyEvent invalid action: " + action);
            return -2;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int sendHotWord(int businessId, String hotWord) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "sendHotWord start");
            if (TextUtils.isEmpty(hotWord)) {
                HwLog.e(TAG, "sendHotWord hotWordStr is null or empty");
                return -2;
            }
            if (sDMSDPAdapter != null) {
                IDMSDPAdapterAgent dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.sendData(businessId, null, 2, hotWord.getBytes(StandardCharsets.UTF_8));
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "sendHotWord ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "sendHotWord DMSDPAdapter or DMSDPService is null");
            return -2;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int setDeviceInfo(int businessId, DeviceInfo deviceInfo) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "setDeviceInfo start");
            if (deviceInfo == null) {
                HwLog.d(TAG, "setDeviceInfo: deviceInfo is null");
                return -2;
            }
            if (sDMSDPAdapter != null) {
                IDMSDPAdapterAgent dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                if (dmsdpService != null) {
                    try {
                        return dmsdpService.setDeviceInfo(businessId, deviceInfo);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "collectDeviceInfo ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "setDeviceInfo DMSDPService is null, createInstance ERROR");
            return -2;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int getTrustDeviceList(int businessId, List<DMSDPDevice> devices) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "getTrustDeviceList start");
            if (sDMSDPAdapter == null) {
                HwLog.e(TAG, "getTrustDeviceList mDMSDPAdapter is null");
                return -2;
            }
            IDMSDPAdapterAgent dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
            if (dmsdpService == null) {
                HwLog.e(TAG, "getTrustDeviceList dmsdpService is null");
                return -2;
            } else if (devices == null) {
                HwLog.e(TAG, "getTrustDeviceList devices is null");
                return -2;
            } else {
                try {
                    return dmsdpService.getTrustDeviceList(businessId, devices);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "getTrustDeviceList failed");
                    return -3;
                }
            }
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public int deleteTrustDevice(int businessId, String deviceId) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "deleteTrustDevice start");
            if (sDMSDPAdapter == null) {
                HwLog.e(TAG, "deleteTrustDevice mDMSDPAdapter is null");
                return -2;
            }
            IDMSDPAdapterAgent dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
            if (dmsdpService == null) {
                HwLog.e(TAG, "deleteTrustDevice dmsdpService is null");
                return -2;
            } else if (TextUtils.isEmpty(deviceId)) {
                HwLog.e(TAG, "deleteTrustDevice deviceId is empty");
                return -2;
            } else {
                try {
                    return dmsdpService.deleteTrustDevice(businessId, deviceId);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "deleteTrustDevice failed");
                    return -3;
                }
            }
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public void setSecureFileListener(int businessId, ISecureFileListener listener) {
        synchronized (sDMSDPLock) {
            HwLog.i(TAG, "setSecureFileListener start");
            if (sDMSDPAdapter == null) {
                HwLog.e(TAG, "setSecureFileListener mDMSDPAdapter is null");
                return;
            }
            IDMSDPAdapterAgent dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
            if (dmsdpService == null) {
                HwLog.e(TAG, "setSecureFileListener dmsdpService is null");
                return;
            }
            try {
                dmsdpService.setSecureFileListener(businessId, listener);
            } catch (RemoteException e) {
                HwLog.e(TAG, "setSecureFileListener failed");
            }
        }
    }

    public static int sendHiSightMotionEvent(MotionEvent event) {
        IDMSDPAdapterAgent iDMSDPAdapterAgent;
        HwLog.d(TAG, "sendMotionEvent start");
        if (event == null || (iDMSDPAdapterAgent = sDMSDPService) == null) {
            HwLog.e(TAG, "sendMotionEvent event but DMSDPService is null");
            return -1;
        }
        try {
            return iDMSDPAdapterAgent.sendData(1, null, 4, Util.parcelableToByteArray(event));
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in sendMotionEvent: " + e.getLocalizedMessage());
            return -1;
        }
    }

    public static int sendHiSightKeyEvent(KeyEvent event) {
        IDMSDPAdapterAgent iDMSDPAdapterAgent;
        HwLog.d(TAG, "sendKeyEvent start");
        if (event == null || (iDMSDPAdapterAgent = sDMSDPService) == null) {
            HwLog.e(TAG, "sendKeyEvent event or DMSDPService is null");
            return -1;
        }
        try {
            return iDMSDPAdapterAgent.sendData(1, null, 5, Util.parcelableToByteArray(event));
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in sendKeyEvent: " + e.getLocalizedMessage());
            return -1;
        }
    }

    @Override // com.huawei.dmsdpsdk2.DMSDPAdapter
    public void reportData(Map params) {
        synchronized (sDMSDPLock) {
            if (params != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapterAgent dmsdpService = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dmsdpService == null) {
                        HwLog.e(TAG, "service error");
                        return;
                    }
                    try {
                        dmsdpService.reportData(params);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "connectDevice ERROR:" + e.getLocalizedMessage());
                    }
                    return;
                }
            }
            HwLog.e(TAG, "params or adapter is null");
        }
    }
}
