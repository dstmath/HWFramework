package java.util;

public class TooManyListenersException extends Exception {
    private static final long serialVersionUID = 5074640544770687831L;

    public TooManyListenersException() {
    }

    public TooManyListenersException(String s) {
        super(s);
    }
}
