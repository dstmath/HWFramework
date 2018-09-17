package com.google.android.maps;

import android.graphics.Matrix;
import android.util.Log;
import android_maps_conflict_avoidance.com.google.common.geom.Point;
import android_maps_conflict_avoidance.com.google.googlenav.map.Map;
import android_maps_conflict_avoidance.com.google.map.MapPoint;

final class PixelConverter implements Projection {
    private final Matrix mInverse;
    private final Map mMap;
    private final Matrix mMatrix;
    private final float[] mTempFloats;
    private final Point mTempPoint;

    PixelConverter(Map map) {
        this.mMatrix = new Matrix();
        this.mInverse = new Matrix();
        this.mTempFloats = new float[2];
        this.mTempPoint = new Point();
        this.mMap = map;
        this.mMatrix.reset();
        this.mInverse.reset();
    }

    PixelConverter(PixelConverter pc) {
        this.mMatrix = new Matrix();
        this.mInverse = new Matrix();
        this.mTempFloats = new float[2];
        this.mTempPoint = new Point();
        this.mMap = pc.mMap;
    }

    void setMatricesFrom(PixelConverter pc) {
        this.mMatrix.set(pc.mMatrix);
        this.mInverse.set(pc.mInverse);
    }

    Matrix getInverseMatrix() {
        return this.mInverse;
    }

    public android.graphics.Point toPixels(GeoPoint in, android.graphics.Point out) {
        return toPixels(in, out, true);
    }

    android.graphics.Point toPixels(GeoPoint in, android.graphics.Point out, boolean transform) {
        if (out == null) {
            out = new android.graphics.Point();
        }
        synchronized (this.mTempPoint) {
            this.mMap.getPointXY(in.getMapPoint(), this.mTempPoint);
            if (transform) {
                transformTempPoint();
            }
            out.x = this.mTempPoint.x;
            out.y = this.mTempPoint.y;
        }
        return out;
    }

    public GeoPoint fromPixels(int x, int y) {
        synchronized (this.mTempPoint) {
            this.mTempFloats[0] = (float) x;
            this.mTempFloats[1] = (float) y;
            this.mInverse.mapPoints(this.mTempFloats);
            x = (int) this.mTempFloats[0];
            y = (int) this.mTempFloats[1];
        }
        MapPoint centerPoint = this.mMap.getCenterPoint();
        Point centerXY = this.mMap.getPointXY(centerPoint);
        return new GeoPoint(centerPoint.pixelOffset(x - centerXY.x, y - centerXY.y, this.mMap.getZoom()));
    }

    public float metersToEquatorPixels(float meters) {
        return this.mMatrix.mapRadius((float) this.mMap.getZoom().getPixelsForDistance((int) meters));
    }

    private void transformTempPoint() {
        this.mTempFloats[0] = (float) this.mTempPoint.x;
        this.mTempFloats[1] = (float) this.mTempPoint.y;
        this.mMatrix.mapPoints(this.mTempFloats);
        this.mTempPoint.x = (int) this.mTempFloats[0];
        this.mTempPoint.y = (int) this.mTempFloats[1];
    }

    void setMatrix(Matrix animationState, float scale, GeoPoint fixed, float fixedX, float fixedY) {
        float correctionX;
        float correctionY;
        this.mMatrix.reset();
        synchronized (this.mTempPoint) {
            this.mMap.getPointXY(fixed.getMapPoint(), this.mTempPoint);
            this.mMatrix.postTranslate((float) (-this.mTempPoint.x), (float) (-this.mTempPoint.y));
            this.mMatrix.postScale(scale, scale);
            this.mMatrix.postTranslate(fixedX, fixedY);
            correctionX = ((float) this.mTempPoint.x) - fixedX;
            correctionY = ((float) this.mTempPoint.y) - fixedY;
        }
        animationState.postTranslate(correctionX, correctionY);
        this.mMatrix.postConcat(animationState);
        if (!this.mMatrix.invert(this.mInverse)) {
            Log.e("PixelConverter", "Setting singular matrix " + this.mMatrix);
        }
    }

    void resetMatrix() {
        this.mMatrix.reset();
        this.mInverse.reset();
    }
}
