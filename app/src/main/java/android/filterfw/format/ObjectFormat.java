package android.filterfw.format;

import android.filterfw.core.MutableFrameFormat;
import android.filterfw.core.NativeBuffer;

public class ObjectFormat {
    public static MutableFrameFormat fromClass(Class clazz, int count, int target) {
        MutableFrameFormat result = new MutableFrameFormat(8, target);
        result.setObjectClass(getBoxedClass(clazz));
        if (count != 0) {
            result.setDimensions(count);
        }
        result.setBytesPerSample(bytesPerSampleForClass(clazz, target));
        return result;
    }

    public static MutableFrameFormat fromClass(Class clazz, int target) {
        return fromClass(clazz, 0, target);
    }

    public static MutableFrameFormat fromObject(Object object, int target) {
        if (object == null) {
            return new MutableFrameFormat(8, target);
        }
        return fromClass(object.getClass(), 0, target);
    }

    public static MutableFrameFormat fromObject(Object object, int count, int target) {
        if (object == null) {
            return new MutableFrameFormat(8, target);
        }
        return fromClass(object.getClass(), count, target);
    }

    private static int bytesPerSampleForClass(Class clazz, int target) {
        if (target != 2) {
            return 1;
        }
        if (NativeBuffer.class.isAssignableFrom(clazz)) {
            try {
                return ((NativeBuffer) clazz.newInstance()).getElementSize();
            } catch (Exception e) {
                throw new RuntimeException("Could not determine the size of an element in a native object-based frame of type " + clazz + "! Perhaps it is missing a " + "default constructor?");
            }
        }
        throw new IllegalArgumentException("Native object-based formats must be of a NativeBuffer subclass! (Received class: " + clazz + ").");
    }

    private static Class getBoxedClass(Class type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Character.TYPE) {
            return Character.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        throw new IllegalArgumentException("Unknown primitive type: " + type.getSimpleName() + "!");
    }
}
