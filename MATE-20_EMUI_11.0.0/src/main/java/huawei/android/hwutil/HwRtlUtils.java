package huawei.android.hwutil;

import android.text.TextUtils;
import java.util.Locale;

public class HwRtlUtils {
    public static String mirrorDirection(String path, String dSign) {
        if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != 1) {
            return path;
        }
        return path.replaceAll(dSign, "‏" + dSign);
    }
}
