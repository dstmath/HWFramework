package com.android.internal.widget;

import android.content.Context;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.widget.RecyclerView.LayoutManager;
import com.android.internal.widget.RecyclerView.LayoutManager.LayoutPrefetchRegistry;
import com.android.internal.widget.RecyclerView.LayoutManager.Properties;
import com.android.internal.widget.RecyclerView.LayoutParams;
import com.android.internal.widget.RecyclerView.Recycler;
import com.android.internal.widget.RecyclerView.SmoothScroller.ScrollVectorProvider;
import com.android.internal.widget.RecyclerView.State;
import com.android.internal.widget.RecyclerView.ViewHolder;
import com.android.internal.widget.helper.ItemTouchHelper.ViewDropHandler;
import java.util.List;

public class LinearLayoutManager extends LayoutManager implements ViewDropHandler, ScrollVectorProvider {
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

    class AnchorInfo {
        int mCoordinate;
        boolean mLayoutFromEnd;
        int mPosition;
        boolean mValid;

        AnchorInfo() {
            reset();
        }

        void reset() {
            this.mPosition = -1;
            this.mCoordinate = Integer.MIN_VALUE;
            this.mLayoutFromEnd = false;
            this.mValid = false;
        }

        void assignCoordinateFromPadding() {
            int endAfterPadding;
            if (this.mLayoutFromEnd) {
                endAfterPadding = LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding();
            } else {
                endAfterPadding = LinearLayoutManager.this.mOrientationHelper.getStartAfterPadding();
            }
            this.mCoordinate = endAfterPadding;
        }

        public String toString() {
            return "AnchorInfo{mPosition=" + this.mPosition + ", mCoordinate=" + this.mCoordinate + ", mLayoutFromEnd=" + this.mLayoutFromEnd + ", mValid=" + this.mValid + '}';
        }

