package ohos.account;

import ohos.account.AccountProxy;
import ohos.rpc.MessageParcel;

/* renamed from: ohos.account.-$$Lambda$AccountProxy$5OwveoCxuY48I1Yap7rqtYJ1MsM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccountProxy$5OwveoCxuY48I1Yap7rqtYJ1MsM implements AccountProxy.UnmarshallingInterface {
    public static final /* synthetic */ $$Lambda$AccountProxy$5OwveoCxuY48I1Yap7rqtYJ1MsM INSTANCE = new $$Lambda$AccountProxy$5OwveoCxuY48I1Yap7rqtYJ1MsM();

    private /* synthetic */ $$Lambda$AccountProxy$5OwveoCxuY48I1Yap7rqtYJ1MsM() {
    }

    @Override // ohos.account.AccountProxy.UnmarshallingInterface
    public final boolean unmarshalling(MessageParcel messageParcel) {
        return AccountProxy.lambda$updateOsAccountDistributedInfo$3(messageParcel);
    }
}
