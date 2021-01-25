package ohos.agp.utils;

import android.icu.util.ULocale;
import android.os.SystemProperties;
import com.android.internal.util.ArrayUtils;
import java.util.Locale;

public class TextToolHelper {
    private static final String DEVELOPMENT_FORCE_RTL = "debug.force_rtl";

    public static boolean isRightToLeft(Locale locale) {
        return ULocale.forLocale(locale).isRightToLeft();
    }

    public static boolean sysGetBooleanRTL() {
        return SystemProperties.getBoolean(DEVELOPMENT_FORCE_RTL, false);
    }

    public static char[] newCharArray(int i) {
        return ArrayUtils.newUnpaddedCharArray(i);
    }
}
