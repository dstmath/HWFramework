package huawei.android.widget.pattern;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwBannerTextStrc extends FrameLayout {
    public static final int ELE_DESCRIBER = 6;
    public static final int ELE_FIRST_LINE = 1;
    public static final int ELE_NUMBER = 2;
    public static final int ELE_NUMBER_DESCRIBER = 4;
    public static final int ELE_NUMBER_UNIT = 3;
    public static final int ELE_TITLE = 5;
    public static final int TYPE_NUM = 0;
    public static final int TYPE_TEXT = 1;
    private int mStyle;
    private TextView mTvDescriber;
    private TextView mTvFirstLine;
    private TextView mTvNumber;
    private TextView mTvNumberDescriber;
    private TextView mTvNumberUnit;
    private TextView mTvTitle;

    public HwBannerTextStrc(Context context) {
        this(context, null);
    }

    public HwBannerTextStrc(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwBannerTextStrc(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwBannerTextStrc(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mStyle = 0;
        initView();
    }

    private void initView() {
        switch (this.mStyle) {
            case 0:
                ResLoaderUtil.getLayout(getContext(), "hwpattern_banner_text_strc_one_layout", this, true);
                this.mTvFirstLine = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_one_tv_first_line"));
                this.mTvNumber = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_one_tv_number"));
                this.mTvNumberUnit = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_one_tv_number_unit"));
                this.mTvNumberDescriber = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_one_tv_number_describer"));
                return;
            case 1:
                ResLoaderUtil.getLayout(getContext(), "hwpattern_banner_text_strc_two_layout", this, true);
                this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_two_tv_title"));
                this.mTvDescriber = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_two_tv_describer"));
                return;
            default:
                return;
        }
    }

    public void setLayoutStyle(int elementStyle) {
        if (this.mStyle != elementStyle) {
            this.mStyle = elementStyle;
            removeAllViews();
            initView();
        }
    }

    public void setElementText(CharSequence text, int elementTag) {
        if (text != null) {
            switch (elementTag) {
                case 1:
                    if (this.mTvFirstLine != null) {
                        this.mTvFirstLine.setText(text);
                        break;
                    }
                    break;
                case 2:
                    if (this.mTvNumber != null) {
                        this.mTvNumber.setText(text);
                        layoutNumberLineView();
                        break;
                    }
                    break;
                case 3:
                    if (this.mTvNumberUnit != null) {
                        this.mTvNumberUnit.setText(text);
                        layoutNumberLineView();
                        break;
                    }
                    break;
                case 4:
                    if (this.mTvNumberDescriber != null) {
                        this.mTvNumberDescriber.setText(text);
                        break;
                    }
                    break;
                case 5:
                    if (this.mTvTitle != null) {
                        this.mTvTitle.setText(text);
                        break;
                    }
                    break;
                case 6:
                    if (this.mTvDescriber != null) {
                        this.mTvDescriber.setText(text);
                        break;
                    }
                    break;
            }
        }
    }

    private void layoutNumberLineView() {
        if (!TextUtils.isEmpty(this.mTvNumber.getText()) && !TextUtils.isEmpty(this.mTvNumberUnit.getText())) {
            int w = View.MeasureSpec.makeMeasureSpec(0, 0);
            int h = View.MeasureSpec.makeMeasureSpec(0, 0);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            this.mTvNumber.measure(w, h);
            int tvNumberWidth = this.mTvNumber.getMeasuredWidth();
            this.mTvNumberUnit.measure(w, h);
            int tvNumberUnitWidth = this.mTvNumberUnit.getMeasuredWidth();
            int allLayoutPaddingStart = (int) getContext().getResources().getDimension(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "hw_banner_text_strc_padding"));
            int allLayoutPaddingEnd = (int) getContext().getResources().getDimension(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "hw_banner_text_strc_padding"));
            if (tvNumberWidth + tvNumberUnitWidth + allLayoutPaddingStart + allLayoutPaddingEnd > screenWidth) {
                this.mTvNumber.setMaxWidth((((screenWidth - allLayoutPaddingStart) - allLayoutPaddingEnd) - allLayoutPaddingEnd) - allLayoutPaddingEnd);
            }
        }
    }
}
