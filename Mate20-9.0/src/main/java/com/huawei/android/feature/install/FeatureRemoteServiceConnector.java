package com.huawei.android.feature.install;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.feature.install.config.RemoteConfig;
import java.util.List;

public class FeatureRemoteServiceConnector extends RemoteServiceConnector {
    private RemoteConfig mRemoteConfig;

    public FeatureRemoteServiceConnector(Context context, RemoteConfig remoteConfig) {
        super(context);
        this.mRemoteConfig = remoteConfig;
    }

    public Intent getBindServiceIntent() {
        return new Intent(this.mRemoteConfig.getBindRemoteServiceAction()).setPackage(this.mRemoteConfig.getBindRemoteServicePkgName());
    }

    public void handleBindRemoteServiceError(List<RemoteRequest> list) {
    }
}
