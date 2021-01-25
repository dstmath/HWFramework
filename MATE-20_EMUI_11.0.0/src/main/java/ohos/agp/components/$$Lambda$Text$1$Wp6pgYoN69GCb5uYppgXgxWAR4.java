package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;
import ohos.agp.text.Font;

/* renamed from: ohos.agp.components.-$$Lambda$Text$1$Wp6pgY-oN69GCb5uYppgXgxWAR4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Text$1$Wp6pgYoN69GCb5uYppgXgxWAR4 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Text$1$Wp6pgYoN69GCb5uYppgXgxWAR4 INSTANCE = new $$Lambda$Text$1$Wp6pgYoN69GCb5uYppgXgxWAR4();

    private /* synthetic */ $$Lambda$Text$1$Wp6pgYoN69GCb5uYppgXgxWAR4() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        Text text;
        text.setFont(new Font.Builder("").setWeight(((Text) obj).getFont().getWeight()).makeItalic(((Value) obj2).asBool()).build());
    }
}
