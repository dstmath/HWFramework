package huawei.com.android.server.policy.fingersense.pixiedust;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;
import com.android.server.gesture.GestureNavConst;

public class GlowParticleSystem {
    static final float DISTANCE_BETWEEN_PARTICLES = 5.0f;
    GlowParticle[] particles;

    GlowParticleSystem(int n, Bitmap sprite, int maxEmit) {
        this.particles = new GlowParticle[n];
        for (int i = 0; i < n; i++) {
            this.particles[i] = new GlowParticle(sprite);
        }
    }

    /* access modifiers changed from: package-private */
    public void setColor(int color) {
        for (GlowParticle p : this.particles) {
            try {
                p.setColor(color);
            } catch (NullPointerException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void update() {
        for (GlowParticle p : this.particles) {
            try {
                p.update();
            } catch (NullPointerException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void draw(Canvas c) {
        for (GlowParticle p : this.particles) {
            try {
                p.draw(c);
            } catch (NullPointerException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addParticles(Path path, long timeBetweenTouches) {
        PathMeasure pmPrev;
        boolean z;
        Path path2 = path;
        long j = timeBetweenTouches;
        PathMeasure pm = new PathMeasure(path2, false);
        PathMeasure pmPrev2 = new PathMeasure(path2, false);
        while (true) {
            pmPrev = pmPrev2;
            if (!pm.nextContour()) {
                break;
            }
            pmPrev.nextContour();
            pmPrev2 = pmPrev;
        }
        float[] pathCoordinates = {GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO};
        float contourLength = pmPrev.getLength();
        float timeDelta = ((float) j) / ((float) Math.ceil((double) (contourLength / DISTANCE_BETWEEN_PARTICLES)));
        GlowParticle[] glowParticleArr = this.particles;
        int length = glowParticleArr.length;
        float distFromEnd = 0.0f;
        float lifetimeOffset = (float) j;
        int i = 0;
        while (i < length) {
            GlowParticle p = glowParticleArr[i];
            try {
                if (!p.isDead() || distFromEnd > contourLength) {
                    z = false;
                    i++;
                    boolean z2 = z;
                    Path path3 = path;
                    long j2 = timeBetweenTouches;
                } else {
                    pmPrev.getPosTan(distFromEnd, pathCoordinates, null);
                    z = false;
                    try {
                        p.rebirth((double) pathCoordinates[0], (double) pathCoordinates[1], lifetimeOffset);
                        distFromEnd += DISTANCE_BETWEEN_PARTICLES;
                        lifetimeOffset -= timeDelta;
                    } catch (NullPointerException e) {
                    }
                    i++;
                    boolean z22 = z;
                    Path path32 = path;
                    long j22 = timeBetweenTouches;
                }
            } catch (NullPointerException e2) {
                z = false;
            }
        }
    }
}
