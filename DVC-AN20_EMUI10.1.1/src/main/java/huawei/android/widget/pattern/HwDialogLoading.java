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
    private ImageView mCancelView;
    private Context mContext;
    private ProgressBar mProgressbar;
    private RelativeLayout mRelativeCancelView;
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
        this.mRelativeCancelView = (RelativeLayout) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_horizontal_progress_view_relative_cancel"));
        this.mProgressbar = (ProgressBar) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_horizontal_progress_view_progressbar"));
        this.mTvProgress = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_horizontal_progress_view_progress"));
        this.mCancelView = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialog_horizontal_progress_view_cancel"));
        this.mRelativeCancelView.setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(this.mContext, 0));
    }

    public void setLayoutStyle(int style) {
        if (this.mStyle != style) {
            this.mStyle = style;
            removeAllViews();
            initView();
        }
    }

    public void setIconImageDrawable(Drawable drawable) {
        ImageView imageView = this.mCancelView;
        if (imageView != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    public void setIconImageResource(int resId) {
        ImageView imageView = this.mCancelView;
        if (imageView != null) {
            imageView.setImageResource(resId);
        }
    }

    public void setTitleText(CharSequence text) {
        TextView textView = this.mTvTitle;
        if (textView != null) {
            textView.setText(text);
        }
    }

    public void setProgress(int progress) {
        ProgressBar progressBar = this.mProgressbar;
        if (progressBar != null && progress >= 0) {
            progressBar.setProgress(progress);
            TextView textView = this.mTvProgress;
            textView.setText(progress + "%");
        }
    }

    public void setIconOnClickListener(View.OnClickListener listener) {
        RelativeLayout relativeLayout = this.mRelativeCancelView;
        if (relativeLayout != null) {
            relativeLayout.setOnClickListener(listener);
        }
    }
}
