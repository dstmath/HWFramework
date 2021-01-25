package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$99XNq73vh8e4HVH9BuxFhbLxKVY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$99XNq73vh8e4HVH9BuxFhbLxKVY implements BiConsumer {
    public static final /* synthetic */ $$Lambda$99XNq73vh8e4HVH9BuxFhbLxKVY INSTANCE = new $$Lambda$99XNq73vh8e4HVH9BuxFhbLxKVY();

    private /* synthetic */ $$Lambda$99XNq73vh8e4HVH9BuxFhbLxKVY() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((DisplayPolicy) obj).onPowerKeyDown(((Boolean) obj2).booleanValue());
    }
}
