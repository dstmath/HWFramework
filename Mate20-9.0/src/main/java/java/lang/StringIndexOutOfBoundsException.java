package java.lang;

public class StringIndexOutOfBoundsException extends IndexOutOfBoundsException {
    private static final long serialVersionUID = -6762910422159637258L;

    public StringIndexOutOfBoundsException() {
    }

    public StringIndexOutOfBoundsException(String s) {
        super(s);
    }

    public StringIndexOutOfBoundsException(int index) {
        super("String index out of range: " + index);
    }

    StringIndexOutOfBoundsException(String s, int index) {
        this(s.length(), index);
    }

    StringIndexOutOfBoundsException(int sourceLength, int index) {
        super("length=" + sourceLength + "; index=" + index);
    }

    StringIndexOutOfBoundsException(String s, int offset, int count) {
        this(s.length(), offset, count);
    }

    StringIndexOutOfBoundsException(int sourceLength, int offset, int count) {
        super("length=" + sourceLength + "; regionStart=" + offset + "; regionLength=" + count);
    }
}
