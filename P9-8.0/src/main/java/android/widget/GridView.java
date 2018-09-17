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
import android.widget.AbsListView.LayoutParams;
import android.widget.RemoteViews.RemoteView;
import com.android.internal.R;

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
        this(context, attrs, defStyleAttr, 0);
    }

    public GridView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mNumColumns = -1;
        this.mHorizontalSpacing = 0;
        this.mVerticalSpacing = 0;
        this.mStretchMode = 2;
        this.mReferenceView = null;
        this.mReferenceViewInSelectedRow = null;
        this.mGravity = Gravity.START;
        this.mTempRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridView, defStyleAttr, defStyleRes);
        setHorizontalSpacing(a.getDimensionPixelOffset(1, 0));
        setVerticalSpacing(a.getDimensionPixelOffset(2, 0));
        int index = a.getInt(3, 2);
        if (index >= 0) {
            setStretchMode(index);
        }
        int columnWidth = a.getDimensionPixelOffset(4, -1);
        if (columnWidth > 0) {
            setColumnWidth(columnWidth);
        }
        setNumColumns(a.getInt(5, 1));
        index = a.getInt(0, -1);
        if (index >= 0) {
            setGravity(index);
        }
        a.recycle();
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
        this.mAdapter = adapter;
        this.mOldSelectedPosition = -1;
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
                position = lookForSelectablePosition(this.mItemCount - 1, false);
            } else {
                position = lookForSelectablePosition(0, true);
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

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int lookForSelectablePosition(int position, boolean lookDown) {
        if (this.mAdapter == null || isInTouchMode() || position < 0 || position >= this.mItemCount) {
            return -1;
        }
        return position;
    }

    void fillGap(boolean down) {
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int count = getChildCount();
        int startOffset;
        int position;
        if (down) {
            int paddingTop = 0;
            if ((this.mGroupFlags & 34) == 34) {
                paddingTop = getListPaddingTop();
            }
            startOffset = count > 0 ? getChildAt(count - 1).getBottom() + verticalSpacing : paddingTop;
            position = this.mFirstPosition + count;
            if (this.mStackFromBottom) {
                position += numColumns - 1;
            }
            fillDown(position, startOffset);
            correctTooHigh(numColumns, verticalSpacing, getChildCount());
            return;
        }
        int paddingBottom = 0;
        if ((this.mGroupFlags & 34) == 34) {
            paddingBottom = getListPaddingBottom();
        }
        startOffset = count > 0 ? getChildAt(0).getTop() - verticalSpacing : getHeight() - paddingBottom;
        position = this.mFirstPosition;
        if (this.mStackFromBottom) {
            position--;
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
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
        return selectedView;
    }

    private View makeRow(int startPos, int y, boolean flow) {
        int nextLeft;
        int last;
        int columnWidth = this.mColumnWidth;
        int horizontalSpacing = this.mHorizontalSpacing;
        boolean isLayoutRtl = isRtlLocale();
        if (isLayoutRtl) {
            nextLeft = ((getWidth() - this.mListPadding.right) - columnWidth) - (this.mStretchMode == 3 ? horizontalSpacing : 0);
        } else {
            nextLeft = this.mListPadding.left + (this.mStretchMode == 3 ? horizontalSpacing : 0);
        }
        if (this.mStackFromBottom) {
            last = startPos + 1;
            startPos = Math.max(0, (startPos - this.mNumColumns) + 1);
            if (last - startPos < this.mNumColumns) {
                nextLeft += (isLayoutRtl ? -1 : 1) * ((this.mNumColumns - (last - startPos)) * (columnWidth + horizontalSpacing));
            }
        } else {
            last = Math.min(this.mNumColumns + startPos, this.mItemCount);
        }
        View selectedView = null;
        boolean hasFocus = shouldShowSelector();
        boolean inClick = touchModeDrawsInPressedState();
        int selectedPosition = this.mSelectedPosition;
        View child = null;
        int nextChildDir = isLayoutRtl ? -1 : 1;
        int pos = startPos;
        while (pos < last) {
            boolean selected = pos == selectedPosition;
            child = makeAndAddView(pos, y, flow, nextLeft, selected, flow ? -1 : pos - startPos);
            nextLeft += nextChildDir * columnWidth;
            if (pos < last - 1) {
                nextLeft += nextChildDir * horizontalSpacing;
            }
            if (selected && (hasFocus || inClick)) {
                selectedView = child;
            }
            pos++;
        }
        this.mReferenceView = child;
        if (selectedView != null) {
            this.mReferenceViewInSelectedRow = this.mReferenceView;
        }
        return selectedView;
    }

    private View fillUp(int pos, int nextBottom) {
        View selectedView = null;
        int end = 0;
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
            this.mFirstPosition = Math.max(0, pos + 1);
        }
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
        return selectedView;
    }

    private View fillFromTop(int nextTop) {
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mSelectedPosition);
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mItemCount - 1);
        if (this.mFirstPosition < 0) {
            this.mFirstPosition = 0;
        }
        this.mFirstPosition -= this.mFirstPosition % this.mNumColumns;
        return fillDown(this.mFirstPosition, nextTop);
    }

    private View fillFromBottom(int lastPosition, int nextBottom) {
        int invertedPosition = (this.mItemCount - 1) - Math.min(Math.max(lastPosition, this.mSelectedPosition), this.mItemCount - 1);
        return fillUp((this.mItemCount - 1) - (invertedPosition - (invertedPosition % this.mNumColumns)), nextBottom);
    }

    private View fillSelection(int childrenTop, int childrenBottom) {
        int rowStart;
        int i;
        int selectedPosition = reconcileSelectedPosition();
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int rowEnd = -1;
        if (this.mStackFromBottom) {
            int invertedSelection = (this.mItemCount - 1) - selectedPosition;
            rowEnd = (this.mItemCount - 1) - (invertedSelection - (invertedSelection % numColumns));
            rowStart = Math.max(0, (rowEnd - numColumns) + 1);
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
            fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
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
            int offset = childrenTop - getChildAt(0).getTop();
            if (offset < 0) {
                offsetChildrenTopAndBottom(offset);
            }
        }
    }

    private void pinToBottom(int childrenBottom) {
        int count = getChildCount();
        if (this.mFirstPosition + count == this.mItemCount) {
            int offset = childrenBottom - getChildAt(count - 1).getBottom();
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
                for (i = childCount - 1; i >= 0; i -= numColumns) {
                    if (y >= getChildAt(i).getTop()) {
                        return this.mFirstPosition + i;
                    }
                }
            } else {
                for (i = 0; i < childCount; i += numColumns) {
                    if (y <= getChildAt(i).getBottom()) {
                        return this.mFirstPosition + i;
                    }
                }
            }
        }
        return -1;
    }

    private View fillSpecific(int position, int top) {
        int motionRowStart;
        int i;
        int numColumns = this.mNumColumns;
        int motionRowEnd = -1;
        if (this.mStackFromBottom) {
            int invertedSelection = (this.mItemCount - 1) - position;
            motionRowEnd = (this.mItemCount - 1) - (invertedSelection - (invertedSelection % numColumns));
            motionRowStart = Math.max(0, (motionRowEnd - numColumns) + 1);
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
            above = fillUp(motionRowStart - 1, referenceView.getTop() - verticalSpacing);
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
                    int i = this.mFirstPosition;
                    if (this.mStackFromBottom) {
                        numColumns = 1;
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
                    if (!this.mStackFromBottom) {
                        numColumns = 1;
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
        int rowEnd = -1;
        if (this.mStackFromBottom) {
            int invertedSelection = (this.mItemCount - 1) - selectedPosition;
            rowEnd = (this.mItemCount - 1) - (invertedSelection - (invertedSelection % numColumns));
            rowStart = Math.max(0, (rowEnd - numColumns) + 1);
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
            fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
        } else {
            fillUp(rowStart - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            fillDown(rowStart + numColumns, referenceView.getBottom() + verticalSpacing);
        }
        return sel;
    }

    private int getBottomSelectionPixel(int childrenBottom, int fadingEdgeLength, int numColumns, int rowStart) {
        int bottomSelectionPixel = childrenBottom;
        if ((rowStart + numColumns) - 1 < this.mItemCount - 1) {
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
        int rowEnd = -1;
        if (this.mStackFromBottom) {
            int invertedSelection = (this.mItemCount - 1) - selectedPosition;
            rowEnd = (this.mItemCount - 1) - (invertedSelection - (invertedSelection % numColumns));
            rowStart = Math.max(0, (rowEnd - numColumns) + 1);
            invertedSelection = (this.mItemCount - 1) - (selectedPosition - delta);
            oldRowStart = Math.max(0, (((this.mItemCount - 1) - (invertedSelection - (invertedSelection % numColumns))) - numColumns) + 1);
        } else {
            oldRowStart = (selectedPosition - delta) - ((selectedPosition - delta) % numColumns);
            rowStart = selectedPosition - (selectedPosition % numColumns);
        }
        int rowDelta = rowStart - oldRowStart;
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, rowStart);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, rowStart);
        this.mFirstPosition = rowStart;
        int i;
        int oldTop;
        if (rowDelta > 0) {
            int oldBottom;
            if (this.mReferenceViewInSelectedRow == null) {
                oldBottom = 0;
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
            oldTop = this.mReferenceViewInSelectedRow == null ? 0 : this.mReferenceViewInSelectedRow.getTop();
            if (this.mStackFromBottom) {
                i = rowEnd;
            } else {
                i = rowStart;
            }
            sel = makeRow(i, oldTop - verticalSpacing, false);
            referenceView = this.mReferenceView;
            adjustForTopFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        } else {
            oldTop = this.mReferenceViewInSelectedRow == null ? 0 : this.mReferenceViewInSelectedRow.getTop();
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
            fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
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
        if (this.mRequestedNumColumns != -1) {
            this.mNumColumns = this.mRequestedNumColumns;
        } else if (requestedColumnWidth > 0) {
            this.mNumColumns = (availableSpace + requestedHorizontalSpacing) / (requestedColumnWidth + requestedHorizontalSpacing);
        } else {
            this.mNumColumns = 2;
        }
        if (this.mNumColumns <= 0) {
            this.mNumColumns = 1;
        }
        switch (stretchMode) {
            case 0:
                this.mColumnWidth = requestedColumnWidth;
                this.mHorizontalSpacing = requestedHorizontalSpacing;
                break;
            default:
                int spaceLeftOver = (availableSpace - (this.mNumColumns * requestedColumnWidth)) - ((this.mNumColumns - 1) * requestedHorizontalSpacing);
                if (spaceLeftOver < 0) {
                    didNotInitiallyFit = true;
                }
                switch (stretchMode) {
                    case 1:
                        this.mColumnWidth = requestedColumnWidth;
                        if (this.mNumColumns <= 1) {
                            this.mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                            break;
                        }
                        this.mHorizontalSpacing = (spaceLeftOver / (this.mNumColumns - 1)) + requestedHorizontalSpacing;
                        break;
                    case 2:
                        this.mColumnWidth = (spaceLeftOver / this.mNumColumns) + requestedColumnWidth;
                        this.mHorizontalSpacing = requestedHorizontalSpacing;
                        break;
                    case 3:
                        this.mColumnWidth = requestedColumnWidth;
                        if (this.mNumColumns <= 1) {
                            this.mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                            break;
                        }
                        this.mHorizontalSpacing = (spaceLeftOver / (this.mNumColumns + 1)) + requestedHorizontalSpacing;
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
                widthSize = (this.mColumnWidth + this.mListPadding.left) + this.mListPadding.right;
            } else {
                widthSize = this.mListPadding.left + this.mListPadding.right;
            }
            widthSize += getVerticalScrollbarWidth();
        }
        boolean didNotInitiallyFit = determineColumns((widthSize - this.mListPadding.left) - this.mListPadding.right);
        int childHeight = 0;
        if (this.mAdapter == null) {
            i = 0;
        } else {
            i = this.mAdapter.getCount();
        }
        this.mItemCount = i;
        int count = this.mItemCount;
        if (count > 0) {
            View child = obtainView(0, this.mIsScrap);
            LayoutParams p = (LayoutParams) child.getLayoutParams();
            if (p == null) {
                p = (LayoutParams) generateDefaultLayoutParams();
                child.-wrap18(p);
            }
            p.viewType = this.mAdapter.getItemViewType(0);
            p.isEnabled = this.mAdapter.isEnabled(0);
            p.forceAdd = true;
            child.measure(ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(this.mColumnWidth, 1073741824), 0, p.width), ViewGroup.getChildMeasureSpec(MeasureSpec.makeSafeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), 0), 0, p.height));
            childHeight = child.getMeasuredHeight();
            int childState = View.combineMeasuredStates(0, child.getMeasuredState());
            if (this.mRecycler.shouldRecycleViewType(p.viewType)) {
                this.mRecycler.addScrapView(child, -1);
            }
        }
        if (heightMode == 0) {
            heightSize = ((this.mListPadding.top + this.mListPadding.bottom) + childHeight) + (getVerticalFadingEdgeLength() * 2);
        }
        if (heightMode == Integer.MIN_VALUE) {
            int ourSize = this.mListPadding.top + this.mListPadding.bottom;
            int numColumns = this.mNumColumns;
            for (int i2 = 0; i2 < count; i2 += numColumns) {
                ourSize += childHeight;
                if (i2 + numColumns < count) {
                    ourSize += this.mVerticalSpacing;
                }
                if (ourSize >= heightSize) {
                    ourSize = heightSize;
                    break;
                }
            }
            heightSize = ourSize;
        }
        if (widthMode == Integer.MIN_VALUE && this.mRequestedNumColumns != -1 && ((((this.mRequestedNumColumns * this.mColumnWidth) + ((this.mRequestedNumColumns - 1) * this.mHorizontalSpacing)) + this.mListPadding.left) + this.mListPadding.right > widthSize || didNotInitiallyFit)) {
            widthSize |= 16777216;
        }
        -wrap6(widthSize, heightSize);
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
            int invertedIndex = (count - 1) - index;
            animationParams.column = (this.mNumColumns - 1) - (invertedIndex % this.mNumColumns);
            animationParams.row = (animationParams.rowsCount - 1) - (invertedIndex / this.mNumColumns);
            return;
        }
        animationParams.column = index % this.mNumColumns;
        animationParams.row = index / this.mNumColumns;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x0093 A:{Catch:{ all -> 0x025a }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00e3 A:{Catch:{ all -> 0x025a }} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x009e A:{Catch:{ all -> 0x025a }} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0093 A:{Catch:{ all -> 0x025a }} */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x009e A:{Catch:{ all -> 0x025a }} */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00e3 A:{Catch:{ all -> 0x025a }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                    if (this.mItemCount == 0) {
                        resetList();
                        invokeOnItemScrollListener();
                        if (!blockLayoutRequests) {
                            this.mBlockLayoutRequests = false;
                        }
                        return;
                    }
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
                                if (!dataChanged || focusChild.hasTransientState() || this.mAdapterHasStableIds) {
                                    accessibilityFocusLayoutRestoreView = focusHost;
                                    accessibilityFocusLayoutRestoreNode = viewRootImpl.getAccessibilityFocusedVirtualView();
                                }
                                accessibilityFocusPosition = getPositionForView(focusChild);
                            }
                        }
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
                                sel = fillSelection(childrenTop, childrenBottom);
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
                            sel = fillSpecific(this.mSelectedPosition, this.mSpecificTop);
                            break;
                        case 5:
                            sel = fillSpecific(this.mSyncPosition, this.mSpecificTop);
                            break;
                        case 6:
                            sel = moveSelection(delta, childrenTop, childrenBottom);
                            break;
                        default:
                            int i2;
                            if (childCount != 0) {
                                if (this.mSelectedPosition >= 0 && this.mSelectedPosition < this.mItemCount) {
                                    i2 = this.mSelectedPosition;
                                    if (oldSel != null) {
                                        childrenTop = oldSel.getTop();
                                    }
                                    sel = fillSpecific(i2, childrenTop);
                                    break;
                                }
                                if (this.mFirstPosition >= this.mItemCount) {
                                    sel = fillSpecific(0, childrenTop);
                                    break;
                                }
                                i2 = this.mFirstPosition;
                                if (oldFirst != null) {
                                    childrenTop = oldFirst.getTop();
                                }
                                sel = fillSpecific(i2, childrenTop);
                                break;
                            } else if (!this.mStackFromBottom) {
                                i2 = (this.mAdapter == null || isInTouchMode()) ? -1 : 0;
                                setSelectedPositionInt(i2);
                                sel = fillFromTop(childrenTop);
                                break;
                            } else {
                                int last = this.mItemCount - 1;
                                if (this.mAdapter == null || isInTouchMode()) {
                                    i2 = -1;
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
                    if (sel != null) {
                        positionSelector(-1, sel);
                        this.mSelectedTop = sel.getTop();
                    } else {
                        boolean inTouchMode = this.mTouchMode > 0 ? this.mTouchMode < 3 : false;
                        View child;
                        if (inTouchMode) {
                            child = getChildAt(this.mMotionPosition - this.mFirstPosition);
                            if (child != null) {
                                positionSelector(this.mMotionPosition, child);
                            }
                        } else if (this.mSelectedPosition != -1) {
                            child = getChildAt(this.mSelectorPosition - this.mFirstPosition);
                            if (child != null) {
                                positionSelector(this.mSelectorPosition, child);
                            }
                        } else {
                            this.mSelectedTop = 0;
                            this.mSelectorRect.setEmpty();
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
                    if (!blockLayoutRequests) {
                        this.mBlockLayoutRequests = false;
                    }
                    return;
                case 6:
                    if (this.mNextSelectedPosition >= 0) {
                        delta = this.mNextSelectedPosition - this.mSelectedPosition;
                    }
                    dataChanged = this.mDataChanged;
                    if (dataChanged) {
                    }
                    if (this.mItemCount == 0) {
                    }
                    break;
                default:
                    index = this.mSelectedPosition - this.mFirstPosition;
                    if (index >= 0 && index < childCount) {
                        oldSel = getChildAt(index);
                    }
                    oldFirst = getChildAt(0);
            }
            dataChanged = this.mDataChanged;
            if (dataChanged) {
            }
            if (this.mItemCount == 0) {
            }
        } finally {
            if (!blockLayoutRequests) {
                this.mBlockLayoutRequests = false;
            }
        }
    }

    private View makeAndAddView(int position, int y, boolean flow, int childrenLeft, boolean selected, int where) {
        if (!this.mDataChanged) {
            View activeView = this.mRecycler.getActiveView(position);
            if (activeView != null) {
                setupChild(activeView, position, y, flow, childrenLeft, selected, true, where);
                return activeView;
            }
        }
        View child = obtainView(position, this.mIsScrap);
        setupChild(child, position, y, flow, childrenLeft, selected, this.mIsScrap[0], where);
        return child;
    }

    private void setupChild(View child, int position, int y, boolean flowDown, int childrenLeft, boolean selected, boolean isAttachedToWindow, int where) {
        boolean needToMeasure;
        int childLeft;
        Trace.traceBegin(8, "setupGridItem");
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
        ViewGroup.LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) generateDefaultLayoutParams();
        }
        p.viewType = this.mAdapter.getItemViewType(position);
        p.isEnabled = this.mAdapter.isEnabled(position);
        if (updateChildSelected) {
            child.setSelected(isSelected);
            if (isSelected) {
                requestFocus();
            }
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
        if (!isAttachedToWindow || (p.forceAdd ^ 1) == 0) {
            p.forceAdd = false;
            addViewInLayout(child, where, p, true);
        } else {
            attachViewToParent(child, where, p);
            if (!(isAttachedToWindow && ((LayoutParams) child.getLayoutParams()).scrappedFromPosition == position)) {
                child.jumpDrawablesToCurrentState();
            }
        }
        if (needToMeasure) {
            child.measure(ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(this.mColumnWidth, 1073741824), 0, p.width), ViewGroup.getChildMeasureSpec(MeasureSpec.makeMeasureSpec(0, 0), 0, p.height));
        } else {
            cleanupLayoutState(child);
        }
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childTop = flowDown ? y : y - h;
        switch (Gravity.getAbsoluteGravity(this.mGravity, getLayoutDirection()) & 7) {
            case 1:
                childLeft = childrenLeft + ((this.mColumnWidth - w) / 2);
                break;
            case 3:
                childLeft = childrenLeft;
                break;
            case 5:
                childLeft = (this.mColumnWidth + childrenLeft) - w;
                break;
            default:
                childLeft = childrenLeft;
                break;
        }
        if (needToMeasure) {
            child.layout(childLeft, childTop, childLeft + w, childTop + h);
        } else {
            child.offsetLeftAndRight(childLeft - child.getLeft());
            child.offsetTopAndBottom(childTop - child.getTop());
        }
        if (this.mCachingStarted && (child.isDrawingCacheEnabled() ^ 1) != 0) {
            child.setDrawingCacheEnabled(true);
        }
        Trace.traceEnd(8);
    }

    public void setSelection(int position) {
        if (isInTouchMode()) {
            this.mResurrectToPosition = position;
        } else {
            setNextSelectedPositionInt(position);
        }
        this.mLayoutMode = 2;
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
            next = (this.mItemCount - 1) - this.mNextSelectedPosition;
        } else {
            next = this.mNextSelectedPosition;
        }
        if (this.mStackFromBottom) {
            previous = (this.mItemCount - 1) - previousSelectedPosition;
        } else {
            previous = previousSelectedPosition;
        }
        if (next / this.mNumColumns != previous / this.mNumColumns) {
            awakenScrollBars();
        }
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
        if (this.mAdapter == null) {
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
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = arrowScroll(33);
                        break;
                    } else {
                        handled = true;
                        break;
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
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = arrowScroll(130);
                        break;
                    } else {
                        handled = true;
                        break;
                    }
                    break;
                case 21:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            handled = arrowScroll(17);
                            break;
                        }
                        handled = true;
                        break;
                    }
                    break;
                case 22:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            handled = arrowScroll(66);
                            break;
                        }
                        handled = true;
                        break;
                    }
                    break;
                case 61:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(1)) {
                            if (!resurrectSelectionIfNeeded()) {
                                handled = sequenceScroll(1);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = sequenceScroll(2);
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
        int nextPage = -1;
        if (direction == 33) {
            nextPage = Math.max(0, this.mSelectedPosition - getChildCount());
        } else if (direction == 130) {
            nextPage = Math.min(this.mItemCount - 1, this.mSelectedPosition + getChildCount());
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
            this.mLayoutMode = 2;
            setSelectionInt(0);
            invokeOnItemScrollListener();
            moved = true;
        } else if (direction == 130) {
            this.mLayoutMode = 2;
            setSelectionInt(this.mItemCount - 1);
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
            endOfRowPos = (this.mItemCount - 1) - ((((this.mItemCount - 1) - selectedPosition) / numColumns) * numColumns);
            startOfRowPos = Math.max(0, (endOfRowPos - numColumns) + 1);
        } else {
            startOfRowPos = (selectedPosition / numColumns) * numColumns;
            endOfRowPos = Math.min((startOfRowPos + numColumns) - 1, this.mItemCount - 1);
        }
        switch (direction) {
            case 33:
                if (startOfRowPos > 0) {
                    this.mLayoutMode = 6;
                    setSelectionInt(Math.max(0, selectedPosition - numColumns));
                    moved = true;
                    break;
                }
                break;
            case 130:
                if (endOfRowPos < this.mItemCount - 1) {
                    this.mLayoutMode = 6;
                    setSelectionInt(Math.min(selectedPosition + numColumns, this.mItemCount - 1));
                    moved = true;
                    break;
                }
                break;
        }
        boolean isLayoutRtl = isRtlLocale();
        if (selectedPosition > startOfRowPos && ((direction == 17 && (isLayoutRtl ^ 1) != 0) || (direction == 66 && isLayoutRtl))) {
            this.mLayoutMode = 6;
            setSelectionInt(Math.max(0, selectedPosition - 1));
            moved = true;
        } else if (selectedPosition < endOfRowPos && ((direction == 17 && isLayoutRtl) || (direction == 66 && (isLayoutRtl ^ 1) != 0))) {
            this.mLayoutMode = 6;
            setSelectionInt(Math.min(selectedPosition + 1, this.mItemCount - 1));
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
            endOfRow = (count - 1) - ((((count - 1) - selectedPosition) / numColumns) * numColumns);
            startOfRow = Math.max(0, (endOfRow - numColumns) + 1);
        } else {
            startOfRow = (selectedPosition / numColumns) * numColumns;
            endOfRow = Math.min((startOfRow + numColumns) - 1, count - 1);
        }
        boolean moved = false;
        boolean showScroll = false;
        switch (direction) {
            case 1:
                if (selectedPosition > 0) {
                    this.mLayoutMode = 6;
                    setSelectionInt(selectedPosition - 1);
                    moved = true;
                    if (selectedPosition != startOfRow) {
                        showScroll = false;
                        break;
                    }
                    showScroll = true;
                    break;
                }
                break;
            case 2:
                if (selectedPosition < count - 1) {
                    this.mLayoutMode = 6;
                    setSelectionInt(selectedPosition + 1);
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
        int closestChildIndex = -1;
        if (gainFocus && previouslyFocusedRect != null) {
            previouslyFocusedRect.offset(this.mScrollX, this.mScrollY);
            Rect otherRect = this.mTempRect;
            int minDistance = Integer.MAX_VALUE;
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
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
        int invertedIndex = (count - 1) - childIndex;
        if (this.mStackFromBottom) {
            rowEnd = (count - 1) - (invertedIndex - (invertedIndex % this.mNumColumns));
            rowStart = Math.max(0, (rowEnd - this.mNumColumns) + 1);
        } else {
            rowStart = childIndex - (childIndex % this.mNumColumns);
            rowEnd = Math.min((this.mNumColumns + rowStart) - 1, count);
        }
        switch (direction) {
            case 1:
                if (childIndex == rowEnd && rowEnd == count - 1) {
                    z2 = true;
                }
                return z2;
            case 2:
                if (childIndex == rowStart && rowStart == 0) {
                    z2 = true;
                }
                return z2;
            case 17:
                if (childIndex != rowEnd) {
                    z = false;
                }
                return z;
            case 33:
                if (rowEnd != count - 1) {
                    z = false;
                }
                return z;
            case 66:
                if (childIndex != rowStart) {
                    z = false;
                }
                return z;
            case 130:
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
                delta = getChildAt(childCount - 1).getBottom() - (getHeight() - this.mListPadding.bottom);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta += this.mVerticalSpacing;
                }
                if (delta > 0) {
                    delta = 0;
                }
            } else {
                delta = getChildAt(0).getTop() - this.mListPadding.top;
                if (this.mFirstPosition != 0) {
                    delta -= this.mVerticalSpacing;
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

    protected int computeVerticalScrollExtent() {
        int count = getChildCount();
        if (count <= 0) {
            return 0;
        }
        int numColumns = this.mNumColumns;
        int extent = (((count + numColumns) - 1) / numColumns) * 100;
        View view = getChildAt(0);
        int top = view.getTop();
        int height = view.getHeight();
        if (height > 0) {
            extent += (top * 100) / height;
        }
        view = getChildAt(count - 1);
        int bottom = view.getBottom();
        height = view.getHeight();
        if (height > 0) {
            extent -= ((bottom - getHeight()) * 100) / height;
        }
        return extent;
    }

    protected int computeVerticalScrollOffset() {
        if (this.mFirstPosition >= 0 && getChildCount() > 0) {
            View view = getChildAt(0);
            int top = view.getTop();
            int height = view.getHeight();
            if (height > 0) {
                int oddItemsOnFirstRow;
                int numColumns = this.mNumColumns;
                int rowCount = ((this.mItemCount + numColumns) - 1) / numColumns;
                if (isStackFromBottom()) {
                    oddItemsOnFirstRow = (rowCount * numColumns) - this.mItemCount;
                } else {
                    oddItemsOnFirstRow = 0;
                }
                return Math.max(((((this.mFirstPosition + oddItemsOnFirstRow) / numColumns) * 100) - ((top * 100) / height)) + ((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) rowCount)) * 100.0f)), 0);
            }
        }
        return 0;
    }

    protected int computeVerticalScrollRange() {
        int numColumns = this.mNumColumns;
        int rowCount = ((this.mItemCount + numColumns) - 1) / numColumns;
        int result = Math.max(rowCount * 100, 0);
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
                int row = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, -1);
                int position = Math.min(row * numColumns, getCount() - 1);
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
            int invertedIndex = (count - 1) - position;
            column = (columnsCount - 1) - (invertedIndex % columnsCount);
            row = (rowsCount - 1) - (invertedIndex / columnsCount);
        } else {
            column = position % columnsCount;
            row = position / columnsCount;
        }
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        boolean isHeading = lp != null && lp.viewType == -2;
        info.setCollectionItemInfo(CollectionItemInfo.obtain(row, 1, column, 1, isHeading, isItemChecked(position)));
    }

    protected void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("numColumns", getNumColumns());
    }
}
