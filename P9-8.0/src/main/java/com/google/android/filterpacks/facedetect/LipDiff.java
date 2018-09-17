package com.google.android.filterpacks.facedetect;

import android.filterfw.core.NativeBuffer;

public class LipDiff extends NativeBuffer {
    private native float nativeGetDirectionDiff(int i);

    private native int nativeGetFaceId(int i);

    private native float nativeGetHorizontalDiff(int i);

    private native float nativeGetTotalDiff(int i);

    private native float nativeGetVerticalDiff(int i);

    private native boolean nativeSetDirectionDiff(int i, float f);

    private native boolean nativeSetFaceId(int i, int i2);

    private native boolean nativeSetHorizontalDiff(int i, float f);

    private native boolean nativeSetTotalDiff(int i, float f);

    private native boolean nativeSetVerticalDiff(int i, float f);

    public native int getElementSize();

    public LipDiff(int count) {
        super(count);
    }

    public int getFaceId(int index) {
        assertReadable();
        return nativeGetFaceId(index);
    }

    public float getTotalDiff(int index) {
        assertReadable();
        return nativeGetTotalDiff(index);
    }

    public float getDirectionDiff(int index) {
        assertReadable();
        return nativeGetDirectionDiff(index);
    }

    public float getHorizontalDiff(int index) {
        assertReadable();
        return nativeGetHorizontalDiff(index);
    }

    public float getVerticalDiff(int index) {
        assertReadable();
        return nativeGetVerticalDiff(index);
    }

    public void setFaceId(int index, int value) {
        assertWritable();
        nativeSetFaceId(index, value);
    }

    public void setTotalDiff(int index, float value) {
        assertWritable();
        nativeSetTotalDiff(index, value);
    }

    public void setDirectionDiff(int index, float value) {
        assertWritable();
        nativeSetDirectionDiff(index, value);
    }

    public void setHorizontalDiff(int index, float value) {
        assertWritable();
        nativeSetHorizontalDiff(index, value);
    }

    public void setVerticalDiff(int index, float value) {
        assertWritable();
        nativeSetVerticalDiff(index, value);
    }

    static {
        System.loadLibrary("filterpack_facedetect");
    }
}
