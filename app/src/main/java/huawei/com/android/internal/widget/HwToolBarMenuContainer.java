package huawei.com.android.internal.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.animation.PathInterpolator;
import com.android.internal.widget.ActionBarOverlayLayout.LayoutParams;

public class HwToolBarMenuContainer extends HwActionBarContainer {
    private static final int ANIMEXTDURATION = 350;
    private static final int ANIMUPDURATION = 150;
    private ValueAnimator mAnimExtend;
    private ObjectAnimator mAnimUp;
    private int mEndLocation;
    private LayoutParams mLayoutParams;
    private View mMenu;
    private int mOldOrientation;
    private int mOrigWidth;
    private int mStartLocation;
    private int mWidthPixels;

    /* renamed from: huawei.com.android.internal.widget.HwToolBarMenuContainer.1 */
    class AnonymousClass1 implements AnimatorUpdateListener {
        final /* synthetic */ int val$end;
        final /* synthetic */ int val$start;

        AnonymousClass1(int val$start, int val$end) {
            this.val$start = val$start;
            this.val$end = val$end;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            int width = (int) (((float) this.val$start) + (((float) (this.val$end - this.val$start)) * animation.getAnimatedFraction()));
            if (width % 2 != 0) {
                width++;
            }
            HwToolBarMenuContainer.this.mLayoutParams.width = width;
            HwToolBarMenuContainer.this.mMenu = HwToolBarMenuContainer.this.getChildAt(0);
            if (HwToolBarMenuContainer.this.mMenu != null) {
                HwToolBarMenuContainer.this.mMenu.setTranslationX((float) ((width / 2) - (this.val$end / 2)));
            }
            HwToolBarMenuContainer.this.setLayoutParams(HwToolBarMenuContainer.this.mLayoutParams);
        }
    }

    public HwToolBarMenuContainer(Context context) {
        this(context, null);
    }

    public HwToolBarMenuContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOrigWidth = 0;
        DisplayMetrics dp = context.getResources().getDisplayMetrics();
        this.mWidthPixels = Math.min(dp.widthPixels, dp.heightPixels);
        setHeight(-1);
        this.mOldOrientation = getResources().getConfiguration().orientation;
    }

    private void initBackgroundResource() {
        if (this.mForcedSplitBackground) {
            if (getSplitBackground() == null) {
                setPadding(0, 0, 0, 0);
            }
            setBackground(getSplitBackground());
        } else if (HwWidgetFactory.isHwDarkTheme(getContext()) || HwWidgetFactory.isHwEmphasizeTheme(getContext())) {
            setBackgroundResource(33751689);
        } else {
            setBackgroundResource(33751688);
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
        if (isAnimationEnabled() && ((this.mAnimUp == null || !this.mAnimUp.isRunning()) && (this.mAnimExtend == null || !this.mAnimExtend.isRunning()))) {
            if (!(dst_width == this.mOrigWidth || this.mOrigWidth == 0)) {
                this.mLayoutParams = (LayoutParams) getLayoutParams();
                setMeasuredDimension(this.mOrigWidth, getMeasuredHeight());
                int start = this.mOrigWidth;
                int end = dst_width;
                this.mOrigWidth = dst_width;
                this.mAnimExtend = ValueAnimator.ofFloat(new float[]{HwFragmentMenuItemView.ALPHA_NORMAL});
                this.mAnimExtend.setDuration(350);
                this.mAnimExtend.setInterpolator(new PathInterpolator(HwFragmentMenuItemView.ALPHA_DISABLE, 0.15f, 0.1f, 0.85f));
                this.mAnimExtend.addUpdateListener(new AnonymousClass1(start, dst_width));
                this.mAnimExtend.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        HwToolBarMenuContainer.this.mAnimExtend = null;
                        HwToolBarMenuContainer.this.mMenu = HwToolBarMenuContainer.this.getChildAt(0);
                        if (HwToolBarMenuContainer.this.mMenu != null) {
                            HwToolBarMenuContainer.this.mMenu.setTranslationX(0.0f);
                        }
                        HwToolBarMenuContainer.this.mLayoutParams.width = -2;
                        HwToolBarMenuContainer.this.setLayoutParams(HwToolBarMenuContainer.this.mLayoutParams);
                    }
                });
                this.mAnimExtend.start();
            }
            if (this.mOrigWidth == 0 && dst_width != 0) {
                this.mOrigWidth = dst_width;
                this.mAnimUp = ObjectAnimator.ofFloat(this, "alpha", new float[]{0.0f, HwFragmentMenuItemView.ALPHA_NORMAL});
                this.mAnimUp.setInterpolator(new PathInterpolator(HwFragmentMenuItemView.ALPHA_DISABLE, 0.15f, 0.1f, 0.85f));
                this.mAnimUp.setDuration(150);
                this.mAnimUp.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        HwToolBarMenuContainer.this.mAnimUp = null;
                    }
                });
                this.mAnimUp.start();
            }
        } else if (!isAnimationEnabled() && this.mOrigWidth == 0 && dst_width != 0) {
            this.mOrigWidth = dst_width;
        }
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
            if (this.mAnimExtend != null && this.mAnimExtend.isRunning()) {
                this.mAnimExtend.end();
            }
            if (this.mAnimUp != null && this.mAnimUp.isRunning()) {
                this.mAnimUp.end();
            }
        }
    }

    private boolean isLandscape() {
        return getResources().getConfiguration().orientation == 2;
    }
}
