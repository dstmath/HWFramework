package tmsdk.common.utils;

import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import java.io.File;
import tmsdkobf.mh;

public class k {
    public static Object a(Object obj, File file, String str, DisplayMetrics displayMetrics, int i) {
        try {
            if (VERSION.SDK_INT < 21) {
                return mh.a(obj, "parsePackage", new Object[]{file, str, displayMetrics, Integer.valueOf(i)});
            }
            return mh.a(obj, "parsePackage", new Object[]{file, Integer.valueOf(i)});
        } catch (Throwable e) {
            f.b("--PackageUtil--", e.getMessage(), e);
            return null;
        }
    }

    public static Object bW(String str) {
        try {
            if (VERSION.SDK_INT >= 21) {
                return mh.a("android.content.pm.PackageParser", null);
            }
            return mh.a("android.content.pm.PackageParser", new Object[]{str});
        } catch (Throwable e) {
            f.b("--PackageUtil--", e.getMessage(), e);
            return null;
        }
    }
}
