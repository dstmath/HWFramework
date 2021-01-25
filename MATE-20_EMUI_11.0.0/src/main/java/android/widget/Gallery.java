package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import com.android.internal.R;

@Deprecated
public class Gallery extends AbsSpinner implements GestureDetector.OnGestureListener {
    private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;
    private static final String TAG = "Gallery";
    private static final boolean localLOGV = false;
    private int mAnimationDuration;
    private AdapterView.AdapterContextMenuInfo mContextMenuInfo;
    private Runnable mDisableSuppressSelectionChangedRunnable;
    @UnsupportedAppUsage
    private int mDownTouchPosition;
    @UnsupportedAppUsage
    private View mDownTouchView;
    @UnsupportedAppUsage
    private FlingRunnable mFlingRunnable;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private GestureDetector mGestureDetector;
    private int mGravity;
    private boolean mIsFirstScroll;
    private boolean mIsRtl;
    private int mLeftMost;
    private boolean mReceivedInvokeKeyDown;
    private int mRightMost;
    private int mSelectedCenterOffset;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private View mSelectedChild;
    private boolean mShouldCallbackDuringFling;
    private boolean mShouldCallbackOnUnselectedItemClick;
    private boolean mShouldStopFling;
    @UnsupportedAppUsage
    private int mSpacing;
    private boolean mSuppressSelectionChanged;
    private float mUnselectedAlpha;

    public Gallery(Context context) {
        this(context, null);
    }

    public Gallery(Context context, AttributeSet attrs) {
        this(context, attrs, 16842864);
    }

