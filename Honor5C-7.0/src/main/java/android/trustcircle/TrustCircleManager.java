package android.trustcircle;

import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CancellationSignal.OnCancelListener;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.MSimTelephonyConstants;
import android.trustcircle.AuthPara.InitAuthInfo;
import android.trustcircle.AuthPara.OnAuthAckInfo;
import android.trustcircle.AuthPara.OnAuthSyncAckInfo;
import android.trustcircle.AuthPara.OnAuthSyncInfo;
import android.trustcircle.AuthPara.RecAckInfo;
import android.trustcircle.AuthPara.RecAuthAckInfo;
import android.trustcircle.AuthPara.RecAuthInfo;
import android.trustcircle.AuthPara.ReqPkInfo;
import android.trustcircle.AuthPara.RespPkInfo;
import android.trustcircle.AuthPara.Type;
import android.util.Log;
import huawei.android.security.IAuthCallback.Stub;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.ILifeCycleCallback;
import huawei.android.security.ITrustCircleManager;
import huawei.android.widget.ViewDragHelper;

public class TrustCircleManager {
    private static final /* synthetic */ int[] -android-trustcircle-AuthPara$TypeSwitchesValues = null;
    private static final int AUTH_ID_ERROR = -1;
    private static final int LIFE_CYCLE_ERROR = -1;
    private static final int RESULT_OK = 0;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "TrustCircleManager";
    private static final int TRUSTCIRCLE_PLUGIN_ID = 5;
    private static volatile TrustCircleManager sInstance;
    private ILifeCycleCallback mILifeCycleCallback;
    private LoginCallback mLoginCallback;
    private LogoutCallback mLogoutCallback;
    private IHwSecurityService mSecurityService;
    private IBinder mToken;
    private UnregisterCallback mUnregisterCallback;

    public interface AuthCallback {
        void onAuthAck(long j, OnAuthAckInfo onAuthAckInfo);

        void onAuthAckError(long j, int i);

        void onAuthError(long j, int i);

        void onAuthSync(long j, OnAuthSyncInfo onAuthSyncInfo);

        void onAuthSyncAck(long j, OnAuthSyncAckInfo onAuthSyncAckInfo);

        void onAuthSyncAckError(long j, int i);

        void requestPK();

        void responsePK(long j, RespPkInfo respPkInfo);
    }

    private static class AuthCallbackInner extends Stub {
        AuthCallback mCallback;

        AuthCallbackInner(AuthCallback callback) {
            this.mCallback = callback;
        }

        public void onAuthSync(long authID, byte[] tcisId, int pkVersion, int taVersion, long nonce, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            if (this.mCallback != null) {
                this.mCallback.onAuthSync(authID, new OnAuthSyncInfo(tcisId, pkVersion, (short) taVersion, nonce, (short) authKeyAlgoType, authKeyInfo, authKeyInfoSign));
            }
        }

        public void onAuthError(long authID, int errorCode) {
            if (this.mCallback != null) {
                this.mCallback.onAuthError(authID, errorCode);
            }
        }

        public void onAuthSyncAck(long authID, byte[] tcisIdSlave, int pkVersionSlave, long nonceSlave, byte[] mac, int authKeyAlgoType, byte[] authKeyInfo, byte[] authKeyInfoSign) {
            if (this.mCallback != null) {
                this.mCallback.onAuthSyncAck(authID, new OnAuthSyncAckInfo(tcisIdSlave, pkVersionSlave, nonceSlave, mac, (short) authKeyAlgoType, authKeyInfo, authKeyInfoSign));
            }
        }

        public void onAuthSyncAckError(long authID, int errorCode) {
            if (this.mCallback != null) {
                this.mCallback.onAuthSyncAckError(authID, errorCode);
            }
        }

        public void onAuthAck(long authID, int result, byte[] sessionKeyIV, byte[] sessionKey, byte[] mac) {
            if (this.mCallback != null) {
                this.mCallback.onAuthAck(authID, new OnAuthAckInfo(result, sessionKeyIV, sessionKey, mac));
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
                this.mCallback.responsePK(authID, new RespPkInfo((short) authKeyAlgoType, authKeyData, authKeyDataSign));
            }
        }
    }

