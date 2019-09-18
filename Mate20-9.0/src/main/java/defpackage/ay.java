package defpackage;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.install.InstallRequest;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallRequest;

/* renamed from: ay  reason: default package */
public final class ay extends av {
    private static ay an = null;

    private ay(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        this.X = context;
        this.W = featureLocalInstallManager;
    }

    public static synchronized ay c(Context context, FeatureLocalInstallManager featureLocalInstallManager) {
        ay ayVar;
        synchronized (ay.class) {
            if (an == null) {
                an = new ay(context, featureLocalInstallManager);
            }
            ayVar = an;
        }
        return ayVar;
    }

    /* access modifiers changed from: private */
    public void f() {
        if (this.ak >= 3 || this.ak < 0) {
            Log.e("PushLogSys", "install NC pushcore error, try to load local pushcore");
            g();
            return;
        }
        this.W.startInstall(InstallRequest.newBuilder().addModule(new FeatureLocalInstallRequest("pushcore", "package://com.huawei.android.pushagent/feature/pushcore.fpk", "1E3EEE2A88A6DF75FB4AF56ADC8373BB818F3CB90A4935C7821582B8CEBB694C")).build(), new az(this));
    }

    /* access modifiers changed from: private */
    public void k() {
        if (this.ak >= 3 || this.ak < 0) {
            Log.e("PushLogSys", "force install NC pushcore error, push is disable!!!");
            return;
        }
        this.W.startInstallForce(new FeatureLocalInstallRequest("pushcore", "package://com.huawei.android.pushagent/feature/pushcore.fpk", "1E3EEE2A88A6DF75FB4AF56ADC8373BB818F3CB90A4935C7821582B8CEBB694C"), new ba(this));
    }

    public final long getVersionCode() {
        long j = -1;
        try {
            return (long) this.X.getPackageManager().getPackageInfo("com.huawei.android.pushagent", 0).versionCode;
        } catch (Exception e) {
            Log.e("PushLogSys", "get nc versionCode error: " + bb.a(e));
            return j;
        }
    }

    public final void i() {
        this.ak = 0;
        f();
    }

    public final void j() {
        Log.i("PushLogSys", "begin force install NC pushcore");
        this.ak = 0;
        k();
    }
}
