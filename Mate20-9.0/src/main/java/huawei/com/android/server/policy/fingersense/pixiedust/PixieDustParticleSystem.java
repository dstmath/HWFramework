package huawei.com.android.server.policy.fingersense.pixiedust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;

class PixieDustParticleSystem {
    int maxEmit;
    PixieDustParticle[] particles;

    PixieDustParticleSystem(int n, Bitmap sprite, int maxEmit2) {
        this.particles = new PixieDustParticle[n];
        for (int i = 0; i < n; i++) {
            this.particles[i] = new PixieDustParticle(sprite);
        }
        this.maxEmit = maxEmit2;
    }

    /* access modifiers changed from: package-private */
    public void setColor(int color) {
        PixieDustParticle.paint.setColorFilter(new LightingColorFilter(color, 1));
    }

    /* access modifiers changed from: package-private */
    public void update() {
        for (PixieDustParticle p : this.particles) {
            p.update();
        }
    }

    /* access modifiers changed from: package-private */
    public void setEmitter(double x, double y) {
        int emitCount = 0;
        for (PixieDustParticle p : this.particles) {
            if (p.isDead() && emitCount < this.maxEmit) {
                emitCount++;
                p.rebirth(x, y);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void kill() {
        for (PixieDustParticle p : this.particles) {
            p.kill();
        }
    }

    /* access modifiers changed from: package-private */
    public void draw(Canvas canvas) {
        for (PixieDustParticle p : this.particles) {
            if (!p.isDead()) {
                p.draw(canvas);
            }
        }
    }
}
