package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.android.hwext.internal.R;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class Switch extends android.widget.Switch {
    private static final int FOCUSED_OUTLINE_COLOR = -14331913;
    private static final int GET_HALF_VALUE = 2;
    private static final int TRACK_FLANKS_PROPORTION = 24;
    protected int mFocusTrackHorizontalPadding;
    private int mFocusTrackVerticalPadding;
    private int mFocusedStrokeColor;
    private Paint mPaint;
    private ResLoader mResLoader;
    private Resources mResources;
    private Drawable mTrackDrawable;

    public Switch(Context context) {
        this(context, null);
    }

    public Switch(Context context, AttributeSet attrs) {
        this(context, attrs, 16843839);
    }

    public Switch(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Switch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, 0);
        this.mFocusedStrokeColor = 0;
        setDefaultFocusHighlightEnabled(false);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.Switch, android.widget.TextView, android.widget.CompoundButton, android.view.View
    public void onDraw(Canvas canvas) {
        if (canvas != null) {
            super.onDraw(canvas);
            drawFocusedOuterStroke(canvas);
        }
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mResLoader = ResLoader.getInstance();
        if (this.mResLoader.getTheme(context) != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HwSwitch, defStyleAttr, defStyleRes);
            this.mFocusedStrokeColor = typedArray.getColor(0, FOCUSED_OUTLINE_COLOR);
            typedArray.recycle();
        }
        this.mResources = ResLoaderUtil.getResources(context);
        this.mPaint = new Paint();
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(this.mFocusedStrokeColor);
        this.mFocusTrackVerticalPadding = (int) this.mResources.getDimension(34472909);
        this.mFocusTrackHorizontalPadding = (int) (this.mResources.getDimension(34472907) - (this.mResources.getDimension(34471989) / 24.0f));
    }

    private void drawFocusedOuterStroke(Canvas canvas) {
        if (this.mTrackDrawable == null) {
            Object trackDrawable = ReflectUtil.getObject(this, "mTrackDrawable", android.widget.Switch.class);
            if (trackDrawable instanceof Drawable) {
                this.mTrackDrawable = (Drawable) trackDrawable;
            }
        }
        if (isFocused() && this.mTrackDrawable != null) {
            Rect trackbounds = this.mTrackDrawable.getBounds();
            this.mPaint.setStrokeWidth((float) ((int) this.mResources.getDimension(34472908)));
            RectF rectF = new RectF((float) (trackbounds.left - this.mFocusTrackHorizontalPadding), (float) (trackbounds.top - this.mFocusTrackVerticalPadding), (float) (trackbounds.right + this.mFocusTrackHorizontalPadding), (float) (trackbounds.bottom + this.mFocusTrackVerticalPadding));
            int roundRadius = ((trackbounds.bottom - trackbounds.top) / 2) + this.mFocusTrackVerticalPadding;
            canvas.drawRoundRect(rectF, (float) roundRadius, (float) roundRadius, this.mPaint);
        }
    }
}
