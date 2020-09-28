package com.huawei.coauth.auth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import com.huawei.android.provider.SettingsEx;
import com.huawei.coauth.remotepin.util.AlgorithmHelper;
import com.huawei.coauth.remotepin.util.HwLog;
import com.huawei.remotepassword.auth.IRemotePassword;
import com.huawei.remotepassword.auth.IRemotePasswordInputCallback;
import com.huawei.remotepassword.auth.IRemotePasswordInputer;
import java.io.UnsupportedEncodingException;
import java.lang.Thread;
import java.util.HashSet;
import java.util.Iterator;

public class RemotePasswordManager {
    private static final int BUSINESS_TYPE_PIN = 0;
    public static final int CREDENTIAL_TYPE_COMPLEX = 5;
    public static final int CREDENTIAL_TYPE_NONE = -1;
    public static final int CREDENTIAL_TYPE_PATTERN = 1;
    public static final int CREDENTIAL_TYPE_PIN = 2;
    public static final int CREDENTIAL_TYPE_PIN_FIX_FOUR = 3;
    public static final int CREDENTIAL_TYPE_PIN_FIX_SIX = 4;
    private static final int DEFAULT_SALT_SIZE = 16;
    private static final int DEFAULT_SET_SIZE = 5;
    private static final String DEFAULT_USER_NAME = "0";
    private static final Object LOCK = new Object();
    private static final int MSG_CONNECTED = 1;
    private static final int MSG_CONNECTING = 0;
    private static final int MSG_DISCONNECTED = 3;
    private static final int MSG_DISCONNECTING = 2;
    private static final String REMOTE_PIN_PACKAGE_NAME = "com.huawei.remotepassword";
    private static final String REMOTE_PIN_SERVICE_NAME = "com.huawei.remotepassword.RemotePasswordService";
    public static final int RESULT_FAIL = -1;
    public static final int RESULT_ROOT = -2;
    public static final int RESULT_SUCCESS = 0;
    private static final int SETTINGS_DISABLED = 0;
    private static final int SETTINGS_ENABLED = 1;
    private static final String SETTINGS_SECURE_REMOTE_PIN_ENABLED = "remote_password_enabled";
    private static final String TAG = RemotePasswordManager.class.getSimpleName();
    private static final int USER_SYSTEM = 0;
    private final HashSet<IConnectionCallback> mConnectionCallbacks;
    private Context mContext;
    private HandlerThread mHandlerThread;
    private boolean mIsBindingService;
    private boolean mIsRemotePinEnabled;
    private volatile boolean mIsRemotePinInit;
    private RemotePinService mRemoteService;
    private ServiceConnection mServiceConnection;
    private ServiceHandler mServiceHandler;
    private byte[] mUserName;

    public interface ICoAuthCallback {
        void onResult(int i);
    }

    /* access modifiers changed from: private */
    public interface IConnectionCallback {
        void onConnected();

        void onDisconnected(int i);
    }

    public interface IInputCallback {
        void sendResult(byte[] bArr);
    }

    public interface IPasswordInputer {
        void requestInput(int i, Bundle bundle, IInputCallback iInputCallback);
    }

    public interface IVerifyCallback {
        byte[] onVerifyCredential(byte[] bArr, int i, long j, int i2);
    }

    private static class StaticSingletonHolder {
        private static final RemotePasswordManager sInstance = new RemotePasswordManager();

        private StaticSingletonHolder() {
        }
    }

    /* access modifiers changed from: private */
    public static class RemotePinService {
        IRemotePassword mService;

        private RemotePinService() {
        }

        /* access modifiers changed from: package-private */
        public void setService(IRemotePassword service) {
            this.mService = service;
        }

        /* access modifiers changed from: package-private */
        public IRemotePassword getService() {
            return this.mService;
        }

        /* access modifiers changed from: package-private */
        public long preEdit(int businessType) {
            HwLog.i(RemotePasswordManager.TAG, "preEdit");
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, preEdit failed!");
                return 0;
            }
            try {
                return iRemotePassword.preEdit(businessType);
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in preEdit");
                return 0;
            } catch (Exception e2) {
                HwLog.e(RemotePasswordManager.TAG, "Exception in preEdit");
                return 0;
            }
        }

