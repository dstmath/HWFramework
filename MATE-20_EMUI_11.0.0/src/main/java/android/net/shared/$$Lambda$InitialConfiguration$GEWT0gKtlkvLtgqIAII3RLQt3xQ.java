package android.net.shared;

import android.net.IpPrefix;
import java.util.function.Predicate;

/* renamed from: android.net.shared.-$$Lambda$InitialConfiguration$GEWT0gKtlkvLtgqIAII3RLQt3xQ  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$InitialConfiguration$GEWT0gKtlkvLtgqIAII3RLQt3xQ implements Predicate {
    public static final /* synthetic */ $$Lambda$InitialConfiguration$GEWT0gKtlkvLtgqIAII3RLQt3xQ INSTANCE = new $$Lambda$InitialConfiguration$GEWT0gKtlkvLtgqIAII3RLQt3xQ();

    private /* synthetic */ $$Lambda$InitialConfiguration$GEWT0gKtlkvLtgqIAII3RLQt3xQ() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return InitialConfiguration.isPrefixLengthCompliant((IpPrefix) obj);
    }
}
