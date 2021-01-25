package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;
import ohos.agp.text.Font;

/* renamed from: ohos.agp.components.-$$Lambda$Text$1$2ZQr5R6a5vEJlNdnvcokaJcEA-0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Text$1$2ZQr5R6a5vEJlNdnvcokaJcEA0 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Text$1$2ZQr5R6a5vEJlNdnvcokaJcEA0 INSTANCE = new $$Lambda$Text$1$2ZQr5R6a5vEJlNdnvcokaJcEA0();

    private /* synthetic */ $$Lambda$Text$1$2ZQr5R6a5vEJlNdnvcokaJcEA0() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        Text text;
        text.setFont(new Font.Builder(((Value) obj2).asString()).setWeight(text.getFont().getWeight()).makeItalic(((Text) obj).getFont().isItalic()).build());
    }
}
