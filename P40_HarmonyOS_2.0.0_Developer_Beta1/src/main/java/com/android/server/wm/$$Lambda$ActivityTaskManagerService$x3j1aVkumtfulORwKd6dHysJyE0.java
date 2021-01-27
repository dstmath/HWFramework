package com.android.server.wm;

import android.content.Intent;
import com.android.internal.util.function.QuadConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$ActivityTaskManagerService$x3j1aVkumtfulORwKd6dHysJyE0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityTaskManagerService$x3j1aVkumtfulORwKd6dHysJyE0 implements QuadConsumer {
    public static final /* synthetic */ $$Lambda$ActivityTaskManagerService$x3j1aVkumtfulORwKd6dHysJyE0 INSTANCE = new $$Lambda$ActivityTaskManagerService$x3j1aVkumtfulORwKd6dHysJyE0();

    private /* synthetic */ $$Lambda$ActivityTaskManagerService$x3j1aVkumtfulORwKd6dHysJyE0() {
    }

    public final void accept(Object obj, Object obj2, Object obj3, Object obj4) {
        ((ActivityTaskManagerService) obj).postHeavyWeightProcessNotification((WindowProcessController) obj2, (Intent) obj3, ((Integer) obj4).intValue());
    }
}
