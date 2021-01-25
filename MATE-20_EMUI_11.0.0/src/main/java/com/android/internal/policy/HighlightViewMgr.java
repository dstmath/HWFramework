package com.android.internal.policy;

import android.app.UiModeManager;
import android.app.WindowConfiguration;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.hwtheme.HwThemeManager;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.huawei.android.app.HwActivityManager;

/* access modifiers changed from: package-private */
public class HighlightViewMgr {
    private static final int DEFAULT_VALUE_SECURE_GESTURE_NAVIGATION = 0;
    private static final int DIM_EXPAND_VALUE = 3;
    private static final float DIP_TO_PIXEL_CONSTANT = 0.5f;
    private static final int DRAG_BAR_VIEW_ID_FREEFORM = 34603285;
    private static final int DRAG_BAR_VIEW_ID_SPLIT = 34603290;
    private static final int DRAG_BAR_VIEW_SRC_ID_FREEFORM = 33751931;
    private static final int DRAG_BAR_VIEW_SRC_ID_SPLIT = 33751930;
    private static final int DRAG_BAR_VIEW_SRC_STYLE_FOCUSED = 33947783;
    private static final int DRAG_BAR_VIEW_SRC_STYLE_FOCUSED_HONOR = 33947784;
    private static final int DRAG_BAR_VIEW_SRC_STYLE_NORMAL = 33947785;
    private static final String KEY_SECURE_GESTURE_NAVIGATION = "secure_gesture_navigation";
    private static final int RECT_FRAME_BORDER_COLOR_DARK = Color.parseColor("#99006CDE");
    private static final int RECT_FRAME_BORDER_COLOR_HONOR = Color.parseColor("#00B1FF");
    private static final int RECT_FRAME_BORDER_COLOR_ID = 33882978;
    private static final int RECT_FRAME_BORDER_WIDTH_DEFAULT = 2;
    private static final int RECT_FRAME_BORDER_WIDTH_ID = 34472571;
    private static final int RECT_FRAME_LAYOUT_NAME = 34013360;
    private static final float RECT_FRAME_RADIUS_DEFAULT = 16.0f;
    private static final float RECT_FRAME_RADIUS_FOR_SCALE = 14.0f;
    private static final int RECT_FRAME_RADIUS_ID_FREEFORM = 34472569;
    private static final int RECT_FRAME_RADIUS_ID_SPLIT = 34472572;
    private static final int RECT_FRAME_VIEW_ID = 34603287;
    private static final String TAG = "HighlightViewMgr";
    private static boolean sIsDragging;
    private final DecorView mDecorView;

    private HighlightViewMgr(DecorView decorView) {
        this.mDecorView = decorView;
    }

    static HighlightViewMgr getInstance(DecorView decorView) {
        return new HighlightViewMgr(decorView);
    }

    /* access modifiers changed from: package-private */
    public void obtainWindowFocus() {
        showRectFrameView();
        updateDragBarView(true);
    }

    /* access modifiers changed from: package-private */
    public void loseWindowFocus() {
        removeRectFrameView();
        updateDragBarView(false);
    }

    public boolean getDragState() {
        return sIsDragging;
    }

    public void setDragState(boolean bool) {
        sIsDragging = bool;
    }

    private void showRectFrameView() {
        View rectFrameView = getRectFrameView();
        if (rectFrameView != null) {
            rectFrameView.setVisibility(0);
        }
    }

    private View getRectFrameView() {
        View rectFrameView = findRectFrameView();
        if (rectFrameView == null) {
            View rectFrameLayout = addRectFrameLayout();
            if (rectFrameLayout == null) {
                return rectFrameLayout;
            }
            rectFrameView = rectFrameLayout.findViewById(34603287);
            if (rectFrameView == null) {
                Log.e(TAG, "showRectFrameView: rect frame view null.");
                return rectFrameView;
            }
        }
        updateRectFrameView(rectFrameView);
        return rectFrameView;
    }

    private void removeRectFrameView() {
        View rectFrameView = findRectFrameView();
        if (rectFrameView == null) {
            Log.i(TAG, "removeRectFrameView: rect frame view null.");
        } else {
            rectFrameView.setVisibility(4);
        }
    }

    private View findRectFrameView() {
        return this.mDecorView.findViewById(34603287);
    }

    private View findDragBarView() {
        if (WindowConfiguration.isHwSplitScreenWindowingMode(this.mDecorView.mWindowMode)) {
            return this.mDecorView.findViewById(34603290);
        }
        if (WindowConfiguration.isHwFreeFormWindowingMode(this.mDecorView.mWindowMode)) {
            return this.mDecorView.findViewById(34603285);
        }
        return null;
    }

