package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.OverScroller;
import android.widget.ScrollView;
import com.android.internal.R;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto;

public class ResolverDrawerLayout extends ViewGroup {
    private static final String TAG = "ResolverDrawerLayout";
    private int mActivePointerId;
    private int mAlwaysShowHeight;
    private int mCollapseOffset;
    private int mCollapsibleHeight;
    private int mCollapsibleHeightReserved;
    private boolean mDismissLocked;
    private boolean mDismissOnScrollerFinished;
    private float mDragRemainder;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIsDragging;
    private float mLastTouchY;
    private int mMaxCollapsedHeight;
    private int mMaxCollapsedHeightSmall;
    private int mMaxWidth;
    private MetricsLogger mMetricsLogger;
    private final float mMinFlingVelocity;
    private AbsListView mNestedScrollingChild;
    private OnCollapsedChangedListener mOnCollapsedChangedListener;
    private OnDismissedListener mOnDismissedListener;
    private boolean mOpenOnClick;
    private boolean mOpenOnLayout;
    private RunOnDismissedListener mRunOnDismissedListener;
    private Drawable mScrollIndicatorDrawable;
    private final OverScroller mScroller;
    private boolean mShowAtTop;
    private boolean mSmallCollapsed;
    private final Rect mTempRect;
    private int mTopOffset;
    private final ViewTreeObserver.OnTouchModeChangeListener mTouchModeChangeListener;
    private final int mTouchSlop;
    private int mUncollapsibleHeight;
    private final VelocityTracker mVelocityTracker;

    public interface OnCollapsedChangedListener {
        void onCollapsedChanged(boolean z);
    }

    public interface OnDismissedListener {
        void onDismissed();
    }

    public ResolverDrawerLayout(Context context) {
        this(context, null);
    }

