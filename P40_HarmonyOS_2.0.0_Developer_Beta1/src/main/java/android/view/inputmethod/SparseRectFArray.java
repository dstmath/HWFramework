package android.view.inputmethod;

import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;
import java.util.Arrays;

public final class SparseRectFArray implements Parcelable {
    public static final Parcelable.Creator<SparseRectFArray> CREATOR = new Parcelable.Creator<SparseRectFArray>() {
        /* class android.view.inputmethod.SparseRectFArray.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SparseRectFArray createFromParcel(Parcel source) {
            return new SparseRectFArray(source);
        }

        @Override // android.os.Parcelable.Creator
        public SparseRectFArray[] newArray(int size) {
            return new SparseRectFArray[size];
        }
    };
    private final float[] mCoordinates;
    private final int[] mFlagsArray;
    private final int[] mKeys;

    public SparseRectFArray(Parcel source) {
        this.mKeys = source.createIntArray();
        this.mCoordinates = source.createFloatArray();
        this.mFlagsArray = source.createIntArray();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(this.mKeys);
        dest.writeFloatArray(this.mCoordinates);
        dest.writeIntArray(this.mFlagsArray);
    }

    public int hashCode() {
        int[] iArr = this.mKeys;
        if (iArr == null || iArr.length == 0) {
            return 0;
        }
        int hash = iArr.length;
        for (int i = 0; i < 4; i++) {
            hash = (int) (((float) (hash * 31)) + this.mCoordinates[i]);
        }
        return (hash * 31) + this.mFlagsArray[0];
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SparseRectFArray)) {
            return false;
        }
        SparseRectFArray that = (SparseRectFArray) obj;
        if (!Arrays.equals(this.mKeys, that.mKeys) || !Arrays.equals(this.mCoordinates, that.mCoordinates) || !Arrays.equals(this.mFlagsArray, that.mFlagsArray)) {
            return false;
        }
        return true;
    }

    public String toString() {
        if (this.mKeys == null || this.mCoordinates == null || this.mFlagsArray == null) {
            return "SparseRectFArray{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SparseRectFArray{");
        for (int i = 0; i < this.mKeys.length; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            int baseIndex = i * 4;
            sb.append(this.mKeys[i]);
            sb.append(":[");
            sb.append(this.mCoordinates[baseIndex + 0]);
            sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
            sb.append(this.mCoordinates[baseIndex + 1]);
            sb.append("],[");
            sb.append(this.mCoordinates[baseIndex + 2]);
            sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
            sb.append(this.mCoordinates[baseIndex + 3]);
            sb.append("]:flagsArray=");
            sb.append(this.mFlagsArray[i]);
        }
        sb.append("}");
        return sb.toString();
    }

    public static final class SparseRectFArrayBuilder {
        private static int INITIAL_SIZE = 16;
        private float[] mCoordinates = null;
        private int mCount = 0;
        private int[] mFlagsArray = null;
        private int[] mKeys = null;

        private void checkIndex(int key) {
            int i = this.mCount;
            if (i != 0 && this.mKeys[i - 1] >= key) {
                throw new IllegalArgumentException("key must be greater than all existing keys.");
            }
        }

        private void ensureBufferSize() {
            if (this.mKeys == null) {
                this.mKeys = new int[INITIAL_SIZE];
            }
            if (this.mCoordinates == null) {
                this.mCoordinates = new float[(INITIAL_SIZE * 4)];
            }
            if (this.mFlagsArray == null) {
                this.mFlagsArray = new int[INITIAL_SIZE];
            }
            int i = this.mCount;
            int requiredIndexArraySize = i + 1;
            int[] iArr = this.mKeys;
            if (iArr.length <= requiredIndexArraySize) {
                int[] newArray = new int[(requiredIndexArraySize * 2)];
                System.arraycopy(iArr, 0, newArray, 0, i);
                this.mKeys = newArray;
            }
            int i2 = this.mCount;
            int requiredCoordinatesArraySize = (i2 + 1) * 4;
            float[] fArr = this.mCoordinates;
            if (fArr.length <= requiredCoordinatesArraySize) {
                float[] newArray2 = new float[(requiredCoordinatesArraySize * 2)];
                System.arraycopy(fArr, 0, newArray2, 0, i2 * 4);
                this.mCoordinates = newArray2;
            }
            int[] iArr2 = this.mFlagsArray;
            if (iArr2.length <= requiredIndexArraySize) {
                int[] newArray3 = new int[(requiredIndexArraySize * 2)];
                System.arraycopy(iArr2, 0, newArray3, 0, this.mCount);
                this.mFlagsArray = newArray3;
            }
        }

        public SparseRectFArrayBuilder append(int key, float left, float top, float right, float bottom, int flags) {
            checkIndex(key);
            ensureBufferSize();
            int i = this.mCount;
            int baseCoordinatesIndex = i * 4;
            float[] fArr = this.mCoordinates;
            fArr[baseCoordinatesIndex + 0] = left;
            fArr[baseCoordinatesIndex + 1] = top;
            fArr[baseCoordinatesIndex + 2] = right;
            fArr[baseCoordinatesIndex + 3] = bottom;
            this.mFlagsArray[this.mCount] = flags;
            this.mKeys[i] = key;
            this.mCount = i + 1;
            return this;
        }

        public boolean isEmpty() {
            return this.mCount <= 0;
        }

        public SparseRectFArray build() {
            return new SparseRectFArray(this);
        }

        public void reset() {
            if (this.mCount == 0) {
                this.mKeys = null;
                this.mCoordinates = null;
                this.mFlagsArray = null;
            }
            this.mCount = 0;
        }
    }

    private SparseRectFArray(SparseRectFArrayBuilder builder) {
        if (builder.mCount == 0) {
            this.mKeys = null;
            this.mCoordinates = null;
            this.mFlagsArray = null;
            return;
        }
        this.mKeys = new int[builder.mCount];
        this.mCoordinates = new float[(builder.mCount * 4)];
        this.mFlagsArray = new int[builder.mCount];
        System.arraycopy(builder.mKeys, 0, this.mKeys, 0, builder.mCount);
        System.arraycopy(builder.mCoordinates, 0, this.mCoordinates, 0, builder.mCount * 4);
        System.arraycopy(builder.mFlagsArray, 0, this.mFlagsArray, 0, builder.mCount);
    }

    public RectF get(int index) {
        int arrayIndex;
        int[] iArr = this.mKeys;
        if (iArr == null || index < 0 || (arrayIndex = Arrays.binarySearch(iArr, index)) < 0) {
            return null;
        }
        int baseCoordIndex = arrayIndex * 4;
        float[] fArr = this.mCoordinates;
        return new RectF(fArr[baseCoordIndex], fArr[baseCoordIndex + 1], fArr[baseCoordIndex + 2], fArr[baseCoordIndex + 3]);
    }

    public int getFlags(int index, int valueIfKeyNotFound) {
        int arrayIndex;
        int[] iArr = this.mKeys;
        if (iArr != null && index >= 0 && (arrayIndex = Arrays.binarySearch(iArr, index)) >= 0) {
            return this.mFlagsArray[arrayIndex];
        }
        return valueIfKeyNotFound;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