    private View addRectFrameLayout() {
        View rectFrameLayout = LayoutInflater.from(this.mDecorView.getContext()).inflate(34013360, (ViewGroup) null);
        if (rectFrameLayout == null) {
            Log.e(TAG, "addRectFrameLayout: rect frame layout null.");
            return null;
        }
        this.mDecorView.addView(rectFrameLayout, new ViewGroup.LayoutParams(-1, -1));
        return rectFrameLayout;
    }

    private void updateRectFrameView(View rectFrameView) {
        if (rectFrameView == null) {
            Log.e(TAG, "updateRectFrameView: rect frame view null.");
            return;
        }
        ViewParent parent = rectFrameView.getParent();
        if (parent instanceof RelativeLayout) {
            RelativeLayout rectFrame = (RelativeLayout) parent;
            rectFrame.setClickable(false);
            rectFrame.setLongClickable(false);
            rectFrameView.setClickable(false);
            rectFrameView.setLongClickable(false);
        }
        ViewUpdator viewUpdator = getViewUpdator(this.mDecorView.mWindowMode);
        if (viewUpdator == null) {
            Log.e(TAG, "updateRectFrameView: view updator null.");
        } else {
            viewUpdator.updateView(rectFrameView);
        }
    }

    private void updateDragBarView(boolean isHasFocus) {
        int i;
        View dragBarImgView = findDragBarView();
        if (dragBarImgView instanceof ImageView) {
            ViewUpdator updator = getViewUpdator(this.mDecorView.mWindowMode);
            if (updator == null) {
                Log.e(TAG, "updateDragBarView: updator null");
                return;
            }
            Drawable drawable = VectorDrawable.create(this.mDecorView.getResources(), updator.getDragBarViewSrcId());
            if (drawable == null) {
                Log.e(TAG, "updateDragBarView: drag bar view src drawable null.");
                return;
            }
            Context context = this.mDecorView.getContext();
            if (isHasFocus) {
                i = HwThemeManager.isHonorBrand() ? 33947784 : 33947783;
            } else {
                i = 33947785;
            }
            drawable.applyTheme(new ContextThemeWrapper(context, i).getTheme());
            ((ImageView) dragBarImgView).setImageDrawable(drawable);
        }
    }

    private ViewUpdator getViewUpdator(int winMode) {
        switch (winMode) {
            case 100:
            case 101:
                return new SplitScreenUpdator();
            case 102:
                return new FreeFormUpdator();
            default:
                return null;
        }
    }

    /* access modifiers changed from: private */
    public abstract class ViewUpdator {
        /* access modifiers changed from: protected */
        public abstract int getDragBarViewSrcId();

        private ViewUpdator() {
        }

        /* access modifiers changed from: protected */
        public int getRectFrameBorderWidth() {
            return HighlightViewMgr.this.dip2px(2.0f);
        }

        /* access modifiers changed from: protected */
        public int getRectFrameBorderColor() {
            if (HwThemeManager.isHonorBrand()) {
                return HighlightViewMgr.RECT_FRAME_BORDER_COLOR_HONOR;
            }
            return HighlightViewMgr.this.isNightMode() ? HighlightViewMgr.RECT_FRAME_BORDER_COLOR_DARK : HighlightViewMgr.this.mDecorView.getContext().getColor(33882978);
        }

        /* access modifiers changed from: protected */
        public float getRectFrameRadius() {
            return (float) HighlightViewMgr.this.dip2px(HighlightViewMgr.RECT_FRAME_RADIUS_DEFAULT);
        }

        public void updateView(View rectFrameView) {
            if (rectFrameView == null) {
                Log.e(HighlightViewMgr.TAG, "updateView: rect frame view null.");
                return;
            }
            Drawable bg = rectFrameView.getBackground();
            if (bg == null) {
                bg = new GradientDrawable();
                rectFrameView.setBackground(bg);
            }
            if (!(bg instanceof GradientDrawable)) {
                Log.e(HighlightViewMgr.TAG, "updateView: type error.");
                return;
            }
            ((GradientDrawable) bg).setStroke(getRectFrameBorderWidth(), getRectFrameBorderColor());
            ((GradientDrawable) bg).setCornerRadius(getRectFrameRadius());
            ((GradientDrawable) bg).setAntiAlias(true);
        }
    }

    /* access modifiers changed from: private */
    public class FreeFormUpdator extends ViewUpdator {
        private static final int RECT_FRAME_BORDER_WIDTH = 1;

