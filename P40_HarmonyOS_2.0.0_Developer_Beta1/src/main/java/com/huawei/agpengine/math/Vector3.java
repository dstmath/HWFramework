package com.huawei.agpengine.math;

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

    public Vector3(float[] data, int offset) {
        this(data[offset + OFFSET_X], data[offset + 1], data[offset + 2]);
    }

    public Vector3(float vectorX, float vectorY, float vectorZ) {
        this.dataX = vectorX;
        this.dataY = vectorY;
        this.dataZ = vectorZ;
    }

    public static Vector3 add(Vector3 lhs, Vector3 rhs) {
        return new Vector3(lhs.dataX + rhs.dataX, lhs.dataY + rhs.dataY, lhs.dataZ + rhs.dataZ);
    }

    public static Vector3 add(Vector3 lhs, float rhs) {
        return new Vector3(lhs.dataX + rhs, lhs.dataY + rhs, lhs.dataZ + rhs);
    }

    public static Vector3 subtract(Vector3 lhs, Vector3 rhs) {
        return new Vector3(lhs.dataX - rhs.dataX, lhs.dataY - rhs.dataY, lhs.dataZ - rhs.dataZ);
    }

    public static Vector3 subtract(Vector3 lhs, float rhs) {
        return new Vector3(lhs.dataX - rhs, lhs.dataY - rhs, lhs.dataZ - rhs);
    }

    public static Vector3 multiply(Vector3 lhs, Vector3 rhs) {
        return new Vector3(lhs.dataX * rhs.dataX, lhs.dataY * rhs.dataY, lhs.dataZ * rhs.dataZ);
    }

    public static Vector3 multiply(Vector3 lhs, float rhs) {
        return new Vector3(lhs.dataX * rhs, lhs.dataY * rhs, lhs.dataZ * rhs);
    }

    public static Vector3 divide(Vector3 lhs, Vector3 rhs) {
        return new Vector3(lhs.dataX / rhs.dataX, lhs.dataY / rhs.dataY, lhs.dataZ / rhs.dataZ);
    }

    public static Vector3 divide(Vector3 lhs, float rhs) {
        return new Vector3(lhs.dataX / rhs, lhs.dataY / rhs, lhs.dataZ / rhs);
    }

    public static float distance(Vector3 lhs, Vector3 rhs) {
        return new Vector3(lhs.dataX - rhs.dataX, lhs.dataY - rhs.dataY, lhs.dataZ - rhs.dataZ).getLength();
    }

    public static float dot(Vector3 lhs, Vector3 rhs) {
        return (lhs.dataX * rhs.dataX) + (lhs.dataY * rhs.dataY) + (lhs.dataZ * rhs.dataZ);
    }

    public static Vector3 cross(Vector3 lhs, Vector3 rhs) {
        float f = lhs.dataY;
        float f2 = rhs.dataZ;
        float f3 = lhs.dataZ;
        float f4 = rhs.dataY;
        float newX = (f * f2) - (f3 * f4);
        float f5 = rhs.dataX;
        float f6 = lhs.dataX;
        return new Vector3(newX, (f3 * f5) - (f2 * f6), (f6 * f4) - (f * f5));
    }

    public static Vector3 min(Vector3 lhs, Vector3 rhs) {
        return new Vector3(Math.min(lhs.dataX, rhs.dataX), Math.min(lhs.dataY, rhs.dataY), Math.min(lhs.dataZ, rhs.dataZ));
    }

    public static Vector3 max(Vector3 lhs, Vector3 rhs) {
        return new Vector3(Math.max(lhs.dataX, rhs.dataX), Math.max(lhs.dataY, rhs.dataY), Math.max(lhs.dataZ, rhs.dataZ));
    }

    public static float length(float vectorX, float vectorY, float vectorZ) {
        return (float) Math.sqrt((double) lengthSquared(vectorX, vectorY, vectorZ));
    }

    public static float lengthSquared(float vectorX, float vectorY, float vectorZ) {
        return (vectorX * vectorX) + (vectorY * vectorY) + (vectorZ * vectorZ);
    }

    public static Vector3 normalize(float vectorX, float vectorY, float vectorZ) {
        float oneOverLen = 1.0f / length(vectorX, vectorY, vectorZ);
        if (Float.isFinite(oneOverLen)) {
            return new Vector3(vectorX * oneOverLen, vectorY * oneOverLen, vectorZ * oneOverLen);
        }
        return ZERO;
    }

    public static Vector3 lerp(Vector3 lhs, Vector3 rhs, float value) {
        float f = lhs.dataX;
        float f2 = f + ((rhs.dataX - f) * value);
        float f3 = lhs.dataY;
        float f4 = f3 + ((rhs.dataY - f3) * value);
        float f5 = lhs.dataZ;
        return new Vector3(f2, f4, f5 + ((rhs.dataZ - f5) * value));
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

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Vector3)) {
            return false;
        }
        Vector3 vec = (Vector3) other;
        if (Math.abs(this.dataX - vec.dataX) >= EPSILON || Math.abs(this.dataY - vec.dataY) >= EPSILON || Math.abs(this.dataZ - vec.dataZ) >= EPSILON) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Arrays.hashCode(new float[]{this.dataX, this.dataY, this.dataZ});
    }

    public String toString() {
        return "[" + this.dataX + ", " + this.dataY + ", " + this.dataZ + "]";
    }
}
