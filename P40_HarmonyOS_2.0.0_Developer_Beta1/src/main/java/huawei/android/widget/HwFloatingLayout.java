package huawei.android.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
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
    private static final long ANIMATION_DURATION = 300;
    private static final int ARRAY_SIZE = 2;
    private static final int DIRECTION_VERTICAL_DOWN = 1;
    private static final int DIRECTION_VERTICAL_UP = -1;
    private static final int HEIGHT_QUARTER_NUM = 4;
    private static final String KEY_NAVIGATION_BAR_STATUS = "navigationbar_is_min";
    private static final String RES_TYPE_ID = "id";
    private static final String TAG = "HwFloatingLayout";
    private Activity mActivity;
    private FrameLayout mContentLayout;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        /* class huawei.android.widget.HwFloatingLayout.AnonymousClass1 */

        @Override // android.database.ContentObserver
        public void onChange(boolean isSelfChange) {
            boolean isExist = HwFloatingLayout.this.isNavigationBarExist();
            if (isExist != HwFloatingLayout.this.mIsNavigationBarExist) {
                HwFloatingLayout.this.mIsNavigationBarExist = isExist;
                HwFloatingLayout.this.updateSystemWidgetsLayout();
            }
        }
    };
    private final Context mContext;
    private View mFab;
    private int mFabHeight;
    private View mHwBottomNavigationView;
    private int mHwBottomNavigationViewHeight;
    private HwFloatingLayout mHwFloatingView;
    private View mHwToolbar;
    private int mHwToolbarHeight;
    private final Rect mInsetsRect = new Rect();
    private Interpolator mInterpolator;
    private boolean mIsInMultiWindowOrPictureInPictureMode;
    private boolean mIsNavigationBarExist;
    private boolean mIsSplitViewListenerAdded;
    private int mLastKeyBoardHeight;
    private LinearLayout mLinearLayout;
    private int mNavigationBarHeight;
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        /* class huawei.android.widget.HwFloatingLayout.AnonymousClass3 */

        @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
        public void onGlobalLayout() {
            int screenHeigh;
            int keyboardHeight;
            View view;
            Rect rect = new Rect();
            HwFloatingLayout.this.mHwFloatingView.getWindowVisibleDisplayFrame(rect);
            int usableHeight = rect.bottom;
            if (usableHeight != HwFloatingLayout.this.mPreviousUsableHeight && (keyboardHeight = (screenHeigh = HwFloatingLayout.this.mHwFloatingView.getRootView().getHeight()) - usableHeight) != HwFloatingLayout.this.mLastKeyBoardHeight) {
                HwFloatingLayout.this.mLastKeyBoardHeight = keyboardHeight;
                if (HwFloatingLayout.this.mLinearLayout != null) {
                    HwFloatingLayout.this.mPreviousUsableHeight = usableHeight;
                    if (keyboardHeight <= screenHeigh / 4) {
                        HwFloatingLayout.this.mLinearLayout.setPadding(0, 0, 0, 0);
                    } else if (HwFloatingLayout.this.mActivity != null && (view = HwFloatingLayout.this.mActivity.getCurrentFocus()) != null && (view instanceof TextView)) {
                        int[] location = new int[2];
                        view.getLocationOnScreen(location);
                        int bottomPadding = keyboardHeight - ((screenHeigh - location[1]) - view.getHeight());
                        if (bottomPadding > 0) {
                            HwFloatingLayout.this.mLinearLayout.setPadding(0, -bottomPadding, 0, 0);
                        }
                    }
                }
            }
        }
    };
    private int mOrientation;
    private int mPreviousUsableHeight;
    private RelativeLayout mRelativeLayoutBottom;
    private RelativeLayout mRelativeLayoutTop;
    private ScrollView mScrollView;
    private View mSplitView;
    private int mSplitViewHeight;
    private View.OnLayoutChangeListener mSplitViewOnLayoutChangeListener = new View.OnLayoutChangeListener() {
        /* class huawei.android.widget.HwFloatingLayout.AnonymousClass2 */

        @Override // android.view.View.OnLayoutChangeListener
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            int splitViewHeight = 0;
            if (HwFloatingLayout.this.mSplitView != null) {
                splitViewHeight = HwFloatingLayout.this.mSplitView.getHeight();
            }
            if (splitViewHeight != HwFloatingLayout.this.mSplitViewHeight) {
                HwFloatingLayout.this.mSplitViewHeight = splitViewHeight;
                HwFloatingLayout.this.mViewHolder.requestLayout();
                HwFloatingLayout.this.mViewHolder.invalidate();
            }
        }
    };
    private View mViewHolder;
    private int mViewHolderHeight;

    public HwFloatingLayout(Context context) {
        super(context, null);
        this.mContext = context;
        Context context2 = this.mContext;
        if (context2 instanceof Activity) {
            this.mActivity = (Activity) context2;
        }
        this.mIsNavigationBarExist = isNavigationBarExist();
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
        this.mInterpolator = AnimationUtils.loadInterpolator(this.mContext, 34078890);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x002f  */
    /* JADX WARNING: Removed duplicated region for block: B:15:? A[RETURN, SYNTHETIC] */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        HwFloatingLayout hwFloatingLayout;
        super.onAttachedToWindow();
        try {
            Uri uri = Settings.Global.getUriFor(KEY_NAVIGATION_BAR_STATUS);
            if (uri != null) {
                if (this.mContentObserver != null) {
                    this.mContext.getContentResolver().registerContentObserver(uri, false, this.mContentObserver);
                    hwFloatingLayout = this.mHwFloatingView;
                    if (hwFloatingLayout == null) {
                        hwFloatingLayout.getViewTreeObserver().addOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
                        return;
                    }
                    return;
                }
            }
            Log.e(TAG, "onAttachedToWindow: uri is null or contentObserver is null");
        } catch (NullPointerException e) {
            Log.e(TAG, "onAttachedToWindow: getUriFor has exception");
        }
        hwFloatingLayout = this.mHwFloatingView;
        if (hwFloatingLayout == null) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mContext.getContentResolver().unregisterContentObserver(this.mContentObserver);
        HwFloatingLayout hwFloatingLayout = this.mHwFloatingView;
        if (hwFloatingLayout != null) {
            hwFloatingLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this.mOnGlobalLayoutListener);
        }
        View view = this.mSplitView;
        if (view != null) {
            view.removeOnLayoutChangeListener(this.mSplitViewOnLayoutChangeListener);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mOrientation = newConfig.orientation;
    }

    public void setContentView(int layoutResId) {
        View view = LayoutInflater.from(this.mContext).inflate(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.LAYOUT, "hw_immersive_mode_layout"), this);
        if (view instanceof HwFloatingLayout) {
            this.mHwFloatingView = (HwFloatingLayout) view;
        }
        this.mRelativeLayoutTop = (RelativeLayout) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, "id", "relative_layout_top"));
        this.mRelativeLayoutBottom = (RelativeLayout) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, "id", "relative_layout_bottom"));
        this.mContentLayout = (FrameLayout) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, "id", "user_content"));
        LayoutInflater.from(this.mContext).inflate(layoutResId, this.mContentLayout);
        this.mLinearLayout = (LinearLayout) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, "id", "linear_layout"));
        this.mScrollView = (ScrollView) findViewById(ResLoader.getInstance().getIdentifier(this.mContext, "id", "scroll_view"));
        this.mScrollView.setVerticalScrollBarEnabled(false);
        this.mViewHolder = findViewById(ResLoader.getInstance().getIdentifier(this.mContext, "id", "view_holder"));
        this.mViewHolder.setBackgroundColor(0);
        Activity activity = this.mActivity;
        if (activity != null) {
            activity.setContentView(this.mHwFloatingView);
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
    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int navigationbarHeight;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int hwToolbarHeight = 0;
        int hwBottomNavigationViewHeight = 0;
        int fabHeight = 0;
        View view = this.mHwToolbar;
        if (view != null) {
            hwToolbarHeight = view.getMeasuredHeight();
        }
        View view2 = this.mHwBottomNavigationView;
        if (view2 != null) {
            hwBottomNavigationViewHeight = view2.getMeasuredHeight();
        }
        View view3 = this.mFab;
        if (view3 != null) {
            fabHeight = view3.getMeasuredHeight();
        }
        handleSplitActionBar();
        boolean isInMultiWindowOrPictureInPictureMode = true;
        if (!isNavigationBarExist() || this.mOrientation != 1) {
            navigationbarHeight = 0;
        } else {
            navigationbarHeight = getNavigationHeight();
        }
        this.mViewHolderHeight = this.mHwBottomNavigationViewHeight + navigationbarHeight + this.mSplitViewHeight + this.mFabHeight;
        int linearLayoutMeasureSpec = View.MeasureSpec.makeSafeMeasureSpec(((View.MeasureSpec.getSize(heightMeasureSpec) - hwToolbarHeight) - getStatusBarHeight()) + this.mViewHolderHeight, View.MeasureSpec.getMode(heightMeasureSpec));
        this.mContentLayout.getLayoutParams().height = (View.MeasureSpec.getSize(heightMeasureSpec) - hwToolbarHeight) - getStatusBarHeight();
        this.mViewHolder.getLayoutParams().height = this.mViewHolderHeight;
        LinearLayout linearLayout = this.mLinearLayout;
        if (linearLayout != null) {
            linearLayout.measure(widthMeasureSpec, linearLayoutMeasureSpec);
        }
        Activity activity = this.mActivity;
        if (activity == null || (!activity.isInMultiWindowMode() && !this.mActivity.isInPictureInPictureMode())) {
            isInMultiWindowOrPictureInPictureMode = false;
        }
        if (!(hwToolbarHeight == this.mHwToolbarHeight && hwBottomNavigationViewHeight == this.mHwBottomNavigationViewHeight && fabHeight == this.mFabHeight && isInMultiWindowOrPictureInPictureMode == this.mIsInMultiWindowOrPictureInPictureMode)) {
            this.mHwToolbarHeight = hwToolbarHeight;
            this.mHwBottomNavigationViewHeight = hwBottomNavigationViewHeight;
            this.mFabHeight = fabHeight;
            this.mIsInMultiWindowOrPictureInPictureMode = isInMultiWindowOrPictureInPictureMode;
            updateSystemWidgetsLayout();
        }
        ScrollView scrollView = this.mScrollView;
        if (scrollView != null) {
            scrollView.setPadding(0, getStatusBarHeight() + this.mHwToolbarHeight, 0, 0);
        }
    }

    private void handleSplitActionBar() {
        View rootView;
        HwFloatingLayout hwFloatingLayout = this.mHwFloatingView;
        if (!(hwFloatingLayout == null || (rootView = hwFloatingLayout.getRootView()) == null)) {
            this.mSplitView = rootView.findViewById(16909435);
        }
        View rootView2 = this.mSplitView;
        if (rootView2 != null && !this.mIsSplitViewListenerAdded) {
            this.mIsSplitViewListenerAdded = true;
            rootView2.addOnLayoutChangeListener(this.mSplitViewOnLayoutChangeListener);
        }
    }

    private void moveView(View view, int toDeltaY) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            int lastBottomMargin = params.bottomMargin;
            if (this.mIsInMultiWindowOrPictureInPictureMode) {
                params.bottomMargin = toDeltaY;
                view.setLayoutParams(params);
                return;
            }
            Animation animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (lastBottomMargin - toDeltaY));
            animation.setDuration(ANIMATION_DURATION);
            animation.setInterpolator(this.mInterpolator);
            animation.setAnimationListener(new MoveViewAnimationListener(view, params, toDeltaY));
            view.startAnimation(animation);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSystemWidgetsLayout() {
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
        int i = this.mOrientation;
        if (i == 1) {
            bottomMargin = this.mNavigationBarHeight;
        } else if (i == 2) {
            bottomMargin = 0;
        }
        View view = this.mHwBottomNavigationView;
        if (view != null) {
            moveView(view, bottomMargin);
        }
        View view2 = this.mSplitView;
        if (view2 != null) {
            moveView(view2, this.mHwBottomNavigationViewHeight + bottomMargin);
        }
        View view3 = this.mFab;
        if (view3 != null) {
            moveView(view3, this.mHwBottomNavigationViewHeight + bottomMargin + this.mSplitViewHeight);
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
    /* access modifiers changed from: public */
    private boolean isNavigationBarExist() {
        boolean isExist = false;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), KEY_NAVIGATION_BAR_STATUS, 0) == 0) {
            isExist = true;
        }
        return isExist;
    }

    private int getStatusBarHeight() {
        return this.mContext.getResources().getDimensionPixelSize(this.mContext.getResources().getIdentifier("status_bar_height", ResLoaderUtil.DIMEN, "android"));
    }

    private int getNavigationHeight() {
        return this.mContext.getResources().getDimensionPixelSize(this.mContext.getResources().getIdentifier("navigation_bar_height", ResLoaderUtil.DIMEN, "android"));
    }

    public static class CustomScrollView extends ScrollView {
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
        @Override // android.widget.ScrollView, android.view.ViewGroup
        public void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight, lp.width), getChildMeasureSpec(parentHeightMeasureSpec, this.mPaddingTop + this.mPaddingBottom, lp.height));
        }

        /* access modifiers changed from: protected */
        @Override // android.widget.ScrollView, android.view.ViewGroup
        public void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
            ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) layoutParams;
                child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight + lp.leftMargin + lp.rightMargin + widthUsed, lp.width), getChildMeasureSpec(parentHeightMeasureSpec, this.mPaddingTop + this.mPaddingBottom + lp.topMargin + lp.bottomMargin + heightUsed, lp.height));
            }
        }

        @Override // android.widget.ScrollView, android.view.ViewParent, android.view.ViewGroup
        public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
            return (nestedScrollAxes & 2) != 0;
        }

        @Override // android.view.ViewParent, android.view.ViewGroup
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
                ViewGroup viewGroup = (ViewGroup) view;
                int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    if (canScrollVertically(viewGroup.getChildAt(i), direction)) {
                        return true;
                    }
                }
            }
            if (view.canScrollVertically(direction)) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public class MoveViewAnimationListener implements Animation.AnimationListener {
        private ViewGroup.MarginLayoutParams mParams;
        private int mToDeltaY;
        private View mView;

        MoveViewAnimationListener(View view, ViewGroup.MarginLayoutParams params, int toDeltaY) {
            this.mView = view;
            this.mParams = params;
            this.mToDeltaY = toDeltaY;
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationEnd(Animation animation) {
            this.mView.clearAnimation();
            ViewGroup.MarginLayoutParams marginLayoutParams = this.mParams;
            marginLayoutParams.bottomMargin = this.mToDeltaY;
            this.mView.setLayoutParams(marginLayoutParams);
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationRepeat(Animation animation) {
        }

        @Override // android.view.animation.Animation.AnimationListener
        public void onAnimationStart(Animation animation) {
        }
    }
}
