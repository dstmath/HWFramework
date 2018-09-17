package huawei.com.android.internal.widget;

import android.content.Context;

public class HwWidgetUtils {
    private static final int ACTIONBAR_BACKGROUND_THEMED_FLAG = 0;

    public static final boolean isActionbarBackgroundThemed(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        if (context.getResources().getColor(33882153) == 0) {
            z = true;
        }
        return z;
    }
}
