package android.opengl;

public class Visibility {
    public static native void computeBoundingSphere(float[] fArr, int i, int i2, float[] fArr2, int i3);

    public static native int frustumCullSpheres(float[] fArr, int i, float[] fArr2, int i2, int i3, int[] iArr, int i4, int i5);

    public static native int visibilityTest(float[] fArr, int i, float[] fArr2, int i2, char[] cArr, int i3, int i4);
}
