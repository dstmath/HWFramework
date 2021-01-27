package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;

/* renamed from: ohos.agp.components.-$$Lambda$Component$1$NtlcPYlUC6ZxjDb934DFgdEqF98  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Component$1$NtlcPYlUC6ZxjDb934DFgdEqF98 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Component$1$NtlcPYlUC6ZxjDb934DFgdEqF98 INSTANCE = new $$Lambda$Component$1$NtlcPYlUC6ZxjDb934DFgdEqF98();

    private /* synthetic */ $$Lambda$Component$1$NtlcPYlUC6ZxjDb934DFgdEqF98() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((Component) obj).setHeight(((Value) obj2).asInteger());
    }
}
