package android.os;

import android.os.Parcelable.ClassLoaderCreator;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import dalvik.system.VMRuntime;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import libcore.util.SneakyThrow;

public final class Parcel {
    private static final boolean DEBUG_ARRAY_MAP = false;
    private static final boolean DEBUG_RECYCLE = false;
    private static final int EX_BAD_PARCELABLE = -2;
    private static final int EX_HAS_REPLY_HEADER = -128;
    private static final int EX_ILLEGAL_ARGUMENT = -3;
    private static final int EX_ILLEGAL_STATE = -5;
    private static final int EX_NETWORK_MAIN_THREAD = -6;
    private static final int EX_NULL_POINTER = -4;
    private static final int EX_PARCELABLE = -9;
    private static final int EX_SECURITY = -1;
    private static final int EX_SERVICE_SPECIFIC = -8;
    private static final int EX_TRANSACTION_FAILED = -129;
    private static final int EX_UNSUPPORTED_OPERATION = -7;
    private static final int POOL_SIZE = 6;
    public static final Creator<String> STRING_CREATOR = new Creator<String>() {
        public String createFromParcel(Parcel source) {
            return source.readString();
        }

        public String[] newArray(int size) {
            return new String[size];
        }
    };
    private static final String TAG = "Parcel";
    private static final int VAL_BOOLEAN = 9;
    private static final int VAL_BOOLEANARRAY = 23;
    private static final int VAL_BUNDLE = 3;
    private static final int VAL_BYTE = 20;
    private static final int VAL_BYTEARRAY = 13;
    private static final int VAL_CHARSEQUENCE = 10;
    private static final int VAL_CHARSEQUENCEARRAY = 24;
    private static final int VAL_DOUBLE = 8;
    private static final int VAL_DOUBLEARRAY = 28;
    private static final int VAL_FLOAT = 7;
    private static final int VAL_IBINDER = 15;
    private static final int VAL_INTARRAY = 18;
    private static final int VAL_INTEGER = 1;
    private static final int VAL_LIST = 11;
    private static final int VAL_LONG = 6;
    private static final int VAL_LONGARRAY = 19;
    private static final int VAL_MAP = 2;
    private static final int VAL_NULL = -1;
    private static final int VAL_OBJECTARRAY = 17;
    private static final int VAL_PARCELABLE = 4;
    private static final int VAL_PARCELABLEARRAY = 16;
    private static final int VAL_PERSISTABLEBUNDLE = 25;
    private static final int VAL_SERIALIZABLE = 21;
    private static final int VAL_SHORT = 5;
    private static final int VAL_SIZE = 26;
    private static final int VAL_SIZEF = 27;
    private static final int VAL_SPARSEARRAY = 12;
    private static final int VAL_SPARSEBOOLEANARRAY = 22;
    private static final int VAL_STRING = 0;
    private static final int VAL_STRINGARRAY = 14;
    private static final HashMap<ClassLoader, HashMap<String, Creator<?>>> mCreators = new HashMap();
    private static final Parcel[] sHolderPool = new Parcel[6];
    private static final Parcel[] sOwnedPool = new Parcel[6];
    private ArrayMap<Class, Object> mClassCookies;
    private long mNativePtr;
    private long mNativeSize;
    private boolean mOwnsNativeParcelObject;
    private RuntimeException mStack;

    static native void clearFileDescriptor(FileDescriptor fileDescriptor);

    @Deprecated
    static native void closeFileDescriptor(FileDescriptor fileDescriptor) throws IOException;

    @Deprecated
    static native FileDescriptor dupFileDescriptor(FileDescriptor fileDescriptor) throws IOException;

    public static native long getGlobalAllocCount();

    public static native long getGlobalAllocSize();

    private static native long nativeAppendFrom(long j, long j2, int i, int i2);

    private static native int nativeCompareData(long j, long j2);

    private static native long nativeCreate();

    private static native byte[] nativeCreateByteArray(long j);

    private static native int nativeDataAvail(long j);

    private static native int nativeDataCapacity(long j);

    private static native int nativeDataPosition(long j);

    private static native int nativeDataSize(long j);

    private static native void nativeDestroy(long j);

    private static native void nativeEnforceInterface(long j, String str);

    private static native long nativeFreeBuffer(long j);

    private static native long nativeGetBlobAshmemSize(long j);

    private static native boolean nativeHasFileDescriptors(long j);

    private static native byte[] nativeMarshall(long j);

    private static native boolean nativePushAllowFds(long j, boolean z);

    private static native byte[] nativeReadBlob(long j);

    private static native double nativeReadDouble(long j);

    private static native FileDescriptor nativeReadFileDescriptor(long j);

    private static native float nativeReadFloat(long j);

    private static native int nativeReadInt(long j);

    private static native long nativeReadLong(long j);

    private static native String nativeReadString(long j);

    private static native IBinder nativeReadStrongBinder(long j);

    private static native void nativeRestoreAllowFds(long j, boolean z);

    private static native void nativeSetDataCapacity(long j, int i);

    private static native void nativeSetDataPosition(long j, int i);

    private static native long nativeSetDataSize(long j, int i);

    private static native long nativeUnmarshall(long j, byte[] bArr, int i, int i2);

    private static native void nativeWriteBlob(long j, byte[] bArr, int i, int i2);

    private static native void nativeWriteByteArray(long j, byte[] bArr, int i, int i2);

    private static native void nativeWriteDouble(long j, double d);

    private static native long nativeWriteFileDescriptor(long j, FileDescriptor fileDescriptor);

    private static native void nativeWriteFloat(long j, float f);

    private static native void nativeWriteInt(long j, int i);

    private static native void nativeWriteInterfaceToken(long j, String str);

    private static native void nativeWriteLong(long j, long j2);

    private static native void nativeWriteString(long j, String str);

    private static native void nativeWriteStrongBinder(long j, IBinder iBinder);

    @Deprecated
    static native FileDescriptor openFileDescriptor(String str, int i) throws FileNotFoundException;

    public static Parcel obtain() {
        Parcel[] pool = sOwnedPool;
        synchronized (pool) {
            for (int i = 0; i < 6; i++) {
                Parcel p = pool[i];
                if (p != null) {
                    pool[i] = null;
                    return p;
                }
            }
            return new Parcel(0);
        }
    }

