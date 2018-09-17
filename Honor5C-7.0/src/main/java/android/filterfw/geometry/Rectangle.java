package android.filterfw.geometry;

import android.hardware.SensorManager;

public class Rectangle extends Quad {
    public Rectangle(float x, float y, float width, float height) {
        super(new Point(x, y), new Point(x + width, y), new Point(x, y + height), new Point(x + width, y + height));
    }

    public Rectangle(Point origin, Point size) {
        super(origin, origin.plus(size.x, 0.0f), origin.plus(0.0f, size.y), origin.plus(size.x, size.y));
    }

    public static Rectangle fromRotatedRect(Point center, Point size, float rotation) {
        return new Rectangle(new Point(center.x - (size.x / 2.0f), center.y - (size.y / 2.0f)).rotatedAround(center, rotation), new Point(center.x + (size.x / 2.0f), center.y - (size.y / 2.0f)).rotatedAround(center, rotation), new Point(center.x - (size.x / 2.0f), center.y + (size.y / 2.0f)).rotatedAround(center, rotation), new Point(center.x + (size.x / 2.0f), center.y + (size.y / 2.0f)).rotatedAround(center, rotation));
    }

    private Rectangle(Point p0, Point p1, Point p2, Point p3) {
        super(p0, p1, p2, p3);
    }

    public static Rectangle fromCenterVerticalAxis(Point center, Point vAxis, Point size) {
        Point dy = vAxis.scaledTo(size.y / 2.0f);
        Point dx = vAxis.rotated90(1).scaledTo(size.x / 2.0f);
        return new Rectangle(center.minus(dx).minus(dy), center.plus(dx).minus(dy), center.minus(dx).plus(dy), center.plus(dx).plus(dy));
    }

    public float getWidth() {
        return this.p1.minus(this.p0).length();
    }

    public float getHeight() {
        return this.p2.minus(this.p0).length();
    }

    public Point center() {
        return this.p0.plus(this.p1).plus(this.p2).plus(this.p3).times(SensorManager.LIGHT_FULLMOON);
    }

    public Rectangle scaled(float s) {
        return new Rectangle(this.p0.times(s), this.p1.times(s), this.p2.times(s), this.p3.times(s));
    }

    public Rectangle scaled(float x, float y) {
        return new Rectangle(this.p0.mult(x, y), this.p1.mult(x, y), this.p2.mult(x, y), this.p3.mult(x, y));
    }
}
