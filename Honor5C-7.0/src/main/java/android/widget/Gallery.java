package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Transformation;
import android.widget.AdapterView.AdapterContextMenuInfo;
import com.android.internal.R;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.indexsearch.IndexSearchConstants;
import com.nxp.nfc.gsma.internal.NxpNfcController;
import javax.microedition.khronos.opengles.GL10;

@Deprecated
public class Gallery extends AbsSpinner implements OnGestureListener {
    private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;
    private static final String TAG = "Gallery";
    private static final boolean localLOGV = false;
    private int mAnimationDuration;
    private AdapterContextMenuInfo mContextMenuInfo;
    private Runnable mDisableSuppressSelectionChangedRunnable;
    private int mDownTouchPosition;
    private View mDownTouchView;
    private FlingRunnable mFlingRunnable;
    private GestureDetector mGestureDetector;
    private int mGravity;
    private boolean mIsFirstScroll;
    private boolean mIsRtl;
    private int mLeftMost;
    private boolean mReceivedInvokeKeyDown;
    private int mRightMost;
    private int mSelectedCenterOffset;
    private View mSelectedChild;
    private boolean mShouldCallbackDuringFling;
    private boolean mShouldCallbackOnUnselectedItemClick;
    private boolean mShouldStopFling;
    private int mSpacing;
    private boolean mSuppressSelectionChanged;
    private float mUnselectedAlpha;

    private class FlingRunnable implements Runnable {
        private int mLastFlingX;
        private Scroller mScroller;

        public FlingRunnable() {
            this.mScroller = new Scroller(Gallery.this.getContext());
        }

        private void startCommon() {
            Gallery.this.removeCallbacks(this);
        }

        public void startUsingVelocity(int initialVelocity) {
            if (initialVelocity != 0) {
                int initialX;
                startCommon();
                if (initialVelocity < 0) {
                    initialX = HwBootFail.STAGE_BOOT_SUCCESS;
                } else {
                    initialX = 0;
                }
                this.mLastFlingX = initialX;
                this.mScroller.fling(initialX, 0, initialVelocity, 0, 0, HwBootFail.STAGE_BOOT_SUCCESS, 0, HwBootFail.STAGE_BOOT_SUCCESS);
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

        private void endFling(boolean scrollIntoSlots) {
            this.mScroller.forceFinished(true);
            if (scrollIntoSlots) {
                Gallery.this.scrollIntoSlots();
            }
        }

        public void run() {
            if (Gallery.this.mItemCount == 0) {
                endFling(true);
                return;
            }
            Gallery.this.mShouldStopFling = false;
            Scroller scroller = this.mScroller;
            boolean more = scroller.computeScrollOffset();
            int x = scroller.getCurrX();
            int delta = this.mLastFlingX - x;
            Gallery gallery;
            int childCount;
            if (delta > 0) {
                gallery = Gallery.this;
                if (Gallery.this.mIsRtl) {
                    childCount = (Gallery.this.mFirstPosition + Gallery.this.getChildCount()) - 1;
                } else {
                    childCount = Gallery.this.mFirstPosition;
                }
                gallery.mDownTouchPosition = childCount;
                delta = Math.min(((Gallery.this.getWidth() - Gallery.this.mPaddingLeft) - Gallery.this.mPaddingRight) - 1, delta);
            } else {
                int offsetToLast = Gallery.this.getChildCount() - 1;
                gallery = Gallery.this;
                if (Gallery.this.mIsRtl) {
                    childCount = Gallery.this.mFirstPosition;
                } else {
                    childCount = (Gallery.this.mFirstPosition + Gallery.this.getChildCount()) - 1;
                }
                gallery.mDownTouchPosition = childCount;
                delta = Math.max(-(((Gallery.this.getWidth() - Gallery.this.mPaddingRight) - Gallery.this.mPaddingLeft) - 1), delta);
            }
            Gallery.this.trackMotionScroll(delta);
            if (!more || Gallery.this.mShouldStopFling) {
                endFling(true);
            } else {
                this.mLastFlingX = x;
                Gallery.this.post(this);
            }
        }
    }

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public Gallery(Context context) {
        this(context, null);
    }

    public Gallery(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.galleryStyle);
    }

