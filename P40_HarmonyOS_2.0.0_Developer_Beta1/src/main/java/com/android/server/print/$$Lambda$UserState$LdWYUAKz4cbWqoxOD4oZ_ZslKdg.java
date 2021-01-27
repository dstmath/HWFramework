package com.android.server.print;

import java.util.function.Consumer;

/* renamed from: com.android.server.print.-$$Lambda$UserState$LdWYUAKz4cbWqoxOD4oZ_ZslKdg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UserState$LdWYUAKz4cbWqoxOD4oZ_ZslKdg implements Consumer {
    public static final /* synthetic */ $$Lambda$UserState$LdWYUAKz4cbWqoxOD4oZ_ZslKdg INSTANCE = new $$Lambda$UserState$LdWYUAKz4cbWqoxOD4oZ_ZslKdg();

    private /* synthetic */ $$Lambda$UserState$LdWYUAKz4cbWqoxOD4oZ_ZslKdg() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((UserState) obj).handleDispatchPrintServicesChanged();
    }
}