    public Gallery(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Gallery(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSpacing = 0;
        this.mAnimationDuration = 400;
        this.mFlingRunnable = new FlingRunnable();
        this.mDisableSuppressSelectionChangedRunnable = new Runnable() {
            /* class android.widget.Gallery.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                Gallery.this.mSuppressSelectionChanged = false;
                Gallery.this.selectionChanged();
            }
        };
        this.mShouldCallbackDuringFling = true;
        this.mShouldCallbackOnUnselectedItemClick = true;
        this.mIsRtl = true;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gallery, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.Gallery, attrs, a, defStyleAttr, defStyleRes);
        int index = a.getInt(0, -1);
        if (index >= 0) {
            setGravity(index);
        }
        int animationDuration = a.getInt(1, -1);
        if (animationDuration > 0) {
            setAnimationDuration(animationDuration);
        }
        setSpacing(a.getDimensionPixelOffset(2, 0));
        setUnselectedAlpha(a.getFloat(3, 0.5f));
        a.recycle();
        this.mGroupFlags |= 1024;
        this.mGroupFlags |= 2048;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(getContext(), this);
            this.mGestureDetector.setIsLongpressEnabled(true);
        }
    }

    public void setCallbackDuringFling(boolean shouldCallback) {
        this.mShouldCallbackDuringFling = shouldCallback;
    }

    public void setCallbackOnUnselectedItemClick(boolean shouldCallback) {
        this.mShouldCallbackOnUnselectedItemClick = shouldCallback;
    }

    public void setAnimationDuration(int animationDurationMillis) {
        this.mAnimationDuration = animationDurationMillis;
    }

    public void setSpacing(int spacing) {
        this.mSpacing = spacing;
    }

    public void setUnselectedAlpha(float unselectedAlpha) {
        this.mUnselectedAlpha = unselectedAlpha;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean getChildStaticTransformation(View child, Transformation t) {
        t.clear();
        t.setAlpha(child == this.mSelectedChild ? 1.0f : this.mUnselectedAlpha);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int computeHorizontalScrollExtent() {
        return 1;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int computeHorizontalScrollOffset() {
        return this.mSelectedPosition;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public int computeHorizontalScrollRange() {
        return this.mItemCount;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsSpinner, android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        layout(0, false);
        this.mInLayout = false;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsSpinner
    public int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void trackMotionScroll(int deltaX) {
        if (getChildCount() != 0) {
            boolean toLeft = deltaX < 0;
            int limitedDeltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
            if (limitedDeltaX != deltaX) {
                this.mFlingRunnable.endFling(false);
                onFinishedMovement();
            }
            offsetChildrenLeftAndRight(limitedDeltaX);
            detachOffScreenChildren(toLeft);
            if (toLeft) {
                fillToGalleryRight();
            } else {
                fillToGalleryLeft();
            }
            this.mRecycler.clear();
            setSelectionToCenterChild();
            View selChild = this.mSelectedChild;
            if (selChild != null) {
                this.mSelectedCenterOffset = (selChild.getLeft() + (selChild.getWidth() / 2)) - (getWidth() / 2);
            }
            onScrollChanged(0, 0, 0, 0);
            invalidate();
        }
    }

    /* access modifiers changed from: package-private */
    public int getLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
        View extremeChild = getChildAt((motionToLeft != this.mIsRtl ? this.mItemCount - 1 : 0) - this.mFirstPosition);
        if (extremeChild == null) {
            return deltaX;
        }
        int extremeChildCenter = getCenterOfView(extremeChild);
        int galleryCenter = getCenterOfGallery();
        if (motionToLeft) {
            if (extremeChildCenter <= galleryCenter) {
                return 0;
            }
        } else if (extremeChildCenter >= galleryCenter) {
            return 0;
        }
        int centerDifference = galleryCenter - extremeChildCenter;
        if (motionToLeft) {
            return Math.max(centerDifference, deltaX);
        }
        return Math.min(centerDifference, deltaX);
    }

    private void offsetChildrenLeftAndRight(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetLeftAndRight(offset);
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int getCenterOfGallery() {
        return (((getWidth() - this.mPaddingLeft) - this.mPaddingRight) / 2) + this.mPaddingLeft;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private static int getCenterOfView(View view) {
        return view.getLeft() + (view.getWidth() / 2);
    }

    private void detachOffScreenChildren(boolean toLeft) {
        int numChildren = getChildCount();
        int firstPosition = this.mFirstPosition;
        int start = 0;
        int count = 0;
        if (toLeft) {
            int galleryLeft = this.mPaddingLeft;
            for (int i = 0; i < numChildren; i++) {
                int n = this.mIsRtl ? (numChildren - 1) - i : i;
                View child = getChildAt(n);
                if (child.getRight() >= galleryLeft) {
                    break;
                }
                start = n;
                count++;
                this.mRecycler.put(firstPosition + n, child);
            }
            if (!this.mIsRtl) {
                start = 0;
            }
        } else {
            int galleryRight = getWidth() - this.mPaddingRight;
            for (int i2 = numChildren - 1; i2 >= 0; i2--) {
                int n2 = this.mIsRtl ? (numChildren - 1) - i2 : i2;
                View child2 = getChildAt(n2);
                if (child2.getLeft() <= galleryRight) {
                    break;
                }
                start = n2;
                count++;
                this.mRecycler.put(firstPosition + n2, child2);
            }
            if (this.mIsRtl) {
                start = 0;
            }
        }
        detachViewsFromParent(start, count);
        if (toLeft != this.mIsRtl) {
            this.mFirstPosition += count;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void scrollIntoSlots() {
        View view;
        if (getChildCount() != 0 && (view = this.mSelectedChild) != null) {
            int scrollAmount = getCenterOfGallery() - getCenterOfView(view);
            if (scrollAmount != 0) {
                this.mFlingRunnable.startUsingDistance(scrollAmount);
            } else {
                onFinishedMovement();
            }
        }
    }

    private void onFinishedMovement() {
        if (this.mSuppressSelectionChanged) {
            this.mSuppressSelectionChanged = false;
            super.selectionChanged();
        }
        this.mSelectedCenterOffset = 0;
        invalidate();
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AdapterView
    public void selectionChanged() {
        if (!this.mSuppressSelectionChanged) {
            super.selectionChanged();
        }
    }

    private void setSelectionToCenterChild() {
        View selView = this.mSelectedChild;
        if (this.mSelectedChild != null) {
            int galleryCenter = getCenterOfGallery();
            if (selView.getLeft() > galleryCenter || selView.getRight() < galleryCenter) {
                int closestEdgeDistance = Integer.MAX_VALUE;
                int newSelectedChildIndex = 0;
                int i = getChildCount() - 1;
                while (true) {
                    if (i < 0) {
                        break;
                    }
                    View child = getChildAt(i);
                    if (child.getLeft() <= galleryCenter && child.getRight() >= galleryCenter) {
                        newSelectedChildIndex = i;
                        break;
                    }
                    int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryCenter), Math.abs(child.getRight() - galleryCenter));
                    if (childClosestEdgeDistance < closestEdgeDistance) {
                        closestEdgeDistance = childClosestEdgeDistance;
                        newSelectedChildIndex = i;
                    }
                    i--;
                }
                int newPos = this.mFirstPosition + newSelectedChildIndex;
                if (newPos != this.mSelectedPosition) {
                    setSelectedPositionInt(newPos);
                    setNextSelectedPositionInt(newPos);
                    checkSelectionChanged();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsSpinner
    public void layout(int delta, boolean animate) {
        this.mIsRtl = isLayoutRtl();
        int childrenLeft = this.mSpinnerPadding.left;
        int childrenWidth = ((this.mRight - this.mLeft) - this.mSpinnerPadding.left) - this.mSpinnerPadding.right;
        if (this.mDataChanged) {
            handleDataChanged();
        }
        if (this.mItemCount == 0) {
            resetList();
            return;
        }
        if (this.mNextSelectedPosition >= 0) {
            setSelectedPositionInt(this.mNextSelectedPosition);
        }
        recycleAllViews();
        detachAllViewsFromParent();
        this.mRightMost = 0;
        this.mLeftMost = 0;
        this.mFirstPosition = this.mSelectedPosition;
        View sel = makeAndAddView(this.mSelectedPosition, 0, 0, true);
        sel.offsetLeftAndRight((((childrenWidth / 2) + childrenLeft) - (sel.getWidth() / 2)) + this.mSelectedCenterOffset);
        fillToGalleryRight();
        fillToGalleryLeft();
        this.mRecycler.clear();
        invalidate();
        checkSelectionChanged();
        this.mDataChanged = false;
        this.mNeedSync = false;
        setNextSelectedPositionInt(this.mSelectedPosition);
        updateSelectedItemMetadata();
    }

    @UnsupportedAppUsage
    private void fillToGalleryLeft() {
        if (this.mIsRtl) {
            fillToGalleryLeftRtl();
        } else {
            fillToGalleryLeftLtr();
        }
    }

    private void fillToGalleryLeftRtl() {
        int curPosition;
        int curPosition2;
        int itemSpacing = this.mSpacing;
        int galleryLeft = this.mPaddingLeft;
        int numChildren = getChildCount();
        int i = this.mItemCount;
        View prevIterationView = getChildAt(numChildren - 1);
        if (prevIterationView != null) {
            curPosition = this.mFirstPosition + numChildren;
            curPosition2 = prevIterationView.getLeft() - itemSpacing;
        } else {
            int i2 = this.mItemCount - 1;
            curPosition = i2;
            this.mFirstPosition = i2;
            curPosition2 = (this.mRight - this.mLeft) - this.mPaddingRight;
            this.mShouldStopFling = true;
        }
        while (curPosition2 > galleryLeft && curPosition < this.mItemCount) {
            curPosition2 = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curPosition2, false).getLeft() - itemSpacing;
            curPosition++;
        }
    }

    private void fillToGalleryLeftLtr() {
        int curPosition;
        int curRightEdge;
        int itemSpacing = this.mSpacing;
        int galleryLeft = this.mPaddingLeft;
        View prevIterationView = getChildAt(0);
        if (prevIterationView != null) {
            curPosition = this.mFirstPosition - 1;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            curPosition = 0;
            this.mShouldStopFling = true;
            curRightEdge = (this.mRight - this.mLeft) - this.mPaddingRight;
        }
        while (curRightEdge > galleryLeft && curPosition >= 0) {
            View prevIterationView2 = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curRightEdge, false);
            this.mFirstPosition = curPosition;
            curRightEdge = prevIterationView2.getLeft() - itemSpacing;
            curPosition--;
        }
    }

    @UnsupportedAppUsage
    private void fillToGalleryRight() {
        if (this.mIsRtl) {
            fillToGalleryRightRtl();
        } else {
            fillToGalleryRightLtr();
        }
    }

    private void fillToGalleryRightRtl() {
        int curLeftEdge;
        int curPosition;
        int itemSpacing = this.mSpacing;
        int galleryRight = (this.mRight - this.mLeft) - this.mPaddingRight;
        View prevIterationView = getChildAt(0);
        if (prevIterationView != null) {
            curPosition = this.mFirstPosition - 1;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            curPosition = 0;
            curLeftEdge = this.mPaddingLeft;
            this.mShouldStopFling = true;
        }
        while (curLeftEdge < galleryRight && curPosition >= 0) {
            View prevIterationView2 = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curLeftEdge, true);
            this.mFirstPosition = curPosition;
            curLeftEdge = prevIterationView2.getRight() + itemSpacing;
            curPosition--;
        }
    }

    private void fillToGalleryRightLtr() {
        int curPosition;
        int curPosition2;
        int itemSpacing = this.mSpacing;
        int galleryRight = (this.mRight - this.mLeft) - this.mPaddingRight;
        int numChildren = getChildCount();
        int numItems = this.mItemCount;
        View prevIterationView = getChildAt(numChildren - 1);
        if (prevIterationView != null) {
            int right = prevIterationView.getRight() + itemSpacing;
            curPosition = this.mFirstPosition + numChildren;
            curPosition2 = right;
        } else {
            int i = this.mItemCount - 1;
            curPosition = i;
            this.mFirstPosition = i;
            curPosition2 = this.mPaddingLeft;
            this.mShouldStopFling = true;
        }
        while (curPosition2 < galleryRight && curPosition < numItems) {
            curPosition2 = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curPosition2, true).getRight() + itemSpacing;
            curPosition++;
        }
    }

    @UnsupportedAppUsage
    private View makeAndAddView(int position, int offset, int x, boolean fromLeft) {
        View child;
        if (this.mDataChanged || (child = this.mRecycler.get(position)) == null) {
            View child2 = this.mAdapter.getView(position, null, this);
            setUpChild(child2, offset, x, fromLeft);
            return child2;
        }
        int childLeft = child.getLeft();
        this.mRightMost = Math.max(this.mRightMost, child.getMeasuredWidth() + childLeft);
        this.mLeftMost = Math.min(this.mLeftMost, childLeft);
        setUpChild(child, offset, x, fromLeft);
        return child;
    }

    private void setUpChild(View child, int offset, int x, boolean fromLeft) {
        int childRight;
        int childLeft;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = (LayoutParams) generateDefaultLayoutParams();
        }
        boolean z = false;
        addViewInLayout(child, fromLeft != this.mIsRtl ? -1 : 0, lp, true);
        if (offset == 0) {
            z = true;
        }
        child.setSelected(z);
        child.measure(ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mSpinnerPadding.left + this.mSpinnerPadding.right, lp.width), ViewGroup.getChildMeasureSpec(this.mHeightMeasureSpec, this.mSpinnerPadding.top + this.mSpinnerPadding.bottom, lp.height));
        int childTop = calculateTop(child, true);
        int childBottom = child.getMeasuredHeight() + childTop;
        int width = child.getMeasuredWidth();
        if (fromLeft) {
            childLeft = x;
            childRight = childLeft + width;
        } else {
            childLeft = x - width;
            childRight = x;
        }
        child.layout(childLeft, childTop, childRight, childBottom);
    }

    private int calculateTop(View child, boolean duringLayout) {
        int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
        int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();
        int i = this.mGravity;
        if (i == 16) {
            return this.mSpinnerPadding.top + ((((myHeight - this.mSpinnerPadding.bottom) - this.mSpinnerPadding.top) - childHeight) / 2);
        }
        if (i == 48) {
            return this.mSpinnerPadding.top;
        }
        if (i != 80) {
            return 0;
        }
        return (myHeight - this.mSpinnerPadding.bottom) - childHeight;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        boolean retValue = this.mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (action == 1) {
            onUp();
        } else if (action == 3) {
            onCancel();
        }
        return retValue;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(MotionEvent e) {
        int i = this.mDownTouchPosition;
        if (i < 0) {
            return false;
        }
        scrollToChild(i - this.mFirstPosition);
        if (!this.mShouldCallbackOnUnselectedItemClick && this.mDownTouchPosition != this.mSelectedPosition) {
            return true;
        }
        performItemClick(this.mDownTouchView, this.mDownTouchPosition, this.mAdapter.getItemId(this.mDownTouchPosition));
        return true;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!this.mShouldCallbackDuringFling) {
            removeCallbacks(this.mDisableSuppressSelectionChangedRunnable);
            if (!this.mSuppressSelectionChanged) {
                this.mSuppressSelectionChanged = true;
            }
        }
        this.mFlingRunnable.startUsingVelocity((int) (-velocityX));
        return true;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        this.mParent.requestDisallowInterceptTouchEvent(true);
        if (!this.mShouldCallbackDuringFling) {
            if (this.mIsFirstScroll) {
                if (!this.mSuppressSelectionChanged) {
                    this.mSuppressSelectionChanged = true;
                }
                postDelayed(this.mDisableSuppressSelectionChangedRunnable, 250);
            }
        } else if (this.mSuppressSelectionChanged) {
            this.mSuppressSelectionChanged = false;
        }
        trackMotionScroll(((int) distanceX) * -1);
        this.mIsFirstScroll = false;
        return true;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent e) {
        this.mFlingRunnable.stop(false);
        this.mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());
        int i = this.mDownTouchPosition;
        if (i >= 0) {
            this.mDownTouchView = getChildAt(i - this.mFirstPosition);
            this.mDownTouchView.setPressed(true);
        }
        this.mIsFirstScroll = true;
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onUp() {
        if (this.mFlingRunnable.mScroller.isFinished()) {
            scrollIntoSlots();
        }
        dispatchUnpress();
    }

    /* access modifiers changed from: package-private */
    public void onCancel() {
        onUp();
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent e) {
        if (this.mDownTouchPosition >= 0) {
            performHapticFeedback(0);
            dispatchLongPress(this.mDownTouchView, this.mDownTouchPosition, getItemIdAtPosition(this.mDownTouchPosition), e.getX(), e.getY(), true);
        }
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(MotionEvent e) {
    }

    private void dispatchPress(View child) {
        if (child != null) {
            child.setPressed(true);
        }
        setPressed(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchUnpress() {
        int i = getChildCount();
        while (true) {
            i--;
            if (i >= 0) {
                getChildAt(i).setPressed(false);
            } else {
                setPressed(false);
                return;
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void dispatchSetSelected(boolean selected) {
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void dispatchSetPressed(boolean pressed) {
        View view = this.mSelectedChild;
        if (view != null) {
            view.setPressed(pressed);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean showContextMenuForChild(View originalView) {
        if (isShowingContextMenuWithCoords()) {
            return false;
        }
        return showContextMenuForChildInternal(originalView, 0.0f, 0.0f, false);
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return showContextMenuForChildInternal(originalView, x, y, true);
    }

    private boolean showContextMenuForChildInternal(View originalView, float x, float y, boolean useOffsets) {
        int longPressPosition = getPositionForView(originalView);
        if (longPressPosition < 0) {
            return false;
        }
        return dispatchLongPress(originalView, longPressPosition, this.mAdapter.getItemId(longPressPosition), x, y, useOffsets);
    }

    @Override // android.view.View
    public boolean showContextMenu() {
        return showContextMenuInternal(0.0f, 0.0f, false);
    }

    @Override // android.view.View
    public boolean showContextMenu(float x, float y) {
        return showContextMenuInternal(x, y, true);
    }

    private boolean showContextMenuInternal(float x, float y, boolean useOffsets) {
        if (!isPressed() || this.mSelectedPosition < 0) {
            return false;
        }
        return dispatchLongPress(getChildAt(this.mSelectedPosition - this.mFirstPosition), this.mSelectedPosition, this.mSelectedRowId, x, y, useOffsets);
    }

    private boolean dispatchLongPress(View view, int position, long id, float x, float y, boolean useOffsets) {
        boolean handled = false;
        if (this.mOnItemLongClickListener != null) {
            handled = this.mOnItemLongClickListener.onItemLongClick(this, this.mDownTouchView, this.mDownTouchPosition, id);
        }
        if (!handled) {
            this.mContextMenuInfo = new AdapterView.AdapterContextMenuInfo(view, position, id);
            if (useOffsets) {
                handled = super.showContextMenuForChild(view, x, y);
            } else {
                handled = super.showContextMenuForChild(this);
            }
        }
        if (handled) {
            performHapticFeedback(0);
        }
        return handled;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        return event.dispatch(this, null, null);
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 66) {
            switch (keyCode) {
                case 21:
                    if (moveDirection(-1)) {
                        playSoundEffect(1);
                        return true;
                    }
                    break;
                case 22:
                    if (moveDirection(1)) {
                        playSoundEffect(3);
                        return true;
                    }
                    break;
            }
            return super.onKeyDown(keyCode, event);
        }
        this.mReceivedInvokeKeyDown = true;
        return super.onKeyDown(keyCode, event);
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!KeyEvent.isConfirmKey(keyCode)) {
            return super.onKeyUp(keyCode, event);
        }
        if (this.mReceivedInvokeKeyDown && this.mItemCount > 0) {
            dispatchPress(this.mSelectedChild);
            postDelayed(new Runnable() {
                /* class android.widget.Gallery.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    Gallery.this.dispatchUnpress();
                }
            }, (long) ViewConfiguration.getPressedStateDuration());
            performItemClick(getChildAt(this.mSelectedPosition - this.mFirstPosition), this.mSelectedPosition, this.mAdapter.getItemId(this.mSelectedPosition));
        }
        this.mReceivedInvokeKeyDown = false;
        return true;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public boolean moveDirection(int direction) {
        int targetPosition = this.mSelectedPosition + (isLayoutRtl() ? -direction : direction);
        if (this.mItemCount <= 0 || targetPosition < 0 || targetPosition >= this.mItemCount) {
            return false;
        }
        scrollToChild(targetPosition - this.mFirstPosition);
        return true;
    }

    private boolean scrollToChild(int childPosition) {
        View child = getChildAt(childPosition);
        if (child == null) {
            return false;
        }
        this.mFlingRunnable.startUsingDistance(getCenterOfGallery() - getCenterOfView(child));
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AdapterView
    public void setSelectedPositionInt(int position) {
        super.setSelectedPositionInt(position);
        updateSelectedItemMetadata();
    }

    private void updateSelectedItemMetadata() {
        View oldSelectedChild = this.mSelectedChild;
        View child = getChildAt(this.mSelectedPosition - this.mFirstPosition);
        this.mSelectedChild = child;
        if (child != null) {
            child.setSelected(true);
            child.setFocusable(true);
            if (hasFocus()) {
                child.requestFocus();
            }
            if (oldSelectedChild != null && oldSelectedChild != child) {
                oldSelectedChild.setSelected(false);
                oldSelectedChild.setFocusable(false);
            }
        }
    }

    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            this.mGravity = gravity;
            requestLayout();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public int getChildDrawingOrder(int childCount, int i) {
        int selectedIndex = this.mSelectedPosition - this.mFirstPosition;
        if (selectedIndex < 0) {
            return i;
        }
        if (i == childCount - 1) {
            return selectedIndex;
        }
        if (i >= selectedIndex) {
            return i + 1;
        }
        return i;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        View view;
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && (view = this.mSelectedChild) != null) {
            view.requestFocus(direction);
            this.mSelectedChild.setSelected(true);
        }
    }

    @Override // android.widget.AbsSpinner, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return Gallery.class.getName();
    }

    @Override // android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setScrollable(this.mItemCount > 1);
        if (isEnabled()) {
            if (this.mItemCount > 0 && this.mSelectedPosition < this.mItemCount - 1) {
                info.addAction(4096);
            }
            if (isEnabled() && this.mItemCount > 0 && this.mSelectedPosition > 0) {
                info.addAction(8192);
            }
        }
    }

    @Override // android.view.View
    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (action != 4096) {
            if (action == 8192 && isEnabled() && this.mItemCount > 0 && this.mSelectedPosition > 0) {
                return scrollToChild((this.mSelectedPosition - this.mFirstPosition) - 1);
            }
            return false;
        } else if (!isEnabled() || this.mItemCount <= 0 || this.mSelectedPosition >= this.mItemCount - 1) {
            return false;
        } else {
            return scrollToChild((this.mSelectedPosition - this.mFirstPosition) + 1);
        }
    }

    /* access modifiers changed from: private */
    public class FlingRunnable implements Runnable {
        private int mLastFlingX;
        private Scroller mScroller;

        public FlingRunnable() {
            this.mScroller = new Scroller(Gallery.this.getContext());
        }

        private void startCommon() {
            Gallery.this.removeCallbacks(this);
        }

        @UnsupportedAppUsage
        public void startUsingVelocity(int initialVelocity) {
            if (initialVelocity != 0) {
                startCommon();
                int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
                this.mLastFlingX = initialX;
                this.mScroller.fling(initialX, 0, initialVelocity, 0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
                Gallery.this.post(this);
            }
        }

        public void startUsingDistance(int distance) {
            if (distance != 0) {
                startCommon();
                this.mLastFlingX = 0;
                this.mScroller.startScroll(0, 0, -distance, 0, Gallery.this.mAnimationDuration);
                Gallery.this.post(this);
            }
        }

        public void stop(boolean scrollIntoSlots) {
            Gallery.this.removeCallbacks(this);
            endFling(scrollIntoSlots);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void endFling(boolean scrollIntoSlots) {
            this.mScroller.forceFinished(true);
            if (scrollIntoSlots) {
                Gallery.this.scrollIntoSlots();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            int delta;
            int i;
            int i2;
            if (Gallery.this.mItemCount == 0) {
                endFling(true);
                return;
            }
            Gallery.this.mShouldStopFling = false;
            Scroller scroller = this.mScroller;
            boolean more = scroller.computeScrollOffset();
            int x = scroller.getCurrX();
            int delta2 = this.mLastFlingX - x;
            if (delta2 > 0) {
                Gallery gallery = Gallery.this;
                if (gallery.mIsRtl) {
                    i2 = (Gallery.this.mFirstPosition + Gallery.this.getChildCount()) - 1;
                } else {
                    i2 = Gallery.this.mFirstPosition;
                }
                gallery.mDownTouchPosition = i2;
                delta = Math.min(((Gallery.this.getWidth() - Gallery.this.mPaddingLeft) - Gallery.this.mPaddingRight) - 1, delta2);
            } else {
                int childCount = Gallery.this.getChildCount() - 1;
                Gallery gallery2 = Gallery.this;
                if (gallery2.mIsRtl) {
                    i = Gallery.this.mFirstPosition;
                } else {
                    i = (Gallery.this.mFirstPosition + Gallery.this.getChildCount()) - 1;
                }
                gallery2.mDownTouchPosition = i;
                delta = Math.max(-(((Gallery.this.getWidth() - Gallery.this.mPaddingRight) - Gallery.this.mPaddingLeft) - 1), delta2);
            }
            Gallery.this.trackMotionScroll(delta);
            if (!more || Gallery.this.mShouldStopFling) {
                endFling(true);
                return;
            }
            this.mLastFlingX = x;
            Gallery.this.post(this);
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}
