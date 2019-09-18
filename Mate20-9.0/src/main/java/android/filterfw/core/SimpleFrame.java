package android.filterfw.core;

import android.filterfw.format.ObjectFormat;
import android.graphics.Bitmap;
import java.nio.ByteBuffer;

public class SimpleFrame extends Frame {
    private Object mObject;

    SimpleFrame(FrameFormat format, FrameManager frameManager) {
        super(format, frameManager);
        initWithFormat(format);
        setReusable(false);
    }

    static SimpleFrame wrapObject(Object object, FrameManager frameManager) {
        SimpleFrame result = new SimpleFrame(ObjectFormat.fromObject(object, 1), frameManager);
        result.setObjectValue(object);
        return result;
    }

    private void initWithFormat(FrameFormat format) {
        int count = format.getLength();
        switch (format.getBaseType()) {
            case 2:
                this.mObject = new byte[count];
                return;
            case 3:
                this.mObject = new short[count];
                return;
            case 4:
                this.mObject = new int[count];
                return;
            case 5:
                this.mObject = new float[count];
                return;
            case 6:
                this.mObject = new double[count];
                return;
            default:
                this.mObject = null;
                return;
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasNativeAllocation() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void releaseNativeAllocation() {
    }

    public Object getObjectValue() {
        return this.mObject;
    }

    public void setInts(int[] ints) {
        assertFrameMutable();
        setGenericObjectValue(ints);
    }

    public int[] getInts() {
        if (this.mObject instanceof int[]) {
            return (int[]) this.mObject;
        }
        return null;
    }

    public void setFloats(float[] floats) {
        assertFrameMutable();
        setGenericObjectValue(floats);
    }

    public float[] getFloats() {
        if (this.mObject instanceof float[]) {
            return (float[]) this.mObject;
        }
        return null;
    }

    public void setData(ByteBuffer buffer, int offset, int length) {
        assertFrameMutable();
        setGenericObjectValue(ByteBuffer.wrap(buffer.array(), offset, length));
    }

    public ByteBuffer getData() {
        if (this.mObject instanceof ByteBuffer) {
            return (ByteBuffer) this.mObject;
        }
        return null;
    }

    public void setBitmap(Bitmap bitmap) {
        assertFrameMutable();
        setGenericObjectValue(bitmap);
    }

    public Bitmap getBitmap() {
        if (this.mObject instanceof Bitmap) {
            return (Bitmap) this.mObject;
        }
        return null;
    }

    private void setFormatObjectClass(Class objectClass) {
        MutableFrameFormat format = getFormat().mutableCopy();
        format.setObjectClass(objectClass);
        setFormat(format);
    }

    /* access modifiers changed from: protected */
    public void setGenericObjectValue(Object object) {
        FrameFormat format = getFormat();
        if (format.getObjectClass() == null) {
            setFormatObjectClass(object.getClass());
        } else if (!format.getObjectClass().isAssignableFrom(object.getClass())) {
            throw new RuntimeException("Attempting to set object value of type '" + object.getClass() + "' on SimpleFrame of type '" + format.getObjectClass() + "'!");
        }
        this.mObject = object;
    }

    public String toString() {
        return "SimpleFrame (" + getFormat() + ")";
    }
}
