package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

    public static class LayoutParams extends LinearLayout.LayoutParams {
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

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
            this.width = -1;
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
            this.width = -1;
            if (source instanceof LayoutParams) {
                this.weight = ((LayoutParams) source).weight;
            }
        }

        /* access modifiers changed from: protected */
        public void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
            this.width = -1;
            if (a.hasValue(heightAttr)) {
                this.height = a.getLayoutDimension(heightAttr, "layout_height");
            } else {
                this.height = -2;
            }
        }
    }

    private class PassThroughHierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {
        /* access modifiers changed from: private */
        public ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;

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
        this.mPassThroughListener = new PassThroughHierarchyChangeListener();
        super.setOnHierarchyChangeListener(this.mPassThroughListener);
        this.mInitialized = true;
    }

    public void setOnHierarchyChangeListener(ViewGroup.OnHierarchyChangeListener listener) {
        ViewGroup.OnHierarchyChangeListener unused = this.mPassThroughListener.mOnHierarchyChangeListener = listener;
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
        return this.mStretchAllColumns || this.mStretchableColumns.get(columnIndex);
    }

    public void setColumnShrinkable(int columnIndex, boolean isShrinkable) {
        this.mShrinkableColumns.put(columnIndex, isShrinkable);
        requestRowsLayout();
    }

    public boolean isColumnShrinkable(int columnIndex) {
        return this.mShrinkAllColumns || this.mShrinkableColumns.get(columnIndex);
    }

    /* access modifiers changed from: private */
    public void trackCollapsedColumns(View child) {
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

    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        requestRowsLayout();
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        requestRowsLayout();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureVertical(widthMeasureSpec, heightMeasureSpec);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutVertical(l, t, r, b);
    }

    /* access modifiers changed from: package-private */
    public void measureChildBeforeLayout(View child, int childIndex, int widthMeasureSpec, int totalWidth, int heightMeasureSpec, int totalHeight) {
        if (child instanceof TableRow) {
            ((TableRow) child).setColumnsWidthConstraints(this.mMaxWidths);
        }
        super.measureChildBeforeLayout(child, childIndex, widthMeasureSpec, totalWidth, heightMeasureSpec, totalHeight);
    }

    /* access modifiers changed from: package-private */
    public void measureVertical(int widthMeasureSpec, int heightMeasureSpec) {
        findLargestCells(widthMeasureSpec, heightMeasureSpec);
        shrinkAndStretchColumns(widthMeasureSpec);
        super.measureVertical(widthMeasureSpec, heightMeasureSpec);
    }

    private void findLargestCells(int widthMeasureSpec, int heightMeasureSpec) {
        int count;
        int count2 = getChildCount();
        int i = 0;
        boolean firstRow = true;
        int i2 = 0;
        while (i2 < count2) {
            View child = getChildAt(i2);
            if (child.getVisibility() == 8) {
                int i3 = widthMeasureSpec;
                int i4 = heightMeasureSpec;
            } else {
                if (child instanceof TableRow) {
                    TableRow row = (TableRow) child;
                    row.getLayoutParams().height = -2;
                    int[] widths = row.getColumnsWidths(widthMeasureSpec, heightMeasureSpec);
                    int newLength = widths.length;
                    if (firstRow) {
                        if (this.mMaxWidths == null || this.mMaxWidths.length != newLength) {
                            this.mMaxWidths = new int[newLength];
                        }
                        System.arraycopy(widths, i, this.mMaxWidths, i, newLength);
                        firstRow = false;
                    } else {
                        int length = this.mMaxWidths.length;
                        int difference = newLength - length;
                        if (difference > 0) {
                            int[] oldMaxWidths = this.mMaxWidths;
                            this.mMaxWidths = new int[newLength];
                            count = count2;
                            System.arraycopy(oldMaxWidths, i, this.mMaxWidths, i, oldMaxWidths.length);
                            System.arraycopy(widths, oldMaxWidths.length, this.mMaxWidths, oldMaxWidths.length, difference);
                        } else {
                            count = count2;
                        }
                        int[] maxWidths = this.mMaxWidths;
                        int length2 = Math.min(length, newLength);
                        for (int j = 0; j < length2; j++) {
                            maxWidths[j] = Math.max(maxWidths[j], widths[j]);
                        }
                    }
                } else {
                    int i5 = widthMeasureSpec;
                    int i6 = heightMeasureSpec;
                    count = count2;
                }
                i2++;
                count2 = count;
                i = 0;
            }
            count = count2;
            i2++;
            count2 = count;
            i = 0;
        }
        int i7 = widthMeasureSpec;
        int i8 = heightMeasureSpec;
        int i9 = count2;
    }

    private void shrinkAndStretchColumns(int widthMeasureSpec) {
        if (this.mMaxWidths != null) {
            int totalWidth = 0;
            for (int width : this.mMaxWidths) {
                totalWidth += width;
            }
            int size = (View.MeasureSpec.getSize(widthMeasureSpec) - this.mPaddingLeft) - this.mPaddingRight;
            if (totalWidth > size && (this.mShrinkAllColumns || this.mShrinkableColumns.size() > 0)) {
                mutateColumnsWidth(this.mShrinkableColumns, this.mShrinkAllColumns, size, totalWidth);
            } else if (totalWidth < size && (this.mStretchAllColumns || this.mStretchableColumns.size() > 0)) {
                mutateColumnsWidth(this.mStretchableColumns, this.mStretchAllColumns, size, totalWidth);
            }
        }
    }

    private void mutateColumnsWidth(SparseBooleanArray columns, boolean allColumns, int size, int totalWidth) {
        int[] maxWidths = this.mMaxWidths;
        int length = maxWidths.length;
        int count = allColumns ? length : columns.size();
        int extraSpace = (size - totalWidth) / count;
        int nbChildren = getChildCount();
        for (int i = 0; i < nbChildren; i++) {
            View child = getChildAt(i);
            if (child instanceof TableRow) {
                child.forceLayout();
            }
        }
        if (!allColumns) {
            int skipped = 0;
            for (int i2 = 0; i2 < count; i2++) {
                int column = columns.keyAt(i2);
                if (columns.valueAt(i2)) {
                    if (column < length) {
                        maxWidths[column] = maxWidths[column] + extraSpace;
                    } else {
                        skipped++;
                    }
                }
            }
            if (skipped > 0 && skipped < count) {
                int extraSpace2 = (skipped * extraSpace) / (count - skipped);
                for (int i3 = 0; i3 < count; i3++) {
                    int column2 = columns.keyAt(i3);
                    if (columns.valueAt(i3) && column2 < length) {
                        if (extraSpace2 > maxWidths[column2]) {
                            maxWidths[column2] = 0;
                        } else {
                            maxWidths[column2] = maxWidths[column2] + extraSpace2;
                        }
                    }
                }
            }
            return;
        }
        for (int i4 = 0; i4 < count; i4++) {
            maxWidths[i4] = maxWidths[i4] + extraSpace;
        }
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    public LinearLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
    public LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public CharSequence getAccessibilityClassName() {
        return TableLayout.class.getName();
    }
}
