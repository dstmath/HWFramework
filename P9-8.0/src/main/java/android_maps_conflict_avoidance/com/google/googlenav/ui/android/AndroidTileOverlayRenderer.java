package android_maps_conflict_avoidance.com.google.googlenav.ui.android;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;
import android_maps_conflict_avoidance.com.google.common.graphics.android.AndroidImage;
import android_maps_conflict_avoidance.com.google.googlenav.map.AndroidTrafficPainter;
import android_maps_conflict_avoidance.com.google.googlenav.map.MapTile;
import android_maps_conflict_avoidance.com.google.googlenav.map.Tile;
import android_maps_conflict_avoidance.com.google.googlenav.map.TrafficTile;
import android_maps_conflict_avoidance.com.google.googlenav.ui.GmmTileOverlayRendererImpl;
import android_maps_conflict_avoidance.com.google.googlenav.ui.ShapeRenderer;
import android_maps_conflict_avoidance.com.google.googlenav.ui.ShapeRenderer.Painter;

public class AndroidTileOverlayRenderer extends GmmTileOverlayRendererImpl {
    private static final PathEffect WALKING_DASH_PATH_EFFECT = new DashPathEffect(new float[]{5.0f, 2.0f}, 0.0f);
    private static final Paint bgPaint = new Paint();
    private final AndroidPainter painter = new AndroidPainter();
    private final AndroidTrafficPainter trafficPainter = new AndroidTrafficPainter();

    private class AndroidPainter implements Painter {
        private Bitmap bitmap = null;
        private Canvas canvas = null;
        private AndroidImage image = null;
        private final Paint linePaint = new Paint();
        private final Path path = new Path();
        private final Paint polygonPaint = new Paint();
        private final Paint vertexPaint = new Paint();

        public AndroidPainter() {
            this.linePaint.setAntiAlias(true);
            this.linePaint.setStyle(Style.STROKE);
            this.vertexPaint.setAntiAlias(true);
            this.vertexPaint.setStrokeWidth(2.0f);
            this.polygonPaint.setAntiAlias(true);
        }

        public void setup(Canvas canvas, Bitmap bitmap, AndroidImage image) {
            this.canvas = canvas;
            this.bitmap = bitmap;
            this.image = image;
        }

        private void ensureCanvas() {
            if (this.canvas == null) {
                this.bitmap = Bitmap.createBitmap(this.image.getWidth(), this.image.getHeight(), Config.RGB_565);
                this.canvas = AndroidTileOverlayRenderer.paintTileBgAndCreateCanvas(this.bitmap, this.image);
            }
        }

        private void setColor(int color, Paint paint) {
            int alpha = (color >> 24) & 255;
            paint.setColor(16777215 & color);
            paint.setAlpha(alpha);
        }

        public void startLine(int color, int width, int lineStyle) {
            ensureCanvas();
            this.path.reset();
            this.linePaint.setStrokeWidth((float) width);
            if (lineStyle != 1) {
                this.linePaint.setPathEffect(null);
            } else {
                this.linePaint.setPathEffect(AndroidTileOverlayRenderer.WALKING_DASH_PATH_EFFECT);
            }
            setColor(color, this.linePaint);
        }

        public void endLine() {
            drawPendingLine();
        }

        public void addLineSegment(int[] endPoint, int[] startPoint, boolean skipTo) {
            if (skipTo) {
                drawPendingLine();
                this.path.moveTo((float) startPoint[0], (float) startPoint[1]);
            }
            this.path.lineTo((float) endPoint[0], (float) endPoint[1]);
        }

        private void drawPendingLine() {
            ensureCanvas();
            this.canvas.drawPath(this.path, this.linePaint);
            this.path.reset();
        }

        public void paintPolygon(long[][] boundaryPixelXY, int lineColor, int lineWidth, int fillColor) {
            ensureCanvas();
            Path path = new Path();
            int boundaryCount = boundaryPixelXY.length;
            for (int boundary = 0; boundary < boundaryCount; boundary++) {
                path.moveTo((float) ShapeRenderer.getX(boundaryPixelXY[boundary][0]), (float) ShapeRenderer.getY(boundaryPixelXY[boundary][0]));
                int pointCount = boundaryPixelXY[boundary].length;
                for (int point = 1; point < pointCount; point++) {
                    path.lineTo((float) ShapeRenderer.getX(boundaryPixelXY[boundary][point]), (float) ShapeRenderer.getY(boundaryPixelXY[boundary][point]));
                }
                path.close();
            }
            path.setFillType(FillType.EVEN_ODD);
            if (fillColor != -1) {
                this.polygonPaint.setStyle(Style.FILL_AND_STROKE);
            } else {
                this.polygonPaint.setStyle(Style.STROKE);
            }
            setColor(fillColor, this.polygonPaint);
            this.canvas.drawPath(path, this.polygonPaint);
            if (lineColor != -1) {
                path.setFillType(FillType.WINDING);
                this.polygonPaint.setStrokeWidth((float) lineWidth);
                this.polygonPaint.setStyle(Style.STROKE);
                setColor(lineColor, this.polygonPaint);
                this.canvas.drawPath(path, this.polygonPaint);
            }
        }

        public void paintEllipse(int x, int y, int width, int height, int outlineWidth, int outlineColor, int fillColor) {
            ensureCanvas();
            RectF rect = new RectF((float) (x - (width / 2)), (float) (y - (height / 2)), (float) width, (float) height);
            this.polygonPaint.setStyle(Style.FILL);
            this.polygonPaint.setAntiAlias(true);
            setColor(fillColor, this.polygonPaint);
            this.canvas.drawOval(rect, this.polygonPaint);
            this.polygonPaint.setStrokeWidth((float) outlineWidth);
            this.polygonPaint.setStyle(Style.STROKE);
            setColor(outlineColor, this.polygonPaint);
            this.canvas.drawOval(rect, this.polygonPaint);
        }

        public Bitmap getBitmap() {
            return this.bitmap;
        }
    }

    public boolean isFast() {
        return true;
    }

    protected GoogleImage generateNewTileImage(MapTile mapTile, TrafficTile tt) {
        AndroidImage image = (AndroidImage) mapTile.getImage();
        Bitmap bitmap = null;
        Canvas canvas = null;
        if (!(tt == null || tt.isEmpty())) {
            bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Config.RGB_565);
            canvas = paintTileBgAndCreateCanvas(bitmap, image);
            this.trafficPainter.setup(canvas);
            this.trafficRenderer.renderTrafficTile(tt, this.trafficPainter);
        }
        this.painter.setup(canvas, bitmap, image);
        if (this.shapeRenderer != null) {
            Tile tile = mapTile.getLocation();
            this.shapeRenderer.render(this.painter, tile.getXPixelTopLeft(), tile.getYPixelTopLeft(), 256, 256, tile.getZoom());
        }
        if (this.painter.getBitmap() == null) {
            return null;
        }
        return new AndroidImage(this.painter.getBitmap());
    }

    private static Canvas paintTileBgAndCreateCanvas(Bitmap bitmap, AndroidImage image) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(image.getBitmap(), 0.0f, 0.0f, bgPaint);
        return canvas;
    }
}
