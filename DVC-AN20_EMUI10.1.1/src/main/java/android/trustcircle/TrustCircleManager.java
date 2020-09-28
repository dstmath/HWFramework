package android.trustcircle;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.os.RemoteException;
import android.trustcircle.AuthPara;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import huawei.android.security.IAuthCallback;
import huawei.android.security.IKaCallback;
import huawei.android.security.ILifeCycleCallback;
import huawei.android.security.ITrustCircleManager;

public class TrustCircleManager {
    private static final int AUTH_ID_ERROR = -1;
    private static final int LIFE_CYCLE_ERROR = -1;
    private static final int RESULT_OK = 0;
    private static final String TAG = "TrustCircleManager";
    private static final String TRUSTCIRCLE_MANAGER_SERVICE = "trustcircle_manager_service";
    private static Object sLock = new Object();
    private ILifeCycleCallback mILifeCycleCallback;
    private LoginCallback mLoginCallback;
    private LogoutCallback mLogoutCallback;
    private UnregisterCallback mUnregisterCallback;

    public interface AuthCallback {
        void onAuthAck(long j, AuthPara.OnAuthAckInfo onAuthAckInfo);

        void onAuthAckError(long j, int i);

        void onAuthError(long j, int i);

        void onAuthExited(long j, int i);

        void onAuthSync(long j, AuthPara.OnAuthSyncInfo onAuthSyncInfo);

        void onAuthSyncAck(long j, AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo);

        void onAuthSyncAckError(long j, int i);

        void requestPK();

        void responsePK(long j, AuthPara.RespPkInfo respPkInfo);
    }

    public interface KaCallback {
        void onKaError(long j, int i);

        void onKaResult(long j, int i, byte[] bArr, byte[] bArr2);
    }

    public interface LoginCallback {
        void onFinalLoginResult(int i);

        void onFinalRegisterResult(int i);

        void onLoginResponse(int i, int i2, String str);

        void onRegisterResponse(int i, int i2, short s, String str, String str2, String str3);

        void onUpdateResponse(int i, int i2, String str);
    }

    public interface LogoutCallback {
        void onLogoutResult(int i);
    }

    public interface UnregisterCallback {
        void onUnregisterResult(int i);
    }

    private TrustCircleManager() {
        this.mILifeCycleCallback = new ILifeCycleCallback.Stub() {
            /* class android.trustcircle.TrustCircleManager.AnonymousClass1 */

            public void onRegisterResponse(int errorCode, int globalKeyId, int authKeyAlgoType, String regAuthKeyData, String regAuthKeyDataSign, String clientChallenge) {
                if (TrustCircleManager.this.mLoginCallback != null) {
                    TrustCircleManager.this.mLoginCallback.onRegisterResponse(errorCode, globalKeyId, (short) authKeyAlgoType, regAuthKeyData, regAuthKeyDataSign, clientChallenge);
                    if (errorCode != 0) {
                        TrustCircleManager.this.mLoginCallback = null;
                    }
                }
            }

            public void onFinalRegisterResult(int errorCode) {
                if (TrustCircleManager.this.mLoginCallback != null) {
                    TrustCircleManager.this.mLoginCallback.onFinalRegisterResult(errorCode);
                    if (errorCode != 0) {
                        TrustCircleManager.this.mLoginCallback = null;
                    }
                }
            }

            public void onLoginResponse(int errorCode, int indexVersion, String clientChallenge) {
                if (TrustCircleManager.this.mLoginCallback != null) {
                    TrustCircleManager.this.mLoginCallback.onLoginResponse(errorCode, indexVersion, clientChallenge);
                    if (errorCode != 0) {
                        TrustCircleManager.this.mLoginCallback = null;
                    }
                }
            }

            public void onUpdateResponse(int errorCode, int indexVersion, String clientChallenge) {
                if (TrustCircleManager.this.mLoginCallback != null) {
                    TrustCircleManager.this.mLoginCallback.onUpdateResponse(errorCode, indexVersion, clientChallenge);
                    if (errorCode != 0) {
                        TrustCircleManager.this.mLoginCallback = null;
                    }
                }
            }

            public void onFinalLoginResult(int errorCode) {
                if (TrustCircleManager.this.mLoginCallback != null) {
                    TrustCircleManager.this.mLoginCallback.onFinalLoginResult(errorCode);
                    TrustCircleManager.this.mLoginCallback = null;
                }
            }

            public void onLogoutResult(int errorCode) {
                if (TrustCircleManager.this.mLogoutCallback != null) {
                    TrustCircleManager.this.mLogoutCallback.onLogoutResult(errorCode);
                    TrustCircleManager.this.mLogoutCallback = null;
                }
            }

            public void onUnregisterResult(int errorCode) {
                if (TrustCircleManager.this.mUnregisterCallback != null) {
                    TrustCircleManager.this.mUnregisterCallback.onUnregisterResult(errorCode);
                    TrustCircleManager.this.mUnregisterCallback = null;
                }
            }
        };
    }

