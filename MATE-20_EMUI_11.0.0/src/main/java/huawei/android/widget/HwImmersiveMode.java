package huawei.android.widget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
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
    private static final int POSITION_ARRAY_SIZE = 2;
    private static final String TAG = "HwImmersiveMode";
    private Activity mActivity;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        /* class huawei.android.widget.HwImmersiveMode.AnonymousClass2 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            Log.d(HwImmersiveMode.TAG, "onNavigationBarStatusChanged:selfChange = " + isSelfChange);
            HwImmersiveMode.this.updateImmersiveMode();
        }
    };
    private final Context mContext;
    private boolean mIsInMultiWindow;
    private boolean mIsInMultiWindowRight;
    private boolean mIsInMultiWindowTop;
    private boolean mIsNavigationBarBlurEnabled;
    private boolean mIsShowHwBlur = HwBlurEngine.getInstance().isShowHwBlur();
    private boolean mIsStatusBarBlurEnabled;
    private ViewTreeObserver.OnGlobalLayoutListener mLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        /* class huawei.android.widget.HwImmersiveMode.AnonymousClass1 */

        @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
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

    public HwImmersiveMode(Activity activity) {
        super(activity);
        this.mContext = activity;
        this.mActivity = activity;
        setupStatusBarView(this.mContext);
        setupNavigationBarView(this.mContext);
        ViewGroup contentViewGroup = (ViewGroup) this.mActivity.findViewById(16908290);
        if (contentViewGroup.getRootView() instanceof ViewGroup) {
            ((ViewGroup) contentViewGroup.getRootView()).addView(this);
        }
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
    }

    public void setStatusBarBlurEnable(boolean isEnabled) {
        StatusBarBlurView statusBarBlurView;
        boolean isEnabledAndSupported = isEnabled && isThemeSupport(this.mActivity);
        if (this.mIsStatusBarBlurEnabled != isEnabledAndSupported) {
            this.mIsStatusBarBlurEnabled = isEnabledAndSupported;
            Log.d(TAG, "setStatusBarBlurEnable: isEnabledAndSupported = " + isEnabledAndSupported);
            if (getWindowVisibility() == 0 && (statusBarBlurView = this.mStatusBarView) != null) {
                statusBarBlurView.updateStatus();
            }
        }
    }

    public void setNavigationBarBlurEnable(boolean isEnabled) {
        NavigationBarBlurView navigationBarBlurView;
        boolean isEnabledAndSupported = isEnabled && isThemeSupport(this.mActivity);
        if (this.mIsNavigationBarBlurEnabled != isEnabledAndSupported) {
            this.mIsNavigationBarBlurEnabled = isEnabledAndSupported;
            Log.d(TAG, "setNavigationBarBlurEnable: isEnabledAndSupported = " + isEnabledAndSupported);
            if (getWindowVisibility() == 0 && (navigationBarBlurView = this.mNavigationBarView) != null) {
                navigationBarBlurView.updateStatus();
            }
        }
    }

    public void setStatusBarOverlayColor(int overlayColor) {
        StatusBarBlurView statusBarBlurView;
        if (this.mIsShowHwBlur && (statusBarBlurView = this.mStatusBarView) != null && this.mIsStatusBarBlurEnabled) {
            statusBarBlurView.setTargetViewOverlayColor(overlayColor);
        }
    }

    public void setNavigationBarOverlayColor(int overlayColor) {
        NavigationBarBlurView navigationBarBlurView;
        if (this.mIsShowHwBlur && (navigationBarBlurView = this.mNavigationBarView) != null && this.mIsNavigationBarBlurEnabled) {
            navigationBarBlurView.setTargetViewOverlayColor(overlayColor);
        }
    }

    private void setStatusBarOverlayColorType(int blurTypeId) {
        HwBlurEngine.BlurType blurType;
        if (this.mIsShowHwBlur && this.mStatusBarView != null && this.mIsStatusBarBlurEnabled && (blurType = HwBlurEngine.BlurType.fromTypeValue(blurTypeId)) != null) {
            this.mStatusBarView.setTargetViewOverlayColorType(blurType);
        }
    }

    private void setNavigationBarOverlayColorType(int blurTypeId) {
        HwBlurEngine.BlurType blurType;
        if (this.mIsShowHwBlur && this.mNavigationBarView != null && this.mIsNavigationBarBlurEnabled && (blurType = HwBlurEngine.BlurType.fromTypeValue(blurTypeId)) != null) {
            this.mNavigationBarView.setTargetViewOverlayColorType(blurType);
        }
    }

    public void setHwToolbarBlurEnable(HwToolbar hwToolbar, boolean isEnabled) {
        if (hwToolbar != null) {
            hwToolbar.setBlurEnable(isEnabled);
        }
    }

    public void setActionBarBlurEnable(ActionBar actionBar, boolean isEnabled) {
        if (actionBar != null) {
            ActionBarEx.setBlurEnable(actionBar, isEnabled);
        }
    }

    public void setSpiltViewBlurEnable(HwToolbar hwToolbar, boolean isEnabled) {
        if (hwToolbar != null) {
            ActionBarEx.setSpiltViewBlurEnable(hwToolbar, isEnabled);
        }
    }

    public void setSpiltViewBlurEnable(ActionBar actionBar, boolean isEnabled) {
        if (actionBar != null) {
            ActionBarEx.setSpiltViewBlurEnable(actionBar, isEnabled);
        }
    }

    public void setSubTabWidgetBlurEnable(SubTabWidget subTabWidget, boolean isEnabled) {
        if (subTabWidget != null) {
            subTabWidget.setBlurEnable(isEnabled);
        }
    }

    public void setHwBottomNavigationViewBlurEnable(HwBottomNavigationView hwBottomNavigationView, boolean isEnabled) {
        if (hwBottomNavigationView != null) {
            hwBottomNavigationView.setBlurEnable(isEnabled);
        }
    }

    public void setMultiWindowModeChanged(boolean isInMultiWindowMode) {
        this.mIsInMultiWindow = isInMultiWindowMode;
        Log.d(TAG, "onMultiWindowModeChanged: isInMultiWindowMode = " + isInMultiWindowMode);
        updateImmersiveMode();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void fetchMultiWindowPosition() {
        int[] locations = new int[2];
        getLocationOnScreen(locations);
        boolean z = true;
        this.mIsInMultiWindowTop = locations[1] <= 0;
        if (locations[0] <= 0) {
            z = false;
        }
        this.mIsInMultiWindowRight = z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMultiWindowPositionChanged() {
        Activity activity = this.mActivity;
        if (activity == null || !activity.isInMultiWindowMode()) {
            return false;
        }
        int[] locations = new int[2];
        getLocationOnScreen(locations);
        boolean isInMultiWindowTop = locations[1] <= 0;
        boolean isInMultiWindowRight = locations[0] > 0;
        if (isInMultiWindowTop == this.mIsInMultiWindowTop && isInMultiWindowRight == this.mIsInMultiWindowRight) {
            return false;
        }
        this.mIsInMultiWindowTop = isInMultiWindowTop;
        this.mIsInMultiWindowRight = isInMultiWindowRight;
        StringBuilder sb = new StringBuilder();
        sb.append("onMultiWindowPositionChanged: ");
        sb.append(isInMultiWindowTop ? "[top]" : "[bottom]");
        sb.append(isInMultiWindowRight ? "[right]" : "[left]");
        Log.d(TAG, sb.toString());
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateImmersiveMode() {
        StatusBarBlurView statusBarBlurView;
        if (this.mActivity != null && (statusBarBlurView = this.mStatusBarView) != null && this.mNavigationBarView != null) {
            statusBarBlurView.updateStatus();
            this.mNavigationBarView.updateStatus();
            updateSplitViewPositon();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mOrientation = newConfig.orientation;
    }

    private void updateSplitViewPositon() {
        Log.d(TAG, "updateSplitViewPositon: ");
        int bottomMargin = 0;
        if (this.mActivity != null) {
            if (this.mSplitView == null) {
                this.mSplitView = getRootView().findViewById(16909433);
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
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this.mLayoutListener);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_NAVIGATION_BAR_STATUS), false, this.mContentObserver);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this.mLayoutListener);
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
    }

    @Override // android.view.View
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
    /* access modifiers changed from: public */
    private boolean isNavigationBarExist() {
        return Settings.Global.getInt(this.mContext.getContentResolver(), KEY_NAVIGATION_BAR_STATUS, 0) == 0;
    }

    private int getStatusBarHeight() {
        return this.mContext.getResources().getDimensionPixelSize(this.mContext.getResources().getIdentifier("status_bar_height", ResLoaderUtil.DIMEN, "android"));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getNavigationHeight() {
        return this.mContext.getResources().getDimensionPixelSize(this.mContext.getResources().getIdentifier("navigation_bar_height", ResLoaderUtil.DIMEN, "android"));
    }

    private boolean isThemeSupport(Context context) {
        if (context == null) {
            return false;
        }
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{ResLoader.getInstance().getIdentifier(context, "attr", "hwBlurEffectEnable")});
        boolean isHwBlurEffectEnable = typedArray.getBoolean(0, false);
        typedArray.recycle();
        return isHwBlurEffectEnable;
    }

    /* access modifiers changed from: private */
    public static class BlurView extends View {
        private static final int ALPHA_CHANNEL = -16777216;
        private static final int DEFAULT_BLUR_TYPE = HwBlurEngine.BlurType.LightBlurWithGray.getValue();
        private static final int DEFAULT_GRAY = -855310;
        private static final int INVALID_BLUR_OVERLAY_COLOR = -16777216;
        protected static final int TRANS_WHITE = 16777215;
        protected HwBlurEngine mBlurEngine = HwBlurEngine.getInstance();
        private boolean mIsOverlayColorChanged = false;
        protected int mOverlayColor = -16777216;
        protected HwBlurEngine.BlurType mOverlayColorType = HwBlurEngine.BlurType.LightBlurWithGray;

        BlurView(Context context) {
            super(context);
            setBackgroundColor(DEFAULT_GRAY);
        }

        @Override // android.view.View
        public void draw(Canvas canvas) {
            if (this.mBlurEngine.isShowHwBlur(this)) {
                this.mBlurEngine.draw(canvas, this);
                super.dispatchDraw(canvas);
                return;
            }
            super.draw(canvas);
        }

        public void setTargetViewOverlayColor(int overlayColor) {
            if (!this.mIsOverlayColorChanged) {
                this.mIsOverlayColorChanged = true;
            }
            this.mOverlayColor = overlayColor;
            setBackgroundColor(-16777216 | overlayColor);
        }

        public void setTargetViewOverlayColorType(HwBlurEngine.BlurType blurType) {
            this.mOverlayColorType = blurType;
        }

        /* access modifiers changed from: protected */
        public void onWindowVisibilityChanged(View targetView, int visibility) {
            if (visibility == 0) {
                this.mBlurEngine.addBlurTargetView(targetView, this.mOverlayColorType);
                this.mBlurEngine.setTargetViewBlurEnable(targetView, true);
                return;
            }
            this.mBlurEngine.removeBlurTargetView(targetView);
        }

        /* access modifiers changed from: protected */
        public void updateOverlayColor(View targetView) {
            if (this.mBlurEngine.isShowHwBlur(targetView) && this.mIsOverlayColorChanged) {
                this.mBlurEngine.setTargetViewOverlayColor(targetView, this.mOverlayColor);
            }
        }

        /* access modifiers changed from: protected */
        public void initBlurColorAndType(Context context, int[] attrsArray) {
            if (context != null) {
                TypedArray typedArray = context.obtainStyledAttributes(attrsArray);
                this.mOverlayColor = typedArray.getColor(0, -16777216);
                int overlayColorTypeId = typedArray.getInteger(1, DEFAULT_BLUR_TYPE);
                int i = this.mOverlayColor;
                if (i != -16777216) {
                    setTargetViewOverlayColor(i);
                }
                typedArray.recycle();
                HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(overlayColorTypeId);
                if (blurType != null) {
                    this.mOverlayColorType = blurType;
                }
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);
            onWindowVisibilityChanged(this, visibility);
            if (visibility == 0) {
                updateStatus();
            }
        }

        /* access modifiers changed from: protected */
        @Override // android.view.View
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            updateStatus();
        }

        public void updateStatus() {
        }
    }

    /* access modifiers changed from: private */
    public class StatusBarBlurView extends BlurView {
        StatusBarBlurView(Context context) {
            super(context);
            initBlurColorAndType(context, new int[]{ResLoader.getInstance().getIdentifier(context, "attr", "statusbarBlurOverlayColor"), ResLoader.getInstance().getIdentifier(context, "attr", "statusbarBlurType")});
        }

        @Override // huawei.android.widget.HwImmersiveMode.BlurView
        public void updateStatus() {
            super.updateStatus();
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

    /* access modifiers changed from: private */
    public class NavigationBarBlurView extends BlurView {
        NavigationBarBlurView(Context context) {
            super(context);
            initBlurColorAndType(context, new int[]{ResLoader.getInstance().getIdentifier(context, "attr", "navigationbarBlurOverlayColor"), ResLoader.getInstance().getIdentifier(context, "attr", "navigationbarBlurType")});
        }

        private boolean isAtBottom() {
            if (HwImmersiveMode.this.mActivity.isInMultiWindowMode()) {
                return false;
            }
            Display display = HwImmersiveMode.this.mActivity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            if (size.y != realSize.y) {
                return true;
            }
            return false;
        }

        @Override // huawei.android.widget.HwImmersiveMode.BlurView
        public void updateStatus() {
            super.updateStatus();
            boolean isLandscape = getResources().getConfiguration().orientation == 2;
            Log.d(HwImmersiveMode.TAG, "updateStatus: [navigation] isScreenLandscape = " + isLandscape);
            if (!isLandscape || isAtBottom()) {
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
                return;
            }
            setVisibility(8);
        }
    }
}
