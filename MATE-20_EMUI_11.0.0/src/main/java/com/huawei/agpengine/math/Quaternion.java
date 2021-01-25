package com.huawei.agpengine.math;

import java.util.Arrays;

public class Quaternion {
    private static final float EPSILON = 1.0E-6f;
    public static final Quaternion IDENTITY = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
    private static final int OFFSET_W = 3;
    private static final int OFFSET_X = 0;
    private static final int OFFSET_Y = 1;
    private static final int OFFSET_Z = 2;
    private static final float RADIANS_HALF = 0.5f;
    private static final float SLERP_DOT_THRESHOLD = 0.9995f;
    public static final Quaternion ZERO = new Quaternion(0.0f, 0.0f, 0.0f, 0.0f);
    private final float dataW;
    private final float dataX;
    private final float dataY;
    private final float dataZ;

    public Quaternion(float[] data, int offset) {
        this(data[offset + OFFSET_X], data[offset + 1], data[offset + 2], data[offset + 3]);
    }

    public Quaternion(float quatX, float quatY, float quatZ, float quatW) {
        this.dataX = quatX;
        this.dataY = quatY;
        this.dataZ = quatZ;
        this.dataW = quatW;
    }

    public static Quaternion add(Quaternion lhs, Quaternion rhs) {
        return new Quaternion(lhs.dataX + rhs.dataX, lhs.dataY + rhs.dataY, lhs.dataZ + rhs.dataZ, lhs.dataW + rhs.dataW);
    }

    public static Quaternion add(Quaternion lhs, float rhs) {
        return new Quaternion(lhs.dataX + rhs, lhs.dataY + rhs, lhs.dataZ + rhs, lhs.dataW + rhs);
    }

    public static Quaternion subtract(Quaternion lhs, Quaternion rhs) {
        return new Quaternion(lhs.dataX - rhs.dataX, lhs.dataY - rhs.dataY, lhs.dataZ - rhs.dataZ, lhs.dataW - rhs.dataW);
    }

    public static Quaternion subtract(Quaternion lhs, float rhs) {
        return new Quaternion(lhs.dataX - rhs, lhs.dataY - rhs, lhs.dataZ - rhs, lhs.dataW - rhs);
    }

    public static Quaternion multiply(Quaternion lhs, Quaternion rhs) {
        float f = lhs.dataW;
        float f2 = rhs.dataX;
        float f3 = lhs.dataX;
        float f4 = rhs.dataW;
        float f5 = lhs.dataY;
        float f6 = rhs.dataZ;
        float f7 = (f * f2) + (f3 * f4) + (f5 * f6);
        float f8 = lhs.dataZ;
        float f9 = rhs.dataY;
        return new Quaternion(f7 - (f8 * f9), (((f * f9) + (f5 * f4)) + (f8 * f2)) - (f3 * f6), (((f * f6) + (f8 * f4)) + (f3 * f9)) - (f5 * f2), (((f * f4) - (f3 * f2)) - (f5 * f9)) - (f8 * f6));
    }

    public static Quaternion multiply(Quaternion lhs, float rhs) {
        return new Quaternion(lhs.dataX * rhs, lhs.dataY * rhs, lhs.dataZ * rhs, lhs.dataW * rhs);
    }

    public static float dot(Quaternion lhs, Quaternion rhs) {
        return (lhs.dataX * rhs.dataX) + (lhs.dataY * rhs.dataY) + (lhs.dataZ * rhs.dataZ) + (lhs.dataW * rhs.dataW);
    }

    public static Quaternion angleAxis(float angleRadians, Vector3 axis) {
        double halfAngleRadians = ((double) angleRadians) * 0.5d;
        Vector3 axisMultiplied = Vector3.multiply(axis, (float) Math.sin(halfAngleRadians));
        return new Quaternion(axisMultiplied.getX(), axisMultiplied.getY(), axisMultiplied.getZ(), (float) Math.cos(halfAngleRadians));
    }

    public static float length(float quatX, float quatY, float quatZ, float quatW) {
        return (float) Math.sqrt((double) lengthSquared(quatX, quatY, quatZ, quatW));
    }

    public static float lengthSquared(float quatX, float quatY, float quatZ, float quatW) {
        return (quatX * quatX) + (quatY * quatY) + (quatZ * quatZ) + (quatW * quatW);
    }

    public static Quaternion normalize(float quatX, float quatY, float quatZ, float quatW) {
        float oneOverLen = 1.0f / length(quatX, quatY, quatZ, quatW);
        if (Float.isFinite(oneOverLen)) {
            return new Quaternion(quatX * oneOverLen, quatY * oneOverLen, quatZ * oneOverLen, quatW * oneOverLen);
        }
        return IDENTITY;
    }

    public static Quaternion conjugate(float quatX, float quatY, float quatZ, float quatW) {
        return new Quaternion(-quatX, -quatY, -quatZ, quatW);
    }

    public static Quaternion slerp(Quaternion lhs, Quaternion rhs, float value) {
        Quaternion q0 = lhs.getNormalized();
        Quaternion q1 = rhs.getNormalized();
        float dot = dot(q0, q1);
        if (dot < 0.0f) {
            q1 = multiply(q1, -1.0f);
            dot = -dot;
        }
        if (dot > SLERP_DOT_THRESHOLD) {
            return add(q0, multiply(subtract(q1, q0), value)).getNormalized();
        }
        double theta0 = Math.acos((double) dot);
        double theta = ((double) value) * theta0;
        double sinTheta = Math.sin(theta);
        double sinTheta0 = Math.sin(theta0);
        return add(multiply(q0, (float) (Math.cos(theta) - ((((double) dot) * sinTheta) / sinTheta0))), multiply(q1, (float) (sinTheta / sinTheta0)));
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

    public Quaternion getNormalized() {
        return normalize(this.dataX, this.dataY, this.dataZ, this.dataW);
    }

    public Quaternion getConjugate() {
        return conjugate(this.dataX, this.dataY, this.dataZ, this.dataW);
    }

    public Vector3 rotatePoint(Vector3 vector) {
        Vector3 quatVector = new Vector3(getX(), getY(), getZ());
        Vector3 uv = Vector3.cross(quatVector, vector);
        return Vector3.add(vector, Vector3.multiply(Vector3.add(Vector3.multiply(uv, getW()), Vector3.cross(quatVector, uv)), 2.0f));
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Quaternion)) {
            return false;
        }
        Quaternion quat = (Quaternion) other;
        if (Math.abs(this.dataX - quat.dataX) >= EPSILON || Math.abs(this.dataY - quat.dataY) >= EPSILON || Math.abs(this.dataZ - quat.dataZ) >= EPSILON || Math.abs(this.dataW - quat.dataW) >= EPSILON) {
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
