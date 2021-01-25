package ohos.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/* access modifiers changed from: package-private */
public class ParcelUtils {
    private static final int VAL_ARRAY_LIST = 20;
    private static final int VAL_BOOLEAN = 6;
    private static final int VAL_BOOLEAN_ARRAY = 15;
    private static final int VAL_BYTE = 0;
    private static final int VAL_BYTE_ARRAY = 9;
    private static final int VAL_CHAR = 7;
    private static final int VAL_CHAR_ARRAY = 16;
    private static final int VAL_DIMENSION = 24;
    private static final int VAL_DOUBLE = 5;
    private static final int VAL_DOUBLE_ARRAY = 14;
    private static final int VAL_FLOAT = 4;
    private static final int VAL_FLOAT_ARRAY = 13;
    private static final int VAL_FLOAT_DIMENSION = 25;
    private static final int VAL_INTEGER = 2;
    private static final int VAL_INTEGER_ARRAY = 11;
    private static final int VAL_LONG = 3;
    private static final int VAL_LONG_ARRAY = 12;
    private static final int VAL_NULL = -1;
    private static final int VAL_PLAIN_ARRAY = 22;
    private static final int VAL_REMOTE_OBJ = 23;
    private static final int VAL_SEQUENCEABLE = 26;
    private static final int VAL_SEQUENCEABLE_ARRAY = 27;
    private static final int VAL_SERIALIZABLE = 21;
    private static final int VAL_SHORT = 1;
    private static final int VAL_SHORT_ARRAY = 10;
    private static final int VAL_STRING = 8;
    private static final int VAL_STRING_ARRAY = 17;

    ParcelUtils() {
    }

    static void writeValueIntoParcel(Object obj, Parcel parcel) {
        if (obj == null) {
            parcel.writeInt(-1);
        } else if (obj instanceof Byte) {
            parcel.writeInt(0);
            parcel.writeByte(((Byte) obj).byteValue());
        } else if (obj instanceof Short) {
            parcel.writeInt(1);
            parcel.writeShort(((Short) obj).shortValue());
        } else if (obj instanceof Integer) {
            parcel.writeInt(2);
            parcel.writeInt(((Integer) obj).intValue());
        } else if (obj instanceof Long) {
            parcel.writeInt(3);
            parcel.writeLong(((Long) obj).longValue());
        } else if (obj instanceof Float) {
            parcel.writeInt(4);
            parcel.writeFloat(((Float) obj).floatValue());
        } else if (obj instanceof Double) {
            parcel.writeInt(5);
            parcel.writeDouble(((Double) obj).doubleValue());
        } else if (obj instanceof Boolean) {
            parcel.writeInt(6);
            parcel.writeBoolean(((Boolean) obj).booleanValue());
        } else if (obj instanceof Character) {
            parcel.writeInt(7);
            parcel.writeChar(((Character) obj).charValue());
        } else if (obj instanceof String) {
            parcel.writeInt(8);
            parcel.writeString((String) obj);
        } else if (obj instanceof byte[]) {
            parcel.writeInt(9);
            parcel.writeByteArray((byte[]) obj);
        } else if (obj instanceof short[]) {
            parcel.writeInt(10);
            parcel.writeShortArray((short[]) obj);
        } else if (obj instanceof int[]) {
            parcel.writeInt(11);
            parcel.writeIntArray((int[]) obj);
        } else if (obj instanceof long[]) {
            parcel.writeInt(12);
            parcel.writeLongArray((long[]) obj);
        } else if (obj instanceof float[]) {
            parcel.writeInt(13);
            parcel.writeFloatArray((float[]) obj);
        } else if (obj instanceof double[]) {
            parcel.writeInt(14);
            parcel.writeDoubleArray((double[]) obj);
        } else if (obj instanceof boolean[]) {
            parcel.writeInt(15);
            parcel.writeBooleanArray((boolean[]) obj);
        } else if (obj instanceof char[]) {
            parcel.writeInt(16);
            parcel.writeCharArray((char[]) obj);
        } else if (obj instanceof String[]) {
            parcel.writeInt(17);
            parcel.writeStringArray((String[]) obj);
        } else if (obj instanceof Sequenceable) {
            parcel.writeInt(26);
            parcel.writeTypedSequenceable((Sequenceable) obj);
        } else if (obj instanceof ArrayList) {
            writeArrayList((ArrayList) obj, parcel);
        } else if (obj instanceof Sequenceable[]) {
            parcel.writeInt(27);
            parcel.writeTypedSequenceableArray((Sequenceable[]) obj);
        } else if (obj instanceof Serializable) {
            parcel.writeInt(21);
            writeSerializable((Serializable) obj, parcel);
        } else if (obj instanceof PlainArray) {
            parcel.writeInt(22);
            parcel.writePlainArray((PlainArray) obj);
        } else if (obj instanceof Dimension) {
            parcel.writeInt(24);
            Dimension dimension = (Dimension) obj;
            parcel.writeInt(dimension.getWidthSize());
            parcel.writeInt(dimension.getHeightSize());
        } else if (obj instanceof FloatDimension) {
            parcel.writeInt(25);
            FloatDimension floatDimension = (FloatDimension) obj;
            parcel.writeFloat(floatDimension.getWidthSize());
            parcel.writeFloat(floatDimension.getHeightSize());
        } else if (!isInstanceof(obj.getClass(), "ohos.rpc.IRemoteObject") || !isInstanceof(parcel.getClass(), "ohos.rpc.MessageParcel")) {
            throw new ParcelException("Unsupported type by write.");
        } else {
            parcel.writeInt(23);
            writeRemoteObject(parcel, obj);
        }
    }

