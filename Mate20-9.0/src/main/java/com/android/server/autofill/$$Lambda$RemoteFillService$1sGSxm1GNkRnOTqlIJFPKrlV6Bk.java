package com.android.server.autofill;

import java.util.function.Consumer;

/* renamed from: com.android.server.autofill.-$$Lambda$RemoteFillService$1sGSxm1GNkRnOTqlIJFPKrlV6Bk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemoteFillService$1sGSxm1GNkRnOTqlIJFPKrlV6Bk implements Consumer {
    public static final /* synthetic */ $$Lambda$RemoteFillService$1sGSxm1GNkRnOTqlIJFPKrlV6Bk INSTANCE = new $$Lambda$RemoteFillService$1sGSxm1GNkRnOTqlIJFPKrlV6Bk();

    private /* synthetic */ $$Lambda$RemoteFillService$1sGSxm1GNkRnOTqlIJFPKrlV6Bk() {
    }

    public final void accept(Object obj) {
        ((RemoteFillService) obj).handleBinderDied();
    }
}
