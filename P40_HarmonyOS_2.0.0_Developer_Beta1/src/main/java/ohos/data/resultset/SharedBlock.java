package ohos.data.resultset;

import ohos.data.rdb.impl.CoreCloseable;
import ohos.data.resultset.ResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class SharedBlock extends CoreCloseable implements Sequenceable {
    private static final int DEFAULT_BLOCK_SIZE = 2097152;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "SharedBlock");
    private long mBlockPtr;
    private String mName;
    private int mStartRowIndex;

    private static native boolean nativeAllocRow(long j);

    private static native void nativeClear(long j);

    private static native long nativeCreate(String str, int i);

    private static native long nativeCreateFromParcel(Parcel parcel);

    private static native void nativeDispose(long j);

    private static native void nativeFreeLastRow(long j);

    private static native byte[] nativeGetBlob(long j, int i, int i2);

    private static native double nativeGetDouble(long j, int i, int i2);

    private static native long nativeGetLong(long j, int i, int i2);

    private static native String nativeGetName(long j);

    private static native byte[] nativeGetRawData(long j);

    private static native int nativeGetRowNum(long j);

    private static native String nativeGetString(long j, int i, int i2);

    private static native int nativeGetType(long j, int i, int i2);

    private static native void nativeInit();

    private static native boolean nativePutBlob(long j, byte[] bArr, int i, int i2);

    private static native boolean nativePutDouble(long j, double d, int i, int i2);

    private static native boolean nativePutLong(long j, long j2, int i, int i2);

    private static native boolean nativePutNull(long j, int i, int i2);

    private static native boolean nativePutString(long j, String str, int i, int i2);

    private static native boolean nativeSetColumnNum(long j, int i);

    private static native void nativeSetRawData(long j, byte[] bArr);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    public boolean unmarshalling(Parcel parcel) {
        return false;
    }

    static {
        try {
            System.loadLibrary("appdatamgr_jni.z");
            System.loadLibrary("utils_jni.z");
        } catch (NullPointerException | SecurityException | UnsatisfiedLinkError unused) {
            HiLog.info(LABEL, "SharedBlock load library failed.", new Object[0]);
        }
        nativeInit();
    }

    public SharedBlock(String str) {
        this.mStartRowIndex = 0;
        this.mName = (str == null || str.length() == 0) ? "unKnown" : str;
        this.mBlockPtr = nativeCreate(this.mName, 2097152);
        if (this.mBlockPtr == 0) {
            throw new AssertionError("Shared block allocation failed");
        }
    }

    public SharedBlock(Parcel parcel) {
        this.mStartRowIndex = parcel.readInt();
        this.mBlockPtr = nativeCreateFromParcel(parcel);
        long j = this.mBlockPtr;
        if (j != 0) {
            this.mName = nativeGetName(j);
            return;
        }
        throw new AssertionError("Shared block could not be created from parcel.");
    }

    public boolean allocateRow() {
        acquireRef();
        try {
            return nativeAllocRow(this.mBlockPtr);
        } finally {
            releaseRef();
        }
    }

    public void freeLastRow() {
        acquireRef();
        try {
            nativeFreeLastRow(this.mBlockPtr);
        } finally {
            releaseRef();
        }
    }

    public ResultSet.ColumnType getType(int i, int i2) {
        acquireRef();
        try {
            return ResultSet.ColumnType.getByValue(nativeGetType(this.mBlockPtr, i - this.mStartRowIndex, i2));
        } finally {
            releaseRef();
        }
    }

    public byte[] getRawData() {
        acquireRef();
        try {
            return nativeGetRawData(this.mBlockPtr);
        } finally {
            releaseRef();
        }
    }

    public void setRawData(byte[] bArr) {
        acquireRef();
        try {
            nativeSetRawData(this.mBlockPtr, bArr);
        } finally {
            releaseRef();
        }
    }

    public String getName() {
        return this.mName;
    }

    public long getBlockPtr() {
        return this.mBlockPtr;
    }

    public void clear() {
        acquireRef();
        try {
            this.mStartRowIndex = 0;
            nativeClear(this.mBlockPtr);
        } finally {
            releaseRef();
        }
    }

    public int getStartRowIndex() {
        return this.mStartRowIndex;
    }

    public void setStartRowIndex(int i) {
        this.mStartRowIndex = i;
    }

    public int getRowCount() {
        acquireRef();
        try {
            return nativeGetRowNum(this.mBlockPtr);
        } finally {
            releaseRef();
        }
    }

    public boolean setColumnCount(int i) {
        acquireRef();
        try {
            return nativeSetColumnNum(this.mBlockPtr, i);
        } finally {
            releaseRef();
        }
    }

    public byte[] getBlob(int i, int i2) {
        acquireRef();
        try {
            return nativeGetBlob(this.mBlockPtr, i - this.mStartRowIndex, i2);
        } finally {
            releaseRef();
        }
    }

    public String getString(int i, int i2) {
        acquireRef();
        try {
            return nativeGetString(this.mBlockPtr, i - this.mStartRowIndex, i2);
        } finally {
            releaseRef();
        }
    }

    public long getLong(int i, int i2) {
        acquireRef();
        try {
            return nativeGetLong(this.mBlockPtr, i - this.mStartRowIndex, i2);
        } finally {
            releaseRef();
        }
    }

    public double getDouble(int i, int i2) {
        acquireRef();
        try {
            return nativeGetDouble(this.mBlockPtr, i - this.mStartRowIndex, i2);
        } finally {
            releaseRef();
        }
    }

    public short getShort(int i, int i2) {
        return (short) ((int) getLong(i, i2));
    }

    public int getInt(int i, int i2) {
        return (int) getLong(i, i2);
    }

    public float getFloat(int i, int i2) {
        return (float) getDouble(i, i2);
    }

    public boolean putBlob(byte[] bArr, int i, int i2) {
        acquireRef();
        try {
            return nativePutBlob(this.mBlockPtr, bArr, i - this.mStartRowIndex, i2);
        } finally {
            releaseRef();
        }
    }

    public boolean putString(String str, int i, int i2) {
        acquireRef();
        try {
            return nativePutString(this.mBlockPtr, str, i - this.mStartRowIndex, i2);
        } finally {
            releaseRef();
        }
    }

    public boolean putLong(long j, int i, int i2) {
        acquireRef();
        try {
            return nativePutLong(this.mBlockPtr, j, i - this.mStartRowIndex, i2);
        } finally {
            releaseRef();
        }
    }

    public boolean putDouble(double d, int i, int i2) {
        acquireRef();
        try {
            return nativePutDouble(this.mBlockPtr, d, i - this.mStartRowIndex, i2);
        } finally {
            releaseRef();
        }
    }

    public boolean putNull(int i, int i2) {
        acquireRef();
        try {
            return nativePutNull(this.mBlockPtr, i - this.mStartRowIndex, i2);
        } finally {
            releaseRef();
        }
    }

    private void dispose() {
        long j = this.mBlockPtr;
        if (j != 0) {
            nativeDispose(j);
            this.mBlockPtr = 0;
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            dispose();
        } finally {
            super.finalize();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.data.rdb.impl.CoreCloseable
    public void onAllRefReleased() {
        dispose();
    }

    /* JADX INFO: finally extract failed */
    public boolean marshalling(Parcel parcel) {
        acquireRef();
        try {
            parcel.writeInt(this.mStartRowIndex);
            nativeWriteToParcel(this.mBlockPtr, parcel);
            releaseRef();
            return true;
        } catch (Throwable th) {
            releaseRef();
            throw th;
        }
    }

    @Override // ohos.data.rdb.impl.CoreCloseable, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        super.close();
    }
}
