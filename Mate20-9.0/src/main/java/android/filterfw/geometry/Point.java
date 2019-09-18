package android.filterfw.geometry;

public class Point {
    public float x;
    public float y;

    public Point() {
    }

    public Point(float x2, float y2) {
        this.x = x2;
        this.y = y2;
    }

    public void set(float x2, float y2) {
        this.x = x2;
        this.y = y2;
    }

    public boolean IsInUnitRange() {
        return this.x >= 0.0f && this.x <= 1.0f && this.y >= 0.0f && this.y <= 1.0f;
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
            float ox = nx;
            nx = ny;
            ny = -ox;
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
