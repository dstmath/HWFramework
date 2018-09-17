package android.icu.text;

public interface Replaceable {
    int char32At(int i);

    char charAt(int i);

    void copy(int i, int i2, int i3);

    void getChars(int i, int i2, char[] cArr, int i3);

    boolean hasMetaData();

    int length();

    void replace(int i, int i2, String str);

    void replace(int i, int i2, char[] cArr, int i3, int i4);
}
