package ohos.aafwk.content;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public class IntentParams implements Sequenceable {
    private static final LogLabel LABEL = LogLabel.create();
    private static final int NULL_OBJ_IN_PARCEL = -1;
    public static final Sequenceable.Producer<IntentParams> PRODUCER = $$Lambda$IntentParams$v9tdAkOk_Zo1ZXhEMCvjsrLvBoM.INSTANCE;
    private static final int VALUE_TYPE_BOOLEAN = 1;
    private static final int VALUE_TYPE_BOOLEANARRAY = 11;
    private static final int VALUE_TYPE_BYTE = 2;
    private static final int VALUE_TYPE_BYTEARRAY = 12;
    private static final int VALUE_TYPE_CHAR = 3;
    private static final int VALUE_TYPE_CHARARRAY = 13;
    private static final int VALUE_TYPE_CHARSEQUENCE = 10;
    private static final int VALUE_TYPE_CHARSEQUENCEARRAY = 20;
    private static final int VALUE_TYPE_DOUBLE = 8;
    private static final int VALUE_TYPE_DOUBLEARRAY = 18;
    private static final int VALUE_TYPE_FLOAT = 7;
    private static final int VALUE_TYPE_FLOATARRAY = 17;
    private static final int VALUE_TYPE_INT = 5;
    private static final int VALUE_TYPE_INTARRAY = 15;
    private static final int VALUE_TYPE_LIST = 50;
    private static final int VALUE_TYPE_LONG = 6;
    private static final int VALUE_TYPE_LONGARRAY = 16;
    private static final int VALUE_TYPE_NULL = -1;
    private static final int VALUE_TYPE_PARCELABLE = 21;
    private static final int VALUE_TYPE_PARCELABLE_ARRAY = 22;
    private static final int VALUE_TYPE_SERIALIZABLE = 23;
    private static final int VALUE_TYPE_SHORT = 4;
    private static final int VALUE_TYPE_SHORTARRAY = 14;
    private static final int VALUE_TYPE_STRING = 9;
    private static final int VALUE_TYPE_STRINGARRAY = 19;
    private ClassLoader classLoader;
    private final Object lockForUnPack = new Object();
    private Map<String, Object> params = new HashMap();
    private byte[] rawParams;
    private volatile boolean unpacked = true;

    public IntentParams() {
    }

    public IntentParams(IntentParams intentParams) {
        if (intentParams != null) {
            for (Map.Entry<String, Object> entry : intentParams.params.entrySet()) {
                this.params.put(entry.getKey(), entry.getValue());
            }
        }
    }

    static /* synthetic */ IntentParams lambda$static$0(Parcel parcel) {
        IntentParams intentParams = new IntentParams();
        intentParams.unmarshalling(parcel);
        return intentParams;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public void setClassLoader(ClassLoader classLoader2) {
        this.classLoader = classLoader2;
    }

    public /* synthetic */ Boolean lambda$marshalling$1$IntentParams(Parcel parcel, Object obj) {
        return Boolean.valueOf(doMarshalling(parcel));
    }

    public boolean marshalling(Parcel parcel) {
        return writeWithSizeAhead(parcel, new BiFunction() {
            /* class ohos.aafwk.content.$$Lambda$IntentParams$FatjOQ29KPth4zjgCvJdMqyukJg */

            @Override // java.util.function.BiFunction
            public final Object apply(Object obj, Object obj2) {
                return IntentParams.this.lambda$marshalling$1$IntentParams((Parcel) obj, obj2);
            }
        }, null);
    }

    private boolean doMarshalling(Parcel parcel) {
        if (isUnpacked()) {
            Log.debug(LABEL, "doMarshalling with unpacked params", new Object[0]);
            if (!parcel.writeInt(this.params.size())) {
                return false;
            }
            for (Map.Entry<String, Object> entry : this.params.entrySet()) {
                if (!writeEntry(parcel, entry)) {
                    return false;
                }
            }
            return true;
        }
        Log.debug(LABEL, "doMarshalling with raw params", new Object[0]);
        return writeWithSizeAhead(parcel, this.rawParams);
    }

    private boolean writeEntry(Parcel parcel, Map.Entry<String, Object> entry) {
        if (!parcel.writeString(entry.getKey())) {
            return false;
        }
        return writeObject(parcel, entry.getValue());
    }

    private boolean writeObject(Parcel parcel, Object obj) {
        if (obj == null) {
            return parcel.writeInt(-1);
        }
        if (obj instanceof Boolean) {
            if (!parcel.writeInt(1)) {
                return false;
            }
            return parcel.writeBoolean(((Boolean) obj).booleanValue());
        } else if (obj instanceof Byte) {
            if (!parcel.writeInt(2)) {
                return false;
            }
            return parcel.writeByte(((Byte) obj).byteValue());
        } else if (obj instanceof Character) {
            if (!parcel.writeInt(3)) {
                return false;
            }
            return parcel.writeInt(((Character) obj).charValue());
        } else if (obj instanceof Short) {
            if (!parcel.writeInt(4)) {
                return false;
            }
            return parcel.writeShort(((Short) obj).shortValue());
        } else if (obj instanceof Integer) {
            if (!parcel.writeInt(5)) {
                return false;
            }
            return parcel.writeInt(((Integer) obj).intValue());
        } else if (obj instanceof Long) {
            if (!parcel.writeInt(6)) {
                return false;
            }
            return parcel.writeLong(((Long) obj).longValue());
        } else if (obj instanceof Float) {
            if (!parcel.writeInt(7)) {
                return false;
            }
            return parcel.writeFloat(((Float) obj).floatValue());
        } else if (obj instanceof Double) {
            if (!parcel.writeInt(8)) {
                return false;
            }
            return parcel.writeDouble(((Double) obj).doubleValue());
        } else if (obj instanceof String) {
            if (!parcel.writeInt(9)) {
                return false;
            }
            return parcel.writeString((String) obj);
        } else if (obj instanceof CharSequence) {
            if (!parcel.writeInt(10)) {
                return false;
            }
            return parcel.writeString(((CharSequence) obj).toString());
        } else if (obj instanceof List) {
            if (!parcel.writeInt(50)) {
                return false;
            }
            return writeWithSizeAhead(parcel, new BiFunction() {
                /* class ohos.aafwk.content.$$Lambda$IntentParams$VOV3uIlpG1Ko5JojQhe9gqyypM */

                @Override // java.util.function.BiFunction
                public final Object apply(Object obj, Object obj2) {
                    return IntentParams.this.lambda$writeObject$2$IntentParams((Parcel) obj, obj2);
                }
            }, obj);
        } else if (obj instanceof boolean[]) {
            if (!parcel.writeInt(11)) {
                return false;
            }
            return parcel.writeBooleanArray((boolean[]) obj);
        } else if (obj instanceof byte[]) {
            if (!parcel.writeInt(12)) {
                return false;
            }
            return parcel.writeByteArray((byte[]) obj);
        } else if (obj instanceof char[]) {
            if (!parcel.writeInt(13)) {
                return false;
            }
            return writeCharArray(parcel, (char[]) obj);
        } else if (obj instanceof short[]) {
            if (!parcel.writeInt(14)) {
                return false;
            }
            return parcel.writeShortArray((short[]) obj);
        } else if (obj instanceof int[]) {
            if (!parcel.writeInt(15)) {
                return false;
            }
            return parcel.writeIntArray((int[]) obj);
        } else if (obj instanceof long[]) {
            if (!parcel.writeInt(16)) {
                return false;
            }
            return parcel.writeLongArray((long[]) obj);
        } else if (obj instanceof float[]) {
            if (!parcel.writeInt(17)) {
                return false;
            }
            return parcel.writeFloatArray((float[]) obj);
        } else if (obj instanceof double[]) {
            if (!parcel.writeInt(18)) {
                return false;
            }
            return parcel.writeDoubleArray((double[]) obj);
        } else if (obj instanceof String[]) {
            if (!parcel.writeInt(19)) {
                return false;
            }
            return parcel.writeStringArray((String[]) obj);
        } else if (obj instanceof CharSequence[]) {
            if (!parcel.writeInt(20)) {
                return false;
            }
            return writeCharSequenceArray(parcel, (CharSequence[]) obj);
        } else if (obj instanceof Sequenceable) {
            if (!parcel.writeInt(21)) {
                return false;
            }
            return writeWithSizeAhead(parcel, $$Lambda$IntentParams$1LQ4dTNJSTKifLHKH4nQQG8zxs8.INSTANCE, obj);
        } else if (obj instanceof Sequenceable[]) {
            if (!parcel.writeInt(22)) {
                return false;
            }
            return writeWithSizeAhead(parcel, $$Lambda$IntentParams$uPCRNiiBl9FXx6E6ervizMPClY.INSTANCE, obj);
        } else if (!(obj instanceof Serializable)) {
            throw new IllegalArgumentException("write IntentParams " + this + ": unknown type " + obj.getClass());
        } else if (!parcel.writeInt(23)) {
            return false;
        } else {
            return writeWithSizeAhead(parcel, new BiFunction() {
                /* class ohos.aafwk.content.$$Lambda$IntentParams$1nBhSjx8punEzibOZdcgO8HEoo */

                @Override // java.util.function.BiFunction
                public final Object apply(Object obj, Object obj2) {
                    return IntentParams.this.lambda$writeObject$5$IntentParams((Parcel) obj, obj2);
                }
            }, obj);
        }
    }

    public /* synthetic */ Boolean lambda$writeObject$2$IntentParams(Parcel parcel, Object obj) {
        return Boolean.valueOf(writeList(parcel, (List) obj));
    }

    public /* synthetic */ Boolean lambda$writeObject$5$IntentParams(Parcel parcel, Object obj) {
        return Boolean.valueOf(writeSerializable(parcel, (Serializable) obj));
    }

    private <T> boolean writeWithSizeAhead(Parcel parcel, BiFunction<Parcel, T, Boolean> biFunction, T t) {
        Parcel create = Parcel.create();
        if (!biFunction.apply(create, t).booleanValue()) {
            return false;
        }
        return writeWithSizeAhead(parcel, create.getBytes());
    }

    private boolean writeWithSizeAhead(Parcel parcel, byte[] bArr) {
        if (!parcel.writeInt(bArr.length)) {
            return false;
        }
        return parcel.writeByteArray(bArr);
    }

    private Object readWithSizeAhead(Parcel parcel, Function<Parcel, Object> function) {
        byte[] readWithSizeAhead = readWithSizeAhead(parcel);
        if (readWithSizeAhead == null) {
            return null;
        }
        Parcel create = Parcel.create();
        create.writeBytes(readWithSizeAhead);
        return function.apply(create);
    }

    private byte[] readWithSizeAhead(Parcel parcel) {
        if (parcel.readInt() < 0) {
            return null;
        }
        return parcel.readByteArray();
    }

    private boolean writeCharArray(Parcel parcel, char[] cArr) {
        if (!parcel.writeInt(cArr.length)) {
            return false;
        }
        for (char c : cArr) {
            if (!parcel.writeInt(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean writeList(Parcel parcel, List list) {
        if (list == null) {
            return parcel.writeInt(-1);
        }
        if (!parcel.writeInt(list.size())) {
            return false;
        }
        for (Object obj : list) {
            writeObject(parcel, obj);
        }
        return true;
    }

    static /* synthetic */ String[] lambda$writeCharSequenceArray$6(int i) {
        return new String[i];
    }

    private boolean writeCharSequenceArray(Parcel parcel, CharSequence[] charSequenceArr) {
        return parcel.writeStringArray((String[]) Arrays.stream(charSequenceArr).map($$Lambda$iuk7EHQtcikNhQI30cjCTYpqE0.INSTANCE).toArray($$Lambda$IntentParams$4ausIqlVZacqAvFFdwpmVBiIlEE.INSTANCE));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0020, code lost:
        $closeResource(r2, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0023, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0026, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0027, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002a, code lost:
        throw r3;
     */
    private boolean writeSerializable(Parcel parcel, Serializable serializable) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(serializable);
            boolean writeByteArray = parcel.writeByteArray(byteArrayOutputStream.toByteArray());
            $closeResource(null, objectOutputStream);
            $closeResource(null, byteArrayOutputStream);
            return writeByteArray;
        } catch (IOException e) {
            throw new UncheckedIOException("Object serialize fail " + e.getMessage(), e);
        }
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    public boolean unmarshalling(Parcel parcel) {
        this.rawParams = readWithSizeAhead(parcel);
        if (this.rawParams != null) {
            this.unpacked = false;
        }
        if (this.rawParams != null) {
            return true;
        }
        return false;
    }

    public boolean isUnpacked() {
        return this.unpacked;
    }

    /* JADX INFO: finally extract failed */
    private void unpack() {
        if (!this.unpacked) {
            synchronized (this.lockForUnPack) {
                if (!this.unpacked) {
                    Log.debug(LABEL, "unpack::rawParams length=%{public}d", Integer.valueOf(this.rawParams.length));
                    Parcel create = Parcel.create();
                    try {
                        create.writeBytes(this.rawParams);
                        if (!doUnmarshalling(create)) {
                            Log.error(LABEL, "unpack::unmarshalling return false", new Object[0]);
                        }
                        create.reclaim();
                        this.unpacked = true;
                        this.rawParams = null;
                    } catch (Throwable th) {
                        create.reclaim();
                        throw th;
                    }
                }
            }
        }
    }

    private boolean doUnmarshalling(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt < 0) {
            return false;
        }
        this.params.clear();
        for (int i = 0; i < readInt; i++) {
            this.params.put(parcel.readString(), readObject(parcel));
        }
        return true;
    }

    private Object readObject(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt == -1) {
            return null;
        }
        if (readInt == 50) {
            return readWithSizeAhead(parcel, new Function() {
                /* class ohos.aafwk.content.$$Lambda$IntentParams$aYeIk522noyoaKbQNwmKpDik_w */

                @Override // java.util.function.Function
                public final Object apply(Object obj) {
                    return IntentParams.this.readList((Parcel) obj);
                }
            });
        }
        switch (readInt) {
            case 1:
                return Boolean.valueOf(parcel.readBoolean());
            case 2:
                return Byte.valueOf(parcel.readByte());
            case 3:
                return Character.valueOf((char) parcel.readInt());
            case 4:
                return Short.valueOf(parcel.readShort());
            case 5:
                return Integer.valueOf(parcel.readInt());
            case 6:
                return Long.valueOf(parcel.readLong());
            case 7:
                return Float.valueOf(parcel.readFloat());
            case 8:
                return Double.valueOf(parcel.readDouble());
            case 9:
                return parcel.readString();
            case 10:
                return parcel.readString();
            case 11:
                return parcel.readBooleanArray();
            case 12:
                return parcel.readByteArray();
            case 13:
                return parcel.readCharArray();
            case 14:
                return parcel.readShortArray();
            case 15:
                return parcel.readIntArray();
            case 16:
                return parcel.readLongArray();
            case 17:
                return parcel.readFloatArray();
            case 18:
                return parcel.readDoubleArray();
            case 19:
            case 20:
                return parcel.readStringArray();
            case 21:
                return readWithSizeAhead(parcel, new Function() {
                    /* class ohos.aafwk.content.$$Lambda$IntentParams$zUGzSZqzopU21j2Desx2nwuPnI */

                    @Override // java.util.function.Function
                    public final Object apply(Object obj) {
                        return IntentParams.this.lambda$readObject$7$IntentParams((Parcel) obj);
                    }
                });
            case 22:
                return readWithSizeAhead(parcel, new Function() {
                    /* class ohos.aafwk.content.$$Lambda$IntentParams$vkQvxXE3iCBg_OoQtYjz0I4Gh4 */

                    @Override // java.util.function.Function
                    public final Object apply(Object obj) {
                        return IntentParams.this.lambda$readObject$8$IntentParams((Parcel) obj);
                    }
                });
            case 23:
                return readWithSizeAhead(parcel, new Function() {
                    /* class ohos.aafwk.content.$$Lambda$IntentParams$sXuMPlH4iV0Soa0qNjeaaJBCc8 */

                    @Override // java.util.function.Function
                    public final Object apply(Object obj) {
                        return IntentParams.this.readSerializable((Parcel) obj);
                    }
                });
            default:
                throw new IllegalArgumentException("IntentParams " + this + ": unknown value type " + readInt);
        }
    }

    public /* synthetic */ Object lambda$readObject$7$IntentParams(Parcel parcel) {
        ClassLoader classLoader2 = this.classLoader;
        if (classLoader2 == null) {
            return parcel.createSequenceable();
        }
        return parcel.createSequenceable(classLoader2);
    }

    public /* synthetic */ Object lambda$readObject$8$IntentParams(Parcel parcel) {
        ClassLoader classLoader2 = this.classLoader;
        if (classLoader2 == null) {
            return parcel.createSequenceableArray();
        }
        return parcel.createSequenceableArray(classLoader2);
    }

    /* access modifiers changed from: private */
    public List readList(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt == -1) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < readInt; i++) {
            arrayList.add(readObject(parcel));
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001f, code lost:
        $closeResource(r3, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0022, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0026, code lost:
        $closeResource(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0029, code lost:
        throw r4;
     */
    public Object readSerializable(Parcel parcel) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(parcel.readByteArray());
            AnonymousClass1 r4 = new ObjectInputStream(byteArrayInputStream) {
                /* class ohos.aafwk.content.IntentParams.AnonymousClass1 */

                /* access modifiers changed from: protected */
                @Override // java.io.ObjectInputStream
                public Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws ClassNotFoundException, IOException {
                    if (IntentParams.this.classLoader == null) {
                        return super.resolveClass(objectStreamClass);
                    }
                    return Class.forName(objectStreamClass.getName(), false, IntentParams.this.classLoader);
                }
            };
            Object readObject = r4.readObject();
            $closeResource(null, r4);
            $closeResource(null, byteArrayInputStream);
            return readObject;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Object deserialize fail " + e.getMessage(), e);
        } catch (IOException e2) {
            throw new UncheckedIOException("Object deserialize fail " + e2.getMessage(), e2);
        }
    }

    public <T> void setParam(String str, T t) {
        unpack();
        this.params.put(str, t);
    }

    public boolean isEmpty() {
        unpack();
        return this.params.isEmpty();
    }

    public int size() {
        unpack();
        return this.params.size();
    }

    public void remove(String str) {
        unpack();
        this.params.remove(str);
    }

    public Object getParam(String str) {
        unpack();
        return this.params.get(str);
    }

    public Map<String, Object> getParams() {
        unpack();
        return this.params;
    }

    public byte[] getRawParams() {
        byte[] bArr = this.rawParams;
        if (bArr == null) {
            return null;
        }
        return Arrays.copyOf(bArr, bArr.length);
    }

    public void setRawParams(byte[] bArr) {
        if (bArr != null) {
            this.unpacked = false;
            this.rawParams = Arrays.copyOf(bArr, bArr.length);
        }
    }

    public boolean hasParam(String str) {
        unpack();
        return this.params.containsKey(str);
    }

    public Set<String> keySet() {
        unpack();
        return this.params.keySet();
    }
}
