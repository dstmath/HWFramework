package ohos.global.icu.impl.duration.impl;

/* access modifiers changed from: package-private */
public interface RecordReader {
    boolean bool(String str);

    boolean[] boolArray(String str);

    char character(String str);

    char[] characterArray(String str);

    boolean close();

    byte namedIndex(String str, String[] strArr);

    byte[] namedIndexArray(String str, String[] strArr);

    boolean open(String str);

    String string(String str);

    String[] stringArray(String str);

    String[][] stringTable(String str);
}
