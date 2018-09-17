package org.apache.harmony.dalvik;

public final class NativeTestTarget {
    public static native void emptyInternalStaticMethod();

    public static native void emptyJniStaticMethod0();

    public static native void emptyJniStaticMethod0_Critical();

    public static native void emptyJniStaticMethod0_Fast();

    public static native void emptyJniStaticMethod6(int i, int i2, int i3, int i4, int i5, int i6);

    public static native void emptyJniStaticMethod6L(String str, String[] strArr, int[][] iArr, Object obj, Object[] objArr, Object[][][][] objArr2);

    public static native void emptyJniStaticMethod6L_Fast(String str, String[] strArr, int[][] iArr, Object obj, Object[] objArr, Object[][][][] objArr2);

    public static native void emptyJniStaticMethod6_Critical(int i, int i2, int i3, int i4, int i5, int i6);

    public static native void emptyJniStaticMethod6_Fast(int i, int i2, int i3, int i4, int i5, int i6);

    public static native synchronized void emptyJniStaticSynchronizedMethod0();

    public native void emptyJniMethod0();

    public native void emptyJniMethod0_Fast();

    public native void emptyJniMethod6(int i, int i2, int i3, int i4, int i5, int i6);

    public native void emptyJniMethod6L(String str, String[] strArr, int[][] iArr, Object obj, Object[] objArr, Object[][][][] objArr2);

    public native void emptyJniMethod6L_Fast(String str, String[] strArr, int[][] iArr, Object obj, Object[] objArr, Object[][][][] objArr2);

    public native void emptyJniMethod6_Fast(int i, int i2, int i3, int i4, int i5, int i6);

    public native synchronized void emptyJniSynchronizedMethod0();

    public static void emptyInlineMethod() {
    }
}
