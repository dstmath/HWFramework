package java.util.stream;

import java.util.DoubleSummaryStatistics;
import java.util.function.ObjDoubleConsumer;

/* renamed from: java.util.stream.-$$Lambda$9-clh6DyAY2rGfAxuH1sO9aEBuU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$9clh6DyAY2rGfAxuH1sO9aEBuU implements ObjDoubleConsumer {
    public static final /* synthetic */ $$Lambda$9clh6DyAY2rGfAxuH1sO9aEBuU INSTANCE = new $$Lambda$9clh6DyAY2rGfAxuH1sO9aEBuU();

    private /* synthetic */ $$Lambda$9clh6DyAY2rGfAxuH1sO9aEBuU() {
    }

    public final void accept(Object obj, double d) {
        ((DoubleSummaryStatistics) obj).accept(d);
    }
}
