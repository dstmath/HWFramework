package huawei.android.widget.pattern;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.ViewPager;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.ArrayList;

public class HwHalfScreenImageText extends LinearLayout {
    private static final float LANDSCAPE_IMAGE_HEIGHT_RATIO_SCREEN = 0.5f;
    private static final float PORTRAIT_IMAGE_HEIGHT_RATIO = 2.0f;
    private static final float PORTRAIT_IMAGE_WIDTH_RATIO = 3.0f;
    private static final float ROUNDING_CONSTANT = 0.5f;
    private Context mContext;
    private FrameLayout mFlyIndicatorWrapper;
    private HwImageTextIndicator mIndicator;
    private TextView mTvContent;
    private TextView mTvTitle;
    private ViewPager mViewPager;

    public HwHalfScreenImageText(Context context) {
        this(context, null);
    }

    public HwHalfScreenImageText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwHalfScreenImageText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwHalfScreenImageText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        initView();
    }

    private void initView() {
        ResLoaderUtil.getLayout(this.mContext, "hwpattern_half_screen_image_text_layout", this, true);
        this.mViewPager = findViewById(ResLoaderUtil.getViewId(this.mContext, "hwhalfscreen_image_text_vp_top"));
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwhalfscreen_image_text_tv_title"));
        this.mTvContent = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwhalfscreen_image_text_tv_content"));
        this.mFlyIndicatorWrapper = (FrameLayout) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwhalfscreen_image_text_fly_indicator_wrapper"));
        this.mIndicator = (HwImageTextIndicator) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwhalfscreen_image_text_indicator"));
        TextView textView = this.mTvContent;
        if (textView != null) {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            ViewPager viewPager = this.mViewPager;
            if (viewPager != null) {
                viewPager.post(new Runnable() {
                    /* class huawei.android.widget.pattern.HwHalfScreenImageText.AnonymousClass1 */

                    @Override // java.lang.Runnable
                    public void run() {
                        int width = HwHalfScreenImageText.this.mViewPager.getWidth();
                        ViewGroup.LayoutParams layoutParams = HwHalfScreenImageText.this.mViewPager.getLayoutParams();
                        if (HwHalfScreenImageText.this.getResources().getConfiguration().orientation == 1) {
                            layoutParams.height = (int) (((((float) width) * HwHalfScreenImageText.PORTRAIT_IMAGE_HEIGHT_RATIO) / HwHalfScreenImageText.PORTRAIT_IMAGE_WIDTH_RATIO) + 0.5f);
                        } else if (HwHalfScreenImageText.this.getResources().getConfiguration().orientation == 2) {
                            layoutParams.height = (int) ((((float) HwHalfScreenImageText.this.getResources().getDisplayMetrics().heightPixels) * 0.5f) + 0.5f);
                        }
                        HwHalfScreenImageText.this.mViewPager.setLayoutParams(layoutParams);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePageSelected(int position, ArrayList<HwImageTextBean> hwImageTextBeans) {
        HwImageTextBean bean;
        if (this.mTvTitle != null && this.mTvContent != null && hwImageTextBeans != null && hwImageTextBeans.size() != 0 && position >= 0 && position < hwImageTextBeans.size() && (bean = hwImageTextBeans.get(position)) != null) {
            if (bean.mTitle == null) {
                this.mTvTitle.setVisibility(8);
            } else {
                this.mTvTitle.setVisibility(0);
                this.mTvTitle.setText(bean.mTitle);
            }
            this.mTvContent.setText(bean.mContent);
        }
    }

    public void setData(final ArrayList<HwImageTextBean> hwImageTextBeans) {
        if (hwImageTextBeans != null && hwImageTextBeans.size() != 0 && this.mViewPager != null && this.mIndicator != null) {
            this.mViewPager.setAdapter(new HwImageTextAdapter(this.mContext, hwImageTextBeans, ImageView.ScaleType.FIT_CENTER));
            this.mIndicator.setViewPager(this.mViewPager);
            this.mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                /* class huawei.android.widget.pattern.HwHalfScreenImageText.AnonymousClass2 */

                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                public void onPageSelected(int position) {
                    HwHalfScreenImageText.this.handlePageSelected(position, hwImageTextBeans);
                }

                public void onPageScrollStateChanged(int state) {
                }
            });
            handlePageSelected(this.mViewPager.getCurrentItem(), hwImageTextBeans);
        }
    }

    public void showOrHideIndicator(boolean isShowOrHide) {
        FrameLayout frameLayout = this.mFlyIndicatorWrapper;
        if (frameLayout != null) {
            int visibility = frameLayout.getVisibility();
            if (!isShowOrHide || visibility != 0) {
                int i = 8;
                if (isShowOrHide || visibility != 8) {
                    FrameLayout frameLayout2 = this.mFlyIndicatorWrapper;
                    if (isShowOrHide) {
                        i = 0;
                    }
                    frameLayout2.setVisibility(i);
                }
            }
        }
    }
}
