package android.filterfw.core;

import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import java.util.Arrays;
import java.util.Map.Entry;

public class FrameFormat {
    public static final int BYTES_PER_SAMPLE_UNSPECIFIED = 1;
    protected static final int SIZE_UNKNOWN = -1;
    public static final int SIZE_UNSPECIFIED = 0;
    public static final int TARGET_GPU = 3;
    public static final int TARGET_NATIVE = 2;
    public static final int TARGET_RS = 5;
    public static final int TARGET_SIMPLE = 1;
    public static final int TARGET_UNSPECIFIED = 0;
    public static final int TARGET_VERTEXBUFFER = 4;
    public static final int TYPE_BIT = 1;
    public static final int TYPE_BYTE = 2;
    public static final int TYPE_DOUBLE = 6;
    public static final int TYPE_FLOAT = 5;
    public static final int TYPE_INT16 = 3;
    public static final int TYPE_INT32 = 4;
    public static final int TYPE_OBJECT = 8;
    public static final int TYPE_POINTER = 7;
    public static final int TYPE_UNSPECIFIED = 0;
    protected int mBaseType = 0;
    protected int mBytesPerSample = 1;
    protected int[] mDimensions;
    protected KeyValueMap mMetaData;
    protected Class mObjectClass;
    protected int mSize = -1;
    protected int mTarget = 0;

    protected FrameFormat() {
    }

    public FrameFormat(int baseType, int target) {
        this.mBaseType = baseType;
        this.mTarget = target;
        initDefaults();
    }

    public static FrameFormat unspecified() {
        return new FrameFormat(0, 0);
    }

    public int getBaseType() {
        return this.mBaseType;
    }

    public boolean isBinaryDataType() {
        return this.mBaseType >= 1 && this.mBaseType <= 6;
    }

    public int getBytesPerSample() {
        return this.mBytesPerSample;
    }

    public int getValuesPerSample() {
        return this.mBytesPerSample / bytesPerSampleOf(this.mBaseType);
    }

    public int getTarget() {
        return this.mTarget;
    }

    public int[] getDimensions() {
        return this.mDimensions;
    }

    public int getDimension(int i) {
        return this.mDimensions[i];
    }

    public int getDimensionCount() {
        return this.mDimensions == null ? 0 : this.mDimensions.length;
    }

    public boolean hasMetaKey(String key) {
        return this.mMetaData != null ? this.mMetaData.containsKey(key) : false;
    }

    public boolean hasMetaKey(String key, Class expectedClass) {
        if (this.mMetaData == null || !this.mMetaData.containsKey(key)) {
            return false;
        }
        if (expectedClass.isAssignableFrom(this.mMetaData.get(key).getClass())) {
            return true;
        }
        throw new RuntimeException("FrameFormat meta-key '" + key + "' is of type " + this.mMetaData.get(key).getClass() + " but expected to be of type " + expectedClass + "!");
    }

    public Object getMetaValue(String key) {
        return this.mMetaData != null ? this.mMetaData.get(key) : null;
    }

    public int getNumberOfDimensions() {
        return this.mDimensions != null ? this.mDimensions.length : 0;
    }

    public int getLength() {
        return (this.mDimensions == null || this.mDimensions.length < 1) ? -1 : this.mDimensions[0];
    }

    public int getWidth() {
        return getLength();
    }

    public int getHeight() {
        return (this.mDimensions == null || this.mDimensions.length < 2) ? -1 : this.mDimensions[1];
    }

    public int getDepth() {
        return (this.mDimensions == null || this.mDimensions.length < 3) ? -1 : this.mDimensions[2];
    }

    public int getSize() {
        if (this.mSize == -1) {
            this.mSize = calcSize(this.mDimensions);
        }
        return this.mSize;
    }

    public Class getObjectClass() {
        return this.mObjectClass;
    }

    public MutableFrameFormat mutableCopy() {
        KeyValueMap keyValueMap = null;
        MutableFrameFormat result = new MutableFrameFormat();
        result.setBaseType(getBaseType());
        result.setTarget(getTarget());
        result.setBytesPerSample(getBytesPerSample());
        result.setDimensions(getDimensions());
        result.setObjectClass(getObjectClass());
        if (this.mMetaData != null) {
            keyValueMap = (KeyValueMap) this.mMetaData.clone();
        }
        result.mMetaData = keyValueMap;
        return result;
    }

    public boolean equals(Object object) {
        boolean z = false;
        if (this == object) {
            return true;
        }
        if (!(object instanceof FrameFormat)) {
            return false;
        }
        FrameFormat format = (FrameFormat) object;
        if (format.mBaseType == this.mBaseType && format.mTarget == this.mTarget && format.mBytesPerSample == this.mBytesPerSample && Arrays.equals(format.mDimensions, this.mDimensions)) {
            z = format.mMetaData.equals(this.mMetaData);
        }
        return z;
    }

    public int hashCode() {
        return ((this.mBaseType ^ 4211) ^ this.mBytesPerSample) ^ getSize();
    }

