package com.huawei.android.feature.install;

import android.content.Context;
import com.huawei.android.feature.install.config.FeatureInstallConfig;
import java.io.File;

public class InstallUpdate {
    public static int checkUpdateInstall() {
        return 0;
    }

    public static int makeUpdateFeaturePkg(Context context, String str, File file) {
        return file.renameTo(new File(InstallStorageManager.getIsolateUpdateDir(context, str), new StringBuilder().append(str).append(FeatureInstallConfig.getSuffix(file.getName())).toString())) ? -14 : -17;
    }
}
