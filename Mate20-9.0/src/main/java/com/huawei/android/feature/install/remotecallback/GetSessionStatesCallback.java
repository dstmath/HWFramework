package com.huawei.android.feature.install.remotecallback;

import android.os.Bundle;
import com.huawei.android.feature.install.InstallSessionState;
import com.huawei.android.feature.install.RemoteServiceCallback;
import com.huawei.android.feature.install.RemoteServiceConnector;
import com.huawei.android.feature.tasks.TaskHolder;
import java.util.ArrayList;
import java.util.List;

public class GetSessionStatesCallback extends RemoteServiceCallback<List<InstallSessionState>> {
    public GetSessionStatesCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder taskHolder) {
        super(remoteServiceConnector, taskHolder);
    }

    public void onGetSessionStates(List<Bundle> list) {
        super.onGetSessionStates(list);
        ArrayList arrayList = new ArrayList(list.size());
        for (Bundle buildWithBundle : list) {
            arrayList.add(InstallSessionState.buildWithBundle(buildWithBundle));
        }
        this.mTaskHolder.notifyResult(arrayList);
    }
}
