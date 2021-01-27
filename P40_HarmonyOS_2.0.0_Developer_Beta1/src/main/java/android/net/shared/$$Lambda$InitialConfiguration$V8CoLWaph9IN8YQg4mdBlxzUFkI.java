package android.net.shared;

import android.net.LinkAddress;
import java.util.function.Predicate;

/* renamed from: android.net.shared.-$$Lambda$InitialConfiguration$V8CoLWaph9IN8YQg4mdBlxzUFkI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$InitialConfiguration$V8CoLWaph9IN8YQg4mdBlxzUFkI implements Predicate {
    public static final /* synthetic */ $$Lambda$InitialConfiguration$V8CoLWaph9IN8YQg4mdBlxzUFkI INSTANCE = new $$Lambda$InitialConfiguration$V8CoLWaph9IN8YQg4mdBlxzUFkI();

    private /* synthetic */ $$Lambda$InitialConfiguration$V8CoLWaph9IN8YQg4mdBlxzUFkI() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return InitialConfiguration.isPrefixLengthCompliant((LinkAddress) obj);
    }
}
