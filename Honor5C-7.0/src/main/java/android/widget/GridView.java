package android.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.RemotableViewMethod;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.ViewRootImpl;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeInfo.CollectionInfo;
import android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.GridLayoutAnimationController.AnimationParameters;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView.LayoutParams;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;
import com.android.internal.os.HwBootFail;
import com.android.internal.telephony.RILConstants;
import com.android.internal.util.AsyncService;
import com.hisi.perfhub.PerfHub;
import com.huawei.android.statistical.StatisticalConstant;
import com.huawei.hwperformance.HwPerformance;
import com.huawei.pgmng.log.LogPower;
import huawei.cust.HwCfgFilePolicy;

@RemoteView
public class GridView extends AbsListView {
    public static final int AUTO_FIT = -1;
    public static final int NO_STRETCH = 0;
    public static final int STRETCH_COLUMN_WIDTH = 2;
    public static final int STRETCH_SPACING = 1;
    public static final int STRETCH_SPACING_UNIFORM = 3;
    private int mColumnWidth;
    private int mGravity;
    private int mHorizontalSpacing;
    private int mNumColumns;
    private View mReferenceView;
    private View mReferenceViewInSelectedRow;
    private int mRequestedColumnWidth;
    private int mRequestedHorizontalSpacing;
    private int mRequestedNumColumns;
    private int mStretchMode;
    private final Rect mTempRect;
    private int mVerticalSpacing;

    public GridView(Context context) {
        this(context, null);
    }

