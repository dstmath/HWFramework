package defpackage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.feature.install.InstallRequest;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallRequest;

/* renamed from: at  reason: default package */
public final class at extends av {
    private static String ah = "pushcore_version";
    private static at ai = null;
    public int time = 0;

    private at(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        this.X = context;
        this.W = featureLocalInstallManager;
    }

    public static synchronized at a(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        at atVar;
        synchronized (at.class) {
            if (ai == null) {
                ai = new at(context, featureLocalInstallManager);
            }
            atVar = ai;
        }
        return atVar;
    }

    public final void f() {
        if (this.time >= 3 || this.time < 0) {
            Log.e("PushLogSys", "install HMS pushcore error, begin to install NC pushcore");
            ay.c(this.X, this.W).i();
            return;
        }
        this.W.startInstall(InstallRequest.newBuilder().addModule(new FeatureLocalInstallRequest("pushcore", "package://com.huawei.hwid/feature/pushcore.fpk", "1E3EEE2A88A6DF75FB4AF56ADC8373BB818F3CB90A4935C7821582B8CEBB694C")).build(), new au(this));
    }

    public final long getVersionCode() {
        try {
            ApplicationInfo applicationInfo = this.X.getPackageManager().getApplicationInfo("com.huawei.hwid", 128);
            if (applicationInfo == null) {
                return -1;
            }
            Bundle bundle = applicationInfo.metaData;
            if (bundle == null) {
                return -1;
            }
            String string = bundle.getString(ah);
            if (TextUtils.isEmpty(string) || string.length() <= 3) {
                return -1;
            }
            return Long.parseLong(string.substring(3));
        } catch (Exception e) {
            Log.e("PushLogSys", "get hms push version code error: " + bb.a(e));
            return -1;
        }
    }
}
