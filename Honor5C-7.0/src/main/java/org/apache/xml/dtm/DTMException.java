package org.apache.xml.dtm;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.transform.SourceLocator;
import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;

public class DTMException extends RuntimeException {
    static final long serialVersionUID = -775576419181334734L;
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
        if (this.containedException == null && cause != null) {
            throw new IllegalStateException(XMLMessages.createXMLMessage(XMLErrorResources.ER_CANNOT_OVERWRITE_CAUSE, null));
        } else if (cause == this) {
            throw new IllegalArgumentException(XMLMessages.createXMLMessage(XMLErrorResources.ER_SELF_CAUSATION_NOT_PERMITTED, null));
        } else {
            this.containedException = cause;
        }
        return this;
    }

    public DTMException(String message) {
        super(message);
        this.containedException = null;
        this.locator = null;
    }

    public DTMException(Throwable e) {
        super(e.getMessage());
        this.containedException = e;
        this.locator = null;
    }

    public DTMException(String message, Throwable e) {
        if (message == null || message.length() == 0) {
            message = e.getMessage();
        }
        super(message);
        this.containedException = e;
        this.locator = null;
    }

    public DTMException(String message, SourceLocator locator) {
        super(message);
        this.containedException = null;
        this.locator = locator;
    }

    public DTMException(String message, SourceLocator locator, Throwable e) {
        super(message);
        this.containedException = e;
        this.locator = locator;
    }

    public String getMessageAndLocation() {
        StringBuffer sbuffer = new StringBuffer();
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
        StringBuffer sbuffer = new StringBuffer();
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
        String locInfo;
        if (s == null) {
            PrintWriter printWriter = new PrintWriter(System.err, true);
        }
        try {
            locInfo = getLocationAsString();
            if (locInfo != null) {
                s.println(locInfo);
            }
            super.printStackTrace(s);
        } catch (Throwable th) {
        }
        boolean isJdk14OrHigher = false;
        try {
            Throwable.class.getMethod("getCause", new Class[]{(Class) null});
            isJdk14OrHigher = true;
        } catch (NoSuchMethodException e) {
        }
        if (!isJdk14OrHigher) {
            Throwable exception = getException();
            for (int i = 0; i < 10 && exception != null; i++) {
                s.println("---------");
                try {
                    if (exception instanceof DTMException) {
                        locInfo = ((DTMException) exception).getLocationAsString();
                        if (locInfo != null) {
                            s.println(locInfo);
                        }
                    }
                    exception.printStackTrace(s);
                } catch (Throwable th2) {
                    s.println("Could not print stack trace...");
                }
                try {
                    Method meth = exception.getClass().getMethod("getException", new Class[]{(Class) null});
                    if (meth != null) {
                        Throwable prev = exception;
                        exception = (Throwable) meth.invoke(exception, new Object[]{(Class) null});
                        if (prev == exception) {
                            return;
                        }
                    } else {
                        exception = null;
                    }
                } catch (InvocationTargetException e2) {
                    exception = null;
                } catch (IllegalAccessException e3) {
                    exception = null;
                } catch (NoSuchMethodException e4) {
                    exception = null;
                }
            }
        }
    }
}
