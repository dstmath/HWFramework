package android.icu.util;

public interface ValueIterator {

    public static final class Element {
        public int integer;
        public Object value;
    }

    boolean next(Element element);

    void reset();

    void setRange(int i, int i2);
}
