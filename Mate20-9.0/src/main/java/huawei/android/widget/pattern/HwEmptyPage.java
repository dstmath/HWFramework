package huawei.android.widget.pattern;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwEmptyPage extends FrameLayout {
    private Button mBtnAction;
    private ImageView mIvIcon;
    private TextView mTvContent;

    public HwEmptyPage(Context context) {
        this(context, null);
    }

    public HwEmptyPage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwEmptyPage(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwEmptyPage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        ResLoaderUtil.getLayout(context, "hwpattern_empty_page_layout", this, true);
        this.mIvIcon = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "hwemptypage_iv_icon"));
        this.mTvContent = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hwemptypage_tv_content"));
        this.mBtnAction = (Button) findViewById(ResLoaderUtil.getViewId(context, "hwemptypage_btn_action"));
    }

    public void setContentText(CharSequence text) {
        if (this.mTvContent != null) {
            this.mTvContent.setText(text);
        }
    }

    public void setActionBtnText(CharSequence text) {
        if (this.mBtnAction != null) {
            if (this.mBtnAction.getVisibility() != 0) {
                this.mBtnAction.setVisibility(0);
            }
            this.mBtnAction.setText(text);
        }
    }

    public void setActionBtnTextColor(ColorStateList colors) {
        if (!(this.mBtnAction == null || colors == null || this.mBtnAction.getVisibility() != 0)) {
            this.mBtnAction.setTextColor(colors);
        }
    }

    public void setActionBtnBackground(Drawable drawable) {
        if (this.mBtnAction != null && this.mBtnAction.getVisibility() == 0) {
            this.mBtnAction.setBackground(drawable);
        }
    }

    public void setIconImageDrawable(Drawable drawable) {
        if (this.mIvIcon != null) {
            this.mIvIcon.setImageDrawable(drawable);
        }
    }

    public void setIconImageResource(int resId) {
        if (this.mIvIcon != null) {
            this.mIvIcon.setImageResource(resId);
        }
    }

    public void setActionBtnOnClickListener(View.OnClickListener listener) {
        if (this.mBtnAction != null && this.mBtnAction.getVisibility() == 0) {
            this.mBtnAction.setOnClickListener(listener);
        }
    }
}
