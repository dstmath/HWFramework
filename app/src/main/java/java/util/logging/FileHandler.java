package java.util.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import sun.util.locale.UnicodeLocaleExtension;

public class FileHandler extends StreamHandler {
    private static final int MAX_LOCKS = 100;
    private static HashMap<String, String> locks;
    private boolean append;
    private int count;
    private File[] files;
    private int limit;
    private String lockFileName;
    private FileOutputStream lockStream;
    private MeteredStream meter;
    private String pattern;

    private static class InitializationErrorManager extends ErrorManager {
        Exception lastException;

        private InitializationErrorManager() {
        }

        public void error(String msg, Exception ex, int code) {
            this.lastException = ex;
        }
    }

    private class MeteredStream extends OutputStream {
        OutputStream out;
        int written;

        MeteredStream(OutputStream out, int written) {
            this.out = out;
            this.written = written;
        }

        public void write(int b) throws IOException {
            this.out.write(b);
            this.written++;
        }

        public void write(byte[] buff) throws IOException {
            this.out.write(buff);
            this.written += buff.length;
        }

        public void write(byte[] buff, int off, int len) throws IOException {
            this.out.write(buff, off, len);
            this.written += len;
        }

        public void flush() throws IOException {
            this.out.flush();
        }

        public void close() throws IOException {
            this.out.close();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.logging.FileHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.logging.FileHandler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.FileHandler.<clinit>():void");
    }

    private void open(File fname, boolean append) throws IOException {
        int len = 0;
        if (append) {
            len = (int) fname.length();
        }
        this.meter = new MeteredStream(new BufferedOutputStream(new FileOutputStream(fname.toString(), append)), len);
        setOutputStream(this.meter);
    }

    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();
        this.pattern = manager.getStringProperty(cname + ".pattern", "%h/java%u.log");
        this.limit = manager.getIntProperty(cname + ".limit", 0);
        if (this.limit < 0) {
            this.limit = 0;
        }
        this.count = manager.getIntProperty(cname + ".count", 1);
        if (this.count <= 0) {
            this.count = 1;
        }
        this.append = manager.getBooleanProperty(cname + ".append", false);
        setLevel(manager.getLevelProperty(cname + ".level", Level.ALL));
        setFilter(manager.getFilterProperty(cname + ".filter", null));
        setFormatter(manager.getFormatterProperty(cname + ".formatter", new XMLFormatter()));
        try {
            setEncoding(manager.getStringProperty(cname + ".encoding", null));
        } catch (Exception e) {
            try {
                setEncoding(null);
            } catch (Exception e2) {
            }
        }
    }

    public FileHandler() throws IOException, SecurityException {
        checkPermission();
        configure();
        openFiles();
    }

    public FileHandler(String pattern) throws IOException, SecurityException {
        if (pattern.length() < 1) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern;
        this.limit = 0;
        this.count = 1;
        openFiles();
    }

    public FileHandler(String pattern, boolean append) throws IOException, SecurityException {
        if (pattern.length() < 1) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern;
        this.limit = 0;
        this.count = 1;
        this.append = append;
        openFiles();
    }

    public FileHandler(String pattern, int limit, int count) throws IOException, SecurityException {
        if (limit < 0 || count < 1 || pattern.length() < 1) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern;
        this.limit = limit;
        this.count = count;
        openFiles();
    }

