package ohos.com.sun.org.apache.xml.internal.utils;

public class StringToStringTable {
    private int m_blocksize;
    private int m_firstFree;
    private String[] m_map;
    private int m_mapSize;

    public StringToStringTable() {
        this.m_firstFree = 0;
        this.m_blocksize = 16;
        int i = this.m_blocksize;
        this.m_mapSize = i;
        this.m_map = new String[i];
    }

    public StringToStringTable(int i) {
        this.m_firstFree = 0;
        this.m_blocksize = i;
        this.m_mapSize = i;
        this.m_map = new String[i];
    }

    public final int getLength() {
        return this.m_firstFree;
    }

    public final void put(String str, String str2) {
        int i = this.m_firstFree;
        int i2 = i + 2;
        int i3 = this.m_mapSize;
        if (i2 >= i3) {
            this.m_mapSize = i3 + this.m_blocksize;
            String[] strArr = new String[this.m_mapSize];
            System.arraycopy(this.m_map, 0, strArr, 0, i + 1);
            this.m_map = strArr;
        }
        String[] strArr2 = this.m_map;
        int i4 = this.m_firstFree;
        strArr2[i4] = str;
        this.m_firstFree = i4 + 1;
        int i5 = this.m_firstFree;
        strArr2[i5] = str2;
        this.m_firstFree = i5 + 1;
    }

    public final String get(String str) {
        for (int i = 0; i < this.m_firstFree; i += 2) {
            if (this.m_map[i].equals(str)) {
                return this.m_map[i + 1];
            }
        }
        return null;
    }

    public final void remove(String str) {
        for (int i = 0; i < this.m_firstFree; i += 2) {
            if (this.m_map[i].equals(str)) {
                int i2 = i + 2;
                int i3 = this.m_firstFree;
                if (i2 < i3) {
                    String[] strArr = this.m_map;
                    System.arraycopy(strArr, i2, strArr, i, i3 - i2);
                }
                this.m_firstFree -= 2;
                String[] strArr2 = this.m_map;
                int i4 = this.m_firstFree;
                strArr2[i4] = null;
                strArr2[i4 + 1] = null;
                return;
            }
        }
    }

    public final String getIgnoreCase(String str) {
        if (str == null) {
            return null;
        }
        for (int i = 0; i < this.m_firstFree; i += 2) {
            if (this.m_map[i].equalsIgnoreCase(str)) {
                return this.m_map[i + 1];
            }
        }
        return null;
    }

    public final String getByValue(String str) {
        for (int i = 1; i < this.m_firstFree; i += 2) {
            if (this.m_map[i].equals(str)) {
                return this.m_map[i - 1];
            }
        }
        return null;
    }

    public final String elementAt(int i) {
        return this.m_map[i];
    }

    public final boolean contains(String str) {
        for (int i = 0; i < this.m_firstFree; i += 2) {
            if (this.m_map[i].equals(str)) {
                return true;
            }
        }
        return false;
    }

    public final boolean containsValue(String str) {
        for (int i = 1; i < this.m_firstFree; i += 2) {
            if (this.m_map[i].equals(str)) {
                return true;
            }
        }
        return false;
    }
}
