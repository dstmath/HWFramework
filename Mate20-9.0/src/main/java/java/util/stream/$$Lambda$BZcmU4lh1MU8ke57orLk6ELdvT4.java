package java.util.stream;

import java.util.DoubleSummaryStatistics;
import java.util.function.BiConsumer;

/* renamed from: java.util.stream.-$$Lambda$BZcmU4lh1MU8ke57orLk6ELdvT4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$BZcmU4lh1MU8ke57orLk6ELdvT4 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$BZcmU4lh1MU8ke57orLk6ELdvT4 INSTANCE = new $$Lambda$BZcmU4lh1MU8ke57orLk6ELdvT4();

    private /* synthetic */ $$Lambda$BZcmU4lh1MU8ke57orLk6ELdvT4() {
    }

    public final void accept(Object obj, Object obj2) {
        ((DoubleSummaryStatistics) obj).combine((DoubleSummaryStatistics) obj2);
    }
}
