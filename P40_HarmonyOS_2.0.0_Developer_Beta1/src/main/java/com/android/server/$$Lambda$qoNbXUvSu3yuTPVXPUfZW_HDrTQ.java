package com.android.server;

import android.location.IGnssMeasurementsListener;
import android.os.IBinder;
import java.util.function.Function;

/* renamed from: com.android.server.-$$Lambda$qoNbXUvSu3yuTPVXPUfZW_HDrTQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$qoNbXUvSu3yuTPVXPUfZW_HDrTQ implements Function {
    public static final /* synthetic */ $$Lambda$qoNbXUvSu3yuTPVXPUfZW_HDrTQ INSTANCE = new $$Lambda$qoNbXUvSu3yuTPVXPUfZW_HDrTQ();

    private /* synthetic */ $$Lambda$qoNbXUvSu3yuTPVXPUfZW_HDrTQ() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return IGnssMeasurementsListener.Stub.asInterface((IBinder) obj);
    }
}
