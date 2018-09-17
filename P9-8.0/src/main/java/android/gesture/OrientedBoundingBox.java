package android.gesture;

import android.graphics.Matrix;
import android.graphics.Path;

public class OrientedBoundingBox {
    public final float centerX;
    public final float centerY;
    public final float height;
    public final float orientation;
    public final float squareness;
    public final float width;

    OrientedBoundingBox(float angle, float cx, float cy, float w, float h) {
        this.orientation = angle;
        this.width = w;
        this.height = h;
        this.centerX = cx;
        this.centerY = cy;
        float ratio = w / h;
        if (ratio > 1.0f) {
            this.squareness = 1.0f / ratio;
        } else {
            this.squareness = ratio;
        }
    }

    public Path toPath() {
        Path path = new Path();
        float[] point = new float[]{(-this.width) / 2.0f, this.height / 2.0f};
        Matrix matrix = new Matrix();
        matrix.setRotate(this.orientation);
        matrix.postTranslate(this.centerX, this.centerY);
        matrix.mapPoints(point);
        path.moveTo(point[0], point[1]);
        point[0] = (-this.width) / 2.0f;
        point[1] = (-this.height) / 2.0f;
        matrix.mapPoints(point);
        path.lineTo(point[0], point[1]);
        point[0] = this.width / 2.0f;
        point[1] = (-this.height) / 2.0f;
        matrix.mapPoints(point);
        path.lineTo(point[0], point[1]);
        point[0] = this.width / 2.0f;
        point[1] = this.height / 2.0f;
        matrix.mapPoints(point);
        path.lineTo(point[0], point[1]);
        path.close();
        return path;
    }
}
