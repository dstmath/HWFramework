package defpackage;

import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;

/* renamed from: z  reason: default package */
public final class z implements Runnable {
    private IFeatureLocalInstall z;

    public z(IFeatureLocalInstall iFeatureLocalInstall) {
        this.z = iFeatureLocalInstall;
    }

    public final void run() {
        this.z.onInstallFeatureBegin();
    }
}
