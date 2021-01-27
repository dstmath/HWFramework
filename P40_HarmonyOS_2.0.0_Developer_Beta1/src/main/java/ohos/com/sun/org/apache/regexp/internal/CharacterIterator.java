package ohos.com.sun.org.apache.regexp.internal;

public interface CharacterIterator {
    char charAt(int i);

    boolean isEnd(int i);

    String substring(int i);

    String substring(int i, int i2);
}
