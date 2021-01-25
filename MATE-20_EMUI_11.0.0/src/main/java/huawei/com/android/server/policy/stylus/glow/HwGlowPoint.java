package huawei.com.android.server.policy.stylus.glow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

public class HwGlowPoint {
    private static final double BASIC_LIFESPAN = 400.0d;
    private static final float BASIC_SIZE = 100.0f;
    private static final double DEFAULT_LIFESPAN = -1.0d;
    private static final float DOUBLE = 2.0f;
    private static final int TRACE_COLOR = 255;
    private Paint mGlowPaint = new Paint();
    private long mLastUpdateTimeMillis;
    private double mLifespan = DEFAULT_LIFESPAN;
    private double mPointX;
    private double mPointY;
    private Matrix mTraceMatrix;
    private Bitmap mTraceShadow;
    private float mTraceSize;

    public HwGlowPoint(Bitmap shadow) {
        this.mTraceShadow = shadow;
        this.mTraceMatrix = new Matrix();
    }

    public void update() {
        if (!isEnded()) {
            long currentTime = System.currentTimeMillis();
            long timeInterval = currentTime - this.mLastUpdateTimeMillis;
            if (timeInterval < 0) {
                timeInterval = 0;
            }
            this.mLastUpdateTimeMillis = currentTime;
            this.mLifespan -= (double) timeInterval;
            this.mTraceSize = (float) ((this.mLifespan / BASIC_LIFESPAN) * 100.0d);
            updateMatrix();
            this.mGlowPaint.setAlpha((int) ((this.mLifespan / BASIC_LIFESPAN) * 255.0d));
        }
    }

    public void draw(Canvas canvas) {
        if (canvas != null && !isEnded() && this.mPointX > 0.0d && this.mPointY > 0.0d) {
            canvas.drawBitmap(this.mTraceShadow, this.mTraceMatrix, this.mGlowPaint);
        }
    }

    public void setTrackColor(int color) {
        this.mGlowPaint.setColorFilter(new LightingColorFilter(color, 1));
    }

    public void reset(double pointX, double pointY, float lifespanOffset) {
        this.mPointX = pointX;
        this.mPointY = pointY;
        this.mLifespan = BASIC_LIFESPAN - ((double) lifespanOffset);
        this.mTraceSize = BASIC_SIZE;
        this.mLastUpdateTimeMillis = System.currentTimeMillis();
    }

    public boolean isEnded() {
        return this.mLifespan < 0.0d;
    }

    private void updateMatrix() {
        this.mTraceMatrix.reset();
        this.mTraceMatrix.postTranslate(((float) (-this.mTraceShadow.getWidth())) / 2.0f, ((float) (-this.mTraceShadow.getHeight())) / 2.0f);
        float proportion = this.mTraceSize / ((float) this.mTraceShadow.getWidth());
        this.mTraceMatrix.postScale(proportion, proportion);
        this.mTraceMatrix.postTranslate((float) this.mPointX, (float) this.mPointY);
    }
}
