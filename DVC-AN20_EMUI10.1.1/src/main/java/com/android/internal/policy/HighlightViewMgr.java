package com.android.internal.policy;

import android.app.UiModeManager;
import android.app.WindowConfiguration;
import android.content.ContentResolver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
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
    private static final float DIP_TO_PIXEL_CONSTANT = 0.5f;
    private static final int DRAG_BAR_VIEW_ID_FREEFORM = 34603265;
    private static final int DRAG_BAR_VIEW_ID_SPLIT = 34603269;
    private static final int DRAG_BAR_VIEW_SRC_ID_FREEFORM = 33751922;
    private static final int DRAG_BAR_VIEW_SRC_ID_SPLIT = 33751921;
    private static final int DRAG_BAR_VIEW_SRC_STYLE_FOCUSED = 33947774;
    private static final int DRAG_BAR_VIEW_SRC_STYLE_NORMAL = 33947775;
    private static final String KEY_SECURE_GESTURE_NAVIGATION = "secure_gesture_navigation";
    private static final int RECT_FRAME_BORDER_COLOR_DARK = Color.parseColor("#99006CDE");
    private static final int RECT_FRAME_BORDER_COLOR_ID = 33882866;
    private static final int RECT_FRAME_BORDER_WIDTH_DEFAULT = 2;
    private static final int RECT_FRAME_BORDER_WIDTH_ID = 34472527;
    private static final int RECT_FRAME_LAYOUT_NAME = 34013359;
    private static final float RECT_FRAME_RADIUS_DEFAULT = 16.0f;
    private static final int RECT_FRAME_RADIUS_ID_FREEFORM = 34472525;
    private static final int RECT_FRAME_RADIUS_ID_SPLIT = 34472528;
    private static final int RECT_FRAME_VIEW_ID = 34603267;
    private static final String TAG = "HighlightViewMgr";
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

    private void showRectFrameView() {
        View rectFrameView = findRectFrameView();
        if (rectFrameView == null) {
            View rectFrameLayout = addRectFrameLayout();
            if (rectFrameLayout != null) {
                rectFrameView = rectFrameLayout.findViewById(34603267);
                if (rectFrameView == null) {
                    Log.e(TAG, "showRectFrameView: rect frame view null.");
                    return;
                }
            } else {
                return;
            }
        }
        updateRectFrameView(rectFrameView);
        rectFrameView.setVisibility(0);
    }

    private void removeRectFrameView() {
        View rectFrameView = findRectFrameView();
        if (rectFrameView == null) {
            Log.i(TAG, "removeRectFrameView: rect frame view null.");
            return;
        }
        ViewParent parent = rectFrameView.getParent();
        if (parent instanceof RelativeLayout) {
            this.mDecorView.removeView((View) RelativeLayout.class.cast(parent));
        }
    }

    private View findRectFrameView() {
        return this.mDecorView.findViewById(34603267);
    }

    private View findDragBarView() {
        if (WindowConfiguration.isHwSplitScreenWindowingMode(this.mDecorView.mWindowMode)) {
            return this.mDecorView.findViewById(34603269);
        }
        if (WindowConfiguration.isHwFreeFormWindowingMode(this.mDecorView.mWindowMode)) {
            return this.mDecorView.findViewById(34603265);
        }
        return null;
    }

    private View addRectFrameLayout() {
        View rectFrameLayout = LayoutInflater.from(this.mDecorView.getContext()).inflate(34013359, (ViewGroup) null);
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
            drawable.applyTheme(new ContextThemeWrapper(this.mDecorView.getContext(), isHasFocus ? 33947774 : 33947775).getTheme());
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
            if (HighlightViewMgr.this.isNightMode()) {
                return HighlightViewMgr.RECT_FRAME_BORDER_COLOR_DARK;
            }
            return HighlightViewMgr.this.mDecorView.getContext().getColor(33882866);
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
            return HighlightViewMgr.this.mDecorView.getResources().getDimension(34472525) - ((float) HighlightViewMgr.this.dip2px(1.0f));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.policy.HighlightViewMgr.ViewUpdator
        public int getDragBarViewSrcId() {
            return 33751922;
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
            return HighlightViewMgr.this.mDecorView.getResources().getDimensionPixelSize(34472527);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.policy.HighlightViewMgr.ViewUpdator
        public float getRectFrameRadius() {
            if (HwActivityManager.IS_PHONE) {
                return 0.0f;
            }
            return HighlightViewMgr.this.mDecorView.getResources().getDimension(34472528) - ((float) HighlightViewMgr.this.dip2px(1.0f));
        }

        /* access modifiers changed from: protected */
        @Override // com.android.internal.policy.HighlightViewMgr.ViewUpdator
        public int getDragBarViewSrcId() {
            return 33751921;
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
}
