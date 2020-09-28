package com.android.internal.infra;

import com.android.internal.infra.AbstractRemoteService;
import java.util.function.BiConsumer;

/* renamed from: com.android.internal.infra.-$$Lambda$EbzSql2RHkXox5Myj8A-7kLC4_A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EbzSql2RHkXox5Myj8A7kLC4_A implements BiConsumer {
    public static final /* synthetic */ $$Lambda$EbzSql2RHkXox5Myj8A7kLC4_A INSTANCE = new $$Lambda$EbzSql2RHkXox5Myj8A7kLC4_A();

    private /* synthetic */ $$Lambda$EbzSql2RHkXox5Myj8A7kLC4_A() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((AbstractRemoteService) obj).handlePendingRequest((AbstractRemoteService.MyAsyncPendingRequest) obj2);
    }
}
