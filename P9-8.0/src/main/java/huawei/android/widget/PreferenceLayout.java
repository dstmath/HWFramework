package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class PreferenceLayout extends LinearLayout {
    private static final String TAG = "PreferenceLayout";
    private View mDetailView;
    private View mIconView;
    private View mTitleView;
    private View mWidgetView;

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
                this.mWidgetView = findViewById(16908312);
                return;
            case 34603108:
                this.mIconView = findViewById(34603108);
                return;
            case 34603109:
                this.mTitleView = findViewById(34603109);
                return;
            case 34603110:
                this.mDetailView = findViewById(34603110);
                return;
            default:
                return;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mTitleView == null || this.mDetailView == null || this.mIconView == null || this.mWidgetView == null) {
            Log.w(TAG, "mTitleView = " + this.mTitleView + " , mDetailView = " + this.mDetailView + " , mIconView = " + this.mIconView + " , mWidgetView = " + this.mWidgetView);
            return;
        }
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        this.mTitleView.measure(0, heightMeasureSpec);
        this.mDetailView.measure(0, heightMeasureSpec);
        int titleWidthOrigin = this.mTitleView.getMeasuredWidth();
        int detailWidthOrigin = this.mDetailView.getMeasuredWidth();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) this.mTitleView.getLayoutParams();
        int titleHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + titleParams.topMargin) + titleParams.bottomMargin, titleParams.height);
        LinearLayout.LayoutParams detailParams = (LinearLayout.LayoutParams) this.mDetailView.getLayoutParams();
        int detailHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, ((getPaddingTop() + getPaddingBottom()) + detailParams.topMargin) + detailParams.bottomMargin, detailParams.height);
        int iconWidth = this.mIconView.getMeasuredWidth();
        int availableSpace = (((widthSize - getPaddingStart()) - getPaddingEnd()) - iconWidth) - this.mWidgetView.getMeasuredWidth();
        int maxTitleWidthWithDetail = (availableSpace * 2) / 3;
        if (this.mTitleView.getVisibility() == 0 && this.mDetailView.getVisibility() == 0) {
            if (titleWidthOrigin > maxTitleWidthWithDetail) {
                this.mTitleView.measure(MeasureSpec.makeMeasureSpec(maxTitleWidthWithDetail, 1073741824), titleHeightMeasureSpec);
                this.mDetailView.measure(MeasureSpec.makeMeasureSpec(availableSpace - maxTitleWidthWithDetail, 1073741824), detailHeightMeasureSpec);
            }
            if (titleWidthOrigin + detailWidthOrigin > availableSpace) {
                if (titleWidthOrigin > availableSpace - maxTitleWidthWithDetail && titleWidthOrigin <= maxTitleWidthWithDetail) {
                    adjustHeight(widthMeasureSpec, titleWidthOrigin, availableSpace - titleWidthOrigin);
                } else if (titleWidthOrigin <= availableSpace - maxTitleWidthWithDetail) {
                    adjustHeight(widthMeasureSpec, availableSpace - maxTitleWidthWithDetail, maxTitleWidthWithDetail);
                }
            } else if (detailWidthOrigin > availableSpace - maxTitleWidthWithDetail) {
                this.mTitleView.measure(MeasureSpec.makeMeasureSpec(availableSpace - detailWidthOrigin, 1073741824), titleHeightMeasureSpec);
                this.mDetailView.measure(MeasureSpec.makeMeasureSpec(detailWidthOrigin, 1073741824), detailHeightMeasureSpec);
            }
        }
    }

    private void adjustHeight(int widthMeasureSpec, int titleWidth, int detailWidth) {
        this.mTitleView.measure(MeasureSpec.makeMeasureSpec(titleWidth, 1073741824), 0);
        this.mDetailView.measure(MeasureSpec.makeMeasureSpec(detailWidth, 1073741824), 0);
        int heightSpec = MeasureSpec.makeMeasureSpec(this.mTitleView.getMeasuredHeight() > this.mDetailView.getMeasuredHeight() ? this.mTitleView.getMeasuredHeight() : this.mDetailView.getMeasuredHeight(), 1073741824);
        this.mIconView.measure(0, heightSpec);
        this.mWidgetView.measure(0, heightSpec);
        setMeasuredDimension(widthMeasureSpec, heightSpec);
    }
}
