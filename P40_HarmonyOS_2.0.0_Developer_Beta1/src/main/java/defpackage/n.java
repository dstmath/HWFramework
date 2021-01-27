package defpackage;

import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;

/* renamed from: n  reason: default package */
public final class n implements Runnable {
    private int code;
    private String featureName;
    private IFeatureLocalInstall o;

    public n(String str, int i, IFeatureLocalInstall iFeatureLocalInstall) {
        this.o = iFeatureLocalInstall;
        this.code = i;
        this.featureName = str;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.o.onInstallProgressUpdate(this.featureName, this.code);
    }
}
