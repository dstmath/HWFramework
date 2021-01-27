package ohos.security.trustcircle;

import ohos.aafwk.content.IntentParams;
import ohos.security.trustcircle.AuthParaGroup;

public interface ITrustCircleProxy {
    long activeAuth(IAuthListener iAuthListener, CancelSignal cancelSignal, AuthParaGroup.InitAuthInfo initAuthInfo);

    IntentParams getTcisInfo();

    long initKeyAgreement(IKaListener iKaListener, int i, long j, byte[] bArr, String str);

    long passiveAuth(IAuthListener iAuthListener, CancelSignal cancelSignal, AuthParaGroup.RecAuthInfo recAuthInfo);

    boolean receiveAuthInfo(int i, long j, Object obj);
}