    public FileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
        if (limit < 0 || count < 1 || pattern.length() < 1) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern;
        this.limit = limit;
        this.count = count;
        this.append = append;
        openFiles();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void openFiles() throws IOException {
        LogManager.getLogManager().checkPermission();
        if (this.count < 1) {
            throw new IllegalArgumentException("file count = " + this.count);
        }
        if (this.limit < 0) {
            this.limit = 0;
        }
        InitializationErrorManager em = new InitializationErrorManager();
        setErrorManager(em);
        int unique = -1;
        while (true) {
            unique++;
            if (unique > MAX_LOCKS) {
                break;
            }
            this.lockFileName = generate(this.pattern, 0, unique).toString() + ".lck";
            synchronized (locks) {
                if (locks.get(this.lockFileName) == null) {
                    try {
                        try {
                            try {
                                try {
                                    this.lockStream = new FileOutputStream(this.lockFileName);
                                    try {
                                        try {
                                            boolean available;
                                            FileChannel fc = this.lockStream.getChannel();
                                            try {
                                                available = fc.tryLock() != null;
                                            } catch (IOException e) {
                                                available = true;
                                            }
                                            if (available) {
                                                locks.put(this.lockFileName, this.lockFileName);
                                                this.files = new File[this.count];
                                                for (int i = 0; i < this.count; i++) {
                                                    this.files[i] = generate(this.pattern, i, unique);
                                                }
                                                if (this.append) {
                                                    open(this.files[0], true);
                                                } else {
                                                    rotate();
                                                }
                                                Object ex = em.lastException;
                                                if (ex == null) {
                                                    setErrorManager(new ErrorManager());
                                                    return;
                                                } else if (ex instanceof IOException) {
                                                    throw ((IOException) ex);
                                                } else if (ex instanceof SecurityException) {
                                                    throw ((SecurityException) ex);
                                                } else {
                                                    throw new IOException("Exception: " + ex);
                                                }
                                            }
                                            fc.close();
                                        } catch (IOException e2) {
                                        }
                                    } catch (IOException e3) {
                                    }
                                } catch (IOException e4) {
                                }
                            } catch (IOException e5) {
                            }
                        } catch (IOException e6) {
                        }
                    } catch (IOException e7) {
                    }
                }
            }
        }
    }

    private File generate(String pattern, int generation, int unique) throws IOException {
        File file = null;
        String word = "";
        int ix = 0;
        boolean sawg = false;
        boolean sawu = false;
        while (ix < pattern.length()) {
            char ch = pattern.charAt(ix);
            ix++;
            char ch2 = '\u0000';
            if (ix < pattern.length()) {
                ch2 = Character.toLowerCase(pattern.charAt(ix));
            }
            if (ch == '/') {
                if (file == null) {
                    file = new File(word);
                } else {
                    file = new File(file, word);
                }
                word = "";
            } else {
                if (ch == '%') {
                    if (ch2 == 't') {
                        String tmpDir = System.getProperty("java.io.tmpdir");
                        if (tmpDir == null) {
                            tmpDir = System.getProperty("user.home");
                        }
                        file = new File(tmpDir);
                        ix++;
                        word = "";
                    } else if (ch2 == 'h') {
                        file = new File(System.getProperty("user.home"));
                        ix++;
                        word = "";
                    } else if (ch2 == 'g') {
                        word = word + generation;
                        sawg = true;
                        ix++;
                    } else if (ch2 == UnicodeLocaleExtension.SINGLETON) {
                        word = word + unique;
                        sawu = true;
                        ix++;
                    } else if (ch2 == '%') {
                        word = word + "%";
                        ix++;
                    }
                }
                word = word + ch;
            }
        }
        if (this.count > 1 && !sawg) {
            word = word + "." + generation;
        }
        if (unique > 0 && !sawu) {
            word = word + "." + unique;
        }
        if (word.length() <= 0) {
            return file;
        }
        if (file == null) {
            return new File(word);
        }
        return new File(file, word);
    }

    private synchronized void rotate() {
        Level oldLevel = getLevel();
        setLevel(Level.OFF);
        super.close();
        for (int i = this.count - 2; i >= 0; i--) {
            File f1 = this.files[i];
            File f2 = this.files[i + 1];
            if (f1.exists()) {
                if (f2.exists()) {
                    f2.delete();
                }
                f1.renameTo(f2);
            }
        }
        try {
            open(this.files[0], false);
        } catch (IOException ix) {
            reportError(null, ix, 4);
        }
        setLevel(oldLevel);
    }

    public synchronized void publish(LogRecord record) {
        if (isLoggable(record)) {
            super.publish(record);
            flush();
            if (this.limit > 0 && this.meter.written >= this.limit) {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        FileHandler.this.rotate();
                        return null;
                    }
                });
            }
        }
    }

    public synchronized void close() throws SecurityException {
        super.close();
        if (this.lockFileName != null) {
            try {
                this.lockStream.close();
            } catch (Exception e) {
            }
            synchronized (locks) {
                locks.remove(this.lockFileName);
            }
            new File(this.lockFileName).delete();
            this.lockFileName = null;
            this.lockStream = null;
        }
    }
}
