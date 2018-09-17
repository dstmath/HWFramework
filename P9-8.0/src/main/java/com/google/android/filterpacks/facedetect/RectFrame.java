package com.google.android.filterpacks.facedetect;

import android.filterfw.core.NativeBuffer;

public class RectFrame extends NativeBuffer {
    private native float nativeGetHeight(int i);

    private native float nativeGetWidth(int i);

    private native float nativeGetX(int i);

    private native float nativeGetY(int i);

    private native boolean nativeSetHeight(int i, float f);

    private native boolean nativeSetWidth(int i, float f);

    private native boolean nativeSetX(int i, float f);

    private native boolean nativeSetY(int i, float f);

    public native int getElementSize();

    public RectFrame(int count) {
        super(count);
    }

    public float getX(int index) {
        assertReadable();
        return nativeGetX(index);
    }

    public float getY(int index) {
        assertReadable();
        return nativeGetY(index);
    }

    public float getWidth(int index) {
        assertReadable();
        return nativeGetWidth(index);
    }

    public float getHeight(int index) {
        assertReadable();
        return nativeGetHeight(index);
    }

    public void setX(int index, float value) {
        assertWritable();
        nativeSetX(index, value);
    }

    public void setY(int index, float value) {
        assertWritable();
        nativeSetY(index, value);
    }

    public void setWidth(int index, float value) {
        assertWritable();
        nativeSetWidth(index, value);
    }

    public void setHeight(int index, float value) {
        assertWritable();
        nativeSetHeight(index, value);
    }

    static {
        System.loadLibrary("filterpack_facedetect");
    }
}