    public ResolverDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResolverDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mDragRemainder = 0.0f;
        this.mActivePointerId = -1;
        this.mTempRect = new Rect();
        this.mTouchModeChangeListener = new ViewTreeObserver.OnTouchModeChangeListener() {
            /* class com.android.internal.widget.ResolverDrawerLayout.AnonymousClass1 */

            @Override // android.view.ViewTreeObserver.OnTouchModeChangeListener
            public void onTouchModeChanged(boolean isInTouchMode) {
                if (!isInTouchMode && ResolverDrawerLayout.this.hasFocus()) {
                    ResolverDrawerLayout resolverDrawerLayout = ResolverDrawerLayout.this;
                    if (resolverDrawerLayout.isDescendantClipped(resolverDrawerLayout.getFocusedChild())) {
                        ResolverDrawerLayout.this.smoothScrollTo(0, 0.0f);
                    }
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ResolverDrawerLayout, defStyleAttr, 0);
        this.mMaxWidth = a.getDimensionPixelSize(0, -1);
        this.mMaxCollapsedHeight = a.getDimensionPixelSize(1, 0);
        this.mMaxCollapsedHeightSmall = a.getDimensionPixelSize(2, this.mMaxCollapsedHeight);
        this.mShowAtTop = a.getBoolean(3, false);
        a.recycle();
        this.mScrollIndicatorDrawable = this.mContext.getDrawable(R.drawable.scroll_indicator_material);
        this.mScroller = new OverScroller(context, AnimationUtils.loadInterpolator(context, 17563653));
        this.mVelocityTracker = VelocityTracker.obtain();
        ViewConfiguration vc = ViewConfiguration.get(context);
        this.mTouchSlop = vc.getScaledTouchSlop();
        this.mMinFlingVelocity = (float) vc.getScaledMinimumFlingVelocity();
        setImportantForAccessibility(1);
    }

    public void setSmallCollapsed(boolean smallCollapsed) {
        this.mSmallCollapsed = smallCollapsed;
        requestLayout();
    }

    public boolean isSmallCollapsed() {
        return this.mSmallCollapsed;
    }

    public boolean isCollapsed() {
        return this.mCollapseOffset > 0;
    }

    public void setShowAtTop(boolean showOnTop) {
        this.mShowAtTop = showOnTop;
        invalidate();
        requestLayout();
    }

    public boolean getShowAtTop() {
        return this.mShowAtTop;
    }

    public void setCollapsed(boolean collapsed) {
        if (!isLaidOut()) {
            this.mOpenOnLayout = collapsed;
        } else {
            smoothScrollTo(collapsed ? this.mCollapsibleHeight : 0, 0.0f);
        }
    }

    public void setCollapsibleHeightReserved(int heightPixels) {
        int oldReserved = this.mCollapsibleHeightReserved;
        this.mCollapsibleHeightReserved = heightPixels;
        int dReserved = this.mCollapsibleHeightReserved - oldReserved;
        if (dReserved != 0 && this.mIsDragging) {
            this.mLastTouchY -= (float) dReserved;
        }
        int oldCollapsibleHeight = this.mCollapsibleHeight;
        this.mCollapsibleHeight = Math.max(this.mCollapsibleHeight, getMaxCollapsedHeight());
        if (!updateCollapseOffset(oldCollapsibleHeight, !isDragging())) {
            invalidate();
        }
    }

    public void setDismissLocked(boolean locked) {
        this.mDismissLocked = locked;
    }

    private boolean isMoving() {
        return this.mIsDragging || !this.mScroller.isFinished();
    }

    private boolean isDragging() {
        return this.mIsDragging || getNestedScrollAxes() == 2;
    }

    private boolean updateCollapseOffset(int oldCollapsibleHeight, boolean remainClosed) {
        int i;
        int i2 = 0;
        boolean isCollapsedNew = false;
        if (oldCollapsibleHeight == this.mCollapsibleHeight) {
            return false;
        }
        if (getShowAtTop()) {
            this.mCollapseOffset = 0;
            return false;
        }
        if (isLaidOut()) {
            boolean isCollapsedOld = this.mCollapseOffset != 0;
            if (!remainClosed || oldCollapsibleHeight >= (i = this.mCollapsibleHeight) || this.mCollapseOffset != oldCollapsibleHeight) {
                this.mCollapseOffset = Math.min(this.mCollapseOffset, this.mCollapsibleHeight);
            } else {
                this.mCollapseOffset = i;
            }
            if (this.mCollapseOffset != 0) {
                isCollapsedNew = true;
            }
            if (isCollapsedOld != isCollapsedNew) {
                onCollapsedChanged(isCollapsedNew);
            }
        } else {
            if (!this.mOpenOnLayout) {
                i2 = this.mCollapsibleHeight;
            }
            this.mCollapseOffset = i2;
        }
        return true;
    }

    private int getMaxCollapsedHeight() {
        return (isSmallCollapsed() ? this.mMaxCollapsedHeightSmall : this.mMaxCollapsedHeight) + this.mCollapsibleHeightReserved;
    }

    public void setOnDismissedListener(OnDismissedListener listener) {
        this.mOnDismissedListener = listener;
    }

    private boolean isDismissable() {
        return this.mOnDismissedListener != null && !this.mDismissLocked;
    }

    public void setOnCollapsedChangedListener(OnCollapsedChangedListener listener) {
        this.mOnCollapsedChangedListener = listener;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if (action == 0) {
            this.mVelocityTracker.clear();
        }
        this.mVelocityTracker.addMovement(ev);
        if (action != 0) {
            if (action != 1) {
                if (action == 2) {
                    float x = ev.getX();
                    float y = ev.getY();
                    float dy = y - this.mInitialTouchY;
                    if (Math.abs(dy) > ((float) this.mTouchSlop) && findChildUnder(x, y) != null && (2 & getNestedScrollAxes()) == 0) {
                        this.mActivePointerId = ev.getPointerId(0);
                        this.mIsDragging = true;
                        float f = this.mLastTouchY;
                        int i = this.mTouchSlop;
                        this.mLastTouchY = Math.max(f - ((float) i), Math.min(f + dy, f + ((float) i)));
                    }
                } else if (action != 3) {
                    if (action == 6) {
                        onSecondaryPointerUp(ev);
                    }
                }
            }
            resetTouch();
        } else {
            float x2 = ev.getX();
            float y2 = ev.getY();
            this.mInitialTouchX = x2;
            this.mLastTouchY = y2;
            this.mInitialTouchY = y2;
            this.mOpenOnClick = isListChildUnderClipped(x2, y2) && this.mCollapseOffset > 0;
        }
        if (this.mIsDragging) {
            abortAnimation();
        }
        if (this.mIsDragging || this.mOpenOnClick) {
            return true;
        }
        return false;
    }

    private boolean isNestedChildScrolled() {
        AbsListView absListView = this.mNestedScrollingChild;
        if (absListView == null || absListView.getChildCount() <= 0) {
            return false;
        }
        if (this.mNestedScrollingChild.getFirstVisiblePosition() > 0 || this.mNestedScrollingChild.getChildAt(0).getTop() < 0) {
            return true;
        }
        return false;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent ev) {
        int i;
        int action = ev.getActionMasked();
        this.mVelocityTracker.addMovement(ev);
        boolean handled = false;
        boolean z = true;
        int i2 = 0;
        if (action == 0) {
            float x = ev.getX();
            float y = ev.getY();
            this.mInitialTouchX = x;
            this.mLastTouchY = y;
            this.mInitialTouchY = y;
            this.mActivePointerId = ev.getPointerId(0);
            boolean hitView = findChildUnder(this.mInitialTouchX, this.mInitialTouchY) != null;
            handled = isDismissable() || this.mCollapsibleHeight > 0;
            if (!hitView || !handled) {
                z = false;
            }
            this.mIsDragging = z;
            abortAnimation();
        } else if (action == 1) {
            boolean wasDragging = this.mIsDragging;
            this.mIsDragging = false;
            if (!wasDragging && findChildUnder(this.mInitialTouchX, this.mInitialTouchY) == null && findChildUnder(ev.getX(), ev.getY()) == null && isDismissable()) {
                dispatchOnDismissed();
                resetTouch();
                return true;
            } else if (!this.mOpenOnClick || Math.abs(ev.getX() - this.mInitialTouchX) >= ((float) this.mTouchSlop) || Math.abs(ev.getY() - this.mInitialTouchY) >= ((float) this.mTouchSlop)) {
                this.mVelocityTracker.computeCurrentVelocity(1000);
                float yvel = this.mVelocityTracker.getYVelocity(this.mActivePointerId);
                if (Math.abs(yvel) <= this.mMinFlingVelocity) {
                    int i3 = this.mCollapseOffset;
                    int i4 = this.mCollapsibleHeight;
                    if (i3 >= i4 / 2) {
                        i2 = i4;
                    }
                    smoothScrollTo(i2, 0.0f);
                } else if (getShowAtTop()) {
                    if (!isDismissable() || yvel >= 0.0f) {
                        if (yvel >= 0.0f) {
                            i2 = this.mCollapsibleHeight;
                        }
                        smoothScrollTo(i2, yvel);
                    } else {
                        abortAnimation();
                        dismiss();
                    }
                } else if (!isDismissable() || yvel <= 0.0f || this.mCollapseOffset <= (i = this.mCollapsibleHeight)) {
                    if (isNestedChildScrolled()) {
                        this.mNestedScrollingChild.smoothScrollToPosition(0);
                    }
                    if (yvel >= 0.0f) {
                        i2 = this.mCollapsibleHeight;
                    }
                    smoothScrollTo(i2, yvel);
                } else {
                    smoothScrollTo(i + this.mUncollapsibleHeight, yvel);
                    this.mDismissOnScrollerFinished = true;
                }
                resetTouch();
            } else {
                smoothScrollTo(0, 0.0f);
                return true;
            }
        } else if (action == 2) {
            int index = ev.findPointerIndex(this.mActivePointerId);
            if (index < 0) {
                Log.e(TAG, "Bad pointer id " + this.mActivePointerId + ", resetting");
                index = 0;
                this.mActivePointerId = ev.getPointerId(0);
                this.mInitialTouchX = ev.getX();
                float y2 = ev.getY();
                this.mLastTouchY = y2;
                this.mInitialTouchY = y2;
            }
            float x2 = ev.getX(index);
            float y3 = ev.getY(index);
            if (!this.mIsDragging) {
                float dy = y3 - this.mInitialTouchY;
                if (Math.abs(dy) > ((float) this.mTouchSlop) && findChildUnder(x2, y3) != null) {
                    this.mIsDragging = true;
                    handled = true;
                    float f = this.mLastTouchY;
                    int i5 = this.mTouchSlop;
                    this.mLastTouchY = Math.max(f - ((float) i5), Math.min(f + dy, f + ((float) i5)));
                }
            }
            if (this.mIsDragging) {
                float dy2 = y3 - this.mLastTouchY;
                if (dy2 <= 0.0f || !isNestedChildScrolled()) {
                    performDrag(dy2);
                } else {
                    this.mNestedScrollingChild.smoothScrollBy((int) (-dy2), 0);
                }
            }
            this.mLastTouchY = y3;
        } else if (action == 3) {
            if (this.mIsDragging) {
                int i6 = this.mCollapseOffset;
                int i7 = this.mCollapsibleHeight;
                if (i6 >= i7 / 2) {
                    i2 = i7;
                }
                smoothScrollTo(i2, 0.0f);
            }
            resetTouch();
            return true;
        } else if (action == 5) {
            int pointerIndex = ev.getActionIndex();
            this.mActivePointerId = ev.getPointerId(pointerIndex);
            this.mInitialTouchX = ev.getX(pointerIndex);
            float y4 = ev.getY(pointerIndex);
            this.mLastTouchY = y4;
            this.mInitialTouchY = y4;
        } else if (action == 6) {
            onSecondaryPointerUp(ev);
        }
        return handled;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = ev.getActionIndex();
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mInitialTouchX = ev.getX(newPointerIndex);
            float y = ev.getY(newPointerIndex);
            this.mLastTouchY = y;
            this.mInitialTouchY = y;
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private void resetTouch() {
        this.mActivePointerId = -1;
        this.mIsDragging = false;
        this.mOpenOnClick = false;
        this.mLastTouchY = 0.0f;
        this.mInitialTouchY = 0.0f;
        this.mInitialTouchX = 0.0f;
        this.mVelocityTracker.clear();
    }

    private void dismiss() {
        this.mRunOnDismissedListener = new RunOnDismissedListener();
        post(this.mRunOnDismissedListener);
    }

    @Override // android.view.View
    public void computeScroll() {
        super.computeScroll();
        if (this.mScroller.computeScrollOffset()) {
            boolean keepGoing = !this.mScroller.isFinished();
            performDrag((float) (this.mScroller.getCurrY() - this.mCollapseOffset));
            if (keepGoing) {
                postInvalidateOnAnimation();
            } else if (this.mDismissOnScrollerFinished && this.mOnDismissedListener != null) {
                dismiss();
            }
        }
    }

    private void abortAnimation() {
        this.mScroller.abortAnimation();
        this.mRunOnDismissedListener = null;
        this.mDismissOnScrollerFinished = false;
    }

    private float performDrag(float tempDy) {
        if (getShowAtTop()) {
            return 0.0f;
        }
        int newPos = Math.max(0, Math.min(this.mCollapseOffset + ((int) tempDy), this.mCollapsibleHeight + this.mUncollapsibleHeight));
        int i = this.mCollapseOffset;
        if (newPos == i) {
            return 0.0f;
        }
        int dy = newPos - i;
        this.mDragRemainder += (float) (dy - dy);
        float f = this.mDragRemainder;
        if (f >= 1.0f) {
            this.mDragRemainder = f - 1.0f;
            dy = (int) (((float) dy) + 1.0f);
        } else if (f <= -1.0f) {
            this.mDragRemainder = f + 1.0f;
            dy = (int) (((float) dy) - 1.0f);
        }
        int childCount = getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            View child = getChildAt(i2);
            if (!((LayoutParams) child.getLayoutParams()).ignoreOffset) {
                child.offsetTopAndBottom(dy);
            }
        }
        int i3 = 1;
        boolean isCollapsedOld = this.mCollapseOffset != 0;
        this.mCollapseOffset = newPos;
        this.mTopOffset += dy;
        boolean isCollapsedNew = newPos != 0;
        if (isCollapsedOld != isCollapsedNew) {
            onCollapsedChanged(isCollapsedNew);
            MetricsLogger metricsLogger = getMetricsLogger();
            LogMaker logMaker = new LogMaker((int) MetricsProto.MetricsEvent.ACTION_SHARESHEET_COLLAPSED_CHANGED);
            if (!isCollapsedNew) {
                i3 = 0;
            }
            metricsLogger.write(logMaker.setSubtype(i3));
        }
        onScrollChanged(0, newPos, 0, newPos - dy);
        postInvalidateOnAnimation();
        return (float) dy;
    }

    private void onCollapsedChanged(boolean isCollapsed) {
        notifyViewAccessibilityStateChangedIfNeeded(0);
        if (this.mScrollIndicatorDrawable != null) {
            setWillNotDraw(!isCollapsed);
        }
        OnCollapsedChangedListener onCollapsedChangedListener = this.mOnCollapsedChangedListener;
        if (onCollapsedChangedListener != null) {
            onCollapsedChangedListener.onCollapsedChanged(isCollapsed);
        }
    }

    /* access modifiers changed from: package-private */
    public void dispatchOnDismissed() {
        OnDismissedListener onDismissedListener = this.mOnDismissedListener;
        if (onDismissedListener != null) {
            onDismissedListener.onDismissed();
        }
        RunOnDismissedListener runOnDismissedListener = this.mRunOnDismissedListener;
        if (runOnDismissedListener != null) {
            removeCallbacks(runOnDismissedListener);
            this.mRunOnDismissedListener = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void smoothScrollTo(int yOffset, float velocity) {
        int duration;
        abortAnimation();
        int sy = this.mCollapseOffset;
        int dy = yOffset - sy;
        if (dy != 0) {
            int height = getHeight();
            int halfHeight = height / 2;
            float distance = ((float) halfHeight) + (((float) halfHeight) * distanceInfluenceForSnapDuration(Math.min(1.0f, (((float) Math.abs(dy)) * 1.0f) / ((float) height))));
            float velocity2 = Math.abs(velocity);
            if (velocity2 > 0.0f) {
                duration = Math.round(Math.abs(distance / velocity2) * 1000.0f) * 4;
            } else {
                duration = (int) ((1.0f + (((float) Math.abs(dy)) / ((float) height))) * 100.0f);
            }
            this.mScroller.startScroll(0, sy, 0, dy, Math.min(duration, 300));
            postInvalidateOnAnimation();
        }
    }

    private float distanceInfluenceForSnapDuration(float f) {
        return (float) Math.sin((double) ((float) (((double) (f - 0.5f)) * 0.4712389167638204d)));
    }

    private View findChildUnder(float x, float y) {
        return findChildUnder(this, x, y);
    }

    private static View findChildUnder(ViewGroup parent, float x, float y) {
        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            View child = parent.getChildAt(i);
            if (isChildUnder(child, x, y)) {
                return child;
            }
        }
        return null;
    }

    private View findListChildUnder(float x, float y) {
        View v = findChildUnder(x, y);
        while (v != null) {
            x -= v.getX();
            y -= v.getY();
            if (v instanceof AbsListView) {
                return findChildUnder((ViewGroup) v, x, y);
            }
            v = v instanceof ViewGroup ? findChildUnder((ViewGroup) v, x, y) : null;
        }
        return v;
    }

    private boolean isListChildUnderClipped(float x, float y) {
        View listChild = findListChildUnder(x, y);
        return listChild != null && isDescendantClipped(listChild);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isDescendantClipped(View child) {
        View v;
        this.mTempRect.set(0, 0, child.getWidth(), child.getHeight());
        offsetDescendantRectToMyCoords(child, this.mTempRect);
        if (child.getParent() == this) {
            v = child;
        } else {
            v = child;
            ViewParent p = child.getParent();
            while (p != this) {
                v = (View) p;
                p = v.getParent();
            }
        }
        int clipEdge = getHeight() - getPaddingBottom();
        int childCount = getChildCount();
        for (int i = indexOfChild(v) + 1; i < childCount; i++) {
            View nextChild = getChildAt(i);
            if (nextChild.getVisibility() != 8) {
                clipEdge = Math.min(clipEdge, nextChild.getTop());
            }
        }
        if (this.mTempRect.bottom > clipEdge) {
            return true;
        }
        return false;
    }

    private static boolean isChildUnder(View child, float x, float y) {
        float left = child.getX();
        float top = child.getY();
        return x >= left && y >= top && x < ((float) child.getWidth()) + left && y < ((float) child.getHeight()) + top;
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (!isInTouchMode() && isDescendantClipped(focused)) {
            smoothScrollTo(0, 0.0f);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnTouchModeChangeListener(this.mTouchModeChangeListener);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnTouchModeChangeListener(this.mTouchModeChangeListener);
        abortAnimation();
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if ((nestedScrollAxes & 2) == 0) {
            return false;
        }
        if (!(child instanceof AbsListView)) {
            return true;
        }
        this.mNestedScrollingChild = (AbsListView) child;
        return true;
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);
        if (this.mScroller.isFinished()) {
            int i = this.mCollapseOffset;
            int i2 = this.mCollapsibleHeight;
            if (i < i2 / 2) {
                i2 = 0;
            }
            smoothScrollTo(i2, 0.0f);
        }
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (dyUnconsumed < 0) {
            performDrag((float) (-dyUnconsumed));
        }
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0) {
            consumed[1] = (int) (-performDrag((float) (-dy)));
        }
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (getShowAtTop() || velocityY <= this.mMinFlingVelocity || this.mCollapseOffset == 0) {
            return false;
        }
        smoothScrollTo(0, velocityY);
        return true;
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        int i;
        int i2 = 0;
        if (consumed || Math.abs(velocityY) <= this.mMinFlingVelocity) {
            return false;
        }
        if (getShowAtTop()) {
            if (!isDismissable() || velocityY <= 0.0f) {
                if (velocityY < 0.0f) {
                    i2 = this.mCollapsibleHeight;
                }
                smoothScrollTo(i2, velocityY);
            } else {
                abortAnimation();
                dismiss();
            }
        } else if (!isDismissable() || velocityY >= 0.0f || this.mCollapseOffset <= (i = this.mCollapsibleHeight)) {
            if (velocityY <= 0.0f) {
                i2 = this.mCollapsibleHeight;
            }
            smoothScrollTo(i2, velocityY);
        } else {
            smoothScrollTo(i + this.mUncollapsibleHeight, velocityY);
            this.mDismissOnScrollerFinished = true;
        }
        return true;
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean onNestedPrePerformAccessibilityAction(View target, int action, Bundle args) {
        if (super.onNestedPrePerformAccessibilityAction(target, action, args)) {
            return true;
        }
        if (action != 4096 || this.mCollapseOffset == 0) {
            return false;
        }
        smoothScrollTo(0, 0.0f);
        return true;
    }

    @Override // android.view.View, android.view.ViewGroup
    public CharSequence getAccessibilityClassName() {
        return ScrollView.class.getName();
    }

    @Override // android.view.View, android.view.ViewGroup
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled() && this.mCollapseOffset != 0) {
            info.addAction(4096);
            info.setScrollable(true);
        }
        info.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
    }

    @Override // android.view.View
    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (action == AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS.getId()) {
            return false;
        }
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (action != 4096 || this.mCollapseOffset == 0) {
            return false;
        }
        smoothScrollTo(0, 0.0f);
        return true;
    }

