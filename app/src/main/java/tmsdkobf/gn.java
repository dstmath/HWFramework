package tmsdkobf;

import android.text.TextUtils;

/* compiled from: Unknown */
public class gn {
    public static int aI(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        int intValue;
        try {
            intValue = Integer.valueOf(str).intValue();
        } catch (NumberFormatException e) {
            intValue = 0;
        }
        return intValue >= 0 ? intValue : 0;
    }
}
