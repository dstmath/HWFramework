package huawei.com.android.server.policy.fingersense.pixiedust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;

public class GlowParticleSystem {
    static final float DISTANCE_BETWEEN_PARTICLES = 5.0f;
    GlowParticle[] particles;

    GlowParticleSystem(int n, Bitmap sprite, int maxEmit) {
        this.particles = new GlowParticle[n];
        for (int i = 0; i < n; i++) {
            this.particles[i] = new GlowParticle(sprite);
        }
    }

    void setColor(int color) {
        for (GlowParticle p : this.particles) {
            p.setColor(color);
        }
    }

    void update() {
        for (GlowParticle p : this.particles) {
            p.update();
        }
    }

    void draw(Canvas c) {
        for (GlowParticle p : this.particles) {
            p.draw(c);
        }
    }

    void addParticles(Path path, long timeBetweenTouches) {
        PathMeasure pm = new PathMeasure(path, false);
        PathMeasure pmPrev = new PathMeasure(path, false);
        while (pm.nextContour()) {
            pmPrev.nextContour();
        }
        float[] pathCoordinates = new float[]{0.0f, 0.0f};
        float contourLength = pmPrev.getLength();
        float distFromEnd = 0.0f;
        float lifetimeOffset = (float) timeBetweenTouches;
        float timeDelta = ((float) timeBetweenTouches) / ((float) Math.ceil((double) (contourLength / DISTANCE_BETWEEN_PARTICLES)));
        for (GlowParticle p : this.particles) {
            if (p.isDead() && distFromEnd <= contourLength) {
                pmPrev.getPosTan(distFromEnd, pathCoordinates, null);
                p.rebirth((double) pathCoordinates[0], (double) pathCoordinates[1], lifetimeOffset);
                distFromEnd += DISTANCE_BETWEEN_PARTICLES;
                lifetimeOffset -= timeDelta;
            }
        }
    }
}
