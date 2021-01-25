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

    /* access modifiers changed from: protected */
    @Override // android.filterfw.core.Frame
    public synchronized boolean hasNativeAllocation() {
        return this.vertexFrameId != -1;
    }

    /* access modifiers changed from: protected */
    @Override // android.filterfw.core.Frame
    public synchronized void releaseNativeAllocation() {
        nativeDeallocate();
        this.vertexFrameId = -1;
    }

    @Override // android.filterfw.core.Frame
    public Object getObjectValue() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    @Override // android.filterfw.core.Frame
    public void setInts(int[] ints) {
        assertFrameMutable();
        if (!setNativeInts(ints)) {
            throw new RuntimeException("Could not set int values for vertex frame!");
        }
    }

    @Override // android.filterfw.core.Frame
    public int[] getInts() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    @Override // android.filterfw.core.Frame
    public void setFloats(float[] floats) {
        assertFrameMutable();
        if (!setNativeFloats(floats)) {
            throw new RuntimeException("Could not set int values for vertex frame!");
        }
    }

    @Override // android.filterfw.core.Frame
    public float[] getFloats() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    @Override // android.filterfw.core.Frame
    public void setData(ByteBuffer buffer, int offset, int length) {
        assertFrameMutable();
        byte[] bytes = buffer.array();
        if (getFormat().getSize() != bytes.length) {
            throw new RuntimeException("Data size in setData does not match vertex frame size!");
        } else if (!setNativeData(bytes, offset, length)) {
            throw new RuntimeException("Could not set vertex frame data!");
        }
    }

    @Override // android.filterfw.core.Frame
    public ByteBuffer getData() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    @Override // android.filterfw.core.Frame
    public void setBitmap(Bitmap bitmap) {
        throw new RuntimeException("Unsupported: Cannot set vertex frame bitmap value!");
    }

    @Override // android.filterfw.core.Frame
    public Bitmap getBitmap() {
        throw new RuntimeException("Vertex frames do not support reading data!");
    }

    @Override // android.filterfw.core.Frame
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
