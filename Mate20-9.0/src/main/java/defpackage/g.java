package defpackage;

import android.content.Intent;
import android.util.Log;
import com.huawei.android.feature.install.FeatureFetcher;
import com.huawei.android.feature.install.InstallSessionState;
import com.huawei.android.feature.install.InstallSessionStateNotifier;
import com.huawei.android.feature.install.signature.FeatureSignatureCompat;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.util.List;

/* renamed from: g  reason: default package */
public final class g implements Runnable {
    final /* synthetic */ InstallSessionState g;
    final /* synthetic */ InstallSessionStateNotifier h;
    final /* synthetic */ FeatureFetcher i;

    public g(FeatureFetcher featureFetcher, InstallSessionState installSessionState, InstallSessionStateNotifier installSessionStateNotifier) {
        this.i = featureFetcher;
        this.g = installSessionState;
        this.h = installSessionStateNotifier;
    }

    public final void run() {
        List<String> list = this.g.mModuleNames;
        List<Intent> list2 = this.g.mUriIntents;
        if (list == null || list.size() == 0) {
            this.h.notifySessionState(3);
            return;
        }
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 < list.size()) {
                if (this.i.fetchApk(list.get(i3), list2.get(i3).getData())) {
                    Log.d(FeatureFetcher.TAG, "installUnverifyFeatures");
                    DynamicModuleManager.installUnverifyFeatures(this.i.mContext, list.get(i3) + ".apk", FeatureSignatureCompat.getInstance().getExceptSignInfo(list.get(i3)));
                    Log.d(FeatureFetcher.TAG, "installUnverifyFeatures finish");
                }
                i2 = i3 + 1;
            } else {
                this.h.notifySessionState(5);
                return;
            }
        }
    }
}
