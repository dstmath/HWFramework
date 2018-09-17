package com.google.android.maps;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.ZoomControls;
import android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidGraphics;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequest;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestListener;
import android_maps_conflict_avoidance.com.google.googlenav.map.Map;
import android_maps_conflict_avoidance.com.google.googlenav.map.TrafficService;
import android_maps_conflict_avoidance.com.google.googlenav.ui.android.AndroidTileOverlayRenderer;
import android_maps_conflict_avoidance.com.google.map.MapPoint;
import android_maps_conflict_avoidance.com.google.map.MapState;
import android_maps_conflict_avoidance.com.google.map.Zoom;
import com.google.android.maps.GestureDetector.SimpleOnGestureListener;
import java.util.List;

public class MapView extends ViewGroup {
    private static final String KEY_CENTER_LATITUDE = (MapView.class.getName() + ".centerLatitude");
    private static final String KEY_CENTER_LONGITUDE = (MapView.class.getName() + ".centerLongitude");
    private static final String KEY_ZOOM_DISPLAYED = (MapView.class.getName() + ".zoomDisplayed");
    private static final String KEY_ZOOM_LEVEL = (MapView.class.getName() + ".zoomLevel");
    private static final long ZOOM_CONTROLS_TIMEOUT = ViewConfiguration.getZoomControlsTimeout();
    private boolean mBuiltInZoomControlsEnabled;
    private MapController mController;
    private PixelConverter mConverter;
    private final AndroidGraphics mDrawer;
    private boolean mFakeStreetViewEnabled;
    private GestureDetector mGestureDetector;
    private final Drawable mGoogleLogo;
    private final int mGoogleLogoHeight;
    private final int mGoogleLogoWidth;
    private Handler mHandler;
    final String mKey;
    private int mLastFlingX;
    private int mLastFlingY;
    private Map mMap;
    private OverlayBundle mOverlayBundle;
    private AndroidTileOverlayRenderer mOverlayRenderer;
    final Repainter mRepainter;
    private Drawable mReticle;
    private ReticleDrawMode mReticleDrawMode;
    private ScaleGestureDetector mScaleGestureDetector;
    private final Scroller mScroller;
    private TrackballGestureDetector mTrackballGestureDetector;
    private ZoomButtonsController mZoomButtonsController;
    private Runnable mZoomControlRunnable;
    private ZoomControls mZoomControls;
    private ZoomHelper mZoomHelper;

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        public static final int BOTTOM = 80;
        public static final int BOTTOM_CENTER = 81;
        public static final int CENTER = 17;
        public static final int CENTER_HORIZONTAL = 1;
        public static final int CENTER_VERTICAL = 16;
        public static final int LEFT = 3;
        public static final int MODE_MAP = 0;
        public static final int MODE_VIEW = 1;
        public static final int RIGHT = 5;
        public static final int TOP = 48;
        public static final int TOP_LEFT = 51;
        public int alignment;
        public int mode;
        public GeoPoint point;
        public int x;
        public int y;

        public LayoutParams(int width, int height, GeoPoint point, int alignment) {
            this(width, height, point, 0, 0, alignment);
        }

        public LayoutParams(int width, int height, GeoPoint point, int x, int y, int alignment) {
            super(width, height);
            this.mode = 0;
            this.point = point;
            this.x = x;
            this.y = y;
            this.alignment = alignment;
        }

        public LayoutParams(int width, int height, int x, int y, int alignment) {
            super(width, height);
            this.mode = 1;
            this.x = x;
            this.y = y;
            this.alignment = alignment;
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
            this.mode = 1;
            this.alignment = 51;
        }

