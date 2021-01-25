package com.huawei.android.feature.install;

import android.content.Context;
import android.util.Log;
import com.huawei.android.feature.tasks.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class InstallManager {
    private static final int MAX_FEATURE_NUM = 100;
    private static final String TAG = InstallManager.class.getSimpleName();
    protected Context mContext;
    public InstallStateUpdateObserver mStateUpdateObserver;

    public class Type {
        public static final int FEATURE_INSTALL = 3;
        public static final int PLUGIN_INSTALL = 2;
        public static final int SDK_INSTALL = 1;
    }

    public InstallManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public abstract Task<Void> cancelInstall(int i);

    public abstract Task<Void> deferredInstall(List<String> list);

    public abstract Task<Void> defferedUninstall(List<String> list);

    public abstract Set<String> getInstallModules();

    /* access modifiers changed from: protected */
    public List<String> getInstallModulesFromRequest(InstallRequest installRequest) {
        List<String> installRequestModules = installRequest.getInstallRequestModules();
        Set<String> installModules = getInstallModules();
        ArrayList arrayList = new ArrayList();
        if (installRequestModules.size() > MAX_FEATURE_NUM) {
            Log.e(TAG, "feature nums exceed the limit");
            return arrayList;
        }
        for (String str : installRequestModules) {
            if (!installModules.contains(str)) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    public abstract Task<InstallSessionState> getSessionState(int i);

    public abstract Task<List<InstallSessionState>> getSessionStates();

    public abstract Task<Integer> startInstallModules(InstallRequest installRequest);
}
