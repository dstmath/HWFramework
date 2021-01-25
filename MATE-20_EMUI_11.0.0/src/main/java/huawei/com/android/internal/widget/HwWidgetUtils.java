package huawei.com.android.internal.widget;

import android.content.Context;

public class HwWidgetUtils {
    private static final int ACTIONBAR_BACKGROUND_THEMED_FLAG = 0;

    private HwWidgetUtils() {
    }

    public static final boolean isActionbarBackgroundThemed(Context context) {
        if (context != null && context.getResources().getColor(33882153) == 0) {
            return true;
        }
        return false;
    }
}