    public final void recycle() {
        Parcel[] pool;
        freeBuffer();
        if (this.mOwnsNativeParcelObject) {
            pool = sOwnedPool;
        } else {
            this.mNativePtr = 0;
            pool = sHolderPool;
        }
        synchronized (pool) {
            for (int i = 0; i < 6; i++) {
                if (pool[i] == null) {
                    pool[i] = this;
                    return;
                }
            }
        }
    }

    public final int dataSize() {
        return nativeDataSize(this.mNativePtr);
    }

    public final int dataAvail() {
        return nativeDataAvail(this.mNativePtr);
    }

    public final int dataPosition() {
        return nativeDataPosition(this.mNativePtr);
    }

    public final int dataCapacity() {
        return nativeDataCapacity(this.mNativePtr);
    }

    public final void setDataSize(int size) {
        updateNativeSize(nativeSetDataSize(this.mNativePtr, size));
    }

    public final void setDataPosition(int pos) {
        nativeSetDataPosition(this.mNativePtr, pos);
    }

    public final void setDataCapacity(int size) {
        nativeSetDataCapacity(this.mNativePtr, size);
    }

    public final boolean pushAllowFds(boolean allowFds) {
        return nativePushAllowFds(this.mNativePtr, allowFds);
    }

    public final void restoreAllowFds(boolean lastValue) {
        nativeRestoreAllowFds(this.mNativePtr, lastValue);
    }

    public final byte[] marshall() {
        return nativeMarshall(this.mNativePtr);
    }

    public final void unmarshall(byte[] data, int offset, int length) {
        updateNativeSize(nativeUnmarshall(this.mNativePtr, data, offset, length));
    }

    public final void appendFrom(Parcel parcel, int offset, int length) {
        updateNativeSize(nativeAppendFrom(this.mNativePtr, parcel.mNativePtr, offset, length));
    }

    public final int compareData(Parcel other) {
        return nativeCompareData(this.mNativePtr, other.mNativePtr);
    }

    public final void setClassCookie(Class clz, Object cookie) {
        if (this.mClassCookies == null) {
            this.mClassCookies = new ArrayMap();
        }
        this.mClassCookies.put(clz, cookie);
    }

    public final Object getClassCookie(Class clz) {
        return this.mClassCookies != null ? this.mClassCookies.get(clz) : null;
    }

    public final void adoptClassCookies(Parcel from) {
        this.mClassCookies = from.mClassCookies;
    }

    public final boolean hasFileDescriptors() {
        return nativeHasFileDescriptors(this.mNativePtr);
    }

    public final void writeInterfaceToken(String interfaceName) {
        nativeWriteInterfaceToken(this.mNativePtr, interfaceName);
    }

    public final void enforceInterface(String interfaceName) {
        nativeEnforceInterface(this.mNativePtr, interfaceName);
    }

    public final void writeByteArray(byte[] b) {
        int length;
        if (b != null) {
            length = b.length;
        } else {
            length = 0;
        }
        writeByteArray(b, 0, length);
    }

    public final void writeByteArray(byte[] b, int offset, int len) {
        if (b == null) {
            writeInt(-1);
            return;
        }
        Arrays.checkOffsetAndCount(b.length, offset, len);
        nativeWriteByteArray(this.mNativePtr, b, offset, len);
    }

    public final void writeBlob(byte[] b) {
        int length;
        if (b != null) {
            length = b.length;
        } else {
            length = 0;
        }
        writeBlob(b, 0, length);
    }

    public final void writeBlob(byte[] b, int offset, int len) {
        if (b == null) {
            writeInt(-1);
            return;
        }
        Arrays.checkOffsetAndCount(b.length, offset, len);
        nativeWriteBlob(this.mNativePtr, b, offset, len);
    }

    public final void writeInt(int val) {
        nativeWriteInt(this.mNativePtr, val);
    }

    public final void writeLong(long val) {
        nativeWriteLong(this.mNativePtr, val);
    }

    public final void writeFloat(float val) {
        nativeWriteFloat(this.mNativePtr, val);
    }

    public final void writeDouble(double val) {
        nativeWriteDouble(this.mNativePtr, val);
    }

    public final void writeString(String val) {
        nativeWriteString(this.mNativePtr, val);
    }

    public final void writeBoolean(boolean val) {
        writeInt(val ? 1 : 0);
    }

    public final void writeCharSequence(CharSequence val) {
        TextUtils.writeToParcel(val, this, 0);
    }

    public final void writeStrongBinder(IBinder val) {
        nativeWriteStrongBinder(this.mNativePtr, val);
    }

    public final void writeStrongInterface(IInterface val) {
        IBinder iBinder = null;
        if (val != null) {
            iBinder = val.asBinder();
        }
        writeStrongBinder(iBinder);
    }

    public final void writeFileDescriptor(FileDescriptor val) {
        updateNativeSize(nativeWriteFileDescriptor(this.mNativePtr, val));
    }

    private void updateNativeSize(long newNativeSize) {
        if (this.mOwnsNativeParcelObject) {
            if (newNativeSize > 2147483647L) {
                newNativeSize = 2147483647L;
            }
            if (newNativeSize != this.mNativeSize) {
                int delta = (int) (newNativeSize - this.mNativeSize);
                if (delta > 0) {
                    VMRuntime.getRuntime().registerNativeAllocation(delta);
                } else {
                    VMRuntime.getRuntime().registerNativeFree(-delta);
                }
                this.mNativeSize = newNativeSize;
            }
        }
    }

    public final void writeRawFileDescriptor(FileDescriptor val) {
        nativeWriteFileDescriptor(this.mNativePtr, val);
    }

    public final void writeRawFileDescriptorArray(FileDescriptor[] value) {
        if (value != null) {
            writeInt(N);
            for (FileDescriptor writeRawFileDescriptor : value) {
                writeRawFileDescriptor(writeRawFileDescriptor);
            }
            return;
        }
        writeInt(-1);
    }

    public final void writeByte(byte val) {
        writeInt(val);
    }

