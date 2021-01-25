package ohos.security.trustcircle;

import ohos.security.trustcircle.AuthParaGroup;

public interface IAuthListener {
    void onAuthAck(long j, AuthParaGroup.OnAuthAckInfo onAuthAckInfo);

    void onAuthAckError(long j, int i);

    void onAuthError(long j, int i);

    void onAuthExited(long j, int i);

    void onAuthSync(long j, AuthParaGroup.OnAuthSyncInfo onAuthSyncInfo);

    void onAuthSyncAck(long j, AuthParaGroup.OnAuthSyncAckInfo onAuthSyncAckInfo);

    void onAuthSyncAckError(long j, int i);

    void requestPublicKey();

    void responsePublicKey(long j, AuthParaGroup.RespPkInfo respPkInfo);
}
