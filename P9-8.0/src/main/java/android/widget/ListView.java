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
import android.util.LogException;
import android.util.MathUtils;
import android.util.SparseBooleanArray;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.RemotableViewMethod;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeInfo.CollectionInfo;
import android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.widget.AbsListView.LayoutParams;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@RemoteView
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

        /* synthetic */ ArrowScrollFocusResult(ArrowScrollFocusResult -this0) {
            this();
        }

        private ArrowScrollFocusResult() {
        }

        void populate(int selectedPosition, int amountToScroll) {
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
    }

    private class FocusSelector implements Runnable {
        private static final int STATE_REQUEST_FOCUS = 3;
        private static final int STATE_SET_SELECTION = 1;
        private static final int STATE_WAIT_FOR_LAYOUT = 2;
        private int mAction;
        private int mPosition;
        private int mPositionTop;

        /* synthetic */ FocusSelector(ListView this$0, FocusSelector -this1) {
            this();
        }

        private FocusSelector() {
        }

        FocusSelector setupForSetSelection(int position, int top) {
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

        Runnable setupFocusIfValid(int position) {
            if (this.mAction != 2 || position != this.mPosition) {
                return null;
            }
            this.mAction = 3;
            return this;
        }

        void onLayoutComplete() {
            if (this.mAction == 2) {
                this.mAction = -1;
            }
        }
    }

    public ListView(Context context) {
        this(context, null);
    }

    public ListView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.listViewStyle);
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
        Object[] entries = a.getTextArray(0);
        if (entries != null) {
            setAdapter(new ArrayAdapter(context, (int) R.layout.simple_list_item_1, entries));
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
        return (int) (((float) (this.mBottom - this.mTop)) * MAX_SCROLL_FACTOR);
    }

    private void adjustViewsUpOrDown() {
        int childCount = getChildCount();
        if (childCount > 0) {
            int delta;
            if (this.mStackFromBottom) {
                delta = getChildAt(childCount - 1).getBottom() - (getHeight() - this.mListPadding.bottom);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta += this.mDividerHeight;
                }
                if (delta > 0) {
                    delta = 0;
                }
            } else {
                delta = getChildAt(0).getTop() - this.mListPadding.top;
                if (this.mFirstPosition != 0) {
                    delta -= this.mDividerHeight;
                }
                if (delta < 0) {
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
            if (((FixedViewInfo) where.get(i)).view == v) {
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
            int position;
            this.mAreAllItemsSelectable = this.mAdapter.areAllItemsEnabled();
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
            checkFocus();
            this.mDataSetObserver = new AdapterDataSetObserver();
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
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

    void resetList() {
        clearRecycledState(this.mHeaderViewInfos);
        clearRecycledState(this.mFooterViewInfos);
        super.resetList();
        this.mLayoutMode = 0;
    }

    private void clearRecycledState(ArrayList<FixedViewInfo> infos) {
        if (infos != null) {
            int count = infos.size();
            for (int i = 0; i < count; i++) {
                LayoutParams p = (LayoutParams) ((FixedViewInfo) infos.get(i)).view.getLayoutParams();
                if (p != null) {
                    p.recycledHeaderFooter = false;
                }
            }
        }
    }

    private boolean showingTopFadingEdge() {
        int listTop = this.mScrollY + this.mListPadding.top;
        if (this.mFirstPosition > 0 || getChildAt(0).getTop() > listTop) {
            return true;
        }
        return false;
    }

    private boolean showingBottomFadingEdge() {
        int childCount = getChildCount();
        int bottomOfBottomChild = getChildAt(childCount - 1).getBottom();
        int listBottom = (this.mScrollY + getHeight()) - this.mListPadding.bottom;
        if ((this.mFirstPosition + childCount) - 1 < this.mItemCount - 1 || bottomOfBottomChild < listBottom) {
            return true;
        }
        return false;
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
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
        int scrollYDelta = 0;
        if (rect.bottom > listUnfadedBottom && rect.top > listUnfadedTop) {
            if (rect.height() > height) {
                scrollYDelta = (rect.top - listUnfadedTop) + 0;
            } else {
                scrollYDelta = (rect.bottom - listUnfadedBottom) + 0;
            }
            scrollYDelta = Math.min(scrollYDelta, bottomOfBottomChild - listUnfadedBottom);
        } else if (rect.top < listUnfadedTop && rect.bottom < listUnfadedBottom) {
            if (rect.height() > height) {
                scrollYDelta = 0 - (listUnfadedBottom - rect.bottom);
            } else {
                scrollYDelta = 0 - (listUnfadedTop - rect.top);
            }
            scrollYDelta = Math.max(scrollYDelta, getChildAt(0).getTop() - listUnfadedTop);
        }
        boolean scroll = scrollYDelta != 0;
        if (scroll) {
            scrollListItemsBy(-scrollYDelta);
            positionSelector(-1, child);
            this.mSelectedTop = child.getTop();
            invalidate();
        }
        return scroll;
    }

    void fillGap(boolean down) {
        int count = getChildCount();
        int startOffset;
        if (down) {
            int paddingTop = 0;
            if ((this.mGroupFlags & 34) == 34) {
                paddingTop = getListPaddingTop();
            }
            if (count > 0) {
                startOffset = getChildAt(count - 1).getBottom() + this.mDividerHeight;
            } else {
                startOffset = paddingTop;
            }
            fillDown(this.mFirstPosition + count, startOffset);
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
        while (nextTop < end && pos < calculateMaxPos()) {
            boolean selected = pos == this.mSelectedPosition;
            View child = makeAndAddView(calculatePos(pos), nextTop, true, this.mListPadding.left, selected);
            if (child == null) {
                break;
            }
            nextTop = child.getBottom() + this.mDividerHeight;
            if (selected) {
                selectedView = child;
            }
            pos++;
        }
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
        return selectedView;
    }

    private View fillUp(int pos, int nextBottom) {
        View selectedView = null;
        int end = 0;
        if ((this.mGroupFlags & 34) == 34) {
            end = this.mListPadding.top;
        }
        while (nextBottom > end && pos >= calculateMinPos()) {
            boolean selected = pos == this.mSelectedPosition;
            View child = makeAndAddView(calculatePos(pos), nextBottom, false, this.mListPadding.left, selected);
            if (child == null) {
                break;
            }
            nextBottom = child.getTop() - this.mDividerHeight;
            if (selected) {
                selectedView = child;
            }
            pos--;
        }
        this.mFirstPosition = pos + 1;
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
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
        if (this.mStackFromBottom) {
            correctTooLow(getChildCount());
        } else {
            correctTooHigh(getChildCount());
        }
        return sel;
    }

    private void fillAboveAndBelow(View sel, int position) {
        int dividerHeight = this.mDividerHeight;
        if (this.mStackFromBottom) {
            fillDown(position + 1, sel.getBottom() + dividerHeight);
            adjustViewsUpOrDown();
            fillUp(position - 1, sel.getTop() - dividerHeight);
            return;
        }
        fillUp(position - 1, sel.getTop() - dividerHeight);
        adjustViewsUpOrDown();
        fillDown(position + 1, sel.getBottom() + dividerHeight);
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
        if (this.mStackFromBottom) {
            correctTooLow(getChildCount());
        } else {
            correctTooHigh(getChildCount());
        }
        return sel;
    }

    private int getBottomSelectionPixel(int childrenBottom, int fadingEdgeLength, int selectedPosition) {
        int bottomSelectionPixel = childrenBottom;
        if (selectedPosition != this.mItemCount - 1) {
            return childrenBottom - fadingEdgeLength;
        }
        return bottomSelectionPixel;
    }

    private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength, int selectedPosition) {
        int topSelectionPixel = childrenTop;
        if (selectedPosition > 0) {
            return childrenTop + fadingEdgeLength;
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
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, selectedPosition);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenTop, fadingEdgeLength, selectedPosition);
        int halfVerticalSpace;
        if (delta > 0) {
            oldSel = makeAndAddView(selectedPosition - 1, oldSel.getTop(), true, this.mListPadding.left, false);
            int dividerHeight = this.mDividerHeight;
            sel = makeAndAddView(selectedPosition, oldSel.getBottom() + dividerHeight, true, this.mListPadding.left, true);
            if (sel.getBottom() > bottomSelectionPixel) {
                halfVerticalSpace = (childrenBottom - childrenTop) / 2;
                int offset = Math.min(Math.min(sel.getTop() - topSelectionPixel, sel.getBottom() - bottomSelectionPixel), halfVerticalSpace);
                oldSel.offsetTopAndBottom(-offset);
                sel.offsetTopAndBottom(-offset);
            }
            if (this.mStackFromBottom) {
                fillDown(this.mSelectedPosition + 1, sel.getBottom() + dividerHeight);
                adjustViewsUpOrDown();
                fillUp(this.mSelectedPosition - 2, sel.getTop() - dividerHeight);
            } else {
                fillUp(this.mSelectedPosition - 2, sel.getTop() - dividerHeight);
                adjustViewsUpOrDown();
                fillDown(this.mSelectedPosition + 1, sel.getBottom() + dividerHeight);
            }
        } else if (delta < 0) {
            if (newSel != null) {
                sel = makeAndAddView(selectedPosition, newSel.getTop(), true, this.mListPadding.left, true);
            } else {
                sel = makeAndAddView(selectedPosition, oldSel.getTop(), false, this.mListPadding.left, true);
            }
            if (sel.getTop() < topSelectionPixel) {
                halfVerticalSpace = (childrenBottom - childrenTop) / 2;
                sel.offsetTopAndBottom(Math.min(Math.min(topSelectionPixel - sel.getTop(), bottomSelectionPixel - sel.getBottom()), halfVerticalSpace));
            }
            fillAboveAndBelow(sel, selectedPosition);
        } else {
            int oldTop = oldSel.getTop();
            sel = makeAndAddView(selectedPosition, oldTop, true, this.mListPadding.left, true);
            if (oldTop < childrenTop && sel.getBottom() < childrenTop + 20) {
                sel.offsetTopAndBottom(childrenTop - sel.getTop());
            }
            fillAboveAndBelow(sel, selectedPosition);
        }
        return sel;
    }

    protected void onDetachedFromWindow() {
        if (this.mFocusSelector != null) {
            removeCallbacks(this.mFocusSelector);
            this.mFocusSelector = null;
        }
        super.onDetachedFromWindow();
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (getChildCount() > 0) {
            View focusedChild = getFocusedChild();
            if (focusedChild != null) {
                int childPosition = this.mFirstPosition + indexOfChild(focusedChild);
                int top = focusedChild.getTop() - Math.max(0, focusedChild.getBottom() - (h - this.mPaddingTop));
                if (this.mFocusSelector == null) {
                    this.mFocusSelector = new FocusSelector(this, null);
                }
                post(this.mFocusSelector.setupForSetSelection(childPosition, top));
            }
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int childWidth = 0;
        int childHeight = 0;
        int childState = 0;
        if (this.mAdapter == null) {
            i = 0;
        } else {
            i = this.mAdapter.getCount();
        }
        this.mItemCount = i;
        if (this.mItemCount > 0 && (widthMode == 0 || heightMode == 0)) {
            View child = obtainView(0, this.mIsScrap);
            measureScrapChild(child, 0, widthMeasureSpec, heightSize);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            childState = View.combineMeasuredStates(0, child.getMeasuredState());
            if (recycleOnMeasure() && this.mRecycler.shouldRecycleViewType(((LayoutParams) child.getLayoutParams()).viewType)) {
                this.mRecycler.addScrapView(child, 0);
            }
        }
        if (widthMode == 0) {
            widthSize = ((this.mListPadding.left + this.mListPadding.right) + childWidth) + getVerticalScrollbarWidth();
        } else {
            widthSize |= -16777216 & childState;
        }
        if (heightMode == 0) {
            heightSize = ((this.mListPadding.top + this.mListPadding.bottom) + childHeight) + (getVerticalFadingEdgeLength() * 2);
        }
        if (heightMode == Integer.MIN_VALUE) {
            heightSize = measureHeightOfChildren(widthMeasureSpec, 0, -1, heightSize, -1);
        }
        -wrap6(widthSize, heightSize);
        this.mWidthMeasureSpec = widthMeasureSpec;
    }

    private void measureScrapChild(View child, int position, int widthMeasureSpec, int heightHint) {
        int childHeightSpec;
        LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) generateDefaultLayoutParams();
            child.-wrap18(p);
        }
        p.viewType = this.mAdapter.getItemViewType(position);
        p.isEnabled = this.mAdapter.isEnabled(position);
        p.forceAdd = true;
        int childWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
        int lpHeight = p.height;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = MeasureSpec.makeSafeMeasureSpec(heightHint, 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
        child.forceLayout();
    }

    @ExportedProperty(category = "list")
    protected boolean recycleOnMeasure() {
        return true;
    }

    final int measureHeightOfChildren(int widthMeasureSpec, int startPosition, int endPosition, int maxHeight, int disallowPartialChildPosition) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null) {
            return this.mListPadding.top + this.mListPadding.bottom;
        }
        int returnedHeight = this.mListPadding.top + this.mListPadding.bottom;
        int dividerHeight = this.mDividerHeight;
        int prevHeightWithoutPartialChild = 0;
        if (endPosition == -1) {
            endPosition = adapter.getCount() - 1;
        }
        RecycleBin recycleBin = this.mRecycler;
        boolean recyle = recycleOnMeasure();
        boolean[] isScrap = this.mIsScrap;
        int i = startPosition;
        while (i <= endPosition) {
            View child = obtainView(i, isScrap);
            measureScrapChild(child, i, widthMeasureSpec, maxHeight);
            if (i > 0) {
                returnedHeight += dividerHeight;
            }
            if (recyle && recycleBin.shouldRecycleViewType(((LayoutParams) child.getLayoutParams()).viewType)) {
                recycleBin.addScrapView(child, -1);
            }
            returnedHeight += child.getMeasuredHeight();
            if (returnedHeight >= maxHeight) {
                if (disallowPartialChildPosition < 0 || i <= disallowPartialChildPosition || prevHeightWithoutPartialChild <= 0 || returnedHeight == maxHeight) {
                    prevHeightWithoutPartialChild = maxHeight;
                }
                return prevHeightWithoutPartialChild;
            }
            if (disallowPartialChildPosition >= 0 && i >= disallowPartialChildPosition) {
                prevHeightWithoutPartialChild = returnedHeight;
            }
            i++;
        }
        return returnedHeight;
    }

    int findMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int i;
            if (this.mStackFromBottom) {
                for (i = childCount - 1; i >= 0; i--) {
                    if (y >= getChildAt(i).getTop()) {
                        return this.mFirstPosition + i;
                    }
                }
            } else {
                for (i = 0; i < childCount; i++) {
                    if (y <= getChildAt(i).getBottom()) {
                        return this.mFirstPosition + i;
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
        int childCount;
        if (this.mStackFromBottom) {
            below = fillDown(position + 1, temp.getBottom() + dividerHeight);
            adjustViewsUpOrDown();
            above = fillUp(position - 1, temp.getTop() - dividerHeight);
            childCount = getChildCount();
            if (childCount > 0) {
                correctTooLow(childCount);
            }
        } else {
            above = fillUp(position - 1, temp.getTop() - dividerHeight);
            adjustViewsUpOrDown();
            below = fillDown(position + 1, temp.getBottom() + dividerHeight);
            childCount = getChildCount();
            if (childCount > 0) {
                correctTooHigh(childCount);
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
        if ((this.mFirstPosition + childCount) - 1 >= this.mItemCount - 1 && childCount > 0) {
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
            int end = (this.mBottom - this.mTop) - this.mListPadding.bottom;
            int topOffset = getChildAt(0).getTop() - this.mListPadding.top;
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

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a8 A:{Catch:{ all -> 0x0144 }} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00e7 A:{Catch:{ all -> 0x0144 }} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00b1 A:{Catch:{ all -> 0x0144 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void layoutChildren() {
        boolean blockLayoutRequests = this.mBlockLayoutRequests;
        if (!blockLayoutRequests) {
            this.mBlockLayoutRequests = true;
            try {
                super.layoutChildren();
                invalidate();
                if (this.mAdapter == null) {
                    resetList();
                    invokeOnItemScrollListener();
                    if (this.mFocusSelector != null) {
                        this.mFocusSelector.onLayoutComplete();
                    }
                    if (!blockLayoutRequests) {
                        this.mBlockLayoutRequests = false;
                    }
                    return;
                }
                boolean dataChanged;
                int childrenTop = this.mListPadding.top;
                int childrenBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
                int childCount = getChildCount();
                int delta = 0;
                View oldSel = null;
                View oldFirst = null;
                View newSel = null;
                int index;
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
                            if (!blockLayoutRequests) {
                                this.mBlockLayoutRequests = false;
                            }
                            return;
                        } else if (this.mItemCount != this.mAdapter.getCount()) {
                            throw new IllegalStateException("The content of the adapter has changed but ListView did not receive a notification. Make sure the content of your adapter is not modified from a background thread, but only from the UI thread. Make sure your adapter calls notifyDataSetChanged() when its content changes. [in ListView(" + getId() + ", " + getClass() + ") with Adapter(" + this.mAdapter.getClass() + ")]");
                        } else {
                            View sel;
                            setSelectedPositionInt(this.mNextSelectedPosition);
                            AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode = null;
                            View accessibilityFocusLayoutRestoreView = null;
                            int accessibilityFocusPosition = -1;
                            ViewRootImpl viewRootImpl = getViewRootImpl();
                            if (viewRootImpl != null) {
                                View focusHost = viewRootImpl.getAccessibilityFocusedHost();
                                if (focusHost != null) {
                                    View focusChild = getAccessibilityFocusedChild(focusHost);
                                    if (focusChild != null) {
                                        if (!dataChanged || isDirectChildHeaderOrFooter(focusChild) || (focusChild.hasTransientState() && this.mAdapterHasStableIds)) {
                                            accessibilityFocusLayoutRestoreView = focusHost;
                                            accessibilityFocusLayoutRestoreNode = viewRootImpl.getAccessibilityFocusedVirtualView();
                                        }
                                        accessibilityFocusPosition = getPositionForView(focusChild);
                                    }
                                }
                            }
                            View focusLayoutRestoreDirectChild = null;
                            View focusLayoutRestoreView = null;
                            View focusedChild = getFocusedChild();
                            if (focusedChild != null) {
                                if (!dataChanged || isDirectChildHeaderOrFooter(focusedChild) || focusedChild.hasTransientState() || this.mAdapterHasStableIds) {
                                    focusLayoutRestoreDirectChild = focusedChild;
                                    focusLayoutRestoreView = findFocus();
                                    if (focusLayoutRestoreView != null) {
                                        focusLayoutRestoreView.dispatchStartTemporaryDetach();
                                    }
                                }
                                requestFocus();
                            }
                            int firstPosition = this.mFirstPosition;
                            RecycleBin recycleBin = this.mRecycler;
                            if (dataChanged) {
                                for (int i = 0; i < childCount; i++) {
                                    recycleBin.addScrapView(getChildAt(i), firstPosition + i);
                                }
                            } else {
                                recycleBin.fillActiveViews(childCount, firstPosition);
                            }
                            detachAllViewsFromParent();
                            recycleBin.removeSkippedScrap();
                            switch (this.mLayoutMode) {
                                case 1:
                                    this.mFirstPosition = 0;
                                    sel = fillFromTop(childrenTop);
                                    adjustViewsUpOrDown();
                                    break;
                                case 2:
                                    if (newSel == null) {
                                        sel = fillFromMiddle(childrenTop, childrenBottom);
                                        break;
                                    } else {
                                        sel = fillFromSelection(newSel.getTop(), childrenTop, childrenBottom);
                                        break;
                                    }
                                case 3:
                                    sel = fillUp(this.mItemCount - 1, childrenBottom);
                                    adjustViewsUpOrDown();
                                    break;
                                case 4:
                                    int selectedPosition = reconcileSelectedPosition();
                                    sel = fillSpecific(selectedPosition, this.mSpecificTop);
                                    if (sel == null && this.mFocusSelector != null) {
                                        Runnable focusRunnable = this.mFocusSelector.setupFocusIfValid(selectedPosition);
                                        if (focusRunnable != null) {
                                            post(focusRunnable);
                                            break;
                                        }
                                    }
                                    break;
                                case 5:
                                    this.mSyncPosition = calculateSyncPosition(this.mSyncPosition);
                                    sel = fillSpecific(this.mSyncPosition, this.mSpecificTop);
                                    break;
                                case 6:
                                    sel = moveSelection(oldSel, newSel, delta, childrenTop, childrenBottom);
                                    break;
                                default:
                                    if (childCount != 0) {
                                        int i2;
                                        if (this.mSelectedPosition < 0 || this.mSelectedPosition >= this.mItemCount) {
                                            if (this.mFirstPosition >= this.mItemCount && !isDoingDeleteAnimation()) {
                                                sel = fillSpecific(0, childrenTop);
                                                break;
                                            }
                                            i2 = this.mFirstPosition;
                                            if (oldFirst != null) {
                                                childrenTop = oldFirst.getTop();
                                            }
                                            sel = fillSpecific(i2, childrenTop);
                                            break;
                                        }
                                        i2 = this.mSelectedPosition;
                                        if (oldSel != null) {
                                            childrenTop = oldSel.getTop();
                                        }
                                        sel = fillSpecific(i2, childrenTop);
                                        break;
                                    } else if (!this.mStackFromBottom) {
                                        setSelectedPositionInt(lookForSelectablePosition(0, true));
                                        sel = fillFromTop(childrenTop);
                                        break;
                                    } else {
                                        setSelectedPositionInt(lookForSelectablePosition(this.mItemCount - 1, false));
                                        sel = fillUp(this.mItemCount - 1, childrenBottom);
                                        break;
                                    }
                                    break;
                            }
                            recycleBin.scrapActiveViews();
                            removeUnusedFixedViews(this.mHeaderViewInfos);
                            removeUnusedFixedViews(this.mFooterViewInfos);
                            if (sel != null) {
                                if (this.mItemsCanFocus && hasFocus() && (sel.hasFocus() ^ 1) != 0) {
                                    boolean focusWasTaken;
                                    if (sel == focusLayoutRestoreDirectChild && focusLayoutRestoreView != null && focusLayoutRestoreView.requestFocus()) {
                                        focusWasTaken = true;
                                    } else {
                                        focusWasTaken = sel.requestFocus();
                                    }
                                    if (focusWasTaken) {
                                        sel.setSelected(false);
                                        this.mSelectorRect.setEmpty();
                                    } else {
                                        View focused = getFocusedChild();
                                        if (focused != null) {
                                            focused.clearFocus();
                                        }
                                        positionSelector(-1, sel);
                                    }
                                } else {
                                    positionSelector(-1, sel);
                                }
                                this.mSelectedTop = sel.getTop();
                            } else {
                                boolean inTouchMode = this.mTouchMode != 1 ? this.mTouchMode == 2 : true;
                                View child;
                                if (inTouchMode) {
                                    child = getChildAt(this.mMotionPosition - this.mFirstPosition);
                                    if (child != null) {
                                        positionSelector(this.mMotionPosition, child);
                                    }
                                } else if (this.mSelectorPosition != -1) {
                                    child = getChildAt(this.mSelectorPosition - this.mFirstPosition);
                                    if (child != null) {
                                        positionSelector(this.mSelectorPosition, child);
                                    }
                                } else {
                                    this.mSelectedTop = 0;
                                    this.mSelectorRect.setEmpty();
                                }
                                if (hasFocus() && focusLayoutRestoreView != null) {
                                    focusLayoutRestoreView.requestFocus();
                                }
                            }
                            if (viewRootImpl != null && viewRootImpl.getAccessibilityFocusedHost() == null) {
                                if (accessibilityFocusLayoutRestoreView != null && accessibilityFocusLayoutRestoreView.isAttachedToWindow()) {
                                    AccessibilityNodeProvider provider = accessibilityFocusLayoutRestoreView.getAccessibilityNodeProvider();
                                    if (accessibilityFocusLayoutRestoreNode == null || provider == null) {
                                        accessibilityFocusLayoutRestoreView.requestAccessibilityFocus();
                                    } else {
                                        provider.performAction(AccessibilityNodeInfo.getVirtualDescendantId(accessibilityFocusLayoutRestoreNode.getSourceNodeId()), 64, null);
                                    }
                                } else if (accessibilityFocusPosition != -1) {
                                    View restoreView = getChildAt(MathUtils.constrain(accessibilityFocusPosition - this.mFirstPosition, 0, getChildCount() - 1));
                                    if (restoreView != null) {
                                        restoreView.requestAccessibilityFocus();
                                    }
                                }
                            }
                            if (!(focusLayoutRestoreView == null || focusLayoutRestoreView.getWindowToken() == null)) {
                                focusLayoutRestoreView.dispatchFinishTemporaryDetach();
                            }
                            this.mLayoutMode = 0;
                            this.mDataChanged = false;
                            if (this.mPositionScrollAfterLayout != null) {
                                post(this.mPositionScrollAfterLayout);
                                this.mPositionScrollAfterLayout = null;
                            }
                            this.mNeedSync = false;
                            setNextSelectedPositionInt(this.mSelectedPosition);
                            updateScrollIndicators();
                            if (this.mItemCount > 0) {
                                checkSelectionChanged();
                            }
                            invokeOnItemScrollListener();
                            if (this.mFocusSelector != null) {
                                this.mFocusSelector.onLayoutComplete();
                            }
                            if (!blockLayoutRequests) {
                                this.mBlockLayoutRequests = false;
                            }
                            return;
                        }
                    default:
                        index = this.mSelectedPosition - this.mFirstPosition;
                        if (index >= 0 && index < childCount) {
                            oldSel = getChildAt(index);
                        }
                        oldFirst = getChildAt(0);
                        if (this.mNextSelectedPosition >= 0) {
                            delta = this.mNextSelectedPosition - this.mSelectedPosition;
                        }
                        newSel = getChildAt(index + delta);
                }
                dataChanged = this.mDataChanged;
                if (dataChanged) {
                }
                if (this.mItemCount != 0) {
                }
            } catch (Throwable th) {
                if (this.mFocusSelector != null) {
                    this.mFocusSelector.onLayoutComplete();
                }
                if (!blockLayoutRequests) {
                    this.mBlockLayoutRequests = false;
                }
            }
        }
    }

    boolean trackMotionScroll(int deltaY, int incrementalDeltaY) {
        boolean result = super.trackMotionScroll(deltaY, incrementalDeltaY);
        removeUnusedFixedViews(this.mHeaderViewInfos);
        removeUnusedFixedViews(this.mFooterViewInfos);
        return result;
    }

    private void removeUnusedFixedViews(List<FixedViewInfo> infoList) {
        if (infoList != null) {
            for (int i = infoList.size() - 1; i >= 0; i--) {
                View view = ((FixedViewInfo) infoList.get(i)).view;
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                if (view.getParent() == null && lp != null && lp.recycledHeaderFooter) {
                    -wrap6(view, false);
                    lp.recycledHeaderFooter = false;
                }
            }
        }
    }

    private boolean isDirectChildHeaderOrFooter(View child) {
        int i;
        ArrayList<FixedViewInfo> headers = this.mHeaderViewInfos;
        int numHeaders = headers.size();
        for (i = 0; i < numHeaders; i++) {
            if (child == ((FixedViewInfo) headers.get(i)).view) {
                return true;
            }
        }
        ArrayList<FixedViewInfo> footers = this.mFooterViewInfos;
        int numFooters = footers.size();
        for (i = 0; i < numFooters; i++) {
            if (child == ((FixedViewInfo) footers.get(i)).view) {
                return true;
            }
        }
        return false;
    }

    private View makeAndAddView(int position, int y, boolean flow, int childrenLeft, boolean selected) {
        if (!this.mDataChanged) {
            View activeView = this.mRecycler.getActiveView(position);
            if (activeView != null) {
                setupChild(activeView, position, y, flow, childrenLeft, selected, true);
                return activeView;
            }
        }
        if ((position < 0 || position >= this.mItemCount) && isDoingDeleteAnimation()) {
            return null;
        }
        UniPerf.getInstance().uniPerfEvent(4116, LogException.NO_VALUE, new int[]{0});
        this.mAddItemViewType = this.mAdapter.getItemViewType(position);
        this.mAddItemViewPosition = position;
        View child = obtainView(position, this.mIsScrap);
        UniPerf.getInstance().uniPerfEvent(4116, LogException.NO_VALUE, new int[]{-1});
        setupChild(child, position, y, flow, childrenLeft, selected, this.mIsScrap[0]);
        this.mAddItemViewType = -10000;
        this.mAddItemViewPosition = -1;
        return child;
    }

    private void setupChild(View child, int position, int y, boolean flowDown, int childrenLeft, boolean selected, boolean isAttachedToWindow) {
        boolean needToMeasure;
        Trace.traceBegin(8, "setupListItem");
        boolean isSelected = selected ? shouldShowSelector() : false;
        boolean updateChildSelected = isSelected != child.isSelected();
        int mode = this.mTouchMode;
        boolean isPressed = (mode <= 0 || mode >= 3) ? false : this.mMotionPosition == position;
        boolean updateChildPressed = isPressed != child.isPressed();
        if (!isAttachedToWindow || updateChildSelected) {
            needToMeasure = true;
        } else {
            needToMeasure = child.isLayoutRequested();
        }
        LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) generateDefaultLayoutParams();
        }
        p.viewType = getHwItemViewType(position);
        p.isEnabled = this.mAdapter.isEnabled(position);
        if (updateChildSelected) {
            child.setSelected(isSelected);
        }
        if (updateChildPressed) {
            child.setPressed(isPressed);
        }
        if (!(this.mChoiceMode == 0 || this.mCheckStates == null)) {
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(this.mCheckStates.get(position));
            } else if (getContext().getApplicationInfo().targetSdkVersion >= 11) {
                child.setActivated(this.mCheckStates.get(position));
            }
        }
        if ((!isAttachedToWindow || (p.forceAdd ^ 1) == 0) && !(p.recycledHeaderFooter && p.viewType == -2)) {
            p.forceAdd = false;
            if (p.viewType == -2) {
                p.recycledHeaderFooter = true;
            }
            addViewInLayout(child, flowDown ? -1 : 0, p, true);
            child.resolveRtlPropertiesIfNeeded();
        } else {
            attachViewToParent(child, flowDown ? -1 : 0, p);
            if (isAttachedToWindow && ((LayoutParams) child.getLayoutParams()).scrappedFromPosition != position) {
                child.jumpDrawablesToCurrentState();
            }
        }
        if (needToMeasure) {
            int childHeightSpec;
            int childWidthSpec = ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
            int lpHeight = p.height;
            if (lpHeight > 0) {
                childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
            } else {
                childHeightSpec = MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), 0);
            }
            child.measure(childWidthSpec, childHeightSpec);
        } else {
            cleanupLayoutState(child);
        }
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childTop = flowDown ? y : y - h;
        if (needToMeasure) {
            child.layout(childrenLeft, childTop, childrenLeft + w, childTop + h);
        } else {
            child.offsetLeftAndRight(childrenLeft - child.getLeft());
            child.offsetTopAndBottom(childTop - child.getTop());
        }
        if (this.mCachingStarted && (child.isDrawingCacheEnabled() ^ 1) != 0) {
            child.setDrawingCacheEnabled(true);
        }
        Trace.traceEnd(8);
    }

    protected boolean canAnimate() {
        return super.canAnimate() && this.mItemCount > 0;
    }

    public void setSelection(int position) {
        setSelectionFromTop(position, 0);
    }

    void setSelectionInt(int position) {
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

    int lookForSelectablePosition(int position, boolean lookDown) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null || isInTouchMode()) {
            return -1;
        }
        int count = adapter.getCount();
        if (!this.mAreAllItemsSelectable) {
            if (lookDown) {
                position = Math.max(0, position);
                while (position < count && (adapter.isEnabled(position) ^ 1) != 0) {
                    position++;
                }
            } else {
                position = Math.min(position, count - 1);
                while (position >= 0 && (adapter.isEnabled(position) ^ 1) != 0) {
                    position--;
                }
            }
        }
        if (position < 0 || position >= count) {
            return -1;
        }
        return position;
    }

    int lookForSelectablePositionAfter(int current, int position, boolean lookDown) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null || isInTouchMode()) {
            return -1;
        }
        int after = lookForSelectablePosition(position, lookDown);
        if (after != -1) {
            return after;
        }
        int count = adapter.getCount();
        current = MathUtils.constrain(current, -1, count - 1);
        if (lookDown) {
            position = Math.min(position - 1, count - 1);
            while (position > current && (adapter.isEnabled(position) ^ 1) != 0) {
                position--;
            }
            if (position <= current) {
                return -1;
            }
        }
        position = Math.max(0, position + 1);
        while (position < current && (adapter.isEnabled(position) ^ 1) != 0) {
            position++;
        }
        if (position >= current) {
            return -1;
        }
        return position;
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
        boolean handled = super.-wrap7(event);
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

    private boolean commonKey(int keyCode, int count, KeyEvent event) {
        if (this.mAdapter == null || (isAttachedToWindow() ^ 1) != 0) {
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
        if (!(handled || action == 1)) {
            int count2;
            switch (keyCode) {
                case 19:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            if (!resurrectSelectionIfNeeded()) {
                                handled = fullScroll(33);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    }
                    handled = resurrectSelectionIfNeeded();
                    if (!handled) {
                        while (true) {
                            count2 = count;
                            count = count2 - 1;
                            if (count2 > 0 && arrowScroll(33)) {
                                handled = true;
                            }
                        }
                    }
                    break;
                case 20:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            if (!resurrectSelectionIfNeeded()) {
                                handled = fullScroll(130);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    }
                    handled = resurrectSelectionIfNeeded();
                    if (!handled) {
                        while (true) {
                            count2 = count;
                            count = count2 - 1;
                            if (count2 > 0 && arrowScroll(130)) {
                                handled = true;
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
                            if (!resurrectSelectionIfNeeded()) {
                                handled = arrowScroll(33);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = arrowScroll(130);
                        break;
                    } else {
                        handled = true;
                        break;
                    }
                    break;
                case 92:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            if (!resurrectSelectionIfNeeded()) {
                                handled = fullScroll(33);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = pageScroll(33);
                        break;
                    } else {
                        handled = true;
                        break;
                    }
                    break;
                case 93:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            if (!resurrectSelectionIfNeeded()) {
                                handled = fullScroll(130);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = pageScroll(130);
                        break;
                    } else {
                        handled = true;
                        break;
                    }
                    break;
                case 122:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            handled = fullScroll(33);
                            break;
                        }
                        handled = true;
                        break;
                    }
                    break;
                case 123:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            handled = fullScroll(130);
                            break;
                        }
                        handled = true;
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

    boolean pageScroll(int direction) {
        int nextPage;
        boolean down;
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

    boolean fullScroll(int direction) {
        boolean moved = false;
        int position;
        if (direction == 33) {
            if (this.mSelectedPosition != 0) {
                position = lookForSelectablePositionAfter(this.mSelectedPosition, 0, true);
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
                position = lookForSelectablePositionAfter(this.mSelectedPosition, lastItem, false);
                if (position >= 0) {
                    this.mLayoutMode = 3;
                    setSelectionInt(position);
                    invokeOnItemScrollListener();
                }
                moved = true;
            }
        }
        if (moved && (awakenScrollBars() ^ 1) != 0) {
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

    boolean arrowScroll(int direction) {
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
        }
    }

    private final int nextSelectedPositionForDirection(View selectedView, int selectedPos, int direction) {
        int nextSelected;
        boolean z = false;
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
                nextSelected = lastPos;
            } else {
                nextSelected = selectedPos - 1;
            }
        }
        if (nextSelected < 0 || nextSelected >= this.mAdapter.getCount()) {
            return -1;
        }
        if (direction == 130) {
            z = true;
        }
        return lookForSelectablePosition(nextSelected, z);
    }

    private boolean arrowScrollImpl(int direction) {
        if (getChildCount() <= 0) {
            return false;
        }
        View focused;
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
            boolean z;
            if (focusResult != null) {
                z = true;
            } else {
                z = false;
            }
            handleNewSelectionChange(selectedView, direction, nextSelectedPosition, z);
            setSelectedPositionInt(nextSelectedPosition);
            setNextSelectedPositionInt(nextSelectedPosition);
            selectedView = getSelectedView();
            selectedPos = nextSelectedPosition;
            if (this.mItemsCanFocus && focusResult == null) {
                focused = getFocusedChild();
                if (focused != null) {
                    focused.clearFocus();
                }
            }
            needToRedraw = true;
            checkSelectionChanged();
        }
        if (amountToScroll > 0) {
            if (direction != 33) {
                amountToScroll = -amountToScroll;
            }
            scrollListItemsBy(amountToScroll);
            needToRedraw = true;
        }
        if (this.mItemsCanFocus && focusResult == null && selectedView != null && selectedView.hasFocus()) {
            focused = selectedView.findFocus();
            if (focused != null && (!isViewAncestorOf(focused, this) || distanceToView(focused) > 0)) {
                focused.clearFocus();
            }
        }
        if (!(nextSelectedPosition != -1 || selectedView == null || (isViewAncestorOf(selectedView, this) ^ 1) == 0)) {
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
        boolean z = false;
        if (newSelectedPosition == -1) {
            throw new IllegalArgumentException("newSelectedPosition needs to be valid");
        }
        int topViewIndex;
        int bottomViewIndex;
        View topView;
        View bottomView;
        boolean topSelected = false;
        int selectedIndex = this.mSelectedPosition - this.mFirstPosition;
        int nextSelectedIndex = newSelectedPosition - this.mFirstPosition;
        if (direction == 33) {
            topViewIndex = nextSelectedIndex;
            bottomViewIndex = selectedIndex;
            topView = getChildAt(nextSelectedIndex);
            bottomView = selectedView;
            topSelected = true;
        } else {
            topViewIndex = selectedIndex;
            bottomViewIndex = nextSelectedIndex;
            topView = selectedView;
            bottomView = getChildAt(nextSelectedIndex);
        }
        int numChildren = getChildCount();
        if (topView != null) {
            boolean z2;
            if (newFocusAssigned) {
                z2 = false;
            } else {
                z2 = topSelected;
            }
            topView.setSelected(z2);
            measureAndAdjustDown(topView, topViewIndex, numChildren);
        }
        if (bottomView != null) {
            if (!newFocusAssigned) {
                z = topSelected ^ 1;
            }
            bottomView.setSelected(z);
            measureAndAdjustDown(bottomView, bottomViewIndex, numChildren);
        }
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
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
        } else {
            childHeightSpec = MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), 0);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private void relayoutMeasuredItem(View child) {
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childLeft = this.mListPadding.left;
        int childRight = childLeft + w;
        int childTop = child.getTop();
        child.layout(childLeft, childTop, childRight, childTop + h);
    }

    private int getArrowScrollPreviewLength() {
        return Math.max(2, getVerticalFadingEdgeLength());
    }

    private int amountToScroll(int direction, int nextSelectedPosition) {
        int listBottom = getHeight() - this.mListPadding.bottom;
        int listTop = this.mListPadding.top;
        int numChildren = getChildCount();
        int indexToMakeVisible;
        int positionToMakeVisible;
        View viewToMakeVisible;
        int amountToScroll;
        if (direction == 130) {
            indexToMakeVisible = numChildren - 1;
            if (nextSelectedPosition != -1) {
                indexToMakeVisible = nextSelectedPosition - this.mFirstPosition;
            }
            while (numChildren <= indexToMakeVisible) {
                addViewBelow(getChildAt(numChildren - 1), (this.mFirstPosition + numChildren) - 1);
                numChildren++;
            }
            positionToMakeVisible = this.mFirstPosition + indexToMakeVisible;
            viewToMakeVisible = getChildAt(indexToMakeVisible);
            int goalBottom = listBottom;
            if (positionToMakeVisible < this.mItemCount - 1) {
                goalBottom = listBottom - getArrowScrollPreviewLength();
            }
            if (viewToMakeVisible.getBottom() <= goalBottom) {
                return 0;
            }
            if (nextSelectedPosition != -1 && goalBottom - viewToMakeVisible.getTop() >= getMaxScrollAmount()) {
                return 0;
            }
            amountToScroll = viewToMakeVisible.getBottom() - goalBottom;
            if (this.mFirstPosition + numChildren == this.mItemCount) {
                amountToScroll = Math.min(amountToScroll, getChildAt(numChildren - 1).getBottom() - listBottom);
            }
            return Math.min(amountToScroll, getMaxScrollAmount());
        }
        indexToMakeVisible = 0;
        if (nextSelectedPosition != -1) {
            indexToMakeVisible = nextSelectedPosition - this.mFirstPosition;
        }
        while (indexToMakeVisible < 0) {
            addViewAbove(getChildAt(0), this.mFirstPosition);
            this.mFirstPosition--;
            indexToMakeVisible = nextSelectedPosition - this.mFirstPosition;
        }
        positionToMakeVisible = this.mFirstPosition + indexToMakeVisible;
        viewToMakeVisible = getChildAt(indexToMakeVisible);
        int goalTop = listTop;
        if (positionToMakeVisible > 0) {
            goalTop = listTop + getArrowScrollPreviewLength();
        }
        if (viewToMakeVisible.getTop() >= goalTop) {
            return 0;
        }
        if (nextSelectedPosition != -1 && viewToMakeVisible.getBottom() - goalTop >= getMaxScrollAmount()) {
            return 0;
        }
        amountToScroll = goalTop - viewToMakeVisible.getTop();
        if (this.mFirstPosition == 0) {
            amountToScroll = Math.min(amountToScroll, listTop - getChildAt(0).getTop());
        }
        return Math.min(amountToScroll, getMaxScrollAmount());
    }

    private int lookForSelectablePositionOnScreen(int direction) {
        int firstPosition = this.mFirstPosition;
        int startPos;
        ListAdapter adapter;
        int pos;
        if (direction == 130) {
            if (this.mSelectedPosition != -1) {
                startPos = this.mSelectedPosition + 1;
            } else {
                startPos = firstPosition;
            }
            if (startPos >= this.mAdapter.getCount()) {
                return -1;
            }
            if (startPos < firstPosition) {
                startPos = firstPosition;
            }
            int lastVisiblePos = getLastVisiblePosition();
            adapter = getAdapter();
            pos = startPos;
            while (pos <= lastVisiblePos) {
                if (adapter.isEnabled(pos) && getChildAt(pos - firstPosition).getVisibility() == 0) {
                    return pos;
                }
                pos++;
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
            adapter = getAdapter();
            pos = startPos;
            while (pos >= firstPosition) {
                if (adapter.isEnabled(pos) && getChildAt(pos - firstPosition).getVisibility() == 0) {
                    return pos;
                }
                pos--;
            }
        }
        return -1;
    }

    private ArrowScrollFocusResult arrowScrollFocused(int direction) {
        View newFocus;
        View selectedView = getSelectedView();
        if (selectedView == null || !selectedView.hasFocus()) {
            int ySearchPoint;
            if (direction == 130) {
                int listTop = this.mListPadding.top + (this.mFirstPosition > 0 ? getArrowScrollPreviewLength() : 0);
                if (selectedView == null || selectedView.getTop() <= listTop) {
                    ySearchPoint = listTop;
                } else {
                    ySearchPoint = selectedView.getTop();
                }
                this.mTempRect.set(0, ySearchPoint, 0, ySearchPoint);
            } else {
                int listBottom = (getHeight() - this.mListPadding.bottom) - ((this.mFirstPosition + getChildCount()) + -1 < this.mItemCount ? getArrowScrollPreviewLength() : 0);
                if (selectedView == null || selectedView.getBottom() >= listBottom) {
                    ySearchPoint = listBottom;
                } else {
                    ySearchPoint = selectedView.getBottom();
                }
                this.mTempRect.set(0, ySearchPoint, 0, ySearchPoint);
            }
            newFocus = FocusFinder.getInstance().findNextFocusFromRect(this, this.mTempRect, direction);
        } else {
            newFocus = FocusFinder.getInstance().findNextFocus(this, selectedView.findFocus(), direction);
        }
        if (newFocus != null) {
            int positionOfNewFocus = positionOfNewFocus(newFocus);
            if (!(this.mSelectedPosition == -1 || positionOfNewFocus == this.mSelectedPosition)) {
                int selectablePosition = lookForSelectablePositionOnScreen(direction);
                if (selectablePosition != -1 && ((direction == 130 && selectablePosition < positionOfNewFocus) || (direction == 33 && selectablePosition > positionOfNewFocus))) {
                    return null;
                }
            }
            int focusScroll = amountToScrollToNewFocus(direction, newFocus, positionOfNewFocus);
            int maxScrollAmount = getMaxScrollAmount();
            if (focusScroll < maxScrollAmount) {
                newFocus.requestFocus(direction);
                this.mArrowScrollFocusResult.populate(positionOfNewFocus, focusScroll);
                return this.mArrowScrollFocusResult;
            } else if (distanceToView(newFocus) < maxScrollAmount) {
                newFocus.requestFocus(direction);
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
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        return theParent instanceof ViewGroup ? isViewAncestorOf((View) theParent, parent) : false;
    }

    private int amountToScrollToNewFocus(int direction, View newFocus, int positionOfNewFocus) {
        newFocus.getDrawingRect(this.mTempRect);
        offsetDescendantRectToMyCoords(newFocus, this.mTempRect);
        int amountToScroll;
        if (direction != 33) {
            int listBottom = getHeight() - this.mListPadding.bottom;
            if (this.mTempRect.bottom <= listBottom) {
                return 0;
            }
            amountToScroll = this.mTempRect.bottom - listBottom;
            if (positionOfNewFocus < this.mItemCount - 1) {
                return amountToScroll + getArrowScrollPreviewLength();
            }
            return amountToScroll;
        } else if (this.mTempRect.top >= this.mListPadding.top) {
            return 0;
        } else {
            amountToScroll = this.mListPadding.top - this.mTempRect.top;
            return positionOfNewFocus > 0 ? amountToScroll + getArrowScrollPreviewLength() : amountToScroll;
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
        RecycleBin recycleBin = this.mRecycler;
        View last;
        View first;
        if (amount < 0) {
            int numChildren = getChildCount();
            last = getChildAt(numChildren - 1);
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
            first = getChildAt(0);
            while (first != null && first.getBottom() < listTop) {
                if (recycleBin.shouldRecycleViewType(((LayoutParams) first.getLayoutParams()).viewType)) {
                    recycleBin.addScrapView(first, this.mFirstPosition);
                }
                detachViewFromParent(first);
                first = getChildAt(0);
                this.mFirstPosition++;
            }
        } else {
            first = getChildAt(0);
            while (first.getTop() > listTop && this.mFirstPosition > 0) {
                first = addViewAbove(first, this.mFirstPosition);
                this.mFirstPosition--;
            }
            if (first.getTop() > listTop) {
                offsetChildrenTopAndBottom(listTop - first.getTop());
            }
            int lastIndex = getChildCount() - 1;
            last = getChildAt(lastIndex);
            while (last != null && last.getTop() > listBottom) {
                if (recycleBin.shouldRecycleViewType(((LayoutParams) last.getLayoutParams()).viewType)) {
                    recycleBin.addScrapView(last, this.mFirstPosition + lastIndex);
                }
                detachViewFromParent(last);
                lastIndex--;
                last = getChildAt(lastIndex);
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
            setDescendantFocusability(393216);
        }
    }

    public boolean getItemsCanFocus() {
        return this.mItemsCanFocus;
    }

    public boolean isOpaque() {
        boolean retValue;
        if (this.mCachingActive && this.mIsCacheColorOpaque && this.mDividerIsOpaque && hasOpaqueScrollbars()) {
            retValue = true;
        } else {
            retValue = super.isOpaque();
        }
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

    void drawOverscrollHeader(Canvas canvas, Drawable drawable, Rect bounds) {
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

    void drawOverscrollFooter(Canvas canvas, Drawable drawable, Rect bounds) {
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

    protected void dispatchDraw(Canvas canvas) {
        if (this.mCachingStarted) {
            this.mCachingActive = true;
        }
        int dividerHeight = this.mDividerHeight;
        Drawable overscrollHeader = this.mOverScrollHeader;
        Drawable overscrollFooter = this.mOverScrollFooter;
        boolean drawOverscrollHeader = overscrollHeader != null;
        boolean drawOverscrollFooter = overscrollFooter != null;
        boolean drawDividers = dividerHeight > 0 && this.mDivider != null;
        if (drawDividers || drawOverscrollHeader || drawOverscrollFooter) {
            Rect bounds = this.mTempRect;
            bounds.left = this.mPaddingLeft;
            bounds.right = (this.mRight - this.mLeft) - this.mPaddingRight;
            int count = getChildCount();
            int headerCount = getHeaderViewsCount();
            int itemCount = this.mItemCount;
            int footerLimit = itemCount - this.mFooterViewInfos.size();
            boolean headerDividers = this.mHeaderDividersEnabled;
            boolean footerDividers = this.mFooterDividersEnabled;
            int first = this.mFirstPosition;
            boolean areAllItemsSelectable = this.mAreAllItemsSelectable;
            ListAdapter adapter = this.mAdapter;
            int fillForMissingDividers = isOpaque() ? super.isOpaque() ^ 1 : 0;
            if (fillForMissingDividers != 0 && this.mDividerPaint == null && this.mIsCacheColorOpaque) {
                this.mDividerPaint = new Paint();
                this.mDividerPaint.setColor(getCacheColorHint());
            }
            Paint paint = this.mDividerPaint;
            int effectivePaddingTop = 0;
            int effectivePaddingBottom = 0;
            if ((this.mGroupFlags & 34) == 34) {
                effectivePaddingTop = this.mListPadding.top;
                effectivePaddingBottom = this.mListPadding.bottom;
            }
            int listBottom = ((this.mBottom - this.mTop) - effectivePaddingBottom) + this.mScrollY;
            int scrollY;
            int i;
            int itemIndex;
            boolean isHeader;
            boolean isFooter;
            if (this.mStackFromBottom) {
                scrollY = this.mScrollY;
                if (count > 0 && drawOverscrollHeader) {
                    bounds.top = scrollY;
                    bounds.bottom = getChildAt(0).getTop();
                    drawOverscrollHeader(canvas, overscrollHeader, bounds);
                }
                int start = drawOverscrollHeader ? 1 : 0;
                i = start;
                while (i < count) {
                    itemIndex = first + i;
                    isHeader = itemIndex < headerCount;
                    isFooter = itemIndex >= footerLimit;
                    if ((headerDividers || (isHeader ^ 1) != 0) && (footerDividers || (isFooter ^ 1) != 0)) {
                        int top = getChildAt(i).getTop();
                        if (drawDividers && top > effectivePaddingTop) {
                            boolean isFirstItem = i == start;
                            int previousIndex = itemIndex - 1;
                            if (checkIsEnabled(adapter, itemIndex) && ((headerDividers || (!isHeader && previousIndex >= headerCount)) && (isFirstItem || (checkIsEnabled(adapter, previousIndex) && (footerDividers || (!isFooter && previousIndex < footerLimit)))))) {
                                bounds.top = top - dividerHeight;
                                bounds.bottom = top;
                                drawDivider(canvas, bounds, i - 1);
                            } else if (fillForMissingDividers != 0) {
                                bounds.top = top - dividerHeight;
                                bounds.bottom = top;
                                canvas.drawRect(bounds, paint);
                            }
                        }
                    }
                    i++;
                }
                if (count > 0 && scrollY > 0) {
                    if (drawOverscrollFooter) {
                        int absListBottom = this.mBottom;
                        bounds.top = absListBottom;
                        bounds.bottom = absListBottom + scrollY;
                        drawOverscrollFooter(canvas, overscrollFooter, bounds);
                    } else if (drawDividers) {
                        bounds.top = listBottom;
                        bounds.bottom = listBottom + dividerHeight;
                        drawDivider(canvas, bounds, -1);
                    }
                }
            } else {
                int bottom = 0;
                scrollY = this.mScrollY;
                if (count > 0 && scrollY < 0) {
                    if (drawOverscrollHeader) {
                        bounds.bottom = 0;
                        bounds.top = scrollY;
                        drawOverscrollHeader(canvas, overscrollHeader, bounds);
                    } else if (drawDividers) {
                        bounds.bottom = 0;
                        bounds.top = -dividerHeight;
                        drawDivider(canvas, bounds, -1);
                    }
                }
                i = 0;
                while (i < count) {
                    itemIndex = first + i;
                    isHeader = itemIndex < headerCount;
                    isFooter = itemIndex >= footerLimit;
                    if ((headerDividers || (isHeader ^ 1) != 0) && (footerDividers || (isFooter ^ 1) != 0)) {
                        bottom = getChildAt(i).getBottom();
                        boolean isLastItem = i == count + -1;
                        if (drawDividers && bottom < listBottom) {
                            if (((drawOverscrollFooter ? isLastItem : 0) ^ 1) != 0) {
                                int nextIndex = itemIndex + 1;
                                if (checkIsEnabled(adapter, itemIndex) && ((headerDividers || (!isHeader && nextIndex >= headerCount)) && (isLastItem || (checkIsEnabled(adapter, nextIndex) && (footerDividers || (!isFooter && nextIndex < footerLimit)))))) {
                                    bounds.top = bottom;
                                    bounds.bottom = bottom + dividerHeight;
                                    drawDivider(canvas, bounds, i);
                                } else if (fillForMissingDividers != 0) {
                                    bounds.top = bottom;
                                    bounds.bottom = bottom + dividerHeight;
                                    canvas.drawRect(bounds, paint);
                                }
                            }
                        }
                    }
                    i++;
                }
                int overFooterBottom = this.mBottom + this.mScrollY;
                if (drawOverscrollFooter && first + count == itemCount && overFooterBottom > bottom) {
                    bounds.top = bottom;
                    bounds.bottom = overFooterBottom;
                    drawOverscrollFooter(canvas, overscrollFooter, bounds);
                }
            }
        }
        super.dispatchDraw(canvas);
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean more = super.drawChild(canvas, child, drawingTime);
        if (this.mCachingActive && child.mCachingFailed) {
            this.mCachingActive = false;
        }
        return more;
    }

    void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        Drawable divider = this.mDivider;
        divider.setBounds(bounds);
        divider.draw(canvas);
    }

    public Drawable getDivider() {
        return this.mDivider;
    }

    public void setDivider(Drawable divider) {
        boolean z = true;
        if (divider != null) {
            this.mDividerHeight = divider.getIntrinsicHeight();
        } else {
            this.mDividerHeight = 0;
        }
        this.mDivider = divider;
        if (!(divider == null || divider.getOpacity() == -1)) {
            z = false;
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

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
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
                    int distance = AbsListView.getDistance(previouslyFocusedRect, otherRect, direction);
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

    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                addHeaderView(getChildAt(i));
            }
            removeAllViews();
        }
    }

    protected <T extends View> T findViewTraversal(int id) {
        View v = super.findViewTraversal(id);
        if (v == null) {
            v = findViewInHeadersOrFooters(this.mHeaderViewInfos, id);
            if (v != null) {
                return v;
            }
            v = findViewInHeadersOrFooters(this.mFooterViewInfos, id);
            if (v != null) {
                return v;
            }
        }
        return v;
    }

    View findViewInHeadersOrFooters(ArrayList<FixedViewInfo> where, int id) {
        if (where != null) {
            int len = where.size();
            for (int i = 0; i < len; i++) {
                View v = ((FixedViewInfo) where.get(i)).view;
                if (!v.isRootNamespace()) {
                    v = v.findViewById(id);
                    if (v != null) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    protected <T extends View> T findViewWithTagTraversal(Object tag) {
        View v = super.findViewWithTagTraversal(tag);
        if (v == null) {
            v = findViewWithTagInHeadersOrFooters(this.mHeaderViewInfos, tag);
            if (v != null) {
                return v;
            }
            v = findViewWithTagInHeadersOrFooters(this.mFooterViewInfos, tag);
            if (v != null) {
                return v;
            }
        }
        return v;
    }

    View findViewWithTagInHeadersOrFooters(ArrayList<FixedViewInfo> where, Object tag) {
        if (where != null) {
            int len = where.size();
            for (int i = 0; i < len; i++) {
                View v = ((FixedViewInfo) where.get(i)).view;
                if (!v.isRootNamespace()) {
                    v = v.findViewWithTag(tag);
                    if (v != null) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    protected <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        View v = super.findViewByPredicateTraversal(predicate, childToSkip);
        if (v == null) {
            v = findViewByPredicateInHeadersOrFooters(this.mHeaderViewInfos, predicate, childToSkip);
            if (v != null) {
                return v;
            }
            v = findViewByPredicateInHeadersOrFooters(this.mFooterViewInfos, predicate, childToSkip);
            if (v != null) {
                return v;
            }
        }
        return v;
    }

    View findViewByPredicateInHeadersOrFooters(ArrayList<FixedViewInfo> where, Predicate<View> predicate, View childToSkip) {
        if (where != null) {
            int len = where.size();
            for (int i = 0; i < len; i++) {
                View v = ((FixedViewInfo) where.get(i)).view;
                if (!(v == childToSkip || (v.isRootNamespace() ^ 1) == 0)) {
                    v = v.findViewByPredicate(predicate);
                    if (v != null) {
                        return v;
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
        int i = 0;
        int checkedCount = 0;
        while (i < count) {
            int checkedCount2;
            if (states.valueAt(i)) {
                checkedCount2 = checkedCount + 1;
                ids[checkedCount] = adapter.getItemId(states.keyAt(i));
            } else {
                checkedCount2 = checkedCount;
            }
            i++;
            checkedCount = checkedCount2;
        }
        if (checkedCount == count) {
            return ids;
        }
        long[] result = new long[checkedCount];
        System.arraycopy(ids, 0, result, 0, checkedCount);
        return result;
    }

    int getHeightForPosition(int position) {
        int height = super.getHeightForPosition(position);
        if (shouldAdjustHeightForDivider(position)) {
            return this.mDividerHeight + height;
        }
        return height;
    }

    private boolean shouldAdjustHeightForDivider(int itemIndex) {
        int dividerHeight = this.mDividerHeight;
        Drawable overscrollHeader = this.mOverScrollHeader;
        Drawable overscrollFooter = this.mOverScrollFooter;
        boolean drawOverscrollHeader = overscrollHeader != null;
        boolean drawOverscrollFooter = overscrollFooter != null;
        boolean drawDividers = dividerHeight > 0 && this.mDivider != null;
        if (drawDividers) {
            int fillForMissingDividers = isOpaque() ? super.isOpaque() ^ 1 : 0;
            int itemCount = this.mItemCount;
            int headerCount = getHeaderViewsCount();
            int footerLimit = itemCount - this.mFooterViewInfos.size();
            boolean isHeader = itemIndex < headerCount;
            boolean isFooter = itemIndex >= footerLimit;
            boolean headerDividers = this.mHeaderDividersEnabled;
            boolean footerDividers = this.mFooterDividersEnabled;
            if ((headerDividers || (isHeader ^ 1) != 0) && (footerDividers || (isFooter ^ 1) != 0)) {
                ListAdapter adapter = this.mAdapter;
                if (this.mStackFromBottom) {
                    boolean isFirstItem = itemIndex == (drawOverscrollHeader ? 1 : 0);
                    if (!isFirstItem) {
                        int previousIndex = itemIndex - 1;
                        if (checkIsEnabled(adapter, itemIndex) && ((headerDividers || (!isHeader && previousIndex >= headerCount)) && (isFirstItem || (checkIsEnabled(adapter, previousIndex) && (footerDividers || (!isFooter && previousIndex < footerLimit)))))) {
                            return true;
                        }
                        if (fillForMissingDividers != 0) {
                            return true;
                        }
                    }
                }
                boolean isLastItem = itemIndex == itemCount + -1;
                if (!(drawOverscrollFooter && (isLastItem ^ 1) == 0)) {
                    int nextIndex = itemIndex + 1;
                    if (checkIsEnabled(adapter, itemIndex) && ((headerDividers || (!isHeader && nextIndex >= headerCount)) && (isLastItem || (checkIsEnabled(adapter, nextIndex) && (footerDividers || (!isFooter && nextIndex < footerLimit)))))) {
                        return true;
                    }
                    if (fillForMissingDividers != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public CharSequence getAccessibilityClassName() {
        return ListView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        int rowsCount = getCount();
        info.setCollectionInfo(CollectionInfo.obtain(rowsCount, 1, false, getSelectionModeForAccessibility()));
        if (rowsCount > 0) {
            info.addAction(AccessibilityAction.ACTION_SCROLL_TO_POSITION);
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        switch (action) {
            case R.id.accessibilityActionScrollToPosition /*16908343*/:
                int row = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, -1);
                int position = Math.min(row, getCount() - 1);
                if (row >= 0) {
                    smoothScrollToPosition(position);
                    return true;
                }
                break;
        }
        return false;
    }

    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoForItem(view, position, info);
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        boolean isHeading = lp != null && lp.viewType == -2;
        info.setCollectionItemInfo(CollectionItemInfo.obtain(position, 1, 0, 1, isHeading, isItemChecked(position)));
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("recycleOnMeasure", recycleOnMeasure());
    }

    protected HeaderViewListAdapter wrapHeaderListAdapterInternal(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footerViewInfos, ListAdapter adapter) {
        return new HeaderViewListAdapter(headerViewInfos, footerViewInfos, adapter);
    }

    protected void wrapHeaderListAdapterInternal() {
        this.mAdapter = wrapHeaderListAdapterInternal(this.mHeaderViewInfos, this.mFooterViewInfos, this.mAdapter);
    }

    protected void dispatchDataSetObserverOnChangedInternal() {
        if (this.mDataSetObserver != null) {
            this.mDataSetObserver.onChanged();
        }
    }

    protected int calculatePos(int position) {
        return position;
    }

    protected int calculateMaxPos() {
        return this.mItemCount;
    }

    protected int calculateMinPos() {
        return 0;
    }

    protected int calculateSyncPosition(int pos) {
        return pos;
    }

    private boolean isDoingDeleteAnimation() {
        return (this.mAnimationViewIndexMap == null || this.mAnimationViewIndexMap.size() == 0) ? false : true;
    }
}
