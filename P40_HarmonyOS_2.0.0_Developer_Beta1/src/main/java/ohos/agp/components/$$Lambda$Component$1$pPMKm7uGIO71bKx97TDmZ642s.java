package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;

/* renamed from: ohos.agp.components.-$$Lambda$Component$1$pPMKm7uGIO71bK-x97TDmZ-642s  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Component$1$pPMKm7uGIO71bKx97TDmZ642s implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Component$1$pPMKm7uGIO71bKx97TDmZ642s INSTANCE = new $$Lambda$Component$1$pPMKm7uGIO71bKx97TDmZ642s();

    private /* synthetic */ $$Lambda$Component$1$pPMKm7uGIO71bKx97TDmZ642s() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((Component) obj).setForeground(((Value) obj2).asElement());
    }
}
