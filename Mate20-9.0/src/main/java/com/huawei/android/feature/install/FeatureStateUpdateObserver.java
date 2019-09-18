package com.huawei.android.feature.install;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.huawei.android.feature.install.config.RemoteConfig;

public class FeatureStateUpdateObserver extends InstallStateUpdateObserver {
    private static final String TAG = FeatureStateUpdateObserver.class.getSimpleName();

    public FeatureStateUpdateObserver(Context context, RemoteConfig remoteConfig) {
        super(context, remoteConfig);
    }

    /* access modifiers changed from: protected */
    public String getIntentFilterAction() {
        return this.mRemoteConfig.getReceiveBroadcastAction();
    }

    /* access modifiers changed from: protected */
    public void handleStateUpdate(Intent intent) {
        Bundle bundleExtra = intent.getBundleExtra("session_state");
        if (bundleExtra == null) {
            Log.d(TAG, "no session state bundle in the intent");
            return;
        }
        InstallSessionState buildWithBundle = InstallSessionState.buildWithBundle(bundleExtra);
        if (buildWithBundle.mStatus != 3 || FetchFeatureReference.get() == null) {
            notifyState(buildWithBundle);
        } else {
            FetchFeatureReference.get().fetch(buildWithBundle, (InstallSessionStateNotifier) new j(this, buildWithBundle));
        }
    }
}
