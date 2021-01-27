package huawei.com.android.internal.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatConsts;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.android.hwext.internal.R;

public class HwButtonBarLayout extends LinearLayout {
    private static final int AUTOSIZE_STEP_GRANULARITY_IN_SP = 1;
    private static final int CHILD_COUNT_ADJUST_FACTOR = 2;
    private static final boolean IS_DEBUG = false;
    private static final int MAX_TEXT_SIZE_IN_SP = 16;
    private static final int MIN_TEXT_SIZE_IN_SP = 9;
    private static final String TAG = "HwButtonBarLayout";
    private int mLastLayoutWidth = 0;
    private float mMaxTextSize;
    private float mMinTextSize;
    private int mOnMeasureCount = 0;
    private float mStepGranularity;

    public HwButtonBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.HwButtonBarLayout);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        this.mMinTextSize = (float) array.getDimensionPixelSize(1, Math.round(TypedValue.applyDimension(2, 9.0f, displayMetrics)));
        this.mMaxTextSize = (float) array.getDimensionPixelSize(0, Math.round(TypedValue.applyDimension(2, 16.0f, displayMetrics)));
        this.mStepGranularity = (float) array.getDimensionPixelSize(2, Math.round(TypedValue.applyDimension(2, 1.0f, displayMetrics)));
        array.recycle();
    }

    private float getMinTextSize() {
        float minTextSize = 0.0f;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !(child instanceof Button))) {
                float btnTextSize = ((Button) child).getTextSize();
                if (btnTextSize < minTextSize) {
                    minTextSize = btnTextSize;
                } else if (minTextSize == 0.0f) {
                    minTextSize = btnTextSize;
                }
            }
        }
        return minTextSize;
    }

    private void unifyBtnTextSize(float uniTextSize) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !(child instanceof Button))) {
                Button btn = (Button) child;
                btn.getTextSize();
                btn.setAutoSizeTextTypeWithDefaults(0);
                btn.setTextSize(0, uniTextSize);
            }
        }
    }

    public void restoreButtonBar(boolean isStacked) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !(child instanceof Button))) {
                Button btn = (Button) child;
                if (isStacked) {
                    btn.setAutoSizeTextTypeUniformWithConfiguration(Math.round(this.mMinTextSize), Math.round(this.mMaxTextSize), Math.round(this.mStepGranularity), 0);
                } else {
                    btn.setAutoSizeTextTypeWithDefaults(0);
                    btn.setTextSize(0, this.mMaxTextSize);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int initialWidthMeasureSpec;
        int layoutWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        if (layoutWidth > this.mLastLayoutWidth && isStacked()) {
            setStacked(false);
        }
        this.mLastLayoutWidth = layoutWidth;
        boolean isNeedsRemeasure = false;
        if (isStacked() || View.MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            initialWidthMeasureSpec = widthMeasureSpec;
        } else {
            initialWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(layoutWidth, FloatConsts.SIGN_BIT_MASK);
            isNeedsRemeasure = true;
        }
        super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec);
        if (!isStacked() && (-16777216 & getMeasuredWidthAndState()) == 16777216) {
            setStacked(true);
            isNeedsRemeasure = true;
        }
        if (isNeedsRemeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        if (isStacked()) {
            unifyBtnTextSize(getMinTextSize());
        }
        this.mOnMeasureCount++;
    }

    private void setStacked(boolean isStacked) {
        if (isStacked != isStacked()) {
            setOrientation(isStacked ? 1 : 0);
            setShowDividers(isStacked ? 0 : 2);
            for (int i = getChildCount() - 2; i >= 0; i--) {
                bringChildToFront(getChildAt(i));
            }
            restoreButtonBar(isStacked);
        }
    }

    public boolean isStacked() {
        return getOrientation() == 1;
    }

    /* access modifiers changed from: protected */
    public boolean shouldMeasureChildDivider() {
        return true;
    }
}
