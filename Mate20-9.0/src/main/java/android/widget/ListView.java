package android.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.iawareperf.UniPerf;
import android.os.Bundle;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MathUtils;
import android.util.SparseBooleanArray;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.RemotableViewMethod;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import android.widget.RemoteViews;
import com.android.internal.R;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@RemoteViews.RemoteView
public class ListView extends HwAbsListView {
    private static final float MAX_SCROLL_FACTOR = 0.33f;
    private static final int MIN_SCROLL_PREVIEW_PIXELS = 2;
    static final int NO_POSITION = -1;
    static final String TAG = "ListView";
    private boolean mAreAllItemsSelectable;
    private final ArrowScrollFocusResult mArrowScrollFocusResult;
    Drawable mDivider;
    int mDividerHeight;
    private boolean mDividerIsOpaque;
    private Paint mDividerPaint;
    private FocusSelector mFocusSelector;
    private boolean mFooterDividersEnabled;
    ArrayList<FixedViewInfo> mFooterViewInfos;
    private boolean mHeaderDividersEnabled;
    ArrayList<FixedViewInfo> mHeaderViewInfos;
    private boolean mIsCacheColorOpaque;
    private boolean mItemsCanFocus;
    Drawable mOverScrollFooter;
    Drawable mOverScrollHeader;
    private final Rect mTempRect;

    private static class ArrowScrollFocusResult {
        private int mAmountToScroll;
        private int mSelectedPosition;

        private ArrowScrollFocusResult() {
        }

        /* access modifiers changed from: package-private */
        public void populate(int selectedPosition, int amountToScroll) {
            this.mSelectedPosition = selectedPosition;
            this.mAmountToScroll = amountToScroll;
        }

        public int getSelectedPosition() {
            return this.mSelectedPosition;
        }

        public int getAmountToScroll() {
            return this.mAmountToScroll;
        }
    }

    public class FixedViewInfo {
        public Object data;
        public boolean isSelectable;
        public View view;

        public FixedViewInfo() {
        }
    }

    private class FocusSelector implements Runnable {
        private static final int STATE_REQUEST_FOCUS = 3;
        private static final int STATE_SET_SELECTION = 1;
        private static final int STATE_WAIT_FOR_LAYOUT = 2;
        private int mAction;
        private int mPosition;
        private int mPositionTop;

        private FocusSelector() {
        }

        /* access modifiers changed from: package-private */
        public FocusSelector setupForSetSelection(int position, int top) {
            this.mPosition = position;
            this.mPositionTop = top;
            this.mAction = 1;
            return this;
        }

        public void run() {
            if (this.mAction == 1) {
                ListView.this.setSelectionFromTop(this.mPosition, this.mPositionTop);
                this.mAction = 2;
            } else if (this.mAction == 3) {
                View child = ListView.this.getChildAt(this.mPosition - ListView.this.mFirstPosition);
                if (child != null) {
                    child.requestFocus();
                }
                this.mAction = -1;
            }
        }

        /* access modifiers changed from: package-private */
        public Runnable setupFocusIfValid(int position) {
            if (this.mAction != 2 || position != this.mPosition) {
                return null;
            }
            this.mAction = 3;
            return this;
        }

        /* access modifiers changed from: package-private */
        public void onLayoutComplete() {
            if (this.mAction == 2) {
                this.mAction = -1;
            }
        }
    }

    public ListView(Context context) {
        this(context, null);
    }

