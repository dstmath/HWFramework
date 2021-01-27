package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwGuidanceTips extends FrameLayout {
    private ImageView mIvIcon;
    private TextView mTvTitle;

    public HwGuidanceTips(Context context) {
        this(context, null);
    }

    public HwGuidanceTips(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwGuidanceTips(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwGuidanceTips(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        ResLoaderUtil.getLayout(context, "hwpattern_guidance_tips_layout", this, true);
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hwguidancetips_tv_text"));
        this.mIvIcon = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "hwguidancetips_iv_icon"));
    }

    public void setTipsText(CharSequence text) {
        TextView textView = this.mTvTitle;
        if (textView != null) {
            textView.setText(text);
        }
    }

    public void setTipsImageDrawable(Drawable drawable) {
        ImageView imageView = this.mIvIcon;
        if (imageView != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    public void setTipsImageResource(int resId) {
        ImageView imageView = this.mIvIcon;
        if (imageView != null) {
            imageView.setImageResource(resId);
        }
    }

    public void setTipsOnClickListener(View.OnClickListener listener) {
        ImageView imageView = this.mIvIcon;
        if (imageView != null) {
            imageView.setOnClickListener(listener);
        }
    }
}
