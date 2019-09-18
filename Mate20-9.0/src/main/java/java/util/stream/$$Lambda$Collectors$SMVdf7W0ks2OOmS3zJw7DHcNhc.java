package java.util.stream;

import java.util.Set;
import java.util.function.BinaryOperator;

/* renamed from: java.util.stream.-$$Lambda$Collectors$SMVdf7W0ks2OOmS3zJw7DHc-Nhc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$Collectors$SMVdf7W0ks2OOmS3zJw7DHcNhc implements BinaryOperator {
    public static final /* synthetic */ $$Lambda$Collectors$SMVdf7W0ks2OOmS3zJw7DHcNhc INSTANCE = new $$Lambda$Collectors$SMVdf7W0ks2OOmS3zJw7DHcNhc();

    private /* synthetic */ $$Lambda$Collectors$SMVdf7W0ks2OOmS3zJw7DHcNhc() {
    }

    public final Object apply(Object obj, Object obj2) {
        return ((Set) obj).addAll((Set) obj2);
    }
}
