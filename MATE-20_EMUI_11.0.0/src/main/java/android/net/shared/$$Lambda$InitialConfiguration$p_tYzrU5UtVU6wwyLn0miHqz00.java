package android.net.shared;

import android.net.IpPrefix;
import java.util.function.Predicate;

/* renamed from: android.net.shared.-$$Lambda$InitialConfiguration$p_tYzrU5UtVU6wwyLn0miHqz-00  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$InitialConfiguration$p_tYzrU5UtVU6wwyLn0miHqz00 implements Predicate {
    public static final /* synthetic */ $$Lambda$InitialConfiguration$p_tYzrU5UtVU6wwyLn0miHqz00 INSTANCE = new $$Lambda$InitialConfiguration$p_tYzrU5UtVU6wwyLn0miHqz00();

    private /* synthetic */ $$Lambda$InitialConfiguration$p_tYzrU5UtVU6wwyLn0miHqz00() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return InitialConfiguration.isIPv6DefaultRoute((IpPrefix) obj);
    }
}
