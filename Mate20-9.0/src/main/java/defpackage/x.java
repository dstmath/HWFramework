package defpackage;

import android.util.Log;
import com.huawei.android.feature.install.InstallRequest;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallManager;
import com.huawei.android.feature.install.localinstall.FeatureLocalInstallRequest;
import com.huawei.android.feature.install.localinstall.IFeatureLocalInstall;
import com.huawei.android.feature.install.localinstall.PathParser;
import com.huawei.android.feature.module.DynamicModuleManager;
import java.io.File;
import java.io.IOException;
import java.util.List;

/* renamed from: x  reason: default package */
public final class x implements Runnable {
    final /* synthetic */ InstallRequest k;
    final /* synthetic */ IFeatureLocalInstall v;
    final /* synthetic */ boolean w;
    final /* synthetic */ FeatureLocalInstallManager x;

    public x(FeatureLocalInstallManager featureLocalInstallManager, IFeatureLocalInstall iFeatureLocalInstall, InstallRequest installRequest, boolean z) {
        this.x = featureLocalInstallManager;
        this.v = iFeatureLocalInstall;
        this.k = installRequest;
        this.w = z;
    }

    public final void run() {
        this.x.notifyFeatureInstallBegin(this.v);
        List<FeatureLocalInstallRequest> installRequestModules = this.k.getInstallRequestModules();
        if (installRequestModules.size() > 100) {
            Log.e(FeatureLocalInstallManager.TAG, "feature nums exceed the limit");
            return;
        }
        for (FeatureLocalInstallRequest featureLocalInstallRequest : installRequestModules) {
            try {
                PathParser pathParser = this.x.mPathFactory.getPathParser(this.x.mContext, featureLocalInstallRequest.getPath());
                int parsePath = pathParser.parsePath();
                if (parsePath != 0) {
                    this.x.notifyFeatureInstallStatus(featureLocalInstallRequest.getFeatureName(), parsePath, this.v);
                } else {
                    int dynamicFeatureState = DynamicModuleManager.getInstance().getDynamicFeatureState(featureLocalInstallRequest.getFeatureName());
                    if (!(dynamicFeatureState == 0 || 5 == dynamicFeatureState)) {
                        parsePath = -18;
                    }
                    if (parsePath != 0) {
                        this.x.notifyFeatureInstallStatus(featureLocalInstallRequest.getFeatureName(), parsePath, this.v);
                    } else {
                        File loadingFile = pathParser.getLoadingFile();
                        if (loadingFile == null) {
                            this.x.notifyFeatureInstallStatus(featureLocalInstallRequest.getFeatureName(), -10, this.v);
                        } else {
                            this.x.addInstallFeatureState(featureLocalInstallRequest.getFeatureName());
                            File file = new File(InstallStorageManager.getUnverifyApksDir(this.x.mContext), loadingFile.getName());
                            try {
                                if (this.w) {
                                    FeatureLocalInstallManager.copy(loadingFile, file);
                                    Log.d(FeatureLocalInstallManager.TAG, "copy apk file finished");
                                } else {
                                    Log.d(FeatureLocalInstallManager.TAG, "rename : " + loadingFile.renameTo(file));
                                }
                            } catch (IOException e) {
                                Log.e(FeatureLocalInstallManager.TAG, e.toString());
                                this.x.procFeatureInstallEnd(featureLocalInstallRequest.getFeatureName(), -17, this.v);
                            }
                            this.x.procFeatureInstallEnd(featureLocalInstallRequest.getFeatureName(), DynamicModuleManager.installUnverifyFeatures(this.x.mContext, loadingFile.getName(), featureLocalInstallRequest.getSignature()), this.v);
                        }
                    }
                }
            } catch (IllegalArgumentException e2) {
                Log.e(FeatureLocalInstallManager.TAG, e2.toString());
                this.x.notifyFeatureInstallStatus(featureLocalInstallRequest.getFeatureName(), -19, this.v);
            }
        }
        this.x.notifyFeatureInstallEnd(this.v);
    }
}
