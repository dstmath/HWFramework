package huawei.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import huawei.android.widget.loader.ResLoaderUtil;

public class PreferenceLayout extends LinearLayout {
    private static final int DEFAULT_WEIGHT = 3;
    private static final int DEFAULT_WEIGHT_END = 2;
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

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        int id = child.getId();
        if (id == ResLoaderUtil.getViewId(getContext(), "icon_frame")) {
            this.mIconView = findViewById(ResLoaderUtil.getViewId(getContext(), "icon_frame"));
        } else if (id == ResLoaderUtil.getViewId(getContext(), "title_frame")) {
            this.mTitleView = findViewById(ResLoaderUtil.getViewId(getContext(), "title_frame"));
        } else if (id == ResLoaderUtil.getViewId(getContext(), "detail")) {
            this.mDetailView = findViewById(ResLoaderUtil.getViewId(getContext(), "detail"));
        } else if (id == 16908312) {
            this.mWidgetView = findViewById(16908312);
        }
    }

    private boolean isShouldMeasure() {
        if (this.mTitleView != null && this.mDetailView != null && this.mIconView != null && this.mWidgetView != null) {
            return true;
        }
        Log.w(TAG, "mTitleView = " + this.mTitleView + " , mDetailView = " + this.mDetailView + " , mIconView = " + this.mIconView + " , mWidgetView = " + this.mWidgetView);
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isShouldMeasure()) {
            this.mTitleView.measure(0, heightMeasureSpec);
            this.mDetailView.measure(0, heightMeasureSpec);
            int titleWidthOrigin = this.mTitleView.getMeasuredWidth();
            int detailWidthOrigin = this.mDetailView.getMeasuredWidth();
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
            int iconWidth = this.mIconView.getMeasuredWidth();
            int availableSpace = (((widthSize - getPaddingStart()) - getPaddingEnd()) - iconWidth) - this.mWidgetView.getMeasuredWidth();
            if (this.mTitleView.getVisibility() == 0 && this.mDetailView.getVisibility() == 0) {
                measureTitleAndDetailView(titleWidthOrigin, detailWidthOrigin, availableSpace, widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    private void measureTitleAndDetailView(int titleWidthOrigin, int detailWidthOrigin, int availableSpace, int widthMeasureSpec, int heightMeasureSpec) {
        ViewGroup.LayoutParams titleParamsTemp = this.mTitleView.getLayoutParams();
        if (titleParamsTemp instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) titleParamsTemp;
            int titleHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom() + titleParams.topMargin + titleParams.bottomMargin, titleParams.height);
            ViewGroup.LayoutParams detailParamsTemp = this.mDetailView.getLayoutParams();
            if (detailParamsTemp instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams detailParams = (LinearLayout.LayoutParams) detailParamsTemp;
                int detailHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom() + detailParams.topMargin + detailParams.bottomMargin, detailParams.height);
                int maxTitleWidthWithDetail = (availableSpace * 2) / 3;
                if (titleWidthOrigin > maxTitleWidthWithDetail) {
                    this.mTitleView.measure(View.MeasureSpec.makeMeasureSpec(maxTitleWidthWithDetail, 1073741824), titleHeightMeasureSpec);
                    this.mDetailView.measure(View.MeasureSpec.makeMeasureSpec(availableSpace - maxTitleWidthWithDetail, 1073741824), detailHeightMeasureSpec);
                }
                if (titleWidthOrigin + detailWidthOrigin > availableSpace) {
                    if (titleWidthOrigin > availableSpace - maxTitleWidthWithDetail && titleWidthOrigin <= maxTitleWidthWithDetail) {
                        adjustHeight(widthMeasureSpec, titleWidthOrigin, availableSpace - titleWidthOrigin);
                    } else if (titleWidthOrigin <= availableSpace - maxTitleWidthWithDetail) {
                        adjustHeight(widthMeasureSpec, availableSpace - maxTitleWidthWithDetail, maxTitleWidthWithDetail);
                    }
                } else if (detailWidthOrigin > availableSpace - maxTitleWidthWithDetail) {
                    this.mTitleView.measure(View.MeasureSpec.makeMeasureSpec(availableSpace - detailWidthOrigin, 1073741824), titleHeightMeasureSpec);
                    this.mDetailView.measure(View.MeasureSpec.makeMeasureSpec(detailWidthOrigin, 1073741824), detailHeightMeasureSpec);
                }
            }
        }
    }

    private void adjustHeight(int widthMeasureSpec, int titleWidth, int detailWidth) {
        this.mTitleView.measure(View.MeasureSpec.makeMeasureSpec(titleWidth, 1073741824), 0);
        this.mDetailView.measure(View.MeasureSpec.makeMeasureSpec(detailWidth, 1073741824), 0);
        int heightSpec = View.MeasureSpec.makeMeasureSpec((this.mTitleView.getMeasuredHeight() > this.mDetailView.getMeasuredHeight() ? this.mTitleView : this.mDetailView).getMeasuredHeight(), 1073741824);
        this.mIconView.measure(0, heightSpec);
        this.mWidgetView.measure(0, heightSpec);
        setMeasuredDimension(widthMeasureSpec, heightSpec);
    }
}
