package defpackage;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.module.DynamicModule;

/* renamed from: ax  reason: default package */
public final class ax extends av {
    private static ax am = null;

    private ax(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        this.X = context;
        this.W = featureLocalInstallManager;
    }

    public static synchronized ax b(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        ax axVar;
        synchronized (ax.class) {
            if (am == null) {
                am = new ax(context, featureLocalInstallManager);
            }
            axVar = am;
        }
        return axVar;
    }

    public final long getVersionCode() {
        try {
            if (this.W.getInstallModules().contains("pushcore")) {
                return new DynamicModule("pushcore").getDynamicModuleInfo().mVersionCode;
            }
            return -1;
        } catch (Exception e) {
            Log.e("PushLogSys", "get local versionCode error");
            return -1;
        }
    }
}
