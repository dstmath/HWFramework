package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;
import com.android.internal.widget.ActionBarContainer;
import huawei.android.widget.effect.engine.HwBlurEngine;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwToolBarMenuContainer extends ActionBarContainer implements HwCornerInsetsInterface {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_BLUR_TYPE = HwBlurEngine.BlurType.LightBlurWithGray.getValue();
    private static final int HALF_DIVISOR = 2;
    private static final int INVALID_BLUR_OVERLAY_COLOR = -16777216;
    private static final String TAG = "HwToolBarMenuContainer";
    private AttributeSet mAttrs;
    private HwBlurEngine mBlurEngine;
    private int mBlurOverlayColor;
    private HwBlurEngine.BlurType mBlurType;
    private Rect mCornerInsetsRect;
    private Drawable mDivider;
    private int mEndLocation;
    private HwWidgetSafeInsets mHwWidgetSafeInsets;
    private boolean mIsBlurEnable;
    private boolean mIsForcedSplitBackground;
    private Drawable mSplitBackground;
    private int mStartLocation;
    private final Rect mTempRect;
    private int mWidthPixels;

    public HwToolBarMenuContainer(Context context) {
        this(context, null);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r12v0, resolved type: huawei.android.widget.HwToolBarMenuContainer */
    /* JADX WARN: Multi-variable type inference failed */
    public HwToolBarMenuContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        int min;
        this.mBlurOverlayColor = INVALID_BLUR_OVERLAY_COLOR;
        this.mBlurType = HwBlurEngine.BlurType.LightBlurWithGray;
        this.mBlurEngine = HwBlurEngine.getInstance();
        this.mIsBlurEnable = DEBUG;
        this.mTempRect = new Rect();
        this.mCornerInsetsRect = new Rect();
        this.mHwWidgetSafeInsets = new HwWidgetSafeInsets(this);
        this.mHwWidgetSafeInsets.parseHwDisplayCutout(context, attrs);
        this.mAttrs = attrs;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (displayMetrics.widthPixels <= displayMetrics.heightPixels) {
            min = displayMetrics.widthPixels;
        } else {
            min = displayMetrics.heightPixels;
        }
        this.mWidthPixels = min;
        ResLoader resLoader = ResLoader.getInstance();
        Resources.Theme theme = resLoader.getTheme(this.mContext);
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(this.mAttrs, resLoader.getIdentifierArray(getContext(), ResLoaderUtil.STAYLEABLE, "HwToolbar"), 16843946, 0);
            HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(typedArray.getInteger(resLoader.getIdentifier(this.mContext, ResLoaderUtil.STAYLEABLE, "HwToolbar_hwToolbarSplitBlurType"), DEFAULT_BLUR_TYPE));
            if (blurType != null) {
                this.mBlurType = blurType;
            }
            this.mBlurOverlayColor = typedArray.getColor(resLoader.getIdentifier(this.mContext, ResLoaderUtil.STAYLEABLE, "HwToolbar_hwToolbarSplitBlurOverlayColor"), INVALID_BLUR_OVERLAY_COLOR);
            typedArray.recycle();
        }
    }

    public void setForcedSplitBackground(boolean isForced) {
        this.mIsForcedSplitBackground = isForced;
    }

    public void setSplitBackground(Drawable background) {
        this.mSplitBackground = background;
        this.mIsForcedSplitBackground = true;
    }

    private void initBackgroundResource() {
        if (this.mIsForcedSplitBackground) {
            setBackground(this.mSplitBackground);
            return;
        }
        ResLoader resLoader = ResLoader.getInstance();
        Resources res = resLoader.getResources(this.mContext);
        Resources.Theme theme = resLoader.getTheme(this.mContext);
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(this.mAttrs, resLoader.getIdentifierArray(getContext(), ResLoaderUtil.STAYLEABLE, "HwToolbar"), 16843946, 0);
            int backgroundId = typedArray.getResourceId(resLoader.getIdentifier(getContext(), ResLoaderUtil.STAYLEABLE, "HwToolbar_hwToolbarSplitBarBackground"), -1);
            if (backgroundId != -1) {
                setBackgroundResource(backgroundId);
            }
            int menuDividerId = typedArray.getResourceId(resLoader.getIdentifier(getContext(), ResLoaderUtil.STAYLEABLE, "HwToolbar_hwToolbarMenuDivider"), -1);
            if (menuDividerId != -1) {
                this.mDivider = res.getDrawable(menuDividerId, theme);
            }
            typedArray.recycle();
        }
    }

    private View getVisibleMenuView() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if ((child == null || child.getVisibility() != 0 || child.getMeasuredHeight() <= 0) ? DEBUG : true) {
                return child;
            }
        }
        return null;
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int widthMeasure = widthMeasureSpec;
        int i2 = this.mEndLocation;
        if (i2 > 0 && i2 >= (i = this.mStartLocation)) {
            widthMeasure = View.MeasureSpec.makeMeasureSpec(i2 - i, 1073741824);
        }
        HwToolBarMenuContainer.super.onMeasure(widthMeasure, heightMeasureSpec);
        View menuView = getVisibleMenuView();
        if (menuView == null || menuView.getMeasuredHeight() <= 0) {
            setMeasuredDimension(View.MeasureSpec.getSize(widthMeasure), 0);
            setBackgroundResource(0);
            return;
        }
        Drawable old = getBackground();
        initBackgroundResource();
        if (old != getBackground()) {
            HwToolBarMenuContainer.super.onMeasure(widthMeasure, heightMeasureSpec);
        }
    }

    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        int parentWidth;
        HwToolBarMenuContainer.super.onLayout(isChanged, left, top, right, bottom);
        this.mHwWidgetSafeInsets.applyDisplaySafeInsets(true);
        Drawable drawable = this.mSplitBackground;
        if (drawable != null) {
            drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
            invalidate();
        }
        int width = getMeasuredWidth();
        int containerWidth = this.mWidthPixels;
        if (getParent() != null && (getParent() instanceof View) && (parentWidth = ((View) getParent()).getWidth()) > 0) {
            containerWidth = parentWidth;
        }
        int parentWidth2 = this.mEndLocation;
        if (parentWidth2 > 0) {
            containerWidth = parentWidth2 - this.mStartLocation;
        }
        int realLeft = left + ((containerWidth - width) / 2) + this.mStartLocation;
        setLeft(realLeft);
        setRight(realLeft + width);
    }

    public void setSplitViewLocation(int start, int end) {
        this.mStartLocation = start;
        this.mEndLocation = end;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: huawei.android.widget.HwToolBarMenuContainer */
    /* JADX WARN: Multi-variable type inference failed */
    public void draw(Canvas canvas) {
        if (this.mBlurEngine.isShowHwBlur(this)) {
            this.mBlurEngine.draw(canvas, this);
            HwToolBarMenuContainer.super.dispatchDraw(canvas);
            drawDivider(canvas);
            return;
        }
        HwToolBarMenuContainer.super.draw(canvas);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: huawei.android.widget.HwToolBarMenuContainer */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int visibility) {
        HwToolBarMenuContainer.super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            this.mBlurEngine.addBlurTargetView(this, this.mBlurType);
            this.mBlurEngine.setTargetViewBlurEnable(this, isBlurEnable());
            int i = this.mBlurOverlayColor;
            if (i != INVALID_BLUR_OVERLAY_COLOR) {
                this.mBlurEngine.setTargetViewOverlayColor(this, i);
                return;
            }
            return;
        }
        this.mBlurEngine.removeBlurTargetView(this);
    }

    @Deprecated
    public void onWindowFocusChanged(boolean hasWindowFocus) {
    }

    public boolean isBlurEnable() {
        return this.mIsBlurEnable;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: huawei.android.widget.HwToolBarMenuContainer */
    /* JADX WARN: Multi-variable type inference failed */
    public void setBlurEnable(boolean isBlurEnable) {
        this.mIsBlurEnable = isBlurEnable;
        this.mBlurEngine.setTargetViewBlurEnable(this, isBlurEnable());
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        HwToolBarMenuContainer.super.dispatchDraw(canvas);
        drawDivider(canvas);
    }

    private void drawDivider(Canvas canvas) {
        if (this.mDivider != null) {
            Rect bounds = this.mTempRect;
            bounds.left = this.mPaddingLeft;
            bounds.right = (this.mRight - this.mLeft) - this.mPaddingRight;
            bounds.top = 0;
            bounds.bottom = this.mDivider.getIntrinsicHeight();
            this.mDivider.setBounds(bounds);
            this.mDivider.draw(canvas);
        }
    }

    public void setBlurColor(int blurColor) {
        this.mBlurOverlayColor = blurColor;
    }

    public void setBlurType(int blurTypeId) {
        HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(blurTypeId);
        if (blurType != null) {
            this.mBlurType = blurType;
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        HwToolBarMenuContainer.super.onAttachedToWindow();
        this.mHwWidgetSafeInsets.updateOriginPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    public void setPadding(int left, int top, int right, int bottom) {
        HwToolBarMenuContainer.super.setPadding(left, top, right, bottom);
        this.mHwWidgetSafeInsets.updateOriginPadding(left, top, right, bottom);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        this.mHwWidgetSafeInsets.updateWindowInsets(insets);
        return HwToolBarMenuContainer.super.onApplyWindowInsets(insets);
    }

    @Override // huawei.android.widget.HwCornerInsetsInterface
    public Rect getCornerInsets() {
        return this.mCornerInsetsRect;
    }

    @Override // huawei.android.widget.HwCornerInsetsInterface
    public void setCornerInsets(Rect rect) {
        if (rect != null) {
            this.mCornerInsetsRect.set(rect);
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), rect.bottom);
        }
    }
}