    @Override // android.view.View
    public void onDrawForeground(Canvas canvas) {
        Drawable drawable = this.mScrollIndicatorDrawable;
        if (drawable != null) {
            drawable.draw(canvas);
        }
        super.onDrawForeground(canvas);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        int sourceWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int i8 = this.mMaxWidth;
        if (i8 >= 0) {
            widthSize = Math.min(sourceWidth, i8);
        } else {
            widthSize = sourceWidth;
        }
        int widthSpec = View.MeasureSpec.makeMeasureSpec(widthSize, 1073741824);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(heightSize, 1073741824);
        int childCount = getChildCount();
        int heightUsed = 0;
        int i9 = 0;
        while (true) {
            i = Integer.MIN_VALUE;
            i2 = -1;
            i3 = 8;
            i4 = 0;
            if (i9 >= childCount) {
                break;
            }
            View child = getChildAt(i9);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.alwaysShow && child.getVisibility() != 8) {
                if (lp.maxHeight != -1) {
                    int remainingHeight = heightSize - heightUsed;
                    measureChildWithMargins(child, widthSpec, 0, View.MeasureSpec.makeMeasureSpec(lp.maxHeight, Integer.MIN_VALUE), lp.maxHeight > remainingHeight ? lp.maxHeight - remainingHeight : 0);
                } else {
                    measureChildWithMargins(child, widthSpec, 0, heightSpec, heightUsed);
                }
                heightUsed += child.getMeasuredHeight();
            }
            i9++;
        }
        this.mAlwaysShowHeight = heightUsed;
        int i10 = 0;
        while (i10 < childCount) {
            View child2 = getChildAt(i10);
            LayoutParams lp2 = (LayoutParams) child2.getLayoutParams();
            if (lp2.alwaysShow || child2.getVisibility() == i3) {
                i7 = i4;
                i6 = i3;
                i5 = i2;
            } else {
                if (lp2.maxHeight != i2) {
                    int remainingHeight2 = heightSize - heightUsed;
                    int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(lp2.maxHeight, i);
                    i7 = i4;
                    i6 = i3;
                    i5 = i2;
                    measureChildWithMargins(child2, widthSpec, 0, makeMeasureSpec, lp2.maxHeight > remainingHeight2 ? lp2.maxHeight - remainingHeight2 : i4);
                } else {
                    i7 = i4;
                    i6 = i3;
                    i5 = i2;
                    measureChildWithMargins(child2, widthSpec, 0, heightSpec, heightUsed);
                }
                heightUsed += child2.getMeasuredHeight();
            }
            i10++;
            i4 = i7;
            i3 = i6;
            i2 = i5;
            i = Integer.MIN_VALUE;
        }
        int oldCollapsibleHeight = this.mCollapsibleHeight;
        this.mCollapsibleHeight = Math.max(i4, (heightUsed - this.mAlwaysShowHeight) - getMaxCollapsedHeight());
        this.mUncollapsibleHeight = heightUsed - this.mCollapsibleHeight;
        updateCollapseOffset(oldCollapsibleHeight, !isDragging());
        if (getShowAtTop()) {
            this.mTopOffset = i4;
        } else {
            this.mTopOffset = Math.max(i4, heightSize - heightUsed) + this.mCollapseOffset;
        }
        setMeasuredDimension(sourceWidth, heightSize);
    }

    public int getAlwaysShowHeight() {
        return this.mAlwaysShowHeight;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int width;
        int width2 = getWidth();
        View indicatorHost = null;
        int ypos = this.mTopOffset;
        int leftEdge = getPaddingLeft();
        int rightEdge = width2 - getPaddingRight();
        int childCount = getChildCount();
        int i = 0;
        while (i < childCount) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.hasNestedScrollIndicator) {
                indicatorHost = child;
            }
            if (child.getVisibility() == 8) {
                width = width2;
            } else {
                int top = lp.topMargin + ypos;
                if (lp.ignoreOffset) {
                    top -= this.mCollapseOffset;
                }
                int bottom = child.getMeasuredHeight() + top;
                int childWidth = child.getMeasuredWidth();
                int left = (((rightEdge - leftEdge) - childWidth) / 2) + leftEdge;
                child.layout(left, top, left + childWidth, bottom);
                width = width2;
                ypos = lp.bottomMargin + bottom;
            }
            i++;
            width2 = width;
        }
        if (this.mScrollIndicatorDrawable == null) {
            return;
        }
        if (indicatorHost != null) {
            int left2 = indicatorHost.getLeft();
            int right = indicatorHost.getRight();
            int bottom2 = indicatorHost.getTop();
            this.mScrollIndicatorDrawable.setBounds(left2, bottom2 - this.mScrollIndicatorDrawable.getIntrinsicHeight(), right, bottom2);
            setWillNotDraw(true ^ isCollapsed());
            return;
        }
        this.mScrollIndicatorDrawable = null;
        setWillNotDraw(true);
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        }
        if (p instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.open = this.mCollapsibleHeight > 0 && this.mCollapseOffset == 0;
        return ss;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mOpenOnLayout = ss.open;
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        public boolean alwaysShow;
        public boolean hasNestedScrollIndicator;
        public boolean ignoreOffset;
        public int maxHeight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ResolverDrawerLayout_LayoutParams);
            this.alwaysShow = a.getBoolean(1, false);
            this.ignoreOffset = a.getBoolean(3, false);
            this.hasNestedScrollIndicator = a.getBoolean(2, false);
            this.maxHeight = a.getDimensionPixelSize(4, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(LayoutParams source) {
            super((ViewGroup.MarginLayoutParams) source);
            this.alwaysShow = source.alwaysShow;
            this.ignoreOffset = source.ignoreOffset;
            this.hasNestedScrollIndicator = source.hasNestedScrollIndicator;
            this.maxHeight = source.maxHeight;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class com.android.internal.widget.ResolverDrawerLayout.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean open;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.open = in.readInt() != 0;
        }

        @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.open ? 1 : 0);
        }
    }

    /* access modifiers changed from: private */
    public class RunOnDismissedListener implements Runnable {
        private RunOnDismissedListener() {
        }

        public void run() {
            ResolverDrawerLayout.this.dispatchOnDismissed();
        }
    }

    private MetricsLogger getMetricsLogger() {
        if (this.mMetricsLogger == null) {
            this.mMetricsLogger = new MetricsLogger();
        }
        return this.mMetricsLogger;
    }
}
