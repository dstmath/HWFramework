package com.android.server.pm;

import java.util.function.Consumer;

/* renamed from: com.android.server.pm.-$$Lambda$sAnQjWlQDJoJcSwHDDCKcU2fneU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$sAnQjWlQDJoJcSwHDDCKcU2fneU implements Consumer {
    public static final /* synthetic */ $$Lambda$sAnQjWlQDJoJcSwHDDCKcU2fneU INSTANCE = new $$Lambda$sAnQjWlQDJoJcSwHDDCKcU2fneU();

    private /* synthetic */ $$Lambda$sAnQjWlQDJoJcSwHDDCKcU2fneU() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ShortcutPackageItem) obj).verifyStates();
    }
}
