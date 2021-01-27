package com.huawei.distributed.teedatatransfer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.ArraySet;
import com.huawei.android.util.SlogEx;
import com.huawei.distributed.kms.HwDistributedKeyManager;
import com.huawei.distributed.teedatatransfer.IConnectServiceCallback;
import com.huawei.distributed.teedatatransfer.IDataTransferCallback;
import com.huawei.distributed.teedatatransfer.IDataTransferService;
import com.huawei.distributed.teedatatransfer.entity.DataTransferInfo;
import com.huawei.hwpartsecurity.BuildConfig;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class TeeDataTransferManager {
    private static final String ACTION = "com.huawei.distributed.teedatatransfer.service.TeeDataTransferService";
    public static final int CREATE_SESSION_FAILED = -2;
    private static final int DEFAULT_SET_SIZE = 5;
    private static final String HKMS_PACKAGE_NAME = "com.huawei.distributed.kms";
    private static final Object LOCK = new Object();
    private static final String LOG_TAG = "TeeDataTransferManager";
    public static final int OPERATE_BEGIN = 0;
    public static final int OPERATE_FINISH = 2;
    public static final int OPERATE_UPDATE = 1;
    public static final int OPERATION_FAIL = -4;
    public static final int OPERATION_SUCCESS = 0;
    private static final String PACKAGE_NAME = "packageName";
    public static final int READ = 0;
    private static final int SERVICE_CONNECT = 0;
    private static final int SERVICE_DISCONNECT = 1;
    private static final int SERVICE_FAILED = 2;
    private static final String TDTS_SERVICE_NAME = "com.huawei.distributed.teedatatransfer.service.TeeDataTransferService";
    public static final int UPDATE_CONTINUE = 1;
    public static final int UPDATE_FINISH = 0;
    public static final int WRITE = 1;
    private static volatile TeeDataTransferManager sInstance = null;
    private final Set<IServiceConnectionCallback> mConnectionCallbacks = new CopyOnWriteArraySet();
    private Context mContext;
    private HwDistributedKeyManager mHwDistributedKeyManager;
    private boolean mIsBindingService;
    private boolean mIsServiceConnected;
    private final Handler mMainHandler = new MainHandler(Looper.getMainLooper());
    private final String mPackageName;
    private IDataTransferService mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.huawei.distributed.teedatatransfer.TeeDataTransferManager.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            SlogEx.i(TeeDataTransferManager.LOG_TAG, "connect data transfer service success.");
            synchronized (TeeDataTransferManager.LOCK) {
                TeeDataTransferManager.this.mIsBindingService = false;
                TeeDataTransferManager.this.mService = IDataTransferService.Stub.asInterface(service);
                TeeDataTransferManager.this.mIsServiceConnected = true;
            }
            IConnectServiceCallback serviceCallback = new IConnectServiceCallback.Stub() {
                /* class com.huawei.distributed.teedatatransfer.TeeDataTransferManager.AnonymousClass1.AnonymousClass1 */

                @Override // com.huawei.distributed.teedatatransfer.IConnectServiceCallback
                public void onConnected() throws RemoteException {
                    SlogEx.i(TeeDataTransferManager.LOG_TAG, "key manager service has connected");
                    for (IServiceConnectionCallback connect : TeeDataTransferManager.this.mConnectionCallbacks) {
                        TeeDataTransferManager.this.sendMessage(connect, 0);
                    }
                }

                @Override // com.huawei.distributed.teedatatransfer.IConnectServiceCallback
                public void onConnectFailed() throws RemoteException {
                    SlogEx.e(TeeDataTransferManager.LOG_TAG, "key manager service connected failed");
                    for (IServiceConnectionCallback connect : TeeDataTransferManager.this.mConnectionCallbacks) {
                        TeeDataTransferManager.this.sendMessage(connect, 2);
                    }
                }

                @Override // com.huawei.distributed.teedatatransfer.IConnectServiceCallback
                public void onDisconnect() throws RemoteException {
                    SlogEx.e(TeeDataTransferManager.LOG_TAG, "key manager service has disconnected");
                    for (IServiceConnectionCallback connect : TeeDataTransferManager.this.mConnectionCallbacks) {
                        TeeDataTransferManager.this.sendMessage(connect, 1);
                    }
                }
            };
            synchronized (TeeDataTransferManager.LOCK) {
                try {
                    TeeDataTransferManager.this.mService.connectKeyManagerService(serviceCallback);
                } catch (RemoteException e) {
                    SlogEx.e(TeeDataTransferManager.LOG_TAG, "connectKeyManagerService RemoteException");
                }
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            SlogEx.i(TeeDataTransferManager.LOG_TAG, "data transfer service disconnected.");
            synchronized (TeeDataTransferManager.LOCK) {
                TeeDataTransferManager.this.mIsBindingService = false;
                TeeDataTransferManager.this.mIsServiceConnected = false;
                TeeDataTransferManager.this.mService = null;
            }
            for (IServiceConnectionCallback connect : TeeDataTransferManager.this.mConnectionCallbacks) {
                TeeDataTransferManager.this.sendMessage(connect, 1);
            }
            TeeDataTransferManager.this.mConnectionCallbacks.clear();
            TeeDataTransferManager.this.mHwDistributedKeyManager.disconnectService();
        }
    };
    private IBinder mToken = new Binder();

    public interface IServiceConnectionCallback {
        void onServiceConnectFailed();

        void onServiceConnected();

        void onServiceDisconnected();
    }

    public interface ITeeDataTransferCallback {
        void onResult(int i, DataTransferInfo dataTransferInfo, int i2, Bundle bundle);

        void onSessionCreate(ITeeDataTransferOpSession iTeeDataTransferOpSession);
    }

    public interface ITeeDataTransferOpSession {
        boolean beginOperation(DataTransferInfo dataTransferInfo, Bundle bundle);

        void finishOperation(Bundle bundle);

        boolean updateOperation(DataTransferInfo dataTransferInfo, Bundle bundle);
    }

    private TeeDataTransferManager(Context context) {
        this.mContext = context;
        this.mHwDistributedKeyManager = HwDistributedKeyManager.getInstance(this.mContext);
        this.mPackageName = this.mContext.getPackageName();
    }

    public static TeeDataTransferManager getInstance(@NonNull Context context) {
        if (context != null) {
            if (sInstance == null) {
                synchronized (TeeDataTransferManager.class) {
                    if (sInstance == null) {
                        sInstance = new TeeDataTransferManager(context);
                    }
                }
            }
            return sInstance;
        }
        throw new IllegalArgumentException("context is null.");
    }

    public void connectService(IServiceConnectionCallback callback) {
        if (callback != null) {
            boolean isConnected = false;
            synchronized (LOCK) {
                if (this.mService != null) {
                    SlogEx.i(LOG_TAG, "service already connected, execute task.");
                    isConnected = true;
                }
            }
            if (isConnected) {
                sendMessage(callback, 0);
                return;
            }
            this.mConnectionCallbacks.add(callback);
            Set<IServiceConnectionCallback> connectFailedCallbacks = new ArraySet<>();
            synchronized (LOCK) {
                if (!this.mIsBindingService) {
                    SlogEx.i(LOG_TAG, "start bind service.");
                    try {
                        boolean isBind = this.mContext.bindService(new Intent().setAction("com.huawei.distributed.teedatatransfer.service.TeeDataTransferService").setComponent(new ComponentName(HKMS_PACKAGE_NAME, "com.huawei.distributed.teedatatransfer.service.TeeDataTransferService")), this.mServiceConnection, 1);
                        SlogEx.i(LOG_TAG, "isBind: " + isBind);
                        this.mIsBindingService = true;
                        if (!isBind) {
                            this.mIsBindingService = false;
                            connectFailedCallbacks.addAll(this.mConnectionCallbacks);
                            this.mConnectionCallbacks.clear();
                        }
                    } catch (SecurityException e) {
                        SlogEx.e(LOG_TAG, "the caller does not have permission to access the service or the service can not be found.");
                        sendMessage(callback, 2);
                        return;
                    }
                }
            }
            for (IServiceConnectionCallback connect : connectFailedCallbacks) {
                sendMessage(connect, 2);
            }
            return;
        }
        throw new IllegalArgumentException("callback is null");
    }

    public void createSession(ITeeDataTransferCallback callback, String deviceId, int operationType, Bundle extraBundle) {
        SlogEx.i(LOG_TAG, "start create session");
        synchronized (LOCK) {
            if (this.mService == null) {
                throw new IllegalStateException("service is not connected, please call connectService first!");
            }
        }
        if (callback == null || TextUtils.isEmpty(deviceId) || operationType < 0 || operationType > 1) {
            SlogEx.e(LOG_TAG, "create session invalid params");
            throw new IllegalArgumentException("createSession invalid params");
        }
        Bundle bundle = extraBundle == null ? new Bundle() : extraBundle;
        bundle.putString("packageName", this.mPackageName);
        new TeeDataTransferSession(callback, deviceId, operationType, bundle).setTeeTransferSession();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMessage(IServiceConnectionCallback connect, int serviceConnect) {
        Message msg = Message.obtain();
        msg.what = serviceConnect;
        msg.obj = connect;
        this.mMainHandler.sendMessage(msg);
    }

    private static class MainHandler extends Handler {
        private MainHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 0) {
                if (i != 1) {
                    if (i == 2 && (msg.obj instanceof IServiceConnectionCallback)) {
                        ((IServiceConnectionCallback) msg.obj).onServiceConnectFailed();
                    }
                } else if (msg.obj instanceof IServiceConnectionCallback) {
                    ((IServiceConnectionCallback) msg.obj).onServiceDisconnected();
                }
            } else if (msg.obj instanceof IServiceConnectionCallback) {
                ((IServiceConnectionCallback) msg.obj).onServiceConnected();
            }
        }
    }

    private class TeeDataTransferSession implements ITeeDataTransferOpSession, Handler.Callback {
        private static final int INVALID_SESSION = 0;
        private static final int MSG_ON_RESULT = 2;
        private static final int MSG_ON_SESSION_CREATE = 1;
        private static final String RESULT_BUNDLE = "resultBundle";
        private static final String RESULT_CODE = "resultCode";
        private static final int SESSION_OPERATION = 3;
        private static final String TYPE = "type";
        private static final int VALID_SESSION = 1;
        private final String mDeviceId;
        private final Bundle mExtraBundle;
        private final Handler mHandler = new Handler(Looper.getMainLooper(), this);
        private IDataTransferOpSession mIDataTransferOpSession;
        private ITeeDataTransferCallback mITeeDataTransferCallback;
        private final int mType;

        TeeDataTransferSession(ITeeDataTransferCallback callback, String deviceId, int operationType, Bundle extraBundle) {
            this.mITeeDataTransferCallback = callback;
            this.mDeviceId = deviceId;
            this.mType = operationType;
            this.mExtraBundle = extraBundle;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0025, code lost:
            com.huawei.android.util.SlogEx.e(com.huawei.distributed.teedatatransfer.TeeDataTransferManager.LOG_TAG, "cannot create Session.");
            r7.mITeeDataTransferCallback.onResult(3, null, -2, r7.mExtraBundle);
            r7.mITeeDataTransferCallback = null;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
            return;
         */
        private void setTeeTransferSession() {
            IDataTransferCallback callBack = new IDataTransferCallback.Stub() {
                /* class com.huawei.distributed.teedatatransfer.TeeDataTransferManager.TeeDataTransferSession.AnonymousClass1 */

                @Override // com.huawei.distributed.teedatatransfer.IDataTransferCallback
                public void onSessionCreate(IDataTransferOpSession session) throws RemoteException {
                    SlogEx.i(TeeDataTransferManager.LOG_TAG, "session has callback");
                    TeeDataTransferSession.this.mIDataTransferOpSession = session;
                    Message msg = Message.obtain();
                    msg.what = 1;
                    if (session != null) {
                        SlogEx.i(TeeDataTransferManager.LOG_TAG, "session is valid");
                        msg.arg1 = 1;
                    } else {
                        msg.arg1 = 0;
                    }
                    TeeDataTransferSession.this.mHandler.sendMessage(msg);
                }

                @Override // com.huawei.distributed.teedatatransfer.IDataTransferCallback
                public void onResult(int type, DataTransferInfo transInfo, int resultCode, Bundle bundle) throws RemoteException {
                    SlogEx.i(TeeDataTransferManager.LOG_TAG, "onResult");
                    Message msg = Message.obtain();
                    msg.what = 2;
                    msg.obj = transInfo;
                    Bundle resultBundle = new Bundle();
                    resultBundle.putInt(TeeDataTransferSession.TYPE, type);
                    resultBundle.putInt(TeeDataTransferSession.RESULT_CODE, resultCode);
                    resultBundle.putBundle(TeeDataTransferSession.RESULT_BUNDLE, bundle);
                    msg.setData(resultBundle);
                    TeeDataTransferSession.this.mHandler.sendMessage(msg);
                }
            };
            synchronized (TeeDataTransferManager.LOCK) {
                TeeDataTransferManager.this.mService.createSession(TeeDataTransferManager.this.mToken, callBack, this.mDeviceId, this.mType, this.mExtraBundle);
            }
        }

        @Override // com.huawei.distributed.teedatatransfer.TeeDataTransferManager.ITeeDataTransferOpSession
        public boolean beginOperation(DataTransferInfo transInfo, Bundle extraBundle) {
            if (this.mIDataTransferOpSession == null) {
                SlogEx.e(TeeDataTransferManager.LOG_TAG, "beginOperation session is null");
                return false;
            }
            Bundle bundle = extraBundle == null ? new Bundle() : extraBundle;
            if (!TeeDataTransferManager.this.isValidParam(transInfo, extraBundle)) {
                return false;
            }
            try {
                return this.mIDataTransferOpSession.beginOperation(transInfo, bundle);
            } catch (RemoteException e) {
                SlogEx.e(TeeDataTransferManager.LOG_TAG, "beginOperation aidl remote exception");
                return false;
            }
        }

        @Override // com.huawei.distributed.teedatatransfer.TeeDataTransferManager.ITeeDataTransferOpSession
        public boolean updateOperation(DataTransferInfo transInfo, Bundle extraBundle) {
            if (this.mIDataTransferOpSession == null) {
                SlogEx.e(TeeDataTransferManager.LOG_TAG, "updateOperation session is null");
                return false;
            }
            Bundle bundle = extraBundle == null ? new Bundle() : extraBundle;
            if (!TeeDataTransferManager.this.isValidParam(transInfo, extraBundle)) {
                return false;
            }
            try {
                return this.mIDataTransferOpSession.updateOperation(transInfo, bundle);
            } catch (RemoteException e) {
                SlogEx.e(TeeDataTransferManager.LOG_TAG, "updateOperation aidl remote exception");
                return false;
            }
        }

        @Override // com.huawei.distributed.teedatatransfer.TeeDataTransferManager.ITeeDataTransferOpSession
        public void finishOperation(Bundle extraBundle) {
            if (this.mIDataTransferOpSession == null) {
                SlogEx.e(TeeDataTransferManager.LOG_TAG, "finishOperation session is null");
                return;
            }
            try {
                this.mIDataTransferOpSession.finishOperation(extraBundle == null ? new Bundle() : extraBundle);
            } catch (RemoteException e) {
                SlogEx.e(TeeDataTransferManager.LOG_TAG, "finishOperation aidl remote exception");
            }
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(@NonNull Message msg) {
            int i = msg.what;
            if (i == 1) {
                if (msg.arg1 == 1) {
                    SlogEx.i(TeeDataTransferManager.LOG_TAG, "session create success.");
                    this.mITeeDataTransferCallback.onSessionCreate(this);
                } else {
                    SlogEx.i(TeeDataTransferManager.LOG_TAG, "session create fail.");
                    this.mITeeDataTransferCallback.onSessionCreate(null);
                }
                return true;
            } else if (i != 2) {
                return false;
            } else {
                if (!(msg.obj instanceof DataTransferInfo)) {
                    SlogEx.e(TeeDataTransferManager.LOG_TAG, "msg is invalid.");
                    return false;
                }
                Bundle messageBundle = msg.getData();
                if (messageBundle == null) {
                    return false;
                }
                this.mITeeDataTransferCallback.onResult(messageBundle.getInt(TYPE), (DataTransferInfo) msg.obj, messageBundle.getInt(RESULT_CODE), messageBundle.getBundle(RESULT_BUNDLE));
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isValidParam(DataTransferInfo transInfo, Bundle extraBundle) {
        if (transInfo != null && !TextUtils.equals(transInfo.getTargetAlias(), BuildConfig.FLAVOR) && !TextUtils.equals(transInfo.getPackageName(), BuildConfig.FLAVOR)) {
            return true;
        }
        SlogEx.e(LOG_TAG, "invalid param");
        return false;
    }

    public void disconnectService() {
        boolean isServiceUnBind;
        synchronized (LOCK) {
            isServiceUnBind = this.mService != null;
        }
        if (isServiceUnBind) {
            SlogEx.i(LOG_TAG, "unbind data transfer service");
            try {
                synchronized (LOCK) {
                    this.mService.finishSession(this.mPackageName, null);
                }
                this.mContext.unbindService(this.mServiceConnection);
            } catch (IllegalArgumentException e) {
                SlogEx.e(LOG_TAG, "unbind service IllegalArgumentException");
            } catch (Exception e2) {
                SlogEx.e(LOG_TAG, "unbind service Exception");
            }
            synchronized (LOCK) {
                this.mIsServiceConnected = false;
                this.mService = null;
            }
        }
        this.mConnectionCallbacks.clear();
    }
}
