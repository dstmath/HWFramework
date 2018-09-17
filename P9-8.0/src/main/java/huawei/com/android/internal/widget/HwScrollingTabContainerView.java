package huawei.com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hwcontrol.HwWidgetFactory;
import android.os.FreezeScreenScene;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.widget.ScrollingTabContainerView;
import com.android.internal.widget.ScrollingTabContainerView.TabView;
import huawei.com.android.internal.app.HwActionBarImpl.HwTabImpl;

public class HwScrollingTabContainerView extends ScrollingTabContainerView {
    private static final float DOUBLE = 2.0f;
    private static final float SCALESIZE = 1.2f;
    private static final float SINGLE = 1.0f;
    private String TAG = "HwSrollingTabContainerView";
    private boolean isWidthPixelsChanged = false;
    private int mAvailableSpace = 0;
    private float mDensity;
    private int mIndicatorHeight;
    private boolean mIsHwDarkTheme;
    private boolean mIsHwEmphasizeTheme;
    private int mLastContainerWidth;
    private boolean mRTLFlag;
    private int mRealWidthPixels;
    private Typeface mRegular;
    private Typeface mRegularCondensed;
    private int mScrollDirection;
    private int mScrollerLastPos;
    private boolean mTabChanged = false;
    private ImageView mTabIndicator;
    private TabIndicatorAnimation mTabIndicatorAnimation;
    private int mTabLayoutWidth;
    private int mTabTitleSelectedColor;
    private int mWidthPixels = 0;
    private WindowManager mWindowManager = ((WindowManager) getContext().getSystemService(FreezeScreenScene.WINDOW_PARAM));

    class TabIndicatorAnimation {
        private static final float SCALE_DELTA_ERROR = 0.01f;
        private ValueAnimator mAnimator;
        private boolean mIsAnimEnd = true;
        private View mView;
        private int mWidth;

        TabIndicatorAnimation(View view) {
            this.mView = view;
        }

        public void setViewWidth(int width) {
            this.mWidth = width;
        }

        public void startAnim(int from, int to) {
            this.mIsAnimEnd = false;
            if (from == to) {
                this.mIsAnimEnd = true;
                HwScrollingTabContainerView.this.mTabClicked = false;
                return;
            }
            int l2r;
            if (from < to) {
                cancelAnim();
                l2r = 1;
            } else {
                cancelAnim();
                l2r = 0;
            }
            anim(from, to, l2r);
        }

        public void cancelAnim() {
            if (this.mAnimator != null) {
                this.mAnimator.cancel();
            }
        }

        private int getX1() {
            LayoutParams lp = (LayoutParams) this.mView.getLayoutParams();
            if (HwScrollingTabContainerView.this.mRTLFlag) {
                return lp.rightMargin;
            }
            return lp.leftMargin;
        }

        private int getX2() {
            LayoutParams lp = (LayoutParams) this.mView.getLayoutParams();
            if (HwScrollingTabContainerView.this.mRTLFlag) {
                return lp.rightMargin + lp.width;
            }
            return lp.leftMargin + lp.width;
        }

        private float getScaleX() {
            return ((float) ((LayoutParams) this.mView.getLayoutParams()).width) / ((float) this.mWidth);
        }

        private void setLine(int x1, int x2) {
            LayoutParams lp = (LayoutParams) this.mView.getLayoutParams();
            if (HwScrollingTabContainerView.this.mRTLFlag) {
                lp.rightMargin = x1;
            } else {
                lp.leftMargin = x1;
            }
            lp.width = x2 - x1;
            if (lp.width > ((int) (((float) this.mWidth) * HwScrollingTabContainerView.SCALESIZE))) {
                lp.width = (int) (((float) this.mWidth) * HwScrollingTabContainerView.SCALESIZE);
            }
            if (lp.width < this.mWidth) {
                lp.width = this.mWidth;
            }
            this.mView.setLayoutParams(lp);
            this.mView.setMinimumWidth(lp.width);
        }

