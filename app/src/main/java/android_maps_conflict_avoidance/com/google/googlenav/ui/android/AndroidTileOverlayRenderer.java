package android_maps_conflict_avoidance.com.google.googlenav.ui.android;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
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
    private static final PathEffect WALKING_DASH_PATH_EFFECT = null;
    private static final Paint bgPaint = null;
    private final AndroidPainter painter;
    private final AndroidTrafficPainter trafficPainter;

    private class AndroidPainter implements Painter {
        private Bitmap bitmap;
        private Canvas canvas;
        private AndroidImage image;
        private final Paint linePaint;
        private final Path path;
        private final Paint polygonPaint;
        private final Paint vertexPaint;

        public AndroidPainter() {
            this.canvas = null;
            this.bitmap = null;
            this.image = null;
            this.path = new Path();
            this.linePaint = new Paint();
            this.vertexPaint = new Paint();
            this.polygonPaint = new Paint();
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.googlenav.ui.android.AndroidTileOverlayRenderer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.googlenav.ui.android.AndroidTileOverlayRenderer.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.googlenav.ui.android.AndroidTileOverlayRenderer.<clinit>():void");
    }

    public AndroidTileOverlayRenderer() {
        this.painter = new AndroidPainter();
        this.trafficPainter = new AndroidTrafficPainter();
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
