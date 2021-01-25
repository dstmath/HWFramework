package android.net.shared;

import java.net.InetAddress;
import java.util.function.Function;

/* renamed from: android.net.shared.-$$Lambda$OsobWheG5dMvEj_cOJtueqUBqBI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$OsobWheG5dMvEj_cOJtueqUBqBI implements Function {
    public static final /* synthetic */ $$Lambda$OsobWheG5dMvEj_cOJtueqUBqBI INSTANCE = new $$Lambda$OsobWheG5dMvEj_cOJtueqUBqBI();

    private /* synthetic */ $$Lambda$OsobWheG5dMvEj_cOJtueqUBqBI() {
    }

    @Override // java.util.function.Function
    public final Object apply(Object obj) {
        return IpConfigurationParcelableUtil.parcelAddress((InetAddress) obj);
    }
}