    public boolean isCompatibleWith(FrameFormat specification) {
        if (specification.getBaseType() != 0 && getBaseType() != specification.getBaseType()) {
            return false;
        }
        if (specification.getTarget() != 0 && getTarget() != specification.getTarget()) {
            return false;
        }
        if (specification.getBytesPerSample() != 1 && getBytesPerSample() != specification.getBytesPerSample()) {
            return false;
        }
        if (specification.getDimensionCount() > 0 && getDimensionCount() != specification.getDimensionCount()) {
            return false;
        }
        int i = 0;
        while (i < specification.getDimensionCount()) {
            int specDim = specification.getDimension(i);
            if (specDim != 0 && getDimension(i) != specDim) {
                return false;
            }
            i++;
        }
        if (specification.getObjectClass() != null && (getObjectClass() == null || (specification.getObjectClass().isAssignableFrom(getObjectClass()) ^ 1) != 0)) {
            return false;
        }
        if (specification.mMetaData != null) {
            for (String specKey : specification.mMetaData.keySet()) {
                if (this.mMetaData == null || (this.mMetaData.containsKey(specKey) ^ 1) != 0) {
                    return false;
                }
                if ((this.mMetaData.get(specKey).equals(specification.mMetaData.get(specKey)) ^ 1) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean mayBeCompatibleWith(FrameFormat specification) {
        if (specification.getBaseType() != 0 && getBaseType() != 0 && getBaseType() != specification.getBaseType()) {
            return false;
        }
        if (specification.getTarget() != 0 && getTarget() != 0 && getTarget() != specification.getTarget()) {
            return false;
        }
        if (specification.getBytesPerSample() != 1 && getBytesPerSample() != 1 && getBytesPerSample() != specification.getBytesPerSample()) {
            return false;
        }
        if (specification.getDimensionCount() > 0 && getDimensionCount() > 0 && getDimensionCount() != specification.getDimensionCount()) {
            return false;
        }
        int i = 0;
        while (i < specification.getDimensionCount()) {
            int specDim = specification.getDimension(i);
            if (specDim != 0 && getDimension(i) != 0 && getDimension(i) != specDim) {
                return false;
            }
            i++;
        }
        if (specification.getObjectClass() != null && getObjectClass() != null && !specification.getObjectClass().isAssignableFrom(getObjectClass())) {
            return false;
        }
        if (!(specification.mMetaData == null || this.mMetaData == null)) {
            for (String specKey : specification.mMetaData.keySet()) {
                if (this.mMetaData.containsKey(specKey) && (this.mMetaData.get(specKey).equals(specification.mMetaData.get(specKey)) ^ 1) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int bytesPerSampleOf(int baseType) {
        switch (baseType) {
            case 1:
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
            case 5:
            case 7:
                return 4;
            case 6:
                return 8;
            default:
                return 1;
        }
    }

    public static String dimensionsToString(int[] dimensions) {
        StringBuffer buffer = new StringBuffer();
        if (dimensions != null) {
            int n = dimensions.length;
            for (int i = 0; i < n; i++) {
                if (dimensions[i] == 0) {
                    buffer.append("[]");
                } else {
                    buffer.append("[" + String.valueOf(dimensions[i]) + "]");
                }
            }
        }
        return buffer.toString();
    }

    public static String baseTypeToString(int baseType) {
        switch (baseType) {
            case 0:
                return "unspecified";
            case 1:
                return "bit";
            case 2:
                return "byte";
            case 3:
                return "int";
            case 4:
                return "int";
            case 5:
                return "float";
            case 6:
                return "double";
            case 7:
                return "pointer";
            case 8:
                return "object";
            default:
                return "unknown";
        }
    }

    public static String targetToString(int target) {
        switch (target) {
            case 0:
                return "unspecified";
            case 1:
                return "simple";
            case 2:
                return "native";
            case 3:
                return "gpu";
            case 4:
                return "vbo";
            case 5:
                return "renderscript";
            default:
                return "unknown";
        }
    }

    public static String metaDataToString(KeyValueMap metaData) {
        if (metaData == null) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("{ ");
        for (Entry<String, Object> entry : metaData.entrySet()) {
            buffer.append(((String) entry.getKey()) + ": " + entry.getValue() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static int readTargetString(String targetString) {
        if (targetString.equalsIgnoreCase("CPU") || targetString.equalsIgnoreCase("NATIVE")) {
            return 2;
        }
        if (targetString.equalsIgnoreCase("GPU")) {
            return 3;
        }
        if (targetString.equalsIgnoreCase("SIMPLE")) {
            return 1;
        }
        if (targetString.equalsIgnoreCase("VERTEXBUFFER")) {
            return 4;
        }
        if (targetString.equalsIgnoreCase("UNSPECIFIED")) {
            return 0;
        }
        throw new RuntimeException("Unknown target type '" + targetString + "'!");
    }

    public String toString() {
        String classString;
        int valuesPerSample = getValuesPerSample();
        String sampleCountString = valuesPerSample == 1 ? ProxyInfo.LOCAL_EXCL_LIST : String.valueOf(valuesPerSample);
        String targetString = this.mTarget == 0 ? ProxyInfo.LOCAL_EXCL_LIST : targetToString(this.mTarget) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        if (this.mObjectClass == null) {
            classString = ProxyInfo.LOCAL_EXCL_LIST;
        } else {
            classString = " class(" + this.mObjectClass.getSimpleName() + ") ";
        }
        return targetString + baseTypeToString(this.mBaseType) + sampleCountString + dimensionsToString(this.mDimensions) + classString + metaDataToString(this.mMetaData);
    }

    private void initDefaults() {
        this.mBytesPerSample = bytesPerSampleOf(this.mBaseType);
    }

    int calcSize(int[] dimensions) {
        int i = 0;
        if (dimensions == null || dimensions.length <= 0) {
            return 0;
        }
        int size = getBytesPerSample();
        while (i < dimensions.length) {
            size *= dimensions[i];
            i++;
        }
        return size;
    }

    boolean isReplaceableBy(FrameFormat format) {
        if (this.mTarget == format.mTarget && getSize() == format.getSize()) {
            return Arrays.equals(format.mDimensions, this.mDimensions);
        }
        return false;
    }
}
