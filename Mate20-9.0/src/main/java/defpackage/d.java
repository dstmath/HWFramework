package defpackage;

import com.huawei.android.feature.install.InstallStorageManager;
import java.io.File;

/* renamed from: d  reason: default package */
public final class d implements Runnable {
    final /* synthetic */ File c;

    public d(File file) {
        this.c = file;
    }

    public final void run() {
        InstallStorageManager.deleteFile(this.c.getParentFile());
    }
}
