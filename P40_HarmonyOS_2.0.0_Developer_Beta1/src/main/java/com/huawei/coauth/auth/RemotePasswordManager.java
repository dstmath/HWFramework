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
import com.huawei.coauth.auth.PasswordInfo;
import com.huawei.coauth.remotepin.util.AlgorithmHelper;
import com.huawei.coauth.remotepin.util.HwLog;
import com.huawei.coauthservice.identitymgr.constants.ServicePackage;
import com.huawei.remotepassword.auth.IRemotePassword;
import com.huawei.remotepassword.auth.IRemotePasswordInputCallback;
import com.huawei.remotepassword.auth.IRemotePasswordInputer;
import java.io.UnsupportedEncodingException;
import java.lang.Thread;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

public class RemotePasswordManager {
    private static final int AUTH_TOKEN_BYTE_ARRAY_LENGTH = 69;
    private static final int BUSINESS_TYPE_APP = 1;
    private static final int BUSINESS_TYPE_PIN = 0;
    private static final int CLIENT_PUBLIC_KEY_BYTE_ARRAY_LENGTH = 384;
    public static final int CREDENTIAL_TYPE_COMPLEX = 5;
    public static final int CREDENTIAL_TYPE_NONE = -1;
    public static final int CREDENTIAL_TYPE_PATTERN = 1;
    public static final int CREDENTIAL_TYPE_PIN = 2;
    public static final int CREDENTIAL_TYPE_PIN_FIX_FOUR = 3;
    public static final int CREDENTIAL_TYPE_PIN_FIX_SIX = 4;
    private static final int DEFAULT_SALT_SIZE = 16;
    private static final int DEFAULT_SET_SIZE = 5;
    private static final String DEFAULT_USER_NAME = "0";
    private static final String HW_PIN_CLIENT_ACTION = "huawei.security.updatepassword";
    private static final String KEY_BUSINESS_TYPE = "businessType";
    private static final String KEY_CHALLENGE_C = "challengeC";
    private static final String KEY_CHALLENGE_S = "challengeS";
    private static final String KEY_KCF_DATA_C = "kcfDataC";
    private static final String KEY_KCF_DATA_S = "kcfDataS";
    private static final int KEY_LENGTH_16 = 16;
    private static final int KEY_LENGTH_32 = 32;
    private static final String KEY_OPERATION_TYPE = "operationType";
    private static final String KEY_PIN_TYPE_LONG = "passwordType";
    private static final String KEY_PUB_KEY_C = "pubKeyC";
    private static final String KEY_PUB_KEY_S = "pubKeyS";
    private static final String KEY_SALT = "salt";
    private static final String KEY_SECRET_DATA = "secretData";
    private static final String KEY_VERIFY_PIN = "verifyPin";
    private static final Object LOCK = new Object();
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MSG_CONNECTED = 1;
    private static final int MSG_CONNECTING = 0;
    private static final int MSG_DISCONNECTED = 3;
    private static final int MSG_DISCONNECTING = 2;
    private static final int OPT_DELETE = 3;
    private static final int OPT_MODIFY = 2;
    private static final int OPT_REG = 1;
    private static final String PROPERTY_RESET_ERROR_COUNT = "RESET_ERROR_COUNT";
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
    private static byte[] localKcfDataS = new byte[0];
    private final HashSet<IConnectionCallback> mConnectionCallbacks;
    private Context mContext;
    private HandlerThread mHandlerThread;
    private InputerInfo mInputerInfo;
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

    public interface IPasswordProcessCallback {
        void onResult(int i);
    }

    public interface IVerifyCallback {
        byte[] onVerifyCredential(byte[] bArr, int i, long j, int i2);
    }

    /* access modifiers changed from: private */
    public static class InputerInfo {
        private String inputerPkgName;
        private IPasswordInputer mInputer;

        private InputerInfo() {
            this.mInputer = null;
            this.inputerPkgName = null;
        }
    }

