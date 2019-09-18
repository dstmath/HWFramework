package com.huawei.android.feature.install;

import android.content.Context;
import android.os.Bundle;
import com.huawei.android.feature.install.config.RemoteConfig;
import com.huawei.android.feature.tasks.Task;
import com.huawei.android.feature.tasks.TaskHolder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class RemoteRequestStrategy {
    /* access modifiers changed from: private */
    public static final String TAG = RemoteRequestStrategy.class.getSimpleName();
    protected Context mContext;
    /* access modifiers changed from: private */
    public String mPackageName;
    protected RemoteConfig mRemoteConfig;
    public RemoteServiceConnector mRemoteConnector;

    public RemoteRequestStrategy(Context context, String str, RemoteConfig remoteConfig) {
        this.mContext = context;
        this.mPackageName = str;
        this.mRemoteConfig = remoteConfig;
        initInstaller();
    }

    public Task<Void> cancelInstall(int i) {
        TaskHolder taskHolder = new TaskHolder();
        this.mRemoteConnector.doRemoteRequest(new n(this, taskHolder, i, taskHolder));
        return taskHolder.getTask();
    }

    public abstract RemoteServiceCallback createCancelInstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<Void> taskHolder);

    public abstract RemoteServiceCallback createDeferredInstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<Void> taskHolder);

    public abstract RemoteServiceCallback createDeferredUnInsallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<Void> taskHolder);

    public abstract Bundle createExtraInfoBundle();

    public abstract RemoteServiceCallback createGetSessionStateCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<InstallSessionState> taskHolder);

    public abstract RemoteServiceCallback createGetSessionStatesCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<List<InstallSessionState>> taskHolder);

    public abstract ArrayList<Bundle> createModuleNameBundle(Collection<String> collection);

    public abstract RemoteServiceCallback createStartInstallCallback(RemoteServiceConnector remoteServiceConnector, TaskHolder<Integer> taskHolder);

    public abstract ArrayList<Bundle> createVersionModuleNameBundle(Collection<String> collection);

    public Task<Void> deferredInstall(List<String> list) {
        TaskHolder taskHolder = new TaskHolder();
        this.mRemoteConnector.doRemoteRequest(new o(this, taskHolder, list, taskHolder));
        return taskHolder.getTask();
    }

    public Task<Void> deferredUninstall(List<String> list) {
        TaskHolder taskHolder = new TaskHolder();
        this.mRemoteConnector.doRemoteRequest(new p(this, taskHolder, list, taskHolder));
        return taskHolder.getTask();
    }

    public Task<InstallSessionState> getSessionState(int i) {
        TaskHolder taskHolder = new TaskHolder();
        this.mRemoteConnector.doRemoteRequest(new q(this, taskHolder, i, taskHolder));
        return taskHolder.getTask();
    }

    public Task<List<InstallSessionState>> getSessionStates() {
        TaskHolder taskHolder = new TaskHolder();
        this.mRemoteConnector.doRemoteRequest(new r(this, taskHolder, taskHolder));
        return taskHolder.getTask();
    }

    /* access modifiers changed from: protected */
    public abstract void initInstaller();

    public Task<Integer> startInstallModules(Collection<String> collection) {
        TaskHolder taskHolder = new TaskHolder();
        this.mRemoteConnector.doRemoteRequest(new m(this, taskHolder, collection, taskHolder));
        return taskHolder.getTask();
    }
}
