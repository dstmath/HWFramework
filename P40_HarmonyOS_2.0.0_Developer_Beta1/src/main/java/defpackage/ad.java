package defpackage;

import android.content.Context;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;

/* renamed from: ad  reason: default package */
public final class ad extends ae {
    private static ad K = null;
    private int time = 0;

    private ad(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        this.context = context;
        this.mInstallManager = featureLocalInstallManager;
    }

    public static synchronized ad a(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        ad adVar;
        synchronized (ad.class) {
            if (K == null) {
                K = new ad(context, featureLocalInstallManager);
            }
            adVar = K;
        }
        return adVar;
    }

    private String c() {
        return new am(this.context).g();
    }

    public final long getVersionCode() {
        return ao.h() ? a(c(), "HMS", "pushcore_version") : a(c(), "HMS", "pushcore_v2_version");
    }
}