        /* access modifiers changed from: package-private */
        public void postEdit(int businessType) {
            HwLog.i(RemotePasswordManager.TAG, "postEdit");
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, postEdit failed!");
                return;
            }
            try {
                iRemotePassword.postEdit(businessType);
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in postEdit");
            } catch (Exception e2) {
                HwLog.e(RemotePasswordManager.TAG, "Exception in postEdit");
            }
        }

        /* access modifiers changed from: package-private */
        public void registerPassword(int businessType, byte[] userName, int passwordType, byte[] password, byte[] authToken) {
            if (this.mService == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, registerPassword failed!");
                return;
            }
            HwLog.i(RemotePasswordManager.TAG, "Begin registerPassword");
            long startedTime = SystemClock.elapsedRealtime();
            try {
                byte[] salt = AlgorithmHelper.generateRandomBytes(16);
                byte[] secretData = AlgorithmHelper.pbkdf2(password, salt);
                Bundle params = new Bundle();
                params.putInt("passwordType", passwordType);
                params.putByteArray("salt", salt);
                params.putByteArray("secretData", secretData);
                this.mService.registerPassword(businessType, userName, authToken, params);
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in registerPassword");
            } catch (Exception e2) {
                HwLog.e(RemotePasswordManager.TAG, "Exception in registerPassword");
            }
            String str = RemotePasswordManager.TAG;
            HwLog.i(str, "End registerPassword, cost: " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
        }

        /* access modifiers changed from: package-private */
        public void deregisterPassword(int businessType, byte[] userName, byte[] authToken) {
            HwLog.i(RemotePasswordManager.TAG, "deregisterPassword");
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, deregisterPassword failed!");
                return;
            }
            try {
                iRemotePassword.deregisterPassword(businessType, userName, authToken);
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in deregisterPassword");
            } catch (Exception e2) {
                HwLog.e(RemotePasswordManager.TAG, "Exception in deregisterPassword");
            }
        }

        /* access modifiers changed from: package-private */
        public int initCoAuth() {
            HwLog.i(RemotePasswordManager.TAG, "initCoAuth");
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, initCoAuth failed!");
                return -1;
            }
            try {
                return iRemotePassword.initCoAuth();
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in initCoAuth");
                return -1;
            } catch (Exception e2) {
                HwLog.e(RemotePasswordManager.TAG, "Exception in initCoAuth");
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public int deinitCoAuth() {
            HwLog.i(RemotePasswordManager.TAG, "deinitCoAuth");
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, deinitCoAuth failed!");
                return -1;
            }
            try {
                return iRemotePassword.deinitCoAuth();
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in deinitCoAuth");
                return -1;
            } catch (Exception e2) {
                HwLog.e(RemotePasswordManager.TAG, "Exception in deinitCoAuth");
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public void registerPasswordInputer(IRemotePasswordInputer inputer) {
            HwLog.i(RemotePasswordManager.TAG, "registerPasswordInputer");
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, registerPasswordInputer failed!");
                return;
            }
            try {
                iRemotePassword.registerPasswordInputer(inputer);
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in registerPasswordInputer");
            } catch (Exception e2) {
                HwLog.e(RemotePasswordManager.TAG, "Exception in registerPasswordInputer");
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what != 0) {
                if (what != 1) {
                    if (what == 2) {
                        RemotePasswordManager.this.handleServiceDisconnecting();
                    } else if (what == 3) {
                        RemotePasswordManager.this.handleServiceDisconnected();
                    }
                } else if (msg.obj == null || !(msg.obj instanceof IBinder)) {
                    RemotePasswordManager.this.handleServiceConnectedError();
                } else {
                    RemotePasswordManager.this.handleServiceConnected((IBinder) msg.obj);
                }
            } else if (msg.obj != null && (msg.obj instanceof IConnectionCallback)) {
                RemotePasswordManager.this.handleServiceConnecting((IConnectionCallback) msg.obj);
            }
        }
    }

    private RemotePasswordManager() {
        this.mUserName = new byte[0];
        this.mRemoteService = new RemotePinService();
        this.mConnectionCallbacks = new HashSet<>(5);
        this.mServiceConnection = new ServiceConnection() {
            /* class com.huawei.coauth.auth.RemotePasswordManager.AnonymousClass1 */

            public void onServiceConnected(ComponentName name, IBinder service) {
                HwLog.i(RemotePasswordManager.TAG, "onServiceConnected");
                RemotePasswordManager.this.sendServiceConnectedMessage(service);
            }

            public void onServiceDisconnected(ComponentName name) {
                HwLog.i(RemotePasswordManager.TAG, "onServiceDisconnected");
                RemotePasswordManager.this.sendServiceDisconnectedMessage();
            }
        };
        try {
            this.mUserName = DEFAULT_USER_NAME.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            HwLog.e(TAG, "Fail to convert user name.");
        }
    }

    private void startConnectService(IConnectionCallback callback) {
        synchronized (LOCK) {
            this.mConnectionCallbacks.add(callback);
            createWorkThread();
        }
        sendServiceConnectingMessage(callback);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startDisconnectService() {
        sendServiceDisconnectingMessage();
    }

    private void sendServiceConnectingMessage(IConnectionCallback callback) {
        ServiceHandler serviceHandler = this.mServiceHandler;
        if (serviceHandler != null) {
            Message msg = serviceHandler.obtainMessage();
            msg.what = 0;
            msg.obj = callback;
            this.mServiceHandler.sendMessage(msg);
            return;
        }
        HwLog.e(TAG, "No work thread find, MSG_CONNECTING send failed!");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendServiceConnectedMessage(IBinder service) {
        ServiceHandler serviceHandler = this.mServiceHandler;
        if (serviceHandler != null) {
            Message msg = serviceHandler.obtainMessage();
            msg.what = 1;
            msg.obj = service;
            this.mServiceHandler.sendMessage(msg);
            return;
        }
        HwLog.e(TAG, "No work thread find, MSG_CONNECTED send failed!");
    }

    private void sendServiceDisconnectingMessage() {
        ServiceHandler serviceHandler = this.mServiceHandler;
        if (serviceHandler != null) {
            Message msg = serviceHandler.obtainMessage();
            msg.what = 2;
            this.mServiceHandler.sendMessage(msg);
            return;
        }
        HwLog.e(TAG, "No work thread find, MSG_DISCONNECTING send failed!");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendServiceDisconnectedMessage() {
        ServiceHandler serviceHandler = this.mServiceHandler;
        if (serviceHandler != null) {
            Message msg = serviceHandler.obtainMessage();
            msg.what = 3;
            this.mServiceHandler.sendMessage(msg);
            return;
        }
        HwLog.e(TAG, "No work thread find, MSG_DISCONNECTED send failed!");
    }

    private void createWorkThread() {
        if (this.mHandlerThread == null) {
            HwLog.d(TAG, "createWorkThread");
            this.mHandlerThread = new HandlerThread(TAG, 10);
            this.mHandlerThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                /* class com.huawei.coauth.auth.RemotePasswordManager.AnonymousClass2 */

                public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                    HwLog.e(RemotePasswordManager.TAG, "Uncaught exception found");
                }
            });
            this.mHandlerThread.start();
            Looper looper = this.mHandlerThread.getLooper();
            if (looper != null) {
                this.mServiceHandler = new ServiceHandler(looper);
            }
        }
    }

    private void destroyWorkThread() {
        if (this.mHandlerThread != null) {
            HwLog.d(TAG, "destroyWorkThread");
            this.mHandlerThread.quitSafely();
            this.mHandlerThread = null;
            this.mServiceHandler = null;
        }
    }

    public static RemotePasswordManager getInstance(@NonNull Context context) {
        if (context == null) {
            return null;
        }
        RemotePasswordManager instance = StaticSingletonHolder.sInstance;
        instance.setContext(context);
        return instance;
    }

    private void setContext(@NonNull Context context) {
        this.mContext = context;
    }

    public void initCoAuth(@NonNull final ICoAuthCallback callback) {
        HwLog.i(TAG, "initCoAuth");
        if (callback != null) {
            startConnectService(new IConnectionCallback() {
                /* class com.huawei.coauth.auth.RemotePasswordManager.AnonymousClass3 */

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onConnected() {
                    callback.onResult(RemotePasswordManager.this.mRemoteService.initCoAuth());
                }

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onDisconnected(int result) {
                    callback.onResult(-1);
                }
            });
            return;
        }
        throw new IllegalArgumentException("Callback cannot be null!");
    }

    public int deinitCoAuth() {
        int result = this.mRemoteService.deinitCoAuth();
        startDisconnectService();
        return result;
    }

    public void registerPasswordInputer(IPasswordInputer inputer) {
        HwLog.i(TAG, "registerPasswordInputer");
        if (inputer != null) {
            this.mRemoteService.registerPasswordInputer(new RemotePasswordInputer(inputer));
            return;
        }
        throw new IllegalArgumentException("Inputer cannot be null!");
    }

    private class RemotePasswordInputer extends IRemotePasswordInputer.Stub {
        private IPasswordInputer mCallback;

        RemotePasswordInputer(IPasswordInputer callback) {
            this.mCallback = callback;
        }

        @Override // com.huawei.remotepassword.auth.IRemotePasswordInputer
        public void requestInput(int passwordType, final byte[] salt, Bundle params, final IRemotePasswordInputCallback callback) throws RemoteException {
            this.mCallback.requestInput(passwordType, params, new IInputCallback() {
                /* class com.huawei.coauth.auth.RemotePasswordManager.RemotePasswordInputer.AnonymousClass1 */

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IInputCallback
                public void sendResult(byte[] credential) {
                    byte[] secretData = AlgorithmHelper.pbkdf2(credential, salt);
                    RemotePasswordManager.this.verifyRemoteCredential(secretData, callback);
                    RemotePasswordManager.this.clearCredential(secretData);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void verifyRemoteCredential(byte[] secretData, IRemotePasswordInputCallback callback) {
        if (callback != null) {
            try {
                callback.sendResult(secretData);
            } catch (RemoteException e) {
                HwLog.e(TAG, "RemoteException in verifyRemoteCredential");
            }
        }
    }

    public void prepare() {
        this.mIsRemotePinEnabled = isRemotePinEnabled(0);
    }

    public void updateRemotePassword(byte[] credential, final int type, final int userId, boolean isForceUpdate, final IVerifyCallback callback) {
        if (userId == 0) {
            CoAuthCount.getInstance(this.mContext).resetCount(CoAuthType.REMOTEPIN);
        }
        if (callback == null) {
            HwLog.e(TAG, "Invalid verify callback.");
        } else if (shouldUpdateRemotePin(isForceUpdate, userId)) {
            final byte[] newCredential = credential != null ? (byte[]) credential.clone() : null;
            startConnectService(new IConnectionCallback() {
                /* class com.huawei.coauth.auth.RemotePasswordManager.AnonymousClass4 */

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onConnected() {
                    if (newCredential != null) {
                        byte[] authToken = callback.onVerifyCredential(newCredential, type, RemotePasswordManager.this.mRemoteService.preEdit(0), userId);
                        RemotePasswordManager.this.mRemoteService.registerPassword(0, RemotePasswordManager.this.mUserName, RemotePasswordManager.this.getPasswordType(type), newCredential, authToken);
                        RemotePasswordManager.this.clearCredential(newCredential);
                    } else {
                        RemotePasswordManager.this.mRemoteService.deregisterPassword(0, RemotePasswordManager.this.mUserName, new byte[0]);
                    }
                    RemotePasswordManager.this.mRemoteService.postEdit(0);
                    RemotePasswordManager.this.startDisconnectService();
                }

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onDisconnected(int result) {
                    RemotePasswordManager.this.clearCredential(newCredential);
                    RemotePasswordManager.this.startDisconnectService();
                }
            });
        }
    }

    private boolean shouldUpdateRemotePin(boolean isForceUpdate, int userId) {
        if (userId != 0) {
            String str = TAG;
            HwLog.i(str, "Invalid user id: " + userId);
            return false;
        } else if (isForceUpdate) {
            this.mIsRemotePinInit = true;
            return true;
        } else if (this.mIsRemotePinInit) {
            return false;
        } else {
            this.mIsRemotePinInit = true;
            return !this.mIsRemotePinEnabled;
        }
    }

    private boolean isRemotePinEnabled(int userId) {
        return SettingsEx.Secure.getIntForUser(this.mContext.getContentResolver(), SETTINGS_SECURE_REMOTE_PIN_ENABLED, 0, userId) == 1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getPasswordType(int type) {
        return type;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearCredential(byte[] credential) {
        if (credential != null) {
            int length = credential.length;
            for (int i = 0; i < length; i++) {
                credential[i] = 0;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleServiceConnected(IBinder service) {
        HashSet<IConnectionCallback> callbacks;
        synchronized (LOCK) {
            String str = TAG;
            HwLog.d(str, "Service connected. Execute tasks, size = " + this.mConnectionCallbacks.size());
            this.mIsBindingService = false;
            this.mRemoteService.setService(IRemotePassword.Stub.asInterface(service));
            callbacks = (HashSet) this.mConnectionCallbacks.clone();
            this.mConnectionCallbacks.clear();
        }
        Iterator<IConnectionCallback> it = callbacks.iterator();
        while (it.hasNext()) {
            it.next().onConnected();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleServiceConnectedError() {
        HashSet<IConnectionCallback> callbacks;
        synchronized (LOCK) {
            callbacks = (HashSet) this.mConnectionCallbacks.clone();
            this.mConnectionCallbacks.clear();
        }
        Iterator<IConnectionCallback> it = callbacks.iterator();
        while (it.hasNext()) {
            it.next().onDisconnected(-1);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleServiceConnecting(IConnectionCallback connection) {
        if (!isRemotePinPackageExist()) {
            HwLog.e(TAG, "RemotePin service is not exists");
            synchronized (LOCK) {
                this.mConnectionCallbacks.remove(connection);
            }
            connection.onDisconnected(-1);
        } else if (this.mRemoteService.getService() != null) {
            synchronized (LOCK) {
                this.mConnectionCallbacks.remove(connection);
            }
            HwLog.d(TAG, "Service already connected, execute task.");
            connection.onConnected();
        } else {
            synchronized (LOCK) {
                if (!this.mIsBindingService) {
                    HwLog.d(TAG, "Start bindService.");
                    this.mContext.bindService(new Intent().setComponent(new ComponentName(REMOTE_PIN_PACKAGE_NAME, REMOTE_PIN_SERVICE_NAME)), this.mServiceConnection, 1);
                    this.mIsBindingService = true;
                }
            }
        }
    }

    private boolean isRemotePinPackageExist() {
        try {
            ApplicationInfo info = this.mContext.getPackageManager().getApplicationInfo(REMOTE_PIN_PACKAGE_NAME, 0);
            if (info == null || !isSystemApp(info) || !isSignedWithPlatformKey(REMOTE_PIN_PACKAGE_NAME)) {
                return false;
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isSystemApp(ApplicationInfo info) {
        return (info.flags & 1) != 0;
    }

    private boolean isSignedWithPlatformKey(String packageName) {
        return this.mContext.getPackageManager().checkSignatures(packageName, "android") == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleServiceDisconnecting() {
        synchronized (LOCK) {
            if (!this.mConnectionCallbacks.isEmpty()) {
                HwLog.i(TAG, "Task is not empty, do not disconnect service.");
                return;
            }
            if (this.mRemoteService.getService() != null) {
                HwLog.d(TAG, "Start unbindService.");
                this.mContext.unbindService(this.mServiceConnection);
                this.mRemoteService.setService(null);
                this.mIsBindingService = false;
            }
            destroyWorkThread();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleServiceDisconnected() {
        synchronized (LOCK) {
            this.mRemoteService.setService(null);
            this.mIsBindingService = false;
        }
    }
}
