package defpackage;

import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;

/* renamed from: l  reason: default package */
public final class l implements Runnable {
    private IFeatureLocalInstall n;

    public l(IFeatureLocalInstall iFeatureLocalInstall) {
        this.n = iFeatureLocalInstall;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.n.onInstallFeatureBegin();
    }
}
