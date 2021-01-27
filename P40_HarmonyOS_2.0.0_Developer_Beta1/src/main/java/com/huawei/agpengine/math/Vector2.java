package com.huawei.agpengine.math;

import java.util.Arrays;

public final class Vector2 {
    private static final float EPSILON = 1.0E-6f;
    public static final Vector2 UNIT_X = new Vector2(1.0f, 0.0f);
    public static final Vector2 UNIT_Y = new Vector2(0.0f, 1.0f);
    public static final Vector2 ZERO = new Vector2(0.0f, 0.0f);
    private final float dataX;
    private final float dataY;

    public Vector2(float[] data, int offset) {
        this(data[offset], data[offset + 1]);
    }

    public Vector2(float vectorX, float vectorY) {
        this.dataX = vectorX;
        this.dataY = vectorY;
    }

    public static Vector2 add(Vector2 lhs, Vector2 rhs) {
        return new Vector2(lhs.dataX + rhs.dataX, lhs.dataY + rhs.dataY);
    }

    public static Vector2 add(Vector2 lhs, float rhs) {
        return new Vector2(lhs.dataX + rhs, lhs.dataY + rhs);
    }

    public static Vector2 subtract(Vector2 lhs, Vector2 rhs) {
        return new Vector2(lhs.dataX - rhs.dataX, lhs.dataY - rhs.dataY);
    }

    public static Vector2 subtract(Vector2 lhs, float rhs) {
        return new Vector2(lhs.dataX - rhs, lhs.dataY - rhs);
    }

    public static Vector2 multiply(Vector2 lhs, Vector2 rhs) {
        return new Vector2(lhs.dataX * rhs.dataX, lhs.dataY * rhs.dataY);
    }

    public static Vector2 multiply(Vector2 lhs, float rhs) {
        return new Vector2(lhs.dataX * rhs, lhs.dataY * rhs);
    }

    public static Vector2 divide(Vector2 lhs, Vector2 rhs) {
        return new Vector2(lhs.dataX / rhs.dataX, lhs.dataY / rhs.dataY);
    }

    public static Vector2 divide(Vector2 lhs, float rhs) {
        return new Vector2(lhs.dataX / rhs, lhs.dataY / rhs);
    }

    public static float distance(Vector2 lhs, Vector2 rhs) {
        return new Vector2(lhs.dataX - rhs.dataX, lhs.dataY - rhs.dataY).getLength();
    }

    public static float dot(Vector2 lhs, Vector2 rhs) {
        return (lhs.dataX * rhs.dataX) + (lhs.dataY * rhs.dataY);
    }

    public static Vector2 min(Vector2 lhs, Vector2 rhs) {
        return new Vector2(Math.min(lhs.dataX, rhs.dataX), Math.min(lhs.dataY, rhs.dataY));
    }

    public static Vector2 max(Vector2 lhs, Vector2 rhs) {
        return new Vector2(Math.max(lhs.dataX, rhs.dataX), Math.max(lhs.dataY, rhs.dataY));
    }

    public static float length(float vectorX, float vectorY) {
        return (float) Math.sqrt((double) lengthSquared(vectorX, vectorY));
    }

    public static float lengthSquared(float vectorX, float vectorY) {
        return (vectorX * vectorX) + (vectorY * vectorY);
    }

    public static Vector2 normalize(float vectorX, float vectorY) {
        float oneOverLen = 1.0f / length(vectorX, vectorY);
        if (Float.isFinite(oneOverLen)) {
            return new Vector2(vectorX * oneOverLen, vectorY * oneOverLen);
        }
        return ZERO;
    }

    public static Vector2 lerp(Vector2 lhs, Vector2 rhs, float value) {
        float f = lhs.dataX;
        float f2 = f + ((rhs.dataX - f) * value);
        float f3 = lhs.dataY;
        return new Vector2(f2, f3 + ((rhs.dataY - f3) * value));
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

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Vector2)) {
            return false;
        }
        Vector2 vec = (Vector2) other;
        if (Math.abs(this.dataX - vec.dataX) >= EPSILON || Math.abs(this.dataY - vec.dataY) >= EPSILON) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Arrays.hashCode(new float[]{this.dataX, this.dataY});
    }

    public String toString() {
        return "[" + this.dataX + ", " + this.dataY + "]";
    }
}
