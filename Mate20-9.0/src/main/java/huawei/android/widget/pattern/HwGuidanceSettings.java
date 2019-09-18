package huawei.android.widget.pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwGuidanceSettings extends FrameLayout {
    public static final int ELE_BTN_FRONT = 2;
    public static final int ELE_BTN_LAST = 4;
    public static final int ELE_BTN_MIDDLE = 3;
    public static final int ELE_TITLE = 1;
    private Button mBtnFront;
    private Button mBtnLast;
    private Button mBtnMiddle;
    private TextView mTvTitle;

    public HwGuidanceSettings(Context context) {
        this(context, null);
    }

    public HwGuidanceSettings(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwGuidanceSettings(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwGuidanceSettings(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        ResLoaderUtil.getLayout(context, "hwpattern_guidance_settings_layout", this, true);
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hwguidance_settings_tv_title"));
        this.mBtnFront = (Button) findViewById(ResLoaderUtil.getViewId(context, "hwguidance_settings_btn_front"));
        this.mBtnMiddle = (Button) findViewById(ResLoaderUtil.getViewId(context, "hwguidance_settings_btn_middle"));
        this.mBtnLast = (Button) findViewById(ResLoaderUtil.getViewId(context, "hwguidance_settings_btn_last"));
    }

    public void setElementText(CharSequence text, int elementTag) {
        switch (elementTag) {
            case 1:
                if (this.mTvTitle != null) {
                    this.mTvTitle.setText(text);
                    break;
                } else {
                    return;
                }
            case 2:
                if (this.mBtnFront != null) {
                    if (this.mBtnFront.getVisibility() != 0) {
                        this.mBtnFront.setVisibility(0);
                    }
                    this.mBtnFront.setText(text);
                    break;
                } else {
                    return;
                }
            case 3:
                if (this.mBtnMiddle != null) {
                    if (this.mBtnMiddle.getVisibility() != 0) {
                        this.mBtnMiddle.setVisibility(0);
                    }
                    this.mBtnMiddle.setText(text);
                    break;
                } else {
                    return;
                }
            case 4:
                if (this.mBtnLast != null) {
                    if (this.mBtnLast.getVisibility() != 0) {
                        this.mBtnLast.setVisibility(0);
                    }
                    this.mBtnLast.setText(text);
                    break;
                } else {
                    return;
                }
        }
    }

    public void setElementOnClickListener(View.OnClickListener listener, int elementTag) {
        switch (elementTag) {
            case 2:
                if (this.mBtnFront != null && this.mBtnFront.getVisibility() == 0) {
                    this.mBtnFront.setOnClickListener(listener);
                    break;
                }
            case 3:
                if (this.mBtnMiddle != null && this.mBtnMiddle.getVisibility() == 0) {
                    this.mBtnMiddle.setOnClickListener(listener);
                    break;
                }
            case 4:
                if (this.mBtnLast != null && this.mBtnLast.getVisibility() == 0) {
                    this.mBtnLast.setOnClickListener(listener);
                    break;
                }
        }
    }
}
