package android.trustcircle;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.trustcircle.AuthPara;
import android.util.Log;
import huawei.android.security.IAuthCallback;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IKaCallback;
import huawei.android.security.ILifeCycleCallback;
import huawei.android.security.ITrustCircleManager;

public class TrustCircleManager {
    private static final int AUTH_ID_ERROR = -1;
    private static final int LIFE_CYCLE_ERROR = -1;
    private static final int RESULT_OK = 0;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "TrustCircleManager";
    private static final int TRUSTCIRCLE_PLUGIN_ID = 5;
    private static volatile TrustCircleManager sInstance;
    private ILifeCycleCallback mILifeCycleCallback = new ILifeCycleCallback.Stub() {
        public void onRegisterResponse(int errorCode, int globalKeyID, int authKeyAlgoType, String regAuthKeyData, String regAuthKeyDataSign, String clientChallenge) {
            if (TrustCircleManager.this.mLoginCallback != null) {
                TrustCircleManager.this.mLoginCallback.onRegisterResponse(errorCode, globalKeyID, (short) authKeyAlgoType, regAuthKeyData, regAuthKeyDataSign, clientChallenge);
                if (errorCode != 0) {
                    LoginCallback unused = TrustCircleManager.this.mLoginCallback = null;
                }
            }
        }

        public void onFinalRegisterResult(int errorCode) {
            if (TrustCircleManager.this.mLoginCallback != null) {
                TrustCircleManager.this.mLoginCallback.onFinalRegisterResult(errorCode);
                if (errorCode != 0) {
                    LoginCallback unused = TrustCircleManager.this.mLoginCallback = null;
                }
            }
        }

        public void onLoginResponse(int errorCode, int indexVersion, String clientChallenge) {
            if (TrustCircleManager.this.mLoginCallback != null) {
                TrustCircleManager.this.mLoginCallback.onLoginResponse(errorCode, indexVersion, clientChallenge);
                if (errorCode != 0) {
                    LoginCallback unused = TrustCircleManager.this.mLoginCallback = null;
                }
            }
        }

        public void onUpdateResponse(int errorCode, int indexVersion, String clientChallenge) {
            if (TrustCircleManager.this.mLoginCallback != null) {
                TrustCircleManager.this.mLoginCallback.onUpdateResponse(errorCode, indexVersion, clientChallenge);
                if (errorCode != 0) {
                    LoginCallback unused = TrustCircleManager.this.mLoginCallback = null;
                }
            }
        }

        public void onFinalLoginResult(int errorCode) {
            if (TrustCircleManager.this.mLoginCallback != null) {
                TrustCircleManager.this.mLoginCallback.onFinalLoginResult(errorCode);
                LoginCallback unused = TrustCircleManager.this.mLoginCallback = null;
            }
        }

        public void onLogoutResult(int errorCode) {
            if (TrustCircleManager.this.mLogoutCallback != null) {
                TrustCircleManager.this.mLogoutCallback.onLogoutResult(errorCode);
                LogoutCallback unused = TrustCircleManager.this.mLogoutCallback = null;
            }
        }

        public void onUnregisterResult(int errorCode) {
            if (TrustCircleManager.this.mUnregisterCallback != null) {
                TrustCircleManager.this.mUnregisterCallback.onUnregisterResult(errorCode);
                UnregisterCallback unused = TrustCircleManager.this.mUnregisterCallback = null;
            }
        }
    };
    /* access modifiers changed from: private */
    public LoginCallback mLoginCallback;
    /* access modifiers changed from: private */
    public LogoutCallback mLogoutCallback;
    private IHwSecurityService mSecurityService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
    /* access modifiers changed from: private */
    public UnregisterCallback mUnregisterCallback;

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

    private static class AuthCallbackInner extends IAuthCallback.Stub {
        AuthCallback mCallback;

        AuthCallbackInner(AuthCallback callback) {
            this.mCallback = callback;
        }

