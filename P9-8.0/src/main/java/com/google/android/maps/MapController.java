package com.google.android.maps;

import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android_maps_conflict_avoidance.com.google.googlenav.map.Map;
import android_maps_conflict_avoidance.com.google.map.MapPoint;
import android_maps_conflict_avoidance.com.google.map.Zoom;
import com.google.android.maps.MapView.LayoutParams;

public final class MapController implements OnKeyListener {
    private static final /* synthetic */ int[] -com-google-android-maps-MapController$HorizPanStateSwitchesValues = null;
    private static final /* synthetic */ int[] -com-google-android-maps-MapController$VertPanStateSwitchesValues = null;
    private static final Transformation EMPTY_TRANSFORM = new Transformation();
    private Message mAnimationCompletedMessage = null;
    private Runnable mAnimationCompletedRunnable = null;
    private int mDeferredLatSpanE6 = -1;
    private int mDeferredLonSpanE6 = -1;
    private final int[] mDeltas = new int[]{0, 0};
    private volatile boolean mDirty;
    private boolean mHasBeenMeasured = false;
    private HorizPanState mHorizPan = HorizPanState.NONE;
    private final Map mMap;
    private MapView mMapView = null;
    private final float[] mOrigin = new float[]{0.0f, 0.0f};
    private Animation mPanAnimation = null;
    private MapPoint mPanPoint = null;
    private VertPanState mVertPan = VertPanState.NONE;
    private float mXPanSpeed = 0.0f;
    private float mYPanSpeed = 0.0f;

    enum HorizPanState {
        LEFT,
        RIGHT,
        NONE
    }

    enum VertPanState {
        UP,
        DOWN,
        NONE
    }

