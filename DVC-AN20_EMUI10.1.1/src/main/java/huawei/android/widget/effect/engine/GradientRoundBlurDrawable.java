package huawei.android.widget.effect.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class GradientRoundBlurDrawable extends Drawable {
    private static final int RADIUS = 25;
    private static final int RADIUS_OFFSET = 4;
    private static final int SHADOW_OFFSET = 19;
    private Bitmap mBitmap;
    private ColorFilter mColorFilter;
    private Context mContext;
    private Paint mPaint;
    private float[] mPositions;
    private int mRadiusOffset;
    private int[] mShadowColors;
    private int mShadowPadding;

    public GradientRoundBlurDrawable(Context context, int[] shadowColors) {
        this(context, shadowColors, 4, SHADOW_OFFSET);
    }

    public GradientRoundBlurDrawable(Context context, int[] shadowColors, int radiusOffset, int shadowPadding) {
        this.mPaint = new Paint();
        this.mPositions = new float[]{0.0f, 1.0f};
        this.mContext = context;
        this.mShadowColors = shadowColors;
        this.mRadiusOffset = radiusOffset;
        this.mShadowPadding = shadowPadding;
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        Rect boundsBlur = new Rect(bounds);
        int i = this.mRadiusOffset;
        boundsBlur.inset(25 - i, 25 - i);
        Bitmap bitmap = this.mBitmap;
        if (bitmap == null || bitmap.getWidth() < bounds.width() || this.mBitmap.getHeight() < bounds.height()) {
            this.mBitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        }
        this.mPaint.setShader(new LinearGradient((float) bounds.left, (float) bounds.top, (float) bounds.right, (float) bounds.top, this.mShadowColors, this.mPositions, Shader.TileMode.CLAMP));
        this.mPaint.setMaskFilter(new BlurMaskFilter(25.0f, BlurMaskFilter.Blur.NORMAL));
        new Canvas(this.mBitmap).drawCircle((float) boundsBlur.centerX(), (float) boundsBlur.centerY(), (float) (boundsBlur.width() >> 1), this.mPaint);
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(this.mBitmap, 0.0f, 0.0f, this.mPaint);
        Paint paint = new Paint();
        paint.setColorFilter(this.mColorFilter);
        paint.setAntiAlias(true);
        canvas.drawCircle((float) getBounds().centerX(), (float) (getBounds().centerY() - this.mShadowPadding), (float) ((getBounds().width() >> 1) - this.mShadowPadding), paint);
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mColorFilter = colorFilter;
    }

    public int getOpacity() {
        return -2;
    }
}
