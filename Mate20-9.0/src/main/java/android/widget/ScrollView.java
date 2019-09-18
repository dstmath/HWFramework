package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hwcontrol.HwWidgetFactory;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import com.android.internal.R;
import java.util.List;

public class ScrollView extends FrameLayout {
    static final int ANIMATED_SCROLL_GAP = 250;
    private static final float DEFAULT_MULTIPLE_FLING_LENGTH_THRESHOLD = 125.0f;
    private static final int INVALID_POINTER = -1;
    private static final String LOG_TAG = "OverScrollerOptimization";
    static final float MAX_SCROLL_FACTOR = 0.5f;
    private static final boolean SMART_SLIDE_PROPERTIES = SystemProperties.getBoolean("uifirst_listview_optimization_enable", false);
    private static final String TAG = "ScrollView";
    private int mActivePointerId;
    private View mChildToScrollTo;
    private EdgeEffect mEdgeGlowBottom;
    private EdgeEffect mEdgeGlowTop;
    @ViewDebug.ExportedProperty(category = "layout")
    private boolean mFillViewport;
    private StrictMode.Span mFlingStrictSpan;
    private boolean mIsBeingDragged;
    private boolean mIsHwTheme;
    private boolean mIsLayoutDirty;
    private int mLastMotionY;
    private long mLastScroll;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private int mNestedYOffset;
    private int mOverflingDistance;
    private int mOverscrollDistance;
    private SavedState mSavedState;
    private final int[] mScrollConsumed;
    private final int[] mScrollOffset;
    private StrictMode.Span mScrollStrictSpan;
    private OverScroller mScroller;
    private boolean mSmoothScrollingEnabled;
    private final Rect mTempRect;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;
    private float mVerticalScrollFactor;

    static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public int scrollPosition;

        SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            this.scrollPosition = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.scrollPosition);
        }

        public String toString() {
            return "ScrollView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " scrollPosition=" + this.scrollPosition + "}";
        }
    }

    public ScrollView(Context context) {
        this(context, null);
    }

    public ScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842880);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTempRect = new Rect();
        this.mIsLayoutDirty = true;
        this.mChildToScrollTo = null;
        this.mIsBeingDragged = false;
        this.mSmoothScrollingEnabled = true;
        this.mActivePointerId = -1;
        this.mScrollOffset = new int[2];
        this.mScrollConsumed = new int[2];
        this.mScrollStrictSpan = null;
        this.mFlingStrictSpan = null;
        initScrollView();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollView, defStyleAttr, defStyleRes);
        setFillViewport(a.getBoolean(0, false));
        a.recycle();
        if (context.getResources().getConfiguration().uiMode == 6) {
            setRevealOnFocusHint(false);
        }
        this.mIsHwTheme = HwWidgetFactory.checkIsHwTheme(context, attrs);
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    /* access modifiers changed from: protected */
    public float getTopFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }
        int length = getVerticalFadingEdgeLength();
        if (this.mScrollY < length) {
            return ((float) this.mScrollY) / ((float) length);
        }
        return 1.0f;
    }

    /* access modifiers changed from: protected */
    public float getBottomFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }
        int length = getVerticalFadingEdgeLength();
        int span = (getChildAt(0).getBottom() - this.mScrollY) - (getHeight() - this.mPaddingBottom);
        if (span < length) {
            return ((float) span) / ((float) length);
        }
        return 1.0f;
    }

    public int getMaxScrollAmount() {
        return (int) (MAX_SCROLL_FACTOR * ((float) (this.mBottom - this.mTop)));
    }

    private void initScrollView() {
        this.mScroller = new OverScroller(getContext());
        setFocusable(true);
        setDescendantFocusability(262144);
        setWillNotDraw(false);
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mOverscrollDistance = configuration.getScaledOverscrollDistance();
        this.mOverflingDistance = configuration.getScaledOverflingDistance();
        this.mVerticalScrollFactor = configuration.getScaledVerticalScrollFactor();
    }

    public void addView(View child) {
        if (getChildCount() <= 0) {
            super.addView(child);
            return;
        }
        throw new IllegalStateException("ScrollView can host only one direct child");
    }

    public void addView(View child, int index) {
        if (getChildCount() <= 0) {
            super.addView(child, index);
            return;
        }
        throw new IllegalStateException("ScrollView can host only one direct child");
    }

    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() <= 0) {
            super.addView(child, params);
            return;
        }
        throw new IllegalStateException("ScrollView can host only one direct child");
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() <= 0) {
            super.addView(child, index, params);
            return;
        }
        throw new IllegalStateException("ScrollView can host only one direct child");
    }

    private boolean canScroll() {
        boolean z = false;
        View child = getChildAt(0);
        if (child == null) {
            return false;
        }
        if (getHeight() < this.mPaddingTop + child.getHeight() + this.mPaddingBottom) {
            z = true;
        }
        return z;
    }

    public boolean isFillViewport() {
        return this.mFillViewport;
    }

    public void setFillViewport(boolean fillViewport) {
        if (fillViewport != this.mFillViewport) {
            this.mFillViewport = fillViewport;
            requestLayout();
        }
    }

    public boolean isSmoothScrollingEnabled() {
        return this.mSmoothScrollingEnabled;
    }

    public void setSmoothScrollingEnabled(boolean smoothScrollingEnabled) {
        this.mSmoothScrollingEnabled = smoothScrollingEnabled;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightPadding;
        int widthPadding;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mFillViewport && View.MeasureSpec.getMode(heightMeasureSpec) != 0 && getChildCount() > 0) {
            View child = getChildAt(0);
            int targetSdkVersion = getContext().getApplicationInfo().targetSdkVersion;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
            if (targetSdkVersion >= 23) {
                widthPadding = this.mPaddingLeft + this.mPaddingRight + lp.leftMargin + lp.rightMargin;
                heightPadding = this.mPaddingTop + this.mPaddingBottom + lp.topMargin + lp.bottomMargin;
            } else {
                widthPadding = this.mPaddingLeft + this.mPaddingRight;
                heightPadding = this.mPaddingTop + this.mPaddingBottom;
            }
            int desiredHeight = getMeasuredHeight() - heightPadding;
            if (child.getMeasuredHeight() < desiredHeight) {
                child.measure(getChildMeasureSpec(widthMeasureSpec, widthPadding, lp.width), View.MeasureSpec.makeMeasureSpec(desiredHeight, 1073741824));
            }
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
    }

    public boolean executeKeyEvent(KeyEvent event) {
        this.mTempRect.setEmpty();
        int i = 130;
        if (!canScroll()) {
            boolean z = false;
            if (!isFocused() || event.getKeyCode() == 4) {
                return false;
            }
            View currentFocused = findFocus();
            if (currentFocused == this) {
                currentFocused = null;
            }
            View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, 130);
            if (!(nextFocused == null || nextFocused == this || !nextFocused.requestFocus(130))) {
                z = true;
            }
            return z;
        }
        boolean handled = false;
        if (event.getAction() == 0) {
            int keyCode = event.getKeyCode();
            if (keyCode != 62) {
                switch (keyCode) {
                    case 19:
                        if (event.isAltPressed()) {
                            handled = fullScroll(33);
                            break;
                        } else {
                            handled = arrowScroll(33);
                            break;
                        }
                    case 20:
                        if (event.isAltPressed()) {
                            handled = fullScroll(130);
                            break;
                        } else {
                            handled = arrowScroll(130);
                            break;
                        }
                }
            } else {
                if (event.isShiftPressed()) {
                    i = 33;
                }
                pageScroll(i);
            }
        }
        return handled;
    }

    private boolean inChild(int x, int y) {
        boolean z = false;
        if (getChildCount() <= 0) {
            return false;
        }
        int scrollY = this.mScrollY;
        View child = getChildAt(0);
        if (y >= child.getTop() - scrollY && y < child.getBottom() - scrollY && x >= child.getLeft() && x < child.getRight()) {
            z = true;
        }
        return z;
    }

    private void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        MotionEvent motionEvent = ev;
        int action = ev.getAction();
        if ((action == 2 && this.mIsBeingDragged) || super.onInterceptTouchEvent(ev)) {
            return true;
        }
        if (getScrollY() == 0 && !canScrollVertically(1)) {
            return false;
        }
        int i = action & 255;
        if (i != 6) {
            switch (i) {
                case 0:
                    int y = (int) ev.getY();
                    if (inChild((int) ev.getX(), y)) {
                        this.mLastMotionY = y;
                        this.mActivePointerId = motionEvent.getPointerId(0);
                        initOrResetVelocityTracker();
                        this.mVelocityTracker.addMovement(motionEvent);
                        this.mScroller.computeScrollOffset();
                        this.mIsBeingDragged = true ^ this.mScroller.isFinished();
                        if (this.mIsBeingDragged && this.mScrollStrictSpan == null) {
                            this.mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
                        }
                        startNestedScroll(2);
                        break;
                    } else {
                        this.mIsBeingDragged = false;
                        recycleVelocityTracker();
                        break;
                    }
                case 1:
                case 3:
                    this.mIsBeingDragged = false;
                    this.mActivePointerId = -1;
                    recycleVelocityTracker();
                    if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        postInvalidateOnAnimation();
                    }
                    stopNestedScroll();
                    break;
                case 2:
                    int activePointerId = this.mActivePointerId;
                    if (activePointerId != -1) {
                        int pointerIndex = motionEvent.findPointerIndex(activePointerId);
                        if (pointerIndex != -1) {
                            int y2 = (int) motionEvent.getY(pointerIndex);
                            if (Math.abs(y2 - this.mLastMotionY) > this.mTouchSlop && (2 & getNestedScrollAxes()) == 0) {
                                this.mIsBeingDragged = true;
                                this.mLastMotionY = y2;
                                initVelocityTrackerIfNotExists();
                                this.mVelocityTracker.addMovement(motionEvent);
                                this.mNestedYOffset = 0;
                                if (this.mScrollStrictSpan == null) {
                                    this.mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
                                }
                                ViewParent parent = getParent();
                                if (parent != null) {
                                    parent.requestDisallowInterceptTouchEvent(true);
                                    break;
                                }
                            }
                        } else {
                            Log.e(TAG, "Invalid pointerId=" + activePointerId + " in onInterceptTouchEvent");
                            break;
                        }
                    }
                    break;
            }
        } else {
            onSecondaryPointerUp(ev);
        }
        return this.mIsBeingDragged;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        MotionEvent motionEvent = ev;
        initVelocityTrackerIfNotExists();
        MotionEvent vtev = MotionEvent.obtain(ev);
        int actionMasked = ev.getActionMasked();
        boolean z = false;
        if (actionMasked == 0) {
            this.mNestedYOffset = 0;
        }
        vtev.offsetLocation(0.0f, (float) this.mNestedYOffset);
        switch (actionMasked) {
            case 0:
                if (getChildCount() != 0) {
                    boolean z2 = !this.mScroller.isFinished();
                    this.mIsBeingDragged = z2;
                    if (z2) {
                        ViewParent parent = getParent();
                        if (parent != null) {
                            parent.requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    if (!this.mScroller.isFinished()) {
                        this.mScroller.abortAnimation();
                        if (this.mFlingStrictSpan != null) {
                            this.mFlingStrictSpan.finish();
                            this.mFlingStrictSpan = null;
                        }
                    }
                    this.mLastMotionY = (int) ev.getY();
                    this.mActivePointerId = motionEvent.getPointerId(0);
                    startNestedScroll(2);
                    break;
                } else {
                    return false;
                }
            case 1:
                if (this.mIsBeingDragged) {
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                    if (Math.abs(initialVelocity) > this.mMinimumVelocity) {
                        flingWithNestedDispatch(-initialVelocity);
                    } else {
                        if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                            postInvalidateOnAnimation();
                        }
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                }
                if (this.mIsHwTheme && ((isTopEdge() || isBottomEdge()) && !this.mScroller.isOverScrolled())) {
                    if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        postInvalidateOnAnimation();
                    }
                    awakenScrollBars();
                    break;
                }
            case 2:
                int activePointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                if (activePointerIndex != -1) {
                    int y = (int) motionEvent.getY(activePointerIndex);
                    int deltaY = this.mLastMotionY - y;
                    if (dispatchNestedPreScroll(0, deltaY, this.mScrollConsumed, this.mScrollOffset)) {
                        deltaY -= this.mScrollConsumed[1];
                        vtev.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                        this.mNestedYOffset += this.mScrollOffset[1];
                    }
                    if (!this.mIsBeingDragged && Math.abs(deltaY) > this.mTouchSlop) {
                        ViewParent parent2 = getParent();
                        if (parent2 != null) {
                            parent2.requestDisallowInterceptTouchEvent(true);
                        }
                        this.mIsBeingDragged = true;
                        if (deltaY > 0) {
                            deltaY -= this.mTouchSlop;
                        } else {
                            deltaY += this.mTouchSlop;
                        }
                    }
                    int deltaY2 = deltaY;
                    if (this.mIsBeingDragged) {
                        this.mLastMotionY = y - this.mScrollOffset[1];
                        int oldY = this.mScrollY;
                        int range = getScrollRange();
                        int overscrollMode = getOverScrollMode();
                        if (overscrollMode == 0 || (overscrollMode == 1 && range > 0)) {
                            z = true;
                        }
                        boolean canOverscroll = z;
                        int i = overscrollMode;
                        int range2 = range;
                        int oldY2 = oldY;
                        int deltaY3 = deltaY2;
                        int i2 = y;
                        int activePointerIndex2 = activePointerIndex;
                        if (overScrollBy(0, deltaY2, 0, this.mScrollY, 0, range2, 0, this.mOverscrollDistance, true) && !hasNestedScrollingParent()) {
                            this.mVelocityTracker.clear();
                        }
                        int scrolledDeltaY = this.mScrollY - oldY2;
                        if (!dispatchNestedScroll(0, scrolledDeltaY, 0, deltaY3 - scrolledDeltaY, this.mScrollOffset)) {
                            if (!canOverscroll) {
                                break;
                            } else {
                                int pulledToY = oldY2 + deltaY3;
                                if (pulledToY < 0) {
                                    this.mEdgeGlowTop.onPull(((float) deltaY3) / ((float) getHeight()), motionEvent.getX(activePointerIndex2) / ((float) getWidth()));
                                    if (!this.mEdgeGlowBottom.isFinished()) {
                                        this.mEdgeGlowBottom.onRelease();
                                    }
                                } else {
                                    int activePointerIndex3 = activePointerIndex2;
                                    if (pulledToY > range2) {
                                        this.mEdgeGlowBottom.onPull(((float) deltaY3) / ((float) getHeight()), 1.0f - (motionEvent.getX(activePointerIndex3) / ((float) getWidth())));
                                        if (!this.mEdgeGlowTop.isFinished()) {
                                            this.mEdgeGlowTop.onRelease();
                                        }
                                    }
                                }
                                if (this.mEdgeGlowTop != null && (!this.mEdgeGlowTop.isFinished() || !this.mEdgeGlowBottom.isFinished())) {
                                    postInvalidateOnAnimation();
                                    break;
                                }
                            }
                        } else {
                            this.mLastMotionY -= this.mScrollOffset[1];
                            vtev.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                            this.mNestedYOffset += this.mScrollOffset[1];
                            int i3 = activePointerIndex2;
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                    break;
                }
                break;
            case 3:
                if (this.mIsBeingDragged != 0 && getChildCount() > 0) {
                    if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        postInvalidateOnAnimation();
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                    break;
                }
            case 5:
                int index = ev.getActionIndex();
                if (index != -1) {
                    this.mLastMotionY = (int) motionEvent.getY(index);
                    this.mActivePointerId = motionEvent.getPointerId(index);
                    break;
                } else {
                    Log.e(TAG, "Invalid index=" + index + " in onTouchEvent");
                    break;
                }
            case 6:
                onSecondaryPointerUp(ev);
                int index2 = motionEvent.findPointerIndex(this.mActivePointerId);
                if (index2 != -1) {
                    this.mLastMotionY = (int) motionEvent.getY(index2);
                    break;
                } else {
                    Log.e(TAG, "Invalid index=" + index2 + " in onTouchEvent");
                    break;
                }
        }
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & 65280) >> 8;
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mLastMotionY = (int) ev.getY(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.clear();
            }
        }
    }

    public boolean onGenericMotionEvent(MotionEvent event) {
        float axisValue;
        if (event.getAction() == 8) {
            if (event.isFromSource(2)) {
                axisValue = event.getAxisValue(9);
            } else if (event.isFromSource(4194304)) {
                axisValue = event.getAxisValue(26);
            } else {
                axisValue = 0.0f;
            }
            int delta = Math.round(this.mVerticalScrollFactor * axisValue);
            if (delta != 0) {
                int range = getScrollRange();
                int oldScrollY = this.mScrollY;
                int newScrollY = oldScrollY - delta;
                if (newScrollY < 0) {
                    newScrollY = 0;
                } else if (newScrollY > range) {
                    newScrollY = range;
                }
                if (newScrollY != oldScrollY) {
                    super.scrollTo(this.mScrollX, newScrollY);
                    return true;
                }
            }
        }
        return super.onGenericMotionEvent(event);
    }

    /* access modifiers changed from: protected */
    public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (!this.mScroller.isFinished()) {
            int oldX = this.mScrollX;
            int oldY = this.mScrollY;
            this.mScrollX = scrollX;
            this.mScrollY = scrollY;
            invalidateParentIfNeeded();
            onScrollChanged(this.mScrollX, this.mScrollY, oldX, oldY);
            if (clampedY) {
                this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange());
            }
        } else {
            super.scrollTo(scrollX, scrollY);
        }
        awakenScrollBars();
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (!isEnabled()) {
            return false;
        }
        if (action != 4096) {
            if (action == 8192 || action == 16908344) {
                int targetScrollY = Math.max(this.mScrollY - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop), 0);
                if (targetScrollY == this.mScrollY) {
                    return false;
                }
                smoothScrollTo(0, targetScrollY);
                return true;
            } else if (action != 16908346) {
                return false;
            }
        }
        int targetScrollY2 = Math.min(this.mScrollY + ((getHeight() - this.mPaddingBottom) - this.mPaddingTop), getScrollRange());
        if (targetScrollY2 == this.mScrollY) {
            return false;
        }
        smoothScrollTo(0, targetScrollY2);
        return true;
    }

    public CharSequence getAccessibilityClassName() {
        return ScrollView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled()) {
            int scrollRange = getScrollRange();
            if (scrollRange > 0) {
                info.setScrollable(true);
                if (this.mScrollY > 0) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP);
                }
                if (this.mScrollY < scrollRange) {
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                    info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN);
                }
            }
        }
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setScrollable(getScrollRange() > 0);
        event.setScrollX(this.mScrollX);
        event.setScrollY(this.mScrollY);
        event.setMaxScrollX(this.mScrollX);
        event.setMaxScrollY(getScrollRange());
    }

    private int getScrollRange() {
        if (getChildCount() > 0) {
            return Math.max(0, getChildAt(0).getHeight() - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop));
        }
        return 0;
    }

    private View findFocusableViewInBounds(boolean topFocus, int top, int bottom) {
        List<View> focusables = getFocusables(2);
        int count = focusables.size();
        boolean foundFullyContainedFocusable = false;
        View focusCandidate = null;
        for (int i = 0; i < count; i++) {
            View view = focusables.get(i);
            int viewTop = view.getTop();
            int viewBottom = view.getBottom();
            if (top < viewBottom && viewTop < bottom) {
                boolean viewIsCloserToBoundary = true;
                boolean viewIsFullyContained = top < viewTop && viewBottom < bottom;
                if (focusCandidate == null) {
                    focusCandidate = view;
                    foundFullyContainedFocusable = viewIsFullyContained;
                } else {
                    if ((!topFocus || viewTop >= focusCandidate.getTop()) && (topFocus || viewBottom <= focusCandidate.getBottom())) {
                        viewIsCloserToBoundary = false;
                    }
                    if (foundFullyContainedFocusable) {
                        if (viewIsFullyContained && viewIsCloserToBoundary) {
                            focusCandidate = view;
                        }
                    } else if (viewIsFullyContained) {
                        focusCandidate = view;
                        foundFullyContainedFocusable = true;
                    } else if (viewIsCloserToBoundary) {
                        focusCandidate = view;
                    }
                }
            }
        }
        return focusCandidate;
    }

    public boolean pageScroll(int direction) {
        boolean down = direction == 130;
        int height = getHeight();
        if (down) {
            this.mTempRect.top = getScrollY() + height;
            int count = getChildCount();
            if (count > 0) {
                View view = getChildAt(count - 1);
                if (this.mTempRect.top + height > view.getBottom()) {
                    this.mTempRect.top = view.getBottom() - height;
                }
            }
        } else {
            this.mTempRect.top = getScrollY() - height;
            if (this.mTempRect.top < 0) {
                this.mTempRect.top = 0;
            }
        }
        this.mTempRect.bottom = this.mTempRect.top + height;
        return scrollAndFocus(direction, this.mTempRect.top, this.mTempRect.bottom);
    }

    public boolean fullScroll(int direction) {
        boolean down = direction == 130;
        int height = getHeight();
        this.mTempRect.top = 0;
        this.mTempRect.bottom = height;
        if (down) {
            int count = getChildCount();
            if (count > 0) {
                this.mTempRect.bottom = getChildAt(count - 1).getBottom() + this.mPaddingBottom;
                this.mTempRect.top = this.mTempRect.bottom - height;
            }
        }
        return scrollAndFocus(direction, this.mTempRect.top, this.mTempRect.bottom);
    }

    private boolean scrollAndFocus(int direction, int top, int bottom) {
        boolean handled = true;
        int height = getHeight();
        int containerTop = getScrollY();
        int containerBottom = containerTop + height;
        boolean up = direction == 33;
        View newFocused = findFocusableViewInBounds(up, top, bottom);
        if (newFocused == null) {
            newFocused = this;
        }
        if (top < containerTop || bottom > containerBottom) {
            doScrollY(up ? top - containerTop : bottom - containerBottom);
        } else {
            handled = false;
        }
        if (newFocused != findFocus()) {
            newFocused.requestFocus(direction);
        }
        return handled;
    }

    public boolean arrowScroll(int direction) {
        View currentFocused = findFocus();
        if (currentFocused == this) {
            currentFocused = null;
        }
        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);
        int maxJump = getMaxScrollAmount();
        if (nextFocused == null || !isWithinDeltaOfScreen(nextFocused, maxJump, getHeight())) {
            int scrollDelta = maxJump;
            if (direction == 33 && getScrollY() < scrollDelta) {
                scrollDelta = getScrollY();
            } else if (direction == 130 && getChildCount() > 0) {
                int daBottom = getChildAt(0).getBottom();
                int screenBottom = (getScrollY() + getHeight()) - this.mPaddingBottom;
                if (daBottom - screenBottom < maxJump) {
                    scrollDelta = daBottom - screenBottom;
                }
            }
            if (scrollDelta == 0) {
                return false;
            }
            doScrollY(direction == 130 ? scrollDelta : -scrollDelta);
        } else {
            nextFocused.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(nextFocused, this.mTempRect);
            doScrollY(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
            nextFocused.requestFocus(direction);
        }
        if (currentFocused != null && currentFocused.isFocused() && isOffScreen(currentFocused)) {
            int descendantFocusability = getDescendantFocusability();
            setDescendantFocusability(131072);
            requestFocus();
            setDescendantFocusability(descendantFocusability);
        }
        return true;
    }

    private boolean isOffScreen(View descendant) {
        return !isWithinDeltaOfScreen(descendant, 0, getHeight());
    }

    private boolean isWithinDeltaOfScreen(View descendant, int delta, int height) {
        descendant.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(descendant, this.mTempRect);
        return this.mTempRect.bottom + delta >= getScrollY() && this.mTempRect.top - delta <= getScrollY() + height;
    }

    private void doScrollY(int delta) {
        if (delta == 0) {
            return;
        }
        if (this.mSmoothScrollingEnabled) {
            smoothScrollBy(0, delta);
        } else {
            scrollBy(0, delta);
        }
    }

    public final void smoothScrollBy(int dx, int dy) {
        if (getChildCount() != 0) {
            if (AnimationUtils.currentAnimationTimeMillis() - this.mLastScroll > 250) {
                int maxY = Math.max(0, getChildAt(0).getHeight() - ((getHeight() - this.mPaddingBottom) - this.mPaddingTop));
                int scrollY = this.mScrollY;
                this.mScroller.startScroll(this.mScrollX, scrollY, 0, Math.max(0, Math.min(scrollY + dy, maxY)) - scrollY);
                postInvalidateOnAnimation();
            } else {
                if (!this.mScroller.isFinished()) {
                    this.mScroller.abortAnimation();
                    if (this.mFlingStrictSpan != null) {
                        this.mFlingStrictSpan.finish();
                        this.mFlingStrictSpan = null;
                    }
                }
                scrollBy(dx, dy);
            }
            this.mLastScroll = AnimationUtils.currentAnimationTimeMillis();
        }
    }

    public final void smoothScrollTo(int x, int y) {
        smoothScrollBy(x - this.mScrollX, y - this.mScrollY);
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollRange() {
        int count = getChildCount();
        int contentHeight = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
        if (count == 0) {
            return contentHeight;
        }
        int scrollRange = getChildAt(0).getBottom();
        int scrollY = this.mScrollY;
        int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }
        return scrollRange;
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    /* access modifiers changed from: protected */
    public void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight, child.getLayoutParams().width), View.MeasureSpec.makeSafeMeasureSpec(Math.max(0, View.MeasureSpec.getSize(parentHeightMeasureSpec) - (this.mPaddingTop + this.mPaddingBottom)), 0));
    }

    /* access modifiers changed from: protected */
    public void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight + lp.leftMargin + lp.rightMargin + widthUsed, lp.width), View.MeasureSpec.makeSafeMeasureSpec(Math.max(0, View.MeasureSpec.getSize(parentHeightMeasureSpec) - ((((this.mPaddingTop + this.mPaddingBottom) + lp.topMargin) + lp.bottomMargin) + heightUsed)), 0));
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            int oldX = this.mScrollX;
            int oldY = this.mScrollY;
            int x = this.mScroller.getCurrX();
            int y = this.mScroller.getCurrY();
            if (!(oldX == x && oldY == y)) {
                int range = getScrollRange();
                int overscrollMode = getOverScrollMode();
                boolean z = true;
                if (overscrollMode != 0 && (overscrollMode != 1 || range <= 0)) {
                    z = false;
                }
                boolean canOverscroll = z;
                int i = overscrollMode;
                overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, range, 0, this.mOverflingDistance, false);
                onScrollChanged(this.mScrollX, this.mScrollY, oldX, oldY);
                if (canOverscroll) {
                    if (y < 0 && oldY >= 0) {
                        this.mEdgeGlowTop.onAbsorb((int) this.mScroller.getCurrVelocity());
                    } else if (y > range && oldY <= range) {
                        this.mEdgeGlowBottom.onAbsorb((int) this.mScroller.getCurrVelocity());
                    }
                }
            }
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        } else if (this.mFlingStrictSpan != null) {
            this.mFlingStrictSpan.finish();
            this.mFlingStrictSpan = null;
        }
    }

    private void scrollToChild(View child) {
        child.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(child, this.mTempRect);
        int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(this.mTempRect);
        if (scrollDelta != 0) {
            scrollBy(0, scrollDelta);
        }
    }

    private boolean scrollToChildRect(Rect rect, boolean immediate) {
        int delta = computeScrollDeltaToGetChildRectOnScreen(rect);
        boolean scroll = delta != 0;
        if (scroll) {
            if (immediate) {
                scrollBy(0, delta);
            } else {
                smoothScrollBy(0, delta);
            }
        }
        return scroll;
    }

    /* access modifiers changed from: protected */
    public int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        int scrollYDelta;
        int scrollYDelta2;
        if (getChildCount() == 0) {
            return 0;
        }
        int height = getHeight();
        int screenTop = getScrollY();
        int screenBottom = screenTop + height;
        int fadingEdge = getVerticalFadingEdgeLength();
        if (rect.top > 0) {
            screenTop += fadingEdge;
        }
        if (rect.bottom < getChildAt(0).getHeight()) {
            screenBottom -= fadingEdge;
        }
        int scrollYDelta3 = 0;
        if (rect.bottom > screenBottom && rect.top > screenTop) {
            if (rect.height() > height) {
                scrollYDelta2 = 0 + (rect.top - screenTop);
            } else {
                scrollYDelta2 = 0 + (rect.bottom - screenBottom);
            }
            scrollYDelta3 = Math.min(scrollYDelta2, getChildAt(0).getBottom() - screenBottom);
        } else if (rect.top < screenTop && rect.bottom < screenBottom) {
            if (rect.height() > height) {
                scrollYDelta = 0 - (screenBottom - rect.bottom);
            } else {
                scrollYDelta = 0 - (screenTop - rect.top);
            }
            scrollYDelta3 = Math.max(scrollYDelta, -getScrollY());
        }
        return scrollYDelta3;
    }

    public void requestChildFocus(View child, View focused) {
        if (focused != null && focused.getRevealOnFocusHint()) {
            if (!this.mIsLayoutDirty) {
                scrollToChild(focused);
            } else {
                this.mChildToScrollTo = focused;
            }
        }
        super.requestChildFocus(child, focused);
    }

    /* access modifiers changed from: protected */
    public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        View nextFocus;
        if (direction == 2) {
            direction = 130;
        } else if (direction == 1) {
            direction = 33;
        }
        if (previouslyFocusedRect == null) {
            nextFocus = FocusFinder.getInstance().findNextFocus(this, null, direction);
        } else {
            nextFocus = FocusFinder.getInstance().findNextFocusFromRect(this, previouslyFocusedRect, direction);
        }
        if (nextFocus != null && !isOffScreen(nextFocus)) {
            return nextFocus.requestFocus(direction, previouslyFocusedRect);
        }
        return false;
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
        return scrollToChildRect(rectangle, immediate);
    }

    public void requestLayout() {
        this.mIsLayoutDirty = true;
        super.requestLayout();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
        if (this.mFlingStrictSpan != null) {
            this.mFlingStrictSpan.finish();
            this.mFlingStrictSpan = null;
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mIsLayoutDirty = false;
        if (this.mChildToScrollTo != null && isViewDescendantOf(this.mChildToScrollTo, this)) {
            scrollToChild(this.mChildToScrollTo);
        }
        this.mChildToScrollTo = null;
        if (!isLaidOut()) {
            if (this.mSavedState != null) {
                this.mScrollY = this.mSavedState.scrollPosition;
                this.mSavedState = null;
            }
            int scrollRange = Math.max(0, (getChildCount() > 0 ? getChildAt(0).getMeasuredHeight() : 0) - (((b - t) - this.mPaddingBottom) - this.mPaddingTop));
            if (this.mScrollY > scrollRange) {
                this.mScrollY = scrollRange;
            } else if (this.mScrollY < 0) {
                this.mScrollY = 0;
            }
        }
        scrollTo(this.mScrollX, this.mScrollY);
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        View currentFocused = findFocus();
        if (!(currentFocused == null || this == currentFocused || !isWithinDeltaOfScreen(currentFocused, 0, oldh))) {
            currentFocused.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(currentFocused, this.mTempRect);
            doScrollY(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
        }
    }

    private static boolean isViewDescendantOf(View child, View parent) {
        boolean z = true;
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        if (!(theParent instanceof ViewGroup) || !isViewDescendantOf((View) theParent, parent)) {
            z = false;
        }
        return z;
    }

    public void fling(int velocityY) {
        if (getChildCount() > 0) {
            int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
            int i = 0;
            int bottom = getChildAt(0).getHeight();
            if (bottom > height) {
                i = bottom - height;
            }
            int maxX = i;
            if (SMART_SLIDE_PROPERTIES) {
                this.mScroller.fling(this.mScrollX, this.mScrollY, 0, velocityY, 0, 0, 0, maxX, 0, height / 2, DEFAULT_MULTIPLE_FLING_LENGTH_THRESHOLD);
            } else {
                OverScroller overScroller = this.mScroller;
                overScroller.fling(this.mScrollX, this.mScrollY, 0, velocityY, 0, 0, 0, maxX, 0, height / 2);
            }
            if (this.mFlingStrictSpan == null) {
                this.mFlingStrictSpan = StrictMode.enterCriticalSpan("ScrollView-fling");
            }
            postInvalidateOnAnimation();
        }
    }

    private void flingWithNestedDispatch(int velocityY) {
        boolean canFling = (this.mScrollY > 0 || velocityY > 0) && (this.mScrollY < getScrollRange() || velocityY < 0);
        if (!dispatchNestedPreFling(0.0f, (float) velocityY)) {
            dispatchNestedFling(0.0f, (float) velocityY, canFling);
            if (canFling) {
                fling(velocityY);
            }
        }
    }

    private void endDrag() {
        this.mIsBeingDragged = false;
        recycleVelocityTracker();
        if (this.mEdgeGlowTop != null) {
            this.mEdgeGlowTop.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        if (this.mScrollStrictSpan != null) {
            this.mScrollStrictSpan.finish();
            this.mScrollStrictSpan = null;
        }
    }

    public void scrollTo(int x, int y) {
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            int x2 = clamp(x, (getWidth() - this.mPaddingRight) - this.mPaddingLeft, child.getWidth());
            int y2 = clamp(y, (getHeight() - this.mPaddingBottom) - this.mPaddingTop, child.getHeight());
            if (x2 != this.mScrollX || y2 != this.mScrollY) {
                super.scrollTo(x2, y2);
            }
        }
    }

    public void setOverScrollMode(int mode) {
        if (mode == 2) {
            this.mEdgeGlowTop = null;
            this.mEdgeGlowBottom = null;
        } else if (this.mEdgeGlowTop == null) {
            Context context = getContext();
            this.mEdgeGlowTop = new EdgeEffect(context);
            this.mEdgeGlowBottom = new EdgeEffect(context);
        }
        super.setOverScrollMode(mode);
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & 2) != 0;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(2);
    }

    public void onStopNestedScroll(View target) {
        super.onStopNestedScroll(target);
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int oldScrollY = this.mScrollY;
        scrollBy(0, dyUnconsumed);
        int myConsumed = this.mScrollY - oldScrollY;
        dispatchNestedScroll(0, myConsumed, 0, dyUnconsumed - myConsumed, null);
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (consumed) {
            return false;
        }
        flingWithNestedDispatch((int) velocityY);
        return true;
    }

    public void draw(Canvas canvas) {
        float translateY;
        float translateX;
        int height;
        int width;
        float translateY2;
        float translateX2;
        int height2;
        int width2;
        super.draw(canvas);
        if (this.mEdgeGlowTop != null) {
            int scrollY = this.mScrollY;
            boolean clipToPadding = getClipToPadding();
            if (!this.mEdgeGlowTop.isFinished()) {
                int restoreCount = canvas.save();
                if (clipToPadding) {
                    width2 = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
                    height2 = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
                    translateX2 = (float) this.mPaddingLeft;
                    translateY2 = (float) this.mPaddingTop;
                } else {
                    width2 = getWidth();
                    height2 = getHeight();
                    translateX2 = 0.0f;
                    translateY2 = 0.0f;
                }
                canvas.translate(translateX2, ((float) Math.min(0, scrollY)) + translateY2);
                this.mEdgeGlowTop.setSize(width2, height2);
                if (this.mEdgeGlowTop.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount);
            }
            if (!this.mEdgeGlowBottom.isFinished()) {
                int restoreCount2 = canvas.save();
                if (clipToPadding) {
                    width = (getWidth() - this.mPaddingLeft) - this.mPaddingRight;
                    height = (getHeight() - this.mPaddingTop) - this.mPaddingBottom;
                    translateX = (float) this.mPaddingLeft;
                    translateY = (float) this.mPaddingTop;
                } else {
                    width = getWidth();
                    height = getHeight();
                    translateX = 0.0f;
                    translateY = 0.0f;
                }
                canvas.translate(((float) (-width)) + translateX, ((float) (Math.max(getScrollRange(), scrollY) + height)) + translateY);
                canvas.rotate(180.0f, (float) width, 0.0f);
                this.mEdgeGlowBottom.setSize(width, height);
                if (this.mEdgeGlowBottom.draw(canvas)) {
                    postInvalidateOnAnimation();
                }
                canvas.restoreToCount(restoreCount2);
            }
        }
    }

    private static int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            return 0;
        }
        if (my + n > child) {
            return child - my;
        }
        return n;
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mSavedState = ss;
        requestLayout();
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            return super.onSaveInstanceState();
        }
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.scrollPosition = this.mScrollY;
        return ss;
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("fillViewport", this.mFillViewport);
    }

    /* access modifiers changed from: protected */
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int maxOverScrollY2;
        int deltaY2;
        int i;
        int deltaY3;
        if (this.mIsHwTheme) {
            if ((isTopEdge() || isBottomEdge()) && isTouchEvent) {
                i = scrollY;
                deltaY3 = getElasticInterpolation(deltaY, i);
            } else {
                deltaY3 = deltaY;
                i = scrollY;
            }
            int maxOverScrollY3 = getHeight();
            invalidate();
            deltaY2 = deltaY3;
            maxOverScrollY2 = maxOverScrollY3;
        } else {
            i = scrollY;
            maxOverScrollY2 = maxOverScrollY;
            deltaY2 = deltaY;
        }
        return super.overScrollBy(deltaX, deltaY2, scrollX, i, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY2, isTouchEvent);
    }

    private int getElasticInterpolation(int delta, int currentPos) {
        if (isBottomEdge()) {
            currentPos -= getScrollRange();
        }
        float len = (float) Math.abs(currentPos);
        int newDelta = (int) ((Math.sqrt(((double) (250.0f * ((float) Math.abs(delta)))) + Math.pow((double) len, 2.0d)) - ((double) len)) * ((double) Math.signum((float) delta)));
        if (Math.abs(newDelta) > Math.abs(delta)) {
            return delta;
        }
        return newDelta;
    }

    private boolean isTopEdge() {
        return getScrollY() < 0;
    }

    private boolean isBottomEdge() {
        return getScrollRange() <= getScrollY();
    }
}
