package ohos.interwork.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import ohos.utils.Parcel;
import ohos.utils.ParcelException;

public class ParcelUtilsEx {
    private static final int VALUE_TAG_BOOLEAN = 9;
    private static final int VALUE_TAG_BYTEARRAY = 13;
    private static final int VALUE_TAG_DOUBLE = 8;
    private static final int VALUE_TAG_FLOAT = 7;
    private static final int VALUE_TAG_INTEGER = 1;
    private static final int VALUE_TAG_LONG = 6;
    private static final int VALUE_TAG_MAP = 2;
    private static final int VALUE_TAG_NULL = -1;
    private static final int VALUE_TAG_PACMAPEX = 3;
    private static final int VALUE_TAG_PARCELABLEEX = 4;
    private static final int VALUE_TAG_SERIALIZABLE = 21;
    private static final int VALUE_TAG_SHORT = 5;
    private static final int VALUE_TAG_STRING = 0;

    public static void writeValueIntoParcel(Object obj, Parcel parcel) {
        if (obj == null) {
            parcel.writeInt(-1);
        } else if (obj instanceof String) {
            parcel.writeInt(0);
            parcel.writeString((String) obj);
        } else if (obj instanceof Boolean) {
            parcel.writeInt(9);
            parcel.writeBoolean(((Boolean) obj).booleanValue());
        } else if (obj instanceof Map) {
            parcel.writeInt(2);
            writeMapEx((Map) obj, parcel);
        } else if (obj instanceof PacMapEx) {
            parcel.writeInt(3);
            parcel.writePacMapEx((PacMapEx) obj);
        } else if (obj instanceof ParcelableEx) {
            parcel.writeInt(4);
            parcel.writeParcelableEx((ParcelableEx) obj);
        } else if (obj instanceof Integer) {
            parcel.writeInt(1);
            parcel.writeInt(((Integer) obj).intValue());
        } else if (obj instanceof Long) {
            parcel.writeInt(6);
            parcel.writeLong(((Long) obj).longValue());
        } else if (obj instanceof Float) {
            parcel.writeInt(7);
            parcel.writeFloat(((Float) obj).floatValue());
        } else if (obj instanceof Double) {
            parcel.writeInt(8);
            parcel.writeDouble(((Double) obj).doubleValue());
        } else if (obj instanceof Short) {
            parcel.writeInt(5);
            parcel.writeShort(((Short) obj).shortValue());
        } else if (obj instanceof byte[]) {
            parcel.writeInt(13);
            parcel.writeByteArrayEx((byte[]) obj);
        } else if (obj instanceof Serializable) {
            parcel.writeInt(21);
            writeSerializableEx((Serializable) obj, parcel);
        } else {
            throw new ParcelException("Unsupported type by write.");
        }
    }

    public static Object readValueFromParcel(Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt == 13) {
            return parcel.readByteArrayEx();
        }
        if (readInt == 21) {
            return readSerializableEx(parcel);
        }
        switch (readInt) {
            case -1:
                return null;
            case 0:
                return parcel.readString();
            case 1:
                return Integer.valueOf(parcel.readInt());
            case 2:
                HashMap hashMap = new HashMap();
                readMapEx(hashMap, parcel);
                return hashMap;
            case 3:
                PacMapEx pacMapEx = new PacMapEx();
                parcel.readPacMapEx(pacMapEx);
                return pacMapEx;
            case 4:
                return parcel.readParcelableEx();
            case 5:
                return Short.valueOf(parcel.readShort());
            case 6:
                return Long.valueOf(parcel.readLong());
            case 7:
                return Float.valueOf(parcel.readFloat());
            case 8:
                return Double.valueOf(parcel.readDouble());
            case 9:
                return Boolean.valueOf(parcel.readBoolean());
            default:
                throw new ParcelException("Unsupported type by read.");
        }
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
    static void writeSerializableEx(Serializable serializable, Parcel parcel) {
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
    static Serializable readSerializableEx(Parcel parcel) {
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

    static void writeMapEx(Map<String, Object> map, Parcel parcel) {
        if (map == null) {
            parcel.writeInt(-1);
            return;
        }
        parcel.writeInt(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            parcel.writeString(entry.getKey());
            writeValueIntoParcel(entry.getValue(), parcel);
        }
    }

    static void readMapEx(Map<String, Object> map, Parcel parcel) {
        int readInt = parcel.readInt();
        if (readInt <= 0) {
            map.clear();
            return;
        }
        for (int i = 0; i < readInt; i++) {
            map.put(parcel.readString(), readValueFromParcel(parcel));
        }
    }
}
