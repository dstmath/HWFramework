package ohos.abilityshell;

import ohos.rpc.IRemoteObject;

/* renamed from: ohos.abilityshell.-$$Lambda$DistributedImpl$vlDSkDffBRWpIc3r4l25p6Ol05w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DistributedImpl$vlDSkDffBRWpIc3r4l25p6Ol05w implements IRemoteObject.DeathRecipient {
    public static final /* synthetic */ $$Lambda$DistributedImpl$vlDSkDffBRWpIc3r4l25p6Ol05w INSTANCE = new $$Lambda$DistributedImpl$vlDSkDffBRWpIc3r4l25p6Ol05w();

    private /* synthetic */ $$Lambda$DistributedImpl$vlDSkDffBRWpIc3r4l25p6Ol05w() {
    }

    public final void onRemoteDied() {
        DistributedImpl.lambda$initDistributedServiceProxy$0();
    }
}
