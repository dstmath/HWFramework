package com.huawei.android.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SpinnerAdapter;
import com.android.internal.app.WindowDecorActionBar;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.HwToolbar;
import huawei.com.android.internal.app.HwActionBarImpl;
import huawei.com.android.internal.widget.HwActionBarContextView;

public class ActionBarEx {
    public static final int DISPLAY_HW_NO_SPLIT_LINE = 32768;

    public interface OnStageChangedListener extends HwActionBarImpl.InnerOnStageChangedListener {
    }

    public static void setStartIcon(ActionBar actionBar, boolean isVisibleIcon, Drawable startDrawable, View.OnClickListener clickListener) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setStartIcon(isVisibleIcon, startDrawable, clickListener);
            setIconLayout(actionBar, toolbar);
        }
    }

    public static void setEndIcon(ActionBar actionBar, boolean isVisibleIcon, Drawable endDrawable, View.OnClickListener clickListener) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setEndIcon(isVisibleIcon, endDrawable, clickListener);
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
        Object object = ReflectUtil.getObject(actionBar, "mActivity", WindowDecorActionBar.class);
        if (object instanceof Activity) {
            View toolbar = ((Activity) object).getWindow().getDecorView().findViewById(16908718);
            if (toolbar instanceof HwToolbar) {
                return (HwToolbar) toolbar;
            }
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

    public static void setSplitToolbarForce(ActionBar actionBar, boolean isForceSplit) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setSplitToolbarForce(isForceSplit);
        }
    }

    public static void setDynamicSplitToolbar(ActionBar actionBar, boolean isSplit) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setDynamicSplitMenu(isSplit);
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

    public static void setShowHideAnimationEnabled(ActionBar actionBar, boolean isEnabled) {
        if (actionBar != null) {
            actionBar.setShowHideAnimationEnabled(isEnabled);
        }
    }

    public static void setSpiltViewBlurEnable(ActionBar actionBar, boolean isEnable) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null && toolbar.getSplieView() != null) {
            toolbar.getSplieView().setBlurEnable(isEnable);
        }
    }

    public static boolean isSpiltViewBlurEnable(ActionBar actionBar) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar == null || toolbar.getSplieView() == null) {
            return false;
        }
        return toolbar.getSplieView().isBlurEnable();
    }

    public static void setBlurEnable(ActionBar actionBar, boolean isEnable) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setBlurEnable(isEnable);
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

    public static void setEditPositiveEnabled(ActionBar actionBar, boolean isEnabled) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setPositiveEnabled(isEnabled);
        }
    }

    public static void setMultiPositiveEnabled(ActionBar actionBar, boolean isEnabled) {
        HwActionBarContextView toolbar = getHwActionBarContextView(actionBar);
        if (toolbar != null) {
            toolbar.setPositiveEnabled(isEnabled);
        }
    }

    private static HwActionBarContextView getHwActionBarContextView(ActionBar actionBar) {
        if (!(actionBar instanceof WindowDecorActionBar)) {
            return null;
        }
        Object object = ReflectUtil.getObject(actionBar, "mActivity", WindowDecorActionBar.class);
        if (object instanceof Activity) {
            View contextView = ((Activity) object).getWindow().getDecorView().findViewById(16908723);
            if (contextView instanceof HwActionBarContextView) {
                return (HwActionBarContextView) contextView;
            }
        }
        return null;
    }

    public static void setBubbleCount(ActionBar actionBar, int count) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setBubbleCount(count);
        }
    }

    public static void setColumnEnabled(ActionBar actionBar, boolean isEnabled) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.setColumnEnabled(isEnabled);
        }
    }

    public static boolean isColumnEnabled(ActionBar actionBar) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            return toolbar.isColumnEnabled();
        }
        return false;
    }

    public static void configureColumn(ActionBar actionBar, int width, int height, float density) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar != null) {
            toolbar.configureColumn(width, height, density);
        }
    }

    public static void setSplitViewCornerInsets(ActionBar actionBar, Rect rect) {
        HwToolbar toolbar;
        if (rect != null && (toolbar = getToolbar(actionBar)) != null && toolbar.getSplieView() != null) {
            toolbar.getSplieView().setCornerInsets(rect);
        }
    }

    public static Rect getSplitViewCornerInsets(ActionBar actionBar) {
        HwToolbar toolbar = getToolbar(actionBar);
        if (toolbar == null || toolbar.getSplieView() == null) {
            return new Rect();
        }
        return toolbar.getSplieView().getCornerInsets();
    }
}
