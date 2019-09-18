package java.util.stream;

import java.util.IntSummaryStatistics;
import java.util.function.BiConsumer;

/* renamed from: java.util.stream.-$$Lambda$YcgMAuDDScc4HC6CSMDq1R0qa40  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$YcgMAuDDScc4HC6CSMDq1R0qa40 implements BiConsumer {
    public static final /* synthetic */ $$Lambda$YcgMAuDDScc4HC6CSMDq1R0qa40 INSTANCE = new $$Lambda$YcgMAuDDScc4HC6CSMDq1R0qa40();

    private /* synthetic */ $$Lambda$YcgMAuDDScc4HC6CSMDq1R0qa40() {
    }

    public final void accept(Object obj, Object obj2) {
        ((IntSummaryStatistics) obj).combine((IntSummaryStatistics) obj2);
    }
}
