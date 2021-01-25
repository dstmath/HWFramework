package com.android.internal.widget;

import android.content.Context;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.widget.RecyclerView;
import com.android.internal.widget.helper.ItemTouchHelper;
import java.util.List;

public class LinearLayoutManager extends RecyclerView.LayoutManager implements ItemTouchHelper.ViewDropHandler, RecyclerView.SmoothScroller.ScrollVectorProvider {
    static final boolean DEBUG = false;
    public static final int HORIZONTAL = 0;
    public static final int INVALID_OFFSET = Integer.MIN_VALUE;
    private static final float MAX_SCROLL_FACTOR = 0.33333334f;
    private static final String TAG = "LinearLayoutManager";
    public static final int VERTICAL = 1;
    final AnchorInfo mAnchorInfo;
    private int mInitialItemPrefetchCount;
    private boolean mLastStackFromEnd;
    private final LayoutChunkResult mLayoutChunkResult;
    private LayoutState mLayoutState;
    int mOrientation;
    OrientationHelper mOrientationHelper;
    SavedState mPendingSavedState;
    int mPendingScrollPosition;
    int mPendingScrollPositionOffset;
    private boolean mRecycleChildrenOnDetach;
    private boolean mReverseLayout;
    boolean mShouldReverseLayout;
    private boolean mSmoothScrollbarEnabled;
    private boolean mStackFromEnd;

    public LinearLayoutManager(Context context) {
        this(context, 1, false);
    }

