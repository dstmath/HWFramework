package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwDialogConfirm extends ScrollView {
    private ImageView mIvIcon;
    private TextView mTvContent;

    public HwDialogConfirm(Context context) {
        this(context, null);
    }

    public HwDialogConfirm(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwDialogConfirm(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwDialogConfirm(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOverScrollMode(2);
        initView(context);
    }

    private void initView(Context context) {
        ResLoaderUtil.getLayout(context, "hwpattern_dialog_confirm_layout", this, true);
        this.mTvContent = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hwdialog_confirm_tv_content"));
        this.mIvIcon = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "hwdialog_confirm_iv_icon"));
    }

    public void setText(CharSequence text) {
        if (this.mTvContent != null) {
            this.mTvContent.setText(text);
        }
    }

    public void setImageDrawable(Drawable drawable) {
        if (this.mIvIcon != null) {
            this.mIvIcon.setImageDrawable(drawable);
        }
    }

    public void setImageResource(int resId) {
        if (this.mIvIcon != null) {
            this.mIvIcon.setImageResource(resId);
        }
    }
}
