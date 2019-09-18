package defpackage;

import android.content.Context;
import com.huawei.android.feature.compat.InstallCompat;
import com.huawei.android.feature.compat.adapter.VersionApiFactory;
import com.huawei.android.feature.compat.adapter.VersionApiReference;
import com.huawei.android.feature.install.FeatureFetcher;
import com.huawei.android.feature.install.FetchFeatureReference;

/* renamed from: c  reason: default package */
public final class c implements Runnable {
    final /* synthetic */ Context b;

    public c(Context context) {
        this.b = context;
    }

    public final void run() {
        FetchFeatureReference.set(new FeatureFetcher(this.b));
        VersionApiReference.set(VersionApiFactory.create());
        int unused = InstallCompat.installIsolatedIfNeed(this.b, true);
        int unused2 = InstallCompat.installNonIsolated(this.b);
    }
}
