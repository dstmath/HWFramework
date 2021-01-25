package defpackage;

import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;

/* renamed from: m  reason: default package */
public final class m implements Runnable {
    private IFeatureLocalInstall n;

    public m(IFeatureLocalInstall iFeatureLocalInstall) {
        this.n = iFeatureLocalInstall;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.n.onInstallFeatureEnd();
    }
}
