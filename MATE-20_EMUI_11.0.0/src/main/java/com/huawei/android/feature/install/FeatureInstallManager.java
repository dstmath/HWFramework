package com.huawei.android.feature.install;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.huawei.android.feature.module.DynamicModuleManager;
import com.huawei.android.feature.tasks.Task;
import com.huawei.android.feature.tasks.Tasks;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureInstallManager extends InstallManager {
    public FeatureInstallManager(Context context) {
        super(context);
        this.mStateUpdateObserver = new FeatureStateUpdateObserver(this.mContext);
    }

    @Override // com.huawei.android.feature.install.InstallManager
    public Task<Void> cancelInstall(int i) {
        return null;
    }

    @Override // com.huawei.android.feature.install.InstallManager
    public Task<Void> deferredInstall(List<String> list) {
        return null;
    }

    @Override // com.huawei.android.feature.install.InstallManager
    public Task<Void> defferedUninstall(List<String> list) {
        return null;
    }

    @Override // com.huawei.android.feature.install.InstallManager
    public Set<String> getInstallModules() {
        Set<String> keySet = DynamicModuleManager.getInstance().getInstalledModules().keySet();
        Set<String> installedModules = BasePackageInfoManager.getInstance(this.mContext).getInstalledModules();
        HashSet hashSet = new HashSet(keySet);
        hashSet.addAll(installedModules);
        return hashSet;
    }

    @Override // com.huawei.android.feature.install.InstallManager
    public Task<InstallSessionState> getSessionState(int i) {
        return null;
    }

    @Override // com.huawei.android.feature.install.InstallManager
    public Task<List<InstallSessionState>> getSessionStates() {
        return null;
    }

    @Override // com.huawei.android.feature.install.InstallManager
    public Task<Integer> startInstallModules(InstallRequest installRequest) {
        if (getInstallModulesFromRequest(installRequest).size() != 0) {
            return null;
        }
        new Handler(Looper.getMainLooper()).post(new g(this, installRequest));
        return Tasks.makeTask(0);
    }
}
