package tmsdk.bg.tcc;

public class TelNumberLocatorException extends RuntimeException {
    private static final long serialVersionUID = 1;
    private int error;

    public TelNumberLocatorException(int i) {
        super("TelNumberLocator error" + Integer.toString(i));
        this.error = i;
    }

    public int getError() {
        return this.error;
    }
}
