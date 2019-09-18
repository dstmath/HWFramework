package com.huawei.android.feature.install.remotecallback;

import android.os.Bundle;
import com.huawei.android.feature.install.RemoteServiceCallback;
import com.huawei.android.feature.install.RemoteServiceConnector;
import com.huawei.android.feature.tasks.TaskHolder;

public class DefferedUninstallCallback extends RemoteServiceCallback<Void> {
    public DefferedUninstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder taskHolder) {
        super(remoteServiceConnector, taskHolder);
    }

    public void onDeferredUninstall(Bundle bundle) {
        super.onDeferredUninstall(bundle);
        this.mTaskHolder.notifyResult(null);
    }
}