    public final void writeMap(Map val) {
        writeMapInternal(val);
    }

    void writeMapInternal(Map<String, Object> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        Set<Entry<String, Object>> entries = val.entrySet();
        writeInt(entries.size());
        for (Entry<String, Object> e : entries) {
            writeValue(e.getKey());
            writeValue(e.getValue());
        }
    }

    void writeArrayMapInternal(ArrayMap<String, Object> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        for (int i = 0; i < N; i++) {
            writeString((String) val.keyAt(i));
            writeValue(val.valueAt(i));
        }
    }

    public void writeArrayMap(ArrayMap<String, Object> val) {
        writeArrayMapInternal(val);
    }

    public void writeArraySet(ArraySet<? extends Object> val) {
        int size = val != null ? val.size() : -1;
        writeInt(size);
        for (int i = 0; i < size; i++) {
            writeValue(val.valueAt(i));
        }
    }

    public final void writeBundle(Bundle val) {
        if (val == null) {
            writeInt(-1);
        } else {
            val.writeToParcel(this, 0);
        }
    }

    public final void writePersistableBundle(PersistableBundle val) {
        if (val == null) {
            writeInt(-1);
        } else {
            val.writeToParcel(this, 0);
        }
    }

    public final void writeSize(Size val) {
        writeInt(val.getWidth());
        writeInt(val.getHeight());
    }

    public final void writeSizeF(SizeF val) {
        writeFloat(val.getWidth());
        writeFloat(val.getHeight());
    }

    public final void writeList(List val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        for (int i = 0; i < N; i++) {
            writeValue(val.get(i));
        }
    }

    public final void writeArray(Object[] val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        writeInt(N);
        for (Object writeValue : val) {
            writeValue(writeValue);
        }
    }

    public final void writeSparseArray(SparseArray<Object> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        for (int i = 0; i < N; i++) {
            writeInt(val.keyAt(i));
            writeValue(val.valueAt(i));
        }
    }

    public final void writeSparseBooleanArray(SparseBooleanArray val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        for (int i = 0; i < N; i++) {
            writeInt(val.keyAt(i));
            writeByte((byte) (val.valueAt(i) ? 1 : 0));
        }
    }

    public final void writeSparseIntArray(SparseIntArray val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        for (int i = 0; i < N; i++) {
            writeInt(val.keyAt(i));
            writeInt(val.valueAt(i));
        }
    }

    public final void writeBooleanArray(boolean[] val) {
        if (val != null) {
            writeInt(N);
            for (boolean z : val) {
                writeInt(z ? 1 : 0);
            }
            return;
        }
        writeInt(-1);
    }

