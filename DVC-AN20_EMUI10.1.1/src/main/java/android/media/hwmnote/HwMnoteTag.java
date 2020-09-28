package android.media.hwmnote;

import android.media.BuildConfig;
import android.util.Log;
import java.io.IOException;

public class HwMnoteTag {
    public static final int SIZE_UNDEFINED = 0;
    private static final String TAG = "HwMnoteTag";
    private static final int[] TYPE_TO_SIZE_MAP = new int[TYPE_TO_SIZE_MAP_LENGTH];
    private static final int TYPE_TO_SIZE_MAP_LENGTH = 11;
    public static final short TYPE_UNDEFINED = 7;
    private static final short TYPE_UNDEFINED_VALUE = 1;
    public static final short TYPE_UNSIGNED_LONG = 4;
    private static final int TYPE_UNSIGNED_LONG_VALUE = 4;
    private static final long UNSIGNED_LONG_MAX = 4294967295L;
    private int mComponentCountActual;
    private final short mDataType;
    private boolean mHasDefinedDefaultComponentCount;
    private int mIfd;
    private int mOffset;
    private final short mTagId;
    private Object mValue = null;

    static {
        int[] iArr = TYPE_TO_SIZE_MAP;
        iArr[4] = 4;
        iArr[7] = 1;
    }

    HwMnoteTag(short tagId, short type, int componentCount, int ifd, boolean hasDefinedComponentCount) {
        this.mTagId = tagId;
        this.mDataType = type;
        this.mComponentCountActual = componentCount;
        this.mHasDefinedDefaultComponentCount = hasDefinedComponentCount;
        this.mIfd = ifd;
    }

    public static boolean isValidIfd(int ifdId) {
        return ifdId == 0 || ifdId == 1 || ifdId == 2;
    }

    public static boolean isValidType(short type) {
        return type == 4 || type == 7;
    }

    public int getIfd() {
        return this.mIfd;
    }

    /* access modifiers changed from: protected */
    public void setIfd(int ifdId) {
        this.mIfd = ifdId;
    }

    public short getTagId() {
        return this.mTagId;
    }

    public short getDataType() {
        return this.mDataType;
    }

    public int getDataSize() {
        short s = this.mDataType;
        if (s >= 0 && s < TYPE_TO_SIZE_MAP_LENGTH) {
            return getComponentCount() * TYPE_TO_SIZE_MAP[this.mDataType];
        }
        Log.e(TAG, "invalid mDataType index in getDataSize" + ((int) this.mDataType));
        return 0;
    }

    public int getComponentCount() {
        return this.mComponentCountActual;
    }

    /* access modifiers changed from: protected */
    public void forceSetComponentCount(int count) {
        this.mComponentCountActual = count;
    }

    public boolean hasValue() {
        return this.mValue != null;
    }

    public boolean setValue(int[] value) {
        if (checkBadComponentCount(value.length) || this.mDataType != 4 || checkOverflowForUnsignedLong(value)) {
            return false;
        }
        long[] data = new long[value.length];
        int length = value.length;
        for (int i = 0; i < length; i++) {
            data[i] = (long) value[i];
        }
        this.mValue = data;
        this.mComponentCountActual = length;
        return true;
    }

    public boolean setValue(int value) {
        return setValue(new int[]{value});
    }

    public boolean setValue(long[] value) {
        if (checkBadComponentCount(value.length) || this.mDataType != 4 || checkOverflowForUnsignedLong(value)) {
            return false;
        }
        this.mValue = value;
        this.mComponentCountActual = value.length;
        return true;
    }

    public boolean setValue(long value) {
        return setValue(new long[]{value});
    }

    public boolean setValue(byte[] value, int offset, int length) {
        if (checkBadComponentCount(length) || this.mDataType != 7) {
            return false;
        }
        this.mValue = new byte[length];
        System.arraycopy(value, offset, this.mValue, 0, length);
        this.mComponentCountActual = length;
        return true;
    }

    public boolean setValue(byte[] value) {
        return setValue(value, 0, value.length);
    }

