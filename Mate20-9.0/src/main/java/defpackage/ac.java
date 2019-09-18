package defpackage;

import com.huawei.android.feature.install.FeatureFetchListener;
import com.huawei.android.feature.install.remotecallback.StartInstallCallback;

/* renamed from: ac  reason: default package */
public final class ac implements FeatureFetchListener {
    final /* synthetic */ StartInstallCallback B;

    public ac(StartInstallCallback startInstallCallback) {
        this.B = startInstallCallback;
    }

    public final void onComplete(int i) {
        this.B.mTaskHolder.notifyResult(Integer.valueOf(i));
    }
}