        public String debug(String output) {
            return output + "MapView.LayoutParams={width=" + sizeToString(this.width) + ", height=" + sizeToString(this.height) + " mode=" + this.mode + " lat=" + this.point.getLatitudeE6() + " lng=" + this.point.getLongitudeE6() + " x= " + this.x + " y= " + this.y + " alignment=" + this.alignment + "}";
        }
    }

    class Repainter implements DataRequestListener {
        private Thread mThread;

        Repainter() {
        }

        void repaint() {
            if (this.mThread == null || (this.mThread.isAlive() ^ 1) != 0) {
                this.mThread = new Thread() {
                    public void run() {
                        try {
                            AnonymousClass1.sleep(500);
                        } catch (InterruptedException e) {
                        }
                        MapView.this.postInvalidate();
                    }
                };
                this.mThread.start();
            }
        }

        public void onComplete(DataRequest dataRequest) {
            if (dataRequest.isImmediate()) {
                repaint();
            }
        }

        public void onNetworkError(int errorCode, boolean networkEverWorked, String debugMessage) {
        }
    }

    public enum ReticleDrawMode {
        DRAW_RETICLE_OVER,
        DRAW_RETICLE_UNDER,
        DRAW_RETICLE_NEVER
    }

    public MapView(Context context, String apiKey) {
        this(context, null, attr.mapViewStyle, apiKey);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, attr.mapViewStyle);
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, null);
    }

    private MapView(Context context, AttributeSet attrs, int defStyle, String apiKey) {
        super(context, attrs, defStyle);
        this.mDrawer = new AndroidGraphics(null);
        this.mOverlayBundle = null;
        this.mReticle = null;
        this.mRepainter = new Repainter();
        if (apiKey == null) {
            TypedArray a = context.obtainStyledAttributes(attrs, styleable.MapView);
            this.mKey = a.getString(styleable.MapView_apiKey);
            a.recycle();
        } else {
            this.mKey = apiKey;
        }
        if (this.mKey == null) {
            throw new IllegalArgumentException("You need to specify an API Key for each MapView.  See the MapView documentation for details.");
        }
        setWillNotDraw(false);
        if (context instanceof MapActivity) {
            ((MapActivity) context).setupMapView(this);
            this.mScroller = new Scroller(context);
            this.mGoogleLogo = context.getResources().getDrawable(drawable.maps_google_logo);
            this.mGoogleLogoWidth = this.mGoogleLogo.getIntrinsicWidth();
            this.mGoogleLogoHeight = this.mGoogleLogo.getIntrinsicHeight();
            return;
        }
        throw new IllegalArgumentException("MapViews can only be created inside instances of MapActivity.");
    }

    void setup(Map map, TrafficService traffic, DataRequestDispatcher dispatcher) {
        this.mMap = map;
        this.mConverter = new PixelConverter(map);
        this.mOverlayBundle = new OverlayBundle();
        this.mController = new MapController(this.mMap, this);
        this.mZoomHelper = new ZoomHelper(this, this.mController);
        this.mReticle = this.mContext.getResources().getDrawable(drawable.reticle);
        this.mReticleDrawMode = ReticleDrawMode.DRAW_RETICLE_OVER;
        this.mOverlayRenderer = new AndroidTileOverlayRenderer();
        this.mOverlayRenderer.setTrafficService(traffic);
        restoreMapReferences(dispatcher);
        this.mHandler = new Handler();
        this.mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
            public boolean onDown(MotionEvent e) {
                if (!MapView.this.mScroller.isFinished()) {
                    MapView.this.mScroller.abortAnimation();
                }
                MapView.this.displayZoomControls(false);
                return false;
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                MapView.this.displayZoomControls(false);
                MapView.this.mZoomHelper.scrollBy((int) distanceX, (int) distanceY);
                float scale = 1.0f / MapView.this.mZoomHelper.getCurrentScale();
                MapView.this.mController.scrollBy((int) (distanceX * scale), (int) (distanceY * scale));
                return true;
            }

            public boolean onSingleTapUp(MotionEvent e) {
                return MapView.this.mOverlayBundle.onTap(MapView.this.mConverter.fromPixels((int) e.getX(), (int) e.getY()), MapView.this);
            }

            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                MapView.this.mScroller.abortAnimation();
                MapView.this.mLastFlingX = 400;
                MapView.this.mLastFlingY = 400;
                MapView.this.mScroller.fling(MapView.this.mLastFlingX, MapView.this.mLastFlingX, ((int) (-velocityX)) / 2, ((int) (-velocityY)) / 2, 0, 800, 0, 800);
                MapView.this.postInvalidate();
                return false;
            }
        });
        this.mGestureDetector.setIsLongpressEnabled(false);
        this.mScaleGestureDetector = new ScaleGestureDetector(getContext(), new OnScaleGestureListener() {
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                MapView.this.mZoomHelper.beginZoom(detector.getFocusX(), detector.getFocusY());
                return true;
            }

            public boolean onScale(ScaleGestureDetector detector) {
                MapView.this.mZoomHelper.updateZoom(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                return true;
            }

            public void onScaleEnd(ScaleGestureDetector detector) {
                MapView.this.mZoomHelper.endZoom();
            }
        });
        this.mTrackballGestureDetector = new TrackballGestureDetector(this.mHandler);
    }

    void restoreMapReferences(DataRequestDispatcher dispatcher) {
        dispatcher.addDataRequestListener(this.mRepainter);
        this.mMap.setTileOverlayRenderer(this.mOverlayRenderer);
    }

    void cleanupMapReferences(DataRequestDispatcher dispatcher) {
        dispatcher.removeDataRequestListener(this.mRepainter);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mMap.resize(w, h);
        if (this.mReticle != null) {
            int left = (w / 2) - (this.mReticle.getIntrinsicWidth() / 2);
            int top = (h / 2) - (this.mReticle.getIntrinsicHeight() / 2);
            this.mReticle.setBounds(left, top, left + this.mReticle.getIntrinsicWidth(), top + this.mReticle.getIntrinsicHeight());
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mZoomButtonsController != null) {
            this.mZoomButtonsController.setVisible(false);
        }
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            int x = this.mScroller.getCurrX() - this.mLastFlingX;
            int y = this.mScroller.getCurrY() - this.mLastFlingY;
            this.mLastFlingX = this.mScroller.getCurrX();
            this.mLastFlingY = this.mScroller.getCurrY();
            this.mController.scrollBy(x, y);
            postInvalidate();
            return;
        }
        super.computeScroll();
    }

    public boolean isOpaque() {
        return true;
    }

    protected final void onDraw(Canvas canvas) {
        if (this.mController.isDirty()) {
            onLayout(true, 0, 0, 0, 0);
        }
        boolean drawAgain = false;
        long drawTime = getDrawingTime();
        boolean fetchTiles = false ^ 1;
        if (this.mZoomHelper.shouldDrawMap(drawTime)) {
            drawAgain = drawMap(canvas, fetchTiles) ^ 1;
        }
        if (this.mReticleDrawMode == ReticleDrawMode.DRAW_RETICLE_UNDER && (isInTouchMode() ^ 1) != 0) {
            this.mReticle.draw(canvas);
        }
        drawAgain = (drawAgain | this.mZoomHelper.onDraw(canvas, this, drawTime)) | this.mOverlayBundle.draw(canvas, this, drawTime);
        if (this.mReticleDrawMode == ReticleDrawMode.DRAW_RETICLE_OVER && (isInTouchMode() ^ 1) != 0) {
            this.mReticle.draw(canvas);
        }
        this.mGoogleLogo.draw(canvas);
        if (drawAgain | this.mController.stepAnimation(drawTime)) {
            requestLayout();
            invalidate();
        }
    }

    protected final void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int maxWidth = getMeasuredWidth();
        int maxHeight = getMeasuredHeight();
        if (maxWidth == 0 || maxHeight == 0) {
            Display display = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            if (maxWidth == 0) {
                maxWidth = display.getWidth();
            }
            if (maxHeight == 0) {
                maxHeight = display.getHeight();
            }
        }
        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec), resolveSize(maxHeight, heightMeasureSpec));
        this.mGoogleLogo.setBounds(10, (getMeasuredHeight() - this.mGoogleLogoHeight) - 10, this.mGoogleLogoWidth + 10, getMeasuredHeight() - 10);
        this.mMap.resize(getMeasuredWidth(), getMeasuredHeight());
        this.mController.onMeasure();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            this.mController.stopPanning();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    public void onFocusChanged(boolean hasFocus, int direction, Rect unused) {
        if (!hasFocus) {
            this.mController.stopPanning();
        }
        super.onWindowFocusChanged(hasFocus);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mOverlayBundle.onKeyDown(keyCode, event, this) || this.mController.onKey(this, keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mOverlayBundle.onKeyUp(keyCode, event, this) || this.mController.onKey(this, keyCode, event)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean onTrackballEvent(MotionEvent event) {
        postInvalidate();
        if (this.mOverlayBundle.onTrackballEvent(event, this)) {
            return true;
        }
        this.mTrackballGestureDetector.analyze(event);
        if (this.mTrackballGestureDetector.isScroll()) {
            this.mController.scrollByTrackball((int) (event.getX() * 10.0f), (int) (event.getY() * 10.0f));
        } else if (this.mTrackballGestureDetector.isTap()) {
            this.mOverlayBundle.onTap(new GeoPoint(this.mMap.getCenterPoint()), this);
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled() || (isClickable() ^ 1) != 0) {
            return false;
        }
        postInvalidate();
        if (this.mOverlayBundle.onTouchEvent(ev, this)) {
            return true;
        }
        this.mScaleGestureDetector.onTouchEvent(ev);
        this.mGestureDetector.onTouchEvent(ev);
        return true;
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (this.mZoomButtonsController != null && visibility != 0) {
            this.mZoomButtonsController.setVisible(false);
        }
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2, new GeoPoint(0, 0), 17);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        updateZoomControls();
        int count = getChildCount();
        Point point = new Point();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.mode == 0) {
                    this.mConverter.toPixels(lp.point, point);
                    point.x += lp.x;
                    point.y += lp.y;
                } else {
                    point.x = lp.x;
                    point.y = lp.y;
                }
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                int x = point.x;
                int y = point.y;
                int alignment = lp.alignment;
                switch (alignment & 7) {
                    case 1:
                        x -= width / 2;
                        break;
                    case LayoutParams.RIGHT /*5*/:
                        x -= width - 1;
                        break;
                }
                switch (alignment & 112) {
                    case LayoutParams.CENTER_VERTICAL /*16*/:
                        y -= height / 2;
                        break;
                    case LayoutParams.BOTTOM /*80*/:
                        y -= height - 1;
                        break;
                }
                int childLeft = this.mPaddingLeft + x;
                int childTop = this.mPaddingTop + y;
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
        this.mController.clean();
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public void displayZoomControls(boolean takeFocus) {
        if (this.mBuiltInZoomControlsEnabled) {
            if (this.mZoomButtonsController != null && (this.mZoomButtonsController.isVisible() ^ 1) != 0) {
                this.mZoomButtonsController.setFocusable(takeFocus);
                this.mZoomButtonsController.setVisible(true);
            }
        } else if (this.mZoomControls != null) {
            if (this.mZoomControls.getVisibility() == 8) {
                this.mZoomControls.show();
            }
            if (takeFocus) {
                this.mZoomControls.requestFocus();
            }
            this.mHandler.removeCallbacks(this.mZoomControlRunnable);
            this.mHandler.postDelayed(this.mZoomControlRunnable, ZOOM_CONTROLS_TIMEOUT);
        }
    }

    private boolean isLocationDisplayed() {
        if (this.mContext instanceof MapActivity) {
            return ((MapActivity) this.mContext).isLocationDisplayed();
        }
        return false;
    }

    private boolean isRouteDisplayed() {
        if (this.mContext instanceof MapActivity) {
            return ((MapActivity) this.mContext).isRouteDisplayed();
        }
        return false;
    }

    boolean drawMap(Canvas canvas, boolean fetchTiles) {
        this.mDrawer.setCanvas(canvas);
        return this.mMap.drawMap(this.mDrawer, fetchTiles, isLocationDisplayed(), isRouteDisplayed(), true, false);
    }

    public boolean canCoverCenter() {
        return this.mMap.canCover(this.mMap.getCenterPoint(), true);
    }

    public void preLoad() {
        this.mMap.preLoad(this.mMap.getCenterPoint());
    }

    Zoom getZoom() {
        return this.mMap.getZoom();
    }

    public int getZoomLevel() {
        return this.mMap.getZoom().getZoomLevel();
    }

    public void setSatellite(boolean on) {
        if (isSatellite() != on) {
            if (on) {
                this.mMap.setMapMode(1);
            } else {
                this.mMap.setMapMode(0);
            }
            updateZoomControls();
            postInvalidate();
        }
    }

    public boolean isSatellite() {
        return this.mMap.isSatellite();
    }

    public void setTraffic(boolean on) {
        if (on) {
            setStreetView(false);
        }
        this.mOverlayRenderer.setShowTraffic(on);
        postInvalidate();
    }

    public boolean isTraffic() {
        return this.mOverlayRenderer.isShowTraffic();
    }

    @Deprecated
    public void setStreetView(boolean on) {
        if (on) {
            setTraffic(false);
        }
        this.mFakeStreetViewEnabled = on;
    }

    public boolean isStreetView() {
        return this.mFakeStreetViewEnabled;
    }

    public GeoPoint getMapCenter() {
        return new GeoPoint(this.mMap.getCenterPoint());
    }

    public MapController getController() {
        return this.mController;
    }

    public final List<Overlay> getOverlays() {
        return this.mOverlayBundle.getOverlays();
    }

    public int getLatitudeSpan() {
        return this.mMap.getLatitudeSpan();
    }

    public int getLongitudeSpan() {
        MapState mapState = this.mMap.getMapState();
        MapPoint point = mapState.getCenterPoint();
        Zoom zoom = mapState.getZoom();
        int halfWidth = this.mMap.getWidth() / 2;
        int shift = 0;
        if (zoom.getZoomLevel() == 1) {
            shift = 2;
            halfWidth >>= 2;
        }
        int diff = point.pixelOffset(halfWidth, 0, zoom).getLongitude() - point.pixelOffset(-halfWidth, 0, zoom).getLongitude();
        if (diff <= 0) {
            diff += 360000000;
        }
        return diff << shift;
    }

    public void setReticleDrawMode(ReticleDrawMode mode) {
        if (mode == null) {
            throw new NullPointerException("The ReticleDrawMode cannot be null");
        }
        this.mReticleDrawMode = mode;
    }

    public int getMaxZoomLevel() {
        return this.mMap.getMaxMapZoomForPoint(this.mMap.getCenterPoint());
    }

    public void onSaveInstanceState(Bundle state) {
        state.putInt(KEY_CENTER_LATITUDE, this.mMap.getCenterPoint().getLatitude());
        state.putInt(KEY_CENTER_LONGITUDE, this.mMap.getCenterPoint().getLongitude());
        state.putInt(KEY_ZOOM_LEVEL, getZoomLevel());
        if ((this.mZoomButtonsController == null || !this.mZoomButtonsController.isVisible()) && (this.mZoomControls == null || this.mZoomControls.getVisibility() != 0)) {
            state.putInt(KEY_ZOOM_DISPLAYED, 0);
        } else {
            state.putInt(KEY_ZOOM_DISPLAYED, 1);
        }
    }

    public void onRestoreInstanceState(Bundle state) {
        if (state != null) {
            if (this.mController != null) {
                int latitude = state.getInt(KEY_CENTER_LATITUDE, Integer.MAX_VALUE);
                int longitude = state.getInt(KEY_CENTER_LONGITUDE, Integer.MAX_VALUE);
                if (!(latitude == Integer.MAX_VALUE || longitude == Integer.MAX_VALUE)) {
                    this.mController.setCenter(new GeoPoint(latitude, longitude));
                }
                int zoomLevel = state.getInt(KEY_ZOOM_LEVEL, Integer.MAX_VALUE);
                if (zoomLevel != Integer.MAX_VALUE) {
                    this.mController.setZoom(zoomLevel);
                }
            }
            if (state.getInt(KEY_ZOOM_DISPLAYED, 0) != 0) {
                displayZoomControls(false);
            }
        }
    }

    @Deprecated
    public View getZoomControls() {
        if (this.mZoomControls == null) {
            this.mZoomControls = createZoomControls();
            this.mZoomControls.setVisibility(8);
            this.mZoomControlRunnable = new Runnable() {
                public void run() {
                    if (MapView.this.mZoomControls.hasFocus()) {
                        MapView.this.mHandler.removeCallbacks(MapView.this.mZoomControlRunnable);
                        MapView.this.mHandler.postDelayed(MapView.this.mZoomControlRunnable, MapView.ZOOM_CONTROLS_TIMEOUT);
                        return;
                    }
                    MapView.this.mZoomControls.hide();
                }
            };
        }
        return this.mZoomControls;
    }

    private ZoomControls createZoomControls() {
        ZoomControls zoomControls = new ZoomControls(this.mContext);
        zoomControls.setZoomSpeed(2000);
        zoomControls.setOnZoomInClickListener(new OnClickListener() {
            public void onClick(View v) {
                MapView.this.doZoom(true);
            }
        });
        zoomControls.setOnZoomOutClickListener(new OnClickListener() {
            public void onClick(View v) {
                MapView.this.doZoom(false);
            }
        });
        return zoomControls;
    }

    public ZoomButtonsController getZoomButtonsController() {
        return this.mZoomButtonsController;
    }

    private ZoomButtonsController createZoomButtonsController() {
        final ZoomButtonsController controller = new ZoomButtonsController(this);
        controller.setZoomSpeed(2000);
        controller.setOnZoomListener(new OnZoomListener() {
            private Point mTempPoint = new Point();

            public void onVisibilityChanged(boolean visible) {
                if (visible) {
                    MapView.this.updateZoomControls();
                } else {
                    controller.setFocusable(false);
                }
            }

            public void onZoom(boolean zoomIn) {
                MapView.this.doZoom(zoomIn);
            }
        });
        return controller;
    }

    public void setBuiltInZoomControls(boolean on) {
        this.mBuiltInZoomControlsEnabled = on;
        if (this.mZoomButtonsController == null) {
            this.mZoomButtonsController = createZoomButtonsController();
        }
    }

    boolean doZoom(boolean zoomIn, int xOffset, int yOffset) {
        boolean success = false;
        if (zoomIn ? canZoomIn() : canZoomOut()) {
            this.mZoomHelper.doZoom(zoomIn, true, xOffset, yOffset);
            success = true;
        }
        updateZoomControls();
        displayZoomControls(false);
        return success;
    }

    boolean doZoom(boolean zoomIn) {
        return doZoom(zoomIn, getMeasuredWidth() / 2, getMeasuredHeight() / 2);
    }

    private void updateZoomControls() {
        if (this.mZoomControls != null) {
            this.mZoomControls.setIsZoomInEnabled(canZoomIn());
            this.mZoomControls.setIsZoomOutEnabled(canZoomOut());
        }
        if (this.mZoomButtonsController != null) {
            this.mZoomButtonsController.setZoomInEnabled(canZoomIn());
            this.mZoomButtonsController.setZoomOutEnabled(canZoomOut());
        }
    }

    private boolean canZoomOut() {
        return this.mMap.getZoom().getZoomLevel() > 1;
    }

    private boolean canZoomIn() {
        return this.mMap.getZoom().getZoomLevel() < getMaxZoomLevel();
    }

    public Projection getProjection() {
        return this.mConverter;
    }
}
