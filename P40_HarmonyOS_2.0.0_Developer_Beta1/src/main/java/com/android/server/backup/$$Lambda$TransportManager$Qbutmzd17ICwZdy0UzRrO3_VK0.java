package com.android.server.backup;

import android.content.ComponentName;
import java.util.function.Predicate;

/* renamed from: com.android.server.backup.-$$Lambda$TransportManager$Qbutmzd17ICwZdy0UzRrO-3_VK0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$TransportManager$Qbutmzd17ICwZdy0UzRrO3_VK0 implements Predicate {
    public static final /* synthetic */ $$Lambda$TransportManager$Qbutmzd17ICwZdy0UzRrO3_VK0 INSTANCE = new $$Lambda$TransportManager$Qbutmzd17ICwZdy0UzRrO3_VK0();

    private /* synthetic */ $$Lambda$TransportManager$Qbutmzd17ICwZdy0UzRrO3_VK0() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return TransportManager.lambda$registerTransports$2((ComponentName) obj);
    }
}
