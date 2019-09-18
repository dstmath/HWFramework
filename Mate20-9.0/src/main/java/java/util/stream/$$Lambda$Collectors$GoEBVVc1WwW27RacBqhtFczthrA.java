package java.util.stream;

import java.util.LongSummaryStatistics;
import java.util.function.BinaryOperator;

/* renamed from: java.util.stream.-$$Lambda$Collectors$GoEBVVc1WwW27RacBqhtFczthrA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Collectors$GoEBVVc1WwW27RacBqhtFczthrA implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$Collectors$GoEBVVc1WwW27RacBqhtFczthrA INSTANCE = new $$Lambda$Collectors$GoEBVVc1WwW27RacBqhtFczthrA();

    private /* synthetic */ $$Lambda$Collectors$GoEBVVc1WwW27RacBqhtFczthrA() {
    }

    public final Object apply(Object obj, Object obj2) {
        return ((LongSummaryStatistics) obj).combine((LongSummaryStatistics) obj2);
    }
}
