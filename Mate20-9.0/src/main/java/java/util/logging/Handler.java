package java.util.logging;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

public abstract class Handler {
    private static final int offValue = Level.OFF.intValue();
    private volatile String encoding;
    private volatile ErrorManager errorManager = new ErrorManager();
    private volatile Filter filter;
    private volatile Formatter formatter;
    private volatile Level logLevel = Level.ALL;
    private final LogManager manager = LogManager.getLogManager();
    boolean sealed = true;

    public abstract void close() throws SecurityException;

    public abstract void flush();

    public abstract void publish(LogRecord logRecord);

    protected Handler() {
    }

    public synchronized void setFormatter(Formatter newFormatter) throws SecurityException {
        checkPermission();
        newFormatter.getClass();
        this.formatter = newFormatter;
    }

    public Formatter getFormatter() {
        return this.formatter;
    }

    public synchronized void setEncoding(String encoding2) throws SecurityException, UnsupportedEncodingException {
        checkPermission();
        if (encoding2 != null) {
            try {
                if (!Charset.isSupported(encoding2)) {
                    throw new UnsupportedEncodingException(encoding2);
                }
            } catch (IllegalCharsetNameException e) {
                throw new UnsupportedEncodingException(encoding2);
            }
        }
        this.encoding = encoding2;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public synchronized void setFilter(Filter newFilter) throws SecurityException {
        checkPermission();
        this.filter = newFilter;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public synchronized void setErrorManager(ErrorManager em) {
        checkPermission();
        if (em != null) {
            this.errorManager = em;
        } else {
            throw new NullPointerException();
        }
    }

    public ErrorManager getErrorManager() {
        checkPermission();
        return this.errorManager;
    }

    /* access modifiers changed from: protected */
    public void reportError(String msg, Exception ex, int code) {
        try {
            this.errorManager.error(msg, ex, code);
        } catch (Exception ex2) {
            System.err.println("Handler.reportError caught:");
            ex2.printStackTrace();
        }
    }

    public synchronized void setLevel(Level newLevel) throws SecurityException {
        if (newLevel != null) {
            checkPermission();
            this.logLevel = newLevel;
        } else {
            throw new NullPointerException();
        }
    }

    public Level getLevel() {
        return this.logLevel;
    }

    public boolean isLoggable(LogRecord record) {
        int levelValue = getLevel().intValue();
        if (record.getLevel().intValue() < levelValue || levelValue == offValue) {
            return false;
        }
        Filter filter2 = getFilter();
        if (filter2 == null) {
            return true;
        }
        return filter2.isLoggable(record);
    }

    /* access modifiers changed from: package-private */
    public void checkPermission() throws SecurityException {
        if (this.sealed) {
            this.manager.checkPermission();
        }
    }
}
