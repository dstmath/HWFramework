package huawei.android.widget.pattern;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwGuidanceRcmd extends FrameLayout {
    public static final int ELE_BTN_NEGATIVE = 4;
    public static final int ELE_BTN_POSITIVE = 3;
    public static final int ELE_SUBTITLE = 2;
    public static final int ELE_TITLE = 1;
    /* access modifiers changed from: private */
    public Button mBtnNegative;
    /* access modifiers changed from: private */
    public CharSequence mBtnNegativeText;
    /* access modifiers changed from: private */
    public Button mBtnPositive;
    /* access modifiers changed from: private */
    public CharSequence mBtnPositiveText;
    private int mBtnsMargin;
    /* access modifiers changed from: private */
    public RelativeLayout mRlyBtnsWrapper;
    /* access modifiers changed from: private */
    public int mRlyBtnsWrapperWidth;
    private TextView mTvSubTitle;
    private TextView mTvTitle;

    public HwGuidanceRcmd(Context context) {
        this(context, null);
    }

    public HwGuidanceRcmd(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwGuidanceRcmd(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwGuidanceRcmd(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        ResLoaderUtil.getLayout(context, "hwpattern_guidance_rcmd_layout", this, true);
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hwguidance_rcmd_tv_title"));
        this.mTvSubTitle = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hwguidance_rcmd_tv_subtitle"));
        this.mBtnPositive = (Button) findViewById(ResLoaderUtil.getViewId(context, "hwguidance_rcmd_btn_positive"));
        this.mBtnNegative = (Button) findViewById(ResLoaderUtil.getViewId(context, "hwguidance_rcmd_btn_negative"));
        this.mRlyBtnsWrapper = (RelativeLayout) findViewById(ResLoaderUtil.getViewId(context, "hwguidance_rcmd_rly_btns_wrapper"));
        this.mBtnsMargin = ResLoaderUtil.getDimensionPixelSize(context, "margin_m") + (2 * ResLoaderUtil.getDimensionPixelSize(context, "margin_l"));
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                int BtnPosHeight = HwGuidanceRcmd.this.mBtnPositive.getHeight();
                int BtnNegHeight = HwGuidanceRcmd.this.mBtnNegative.getHeight();
                HwGuidanceRcmd.this.mBtnPositive.setHeight(BtnPosHeight);
                HwGuidanceRcmd.this.mBtnNegative.setHeight(BtnNegHeight);
                int unused = HwGuidanceRcmd.this.mRlyBtnsWrapperWidth = HwGuidanceRcmd.this.mRlyBtnsWrapper.getWidth();
                HwGuidanceRcmd.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mBtnPositive.setText("");
        this.mBtnNegative.setText("");
        post(new Runnable() {
            public void run() {
                HwGuidanceRcmd.this.setButtonTextWithMaxWidth(HwGuidanceRcmd.this.mBtnNegative, HwGuidanceRcmd.this.mBtnPositive, HwGuidanceRcmd.this.mBtnNegativeText);
                HwGuidanceRcmd.this.setButtonTextWithMaxWidth(HwGuidanceRcmd.this.mBtnPositive, HwGuidanceRcmd.this.mBtnNegative, HwGuidanceRcmd.this.mBtnPositiveText);
            }
        });
    }

    /* access modifiers changed from: private */
    public void setButtonTextWithMaxWidth(Button baseButton, Button associateButton, CharSequence baseButtonText) {
        if (this.mRlyBtnsWrapper != null && baseButton != null && associateButton != null) {
            this.mRlyBtnsWrapperWidth = this.mRlyBtnsWrapper.getWidth();
            int halfWith = (int) ((((float) (this.mRlyBtnsWrapperWidth - this.mBtnsMargin)) / 2.0f) + 0.5f);
            if (TextUtils.isEmpty(associateButton.getText())) {
                baseButton.setMaxWidth(halfWith);
                baseButton.setText(baseButtonText);
                return;
            }
            associateButton.measure(0, 0);
            int asoButtonWidth = associateButton.getMeasuredWidth();
            int asoButtonMaxWidth = associateButton.getMaxWidth();
            baseButton.setMaxWidth((this.mRlyBtnsWrapperWidth - (asoButtonWidth < asoButtonMaxWidth ? asoButtonWidth : asoButtonMaxWidth)) - this.mBtnsMargin);
            baseButton.setText(baseButtonText);
            if (TextUtils.isEmpty(baseButtonText)) {
                associateButton.setMaxWidth(halfWith);
                return;
            }
            baseButton.measure(0, 0);
            int baseButtonWidth = baseButton.getMeasuredWidth();
            int baseButtonMaxWidth = baseButton.getMaxWidth();
            associateButton.setMaxWidth((this.mRlyBtnsWrapperWidth - (baseButtonWidth < baseButtonMaxWidth ? baseButtonWidth : baseButtonMaxWidth)) - this.mBtnsMargin);
        }
    }

    public void isShowTitle(boolean showTitle) {
        if (this.mTvTitle != null) {
            this.mTvTitle.clearAnimation();
            this.mTvTitle.setVisibility(showTitle ? 0 : 8);
        }
    }

    public void setElementText(CharSequence text, int elementTag) {
        switch (elementTag) {
            case 1:
                if (!(this.mTvTitle == null || this.mTvTitle.getVisibility() == 8)) {
                    this.mTvTitle.setText(text);
                    break;
                }
            case 2:
                if (this.mTvSubTitle != null) {
                    this.mTvSubTitle.setText(text);
                    break;
                } else {
                    return;
                }
            case 3:
                if (this.mBtnPositive != null) {
                    setButtonTextWithMaxWidth(this.mBtnPositive, this.mBtnNegative, text);
                    this.mBtnPositiveText = text;
                    break;
                } else {
                    return;
                }
            case 4:
                if (this.mBtnNegative != null) {
                    setButtonTextWithMaxWidth(this.mBtnNegative, this.mBtnPositive, text);
                    this.mBtnNegativeText = text;
                    break;
                } else {
                    return;
                }
        }
    }

    public void setElementOnClickListener(View.OnClickListener listener, int elementTag) {
        switch (elementTag) {
            case 3:
                if (this.mBtnPositive != null) {
                    this.mBtnPositive.setOnClickListener(listener);
                    break;
                } else {
                    return;
                }
            case 4:
                if (this.mBtnNegative != null) {
                    this.mBtnNegative.setOnClickListener(listener);
                    break;
                } else {
                    return;
                }
        }
    }
}
