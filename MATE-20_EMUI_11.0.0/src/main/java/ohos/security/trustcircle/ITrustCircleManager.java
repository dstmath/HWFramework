package ohos.security.trustcircle;

import ohos.aafwk.content.IntentParams;
import ohos.security.trustcircle.AuthParaGroup;

public abstract class ITrustCircleManager {
    public abstract long activeAuth(IAuthListener iAuthListener, CancelSignal cancelSignal, AuthParaGroup.InitAuthInfo initAuthInfo);

    public abstract IntentParams getTcisInfo();

    public abstract long initKeyAgreement(IKaListener iKaListener, int i, long j, byte[] bArr, String str);

    public abstract long passiveAuth(IAuthListener iAuthListener, CancelSignal cancelSignal, AuthParaGroup.RecAuthInfo recAuthInfo);

    public abstract boolean receiveAuthInfo(int i, long j, Object obj);
}
