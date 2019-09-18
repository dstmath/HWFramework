package defpackage;

import com.huawei.android.feature.install.FeatureStateUpdateObserver;
import com.huawei.android.feature.install.InstallSessionState;
import com.huawei.android.feature.install.InstallSessionStateNotifier;

/* renamed from: j  reason: default package */
public final class j extends InstallSessionStateNotifier {
    final /* synthetic */ FeatureStateUpdateObserver m;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public j(FeatureStateUpdateObserver featureStateUpdateObserver, InstallSessionState installSessionState) {
        super(installSessionState);
        this.m = featureStateUpdateObserver;
    }

    public final void notifySessionState(int i) {
        this.mSessionState.mStatus = i;
        this.m.notifyState(this.mSessionState);
    }
}
