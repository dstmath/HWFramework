package com.google.android.maps;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android_maps_conflict_avoidance.com.google.map.Zoom;

class ZoomHelper {
    private final AnimationSet mAnimations = new AnimationSet(false);
    private final Paint mBitmapPaint = new Paint();
    private long mCommitTime = Long.MAX_VALUE;
    private final MapController mController;
    private boolean mFading = false;
    private final Transformation mLastDrawnScale = new Transformation();
    private boolean mManualZoomActive;
    private final MapView mMapView;
    private PixelConverter mPCAtBeginningOfManualZoom;
    private Snapshot mSnapshot = null;
    private float mSnapshotOffsetX;
    private float mSnapshotOffsetY;
    private float mSnapshotScale = 1.0f;
    private final Matrix mTempMatrix = new Matrix();
    protected final Point mTempPoint = new Point();

    private static class Snapshot {
        public Bitmap bitmap;
        public GeoPoint fixedPoint;
        public final float[] fixedPointCoords;
        public final float[] fixedPointScreenCoords;
        public Zoom zoom;

        /* synthetic */ Snapshot(Snapshot -this0) {
            this();
        }

        private Snapshot() {
            this.fixedPointCoords = new float[2];
            this.fixedPointScreenCoords = new float[2];
        }
    }

    ZoomHelper(MapView mapView, MapController controller) {
        this.mMapView = mapView;
        this.mController = controller;
        this.mBitmapPaint.setFilterBitmap(true);
        this.mPCAtBeginningOfManualZoom = new PixelConverter((PixelConverter) mapView.getProjection());
    }

    boolean doZoom(boolean zoomIn, boolean delay, int xOffset, int yOffset) {
        Zoom newZoom;
        Zoom currentZoom = this.mMapView.getZoom();
        if (zoomIn) {
            newZoom = currentZoom.getNextHigherZoom();
        } else {
            newZoom = currentZoom.getNextLowerZoom();
        }
        if (newZoom == null || newZoom.getZoomLevel() > this.mMapView.getMaxZoomLevel()) {
            return false;
        }
        return doZoom(newZoom, delay, xOffset, yOffset);
    }

    boolean doZoom(Zoom newZoom, boolean delay, int xOffset, int yOffset) {
        if (this.mSnapshot == null) {
            createSnapshot();
        }
        PixelConverter pc = (PixelConverter) this.mMapView.getProjection();
        updateSnapshotFixedPoint((float) xOffset, (float) yOffset);
        this.mController.zoomTo(newZoom);
        if (!(xOffset == this.mMapView.getWidth() / 2 && yOffset == this.mMapView.getHeight() / 2)) {
            Point realLocationOfFixedPoint = pc.toPixels(this.mSnapshot.fixedPoint, null, false);
            this.mController.scrollBy(realLocationOfFixedPoint.x - xOffset, realLocationOfFixedPoint.y - yOffset);
        }
        addScale(300);
        stepAnimation(AnimationUtils.currentAnimationTimeMillis(), pc);
        if (delay) {
            this.mCommitTime = AnimationUtils.currentAnimationTimeMillis() + 600;
        } else {
            this.mMapView.preLoad();
        }
        return true;
    }

    void beginZoom(float xOffset, float yOffset) {
        this.mManualZoomActive = true;
        createSnapshot();
        this.mSnapshotOffsetX = 0.0f;
        this.mSnapshotOffsetY = 0.0f;
        this.mSnapshotScale = 1.0f;
        updateSnapshotFixedPoint(xOffset, yOffset);
        this.mPCAtBeginningOfManualZoom.setMatricesFrom((PixelConverter) this.mMapView.getProjection());
    }

