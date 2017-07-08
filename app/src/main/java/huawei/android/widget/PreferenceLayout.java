package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class PreferenceLayout extends LinearLayout {
    private View detail_frame;
    private View icon_frame;
    private View title_frame;
    private View widget_frame;

    public PreferenceLayout(Context context) {
        this(context, null);
    }

    public PreferenceLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        switch (child.getId()) {
            case 16908312:
                this.widget_frame = findViewById(16908312);
            case 34603122:
                this.icon_frame = findViewById(34603122);
            case 34603163:
                this.title_frame = findViewById(34603163);
            case 34603165:
                this.detail_frame = findViewById(34603165);
            default:
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        this.title_frame.measure(0, heightMeasureSpec);
        this.detail_frame.measure(0, heightMeasureSpec);
        int titleWidth_origin = this.title_frame.getMeasuredWidth();
        int detailWidth_origin = this.detail_frame.getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LinearLayout.LayoutParams title_lp = (LinearLayout.LayoutParams) this.title_frame.getLayoutParams();
        int titleHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + title_lp.topMargin) + title_lp.bottomMargin, title_lp.height);
        LinearLayout.LayoutParams detail_lp = (LinearLayout.LayoutParams) this.detail_frame.getLayoutParams();
        int detailHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + detail_lp.topMargin) + detail_lp.bottomMargin, detail_lp.height);
        int iconWidth = this.icon_frame.getMeasuredWidth();
        int availableSpace = (((widthSize - getPaddingStart()) - getPaddingEnd()) - iconWidth) - this.widget_frame.getMeasuredWidth();
        int maxTitleWidthWithDetail = (availableSpace * 2) / 3;
        if (this.title_frame.getVisibility() != 0 || this.detail_frame.getVisibility() != 0 || titleWidth_origin + detailWidth_origin <= availableSpace) {
            return;
        }
        if (titleWidth_origin > maxTitleWidthWithDetail) {
            this.title_frame.measure(MeasureSpec.makeMeasureSpec(maxTitleWidthWithDetail, 1073741824), titleHeightMeasureSpec);
            this.detail_frame.measure(MeasureSpec.makeMeasureSpec(availableSpace - maxTitleWidthWithDetail, 1073741824), detailHeightMeasureSpec);
        } else if (titleWidth_origin > availableSpace - maxTitleWidthWithDetail) {
            this.title_frame.measure(MeasureSpec.makeMeasureSpec(titleWidth_origin, 1073741824), titleHeightMeasureSpec);
            this.detail_frame.measure(MeasureSpec.makeMeasureSpec(availableSpace - titleWidth_origin, 1073741824), detailHeightMeasureSpec);
        } else {
            this.title_frame.measure(MeasureSpec.makeMeasureSpec(availableSpace - maxTitleWidthWithDetail, 1073741824), titleHeightMeasureSpec);
            this.detail_frame.measure(MeasureSpec.makeMeasureSpec(maxTitleWidthWithDetail, 1073741824), detailHeightMeasureSpec);
        }
    }
}
