package java.util.stream;

import java.util.OptionalDouble;
import java.util.function.Predicate;

/* renamed from: java.util.stream.-$$Lambda$yrGzfUbU_IPNM4mz8V8FlMUHCw4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$yrGzfUbU_IPNM4mz8V8FlMUHCw4 implements Predicate {
    public static final /* synthetic */ $$Lambda$yrGzfUbU_IPNM4mz8V8FlMUHCw4 INSTANCE = new $$Lambda$yrGzfUbU_IPNM4mz8V8FlMUHCw4();

    private /* synthetic */ $$Lambda$yrGzfUbU_IPNM4mz8V8FlMUHCw4() {
    }

    public final boolean test(Object obj) {
        return ((OptionalDouble) obj).isPresent();
    }
}
