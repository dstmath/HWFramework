package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwDialogTitle extends LinearLayout {
    public static final int ELE_LEFT_ICON = 3;
    public static final int ELE_RIGHT_ICON = 4;
    public static final int ELE_SUBTITLE_TXT = 2;
    public static final int ELE_TITLE_TXT = 1;
    private static final String ID_DIALOGTITLE_SUBTITLE = "hwpattern_dialogtitle_subtitle";
    private static final String ID_DIALOGTITLE_TITLE = "hwpattern_dialogtitle_title";
    public static final int LAYOUT_STYLE_DOUBLE_TITLE = 1;
    public static final int LAYOUT_STYLE_DOUBLE_TITLE_ONE_ICON = 2;
    public static final int LAYOUT_STYLE_DOUBLE_TITLE_TWO_ICON = 3;
    public static final int LAYOUT_STYLE_SINGLE_TITLE_PROGRESS = 4;
    private Context mContext;
    private ImageView mLeftIv;
    private ImageView mRightIv;
    private int mStyle;
    private TextView mSubtitleTv;
    private TextView mTitleTv;

    public HwDialogTitle(Context context) {
        this(context, null);
    }

    public HwDialogTitle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwDialogTitle(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwDialogTitle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mStyle = 1;
        this.mContext = context;
        initView();
    }

    private void initView() {
        int i = this.mStyle;
        if (i == 2) {
            ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialogtitle_double_title_one_icon", this, true);
            this.mTitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, ID_DIALOGTITLE_TITLE));
            this.mSubtitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, ID_DIALOGTITLE_SUBTITLE));
            this.mLeftIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_left_image"));
        } else if (i == 3) {
            ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialogtitle_double_title_two_icon", this, true);
            this.mTitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, ID_DIALOGTITLE_TITLE));
            this.mSubtitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, ID_DIALOGTITLE_SUBTITLE));
            this.mLeftIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_left_image"));
            this.mRightIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_right_image"));
        } else if (i != 4) {
            ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialogtitle_double_title", this, true);
            this.mTitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, ID_DIALOGTITLE_TITLE));
            this.mSubtitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, ID_DIALOGTITLE_SUBTITLE));
        } else {
            ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialogtitle_single_title_progress", this, true);
            this.mTitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, ID_DIALOGTITLE_TITLE));
        }
    }

    public void setLayoutStyle(int style) {
        if (this.mStyle != style) {
            this.mStyle = style;
            removeAllViews();
            initView();
        }
    }

    public void setElementText(CharSequence text, int elementTag) {
        if (elementTag != 1) {
            if (elementTag == 2 && this.mSubtitleTv != null && !TextUtils.isEmpty(text)) {
                this.mSubtitleTv.setText(text);
            }
        } else if (this.mTitleTv != null && !TextUtils.isEmpty(text)) {
            this.mTitleTv.setText(text);
        }
    }

    public void setElementImageDrawable(Drawable image, int elementTag) {
        ImageView imageView;
        if (elementTag == 3) {
            ImageView imageView2 = this.mLeftIv;
            if (imageView2 != null) {
                imageView2.setImageDrawable(image);
            }
        } else if (elementTag == 4 && (imageView = this.mRightIv) != null) {
            imageView.setImageDrawable(image);
        }
    }

    public void setElementImageResource(int resId, int elementTag) {
        ImageView imageView;
        if (elementTag == 3) {
            ImageView imageView2 = this.mLeftIv;
            if (imageView2 != null) {
                imageView2.setImageResource(resId);
            }
        } else if (elementTag == 4 && (imageView = this.mRightIv) != null) {
            imageView.setImageResource(resId);
        }
    }

    public void setElementOnClickListener(View.OnClickListener listener, int elementTag) {
        ImageView imageView;
        if (elementTag == 3) {
            ImageView imageView2 = this.mLeftIv;
            if (imageView2 != null) {
                imageView2.setOnClickListener(listener);
            }
        } else if (elementTag == 4 && (imageView = this.mRightIv) != null) {
            imageView.setOnClickListener(listener);
        }
    }
}
