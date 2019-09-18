package defpackage;

import com.huawei.android.feature.install.FeatureInstallManager;
import com.huawei.android.feature.install.InstallRequest;
import com.huawei.android.feature.install.InstallSessionState;

/* renamed from: i  reason: default package */
public final class i implements Runnable {
    final /* synthetic */ InstallRequest k;
    final /* synthetic */ FeatureInstallManager l;

    public i(FeatureInstallManager featureInstallManager, InstallRequest installRequest) {
        this.l = featureInstallManager;
        this.k = installRequest;
    }

    public final void run() {
        this.l.mStateUpdateObserver.notifyState(InstallSessionState.buildWithBundle(InstallSessionState.buildInstalledBundle(this.k.getInstallRequestModules())));
    }
}
