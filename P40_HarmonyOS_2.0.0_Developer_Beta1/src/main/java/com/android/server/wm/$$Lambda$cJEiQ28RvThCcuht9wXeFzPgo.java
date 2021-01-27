package com.android.server.wm;

import java.util.function.Consumer;

/* renamed from: com.android.server.wm.-$$Lambda$cJE-iQ28Rv-ThCcuht9wXeFzPgo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$cJEiQ28RvThCcuht9wXeFzPgo implements Consumer {
    public static final /* synthetic */ $$Lambda$cJEiQ28RvThCcuht9wXeFzPgo INSTANCE = new $$Lambda$cJEiQ28RvThCcuht9wXeFzPgo();

    private /* synthetic */ $$Lambda$cJEiQ28RvThCcuht9wXeFzPgo() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((DisplayPolicy) obj).systemReady();
    }
}
