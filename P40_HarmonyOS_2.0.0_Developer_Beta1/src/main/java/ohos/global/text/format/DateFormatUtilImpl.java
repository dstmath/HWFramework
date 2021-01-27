package ohos.global.text.format;

import ohos.app.Context;

public class DateFormatUtilImpl {
    public static boolean is24HourFormat(Context context) {
        return DateFormatUtilAdapter.is24HourFormat(context.getHostContext());
    }
}
