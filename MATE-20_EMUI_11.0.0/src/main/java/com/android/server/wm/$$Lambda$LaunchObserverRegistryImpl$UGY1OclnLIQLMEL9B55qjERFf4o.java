package com.android.server.wm;

import com.android.internal.util.function.TriConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$LaunchObserverRegistryImpl$UGY1OclnLIQLMEL9B55qjERFf4o  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$LaunchObserverRegistryImpl$UGY1OclnLIQLMEL9B55qjERFf4o implements TriConsumer {
    public static final /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$UGY1OclnLIQLMEL9B55qjERFf4o INSTANCE = new $$Lambda$LaunchObserverRegistryImpl$UGY1OclnLIQLMEL9B55qjERFf4o();

    private /* synthetic */ $$Lambda$LaunchObserverRegistryImpl$UGY1OclnLIQLMEL9B55qjERFf4o() {
    }

    public final void accept(Object obj, Object obj2, Object obj3) {
        ((LaunchObserverRegistryImpl) obj).handleOnActivityLaunched((byte[]) obj2, ((Integer) obj3).intValue());
    }
}