    public interface LoginCallback {
        void onFinalLoginResult(int i);

        void onFinalRegisterResult(int i);

        void onLoginResponse(int i, int i2, String str);

        void onRegisterResponse(int i, int i2, short s, String str, String str2, String str3);
    }

    public interface LogoutCallback {
        void onLogoutResult(int i);
    }

    private class OnAuthenticationCancelListener implements OnCancelListener {
        private long authID;

        OnAuthenticationCancelListener(long authID) {
            this.authID = authID;
        }

        public void onCancel() {
            TrustCircleManager.this.cancelAuthentication(this.authID);
        }
    }

    private class OnRegOrLoginCancelListener implements OnCancelListener {
        private long userID;

        public OnRegOrLoginCancelListener(long userID) {
            this.userID = userID;
        }

        public void onCancel() {
            Log.d(TrustCircleManager.TAG, "cancelRegOrLogin");
            TrustCircleManager.this.cancelRegOrUpdate(this.userID);
        }
    }

    public interface UnregisterCallback {
        void onUnregisterResult(int i);
    }

    private static /* synthetic */ int[] -getandroid-trustcircle-AuthPara$TypeSwitchesValues() {
        if (-android-trustcircle-AuthPara$TypeSwitchesValues != null) {
            return -android-trustcircle-AuthPara$TypeSwitchesValues;
        }
        int[] iArr = new int[Type.values().length];
        try {
            iArr[Type.REC_ACK.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Type.REC_AUTH_SYNC_ACK.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Type.REC_PK.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Type.REQ_PK.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-trustcircle-AuthPara$TypeSwitchesValues = iArr;
        return iArr;
    }

    private TrustCircleManager() {
        this.mToken = new Binder();
        this.mILifeCycleCallback = new ILifeCycleCallback.Stub() {
            public void onRegisterResponse(int errorCode, int globalKeyID, int authKeyAlgoType, String regAuthKeyData, String regAuthKeyDataSign, String clientChallenge) {
                if (TrustCircleManager.this.mLoginCallback != null) {
                    TrustCircleManager.this.mLoginCallback.onRegisterResponse(errorCode, globalKeyID, (short) authKeyAlgoType, regAuthKeyData, regAuthKeyDataSign, clientChallenge);
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
        this.mSecurityService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
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
                    ITrustCircleManager tcisService = ITrustCircleManager.Stub.asInterface(this.mSecurityService.bind(TRUSTCIRCLE_PLUGIN_ID, this.mToken));
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

    public void loginServerRequest(LoginCallback callback, CancellationSignal cancel, long userID, int serverRegisterStatus, String sessionID) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an login callback");
        } else if (sessionID == null) {
            Log.e(TAG, "session id should't be null");
            callback.onLoginResponse(LIFE_CYCLE_ERROR, LIFE_CYCLE_ERROR, null);
        } else {
            if (cancel != null) {
                if (cancel.isCanceled()) {
                    Log.e(TAG, "login already canceled");
                    return;
                }
                cancel.setOnCancelListener(new OnRegOrLoginCancelListener(userID));
            }
            ITrustCircleManager plugin = getTrustCirclePlugin();
            if (plugin != null) {
                this.mLoginCallback = callback;
                try {
                    plugin.loginServerRequest(this.mILifeCycleCallback, userID, serverRegisterStatus, sessionID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when loginServerRequest is invoked");
                    if (this.mLoginCallback != null) {
                        this.mLoginCallback.onRegisterResponse(LIFE_CYCLE_ERROR, LIFE_CYCLE_ERROR, (short) -1, null, null, null);
                        this.mLoginCallback = null;
                    }
                }
            }
        }
    }

    public void finalRegister(String authPKData, String authPKDataSign, String updateIndexInfo, String updateIndexSignature) {
        if (authPKData == null || authPKDataSign == null) {
            StringBuffer exception = new StringBuffer();
            String str = authPKData == null ? "authPKData " : authPKDataSign == null ? "and " : MSimTelephonyConstants.MY_RADIO_PLATFORM;
            exception.append(str).append(authPKDataSign == null ? "authPKDataSign " : MSimTelephonyConstants.MY_RADIO_PLATFORM).append("should't be null");
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
                    this.mLoginCallback.onFinalRegisterResult(LIFE_CYCLE_ERROR);
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
                    this.mLoginCallback.onFinalLoginResult(LIFE_CYCLE_ERROR);
                    this.mLoginCallback = null;
                }
            }
        }
    }

    public void logout(LogoutCallback callback, long userID) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an logout callback");
        }
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (plugin != null) {
            try {
                this.mLogoutCallback = callback;
                plugin.logout(this.mILifeCycleCallback, userID);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when logout is invoked");
                if (this.mLogoutCallback != null) {
                    this.mLogoutCallback.onLogoutResult(LIFE_CYCLE_ERROR);
                    this.mLogoutCallback = null;
                }
            }
        }
    }

    private void cancelRegOrUpdate(long userID) {
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
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an unregister callback");
        }
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (plugin != null) {
            try {
                this.mUnregisterCallback = callback;
                plugin.unregister(this.mILifeCycleCallback, userID);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when unregister is invoked");
            }
        }
    }

