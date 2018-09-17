package com.google.android.filterpacks.facedetect;

import android.filterfw.core.NativeBuffer;

public class FaceMeta extends NativeBuffer {
    private native float nativeGetConfidence(int i);

    private native float nativeGetFaceX0(int i);

    private native float nativeGetFaceX1(int i);

    private native float nativeGetFaceY0(int i);

    private native float nativeGetFaceY1(int i);

    private native int nativeGetId(int i);

    private native float nativeGetLeftEyeX(int i);

    private native float nativeGetLeftEyeY(int i);

    private native float nativeGetLowerLipX(int i);

    private native float nativeGetLowerLipY(int i);

    private native float nativeGetMouthLeftX(int i);

    private native float nativeGetMouthLeftY(int i);

    private native float nativeGetMouthRightX(int i);

    private native float nativeGetMouthRightY(int i);

    private native float nativeGetMouthX(int i);

    private native float nativeGetMouthY(int i);

    private native float nativeGetRightEyeX(int i);

    private native float nativeGetRightEyeY(int i);

    private native float nativeGetUpperLipX(int i);

    private native float nativeGetUpperLipY(int i);

    private native boolean nativeSetConfidence(int i, float f);

    private native boolean nativeSetFaceX0(int i, float f);

    private native boolean nativeSetFaceX1(int i, float f);

    private native boolean nativeSetFaceY0(int i, float f);

    private native boolean nativeSetFaceY1(int i, float f);

    private native boolean nativeSetId(int i, int i2);

    private native boolean nativeSetLeftEyeX(int i, float f);

    private native boolean nativeSetLeftEyeY(int i, float f);

    private native boolean nativeSetLowerLipX(int i, float f);

    private native boolean nativeSetLowerLipY(int i, float f);

    private native boolean nativeSetMouthLeftX(int i, float f);

    private native boolean nativeSetMouthLeftY(int i, float f);

    private native boolean nativeSetMouthRightX(int i, float f);

    private native boolean nativeSetMouthRightY(int i, float f);

    private native boolean nativeSetMouthX(int i, float f);

    private native boolean nativeSetMouthY(int i, float f);

    private native boolean nativeSetRightEyeX(int i, float f);

    private native boolean nativeSetRightEyeY(int i, float f);

    private native boolean nativeSetUpperLipX(int i, float f);

    private native boolean nativeSetUpperLipY(int i, float f);

    public native int getElementSize();

    public FaceMeta(int count) {
        super(count);
    }

    public int getId(int index) {
        assertReadable();
        return nativeGetId(index);
    }

    public float getFaceX0(int index) {
        assertReadable();
        return nativeGetFaceX0(index);
    }

    public float getFaceY0(int index) {
        assertReadable();
        return nativeGetFaceY0(index);
    }

    public float getFaceX1(int index) {
        assertReadable();
        return nativeGetFaceX1(index);
    }

    public float getFaceY1(int index) {
        assertReadable();
        return nativeGetFaceY1(index);
    }

    public float getLeftEyeX(int index) {
        assertReadable();
        return nativeGetLeftEyeX(index);
    }

    public float getLeftEyeY(int index) {
        assertReadable();
        return nativeGetLeftEyeY(index);
    }

    public float getRightEyeX(int index) {
        assertReadable();
        return nativeGetRightEyeX(index);
    }

    public float getRightEyeY(int index) {
        assertReadable();
        return nativeGetRightEyeY(index);
    }

    public float getLowerLipX(int index) {
        assertReadable();
        return nativeGetLowerLipX(index);
    }

    public float getLowerLipY(int index) {
        assertReadable();
        return nativeGetLowerLipY(index);
    }

    public float getUpperLipX(int index) {
        assertReadable();
        return nativeGetUpperLipX(index);
    }

    public float getUpperLipY(int index) {
        assertReadable();
        return nativeGetUpperLipY(index);
    }

    public float getMouthX(int index) {
        assertReadable();
        return nativeGetMouthX(index);
    }

    public float getMouthY(int index) {
        assertReadable();
        return nativeGetMouthY(index);
    }

    public float getMouthLeftX(int index) {
        assertReadable();
        return nativeGetMouthLeftX(index);
    }

    public float getMouthLeftY(int index) {
        assertReadable();
        return nativeGetMouthLeftY(index);
    }

    public float getMouthRightX(int index) {
        assertReadable();
        return nativeGetMouthRightX(index);
    }

    public float getMouthRightY(int index) {
        assertReadable();
        return nativeGetMouthRightY(index);
    }

    public float getConfidence(int index) {
        assertReadable();
        return nativeGetConfidence(index);
    }

    public void setId(int index, int value) {
        assertWritable();
        nativeSetId(index, value);
    }

    public void setFaceX0(int index, float value) {
        assertWritable();
        nativeSetFaceX0(index, value);
    }

    public void setFaceY0(int index, float value) {
        assertWritable();
        nativeSetFaceY0(index, value);
    }

    public void setFaceX1(int index, float value) {
        assertWritable();
        nativeSetFaceX1(index, value);
    }

    public void setFaceY1(int index, float value) {
        assertWritable();
        nativeSetFaceY1(index, value);
    }

    public void setLeftEyeX(int index, float value) {
        assertWritable();
        nativeSetLeftEyeX(index, value);
    }

    public void setLeftEyeY(int index, float value) {
        assertWritable();
        nativeSetLeftEyeY(index, value);
    }

    public void setRightEyeX(int index, float value) {
        assertWritable();
        nativeSetRightEyeX(index, value);
    }

    public void setRightEyeY(int index, float value) {
        assertWritable();
        nativeSetRightEyeY(index, value);
    }

    public void setLowerLipX(int index, float value) {
        assertWritable();
        nativeSetLowerLipX(index, value);
    }

    public void setLowerLipY(int index, float value) {
        assertWritable();
        nativeSetLowerLipY(index, value);
    }

    public void setUpperLipX(int index, float value) {
        assertWritable();
        nativeSetUpperLipX(index, value);
    }

    public void setUpperLipY(int index, float value) {
        assertWritable();
        nativeSetUpperLipY(index, value);
    }

    public void setMouthX(int index, float value) {
        assertWritable();
        nativeSetMouthX(index, value);
    }

    public void setMouthY(int index, float value) {
        assertWritable();
        nativeSetMouthY(index, value);
    }

    public void setMouthLeftX(int index, float value) {
        assertWritable();
        nativeSetMouthLeftX(index, value);
    }

    public void setMouthLeftY(int index, float value) {
        assertWritable();
        nativeSetMouthLeftY(index, value);
    }

    public void setMouthRightX(int index, float value) {
        assertWritable();
        nativeSetMouthRightX(index, value);
    }

    public void setMouthRightY(int index, float value) {
        assertWritable();
        nativeSetMouthRightY(index, value);
    }

    public void setConfidence(int index, float value) {
        assertWritable();
        nativeSetConfidence(index, value);
    }

    static {
        System.loadLibrary("filterpack_facedetect");
    }
}
