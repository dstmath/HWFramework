package com.huawei.android.view;

import android.graphics.Rect;
import android.trustspace.TrustSpaceManager;
import android.view.View;
import com.huawei.android.app.AppOpsManagerEx;

public class ViewEx {
    public static final int STATUS_BAR_DISABLE_CLOCK = 8388608;
    public static final int STATUS_BAR_DISABLE_CLOCK_FLAG = 5;
    public static final int STATUS_BAR_DISABLE_EXPAND_FLAG = 0;
    public static final int STATUS_BAR_DISABLE_HOME_FLAG = 3;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_ALERTS_FLAG = 2;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_ICONS_FLAG = 1;
    public static final int STATUS_BAR_DISABLE_RECENT_FLAG = 4;
    public static final int STATUS_BAR_DISABLE_SEARCH_FLAG = 6;

    public static int getStatusBarFlag(int flag) {
        switch (flag) {
            case 0:
                return 65536;
            case 1:
                return 131072;
            case 2:
                return AppOpsManagerEx.TYPE_DELETE_CALLLOG;
            case 3:
                return 2097152;
            case 4:
                return TrustSpaceManager.FLAG_HW_TRUSTSPACE;
            case 5:
                return 8388608;
            case 6:
                return 33554432;
            default:
                return 0;
        }
    }

    public static void requestAccessibilityFocus(View view) {
        view.requestAccessibilityFocus();
    }

    public static void getBoundsOnScreen(View view, Rect outRect, boolean clipToParent) {
        view.getBoundsOnScreen(outRect, clipToParent);
    }

    public static void setTouchInOtherThread(View view, boolean touchable) {
        if (view != null) {
            view.setTouchInOtherThread(touchable);
        }
    }
}
