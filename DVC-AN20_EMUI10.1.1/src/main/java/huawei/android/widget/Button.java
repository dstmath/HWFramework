package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.huawei.uikit.effect.FocusAnimation;
import huawei.android.widget.loader.ResLoaderUtil;

@RemoteViews.RemoteView
public class Button extends android.widget.Button implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final int DEVICE_TYPE_TELEVISION = 2;
    private static final int FOCUSED_PADDING = 3;
    private FocusAnimation mFocusedAnimation;
    private int mFocusedOutlineColor;
    private int mFocusedPadding;
    private boolean mIsOldViewGainFocus;
    private boolean mIsOldWindowGainFocus;
    private boolean mIsTvMode;
    private float mOriginTextSize;
    private Path mPath;
    private Rect mRect;

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
        this.mOriginTextSize = getTextSize();
        init();
    }

    private void init() {
        if (this.mContext.getResources().getInteger(34275377) == 2) {
            this.mIsTvMode = true;
            this.mFocusedAnimation = new FocusAnimation(getContext(), this);
        }
        this.mFocusedOutlineColor = ResLoaderUtil.getColor(getContext(), "emui_control_focused_dark");
        this.mFocusedPadding = (int) TypedValue.applyDimension(1, 3.0f, Resources.getSystem().getDisplayMetrics());
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        if (this.mIsTvMode) {
            disableViewClipChildren(getParent());
            this.mIsOldViewGainFocus = hasFocus();
            this.mIsOldWindowGainFocus = hasWindowFocus();
            onHwButtonFocusChanged(this.mIsOldViewGainFocus && this.mIsOldWindowGainFocus);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        FocusAnimation focusAnimation = this.mFocusedAnimation;
        if (focusAnimation != null) {
            focusAnimation.stopAnimation();
        }
    }

    public void onGlobalLayout() {
        HwWidgetFactory.autoTextSize(this, getContext(), this.mOriginTextSize);
    }

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
    public void onFocusChanged(boolean isGainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(isGainFocus, direction, previouslyFocusedRect);
        if (this.mIsTvMode) {
            if (isFocusChanged(isGainFocus, this.mIsOldWindowGainFocus)) {
                onHwButtonFocusChanged(isGainFocus);
            }
            this.mIsOldViewGainFocus = isGainFocus;
        }
    }

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
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mIsTvMode && hasFocus() && hasWindowFocus()) {
            resetFocusedOutlinePath(getOutlineProvider());
            this.mFocusedAnimation.drawFocusAnimation(canvas, this.mPath, this.mRect, this.mFocusedOutlineColor);
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

    public boolean hasOverlappingRendering() {
        if (this.mIsTvMode) {
            return false;
        }
        return super.hasOverlappingRendering();
    }
}
