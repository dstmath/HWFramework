package com.android.server.print;

import java.util.function.Consumer;

/* renamed from: com.android.server.print.-$$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg implements Consumer {
    public static final /* synthetic */ $$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg INSTANCE = new $$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg();

    private /* synthetic */ $$Lambda$RemotePrintService$tI07K2u4Z5L72sd1hvSEunGclrg() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((RemotePrintService) obj).handleDestroy();
    }
}
