package ohos.utils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.interwork.utils.PacMapEx;
import ohos.interwork.utils.ParcelUtilsEx;
import ohos.interwork.utils.ParcelableEx;
import ohos.utils.Sequenceable;

public class Parcel {
    private static final int INVALID = -1;
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218119424, LOG_TAG);
    private static final String LOG_TAG = "Parcel";
    private static final int POOL_SIZE = 8;
    private static final Parcel[] pool = new Parcel[8];
    private List<ClassLoader> classLoaders = new ArrayList();
    private boolean isOwner;
    private long mNativeHandle;

    private static native long nativeConstruct();

    private static native void nativeDestruct(long j);

    private static native void nativeFlushBuffer(long j);

    private static native int nativeGetCapacity(long j);

    private static native int nativeGetReadPosition(long j);

    private static native int nativeGetReadableBytes(long j);

    private static native int nativeGetSize(long j);

    private static native int nativeGetWritableBytes(long j);

    private static native int nativeGetWritePosition(long j);

    private static native boolean nativeReadBoolean(long j);

    private static native byte nativeReadByte(long j);

    private static native boolean nativeReadByteArray(long j, byte[] bArr, int i);

    private static native char nativeReadChar(long j);

    private static native double nativeReadDouble(long j);

    private static native float nativeReadFloat(long j);

    private static native int nativeReadInt(long j);

    private static native long nativeReadLong(long j);

    private static native short nativeReadShort(long j);

    private static native String nativeReadString(long j);

    private static native boolean nativeRewindRead(long j, int i);

    private static native boolean nativeRewindWrite(long j, int i);

    private static native boolean nativeSetCapacity(long j, int i);

    private static native boolean nativeSetSize(long j, int i);

    private static native boolean nativeWriteBoolean(long j, boolean z);

    private static native boolean nativeWriteByte(long j, byte b);

    private static native boolean nativeWriteByteArray(long j, byte[] bArr, int i);

    private static native boolean nativeWriteChar(long j, char c);

    private static native boolean nativeWriteDouble(long j, double d);

    private static native boolean nativeWriteFloat(long j, float f);

    private static native boolean nativeWriteInt(long j, int i);

    private static native boolean nativeWriteLong(long j, long j2);

    private static native boolean nativeWriteShort(long j, short s);

    private static native boolean nativeWriteString(long j, String str, int i);

    static {
        System.loadLibrary("utils_jni.z");
    }

    protected Parcel() {
        this.classLoaders.add(getClass().getClassLoader());
    }

    private Parcel(long j) {
        this.classLoaders.add(getClass().getClassLoader());
        construct(j);
    }

    private void construct(long j) {
        if (j != 0) {
            this.mNativeHandle = j;
            this.isOwner = false;
            return;
        }
        this.mNativeHandle = nativeConstruct();
        this.isOwner = true;
    }

    private void destruct() {
        long j = this.mNativeHandle;
        if (j != 0) {
            if (this.isOwner) {
                nativeDestruct(j);
            }
            this.mNativeHandle = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void flushBuffer() {
        nativeFlushBuffer(this.mNativeHandle);
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        destruct();
    }

    private void checkCapacityBeforeReadArray(int i, int i2) {
        if (i > 0 && i * i2 > getSize() - getReadPosition()) {
            throw new ParcelException("Out of capacity.");
        }
    }

    public static Parcel create() {
        return new Parcel(0);
    }

    public static Parcel create(long j) {
        synchronized (pool) {
            for (int i = 0; i < 8; i++) {
                Parcel parcel = pool[i];
                if (parcel != null) {
                    pool[i] = null;
                    parcel.construct(j);
                    return parcel;
                }
            }
            return new Parcel(j);
        }
    }

    public void reclaim() {
        flushBuffer();
    }

    public final int getSize() {
        return nativeGetSize(this.mNativeHandle);
    }

    public final int getCapacity() {
        return nativeGetCapacity(this.mNativeHandle);
    }

    public final boolean setSize(int i) {
        return nativeSetSize(this.mNativeHandle, i);
    }

    public final boolean setCapacity(int i) {
        return nativeSetCapacity(this.mNativeHandle, i);
    }

    public final int getWritableBytes() {
        return nativeGetWritableBytes(this.mNativeHandle);
    }

    public final int getReadableBytes() {
        return nativeGetReadableBytes(this.mNativeHandle);
    }

    public final int getReadPosition() {
        return nativeGetReadPosition(this.mNativeHandle);
    }

    public final int getWritePosition() {
        return nativeGetWritePosition(this.mNativeHandle);
    }

    public final boolean rewindRead(int i) {
        return nativeRewindRead(this.mNativeHandle, i);
    }

    public final boolean rewindWrite(int i) {
        return nativeRewindWrite(this.mNativeHandle, i);
    }

    public final boolean writeByte(byte b) {
        return nativeWriteByte(this.mNativeHandle, b);
    }

    public final boolean writeShort(short s) {
        return nativeWriteShort(this.mNativeHandle, s);
    }

    public final boolean writeInt(int i) {
        return nativeWriteInt(this.mNativeHandle, i);
    }

    public final boolean writeLong(long j) {
        return nativeWriteLong(this.mNativeHandle, j);
    }

    public final boolean writeFloat(float f) {
        return nativeWriteFloat(this.mNativeHandle, f);
    }

    public final boolean writeDouble(double d) {
        return nativeWriteDouble(this.mNativeHandle, d);
    }

    public final boolean writeBoolean(boolean z) {
        return nativeWriteBoolean(this.mNativeHandle, z);
    }

    public final boolean writeChar(char c) {
        return nativeWriteChar(this.mNativeHandle, c);
    }

    public final boolean writeString(String str) {
        if (str == null) {
            return nativeWriteString(this.mNativeHandle, null, 0);
        }
        return nativeWriteString(this.mNativeHandle, str, str.length());
    }

    public final void writeSequenceable(Sequenceable sequenceable) {
        if (sequenceable == null) {
            writeInt(0);
            return;
        }
        writeInt(1);
        sequenceable.marshalling(this);
    }

    public final byte readByte() {
        return nativeReadByte(this.mNativeHandle);
    }

    public final short readShort() {
        return nativeReadShort(this.mNativeHandle);
    }

    public final int readInt() {
        return nativeReadInt(this.mNativeHandle);
    }

    public final long readLong() {
        return nativeReadLong(this.mNativeHandle);
    }

    public final float readFloat() {
        return nativeReadFloat(this.mNativeHandle);
    }

    public final double readDouble() {
        return nativeReadDouble(this.mNativeHandle);
    }

    public final boolean readBoolean() {
        return nativeReadBoolean(this.mNativeHandle);
    }

    public final char readChar() {
        return nativeReadChar(this.mNativeHandle);
    }

    public final String readString() {
        return nativeReadString(this.mNativeHandle);
    }

    public final boolean readSequenceable(Sequenceable sequenceable) {
        if (readInt() == 0) {
            return false;
        }
        return sequenceable.unmarshalling(this);
    }

    public final boolean writeByteArray(byte[] bArr) {
        if (bArr == null) {
            return false;
        }
        int length = bArr.length;
        if (!writeInt(length)) {
            return false;
        }
        return nativeWriteByteArray(this.mNativeHandle, bArr, length);
    }

    public final boolean writeBytes(byte[] bArr) {
        if (bArr == null) {
            return false;
        }
        return nativeWriteByteArray(this.mNativeHandle, bArr, bArr.length);
    }

    public final boolean writeShortArray(short[] sArr) {
        if (sArr == null) {
            return false;
        }
        int length = sArr.length;
        if (!writeInt(length)) {
            return false;
        }
        for (short s : sArr) {
            if (!writeShort(s)) {
                return false;
            }
        }
        return true;
    }

    public final boolean writeIntArray(int[] iArr) {
        if (iArr == null) {
            return false;
        }
        int length = iArr.length;
        if (!writeInt(length)) {
            return false;
        }
        for (int i : iArr) {
            if (!writeInt(i)) {
                return false;
            }
        }
        return true;
    }

    public final boolean writeLongArray(long[] jArr) {
        if (jArr == null) {
            return false;
        }
        int length = jArr.length;
        if (!writeInt(length)) {
            return false;
        }
        for (long j : jArr) {
            if (!writeLong(j)) {
                return false;
            }
        }
        return true;
    }

    public final boolean writeFloatArray(float[] fArr) {
        if (fArr == null) {
            return false;
        }
        int length = fArr.length;
        if (!writeInt(length)) {
            return false;
        }
        for (float f : fArr) {
            if (!writeFloat(f)) {
                return false;
            }
        }
        return true;
    }

    public final boolean writeDoubleArray(double[] dArr) {
        if (dArr == null) {
            return false;
        }
        int length = dArr.length;
        if (!writeInt(length)) {
            return false;
        }
        for (double d : dArr) {
            if (!writeDouble(d)) {
                return false;
            }
        }
        return true;
    }

    public final boolean writeBooleanArray(boolean[] zArr) {
        if (zArr == null) {
            return false;
        }
        int length = zArr.length;
        if (!writeInt(length)) {
            return false;
        }
        for (boolean z : zArr) {
            if (!writeBoolean(z)) {
                return false;
            }
        }
        return true;
    }

    public final boolean writeCharArray(char[] cArr) {
        if (cArr == null) {
            return false;
        }
        int length = cArr.length;
        if (!writeInt(length)) {
            return false;
        }
        for (char c : cArr) {
            if (!writeChar(c)) {
                return false;
            }
        }
        return true;
    }

    public final boolean writeStringArray(String[] strArr) {
        if (strArr == null) {
            return false;
        }
        int length = strArr.length;
        if (!writeInt(length)) {
            return false;
        }
        for (String str : strArr) {
            if (!writeString(str)) {
                return false;
            }
        }
        return true;
    }

    public final boolean writeStringList(List<String> list) {
        if (list == null || !writeInt(list.size())) {
            return false;
        }
        for (String str : list) {
            if (!writeString(str)) {
                return false;
            }
        }
        return true;
    }

    public final boolean writeSequenceableArray(Sequenceable[] sequenceableArr) {
        if (sequenceableArr == null) {
            return false;
        }
        int length = sequenceableArr.length;
        if (!writeInt(length)) {
            return false;
        }
        for (Sequenceable sequenceable : sequenceableArr) {
            writeSequenceable(sequenceable);
        }
        return true;
    }

    public final boolean writeSequenceableList(List<? extends Sequenceable> list) {
        if (list == null || !writeInt(list.size())) {
            return false;
        }
        for (Sequenceable sequenceable : list) {
            writeSequenceable(sequenceable);
        }
        return true;
    }

    public final boolean writeSequenceableMap(Map<String, ? extends Sequenceable> map) {
        if (map == null || !writeInt(map.size())) {
            return false;
        }
        for (Map.Entry<String, ? extends Sequenceable> entry : map.entrySet()) {
            if (!writeString(entry.getKey())) {
                return false;
            }
            writeSequenceable((Sequenceable) entry.getValue());
        }
        return true;
    }

    public final void readByteArray(byte[] bArr) {
        int readInt = readInt();
        if (bArr.length != readInt) {
            throw new ParcelException("Bad length reading byteArray, val lenght is: " + bArr.length + " read length is: " + readInt);
        } else if (!nativeReadByteArray(this.mNativeHandle, bArr, readInt)) {
            throw new ParcelException("Read byte array failed");
        }
    }

    public final byte[] readByteArray() {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 1);
        if (readInt <= 0) {
            return new byte[0];
        }
        byte[] bArr = new byte[readInt];
        return !nativeReadByteArray(this.mNativeHandle, bArr, readInt) ? new byte[0] : bArr;
    }

    public final byte[] getBytes() {
        int size = getSize();
        if (size <= 0) {
            return new byte[0];
        }
        byte[] bArr = new byte[size];
        return !nativeReadByteArray(this.mNativeHandle, bArr, size) ? new byte[0] : bArr;
    }

    public final void readShortArray(short[] sArr) {
        int readInt = readInt();
        if (sArr.length == readInt) {
            for (int i = 0; i < readInt; i++) {
                sArr[i] = readShort();
            }
            return;
        }
        throw new ParcelException("Bad length while reading short array");
    }

    public final short[] readShortArray() {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 4);
        if (readInt <= 0) {
            return new short[0];
        }
        short[] sArr = new short[readInt];
        for (int i = 0; i < readInt; i++) {
            sArr[i] = readShort();
        }
        return sArr;
    }

    public final void readIntArray(int[] iArr) {
        int readInt = readInt();
        if (iArr.length == readInt) {
            for (int i = 0; i < readInt; i++) {
                iArr[i] = readInt();
            }
            return;
        }
        throw new ParcelException("Bad length while reading int array");
    }

    public final int[] readIntArray() {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 4);
        if (readInt <= 0) {
            return new int[0];
        }
        int[] iArr = new int[readInt];
        for (int i = 0; i < readInt; i++) {
            iArr[i] = readInt();
        }
        return iArr;
    }

    public final void readLongArray(long[] jArr) {
        int readInt = readInt();
        if (jArr.length == readInt) {
            for (int i = 0; i < readInt; i++) {
                jArr[i] = readLong();
            }
            return;
        }
        throw new ParcelException("Bad length while reading long array");
    }

    public final long[] readLongArray() {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 8);
        if (readInt <= 0) {
            return new long[0];
        }
        long[] jArr = new long[readInt];
        for (int i = 0; i < readInt; i++) {
            jArr[i] = readLong();
        }
        return jArr;
    }

    public final void readFloatArray(float[] fArr) {
        int readInt = readInt();
        if (fArr.length == readInt) {
            for (int i = 0; i < readInt; i++) {
                fArr[i] = readFloat();
            }
            return;
        }
        throw new ParcelException("Bad length while reading float array");
    }

    public final float[] readFloatArray() {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 4);
        if (readInt <= 0) {
            return new float[0];
        }
        float[] fArr = new float[readInt];
        for (int i = 0; i < readInt; i++) {
            fArr[i] = readFloat();
        }
        return fArr;
    }

    public final void readDoubleArray(double[] dArr) {
        int readInt = readInt();
        if (dArr.length == readInt) {
            for (int i = 0; i < readInt; i++) {
                dArr[i] = readDouble();
            }
            return;
        }
        throw new ParcelException("Bad length while reading double array");
    }

    public final double[] readDoubleArray() {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 8);
        if (readInt <= 0) {
            return new double[0];
        }
        double[] dArr = new double[readInt];
        for (int i = 0; i < readInt; i++) {
            dArr[i] = readDouble();
        }
        return dArr;
    }

    public final void readBooleanArray(boolean[] zArr) {
        int readInt = readInt();
        if (zArr.length == readInt) {
            for (int i = 0; i < readInt; i++) {
                zArr[i] = readBoolean();
            }
            return;
        }
        throw new ParcelException("Bad length while reading boolean array");
    }

    public final boolean[] readBooleanArray() {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 4);
        if (readInt <= 0) {
            return new boolean[0];
        }
        boolean[] zArr = new boolean[readInt];
        for (int i = 0; i < readInt; i++) {
            zArr[i] = readBoolean();
        }
        return zArr;
    }

    public final void readCharArray(char[] cArr) {
        int readInt = readInt();
        if (cArr.length == readInt) {
            for (int i = 0; i < readInt; i++) {
                cArr[i] = readChar();
            }
            return;
        }
        throw new ParcelException("Bad length while reading char array");
    }

    public final char[] readCharArray() {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 4);
        if (readInt <= 0) {
            return new char[0];
        }
        char[] cArr = new char[readInt];
        for (int i = 0; i < readInt; i++) {
            cArr[i] = readChar();
        }
        return cArr;
    }

    public final void readStringArray(String[] strArr) {
        int readInt = readInt();
        if (strArr.length == readInt) {
            for (int i = 0; i < readInt; i++) {
                strArr[i] = readString();
            }
            return;
        }
        throw new ParcelException("Bad length while reading string array");
    }

    public final String[] readStringArray() {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 1);
        if (readInt <= 0) {
            return new String[0];
        }
        String[] strArr = new String[readInt];
        for (int i = 0; i < readInt; i++) {
            if (getReadableBytes() <= 0) {
                String[] strArr2 = new String[i];
                System.arraycopy(strArr, 0, strArr2, 0, i);
                return strArr2;
            }
            strArr[i] = readString();
        }
        return strArr;
    }

    public final List<String> readStringList() {
        return Arrays.asList(readStringArray());
    }

    public final void readSequenceableArray(Sequenceable[] sequenceableArr) {
        int readInt = readInt();
        if (sequenceableArr.length == readInt) {
            for (int i = 0; i < readInt; i++) {
                readSequenceable(sequenceableArr[i]);
            }
            return;
        }
        throw new ParcelException("Bad length while reading Sequenceable array");
    }

    public final <T extends Sequenceable> List<T> readSequenceableList(Class<T> cls) {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 1);
        if (readInt == 0) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList();
        Sequenceable.Producer<?> createSequenceableProducer = createSequenceableProducer(cls.getName());
        for (int i = 0; i < readInt && getReadableBytes() > 0; i++) {
            arrayList.add(createSequenceable(createSequenceableProducer, true));
        }
        return arrayList;
    }

    public final <T extends Sequenceable> Map<String, T> readSequenceableMap(Class<T> cls) {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 1);
        if (readInt == 0) {
            return new HashMap();
        }
        HashMap hashMap = new HashMap();
        Sequenceable.Producer<?> createSequenceableProducer = createSequenceableProducer(cls.getName());
        for (int i = 0; i < readInt && getReadableBytes() > 0; i++) {
            hashMap.put(readString(), createSequenceable(createSequenceableProducer, true));
        }
        return hashMap;
    }

    public final void writeTypedSequenceable(Sequenceable sequenceable) {
        if (sequenceable == null) {
            writeString(null);
        } else if (writeString(sequenceable.getClass().getName())) {
            sequenceable.marshalling(this);
        } else {
            throw new ParcelException("fail to write parcelable class name into parcel");
        }
    }

    public final <T extends Sequenceable> T createSequenceable() {
        String readString = readString();
        if (readString == null) {
            return null;
        }
        return (T) createSequenceable(createSequenceableProducer(readString), false);
    }

    public final <T extends Sequenceable> T createSequenceable(ClassLoader classLoader) {
        addAppClassLoader(classLoader);
        String readString = readString();
        if (readString == null) {
            return null;
        }
        return (T) createSequenceable(createSequenceableProducer(readString), false);
    }

    /* access modifiers changed from: package-private */
    public final <T extends Sequenceable> T createSequenceable(Sequenceable.Producer<?> producer, boolean z) {
        if (!z || readInt() != 0) {
            return (T) ((Sequenceable) producer.createFromParcel(this));
        }
        return null;
    }

    public final <T extends Sequenceable> void writeTypedSequenceableArray(T[] tArr) {
        if (tArr == null) {
            writeInt(-1);
            return;
        }
        writeInt(tArr.length);
        for (T t : tArr) {
            writeTypedSequenceable(t);
        }
    }

    public final void writeSerializable(Serializable serializable) {
        ParcelUtils.writeSerializable(serializable, this);
    }

    public final <T extends Serializable> T readSerializable(Class<T> cls) {
        try {
            return cls.cast(ParcelUtils.readSerializable(this));
        } catch (ClassCastException unused) {
            throw new ParcelException("Class" + cls.getSimpleName() + " is not assignable ");
        }
    }

    public final Sequenceable[] createSequenceableArray() {
        return createSequenceableArray(getClass().getClassLoader());
    }

    public final Sequenceable[] createSequenceableArray(ClassLoader classLoader) {
        int readInt = readInt();
        checkCapacityBeforeReadArray(readInt, 1);
        if (readInt < 0) {
            return null;
        }
        Sequenceable[] sequenceableArr = new Sequenceable[readInt];
        for (int i = 0; i < readInt; i++) {
            if (getReadableBytes() <= 0) {
                Sequenceable[] sequenceableArr2 = new Sequenceable[i];
                System.arraycopy(sequenceableArr, 0, sequenceableArr2, 0, i);
                return sequenceableArr2;
            }
            sequenceableArr[i] = createSequenceable(classLoader);
        }
        return sequenceableArr;
    }

    private Sequenceable.Producer<?> createSequenceableProducer(String str) {
        Objects.requireNonNull(str, "Class name is null");
        Class<?> classByLoaders = getClassByLoaders(str);
        if (classByLoaders == null) {
            throw new ParcelException("Fail to load class by all possible ClassLoaders.");
        } else if (Sequenceable.class.isAssignableFrom(classByLoaders)) {
            try {
                Field field = classByLoaders.getField("PRODUCER");
                if ((field.getModifiers() & 8) == 0) {
                    throw new ParcelException("fail to create sequenceable creator due to PRODUCER is not static in class " + str);
                } else if (Sequenceable.Producer.class.isAssignableFrom(field.getType())) {
                    return (Sequenceable.Producer) field.get(null);
                } else {
                    throw new ParcelException("fail to create sequenceable creator due to PRODUCER type is error in class " + str);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new ParcelException("fail to create sequenceable creator due to " + e.getClass().getSimpleName() + ": " + e.getMessage() + ", class:" + str);
            }
        } else {
            throw new ParcelException("fail to create parcelable creator due to this class is not sequenceable");
        }
    }

    public List<?> readList() {
        int readInt = readInt();
        if (readInt <= 0) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < readInt && getReadableBytes() > 0; i++) {
            arrayList.add(ParcelUtils.readValueFromParcel(this));
        }
        return arrayList;
    }

    public void writeList(List<?> list) {
        if (list == null) {
            writeInt(0);
            return;
        }
        writeInt(list.size());
        Iterator<?> it = list.iterator();
        while (it.hasNext()) {
            ParcelUtils.writeValueIntoParcel(it.next(), this);
        }
    }

    public Map<?, ?> readMap() {
        int readInt = readInt();
        if (readInt == 0) {
            return new HashMap();
        }
        HashMap hashMap = new HashMap();
        for (int i = 0; i < readInt && getReadableBytes() > 0; i++) {
            hashMap.put(ParcelUtils.readValueFromParcel(this), ParcelUtils.readValueFromParcel(this));
        }
        return hashMap;
    }

    public void writeMap(Map<?, ?> map) {
        if (map == null) {
            writeInt(0);
            return;
        }
        writeInt(map.size());
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            ParcelUtils.writeValueIntoParcel(entry.getKey(), this);
            ParcelUtils.writeValueIntoParcel(entry.getValue(), this);
        }
    }

    public final void writeValue(Object obj) {
        if (obj instanceof ParcelableEx) {
            ParcelUtilsEx.writeValueIntoParcel(obj, this);
        } else {
            ParcelUtils.writeValueIntoParcel(obj, this);
        }
    }

    public final Object readValue() {
        return ParcelUtils.readValueFromParcel(this);
    }

    public final boolean appendFrom(Parcel parcel) {
        Objects.requireNonNull(parcel, "Appended parcel is null");
        byte[] bytes = parcel.getBytes();
        return nativeWriteByteArray(this.mNativeHandle, bytes, bytes.length);
    }

    public final boolean writePlainBooleanArray(PlainBooleanArray plainBooleanArray) {
        Objects.requireNonNull(plainBooleanArray, "value is null");
        if (!writeInt(plainBooleanArray.size())) {
            return false;
        }
        for (int i = 0; i < plainBooleanArray.size(); i++) {
            if (!writeInt(plainBooleanArray.keyAt(i)) || !writeBoolean(plainBooleanArray.valueAt(i))) {
                return false;
            }
        }
        return true;
    }

    public final PlainBooleanArray readPlainBooleanArray() {
        PlainBooleanArray plainBooleanArray = new PlainBooleanArray();
        int readInt = readInt();
        for (int i = 0; i < readInt && getReadableBytes() > 0; i++) {
            plainBooleanArray.append(readInt(), readBoolean());
        }
        return plainBooleanArray;
    }

    public final <T> void writePlainArray(PlainArray<T> plainArray) {
        Objects.requireNonNull(plainArray, "value is null.");
        if (writeInt(plainArray.size())) {
            for (int i = 0; i < plainArray.size(); i++) {
                if (writeInt(plainArray.keyAt(i))) {
                    ParcelUtils.writeValueIntoParcel(plainArray.valueAt(i), this);
                } else {
                    throw new ParcelException("Write key fail.");
                }
            }
            return;
        }
        throw new ParcelException("Write size fail.");
    }

    public final <T> PlainArray<T> readPlainArray(Class<T> cls) {
        PlainArray<T> plainArray = new PlainArray<>();
        int readInt = readInt();
        for (int i = 0; i < readInt && getReadableBytes() > 0; i++) {
            plainArray.append(readInt(), cls.cast(ParcelUtils.readValueFromParcel(this)));
        }
        return plainArray;
    }

    public final void writeParcelableEx(ParcelableEx parcelableEx) {
        if (parcelableEx == null) {
            writeString(null);
        } else {
            parcelableEx.marshallingEx(this);
        }
    }

    public final void writePacMapEx(PacMapEx pacMapEx) {
        if (pacMapEx == null) {
            writeInt(-1);
        } else {
            pacMapEx.marshallingEx(this);
        }
    }

    public final void readPacMapEx(PacMapEx pacMapEx) {
        pacMapEx.unmarshallingEx(this);
    }

    public final ParcelableEx readParcelableEx() {
        String readString = readString();
        if (readString != null) {
            try {
                Object newInstance = Class.forName(readString, false, null).newInstance();
                if (newInstance instanceof ParcelableEx) {
                    ParcelableEx parcelableEx = (ParcelableEx) newInstance;
                    parcelableEx.unmarshallingEx(this);
                    return parcelableEx;
                }
                HiLog.error(LOG_LABEL, "not a ParcelableEx", new Object[0]);
                return null;
            } catch (ClassNotFoundException unused) {
                HiLog.error(LOG_LABEL, "class not found, %{public}s", readString);
                return null;
            } catch (InstantiationException unused2) {
                HiLog.error(LOG_LABEL, "class instantiation failed, %{public}s", readString);
                return null;
            } catch (IllegalAccessException unused3) {
                HiLog.error(LOG_LABEL, "class constructor can not accessed, %{public}s", readString);
                return null;
            }
        } else {
            HiLog.error(LOG_LABEL, "no class name. read failed.", new Object[0]);
            return null;
        }
    }

    public final boolean writeByteArrayEx(byte[] bArr) {
        if (bArr == null) {
            writeInt(-1);
            return false;
        }
        int length = bArr.length;
        HiLog.error(LOG_LABEL, "byte len: %{pubilic}d", Integer.valueOf(length));
        if (!writeInt(length)) {
            return false;
        }
        return nativeWriteByteArray(this.mNativeHandle, bArr, length);
    }

    public final byte[] readByteArrayEx() {
        int readInt = readInt();
        if (readInt == -1) {
            return null;
        }
        if (readInt == 0) {
            return new byte[0];
        }
        if (readInt * 1 > getSize() - getReadPosition()) {
            return null;
        }
        byte[] bArr = new byte[readInt];
        if (!nativeReadByteArray(this.mNativeHandle, bArr, readInt)) {
            return null;
        }
        return bArr;
    }

    public void addAppClassLoader(ClassLoader classLoader) {
        if (!this.classLoaders.contains(classLoader)) {
            this.classLoaders.add(classLoader);
        }
    }

    private Class<?> getClassByLoaders(String str) {
        Iterator<ClassLoader> it = this.classLoaders.iterator();
        while (it.hasNext()) {
            try {
                return Class.forName(str, false, it.next());
            } catch (ClassNotFoundException unused) {
            }
        }
        return null;
    }
}