    public boolean setValue(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof int[]) {
            return setValue((int[]) obj);
        }
        if (obj instanceof long[]) {
            return setValue((long[]) obj);
        }
        if (obj instanceof byte[]) {
            return setValue((byte[]) obj);
        }
        if (obj instanceof Integer) {
            return setValue(((Integer) obj).intValue());
        }
        if (obj instanceof Long) {
            return setValue(((Long) obj).longValue());
        }
        if (obj instanceof Integer[]) {
            return setValue(getInts((Integer[]) obj));
        }
        if (obj instanceof Long[]) {
            return setValue(getLongs((Long[]) obj));
        }
        if (obj instanceof Byte[]) {
            return setValue(getBytes((Byte[]) obj));
        }
        Log.w(TAG, "set value with error type, tagId:" + ((int) this.mTagId));
        return false;
    }

    private int[] getInts(Integer[] obj) {
        int length = obj.length;
        int[] fin = new int[length];
        for (int i = 0; i < length; i++) {
            fin[i] = obj[i] == null ? 0 : obj[i].intValue();
        }
        return fin;
    }

    private long[] getLongs(Long[] obj) {
        int length = obj.length;
        long[] fin = new long[length];
        for (int i = 0; i < length; i++) {
            fin[i] = obj[i] == null ? 0 : obj[i].longValue();
        }
        return fin;
    }

    private byte[] getBytes(Byte[] obj) {
        int length = obj.length;
        byte[] fin = new byte[length];
        for (int i = 0; i < length; i++) {
            fin[i] = obj[i] == null ? 0 : obj[i].byteValue();
        }
        return fin;
    }

    public byte[] getValueAsBytes() {
        Object obj = this.mValue;
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        return new byte[0];
    }

    public int[] getValueAsInts() {
        Object obj = this.mValue;
        if (obj == null || !(obj instanceof long[])) {
            return new int[0];
        }
        long[] val = (long[]) obj;
        int length = val.length;
        int[] arr = new int[length];
        for (int i = 0; i < length; i++) {
            arr[i] = (int) val[i];
        }
        return arr;
    }

    public int getValueAsInt(int defaultValue) {
        int[] valueArray = getValueAsInts();
        if (valueArray == null || valueArray.length < 1) {
            return defaultValue;
        }
        return valueArray[0];
    }

    public long[] getValueAsLongs() {
        Object obj = this.mValue;
        if (obj instanceof long[]) {
            return (long[]) obj;
        }
        return new long[0];
    }

    public long getValueAsLong(long defaultValue) {
        long[] valueArray = getValueAsLongs();
        if (valueArray.length < 1) {
            return defaultValue;
        }
        return valueArray[0];
    }

    public Object getValue() {
        return this.mValue;
    }

    /* access modifiers changed from: protected */
    public long getValueAt(int index) throws IOException {
        Object obj = this.mValue;
        if (obj instanceof long[]) {
            if (index >= 0 && index < ((long[]) obj).length) {
                return ((long[]) obj)[index];
            }
            Log.e(TAG, "invalid index in getValueAt long" + index);
        }
        Object obj2 = this.mValue;
        if (obj2 instanceof byte[]) {
            if (index >= 0 && index < ((byte[]) obj2).length) {
                return (long) ((byte[]) obj2)[index];
            }
            Log.e(TAG, "invalid index in getValueAt byte" + index);
        }
        throw new IOException("Cannot get integer value from " + convertTypeToString(this.mDataType));
    }

    /* access modifiers changed from: protected */
    public void getBytes(byte[] buf) throws IOException {
        getBytes(buf, 0, buf.length);
    }

    /* access modifiers changed from: protected */
    public void getBytes(byte[] buf, int offset, int length) throws IOException {
        if (this.mDataType == 7) {
            Object obj = this.mValue;
            int i = this.mComponentCountActual;
            if (length <= i) {
                i = length;
            }
            System.arraycopy(obj, 0, buf, offset, i);
            return;
        }
        throw new IOException("Cannot get BYTE value from " + convertTypeToString(this.mDataType));
    }

    /* access modifiers changed from: protected */
    public int getOffset() {
        return this.mOffset;
    }

    /* access modifiers changed from: protected */
    public void setOffset(int offset) {
        this.mOffset = offset;
    }

    /* access modifiers changed from: protected */
    public void setHasDefinedCount(boolean hasDefinedCount) {
        this.mHasDefinedDefaultComponentCount = hasDefinedCount;
    }

    /* access modifiers changed from: protected */
    public boolean hasDefinedCount() {
        return this.mHasDefinedDefaultComponentCount;
    }

    private boolean checkBadComponentCount(int count) {
        if (!this.mHasDefinedDefaultComponentCount || this.mComponentCountActual == count) {
            return false;
        }
        return true;
    }

    private static String convertTypeToString(short type) {
        if (type == 4) {
            return "UNSIGNED_LONG";
        }
        if (type != 7) {
            return BuildConfig.FLAVOR;
        }
        return "UNDEFINED";
    }

    private boolean checkOverflowForUnsignedLong(long[] value) {
        for (long item : value) {
            if (item < 0 || item > UNSIGNED_LONG_MAX) {
                return true;
            }
        }
        return false;
    }

    private boolean checkOverflowForUnsignedLong(int[] value) {
        for (int item : value) {
            if (item < 0) {
                return true;
            }
        }
        return false;
    }
}
