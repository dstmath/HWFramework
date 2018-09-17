package com.huawei.android.app;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import huawei.com.android.internal.app.HwActionBarImpl;
import huawei.com.android.internal.app.HwActionBarImpl.HwTabImpl;
import huawei.com.android.internal.app.HwActionBarImpl.InnerOnStageChangedListener;

public class ActionBarEx {
    public static final int DISPLAY_HW_NO_SPLIT_LINE = 32768;

    public interface OnStageChangedListener extends InnerOnStageChangedListener {
    }

    public static void setStartIcon(ActionBar actionBar, boolean icon1Visible, Drawable icon1, OnClickListener listener1) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setStartIcon(icon1Visible, icon1, listener1);
        }
    }

    public static void setEndIcon(ActionBar actionBar, boolean icon2Visible, Drawable icon2, OnClickListener listener2) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setEndIcon(icon2Visible, icon2, listener2);
        }
    }

    public static void setCustomTitle(ActionBar actionBar, View view) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setCustomTitle(view);
        }
    }

    public static void setStartContentDescription(ActionBar actionBar, CharSequence contentDescription) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setStartContentDescription(contentDescription);
        }
    }

    public static void setEndContentDescription(ActionBar actionBar, CharSequence contentDescription) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setEndContentDescription(contentDescription);
        }
    }

    public static HwActionBarImpl getHwActionBarImpl(ActionBar actionBar) {
        if (actionBar == null || !(actionBar instanceof HwActionBarImpl)) {
            return null;
        }
        return (HwActionBarImpl) actionBar;
    }

    public static void setCustomDragView(ActionBar actionBar, View view) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setCustomDragView(view);
        }
    }

    public static void setCustomDragView(ActionBar actionBar, View view, View secondView) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setCustomDragView(view, secondView);
        }
    }

    public static void startStageAnimation(ActionBar actionBar, int stage, boolean isScrollDown) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.startStageAnimation(stage, isScrollDown);
        }
    }

    public static void setCanDragFromContent(ActionBar actionBar, boolean canDragFromContent) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setCanDragFromContent(canDragFromContent);
        }
    }

    public static void setStillView(ActionBar actionBar, View view, boolean isStill) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setStillView(view, isStill);
        }
    }

    public static int getDragAnimationStage(ActionBar actionBar) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            return abl.getDragAnimationStage();
        }
        return 0;
    }

    public static void setStageChangedCallBack(ActionBar actionBar, OnStageChangedListener callback) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setStageChangedCallBack(callback);
        }
    }

    public static void setStartStageChangedCallBack(ActionBar actionBar, OnStageChangedListener callback) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setStartStageChangedCallBack(callback);
        }
    }

    public static void resetDragAnimation(ActionBar actionBar) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.resetDragAnimation();
        }
    }

    public static void setLazyMode(ActionBar actionBar, boolean isLazyMode) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setLazyMode(isLazyMode);
        }
    }

    public static void setActionBarDraggable(ActionBar actionBar, boolean isDraggable) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setActionBarDraggable(isDraggable);
        }
    }

    public static void setTabScrollingOffsets(ActionBar actionBar, int index, float offset) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setTabScrollingOffsets(index, offset);
        }
    }

    public static void setTabViewId(Tab tab, int id) {
        if (tab instanceof HwTabImpl) {
            ((HwTabImpl) tab).setTabViewId(id);
        }
    }

    public static int getTabViewId(Tab tab) {
        if (tab instanceof HwTabImpl) {
            return ((HwTabImpl) tab).getTabViewId();
        }
        return -1;
    }

    public static void setSplitViewLocation(ActionBar actionBar, int start, int end) {
        HwActionBarImpl abl = getHwActionBarImpl(actionBar);
        if (abl != null) {
            abl.setSplitViewLocation(start, end);
        }
    }

    public static void setShowHideAnimationEnabled(ActionBar actionBar, boolean enabled) {
        actionBar.setShowHideAnimationEnabled(enabled);
    }
}
