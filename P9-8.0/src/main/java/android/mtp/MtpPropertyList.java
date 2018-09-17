package android.mtp;

public class MtpPropertyList {
    private int mCount;
    public final int[] mDataTypes;
    public long[] mLongValues;
    private final int mMaxCount;
    public final int[] mObjectHandles;
    public final int[] mPropertyCodes;
    public int mResult;
    public String[] mStringValues;

    public MtpPropertyList(int maxCount, int result) {
        this.mMaxCount = maxCount;
        this.mResult = result;
        this.mObjectHandles = new int[maxCount];
        this.mPropertyCodes = new int[maxCount];
        this.mDataTypes = new int[maxCount];
    }

    public void append(int handle, int property, int type, long value) {
        int index = this.mCount;
        this.mCount = index + 1;
        if (this.mLongValues == null) {
            this.mLongValues = new long[this.mMaxCount];
        }
        this.mObjectHandles[index] = handle;
        this.mPropertyCodes[index] = property;
        this.mDataTypes[index] = type;
        this.mLongValues[index] = value;
    }

    public void append(int handle, int property, String value) {
        int index = this.mCount;
        this.mCount = index + 1;
        if (this.mStringValues == null) {
            this.mStringValues = new String[this.mMaxCount];
        }
        this.mObjectHandles[index] = handle;
        this.mPropertyCodes[index] = property;
        this.mDataTypes[index] = 65535;
        this.mStringValues[index] = value;
    }

    public void setResult(int result) {
        this.mResult = result;
    }
}
