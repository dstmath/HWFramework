package ohos.global.text.format;

import ohos.app.Context;

public class DateFormatUtilImpl extends DateFormatUtil {
    public static boolean is24HourFormat(Context context) {
        return DateFormatUtilAdapter.is24HourFormat(context.getHostContext());
    }
}
