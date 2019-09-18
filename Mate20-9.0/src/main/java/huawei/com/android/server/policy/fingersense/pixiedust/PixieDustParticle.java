package huawei.com.android.server.policy.fingersense.pixiedust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import java.security.SecureRandom;

class PixieDustParticle {
    static Paint paint = new Paint();
    static SecureRandom random = new SecureRandom();
    double INITIAL_LIFESPAN = 15.0d;
    double gravityX = 0.0d;
    double gravityY = 0.5d;
    double lifespan = -1.0d;
    Matrix matrix;
    Bitmap sprite;
    double velocityX;
    double velocityY;
    double x;
    double y;

    public PixieDustParticle(Bitmap sprite2) {
        this.sprite = sprite2;
        this.matrix = new Matrix();
        float scaleBy = ((float) (random.nextInt(80) + 20)) / ((float) sprite2.getWidth());
        this.matrix.postTranslate(((float) (-sprite2.getWidth())) / 2.0f, ((float) (-sprite2.getHeight())) / 2.0f);
        this.matrix.postScale(scaleBy, scaleBy);
    }

    /* access modifiers changed from: package-private */
    public void rebirth(double x2, double y2) {
        double a = random.nextDouble() * 2.0d * 3.141592653589793d;
        double speed = ((double) random.nextInt(8)) + 8.0d;
        this.velocityX = Math.cos(a) * speed;
        this.velocityY = Math.sin(a) * speed;
        this.x = x2;
        this.y = y2;
        this.lifespan = this.INITIAL_LIFESPAN;
    }

    /* access modifiers changed from: package-private */
    public boolean isDead() {
        return this.lifespan < 0.0d;
    }

    public void update() {
        if (!isDead()) {
            this.lifespan -= 1.0d;
            this.x += this.velocityX;
            this.y += this.velocityY;
            this.velocityX += this.gravityX;
            this.velocityY += this.gravityY;
        }
    }

    public void kill() {
        this.lifespan = -1.0d;
    }

    public void draw(Canvas c) {
        if (!isDead() && this.x > 0.0d && this.y > 0.0d) {
            paint.setAlpha((int) (255.0d * (this.lifespan / this.INITIAL_LIFESPAN)));
            Matrix m2 = new Matrix(this.matrix);
            m2.postTranslate((float) this.x, (float) this.y);
            c.drawBitmap(this.sprite, m2, paint);
        }
    }
}
