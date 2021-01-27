package com.android.internal.infra;

import com.android.internal.infra.AbstractRemoteService;
import java.util.function.BiConsumer;

/* renamed from: com.android.internal.infra.-$$Lambda$AbstractRemoteService$6FcEKfZ-7TXLg6dcCU8EMuMNAy4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AbstractRemoteService$6FcEKfZ7TXLg6dcCU8EMuMNAy4 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$AbstractRemoteService$6FcEKfZ7TXLg6dcCU8EMuMNAy4 INSTANCE = new $$Lambda$AbstractRemoteService$6FcEKfZ7TXLg6dcCU8EMuMNAy4();

    private /* synthetic */ $$Lambda$AbstractRemoteService$6FcEKfZ7TXLg6dcCU8EMuMNAy4() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((AbstractRemoteService) obj).handleFinishRequest((AbstractRemoteService.BasePendingRequest) obj2);
    }
}
