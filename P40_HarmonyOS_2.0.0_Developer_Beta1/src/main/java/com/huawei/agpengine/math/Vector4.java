package com.huawei.agpengine.math;

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

    public Vector4(float[] data, int offset) {
        this(data[offset + OFFSET_X], data[offset + 1], data[offset + 2], data[offset + 3]);
    }

    public Vector4(float vectorX, float vectorY, float vectorZ, float vectorW) {
        this.dataX = vectorX;
        this.dataY = vectorY;
        this.dataZ = vectorZ;
        this.dataW = vectorW;
    }

    public static Vector4 add(Vector4 lhs, Vector4 rhs) {
        return new Vector4(lhs.dataX + rhs.dataX, lhs.dataY + rhs.dataY, lhs.dataZ + rhs.dataZ, lhs.dataW + rhs.dataW);
    }

    public static Vector4 add(Vector4 lhs, float rhs) {
        return new Vector4(lhs.dataX + rhs, lhs.dataY + rhs, lhs.dataZ + rhs, lhs.dataW + rhs);
    }

    public static Vector4 subtract(Vector4 lhs, Vector4 rhs) {
        return new Vector4(lhs.dataX - rhs.dataX, lhs.dataY - rhs.dataY, lhs.dataZ - rhs.dataZ, lhs.dataW - rhs.dataW);
    }

    public static Vector4 subtract(Vector4 lhs, float rhs) {
        return new Vector4(lhs.dataX - rhs, lhs.dataY - rhs, lhs.dataZ - rhs, lhs.dataW - rhs);
    }

    public static Vector4 multiply(Vector4 lhs, Vector4 rhs) {
        return new Vector4(lhs.dataX * rhs.dataX, lhs.dataY * rhs.dataY, lhs.dataZ * rhs.dataZ, lhs.dataW * rhs.dataW);
    }

    public static Vector4 multiply(Vector4 lhs, float rhs) {
        return new Vector4(lhs.dataX * rhs, lhs.dataY * rhs, lhs.dataZ * rhs, lhs.dataW * rhs);
    }

    public static Vector4 divide(Vector4 lhs, Vector4 rhs) {
        return new Vector4(lhs.dataX / rhs.dataX, lhs.dataY / rhs.dataY, lhs.dataZ / rhs.dataZ, lhs.dataW / rhs.dataW);
    }

    public static Vector4 divide(Vector4 lhs, float rhs) {
        return new Vector4(lhs.dataX / rhs, lhs.dataY / rhs, lhs.dataZ / rhs, lhs.dataW / rhs);
    }

    public static float distance(Vector4 lhs, Vector4 rhs) {
        return new Vector4(lhs.dataX - rhs.dataX, lhs.dataY - rhs.dataY, lhs.dataZ - rhs.dataZ, lhs.dataW - rhs.dataW).getLength();
    }

    public static float dot(Vector4 lhs, Vector4 rhs) {
        return (lhs.dataX * rhs.dataX) + (lhs.dataY * rhs.dataY) + (lhs.dataZ * rhs.dataZ) + (lhs.dataW * rhs.dataW);
    }

    public static Vector4 min(Vector4 lhs, Vector4 rhs) {
        return new Vector4(Math.min(lhs.dataX, rhs.dataX), Math.min(lhs.dataY, rhs.dataY), Math.min(lhs.dataZ, rhs.dataZ), Math.min(lhs.dataW, rhs.dataW));
    }

    public static Vector4 max(Vector4 lhs, Vector4 rhs) {
        return new Vector4(Math.max(lhs.dataX, rhs.dataX), Math.max(lhs.dataY, rhs.dataY), Math.max(lhs.dataZ, rhs.dataZ), Math.max(lhs.dataW, rhs.dataW));
    }

    public static float length(float vectorX, float vectorY, float vectorZ, float vectorW) {
        return (float) Math.sqrt((double) lengthSquared(vectorX, vectorY, vectorZ, vectorW));
    }

    public static float lengthSquared(float vectorX, float vectorY, float vectorZ, float vectorW) {
        return (vectorX * vectorX) + (vectorY * vectorY) + (vectorZ * vectorZ) + (vectorW * vectorW);
    }

    public static Vector4 normalize(float vectorX, float vectorY, float vectorZ, float vectorW) {
        float oneOverLen = 1.0f / length(vectorX, vectorY, vectorZ, vectorW);
        if (Float.isFinite(oneOverLen)) {
            return new Vector4(vectorX * oneOverLen, vectorY * oneOverLen, vectorZ * oneOverLen, vectorW * oneOverLen);
        }
        return ZERO;
    }

    public static Vector4 lerp(Vector4 lhs, Vector4 rhs, float value) {
        float f = lhs.dataX;
        float f2 = f + ((rhs.dataX - f) * value);
        float f3 = lhs.dataY;
        float f4 = f3 + ((rhs.dataY - f3) * value);
        float f5 = lhs.dataZ;
        float f6 = f5 + ((rhs.dataZ - f5) * value);
        float f7 = lhs.dataW;
        return new Vector4(f2, f4, f6, f7 + ((rhs.dataW - f7) * value));
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

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Vector4)) {
            return false;
        }
        Vector4 vec = (Vector4) other;
        if (Math.abs(this.dataX - vec.dataX) >= EPSILON || Math.abs(this.dataY - vec.dataY) >= EPSILON || Math.abs(this.dataZ - vec.dataZ) >= EPSILON || Math.abs(this.dataW - vec.dataW) >= EPSILON) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Arrays.hashCode(new float[]{this.dataX, this.dataY, this.dataZ, this.dataW});
    }

    public String toString() {
        return "[" + this.dataX + ", " + this.dataY + ", " + this.dataZ + ", " + this.dataW + "]";
    }
}
