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
    private TextView mTvAd;
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
        this.mTvAd = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_ad"));
        this.mVideoIcon = (ImageView) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_video_icon"));
        this.mTvVideo = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_video_text"));
        this.mTvCompanyRight = (TextView) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_company_right_text"));
        this.mTvVideoInfo = (LinearLayout) findViewById(ResLoaderUtil.getViewId(context, "hw_splash_view_video_info"));
        if (Build.VERSION.SDK_INT > 27) {
            this.mHwCutoutUtil = new HwCutoutUtil();
            HwCutoutUtil hwCutoutUtil = this.mHwCutoutUtil;
            this.mDisplayRotate = HwCutoutUtil.getDisplayRotate(context);
            float fontScale = getContext().getResources().getConfiguration().fontScale;
            if (this.mDisplayRotate == 1 || this.mDisplayRotate == 3 || fontScale > 1.0f) {
                LinearLayout.LayoutParams paramsTvVideoInfo = (LinearLayout.LayoutParams) this.mTvVideoInfo.getLayoutParams();
                paramsTvVideoInfo.topMargin = this.mTvVideoMarginTop;
                this.mTvVideoInfo.setLayoutParams(paramsTvVideoInfo);
            }
        }
    }

    public void setElementText(CharSequence text, int elementTag) {
        if (text != null) {
            switch (elementTag) {
                case 1:
                    this.mTvJump.setText(text);
                    break;
                case 2:
                    this.mTvAd.setText(text);
                    break;
                case 3:
                    this.mTvVideo.setText(text);
                    break;
                case 4:
                    this.mTvCompanyRight.setText(text);
                    break;
            }
        }
    }

    public void addDisplayView(View view) {
        if (view != null) {
            this.mMainDisplayParent.addView(view);
        }
    }

    public void setBottomIconImageDrawable(Drawable drawable) {
        if (!(drawable == null || this.mVideoIcon == null)) {
            this.mVideoIcon.setImageDrawable(drawable);
        }
    }

    public void setBottomIconImageResource(int resId) {
        if (this.mVideoIcon != null) {
            this.mVideoIcon.setImageResource(resId);
        }
    }

    public void setJumpTextClick(View.OnClickListener clickListener) {
        this.mTvJump.setOnClickListener(clickListener);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (this.mHwCutoutUtil != null) {
            this.mHwCutoutUtil.checkCutoutStatus(insets, this, getContext());
            int mCutoutPadding = this.mHwCutoutUtil.getCutoutPadding();
            HwCutoutUtil hwCutoutUtil = this.mHwCutoutUtil;
            boolean mHasNotchInScreenm = HwCutoutUtil.needDoCutoutFit(this, getContext());
            ViewGroup.MarginLayoutParams paramsJump = (ViewGroup.MarginLayoutParams) this.mTvJump.getLayoutParams();
            ViewGroup.MarginLayoutParams paramsAd = (ViewGroup.MarginLayoutParams) this.mTvAd.getLayoutParams();
            if (mHasNotchInScreenm) {
                if (getResources().getConfiguration().orientation == 1) {
                    DisplayCutout displayCutout = null;
                    if (insets != null) {
                        displayCutout = insets.getDisplayCutout();
                    }
                    if (displayCutout != null) {
                        paramsJump.topMargin = this.mTvJumpMargin + displayCutout.getSafeInsetTop();
                        this.mTvJump.setLayoutParams(paramsJump);
                    }
                } else if (this.mTvAd == null || this.mTvJump == null || this.mDisplayRotate != 1) {
                    if (!(this.mTvAd == null || this.mTvJump == null || this.mDisplayRotate != 3)) {
                        if (isLayoutRtl()) {
                            paramsAd.setMarginStart(this.mTvJumpMargin + mCutoutPadding);
                            this.mTvAd.setLayoutParams(paramsAd);
                        } else {
                            paramsJump.setMarginEnd(this.mTvJumpMargin + mCutoutPadding);
                            this.mTvJump.setLayoutParams(paramsJump);
                        }
                    }
                } else if (isLayoutRtl()) {
                    paramsJump.setMarginEnd(this.mTvJumpMargin + mCutoutPadding);
                    this.mTvJump.setLayoutParams(paramsJump);
                } else {
                    paramsAd.setMarginStart(this.mTvJumpMargin + mCutoutPadding);
                    this.mTvAd.setLayoutParams(paramsAd);
                }
            }
        }
        return super.onApplyWindowInsets(insets);
    }
}
