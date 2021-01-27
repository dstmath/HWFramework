package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.HwWidgetUtils;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwBannerTipView extends LinearLayout {
    private static final int DENOMINATOR_FACTOR = 3;
    public static final int ELE_LEFT_ICON = 1;
    public static final int ELE_RIGHT_ARROW = 2;
    public static final int ELE_SETTING_TXT = 4;
    public static final int ELE_WARN_TXT = 3;
    public static final int ID_NET_CANNOT_GET_X = 3;
    public static final int ID_NET_OFF = 0;
    public static final int ID_NET_SERVER_UNLINK = 2;
    public static final int ID_NET_WEEK = 1;
    private static final int PADDING_MULTIPLE = 4;
    private TextView mGoToSetTv;
    private ImageView mJumpIv;
    private LinearLayout mJumpLl;
    private int mNetWorkId;
    private final int mPaddingNum;
    private ImageView mWarningIv;
    private TextView mWarningTextView;

    public HwBannerTipView(Context context) {
        this(context, null);
    }

    public HwBannerTipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwBannerTipView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwBannerTipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mPaddingNum = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_l");
        initView();
    }

    private void initView() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_banner_tip_layout"), (ViewGroup) this, true);
        this.mJumpLl = (LinearLayout) findViewById(ResLoaderUtil.getViewId(getContext(), "jump_ll"));
        this.mWarningIv = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "prompt_iv"));
        this.mWarningTextView = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "warning_tv"));
        this.mGoToSetTv = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "set_tv"));
        this.mJumpIv = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "goto_iv"));
        setNetWorkId(0, "");
        setBackgroundColor(ResLoaderUtil.getColor(getContext(), "bannertipview_emui_color_8_alpha_10"));
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.View, android.view.ViewGroup
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        int hasNoUseWidth = getMeasuredWidth() - ((this.mWarningIv.getMeasuredWidth() + this.mJumpIv.getMeasuredWidth()) + (this.mPaddingNum * 4));
        float warningWidth = this.mWarningTextView.getPaint().measureText(this.mWarningTextView.getText().toString());
        int settingMinWidth = hasNoUseWidth / 3;
        if (warningWidth < ((float) (hasNoUseWidth - settingMinWidth))) {
            this.mGoToSetTv.setMaxWidth(hasNoUseWidth - ((int) warningWidth));
        } else {
            this.mGoToSetTv.setMaxWidth(settingMinWidth);
        }
    }

    public void setNetWorkId(int netWorkId, String param) {
        String warnText = "";
        this.mNetWorkId = netWorkId;
        int i = this.mNetWorkId;
        if (i == 0) {
            warnText = ResLoaderUtil.getString(getContext(), "bannertipview_net_off");
        } else if (i == 1) {
            warnText = ResLoaderUtil.getString(getContext(), "bannertipview_net_off_retry");
        } else if (i == 2) {
            warnText = ResLoaderUtil.getString(getContext(), "bannertipview_net_off_server_retry");
        } else if (i == 3) {
            warnText = String.format(ResLoaderUtil.getString(getContext(), "bannertipview_net_off_refresh"), param);
        }
        setElementText(warnText, 3);
    }

    public void setElementTextSize(int textSize, int elementTag) {
        if (elementTag == 3) {
            this.mWarningTextView.setTextSize(0, (float) textSize);
        } else if (elementTag == 4) {
            this.mGoToSetTv.setTextSize(0, (float) textSize);
        }
    }

    public void setElementTextColor(int textColor, int elementTag) {
        if (elementTag == 3) {
            this.mWarningTextView.setTextColor(textColor);
        } else if (elementTag == 4) {
            this.mGoToSetTv.setTextColor(textColor);
        }
    }

    public void setElementText(String text, int elementTag) {
        if (elementTag == 3) {
            this.mWarningTextView.setText(text);
        } else if (elementTag == 4) {
            this.mGoToSetTv.setText(text);
        }
    }

    public void setElementImageDrawable(Drawable image, int elementTag) {
        if (elementTag == 1) {
            this.mWarningIv.setBackground(image);
        } else if (elementTag == 2) {
            this.mJumpIv.setBackground(image);
        }
    }

    public void setElementImageResource(int resId, int elementTag) {
        if (elementTag == 1) {
            this.mWarningIv.setBackgroundResource(resId);
        } else if (elementTag == 2) {
            this.mJumpIv.setBackgroundResource(resId);
        }
    }

    public void setJumpViewVisibility(int visibility) {
        this.mJumpLl.setVisibility(visibility);
    }

    public void setJumViewOnClick(View.OnClickListener clickListener) {
        this.mJumpLl.setOnClickListener(clickListener);
        this.mJumpLl.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(getContext(), 0));
    }
}
