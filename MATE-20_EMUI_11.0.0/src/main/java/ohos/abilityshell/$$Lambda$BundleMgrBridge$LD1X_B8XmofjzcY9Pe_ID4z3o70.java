package ohos.abilityshell;

import ohos.rpc.IRemoteObject;

/* renamed from: ohos.abilityshell.-$$Lambda$BundleMgrBridge$LD1X_B8XmofjzcY9Pe_ID4z3o70  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BundleMgrBridge$LD1X_B8XmofjzcY9Pe_ID4z3o70 implements IRemoteObject.DeathRecipient {
    public static final /* synthetic */ $$Lambda$BundleMgrBridge$LD1X_B8XmofjzcY9Pe_ID4z3o70 INSTANCE = new $$Lambda$BundleMgrBridge$LD1X_B8XmofjzcY9Pe_ID4z3o70();

    private /* synthetic */ $$Lambda$BundleMgrBridge$LD1X_B8XmofjzcY9Pe_ID4z3o70() {
    }

    public final void onRemoteDied() {
        BundleMgrBridge.lambda$initBundleMgrProxy$0();
    }
}
