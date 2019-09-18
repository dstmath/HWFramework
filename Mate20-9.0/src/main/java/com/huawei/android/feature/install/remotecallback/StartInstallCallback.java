package com.huawei.android.feature.install.remotecallback;

import android.os.Bundle;
import com.huawei.android.feature.install.FeatureFetchListener;
import com.huawei.android.feature.install.FetchFeatureReference;
import com.huawei.android.feature.install.InstallSessionState;
import com.huawei.android.feature.install.RemoteServiceCallback;
import com.huawei.android.feature.install.RemoteServiceConnector;
import com.huawei.android.feature.tasks.TaskHolder;

public class StartInstallCallback extends RemoteServiceCallback<Integer> {
    public StartInstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder taskHolder) {
        super(remoteServiceConnector, taskHolder);
    }

    public void onStartInstall(int i, Bundle bundle) {
        super.onStartInstall(i, bundle);
        if (i == 0) {
            FetchFeatureReference.get().fetch(InstallSessionState.buildWithBundle(bundle), (FeatureFetchListener) new ac(this));
            return;
        }
        this.mTaskHolder.notifyResult(Integer.valueOf(i));
    }
}