        boolean isViewValidAsAnchor(View child, State state) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.isItemRemoved() || lp.getViewLayoutPosition() < 0 || lp.getViewLayoutPosition() >= state.getItemCount()) {
                return false;
            }
            return true;
        }

        public void assignFromViewAndKeepVisibleRect(View child) {
            int spaceChange = LinearLayoutManager.this.mOrientationHelper.getTotalSpaceChange();
            if (spaceChange >= 0) {
                assignFromView(child);
                return;
            }
            this.mPosition = LinearLayoutManager.this.getPosition(child);
            int startMargin;
            if (this.mLayoutFromEnd) {
                int previousEndMargin = (LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding() - spaceChange) - LinearLayoutManager.this.mOrientationHelper.getDecoratedEnd(child);
                this.mCoordinate = LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding() - previousEndMargin;
                if (previousEndMargin > 0) {
                    int estimatedChildStart = this.mCoordinate - LinearLayoutManager.this.mOrientationHelper.getDecoratedMeasurement(child);
                    int layoutStart = LinearLayoutManager.this.mOrientationHelper.getStartAfterPadding();
                    startMargin = estimatedChildStart - (layoutStart + Math.min(LinearLayoutManager.this.mOrientationHelper.getDecoratedStart(child) - layoutStart, 0));
                    if (startMargin < 0) {
                        this.mCoordinate += Math.min(previousEndMargin, -startMargin);
                    }
                }
            } else {
                int childStart = LinearLayoutManager.this.mOrientationHelper.getDecoratedStart(child);
                startMargin = childStart - LinearLayoutManager.this.mOrientationHelper.getStartAfterPadding();
                this.mCoordinate = childStart;
                if (startMargin > 0) {
                    int endMargin = (LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding() - Math.min(0, (LinearLayoutManager.this.mOrientationHelper.getEndAfterPadding() - spaceChange) - LinearLayoutManager.this.mOrientationHelper.getDecoratedEnd(child))) - (childStart + LinearLayoutManager.this.mOrientationHelper.getDecoratedMeasurement(child));
                    if (endMargin < 0) {
                        this.mCoordinate -= Math.min(startMargin, -endMargin);
                    }
                }
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

    protected static class LayoutChunkResult {
        public int mConsumed;
        public boolean mFinished;
        public boolean mFocusable;
        public boolean mIgnoreConsumed;

        protected LayoutChunkResult() {
        }

        void resetInternal() {
            this.mConsumed = 0;
            this.mFinished = false;
            this.mIgnoreConsumed = false;
            this.mFocusable = false;
        }
    }

    static class LayoutState {
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
        List<ViewHolder> mScrapList = null;
        int mScrollingOffset;

        LayoutState() {
        }

        boolean hasMore(State state) {
            return this.mCurrentPosition >= 0 && this.mCurrentPosition < state.getItemCount();
        }

        View next(Recycler recycler) {
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
                View view = ((ViewHolder) this.mScrapList.get(i)).itemView;
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
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
                this.mCurrentPosition = ((LayoutParams) closest.getLayoutParams()).getViewLayoutPosition();
            }
        }

        public View nextViewInLimitedList(View ignore) {
            int size = this.mScrapList.size();
            View closest = null;
            int closestDistance = Integer.MAX_VALUE;
            for (int i = 0; i < size; i++) {
                View view = ((ViewHolder) this.mScrapList.get(i)).itemView;
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                if (!(view == ignore || lp.isItemRemoved())) {
                    int distance = (lp.getViewLayoutPosition() - this.mCurrentPosition) * this.mItemDirection;
                    if (distance >= 0 && distance < closestDistance) {
                        closest = view;
                        closestDistance = distance;
                        if (distance == 0) {
                            break;
                        }
                    }
                }
            }
            return closest;
        }

        void log() {
            Log.d(TAG, "avail:" + this.mAvailable + ", ind:" + this.mCurrentPosition + ", dir:" + this.mItemDirection + ", offset:" + this.mOffset + ", layoutDir:" + this.mLayoutDirection);
        }
    }

    public static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean mAnchorLayoutFromEnd;
        int mAnchorOffset;
        int mAnchorPosition;

        SavedState(Parcel in) {
            boolean z = true;
            this.mAnchorPosition = in.readInt();
            this.mAnchorOffset = in.readInt();
            if (in.readInt() != 1) {
                z = false;
            }
            this.mAnchorLayoutFromEnd = z;
        }

        public SavedState(SavedState other) {
            this.mAnchorPosition = other.mAnchorPosition;
            this.mAnchorOffset = other.mAnchorOffset;
            this.mAnchorLayoutFromEnd = other.mAnchorLayoutFromEnd;
        }

        boolean hasValidAnchor() {
            return this.mAnchorPosition >= 0;
        }

        void invalidateAnchor() {
            this.mAnchorPosition = -1;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mAnchorPosition);
            dest.writeInt(this.mAnchorOffset);
            dest.writeInt(this.mAnchorLayoutFromEnd ? 1 : 0);
        }
    }

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
        Properties properties = LayoutManager.getProperties(context, attrs, defStyleAttr, defStyleRes);
        setOrientation(properties.orientation);
        setReverseLayout(properties.reverseLayout);
        setStackFromEnd(properties.stackFromEnd);
        setAutoMeasureEnabled(true);
    }

    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    public boolean getRecycleChildrenOnDetach() {
        return this.mRecycleChildrenOnDetach;
    }

    public void setRecycleChildrenOnDetach(boolean recycleChildrenOnDetach) {
        this.mRecycleChildrenOnDetach = recycleChildrenOnDetach;
    }

    public void onDetachedFromWindow(RecyclerView view, Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        if (this.mRecycleChildrenOnDetach) {
            removeAndRecycleAllViews(recycler);
            recycler.clear();
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        if (getChildCount() > 0) {
            event.setFromIndex(findFirstVisibleItemPosition());
            event.setToIndex(findLastVisibleItemPosition());
        }
    }

    public Parcelable onSaveInstanceState() {
        if (this.mPendingSavedState != null) {
            return new SavedState(this.mPendingSavedState);
        }
        SavedState state = new SavedState();
        if (getChildCount() > 0) {
            ensureLayoutState();
            boolean didLayoutFromEnd = this.mLastStackFromEnd ^ this.mShouldReverseLayout;
            state.mAnchorLayoutFromEnd = didLayoutFromEnd;
            View refChild;
            if (didLayoutFromEnd) {
                refChild = getChildClosestToEnd();
                state.mAnchorOffset = this.mOrientationHelper.getEndAfterPadding() - this.mOrientationHelper.getDecoratedEnd(refChild);
                state.mAnchorPosition = getPosition(refChild);
            } else {
                refChild = getChildClosestToStart();
                state.mAnchorPosition = getPosition(refChild);
                state.mAnchorOffset = this.mOrientationHelper.getDecoratedStart(refChild) - this.mOrientationHelper.getStartAfterPadding();
            }
        } else {
            state.invalidateAnchor();
        }
        return state;
    }

    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            this.mPendingSavedState = (SavedState) state;
            requestLayout();
        }
    }

    public boolean canScrollHorizontally() {
        return this.mOrientation == 0;
    }

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
        if (this.mOrientation == 1 || (isLayoutRTL() ^ 1) != 0) {
            this.mShouldReverseLayout = this.mReverseLayout;
        } else {
            this.mShouldReverseLayout = this.mReverseLayout ^ 1;
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

    protected int getExtraLayoutSpace(State state) {
        if (state.hasTargetScrollPosition()) {
            return this.mOrientationHelper.getTotalSpace();
        }
        return 0;
    }

    public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    public PointF computeScrollVectorForPosition(int targetPosition) {
        boolean z = false;
        if (getChildCount() == 0) {
            return null;
        }
        if (targetPosition < getPosition(getChildAt(0))) {
            z = true;
        }
        int direction = z != this.mShouldReverseLayout ? -1 : 1;
        if (this.mOrientation == 0) {
            return new PointF((float) direction, 0.0f);
        }
        return new PointF(0.0f, (float) direction);
    }

    public void onLayoutChildren(Recycler recycler, State state) {
        if (!(this.mPendingSavedState == null && this.mPendingScrollPosition == -1) && state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }
        int extraForEnd;
        int extraForStart;
        int firstLayoutDirection;
        int startOffset;
        int endOffset;
        if (this.mPendingSavedState != null && this.mPendingSavedState.hasValidAnchor()) {
            this.mPendingScrollPosition = this.mPendingSavedState.mAnchorPosition;
        }
        ensureLayoutState();
        this.mLayoutState.mRecycle = false;
        resolveShouldLayoutReverse();
        if (!(this.mAnchorInfo.mValid && this.mPendingScrollPosition == -1 && this.mPendingSavedState == null)) {
            this.mAnchorInfo.reset();
            this.mAnchorInfo.mLayoutFromEnd = this.mShouldReverseLayout ^ this.mStackFromEnd;
            updateAnchorInfoForLayout(recycler, state, this.mAnchorInfo);
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
        extraForStart += this.mOrientationHelper.getStartAfterPadding();
        extraForEnd += this.mOrientationHelper.getEndPadding();
        if (!(!state.isPreLayout() || this.mPendingScrollPosition == -1 || this.mPendingScrollPositionOffset == Integer.MIN_VALUE)) {
            View existing = findViewByPosition(this.mPendingScrollPosition);
            if (existing != null) {
                int upcomingOffset;
                if (this.mShouldReverseLayout) {
                    upcomingOffset = (this.mOrientationHelper.getEndAfterPadding() - this.mOrientationHelper.getDecoratedEnd(existing)) - this.mPendingScrollPositionOffset;
                } else {
                    upcomingOffset = this.mPendingScrollPositionOffset - (this.mOrientationHelper.getDecoratedStart(existing) - this.mOrientationHelper.getStartAfterPadding());
                }
                if (upcomingOffset > 0) {
                    extraForStart += upcomingOffset;
                } else {
                    extraForEnd -= upcomingOffset;
                }
            }
        }
        if (this.mAnchorInfo.mLayoutFromEnd) {
            if (this.mShouldReverseLayout) {
                firstLayoutDirection = 1;
            } else {
                firstLayoutDirection = -1;
            }
        } else if (this.mShouldReverseLayout) {
            firstLayoutDirection = -1;
        } else {
            firstLayoutDirection = 1;
        }
        onAnchorReady(recycler, state, this.mAnchorInfo, firstLayoutDirection);
        detachAndScrapAttachedViews(recycler);
        this.mLayoutState.mInfinite = resolveIsInfinite();
        this.mLayoutState.mIsPreLayout = state.isPreLayout();
        LayoutState layoutState;
        if (this.mAnchorInfo.mLayoutFromEnd) {
            updateLayoutStateToFillStart(this.mAnchorInfo);
            this.mLayoutState.mExtra = extraForStart;
            fill(recycler, this.mLayoutState, state, false);
            startOffset = this.mLayoutState.mOffset;
            int firstElement = this.mLayoutState.mCurrentPosition;
            if (this.mLayoutState.mAvailable > 0) {
                extraForEnd += this.mLayoutState.mAvailable;
            }
            updateLayoutStateToFillEnd(this.mAnchorInfo);
            this.mLayoutState.mExtra = extraForEnd;
            layoutState = this.mLayoutState;
            layoutState.mCurrentPosition += this.mLayoutState.mItemDirection;
            fill(recycler, this.mLayoutState, state, false);
            endOffset = this.mLayoutState.mOffset;
            if (this.mLayoutState.mAvailable > 0) {
                extraForStart = this.mLayoutState.mAvailable;
                updateLayoutStateToFillStart(firstElement, startOffset);
                this.mLayoutState.mExtra = extraForStart;
                fill(recycler, this.mLayoutState, state, false);
                startOffset = this.mLayoutState.mOffset;
            }
        } else {
            updateLayoutStateToFillEnd(this.mAnchorInfo);
            this.mLayoutState.mExtra = extraForEnd;
            fill(recycler, this.mLayoutState, state, false);
            endOffset = this.mLayoutState.mOffset;
            int lastElement = this.mLayoutState.mCurrentPosition;
            if (this.mLayoutState.mAvailable > 0) {
                extraForStart += this.mLayoutState.mAvailable;
            }
            updateLayoutStateToFillStart(this.mAnchorInfo);
            this.mLayoutState.mExtra = extraForStart;
            layoutState = this.mLayoutState;
            layoutState.mCurrentPosition += this.mLayoutState.mItemDirection;
            fill(recycler, this.mLayoutState, state, false);
            startOffset = this.mLayoutState.mOffset;
            if (this.mLayoutState.mAvailable > 0) {
                extraForEnd = this.mLayoutState.mAvailable;
                updateLayoutStateToFillEnd(lastElement, endOffset);
                this.mLayoutState.mExtra = extraForEnd;
                fill(recycler, this.mLayoutState, state, false);
                endOffset = this.mLayoutState.mOffset;
            }
        }
        if (getChildCount() > 0) {
            int fixOffset;
            if ((this.mShouldReverseLayout ^ this.mStackFromEnd) != 0) {
                fixOffset = fixLayoutEndGap(endOffset, recycler, state, true);
                startOffset += fixOffset;
                endOffset += fixOffset;
                fixOffset = fixLayoutStartGap(startOffset, recycler, state, false);
                startOffset += fixOffset;
                endOffset += fixOffset;
            } else {
                fixOffset = fixLayoutStartGap(startOffset, recycler, state, true);
                startOffset += fixOffset;
                endOffset += fixOffset;
                fixOffset = fixLayoutEndGap(endOffset, recycler, state, false);
                startOffset += fixOffset;
                endOffset += fixOffset;
            }
        }
        layoutForPredictiveAnimations(recycler, state, startOffset, endOffset);
        if (state.isPreLayout()) {
            this.mAnchorInfo.reset();
        } else {
            this.mOrientationHelper.onLayoutComplete();
        }
        this.mLastStackFromEnd = this.mStackFromEnd;
    }

    public void onLayoutCompleted(State state) {
        super.onLayoutCompleted(state);
        this.mPendingSavedState = null;
        this.mPendingScrollPosition = -1;
        this.mPendingScrollPositionOffset = Integer.MIN_VALUE;
        this.mAnchorInfo.reset();
    }

    void onAnchorReady(Recycler recycler, State state, AnchorInfo anchorInfo, int firstLayoutItemDirection) {
    }

    private void layoutForPredictiveAnimations(Recycler recycler, State state, int startOffset, int endOffset) {
        if (state.willRunPredictiveAnimations() && getChildCount() != 0 && !state.isPreLayout() && (supportsPredictiveItemAnimations() ^ 1) == 0) {
            int scrapExtraStart = 0;
            int scrapExtraEnd = 0;
            List<ViewHolder> scrapList = recycler.getScrapList();
            int scrapSize = scrapList.size();
            int firstChildPos = getPosition(getChildAt(0));
            for (int i = 0; i < scrapSize; i++) {
                ViewHolder scrap = (ViewHolder) scrapList.get(i);
                if (!scrap.isRemoved()) {
                    if (((scrap.getLayoutPosition() < firstChildPos) != this.mShouldReverseLayout ? -1 : 1) == -1) {
                        scrapExtraStart += this.mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
                    } else {
                        scrapExtraEnd += this.mOrientationHelper.getDecoratedMeasurement(scrap.itemView);
                    }
                }
            }
            this.mLayoutState.mScrapList = scrapList;
            if (scrapExtraStart > 0) {
                updateLayoutStateToFillStart(getPosition(getChildClosestToStart()), startOffset);
                this.mLayoutState.mExtra = scrapExtraStart;
                this.mLayoutState.mAvailable = 0;
                this.mLayoutState.assignPositionFromScrapList();
                fill(recycler, this.mLayoutState, state, false);
            }
            if (scrapExtraEnd > 0) {
                updateLayoutStateToFillEnd(getPosition(getChildClosestToEnd()), endOffset);
                this.mLayoutState.mExtra = scrapExtraEnd;
                this.mLayoutState.mAvailable = 0;
                this.mLayoutState.assignPositionFromScrapList();
                fill(recycler, this.mLayoutState, state, false);
            }
            this.mLayoutState.mScrapList = null;
        }
    }

    private void updateAnchorInfoForLayout(Recycler recycler, State state, AnchorInfo anchorInfo) {
        if (!updateAnchorFromPendingData(state, anchorInfo) && !updateAnchorFromChildren(recycler, state, anchorInfo)) {
            anchorInfo.assignCoordinateFromPadding();
            anchorInfo.mPosition = this.mStackFromEnd ? state.getItemCount() - 1 : 0;
        }
    }

    private boolean updateAnchorFromChildren(Recycler recycler, State state, AnchorInfo anchorInfo) {
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
            View referenceChild;
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
                boolean notVisible = this.mOrientationHelper.getDecoratedStart(referenceChild) < this.mOrientationHelper.getEndAfterPadding() ? this.mOrientationHelper.getDecoratedEnd(referenceChild) < this.mOrientationHelper.getStartAfterPadding() : true;
                if (notVisible) {
                    int endAfterPadding;
                    if (anchorInfo.mLayoutFromEnd) {
                        endAfterPadding = this.mOrientationHelper.getEndAfterPadding();
                    } else {
                        endAfterPadding = this.mOrientationHelper.getStartAfterPadding();
                    }
                    anchorInfo.mCoordinate = endAfterPadding;
                }
            }
            return true;
        }
    }

    private boolean updateAnchorFromPendingData(State state, AnchorInfo anchorInfo) {
        boolean z = false;
        if (state.isPreLayout() || this.mPendingScrollPosition == -1) {
            return false;
        }
        if (this.mPendingScrollPosition < 0 || this.mPendingScrollPosition >= state.getItemCount()) {
            this.mPendingScrollPosition = -1;
            this.mPendingScrollPositionOffset = Integer.MIN_VALUE;
            return false;
        }
        anchorInfo.mPosition = this.mPendingScrollPosition;
        if (this.mPendingSavedState != null && this.mPendingSavedState.hasValidAnchor()) {
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
                    boolean z2;
                    if (this.mPendingScrollPosition < getPosition(getChildAt(0))) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    if (z2 == this.mShouldReverseLayout) {
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
                int decoratedEnd;
                if (anchorInfo.mLayoutFromEnd) {
                    decoratedEnd = this.mOrientationHelper.getDecoratedEnd(child) + this.mOrientationHelper.getTotalSpaceChange();
                } else {
                    decoratedEnd = this.mOrientationHelper.getDecoratedStart(child);
                }
                anchorInfo.mCoordinate = decoratedEnd;
            }
            return true;
        } else {
            anchorInfo.mLayoutFromEnd = this.mShouldReverseLayout;
            if (this.mShouldReverseLayout) {
                anchorInfo.mCoordinate = this.mOrientationHelper.getEndAfterPadding() - this.mPendingScrollPositionOffset;
            } else {
                anchorInfo.mCoordinate = this.mOrientationHelper.getStartAfterPadding() + this.mPendingScrollPositionOffset;
            }
            return true;
        }
    }

    private int fixLayoutEndGap(int endOffset, Recycler recycler, State state, boolean canOffsetChildren) {
        int gap = this.mOrientationHelper.getEndAfterPadding() - endOffset;
        if (gap <= 0) {
            return 0;
        }
        int fixOffset = -scrollBy(-gap, recycler, state);
        endOffset += fixOffset;
        if (canOffsetChildren) {
            gap = this.mOrientationHelper.getEndAfterPadding() - endOffset;
            if (gap > 0) {
                this.mOrientationHelper.offsetChildren(gap);
                return gap + fixOffset;
            }
        }
        return fixOffset;
    }

    private int fixLayoutStartGap(int startOffset, Recycler recycler, State state, boolean canOffsetChildren) {
        int gap = startOffset - this.mOrientationHelper.getStartAfterPadding();
        if (gap <= 0) {
            return 0;
        }
        int fixOffset = -scrollBy(gap, recycler, state);
        startOffset += fixOffset;
        if (canOffsetChildren) {
            gap = startOffset - this.mOrientationHelper.getStartAfterPadding();
            if (gap > 0) {
                this.mOrientationHelper.offsetChildren(-gap);
                return fixOffset - gap;
            }
        }
        return fixOffset;
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
        this.mLayoutState.mCurrentPosition = itemPosition;
        this.mLayoutState.mLayoutDirection = 1;
        this.mLayoutState.mOffset = offset;
        this.mLayoutState.mScrollingOffset = Integer.MIN_VALUE;
    }

    private void updateLayoutStateToFillStart(AnchorInfo anchorInfo) {
        updateLayoutStateToFillStart(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillStart(int itemPosition, int offset) {
        int i;
        this.mLayoutState.mAvailable = offset - this.mOrientationHelper.getStartAfterPadding();
        this.mLayoutState.mCurrentPosition = itemPosition;
        LayoutState layoutState = this.mLayoutState;
        if (this.mShouldReverseLayout) {
            i = 1;
        } else {
            i = -1;
        }
        layoutState.mItemDirection = i;
        this.mLayoutState.mLayoutDirection = -1;
        this.mLayoutState.mOffset = offset;
        this.mLayoutState.mScrollingOffset = Integer.MIN_VALUE;
    }

    protected boolean isLayoutRTL() {
        return getLayoutDirection() == 1;
    }

    void ensureLayoutState() {
        if (this.mLayoutState == null) {
            this.mLayoutState = createLayoutState();
        }
        if (this.mOrientationHelper == null) {
            this.mOrientationHelper = OrientationHelper.createOrientationHelper(this, this.mOrientation);
        }
    }

    LayoutState createLayoutState() {
        return new LayoutState();
    }

    public void scrollToPosition(int position) {
        this.mPendingScrollPosition = position;
        this.mPendingScrollPositionOffset = Integer.MIN_VALUE;
        if (this.mPendingSavedState != null) {
            this.mPendingSavedState.invalidateAnchor();
        }
        requestLayout();
    }

    public void scrollToPositionWithOffset(int position, int offset) {
        this.mPendingScrollPosition = position;
        this.mPendingScrollPositionOffset = offset;
        if (this.mPendingSavedState != null) {
            this.mPendingSavedState.invalidateAnchor();
        }
        requestLayout();
    }

    public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
        if (this.mOrientation == 1) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
        if (this.mOrientation == 0) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    public int computeHorizontalScrollOffset(State state) {
        return computeScrollOffset(state);
    }

    public int computeVerticalScrollOffset(State state) {
        return computeScrollOffset(state);
    }

    public int computeHorizontalScrollExtent(State state) {
        return computeScrollExtent(state);
    }

    public int computeVerticalScrollExtent(State state) {
        return computeScrollExtent(state);
    }

    public int computeHorizontalScrollRange(State state) {
        return computeScrollRange(state);
    }

    public int computeVerticalScrollRange(State state) {
        return computeScrollRange(state);
    }

    private int computeScrollOffset(State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelper.computeScrollOffset(state, this.mOrientationHelper, findFirstVisibleChildClosestToStart(this.mSmoothScrollbarEnabled ^ 1, true), findFirstVisibleChildClosestToEnd(this.mSmoothScrollbarEnabled ^ 1, true), this, this.mSmoothScrollbarEnabled, this.mShouldReverseLayout);
    }

    private int computeScrollExtent(State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelper.computeScrollExtent(state, this.mOrientationHelper, findFirstVisibleChildClosestToStart(this.mSmoothScrollbarEnabled ^ 1, true), findFirstVisibleChildClosestToEnd(this.mSmoothScrollbarEnabled ^ 1, true), this, this.mSmoothScrollbarEnabled);
    }

    private int computeScrollRange(State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        ensureLayoutState();
        return ScrollbarHelper.computeScrollRange(state, this.mOrientationHelper, findFirstVisibleChildClosestToStart(this.mSmoothScrollbarEnabled ^ 1, true), findFirstVisibleChildClosestToEnd(this.mSmoothScrollbarEnabled ^ 1, true), this, this.mSmoothScrollbarEnabled);
    }

    public void setSmoothScrollbarEnabled(boolean enabled) {
        this.mSmoothScrollbarEnabled = enabled;
    }

    public boolean isSmoothScrollbarEnabled() {
        return this.mSmoothScrollbarEnabled;
    }

    private void updateLayoutState(int layoutDirection, int requiredSpace, boolean canUseExistingSpace, State state) {
        int scrollingOffset;
        int i = -1;
        int i2 = 1;
        this.mLayoutState.mInfinite = resolveIsInfinite();
        this.mLayoutState.mExtra = getExtraLayoutSpace(state);
        this.mLayoutState.mLayoutDirection = layoutDirection;
        LayoutState layoutState;
        View child;
        if (layoutDirection == 1) {
            layoutState = this.mLayoutState;
            layoutState.mExtra += this.mOrientationHelper.getEndPadding();
            child = getChildClosestToEnd();
            layoutState = this.mLayoutState;
            if (!this.mShouldReverseLayout) {
                i = 1;
            }
            layoutState.mItemDirection = i;
            this.mLayoutState.mCurrentPosition = getPosition(child) + this.mLayoutState.mItemDirection;
            this.mLayoutState.mOffset = this.mOrientationHelper.getDecoratedEnd(child);
            scrollingOffset = this.mOrientationHelper.getDecoratedEnd(child) - this.mOrientationHelper.getEndAfterPadding();
        } else {
            child = getChildClosestToStart();
            layoutState = this.mLayoutState;
            layoutState.mExtra += this.mOrientationHelper.getStartAfterPadding();
            layoutState = this.mLayoutState;
            if (!this.mShouldReverseLayout) {
                i2 = -1;
            }
            layoutState.mItemDirection = i2;
            this.mLayoutState.mCurrentPosition = getPosition(child) + this.mLayoutState.mItemDirection;
            this.mLayoutState.mOffset = this.mOrientationHelper.getDecoratedStart(child);
            scrollingOffset = (-this.mOrientationHelper.getDecoratedStart(child)) + this.mOrientationHelper.getStartAfterPadding();
        }
        this.mLayoutState.mAvailable = requiredSpace;
        if (canUseExistingSpace) {
            LayoutState layoutState2 = this.mLayoutState;
            layoutState2.mAvailable -= scrollingOffset;
        }
        this.mLayoutState.mScrollingOffset = scrollingOffset;
    }

    boolean resolveIsInfinite() {
        if (this.mOrientationHelper.getMode() == 0 && this.mOrientationHelper.getEnd() == 0) {
            return true;
        }
        return false;
    }

    void collectPrefetchPositionsForLayoutState(State state, LayoutState layoutState, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int pos = layoutState.mCurrentPosition;
        if (pos >= 0 && pos < state.getItemCount()) {
            layoutPrefetchRegistry.addPosition(pos, layoutState.mScrollingOffset);
        }
    }

    public void collectInitialPrefetchPositions(int adapterItemCount, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        boolean fromEnd;
        int anchorPos;
        int direction;
        if (this.mPendingSavedState == null || !this.mPendingSavedState.hasValidAnchor()) {
            resolveShouldLayoutReverse();
            fromEnd = this.mShouldReverseLayout;
            anchorPos = this.mPendingScrollPosition == -1 ? fromEnd ? adapterItemCount - 1 : 0 : this.mPendingScrollPosition;
        } else {
            fromEnd = this.mPendingSavedState.mAnchorLayoutFromEnd;
            anchorPos = this.mPendingSavedState.mAnchorPosition;
        }
        if (fromEnd) {
            direction = -1;
        } else {
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

    public void collectAdjacentPrefetchPositions(int dx, int dy, State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        int delta = this.mOrientation == 0 ? dx : dy;
        if (getChildCount() != 0 && delta != 0) {
            updateLayoutState(delta > 0 ? 1 : -1, Math.abs(delta), true, state);
            collectPrefetchPositionsForLayoutState(state, this.mLayoutState, layoutPrefetchRegistry);
        }
    }

    int scrollBy(int dy, Recycler recycler, State state) {
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

    public void assertNotInLayoutOrScroll(String message) {
        if (this.mPendingSavedState == null) {
            super.assertNotInLayoutOrScroll(message);
        }
    }

    private void recycleChildren(Recycler recycler, int startIndex, int endIndex) {
        if (startIndex != endIndex) {
            int i;
            if (endIndex > startIndex) {
                for (i = endIndex - 1; i >= startIndex; i--) {
                    removeAndRecycleViewAt(i, recycler);
                }
            } else {
                for (i = startIndex; i > endIndex; i--) {
                    removeAndRecycleViewAt(i, recycler);
                }
            }
        }
    }

    private void recycleViewsFromStart(Recycler recycler, int dt) {
        if (dt >= 0) {
            int limit = dt;
            int childCount = getChildCount();
            int i;
            View child;
            if (this.mShouldReverseLayout) {
                for (i = childCount - 1; i >= 0; i--) {
                    child = getChildAt(i);
                    if (this.mOrientationHelper.getDecoratedEnd(child) > dt || this.mOrientationHelper.getTransformedEndWithDecoration(child) > dt) {
                        recycleChildren(recycler, childCount - 1, i);
                        return;
                    }
                }
            } else {
                for (i = 0; i < childCount; i++) {
                    child = getChildAt(i);
                    if (this.mOrientationHelper.getDecoratedEnd(child) > dt || this.mOrientationHelper.getTransformedEndWithDecoration(child) > dt) {
                        recycleChildren(recycler, 0, i);
                        return;
                    }
                }
            }
        }
    }

    private void recycleViewsFromEnd(Recycler recycler, int dt) {
        int childCount = getChildCount();
        if (dt >= 0) {
            int limit = this.mOrientationHelper.getEnd() - dt;
            int i;
            View child;
            if (this.mShouldReverseLayout) {
                for (i = 0; i < childCount; i++) {
                    child = getChildAt(i);
                    if (this.mOrientationHelper.getDecoratedStart(child) < limit || this.mOrientationHelper.getTransformedStartWithDecoration(child) < limit) {
                        recycleChildren(recycler, 0, i);
                        return;
                    }
                }
            } else {
                for (i = childCount - 1; i >= 0; i--) {
                    child = getChildAt(i);
                    if (this.mOrientationHelper.getDecoratedStart(child) < limit || this.mOrientationHelper.getTransformedStartWithDecoration(child) < limit) {
                        recycleChildren(recycler, childCount - 1, i);
                        return;
                    }
                }
            }
        }
    }

    private void recycleByLayoutState(Recycler recycler, LayoutState layoutState) {
        if (layoutState.mRecycle && !layoutState.mInfinite) {
            if (layoutState.mLayoutDirection == -1) {
                recycleViewsFromEnd(recycler, layoutState.mScrollingOffset);
            } else {
                recycleViewsFromStart(recycler, layoutState.mScrollingOffset);
            }
        }
    }

    int fill(Recycler recycler, LayoutState layoutState, State state, boolean stopOnFocusable) {
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
                if (!(layoutChunkResult.mIgnoreConsumed && this.mLayoutState.mScrapList == null && (state.isPreLayout() ^ 1) == 0)) {
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

    void layoutChunk(Recycler recycler, State state, LayoutState layoutState, LayoutChunkResult result) {
        View view = layoutState.next(recycler);
        if (view == null) {
            result.mFinished = true;
            return;
        }
        int right;
        int left;
        int bottom;
        int top;
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        boolean z;
        boolean z2;
        if (layoutState.mScrapList == null) {
            z = this.mShouldReverseLayout;
            if (layoutState.mLayoutDirection == -1) {
                z2 = true;
            } else {
                z2 = false;
            }
            if (z == z2) {
                addView(view);
            } else {
                addView(view, 0);
            }
        } else {
            z = this.mShouldReverseLayout;
            if (layoutState.mLayoutDirection == -1) {
                z2 = true;
            } else {
                z2 = false;
            }
            if (z == z2) {
                addDisappearingView(view);
            } else {
                addDisappearingView(view, 0);
            }
        }
        measureChildWithMargins(view, 0, 0);
        result.mConsumed = this.mOrientationHelper.getDecoratedMeasurement(view);
        if (this.mOrientation == 1) {
            if (isLayoutRTL()) {
                right = getWidth() - getPaddingRight();
                left = right - this.mOrientationHelper.getDecoratedMeasurementInOther(view);
            } else {
                left = getPaddingLeft();
                right = left + this.mOrientationHelper.getDecoratedMeasurementInOther(view);
            }
            if (layoutState.mLayoutDirection == -1) {
                bottom = layoutState.mOffset;
                top = layoutState.mOffset - result.mConsumed;
            } else {
                top = layoutState.mOffset;
                bottom = layoutState.mOffset + result.mConsumed;
            }
        } else {
            top = getPaddingTop();
            bottom = top + this.mOrientationHelper.getDecoratedMeasurementInOther(view);
            if (layoutState.mLayoutDirection == -1) {
                right = layoutState.mOffset;
                left = layoutState.mOffset - result.mConsumed;
            } else {
                left = layoutState.mOffset;
                right = layoutState.mOffset + result.mConsumed;
            }
        }
        layoutDecoratedWithMargins(view, left, top, right, bottom);
        if (params.isItemRemoved() || params.isItemChanged()) {
            result.mIgnoreConsumed = true;
        }
        result.mFocusable = view.isFocusable();
    }

    boolean shouldMeasureTwice() {
        if (getHeightMode() == 1073741824 || getWidthMode() == 1073741824) {
            return false;
        }
        return hasFlexibleChildInBothOrientations();
    }

    int convertFocusDirectionToLayoutDirection(int focusDirection) {
        int i = -1;
        int i2 = Integer.MIN_VALUE;
        int i3 = 1;
        switch (focusDirection) {
            case 1:
                return (this.mOrientation != 1 && isLayoutRTL()) ? 1 : -1;
            case 2:
                return (this.mOrientation != 1 && isLayoutRTL()) ? -1 : 1;
            case 17:
                if (this.mOrientation != 0) {
                    i = Integer.MIN_VALUE;
                }
                return i;
            case 33:
                if (this.mOrientation != 1) {
                    i = Integer.MIN_VALUE;
                }
                return i;
            case 66:
                if (this.mOrientation != 0) {
                    i3 = Integer.MIN_VALUE;
                }
                return i3;
            case 130:
                if (this.mOrientation == 1) {
                    i2 = 1;
                }
                return i2;
            default:
                return Integer.MIN_VALUE;
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

    private View findReferenceChildClosestToEnd(Recycler recycler, State state) {
        if (this.mShouldReverseLayout) {
            return findFirstReferenceChild(recycler, state);
        }
        return findLastReferenceChild(recycler, state);
    }

    private View findReferenceChildClosestToStart(Recycler recycler, State state) {
        if (this.mShouldReverseLayout) {
            return findLastReferenceChild(recycler, state);
        }
        return findFirstReferenceChild(recycler, state);
    }

    private View findFirstReferenceChild(Recycler recycler, State state) {
        return findReferenceChild(recycler, state, 0, getChildCount(), state.getItemCount());
    }

    private View findLastReferenceChild(Recycler recycler, State state) {
        return findReferenceChild(recycler, state, getChildCount() - 1, -1, state.getItemCount());
    }

    View findReferenceChild(Recycler recycler, State state, int start, int end, int itemCount) {
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
                if (((LayoutParams) view.getLayoutParams()).isItemRemoved()) {
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
        if (outOfBoundsMatch == null) {
            outOfBoundsMatch = invalidMatch;
        }
        return outOfBoundsMatch;
    }

    public int findFirstVisibleItemPosition() {
        View child = findOneVisibleChild(0, getChildCount(), false, true);
        return child == null ? -1 : getPosition(child);
    }

    public int findFirstCompletelyVisibleItemPosition() {
        View child = findOneVisibleChild(0, getChildCount(), true, false);
        return child == null ? -1 : getPosition(child);
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

    View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible, boolean acceptPartiallyVisible) {
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

    public View onFocusSearchFailed(View focused, int focusDirection, Recycler recycler, State state) {
        resolveShouldLayoutReverse();
        if (getChildCount() == 0) {
            return null;
        }
        int layoutDir = convertFocusDirectionToLayoutDirection(focusDirection);
        if (layoutDir == Integer.MIN_VALUE) {
            return null;
        }
        View referenceChild;
        ensureLayoutState();
        if (layoutDir == -1) {
            referenceChild = findReferenceChildClosestToStart(recycler, state);
        } else {
            referenceChild = findReferenceChildClosestToEnd(recycler, state);
        }
        if (referenceChild == null) {
            return null;
        }
        View nextFocus;
        ensureLayoutState();
        updateLayoutState(layoutDir, (int) (((float) this.mOrientationHelper.getTotalSpace()) * MAX_SCROLL_FACTOR), false, state);
        this.mLayoutState.mScrollingOffset = Integer.MIN_VALUE;
        this.mLayoutState.mRecycle = false;
        fill(recycler, this.mLayoutState, state, true);
        if (layoutDir == -1) {
            nextFocus = getChildClosestToStart();
        } else {
            nextFocus = getChildClosestToEnd();
        }
        if (nextFocus == referenceChild || (nextFocus.isFocusable() ^ 1) != 0) {
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

    void validateChildOrder() {
        boolean z = true;
        Log.d(TAG, "validating child count " + getChildCount());
        if (getChildCount() >= 1) {
            int lastPos = getPosition(getChildAt(0));
            int lastScreenLoc = this.mOrientationHelper.getDecoratedStart(getChildAt(0));
            int i;
            View child;
            int pos;
            int screenLoc;
            StringBuilder append;
            if (this.mShouldReverseLayout) {
                i = 1;
                while (i < getChildCount()) {
                    child = getChildAt(i);
                    pos = getPosition(child);
                    screenLoc = this.mOrientationHelper.getDecoratedStart(child);
                    if (pos < lastPos) {
                        logChildren();
                        append = new StringBuilder().append("detected invalid position. loc invalid? ");
                        if (screenLoc >= lastScreenLoc) {
                            z = false;
                        }
                        throw new RuntimeException(append.append(z).toString());
                    } else if (screenLoc > lastScreenLoc) {
                        logChildren();
                        throw new RuntimeException("detected invalid location");
                    } else {
                        i++;
                    }
                }
            } else {
                i = 1;
                while (i < getChildCount()) {
                    child = getChildAt(i);
                    pos = getPosition(child);
                    screenLoc = this.mOrientationHelper.getDecoratedStart(child);
                    if (pos < lastPos) {
                        logChildren();
                        append = new StringBuilder().append("detected invalid position. loc invalid? ");
                        if (screenLoc >= lastScreenLoc) {
                            z = false;
                        }
                        throw new RuntimeException(append.append(z).toString());
                    } else if (screenLoc < lastScreenLoc) {
                        logChildren();
                        throw new RuntimeException("detected invalid location");
                    } else {
                        i++;
                    }
                }
            }
        }
    }

    public boolean supportsPredictiveItemAnimations() {
        return this.mPendingSavedState == null && this.mLastStackFromEnd == this.mStackFromEnd;
    }

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
}