    private static class Singleton {
        private static final TrustCircleManager INSTANCE = new TrustCircleManager();

        private Singleton() {
        }
    }

    public static TrustCircleManager getInstance() {
        return Singleton.INSTANCE;
    }

    private ITrustCircleManager getTrustCircleService() {
        synchronized (sLock) {
            IBinder tcisBinder = ServiceManagerEx.getService(TRUSTCIRCLE_MANAGER_SERVICE);
            if (tcisBinder == null) {
                Log.e(TAG, "getService binder null");
                return null;
            }
            ITrustCircleManager tcisService = ITrustCircleManager.Stub.asInterface(tcisBinder);
            if (tcisService != null) {
                return tcisService;
            }
            Log.e(TAG, "getService service null");
            return null;
        }
    }

    public Bundle getTcisInfo() {
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService == null) {
            return null;
        }
        try {
            return tcisService.getTcisInfo();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException when getTcisInfo is invoked");
            return null;
        }
    }

    public long initKeyAgreement(KaCallback callback, int kaVersion, long userId, byte[] aesTmpKey, String kaInfo) {
        if (callback == null || aesTmpKey == null || kaInfo == null) {
            throw new IllegalArgumentException("illegal null params.");
        }
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService != null) {
            IKaCallbackInner mKaCallback = new IKaCallbackInner(callback);
            try {
                return tcisService.initKeyAgreement(mKaCallback, kaVersion, userId, aesTmpKey, kaInfo);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when initKeyAgreement is invoked");
                mKaCallback.onKaError(-1, -1);
            }
        }
        return -1;
    }

    private static class IKaCallbackInner extends IKaCallback.Stub {
        KaCallback mKaCallback;

        public IKaCallbackInner(KaCallback callback) {
            this.mKaCallback = callback;
        }

        public void onKaResult(long authId, int result, byte[] iv, byte[] payload) {
            KaCallback kaCallback = this.mKaCallback;
            if (kaCallback != null) {
                kaCallback.onKaResult(authId, result, iv, payload);
                this.mKaCallback = null;
            }
        }

        public void onKaError(long authId, int errorCode) {
            KaCallback kaCallback = this.mKaCallback;
            if (kaCallback != null) {
                kaCallback.onKaError(authId, errorCode);
                this.mKaCallback = null;
            }
        }
    }

    public void loginServerRequest(LoginCallback callback, CancellationSignal cancel, long userId, int serverRegisterStatus, String sessionId) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an login callback");
        } else if (sessionId == null) {
            Log.e(TAG, "session id should't be null");
            callback.onLoginResponse(-1, -1, null);
        } else {
            if (cancel != null) {
                if (cancel.isCanceled()) {
                    Log.e(TAG, "login already canceled");
                    return;
                }
                cancel.setOnCancelListener(new OnRegOrLoginCancelListener(userId));
            }
            ITrustCircleManager tcisService = getTrustCircleService();
            if (tcisService != null) {
                this.mLoginCallback = callback;
                try {
                    tcisService.loginServerRequest(this.mILifeCycleCallback, userId, serverRegisterStatus, sessionId);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when loginServerRequest is invoked");
                    this.mLoginCallback.onRegisterResponse(-1, -1, -1, null, null, null);
                    this.mLoginCallback = null;
                }
            }
        }
    }

    public void updateServerRequest(LoginCallback callback, CancellationSignal cancel, long userId) {
        if (callback != null) {
            if (cancel != null) {
                if (cancel.isCanceled()) {
                    Log.e(TAG, "updateServerRequest already canceled");
                    return;
                }
                cancel.setOnCancelListener(new OnRegOrLoginCancelListener(userId));
            }
            ITrustCircleManager tcisService = getTrustCircleService();
            if (tcisService != null) {
                this.mLoginCallback = callback;
                try {
                    tcisService.updateServerRequest(this.mILifeCycleCallback, userId);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when loginServerRequest is invoked");
                    this.mLoginCallback.onUpdateResponse(-1, -1, null);
                    this.mLoginCallback = null;
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an login callback");
        }
    }

    private class OnRegOrLoginCancelListener implements CancellationSignal.OnCancelListener {
        private long mUserId;

        public OnRegOrLoginCancelListener(long userId) {
            this.mUserId = userId;
        }

        public void onCancel() {
            Log.d(TrustCircleManager.TAG, "cancelRegOrLogin");
            TrustCircleManager.this.cancelRegOrUpdate(this.mUserId);
        }
    }

    public void finalRegister(String authPkData, String authPkDataSign, String updateIndexInfo, String updateIndexSignature) {
        if (authPkData == null) {
            Log.e(TAG, "authPKData should't be null");
        } else if (authPkDataSign == null) {
            Log.e(TAG, "authPKDataSign should't be null");
        } else {
            ITrustCircleManager tcisService = getTrustCircleService();
            if (tcisService != null) {
                try {
                    tcisService.finalRegister(this.mILifeCycleCallback, authPkData, authPkDataSign, updateIndexInfo, updateIndexSignature);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when finalRegister is invoked");
                    LoginCallback loginCallback = this.mLoginCallback;
                    if (loginCallback != null) {
                        loginCallback.onFinalRegisterResult(-1);
                        this.mLoginCallback = null;
                    }
                }
            }
        }
    }

    public void finalLogin(int updateResult, String updateIndexInfo, String updateIndexSignature) {
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService != null) {
            try {
                tcisService.finalLogin(this.mILifeCycleCallback, updateResult, updateIndexInfo, updateIndexSignature);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when finalLogin is invoked");
                LoginCallback loginCallback = this.mLoginCallback;
                if (loginCallback != null) {
                    loginCallback.onFinalLoginResult(-1);
                    this.mLoginCallback = null;
                }
            }
        }
    }

    public void logout(LogoutCallback callback, long userId) {
        if (callback != null) {
            ITrustCircleManager tcisService = getTrustCircleService();
            if (tcisService != null) {
                try {
                    this.mLogoutCallback = callback;
                    tcisService.logout(this.mILifeCycleCallback, userId);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when logout is invoked");
                    this.mLogoutCallback.onLogoutResult(-1);
                    this.mLogoutCallback = null;
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an logout callback");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelRegOrUpdate(long userId) {
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService != null) {
            try {
                tcisService.cancelRegOrLogin(this.mILifeCycleCallback, userId);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when cancelRegOrUpdate is invoked");
            }
        }
    }

    public void unregister(UnregisterCallback callback, long userId) {
        if (callback != null) {
            ITrustCircleManager tcisService = getTrustCircleService();
            if (tcisService != null) {
                try {
                    this.mUnregisterCallback = callback;
                    tcisService.unregister(this.mILifeCycleCallback, userId);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when unregister is invoked");
                    this.mUnregisterCallback.onUnregisterResult(-1);
                    this.mUnregisterCallback = null;
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an unregister callback");
        }
    }

    public long activeAuth(AuthCallback callback, CancellationSignal cancel, AuthPara.InitAuthInfo info) {
        ITrustCircleManager tcisService = getTrustCircleService();
        long authId = -1;
        if (!(tcisService == null || info == null || callback == null)) {
            try {
                authId = tcisService.initAuthenticate(new AuthCallbackInner(callback), info.mAuthType, info.mAuthVersion, info.mPolicy, info.mUserID, info.mAESTmpKey);
            } catch (RemoteException e) {
                Log.e(TAG, "initAuthenticate failed");
            }
        }
        if (!(cancel == null || cancel.isCanceled() || authId == -1)) {
            cancel.setOnCancelListener(new OnAuthenticationCancelListener(authId));
        }
        return authId;
    }

    public long passiveAuth(AuthCallback callback, CancellationSignal cancel, AuthPara.RecAuthInfo info) {
        long authId;
        ITrustCircleManager tcisService = getTrustCircleService();
        if (!(tcisService == null || info == null || callback == null)) {
            try {
                authId = tcisService.receiveAuthSync(new AuthCallbackInner(callback), info.mAuthType, info.mAuthVersion, info.mTAVersion, info.mPolicy, info.mUserID, info.mAESTmpKey, info.mTcisId, info.mPkVersion, info.mNonce, info.mAuthKeyAlgoType, info.mAuthKeyInfo, info.mAuthKeyInfoSign);
            } catch (RemoteException e) {
                Log.e(TAG, "passiveAuth failed");
            }
            if (cancel == null && !cancel.isCanceled() && authId != -1) {
                cancel.setOnCancelListener(new OnAuthenticationCancelListener(authId));
            }
            return authId;
        }
        authId = -1;
        if (cancel == null) {
        }
        return authId;
    }

    public boolean receiveAuthInfo(AuthPara.Type type, long authId, Object info) {
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService == null) {
            return false;
        }
        if (type != null && info != null && authId >= 0) {
            try {
                int i = AnonymousClass2.$SwitchMap$android$trustcircle$AuthPara$Type[type.ordinal()];
                if (i != 1) {
                    if (i != 2) {
                        if (i != 3) {
                            if (i == 4) {
                                try {
                                    if (info instanceof AuthPara.ReqPkInfo) {
                                        return tcisService.requestPK(authId, ((AuthPara.ReqPkInfo) info).mUserID);
                                    }
                                } catch (RemoteException e) {
                                    Log.e(TAG, "receiveAuthInfo, type: " + type + " failed");
                                    return false;
                                }
                            }
                        } else if (info instanceof AuthPara.RespPkInfo) {
                            AuthPara.RespPkInfo pkInfo = (AuthPara.RespPkInfo) info;
                            return tcisService.receivePK(authId, pkInfo.mAuthKeyAlgoType, pkInfo.mAuthKeyData, pkInfo.mAuthKeyDataSign);
                        }
                    } else if (info instanceof AuthPara.RecAckInfo) {
                        return tcisService.receiveAck(authId, ((AuthPara.RecAckInfo) info).mMAC);
                    }
                } else if (info instanceof AuthPara.RecAuthAckInfo) {
                    AuthPara.RecAuthAckInfo authAckInfo = (AuthPara.RecAuthAckInfo) info;
                    try {
                        return tcisService.receiveAuthSyncAck(authId, authAckInfo.mTcisIDSlave, authAckInfo.mPkVersionSlave, authAckInfo.mNonceSlave, authAckInfo.mMacSlave, authAckInfo.mAuthKeyAlgoTypeSlave, authAckInfo.mAuthKeyInfoSlave, authAckInfo.mAuthKeyInfoSignSlave);
                    } catch (RemoteException e2) {
                        Log.e(TAG, "receiveAuthInfo, type: " + type + " failed");
                        return false;
                    }
                }
            } catch (RemoteException e3) {
                Log.e(TAG, "receiveAuthInfo, type: " + type + " failed");
                return false;
            }
        }
        return false;
    }

    /* renamed from: android.trustcircle.TrustCircleManager$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$android$trustcircle$AuthPara$Type = new int[AuthPara.Type.values().length];

        static {
            try {
                $SwitchMap$android$trustcircle$AuthPara$Type[AuthPara.Type.REC_AUTH_SYNC_ACK.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$trustcircle$AuthPara$Type[AuthPara.Type.REC_ACK.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$trustcircle$AuthPara$Type[AuthPara.Type.REC_PK.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$trustcircle$AuthPara$Type[AuthPara.Type.REQ_PK.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private class OnAuthenticationCancelListener implements CancellationSignal.OnCancelListener {
        private long mAuthID;

        OnAuthenticationCancelListener(long authId) {
            this.mAuthID = authId;
        }

        public void onCancel() {
            TrustCircleManager.this.cancelAuthentication(this.mAuthID);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelAuthentication(long authId) {
        ITrustCircleManager tcisService = getTrustCircleService();
        if (tcisService != null) {
            try {
                tcisService.cancelAuthentication(authId);
            } catch (RemoteException e) {
                Log.e(TAG, "cancelAuthentication failed");
            }
        }
    }

    private static class AuthCallbackInner extends IAuthCallback.Stub {
        AuthCallback mCallback;

        AuthCallbackInner(AuthCallback callback) {
            this.mCallback = callback;
        }

        public void onAuthSync(long authId, byte[] tcisId, int pkVersion, int taVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            AuthCallback authCallback = this.mCallback;
            if (authCallback != null) {
                authCallback.onAuthSync(authId, new AuthPara.OnAuthSyncInfo(tcisId, pkVersion, (short) taVersion, nonce, (short) authKeyAlgoType, authKeyInfo, authKeyInfoSign));
            }
        }

        public void onAuthError(long authId, int errorCode) {
            AuthCallback authCallback = this.mCallback;
            if (authCallback != null) {
                authCallback.onAuthError(authId, errorCode);
            }
        }

        public void onAuthSyncAck(long authId, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            AuthCallback authCallback = this.mCallback;
            if (authCallback != null) {
                authCallback.onAuthSyncAck(authId, new AuthPara.OnAuthSyncAckInfo(tcisIdSlave, pkVersionSlave, nonceSlave, mac, (short) authKeyAlgoType, authKeyInfo, authKeyInfoSign));
            }
        }

        public void onAuthSyncAckError(long authId, int errorCode) {
            AuthCallback authCallback = this.mCallback;
            if (authCallback != null) {
                authCallback.onAuthSyncAckError(authId, errorCode);
            }
        }

        public void onAuthAck(long authId, int result, byte[] sessionKeyIv, byte[] sessionKey, byte[] mac) {
            AuthCallback authCallback = this.mCallback;
            if (authCallback != null) {
                authCallback.onAuthAck(authId, new AuthPara.OnAuthAckInfo(result, sessionKeyIv, sessionKey, mac));
            }
        }

        public void onAuthAckError(long authId, int errorCode) {
            AuthCallback authCallback = this.mCallback;
            if (authCallback != null) {
                authCallback.onAuthAckError(authId, errorCode);
            }
        }

        public void requestPK() {
            AuthCallback authCallback = this.mCallback;
            if (authCallback != null) {
                authCallback.requestPK();
            }
        }

        public void responsePK(long authId, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) {
            AuthCallback authCallback = this.mCallback;
            if (authCallback != null) {
                authCallback.responsePK(authId, new AuthPara.RespPkInfo((short) authKeyAlgoType, authKeyData, authKeyDataSign));
            }
        }

        public void onAuthExited(long authId, int resultCode) {
            AuthCallback authCallback = this.mCallback;
            if (authCallback != null) {
                authCallback.onAuthExited(authId, resultCode);
            }
        }
    }
}
