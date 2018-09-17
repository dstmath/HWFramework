package android.icu.impl.duration.impl;

interface RecordWriter {
    void bool(String str, boolean z);

    void boolArray(String str, boolean[] zArr);

    void character(String str, char c);

    void characterArray(String str, char[] cArr);

    boolean close();

    void namedIndex(String str, String[] strArr, int i);

    void namedIndexArray(String str, String[] strArr, byte[] bArr);

    boolean open(String str);

    void string(String str, String str2);

    void stringArray(String str, String[] strArr);

    void stringTable(String str, String[][] strArr);
}
