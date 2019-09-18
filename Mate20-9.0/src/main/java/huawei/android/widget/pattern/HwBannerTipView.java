package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.HwWidgetUtils;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwBannerTipView extends LinearLayout {
    public static final int ELE_LEFT_ICON = 1;
    public static final int ELE_RIGHT_ARROW = 2;
    public static final int ELE_SETTING_TXT = 4;
    public static final int ELE_WARN_TXT = 3;
    public static final int ID_NET_CANNOT_GET_X = 3;
    public static final int ID_NET_OFF = 0;
    public static final int ID_NET_SERVER_UNLINK = 2;
    public static final int ID_NET_WEEK = 1;
    private final int PADDING_L;
    private TextView mGoToSetTv;
    private ImageView mJumpIv;
    private LinearLayout mJumpLl;
    private int mNetWorkId;
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
        this.PADDING_L = ResLoaderUtil.getDimensionPixelSize(getContext(), "padding_l");
        initView();
    }

    private void initView() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_banner_tip_layout"), this, true);
        this.mJumpLl = (LinearLayout) findViewById(ResLoaderUtil.getViewId(getContext(), "jump_ll"));
        this.mWarningIv = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "prompt_iv"));
        this.mWarningTextView = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "warning_tv"));
        this.mGoToSetTv = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "set_tv"));
        this.mJumpIv = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "goto_iv"));
        setNetWorkId(0, "");
        setBackgroundColor(ResLoaderUtil.getColor(getContext(), "bannertipview_emui_color_8_alpha_10"));
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int hasNoUseWidth = getMeasuredWidth() - ((this.mWarningIv.getMeasuredWidth() + this.mJumpIv.getMeasuredWidth()) + (4 * this.PADDING_L));
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
        switch (this.mNetWorkId) {
            case 0:
                warnText = ResLoaderUtil.getString(getContext(), "bannertipview_net_off");
                break;
            case 1:
                warnText = ResLoaderUtil.getString(getContext(), "bannertipview_net_off_retry");
                break;
            case 2:
                warnText = ResLoaderUtil.getString(getContext(), "bannertipview_net_off_server_retry");
                break;
            case 3:
                warnText = String.format(ResLoaderUtil.getString(getContext(), "bannertipview_net_off_refresh"), new Object[]{param});
                break;
        }
        setElementText(warnText, 3);
    }

    public void setElementTextSize(int textSize, int elementTag) {
        switch (elementTag) {
            case 3:
                this.mWarningTextView.setTextSize(0, (float) textSize);
                return;
            case 4:
                this.mGoToSetTv.setTextSize(0, (float) textSize);
                return;
            default:
                return;
        }
    }

    public void setElementTextColor(int textColor, int elementTag) {
        switch (elementTag) {
            case 3:
                this.mWarningTextView.setTextColor(textColor);
                return;
            case 4:
                this.mGoToSetTv.setTextColor(textColor);
                return;
            default:
                return;
        }
    }

    public void setElementText(String text, int elementTag) {
        switch (elementTag) {
            case 3:
                this.mWarningTextView.setText(text);
                return;
            case 4:
                this.mGoToSetTv.setText(text);
                return;
            default:
                return;
        }
    }

    public void setElementImageDrawable(Drawable image, int elementTag) {
        switch (elementTag) {
            case 1:
                this.mWarningIv.setBackground(image);
                return;
            case 2:
                this.mJumpIv.setBackground(image);
                return;
            default:
                return;
        }
    }

    public void setElementImageResource(int resId, int elementTag) {
        switch (elementTag) {
            case 1:
                this.mWarningIv.setBackgroundResource(resId);
                return;
            case 2:
                this.mJumpIv.setBackgroundResource(resId);
                return;
            default:
                return;
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