        private FreeFormUpdator() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.policy.HighlightViewMgr.ViewUpdator
        public int getRectFrameBorderWidth() {
            return HighlightViewMgr.this.dip2px(1.0f);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.policy.HighlightViewMgr.ViewUpdator
        public float getRectFrameRadius() {
            return HighlightViewMgr.this.mDecorView.getResources().getDimension(34472569) - ((float) HighlightViewMgr.this.dip2px(1.0f));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.policy.HighlightViewMgr.ViewUpdator
        public int getDragBarViewSrcId() {
            return 33751931;
        }
    }

    /* access modifiers changed from: private */
    public class SplitScreenUpdator extends ViewUpdator {
        private SplitScreenUpdator() {
            super();
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.policy.HighlightViewMgr.ViewUpdator
        public int getRectFrameBorderWidth() {
            return HighlightViewMgr.this.mDecorView.getResources().getDimensionPixelSize(34472571);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.policy.HighlightViewMgr.ViewUpdator
        public float getRectFrameRadius() {
            if (HwActivityManager.IS_PHONE) {
                return 0.0f;
            }
            return HighlightViewMgr.this.mDecorView.getResources().getDimension(34472572) - ((float) HighlightViewMgr.this.dip2px(1.0f));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.policy.HighlightViewMgr.ViewUpdator
        public int getDragBarViewSrcId() {
            return 33751930;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int dip2px(float dpValue) {
        return (int) ((dpValue * this.mDecorView.getContext().getResources().getDisplayMetrics().density) + DIP_TO_PIXEL_CONSTANT);
    }

    public static boolean isGestureNavigation(ContentResolver cr) {
        if (Settings.Secure.getInt(cr, "secure_gesture_navigation", 0) == 1) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNightMode() {
        int nightMode = 1;
        try {
            UiModeManager uiModeManager = (UiModeManager) this.mDecorView.getContext().getSystemService(UiModeManager.class);
            if (uiModeManager != null) {
                nightMode = uiModeManager.getNightMode();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "isNightMode: runtime exception.");
        }
        return nightMode == 2;
    }

    static boolean isApplication(WindowManager.LayoutParams attrs) {
        return attrs.type == 1 || attrs.type == 2 || attrs.type == 4;
    }

    /* access modifiers changed from: package-private */
    public void onConfigurationChanged() {
        Log.d(TAG, "onConfigurationChanged: enter.");
        if (!WindowConfiguration.isHwMultiStackWindowingMode(this.mDecorView.mWindowMode)) {
            removeRectFrameView();
        }
    }

    /* access modifiers changed from: package-private */
    public void onScaleFreeFrom(int state, boolean isHasFocus) {
        Log.d(TAG, "onScaleFreeFrom: enter.");
        if (WindowConfiguration.isHwFreeFormWindowingMode(this.mDecorView.mWindowMode)) {
            if (state == -1 && isHasFocus) {
                sIsDragging = true;
                obtainFreeFromOutlineDuringScale();
                updateDragBarView(true);
            } else if (state == -1 && !isHasFocus) {
                sIsDragging = true;
                obtainFreeFromOutlineDuringScale();
            } else if (state == -2 && isHasFocus) {
                sIsDragging = false;
                obtainWindowFocus();
            } else if (state == -2 && !isHasFocus) {
                sIsDragging = false;
                loseWindowFocus();
            }
        }
    }

    private void obtainFreeFromOutlineDuringScale() {
        View rectFrameView;
        int i;
        if (WindowConfiguration.isHwFreeFormWindowingMode(this.mDecorView.mWindowMode) && (rectFrameView = getRectFrameView()) != null) {
            Drawable bg = rectFrameView.getBackground();
            if (bg == null) {
                bg = new GradientDrawable();
                rectFrameView.setBackground(bg);
            }
            if (!(bg instanceof GradientDrawable)) {
                Log.e(TAG, "updateView: type error.");
                return;
            }
            GradientDrawable gradientDrawable = (GradientDrawable) bg;
            int dip2px = dip2px(3.0f);
            if (HwThemeManager.isHonorBrand()) {
                i = RECT_FRAME_BORDER_COLOR_HONOR;
            } else {
                i = isNightMode() ? RECT_FRAME_BORDER_COLOR_DARK : this.mDecorView.getContext().getColor(33882978);
            }
            gradientDrawable.setStroke(dip2px, i);
            ((GradientDrawable) bg).setCornerRadius((float) dip2px(RECT_FRAME_RADIUS_FOR_SCALE));
            ((GradientDrawable) bg).setAntiAlias(true);
            rectFrameView.setVisibility(0);
        }
    }
}
