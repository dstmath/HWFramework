package com.huawei.android.feature.install.localinstall;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.huawei.android.feature.install.BasePackageInfoManager;
import com.huawei.android.feature.install.InstallBgExecutor;
import com.huawei.android.feature.install.InstallRequest;
import com.huawei.android.feature.module.DynamicFeatureState;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

public class FeatureLocalInstallManager {
    private static final int MAX_FEATURE_NUM = 100;
    /* access modifiers changed from: private */
    public static final String TAG = FeatureLocalInstallManager.class.getSimpleName();
    /* access modifiers changed from: private */
    public Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    /* access modifiers changed from: private */
    public PathFactory mPathFactory;

    public FeatureLocalInstallManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("params must not be null.");
        }
        this.mContext = context;
        this.mPathFactory = new PathFactory();
    }

    /* access modifiers changed from: private */
    public void addInstallFeatureState(String str) {
        int dynamicFeatureState = DynamicModuleManager.getInstance().getDynamicFeatureState(str);
        if (5 == dynamicFeatureState) {
            DynamicModuleManager.getInstance().addDynamicFeatureState(new DynamicFeatureState(str, 10));
        } else if (dynamicFeatureState == 0) {
            DynamicModuleManager.getInstance().addDynamicFeatureState(new DynamicFeatureState(str, 4));
        } else {
            Log.w(TAG, "it's not right to addFeatureState at statue:" + dynamicFeatureState);
        }
    }

    public static void copy(File file, File file2) {
        FileOutputStream fileOutputStream;
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            fileOutputStream = new FileOutputStream(file2);
            byte[] bArr = new byte[1024];
            while (true) {
                int read = fileInputStream.read(bArr);
                if (read > 0) {
                    fileOutputStream.write(bArr, 0, read);
                } else {
                    fileOutputStream.close();
                    fileInputStream.close();
                    return;
                }
            }
        } catch (Throwable th) {
            fileInputStream.close();
            throw th;
        }
    }

    public static long getInstalledModuleVersionCode(String str) {
        if (DynamicModuleManager.getInstance().getDynamicModule(str) == null) {
            return -1;
        }
        return DynamicModuleManager.getInstance().getDynamicModule(str).getModuleInfo().mVersionCode;
    }

    /* access modifiers changed from: private */
    public void notifyFeatureInstallBegin(IFeatureLocalInstall iFeatureLocalInstall) {
        this.mHandler.post(new z(iFeatureLocalInstall));
    }

    /* access modifiers changed from: private */
    public void notifyFeatureInstallEnd(IFeatureLocalInstall iFeatureLocalInstall) {
        this.mHandler.post(new aa(iFeatureLocalInstall));
    }

    /* access modifiers changed from: private */
    public void notifyFeatureInstallStatus(String str, int i, IFeatureLocalInstall iFeatureLocalInstall) {
        this.mHandler.post(new ab(str, i, iFeatureLocalInstall));
    }

    /* access modifiers changed from: private */
    public void procFeatureInstallEnd(String str, int i, IFeatureLocalInstall iFeatureLocalInstall) {
        updateFeatureState(str, i);
        this.mHandler.post(new ab(str, i, iFeatureLocalInstall));
    }

    private void startInstallExtend(InstallRequest installRequest, IFeatureLocalInstall iFeatureLocalInstall, boolean z) {
        if (installRequest == null || iFeatureLocalInstall == null) {
            throw new IllegalArgumentException("params must not be null.");
        }
        InstallBgExecutor.getExecutor().execute(new x(this, iFeatureLocalInstall, installRequest, z));
    }

    private void updateFeatureState(String str, int i) {
        if (i == 0 || 10 == DynamicModuleManager.getInstance().getDynamicFeatureState(str)) {
            DynamicModuleManager.getInstance().addDynamicFeatureState(new DynamicFeatureState(str, 5));
        } else {
            DynamicModuleManager.getInstance().delDynamicFeatureState(str);
        }
    }

    public Set<String> getInstallModules() {
        Set<String> keySet = DynamicModuleManager.getInstance().getInstalledModules().keySet();
        Set<String> installedModules = BasePackageInfoManager.getInstance(this.mContext).getInstalledModules();
        HashSet hashSet = new HashSet(keySet);
        hashSet.addAll(installedModules);
        return hashSet;
    }

    public void startInstall(InstallRequest installRequest, IFeatureLocalInstall iFeatureLocalInstall) {
        startInstallExtend(installRequest, iFeatureLocalInstall, false);
    }

    public void startInstallBackup(InstallRequest installRequest, IFeatureLocalInstall iFeatureLocalInstall) {
        startInstallExtend(installRequest, iFeatureLocalInstall, true);
    }

    public void startInstallForce(FeatureLocalInstallRequest featureLocalInstallRequest, IFeatureLocalInstall iFeatureLocalInstall) {
        if (featureLocalInstallRequest == null || iFeatureLocalInstall == null) {
            throw new IllegalArgumentException("params must not be null.");
        }
        InstallBgExecutor.getExecutor().execute(new y(this, iFeatureLocalInstall, featureLocalInstallRequest));
    }
}
