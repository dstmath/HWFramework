package tmsdkobf;

import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import java.io.File;

public class mf {
    public static Object a(Object obj, File file, String str, DisplayMetrics displayMetrics, int i) {
        try {
            if (VERSION.SDK_INT < 21) {
                return mh.a(obj, "parsePackage", new Object[]{file, str, displayMetrics, Integer.valueOf(i)});
            }
            return mh.a(obj, "parsePackage", new Object[]{file, Integer.valueOf(i)});
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public static Object bW(String str) {
        try {
            if (VERSION.SDK_INT >= 21) {
                return mh.a("android.content.pm.PackageParser", null);
            }
            return mh.a("android.content.pm.PackageParser", new Object[]{str});
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
}
