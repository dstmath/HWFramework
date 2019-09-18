package huawei.android.widget.pattern;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.ArrayList;

public class HwThirdPartyBrand extends FrameLayout {
    private LinearLayout mAllLinear;
    private Context mContext;
    private int mDividerHeight;
    private int mDividerWidth;
    private LinearLayout mLogoLinear;
    private ArrayList<ImageView> mLogoList;
    private int mMargin;
    private int mPadding;
    private int mSingleLineMaxWidth;
    private TextView mTvSupport;
    private int mWidth;

    public HwThirdPartyBrand(Context context) {
        this(context, null);
    }

    public HwThirdPartyBrand(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwThirdPartyBrand(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwThirdPartyBrand(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDividerWidth = 1;
        this.mLogoList = new ArrayList<>();
        this.mContext = context;
        initView();
    }

    private void initView() {
        ResLoaderUtil.getLayout(this.mContext, "hwpattern_third_party_brand", this, true);
        this.mAllLinear = (LinearLayout) findViewById(ResLoaderUtil.getViewId(this.mContext, "hw_third_party_brand_all_linear"));
        this.mTvSupport = (TextView) findViewById(ResLoaderUtil.getViewId(this.mContext, "hw_third_party_brand_tech_support_tv"));
        this.mLogoLinear = (LinearLayout) findViewById(ResLoaderUtil.getViewId(this.mContext, "hw_third_party_brand_logo_linear"));
        this.mPadding = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hw_third_party_brand_padding");
        this.mMargin = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hw_third_party_brand_margin");
        this.mDividerHeight = ResLoaderUtil.getDimensionPixelSize(this.mContext, "hw_third_party_brand_divider_height");
        this.mWidth = ResLoaderUtil.getResources(this.mContext).getDisplayMetrics().widthPixels;
        this.mSingleLineMaxWidth = this.mWidth - (2 * this.mPadding);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.mWidth = getMeasuredWidth();
        this.mSingleLineMaxWidth = this.mWidth - (2 * this.mPadding);
    }

    public void setElementText(CharSequence text) {
        if (this.mTvSupport != null && text != null && this.mLogoLinear != null) {
            this.mTvSupport.setText(text);
            if (this.mLogoLinear.getMeasuredWidth() > 0) {
                reLayout();
            }
        }
    }

    public void setElementImageResource(int... resIdArr) {
        if (resIdArr != null && this.mLogoLinear != null) {
            this.mLogoList.clear();
            this.mLogoLinear.removeAllViews();
            int logosAllWidth = 0;
            for (int resId : resIdArr) {
                ImageView thirdBrandLogo = new ImageView(this.mContext);
                thirdBrandLogo.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
                thirdBrandLogo.setMaxWidth(this.mSingleLineMaxWidth);
                thirdBrandLogo.setScaleType(ImageView.ScaleType.MATRIX);
                thirdBrandLogo.setImageResource(resId);
                thirdBrandLogo.measure(0, 0);
                int logoMeasuredWidth = thirdBrandLogo.getMeasuredWidth();
                logosAllWidth += logoMeasuredWidth < this.mSingleLineMaxWidth ? logoMeasuredWidth : this.mSingleLineMaxWidth;
                this.mLogoList.add(thirdBrandLogo);
            }
            logoLinearAddView(getLogosCount(logosAllWidth + ((resLength - 1) * (this.mMargin + this.mMargin + this.mDividerWidth)), this.mSingleLineMaxWidth), this.mSingleLineMaxWidth);
            this.mTvSupport.measure(0, 0);
            if (this.mTvSupport.getMeasuredWidth() > 0) {
                reLayout();
            }
        }
    }

    public void setElementImageDrawable(Drawable... drawables) {
        if (drawables != null && this.mLogoLinear != null) {
            this.mLogoList.clear();
            this.mLogoLinear.removeAllViews();
            int logosAllWidth = 0;
            for (Drawable drawable : drawables) {
                ImageView thirdBrandLogo = new ImageView(this.mContext);
                thirdBrandLogo.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
                thirdBrandLogo.setMaxWidth(this.mSingleLineMaxWidth);
                thirdBrandLogo.setScaleType(ImageView.ScaleType.MATRIX);
                thirdBrandLogo.setImageDrawable(drawable);
                thirdBrandLogo.measure(0, 0);
                int logoMeasuredWidth = thirdBrandLogo.getMeasuredWidth();
                logosAllWidth += logoMeasuredWidth < this.mSingleLineMaxWidth ? logoMeasuredWidth : this.mSingleLineMaxWidth;
                this.mLogoList.add(thirdBrandLogo);
            }
            logoLinearAddView(getLogosCount(logosAllWidth + ((drawablesLength - 1) * (this.mMargin + this.mMargin + this.mDividerWidth)), this.mSingleLineMaxWidth), this.mSingleLineMaxWidth);
            this.mTvSupport.measure(0, 0);
            if (this.mTvSupport.getMeasuredWidth() > 0) {
                reLayout();
            }
        }
    }

    private int getLogosCount(int logosAllWidth, int singleLineMaxWidth) {
        int logosCount;
        if (singleLineMaxWidth == 0) {
            return 0;
        }
        if (logosAllWidth % singleLineMaxWidth != 0) {
            logosCount = (logosAllWidth / singleLineMaxWidth) + 1;
        } else {
            logosCount = logosAllWidth / singleLineMaxWidth;
        }
        int logosCount2 = 2;
        if (logosCount <= 2) {
            logosCount2 = logosCount;
        }
        return logosCount2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00c9, code lost:
        r0 = r7;
     */
    private void logoLinearAddView(int logosCount, int singleLineMaxWidth) {
        if (this.mLogoLinear != null) {
            int j = 0;
            boolean startWithDivider = false;
            for (int i = 0; i < logosCount; i++) {
                LinearLayout singleLineLogosWarpper = new LinearLayout(this.mContext);
                singleLineLogosWarpper.setGravity(16);
                singleLineLogosWarpper.setLayoutParams(new LinearLayout.LayoutParams(-2, ResLoaderUtil.getDimensionPixelSize(this.mContext, "margin_xl")));
                int singleLineLogosWidth = 0;
                if (startWithDivider) {
                    singleLineLogosWidth = 0 + this.mDividerWidth + this.mMargin + this.mMargin;
                    singleLineLogosWarpper.addView(newDividerInstance());
                }
                int logoListSize = this.mLogoList.size();
                int startIndex = j;
                while (true) {
                    if (j >= logoListSize) {
                        break;
                    }
                    int logoMaxWidth = this.mLogoList.get(j).getMaxWidth();
                    int logoMeasuredWidth = this.mLogoList.get(j).getMeasuredWidth();
                    int singleLineLogosWidth2 = singleLineLogosWidth + (logoMeasuredWidth < logoMaxWidth ? logoMeasuredWidth : logoMaxWidth);
                    if (!startWithDivider || j != startIndex || singleLineLogosWidth2 <= singleLineMaxWidth) {
                        if (singleLineLogosWidth2 <= singleLineMaxWidth) {
                            singleLineLogosWarpper.addView(this.mLogoList.get(j));
                            startIndex = j + 1;
                            if (j != logoListSize - 1) {
                                int singleLineLogosWidth3 = singleLineLogosWidth2 + this.mDividerWidth + this.mMargin + this.mMargin;
                                if (singleLineLogosWidth3 <= singleLineMaxWidth) {
                                    int singleLineLogosWidth4 = singleLineLogosWidth3 + this.mLogoList.get(j + 1).getMeasuredWidth();
                                    if (i == 1 && singleLineLogosWidth4 > singleLineMaxWidth) {
                                        break;
                                    }
                                    singleLineLogosWidth = singleLineLogosWidth4 - this.mLogoList.get(j + 1).getMeasuredWidth();
                                    singleLineLogosWarpper.addView(newDividerInstance());
                                    j++;
                                } else {
                                    startWithDivider = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            int startIndex2 = j;
                            startWithDivider = false;
                            break;
                        }
                    } else {
                        singleLineLogosWarpper.addView(this.mLogoList.get(j));
                        startIndex = j + 1;
                        startWithDivider = false;
                        break;
                    }
                }
                this.mLogoLinear.addView(singleLineLogosWarpper);
            }
        }
    }

    private void reLayout() {
        this.mTvSupport.measure(0, 0);
        int techSupportTvWidth = this.mTvSupport.getMeasuredWidth();
        this.mLogoLinear.measure(0, 0);
        int brandsLogoLinearWidth = this.mLogoLinear.getMeasuredWidth();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        if (techSupportTvWidth + brandsLogoLinearWidth + this.mMargin >= this.mSingleLineMaxWidth || this.mLogoLinear.getChildCount() != 1) {
            this.mAllLinear.setOrientation(1);
            params.setMarginEnd(0);
            this.mTvSupport.setLayoutParams(params);
            return;
        }
        this.mAllLinear.setOrientation(0);
        if (brandsLogoLinearWidth != 0) {
            params.setMarginEnd(this.mMargin);
        } else {
            params.setMarginEnd(0);
        }
        this.mTvSupport.setLayoutParams(params);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mWidth = ResLoaderUtil.getResources(this.mContext).getDisplayMetrics().widthPixels;
        this.mSingleLineMaxWidth = this.mWidth - (2 * this.mPadding);
        int childCount = this.mLogoLinear.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((ViewGroup) this.mLogoLinear.getChildAt(i)).removeAllViews();
        }
        this.mLogoLinear.removeAllViews();
        int logoListSize = this.mLogoList.size();
        int logosAllWidth = 0;
        for (int i2 = 0; i2 < logoListSize; i2++) {
            ImageView thirdBrandLogo = this.mLogoList.get(i2);
            thirdBrandLogo.setMaxWidth(this.mSingleLineMaxWidth);
            thirdBrandLogo.setScaleType(ImageView.ScaleType.MATRIX);
            thirdBrandLogo.measure(0, 0);
            int logoMeasuredWidth = thirdBrandLogo.getMeasuredWidth();
            logosAllWidth += logoMeasuredWidth < this.mSingleLineMaxWidth ? logoMeasuredWidth : this.mSingleLineMaxWidth;
        }
        logoLinearAddView(getLogosCount(logosAllWidth + ((logoListSize - 1) * (this.mMargin + this.mMargin + this.mDividerWidth)), this.mSingleLineMaxWidth), this.mSingleLineMaxWidth);
        this.mTvSupport.measure(0, 0);
        if (this.mTvSupport.getMeasuredWidth() > 0) {
            reLayout();
        }
    }

    private View newDividerInstance() {
        View divider = new View(this.mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(this.mDividerWidth, this.mDividerHeight);
        lp.setMargins(this.mMargin, 0, this.mMargin, 0);
        Resources.Theme theme = ResLoader.getInstance().getTheme(getContext());
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(new int[]{16843530});
            Drawable drawable = typedArray.getDrawable(0);
            if (drawable != null) {
                divider.setBackground(drawable);
            } else {
                divider.setBackgroundColor(typedArray.getColor(0, ResLoaderUtil.getColor(getContext(), "emui_black")));
            }
            typedArray.recycle();
        }
        divider.setLayoutParams(lp);
        return divider;
    }
}
