package com.android.internal.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.UnsupportedAppUsage;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.IntProperty;
import android.util.Log;
import android.util.Property;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.OverScroller;
import android.widget.Toolbar;
import com.android.internal.R;
import com.android.internal.view.menu.MenuPresenter;

public class ActionBarOverlayLayout extends ViewGroup implements DecorContentParent {
    public static final Property<ActionBarOverlayLayout, Integer> ACTION_BAR_HIDE_OFFSET = new IntProperty<ActionBarOverlayLayout>("actionBarHideOffset") {
        /* class com.android.internal.widget.ActionBarOverlayLayout.AnonymousClass5 */

        public void setValue(ActionBarOverlayLayout object, int value) {
            object.setActionBarHideOffset(value);
        }

        public Integer get(ActionBarOverlayLayout object) {
            return Integer.valueOf(object.getActionBarHideOffset());
        }
    };
    static final int[] ATTRS = {16843499, 16842841};
    private static final String DISPLAY_NOTCH_STATUS = "display_notch_status";
    private static final int DISPLAY_NOTCH_STATUS_DEFAULT = 0;
    private static final String KEY_NAVIGATION_BAR_STATUS = "navigationbar_is_min";
    private static final int LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS = 1;
    private static final String TAG = "ActionBarOverlayLayout";
    private final int ACTION_BAR_ANIMATE_DELAY = 600;
    private ActionBarContainer mActionBarBottom;
    private int mActionBarHeight;
    private ActionBarContainer mActionBarTop;
    private ActionBarVisibilityCallback mActionBarVisibilityCallback;
    private final Runnable mAddActionBarHideOffset = new Runnable() {
        /* class com.android.internal.widget.ActionBarOverlayLayout.AnonymousClass4 */

        public void run() {
            ActionBarOverlayLayout.this.haltActionBarHideOffsetAnimations();
            ActionBarOverlayLayout actionBarOverlayLayout = ActionBarOverlayLayout.this;
            actionBarOverlayLayout.mCurrentActionBarTopAnimator = actionBarOverlayLayout.mActionBarTop.animate().translationY((float) (-ActionBarOverlayLayout.this.mActionBarTop.getHeight())).setListener(ActionBarOverlayLayout.this.mTopAnimatorListener);
            if (ActionBarOverlayLayout.this.mActionBarBottom != null && ActionBarOverlayLayout.this.mActionBarBottom.getVisibility() != 8) {
                ActionBarOverlayLayout actionBarOverlayLayout2 = ActionBarOverlayLayout.this;
                actionBarOverlayLayout2.mCurrentActionBarBottomAnimator = actionBarOverlayLayout2.mActionBarBottom.animate().translationY((float) ActionBarOverlayLayout.this.mActionBarBottom.getHeight()).setListener(ActionBarOverlayLayout.this.mBottomAnimatorListener);
            }
        }
    };
    private boolean mAlwaysSplit = false;
    private boolean mAnimatingForFling;
    private final Rect mBaseContentInsets = new Rect();
    private WindowInsets mBaseInnerInsets = WindowInsets.CONSUMED;
    private final Animator.AnimatorListener mBottomAnimatorListener = new AnimatorListenerAdapter() {
        /* class com.android.internal.widget.ActionBarOverlayLayout.AnonymousClass2 */

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationEnd(Animator animation) {
            ActionBarOverlayLayout.this.mCurrentActionBarBottomAnimator = null;
            ActionBarOverlayLayout.this.mAnimatingForFling = false;
        }

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationCancel(Animator animation) {
            ActionBarOverlayLayout.this.mCurrentActionBarBottomAnimator = null;
            ActionBarOverlayLayout.this.mAnimatingForFling = false;
        }
    };
    private View mContent;
    private final Rect mContentInsets = new Rect();
    private ViewPropertyAnimator mCurrentActionBarBottomAnimator;
    private ViewPropertyAnimator mCurrentActionBarTopAnimator;
    private DecorToolbar mDecorToolbar;
    private OverScroller mFlingEstimator;
    private boolean mHasNonEmbeddedTabs;
    private boolean mHideOnContentScroll;
    private int mHideOnContentScrollReference;
    private boolean mIgnoreWindowContentOverlay;
    private WindowInsets mInnerInsets = WindowInsets.CONSUMED;
    private final Rect mLastBaseContentInsets = new Rect();
    private WindowInsets mLastBaseInnerInsets = WindowInsets.CONSUMED;
    private WindowInsets mLastInnerInsets = WindowInsets.CONSUMED;
    private int mLastSystemUiVisibility;
    private boolean mOverlayMode;
    private final Runnable mRemoveActionBarHideOffset = new Runnable() {
        /* class com.android.internal.widget.ActionBarOverlayLayout.AnonymousClass3 */

        public void run() {
            ActionBarOverlayLayout.this.haltActionBarHideOffsetAnimations();
            ActionBarOverlayLayout actionBarOverlayLayout = ActionBarOverlayLayout.this;
            actionBarOverlayLayout.mCurrentActionBarTopAnimator = actionBarOverlayLayout.mActionBarTop.animate().translationY(0.0f).setListener(ActionBarOverlayLayout.this.mTopAnimatorListener);
            if (ActionBarOverlayLayout.this.mActionBarBottom != null && ActionBarOverlayLayout.this.mActionBarBottom.getVisibility() != 8) {
                ActionBarOverlayLayout actionBarOverlayLayout2 = ActionBarOverlayLayout.this;
                actionBarOverlayLayout2.mCurrentActionBarBottomAnimator = actionBarOverlayLayout2.mActionBarBottom.animate().translationY(0.0f).setListener(ActionBarOverlayLayout.this.mBottomAnimatorListener);
            }
        }
    };
    private final Animator.AnimatorListener mTopAnimatorListener = new AnimatorListenerAdapter() {
        /* class com.android.internal.widget.ActionBarOverlayLayout.AnonymousClass1 */

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationEnd(Animator animation) {
            ActionBarOverlayLayout.this.mCurrentActionBarTopAnimator = null;
            ActionBarOverlayLayout.this.mAnimatingForFling = false;
        }

        @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
        public void onAnimationCancel(Animator animation) {
            ActionBarOverlayLayout.this.mCurrentActionBarTopAnimator = null;
            ActionBarOverlayLayout.this.mAnimatingForFling = false;
        }
    };
    private Drawable mWindowContentOverlay;
    private int mWindowVisibility = 0;

