package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;
import ohos.agp.text.Font;

/* renamed from: ohos.agp.components.-$$Lambda$Text$1$6TVR3fzgOtMrcXg1FXtPA6Gpluw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Text$1$6TVR3fzgOtMrcXg1FXtPA6Gpluw implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Text$1$6TVR3fzgOtMrcXg1FXtPA6Gpluw INSTANCE = new $$Lambda$Text$1$6TVR3fzgOtMrcXg1FXtPA6Gpluw();

    private /* synthetic */ $$Lambda$Text$1$6TVR3fzgOtMrcXg1FXtPA6Gpluw() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        Text text;
        text.setFont(new Font.Builder(((Value) obj2).asString()).setWeight(text.getFont().getWeight()).makeItalic(((Text) obj).getFont().isItalic()).build());
    }
}