    private static /* synthetic */ int[] -getcom-google-android-maps-MapController$HorizPanStateSwitchesValues() {
        if (-com-google-android-maps-MapController$HorizPanStateSwitchesValues != null) {
            return -com-google-android-maps-MapController$HorizPanStateSwitchesValues;
        }
        int[] iArr = new int[HorizPanState.values().length];
        try {
            iArr[HorizPanState.LEFT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[HorizPanState.NONE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[HorizPanState.RIGHT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -com-google-android-maps-MapController$HorizPanStateSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getcom-google-android-maps-MapController$VertPanStateSwitchesValues() {
        if (-com-google-android-maps-MapController$VertPanStateSwitchesValues != null) {
            return -com-google-android-maps-MapController$VertPanStateSwitchesValues;
        }
        int[] iArr = new int[VertPanState.values().length];
        try {
            iArr[VertPanState.DOWN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[VertPanState.NONE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[VertPanState.UP.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -com-google-android-maps-MapController$VertPanStateSwitchesValues = iArr;
        return iArr;
    }

    MapController(Map map, MapView mapView) {
        this.mMap = map;
        this.mMapView = mapView;
    }

    public void stopPanning() {
        this.mHorizPan = HorizPanState.NONE;
        this.mVertPan = VertPanState.NONE;
    }

    int[] getDeltas() {
        switch (-getcom-google-android-maps-MapController$HorizPanStateSwitchesValues()[this.mHorizPan.ordinal()]) {
            case 1:
                this.mXPanSpeed = curve(this.mXPanSpeed, -8.0f);
                break;
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                this.mXPanSpeed = 0.0f;
                break;
            case LayoutParams.LEFT /*3*/:
                this.mXPanSpeed = curve(this.mXPanSpeed, 8.0f);
                break;
        }
        switch (-getcom-google-android-maps-MapController$VertPanStateSwitchesValues()[this.mVertPan.ordinal()]) {
            case 1:
                this.mYPanSpeed = curve(this.mYPanSpeed, 8.0f);
                break;
            case OverlayItem.ITEM_STATE_SELECTED_MASK /*2*/:
                this.mYPanSpeed = 0.0f;
                break;
            case LayoutParams.LEFT /*3*/:
                this.mYPanSpeed = curve(this.mYPanSpeed, -8.0f);
                break;
        }
        this.mDeltas[0] = (int) this.mXPanSpeed;
        this.mDeltas[1] = (int) this.mYPanSpeed;
        return this.mDeltas;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (event.getAction()) {
            case LayoutParams.MODE_MAP /*0*/:
                if (!onKeyDown(keyCode)) {
                    return false;
                }
                repaint();
                return true;
            case 1:
                if (!onKeyUp(keyCode)) {
                    return false;
                }
                repaint();
                return true;
            default:
                throw new IllegalArgumentException("Unknown key action: " + event.getAction());
        }
    }

    private boolean onKeyUp(int keyCode) {
        switch (keyCode) {
            case 19:
                if (this.mVertPan != VertPanState.UP) {
                    return false;
                }
                this.mVertPan = VertPanState.NONE;
                return true;
            case 20:
                if (this.mVertPan != VertPanState.DOWN) {
                    return false;
                }
                this.mVertPan = VertPanState.NONE;
                return true;
            case 21:
                if (this.mHorizPan != HorizPanState.LEFT) {
                    return false;
                }
                this.mHorizPan = HorizPanState.NONE;
                return true;
            case 22:
                if (this.mHorizPan != HorizPanState.RIGHT) {
                    return false;
                }
                this.mHorizPan = HorizPanState.NONE;
                return true;
            default:
                return false;
        }
    }

    private boolean onKeyDown(int keyCode) {
        switch (keyCode) {
            case 19:
                this.mVertPan = VertPanState.UP;
                return true;
            case 20:
                this.mVertPan = VertPanState.DOWN;
                return true;
            case 21:
                this.mHorizPan = HorizPanState.LEFT;
                return true;
            case 22:
                this.mHorizPan = HorizPanState.RIGHT;
                return true;
            default:
                return false;
        }
    }

    private float curve(float last, float max) {
        return ((max - last) / 8.0f) + last;
    }

    public void animateTo(GeoPoint point) {
        animateTo(point, null, null);
    }

    public void animateTo(GeoPoint point, Message message) {
        animateTo(point, null, message);
    }

    public void animateTo(GeoPoint point, Runnable runnable) {
        animateTo(point, runnable, null);
    }

    private void animateTo(GeoPoint point, Runnable runnable, Message message) {
        this.mAnimationCompletedRunnable = runnable;
        this.mAnimationCompletedMessage = message;
        MapPoint mapPoint = point.getMapPoint();
        stopAnimation(false);
        this.mMap.preLoad(mapPoint);
        this.mPanPoint = mapPoint;
        MapPoint center = this.mMap.getCenterPoint();
        int animateMillis = (int) Math.min((10.0d * Math.sqrt((double) ((int) mapPoint.pixelDistanceSquared(center, this.mMap.getZoom())))) + 200.0d, 800.0d);
        this.mPanAnimation = new TranslateAnimation(((float) center.getLatitude()) / 1000000.0f, ((float) mapPoint.getLatitude()) / 1000000.0f, ((float) center.getLongitude()) / 1000000.0f, ((float) mapPoint.getLongitude()) / 1000000.0f);
        this.mPanAnimation.setDuration((long) animateMillis);
        this.mPanAnimation.startNow();
        this.mPanAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        this.mPanAnimation.initialize(0, 0, 0, 0);
        repaint();
    }

    boolean stepAnimation(long drawTime) {
        int[] panDeltas = getDeltas();
        if (panDeltas[0] != 0 || panDeltas[1] != 0) {
            scrollBy(panDeltas[0], panDeltas[1]);
            return true;
        } else if (this.mPanAnimation == null) {
            return false;
        } else {
            Transformation xform = EMPTY_TRANSFORM;
            xform.clear();
            if (this.mPanAnimation.getTransformation(drawTime, xform)) {
                this.mOrigin[0] = 0.0f;
                this.mOrigin[1] = 0.0f;
                xform.getMatrix().mapPoints(this.mOrigin);
                centerMapToInternal(new MapPoint((int) (((double) this.mOrigin[0]) * 1000000.0d), (int) (((double) this.mOrigin[1]) * 1000000.0d)));
                return true;
            }
            centerMapToInternal(this.mPanPoint);
            this.mPanPoint = null;
            this.mPanAnimation = null;
            if (this.mAnimationCompletedMessage != null) {
                this.mAnimationCompletedMessage.sendToTarget();
                this.mAnimationCompletedMessage = null;
            }
            if (this.mAnimationCompletedRunnable != null) {
                this.mMapView.post(this.mAnimationCompletedRunnable);
                this.mAnimationCompletedRunnable = null;
            }
            return false;
        }
    }

    public void scrollBy(int x, int y) {
        stopAnimation(false);
        centerMapToInternal(this.mMap.getCenterPoint().pixelOffset(x, y, this.mMap.getZoom()));
    }

    void scrollByTrackball(int x, int y) {
        if (this.mPanAnimation == null || AnimationUtils.currentAnimationTimeMillis() - this.mPanAnimation.getStartTime() >= 250) {
            scrollBy(x, y);
        }
    }

    void repaint() {
        this.mDirty = true;
        this.mMapView.postInvalidate();
    }

    public void setCenter(GeoPoint point) {
        centerMapToInternal(point.getMapPoint());
    }

    private void centerMapToInternal(MapPoint mapPoint) {
        this.mMap.setCenterPoint(mapPoint);
        repaint();
    }

    public void stopAnimation(boolean jumpToFinish) {
        if (this.mPanAnimation != null) {
            if (jumpToFinish) {
                synchronized (this.mMap) {
                    centerMapToInternal(this.mPanPoint);
                }
            }
            this.mPanAnimation = null;
            this.mPanPoint = null;
        }
        this.mAnimationCompletedMessage = null;
    }

    void zoomTo(Zoom zoom) {
        this.mMap.setZoom(zoom);
        repaint();
    }

    public int setZoom(int zoomLevel) {
        zoomLevel = Math.min(22, Math.max(1, zoomLevel));
        zoomTo(Zoom.getZoom(zoomLevel));
        return zoomLevel;
    }

    public void zoomToSpan(int latSpanE6, int lonSpanE6) {
        if (this.mHasBeenMeasured) {
            this.mMap.zoomToSpan(latSpanE6, lonSpanE6);
            repaint();
            return;
        }
        this.mDeferredLatSpanE6 = latSpanE6;
        this.mDeferredLonSpanE6 = lonSpanE6;
    }

    public boolean zoomIn() {
        return this.mMapView.doZoom(true);
    }

    public boolean zoomOut() {
        return this.mMapView.doZoom(false);
    }

    public boolean zoomInFixing(int xPixel, int yPixel) {
        return this.mMapView.doZoom(true, xPixel, yPixel);
    }

    public boolean zoomOutFixing(int xPixel, int yPixel) {
        return this.mMapView.doZoom(false, xPixel, yPixel);
    }

    void onMeasure() {
        if (!this.mHasBeenMeasured) {
            this.mHasBeenMeasured = true;
            if (this.mDeferredLatSpanE6 >= 0) {
                zoomToSpan(this.mDeferredLatSpanE6, this.mDeferredLonSpanE6);
            }
        }
    }

    boolean isDirty() {
        return this.mDirty;
    }

    void clean() {
        this.mDirty = false;
    }
}
