package ohos.account;

import ohos.account.AccountProxy;
import ohos.rpc.MessageParcel;

/* renamed from: ohos.account.-$$Lambda$AccountProxy$JWCW8n6tgP3XYtklZEi55NhhLVY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccountProxy$JWCW8n6tgP3XYtklZEi55NhhLVY implements AccountProxy.UnmarshallingInterface {
    public static final /* synthetic */ $$Lambda$AccountProxy$JWCW8n6tgP3XYtklZEi55NhhLVY INSTANCE = new $$Lambda$AccountProxy$JWCW8n6tgP3XYtklZEi55NhhLVY();

    private /* synthetic */ $$Lambda$AccountProxy$JWCW8n6tgP3XYtklZEi55NhhLVY() {
    }

    @Override // ohos.account.AccountProxy.UnmarshallingInterface
    public final boolean unmarshalling(MessageParcel messageParcel) {
        return AccountProxy.lambda$setOsAccountName$17(messageParcel);
    }
}
