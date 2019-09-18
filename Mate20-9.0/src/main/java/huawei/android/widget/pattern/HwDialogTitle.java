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
        switch (this.mStyle) {
            case 2:
                ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialogtitle_double_title_one_icon", this, true);
                this.mTitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_title"));
                this.mSubtitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_subtitle"));
                this.mLeftIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_left_image"));
                return;
            case 3:
                ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialogtitle_double_title_two_icon", this, true);
                this.mTitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_title"));
                this.mSubtitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_subtitle"));
                this.mLeftIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_left_image"));
                this.mRightIv = (ImageView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_right_image"));
                return;
            case 4:
                ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialogtitle_single_title_progress", this, true);
                this.mTitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_title"));
                return;
            default:
                ResLoaderUtil.getLayout(this.mContext, "hwpattern_dialogtitle_double_title", this, true);
                this.mTitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_title"));
                this.mSubtitleTv = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwpattern_dialogtitle_subtitle"));
                return;
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
        switch (elementTag) {
            case 1:
                if (this.mTitleTv != null && !TextUtils.isEmpty(text)) {
                    this.mTitleTv.setText(text);
                    return;
                }
                return;
            case 2:
                if (this.mSubtitleTv != null && !TextUtils.isEmpty(text)) {
                    this.mSubtitleTv.setText(text);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void setElementImageDrawable(Drawable image, int elementTag) {
        switch (elementTag) {
            case 3:
                if (this.mLeftIv != null) {
                    this.mLeftIv.setImageDrawable(image);
                    return;
                }
                return;
            case 4:
                if (this.mRightIv != null) {
                    this.mRightIv.setImageDrawable(image);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void setElementImageResource(int resId, int elementTag) {
        switch (elementTag) {
            case 3:
                if (this.mLeftIv != null) {
                    this.mLeftIv.setImageResource(resId);
                    return;
                }
                return;
            case 4:
                if (this.mRightIv != null) {
                    this.mRightIv.setImageResource(resId);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void setElementOnClickListener(View.OnClickListener listener, int elementTag) {
        switch (elementTag) {
            case 3:
                if (this.mLeftIv != null) {
                    this.mLeftIv.setOnClickListener(listener);
                    return;
                }
                return;
            case 4:
                if (this.mRightIv != null) {
                    this.mRightIv.setOnClickListener(listener);
                    return;
                }
                return;
            default:
                return;
        }
    }
}
