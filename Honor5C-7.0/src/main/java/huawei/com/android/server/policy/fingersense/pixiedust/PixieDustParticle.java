package huawei.com.android.server.policy.fingersense.pixiedust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import java.security.SecureRandom;

class PixieDustParticle {
    static Paint paint;
    static SecureRandom random;
    double INITIAL_LIFESPAN;
    double gravityX;
    double gravityY;
    double lifespan;
    Matrix matrix;
    Bitmap sprite;
    double velocityX;
    double velocityY;
    double x;
    double y;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.server.policy.fingersense.pixiedust.PixieDustParticle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.server.policy.fingersense.pixiedust.PixieDustParticle.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.server.policy.fingersense.pixiedust.PixieDustParticle.<clinit>():void");
    }

    public PixieDustParticle(Bitmap sprite) {
        this.gravityX = 0.0d;
        this.gravityY = 0.5d;
        this.INITIAL_LIFESPAN = 15.0d;
        this.lifespan = -1.0d;
        this.sprite = sprite;
        this.matrix = new Matrix();
        float scaleBy = ((float) (random.nextInt(80) + 20)) / ((float) sprite.getWidth());
        this.matrix.postTranslate(((float) (-sprite.getWidth())) / 2.0f, ((float) (-sprite.getHeight())) / 2.0f);
        this.matrix.postScale(scaleBy, scaleBy);
    }

    void rebirth(double x, double y) {
        double a = (random.nextDouble() * 2.0d) * 3.141592653589793d;
        double speed = ((double) random.nextInt(8)) + 8.0d;
        this.velocityX = Math.cos(a) * speed;
        this.velocityY = Math.sin(a) * speed;
        this.x = x;
        this.y = y;
        this.lifespan = this.INITIAL_LIFESPAN;
    }

    boolean isDead() {
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
            paint.setAlpha((int) ((this.lifespan / this.INITIAL_LIFESPAN) * 255.0d));
            Matrix m2 = new Matrix(this.matrix);
            m2.postTranslate((float) this.x, (float) this.y);
            c.drawBitmap(this.sprite, m2, paint);
        }
    }
}
