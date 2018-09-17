package android.filterfw.core;

import android.graphics.Bitmap;
import java.nio.ByteBuffer;

public class VertexFrame extends Frame {
    private int vertexFrameId = -1;

    private native int getNativeVboId();

    private native boolean nativeAllocate(int i);

    private native boolean nativeDeallocate();

    private native boolean setNativeData(byte[] bArr, int i, int i2);

    private native boolean setNativeFloats(float[] fArr);

    private native boolean setNativeInts(int[] iArr);

    VertexFrame(FrameFormat format, FrameManager frameManager) {
        super(format, frameManager);
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

    static {
        System.loadLibrary("filterfw");
    }
}
