package huawei.com.android.server.policy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.widget.FrameLayout;
import com.huawei.uikit.effect.FocusAnimation;

public class HwGlobalActionsFrameLayout extends FrameLayout {
    private static final int DP_OUT_VALUE = 3;
    private int mDrawColor;
    private Path mFocusPath;
    private int mFocusPathPadding;
    private Rect mFocusRect;
    private FocusAnimation mGlowAnimation;

    public HwGlobalActionsFrameLayout(Context context) {
        super(context);
        init(context);
    }

    public HwGlobalActionsFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.mGlowAnimation = new FocusAnimation(getContext(), this);
        this.mFocusRect = null;
        this.mFocusPath = new Path();
        this.mFocusPathPadding = (int) TypedValue.applyDimension(1, 3.0f, context.getResources().getDisplayMetrics());
        this.mDrawColor = context.getResources().getColor(33882713);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hasFocus()) {
            if (this.mFocusRect == null) {
                this.mFocusRect = getFocusOutlinePath(getContext(), getOutlineProvider(), this, this.mFocusPath);
            }
            this.mGlowAnimation.drawFocusAnimation(canvas, this.mFocusPath, this.mFocusRect, this.mDrawColor);
        }
    }

    private void disableViewClipChildren(ViewParent view) {
        if (view != null) {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                viewGroup.setClipChildren(false);
                viewGroup.setClipToPadding(false);
            }
            if (view.getParent() != null && (view.getParent() instanceof ViewGroup)) {
                ViewGroup viewGroup2 = (ViewGroup) view.getParent();
                viewGroup2.setClipChildren(false);
                viewGroup2.setClipToPadding(false);
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        disableViewClipChildren(getParent());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        FocusAnimation focusAnimation = this.mGlowAnimation;
        if (focusAnimation != null) {
            focusAnimation.stopAnimation();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            this.mGlowAnimation.startAnimation();
        } else {
            this.mGlowAnimation.stopAnimation();
        }
    }

    private Rect getFocusOutlinePath(Context context, ViewOutlineProvider provider, View view, Path path) {
        if (provider == null || view == null || path == null) {
            return new Rect();
        }
        Outline outline = new Outline();
        provider.getOutline(view, outline);
        float radius = outline.getRadius();
        Rect outRect = new Rect();
        outline.getRect(outRect);
        RectF roundRect = new RectF((float) (outRect.left - this.mFocusPathPadding), (float) (outRect.top - this.mFocusPathPadding), (float) (outRect.right + this.mFocusPathPadding), (float) (outRect.bottom + this.mFocusPathPadding));
        float newRadius = ((float) this.mFocusPathPadding) + radius;
        path.addRoundRect(roundRect, newRadius, newRadius, Path.Direction.CW);
        return new Rect(outRect.left - this.mFocusPathPadding, outRect.top - this.mFocusPathPadding, outRect.right + this.mFocusPathPadding, outRect.bottom + this.mFocusPathPadding);
    }
}
