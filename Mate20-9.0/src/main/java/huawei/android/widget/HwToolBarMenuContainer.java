package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import com.android.internal.widget.ActionBarContainer;
import huawei.android.widget.effect.engine.HwBlurEngine;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwToolBarMenuContainer extends ActionBarContainer {
    private static final boolean DEBUG = true;
    private static final int DEFAULT_BLUR_TYPE = HwBlurEngine.BlurType.LightBlurWithGray.getValue();
    private static final int INVALID_BLUR_OVERLAY_COLOR = -16777216;
    private static final String TAG = "HwToolBarMenuContainer";
    private boolean isBlurEnable;
    private AttributeSet mAttrs;
    private HwBlurEngine mBlurEngine;
    private int mBlurOverlayColor;
    private HwBlurEngine.BlurType mBlurType;
    private Drawable mDivider;
    private int mEndLocation;
    private boolean mForcedSplitBackground;
    private Drawable mSplitBackground;
    private int mStartLocation;
    private final Rect mTempRect;
    private int mWidthPixels;

    public HwToolBarMenuContainer(Context context) {
        this(context, null);
    }

    public HwToolBarMenuContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mBlurOverlayColor = INVALID_BLUR_OVERLAY_COLOR;
        this.mBlurType = HwBlurEngine.BlurType.LightBlurWithGray;
        this.mBlurEngine = HwBlurEngine.getInstance();
        this.isBlurEnable = false;
        this.mTempRect = new Rect();
        Log.d(TAG, "new HwToolBarMenuContainer");
        this.mAttrs = attrs;
        DisplayMetrics dp = context.getResources().getDisplayMetrics();
        this.mWidthPixels = dp.widthPixels <= dp.heightPixels ? dp.widthPixels : dp.heightPixels;
        ResLoader resLoader = ResLoader.getInstance();
        Resources.Theme theme = resLoader.getTheme(this.mContext);
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(this.mAttrs, resLoader.getIdentifierArray(getContext(), ResLoaderUtil.STAYLEABLE, "HwToolbar"), 16843946, 0);
            HwBlurEngine.BlurType blurType = HwBlurEngine.BlurType.fromTypeValue(a.getInteger(resLoader.getIdentifier(this.mContext, ResLoaderUtil.STAYLEABLE, "HwToolbar_hwToolbarSplitBlurType"), DEFAULT_BLUR_TYPE));
            if (blurType != null) {
                this.mBlurType = blurType;
            }
            this.mBlurOverlayColor = a.getColor(resLoader.getIdentifier(this.mContext, ResLoaderUtil.STAYLEABLE, "HwToolbar_hwToolbarSplitBlurOverlayColor"), INVALID_BLUR_OVERLAY_COLOR);
            a.recycle();
        }
    }

    public void setForcedSplitBackground(boolean forced) {
        this.mForcedSplitBackground = forced;
    }

    public void setSplitBackground(Drawable bg) {
        this.mSplitBackground = bg;
        this.mForcedSplitBackground = DEBUG;
    }

    private void initBackgroundResource() {
        if (this.mForcedSplitBackground) {
            if (this.mSplitBackground == null) {
                setPadding(0, 0, 0, 0);
            }
            setBackground(this.mSplitBackground);
        } else {
            ResLoader resLoader = ResLoader.getInstance();
            Resources res = resLoader.getResources(this.mContext);
            Resources.Theme theme = resLoader.getTheme(this.mContext);
            if (theme != null) {
                TypedArray a = theme.obtainStyledAttributes(this.mAttrs, resLoader.getIdentifierArray(getContext(), ResLoaderUtil.STAYLEABLE, "HwToolbar"), 16843946, 0);
                int backgroundId = a.getResourceId(resLoader.getIdentifier(getContext(), ResLoaderUtil.STAYLEABLE, "HwToolbar_hwToolbarSplitBarBackground"), -1);
                if (backgroundId != -1) {
                    setBackgroundResource(backgroundId);
                }
                int menuDividerId = a.getResourceId(resLoader.getIdentifier(getContext(), ResLoaderUtil.STAYLEABLE, "HwToolbar_hwToolbarMenuDivider"), -1);
                if (menuDividerId != -1) {
                    this.mDivider = res.getDrawable(menuDividerId, theme);
                }
                a.recycle();
            }
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
        HwToolBarMenuContainer.super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View menuView = getVisibleMenuView();
        if (menuView == null || menuView.getMeasuredHeight() <= 0) {
            setMeasuredDimension(0, 0);
            setPadding(0, 0, 0, 0);
            setBackgroundResource(0);
            return;
        }
        Drawable old = getBackground();
        initBackgroundResource();
        if (old != getBackground()) {
            HwToolBarMenuContainer.super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        HwToolBarMenuContainer.super.onLayout(changed, left, top, right, bottom);
        if (this.mSplitBackground != null) {
            this.mSplitBackground.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
            invalidate();
        }
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
        int realLeft = left + ((containerW - w) / 2) + this.mStartLocation;
        setLeft(realLeft);
        setRight(realLeft + w);
    }

    public void setSplitViewLocation(int start, int end) {
        this.mStartLocation = start;
        this.mEndLocation = end;
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.view.View, com.android.internal.widget.ActionBarContainer, huawei.android.widget.HwToolBarMenuContainer] */
    public void draw(Canvas canvas) {
        if (this.mBlurEngine.isShowHwBlur(this)) {
            this.mBlurEngine.draw(canvas, this);
            HwToolBarMenuContainer.super.dispatchDraw(canvas);
            drawDivider(canvas);
            return;
        }
        HwToolBarMenuContainer.super.draw(canvas);
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [android.view.View, com.android.internal.widget.ActionBarContainer, huawei.android.widget.HwToolBarMenuContainer] */
    /* access modifiers changed from: protected */
    public void onWindowVisibilityChanged(int visibility) {
        HwToolBarMenuContainer.super.onWindowVisibilityChanged(visibility);
        if (visibility == 0) {
            this.mBlurEngine.addBlurTargetView(this, this.mBlurType);
            this.mBlurEngine.setTargetViewBlurEnable(this, isBlurEnable());
            if (this.mBlurOverlayColor != INVALID_BLUR_OVERLAY_COLOR) {
                this.mBlurEngine.setTargetViewOverlayColor(this, this.mBlurOverlayColor);
                return;
            }
            return;
        }
        this.mBlurEngine.removeBlurTargetView(this);
    }

    public boolean isBlurEnable() {
        return this.isBlurEnable;
    }

    /* JADX WARNING: type inference failed for: r2v0, types: [android.view.View, huawei.android.widget.HwToolBarMenuContainer] */
    public void setBlurEnable(boolean blurEnable) {
        this.isBlurEnable = blurEnable;
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
}
