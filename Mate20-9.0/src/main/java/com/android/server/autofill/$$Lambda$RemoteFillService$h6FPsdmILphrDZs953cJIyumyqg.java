package com.android.server.autofill;

import com.android.server.autofill.RemoteFillService;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.autofill.-$$Lambda$RemoteFillService$h6FPsdmILphrDZs953cJIyumyqg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$RemoteFillService$h6FPsdmILphrDZs953cJIyumyqg implements BiConsumer {
    public static final /* synthetic */ $$Lambda$RemoteFillService$h6FPsdmILphrDZs953cJIyumyqg INSTANCE = new $$Lambda$RemoteFillService$h6FPsdmILphrDZs953cJIyumyqg();

    private /* synthetic */ $$Lambda$RemoteFillService$h6FPsdmILphrDZs953cJIyumyqg() {
    }

    public final void accept(Object obj, Object obj2) {
        ((RemoteFillService) obj).handlePendingRequest((RemoteFillService.PendingRequest) obj2);
    }
}
