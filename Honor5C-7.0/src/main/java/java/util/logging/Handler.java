package java.util.logging;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

public abstract class Handler {
    private static final int offValue = 0;
    private String encoding;
    private ErrorManager errorManager;
    private Filter filter;
    private Formatter formatter;
    private Level logLevel;
    private LogManager manager;
    boolean sealed;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.logging.Handler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.logging.Handler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.Handler.<clinit>():void");
    }

    public abstract void close() throws SecurityException;

    public abstract void flush();

    public abstract void publish(LogRecord logRecord);

    protected Handler() {
        this.manager = LogManager.getLogManager();
        this.logLevel = Level.ALL;
        this.errorManager = new ErrorManager();
        this.sealed = true;
    }

    public void setFormatter(Formatter newFormatter) throws SecurityException {
        checkPermission();
        newFormatter.getClass();
        this.formatter = newFormatter;
    }

    public Formatter getFormatter() {
        return this.formatter;
    }

    public void setEncoding(String encoding) throws SecurityException, UnsupportedEncodingException {
        checkPermission();
        if (encoding != null) {
            try {
                if (!Charset.isSupported(encoding)) {
                    throw new UnsupportedEncodingException(encoding);
                }
            } catch (IllegalCharsetNameException e) {
                throw new UnsupportedEncodingException(encoding);
            }
        }
        this.encoding = encoding;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setFilter(Filter newFilter) throws SecurityException {
        checkPermission();
        this.filter = newFilter;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public void setErrorManager(ErrorManager em) {
        checkPermission();
        if (em == null) {
            throw new NullPointerException();
        }
        this.errorManager = em;
    }

    public ErrorManager getErrorManager() {
        checkPermission();
        return this.errorManager;
    }

    protected void reportError(String msg, Exception ex, int code) {
        try {
            this.errorManager.error(msg, ex, code);
        } catch (Exception ex2) {
            System.err.println("Handler.reportError caught:");
            ex2.printStackTrace();
        }
    }

    public synchronized void setLevel(Level newLevel) throws SecurityException {
        if (newLevel == null) {
            throw new NullPointerException();
        }
        checkPermission();
        this.logLevel = newLevel;
    }

    public synchronized Level getLevel() {
        return this.logLevel;
    }

    public boolean isLoggable(LogRecord record) {
        int levelValue = getLevel().intValue();
        if (record.getLevel().intValue() < levelValue || levelValue == offValue) {
            return false;
        }
        Filter filter = getFilter();
        if (filter == null) {
            return true;
        }
        return filter.isLoggable(record);
    }

    void checkPermission() throws SecurityException {
        if (this.sealed) {
            this.manager.checkPermission();
        }
    }
}
