package android.os;

import android.hardware.camera2.params.TonemapCurve;
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
    private static volatile boolean sShouldDefuse = false;
    private ClassLoader mClassLoader;
    int mFlags;
    ArrayMap<String, Object> mMap;
    Parcel mParcelledData;

    static final class NoImagePreloadHolder {
        public static final Parcel EMPTY_PARCEL = Parcel.obtain();

        NoImagePreloadHolder() {
        }
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
        copyInternal(b, false);
    }

    BaseBundle(boolean doInit) {
        this.mMap = null;
        this.mParcelledData = null;
    }

    public String getPairValue() {
        unparcel();
        int size = this.mMap.size();
        if (size > 1) {
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

    void unparcel() {
        synchronized (this) {
            Parcel parcelledData = this.mParcelledData;
            if (parcelledData == null) {
            } else if (isEmptyParcel()) {
                if (this.mMap == null) {
                    this.mMap = new ArrayMap(1);
                } else {
                    this.mMap.erase();
                }
                this.mParcelledData = null;
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
                    }
                    throw e;
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
        return this.mParcelledData != null;
    }

    public boolean isEmptyParcel() {
        return this.mParcelledData == NoImagePreloadHolder.EMPTY_PARCEL;
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

    public boolean maybeIsEmpty() {
        if (isParcelled()) {
            return isEmptyParcel();
        }
        return isEmpty();
    }

    public boolean kindofEquals(BaseBundle other) {
        boolean z = false;
        if (other == null || isParcelled() != other.isParcelled()) {
            return false;
        }
        if (!isParcelled()) {
            return this.mMap.equals(other.mMap);
        }
        if (this.mParcelledData.compareData(other.mParcelledData) == 0) {
            z = true;
        }
        return z;
    }

    public void clear() {
        unparcel();
        this.mMap.clear();
    }

    void copyInternal(BaseBundle from, boolean deep) {
        synchronized (from) {
            if (from.mParcelledData == null) {
                this.mParcelledData = null;
            } else if (from.isEmptyParcel()) {
                this.mParcelledData = NoImagePreloadHolder.EMPTY_PARCEL;
            } else {
                this.mParcelledData = Parcel.obtain();
                this.mParcelledData.appendFrom(from.mParcelledData, 0, from.mParcelledData.dataSize());
                this.mParcelledData.setDataPosition(0);
            }
            if (from.mMap == null) {
                this.mMap = null;
            } else if (deep) {
                ArrayMap<String, Object> fromMap = from.mMap;
                int N = fromMap.size();
                this.mMap = new ArrayMap(N);
                for (int i = 0; i < N; i++) {
                    this.mMap.append((String) fromMap.keyAt(i), deepCopyValue(fromMap.valueAt(i)));
                }
            } else {
                this.mMap = new ArrayMap(from.mMap);
            }
            this.mClassLoader = from.mClassLoader;
        }
    }

    Object deepCopyValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Bundle) {
            return ((Bundle) value).deepCopy();
        }
        if (value instanceof PersistableBundle) {
            return ((PersistableBundle) value).deepCopy();
        }
        if (value instanceof ArrayList) {
            return deepcopyArrayList((ArrayList) value);
        }
        if (value.getClass().isArray()) {
            if (value instanceof int[]) {
                return ((int[]) value).clone();
            }
            if (value instanceof long[]) {
                return ((long[]) value).clone();
            }
            if (value instanceof float[]) {
                return ((float[]) value).clone();
            }
            if (value instanceof double[]) {
                return ((double[]) value).clone();
            }
            if (value instanceof Object[]) {
                return ((Object[]) value).clone();
            }
            if (value instanceof byte[]) {
                return ((byte[]) value).clone();
            }
            if (value instanceof short[]) {
                return ((short[]) value).clone();
            }
            if (value instanceof char[]) {
                return ((char[]) value).clone();
            }
        }
        return value;
    }

    ArrayList deepcopyArrayList(ArrayList from) {
        int N = from.size();
        ArrayList out = new ArrayList(N);
        for (int i = 0; i < N; i++) {
            out.add(deepCopyValue(from.get(i)));
        }
        return out;
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
        return getBoolean(key, false);
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
        return getChar(key, 0);
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
        return getFloat(key, TonemapCurve.LEVEL_BLACK);
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

    /* JADX WARNING: Missing block: B:9:0x0014, code:
            return;
     */
    /* JADX WARNING: Missing block: B:18:0x0031, code:
            if (r3 == null) goto L_0x0039;
     */
    /* JADX WARNING: Missing block: B:20:0x0037, code:
            if (r3.size() > 0) goto L_0x003d;
     */
    /* JADX WARNING: Missing block: B:21:0x0039, code:
            r9.writeInt(0);
     */
    /* JADX WARNING: Missing block: B:22:0x003c, code:
            return;
     */
    /* JADX WARNING: Missing block: B:23:0x003d, code:
            r2 = r9.dataPosition();
            r9.writeInt(-1);
            r9.writeInt(BUNDLE_MAGIC);
            r4 = r9.dataPosition();
            r9.writeArrayMapInternal(r3);
            r0 = r9.dataPosition();
            r9.setDataPosition(r2);
            r9.writeInt(r0 - r4);
            r9.setDataPosition(r0);
     */
    /* JADX WARNING: Missing block: B:24:0x005e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void writeToParcelInner(Parcel parcel, int flags) {
        synchronized (this) {
            if (this.mParcelledData == null) {
                ArrayMap<String, Object> map = this.mMap;
            } else if (this.mParcelledData == NoImagePreloadHolder.EMPTY_PARCEL) {
                parcel.writeInt(0);
            } else {
                int length = this.mParcelledData.dataSize();
                parcel.writeInt(length);
                parcel.writeInt(BUNDLE_MAGIC);
                parcel.appendFrom(this.mParcelledData, 0, length);
            }
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
            p.adoptClassCookies(parcel);
            p.setDataPosition(0);
            this.mParcelledData = p;
        }
    }
}
