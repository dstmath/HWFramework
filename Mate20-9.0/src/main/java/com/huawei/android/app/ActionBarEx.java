package com.huawei.android.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SpinnerAdapter;
import com.android.internal.app.WindowDecorActionBar;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.HwToolbar;
import huawei.com.android.internal.app.HwActionBarImpl;

public class ActionBarEx {
    public static final int DISPLAY_HW_NO_SPLIT_LINE = 32768;

    public interface OnStageChangedListener extends HwActionBarImpl.InnerOnStageChangedListener {
    }

    public static void setStartIcon(ActionBar actionBar, boolean icon1Visible, Drawable icon1, View.OnClickListener listener1) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setStartIcon(icon1Visible, icon1, listener1);
            setIconLayout(actionBar, toolbar);
        }
    }

    public static void setEndIcon(ActionBar actionBar, boolean icon2Visible, Drawable icon2, View.OnClickListener listener2) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setEndIcon(icon2Visible, icon2, listener2);
            setIconLayout(actionBar, toolbar);
        }
    }

    public static void setCustomTitle(ActionBar actionBar, View view) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setCustomTitle(view);
            setIconLayout(actionBar, toolbar);
        }
    }

    public static void setStartContentDescription(ActionBar actionBar, CharSequence contentDescription) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setStartContentDescription(contentDescription);
        }
    }

    public static void setEndContentDescription(ActionBar actionBar, CharSequence contentDescription) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setEndContentDescription(contentDescription);
        }
    }

    public static void setAppbarBackground(ActionBar actionBar, Drawable drawable) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setBackground(drawable);
        }
    }

    public static HwActionBarImpl getHwActionBarImpl(ActionBar actionBar) {
        return null;
    }

    private static HwToolbar getToolbar(ActionBar actionBar) {
        if (!(actionBar instanceof WindowDecorActionBar)) {
            return null;
        }
        View toolbar = ((Activity) ReflectUtil.getObject(actionBar, "mActivity", WindowDecorActionBar.class)).getWindow().getDecorView().findViewById(16908692);
        if (toolbar instanceof HwToolbar) {
            return (HwToolbar) toolbar;
        }
        return null;
    }

    public static void setCustomDragView(ActionBar actionBar, View view) {
    }

    public static void setCustomDragView(ActionBar actionBar, View view, View secondView) {
    }

    public static void setDisplySpinnerMode(ActionBar actionBar, int contentId, AdapterView.OnItemSelectedListener listener) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setDisplaySpinner(contentId, listener);
            actionBar.setDisplayShowTitleEnabled(false);
            setIconLayout(actionBar, toolbar);
        }
    }

    public static SpinnerAdapter getSpinnerAdapter(ActionBar actionBar) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            return toolbar.getSpinnerAdapter();
        }
        return null;
    }

    public static int getDropdownSelectedPosition(ActionBar actionBar) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            return toolbar.getDropdownSelectedPosition();
        }
        return 0;
    }

    public static int getDropdownItemCount(ActionBar actionBar) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            return toolbar.getDropdownItemCount();
        }
        return 0;
    }

    public static void setSplitToolbarForce(ActionBar actionBar, boolean forceSplit) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setSplitToolbarForce(forceSplit);
        }
    }

    public static void setDynamicSplitToolbar(ActionBar actionBar, boolean split) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setDynamicSplitMenu(split);
        }
    }

    public static void startStageAnimation(ActionBar actionBar, int stage, boolean isScrollDown) {
    }

    public static void setCanDragFromContent(ActionBar actionBar, boolean canDragFromContent) {
    }

    public static void setStillView(ActionBar actionBar, View view, boolean isStill) {
    }

    public static int getDragAnimationStage(ActionBar actionBar) {
        return 0;
    }

    public static void setStageChangedCallBack(ActionBar actionBar, OnStageChangedListener callback) {
    }

    public static void setStartStageChangedCallBack(ActionBar actionBar, OnStageChangedListener callback) {
    }

    public static void resetDragAnimation(ActionBar actionBar) {
    }

    public static void setLazyMode(ActionBar actionBar, boolean isLazyMode) {
    }

    public static void setActionBarDraggable(ActionBar actionBar, boolean isDraggable) {
    }

    public static void setTabScrollingOffsets(ActionBar actionBar, int index, float offset) {
    }

    public static void setTabViewId(ActionBar.Tab tab, int id) {
    }

    public static int getTabViewId(ActionBar.Tab tab) {
        return 0;
    }

    public static void setSplitViewLocation(ActionBar actionBar, int start, int end) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setSplitViewLocation(start, end);
        }
    }

    public static void setShowHideAnimationEnabled(ActionBar actionBar, boolean enabled) {
        if (actionBar != null) {
            actionBar.setShowHideAnimationEnabled(enabled);
        }
    }

    public static void setSpiltViewBlurEnable(ActionBar actionBar, boolean enable) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null && toolbar.getSplieView() != null) {
            toolbar.getSplieView().setBlurEnable(enable);
        }
    }

    public static boolean isSpiltViewBlurEnable(ActionBar actionBar) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar == null || toolbar.getSplieView() == null) {
            return false;
        }
        return toolbar.getSplieView().isBlurEnable();
    }

    public static void setBlurEnable(ActionBar actionBar, boolean enable) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setBlurEnable(enable);
        }
    }

    public static boolean isBlurEnable(ActionBar actionBar) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            return toolbar.isBlurEnable();
        }
        return false;
    }

    public static void setBlurColor(ActionBar actionBar, int blurColor) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setBlurColor(blurColor);
        }
    }

    public static void setSplitViewBlurColor(ActionBar actionBar, int blurColor) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null && toolbar.getSplieView() != null) {
            toolbar.getSplieView().setBlurColor(blurColor);
        }
    }

    public static void setSmartColor(ActionBar actionBar, ColorStateList iconColor, ColorStateList titleColor) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setSmartColor(iconColor, titleColor);
        }
    }

    private static void setIconLayout(ActionBar actionBar, HwToolbar toolbar) {
        if (actionBar != null && toolbar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            if (toolbar.getIconLayout() != null) {
                actionBar.setCustomView(toolbar.getIconLayout(), new ActionBar.LayoutParams(-1, -2));
            }
        }
    }
}
