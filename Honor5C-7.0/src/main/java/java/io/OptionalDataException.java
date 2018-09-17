package java.io;

public class OptionalDataException extends ObjectStreamException {
    private static final long serialVersionUID = -8011121865681257820L;
    public boolean eof;
    public int length;

    OptionalDataException(int len) {
        this.eof = false;
        this.length = len;
    }

    OptionalDataException(boolean end) {
        this.length = 0;
        this.eof = end;
    }
}
