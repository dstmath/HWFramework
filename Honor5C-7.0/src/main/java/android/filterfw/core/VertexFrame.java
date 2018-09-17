package android.filterfw.core;

import android.graphics.Bitmap;
import java.nio.ByteBuffer;

public class VertexFrame extends Frame {
    private int vertexFrameId;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.filterfw.core.VertexFrame.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.filterfw.core.VertexFrame.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.filterfw.core.VertexFrame.<clinit>():void");
    }

    private native int getNativeVboId();

    private native boolean nativeAllocate(int i);

    private native boolean nativeDeallocate();

    private native boolean setNativeData(byte[] bArr, int i, int i2);

    private native boolean setNativeFloats(float[] fArr);

    private native boolean setNativeInts(int[] iArr);

    VertexFrame(FrameFormat format, FrameManager frameManager) {
        super(format, frameManager);
        this.vertexFrameId = -1;
        if (getFormat().getSize() <= 0) {
            throw new IllegalArgumentException("Initializing vertex frame with zero size!");
        } else if (!nativeAllocate(getFormat().getSize())) {
            throw new RuntimeException("Could not allocate vertex frame!");
        }
    }

    protected synchronized boolean hasNativeAllocation() {
        return this.vertexFrameId != -1;
    }

    protected synchronized void releaseNativeAllocation() {
        nativeDeallocate();
        this.vertexFrameId = -1;
    }

    public Object getObjectValue() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    public void setInts(int[] ints) {
        assertFrameMutable();
        if (!setNativeInts(ints)) {
            throw new RuntimeException("Could not set int values for vertex frame!");
        }
    }

    public int[] getInts() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    public void setFloats(float[] floats) {
        assertFrameMutable();
        if (!setNativeFloats(floats)) {
            throw new RuntimeException("Could not set int values for vertex frame!");
        }
    }

    public float[] getFloats() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    public void setData(ByteBuffer buffer, int offset, int length) {
        assertFrameMutable();
        byte[] bytes = buffer.array();
        if (getFormat().getSize() != bytes.length) {
            throw new RuntimeException("Data size in setData does not match vertex frame size!");
        } else if (!setNativeData(bytes, offset, length)) {
            throw new RuntimeException("Could not set vertex frame data!");
        }
    }

    public ByteBuffer getData() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    public void setBitmap(Bitmap bitmap) {
        throw new RuntimeException("Unsupported: Cannot set vertex frame bitmap value!");
    }

    public Bitmap getBitmap() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    public void setDataFromFrame(Frame frame) {
        super.setDataFromFrame(frame);
    }

    public int getVboId() {
        return getNativeVboId();
    }

    public String toString() {
        return "VertexFrame (" + getFormat() + ") with VBO ID " + getVboId();
    }
}
