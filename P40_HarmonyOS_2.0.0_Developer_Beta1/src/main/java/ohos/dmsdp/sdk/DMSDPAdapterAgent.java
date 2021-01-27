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
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import ohos.dmsdp.sdk.agent.IDMSDPAdapter;
import ohos.dmsdp.sdk.util.Util;

public final class DMSDPAdapterAgent extends DMSDPAdapter {
    private static final String DMSDP_ACTION_NAME = "com.huawei.dmsdpdevice.DMSDP_DEVICE_SERVICE";
    private static final Object DMSDP_LOCK = new Object();
    private static final String DMSDP_PACKAGE_NAME = "com.huawei.dmsdpdevice";
    private static final String DMSDP_SERVICE_NAME = "com.huawei.dmsdpdevice.DMSDPService";
    private static final int KEY_ACTION_MAX = 2;
    private static final int KEY_ACTION_MIN = 0;
    private static final String TAG = "DMSDPAdapterAgent";
    private static DMSDPServiceConnection sConnection;
    private static DMSDPAdapterAgent sDMSDPAdapter;
    private static boolean sDMSDPBound = false;
    private static Context sDMSDPContext;
    private static IDMSDPAdapter sDMSDPService;
    private static HandlerThread sThread;

    static class DMSDPServiceWrapperAgent implements DMSDPServiceWrapper {
        DMSDPServiceWrapperAgent() {
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public boolean hasNullService() {
            return DMSDPAdapterAgent.sDMSDPService == null;
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int startDiscover(int i, int i2, int i3, int i4, IDiscoverListener iDiscoverListener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.startDiscover(i, i2, i3, i4, iDiscoverListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int stopDiscover(int i, int i2, IDiscoverListener iDiscoverListener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.stopDiscover(i, i2, iDiscoverListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int startScan(int i, int i2) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.startScan(i, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int stopScan(int i, int i2) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.stopScan(i, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int connectDevice(int i, int i2, DMSDPDevice dMSDPDevice, Map<String, Object> map) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.connectDevice(i, i2, dMSDPDevice, map);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int disconnectDevice(int i, int i2, DMSDPDevice dMSDPDevice) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.disconnectDevice(i, i2, dMSDPDevice);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int requestDeviceService(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.requestDeviceService(i, dMSDPDevice, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int startDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map<String, Object> map) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.startDeviceService(i, dMSDPDeviceService, i2, map);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int stopDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.stopDeviceService(i, dMSDPDeviceService, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int updateDeviceService(int i, DMSDPDeviceService dMSDPDeviceService, int i2, Map<String, Object> map) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.updateDeviceService(i, dMSDPDeviceService, i2, map);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int registerDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.registerDMSDPListener(i, iDMSDPListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int unRegisterDMSDPListener(int i, IDMSDPListener iDMSDPListener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.unRegisterDMSDPListener(i, iDMSDPListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int registerDataListener(int i, DMSDPDevice dMSDPDevice, int i2, IDataListener iDataListener) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.registerDataListener(i, dMSDPDevice, i2, iDataListener);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int unRegisterDataListener(int i, DMSDPDevice dMSDPDevice, int i2) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.unRegisterDataListener(i, dMSDPDevice, i2);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public int sendData(int i, DMSDPDevice dMSDPDevice, int i2, byte[] bArr) throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.sendData(i, dMSDPDevice, i2, bArr);
        }

        @Override // ohos.dmsdp.sdk.DMSDPServiceWrapper
        public boolean hasInit() throws RemoteException {
            return DMSDPAdapterAgent.sDMSDPService.hasInit();
        }
    }

    private DMSDPAdapterAgent() {
        super(new DMSDPServiceWrapperAgent());
        synchronized (DMSDP_LOCK) {
            if (sThread == null) {
                sThread = new HandlerThread("DMSDPAdapter Looper");
                sThread.start();
            }
        }
        HwLog.d(TAG, "DMSDPAdapter init");
    }

    public static int getDMSDPAdapter(Context context, DMSDPAdapterCallback dMSDPAdapterCallback) {
        HwLog.d(TAG, "getDMSDPAdapter");
        if (context == null) {
            HwLog.e(TAG, "context is null and return.");
            return -2;
        } else if (dMSDPAdapterCallback == null) {
            HwLog.e(TAG, "callback is null and return.");
            return -2;
        } else {
            synchronized (DMSDP_LOCK) {
                if (sDMSDPAdapter != null) {
                    dMSDPAdapterCallback.onAdapterGet(sDMSDPAdapter);
                    return 0;
                }
                return bindAidlService(context, dMSDPAdapterCallback);
            }
        }
    }

    /* access modifiers changed from: private */
    public static void setDMSDPService(IDMSDPAdapter iDMSDPAdapter) {
        sDMSDPService = iDMSDPAdapter;
    }

    public static int createInstance(Context context, DMSDPAdapterCallback dMSDPAdapterCallback) {
        HwLog.d(TAG, "createInstance start ");
        if (context == null || dMSDPAdapterCallback == null) {
            HwLog.e(TAG, "createInstance context or callback null");
            throw new IllegalArgumentException("createInstance context or callback null");
        }
        synchronized (DMSDP_LOCK) {
            if (sDMSDPAdapter != null) {
                sDMSDPContext = context;
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

    public static void releaseInstance() {
        synchronized (DMSDP_LOCK) {
            if (sDMSDPContext == null) {
                HwLog.e(TAG, "Instance of DMDSPAdapter already released or have not got yet");
                return;
            }
            unbindAidlService(sDMSDPContext);
            sDMSDPContext = null;
        }
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
        DMSDPAdapterAgent dMSDPAdapterAgent;
        synchronized (DMSDP_LOCK) {
            dMSDPAdapterAgent = sDMSDPAdapter;
        }
        return dMSDPAdapterAgent;
    }

    public static int bindAidlService(Context context, DMSDPAdapterCallback dMSDPAdapterCallback) {
        HwLog.d(TAG, "bindAidlService");
        synchronized (DMSDP_LOCK) {
            if (sDMSDPBound) {
                return 0;
            }
            Intent intent = new Intent();
            intent.setAction(DMSDP_ACTION_NAME);
            intent.setPackage(DMSDP_PACKAGE_NAME);
            intent.setComponent(new ComponentName(DMSDP_PACKAGE_NAME, DMSDP_SERVICE_NAME));
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
            HwLog.d(DMSDPAdapterAgent.TAG, "DMSDPServiceConnection construct");
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            synchronized (DMSDPAdapterAgent.DMSDP_LOCK) {
                HwLog.d(DMSDPAdapterAgent.TAG, "client onServiceConnected  || service " + iBinder);
                IDMSDPAdapter unused = DMSDPAdapterAgent.sDMSDPService = IDMSDPAdapter.Stub.asInterface(iBinder);
                boolean z = false;
                try {
                    z = DMSDPAdapterAgent.sDMSDPService.hasInit();
                } catch (RemoteException e) {
                    HwLog.e(DMSDPAdapterAgent.TAG, "error in onServiceConnected" + e.getLocalizedMessage());
                } catch (Exception e2) {
                    HwLog.e(DMSDPAdapterAgent.TAG, "error in onServiceConnected" + e2.getLocalizedMessage());
                }
                if (!z) {
                    HwLog.d(DMSDPAdapterAgent.TAG, "DMSDPService has not init. set mDMSDPService = null");
                    IDMSDPAdapter unused2 = DMSDPAdapterAgent.sDMSDPService = null;
                }
                if (DMSDPAdapterAgent.sDMSDPAdapter == null) {
                    DMSDPAdapterAgent unused3 = DMSDPAdapterAgent.sDMSDPAdapter = new DMSDPAdapterAgent();
                }
                DMSDPAdapterAgent.setDMSDPService(DMSDPAdapterAgent.sDMSDPService);
                DMSDPServiceConnection unused4 = DMSDPAdapterAgent.sConnection = this;
                boolean unused5 = DMSDPAdapterAgent.sDMSDPBound = true;
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
        public void onServiceDisconnected(ComponentName componentName) {
            synchronized (DMSDPAdapterAgent.DMSDP_LOCK) {
                HwLog.d(DMSDPAdapterAgent.TAG, "onServiceDisconnected");
                boolean unused = DMSDPAdapterAgent.sDMSDPBound = false;
                IDMSDPAdapter unused2 = DMSDPAdapterAgent.sDMSDPService = null;
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

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int sendKeyEvent(int i, int i2, int i3) {
        IDMSDPAdapter dMSDPServiceInner;
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "sendKeyEvent start");
            if (i3 >= 0) {
                if (i3 <= 2) {
                    if (sDMSDPAdapter == null || (dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner()) == null) {
                        HwLog.e(TAG, "sendKeyEvent DMSDPAdapter or DMSDPService is null");
                        return -2;
                    }
                    try {
                        byte[] intToByteArray = Util.intToByteArray(i2);
                        byte[] intToByteArray2 = Util.intToByteArray(i3);
                        byte[] bArr = new byte[(intToByteArray.length + intToByteArray2.length)];
                        System.arraycopy(intToByteArray, 0, bArr, 0, intToByteArray.length);
                        System.arraycopy(intToByteArray2, 0, bArr, intToByteArray.length, intToByteArray2.length);
                        return dMSDPServiceInner.sendData(i, null, 3, bArr);
                    } catch (RemoteException e) {
                        HwLog.e(TAG, "sendKeyEvent ERROR:" + e.getLocalizedMessage());
                        return -3;
                    }
                }
            }
            HwLog.e(TAG, "sendKeyEvent invalid action: " + i3);
            return -2;
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int sendHotWord(int i, String str) {
        IDMSDPAdapter dMSDPServiceInner;
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "sendHotWord start");
            if (TextUtils.isEmpty(str)) {
                HwLog.e(TAG, "sendHotWord hotWordStr is null or empty");
                return -2;
            } else if (sDMSDPAdapter == null || (dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner()) == null) {
                HwLog.e(TAG, "sendHotWord DMSDPAdapter or DMSDPService is null");
                return -2;
            } else {
                try {
                    return dMSDPServiceInner.sendData(i, null, 2, str.getBytes(StandardCharsets.UTF_8));
                } catch (RemoteException e) {
                    HwLog.e(TAG, "sendHotWord ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int setDeviceInfo(int i, DeviceInfo deviceInfo) {
        IDMSDPAdapter dMSDPServiceInner;
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "setDeviceInfo start");
            if (deviceInfo == null) {
                HwLog.d(TAG, "setDeviceInfo: deviceInfo is null");
                return -2;
            } else if (sDMSDPAdapter == null || (dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner()) == null) {
                HwLog.e(TAG, "setDeviceInfo DMSDPService is null, createInstance ERROR");
                return -2;
            } else {
                try {
                    return dMSDPServiceInner.setDeviceInfo(i, deviceInfo);
                } catch (RemoteException e) {
                    HwLog.e(TAG, "collectDeviceInfo ERROR:" + e.getLocalizedMessage());
                    return -3;
                }
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int getTrustDeviceList(int i, List<DMSDPDevice> list) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "getTrustDeviceList start");
            if (sDMSDPAdapter == null) {
                HwLog.e(TAG, "getTrustDeviceList mDMSDPAdapter is null");
                return -2;
            }
            IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
            if (dMSDPServiceInner == null) {
                HwLog.e(TAG, "getTrustDeviceList dmsdpService is null");
                return -2;
            } else if (list == null) {
                HwLog.e(TAG, "getTrustDeviceList devices is null");
                return -2;
            } else {
                try {
                    return dMSDPServiceInner.getTrustDeviceList(i, list);
                } catch (RemoteException unused) {
                    HwLog.e(TAG, "getTrustDeviceList failed");
                    return -3;
                }
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int deleteTrustDevice(int i, String str) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "deleteTrustDevice start");
            if (sDMSDPAdapter == null) {
                HwLog.e(TAG, "deleteTrustDevice mDMSDPAdapter is null");
                return -2;
            }
            IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
            if (dMSDPServiceInner == null) {
                HwLog.e(TAG, "deleteTrustDevice dmsdpService is null");
                return -2;
            } else if (TextUtils.isEmpty(str)) {
                HwLog.e(TAG, "deleteTrustDevice deviceId is empty");
                return -2;
            } else {
                try {
                    return dMSDPServiceInner.deleteTrustDevice(i, str);
                } catch (RemoteException unused) {
                    HwLog.e(TAG, "deleteTrustDevice failed");
                    return -3;
                }
            }
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public void setSecureFileListener(int i, ISecureFileListener iSecureFileListener) {
        synchronized (DMSDP_LOCK) {
            HwLog.i(TAG, "setSecureFileListener start");
            if (sDMSDPAdapter == null) {
                HwLog.e(TAG, "setSecureFileListener mDMSDPAdapter is null");
                return;
            }
            IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
            if (dMSDPServiceInner == null) {
                HwLog.e(TAG, "setSecureFileListener dmsdpService is null");
                return;
            }
            try {
                dMSDPServiceInner.setSecureFileListener(i, iSecureFileListener);
            } catch (RemoteException unused) {
                HwLog.e(TAG, "setSecureFileListener failed");
            }
        }
    }

    public static int sendHiSightMotionEvent(MotionEvent motionEvent) {
        IDMSDPAdapter iDMSDPAdapter;
        HwLog.d(TAG, "sendMotionEvent start");
        if (motionEvent == null || (iDMSDPAdapter = sDMSDPService) == null) {
            HwLog.e(TAG, "sendMotionEvent event but DMSDPService is null");
            return -1;
        }
        try {
            return iDMSDPAdapter.sendData(1, null, 4, Util.parcelableToByteArray(motionEvent));
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in sendMotionEvent: " + e.getLocalizedMessage());
            return -1;
        }
    }

    public static int sendHiSightKeyEvent(KeyEvent keyEvent) {
        IDMSDPAdapter iDMSDPAdapter;
        HwLog.d(TAG, "sendKeyEvent start");
        if (keyEvent == null || (iDMSDPAdapter = sDMSDPService) == null) {
            HwLog.e(TAG, "sendKeyEvent event or DMSDPService is null");
            return -1;
        }
        try {
            return iDMSDPAdapter.sendData(1, null, 5, Util.parcelableToByteArray(keyEvent));
        } catch (RemoteException e) {
            HwLog.e(TAG, "error in sendKeyEvent: " + e.getLocalizedMessage());
            return -1;
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public int setVideoSurface(int i, Surface surface) {
        IDMSDPAdapter iDMSDPAdapter;
        HwLog.i(TAG, "setVideoSurface start");
        if (surface == null || (iDMSDPAdapter = sDMSDPService) == null) {
            HwLog.e(TAG, "setVideoSurface event or DMSDPService is null");
            return -1;
        }
        try {
            return iDMSDPAdapter.setVideoSurface(i, surface);
        } catch (RemoteException e) {
            HwLog.e(TAG, "setVideoSurface start: " + e.getLocalizedMessage());
            return -1;
        }
    }

    @Override // ohos.dmsdp.sdk.DMSDPAdapter
    public void reportData(Map<String, Object> map) {
        synchronized (DMSDP_LOCK) {
            if (map != null) {
                if (sDMSDPAdapter != null) {
                    IDMSDPAdapter dMSDPServiceInner = sDMSDPAdapter.getDMSDPServiceInner();
                    if (dMSDPServiceInner == null) {
                        HwLog.e(TAG, "service error");
                        return;
                    }
                    try {
                        dMSDPServiceInner.reportData(map);
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
