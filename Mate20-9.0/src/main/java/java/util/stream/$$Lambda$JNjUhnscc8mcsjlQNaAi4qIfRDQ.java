package java.util.stream;

import java.util.LongSummaryStatistics;
import java.util.function.BiConsumer;

/* renamed from: java.util.stream.-$$Lambda$JNjUhnscc8mcsjlQNaAi4qIfRDQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$JNjUhnscc8mcsjlQNaAi4qIfRDQ implements BiConsumer {
    public static final /* synthetic */ $$Lambda$JNjUhnscc8mcsjlQNaAi4qIfRDQ INSTANCE = new $$Lambda$JNjUhnscc8mcsjlQNaAi4qIfRDQ();

    private /* synthetic */ $$Lambda$JNjUhnscc8mcsjlQNaAi4qIfRDQ() {
    }

    public final void accept(Object obj, Object obj2) {
        ((LongSummaryStatistics) obj).combine((LongSummaryStatistics) obj2);
    }
}
