package java.text;

public interface CharacterIterator extends Cloneable {
    public static final char DONE = '￿';

    Object clone();

    char current();

    char first();

    int getBeginIndex();

    int getEndIndex();

    int getIndex();

    char last();

    char next();

    char previous();

    char setIndex(int i);
}
