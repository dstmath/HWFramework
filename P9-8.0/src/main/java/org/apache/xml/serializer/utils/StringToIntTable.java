package org.apache.xml.serializer.utils;

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
        this.m_mapSize = this.m_blocksize;
        this.m_map = new String[this.m_blocksize];
        this.m_values = new int[this.m_blocksize];
    }

    public StringToIntTable(int blocksize) {
        this.m_firstFree = 0;
        this.m_blocksize = blocksize;
        this.m_mapSize = blocksize;
        this.m_map = new String[blocksize];
        this.m_values = new int[this.m_blocksize];
    }

    public final int getLength() {
        return this.m_firstFree;
    }

    public final void put(String key, int value) {
        if (this.m_firstFree + 1 >= this.m_mapSize) {
            this.m_mapSize += this.m_blocksize;
            String[] newMap = new String[this.m_mapSize];
            System.arraycopy(this.m_map, 0, newMap, 0, this.m_firstFree + 1);
            this.m_map = newMap;
            int[] newValues = new int[this.m_mapSize];
            System.arraycopy(this.m_values, 0, newValues, 0, this.m_firstFree + 1);
            this.m_values = newValues;
        }
        this.m_map[this.m_firstFree] = key;
        this.m_values[this.m_firstFree] = value;
        this.m_firstFree++;
    }

    public final int get(String key) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i].equals(key)) {
                return this.m_values[i];
            }
        }
        return -10000;
    }

    public final int getIgnoreCase(String key) {
        if (key == null) {
            return -10000;
        }
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i].equalsIgnoreCase(key)) {
                return this.m_values[i];
            }
        }
        return -10000;
    }

    public final boolean contains(String key) {
        for (int i = 0; i < this.m_firstFree; i++) {
            if (this.m_map[i].equals(key)) {
                return true;
            }
        }
        return false;
    }

    public final String[] keys() {
        String[] keysArr = new String[this.m_firstFree];
        for (int i = 0; i < this.m_firstFree; i++) {
            keysArr[i] = this.m_map[i];
        }
        return keysArr;
    }
}
