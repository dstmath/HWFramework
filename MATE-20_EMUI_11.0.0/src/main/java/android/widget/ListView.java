package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.app.slice.Slice;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Bundle;
import android.os.Trace;
import android.rms.iaware.IAwareSdk;
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
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
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
    private static final int SCREEN_PAGE_NUMBER = 15;
    private static final int SCROLL_TO_TOP_DURATION = 600;
    static final String TAG = "ListView";
    @UnsupportedAppUsage
    private boolean mAreAllItemsSelectable;
    private final ArrowScrollFocusResult mArrowScrollFocusResult;
    @UnsupportedAppUsage
    Drawable mDivider;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    int mDividerHeight;
    private boolean mDividerIsOpaque;
    private Paint mDividerPaint;
    private FocusSelector mFocusSelector;
    private boolean mFooterDividersEnabled;
    @UnsupportedAppUsage
    ArrayList<FixedViewInfo> mFooterViewInfos;
    private boolean mHeaderDividersEnabled;
    @UnsupportedAppUsage
    ArrayList<FixedViewInfo> mHeaderViewInfos;
    private boolean mIsCacheColorOpaque;
    private boolean mItemsCanFocus;
    Drawable mOverScrollFooter;
    Drawable mOverScrollHeader;
    private final Rect mTempRect;

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<ListView> {
        private int mDividerHeightId;
        private int mDividerId;
        private int mFooterDividersEnabledId;
        private int mHeaderDividersEnabledId;
        private boolean mPropertiesMapped = false;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mDividerId = propertyMapper.mapObject("divider", 16843049);
            this.mDividerHeightId = propertyMapper.mapInt("dividerHeight", 16843050);
            this.mFooterDividersEnabledId = propertyMapper.mapBoolean("footerDividersEnabled", 16843311);
            this.mHeaderDividersEnabledId = propertyMapper.mapBoolean("headerDividersEnabled", 16843310);
            this.mPropertiesMapped = true;
        }

        public void readProperties(ListView node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readObject(this.mDividerId, node.getDivider());
                propertyReader.readInt(this.mDividerHeightId, node.getDividerHeight());
                propertyReader.readBoolean(this.mFooterDividersEnabledId, node.areFooterDividersEnabled());
                propertyReader.readBoolean(this.mHeaderDividersEnabledId, node.areHeaderDividersEnabled());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
    }

    public class FixedViewInfo {
        public Object data;
        public boolean isSelectable;
        public View view;

        public FixedViewInfo() {
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
        int dividerHeight;
        this.mHeaderViewInfos = Lists.newArrayList();
        this.mFooterViewInfos = Lists.newArrayList();
        this.mAreAllItemsSelectable = true;
        this.mItemsCanFocus = false;
        this.mTempRect = new Rect();
        this.mArrowScrollFocusResult = new ArrowScrollFocusResult();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ListView, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.ListView, attrs, a, defStyleAttr, defStyleRes);
        CharSequence[] entries = a.getTextArray(0);
        if (entries != null) {
            setAdapter((ListAdapter) new ArrayAdapter(context, 17367043, entries));
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
        if (a.hasValueOrEmpty(2) && (dividerHeight = a.getDimensionPixelSize(2, 0)) != 0) {
            setDividerHeight(dividerHeight);
        }
        this.mHeaderDividersEnabled = a.getBoolean(3, true);
        this.mFooterDividersEnabled = a.getBoolean(4, true);
        a.recycle();
        this.mHwParallelWorker = HwWidgetFactory.getHwParallelWorker(this);
    }

    public int getMaxScrollAmount() {
        return (int) (((float) (this.mBottom - this.mTop)) * MAX_SCROLL_FACTOR);
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
                delta = getChildAt(childCount - 1).getBottom() - (getHeight() - this.mListPadding.bottom);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta += this.mDividerHeight;
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

    @Override // android.widget.AbsListView
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

    @Override // android.widget.AbsListView
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

    @Override // android.widget.AdapterView
    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    @Override // android.widget.AbsListView
    @RemotableViewMethod(asyncImpl = "setRemoteViewsAdapterAsync")
    public void setRemoteViewsAdapter(Intent intent) {
        super.setRemoteViewsAdapter(intent);
    }

    @Override // android.widget.AbsListView
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
    @Override // android.widget.AbsListView
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

    private boolean handleScrollToTop() {
        if (getScrollY() != 0 || !isScrollToTopEnabled()) {
            return false;
        }
        post(new Runnable() {
            /* class android.widget.ListView.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                int num = ListView.this.getChildCount() * 15;
                if (ListView.this.getFirstVisiblePosition() > num) {
                    ListView.this.setSelection(num);
                }
                ListView.this.smoothScrollToPositionFromTop(0, 0, 600);
            }
        });
        return true;
    }

    @Override // android.view.ViewGroup, android.view.View
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

    @Override // android.view.ViewGroup, android.view.ViewParent
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
        boolean scroll = true;
        if (showingBottomFadingEdge() && (this.mSelectedPosition < this.mItemCount - 1 || rect.bottom < bottomOfBottomChild - fadingEdge)) {
            listUnfadedBottom -= fadingEdge;
        }
        int scrollYDelta3 = 0;
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
        if (scrollYDelta3 == 0) {
            scroll = false;
        }
        if (scroll) {
            scrollListItemsBy(-scrollYDelta3);
            positionSelector(-1, child);
            this.mSelectedTop = child.getTop();
            invalidate();
        }
        return scroll;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsListView
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

    @UnsupportedAppUsage(maxTargetSdk = 28)
    private View fillDown(int pos, int nextTop) {
        View selectedView = null;
        int end = this.mBottom - this.mTop;
        if ((this.mGroupFlags & 34) == 34) {
            end -= this.mListPadding.bottom;
        }
        while (true) {
            boolean selected = true;
            if (nextTop >= end || pos >= this.mItemCount - this.mAnimOffset) {
                break;
            }
            if (pos != this.mSelectedPosition) {
                selected = false;
            }
            View child = makeAndAddView(pos, nextTop, true, this.mListPadding.left, selected);
            nextTop = child.getBottom() + this.mDividerHeight;
            if (selected) {
                selectedView = child;
            }
            pos++;
        }
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
        onFirstPositionChange();
        return selectedView;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
    private View fillUp(int pos, int nextBottom) {
        View selectedView = null;
        int end = 0;
        if ((this.mGroupFlags & 34) == 34) {
            end = this.mListPadding.top;
        }
        while (true) {
            boolean selected = true;
            if (nextBottom <= end || pos < 0) {
                break;
            }
            if (pos != this.mSelectedPosition) {
                selected = false;
            }
            View child = makeAndAddView(pos, nextBottom, false, this.mListPadding.left, selected);
            nextBottom = child.getTop() - this.mDividerHeight;
            if (selected) {
                selectedView = child;
            }
            pos--;
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
        if (selectedPosition != this.mItemCount - 1) {
            return childrenBottom - fadingEdgeLength;
        }
        return childrenBottom;
    }

    private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength, int selectedPosition) {
        if (selectedPosition > 0) {
            return childrenTop + fadingEdgeLength;
        }
        return childrenTop;
    }

    @Override // android.widget.AbsListView
    @RemotableViewMethod
    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position);
    }

    @Override // android.widget.AbsListView
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
        if (delta > 0) {
            View oldSel2 = makeAndAddView(selectedPosition - 1, oldSel.getTop(), true, this.mListPadding.left, false);
            int dividerHeight = this.mDividerHeight;
            sel = makeAndAddView(selectedPosition, oldSel2.getBottom() + dividerHeight, true, this.mListPadding.left, true);
            if (sel.getBottom() > bottomSelectionPixel) {
                int offset = Math.min(Math.min(sel.getTop() - topSelectionPixel, sel.getBottom() - bottomSelectionPixel), (childrenBottom - childrenTop) / 2);
                oldSel2.offsetTopAndBottom(-offset);
                sel.offsetTopAndBottom(-offset);
            }
            if (!this.mStackFromBottom) {
                fillUp(this.mSelectedPosition - 2, sel.getTop() - dividerHeight);
                adjustViewsUpOrDown();
                fillDown(this.mSelectedPosition + 1, sel.getBottom() + dividerHeight);
            } else {
                fillDown(this.mSelectedPosition + 1, sel.getBottom() + dividerHeight);
                adjustViewsUpOrDown();
                fillUp(this.mSelectedPosition - 2, sel.getTop() - dividerHeight);
            }
        } else if (delta < 0) {
            if (newSel != null) {
                sel = makeAndAddView(selectedPosition, newSel.getTop(), true, this.mListPadding.left, true);
            } else {
                sel = makeAndAddView(selectedPosition, oldSel.getTop(), false, this.mListPadding.left, true);
            }
            if (sel.getTop() < topSelectionPixel) {
                sel.offsetTopAndBottom(Math.min(Math.min(topSelectionPixel - sel.getTop(), bottomSelectionPixel - sel.getBottom()), (childrenBottom - childrenTop) / 2));
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

    /* access modifiers changed from: private */
    public class FocusSelector implements Runnable {
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

        @Override // java.lang.Runnable
        public void run() {
            int i = this.mAction;
            if (i == 1) {
                ListView.this.setSelectionFromTop(this.mPosition, this.mPositionTop);
                this.mAction = 2;
            } else if (i == 3) {
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

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        FocusSelector focusSelector = this.mFocusSelector;
        if (focusSelector != null) {
            removeCallbacks(focusSelector);
            this.mFocusSelector = null;
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.View
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        View focusedChild;
        if (getChildCount() > 0 && (focusedChild = getFocusedChild()) != null) {
            int childPosition = this.mFirstPosition + indexOfChild(focusedChild);
            int top = focusedChild.getTop() - Math.max(0, focusedChild.getBottom() - (h - this.mPaddingTop));
            if (this.mFocusSelector == null) {
                this.mFocusSelector = new FocusSelector();
            }
            post(this.mFocusSelector.setupForSetSelection(childPosition, top));
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize;
        int heightSize;
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
            measureScrapChild(child, 0, widthMeasureSpec, heightSize2);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            childState = combineMeasuredStates(0, child.getMeasuredState());
            if (recycleOnMeasure() && this.mRecycler.shouldRecycleViewType(((AbsListView.LayoutParams) child.getLayoutParams()).viewType)) {
                this.mRecycler.addScrapView(child, 0);
            }
        }
        if (widthMode == 0) {
            widthSize = this.mListPadding.left + this.mListPadding.right + childWidth + getVerticalScrollbarWidth();
        } else {
            widthSize = (-16777216 & childState) | widthSize2;
        }
        if (heightMode == 0) {
            heightSize = this.mListPadding.top + this.mListPadding.bottom + childHeight + (getVerticalFadingEdgeLength() * 2);
        } else {
            heightSize = heightSize2;
        }
        if (heightMode == Integer.MIN_VALUE) {
            heightSize = measureHeightOfChildren(widthMeasureSpec, 0, -1, heightSize, -1);
        }
        setMeasuredDimension(widthSize, heightSize);
        this.mWidthMeasureSpec = widthMeasureSpec;
    }

    private void measureScrapChild(View child, int position, int widthMeasureSpec, int heightHint) {
        int childHeightSpec;
        AbsListView.LayoutParams p = (AbsListView.LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (AbsListView.LayoutParams) generateDefaultLayoutParams();
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
    @ViewDebug.ExportedProperty(category = Slice.HINT_LIST)
    public boolean recycleOnMeasure() {
        return true;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public final int measureHeightOfChildren(int widthMeasureSpec, int startPosition, int endPosition, int maxHeight, int disallowPartialChildPosition) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null) {
            return this.mListPadding.top + this.mListPadding.bottom;
        }
        int returnedHeight = this.mListPadding.top + this.mListPadding.bottom;
        int dividerHeight = this.mDividerHeight;
        int prevHeightWithoutPartialChild = 0;
        int endPosition2 = endPosition == -1 ? adapter.getCount() - 1 : endPosition;
        AbsListView.RecycleBin recycleBin = this.mRecycler;
        boolean recyle = recycleOnMeasure();
        boolean[] isScrap = this.mIsScrap;
        int i = startPosition;
        while (i <= endPosition2) {
            View child = obtainView(i, isScrap);
            measureScrapChild(child, i, widthMeasureSpec, maxHeight);
            if (i > 0) {
                returnedHeight += dividerHeight;
            }
            if (recyle && recycleBin.shouldRecycleViewType(((AbsListView.LayoutParams) child.getLayoutParams()).viewType)) {
                recycleBin.addScrapView(child, -1);
            }
            returnedHeight += child.getMeasuredHeight();
            if (returnedHeight >= maxHeight) {
                return (disallowPartialChildPosition < 0 || i <= disallowPartialChildPosition || prevHeightWithoutPartialChild <= 0 || returnedHeight == maxHeight) ? maxHeight : prevHeightWithoutPartialChild;
            }
            if (disallowPartialChildPosition >= 0 && i >= disallowPartialChildPosition) {
                prevHeightWithoutPartialChild = returnedHeight;
            }
            i++;
        }
        return returnedHeight;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsListView
    public int findMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return -1;
        }
        if (!this.mStackFromBottom) {
            for (int i = 0; i < childCount; i++) {
                if (y <= getChildAt(i).getBottom()) {
                    return this.mFirstPosition + i;
                }
            }
            return -1;
        }
        for (int i2 = childCount - 1; i2 >= 0; i2--) {
            if (y >= getChildAt(i2).getTop()) {
                return this.mFirstPosition + i2;
            }
        }
        return -1;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
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

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
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

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
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

    /* JADX INFO: Multiple debug info for r1v9 boolean: [D('dataChanged' boolean), D('index' int)] */
    /* JADX INFO: Multiple debug info for r1v17 int: [D('firstPosition' int), D('focusLayoutRestoreDirectChild' android.view.View)] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00ae  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c7  */
    @Override // android.widget.AbsListView
    public void layoutChildren() {
        FocusSelector focusSelector;
        int delta;
        View newSel;
        View oldFirst;
        View oldSel;
        boolean dataChanged;
        AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode;
        View accessibilityFocusLayoutRestoreView;
        int accessibilityFocusPosition;
        View focusLayoutRestoreView;
        View focusLayoutRestoreDirectChild;
        int accessibilityFocusPosition2;
        View focusLayoutRestoreDirectChild2;
        AbsListView.RecycleBin recycleBin;
        View sel;
        View restoreView;
        Runnable focusRunnable;
        View focusHost;
        View focusChild;
        boolean blockLayoutRequests = this.mBlockLayoutRequests;
        if (!blockLayoutRequests) {
            this.mBlockLayoutRequests = true;
            try {
                super.layoutChildren();
                invalidate();
                if (this.mAdapter == null) {
                    resetList();
                    invokeOnItemScrollListener();
                    FocusSelector focusSelector2 = this.mFocusSelector;
                    if (focusSelector2 != null) {
                        focusSelector2.onLayoutComplete();
                    }
                    if (!blockLayoutRequests) {
                        this.mBlockLayoutRequests = false;
                        return;
                    }
                    return;
                }
                int childrenTop = this.mListPadding.top;
                int childrenBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
                int childCount = getChildCount();
                int delta2 = 0;
                View oldSel2 = null;
                int i = this.mLayoutMode;
                if (i != 1) {
                    if (i == 2) {
                        int index = this.mNextSelectedPosition - this.mFirstPosition;
                        if (index < 0 || index >= childCount) {
                            delta = 0;
                            oldSel = null;
                            oldFirst = null;
                            newSel = null;
                        } else {
                            delta = 0;
                            oldSel = null;
                            oldFirst = null;
                            newSel = getChildAt(index);
                        }
                    } else if (!(i == 3 || i == 4 || i == 5)) {
                        int index2 = this.mSelectedPosition - this.mFirstPosition;
                        if (index2 >= 0 && index2 < childCount) {
                            oldSel2 = getChildAt(index2);
                        }
                        View oldFirst2 = getChildAt(0);
                        if (this.mNextSelectedPosition >= 0) {
                            delta2 = this.mNextSelectedPosition - this.mSelectedPosition;
                        }
                        delta = delta2;
                        oldSel = oldSel2;
                        oldFirst = oldFirst2;
                        newSel = getChildAt(index2 + delta2);
                    }
                    dataChanged = this.mDataChanged;
                    if (dataChanged) {
                        handleDataChanged();
                    }
                    if (this.mItemCount != 0) {
                        resetList();
                        invokeOnItemScrollListener();
                        FocusSelector focusSelector3 = this.mFocusSelector;
                        if (focusSelector3 != null) {
                            focusSelector3.onLayoutComplete();
                        }
                        if (!blockLayoutRequests) {
                            this.mBlockLayoutRequests = false;
                            return;
                        }
                        return;
                    } else if (this.mItemCount == this.mAdapter.getCount()) {
                        setSelectedPositionInt(this.mNextSelectedPosition);
                        AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode2 = null;
                        View accessibilityFocusLayoutRestoreView2 = null;
                        ViewRootImpl viewRootImpl = getViewRootImpl();
                        if (viewRootImpl == null || (focusHost = viewRootImpl.getAccessibilityFocusedHost()) == null || (focusChild = getAccessibilityFocusedChild(focusHost)) == null) {
                            accessibilityFocusLayoutRestoreNode = null;
                            accessibilityFocusLayoutRestoreView = null;
                            accessibilityFocusPosition = -1;
                        } else {
                            if (!dataChanged || isDirectChildHeaderOrFooter(focusChild) || (focusChild.hasTransientState() && this.mAdapterHasStableIds)) {
                                accessibilityFocusLayoutRestoreView2 = focusHost;
                                accessibilityFocusLayoutRestoreNode2 = viewRootImpl.getAccessibilityFocusedVirtualView();
                            }
                            accessibilityFocusPosition = getPositionForView(focusChild);
                            accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode2;
                            accessibilityFocusLayoutRestoreView = accessibilityFocusLayoutRestoreView2;
                        }
                        View focusLayoutRestoreDirectChild3 = null;
                        View focusLayoutRestoreView2 = null;
                        View focusedChild = getFocusedChild();
                        if (focusedChild != null) {
                            if (!dataChanged || isDirectChildHeaderOrFooter(focusedChild) || focusedChild.hasTransientState() || this.mAdapterHasStableIds) {
                                focusLayoutRestoreDirectChild3 = focusedChild;
                                focusLayoutRestoreView2 = findFocus();
                                if (focusLayoutRestoreView2 != null) {
                                    focusLayoutRestoreView2.dispatchStartTemporaryDetach();
                                }
                            }
                            requestFocus();
                            focusLayoutRestoreDirectChild = focusLayoutRestoreDirectChild3;
                            focusLayoutRestoreView = focusLayoutRestoreView2;
                        } else {
                            focusLayoutRestoreDirectChild = null;
                            focusLayoutRestoreView = null;
                        }
                        int firstPosition = this.mFirstPosition;
                        AbsListView.RecycleBin recycleBin2 = this.mRecycler;
                        if (dataChanged) {
                            for (int i2 = 0; i2 < childCount; i2++) {
                                recycleBin2.addScrapView(getChildAt(i2), firstPosition + i2);
                            }
                        } else {
                            recycleBin2.fillActiveViews(childCount, firstPosition);
                        }
                        detachAllViewsFromParent();
                        recycleBin2.removeSkippedScrap();
                        switch (this.mLayoutMode) {
                            case 1:
                                recycleBin = recycleBin2;
                                focusLayoutRestoreDirectChild2 = focusLayoutRestoreDirectChild;
                                accessibilityFocusPosition2 = accessibilityFocusPosition;
                                this.mFirstPosition = 0;
                                sel = fillFromTop(childrenTop);
                                adjustViewsUpOrDown();
                                break;
                            case 2:
                                recycleBin = recycleBin2;
                                focusLayoutRestoreDirectChild2 = focusLayoutRestoreDirectChild;
                                accessibilityFocusPosition2 = accessibilityFocusPosition;
                                if (newSel != null) {
                                    sel = fillFromSelection(newSel.getTop(), childrenTop, childrenBottom);
                                    break;
                                } else {
                                    sel = fillFromMiddle(childrenTop, childrenBottom);
                                    break;
                                }
                            case 3:
                                recycleBin = recycleBin2;
                                focusLayoutRestoreDirectChild2 = focusLayoutRestoreDirectChild;
                                accessibilityFocusPosition2 = accessibilityFocusPosition;
                                sel = fillUp(this.mItemCount - 1, childrenBottom);
                                adjustViewsUpOrDown();
                                break;
                            case 4:
                                recycleBin = recycleBin2;
                                focusLayoutRestoreDirectChild2 = focusLayoutRestoreDirectChild;
                                accessibilityFocusPosition2 = accessibilityFocusPosition;
                                int selectedPosition = reconcileSelectedPosition();
                                View sel2 = fillSpecific(selectedPosition, this.mSpecificTop);
                                if (!(sel2 != null || this.mFocusSelector == null || (focusRunnable = this.mFocusSelector.setupFocusIfValid(selectedPosition)) == null)) {
                                    post(focusRunnable);
                                }
                                sel = sel2;
                                break;
                            case 5:
                                recycleBin = recycleBin2;
                                focusLayoutRestoreDirectChild2 = focusLayoutRestoreDirectChild;
                                accessibilityFocusPosition2 = accessibilityFocusPosition;
                                sel = fillSpecific(this.mSyncPosition, this.mSpecificTop);
                                break;
                            case 6:
                                recycleBin = recycleBin2;
                                focusLayoutRestoreDirectChild2 = focusLayoutRestoreDirectChild;
                                accessibilityFocusPosition2 = accessibilityFocusPosition;
                                sel = moveSelection(oldSel, newSel, delta, childrenTop, childrenBottom);
                                break;
                            default:
                                recycleBin = recycleBin2;
                                focusLayoutRestoreDirectChild2 = focusLayoutRestoreDirectChild;
                                accessibilityFocusPosition2 = accessibilityFocusPosition;
                                if (childCount == 0) {
                                    if (!this.mStackFromBottom) {
                                        setSelectedPositionInt(lookForSelectablePosition(0, true));
                                        sel = fillFromTop(childrenTop);
                                        break;
                                    } else {
                                        setSelectedPositionInt(lookForSelectablePosition(this.mItemCount - 1, false));
                                        sel = fillUp(this.mItemCount - 1, childrenBottom);
                                        break;
                                    }
                                } else if (this.mSelectedPosition < 0 || this.mSelectedPosition >= this.mItemCount) {
                                    if (this.mFirstPosition < this.mItemCount) {
                                        sel = fillSpecific(this.mFirstPosition, oldFirst == null ? childrenTop : oldFirst.getTop());
                                        break;
                                    } else {
                                        sel = fillSpecific(0, childrenTop);
                                        break;
                                    }
                                } else {
                                    sel = fillSpecific(this.mSelectedPosition, oldSel == null ? childrenTop : oldSel.getTop());
                                    break;
                                }
                                break;
                        }
                        recycleBin.scrapActiveViews();
                        removeUnusedFixedViews(this.mHeaderViewInfos);
                        removeUnusedFixedViews(this.mFooterViewInfos);
                        if (sel != null) {
                            if (!this.mItemsCanFocus || !hasFocus() || sel.hasFocus()) {
                                positionSelector(-1, sel);
                            } else if (!((sel == focusLayoutRestoreDirectChild2 && focusLayoutRestoreView != null && focusLayoutRestoreView.requestFocus()) || sel.requestFocus())) {
                                View focused = getFocusedChild();
                                if (focused != null) {
                                    focused.clearFocus();
                                }
                                positionSelector(-1, sel);
                            } else {
                                sel.setSelected(false);
                                this.mSelectorRect.setEmpty();
                            }
                            this.mSelectedTop = sel.getTop();
                        } else {
                            if (this.mTouchMode == 1 || this.mTouchMode == 2) {
                                View child = getChildAt(this.mMotionPosition - this.mFirstPosition);
                                if (child != null) {
                                    positionSelector(this.mMotionPosition, child);
                                }
                            } else if (this.mSelectorPosition != -1) {
                                View child2 = getChildAt(this.mSelectorPosition - this.mFirstPosition);
                                if (child2 != null) {
                                    positionSelector(this.mSelectorPosition, child2);
                                }
                            } else {
                                this.mSelectedTop = 0;
                                this.mSelectorRect.setEmpty();
                            }
                            if (hasFocus() && focusLayoutRestoreView != null) {
                                focusLayoutRestoreView.requestFocus();
                            }
                        }
                        if (viewRootImpl != null) {
                            if (viewRootImpl.getAccessibilityFocusedHost() == null) {
                                if (accessibilityFocusLayoutRestoreView != null && accessibilityFocusLayoutRestoreView.isAttachedToWindow()) {
                                    AccessibilityNodeProvider provider = accessibilityFocusLayoutRestoreView.getAccessibilityNodeProvider();
                                    if (accessibilityFocusLayoutRestoreNode == null || provider == null) {
                                        accessibilityFocusLayoutRestoreView.requestAccessibilityFocus();
                                    } else {
                                        provider.performAction(AccessibilityNodeInfo.getVirtualDescendantId(accessibilityFocusLayoutRestoreNode.getSourceNodeId()), 64, null);
                                    }
                                } else if (!(accessibilityFocusPosition2 == -1 || (restoreView = getChildAt(MathUtils.constrain(accessibilityFocusPosition2 - this.mFirstPosition, 0, getChildCount() - 1))) == null)) {
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
                        if (blockLayoutRequests) {
                            return;
                        }
                        return;
                    } else {
                        throw new IllegalStateException("The content of the adapter has changed but ListView did not receive a notification. Make sure the content of your adapter is not modified from a background thread, but only from the UI thread. Make sure your adapter calls notifyDataSetChanged() when its content changes. [in ListView(" + getId() + ", " + getClass() + ") with Adapter(" + this.mAdapter.getClass() + ")]");
                    }
                }
                delta = 0;
                oldSel = null;
                oldFirst = null;
                newSel = null;
                dataChanged = this.mDataChanged;
                if (dataChanged) {
                }
                if (this.mItemCount != 0) {
                }
            } finally {
                focusSelector = this.mFocusSelector;
                if (focusSelector != null) {
                    focusSelector.onLayoutComplete();
                }
                if (!blockLayoutRequests) {
                    this.mBlockLayoutRequests = false;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsListView
    @UnsupportedAppUsage
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

    /* JADX INFO: Multiple debug info for r2v2 java.util.ArrayList<android.widget.ListView$FixedViewInfo>: [D('i' int), D('footers' java.util.ArrayList<android.widget.ListView$FixedViewInfo>)] */
    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    private View makeAndAddView(int position, int y, boolean flow, int childrenLeft, boolean selected) {
        View activeView;
        if (this.mDataChanged || (activeView = this.mRecycler.getActiveView(position)) == null) {
            if (!this.mDataChanged && this.mHwParallelWorker != null && this.mHwParallelWorker.isPrefetchOptimizeEnable()) {
                View prefetchView = this.mHwParallelWorker.getPrefetchView();
                boolean isScrapData = this.mHwParallelWorker.isPrefetchViewScrap();
                boolean isCheckViewPosPass = this.mHwParallelWorker.isPrefetchViewPosValid(position);
                this.mHwParallelWorker.clearPrefetchInfo();
                if (isCheckViewPosPass && prefetchView != null) {
                    setupChild(prefetchView, position, y, flow, childrenLeft, selected, isScrapData);
                    return prefetchView;
                }
            }
            IAwareSdk.asyncSendData("UniperfClient", 4116, 0);
            this.mAddItemViewType = this.mAdapter.getItemViewType(position);
            this.mAddItemViewPosition = position;
            View child = obtainView(position, this.mIsScrap);
            IAwareSdk.asyncSendData("UniperfClient", 4116, -1);
            setupChild(child, position, y, flow, childrenLeft, selected, this.mIsScrap[0]);
            this.mAddItemViewType = -10000;
            this.mAddItemViewPosition = -1;
            return child;
        }
        setupChild(activeView, position, y, flow, childrenLeft, selected, true);
        return activeView;
    }

    private void setupChild(View child, int position, int y, boolean flowDown, int childrenLeft, boolean selected, boolean isAttachedToWindow) {
        int childHeightSpec;
        Trace.traceBegin(8, "setupListItem");
        boolean isSelected = selected && shouldShowSelector();
        boolean updateChildSelected = isSelected != child.isSelected();
        int mode = this.mTouchMode;
        boolean isPressed = mode > 0 && mode < 3 && this.mMotionPosition == position;
        boolean updateChildPressed = isPressed != child.isPressed();
        boolean needToMeasure = !isAttachedToWindow || updateChildSelected || child.isLayoutRequested();
        AbsListView.LayoutParams p = (AbsListView.LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (AbsListView.LayoutParams) generateDefaultLayoutParams();
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
        int i = -1;
        if ((!isAttachedToWindow || p.forceAdd) && (!p.recycledHeaderFooter || p.viewType != -2)) {
            p.forceAdd = false;
            if (p.viewType == -2) {
                p.recycledHeaderFooter = true;
            }
            if (!flowDown) {
                i = 0;
            }
            addViewInLayout(child, i, p, true);
            child.resolveRtlPropertiesIfNeeded();
        } else {
            if (!flowDown) {
                i = 0;
            }
            attachViewToParent(child, i, p);
            if (isAttachedToWindow && ((AbsListView.LayoutParams) child.getLayoutParams()).scrappedFromPosition != position) {
                child.jumpDrawablesToCurrentState();
            }
        }
        if (needToMeasure) {
            int childWidthSpec = ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
            int lpHeight = p.height;
            if (lpHeight > 0) {
                childHeightSpec = View.MeasureSpec.makeMeasureSpec(lpHeight, 1073741824);
            } else {
                childHeightSpec = View.MeasureSpec.makeSafeMeasureSpec(getMeasuredHeight(), 0);
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
        if (this.mCachingStarted && !child.isDrawingCacheEnabled()) {
            child.setDrawingCacheEnabled(true);
        }
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AdapterView, android.view.ViewGroup
    public boolean canAnimate() {
        return super.canAnimate() && this.mItemCount > 0;
    }

    @Override // android.widget.AdapterView
    public void setSelection(int position) {
        setSelectionFromTop(position, 0);
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsListView
    @UnsupportedAppUsage
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
    @Override // android.widget.AdapterView
    @UnsupportedAppUsage
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
                    position--;
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
            position2 = Math.max(0, position + 1);
            while (position2 < current2 && !adapter.isEnabled(position2)) {
                position2++;
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
        } else if (this.mAdapter != null) {
            setSelection(count);
        } else {
            this.mNextSelectedPosition = count;
            this.mLayoutMode = 2;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = super.dispatchKeyEvent(event);
        if (handled || getFocusedChild() == null || event.getAction() != 0) {
            return handled;
        }
        return onKeyDown(event.getKeyCode(), event);
    }

    @Override // android.widget.AbsListView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return commonKey(keyCode, repeatCount, event);
    }

    @Override // android.widget.AbsListView, android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

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
        if (KeyEvent.isConfirmKey(keyCode) && event.hasNoModifiers() && action != 1 && !(handled = resurrectSelectionIfNeeded()) && event.getRepeatCount() == 0 && getChildCount() > 0) {
            keyPressed();
            handled = true;
        }
        if (!handled && action != 1) {
            if (keyCode != 61) {
                if (keyCode != 92) {
                    if (keyCode != 93) {
                        if (keyCode != 122) {
                            if (keyCode != 123) {
                                switch (keyCode) {
                                    case 19:
                                        if (!event.hasNoModifiers()) {
                                            if (event.hasModifiers(2)) {
                                                handled = resurrectSelectionIfNeeded() || fullScroll(33);
                                                break;
                                            }
                                        } else {
                                            handled = resurrectSelectionIfNeeded();
                                            if (!handled) {
                                                while (true) {
                                                    count2 = count - 1;
                                                    if (count > 0 && arrowScroll(33)) {
                                                        handled = true;
                                                        count = count2;
                                                    }
                                                }
                                                count = count2;
                                                break;
                                            }
                                        }
                                        break;
                                    case 20:
                                        if (!event.hasNoModifiers()) {
                                            if (event.hasModifiers(2)) {
                                                handled = resurrectSelectionIfNeeded() || fullScroll(130);
                                                break;
                                            }
                                        } else {
                                            handled = resurrectSelectionIfNeeded();
                                            if (!handled) {
                                                while (true) {
                                                    count2 = count - 1;
                                                    if (count > 0 && arrowScroll(130)) {
                                                        handled = true;
                                                        count = count2;
                                                    }
                                                }
                                                count = count2;
                                                break;
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
                                }
                            } else if (event.hasNoModifiers()) {
                                handled = resurrectSelectionIfNeeded() || fullScroll(130);
                            }
                        } else if (event.hasNoModifiers()) {
                            handled = resurrectSelectionIfNeeded() || fullScroll(33);
                        }
                    } else if (event.hasNoModifiers()) {
                        handled = resurrectSelectionIfNeeded() || pageScroll(130);
                    } else if (event.hasModifiers(2)) {
                        handled = resurrectSelectionIfNeeded() || fullScroll(130);
                    }
                } else if (event.hasNoModifiers()) {
                    handled = resurrectSelectionIfNeeded() || pageScroll(33);
                } else if (event.hasModifiers(2)) {
                    handled = resurrectSelectionIfNeeded() || fullScroll(33);
                }
            } else if (event.hasNoModifiers()) {
                handled = resurrectSelectionIfNeeded() || arrowScroll(130);
            } else if (event.hasModifiers(1)) {
                handled = resurrectSelectionIfNeeded() || arrowScroll(33);
            }
        }
        if (handled || sendToTextFilter(keyCode, count, event)) {
            return true;
        }
        if (action == 0) {
            return super.onKeyDown(keyCode, event);
        }
        if (action == 1) {
            return super.onKeyUp(keyCode, event);
        }
        if (action != 2) {
            return false;
        }
        return super.onKeyMultiple(keyCode, count, event);
    }

    /* access modifiers changed from: package-private */
    public boolean pageScroll(int direction) {
        boolean down;
        int nextPage;
        int position;
        if (direction == 33) {
            nextPage = Math.max(0, (this.mSelectedPosition - getChildCount()) - 1);
            down = false;
        } else if (direction != 130) {
            return false;
        } else {
            nextPage = Math.min(this.mItemCount - 1, (this.mSelectedPosition + getChildCount()) - 1);
            down = true;
        }
        if (nextPage < 0 || (position = lookForSelectablePositionAfter(this.mSelectedPosition, nextPage, down)) < 0) {
            return false;
        }
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

    /* access modifiers changed from: package-private */
    public boolean fullScroll(int direction) {
        int lastItem;
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
        } else if (direction == 130 && this.mSelectedPosition < (lastItem = this.mItemCount - 1)) {
            int position2 = lookForSelectablePositionAfter(this.mSelectedPosition, lastItem, false);
            if (position2 >= 0) {
                this.mLayoutMode = 3;
                setSelectionInt(position2);
                invokeOnItemScrollListener();
            }
            moved = true;
        }
        if (moved && !awakenScrollBars()) {
            awakenScrollBars();
            invalidate();
        }
        return moved;
    }

    private boolean handleHorizontalFocusWithinListItem(int direction) {
        View selectedView;
        if (direction == 17 || direction == 66) {
            int numChildren = getChildCount();
            if (!this.mItemsCanFocus || numChildren <= 0 || this.mSelectedPosition == -1 || (selectedView = getSelectedView()) == null || !selectedView.hasFocus() || !(selectedView instanceof ViewGroup)) {
                return false;
            }
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
            return false;
        }
        throw new IllegalArgumentException("direction must be one of {View.FOCUS_LEFT, View.FOCUS_RIGHT}");
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
        if (nextSelected < 0 || nextSelected >= this.mAdapter.getCount()) {
            return -1;
        }
        if (direction != 130) {
            z = false;
        }
        return lookForSelectablePosition(nextSelected, z);
    }

    private boolean arrowScrollImpl(int direction) {
        View focused;
        View focused2;
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
            if (this.mItemsCanFocus && focusResult == null && (focused2 = getFocusedChild()) != null) {
                focused2.clearFocus();
            }
            needToRedraw = true;
            checkSelectionChanged();
        }
        if (amountToScroll > 0) {
            scrollListItemsBy(direction == 33 ? amountToScroll : -amountToScroll);
            needToRedraw = true;
        }
        if (this.mItemsCanFocus && focusResult == null && selectedView != null && selectedView.hasFocus() && (focused = selectedView.findFocus()) != null && (!isViewAncestorOf(focused, this) || distanceToView(focused) > 0)) {
            focused.clearFocus();
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

    /* access modifiers changed from: private */
    public static class ArrowScrollFocusResult {
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

    private int lookForSelectablePositionOnScreen(int direction) {
        int startPos;
        int firstPosition = this.mFirstPosition;
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
            ListAdapter adapter = getAdapter();
            for (int pos = startPos; pos <= lastVisiblePos; pos++) {
                if (adapter.isEnabled(pos) && getChildAt(pos - firstPosition).getVisibility() == 0) {
                    return pos;
                }
            }
        } else {
            int last = (getChildCount() + firstPosition) - 1;
            int startPos2 = this.mSelectedPosition != -1 ? this.mSelectedPosition - 1 : (getChildCount() + firstPosition) - 1;
            if (startPos2 < 0 || startPos2 >= this.mAdapter.getCount()) {
                return -1;
            }
            if (startPos2 > last) {
                startPos2 = last;
            }
            ListAdapter adapter2 = getAdapter();
            for (int pos2 = startPos2; pos2 >= firstPosition; pos2--) {
                if (adapter2.isEnabled(pos2) && getChildAt(pos2 - firstPosition).getVisibility() == 0) {
                    return pos2;
                }
            }
        }
        return -1;
    }

    private ArrowScrollFocusResult arrowScrollFocused(int direction) {
        View oldFocus;
        int selectablePosition;
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
            if (this.mSelectedPosition != -1 && positionOfNewFocus != this.mSelectedPosition && (selectablePosition = lookForSelectablePositionOnScreen(direction)) != -1 && ((direction == 130 && selectablePosition < positionOfNewFocus) || (direction == 33 && selectablePosition > positionOfNewFocus))) {
                return null;
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
        if (child == parent) {
            return true;
        }
        ViewParent theParent = child.getParent();
        if (!(theParent instanceof ViewGroup) || !isViewAncestorOf((View) theParent, parent)) {
            return false;
        }
        return true;
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

    @UnsupportedAppUsage
    private void scrollListItemsBy(int amount) {
        int lastVisiblePosition;
        offsetChildrenTopAndBottom(amount);
        int listBottom = getHeight() - this.mListPadding.bottom;
        int listTop = this.mListPadding.top;
        AbsListView.RecycleBin recycleBin = this.mRecycler;
        if (amount < 0) {
            int numChildren = getChildCount();
            View last = getChildAt(numChildren - 1);
            while (last.getBottom() < listBottom && (this.mFirstPosition + numChildren) - 1 < this.mItemCount - 1) {
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
            setDescendantFocusability(393216);
        }
    }

    public boolean getItemsCanFocus() {
        return this.mItemsCanFocus;
    }

    @Override // android.view.View
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

    @Override // android.widget.AbsListView
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

    /* JADX INFO: Multiple debug info for r5v2 android.widget.ListAdapter: [D('areAllItemsSelectable' boolean), D('adapter' android.widget.ListAdapter)] */
    /* JADX INFO: Multiple debug info for r5v3 android.graphics.Paint: [D('paint' android.graphics.Paint), D('adapter' android.widget.ListAdapter)] */
    /* JADX INFO: Multiple debug info for r13v14 int: [D('itemIndex' int), D('itemCount' int)] */
    /* JADX INFO: Multiple debug info for r8v9 int: [D('previousIndex' int), D('start' int)] */
    /* JADX INFO: Multiple debug info for r4v4 int: [D('overscrollHeader' android.graphics.drawable.Drawable), D('itemIndex' int)] */
    /* JADX INFO: Multiple debug info for r13v20 int: [D('nextIndex' int), D('listBottom' int)] */
    /* JADX INFO: Multiple debug info for r9v8 'adapter'  android.widget.ListAdapter: [D('adapter' android.widget.ListAdapter), D('drawDividers' boolean)] */
    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.ViewGroup, android.view.View
    public void dispatchDraw(Canvas canvas) {
        ListAdapter adapter;
        int itemCount;
        int effectivePaddingTop;
        int effectivePaddingBottom;
        Drawable overscrollHeader;
        boolean footerDividers;
        int start;
        Drawable overscrollFooter;
        int first;
        int bottom;
        Drawable overscrollFooter2;
        boolean drawDividers;
        int listBottom;
        boolean drawOverscrollHeader;
        ListAdapter adapter2;
        Paint paint;
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
            int effectivePaddingTop2 = effectivePaddingTop;
            int listBottom2 = ((this.mBottom - this.mTop) - effectivePaddingBottom) + this.mScrollY;
            if (!this.mStackFromBottom) {
                int scrollY = this.mScrollY;
                if (count <= 0 || scrollY >= 0) {
                    bottom = 0;
                } else if (drawOverscrollHeader2) {
                    bottom = 0;
                    bounds.bottom = 0;
                    bounds.top = scrollY;
                    drawOverscrollHeader(canvas, overscrollHeader2, bounds);
                } else {
                    bottom = 0;
                    if (drawDividers2) {
                        bounds.bottom = 0;
                        bounds.top = -dividerHeight;
                        drawDivider(canvas, bounds, -1);
                    }
                }
                int i = 0;
                int bottom2 = bottom;
                while (i < count) {
                    int itemIndex = first2 + i;
                    boolean isHeader = itemIndex < headerCount;
                    boolean isFooter = itemIndex >= footerLimit;
                    if ((headerDividers || !isHeader) && (footerDividers2 || !isFooter)) {
                        bottom2 = getChildAt(i).getBottom();
                        drawOverscrollHeader = drawOverscrollHeader2;
                        boolean isLastItem = i == count + -1;
                        if (!drawDividers2 || bottom2 >= listBottom2) {
                            drawDividers = drawDividers2;
                            listBottom = listBottom2;
                            adapter2 = adapter;
                            paint = paint2;
                        } else if (!drawOverscrollFooter || !isLastItem) {
                            listBottom = listBottom2;
                            int listBottom3 = itemIndex + 1;
                            drawDividers = drawDividers2;
                            adapter2 = adapter;
                            if (checkIsEnabled(adapter2, itemIndex)) {
                                if (headerDividers || (!isHeader && listBottom3 >= headerCount)) {
                                    if (isLastItem || (checkIsEnabled(adapter2, listBottom3) && (footerDividers2 || (!isFooter && listBottom3 < footerLimit)))) {
                                        bounds.top = bottom2;
                                        bounds.bottom = bottom2 + dividerHeight;
                                        drawDivider(canvas, bounds, i);
                                        paint = paint2;
                                    }
                                }
                            }
                            if (fillForMissingDividers) {
                                bounds.top = bottom2;
                                bounds.bottom = bottom2 + dividerHeight;
                                paint = paint2;
                                canvas.drawRect(bounds, paint);
                            } else {
                                paint = paint2;
                            }
                        } else {
                            drawDividers = drawDividers2;
                            listBottom = listBottom2;
                            adapter2 = adapter;
                            paint = paint2;
                        }
                    } else {
                        drawOverscrollHeader = drawOverscrollHeader2;
                        drawDividers = drawDividers2;
                        listBottom = listBottom2;
                        adapter2 = adapter;
                        paint = paint2;
                    }
                    i++;
                    paint2 = paint;
                    adapter = adapter2;
                    overscrollHeader2 = overscrollHeader2;
                    drawOverscrollHeader2 = drawOverscrollHeader;
                    listBottom2 = listBottom;
                    drawDividers2 = drawDividers;
                }
                int overFooterBottom = this.mBottom + this.mScrollY;
                if (!drawOverscrollFooter) {
                    overscrollFooter2 = overscrollFooter3;
                } else if (first2 + count != itemCount || overFooterBottom <= bottom2) {
                    overscrollFooter2 = overscrollFooter3;
                } else {
                    bounds.top = bottom2;
                    bounds.bottom = overFooterBottom;
                    overscrollFooter2 = overscrollFooter3;
                    drawOverscrollFooter(canvas, overscrollFooter2, bounds);
                }
            } else {
                Drawable overscrollFooter4 = overscrollFooter3;
                int itemCount3 = itemCount;
                int scrollY2 = this.mScrollY;
                if (count <= 0 || !drawOverscrollHeader2) {
                    overscrollHeader = overscrollHeader2;
                } else {
                    bounds.top = scrollY2;
                    bounds.bottom = getChildAt(0).getTop();
                    overscrollHeader = overscrollHeader2;
                    drawOverscrollHeader(canvas, overscrollHeader, bounds);
                }
                int i2 = drawOverscrollHeader2 ? 1 : 0;
                int start2 = i2;
                int i3 = i2;
                while (i3 < count) {
                    int itemCount4 = first2 + i3;
                    boolean isHeader2 = itemCount4 < headerCount;
                    boolean isFooter2 = itemCount4 >= footerLimit;
                    if ((headerDividers || !isHeader2) && (footerDividers2 || !isFooter2)) {
                        first = first2;
                        int top = getChildAt(i3).getTop();
                        if (drawDividers2) {
                            overscrollFooter = overscrollFooter4;
                            if (top > effectivePaddingTop2) {
                                effectivePaddingTop2 = effectivePaddingTop2;
                                boolean isFirstItem = i3 == start2;
                                start = start2;
                                int start3 = itemCount4 - 1;
                                if (!checkIsEnabled(adapter, itemCount4)) {
                                    footerDividers = footerDividers2;
                                } else if (!headerDividers && (isHeader2 || start3 < headerCount)) {
                                    footerDividers = footerDividers2;
                                } else if (isFirstItem || (checkIsEnabled(adapter, start3) && (footerDividers2 || (!isFooter2 && start3 < footerLimit)))) {
                                    footerDividers = footerDividers2;
                                    bounds.top = top - dividerHeight;
                                    bounds.bottom = top;
                                    drawDivider(canvas, bounds, i3 - 1);
                                } else {
                                    footerDividers = footerDividers2;
                                }
                                if (fillForMissingDividers) {
                                    bounds.top = top - dividerHeight;
                                    bounds.bottom = top;
                                    canvas.drawRect(bounds, paint2);
                                }
                            } else {
                                footerDividers = footerDividers2;
                                effectivePaddingTop2 = effectivePaddingTop2;
                                start = start2;
                            }
                        } else {
                            footerDividers = footerDividers2;
                            overscrollFooter = overscrollFooter4;
                            start = start2;
                        }
                    } else {
                        footerDividers = footerDividers2;
                        first = first2;
                        overscrollFooter = overscrollFooter4;
                        start = start2;
                    }
                    i3++;
                    itemCount3 = itemCount3;
                    first2 = first;
                    overscrollFooter4 = overscrollFooter;
                    start2 = start;
                    footerDividers2 = footerDividers;
                }
                if (count > 0 && scrollY2 > 0) {
                    if (drawOverscrollFooter) {
                        int absListBottom = this.mBottom;
                        bounds.top = absListBottom;
                        bounds.bottom = absListBottom + scrollY2;
                        drawOverscrollFooter(canvas, overscrollFooter4, bounds);
                    } else if (drawDividers2) {
                        bounds.top = listBottom2;
                        bounds.bottom = listBottom2 + dividerHeight;
                        drawDivider(canvas, bounds, -1);
                    }
                }
            }
        }
        super.dispatchDraw(canvas);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
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
        if (needDrawThisDivider(childIndex, divider, true)) {
            divider.setBounds(bounds);
            divider.draw(canvas);
            needDrawThisDivider(childIndex, divider, false);
        }
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
    @Override // android.widget.AbsListView, android.view.View
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
    @Override // android.view.View
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
    @Override // android.view.ViewGroup, android.view.View
    public <T extends View> T findViewTraversal(int id) {
        T t = (T) super.findViewTraversal(id);
        if (t == null) {
            T t2 = (T) findViewInHeadersOrFooters(this.mHeaderViewInfos, id);
            if (t2 != null) {
                return t2;
            }
            t = (T) findViewInHeadersOrFooters(this.mFooterViewInfos, id);
            if (t != null) {
                return t;
            }
        }
        return t;
    }

    /* access modifiers changed from: package-private */
    public View findViewInHeadersOrFooters(ArrayList<FixedViewInfo> where, int id) {
        View v;
        if (where == null) {
            return null;
        }
        int len = where.size();
        for (int i = 0; i < len; i++) {
            View v2 = where.get(i).view;
            if (!(v2.isRootNamespace() || (v = v2.findViewById(id)) == null)) {
                return v;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public <T extends View> T findViewWithTagTraversal(Object tag) {
        T t = (T) super.findViewWithTagTraversal(tag);
        if (t == null) {
            T t2 = (T) findViewWithTagInHeadersOrFooters(this.mHeaderViewInfos, tag);
            if (t2 != null) {
                return t2;
            }
            t = (T) findViewWithTagInHeadersOrFooters(this.mFooterViewInfos, tag);
            if (t != null) {
                return t;
            }
        }
        return t;
    }

    /* access modifiers changed from: package-private */
    public View findViewWithTagInHeadersOrFooters(ArrayList<FixedViewInfo> where, Object tag) {
        View v;
        if (where == null) {
            return null;
        }
        int len = where.size();
        for (int i = 0; i < len; i++) {
            View v2 = where.get(i).view;
            if (!(v2.isRootNamespace() || (v = v2.findViewWithTag(tag)) == null)) {
                return v;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public <T extends View> T findViewByPredicateTraversal(Predicate<View> predicate, View childToSkip) {
        T t = (T) super.findViewByPredicateTraversal(predicate, childToSkip);
        if (t == null) {
            T t2 = (T) findViewByPredicateInHeadersOrFooters(this.mHeaderViewInfos, predicate, childToSkip);
            if (t2 != null) {
                return t2;
            }
            t = (T) findViewByPredicateInHeadersOrFooters(this.mFooterViewInfos, predicate, childToSkip);
            if (t != null) {
                return t;
            }
        }
        return t;
    }

    /* access modifiers changed from: package-private */
    public View findViewByPredicateInHeadersOrFooters(ArrayList<FixedViewInfo> where, Predicate<View> predicate, View childToSkip) {
        View v;
        if (where == null) {
            return null;
        }
        int len = where.size();
        for (int i = 0; i < len; i++) {
            View v2 = where.get(i).view;
            if (!(v2 == childToSkip || v2.isRootNamespace() || (v = v2.findViewByPredicate(predicate)) == null)) {
                return v;
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
    @Override // android.widget.AbsListView
    @UnsupportedAppUsage
    public int getHeightForPosition(int position) {
        int height = super.getHeightForPosition(position);
        if (shouldAdjustHeightForDivider(position)) {
            return this.mDividerHeight + height;
        }
        return height;
    }

    /* JADX INFO: Multiple debug info for r2v2 android.widget.ListAdapter: [D('dividerHeight' int), D('adapter' android.widget.ListAdapter)] */
    /* JADX INFO: Multiple debug info for r3v3 int: [D('previousIndex' int), D('start' int)] */
    /* JADX INFO: Multiple debug info for r4v4 int: [D('overscrollFooter' android.graphics.drawable.Drawable), D('nextIndex' int)] */
    private boolean shouldAdjustHeightForDivider(int itemIndex) {
        boolean z;
        boolean z2;
        int dividerHeight = this.mDividerHeight;
        Drawable overscrollHeader = this.mOverScrollHeader;
        Drawable overscrollFooter = this.mOverScrollFooter;
        boolean drawOverscrollHeader = overscrollHeader != null;
        boolean drawOverscrollFooter = overscrollFooter != null;
        if (!(dividerHeight > 0 && this.mDivider != null)) {
            return false;
        }
        boolean fillForMissingDividers = isOpaque() && !super.isOpaque();
        int itemCount = this.mItemCount;
        int headerCount = getHeaderViewsCount();
        int footerLimit = itemCount - this.mFooterViewInfos.size();
        boolean isHeader = itemIndex < headerCount;
        boolean isFooter = itemIndex >= footerLimit;
        boolean headerDividers = this.mHeaderDividersEnabled;
        boolean footerDividers = this.mFooterDividersEnabled;
        if ((!headerDividers && isHeader) || (!footerDividers && isFooter)) {
            return false;
        }
        ListAdapter adapter = this.mAdapter;
        if (!this.mStackFromBottom) {
            boolean isLastItem = itemIndex == itemCount + -1;
            if (drawOverscrollFooter && isLastItem) {
                return false;
            }
            int nextIndex = itemIndex + 1;
            if (!checkIsEnabled(adapter, itemIndex)) {
                z2 = true;
            } else if (!headerDividers && (isHeader || nextIndex < headerCount)) {
                z2 = true;
            } else if (isLastItem) {
                return true;
            } else {
                if (checkIsEnabled(adapter, nextIndex)) {
                    if (footerDividers) {
                        return true;
                    }
                    if (!isFooter && nextIndex < footerLimit) {
                        return true;
                    }
                }
                z2 = true;
            }
            if (fillForMissingDividers) {
                return z2;
            }
            return false;
        }
        boolean isFirstItem = itemIndex == (drawOverscrollHeader ? 1 : 0);
        if (isFirstItem) {
            return false;
        }
        int start = itemIndex - 1;
        if (!checkIsEnabled(adapter, itemIndex)) {
            z = true;
        } else if (!headerDividers && (isHeader || start < headerCount)) {
            z = true;
        } else if (isFirstItem) {
            return true;
        } else {
            if (checkIsEnabled(adapter, start)) {
                if (footerDividers) {
                    return true;
                }
                if (!isFooter && start < footerLimit) {
                    return true;
                }
            }
            z = true;
        }
        if (fillForMissingDividers) {
            return z;
        }
        return false;
    }

    @Override // android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return ListView.class.getName();
    }

    @Override // android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        int rowsCount = getCount();
        info.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(rowsCount, 1, false, getSelectionModeForAccessibility()));
        if (rowsCount > 0) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION);
        }
    }

    @Override // android.widget.AbsListView, android.view.View
    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (action != 16908343) {
            return false;
        }
        int row = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, -1);
        int position = Math.min(row, getCount() - 1);
        if (row < 0) {
            return false;
        }
        smoothScrollToPosition(position);
        return true;
    }

    @Override // android.widget.AbsListView
    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoForItem(view, position, info);
        AbsListView.LayoutParams lp = (AbsListView.LayoutParams) view.getLayoutParams();
        info.setCollectionItemInfo(AccessibilityNodeInfo.CollectionItemInfo.obtain(position, 1, 0, 1, lp != null && lp.viewType == -2, isItemChecked(position)));
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
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

    /* access modifiers changed from: protected */
    public boolean needDrawThisDivider(int childIndex, Drawable divider, boolean isBeforeDraw) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void setFirstPosition(int position) {
        if (this.mAdapter == null) {
            Log.w("listDeleteAnimation", "setFirstPosition: mAdapter is null. position " + position);
        } else if (position < 0 || position >= this.mAdapter.getCount()) {
            Log.w("listDeleteAnimation", "setFirstPosition: position is invalid. position " + position + ", ItemCount " + this.mAdapter.getCount());
        } else {
            this.mFirstPosition = position;
        }
    }
}
