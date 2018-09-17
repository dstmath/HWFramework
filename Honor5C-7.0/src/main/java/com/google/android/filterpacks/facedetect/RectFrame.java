package com.google.android.filterpacks.facedetect;

import android.filterfw.core.NativeBuffer;

public class RectFrame extends NativeBuffer {
    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.filterpacks.facedetect.RectFrame.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.filterpacks.facedetect.RectFrame.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.filterpacks.facedetect.RectFrame.<clinit>():void");
    }

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
}
