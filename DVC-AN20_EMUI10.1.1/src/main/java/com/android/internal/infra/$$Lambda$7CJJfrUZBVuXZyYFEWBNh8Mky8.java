package com.android.internal.infra;

import com.android.internal.infra.AbstractRemoteService;
import java.util.function.BiConsumer;

/* renamed from: com.android.internal.infra.-$$Lambda$7-CJJfrUZBVuXZyYFEWBNh8Mky8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$7CJJfrUZBVuXZyYFEWBNh8Mky8 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$7CJJfrUZBVuXZyYFEWBNh8Mky8 INSTANCE = new $$Lambda$7CJJfrUZBVuXZyYFEWBNh8Mky8();

    private /* synthetic */ $$Lambda$7CJJfrUZBVuXZyYFEWBNh8Mky8() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((AbstractRemoteService) obj).handlePendingRequest((AbstractRemoteService.BasePendingRequest) obj2);
    }
}
