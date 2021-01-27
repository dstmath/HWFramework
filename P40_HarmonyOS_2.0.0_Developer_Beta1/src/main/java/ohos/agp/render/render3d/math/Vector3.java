package ohos.agp.render.render3d.math;

import java.util.Arrays;

public final class Vector3 {
    private static final float EPSILON = 1.0E-6f;
    private static final int OFFSET_X = 0;
    private static final int OFFSET_Y = 1;
    private static final int OFFSET_Z = 2;
    public static final Vector3 UNIT_X = new Vector3(1.0f, 0.0f, 0.0f);
    public static final Vector3 UNIT_Y = new Vector3(0.0f, 1.0f, 0.0f);
    public static final Vector3 UNIT_Z = new Vector3(0.0f, 0.0f, 1.0f);
    public static final Vector3 ZERO = new Vector3(0.0f, 0.0f, 0.0f);
    private final float dataX;
    private final float dataY;
    private final float dataZ;

    public static float lengthSquared(float f, float f2, float f3) {
        return (f * f) + (f2 * f2) + (f3 * f3);
    }

    public Vector3(float[] fArr, int i) {
        this(fArr[i + 0], fArr[i + 1], fArr[i + 2]);
    }

    public Vector3(float f, float f2, float f3) {
        this.dataX = f;
        this.dataY = f2;
        this.dataZ = f3;
    }

    public static Vector3 add(Vector3 vector3, Vector3 vector32) {
        return new Vector3(vector3.dataX + vector32.dataX, vector3.dataY + vector32.dataY, vector3.dataZ + vector32.dataZ);
    }

    public static Vector3 add(Vector3 vector3, float f) {
        return new Vector3(vector3.dataX + f, vector3.dataY + f, vector3.dataZ + f);
    }

    public static Vector3 subtract(Vector3 vector3, Vector3 vector32) {
        return new Vector3(vector3.dataX - vector32.dataX, vector3.dataY - vector32.dataY, vector3.dataZ - vector32.dataZ);
    }

    public static Vector3 subtract(Vector3 vector3, float f) {
        return new Vector3(vector3.dataX - f, vector3.dataY - f, vector3.dataZ - f);
    }

    public static Vector3 multiply(Vector3 vector3, Vector3 vector32) {
        return new Vector3(vector3.dataX * vector32.dataX, vector3.dataY * vector32.dataY, vector3.dataZ * vector32.dataZ);
    }

    public static Vector3 multiply(Vector3 vector3, float f) {
        return new Vector3(vector3.dataX * f, vector3.dataY * f, vector3.dataZ * f);
    }

    public static Vector3 divide(Vector3 vector3, Vector3 vector32) {
        return new Vector3(vector3.dataX / vector32.dataX, vector3.dataY / vector32.dataY, vector3.dataZ / vector32.dataZ);
    }

    public static Vector3 divide(Vector3 vector3, float f) {
        return new Vector3(vector3.dataX / f, vector3.dataY / f, vector3.dataZ / f);
    }

    public static float distance(Vector3 vector3, Vector3 vector32) {
        return new Vector3(vector3.dataX - vector32.dataX, vector3.dataY - vector32.dataY, vector3.dataZ - vector32.dataZ).getLength();
    }

    public static float dot(Vector3 vector3, Vector3 vector32) {
        return (vector3.dataX * vector32.dataX) + (vector3.dataY * vector32.dataY) + (vector3.dataZ * vector32.dataZ);
    }

    public static Vector3 cross(Vector3 vector3, Vector3 vector32) {
        float f = vector3.dataY;
        float f2 = vector32.dataZ;
        float f3 = vector3.dataZ;
        float f4 = vector32.dataY;
        float f5 = vector32.dataX;
        float f6 = vector3.dataX;
        return new Vector3((f * f2) - (f3 * f4), (f3 * f5) - (f2 * f6), (f6 * f4) - (f * f5));
    }

    public static Vector3 min(Vector3 vector3, Vector3 vector32) {
        return new Vector3(Math.min(vector3.dataX, vector32.dataX), Math.min(vector3.dataY, vector32.dataY), Math.min(vector3.dataZ, vector32.dataZ));
    }

    public static Vector3 max(Vector3 vector3, Vector3 vector32) {
        return new Vector3(Math.max(vector3.dataX, vector32.dataX), Math.max(vector3.dataY, vector32.dataY), Math.max(vector3.dataZ, vector32.dataZ));
    }

    public static float length(float f, float f2, float f3) {
        return (float) Math.sqrt((double) lengthSquared(f, f2, f3));
    }

    public static Vector3 normalize(float f, float f2, float f3) {
        float length = 1.0f / length(f, f2, f3);
        if (Float.isFinite(length)) {
            return new Vector3(f * length, f2 * length, f3 * length);
        }
        return ZERO;
    }

    public static Vector3 lerp(Vector3 vector3, Vector3 vector32, float f) {
        float f2 = vector3.dataX;
        float f3 = f2 + ((vector32.dataX - f2) * f);
        float f4 = vector3.dataY;
        float f5 = vector3.dataZ;
        return new Vector3(f3, f4 + ((vector32.dataY - f4) * f), f5 + ((vector32.dataZ - f5) * f));
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

    public float getLength() {
        return length(this.dataX, this.dataY, this.dataZ);
    }

    public float getLengthSquared() {
        return lengthSquared(this.dataX, this.dataY, this.dataZ);
    }

    public Vector3 getNormalized() {
        return normalize(this.dataX, this.dataY, this.dataZ);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vector3)) {
            return false;
        }
        Vector3 vector3 = (Vector3) obj;
        return Math.abs(this.dataX - vector3.dataX) < 1.0E-6f && Math.abs(this.dataY - vector3.dataY) < 1.0E-6f && Math.abs(this.dataZ - vector3.dataZ) < 1.0E-6f;
    }

    public int hashCode() {
        return Arrays.hashCode(new float[]{this.dataX, this.dataY, this.dataZ});
    }

    public String toString() {
        return "[" + this.dataX + ", " + this.dataY + ", " + this.dataZ + "]";
    }
}
