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
    private static final String TAG = FeatureLocalInstallManager.class.getSimpleName();
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private PathFactory mPathFactory;

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
            Log.w(TAG, "it's not right to addFeatureState at statue:".concat(String.valueOf(dynamicFeatureState)));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        r2 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0026, code lost:
        r1 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0027, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x003a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x003b, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0041, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0042, code lost:
        r3.addSuppressed(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0046, code lost:
        r4.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x004a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x004b, code lost:
        r1 = r0;
        r2 = null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x002a A[SYNTHETIC, Splitter:B:24:0x002a] */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x003a A[ExcHandler: all (r0v0 'th' java.lang.Throwable A[CUSTOM_DECLARE])] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0046  */
    public static void copy(File file, File file2) {
        Throwable th;
        Throwable th2;
        Throwable th3;
        Throwable th4 = null;
        FileInputStream fileInputStream = new FileInputStream(file);
        FileOutputStream fileOutputStream = new FileOutputStream(file2);
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
        throw th2;
        if (th4 == null) {
        }
        throw th;
        throw th;
        if (th3 != null) {
            try {
                fileOutputStream.close();
            } catch (Throwable th5) {
            }
        } else {
            fileOutputStream.close();
        }
        throw th2;
    }

    public static long getInstalledModuleVersionCode(String str) {
        if (DynamicModuleManager.getInstance().getDynamicModule(str) == null) {
            return -1;
        }
        return DynamicModuleManager.getInstance().getDynamicModule(str).getModuleInfo().mVersionCode;
    }

    /* access modifiers changed from: private */
    public void notifyFeatureInstallBegin(IFeatureLocalInstall iFeatureLocalInstall, Handler handler) {
        if (handler == null) {
            handler = this.mHandler;
        }
        handler.post(new l(iFeatureLocalInstall));
    }

    /* access modifiers changed from: private */
    public void notifyFeatureInstallEnd(IFeatureLocalInstall iFeatureLocalInstall, Handler handler) {
        if (handler == null) {
            handler = this.mHandler;
        }
        handler.post(new m(iFeatureLocalInstall));
    }

    /* access modifiers changed from: private */
    public void notifyFeatureInstallStatus(String str, int i, IFeatureLocalInstall iFeatureLocalInstall, Handler handler) {
        if (handler == null) {
            handler = this.mHandler;
        }
        handler.post(new n(str, i, iFeatureLocalInstall));
    }

    /* access modifiers changed from: private */
    public void procFeatureInstallEnd(String str, int i, IFeatureLocalInstall iFeatureLocalInstall, Handler handler) {
        updateFeatureState(str, i);
        if (handler == null) {
            handler = this.mHandler;
        }
        handler.post(new n(str, i, iFeatureLocalInstall));
    }

    private void startInstallExtend(InstallRequest installRequest, IFeatureLocalInstall iFeatureLocalInstall, boolean z, Handler handler) {
        if (installRequest == null || iFeatureLocalInstall == null) {
            throw new IllegalArgumentException("params must not be null.");
        }
        InstallBgExecutor.getExecutor().execute(new j(this, iFeatureLocalInstall, handler, installRequest, z));
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
        startInstallExtend(installRequest, iFeatureLocalInstall, false, null);
    }

    public void startInstall(InstallRequest installRequest, IFeatureLocalInstall iFeatureLocalInstall, Handler handler) {
        startInstallExtend(installRequest, iFeatureLocalInstall, false, handler);
    }

    public void startInstallBackup(InstallRequest installRequest, IFeatureLocalInstall iFeatureLocalInstall) {
        startInstallExtend(installRequest, iFeatureLocalInstall, true, null);
    }

    public void startInstallBackup(InstallRequest installRequest, IFeatureLocalInstall iFeatureLocalInstall, Handler handler) {
        startInstallExtend(installRequest, iFeatureLocalInstall, true, handler);
    }

    public void startInstallForce(FeatureLocalInstallRequest featureLocalInstallRequest, IFeatureLocalInstall iFeatureLocalInstall) {
        startInstallForce(featureLocalInstallRequest, iFeatureLocalInstall, null);
    }

    public void startInstallForce(FeatureLocalInstallRequest featureLocalInstallRequest, IFeatureLocalInstall iFeatureLocalInstall, Handler handler) {
        if (featureLocalInstallRequest == null || iFeatureLocalInstall == null) {
            throw new IllegalArgumentException("params must not be null.");
        }
        InstallBgExecutor.getExecutor().execute(new k(this, iFeatureLocalInstall, handler, featureLocalInstallRequest));
    }
}
