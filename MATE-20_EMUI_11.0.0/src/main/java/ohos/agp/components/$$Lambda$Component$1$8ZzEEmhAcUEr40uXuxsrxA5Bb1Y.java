package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;

/* renamed from: ohos.agp.components.-$$Lambda$Component$1$8ZzEEmhAcUEr40uXuxsrxA5Bb1Y  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Component$1$8ZzEEmhAcUEr40uXuxsrxA5Bb1Y implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Component$1$8ZzEEmhAcUEr40uXuxsrxA5Bb1Y INSTANCE = new $$Lambda$Component$1$8ZzEEmhAcUEr40uXuxsrxA5Bb1Y();

    private /* synthetic */ $$Lambda$Component$1$8ZzEEmhAcUEr40uXuxsrxA5Bb1Y() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((Component) obj).setBackground(((Value) obj2).asElement());
    }
}