    private static class StaticSingletonHolder {
        private static final RemotePasswordManager SINSTANCE = new RemotePasswordManager();

        private StaticSingletonHolder() {
        }
    }

    /* access modifiers changed from: private */
    public static class RemotePinService {
        private IRemotePassword mService;

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
            }
        }

        /* access modifiers changed from: package-private */
        public int registerPassword(int businessType, byte[] userName, PasswordInfo passwordInfo, byte[] authToken, int operationType) {
            if (this.mService == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, registerPassword failed!");
                return -1;
            }
            int result = 0;
            HwLog.i(RemotePasswordManager.TAG, "Begin registerPassword");
            long startedTime = SystemClock.elapsedRealtime();
            try {
                byte[] salt = AlgorithmHelper.generateRandomBytes(16);
                byte[] secretData = AlgorithmHelper.pbkdf2(passwordInfo.getPassword(), salt);
                Bundle params = new Bundle();
                params.putInt(RemotePasswordManager.KEY_PIN_TYPE_LONG, passwordInfo.getPassType());
                params.putByteArray(RemotePasswordManager.KEY_SALT, salt);
                params.putByteArray(RemotePasswordManager.KEY_SECRET_DATA, secretData);
                params.putInt(RemotePasswordManager.KEY_OPERATION_TYPE, operationType);
                result = this.mService.registerPassword(businessType, userName, authToken, params);
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in registerPassword");
            }
            String str = RemotePasswordManager.TAG;
            HwLog.i(str, "End registerPassword, cost: " + (SystemClock.elapsedRealtime() - startedTime) + " ms");
            return result;
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
            }
        }

        private byte[] calcClientPubKey(byte[] preSharedSecret, byte[] clientRand) {
            if (preSharedSecret == null || clientRand == null) {
                HwLog.e(RemotePasswordManager.TAG, "preSharedSecret or clientRand is null, calcClientPubKey fail");
                return new byte[0];
            }
            byte[] originalPubKey = AlgorithmHelper.powerModP(AlgorithmHelper.squareModP(preSharedSecret), clientRand);
            if (originalPubKey.length <= RemotePasswordManager.CLIENT_PUBLIC_KEY_BYTE_ARRAY_LENGTH) {
                return Arrays.copyOfRange(originalPubKey, 0, originalPubKey.length);
            }
            HwLog.i(RemotePasswordManager.TAG, "less Pub Key size");
            return Arrays.copyOfRange(originalPubKey, originalPubKey.length - RemotePasswordManager.CLIENT_PUBLIC_KEY_BYTE_ARRAY_LENGTH, originalPubKey.length);
        }

        private byte[] calcClientSessionKey(byte[] serverPublicKey, byte[] clientRand) {
            if (serverPublicKey == null || clientRand == null) {
                HwLog.e(RemotePasswordManager.TAG, "serverPublicKey or clientRand is null, calcClientSessionKey fail");
                return new byte[0];
            }
            byte[] originalSessionKey = AlgorithmHelper.powerModP(serverPublicKey, clientRand);
            if (originalSessionKey.length <= RemotePasswordManager.CLIENT_PUBLIC_KEY_BYTE_ARRAY_LENGTH) {
                return Arrays.copyOfRange(originalSessionKey, 0, originalSessionKey.length);
            }
            String str = RemotePasswordManager.TAG;
            HwLog.e(str, "less Session Key size " + originalSessionKey.length);
            return Arrays.copyOfRange(originalSessionKey, originalSessionKey.length - RemotePasswordManager.CLIENT_PUBLIC_KEY_BYTE_ARRAY_LENGTH, originalSessionKey.length);
        }

        private void clearLocalKcfDataS() {
            if (RemotePasswordManager.localKcfDataS != null) {
                int length = RemotePasswordManager.localKcfDataS.length;
                for (int i = 0; i < length; i++) {
                    RemotePasswordManager.localKcfDataS[i] = 0;
                }
            }
            byte[] unused = RemotePasswordManager.localKcfDataS = new byte[0];
        }

        private void setLocalKcfDataS(byte[] KcfDataS) {
            clearLocalKcfDataS();
            byte[] unused = RemotePasswordManager.localKcfDataS = KcfDataS;
        }

        private Bundle generateResponse(byte[] serverChallenge, byte[] clientPublicKey, byte[] hmacKey) {
            byte[] clientChallenge = AlgorithmHelper.generateRandomBytes(16);
            setLocalKcfDataS(AlgorithmHelper.getHmac(AlgorithmHelper.mergeBytes(serverChallenge, clientChallenge), hmacKey));
            Bundle responseDict = new Bundle();
            responseDict.putByteArray(RemotePasswordManager.KEY_PUB_KEY_C, clientPublicKey);
            responseDict.putByteArray(RemotePasswordManager.KEY_CHALLENGE_C, clientChallenge);
            responseDict.putByteArray(RemotePasswordManager.KEY_KCF_DATA_C, AlgorithmHelper.getHmac(AlgorithmHelper.mergeBytes(clientChallenge, serverChallenge), hmacKey));
            return responseDict;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int updateOrDeRegisterPassword(byte[] oldCredential, byte[] newCredential, byte[] userName, int businessType) {
            HwLog.i(RemotePasswordManager.TAG, "updateOrDeRegisterPassword");
            if (this.mService == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, updateOrDeRegisterPassword failed!");
                return -1;
            }
            try {
                Bundle firstDict = new Bundle();
                try {
                    if (this.mService.getRemoteServerKey(businessType, userName, firstDict) != 0) {
                        HwLog.e(RemotePasswordManager.TAG, "getRemoteServerKey fail");
                        return -1;
                    }
                    try {
                        Optional<Bundle> secondDict = calRemoteClientPublic(firstDict, oldCredential);
                        if (!secondDict.isPresent()) {
                            HwLog.e(RemotePasswordManager.TAG, "calRemoteClientPublic fail");
                            return -1;
                        }
                        Bundle params = new Bundle();
                        params.putInt(RemotePasswordManager.KEY_OPERATION_TYPE, 3);
                        if (newCredential != null) {
                            byte[] salt = AlgorithmHelper.generateRandomBytes(16);
                            byte[] secretData = AlgorithmHelper.pbkdf2(newCredential, salt);
                            params.putInt(RemotePasswordManager.KEY_OPERATION_TYPE, 2);
                            params.putByteArray(RemotePasswordManager.KEY_SALT, salt);
                            params.putByteArray(RemotePasswordManager.KEY_SECRET_DATA, secretData);
                            if (!firstDict.containsKey(RemotePasswordManager.KEY_PIN_TYPE_LONG)) {
                                HwLog.e(RemotePasswordManager.TAG, "firstDict data is null, updateOrDeRegisterPassword fail");
                                return -1;
                            }
                            params.putInt(RemotePasswordManager.KEY_PIN_TYPE_LONG, firstDict.getInt(RemotePasswordManager.KEY_PIN_TYPE_LONG));
                        }
                        Bundle thirdDict = new Bundle();
                        if (this.mService.verifyRemotePassword(businessType, userName, secondDict.get(), params, thirdDict) != 0) {
                            HwLog.e(RemotePasswordManager.TAG, "verifyRemotePassword fail");
                            return -1;
                        }
                        int result = verifyDcfDataS(thirdDict);
                        String str = RemotePasswordManager.TAG;
                        HwLog.i(str, "updateOrDeRegisterPassword result is " + result);
                        clearLocalKcfDataS();
                        return 0;
                    } catch (RemoteException e) {
                        HwLog.e(RemotePasswordManager.TAG, "RemoteException in updateOrDeRegisterPassword");
                        return 0;
                    }
                } catch (RemoteException e2) {
                    HwLog.e(RemotePasswordManager.TAG, "RemoteException in updateOrDeRegisterPassword");
                    return 0;
                }
            } catch (RemoteException e3) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in updateOrDeRegisterPassword");
                return 0;
            }
        }

        private Optional<Bundle> calRemoteClientPublic(Bundle inDict, byte[] credential) {
            if (inDict == null) {
                HwLog.e(RemotePasswordManager.TAG, "InDict is null, calRemoteClientPublic failed!");
                return Optional.of(new Bundle());
            }
            try {
                byte[] serverPublicKey = inDict.getByteArray(RemotePasswordManager.KEY_PUB_KEY_S);
                byte[] serverChallenge = inDict.getByteArray(RemotePasswordManager.KEY_CHALLENGE_S);
                byte[] preSharedSecret = AlgorithmHelper.pbkdf2(credential, inDict.getByteArray(RemotePasswordManager.KEY_SALT));
                byte[] clientRand = AlgorithmHelper.generateRandomBytes(32);
                return Optional.of(generateResponse(serverChallenge, calcClientPubKey(preSharedSecret, clientRand), AlgorithmHelper.getSha256(calcClientSessionKey(serverPublicKey, clientRand))));
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(RemotePasswordManager.TAG, "credential getByteArray error, calRemoteClientPublic fail");
                return Optional.of(new Bundle());
            }
        }

        private int verifyDcfDataS(Bundle inDict) {
            if (inDict == null) {
                HwLog.e(RemotePasswordManager.TAG, "InDict is null, verifyDcfDataS failed!");
                return -1;
            }
            try {
                int ret = Arrays.equals(inDict.getByteArray(RemotePasswordManager.KEY_KCF_DATA_S), RemotePasswordManager.localKcfDataS) ? 0 : -1;
                String str = RemotePasswordManager.TAG;
                HwLog.i(str, "verifyDcfDataS " + ret);
                return ret;
            } catch (ArrayIndexOutOfBoundsException e) {
                HwLog.e(RemotePasswordManager.TAG, "ArrayIndexOutOfBoundsException in verifyDcfDataS");
                return -1;
            }
        }

        /* access modifiers changed from: package-private */
        public int isPasswordRegistered(int businessType, byte[] userName) {
            HwLog.i(RemotePasswordManager.TAG, "isPasswordRegistered");
            int result = -1;
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, initCoAuth failed!");
                return -1;
            }
            try {
                result = iRemotePassword.isPasswordRegistered(businessType, userName);
                String str = RemotePasswordManager.TAG;
                HwLog.i(str, "isPasswordRegistered :" + result);
                return result;
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in isPasswordRegistered");
                return result;
            }
        }

        /* access modifiers changed from: package-private */
        public int initCoAuth() {
            HwLog.i(RemotePasswordManager.TAG, "initCoAuth");
            int result = -1;
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, initCoAuth failed!");
                return -1;
            }
            try {
                result = iRemotePassword.initCoAuth();
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in initCoAuth");
            }
            HwLog.i(RemotePasswordManager.TAG, "initCoAuth end");
            return result;
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
            }
        }

        /* access modifiers changed from: package-private */
        public void registerPasswordInputerFor(IRemotePasswordInputer inputer, String pkgName) {
            HwLog.i(RemotePasswordManager.TAG, "registerPasswordInputerFor");
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, registerPasswordInputerFor failed!");
                return;
            }
            try {
                iRemotePassword.registerPasswordInputerFor(inputer, pkgName);
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in registerPasswordInputerFor");
            }
        }

        /* access modifiers changed from: package-private */
        public int onSetProperty(byte[] property, byte[] value) {
            HwLog.i(RemotePasswordManager.TAG, "onSetProperty");
            IRemotePassword iRemotePassword = this.mService;
            if (iRemotePassword == null) {
                HwLog.e(RemotePasswordManager.TAG, "Service is not connected, onSetProperty failed!");
                return -1;
            }
            try {
                int ret = iRemotePassword.onLocalSetProperty(property, value);
                String str = RemotePasswordManager.TAG;
                HwLog.i(str, "onSetProperty result:" + ret);
                return 0;
            } catch (RemoteException e) {
                HwLog.e(RemotePasswordManager.TAG, "RemoteException in onSetProperty");
                return 0;
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
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

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName name, IBinder service) {
                HwLog.i(RemotePasswordManager.TAG, "onServiceConnected");
                RemotePasswordManager.this.sendServiceConnectedMessage(service);
            }

            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName name) {
                HwLog.i(RemotePasswordManager.TAG, "onServiceDisconnected");
                RemotePasswordManager.this.sendServiceDisconnectedMessage();
                if (RemotePasswordManager.this.mInputerInfo != null) {
                    HwLog.i(RemotePasswordManager.TAG, "try to reconnect");
                    RemotePasswordManager.this.initCoAuth(new ICoAuthCallback() {
                        /* class com.huawei.coauth.auth.RemotePasswordManager.AnonymousClass1.AnonymousClass1 */

                        @Override // com.huawei.coauth.auth.RemotePasswordManager.ICoAuthCallback
                        public void onResult(int result) {
                            if (result == -2 || result == 0) {
                                RemotePasswordManager.this.registerPasswordInputerFor(RemotePasswordManager.this.mInputerInfo.mInputer, RemotePasswordManager.this.mInputerInfo.inputerPkgName);
                            } else {
                                HwLog.e(RemotePasswordManager.TAG, "InitCoAuth failed!");
                            }
                        }
                    });
                }
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

                @Override // java.lang.Thread.UncaughtExceptionHandler
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
        RemotePasswordManager instance = StaticSingletonHolder.SINSTANCE;
        instance.setContext(context);
        return instance;
    }

    private void setContext(@NonNull Context context) {
        this.mContext = context.getApplicationContext();
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
        this.mInputerInfo = null;
        int result = this.mRemoteService.deinitCoAuth();
        startDisconnectService();
        return result;
    }

    public void registerPasswordInputerFor(IPasswordInputer inputer, String pkgName) {
        HwLog.i(TAG, "registerPasswordInputerFor");
        if (pkgName == null || pkgName.isEmpty()) {
            HwLog.e(TAG, "pkgName is null, registerPasswordInputerFor fail");
        } else if (inputer == null) {
            HwLog.i(TAG, "inputer is null");
            this.mInputerInfo = null;
            this.mRemoteService.registerPasswordInputerFor(null, pkgName);
        } else {
            this.mInputerInfo = new InputerInfo();
            this.mInputerInfo.inputerPkgName = pkgName;
            this.mInputerInfo.mInputer = inputer;
            this.mRemoteService.registerPasswordInputerFor(new RemotePasswordInputer(inputer), pkgName);
        }
    }

    public void registerPasswordInputer(IPasswordInputer inputer) {
        HwLog.i(TAG, "registerPasswordInputer");
        if (inputer != null) {
            this.mRemoteService.registerPasswordInputer(new RemotePasswordInputer(inputer));
            return;
        }
        throw new IllegalArgumentException("Inputer cannot be null!");
    }

    /* access modifiers changed from: private */
    public class RemotePasswordInputer extends IRemotePasswordInputer.Stub {
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

    /* access modifiers changed from: package-private */
    public void systemResetCount() {
        HwLog.i(TAG, "systemResetCount start.");
        onSetProperty(PROPERTY_RESET_ERROR_COUNT.getBytes(StandardCharsets.UTF_8), DEFAULT_USER_NAME.getBytes(StandardCharsets.UTF_8));
    }

    public void updateRemotePassword(byte[] credential, final int type, final int userId, boolean isForceUpdate, final IVerifyCallback callback) {
        if (userId == 0) {
            systemResetCount();
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
                        long challenge = RemotePasswordManager.this.mRemoteService.preEdit(0);
                        PasswordInfo passwordInfo = new PasswordInfo();
                        passwordInfo.setPassword(newCredential);
                        passwordInfo.setPassType(type);
                        RemotePasswordManager.this.mRemoteService.registerPassword(0, RemotePasswordManager.this.mUserName, passwordInfo, callback.onVerifyCredential(newCredential, type, challenge, userId), 2);
                        RemotePasswordManager.this.clearCredential(newCredential);
                        passwordInfo.clearPassword();
                        RemotePasswordManager.this.mRemoteService.postEdit(0);
                    } else {
                        RemotePasswordManager.this.mRemoteService.deregisterPassword(0, RemotePasswordManager.this.mUserName, new byte[0]);
                    }
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

    private int checkParam(int businessType, int passwordType, byte[] userName) {
        if (businessType != 1) {
            String str = TAG;
            HwLog.w(str, "Invalid Param! businessType: " + businessType);
            return -1;
        }
        PasswordInfo.LockScreenType type = PasswordInfo.LockScreenType.PATTERN;
        if (passwordType > type.ordinal()) {
            String str2 = TAG;
            HwLog.w(str2, "Invalid Param! type: " + type.ordinal());
            return -1;
        } else if (userName == null || userName.length == 0 || userName.length > 20) {
            return -1;
        } else {
            for (int i = 0; i < userName.length; i++) {
                if ((userName[i] < 48 || userName[i] > 57) && ((userName[i] < 97 || userName[i] > 122) && (userName[i] < 65 || userName[i] > 90))) {
                    return -1;
                }
            }
            return 0;
        }
    }

    public void registerRemotePassword(final int businessType, final byte[] userName, final int passwordType, byte[] credential, final IPasswordProcessCallback callback) {
        if (callback == null) {
            HwLog.e(TAG, "Invalid passwordProcess callback.");
        } else if (checkParam(businessType, passwordType, userName) != 0) {
            HwLog.w(TAG, "Invalid Param!");
            callback.onResult(-1);
        } else {
            final PasswordInfo localPasswordInfo = new PasswordInfo();
            localPasswordInfo.setPassword(credential);
            startConnectService(new IConnectionCallback() {
                /* class com.huawei.coauth.auth.RemotePasswordManager.AnonymousClass5 */

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onConnected() {
                    if (localPasswordInfo.getPassword().length != 0) {
                        byte[] authToken = new byte[RemotePasswordManager.AUTH_TOKEN_BYTE_ARRAY_LENGTH];
                        authToken[0] = 0;
                        localPasswordInfo.setPassType(RemotePasswordManager.this.getPasswordType(passwordType));
                        int errorCode = RemotePasswordManager.this.mRemoteService.registerPassword(businessType, userName, localPasswordInfo, authToken, 1);
                        localPasswordInfo.clearPassword();
                        callback.onResult(errorCode);
                    }
                    RemotePasswordManager.this.startDisconnectService();
                }

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onDisconnected(int result) {
                    localPasswordInfo.clearPassword();
                    RemotePasswordManager.this.startDisconnectService();
                }
            });
        }
    }

    private void updateOrDeRegisterRemotePassword(byte[] oldCredential, byte[] newCredential, final byte[] userName, final int businessType, final IPasswordProcessCallback callback) {
        final byte[] oldLocalCredential = null;
        final byte[] newLocalCredential = newCredential != null ? (byte[]) newCredential.clone() : null;
        if (oldCredential != null) {
            oldLocalCredential = (byte[]) oldCredential.clone();
        }
        startConnectService(new IConnectionCallback() {
            /* class com.huawei.coauth.auth.RemotePasswordManager.AnonymousClass6 */

            @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
            public void onConnected() {
                int errorCode = RemotePasswordManager.this.mRemoteService.updateOrDeRegisterPassword(oldLocalCredential, newLocalCredential, userName, businessType);
                IPasswordProcessCallback iPasswordProcessCallback = callback;
                if (iPasswordProcessCallback == null) {
                    HwLog.e(RemotePasswordManager.TAG, "callback is null, updateOrDeRegisterRemotePassword failed!");
                    return;
                }
                iPasswordProcessCallback.onResult(errorCode);
                RemotePasswordManager.this.startDisconnectService();
                RemotePasswordManager.this.clearCredential(newLocalCredential);
                RemotePasswordManager.this.clearCredential(oldLocalCredential);
            }

            @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
            public void onDisconnected(int result) {
                RemotePasswordManager.this.startDisconnectService();
                RemotePasswordManager.this.clearCredential(newLocalCredential);
                RemotePasswordManager.this.clearCredential(oldLocalCredential);
            }
        });
    }

    public void updateRemotePassword(int businessType, byte[] userName, byte[] oldCredential, byte[] newCredential, IPasswordProcessCallback callback) {
        if (checkParam(businessType, PasswordInfo.LockScreenType.NONE.ordinal(), userName) != 0) {
            HwLog.w(TAG, "Invalid Param!");
            if (callback == null) {
                HwLog.e(TAG, "callback is null, updateRemotePassword failed!");
            } else {
                callback.onResult(-1);
            }
        } else {
            updateOrDeRegisterRemotePassword(oldCredential, newCredential, userName, businessType, callback);
        }
    }

    public void deRegisterRemotePassword(int businessType, byte[] userName, byte[] oldCredential, IPasswordProcessCallback callback) {
        if (checkParam(businessType, PasswordInfo.LockScreenType.NONE.ordinal(), userName) != 0) {
            HwLog.w(TAG, "Invalid Param!");
            if (callback == null) {
                HwLog.e(TAG, "callback is null, deRegisterRemotePassword failed!");
            } else {
                callback.onResult(-1);
            }
        } else {
            updateOrDeRegisterRemotePassword(oldCredential, null, userName, businessType, callback);
        }
    }

    public void isPasswordRegistered(final int businessType, final byte[] userName, final IPasswordProcessCallback callback) {
        if (userName == null) {
            HwLog.e(TAG, "Invalid Param, isPasswordRegistered failed!");
        } else if (callback == null) {
            HwLog.e(TAG, "callback is null, isPasswordRegistered failed!");
        } else {
            startConnectService(new IConnectionCallback() {
                /* class com.huawei.coauth.auth.RemotePasswordManager.AnonymousClass7 */

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onConnected() {
                    callback.onResult(RemotePasswordManager.this.mRemoteService.isPasswordRegistered(businessType, userName));
                    RemotePasswordManager.this.startDisconnectService();
                }

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onDisconnected(int result) {
                    RemotePasswordManager.this.startDisconnectService();
                }
            });
        }
    }

    public void onSetProperty(final byte[] property, final byte[] value) {
        if (property == null || value == null) {
            HwLog.e(TAG, "Invalid Param, onSetProperty failed!");
        } else {
            startConnectService(new IConnectionCallback() {
                /* class com.huawei.coauth.auth.RemotePasswordManager.AnonymousClass8 */

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onConnected() {
                    RemotePasswordManager.this.mRemoteService.onSetProperty(property, value);
                    RemotePasswordManager.this.startDisconnectService();
                }

                @Override // com.huawei.coauth.auth.RemotePasswordManager.IConnectionCallback
                public void onDisconnected(int result) {
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
                    HwLog.i(TAG, "Start bindService.");
                    this.mContext.bindService(new Intent(HW_PIN_CLIENT_ACTION).setComponent(new ComponentName(REMOTE_PIN_PACKAGE_NAME, REMOTE_PIN_SERVICE_NAME)), this.mServiceConnection, 1);
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
        return this.mContext.getPackageManager().checkSignatures(packageName, ServicePackage.CHECK_PACKAGE) == 0;
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
