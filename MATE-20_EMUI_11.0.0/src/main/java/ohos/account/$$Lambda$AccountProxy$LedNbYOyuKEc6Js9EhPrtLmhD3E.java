package ohos.account;

import ohos.account.AccountProxy;
import ohos.rpc.MessageParcel;

/* renamed from: ohos.account.-$$Lambda$AccountProxy$LedNbYOyuKEc6Js9EhPrtLmhD3E  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccountProxy$LedNbYOyuKEc6Js9EhPrtLmhD3E implements AccountProxy.UnmarshallingInterface {
    public static final /* synthetic */ $$Lambda$AccountProxy$LedNbYOyuKEc6Js9EhPrtLmhD3E INSTANCE = new $$Lambda$AccountProxy$LedNbYOyuKEc6Js9EhPrtLmhD3E();

    private /* synthetic */ $$Lambda$AccountProxy$LedNbYOyuKEc6Js9EhPrtLmhD3E() {
    }

    @Override // ohos.account.AccountProxy.UnmarshallingInterface
    public final boolean unmarshalling(MessageParcel messageParcel) {
        return AccountProxy.lambda$activateOsAccount$14(messageParcel);
    }
}