        public void onAuthSync(long authID, byte[] tcisId, int pkVersion, int taVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            if (this.mCallback != null) {
                AuthCallback authCallback = this.mCallback;
                AuthPara.OnAuthSyncInfo onAuthSyncInfo = new AuthPara.OnAuthSyncInfo(tcisId, pkVersion, (short) taVersion, nonce, (short) authKeyAlgoType, authKeyInfo, authKeyInfoSign);
                authCallback.onAuthSync(authID, onAuthSyncInfo);
                return;
            }
            long j = authID;
            int i = taVersion;
            int i2 = authKeyAlgoType;
        }

        public void onAuthError(long authID, int errorCode) {
            if (this.mCallback != null) {
                this.mCallback.onAuthError(authID, errorCode);
            }
        }

        public void onAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            if (this.mCallback != null) {
                AuthCallback authCallback = this.mCallback;
                AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo = new AuthPara.OnAuthSyncAckInfo(tcisIdSlave, pkVersionSlave, nonceSlave, mac, (short) authKeyAlgoType, authKeyInfo, authKeyInfoSign);
                authCallback.onAuthSyncAck(authID, onAuthSyncAckInfo);
                return;
            }
            long j = authID;
            int i = authKeyAlgoType;
        }

        public void onAuthSyncAckError(long authID, int errorCode) {
            if (this.mCallback != null) {
                this.mCallback.onAuthSyncAckError(authID, errorCode);
            }
        }

        public void onAuthAck(long authID, int result, byte[] sessionKeyIV, byte[] sessionKey, byte[] mac) {
            if (this.mCallback != null) {
                this.mCallback.onAuthAck(authID, new AuthPara.OnAuthAckInfo(result, sessionKeyIV, sessionKey, mac));
            }
        }

        public void onAuthAckError(long authID, int errorCode) {
            if (this.mCallback != null) {
                this.mCallback.onAuthAckError(authID, errorCode);
            }
        }

        public void requestPK() {
            if (this.mCallback != null) {
                this.mCallback.requestPK();
            }
        }

        public void responsePK(long authID, int authKeyAlgoType, byte[] authKeyData, byte[] authKeyDataSign) {
            if (this.mCallback != null) {
                this.mCallback.responsePK(authID, new AuthPara.RespPkInfo((short) authKeyAlgoType, authKeyData, authKeyDataSign));
            }
        }

