package ohos.aafwk.ability;

import java.util.function.BiFunction;
import ohos.agp.components.ComponentProvider;
import ohos.app.Context;

/* renamed from: ohos.aafwk.ability.-$$Lambda$w41QkgBP4f-V7giPP62RbMZpBqI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$w41QkgBP4fV7giPP62RbMZpBqI implements BiFunction {
    public static final /* synthetic */ $$Lambda$w41QkgBP4fV7giPP62RbMZpBqI INSTANCE = new $$Lambda$w41QkgBP4fV7giPP62RbMZpBqI();

    private /* synthetic */ $$Lambda$w41QkgBP4fV7giPP62RbMZpBqI() {
    }

    @Override // java.util.function.BiFunction
    public final Object apply(Object obj, Object obj2) {
        return new ComponentProvider(((Integer) obj).intValue(), (Context) obj2);
    }
}
