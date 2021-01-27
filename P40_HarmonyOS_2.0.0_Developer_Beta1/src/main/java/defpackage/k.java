package defpackage;

import android.os.Handler;
import android.util.Log;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.install.InstallUpdate;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallRequest;
import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;
import com.huawei.android.feature.install.localinstall.PathParser;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.io.File;

/* renamed from: k  reason: default package */
public final class k implements Runnable {
    final /* synthetic */ IFeatureLocalInstall i;
    final /* synthetic */ Handler j;
    final /* synthetic */ FeatureLocalInstallManager l;
    final /* synthetic */ FeatureLocalInstallRequest m;

    public k(FeatureLocalInstallManager featureLocalInstallManager, IFeatureLocalInstall iFeatureLocalInstall, Handler handler, FeatureLocalInstallRequest featureLocalInstallRequest) {
        this.l = featureLocalInstallManager;
        this.i = iFeatureLocalInstall;
        this.j = handler;
        this.m = featureLocalInstallRequest;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int installUnverifyFeatures;
        this.l.notifyFeatureInstallBegin(this.i, this.j);
        try {
            PathParser pathParser = this.l.mPathFactory.getPathParser(this.l.mContext, this.m.getPath());
            int parsePath = pathParser.parsePath();
            if (parsePath != 0) {
                this.l.notifyFeatureInstallStatus(this.m.getFeatureName(), parsePath, this.i, this.j);
                this.l.notifyFeatureInstallEnd(this.i, this.j);
                return;
            }
            File loadingFile = pathParser.getLoadingFile();
            if (loadingFile == null) {
                this.l.notifyFeatureInstallStatus(this.m.getFeatureName(), -10, this.i, this.j);
                this.l.notifyFeatureInstallEnd(this.i, this.j);
                return;
            }
            int dynamicFeatureState = DynamicModuleManager.getInstance().getDynamicFeatureState(this.m.getFeatureName());
            if (dynamicFeatureState == 0 || 5 == dynamicFeatureState) {
                this.l.addInstallFeatureState(this.m.getFeatureName());
                if (5 == dynamicFeatureState) {
                    installUnverifyFeatures = InstallUpdate.makeUpdateFeaturePkg(this.l.mContext, this.m.getFeatureName(), loadingFile);
                } else {
                    Log.d(FeatureLocalInstallManager.TAG, "rename : ".concat(String.valueOf(loadingFile.renameTo(new File(InstallStorageManager.getUnverifyApksDir(this.l.mContext), loadingFile.getName())))));
                    installUnverifyFeatures = DynamicModuleManager.installUnverifyFeatures(this.l.mContext, loadingFile.getName(), this.m.getSignature());
                }
                this.l.procFeatureInstallEnd(this.m.getFeatureName(), installUnverifyFeatures, this.i, this.j);
                this.l.notifyFeatureInstallEnd(this.i, this.j);
                return;
            }
            this.l.notifyFeatureInstallStatus(this.m.getFeatureName(), -18, this.i, this.j);
            this.l.notifyFeatureInstallEnd(this.i, this.j);
        } catch (IllegalArgumentException e) {
            Log.e(FeatureLocalInstallManager.TAG, e.toString());
            this.l.notifyFeatureInstallStatus(this.m.getFeatureName(), -19, this.i, this.j);
            this.l.notifyFeatureInstallEnd(this.i, this.j);
        }
    }
}
