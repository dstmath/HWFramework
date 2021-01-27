package com.huawei.aod;

import huawei.android.aod.Trigger;
import java.util.function.ToIntFunction;

/* renamed from: com.huawei.aod.-$$Lambda$HwAodManagerService$_aMtMyQHkc0rAVlyltAHTQQlklA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwAodManagerService$_aMtMyQHkc0rAVlyltAHTQQlklA implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$HwAodManagerService$_aMtMyQHkc0rAVlyltAHTQQlklA INSTANCE = new $$Lambda$HwAodManagerService$_aMtMyQHkc0rAVlyltAHTQQlklA();

    private /* synthetic */ $$Lambda$HwAodManagerService$_aMtMyQHkc0rAVlyltAHTQQlklA() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((Trigger) obj).getLayerList().stream().mapToInt($$Lambda$HwAodManagerService$ynNNkoD4AjJG4oIGw6J_o716_jU.INSTANCE).sum();
    }
}
