package com.huawei.distributed.kms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import com.huawei.android.util.SlogEx;
import com.huawei.coauthservice.identitymgr.constants.ServicePackage;
import com.huawei.distributed.kms.IHwDeviceQueryCallback;
import com.huawei.distributed.kms.IHwKeyManagerService;
import com.huawei.distributed.kms.IHwKeyOpCallback;
import com.huawei.distributed.kms.entity.DistributedDeviceInfo;
import com.huawei.distributed.kms.entity.DistributedKeyInfo;
import com.huawei.distributed.kms.entity.DistributedKeySecurityLevel;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwDistributedKeyManager {
    private static final String ACTION = "com.huawei.distributed.kms.service.HwKeyManagerService";
    private static final int DEFAULT_SET_SIZE = 16;
    public static final int ERROR_CODE_DEVICE_DISCONNECTED = -101;
    public static final int ERROR_CODE_EMPTY_PARAMS = -25;
    public static final int ERROR_CODE_INVALID_PARAMS = -5;
    public static final int ERROR_CODE_REQUEST_TIMEOUT = -2;
    private static final String HKMS_PACKAGE_NAME = "com.huawei.distributed.kms";
    private static final String HKMS_SERVICE_NAME = "com.huawei.distributed.kms.service.HwKeyManagerService";
    private static final Object LOCK = new Object();
    public static final int RELATION_TYPE_DMC = 1;
    public static final int RELATION_TYPE_HICOM = 2;
    public static final int RELATION_TYPE_HICOM_PTP = 4;
    private static final int RELATION_TYPE_MAX = 7;
    public static final int RELATION_TYPE_NONE = 0;
    private static final int RELATION_TYPE_SUPPORT_ETS = -1;
    public static final int SUCCESS = 0;
    private static final String TAG = "HwDistributedKeyManager";
    private static volatile HwDistributedKeyManager sInstance = null;
    private final HashMap<IConnectServiceCallback, Integer> mConnectionCallbacks = new HashMap<>(16);
    @NonNull
    private Context mContext;
    private boolean mIsBindingService;
    private IHwKeyManagerService mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.huawei.distributed.kms.HwDistributedKeyManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            SlogEx.i(HwDistributedKeyManager.TAG, "Connect hkms success.");
            HwDistributedKeyManager.this.serviceConnected(service);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            SlogEx.i(HwDistributedKeyManager.TAG, "Hkms disconnected.");
            HwDistributedKeyManager.this.serviceDisconnected();
        }
    };
    private IBinder mToken = new Binder();

    public interface IConnectServiceCallback {
        void onConnectFailed();

        void onConnected(List<DistributedDeviceInfo> list);

        void onDisconnect();
    }

    public interface IDistributedKeyCallback {
        void onDeviceConnect(String str);

        void onDeviceDisconnected(String str);

        void onResult(int i, int i2, int i3, Bundle bundle);
    }

    public interface IDistributedKeySession {
        void closeSession();

        boolean requestDeliverKey(DistributedKeyInfo distributedKeyInfo, DistributedKeySecurityLevel distributedKeySecurityLevel, int i, Bundle bundle);

        boolean requestEscrowKey(DistributedKeyInfo distributedKeyInfo, DistributedKeySecurityLevel distributedKeySecurityLevel, int i, Bundle bundle);
    }

    private HwDistributedKeyManager(Context context) {
        this.mContext = context.getApplicationContext();
        if (!isHkmsPackgeExists()) {
            throw new IllegalArgumentException("Distributed key manager service is not exist!");
        }
    }

    public static HwDistributedKeyManager getInstance(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (HwDistributedKeyManager.class) {
                if (sInstance == null) {
                    if (context != null) {
                        sInstance = new HwDistributedKeyManager(context);
                    } else {
                        throw new IllegalArgumentException("Context is null");
                    }
                }
            }
        }
        return sInstance;
    }

    public void connectService(IConnectServiceCallback callback, int relationType, Bundle options) {
        if (relationType < -1 || relationType > 7) {
            callback.onConnectFailed();
            return;
        }
        synchronized (LOCK) {
            if (callback == null) {
                SlogEx.e(TAG, "IConnectServiceCallback is null, can not connect service.");
                return;
            }
            this.mConnectionCallbacks.put(callback, Integer.valueOf(relationType));
            connectServiceInner(callback);
        }
    }

    public void disconnectService() {
        HashMap<IConnectServiceCallback, Integer> callbacks = null;
        synchronized (LOCK) {
            if (this.mService != null) {
                SlogEx.i(TAG, "Unbind HKMS service.");
                this.mContext.unbindService(this.mServiceConnection);
                this.mIsBindingService = false;
                this.mService = null;
                callbacks = new HashMap<>(this.mConnectionCallbacks);
                this.mConnectionCallbacks.clear();
            }
        }
        if (callbacks != null) {
            for (IConnectServiceCallback connect : callbacks.keySet()) {
                connect.onDisconnect();
            }
        }
    }

    private void connectServiceInner(IConnectServiceCallback connectCallback) {
        int type;
        if (this.mService != null) {
            SlogEx.i(TAG, "Service already connected, execute task.");
            synchronized (LOCK) {
                type = this.mConnectionCallbacks.getOrDefault(connectCallback, 0).intValue();
            }
            queryDeviceInfo(connectCallback, type);
            return;
        }
        HashMap<IConnectServiceCallback, Integer> callbacks = null;
        synchronized (LOCK) {
            if (!this.mIsBindingService) {
                SlogEx.i(TAG, "Start bindService.");
                boolean isBind = this.mContext.bindService(new Intent().setAction("com.huawei.distributed.kms.service.HwKeyManagerService").setComponent(new ComponentName(HKMS_PACKAGE_NAME, "com.huawei.distributed.kms.service.HwKeyManagerService")), this.mServiceConnection, 1);
                this.mIsBindingService = true;
                if (!isBind) {
                    this.mIsBindingService = false;
                    callbacks = new HashMap<>(this.mConnectionCallbacks);
                    this.mConnectionCallbacks.clear();
                }
            }
        }
        if (callbacks != null) {
            for (IConnectServiceCallback callback : callbacks.keySet()) {
                callback.onConnectFailed();
            }
        }
    }

    private boolean isHkmsPackgeExists() {
        ApplicationInfo info;
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            if (packageManager == null || (info = packageManager.getApplicationInfo(HKMS_PACKAGE_NAME, 0)) == null || !isSystemApp(info) || !isSignedWithPlatformKey(HKMS_PACKAGE_NAME)) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            SlogEx.e(TAG, "Package not exist.");
        }
        return false;
    }

    private boolean isSystemApp(ApplicationInfo info) {
        return (info.flags & 1) != 0;
    }

    private boolean isSignedWithPlatformKey(String packageName) {
        return this.mContext.getPackageManager().checkSignatures(packageName, ServicePackage.CHECK_PACKAGE) == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void serviceConnected(IBinder service) {
        HashMap<IConnectServiceCallback, Integer> callbacks;
        synchronized (LOCK) {
            this.mIsBindingService = false;
            this.mService = IHwKeyManagerService.Stub.asInterface(service);
            callbacks = new HashMap<>(this.mConnectionCallbacks);
        }
        if (!callbacks.isEmpty()) {
            for (Map.Entry<IConnectServiceCallback, Integer> entry : callbacks.entrySet()) {
                queryDeviceInfo(entry.getKey(), entry.getValue().intValue());
            }
        }
    }

    private void queryDeviceInfo(final IConnectServiceCallback connectCallback, int type) {
        if (type == 0) {
            connectCallback.onConnected(Collections.emptyList());
            return;
        }
        try {
            this.mService.getDistributedDeviceInfo(type, new IHwDeviceQueryCallback.Stub() {
                /* class com.huawei.distributed.kms.HwDistributedKeyManager.AnonymousClass2 */

                @Override // com.huawei.distributed.kms.IHwDeviceQueryCallback
                public void onGetDistributedDeviceInfoList(List<DistributedDeviceInfo> list) throws RemoteException {
                    connectCallback.onConnected(list);
                }
            });
        } catch (RemoteException e) {
            SlogEx.e(TAG, "Can not get distributed device info.");
            connectCallback.onConnected(Collections.emptyList());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void serviceDisconnected() {
        HashMap<IConnectServiceCallback, Integer> callbacks;
        synchronized (LOCK) {
            this.mService = null;
            this.mIsBindingService = false;
            callbacks = new HashMap<>(this.mConnectionCallbacks);
            this.mConnectionCallbacks.clear();
        }
        if (!callbacks.isEmpty()) {
            for (IConnectServiceCallback connect : callbacks.keySet()) {
                connect.onDisconnect();
            }
        }
    }

    public IDistributedKeySession createKeyDeliverySession(@NonNull IDistributedKeyCallback callback, @NonNull DistributedDeviceInfo deviceInfo, Bundle options) {
        if (this.mService != null) {
            HwDeliverySession hwDeliverySession = new HwDeliverySession(deviceInfo, options, callback);
            hwDeliverySession.setHwKeyOpSession();
            return hwDeliverySession;
        }
        throw new IllegalArgumentException("Service is not connected, please call connectService() first!");
    }

    private class HwDeliverySession implements IDistributedKeySession {
        private DistributedDeviceInfo mDeviceInfo;
        private IDistributedKeyCallback mIDeliverKeyCallback;
        private IHwKeyOpSession mIHwKeyOpSession;
        private Bundle mOptions;

        HwDeliverySession(DistributedDeviceInfo deviceInfo, Bundle options, IDistributedKeyCallback callback) {
            this.mDeviceInfo = deviceInfo;
            this.mOptions = options;
            this.mIDeliverKeyCallback = callback;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setHwKeyOpSession() {
            IHwKeyOpCallback callback = new IHwKeyOpCallback.Stub() {
                /* class com.huawei.distributed.kms.HwDistributedKeyManager.HwDeliverySession.AnonymousClass1 */

                @Override // com.huawei.distributed.kms.IHwKeyOpCallback
                public void onDeviceConnect(String deviceId) throws RemoteException {
                    SlogEx.i(HwDistributedKeyManager.TAG, "Create session success.");
                    if (HwDeliverySession.this.mIDeliverKeyCallback == null) {
                        SlogEx.i(HwDistributedKeyManager.TAG, "Callback is empty!");
                    } else {
                        HwDeliverySession.this.mIDeliverKeyCallback.onDeviceConnect(deviceId);
                    }
                }

                @Override // com.huawei.distributed.kms.IHwKeyOpCallback
                public void onDeviceDisconnected(String deviceId) throws RemoteException {
                    SlogEx.i(HwDistributedKeyManager.TAG, "Device not connected!");
                    if (HwDeliverySession.this.mIDeliverKeyCallback == null) {
                        SlogEx.i(HwDistributedKeyManager.TAG, "Callback is empty!");
                    } else {
                        HwDeliverySession.this.mIDeliverKeyCallback.onDeviceDisconnected(deviceId);
                    }
                }

                @Override // com.huawei.distributed.kms.IHwKeyOpCallback
                public void onResult(int requestCode, int type, int resultCode, Bundle data) throws RemoteException {
                    if (HwDeliverySession.this.mIDeliverKeyCallback == null) {
                        SlogEx.i(HwDistributedKeyManager.TAG, "Callback is empty!");
                    } else {
                        HwDeliverySession.this.mIDeliverKeyCallback.onResult(requestCode, type, resultCode, data);
                    }
                }
            };
            try {
                if (HwDistributedKeyManager.this.mService == null) {
                    SlogEx.e(HwDistributedKeyManager.TAG, "Service is not connected, cannot createSession!");
                } else {
                    this.mIHwKeyOpSession = HwDistributedKeyManager.this.mService.createKeySession(HwDistributedKeyManager.this.mToken, callback, this.mDeviceInfo, this.mOptions);
                }
            } catch (RemoteException e) {
                SlogEx.e(HwDistributedKeyManager.TAG, "Cannot create KeySession");
                IDistributedKeyCallback iDistributedKeyCallback = this.mIDeliverKeyCallback;
                if (iDistributedKeyCallback != null) {
                    iDistributedKeyCallback.onDeviceDisconnected(this.mDeviceInfo.getDeviceId());
                    this.mIDeliverKeyCallback = null;
                }
                this.mIHwKeyOpSession = null;
            }
        }

        @Override // com.huawei.distributed.kms.HwDistributedKeyManager.IDistributedKeySession
        public boolean requestDeliverKey(DistributedKeyInfo info, DistributedKeySecurityLevel level, int requestCode, Bundle options) {
            if (!isSessionValid()) {
                SlogEx.e(HwDistributedKeyManager.TAG, "Session is not valid, can not request delivery key.");
                return false;
            }
            try {
                return this.mIHwKeyOpSession.requestDeliverKey(info, level, requestCode, options);
            } catch (RemoteException e) {
                SlogEx.e(HwDistributedKeyManager.TAG, "Remote Exception when call request DeliverKey");
                return false;
            }
        }

        @Override // com.huawei.distributed.kms.HwDistributedKeyManager.IDistributedKeySession
        public boolean requestEscrowKey(DistributedKeyInfo info, DistributedKeySecurityLevel level, int requestCode, Bundle options) {
            if (!isSessionValid()) {
                SlogEx.e(HwDistributedKeyManager.TAG, "Session is not valid, can not request escrow key.");
                return false;
            }
            try {
                return this.mIHwKeyOpSession.requestEscrowKey(info, level, requestCode, options);
            } catch (RemoteException e) {
                SlogEx.e(HwDistributedKeyManager.TAG, "Remote Exception when call requestEscrowKey");
                return false;
            }
        }

        @Override // com.huawei.distributed.kms.HwDistributedKeyManager.IDistributedKeySession
        public void closeSession() {
            this.mIDeliverKeyCallback = null;
            if (!isSessionValid()) {
                SlogEx.e(HwDistributedKeyManager.TAG, "No need to close this session.");
                this.mIHwKeyOpSession = null;
                return;
            }
            try {
                this.mIHwKeyOpSession.closeSession();
            } catch (RemoteException e) {
                SlogEx.e(HwDistributedKeyManager.TAG, "Remote exception when call closeSession");
            }
            this.mIHwKeyOpSession = null;
        }

        private boolean isSessionValid() {
            if (HwDistributedKeyManager.this.mService == null) {
                SlogEx.e(HwDistributedKeyManager.TAG, "Service is not connected.");
                return false;
            } else if (this.mIHwKeyOpSession != null) {
                return true;
            } else {
                SlogEx.e(HwDistributedKeyManager.TAG, "Session is not valid!");
                return false;
            }
        }
    }
}
