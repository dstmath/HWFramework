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
        int baseType = format.getBaseType();
        if (baseType == 2) {
            this.mObject = new byte[count];
        } else if (baseType == 3) {
            this.mObject = new short[count];
        } else if (baseType == 4) {
            this.mObject = new int[count];
        } else if (baseType == 5) {
            this.mObject = new float[count];
        } else if (baseType != 6) {
            this.mObject = null;
        } else {
            this.mObject = new double[count];
        }
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
        return this.mObject;
    }

    @Override // android.filterfw.core.Frame
    public void setInts(int[] ints) {
        assertFrameMutable();
        setGenericObjectValue(ints);
    }

    @Override // android.filterfw.core.Frame
    public int[] getInts() {
        Object obj = this.mObject;
        if (obj instanceof int[]) {
            return (int[]) obj;
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
        Object obj = this.mObject;
        if (obj instanceof float[]) {
            return (float[]) obj;
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
        Object obj = this.mObject;
        if (obj instanceof ByteBuffer) {
            return (ByteBuffer) obj;
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
        Object obj = this.mObject;
        if (obj instanceof Bitmap) {
            return (Bitmap) obj;
        }
        return null;
    }

    private void setFormatObjectClass(Class objectClass) {
        MutableFrameFormat format = getFormat().mutableCopy();
        format.setObjectClass(objectClass);
        setFormat(format);
    }

    /* access modifiers changed from: protected */
    @Override // android.filterfw.core.Frame
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