    static Object readValueFromParcel(Parcel parcel) {
        switch (parcel.readInt()) {
            case -1:
                return null;
            case 0:
                return Byte.valueOf(parcel.readByte());
            case 1:
                return Short.valueOf(parcel.readShort());
            case 2:
                return Integer.valueOf(parcel.readInt());
            case 3:
                return Long.valueOf(parcel.readLong());
            case 4:
                return Float.valueOf(parcel.readFloat());
            case 5:
                return Double.valueOf(parcel.readDouble());
            case 6:
                return Boolean.valueOf(parcel.readBoolean());
            case 7:
                return Character.valueOf(parcel.readChar());
            case 8:
                return parcel.readString();
            case 9:
                return parcel.readByteArray();
            case 10:
                return parcel.readShortArray();
            case 11:
                return parcel.readIntArray();
            case 12:
                return parcel.readLongArray();
            case 13:
                return parcel.readFloatArray();
            case 14:
                return parcel.readDoubleArray();
            case 15:
                return parcel.readBooleanArray();
            case 16:
                return parcel.readCharArray();
            case 17:
                return parcel.readStringArray();
            case 18:
            case 19:
            default:
                throw new ParcelException("Unsupported type by read.");
            case 20:
                return readArrayList(parcel);
            case 21:
                return readSerializable(parcel);
            case 22:
                return parcel.readPlainArray(Sequenceable.class);
            case 23:
                try {
                    return readRemoteObject(parcel, Class.forName("ohos.rpc.RemoteObject"));
                } catch (ClassNotFoundException unused) {
                    throw new ParcelException("Can not find class : ohos.rpc.RemoteObject");
                }
            case 24:
                return readDimension(parcel);
            case 25:
                return readFloatDimension(parcel);
            case 26:
                return parcel.createSequenceable();
            case 27:
                return parcel.createSequenceableArray();
        }
    }

    private static void writeArrayList(ArrayList<Object> arrayList, Parcel parcel) {
        if (arrayList == null) {
            parcel.writeInt(-1);
            return;
        }
        parcel.writeInt(20);
        parcel.writeInt(arrayList.size());
        Iterator<Object> it = arrayList.iterator();
        while (it.hasNext()) {
            writeValueIntoParcel(it.next(), parcel);
        }
    }

