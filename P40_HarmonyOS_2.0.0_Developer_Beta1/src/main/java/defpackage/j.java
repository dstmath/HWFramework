package defpackage;

import android.os.Handler;
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

/* renamed from: j  reason: default package */
public final class j implements Runnable {
    final /* synthetic */ InstallRequest g;
    final /* synthetic */ IFeatureLocalInstall i;
    final /* synthetic */ Handler j;
    final /* synthetic */ boolean k;
    final /* synthetic */ FeatureLocalInstallManager l;

    public j(FeatureLocalInstallManager featureLocalInstallManager, IFeatureLocalInstall iFeatureLocalInstall, Handler handler, InstallRequest installRequest, boolean z) {
        this.l = featureLocalInstallManager;
        this.i = iFeatureLocalInstall;
        this.j = handler;
        this.g = installRequest;
        this.k = z;
    }

    @Override // java.lang.Runnable
    public final void run() {
        this.l.notifyFeatureInstallBegin(this.i, this.j);
        List<FeatureLocalInstallRequest> installRequestModules = this.g.getInstallRequestModules();
        if (installRequestModules.size() > 100) {
            Log.e(FeatureLocalInstallManager.TAG, "feature nums exceed the limit");
            return;
        }
        for (FeatureLocalInstallRequest featureLocalInstallRequest : installRequestModules) {
            try {
                PathParser pathParser = this.l.mPathFactory.getPathParser(this.l.mContext, featureLocalInstallRequest.getPath());
                int parsePath = pathParser.parsePath();
                if (parsePath != 0) {
                    this.l.notifyFeatureInstallStatus(featureLocalInstallRequest.getFeatureName(), parsePath, this.i, this.j);
                } else {
                    int dynamicFeatureState = DynamicModuleManager.getInstance().getDynamicFeatureState(featureLocalInstallRequest.getFeatureName());
                    if (!(dynamicFeatureState == 0 || 5 == dynamicFeatureState)) {
                        parsePath = -18;
                    }
                    if (parsePath != 0) {
                        this.l.notifyFeatureInstallStatus(featureLocalInstallRequest.getFeatureName(), parsePath, this.i, this.j);
                    } else {
                        File loadingFile = pathParser.getLoadingFile();
                        if (loadingFile == null) {
                            this.l.notifyFeatureInstallStatus(featureLocalInstallRequest.getFeatureName(), -10, this.i, this.j);
                        } else {
                            this.l.addInstallFeatureState(featureLocalInstallRequest.getFeatureName());
                            File file = new File(InstallStorageManager.getUnverifyApksDir(this.l.mContext), loadingFile.getName());
                            try {
                                if (this.k) {
                                    FeatureLocalInstallManager.copy(loadingFile, file);
                                    Log.d(FeatureLocalInstallManager.TAG, "copy apk file finished");
                                } else {
                                    Log.d(FeatureLocalInstallManager.TAG, "rename : ".concat(String.valueOf(loadingFile.renameTo(file))));
                                }
                            } catch (IOException e) {
                                Log.e(FeatureLocalInstallManager.TAG, e.toString());
                                this.l.procFeatureInstallEnd(featureLocalInstallRequest.getFeatureName(), -17, this.i, this.j);
                            }
                            this.l.procFeatureInstallEnd(featureLocalInstallRequest.getFeatureName(), DynamicModuleManager.installUnverifyFeatures(this.l.mContext, loadingFile.getName(), featureLocalInstallRequest.getSignature()), this.i, this.j);
                        }
                    }
                }
            } catch (IllegalArgumentException e2) {
                Log.e(FeatureLocalInstallManager.TAG, e2.toString());
                this.l.notifyFeatureInstallStatus(featureLocalInstallRequest.getFeatureName(), -19, this.i, this.j);
            }
        }
        this.l.notifyFeatureInstallEnd(this.i, this.j);
    }
}
