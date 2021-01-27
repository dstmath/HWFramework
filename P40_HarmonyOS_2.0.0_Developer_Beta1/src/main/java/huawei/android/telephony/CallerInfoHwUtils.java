package huawei.android.telephony;

import android.database.Cursor;

public class CallerInfoHwUtils {
    public static final int MIN_MATCH = 7;

    public static boolean isFixedIndexValid(String cookie, Cursor cursor) {
        return CallerInfoHW.isfixedIndexValid(cookie, cursor);
    }
}
