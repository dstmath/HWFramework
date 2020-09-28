package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hwcontrol.HwWidgetFactory;
import android.media.TtmlUtils;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.StrictMode;
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
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import android.widget.FrameLayout;
import com.android.internal.R;
import java.util.List;

public class ScrollView extends FrameLayout {
    static final int ANIMATED_SCROLL_GAP = 250;
    private static final int INVALID_POINTER = -1;
    static final float MAX_SCROLL_FACTOR = 0.5f;
    private static final String TAG = "ScrollView";
    private int mActivePointerId;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769715)
    private View mChildToScrollTo;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769386)
    private EdgeEffect mEdgeGlowBottom;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123768600)
    private EdgeEffect mEdgeGlowTop;
    @ViewDebug.ExportedProperty(category = TtmlUtils.TAG_LAYOUT)
    private boolean mFillViewport;
    @UnsupportedAppUsage
    private StrictMode.Span mFlingStrictSpan;
    private HwSpringBackHelper mHwSpringBackHelper;
    @UnsupportedAppUsage
    private boolean mIsBeingDragged;
    private boolean mIsHwTheme;
    private boolean mIsLayoutDirty;
    @UnsupportedAppUsage
    private int mLastMotionY;
    @UnsupportedAppUsage
    private long mLastScroll;
    private int mMaximumVelocity;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124051125)
    private int mMinimumVelocity;
    private int mNestedYOffset;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050903)
    private int mOverflingDistance;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 124050903)
    private int mOverscrollDistance;
    private SavedState mSavedState;
    private final int[] mScrollConsumed;
    private final int[] mScrollOffset;
    private StrictMode.Span mScrollStrictSpan;
    @UnsupportedAppUsage
    private OverScroller mScroller;
    private boolean mSmoothScrollingEnabled;
    private final Rect mTempRect;
    private int mTouchSlop;
    @UnsupportedAppUsage
    private VelocityTracker mVelocityTracker;
    private float mVerticalScrollFactor;

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<ScrollView> {
        private int mFillViewportId;
        private boolean mPropertiesMapped = false;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mFillViewportId = propertyMapper.mapBoolean("fillViewport", 16843130);
            this.mPropertiesMapped = true;
        }

        public void readProperties(ScrollView node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readBoolean(this.mFillViewportId, node.isFillViewport());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
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
        this.mEdgeGlowTop = new EdgeEffect(getContext());
        this.mEdgeGlowBottom = new EdgeEffect(getContext());
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
        saveAttributeDataForStyleable(context, R.styleable.ScrollView, attrs, a, defStyleAttr, defStyleRes);
        setFillViewport(a.getBoolean(0, false));
        a.recycle();
        if (context.getResources().getConfiguration().uiMode == 6) {
            setRevealOnFocusHint(false);
        }
        this.mIsHwTheme = HwWidgetFactory.checkIsHwTheme(context, attrs);
        if (this.mIsHwTheme) {
            this.mHwSpringBackHelper = HwWidgetFactory.getHwSpringBackHelper();
        }
    }

    private boolean handleScrollToTop() {
        if (getScrollY() <= 0 || getScrollY() > getScrollRange() || !isScrollToTopEnabled()) {
            return false;
        }
        post(new Runnable() {
            /* class android.widget.ScrollView.AnonymousClass1 */

            public void run() {
                ScrollView.this.smoothScrollTo(0, 0);
            }
        });
        return true;
    }

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchStatusBarTop() {
        boolean result = false;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view != null) {
                result |= view.dispatchStatusBarTop();
            }
        }
        if (result || !isNeedScrollToTop()) {
            return result;
        }
        return handleScrollToTop();
    }

    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
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
    @Override // android.view.View
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

    public void setEdgeEffectColor(int color) {
        setTopEdgeEffectColor(color);
        setBottomEdgeEffectColor(color);
    }

    public void setBottomEdgeEffectColor(int color) {
        this.mEdgeGlowBottom.setColor(color);
    }

    public void setTopEdgeEffectColor(int color) {
        this.mEdgeGlowTop.setColor(color);
    }

    public int getTopEdgeEffectColor() {
        return this.mEdgeGlowTop.getColor();
    }

    public int getBottomEdgeEffectColor() {
        return this.mEdgeGlowBottom.getColor();
    }

    public int getMaxScrollAmount() {
        return (int) (((float) (this.mBottom - this.mTop)) * MAX_SCROLL_FACTOR);
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

    @Override // android.view.ViewGroup
    public void addView(View child) {
        if (getChildCount() <= 0) {
            super.addView(child);
            return;
        }
        throw new IllegalStateException("ScrollView can host only one direct child");
    }

    @Override // android.view.ViewGroup
    public void addView(View child, int index) {
        if (getChildCount() <= 0) {
            super.addView(child, index);
            return;
        }
        throw new IllegalStateException("ScrollView can host only one direct child");
    }

    @Override // android.view.ViewGroup, android.view.ViewManager
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() <= 0) {
            super.addView(child, params);
            return;
        }
        throw new IllegalStateException("ScrollView can host only one direct child");
    }

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() <= 0) {
            super.addView(child, index, params);
            return;
        }
        throw new IllegalStateException("ScrollView can host only one direct child");
    }

    @UnsupportedAppUsage
    private boolean canScroll() {
        View child = getChildAt(0);
        if (child == null) {
            return false;
        }
        if (getHeight() < this.mPaddingTop + child.getHeight() + this.mPaddingBottom) {
            return true;
        }
        return false;
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
    @Override // android.widget.FrameLayout, android.view.View
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

    @Override // android.view.View, android.view.ViewGroup
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event) || executeKeyEvent(event);
    }

    public boolean executeKeyEvent(KeyEvent event) {
        this.mTempRect.setEmpty();
        int i = 130;
        if (!canScroll()) {
            if (!isFocused() || event.getKeyCode() == 4) {
                return false;
            }
            View currentFocused = findFocus();
            if (currentFocused == this) {
                currentFocused = null;
            }
            View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, 130);
            if (nextFocused == null || nextFocused == this || !nextFocused.requestFocus(130)) {
                return false;
            }
            return true;
        } else if (event.getAction() != 0) {
            return false;
        } else {
            int keyCode = event.getKeyCode();
            if (keyCode != 19) {
                if (keyCode != 20) {
                    if (keyCode != 62) {
                        return false;
                    }
                    if (event.isShiftPressed()) {
                        i = 33;
                    }
                    pageScroll(i);
                    return false;
                } else if (!event.isAltPressed()) {
                    return arrowScroll(130);
                } else {
                    return fullScroll(130);
                }
            } else if (!event.isAltPressed()) {
                return arrowScroll(33);
            } else {
                return fullScroll(33);
            }
        }
    }

    private boolean inChild(int x, int y) {
        if (getChildCount() <= 0) {
            return false;
        }
        int scrollY = this.mScrollY;
        View child = getChildAt(0);
        if (y < child.getTop() - scrollY || y >= child.getBottom() - scrollY || x < child.getLeft() || x >= child.getRight()) {
            return false;
        }
        return true;
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public boolean isEnforceableOverScrollEnabled() {
        return false;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if ((action == 2 && this.mIsBeingDragged) || super.onInterceptTouchEvent(ev)) {
            return true;
        }
        if (!isEnforceableOverScrollEnabled() && getScrollY() == 0 && !canScrollVertically(1)) {
            return false;
        }
        int i = action & 255;
        if (i != 0) {
            if (i != 1) {
                if (i == 2) {
                    int activePointerId = this.mActivePointerId;
                    if (activePointerId != -1) {
                        int pointerIndex = ev.findPointerIndex(activePointerId);
                        if (pointerIndex == -1) {
                            Log.e(TAG, "Invalid pointerId=" + activePointerId + " in onInterceptTouchEvent");
                        } else {
                            int y = (int) ev.getY(pointerIndex);
                            if (Math.abs(y - this.mLastMotionY) > this.mTouchSlop && (2 & getNestedScrollAxes()) == 0) {
                                this.mIsBeingDragged = true;
                                this.mLastMotionY = y;
                                initVelocityTrackerIfNotExists();
                                this.mVelocityTracker.addMovement(ev);
                                this.mNestedYOffset = 0;
                                if (this.mScrollStrictSpan == null) {
                                    this.mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
                                }
                                ViewParent parent = getParent();
                                if (parent != null) {
                                    parent.requestDisallowInterceptTouchEvent(true);
                                }
                            }
                        }
                    }
                } else if (i != 3) {
                    if (i == 6) {
                        onSecondaryPointerUp(ev);
                    }
                }
            }
            this.mIsBeingDragged = false;
            this.mActivePointerId = -1;
            recycleVelocityTracker();
            if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                hwSpringBack();
                postInvalidateOnAnimation();
            }
            stopNestedScroll();
        } else {
            int y2 = (int) ev.getY();
            if (!inChild((int) ev.getX(), y2)) {
                this.mIsBeingDragged = false;
                recycleVelocityTracker();
            } else {
                this.mLastMotionY = y2;
                this.mActivePointerId = ev.getPointerId(0);
                initOrResetVelocityTracker();
                this.mVelocityTracker.addMovement(ev);
                this.mScroller.computeScrollOffset();
                this.mIsBeingDragged = !this.mScroller.isFinished();
                HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
                if (hwSpringBackHelper != null && hwSpringBackHelper.computeScrollOffset()) {
                    this.mIsBeingDragged = true ^ this.mHwSpringBackHelper.isFinished();
                }
                if (this.mIsBeingDragged && this.mScrollStrictSpan == null) {
                    this.mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
                }
                startNestedScroll(2);
            }
        }
        return this.mIsBeingDragged;
    }

    private boolean shouldDisplayEdgeEffects() {
        return getOverScrollMode() != 2;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        ViewParent parent;
        int deltaY;
        initVelocityTrackerIfNotExists();
        MotionEvent vtev = MotionEvent.obtain(ev);
        int actionMasked = ev.getActionMasked();
        boolean canOverscroll = false;
        if (actionMasked == 0) {
            this.mNestedYOffset = 0;
        }
        vtev.offsetLocation(0.0f, (float) this.mNestedYOffset);
        if (actionMasked != 0) {
            if (actionMasked == 1) {
                if (this.mIsBeingDragged) {
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                    int initialVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                    if (Math.abs(initialVelocity) > this.mMinimumVelocity) {
                        flingWithNestedDispatch(-initialVelocity);
                    } else if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        hwSpringBack();
                        postInvalidateOnAnimation();
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                }
                if (this.mIsHwTheme && ((isTopEdge() || isBottomEdge()) && !this.mScroller.isOverScrolled())) {
                    if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                        hwSpringBack();
                        postInvalidateOnAnimation();
                    }
                    awakenScrollBars();
                }
            } else if (actionMasked == 2) {
                int activePointerIndex = ev.findPointerIndex(this.mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                } else {
                    int y = (int) ev.getY(activePointerIndex);
                    int deltaY2 = this.mLastMotionY - y;
                    if (dispatchNestedPreScroll(0, deltaY2, this.mScrollConsumed, this.mScrollOffset)) {
                        deltaY2 -= this.mScrollConsumed[1];
                        vtev.offsetLocation(0.0f, (float) this.mScrollOffset[1]);
                        this.mNestedYOffset += this.mScrollOffset[1];
                    }
                    if (this.mIsBeingDragged || Math.abs(deltaY2) <= this.mTouchSlop) {
                        deltaY = deltaY2;
                    } else {
                        ViewParent parent2 = getParent();
                        if (parent2 != null) {
                            parent2.requestDisallowInterceptTouchEvent(true);
                        }
                        this.mIsBeingDragged = true;
                        if (deltaY2 > 0) {
                            deltaY = deltaY2 - this.mTouchSlop;
                        } else {
                            deltaY = deltaY2 + this.mTouchSlop;
                        }
                    }
                    if (this.mIsBeingDragged) {
                        this.mLastMotionY = y - this.mScrollOffset[1];
                        int oldY = this.mScrollY;
                        int range = getScrollRange();
                        int overscrollMode = getOverScrollMode();
                        if (overscrollMode == 0 || (overscrollMode == 1 && range > 0)) {
                            canOverscroll = true;
                        }
                        if (overScrollBy(0, deltaY, 0, this.mScrollY, 0, range, 0, this.mOverscrollDistance, true) && !hasNestedScrollingParent()) {
                            this.mVelocityTracker.clear();
                        }
                        int scrolledDeltaY = this.mScrollY - oldY;
                        if (dispatchNestedScroll(0, scrolledDeltaY, 0, deltaY - scrolledDeltaY, this.mScrollOffset)) {
                            int i = this.mLastMotionY;
                            int[] iArr = this.mScrollOffset;
                            this.mLastMotionY = i - iArr[1];
                            vtev.offsetLocation(0.0f, (float) iArr[1]);
                            this.mNestedYOffset += this.mScrollOffset[1];
                        } else if (canOverscroll) {
                            int pulledToY = oldY + deltaY;
                            if (pulledToY < 0) {
                                this.mEdgeGlowTop.onPull(((float) deltaY) / ((float) getHeight()), ev.getX(activePointerIndex) / ((float) getWidth()));
                                if (!this.mEdgeGlowBottom.isFinished()) {
                                    this.mEdgeGlowBottom.onRelease();
                                }
                            } else if (pulledToY > range) {
                                this.mEdgeGlowBottom.onPull(((float) deltaY) / ((float) getHeight()), 1.0f - (ev.getX(activePointerIndex) / ((float) getWidth())));
                                if (!this.mEdgeGlowTop.isFinished()) {
                                    this.mEdgeGlowTop.onRelease();
                                }
                            }
                            if (shouldDisplayEdgeEffects() && (!this.mEdgeGlowTop.isFinished() || !this.mEdgeGlowBottom.isFinished())) {
                                postInvalidateOnAnimation();
                            }
                        }
                    }
                }
            } else if (actionMasked != 3) {
                if (actionMasked == 5) {
                    int index = ev.getActionIndex();
                    if (index == -1) {
                        Log.e(TAG, "Invalid index=" + index + " in onTouchEvent");
                    } else {
                        this.mLastMotionY = (int) ev.getY(index);
                        this.mActivePointerId = ev.getPointerId(index);
                    }
                } else if (actionMasked == 6) {
                    onSecondaryPointerUp(ev);
                    int index2 = ev.findPointerIndex(this.mActivePointerId);
                    if (index2 == -1) {
                        Log.e(TAG, "Invalid index=" + index2 + " in onTouchEvent");
                    } else {
                        this.mLastMotionY = (int) ev.getY(index2);
                    }
                }
            } else if (this.mIsBeingDragged && getChildCount() > 0) {
                if (this.mScroller.springBack(this.mScrollX, this.mScrollY, 0, 0, 0, getScrollRange())) {
                    hwSpringBack();
                    postInvalidateOnAnimation();
                }
                this.mActivePointerId = -1;
                endDrag();
            }
        } else if (getChildCount() == 0) {
            return false;
        } else {
            boolean z = !this.mScroller.isFinished();
            this.mIsBeingDragged = z;
            if (z && (parent = getParent()) != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
            if (!this.mScroller.isFinished()) {
                this.mScroller.abortAnimation();
                StrictMode.Span span = this.mFlingStrictSpan;
                if (span != null) {
                    span.finish();
                    this.mFlingStrictSpan = null;
                }
            }
            onHwTouchDown();
            this.mLastMotionY = (int) ev.getY();
            this.mActivePointerId = ev.getPointerId(0);
            startNestedScroll(2);
        }
        VelocityTracker velocityTracker2 = this.mVelocityTracker;
        if (velocityTracker2 != null) {
            velocityTracker2.addMovement(vtev);
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
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
    }

    @Override // android.view.View
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
    @Override // android.view.View
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
                hwSpringBack();
            }
        } else {
            super.scrollTo(scrollX, scrollY);
        }
        awakenScrollBars();
    }

    @Override // android.view.View
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

    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public CharSequence getAccessibilityClassName() {
        return ScrollView.class.getName();
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        int scrollRange;
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled() && (scrollRange = getScrollRange()) > 0) {
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

    @Override // android.view.View
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
        View focusCandidate = null;
        boolean foundFullyContainedFocusable = false;
        int count = focusables.size();
        for (int i = 0; i < count; i++) {
            View view = focusables.get(i);
            int viewTop = view.getTop();
            int viewBottom = view.getBottom();
            if (top < viewBottom && viewTop < bottom) {
                boolean viewIsCloserToBoundary = false;
                boolean viewIsFullyContained = top < viewTop && viewBottom < bottom;
                if (focusCandidate == null) {
                    focusCandidate = view;
                    foundFullyContainedFocusable = viewIsFullyContained;
                } else {
                    if ((topFocus && viewTop < focusCandidate.getTop()) || (!topFocus && viewBottom > focusCandidate.getBottom())) {
                        viewIsCloserToBoundary = true;
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
        Rect rect = this.mTempRect;
        rect.bottom = rect.top + height;
        return scrollAndFocus(direction, this.mTempRect.top, this.mTempRect.bottom);
    }

    public boolean fullScroll(int direction) {
        int count;
        boolean down = direction == 130;
        int height = getHeight();
        Rect rect = this.mTempRect;
        rect.top = 0;
        rect.bottom = height;
        if (down && (count = getChildCount()) > 0) {
            this.mTempRect.bottom = getChildAt(count - 1).getBottom() + this.mPaddingBottom;
            Rect rect2 = this.mTempRect;
            rect2.top = rect2.bottom - height;
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
        if (currentFocused == null || !currentFocused.isFocused() || !isOffScreen(currentFocused)) {
            return true;
        }
        int descendantFocusability = getDescendantFocusability();
        setDescendantFocusability(131072);
        requestFocus();
        setDescendantFocusability(descendantFocusability);
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
                    StrictMode.Span span = this.mFlingStrictSpan;
                    if (span != null) {
                        span.finish();
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
    @Override // android.view.View
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
            return scrollRange - scrollY;
        }
        if (scrollY > overscrollBottom) {
            return scrollRange + (scrollY - overscrollBottom);
        }
        return scrollRange;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight, child.getLayoutParams().width), View.MeasureSpec.makeSafeMeasureSpec(Math.max(0, View.MeasureSpec.getSize(parentHeightMeasureSpec) - (this.mPaddingTop + this.mPaddingBottom)), 0));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        child.measure(getChildMeasureSpec(parentWidthMeasureSpec, this.mPaddingLeft + this.mPaddingRight + lp.leftMargin + lp.rightMargin + widthUsed, lp.width), View.MeasureSpec.makeSafeMeasureSpec(Math.max(0, View.MeasureSpec.getSize(parentHeightMeasureSpec) - ((((this.mPaddingTop + this.mPaddingBottom) + lp.topMargin) + lp.bottomMargin) + heightUsed)), 0));
    }

    @Override // android.view.View
    public void computeScroll() {
        int newY;
        HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
        if (hwSpringBackHelper != null && hwSpringBackHelper.computeScrollOffset()) {
            this.mScroller.computeScrollOffset();
            computeHwScroll();
        } else if (this.mScroller.computeScrollOffset()) {
            int oldX = this.mScrollX;
            int oldY = this.mScrollY;
            int newX = this.mScroller.getCurrX();
            int newY2 = this.mScroller.getCurrY();
            if (!(oldX == newX && oldY == newY2)) {
                int range = getScrollRange();
                int overscrollMode = getOverScrollMode();
                boolean canOverscroll = true;
                if (overscrollMode != 0 && (overscrollMode != 1 || range <= 0)) {
                    canOverscroll = false;
                }
                if (canOverscroll) {
                    if (newY2 < 0 && oldY >= 0) {
                        this.mEdgeGlowTop.onAbsorb((int) this.mScroller.getCurrVelocity());
                        HwSpringBackHelper hwSpringBackHelper2 = this.mHwSpringBackHelper;
                        if (hwSpringBackHelper2 != null) {
                            hwSpringBackHelper2.overFling(-this.mScroller.getCurrVelocity(), -1, 0);
                            this.mScroller.abortAnimation();
                            newY = -1;
                            overScrollBy(newX - oldX, newY - oldY, oldX, oldY, 0, range, 0, this.mOverflingDistance, false);
                            onScrollChanged(this.mScrollX, this.mScrollY, oldX, oldY);
                        }
                    } else if (newY2 > range && oldY <= range) {
                        this.mEdgeGlowBottom.onAbsorb((int) this.mScroller.getCurrVelocity());
                        HwSpringBackHelper hwSpringBackHelper3 = this.mHwSpringBackHelper;
                        if (hwSpringBackHelper3 != null) {
                            int newY3 = range + 1;
                            hwSpringBackHelper3.overFling(this.mScroller.getCurrVelocity(), newY3, range);
                            this.mScroller.abortAnimation();
                            newY = newY3;
                            overScrollBy(newX - oldX, newY - oldY, oldX, oldY, 0, range, 0, this.mOverflingDistance, false);
                            onScrollChanged(this.mScrollX, this.mScrollY, oldX, oldY);
                        }
                    }
                }
                newY = newY2;
                overScrollBy(newX - oldX, newY - oldY, oldX, oldY, 0, range, 0, this.mOverflingDistance, false);
                onScrollChanged(this.mScrollX, this.mScrollY, oldX, oldY);
            }
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        } else {
            StrictMode.Span span = this.mFlingStrictSpan;
            if (span != null) {
                span.finish();
                this.mFlingStrictSpan = null;
            }
        }
    }

    public void scrollToDescendant(View child) {
        if (!this.mIsLayoutDirty) {
            child.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(child, this.mTempRect);
            int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(this.mTempRect);
            if (scrollDelta != 0) {
                scrollBy(0, scrollDelta);
                return;
            }
            return;
        }
        this.mChildToScrollTo = child;
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
        if (rect.bottom > screenBottom && rect.top > screenTop) {
            if (rect.height() > height) {
                scrollYDelta2 = 0 + (rect.top - screenTop);
            } else {
                scrollYDelta2 = 0 + (rect.bottom - screenBottom);
            }
            return Math.min(scrollYDelta2, getChildAt(0).getBottom() - screenBottom);
        } else if (rect.top >= screenTop || rect.bottom >= screenBottom) {
            return 0;
        } else {
            if (rect.height() > height) {
                scrollYDelta = 0 - (screenBottom - rect.bottom);
            } else {
                scrollYDelta = 0 - (screenTop - rect.top);
            }
            return Math.max(scrollYDelta, -getScrollY());
        }
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void requestChildFocus(View child, View focused) {
        if (focused != null && focused.getRevealOnFocusHint()) {
            if (!this.mIsLayoutDirty) {
                scrollToDescendant(focused);
            } else {
                this.mChildToScrollTo = focused;
            }
        }
        super.requestChildFocus(child, focused);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
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

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        rectangle.offset(child.getLeft() - child.getScrollX(), child.getTop() - child.getScrollY());
        return scrollToChildRect(rectangle, immediate);
    }

    @Override // android.view.ViewParent, android.view.View
    public void requestLayout() {
        this.mIsLayoutDirty = true;
        super.requestLayout();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        StrictMode.Span span = this.mScrollStrictSpan;
        if (span != null) {
            span.finish();
            this.mScrollStrictSpan = null;
        }
        StrictMode.Span span2 = this.mFlingStrictSpan;
        if (span2 != null) {
            span2.finish();
            this.mFlingStrictSpan = null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mIsLayoutDirty = false;
        View view = this.mChildToScrollTo;
        if (view != null && isViewDescendantOf(view, this)) {
            scrollToDescendant(this.mChildToScrollTo);
        }
        this.mChildToScrollTo = null;
        if (!isLaidOut()) {
            SavedState savedState = this.mSavedState;
            if (savedState != null) {
                this.mScrollY = savedState.scrollPosition;
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
    @Override // android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        View currentFocused = findFocus();
        if (currentFocused != null && this != currentFocused && isWithinDeltaOfScreen(currentFocused, 0, oldh)) {
            currentFocused.getDrawingRect(this.mTempRect);
            offsetDescendantRectToMyCoords(currentFocused, this.mTempRect);
            doScrollY(computeScrollDeltaToGetChildRectOnScreen(this.mTempRect));
        }
    }

    private static boolean isViewDescendantOf(View child, View parent) {
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        if (!(theParent instanceof ViewGroup) || !isViewDescendantOf((View) theParent, parent)) {
            return false;
        }
        return true;
    }

    public void fling(int velocityY) {
        if (getChildCount() > 0) {
            int height = (getHeight() - this.mPaddingBottom) - this.mPaddingTop;
            int bottom = getChildAt(0).getHeight();
            if (this.mHwSpringBackHelper != null) {
                this.mScroller.fling(0, this.mScrollY, 0, velocityY, 0, 0, -height, getScrollRange() + height, 0, 0);
            } else {
                this.mScroller.fling(this.mScrollX, this.mScrollY, 0, velocityY, 0, 0, 0, Math.max(0, bottom - height), 0, height / 2);
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

    @UnsupportedAppUsage
    private void endDrag() {
        this.mIsBeingDragged = false;
        recycleVelocityTracker();
        if (shouldDisplayEdgeEffects()) {
            this.mEdgeGlowTop.onRelease();
            this.mEdgeGlowBottom.onRelease();
        }
        StrictMode.Span span = this.mScrollStrictSpan;
        if (span != null) {
            span.finish();
            this.mScrollStrictSpan = null;
        }
    }

    @Override // android.view.View
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

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & 2) != 0;
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(2);
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onStopNestedScroll(View target) {
        super.onStopNestedScroll(target);
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int oldScrollY = this.mScrollY;
        scrollBy(0, dyUnconsumed);
        int myConsumed = this.mScrollY - oldScrollY;
        dispatchNestedScroll(0, myConsumed, 0, dyUnconsumed - myConsumed, null);
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (consumed) {
            return false;
        }
        flingWithNestedDispatch((int) velocityY);
        return true;
    }

    @Override // android.view.View
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
        if (shouldDisplayEdgeEffects()) {
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
    @Override // android.view.View
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
    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        if (this.mContext.getApplicationInfo().targetSdkVersion <= 18) {
            return super.onSaveInstanceState();
        }
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.scrollPosition = this.mScrollY;
        return ss;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.View, android.view.ViewGroup
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("fillViewport", this.mFillViewport);
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class android.widget.ScrollView.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
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

        @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.scrollPosition);
        }

        public String toString() {
            return "ScrollView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " scrollPosition=" + this.scrollPosition + "}";
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int maxOverScrollY2;
        int deltaY2;
        int deltaY3;
        if (this.mIsHwTheme) {
            if (isTopEdge() || isBottomEdge()) {
                deltaY3 = isTouchEvent ? getElasticInterpolation(deltaY, scrollY) : deltaY;
            } else {
                deltaY3 = deltaY;
            }
            int maxOverScrollY3 = getHeight();
            invalidate();
            deltaY2 = deltaY3;
            maxOverScrollY2 = maxOverScrollY3;
        } else {
            maxOverScrollY2 = maxOverScrollY;
            deltaY2 = deltaY;
        }
        return super.overScrollBy(deltaX, deltaY2, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY2, isTouchEvent);
    }

    private int getElasticInterpolation(int delta, int currentPos) {
        if (isBottomEdge()) {
            currentPos -= getScrollRange();
        }
        HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
        if (hwSpringBackHelper != null) {
            return hwSpringBackHelper.getDynamicCurvedRateDelta(getHeight(), delta, currentPos);
        }
        float len = (float) Math.abs(currentPos);
        int newDelta = (int) ((Math.sqrt(((double) (250.0f * ((float) Math.abs(delta)))) + Math.pow((double) len, 2.0d)) - ((double) len)) * ((double) Math.signum((float) delta)));
        return Math.abs(newDelta) > Math.abs(delta) ? delta : newDelta;
    }

    private boolean isTopEdge() {
        return getScrollY() < 0;
    }

    private boolean isBottomEdge() {
        return getScrollRange() <= getScrollY();
    }

    private void computeHwScroll() {
        if (this.mHwSpringBackHelper != null) {
            int oldX = this.mScrollX;
            int oldY = this.mScrollY;
            int newY = this.mHwSpringBackHelper.getCurrentOffset();
            if (oldY != newY) {
                overScrollBy(0, newY - oldY, oldX, oldY, 0, getScrollRange(), 0, this.mOverflingDistance, false);
                onScrollChanged(this.mScrollX, this.mScrollY, oldX, oldY);
            }
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        }
    }

    private void onHwTouchDown() {
        HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
        if (hwSpringBackHelper != null && !hwSpringBackHelper.isFinished()) {
            this.mIsBeingDragged = true;
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
            this.mHwSpringBackHelper.abortAnimation();
            StrictMode.Span span = this.mFlingStrictSpan;
            if (span != null) {
                span.finish();
                this.mFlingStrictSpan = null;
            }
        }
    }

    private void hwSpringBack() {
        HwSpringBackHelper hwSpringBackHelper = this.mHwSpringBackHelper;
        if (hwSpringBackHelper != null) {
            hwSpringBackHelper.springBack(this.mScrollY, 0, getScrollRange());
        }
    }
}