    public LinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        this.mReverseLayout = false;
        this.mShouldReverseLayout = false;
        this.mStackFromEnd = false;
        this.mSmoothScrollbarEnabled = true;
        this.mPendingScrollPosition = -1;
        this.mPendingScrollPositionOffset = Integer.MIN_VALUE;
        this.mPendingSavedState = null;
        this.mAnchorInfo = new AnchorInfo();
        this.mLayoutChunkResult = new LayoutChunkResult();
        this.mInitialItemPrefetchCount = 2;
        setOrientation(orientation);
        setReverseLayout(reverseLayout);
        setAutoMeasureEnabled(true);
    }

    public LinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mReverseLayout = false;
        this.mShouldReverseLayout = false;
        this.mStackFromEnd = false;
        this.mSmoothScrollbarEnabled = true;
        this.mPendingScrollPosition = -1;
        this.mPendingScrollPositionOffset = Integer.MIN_VALUE;
        this.mPendingSavedState = null;
        this.mAnchorInfo = new AnchorInfo();
        this.mLayoutChunkResult = new LayoutChunkResult();
        this.mInitialItemPrefetchCount = 2;
        RecyclerView.LayoutManager.Properties properties = getProperties(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(properties.orientation);
        setReverseLayout(properties.reverseLayout);
        setStackFromEnd(properties.stackFromEnd);
        setAutoMeasureEnabled(true);
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(-2, -2);
    }

    public boolean getRecycleChildrenOnDetach() {
        return this.mRecycleChildrenOnDetach;
    }

    public void setRecycleChildrenOnDetach(boolean recycleChildrenOnDetach) {
        this.mRecycleChildrenOnDetach = recycleChildrenOnDetach;
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        if (this.mRecycleChildrenOnDetach) {
            removeAndRecycleAllViews(recycler);
            recycler.clear();
        }
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (getChildCount() > 0) {
            event.setFromIndex(findFirstVisibleItemPosition());
            event.setToIndex(findLastVisibleItemPosition());
        }
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public Parcelable onSaveInstanceState() {
        SavedState savedState = this.mPendingSavedState;
        if (savedState != null) {
            return new SavedState(savedState);
        }
        SavedState state = new SavedState();
        if (getChildCount() > 0) {
            ensureLayoutState();
            boolean didLayoutFromEnd = this.mLastStackFromEnd ^ this.mShouldReverseLayout;
            state.mAnchorLayoutFromEnd = didLayoutFromEnd;
            if (didLayoutFromEnd) {
                View refChild = getChildClosestToEnd();
                state.mAnchorOffset = this.mOrientationHelper.getEndAfterPadding() - this.mOrientationHelper.getDecoratedEnd(refChild);
                state.mAnchorPosition = getPosition(refChild);
            } else {
                View refChild2 = getChildClosestToStart();
                state.mAnchorPosition = getPosition(refChild2);
                state.mAnchorOffset = this.mOrientationHelper.getDecoratedStart(refChild2) - this.mOrientationHelper.getStartAfterPadding();
            }
        } else {
            state.invalidateAnchor();
        }
        return state;
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            this.mPendingSavedState = (SavedState) state;
            requestLayout();
        }
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public boolean canScrollHorizontally() {
        return this.mOrientation == 0;
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public boolean canScrollVertically() {
        return this.mOrientation == 1;
    }

    public void setStackFromEnd(boolean stackFromEnd) {
        assertNotInLayoutOrScroll(null);
        if (this.mStackFromEnd != stackFromEnd) {
            this.mStackFromEnd = stackFromEnd;
            requestLayout();
        }
    }

    public boolean getStackFromEnd() {
        return this.mStackFromEnd;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public void setOrientation(int orientation) {
        if (orientation == 0 || orientation == 1) {
            assertNotInLayoutOrScroll(null);
            if (orientation != this.mOrientation) {
                this.mOrientation = orientation;
                this.mOrientationHelper = null;
                requestLayout();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("invalid orientation:" + orientation);
    }

    private void resolveShouldLayoutReverse() {
        if (this.mOrientation == 1 || !isLayoutRTL()) {
            this.mShouldReverseLayout = this.mReverseLayout;
        } else {
            this.mShouldReverseLayout = !this.mReverseLayout;
        }
    }

    public boolean getReverseLayout() {
        return this.mReverseLayout;
    }

    public void setReverseLayout(boolean reverseLayout) {
        assertNotInLayoutOrScroll(null);
        if (reverseLayout != this.mReverseLayout) {
            this.mReverseLayout = reverseLayout;
            requestLayout();
        }
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public View findViewByPosition(int position) {
        int childCount = getChildCount();
        if (childCount == 0) {
            return null;
        }
        int viewPosition = position - getPosition(getChildAt(0));
        if (viewPosition >= 0 && viewPosition < childCount) {
            View child = getChildAt(viewPosition);
            if (getPosition(child) == position) {
                return child;
            }
        }
        return super.findViewByPosition(position);
    }

    /* access modifiers changed from: protected */
    public int getExtraLayoutSpace(RecyclerView.State state) {
        if (state.hasTargetScrollPosition()) {
            return this.mOrientationHelper.getTotalSpace();
        }
        return 0;
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    @Override // com.android.internal.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        boolean z = false;
        int direction = 1;
        if (targetPosition < getPosition(getChildAt(0))) {
            z = true;
        }
        if (z != this.mShouldReverseLayout) {
            direction = -1;
        }
        if (this.mOrientation == 0) {
            return new PointF((float) direction, 0.0f);
        }
        return new PointF(0.0f, (float) direction);
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int extraForStart;
        int extraForEnd;
        int endOffset;
        int firstElement;
        int i;
        View existing;
        int upcomingOffset;
        int firstLayoutDirection = -1;
        if (!(this.mPendingSavedState == null && this.mPendingScrollPosition == -1) && state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }
        SavedState savedState = this.mPendingSavedState;
        if (savedState != null && savedState.hasValidAnchor()) {
            this.mPendingScrollPosition = this.mPendingSavedState.mAnchorPosition;
        }
        ensureLayoutState();
        this.mLayoutState.mRecycle = false;
        resolveShouldLayoutReverse();
        if (!(this.mAnchorInfo.mValid && this.mPendingScrollPosition == -1 && this.mPendingSavedState == null)) {
            this.mAnchorInfo.reset();
            AnchorInfo anchorInfo = this.mAnchorInfo;
            anchorInfo.mLayoutFromEnd = this.mShouldReverseLayout ^ this.mStackFromEnd;
            updateAnchorInfoForLayout(recycler, state, anchorInfo);
            this.mAnchorInfo.mValid = true;
        }
        int extra = getExtraLayoutSpace(state);
        if (this.mLayoutState.mLastScrollDelta >= 0) {
            extraForEnd = extra;
            extraForStart = 0;
        } else {
            extraForStart = extra;
            extraForEnd = 0;
        }
        int extraForStart2 = extraForStart + this.mOrientationHelper.getStartAfterPadding();
        int extraForEnd2 = extraForEnd + this.mOrientationHelper.getEndPadding();
        if (!(!state.isPreLayout() || (i = this.mPendingScrollPosition) == -1 || this.mPendingScrollPositionOffset == Integer.MIN_VALUE || (existing = findViewByPosition(i)) == null)) {
            if (this.mShouldReverseLayout) {
                upcomingOffset = (this.mOrientationHelper.getEndAfterPadding() - this.mOrientationHelper.getDecoratedEnd(existing)) - this.mPendingScrollPositionOffset;
            } else {
                upcomingOffset = this.mPendingScrollPositionOffset - (this.mOrientationHelper.getDecoratedStart(existing) - this.mOrientationHelper.getStartAfterPadding());
            }
            if (upcomingOffset > 0) {
                extraForStart2 += upcomingOffset;
            } else {
                extraForEnd2 -= upcomingOffset;
            }
        }
        if (this.mAnchorInfo.mLayoutFromEnd) {
            if (this.mShouldReverseLayout) {
                firstLayoutDirection = 1;
            }
        } else if (!this.mShouldReverseLayout) {
            firstLayoutDirection = 1;
        }
        onAnchorReady(recycler, state, this.mAnchorInfo, firstLayoutDirection);
        detachAndScrapAttachedViews(recycler);
        this.mLayoutState.mInfinite = resolveIsInfinite();
        this.mLayoutState.mIsPreLayout = state.isPreLayout();
        if (this.mAnchorInfo.mLayoutFromEnd) {
            updateLayoutStateToFillStart(this.mAnchorInfo);
            LayoutState layoutState = this.mLayoutState;
            layoutState.mExtra = extraForStart2;
            fill(recycler, layoutState, state, false);
            int startOffset = this.mLayoutState.mOffset;
            int firstElement2 = this.mLayoutState.mCurrentPosition;
            if (this.mLayoutState.mAvailable > 0) {
                extraForEnd2 += this.mLayoutState.mAvailable;
            }
            updateLayoutStateToFillEnd(this.mAnchorInfo);
            LayoutState layoutState2 = this.mLayoutState;
            layoutState2.mExtra = extraForEnd2;
            layoutState2.mCurrentPosition += this.mLayoutState.mItemDirection;
            fill(recycler, this.mLayoutState, state, false);
            endOffset = this.mLayoutState.mOffset;
            if (this.mLayoutState.mAvailable > 0) {
                int extraForStart3 = this.mLayoutState.mAvailable;
                updateLayoutStateToFillStart(firstElement2, startOffset);
                LayoutState layoutState3 = this.mLayoutState;
                layoutState3.mExtra = extraForStart3;
                fill(recycler, layoutState3, state, false);
                startOffset = this.mLayoutState.mOffset;
            }
            firstElement = startOffset;
        } else {
            updateLayoutStateToFillEnd(this.mAnchorInfo);
            LayoutState layoutState4 = this.mLayoutState;
            layoutState4.mExtra = extraForEnd2;
            fill(recycler, layoutState4, state, false);
            endOffset = this.mLayoutState.mOffset;
            int lastElement = this.mLayoutState.mCurrentPosition;
            if (this.mLayoutState.mAvailable > 0) {
                extraForStart2 += this.mLayoutState.mAvailable;
            }
            updateLayoutStateToFillStart(this.mAnchorInfo);
            LayoutState layoutState5 = this.mLayoutState;
            layoutState5.mExtra = extraForStart2;
            layoutState5.mCurrentPosition += this.mLayoutState.mItemDirection;
            fill(recycler, this.mLayoutState, state, false);
            firstElement = this.mLayoutState.mOffset;
            if (this.mLayoutState.mAvailable > 0) {
                int extraForEnd3 = this.mLayoutState.mAvailable;
                updateLayoutStateToFillEnd(lastElement, endOffset);
                LayoutState layoutState6 = this.mLayoutState;
                layoutState6.mExtra = extraForEnd3;
                fill(recycler, layoutState6, state, false);
                endOffset = this.mLayoutState.mOffset;
            }
        }
        if (getChildCount() > 0) {
            if (this.mShouldReverseLayout ^ this.mStackFromEnd) {
                int fixOffset = fixLayoutEndGap(endOffset, recycler, state, true);
                int startOffset2 = firstElement + fixOffset;
                int fixOffset2 = fixLayoutStartGap(startOffset2, recycler, state, false);
                firstElement = startOffset2 + fixOffset2;
                endOffset = endOffset + fixOffset + fixOffset2;
            } else {
                int fixOffset3 = fixLayoutStartGap(firstElement, recycler, state, true);
                int endOffset2 = endOffset + fixOffset3;
                int fixOffset4 = fixLayoutEndGap(endOffset2, recycler, state, false);
                firstElement = firstElement + fixOffset3 + fixOffset4;
                endOffset = endOffset2 + fixOffset4;
            }
        }
        layoutForPredictiveAnimations(recycler, state, firstElement, endOffset);
        if (!state.isPreLayout()) {
            this.mOrientationHelper.onLayoutComplete();
        } else {
            this.mAnchorInfo.reset();
        }
        this.mLastStackFromEnd = this.mStackFromEnd;
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        this.mPendingSavedState = null;
        this.mPendingScrollPosition = -1;
        this.mPendingScrollPositionOffset = Integer.MIN_VALUE;
        this.mAnchorInfo.reset();
    }

    /* access modifiers changed from: package-private */
    public void onAnchorReady(RecyclerView.Recycler recycler, RecyclerView.State state, AnchorInfo anchorInfo, int firstLayoutItemDirection) {
    }

    private void layoutForPredictiveAnimations(RecyclerView.Recycler recycler, RecyclerView.State state, int startOffset, int endOffset) {
        if (!(!state.willRunPredictiveAnimations() || getChildCount() == 0 || state.isPreLayout())) {
            if (supportsPredictiveItemAnimations()) {
                int scrapExtraStart = 0;
                int scrapExtraEnd = 0;
                List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
                int scrapSize = scrapList.size();
                int firstChildPos = getPosition(getChildAt(0));
                for (int i = 0; i < scrapSize; i++) {
                    RecyclerView.ViewHolder scrap = scrapList.get(i);
                    if (!scrap.isRemoved()) {
                        int direction = 1;
                        if ((scrap.getLayoutPosition() < firstChildPos) != this.mShouldReverseLayout) {
                            direction = -1;
                        }
                        if (direction == -1) {
                            scrapExtraStart += this.mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
                        } else {
                            scrapExtraEnd += this.mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
                        }
                    }
                }
                this.mLayoutState.mScrapList = scrapList;
                if (scrapExtraStart > 0) {
                    updateLayoutStateToFillStart(getPosition(getChildClosestToStart()), startOffset);
                    LayoutState layoutState = this.mLayoutState;
                    layoutState.mExtra = scrapExtraStart;
                    layoutState.mAvailable = 0;
                    layoutState.assignPositionFromScrapList();
                    fill(recycler, this.mLayoutState, state, false);
                }
                if (scrapExtraEnd > 0) {
                    updateLayoutStateToFillEnd(getPosition(getChildClosestToEnd()), endOffset);
                    LayoutState layoutState2 = this.mLayoutState;
                    layoutState2.mExtra = scrapExtraEnd;
                    layoutState2.mAvailable = 0;
                    layoutState2.assignPositionFromScrapList();
                    fill(recycler, this.mLayoutState, state, false);
                }
                this.mLayoutState.mScrapList = null;
            }
        }
    }

    private void updateAnchorInfoForLayout(RecyclerView.Recycler recycler, RecyclerView.State state, AnchorInfo anchorInfo) {
        if (!updateAnchorFromPendingData(state, anchorInfo) && !updateAnchorFromChildren(recycler, state, anchorInfo)) {
            anchorInfo.assignCoordinateFromPadding();
            anchorInfo.mPosition = this.mStackFromEnd ? state.getItemCount() - 1 : 0;
        }
    }

    private boolean updateAnchorFromChildren(RecyclerView.Recycler recycler, RecyclerView.State state, AnchorInfo anchorInfo) {
        View referenceChild;
        int i;
        boolean notVisible = false;
        if (getChildCount() == 0) {
            return false;
        }
        View focused = getFocusedChild();
        if (focused != null && anchorInfo.isViewValidAsAnchor(focused, state)) {
            anchorInfo.assignFromViewAndKeepVisibleRect(focused);
            return true;
        } else if (this.mLastStackFromEnd != this.mStackFromEnd) {
            return false;
        } else {
            if (anchorInfo.mLayoutFromEnd) {
                referenceChild = findReferenceChildClosestToEnd(recycler, state);
            } else {
                referenceChild = findReferenceChildClosestToStart(recycler, state);
            }
            if (referenceChild == null) {
                return false;
            }
            anchorInfo.assignFromView(referenceChild);
            if (!state.isPreLayout() && supportsPredictiveItemAnimations()) {
                if (this.mOrientationHelper.getDecoratedStart(referenceChild) >= this.mOrientationHelper.getEndAfterPadding() || this.mOrientationHelper.getDecoratedEnd(referenceChild) < this.mOrientationHelper.getStartAfterPadding()) {
                    notVisible = true;
                }
                if (notVisible) {
                    if (anchorInfo.mLayoutFromEnd) {
                        i = this.mOrientationHelper.getEndAfterPadding();
                    } else {
                        i = this.mOrientationHelper.getStartAfterPadding();
                    }
                    anchorInfo.mCoordinate = i;
                }
            }
            return true;
        }
    }

    private boolean updateAnchorFromPendingData(RecyclerView.State state, AnchorInfo anchorInfo) {
        int i;
        int i2;
        boolean z = false;
        if (state.isPreLayout() || (i = this.mPendingScrollPosition) == -1) {
            return false;
        }
        if (i < 0 || i >= state.getItemCount()) {
            this.mPendingScrollPosition = -1;
            this.mPendingScrollPositionOffset = Integer.MIN_VALUE;
            return false;
        }
        anchorInfo.mPosition = this.mPendingScrollPosition;
        SavedState savedState = this.mPendingSavedState;
        if (savedState != null && savedState.hasValidAnchor()) {
            anchorInfo.mLayoutFromEnd = this.mPendingSavedState.mAnchorLayoutFromEnd;
            if (anchorInfo.mLayoutFromEnd) {
                anchorInfo.mCoordinate = this.mOrientationHelper.getEndAfterPadding() - this.mPendingSavedState.mAnchorOffset;
            } else {
                anchorInfo.mCoordinate = this.mOrientationHelper.getStartAfterPadding() + this.mPendingSavedState.mAnchorOffset;
            }
            return true;
        } else if (this.mPendingScrollPositionOffset == Integer.MIN_VALUE) {
            View child = findViewByPosition(this.mPendingScrollPosition);
            if (child == null) {
                if (getChildCount() > 0) {
                    if ((this.mPendingScrollPosition < getPosition(getChildAt(0))) == this.mShouldReverseLayout) {
                        z = true;
                    }
                    anchorInfo.mLayoutFromEnd = z;
                }
                anchorInfo.assignCoordinateFromPadding();
            } else if (this.mOrientationHelper.getDecoratedMeasurement(child) > this.mOrientationHelper.getTotalSpace()) {
                anchorInfo.assignCoordinateFromPadding();
                return true;
            } else if (this.mOrientationHelper.getDecoratedStart(child) - this.mOrientationHelper.getStartAfterPadding() < 0) {
                anchorInfo.mCoordinate = this.mOrientationHelper.getStartAfterPadding();
                anchorInfo.mLayoutFromEnd = false;
                return true;
            } else if (this.mOrientationHelper.getEndAfterPadding() - this.mOrientationHelper.getDecoratedEnd(child) < 0) {
                anchorInfo.mCoordinate = this.mOrientationHelper.getEndAfterPadding();
                anchorInfo.mLayoutFromEnd = true;
                return true;
            } else {
                if (anchorInfo.mLayoutFromEnd) {
                    i2 = this.mOrientationHelper.getDecoratedEnd(child) + this.mOrientationHelper.getTotalSpaceChange();
                } else {
                    i2 = this.mOrientationHelper.getDecoratedStart(child);
                }
                anchorInfo.mCoordinate = i2;
            }
            return true;
        } else {
            boolean z2 = this.mShouldReverseLayout;
            anchorInfo.mLayoutFromEnd = z2;
            if (z2) {
                anchorInfo.mCoordinate = this.mOrientationHelper.getEndAfterPadding() - this.mPendingScrollPositionOffset;
            } else {
                anchorInfo.mCoordinate = this.mOrientationHelper.getStartAfterPadding() + this.mPendingScrollPositionOffset;
            }
            return true;
        }
    }

    private int fixLayoutEndGap(int endOffset, RecyclerView.Recycler recycler, RecyclerView.State state, boolean canOffsetChildren) {
        int gap;
        int gap2 = this.mOrientationHelper.getEndAfterPadding() - endOffset;
        if (gap2 <= 0) {
            return 0;
        }
        int fixOffset = -scrollBy(-gap2, recycler, state);
        int endOffset2 = endOffset + fixOffset;
        if (!canOffsetChildren || (gap = this.mOrientationHelper.getEndAfterPadding() - endOffset2) <= 0) {
            return fixOffset;
        }
        this.mOrientationHelper.offsetChildren(gap);
        return gap + fixOffset;
    }

    private int fixLayoutStartGap(int startOffset, RecyclerView.Recycler recycler, RecyclerView.State state, boolean canOffsetChildren) {
        int gap;
        int gap2 = startOffset - this.mOrientationHelper.getStartAfterPadding();
        if (gap2 <= 0) {
            return 0;
        }
        int fixOffset = -scrollBy(gap2, recycler, state);
        int startOffset2 = startOffset + fixOffset;
        if (!canOffsetChildren || (gap = startOffset2 - this.mOrientationHelper.getStartAfterPadding()) <= 0) {
            return fixOffset;
        }
        this.mOrientationHelper.offsetChildren(-gap);
        return fixOffset - gap;
    }

    private void updateLayoutStateToFillEnd(AnchorInfo anchorInfo) {
        updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
        int i;
        this.mLayoutState.mAvailable = this.mOrientationHelper.getEndAfterPadding() - offset;
        LayoutState layoutState = this.mLayoutState;
        if (this.mShouldReverseLayout) {
            i = -1;
        } else {
            i = 1;
        }
        layoutState.mItemDirection = i;
        LayoutState layoutState2 = this.mLayoutState;
        layoutState2.mCurrentPosition = itemPosition;
        layoutState2.mLayoutDirection = 1;
        layoutState2.mOffset = offset;
        layoutState2.mScrollingOffset = Integer.MIN_VALUE;
    }

    private void updateLayoutStateToFillStart(AnchorInfo anchorInfo) {
        updateLayoutStateToFillStart(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillStart(int itemPosition, int offset) {
        int i;
        this.mLayoutState.mAvailable = offset - this.mOrientationHelper.getStartAfterPadding();
        LayoutState layoutState = this.mLayoutState;
        layoutState.mCurrentPosition = itemPosition;
        if (this.mShouldReverseLayout) {
            i = 1;
        } else {
            i = -1;
        }
        layoutState.mItemDirection = i;
        LayoutState layoutState2 = this.mLayoutState;
        layoutState2.mLayoutDirection = -1;
        layoutState2.mOffset = offset;
        layoutState2.mScrollingOffset = Integer.MIN_VALUE;
    }

    /* access modifiers changed from: protected */
    public boolean isLayoutRTL() {
        return getLayoutDirection() == 1;
    }

    /* access modifiers changed from: package-private */
    public void ensureLayoutState() {
        if (this.mLayoutState == null) {
            this.mLayoutState = createLayoutState();
        }
        if (this.mOrientationHelper == null) {
            this.mOrientationHelper = OrientationHelper.createOrientationHelper(this, this.mOrientation);
        }
    }

    /* access modifiers changed from: package-private */
    public LayoutState createLayoutState() {
        return new LayoutState();
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void scrollToPosition(int position) {
        this.mPendingScrollPosition = position;
        this.mPendingScrollPositionOffset = Integer.MIN_VALUE;
        SavedState savedState = this.mPendingSavedState;
        if (savedState != null) {
            savedState.invalidateAnchor();
        }
        requestLayout();
    }

    public void scrollToPositionWithOffset(int position, int offset) {
        this.mPendingScrollPosition = position;
        this.mPendingScrollPositionOffset = offset;
        SavedState savedState = this.mPendingSavedState;
        if (savedState != null) {
            savedState.invalidateAnchor();
        }
        requestLayout();
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 1) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mOrientation == 0) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        return computeScrollOffset(state);
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return computeScrollOffset(state);
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public int computeHorizontalScrollExtent(RecyclerView.State state) {
        return computeScrollExtent(state);
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return computeScrollExtent(state);
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        return computeScrollRange(state);
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return computeScrollRange(state);
    }

    private int computeScrollOffset(RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelper.computeScrollOffset(state, this.mOrientationHelper, findFirstVisibleChildClosestToStart(!this.mSmoothScrollbarEnabled, true), findFirstVisibleChildClosestToEnd(!this.mSmoothScrollbarEnabled, true), this, this.mSmoothScrollbarEnabled, this.mShouldReverseLayout);
    }

    private int computeScrollExtent(RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelper.computeScrollExtent(state, this.mOrientationHelper, findFirstVisibleChildClosestToStart(!this.mSmoothScrollbarEnabled, true), findFirstVisibleChildClosestToEnd(!this.mSmoothScrollbarEnabled, true), this, this.mSmoothScrollbarEnabled);
    }

    private int computeScrollRange(RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelper.computeScrollRange(state, this.mOrientationHelper, findFirstVisibleChildClosestToStart(!this.mSmoothScrollbarEnabled, true), findFirstVisibleChildClosestToEnd(!this.mSmoothScrollbarEnabled, true), this, this.mSmoothScrollbarEnabled);
    }

    public void setSmoothScrollbarEnabled(boolean enabled) {
        this.mSmoothScrollbarEnabled = enabled;
    }

    public boolean isSmoothScrollbarEnabled() {
        return this.mSmoothScrollbarEnabled;
    }

    private void updateLayoutState(int layoutDirection, int requiredSpace, boolean canUseExistingSpace, RecyclerView.State state) {
        int scrollingOffset;
        this.mLayoutState.mInfinite = resolveIsInfinite();
        this.mLayoutState.mExtra = getExtraLayoutSpace(state);
        LayoutState layoutState = this.mLayoutState;
        layoutState.mLayoutDirection = layoutDirection;
        int i = -1;
        if (layoutDirection == 1) {
            layoutState.mExtra += this.mOrientationHelper.getEndPadding();
            View child = getChildClosestToEnd();
            LayoutState layoutState2 = this.mLayoutState;
            if (!this.mShouldReverseLayout) {
                i = 1;
            }
            layoutState2.mItemDirection = i;
            this.mLayoutState.mCurrentPosition = getPosition(child) + this.mLayoutState.mItemDirection;
            this.mLayoutState.mOffset = this.mOrientationHelper.getDecoratedEnd(child);
            scrollingOffset = this.mOrientationHelper.getDecoratedEnd(child) - this.mOrientationHelper.getEndAfterPadding();
        } else {
            View child2 = getChildClosestToStart();
            this.mLayoutState.mExtra += this.mOrientationHelper.getStartAfterPadding();
            LayoutState layoutState3 = this.mLayoutState;
            if (this.mShouldReverseLayout) {
                i = 1;
            }
            layoutState3.mItemDirection = i;
            this.mLayoutState.mCurrentPosition = getPosition(child2) + this.mLayoutState.mItemDirection;
            this.mLayoutState.mOffset = this.mOrientationHelper.getDecoratedStart(child2);
            scrollingOffset = (-this.mOrientationHelper.getDecoratedStart(child2)) + this.mOrientationHelper.getStartAfterPadding();
        }
        LayoutState layoutState4 = this.mLayoutState;
        layoutState4.mAvailable = requiredSpace;
        if (canUseExistingSpace) {
            layoutState4.mAvailable -= scrollingOffset;
        }
        this.mLayoutState.mScrollingOffset = scrollingOffset;
    }

    /* access modifiers changed from: package-private */
    public boolean resolveIsInfinite() {
        return this.mOrientationHelper.getMode() == 0 && this.mOrientationHelper.getEnd() == 0;
    }

    /* access modifiers changed from: package-private */
    public void collectPrefetchPositionsForLayoutState(RecyclerView.State state, LayoutState layoutState, RecyclerView.LayoutManager.LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int pos = layoutState.mCurrentPosition;
        if (pos >= 0 && pos < state.getItemCount()) {
            layoutPrefetchRegistry.addPosition(pos, layoutState.mScrollingOffset);
        }
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void collectInitialPrefetchPositions(int adapterItemCount, RecyclerView.LayoutManager.LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int anchorPos;
        boolean fromEnd;
        SavedState savedState = this.mPendingSavedState;
        int direction = -1;
        if (savedState == null || !savedState.hasValidAnchor()) {
            resolveShouldLayoutReverse();
            fromEnd = this.mShouldReverseLayout;
            if (this.mPendingScrollPosition == -1) {
                anchorPos = fromEnd ? adapterItemCount - 1 : 0;
            } else {
                anchorPos = this.mPendingScrollPosition;
            }
        } else {
            fromEnd = this.mPendingSavedState.mAnchorLayoutFromEnd;
            anchorPos = this.mPendingSavedState.mAnchorPosition;
        }
        if (!fromEnd) {
            direction = 1;
        }
        int targetPos = anchorPos;
        for (int i = 0; i < this.mInitialItemPrefetchCount && targetPos >= 0 && targetPos < adapterItemCount; i++) {
            layoutPrefetchRegistry.addPosition(targetPos, 0);
            targetPos += direction;
        }
    }

    public void setInitialPrefetchItemCount(int itemCount) {
        this.mInitialItemPrefetchCount = itemCount;
    }

    public int getInitialItemPrefetchCount() {
        return this.mInitialItemPrefetchCount;
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void collectAdjacentPrefetchPositions(int dx, int dy, RecyclerView.State state, RecyclerView.LayoutManager.LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int delta = this.mOrientation == 0 ? dx : dy;
        if (getChildCount() != 0 && delta != 0) {
            updateLayoutState(delta > 0 ? 1 : -1, Math.abs(delta), true, state);
            collectPrefetchPositionsForLayoutState(state, this.mLayoutState, layoutPrefetchRegistry);
        }
    }

    /* access modifiers changed from: package-private */
    public int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        this.mLayoutState.mRecycle = true;
        ensureLayoutState();
        int layoutDirection = dy > 0 ? 1 : -1;
        int absDy = Math.abs(dy);
        updateLayoutState(layoutDirection, absDy, true, state);
        int consumed = this.mLayoutState.mScrollingOffset + fill(recycler, this.mLayoutState, state, false);
        if (consumed < 0) {
            return 0;
        }
        int scrolled = absDy > consumed ? layoutDirection * consumed : dy;
        this.mOrientationHelper.offsetChildren(-scrolled);
        this.mLayoutState.mLastScrollDelta = scrolled;
        return scrolled;
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public void assertNotInLayoutOrScroll(String message) {
        if (this.mPendingSavedState == null) {
            super.assertNotInLayoutOrScroll(message);
        }
    }

    private void recycleChildren(RecyclerView.Recycler recycler, int startIndex, int endIndex) {
        if (startIndex != endIndex) {
            if (endIndex > startIndex) {
                for (int i = endIndex - 1; i >= startIndex; i--) {
                    removeAndRecycleViewAt(i, recycler);
                }
                return;
            }
            for (int i2 = startIndex; i2 > endIndex; i2--) {
                removeAndRecycleViewAt(i2, recycler);
            }
        }
    }

    private void recycleViewsFromStart(RecyclerView.Recycler recycler, int dt) {
        if (dt >= 0) {
            int childCount = getChildCount();
            if (this.mShouldReverseLayout) {
                for (int i = childCount - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    if (this.mOrientationHelper.getDecoratedEnd(child) > dt || this.mOrientationHelper.getTransformedEndWithDecoration(child) > dt) {
                        recycleChildren(recycler, childCount - 1, i);
                        return;
                    }
                }
                return;
            }
            for (int i2 = 0; i2 < childCount; i2++) {
                View child2 = getChildAt(i2);
                if (this.mOrientationHelper.getDecoratedEnd(child2) > dt || this.mOrientationHelper.getTransformedEndWithDecoration(child2) > dt) {
                    recycleChildren(recycler, 0, i2);
                    return;
                }
            }
        }
    }

    private void recycleViewsFromEnd(RecyclerView.Recycler recycler, int dt) {
        int childCount = getChildCount();
        if (dt >= 0) {
            int limit = this.mOrientationHelper.getEnd() - dt;
            if (this.mShouldReverseLayout) {
                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (this.mOrientationHelper.getDecoratedStart(child) < limit || this.mOrientationHelper.getTransformedStartWithDecoration(child) < limit) {
                        recycleChildren(recycler, 0, i);
                        return;
                    }
                }
                return;
            }
            for (int i2 = childCount - 1; i2 >= 0; i2--) {
                View child2 = getChildAt(i2);
                if (this.mOrientationHelper.getDecoratedStart(child2) < limit || this.mOrientationHelper.getTransformedStartWithDecoration(child2) < limit) {
                    recycleChildren(recycler, childCount - 1, i2);
                    return;
                }
            }
        }
    }

    private void recycleByLayoutState(RecyclerView.Recycler recycler, LayoutState layoutState) {
        if (layoutState.mRecycle && !layoutState.mInfinite) {
            if (layoutState.mLayoutDirection == -1) {
                recycleViewsFromEnd(recycler, layoutState.mScrollingOffset);
            } else {
                recycleViewsFromStart(recycler, layoutState.mScrollingOffset);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int fill(RecyclerView.Recycler recycler, LayoutState layoutState, RecyclerView.State state, boolean stopOnFocusable) {
        int start = layoutState.mAvailable;
        if (layoutState.mScrollingOffset != Integer.MIN_VALUE) {
            if (layoutState.mAvailable < 0) {
                layoutState.mScrollingOffset += layoutState.mAvailable;
            }
            recycleByLayoutState(recycler, layoutState);
        }
        int remainingSpace = layoutState.mAvailable + layoutState.mExtra;
        LayoutChunkResult layoutChunkResult = this.mLayoutChunkResult;
        while (true) {
            if ((!layoutState.mInfinite && remainingSpace <= 0) || !layoutState.hasMore(state)) {
                break;
            }
            layoutChunkResult.resetInternal();
            layoutChunk(recycler, state, layoutState, layoutChunkResult);
            if (!layoutChunkResult.mFinished) {
                layoutState.mOffset += layoutChunkResult.mConsumed * layoutState.mLayoutDirection;
                if (!layoutChunkResult.mIgnoreConsumed || this.mLayoutState.mScrapList != null || !state.isPreLayout()) {
                    layoutState.mAvailable -= layoutChunkResult.mConsumed;
                    remainingSpace -= layoutChunkResult.mConsumed;
                }
                if (layoutState.mScrollingOffset != Integer.MIN_VALUE) {
                    layoutState.mScrollingOffset += layoutChunkResult.mConsumed;
                    if (layoutState.mAvailable < 0) {
                        layoutState.mScrollingOffset += layoutState.mAvailable;
                    }
                    recycleByLayoutState(recycler, layoutState);
                }
                if (stopOnFocusable && layoutChunkResult.mFocusable) {
                    break;
                }
            } else {
                break;
            }
        }
        return start - layoutState.mAvailable;
    }

    /* JADX INFO: Multiple debug info for r1v2 int: [D('right' int), D('left' int)] */
    /* JADX INFO: Multiple debug info for r1v4 int: [D('bottom' int), D('top' int)] */
    /* access modifiers changed from: package-private */
    public void layoutChunk(RecyclerView.Recycler recycler, RecyclerView.State state, LayoutState layoutState, LayoutChunkResult result) {
        int bottom;
        int right;
        int top;
        int left;
        int left2;
        int right2;
        View view = layoutState.next(recycler);
        if (view == null) {
            result.mFinished = true;
            return;
        }
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
        if (layoutState.mScrapList == null) {
            if (this.mShouldReverseLayout == (layoutState.mLayoutDirection == -1)) {
                addView(view);
            } else {
                addView(view, 0);
            }
        } else {
            if (this.mShouldReverseLayout == (layoutState.mLayoutDirection == -1)) {
                addDisappearingView(view);
            } else {
                addDisappearingView(view, 0);
            }
        }
        measureChildWithMargins(view, 0, 0);
        result.mConsumed = this.mOrientationHelper.getDecoratedMeasurement(view);
        if (this.mOrientation == 1) {
            if (isLayoutRTL()) {
                right2 = getWidth() - getPaddingRight();
                left2 = right2 - this.mOrientationHelper.getDecoratedMeasurementInOther(view);
            } else {
                left2 = getPaddingLeft();
                right2 = this.mOrientationHelper.getDecoratedMeasurementInOther(view) + left2;
            }
            if (layoutState.mLayoutDirection == -1) {
                right = right2;
                bottom = layoutState.mOffset;
                left = left2;
                top = layoutState.mOffset - result.mConsumed;
            } else {
                right = right2;
                top = layoutState.mOffset;
                left = left2;
                bottom = layoutState.mOffset + result.mConsumed;
            }
        } else {
            int top2 = getPaddingTop();
            int bottom2 = this.mOrientationHelper.getDecoratedMeasurementInOther(view) + top2;
            if (layoutState.mLayoutDirection == -1) {
                top = top2;
                right = layoutState.mOffset;
                bottom = bottom2;
                left = layoutState.mOffset - result.mConsumed;
            } else {
                top = top2;
                left = layoutState.mOffset;
                bottom = bottom2;
                right = layoutState.mOffset + result.mConsumed;
            }
        }
        layoutDecoratedWithMargins(view, left, top, right, bottom);
        if (params.isItemRemoved() || params.isItemChanged()) {
            result.mIgnoreConsumed = true;
        }
        result.mFocusable = view.isFocusable();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public boolean shouldMeasureTwice() {
        return (getHeightMode() == 1073741824 || getWidthMode() == 1073741824 || !hasFlexibleChildInBothOrientations()) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public int convertFocusDirectionToLayoutDirection(int focusDirection) {
        if (focusDirection != 1) {
            if (focusDirection != 2) {
                if (focusDirection != 17) {
                    if (focusDirection != 33) {
                        if (focusDirection == 66) {
                            return this.mOrientation == 0 ? 1 : Integer.MIN_VALUE;
                        }
                        if (focusDirection != 130) {
                            return Integer.MIN_VALUE;
                        }
                        return this.mOrientation == 1 ? 1 : Integer.MIN_VALUE;
                    } else if (this.mOrientation == 1) {
                        return -1;
                    } else {
                        return Integer.MIN_VALUE;
                    }
                } else if (this.mOrientation == 0) {
                    return -1;
                } else {
                    return Integer.MIN_VALUE;
                }
            } else if (this.mOrientation != 1 && isLayoutRTL()) {
                return -1;
            } else {
                return 1;
            }
        } else if (this.mOrientation != 1 && isLayoutRTL()) {
            return 1;
        } else {
            return -1;
        }
    }

    private View getChildClosestToStart() {
        return getChildAt(this.mShouldReverseLayout ? getChildCount() - 1 : 0);
    }

    private View getChildClosestToEnd() {
        return getChildAt(this.mShouldReverseLayout ? 0 : getChildCount() - 1);
    }

    private View findFirstVisibleChildClosestToStart(boolean completelyVisible, boolean acceptPartiallyVisible) {
        if (this.mShouldReverseLayout) {
            return findOneVisibleChild(getChildCount() - 1, -1, completelyVisible, acceptPartiallyVisible);
        }
        return findOneVisibleChild(0, getChildCount(), completelyVisible, acceptPartiallyVisible);
    }

    private View findFirstVisibleChildClosestToEnd(boolean completelyVisible, boolean acceptPartiallyVisible) {
        if (this.mShouldReverseLayout) {
            return findOneVisibleChild(0, getChildCount(), completelyVisible, acceptPartiallyVisible);
        }
        return findOneVisibleChild(getChildCount() - 1, -1, completelyVisible, acceptPartiallyVisible);
    }

    private View findReferenceChildClosestToEnd(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mShouldReverseLayout) {
            return findFirstReferenceChild(recycler, state);
        }
        return findLastReferenceChild(recycler, state);
    }

    private View findReferenceChildClosestToStart(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (this.mShouldReverseLayout) {
            return findLastReferenceChild(recycler, state);
        }
        return findFirstReferenceChild(recycler, state);
    }

    private View findFirstReferenceChild(RecyclerView.Recycler recycler, RecyclerView.State state) {
        return findReferenceChild(recycler, state, 0, getChildCount(), state.getItemCount());
    }

    private View findLastReferenceChild(RecyclerView.Recycler recycler, RecyclerView.State state) {
        return findReferenceChild(recycler, state, getChildCount() - 1, -1, state.getItemCount());
    }

    /* access modifiers changed from: package-private */
    public View findReferenceChild(RecyclerView.Recycler recycler, RecyclerView.State state, int start, int end, int itemCount) {
        ensureLayoutState();
        View invalidMatch = null;
        View outOfBoundsMatch = null;
        int boundsStart = this.mOrientationHelper.getStartAfterPadding();
        int boundsEnd = this.mOrientationHelper.getEndAfterPadding();
        int diff = end > start ? 1 : -1;
        for (int i = start; i != end; i += diff) {
            View view = getChildAt(i);
            int position = getPosition(view);
            if (position >= 0 && position < itemCount) {
                if (((RecyclerView.LayoutParams) view.getLayoutParams()).isItemRemoved()) {
                    if (invalidMatch == null) {
                        invalidMatch = view;
                    }
                } else if (this.mOrientationHelper.getDecoratedStart(view) < boundsEnd && this.mOrientationHelper.getDecoratedEnd(view) >= boundsStart) {
                    return view;
                } else {
                    if (outOfBoundsMatch == null) {
                        outOfBoundsMatch = view;
                    }
                }
            }
        }
        return outOfBoundsMatch != null ? outOfBoundsMatch : invalidMatch;
    }

    public int findFirstVisibleItemPosition() {
        View child = findOneVisibleChild(0, getChildCount(), false, true);
        if (child == null) {
            return -1;
        }
        return getPosition(child);
    }

    public int findFirstCompletelyVisibleItemPosition() {
        View child = findOneVisibleChild(0, getChildCount(), true, false);
        if (child == null) {
            return -1;
        }
        return getPosition(child);
    }

    public int findLastVisibleItemPosition() {
        View child = findOneVisibleChild(getChildCount() - 1, -1, false, true);
        if (child == null) {
            return -1;
        }
        return getPosition(child);
    }

    public int findLastCompletelyVisibleItemPosition() {
        View child = findOneVisibleChild(getChildCount() - 1, -1, true, false);
        if (child == null) {
            return -1;
        }
        return getPosition(child);
    }

    /* access modifiers changed from: package-private */
    public View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible, boolean acceptPartiallyVisible) {
        ensureLayoutState();
        int start = this.mOrientationHelper.getStartAfterPadding();
        int end = this.mOrientationHelper.getEndAfterPadding();
        int next = toIndex > fromIndex ? 1 : -1;
        View partiallyVisible = null;
        for (int i = fromIndex; i != toIndex; i += next) {
            View child = getChildAt(i);
            int childStart = this.mOrientationHelper.getDecoratedStart(child);
            int childEnd = this.mOrientationHelper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (!completelyVisible) {
                    return child;
                }
                if (childStart >= start && childEnd <= end) {
                    return child;
                }
                if (acceptPartiallyVisible && partiallyVisible == null) {
                    partiallyVisible = child;
                }
            }
        }
        return partiallyVisible;
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int layoutDir;
        View referenceChild;
        View nextFocus;
        resolveShouldLayoutReverse();
        if (getChildCount() == 0 || (layoutDir = convertFocusDirectionToLayoutDirection(focusDirection)) == Integer.MIN_VALUE) {
            return null;
        }
        ensureLayoutState();
        if (layoutDir == -1) {
            referenceChild = findReferenceChildClosestToStart(recycler, state);
        } else {
            referenceChild = findReferenceChildClosestToEnd(recycler, state);
        }
        if (referenceChild == null) {
            return null;
        }
        ensureLayoutState();
        updateLayoutState(layoutDir, (int) (((float) this.mOrientationHelper.getTotalSpace()) * MAX_SCROLL_FACTOR), false, state);
        LayoutState layoutState = this.mLayoutState;
        layoutState.mScrollingOffset = Integer.MIN_VALUE;
        layoutState.mRecycle = false;
        fill(recycler, layoutState, state, true);
        if (layoutDir == -1) {
            nextFocus = getChildClosestToStart();
        } else {
            nextFocus = getChildClosestToEnd();
        }
        if (nextFocus == referenceChild || !nextFocus.isFocusable()) {
            return null;
        }
        return nextFocus;
    }

    private void logChildren() {
        Log.d(TAG, "internal representation of views on the screen");
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            Log.d(TAG, "item " + getPosition(child) + ", coord:" + this.mOrientationHelper.getDecoratedStart(child));
        }
        Log.d(TAG, "==============");
    }

    /* access modifiers changed from: package-private */
    public void validateChildOrder() {
        Log.d(TAG, "validating child count " + getChildCount());
        if (getChildCount() >= 1) {
            boolean z = false;
            int lastPos = getPosition(getChildAt(0));
            int lastScreenLoc = this.mOrientationHelper.getDecoratedStart(getChildAt(0));
            if (this.mShouldReverseLayout) {
                for (int i = 1; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    int pos = getPosition(child);
                    int screenLoc = this.mOrientationHelper.getDecoratedStart(child);
                    if (pos < lastPos) {
                        logChildren();
                        StringBuilder sb = new StringBuilder();
                        sb.append("detected invalid position. loc invalid? ");
                        if (screenLoc < lastScreenLoc) {
                            z = true;
                        }
                        sb.append(z);
                        throw new RuntimeException(sb.toString());
                    } else if (screenLoc > lastScreenLoc) {
                        logChildren();
                        throw new RuntimeException("detected invalid location");
                    }
                }
                return;
            }
            for (int i2 = 1; i2 < getChildCount(); i2++) {
                View child2 = getChildAt(i2);
                int pos2 = getPosition(child2);
                int screenLoc2 = this.mOrientationHelper.getDecoratedStart(child2);
                if (pos2 < lastPos) {
                    logChildren();
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("detected invalid position. loc invalid? ");
                    if (screenLoc2 < lastScreenLoc) {
                        z = true;
                    }
                    sb2.append(z);
                    throw new RuntimeException(sb2.toString());
                } else if (screenLoc2 < lastScreenLoc) {
                    logChildren();
                    throw new RuntimeException("detected invalid location");
                }
            }
        }
    }

    @Override // com.android.internal.widget.RecyclerView.LayoutManager
    public boolean supportsPredictiveItemAnimations() {
        return this.mPendingSavedState == null && this.mLastStackFromEnd == this.mStackFromEnd;
    }

    @Override // com.android.internal.widget.helper.ItemTouchHelper.ViewDropHandler
    public void prepareForDrop(View view, View target, int x, int y) {
        int dropDirection;
        assertNotInLayoutOrScroll("Cannot drop a view during a scroll or layout calculation");
        ensureLayoutState();
        resolveShouldLayoutReverse();
        int myPos = getPosition(view);
        int targetPos = getPosition(target);
        if (myPos < targetPos) {
            dropDirection = 1;
        } else {
            dropDirection = -1;
        }
        if (this.mShouldReverseLayout) {
            if (dropDirection == 1) {
                scrollToPositionWithOffset(targetPos, this.mOrientationHelper.getEndAfterPadding() - (this.mOrientationHelper.getDecoratedStart(target) + this.mOrientationHelper.getDecoratedMeasurement(view)));
            } else {
                scrollToPositionWithOffset(targetPos, this.mOrientationHelper.getEndAfterPadding() - this.mOrientationHelper.getDecoratedEnd(target));
            }
        } else if (dropDirection == -1) {
            scrollToPositionWithOffset(targetPos, this.mOrientationHelper.getDecoratedStart(target));
        } else {
            scrollToPositionWithOffset(targetPos, this.mOrientationHelper.getDecoratedEnd(target) - this.mOrientationHelper.getDecoratedMeasurement(view));
        }
    }

    /* access modifiers changed from: package-private */
    public static class LayoutState {
        static final int INVALID_LAYOUT = Integer.MIN_VALUE;
        static final int ITEM_DIRECTION_HEAD = -1;
        static final int ITEM_DIRECTION_TAIL = 1;
        static final int LAYOUT_END = 1;
        static final int LAYOUT_START = -1;
        static final int SCROLLING_OFFSET_NaN = Integer.MIN_VALUE;
        static final String TAG = "LLM#LayoutState";
        int mAvailable;
        int mCurrentPosition;
        int mExtra = 0;
        boolean mInfinite;
        boolean mIsPreLayout = false;
        int mItemDirection;
        int mLastScrollDelta;
        int mLayoutDirection;
        int mOffset;
        boolean mRecycle = true;
        List<RecyclerView.ViewHolder> mScrapList = null;
        int mScrollingOffset;

        LayoutState() {
        }

        /* access modifiers changed from: package-private */
        public boolean hasMore(RecyclerView.State state) {
            int i = this.mCurrentPosition;
            return i >= 0 && i < state.getItemCount();
        }

        /* access modifiers changed from: package-private */
        public View next(RecyclerView.Recycler recycler) {
            if (this.mScrapList != null) {
                return nextViewFromScrapList();
            }
            View view = recycler.getViewForPosition(this.mCurrentPosition);
            this.mCurrentPosition += this.mItemDirection;
            return view;
        }

        private View nextViewFromScrapList() {
            int size = this.mScrapList.size();
            for (int i = 0; i < size; i++) {
                View view = this.mScrapList.get(i).itemView;
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (!lp.isItemRemoved() && this.mCurrentPosition == lp.getViewLayoutPosition()) {
                    assignPositionFromScrapList(view);
                    return view;
                }
            }
            return null;
        }

        public void assignPositionFromScrapList() {
            assignPositionFromScrapList(null);
        }

        public void assignPositionFromScrapList(View ignore) {
            View closest = nextViewInLimitedList(ignore);
            if (closest == null) {
                this.mCurrentPosition = -1;
            } else {
                this.mCurrentPosition = ((RecyclerView.LayoutParams) closest.getLayoutParams()).getViewLayoutPosition();
            }
        }

        public View nextViewInLimitedList(View ignore) {
            int distance;
            int size = this.mScrapList.size();
            View closest = null;
            int closestDistance = Integer.MAX_VALUE;
            for (int i = 0; i < size; i++) {
                View view = this.mScrapList.get(i).itemView;
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                if (view != ignore && !lp.isItemRemoved() && (distance = (lp.getViewLayoutPosition() - this.mCurrentPosition) * this.mItemDirection) >= 0 && distance < closestDistance) {
                    closest = view;
                    closestDistance = distance;
                    if (distance == 0) {
                        break;
                    }
                }
            }
            return closest;
        }

        /* access modifiers changed from: package-private */
        public void log() {
            Log.d(TAG, "avail:" + this.mAvailable + ", ind:" + this.mCurrentPosition + ", dir:" + this.mItemDirection + ", offset:" + this.mOffset + ", layoutDir:" + this.mLayoutDirection);
        }
    }

    public static class SavedState implements Parcelable {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class com.android.internal.widget.LinearLayoutManager.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean mAnchorLayoutFromEnd;
        int mAnchorOffset;
        int mAnchorPosition;

        public SavedState() {
        }

        SavedState(Parcel in) {
            this.mAnchorPosition = in.readInt();
            this.mAnchorOffset = in.readInt();
            this.mAnchorLayoutFromEnd = in.readInt() != 1 ? false : true;
        }

        public SavedState(SavedState other) {
            this.mAnchorPosition = other.mAnchorPosition;
            this.mAnchorOffset = other.mAnchorOffset;
            this.mAnchorLayoutFromEnd = other.mAnchorLayoutFromEnd;
        }

        /* access modifiers changed from: package-private */
        public boolean hasValidAnchor() {
            return this.mAnchorPosition >= 0;
        }

        /* access modifiers changed from: package-private */
        public void invalidateAnchor() {
            this.mAnchorPosition = -1;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mAnchorPosition);
            dest.writeInt(this.mAnchorOffset);
            dest.writeInt(this.mAnchorLayoutFromEnd ? 1 : 0);
        }
    }

    /* access modifiers changed from: package-private */
    public class AnchorInfo {
        int mCoordinate;
        boolean mLayoutFromEnd;
        int mPosition;
        boolean mValid;

        AnchorInfo() {
            reset();
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.mPosition = -1;
            this.mCoordinate = Integer.MIN_VALUE;
            this.mLayoutFromEnd = false;
            this.mValid = false;
        }

        /* access modifiers changed from: package-private */
        public void assignCoordinateFromPadding() {
            int i;
            if (this.mLayoutFromEnd) {
                i = LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding();
            } else {
                i = LinearLayoutManager.this.mOrientationHelper.getStartAfterPadding();
            }
            this.mCoordinate = i;
        }

        public String toString() {
            return "AnchorInfo{mPosition=" + this.mPosition + ", mCoordinate=" + this.mCoordinate + ", mLayoutFromEnd=" + this.mLayoutFromEnd + ", mValid=" + this.mValid + '}';
        }

        /* access modifiers changed from: package-private */
        public boolean isViewValidAsAnchor(View child, RecyclerView.State state) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            return !lp.isItemRemoved() && lp.getViewLayoutPosition() >= 0 && lp.getViewLayoutPosition() < state.getItemCount();
        }

        public void assignFromViewAndKeepVisibleRect(View child) {
            int endMargin;
            int spaceChange = LinearLayoutManager.this.mOrientationHelper.getTotalSpaceChange();
            if (spaceChange >= 0) {
                assignFromView(child);
                return;
            }
            this.mPosition = LinearLayoutManager.this.getPosition(child);
            if (this.mLayoutFromEnd) {
                int previousEndMargin = (LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding() - spaceChange) - LinearLayoutManager.this.mOrientationHelper.getDecoratedEnd(child);
                this.mCoordinate = LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding() - previousEndMargin;
                if (previousEndMargin > 0) {
                    int childSize = LinearLayoutManager.this.mOrientationHelper.getDecoratedMeasurement(child);
                    int layoutStart = LinearLayoutManager.this.mOrientationHelper.getStartAfterPadding();
                    int startMargin = (this.mCoordinate - childSize) - (Math.min(LinearLayoutManager.this.mOrientationHelper.getDecoratedStart(child) - layoutStart, 0) + layoutStart);
                    if (startMargin < 0) {
                        this.mCoordinate += Math.min(previousEndMargin, -startMargin);
                        return;
                    }
                    return;
                }
                return;
            }
            int childStart = LinearLayoutManager.this.mOrientationHelper.getDecoratedStart(child);
            int startMargin2 = childStart - LinearLayoutManager.this.mOrientationHelper.getStartAfterPadding();
            this.mCoordinate = childStart;
            if (startMargin2 > 0 && (endMargin = (LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding() - Math.min(0, (LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding() - spaceChange) - LinearLayoutManager.this.mOrientationHelper.getDecoratedEnd(child))) - (LinearLayoutManager.this.mOrientationHelper.getDecoratedMeasurement(child) + childStart)) < 0) {
                this.mCoordinate -= Math.min(startMargin2, -endMargin);
            }
        }

        public void assignFromView(View child) {
            if (this.mLayoutFromEnd) {
                this.mCoordinate = LinearLayoutManager.this.mOrientationHelper.getDecoratedEnd(child) + LinearLayoutManager.this.mOrientationHelper.getTotalSpaceChange();
            } else {
                this.mCoordinate = LinearLayoutManager.this.mOrientationHelper.getDecoratedStart(child);
            }
            this.mPosition = LinearLayoutManager.this.getPosition(child);
        }
    }

    /* access modifiers changed from: protected */
    public static class LayoutChunkResult {
        public int mConsumed;
        public boolean mFinished;
        public boolean mFocusable;
        public boolean mIgnoreConsumed;

        protected LayoutChunkResult() {
        }

        /* access modifiers changed from: package-private */
        public void resetInternal() {
            this.mConsumed = 0;
            this.mFinished = false;
            this.mIgnoreConsumed = false;
            this.mFocusable = false;
        }
    }
}
