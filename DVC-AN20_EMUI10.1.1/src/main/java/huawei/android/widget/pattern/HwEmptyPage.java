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
        View view = findViewById(ResLoaderUtil.getViewId(context, "hwemptypage_iv_icon"));
        if (view != null && (view instanceof ImageView)) {
            this.mIvIcon = (ImageView) view;
        }
        View view2 = findViewById(ResLoaderUtil.getViewId(context, "hwemptypage_tv_content"));
        if (view2 != null && (view2 instanceof TextView)) {
            this.mTvContent = (TextView) view2;
        }
        View view3 = findViewById(ResLoaderUtil.getViewId(context, "hwemptypage_btn_action"));
        if (view3 != null && (view3 instanceof Button)) {
            this.mBtnAction = (Button) view3;
        }
    }

    public void setContentText(CharSequence text) {
        TextView textView = this.mTvContent;
        if (textView != null) {
            textView.setText(text);
        }
    }

    public void setActionBtnText(CharSequence text) {
        Button button = this.mBtnAction;
        if (button != null) {
            if (button.getVisibility() != 0) {
                this.mBtnAction.setVisibility(0);
            }
            this.mBtnAction.setText(text);
        }
    }

    public void setActionBtnTextColor(ColorStateList colors) {
        Button button = this.mBtnAction;
        if (button != null && colors != null && button.getVisibility() == 0) {
            this.mBtnAction.setTextColor(colors);
        }
    }

    public void setActionBtnBackground(Drawable drawable) {
        Button button = this.mBtnAction;
        if (button != null && button.getVisibility() == 0) {
            this.mBtnAction.setBackground(drawable);
        }
    }

    public void setIconImageDrawable(Drawable drawable) {
        ImageView imageView = this.mIvIcon;
        if (imageView != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    public void setIconImageResource(int resId) {
        ImageView imageView = this.mIvIcon;
        if (imageView != null) {
            imageView.setImageResource(resId);
        }
    }

    public void setActionBtnOnClickListener(View.OnClickListener listener) {
        Button button = this.mBtnAction;
        if (button != null && button.getVisibility() == 0) {
            this.mBtnAction.setOnClickListener(listener);
        }
    }
}
