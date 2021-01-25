package defpackage;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/* renamed from: aj  reason: default package */
public final class aj {
    private final PackageManager S;

    public aj(Context context) {
        this.S = context.getPackageManager();
    }

    /* access modifiers changed from: package-private */
    public final byte[] a(String str) {
        try {
            PackageInfo packageInfo = this.S.getPackageInfo(str, 64);
            if (!(packageInfo == null || packageInfo.signatures == null || packageInfo.signatures.length <= 0)) {
                return packageInfo.signatures[0].toByteArray();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("PushLogSys", "Failed to get application signature certificate fingerprint." + e.getMessage());
        }
        Log.e("PushLogSys", "Failed to get application signature certificate fingerprint.");
        return new byte[0];
    }
}
