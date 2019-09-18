package java.util.stream;

import java.util.IntSummaryStatistics;
import java.util.function.ObjIntConsumer;

/* renamed from: java.util.stream.-$$Lambda$UowTf7vzuMsu4sv1-eMs5iEeNh0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$UowTf7vzuMsu4sv1eMs5iEeNh0 implements ObjIntConsumer {
    public static final /* synthetic */ $$Lambda$UowTf7vzuMsu4sv1eMs5iEeNh0 INSTANCE = new $$Lambda$UowTf7vzuMsu4sv1eMs5iEeNh0();

    private /* synthetic */ $$Lambda$UowTf7vzuMsu4sv1eMs5iEeNh0() {
    }

    public final void accept(Object obj, int i) {
        ((IntSummaryStatistics) obj).accept(i);
    }
}