    void updateZoom(float scale, float xOffset, float yOffset) {
        updateSnapshotFixedPoint(this.mPCAtBeginningOfManualZoom, this.mPCAtBeginningOfManualZoom.getInverseMatrix(), xOffset, yOffset);
        Matrix m = this.mLastDrawnScale.getMatrix();
        m.postTranslate(-xOffset, -yOffset);
        m.postScale(scale, scale);
        m.postTranslate(xOffset, yOffset);
        this.mSnapshotScale *= scale;
        ((PixelConverter) this.mMapView.getProjection()).setMatrix(m, getScale(this.mMapView.getZoom(), this.mSnapshot.zoom), this.mSnapshot.fixedPoint, this.mSnapshot.fixedPointCoords[0], this.mSnapshot.fixedPointCoords[1]);
    }

    void scrollBy(int dx, int dy) {
        this.mSnapshotOffsetX -= ((float) dx) / this.mSnapshotScale;
        this.mSnapshotOffsetY -= ((float) dy) / this.mSnapshotScale;
    }

    void endZoom() {
        PixelConverter pc = (PixelConverter) this.mMapView.getProjection();
        this.mController.zoomTo(Zoom.getZoom(calculateRoundedZoom(this.mLastDrawnScale.getMatrix().mapRadius(1.0f), this.mMapView.getZoom().getZoomLevel())));
        float focusX = this.mSnapshot.fixedPointScreenCoords[0];
        float centerY = (float) (this.mMapView.getHeight() / 2);
        int xOffset = (int) focusX;
        int yOffset = (int) this.mSnapshot.fixedPointScreenCoords[1];
        if (!(((float) xOffset) == ((float) (this.mMapView.getWidth() / 2)) && ((float) yOffset) == centerY)) {
            Point realLocationOfFixedPoint = pc.toPixels(this.mSnapshot.fixedPoint, null, false);
            this.mController.scrollBy(realLocationOfFixedPoint.x - xOffset, realLocationOfFixedPoint.y - yOffset);
        }
        addScale(200);
        stepAnimation(AnimationUtils.currentAnimationTimeMillis(), pc);
        this.mMapView.preLoad();
        this.mManualZoomActive = false;
        this.mSnapshotScale = 1.0f;
    }

    float getCurrentScale() {
        return this.mSnapshotScale;
    }

    private void updateSnapshotFixedPoint(float xOffset, float yOffset) {
        PixelConverter pc = (PixelConverter) this.mMapView.getProjection();
        Matrix inverse = this.mTempMatrix;
        if (!this.mLastDrawnScale.getMatrix().invert(inverse)) {
            Log.e("ZoomHelper", "Singular matrix " + this.mLastDrawnScale.getMatrix());
        }
        updateSnapshotFixedPoint(pc, inverse, xOffset, yOffset);
    }

    private void updateSnapshotFixedPoint(PixelConverter pc, Matrix inverse, float xOffset, float yOffset) {
        this.mSnapshot.fixedPoint = pc.fromPixels(Math.round(xOffset), Math.round(yOffset));
        this.mSnapshot.fixedPointCoords[0] = xOffset;
        this.mSnapshot.fixedPointCoords[1] = yOffset;
        this.mSnapshot.fixedPointScreenCoords[0] = xOffset;
        this.mSnapshot.fixedPointScreenCoords[1] = yOffset;
        inverse.mapPoints(this.mSnapshot.fixedPointCoords);
    }

    private static int calculateRoundedZoom(float scale, int startingZoomLevel) {
        boolean zoomOut = scale < 1.0f;
        if (zoomOut) {
            scale = 1.0f / scale;
        }
        int intZoom = (int) scale;
        int i = 0;
        while (intZoom > 1) {
            intZoom >>= 1;
            i++;
        }
        if (Math.abs(1.0d - ((double) scale)) > 0.25d) {
            i++;
        }
        if (zoomOut) {
            i = -i;
        }
        return startingZoomLevel + i;
    }

