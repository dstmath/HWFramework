package huawei.android.widget.pattern;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.ViewPager;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.ArrayList;

public class HwFullScreenImageText extends LinearLayout {
    private static final int BTM_BTN_MIN_WIDTH_DP = 180;
    private static final int BTM_BTN_PADDING_DP = 16;
    private static final int BTM_BTN_SECOND_MARGIN_START_DP = 8;
    private static final int BTM_BTN_SECOND_MARGIN_TOP_DP = 8;
    public static final int ELE_BTN_FIRST = 1;
    public static final int ELE_BTN_SECOND = 2;
    private Button mBtnBtmFirst;
    private Button mBtnBtmSecond;
    private Context mContext;
    private FrameLayout mFlyIndicatorWrapper;
    private HwImageTextIndicator mIndicator;
    private boolean mIsIndicatorShowFlag;
    private LinearLayout mLlyBtmBtnWrapper;
    private TextView mTvContent;
    private TextView mTvTitle;
    private ViewPager mViewPager;

    public HwFullScreenImageText(Context context) {
        this(context, null);
    }

    public HwFullScreenImageText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwFullScreenImageText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwFullScreenImageText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mIsIndicatorShowFlag = true;
        this.mContext = context;
        initView();
    }

    private void initView() {
        ResLoaderUtil.getLayout(this.mContext, "hwpattern_full_screen_image_text_layout", this, true);
        this.mViewPager = findViewById(ResLoaderUtil.getViewId(this.mContext, "hwfullscreen_image_text_vp_top"));
        this.mTvTitle = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwfullscreen_image_text_tv_title"));
        this.mTvContent = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwfullscreen_image_text_tv_content"));
        this.mFlyIndicatorWrapper = (FrameLayout) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwfullscreen_image_text_fly_indicator_wrapper"));
        this.mIndicator = (HwImageTextIndicator) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwfullscreen_image_text_indicator"));
        this.mLlyBtmBtnWrapper = (LinearLayout) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwfullscreen_image_text_btm_btn_lly_wrapper"));
        this.mBtnBtmFirst = (Button) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwfullscreen_image_text_btm_btn_first"));
        this.mBtnBtmSecond = (Button) findViewById(ResLoaderUtil.getViewId(this.mContext, "hwfullscreen_image_text_btm_btn_second"));
        if (this.mTvContent != null) {
            Resources.Theme theme = ResLoader.getInstance().getTheme(this.mContext);
            int linkTextBgColorId = ResLoader.getInstance().getIdentifier(this.mContext, "attr", "hwGuidanceTipsIconBg");
            int linkTextBgColor = 0;
            if (!(theme == null || linkTextBgColorId == 0)) {
                TypedArray typedArray = theme.obtainStyledAttributes(new int[]{linkTextBgColorId});
                linkTextBgColor = typedArray.getColorStateList(0).getColorForState(new int[]{16842919}, 0);
                typedArray.recycle();
            }
            this.mTvContent.setMovementMethod(new LinkTouchMovementMethod(linkTextBgColor));
            ViewPager viewPager = this.mViewPager;
            if (viewPager != null) {
                viewPager.post(new Runnable() {
                    /* class huawei.android.widget.pattern.HwFullScreenImageText.AnonymousClass1 */

                    public void run() {
                        Resources.Theme theme = ResLoader.getInstance().getTheme(HwFullScreenImageText.this.mContext);
                        int selectDotColor = 0;
                        int unselectDotColor = 0;
                        if (HwFullScreenImageText.this.getResources().getConfiguration().orientation == 2) {
                            if (theme != null) {
                                TypedArray typedArraySelectDotLandscape = theme.obtainStyledAttributes(new int[]{16843270});
                                selectDotColor = typedArraySelectDotLandscape.getColor(0, 0);
                                unselectDotColor = selectDotColor;
                                typedArraySelectDotLandscape.recycle();
                            }
                        } else if (HwFullScreenImageText.this.getResources().getConfiguration().orientation == 1) {
                            if (theme != null) {
                                TypedArray typedArraySelectDotPortrait = theme.obtainStyledAttributes(new int[]{16843818});
                                TypedArray typedArrayUnselectDotPortrait = theme.obtainStyledAttributes(new int[]{16842800});
                                selectDotColor = typedArraySelectDotPortrait.getColor(0, 0);
                                unselectDotColor = typedArrayUnselectDotPortrait.getColor(0, 0);
                                typedArraySelectDotPortrait.recycle();
                                typedArrayUnselectDotPortrait.recycle();
                            }
                            int width = HwFullScreenImageText.this.mViewPager.getWidth();
                            ViewGroup.LayoutParams layoutParams = HwFullScreenImageText.this.mViewPager.getLayoutParams();
                            layoutParams.height = width;
                            HwFullScreenImageText.this.mViewPager.setLayoutParams(layoutParams);
                        }
                        HwFullScreenImageText hwFullScreenImageText = HwFullScreenImageText.this;
                        hwFullScreenImageText.showOrHideIndicator(hwFullScreenImageText.mIsIndicatorShowFlag);
                        HwFullScreenImageText.this.mIndicator.setDotsColor(selectDotColor, unselectDotColor);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePageSelected(int position, ArrayList<HwImageTextBean> hwImageTextBeans) {
        if (this.mTvTitle != null && this.mTvContent != null && hwImageTextBeans != null && hwImageTextBeans.size() != 0 && position >= 0 && position < hwImageTextBeans.size()) {
            HwImageTextBean bean = hwImageTextBeans.get(position);
            if (bean.mTitle == null) {
                this.mTvTitle.setVisibility(8);
            } else {
                this.mTvTitle.setVisibility(0);
                this.mTvTitle.setText(bean.mTitle);
            }
            if (bean.mSpannableText == null) {
                this.mTvContent.setText(bean.mContent);
            } else {
                this.mTvContent.setText(bean.mSpannableText);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(1, dpValue, this.mContext.getResources().getDisplayMetrics());
    }

    public void setData(final ArrayList<HwImageTextBean> hwImageTextBeans) {
        if (hwImageTextBeans != null && hwImageTextBeans.size() != 0 && this.mViewPager != null && this.mIndicator != null) {
            this.mViewPager.setAdapter(new HwImageTextAdapter(this.mContext, hwImageTextBeans, ImageView.ScaleType.CENTER_CROP));
            this.mIndicator.setViewPager(this.mViewPager);
            this.mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                /* class huawei.android.widget.pattern.HwFullScreenImageText.AnonymousClass2 */

                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                public void onPageSelected(int position) {
                    HwFullScreenImageText.this.handlePageSelected(position, hwImageTextBeans);
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
                    this.mIsIndicatorShowFlag = isShowOrHide;
                    if (this.mLlyBtmBtnWrapper != null) {
                        boolean isOrientationPortrait = true;
                        if (getResources().getConfiguration().orientation != 1) {
                            isOrientationPortrait = false;
                        }
                        if (isShowOrHide || !isOrientationPortrait) {
                            this.mLlyBtmBtnWrapper.setPadding(0, 0, 0, dp2px(16.0f));
                        } else {
                            this.mLlyBtmBtnWrapper.setPadding(0, dp2px(16.0f), 0, dp2px(16.0f));
                        }
                    }
                }
            }
        }
    }

    public void setButtonsText(CharSequence... buttonText) {
        LinearLayout linearLayout = this.mLlyBtmBtnWrapper;
        if (linearLayout != null && this.mBtnBtmFirst != null && this.mBtnBtmSecond != null) {
            int length = buttonText.length;
            if (length == 0) {
                if (linearLayout.getVisibility() != 8) {
                    this.mLlyBtmBtnWrapper.setVisibility(8);
                }
            } else if (length == 1) {
                if (linearLayout.getVisibility() != 0) {
                    this.mLlyBtmBtnWrapper.setVisibility(0);
                }
                if (this.mBtnBtmFirst.getVisibility() != 0) {
                    this.mBtnBtmFirst.setVisibility(0);
                }
                if (this.mBtnBtmSecond.getVisibility() != 8) {
                    this.mBtnBtmSecond.setVisibility(8);
                }
                this.mBtnBtmFirst.setText(buttonText[0]);
                this.mLlyBtmBtnWrapper.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
                this.mLlyBtmBtnWrapper.setMinimumWidth(0);
                this.mBtnBtmFirst.setMinimumWidth(dp2px(180.0f));
                this.mBtnBtmFirst.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            } else if (length > 1) {
                if (linearLayout.getVisibility() != 0) {
                    this.mLlyBtmBtnWrapper.setVisibility(0);
                }
                if (this.mBtnBtmFirst.getVisibility() != 0) {
                    this.mBtnBtmFirst.setVisibility(0);
                }
                if (this.mBtnBtmSecond.getVisibility() != 0) {
                    this.mBtnBtmSecond.setVisibility(0);
                }
                this.mBtnBtmFirst.setText(buttonText[0]);
                this.mBtnBtmSecond.setText(buttonText[1]);
                LinearLayout.LayoutParams paramsFirstH = new LinearLayout.LayoutParams(0, -2);
                LinearLayout.LayoutParams paramsSecondH = new LinearLayout.LayoutParams(0, -2);
                paramsFirstH.weight = 1.0f;
                paramsSecondH.weight = 1.0f;
                paramsSecondH.setMarginStart(dp2px(8.0f));
                this.mLlyBtmBtnWrapper.setOrientation(0);
                this.mLlyBtmBtnWrapper.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
                this.mLlyBtmBtnWrapper.setMinimumWidth(0);
                this.mBtnBtmFirst.setMinimumWidth(0);
                this.mBtnBtmFirst.setLayoutParams(paramsFirstH);
                this.mBtnBtmSecond.setLayoutParams(paramsSecondH);
                this.mBtnBtmFirst.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    /* class huawei.android.widget.pattern.HwFullScreenImageText.AnonymousClass3 */

                    public void onGlobalLayout() {
                        HwFullScreenImageText.this.mBtnBtmFirst.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        if (HwFullScreenImageText.this.mBtnBtmFirst.getLayout() != null && HwFullScreenImageText.this.mBtnBtmSecond.getLayout() != null) {
                            if (HwFullScreenImageText.this.mBtnBtmFirst.getLayout().getEllipsisCount(0) > 0 || HwFullScreenImageText.this.mBtnBtmSecond.getLayout().getEllipsisCount(0) > 0) {
                                LinearLayout.LayoutParams paramsWrapperV = new LinearLayout.LayoutParams(-2, -2);
                                LinearLayout.LayoutParams paramsSecondV = new LinearLayout.LayoutParams(-1, -2);
                                paramsSecondV.topMargin = HwFullScreenImageText.this.dp2px(8.0f);
                                HwFullScreenImageText.this.mLlyBtmBtnWrapper.setOrientation(1);
                                HwFullScreenImageText.this.mLlyBtmBtnWrapper.setLayoutParams(paramsWrapperV);
                                HwFullScreenImageText.this.mLlyBtmBtnWrapper.setMinimumWidth(HwFullScreenImageText.this.dp2px(180.0f));
                                HwFullScreenImageText.this.mBtnBtmFirst.setMinimumWidth(0);
                                HwFullScreenImageText.this.mBtnBtmFirst.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
                                HwFullScreenImageText.this.mBtnBtmSecond.setLayoutParams(paramsSecondV);
                            }
                        }
                    }
                });
            }
        }
    }

    public void setElementOnClickListener(View.OnClickListener listener, int elementTag) {
        Button button;
        if (elementTag == 1) {
            Button button2 = this.mBtnBtmFirst;
            if (button2 != null && button2.getVisibility() == 0) {
                this.mBtnBtmFirst.setOnClickListener(listener);
            }
        } else if (elementTag == 2 && (button = this.mBtnBtmSecond) != null && button.getVisibility() == 0) {
            this.mBtnBtmSecond.setOnClickListener(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public class LinkTouchMovementMethod extends LinkMovementMethod {
        private BackgroundColorSpan mBackgroundColorSpan;

        LinkTouchMovementMethod(int pressedBackgroundColor) {
            this.mBackgroundColorSpan = new BackgroundColorSpan(pressedBackgroundColor);
        }

        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            if (event == null) {
                return false;
            }
            int action = event.getAction();
            if (action == 1 || action == 0) {
                int x = ((int) event.getX()) - widget.getTotalPaddingLeft();
                int y = ((int) event.getY()) - widget.getTotalPaddingTop();
                int x2 = x + widget.getScrollX();
                int y2 = y + widget.getScrollY();
                Layout layout = widget.getLayout();
                int off = layout.getOffsetForHorizontal(layout.getLineForVertical(y2), (float) x2);
                ClickableSpan[] links = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                if (links.length != 0) {
                    ClickableSpan link = links[0];
                    if (action == 1) {
                        link.onClick(widget);
                        buffer.removeSpan(this.mBackgroundColorSpan);
                        widget.setText(buffer);
                    } else if (action == 0) {
                        Selection.setSelection(buffer, buffer.getSpanStart(link), buffer.getSpanEnd(link));
                        buffer.setSpan(this.mBackgroundColorSpan, buffer.getSpanStart(link), buffer.getSpanEnd(link), 18);
                    }
                    return true;
                }
                buffer.removeSpan(this.mBackgroundColorSpan);
                Selection.removeSelection(buffer);
            }
            return super.onTouchEvent(widget, buffer, event);
        }
    }
}
