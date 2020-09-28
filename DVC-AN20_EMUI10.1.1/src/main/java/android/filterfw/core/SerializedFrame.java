package android.filterfw.core;

import android.filterfw.format.ObjectFormat;
import android.graphics.Bitmap;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SerializedFrame extends Frame {
    private static final int INITIAL_CAPACITY = 64;
    private DirectByteOutputStream mByteOutputStream;
    private ObjectOutputStream mObjectOut;

    /* access modifiers changed from: private */
    public class DirectByteOutputStream extends OutputStream {
        private byte[] mBuffer = null;
        private int mDataOffset = 0;
        private int mOffset = 0;

        public DirectByteOutputStream(int size) {
            this.mBuffer = new byte[size];
        }

        private final void ensureFit(int bytesToWrite) {
            int i = this.mOffset;
            int i2 = i + bytesToWrite;
            byte[] bArr = this.mBuffer;
            if (i2 > bArr.length) {
                byte[] oldBuffer = this.mBuffer;
                this.mBuffer = new byte[Math.max(i + bytesToWrite, bArr.length * 2)];
                System.arraycopy(oldBuffer, 0, this.mBuffer, 0, this.mOffset);
            }
        }

        public final void markHeaderEnd() {
            this.mDataOffset = this.mOffset;
        }

        public final int getSize() {
            return this.mOffset;
        }

        public byte[] getByteArray() {
            return this.mBuffer;
        }

        @Override // java.io.OutputStream
        public final void write(byte[] b) {
            write(b, 0, b.length);
        }

        @Override // java.io.OutputStream
        public final void write(byte[] b, int off, int len) {
            ensureFit(len);
            System.arraycopy(b, off, this.mBuffer, this.mOffset, len);
            this.mOffset += len;
        }

        @Override // java.io.OutputStream
        public final void write(int b) {
            ensureFit(1);
            byte[] bArr = this.mBuffer;
            int i = this.mOffset;
            this.mOffset = i + 1;
            bArr[i] = (byte) b;
        }

        public final void reset() {
            this.mOffset = this.mDataOffset;
        }

        public final DirectByteInputStream getInputStream() {
            return new DirectByteInputStream(this.mBuffer, this.mOffset);
        }
    }

    /* access modifiers changed from: private */
    public class DirectByteInputStream extends InputStream {
        private byte[] mBuffer;
        private int mPos = 0;
        private int mSize;

        public DirectByteInputStream(byte[] buffer, int size) {
            this.mBuffer = buffer;
            this.mSize = size;
        }

        @Override // java.io.InputStream
        public final int available() {
            return this.mSize - this.mPos;
        }

        @Override // java.io.InputStream
        public final int read() {
            int i = this.mPos;
            if (i >= this.mSize) {
                return -1;
            }
            byte[] bArr = this.mBuffer;
            this.mPos = i + 1;
            return bArr[i] & 255;
        }

        @Override // java.io.InputStream
        public final int read(byte[] b, int off, int len) {
            int i = this.mPos;
            int i2 = this.mSize;
            if (i >= i2) {
                return -1;
            }
            if (i + len > i2) {
                len = i2 - i;
            }
            System.arraycopy(this.mBuffer, this.mPos, b, off, len);
            this.mPos += len;
            return len;
        }

        @Override // java.io.InputStream
        public final long skip(long n) {
            int i = this.mPos;
            int i2 = this.mSize;
            if (((long) i) + n > ((long) i2)) {
                n = (long) (i2 - i);
            }
            if (n < 0) {
                return 0;
            }
            this.mPos = (int) (((long) this.mPos) + n);
            return n;
        }
    }

    SerializedFrame(FrameFormat format, FrameManager frameManager) {
        super(format, frameManager);
        setReusable(false);
        try {
            this.mByteOutputStream = new DirectByteOutputStream(64);
            this.mObjectOut = new ObjectOutputStream(this.mByteOutputStream);
            this.mByteOutputStream.markHeaderEnd();
        } catch (IOException e) {
            throw new RuntimeException("Could not create serialization streams for SerializedFrame!", e);
        }
    }

    static SerializedFrame wrapObject(Object object, FrameManager frameManager) {
        SerializedFrame result = new SerializedFrame(ObjectFormat.fromObject(object, 1), frameManager);
        result.setObjectValue(object);
        return result;
    }

    /* access modifiers changed from: protected */
    @Override // android.filterfw.core.Frame
    public boolean hasNativeAllocation() {
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // android.filterfw.core.Frame
    public void releaseNativeAllocation() {
    }

    @Override // android.filterfw.core.Frame
    public Object getObjectValue() {
        return deserializeObjectValue();
    }

    @Override // android.filterfw.core.Frame
    public void setInts(int[] ints) {
        assertFrameMutable();
        setGenericObjectValue(ints);
    }

    @Override // android.filterfw.core.Frame
    public int[] getInts() {
        Object result = deserializeObjectValue();
        if (result instanceof int[]) {
            return (int[]) result;
        }
        return null;
    }

    @Override // android.filterfw.core.Frame
    public void setFloats(float[] floats) {
        assertFrameMutable();
        setGenericObjectValue(floats);
    }

    @Override // android.filterfw.core.Frame
    public float[] getFloats() {
        Object result = deserializeObjectValue();
        if (result instanceof float[]) {
            return (float[]) result;
        }
        return null;
    }

    @Override // android.filterfw.core.Frame
    public void setData(ByteBuffer buffer, int offset, int length) {
        assertFrameMutable();
        setGenericObjectValue(ByteBuffer.wrap(buffer.array(), offset, length));
    }

    @Override // android.filterfw.core.Frame
    public ByteBuffer getData() {
        Object result = deserializeObjectValue();
        if (result instanceof ByteBuffer) {
            return (ByteBuffer) result;
        }
        return null;
    }

    @Override // android.filterfw.core.Frame
    public void setBitmap(Bitmap bitmap) {
        assertFrameMutable();
        setGenericObjectValue(bitmap);
    }

    @Override // android.filterfw.core.Frame
    public Bitmap getBitmap() {
        Object result = deserializeObjectValue();
        if (result instanceof Bitmap) {
            return (Bitmap) result;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // android.filterfw.core.Frame
    public void setGenericObjectValue(Object object) {
        serializeObjectValue(object);
    }

    private final void serializeObjectValue(Object object) {
        try {
            this.mByteOutputStream.reset();
            this.mObjectOut.writeObject(object);
            this.mObjectOut.flush();
            this.mObjectOut.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize object " + object + " in " + this + "!", e);
        }
    }

    private final Object deserializeObjectValue() {
        try {
            return new ObjectInputStream(this.mByteOutputStream.getInputStream()).readObject();
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize object in " + this + "!", e);
        } catch (ClassNotFoundException e2) {
            throw new RuntimeException("Unable to deserialize object of unknown class in " + this + "!", e2);
        }
    }

    public String toString() {
        return "SerializedFrame (" + getFormat() + ")";
    }
}
