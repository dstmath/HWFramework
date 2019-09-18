package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import huawei.android.widget.HwWidgetUtils;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwDialogLoading extends FrameLayout {
    public static final int PROGRESS_BAR_STYLE_CIRCLE = 0;
    public static final int PROGRESS_BAR_STYLE_HORIZONTAL = 1;
    private ImageView mCancel;
    private Context mContext;
    private ProgressBar mProgressbar;
    private RelativeLayout mRelativeCancel;
    private int mStyle;
    private TextView mTvProgress;
    private TextView mTvTitle;

    public HwDialogLoading(Context context) {
        this(context, null);
    }

    public HwDialogLoading(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwDialogLoading(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwDialogLoading(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mStyle = 0;
        this.mContext = context;
        initView();
    }

    private void initView() {
        if (this.mStyle != 1) {
            ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialog_circle_progress_view", this, true);
            this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_circle_progress_view_title"));
            return;
        }
        ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialog_horizontal_progress_view", this, true);
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_horizontal_progress_view_title"));
        this.mRelativeCancel = (RelativeLayout) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_horizontal_progress_view_relative_cancel"));
        this.mProgressbar = (ProgressBar) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_horizontal_progress_view_progressbar"));
        this.mTvProgress = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_horizontal_progress_view_progress"));
        this.mCancel = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_horizontal_progress_view_cancel"));
        this.mRelativeCancel.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
    }

    public void setLayoutStyle(int style) {
        if (this.mStyle != style) {
            this.mStyle = style;
            removeAllViews();
            initView();
        }
    }

    public void setIconImageDrawable(Drawable drawable) {
        if (this.mCancel != null) {
            this.mCancel.setImageDrawable(drawable);
        }
    }

    public void setIconImageResource(int resId) {
        if (this.mCancel != null) {
            this.mCancel.setImageResource(resId);
        }
    }

    public void setTitleText(CharSequence text) {
        if (this.mTvTitle != null) {
            this.mTvTitle.setText(text);
        }
    }

    public void setProgress(int progress) {
        if (this.mProgressbar != null && progress >= 0) {
            this.mProgressbar.setProgress(progress);
            TextView textView = this.mTvProgress;
            textView.setText(progress + "%");
        }
    }

    public void setIconOnClickListener(View.OnClickListener listener) {
        if (this.mRelativeCancel != null) {
            this.mRelativeCancel.setOnClickListener(listener);
        }
    }
}
