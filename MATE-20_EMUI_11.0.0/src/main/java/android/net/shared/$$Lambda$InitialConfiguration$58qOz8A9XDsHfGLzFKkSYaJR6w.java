package android.net.shared;

import android.net.LinkAddress;
import java.util.function.Predicate;

/* renamed from: android.net.shared.-$$Lambda$InitialConfiguration$58qOz8A9XDsHfGLzFKkSY-aJR6w  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$InitialConfiguration$58qOz8A9XDsHfGLzFKkSYaJR6w implements Predicate {
    public static final /* synthetic */ $$Lambda$InitialConfiguration$58qOz8A9XDsHfGLzFKkSYaJR6w INSTANCE = new $$Lambda$InitialConfiguration$58qOz8A9XDsHfGLzFKkSYaJR6w();

    private /* synthetic */ $$Lambda$InitialConfiguration$58qOz8A9XDsHfGLzFKkSYaJR6w() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return InitialConfiguration.isIPv4((LinkAddress) obj);
    }
}
