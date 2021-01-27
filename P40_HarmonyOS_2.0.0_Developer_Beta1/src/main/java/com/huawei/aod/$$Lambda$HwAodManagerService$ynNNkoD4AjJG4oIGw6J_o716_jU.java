package com.huawei.aod;

import huawei.android.aod.Layer;
import java.util.function.ToIntFunction;

/* renamed from: com.huawei.aod.-$$Lambda$HwAodManagerService$ynNNkoD4AjJG4oIGw6J_o716_jU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwAodManagerService$ynNNkoD4AjJG4oIGw6J_o716_jU implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$HwAodManagerService$ynNNkoD4AjJG4oIGw6J_o716_jU INSTANCE = new $$Lambda$HwAodManagerService$ynNNkoD4AjJG4oIGw6J_o716_jU();

    private /* synthetic */ $$Lambda$HwAodManagerService$ynNNkoD4AjJG4oIGw6J_o716_jU() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((Layer) obj).getActionList().stream().mapToInt($$Lambda$HwAodManagerService$Uimosp8Q6YSubuYCIihHmH7gb4.INSTANCE).sum();
    }
}