    public long activeAuth(AuthCallback callback, CancellationSignal cancel, InitAuthInfo info) {
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

    public long passiveAuth(AuthCallback callback, CancellationSignal cancel, RecAuthInfo info) {
        ITrustCircleManager plugin = getTrustCirclePlugin();
        long authID = -1;
        if (!(plugin == null || info == null || callback == null)) {
            try {
                authID = plugin.receiveAuthSync(new AuthCallbackInner(callback), info.mAuthType, info.mAuthVersion, info.mTAVersion, info.mPolicy, info.mUserID, info.mAESTmpKey, info.mTcisId, info.mPkVersion, info.mNonce, info.mAuthKeyAlgoType, info.mAuthKeyInfo, info.mAuthKeyInfoSign);
            } catch (RemoteException e) {
                Log.e(TAG, "passiveAuth failed");
            }
        }
        if (!(cancel == null || cancel.isCanceled() || authID == -1)) {
            cancel.setOnCancelListener(new OnAuthenticationCancelListener(authID));
        }
        return authID;
    }

    public boolean reveiveAuthInfo(Type type, long authID, Object info) {
        ITrustCircleManager plugin = getTrustCirclePlugin();
        if (!(plugin == null || type == null || info == null || authID < 0)) {
            try {
                switch (-getandroid-trustcircle-AuthPara$TypeSwitchesValues()[type.ordinal()]) {
                    case ViewDragHelper.STATE_DRAGGING /*1*/:
                        if (info instanceof RecAckInfo) {
                            return plugin.receiveAck(authID, ((RecAckInfo) info).mMAC);
                        }
                        break;
                    case ViewDragHelper.STATE_SETTLING /*2*/:
                        if (info instanceof RecAuthAckInfo) {
                            RecAuthAckInfo authAckInfo = (RecAuthAckInfo) info;
                            return plugin.receiveAuthSyncAck(authID, authAckInfo.mTcisIDSlave, authAckInfo.mPkVersionSlave, authAckInfo.mNonceSlave, authAckInfo.mMacSlave, authAckInfo.mAuthKeyAlgoTypeSlave, authAckInfo.mAuthKeyInfoSlave, authAckInfo.mAuthKeyInfoSignSlave);
                        }
                        break;
                    case ViewDragHelper.DIRECTION_ALL /*3*/:
                        if (info instanceof RespPkInfo) {
                            RespPkInfo pkInfo = (RespPkInfo) info;
                            return plugin.receivePK(authID, pkInfo.mAuthKeyAlgoType, pkInfo.mAuthKeyData, pkInfo.mAuthKeyDataSign);
                        }
                        break;
                    case ViewDragHelper.EDGE_TOP /*4*/:
                        if (info instanceof ReqPkInfo) {
                            return plugin.requestPK(authID, ((ReqPkInfo) info).mUserID);
                        }
                        break;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "reveiveAuthInfo, type: " + type + " failed");
            }
        }
        return false;
    }

    private void cancelAuthentication(long authId) {
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
