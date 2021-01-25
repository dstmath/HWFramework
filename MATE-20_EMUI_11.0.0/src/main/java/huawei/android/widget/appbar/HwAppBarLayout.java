package huawei.android.widget.appbar;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import com.android.hwext.internal.R;
import huawei.android.widget.appbar.HwCoordinatorLayout;
import huawei.android.widget.loader.ResLoader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@HwCoordinatorLayout.DefaultBehavior(Behavior.class)
public class HwAppBarLayout extends LinearLayout {
    private static final int BUILD_VERSION_SDK_ELEVEN = 11;
    private static final int BUILD_VERSION_SDK_NIGHTEEN = 19;
    private static final int DOUBLE_RATE = 2;
    private static final int DURATION = 150;
    private static final int INVALID_APPBAR_HEIGHT = -1;
    private static final int INVALID_SCROLL_RANGE = -1;
    private static final float LIST_APPBAR_OVER_SCROLL_RATIO = 5.0f;
    private static final int LIST_INIT_CAPACITY = 4;
    private static final float LIST_OVER_SCROLL_DP_WHEN_TITLE_SIZE_MAX = 240.0f;
    private static final float LIST_REAL_SCROLL_RATIO = 1.2f;
    private static final int MAX_VALUE = 1000;
    static final int PENDING_ACTION_ANIMATE_ENABLED = 4;
    static final int PENDING_ACTION_COLLAPSED = 2;
    static final int PENDING_ACTION_EXPANDED = 1;
    static final int PENDING_ACTION_EXPANDING = 16;
    static final int PENDING_ACTION_FORCE = 8;
    static final int PENDING_ACTION_NONE = 0;
    private static final float ROUND_UP_DOWN_PLUS = 0.5f;
    private static final int THIRD_RATE = 3;
    private AppBarMoveStateListener mAppBarMoveStateListener;
    private int mDownPreScrollRange;
    private int mDownScrollRange;
    private int mExpandedAppBarDefaultHeight;
    private int mExpandingVisibleHeight;
    private boolean mIsCollapsed;
    private boolean mIsCollapsible;
    private boolean mIsHaveChildWithInterpolator;
    private boolean mIsNestedScrollStarted;
    private boolean mIsOverScrollInterrupted;
    private boolean mIsOverScrolled;
    private WindowInsets mLastInsets;
    private List<OnOffsetChangedListener> mListeners;
    private AppBarOverScrollListener mOnAppBarOverScrollListener;
    private int mPendingAction;
    private int[] mTmpStatesArrays;
    private int mTotalScrollRange;

    public interface AppBarMoveStateListener {
        void notifyAppBarIsMoved(boolean z);
    }

    public interface AppBarOverScrollListener {
        void onAppBarOverScrolled(float f, float f2, boolean z);
    }

    public interface OnOffsetChangedListener {
        void onOffsetChanged(HwAppBarLayout hwAppBarLayout, int i);
    }

    public HwAppBarLayout(Context context) {
        this(context, null);
    }

