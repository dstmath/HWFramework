package android.filterfw.geometry;

import android.annotation.UnsupportedAppUsage;

public class Point {
    @UnsupportedAppUsage
    public float x;
    @UnsupportedAppUsage
    public float y;

    @UnsupportedAppUsage
    public Point() {
    }

    @UnsupportedAppUsage
    public Point(float x2, float y2) {
        this.x = x2;
        this.y = y2;
    }

    public void set(float x2, float y2) {
        this.x = x2;
        this.y = y2;
    }

    public boolean IsInUnitRange() {
        float f = this.x;
        if (f >= 0.0f && f <= 1.0f) {
            float f2 = this.y;
            if (f2 >= 0.0f && f2 <= 1.0f) {
                return true;
            }
        }
        return false;
    }

    public Point plus(float x2, float y2) {
        return new Point(this.x + x2, this.y + y2);
    }

    public Point plus(Point point) {
        return plus(point.x, point.y);
    }

    public Point minus(float x2, float y2) {
        return new Point(this.x - x2, this.y - y2);
    }

    public Point minus(Point point) {
        return minus(point.x, point.y);
    }

    public Point times(float s) {
        return new Point(this.x * s, this.y * s);
    }

    public Point mult(float x2, float y2) {
        return new Point(this.x * x2, this.y * y2);
    }

    public float length() {
        return (float) Math.hypot((double) this.x, (double) this.y);
    }

    public float distanceTo(Point p) {
        return p.minus(this).length();
    }

    public Point scaledTo(float length) {
        return times(length / length());
    }

    public Point normalize() {
        return scaledTo(1.0f);
    }

    public Point rotated90(int count) {
        float nx = this.x;
        float ny = this.y;
        for (int i = 0; i < count; i++) {
            nx = ny;
            ny = -nx;
        }
        return new Point(nx, ny);
    }

    public Point rotated(float radians) {
        return new Point((float) ((Math.cos((double) radians) * ((double) this.x)) - (Math.sin((double) radians) * ((double) this.y))), (float) ((Math.sin((double) radians) * ((double) this.x)) + (Math.cos((double) radians) * ((double) this.y))));
    }

    public Point rotatedAround(Point center, float radians) {
        return minus(center).rotated(radians).plus(center);
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
