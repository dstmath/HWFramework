package android.zrhung;

import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;
import java.util.Set;

public class ZrHungData {
    private static final String TAG = "ZrHungData";
    private ArrayMap<String, Object> mMap;

    public ZrHungData() {
        this.mMap = null;
        this.mMap = new ArrayMap<>();
    }

    public ArrayMap<String, Object> getMap() {
        return this.mMap;
    }

    public int size() {
        return this.mMap.size();
    }

    public boolean isEmpty() {
        return this.mMap.isEmpty();
    }

    public void clear() {
        this.mMap.clear();
    }

    public Object get(String key) {
        return this.mMap.get(key);
    }

    public void remove(String key) {
        this.mMap.remove(key);
    }

    public void put(String key, Object obj) {
        this.mMap.put(key, obj);
    }

    public void putAll(ArrayMap map) {
        this.mMap.putAll((ArrayMap<? extends String, ? extends Object>) map);
    }

    public Set<String> keySet() {
        return this.mMap.keySet();
    }

    public void putBoolean(String key, boolean value) {
        this.mMap.put(key, Boolean.valueOf(value));
    }

    public void putByte(String key, byte value) {
        this.mMap.put(key, Byte.valueOf(value));
    }

    public void putChar(String key, char value) {
        this.mMap.put(key, Character.valueOf(value));
    }

    public void putShort(String key, short value) {
        this.mMap.put(key, Short.valueOf(value));
    }

    public void putInt(String key, int value) {
        this.mMap.put(key, Integer.valueOf(value));
    }

    public void putLong(String key, long value) {
        this.mMap.put(key, Long.valueOf(value));
    }

    public void putFloat(String key, float value) {
        this.mMap.put(key, Float.valueOf(value));
    }

    public void putDouble(String key, double value) {
        this.mMap.put(key, Double.valueOf(value));
    }

    public void putString(String key, String value) {
        this.mMap.put(key, value);
    }

    public void putCharSequence(String key, CharSequence value) {
        this.mMap.put(key, value);
    }

    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        this.mMap.put(key, value);
    }

    public void putStringArrayList(String key, ArrayList<String> value) {
        this.mMap.put(key, value);
    }

    public void putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        this.mMap.put(key, value);
    }

    public void putBooleanArray(String key, boolean[] value) {
        this.mMap.put(key, value);
    }

    public void putByteArray(String key, byte[] value) {
        this.mMap.put(key, value);
    }

    public void putShortArray(String key, short[] value) {
        this.mMap.put(key, value);
    }

    public void putCharArray(String key, char[] value) {
        this.mMap.put(key, value);
    }

    public void putIntArray(String key, int[] value) {
        this.mMap.put(key, value);
    }

    public void putLongArray(String key, long[] value) {
        this.mMap.put(key, value);
    }

    public void putFloatArray(String key, float[] value) {
        this.mMap.put(key, value);
    }

    public void putDoubleArray(String key, double[] value) {
        this.mMap.put(key, value);
    }

    public void putStringArray(String key, String[] value) {
        this.mMap.put(key, value);
    }

    public void putCharSequenceArray(String key, CharSequence[] value) {
        this.mMap.put(key, value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object object = this.mMap.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return ((Boolean) object).booleanValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public byte getByte(String key) {
        return getByte(key, (byte) 0).byteValue();
    }

    public Byte getByte(String key, byte defaultValue) {
        Object object = this.mMap.get(key);
        if (object == null) {
            return Byte.valueOf(defaultValue);
        }
        try {
            return (Byte) object;
        } catch (ClassCastException e) {
            return Byte.valueOf(defaultValue);
        }
    }

    public char getChar(String key) {
        return getChar(key, 0);
    }

    public char getChar(String key, char defaultValue) {
        Object object = this.mMap.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return ((Character) object).charValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public short getShort(String key) {
        return getShort(key, 0);
    }

    public short getShort(String key, short defaultValue) {
        Object object = this.mMap.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return ((Short) object).shortValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        Object object = this.mMap.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return ((Integer) object).intValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        Object object = this.mMap.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return ((Long) object).longValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    public float getFloat(String key, float defaultValue) {
        Object object = this.mMap.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return ((Float) object).floatValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key) {
        return getDouble(key, 0.0d);
    }

    public double getDouble(String key, double defaultValue) {
        Object object = this.mMap.get(key);
        if (object == null) {
            return defaultValue;
        }
        try {
            return ((Double) object).doubleValue();
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public String getString(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof String) {
            return (String) object;
        }
        return null;
    }

    public String getString(String key, String defaultValue) {
        String str = getString(key);
        return str == null ? defaultValue : str;
    }

    public CharSequence getCharSequence(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof CharSequence) {
            return (CharSequence) object;
        }
        return null;
    }

    public CharSequence getCharSequence(String key, CharSequence defaultValue) {
        CharSequence cs = getCharSequence(key);
        return cs == null ? defaultValue : cs;
    }

    public ArrayList<Integer> getIntegerArrayList(String key) {
        try {
            return (ArrayList) this.mMap.get(key);
        } catch (ClassCastException e) {
            Log.i(TAG, "getIntegerArrayList ClassCastException");
            return null;
        }
    }

    public ArrayList<String> getStringArrayList(String key) {
        try {
            return (ArrayList) this.mMap.get(key);
        } catch (ClassCastException e) {
            Log.i(TAG, "getStringArrayList ClassCastException");
            return null;
        }
    }

    public ArrayList<CharSequence> getCharSequenceArrayList(String key) {
        try {
            return (ArrayList) this.mMap.get(key);
        } catch (ClassCastException e) {
            Log.i(TAG, "getStringArrayList ClassCastException");
            return null;
        }
    }

    public boolean[] getBooleanArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof boolean[]) {
            return (boolean[]) object;
        }
        return null;
    }

    public byte[] getByteArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof byte[]) {
            return (byte[]) object;
        }
        return null;
    }

    public short[] getShortArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof short[]) {
            return (short[]) object;
        }
        return null;
    }

    public char[] getCharArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof char[]) {
            return (char[]) object;
        }
        return null;
    }

    public int[] getIntArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof int[]) {
            return (int[]) object;
        }
        return null;
    }

    public long[] getLongArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof long[]) {
            return (long[]) object;
        }
        return null;
    }

    public float[] getFloatArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof float[]) {
            return (float[]) object;
        }
        return null;
    }

    public double[] getDoubleArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof double[]) {
            return (double[]) object;
        }
        return null;
    }

    public String[] getStringArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof String[]) {
            return (String[]) object;
        }
        return null;
    }

    public CharSequence[] getCharSequenceArray(String key) {
        Object object = this.mMap.get(key);
        if (object instanceof CharSequence[]) {
            return (CharSequence[]) object;
        }
        return null;
    }
}
