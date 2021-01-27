package huawei.com.android.server.policy.stylus.glow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Path;
import android.graphics.PathMeasure;

public class HwGlowTraceSystem {
    private static final float BASIC_DISTANCE_BETWEEN_POINTS = 5.0f;
    private HwDivergentPoint[] mDivergentPoints;
    private HwGlowPoint[] mGlowPoints;
    private int mMaxDivergenceNum;

    public HwGlowTraceSystem(int pointSize, Bitmap shadow, int maxDivergenceNum) {
        this.mGlowPoints = new HwGlowPoint[pointSize];
        this.mDivergentPoints = new HwDivergentPoint[pointSize];
        for (int i = 0; i < pointSize; i++) {
            this.mDivergentPoints[i] = new HwDivergentPoint(shadow);
            this.mGlowPoints[i] = new HwGlowPoint(shadow);
        }
        this.mMaxDivergenceNum = maxDivergenceNum;
    }

    public void setTrackColor(int trackColor) {
        HwDivergentPoint.sTracePaint.setColorFilter(new LightingColorFilter(trackColor, 1));
        HwGlowPoint[] hwGlowPointArr = this.mGlowPoints;
        for (HwGlowPoint particle : hwGlowPointArr) {
            if (particle != null) {
                particle.setTrackColor(trackColor);
            }
        }
    }

    public void clear() {
        for (HwDivergentPoint divergentPoint : this.mDivergentPoints) {
            divergentPoint.clear();
        }
    }

    public void update() {
        int particleSize = this.mGlowPoints.length;
        for (int i = 0; i < particleSize; i++) {
            this.mGlowPoints[i].update();
            this.mDivergentPoints[i].update();
        }
    }

    public void draw(Canvas canvas) {
        HwGlowPoint[] hwGlowPointArr = this.mGlowPoints;
        for (HwGlowPoint glowPoint : hwGlowPointArr) {
            if (glowPoint != null) {
                glowPoint.draw(canvas);
            }
        }
        HwDivergentPoint[] hwDivergentPointArr = this.mDivergentPoints;
        for (HwDivergentPoint divergentPoint : hwDivergentPointArr) {
            if (!divergentPoint.isEnded()) {
                divergentPoint.draw(canvas);
            }
        }
    }

    public void resetDivergentPoints(double pointX, double pointY) {
        int divergenceNum = 0;
        HwDivergentPoint[] hwDivergentPointArr = this.mDivergentPoints;
        for (HwDivergentPoint divergentPoint : hwDivergentPointArr) {
            if (divergentPoint.isEnded() && divergenceNum < this.mMaxDivergenceNum) {
                divergenceNum++;
                divergentPoint.reset(pointX, pointY);
            }
        }
    }

    public void addGlowPoints(Path path, long timeInteval) {
        PathMeasure prevPathMeasure;
        char c;
        char c2 = 0;
        PathMeasure pathMeasure = new PathMeasure(path, false);
        PathMeasure prevPathMeasure2 = new PathMeasure(path, false);
        while (pathMeasure.nextContour()) {
            prevPathMeasure2.nextContour();
        }
        float[] pathPoints = {0.0f, 0.0f};
        float pathLength = prevPathMeasure2.getLength();
        char c3 = 0;
        float baseTime = ((float) timeInteval) / ((float) Math.ceil((double) (pathLength / BASIC_DISTANCE_BETWEEN_POINTS)));
        HwGlowPoint[] hwGlowPointArr = this.mGlowPoints;
        int length = hwGlowPointArr.length;
        float lifespanOffset = (float) timeInteval;
        float distanceFromEnd = 0.0f;
        int i = 0;
        while (i < length) {
            HwGlowPoint glowPoint = hwGlowPointArr[i];
            if (glowPoint == null || !glowPoint.isEnded() || distanceFromEnd > pathLength) {
                prevPathMeasure = prevPathMeasure2;
                c = c3;
            } else {
                prevPathMeasure2.getPosTan(distanceFromEnd, pathPoints, null);
                prevPathMeasure = prevPathMeasure2;
                glowPoint.reset((double) pathPoints[c2], (double) pathPoints[1], lifespanOffset);
                c = 0;
                distanceFromEnd += BASIC_DISTANCE_BETWEEN_POINTS;
                lifespanOffset -= baseTime;
            }
            i++;
            c3 = c;
            prevPathMeasure2 = prevPathMeasure;
            c2 = 0;
        }
    }
}