    public Gallery(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Gallery(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSpacing = 0;
        this.mAnimationDuration = StatisticalConstant.TYPE_WIFI_HiLink_CONNECT_ACTION;
        this.mFlingRunnable = new FlingRunnable();
        this.mDisableSuppressSelectionChangedRunnable = new Runnable() {
            public void run() {
                Gallery.this.mSuppressSelectionChanged = false;
                Gallery.this.selectionChanged();
            }
        };
        this.mShouldCallbackDuringFling = true;
        this.mShouldCallbackOnUnselectedItemClick = true;
        this.mIsRtl = true;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gallery, defStyleAttr, defStyleRes);
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
        this.mGroupFlags |= GL10.GL_STENCIL_BUFFER_BIT;
        this.mGroupFlags |= GL10.GL_EXP;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mGestureDetector == null) {
            this.mGestureDetector = new GestureDetector(getContext(), (OnGestureListener) this);
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

    protected boolean getChildStaticTransformation(View child, Transformation t) {
        t.clear();
        t.setAlpha(child == this.mSelectedChild ? android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL : this.mUnselectedAlpha);
        return true;
    }

    protected int computeHorizontalScrollExtent() {
        return 1;
    }

    protected int computeHorizontalScrollOffset() {
        return this.mSelectedPosition;
    }

    protected int computeHorizontalScrollRange() {
        return this.mItemCount;
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        layout(0, false);
        this.mInLayout = false;
    }

    int getChildHeight(View child) {
        return child.getMeasuredHeight();
    }

    void trackMotionScroll(int deltaX) {
        if (getChildCount() != 0) {
            boolean toLeft;
            if (deltaX < 0) {
                toLeft = true;
            } else {
                toLeft = false;
            }
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
                int galleryCenter = getWidth() / 2;
                this.mSelectedCenterOffset = (selChild.getLeft() + (selChild.getWidth() / 2)) - galleryCenter;
            }
            onScrollChanged(0, 0, 0, 0);
            invalidate();
        }
    }

