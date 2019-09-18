package com.huawei.android.feature.install.remotecallback;

import android.os.Bundle;
import com.huawei.android.feature.install.RemoteServiceCallback;
import com.huawei.android.feature.install.RemoteServiceConnector;
import com.huawei.android.feature.tasks.TaskHolder;

public class DefferedInstallCallback extends RemoteServiceCallback<Void> {
    public DefferedInstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<Void> taskHolder) {
        super(remoteServiceConnector, taskHolder);
    }

    public void onDeferredInstall(Bundle bundle) {
        super.onDeferredInstall(bundle);
        this.mTaskHolder.notifyResult(null);
    }
}
