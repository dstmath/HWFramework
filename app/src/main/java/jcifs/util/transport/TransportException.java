package jcifs.util.transport;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class TransportException extends IOException {
    private Throwable rootCause;

    public TransportException(String msg) {
        super(msg);
    }

    public TransportException(Throwable rootCause) {
        this.rootCause = rootCause;
    }

    public TransportException(String msg, Throwable rootCause) {
        super(msg);
        this.rootCause = rootCause;
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
