package android.content.pm;

import android.content.pm.PackageInstaller;
import java.util.function.BiConsumer;

/* renamed from: android.content.pm.-$$Lambda$B12dZLpdwpXn89QSesmkaZjD72Q  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$B12dZLpdwpXn89QSesmkaZjD72Q implements BiConsumer {
    public static final /* synthetic */ $$Lambda$B12dZLpdwpXn89QSesmkaZjD72Q INSTANCE = new $$Lambda$B12dZLpdwpXn89QSesmkaZjD72Q();

    private /* synthetic */ $$Lambda$B12dZLpdwpXn89QSesmkaZjD72Q() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((PackageInstaller.SessionCallback) obj).onBadgingChanged(((Integer) obj2).intValue());
    }
}
