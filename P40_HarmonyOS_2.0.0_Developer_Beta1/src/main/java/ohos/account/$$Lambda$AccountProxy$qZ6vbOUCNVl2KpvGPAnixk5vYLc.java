package ohos.account;

import ohos.account.AccountProxy;
import ohos.rpc.MessageParcel;

/* renamed from: ohos.account.-$$Lambda$AccountProxy$qZ6vbOUCNVl2KpvGPAnixk5vYLc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccountProxy$qZ6vbOUCNVl2KpvGPAnixk5vYLc implements AccountProxy.UnmarshallingInterface {
    public static final /* synthetic */ $$Lambda$AccountProxy$qZ6vbOUCNVl2KpvGPAnixk5vYLc INSTANCE = new $$Lambda$AccountProxy$qZ6vbOUCNVl2KpvGPAnixk5vYLc();

    private /* synthetic */ $$Lambda$AccountProxy$qZ6vbOUCNVl2KpvGPAnixk5vYLc() {
    }

    @Override // ohos.account.AccountProxy.UnmarshallingInterface
    public final boolean unmarshalling(MessageParcel messageParcel) {
        return AccountProxy.lambda$setOsAccountConstraints$19(messageParcel);
    }
}
