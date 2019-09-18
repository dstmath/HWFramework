package java.util.stream;

import java.util.IntSummaryStatistics;
import java.util.function.BinaryOperator;

/* renamed from: java.util.stream.-$$Lambda$Collectors$HtCSWMKsL2vCjP_AudE9j5Li4Q4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Collectors$HtCSWMKsL2vCjP_AudE9j5Li4Q4 implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$Collectors$HtCSWMKsL2vCjP_AudE9j5Li4Q4 INSTANCE = new $$Lambda$Collectors$HtCSWMKsL2vCjP_AudE9j5Li4Q4();

    private /* synthetic */ $$Lambda$Collectors$HtCSWMKsL2vCjP_AudE9j5Li4Q4() {
    }

    public final Object apply(Object obj, Object obj2) {
        return ((IntSummaryStatistics) obj).combine((IntSummaryStatistics) obj2);
    }
}
