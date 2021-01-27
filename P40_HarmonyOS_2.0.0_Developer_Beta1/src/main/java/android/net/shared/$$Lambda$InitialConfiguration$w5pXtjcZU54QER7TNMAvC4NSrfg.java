package android.net.shared;

import android.net.LinkAddress;
import java.util.function.Predicate;

/* renamed from: android.net.shared.-$$Lambda$InitialConfiguration$w5pXtjcZU54QER7TNMAvC4NSrfg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$InitialConfiguration$w5pXtjcZU54QER7TNMAvC4NSrfg implements Predicate {
    public static final /* synthetic */ $$Lambda$InitialConfiguration$w5pXtjcZU54QER7TNMAvC4NSrfg INSTANCE = new $$Lambda$InitialConfiguration$w5pXtjcZU54QER7TNMAvC4NSrfg();

    private /* synthetic */ $$Lambda$InitialConfiguration$w5pXtjcZU54QER7TNMAvC4NSrfg() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return InitialConfiguration.isIPv6GUA((LinkAddress) obj);
    }
}
