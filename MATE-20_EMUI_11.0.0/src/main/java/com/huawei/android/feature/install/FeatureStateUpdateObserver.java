package com.huawei.android.feature.install;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class FeatureStateUpdateObserver extends InstallStateUpdateObserver {
    private static final String TAG = FeatureStateUpdateObserver.class.getSimpleName();

    public FeatureStateUpdateObserver(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.android.feature.install.InstallStateUpdateObserver
    public String getIntentFilterAction() {
        return "not use yet";
    }

    /* access modifiers changed from: protected */
    @Override // com.huawei.android.feature.install.InstallStateUpdateObserver
    public void handleStateUpdate(Intent intent) {
        Bundle bundleExtra = intent.getBundleExtra("session_state");
        if (bundleExtra == null) {
            Log.d(TAG, "no session state bundle in the intent");
        } else {
            notifyState(InstallSessionState.buildWithBundle(bundleExtra));
        }
    }
}