    boolean onDraw(Canvas canvas, MapView mapView, long when) {
        if (this.mSnapshot == null) {
            return false;
        }
        PixelConverter converter = (PixelConverter) this.mMapView.getProjection();
        if (!shouldDrawMap(when)) {
            canvas.drawARGB(255, 255, 255, 255);
        }
        if (when > this.mCommitTime) {
            this.mMapView.preLoad();
            this.mCommitTime = Long.MAX_VALUE;
        }
        if (!this.mManualZoomActive) {
            stepAnimation(when, converter);
        }
        this.mBitmapPaint.setAlpha((int) (this.mLastDrawnScale.getAlpha() * 255.0f));
        canvas.save();
        canvas.concat(this.mLastDrawnScale.getMatrix());
        canvas.translate(this.mSnapshotOffsetX, this.mSnapshotOffsetY);
        canvas.drawBitmap(this.mSnapshot.bitmap, 0.0f, 0.0f, this.mBitmapPaint);
        canvas.restore();
        if (this.mManualZoomActive || !this.mAnimations.hasEnded()) {
            return true;
        }
        if (this.mFading) {
            this.mAnimations.getAnimations().clear();
            this.mSnapshot = null;
            this.mFading = false;
            converter.resetMatrix();
            return false;
        } else if (this.mMapView.canCoverCenter()) {
            this.mFading = true;
            addFade();
            return true;
        } else {
            this.mMapView.mRepainter.repaint();
            return false;
        }
    }

    private float getScale(Zoom numerator, Zoom denominator) {
        if (denominator.isMoreZoomedIn(numerator)) {
            return (float) numerator.getZoomRatio(denominator);
        }
        return 1.0f / ((float) denominator.getZoomRatio(numerator));
    }

    private void addScale(long time) {
        float fromFactor = this.mLastDrawnScale.getMatrix().mapRadius(1.0f);
        float toFactor = getScale(this.mSnapshot.zoom, this.mMapView.getZoom());
        ScaleAnimation scale = new ScaleAnimation(fromFactor, toFactor, fromFactor, toFactor, this.mSnapshot.fixedPointScreenCoords[0], this.mSnapshot.fixedPointScreenCoords[1]);
        scale.setFillAfter(true);
        scale.setDuration(time);
        scale.initialize(0, 0, 0, 0);
        scale.startNow();
        scale.setInterpolator(new LinearInterpolator());
        this.mFading = false;
        this.mAnimations.getAnimations().clear();
        this.mAnimations.addAnimation(scale);
    }

    private void addFade() {
        AlphaAnimation fade = new AlphaAnimation(1.0f, 0.0f);
        fade.setFillAfter(true);
        fade.setDuration(150);
        fade.initialize(0, 0, 0, 0);
        fade.startNow();
        this.mAnimations.addAnimation(fade);
    }

    boolean shouldDrawMap(long when) {
        if (this.mManualZoomActive) {
            return false;
        }
        return (this.mSnapshot == null || this.mFading) ? true : this.mAnimations.hasEnded();
    }

    private void createSnapshot() {
        Snapshot snapshot = new Snapshot();
        snapshot.bitmap = Bitmap.createBitmap(this.mMapView.getWidth(), this.mMapView.getHeight(), Config.RGB_565);
        Canvas canvas = new Canvas(snapshot.bitmap);
        canvas.drawColor(0);
        this.mMapView.drawMap(canvas, false);
        snapshot.zoom = this.mMapView.getZoom();
        this.mLastDrawnScale.clear();
        this.mSnapshot = snapshot;
    }

    private void stepAnimation(long when, PixelConverter converter) {
        this.mAnimations.getTransformation(when, this.mLastDrawnScale);
        converter.setMatrix(this.mLastDrawnScale.getMatrix(), getScale(this.mMapView.getZoom(), this.mSnapshot.zoom), this.mSnapshot.fixedPoint, this.mSnapshot.fixedPointCoords[0], this.mSnapshot.fixedPointCoords[1]);
    }
}
