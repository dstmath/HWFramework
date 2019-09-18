package com.huawei.android.view;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.widget.CachingIconView;
import com.huawei.android.app.AppOpsManagerEx;

public class ViewEx {
    public static final int NAVIGATION_BAR_TRANSLUCENT = Integer.MIN_VALUE;
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
                return 16777216;
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

    public static void setIsRootNamespace(View view, boolean isRoot) {
        if (view != null) {
            view.setIsRootNamespace(isRoot);
        }
    }

    public static final int getUndefinedLayoutDirection() {
        return -1;
    }

    public static boolean isVisibleToUser(View view) {
        if (view != null) {
            return view.isVisibleToUser();
        }
        return false;
    }

    public static void removeTransientView(ViewGroup viewGroup, View view) {
        if (viewGroup != null) {
            viewGroup.removeTransientView(view);
        }
    }

    public static int getTransientViewCount(ViewGroup viewGroup) {
        if (viewGroup != null) {
            return viewGroup.getTransientViewCount();
        }
        return 0;
    }

    public static void addTransientView(ViewGroup viewGroup, View view, int index) {
        if (viewGroup != null) {
            viewGroup.addTransientView(view, index);
        }
    }

    public static void setForceHidden(ImageView view, boolean forceHidden) {
        if (view instanceof CachingIconView) {
            ((CachingIconView) view).setForceHidden(forceHidden);
        }
    }

    public static boolean performAccessibilityAction(View view, int action, Bundle arguments) {
        if (view != null) {
            return view.performAccessibilityActionInternal(action, arguments);
        }
        return false;
    }

    public static void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo info) {
        if (view != null) {
            view.onInitializeAccessibilityNodeInfoInternal(info);
        }
    }

    public static boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View child, AccessibilityEvent event) {
        if (viewGroup != null) {
            return viewGroup.onRequestSendAccessibilityEventInternal(child, event);
        }
        return false;
    }

    public static boolean dispatchPopulateAccessibilityEvent(LinearLayout linearLayout, AccessibilityEvent event) {
        if (linearLayout != null) {
            return linearLayout.dispatchPopulateAccessibilityEventInternal(event);
        }
        return false;
    }
}
