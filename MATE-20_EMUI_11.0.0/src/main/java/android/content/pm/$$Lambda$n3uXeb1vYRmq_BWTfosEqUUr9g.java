package android.content.pm;

import android.content.pm.PackageInstaller;
import com.android.internal.util.function.TriConsumer;

/* renamed from: android.content.pm.-$$Lambda$n3uXeb1v-YRmq_BWTfosEqUUr9g  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$n3uXeb1vYRmq_BWTfosEqUUr9g implements TriConsumer {
    public static final /* synthetic */ $$Lambda$n3uXeb1vYRmq_BWTfosEqUUr9g INSTANCE = new $$Lambda$n3uXeb1vYRmq_BWTfosEqUUr9g();

    private /* synthetic */ $$Lambda$n3uXeb1vYRmq_BWTfosEqUUr9g() {
    }

    @Override // com.android.internal.util.function.TriConsumer
    public final void accept(Object obj, Object obj2, Object obj3) {
        ((PackageInstaller.SessionCallback) obj).onProgressChanged(((Integer) obj2).intValue(), ((Float) obj3).floatValue());
    }
}
