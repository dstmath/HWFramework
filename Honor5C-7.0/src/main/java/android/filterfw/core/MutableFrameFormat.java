package android.filterfw.core;

import java.util.Arrays;

public class MutableFrameFormat extends FrameFormat {
    public MutableFrameFormat(int baseType, int target) {
        super(baseType, target);
    }

    public void setBaseType(int baseType) {
        this.mBaseType = baseType;
        this.mBytesPerSample = FrameFormat.bytesPerSampleOf(baseType);
    }

    public void setTarget(int target) {
        this.mTarget = target;
    }

    public void setBytesPerSample(int bytesPerSample) {
        this.mBytesPerSample = bytesPerSample;
        this.mSize = -1;
    }

    public void setDimensions(int[] dimensions) {
        int[] iArr = null;
        if (dimensions != null) {
            iArr = Arrays.copyOf(dimensions, dimensions.length);
        }
        this.mDimensions = iArr;
        this.mSize = -1;
    }

    public void setDimensions(int size) {
        this.mDimensions = new int[]{size};
        this.mSize = -1;
    }

    public void setDimensions(int width, int height) {
        this.mDimensions = new int[]{width, height};
        this.mSize = -1;
    }

    public void setDimensions(int width, int height, int depth) {
        this.mDimensions = new int[]{width, height, depth};
        this.mSize = -1;
    }

    public void setDimensionCount(int count) {
        this.mDimensions = new int[count];
    }

    public void setObjectClass(Class objectClass) {
        this.mObjectClass = objectClass;
    }

    public void setMetaValue(String key, Object value) {
        if (this.mMetaData == null) {
            this.mMetaData = new KeyValueMap();
        }
        this.mMetaData.put(key, value);
    }
}
