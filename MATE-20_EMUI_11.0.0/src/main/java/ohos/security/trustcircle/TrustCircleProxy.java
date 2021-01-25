package ohos.security.trustcircle;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.trustcircle.AuthPara;
import android.trustcircle.TrustCircleManager;
import ohos.aafwk.content.IntentParams;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.security.trustcircle.AuthParaGroup;

public class TrustCircleProxy implements ITrustCircleProxy {
    private static final AuthPara.Type[] AUTH_PARA_TYPE_ARRAY = {AuthPara.Type.REC_AUTH_SYNC_ACK, AuthPara.Type.REC_ACK, AuthPara.Type.REQ_PK, AuthPara.Type.REC_PK};
    private static final String HW_USER_ID = "hwUserId";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218115840, "TrustCircleProxy");
    private static final String TA_VERSION = "TAVersion";
    private static final String TCIS_ID = "tcisID";
    private final IRemoteObject mRemoteService;

    public TrustCircleProxy(IRemoteObject iRemoteObject) {
        this.mRemoteService = iRemoteObject;
    }

    @Override // ohos.security.trustcircle.ITrustCircleProxy
    public IntentParams getTcisInfo() {
        if (this.mRemoteService == null) {
            HiLog.info(LABEL, "remote service cant be reached.", new Object[0]);
        }
        Bundle tcisInfo = TrustCircleManager.getInstance().getTcisInfo();
        if (tcisInfo != null) {
            return convertTcisInfo(tcisInfo);
        }
        return null;
    }

    @Override // ohos.security.trustcircle.ITrustCircleProxy
    public long initKeyAgreement(IKaListener iKaListener, int i, long j, byte[] bArr, String str) {
        return TrustCircleManager.getInstance().initKeyAgreement(new KaCallbackProxy(iKaListener), i, j, bArr, str);
    }

    @Override // ohos.security.trustcircle.ITrustCircleProxy
    public long activeAuth(IAuthListener iAuthListener, CancelSignal cancelSignal, AuthParaGroup.InitAuthInfo initAuthInfo) {
        return TrustCircleManager.getInstance().activeAuth(new AuthCallbackProxy(iAuthListener), new CancellationSignal(), new InitAuthInfoBase(initAuthInfo).getInitAuthInfo());
    }

    @Override // ohos.security.trustcircle.ITrustCircleProxy
    public long passiveAuth(IAuthListener iAuthListener, CancelSignal cancelSignal, AuthParaGroup.RecAuthInfo recAuthInfo) {
        return TrustCircleManager.getInstance().passiveAuth(new AuthCallbackProxy(iAuthListener), new CancellationSignal(), new RecAuthInfoBase(recAuthInfo).getRecAuthInfo());
    }

    @Override // ohos.security.trustcircle.ITrustCircleProxy
    public boolean receiveAuthInfo(int i, long j, Object obj) {
        if (i <= 0 || i >= 5) {
            return false;
        }
        AuthPara.Type type = AUTH_PARA_TYPE_ARRAY[i - 1];
        return TrustCircleManager.getInstance().receiveAuthInfo(type, j, convertIotObject(type, obj, j));
    }

    private IntentParams convertTcisInfo(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        IntentParams intentParams = new IntentParams();
        intentParams.setParam(TCIS_ID, bundle.getString(TCIS_ID, ""));
        intentParams.setParam(TA_VERSION, Short.valueOf(bundle.getShort(TA_VERSION, 0)));
        intentParams.setParam(HW_USER_ID, bundle.getString(HW_USER_ID, null));
        return intentParams;
    }

    private static class AuthCallbackProxy implements TrustCircleManager.AuthCallback {
        private final IAuthListener mListener;

        AuthCallbackProxy(IAuthListener iAuthListener) {
            this.mListener = iAuthListener;
        }

        public void onAuthSync(long j, AuthPara.OnAuthSyncInfo onAuthSyncInfo) {
            if (this.mListener == null) {
                HiLog.error(TrustCircleProxy.LABEL, "no resultListener, do nothing", new Object[0]);
                return;
            }
            this.mListener.onAuthSync(j, new AuthSyncInfoBase(onAuthSyncInfo).getAuthSyncInfo());
        }

        public void onAuthError(long j, int i) {
            IAuthListener iAuthListener = this.mListener;
            if (iAuthListener != null) {
                iAuthListener.onAuthError(j, i);
            }
        }

        public void onAuthSyncAck(long j, AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo) {
            if (this.mListener != null && onAuthSyncAckInfo != null) {
                this.mListener.onAuthSyncAck(j, new AuthSyncAckInfoBase(onAuthSyncAckInfo).getAuthSyncAckInfo());
            }
        }

        public void onAuthSyncAckError(long j, int i) {
            IAuthListener iAuthListener = this.mListener;
            if (iAuthListener != null) {
                iAuthListener.onAuthSyncAckError(j, i);
            }
        }

        public void onAuthAck(long j, AuthPara.OnAuthAckInfo onAuthAckInfo) {
            if (this.mListener != null && onAuthAckInfo != null) {
                this.mListener.onAuthAck(j, new AuthParaGroup.OnAuthAckInfo(onAuthAckInfo.mResult, onAuthAckInfo.mSessionKeyIV, onAuthAckInfo.mSessionKey, onAuthAckInfo.mMAC));
            }
        }

        public void onAuthAckError(long j, int i) {
            IAuthListener iAuthListener = this.mListener;
            if (iAuthListener != null) {
                iAuthListener.onAuthAckError(j, i);
            }
        }

        public void requestPK() {
            IAuthListener iAuthListener = this.mListener;
            if (iAuthListener != null) {
                iAuthListener.requestPublicKey();
            }
        }

        public void responsePK(long j, AuthPara.RespPkInfo respPkInfo) {
            if (this.mListener != null && respPkInfo != null) {
                this.mListener.responsePublicKey(j, new AuthParaGroup.RespPkInfo(respPkInfo.mAuthKeyAlgoType, respPkInfo.mAuthKeyData, respPkInfo.mAuthKeyDataSign));
            }
        }

        public void onAuthExited(long j, int i) {
            IAuthListener iAuthListener = this.mListener;
            if (iAuthListener != null) {
                iAuthListener.onAuthExited(j, i);
            }
        }
    }

    private static class KaCallbackProxy implements TrustCircleManager.KaCallback {
        private final IKaListener mCallback;

        KaCallbackProxy(IKaListener iKaListener) {
            this.mCallback = iKaListener;
        }

        public void onKaResult(long j, int i, byte[] bArr, byte[] bArr2) {
            IKaListener iKaListener = this.mCallback;
            if (iKaListener != null) {
                iKaListener.onResult(j, i, bArr, bArr2);
            }
        }

        public void onKaError(long j, int i) {
            IKaListener iKaListener = this.mCallback;
            if (iKaListener != null) {
                iKaListener.onError(j, i);
            }
        }
    }

    private Object convertIotObject(AuthPara.Type type, Object obj, long j) {
        if (obj == null) {
            return null;
        }
        Object switchAck = switchAck(type, obj);
        return switchAck == null ? switchPubKey(type, obj) : switchAck;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.security.trustcircle.TrustCircleProxy$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$trustcircle$AuthPara$Type = new int[AuthPara.Type.values().length];

        static {
            try {
                $SwitchMap$android$trustcircle$AuthPara$Type[AuthPara.Type.REC_AUTH_SYNC_ACK.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$android$trustcircle$AuthPara$Type[AuthPara.Type.REC_ACK.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$android$trustcircle$AuthPara$Type[AuthPara.Type.REC_PK.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$android$trustcircle$AuthPara$Type[AuthPara.Type.REQ_PK.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    private Object switchAck(AuthPara.Type type, Object obj) {
        int i = AnonymousClass1.$SwitchMap$android$trustcircle$AuthPara$Type[type.ordinal()];
        if (i != 1) {
            if (i == 2 && (obj instanceof AuthParaGroup.RecAckInfo)) {
                return new AuthPara.RecAckInfo(((AuthParaGroup.RecAckInfo) obj).getMac());
            }
            return null;
        } else if (!(obj instanceof AuthParaGroup.RecAuthAckInfo)) {
            return null;
        } else {
            AuthParaGroup.RecAuthAckInfo recAuthAckInfo = (AuthParaGroup.RecAuthAckInfo) obj;
            return new AuthPara.RecAuthAckInfo(recAuthAckInfo.getTcisIdSlave(), recAuthAckInfo.getPkVersionSlave(), recAuthAckInfo.getNonceSlave(), recAuthAckInfo.getMacSlave(), recAuthAckInfo.getAuthKeyAlgoTypeSlave(), recAuthAckInfo.getAuthKeyInfoSlave(), recAuthAckInfo.getAuthKeyInfoSignSlave());
        }
    }

    private Object switchPubKey(AuthPara.Type type, Object obj) {
        int i = AnonymousClass1.$SwitchMap$android$trustcircle$AuthPara$Type[type.ordinal()];
        if (i != 3) {
            if (i == 4 && (obj instanceof AuthParaGroup.ReqPkInfo)) {
                return new AuthPara.ReqPkInfo(((AuthParaGroup.ReqPkInfo) obj).getUserId());
            }
            return null;
        } else if (!(obj instanceof AuthParaGroup.RespPkInfo)) {
            return null;
        } else {
            AuthParaGroup.RespPkInfo respPkInfo = (AuthParaGroup.RespPkInfo) obj;
            return new AuthPara.RespPkInfo(respPkInfo.getAuthKeyAlgoType(), respPkInfo.getAuthKeyData(), respPkInfo.getAuthKeyDataSign());
        }
    }

    private static class RecAuthInfoBase {
        private final AuthPara.RecAuthInfo mRecAuthInfo;

        RecAuthInfoBase(AuthParaGroup.RecAuthInfo recAuthInfo) {
            if (recAuthInfo == null) {
                this.mRecAuthInfo = null;
            } else {
                this.mRecAuthInfo = new AuthPara.RecAuthInfo(recAuthInfo.getAuthType(), recAuthInfo.getAuthVersion(), recAuthInfo.getTaVersion(), recAuthInfo.getPolicy(), recAuthInfo.getUserId(), recAuthInfo.getAesTmpKey(), recAuthInfo.getTcisId(), recAuthInfo.getPkVersion(), recAuthInfo.getNonce(), recAuthInfo.getAuthKeyAlgoType(), recAuthInfo.getAuthKeyInfo(), recAuthInfo.getAuthKeyInfoSign());
            }
        }

        public AuthPara.RecAuthInfo getRecAuthInfo() {
            return this.mRecAuthInfo;
        }
    }

    private static class AuthSyncAckInfoBase {
        private final AuthParaGroup.OnAuthSyncAckInfo mAckInfo;

        AuthSyncAckInfoBase(AuthPara.OnAuthSyncAckInfo onAuthSyncAckInfo) {
            if (onAuthSyncAckInfo == null) {
                this.mAckInfo = null;
            } else {
                this.mAckInfo = new AuthParaGroup.OnAuthSyncAckInfo.Builder().setTcisIdSlave(onAuthSyncAckInfo.mTcisIdSlave).setPkVersionSlave(onAuthSyncAckInfo.mPkVersionSlave).setNonceSlave(onAuthSyncAckInfo.mNonceSlave).setMac(onAuthSyncAckInfo.mMAC).setAuthKeyAlgoType(onAuthSyncAckInfo.mAuthKeyAlgoType).setAuthKeyInfo(onAuthSyncAckInfo.mAuthKeyInfo).setAuthKeyInfoSign(onAuthSyncAckInfo.mAuthKeyInfoSign).create();
            }
        }

        public AuthParaGroup.OnAuthSyncAckInfo getAuthSyncAckInfo() {
            return this.mAckInfo;
        }
    }

    private static class InitAuthInfoBase {
        private final AuthPara.InitAuthInfo mInitAuthInfo;

        InitAuthInfoBase(AuthParaGroup.InitAuthInfo initAuthInfo) {
            if (initAuthInfo == null) {
                this.mInitAuthInfo = null;
            } else {
                this.mInitAuthInfo = new AuthPara.InitAuthInfo(initAuthInfo.getAuthType(), initAuthInfo.getAuthVersion(), initAuthInfo.getPolicy(), initAuthInfo.getUserId(), initAuthInfo.getAesTmpKey());
            }
        }

        public AuthPara.InitAuthInfo getInitAuthInfo() {
            return this.mInitAuthInfo;
        }
    }

    private static class AuthSyncInfoBase {
        private final AuthParaGroup.OnAuthSyncInfo mSyncInfo;

        AuthSyncInfoBase(AuthPara.OnAuthSyncInfo onAuthSyncInfo) {
            if (onAuthSyncInfo == null) {
                this.mSyncInfo = null;
            } else {
                this.mSyncInfo = new AuthParaGroup.OnAuthSyncInfo.Builder().setTcisId(onAuthSyncInfo.mTcisId).setPkVersion(onAuthSyncInfo.mPkVersion).setTaVersion(onAuthSyncInfo.mTAVersion).setNonce(onAuthSyncInfo.mNonce).setAuthKeyAlgoType(onAuthSyncInfo.mAuthKeyAlgoType).setAuthKeyInfo(onAuthSyncInfo.mAuthKeyInfo).setAuthKeyInfoSign(onAuthSyncInfo.mAuthKeyInfoSign).create();
            }
        }

        public AuthParaGroup.OnAuthSyncInfo getAuthSyncInfo() {
            return this.mSyncInfo;
        }
    }
}
