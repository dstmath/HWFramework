package defpackage;

import android.util.Log;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.install.InstallUpdate;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallRequest;
import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;
import com.huawei.android.feature.install.localinstall.PathParser;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.io.File;

/* renamed from: y  reason: default package */
public final class y implements Runnable {
    final /* synthetic */ IFeatureLocalInstall v;
    final /* synthetic */ FeatureLocalInstallManager x;
    final /* synthetic */ FeatureLocalInstallRequest y;

    public y(FeatureLocalInstallManager featureLocalInstallManager, IFeatureLocalInstall iFeatureLocalInstall, FeatureLocalInstallRequest featureLocalInstallRequest) {
        this.x = featureLocalInstallManager;
        this.v = iFeatureLocalInstall;
        this.y = featureLocalInstallRequest;
    }

    public final void run() {
        int installUnverifyFeatures;
        this.x.notifyFeatureInstallBegin(this.v);
        try {
            PathParser pathParser = this.x.mPathFactory.getPathParser(this.x.mContext, this.y.getPath());
            int parsePath = pathParser.parsePath();
            if (parsePath != 0) {
                this.x.notifyFeatureInstallStatus(this.y.getFeatureName(), parsePath, this.v);
                this.x.notifyFeatureInstallEnd(this.v);
                return;
            }
            File loadingFile = pathParser.getLoadingFile();
            if (loadingFile == null) {
                this.x.notifyFeatureInstallStatus(this.y.getFeatureName(), -10, this.v);
                this.x.notifyFeatureInstallEnd(this.v);
                return;
            }
            int dynamicFeatureState = DynamicModuleManager.getInstance().getDynamicFeatureState(this.y.getFeatureName());
            if (dynamicFeatureState == 0 || 5 == dynamicFeatureState) {
                this.x.addInstallFeatureState(this.y.getFeatureName());
                if (5 == dynamicFeatureState) {
                    installUnverifyFeatures = InstallUpdate.makeUpdateFeaturePkg(this.x.mContext, this.y.getFeatureName(), loadingFile);
                } else {
                    Log.d(FeatureLocalInstallManager.TAG, "rename : " + loadingFile.renameTo(new File(InstallStorageManager.getUnverifyApksDir(this.x.mContext), loadingFile.getName())));
                    installUnverifyFeatures = DynamicModuleManager.installUnverifyFeatures(this.x.mContext, loadingFile.getName(), this.y.getSignature());
                }
                this.x.procFeatureInstallEnd(this.y.getFeatureName(), installUnverifyFeatures, this.v);
                this.x.notifyFeatureInstallEnd(this.v);
                return;
            }
            this.x.notifyFeatureInstallStatus(this.y.getFeatureName(), -18, this.v);
            this.x.notifyFeatureInstallEnd(this.v);
        } catch (IllegalArgumentException e) {
            Log.e(FeatureLocalInstallManager.TAG, e.toString());
            this.x.notifyFeatureInstallStatus(this.y.getFeatureName(), -19, this.v);
            this.x.notifyFeatureInstallEnd(this.v);
        }
    }
}
