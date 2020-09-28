package android.content.pm;

import android.content.pm.PackageInstaller;
import java.util.function.BiConsumer;

/* renamed from: android.content.pm.-$$Lambda$ciir_QAmv6RwJro4I58t77dPnxU  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ciir_QAmv6RwJro4I58t77dPnxU implements BiConsumer {
    public static final /* synthetic */ $$Lambda$ciir_QAmv6RwJro4I58t77dPnxU INSTANCE = new $$Lambda$ciir_QAmv6RwJro4I58t77dPnxU();

    private /* synthetic */ $$Lambda$ciir_QAmv6RwJro4I58t77dPnxU() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((PackageInstaller.SessionCallback) obj).onCreated(((Integer) obj2).intValue());
    }
}
