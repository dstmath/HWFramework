package com.android.ims;

import com.android.ims.ImsManager;

/* renamed from: com.android.ims.-$$Lambda$ImsManager$Flxe43OUFnnU0pgnksvwPE6o3Mk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImsManager$Flxe43OUFnnU0pgnksvwPE6o3Mk implements ImsManager.ExecutorFactory {
    public static final /* synthetic */ $$Lambda$ImsManager$Flxe43OUFnnU0pgnksvwPE6o3Mk INSTANCE = new $$Lambda$ImsManager$Flxe43OUFnnU0pgnksvwPE6o3Mk();

    private /* synthetic */ $$Lambda$ImsManager$Flxe43OUFnnU0pgnksvwPE6o3Mk() {
    }

    @Override // com.android.ims.ImsManager.ExecutorFactory
    public final void executeRunnable(Runnable runnable) {
        new Thread(runnable).start();
    }
}
