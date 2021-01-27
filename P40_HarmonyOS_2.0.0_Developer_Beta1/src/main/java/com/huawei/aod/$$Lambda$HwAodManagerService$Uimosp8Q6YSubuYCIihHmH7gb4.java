package com.huawei.aod;

import huawei.android.aod.Action;
import java.util.function.ToIntFunction;

/* renamed from: com.huawei.aod.-$$Lambda$HwAodManagerService$Uimosp8Q6YSubuYCIihHmH7-gb4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwAodManagerService$Uimosp8Q6YSubuYCIihHmH7gb4 implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$HwAodManagerService$Uimosp8Q6YSubuYCIihHmH7gb4 INSTANCE = new $$Lambda$HwAodManagerService$Uimosp8Q6YSubuYCIihHmH7gb4();

    private /* synthetic */ $$Lambda$HwAodManagerService$Uimosp8Q6YSubuYCIihHmH7gb4() {
    }

    @Override // java.util.function.ToIntFunction
    public final int applyAsInt(Object obj) {
        return ((Action) obj).getActionSize();
    }
}
