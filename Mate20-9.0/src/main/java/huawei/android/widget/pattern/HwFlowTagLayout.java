package huawei.android.widget.pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.ArrayList;
import java.util.List;

public class HwFlowTagLayout extends ViewGroup {
    /* access modifiers changed from: private */
    public int mHorizontalSpacing = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_m");
    private Line mLine = null;
    private final List<Line> mLines = new ArrayList();
    private int mMaxLinesCount = 3;
    private int mUsedWidth = 0;
    private int mVerticalSpacing = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_m");

    class Line {
        /* access modifiers changed from: private */
        public int mHeight = 0;
        private int mWidth = 0;
        private List<View> views = new ArrayList();

        Line() {
        }

        /* access modifiers changed from: protected */
        public void addView(View child) {
            this.views.add(child);
            this.mWidth += child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            this.mHeight = this.mHeight < childHeight ? childHeight : this.mHeight;
        }

        /* access modifiers changed from: protected */
        public void layout(int l, int t) {
            int i;
            int left = l;
            int count = getViewCount();
            int parentWidth = HwFlowTagLayout.this.getMeasuredWidth();
            boolean z = true;
            if (HwFlowTagLayout.this.getLayoutDirection() != 1) {
                z = false;
            }
            boolean isRtl = z;
            for (int i2 = 0; i2 < count; i2++) {
                View child = this.views.get(i2);
                int childHeight = child.getMeasuredHeight();
                int childWidth = child.getMeasuredWidth();
                if (isRtl) {
                    child.layout(parentWidth - (left + childWidth), t, parentWidth - left, t + childHeight);
                    i = HwFlowTagLayout.this.mHorizontalSpacing;
                } else {
                    child.layout(left, t, left + childWidth, t + childHeight);
                    i = HwFlowTagLayout.this.mHorizontalSpacing;
                }
                left += i + childWidth;
            }
        }

        /* access modifiers changed from: protected */
        public int getViewCount() {
            return this.views.size();
        }

        /* access modifiers changed from: protected */
        public int getHeight() {
            return this.mHeight;
        }
    }

    public HwFlowTagLayout(Context context) {
        super(context);
    }

    public HwFlowTagLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = (View.MeasureSpec.getSize(heightMeasureSpec) - getPaddingBottom()) - getPaddingTop();
        int widthSize = (View.MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()) - getPaddingRight();
        restoreLine();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int i2 = Integer.MIN_VALUE;
            int widthSpec = View.MeasureSpec.makeMeasureSpec(widthSize, widthMode == 1073741824 ? Integer.MIN_VALUE : widthMode);
            if (heightMode != 1073741824) {
                i2 = heightMode;
            }
            child.measure(widthSpec, View.MeasureSpec.makeMeasureSpec(heightSize, i2));
            if (this.mLine == null) {
                this.mLine = new Line();
            }
            int childWidth = child.getMeasuredWidth();
            this.mUsedWidth += childWidth;
            if (this.mUsedWidth < widthSize) {
                this.mLine.addView(child);
                this.mUsedWidth += this.mHorizontalSpacing;
            } else if (this.mLine.getViewCount() == 0) {
                this.mLine.addView(child);
            } else if (newLine()) {
                this.mLine.addView(child);
                this.mUsedWidth += this.mHorizontalSpacing + childWidth;
            }
        }
        if (this.mLine != null && this.mLine.getViewCount() > 0 && !this.mLines.contains(this.mLine)) {
            this.mLines.add(this.mLine);
        }
        int totoalHeight = 0;
        int linesNum = this.mLines.size();
        for (int i3 = 0; i3 < linesNum; i3++) {
            totoalHeight += this.mLines.get(i3).getHeight();
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), resolveSize(totoalHeight + ((this.mLines.size() - 1) * this.mVerticalSpacing) + getPaddingBottom() + getPaddingTop(), heightMeasureSpec));
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = this.mLines.size();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        for (int i = 0; i < count; i++) {
            Line line = this.mLines.get(i);
            line.layout(left, top);
            top += this.mVerticalSpacing + line.mHeight;
        }
    }

    private void restoreLine() {
        this.mLines.clear();
        this.mLine = new Line();
        this.mUsedWidth = 0;
    }

    private boolean newLine() {
        if (this.mLines.size() >= this.mMaxLinesCount - 1) {
            return false;
        }
        this.mLines.add(this.mLine);
        this.mLine = new Line();
        this.mUsedWidth = 0;
        return true;
    }
}
