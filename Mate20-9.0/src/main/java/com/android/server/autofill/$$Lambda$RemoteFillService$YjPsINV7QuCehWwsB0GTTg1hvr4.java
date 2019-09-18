package com.android.server.autofill;

import java.util.function.Consumer;

/* renamed from: com.android.server.autofill.-$$Lambda$RemoteFillService$YjPsINV7QuCehWwsB0GTTg1hvr4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemoteFillService$YjPsINV7QuCehWwsB0GTTg1hvr4 implements Consumer {
    public static final /* synthetic */ $$Lambda$RemoteFillService$YjPsINV7QuCehWwsB0GTTg1hvr4 INSTANCE = new $$Lambda$RemoteFillService$YjPsINV7QuCehWwsB0GTTg1hvr4();

    private /* synthetic */ $$Lambda$RemoteFillService$YjPsINV7QuCehWwsB0GTTg1hvr4() {
    }

    public final void accept(Object obj) {
        ((RemoteFillService) obj).handleUnbind();
    }
}
