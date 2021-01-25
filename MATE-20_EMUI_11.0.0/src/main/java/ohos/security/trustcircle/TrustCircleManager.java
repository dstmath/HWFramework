package ohos.security.trustcircle;

import ohos.aafwk.content.IntentParams;
import ohos.security.trustcircle.AuthParaGroup;
import ohos.sysability.samgr.SysAbilityManager;

public class TrustCircleManager {
    private static final long DEFAULT_ERROR = -1;
    private static final int SA_ID = 3599;

    private TrustCircleManager() {
    }

    public static ITrustCircleManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final TrustCircleManagerSingleton INSTANCE = new TrustCircleManagerSingleton();

        private SingletonHolder() {
        }
    }

    /* access modifiers changed from: private */
    public static class TrustCircleManagerSingleton extends ITrustCircleManager {
        private TrustCircleProxy mRemoteProxy = new TrustCircleProxy(SysAbilityManager.getSysAbility(TrustCircleManager.SA_ID));

        TrustCircleManagerSingleton() {
        }

        @Override // ohos.security.trustcircle.ITrustCircleManager
        public IntentParams getTcisInfo() {
            TrustCircleProxy trustCircleProxy = this.mRemoteProxy;
            if (trustCircleProxy != null) {
                return trustCircleProxy.getTcisInfo();
            }
            return null;
        }

        @Override // ohos.security.trustcircle.ITrustCircleManager
        public long initKeyAgreement(IKaListener iKaListener, int i, long j, byte[] bArr, String str) {
            TrustCircleProxy trustCircleProxy = this.mRemoteProxy;
            if (trustCircleProxy != null) {
                return trustCircleProxy.initKeyAgreement(iKaListener, i, j, bArr, str);
            }
            return -1;
        }

        @Override // ohos.security.trustcircle.ITrustCircleManager
        public long activeAuth(IAuthListener iAuthListener, CancelSignal cancelSignal, AuthParaGroup.InitAuthInfo initAuthInfo) {
            TrustCircleProxy trustCircleProxy = this.mRemoteProxy;
            if (trustCircleProxy != null) {
                return trustCircleProxy.activeAuth(iAuthListener, cancelSignal, initAuthInfo);
            }
            return -1;
        }

        @Override // ohos.security.trustcircle.ITrustCircleManager
        public long passiveAuth(IAuthListener iAuthListener, CancelSignal cancelSignal, AuthParaGroup.RecAuthInfo recAuthInfo) {
            TrustCircleProxy trustCircleProxy = this.mRemoteProxy;
            if (trustCircleProxy != null) {
                return trustCircleProxy.passiveAuth(iAuthListener, cancelSignal, recAuthInfo);
            }
            return -1;
        }

        @Override // ohos.security.trustcircle.ITrustCircleManager
        public boolean receiveAuthInfo(int i, long j, Object obj) {
            TrustCircleProxy trustCircleProxy = this.mRemoteProxy;
            if (trustCircleProxy != null) {
                return trustCircleProxy.receiveAuthInfo(i, j, obj);
            }
            return true;
        }
    }
}
