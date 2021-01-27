package android.widget;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.util.SparseArray;
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
import android.view.inspector.InspectionCompanion;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import android.widget.AbsListView;
import android.widget.RemoteViews;
import com.android.internal.R;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.function.IntFunction;

@RemoteViews.RemoteView
public class GridView extends AbsListView {
    public static final int AUTO_FIT = -1;
    public static final int NO_STRETCH = 0;
    public static final int STRETCH_COLUMN_WIDTH = 2;
    public static final int STRETCH_SPACING = 1;
    public static final int STRETCH_SPACING_UNIFORM = 3;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 117521079)
    private int mColumnWidth;
    private int mGravity;
    @UnsupportedAppUsage
    private int mHorizontalSpacing;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 117521080)
    private int mNumColumns;
    private int mPressedStateDuration;
    private View mReferenceView;
    private View mReferenceViewInSelectedRow;
    @UnsupportedAppUsage
    private int mRequestedColumnWidth;
    @UnsupportedAppUsage
    private int mRequestedHorizontalSpacing;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769395)
    private int mRequestedNumColumns;
    private int mStretchMode;
    private final Rect mTempRect;
    @UnsupportedAppUsage
    private int mVerticalSpacing;

    @Retention(RetentionPolicy.SOURCE)
    public @interface StretchMode {
    }

    public final class InspectionCompanion implements android.view.inspector.InspectionCompanion<GridView> {
        private int mColumnWidthId;
        private int mGravityId;
        private int mHorizontalSpacingId;
        private int mNumColumnsId;
        private boolean mPropertiesMapped = false;
        private int mStretchModeId;
        private int mVerticalSpacingId;

        @Override // android.view.inspector.InspectionCompanion
        public void mapProperties(PropertyMapper propertyMapper) {
            this.mColumnWidthId = propertyMapper.mapInt("columnWidth", 16843031);
            this.mGravityId = propertyMapper.mapGravity("gravity", 16842927);
            this.mHorizontalSpacingId = propertyMapper.mapInt("horizontalSpacing", 16843028);
            this.mNumColumnsId = propertyMapper.mapInt("numColumns", 16843032);
            SparseArray<String> stretchModeEnumMapping = new SparseArray<>();
            stretchModeEnumMapping.put(0, "none");
            stretchModeEnumMapping.put(1, "spacingWidth");
            stretchModeEnumMapping.put(2, "columnWidth");
            stretchModeEnumMapping.put(3, "spacingWidthUniform");
            Objects.requireNonNull(stretchModeEnumMapping);
            this.mStretchModeId = propertyMapper.mapIntEnum("stretchMode", 16843030, new IntFunction() {
                /* class android.widget.$$Lambda$QY3N4tzLteuFdjRnyJFCbR1ajSI */

                @Override // java.util.function.IntFunction
                public final Object apply(int i) {
                    return (String) SparseArray.this.get(i);
                }
            });
            this.mVerticalSpacingId = propertyMapper.mapInt("verticalSpacing", 16843029);
            this.mPropertiesMapped = true;
        }

        public void readProperties(GridView node, PropertyReader propertyReader) {
            if (this.mPropertiesMapped) {
                propertyReader.readInt(this.mColumnWidthId, node.getColumnWidth());
                propertyReader.readGravity(this.mGravityId, node.getGravity());
                propertyReader.readInt(this.mHorizontalSpacingId, node.getHorizontalSpacing());
                propertyReader.readInt(this.mNumColumnsId, node.getNumColumns());
                propertyReader.readIntEnum(this.mStretchModeId, node.getStretchMode());
                propertyReader.readInt(this.mVerticalSpacingId, node.getVerticalSpacing());
                return;
            }
            throw new InspectionCompanion.UninitializedPropertyMapException();
        }
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
        saveAttributeDataForStyleable(context, R.styleable.GridView, attrs, a, defStyleAttr, defStyleRes);
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
        this.mPressedStateDuration = context.getResources().getInteger(com.android.hwext.internal.R.integer.config_gridview_pressed_state_duration);
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
    @Override // android.widget.AdapterView
    public int lookForSelectablePosition(int position, boolean lookDown) {
        if (this.mAdapter == null || isInTouchMode() || position < 0 || position >= this.mItemCount) {
            return -1;
        }
        return position;
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsListView
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

    @UnsupportedAppUsage(maxTargetSdk = 28)
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
        if (!this.mStackFromBottom) {
            startPos2 = startPos;
            last = Math.min(startPos + this.mNumColumns, this.mItemCount);
        } else {
            int last2 = startPos + 1;
            int startPos3 = Math.max(0, (startPos - this.mNumColumns) + 1);
            int i = last2 - startPos3;
            int i2 = this.mNumColumns;
            if (i < i2) {
                nextLeft += (isLayoutRtl ? -1 : 1) * (i2 - (last2 - startPos3)) * (columnWidth + horizontalSpacing);
                last = last2;
                startPos2 = startPos3;
            } else {
                last = last2;
                startPos2 = startPos3;
            }
        }
        boolean hasFocus = shouldShowSelector();
        boolean inClick = touchModeDrawsInPressedState();
        int selectedPosition = this.mSelectedPosition;
        int nextChildDir = isLayoutRtl ? -1 : 1;
        View selectedView = null;
        int nextLeft2 = nextLeft;
        int pos = startPos2;
        View child = null;
        while (pos < last) {
            boolean selected = pos == selectedPosition ? true : z;
            child = makeAndAddView(pos, y, flow, nextLeft2, selected, flow ? -1 : pos - startPos2);
            nextLeft2 += nextChildDir * columnWidth;
            if (pos < last - 1) {
                nextLeft2 += nextChildDir * horizontalSpacing;
            }
            if (selected && (hasFocus || inClick)) {
                selectedView = child;
            }
            pos++;
            selectedPosition = selectedPosition;
            z = false;
        }
        this.mReferenceView = child;
        if (selectedView != null) {
            this.mReferenceViewInSelectedRow = this.mReferenceView;
        }
        return selectedView;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28)
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
        int offset;
        if (this.mFirstPosition == 0 && (offset = childrenTop - getChildAt(0).getTop()) < 0) {
            offsetChildrenTopAndBottom(offset);
        }
    }

    private void pinToBottom(int childrenBottom) {
        int offset;
        int count = getChildCount();
        if (this.mFirstPosition + count == this.mItemCount && (offset = childrenBottom - getChildAt(count - 1).getBottom()) > 0) {
            offsetChildrenTopAndBottom(offset);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.widget.AbsListView
    public int findMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount <= 0) {
            return -1;
        }
        int numColumns = this.mNumColumns;
        if (!this.mStackFromBottom) {
            for (int i = 0; i < childCount; i += numColumns) {
                if (y <= getChildAt(i).getBottom()) {
                    return this.mFirstPosition + i;
                }
            }
            return -1;
        }
        for (int i2 = childCount - 1; i2 >= 0; i2 -= numColumns) {
            if (y >= getChildAt(i2).getTop()) {
                return this.mFirstPosition + i2;
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
        if ((rowStart + numColumns) - 1 < this.mItemCount - 1) {
            return childrenBottom - fadingEdgeLength;
        }
        return childrenBottom;
    }

    private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength, int rowStart) {
        if (rowStart > 0) {
            return childrenTop + fadingEdgeLength;
        }
        return childrenTop;
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

    /* JADX INFO: Multiple debug info for r10v0 int: [D('invertedSelection' int), D('rowDelta' int)] */
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
        int oldBottom = 0;
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
        int rowDelta = rowStart - oldRowStart;
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, rowStart);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, rowStart);
        this.mFirstPosition = rowStart;
        if (rowDelta > 0) {
            View view = this.mReferenceViewInSelectedRow;
            if (view != null) {
                oldBottom = view.getBottom();
            }
            View sel2 = makeRow(this.mStackFromBottom ? rowEnd : rowStart, oldBottom + verticalSpacing, true);
            referenceView = this.mReferenceView;
            adjustForBottomFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
            sel = sel2;
        } else if (rowDelta < 0) {
            View view2 = this.mReferenceViewInSelectedRow;
            sel = makeRow(this.mStackFromBottom ? rowEnd : rowStart, (view2 == null ? 0 : view2.getTop()) - verticalSpacing, false);
            referenceView = this.mReferenceView;
            adjustForTopFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        } else {
            View view3 = this.mReferenceViewInSelectedRow;
            if (view3 != null) {
                oldBottom = view3.getTop();
            }
            sel = makeRow(this.mStackFromBottom ? rowEnd : rowStart, oldBottom, true);
            referenceView = this.mReferenceView;
        }
        if (!this.mStackFromBottom) {
            fillUp(rowStart - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            fillDown(rowStart + numColumns, referenceView.getBottom() + verticalSpacing);
        } else {
            fillDown(rowEnd + numColumns, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
        }
        return sel;
    }

    @UnsupportedAppUsage
    private boolean determineColumns(int availableSpace) {
        int requestedHorizontalSpacing = this.mRequestedHorizontalSpacing;
        int stretchMode = this.mStretchMode;
        int requestedColumnWidth = this.mRequestedColumnWidth;
        boolean didNotInitiallyFit = false;
        int i = this.mRequestedNumColumns;
        if (i != -1) {
            this.mNumColumns = i;
        } else if (requestedColumnWidth > 0) {
            this.mNumColumns = (availableSpace + requestedHorizontalSpacing) / (requestedColumnWidth + requestedHorizontalSpacing);
        } else {
            this.mNumColumns = 2;
        }
        if (this.mNumColumns <= 0) {
            this.mNumColumns = 1;
        }
        if (stretchMode != 0) {
            int i2 = this.mNumColumns;
            int spaceLeftOver = (availableSpace - (i2 * requestedColumnWidth)) - ((i2 - 1) * requestedHorizontalSpacing);
            if (spaceLeftOver < 0) {
                didNotInitiallyFit = true;
            }
            if (stretchMode == 1) {
                this.mColumnWidth = requestedColumnWidth;
                int i3 = this.mNumColumns;
                if (i3 > 1) {
                    this.mHorizontalSpacing = (spaceLeftOver / (i3 - 1)) + requestedHorizontalSpacing;
                } else {
                    this.mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                }
            } else if (stretchMode == 2) {
                this.mColumnWidth = (spaceLeftOver / this.mNumColumns) + requestedColumnWidth;
                this.mHorizontalSpacing = requestedHorizontalSpacing;
            } else if (stretchMode == 3) {
                this.mColumnWidth = requestedColumnWidth;
                int i4 = this.mNumColumns;
                if (i4 > 1) {
                    this.mHorizontalSpacing = (spaceLeftOver / (i4 + 1)) + requestedHorizontalSpacing;
                } else {
                    this.mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                }
            }
        } else {
            this.mColumnWidth = requestedColumnWidth;
            this.mHorizontalSpacing = requestedHorizontalSpacing;
        }
        return didNotInitiallyFit;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int widthSize;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize2 = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == 0) {
            int i2 = this.mColumnWidth;
            if (i2 > 0) {
                widthSize = i2 + this.mListPadding.left + this.mListPadding.right;
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
                p = (AbsListView.LayoutParams) generateDefaultLayoutParams();
                child.setLayoutParams(p);
            }
            p.viewType = this.mAdapter.getItemViewType(0);
            p.isEnabled = this.mAdapter.isEnabled(0);
            p.forceAdd = true;
            child.measure(getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(this.mColumnWidth, 1073741824), 0, p.width), getChildMeasureSpec(View.MeasureSpec.makeSafeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec), 0), 0, p.height));
            childHeight = child.getMeasuredHeight();
            combineMeasuredStates(0, child.getMeasuredState());
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
            int i3 = 0;
            while (true) {
                if (i3 >= count) {
                    break;
                }
                ourSize += childHeight;
                if (i3 + numColumns < count) {
                    ourSize += this.mVerticalSpacing;
                }
                if (ourSize >= heightSize) {
                    ourSize = heightSize;
                    break;
                }
                i3 += numColumns;
            }
            heightSize = ourSize;
        }
        if (widthMode == Integer.MIN_VALUE && (i = this.mRequestedNumColumns) != -1 && ((this.mColumnWidth * i) + ((i - 1) * this.mHorizontalSpacing) + this.mListPadding.left + this.mListPadding.right > widthSize2 || didNotInitiallyFit)) {
            widthSize2 |= 16777216;
        }
        setMeasuredDimension(widthSize2, heightSize);
        this.mWidthMeasureSpec = widthMeasureSpec;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
        GridLayoutAnimationController.AnimationParameters animationParams = (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;
        if (animationParams == null) {
            animationParams = new GridLayoutAnimationController.AnimationParameters();
            params.layoutAnimationParameters = animationParams;
        }
        animationParams.count = count;
        animationParams.index = index;
        int i = this.mNumColumns;
        animationParams.columnsCount = i;
        animationParams.rowsCount = count / i;
        if (!this.mStackFromBottom) {
            int i2 = this.mNumColumns;
            animationParams.column = index % i2;
            animationParams.row = index / i2;
            return;
        }
        int invertedIndex = (count - 1) - index;
        int i3 = this.mNumColumns;
        animationParams.column = (i3 - 1) - (invertedIndex % i3);
        animationParams.row = (animationParams.rowsCount - 1) - (invertedIndex / this.mNumColumns);
    }

    /* JADX INFO: Multiple debug info for r11v1 boolean: [D('dataChanged' boolean), D('index' int)] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:165:0x02a4  */
    @Override // android.widget.AbsListView
    public void layoutChildren() {
        boolean blockLayoutRequests;
        Throwable th;
        AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode;
        boolean blockLayoutRequests2;
        View sel;
        int i;
        int i2;
        AccessibilityNodeInfo accessibilityFocusLayoutRestoreNode2;
        boolean blockLayoutRequests3 = this.mBlockLayoutRequests;
        if (!blockLayoutRequests3) {
            this.mBlockLayoutRequests = true;
        }
        try {
            super.layoutChildren();
            invalidate();
            if (this.mAdapter == null) {
                try {
                    resetList();
                    invokeOnItemScrollListener();
                    if (!blockLayoutRequests3) {
                        this.mBlockLayoutRequests = false;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    blockLayoutRequests = blockLayoutRequests3;
                    if (!blockLayoutRequests) {
                    }
                    throw th;
                }
            } else {
                int childrenTop = this.mListPadding.top;
                int childrenBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
                int childCount = getChildCount();
                int delta = 0;
                View oldSel = null;
                View oldFirst = null;
                View newSel = null;
                switch (this.mLayoutMode) {
                    case 1:
                    case 3:
                    case 4:
                    case 5:
                        break;
                    case 2:
                        int index = this.mNextSelectedPosition - this.mFirstPosition;
                        if (index >= 0 && index < childCount) {
                            newSel = getChildAt(index);
                            break;
                        }
                    case 6:
                        if (this.mNextSelectedPosition >= 0) {
                            delta = this.mNextSelectedPosition - this.mSelectedPosition;
                            break;
                        }
                        break;
                    default:
                        int index2 = this.mSelectedPosition - this.mFirstPosition;
                        if (index2 >= 0 && index2 < childCount) {
                            oldSel = getChildAt(index2);
                        }
                        oldFirst = getChildAt(0);
                        break;
                }
                boolean dataChanged = this.mDataChanged;
                if (dataChanged) {
                    handleDataChanged();
                }
                if (this.mItemCount == 0) {
                    resetList();
                    invokeOnItemScrollListener();
                    if (!blockLayoutRequests3) {
                        this.mBlockLayoutRequests = false;
                        return;
                    }
                    return;
                }
                setSelectedPositionInt(this.mNextSelectedPosition);
                View accessibilityFocusLayoutRestoreView = null;
                int accessibilityFocusPosition = -1;
                ViewRootImpl viewRootImpl = getViewRootImpl();
                if (viewRootImpl != null) {
                    View focusHost = viewRootImpl.getAccessibilityFocusedHost();
                    if (focusHost != null) {
                        View focusChild = getAccessibilityFocusedChild(focusHost);
                        if (focusChild != null) {
                            if (dataChanged && !focusChild.hasTransientState()) {
                                if (!this.mAdapterHasStableIds) {
                                    accessibilityFocusLayoutRestoreNode2 = null;
                                    accessibilityFocusPosition = getPositionForView(focusChild);
                                    accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode2;
                                }
                            }
                            accessibilityFocusLayoutRestoreView = focusHost;
                            accessibilityFocusLayoutRestoreNode2 = viewRootImpl.getAccessibilityFocusedVirtualView();
                            accessibilityFocusPosition = getPositionForView(focusChild);
                            accessibilityFocusLayoutRestoreNode = accessibilityFocusLayoutRestoreNode2;
                        } else {
                            accessibilityFocusLayoutRestoreNode = null;
                        }
                    } else {
                        accessibilityFocusLayoutRestoreNode = null;
                    }
                } else {
                    accessibilityFocusLayoutRestoreNode = null;
                }
                int firstPosition = this.mFirstPosition;
                AbsListView.RecycleBin recycleBin = this.mRecycler;
                if (dataChanged) {
                    int i3 = 0;
                    while (i3 < childCount) {
                        blockLayoutRequests = blockLayoutRequests3;
                        try {
                            recycleBin.addScrapView(getChildAt(i3), firstPosition + i3);
                            i3++;
                            dataChanged = dataChanged;
                            blockLayoutRequests3 = blockLayoutRequests;
                        } catch (Throwable th3) {
                            th = th3;
                            if (!blockLayoutRequests) {
                                this.mBlockLayoutRequests = false;
                            }
                            throw th;
                        }
                    }
                    blockLayoutRequests2 = blockLayoutRequests3;
                } else {
                    blockLayoutRequests2 = blockLayoutRequests3;
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
                        if (childCount != 0) {
                            if (this.mSelectedPosition < 0 || this.mSelectedPosition >= this.mItemCount) {
                                if (this.mFirstPosition >= this.mItemCount) {
                                    sel = fillSpecific(0, childrenTop);
                                    break;
                                } else {
                                    sel = fillSpecific(this.mFirstPosition, oldFirst == null ? childrenTop : oldFirst.getTop());
                                    break;
                                }
                            } else {
                                sel = fillSpecific(this.mSelectedPosition, oldSel == null ? childrenTop : oldSel.getTop());
                                break;
                            }
                        } else if (!this.mStackFromBottom) {
                            if (this.mAdapter != null) {
                                if (!isInTouchMode()) {
                                    i2 = 0;
                                    setSelectedPositionInt(i2);
                                    sel = fillFromTop(childrenTop);
                                    break;
                                }
                            }
                            i2 = -1;
                            setSelectedPositionInt(i2);
                            sel = fillFromTop(childrenTop);
                        } else {
                            int last = this.mItemCount - 1;
                            if (this.mAdapter != null) {
                                if (!isInTouchMode()) {
                                    i = last;
                                    setSelectedPositionInt(i);
                                    sel = fillFromBottom(last, childrenBottom);
                                    break;
                                }
                            }
                            i = -1;
                            setSelectedPositionInt(i);
                            sel = fillFromBottom(last, childrenBottom);
                        }
                        break;
                }
                recycleBin.scrapActiveViews();
                if (sel != null) {
                    positionSelector(-1, sel);
                    this.mSelectedTop = sel.getTop();
                } else {
                    if (this.mTouchMode > 0 && this.mTouchMode < 3) {
                        View child = getChildAt(this.mMotionPosition - this.mFirstPosition);
                        if (child != null) {
                            positionSelector(this.mMotionPosition, child);
                        }
                    } else if (this.mSelectedPosition != -1) {
                        View child2 = getChildAt(this.mSelectorPosition - this.mFirstPosition);
                        if (child2 != null) {
                            positionSelector(this.mSelectorPosition, child2);
                        }
                    } else {
                        this.mSelectedTop = 0;
                        this.mSelectorRect.setEmpty();
                    }
                }
                if (viewRootImpl != null) {
                    if (viewRootImpl.getAccessibilityFocusedHost() == null) {
                        if (accessibilityFocusLayoutRestoreView != null) {
                            if (accessibilityFocusLayoutRestoreView.isAttachedToWindow()) {
                                AccessibilityNodeProvider provider = accessibilityFocusLayoutRestoreView.getAccessibilityNodeProvider();
                                if (accessibilityFocusLayoutRestoreNode == null || provider == null) {
                                    accessibilityFocusLayoutRestoreView.requestAccessibilityFocus();
                                } else {
                                    provider.performAction(AccessibilityNodeInfo.getVirtualDescendantId(accessibilityFocusLayoutRestoreNode.getSourceNodeId()), 64, null);
                                }
                            }
                        }
                        if (accessibilityFocusPosition != -1) {
                            View restoreView = getChildAt(MathUtils.constrain(accessibilityFocusPosition - this.mFirstPosition, 0, getChildCount() - 1));
                            if (restoreView != null) {
                                restoreView.requestAccessibilityFocus();
                            }
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
                if (!blockLayoutRequests2) {
                    this.mBlockLayoutRequests = false;
                }
            }
        } catch (Throwable th4) {
            th = th4;
            blockLayoutRequests = blockLayoutRequests3;
            if (!blockLayoutRequests) {
            }
            throw th;
        }
    }

    private View makeAndAddView(int position, int y, boolean flow, int childrenLeft, boolean selected, int where) {
        View activeView;
        if (this.mDataChanged || (activeView = this.mRecycler.getActiveView(position)) == null) {
            View child = obtainView(position, this.mIsScrap);
            setupChild(child, position, y, flow, childrenLeft, selected, this.mIsScrap[0], where);
            return child;
        }
        setupChild(activeView, position, y, flow, childrenLeft, selected, true, where);
        return activeView;
    }

    /* JADX INFO: Multiple debug info for r4v7 int: [D('childBottom' int), D('w' int)] */
    private void setupChild(View child, int position, int y, boolean flowDown, int childrenLeft, boolean selected, boolean isAttachedToWindow, int where) {
        int childLeft;
        Trace.traceBegin(8, "setupGridItem");
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
        refreshViewByCheckStatus(child, position);
        if (!isAttachedToWindow || p.forceAdd) {
            p.forceAdd = false;
            addViewInLayout(child, where, p, true);
        } else {
            attachViewToParent(child, where, p);
            if (!isAttachedToWindow || ((AbsListView.LayoutParams) child.getLayoutParams()).scrappedFromPosition != position) {
                child.jumpDrawablesToCurrentState();
            }
        }
        if (needToMeasure) {
            child.measure(ViewGroup.getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(this.mColumnWidth, 1073741824), 0, p.width), ViewGroup.getChildMeasureSpec(View.MeasureSpec.makeMeasureSpec(0, 0), 0, p.height));
        } else {
            cleanupLayoutState(child);
        }
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childTop = flowDown ? y : y - h;
        int absoluteGravity = Gravity.getAbsoluteGravity(this.mGravity, getLayoutDirection()) & 7;
        if (absoluteGravity == 1) {
            childLeft = childrenLeft + ((this.mColumnWidth - w) / 2);
        } else if (absoluteGravity == 3) {
            childLeft = childrenLeft;
        } else if (absoluteGravity != 5) {
            childLeft = childrenLeft;
        } else {
            childLeft = (childrenLeft + this.mColumnWidth) - w;
        }
        if (needToMeasure) {
            child.layout(childLeft, childTop, childLeft + w, childTop + h);
        } else {
            child.offsetLeftAndRight(childLeft - child.getLeft());
            child.offsetTopAndBottom(childTop - child.getTop());
        }
        if (this.mCachingStarted && !child.isDrawingCacheEnabled()) {
            child.setDrawingCacheEnabled(true);
        }
        Trace.traceEnd(8);
    }

    @Override // android.widget.AdapterView
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
    @Override // android.widget.AbsListView
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
        int previous = this.mStackFromBottom ? (this.mItemCount - 1) - previousSelectedPosition : previousSelectedPosition;
        int i = this.mNumColumns;
        if (next / i != previous / i) {
            awakenScrollBars();
        }
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
        if (this.mAdapter == null) {
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
                handled = resurrectSelectionIfNeeded() || sequenceScroll(2);
            } else if (event.hasModifiers(1)) {
                handled = resurrectSelectionIfNeeded() || sequenceScroll(1);
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
    @UnsupportedAppUsage
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
        if (direction != 1) {
            if (direction == 2 && selectedPosition < count - 1) {
                this.mLayoutMode = 6;
                setSelectionInt(selectedPosition + 1);
                moved = true;
                if (selectedPosition == endOfRow) {
                    z = true;
                }
                showScroll = z;
            }
        } else if (selectedPosition > 0) {
            this.mLayoutMode = 6;
            setSelectionInt(selectedPosition - 1);
            moved = true;
            if (selectedPosition == startOfRow) {
                z = true;
            }
            showScroll = z;
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
    @Override // android.widget.AbsListView, android.view.View
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
        int rowStart;
        int rowEnd;
        int count = getChildCount();
        int invertedIndex = (count - 1) - childIndex;
        if (!this.mStackFromBottom) {
            int i = this.mNumColumns;
            rowStart = childIndex - (childIndex % i);
            rowEnd = Math.min((i + rowStart) - 1, count);
        } else {
            int i2 = this.mNumColumns;
            rowEnd = (count - 1) - (invertedIndex - (invertedIndex % i2));
            rowStart = Math.max(0, (rowEnd - i2) + 1);
        }
        if (direction == 1) {
            return childIndex == rowEnd && rowEnd == count + -1;
        }
        if (direction == 2) {
            return childIndex == rowStart && rowStart == 0;
        }
        if (direction == 17) {
            return childIndex == rowEnd;
        }
        if (direction == 33) {
            return rowEnd == count + -1;
        }
        if (direction == 66) {
            return childIndex == rowStart;
        }
        if (direction == 130) {
            return rowStart == 0;
        }
        throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
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
                delta = getChildAt(childCount - 1).getBottom() - (getHeight() - this.mListPadding.bottom);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta += this.mVerticalSpacing;
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
    @Override // android.widget.AbsListView, android.view.View
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
            return extent - (((bottom - getHeight()) * 100) / height2);
        }
        return extent;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.view.View
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
    @Override // android.widget.AbsListView, android.view.View
    public int computeVerticalScrollRange() {
        int numColumns = this.mNumColumns;
        int rowCount = ((this.mItemCount + numColumns) - 1) / numColumns;
        int result = Math.max(rowCount * 100, 0);
        if (this.mScrollY != 0) {
            return result + Math.abs((int) ((((float) this.mScrollY) / ((float) getHeight())) * ((float) rowCount) * 100.0f));
        }
        return result;
    }

    @Override // android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public CharSequence getAccessibilityClassName() {
        return GridView.class.getName();
    }

    @Override // android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        int columnsCount = getNumColumns();
        int rowsCount = getCount() / columnsCount;
        info.setCollectionInfo(AccessibilityNodeInfo.CollectionInfo.obtain(rowsCount, columnsCount, false, getSelectionModeForAccessibility()));
        if (columnsCount > 0 || rowsCount > 0) {
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
        int numColumns = getNumColumns();
        int row = arguments.getInt(AccessibilityNodeInfo.ACTION_ARGUMENT_ROW_INT, -1);
        int position = Math.min(row * numColumns, getCount() - 1);
        if (row < 0) {
            return false;
        }
        smoothScrollToPosition(position);
        return true;
    }

    @Override // android.widget.AbsListView
    public void onInitializeAccessibilityNodeInfoForItem(View view, int position, AccessibilityNodeInfo info) {
        int column;
        int invertedIndex;
        super.onInitializeAccessibilityNodeInfoForItem(view, position, info);
        int count = getCount();
        int columnsCount = getNumColumns();
        int rowsCount = count / columnsCount;
        if (!this.mStackFromBottom) {
            invertedIndex = position % columnsCount;
            column = position / columnsCount;
        } else {
            int invertedIndex2 = (count - 1) - position;
            column = (rowsCount - 1) - (invertedIndex2 / columnsCount);
            invertedIndex = (columnsCount - 1) - (invertedIndex2 % columnsCount);
        }
        AbsListView.LayoutParams lp = (AbsListView.LayoutParams) view.getLayoutParams();
        info.setCollectionItemInfo(AccessibilityNodeInfo.CollectionItemInfo.obtain(column, 1, invertedIndex, 1, lp != null && lp.viewType == -2, isItemChecked(position)));
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView, android.widget.AdapterView, android.view.ViewGroup, android.view.View
    public void encodeProperties(ViewHierarchyEncoder encoder) {
        super.encodeProperties(encoder);
        encoder.addProperty("numColumns", getNumColumns());
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.AbsListView
    public int getPressedStateDuration() {
        return this.mPressedStateDuration;
    }

    /* access modifiers changed from: protected */
    public void refreshViewByCheckStatus(View child, int position) {
    }

    /* access modifiers changed from: protected */
    public void setFirstVisiblePosition(int position) {
        if (this.mAdapter != null && position >= 0 && position < this.mAdapter.getCount()) {
            this.mFirstPosition = position;
        }
    }
}