    public interface ActionBarVisibilityCallback {
        void enableContentAnimations(boolean z);

        void hideForSystem();

        void onContentScrollStarted();

        void onContentScrollStopped();

        void onWindowVisibilityChanged(int i);

        void showForSystem();
    }

    public ActionBarOverlayLayout(Context context) {
        super(context);
        init(context);
    }

    @UnsupportedAppUsage
    public ActionBarOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(ATTRS);
        boolean z = false;
        this.mActionBarHeight = ta.getDimensionPixelSize(0, 0);
        this.mWindowContentOverlay = ta.getDrawable(1);
        setWillNotDraw(this.mWindowContentOverlay == null);
        ta.recycle();
        if (context.getApplicationInfo().targetSdkVersion < 19) {
            z = true;
        }
        this.mIgnoreWindowContentOverlay = z;
        this.mFlingEstimator = new OverScroller(context);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        haltActionBarHideOffsetAnimations();
    }

    public void setActionBarVisibilityCallback(ActionBarVisibilityCallback cb) {
        this.mActionBarVisibilityCallback = cb;
        if (getWindowToken() != null) {
            this.mActionBarVisibilityCallback.onWindowVisibilityChanged(this.mWindowVisibility);
            if (this.mLastSystemUiVisibility != 0) {
                onWindowSystemUiVisibilityChanged(this.mLastSystemUiVisibility);
                requestApplyInsets();
            }
        }
    }

    public void setOverlayMode(boolean overlayMode) {
        this.mOverlayMode = overlayMode;
        this.mIgnoreWindowContentOverlay = overlayMode && getContext().getApplicationInfo().targetSdkVersion < 19;
    }

    public boolean isInOverlayMode() {
        return this.mOverlayMode;
    }

    public void setHasNonEmbeddedTabs(boolean hasNonEmbeddedTabs) {
        this.mHasNonEmbeddedTabs = hasNonEmbeddedTabs;
    }

    public void setShowingForActionMode(boolean showing) {
        if (!showing) {
            setDisabledSystemUiVisibility(0);
        } else if ((getWindowSystemUiVisibility() & 1280) == 1280) {
            setDisabledSystemUiVisibility(4);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        init(getContext());
        requestApplyInsets();
    }

    @Override // android.view.View
    public void onWindowSystemUiVisibilityChanged(int visible) {
        super.onWindowSystemUiVisibilityChanged(visible);
        pullChildren();
        int diff = this.mLastSystemUiVisibility ^ visible;
        this.mLastSystemUiVisibility = visible;
        boolean z = true;
        boolean barVisible = (visible & 4) == 0;
        boolean stable = (visible & 256) != 0;
        ActionBarVisibilityCallback actionBarVisibilityCallback = this.mActionBarVisibilityCallback;
        if (actionBarVisibilityCallback != null) {
            if (stable) {
                z = false;
            }
            actionBarVisibilityCallback.enableContentAnimations(z);
            if (barVisible || !stable) {
                this.mActionBarVisibilityCallback.showForSystem();
            } else {
                this.mActionBarVisibilityCallback.hideForSystem();
            }
        }
        if ((diff & 256) != 0 && this.mActionBarVisibilityCallback != null) {
            requestApplyInsets();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.mWindowVisibility = visibility;
        ActionBarVisibilityCallback actionBarVisibilityCallback = this.mActionBarVisibilityCallback;
        if (actionBarVisibilityCallback != null) {
            actionBarVisibilityCallback.onWindowVisibilityChanged(visibility);
        }
    }

    private boolean isUseSideRegion(Context context) {
        WindowManager.LayoutParams attrs = null;
        Activity activity = getActivityFromContext(context);
        if (activity != null) {
            attrs = activity.getWindow().getAttributes();
        }
        if (attrs == null || attrs.layoutInDisplaySideMode != 1) {
            return false;
        }
        return true;
    }

    private boolean isNavigationBarExist(Context context) {
        if (context == null || Settings.Global.getInt(context.getContentResolver(), "navigationbar_is_min", 0) != 0) {
            return false;
        }
        return true;
    }

    private int getDisplayRotate(Context context) {
        WindowManager wmManager;
        if (context == null || (wmManager = (WindowManager) context.getSystemService("window")) == null) {
            return 0;
        }
        return wmManager.getDefaultDisplay().getRotation();
    }

    private boolean needDoCutoutFit(Context context) {
        WindowManager.LayoutParams attrs = null;
        int systemUiVisibillity = getWindowSystemUiVisibility();
        Activity activity = getActivityFromContext(context);
        if (activity != null) {
            attrs = activity.getWindow().getAttributes();
        }
        if (attrs == null) {
            return false;
        }
        if (((systemUiVisibillity & 1024) != 0 || (systemUiVisibillity & 2048) != 0 || (systemUiVisibillity & 512) != 0 || (attrs.flags & 67108864) != 0) && attrs.layoutInDisplayCutoutMode == 1 && getDisplayCutoutStatus(context)) {
            return true;
        }
        return false;
    }

    private boolean getDisplayCutoutStatus(Context context) {
        if (context == null || Settings.Secure.getInt(context.getContentResolver(), DISPLAY_NOTCH_STATUS, 0) != 0) {
            return false;
        }
        return true;
    }

    private Activity getActivityFromContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private boolean applyInsets(View view, Rect insets, boolean left, boolean top, boolean bottom, boolean right) {
        boolean changed = false;
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (left && lp.leftMargin != insets.left) {
            changed = true;
            if (!HwWidgetFactory.isHwTheme(getContext()) || !needDoCutoutFit(getContext())) {
                lp.leftMargin = insets.left;
            }
        }
        if (top && lp.topMargin != insets.top) {
            changed = true;
            lp.topMargin = insets.top;
        }
        if (right && lp.rightMargin != insets.right) {
            changed = true;
            int rotate = getDisplayRotate(getContext());
            if (!needDoCutoutFit(getContext()) || !HwWidgetFactory.isHwTheme(getContext()) || 3 != rotate || isNavigationBarExist(getContext())) {
                lp.rightMargin = insets.right;
            } else {
                lp.rightMargin = 0;
            }
        }
        if (!bottom || lp.bottomMargin == insets.bottom) {
            return changed;
        }
        lp.bottomMargin = insets.bottom;
        return true;
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        pullChildren();
        getWindowSystemUiVisibility();
        Rect systemInsets = insets.getSystemWindowInsetsAsRect();
        boolean changed = applyInsets(this.mActionBarTop, systemInsets, true, true, false, true);
        ActionBarContainer actionBarContainer = this.mActionBarBottom;
        if (actionBarContainer != null) {
            changed |= applyInsets(actionBarContainer, systemInsets, true, false, true, true);
        }
        computeSystemWindowInsets(insets, this.mBaseContentInsets);
        this.mBaseInnerInsets = insets.inset(this.mBaseContentInsets);
        if (!this.mLastBaseInnerInsets.equals(this.mBaseInnerInsets)) {
            changed = true;
            this.mLastBaseInnerInsets = this.mBaseInnerInsets;
        }
        if (!this.mLastBaseContentInsets.equals(this.mBaseContentInsets)) {
            changed = true;
            this.mLastBaseContentInsets.set(this.mBaseContentInsets);
        }
        if (changed) {
            if (HwWidgetFactory.isHwTheme(getContext()) && (needDoCutoutFit(getContext()) || isUseSideRegion(getContext()))) {
                ActionBarContainer actionBarContainer2 = this.mActionBarTop;
                if (actionBarContainer2 != null) {
                    actionBarContainer2.dispatchApplyWindowInsets(insets);
                }
                ActionBarContainer actionBarContainer3 = this.mActionBarBottom;
                if (actionBarContainer3 != null) {
                    actionBarContainer3.dispatchApplyWindowInsets(insets);
                }
            }
            requestLayout();
        }
        return WindowInsets.CONSUMED;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -1);
    }

    @Override // android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxHeight;
        ActionBarContainer actionBarContainer;
        pullChildren();
        int topInset = 0;
        int bottomInset = 0;
        measureChildWithMargins(this.mActionBarTop, widthMeasureSpec, 0, heightMeasureSpec, 0);
        LayoutParams lp = (LayoutParams) this.mActionBarTop.getLayoutParams();
        int maxWidth = Math.max(0, this.mActionBarTop.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
        int maxHeight2 = Math.max(0, this.mActionBarTop.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
        int childState = combineMeasuredStates(0, this.mActionBarTop.getMeasuredState());
        ActionBarContainer actionBarContainer2 = this.mActionBarBottom;
        if (actionBarContainer2 != null) {
            measureChildWithMargins(actionBarContainer2, widthMeasureSpec, 0, heightMeasureSpec, 0);
            LayoutParams lp2 = (LayoutParams) this.mActionBarBottom.getLayoutParams();
            maxWidth = Math.max(maxWidth, this.mActionBarBottom.getMeasuredWidth() + lp2.leftMargin + lp2.rightMargin);
            int maxHeight3 = Math.max(maxHeight2, this.mActionBarBottom.getMeasuredHeight() + lp2.topMargin + lp2.bottomMargin);
            childState = combineMeasuredStates(childState, this.mActionBarBottom.getMeasuredState());
            maxHeight = maxHeight3;
        } else {
            maxHeight = maxHeight2;
        }
        boolean stable = (getWindowSystemUiVisibility() & 256) != 0;
        if (stable) {
            topInset = this.mActionBarHeight;
            if (this.mHasNonEmbeddedTabs && this.mActionBarTop.getTabContainer() != null) {
                topInset += this.mActionBarHeight;
            }
        } else if (this.mActionBarTop.getVisibility() != 8) {
            topInset = this.mActionBarTop.getMeasuredHeight();
        }
        if (this.mDecorToolbar.isSplit() && (actionBarContainer = this.mActionBarBottom) != null) {
            bottomInset = stable ? this.mActionBarHeight : actionBarContainer.getMeasuredHeight();
        }
        this.mContentInsets.set(this.mBaseContentInsets);
        this.mInnerInsets = this.mBaseInnerInsets;
        if (this.mOverlayMode || stable) {
            WindowInsets windowInsets = this.mInnerInsets;
            this.mInnerInsets = windowInsets.replaceSystemWindowInsets(windowInsets.getSystemWindowInsetLeft(), this.mInnerInsets.getSystemWindowInsetTop() + topInset, this.mInnerInsets.getSystemWindowInsetRight(), this.mInnerInsets.getSystemWindowInsetBottom() + bottomInset);
        } else {
            this.mContentInsets.top += topInset;
            this.mContentInsets.bottom += bottomInset;
            this.mInnerInsets = this.mInnerInsets.inset(0, topInset, 0, bottomInset);
        }
        applyInsets(this.mContent, this.mContentInsets, true, true, true, true);
        if (!this.mLastInnerInsets.equals(this.mInnerInsets)) {
            WindowInsets windowInsets2 = this.mInnerInsets;
            this.mLastInnerInsets = windowInsets2;
            this.mContent.dispatchApplyWindowInsets(windowInsets2);
        }
        measureChildWithMargins(this.mContent, widthMeasureSpec, 0, heightMeasureSpec, 0);
        LayoutParams lp3 = (LayoutParams) this.mContent.getLayoutParams();
        int maxWidth2 = Math.max(maxWidth, this.mContent.getMeasuredWidth() + lp3.leftMargin + lp3.rightMargin);
        int maxHeight4 = Math.max(maxHeight, this.mContent.getMeasuredHeight() + lp3.topMargin + lp3.bottomMargin);
        int childState2 = combineMeasuredStates(childState, this.mContent.getMeasuredState());
        setMeasuredDimension(resolveSizeAndState(Math.max(maxWidth2 + getPaddingLeft() + getPaddingRight(), getSuggestedMinimumWidth()), widthMeasureSpec, childState2), resolveSizeAndState(Math.max(maxHeight4 + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight()), heightMeasureSpec, childState2 << 16));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childTop;
        int count = getChildCount();
        int parentLeft = getPaddingLeft();
        int paddingRight = (right - left) - getPaddingRight();
        int parentTop = getPaddingTop();
        int parentBottom = (bottom - top) - getPaddingBottom();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int childLeft = lp.leftMargin + parentLeft;
                if (child == this.mActionBarBottom) {
                    childTop = (parentBottom - height) - lp.bottomMargin;
                } else {
                    childTop = lp.topMargin + parentTop;
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    @Override // android.view.View
    public void draw(Canvas c) {
        super.draw(c);
        if (this.mWindowContentOverlay != null && !this.mIgnoreWindowContentOverlay) {
            int top = this.mActionBarTop.getVisibility() == 0 ? (int) (((float) this.mActionBarTop.getBottom()) + this.mActionBarTop.getTranslationY() + 0.5f) : 0;
            this.mWindowContentOverlay.setBounds(0, top, getWidth(), this.mWindowContentOverlay.getIntrinsicHeight() + top);
            this.mWindowContentOverlay.draw(c);
        }
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean onStartNestedScroll(View child, View target, int axes) {
        if ((axes & 2) == 0 || this.mActionBarTop.getVisibility() != 0) {
            return false;
        }
        return this.mHideOnContentScroll;
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        this.mHideOnContentScrollReference = getActionBarHideOffset();
        haltActionBarHideOffsetAnimations();
        ActionBarVisibilityCallback actionBarVisibilityCallback = this.mActionBarVisibilityCallback;
        if (actionBarVisibilityCallback != null) {
            actionBarVisibilityCallback.onContentScrollStarted();
        }
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        this.mHideOnContentScrollReference += dyConsumed;
        setActionBarHideOffset(this.mHideOnContentScrollReference);
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onStopNestedScroll(View target) {
        super.onStopNestedScroll(target);
        if (this.mHideOnContentScroll && !this.mAnimatingForFling) {
            if (this.mHideOnContentScrollReference <= this.mActionBarTop.getHeight()) {
                postRemoveActionBarHideOffset();
            } else {
                postAddActionBarHideOffset();
            }
        }
        ActionBarVisibilityCallback actionBarVisibilityCallback = this.mActionBarVisibilityCallback;
        if (actionBarVisibilityCallback != null) {
            actionBarVisibilityCallback.onContentScrollStopped();
        }
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (!this.mHideOnContentScroll || !consumed) {
            return false;
        }
        if (shouldHideActionBarOnFling(velocityX, velocityY)) {
            addActionBarHideOffset();
        } else {
            removeActionBarHideOffset();
        }
        this.mAnimatingForFling = true;
        return true;
    }

    /* access modifiers changed from: package-private */
    public void pullChildren() {
        if (this.mContent == null) {
            this.mContent = findViewById(16908290);
            this.mActionBarTop = (ActionBarContainer) findViewById(R.id.action_bar_container);
            this.mDecorToolbar = getDecorToolbar(findViewById(R.id.action_bar));
            this.mActionBarBottom = (ActionBarContainer) findViewById(R.id.split_action_bar);
        }
    }

    private DecorToolbar getDecorToolbar(View view) {
        if (view instanceof DecorToolbar) {
            return (DecorToolbar) view;
        }
        if (view instanceof Toolbar) {
            return ((Toolbar) view).getWrapper();
        }
        throw new IllegalStateException("Can't make a decor toolbar out of " + view.getClass().getSimpleName());
    }

    public void setHideOnContentScrollEnabled(boolean hideOnContentScroll) {
        if (hideOnContentScroll != this.mHideOnContentScroll) {
            this.mHideOnContentScroll = hideOnContentScroll;
            if (!hideOnContentScroll) {
                stopNestedScroll();
                haltActionBarHideOffsetAnimations();
                setActionBarHideOffset(0);
            }
        }
    }

    public boolean isHideOnContentScrollEnabled() {
        return this.mHideOnContentScroll;
    }

    public int getActionBarHideOffset() {
        ActionBarContainer actionBarContainer = this.mActionBarTop;
        if (actionBarContainer != null) {
            return -((int) actionBarContainer.getTranslationY());
        }
        return 0;
    }

    public void setActionBarHideOffset(int offset) {
        haltActionBarHideOffsetAnimations();
        int topHeight = this.mActionBarTop.getHeight();
        int offset2 = Math.max(0, Math.min(offset, topHeight));
        this.mActionBarTop.setTranslationY((float) (-offset2));
        ActionBarContainer actionBarContainer = this.mActionBarBottom;
        if (actionBarContainer != null && actionBarContainer.getVisibility() != 8) {
            this.mActionBarBottom.setTranslationY((float) ((int) (((float) this.mActionBarBottom.getHeight()) * (((float) offset2) / ((float) topHeight)))));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void haltActionBarHideOffsetAnimations() {
        removeCallbacks(this.mRemoveActionBarHideOffset);
        removeCallbacks(this.mAddActionBarHideOffset);
        ViewPropertyAnimator viewPropertyAnimator = this.mCurrentActionBarTopAnimator;
        if (viewPropertyAnimator != null) {
            viewPropertyAnimator.cancel();
        }
        ViewPropertyAnimator viewPropertyAnimator2 = this.mCurrentActionBarBottomAnimator;
        if (viewPropertyAnimator2 != null) {
            viewPropertyAnimator2.cancel();
        }
    }

    private void postRemoveActionBarHideOffset() {
        haltActionBarHideOffsetAnimations();
        postDelayed(this.mRemoveActionBarHideOffset, 600);
    }

    private void postAddActionBarHideOffset() {
        haltActionBarHideOffsetAnimations();
        postDelayed(this.mAddActionBarHideOffset, 600);
    }

    private void removeActionBarHideOffset() {
        haltActionBarHideOffsetAnimations();
        this.mRemoveActionBarHideOffset.run();
    }

    private void addActionBarHideOffset() {
        haltActionBarHideOffsetAnimations();
        this.mAddActionBarHideOffset.run();
    }

    private boolean shouldHideActionBarOnFling(float velocityX, float velocityY) {
        this.mFlingEstimator.fling(0, 0, 0, (int) velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return this.mFlingEstimator.getFinalY() > this.mActionBarTop.getHeight();
    }

    @Override // com.android.internal.widget.DecorContentParent
    @UnsupportedAppUsage
    public void setWindowCallback(Window.Callback cb) {
        pullChildren();
        this.mDecorToolbar.setWindowCallback(cb);
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void setWindowTitle(CharSequence title) {
        pullChildren();
        this.mDecorToolbar.setWindowTitle(title);
    }

    @Override // com.android.internal.widget.DecorContentParent
    public CharSequence getTitle() {
        pullChildren();
        return this.mDecorToolbar.getTitle();
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void initFeature(int windowFeature) {
        pullChildren();
        if (windowFeature == 2) {
            this.mDecorToolbar.initProgress();
        } else if (windowFeature == 5) {
            this.mDecorToolbar.initIndeterminateProgress();
        } else if (windowFeature == 9) {
            setOverlayMode(true);
        }
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void setSplitActionBarAlways(boolean bAlwaysSplit) {
        this.mAlwaysSplit = bAlwaysSplit;
        this.mDecorToolbar.setSplitActionBarAlways(bAlwaysSplit);
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void setUiOptions(int uiOptions) {
        boolean splitActionBar = false;
        boolean z = true;
        boolean splitWhenNarrow = (uiOptions & 1) != 0;
        if (splitWhenNarrow) {
            if (!this.mAlwaysSplit) {
                z = getContext().getResources().getBoolean(R.bool.split_action_bar_is_narrow);
            }
            splitActionBar = z;
        }
        if (splitActionBar) {
            pullChildren();
            if (this.mActionBarBottom != null && this.mDecorToolbar.canSplit()) {
                this.mDecorToolbar.setSplitView(this.mActionBarBottom);
                this.mDecorToolbar.setSplitToolbar(splitActionBar);
                this.mDecorToolbar.setSplitWhenNarrow(splitWhenNarrow);
                ActionBarContextView cab = (ActionBarContextView) findViewById(R.id.action_context_bar);
                cab.setSplitView(this.mActionBarBottom);
                cab.setSplitToolbar(splitActionBar);
                cab.setSplitWhenNarrow(splitWhenNarrow);
            } else if (splitActionBar) {
                Log.e(TAG, "Requested split action bar with incompatible window decor! Ignoring request.");
            }
        }
    }

    @Override // com.android.internal.widget.DecorContentParent
    public boolean hasIcon() {
        pullChildren();
        return this.mDecorToolbar.hasIcon();
    }

    @Override // com.android.internal.widget.DecorContentParent
    public boolean hasLogo() {
        pullChildren();
        return this.mDecorToolbar.hasLogo();
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void setIcon(int resId) {
        pullChildren();
        this.mDecorToolbar.setIcon(resId);
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void setIcon(Drawable d) {
        pullChildren();
        this.mDecorToolbar.setIcon(d);
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void setLogo(int resId) {
        pullChildren();
        this.mDecorToolbar.setLogo(resId);
    }

    @Override // com.android.internal.widget.DecorContentParent
    public boolean canShowOverflowMenu() {
        pullChildren();
        return this.mDecorToolbar.canShowOverflowMenu();
    }

    @Override // com.android.internal.widget.DecorContentParent
    public boolean isOverflowMenuShowing() {
        pullChildren();
        return this.mDecorToolbar.isOverflowMenuShowing();
    }

    @Override // com.android.internal.widget.DecorContentParent
    public boolean isOverflowMenuShowPending() {
        pullChildren();
        return this.mDecorToolbar.isOverflowMenuShowPending();
    }

    @Override // com.android.internal.widget.DecorContentParent
    public boolean showOverflowMenu() {
        pullChildren();
        return this.mDecorToolbar.showOverflowMenu();
    }

    @Override // com.android.internal.widget.DecorContentParent
    public boolean hideOverflowMenu() {
        pullChildren();
        return this.mDecorToolbar.hideOverflowMenu();
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void setMenuPrepared() {
        pullChildren();
        this.mDecorToolbar.setMenuPrepared();
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void setMenu(Menu menu, MenuPresenter.Callback cb) {
        pullChildren();
        this.mDecorToolbar.setMenu(menu, cb);
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void saveToolbarHierarchyState(SparseArray<Parcelable> toolbarStates) {
        pullChildren();
        this.mDecorToolbar.saveHierarchyState(toolbarStates);
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void restoreToolbarHierarchyState(SparseArray<Parcelable> toolbarStates) {
        pullChildren();
        this.mDecorToolbar.restoreHierarchyState(toolbarStates);
    }

    @Override // com.android.internal.widget.DecorContentParent
    public void dismissPopups() {
        pullChildren();
        this.mDecorToolbar.dismissPopupMenus();
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }

    public View getContent() {
        return this.mContent;
    }

    public View getActionBarBottom() {
        return this.mActionBarBottom;
    }

    public View getActionBarContainer() {
        return this.mActionBarTop;
    }

    public ActionBarVisibilityCallback getActionBar() {
        return this.mActionBarVisibilityCallback;
    }

    public int getActionBarHeight() {
        return this.mActionBarHeight;
    }

    /* access modifiers changed from: protected */
    public DecorToolbar getDecorToolbar() {
        return this.mDecorToolbar;
    }

    /* access modifiers changed from: protected */
    public int getBottomInset(int bottomInset) {
        return bottomInset;
    }

    public void setHwDrawerFeature(boolean using, int overlayActionBar) {
    }

    public void setDrawerOpend(boolean open) {
    }
}