    private static ArrayList<Object> readArrayList(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt >= 0) {
            ArrayList<Object> arrayList = new ArrayList<>(readInt);
            for (int i = 0; i < readInt && parcel.getReadableBytes() > 0; i++) {
                arrayList.add(readValueFromParcel(parcel));
            }
            return arrayList;
        }
        throw new ParcelException("readArrayList failed due to data size mismatch");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002f, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0033, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0036, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0037, code lost:
        $closeResource(r3, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x003a, code lost:
        throw r4;
     */
    protected static void writeSerializable(Serializable serializable, Parcel parcel) {
        if (serializable == null) {
            parcel.writeString(null);
            return;
        }
        parcel.writeString(serializable.getClass().getName());
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(serializable);
            parcel.writeByteArray(byteArrayOutputStream.toByteArray());
            $closeResource(null, objectOutputStream);
            $closeResource(null, byteArrayOutputStream);
        } catch (IOException e) {
            throw new ParcelException("fail to writeSerializable due to io error: " + e.getMessage());
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

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0025, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0026, code lost:
        $closeResource(r1, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0029, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x002c, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002d, code lost:
        $closeResource(r3, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0030, code lost:
        throw r1;
     */
    protected static Serializable readSerializable(Parcel parcel) {
        if (parcel.readString() == null) {
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(parcel.readByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Serializable serializable = (Serializable) objectInputStream.readObject();
        $closeResource(null, objectInputStream);
        try {
            $closeResource(null, byteArrayInputStream);
            return serializable;
        } catch (IOException | ClassNotFoundException e) {
            throw new ParcelException("fail to readSerializable due to error: " + e.getMessage());
        }
    }

    protected static void writeRemoteObject(Parcel parcel, Object obj) {
        checkMessageParcelDeclare(parcel.getClass(), obj.getClass());
        Method findMethodByName = findMethodByName(parcel.getClass().getDeclaredMethods(), "writeRemoteObject");
        if (findMethodByName != null) {
            try {
                findMethodByName.invoke(parcel, obj);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException unused) {
                throw new ParcelException("Calling MessageParcel.writeRemoteObject fail.");
            }
        } else {
            throw new ParcelException("No such method: writeRemoteObject");
        }
    }

    protected static <T> T readRemoteObject(Parcel parcel, Class<T> cls) {
        checkMessageParcelDeclare(parcel.getClass(), cls);
        try {
            return cls.cast(parcel.getClass().getDeclaredMethod("readRemoteObject", new Class[0]).invoke(parcel, new Object[0]));
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException unused) {
            throw new ParcelException("Calling MessageParcel.readRemoteObject fail.");
        }
    }

    private static void checkMessageParcelDeclare(Class<?> cls, Class<?> cls2) {
        if (!isInstanceof(cls, "ohos.rpc.MessageParcel")) {
            throw new ParcelException("Parcel type invalid, may be MessageParcel.");
        } else if (!isInstanceof(cls2, "ohos.rpc.IRemoteObject")) {
            throw new ParcelException("Object type invalid.");
        }
    }

    private static Method findMethodByName(Method[] methodArr, String str) {
        for (Method method : methodArr) {
            if (method.getName().equals(str)) {
                return method;
            }
        }
        return null;
    }

    static boolean isInstanceof(Class<?> cls, String str) {
        for (Class<?> cls2 : cls.getInterfaces()) {
            if (cls2.getName().equals(str)) {
                return true;
            }
        }
        while (cls != null) {
            if (cls.getName().equals(str)) {
                return true;
            }
            if (cls.getName().equals("java.lang.Object")) {
                break;
            }
            cls = cls.getSuperclass();
        }
        return false;
    }

    private static Object readDimension(Parcel parcel) {
        return new Dimension(parcel.readInt(), parcel.readInt());
    }

    private static Object readFloatDimension(Parcel parcel) {
        return new FloatDimension(parcel.readFloat(), parcel.readFloat());
    }
}
