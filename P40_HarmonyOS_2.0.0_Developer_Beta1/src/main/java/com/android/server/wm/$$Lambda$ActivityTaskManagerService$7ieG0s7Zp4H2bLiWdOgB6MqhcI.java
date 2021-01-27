package com.android.server.wm;

import android.os.IBinder;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$ActivityTaskManagerService$7ieG0s-7Zp4H2bLiWdOgB6MqhcI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityTaskManagerService$7ieG0s7Zp4H2bLiWdOgB6MqhcI implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ActivityTaskManagerService$7ieG0s7Zp4H2bLiWdOgB6MqhcI INSTANCE = new $$Lambda$ActivityTaskManagerService$7ieG0s7Zp4H2bLiWdOgB6MqhcI();

    private /* synthetic */ $$Lambda$ActivityTaskManagerService$7ieG0s7Zp4H2bLiWdOgB6MqhcI() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ActivityTaskManagerService) obj).forgetStartAsCallerTokenMsg((IBinder) obj2);
    }
}
