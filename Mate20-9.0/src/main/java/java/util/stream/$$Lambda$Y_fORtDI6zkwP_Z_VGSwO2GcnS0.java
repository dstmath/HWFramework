package java.util.stream;

import java.util.LongSummaryStatistics;
import java.util.function.ObjLongConsumer;

/* renamed from: java.util.stream.-$$Lambda$Y_fORtDI6zkwP_Z_VGSwO2GcnS0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Y_fORtDI6zkwP_Z_VGSwO2GcnS0 implements ObjLongConsumer {
    public static final /* synthetic */ $$Lambda$Y_fORtDI6zkwP_Z_VGSwO2GcnS0 INSTANCE = new $$Lambda$Y_fORtDI6zkwP_Z_VGSwO2GcnS0();

    private /* synthetic */ $$Lambda$Y_fORtDI6zkwP_Z_VGSwO2GcnS0() {
    }

    public final void accept(Object obj, long j) {
        ((LongSummaryStatistics) obj).accept(j);
    }
}
