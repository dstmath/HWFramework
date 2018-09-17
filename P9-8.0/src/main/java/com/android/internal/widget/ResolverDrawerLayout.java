package com.android.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.OverScroller;
import android.widget.ScrollView;
import com.android.internal.R;

public class ResolverDrawerLayout extends ViewGroup {
    private static final String TAG = "ResolverDrawerLayout";
    private int mActivePointerId;
    private int mCollapseOffset;
    private int mCollapsibleHeight;
    private int mCollapsibleHeightReserved;
    private boolean mDismissLocked;
    private boolean mDismissOnScrollerFinished;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIsDragging;
    private float mLastTouchY;
    private int mMaxCollapsedHeight;
    private int mMaxCollapsedHeightSmall;
    private int mMaxWidth;
    private final float mMinFlingVelocity;
    private OnDismissedListener mOnDismissedListener;
    private boolean mOpenOnClick;
    private boolean mOpenOnLayout;
    private RunOnDismissedListener mRunOnDismissedListener;
    private Drawable mScrollIndicatorDrawable;
    private final OverScroller mScroller;
    private boolean mSmallCollapsed;
    private final Rect mTempRect;
    private int mTopOffset;
    private final OnTouchModeChangeListener mTouchModeChangeListener;
    private final int mTouchSlop;
    private int mUncollapsibleHeight;
    private final VelocityTracker mVelocityTracker;

    public interface OnDismissedListener {
        void onDismissed();
    }