        private void setTranslationX(int x) {
            LayoutParams lp = (LayoutParams) this.mView.getLayoutParams();
            if (HwScrollingTabContainerView.this.mRTLFlag) {
                lp.rightMargin = x;
            } else {
                lp.leftMargin = x;
            }
            this.mView.setLayoutParams(lp);
        }

        public boolean isAnimEnd() {
            return this.mIsAnimEnd;
        }

        private void anim(int from, int to, int l2r) {
            int totalLength;
            int duration;
            final int toX1 = to * this.mWidth;
            final int toX2 = (to + 1) * this.mWidth;
            int x1 = getX1();
            int x2 = getX2();
            if (from < to) {
                totalLength = (int) (((float) Math.abs(toX2 - x2)) + (((float) this.mWidth) * 0.20000005f));
            } else {
                totalLength = (int) (((float) Math.abs(toX1 - x1)) + (((float) this.mWidth) * 0.20000005f));
            }
            if (Math.abs(to - from) > 1) {
                duration = 250;
            } else {
                duration = 200;
            }
            final int stepSize = (int) (((float) totalLength) / (((float) duration) / 16.0f));
            this.mAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
            this.mAnimator.setTarget(this.mView);
            this.mAnimator.setDuration((long) duration).start();
            this.mAnimator.setInterpolator(new PathInterpolator(0.2f, 0.5f, 0.8f, 0.5f));
            final int i = l2r;
            this.mAnimator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    int x1 = TabIndicatorAnimation.this.getX1();
                    int x2 = TabIndicatorAnimation.this.getX2();
                    float scale = TabIndicatorAnimation.this.getScaleX();
                    if (x1 == toX1 && x2 == toX2) {
                        TabIndicatorAnimation.this.mAnimator.cancel();
                    }
                    int step = stepSize;
                    if (i == 1) {
                        if (x2 < toX2) {
                            if (scale >= 1.0f && scale < 1.19f) {
                                if (stepSize + x2 > toX2) {
                                    step = toX2 - x2;
                                }
                                TabIndicatorAnimation.this.setLine(x1, x2 + step);
                            } else if (Math.abs(scale - HwScrollingTabContainerView.SCALESIZE) <= TabIndicatorAnimation.SCALE_DELTA_ERROR) {
                                if (stepSize + x2 > toX2) {
                                    step = toX2 - x2;
                                }
                                TabIndicatorAnimation.this.setTranslationX(x1 + step);
                            }
                        } else if (x2 == toX2) {
                            if (x1 < toX1) {
                                if (stepSize + x1 > toX1) {
                                    step = toX1 - x1;
                                }
                                TabIndicatorAnimation.this.setLine(x1 + step, x2);
                            }
                        } else if (x2 > toX2) {
                            if (x2 - stepSize < toX2) {
                                step = x2 - toX2;
                            }
                            TabIndicatorAnimation.this.setLine(x1, x2 - step);
                        }
                    } else if (x1 > toX1) {
                        if (scale >= 1.0f && scale < 1.19f) {
                            if (x1 - stepSize < toX1) {
                                step = x1 - toX1;
                            }
                            TabIndicatorAnimation.this.setLine(x1 - step, x2);
                        } else if (Math.abs(scale - HwScrollingTabContainerView.SCALESIZE) <= TabIndicatorAnimation.SCALE_DELTA_ERROR) {
                            if (x1 - stepSize < toX1) {
                                step = x1 - toX1;
                            }
                            TabIndicatorAnimation.this.setTranslationX(x1 - step);
                        } else {
                            int i = (scale > 1.0f ? 1 : (scale == 1.0f ? 0 : -1));
                        }
                    } else if (x1 == toX1) {
                        if (x2 > toX2) {
                            if (x2 - stepSize < toX2) {
                                step = x2 - toX2;
                            }
                            TabIndicatorAnimation.this.setLine(x1, x2 - step);
                        }
                    } else if (x1 < toX1) {
                        if (stepSize + x1 > toX1) {
                            step = toX1 - x1;
                        }
                        TabIndicatorAnimation.this.setLine(x1 + step, x2);
                    }
                }
            });
            this.mAnimator.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                    TabIndicatorAnimation.this.mIsAnimEnd = true;
                    HwScrollingTabContainerView.this.mTabClicked = false;
                    TabIndicatorAnimation.this.setLine(toX1, toX2);
                }

                public void onAnimationCancel(Animator animation) {
                    HwScrollingTabContainerView.this.mTabClicked = false;
                }
            });
        }
    }

    public HwScrollingTabContainerView(Context context) {
        super(context);
        this.mDensity = context.getResources().getDisplayMetrics().density;
        this.mIsHwDarkTheme = HwWidgetFactory.isHwDarkTheme(context);
        this.mIsHwEmphasizeTheme = HwWidgetFactory.isHwEmphasizeTheme(context);
        updateTabViewContainerWidth(context);
        this.mRegular = Typeface.create("sans-serif", 0);
        this.mRegularCondensed = Typeface.create("sans-serif-condensed-regular", 0);
        this.mRTLFlag = SystemProperties.getRTLFlag();
        createTabIndicator();
    }

    public void setShouldAnimToTab(boolean shouldAnim) {
        this.mShouldAnimToTab = shouldAnim;
    }

    private void updateOrigPos() {
        LinearLayout tabLayout = getTabLayout();
        this.mTabLayoutWidth = tabLayout.getWidth();
        TabView tabView = (TabView) tabLayout.getChildAt(getSelectedTabIndex());
        int translationX = tabView.getLeft();
        if (this.mRTLFlag) {
            translationX = this.mRealWidthPixels - tabView.getRight();
        }
        int translationY = tabView.getBottom() - this.mIndicatorHeight;
        if (this.mTabIndicatorAnimation != null) {
            this.mTabIndicatorAnimation.cancelAnim();
        }
        int count = tabLayout.getChildCount();
        this.mTabIndicator.setTranslationY((float) translationY);
        LayoutParams lp = (LayoutParams) this.mTabIndicator.getLayoutParams();
        if (this.mRTLFlag) {
            lp.rightMargin = translationX;
        } else {
            lp.leftMargin = translationX;
        }
        lp.width = this.mRealWidthPixels / count;
        this.mTabIndicator.setLayoutParams(lp);
        this.mTabIndicator.setMinimumWidth(lp.width);
        if (this.mTabIndicatorAnimation != null) {
            this.mTabIndicatorAnimation.setViewWidth(lp.width);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateTabViewContainerWidth(this.mContext);
        destroyTabIndicator();
        createTabIndicator();
        getTabLayout().requestLayout();
    }

    public void updateTabViewContainerWidth(Context context) {
        int tabContainerViewWidth = context.getResources().getDimensionPixelOffset(34472070);
        boolean isLandscape = context.getResources().getConfiguration().orientation == 2;
        if (tabContainerViewWidth != 0) {
            this.mWidthPixels = tabContainerViewWidth;
            return;
        }
        int sw = (int) (this.mDensity * ((float) context.getResources().getConfiguration().screenWidthDp));
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        if (point.x < point.y) {
            this.mWidthPixels = point.x;
        } else if (isLandscape) {
            this.mWidthPixels = (sw * 8) / 12;
        } else {
            this.mWidthPixels = sw;
        }
    }

    private void createTabIndicator() {
        this.mIndicatorHeight = getResources().getDimensionPixelSize(34472180);
        if (!HwWidgetUtils.isActionbarBackgroundThemed(getContext())) {
            int titleColorRes;
            if (this.mTabIndicator == null) {
                this.mTabIndicator = new ImageView(this.mContext);
            }
            if (this.mIsHwDarkTheme) {
                titleColorRes = 33882247;
            } else if (this.mIsHwEmphasizeTheme) {
                titleColorRes = 33882248;
            } else {
                titleColorRes = 33882241;
            }
            this.mTabTitleSelectedColor = this.mContext.getResources().getColor(titleColorRes);
            this.mTabIndicator.setBackgroundColor(this.mTabTitleSelectedColor);
            this.mTabIndicator.setScaleType(ScaleType.FIT_XY);
            LayoutParams lp = new LayoutParams(-1, -2);
            lp.width = this.mWidthPixels;
            lp.height = this.mIndicatorHeight;
            this.mTabIndicator.setLayoutParams(lp);
            if (this.mTabIndicatorAnimation == null) {
                this.mTabIndicatorAnimation = new TabIndicatorAnimation(this.mTabIndicator);
            }
        }
    }

    private void destroyTabIndicator() {
        if (this.mTabIndicator != null) {
            removeView(this.mTabIndicator);
            this.mTabIndicator = null;
            this.mTabIndicatorAnimation = null;
        }
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mTabChanged = true;
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mTabIndicatorAnimation != null && this.mTabIndicatorAnimation.isAnimEnd()) {
            LinearLayout tabLayout = getTabLayout();
            if (tabLayout.getChildCount() > 0) {
                if (tabLayout.getChildCount() <= 1) {
                    if (this.mTabIndicator != null) {
                        this.mTabIndicator.setVisibility(8);
                    }
                } else if (this.mTabIndicator != null) {
                    this.mTabIndicator.setVisibility(0);
                }
                if (!(((TabView) tabLayout.getChildAt(getSelectedTabIndex())) == null || (HwWidgetUtils.isActionbarBackgroundThemed(getContext()) ^ 1) == 0 || this.mTabIndicator == null)) {
                    if (this.isWidthPixelsChanged) {
                        setTabScrollingOffsets(this.mLastPos, 0.0f);
                        this.isWidthPixelsChanged = false;
                    }
                    if (this.mTabIndicator.getParent() != this) {
                        addViewInLayout(this.mTabIndicator, 1, this.mTabIndicator.getLayoutParams());
                        updateOrigPos();
                    } else if (this.mTabChanged) {
                        updateTabViewContainerWidth(this.mContext);
                        updateOrigPos();
                        this.mTabChanged = false;
                    }
                    if (this.mTabLayoutWidth != tabLayout.getWidth()) {
                        updateOrigPos();
                        this.mTabLayoutWidth = tabLayout.getWidth();
                    }
                }
                this.mLastPos = getSelectedTabIndex();
            }
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LinearLayout tabLayout = getTabLayout();
        int childCount = tabLayout.getChildCount();
        setAllowCollapse(false);
        tabLayout.setMeasureWithLargestChildEnabled(false);
        if (childCount > 0) {
            int totalW = this.mWidthPixels;
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            if (widthSize < totalW) {
                this.mAvailableSpace = 0;
                totalW = widthSize;
            } else {
                this.mAvailableSpace = widthSize - this.mWidthPixels;
            }
            this.mRealWidthPixels = totalW;
            int w = totalW / childCount;
            if (this.mAvailableSpace > 0) {
                this.mAvailableSpace += totalW % childCount;
            }
            for (int j = 0; j < childCount; j++) {
                TabView child = (TabView) tabLayout.getChildAt(j);
                if (child != null) {
                    measureTabView(child, w, heightMeasureSpec);
                }
            }
            if (!(this.mLastContainerWidth == 0 || this.mTabIndicator == null || this.mLastContainerWidth == totalW || this.mTabIndicator.getParent() != this)) {
                this.isWidthPixelsChanged = true;
            }
            this.mLastContainerWidth = totalW;
            if (this.mTabIndicatorAnimation != null) {
                this.mTabIndicatorAnimation.setViewWidth(w);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void handlePressed(View view, boolean pressed) {
        if (!HwWidgetUtils.isActionbarBackgroundThemed(getContext()) && this.mTabIndicator != null) {
            if (((TabView) getTabLayout().getChildAt(getSelectedTabIndex())) == view || (pressed ^ 1) != 0) {
                this.mTabIndicator.setPressed(pressed);
            }
        }
    }

    private void measureTabView(TabView child, int w, int heightMeasureSpec) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, -1);
        child.setLayoutParams(lp);
        TextView tv = child.getTextView();
        ImageView iv = child.getIconView();
        int tabPadding = child.getPaddingStart() + child.getPaddingEnd();
        if (child.getCustomView() == null && tv != null) {
            tv.setSingleLine(true);
            tv.setMaxLines(1);
            int iconSize = iv == null ? 0 : iv.getMeasuredWidth();
            tv.setTextSize(2, 18.0f);
            tv.setTypeface(this.mRegular);
            tv.measure(0, heightMeasureSpec);
            int requiredWidth = (tv.getMeasuredWidth() + iconSize) + tabPadding;
            if (requiredWidth > w) {
                tv.setTypeface(this.mRegularCondensed);
                tv.measure(0, heightMeasureSpec);
                requiredWidth = (tv.getMeasuredWidth() + iconSize) + tabPadding;
            }
            if (requiredWidth > w) {
                if (this.mAvailableSpace > 0) {
                    int diff = requiredWidth - w;
                    if (diff <= this.mAvailableSpace) {
                        this.mAvailableSpace -= diff;
                        lp.width = w + diff;
                        child.setLayoutParams(lp);
                        return;
                    }
                    tv.setTextSize(2, 15.0f);
                    tv.measure(0, heightMeasureSpec);
                    int requiredWidthSmallFont = (tv.getMeasuredWidth() + iconSize) + tabPadding;
                    if (requiredWidthSmallFont > w) {
                        int diffSmallFont = requiredWidthSmallFont - w;
                        if (diffSmallFont <= this.mAvailableSpace) {
                            this.mAvailableSpace -= diffSmallFont;
                            lp.width = w + diffSmallFont;
                            child.setLayoutParams(lp);
                            return;
                        }
                        lp.width = this.mAvailableSpace + w;
                        this.mAvailableSpace = 0;
                        child.setLayoutParams(lp);
                    } else {
                        return;
                    }
                }
                tv.setTextSize(2, 15.0f);
                tv.measure(0, heightMeasureSpec);
                if ((tv.getMeasuredWidth() + iconSize) + tabPadding > lp.width) {
                    tv.setSingleLine(false);
                    if (((double) getContext().getResources().getConfiguration().fontScale) > 1.15d) {
                        tv.setTextSize(2, 12.0f);
                    }
                    tv.setMaxLines(2);
                }
            }
        }
    }

    public void setTabScrollingOffsets(int position, float x) {
        if (this.mTabIndicatorAnimation != null && this.mTabIndicatorAnimation.isAnimEnd()) {
            if (!HwWidgetUtils.isActionbarBackgroundThemed(getContext()) && this.mTabIndicator != null) {
                LinearLayout mTabLayout = getTabLayout();
                int count = mTabLayout.getChildCount();
                if (count <= 1 || ((position == count - 1 && x > 0.0f) || this.mTabClicked)) {
                    Log.w(this.TAG, "Do not scroll tab point");
                    return;
                }
                if (this.mScrollerLastPos > position) {
                    this.mScrollDirection = 1;
                }
                if (this.mScrollerLastPos < position) {
                    this.mScrollDirection = 0;
                }
                int tabWidth = mTabLayout.getChildAt(0).getWidth();
                float tabOffset = (float) mTabLayout.getChildAt(position).getLeft();
                if (this.mRTLFlag) {
                    tabOffset = (float) (this.mRealWidthPixels - mTabLayout.getChildAt(position).getRight());
                }
                float scrollerOffset = Math.abs(((float) (mTabLayout.getChildAt(1).getLeft() - mTabLayout.getChildAt(0).getLeft())) * x);
                LayoutParams lp = (LayoutParams) this.mTabIndicator.getLayoutParams();
                float scale;
                if (this.mScrollDirection == 0) {
                    if (x < 0.20000005f) {
                        if (this.mRTLFlag) {
                            lp.rightMargin = (int) tabOffset;
                        } else {
                            lp.leftMargin = (int) tabOffset;
                        }
                        this.mTabIndicator.setMinimumWidth((int) (((float) tabWidth) + scrollerOffset));
                    } else {
                        scale = (x - 0.20000005f) / 0.79999995f;
                        if (this.mRTLFlag) {
                            lp.rightMargin = (int) ((((float) tabWidth) * scale) + tabOffset);
                        } else {
                            lp.leftMargin = (int) ((((float) tabWidth) * scale) + tabOffset);
                        }
                        this.mTabIndicator.setMinimumWidth((int) ((((float) tabWidth) * SCALESIZE) - ((((float) tabWidth) * 0.20000005f) * scale)));
                    }
                } else if (x < 0.79999995f) {
                    scale = (0.79999995f - x) / 0.79999995f;
                    if (this.mRTLFlag) {
                        lp.rightMargin = (int) (tabOffset + scrollerOffset);
                    } else {
                        lp.leftMargin = (int) (tabOffset + scrollerOffset);
                    }
                    this.mTabIndicator.setMinimumWidth((int) ((((float) tabWidth) * SCALESIZE) - ((((float) tabWidth) * 0.20000005f) * scale)));
                    if (x == 0.0f) {
                        this.mScrollDirection = 0;
                    }
                } else {
                    if (this.mRTLFlag) {
                        lp.rightMargin = (int) (tabOffset + scrollerOffset);
                    } else {
                        lp.leftMargin = (int) (tabOffset + scrollerOffset);
                    }
                    this.mTabIndicator.setMinimumWidth((int) ((((float) tabWidth) * DOUBLE) - scrollerOffset));
                }
                this.mTabIndicator.setLayoutParams(lp);
            } else {
                return;
            }
        }
        this.mScrollerLastPos = position;
    }

    public void removeTabAt(int position) {
        super.removeTabAt(position);
        this.mTabChanged = true;
    }

    public void removeAllTabs() {
        super.removeAllTabs();
        this.mTabChanged = true;
    }

    public void addTab(Tab tab, boolean setSelected) {
        super.addTab(tab, setSelected);
        this.mTabChanged = true;
    }

    public void addTab(Tab tab, int position, boolean setSelected) {
        super.addTab(tab, position, setSelected);
        this.mTabChanged = true;
    }

    protected boolean disableMaxTabWidth() {
        return true;
    }

    protected void handleTabViewCreated(TabView tabView) {
        if (tabView != null) {
            Tab tab = tabView.getTab();
            if (tab instanceof HwTabImpl) {
                tabView.setId(((HwTabImpl) tab).getTabViewId());
            }
            if (HwWidgetUtils.isActionbarBackgroundThemed(getContext())) {
                tabView.setBackgroundResource(33751382);
                return;
            }
            int resBackground = HwWidgetFactory.getImmersionResource(this.mContext, 33751384, 0, 33751385, false);
            if (this.mIsHwEmphasizeTheme) {
                resBackground = 33751386;
            }
            tabView.setBackgroundResource(resBackground);
        }
    }

    protected void handleTabClicked(int position) {
        if (this.mTabIndicatorAnimation != null) {
            int to = position;
            this.mTabIndicatorAnimation.startAnim(this.mLastPos, position);
        }
        this.mLastPos = position;
        this.mScrollerLastPos = position;
    }

    protected void initTitleAppearance(TextView textView) {
        int colorRes = 33882440;
        if (this.mIsHwDarkTheme) {
            colorRes = 33882441;
        } else if (this.mIsHwEmphasizeTheme) {
            colorRes = 33882442;
        }
        textView.setTextColor(this.mContext.getResources().getColorStateList(colorRes));
    }

    protected int adjustPadding(int availableWidth, int itemPaddingSize) {
        if (availableWidth == this.mWidthPixels) {
            return 0;
        }
        return itemPaddingSize * 2;
    }

    public void setTabSelected(int position) {
        super.setTabSelected(position);
        requestLayout();
    }
}
