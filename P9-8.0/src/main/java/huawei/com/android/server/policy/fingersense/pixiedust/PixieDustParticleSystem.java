package huawei.com.android.server.policy.fingersense.pixiedust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;

class PixieDustParticleSystem {
    int maxEmit;
    PixieDustParticle[] particles;

    PixieDustParticleSystem(int n, Bitmap sprite, int maxEmit) {
        this.particles = new PixieDustParticle[n];
        for (int i = 0; i < n; i++) {
            this.particles[i] = new PixieDustParticle(sprite);
        }
        this.maxEmit = maxEmit;
    }

    void setColor(int color) {
        PixieDustParticle.paint.setColorFilter(new LightingColorFilter(color, 1));
    }

    void update() {
        for (PixieDustParticle p : this.particles) {
            p.update();
        }
    }

    void setEmitter(double x, double y) {
        int emitCount = 0;
        for (PixieDustParticle p : this.particles) {
            if (p.isDead() && emitCount < this.maxEmit) {
                emitCount++;
                p.rebirth(x, y);
            }
        }
    }

    void kill() {
        for (PixieDustParticle p : this.particles) {
            p.kill();
        }
    }

    void draw(Canvas canvas) {
        for (PixieDustParticle p : this.particles) {
            if (!p.isDead()) {
                p.draw(canvas);
            }
        }
    }
}
