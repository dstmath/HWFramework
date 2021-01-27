package ohos.agp.components;

import java.util.function.BiConsumer;
import ohos.agp.styles.Value;

/* renamed from: ohos.agp.components.-$$Lambda$Text$1$QDCHcoNhk0Jh1D2gk-jG6mRm7ZQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Text$1$QDCHcoNhk0Jh1D2gkjG6mRm7ZQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$Text$1$QDCHcoNhk0Jh1D2gkjG6mRm7ZQ INSTANCE = new $$Lambda$Text$1$QDCHcoNhk0Jh1D2gkjG6mRm7ZQ();

    private /* synthetic */ $$Lambda$Text$1$QDCHcoNhk0Jh1D2gkjG6mRm7ZQ() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((Text) obj).setAutoScrollingDuration((long) ((Value) obj2).asInteger());
    }
}
