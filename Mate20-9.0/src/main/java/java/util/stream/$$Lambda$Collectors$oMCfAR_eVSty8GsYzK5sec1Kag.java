package java.util.stream;

import java.util.DoubleSummaryStatistics;
import java.util.function.BinaryOperator;

/* renamed from: java.util.stream.-$$Lambda$Collectors$oMCfAR-_eVSty8GsYzK5sec1Kag  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Collectors$oMCfAR_eVSty8GsYzK5sec1Kag implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$Collectors$oMCfAR_eVSty8GsYzK5sec1Kag INSTANCE = new $$Lambda$Collectors$oMCfAR_eVSty8GsYzK5sec1Kag();

    private /* synthetic */ $$Lambda$Collectors$oMCfAR_eVSty8GsYzK5sec1Kag() {
    }

    public final Object apply(Object obj, Object obj2) {
        return ((DoubleSummaryStatistics) obj).combine((DoubleSummaryStatistics) obj2);
    }
}