    public GridView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.gridViewStyle);
    }

    public GridView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, NO_STRETCH);
    }

    public GridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mNumColumns = AUTO_FIT;
        this.mHorizontalSpacing = NO_STRETCH;
        this.mVerticalSpacing = NO_STRETCH;
        this.mStretchMode = STRETCH_COLUMN_WIDTH;
        this.mReferenceView = null;
        this.mReferenceViewInSelectedRow = null;
        this.mGravity = Gravity.START;
        this.mTempRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridView, defStyleAttr, defStyleRes);
        setHorizontalSpacing(a.getDimensionPixelOffset(STRETCH_SPACING, NO_STRETCH));
        setVerticalSpacing(a.getDimensionPixelOffset(STRETCH_COLUMN_WIDTH, NO_STRETCH));
        int index = a.getInt(STRETCH_SPACING_UNIFORM, STRETCH_COLUMN_WIDTH);
        if (index >= 0) {
            setStretchMode(index);
        }
        int columnWidth = a.getDimensionPixelOffset(4, AUTO_FIT);
        if (columnWidth > 0) {
            setColumnWidth(columnWidth);
        }
        setNumColumns(a.getInt(5, STRETCH_SPACING));
        index = a.getInt(NO_STRETCH, AUTO_FIT);
        if (index >= 0) {
            setGravity(index);
        }
        a.recycle();
    }

    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    @RemotableViewMethod
    public void setRemoteViewsAdapter(Intent intent) {
        super.setRemoteViewsAdapter(intent);
    }

    public void setAdapter(ListAdapter adapter) {
        if (!(this.mAdapter == null || this.mDataSetObserver == null)) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }
        resetList();
        this.mRecycler.clear();
        this.mAdapter = adapter;
        this.mOldSelectedPosition = AUTO_FIT;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        super.setAdapter(adapter);
        if (this.mAdapter != null) {
            int position;
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
            this.mDataChanged = true;
            checkFocus();
            this.mDataSetObserver = new AdapterDataSetObserver();
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            this.mRecycler.setViewTypeCount(this.mAdapter.getViewTypeCount());
            if (this.mStackFromBottom) {
                position = lookForSelectablePosition(this.mItemCount + AUTO_FIT, false);
            } else {
                position = lookForSelectablePosition(NO_STRETCH, true);
            }
            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);
            checkSelectionChanged();
        } else {
            checkFocus();
            checkSelectionChanged();
        }
        requestLayout();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int lookForSelectablePosition(int position, boolean lookDown) {
        if (this.mAdapter == null || isInTouchMode() || position < 0 || position >= this.mItemCount) {
            return AUTO_FIT;
        }
        return position;
    }

    void fillGap(boolean down) {
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int count = getChildCount();
        int position;
        if (down) {
            int paddingTop = NO_STRETCH;
            if ((this.mGroupFlags & 34) == 34) {
                paddingTop = getListPaddingTop();
            }
            int startOffset = count > 0 ? getChildAt(count + AUTO_FIT).getBottom() + verticalSpacing : paddingTop;
            position = this.mFirstPosition + count;
            if (this.mStackFromBottom) {
                position += numColumns + AUTO_FIT;
            }
            fillDown(position, startOffset);
            correctTooHigh(numColumns, verticalSpacing, getChildCount());
            return;
        }
        int paddingBottom = NO_STRETCH;
        if ((this.mGroupFlags & 34) == 34) {
            paddingBottom = getListPaddingBottom();
        }
        startOffset = count > 0 ? getChildAt(NO_STRETCH).getTop() - verticalSpacing : getHeight() - paddingBottom;
        position = this.mFirstPosition;
        if (this.mStackFromBottom) {
            position += AUTO_FIT;
        } else {
            position -= numColumns;
        }
        fillUp(position, startOffset);
        correctTooLow(numColumns, verticalSpacing, getChildCount());
    }

    private View fillDown(int pos, int nextTop) {
        View selectedView = null;
        int end = this.mBottom - this.mTop;
        if ((this.mGroupFlags & 34) == 34) {
            end -= this.mListPadding.bottom;
        }
        while (nextTop < end && pos < this.mItemCount) {
            View temp = makeRow(pos, nextTop, true);
            if (temp != null) {
                selectedView = temp;
            }
            nextTop = this.mReferenceView.getBottom() + this.mVerticalSpacing;
            pos += this.mNumColumns;
        }
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) + AUTO_FIT);
        return selectedView;
    }

    private View makeRow(int startPos, int y, boolean flow) {
        int nextLeft;
        int last;
        int columnWidth = this.mColumnWidth;
        int horizontalSpacing = this.mHorizontalSpacing;
        boolean isLayoutRtl = isRtlLocale();
        if (isLayoutRtl) {
            nextLeft = ((getWidth() - this.mListPadding.right) - columnWidth) - (this.mStretchMode == STRETCH_SPACING_UNIFORM ? horizontalSpacing : NO_STRETCH);
        } else {
            nextLeft = this.mListPadding.left + (this.mStretchMode == STRETCH_SPACING_UNIFORM ? horizontalSpacing : NO_STRETCH);
        }
        if (this.mStackFromBottom) {
            last = startPos + STRETCH_SPACING;
            startPos = Math.max(NO_STRETCH, (startPos - this.mNumColumns) + STRETCH_SPACING);
            if (last - startPos < this.mNumColumns) {
                nextLeft += (isLayoutRtl ? AUTO_FIT : STRETCH_SPACING) * ((this.mNumColumns - (last - startPos)) * (columnWidth + horizontalSpacing));
            }
        } else {
            last = Math.min(this.mNumColumns + startPos, this.mItemCount);
        }
        View selectedView = null;
        boolean hasFocus = shouldShowSelector();
        boolean inClick = touchModeDrawsInPressedState();
        int selectedPosition = this.mSelectedPosition;
        View child = null;
        int nextChildDir = isLayoutRtl ? AUTO_FIT : STRETCH_SPACING;
        int pos = startPos;
        while (pos < last) {
            boolean selected = pos == selectedPosition;
            child = makeAndAddView(pos, y, flow, nextLeft, selected, flow ? AUTO_FIT : pos - startPos);
            nextLeft += nextChildDir * columnWidth;
            if (pos < last + AUTO_FIT) {
                nextLeft += nextChildDir * horizontalSpacing;
            }
            if (selected && (hasFocus || inClick)) {
                selectedView = child;
            }
            pos += STRETCH_SPACING;
        }
        this.mReferenceView = child;
        if (selectedView != null) {
            this.mReferenceViewInSelectedRow = this.mReferenceView;
        }
        return selectedView;
    }

    private View fillUp(int pos, int nextBottom) {
        View selectedView = null;
        int end = NO_STRETCH;
        if ((this.mGroupFlags & 34) == 34) {
            end = this.mListPadding.top;
        }
        while (nextBottom > end && pos >= 0) {
            View temp = makeRow(pos, nextBottom, false);
            if (temp != null) {
                selectedView = temp;
            }
            nextBottom = this.mReferenceView.getTop() - this.mVerticalSpacing;
            this.mFirstPosition = pos;
            pos -= this.mNumColumns;
        }
        if (this.mStackFromBottom) {
            this.mFirstPosition = Math.max(NO_STRETCH, pos + STRETCH_SPACING);
        }
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) + AUTO_FIT);
        return selectedView;
    }

    private View fillFromTop(int nextTop) {
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mSelectedPosition);
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mItemCount + AUTO_FIT);
        if (this.mFirstPosition < 0) {
            this.mFirstPosition = NO_STRETCH;
        }
        this.mFirstPosition -= this.mFirstPosition % this.mNumColumns;
        return fillDown(this.mFirstPosition, nextTop);
    }

    private View fillFromBottom(int lastPosition, int nextBottom) {
        int invertedPosition = (this.mItemCount + AUTO_FIT) - Math.min(Math.max(lastPosition, this.mSelectedPosition), this.mItemCount + AUTO_FIT);
        return fillUp((this.mItemCount + AUTO_FIT) - (invertedPosition - (invertedPosition % this.mNumColumns)), nextBottom);
    }

    private View fillSelection(int childrenTop, int childrenBottom) {
        int rowStart;
        int i;
        int selectedPosition = reconcileSelectedPosition();
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int rowEnd = AUTO_FIT;
        if (this.mStackFromBottom) {
            int invertedSelection = (this.mItemCount + AUTO_FIT) - selectedPosition;
            rowEnd = (this.mItemCount + AUTO_FIT) - (invertedSelection - (invertedSelection % numColumns));
            rowStart = Math.max(NO_STRETCH, (rowEnd - numColumns) + STRETCH_SPACING);
        } else {
            rowStart = selectedPosition - (selectedPosition % numColumns);
        }
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, rowStart);
        if (this.mStackFromBottom) {
            i = rowEnd;
        } else {
            i = rowStart;
        }
        View sel = makeRow(i, topSelectionPixel, true);
        this.mFirstPosition = rowStart;
        View referenceView = this.mReferenceView;
        if (this.mStackFromBottom) {
            offsetChildrenTopAndBottom(getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, rowStart) - referenceView.getBottom());
            fillUp(rowStart + AUTO_FIT, referenceView.getTop() - verticalSpacing);
            pinToTop(childrenTop);
            fillDown(rowEnd + numColumns, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
        } else {
            fillDown(rowStart + numColumns, referenceView.getBottom() + verticalSpacing);
            pinToBottom(childrenBottom);
            fillUp(rowStart - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
        }
        return sel;
    }

    private void pinToTop(int childrenTop) {
        if (this.mFirstPosition == 0) {
            int offset = childrenTop - getChildAt(NO_STRETCH).getTop();
            if (offset < 0) {
                offsetChildrenTopAndBottom(offset);
            }
        }
    }

    private void pinToBottom(int childrenBottom) {
        int count = getChildCount();
        if (this.mFirstPosition + count == this.mItemCount) {
            int offset = childrenBottom - getChildAt(count + AUTO_FIT).getBottom();
            if (offset > 0) {
                offsetChildrenTopAndBottom(offset);
            }
        }
    }

    int findMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int numColumns = this.mNumColumns;
            int i;
            if (this.mStackFromBottom) {
                for (i = childCount + AUTO_FIT; i >= 0; i -= numColumns) {
                    if (y >= getChildAt(i).getTop()) {
                        return this.mFirstPosition + i;
                    }
                }
            } else {
                for (i = NO_STRETCH; i < childCount; i += numColumns) {
                    if (y <= getChildAt(i).getBottom()) {
                        return this.mFirstPosition + i;
                    }
                }
            }
        }
        return AUTO_FIT;
    }

    private View fillSpecific(int position, int top) {
        int motionRowStart;
        int i;
        int numColumns = this.mNumColumns;
        int motionRowEnd = AUTO_FIT;
        if (this.mStackFromBottom) {
            int invertedSelection = (this.mItemCount + AUTO_FIT) - position;
            motionRowEnd = (this.mItemCount + AUTO_FIT) - (invertedSelection - (invertedSelection % numColumns));
            motionRowStart = Math.max(NO_STRETCH, (motionRowEnd - numColumns) + STRETCH_SPACING);
        } else {
            motionRowStart = position - (position % numColumns);
        }
        if (this.mStackFromBottom) {
            i = motionRowEnd;
        } else {
            i = motionRowStart;
        }
        View temp = makeRow(i, top, true);
        this.mFirstPosition = motionRowStart;
        View referenceView = this.mReferenceView;
        if (referenceView == null) {
            return null;
        }
        View below;
        View above;
        int verticalSpacing = this.mVerticalSpacing;
        int childCount;
        if (this.mStackFromBottom) {
            below = fillDown(motionRowEnd + numColumns, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            above = fillUp(motionRowStart + AUTO_FIT, referenceView.getTop() - verticalSpacing);
            childCount = getChildCount();
            if (childCount > 0) {
                correctTooLow(numColumns, verticalSpacing, childCount);
            }
        } else {
            above = fillUp(motionRowStart - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            below = fillDown(motionRowStart + numColumns, referenceView.getBottom() + verticalSpacing);
            childCount = getChildCount();
            if (childCount > 0) {
                correctTooHigh(numColumns, verticalSpacing, childCount);
            }
        }
        if (temp != null) {
            return temp;
        }
        if (above != null) {
            return above;
        }
        return below;
    }

    private void correctTooHigh(int numColumns, int verticalSpacing, int childCount) {
        if ((this.mFirstPosition + childCount) + AUTO_FIT == this.mItemCount + AUTO_FIT && childCount > 0) {
            int bottomOffset = ((this.mBottom - this.mTop) - this.mListPadding.bottom) - getChildAt(childCount + AUTO_FIT).getBottom();
            View firstChild = getChildAt(NO_STRETCH);
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
                    int i = this.mFirstPosition;
                    if (this.mStackFromBottom) {
                        numColumns = STRETCH_SPACING;
                    }
                    fillUp(i - numColumns, firstChild.getTop() - verticalSpacing);
                    adjustViewsUpOrDown();
                }
            }
        }
    }

    private void correctTooLow(int numColumns, int verticalSpacing, int childCount) {
        if (this.mFirstPosition == 0 && childCount > 0) {
            int end = (this.mBottom - this.mTop) - this.mListPadding.bottom;
            int topOffset = getChildAt(NO_STRETCH).getTop() - this.mListPadding.top;
            View lastChild = getChildAt(childCount + AUTO_FIT);
            int lastBottom = lastChild.getBottom();
            int lastPosition = (this.mFirstPosition + childCount) + AUTO_FIT;
            if (topOffset <= 0) {
                return;
            }
            if (lastPosition < this.mItemCount + AUTO_FIT || lastBottom > end) {
                if (lastPosition == this.mItemCount + AUTO_FIT) {
                    topOffset = Math.min(topOffset, lastBottom - end);
                }
                offsetChildrenTopAndBottom(-topOffset);
                if (lastPosition < this.mItemCount + AUTO_FIT) {
                    if (!this.mStackFromBottom) {
                        numColumns = STRETCH_SPACING;
                    }
                    fillDown(lastPosition + numColumns, lastChild.getBottom() + verticalSpacing);
                    adjustViewsUpOrDown();
                }
            }
        }
    }

    private View fillFromSelection(int selectedTop, int childrenTop, int childrenBottom) {
        int rowStart;
        int i;
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int rowEnd = AUTO_FIT;
        if (this.mStackFromBottom) {
            int invertedSelection = (this.mItemCount + AUTO_FIT) - selectedPosition;
            rowEnd = (this.mItemCount + AUTO_FIT) - (invertedSelection - (invertedSelection % numColumns));
            rowStart = Math.max(NO_STRETCH, (rowEnd - numColumns) + STRETCH_SPACING);
        } else {
            rowStart = selectedPosition - (selectedPosition % numColumns);
        }
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, rowStart);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, rowStart);
        if (this.mStackFromBottom) {
            i = rowEnd;
        } else {
            i = rowStart;
        }
        View sel = makeRow(i, selectedTop, true);
        this.mFirstPosition = rowStart;
        View referenceView = this.mReferenceView;
        adjustForTopFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        adjustForBottomFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        if (this.mStackFromBottom) {
            fillDown(rowEnd + numColumns, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            fillUp(rowStart + AUTO_FIT, referenceView.getTop() - verticalSpacing);
        } else {
            fillUp(rowStart - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            fillDown(rowStart + numColumns, referenceView.getBottom() + verticalSpacing);
        }
        return sel;
    }

    private int getBottomSelectionPixel(int childrenBottom, int fadingEdgeLength, int numColumns, int rowStart) {
        int bottomSelectionPixel = childrenBottom;
        if ((rowStart + numColumns) + AUTO_FIT < this.mItemCount + AUTO_FIT) {
            return childrenBottom - fadingEdgeLength;
        }
        return bottomSelectionPixel;
    }

    private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength, int rowStart) {
        int topSelectionPixel = childrenTop;
        if (rowStart > 0) {
            return childrenTop + fadingEdgeLength;
        }
        return topSelectionPixel;
    }

    private void adjustForBottomFadingEdge(View childInSelectedRow, int topSelectionPixel, int bottomSelectionPixel) {
        if (childInSelectedRow.getBottom() > bottomSelectionPixel) {
            offsetChildrenTopAndBottom(-Math.min(childInSelectedRow.getTop() - topSelectionPixel, childInSelectedRow.getBottom() - bottomSelectionPixel));
        }
    }

    private void adjustForTopFadingEdge(View childInSelectedRow, int topSelectionPixel, int bottomSelectionPixel) {
        if (childInSelectedRow.getTop() < topSelectionPixel) {
            offsetChildrenTopAndBottom(Math.min(topSelectionPixel - childInSelectedRow.getTop(), bottomSelectionPixel - childInSelectedRow.getBottom()));
        }
    }

    @RemotableViewMethod
    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position);
    }

    @RemotableViewMethod
    public void smoothScrollByOffset(int offset) {
        super.smoothScrollByOffset(offset);
    }

    private View moveSelection(int delta, int childrenTop, int childrenBottom) {
        int rowStart;
        int oldRowStart;
        View sel;
        View referenceView;
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int rowEnd = AUTO_FIT;
        if (this.mStackFromBottom) {
            int invertedSelection = (this.mItemCount + AUTO_FIT) - selectedPosition;
            rowEnd = (this.mItemCount + AUTO_FIT) - (invertedSelection - (invertedSelection % numColumns));
            rowStart = Math.max(NO_STRETCH, (rowEnd - numColumns) + STRETCH_SPACING);
            invertedSelection = (this.mItemCount + AUTO_FIT) - (selectedPosition - delta);
            oldRowStart = Math.max(NO_STRETCH, (((this.mItemCount + AUTO_FIT) - (invertedSelection - (invertedSelection % numColumns))) - numColumns) + STRETCH_SPACING);
        } else {
            oldRowStart = (selectedPosition - delta) - ((selectedPosition - delta) % numColumns);
            rowStart = selectedPosition - (selectedPosition % numColumns);
        }
        int rowDelta = rowStart - oldRowStart;
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, rowStart);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, rowStart);
        this.mFirstPosition = rowStart;
        int i;
        if (rowDelta > 0) {
            int oldBottom;
            if (this.mReferenceViewInSelectedRow == null) {
                oldBottom = NO_STRETCH;
            } else {
                oldBottom = this.mReferenceViewInSelectedRow.getBottom();
            }
            if (this.mStackFromBottom) {
                i = rowEnd;
            } else {
                i = rowStart;
            }
            sel = makeRow(i, oldBottom + verticalSpacing, true);
            referenceView = this.mReferenceView;
            adjustForBottomFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        } else if (rowDelta < 0) {
            if (this.mReferenceViewInSelectedRow == null) {
                oldTop = NO_STRETCH;
            } else {
                oldTop = this.mReferenceViewInSelectedRow.getTop();
            }
            if (this.mStackFromBottom) {
                i = rowEnd;
            } else {
                i = rowStart;
            }
            sel = makeRow(i, oldTop - verticalSpacing, false);
            referenceView = this.mReferenceView;
            adjustForTopFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        } else {
            if (this.mReferenceViewInSelectedRow == null) {
                oldTop = NO_STRETCH;
            } else {
                oldTop = this.mReferenceViewInSelectedRow.getTop();
            }
            if (this.mStackFromBottom) {
                i = rowEnd;
            } else {
                i = rowStart;
            }
            sel = makeRow(i, oldTop, true);
            referenceView = this.mReferenceView;
        }
        if (this.mStackFromBottom) {
            fillDown(rowEnd + numColumns, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            fillUp(rowStart + AUTO_FIT, referenceView.getTop() - verticalSpacing);
        } else {
            fillUp(rowStart - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            fillDown(rowStart + numColumns, referenceView.getBottom() + verticalSpacing);
        }
        return sel;
    }

    private boolean determineColumns(int availableSpace) {
        int requestedHorizontalSpacing = this.mRequestedHorizontalSpacing;
        int stretchMode = this.mStretchMode;
        int requestedColumnWidth = this.mRequestedColumnWidth;
        boolean didNotInitiallyFit = false;
        if (this.mRequestedNumColumns != AUTO_FIT) {
            this.mNumColumns = this.mRequestedNumColumns;
        } else if (requestedColumnWidth > 0) {
            this.mNumColumns = (availableSpace + requestedHorizontalSpacing) / (requestedColumnWidth + requestedHorizontalSpacing);
        } else {
            this.mNumColumns = STRETCH_COLUMN_WIDTH;
        }
        if (this.mNumColumns <= 0) {
            this.mNumColumns = STRETCH_SPACING;
        }
        switch (stretchMode) {
            case NO_STRETCH /*0*/:
                this.mColumnWidth = requestedColumnWidth;
                this.mHorizontalSpacing = requestedHorizontalSpacing;
                break;
            default:
                int spaceLeftOver = (availableSpace - (this.mNumColumns * requestedColumnWidth)) - ((this.mNumColumns + AUTO_FIT) * requestedHorizontalSpacing);
                if (spaceLeftOver < 0) {
                    didNotInitiallyFit = true;
                }
                switch (stretchMode) {
                    case STRETCH_SPACING /*1*/:
                        this.mColumnWidth = requestedColumnWidth;
                        if (this.mNumColumns <= STRETCH_SPACING) {
                            this.mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                            break;
                        }
                        this.mHorizontalSpacing = (spaceLeftOver / (this.mNumColumns + AUTO_FIT)) + requestedHorizontalSpacing;
                        break;
                    case STRETCH_COLUMN_WIDTH /*2*/:
                        this.mColumnWidth = (spaceLeftOver / this.mNumColumns) + requestedColumnWidth;
                        this.mHorizontalSpacing = requestedHorizontalSpacing;
                        break;
                    case STRETCH_SPACING_UNIFORM /*3*/:
                        this.mColumnWidth = requestedColumnWidth;
                        if (this.mNumColumns <= STRETCH_SPACING) {
                            this.mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                            break;
                        }
                        this.mHorizontalSpacing = (spaceLeftOver / (this.mNumColumns + STRETCH_SPACING)) + requestedHorizontalSpacing;
                        break;
                }
                break;
        }
        return didNotInitiallyFit;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == 0) {
            if (this.mColumnWidth > 0) {
                i = this.mColumnWidth;
                int i2 = this.mListPadding.left;
                widthSize = (r0 + r0) + this.mListPadding.right;
            } else {
                widthSize = this.mListPadding.left + this.mListPadding.right;
            }
            widthSize += getVerticalScrollbarWidth();
        }
        i = this.mListPadding.left;
        boolean didNotInitiallyFit = determineColumns((widthSize - r0) - this.mListPadding.right);
        int childHeight = NO_STRETCH;
        if (this.mAdapter == null) {
            i = NO_STRETCH;
        } else {
            i = this.mAdapter.getCount();
        }
        this.mItemCount = i;
        int count = this.mItemCount;
        if (count > 0) {
            View child = obtainView(NO_STRETCH, this.mIsScrap);
            LayoutParams p = (LayoutParams) child.getLayoutParams();
            if (p == null) {
                p = (LayoutParams) generateDefaultLayoutParams();
                child.setLayoutParams(p);
            }
            p.viewType = this.mAdapter.getItemViewType(NO_STRETCH);
            p.isEnabled = this.mAdapter.isEnabled(NO_STRETCH);
            p.forceAdd = true;
            child.measure(ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(this.mColumnWidth, EditorInfo.IME_FLAG_NO_ENTER_ACTION), NO_STRETCH, p.width), ViewGroup.getChildMeasureSpec(MeasureSpec.makeSafeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), NO_STRETCH), NO_STRETCH, p.height));
            childHeight = child.getMeasuredHeight();
            int childState = View.combineMeasuredStates(NO_STRETCH, child.getMeasuredState());
            if (this.mRecycler.shouldRecycleViewType(p.viewType)) {
                this.mRecycler.addScrapView(child, AUTO_FIT);
            }
        }
        if (heightMode == 0) {
            heightSize = ((this.mListPadding.top + this.mListPadding.bottom) + childHeight) + (getVerticalFadingEdgeLength() * STRETCH_COLUMN_WIDTH);
        }
        if (heightMode == Integer.MIN_VALUE) {
            int ourSize = this.mListPadding.top + this.mListPadding.bottom;
            int numColumns = this.mNumColumns;
            for (int i3 = NO_STRETCH; i3 < count; i3 += numColumns) {
                ourSize += childHeight;
                if (i3 + numColumns < count) {
                    ourSize += this.mVerticalSpacing;
                }
                if (ourSize >= heightSize) {
                    ourSize = heightSize;
                    break;
                }
            }
            heightSize = ourSize;
        }
        if (widthMode == Integer.MIN_VALUE) {
            i = this.mRequestedNumColumns;
            if (r0 != AUTO_FIT) {
                i = this.mRequestedNumColumns;
                i2 = this.mColumnWidth;
                i2 = this.mRequestedNumColumns;
                int i4 = this.mHorizontalSpacing;
                i2 = this.mListPadding.left;
                if ((((r0 * r0) + ((r0 + AUTO_FIT) * r0)) + r0) + this.mListPadding.right > widthSize || didNotInitiallyFit) {
                    widthSize |= AsyncService.CMD_ASYNC_SERVICE_DESTROY;
                }
            }
        }
        setMeasuredDimension(widthSize, heightSize);
        this.mWidthMeasureSpec = widthMeasureSpec;
    }

    protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
        AnimationParameters animationParams = params.layoutAnimationParameters;
        if (animationParams == null) {
            animationParams = new AnimationParameters();
            params.layoutAnimationParameters = animationParams;
        }
        animationParams.count = count;
        animationParams.index = index;
        animationParams.columnsCount = this.mNumColumns;
        animationParams.rowsCount = count / this.mNumColumns;
        if (this.mStackFromBottom) {
            int invertedIndex = (count + AUTO_FIT) - index;
            animationParams.column = (this.mNumColumns + AUTO_FIT) - (invertedIndex % this.mNumColumns);
            animationParams.row = (animationParams.rowsCount + AUTO_FIT) - (invertedIndex / this.mNumColumns);
            return;
        }
        animationParams.column = index % this.mNumColumns;
        animationParams.row = index / this.mNumColumns;
    }

    protected void layoutChildren() {
        boolean blockLayoutRequests = this.mBlockLayoutRequests;
        if (!blockLayoutRequests) {
            this.mBlockLayoutRequests = true;
        }
        try {
            super.layoutChildren();
            invalidate();
            if (this.mAdapter == null) {
                resetList();
                invokeOnItemScrollListener();
                return;
            }
            boolean dataChanged;
            AccessibilityNodeInfo accessibilityNodeInfo;
            View view;
            int accessibilityFocusPosition;
            ViewRootImpl viewRootImpl;
            View focusHost;
            View focusChild;
            int firstPosition;
            RecycleBin recycleBin;
            int i;
            View sel;
            int i2;
            int last;
            boolean inTouchMode;
            View child;
            View restoreView;
            int childrenTop = this.mListPadding.top;
            int childrenBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
            int childCount = getChildCount();
            int delta = NO_STRETCH;
            View view2 = null;
            View oldFirst = null;
            View newSel = null;
            int index;
            switch (this.mLayoutMode) {
                case STRETCH_COLUMN_WIDTH /*2*/:
                    index = this.mNextSelectedPosition - this.mFirstPosition;
                    if (index >= 0 && index < childCount) {
                        newSel = getChildAt(index);
                    }
                case STRETCH_SPACING /*1*/:
                case STRETCH_SPACING_UNIFORM /*3*/:
                case HwCfgFilePolicy.CUST /*4*/:
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    dataChanged = this.mDataChanged;
                    if (dataChanged) {
                        handleDataChanged();
                    }
                    if (this.mItemCount == 0) {
                        resetList();
                        invokeOnItemScrollListener();
                        if (!blockLayoutRequests) {
                            this.mBlockLayoutRequests = false;
                        }
                        return;
                    }
                    setSelectedPositionInt(this.mNextSelectedPosition);
                    accessibilityNodeInfo = null;
                    view = null;
                    accessibilityFocusPosition = AUTO_FIT;
                    viewRootImpl = getViewRootImpl();
                    if (viewRootImpl != null) {
                        focusHost = viewRootImpl.getAccessibilityFocusedHost();
                        if (focusHost != null) {
                            focusChild = getAccessibilityFocusedChild(focusHost);
                            if (focusChild != null) {
                                if (!dataChanged || focusChild.hasTransientState() || this.mAdapterHasStableIds) {
                                    view = focusHost;
                                    accessibilityNodeInfo = viewRootImpl.getAccessibilityFocusedVirtualView();
                                }
                                accessibilityFocusPosition = getPositionForView(focusChild);
                            }
                        }
                    }
                    firstPosition = this.mFirstPosition;
                    recycleBin = this.mRecycler;
                    if (dataChanged) {
                        for (i = NO_STRETCH; i < childCount; i += STRETCH_SPACING) {
                            recycleBin.addScrapView(getChildAt(i), firstPosition + i);
                        }
                    } else {
                        recycleBin.fillActiveViews(childCount, firstPosition);
                    }
                    detachAllViewsFromParent();
                    recycleBin.removeSkippedScrap();
                    switch (this.mLayoutMode) {
                        case STRETCH_SPACING /*1*/:
                            this.mFirstPosition = NO_STRETCH;
                            sel = fillFromTop(childrenTop);
                            adjustViewsUpOrDown();
                            break;
                        case STRETCH_COLUMN_WIDTH /*2*/:
                            if (newSel == null) {
                                sel = fillSelection(childrenTop, childrenBottom);
                                break;
                            } else {
                                sel = fillFromSelection(newSel.getTop(), childrenTop, childrenBottom);
                                break;
                            }
                        case STRETCH_SPACING_UNIFORM /*3*/:
                            sel = fillUp(this.mItemCount + AUTO_FIT, childrenBottom);
                            adjustViewsUpOrDown();
                            break;
                        case HwCfgFilePolicy.CUST /*4*/:
                            sel = fillSpecific(this.mSelectedPosition, this.mSpecificTop);
                            break;
                        case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                            sel = fillSpecific(this.mSyncPosition, this.mSpecificTop);
                            break;
                        case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                            sel = moveSelection(delta, childrenTop, childrenBottom);
                            break;
                        default:
                            if (childCount == 0) {
                                if (this.mSelectedPosition < 0 && this.mSelectedPosition < this.mItemCount) {
                                    i2 = this.mSelectedPosition;
                                    if (view2 != null) {
                                        childrenTop = view2.getTop();
                                    }
                                    sel = fillSpecific(i2, childrenTop);
                                    break;
                                }
                                if (this.mFirstPosition >= this.mItemCount) {
                                    sel = fillSpecific(NO_STRETCH, childrenTop);
                                    break;
                                }
                                i2 = this.mFirstPosition;
                                if (oldFirst != null) {
                                    childrenTop = oldFirst.getTop();
                                }
                                sel = fillSpecific(i2, childrenTop);
                                break;
                            } else if (!this.mStackFromBottom) {
                                i2 = (this.mAdapter != null || isInTouchMode()) ? AUTO_FIT : NO_STRETCH;
                                setSelectedPositionInt(i2);
                                sel = fillFromTop(childrenTop);
                                break;
                            } else {
                                last = this.mItemCount + AUTO_FIT;
                                if (this.mAdapter != null || isInTouchMode()) {
                                    i2 = AUTO_FIT;
                                } else {
                                    i2 = last;
                                }
                                setSelectedPositionInt(i2);
                                sel = fillFromBottom(last, childrenBottom);
                                break;
                            }
                            break;
                    }
                    recycleBin.scrapActiveViews();
                    if (sel == null) {
                        positionSelector(AUTO_FIT, sel);
                        this.mSelectedTop = sel.getTop();
                    } else {
                        if (this.mTouchMode > 0) {
                            i2 = this.mTouchMode;
                            inTouchMode = r0 < STRETCH_SPACING_UNIFORM;
                        } else {
                            inTouchMode = false;
                        }
                        if (!inTouchMode) {
                            child = getChildAt(this.mMotionPosition - this.mFirstPosition);
                            if (child != null) {
                                positionSelector(this.mMotionPosition, child);
                            }
                        } else {
                            i2 = this.mSelectedPosition;
                            if (r0 == AUTO_FIT) {
                                child = getChildAt(this.mSelectorPosition - this.mFirstPosition);
                                if (child != null) {
                                    positionSelector(this.mSelectorPosition, child);
                                }
                            } else {
                                this.mSelectedTop = NO_STRETCH;
                                this.mSelectorRect.setEmpty();
                            }
                        }
                    }
                    if (viewRootImpl != null && viewRootImpl.getAccessibilityFocusedHost() == null) {
                        if (view == null && view.isAttachedToWindow()) {
                            AccessibilityNodeProvider provider = view.getAccessibilityNodeProvider();
                            if (accessibilityNodeInfo == null || provider == null) {
                                view.requestAccessibilityFocus();
                            } else {
                                provider.performAction(AccessibilityNodeInfo.getVirtualDescendantId(accessibilityNodeInfo.getSourceNodeId()), 64, null);
                            }
                        } else if (accessibilityFocusPosition != AUTO_FIT) {
                            restoreView = getChildAt(MathUtils.constrain(accessibilityFocusPosition - this.mFirstPosition, NO_STRETCH, getChildCount() + AUTO_FIT));
                            if (restoreView != null) {
                                restoreView.requestAccessibilityFocus();
                            }
                        }
                    }
                    this.mLayoutMode = NO_STRETCH;
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
                    if (!blockLayoutRequests) {
                        this.mBlockLayoutRequests = false;
                    }
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                    if (this.mNextSelectedPosition >= 0) {
                        delta = this.mNextSelectedPosition - this.mSelectedPosition;
                    }
                    dataChanged = this.mDataChanged;
                    if (dataChanged) {
                        handleDataChanged();
                    }
                    if (this.mItemCount == 0) {
                        setSelectedPositionInt(this.mNextSelectedPosition);
                        accessibilityNodeInfo = null;
                        view = null;
                        accessibilityFocusPosition = AUTO_FIT;
                        viewRootImpl = getViewRootImpl();
                        if (viewRootImpl != null) {
                            focusHost = viewRootImpl.getAccessibilityFocusedHost();
                            if (focusHost != null) {
                                focusChild = getAccessibilityFocusedChild(focusHost);
                                if (focusChild != null) {
                                    view = focusHost;
                                    accessibilityNodeInfo = viewRootImpl.getAccessibilityFocusedVirtualView();
                                    accessibilityFocusPosition = getPositionForView(focusChild);
                                }
                            }
                        }
                        firstPosition = this.mFirstPosition;
                        recycleBin = this.mRecycler;
                        if (dataChanged) {
                            recycleBin.fillActiveViews(childCount, firstPosition);
                        } else {
                            for (i = NO_STRETCH; i < childCount; i += STRETCH_SPACING) {
                                recycleBin.addScrapView(getChildAt(i), firstPosition + i);
                            }
                        }
                        detachAllViewsFromParent();
                        recycleBin.removeSkippedScrap();
                        switch (this.mLayoutMode) {
                            case STRETCH_SPACING /*1*/:
                                this.mFirstPosition = NO_STRETCH;
                                sel = fillFromTop(childrenTop);
                                adjustViewsUpOrDown();
                                break;
                            case STRETCH_COLUMN_WIDTH /*2*/:
                                if (newSel == null) {
                                    sel = fillFromSelection(newSel.getTop(), childrenTop, childrenBottom);
                                    break;
                                } else {
                                    sel = fillSelection(childrenTop, childrenBottom);
                                    break;
                                }
                            case STRETCH_SPACING_UNIFORM /*3*/:
                                sel = fillUp(this.mItemCount + AUTO_FIT, childrenBottom);
                                adjustViewsUpOrDown();
                                break;
                            case HwCfgFilePolicy.CUST /*4*/:
                                sel = fillSpecific(this.mSelectedPosition, this.mSpecificTop);
                                break;
                            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                                sel = fillSpecific(this.mSyncPosition, this.mSpecificTop);
                                break;
                            case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                                sel = moveSelection(delta, childrenTop, childrenBottom);
                                break;
                            default:
                                if (childCount == 0) {
                                    if (!this.mStackFromBottom) {
                                        last = this.mItemCount + AUTO_FIT;
                                        if (this.mAdapter != null) {
                                            break;
                                        }
                                        i2 = AUTO_FIT;
                                        setSelectedPositionInt(i2);
                                        sel = fillFromBottom(last, childrenBottom);
                                        break;
                                    }
                                    if (this.mAdapter != null) {
                                        break;
                                    }
                                    setSelectedPositionInt(i2);
                                    sel = fillFromTop(childrenTop);
                                    break;
                                }
                                if (this.mSelectedPosition < 0) {
                                    break;
                                }
                                if (this.mFirstPosition >= this.mItemCount) {
                                    i2 = this.mFirstPosition;
                                    if (oldFirst != null) {
                                        childrenTop = oldFirst.getTop();
                                    }
                                    sel = fillSpecific(i2, childrenTop);
                                    break;
                                }
                                sel = fillSpecific(NO_STRETCH, childrenTop);
                                break;
                        }
                        recycleBin.scrapActiveViews();
                        if (sel == null) {
                            positionSelector(AUTO_FIT, sel);
                            this.mSelectedTop = sel.getTop();
                            break;
                        }
                        if (this.mTouchMode > 0) {
                            inTouchMode = false;
                        } else {
                            i2 = this.mTouchMode;
                            if (r0 < STRETCH_SPACING_UNIFORM) {
                            }
                        }
                        if (!inTouchMode) {
                            child = getChildAt(this.mMotionPosition - this.mFirstPosition);
                            if (child != null) {
                                positionSelector(this.mMotionPosition, child);
                                break;
                            }
                        }
                        i2 = this.mSelectedPosition;
                        if (r0 == AUTO_FIT) {
                            child = getChildAt(this.mSelectorPosition - this.mFirstPosition);
                            if (child != null) {
                                positionSelector(this.mSelectorPosition, child);
                                break;
                            }
                        }
                        this.mSelectedTop = NO_STRETCH;
                        this.mSelectorRect.setEmpty();
                        break;
                        if (view == null) {
                            break;
                        }
                        if (accessibilityFocusPosition != AUTO_FIT) {
                            restoreView = getChildAt(MathUtils.constrain(accessibilityFocusPosition - this.mFirstPosition, NO_STRETCH, getChildCount() + AUTO_FIT));
                            if (restoreView != null) {
                                restoreView.requestAccessibilityFocus();
                            }
                        }
                        this.mLayoutMode = NO_STRETCH;
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
                            this.mBlockLayoutRequests = false;
                        }
                    }
                    resetList();
                    invokeOnItemScrollListener();
                    if (blockLayoutRequests) {
                        this.mBlockLayoutRequests = false;
                    }
                    return;
                default:
                    index = this.mSelectedPosition - this.mFirstPosition;
                    if (index >= 0 && index < childCount) {
                        view2 = getChildAt(index);
                    }
                    oldFirst = getChildAt(NO_STRETCH);
            }
            dataChanged = this.mDataChanged;
            if (dataChanged) {
                handleDataChanged();
            }
            if (this.mItemCount == 0) {
                resetList();
                invokeOnItemScrollListener();
                if (blockLayoutRequests) {
                    this.mBlockLayoutRequests = false;
                }
                return;
            }
            setSelectedPositionInt(this.mNextSelectedPosition);
            accessibilityNodeInfo = null;
            view = null;
            accessibilityFocusPosition = AUTO_FIT;
            viewRootImpl = getViewRootImpl();
            if (viewRootImpl != null) {
                focusHost = viewRootImpl.getAccessibilityFocusedHost();
                if (focusHost != null) {
                    focusChild = getAccessibilityFocusedChild(focusHost);
                    if (focusChild != null) {
                        view = focusHost;
                        accessibilityNodeInfo = viewRootImpl.getAccessibilityFocusedVirtualView();
                        accessibilityFocusPosition = getPositionForView(focusChild);
                    }
                }
            }
            firstPosition = this.mFirstPosition;
            recycleBin = this.mRecycler;
            if (dataChanged) {
                for (i = NO_STRETCH; i < childCount; i += STRETCH_SPACING) {
                    recycleBin.addScrapView(getChildAt(i), firstPosition + i);
                }
            } else {
                recycleBin.fillActiveViews(childCount, firstPosition);
            }
            detachAllViewsFromParent();
            recycleBin.removeSkippedScrap();
            switch (this.mLayoutMode) {
                case STRETCH_SPACING /*1*/:
                    this.mFirstPosition = NO_STRETCH;
                    sel = fillFromTop(childrenTop);
                    adjustViewsUpOrDown();
                    break;
                case STRETCH_COLUMN_WIDTH /*2*/:
                    if (newSel == null) {
                        sel = fillSelection(childrenTop, childrenBottom);
                        break;
                    } else {
                        sel = fillFromSelection(newSel.getTop(), childrenTop, childrenBottom);
                        break;
                    }
                case STRETCH_SPACING_UNIFORM /*3*/:
                    sel = fillUp(this.mItemCount + AUTO_FIT, childrenBottom);
                    adjustViewsUpOrDown();
                    break;
                case HwCfgFilePolicy.CUST /*4*/:
                    sel = fillSpecific(this.mSelectedPosition, this.mSpecificTop);
                    break;
                case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                    sel = fillSpecific(this.mSyncPosition, this.mSpecificTop);
                    break;
                case HwCfgFilePolicy.CLOUD_DPLMN /*6*/:
                    sel = moveSelection(delta, childrenTop, childrenBottom);
                    break;
                default:
                    if (childCount == 0) {
                        if (!this.mStackFromBottom) {
                            if (this.mAdapter != null) {
                                break;
                            }
                            setSelectedPositionInt(i2);
                            sel = fillFromTop(childrenTop);
                            break;
                        }
                        last = this.mItemCount + AUTO_FIT;
                        if (this.mAdapter != null) {
                            break;
                        }
                        i2 = AUTO_FIT;
                        setSelectedPositionInt(i2);
                        sel = fillFromBottom(last, childrenBottom);
                        break;
                    }
                    if (this.mSelectedPosition < 0) {
                        break;
                    }
                    if (this.mFirstPosition >= this.mItemCount) {
                        sel = fillSpecific(NO_STRETCH, childrenTop);
                        break;
                    }
                    i2 = this.mFirstPosition;
                    if (oldFirst != null) {
                        childrenTop = oldFirst.getTop();
                    }
                    sel = fillSpecific(i2, childrenTop);
                    break;
            }
            recycleBin.scrapActiveViews();
            if (sel == null) {
                positionSelector(AUTO_FIT, sel);
                this.mSelectedTop = sel.getTop();
            } else {
                if (this.mTouchMode > 0) {
                    i2 = this.mTouchMode;
                    if (r0 < STRETCH_SPACING_UNIFORM) {
                    }
                } else {
                    inTouchMode = false;
                }
                if (!inTouchMode) {
                    child = getChildAt(this.mMotionPosition - this.mFirstPosition);
                    if (child != null) {
                        positionSelector(this.mMotionPosition, child);
                    }
                } else {
                    i2 = this.mSelectedPosition;
                    if (r0 == AUTO_FIT) {
                        child = getChildAt(this.mSelectorPosition - this.mFirstPosition);
                        if (child != null) {
                            positionSelector(this.mSelectorPosition, child);
                        }
                    } else {
                        this.mSelectedTop = NO_STRETCH;
                        this.mSelectorRect.setEmpty();
                    }
                }
            }
            if (view == null) {
            }
            if (accessibilityFocusPosition != AUTO_FIT) {
                restoreView = getChildAt(MathUtils.constrain(accessibilityFocusPosition - this.mFirstPosition, NO_STRETCH, getChildCount() + AUTO_FIT));
                if (restoreView != null) {
                    restoreView.requestAccessibilityFocus();
                }
            }
            this.mLayoutMode = NO_STRETCH;
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
                this.mBlockLayoutRequests = false;
            }
        } finally {
            if (!blockLayoutRequests) {
                this.mBlockLayoutRequests = false;
            }
        }
    }

    private View makeAndAddView(int position, int y, boolean flow, int childrenLeft, boolean selected, int where) {
        View child;
        if (!this.mDataChanged) {
            child = this.mRecycler.getActiveView(position);
            if (child != null) {
                setupChild(child, position, y, flow, childrenLeft, selected, true, where);
                return child;
            }
        }
        child = obtainView(position, this.mIsScrap);
        setupChild(child, position, y, flow, childrenLeft, selected, this.mIsScrap[NO_STRETCH], where);
        return child;
    }

    private void setupChild(View child, int position, int y, boolean flow, int childrenLeft, boolean selected, boolean recycled, int where) {
        boolean isPressed;
        int childLeft;
        Trace.traceBegin(8, "setupGridItem");
        boolean shouldShowSelector = selected ? shouldShowSelector() : false;
        boolean updateChildSelected = shouldShowSelector != child.isSelected();
        int mode = this.mTouchMode;
        if (mode <= 0 || mode >= STRETCH_SPACING_UNIFORM) {
            isPressed = false;
        } else {
            int i = this.mMotionPosition;
            isPressed = r0 == position;
        }
        boolean updateChildPressed = isPressed != child.isPressed();
        boolean isLayoutRequested = (!recycled || updateChildSelected) ? true : child.isLayoutRequested();
        ViewGroup.LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) generateDefaultLayoutParams();
        }
        p.viewType = this.mAdapter.getItemViewType(position);
        p.isEnabled = this.mAdapter.isEnabled(position);
        if (!recycled || p.forceAdd) {
            p.forceAdd = false;
            addViewInLayout(child, where, p, true);
        } else {
            attachViewToParent(child, where, p);
        }
        if (updateChildSelected) {
            child.setSelected(shouldShowSelector);
            if (shouldShowSelector) {
                requestFocus();
            }
        }
        if (updateChildPressed) {
            child.setPressed(isPressed);
        }
        if (!(this.mChoiceMode == 0 || this.mCheckStates == null)) {
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(this.mCheckStates.get(position));
            } else {
                i = getContext().getApplicationInfo().targetSdkVersion;
                if (r0 >= 11) {
                    child.setActivated(this.mCheckStates.get(position));
                }
            }
        }
        if (isLayoutRequested) {
            child.measure(ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(this.mColumnWidth, EditorInfo.IME_FLAG_NO_ENTER_ACTION), NO_STRETCH, p.width), ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(NO_STRETCH, NO_STRETCH), NO_STRETCH, p.height));
        } else {
            cleanupLayoutState(child);
        }
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childTop = flow ? y : y - h;
        switch (Gravity.getAbsoluteGravity(this.mGravity, getLayoutDirection()) & 7) {
            case STRETCH_SPACING /*1*/:
                childLeft = childrenLeft + ((this.mColumnWidth - w) / STRETCH_COLUMN_WIDTH);
                break;
            case STRETCH_SPACING_UNIFORM /*3*/:
                childLeft = childrenLeft;
                break;
            case HwCfgFilePolicy.CLOUD_MCC /*5*/:
                childLeft = (this.mColumnWidth + childrenLeft) - w;
                break;
            default:
                childLeft = childrenLeft;
                break;
        }
        if (isLayoutRequested) {
            child.layout(childLeft, childTop, childLeft + w, childTop + h);
        } else {
            child.offsetLeftAndRight(childLeft - child.getLeft());
            child.offsetTopAndBottom(childTop - child.getTop());
        }
        if (this.mCachingStarted) {
            child.setDrawingCacheEnabled(true);
        }
        if (recycled) {
            i = ((LayoutParams) child.getLayoutParams()).scrappedFromPosition;
            if (r0 != position) {
                child.jumpDrawablesToCurrentState();
            }
        }
        Trace.traceEnd(8);
    }

    public void setSelection(int position) {
        if (isInTouchMode()) {
            this.mResurrectToPosition = position;
        } else {
            setNextSelectedPositionInt(position);
        }
        this.mLayoutMode = STRETCH_COLUMN_WIDTH;
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        requestLayout();
    }

    void setSelectionInt(int position) {
        int next;
        int previous;
        int previousSelectedPosition = this.mNextSelectedPosition;
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        setNextSelectedPositionInt(position);
        layoutChildren();
        if (this.mStackFromBottom) {
            next = (this.mItemCount + AUTO_FIT) - this.mNextSelectedPosition;
        } else {
            next = this.mNextSelectedPosition;
        }
        if (this.mStackFromBottom) {
            previous = (this.mItemCount + AUTO_FIT) - previousSelectedPosition;
        } else {
            previous = previousSelectedPosition;
        }
        if (next / this.mNumColumns != previous / this.mNumColumns) {
            awakenScrollBars();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return commonKey(keyCode, STRETCH_SPACING, event);
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return commonKey(keyCode, repeatCount, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return commonKey(keyCode, STRETCH_SPACING, event);
    }

    private boolean commonKey(int keyCode, int count, KeyEvent event) {
        if (this.mAdapter == null) {
            return false;
        }
        if (this.mDataChanged) {
            layoutChildren();
        }
        boolean z = false;
        int action = event.getAction();
        if (KeyEvent.isConfirmKey(keyCode) && event.hasNoModifiers() && action != STRETCH_SPACING) {
            z = resurrectSelectionIfNeeded();
            if (!z && event.getRepeatCount() == 0 && getChildCount() > 0) {
                keyPressed();
                z = true;
            }
        }
        if (!(z || action == STRETCH_SPACING)) {
            switch (keyCode) {
                case PerfHub.PERF_TAG_IPA_SUSTAINABLE_POWER /*19*/:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(STRETCH_COLUMN_WIDTH)) {
                            if (!resurrectSelectionIfNeeded()) {
                                z = fullScroll(33);
                                break;
                            }
                            z = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        z = arrowScroll(33);
                        break;
                    } else {
                        z = true;
                        break;
                    }
                    break;
                case HwPerformance.PERF_TAG_TASK_FORK_ON_B_CLUSTER /*20*/:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(STRETCH_COLUMN_WIDTH)) {
                            if (!resurrectSelectionIfNeeded()) {
                                z = fullScroll(LogPower.END_CHG_ROTATION);
                                break;
                            }
                            z = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        z = arrowScroll(LogPower.END_CHG_ROTATION);
                        break;
                    } else {
                        z = true;
                        break;
                    }
                    break;
                case HwPerformance.PERF_TAG_DEF_L_CPU_MIN /*21*/:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            z = arrowScroll(17);
                            break;
                        }
                        z = true;
                        break;
                    }
                    break;
                case HwPerformance.PERF_TAG_DEF_L_CPU_MAX /*22*/:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            z = arrowScroll(66);
                            break;
                        }
                        z = true;
                        break;
                    }
                    break;
                case RILConstants.RIL_REQUEST_CDMA_GET_BROADCAST_CONFIG /*92*/:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(STRETCH_COLUMN_WIDTH)) {
                            if (!resurrectSelectionIfNeeded()) {
                                z = fullScroll(33);
                                break;
                            }
                            z = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        z = pageScroll(33);
                        break;
                    } else {
                        z = true;
                        break;
                    }
                    break;
                case RILConstants.RIL_REQUEST_CDMA_SET_BROADCAST_CONFIG /*93*/:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(STRETCH_COLUMN_WIDTH)) {
                            if (!resurrectSelectionIfNeeded()) {
                                z = fullScroll(LogPower.END_CHG_ROTATION);
                                break;
                            }
                            z = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        z = pageScroll(LogPower.END_CHG_ROTATION);
                        break;
                    } else {
                        z = true;
                        break;
                    }
                    break;
                case LogPower.NOTIFICATION_ENQUEUE /*122*/:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            z = fullScroll(33);
                            break;
                        }
                        z = true;
                        break;
                    }
                    break;
                case LogPower.NOTIFICATION_CANCEL /*123*/:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            z = fullScroll(LogPower.END_CHG_ROTATION);
                            break;
                        }
                        z = true;
                        break;
                    }
                    break;
            }
        }
        if (z || sendToTextFilter(keyCode, count, event)) {
            return true;
        }
        switch (action) {
            case NO_STRETCH /*0*/:
                return super.onKeyDown(keyCode, event);
            case STRETCH_SPACING /*1*/:
                return super.onKeyUp(keyCode, event);
            case STRETCH_COLUMN_WIDTH /*2*/:
                return super.onKeyMultiple(keyCode, count, event);
            default:
                return false;
        }
    }

    boolean pageScroll(int direction) {
        int nextPage = AUTO_FIT;
        if (direction == 33) {
            nextPage = Math.max(NO_STRETCH, this.mSelectedPosition - getChildCount());
        } else if (direction == LogPower.END_CHG_ROTATION) {
            nextPage = Math.min(this.mItemCount + AUTO_FIT, this.mSelectedPosition + getChildCount());
        }
        if (nextPage < 0) {
            return false;
        }
        setSelectionInt(nextPage);
        invokeOnItemScrollListener();
        awakenScrollBars();
        return true;
    }

    boolean fullScroll(int direction) {
        boolean moved = false;
        if (direction == 33) {
            this.mLayoutMode = STRETCH_COLUMN_WIDTH;
            setSelectionInt(NO_STRETCH);
            invokeOnItemScrollListener();
            moved = true;
        } else if (direction == LogPower.END_CHG_ROTATION) {
            this.mLayoutMode = STRETCH_COLUMN_WIDTH;
            setSelectionInt(this.mItemCount + AUTO_FIT);
            invokeOnItemScrollListener();
            moved = true;
        }
        if (moved) {
            awakenScrollBars();
        }
        return moved;
    }

    boolean arrowScroll(int direction) {
        int endOfRowPos;
        int startOfRowPos;
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        boolean moved = false;
        if (this.mStackFromBottom) {
            endOfRowPos = (this.mItemCount + AUTO_FIT) - ((((this.mItemCount + AUTO_FIT) - selectedPosition) / numColumns) * numColumns);
            startOfRowPos = Math.max(NO_STRETCH, (endOfRowPos - numColumns) + STRETCH_SPACING);
        } else {
            startOfRowPos = (selectedPosition / numColumns) * numColumns;
            endOfRowPos = Math.min((startOfRowPos + numColumns) + AUTO_FIT, this.mItemCount + AUTO_FIT);
        }
        switch (direction) {
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                if (startOfRowPos > 0) {
                    this.mLayoutMode = 6;
                    setSelectionInt(Math.max(NO_STRETCH, selectedPosition - numColumns));
                    moved = true;
                    break;
                }
                break;
            case LogPower.END_CHG_ROTATION /*130*/:
                if (endOfRowPos < this.mItemCount + AUTO_FIT) {
                    this.mLayoutMode = 6;
                    setSelectionInt(Math.min(selectedPosition + numColumns, this.mItemCount + AUTO_FIT));
                    moved = true;
                    break;
                }
                break;
        }
        boolean isLayoutRtl = isRtlLocale();
        if (selectedPosition > startOfRowPos && ((direction == 17 && !isLayoutRtl) || (direction == 66 && isLayoutRtl))) {
            this.mLayoutMode = 6;
            setSelectionInt(Math.max(NO_STRETCH, selectedPosition + AUTO_FIT));
            moved = true;
        } else if (selectedPosition < endOfRowPos && ((direction == 17 && isLayoutRtl) || (direction == 66 && !isLayoutRtl))) {
            this.mLayoutMode = 6;
            setSelectionInt(Math.min(selectedPosition + STRETCH_SPACING, this.mItemCount + AUTO_FIT));
            moved = true;
        }
        if (moved) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
            invokeOnItemScrollListener();
        }
        if (moved) {
            awakenScrollBars();
        }
        return moved;
    }

    boolean sequenceScroll(int direction) {
        int endOfRow;
        int startOfRow;
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        int count = this.mItemCount;
        if (this.mStackFromBottom) {
            endOfRow = (count + AUTO_FIT) - ((((count + AUTO_FIT) - selectedPosition) / numColumns) * numColumns);
            startOfRow = Math.max(NO_STRETCH, (endOfRow - numColumns) + STRETCH_SPACING);
        } else {
            startOfRow = (selectedPosition / numColumns) * numColumns;
            endOfRow = Math.min((startOfRow + numColumns) + AUTO_FIT, count + AUTO_FIT);
        }
        boolean moved = false;
        boolean showScroll = false;
        switch (direction) {
            case STRETCH_SPACING /*1*/:
                if (selectedPosition > 0) {
                    this.mLayoutMode = 6;
                    setSelectionInt(selectedPosition + AUTO_FIT);
                    moved = true;
                    if (selectedPosition != startOfRow) {
                        showScroll = false;
                        break;
                    }
                    showScroll = true;
                    break;
                }
                break;
            case STRETCH_COLUMN_WIDTH /*2*/:
                if (selectedPosition < count + AUTO_FIT) {
                    this.mLayoutMode = 6;
                    setSelectionInt(selectedPosition + STRETCH_SPACING);
                    moved = true;
                    if (selectedPosition != endOfRow) {
                        showScroll = false;
                        break;
                    }
                    showScroll = true;
                    break;
                }
                break;
        }
        if (moved) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
            invokeOnItemScrollListener();
        }
        if (showScroll) {
            awakenScrollBars();
        }
        return moved;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        int closestChildIndex = AUTO_FIT;
        if (gainFocus && previouslyFocusedRect != null) {
            previouslyFocusedRect.offset(this.mScrollX, this.mScrollY);
            Rect otherRect = this.mTempRect;
            int minDistance = HwBootFail.STAGE_BOOT_SUCCESS;
            int childCount = getChildCount();
            for (int i = NO_STRETCH; i < childCount; i += STRETCH_SPACING) {
                if (isCandidateSelection(i, direction)) {
                    View other = getChildAt(i);
                    other.getDrawingRect(otherRect);
                    offsetDescendantRectToMyCoords(other, otherRect);
                    int distance = AbsListView.getDistance(previouslyFocusedRect, otherRect, direction);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestChildIndex = i;
                    }
                }
            }
        }
        if (closestChildIndex >= 0) {
            setSelection(this.mFirstPosition + closestChildIndex);
        } else {
            requestLayout();
        }
    }

    private boolean isCandidateSelection(int childIndex, int direction) {
        int rowEnd;
        int rowStart;
        boolean z = true;
        boolean z2 = false;
        int count = getChildCount();
        int invertedIndex = (count + AUTO_FIT) - childIndex;
        if (this.mStackFromBottom) {
            rowEnd = (count + AUTO_FIT) - (invertedIndex - (invertedIndex % this.mNumColumns));
            rowStart = Math.max(NO_STRETCH, (rowEnd - this.mNumColumns) + STRETCH_SPACING);
        } else {
            rowStart = childIndex - (childIndex % this.mNumColumns);
            rowEnd = Math.max((this.mNumColumns + rowStart) + AUTO_FIT, count);
        }
        switch (direction) {
            case STRETCH_SPACING /*1*/:
                if (childIndex == rowEnd && rowEnd == count + AUTO_FIT) {
                    z2 = true;
                }
                return z2;
            case STRETCH_COLUMN_WIDTH /*2*/:
                if (childIndex == rowStart && rowStart == 0) {
                    z2 = true;
                }
                return z2;
            case StatisticalConstant.TYPE_TRIKEY_RIGHT_RECENT /*17*/:
                if (childIndex != rowEnd) {
                    z = false;
                }
                return z;
            case StatisticalConstant.TYPE_MULTIWINDOW_BUBBLE_MENU_CLOSE /*33*/:
                if (rowEnd != count + AUTO_FIT) {
                    z = false;
                }
                return z;
            case RILConstants.RIL_REQUEST_QUERY_AVAILABLE_BAND_MODE /*66*/:
                if (childIndex != rowStart) {
                    z = false;
                }
                return z;
            case LogPower.END_CHG_ROTATION /*130*/:
                if (rowStart != 0) {
                    z = false;
                }
                return z;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
        }
    }

    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            this.mGravity = gravity;
            requestLayoutIfNecessary();
        }
    }

    public int getGravity() {
        return this.mGravity;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        if (horizontalSpacing != this.mRequestedHorizontalSpacing) {
            this.mRequestedHorizontalSpacing = horizontalSpacing;
            requestLayoutIfNecessary();
        }
    }

    public int getHorizontalSpacing() {
        return this.mHorizontalSpacing;
    }

    public int getRequestedHorizontalSpacing() {
        return this.mRequestedHorizontalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        if (verticalSpacing != this.mVerticalSpacing) {
            this.mVerticalSpacing = verticalSpacing;
            requestLayoutIfNecessary();
        }
    }

    public int getVerticalSpacing() {
        return this.mVerticalSpacing;
    }

    public void setStretchMode(int stretchMode) {
        if (stretchMode != this.mStretchMode) {
            this.mStretchMode = stretchMode;
            requestLayoutIfNecessary();
        }
    }

    public int getStretchMode() {
        return this.mStretchMode;
    }

    public void setColumnWidth(int columnWidth) {
        if (columnWidth != this.mRequestedColumnWidth) {
            this.mRequestedColumnWidth = columnWidth;
            requestLayoutIfNecessary();
        }
    }

    public int getColumnWidth() {
        return this.mColumnWidth;
    }

    public int getRequestedColumnWidth() {
        return this.mRequestedColumnWidth;
    }

    public void setNumColumns(int numColumns) {
        if (numColumns != this.mRequestedNumColumns) {
            this.mRequestedNumColumns = numColumns;
            requestLayoutIfNecessary();
        }
    }

    @ExportedProperty
    public int getNumColumns() {
        return this.mNumColumns;
    }

    private void adjustViewsUpOrDown() {
        int childCount = getChildCount();
        if (childCount > 0) {
            int delta;
            if (this.mStackFromBottom) {
                delta = getChildAt(childCount + AUTO_FIT).getBottom() - (getHeight() - this.mListPadding.bottom);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta += this.mVerticalSpacing;
                }
                if (delta > 0) {
                    delta = NO_STRETCH;
                }
            } else {
                delta = getChildAt(NO_STRETCH).getTop() - this.mListPadding.top;
                if (this.mFirstPosition != 0) {
                    delta -= this.mVerticalSpacing;
                }
                if (delta < 0) {
                    delta = NO_STRETCH;
                }
            }
            if (delta != 0) {
                offsetChildrenTopAndBottom(-delta);
            }
        }
    }

    protected int computeVerticalScrollExtent() {
        int count = getChildCount();
        if (count <= 0) {
            return NO_STRETCH;
        }
        int numColumns = this.mNumColumns;
        int extent = (((count + numColumns) + AUTO_FIT) / numColumns) * 100;
        View view = getChildAt(NO_STRETCH);
        int top = view.getTop();
        int height = view.getHeight();
        if (height > 0) {
            extent += (top * 100) / height;
        }
        view = getChildAt(count + AUTO_FIT);
        int bottom = view.getBottom();
        height = view.getHeight();
        if (height > 0) {
            extent -= ((bottom - getHeight()) * 100) / height;
        }
        return extent;
    }

    protected int computeVerticalScrollOffset() {
        if (this.mFirstPosition >= 0 && getChildCount() > 0) {
            View view = getChildAt(NO_STRETCH);
            int top = view.getTop();
            int height = view.getHeight();
            if (height > 0) {
                int oddItemsOnFirstRow;
                int numColumns = this.mNumColumns;
                int rowCount = ((this.mItemCount + numColumns) + AUTO_FIT) / numColumns;
                if (isStackFromBottom()) {
                    oddItemsOnFirstRow = (rowCount * numColumns) - this.mItemCount;
                } else {
                    oddItemsOnFirstRow = NO_STRETCH;
                }
                return Math.max(((((this.mFirstPosition + oddItemsOnFirstRow) / numColumns) * 100) - ((top * 100) / height)) + ((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) rowCount)) * 100.0f)), NO_STRETCH);
            }
        }
        return NO_STRETCH;
    }

    protected int computeVerticalScrollRange() {
        int numColumns = this.mNumColumns;
        int rowCount = ((this.mItemCount + numColumns) + AUTO_FIT) / numColumns;
        int result = Math.max(rowCount * 100, NO_STRETCH);
        if (this.mScrollY != 0) {
            return result + Math.abs((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) rowCount)) * 100.0f));
        }
        return result;
    }

    public CharSequence getAccessibilityClassName() {
        return GridView.class.getName();
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        int columnsCount = getNumColumns();
        int rowsCount = getCount() / columnsCount;
        info.setCollectionInfo(CollectionInfo.obtain(rowsCount, columnsCount, false, getSelectionModeForAccessibility()));
        if (columnsCount > 0 || rowsCount > 0) {
            info.addAction(AccessibilityAction.ACTION_SCROLL_TO_POSITION);
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        switch (action) {
            case R.id.accessibilityActionScrollToPosition /*16908343*/:
                int numColumns = getNumColumns();
                int row = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, AUTO_FIT);
                int position = Math.min(row * numColumns, getCount() + AUTO_FIT);
                if (row >= 0) {
                    smoothScrollToPosition(position);
                    return true;
                }
                break;
        }
        return false;
    }

    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        int column;
        int row;
        super.onInitializeAccessibilityNodeInfoForItem(view, position, info);
        int count = getCount();
        int columnsCount = getNumColumns();
        int rowsCount = count / columnsCount;
        if (this.mStackFromBottom) {
            int invertedIndex = (count + AUTO_FIT) - position;
            column = (columnsCount + AUTO_FIT) - (invertedIndex % columnsCount);
            row = (rowsCount + AUTO_FIT) - (invertedIndex / columnsCount);
        } else {
            column = position % columnsCount;
            row = position / columnsCount;
        }
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        boolean isHeading = lp != null && lp.viewType == -2;
        info.setCollectionItemInfo(CollectionItemInfo.obtain(row, STRETCH_SPACING, column, STRETCH_SPACING, isHeading, isItemChecked(position)));
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("numColumns", getNumColumns());
    }
}
