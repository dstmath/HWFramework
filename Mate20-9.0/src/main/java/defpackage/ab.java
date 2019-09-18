package defpackage;

import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;

/* renamed from: ab  reason: default package */
public final class ab implements Runnable {
    private IFeatureLocalInstall A;
    private int code;
    private String featureName;

    public ab(String str, int i, IFeatureLocalInstall iFeatureLocalInstall) {
        this.A = iFeatureLocalInstall;
        this.code = i;
        this.featureName = str;
    }

    public final void run() {
        this.A.onInstallProgressUpdate(this.featureName, this.code);
    }
}
