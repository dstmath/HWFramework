package defpackage;

import android.content.Context;
import com.huawei.android.feature.install.InstallStorageManager;
import com.huawei.android.feature.module.DynamicModuleInfo;

/* renamed from: e  reason: default package */
public final class e implements Runnable {
    final /* synthetic */ Context b;
    final /* synthetic */ DynamicModuleInfo d;

    public e(Context context, DynamicModuleInfo dynamicModuleInfo) {
        this.b = context;
        this.d = dynamicModuleInfo;
    }

    public final void run() {
        InstallStorageManager.deleteFile(InstallStorageManager.getIsolatedModuleDir(this.b, this.d.mModuleName));
    }
}
