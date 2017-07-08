package android.gesture;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class GestureStroke {
    static final float TOUCH_TOLERANCE = 3.0f;
    public final RectF boundingBox;
    public final float length;
    private Path mCachedPath;
    public final float[] points;
    private final long[] timestamps;

    public GestureStroke(ArrayList<GesturePoint> points) {
        int count = points.size();
        float[] tmpPoints = new float[(count * 2)];
        long[] times = new long[count];
        RectF bx = null;
        float len = 0.0f;
        int index = 0;
        for (int i = 0; i < count; i++) {
            GesturePoint p = (GesturePoint) points.get(i);
            tmpPoints[i * 2] = p.x;
            tmpPoints[(i * 2) + 1] = p.y;
            times[index] = p.timestamp;
            if (bx == null) {
                bx = new RectF();
                bx.top = p.y;
                bx.left = p.x;
                bx.right = p.x;
                bx.bottom = p.y;
                len = 0.0f;
            } else {
                len = (float) (((double) len) + Math.hypot((double) (p.x - tmpPoints[(i - 1) * 2]), (double) (p.y - tmpPoints[((i - 1) * 2) + 1])));
                bx.union(p.x, p.y);
            }
            index++;
        }
        this.timestamps = times;
        this.points = tmpPoints;
        this.boundingBox = bx;
        this.length = len;
    }

    private GestureStroke(RectF bbx, float len, float[] pts, long[] times) {
        this.boundingBox = new RectF(bbx.left, bbx.top, bbx.right, bbx.bottom);
        this.length = len;
        this.points = (float[]) pts.clone();
        this.timestamps = (long[]) times.clone();
    }

    public Object clone() {
        return new GestureStroke(this.boundingBox, this.length, this.points, this.timestamps);
    }

    void draw(Canvas canvas, Paint paint) {
        if (this.mCachedPath == null) {
            makePath();
        }
        canvas.drawPath(this.mCachedPath, paint);
    }

    public Path getPath() {
        if (this.mCachedPath == null) {
            makePath();
        }
        return this.mCachedPath;
    }

    private void makePath() {
        float[] localPoints = this.points;
        int count = localPoints.length;
        Path path = null;
        float mX = 0.0f;
        float mY = 0.0f;
        for (int i = 0; i < count; i += 2) {
            float x = localPoints[i];
            float y = localPoints[i + 1];
            if (path == null) {
                path = new Path();
                path.moveTo(x, y);
                mX = x;
                mY = y;
            } else {
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.quadTo(mX, mY, (x + mX) / 2.0f, (y + mY) / 2.0f);
                    mX = x;
                    mY = y;
                }
            }
        }
        this.mCachedPath = path;
    }

    public Path toPath(float width, float height, int numSample) {
        float scale;
        float[] pts = GestureUtils.temporalSampling(this, numSample);
        RectF rect = this.boundingBox;
        GestureUtils.translate(pts, -rect.left, -rect.top);
        float sx = width / rect.width();
        float sy = height / rect.height();
        if (sx > sy) {
            scale = sy;
        } else {
            scale = sx;
        }
        GestureUtils.scale(pts, scale, scale);
        float mX = 0.0f;
        float mY = 0.0f;
        Path path = null;
        int count = pts.length;
        for (int i = 0; i < count; i += 2) {
            float x = pts[i];
            float y = pts[i + 1];
            if (path == null) {
                path = new Path();
                path.moveTo(x, y);
                mX = x;
                mY = y;
            } else {
                float dx = Math.abs(x - mX);
                float dy = Math.abs(y - mY);
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    path.quadTo(mX, mY, (x + mX) / 2.0f, (y + mY) / 2.0f);
                    mX = x;
                    mY = y;
                }
            }
        }
        return path;
    }

    void serialize(DataOutputStream out) throws IOException {
        float[] pts = this.points;
        long[] times = this.timestamps;
        int count = this.points.length;
        out.writeInt(count / 2);
        for (int i = 0; i < count; i += 2) {
            out.writeFloat(pts[i]);
            out.writeFloat(pts[i + 1]);
            out.writeLong(times[i / 2]);
        }
    }

    static GestureStroke deserialize(DataInputStream in) throws IOException {
        int count = in.readInt();
        ArrayList<GesturePoint> points = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            points.add(GesturePoint.deserialize(in));
        }
        return new GestureStroke(points);
    }

    public void clearPath() {
        if (this.mCachedPath != null) {
            this.mCachedPath.rewind();
        }
    }

    public OrientedBoundingBox computeOrientedBoundingBox() {
        return GestureUtils.computeOrientedBoundingBox(this.points);
    }
}
