package com.huawei.android.feature.install.remotecallback;

import android.os.Bundle;
import com.huawei.android.feature.install.InstallSessionState;
import com.huawei.android.feature.install.RemoteServiceCallback;
import com.huawei.android.feature.install.RemoteServiceConnector;
import com.huawei.android.feature.tasks.TaskHolder;

public class GetSessionStateCallback extends RemoteServiceCallback<InstallSessionState> {
    public GetSessionStateCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder taskHolder) {
        super(remoteServiceConnector, taskHolder);
    }

    public void onGetSession(int i, Bundle bundle) {
        super.onGetSession(i, bundle);
        this.mTaskHolder.notifyResult(InstallSessionState.buildWithBundle(bundle));
    }
}
