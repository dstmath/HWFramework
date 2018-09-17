package com.google.android.filterpacks.facedetect;

import android.filterfw.core.NativeBuffer;

public class LipDiff extends NativeBuffer {
    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.filterpacks.facedetect.LipDiff.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.filterpacks.facedetect.LipDiff.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.filterpacks.facedetect.LipDiff.<clinit>():void");
    }

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
}
