package defpackage;

import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;

/* renamed from: aa  reason: default package */
public final class aa implements Runnable {
    private IFeatureLocalInstall z;

    public aa(IFeatureLocalInstall iFeatureLocalInstall) {
        this.z = iFeatureLocalInstall;
    }

    public final void run() {
        this.z.onInstallFeatureEnd();
    }
}
