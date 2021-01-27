package com.android.server;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.-$$Lambda$PinnerService$3$3Ta6TX4Jq9YbpUYE5Y0r8Xt8rBw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PinnerService$3$3Ta6TX4Jq9YbpUYE5Y0r8Xt8rBw implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PinnerService$3$3Ta6TX4Jq9YbpUYE5Y0r8Xt8rBw INSTANCE = new $$Lambda$PinnerService$3$3Ta6TX4Jq9YbpUYE5Y0r8Xt8rBw();

    private /* synthetic */ $$Lambda$PinnerService$3$3Ta6TX4Jq9YbpUYE5Y0r8Xt8rBw() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((PinnerService) obj).handleUidActive(((Integer) obj2).intValue());
    }
}
