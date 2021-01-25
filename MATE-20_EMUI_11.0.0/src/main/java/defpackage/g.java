package defpackage;

import com.huawei.android.feature.install.FeatureInstallManager;
import com.huawei.android.feature.install.InstallRequest;
import com.huawei.android.feature.install.InstallSessionState;

/* renamed from: g  reason: default package */
public final class g implements Runnable {
    final /* synthetic */ InstallRequest g;
    final /* synthetic */ FeatureInstallManager h;

    public g(FeatureInstallManager featureInstallManager, InstallRequest installRequest) {
        this.h = featureInstallManager;
        this.g = installRequest;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.h.mStateUpdateObserver.notifyState(InstallSessionState.buildWithBundle(InstallSessionState.buildInstalledBundle(this.g.getInstallRequestModules())));
    }
}
