package java.util.stream;

import java.util.OptionalLong;
import java.util.function.Predicate;

/* renamed from: java.util.stream.-$$Lambda$XcCQq8gYss3OrVBeBIbyvBZpOz8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$XcCQq8gYss3OrVBeBIbyvBZpOz8 implements Predicate {
    public static final /* synthetic */ $$Lambda$XcCQq8gYss3OrVBeBIbyvBZpOz8 INSTANCE = new $$Lambda$XcCQq8gYss3OrVBeBIbyvBZpOz8();

    private /* synthetic */ $$Lambda$XcCQq8gYss3OrVBeBIbyvBZpOz8() {
    }

    public final boolean test(Object obj) {
        return ((OptionalLong) obj).isPresent();
    }
}
