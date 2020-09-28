package huawei.android.widget.appbar;

import android.animation.ValueAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.ActionMenuView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.hwext.internal.R;
import huawei.android.widget.AnimationUtils;
import huawei.android.widget.HwToolbar;
import huawei.android.widget.appbar.HwAppBarLayout;
import huawei.android.widget.appbar.HwExpandedAppbarController;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class HwCollapsingToolbarLayout extends FrameLayout {
    private static final int ALPHA = 255;
    private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;
    private static final Interpolator TEXT_SIZE_INTERPOLATOR = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);
    private static final int VALUE_THREE = 3;
    private static final int VALUE_TWO = 2;
    private HwAppBarLayout.AppBarMoveStateListener mAppBarMoveStateListener;
    final CollapsingTextHelper mCollapsingTextHelper;
    private boolean mCollapsingTitleEnabled;
    private Drawable mContentScrim;
    int mCurrentOffset;
    private SmoothScaleLinearLayout mCustomLinearLayout;
    private HwExpandedAppbarController.OnDragListener mDragListener;
    private boolean mDrawCollapsingTitle;
    private View mDummyView;
    private int mExpandedMarginBottom;
    private int mExpandedMarginEnd;
    private int mExpandedMarginStart;
    private int mExpandedMarginTop;
    WindowInsets mLastInsets;
    private HwAppBarLayout.AppBarOverScrollListener mOnAppBarOverScrollListener;
    private HwAppBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;
    private boolean mRefreshToolbar;
    private int mScrimAlpha;
    private long mScrimAnimationDuration;
    private ValueAnimator mScrimAnimator;
    private int mScrimVisibleHeightTrigger;
    private boolean mScrimsAreShown;
    Drawable mStatusBarScrim;
    private TextView mSubTitleView;
    private TextView mTitleView;
    private final Rect mTmpRect;
    private HwToolbar mToolbar;
    private View mToolbarDirectChild;
    private int mToolbarId;

    public HwCollapsingToolbarLayout(Context context) {
        this(context, null);
    }

    public HwCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mRefreshToolbar = true;
        this.mTmpRect = new Rect();
        this.mScrimVisibleHeightTrigger = -1;
        this.mCollapsingTextHelper = new CollapsingTextHelper(this);
        this.mCollapsingTextHelper.setTextSizeInterpolator(TEXT_SIZE_INTERPOLATOR);
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.HwCollapsingToolbarLayout, 34668591, 33947763);
            this.mCollapsingTextHelper.setExpandedTextGravity(a.getInt(10, 8388691));
            this.mCollapsingTextHelper.setCollapsedTextGravity(a.getInt(9, 8388627));
            int dimensionPixelSize = a.getDimensionPixelSize(2, 0);
            this.mExpandedMarginBottom = dimensionPixelSize;
            this.mExpandedMarginEnd = dimensionPixelSize;
            this.mExpandedMarginTop = dimensionPixelSize;
            this.mExpandedMarginStart = dimensionPixelSize;
            this.mExpandedMarginStart = a.getDimensionPixelSize(5, 0);
            this.mExpandedMarginEnd = a.getDimensionPixelSize(4, 0);
            this.mExpandedMarginTop = a.getDimensionPixelSize(6, 0);
            this.mExpandedMarginBottom = a.getDimensionPixelSize(3, 0);
            this.mCollapsingTitleEnabled = a.getBoolean(16, true);
            setTitle(a.getText(15));
            this.mCollapsingTextHelper.setSubTextAppearance(a.getResourceId(14, 0));
            this.mCollapsingTextHelper.setEyebrowTextAppearance(a.getResourceId(8, 0));
            this.mCollapsingTextHelper.setExpandedTextAppearance(a.getResourceId(7, 0));
            this.mCollapsingTextHelper.setCollapsedTextAppearance(a.getResourceId(0, 0));
            this.mScrimVisibleHeightTrigger = a.getDimensionPixelSize(12, -1);
            this.mScrimAnimationDuration = (long) a.getInt(11, DEFAULT_SCRIM_ANIMATION_DURATION);
            setContentScrim(a.getDrawable(1));
            setStatusBarScrim(a.getDrawable(13));
            this.mToolbarId = a.getResourceId(17, -1);
            a.recycle();
        }
        setWillNotDraw(false);
        setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            /* class huawei.android.widget.appbar.HwCollapsingToolbarLayout.AnonymousClass1 */

            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                return HwCollapsingToolbarLayout.this.onWindowInsetChanged(insets);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        if (parent instanceof HwAppBarLayout) {
            setFitsSystemWindows(((View) parent).getFitsSystemWindows());
            if (this.mOnOffsetChangedListener == null) {
                this.mOnOffsetChangedListener = new OffsetUpdateListener();
            }
            ((HwAppBarLayout) parent).addOnOffsetChangedListener(this.mOnOffsetChangedListener);
            if (this.mOnAppBarOverScrollListener == null) {
                this.mOnAppBarOverScrollListener = new AppBarOverScrollListener();
            }
            ((HwAppBarLayout) parent).setOnAppBarOverScrollListener(this.mOnAppBarOverScrollListener);
            if (this.mAppBarMoveStateListener == null) {
                this.mAppBarMoveStateListener = new AppBarMoveStateListener();
            }
            ((HwAppBarLayout) parent).setAppBarMoveStateListener(this.mAppBarMoveStateListener);
            requestApplyInsets();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        ViewParent parent = getParent();
        HwAppBarLayout.OnOffsetChangedListener onOffsetChangedListener = this.mOnOffsetChangedListener;
        if (onOffsetChangedListener != null && (parent instanceof HwAppBarLayout)) {
            ((HwAppBarLayout) parent).removeOnOffsetChangedListener(onOffsetChangedListener);
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: package-private */
    public WindowInsets onWindowInsetChanged(WindowInsets insets) {
        WindowInsets newInsets = null;
        if (getFitsSystemWindows()) {
            newInsets = insets;
        }
        if (!Objects.equals(this.mLastInsets, newInsets)) {
            this.mLastInsets = newInsets;
            requestLayout();
        }
        return insets.consumeSystemWindowInsets();
    }

    public void draw(Canvas canvas) {
        Drawable drawable;
        super.draw(canvas);
        ensureToolbar();
        if (this.mToolbar == null && (drawable = this.mContentScrim) != null && this.mScrimAlpha > 0) {
            drawable.mutate().setAlpha(this.mScrimAlpha);
            this.mContentScrim.draw(canvas);
        }
        if (this.mCollapsingTitleEnabled && this.mDrawCollapsingTitle) {
            this.mCollapsingTextHelper.draw(canvas);
        }
        if (this.mStatusBarScrim != null && this.mScrimAlpha > 0) {
            WindowInsets windowInsets = this.mLastInsets;
            int topInset = windowInsets != null ? windowInsets.getSystemWindowInsetTop() : 0;
            if (topInset > 0) {
                this.mStatusBarScrim.setBounds(0, -this.mCurrentOffset, getWidth(), topInset - this.mCurrentOffset);
                this.mStatusBarScrim.mutate().setAlpha(this.mScrimAlpha);
                this.mStatusBarScrim.draw(canvas);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean invalidated = false;
        if (this.mContentScrim != null && this.mScrimAlpha > 0 && isToolbarChild(child)) {
            this.mContentScrim.mutate().setAlpha(this.mScrimAlpha);
            this.mContentScrim.draw(canvas);
            invalidated = true;
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Drawable drawable = this.mContentScrim;
        if (drawable != null) {
            drawable.setBounds(0, 0, w, h);
        }
    }

    private void ensureToolbar() {
        if (this.mRefreshToolbar) {
            this.mToolbar = null;
            this.mToolbarDirectChild = null;
            int i = this.mToolbarId;
            if (i != -1) {
                this.mToolbar = (HwToolbar) findViewById(i);
                HwToolbar hwToolbar = this.mToolbar;
                if (hwToolbar != null) {
                    this.mToolbarDirectChild = findDirectChild(hwToolbar);
                }
            }
            if (this.mToolbar == null) {
                HwToolbar toolbar = null;
                int i2 = 0;
                int count = getChildCount();
                while (true) {
                    if (i2 >= count) {
                        break;
                    }
                    View child = getChildAt(i2);
                    if (child instanceof HwToolbar) {
                        toolbar = (HwToolbar) child;
                        toolbar.setBackground(null);
                        break;
                    }
                    i2++;
                }
                this.mToolbar = toolbar;
            }
            Log.w("HwCollapsingToolbarlayout", "ensureToolbar.updateDummyView");
            updateDummyView();
            this.mRefreshToolbar = false;
        }
    }

    private boolean isToolbarChild(View child) {
        View view = this.mToolbarDirectChild;
        return (view == null || view == this) ? child == this.mToolbar : child == view;
    }

    private View findDirectChild(View descendant) {
        View directChild = descendant;
        ViewParent parent = descendant.getParent();
        while (parent != this && parent != null) {
            if (parent instanceof View) {
                directChild = (View) parent;
            }
            parent = parent.getParent();
        }
        return directChild;
    }

    private void updateDummyView() {
        View view;
        if (!this.mCollapsingTitleEnabled && (view = this.mDummyView) != null) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.mDummyView);
            }
        }
        if (this.mCollapsingTitleEnabled && this.mToolbar != null) {
            if (TextUtils.isEmpty(this.mCollapsingTextHelper.getText())) {
                this.mCollapsingTextHelper.setText(this.mToolbar.getTitle());
            }
            this.mToolbar.setTitle((CharSequence) null);
            this.mDummyView = this.mToolbar.getIconLayout();
            if (this.mDummyView == null) {
                this.mToolbar.initIconLayout();
                this.mDummyView = this.mToolbar.getIconLayout();
            }
            if (this.mDummyView != null) {
                Activity activity = null;
                if (this.mContext instanceof Activity) {
                    activity = (Activity) this.mContext;
                }
                if (this.mDummyView.getParent() == null) {
                    boolean isActionBar = (activity == null || activity.getActionBar() == null) ? false : true;
                    if (isActionBar && activity.getActionBar().getCustomView() == null) {
                        ActionBar actionBar = activity.getActionBar();
                        actionBar.setDisplayShowCustomEnabled(true);
                        actionBar.setCustomView(this.mDummyView, new ActionBar.LayoutParams(-1, -2));
                    }
                    if (!isActionBar) {
                        this.mToolbar.addView(this.mDummyView, -1, -2);
                    }
                }
                this.mTitleView = (TextView) this.mDummyView.findViewById(16908715);
                this.mTitleView.setVisibility(4);
                LinearLayout.LayoutParams lpTitle = (LinearLayout.LayoutParams) this.mTitleView.getLayoutParams();
                lpTitle.width = -1;
                this.mTitleView.setLayoutParams(lpTitle);
                this.mSubTitleView = (TextView) this.mDummyView.findViewById(16908714);
                setSubTitleVisible();
            }
        }
    }

    private void setTitleTextSize() {
        int titleTextSize = getContext().getResources().getInteger(ResLoader.getInstance().getIdentifier(this.mContext, "integer", "hwtoolbar_title_normal_textsize"));
        TextView textView = this.mTitleView;
        if (textView != null) {
            textView.setTextSize(1, (float) titleTextSize);
            this.mCollapsingTextHelper.setCollapsedTextSize(this.mTitleView.getTextSize());
            TextView textView2 = this.mSubTitleView;
            if (textView2 != null && textView2.getVisibility() != 8) {
                this.mSubTitleView.setTextSize(1, (float) getContext().getResources().getInteger(ResLoader.getInstance().getIdentifier(this.mContext, "integer", "hwtoolbar_subtitle_normal_textsize")));
                this.mCollapsingTextHelper.setCollapsedSubTextSize(this.mSubTitleView.getTextSize());
            }
        }
    }

    private boolean hasMenuItem() {
        int count;
        HwToolbar hwToolbar = this.mToolbar;
        if (hwToolbar != null && (count = hwToolbar.getChildCount()) > 0) {
            for (int i = 0; i < count; i++) {
                View view = this.mToolbar.getChildAt(i);
                if (view instanceof ActionMenuView) {
                    if (((ActionMenuView) view).getChildCount() == 0) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureToolbar();
        if (this.mToolbar != null) {
            setTitleTextSize();
            setSubTitleVisible();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mode = View.MeasureSpec.getMode(heightMeasureSpec);
        WindowInsets windowInsets = this.mLastInsets;
        int topInset = windowInsets != null ? windowInsets.getSystemWindowInsetTop() : 0;
        if (mode == 0 && topInset > 0) {
            super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(getMeasuredHeight() + topInset, 1073741824));
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View view;
        int i;
        int i2;
        super.onLayout(changed, left, top, right, bottom);
        WindowInsets windowInsets = this.mLastInsets;
        if (windowInsets != null) {
            int insetTop = windowInsets.getSystemWindowInsetTop();
            int childCount = getChildCount();
            for (int i3 = 0; i3 < childCount; i3++) {
                View child = getChildAt(i3);
                if (!child.getFitsSystemWindows() && child.getTop() < insetTop) {
                    ViewOffsetHelper.offsetTopAndBottom(child, insetTop);
                }
            }
        }
        if (!(!this.mCollapsingTitleEnabled || (view = this.mDummyView) == null || this.mTitleView == null)) {
            boolean isRtl = false;
            this.mDrawCollapsingTitle = view.isAttachedToWindow() && this.mDummyView.getVisibility() == 0;
            if (this.mDrawCollapsingTitle) {
                if (getLayoutDirection() == 1) {
                    isRtl = true;
                }
                View view2 = this.mToolbarDirectChild;
                if (view2 == null) {
                    view2 = this.mToolbar;
                }
                int maxOffset = getMaxOffsetForPinChild(view2);
                ViewGroupUtils.getDescendantRect(this, this.mTitleView, this.mTmpRect);
                this.mCollapsingTextHelper.setCollapsedBounds(this.mTmpRect.left, this.mTmpRect.top + maxOffset + this.mToolbar.getTitleMarginTop(), this.mTmpRect.right, (this.mTmpRect.bottom + maxOffset) - this.mToolbar.getTitleMarginBottom());
                initColumnSize();
                CollapsingTextHelper collapsingTextHelper = this.mCollapsingTextHelper;
                if (isRtl) {
                    i = this.mExpandedMarginEnd + this.mToolbar.getPaddingRight();
                } else {
                    i = this.mExpandedMarginStart + this.mToolbar.getPaddingLeft();
                }
                int i4 = this.mTmpRect.top + this.mExpandedMarginTop;
                int i5 = right - left;
                if (isRtl) {
                    i2 = this.mExpandedMarginStart + this.mToolbar.getPaddingRight();
                } else {
                    i2 = this.mExpandedMarginEnd + this.mToolbar.getPaddingLeft();
                }
                collapsingTextHelper.setExpandedBounds(i, i4, i5 - i2, (bottom - top) - this.mExpandedMarginBottom);
                this.mCollapsingTextHelper.recalculate();
            }
        }
        int childCount2 = getChildCount();
        for (int i6 = 0; i6 < childCount2; i6++) {
            getViewOffsetHelper(getChildAt(i6)).onViewLayout();
        }
        if (this.mToolbar != null) {
            View view3 = this.mToolbarDirectChild;
            if (view3 == null || view3 == this) {
                setMinimumHeight(getHeightWithMargins(this.mToolbar));
            } else {
                setMinimumHeight(getHeightWithMargins(view3));
            }
        }
        updateScrimVisibility();
    }

    private static int getHeightWithMargins(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (!(lp instanceof ViewGroup.MarginLayoutParams)) {
            return view.getHeight();
        }
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
        return view.getHeight() + mlp.topMargin + mlp.bottomMargin;
    }

    static ViewOffsetHelper getViewOffsetHelper(View view) {
        int offsetHelperId = ResLoader.getInstance().getIdentifier(view.getContext(), ResLoaderUtil.ID, "view_offset_helper");
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(offsetHelperId);
        if (offsetHelper != null) {
            return offsetHelper;
        }
        ViewOffsetHelper offsetHelper2 = new ViewOffsetHelper(view);
        view.setTag(offsetHelperId, offsetHelper2);
        return offsetHelper2;
    }

    public void setTitle(CharSequence title) {
        this.mCollapsingTextHelper.setText(title);
    }

    public void setSubTitle(CharSequence subTitle) {
        this.mCollapsingTextHelper.setSubText(subTitle);
        setSubTitleVisible();
    }

    private void initColumnSize() {
        int expandedMarginStartId = ResLoader.getInstance().getIdentifier(getContext(), ResLoaderUtil.DIMEN, "emui_dimens_max_start");
        int expandedMarginEndId = ResLoader.getInstance().getIdentifier(getContext(), ResLoaderUtil.DIMEN, "emui_dimens_max_end");
        this.mExpandedMarginStart = getContext().getResources().getDimensionPixelSize(expandedMarginStartId);
        this.mExpandedMarginEnd = getContext().getResources().getDimensionPixelSize(expandedMarginEndId);
    }

    private void setSubTitleVisible() {
        if (this.mSubTitleView == null) {
            return;
        }
        if (getSubTitle() == null || getSubTitleBehavior() != 0) {
            this.mSubTitleView.setVisibility(8);
        } else {
            this.mSubTitleView.setVisibility(4);
        }
    }

    public void setEyebrowTitle(CharSequence eyebrowTitle) {
        this.mCollapsingTextHelper.setEyebrowText(eyebrowTitle);
    }

    public CharSequence getTitle() {
        if (this.mCollapsingTitleEnabled) {
            return this.mCollapsingTextHelper.getText();
        }
        return null;
    }

    public CharSequence getSubTitle() {
        if (this.mCollapsingTitleEnabled) {
            return this.mCollapsingTextHelper.getSubText();
        }
        return null;
    }

    public CharSequence getEyebrowText() {
        if (this.mCollapsingTitleEnabled) {
            return this.mCollapsingTextHelper.getEyebrowText();
        }
        return null;
    }

    public void setTitleEnabled(boolean enabled) {
        if (enabled != this.mCollapsingTitleEnabled) {
            this.mCollapsingTitleEnabled = enabled;
            updateDummyView();
            requestLayout();
        }
    }

    public boolean isTitleEnabled() {
        return this.mCollapsingTitleEnabled;
    }

    public void setScrimsShown(boolean shown) {
        setScrimsShown(shown, isLaidOut() && !isInEditMode());
    }

    public void setScrimsShown(boolean shown, boolean animate) {
        if (this.mScrimsAreShown != shown) {
            int i = ALPHA;
            if (animate) {
                if (!shown) {
                    i = 0;
                }
                animateScrim(i);
            } else {
                if (!shown) {
                    i = 0;
                }
                setScrimAlpha(i);
            }
            this.mScrimsAreShown = shown;
        }
    }

    private void animateScrim(int targetAlpha) {
        Interpolator interpolator;
        ensureToolbar();
        ValueAnimator valueAnimator = this.mScrimAnimator;
        if (valueAnimator == null) {
            this.mScrimAnimator = new ValueAnimator();
            this.mScrimAnimator.setDuration(this.mScrimAnimationDuration);
            ValueAnimator valueAnimator2 = this.mScrimAnimator;
            if (targetAlpha > this.mScrimAlpha) {
                interpolator = AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR;
            } else {
                interpolator = AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR;
            }
            valueAnimator2.setInterpolator(interpolator);
            this.mScrimAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                /* class huawei.android.widget.appbar.HwCollapsingToolbarLayout.AnonymousClass2 */

                public void onAnimationUpdate(ValueAnimator animator) {
                    HwCollapsingToolbarLayout.this.setScrimAlpha(((Integer) animator.getAnimatedValue()).intValue());
                }
            });
        } else if (valueAnimator.isRunning()) {
            this.mScrimAnimator.cancel();
        }
        this.mScrimAnimator.setIntValues(this.mScrimAlpha, targetAlpha);
        this.mScrimAnimator.start();
    }

    /* access modifiers changed from: package-private */
    public void setScrimAlpha(int alpha) {
        HwToolbar hwToolbar;
        if (alpha != this.mScrimAlpha) {
            if (!(this.mContentScrim == null || (hwToolbar = this.mToolbar) == null)) {
                hwToolbar.postInvalidateOnAnimation();
            }
            this.mScrimAlpha = alpha;
            postInvalidateOnAnimation();
        }
    }

    /* access modifiers changed from: package-private */
    public int getScrimAlpha() {
        return this.mScrimAlpha;
    }

    public void setContentScrim(Drawable drawable) {
        Drawable drawable2 = this.mContentScrim;
        if (drawable2 != drawable) {
            Drawable drawable3 = null;
            if (drawable2 != null) {
                drawable2.setCallback(null);
            }
            if (drawable != null) {
                drawable3 = drawable.mutate();
            }
            this.mContentScrim = drawable3;
            Drawable drawable4 = this.mContentScrim;
            if (drawable4 != null) {
                drawable4.setBounds(0, 0, getWidth(), getHeight());
                this.mContentScrim.setCallback(this);
                this.mContentScrim.setAlpha(this.mScrimAlpha);
            }
            postInvalidateOnAnimation();
        }
    }

    public void setContentScrimColor(int color) {
        setContentScrim(new ColorDrawable(color));
    }

    public void setContentScrimResource(int resId) {
        setContentScrim(getContext().getResources().getDrawable(resId));
    }

    public Drawable getContentScrim() {
        return this.mContentScrim;
    }

    public void setStatusBarScrim(Drawable drawable) {
        Drawable drawable2 = this.mStatusBarScrim;
        if (drawable2 != drawable) {
            Drawable drawable3 = null;
            if (drawable2 != null) {
                drawable2.setCallback(null);
            }
            if (drawable != null) {
                drawable3 = drawable.mutate();
            }
            this.mStatusBarScrim = drawable3;
            Drawable drawable4 = this.mStatusBarScrim;
            if (drawable4 != null) {
                if (drawable4.isStateful()) {
                    this.mStatusBarScrim.setState(getDrawableState());
                }
                this.mStatusBarScrim.setLayoutDirection(getLayoutDirection());
                this.mStatusBarScrim.setVisible(getVisibility() == 0, false);
                this.mStatusBarScrim.setCallback(this);
                this.mStatusBarScrim.setAlpha(this.mScrimAlpha);
            }
            postInvalidateOnAnimation();
        }
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        int[] state = getDrawableState();
        boolean changed = false;
        Drawable drawable = this.mStatusBarScrim;
        if (drawable != null && drawable.isStateful()) {
            changed = false | drawable.setState(state);
        }
        Drawable drawable2 = this.mContentScrim;
        if (drawable2 != null && drawable2.isStateful()) {
            changed |= drawable2.setState(state);
        }
        CollapsingTextHelper collapsingTextHelper = this.mCollapsingTextHelper;
        if (collapsingTextHelper != null) {
            changed |= collapsingTextHelper.setState(state);
        }
        if (changed) {
            invalidate();
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == this.mContentScrim || who == this.mStatusBarScrim;
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        boolean visible = visibility == 0;
        Drawable drawable = this.mStatusBarScrim;
        if (!(drawable == null || drawable.isVisible() == visible)) {
            this.mStatusBarScrim.setVisible(visible, false);
        }
        Drawable drawable2 = this.mContentScrim;
        if (drawable2 != null && drawable2.isVisible() != visible) {
            this.mContentScrim.setVisible(visible, false);
        }
    }

    public void setStatusBarScrimColor(int color) {
        setStatusBarScrim(new ColorDrawable(color));
    }

    public void setStatusBarScrimResource(int resId) {
        setStatusBarScrim(getContext().getResources().getDrawable(resId));
    }

    public Drawable getStatusBarScrim() {
        return this.mStatusBarScrim;
    }

    public void setCollapsedTitleTextAppearance(int resId) {
        this.mCollapsingTextHelper.setCollapsedTextAppearance(resId);
    }

    public void setCollapsedTitleTextColor(int color) {
        setCollapsedTitleTextColor(ColorStateList.valueOf(color));
    }

    public void setCollapsedTitleTextColor(ColorStateList colors) {
        this.mCollapsingTextHelper.setCollapsedTextColor(colors);
    }

    public void setCollapsedTitleGravity(int gravity) {
        this.mCollapsingTextHelper.setCollapsedTextGravity(gravity);
    }

    public int getCollapsedTitleGravity() {
        return this.mCollapsingTextHelper.getCollapsedTextGravity();
    }

    public void setExpandedTitleTextAppearance(int resId) {
        this.mCollapsingTextHelper.setExpandedTextAppearance(resId);
    }

    public void setExpandedTitleColor(int color) {
        setExpandedTitleTextColor(ColorStateList.valueOf(color));
    }

    public void setExpandedTitleTextColor(ColorStateList colors) {
        this.mCollapsingTextHelper.setExpandedTextColor(colors);
    }

    public void setExpandedTitleGravity(int gravity) {
        this.mCollapsingTextHelper.setExpandedTextGravity(gravity);
    }

    public int getExpandedTitleGravity() {
        return this.mCollapsingTextHelper.getExpandedTextGravity();
    }

    public void setCollapsedTitleTypeface(Typeface typeface) {
        this.mCollapsingTextHelper.setCollapsedTypeface(typeface);
    }

    public Typeface getCollapsedTitleTypeface() {
        return this.mCollapsingTextHelper.getCollapsedTypeface();
    }

    public void setExpandedTitleTypeface(Typeface typeface) {
        this.mCollapsingTextHelper.setExpandedTypeface(typeface);
    }

    public Typeface getExpandedTitleTypeface() {
        return this.mCollapsingTextHelper.getExpandedTypeface();
    }

    public void setExpandedTitleMargin(int start, int top, int end, int bottom) {
        this.mExpandedMarginStart = start;
        this.mExpandedMarginTop = top;
        this.mExpandedMarginEnd = end;
        this.mExpandedMarginBottom = bottom;
        requestLayout();
    }

    public int getExpandedTitleMarginStart() {
        return this.mExpandedMarginStart;
    }

    public void setExpandedTitleMarginStart(int margin) {
        this.mExpandedMarginStart = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginTop() {
        return this.mExpandedMarginTop;
    }

    public void setExpandedTitleMarginTop(int margin) {
        this.mExpandedMarginTop = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginEnd() {
        return this.mExpandedMarginEnd;
    }

    public void setExpandedTitleMarginEnd(int margin) {
        this.mExpandedMarginEnd = margin;
        requestLayout();
    }

    public int getExpandedTitleMarginBottom() {
        return this.mExpandedMarginBottom;
    }

    public void setExpandedTitleMarginBottom(int margin) {
        this.mExpandedMarginBottom = margin;
        requestLayout();
    }

    public void setScrimVisibleHeightTrigger(int height) {
        if (this.mScrimVisibleHeightTrigger != height) {
            this.mScrimVisibleHeightTrigger = height;
            updateScrimVisibility();
        }
    }

    public int getScrimVisibleHeightTrigger() {
        int i = this.mScrimVisibleHeightTrigger;
        if (i >= 0) {
            return i;
        }
        WindowInsets windowInsets = this.mLastInsets;
        int insetTop = windowInsets != null ? windowInsets.getSystemWindowInsetTop() : 0;
        int minHeight = getMinimumHeight();
        if (minHeight > 0) {
            return MathUtils.min((minHeight * 2) + insetTop, getHeight());
        }
        return getHeight() / 3;
    }

    public void setScrimAnimationDuration(long duration) {
        this.mScrimAnimationDuration = duration;
    }

    public void setBubbleCount(int count) {
        CollapsingTextHelper collapsingTextHelper = this.mCollapsingTextHelper;
        if (collapsingTextHelper != null) {
            collapsingTextHelper.setBubbleCount(count);
        }
    }

    public long getScrimAnimationDuration() {
        return this.mScrimAnimationDuration;
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams params) {
        return params instanceof LayoutParams;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.widget.FrameLayout
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-1, -1);
    }

    @Override // android.widget.FrameLayout, android.widget.FrameLayout, android.view.ViewGroup
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup
    public FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams params) {
        return new LayoutParams(params);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public static final int COLLAPSE_MODE_OFF = 0;
        public static final int COLLAPSE_MODE_PARALLAX = 2;
        public static final int COLLAPSE_MODE_PIN = 1;
        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;
        int mCollapseMode = 0;
        float mParallaxMult = 0.5f;

        @Retention(RetentionPolicy.SOURCE)
        @interface CollapseMode {
        }

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HwCollapsingToolbarLayout_Layout);
            this.mCollapseMode = a.getInt(0, 0);
            setParallaxMultiplier(a.getFloat(1, 0.5f));
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams params) {
            super(params);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }

        public void setCollapseMode(int collapseMode) {
            this.mCollapseMode = collapseMode;
        }

        public int getCollapseMode() {
            return this.mCollapseMode;
        }

        public void setParallaxMultiplier(float multiplier) {
            this.mParallaxMult = multiplier;
        }

        public float getParallaxMultiplier() {
            return this.mParallaxMult;
        }
    }

    /* access modifiers changed from: package-private */
    public final void updateScrimVisibility() {
        if (this.mContentScrim != null || this.mStatusBarScrim != null) {
            setScrimsShown(getHeight() + this.mCurrentOffset < getScrimVisibleHeightTrigger());
        }
    }

    /* access modifiers changed from: package-private */
    public final int getMaxOffsetForPinChild(View child) {
        return ((getHeight() - getViewOffsetHelper(child).getLayoutTop()) - child.getHeight()) - ((LayoutParams) child.getLayoutParams()).bottomMargin;
    }

    private class OffsetUpdateListener implements HwAppBarLayout.OnOffsetChangedListener {
        OffsetUpdateListener() {
        }

        @Override // huawei.android.widget.appbar.HwAppBarLayout.OnOffsetChangedListener
        public void onOffsetChanged(HwAppBarLayout layout, int verticalOffset) {
            if (layout.isOverScrolled() && verticalOffset != 0) {
                layout.setIsOverScrolled(false);
            }
            HwCollapsingToolbarLayout hwCollapsingToolbarLayout = HwCollapsingToolbarLayout.this;
            hwCollapsingToolbarLayout.mCurrentOffset = verticalOffset;
            int insetTop = hwCollapsingToolbarLayout.mLastInsets != null ? HwCollapsingToolbarLayout.this.mLastInsets.getSystemWindowInsetTop() : 0;
            int z = HwCollapsingToolbarLayout.this.getChildCount();
            for (int i = 0; i < z; i++) {
                View child = HwCollapsingToolbarLayout.this.getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                ViewOffsetHelper offsetHelper = HwCollapsingToolbarLayout.getViewOffsetHelper(child);
                int i2 = lp.mCollapseMode;
                if (i2 == 1) {
                    offsetHelper.setTopAndBottomOffset(MathUtils.clamp(-verticalOffset, 0, HwCollapsingToolbarLayout.this.getMaxOffsetForPinChild(child)));
                } else if (i2 == 2) {
                    offsetHelper.setTopAndBottomOffset(Math.round(((float) (-verticalOffset)) * lp.mParallaxMult));
                }
            }
            HwCollapsingToolbarLayout.this.updateScrimVisibility();
            if (HwCollapsingToolbarLayout.this.mStatusBarScrim != null && insetTop > 0) {
                HwCollapsingToolbarLayout.this.postInvalidateOnAnimation();
            }
            HwCollapsingToolbarLayout.this.mCollapsingTextHelper.setExpansionFraction(((float) Math.abs(verticalOffset)) / ((float) ((HwCollapsingToolbarLayout.this.getHeight() - HwCollapsingToolbarLayout.this.getMinimumHeight()) - insetTop)));
            if (HwCollapsingToolbarLayout.this.mDragListener != null) {
                HwCollapsingToolbarLayout.this.mDragListener.onDrag(0, verticalOffset);
            }
        }
    }

    private class AppBarOverScrollListener implements HwAppBarLayout.AppBarOverScrollListener {
        private AppBarOverScrollListener() {
        }

        @Override // huawei.android.widget.appbar.HwAppBarLayout.AppBarOverScrollListener
        public void onAppBarOverScrolled(float overScrollFraction, float appBarOverScrollY, boolean hasAppBarHeightChanged) {
            HwCollapsingToolbarLayout.this.mCollapsingTextHelper.setOverScrollParameters(overScrollFraction, appBarOverScrollY);
            if (!hasAppBarHeightChanged) {
                HwCollapsingToolbarLayout.this.mCollapsingTextHelper.calculateTextSizeOnOverScroll();
                HwCollapsingToolbarLayout.this.invalidate();
                HwCollapsingToolbarLayout.this.mCollapsingTextHelper.resetTargetOverScrollFraction();
            }
        }
    }

    private class AppBarMoveStateListener implements HwAppBarLayout.AppBarMoveStateListener {
        private AppBarMoveStateListener() {
        }

        @Override // huawei.android.widget.appbar.HwAppBarLayout.AppBarMoveStateListener
        public void notifyAppBarIsMoved(boolean isAppBarMoved) {
            HwCollapsingToolbarLayout.this.mCollapsingTextHelper.setIsAppBarMoved(isAppBarMoved);
        }
    }

    public void setCustomView(View customView) {
        if (customView == null) {
            SmoothScaleLinearLayout smoothScaleLinearLayout = this.mCustomLinearLayout;
            if (smoothScaleLinearLayout != null) {
                removeView(smoothScaleLinearLayout);
                this.mCustomLinearLayout = null;
                this.mCollapsingTextHelper.setCustomView(null);
                return;
            }
            return;
        }
        if (this.mCustomLinearLayout == null) {
            this.mCustomLinearLayout = new SmoothScaleLinearLayout(getContext());
            addView(this.mCustomLinearLayout, new ViewGroup.LayoutParams(-1, -2));
        }
        this.mCustomLinearLayout.removeAllViews();
        this.mCustomLinearLayout.addView(customView);
        this.mCollapsingTextHelper.setCustomView(this.mCustomLinearLayout);
    }

    /* access modifiers changed from: package-private */
    public void setOnDragListener(HwExpandedAppbarController.OnDragListener dragListener) {
        this.mDragListener = dragListener;
    }

    /* access modifiers changed from: package-private */
    public void setSubTitleBehavior(int behaviorFlag) {
        this.mCollapsingTextHelper.setSubTextBehavior(behaviorFlag);
    }

    /* access modifiers changed from: package-private */
    public void setCustomViewSmoothScaleEnabled(boolean isSmoothScaleEnabled) {
        this.mCollapsingTextHelper.setCustomViewSmoothScaleEnabled(isSmoothScaleEnabled);
    }

    /* access modifiers changed from: package-private */
    public int getSubTitleBehavior() {
        return this.mCollapsingTextHelper.getSubTextBehavior();
    }
}
