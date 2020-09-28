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
        int i = this.mStyle;
        if (i == 0) {
            ResLoaderUtil.getLayout(getContext(), "hwpattern_banner_text_strc_one_layout", this, true);
            this.mTvFirstLine = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_one_tv_first_line"));
            this.mTvNumber = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_one_tv_number"));
            this.mTvNumberUnit = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_one_tv_number_unit"));
            this.mTvNumberDescriber = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_one_tv_number_describer"));
        } else if (i == 1) {
            ResLoaderUtil.getLayout(getContext(), "hwpattern_banner_text_strc_two_layout", this, true);
            this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_two_tv_title"));
            this.mTvDescriber = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hw_banner_text_strc_two_tv_describer"));
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
                    TextView textView = this.mTvFirstLine;
                    if (textView != null) {
                        textView.setText(text);
                        return;
                    }
                    return;
                case 2:
                    TextView textView2 = this.mTvNumber;
                    if (textView2 != null) {
                        textView2.setText(text);
                        layoutNumberLineView();
                        return;
                    }
                    return;
                case 3:
                    TextView textView3 = this.mTvNumberUnit;
                    if (textView3 != null) {
                        textView3.setText(text);
                        layoutNumberLineView();
                        return;
                    }
                    return;
                case 4:
                    TextView textView4 = this.mTvNumberDescriber;
                    if (textView4 != null) {
                        textView4.setText(text);
                        return;
                    }
                    return;
                case 5:
                    TextView textView5 = this.mTvTitle;
                    if (textView5 != null) {
                        textView5.setText(text);
                        return;
                    }
                    return;
                case 6:
                    TextView textView6 = this.mTvDescriber;
                    if (textView6 != null) {
                        textView6.setText(text);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private void layoutNumberLineView() {
        if (!TextUtils.isEmpty(this.mTvNumber.getText()) && !TextUtils.isEmpty(this.mTvNumberUnit.getText())) {
            int width = View.MeasureSpec.makeMeasureSpec(0, 0);
            int height = View.MeasureSpec.makeMeasureSpec(0, 0);
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            this.mTvNumber.measure(width, height);
            int tvNumberWidth = this.mTvNumber.getMeasuredWidth();
            this.mTvNumberUnit.measure(width, height);
            int tvNumberUnitWidth = this.mTvNumberUnit.getMeasuredWidth();
            int allLayoutPaddingStart = (int) getContext().getResources().getDimension(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "hw_banner_text_strc_padding"));
            int allLayoutPaddingEnd = (int) getContext().getResources().getDimension(ResLoader.getInstance().getIdentifier(this.mContext, ResLoaderUtil.DIMEN, "hw_banner_text_strc_padding"));
            if (tvNumberWidth + tvNumberUnitWidth + allLayoutPaddingStart + allLayoutPaddingEnd > screenWidth) {
                this.mTvNumber.setMaxWidth((((screenWidth - allLayoutPaddingStart) - allLayoutPaddingEnd) - allLayoutPaddingEnd) - allLayoutPaddingEnd);
            }
        }
    }
}
