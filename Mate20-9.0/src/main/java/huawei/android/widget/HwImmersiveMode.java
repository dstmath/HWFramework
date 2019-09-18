package huawei.android.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import com.android.internal.app.ToolbarActionBar;
import com.huawei.android.app.ActionBarEx;
import huawei.android.widget.effect.engine.HwBlurEngine;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwImmersiveMode extends RelativeLayout {
    private static final String KEY_NAVIGATION_BAR_STATUS = "navigationbar_is_min";
    private static final String TAG = "HwImmersiveMode";
    /* access modifiers changed from: private */
    public Activity mActivity;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.d(HwImmersiveMode.TAG, "onNavigationBarStatusChanged: ------------ selfChange = " + selfChange);
            HwImmersiveMode.this.updateImmersiveMode();
        }
    };
    private final Context mContext;
    private boolean mIsInMultiWindow;
    /* access modifiers changed from: private */
    public boolean mIsInMultiWindowRight;
    /* access modifiers changed from: private */
    public boolean mIsInMultiWindowTop;
    /* access modifiers changed from: private */
    public boolean mIsNavigationBarBlurEnabled;
    private boolean mIsShowHwBlur = HwBlurEngine.getInstance().isShowHwBlur();
    /* access modifiers changed from: private */
    public boolean mIsStatusBarBlurEnabled;
    private ViewTreeObserver.OnGlobalLayoutListener mLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            if (HwImmersiveMode.this.isMultiWindowPositionChanged()) {
                HwImmersiveMode.this.updateImmersiveMode();
            }
        }
    };
    private NavigationBarBlurView mNavigationBarView;
    private int mOrientation;
    private View mSplitView;
    private StatusBarBlurView mStatusBarView;

    private class BlurView extends View {
        private static final int ALPHA_CHANNEL = -16777216;
        private static final int DEFAULT_GRAY = -855310;
        private static final int INVALID_BLUR_OVERLAY_COLOR = -16777216;
        protected static final int TRANS_WHITE = 16777215;
        private final int DEFAULT_BLUR_TYPE = HwBlurEngine.BlurType.LightBlurWithGray.getValue();
        protected HwBlurEngine blurEngine = HwBlurEngine.getInstance();
        private boolean isOverlayColorChanged = false;
        protected int overlayColor = -16777216;
        protected HwBlurEngine.BlurType overlayColorType = HwBlurEngine.BlurType.LightBlurWithGray;

        public BlurView(Context context) {
            super(context);
            setBackgroundColor(DEFAULT_GRAY);
        }

        public void draw(Canvas canvas) {
            if (this.blurEngine.isShowHwBlur(this)) {
                this.blurEngine.draw(canvas, this);
                super.dispatchDraw(canvas);
                return;
            }
            super.draw(canvas);
        }

        public void setTargetViewOverlayColor(int overlayColor2) {
            if (!this.isOverlayColorChanged) {
                this.isOverlayColorChanged = true;
            }
            this.overlayColor = overlayColor2;
            setBackgroundColor(-16777216 | overlayColor2);
        }

        public void setTargetViewOverlayColorType(HwBlurEngine.BlurType blurType) {
            this.overlayColorType = blurType;
        }

        /* access modifiers changed from: protected */
        public void onWindowVisibilityChanged(View targetView, int visibility) {
            if (visibility == 0) {
                this.blurEngine.addBlurTargetView(targetView, this.overlayColorType);
                this.blurEngine.setTargetViewBlurEnable(targetView, true);
                return;
            }
            this.blurEngine.removeBlurTargetView(targetView);
        }

        /* access modifiers changed from: protected */
        public void updateOverlayColor(View targetView) {
            if (this.blurEngine.isShowHwBlur(targetView) && this.isOverlayColorChanged) {
                this.blurEngine.setTargetViewOverlayColor(targetView, this.overlayColor);
            }
        }

        /* access modifiers changed from: protected */
        public void initBlurColorAndType(Context context, int[] attrsArray) {
            if (context != null) {
                TypedArray typedArray = context.obtainStyledAttributes(attrsArray);
                this.overlayColor = typedArray.getColor(0, -16777216);
                int overlayColorTypeId = typedArray.getInteger(1, this.DEFAULT_BLUR_TYPE);
                if (this.overlayColor != -16777216) {
                    setTargetViewOverlayColor(this.overlayColor);
                }
                typedArray.recycle();
                HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(overlayColorTypeId);
                if (blurType != null) {
                    this.overlayColorType = blurType;
                }
            }
        }
    }

    private class NavigationBarBlurView extends BlurView {
        public NavigationBarBlurView(Context context) {
            super(context);
            initBlurColorAndType(context, new int[]{ResLoader.getInstance().getIdentifier(context, "attr", "navigationbarBlurOverlayColor"), ResLoader.getInstance().getIdentifier(context, "attr", "navigationbarBlurType")});
        }

        /* access modifiers changed from: protected */
        public void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);
            onWindowVisibilityChanged(this, visibility);
            if (visibility == 0) {
                updateStatus();
            }
        }

        public void updateStatus() {
            boolean isLandscape = getResources().getConfiguration().orientation == 2;
            Log.d(HwImmersiveMode.TAG, "updateStatus: [navigation] isScreenLandscape = " + isLandscape);
            if (isLandscape) {
                setVisibility(8);
                return;
            }
            Log.d(HwImmersiveMode.TAG, "updateStatus: [navigation] isNavigationBarExist = " + HwImmersiveMode.this.isNavigationBarExist());
            if (!HwImmersiveMode.this.isNavigationBarExist()) {
                setVisibility(8);
                return;
            }
            Log.d(HwImmersiveMode.TAG, "updateStatus: [navigation] mIsNavigationBarBlurEnabled = " + HwImmersiveMode.this.mIsNavigationBarBlurEnabled);
            if (!HwImmersiveMode.this.mIsNavigationBarBlurEnabled) {
                setVisibility(8);
                return;
            }
            boolean isInMultiWindow = HwImmersiveMode.this.mActivity.isInMultiWindowMode();
            Log.d(HwImmersiveMode.TAG, "updateStatus: [navigation] isInMultiWindow = " + isInMultiWindow);
            if (isInMultiWindow) {
                HwImmersiveMode.this.fetchMultiWindowPosition();
                Log.d(HwImmersiveMode.TAG, "updateStatus: [navigation] mIsInMultiWindowTop = " + HwImmersiveMode.this.mIsInMultiWindowTop);
                Log.d(HwImmersiveMode.TAG, "updateStatus: [navigation] mIsInMultiWindowRight = " + HwImmersiveMode.this.mIsInMultiWindowRight);
            }
            if (!isInMultiWindow || !HwImmersiveMode.this.mIsInMultiWindowTop) {
                setVisibility(0);
                HwImmersiveMode.this.mActivity.getWindow().setNavigationBarColor(16777215);
                updateOverlayColor(this);
                return;
            }
            setVisibility(8);
        }
    }

    private class StatusBarBlurView extends BlurView {
        public StatusBarBlurView(Context context) {
            super(context);
            initBlurColorAndType(context, new int[]{ResLoader.getInstance().getIdentifier(context, "attr", "statusbarBlurOverlayColor"), ResLoader.getInstance().getIdentifier(context, "attr", "statusbarBlurType")});
        }

        /* access modifiers changed from: protected */
        public void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);
            onWindowVisibilityChanged(this, visibility);
            if (visibility == 0) {
                updateStatus();
            }
        }

        public void updateStatus() {
            Log.d(HwImmersiveMode.TAG, "updateStatus: [status] mIsStatusBarBlurEnabled = " + HwImmersiveMode.this.mIsStatusBarBlurEnabled);
            if (!HwImmersiveMode.this.mIsStatusBarBlurEnabled) {
                setVisibility(8);
                return;
            }
            boolean isInMultiWindow = HwImmersiveMode.this.mActivity.isInMultiWindowMode();
            Log.d(HwImmersiveMode.TAG, "updateStatus: [status] isInMultiWindow = " + isInMultiWindow);
            if (isInMultiWindow) {
                HwImmersiveMode.this.fetchMultiWindowPosition();
                Log.d(HwImmersiveMode.TAG, "updateStatus: [status] mIsInMultiWindowTop = " + HwImmersiveMode.this.mIsInMultiWindowTop);
                Log.d(HwImmersiveMode.TAG, "updateStatus: [status] mIsInMultiWindowRight = " + HwImmersiveMode.this.mIsInMultiWindowRight);
            }
            if (!isInMultiWindow || HwImmersiveMode.this.mIsInMultiWindowTop) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
                if (!isInMultiWindow || !HwImmersiveMode.this.mIsInMultiWindowRight || !HwImmersiveMode.this.isNavigationBarExist()) {
                    params.setMargins(0, 0, 0, 0);
                } else {
                    params.setMargins(0, 0, HwImmersiveMode.this.getNavigationHeight(), 0);
                }
                setLayoutParams(params);
                setVisibility(0);
                HwImmersiveMode.this.mActivity.getWindow().setStatusBarColor(16777215);
                updateOverlayColor(this);
                return;
            }
            setVisibility(8);
        }
    }

    public HwImmersiveMode(Activity activity) {
        super(activity);
        this.mContext = activity;
        this.mActivity = activity;
        setupStatusBarView(this.mContext);
        setupNavigationBarView(this.mContext);
        ((ViewGroup) ((ViewGroup) this.mActivity.findViewById(16908290)).getRootView()).addView(this);
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
    }

    public void setStatusBarBlurEnable(boolean enabled) {
        boolean enabled2 = enabled && isThemeSupport(this.mActivity);
        if (this.mIsStatusBarBlurEnabled != enabled2) {
            this.mIsStatusBarBlurEnabled = enabled2;
            Log.d(TAG, "setStatusBarBlurEnable: enabled = " + enabled2);
            if (getWindowVisibility() == 0 && this.mStatusBarView != null) {
                this.mStatusBarView.updateStatus();
            }
        }
    }

    public void setNavigationBarBlurEnable(boolean enabled) {
        boolean enabled2 = enabled && isThemeSupport(this.mActivity);
        if (this.mIsNavigationBarBlurEnabled != enabled2) {
            this.mIsNavigationBarBlurEnabled = enabled2;
            Log.d(TAG, "setNavigationBarBlurEnable: enabled = " + enabled2);
            if (getWindowVisibility() == 0 && this.mNavigationBarView != null) {
                this.mNavigationBarView.updateStatus();
            }
        }
    }

    public void setStatusBarOverlayColor(int overlayColor) {
        if (this.mIsShowHwBlur && this.mStatusBarView != null && this.mIsStatusBarBlurEnabled) {
            this.mStatusBarView.setTargetViewOverlayColor(overlayColor);
        }
    }

    public void setNavigationBarOverlayColor(int overlayColor) {
        if (this.mIsShowHwBlur && this.mNavigationBarView != null && this.mIsNavigationBarBlurEnabled) {
            this.mNavigationBarView.setTargetViewOverlayColor(overlayColor);
        }
    }

    private void setStatusBarOverlayColorType(int blurTypeId) {
        if (this.mIsShowHwBlur && this.mStatusBarView != null && this.mIsStatusBarBlurEnabled) {
            HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(blurTypeId);
            if (blurType != null) {
                this.mStatusBarView.setTargetViewOverlayColorType(blurType);
            }
        }
    }

    private void setNavigationBarOverlayColorType(int blurTypeId) {
        if (this.mIsShowHwBlur && this.mNavigationBarView != null && this.mIsNavigationBarBlurEnabled) {
            HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(blurTypeId);
            if (blurType != null) {
                this.mNavigationBarView.setTargetViewOverlayColorType(blurType);
            }
        }
    }

    public void setHwToolbarBlurEnable(HwToolbar hwToolbar, boolean enabled) {
        if (hwToolbar != null) {
            hwToolbar.setBlurEnable(enabled);
        }
    }

    public void setSpiltViewBlurEnable(HwToolbar hwToolbar, boolean enabled) {
        if (hwToolbar != null) {
            ActionBarEx.setSpiltViewBlurEnable(hwToolbar, enabled);
        }
    }

    public void setActionBarBlurEnable(ActionBar actionBar, boolean enabled) {
        if (actionBar != null) {
            ActionBarEx.setBlurEnable(actionBar, enabled);
        }
    }

    public void setSpiltViewBlurEnable(ActionBar actionBar, boolean enabled) {
        if (actionBar != null) {
            ActionBarEx.setSpiltViewBlurEnable(actionBar, enabled);
        }
    }

    public void setSubTabWidgetBlurEnable(SubTabWidget subTabWidget, boolean enabled) {
        if (subTabWidget != null) {
            subTabWidget.setBlurEnable(enabled);
        }
    }

    public void setHwBottomNavigationViewBlurEnable(HwBottomNavigationView hwBottomNavigationView, boolean enabled) {
        if (hwBottomNavigationView != null) {
            hwBottomNavigationView.setBlurEnable(enabled);
        }
    }

    public void setMultiWindowModeChanged(boolean isInMultiWindowMode) {
        this.mIsInMultiWindow = isInMultiWindowMode;
        Log.d(TAG, "onMultiWindowModeChanged: ------------isInMultiWindowMode = " + isInMultiWindowMode);
        updateImmersiveMode();
    }

    /* access modifiers changed from: private */
    public void fetchMultiWindowPosition() {
        int[] location = new int[2];
        getLocationOnScreen(location);
        boolean z = true;
        this.mIsInMultiWindowTop = location[1] <= 0;
        if (location[0] <= 0) {
            z = false;
        }
        this.mIsInMultiWindowRight = z;
    }

    /* access modifiers changed from: private */
    public boolean isMultiWindowPositionChanged() {
        if (this.mActivity == null || !this.mActivity.isInMultiWindowMode()) {
            return false;
        }
        int[] location = new int[2];
        getLocationOnScreen(location);
        boolean isInMultiWindowTop = location[1] <= 0;
        boolean isInMultiWindowRight = location[0] > 0;
        if (isInMultiWindowTop == this.mIsInMultiWindowTop && isInMultiWindowRight == this.mIsInMultiWindowRight) {
            return false;
        }
        this.mIsInMultiWindowTop = isInMultiWindowTop;
        this.mIsInMultiWindowRight = isInMultiWindowRight;
        StringBuilder sb = new StringBuilder();
        sb.append("onMultiWindowPositionChanged: ------------ ");
        sb.append(isInMultiWindowTop ? "[top]" : "[bottom]");
        sb.append(isInMultiWindowRight ? "[right]" : "[left]");
        Log.d(TAG, sb.toString());
        return true;
    }

    /* access modifiers changed from: private */
    public void updateImmersiveMode() {
        if (this.mActivity != null && this.mStatusBarView != null && this.mNavigationBarView != null) {
            this.mStatusBarView.updateStatus();
            this.mNavigationBarView.updateStatus();
            updateSplitViewPositon();
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mOrientation = newConfig.orientation;
    }

    private void updateSplitViewPositon() {
        Log.d(TAG, "updateSplitViewPositon: ");
        int bottomMargin = 0;
        if (this.mActivity != null) {
            if (this.mSplitView == null) {
                this.mSplitView = getRootView().findViewById(16909362);
                if (this.mSplitView == null) {
                    return;
                }
            }
            if (!this.mActivity.isInMultiWindowMode() && isNavigationBarExist() && getResources().getConfiguration().orientation == 1) {
                bottomMargin = getNavigationHeight();
                Log.d(TAG, "updateSplitViewPositon: bottomMargin=" + bottomMargin);
            }
            if (this.mActivity.getActionBar() instanceof ToolbarActionBar) {
                this.mSplitView.setTranslationY((float) (-bottomMargin));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this.mLayoutListener);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_NAVIGATION_BAR_STATUS), false, this.mContentObserver);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this.mLayoutListener);
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        updateSplitViewPositon();
    }

    private void setupStatusBarView(Context context) {
        this.mStatusBarView = new StatusBarBlurView(context);
        addView(this.mStatusBarView, new RelativeLayout.LayoutParams(-1, getStatusBarHeight()));
    }

    private void setupNavigationBarView(Context context) {
        this.mNavigationBarView = new NavigationBarBlurView(context);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, getNavigationHeight());
        params.addRule(12, -1);
        addView(this.mNavigationBarView, params);
    }

    /* access modifiers changed from: private */
    public boolean isNavigationBarExist() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), KEY_NAVIGATION_BAR_STATUS, 0) == 0;
    }

    private int getStatusBarHeight() {
        return this.mContext.getResources().getDimensionPixelSize(this.mContext.getResources().getIdentifier("status_bar_height", ResLoaderUtil.DIMEN, "android"));
    }

    /* access modifiers changed from: private */
    public int getNavigationHeight() {
        return this.mContext.getResources().getDimensionPixelSize(this.mContext.getResources().getIdentifier("navigation_bar_height", ResLoaderUtil.DIMEN, "android"));
    }

    private boolean isThemeSupport(Context context) {
        if (context == null) {
            return false;
        }
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{ResLoader.getInstance().getIdentifier(context, "attr", "hwBlurEffectEnable")});
        boolean hwBlurEffectEnable = typedArray.getBoolean(0, false);
        typedArray.recycle();
        return hwBlurEffectEnable;
    }
}
