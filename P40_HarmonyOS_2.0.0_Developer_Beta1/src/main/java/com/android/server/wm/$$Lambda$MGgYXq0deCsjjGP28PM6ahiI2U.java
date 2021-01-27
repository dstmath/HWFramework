package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$MGgYXq0deCsjjGP-28PM6ahiI2U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$MGgYXq0deCsjjGP28PM6ahiI2U implements Consumer {
    public static final /* synthetic */ $$Lambda$MGgYXq0deCsjjGP28PM6ahiI2U INSTANCE = new $$Lambda$MGgYXq0deCsjjGP28PM6ahiI2U();

    private /* synthetic */ $$Lambda$MGgYXq0deCsjjGP28PM6ahiI2U() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((WindowProcessListener) obj).appDied();
    }
}
