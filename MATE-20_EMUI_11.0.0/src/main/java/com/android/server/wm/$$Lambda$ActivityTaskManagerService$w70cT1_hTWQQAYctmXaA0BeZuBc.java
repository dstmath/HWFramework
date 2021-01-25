package com.android.server.wm;

import java.util.function.BiConsumer;

/* renamed from: com.android.server.wm.-$$Lambda$ActivityTaskManagerService$w70cT1_hTWQQAYctmXaA0BeZuBc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityTaskManagerService$w70cT1_hTWQQAYctmXaA0BeZuBc implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ActivityTaskManagerService$w70cT1_hTWQQAYctmXaA0BeZuBc INSTANCE = new $$Lambda$ActivityTaskManagerService$w70cT1_hTWQQAYctmXaA0BeZuBc();

    private /* synthetic */ $$Lambda$ActivityTaskManagerService$w70cT1_hTWQQAYctmXaA0BeZuBc() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((ActivityTaskManagerService) obj).cancelHeavyWeightProcessNotification(((Integer) obj2).intValue());
    }
}
