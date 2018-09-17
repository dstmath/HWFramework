package android.database;

public class CursorIndexOutOfBoundsException extends IndexOutOfBoundsException {
    public CursorIndexOutOfBoundsException(int index, int size) {
        super("Index " + index + " requested, with a size of " + size);
    }

    public CursorIndexOutOfBoundsException(String message) {
        super(message);
    }
}
