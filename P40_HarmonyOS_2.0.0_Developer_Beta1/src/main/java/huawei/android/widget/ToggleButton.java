package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import com.android.hwext.internal.R;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class ToggleButton extends android.widget.ToggleButton {
    private static final int FOCUSED_OUTLINE_COLOR = -14331913;
    private GradientDrawable mFocusedDrawable;
    private int mFocusedStatusPadding;
    private float mFocusedStatusRectRadius;
    private float mHoverStatusScale;
    private float mOutHoveredStatusScale;
    private ResLoader mResLoader;
    private Resources mResources;
    private int mToggleStrokeColor;

    public ToggleButton(Context context) {
        this(context, null);
    }

    public ToggleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 16842827);
    }

    public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ToggleButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, 0);
        this.mHoverStatusScale = 1.0f;
        this.mFocusedStatusPadding = 0;
        this.mOutHoveredStatusScale = 1.0f;
        this.mToggleStrokeColor = 0;
        this.mFocusedStatusRectRadius = 0.0f;
        setDefaultFocusHighlightEnabled(false);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mResLoader = ResLoader.getInstance();
        if (this.mResLoader.getTheme(context) != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.hwToggleButton, defStyleAttr, defStyleRes);
            Drawable drawable = array.getDrawable(0);
            if (drawable instanceof GradientDrawable) {
                this.mFocusedDrawable = (GradientDrawable) drawable;
            }
            array.recycle();
        }
        this.mResources = ResLoaderUtil.getResources(context);
        TypedValue outValue = new TypedValue();
        this.mResources.getValue(34472513, outValue, true);
        this.mHoverStatusScale = outValue.getFloat();
        this.mResources.getValue(34472739, outValue, true);
        this.mOutHoveredStatusScale = outValue.getFloat();
        this.mFocusedStatusPadding = (int) this.mResources.getDimension(34472734);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            viewGroup.setClipChildren(false);
            viewGroup.setClipToPadding(false);
        }
    }

    @Override // android.view.View
    public boolean onHoverEvent(MotionEvent event) {
        if (event == null || !isEnabled()) {
            return false;
        }
        int action = event.getAction();
        if (action == 9) {
            setScaleX(this.mHoverStatusScale);
            setScaleY(this.mHoverStatusScale);
        } else if (action == 10) {
            setScaleX(this.mOutHoveredStatusScale);
            setScaleY(this.mOutHoveredStatusScale);
        }
        return super.onHoverEvent(event);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.widget.CompoundButton, android.view.View
    public void onDraw(Canvas canvas) {
        if (canvas != null) {
            super.onDraw(canvas);
            if (isFocused() && this.mFocusedDrawable != null) {
                canvas.translate((float) getScrollX(), (float) getScrollY());
                int dimension = (int) this.mResources.getDimension(34472736);
                GradientDrawable gradientDrawable = this.mFocusedDrawable;
                int i = this.mFocusedStatusPadding;
                gradientDrawable.setBounds((-i) - dimension, (-i) - dimension, getWidth() + this.mFocusedStatusPadding + dimension, getHeight() + this.mFocusedStatusPadding + dimension);
                this.mFocusedDrawable.draw(canvas);
                canvas.translate((float) (-getScrollX()), (float) (-getScrollY()));
            }
        }
    }
}
