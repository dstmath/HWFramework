package javax.xml.transform;

import java.io.PrintStream;
import java.io.PrintWriter;

public class TransformerException extends Exception {
    private static final long serialVersionUID = 975798773772956428L;
    Throwable containedException;
    SourceLocator locator;

    public SourceLocator getLocator() {
        return this.locator;
    }

    public void setLocator(SourceLocator location) {
        this.locator = location;
    }

    public Throwable getException() {
        return this.containedException;
    }

    public Throwable getCause() {
        if (this.containedException == this) {
            return null;
        }
        return this.containedException;
    }

    public synchronized Throwable initCause(Throwable cause) {
        if (this.containedException != null) {
            throw new IllegalStateException("Can't overwrite cause");
        } else if (cause != this) {
            this.containedException = cause;
        } else {
            throw new IllegalArgumentException("Self-causation not permitted");
        }
        return this;
    }

    public TransformerException(String message) {
        super(message);
        this.containedException = null;
        this.locator = null;
    }

    public TransformerException(Throwable e) {
        super(e.toString());
        this.containedException = e;
        this.locator = null;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public TransformerException(String message, Throwable e) {
        super(r0);
        String str;
        if (message == null || message.length() == 0) {
            str = e.toString();
        } else {
            str = message;
        }
        this.containedException = e;
        this.locator = null;
    }

    public TransformerException(String message, SourceLocator locator2) {
        super(message);
        this.containedException = null;
        this.locator = locator2;
    }

    public TransformerException(String message, SourceLocator locator2, Throwable e) {
        super(message);
        this.containedException = e;
        this.locator = locator2;
    }

    public String getMessageAndLocation() {
        StringBuilder sbuffer = new StringBuilder();
        String message = super.getMessage();
        if (message != null) {
            sbuffer.append(message);
        }
        if (this.locator != null) {
            String systemID = this.locator.getSystemId();
            int line = this.locator.getLineNumber();
            int column = this.locator.getColumnNumber();
            if (systemID != null) {
                sbuffer.append("; SystemID: ");
                sbuffer.append(systemID);
            }
            if (line != 0) {
                sbuffer.append("; Line#: ");
                sbuffer.append(line);
            }
            if (column != 0) {
                sbuffer.append("; Column#: ");
                sbuffer.append(column);
            }
        }
        return sbuffer.toString();
    }

    public String getLocationAsString() {
        if (this.locator == null) {
            return null;
        }
        StringBuilder sbuffer = new StringBuilder();
        String systemID = this.locator.getSystemId();
        int line = this.locator.getLineNumber();
        int column = this.locator.getColumnNumber();
        if (systemID != null) {
            sbuffer.append("; SystemID: ");
            sbuffer.append(systemID);
        }
        if (line != 0) {
            sbuffer.append("; Line#: ");
            sbuffer.append(line);
        }
        if (column != 0) {
            sbuffer.append("; Column#: ");
            sbuffer.append(column);
        }
        return sbuffer.toString();
    }

    public void printStackTrace() {
        printStackTrace(new PrintWriter(System.err, true));
    }

    public void printStackTrace(PrintStream s) {
        printStackTrace(new PrintWriter(s));
    }

    public void printStackTrace(PrintWriter s) {
        if (s == null) {
            s = new PrintWriter(System.err, true);
        }
        try {
            String locInfo = getLocationAsString();
            if (locInfo != null) {
                s.println(locInfo);
            }
            super.printStackTrace(s);
        } catch (Throwable th) {
        }
    }
}
