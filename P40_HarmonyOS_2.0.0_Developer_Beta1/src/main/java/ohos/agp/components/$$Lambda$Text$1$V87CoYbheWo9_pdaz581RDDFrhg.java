package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;

/* renamed from: ohos.agp.components.-$$Lambda$Text$1$V87CoYbheWo9_pdaz581RDDFrhg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Text$1$V87CoYbheWo9_pdaz581RDDFrhg implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Text$1$V87CoYbheWo9_pdaz581RDDFrhg INSTANCE = new $$Lambda$Text$1$V87CoYbheWo9_pdaz581RDDFrhg();

    private /* synthetic */ $$Lambda$Text$1$V87CoYbheWo9_pdaz581RDDFrhg() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((Text) obj).setInputMethodOption(((Value) obj2).asInteger());
    }
}
