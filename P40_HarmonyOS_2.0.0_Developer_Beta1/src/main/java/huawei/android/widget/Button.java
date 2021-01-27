package huawei.android.widget;

import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.RemoteViews;
import android.widget.TextView;
import androidhwext.R;
import com.huawei.uikit.effect.FocusAnimation;
import huawei.android.view.HwClickAnimationUtils;
import huawei.android.widget.loader.ResLoaderUtil;

@RemoteViews.RemoteView
public class Button extends android.widget.Button implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final float DEFAULT_HOVERED_STATUS_SCALE = 1.0f;
    private static final int DEVICE_TYPE_TELEVISION = 2;
    private static final int DEVICE_TYPE_WATCH = 8;
    private static final int FOCUSED_PADDING = 3;
    private AnimatorSet mDownAnimatorSet;
    private FocusAnimation mFocusedAnimation;
    private Drawable mFocusedDrawable;
    private int mFocusedOutlineColor;
    private int mFocusedPadding;
    private int mFocusedPathPadding;
    private int mFocusedPathWidth;
    private float mHoveredZoomScale;
    private boolean mIsOldViewGainFocus;
    private boolean mIsOldWindowGainFocus;
    private boolean mIsTvMode;
    private boolean mIsWatchMode;
    private float mOriginTextSize;
    private Path mPath;
    private Rect mRect;
    private AnimatorSet mUpAnimatorSet;

    public Button(Context context) {
        this(context, null);
        init();
    }

    public Button(Context context, AttributeSet attrs) {
        this(context, attrs, 16842824);
        init();
    }

    public Button(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        init();
    }

    public Button(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mPath = new Path();
        this.mIsTvMode = false;
        this.mIsWatchMode = false;
        this.mDownAnimatorSet = null;
        this.mUpAnimatorSet = null;
        this.mOriginTextSize = getTextSize();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HwButton, defStyleAttr, 0);
        this.mHoveredZoomScale = typedArray.getFloat(2, 1.0f);
        this.mFocusedDrawable = typedArray.getDrawable(0);
        this.mFocusedPathPadding = typedArray.getDimensionPixelSize(1, 0);
        this.mFocusedPathWidth = typedArray.getDimensionPixelSize(3, 0);
        typedArray.recycle();
        init();
    }

    private void init() {
        int deviceType = this.mContext.getResources().getInteger(34275393);
        if (deviceType == 2) {
            this.mIsTvMode = true;
            this.mFocusedAnimation = new FocusAnimation(getContext(), this);
        } else if (deviceType == 8) {
            this.mIsWatchMode = true;
        }
        this.mFocusedOutlineColor = ResLoaderUtil.getColor(getContext(), "emui_control_focused_dark");
        this.mFocusedPadding = (int) TypedValue.applyDimension(1, 3.0f, Resources.getSystem().getDisplayMetrics());
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        if (this.mIsTvMode) {
            disableViewClipChildren(getParent());
            this.mIsOldViewGainFocus = hasFocus();
            this.mIsOldWindowGainFocus = hasWindowFocus();
            onHwButtonFocusChanged(this.mIsOldViewGainFocus && this.mIsOldWindowGainFocus);
        }
        if (!this.mIsWatchMode && !this.mIsTvMode) {
            ViewParent parent = getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                viewGroup.setClipChildren(false);
                viewGroup.setClipToPadding(false);
            }
        }
    }

    @Override // android.view.View
    public void onHoverChanged(boolean isHovered) {
        super.onHoverChanged(isHovered);
        if (!this.mIsWatchMode && !this.mIsTvMode && Float.compare(this.mHoveredZoomScale, 1.0f) != 0) {
            if (isHovered) {
                setScaleX(this.mHoveredZoomScale);
                setScaleY(this.mHoveredZoomScale);
                return;
            }
            setScaleX(1.0f);
            setScaleY(1.0f);
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        FocusAnimation focusAnimation = this.mFocusedAnimation;
        if (focusAnimation != null) {
            focusAnimation.stopAnimation();
        }
    }

    @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
    public void onGlobalLayout() {
        HwWidgetFactory.autoTextSize(this, getContext(), this.mOriginTextSize);
    }

    @Override // android.widget.TextView
    public void setTextSize(int unit, float size) {
        this.mOriginTextSize = TypedValue.applyDimension(unit, size, getResources().getDisplayMetrics());
        super.setTextSize(unit, size);
    }

    @Override // android.widget.TextView
    public void setText(CharSequence text, TextView.BufferType type) {
        super.setText(text, type);
        requestLayout();
        invalidate();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onFocusChanged(boolean isGainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(isGainFocus, direction, previouslyFocusedRect);
        if (this.mIsTvMode) {
            if (isFocusChanged(isGainFocus, this.mIsOldWindowGainFocus)) {
                onHwButtonFocusChanged(isGainFocus);
            }
            this.mIsOldViewGainFocus = isGainFocus;
        }
    }

    @Override // android.widget.TextView, android.view.View
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (this.mIsTvMode) {
            if (isFocusChanged(this.mIsOldViewGainFocus, hasWindowFocus)) {
                onHwButtonFocusChanged(hasWindowFocus);
            }
            this.mIsOldWindowGainFocus = hasWindowFocus;
        }
    }

    private boolean isFocusChanged(boolean isNewViewGainFocus, boolean isNewWindowGainFocus) {
        boolean isOldGainFocus = this.mIsOldViewGainFocus && this.mIsOldWindowGainFocus;
        boolean isNewGainFocus = isNewViewGainFocus && isNewWindowGainFocus;
        return (isOldGainFocus && !isNewGainFocus) || (!isOldGainFocus && isNewGainFocus);
    }

    private void onHwButtonFocusChanged(boolean isGainFocus) {
        if (!isGainFocus) {
            this.mFocusedAnimation.stopAnimation();
        } else if (this.mFocusedOutlineColor != 0) {
            this.mFocusedAnimation.startAnimation();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onDraw(Canvas canvas) {
        if (canvas != null) {
            super.onDraw(canvas);
            drawFocused(canvas);
        }
    }

    private void drawFocused(Canvas canvas) {
        int i;
        int i2;
        if (hasFocus() && !this.mIsWatchMode && hasWindowFocus()) {
            if (this.mIsTvMode) {
                resetFocusedOutlinePath(getOutlineProvider());
                this.mFocusedAnimation.drawFocusAnimation(canvas, this.mPath, this.mRect, this.mFocusedOutlineColor);
            } else if (this.mFocusedDrawable != null && (i = this.mFocusedPathPadding) != 0 && (i2 = this.mFocusedPathWidth) != 0) {
                Drawable drawable = this.mFocusedDrawable;
                drawable.setBounds((-i) - i2, (-i) - i2, getWidth() + this.mFocusedPathPadding + this.mFocusedPathWidth, getHeight() + this.mFocusedPathPadding + this.mFocusedPathWidth);
                drawable.draw(canvas);
            }
        }
    }

    private void resetFocusedOutlinePath(ViewOutlineProvider provider) {
        Outline outline = new Outline();
        provider.getOutline(this, outline);
        float radius = outline.getRadius();
        Rect outRect = new Rect();
        outline.getRect(outRect);
        Rect rect = new Rect();
        rect.left = outRect.left - this.mFocusedPadding;
        rect.top = outRect.top - this.mFocusedPadding;
        rect.right = outRect.right + this.mFocusedPadding;
        rect.bottom = outRect.bottom + this.mFocusedPadding;
        if (!rect.equals(this.mRect)) {
            float focusedPathRadius = Float.compare(radius, 0.0f) == 0 ? radius : ((float) this.mFocusedPadding) + radius;
            this.mPath.reset();
            this.mPath.addRoundRect((float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, focusedPathRadius, focusedPathRadius, Path.Direction.CW);
            this.mRect = rect;
        }
    }

    private void disableViewClipChildren(ViewParent view) {
        if (view != null && (view instanceof ViewGroup)) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
        }
    }

    @Override // android.widget.TextView, android.view.View
    public boolean hasOverlappingRendering() {
        if (this.mIsTvMode) {
            return false;
        }
        return super.hasOverlappingRendering();
    }

    @Override // android.widget.TextView, android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) {
            return false;
        }
        if (!this.mIsWatchMode || !isEnabled()) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        if (action == 0) {
            AnimatorSet animatorSet = this.mUpAnimatorSet;
            if (animatorSet != null) {
                animatorSet.cancel();
            }
            this.mDownAnimatorSet = HwClickAnimationUtils.getActionDownAnimation(this, 2);
            this.mDownAnimatorSet.start();
        } else if (action == 1 || action == 3) {
            AnimatorSet animatorSet2 = this.mDownAnimatorSet;
            if (animatorSet2 != null) {
                animatorSet2.cancel();
            }
            this.mUpAnimatorSet = HwClickAnimationUtils.getActionUpAnimation(this, 2);
            this.mUpAnimatorSet.start();
        }
        return super.onTouchEvent(event);
    }
}
