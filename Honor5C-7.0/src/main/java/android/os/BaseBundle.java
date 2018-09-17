package android.os;

import android.util.ArrayMap;
import android.util.Log;
import android.util.MathUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

public class BaseBundle {
    static final int BUNDLE_MAGIC = 1279544898;
    static final boolean DEBUG = false;
    static final int FLAG_DEFUSABLE = 1;
    private static final boolean LOG_DEFUSABLE = false;
    private static final String TAG = "Bundle";
    private static volatile boolean sShouldDefuse;
    private ClassLoader mClassLoader;
    int mFlags;
    ArrayMap<String, Object> mMap;
    Parcel mParcelledData;

    static final class NoImagePreloadHolder {
        public static final Parcel EMPTY_PARCEL = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.BaseBundle.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.BaseBundle.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.os.BaseBundle.NoImagePreloadHolder.<clinit>():void");
        }

        NoImagePreloadHolder() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.BaseBundle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.BaseBundle.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.BaseBundle.<clinit>():void");
    }

    public static void setShouldDefuse(boolean shouldDefuse) {
        sShouldDefuse = shouldDefuse;
    }

    BaseBundle(ClassLoader loader, int capacity) {
        this.mMap = null;
        this.mParcelledData = null;
        this.mMap = capacity > 0 ? new ArrayMap(capacity) : new ArrayMap();
        if (loader == null) {
            loader = getClass().getClassLoader();
        }
        this.mClassLoader = loader;
    }

    BaseBundle() {
        this((ClassLoader) null, 0);
    }

    BaseBundle(Parcel parcelledData) {
        this.mMap = null;
        this.mParcelledData = null;
        readFromParcelInner(parcelledData);
    }

    BaseBundle(Parcel parcelledData, int length) {
        this.mMap = null;
        this.mParcelledData = null;
        readFromParcelInner(parcelledData, length);
    }

    BaseBundle(ClassLoader loader) {
        this(loader, 0);
    }

    BaseBundle(int capacity) {
        this((ClassLoader) null, capacity);
    }

    BaseBundle(BaseBundle b) {
        this.mMap = null;
        this.mParcelledData = null;
        if (b.mParcelledData == null) {
            this.mParcelledData = null;
        } else if (b.isEmptyParcel()) {
            this.mParcelledData = NoImagePreloadHolder.EMPTY_PARCEL;
        } else {
            this.mParcelledData = Parcel.obtain();
            this.mParcelledData.appendFrom(b.mParcelledData, 0, b.mParcelledData.dataSize());
            this.mParcelledData.setDataPosition(0);
        }
        if (b.mMap != null) {
            this.mMap = new ArrayMap(b.mMap);
        } else {
            this.mMap = null;
        }
        this.mClassLoader = b.mClassLoader;
    }

    public String getPairValue() {
        unparcel();
        int size = this.mMap.size();
        if (size > FLAG_DEFUSABLE) {
            Log.w(TAG, "getPairValue() used on Bundle with multiple pairs.");
        }
        if (size == 0) {
            return null;
        }
        Object o = this.mMap.valueAt(0);
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning("getPairValue()", o, "String", e);
            return null;
        }
    }

    void setClassLoader(ClassLoader loader) {
        this.mClassLoader = loader;
    }

    ClassLoader getClassLoader() {
        return this.mClassLoader;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized void unparcel() {
        synchronized (this) {
            Parcel parcelledData = this.mParcelledData;
            if (parcelledData == null) {
                return;
            } else if (isEmptyParcel()) {
                if (this.mMap == null) {
                    this.mMap = new ArrayMap(FLAG_DEFUSABLE);
                } else {
                    this.mMap.erase();
                }
                this.mParcelledData = null;
                return;
            } else {
                int N = parcelledData.readInt();
                if (N < 0) {
                    return;
                }
                ArrayMap<String, Object> map = this.mMap;
                if (map == null) {
                    map = new ArrayMap(N);
                } else {
                    map.erase();
                    map.ensureCapacity(N);
                }
                try {
                    parcelledData.readArrayMapInternal(map, N, this.mClassLoader);
                } catch (BadParcelableException e) {
                    if (sShouldDefuse) {
                        Log.w(TAG, "Failed to parse Bundle, but defusing quietly", e);
                        map.erase();
                    } else {
                        throw e;
                    }
                } catch (RuntimeException e2) {
                    Log.e(TAG, "unparcel readArrayMapInternal Exception", e2);
                } finally {
                    this.mMap = map;
                    parcelledData.recycle();
                    this.mParcelledData = null;
                }
            }
        }
    }

    public boolean isParcelled() {
        return this.mParcelledData != null ? true : LOG_DEFUSABLE;
    }

    public boolean isEmptyParcel() {
        return this.mParcelledData == NoImagePreloadHolder.EMPTY_PARCEL ? true : LOG_DEFUSABLE;
    }

    ArrayMap<String, Object> getMap() {
        unparcel();
        return this.mMap;
    }

    public int size() {
        unparcel();
        return this.mMap.size();
    }

    public boolean isEmpty() {
        unparcel();
        return this.mMap.isEmpty();
    }

    public void clear() {
        unparcel();
        this.mMap.clear();
    }

    public boolean containsKey(String key) {
        unparcel();
        return this.mMap.containsKey(key);
    }

    public Object get(String key) {
        unparcel();
        return this.mMap.get(key);
    }

    public void remove(String key) {
        unparcel();
        this.mMap.remove(key);
    }

    public void putAll(PersistableBundle bundle) {
        unparcel();
        bundle.unparcel();
        this.mMap.putAll(bundle.mMap);
    }

    void putAll(ArrayMap map) {
        unparcel();
        this.mMap.putAll(map);
    }

    public Set<String> keySet() {
        unparcel();
        return this.mMap.keySet();
    }

    public void putBoolean(String key, boolean value) {
        unparcel();
        this.mMap.put(key, Boolean.valueOf(value));
    }

    void putByte(String key, byte value) {
        unparcel();
        this.mMap.put(key, Byte.valueOf(value));
    }

    void putChar(String key, char value) {
        unparcel();
        this.mMap.put(key, Character.valueOf(value));
    }

    void putShort(String key, short value) {
        unparcel();
        this.mMap.put(key, Short.valueOf(value));
    }

    public void putInt(String key, int value) {
        unparcel();
        this.mMap.put(key, Integer.valueOf(value));
    }

    public void putLong(String key, long value) {
        unparcel();
        this.mMap.put(key, Long.valueOf(value));
    }

    void putFloat(String key, float value) {
        unparcel();
        this.mMap.put(key, Float.valueOf(value));
    }

    public void putDouble(String key, double value) {
        unparcel();
        this.mMap.put(key, Double.valueOf(value));
    }

    public void putString(String key, String value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putCharSequence(String key, CharSequence value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putIntegerArrayList(String key, ArrayList<Integer> value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putStringArrayList(String key, ArrayList<String> value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putSerializable(String key, Serializable value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putBooleanArray(String key, boolean[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putByteArray(String key, byte[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putShortArray(String key, short[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putCharArray(String key, char[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putIntArray(String key, int[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putLongArray(String key, long[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putFloatArray(String key, float[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putDoubleArray(String key, double[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public void putStringArray(String key, String[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    void putCharSequenceArray(String key, CharSequence[] value) {
        unparcel();
        this.mMap.put(key, value);
    }

    public boolean getBoolean(String key) {
        unparcel();
        return getBoolean(key, LOG_DEFUSABLE);
    }

    void typeWarning(String key, Object value, String className, Object defaultValue, ClassCastException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Key ");
        sb.append(key);
        sb.append(" expected ");
        sb.append(className);
        sb.append(" but value was a ");
        sb.append(value.getClass().getName());
        sb.append(".  The default value ");
        sb.append(defaultValue);
        sb.append(" was returned.");
        Log.w(TAG, sb.toString());
        Log.w(TAG, "Attempt to cast generated internal exception:", e);
    }

    void typeWarning(String key, Object value, String className, ClassCastException e) {
        typeWarning(key, value, className, "<null>", e);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Boolean) o).booleanValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Boolean", Boolean.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    byte getByte(String key) {
        unparcel();
        return getByte(key, (byte) 0).byteValue();
    }

    Byte getByte(String key, byte defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return Byte.valueOf(defaultValue);
        }
        try {
            return (Byte) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Byte", Byte.valueOf(defaultValue), e);
            return Byte.valueOf(defaultValue);
        }
    }

    char getChar(String key) {
        unparcel();
        return getChar(key, '\u0000');
    }

    char getChar(String key, char defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Character) o).charValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Character", Character.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    short getShort(String key) {
        unparcel();
        return getShort(key, (short) 0);
    }

    short getShort(String key, short defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Short) o).shortValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Short", Short.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    public int getInt(String key) {
        unparcel();
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Integer) o).intValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Integer", Integer.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    public long getLong(String key) {
        unparcel();
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Long) o).longValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Long", Long.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    float getFloat(String key) {
        unparcel();
        return getFloat(key, 0.0f);
    }

    float getFloat(String key, float defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Float) o).floatValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Float", Float.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    public double getDouble(String key) {
        unparcel();
        return getDouble(key, 0.0d);
    }

    public double getDouble(String key, double defaultValue) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return ((Double) o).doubleValue();
        } catch (ClassCastException e) {
            typeWarning(key, o, "Double", Double.valueOf(defaultValue), e);
            return defaultValue;
        }
    }

    public String getString(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String", e);
            return null;
        }
    }

    public String getString(String key, String defaultValue) {
        String s = getString(key);
        return s == null ? defaultValue : s;
    }

    CharSequence getCharSequence(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        try {
            return (CharSequence) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence", e);
            return null;
        }
    }

    CharSequence getCharSequence(String key, CharSequence defaultValue) {
        CharSequence cs = getCharSequence(key);
        return cs == null ? defaultValue : cs;
    }

    Serializable getSerializable(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (Serializable) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Serializable", e);
            return null;
        }
    }

    ArrayList<Integer> getIntegerArrayList(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<Integer>", e);
            return null;
        }
    }

    ArrayList<String> getStringArrayList(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<String>", e);
            return null;
        }
    }

    ArrayList<CharSequence> getCharSequenceArrayList(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (ArrayList) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "ArrayList<CharSequence>", e);
            return null;
        }
    }

    public boolean[] getBooleanArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (boolean[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    byte[] getByteArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (byte[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "byte[]", e);
            return null;
        }
    }

    short[] getShortArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (short[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "short[]", e);
            return null;
        }
    }

    char[] getCharArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (char[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "char[]", e);
            return null;
        }
    }

    public int[] getIntArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (int[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "int[]", e);
            return null;
        }
    }

    public long[] getLongArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (long[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "long[]", e);
            return null;
        }
    }

    float[] getFloatArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (float[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "float[]", e);
            return null;
        }
    }

    public double[] getDoubleArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (double[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "double[]", e);
            return null;
        }
    }

    public String[] getStringArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (String[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String[]", e);
            return null;
        }
    }

    CharSequence[] getCharSequenceArray(String key) {
        unparcel();
        Object o = this.mMap.get(key);
        if (o == null) {
            return null;
        }
        try {
            return (CharSequence[]) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "CharSequence[]", e);
            return null;
        }
    }

    void writeToParcelInner(Parcel parcel, int flags) {
        synchronized (this) {
            Parcel parcelledData = this.mParcelledData;
        }
        if (parcelledData != null) {
            if (isEmptyParcel()) {
                parcel.writeInt(0);
            } else {
                int length = parcelledData.dataSize();
                parcel.writeInt(length);
                parcel.writeInt(BUNDLE_MAGIC);
                parcel.appendFrom(parcelledData, 0, length);
            }
        } else if (this.mMap == null || this.mMap.size() <= 0) {
            parcel.writeInt(0);
        } else {
            int lengthPos = parcel.dataPosition();
            parcel.writeInt(-1);
            parcel.writeInt(BUNDLE_MAGIC);
            int startPos = parcel.dataPosition();
            parcel.writeArrayMapInternal(this.mMap);
            int endPos = parcel.dataPosition();
            parcel.setDataPosition(lengthPos);
            parcel.writeInt(endPos - startPos);
            parcel.setDataPosition(endPos);
        }
    }

    void readFromParcelInner(Parcel parcel) {
        readFromParcelInner(parcel, parcel.readInt());
    }

    private void readFromParcelInner(Parcel parcel, int length) {
        if (length < 0) {
            throw new RuntimeException("Bad length in parcel: " + length);
        } else if (length == 0) {
            this.mParcelledData = NoImagePreloadHolder.EMPTY_PARCEL;
        } else {
            int magic = parcel.readInt();
            if (magic != BUNDLE_MAGIC) {
                throw new IllegalStateException("Bad magic number for Bundle: 0x" + Integer.toHexString(magic));
            }
            int offset = parcel.dataPosition();
            parcel.setDataPosition(MathUtils.addOrThrow(offset, length));
            Parcel p = Parcel.obtain();
            p.setDataPosition(0);
            p.appendFrom(parcel, offset, length);
            p.setDataPosition(0);
            this.mParcelledData = p;
        }
    }
}
