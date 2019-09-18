package defpackage;

import android.content.Intent;
import android.util.Log;
import com.huawei.android.feature.install.FeatureFetchListener;
import com.huawei.android.feature.install.FeatureFetcher;
import com.huawei.android.feature.install.InstallSessionState;
import com.huawei.android.feature.install.signature.FeatureSignatureCompat;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.util.List;

/* renamed from: h  reason: default package */
public final class h implements Runnable {
    final /* synthetic */ InstallSessionState g;
    final /* synthetic */ FeatureFetcher i;
    final /* synthetic */ FeatureFetchListener j;

    public h(FeatureFetcher featureFetcher, InstallSessionState installSessionState, FeatureFetchListener featureFetchListener) {
        this.i = featureFetcher;
        this.g = installSessionState;
        this.j = featureFetchListener;
    }

    public final void run() {
        List<String> list = this.g.mModuleNames;
        List<Intent> list2 = this.g.mUriIntents;
        if (list == null || list.size() == 0) {
            this.j.onComplete(-1);
            return;
        }
        for (int i2 = 0; i2 < list.size(); i2++) {
            if (this.i.fetchApk(list.get(i2), list2.get(i2).getData())) {
                Log.d(FeatureFetcher.TAG, "installUnverifyFeatures");
                DynamicModuleManager.installUnverifyFeatures(this.i.mContext, list.get(i2) + ".apk", FeatureSignatureCompat.getInstance().getExceptSignInfo(list.get(i2)));
                Log.d(FeatureFetcher.TAG, "installUnverifyFeatures finish");
            }
        }
        this.j.onComplete(0);
    }
}
