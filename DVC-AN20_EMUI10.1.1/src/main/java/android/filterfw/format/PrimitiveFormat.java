package android.filterfw.format;

import android.filterfw.core.MutableFrameFormat;

public class PrimitiveFormat {
    public static MutableFrameFormat createByteFormat(int count, int target) {
        return createFormat(2, count, target);
    }

    public static MutableFrameFormat createInt16Format(int count, int target) {
        return createFormat(3, count, target);
    }

    public static MutableFrameFormat createInt32Format(int count, int target) {
        return createFormat(4, count, target);
    }

    public static MutableFrameFormat createFloatFormat(int count, int target) {
        return createFormat(5, count, target);
    }

    public static MutableFrameFormat createDoubleFormat(int count, int target) {
        return createFormat(6, count, target);
    }

    public static MutableFrameFormat createByteFormat(int target) {
        return createFormat(2, target);
    }

    public static MutableFrameFormat createInt16Format(int target) {
        return createFormat(3, target);
    }

    public static MutableFrameFormat createInt32Format(int target) {
        return createFormat(4, target);
    }

    public static MutableFrameFormat createFloatFormat(int target) {
        return createFormat(5, target);
    }

    public static MutableFrameFormat createDoubleFormat(int target) {
        return createFormat(6, target);
    }

    private static MutableFrameFormat createFormat(int baseType, int count, int target) {
        MutableFrameFormat result = new MutableFrameFormat(baseType, target);
        result.setDimensions(count);
        return result;
    }

    private static MutableFrameFormat createFormat(int baseType, int target) {
        MutableFrameFormat result = new MutableFrameFormat(baseType, target);
        result.setDimensionCount(1);
        return result;
    }
}
