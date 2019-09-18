package huawei.android.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwFloatingLayout extends FrameLayout {
    private static final String KEY_NAVIGATION_BAR_STATUS = "navigationbar_is_min";
    private static final String TAG = "HwFloatingLayout";
    private static final long mDurationMillis = 300;
    /* access modifiers changed from: private */
    public Activity mActivity;
    private FrameLayout mContentLayout;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            boolean exist = HwFloatingLayout.this.isNavigationBarExist();
            if (exist != HwFloatingLayout.this.mIsNavigationBarExist) {
                boolean unused = HwFloatingLayout.this.mIsNavigationBarExist = exist;
                HwFloatingLayout.this.updateSystemWidgetsLayout();
            }
        }
    };
    private final Context mContext;
    private View mFab;
    private int mFabHeight;
    private View mHwBottomNavigationView;
    private int mHwBottomNavigationViewHeight;
    /* access modifiers changed from: private */
    public HwFloatingLayout mHwFloatingView;
    private View mHwToolbar;
    private int mHwToolbarHeight;
    private final Rect mInsetsRect = new Rect();
    private Interpolator mInterpolator;
    private boolean mIsInMultiWindowOrPictureInPictureMode;
    /* access modifiers changed from: private */
    public boolean mIsNavigationBarExist;
    private boolean mIsSplitViewListenerAdded;
    /* access modifiers changed from: private */
    public int mLastKeyBoardHeight;
    /* access modifiers changed from: private */
    public LinearLayout mLinearLayout;
    private int mNavigationBarHeight;
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            Rect r = new Rect();
            HwFloatingLayout.this.mHwFloatingView.getWindowVisibleDisplayFrame(r);
            int usableHeight = r.bottom;
            if (usableHeight != HwFloatingLayout.this.mPreviousUsableHeight) {
                int screenHeigh = HwFloatingLayout.this.mHwFloatingView.getRootView().getHeight();
                int keyboardHeight = screenHeigh - usableHeight;
                if (keyboardHeight != HwFloatingLayout.this.mLastKeyBoardHeight) {
                    int unused = HwFloatingLayout.this.mLastKeyBoardHeight = keyboardHeight;
                    if (HwFloatingLayout.this.mLinearLayout != null) {
                        if (keyboardHeight <= screenHeigh / 4) {
                            HwFloatingLayout.this.mLinearLayout.setPadding(0, 0, 0, 0);
                        } else if (HwFloatingLayout.this.mActivity != null) {
                            View view = HwFloatingLayout.this.mActivity.getCurrentFocus();
                            if (view != null && (view instanceof TextView)) {
                                int[] location = new int[2];
                                view.getLocationOnScreen(location);
                                int bottomPadding = keyboardHeight - ((screenHeigh - location[1]) - view.getHeight());
                                if (bottomPadding > 0) {
                                    HwFloatingLayout.this.mLinearLayout.setPadding(0, -bottomPadding, 0, 0);
                                }
                            }
                        }
                        int unused2 = HwFloatingLayout.this.mPreviousUsableHeight = usableHeight;
                    }
                }
            }
        }
    };
    private int mOrientation;
    /* access modifiers changed from: private */
    public int mPreviousUsableHeight;
    private RelativeLayout mRelativeLayoutBottom;
    private RelativeLayout mRelativeLayoutTop;
    private ScrollView mScrollView;
    /* access modifiers changed from: private */
    public View mSplitView;
    /* access modifiers changed from: private */
    public int mSplitViewHeight;
    private View.OnLayoutChangeListener mSplitViewOnLayoutChangeListener = new View.OnLayoutChangeListener() {
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            int splitViewHeight = 0;
            if (HwFloatingLayout.this.mSplitView != null) {
                splitViewHeight = HwFloatingLayout.this.mSplitView.getHeight();
            }
            if (splitViewHeight != HwFloatingLayout.this.mSplitViewHeight) {
                int unused = HwFloatingLayout.this.mSplitViewHeight = splitViewHeight;
                HwFloatingLayout.this.mViewHolder.requestLayout();
                HwFloatingLayout.this.mViewHolder.invalidate();
            }
        }
    };
    /* access modifiers changed from: private */
    public View mViewHolder;
    private int mViewHolderHeight;

    private static class CustomScrollView extends ScrollView {
        public CustomScrollView(Context context) {
            super(context);
        }

        public CustomScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        /* access modifiers changed from: protected */
        public void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight, lp.width), getChildMeasureSpec(parentHeightMeasureSpec, this.mPaddingTop + this.mPaddingBottom, lp.height));
        }

        /* access modifiers changed from: protected */
        public void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight + lp.leftMargin + lp.rightMargin + widthUsed, lp.width), getChildMeasureSpec(parentHeightMeasureSpec, this.mPaddingTop + this.mPaddingBottom + lp.topMargin + lp.bottomMargin + heightUsed, lp.height));
        }

        public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
            return (nestedScrollAxes & 2) != 0;
        }

        public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
            if (dy > 0) {
                if (!canScrollVerticallyDown(target) && canScrollVerticallyDown(this)) {
                    smoothScrollBy(0, dy);
                    consumed[1] = dy;
                }
            } else if (!canScrollVerticallyUp(target) && canScrollVerticallyUp(this)) {
                smoothScrollBy(0, dy);
                consumed[1] = dy;
            }
        }

        private boolean canScrollVerticallyDown(View view) {
            return canScrollVertically(view, 1);
        }

        private boolean canScrollVerticallyUp(View view) {
            return canScrollVertically(view, -1);
        }

        private boolean canScrollVertically(View view, int direction) {
            if (view instanceof ViewGroup) {
                if (view.canScrollVertically(direction)) {
                    return true;
                }
                ViewGroup vGroup = (ViewGroup) view;
                int count = vGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    if (canScrollVertically(vGroup.getChildAt(i), direction)) {
                        return true;
                    }
                }
            }
            return view.canScrollVertically(direction);
        }
    }

    class MoveViewAnimationListener implements Animation.AnimationListener {
        private ViewGroup.MarginLayoutParams mParams;
        private int mToYDelta;
        private View mView;

        public MoveViewAnimationListener(View view, ViewGroup.MarginLayoutParams params, int toYDelta) {
            this.mView = view;
            this.mParams = params;
            this.mToYDelta = toYDelta;
        }

        public void onAnimationEnd(Animation animation) {
            this.mView.clearAnimation();
            this.mParams.bottomMargin = this.mToYDelta;
            this.mView.setLayoutParams(this.mParams);
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }

    public HwFloatingLayout(Context context) {
        super(context, null);
        this.mContext = context;
        if (this.mContext instanceof Activity) {
            this.mActivity = (Activity) this.mContext;
        }
        this.mIsNavigationBarExist = isNavigationBarExist();
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
        this.mInterpolator = AnimationUtils.loadInterpolator(this.mContext, 34078890);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(KEY_NAVIGATION_BAR_STATUS), false, this.mContentObserver);
        if (this.mHwFloatingView != null) {
            this.mHwFloatingView.getViewTreeObserver().addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        if (this.mHwFloatingView != null) {
            this.mHwFloatingView.getViewTreeObserver().removeOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
        }
        if (this.mSplitView != null) {
            this.mSplitView.removeOnLayoutChangeListener(this.mSplitViewOnLayoutChangeListener);
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mOrientation = newConfig.orientation;
    }

    public void setContentView(int layoutResID) {
        this.mHwFloatingView = (HwFloatingLayout) LayoutInflater.from(this.mContext).inflate(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "hw_immersive_mode_layout"), this);
        this.mRelativeLayoutTop = (RelativeLayout) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.ID, "relative_layout_top"));
        this.mRelativeLayoutBottom = (RelativeLayout) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.ID, "relative_layout_bottom"));
        this.mContentLayout = (FrameLayout) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.ID, "user_content"));
        LayoutInflater.from(this.mContext).inflate(layoutResID, this.mContentLayout);
        this.mLinearLayout = (LinearLayout) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.ID, "linear_layout"));
        this.mScrollView = (ScrollView) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.ID, "scroll_view"));
        this.mScrollView.setVerticalScrollBarEnabled(false);
        this.mViewHolder = findViewById(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.ID, "view_holder"));
        this.mViewHolder.setBackgroundColor(0);
        if (this.mActivity != null) {
            this.mActivity.setContentView(this.mHwFloatingView);
        }
    }

    public void addHwToolbar(View hwToolbar) {
        if (hwToolbar != null) {
            this.mHwToolbar = hwToolbar;
            this.mRelativeLayoutTop.addView(this.mHwToolbar, new RelativeLayout.LayoutParams(-1, -2));
        }
    }

    public void addHwBottomNavigationView(View hwBottomNavigationView) {
        if (hwBottomNavigationView != null) {
            this.mHwBottomNavigationView = hwBottomNavigationView;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            params.addRule(12, -1);
            this.mRelativeLayoutBottom.addView(this.mHwBottomNavigationView, params);
        }
    }

    public void addFAB(View fab) {
        if (fab != null) {
            this.mFab = fab;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(12, -1);
            params.addRule(11, -1);
            this.mRelativeLayoutBottom.addView(this.mFab, params);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean isInMultiWindowOrPictureInPictureMode;
        int navigationbarHeight;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int hwToolbarHeight = 0;
        int hwBottomNavigationViewHeight = 0;
        int fabHeight = 0;
        if (this.mHwToolbar != null) {
            hwToolbarHeight = this.mHwToolbar.getMeasuredHeight();
        }
        if (this.mHwBottomNavigationView != null) {
            hwBottomNavigationViewHeight = this.mHwBottomNavigationView.getMeasuredHeight();
        }
        if (this.mFab != null) {
            fabHeight = this.mFab.getMeasuredHeight();
        }
        if (this.mHwFloatingView != null) {
            View rootView = this.mHwFloatingView.getRootView();
            if (rootView != null) {
                this.mSplitView = rootView.findViewById(16909362);
            }
        }
        if (this.mSplitView != null && !this.mIsSplitViewListenerAdded) {
            this.mIsSplitViewListenerAdded = true;
            this.mSplitView.addOnLayoutChangeListener(this.mSplitViewOnLayoutChangeListener);
        }
        if (this.mActivity == null || (!this.mActivity.isInMultiWindowMode() && !this.mActivity.isInPictureInPictureMode())) {
            isInMultiWindowOrPictureInPictureMode = false;
        } else {
            isInMultiWindowOrPictureInPictureMode = true;
        }
        if (!isNavigationBarExist() || this.mOrientation != 1) {
            navigationbarHeight = 0;
        } else {
            navigationbarHeight = getNavigationHeight();
        }
        this.mViewHolderHeight = this.mHwBottomNavigationViewHeight + navigationbarHeight + this.mSplitViewHeight + this.mFabHeight;
        int linearLayoutMeasureSpec = View.MeasureSpec.makeSafeMeasureSpec(((View.MeasureSpec.getSize(heightMeasureSpec) - hwToolbarHeight) - getStatusBarHeight()) + this.mViewHolderHeight, View.MeasureSpec.getMode(heightMeasureSpec));
        this.mContentLayout.getLayoutParams().height = (View.MeasureSpec.getSize(heightMeasureSpec) - hwToolbarHeight) - getStatusBarHeight();
        this.mViewHolder.getLayoutParams().height = this.mViewHolderHeight;
        if (this.mLinearLayout != null) {
            this.mLinearLayout.measure(widthMeasureSpec, linearLayoutMeasureSpec);
        } else {
            int i = widthMeasureSpec;
        }
        if (!(hwToolbarHeight == this.mHwToolbarHeight && hwBottomNavigationViewHeight == this.mHwBottomNavigationViewHeight && fabHeight == this.mFabHeight && isInMultiWindowOrPictureInPictureMode == this.mIsInMultiWindowOrPictureInPictureMode)) {
            this.mHwToolbarHeight = hwToolbarHeight;
            this.mHwBottomNavigationViewHeight = hwBottomNavigationViewHeight;
            this.mFabHeight = fabHeight;
            this.mIsInMultiWindowOrPictureInPictureMode = isInMultiWindowOrPictureInPictureMode;
            updateSystemWidgetsLayout();
        }
        if (this.mScrollView != null) {
            this.mScrollView.setPadding(0, getStatusBarHeight() + this.mHwToolbarHeight, 0, 0);
        }
    }

    private void moveView(View view, int toYDelta) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int last_bottomMargin = params.bottomMargin;
            if (this.mIsInMultiWindowOrPictureInPictureMode) {
                params.bottomMargin = toYDelta;
                view.setLayoutParams(params);
                return;
            }
            Animation animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (last_bottomMargin - toYDelta));
            animation.setDuration(mDurationMillis);
            animation.setInterpolator(this.mInterpolator);
            animation.setAnimationListener(new MoveViewAnimationListener(view, params, toYDelta));
            view.startAnimation(animation);
        }
    }

    /* access modifiers changed from: private */
    public void updateSystemWidgetsLayout() {
        int bottomMargin = 0;
        if (this.mHwToolbar != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            params.topMargin = getStatusBarHeight();
            this.mHwToolbar.setLayoutParams(params);
        }
        if (this.mIsInMultiWindowOrPictureInPictureMode || !isNavigationBarExist()) {
            this.mNavigationBarHeight = 0;
        } else {
            this.mNavigationBarHeight = getNavigationHeight();
        }
        if (this.mOrientation == 1) {
            bottomMargin = this.mNavigationBarHeight;
        } else if (this.mOrientation == 2) {
            bottomMargin = 0;
        }
        if (this.mHwBottomNavigationView != null) {
            moveView(this.mHwBottomNavigationView, bottomMargin);
        }
        if (this.mSplitView != null) {
            moveView(this.mSplitView, this.mHwBottomNavigationViewHeight + bottomMargin);
        }
        if (this.mFab != null) {
            moveView(this.mFab, this.mHwBottomNavigationViewHeight + bottomMargin + this.mSplitViewHeight);
        }
        setPadding();
    }

    private void setPadding() {
        if (this.mOrientation == 2) {
            this.mRelativeLayoutBottom.setPadding(0, 0, this.mNavigationBarHeight, 0);
            this.mRelativeLayoutTop.setPadding(0, 0, this.mNavigationBarHeight, 0);
            this.mLinearLayout.setPadding(0, 0, this.mNavigationBarHeight, 0);
        }
    }

    /* access modifiers changed from: private */
    public boolean isNavigationBarExist() {
        boolean exist = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), KEY_NAVIGATION_BAR_STATUS, 0) == 0) {
            exist = true;
        }
        return exist;
    }

    private int getStatusBarHeight() {
        return this.mContext.getResources().getDimensionPixelSize(this.mContext.getResources().getIdentifier("status_bar_height", ResLoaderUtil.DIMEN, "android"));
    }

    private int getNavigationHeight() {
        return this.mContext.getResources().getDimensionPixelSize(this.mContext.getResources().getIdentifier("navigation_bar_height", ResLoaderUtil.DIMEN, "android"));
    }
}
