package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
import com.android.internal.R;
import java.util.regex.Pattern;

public class TableLayout extends LinearLayout {
    private SparseBooleanArray mCollapsedColumns;
    private boolean mInitialized;
    private int[] mMaxWidths;
    private PassThroughHierarchyChangeListener mPassThroughListener;
    private boolean mShrinkAllColumns;
    private SparseBooleanArray mShrinkableColumns;
    private boolean mStretchAllColumns;
    private SparseBooleanArray mStretchableColumns;

    public static class LayoutParams extends android.widget.LinearLayout.LayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(-1, h);
        }

        public LayoutParams(int w, int h, float initWeight) {
            super(-1, h, initWeight);
        }

        public LayoutParams() {
            super(-1, -2);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
            this.width = -1;
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
            this.width = -1;
            if (source instanceof LayoutParams) {
                this.weight = ((LayoutParams) source).weight;
            }
        }

        protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
            this.width = -1;
            if (a.hasValue(heightAttr)) {
                this.height = a.getLayoutDimension(heightAttr, "layout_height");
            } else {
                this.height = -2;
            }
        }
    }

    private class PassThroughHierarchyChangeListener implements OnHierarchyChangeListener {
        private OnHierarchyChangeListener mOnHierarchyChangeListener;

        /* synthetic */ PassThroughHierarchyChangeListener(TableLayout this$0, PassThroughHierarchyChangeListener -this1) {
            this();
        }

        private PassThroughHierarchyChangeListener() {
        }

        public void onChildViewAdded(View parent, View child) {
            TableLayout.this.trackCollapsedColumns(child);
            if (this.mOnHierarchyChangeListener != null) {
                this.mOnHierarchyChangeListener.onChildViewAdded(parent, child);
            }
        }

        public void onChildViewRemoved(View parent, View child) {
            if (this.mOnHierarchyChangeListener != null) {
                this.mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
            }
        }
    }

    public TableLayout(Context context) {
        super(context);
        initTableLayout();
    }

    public TableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TableLayout);
        String stretchedColumns = a.getString(0);
        if (stretchedColumns != null) {
            if (stretchedColumns.charAt(0) == '*') {
                this.mStretchAllColumns = true;
            } else {
                this.mStretchableColumns = parseColumns(stretchedColumns);
            }
        }
        String shrinkedColumns = a.getString(1);
        if (shrinkedColumns != null) {
            if (shrinkedColumns.charAt(0) == '*') {
                this.mShrinkAllColumns = true;
            } else {
                this.mShrinkableColumns = parseColumns(shrinkedColumns);
            }
        }
        String collapsedColumns = a.getString(2);
        if (collapsedColumns != null) {
            this.mCollapsedColumns = parseColumns(collapsedColumns);
        }
        a.recycle();
        initTableLayout();
    }

    private static SparseBooleanArray parseColumns(String sequence) {
        SparseBooleanArray columns = new SparseBooleanArray();
        for (String columnIdentifier : Pattern.compile("\\s*,\\s*").split(sequence)) {
            try {
                int columnIndex = Integer.parseInt(columnIdentifier);
                if (columnIndex >= 0) {
                    columns.put(columnIndex, true);
                }
            } catch (NumberFormatException e) {
            }
        }
        return columns;
    }

    private void initTableLayout() {
        if (this.mCollapsedColumns == null) {
            this.mCollapsedColumns = new SparseBooleanArray();
        }
        if (this.mStretchableColumns == null) {
            this.mStretchableColumns = new SparseBooleanArray();
        }
        if (this.mShrinkableColumns == null) {
            this.mShrinkableColumns = new SparseBooleanArray();
        }
        setOrientation(1);
        this.mPassThroughListener = new PassThroughHierarchyChangeListener(this, null);
        super.setOnHierarchyChangeListener(this.mPassThroughListener);
        this.mInitialized = true;
    }

    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
        this.mPassThroughListener.mOnHierarchyChangeListener = listener;
    }

    private void requestRowsLayout() {
        if (this.mInitialized) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).requestLayout();
            }
        }
    }

    public void requestLayout() {
        if (this.mInitialized) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                getChildAt(i).forceLayout();
            }
        }
        super.requestLayout();
    }

    public boolean isShrinkAllColumns() {
        return this.mShrinkAllColumns;
    }

    public void setShrinkAllColumns(boolean shrinkAllColumns) {
        this.mShrinkAllColumns = shrinkAllColumns;
    }

    public boolean isStretchAllColumns() {
        return this.mStretchAllColumns;
    }

    public void setStretchAllColumns(boolean stretchAllColumns) {
        this.mStretchAllColumns = stretchAllColumns;
    }

    public void setColumnCollapsed(int columnIndex, boolean isCollapsed) {
        this.mCollapsedColumns.put(columnIndex, isCollapsed);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            if (view instanceof TableRow) {
                ((TableRow) view).setColumnCollapsed(columnIndex, isCollapsed);
            }
        }
        requestRowsLayout();
    }

    public boolean isColumnCollapsed(int columnIndex) {
        return this.mCollapsedColumns.get(columnIndex);
    }

    public void setColumnStretchable(int columnIndex, boolean isStretchable) {
        this.mStretchableColumns.put(columnIndex, isStretchable);
        requestRowsLayout();
    }

    public boolean isColumnStretchable(int columnIndex) {
        return !this.mStretchAllColumns ? this.mStretchableColumns.get(columnIndex) : true;
    }

    public void setColumnShrinkable(int columnIndex, boolean isShrinkable) {
        this.mShrinkableColumns.put(columnIndex, isShrinkable);
        requestRowsLayout();
    }

    public boolean isColumnShrinkable(int columnIndex) {
        return !this.mShrinkAllColumns ? this.mShrinkableColumns.get(columnIndex) : true;
    }

    private void trackCollapsedColumns(View child) {
        if (child instanceof TableRow) {
            TableRow row = (TableRow) child;
            SparseBooleanArray collapsedColumns = this.mCollapsedColumns;
            int count = collapsedColumns.size();
            for (int i = 0; i < count; i++) {
                int columnIndex = collapsedColumns.keyAt(i);
                boolean isCollapsed = collapsedColumns.valueAt(i);
                if (isCollapsed) {
                    row.setColumnCollapsed(columnIndex, isCollapsed);
                }
            }
        }
    }

    public void addView(View child) {
        super.addView(child);
        requestRowsLayout();
    }

    public void addView(View child, int index) {
        super.addView(child, index);
        requestRowsLayout();
    }

    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        requestRowsLayout();
    }

    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        requestRowsLayout();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureVertical(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutVertical(l, t, r, b);
    }

    void measureChildBeforeLayout(View child, int childIndex, int widthMeasureSpec, int totalWidth, int heightMeasureSpec, int totalHeight) {
        if (child instanceof TableRow) {
            ((TableRow) child).setColumnsWidthConstraints(this.mMaxWidths);
        }
        super.measureChildBeforeLayout(child, childIndex, widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        findLargestCells(widthMeasureSpec, heightMeasureSpec);
        shrinkAndStretchColumns(widthMeasureSpec);
        super.measureVertical(widthMeasureSpec, heightMeasureSpec);
    }

    private void findLargestCells(int widthMeasureSpec, int heightMeasureSpec) {
        boolean firstRow = true;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8 && (child instanceof TableRow)) {
                TableRow row = (TableRow) child;
                row.getLayoutParams().height = -2;
                int[] widths = row.getColumnsWidths(widthMeasureSpec, heightMeasureSpec);
                int newLength = widths.length;
                if (firstRow) {
                    if (this.mMaxWidths == null || this.mMaxWidths.length != newLength) {
                        this.mMaxWidths = new int[newLength];
                    }
                    System.arraycopy(widths, 0, this.mMaxWidths, 0, newLength);
                    firstRow = false;
                } else {
                    int length = this.mMaxWidths.length;
                    int difference = newLength - length;
                    if (difference > 0) {
                        int[] oldMaxWidths = this.mMaxWidths;
                        this.mMaxWidths = new int[newLength];
                        System.arraycopy(oldMaxWidths, 0, this.mMaxWidths, 0, oldMaxWidths.length);
                        System.arraycopy(widths, oldMaxWidths.length, this.mMaxWidths, oldMaxWidths.length, difference);
                    }
                    int[] maxWidths = this.mMaxWidths;
                    length = Math.min(length, newLength);
                    for (int j = 0; j < length; j++) {
                        maxWidths[j] = Math.max(maxWidths[j], widths[j]);
                    }
                }
            }
        }
    }

    private void shrinkAndStretchColumns(int widthMeasureSpec) {
        if (this.mMaxWidths != null) {
            int totalWidth = 0;
            for (int width : this.mMaxWidths) {
                totalWidth += width;
            }
            int size = (MeasureSpec.getSize(widthMeasureSpec) - this.mPaddingLeft) - this.mPaddingRight;
            if (totalWidth > size && (this.mShrinkAllColumns || this.mShrinkableColumns.size() > 0)) {
                mutateColumnsWidth(this.mShrinkableColumns, this.mShrinkAllColumns, size, totalWidth);
            } else if (totalWidth < size && (this.mStretchAllColumns || this.mStretchableColumns.size() > 0)) {
                mutateColumnsWidth(this.mStretchableColumns, this.mStretchAllColumns, size, totalWidth);
            }
        }
    }

    private void mutateColumnsWidth(SparseBooleanArray columns, boolean allColumns, int size, int totalWidth) {
        int i;
        int skipped = 0;
        int[] maxWidths = this.mMaxWidths;
        int length = maxWidths.length;
        int count = allColumns ? length : columns.size();
        int extraSpace = (size - totalWidth) / count;
        int nbChildren = getChildCount();
        for (i = 0; i < nbChildren; i++) {
            View child = getChildAt(i);
            if (child instanceof TableRow) {
                child.forceLayout();
            }
        }
        if (allColumns) {
            for (i = 0; i < count; i++) {
                maxWidths[i] = maxWidths[i] + extraSpace;
            }
            return;
        }
        int column;
        for (i = 0; i < count; i++) {
            column = columns.keyAt(i);
            if (columns.valueAt(i)) {
                if (column < length) {
                    maxWidths[column] = maxWidths[column] + extraSpace;
                } else {
                    skipped++;
                }
            }
        }
        if (skipped > 0 && skipped < count) {
            extraSpace = (skipped * extraSpace) / (count - skipped);
            for (i = 0; i < count; i++) {
                column = columns.keyAt(i);
                if (columns.valueAt(i) && column < length) {
                    if (extraSpace > maxWidths[column]) {
                        maxWidths[column] = 0;
                    } else {
                        maxWidths[column] = maxWidths[column] + extraSpace;
                    }
                }
            }
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected android.widget.LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.widget.LinearLayout.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public CharSequence getAccessibilityClassName() {
        return TableLayout.class.getName();
    }
}
