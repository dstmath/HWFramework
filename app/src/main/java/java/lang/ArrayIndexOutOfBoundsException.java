package java.lang;

public class ArrayIndexOutOfBoundsException extends IndexOutOfBoundsException {
    private static final long serialVersionUID = -5116101128118950844L;

    public ArrayIndexOutOfBoundsException(int index) {
        super("Array index out of range: " + index);
    }

    public ArrayIndexOutOfBoundsException(String s) {
        super(s);
    }

    public ArrayIndexOutOfBoundsException(int sourceLength, int index) {
        super("length=" + sourceLength + "; index=" + index);
    }

    public ArrayIndexOutOfBoundsException(int sourceLength, int offset, int count) {
        super("length=" + sourceLength + "; regionStart=" + offset + "; regionLength=" + count);
    }
}
