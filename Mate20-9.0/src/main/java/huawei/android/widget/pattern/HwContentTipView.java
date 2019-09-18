package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.Button;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwContentTipView extends LinearLayout {
    public static final int ELE_SETTING_BUTTON = 2;
    public static final int ELE_TIPS_TXT = 1;
    private ImageView mIconIv;
    private int mNetWorkId;
    private Button mSettingBtn;
    private TextView mTipsTv;

    public HwContentTipView(Context context) {
        this(context, null);
    }

    public HwContentTipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwContentTipView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwContentTipView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_content_tip"), this, true);
        setOrientation(1);
        this.mIconIv = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_tips_iv"));
        this.mTipsTv = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_tips_tv"));
        this.mSettingBtn = (Button) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_setting_btn"));
        setNetWorkId(0, "");
    }

    public void setImageDrawable(Drawable image) {
        this.mIconIv.setBackground(image);
    }

    public void setImageResource(int resId) {
        this.mIconIv.setBackgroundResource(resId);
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
        setElementText(warnText, 1);
    }

    public void setElementText(String text, int elementTag) {
        switch (elementTag) {
            case 1:
                this.mTipsTv.setText(text);
                return;
            case 2:
                this.mSettingBtn.setText(text);
                return;
            default:
                return;
        }
    }

    public void setSettingBtnVisibility(int visibility) {
        this.mSettingBtn.setVisibility(visibility);
    }

    public void setSettingBtnClick(View.OnClickListener listener) {
        this.mSettingBtn.setOnClickListener(listener);
    }
}