    int getLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
        View extremeChild = getChildAt((motionToLeft != this.mIsRtl ? this.mItemCount - 1 : 0) - this.mFirstPosition);
        if (extremeChild == null) {
            return deltaX;
        }
        int max;
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
            max = Math.max(centerDifference, deltaX);
        } else {
            max = Math.min(centerDifference, deltaX);
        }
        return max;
    }

    private void offsetChildrenLeftAndRight(int offset) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).offsetLeftAndRight(offset);
        }
    }

    private int getCenterOfGallery() {
        return (((getWidth() - this.mPaddingLeft) - this.mPaddingRight) / 2) + this.mPaddingLeft;
    }

    private static int getCenterOfView(View view) {
        return view.getLeft() + (view.getWidth() / 2);
    }

    private void detachOffScreenChildren(boolean toLeft) {
        int numChildren = getChildCount();
        int firstPosition = this.mFirstPosition;
        int start = 0;
        int count = 0;
        int i;
        int n;
        View child;
        if (toLeft) {
            int galleryLeft = this.mPaddingLeft;
            for (i = 0; i < numChildren; i++) {
                if (this.mIsRtl) {
                    n = (numChildren - 1) - i;
                } else {
                    n = i;
                }
                child = getChildAt(n);
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
            i = numChildren - 1;
            while (i >= 0) {
                n = this.mIsRtl ? (numChildren - 1) - i : i;
                child = getChildAt(n);
                if (child.getLeft() <= galleryRight) {
                    break;
                }
                start = n;
                count++;
                this.mRecycler.put(firstPosition + n, child);
                i--;
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

    private void scrollIntoSlots() {
        if (getChildCount() != 0 && this.mSelectedChild != null) {
            int scrollAmount = getCenterOfGallery() - getCenterOfView(this.mSelectedChild);
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

    void selectionChanged() {
        if (!this.mSuppressSelectionChanged) {
            super.selectionChanged();
        }
    }

    private void setSelectionToCenterChild() {
        View selView = this.mSelectedChild;
        if (this.mSelectedChild != null) {
            int galleryCenter = getCenterOfGallery();
            if (selView.getLeft() > galleryCenter || selView.getRight() < galleryCenter) {
                int closestEdgeDistance = HwBootFail.STAGE_BOOT_SUCCESS;
                int newSelectedChildIndex = 0;
                for (int i = getChildCount() - 1; i >= 0; i--) {
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

    void layout(int delta, boolean animate) {
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

    private void fillToGalleryLeft() {
        if (this.mIsRtl) {
            fillToGalleryLeftRtl();
        } else {
            fillToGalleryLeftLtr();
        }
    }

    private void fillToGalleryLeftRtl() {
        int curPosition;
        int curRightEdge;
        int itemSpacing = this.mSpacing;
        int galleryLeft = this.mPaddingLeft;
        int numChildren = getChildCount();
        int numItems = this.mItemCount;
        View prevIterationView = getChildAt(numChildren - 1);
        if (prevIterationView != null) {
            curPosition = this.mFirstPosition + numChildren;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
        } else {
            curPosition = this.mItemCount - 1;
            this.mFirstPosition = curPosition;
            curRightEdge = (this.mRight - this.mLeft) - this.mPaddingRight;
            this.mShouldStopFling = true;
        }
        while (curRightEdge > galleryLeft && curPosition < this.mItemCount) {
            curRightEdge = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curRightEdge, false).getLeft() - itemSpacing;
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
            curRightEdge = (this.mRight - this.mLeft) - this.mPaddingRight;
            this.mShouldStopFling = true;
        }
        while (curRightEdge > galleryLeft && curPosition >= 0) {
            prevIterationView = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curRightEdge, false);
            this.mFirstPosition = curPosition;
            curRightEdge = prevIterationView.getLeft() - itemSpacing;
            curPosition--;
        }
    }

    private void fillToGalleryRight() {
        if (this.mIsRtl) {
            fillToGalleryRightRtl();
        } else {
            fillToGalleryRightLtr();
        }
    }

    private void fillToGalleryRightRtl() {
        int curPosition;
        int curLeftEdge;
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
            prevIterationView = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curLeftEdge, true);
            this.mFirstPosition = curPosition;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
            curPosition--;
        }
    }

    private void fillToGalleryRightLtr() {
        int curPosition;
        int curLeftEdge;
        int itemSpacing = this.mSpacing;
        int galleryRight = (this.mRight - this.mLeft) - this.mPaddingRight;
        int numChildren = getChildCount();
        int numItems = this.mItemCount;
        View prevIterationView = getChildAt(numChildren - 1);
        if (prevIterationView != null) {
            curPosition = this.mFirstPosition + numChildren;
            curLeftEdge = prevIterationView.getRight() + itemSpacing;
        } else {
            curPosition = this.mItemCount - 1;
            this.mFirstPosition = curPosition;
            curLeftEdge = this.mPaddingLeft;
            this.mShouldStopFling = true;
        }
        while (curLeftEdge < galleryRight && curPosition < numItems) {
            curLeftEdge = makeAndAddView(curPosition, curPosition - this.mSelectedPosition, curLeftEdge, true).getRight() + itemSpacing;
            curPosition++;
        }
    }

    private View makeAndAddView(int position, int offset, int x, boolean fromLeft) {
        View child;
        if (!this.mDataChanged) {
            child = this.mRecycler.get(position);
            if (child != null) {
                int childLeft = child.getLeft();
                this.mRightMost = Math.max(this.mRightMost, child.getMeasuredWidth() + childLeft);
                this.mLeftMost = Math.min(this.mLeftMost, childLeft);
                setUpChild(child, offset, x, fromLeft);
                return child;
            }
        }
        child = this.mAdapter.getView(position, null, this);
        setUpChild(child, offset, x, fromLeft);
        return child;
    }

    private void setUpChild(View child, int offset, int x, boolean fromLeft) {
        int childLeft;
        int childRight;
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp == null) {
            lp = (LayoutParams) generateDefaultLayoutParams();
        }
        addViewInLayout(child, fromLeft != this.mIsRtl ? -1 : 0, lp, true);
        child.setSelected(offset == 0);
        child.measure(ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mSpinnerPadding.left + this.mSpinnerPadding.right, lp.width), ViewGroup.getChildMeasureSpec(this.mHeightMeasureSpec, this.mSpinnerPadding.top + this.mSpinnerPadding.bottom, lp.height));
        int childTop = calculateTop(child, true);
        int childBottom = childTop + child.getMeasuredHeight();
        int width = child.getMeasuredWidth();
        if (fromLeft) {
            childLeft = x;
            childRight = x + width;
        } else {
            childLeft = x - width;
            childRight = x;
        }
        child.layout(childLeft, childTop, childRight, childBottom);
    }

    private int calculateTop(View child, boolean duringLayout) {
        int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
        int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();
        switch (this.mGravity) {
            case NxpNfcController.PROTOCOL_ISO_DEP /*16*/:
                return this.mSpinnerPadding.top + ((((myHeight - this.mSpinnerPadding.bottom) - this.mSpinnerPadding.top) - childHeight) / 2);
            case IndexSearchConstants.INDEX_BUILD_FLAG_EXTERNAL_FILE /*48*/:
                return this.mSpinnerPadding.top;
            case StatisticalConstant.TYPE_SCREEN_SHOT_END /*80*/:
                return (myHeight - this.mSpinnerPadding.bottom) - childHeight;
            default:
                return 0;
        }
    }

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

    public boolean onSingleTapUp(MotionEvent e) {
        if (this.mDownTouchPosition < 0) {
            return false;
        }
        scrollToChild(this.mDownTouchPosition - this.mFirstPosition);
        if (this.mShouldCallbackOnUnselectedItemClick || this.mDownTouchPosition == this.mSelectedPosition) {
            performItemClick(this.mDownTouchView, this.mDownTouchPosition, this.mAdapter.getItemId(this.mDownTouchPosition));
        }
        return true;
    }

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

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        this.mParent.requestDisallowInterceptTouchEvent(true);
        if (this.mShouldCallbackDuringFling) {
            if (this.mSuppressSelectionChanged) {
                this.mSuppressSelectionChanged = false;
            }
        } else if (this.mIsFirstScroll) {
            if (!this.mSuppressSelectionChanged) {
                this.mSuppressSelectionChanged = true;
            }
            postDelayed(this.mDisableSuppressSelectionChangedRunnable, 250);
        }
        trackMotionScroll(((int) distanceX) * -1);
        this.mIsFirstScroll = false;
        return true;
    }

    public boolean onDown(MotionEvent e) {
        this.mFlingRunnable.stop(false);
        this.mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());
        if (this.mDownTouchPosition >= 0) {
            this.mDownTouchView = getChildAt(this.mDownTouchPosition - this.mFirstPosition);
            this.mDownTouchView.setPressed(true);
        }
        this.mIsFirstScroll = true;
        return true;
    }

    void onUp() {
        if (this.mFlingRunnable.mScroller.isFinished()) {
            scrollIntoSlots();
        }
        dispatchUnpress();
    }

    void onCancel() {
        onUp();
    }

    public void onLongPress(MotionEvent e) {
        if (this.mDownTouchPosition >= 0) {
            performHapticFeedback(0);
            dispatchLongPress(this.mDownTouchView, this.mDownTouchPosition, getItemIdAtPosition(this.mDownTouchPosition), e.getX(), e.getY(), true);
        }
    }

    public void onShowPress(MotionEvent e) {
    }

    private void dispatchPress(View child) {
        if (child != null) {
            child.setPressed(true);
        }
        setPressed(true);
    }

    private void dispatchUnpress() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            getChildAt(i).setPressed(false);
        }
        setPressed(false);
    }

    public void dispatchSetSelected(boolean selected) {
    }

    protected void dispatchSetPressed(boolean pressed) {
        if (this.mSelectedChild != null) {
            this.mSelectedChild.setPressed(pressed);
        }
    }

    protected ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    public boolean showContextMenuForChild(View originalView) {
        if (isShowingContextMenuWithCoords()) {
            return false;
        }
        return showContextMenuForChildInternal(originalView, 0.0f, 0.0f, false);
    }

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

    public boolean showContextMenu() {
        return showContextMenuInternal(0.0f, 0.0f, false);
    }

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
            this.mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
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

    public boolean dispatchKeyEvent(KeyEvent event) {
        return event.dispatch(this, null, null);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
                if (moveDirection(-1)) {
                    playSoundEffect(1);
                    return true;
                }
                break;
            case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                if (moveDirection(1)) {
                    playSoundEffect(3);
                    return true;
                }
                break;
            case HwPerformance.PERF_TAG_DEF_B_CPU_MIN /*23*/:
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                this.mReceivedInvokeKeyDown = true;
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!KeyEvent.isConfirmKey(keyCode)) {
            return super.onKeyUp(keyCode, event);
        }
        if (this.mReceivedInvokeKeyDown && this.mItemCount > 0) {
            dispatchPress(this.mSelectedChild);
            postDelayed(new Runnable() {
                public void run() {
                    Gallery.this.dispatchUnpress();
                }
            }, (long) ViewConfiguration.getPressedStateDuration());
            performItemClick(getChildAt(this.mSelectedPosition - this.mFirstPosition), this.mSelectedPosition, this.mAdapter.getItemId(this.mSelectedPosition));
        }
        this.mReceivedInvokeKeyDown = false;
        return true;
    }

    boolean moveDirection(int direction) {
        if (isLayoutRtl()) {
            direction = -direction;
        }
        int targetPosition = this.mSelectedPosition + direction;
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

    void setSelectedPositionInt(int position) {
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
            if (!(oldSelectedChild == null || oldSelectedChild == child)) {
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

    protected int getChildDrawingOrder(int childCount, int i) {
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

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && this.mSelectedChild != null) {
            this.mSelectedChild.requestFocus(direction);
            this.mSelectedChild.setSelected(true);
        }
    }

    public CharSequence getAccessibilityClassName() {
        return Gallery.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        boolean z = true;
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (this.mItemCount <= 1) {
            z = false;
        }
        info.setScrollable(z);
        if (isEnabled()) {
            if (this.mItemCount > 0 && this.mSelectedPosition < this.mItemCount - 1) {
                info.addAction((int) HwPerformance.PERF_EVENT_RAW_REQ);
            }
            if (isEnabled() && this.mItemCount > 0 && this.mSelectedPosition > 0) {
                info.addAction((int) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
            }
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        switch (action) {
            case HwPerformance.PERF_EVENT_RAW_REQ /*4096*/:
                if (!isEnabled() || this.mItemCount <= 0 || this.mSelectedPosition >= this.mItemCount - 1) {
                    return false;
                }
                return scrollToChild((this.mSelectedPosition - this.mFirstPosition) + 1);
            case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD /*8192*/:
                if (!isEnabled() || this.mItemCount <= 0 || this.mSelectedPosition <= 0) {
                    return false;
                }
                return scrollToChild((this.mSelectedPosition - this.mFirstPosition) - 1);
            default:
                return false;
        }
    }
}
