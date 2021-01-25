package huawei.android.widget;

import android.app.ActionBar;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SpinnerAdapter;

public class ActionBarEx {

    public interface OnStageChangedListener {
        void onEnterNextStage();

        void onExitNextStage();
    }

    public static void setStartIcon(ActionBar actionBar, HwToolbar toolbar, boolean isIcon1Visible, Drawable icon, View.OnClickListener listener) {
        toolbar.setStartIcon(isIcon1Visible, icon, listener);
        setIconLayout(actionBar, toolbar);
    }

    public static void setEndIcon(ActionBar actionBar, HwToolbar toolbar, boolean isIcon2Visible, Drawable icon, View.OnClickListener listener) {
        toolbar.setEndIcon(isIcon2Visible, icon, listener);
        setIconLayout(actionBar, toolbar);
    }

    public static void setSpiltViewBlurEnable(HwToolbar toolbar, boolean isEnabled) {
        if (toolbar.getSplieView() != null) {
            toolbar.getSplieView().setBlurEnable(isEnabled);
        }
    }

    public static boolean isSpiltViewBlurEnable(HwToolbar toolbar) {
        if (toolbar.getSplieView() != null) {
            return toolbar.getSplieView().isBlurEnable();
        }
        return false;
    }

    public static void setBlurEnable(HwToolbar toolbar, boolean isEnabled) {
        toolbar.setBlurEnable(isEnabled);
    }

    public static boolean isBlurEnable(HwToolbar toolbar) {
        return toolbar.isBlurEnable();
    }

    public static void setBlurColor(HwToolbar toolbar, int blurColor) {
        toolbar.setBlurColor(blurColor);
    }

    public static void setSplitViewBlurColor(HwToolbar toolbar, int blurColor) {
        if (toolbar.getSplieView() != null) {
            toolbar.getSplieView().setBlurColor(blurColor);
        }
    }

    public static void setStartContentDescription(HwToolbar toolbar, CharSequence contentDescription) {
        toolbar.setStartContentDescription(contentDescription);
    }

    public static void setEndContentDescription(HwToolbar toolbar, CharSequence contentDescription) {
        toolbar.setEndContentDescription(contentDescription);
    }

    public static void setCustomTitle(ActionBar actionBar, HwToolbar toolbar, View view) {
        toolbar.setCustomTitle(view);
        setIconLayout(actionBar, toolbar);
    }

    public static void setSplitViewLocation(ActionBar actionBar, HwToolbar toolbar, int start, int end) {
        toolbar.setSplitViewLocation(start, end);
    }

    public static void setSplitToolbarForce(HwToolbar toolbar, boolean shouldForceSplit) {
        toolbar.setSplitToolbarForce(shouldForceSplit);
    }

    public static void setDynamicSplitToolbar(HwToolbar toolbar, boolean shouldSplit) {
        toolbar.setDynamicSplitMenu(shouldSplit);
    }

    public static void setSplitBackgroundDrawable(HwToolbar toolbar, Drawable drawable) {
        toolbar.setSplitBackgroundDrawable(drawable);
    }

    public static void setDisplySpinnerMode(HwToolbar toolbar, ActionBar actionBar, int contentId, AdapterView.OnItemSelectedListener listener) {
        toolbar.setDisplaySpinner(contentId, listener);
        actionBar.setDisplayShowTitleEnabled(false);
        setIconLayout(actionBar, toolbar);
    }

    public static SpinnerAdapter getSpinnerAdapter(HwToolbar toolbar) {
        return toolbar.getSpinnerAdapter();
    }

    public static int getDropdownSelectedPosition(HwToolbar toolbar) {
        return toolbar.getDropdownSelectedPosition();
    }

    public static int getDropdownItemCount(HwToolbar toolbar) {
        return toolbar.getDropdownItemCount();
    }

    public static void setTabScrollingOffsets(ActionBar actionBar, int index, float offset) {
    }

    public static void setTabViewId(ActionBar.Tab tab, int id) {
    }

    public static int getTabViewId(ActionBar.Tab tab) {
        return 0;
    }

    public static void setCustomDragView(ActionBar actionBar, View view) {
    }

    public static void setCustomDragView(ActionBar actionBar, View view, View secondView) {
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

    private static void setIconLayout(ActionBar actionBar, HwToolbar toolbar) {
        actionBar.setDisplayShowCustomEnabled(true);
        if (toolbar.getIconLayout() != null) {
            actionBar.setCustomView(toolbar.getIconLayout(), new ActionBar.LayoutParams(-1, -2));
        }
    }

    public static void setEditPositiveEnabled(HwToolbar toolbar, boolean isEnabled) {
        toolbar.setPositiveEnabled(isEnabled);
    }

    public static void setBubbleCount(HwToolbar toolbar, int count) {
        toolbar.setBubbleCount(count);
    }

    public static void setColumnEnabled(HwToolbar toolbar, boolean isEnabled) {
        toolbar.setColumnEnabled(isEnabled);
    }

    public static boolean isColumnEnabled(HwToolbar toolbar) {
        return toolbar.isColumnEnabled();
    }

    public static void configureColumn(HwToolbar toolbar, int width, int height, float density) {
        toolbar.configureColumn(width, height, density);
    }

    public static void setSplitViewCornerInsets(HwToolbar toolbar, Rect rect) {
        if (rect != null && toolbar != null && toolbar.getSplieView() != null) {
            toolbar.getSplieView().setCornerInsets(rect);
        }
    }

    public static Rect getSplitViewCornerInsets(HwToolbar toolbar) {
        if (toolbar == null || toolbar.getSplieView() == null) {
            return new Rect();
        }
        return toolbar.getSplieView().getCornerInsets();
    }
}
