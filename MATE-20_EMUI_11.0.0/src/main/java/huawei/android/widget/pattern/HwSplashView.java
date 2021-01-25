package huawei.android.widget.pattern;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.HwCutoutUtil;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwSplashView extends FrameLayout {
    public static final int ELE_TEXT_SPLASH_AD = 2;
    public static final int ELE_TEXT_SPLASH_COMPANY_RIGHT_TEXT = 4;
    public static final int ELE_TEXT_SPLASH_JUMP = 1;
    public static final int ELE_TEXT_SPLASH_VIDEO_TEXT = 3;
    private int mDisplayRotate;
    private HwCutoutUtil mHwCutoutUtil;
    private FrameLayout mMainDisplayParent;
    private TextView mTvAdvert;
    private TextView mTvCompanyRight;
    private TextView mTvJump;
    private int mTvJumpMargin;
    private TextView mTvVideo;
    private LinearLayout mTvVideoInfo;
    private int mTvVideoMarginTop;
    private ImageView mVideoIcon;

    public HwSplashView(Context context) {
        this(context, null);
    }

    public HwSplashView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwSplashView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwSplashView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mHwCutoutUtil = null;
        this.mTvJumpMargin = ResLoaderUtil.getDimensionPixelSize(getContext(), "hw_splash_view_margin");
        this.mTvVideoMarginTop = ResLoaderUtil.getDimensionPixelSize(getContext(), "hw_splash_view_button_padding");
        initView(context);
    }

    private void initView(Context context) {
        ResLoaderUtil.getLayout(context, "hwpattern_splash_view_layout", this, true);
        this.mMainDisplayParent = (FrameLayout) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_main_pic"));
        this.mTvJump = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_jump"));
        this.mTvAdvert = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_ad"));
        this.mVideoIcon = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_video_icon"));
        this.mTvVideo = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_video_text"));
        this.mTvCompanyRight = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_company_right_text"));
        this.mTvVideoInfo = (LinearLayout) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_video_info"));
        if (Build.VERSION.SDK_INT > 27) {
            this.mHwCutoutUtil = new HwCutoutUtil();
            this.mDisplayRotate = HwCutoutUtil.getDisplayRotate(context);
            float fontScale = getContext().getResources().getConfiguration().fontScale;
            int i = this.mDisplayRotate;
            if (i == 1 || i == 3 || fontScale > 1.0f) {
                ViewGroup.LayoutParams paramsTvVideoInfoTemp = this.mTvVideoInfo.getLayoutParams();
                if (paramsTvVideoInfoTemp instanceof LinearLayout.LayoutParams) {
                    LinearLayout.LayoutParams paramsTvVideoInfo = (LinearLayout.LayoutParams) paramsTvVideoInfoTemp;
                    paramsTvVideoInfo.topMargin = this.mTvVideoMarginTop;
                    this.mTvVideoInfo.setLayoutParams(paramsTvVideoInfo);
                }
            }
        }
    }

    public void setElementText(CharSequence text, int elementTag) {
        if (text != null) {
            if (elementTag == 1) {
                this.mTvJump.setText(text);
            } else if (elementTag == 2) {
                this.mTvAdvert.setText(text);
            } else if (elementTag == 3) {
                this.mTvVideo.setText(text);
            } else if (elementTag == 4) {
                this.mTvCompanyRight.setText(text);
            }
        }
    }

    public void addDisplayView(View view) {
        if (view != null) {
            this.mMainDisplayParent.addView(view);
        }
    }

    public void setBottomIconImageDrawable(Drawable drawable) {
        ImageView imageView;
        if (drawable != null && (imageView = this.mVideoIcon) != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    public void setBottomIconImageResource(int resId) {
        ImageView imageView = this.mVideoIcon;
        if (imageView != null) {
            imageView.setImageResource(resId);
        }
    }

    public void setJumpTextClick(View.OnClickListener clickListener) {
        this.mTvJump.setOnClickListener(clickListener);
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (insets == null) {
            return null;
        }
        if (this.mHwCutoutUtil == null || !HwCutoutUtil.needDoCutoutFit(this, getContext())) {
            return super.onApplyWindowInsets(insets);
        }
        this.mHwCutoutUtil.checkCutoutStatus(insets, this, getContext());
        int cutoutPadding = this.mHwCutoutUtil.getCutoutPadding();
        if (getResources().getConfiguration().orientation == 1) {
            resetTextMarginWhenPortrait(insets);
        } else {
            int i = this.mDisplayRotate;
            if (i == 1) {
                resetTextMarginWhenRotation90(cutoutPadding);
            } else if (i != 3) {
                return super.onApplyWindowInsets(insets);
            } else {
                resetTextMarginWhenRotation270(cutoutPadding);
            }
        }
        return super.onApplyWindowInsets(insets);
    }

    private void resetTextMarginWhenPortrait(WindowInsets insets) {
        DisplayCutout displayCutout = insets.getDisplayCutout();
        if (displayCutout != null && this.mTvJump != null) {
            int mCutoutPaddingTop = displayCutout.getSafeInsetTop();
            ViewGroup.LayoutParams paramsJumpTemp = this.mTvJump.getLayoutParams();
            if (paramsJumpTemp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams paramsJump = (ViewGroup.MarginLayoutParams) paramsJumpTemp;
                paramsJump.topMargin = this.mTvJumpMargin + mCutoutPaddingTop;
                this.mTvJump.setLayoutParams(paramsJump);
            }
        }
    }

    private void resetTextMarginWhenRotation90(int cutoutPadding) {
        if (this.mTvAdvert != null && this.mTvJump != null) {
            if (isLayoutRtl()) {
                ViewGroup.LayoutParams paramsJumpTemp = this.mTvJump.getLayoutParams();
                if (paramsJumpTemp instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams paramsJump = (ViewGroup.MarginLayoutParams) paramsJumpTemp;
                    paramsJump.setMarginEnd(this.mTvJumpMargin + cutoutPadding);
                    this.mTvJump.setLayoutParams(paramsJump);
                    return;
                }
                return;
            }
            ViewGroup.LayoutParams advertParamsTemp = this.mTvAdvert.getLayoutParams();
            if (advertParamsTemp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams advertParams = (ViewGroup.MarginLayoutParams) advertParamsTemp;
                advertParams.setMarginStart(this.mTvJumpMargin + cutoutPadding);
                this.mTvAdvert.setLayoutParams(advertParams);
            }
        }
    }

    private void resetTextMarginWhenRotation270(int cutoutPadding) {
        if (this.mTvAdvert != null && this.mTvJump != null) {
            if (isLayoutRtl()) {
                ViewGroup.LayoutParams advertParamsTemp = this.mTvAdvert.getLayoutParams();
                if (advertParamsTemp instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams advertParams = (ViewGroup.MarginLayoutParams) advertParamsTemp;
                    advertParams.setMarginStart(this.mTvJumpMargin + cutoutPadding);
                    this.mTvAdvert.setLayoutParams(advertParams);
                    return;
                }
                return;
            }
            ViewGroup.LayoutParams paramsJumpTemp = this.mTvJump.getLayoutParams();
            if (paramsJumpTemp instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams paramsJump = (ViewGroup.MarginLayoutParams) paramsJumpTemp;
                paramsJump.setMarginEnd(this.mTvJumpMargin + cutoutPadding);
                this.mTvJump.setLayoutParams(paramsJump);
            }
        }
    }
}
