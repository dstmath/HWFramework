package android_maps_conflict_avoidance.com.google.googlenav.map;

import android.graphics.AvoidXfermode;
import android.graphics.AvoidXfermode.Mode;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Xfermode;
import android_maps_conflict_avoidance.com.google.googlenav.labs.android.TrafficWithLabelsLab;
import android_maps_conflict_avoidance.com.google.googlenav.map.TrafficRenderer.Path;
import android_maps_conflict_avoidance.com.google.googlenav.map.TrafficRenderer.TrafficPainter;

public class AndroidTrafficPainter implements TrafficPainter {
    private static final float[] MAJOR_DASH_INTERVALS = new float[]{12.0f, 6.0f};
    private static final float[] MINOR_DASH_INTERVALS = new float[]{6.0f, 12.0f};
    private Canvas canvas = null;
    private boolean isPreserveLabels;
    private Xfermode mainXfermode;
    private final PathEffect majorDash = new DashPathEffect(MAJOR_DASH_INTERVALS, 0.0f);
    private final PathEffect minorDash = new DashPathEffect(MINOR_DASH_INTERVALS, 6.0f);
    private final Paint paint = new Paint();
    private final AvoidXfermode preserveLabels = new AvoidXfermode(-12566464, 253, Mode.AVOID);

    private static class AndroidPath implements Path {
        private final android.graphics.Path path;

        private AndroidPath() {
            this.path = new android.graphics.Path();
        }

        public void lineTo(int x, int y) {
            this.path.lineTo((float) (x >> 8), (float) (y >> 8));
        }

        public void moveTo(int x, int y) {
            this.path.moveTo((float) (x >> 8), (float) (y >> 8));
        }

        public android.graphics.Path getPath() {
            return this.path;
        }
    }

    public void setup(Canvas canvas) {
        this.canvas = canvas;
        this.paint.setAntiAlias(true);
        this.paint.setStrokeJoin(Join.ROUND);
        this.paint.setStrokeCap(Cap.ROUND);
        this.paint.setStyle(Style.STROKE);
        this.isPreserveLabels = TrafficWithLabelsLab.INSTANCE.isActive();
        this.mainXfermode = !this.isPreserveLabels ? null : this.preserveLabels;
    }

    public void addTrafficLine(Path path, int color, int width) {
        boolean isDashed = false;
        width >>= 8;
        if (this.isPreserveLabels && color == -788529153) {
            color = -1;
            width += 2;
        }
        if (color == -6553600) {
            isDashed = true;
        }
        this.paint.setStrokeWidth((float) width);
        this.paint.setColor(color);
        this.paint.setPathEffect(!isDashed ? null : this.majorDash);
        this.paint.setXfermode(this.mainXfermode);
        android.graphics.Path graphicsPath = ((AndroidPath) path).getPath();
        this.canvas.drawPath(graphicsPath, this.paint);
        if (isDashed) {
            this.paint.setPathEffect(this.minorDash);
            this.paint.setColor(-16777216);
            this.paint.setXfermode(null);
            this.canvas.drawPath(graphicsPath, this.paint);
        }
    }

    public Path createPathObject() {
        return new AndroidPath();
    }
}
