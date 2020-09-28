package com.android.internal.infra;

import java.util.function.Consumer;

/* renamed from: com.android.internal.infra.-$$Lambda$AbstractRemoteService$YSUzqqi1Pbrg2dlwMGMtKWbGXck  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AbstractRemoteService$YSUzqqi1Pbrg2dlwMGMtKWbGXck implements Consumer {
    public static final /* synthetic */ $$Lambda$AbstractRemoteService$YSUzqqi1Pbrg2dlwMGMtKWbGXck INSTANCE = new $$Lambda$AbstractRemoteService$YSUzqqi1Pbrg2dlwMGMtKWbGXck();

    private /* synthetic */ $$Lambda$AbstractRemoteService$YSUzqqi1Pbrg2dlwMGMtKWbGXck() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((AbstractRemoteService) obj).handleEnsureBound();
    }
}
