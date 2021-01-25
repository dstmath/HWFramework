package defpackage;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;

/* renamed from: ai  reason: default package */
public final class ai extends ae {
    private static ai R = null;

    private ai(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        this.context = context;
        this.mInstallManager = featureLocalInstallManager;
    }

    public static synchronized ai c(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        ai aiVar;
        synchronized (ai.class) {
            if (R == null) {
                R = new ai(context, featureLocalInstallManager);
            }
            aiVar = R;
        }
        return aiVar;
    }

    public final long getVersionCode() {
        long a = a("com.huawei.android.pushagent", "NC", "pushcore_version");
        if (a != -1) {
            Log.i("PushLogSys", "get nc meta push version");
            return a;
        }
        try {
            return (long) this.context.getPackageManager().getPackageInfo("com.huawei.android.pushagent", 0).versionCode;
        } catch (Exception e) {
            Log.e("PushLogSys", "get nc versionCode error: " + ao.a(e));
            return a;
        }
    }
}
