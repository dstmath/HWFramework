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
    private static final float DICHOTOMY_SIZE = 2.0f;
    private static final int DOUBLE_SIZE = 2;
    public static final int ELE_BTN_NEGATIVE = 4;
    public static final int ELE_BTN_POSITIVE = 3;
    public static final int ELE_SUBTITLE = 2;
    public static final int ELE_TITLE = 1;
    private Button mBtnNegative;
    private CharSequence mBtnNegativeText;
    private Button mBtnPositive;
    private CharSequence mBtnPositiveText;
    private int mBtnsMargin;
    private RelativeLayout mRlyBtnsWrapper;
    private int mRlyBtnsWrapperWidth;
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
        this.mBtnsMargin = ResLoaderUtil.getDimensionPixelSize(context, "margin_m") + (ResLoaderUtil.getDimensionPixelSize(context, "margin_l") * 2);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            /* class huawei.android.widget.pattern.HwGuidanceRcmd.AnonymousClass1 */

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                int btnPosHeight = HwGuidanceRcmd.this.mBtnPositive.getHeight();
                int btnNegHeight = HwGuidanceRcmd.this.mBtnNegative.getHeight();
                HwGuidanceRcmd.this.mBtnPositive.setHeight(btnPosHeight);
                HwGuidanceRcmd.this.mBtnNegative.setHeight(btnNegHeight);
                HwGuidanceRcmd hwGuidanceRcmd = HwGuidanceRcmd.this;
                hwGuidanceRcmd.mRlyBtnsWrapperWidth = hwGuidanceRcmd.mRlyBtnsWrapper.getWidth();
                HwGuidanceRcmd.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mBtnPositive.setText("");
        this.mBtnNegative.setText("");
        post(new Runnable() {
            /* class huawei.android.widget.pattern.HwGuidanceRcmd.AnonymousClass2 */

            @Override // java.lang.Runnable
            public void run() {
                HwGuidanceRcmd hwGuidanceRcmd = HwGuidanceRcmd.this;
                hwGuidanceRcmd.setButtonTextWithMaxWidth(hwGuidanceRcmd.mBtnNegative, HwGuidanceRcmd.this.mBtnPositive, HwGuidanceRcmd.this.mBtnNegativeText);
                HwGuidanceRcmd hwGuidanceRcmd2 = HwGuidanceRcmd.this;
                hwGuidanceRcmd2.setButtonTextWithMaxWidth(hwGuidanceRcmd2.mBtnPositive, HwGuidanceRcmd.this.mBtnNegative, HwGuidanceRcmd.this.mBtnPositiveText);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setButtonTextWithMaxWidth(Button baseButton, Button associateButton, CharSequence baseButtonText) {
        RelativeLayout relativeLayout = this.mRlyBtnsWrapper;
        if (relativeLayout != null && baseButton != null && associateButton != null) {
            this.mRlyBtnsWrapperWidth = relativeLayout.getWidth();
            int halfWith = Math.round(((float) (this.mRlyBtnsWrapperWidth - this.mBtnsMargin)) / DICHOTOMY_SIZE);
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

    public void isShowTitle(boolean isShowTitle) {
        TextView textView = this.mTvTitle;
        if (textView != null) {
            textView.clearAnimation();
            this.mTvTitle.setVisibility(isShowTitle ? 0 : 8);
        }
    }

    public void setElementText(CharSequence text, int elementTag) {
        Button button;
        if (elementTag == 1) {
            TextView textView = this.mTvTitle;
            if (textView != null && textView.getVisibility() != 8) {
                this.mTvTitle.setText(text);
            }
        } else if (elementTag == 2) {
            TextView textView2 = this.mTvSubTitle;
            if (textView2 != null) {
                textView2.setText(text);
            }
        } else if (elementTag == 3) {
            Button button2 = this.mBtnPositive;
            if (button2 != null) {
                setButtonTextWithMaxWidth(button2, this.mBtnNegative, text);
                this.mBtnPositiveText = text;
            }
        } else if (elementTag == 4 && (button = this.mBtnNegative) != null) {
            setButtonTextWithMaxWidth(button, this.mBtnPositive, text);
            this.mBtnNegativeText = text;
        }
    }

    public void setElementOnClickListener(View.OnClickListener listener, int elementTag) {
        Button button;
        if (elementTag == 3) {
            Button button2 = this.mBtnPositive;
            if (button2 != null) {
                button2.setOnClickListener(listener);
            }
        } else if (elementTag == 4 && (button = this.mBtnNegative) != null) {
            button.setOnClickListener(listener);
        }
    }
}
