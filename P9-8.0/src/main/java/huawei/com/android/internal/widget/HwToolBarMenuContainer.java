package huawei.com.android.internal.widget;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.PathInterpolator;

public class HwToolBarMenuContainer extends HwActionBarContainer {
    private static final int ANIMEXTDURATION = 350;
    private static final int ANIMUPDURATION = 150;
    private static final float MIN_RESET_FRACTION = 0.99f;
    private ValueAnimator mAnimExtend;
    private int mAnimExtendFrom;
    private int mAnimExtendTo;
    private ObjectAnimator mAnimUp;
    private int mEndLocation;
    private LayoutParams mLayoutParams;
    private View mMenu;
    private int mOldOrientation;
    private int mOrigWidth;
    private int mStartLocation;
    private int mWidthPixels;

    public HwToolBarMenuContainer(Context context) {
        this(context, null);
    }

    public HwToolBarMenuContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOrigWidth = 0;
        DisplayMetrics dp = context.getResources().getDisplayMetrics();
        this.mWidthPixels = dp.widthPixels < dp.heightPixels ? dp.widthPixels : dp.heightPixels;
        setHeight(-1);
        this.mOldOrientation = getResources().getConfiguration().orientation;
        createExtendAnimation();
        createUpAnimation();
    }

    private void initBackgroundResource() {
        if (this.mForcedSplitBackground) {
            if (getSplitBackground() == null) {
                setPadding(0, 0, 0, 0);
            }
            setBackground(getSplitBackground());
        } else if (HwWidgetFactory.isHwDarkTheme(getContext()) || HwWidgetFactory.isHwEmphasizeTheme(getContext())) {
            setBackgroundResource(33751610);
        } else {
            setBackgroundResource(33751609);
        }
        Drawable d = getBackground();
        if (d != null) {
            Rect padding = new Rect();
            d.getPadding(padding);
            setPadding(padding.left, padding.top, padding.right, padding.bottom);
        }
    }

    private View getVisibleMenuView() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).getVisibility() == 0) {
                return getChildAt(i);
            }
        }
        return null;
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View menuView = getVisibleMenuView();
        if (menuView == null || menuView.getMeasuredHeight() <= 0) {
            setMeasuredDimension(0, 0);
            setPadding(0, 0, 0, 0);
            setBackgroundResource(0);
        } else {
            Drawable old = getBackground();
            initBackgroundResource();
            if (old != getBackground()) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
        int dst_width = getMeasuredWidth();
        if (isLandscape()) {
            this.mOrigWidth = 0;
        }
        if (isAnimatable()) {
            if (!(dst_width == this.mOrigWidth || this.mOrigWidth == 0 || dst_width == 0)) {
                this.mLayoutParams = getLayoutParams();
                setMeasuredDimension(this.mOrigWidth, getMeasuredHeight());
                this.mAnimExtendFrom = this.mOrigWidth;
                this.mAnimExtendTo = dst_width;
                this.mOrigWidth = dst_width;
                this.mAnimExtend.start();
            }
            if (this.mOrigWidth == 0 && dst_width != 0) {
                this.mOrigWidth = dst_width;
                this.mAnimUp.start();
            }
            if (this.mOrigWidth != 0 && dst_width == 0) {
                disappearNoAnimation();
            }
        } else if (!isLandscape()) {
            this.mOrigWidth = dst_width;
        }
    }

    private void createExtendAnimation() {
        this.mAnimExtend = ValueAnimator.ofFloat(new float[]{1.0f});
        this.mAnimExtend.setDuration(350);
        this.mAnimExtend.setInterpolator(new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
        this.mAnimExtend.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                if (Float.compare(fraction, HwToolBarMenuContainer.MIN_RESET_FRACTION) < 0) {
                    int width = (int) (((float) HwToolBarMenuContainer.this.mAnimExtendFrom) + (((float) (HwToolBarMenuContainer.this.mAnimExtendTo - HwToolBarMenuContainer.this.mAnimExtendFrom)) * fraction));
                    HwToolBarMenuContainer.this.mMenu = HwToolBarMenuContainer.this.getChildAt(0);
                    if (HwToolBarMenuContainer.this.mMenu != null) {
                        HwToolBarMenuContainer.this.mMenu.setTranslationX((float) ((width / 2) - (HwToolBarMenuContainer.this.mAnimExtendTo / 2)));
                    }
                    HwToolBarMenuContainer.this.mLayoutParams.width = width;
                    HwToolBarMenuContainer.this.setLayoutParams(HwToolBarMenuContainer.this.mLayoutParams);
                    return;
                }
                HwToolBarMenuContainer.this.mMenu = HwToolBarMenuContainer.this.getChildAt(0);
                if (HwToolBarMenuContainer.this.mMenu != null) {
                    HwToolBarMenuContainer.this.mMenu.setTranslationX(0.0f);
                }
                HwToolBarMenuContainer.this.mLayoutParams.width = -2;
                HwToolBarMenuContainer.this.setLayoutParams(HwToolBarMenuContainer.this.mLayoutParams);
            }
        });
    }

    private void createUpAnimation() {
        this.mAnimUp = ObjectAnimator.ofFloat(this, "alpha", new float[]{0.0f, 1.0f});
        this.mAnimUp.setInterpolator(new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
        this.mAnimUp.setDuration(150);
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w = getMeasuredWidth();
        int containerW = this.mWidthPixels;
        if (getParent() != null) {
            int pw = ((View) getParent()).getWidth();
            if (pw > 0) {
                containerW = pw;
            }
        }
        if (this.mEndLocation > 0) {
            containerW = this.mEndLocation - this.mStartLocation;
        }
        int realLeft = (left + ((containerW - w) / 2)) + this.mStartLocation;
        setLeft(realLeft);
        setRight(realLeft + w);
        resetLocationX();
    }

    public void setSplitViewLocation(int start, int end) {
        this.mStartLocation = start;
        this.mEndLocation = end;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != this.mOldOrientation) {
            this.mOldOrientation = newConfig.orientation;
            this.mOrigWidth = 0;
            if (this.mAnimExtend.isRunning()) {
                this.mAnimExtend.end();
            }
            if (this.mAnimUp.isRunning()) {
                this.mAnimUp.end();
            }
        }
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == 2;
    }

    private boolean isAnimatable() {
        if (!isAnimationEnabled()) {
            return false;
        }
        return (!this.mAnimUp.isRunning() ? this.mAnimExtend.isRunning() : 1) ^ 1;
    }

    private void disappearNoAnimation() {
        this.mOrigWidth = 0;
        setMeasuredDimension(0, 0);
        this.mLayoutParams = getLayoutParams();
        this.mLayoutParams.width = -2;
        setLayoutParams(this.mLayoutParams);
    }

    private void resetLocationX() {
        this.mMenu = getChildAt(0);
        if (this.mMenu != null) {
            this.mMenu.setTranslationX(0.0f);
        }
    }
}
