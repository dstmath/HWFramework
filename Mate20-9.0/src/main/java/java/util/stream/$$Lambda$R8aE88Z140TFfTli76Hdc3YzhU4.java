package java.util.stream;

import java.util.function.BinaryOperator;

/* renamed from: java.util.stream.-$$Lambda$R8aE88Z140TFfTli76Hdc3YzhU4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$R8aE88Z140TFfTli76Hdc3YzhU4 implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$R8aE88Z140TFfTli76Hdc3YzhU4 INSTANCE = new $$Lambda$R8aE88Z140TFfTli76Hdc3YzhU4();

    private /* synthetic */ $$Lambda$R8aE88Z140TFfTli76Hdc3YzhU4() {
    }

    public final Object apply(Object obj, Object obj2) {
        return Long.valueOf(Long.sum(((Long) obj).longValue(), ((Long) obj2).longValue()));
    }
}
