package android.content.pm;

import android.content.pm.PackageInstaller;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.content.pm.-$$Lambda$zO9HBUVgPeroyDQPLJE-MNMvSqc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$zO9HBUVgPeroyDQPLJEMNMvSqc implements TriConsumer {
    public static final /* synthetic */ $$Lambda$zO9HBUVgPeroyDQPLJEMNMvSqc INSTANCE = new $$Lambda$zO9HBUVgPeroyDQPLJEMNMvSqc();

    private /* synthetic */ $$Lambda$zO9HBUVgPeroyDQPLJEMNMvSqc() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((PackageInstaller.SessionCallback) obj).onFinished(((Integer) obj2).intValue(), ((Boolean) obj3).booleanValue());
    }
}
