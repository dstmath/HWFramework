package huawei.com.android.server.policy.stylus.glow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import java.security.SecureRandom;

public class HwDivergentPoint {
    private static final double BASIC_GRAVITY_Y = 0.5d;
    private static final double BASIC_LIFESPAN = 15.0d;
    private static final double DEFAULT_LIFESPAN = -1.0d;
    private static final int DEFAULT_SIZE = 80;
    private static final int DEFAULT_SIZE_OFFSET = 20;
    private static final int DEFAULT_SPEED = 8;
    private static final int DEFAULT_SPEED_OFFSET = 8;
    private static final float DOUBLE = 2.0f;
    private static final int TRACE_COLOR = 255;
    private static SecureRandom sRandom = new SecureRandom();
    static Paint sTracePaint = new Paint();
    private double mLifespan = DEFAULT_LIFESPAN;
    private double mMoveVelocityX;
    private double mMoveVelocityY;
    private double mPointX;
    private double mPointY;
    private Matrix mTraceMatrix;
    private Bitmap mTraceShadow;

    public HwDivergentPoint(Bitmap shadow) {
        this.mTraceShadow = shadow;
        this.mTraceMatrix = new Matrix();
        float newSize = (float) (sRandom.nextInt(80) + 20);
        float proportion = 0.0f;
        int width = shadow.getWidth();
        proportion = width != 0 ? newSize / ((float) width) : proportion;
        this.mTraceMatrix.postTranslate(((float) (-width)) / 2.0f, ((float) (-shadow.getHeight())) / 2.0f);
        this.mTraceMatrix.postScale(proportion, proportion);
    }

    public void update() {
        if (!isEnded()) {
            this.mLifespan -= 1.0d;
            this.mPointX += this.mMoveVelocityX;
            double d = this.mPointY;
            double d2 = this.mMoveVelocityY;
            this.mPointY = d + d2;
            this.mMoveVelocityY = d2 + BASIC_GRAVITY_Y;
        }
    }

    public void clear() {
        this.mLifespan = DEFAULT_LIFESPAN;
    }

    public void draw(Canvas canvas) {
        if (canvas != null && !isEnded() && this.mPointX > 0.0d && this.mPointY > 0.0d) {
            sTracePaint.setAlpha((int) ((this.mLifespan / BASIC_LIFESPAN) * 255.0d));
            Matrix matrix = new Matrix(this.mTraceMatrix);
            matrix.postTranslate((float) this.mPointX, (float) this.mPointY);
            canvas.drawBitmap(this.mTraceShadow, matrix, sTracePaint);
        }
    }

    public void reset(double pointX, double pointY) {
        this.mPointX = pointX;
        this.mPointY = pointY;
        this.mLifespan = BASIC_LIFESPAN;
        double baseVelocity = sRandom.nextDouble() * 2.0d * 3.141592653589793d;
        double moveSpeed = ((double) sRandom.nextInt(8)) + 8.0d;
        this.mMoveVelocityX = Math.cos(baseVelocity) * moveSpeed;
        this.mMoveVelocityY = Math.sin(baseVelocity) * moveSpeed;
    }

    public boolean isEnded() {
        return this.mLifespan < 0.0d;
    }
}
