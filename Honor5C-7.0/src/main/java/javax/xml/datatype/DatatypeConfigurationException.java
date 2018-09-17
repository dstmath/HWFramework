package javax.xml.datatype;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class DatatypeConfigurationException extends Exception {
    private static final long serialVersionUID = -1699373159027047238L;
    private Throwable causeOnJDK13OrBelow;
    private transient boolean isJDK14OrAbove;

    public DatatypeConfigurationException() {
        this.isJDK14OrAbove = false;
    }

    public DatatypeConfigurationException(String message) {
        super(message);
        this.isJDK14OrAbove = false;
    }

    public DatatypeConfigurationException(String message, Throwable cause) {
        super(message);
        this.isJDK14OrAbove = false;
        initCauseByReflection(cause);
    }

    public DatatypeConfigurationException(Throwable cause) {
        String str = null;
        if (cause != null) {
            str = cause.toString();
        }
        super(str);
        this.isJDK14OrAbove = false;
        initCauseByReflection(cause);
    }

    public void printStackTrace() {
        if (this.isJDK14OrAbove || this.causeOnJDK13OrBelow == null) {
            super.printStackTrace();
        } else {
            printStackTrace0(new PrintWriter(System.err, true));
        }
    }

    public void printStackTrace(PrintStream s) {
        if (this.isJDK14OrAbove || this.causeOnJDK13OrBelow == null) {
            super.printStackTrace(s);
        } else {
            printStackTrace0(new PrintWriter(s));
        }
    }

    public void printStackTrace(PrintWriter s) {
        if (this.isJDK14OrAbove || this.causeOnJDK13OrBelow == null) {
            super.printStackTrace(s);
        } else {
            printStackTrace0(s);
        }
    }

    private void printStackTrace0(PrintWriter s) {
        this.causeOnJDK13OrBelow.printStackTrace(s);
        s.println("------------------------------------------");
        super.printStackTrace(s);
    }

    private void initCauseByReflection(Throwable cause) {
        this.causeOnJDK13OrBelow = cause;
        try {
            getClass().getMethod("initCause", new Class[]{Throwable.class}).invoke(this, new Object[]{cause});
            this.isJDK14OrAbove = true;
        } catch (Exception e) {
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            Throwable cause = (Throwable) getClass().getMethod("getCause", new Class[0]).invoke(this, new Object[0]);
            if (this.causeOnJDK13OrBelow == null) {
                this.causeOnJDK13OrBelow = cause;
            } else if (cause == null) {
                getClass().getMethod("initCause", new Class[]{Throwable.class}).invoke(this, new Object[]{this.causeOnJDK13OrBelow});
            }
            this.isJDK14OrAbove = true;
        } catch (Exception e) {
        }
    }
}
