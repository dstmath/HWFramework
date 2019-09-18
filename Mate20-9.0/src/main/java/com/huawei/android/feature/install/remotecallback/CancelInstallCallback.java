package com.huawei.android.feature.install.remotecallback;

import android.os.Bundle;
import com.huawei.android.feature.install.RemoteServiceCallback;
import com.huawei.android.feature.install.RemoteServiceConnector;
import com.huawei.android.feature.tasks.TaskHolder;

public class CancelInstallCallback extends RemoteServiceCallback<Void> {
    public CancelInstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder taskHolder) {
        super(remoteServiceConnector, taskHolder);
    }

    public final void onCancelInstall(int i, Bundle bundle) {
        super.onCancelInstall(i, bundle);
        this.mTaskHolder.notifyResult(null);
    }
}
