package android.support.v4.content.pm;

import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import android.support.v4.os.BuildCompat;

public final class PackageInfoCompat {
    public static long getLongVersionCode(@NonNull PackageInfo info) {
        if (BuildCompat.isAtLeastP()) {
            return info.getLongVersionCode();
        }
        return (long) info.versionCode;
    }

    private PackageInfoCompat() {
    }
}
