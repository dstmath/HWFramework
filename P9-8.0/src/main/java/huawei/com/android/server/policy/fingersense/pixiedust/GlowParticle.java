package huawei.com.android.server.policy.fingersense.pixiedust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

public class GlowParticle {
    static float START_SIZE = 100.0f;
    double INITIAL_LIFESPAN = 400.0d;
    long lastUpdateMs;
    double lifespan = -1.0d;
    Matrix matrix;
    Paint paint = new Paint();
    float size;
    Bitmap sprite;
    double x;
    double y;

    public GlowParticle(Bitmap sprite) {
        this.sprite = sprite;
        this.matrix = new Matrix();
    }

    void setColor(int color) {
        this.paint.setColorFilter(new LightingColorFilter(color, 1));
    }

    void rebirth(double x, double y, float lifespanOffset) {
        this.x = x;
        this.y = y;
        this.lifespan = this.INITIAL_LIFESPAN - ((double) lifespanOffset);
        this.size = START_SIZE;
        this.lastUpdateMs = System.currentTimeMillis();
    }

    void updateMatrix() {
        float scaleBy = this.size / ((float) this.sprite.getWidth());
        this.matrix.reset();
        this.matrix.postTranslate(((float) (-this.sprite.getWidth())) / 2.0f, ((float) (-this.sprite.getHeight())) / 2.0f);
        this.matrix.postScale(scaleBy, scaleBy);
        this.matrix.postTranslate((float) this.x, (float) this.y);
    }

    void updatePaint() {
        this.paint.setAlpha((int) ((this.lifespan / this.INITIAL_LIFESPAN) * 255.0d));
    }

    boolean isDead() {
        return this.lifespan < 0.0d;
    }

    public void update() {
        if (!isDead()) {
            long now = System.currentTimeMillis();
            long dt = now - this.lastUpdateMs;
            if (dt < 0) {
                dt = 0;
            }
            this.lastUpdateMs = now;
            this.lifespan -= (double) dt;
            this.size = (float) ((this.lifespan / this.INITIAL_LIFESPAN) * ((double) START_SIZE));
            updateMatrix();
            updatePaint();
        }
    }

    public void draw(Canvas c) {
        if (!isDead() && this.x > 0.0d && this.y > 0.0d) {
            c.drawBitmap(this.sprite, this.matrix, this.paint);
        }
    }
}
