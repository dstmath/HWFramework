package jcifs.util.transport;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class TransportException extends IOException {
    private Throwable rootCause;

    public TransportException() {
    }

    public TransportException(String msg) {
        super(msg);
    }

    public TransportException(Throwable rootCause2) {
        this.rootCause = rootCause2;
    }

    public TransportException(String msg, Throwable rootCause2) {
        super(msg);
        this.rootCause = rootCause2;
    }

    public Throwable getRootCause() {
        return this.rootCause;
    }

    public String toString() {
        if (this.rootCause == null) {
            return super.toString();
        }
        StringWriter sw = new StringWriter();
        this.rootCause.printStackTrace(new PrintWriter(sw));
        return super.toString() + "\n" + sw;
    }
}
