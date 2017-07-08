package huawei.com.android.server.policy.fingersense.pixiedust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

public class GlowParticle {
    static float START_SIZE;
    double INITIAL_LIFESPAN;
    long lastUpdateMs;
    double lifespan;
    Matrix matrix;
    Paint paint;
    float size;
    Bitmap sprite;
    double x;
    double y;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.com.android.server.policy.fingersense.pixiedust.GlowParticle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.com.android.server.policy.fingersense.pixiedust.GlowParticle.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: huawei.com.android.server.policy.fingersense.pixiedust.GlowParticle.<clinit>():void");
    }

    public GlowParticle(Bitmap sprite) {
        this.paint = new Paint();
        this.INITIAL_LIFESPAN = 400.0d;
        this.lifespan = -1.0d;
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