    public final boolean[] createBooleanArray() {
        int N = readInt();
        if (N < 0 || N > (dataAvail() >> 2)) {
            return null;
        }
        boolean[] val = new boolean[N];
        for (int i = 0; i < N; i++) {
            boolean z;
            if (readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            val[i] = z;
        }
        return val;
    }

    public final void readBooleanArray(boolean[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                boolean z;
                if (readInt() != 0) {
                    z = true;
                } else {
                    z = false;
                }
                val[i] = z;
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final void writeCharArray(char[] val) {
        if (val != null) {
            writeInt(N);
            for (char writeInt : val) {
                writeInt(writeInt);
            }
            return;
        }
        writeInt(-1);
    }

    public final char[] createCharArray() {
        int N = readInt();
        if (N < 0 || N > (dataAvail() >> 2)) {
            return null;
        }
        char[] val = new char[N];
        for (int i = 0; i < N; i++) {
            val[i] = (char) readInt();
        }
        return val;
    }

    public final void readCharArray(char[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                val[i] = (char) readInt();
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final void writeIntArray(int[] val) {
        if (val != null) {
            writeInt(N);
            for (int writeInt : val) {
                writeInt(writeInt);
            }
            return;
        }
        writeInt(-1);
    }

    public final int[] createIntArray() {
        int N = readInt();
        if (N < 0 || N > (dataAvail() >> 2)) {
            return null;
        }
        int[] val = new int[N];
        for (int i = 0; i < N; i++) {
            val[i] = readInt();
        }
        return val;
    }

    public final void readIntArray(int[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                val[i] = readInt();
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final void writeLongArray(long[] val) {
        if (val != null) {
            writeInt(N);
            for (long writeLong : val) {
                writeLong(writeLong);
            }
            return;
        }
        writeInt(-1);
    }

    public final long[] createLongArray() {
        int N = readInt();
        if (N < 0 || N > (dataAvail() >> 3)) {
            return null;
        }
        long[] val = new long[N];
        for (int i = 0; i < N; i++) {
            val[i] = readLong();
        }
        return val;
    }

    public final void readLongArray(long[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                val[i] = readLong();
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final void writeFloatArray(float[] val) {
        if (val != null) {
            writeInt(N);
            for (float writeFloat : val) {
                writeFloat(writeFloat);
            }
            return;
        }
        writeInt(-1);
    }

    public final float[] createFloatArray() {
        int N = readInt();
        if (N < 0 || N > (dataAvail() >> 2)) {
            return null;
        }
        float[] val = new float[N];
        for (int i = 0; i < N; i++) {
            val[i] = readFloat();
        }
        return val;
    }

    public final void readFloatArray(float[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                val[i] = readFloat();
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final void writeDoubleArray(double[] val) {
        if (val != null) {
            writeInt(N);
            for (double writeDouble : val) {
                writeDouble(writeDouble);
            }
            return;
        }
        writeInt(-1);
    }

    public final double[] createDoubleArray() {
        int N = readInt();
        if (N < 0 || N > (dataAvail() >> 3)) {
            return null;
        }
        double[] val = new double[N];
        for (int i = 0; i < N; i++) {
            val[i] = readDouble();
        }
        return val;
    }

    public final void readDoubleArray(double[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                val[i] = readDouble();
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final void writeStringArray(String[] val) {
        if (val != null) {
            writeInt(N);
            for (String writeString : val) {
                writeString(writeString);
            }
            return;
        }
        writeInt(-1);
    }

    public final String[] createStringArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        String[] val = new String[N];
        for (int i = 0; i < N; i++) {
            val[i] = readString();
        }
        return val;
    }

    public final void readStringArray(String[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                val[i] = readString();
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final void writeBinderArray(IBinder[] val) {
        if (val != null) {
            writeInt(N);
            for (IBinder writeStrongBinder : val) {
                writeStrongBinder(writeStrongBinder);
            }
            return;
        }
        writeInt(-1);
    }

    public final void writeCharSequenceArray(CharSequence[] val) {
        if (val != null) {
            writeInt(N);
            for (CharSequence writeCharSequence : val) {
                writeCharSequence(writeCharSequence);
            }
            return;
        }
        writeInt(-1);
    }

    public final void writeCharSequenceList(ArrayList<CharSequence> val) {
        if (val != null) {
            int N = val.size();
            writeInt(N);
            for (int i = 0; i < N; i++) {
                writeCharSequence((CharSequence) val.get(i));
            }
            return;
        }
        writeInt(-1);
    }

    public final IBinder[] createBinderArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        IBinder[] val = new IBinder[N];
        for (int i = 0; i < N; i++) {
            val[i] = readStrongBinder();
        }
        return val;
    }

    public final void readBinderArray(IBinder[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                val[i] = readStrongBinder();
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final <T extends Parcelable> void writeTypedList(List<T> val) {
        writeTypedList(val, 0);
    }

    public <T extends Parcelable> void writeTypedList(List<T> val, int parcelableFlags) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        for (int i = 0; i < N; i++) {
            writeTypedObject((Parcelable) val.get(i), parcelableFlags);
        }
    }

    public final void writeStringList(List<String> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        for (int i = 0; i < N; i++) {
            writeString((String) val.get(i));
        }
    }

    public final void writeBinderList(List<IBinder> val) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        for (int i = 0; i < N; i++) {
            writeStrongBinder((IBinder) val.get(i));
        }
    }

    public final <T extends Parcelable> void writeParcelableList(List<T> val, int flags) {
        if (val == null) {
            writeInt(-1);
            return;
        }
        int N = val.size();
        writeInt(N);
        for (int i = 0; i < N; i++) {
            writeParcelable((Parcelable) val.get(i), flags);
        }
    }

    public final <T extends Parcelable> void writeTypedArray(T[] val, int parcelableFlags) {
        if (val != null) {
            writeInt(N);
            for (T item : val) {
                if (item != null) {
                    writeInt(1);
                    item.writeToParcel(this, parcelableFlags);
                } else {
                    writeInt(0);
                }
            }
            return;
        }
        writeInt(-1);
    }

    public final <T extends Parcelable> void writeTypedArrayList(ArrayList<T> list, int parcelableFlags) {
        if (list != null) {
            int N = list.size();
            writeInt(N);
            boolean wroteCreator = false;
            for (int i = 0; i < N; i++) {
                Parcelable item = (Parcelable) list.get(i);
                if (item != null) {
                    writeInt(1);
                    if (!wroteCreator) {
                        writeParcelableCreator(item);
                        wroteCreator = true;
                    }
                    item.writeToParcel(this, parcelableFlags);
                } else {
                    writeInt(0);
                }
            }
            return;
        }
        writeInt(-1);
    }

    public final <T> ArrayList<T> readTypedArrayList(ClassLoader loader) {
        int N = readInt();
        if (N <= 0) {
            return null;
        }
        Creator<?> creator = null;
        ArrayList<T> result = new ArrayList(N);
        for (int i = 0; i < N; i++) {
            if (readInt() != 0) {
                T parcelable;
                if (creator == null) {
                    creator = readParcelableCreator(loader);
                    if (creator == null) {
                        return null;
                    }
                }
                if (creator instanceof ClassLoaderCreator) {
                    parcelable = ((ClassLoaderCreator) creator).createFromParcel(this, loader);
                } else {
                    parcelable = creator.createFromParcel(this);
                }
                result.add(parcelable);
            } else {
                result.add(null);
            }
        }
        return result;
    }

    public final <T extends Parcelable> void writeTypedArraySet(ArraySet<T> set, int parcelableFlags) {
        if (set != null) {
            int N = set.size();
            writeInt(N);
            boolean wroteCreator = false;
            for (int i = 0; i < N; i++) {
                Parcelable item = (Parcelable) set.valueAt(i);
                if (item != null) {
                    writeInt(1);
                    if (!wroteCreator) {
                        writeParcelableCreator(item);
                        wroteCreator = true;
                    }
                    item.writeToParcel(this, parcelableFlags);
                } else {
                    writeInt(0);
                }
            }
            return;
        }
        writeInt(-1);
    }

    public final <T> ArraySet<T> readTypedArraySet(ClassLoader loader) {
        int N = readInt();
        if (N <= 0) {
            return null;
        }
        Creator<?> creator = null;
        ArraySet<T> result = new ArraySet(N);
        for (int i = 0; i < N; i++) {
            T parcelable = null;
            if (readInt() != 0) {
                if (creator == null) {
                    creator = readParcelableCreator(loader);
                    if (creator == null) {
                        return null;
                    }
                }
                if (creator instanceof ClassLoaderCreator) {
                    parcelable = ((ClassLoaderCreator) creator).createFromParcel(this, loader);
                } else {
                    parcelable = creator.createFromParcel(this);
                }
            }
            result.append(parcelable);
        }
        return result;
    }

    public final <T extends Parcelable> void writeTypedObject(T val, int parcelableFlags) {
        if (val != null) {
            writeInt(1);
            val.writeToParcel(this, parcelableFlags);
            return;
        }
        writeInt(0);
    }

    public final void writeValue(Object v) {
        int i = 1;
        if (v == null) {
            writeInt(-1);
        } else if (v instanceof String) {
            writeInt(0);
            writeString((String) v);
        } else if (v instanceof Integer) {
            writeInt(1);
            writeInt(((Integer) v).intValue());
        } else if (v instanceof Map) {
            writeInt(2);
            writeMap((Map) v);
        } else if (v instanceof Bundle) {
            writeInt(3);
            writeBundle((Bundle) v);
        } else if (v instanceof PersistableBundle) {
            writeInt(25);
            writePersistableBundle((PersistableBundle) v);
        } else if (v instanceof Parcelable) {
            writeInt(4);
            writeParcelable((Parcelable) v, 0);
        } else if (v instanceof Short) {
            writeInt(5);
            writeInt(((Short) v).intValue());
        } else if (v instanceof Long) {
            writeInt(6);
            writeLong(((Long) v).longValue());
        } else if (v instanceof Float) {
            writeInt(7);
            writeFloat(((Float) v).floatValue());
        } else if (v instanceof Double) {
            writeInt(8);
            writeDouble(((Double) v).doubleValue());
        } else if (v instanceof Boolean) {
            writeInt(9);
            if (!((Boolean) v).booleanValue()) {
                i = 0;
            }
            writeInt(i);
        } else if (v instanceof CharSequence) {
            writeInt(10);
            writeCharSequence((CharSequence) v);
        } else if (v instanceof List) {
            writeInt(11);
            writeList((List) v);
        } else if (v instanceof SparseArray) {
            writeInt(12);
            writeSparseArray((SparseArray) v);
        } else if (v instanceof boolean[]) {
            writeInt(23);
            writeBooleanArray((boolean[]) v);
        } else if (v instanceof byte[]) {
            writeInt(13);
            writeByteArray((byte[]) v);
        } else if (v instanceof String[]) {
            writeInt(14);
            writeStringArray((String[]) v);
        } else if (v instanceof CharSequence[]) {
            writeInt(24);
            writeCharSequenceArray((CharSequence[]) v);
        } else if (v instanceof IBinder) {
            writeInt(15);
            writeStrongBinder((IBinder) v);
        } else if (v instanceof Parcelable[]) {
            writeInt(16);
            writeParcelableArray((Parcelable[]) v, 0);
        } else if (v instanceof int[]) {
            writeInt(18);
            writeIntArray((int[]) v);
        } else if (v instanceof long[]) {
            writeInt(19);
            writeLongArray((long[]) v);
        } else if (v instanceof Byte) {
            writeInt(20);
            writeInt(((Byte) v).byteValue());
        } else if (v instanceof Size) {
            writeInt(26);
            writeSize((Size) v);
        } else if (v instanceof SizeF) {
            writeInt(27);
            writeSizeF((SizeF) v);
        } else if (v instanceof double[]) {
            writeInt(28);
            writeDoubleArray((double[]) v);
        } else {
            Class<?> clazz = v.getClass();
            if (clazz.isArray() && clazz.getComponentType() == Object.class) {
                writeInt(17);
                writeArray((Object[]) v);
            } else if (v instanceof Serializable) {
                writeInt(21);
                writeSerializable((Serializable) v);
            } else {
                throw new RuntimeException("Parcel: unable to marshal value " + v);
            }
        }
    }

    public final void writeParcelable(Parcelable p, int parcelableFlags) {
        if (p == null) {
            writeString(null);
            return;
        }
        writeParcelableCreator(p);
        p.writeToParcel(this, parcelableFlags);
    }

    public final void writeParcelableCreator(Parcelable p) {
        writeString(p.getClass().getName());
    }

    public final void writeSerializable(Serializable s) {
        if (s == null) {
            writeString(null);
            return;
        }
        String name = s.getClass().getName();
        writeString(name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(s);
            oos.close();
            writeByteArray(baos.toByteArray());
        } catch (IOException ioe) {
            throw new RuntimeException("Parcelable encountered IOException writing serializable object (name = " + name + ")", ioe);
        }
    }

    public final void writeException(Exception e) {
        int code = 0;
        if ((e instanceof Parcelable) && e.getClass().getClassLoader() == Parcelable.class.getClassLoader()) {
            code = -9;
        } else if (e instanceof SecurityException) {
            code = -1;
        } else if (e instanceof BadParcelableException) {
            code = -2;
        } else if (e instanceof IllegalArgumentException) {
            code = -3;
        } else if (e instanceof NullPointerException) {
            code = -4;
        } else if (e instanceof IllegalStateException) {
            code = -5;
        } else if (e instanceof NetworkOnMainThreadException) {
            code = -6;
        } else if (e instanceof UnsupportedOperationException) {
            code = -7;
        } else if (e instanceof ServiceSpecificException) {
            code = -8;
        }
        writeInt(code);
        StrictMode.clearGatheredViolations();
        if (code != 0) {
            e.printStackTrace();
            writeString(e.getMessage());
            switch (code) {
                case -9:
                    int sizePosition = dataPosition();
                    writeInt(0);
                    writeParcelable((Parcelable) e, 1);
                    int payloadPosition = dataPosition();
                    setDataPosition(sizePosition);
                    writeInt(payloadPosition - sizePosition);
                    setDataPosition(payloadPosition);
                    return;
                case -8:
                    writeInt(((ServiceSpecificException) e).errorCode);
                    return;
                default:
                    return;
            }
        } else if (e instanceof RuntimeException) {
            throw ((RuntimeException) e);
        } else {
            throw new RuntimeException(e);
        }
    }

    public final void writeNoException() {
        if (StrictMode.hasGatheredViolations()) {
            writeInt(-128);
            int sizePosition = dataPosition();
            writeInt(0);
            StrictMode.writeGatheredViolationsToParcel(this);
            int payloadPosition = dataPosition();
            setDataPosition(sizePosition);
            writeInt(payloadPosition - sizePosition);
            setDataPosition(payloadPosition);
            return;
        }
        writeInt(0);
    }

    public final void readException() {
        int code = readExceptionCode();
        if (code != 0) {
            readException(code, readString());
        }
    }

    public final int readExceptionCode() {
        int code = readInt();
        if (code != -128) {
            return code;
        }
        if (readInt() == 0) {
            Log.e(TAG, "Unexpected zero-sized Parcel reply header.");
        } else {
            StrictMode.readAndHandleBinderCallViolations(this);
        }
        return 0;
    }

    public final void readException(int code, String msg) {
        switch (code) {
            case -9:
                if (readInt() > 0) {
                    SneakyThrow.sneakyThrow((Exception) readParcelable(Parcelable.class.getClassLoader()));
                    break;
                }
                throw new RuntimeException(msg + " [missing Parcelable]");
            case -8:
                throw new ServiceSpecificException(readInt(), msg);
            case -7:
                throw new UnsupportedOperationException(msg);
            case -6:
                throw new NetworkOnMainThreadException();
            case -5:
                throw new IllegalStateException(msg);
            case -4:
                throw new NullPointerException(msg);
            case -3:
                throw new IllegalArgumentException(msg);
            case -2:
                throw new BadParcelableException(msg);
            case -1:
                break;
            default:
                throw new RuntimeException("Unknown exception code: " + code + " msg " + msg);
        }
        throw new SecurityException(msg);
    }

    public final int readInt() {
        return nativeReadInt(this.mNativePtr);
    }

    public final long readLong() {
        return nativeReadLong(this.mNativePtr);
    }

    public final float readFloat() {
        return nativeReadFloat(this.mNativePtr);
    }

    public final double readDouble() {
        return nativeReadDouble(this.mNativePtr);
    }

    public final String readString() {
        return nativeReadString(this.mNativePtr);
    }

    public final boolean readBoolean() {
        return readInt() != 0;
    }

    public final CharSequence readCharSequence() {
        return (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(this);
    }

    public final IBinder readStrongBinder() {
        return nativeReadStrongBinder(this.mNativePtr);
    }

    public final ParcelFileDescriptor readFileDescriptor() {
        FileDescriptor fd = nativeReadFileDescriptor(this.mNativePtr);
        if (fd != null) {
            return new ParcelFileDescriptor(fd);
        }
        return null;
    }

    public final FileDescriptor readRawFileDescriptor() {
        return nativeReadFileDescriptor(this.mNativePtr);
    }

    public final FileDescriptor[] createRawFileDescriptorArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        FileDescriptor[] f = new FileDescriptor[N];
        for (int i = 0; i < N; i++) {
            f[i] = readRawFileDescriptor();
        }
        return f;
    }

    public final void readRawFileDescriptorArray(FileDescriptor[] val) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                val[i] = readRawFileDescriptor();
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final byte readByte() {
        return (byte) (readInt() & 255);
    }

    public final void readMap(Map outVal, ClassLoader loader) {
        readMapInternal(outVal, readInt(), loader);
    }

    public final void readList(List outVal, ClassLoader loader) {
        readListInternal(outVal, readInt(), loader);
    }

    public final HashMap readHashMap(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        HashMap m = new HashMap(N);
        readMapInternal(m, N, loader);
        return m;
    }

    public final Bundle readBundle() {
        return readBundle(null);
    }

    public final Bundle readBundle(ClassLoader loader) {
        int length = readInt();
        if (length < 0) {
            return null;
        }
        Bundle bundle = new Bundle(this, length);
        if (loader != null) {
            bundle.setClassLoader(loader);
        }
        return bundle;
    }

    public final PersistableBundle readPersistableBundle() {
        return readPersistableBundle(null);
    }

    public final PersistableBundle readPersistableBundle(ClassLoader loader) {
        int length = readInt();
        if (length < 0) {
            return null;
        }
        PersistableBundle bundle = new PersistableBundle(this, length);
        if (loader != null) {
            bundle.setClassLoader(loader);
        }
        return bundle;
    }

    public final Size readSize() {
        return new Size(readInt(), readInt());
    }

    public final SizeF readSizeF() {
        return new SizeF(readFloat(), readFloat());
    }

    public final byte[] createByteArray() {
        return nativeCreateByteArray(this.mNativePtr);
    }

    public final void readByteArray(byte[] val) {
        byte[] ba = createByteArray();
        if (ba.length == val.length) {
            System.arraycopy(ba, 0, val, 0, ba.length);
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    public final byte[] readBlob() {
        return nativeReadBlob(this.mNativePtr);
    }

    public final String[] readStringArray() {
        String[] array = null;
        int length = readInt();
        if (length >= 0) {
            array = new String[length];
            for (int i = 0; i < length; i++) {
                array[i] = readString();
            }
        }
        return array;
    }

    public final CharSequence[] readCharSequenceArray() {
        CharSequence[] array = null;
        int length = readInt();
        if (length >= 0) {
            array = new CharSequence[length];
            for (int i = 0; i < length; i++) {
                array[i] = readCharSequence();
            }
        }
        return array;
    }

    public final ArrayList<CharSequence> readCharSequenceList() {
        ArrayList<CharSequence> array = null;
        int length = readInt();
        if (length >= 0) {
            array = new ArrayList(length);
            for (int i = 0; i < length; i++) {
                array.add(readCharSequence());
            }
        }
        return array;
    }

    public final ArrayList readArrayList(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList l = new ArrayList(N);
        readListInternal(l, N, loader);
        return l;
    }

    public final Object[] readArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        Object[] l = new Object[N];
        readArrayInternal(l, N, loader);
        return l;
    }

    public final SparseArray readSparseArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        SparseArray sa = new SparseArray(N);
        readSparseArrayInternal(sa, N, loader);
        return sa;
    }

    public final SparseBooleanArray readSparseBooleanArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        SparseBooleanArray sa = new SparseBooleanArray(N);
        readSparseBooleanArrayInternal(sa, N);
        return sa;
    }

    public final SparseIntArray readSparseIntArray() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        SparseIntArray sa = new SparseIntArray(N);
        readSparseIntArrayInternal(sa, N);
        return sa;
    }

    public final <T> ArrayList<T> createTypedArrayList(Creator<T> c) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList<T> l = new ArrayList(N);
        while (N > 0) {
            if (readInt() != 0) {
                l.add(c.createFromParcel(this));
            } else {
                l.add(null);
            }
            N--;
        }
        return l;
    }

    public final <T> void readTypedList(List<T> list, Creator<T> c) {
        int M = list.size();
        int N = readInt();
        int i = 0;
        while (i < M && i < N) {
            if (readInt() != 0) {
                list.set(i, c.createFromParcel(this));
            } else {
                list.set(i, null);
            }
            i++;
        }
        while (i < N) {
            if (readInt() != 0) {
                list.add(c.createFromParcel(this));
            } else {
                list.add(null);
            }
            i++;
        }
        while (i < M) {
            list.remove(N);
            i++;
        }
    }

    public final ArrayList<String> createStringArrayList() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList<String> l = new ArrayList(N);
        while (N > 0) {
            l.add(readString());
            N--;
        }
        return l;
    }

    public final ArrayList<IBinder> createBinderArrayList() {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        ArrayList<IBinder> l = new ArrayList(N);
        while (N > 0) {
            l.add(readStrongBinder());
            N--;
        }
        return l;
    }

    public final void readStringList(List<String> list) {
        int M = list.size();
        int N = readInt();
        int i = 0;
        while (i < M && i < N) {
            list.set(i, readString());
            i++;
        }
        while (i < N) {
            list.add(readString());
            i++;
        }
        while (i < M) {
            list.remove(N);
            i++;
        }
    }

    public final void readBinderList(List<IBinder> list) {
        int M = list.size();
        int N = readInt();
        int i = 0;
        while (i < M && i < N) {
            list.set(i, readStrongBinder());
            i++;
        }
        while (i < N) {
            list.add(readStrongBinder());
            i++;
        }
        while (i < M) {
            list.remove(N);
            i++;
        }
    }

    public final <T extends Parcelable> List<T> readParcelableList(List<T> list, ClassLoader cl) {
        int N = readInt();
        if (N == -1) {
            list.clear();
            return list;
        }
        int M = list.size();
        int i = 0;
        while (i < M && i < N) {
            list.set(i, readParcelable(cl));
            i++;
        }
        while (i < N) {
            list.add(readParcelable(cl));
            i++;
        }
        while (i < M) {
            list.remove(N);
            i++;
        }
        return list;
    }

    public final <T> T[] createTypedArray(Creator<T> c) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        T[] l = c.newArray(N);
        for (int i = 0; i < N; i++) {
            if (readInt() != 0) {
                l[i] = c.createFromParcel(this);
            }
        }
        return l;
    }

    public final <T> void readTypedArray(T[] val, Creator<T> c) {
        int N = readInt();
        if (N == val.length) {
            for (int i = 0; i < N; i++) {
                if (readInt() != 0) {
                    val[i] = c.createFromParcel(this);
                } else {
                    val[i] = null;
                }
            }
            return;
        }
        throw new RuntimeException("bad array lengths");
    }

    @Deprecated
    public final <T> T[] readTypedArray(Creator<T> c) {
        return createTypedArray(c);
    }

    public final <T> T readTypedObject(Creator<T> c) {
        if (readInt() != 0) {
            return c.createFromParcel(this);
        }
        return null;
    }

    public final <T extends Parcelable> void writeParcelableArray(T[] value, int parcelableFlags) {
        if (value != null) {
            writeInt(N);
            for (Parcelable writeParcelable : value) {
                writeParcelable(writeParcelable, parcelableFlags);
            }
            return;
        }
        writeInt(-1);
    }

    public final Object readValue(ClassLoader loader) {
        boolean z = true;
        int type = readInt();
        switch (type) {
            case -1:
                return null;
            case 0:
                return readString();
            case 1:
                return Integer.valueOf(readInt());
            case 2:
                return readHashMap(loader);
            case 3:
                return readBundle(loader);
            case 4:
                return readParcelable(loader);
            case 5:
                return Short.valueOf((short) readInt());
            case 6:
                return Long.valueOf(readLong());
            case 7:
                return Float.valueOf(readFloat());
            case 8:
                return Double.valueOf(readDouble());
            case 9:
                if (readInt() != 1) {
                    z = false;
                }
                return Boolean.valueOf(z);
            case 10:
                return readCharSequence();
            case 11:
                return readArrayList(loader);
            case 12:
                return readSparseArray(loader);
            case 13:
                return createByteArray();
            case 14:
                return readStringArray();
            case 15:
                return readStrongBinder();
            case 16:
                return readParcelableArray(loader);
            case 17:
                return readArray(loader);
            case 18:
                return createIntArray();
            case 19:
                return createLongArray();
            case 20:
                return Byte.valueOf(readByte());
            case 21:
                return readSerializable(loader);
            case 22:
                return readSparseBooleanArray();
            case 23:
                return createBooleanArray();
            case 24:
                return readCharSequenceArray();
            case 25:
                return readPersistableBundle(loader);
            case 26:
                return readSize();
            case 27:
                return readSizeF();
            case 28:
                return createDoubleArray();
            default:
                throw new RuntimeException("Parcel " + this + ": Unmarshalling unknown type code " + type + " at offset " + (dataPosition() - 4));
        }
    }

    public final <T extends Parcelable> T readParcelable(ClassLoader loader) {
        Creator<?> creator = readParcelableCreator(loader);
        if (creator == null) {
            return null;
        }
        if (creator instanceof ClassLoaderCreator) {
            return (Parcelable) ((ClassLoaderCreator) creator).createFromParcel(this, loader);
        }
        return (Parcelable) creator.createFromParcel(this);
    }

    public final <T extends Parcelable> T readCreator(Creator<?> creator, ClassLoader loader) {
        if (creator instanceof ClassLoaderCreator) {
            return (Parcelable) ((ClassLoaderCreator) creator).createFromParcel(this, loader);
        }
        return (Parcelable) creator.createFromParcel(this);
    }

    public final Creator<?> readParcelableCreator(ClassLoader loader) {
        String name = readString();
        if (name == null) {
            return null;
        }
        Creator<?> creator;
        synchronized (mCreators) {
            HashMap<String, Creator<?>> map = (HashMap) mCreators.get(loader);
            if (map == null) {
                map = new HashMap();
                mCreators.put(loader, map);
            }
            creator = (Creator) map.get(name);
            if (creator == null) {
                ClassLoader parcelableClassLoader;
                if (loader == null) {
                    try {
                        parcelableClassLoader = getClass().getClassLoader();
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, "Illegal access when unmarshalling: " + name, e);
                        throw new BadParcelableException("IllegalAccessException when unmarshalling: " + name);
                    } catch (ClassNotFoundException e2) {
                        Log.e(TAG, "Class not found when unmarshalling: " + name, e2);
                        throw new BadParcelableException("ClassNotFoundException when unmarshalling: " + name);
                    } catch (NoSuchFieldException e3) {
                        throw new BadParcelableException("Parcelable protocol requires a Parcelable.Creator object called CREATOR on class " + name);
                    }
                }
                parcelableClassLoader = loader;
                Class<?> parcelableClass = Class.forName(name, false, parcelableClassLoader);
                if (Parcelable.class.isAssignableFrom(parcelableClass)) {
                    Field f = parcelableClass.getField("CREATOR");
                    if ((f.getModifiers() & 8) == 0) {
                        throw new BadParcelableException("Parcelable protocol requires the CREATOR object to be static on class " + name);
                    }
                    if (Creator.class.isAssignableFrom(f.getType())) {
                        creator = (Creator) f.get(null);
                        if (creator == null) {
                            throw new BadParcelableException("Parcelable protocol requires a non-null Parcelable.Creator object called CREATOR on class " + name);
                        }
                        map.put(name, creator);
                    } else {
                        throw new BadParcelableException("Parcelable protocol requires a Parcelable.Creator object called CREATOR on class " + name);
                    }
                }
                throw new BadParcelableException("Parcelable protocol requires that the class implements Parcelable");
            }
        }
        return creator;
    }

    public final Parcelable[] readParcelableArray(ClassLoader loader) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        Parcelable[] p = new Parcelable[N];
        for (int i = 0; i < N; i++) {
            p[i] = readParcelable(loader);
        }
        return p;
    }

    public final <T extends Parcelable> T[] readParcelableArray(ClassLoader loader, Class<T> clazz) {
        int N = readInt();
        if (N < 0) {
            return null;
        }
        Parcelable[] p = (Parcelable[]) Array.newInstance(clazz, N);
        for (int i = 0; i < N; i++) {
            p[i] = readParcelable(loader);
        }
        return p;
    }

    public final Serializable readSerializable() {
        return readSerializable(null);
    }

    private final Serializable readSerializable(final ClassLoader loader) {
        String name = readString();
        if (name == null) {
            return null;
        }
        try {
            return (Serializable) new ObjectInputStream(new ByteArrayInputStream(createByteArray())) {
                protected Class<?> resolveClass(ObjectStreamClass osClass) throws IOException, ClassNotFoundException {
                    if (loader != null) {
                        Class<?> c = Class.forName(osClass.getName(), false, loader);
                        if (c != null) {
                            return c;
                        }
                    }
                    return super.resolveClass(osClass);
                }
            }.readObject();
        } catch (IOException ioe) {
            throw new RuntimeException("Parcelable encountered IOException reading a Serializable object (name = " + name + ")", ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Parcelable encountered ClassNotFoundException reading a Serializable object (name = " + name + ")", cnfe);
        }
    }

    protected static final Parcel obtain(int obj) {
        throw new UnsupportedOperationException();
    }

    protected static final Parcel obtain(long obj) {
        Parcel[] pool = sHolderPool;
        synchronized (pool) {
            for (int i = 0; i < 6; i++) {
                Parcel p = pool[i];
                if (p != null) {
                    pool[i] = null;
                    p.init(obj);
                    return p;
                }
            }
            return new Parcel(obj);
        }
    }

    private Parcel(long nativePtr) {
        init(nativePtr);
    }

    private void init(long nativePtr) {
        if (nativePtr != 0) {
            this.mNativePtr = nativePtr;
            this.mOwnsNativeParcelObject = false;
            return;
        }
        this.mNativePtr = nativeCreate();
        this.mOwnsNativeParcelObject = true;
    }

    private void freeBuffer() {
        if (this.mOwnsNativeParcelObject) {
            updateNativeSize(nativeFreeBuffer(this.mNativePtr));
        }
    }

    private void destroy() {
        if (this.mNativePtr != 0) {
            if (this.mOwnsNativeParcelObject) {
                nativeDestroy(this.mNativePtr);
                updateNativeSize(0);
            }
            this.mNativePtr = 0;
        }
    }

    protected void finalize() throws Throwable {
        destroy();
    }

    void readMapInternal(Map outVal, int N, ClassLoader loader) {
        while (N > 0) {
            outVal.put(readValue(loader), readValue(loader));
            N--;
        }
    }

    void readArrayMapInternal(ArrayMap outVal, int N, ClassLoader loader) {
        while (N > 0) {
            outVal.append(readString(), readValue(loader));
            N--;
        }
        outVal.validate();
    }

    void readArrayMapSafelyInternal(ArrayMap outVal, int N, ClassLoader loader) {
        while (N > 0) {
            outVal.put(readString(), readValue(loader));
            N--;
        }
    }

    public void readArrayMap(ArrayMap outVal, ClassLoader loader) {
        int N = readInt();
        if (N >= 0) {
            readArrayMapInternal(outVal, N, loader);
        }
    }

    public ArraySet<? extends Object> readArraySet(ClassLoader loader) {
        int size = readInt();
        if (size < 0) {
            return null;
        }
        ArraySet<Object> result = new ArraySet(size);
        for (int i = 0; i < size; i++) {
            result.append(readValue(loader));
        }
        return result;
    }

    private void readListInternal(List outVal, int N, ClassLoader loader) {
        while (N > 0) {
            outVal.add(readValue(loader));
            N--;
        }
    }

    private void readArrayInternal(Object[] outVal, int N, ClassLoader loader) {
        for (int i = 0; i < N; i++) {
            outVal[i] = readValue(loader);
        }
    }

    private void readSparseArrayInternal(SparseArray outVal, int N, ClassLoader loader) {
        while (N > 0) {
            outVal.append(readInt(), readValue(loader));
            N--;
        }
    }

    private void readSparseBooleanArrayInternal(SparseBooleanArray outVal, int N) {
        while (N > 0) {
            outVal.append(readInt(), readByte() == (byte) 1);
            N--;
        }
    }

    private void readSparseIntArrayInternal(SparseIntArray outVal, int N) {
        while (N > 0) {
            outVal.append(readInt(), readInt());
            N--;
        }
    }

    public long getBlobAshmemSize() {
        return nativeGetBlobAshmemSize(this.mNativePtr);
    }
}
