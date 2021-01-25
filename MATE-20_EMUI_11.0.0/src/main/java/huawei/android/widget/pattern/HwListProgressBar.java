package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.Locale;

public class HwListProgressBar extends FrameLayout {
    public static final int ELE_SUBTITLE = 1;
    public static final int ELE_TITLE = 0;
    public static final int TYPE_LARGE = 1;
    public static final int TYPE_SMALL = 0;
    private ImageView mIvIcon;
    private ProgressBar mProgressBar;
    private TextView mTvSubTitle;
    private TextView mTvTitle;
    private int mType;

    public HwListProgressBar(Context context) {
        this(context, null);
    }

    public HwListProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwListProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwListProgressBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setType(int type) {
        this.mType = type;
        LayoutInflater.from(getContext()).inflate(ResLoaderUtil.getLayoutId(getContext(), this.mType == 0 ? "hwpattern_progressbar_small" : "hwpattern_progressbar_large"), this);
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_progressbar_title"));
        this.mTvSubTitle = (TextView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_progressbar_subtitle"));
        this.mIvIcon = (ImageView) findViewById(ResLoaderUtil.getViewId(getContext(), "list_progressbar_icon"));
        this.mProgressBar = (ProgressBar) findViewById(ResLoaderUtil.getViewId(getContext(), "list_progressBar"));
        if (isUrLanguage()) {
            this.mTvTitle.setGravity(3);
            this.mTvSubTitle.setGravity(5);
        }
    }

    private boolean isUrLanguage() {
        return "ur".equals(Locale.getDefault().getLanguage());
    }

    public void setElementTitle(CharSequence text, int elementTag) {
        if (elementTag == 0) {
            this.mTvTitle.setText(text);
        } else if (elementTag == 1) {
            this.mTvSubTitle.setText(text);
        }
    }

    public void setImageDrawable(Drawable drawable) {
        this.mIvIcon.setImageDrawable(drawable);
    }

    public void setImageResource(int resId) {
        this.mIvIcon.setImageResource(resId);
    }

    public void setProgress(int progress) {
        this.mProgressBar.setProgress(progress);
    }
}
