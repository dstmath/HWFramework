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
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewHierarchyEncoder;
import android.view.ViewRootImpl;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.GridLayoutAnimationController;
import android.widget.AbsListView;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@RemoteViews.RemoteView
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
    private int mPressedStateDuration;
    private View mReferenceView;
    private View mReferenceViewInSelectedRow;
    private int mRequestedColumnWidth;
    private int mRequestedHorizontalSpacing;
    private int mRequestedNumColumns;
    private int mStretchMode;
    private final Rect mTempRect;
    private int mVerticalSpacing;

    @Retention(RetentionPolicy.SOURCE)
    public @interface StretchMode {
    }

    public GridView(Context context) {
        this(context, null);
    }

    public GridView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842865);
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
        this.mPressedStateDuration = ViewConfiguration.getPressedStateDuration();
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
        int index2 = a.getInt(0, -1);
        if (index2 >= 0) {
            setGravity(index2);
        }
        a.recycle();
        this.mPressedStateDuration = context.getResources().getInteger(com.android.hwext.internal.R.integer.config_gridviewPressedStateDuration);
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
        this.mAdapter = adapter;
        this.mOldSelectedPosition = -1;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        super.setAdapter(adapter);
        if (this.mAdapter != null) {
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
            this.mDataChanged = true;
            checkFocus();
            this.mDataSetObserver = new AbsListView.AdapterDataSetObserver();
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

    /* access modifiers changed from: package-private */
    public int lookForSelectablePosition(int position, boolean lookDown) {
        if (this.mAdapter == null || isInTouchMode() || position < 0 || position >= this.mItemCount) {
            return -1;
        }
        return position;
    }

    /* access modifiers changed from: package-private */
    public void fillGap(boolean down) {
        int position;
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int count = getChildCount();
        if (down) {
            int paddingTop = 0;
            if ((this.mGroupFlags & 34) == 34) {
                paddingTop = getListPaddingTop();
            }
            int startOffset = count > 0 ? getChildAt(count - 1).getBottom() + verticalSpacing : paddingTop;
            int position2 = this.mFirstPosition + count;
            if (this.mStackFromBottom) {
                position2 += numColumns - 1;
            }
            fillDown(position2, startOffset);
            correctTooHigh(numColumns, verticalSpacing, getChildCount());
            return;
        }
        int paddingBottom = 0;
        if ((this.mGroupFlags & 34) == 34) {
            paddingBottom = getListPaddingBottom();
        }
        int startOffset2 = count > 0 ? getChildAt(0).getTop() - verticalSpacing : getHeight() - paddingBottom;
        int position3 = this.mFirstPosition;
        if (!this.mStackFromBottom) {
            position = position3 - numColumns;
        } else {
            position = position3 - 1;
        }
        fillUp(position, startOffset2);
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
        int startPos2;
        int columnWidth = this.mColumnWidth;
        int horizontalSpacing = this.mHorizontalSpacing;
        boolean isLayoutRtl = isLayoutRtl();
        boolean z = false;
        if (isLayoutRtl) {
            nextLeft = ((getWidth() - this.mListPadding.right) - columnWidth) - (this.mStretchMode == 3 ? horizontalSpacing : 0);
        } else {
            nextLeft = this.mListPadding.left + (this.mStretchMode == 3 ? horizontalSpacing : 0);
        }
        int nextLeft2 = nextLeft;
        if (this.mStackFromBottom == 0) {
            last = Math.min(startPos + this.mNumColumns, this.mItemCount);
            startPos2 = startPos;
        } else {
            last = startPos + 1;
            int startPos3 = Math.max(0, (startPos - this.mNumColumns) + 1);
            if (last - startPos3 < this.mNumColumns) {
                nextLeft2 += (isLayoutRtl ? -1 : 1) * (this.mNumColumns - (last - startPos3)) * (columnWidth + horizontalSpacing);
            }
            startPos2 = startPos3;
        }
        int last2 = last;
        boolean hasFocus = shouldShowSelector();
        boolean inClick = touchModeDrawsInPressedState();
        int selectedPosition = this.mSelectedPosition;
        int nextChildDir = isLayoutRtl ? -1 : 1;
        View selectedView = null;
        int nextLeft3 = nextLeft2;
        View child = null;
        int pos = startPos2;
        while (true) {
            int pos2 = pos;
            if (pos2 >= last2) {
                break;
            }
            boolean selected = pos2 == selectedPosition ? true : z;
            int pos3 = pos2;
            View view = child;
            int selectedPosition2 = selectedPosition;
            child = makeAndAddView(pos2, y, flow, nextLeft3, selected, flow ? -1 : pos2 - startPos2);
            nextLeft3 += nextChildDir * columnWidth;
            if (pos3 < last2 - 1) {
                nextLeft3 += nextChildDir * horizontalSpacing;
            }
            if (selected && (hasFocus || inClick)) {
                selectedView = child;
            }
            pos = pos3 + 1;
            selectedPosition = selectedPosition2;
            z = false;
        }
        int i = selectedPosition;
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
        int invertedSelection;
        int selectedPosition = reconcileSelectedPosition();
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int rowEnd = -1;
        if (!this.mStackFromBottom) {
            invertedSelection = selectedPosition - (selectedPosition % numColumns);
        } else {
            int invertedSelection2 = (this.mItemCount - 1) - selectedPosition;
            rowEnd = (this.mItemCount - 1) - (invertedSelection2 - (invertedSelection2 % numColumns));
            invertedSelection = Math.max(0, (rowEnd - numColumns) + 1);
        }
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        View sel = makeRow(this.mStackFromBottom ? rowEnd : invertedSelection, getTopSelectionPixel(childrenTop, fadingEdgeLength, invertedSelection), true);
        this.mFirstPosition = invertedSelection;
        View referenceView = this.mReferenceView;
        if (!this.mStackFromBottom) {
            fillDown(invertedSelection + numColumns, referenceView.getBottom() + verticalSpacing);
            pinToBottom(childrenBottom);
            fillUp(invertedSelection - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
        } else {
            offsetChildrenTopAndBottom(getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, invertedSelection) - referenceView.getBottom());
            fillUp(invertedSelection - 1, referenceView.getTop() - verticalSpacing);
            pinToTop(childrenTop);
            fillDown(rowEnd + numColumns, referenceView.getBottom() + verticalSpacing);
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

    /* access modifiers changed from: package-private */
    public int findMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int numColumns = this.mNumColumns;
            if (!this.mStackFromBottom) {
                for (int i = 0; i < childCount; i += numColumns) {
                    if (y <= getChildAt(i).getBottom()) {
                        return this.mFirstPosition + i;
                    }
                }
            } else {
                for (int i2 = childCount - 1; i2 >= 0; i2 -= numColumns) {
                    if (y >= getChildAt(i2).getTop()) {
                        return this.mFirstPosition + i2;
                    }
                }
            }
        }
        return -1;
    }

    private View fillSpecific(int position, int top) {
        int invertedSelection;
        View below;
        View above;
        int numColumns = this.mNumColumns;
        int motionRowEnd = -1;
        if (!this.mStackFromBottom) {
            invertedSelection = position - (position % numColumns);
        } else {
            int invertedSelection2 = (this.mItemCount - 1) - position;
            motionRowEnd = (this.mItemCount - 1) - (invertedSelection2 - (invertedSelection2 % numColumns));
            invertedSelection = Math.max(0, (motionRowEnd - numColumns) + 1);
        }
        View temp = makeRow(this.mStackFromBottom ? motionRowEnd : invertedSelection, top, true);
        this.mFirstPosition = invertedSelection;
        View referenceView = this.mReferenceView;
        if (referenceView == null) {
            return null;
        }
        int verticalSpacing = this.mVerticalSpacing;
        if (!this.mStackFromBottom) {
            above = fillUp(invertedSelection - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            below = fillDown(invertedSelection + numColumns, referenceView.getBottom() + verticalSpacing);
            int childCount = getChildCount();
            if (childCount > 0) {
                correctTooHigh(numColumns, verticalSpacing, childCount);
            }
        } else {
            below = fillDown(motionRowEnd + numColumns, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            above = fillUp(invertedSelection - 1, referenceView.getTop() - verticalSpacing);
            int childCount2 = getChildCount();
            if (childCount2 > 0) {
                correctTooLow(numColumns, verticalSpacing, childCount2);
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
        int i = 1;
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
                    int i2 = this.mFirstPosition;
                    if (!this.mStackFromBottom) {
                        i = numColumns;
                    }
                    fillUp(i2 - i, firstChild.getTop() - verticalSpacing);
                    adjustViewsUpOrDown();
                }
            }
        }
    }

    private void correctTooLow(int numColumns, int verticalSpacing, int childCount) {
        if (this.mFirstPosition == 0 && childCount > 0) {
            int firstTop = getChildAt(0).getTop();
            int start = this.mListPadding.top;
            int end = (this.mBottom - this.mTop) - this.mListPadding.bottom;
            int topOffset = firstTop - start;
            View lastChild = getChildAt(childCount - 1);
            int lastBottom = lastChild.getBottom();
            int i = 1;
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
                    if (this.mStackFromBottom) {
                        i = numColumns;
                    }
                    fillDown(i + lastPosition, lastChild.getBottom() + verticalSpacing);
                    adjustViewsUpOrDown();
                }
            }
        }
    }

    private View fillFromSelection(int selectedTop, int childrenTop, int childrenBottom) {
        int invertedSelection;
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int rowEnd = -1;
        if (!this.mStackFromBottom) {
            invertedSelection = selectedPosition - (selectedPosition % numColumns);
        } else {
            int invertedSelection2 = (this.mItemCount - 1) - selectedPosition;
            rowEnd = (this.mItemCount - 1) - (invertedSelection2 - (invertedSelection2 % numColumns));
            invertedSelection = Math.max(0, (rowEnd - numColumns) + 1);
        }
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, invertedSelection);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, invertedSelection);
        View sel = makeRow(this.mStackFromBottom ? rowEnd : invertedSelection, selectedTop, true);
        this.mFirstPosition = invertedSelection;
        View referenceView = this.mReferenceView;
        adjustForTopFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        adjustForBottomFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        if (!this.mStackFromBottom) {
            fillUp(invertedSelection - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            fillDown(invertedSelection + numColumns, referenceView.getBottom() + verticalSpacing);
        } else {
            fillDown(rowEnd + numColumns, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            fillUp(invertedSelection - 1, referenceView.getTop() - verticalSpacing);
        }
        return sel;
    }

    private int getBottomSelectionPixel(int childrenBottom, int fadingEdgeLength, int numColumns, int rowStart) {
        int bottomSelectionPixel = childrenBottom;
        if ((rowStart + numColumns) - 1 < this.mItemCount - 1) {
            return bottomSelectionPixel - fadingEdgeLength;
        }
        return bottomSelectionPixel;
    }

    private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength, int rowStart) {
        int topSelectionPixel = childrenTop;
        if (rowStart > 0) {
            return topSelectionPixel + fadingEdgeLength;
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
        View referenceView;
        View sel;
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int rowEnd = -1;
        if (!this.mStackFromBottom) {
            oldRowStart = (selectedPosition - delta) - ((selectedPosition - delta) % numColumns);
            rowStart = selectedPosition - (selectedPosition % numColumns);
        } else {
            int invertedSelection = (this.mItemCount - 1) - selectedPosition;
            rowEnd = (this.mItemCount - 1) - (invertedSelection - (invertedSelection % numColumns));
            rowStart = Math.max(0, (rowEnd - numColumns) + 1);
            int invertedSelection2 = (this.mItemCount - 1) - (selectedPosition - delta);
            oldRowStart = Math.max(0, (((this.mItemCount - 1) - (invertedSelection2 - (invertedSelection2 % numColumns))) - numColumns) + 1);
        }
        int invertedSelection3 = rowStart - oldRowStart;
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, rowStart);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, rowStart);
        this.mFirstPosition = rowStart;
        if (invertedSelection3 > 0) {
            int i = fadingEdgeLength;
            View sel2 = makeRow(this.mStackFromBottom ? rowEnd : rowStart, (this.mReferenceViewInSelectedRow == null ? 0 : this.mReferenceViewInSelectedRow.getBottom()) + verticalSpacing, true);
            View referenceView2 = this.mReferenceView;
            adjustForBottomFadingEdge(referenceView2, topSelectionPixel, bottomSelectionPixel);
            View view = referenceView2;
            referenceView = sel2;
            sel = view;
        } else {
            if (invertedSelection3 < 0) {
                referenceView = makeRow(this.mStackFromBottom ? rowEnd : rowStart, (this.mReferenceViewInSelectedRow == null ? 0 : this.mReferenceViewInSelectedRow.getTop()) - verticalSpacing, false);
                View referenceView3 = this.mReferenceView;
                adjustForTopFadingEdge(referenceView3, topSelectionPixel, bottomSelectionPixel);
                sel = referenceView3;
            } else {
                referenceView = makeRow(this.mStackFromBottom ? rowEnd : rowStart, this.mReferenceViewInSelectedRow == null ? 0 : this.mReferenceViewInSelectedRow.getTop(), true);
                sel = this.mReferenceView;
            }
        }
        if (!this.mStackFromBottom) {
            fillUp(rowStart - numColumns, sel.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            fillDown(rowStart + numColumns, sel.getBottom() + verticalSpacing);
        } else {
            fillDown(rowEnd + numColumns, sel.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            fillUp(rowStart - 1, sel.getTop() - verticalSpacing);
        }
        return referenceView;
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
        if (stretchMode != 0) {
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
                    } else {
                        this.mHorizontalSpacing = (spaceLeftOver / (this.mNumColumns - 1)) + requestedHorizontalSpacing;
                        break;
                    }
                case 2:
                    this.mColumnWidth = (spaceLeftOver / this.mNumColumns) + requestedColumnWidth;
                    this.mHorizontalSpacing = requestedHorizontalSpacing;
                    break;
                case 3:
                    this.mColumnWidth = requestedColumnWidth;
                    if (this.mNumColumns <= 1) {
                        this.mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                        break;
                    } else {
                        this.mHorizontalSpacing = (spaceLeftOver / (this.mNumColumns + 1)) + requestedHorizontalSpacing;
                        break;
                    }
            }
        } else {
            this.mColumnWidth = requestedColumnWidth;
            this.mHorizontalSpacing = requestedHorizontalSpacing;
        }
        return didNotInitiallyFit;
    }

    /* JADX WARNING: type inference failed for: r15v11, types: [android.view.ViewGroup$LayoutParams] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize2 = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == 0) {
            if (this.mColumnWidth > 0) {
                widthSize = this.mColumnWidth + this.mListPadding.left + this.mListPadding.right;
            } else {
                widthSize = this.mListPadding.left + this.mListPadding.right;
            }
            widthSize2 = getVerticalScrollbarWidth() + widthSize;
        }
        boolean didNotInitiallyFit = determineColumns((widthSize2 - this.mListPadding.left) - this.mListPadding.right);
        int childHeight = 0;
        this.mItemCount = this.mAdapter == null ? 0 : this.mAdapter.getCount();
        int count = this.mItemCount;
        if (count > 0) {
            View child = obtainView(0, this.mIsScrap);
            AbsListView.LayoutParams p = (AbsListView.LayoutParams) child.getLayoutParams();
            if (p == null) {
                p = generateDefaultLayoutParams();
                child.setLayoutParams(p);
            }
            p.viewType = this.mAdapter.getItemViewType(0);
            p.isEnabled = this.mAdapter.isEnabled(0);
            p.forceAdd = true;
            child.measure(getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(this.mColumnWidth, 1073741824), 0, p.width), getChildMeasureSpec(View.MeasureSpec.makeSafeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec), 0), 0, p.height));
            childHeight = child.getMeasuredHeight();
            int childState = combineMeasuredStates(0, child.getMeasuredState());
            if (this.mRecycler.shouldRecycleViewType(p.viewType)) {
                this.mRecycler.addScrapView(child, -1);
            }
        }
        if (heightMode == 0) {
            heightSize = this.mListPadding.top + this.mListPadding.bottom + childHeight + (getVerticalFadingEdgeLength() * 2);
        }
        if (heightMode == Integer.MIN_VALUE) {
            int ourSize = this.mListPadding.top + this.mListPadding.bottom;
            int numColumns = this.mNumColumns;
            int i = 0;
            while (true) {
                int i2 = i;
                if (i2 >= count) {
                    break;
                }
                ourSize += childHeight;
                if (i2 + numColumns < count) {
                    ourSize += this.mVerticalSpacing;
                }
                if (ourSize >= heightSize) {
                    ourSize = heightSize;
                    break;
                }
                i = i2 + numColumns;
            }
            heightSize = ourSize;
        }
        if (widthMode == Integer.MIN_VALUE && this.mRequestedNumColumns != -1 && ((this.mRequestedNumColumns * this.mColumnWidth) + ((this.mRequestedNumColumns - 1) * this.mHorizontalSpacing) + this.mListPadding.left + this.mListPadding.right > widthSize2 || didNotInitiallyFit)) {
            widthSize2 |= 16777216;
        }
        setMeasuredDimension(widthSize2, heightSize);
        this.mWidthMeasureSpec = widthMeasureSpec;
    }

    /* access modifiers changed from: protected */
    public void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
        GridLayoutAnimationController.AnimationParameters animationParams = (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;
        if (animationParams == null) {
            animationParams = new GridLayoutAnimationController.AnimationParameters();
            params.layoutAnimationParameters = animationParams;
        }
        animationParams.count = count;
        animationParams.index = index;
        animationParams.columnsCount = this.mNumColumns;
        animationParams.rowsCount = count / this.mNumColumns;
        if (!this.mStackFromBottom) {
            animationParams.column = index % this.mNumColumns;
            animationParams.row = index / this.mNumColumns;
            return;
        }
        int invertedIndex = (count - 1) - index;
        animationParams.column = (this.mNumColumns - 1) - (invertedIndex % this.mNumColumns);
        animationParams.row = (animationParams.rowsCount - 1) - (invertedIndex / this.mNumColumns);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* JADX WARNING: Removed duplicated region for block: B:129:0x01cb A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x01d7 A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:149:0x021a A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x028e A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x02a5 A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x02ad  */
    /* JADX WARNING: Removed duplicated region for block: B:186:0x02b8  */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x00fb A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0110 A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0115 A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x011b A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x0125 A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x012f A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x013c A[Catch:{ all -> 0x02b1 }] */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x014e A[Catch:{ all -> 0x02b1 }] */
    public void layoutChildren() {
        boolean blockLayoutRequests;
        AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode;
        AbsListView.RecycleBin recycleBin;
        int accessibilityFocusPosition;
        View sel;
        int i;
        int i2;
        int i3;
        int i4;
        GridView gridView = this;
        boolean blockLayoutRequests2 = gridView.mBlockLayoutRequests;
        if (!blockLayoutRequests2) {
            gridView.mBlockLayoutRequests = true;
        }
        super.layoutChildren();
        invalidate();
        if (gridView.mAdapter == null) {
            try {
                resetList();
                invokeOnItemScrollListener();
                if (!blockLayoutRequests2) {
                    gridView.mBlockLayoutRequests = false;
                }
            } catch (Throwable th) {
                th = th;
                blockLayoutRequests = blockLayoutRequests2;
                if (!blockLayoutRequests) {
                }
                throw th;
            }
        } else {
            int childrenTop = gridView.mListPadding.top;
            int childrenBottom = (gridView.mBottom - gridView.mTop) - gridView.mListPadding.bottom;
            int childCount = getChildCount();
            int delta = 0;
            View oldSel = null;
            View oldFirst = null;
            View newSel = null;
            switch (gridView.mLayoutMode) {
                case 1:
                case 3:
                case 4:
                case 5:
                    break;
                case 2:
                    int index = gridView.mNextSelectedPosition - gridView.mFirstPosition;
                    if (index >= 0 && index < childCount) {
                        newSel = gridView.getChildAt(index);
                    }
                case 6:
                    if (gridView.mNextSelectedPosition >= 0) {
                        delta = gridView.mNextSelectedPosition - gridView.mSelectedPosition;
                    }
                default:
                    int index2 = gridView.mSelectedPosition - gridView.mFirstPosition;
                    if (index2 >= 0 && index2 < childCount) {
                        oldSel = gridView.getChildAt(index2);
                    }
                    try {
                        oldFirst = gridView.getChildAt(0);
                    } catch (Throwable th2) {
                        th = th2;
                        blockLayoutRequests = blockLayoutRequests2;
                        if (!blockLayoutRequests) {
                        }
                        throw th;
                    }
                    break;
            }
            int i5 = gridView.mDataChanged;
            if (i5 != 0) {
                handleDataChanged();
            }
            if (gridView.mItemCount == 0) {
                resetList();
                invokeOnItemScrollListener();
                if (!blockLayoutRequests2) {
                    gridView.mBlockLayoutRequests = false;
                }
                return;
            }
            gridView.setSelectedPositionInt(gridView.mNextSelectedPosition);
            View accessibilityFocusLayoutRestoreView = null;
            int accessibilityFocusPosition2 = -1;
            ViewRootImpl viewRootImpl = getViewRootImpl();
            if (viewRootImpl != null) {
                View focusHost = viewRootImpl.getAccessibilityFocusedHost();
                if (focusHost != null) {
                    View focusChild = gridView.getAccessibilityFocusedChild(focusHost);
                    if (focusChild != null) {
                        if (i5 != 0 && !focusChild.hasTransientState()) {
                            if (!gridView.mAdapterHasStableIds) {
                                accessibilityFocusLayoutRestoreNode = null;
                                accessibilityFocusPosition2 = gridView.getPositionForView(focusChild);
                                int firstPosition = gridView.mFirstPosition;
                                recycleBin = gridView.mRecycler;
                                if (i5 != 0) {
                                    int i6 = 0;
                                    while (true) {
                                        int dataChanged = i5;
                                        int i7 = i6;
                                        if (i7 < childCount) {
                                            blockLayoutRequests = blockLayoutRequests2;
                                            try {
                                                recycleBin.addScrapView(gridView.getChildAt(i7), firstPosition + i7);
                                                i6 = i7 + 1;
                                                i5 = dataChanged;
                                                blockLayoutRequests2 = blockLayoutRequests;
                                                accessibilityFocusPosition2 = accessibilityFocusPosition2;
                                            } catch (Throwable th3) {
                                                th = th3;
                                                if (!blockLayoutRequests) {
                                                }
                                                throw th;
                                            }
                                        } else {
                                            blockLayoutRequests = blockLayoutRequests2;
                                            accessibilityFocusPosition = accessibilityFocusPosition2;
                                        }
                                    }
                                } else {
                                    blockLayoutRequests = blockLayoutRequests2;
                                    int i8 = i5;
                                    accessibilityFocusPosition = accessibilityFocusPosition2;
                                    recycleBin.fillActiveViews(childCount, firstPosition);
                                }
                                detachAllViewsFromParent();
                                recycleBin.removeSkippedScrap();
                                switch (gridView.mLayoutMode) {
                                    case 1:
                                        gridView.mFirstPosition = 0;
                                        sel = gridView.fillFromTop(childrenTop);
                                        adjustViewsUpOrDown();
                                        break;
                                    case 2:
                                        if (newSel == null) {
                                            sel = gridView.fillSelection(childrenTop, childrenBottom);
                                            break;
                                        } else {
                                            sel = gridView.fillFromSelection(newSel.getTop(), childrenTop, childrenBottom);
                                        }
                                    case 3:
                                        sel = gridView.fillUp(gridView.mItemCount - 1, childrenBottom);
                                        adjustViewsUpOrDown();
                                        break;
                                    case 4:
                                        sel = gridView.fillSpecific(gridView.mSelectedPosition, gridView.mSpecificTop);
                                        break;
                                    case 5:
                                        sel = gridView.fillSpecific(gridView.mSyncPosition, gridView.mSpecificTop);
                                        break;
                                    case 6:
                                        sel = gridView.moveSelection(delta, childrenTop, childrenBottom);
                                        break;
                                    default:
                                        if (childCount == 0) {
                                            if (!gridView.mStackFromBottom) {
                                                if (gridView.mAdapter != null) {
                                                    if (!isInTouchMode()) {
                                                        i4 = 0;
                                                        gridView.setSelectedPositionInt(i4);
                                                        sel = gridView.fillFromTop(childrenTop);
                                                        break;
                                                    }
                                                }
                                                i4 = -1;
                                                gridView.setSelectedPositionInt(i4);
                                                sel = gridView.fillFromTop(childrenTop);
                                            } else {
                                                int last = gridView.mItemCount - 1;
                                                if (gridView.mAdapter != null) {
                                                    if (!isInTouchMode()) {
                                                        i3 = last;
                                                        gridView.setSelectedPositionInt(i3);
                                                        sel = gridView.fillFromBottom(last, childrenBottom);
                                                        break;
                                                    }
                                                }
                                                i3 = -1;
                                                gridView.setSelectedPositionInt(i3);
                                                sel = gridView.fillFromBottom(last, childrenBottom);
                                            }
                                        } else if (gridView.mSelectedPosition < 0 || gridView.mSelectedPosition >= gridView.mItemCount) {
                                            if (gridView.mFirstPosition >= gridView.mItemCount) {
                                                sel = gridView.fillSpecific(0, childrenTop);
                                                break;
                                            } else {
                                                int i9 = gridView.mFirstPosition;
                                                if (oldFirst == null) {
                                                    i = childrenTop;
                                                } else {
                                                    i = oldFirst.getTop();
                                                }
                                                sel = gridView.fillSpecific(i9, i);
                                            }
                                        } else {
                                            int i10 = gridView.mSelectedPosition;
                                            if (oldSel == null) {
                                                i2 = childrenTop;
                                            } else {
                                                i2 = oldSel.getTop();
                                            }
                                            sel = gridView.fillSpecific(i10, i2);
                                        }
                                        break;
                                }
                                recycleBin.scrapActiveViews();
                                if (sel != null) {
                                    gridView.positionSelector(-1, sel);
                                    gridView.mSelectedTop = sel.getTop();
                                    int i11 = firstPosition;
                                } else {
                                    if (gridView.mTouchMode > 0 && gridView.mTouchMode < 3) {
                                        int i12 = firstPosition;
                                        View child = gridView.getChildAt(gridView.mMotionPosition - gridView.mFirstPosition);
                                        if (child != null) {
                                            gridView.positionSelector(gridView.mMotionPosition, child);
                                        }
                                    } else {
                                        if (gridView.mSelectedPosition != -1) {
                                            View child2 = gridView.getChildAt(gridView.mSelectorPosition - gridView.mFirstPosition);
                                            if (child2 != null) {
                                                gridView.positionSelector(gridView.mSelectorPosition, child2);
                                            }
                                        } else {
                                            gridView.mSelectedTop = 0;
                                            gridView.mSelectorRect.setEmpty();
                                        }
                                    }
                                }
                                if (viewRootImpl != null || viewRootImpl.getAccessibilityFocusedHost() != null) {
                                    AbsListView.RecycleBin recycleBin2 = recycleBin;
                                    int i13 = accessibilityFocusPosition;
                                } else if (accessibilityFocusLayoutRestoreView == null || !accessibilityFocusLayoutRestoreView.isAttachedToWindow()) {
                                    View view = sel;
                                    int accessibilityFocusPosition3 = accessibilityFocusPosition;
                                    if (accessibilityFocusPosition3 != -1) {
                                        gridView = this;
                                        AbsListView.RecycleBin recycleBin3 = recycleBin;
                                        View restoreView = gridView.getChildAt(MathUtils.constrain(accessibilityFocusPosition3 - gridView.mFirstPosition, 0, getChildCount() - 1));
                                        if (restoreView != null) {
                                            restoreView.requestAccessibilityFocus();
                                        }
                                    } else {
                                        gridView = this;
                                    }
                                } else {
                                    AccessibilityNodeProvider provider = accessibilityFocusLayoutRestoreView.getAccessibilityNodeProvider();
                                    if (accessibilityFocusLayoutRestoreNode == null || provider == null) {
                                        accessibilityFocusLayoutRestoreView.requestAccessibilityFocus();
                                    } else {
                                        try {
                                            View view2 = sel;
                                            provider.performAction(AccessibilityNodeInfo.getVirtualDescendantId(accessibilityFocusLayoutRestoreNode.getSourceNodeId()), 64, null);
                                        } catch (Throwable th4) {
                                            th = th4;
                                            gridView = this;
                                            if (!blockLayoutRequests) {
                                                gridView.mBlockLayoutRequests = false;
                                            }
                                            throw th;
                                        }
                                    }
                                    AbsListView.RecycleBin recycleBin4 = recycleBin;
                                    int i14 = accessibilityFocusPosition;
                                    gridView = this;
                                }
                                gridView.mLayoutMode = 0;
                                gridView.mDataChanged = false;
                                if (gridView.mPositionScrollAfterLayout != null) {
                                    gridView.post(gridView.mPositionScrollAfterLayout);
                                    gridView.mPositionScrollAfterLayout = null;
                                }
                                gridView.mNeedSync = false;
                                gridView.setNextSelectedPositionInt(gridView.mSelectedPosition);
                                updateScrollIndicators();
                                if (gridView.mItemCount > 0) {
                                    checkSelectionChanged();
                                }
                                invokeOnItemScrollListener();
                                if (!blockLayoutRequests) {
                                    gridView.mBlockLayoutRequests = false;
                                }
                            }
                        }
                        accessibilityFocusLayoutRestoreView = focusHost;
                        accessibilityFocusLayoutRestoreNode = viewRootImpl.getAccessibilityFocusedVirtualView();
                        accessibilityFocusPosition2 = gridView.getPositionForView(focusChild);
                        int firstPosition2 = gridView.mFirstPosition;
                        recycleBin = gridView.mRecycler;
                        if (i5 != 0) {
                        }
                        detachAllViewsFromParent();
                        recycleBin.removeSkippedScrap();
                        switch (gridView.mLayoutMode) {
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                            case 5:
                                break;
                            case 6:
                                break;
                        }
                        recycleBin.scrapActiveViews();
                        if (sel != null) {
                        }
                        if (viewRootImpl != null) {
                        }
                        AbsListView.RecycleBin recycleBin22 = recycleBin;
                        int i132 = accessibilityFocusPosition;
                        gridView.mLayoutMode = 0;
                        gridView.mDataChanged = false;
                        if (gridView.mPositionScrollAfterLayout != null) {
                        }
                        gridView.mNeedSync = false;
                        gridView.setNextSelectedPositionInt(gridView.mSelectedPosition);
                        updateScrollIndicators();
                        if (gridView.mItemCount > 0) {
                        }
                        invokeOnItemScrollListener();
                        if (!blockLayoutRequests) {
                        }
                    }
                }
            }
            accessibilityFocusLayoutRestoreNode = null;
            int firstPosition22 = gridView.mFirstPosition;
            recycleBin = gridView.mRecycler;
            if (i5 != 0) {
            }
            detachAllViewsFromParent();
            recycleBin.removeSkippedScrap();
            switch (gridView.mLayoutMode) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
            }
            recycleBin.scrapActiveViews();
            if (sel != null) {
            }
            if (viewRootImpl != null) {
            }
            AbsListView.RecycleBin recycleBin222 = recycleBin;
            int i1322 = accessibilityFocusPosition;
            gridView.mLayoutMode = 0;
            gridView.mDataChanged = false;
            if (gridView.mPositionScrollAfterLayout != null) {
            }
            gridView.mNeedSync = false;
            gridView.setNextSelectedPositionInt(gridView.mSelectedPosition);
            updateScrollIndicators();
            if (gridView.mItemCount > 0) {
            }
            invokeOnItemScrollListener();
            if (!blockLayoutRequests) {
            }
        }
    }

    private View makeAndAddView(int position, int y, boolean flow, int childrenLeft, boolean selected, int where) {
        int i = position;
        if (!this.mDataChanged) {
            View activeView = this.mRecycler.getActiveView(i);
            if (activeView != null) {
                setupChild(activeView, i, y, flow, childrenLeft, selected, true, where);
                return activeView;
            }
        }
        View child = obtainView(i, this.mIsScrap);
        setupChild(child, i, y, flow, childrenLeft, selected, this.mIsScrap[0], where);
        return child;
    }

    /* JADX WARNING: type inference failed for: r16v0, types: [android.view.ViewGroup$LayoutParams] */
    /* JADX WARNING: Multi-variable type inference failed */
    private void setupChild(View child, int position, int y, boolean flowDown, int childrenLeft, boolean selected, boolean isAttachedToWindow, int where) {
        int i;
        int childLeft;
        View view = child;
        int i2 = position;
        int i3 = where;
        Trace.traceBegin(8, "setupGridItem");
        boolean isSelected = selected && shouldShowSelector();
        boolean updateChildSelected = isSelected != child.isSelected();
        int mode = this.mTouchMode;
        boolean isPressed = mode > 0 && mode < 3 && this.mMotionPosition == i2;
        boolean updateChildPressed = isPressed != child.isPressed();
        boolean needToMeasure = !isAttachedToWindow || updateChildSelected || child.isLayoutRequested();
        AbsListView.LayoutParams p = (AbsListView.LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = generateDefaultLayoutParams();
        }
        AbsListView.LayoutParams p2 = p;
        p2.viewType = this.mAdapter.getItemViewType(i2);
        p2.isEnabled = this.mAdapter.isEnabled(i2);
        if (updateChildSelected) {
            view.setSelected(isSelected);
            if (isSelected) {
                requestFocus();
            }
        }
        if (updateChildPressed) {
            view.setPressed(isPressed);
        }
        if (!(this.mChoiceMode == 0 || this.mCheckStates == null)) {
            if (view instanceof Checkable) {
                ((Checkable) view).setChecked(this.mCheckStates.get(i2));
            } else if (getContext().getApplicationInfo().targetSdkVersion >= 11) {
                view.setActivated(this.mCheckStates.get(i2));
            }
        }
        if (!isAttachedToWindow || p2.forceAdd) {
            i = 0;
            p2.forceAdd = false;
            addViewInLayout(view, i3, p2, true);
        } else {
            attachViewToParent(view, i3, p2);
            if (!isAttachedToWindow || ((AbsListView.LayoutParams) child.getLayoutParams()).scrappedFromPosition != i2) {
                child.jumpDrawablesToCurrentState();
            }
            i = 0;
        }
        if (needToMeasure) {
            view.measure(ViewGroup.getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(this.mColumnWidth, 1073741824), 0, p2.width), ViewGroup.getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(i, i), i, p2.height));
        } else {
            cleanupLayoutState(child);
        }
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childTop = flowDown ? y : y - h;
        int layoutDirection = getLayoutDirection();
        int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity, layoutDirection);
        int i4 = layoutDirection;
        int layoutDirection2 = absoluteGravity & 7;
        int i5 = absoluteGravity;
        if (layoutDirection2 == 1) {
            childLeft = childrenLeft + ((this.mColumnWidth - w) / 2);
        } else if (layoutDirection2 == 3) {
            childLeft = childrenLeft;
        } else if (layoutDirection2 != 5) {
            childLeft = childrenLeft;
        } else {
            childLeft = (childrenLeft + this.mColumnWidth) - w;
        }
        if (needToMeasure) {
            int childRight = childLeft + w;
            int i6 = w;
            int childTop2 = childTop;
            view.layout(childLeft, childTop2, childRight, childTop2 + h);
        } else {
            view.offsetLeftAndRight(childLeft - child.getLeft());
            view.offsetTopAndBottom(childTop - child.getTop());
        }
        if (this.mCachingStarted && !child.isDrawingCacheEnabled()) {
            view.setDrawingCacheEnabled(true);
        }
        Trace.traceEnd(8);
    }

    public void setSelection(int position) {
        if (!isInTouchMode()) {
            setNextSelectedPositionInt(position);
        } else {
            this.mResurrectToPosition = position;
        }
        this.mLayoutMode = 2;
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        requestLayout();
    }

    /* access modifiers changed from: package-private */
    public void setSelectionInt(int position) {
        int next;
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
        if (next / this.mNumColumns != (this.mStackFromBottom ? (this.mItemCount - 1) - previousSelectedPosition : previousSelectedPosition) / this.mNumColumns) {
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
        if (!handled && action != 1) {
            switch (keyCode) {
                case 19:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            handled = resurrectSelectionIfNeeded() || fullScroll(33);
                            break;
                        }
                    } else {
                        handled = resurrectSelectionIfNeeded() || arrowScroll(33);
                        break;
                    }
                    break;
                case 20:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            handled = resurrectSelectionIfNeeded() || fullScroll(130);
                            break;
                        }
                    } else {
                        handled = resurrectSelectionIfNeeded() || arrowScroll(130);
                        break;
                    }
                    break;
                case 21:
                    if (event.hasNoModifiers()) {
                        handled = resurrectSelectionIfNeeded() || arrowScroll(17);
                        break;
                    }
                    break;
                case 22:
                    if (event.hasNoModifiers()) {
                        handled = resurrectSelectionIfNeeded() || arrowScroll(66);
                        break;
                    }
                    break;
                case 61:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(1)) {
                            handled = resurrectSelectionIfNeeded() || sequenceScroll(1);
                            break;
                        }
                    } else {
                        handled = resurrectSelectionIfNeeded() || sequenceScroll(2);
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

    /* access modifiers changed from: package-private */
    public boolean fullScroll(int direction) {
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

    /* access modifiers changed from: package-private */
    public boolean arrowScroll(int direction) {
        int endOfRowPos;
        int startOfRowPos;
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        boolean moved = false;
        if (!this.mStackFromBottom) {
            startOfRowPos = (selectedPosition / numColumns) * numColumns;
            endOfRowPos = Math.min((startOfRowPos + numColumns) - 1, this.mItemCount - 1);
        } else {
            endOfRowPos = (this.mItemCount - 1) - ((((this.mItemCount - 1) - selectedPosition) / numColumns) * numColumns);
            startOfRowPos = Math.max(0, (endOfRowPos - numColumns) + 1);
        }
        if (direction != 33) {
            if (direction == 130 && endOfRowPos < this.mItemCount - 1) {
                this.mLayoutMode = 6;
                setSelectionInt(Math.min(selectedPosition + numColumns, this.mItemCount - 1));
                moved = true;
            }
        } else if (startOfRowPos > 0) {
            this.mLayoutMode = 6;
            setSelectionInt(Math.max(0, selectedPosition - numColumns));
            moved = true;
        }
        boolean isLayoutRtl = isLayoutRtl();
        if (selectedPosition > startOfRowPos && ((direction == 17 && !isLayoutRtl) || (direction == 66 && isLayoutRtl))) {
            this.mLayoutMode = 6;
            setSelectionInt(Math.max(0, selectedPosition - 1));
            moved = true;
        } else if (selectedPosition < endOfRowPos && ((direction == 17 && isLayoutRtl) || (direction == 66 && !isLayoutRtl))) {
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

    /* access modifiers changed from: package-private */
    public boolean sequenceScroll(int direction) {
        int endOfRow;
        int startOfRow;
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        int count = this.mItemCount;
        boolean z = false;
        if (!this.mStackFromBottom) {
            startOfRow = (selectedPosition / numColumns) * numColumns;
            endOfRow = Math.min((startOfRow + numColumns) - 1, count - 1);
        } else {
            endOfRow = (count - 1) - ((((count - 1) - selectedPosition) / numColumns) * numColumns);
            startOfRow = Math.max(0, (endOfRow - numColumns) + 1);
        }
        boolean moved = false;
        boolean showScroll = false;
        switch (direction) {
            case 1:
                if (selectedPosition > 0) {
                    this.mLayoutMode = 6;
                    setSelectionInt(selectedPosition - 1);
                    moved = true;
                    if (selectedPosition == startOfRow) {
                        z = true;
                    }
                    showScroll = z;
                    break;
                }
                break;
            case 2:
                if (selectedPosition < count - 1) {
                    this.mLayoutMode = 6;
                    setSelectionInt(selectedPosition + 1);
                    moved = true;
                    if (selectedPosition == endOfRow) {
                        z = true;
                    }
                    showScroll = z;
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

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
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
                    int distance = getDistance(previouslyFocusedRect, otherRect, direction);
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
        int count;
        int rowEnd;
        int rowStart;
        int invertedIndex = (getChildCount() - 1) - childIndex;
        boolean z = false;
        if (!this.mStackFromBottom) {
            rowStart = childIndex - (childIndex % this.mNumColumns);
            rowEnd = Math.min((this.mNumColumns + rowStart) - 1, count);
        } else {
            rowEnd = (count - 1) - (invertedIndex - (invertedIndex % this.mNumColumns));
            rowStart = Math.max(0, (rowEnd - this.mNumColumns) + 1);
        }
        if (direction == 17) {
            if (childIndex == rowEnd) {
                z = true;
            }
            return z;
        } else if (direction == 33) {
            if (rowEnd == count - 1) {
                z = true;
            }
            return z;
        } else if (direction == 66) {
            if (childIndex == rowStart) {
                z = true;
            }
            return z;
        } else if (direction != 130) {
            switch (direction) {
                case 1:
                    if (childIndex == rowEnd && rowEnd == count - 1) {
                        z = true;
                    }
                    return z;
                case 2:
                    if (childIndex == rowStart && rowStart == 0) {
                        z = true;
                    }
                    return z;
                default:
                    throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
            }
        } else {
            if (rowStart == 0) {
                z = true;
            }
            return z;
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

    @ViewDebug.ExportedProperty
    public int getNumColumns() {
        return this.mNumColumns;
    }

    private void adjustViewsUpOrDown() {
        int delta;
        int childCount = getChildCount();
        if (childCount > 0) {
            if (!this.mStackFromBottom) {
                delta = getChildAt(0).getTop() - this.mListPadding.top;
                if (this.mFirstPosition != 0) {
                    delta -= this.mVerticalSpacing;
                }
                if (delta < 0) {
                    delta = 0;
                }
            } else {
                int delta2 = getChildAt(childCount - 1).getBottom() - (getHeight() - this.mListPadding.bottom);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta2 += this.mVerticalSpacing;
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

    /* access modifiers changed from: protected */
    public int computeVerticalScrollExtent() {
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
        View view2 = getChildAt(count - 1);
        int bottom = view2.getBottom();
        int height2 = view2.getHeight();
        if (height2 > 0) {
            extent -= ((bottom - getHeight()) * 100) / height2;
        }
        return extent;
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollOffset() {
        int oddItemsOnFirstRow;
        if (this.mFirstPosition >= 0 && getChildCount() > 0) {
            View view = getChildAt(0);
            int top = view.getTop();
            int height = view.getHeight();
            if (height > 0) {
                int numColumns = this.mNumColumns;
                int rowCount = ((this.mItemCount + numColumns) - 1) / numColumns;
                if (isStackFromBottom()) {
                    oddItemsOnFirstRow = (rowCount * numColumns) - this.mItemCount;
                } else {
                    oddItemsOnFirstRow = 0;
                }
                return Math.max(((((this.mFirstPosition + oddItemsOnFirstRow) / numColumns) * 100) - ((top * 100) / height)) + ((int) ((((float) this.mScrollY) / ((float) getHeight())) * ((float) rowCount) * 100.0f)), 0);
            }
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int computeVerticalScrollRange() {
        int numColumns = this.mNumColumns;
        int rowCount = ((this.mItemCount + numColumns) - 1) / numColumns;
        int result = Math.max(rowCount * 100, 0);
        if (this.mScrollY != 0) {
            return result + Math.abs((int) ((((float) this.mScrollY) / ((float) getHeight())) * ((float) rowCount) * 100.0f));
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
        info.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(rowsCount, columnsCount, false, getSelectionModeForAccessibility()));
        if (columnsCount > 0 || rowsCount > 0) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_TO_POSITION);
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (super.performAccessibilityActionInternal(action, arguments)) {
            return true;
        }
        if (action == 16908343) {
            int numColumns = getNumColumns();
            int row = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, -1);
            int position = Math.min(row * numColumns, getCount() - 1);
            if (row >= 0) {
                smoothScrollToPosition(position);
                return true;
            }
        }
        return false;
    }

    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        int row;
        int column;
        super.onInitializeAccessibilityNodeInfoForItem(view, position, info);
        int count = getCount();
        int columnsCount = getNumColumns();
        int rowsCount = count / columnsCount;
        if (!this.mStackFromBottom) {
            column = position % columnsCount;
            row = position / columnsCount;
        } else {
            int invertedIndex = (count - 1) - position;
            row = (rowsCount - 1) - (invertedIndex / columnsCount);
            column = (columnsCount - 1) - (invertedIndex % columnsCount);
        }
        AbsListView.LayoutParams lp = (AbsListView.LayoutParams) view.getLayoutParams();
        info.setCollectionItemInfo(AccessibilityNodeInfo.CollectionItemInfo.obtain(row, 1, column, 1, lp != null && lp.viewType == -2, isItemChecked(position)));
    }

    /* access modifiers changed from: protected */
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("numColumns", getNumColumns());
    }

    /* access modifiers changed from: protected */
    public int getPressedStateDuration() {
        return this.mPressedStateDuration;
    }
}
