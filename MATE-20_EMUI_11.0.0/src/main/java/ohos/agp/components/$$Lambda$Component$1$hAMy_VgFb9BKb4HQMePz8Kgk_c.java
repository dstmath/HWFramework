package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;

/* renamed from: ohos.agp.components.-$$Lambda$Component$1$hAMy_VgFb9BKb4-HQMePz8Kgk_c  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Component$1$hAMy_VgFb9BKb4HQMePz8Kgk_c implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Component$1$hAMy_VgFb9BKb4HQMePz8Kgk_c INSTANCE = new $$Lambda$Component$1$hAMy_VgFb9BKb4HQMePz8Kgk_c();

    private /* synthetic */ $$Lambda$Component$1$hAMy_VgFb9BKb4HQMePz8Kgk_c() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((Component) obj).setWidth(((Value) obj2).asInteger());
    }
}