    public HwAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTotalScrollRange = -1;
        this.mDownPreScrollRange = -1;
        this.mDownScrollRange = -1;
        this.mPendingAction = 0;
        this.mIsOverScrolled = false;
        this.mIsNestedScrollStarted = false;
        this.mIsOverScrollInterrupted = false;
        this.mExpandedAppBarDefaultHeight = -1;
        setOrientation(1);
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray array = theme.obtainStyledAttributes(attrs, R.styleable.HwAppBarLayout, 34668594, 33947771);
            setBackground(array.getDrawable(0));
            setExpanded(array.getBoolean(3, isNeedExpand()), false, true);
            array.recycle();
        }
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            /* class huawei.android.widget.appbar.HwAppBarLayout.AnonymousClass1 */

            @Override // android.view.View.OnApplyWindowInsetsListener
            public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                return HwAppBarLayout.this.onWindowInsetChanged(insets);
            }
        });
    }

    public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
        if (this.mListeners == null) {
            this.mListeners = new ArrayList(4);
        }
        if (listener != null && !this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
        }
    }

    public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
        List<OnOffsetChangedListener> list = this.mListeners;
        if (list != null && listener != null) {
            list.remove(listener);
        }
    }

    public void setOnAppBarOverScrollListener(AppBarOverScrollListener listener) {
        if (listener != null) {
            this.mOnAppBarOverScrollListener = listener;
        }
    }

    public void setAppBarMoveStateListener(AppBarMoveStateListener listener) {
        if (listener != null) {
            this.mAppBarMoveStateListener = listener;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        invalidateScrollRanges();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        invalidateScrollRanges();
        this.mIsHaveChildWithInterpolator = false;
        int childCount = getChildCount();
        int i = 0;
        while (true) {
            if (i >= childCount) {
                break;
            }
            View child = getChildAt(i);
            if ((child.getLayoutParams() instanceof LayoutParams) && ((LayoutParams) child.getLayoutParams()).getScrollInterpolator() != null) {
                this.mIsHaveChildWithInterpolator = true;
                break;
            }
            i++;
        }
        if (this.mExpandedAppBarDefaultHeight == -1) {
            this.mExpandedAppBarDefaultHeight = getHeight();
        }
        updateCollapsible();
    }

    private void updateCollapsible() {
        boolean isHaveCollapsibleChild = false;
        int childCount = getChildCount();
        int i = 0;
        while (true) {
            if (i >= childCount) {
                break;
            }
            ViewGroup.LayoutParams layoutParams = getChildAt(i).getLayoutParams();
            if ((layoutParams instanceof LayoutParams) && ((LayoutParams) layoutParams).isCollapsible()) {
                isHaveCollapsibleChild = true;
                break;
            }
            i++;
        }
        setCollapsibleState(isHaveCollapsibleChild);
    }

    private void invalidateScrollRanges() {
        this.mTotalScrollRange = -1;
        this.mDownPreScrollRange = -1;
        this.mDownScrollRange = -1;
    }

    @Override // android.widget.LinearLayout
    public void setOrientation(int orientation) {
        if (orientation == 1) {
            super.setOrientation(orientation);
            return;
        }
        throw new IllegalArgumentException("AppBarLayout is always vertical and does not support horizontal orientation");
    }

    public void setExpanded(boolean isExpanded) {
        setExpanded(isExpanded, isLaidOut());
    }

    public void setExpanded(boolean isExpanded, boolean isAnimate) {
        if (isOverScrolled()) {
            interruptOverScrollAction();
        }
        setExpanded(isExpanded, isAnimate, true);
    }

    private void setExpanded(boolean isExpanded, boolean isAnimate, boolean isForce) {
        int i = 0;
        int i2 = (isExpanded ? 1 : 2) | (isAnimate ? 4 : 0);
        if (isForce) {
            i = 8;
        }
        this.mPendingAction = i2 | i;
        requestLayout();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2);
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams params) {
        if (Build.VERSION.SDK_INT >= BUILD_VERSION_SDK_NIGHTEEN && (params instanceof LinearLayout.LayoutParams)) {
            return new LayoutParams((LinearLayout.LayoutParams) params);
        }
        if (params instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) params);
        }
        return new LayoutParams(params);
    }

    /* access modifiers changed from: package-private */
    public boolean hasChildWithInterpolator() {
        return this.mIsHaveChildWithInterpolator;
    }

    public final int getTotalScrollRange() {
        int i = this.mTotalScrollRange;
        if (i != -1) {
            return i;
        }
        int range = 0;
        int childCount = getChildCount();
        int i2 = 0;
        while (true) {
            if (i2 >= childCount) {
                break;
            }
            View child = getChildAt(i2);
            if (child.getLayoutParams() instanceof LayoutParams) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                int childHeight = child.getMeasuredHeight();
                int flags = params.mScrollFlags;
                if ((flags & 1) == 0) {
                    break;
                }
                range += params.topMargin + childHeight + params.bottomMargin;
                if ((flags & 2) != 0) {
                    range -= child.getMinimumHeight();
                    break;
                }
            }
            i2++;
        }
        this.mTotalScrollRange = MathUtils.max(0, range - getTopInset());
        return this.mTotalScrollRange;
    }

    /* access modifiers changed from: package-private */
    public boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    /* access modifiers changed from: package-private */
    public int getUpNestedPreScrollRange() {
        return getTotalScrollRange();
    }

    /* access modifiers changed from: package-private */
    public int getDownNestedPreScrollRange() {
        int i = this.mDownPreScrollRange;
        if (i != -1) {
            return i;
        }
        int range = 0;
        for (int i2 = getChildCount() - 1; i2 >= 0; i2--) {
            View child = getChildAt(i2);
            if (child.getLayoutParams() instanceof LayoutParams) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                int childHeight = child.getMeasuredHeight();
                int flags = params.mScrollFlags;
                if ((flags & 5) == 5) {
                    int range2 = range + params.topMargin + params.bottomMargin;
                    if ((flags & 8) != 0) {
                        range = range2 + child.getMinimumHeight();
                    } else if ((flags & 2) != 0) {
                        range = range2 + (childHeight - child.getMinimumHeight());
                    } else {
                        range = range2 + (childHeight - getTopInset());
                    }
                } else if (range > 0) {
                    break;
                }
            }
        }
        this.mDownPreScrollRange = MathUtils.max(0, range);
        return this.mDownPreScrollRange;
    }

    public int getDownNestedScrollRange() {
        int i = this.mDownScrollRange;
        if (i != -1) {
            return i;
        }
        int range = 0;
        int childCount = getChildCount();
        int i2 = 0;
        while (true) {
            if (i2 >= childCount) {
                break;
            }
            View child = getChildAt(i2);
            if (child.getLayoutParams() instanceof LayoutParams) {
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                int childHeight = child.getMeasuredHeight() + params.topMargin + params.bottomMargin;
                int flags = params.mScrollFlags;
                if ((flags & 1) == 0) {
                    break;
                }
                range += childHeight;
                if ((flags & 2) != 0) {
                    range -= child.getMinimumHeight() + getTopInset();
                    break;
                }
            }
            i2++;
        }
        this.mDownScrollRange = MathUtils.max(0, range);
        return this.mDownScrollRange;
    }

    /* access modifiers changed from: package-private */
    public void dispatchOffsetUpdates(int offset) {
        List<OnOffsetChangedListener> list = this.mListeners;
        if (list != null) {
            for (OnOffsetChangedListener listener : list) {
                if (listener != null) {
                    listener.onOffsetChanged(this, offset);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyAppBarOverScroll(float overScrollFraction, float appBarOverScrollY, boolean hasAppBarHeightChanged) {
        AppBarOverScrollListener appBarOverScrollListener = this.mOnAppBarOverScrollListener;
        if (appBarOverScrollListener != null) {
            appBarOverScrollListener.onAppBarOverScrolled(overScrollFraction, appBarOverScrollY, hasAppBarHeightChanged);
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyAppBarIsMoved(boolean isAppBarMoved) {
        AppBarMoveStateListener appBarMoveStateListener = this.mAppBarMoveStateListener;
        if (appBarMoveStateListener != null) {
            appBarMoveStateListener.notifyAppBarIsMoved(isAppBarMoved);
        }
    }

    /* access modifiers changed from: package-private */
    public final int getMinimumHeightForVisibleOverlappingContent() {
        int topInset = getTopInset();
        int minHeight = getMinimumHeight();
        if (minHeight != 0) {
            return (minHeight * 2) + topInset;
        }
        int childCount = getChildCount();
        int lastChildMinHeight = childCount >= 1 ? getChildAt(childCount - 1).getMinimumHeight() : 0;
        if (lastChildMinHeight != 0) {
            return (lastChildMinHeight * 2) + topInset;
        }
        return getHeight() / 3;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public int[] onCreateDrawableState(int extraSpace) {
        if (this.mTmpStatesArrays == null) {
            this.mTmpStatesArrays = new int[2];
        }
        int[] extraStates = this.mTmpStatesArrays;
        int[] states = super.onCreateDrawableState(extraStates.length + extraSpace);
        int collapsible = ResLoader.getInstance().getIdentifier(getContext(), "attr", "state_collapsible");
        int collapsed = ResLoader.getInstance().getIdentifier(getContext(), "attr", "state_collapsed");
        extraStates[0] = this.mIsCollapsible ? collapsible : -collapsible;
        extraStates[1] = (!this.mIsCollapsible || !this.mIsCollapsed) ? -collapsed : collapsed;
        return mergeDrawableStates(states, extraStates);
    }

    private boolean setCollapsibleState(boolean isCollapsible) {
        if (this.mIsCollapsible == isCollapsible) {
            return false;
        }
        this.mIsCollapsible = isCollapsible;
        refreshDrawableState();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setCollapsedState(boolean isCollapsed) {
        if (this.mIsCollapsed == isCollapsed) {
            return false;
        }
        this.mIsCollapsed = isCollapsed;
        refreshDrawableState();
        return true;
    }

    /* access modifiers changed from: package-private */
    public int getPendingAction() {
        return this.mPendingAction;
    }

    /* access modifiers changed from: package-private */
    public void resetPendingAction() {
        this.mPendingAction = 0;
    }

    /* access modifiers changed from: package-private */
    public final int getTopInset() {
        WindowInsets windowInsets = this.mLastInsets;
        if (windowInsets != null) {
            return windowInsets.getSystemWindowInsetTop();
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public WindowInsets onWindowInsetChanged(WindowInsets insets) {
        WindowInsets newInsets = null;
        if (getFitsSystemWindows()) {
            newInsets = insets;
        }
        if (!Objects.equals(this.mLastInsets, newInsets)) {
            this.mLastInsets = newInsets;
            invalidateScrollRanges();
        }
        return insets;
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {
        static final int COLLAPSIBLE_FLAGS = 10;
        static final int FLAG_QUICK_RETURN = 5;
        static final int FLAG_SNAP = 17;
        public static final int SCROLL_FLAG_ENTER_ALWAYS = 4;
        public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 8;
        public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 2;
        public static final int SCROLL_FLAG_SCROLL = 1;
        public static final int SCROLL_FLAG_SNAP = 16;
        int mScrollFlags = 1;
        Interpolator mScrollInterpolator;

        @Retention(RetentionPolicy.SOURCE)
        public @interface ScrollFlags {
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HwAppBarLayout_Layout);
            this.mScrollFlags = array.getInt(0, 0);
            if (array.hasValue(1)) {
                this.mScrollInterpolator = AnimationUtils.loadInterpolator(context, array.getResourceId(1, 0));
            }
            array.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
        }

        public LayoutParams(ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LinearLayout.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super((LinearLayout.LayoutParams) source);
            this.mScrollFlags = source.mScrollFlags;
            this.mScrollInterpolator = source.mScrollInterpolator;
        }

        public void setScrollFlags(int flags) {
            this.mScrollFlags = flags;
        }

        public int getScrollFlags() {
            return this.mScrollFlags;
        }

        public void setScrollInterpolator(Interpolator interpolator) {
            this.mScrollInterpolator = interpolator;
        }

        public Interpolator getScrollInterpolator() {
            return this.mScrollInterpolator;
        }

        /* access modifiers changed from: package-private */
        public boolean isCollapsible() {
            int i = this.mScrollFlags;
            return (i & 1) == 1 && (i & COLLAPSIBLE_FLAGS) != 0;
        }
    }

    public static class Behavior extends HeaderBehavior<HwAppBarLayout> {
        private static final int INVALID_POSITION = -1;
        private static final int MAX_OFFSET_ANIMATION_DURATION = 600;
        private boolean mIsOffsetToChildIndexOnLayoutIsMinHeight;
        private WeakReference<View> mLastNestedScrollingChildRef;
        private ValueAnimator mOffsetAnimator;
        private int mOffsetDelta;
        private int mOffsetToChildIndexOnLayout = -1;
        private float mOffsetToChildIndexOnLayoutPer;
        private DragCallback mOnDragCallback;

        public static abstract class DragCallback {
            public abstract boolean canDrag(HwAppBarLayout hwAppBarLayout);
        }

        @Override // huawei.android.widget.appbar.ViewOffsetBehavior
        public /* bridge */ /* synthetic */ int getLeftAndRightOffset() {
            return super.getLeftAndRightOffset();
        }

        @Override // huawei.android.widget.appbar.ViewOffsetBehavior
        public /* bridge */ /* synthetic */ int getTopAndBottomOffset() {
            return super.getTopAndBottomOffset();
        }

        @Override // huawei.android.widget.appbar.HeaderBehavior
        public /* bridge */ /* synthetic */ void postOnAnimation(View view, Runnable runnable) {
            super.postOnAnimation(view, runnable);
        }

        @Override // huawei.android.widget.appbar.ViewOffsetBehavior
        public /* bridge */ /* synthetic */ boolean setLeftAndRightOffset(int i) {
            return super.setLeftAndRightOffset(i);
        }

        @Override // huawei.android.widget.appbar.ViewOffsetBehavior
        public /* bridge */ /* synthetic */ boolean setTopAndBottomOffset(int i) {
            return super.setTopAndBottomOffset(i);
        }

        public Behavior() {
        }

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public boolean onStartNestedScroll(HwCoordinatorLayout parent, HwAppBarLayout child, View directTargetChild, View target, int nestedScrollAxes, int type) {
            ValueAnimator valueAnimator;
            boolean isStarted = false;
            if (child.isOverScrolled()) {
                return false;
            }
            if ((nestedScrollAxes & 2) != 0 && child.hasScrollableChildren() && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight()) {
                isStarted = true;
            }
            if (isStarted && (valueAnimator = this.mOffsetAnimator) != null) {
                valueAnimator.cancel();
            }
            this.mLastNestedScrollingChildRef = null;
            boolean isNestedScrollStarted = isStarted;
            if (child.getCurrentVisibleHeight() == child.mExpandedAppBarDefaultHeight) {
                isNestedScrollStarted = false;
            }
            child.setIsNestedScrollStarted(isNestedScrollStarted);
            child.notifyAppBarIsMoved(isStarted);
            return isStarted;
        }

        public void onNestedPreScroll(HwCoordinatorLayout coordinatorLayout, HwAppBarLayout child, View target, int deltaX, int deltaY, int[] consumeArrays, int type) {
            int max;
            int min;
            if (deltaY != 0) {
                if (deltaY < 0) {
                    int min2 = -child.getTotalScrollRange();
                    min = min2;
                    max = child.getDownNestedPreScrollRange() + min2;
                } else {
                    min = -child.getUpNestedPreScrollRange();
                    max = 0;
                }
                if (min != max) {
                    child.notifyAppBarIsMoved(true);
                    consumeArrays[1] = scroll(coordinatorLayout, child, deltaY, min, max);
                }
                if (child.getCurrentVisibleHeight() == child.mExpandedAppBarDefaultHeight) {
                    child.setIsNestedScrollStarted(false);
                }
            }
        }

        public void onNestedScroll(HwCoordinatorLayout coordinatorLayout, HwAppBarLayout child, View target, int consumeDeltaX, int consumeDeltaY, int unconsumedDeltaX, int unconsumedDeltaY, int type) {
            if (unconsumedDeltaY < 0) {
                child.notifyAppBarIsMoved(true);
                scroll(coordinatorLayout, child, unconsumedDeltaY, -child.getDownNestedScrollRange(), 0);
                if (child.getCurrentVisibleHeight() == child.mExpandedAppBarDefaultHeight) {
                    child.setIsNestedScrollStarted(false);
                }
            }
        }

        public void onStopNestedScroll(HwCoordinatorLayout coordinatorLayout, HwAppBarLayout layout, View target, int type) {
            if (type == 0) {
                snapToChildIfNeeded(coordinatorLayout, layout);
            }
            this.mLastNestedScrollingChildRef = new WeakReference<>(target);
            layout.setIsNestedScrollStarted(false);
            layout.notifyAppBarIsMoved(false);
        }

        public void setDragCallback(DragCallback callback) {
            this.mOnDragCallback = callback;
        }

        private void animateOffsetTo(HwCoordinatorLayout coordinatorLayout, HwAppBarLayout child, int offset, float velocity) {
            int duration;
            int distance = Math.abs(getTopBottomOffsetForScrollingSibling() - offset);
            float realVelocity = Math.abs(velocity);
            if (realVelocity > 0.0f) {
                duration = Math.round((((float) distance) / realVelocity) * 1000.0f) * 3;
            } else {
                duration = (int) ((1.0f + (((float) distance) / ((float) child.getHeight()))) * 150.0f);
            }
            animateOffsetWithDuration(coordinatorLayout, child, offset, duration);
        }

        private void animateOffsetWithDuration(final HwCoordinatorLayout coordinatorLayout, final HwAppBarLayout child, int offset, int duration) {
            int currentOffset = getTopBottomOffsetForScrollingSibling();
            if (currentOffset == offset) {
                ValueAnimator valueAnimator = this.mOffsetAnimator;
                if (valueAnimator != null && valueAnimator.isRunning()) {
                    this.mOffsetAnimator.cancel();
                    return;
                }
                return;
            }
            ValueAnimator valueAnimator2 = this.mOffsetAnimator;
            if (valueAnimator2 == null) {
                this.mOffsetAnimator = new ValueAnimator();
                this.mOffsetAnimator.setInterpolator(huawei.android.widget.AnimationUtils.DECELERATE_INTERPOLATOR);
                this.mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    /* class huawei.android.widget.appbar.HwAppBarLayout.Behavior.AnonymousClass1 */

                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Behavior.this.setHeaderTopBottomOffset(coordinatorLayout, child, ((Integer) animation.getAnimatedValue()).intValue());
                    }
                });
            } else {
                valueAnimator2.cancel();
            }
            this.mOffsetAnimator.setDuration((long) MathUtils.min(duration, MAX_OFFSET_ANIMATION_DURATION));
            this.mOffsetAnimator.setIntValues(currentOffset, offset);
            this.mOffsetAnimator.start();
        }

        private int getChildIndexOnOffset(HwAppBarLayout layout, int offset) {
            int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = layout.getChildAt(i);
                if (child.getTop() <= (-offset) && child.getBottom() >= (-offset)) {
                    return i;
                }
            }
            return -1;
        }

        private void snapToChildIfNeeded(HwCoordinatorLayout coordinatorLayout, HwAppBarLayout layout) {
            int offset = getTopBottomOffsetForScrollingSibling();
            int offsetChildIndex = getChildIndexOnOffset(layout, offset);
            if (offsetChildIndex >= 0) {
                View offsetChild = layout.getChildAt(offsetChildIndex);
                if (offsetChild.getLayoutParams() instanceof LayoutParams) {
                    int flags = ((LayoutParams) offsetChild.getLayoutParams()).getScrollFlags();
                    if ((flags & 17) == 17) {
                        int snapTop = -offsetChild.getTop();
                        int snapBottom = -offsetChild.getBottom();
                        if (offsetChildIndex == layout.getChildCount() - 1) {
                            snapBottom += layout.getTopInset();
                        }
                        if (checkFlag(flags, 2)) {
                            snapBottom += offsetChild.getMinimumHeight();
                        } else if (checkFlag(flags, 5)) {
                            int seam = offsetChild.getMinimumHeight() + snapBottom;
                            snapTop = offset < seam ? seam : snapTop;
                            snapBottom = offset < seam ? snapBottom : seam;
                        }
                        animateOffsetTo(coordinatorLayout, layout, MathUtils.clamp(offset < (snapBottom + snapTop) / 2 ? snapBottom : snapTop, -layout.getTotalScrollRange(), 0), 0.0f);
                    }
                }
            }
        }

        private static boolean checkFlag(int flags, int check) {
            return (flags & check) == check;
        }

        public boolean onMeasureChild(HwCoordinatorLayout parent, HwAppBarLayout child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
            if (!(child.getLayoutParams() instanceof HwCoordinatorLayout.LayoutParams)) {
                return super.onMeasureChild(parent, (HwCoordinatorLayout) child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
            }
            if (((HwCoordinatorLayout.LayoutParams) child.getLayoutParams()).height != -2) {
                return super.onMeasureChild(parent, (HwCoordinatorLayout) child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
            }
            parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, View.MeasureSpec.makeMeasureSpec(0, 0), heightUsed);
            return true;
        }

        public boolean onLayoutChild(HwCoordinatorLayout parent, HwAppBarLayout appBarLayout, int layoutDirection) {
            int offset;
            boolean isHandled = super.onLayoutChild(parent, (HwCoordinatorLayout) appBarLayout, layoutDirection);
            int pendingAction = appBarLayout.getPendingAction();
            int i = this.mOffsetToChildIndexOnLayout;
            if (i >= 0 && (pendingAction & 8) == 0) {
                View child = appBarLayout.getChildAt(i);
                if (child != null) {
                    int offset2 = -child.getBottom();
                    if (this.mIsOffsetToChildIndexOnLayoutIsMinHeight) {
                        offset = offset2 + child.getMinimumHeight() + appBarLayout.getTopInset();
                    } else {
                        offset = offset2 + Math.round(((float) child.getHeight()) * this.mOffsetToChildIndexOnLayoutPer);
                    }
                    setHeaderTopBottomOffset(parent, appBarLayout, offset);
                }
            } else if (pendingAction != 0) {
                onLayoutChild(pendingAction, parent, appBarLayout);
            }
            appBarLayout.resetPendingAction();
            this.mOffsetToChildIndexOnLayout = -1;
            setTopAndBottomOffset(MathUtils.clamp(getTopAndBottomOffset(), -appBarLayout.getTotalScrollRange(), 0));
            updateAppBarLayoutDrawableState(parent, appBarLayout, getTopAndBottomOffset(), 0, true);
            appBarLayout.dispatchOffsetUpdates(getTopAndBottomOffset());
            return isHandled;
        }

        private void onLayoutChild(int pendingAction, HwCoordinatorLayout parent, HwAppBarLayout appBarLayout) {
            boolean isAnimate = (pendingAction & 4) != 0;
            if ((pendingAction & 2) != 0) {
                int offset = -appBarLayout.getUpNestedPreScrollRange();
                if (isAnimate) {
                    animateOffsetTo(parent, appBarLayout, offset, 0.0f);
                } else {
                    setHeaderTopBottomOffset(parent, appBarLayout, offset);
                }
            } else if ((pendingAction & 1) != 0) {
                if (isAnimate) {
                    animateOffsetTo(parent, appBarLayout, 0, 0.0f);
                } else {
                    setHeaderTopBottomOffset(parent, appBarLayout, 0);
                }
            } else if ((pendingAction & 16) != 0) {
                int expandingOffset = appBarLayout.getRealVisibleHeight() - appBarLayout.getHeight();
                if (isAnimate) {
                    animateOffsetTo(parent, appBarLayout, expandingOffset, 0.0f);
                } else {
                    setHeaderTopBottomOffset(parent, appBarLayout, expandingOffset);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean canDragView(HwAppBarLayout view) {
            DragCallback dragCallback = this.mOnDragCallback;
            if (dragCallback != null) {
                return dragCallback.canDrag(view);
            }
            WeakReference<View> weakReference = this.mLastNestedScrollingChildRef;
            if (weakReference == null) {
                return true;
            }
            View scrollingView = weakReference.get();
            if (scrollingView == null || !scrollingView.isShown() || scrollingView.canScrollVertically(-1)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public void onFlingFinished(HwCoordinatorLayout parent, HwAppBarLayout layout) {
            snapToChildIfNeeded(parent, layout);
        }

        /* access modifiers changed from: package-private */
        public int getMaxDragOffset(HwAppBarLayout view) {
            return -view.getDownNestedScrollRange();
        }

        /* access modifiers changed from: package-private */
        public int getScrollRangeForDragFling(HwAppBarLayout view) {
            return view.getTotalScrollRange();
        }

        /* access modifiers changed from: package-private */
        public int setHeaderTopBottomOffset(HwCoordinatorLayout coordinatorLayout, HwAppBarLayout appBarLayout, int newOffset, int minOffset, int maxOffset) {
            int curOffset = getTopBottomOffsetForScrollingSibling();
            if (minOffset != 0 && curOffset >= minOffset) {
                if (curOffset <= maxOffset) {
                    int realNewOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);
                    if (curOffset == realNewOffset) {
                        return 0;
                    }
                    int interpolatedOffset = appBarLayout.hasChildWithInterpolator() ? interpolateOffset(appBarLayout, realNewOffset) : realNewOffset;
                    boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);
                    int consumed = curOffset - realNewOffset;
                    this.mOffsetDelta = realNewOffset - interpolatedOffset;
                    if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
                        coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
                    }
                    appBarLayout.dispatchOffsetUpdates(getTopAndBottomOffset());
                    updateAppBarLayoutDrawableState(coordinatorLayout, appBarLayout, realNewOffset, newOffset < curOffset ? -1 : 1, false);
                    return consumed;
                }
            }
            this.mOffsetDelta = 0;
            return 0;
        }

        /* access modifiers changed from: package-private */
        public boolean isOffsetAnimatorRunning() {
            ValueAnimator valueAnimator = this.mOffsetAnimator;
            return valueAnimator != null && valueAnimator.isRunning();
        }

        private int interpolateOffset(HwAppBarLayout layout, int offset) {
            int absOffset = Math.abs(offset);
            int childCount = layout.getChildCount();
            int i = 0;
            while (true) {
                if (i >= childCount) {
                    break;
                }
                View child = layout.getChildAt(i);
                if (child.getLayoutParams() instanceof LayoutParams) {
                    LayoutParams params = (LayoutParams) child.getLayoutParams();
                    Interpolator interpolator = params.getScrollInterpolator();
                    if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
                        if (interpolator != null) {
                            int childScrollableHeight = 0;
                            int flags = params.getScrollFlags();
                            if ((flags & 1) != 0) {
                                childScrollableHeight = (0 + ((child.getHeight() + params.topMargin) + params.bottomMargin)) - ((flags & 2) != 0 ? child.getMinimumHeight() : 0);
                            }
                            if (child.getFitsSystemWindows()) {
                                childScrollableHeight -= layout.getTopInset();
                            }
                            if (childScrollableHeight > 0) {
                                return Integer.signum(offset) * (child.getTop() + Math.round(((float) childScrollableHeight) * interpolator.getInterpolation(((float) (absOffset - child.getTop())) / ((float) childScrollableHeight))));
                            }
                        }
                    }
                }
                i++;
            }
            return offset;
        }

        private void updateAppBarLayoutDrawableState(HwCoordinatorLayout parent, HwAppBarLayout layout, int offset, int direction, boolean isForceJump) {
            View child = getAppBarChildOnOffset(layout, offset);
            if (child != null && (child.getLayoutParams() instanceof LayoutParams)) {
                int flags = ((LayoutParams) child.getLayoutParams()).getScrollFlags();
                boolean isCollapsed = false;
                if ((flags & 1) != 0) {
                    int minHeight = child.getMinimumHeight();
                    boolean z = false;
                    if (direction > 0 && (flags & 12) != 0) {
                        if ((-offset) >= (child.getBottom() - minHeight) - layout.getTopInset()) {
                            z = true;
                        }
                        isCollapsed = z;
                    } else if ((flags & 2) != 0) {
                        if ((-offset) >= (child.getBottom() - minHeight) - layout.getTopInset()) {
                            z = true;
                        }
                        isCollapsed = z;
                    }
                }
                boolean isChanged = layout.setCollapsedState(isCollapsed);
                if (Build.VERSION.SDK_INT < HwAppBarLayout.BUILD_VERSION_SDK_ELEVEN) {
                    return;
                }
                if (isForceJump || (isChanged && shouldJumpElevationState(parent, layout))) {
                    layout.jumpDrawablesToCurrentState();
                }
            }
        }

        private boolean shouldJumpElevationState(HwCoordinatorLayout parent, HwAppBarLayout layout) {
            for (View dependency : parent.getDependents(layout)) {
                HwCoordinatorLayout.Behavior behavior = ((HwCoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
                if (behavior instanceof ScrollingViewBehavior) {
                    if (((ScrollingViewBehavior) behavior).getOverlayTop() != 0) {
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }

        private static View getAppBarChildOnOffset(HwAppBarLayout layout, int offset) {
            int absOffset = Math.abs(offset);
            int childCount = layout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = layout.getChildAt(i);
                if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
                    return child;
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        @Override // huawei.android.widget.appbar.HeaderBehavior
        public int getTopBottomOffsetForScrollingSibling() {
            return getTopAndBottomOffset() + this.mOffsetDelta;
        }

        public Parcelable onSaveInstanceState(HwCoordinatorLayout parent, HwAppBarLayout appBarLayout) {
            Parcelable superState = super.onSaveInstanceState(parent, (HwCoordinatorLayout) appBarLayout);
            int offset = getTopAndBottomOffset();
            int childCount = appBarLayout.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = appBarLayout.getChildAt(i);
                int visBottom = child.getBottom() + offset;
                if (child.getTop() + offset <= 0 && visBottom >= 0) {
                    SavedState savedState = new SavedState(superState);
                    savedState.mFirstVisibleChildIndex = i;
                    savedState.mIsFirstVisibleChildAtMinimumHeight = visBottom == child.getMinimumHeight() + appBarLayout.getTopInset();
                    savedState.mFirstVisibleChildPercentageShown = ((float) visBottom) / ((float) child.getHeight());
                    return savedState;
                }
            }
            return superState;
        }

        public void onRestoreInstanceState(HwCoordinatorLayout parent, HwAppBarLayout appBarLayout, Parcelable state) {
            if (state instanceof SavedState) {
                SavedState savedState = (SavedState) state;
                super.onRestoreInstanceState(parent, (HwCoordinatorLayout) appBarLayout, savedState.getSuperState());
                this.mOffsetToChildIndexOnLayout = savedState.mFirstVisibleChildIndex;
                this.mOffsetToChildIndexOnLayoutPer = savedState.mFirstVisibleChildPercentageShown;
                this.mIsOffsetToChildIndexOnLayoutIsMinHeight = savedState.mIsFirstVisibleChildAtMinimumHeight;
                return;
            }
            super.onRestoreInstanceState(parent, (HwCoordinatorLayout) appBarLayout, state);
            this.mOffsetToChildIndexOnLayout = -1;
        }

        /* access modifiers changed from: protected */
        public static class SavedState extends AbsSavedState {
            public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>() {
                /* class huawei.android.widget.appbar.HwAppBarLayout.Behavior.SavedState.AnonymousClass1 */

                @Override // android.os.Parcelable.ClassLoaderCreator
                public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                    return new SavedState(source, loader);
                }

                @Override // android.os.Parcelable.Creator
                public SavedState createFromParcel(Parcel source) {
                    return new SavedState(source, null);
                }

                @Override // android.os.Parcelable.Creator
                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
            int mFirstVisibleChildIndex;
            float mFirstVisibleChildPercentageShown;
            boolean mIsFirstVisibleChildAtMinimumHeight;

            public SavedState(Parcel source, ClassLoader loader) {
                super(source, loader);
                this.mFirstVisibleChildIndex = source.readInt();
                this.mFirstVisibleChildPercentageShown = source.readFloat();
                this.mIsFirstVisibleChildAtMinimumHeight = source.readByte() != 0;
            }

            public SavedState(Parcelable superState) {
                super(superState);
            }

            @Override // android.view.AbsSavedState, android.os.Parcelable
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(this.mFirstVisibleChildIndex);
                dest.writeFloat(this.mFirstVisibleChildPercentageShown);
                dest.writeByte(this.mIsFirstVisibleChildAtMinimumHeight ? (byte) 1 : 0);
            }
        }
    }

    public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {
        @Override // huawei.android.widget.appbar.ViewOffsetBehavior
        public /* bridge */ /* synthetic */ int getLeftAndRightOffset() {
            return super.getLeftAndRightOffset();
        }

        @Override // huawei.android.widget.appbar.ViewOffsetBehavior
        public /* bridge */ /* synthetic */ int getTopAndBottomOffset() {
            return super.getTopAndBottomOffset();
        }

        @Override // huawei.android.widget.appbar.ViewOffsetBehavior, huawei.android.widget.appbar.HwCoordinatorLayout.Behavior
        public /* bridge */ /* synthetic */ boolean onLayoutChild(HwCoordinatorLayout hwCoordinatorLayout, View view, int i) {
            return super.onLayoutChild(hwCoordinatorLayout, view, i);
        }

        @Override // huawei.android.widget.appbar.HeaderScrollingViewBehavior, huawei.android.widget.appbar.HwCoordinatorLayout.Behavior
        public /* bridge */ /* synthetic */ boolean onMeasureChild(HwCoordinatorLayout hwCoordinatorLayout, View view, int i, int i2, int i3, int i4) {
            return super.onMeasureChild(hwCoordinatorLayout, view, i, i2, i3, i4);
        }

        @Override // huawei.android.widget.appbar.ViewOffsetBehavior
        public /* bridge */ /* synthetic */ boolean setLeftAndRightOffset(int i) {
            return super.setLeftAndRightOffset(i);
        }

        @Override // huawei.android.widget.appbar.ViewOffsetBehavior
        public /* bridge */ /* synthetic */ boolean setTopAndBottomOffset(int i) {
            return super.setTopAndBottomOffset(i);
        }

        public ScrollingViewBehavior() {
        }

        public ScrollingViewBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScrollingViewBehavior_Layout);
            setOverlayTop(array.getDimensionPixelSize(0, 0));
            array.recycle();
        }

        @Override // huawei.android.widget.appbar.HwCoordinatorLayout.Behavior
        public boolean layoutDependsOn(HwCoordinatorLayout parent, View child, View dependency) {
            return dependency instanceof HwAppBarLayout;
        }

        @Override // huawei.android.widget.appbar.HwCoordinatorLayout.Behavior
        public boolean onDependentViewChanged(HwCoordinatorLayout parent, View child, View dependency) {
            offsetChildAsNeeded(parent, child, dependency);
            return false;
        }

        @Override // huawei.android.widget.appbar.HwCoordinatorLayout.Behavior
        public boolean onRequestChildRectangleOnScreen(HwCoordinatorLayout parent, View child, Rect rectangle, boolean isImmediate) {
            HwAppBarLayout header = findFirstDependency(parent.getDependencies(child));
            if (header == null) {
                return false;
            }
            rectangle.offset(child.getLeft(), child.getTop());
            Rect parentRect = this.mTempRect1;
            parentRect.set(0, 0, parent.getWidth(), parent.getHeight());
            if (parentRect.contains(rectangle)) {
                return false;
            }
            header.setExpanded(false, !isImmediate);
            return true;
        }

        private void offsetChildAsNeeded(HwCoordinatorLayout parent, View child, View dependency) {
            HwCoordinatorLayout.Behavior behavior = ((HwCoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
            if ((behavior instanceof Behavior) && !((HwAppBarLayout) dependency).isOverScrolled()) {
                ViewOffsetHelper.offsetTopAndBottom(child, (((dependency.getBottom() - child.getTop()) + ((Behavior) behavior).mOffsetDelta) + getVerticalLayoutGap()) - getOverlapPixelsForOffset(dependency));
            }
        }

        /* access modifiers changed from: package-private */
        @Override // huawei.android.widget.appbar.HeaderScrollingViewBehavior
        public float getOverlapRatioForOffset(View header) {
            int availScrollRange;
            if (!(header instanceof HwAppBarLayout)) {
                return 0.0f;
            }
            HwAppBarLayout appBarLayout = (HwAppBarLayout) header;
            int totalScrollRange = appBarLayout.getTotalScrollRange();
            int preScrollDown = appBarLayout.getDownNestedPreScrollRange();
            int offset = getAppBarLayoutOffset(appBarLayout);
            if ((preScrollDown == 0 || totalScrollRange + offset > preScrollDown) && (availScrollRange = totalScrollRange - preScrollDown) != 0) {
                return (((float) offset) / ((float) availScrollRange)) + 1.0f;
            }
            return 0.0f;
        }

        private static int getAppBarLayoutOffset(HwAppBarLayout appBarLayout) {
            HwCoordinatorLayout.Behavior behavior = ((HwCoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).getBehavior();
            if (behavior instanceof Behavior) {
                return ((Behavior) behavior).getTopBottomOffsetForScrollingSibling();
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        @Override // huawei.android.widget.appbar.HeaderScrollingViewBehavior
        public HwAppBarLayout findFirstDependency(List<View> views) {
            for (View view : views) {
                if (view instanceof HwAppBarLayout) {
                    return (HwAppBarLayout) view;
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        @Override // huawei.android.widget.appbar.HeaderScrollingViewBehavior
        public int getScrollRange(View view) {
            if (view instanceof HwAppBarLayout) {
                return ((HwAppBarLayout) view).getTotalScrollRange();
            }
            return super.getScrollRange(view);
        }
    }

    public void setExpandedLayoutY(int offsetY) {
        ViewParent viewParent;
        HwCoordinatorLayout.Behavior behavior;
        if (this.mIsCollapsible && (viewParent = getParent()) != null && (viewParent instanceof HwCoordinatorLayout)) {
            int childCount = ((HwCoordinatorLayout) viewParent).getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = ((HwCoordinatorLayout) viewParent).getChildAt(i);
                if ((child.getLayoutParams() instanceof HwCoordinatorLayout.LayoutParams) && (behavior = ((HwCoordinatorLayout.LayoutParams) child.getLayoutParams()).getBehavior()) != null && (behavior instanceof HeaderBehavior)) {
                    ((HeaderBehavior) behavior).scroll((HwCoordinatorLayout) viewParent, child, offsetY, ((HeaderBehavior) behavior).getMaxDragOffset(child), 0);
                }
            }
        }
    }

    public boolean isCollapsible() {
        return this.mIsCollapsible;
    }

    public boolean isCollapsed() {
        return this.mIsCollapsed;
    }

    public boolean isOverScrolled() {
        return this.mIsOverScrolled;
    }

    public void setIsOverScrolled(boolean isOverScrolled) {
        this.mIsOverScrolled = isOverScrolled;
    }

    public boolean isOverScrollInterrupted() {
        return this.mIsOverScrollInterrupted;
    }

    public void setIsOverScrollInterrupted(boolean isOverScrollInterrupted) {
        this.mIsOverScrollInterrupted = isOverScrollInterrupted;
    }

    public boolean isNestedScrollStarted() {
        return this.mIsNestedScrollStarted;
    }

    public void setIsNestedScrollStarted(boolean isNestedScrollStarted) {
        this.mIsNestedScrollStarted = isNestedScrollStarted;
    }

    public void notifyContentOverScroll(int overScrollY) {
        int i;
        if (isOverScrollInterrupted() && overScrollY == 0) {
            setIsOverScrollInterrupted(false);
        }
        if (isOverScrollInterrupted() || overScrollY < 0 || getCurrentVisibleHeight() < this.mExpandedAppBarDefaultHeight || isNestedScrollStarted()) {
            return;
        }
        if (isOverScrolled() || overScrollY != 0) {
            setIsOverScrolled(overScrollY != 0);
            ViewGroup.LayoutParams params = getLayoutParams();
            float appBarScrollY = ((float) overScrollY) / LIST_APPBAR_OVER_SCROLL_RATIO;
            int oldHeight = params.height;
            if (overScrollY == 0) {
                i = this.mExpandedAppBarDefaultHeight;
            } else {
                i = this.mExpandedAppBarDefaultHeight + ((int) Math.rint((double) appBarScrollY));
            }
            params.height = i;
            float fraction = (((float) overScrollY) * LIST_REAL_SCROLL_RATIO) / ((float) dip2px(LIST_OVER_SCROLL_DP_WHEN_TITLE_SIZE_MAX));
            if (oldHeight != params.height) {
                notifyAppBarOverScroll(fraction, appBarScrollY, true);
                setLayoutParams(params);
                return;
            }
            notifyAppBarOverScroll(fraction, appBarScrollY, false);
        }
    }

    private void resetAppBarDefaultHeight() {
        int i;
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null && (i = this.mExpandedAppBarDefaultHeight) != -1) {
            params.height = i;
            setLayoutParams(params);
        }
    }

    private void interruptOverScrollAction() {
        resetAppBarDefaultHeight();
        setIsOverScrolled(false);
        setIsOverScrollInterrupted(true);
    }

    private int dip2px(float dpValue) {
        return (int) ((dpValue * getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void setDownScrollRange(int downScrollRange) {
        this.mDownScrollRange = downScrollRange;
    }

    /* access modifiers changed from: package-private */
    public int getCurrentVisibleHeight() {
        HwCoordinatorLayout.Behavior behavior;
        int currentVisibleHeight = getHeight();
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (!(layoutParams instanceof HwCoordinatorLayout.LayoutParams) || (behavior = ((HwCoordinatorLayout.LayoutParams) layoutParams).getBehavior()) == null || !(behavior instanceof HeaderBehavior)) {
            return currentVisibleHeight;
        }
        return getHeight() + ((HeaderBehavior) behavior).getTopAndBottomOffset();
    }

    /* access modifiers changed from: package-private */
    public void expand(int visibleHeight, boolean isHasAnimate) {
        this.mExpandingVisibleHeight = visibleHeight;
        this.mPendingAction = (isHasAnimate ? 4 : 0) | 8 | 16;
        requestLayout();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getRealVisibleHeight() {
        int realVisibleHeight = this.mExpandingVisibleHeight;
        int minVisibleHeight = getHeight() - getTotalScrollRange();
        if (realVisibleHeight < minVisibleHeight) {
            return minVisibleHeight;
        }
        if (realVisibleHeight > getHeight()) {
            return getHeight();
        }
        return realVisibleHeight;
    }

    private boolean isNeedExpand() {
        return this.mContext.getResources().getBoolean(34537472) || (this.mContext.getResources().getConfiguration().orientation == 1);
    }

    public void setInnerAlpha(float alpha) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child != null) {
                child.setAlpha(alpha);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isCollapsed()) {
            this.mPendingAction = 10;
        }
    }
}