        public void onAuthExited(long authID, int resultCode) {
            if (this.mCallback != null) {
                this.mCallback.onAuthExited(authID, resultCode);
            }
        }
    }

    private static class IKaCallbackInner extends IKaCallback.Stub {
        KaCallback mKaCallback;

        public IKaCallbackInner(KaCallback callback) {
            this.mKaCallback = callback;
        }

        public void onKaResult(long authId, int result, byte[] iv, byte[] payload) {
            if (this.mKaCallback != null) {
                this.mKaCallback.onKaResult(authId, result, iv, payload);
                this.mKaCallback = null;
            }
        }

        public void onKaError(long authId, int errorCode) {
            if (this.mKaCallback != null) {
                this.mKaCallback.onKaError(authId, errorCode);
                this.mKaCallback = null;
            }
        }
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

    private class OnAuthenticationCancelListener implements CancellationSignal.OnCancelListener {
        private long authID;

        OnAuthenticationCancelListener(long authID2) {
            this.authID = authID2;
        }

        public void onCancel() {
            TrustCircleManager.this.cancelAuthentication(this.authID);
        }
    }

    private class OnRegOrLoginCancelListener implements CancellationSignal.OnCancelListener {
        private long userID;

        public OnRegOrLoginCancelListener(long userID2) {
            this.userID = userID2;
        }

        public void onCancel() {
            Log.d(TrustCircleManager.TAG, "cancelRegOrLogin");
            TrustCircleManager.this.cancelRegOrUpdate(this.userID);
        }
    }

    public interface UnregisterCallback {
        void onUnregisterResult(int i);
    }

    private TrustCircleManager() {
        if (this.mSecurityService == null) {
            Log.e(TAG, "error, securityservice was null");
        }
    }

    public static TrustCircleManager getInstance() {
        if (sInstance == null) {
            synchronized (TrustCircleManager.class) {
                if (sInstance == null) {
                    sInstance = new TrustCircleManager();
                }
            }
        }
        return sInstance;
    }

    private ITrustCircleManager getTrustCirclePlugin() {
        synchronized (this) {
            if (this.mSecurityService != null) {
                try {
                    ITrustCircleManager tcisService = ITrustCircleManager.Stub.asInterface(this.mSecurityService.querySecurityInterface(5));
                    if (tcisService == null) {
                        Log.e(TAG, "error, TrustCirclePlugin is null");
                    }
                    return tcisService;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getTrustCirclePlugin invoked");
                }
            }
            Log.e(TAG, "error, SecurityService is null");
            return null;
        }
    }

    public Bundle getTcisInfo() {
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (plugin != null) {
            try {
                return plugin.getTcisInfo();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when getTcisInfo is invoked");
            }
        }
        return null;
    }

    public long initKeyAgreement(KaCallback callback, int kaVersion, long userId, byte[] aesTmpKey, String kaInfo) {
        KaCallback kaCallback = callback;
        if (kaCallback == null || aesTmpKey == null || kaInfo == null) {
            throw new IllegalArgumentException("illegal null params.");
        }
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (plugin != null) {
            IKaCallbackInner mKaCallback = new IKaCallbackInner(kaCallback);
            try {
                return plugin.initKeyAgreement(mKaCallback, kaVersion, userId, aesTmpKey, kaInfo);
            } catch (RemoteException e) {
                RemoteException remoteException = e;
                Log.e(TAG, "RemoteException when initKeyAgreement is invoked");
                mKaCallback.onKaError(-1, -1);
            }
        }
        return -1;
    }

    public void loginServerRequest(LoginCallback callback, CancellationSignal cancel, long userID, int serverRegisterStatus, String sessionID) {
        long j;
        LoginCallback loginCallback = callback;
        CancellationSignal cancellationSignal = cancel;
        if (loginCallback == null) {
            long j2 = userID;
            throw new IllegalArgumentException("Must supply an login callback");
        } else if (sessionID == null) {
            Log.e(TAG, "session id should't be null");
            loginCallback.onLoginResponse(-1, -1, null);
        } else {
            if (cancellationSignal == null) {
                j = userID;
            } else if (cancel.isCanceled()) {
                Log.e(TAG, "login already canceled");
                return;
            } else {
                j = userID;
                cancellationSignal.setOnCancelListener(new OnRegOrLoginCancelListener(j));
            }
            ITrustCircleManager plugin = getTrustCirclePlugin();
            if (plugin != null) {
                this.mLoginCallback = loginCallback;
                try {
                    plugin.loginServerRequest(this.mILifeCycleCallback, j, serverRegisterStatus, sessionID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when loginServerRequest is invoked");
                    if (this.mLoginCallback != null) {
                        this.mLoginCallback.onRegisterResponse(-1, -1, -1, null, null, null);
                        this.mLoginCallback = null;
                    }
                }
            }
        }
    }

    public void updateServerRequest(LoginCallback callback, CancellationSignal cancel, long userID) {
        if (callback != null) {
            if (cancel != null) {
                if (cancel.isCanceled()) {
                    Log.e(TAG, "update already canceled");
                    return;
                }
                cancel.setOnCancelListener(new OnRegOrLoginCancelListener(userID));
            }
            ITrustCircleManager plugin = getTrustCirclePlugin();
            if (plugin != null) {
                this.mLoginCallback = callback;
                try {
                    plugin.updateServerRequest(this.mILifeCycleCallback, userID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when loginServerRequest is invoked");
                    if (this.mLoginCallback != null) {
                        this.mLoginCallback.onUpdateResponse(-1, -1, null);
                        this.mLoginCallback = null;
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException("Must supply an login callback");
    }

    public void finalRegister(String authPKData, String authPKDataSign, String updateIndexInfo, String updateIndexSignature) {
        String str;
        if (authPKData == null || authPKDataSign == null) {
            StringBuffer exception = new StringBuffer();
            if (authPKData == null) {
                str = "authPKData ";
            } else {
                str = authPKDataSign == null ? "and " : "";
            }
            exception.append(str);
            exception.append(authPKDataSign == null ? "authPKDataSign " : "");
            exception.append("should't be null");
            Log.e(TAG, exception.toString());
            return;
        }
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (plugin != null) {
            try {
                plugin.finalRegister(this.mILifeCycleCallback, authPKData, authPKDataSign, updateIndexInfo, updateIndexSignature);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when finalRegister is invoked");
                if (this.mLoginCallback != null) {
                    this.mLoginCallback.onFinalRegisterResult(-1);
                    this.mLoginCallback = null;
                }
            }
        }
    }

    public void finalLogin(int updateResult, String updateIndexInfo, String updateIndexSignature) {
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (plugin != null) {
            try {
                plugin.finalLogin(this.mILifeCycleCallback, updateResult, updateIndexInfo, updateIndexSignature);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when finalLogin is invoked");
                if (this.mLoginCallback != null) {
                    this.mLoginCallback.onFinalLoginResult(-1);
                    this.mLoginCallback = null;
                }
            }
        }
    }

    public void logout(LogoutCallback callback, long userID) {
        if (callback != null) {
            ITrustCircleManager plugin = getTrustCirclePlugin();
            if (plugin != null) {
                try {
                    this.mLogoutCallback = callback;
                    plugin.logout(this.mILifeCycleCallback, userID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when logout is invoked");
                    if (this.mLogoutCallback != null) {
                        this.mLogoutCallback.onLogoutResult(-1);
                        this.mLogoutCallback = null;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an logout callback");
        }
    }

    /* access modifiers changed from: private */
    public void cancelRegOrUpdate(long userID) {
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (plugin != null) {
            try {
                plugin.cancelRegOrLogin(this.mILifeCycleCallback, userID);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when cancelRegOrUpdate is invoked");
            }
        }
    }

    public void unregister(UnregisterCallback callback, long userID) {
        if (callback != null) {
            ITrustCircleManager plugin = getTrustCirclePlugin();
            if (plugin != null) {
                try {
                    this.mUnregisterCallback = callback;
                    plugin.unregister(this.mILifeCycleCallback, userID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when unregister is invoked");
                    if (this.mUnregisterCallback != null) {
                        this.mUnregisterCallback.onUnregisterResult(-1);
                        this.mUnregisterCallback = null;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an unregister callback");
        }
    }

    public long activeAuth(AuthCallback callback, CancellationSignal cancel, AuthPara.InitAuthInfo info) {
        ITrustCircleManager plugin = getTrustCirclePlugin();
        long authID = -1;
        if (!(plugin == null || info == null || callback == null)) {
            try {
                authID = plugin.initAuthenticate(new AuthCallbackInner(callback), info.mAuthType, info.mAuthVersion, info.mPolicy, info.mUserID, info.mAESTmpKey);
            } catch (RemoteException e) {
                Log.e(TAG, "initAuthenticate failed");
            }
        }
        if (!(cancel == null || cancel.isCanceled() || authID == -1)) {
            cancel.setOnCancelListener(new OnAuthenticationCancelListener(authID));
        }
        return authID;
    }

    public long passiveAuth(AuthCallback callback, CancellationSignal cancel, AuthPara.RecAuthInfo info) {
        long authID;
        CancellationSignal cancellationSignal;
        AuthCallback authCallback = callback;
        CancellationSignal cancellationSignal2 = cancel;
        AuthPara.RecAuthInfo recAuthInfo = info;
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (!(plugin == null || recAuthInfo == null || authCallback == null)) {
            try {
                authID = plugin.receiveAuthSync(new AuthCallbackInner(authCallback), recAuthInfo.mAuthType, recAuthInfo.mAuthVersion, recAuthInfo.mTAVersion, recAuthInfo.mPolicy, recAuthInfo.mUserID, recAuthInfo.mAESTmpKey, recAuthInfo.mTcisId, recAuthInfo.mPkVersion, recAuthInfo.mNonce, recAuthInfo.mAuthKeyAlgoType, recAuthInfo.mAuthKeyInfo, recAuthInfo.mAuthKeyInfoSign);
            } catch (RemoteException e) {
                Log.e(TAG, "passiveAuth failed");
            }
            cancellationSignal = cancel;
            if (cancellationSignal == null && !cancel.isCanceled() && authID != -1) {
                cancellationSignal.setOnCancelListener(new OnAuthenticationCancelListener(authID));
            }
            return authID;
        }
        authID = -1;
        cancellationSignal = cancel;
        if (cancellationSignal == null) {
        }
        return authID;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0054, code lost:
        r17 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0021, code lost:
        r17 = r12;
     */
    public boolean receiveAuthInfo(AuthPara.Type type, long authID, Object info) {
        AuthPara.Type type2 = type;
        long j = authID;
        Object obj = info;
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (plugin == null || type2 == null || obj == null || j < 0) {
        } else {
            try {
                switch (type) {
                    case REC_AUTH_SYNC_ACK:
                        if (obj instanceof AuthPara.RecAuthAckInfo) {
                            AuthPara.RecAuthAckInfo authAckInfo = (AuthPara.RecAuthAckInfo) obj;
                            ITrustCircleManager iTrustCircleManager = plugin;
                            try {
                                return plugin.receiveAuthSyncAck(j, authAckInfo.mTcisIDSlave, authAckInfo.mPkVersionSlave, authAckInfo.mNonceSlave, authAckInfo.mMacSlave, authAckInfo.mAuthKeyAlgoTypeSlave, authAckInfo.mAuthKeyInfoSlave, authAckInfo.mAuthKeyInfoSignSlave);
                            } catch (RemoteException e) {
                                Log.e(TAG, "reveiveAuthInfo, type: " + type2 + " failed");
                                return false;
                            }
                        }
                        break;
                    case REC_ACK:
                        if (obj instanceof AuthPara.RecAckInfo) {
                            return plugin.receiveAck(j, ((AuthPara.RecAckInfo) obj).mMAC);
                        }
                        break;
                    case REC_PK:
                        if (obj instanceof AuthPara.RespPkInfo) {
                            AuthPara.RespPkInfo pkInfo = (AuthPara.RespPkInfo) obj;
                            return plugin.receivePK(j, pkInfo.mAuthKeyAlgoType, pkInfo.mAuthKeyData, pkInfo.mAuthKeyDataSign);
                        }
                        break;
                    case REQ_PK:
                        try {
                            if (obj instanceof AuthPara.ReqPkInfo) {
                                return plugin.requestPK(j, ((AuthPara.ReqPkInfo) obj).mUserID);
                            }
                        } catch (RemoteException e2) {
                            ITrustCircleManager iTrustCircleManager2 = plugin;
                            Log.e(TAG, "reveiveAuthInfo, type: " + type2 + " failed");
                            return false;
                        }
                        break;
                }
            } catch (RemoteException e3) {
                ITrustCircleManager iTrustCircleManager3 = plugin;
                Log.e(TAG, "reveiveAuthInfo, type: " + type2 + " failed");
                return false;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void cancelAuthentication(long authId) {
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (plugin != null) {
            try {
                plugin.cancelAuthentication(authId);
            } catch (RemoteException e) {
                Log.e(TAG, "cancelAuthentication failed");
            }
        }
    }
}
