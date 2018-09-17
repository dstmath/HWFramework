package tmsdkobf;

import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import java.io.File;

/* compiled from: Unknown */
public class ne {
    public static Object a(Object obj, File file, String str, DisplayMetrics displayMetrics, int i) {
        try {
            if (VERSION.SDK_INT < 21) {
                return ng.a(obj, "parsePackage", new Object[]{file, str, displayMetrics, Integer.valueOf(i)});
            }
            return ng.a(obj, "parsePackage", new Object[]{file, Integer.valueOf(i)});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object cI(String str) {
        try {
            if (VERSION.SDK_INT >= 21) {
                return ng.a("android.content.pm.PackageParser", null);
            }
            return ng.a("android.content.pm.PackageParser", new Object[]{str});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
