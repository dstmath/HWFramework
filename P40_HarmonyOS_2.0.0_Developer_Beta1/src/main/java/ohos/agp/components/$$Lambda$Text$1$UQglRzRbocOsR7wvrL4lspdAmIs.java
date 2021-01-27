package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;

/* renamed from: ohos.agp.components.-$$Lambda$Text$1$UQglRzRbocOsR7wvrL4lspdAmIs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Text$1$UQglRzRbocOsR7wvrL4lspdAmIs implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Text$1$UQglRzRbocOsR7wvrL4lspdAmIs INSTANCE = new $$Lambda$Text$1$UQglRzRbocOsR7wvrL4lspdAmIs();

    private /* synthetic */ $$Lambda$Text$1$UQglRzRbocOsR7wvrL4lspdAmIs() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((Text) obj).setPaddingForText(((Value) obj2).asBool());
    }
}
