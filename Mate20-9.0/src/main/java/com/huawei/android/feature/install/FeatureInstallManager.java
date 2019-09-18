package com.huawei.android.feature.install;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.huawei.android.feature.install.config.RemoteConfig;
import com.huawei.android.feature.module.DynamicModuleManager;
import com.huawei.android.feature.tasks.Task;
import com.huawei.android.feature.tasks.Tasks;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureInstallManager extends InstallManager {
    public FeatureInstallManager(Context context, RemoteConfig remoteConfig) {
        super(context);
        this.mRemoteRequestStrategy = new FeatureRemoteRequestStrategy(this.mContext, remoteConfig);
        this.mStateUpdateObserver = new FeatureStateUpdateObserver(this.mContext, remoteConfig);
    }

    public Task<Void> cancelInstall(int i) {
        return this.mRemoteRequestStrategy.cancelInstall(i);
    }

    public Task<Void> deferredInstall(List<String> list) {
        return this.mRemoteRequestStrategy.deferredInstall(list);
    }

    public Task<Void> defferedUninstall(List<String> list) {
        return this.mRemoteRequestStrategy.deferredUninstall(list);
    }

    public Set<String> getInstallModules() {
        Set<String> keySet = DynamicModuleManager.getInstance().getInstalledModules().keySet();
        Set<String> installedModules = BasePackageInfoManager.getInstance(this.mContext).getInstalledModules();
        HashSet hashSet = new HashSet(keySet);
        hashSet.addAll(installedModules);
        return hashSet;
    }

    public Task<InstallSessionState> getSessionState(int i) {
        return this.mRemoteRequestStrategy.getSessionState(i);
    }

    public Task<List<InstallSessionState>> getSessionStates() {
        return this.mRemoteRequestStrategy.getSessionStates();
    }

    public void registerListener(InstallStateUpdatedListener installStateUpdatedListener) {
        this.mStateUpdateObserver.registerListener(installStateUpdatedListener);
    }

    public Task<Integer> startInstallModules(InstallRequest installRequest) {
        List<String> installModulesFromRequest = getInstallModulesFromRequest(installRequest);
        if (installModulesFromRequest.size() != 0) {
            return this.mRemoteRequestStrategy.startInstallModules(installModulesFromRequest);
        }
        new Handler(Looper.getMainLooper()).post(new i(this, installRequest));
        return Tasks.makeTask(0);
    }

    public void unregisterListener(InstallStateUpdatedListener installStateUpdatedListener) {
        this.mStateUpdateObserver.unregisterListener(installStateUpdatedListener);
    }
}
