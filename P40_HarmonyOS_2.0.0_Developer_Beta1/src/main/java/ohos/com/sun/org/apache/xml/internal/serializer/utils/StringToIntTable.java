package ohos.com.sun.org.apache.xml.internal.serializer.utils;

public final class StringToIntTable {
    public static final int INVALID_KEY = -10000;
    private int m_blocksize;
    private int m_firstFree;
    private String[] m_map;
    private int m_mapSize;
    private int[] m_values;

    public StringToIntTable() {
        this.m_firstFree = 0;
        this.m_blocksize = 8;
        int i = this.m_blocksize;
        this.m_mapSize = i;
        this.m_map = new String[i];
        this.m_values = new int[i];
    }

    public StringToIntTable(int i) {
        this.m_firstFree = 0;
        this.m_blocksize = i;
        this.m_mapSize = i;
        this.m_map = new String[i];
        this.m_values = new int[this.m_blocksize];
    }

    public final int getLength() {
        return this.m_firstFree;
    }

    public final void put(String str, int i) {
        int i2 = this.m_firstFree;
        int i3 = i2 + 1;
        int i4 = this.m_mapSize;
        if (i3 >= i4) {
            this.m_mapSize = i4 + this.m_blocksize;
            String[] strArr = new String[this.m_mapSize];
            System.arraycopy(this.m_map, 0, strArr, 0, i2 + 1);
            this.m_map = strArr;
            int[] iArr = new int[this.m_mapSize];
            System.arraycopy(this.m_values, 0, iArr, 0, this.m_firstFree + 1);
            this.m_values = iArr;
        }
        String[] strArr2 = this.m_map;
        int i5 = this.m_firstFree;
        strArr2[i5] = str;
        this.m_values[i5] = i;
        this.m_firstFree = i5 + 1;
    }

    public final int get(String str) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i].equals(str)) {
                return this.m_values[i];
            }
        }
        return -10000;
    }

    public final int getIgnoreCase(String str) {
        if (str == null) {
            return -10000;
        }
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i].equalsIgnoreCase(str)) {
                return this.m_values[i];
            }
        }
        return -10000;
    }

    public final boolean contains(String str) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i].equals(str)) {
                return true;
            }
        }
        return false;
    }

    public final String[] keys() {
        String[] strArr = new String[this.m_firstFree];
        for (int i = 0; i < this.m_firstFree; i++) {
            strArr[i] = this.m_map[i];
        }
        return strArr;
    }
}