    public ListView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842868);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHeaderViewInfos = Lists.newArrayList();
        this.mFooterViewInfos = Lists.newArrayList();
        this.mAreAllItemsSelectable = true;
        this.mItemsCanFocus = false;
        this.mTempRect = new Rect();
        this.mArrowScrollFocusResult = new ArrowScrollFocusResult();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListView, defStyleAttr, defStyleRes);
        CharSequence[] entries = a.getTextArray(0);
        if (entries != null) {
            setAdapter((ListAdapter) new ArrayAdapter(context, 17367043, (T[]) entries));
        }
        Drawable d = a.getDrawable(1);
        if (d != null) {
            setDivider(d);
        }
        Drawable osHeader = a.getDrawable(5);
        if (osHeader != null) {
            setOverscrollHeader(osHeader);
        }
        Drawable osFooter = a.getDrawable(6);
        if (osFooter != null) {
            setOverscrollFooter(osFooter);
        }
        if (a.hasValueOrEmpty(2)) {
            int dividerHeight = a.getDimensionPixelSize(2, 0);
            if (dividerHeight != 0) {
                setDividerHeight(dividerHeight);
            }
        }
        this.mHeaderDividersEnabled = a.getBoolean(3, true);
        this.mFooterDividersEnabled = a.getBoolean(4, true);
        a.recycle();
    }

    public int getMaxScrollAmount() {
        return (int) (MAX_SCROLL_FACTOR * ((float) (this.mBottom - this.mTop)));
    }

    private void adjustViewsUpOrDown() {
        int delta;
        int childCount = getChildCount();
        if (childCount > 0) {
            if (!this.mStackFromBottom) {
                delta = getChildAt(0).getTop() - this.mListPadding.top;
                if (this.mFirstPosition != 0) {
                    delta -= this.mDividerHeight;
                }
                if (delta < 0) {
                    delta = 0;
                }
            } else {
                int delta2 = getChildAt(childCount - 1).getBottom() - (getHeight() - this.mListPadding.bottom);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta2 += this.mDividerHeight;
                }
                if (delta > 0) {
                    delta = 0;
                }
            }
            if (delta != 0) {
                offsetChildrenTopAndBottom(-delta);
            }
        }
    }

    public void addHeaderView(View v, Object data, boolean isSelectable) {
        if (!(v.getParent() == null || v.getParent() == this || !Log.isLoggable(TAG, 5))) {
            Log.w(TAG, "The specified child already has a parent. You must call removeView() on the child's parent first.");
        }
        FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        this.mHeaderViewInfos.add(info);
        this.mAreAllItemsSelectable &= isSelectable;
        if (this.mAdapter != null) {
            if (!(this.mAdapter instanceof HeaderViewListAdapter)) {
                wrapHeaderListAdapterInternal();
            }
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
        }
    }

    public void addHeaderView(View v) {
        addHeaderView(v, null, true);
    }

    public int getHeaderViewsCount() {
        return this.mHeaderViewInfos.size();
    }

    public boolean removeHeaderView(View v) {
        if (this.mHeaderViewInfos.size() <= 0) {
            return false;
        }
        boolean result = false;
        if (this.mAdapter != null && ((HeaderViewListAdapter) this.mAdapter).removeHeader(v)) {
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
            result = true;
        }
        removeFixedViewInfo(v, this.mHeaderViewInfos);
        return result;
    }

    private void removeFixedViewInfo(View v, ArrayList<FixedViewInfo> where) {
        int len = where.size();
        for (int i = 0; i < len; i++) {
            if (where.get(i).view == v) {
                where.remove(i);
                return;
            }
        }
    }

    public void addFooterView(View v, Object data, boolean isSelectable) {
        if (!(v.getParent() == null || v.getParent() == this || !Log.isLoggable(TAG, 5))) {
            Log.w(TAG, "The specified child already has a parent. You must call removeView() on the child's parent first.");
        }
        FixedViewInfo info = new FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        this.mFooterViewInfos.add(info);
        this.mAreAllItemsSelectable &= isSelectable;
        if (this.mAdapter != null) {
            if (!(this.mAdapter instanceof HeaderViewListAdapter)) {
                wrapHeaderListAdapterInternal();
            }
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
        }
    }

    public void addFooterView(View v) {
        addFooterView(v, null, true);
    }

    public int getFooterViewsCount() {
        return this.mFooterViewInfos.size();
    }

    public boolean removeFooterView(View v) {
        if (this.mFooterViewInfos.size() <= 0) {
            return false;
        }
        boolean result = false;
        if (this.mAdapter != null && ((HeaderViewListAdapter) this.mAdapter).removeFooter(v)) {
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
            result = true;
        }
        removeFixedViewInfo(v, this.mFooterViewInfos);
        return result;
    }

    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    @RemotableViewMethod(asyncImpl = "setRemoteViewsAdapterAsync")
    public void setRemoteViewsAdapter(Intent intent) {
        super.setRemoteViewsAdapter(intent);
    }

    public void setAdapter(ListAdapter adapter) {
        int position;
        if (!(this.mAdapter == null || this.mDataSetObserver == null)) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }
        resetList();
        this.mRecycler.clear();
        if (this.mHeaderViewInfos.size() > 0 || this.mFooterViewInfos.size() > 0) {
            this.mAdapter = wrapHeaderListAdapterInternal(this.mHeaderViewInfos, this.mFooterViewInfos, adapter);
        } else {
            this.mAdapter = adapter;
        }
        this.mOldSelectedPosition = -1;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        super.setAdapter(adapter);
        if (this.mAdapter != null) {
            this.mAreAllItemsSelectable = this.mAdapter.areAllItemsEnabled();
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
            checkFocus();
            this.mDataSetObserver = new AbsListView.AdapterDataSetObserver();
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            wrapObserver();
            this.mRecycler.setViewTypeCount(this.mAdapter.getViewTypeCount());
            if (this.mStackFromBottom) {
                position = lookForSelectablePosition(this.mItemCount - 1, false);
            } else {
                position = lookForSelectablePosition(0, true);
            }
            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);
            if (this.mItemCount == 0) {
                checkSelectionChanged();
            }
        } else {
            this.mAreAllItemsSelectable = true;
            checkFocus();
            checkSelectionChanged();
        }
        requestLayout();
    }

    /* access modifiers changed from: package-private */
    public void resetList() {
        clearRecycledState(this.mHeaderViewInfos);
        clearRecycledState(this.mFooterViewInfos);
        super.resetList();
        this.mLayoutMode = 0;
    }

    private void clearRecycledState(ArrayList<FixedViewInfo> infos) {
        if (infos != null) {
            int count = infos.size();
            for (int i = 0; i < count; i++) {
                ViewGroup.LayoutParams params = infos.get(i).view.getLayoutParams();
                if (checkLayoutParams(params)) {
                    ((AbsListView.LayoutParams) params).recycledHeaderFooter = false;
                }
            }
        }
    }

    private boolean showingTopFadingEdge() {
        return this.mFirstPosition > 0 || getChildAt(0).getTop() > this.mScrollY + this.mListPadding.top;
    }

    private boolean showingBottomFadingEdge() {
        int childCount = getChildCount();
        int bottomOfBottomChild = getChildAt(childCount - 1).getBottom();
        int lastVisiblePosition = (this.mFirstPosition + childCount) - 1;
        int listBottom = (this.mScrollY + getHeight()) - this.mListPadding.bottom;
        if (lastVisiblePosition < this.mItemCount - 1 || bottomOfBottomChild < listBottom) {
            return true;
        }
        return false;
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        int scrollYDelta;
        int scrollYDelta2;
        int rectTopWithinChild = rect.top;
        rect.offset(child.getLeft(), child.getTop());
        rect.offset(-child.getScrollX(), -child.getScrollY());
        int height = getHeight();
        int listUnfadedTop = getScrollY();
        int listUnfadedBottom = listUnfadedTop + height;
        int fadingEdge = getVerticalFadingEdgeLength();
        if (showingTopFadingEdge() && (this.mSelectedPosition > 0 || rectTopWithinChild > fadingEdge)) {
            listUnfadedTop += fadingEdge;
        }
        int bottomOfBottomChild = getChildAt(getChildCount() - 1).getBottom();
        if (showingBottomFadingEdge() && (this.mSelectedPosition < this.mItemCount - 1 || rect.bottom < bottomOfBottomChild - fadingEdge)) {
            listUnfadedBottom -= fadingEdge;
        }
        int scrollYDelta3 = 0;
        boolean z = false;
        if (rect.bottom > listUnfadedBottom && rect.top > listUnfadedTop) {
            if (rect.height() > height) {
                scrollYDelta2 = 0 + (rect.top - listUnfadedTop);
            } else {
                scrollYDelta2 = 0 + (rect.bottom - listUnfadedBottom);
            }
            scrollYDelta3 = Math.min(scrollYDelta2, bottomOfBottomChild - listUnfadedBottom);
        } else if (rect.top < listUnfadedTop && rect.bottom < listUnfadedBottom) {
            if (rect.height() > height) {
                scrollYDelta = 0 - (listUnfadedBottom - rect.bottom);
            } else {
                scrollYDelta = 0 - (listUnfadedTop - rect.top);
            }
            scrollYDelta3 = Math.max(scrollYDelta, getChildAt(0).getTop() - listUnfadedTop);
        }
        if (scrollYDelta3 != 0) {
            z = true;
        }
        boolean scroll = z;
        if (scroll) {
            scrollListItemsBy(-scrollYDelta3);
            positionSelector(-1, child);
            this.mSelectedTop = child.getTop();
            invalidate();
        }
        return scroll;
    }

    /* access modifiers changed from: package-private */
    public void fillGap(boolean down) {
        int startOffset;
        int startOffset2;
        int count = getChildCount();
        if (down) {
            int paddingTop = 0;
            if ((this.mGroupFlags & 34) == 34) {
                paddingTop = getListPaddingTop();
            }
            if (count > 0) {
                startOffset2 = getChildAt(count - 1).getBottom() + this.mDividerHeight;
            } else {
                startOffset2 = paddingTop;
            }
            fillDown(this.mFirstPosition + count, startOffset2);
            correctTooHigh(getChildCount());
            return;
        }
        int paddingBottom = 0;
        if ((this.mGroupFlags & 34) == 34) {
            paddingBottom = getListPaddingBottom();
        }
        if (count > 0) {
            startOffset = getChildAt(0).getTop() - this.mDividerHeight;
        } else {
            startOffset = getHeight() - paddingBottom;
        }
        fillUp(this.mFirstPosition - 1, startOffset);
        correctTooLow(getChildCount());
    }

    private View fillDown(int pos, int nextTop) {
        View selectedView = null;
        int end = this.mBottom - this.mTop;
        if ((this.mGroupFlags & 34) == 34) {
            end -= this.mListPadding.bottom;
        }
        while (true) {
            boolean z = true;
            if (nextTop >= end || pos >= this.mItemCount - this.mAnimOffset) {
                setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
                onFirstPositionChange();
            } else {
                if (pos != this.mSelectedPosition) {
                    z = false;
                }
                boolean selected = z;
                View child = makeAndAddView(pos, nextTop, true, this.mListPadding.left, selected);
                nextTop = child.getBottom() + this.mDividerHeight;
                if (selected) {
                    selectedView = child;
                }
                pos++;
            }
        }
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
        onFirstPositionChange();
        return selectedView;
    }

    private View fillUp(int pos, int nextBottom) {
        View selectedView = null;
        int end = 0;
        if ((this.mGroupFlags & 34) == 34) {
            end = this.mListPadding.top;
        }
        while (true) {
            boolean z = true;
            if (nextBottom <= end || pos < 0) {
                this.mFirstPosition = pos + 1;
                setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
                onFirstPositionChange();
            } else {
                if (pos != this.mSelectedPosition) {
                    z = false;
                }
                boolean selected = z;
                View child = makeAndAddView(pos, nextBottom, false, this.mListPadding.left, selected);
                nextBottom = child.getTop() - this.mDividerHeight;
                if (selected) {
                    selectedView = child;
                }
                pos--;
            }
        }
        this.mFirstPosition = pos + 1;
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
        onFirstPositionChange();
        return selectedView;
    }

    private View fillFromTop(int nextTop) {
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mSelectedPosition);
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mItemCount - 1);
        if (this.mFirstPosition < 0) {
            this.mFirstPosition = 0;
        }
        return fillDown(this.mFirstPosition, nextTop);
    }

    private View fillFromMiddle(int childrenTop, int childrenBottom) {
        int height = childrenBottom - childrenTop;
        int position = reconcileSelectedPosition();
        View sel = makeAndAddView(position, childrenTop, true, this.mListPadding.left, true);
        this.mFirstPosition = position;
        int selHeight = sel.getMeasuredHeight();
        if (selHeight <= height) {
            sel.offsetTopAndBottom((height - selHeight) / 2);
        }
        fillAboveAndBelow(sel, position);
        if (!this.mStackFromBottom) {
            correctTooHigh(getChildCount());
        } else {
            correctTooLow(getChildCount());
        }
        return sel;
    }

    private void fillAboveAndBelow(View sel, int position) {
        int dividerHeight = this.mDividerHeight;
        if (!this.mStackFromBottom) {
            fillUp(position - 1, sel.getTop() - dividerHeight);
            adjustViewsUpOrDown();
            fillDown(position + 1, sel.getBottom() + dividerHeight);
            return;
        }
        fillDown(position + 1, sel.getBottom() + dividerHeight);
        adjustViewsUpOrDown();
        fillUp(position - 1, sel.getTop() - dividerHeight);
    }

    private View fillFromSelection(int selectedTop, int childrenTop, int childrenBottom) {
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, selectedPosition);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, selectedPosition);
        View sel = makeAndAddView(selectedPosition, selectedTop, true, this.mListPadding.left, true);
        if (sel.getBottom() > bottomSelectionPixel) {
            sel.offsetTopAndBottom(-Math.min(sel.getTop() - topSelectionPixel, sel.getBottom() - bottomSelectionPixel));
        } else if (sel.getTop() < topSelectionPixel) {
            sel.offsetTopAndBottom(Math.min(topSelectionPixel - sel.getTop(), bottomSelectionPixel - sel.getBottom()));
        }
        fillAboveAndBelow(sel, selectedPosition);
        if (!this.mStackFromBottom) {
            correctTooHigh(getChildCount());
        } else {
            correctTooLow(getChildCount());
        }
        return sel;
    }

    private int getBottomSelectionPixel(int childrenBottom, int fadingEdgeLength, int selectedPosition) {
        int bottomSelectionPixel = childrenBottom;
        if (selectedPosition != this.mItemCount - 1) {
            return bottomSelectionPixel - fadingEdgeLength;
        }
        return bottomSelectionPixel;
    }

    private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength, int selectedPosition) {
        int topSelectionPixel = childrenTop;
        if (selectedPosition > 0) {
            return topSelectionPixel + fadingEdgeLength;
        }
        return topSelectionPixel;
    }

    @RemotableViewMethod
    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position);
    }

    @RemotableViewMethod
    public void smoothScrollByOffset(int offset) {
        super.smoothScrollByOffset(offset);
    }

    private View moveSelection(View oldSel, View newSel, int delta, int childrenTop, int childrenBottom) {
        View sel;
        int i = childrenTop;
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int topSelectionPixel = getTopSelectionPixel(i, fadingEdgeLength, selectedPosition);
        int bottomSelectionPixel = getBottomSelectionPixel(i, fadingEdgeLength, selectedPosition);
        if (delta > 0) {
            View oldSel2 = makeAndAddView(selectedPosition - 1, oldSel.getTop(), true, this.mListPadding.left, false);
            int dividerHeight = this.mDividerHeight;
            sel = makeAndAddView(selectedPosition, oldSel2.getBottom() + dividerHeight, true, this.mListPadding.left, true);
            if (sel.getBottom() > bottomSelectionPixel) {
                int offset = Math.min(Math.min(sel.getTop() - topSelectionPixel, sel.getBottom() - bottomSelectionPixel), (childrenBottom - i) / 2);
                oldSel2.offsetTopAndBottom(-offset);
                sel.offsetTopAndBottom(-offset);
            }
            if (this.mStackFromBottom == 0) {
                fillUp(this.mSelectedPosition - 2, sel.getTop() - dividerHeight);
                adjustViewsUpOrDown();
                fillDown(this.mSelectedPosition + 1, sel.getBottom() + dividerHeight);
            } else {
                fillDown(this.mSelectedPosition + 1, sel.getBottom() + dividerHeight);
                adjustViewsUpOrDown();
                fillUp(this.mSelectedPosition - 2, sel.getTop() - dividerHeight);
            }
            View view = oldSel2;
        } else {
            if (delta < 0) {
                if (newSel != null) {
                    sel = makeAndAddView(selectedPosition, newSel.getTop(), true, this.mListPadding.left, true);
                } else {
                    sel = makeAndAddView(selectedPosition, oldSel.getTop(), false, this.mListPadding.left, true);
                }
                if (sel.getTop() < topSelectionPixel) {
                    sel.offsetTopAndBottom(Math.min(Math.min(topSelectionPixel - sel.getTop(), bottomSelectionPixel - sel.getBottom()), (childrenBottom - i) / 2));
                }
                fillAboveAndBelow(sel, selectedPosition);
            } else {
                int oldTop = oldSel.getTop();
                sel = makeAndAddView(selectedPosition, oldTop, true, this.mListPadding.left, true);
                if (oldTop < i && sel.getBottom() < i + 20) {
                    sel.offsetTopAndBottom(i - sel.getTop());
                }
                fillAboveAndBelow(sel, selectedPosition);
            }
        }
        return sel;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        if (this.mFocusSelector != null) {
            removeCallbacks(this.mFocusSelector);
            this.mFocusSelector = null;
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (getChildCount() > 0) {
            View focusedChild = getFocusedChild();
            if (focusedChild != null) {
                int childPosition = this.mFirstPosition + indexOfChild(focusedChild);
                int top = focusedChild.getTop() - Math.max(0, focusedChild.getBottom() - (h - this.mPaddingTop));
                if (this.mFocusSelector == null) {
                    this.mFocusSelector = new FocusSelector();
                }
                post(this.mFocusSelector.setupForSetSelection(childPosition, top));
            }
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize;
        int heightSize;
        int i = widthMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize2 = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize2 = View.MeasureSpec.getSize(heightMeasureSpec);
        int childWidth = 0;
        int childHeight = 0;
        int childState = 0;
        this.mItemCount = this.mAdapter == null ? 0 : this.mAdapter.getCount();
        if (this.mItemCount > 0 && (widthMode == 0 || heightMode == 0)) {
            View child = obtainView(0, this.mIsScrap);
            measureScrapChild(child, 0, i, heightSize2);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            childState = combineMeasuredStates(0, child.getMeasuredState());
            if (recycleOnMeasure() && this.mRecycler.shouldRecycleViewType(((AbsListView.LayoutParams) child.getLayoutParams()).viewType)) {
                this.mRecycler.addScrapView(child, 0);
            }
        }
        int childWidth2 = childWidth;
        int childHeight2 = childHeight;
        int childState2 = childState;
        if (widthMode == 0) {
            widthSize = this.mListPadding.left + this.mListPadding.right + childWidth2 + getVerticalScrollbarWidth();
        } else {
            widthSize = (-16777216 & childState2) | widthSize2;
        }
        int widthSize3 = widthSize;
        if (heightMode == 0) {
            heightSize = this.mListPadding.top + this.mListPadding.bottom + childHeight2 + (getVerticalFadingEdgeLength() * 2);
        } else {
            heightSize = heightSize2;
        }
        if (heightMode == Integer.MIN_VALUE) {
            heightSize = measureHeightOfChildren(i, 0, -1, heightSize, -1);
        }
        setMeasuredDimension(widthSize3, heightSize);
        this.mWidthMeasureSpec = i;
    }

    /* JADX WARNING: type inference failed for: r1v9, types: [android.view.ViewGroup$LayoutParams] */
    /* JADX WARNING: Multi-variable type inference failed */
    private void measureScrapChild(View child, int position, int widthMeasureSpec, int heightHint) {
        int childHeightSpec;
        AbsListView.LayoutParams p = (AbsListView.LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = generateDefaultLayoutParams();
            child.setLayoutParams(p);
        }
        p.viewType = this.mAdapter.getItemViewType(position);
        p.isEnabled = this.mAdapter.isEnabled(position);
        p.forceAdd = true;
        int childWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = View.MeasureSpec.makeSafeMeasureSpec(heightHint, 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
        child.forceLayout();
    }

    /* access modifiers changed from: protected */
    @ViewDebug.ExportedProperty(category = "list")
    public boolean recycleOnMeasure() {
        return true;
    }

    /* access modifiers changed from: package-private */
    public final int measureHeightOfChildren(int widthMeasureSpec, int startPosition, int endPosition, int maxHeight, int disallowPartialChildPosition) {
        int i = maxHeight;
        int i2 = disallowPartialChildPosition;
        if (this.mAdapter == null) {
            return this.mListPadding.top + this.mListPadding.bottom;
        }
        int returnedHeight = this.mListPadding.top + this.mListPadding.bottom;
        int dividerHeight = this.mDividerHeight;
        int i3 = endPosition;
        int endPosition2 = i3 == -1 ? adapter.getCount() - 1 : i3;
        AbsListView.RecycleBin recycleBin = this.mRecycler;
        boolean recyle = recycleOnMeasure();
        boolean[] isScrap = this.mIsScrap;
        int prevHeightWithoutPartialChild = 0;
        int returnedHeight2 = returnedHeight;
        int i4 = startPosition;
        while (i4 <= endPosition2) {
            View child = obtainView(i4, isScrap);
            measureScrapChild(child, i4, widthMeasureSpec, i);
            if (i4 > 0) {
                returnedHeight2 += dividerHeight;
            }
            if (recyle && recycleBin.shouldRecycleViewType(((AbsListView.LayoutParams) child.getLayoutParams()).viewType)) {
                recycleBin.addScrapView(child, -1);
            }
            returnedHeight2 += child.getMeasuredHeight();
            if (returnedHeight2 >= i) {
                return (i2 < 0 || i4 <= i2 || prevHeightWithoutPartialChild <= 0 || returnedHeight2 == i) ? i : prevHeightWithoutPartialChild;
            }
            if (i2 >= 0 && i4 >= i2) {
                prevHeightWithoutPartialChild = returnedHeight2;
            }
            i4++;
        }
        int i5 = widthMeasureSpec;
        return returnedHeight2;
    }

    /* access modifiers changed from: package-private */
    public int findMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount > 0) {
            if (!this.mStackFromBottom) {
                for (int i = 0; i < childCount; i++) {
                    if (y <= getChildAt(i).getBottom()) {
                        return this.mFirstPosition + i;
                    }
                }
            } else {
                for (int i2 = childCount - 1; i2 >= 0; i2--) {
                    if (y >= getChildAt(i2).getTop()) {
                        return this.mFirstPosition + i2;
                    }
                }
            }
        }
        return -1;
    }

    private View fillSpecific(int position, int top) {
        View below;
        View above;
        boolean tempIsSelected = position == this.mSelectedPosition;
        View temp = makeAndAddView(position, top, true, this.mListPadding.left, tempIsSelected);
        this.mFirstPosition = position;
        int dividerHeight = this.mDividerHeight;
        if (!this.mStackFromBottom) {
            above = fillUp(position - 1, temp.getTop() - dividerHeight);
            adjustViewsUpOrDown();
            below = fillDown(position + 1, temp.getBottom() + dividerHeight);
            int childCount = getChildCount();
            if (childCount > 0) {
                correctTooHigh(childCount);
            }
        } else {
            below = fillDown(position + 1, temp.getBottom() + dividerHeight);
            adjustViewsUpOrDown();
            above = fillUp(position - 1, temp.getTop() - dividerHeight);
            int childCount2 = getChildCount();
            if (childCount2 > 0) {
                correctTooLow(childCount2);
            }
        }
        if (tempIsSelected) {
            return temp;
        }
        if (above != null) {
            return above;
        }
        return below;
    }

    private void correctTooHigh(int childCount) {
        if ((this.mFirstPosition + childCount) - 1 == this.mItemCount - 1 && childCount > 0) {
            int bottomOffset = ((this.mBottom - this.mTop) - this.mListPadding.bottom) - getChildAt(childCount - 1).getBottom();
            View firstChild = getChildAt(0);
            int firstTop = firstChild.getTop();
            if (bottomOffset <= 0) {
                return;
            }
            if (this.mFirstPosition > 0 || firstTop < this.mListPadding.top) {
                if (this.mFirstPosition == 0) {
                    bottomOffset = Math.min(bottomOffset, this.mListPadding.top - firstTop);
                }
                offsetChildrenTopAndBottom(bottomOffset);
                if (this.mFirstPosition > 0) {
                    fillUp(this.mFirstPosition - 1, firstChild.getTop() - this.mDividerHeight);
                    adjustViewsUpOrDown();
                }
            }
        }
    }

    private void correctTooLow(int childCount) {
        if (this.mFirstPosition == 0 && childCount > 0) {
            int firstTop = getChildAt(0).getTop();
            int start = this.mListPadding.top;
            int end = (this.mBottom - this.mTop) - this.mListPadding.bottom;
            int topOffset = firstTop - start;
            View lastChild = getChildAt(childCount - 1);
            int lastBottom = lastChild.getBottom();
            int lastPosition = (this.mFirstPosition + childCount) - 1;
            if (topOffset <= 0) {
                return;
            }
            if (lastPosition < this.mItemCount - 1 || lastBottom > end) {
                if (lastPosition == this.mItemCount - 1) {
                    topOffset = Math.min(topOffset, lastBottom - end);
                }
                offsetChildrenTopAndBottom(-topOffset);
                if (lastPosition < this.mItemCount - 1) {
                    fillDown(lastPosition + 1, lastChild.getBottom() + this.mDividerHeight);
                    adjustViewsUpOrDown();
                }
            } else if (lastPosition == this.mItemCount - 1) {
                adjustViewsUpOrDown();
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:129:0x01de, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:157:0x029b, code lost:
        r9.scrapActiveViews();
        removeUnusedFixedViews(r7.mHeaderViewInfos);
        removeUnusedFixedViews(r7.mFooterViewInfos);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:158:0x02a9, code lost:
        if (r1 == null) goto L_0x02f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:160:0x02ad, code lost:
        if (r7.mItemsCanFocus == false) goto L_0x02e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:162:0x02b3, code lost:
        if (hasFocus() == false) goto L_0x02e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:164:0x02b9, code lost:
        if (r1.hasFocus() != false) goto L_0x02e7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:165:0x02bb, code lost:
        if (r1 != r0) goto L_0x02c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x02bd, code lost:
        if (r8 == null) goto L_0x02c5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x02c3, code lost:
        if (r8.requestFocus() != false) goto L_0x02cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x02c9, code lost:
        if (r1.requestFocus() == false) goto L_0x02cd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:0x02cb, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x02cd, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x02ce, code lost:
        if (r3 != false) goto L_0x02dd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x02d0, code lost:
        r4 = getFocusedChild();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:175:0x02d4, code lost:
        if (r4 == null) goto L_0x02d9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x02d6, code lost:
        r4.clearFocus();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x02d9, code lost:
        positionSelector(-1, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:178:0x02dd, code lost:
        r1.setSelected(false);
        r7.mSelectorRect.setEmpty();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:180:0x02e7, code lost:
        positionSelector(-1, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:181:0x02ea, code lost:
        r7.mSelectedTop = r1.getTop();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:183:0x02f4, code lost:
        if (r7.mTouchMode == 1) goto L_0x02fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:185:0x02f9, code lost:
        if (r7.mTouchMode != 2) goto L_0x02fc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:187:0x02fc, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:188:0x02fe, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:189:0x02ff, code lost:
        if (r3 == false) goto L_0x0312;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x0301, code lost:
        r4 = getChildAt(r7.mMotionPosition - r7.mFirstPosition);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x030a, code lost:
        if (r4 == null) goto L_0x0311;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x030c, code lost:
        positionSelector(r7.mMotionPosition, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:0x0314, code lost:
        if (r7.mSelectorPosition == -1) goto L_0x0327;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x0316, code lost:
        r4 = getChildAt(r7.mSelectorPosition - r7.mFirstPosition);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x031f, code lost:
        if (r4 == null) goto L_0x0326;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:198:0x0321, code lost:
        positionSelector(r7.mSelectorPosition, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:200:0x0327, code lost:
        r7.mSelectedTop = 0;
        r7.mSelectorRect.setEmpty();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:202:0x0333, code lost:
        if (hasFocus() == false) goto L_0x033a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:203:0x0335, code lost:
        if (r8 == null) goto L_0x033a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:204:0x0337, code lost:
        r8.requestFocus();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:205:0x033a, code lost:
        r4 = r29;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x033c, code lost:
        if (r4 == null) goto L_0x03a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x0342, code lost:
        if (r4.getAccessibilityFocusedHost() != null) goto L_0x03a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:209:0x0344, code lost:
        if (r28 == null) goto L_0x0383;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x0346, code lost:
        r6 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x034c, code lost:
        if (r6.isAttachedToWindow() == false) goto L_0x037c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:212:0x034e, code lost:
        r2 = r6.getAccessibilityNodeProvider();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:213:0x0353, code lost:
        if (r27 == null) goto L_0x036f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:214:0x0355, code lost:
        if (r2 == null) goto L_0x036f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:215:0x0357, code lost:
        r30 = r0;
        r31 = r1;
        r3 = r27;
        r32 = r3;
        r2.performAction(android.view.accessibility.AccessibilityNodeInfo.getVirtualDescendantId(r3.getSourceNodeId()), 64, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x036f, code lost:
        r30 = r0;
        r31 = r1;
        r32 = r27;
        r6.requestAccessibilityFocus();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x0378, code lost:
        r3 = r26;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:218:0x037c, code lost:
        r30 = r0;
        r31 = r1;
        r32 = r27;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:219:0x0383, code lost:
        r30 = r0;
        r31 = r1;
        r32 = r27;
        r6 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:220:0x038b, code lost:
        r3 = r26;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:221:0x038d, code lost:
        if (r3 == -1) goto L_0x03b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:222:0x038f, code lost:
        r1 = getChildAt(android.util.MathUtils.constrain(r3 - r7.mFirstPosition, 0, getChildCount() - 1));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:223:0x03a2, code lost:
        if (r1 == null) goto L_0x03b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:224:0x03a4, code lost:
        r1.requestAccessibilityFocus();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:225:0x03a8, code lost:
        r30 = r0;
        r31 = r1;
        r3 = r26;
        r32 = r27;
        r6 = r28;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:226:0x03b2, code lost:
        if (r8 == null) goto L_0x03bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x03b8, code lost:
        if (r8.getWindowToken() == null) goto L_0x03bd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:229:0x03ba, code lost:
        r8.dispatchFinishTemporaryDetach();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:230:0x03bd, code lost:
        r7.mLayoutMode = 0;
        r7.mDataChanged = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:231:0x03c4, code lost:
        if (r7.mPositionScrollAfterLayout == null) goto L_0x03ce;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:0x03c6, code lost:
        post(r7.mPositionScrollAfterLayout);
        r7.mPositionScrollAfterLayout = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:0x03ce, code lost:
        r7.mNeedSync = false;
        setNextSelectedPositionInt(r7.mSelectedPosition);
        updateScrollIndicators();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x03db, code lost:
        if (r7.mItemCount <= 0) goto L_0x03e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:235:0x03dd, code lost:
        checkSelectionChanged();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x03e0, code lost:
        invokeOnItemScrollListener();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x03e5, code lost:
        if (r7.mFocusSelector == null) goto L_0x03ec;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:239:0x03e7, code lost:
        r7.mFocusSelector.onLayoutComplete();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:240:0x03ec, code lost:
        if (r25 != false) goto L_0x03f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x03ee, code lost:
        r7.mBlockLayoutRequests = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x03f1, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x010c, code lost:
        if (r7.mAdapterHasStableIds == false) goto L_0x011a;
     */
    /* JADX WARNING: Removed duplicated region for block: B:252:0x0437  */
    /* JADX WARNING: Removed duplicated region for block: B:254:0x043e  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0093 A[SYNTHETIC, Splitter:B:48:0x0093] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x009a A[SYNTHETIC, Splitter:B:53:0x009a] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x00ae A[SYNTHETIC, Splitter:B:61:0x00ae] */
    public void layoutChildren() {
        boolean blockLayoutRequests;
        boolean dataChanged;
        View accessibilityFocusLayoutRestoreView;
        int accessibilityFocusPosition;
        View focusedChild;
        ViewRootImpl viewRootImpl;
        View accessibilityFocusLayoutRestoreView2;
        AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode;
        int accessibilityFocusPosition2;
        AbsListView.RecycleBin recycleBin;
        View focusLayoutRestoreView;
        View sel;
        View sel2;
        int i;
        int i2;
        boolean blockLayoutRequests2 = this.mBlockLayoutRequests;
        if (!blockLayoutRequests2) {
            this.mBlockLayoutRequests = true;
            super.layoutChildren();
            invalidate();
            if (this.mAdapter == null) {
                try {
                    resetList();
                    invokeOnItemScrollListener();
                    if (this.mFocusSelector != null) {
                        this.mFocusSelector.onLayoutComplete();
                    }
                    if (!blockLayoutRequests2) {
                        this.mBlockLayoutRequests = false;
                    }
                } catch (Throwable th) {
                    th = th;
                    blockLayoutRequests = blockLayoutRequests2;
                    if (this.mFocusSelector != null) {
                    }
                    if (!blockLayoutRequests) {
                    }
                    throw th;
                }
            } else {
                int childrenTop = this.mListPadding.top;
                int childrenBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
                int childCount = getChildCount();
                int index = 0;
                int delta = 0;
                View oldSel = null;
                View oldFirst = null;
                View newSel = null;
                switch (this.mLayoutMode) {
                    case 2:
                        index = this.mNextSelectedPosition - this.mFirstPosition;
                        if (index >= 0 && index < childCount) {
                            newSel = getChildAt(index);
                        }
                    case 1:
                    case 3:
                    case 4:
                    case 5:
                        int delta2 = delta;
                        View oldSel2 = oldSel;
                        View oldFirst2 = oldFirst;
                        View newSel2 = newSel;
                        dataChanged = this.mDataChanged;
                        if (dataChanged) {
                            handleDataChanged();
                        }
                        if (this.mItemCount != 0) {
                            resetList();
                            invokeOnItemScrollListener();
                            if (this.mFocusSelector != null) {
                                this.mFocusSelector.onLayoutComplete();
                            }
                            if (!blockLayoutRequests2) {
                                this.mBlockLayoutRequests = false;
                            }
                            return;
                        } else if (this.mItemCount == this.mAdapter.getCount()) {
                            setSelectedPositionInt(this.mNextSelectedPosition);
                            AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode2 = null;
                            View accessibilityFocusLayoutRestoreView3 = null;
                            int accessibilityFocusPosition3 = -1;
                            ViewRootImpl viewRootImpl2 = getViewRootImpl();
                            if (viewRootImpl2 != null) {
                                View focusHost = viewRootImpl2.getAccessibilityFocusedHost();
                                if (focusHost != null) {
                                    View focusChild = getAccessibilityFocusedChild(focusHost);
                                    if (focusChild != null) {
                                        if (!dataChanged || isDirectChildHeaderOrFooter(focusChild) || (focusChild.hasTransientState() && this.mAdapterHasStableIds)) {
                                            accessibilityFocusLayoutRestoreView3 = focusHost;
                                            accessibilityFocusLayoutRestoreNode2 = viewRootImpl2.getAccessibilityFocusedVirtualView();
                                        }
                                        accessibilityFocusPosition3 = getPositionForView(focusChild);
                                    }
                                }
                            }
                            AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode3 = accessibilityFocusLayoutRestoreNode2;
                            View accessibilityFocusLayoutRestoreView4 = accessibilityFocusLayoutRestoreView3;
                            int accessibilityFocusPosition4 = accessibilityFocusPosition3;
                            View focusLayoutRestoreView2 = null;
                            View focusLayoutRestoreView3 = null;
                            View focusedChild2 = getFocusedChild();
                            if (focusedChild2 != null) {
                                if (dataChanged) {
                                    if (!isDirectChildHeaderOrFooter(focusedChild2)) {
                                        if (!focusedChild2.hasTransientState()) {
                                            break;
                                        }
                                    }
                                }
                                View focusLayoutRestoreDirectChild = focusedChild2;
                                View focusLayoutRestoreDirectChild2 = findFocus();
                                if (focusLayoutRestoreDirectChild2 != null) {
                                    focusLayoutRestoreDirectChild2.dispatchStartTemporaryDetach();
                                }
                                focusLayoutRestoreView3 = focusLayoutRestoreDirectChild2;
                                focusLayoutRestoreView2 = focusLayoutRestoreDirectChild;
                                requestFocus();
                            }
                            View focusLayoutRestoreDirectChild3 = focusLayoutRestoreView2;
                            int firstPosition = this.mFirstPosition;
                            View focusLayoutRestoreView4 = focusLayoutRestoreView3;
                            AbsListView.RecycleBin recycleBin2 = this.mRecycler;
                            if (dataChanged) {
                                int i3 = 0;
                                while (true) {
                                    focusedChild = focusedChild2;
                                    int i4 = i3;
                                    if (i4 < childCount) {
                                        int accessibilityFocusPosition5 = accessibilityFocusPosition4;
                                        View accessibilityFocusLayoutRestoreView5 = accessibilityFocusLayoutRestoreView4;
                                        recycleBin2.addScrapView(getChildAt(i4), firstPosition + i4);
                                        i3 = i4 + 1;
                                        focusedChild2 = focusedChild;
                                        accessibilityFocusPosition4 = accessibilityFocusPosition5;
                                        accessibilityFocusLayoutRestoreView4 = accessibilityFocusLayoutRestoreView5;
                                    } else {
                                        accessibilityFocusPosition = accessibilityFocusPosition4;
                                        accessibilityFocusLayoutRestoreView = accessibilityFocusLayoutRestoreView4;
                                    }
                                }
                            } else {
                                focusedChild = focusedChild2;
                                accessibilityFocusPosition = accessibilityFocusPosition4;
                                accessibilityFocusLayoutRestoreView = accessibilityFocusLayoutRestoreView4;
                                recycleBin2.fillActiveViews(childCount, firstPosition);
                            }
                            detachAllViewsFromParent();
                            recycleBin2.removeSkippedScrap();
                            switch (this.mLayoutMode) {
                                case 1:
                                    viewRootImpl = viewRootImpl2;
                                    blockLayoutRequests = blockLayoutRequests2;
                                    accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode3;
                                    focusLayoutRestoreView = focusLayoutRestoreView4;
                                    View focusLayoutRestoreView5 = focusedChild;
                                    accessibilityFocusPosition2 = accessibilityFocusPosition;
                                    accessibilityFocusLayoutRestoreView2 = accessibilityFocusLayoutRestoreView;
                                    recycleBin = recycleBin2;
                                    this.mFirstPosition = 0;
                                    sel = fillFromTop(childrenTop);
                                    adjustViewsUpOrDown();
                                    break;
                                case 2:
                                    viewRootImpl = viewRootImpl2;
                                    blockLayoutRequests = blockLayoutRequests2;
                                    accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode3;
                                    focusLayoutRestoreView = focusLayoutRestoreView4;
                                    View focusLayoutRestoreView6 = focusedChild;
                                    accessibilityFocusPosition2 = accessibilityFocusPosition;
                                    accessibilityFocusLayoutRestoreView2 = accessibilityFocusLayoutRestoreView;
                                    recycleBin = recycleBin2;
                                    if (newSel2 == null) {
                                        sel = fillFromMiddle(childrenTop, childrenBottom);
                                        break;
                                    } else {
                                        sel = fillFromSelection(newSel2.getTop(), childrenTop, childrenBottom);
                                    }
                                case 3:
                                    viewRootImpl = viewRootImpl2;
                                    blockLayoutRequests = blockLayoutRequests2;
                                    accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode3;
                                    focusLayoutRestoreView = focusLayoutRestoreView4;
                                    View focusLayoutRestoreView7 = focusedChild;
                                    accessibilityFocusPosition2 = accessibilityFocusPosition;
                                    accessibilityFocusLayoutRestoreView2 = accessibilityFocusLayoutRestoreView;
                                    recycleBin = recycleBin2;
                                    sel = fillUp(this.mItemCount - 1, childrenBottom);
                                    adjustViewsUpOrDown();
                                    break;
                                case 4:
                                    viewRootImpl = viewRootImpl2;
                                    blockLayoutRequests = blockLayoutRequests2;
                                    accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode3;
                                    focusLayoutRestoreView = focusLayoutRestoreView4;
                                    View focusLayoutRestoreView8 = focusedChild;
                                    accessibilityFocusPosition2 = accessibilityFocusPosition;
                                    accessibilityFocusLayoutRestoreView2 = accessibilityFocusLayoutRestoreView;
                                    recycleBin = recycleBin2;
                                    int selectedPosition = reconcileSelectedPosition();
                                    sel2 = fillSpecific(selectedPosition, this.mSpecificTop);
                                    if (sel2 == null && this.mFocusSelector != null) {
                                        Runnable focusRunnable = this.mFocusSelector.setupFocusIfValid(selectedPosition);
                                        if (focusRunnable != null) {
                                            post(focusRunnable);
                                            break;
                                        }
                                    }
                                    break;
                                case 5:
                                    viewRootImpl = viewRootImpl2;
                                    blockLayoutRequests = blockLayoutRequests2;
                                    accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode3;
                                    focusLayoutRestoreView = focusLayoutRestoreView4;
                                    View focusLayoutRestoreView9 = focusedChild;
                                    accessibilityFocusPosition2 = accessibilityFocusPosition;
                                    accessibilityFocusLayoutRestoreView2 = accessibilityFocusLayoutRestoreView;
                                    recycleBin = recycleBin2;
                                    sel = fillSpecific(this.mSyncPosition, this.mSpecificTop);
                                    break;
                                case 6:
                                    View focusLayoutRestoreView10 = focusLayoutRestoreView4;
                                    View focusLayoutRestoreView11 = focusedChild;
                                    blockLayoutRequests = blockLayoutRequests2;
                                    accessibilityFocusPosition2 = accessibilityFocusPosition;
                                    focusLayoutRestoreView = focusLayoutRestoreView10;
                                    accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode3;
                                    accessibilityFocusLayoutRestoreView2 = accessibilityFocusLayoutRestoreView;
                                    recycleBin = recycleBin2;
                                    viewRootImpl = viewRootImpl2;
                                    sel = moveSelection(oldSel2, newSel2, delta2, childrenTop, childrenBottom);
                                    break;
                                default:
                                    int i5 = firstPosition;
                                    viewRootImpl = viewRootImpl2;
                                    blockLayoutRequests = blockLayoutRequests2;
                                    accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode3;
                                    focusLayoutRestoreView = focusLayoutRestoreView4;
                                    View focusLayoutRestoreView12 = focusedChild;
                                    accessibilityFocusPosition2 = accessibilityFocusPosition;
                                    accessibilityFocusLayoutRestoreView2 = accessibilityFocusLayoutRestoreView;
                                    recycleBin = recycleBin2;
                                    if (childCount == 0) {
                                        try {
                                            if (this.mStackFromBottom) {
                                                setSelectedPositionInt(lookForSelectablePosition(this.mItemCount - 1, false));
                                                sel = fillUp(this.mItemCount - 1, childrenBottom);
                                                break;
                                            } else {
                                                setSelectedPositionInt(lookForSelectablePosition(0, true));
                                                sel = fillFromTop(childrenTop);
                                                break;
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            if (this.mFocusSelector != null) {
                                            }
                                            if (!blockLayoutRequests) {
                                            }
                                            throw th;
                                        }
                                    } else if (this.mSelectedPosition < 0 || this.mSelectedPosition >= this.mItemCount) {
                                        if (this.mFirstPosition >= this.mItemCount) {
                                            sel2 = fillSpecific(0, childrenTop);
                                            break;
                                        } else {
                                            int i6 = this.mFirstPosition;
                                            if (oldFirst2 == null) {
                                                i = childrenTop;
                                            } else {
                                                i = oldFirst2.getTop();
                                            }
                                            sel = fillSpecific(i6, i);
                                        }
                                    } else {
                                        int i7 = this.mSelectedPosition;
                                        if (oldSel2 == null) {
                                            i2 = childrenTop;
                                        } else {
                                            i2 = oldSel2.getTop();
                                        }
                                        sel = fillSpecific(i7, i2);
                                    }
                                    break;
                            }
                        } else {
                            blockLayoutRequests = blockLayoutRequests2;
                            throw new IllegalStateException("The content of the adapter has changed but ListView did not receive a notification. Make sure the content of your adapter is not modified from a background thread, but only from the UI thread. Make sure your adapter calls notifyDataSetChanged() when its content changes. [in ListView(" + getId() + ", " + getClass() + ") with Adapter(" + this.mAdapter.getClass() + ")]");
                        }
                        break;
                    default:
                        index = this.mSelectedPosition - this.mFirstPosition;
                        if (index >= 0 && index < childCount) {
                            oldSel = getChildAt(index);
                        }
                        try {
                            oldFirst = getChildAt(0);
                            if (this.mNextSelectedPosition >= 0) {
                                delta = this.mNextSelectedPosition - this.mSelectedPosition;
                            }
                            newSel = getChildAt(index + delta);
                        } catch (Throwable th3) {
                            th = th3;
                            blockLayoutRequests = blockLayoutRequests2;
                            if (this.mFocusSelector != null) {
                                this.mFocusSelector.onLayoutComplete();
                            }
                            if (!blockLayoutRequests) {
                                this.mBlockLayoutRequests = false;
                            }
                            throw th;
                        }
                }
                int delta22 = delta;
                View oldSel22 = oldSel;
                View oldFirst22 = oldFirst;
                View newSel22 = newSel;
                dataChanged = this.mDataChanged;
                if (dataChanged) {
                }
                if (this.mItemCount != 0) {
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        boolean result = super.trackMotionScroll(deltaY, incrementalDeltaY);
        removeUnusedFixedViews(this.mHeaderViewInfos);
        removeUnusedFixedViews(this.mFooterViewInfos);
        return result;
    }

    private void removeUnusedFixedViews(List<FixedViewInfo> infoList) {
        if (infoList != null) {
            for (int i = infoList.size() - 1; i >= 0; i--) {
                View view = infoList.get(i).view;
                AbsListView.LayoutParams lp = (AbsListView.LayoutParams) view.getLayoutParams();
                if (view.getParent() == null && lp != null && lp.recycledHeaderFooter) {
                    removeDetachedView(view, false);
                    lp.recycledHeaderFooter = false;
                }
            }
        }
    }

    private boolean isDirectChildHeaderOrFooter(View child) {
        ArrayList<FixedViewInfo> headers = this.mHeaderViewInfos;
        int numHeaders = headers.size();
        for (int i = 0; i < numHeaders; i++) {
            if (child == headers.get(i).view) {
                return true;
            }
        }
        ArrayList<FixedViewInfo> footers = this.mFooterViewInfos;
        int numFooters = footers.size();
        for (int i2 = 0; i2 < numFooters; i2++) {
            if (child == footers.get(i2).view) {
                return true;
            }
        }
        return false;
    }

    private View makeAndAddView(int position, int y, boolean flow, int childrenLeft, boolean selected) {
        int i = position;
        if (!this.mDataChanged) {
            View activeView = this.mRecycler.getActiveView(i);
            if (activeView != null) {
                setupChild(activeView, i, y, flow, childrenLeft, selected, true);
                return activeView;
            }
        }
        UniPerf.getInstance().uniPerfEvent(4116, "", new int[]{0});
        this.mAddItemViewType = this.mAdapter.getItemViewType(i);
        this.mAddItemViewPosition = i;
        View child = obtainView(i, this.mIsScrap);
        UniPerf.getInstance().uniPerfEvent(4116, "", new int[]{-1});
        setupChild(child, i, y, flow, childrenLeft, selected, this.mIsScrap[0]);
        this.mAddItemViewType = -10000;
        this.mAddItemViewPosition = -1;
        return child;
    }

    /* JADX WARNING: type inference failed for: r16v0, types: [android.view.ViewGroup$LayoutParams] */
    /* JADX WARNING: Multi-variable type inference failed */
    private void setupChild(View child, int position, int y, boolean flowDown, int childrenLeft, boolean selected, boolean isAttachedToWindow) {
        int childHeightSpec;
        boolean z;
        View view = child;
        int i = position;
        int i2 = childrenLeft;
        Trace.traceBegin(8, "setupListItem");
        boolean isSelected = selected && shouldShowSelector();
        boolean updateChildSelected = isSelected != child.isSelected();
        int mode = this.mTouchMode;
        boolean isPressed = mode > 0 && mode < 3 && this.mMotionPosition == i;
        boolean updateChildPressed = isPressed != child.isPressed();
        boolean needToMeasure = !isAttachedToWindow || updateChildSelected || child.isLayoutRequested();
        AbsListView.LayoutParams p = (AbsListView.LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = generateDefaultLayoutParams();
        }
        AbsListView.LayoutParams p2 = p;
        p2.viewType = getHwItemViewType(i);
        p2.isEnabled = this.mAdapter.isEnabled(i);
        if (updateChildSelected) {
            view.setSelected(isSelected);
        }
        if (updateChildPressed) {
            view.setPressed(isPressed);
        }
        if (!(this.mChoiceMode == 0 || this.mCheckStates == null)) {
            if (view instanceof Checkable) {
                ((Checkable) view).setChecked(this.mCheckStates.get(i));
            } else if (getContext().getApplicationInfo().targetSdkVersion >= 11) {
                view.setActivated(this.mCheckStates.get(i));
            }
        }
        if ((!isAttachedToWindow || p2.forceAdd) && (!p2.recycledHeaderFooter || p2.viewType != -2)) {
            p2.forceAdd = false;
            if (p2.viewType == -2) {
                z = true;
                p2.recycledHeaderFooter = true;
            } else {
                z = true;
            }
            addViewInLayout(view, flowDown ? -1 : 0, p2, z);
            child.resolveRtlPropertiesIfNeeded();
        } else {
            attachViewToParent(view, flowDown ? -1 : 0, p2);
            if (isAttachedToWindow && ((AbsListView.LayoutParams) child.getLayoutParams()).scrappedFromPosition != i) {
                child.jumpDrawablesToCurrentState();
            }
        }
        if (needToMeasure) {
            int childWidthSpec = ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p2.width);
            int lpHeight = p2.height;
            if (lpHeight > 0) {
                childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
            } else {
                childHeightSpec = View.MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), 0);
            }
            view.measure(childWidthSpec, childHeightSpec);
        } else {
            cleanupLayoutState(child);
        }
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childTop = flowDown ? y : y - h;
        if (needToMeasure) {
            int i3 = w;
            view.layout(i2, childTop, i2 + w, childTop + h);
        } else {
            view.offsetLeftAndRight(i2 - child.getLeft());
            view.offsetTopAndBottom(childTop - child.getTop());
        }
        if (this.mCachingStarted && !child.isDrawingCacheEnabled()) {
            view.setDrawingCacheEnabled(true);
        }
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: protected */
    public boolean canAnimate() {
        return super.canAnimate() && this.mItemCount > 0;
    }

    public void setSelection(int position) {
        setSelectionFromTop(position, 0);
    }

    /* access modifiers changed from: package-private */
    public void setSelectionInt(int position) {
        setNextSelectedPositionInt(position);
        boolean awakeScrollbars = false;
        int selectedPosition = this.mSelectedPosition;
        if (selectedPosition >= 0) {
            if (position == selectedPosition - 1) {
                awakeScrollbars = true;
            } else if (position == selectedPosition + 1) {
                awakeScrollbars = true;
            }
        }
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        layoutChildren();
        if (awakeScrollbars) {
            awakenScrollBars();
        }
    }

    /* access modifiers changed from: package-private */
    public int lookForSelectablePosition(int position, boolean lookDown) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null || isInTouchMode()) {
            return -1;
        }
        int count = adapter.getCount();
        if (!this.mAreAllItemsSelectable) {
            if (lookDown) {
                position = Math.max(0, position);
                while (position < count && !adapter.isEnabled(position)) {
                    position++;
                }
            } else {
                position = Math.min(position, count - 1);
                while (position >= 0 && !adapter.isEnabled(position)) {
                    position = position - 1;
                }
            }
        }
        if (position < 0 || position >= count) {
            return -1;
        }
        return position;
    }

    /* access modifiers changed from: package-private */
    public int lookForSelectablePositionAfter(int current, int position, boolean lookDown) {
        int position2;
        ListAdapter adapter = this.mAdapter;
        if (adapter == null || isInTouchMode()) {
            return -1;
        }
        int after = lookForSelectablePosition(position, lookDown);
        if (after != -1) {
            return after;
        }
        int count = adapter.getCount();
        int current2 = MathUtils.constrain(current, -1, count - 1);
        if (lookDown) {
            position2 = Math.min(position - 1, count - 1);
            while (position2 > current2 && !adapter.isEnabled(position2)) {
                position2--;
            }
            if (position2 <= current2) {
                return -1;
            }
        } else {
            int position3 = Math.max(0, position + 1);
            while (position2 < current2 && !adapter.isEnabled(position2)) {
                position3 = position2 + 1;
            }
            if (position2 >= current2) {
                return -1;
            }
        }
        return position2;
    }

    public void setSelectionAfterHeaderView() {
        int count = getHeaderViewsCount();
        if (count > 0) {
            this.mNextSelectedPosition = 0;
            return;
        }
        if (this.mAdapter != null) {
            setSelection(count);
        } else {
            this.mNextSelectedPosition = count;
            this.mLayoutMode = 2;
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = super.dispatchKeyEvent(event);
        if (handled || getFocusedChild() == null || event.getAction() != 0) {
            return handled;
        }
        return onKeyDown(event.getKeyCode(), event);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return commonKey(keyCode, repeatCount, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x017a, code lost:
        r9 = r3;
     */
    private boolean commonKey(int keyCode, int count, KeyEvent event) {
        int count2;
        if (this.mAdapter == null || !isAttachedToWindow()) {
            return false;
        }
        if (this.mDataChanged) {
            layoutChildren();
        }
        boolean handled = false;
        int action = event.getAction();
        if (KeyEvent.isConfirmKey(keyCode) && event.hasNoModifiers() && action != 1) {
            handled = resurrectSelectionIfNeeded();
            if (!handled && event.getRepeatCount() == 0 && getChildCount() > 0) {
                keyPressed();
                handled = true;
            }
        }
        if (!handled && action != 1) {
            switch (keyCode) {
                case 19:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2) != 0) {
                            handled = resurrectSelectionIfNeeded() || fullScroll(33);
                            break;
                        }
                    } else {
                        handled = resurrectSelectionIfNeeded();
                        if (!handled) {
                            while (true) {
                                count2 = count - 1;
                                if (count > 0 && arrowScroll(33) != 0) {
                                    handled = true;
                                    count = count2;
                                }
                            }
                        }
                    }
                    break;
                case 20:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2) != 0) {
                            handled = resurrectSelectionIfNeeded() || fullScroll(130);
                            break;
                        }
                    } else {
                        handled = resurrectSelectionIfNeeded();
                        if (!handled) {
                            while (true) {
                                count2 = count - 1;
                                if (count > 0 && arrowScroll(130) != 0) {
                                    handled = true;
                                    count = count2;
                                }
                            }
                        }
                    }
                    break;
                case 21:
                    if (event.hasNoModifiers()) {
                        handled = handleHorizontalFocusWithinListItem(17);
                        break;
                    }
                    break;
                case 22:
                    if (event.hasNoModifiers()) {
                        handled = handleHorizontalFocusWithinListItem(66);
                        break;
                    }
                    break;
                case 61:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(1)) {
                            handled = resurrectSelectionIfNeeded() || arrowScroll(33);
                            break;
                        }
                    } else {
                        handled = resurrectSelectionIfNeeded() || arrowScroll(130);
                        break;
                    }
                    break;
                case 92:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            handled = resurrectSelectionIfNeeded() || fullScroll(33);
                            break;
                        }
                    } else {
                        handled = resurrectSelectionIfNeeded() || pageScroll(33);
                        break;
                    }
                    break;
                case 93:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            handled = resurrectSelectionIfNeeded() || fullScroll(130);
                            break;
                        }
                    } else {
                        handled = resurrectSelectionIfNeeded() || pageScroll(130);
                        break;
                    }
                    break;
                case 122:
                    if (event.hasNoModifiers()) {
                        handled = resurrectSelectionIfNeeded() || fullScroll(33);
                        break;
                    }
                    break;
                case 123:
                    if (event.hasNoModifiers()) {
                        handled = resurrectSelectionIfNeeded() || fullScroll(130);
                        break;
                    }
                    break;
            }
        }
        if (handled || sendToTextFilter(keyCode, count, event)) {
            return true;
        }
        switch (action) {
            case 0:
                return super.onKeyDown(keyCode, event);
            case 1:
                return super.onKeyUp(keyCode, event);
            case 2:
                return super.onKeyMultiple(keyCode, count, event);
            default:
                return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean pageScroll(int direction) {
        boolean down;
        int nextPage;
        if (direction == 33) {
            nextPage = Math.max(0, (this.mSelectedPosition - getChildCount()) - 1);
            down = false;
        } else if (direction != 130) {
            return false;
        } else {
            nextPage = Math.min(this.mItemCount - 1, (this.mSelectedPosition + getChildCount()) - 1);
            down = true;
        }
        if (nextPage >= 0) {
            int position = lookForSelectablePositionAfter(this.mSelectedPosition, nextPage, down);
            if (position >= 0) {
                this.mLayoutMode = 4;
                this.mSpecificTop = this.mPaddingTop + getVerticalFadingEdgeLength();
                if (down && position > this.mItemCount - getChildCount()) {
                    this.mLayoutMode = 3;
                }
                if (!down && position < getChildCount()) {
                    this.mLayoutMode = 1;
                }
                setSelectionInt(position);
                invokeOnItemScrollListener();
                if (!awakenScrollBars()) {
                    invalidate();
                }
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean fullScroll(int direction) {
        boolean moved = false;
        if (direction == 33) {
            if (this.mSelectedPosition != 0) {
                int position = lookForSelectablePositionAfter(this.mSelectedPosition, 0, true);
                if (position >= 0) {
                    this.mLayoutMode = 1;
                    setSelectionInt(position);
                    invokeOnItemScrollListener();
                }
                moved = true;
            }
        } else if (direction == 130) {
            int lastItem = this.mItemCount - 1;
            if (this.mSelectedPosition < lastItem) {
                int position2 = lookForSelectablePositionAfter(this.mSelectedPosition, lastItem, false);
                if (position2 >= 0) {
                    this.mLayoutMode = 3;
                    setSelectionInt(position2);
                    invokeOnItemScrollListener();
                }
                moved = true;
            }
        }
        if (moved && !awakenScrollBars()) {
            awakenScrollBars();
            invalidate();
        }
        return moved;
    }

    private boolean handleHorizontalFocusWithinListItem(int direction) {
        if (direction == 17 || direction == 66) {
            int numChildren = getChildCount();
            if (this.mItemsCanFocus && numChildren > 0 && this.mSelectedPosition != -1) {
                View selectedView = getSelectedView();
                if (selectedView != null && selectedView.hasFocus() && (selectedView instanceof ViewGroup)) {
                    View currentFocus = selectedView.findFocus();
                    View nextFocus = FocusFinder.getInstance().findNextFocus((ViewGroup) selectedView, currentFocus, direction);
                    if (nextFocus != null) {
                        Rect focusedRect = this.mTempRect;
                        if (currentFocus != null) {
                            currentFocus.getFocusedRect(focusedRect);
                            offsetDescendantRectToMyCoords(currentFocus, focusedRect);
                            offsetRectIntoDescendantCoords(nextFocus, focusedRect);
                        } else {
                            focusedRect = null;
                        }
                        if (nextFocus.requestFocus(direction, focusedRect)) {
                            return true;
                        }
                    }
                    View globalNextFocus = FocusFinder.getInstance().findNextFocus((ViewGroup) getRootView(), currentFocus, direction);
                    if (globalNextFocus != null) {
                        return isViewAncestorOf(globalNextFocus, this);
                    }
                }
            }
            return false;
        }
        throw new IllegalArgumentException("direction must be one of {View.FOCUS_LEFT, View.FOCUS_RIGHT}");
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public boolean arrowScroll(int direction) {
        try {
            this.mInLayout = true;
            boolean handled = arrowScrollImpl(direction);
            if (handled) {
                playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
            }
            this.mInLayout = false;
            return handled;
        } catch (Throwable th) {
            this.mInLayout = false;
            throw th;
        }
    }

    private final int nextSelectedPositionForDirection(View selectedView, int selectedPos, int direction) {
        int nextSelected;
        int i;
        boolean z = true;
        if (direction == 130) {
            int listBottom = getHeight() - this.mListPadding.bottom;
            if (selectedView == null || selectedView.getBottom() > listBottom) {
                return -1;
            }
            if (selectedPos == -1 || selectedPos < this.mFirstPosition) {
                nextSelected = this.mFirstPosition;
            } else {
                nextSelected = selectedPos + 1;
            }
        } else {
            int listTop = this.mListPadding.top;
            if (selectedView == null || selectedView.getTop() < listTop) {
                return -1;
            }
            int lastPos = (this.mFirstPosition + getChildCount()) - 1;
            if (selectedPos == -1 || selectedPos > lastPos) {
                i = lastPos;
            } else {
                i = selectedPos - 1;
            }
            nextSelected = i;
        }
        int nextSelected2 = nextSelected;
        if (nextSelected2 < 0 || nextSelected2 >= this.mAdapter.getCount()) {
            return -1;
        }
        if (direction != 130) {
            z = false;
        }
        return lookForSelectablePosition(nextSelected2, z);
    }

    private boolean arrowScrollImpl(int direction) {
        if (getChildCount() <= 0) {
            return false;
        }
        View selectedView = getSelectedView();
        int selectedPos = this.mSelectedPosition;
        int nextSelectedPosition = nextSelectedPositionForDirection(selectedView, selectedPos, direction);
        int amountToScroll = amountToScroll(direction, nextSelectedPosition);
        ArrowScrollFocusResult focusResult = this.mItemsCanFocus ? arrowScrollFocused(direction) : null;
        if (focusResult != null) {
            nextSelectedPosition = focusResult.getSelectedPosition();
            amountToScroll = focusResult.getAmountToScroll();
        }
        boolean needToRedraw = focusResult != null;
        if (nextSelectedPosition != -1) {
            handleNewSelectionChange(selectedView, direction, nextSelectedPosition, focusResult != null);
            setSelectedPositionInt(nextSelectedPosition);
            setNextSelectedPositionInt(nextSelectedPosition);
            selectedView = getSelectedView();
            selectedPos = nextSelectedPosition;
            if (this.mItemsCanFocus && focusResult == null) {
                View focused = getFocusedChild();
                if (focused != null) {
                    focused.clearFocus();
                }
            }
            needToRedraw = true;
            checkSelectionChanged();
        }
        if (amountToScroll > 0) {
            scrollListItemsBy(direction == 33 ? amountToScroll : -amountToScroll);
            needToRedraw = true;
        }
        if (this.mItemsCanFocus && focusResult == null && selectedView != null && selectedView.hasFocus()) {
            View focused2 = selectedView.findFocus();
            if (focused2 != null && (!isViewAncestorOf(focused2, this) || distanceToView(focused2) > 0)) {
                focused2.clearFocus();
            }
        }
        if (nextSelectedPosition == -1 && selectedView != null && !isViewAncestorOf(selectedView, this)) {
            selectedView = null;
            hideSelector();
            this.mResurrectToPosition = -1;
        }
        if (!needToRedraw) {
            return false;
        }
        if (selectedView != null) {
            positionSelectorLikeFocus(selectedPos, selectedView);
            this.mSelectedTop = selectedView.getTop();
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        invokeOnItemScrollListener();
        return true;
    }

    private void handleNewSelectionChange(View selectedView, int direction, int newSelectedPosition, boolean newFocusAssigned) {
        View bottomView;
        View topView;
        int bottomViewIndex;
        int topViewIndex;
        if (newSelectedPosition != -1) {
            boolean topSelected = false;
            int selectedIndex = this.mSelectedPosition - this.mFirstPosition;
            int nextSelectedIndex = newSelectedPosition - this.mFirstPosition;
            if (direction == 33) {
                topViewIndex = nextSelectedIndex;
                bottomViewIndex = selectedIndex;
                topView = getChildAt(topViewIndex);
                bottomView = selectedView;
                topSelected = true;
            } else {
                topViewIndex = selectedIndex;
                bottomViewIndex = nextSelectedIndex;
                topView = selectedView;
                bottomView = getChildAt(bottomViewIndex);
            }
            int numChildren = getChildCount();
            boolean z = true;
            if (topView != null) {
                topView.setSelected(!newFocusAssigned && topSelected);
                measureAndAdjustDown(topView, topViewIndex, numChildren);
            }
            if (bottomView != null) {
                if (newFocusAssigned || topSelected) {
                    z = false;
                }
                bottomView.setSelected(z);
                measureAndAdjustDown(bottomView, bottomViewIndex, numChildren);
                return;
            }
            return;
        }
        throw new IllegalArgumentException("newSelectedPosition needs to be valid");
    }

    private void measureAndAdjustDown(View child, int childIndex, int numChildren) {
        int oldHeight = child.getHeight();
        measureItem(child);
        if (child.getMeasuredHeight() != oldHeight) {
            relayoutMeasuredItem(child);
            int heightDelta = child.getMeasuredHeight() - oldHeight;
            for (int i = childIndex + 1; i < numChildren; i++) {
                getChildAt(i).offsetTopAndBottom(heightDelta);
            }
        }
    }

    private void measureItem(View child) {
        int childHeightSpec;
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(-1, -2);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = View.MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private void relayoutMeasuredItem(View child) {
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childLeft = this.mListPadding.left;
        int childTop = child.getTop();
        child.layout(childLeft, childTop, childLeft + w, childTop + h);
    }

    private int getArrowScrollPreviewLength() {
        return Math.max(2, getVerticalFadingEdgeLength());
    }

    private int amountToScroll(int direction, int nextSelectedPosition) {
        int listBottom = getHeight() - this.mListPadding.bottom;
        int listTop = this.mListPadding.top;
        int numChildren = getChildCount();
        if (direction == 130) {
            int indexToMakeVisible = numChildren - 1;
            if (nextSelectedPosition != -1) {
                indexToMakeVisible = nextSelectedPosition - this.mFirstPosition;
            }
            while (numChildren <= indexToMakeVisible) {
                addViewBelow(getChildAt(numChildren - 1), (this.mFirstPosition + numChildren) - 1);
                numChildren++;
            }
            int positionToMakeVisible = this.mFirstPosition + indexToMakeVisible;
            View viewToMakeVisible = getChildAt(indexToMakeVisible);
            int goalBottom = listBottom;
            if (positionToMakeVisible < this.mItemCount - 1) {
                goalBottom -= getArrowScrollPreviewLength();
            }
            if (viewToMakeVisible.getBottom() <= goalBottom) {
                return 0;
            }
            if (nextSelectedPosition != -1 && goalBottom - viewToMakeVisible.getTop() >= getMaxScrollAmount()) {
                return 0;
            }
            int amountToScroll = viewToMakeVisible.getBottom() - goalBottom;
            if (this.mFirstPosition + numChildren == this.mItemCount) {
                amountToScroll = Math.min(amountToScroll, getChildAt(numChildren - 1).getBottom() - listBottom);
            }
            return Math.min(amountToScroll, getMaxScrollAmount());
        }
        int indexToMakeVisible2 = 0;
        if (nextSelectedPosition != -1) {
            indexToMakeVisible2 = nextSelectedPosition - this.mFirstPosition;
        }
        while (indexToMakeVisible2 < 0) {
            addViewAbove(getChildAt(0), this.mFirstPosition);
            this.mFirstPosition--;
            indexToMakeVisible2 = nextSelectedPosition - this.mFirstPosition;
        }
        int positionToMakeVisible2 = this.mFirstPosition + indexToMakeVisible2;
        View viewToMakeVisible2 = getChildAt(indexToMakeVisible2);
        int goalTop = listTop;
        if (positionToMakeVisible2 > 0) {
            goalTop += getArrowScrollPreviewLength();
        }
        if (viewToMakeVisible2.getTop() >= goalTop) {
            return 0;
        }
        if (nextSelectedPosition != -1 && viewToMakeVisible2.getBottom() - goalTop >= getMaxScrollAmount()) {
            return 0;
        }
        int amountToScroll2 = goalTop - viewToMakeVisible2.getTop();
        if (this.mFirstPosition == 0) {
            amountToScroll2 = Math.min(amountToScroll2, listTop - getChildAt(0).getTop());
        }
        return Math.min(amountToScroll2, getMaxScrollAmount());
    }

    private int lookForSelectablePositionOnScreen(int direction) {
        int startPos;
        int startPos2;
        int firstPosition = this.mFirstPosition;
        if (direction == 130) {
            if (this.mSelectedPosition != -1) {
                startPos2 = this.mSelectedPosition + 1;
            } else {
                startPos2 = firstPosition;
            }
            if (startPos2 >= this.mAdapter.getCount()) {
                return -1;
            }
            if (startPos2 < firstPosition) {
                startPos2 = firstPosition;
            }
            int lastVisiblePos = getLastVisiblePosition();
            ListAdapter adapter = getAdapter();
            for (int pos = startPos2; pos <= lastVisiblePos; pos++) {
                if (adapter.isEnabled(pos) && getChildAt(pos - firstPosition).getVisibility() == 0) {
                    return pos;
                }
            }
        } else {
            int last = (getChildCount() + firstPosition) - 1;
            if (this.mSelectedPosition != -1) {
                startPos = this.mSelectedPosition - 1;
            } else {
                startPos = (getChildCount() + firstPosition) - 1;
            }
            if (startPos < 0 || startPos >= this.mAdapter.getCount()) {
                return -1;
            }
            if (startPos > last) {
                startPos = last;
            }
            ListAdapter adapter2 = getAdapter();
            for (int pos2 = startPos; pos2 >= firstPosition; pos2--) {
                if (adapter2.isEnabled(pos2) && getChildAt(pos2 - firstPosition).getVisibility() == 0) {
                    return pos2;
                }
            }
        }
        return -1;
    }

    private ArrowScrollFocusResult arrowScrollFocused(int direction) {
        View oldFocus;
        int ySearchPoint;
        int ySearchPoint2;
        View selectedView = getSelectedView();
        if (selectedView == null || !selectedView.hasFocus()) {
            boolean topFadingEdgeShowing = true;
            if (direction == 130) {
                if (this.mFirstPosition <= 0) {
                    topFadingEdgeShowing = false;
                }
                int listTop = this.mListPadding.top + (topFadingEdgeShowing ? getArrowScrollPreviewLength() : 0);
                if (selectedView == null || selectedView.getTop() <= listTop) {
                    ySearchPoint2 = listTop;
                } else {
                    ySearchPoint2 = selectedView.getTop();
                }
                this.mTempRect.set(0, ySearchPoint2, 0, ySearchPoint2);
            } else {
                if ((this.mFirstPosition + getChildCount()) - 1 >= this.mItemCount) {
                    topFadingEdgeShowing = false;
                }
                int listBottom = (getHeight() - this.mListPadding.bottom) - (topFadingEdgeShowing ? getArrowScrollPreviewLength() : 0);
                if (selectedView == null || selectedView.getBottom() >= listBottom) {
                    ySearchPoint = listBottom;
                } else {
                    ySearchPoint = selectedView.getBottom();
                }
                this.mTempRect.set(0, ySearchPoint, 0, ySearchPoint);
            }
            oldFocus = FocusFinder.getInstance().findNextFocusFromRect(this, this.mTempRect, direction);
        } else {
            oldFocus = FocusFinder.getInstance().findNextFocus(this, selectedView.findFocus(), direction);
        }
        if (oldFocus != null) {
            int positionOfNewFocus = positionOfNewFocus(oldFocus);
            if (!(this.mSelectedPosition == -1 || positionOfNewFocus == this.mSelectedPosition)) {
                int selectablePosition = lookForSelectablePositionOnScreen(direction);
                if (selectablePosition != -1 && ((direction == 130 && selectablePosition < positionOfNewFocus) || (direction == 33 && selectablePosition > positionOfNewFocus))) {
                    return null;
                }
            }
            int focusScroll = amountToScrollToNewFocus(direction, oldFocus, positionOfNewFocus);
            int maxScrollAmount = getMaxScrollAmount();
            if (focusScroll < maxScrollAmount) {
                oldFocus.requestFocus(direction);
                this.mArrowScrollFocusResult.populate(positionOfNewFocus, focusScroll);
                return this.mArrowScrollFocusResult;
            } else if (distanceToView(oldFocus) < maxScrollAmount) {
                oldFocus.requestFocus(direction);
                this.mArrowScrollFocusResult.populate(positionOfNewFocus, maxScrollAmount);
                return this.mArrowScrollFocusResult;
            }
        }
        return null;
    }

    private int positionOfNewFocus(View newFocus) {
        int numChildren = getChildCount();
        for (int i = 0; i < numChildren; i++) {
            if (isViewAncestorOf(newFocus, getChildAt(i))) {
                return this.mFirstPosition + i;
            }
        }
        throw new IllegalArgumentException("newFocus is not a child of any of the children of the list!");
    }

    private boolean isViewAncestorOf(View child, View parent) {
        boolean z = true;
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        if (!(theParent instanceof ViewGroup) || !isViewAncestorOf((View) theParent, parent)) {
            z = false;
        }
        return z;
    }

    private int amountToScrollToNewFocus(int direction, View newFocus, int positionOfNewFocus) {
        newFocus.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(newFocus, this.mTempRect);
        if (direction != 33) {
            int listBottom = getHeight() - this.mListPadding.bottom;
            if (this.mTempRect.bottom <= listBottom) {
                return 0;
            }
            int amountToScroll = this.mTempRect.bottom - listBottom;
            if (positionOfNewFocus < this.mItemCount - 1) {
                return amountToScroll + getArrowScrollPreviewLength();
            }
            return amountToScroll;
        } else if (this.mTempRect.top >= this.mListPadding.top) {
            return 0;
        } else {
            int amountToScroll2 = this.mListPadding.top - this.mTempRect.top;
            if (positionOfNewFocus > 0) {
                return amountToScroll2 + getArrowScrollPreviewLength();
            }
            return amountToScroll2;
        }
    }

    private int distanceToView(View descendant) {
        descendant.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(descendant, this.mTempRect);
        int listBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
        if (this.mTempRect.bottom < this.mListPadding.top) {
            return this.mListPadding.top - this.mTempRect.bottom;
        }
        if (this.mTempRect.top > listBottom) {
            return this.mTempRect.top - listBottom;
        }
        return 0;
    }

    private void scrollListItemsBy(int amount) {
        offsetChildrenTopAndBottom(amount);
        int listBottom = getHeight() - this.mListPadding.bottom;
        int listTop = this.mListPadding.top;
        AbsListView.RecycleBin recycleBin = this.mRecycler;
        if (amount < 0) {
            int numChildren = getChildCount();
            View last = getChildAt(numChildren - 1);
            while (last.getBottom() < listBottom) {
                int lastVisiblePosition = (this.mFirstPosition + numChildren) - 1;
                if (lastVisiblePosition >= this.mItemCount - 1) {
                    break;
                }
                last = addViewBelow(last, lastVisiblePosition);
                numChildren++;
            }
            if (last.getBottom() < listBottom) {
                offsetChildrenTopAndBottom(listBottom - last.getBottom());
            }
            View first = getChildAt(0);
            while (first != null && first.getBottom() < listTop) {
                if (recycleBin.shouldRecycleViewType(((AbsListView.LayoutParams) first.getLayoutParams()).viewType)) {
                    recycleBin.addScrapView(first, this.mFirstPosition);
                }
                detachViewFromParent(first);
                first = getChildAt(0);
                this.mFirstPosition++;
            }
        } else {
            View first2 = getChildAt(0);
            while (first2.getTop() > listTop && this.mFirstPosition > 0) {
                first2 = addViewAbove(first2, this.mFirstPosition);
                this.mFirstPosition--;
            }
            if (first2.getTop() > listTop) {
                offsetChildrenTopAndBottom(listTop - first2.getTop());
            }
            int lastIndex = getChildCount() - 1;
            View last2 = getChildAt(lastIndex);
            while (last2 != null && last2.getTop() > listBottom) {
                if (recycleBin.shouldRecycleViewType(((AbsListView.LayoutParams) last2.getLayoutParams()).viewType)) {
                    recycleBin.addScrapView(last2, this.mFirstPosition + lastIndex);
                }
                detachViewFromParent(last2);
                lastIndex--;
                last2 = getChildAt(lastIndex);
            }
        }
        recycleBin.fullyDetachScrapViews();
        removeUnusedFixedViews(this.mHeaderViewInfos);
        removeUnusedFixedViews(this.mFooterViewInfos);
    }

    private View addViewAbove(View theView, int position) {
        int abovePosition = position - 1;
        View view = obtainView(abovePosition, this.mIsScrap);
        setupChild(view, abovePosition, theView.getTop() - this.mDividerHeight, false, this.mListPadding.left, false, this.mIsScrap[0]);
        return view;
    }

    private View addViewBelow(View theView, int position) {
        int belowPosition = position + 1;
        View view = obtainView(belowPosition, this.mIsScrap);
        setupChild(view, belowPosition, theView.getBottom() + this.mDividerHeight, true, this.mListPadding.left, false, this.mIsScrap[0]);
        return view;
    }

    public void setItemsCanFocus(boolean itemsCanFocus) {
        this.mItemsCanFocus = itemsCanFocus;
        if (!itemsCanFocus) {
            setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }
    }

    public boolean getItemsCanFocus() {
        return this.mItemsCanFocus;
    }

    public boolean isOpaque() {
        boolean retValue = (this.mCachingActive && this.mIsCacheColorOpaque && this.mDividerIsOpaque && hasOpaqueScrollbars()) || super.isOpaque();
        if (retValue) {
            int listTop = this.mListPadding != null ? this.mListPadding.top : this.mPaddingTop;
            View first = getChildAt(0);
            if (first == null || first.getTop() > listTop) {
                return false;
            }
            int listBottom = getHeight() - (this.mListPadding != null ? this.mListPadding.bottom : this.mPaddingBottom);
            View last = getChildAt(getChildCount() - 1);
            if (last == null || last.getBottom() < listBottom) {
                return false;
            }
        }
        return retValue;
    }

    public void setCacheColorHint(int color) {
        boolean opaque = (color >>> 24) == 255;
        this.mIsCacheColorOpaque = opaque;
        if (opaque) {
            if (this.mDividerPaint == null) {
                this.mDividerPaint = new Paint();
            }
            this.mDividerPaint.setColor(color);
        }
        super.setCacheColorHint(color);
    }

    /* access modifiers changed from: package-private */
    public void drawOverscrollHeader(Canvas canvas, Drawable drawable, Rect bounds) {
        int height = drawable.getMinimumHeight();
        canvas.save();
        canvas.clipRect(bounds);
        if (bounds.bottom - bounds.top < height) {
            bounds.top = bounds.bottom - height;
        }
        drawable.setBounds(bounds);
        drawable.draw(canvas);
        canvas.restore();
    }

    /* access modifiers changed from: package-private */
    public void drawOverscrollFooter(Canvas canvas, Drawable drawable, Rect bounds) {
        int height = drawable.getMinimumHeight();
        canvas.save();
        canvas.clipRect(bounds);
        if (bounds.bottom - bounds.top < height) {
            bounds.bottom = bounds.top + height;
        }
        drawable.setBounds(bounds);
        drawable.draw(canvas);
        canvas.restore();
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        ListAdapter adapter;
        int itemCount;
        int effectivePaddingTop;
        int effectivePaddingBottom;
        Drawable overscrollHeader;
        int i;
        boolean footerDividers;
        int start;
        int effectivePaddingTop2;
        Drawable overscrollFooter;
        int first;
        int bottom;
        Drawable overscrollFooter2;
        boolean drawDividers;
        int listBottom;
        boolean drawOverscrollHeader;
        ListAdapter adapter2;
        Paint paint;
        Canvas canvas2 = canvas;
        if (this.mCachingStarted) {
            this.mCachingActive = true;
        }
        int dividerHeight = this.mDividerHeight;
        Drawable overscrollHeader2 = this.mOverScrollHeader;
        Drawable overscrollFooter3 = this.mOverScrollFooter;
        boolean drawOverscrollHeader2 = overscrollHeader2 != null;
        boolean drawOverscrollFooter = overscrollFooter3 != null;
        boolean drawDividers2 = dividerHeight > 0 && this.mDivider != null;
        if (drawDividers2 || drawOverscrollHeader2 || drawOverscrollFooter) {
            Rect bounds = this.mTempRect;
            bounds.left = this.mPaddingLeft;
            bounds.right = (this.mRight - this.mLeft) - this.mPaddingRight;
            int count = getChildCount();
            int headerCount = getHeaderViewsCount();
            int itemCount2 = this.mItemCount;
            int footerLimit = itemCount2 - this.mFooterViewInfos.size();
            boolean headerDividers = this.mHeaderDividersEnabled;
            boolean footerDividers2 = this.mFooterDividersEnabled;
            int first2 = this.mFirstPosition;
            Drawable overscrollFooter4 = overscrollFooter3;
            boolean z = this.mAreAllItemsSelectable;
            ListAdapter adapter3 = this.mAdapter;
            boolean fillForMissingDividers = isOpaque() && !super.isOpaque();
            if (fillForMissingDividers) {
                itemCount = itemCount2;
                if (this.mDividerPaint != null || !this.mIsCacheColorOpaque) {
                    adapter = adapter3;
                } else {
                    this.mDividerPaint = new Paint();
                    adapter = adapter3;
                    this.mDividerPaint.setColor(getCacheColorHint());
                }
            } else {
                adapter = adapter3;
                itemCount = itemCount2;
            }
            Paint paint2 = this.mDividerPaint;
            if ((this.mGroupFlags & 34) == 34) {
                effectivePaddingTop = this.mListPadding.top;
                effectivePaddingBottom = this.mListPadding.bottom;
            } else {
                effectivePaddingBottom = 0;
                effectivePaddingTop = 0;
            }
            int effectivePaddingTop3 = effectivePaddingTop;
            boolean drawOverscrollFooter2 = drawOverscrollFooter;
            int listBottom2 = ((this.mBottom - this.mTop) - effectivePaddingBottom) + this.mScrollY;
            int i2 = effectivePaddingBottom;
            if (!this.mStackFromBottom) {
                int scrollY = this.mScrollY;
                if (count <= 0 || scrollY >= 0) {
                    bottom = 0;
                } else if (drawOverscrollHeader2) {
                    bottom = 0;
                    bounds.bottom = 0;
                    bounds.top = scrollY;
                    drawOverscrollHeader(canvas2, overscrollHeader2, bounds);
                } else {
                    bottom = 0;
                    if (drawDividers2) {
                        bounds.bottom = 0;
                        bounds.top = -dividerHeight;
                        drawDivider(canvas2, bounds, -1);
                    }
                }
                int i3 = scrollY;
                int bottom2 = bottom;
                int i4 = 0;
                while (i4 < count) {
                    Drawable overscrollHeader3 = overscrollHeader2;
                    int itemIndex = first2 + i4;
                    boolean isHeader = itemIndex < headerCount;
                    boolean isFooter = itemIndex >= footerLimit;
                    if ((headerDividers || !isHeader) && (footerDividers2 || !isFooter)) {
                        drawOverscrollHeader = drawOverscrollHeader2;
                        View child = getChildAt(i4);
                        bottom2 = child.getBottom();
                        View view = child;
                        boolean isLastItem = i4 == count + -1;
                        if (!drawDividers2 || bottom2 >= listBottom2) {
                            drawDividers = drawDividers2;
                            listBottom = listBottom2;
                            adapter2 = adapter;
                            paint = paint2;
                            i4++;
                            paint2 = paint;
                            adapter = adapter2;
                            overscrollHeader2 = overscrollHeader3;
                            drawOverscrollHeader2 = drawOverscrollHeader;
                            listBottom2 = listBottom;
                            drawDividers2 = drawDividers;
                        } else if (!drawOverscrollFooter2 || !isLastItem) {
                            listBottom = listBottom2;
                            int listBottom3 = itemIndex + 1;
                            drawDividers = drawDividers2;
                            adapter2 = adapter;
                            if (!checkIsEnabled(adapter2, itemIndex)) {
                            } else if ((headerDividers || (!isHeader && listBottom3 >= headerCount)) && (isLastItem || (checkIsEnabled(adapter2, listBottom3) && (footerDividers2 || (!isFooter && listBottom3 < footerLimit))))) {
                                bounds.top = bottom2;
                                int i5 = itemIndex;
                                bounds.bottom = bottom2 + dividerHeight;
                                drawDivider(canvas2, bounds, i4);
                                paint = paint2;
                                i4++;
                                paint2 = paint;
                                adapter = adapter2;
                                overscrollHeader2 = overscrollHeader3;
                                drawOverscrollHeader2 = drawOverscrollHeader;
                                listBottom2 = listBottom;
                                drawDividers2 = drawDividers;
                            } else {
                                int i6 = itemIndex;
                            }
                            if (fillForMissingDividers) {
                                bounds.top = bottom2;
                                bounds.bottom = bottom2 + dividerHeight;
                                paint = paint2;
                                canvas2.drawRect(bounds, paint);
                            } else {
                                paint = paint2;
                            }
                            i4++;
                            paint2 = paint;
                            adapter = adapter2;
                            overscrollHeader2 = overscrollHeader3;
                            drawOverscrollHeader2 = drawOverscrollHeader;
                            listBottom2 = listBottom;
                            drawDividers2 = drawDividers;
                        }
                    } else {
                        drawOverscrollHeader = drawOverscrollHeader2;
                    }
                    drawDividers = drawDividers2;
                    listBottom = listBottom2;
                    adapter2 = adapter;
                    paint = paint2;
                    i4++;
                    paint2 = paint;
                    adapter = adapter2;
                    overscrollHeader2 = overscrollHeader3;
                    drawOverscrollHeader2 = drawOverscrollHeader;
                    listBottom2 = listBottom;
                    drawDividers2 = drawDividers;
                }
                Drawable overscrollHeader4 = overscrollHeader2;
                boolean z2 = drawOverscrollHeader2;
                boolean z3 = drawDividers2;
                int i7 = listBottom2;
                ListAdapter listAdapter = adapter;
                Paint paint3 = paint2;
                int overFooterBottom = this.mBottom + this.mScrollY;
                if (!drawOverscrollFooter2) {
                    overscrollFooter2 = overscrollFooter4;
                    int i8 = itemCount;
                } else if (first2 + count != itemCount || overFooterBottom <= bottom2) {
                    overscrollFooter2 = overscrollFooter4;
                } else {
                    bounds.top = bottom2;
                    bounds.bottom = overFooterBottom;
                    overscrollFooter2 = overscrollFooter4;
                    drawOverscrollFooter(canvas2, overscrollFooter2, bounds);
                }
                Drawable drawable = overscrollFooter2;
                Drawable drawable2 = overscrollHeader4;
            } else {
                Drawable overscrollHeader5 = overscrollHeader2;
                boolean drawOverscrollHeader3 = drawOverscrollHeader2;
                boolean drawDividers3 = drawDividers2;
                int listBottom4 = listBottom2;
                Drawable overscrollFooter5 = overscrollFooter4;
                int itemCount3 = itemCount;
                ListAdapter adapter4 = adapter;
                Paint paint4 = paint2;
                int scrollY2 = this.mScrollY;
                if (count <= 0 || !drawOverscrollHeader3) {
                    overscrollHeader = overscrollHeader5;
                    i = 0;
                } else {
                    bounds.top = scrollY2;
                    int i9 = itemCount3;
                    i = 0;
                    bounds.bottom = getChildAt(0).getTop();
                    overscrollHeader = overscrollHeader5;
                    drawOverscrollHeader(canvas2, overscrollHeader, bounds);
                }
                int i10 = drawOverscrollHeader3 ? 1 : i;
                int start2 = i10;
                while (true) {
                    int i11 = i10;
                    if (i11 >= count) {
                        break;
                    }
                    Drawable overscrollHeader6 = overscrollHeader;
                    int itemIndex2 = first2 + i11;
                    boolean isHeader2 = itemIndex2 < headerCount;
                    boolean isFooter2 = itemIndex2 >= footerLimit;
                    if ((headerDividers || !isHeader2) && (footerDividers2 || !isFooter2)) {
                        first = first2;
                        View child2 = getChildAt(i11);
                        overscrollFooter = overscrollFooter5;
                        int top = child2.getTop();
                        if (drawDividers3) {
                            View view2 = child2;
                            int effectivePaddingTop4 = effectivePaddingTop3;
                            if (top > effectivePaddingTop4) {
                                effectivePaddingTop2 = effectivePaddingTop4;
                                int start3 = start2;
                                boolean isFirstItem = i11 == start3;
                                start = start3;
                                int previousIndex = itemIndex2 - 1;
                                if (!checkIsEnabled(adapter4, itemIndex2)) {
                                    footerDividers = footerDividers2;
                                } else if ((headerDividers || (!isHeader2 && previousIndex >= headerCount)) && (isFirstItem || (checkIsEnabled(adapter4, previousIndex) && (footerDividers2 || (!isFooter2 && previousIndex < footerLimit))))) {
                                    footerDividers = footerDividers2;
                                    bounds.top = top - dividerHeight;
                                    bounds.bottom = top;
                                    drawDivider(canvas2, bounds, i11 - 1);
                                } else {
                                    footerDividers = footerDividers2;
                                }
                                if (fillForMissingDividers) {
                                    bounds.top = top - dividerHeight;
                                    bounds.bottom = top;
                                    canvas2.drawRect(bounds, paint4);
                                }
                            } else {
                                footerDividers = footerDividers2;
                                effectivePaddingTop2 = effectivePaddingTop4;
                                start = start2;
                            }
                        } else {
                            footerDividers = footerDividers2;
                            effectivePaddingTop2 = effectivePaddingTop3;
                            start = start2;
                        }
                    } else {
                        footerDividers = footerDividers2;
                        first = first2;
                        overscrollFooter = overscrollFooter5;
                        effectivePaddingTop2 = effectivePaddingTop3;
                        start = start2;
                    }
                    i10 = i11 + 1;
                    overscrollHeader = overscrollHeader6;
                    first2 = first;
                    overscrollFooter5 = overscrollFooter;
                    effectivePaddingTop3 = effectivePaddingTop2;
                    start2 = start;
                    footerDividers2 = footerDividers;
                }
                int i12 = first2;
                Drawable overscrollFooter6 = overscrollFooter5;
                Drawable drawable3 = overscrollHeader;
                int i13 = effectivePaddingTop3;
                int i14 = start2;
                if (count <= 0 || scrollY2 <= 0) {
                } else if (drawOverscrollFooter2) {
                    int absListBottom = this.mBottom;
                    bounds.top = absListBottom;
                    bounds.bottom = absListBottom + scrollY2;
                    drawOverscrollFooter(canvas2, overscrollFooter6, bounds);
                } else {
                    if (drawDividers3) {
                        int listBottom5 = listBottom4;
                        bounds.top = listBottom5;
                        bounds.bottom = listBottom5 + dividerHeight;
                        drawDivider(canvas2, bounds, -1);
                    }
                }
            }
        } else {
            Drawable drawable4 = overscrollHeader2;
            Drawable drawable5 = overscrollFooter3;
            boolean z4 = drawOverscrollHeader2;
            boolean z5 = drawOverscrollFooter;
            boolean z6 = drawDividers2;
        }
        super.dispatchDraw(canvas);
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean more = super.drawChild(canvas, child, drawingTime);
        if (this.mCachingActive && child.mCachingFailed) {
            this.mCachingActive = false;
        }
        return more;
    }

    /* access modifiers changed from: package-private */
    public void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        Drawable divider = this.mDivider;
        divider.setBounds(bounds);
        divider.draw(canvas);
    }

    public Drawable getDivider() {
        return this.mDivider;
    }

    public void setDivider(Drawable divider) {
        boolean z = false;
        if (divider != null) {
            this.mDividerHeight = divider.getIntrinsicHeight();
        } else {
            this.mDividerHeight = 0;
        }
        this.mDivider = divider;
        if (divider == null || divider.getOpacity() == -1) {
            z = true;
        }
        this.mDividerIsOpaque = z;
        requestLayout();
        invalidate();
    }

    public int getDividerHeight() {
        return this.mDividerHeight;
    }

    public void setDividerHeight(int height) {
        this.mDividerHeight = height;
        requestLayout();
        invalidate();
    }

    public void setHeaderDividersEnabled(boolean headerDividersEnabled) {
        this.mHeaderDividersEnabled = headerDividersEnabled;
        invalidate();
    }

    public boolean areHeaderDividersEnabled() {
        return this.mHeaderDividersEnabled;
    }

    public void setFooterDividersEnabled(boolean footerDividersEnabled) {
        this.mFooterDividersEnabled = footerDividersEnabled;
        invalidate();
    }

    public boolean areFooterDividersEnabled() {
        return this.mFooterDividersEnabled;
    }

    public void setOverscrollHeader(Drawable header) {
        this.mOverScrollHeader = header;
        if (this.mScrollY < 0) {
            invalidate();
        }
    }

    public Drawable getOverscrollHeader() {
        return this.mOverScrollHeader;
    }

    public void setOverscrollFooter(Drawable footer) {
        this.mOverScrollFooter = footer;
        invalidate();
    }

    public Drawable getOverscrollFooter() {
        return this.mOverScrollFooter;
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        ListAdapter adapter = this.mAdapter;
        int closetChildIndex = -1;
        int closestChildTop = 0;
        if (!(adapter == null || !gainFocus || previouslyFocusedRect == null)) {
            previouslyFocusedRect.offset(this.mScrollX, this.mScrollY);
            if (adapter.getCount() < getChildCount() + this.mFirstPosition) {
                this.mLayoutMode = 0;
                layoutChildren();
            }
            Rect otherRect = this.mTempRect;
            int minDistance = Integer.MAX_VALUE;
            int childCount = getChildCount();
            int firstPosition = this.mFirstPosition;
            for (int i = 0; i < childCount; i++) {
                if (adapter.isEnabled(firstPosition + i)) {
                    View other = getChildAt(i);
                    other.getDrawingRect(otherRect);
                    offsetDescendantRectToMyCoords(other, otherRect);
                    int distance = getDistance(previouslyFocusedRect, otherRect, direction);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closetChildIndex = i;
                        closestChildTop = other.getTop();
                    }
                }
            }
        }
        if (closetChildIndex >= 0) {
            setSelectionFromTop(this.mFirstPosition + closetChildIndex, closestChildTop);
        } else {
            requestLayout();
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                addHeaderView(getChildAt(i));
            }
            removeAllViews();
        }
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewTraversal(int id) {
        View v = super.findViewTraversal(id);
        if (v == null) {
            View v2 = findViewInHeadersOrFooters(this.mHeaderViewInfos, id);
            if (v2 != null) {
                return v2;
            }
            v = findViewInHeadersOrFooters(this.mFooterViewInfos, id);
            if (v != null) {
                return v;
            }
        }
        return v;
    }

    /* access modifiers changed from: package-private */
    public View findViewInHeadersOrFooters(ArrayList<FixedViewInfo> where, int id) {
        if (where != null) {
            int len = where.size();
            for (int i = 0; i < len; i++) {
                View v = where.get(i).view;
                if (!v.isRootNamespace()) {
                    View v2 = v.findViewById(id);
                    if (v2 != null) {
                        return v2;
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewWithTagTraversal(Object tag) {
        View v = super.findViewWithTagTraversal(tag);
        if (v == null) {
            View v2 = findViewWithTagInHeadersOrFooters(this.mHeaderViewInfos, tag);
            if (v2 != null) {
                return v2;
            }
            v = findViewWithTagInHeadersOrFooters(this.mFooterViewInfos, tag);
            if (v != null) {
                return v;
            }
        }
        return v;
    }

    /* access modifiers changed from: package-private */
    public View findViewWithTagInHeadersOrFooters(ArrayList<FixedViewInfo> where, Object tag) {
        if (where != null) {
            int len = where.size();
            for (int i = 0; i < len; i++) {
                View v = where.get(i).view;
                if (!v.isRootNamespace()) {
                    View v2 = v.findViewWithTag(tag);
                    if (v2 != null) {
                        return v2;
                    }
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        View v = super.findViewByPredicateTraversal(predicate, childToSkip);
        if (v == null) {
            View v2 = findViewByPredicateInHeadersOrFooters(this.mHeaderViewInfos, predicate, childToSkip);
            if (v2 != null) {
                return v2;
            }
            v = findViewByPredicateInHeadersOrFooters(this.mFooterViewInfos, predicate, childToSkip);
            if (v != null) {
                return v;
            }
        }
        return v;
    }

    /* access modifiers changed from: package-private */
    public View findViewByPredicateInHeadersOrFooters(ArrayList<FixedViewInfo> where, Predicate<View> predicate, View childToSkip) {
        if (where != null) {
            int len = where.size();
            for (int i = 0; i < len; i++) {
                View v = where.get(i).view;
                if (v != childToSkip && !v.isRootNamespace()) {
                    View v2 = v.findViewByPredicate(predicate);
                    if (v2 != null) {
                        return v2;
                    }
                }
            }
        }
        return null;
    }

    @Deprecated
    public long[] getCheckItemIds() {
        if (this.mAdapter != null && this.mAdapter.hasStableIds()) {
            return getCheckedItemIds();
        }
        if (this.mChoiceMode == 0 || this.mCheckStates == null || this.mAdapter == null) {
            return new long[0];
        }
        SparseBooleanArray states = this.mCheckStates;
        int count = states.size();
        long[] ids = new long[count];
        ListAdapter adapter = this.mAdapter;
        int checkedCount = 0;
        for (int i = 0; i < count; i++) {
            if (states.valueAt(i)) {
                ids[checkedCount] = adapter.getItemId(states.keyAt(i));
                checkedCount++;
            }
        }
        if (checkedCount == count) {
            return ids;
        }
        long[] result = new long[checkedCount];
        System.arraycopy(ids, 0, result, 0, checkedCount);
        return result;
    }

    /* access modifiers changed from: package-private */
    public int getHeightForPosition(int position) {
        int height = super.getHeightForPosition(position);
        if (shouldAdjustHeightForDivider(position)) {
            return this.mDividerHeight + height;
        }
        return height;
    }

    private boolean shouldAdjustHeightForDivider(int itemIndex) {
        int i = itemIndex;
        int dividerHeight = this.mDividerHeight;
        Drawable overscrollHeader = this.mOverScrollHeader;
        Drawable overscrollFooter = this.mOverScrollFooter;
        boolean drawOverscrollHeader = overscrollHeader != null;
        boolean drawOverscrollFooter = overscrollFooter != null;
        if (dividerHeight > 0 && this.mDivider != null) {
            boolean fillForMissingDividers = isOpaque() && !super.isOpaque();
            int itemCount = this.mItemCount;
            int headerCount = getHeaderViewsCount();
            int footerLimit = itemCount - this.mFooterViewInfos.size();
            boolean isHeader = i < headerCount;
            boolean isFooter = i >= footerLimit;
            boolean headerDividers = this.mHeaderDividersEnabled;
            boolean footerDividers = this.mFooterDividersEnabled;
            if ((headerDividers || !isHeader) && (footerDividers || !isFooter)) {
                int i2 = dividerHeight;
                ListAdapter adapter = this.mAdapter;
                Drawable drawable = overscrollHeader;
                if (!this.mStackFromBottom) {
                    boolean isLastItem = i == itemCount + -1;
                    if (!drawOverscrollFooter || !isLastItem) {
                        Drawable drawable2 = overscrollFooter;
                        int nextIndex = i + 1;
                        if (checkIsEnabled(adapter, i) && ((headerDividers || (!isHeader && nextIndex >= headerCount)) && (isLastItem || (checkIsEnabled(adapter, nextIndex) && (footerDividers || (!isFooter && nextIndex < footerLimit)))))) {
                            return true;
                        }
                        if (fillForMissingDividers) {
                            return true;
                        }
                    } else {
                        Drawable drawable3 = overscrollFooter;
                    }
                } else {
                    int start = drawOverscrollHeader ? 1 : 0;
                    boolean isFirstItem = i == start;
                    if (!isFirstItem) {
                        int i3 = start;
                        int previousIndex = i - 1;
                        if (checkIsEnabled(adapter, i) && ((headerDividers || (!isHeader && previousIndex >= headerCount)) && (isFirstItem || (checkIsEnabled(adapter, previousIndex) && (footerDividers || (!isFooter && previousIndex < footerLimit)))))) {
                            return true;
                        }
                        if (fillForMissingDividers) {
                            return true;
                        }
                    }
                }
            } else {
                int i4 = dividerHeight;
                Drawable drawable4 = overscrollHeader;
                Drawable drawable5 = overscrollFooter;
            }
        } else {
            Drawable drawable6 = overscrollHeader;
            Drawable drawable7 = overscrollFooter;
        }
        return false;
    }

    public CharSequence getAccessibilityClassName() {
        return ListView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        int rowsCount = getCount();
        info.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(rowsCount, 1, false, getSelectionModeForAccessibility()));
        if (rowsCount > 0) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION);
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (action == 16908343) {
            int row = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, -1);
            int position = Math.min(row, getCount() - 1);
            if (row >= 0) {
                smoothScrollToPosition(position);
                return true;
            }
        }
        return false;
    }

    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoForItem(view, position, info);
        AbsListView.LayoutParams lp = (AbsListView.LayoutParams) view.getLayoutParams();
        info.setCollectionItemInfo(AccessibilityNodeInfo.CollectionItemInfo.obtain(position, 1, 0, 1, lp != null && lp.viewType == -2, isItemChecked(position)));
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("recycleOnMeasure", recycleOnMeasure());
    }

    /* access modifiers changed from: protected */
    public HeaderViewListAdapter wrapHeaderListAdapterInternal(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footerViewInfos, ListAdapter adapter) {
        return new HeaderViewListAdapter(headerViewInfos, footerViewInfos, adapter);
    }

    /* access modifiers changed from: protected */
    public void wrapHeaderListAdapterInternal() {
        this.mAdapter = wrapHeaderListAdapterInternal(this.mHeaderViewInfos, this.mFooterViewInfos, this.mAdapter);
    }

    /* access modifiers changed from: protected */
    public void dispatchDataSetObserverOnChangedInternal() {
        if (this.mDataSetObserver != null) {
            this.mDataSetObserver.onChanged();
        }
    }
}
