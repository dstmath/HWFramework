package com.android.server.wm;

import java.util.Locale;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$ActivityTaskManagerService$U6g1UdnOPnEF9wX1OTm9nKVXY5k  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityTaskManagerService$U6g1UdnOPnEF9wX1OTm9nKVXY5k implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ActivityTaskManagerService$U6g1UdnOPnEF9wX1OTm9nKVXY5k INSTANCE = new $$Lambda$ActivityTaskManagerService$U6g1UdnOPnEF9wX1OTm9nKVXY5k();

    private /* synthetic */ $$Lambda$ActivityTaskManagerService$U6g1UdnOPnEF9wX1OTm9nKVXY5k() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ActivityTaskManagerService) obj).sendLocaleToMountDaemonMsg((Locale) obj2);
    }
}
