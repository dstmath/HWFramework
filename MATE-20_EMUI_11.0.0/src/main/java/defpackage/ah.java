package defpackage;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.module.DynamicModule;

/* renamed from: ah  reason: default package */
public final class ah extends ae {
    private static ah Q = null;

    private ah(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        this.context = context;
        this.mInstallManager = featureLocalInstallManager;
    }

    public static synchronized ah b(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        ah ahVar;
        synchronized (ah.class) {
            if (Q == null) {
                Q = new ah(context, featureLocalInstallManager);
            }
            ahVar = Q;
        }
        return ahVar;
    }

    public final long getVersionCode() {
        try {
            if (this.mInstallManager.getInstallModules().contains("pushcore")) {
                return new DynamicModule("pushcore").getDynamicModuleInfo().mVersionCode;
            }
            return -1;
        } catch (Exception e) {
            Log.e("PushLogSys", "get local versionCode error");
            return -1;
        }
    }
}
