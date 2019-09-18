package javax.xml.xpath;

import java.io.PrintStream;
import java.io.PrintWriter;

public class XPathException extends Exception {
    private static final long serialVersionUID = -1837080260374986980L;
    private final Throwable cause;

    public XPathException(String message) {
        super(message);
        if (message != null) {
            this.cause = null;
            return;
        }
        throw new NullPointerException("message == null");
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public XPathException(Throwable cause2) {
        super(cause2 == null ? null : cause2.toString());
        this.cause = cause2;
        if (cause2 == null) {
            throw new NullPointerException("cause == null");
        }
    }

    public Throwable getCause() {
        return this.cause;
    }

    public void printStackTrace(PrintStream s) {
        if (getCause() != null) {
            getCause().printStackTrace(s);
            s.println("--------------- linked to ------------------");
        }
        super.printStackTrace(s);
    }

    public void printStackTrace() {
        printStackTrace(System.err);
    }

    public void printStackTrace(PrintWriter s) {
        if (getCause() != null) {
            getCause().printStackTrace(s);
            s.println("--------------- linked to ------------------");
        }
        super.printStackTrace(s);
    }
}
