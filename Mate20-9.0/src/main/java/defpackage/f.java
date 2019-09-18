package defpackage;

import android.content.Context;
import com.huawei.android.feature.install.InstallStorageManager;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;

/* renamed from: f  reason: default package */
public final class f implements Runnable {
    final /* synthetic */ Context b;
    final /* synthetic */ HashSet e;
    final /* synthetic */ long f;

    public f(HashSet hashSet, Context context, long j) {
        this.e = hashSet;
        this.b = context;
        this.f = j;
    }

    public final void run() {
        Iterator it = this.e.iterator();
        while (it.hasNext()) {
            InstallStorageManager.deleteFile(new File(InstallStorageManager.getNonIsolatedVerifyDir(this.b, this.f), (String) it.next()));
        }
    }
}
