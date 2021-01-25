package ohos.agp.render.render3d.math;

import java.util.Arrays;

public final class Vector2 {
    private static final float EPSILON = 1.0E-6f;
    public static final Vector2 UNIT_X = new Vector2(1.0f, 0.0f);
    public static final Vector2 UNIT_Y = new Vector2(0.0f, 1.0f);
    public static final Vector2 ZERO = new Vector2(0.0f, 0.0f);
    private final float dataX;
    private final float dataY;

    public static float lengthSquared(float f, float f2) {
        return (f * f) + (f2 * f2);
    }

    public Vector2(float[] fArr, int i) {
        this(fArr[i], fArr[i + 1]);
    }

    public Vector2(float f, float f2) {
        this.dataX = f;
        this.dataY = f2;
    }

    public static Vector2 add(Vector2 vector2, Vector2 vector22) {
        return new Vector2(vector2.dataX + vector22.dataX, vector2.dataY + vector22.dataY);
    }

    public static Vector2 add(Vector2 vector2, float f) {
        return new Vector2(vector2.dataX + f, vector2.dataY + f);
    }

    public static Vector2 subtract(Vector2 vector2, Vector2 vector22) {
        return new Vector2(vector2.dataX - vector22.dataX, vector2.dataY - vector22.dataY);
    }

    public static Vector2 subtract(Vector2 vector2, float f) {
        return new Vector2(vector2.dataX - f, vector2.dataY - f);
    }

    public static Vector2 multiply(Vector2 vector2, Vector2 vector22) {
        return new Vector2(vector2.dataX * vector22.dataX, vector2.dataY * vector22.dataY);
    }

    public static Vector2 multiply(Vector2 vector2, float f) {
        return new Vector2(vector2.dataX * f, vector2.dataY * f);
    }

    public static Vector2 divide(Vector2 vector2, Vector2 vector22) {
        return new Vector2(vector2.dataX / vector22.dataX, vector2.dataY / vector22.dataY);
    }

    public static Vector2 divide(Vector2 vector2, float f) {
        return new Vector2(vector2.dataX / f, vector2.dataY / f);
    }

    public static float distance(Vector2 vector2, Vector2 vector22) {
        return new Vector2(vector2.dataX - vector22.dataX, vector2.dataY - vector22.dataY).getLength();
    }

    public static float dot(Vector2 vector2, Vector2 vector22) {
        return (vector2.dataX * vector22.dataX) + (vector2.dataY * vector22.dataY);
    }

    public static Vector2 min(Vector2 vector2, Vector2 vector22) {
        return new Vector2(Math.min(vector2.dataX, vector22.dataX), Math.min(vector2.dataY, vector22.dataY));
    }

    public static Vector2 max(Vector2 vector2, Vector2 vector22) {
        return new Vector2(Math.max(vector2.dataX, vector22.dataX), Math.max(vector2.dataY, vector22.dataY));
    }

    public static float length(float f, float f2) {
        return (float) Math.sqrt((double) lengthSquared(f, f2));
    }

    public static Vector2 normalize(float f, float f2) {
        float length = 1.0f / length(f, f2);
        if (Float.isFinite(length)) {
            return new Vector2(f * length, f2 * length);
        }
        return ZERO;
    }

    public static Vector2 lerp(Vector2 vector2, Vector2 vector22, float f) {
        float f2 = vector2.dataX;
        float f3 = vector2.dataY;
        return new Vector2(f2 + ((vector22.dataX - f2) * f), f3 + ((vector22.dataY - f3) * f));
    }

    public float getX() {
        return this.dataX;
    }

    public float getY() {
        return this.dataY;
    }

    public float getLength() {
        return length(this.dataX, this.dataY);
    }

    public float getLengthSquared() {
        return lengthSquared(this.dataX, this.dataY);
    }

    public Vector2 getNormalized() {
        return normalize(this.dataX, this.dataY);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vector2)) {
            return false;
        }
        Vector2 vector2 = (Vector2) obj;
        return Math.abs(this.dataX - vector2.dataX) < 1.0E-6f && Math.abs(this.dataY - vector2.dataY) < 1.0E-6f;
    }

    public int hashCode() {
        return Arrays.hashCode(new float[]{this.dataX, this.dataY});
    }

    public String toString() {
        return "[" + this.dataX + ", " + this.dataY + "]";
    }
}
