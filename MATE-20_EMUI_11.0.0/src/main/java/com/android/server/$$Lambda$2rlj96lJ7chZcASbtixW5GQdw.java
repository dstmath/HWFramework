package com.android.server;

import com.android.server.SensorPrivacyService;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.-$$Lambda$2rlj96lJ7chZc-A-SbtixW5GQdw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$2rlj96lJ7chZcASbtixW5GQdw implements BiConsumer {
    public static final /* synthetic */ $$Lambda$2rlj96lJ7chZcASbtixW5GQdw INSTANCE = new $$Lambda$2rlj96lJ7chZcASbtixW5GQdw();

    private /* synthetic */ $$Lambda$2rlj96lJ7chZcASbtixW5GQdw() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((SensorPrivacyService.SensorPrivacyHandler) obj).handleSensorPrivacyChanged(((Boolean) obj2).booleanValue());
    }
}
