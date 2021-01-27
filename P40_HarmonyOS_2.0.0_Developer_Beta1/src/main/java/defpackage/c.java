package defpackage;

import android.content.Context;
import com.huawei.android.feature.compat.InstallCompat;
import com.huawei.android.feature.compat.adapter.VersionApiFactory;
import com.huawei.android.feature.compat.adapter.VersionApiReference;

/* renamed from: c  reason: default package */
public final class c implements Runnable {
    final /* synthetic */ Context b;

    public c(Context context) {
        this.b = context;
    }

    @Override // java.lang.Runnable
    public final void run() {
        VersionApiReference.set(VersionApiFactory.create());
        int unused = InstallCompat.installIsolatedIfNeed(this.b, true);
        int unused2 = InstallCompat.installNonIsolated(this.b);
    }
}
