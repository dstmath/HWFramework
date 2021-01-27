package ohos.global.text.format;

import android.content.Context;
import android.text.format.DateFormat;
import ohos.hiviewdfx.HiLogLabel;

public class DateFormatUtilAdapter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "DateFormatUtilAdapter");

    public static boolean is24HourFormat(Object obj) {
        if (obj instanceof Context) {
            return DateFormat.is24HourFormat((Context) obj);
        }
        return false;
    }
}
