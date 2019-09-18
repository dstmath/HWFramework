package huawei.com.android.internal.app;

import android.content.Context;
import android.content.res.Resources;
import android.rms.iaware.AppTypeInfo;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class HwButtonBarLayout extends LinearLayout {
    private static final boolean DBG = false;
    private static final int MAX_TEXT_SIZE_IN_SP = 15;
    private static final int MIN_TEXT_SIZE_IN_SP = 9;
    private static final String TAG = "HwButtonBarLayout";
    private int lastLayoutWidth = 0;
    private int maxTextSizeInSp;
    private int minTextSizeInSp;
    private int onMeasureCnt = 0;

    public HwButtonBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.minTextSizeInSp = getDimensionFromRes(context, 34472197, 9);
        this.maxTextSizeInSp = getDimensionFromRes(context, 34472234, 15);
    }

    private static int getDimensionFromRes(Context context, int resId, int defVal) {
        int val = defVal;
        if (resId == 0) {
            return val;
        }
        try {
            return Math.round(context.getResources().getDimension(resId) / context.getResources().getDisplayMetrics().scaledDensity);
        } catch (Resources.NotFoundException | Exception e) {
            return val;
        }
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
                float textSize = btn.getTextSize();
                btn.setAutoSizeTextTypeWithDefaults(0);
                btn.setTextSize(0, uniTextSize);
            }
        }
    }

    public void restoreButtonBar(boolean stacked) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (!(child == null || child.getVisibility() == 8 || !(child instanceof Button))) {
                Button btn = (Button) child;
                if (stacked) {
                    btn.setAutoSizeTextTypeUniformWithConfiguration(this.minTextSizeInSp, this.maxTextSizeInSp, 1, 2);
                } else {
                    btn.setAutoSizeTextTypeWithDefaults(0);
                    btn.setTextSize(2, (float) this.maxTextSizeInSp);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int initialWidthMeasureSpec;
        int layoutWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        if (layoutWidth > this.lastLayoutWidth && isStacked()) {
            setStacked(false);
        }
        this.lastLayoutWidth = layoutWidth;
        boolean needsRemeasure = false;
        if (isStacked() || View.MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            initialWidthMeasureSpec = widthMeasureSpec;
        } else {
            initialWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(layoutWidth, AppTypeInfo.APP_ATTRIBUTE_OVERSEA);
            needsRemeasure = true;
        }
        super.onMeasure(initialWidthMeasureSpec, heightMeasureSpec);
        if (!isStacked() && (-16777216 & getMeasuredWidthAndState()) == 16777216) {
            setStacked(true);
            needsRemeasure = true;
        }
        if (needsRemeasure) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        if (isStacked()) {
            unifyBtnTextSize(getMinTextSize());
        }
        this.onMeasureCnt++;
    }

    private void setStacked(boolean stacked) {
        if (stacked != isStacked()) {
            setOrientation(stacked);
            setShowDividers(stacked ? 0 : 2);
            for (int i = getChildCount() - 2; i >= 0; i--) {
                bringChildToFront(getChildAt(i));
            }
            restoreButtonBar(stacked);
        }
    }

    public boolean isStacked() {
        return getOrientation() == 1;
    }
}
