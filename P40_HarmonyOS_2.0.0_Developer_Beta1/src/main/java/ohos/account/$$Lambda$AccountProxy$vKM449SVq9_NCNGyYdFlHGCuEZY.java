package ohos.account;

import ohos.account.AccountProxy;
import ohos.rpc.MessageParcel;

/* renamed from: ohos.account.-$$Lambda$AccountProxy$vKM449SVq9_NCNGyYdFlHGCuEZY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AccountProxy$vKM449SVq9_NCNGyYdFlHGCuEZY implements AccountProxy.UnmarshallingInterface {
    public static final /* synthetic */ $$Lambda$AccountProxy$vKM449SVq9_NCNGyYdFlHGCuEZY INSTANCE = new $$Lambda$AccountProxy$vKM449SVq9_NCNGyYdFlHGCuEZY();

    private /* synthetic */ $$Lambda$AccountProxy$vKM449SVq9_NCNGyYdFlHGCuEZY() {
    }

    @Override // ohos.account.AccountProxy.UnmarshallingInterface
    public final boolean unmarshalling(MessageParcel messageParcel) {
        return AccountProxy.lambda$removeOsAccount$7(messageParcel);
    }
}
