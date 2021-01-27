package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;

/* renamed from: ohos.agp.components.-$$Lambda$Text$1$I2d86JY8q__xunm616avwihAzg8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Text$1$I2d86JY8q__xunm616avwihAzg8 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Text$1$I2d86JY8q__xunm616avwihAzg8 INSTANCE = new $$Lambda$Text$1$I2d86JY8q__xunm616avwihAzg8();

    private /* synthetic */ $$Lambda$Text$1$I2d86JY8q__xunm616avwihAzg8() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((Text) obj).setAutoScrollingCount(((Value) obj2).asInteger());
    }
}
