package com.huawei.android.feature.install;

import android.content.Context;
import android.os.Bundle;
import com.huawei.android.feature.install.config.RemoteConfig;
import com.huawei.android.feature.install.remotecallback.CancelInstallCallback;
import com.huawei.android.feature.install.remotecallback.DefferedInstallCallback;
import com.huawei.android.feature.install.remotecallback.DefferedUninstallCallback;
import com.huawei.android.feature.install.remotecallback.GetSessionStateCallback;
import com.huawei.android.feature.install.remotecallback.GetSessionStatesCallback;
import com.huawei.android.feature.install.remotecallback.StartInstallCallback;
import com.huawei.android.feature.module.DynamicModuleInternal;
import com.huawei.android.feature.module.DynamicModuleManager;
import com.huawei.android.feature.tasks.TaskHolder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FeatureRemoteRequestStrategy extends RemoteRequestStrategy {
    public FeatureRemoteRequestStrategy(Context context, RemoteConfig remoteConfig) {
        super(context, context.getPackageName(), remoteConfig);
    }

    /* access modifiers changed from: protected */
    public RemoteServiceCallback createCancelInstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<Void> taskHolder) {
        return new CancelInstallCallback(remoteServiceConnector, taskHolder);
    }

    /* access modifiers changed from: protected */
    public RemoteServiceCallback createDeferredInstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<Void> taskHolder) {
        return new DefferedInstallCallback(remoteServiceConnector, taskHolder);
    }

    /* access modifiers changed from: protected */
    public RemoteServiceCallback createDeferredUnInsallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<Void> taskHolder) {
        return new DefferedUninstallCallback(remoteServiceConnector, taskHolder);
    }

    /* access modifiers changed from: protected */
    public Bundle createExtraInfoBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("client_sdk_version_code", 1);
        bundle.putString("client_sdk_broadcast_action", this.mRemoteConfig.getReceiveBroadcastAction());
        return bundle;
    }

    /* access modifiers changed from: protected */
    public RemoteServiceCallback createGetSessionStateCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<InstallSessionState> taskHolder) {
        return new GetSessionStateCallback(remoteServiceConnector, taskHolder);
    }

    /* access modifiers changed from: protected */
    public RemoteServiceCallback createGetSessionStatesCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<List<InstallSessionState>> taskHolder) {
        return new GetSessionStatesCallback(remoteServiceConnector, taskHolder);
    }

    /* access modifiers changed from: protected */
    public ArrayList<Bundle> createModuleNameBundle(Collection<String> collection) {
        ArrayList<Bundle> arrayList = new ArrayList<>();
        for (String putString : collection) {
            Bundle bundle = new Bundle();
            bundle.putString("module_name", putString);
            arrayList.add(bundle);
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public RemoteServiceCallback createStartInstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder taskHolder) {
        return new StartInstallCallback(remoteServiceConnector, taskHolder);
    }

    /* access modifiers changed from: protected */
    public ArrayList<Bundle> createVersionModuleNameBundle(Collection<String> collection) {
        ArrayList<Bundle> arrayList = new ArrayList<>();
        for (String next : collection) {
            Bundle bundle = new Bundle();
            bundle.putString("module_name", next);
            DynamicModuleInternal dynamicModule = DynamicModuleManager.getInstance().getDynamicModule(next);
            bundle.putLong("module_version", dynamicModule == null ? -1 : dynamicModule.getModuleInfo().mVersionCode);
            arrayList.add(bundle);
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public void initInstaller() {
        this.mRemoteConnector = new FeatureRemoteServiceConnector(this.mContext, this.mRemoteConfig);
    }
}
