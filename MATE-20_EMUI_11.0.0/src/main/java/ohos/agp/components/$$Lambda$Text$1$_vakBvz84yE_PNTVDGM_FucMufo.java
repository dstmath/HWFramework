package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;
import ohos.agp.text.Font;

/* renamed from: ohos.agp.components.-$$Lambda$Text$1$_vakBvz84yE_PNTVDGM_FucMufo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Text$1$_vakBvz84yE_PNTVDGM_FucMufo implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Text$1$_vakBvz84yE_PNTVDGM_FucMufo INSTANCE = new $$Lambda$Text$1$_vakBvz84yE_PNTVDGM_FucMufo();

    private /* synthetic */ $$Lambda$Text$1$_vakBvz84yE_PNTVDGM_FucMufo() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        Text text;
        text.setFont(new Font.Builder("").setWeight(((Value) obj2).asInteger()).makeItalic(((Text) obj).getFont().isItalic()).build());
    }
}
