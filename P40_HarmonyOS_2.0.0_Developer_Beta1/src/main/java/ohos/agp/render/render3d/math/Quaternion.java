package ohos.agp.render.render3d.math;

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

    public static float lengthSquared(float f, float f2, float f3, float f4) {
        return (f * f) + (f2 * f2) + (f3 * f3) + (f4 * f4);
    }

    public Quaternion(float[] fArr, int i) {
        this(fArr[i + 0], fArr[i + 1], fArr[i + 2], fArr[i + 3]);
    }

    public Quaternion(float f, float f2, float f3, float f4) {
        this.dataX = f;
        this.dataY = f2;
        this.dataZ = f3;
        this.dataW = f4;
    }

    public static Quaternion add(Quaternion quaternion, Quaternion quaternion2) {
        return new Quaternion(quaternion.dataX + quaternion2.dataX, quaternion.dataY + quaternion2.dataY, quaternion.dataZ + quaternion2.dataZ, quaternion.dataW + quaternion2.dataW);
    }

    public static Quaternion add(Quaternion quaternion, float f) {
        return new Quaternion(quaternion.dataX + f, quaternion.dataY + f, quaternion.dataZ + f, quaternion.dataW + f);
    }

    public static Quaternion subtract(Quaternion quaternion, Quaternion quaternion2) {
        return new Quaternion(quaternion.dataX - quaternion2.dataX, quaternion.dataY - quaternion2.dataY, quaternion.dataZ - quaternion2.dataZ, quaternion.dataW - quaternion2.dataW);
    }

    public static Quaternion subtract(Quaternion quaternion, float f) {
        return new Quaternion(quaternion.dataX - f, quaternion.dataY - f, quaternion.dataZ - f, quaternion.dataW - f);
    }

    public static Quaternion multiply(Quaternion quaternion, Quaternion quaternion2) {
        float f = quaternion.dataW;
        float f2 = quaternion2.dataX;
        float f3 = quaternion.dataX;
        float f4 = quaternion2.dataW;
        float f5 = (f * f2) + (f3 * f4);
        float f6 = quaternion.dataY;
        float f7 = quaternion2.dataZ;
        float f8 = quaternion.dataZ;
        float f9 = quaternion2.dataY;
        return new Quaternion((f5 + (f6 * f7)) - (f8 * f9), (((f * f9) + (f6 * f4)) + (f8 * f2)) - (f3 * f7), (((f * f7) + (f8 * f4)) + (f3 * f9)) - (f6 * f2), (((f * f4) - (f3 * f2)) - (f6 * f9)) - (f8 * f7));
    }

    public static Quaternion multiply(Quaternion quaternion, float f) {
        return new Quaternion(quaternion.dataX * f, quaternion.dataY * f, quaternion.dataZ * f, quaternion.dataW * f);
    }

    public static float dot(Quaternion quaternion, Quaternion quaternion2) {
        return (quaternion.dataX * quaternion2.dataX) + (quaternion.dataY * quaternion2.dataY) + (quaternion.dataZ * quaternion2.dataZ) + (quaternion.dataW * quaternion2.dataW);
    }

    public static Quaternion angleAxis(float f, Vector3 vector3) {
        double d = ((double) f) * 0.5d;
        Vector3 multiply = Vector3.multiply(vector3, (float) Math.sin(d));
        return new Quaternion(multiply.getX(), multiply.getY(), multiply.getZ(), (float) Math.cos(d));
    }

    public static float length(float f, float f2, float f3, float f4) {
        return (float) Math.sqrt((double) lengthSquared(f, f2, f3, f4));
    }

    public static Quaternion normalize(float f, float f2, float f3, float f4) {
        float length = 1.0f / length(f, f2, f3, f4);
        if (Float.isFinite(length)) {
            return new Quaternion(f * length, f2 * length, f3 * length, f4 * length);
        }
        return IDENTITY;
    }

    public static Quaternion conjugate(float f, float f2, float f3, float f4) {
        return new Quaternion(-f, -f2, -f3, f4);
    }

    public static Quaternion slerp(Quaternion quaternion, Quaternion quaternion2, float f) {
        Quaternion normalized = quaternion.getNormalized();
        Quaternion normalized2 = quaternion2.getNormalized();
        float dot = dot(normalized, normalized2);
        if (dot < 0.0f) {
            normalized2 = multiply(normalized2, -1.0f);
            dot = -dot;
        }
        if (dot > SLERP_DOT_THRESHOLD) {
            return add(normalized, multiply(subtract(normalized2, normalized), f)).getNormalized();
        }
        double d = (double) dot;
        double acos = Math.acos(d);
        double d2 = ((double) f) * acos;
        double sin = Math.sin(d2);
        double sin2 = Math.sin(acos);
        return add(multiply(normalized, (float) (Math.cos(d2) - ((d * sin) / sin2))), multiply(normalized2, (float) (sin / sin2)));
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

    public Vector3 rotatePoint(Vector3 vector3) {
        Vector3 vector32 = new Vector3(getX(), getY(), getZ());
        Vector3 cross = Vector3.cross(vector32, vector3);
        return Vector3.add(vector3, Vector3.multiply(Vector3.add(Vector3.multiply(cross, getW()), Vector3.cross(vector32, cross)), 2.0f));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Quaternion)) {
            return false;
        }
        Quaternion quaternion = (Quaternion) obj;
        return Math.abs(this.dataX - quaternion.dataX) < 1.0E-6f && Math.abs(this.dataY - quaternion.dataY) < 1.0E-6f && Math.abs(this.dataZ - quaternion.dataZ) < 1.0E-6f && Math.abs(this.dataW - quaternion.dataW) < 1.0E-6f;
    }

    public int hashCode() {
        return Arrays.hashCode(new float[]{this.dataX, this.dataY, this.dataZ, this.dataW});
    }

    public String toString() {
        return "[" + this.dataX + ", " + this.dataY + ", " + this.dataZ + ", " + this.dataW + "]";
    }
}
