package android.icu.text;

public interface UForwardCharacterIterator {
    public static final int DONE = -1;

    int next();

    int nextCodePoint();
}
