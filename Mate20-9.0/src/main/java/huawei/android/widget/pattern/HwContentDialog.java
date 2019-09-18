package huawei.android.widget.pattern;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwContentDialog extends LinearLayout {
    private CheckBox mCheckBox;
    private ImageView mContentIv;
    private TextView mContentTv;
    private int mImageIconId;
    private TextView mTitleTv;

    public HwContentDialog(Context context) {
        this(context, null);
    }

    public HwContentDialog(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwContentDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwContentDialog(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(ResLoaderUtil.getLayoutId(getContext(), "hwpattern_image_dialog_layout"), this, true);
        this.mTitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_dialog_title_tv"));
        this.mContentTv = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_message_tv"));
        this.mCheckBox = (CheckBox) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_check_box"));
        this.mContentIv = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "hwpattern_content_iv"));
    }

    public void setImageIcon(int imageIconId) {
        this.mImageIconId = imageIconId;
        this.mContentIv.setBackgroundResource(this.mImageIconId);
    }

    public void setTitleTextColor(int titleTextColor) {
        this.mTitleTv.setTextColor(titleTextColor);
    }

    public void setTitleTextSize(int titleTextSize) {
        this.mTitleTv.setTextSize(0, (float) titleTextSize);
    }

    public void setContentTextColor(int contentTextColor) {
        this.mContentTv.setTextColor(contentTextColor);
    }

    public void setContentTextSize(int contentTextSize) {
        this.mContentTv.setTextSize(0, (float) contentTextSize);
    }

    public void setCheckText(String checkText) {
        this.mCheckBox.setText(checkText);
    }

    public void setTitleText(String titleText) {
        this.mTitleTv.setText(titleText);
    }

    public void setContentText(String contentText) {
        this.mContentTv.setText(contentText);
    }
}
