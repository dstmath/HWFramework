package com.huawei.android.view;

import android.graphics.Rect;
import android.os.Binder;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.widget.CachingIconView;
import com.huawei.android.os.UserHandleEx;
import com.huawei.annotation.HwSystemApi;

public class ViewEx {
    public static final int NAVIGATION_BAR_TRANSLUCENT = Integer.MIN_VALUE;
    @HwSystemApi
    public static final int NAVIGATION_BAR_TRANSPARENT = 32768;
    @HwSystemApi
    public static final int STATUS_BAR_DISABLE_BACK = 4194304;
    public static final int STATUS_BAR_DISABLE_CLOCK = 8388608;
    public static final int STATUS_BAR_DISABLE_CLOCK_FLAG = 5;
    public static final int STATUS_BAR_DISABLE_EXPAND_FLAG = 0;
    @HwSystemApi
    public static final int STATUS_BAR_DISABLE_HOME = 2097152;
    public static final int STATUS_BAR_DISABLE_HOME_FLAG = 3;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_ALERTS_FLAG = 2;
    public static final int STATUS_BAR_DISABLE_NOTIFICATION_ICONS_FLAG = 1;
    @HwSystemApi
    public static final int STATUS_BAR_DISABLE_RECENT = 16777216;
    public static final int STATUS_BAR_DISABLE_RECENT_FLAG = 4;
    public static final int STATUS_BAR_DISABLE_SEARCH_FLAG = 6;
    @HwSystemApi
    public static final int STATUS_BAR_TRANSLUCENT = 1073741824;
    @HwSystemApi
    public static final int STATUS_BAR_TRANSPARENT = 8;
    private static final float UNSCALED_RATIO = 1.0f;

    public static int getStatusBarFlag(int flag) {
        switch (flag) {
            case 0:
                return 65536;
            case 1:
                return 131072;
            case 2:
                return 262144;
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

    public static void getBoundsOnScreen(View view, Rect outRect, boolean isClipToParent) {
        view.getBoundsOnScreen(outRect, isClipToParent);
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

    public static void setForceHidden(ImageView view, boolean isForceHidden) {
        if (view instanceof CachingIconView) {
            ((CachingIconView) view).setForceHidden(isForceHidden);
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

    @HwSystemApi
    public static boolean dispatchPointerEvent(View view, MotionEvent event) {
        return view.dispatchPointerEvent(event);
    }

    public static boolean performHwHapticFeedback(View view, int feedbackConstant, int flags) {
        return view.performHapticFeedback(UserHandleEx.PER_USER_RANGE + feedbackConstant, flags);
    }

    @HwSystemApi
    public static ViewRootImplEx getViewRootImpl(View view) {
        ViewRootImpl viewRoot = view.getViewRootImpl();
        if (viewRoot == null) {
            return null;
        }
        ViewRootImplEx viewRootEx = new ViewRootImplEx();
        viewRootEx.setViewRootImpl(viewRoot);
        return viewRootEx;
    }

    public static void setDragAcceptableMimeType(String[] mimeTypes, View view) {
        if (mimeTypes != null && view != null) {
            view.setDragAcceptableMimeType(mimeTypes);
        }
    }

    public static void startMovingTask(View view, float startX, float startY) {
        if (view != null && view.getContext() != null && view.getContext().checkPermission("android.permission.MANAGE_ACTIVITY_STACKS", Binder.getCallingPid(), Binder.getCallingUid()) == 0) {
            view.startMovingTask(getUnscaledCordinate(view, startX), getUnscaledCordinate(view, startY));
        }
    }

    private static float getUnscaledCordinate(View view, float coordinate) {
        float windowScale;
        if (view == null || view.getDisplay() == null || view.getDisplay().getDisplayAdjustments() == null) {
            windowScale = UNSCALED_RATIO;
        } else {
            windowScale = view.getDisplay().getDisplayAdjustments().getCompatibilityInfo().getSdrLowResolutionRatio();
        }
        if (windowScale != UNSCALED_RATIO) {
            return coordinate / windowScale;
        }
        return coordinate;
    }

    public static void setBlurEnabled(View view, boolean isEnabled) {
        if (WindowManagerEx.getBlurFeatureEnabled() && view != null) {
            view.setBlurEnabled(isEnabled);
        }
    }

    public static void setShadowClip(View view, boolean isEnabled) {
        if (WindowManagerEx.getBlurFeatureEnabled() && view != null) {
            view.setShadowClip(isEnabled);
        }
    }

    public static void setBlurCornerRadius(View view, int roundXdp, int roundYdp) {
        if (WindowManagerEx.getBlurFeatureEnabled() && view != null) {
            view.setBlurCornerRadius(roundXdp, roundYdp);
        }
    }
}
