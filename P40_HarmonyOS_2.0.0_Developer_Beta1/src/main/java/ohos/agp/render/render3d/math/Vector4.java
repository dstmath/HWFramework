package ohos.agp.render.render3d.math;

import java.util.Arrays;

public final class Vector4 {
    private static final float EPSILON = 1.0E-6f;
    private static final int OFFSET_W = 3;
    private static final int OFFSET_X = 0;
    private static final int OFFSET_Y = 1;
    private static final int OFFSET_Z = 2;
    public static final Vector4 UNIT_W = new Vector4(0.0f, 0.0f, 0.0f, 1.0f);
    public static final Vector4 UNIT_X = new Vector4(1.0f, 0.0f, 0.0f, 0.0f);
    public static final Vector4 UNIT_Y = new Vector4(0.0f, 1.0f, 0.0f, 0.0f);
    public static final Vector4 UNIT_Z = new Vector4(0.0f, 0.0f, 1.0f, 0.0f);
    public static final Vector4 ZERO = new Vector4(0.0f, 0.0f, 0.0f, 0.0f);
    private final float dataW;
    private final float dataX;
    private final float dataY;
    private final float dataZ;

    public static float lengthSquared(float f, float f2, float f3, float f4) {
        return (f * f) + (f2 * f2) + (f3 * f3) + (f4 * f4);
    }

    public Vector4(float[] fArr, int i) {
        this(fArr[i + 0], fArr[i + 1], fArr[i + 2], fArr[i + 3]);
    }

    public Vector4(float f, float f2, float f3, float f4) {
        this.dataX = f;
        this.dataY = f2;
        this.dataZ = f3;
        this.dataW = f4;
    }

    public static Vector4 add(Vector4 vector4, Vector4 vector42) {
        return new Vector4(vector4.dataX + vector42.dataX, vector4.dataY + vector42.dataY, vector4.dataZ + vector42.dataZ, vector4.dataW + vector42.dataW);
    }

    public static Vector4 add(Vector4 vector4, float f) {
        return new Vector4(vector4.dataX + f, vector4.dataY + f, vector4.dataZ + f, vector4.dataW + f);
    }

    public static Vector4 subtract(Vector4 vector4, Vector4 vector42) {
        return new Vector4(vector4.dataX - vector42.dataX, vector4.dataY - vector42.dataY, vector4.dataZ - vector42.dataZ, vector4.dataW - vector42.dataW);
    }

    public static Vector4 subtract(Vector4 vector4, float f) {
        return new Vector4(vector4.dataX - f, vector4.dataY - f, vector4.dataZ - f, vector4.dataW - f);
    }

    public static Vector4 multiply(Vector4 vector4, Vector4 vector42) {
        return new Vector4(vector4.dataX * vector42.dataX, vector4.dataY * vector42.dataY, vector4.dataZ * vector42.dataZ, vector4.dataW * vector42.dataW);
    }

    public static Vector4 multiply(Vector4 vector4, float f) {
        return new Vector4(vector4.dataX * f, vector4.dataY * f, vector4.dataZ * f, vector4.dataW * f);
    }

    public static Vector4 divide(Vector4 vector4, Vector4 vector42) {
        return new Vector4(vector4.dataX / vector42.dataX, vector4.dataY / vector42.dataY, vector4.dataZ / vector42.dataZ, vector4.dataW / vector42.dataW);
    }

    public static Vector4 divide(Vector4 vector4, float f) {
        return new Vector4(vector4.dataX / f, vector4.dataY / f, vector4.dataZ / f, vector4.dataW / f);
    }

    public static float distance(Vector4 vector4, Vector4 vector42) {
        return new Vector4(vector4.dataX - vector42.dataX, vector4.dataY - vector42.dataY, vector4.dataZ - vector42.dataZ, vector4.dataW - vector42.dataW).getLength();
    }

    public static float dot(Vector4 vector4, Vector4 vector42) {
        return (vector4.dataX * vector42.dataX) + (vector4.dataY * vector42.dataY) + (vector4.dataZ * vector42.dataZ) + (vector4.dataW * vector42.dataW);
    }

    public static Vector4 min(Vector4 vector4, Vector4 vector42) {
        return new Vector4(Math.min(vector4.dataX, vector42.dataX), Math.min(vector4.dataY, vector42.dataY), Math.min(vector4.dataZ, vector42.dataZ), Math.min(vector4.dataW, vector42.dataW));
    }

    public static Vector4 max(Vector4 vector4, Vector4 vector42) {
        return new Vector4(Math.max(vector4.dataX, vector42.dataX), Math.max(vector4.dataY, vector42.dataY), Math.max(vector4.dataZ, vector42.dataZ), Math.max(vector4.dataW, vector42.dataW));
    }

    public static float length(float f, float f2, float f3, float f4) {
        return (float) Math.sqrt((double) lengthSquared(f, f2, f3, f4));
    }

    public static Vector4 normalize(float f, float f2, float f3, float f4) {
        float length = 1.0f / length(f, f2, f3, f4);
        if (Float.isFinite(length)) {
            return new Vector4(f * length, f2 * length, f3 * length, f4 * length);
        }
        return ZERO;
    }

    public static Vector4 lerp(Vector4 vector4, Vector4 vector42, float f) {
        float f2 = vector4.dataX;
        float f3 = f2 + ((vector42.dataX - f2) * f);
        float f4 = vector4.dataY;
        float f5 = f4 + ((vector42.dataY - f4) * f);
        float f6 = vector4.dataZ;
        float f7 = vector4.dataW;
        return new Vector4(f3, f5, f6 + ((vector42.dataZ - f6) * f), f7 + ((vector42.dataW - f7) * f));
    }

    public float getX() {
        return this.dataX;
    }

    public float getY() {
        return this.dataY;
    }

    public float getZ() {
        return this.dataZ;
    }

    public float getW() {
        return this.dataW;
    }

    public float getLength() {
        return length(this.dataX, this.dataY, this.dataZ, this.dataW);
    }

    public float getLengthSquared() {
        return lengthSquared(this.dataX, this.dataY, this.dataZ, this.dataW);
    }

    public Vector4 getNormalized() {
        return normalize(this.dataX, this.dataY, this.dataZ, this.dataW);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vector4)) {
            return false;
        }
        Vector4 vector4 = (Vector4) obj;
        return Math.abs(this.dataX - vector4.dataX) < 1.0E-6f && Math.abs(this.dataY - vector4.dataY) < 1.0E-6f && Math.abs(this.dataZ - vector4.dataZ) < 1.0E-6f && Math.abs(this.dataW - vector4.dataW) < 1.0E-6f;
    }

    public int hashCode() {
        return Arrays.hashCode(new float[]{this.dataX, this.dataY, this.dataZ, this.dataW});
    }

    public String toString() {
        return "[" + this.dataX + ", " + this.dataY + ", " + this.dataZ + ", " + this.dataW + "]";
    }
}