    public static class LayoutParams extends MarginLayoutParams {
        public boolean alwaysShow;
        public boolean hasNestedScrollIndicator;
        public boolean ignoreOffset;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ResolverDrawerLayout_LayoutParams);
            this.alwaysShow = a.getBoolean(1, false);
            this.ignoreOffset = a.getBoolean(3, false);
            this.hasNestedScrollIndicator = a.getBoolean(2, false);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(LayoutParams source) {
            super((MarginLayoutParams) source);
            this.alwaysShow = source.alwaysShow;
            this.ignoreOffset = source.ignoreOffset;
            this.hasNestedScrollIndicator = source.hasNestedScrollIndicator;
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    private class RunOnDismissedListener implements Runnable {
        /* synthetic */ RunOnDismissedListener(ResolverDrawerLayout this$0, RunOnDismissedListener -this1) {
            this();
        }

        private RunOnDismissedListener() {
        }

        public void run() {
            ResolverDrawerLayout.this.dispatchOnDismissed();
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in, null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean open;

        /* synthetic */ SavedState(Parcel in, SavedState -this1) {
            this(in);
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            boolean z = false;
            super(in);
            if (in.readInt() != 0) {
                z = true;
            }
            this.open = z;
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.open ? 1 : 0);
        }
    }

    public ResolverDrawerLayout(Context context) {
        this(context, null);
    }

    public ResolverDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResolverDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mActivePointerId = -1;
        this.mTempRect = new Rect();
        this.mTouchModeChangeListener = new OnTouchModeChangeListener() {
            public void onTouchModeChanged(boolean isInTouchMode) {
                if (!isInTouchMode && ResolverDrawerLayout.this.hasFocus() && ResolverDrawerLayout.this.isDescendantClipped(ResolverDrawerLayout.this.getFocusedChild())) {
                    ResolverDrawerLayout.this.smoothScrollTo(0, 0.0f);
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ResolverDrawerLayout, defStyleAttr, 0);
        this.mMaxWidth = a.getDimensionPixelSize(0, -1);
        this.mMaxCollapsedHeight = a.getDimensionPixelSize(1, 0);
        this.mMaxCollapsedHeightSmall = a.getDimensionPixelSize(2, this.mMaxCollapsedHeight);
        a.recycle();
        this.mScrollIndicatorDrawable = this.mContext.getDrawable(R.drawable.scroll_indicator_material);
        this.mScroller = new OverScroller(context, AnimationUtils.loadInterpolator(context, R.interpolator.decelerate_quint));
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

    public void setCollapsed(boolean collapsed) {
        if (isLaidOut()) {
            smoothScrollTo(collapsed ? this.mCollapsibleHeight : 0, 0.0f);
        } else {
            this.mOpenOnLayout = collapsed;
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
        if (!updateCollapseOffset(oldCollapsibleHeight, isDragging() ^ 1)) {
            invalidate();
        }
    }

    public void setDismissLocked(boolean locked) {
        this.mDismissLocked = locked;
    }

    private boolean isMoving() {
        return !this.mIsDragging ? this.mScroller.isFinished() ^ 1 : true;
    }

    private boolean isDragging() {
        return this.mIsDragging || getNestedScrollAxes() == 2;
    }

    private boolean updateCollapseOffset(int oldCollapsibleHeight, boolean remainClosed) {
        int i = 0;
        if (oldCollapsibleHeight == this.mCollapsibleHeight) {
            return false;
        }
        if (isLaidOut()) {
            boolean isCollapsedOld = this.mCollapseOffset != 0;
            if (remainClosed && oldCollapsibleHeight < this.mCollapsibleHeight && this.mCollapseOffset == oldCollapsibleHeight) {
                this.mCollapseOffset = this.mCollapsibleHeight;
            } else {
                this.mCollapseOffset = Math.min(this.mCollapseOffset, this.mCollapsibleHeight);
            }
            boolean isCollapsedNew = this.mCollapseOffset != 0;
            if (isCollapsedOld != isCollapsedNew) {
                onCollapsedChanged(isCollapsedNew);
            }
        } else {
            if (!this.mOpenOnLayout) {
                i = this.mCollapsibleHeight;
            }
            this.mCollapseOffset = i;
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
        return this.mOnDismissedListener != null ? this.mDismissLocked ^ 1 : false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        int action = ev.getActionMasked();
        if (action == 0) {
            this.mVelocityTracker.clear();
        }
        this.mVelocityTracker.addMovement(ev);
        float x;
        float y;
        switch (action) {
            case 0:
                x = ev.getX();
                y = ev.getY();
                this.mInitialTouchX = x;
                this.mLastTouchY = y;
                this.mInitialTouchY = y;
                if (isListChildUnderClipped(x, y) && this.mCollapseOffset > 0) {
                    z = true;
                }
                this.mOpenOnClick = z;
                break;
            case 1:
            case 3:
                resetTouch();
                break;
            case 2:
                x = ev.getX();
                y = ev.getY();
                float dy = y - this.mInitialTouchY;
                if (Math.abs(dy) > ((float) this.mTouchSlop) && findChildUnder(x, y) != null && (getNestedScrollAxes() & 2) == 0) {
                    this.mActivePointerId = ev.getPointerId(0);
                    this.mIsDragging = true;
                    this.mLastTouchY = Math.max(this.mLastTouchY - ((float) this.mTouchSlop), Math.min(this.mLastTouchY + dy, this.mLastTouchY + ((float) this.mTouchSlop)));
                    break;
                }
            case 6:
                onSecondaryPointerUp(ev);
                break;
        }
        if (this.mIsDragging) {
            abortAnimation();
        }
        if (this.mIsDragging) {
            return true;
        }
        return this.mOpenOnClick;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        this.mVelocityTracker.addMovement(ev);
        boolean handled = false;
        float x;
        float y;
        float y2;
        switch (action) {
            case 0:
                x = ev.getX();
                y = ev.getY();
                this.mInitialTouchX = x;
                this.mLastTouchY = y;
                this.mInitialTouchY = y;
                this.mActivePointerId = ev.getPointerId(0);
                boolean hitView = findChildUnder(this.mInitialTouchX, this.mInitialTouchY) != null;
                handled = isDismissable() || this.mCollapsibleHeight > 0;
                this.mIsDragging = hitView ? handled : false;
                abortAnimation();
                break;
            case 1:
                boolean wasDragging = this.mIsDragging;
                this.mIsDragging = false;
                if (!wasDragging) {
                    if (findChildUnder(this.mInitialTouchX, this.mInitialTouchY) == null) {
                        if (findChildUnder(ev.getX(), ev.getY()) == null && isDismissable()) {
                            dispatchOnDismissed();
                            resetTouch();
                            return true;
                        }
                    }
                }
                if (!this.mOpenOnClick || Math.abs(ev.getX() - this.mInitialTouchX) >= ((float) this.mTouchSlop) || Math.abs(ev.getY() - this.mInitialTouchY) >= ((float) this.mTouchSlop)) {
                    this.mVelocityTracker.computeCurrentVelocity(1000);
                    float yvel = this.mVelocityTracker.getYVelocity(this.mActivePointerId);
                    if (Math.abs(yvel) <= this.mMinFlingVelocity) {
                        smoothScrollTo(this.mCollapseOffset < this.mCollapsibleHeight / 2 ? 0 : this.mCollapsibleHeight, 0.0f);
                    } else if (!isDismissable() || yvel <= 0.0f || this.mCollapseOffset <= this.mCollapsibleHeight) {
                        smoothScrollTo(yvel < 0.0f ? 0 : this.mCollapsibleHeight, yvel);
                    } else {
                        smoothScrollTo(this.mCollapsibleHeight + this.mUncollapsibleHeight, yvel);
                        this.mDismissOnScrollerFinished = true;
                    }
                    resetTouch();
                    break;
                }
                smoothScrollTo(0, 0.0f);
                return true;
                break;
            case 2:
                int index = ev.findPointerIndex(this.mActivePointerId);
                if (index < 0) {
                    Log.e(TAG, "Bad pointer id " + this.mActivePointerId + ", resetting");
                    index = 0;
                    this.mActivePointerId = ev.getPointerId(0);
                    this.mInitialTouchX = ev.getX();
                    y2 = ev.getY();
                    this.mLastTouchY = y2;
                    this.mInitialTouchY = y2;
                }
                x = ev.getX(index);
                y = ev.getY(index);
                if (!this.mIsDragging) {
                    float dy = y - this.mInitialTouchY;
                    if (Math.abs(dy) > ((float) this.mTouchSlop) && findChildUnder(x, y) != null) {
                        this.mIsDragging = true;
                        handled = true;
                        this.mLastTouchY = Math.max(this.mLastTouchY - ((float) this.mTouchSlop), Math.min(this.mLastTouchY + dy, this.mLastTouchY + ((float) this.mTouchSlop)));
                    }
                }
                if (this.mIsDragging) {
                    performDrag(y - this.mLastTouchY);
                }
                this.mLastTouchY = y;
                break;
            case 3:
                if (this.mIsDragging) {
                    int i;
                    if (this.mCollapseOffset < this.mCollapsibleHeight / 2) {
                        i = 0;
                    } else {
                        i = this.mCollapsibleHeight;
                    }
                    smoothScrollTo(i, 0.0f);
                }
                resetTouch();
                return true;
            case 5:
                int pointerIndex = ev.getActionIndex();
                this.mActivePointerId = ev.getPointerId(pointerIndex);
                this.mInitialTouchX = ev.getX(pointerIndex);
                y2 = ev.getY(pointerIndex);
                this.mLastTouchY = y2;
                this.mInitialTouchY = y2;
                break;
            case 6:
                onSecondaryPointerUp(ev);
                break;
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

    public void computeScroll() {
        super.-wrap15();
        if (this.mScroller.computeScrollOffset()) {
            boolean keepGoing = this.mScroller.isFinished() ^ 1;
            performDrag((float) (this.mScroller.getCurrY() - this.mCollapseOffset));
            if (keepGoing) {
                postInvalidateOnAnimation();
            } else if (this.mDismissOnScrollerFinished && this.mOnDismissedListener != null) {
                this.mRunOnDismissedListener = new RunOnDismissedListener(this, null);
                post(this.mRunOnDismissedListener);
            }
        }
    }

    private void abortAnimation() {
        this.mScroller.abortAnimation();
        this.mRunOnDismissedListener = null;
        this.mDismissOnScrollerFinished = false;
    }

    private float performDrag(float tempDy) {
        int newPos = Math.max(0, Math.min(this.mCollapseOffset + ((int) tempDy), this.mCollapsibleHeight + this.mUncollapsibleHeight));
        if (newPos == this.mCollapseOffset) {
            return 0.0f;
        }
        int dy = newPos - this.mCollapseOffset;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!((LayoutParams) child.getLayoutParams()).ignoreOffset) {
                child.offsetTopAndBottom(dy);
            }
        }
        boolean isCollapsedOld = this.mCollapseOffset != 0;
        this.mCollapseOffset = newPos;
        this.mTopOffset += dy;
        boolean isCollapsedNew = newPos != 0;
        if (isCollapsedOld != isCollapsedNew) {
            onCollapsedChanged(isCollapsedNew);
        }
        postInvalidateOnAnimation();
        return (float) dy;
    }

    private void onCollapsedChanged(boolean isCollapsed) {
        notifyViewAccessibilityStateChangedIfNeeded(0);
        if (this.mScrollIndicatorDrawable != null) {
            setWillNotDraw(isCollapsed ^ 1);
        }
    }

    void dispatchOnDismissed() {
        if (this.mOnDismissedListener != null) {
            this.mOnDismissedListener.onDismissed();
        }
        if (this.mRunOnDismissedListener != null) {
            removeCallbacks(this.mRunOnDismissedListener);
            this.mRunOnDismissedListener = null;
        }
    }

    private void smoothScrollTo(int yOffset, float velocity) {
        abortAnimation();
        int sy = this.mCollapseOffset;
        int dy = yOffset - sy;
        if (dy != 0) {
            int duration;
            int height = getHeight();
            int halfHeight = height / 2;
            float distance = ((float) halfHeight) + (((float) halfHeight) * distanceInfluenceForSnapDuration(Math.min(1.0f, (((float) Math.abs(dy)) * 1.0f) / ((float) height))));
            velocity = Math.abs(velocity);
            if (velocity > 0.0f) {
                duration = Math.round(Math.abs(distance / velocity) * 1000.0f) * 4;
            } else {
                duration = (int) (((((float) Math.abs(dy)) / ((float) height)) + 1.0f) * 100.0f);
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
        return listChild != null ? isDescendantClipped(listChild) : false;
    }

    private boolean isDescendantClipped(View child) {
        View directChild;
        this.mTempRect.set(0, 0, child.getWidth(), child.getHeight());
        offsetDescendantRectToMyCoords(child, this.mTempRect);
        if (child.getParent() == this) {
            directChild = child;
        } else {
            View v = child;
            View p = child.getParent();
            while (p != this) {
                v = p;
                p = v.getParent();
            }
            directChild = v;
        }
        int clipEdge = getHeight() - getPaddingBottom();
        int childCount = getChildCount();
        for (int i = indexOfChild(directChild) + 1; i < childCount; i++) {
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
        float right = left + ((float) child.getWidth());
        float bottom = top + ((float) child.getHeight());
        if (x < left || y < top || x >= right || y >= bottom) {
            return false;
        }
        return true;
    }

    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (!isInTouchMode() && isDescendantClipped(focused)) {
            smoothScrollTo(0, 0.0f);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnTouchModeChangeListener(this.mTouchModeChangeListener);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnTouchModeChangeListener(this.mTouchModeChangeListener);
        abortAnimation();
    }

    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & 2) != 0;
    }

    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
    }

    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);
        if (this.mScroller.isFinished()) {
            smoothScrollTo(this.mCollapseOffset < this.mCollapsibleHeight / 2 ? 0 : this.mCollapsibleHeight, 0.0f);
        }
    }

    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (dyUnconsumed < 0) {
            performDrag((float) (-dyUnconsumed));
        }
    }

    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0) {
            consumed[1] = (int) (-performDrag((float) (-dy)));
        }
    }

    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (velocityY <= this.mMinFlingVelocity || this.mCollapseOffset == 0) {
            return false;
        }
        smoothScrollTo(0, velocityY);
        return true;
    }

    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        int i = 0;
        if (consumed || Math.abs(velocityY) <= this.mMinFlingVelocity) {
            return false;
        }
        if (!isDismissable() || velocityY >= 0.0f || this.mCollapseOffset <= this.mCollapsibleHeight) {
            if (velocityY <= 0.0f) {
                i = this.mCollapsibleHeight;
            }
            smoothScrollTo(i, velocityY);
        } else {
            smoothScrollTo(this.mCollapsibleHeight + this.mUncollapsibleHeight, velocityY);
            this.mDismissOnScrollerFinished = true;
        }
        return true;
    }

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

    public CharSequence getAccessibilityClassName() {
        return ScrollView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled() && this.mCollapseOffset != 0) {
            info.addAction(4096);
            info.setScrollable(true);
        }
        info.removeAction(AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (action == AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS.getId()) {
            return false;
        }
        if (super.-wrap10(action, arguments)) {
            return true;
        }
        if (action != 4096 || this.mCollapseOffset == 0) {
            return false;
        }
        smoothScrollTo(0, 0.0f);
        return true;
    }

    public void onDrawForeground(Canvas canvas) {
        if (this.mScrollIndicatorDrawable != null) {
            this.mScrollIndicatorDrawable.draw(canvas);
        }
        super.onDrawForeground(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        View child;
        int sourceWidth = MeasureSpec.getSize(widthMeasureSpec);
        int widthSize = sourceWidth;
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (this.mMaxWidth >= 0) {
            widthSize = Math.min(sourceWidth, this.mMaxWidth);
        }
        int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, 1073741824);
        int heightSpec = MeasureSpec.makeMeasureSpec(heightSize, 1073741824);
        int widthPadding = getPaddingLeft() + getPaddingRight();
        int heightUsed = getPaddingTop() + getPaddingBottom();
        int childCount = getChildCount();
        for (i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (((LayoutParams) child.getLayoutParams()).alwaysShow && child.getVisibility() != 8) {
                measureChildWithMargins(child, widthSpec, widthPadding, heightSpec, heightUsed);
                heightUsed += child.getMeasuredHeight();
            }
        }
        int alwaysShowHeight = heightUsed;
        for (i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (!(((LayoutParams) child.getLayoutParams()).alwaysShow || child.getVisibility() == 8)) {
                measureChildWithMargins(child, widthSpec, widthPadding, heightSpec, heightUsed);
                heightUsed += child.getMeasuredHeight();
            }
        }
        int oldCollapsibleHeight = this.mCollapsibleHeight;
        this.mCollapsibleHeight = Math.max(0, (heightUsed - alwaysShowHeight) - getMaxCollapsedHeight());
        this.mUncollapsibleHeight = heightUsed - this.mCollapsibleHeight;
        updateCollapseOffset(oldCollapsibleHeight, isDragging() ^ 1);
        this.mTopOffset = Math.max(0, heightSize - heightUsed) + this.mCollapseOffset;
        -wrap3(sourceWidth, heightSize);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int bottom;
        int left;
        int width = getWidth();
        View indicatorHost = null;
        int ypos = this.mTopOffset;
        int leftEdge = getPaddingLeft();
        int rightEdge = width - getPaddingRight();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.hasNestedScrollIndicator) {
                indicatorHost = child;
            }
            if (child.getVisibility() != 8) {
                int top = ypos + lp.topMargin;
                if (lp.ignoreOffset) {
                    top -= this.mCollapseOffset;
                }
                bottom = top + child.getMeasuredHeight();
                int childWidth = child.getMeasuredWidth();
                left = leftEdge + (((rightEdge - leftEdge) - childWidth) / 2);
                child.layout(left, top, left + childWidth, bottom);
                ypos = bottom + lp.bottomMargin;
            }
        }
        if (this.mScrollIndicatorDrawable == null) {
            return;
        }
        if (indicatorHost != null) {
            left = indicatorHost.getLeft();
            int right = indicatorHost.getRight();
            bottom = indicatorHost.getTop();
            this.mScrollIndicatorDrawable.setBounds(left, bottom - this.mScrollIndicatorDrawable.getIntrinsicHeight(), right, bottom);
            setWillNotDraw(isCollapsed() ^ 1);
            return;
        }
        this.mScrollIndicatorDrawable = null;
        setWillNotDraw(true);
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        }
        if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -2);
    }

    protected Parcelable onSaveInstanceState() {
        boolean z = false;
        SavedState ss = new SavedState(super.-wrap0());
        if (this.mCollapsibleHeight > 0 && this.mCollapseOffset == 0) {
            z = true;
        }
        ss.open = z;
        return ss;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.-wrap2(ss.getSuperState());
        this.mOpenOnLayout = ss.open;
    }
}
