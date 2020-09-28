package huawei.com.android.internal.view.menu;

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
import com.android.internal.view.menu.ListMenuItemView;
import com.huawei.uikit.effect.FocusAnimation;

public class HwFocusedListMenuItemView extends ListMenuItemView {
    private static final int DP_OUT_VALUE = 3;
    private int mDrawColor;
    private int mFocusPathPadding;
    private FocusAnimation mGlowAnimation;
    private Path mPath;
    private Rect mRect;

    public HwFocusedListMenuItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mPath = new Path();
        init(context);
    }

    public HwFocusedListMenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwFocusedListMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 16844018);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: huawei.com.android.internal.view.menu.HwFocusedListMenuItemView */
    /* JADX WARN: Multi-variable type inference failed */
    private void init(Context context) {
        this.mGlowAnimation = new FocusAnimation(getContext(), this);
        this.mDrawColor = context.getResources().getColor(33882624);
        this.mFocusPathPadding = (int) TypedValue.applyDimension(1, 3.0f, context.getResources().getDisplayMetrics());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: huawei.com.android.internal.view.menu.HwFocusedListMenuItemView */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        HwFocusedListMenuItemView.super.onDraw(canvas);
        if (isSelected() && hasWindowFocus()) {
            this.mGlowAnimation.drawFocusAnimation(canvas, this.mPath, getFocusOutlinePath(getOutlineProvider(), this, this.mFocusPathPadding, this.mPath), this.mDrawColor);
        }
    }

    private void disableViewClipChildren(ViewParent view) {
        if (view != null && (view instanceof ViewGroup)) {
            ViewGroup viewGroup = (ViewGroup) view;
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        HwFocusedListMenuItemView.super.onAttachedToWindow();
        disableViewClipChildren(getParent());
    }

    public void setSelected(boolean selected) {
        HwFocusedListMenuItemView.super.setSelected(selected);
        if (selected) {
            this.mGlowAnimation.startAnimation();
        } else {
            this.mGlowAnimation.stopAnimation();
        }
    }

    private Rect getFocusOutlinePath(ViewOutlineProvider provider, View view, int padding, Path path) {
        if (provider == null || view == null || path == null) {
            return new Rect();
        }
        Outline outline = new Outline();
        provider.getOutline(view, outline);
        float radius = outline.getRadius();
        Rect outRect = new Rect();
        outline.getRect(outRect);
        RectF roundRect = new RectF((float) (outRect.left - padding), (float) (outRect.top - padding), (float) (outRect.right + padding), (float) (outRect.bottom + padding));
        Rect rect = new Rect(outRect.left - padding, outRect.top - padding, outRect.right + padding, outRect.bottom + padding);
        if (!rect.equals(this.mRect)) {
            path.addRoundRect(roundRect, ((float) padding) + radius, ((float) padding) + radius, Path.Direction.CW);
            this.mRect = rect;
        }
        return rect;
    }
}
