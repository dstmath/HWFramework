package com.android.server.location;

import java.util.function.Consumer;

/* renamed from: com.android.server.location.-$$Lambda$ContextHubClientManager$aRAV9Gn84ao-4XOiN6tFizfZjHo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ContextHubClientManager$aRAV9Gn84ao4XOiN6tFizfZjHo implements Consumer {
    public static final /* synthetic */ $$Lambda$ContextHubClientManager$aRAV9Gn84ao4XOiN6tFizfZjHo INSTANCE = new $$Lambda$ContextHubClientManager$aRAV9Gn84ao4XOiN6tFizfZjHo();

    private /* synthetic */ $$Lambda$ContextHubClientManager$aRAV9Gn84ao4XOiN6tFizfZjHo() {
    }

    @Override // java.util.function.Consumer
    public final void accept(Object obj) {
        ((ContextHubClientBroker) obj).onHubReset();
    }
}
