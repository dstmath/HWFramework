package java.util.logging;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

public class FileHandler extends StreamHandler {
    private static final int MAX_LOCKS = 100;
    private static final Set<String> locks = new HashSet();
    private boolean append;
    private int count;
    private File[] files;
    private int limit;
    private FileChannel lockFileChannel;
    private String lockFileName;
    private MeteredStream meter;
    private String pattern;

    private static class InitializationErrorManager extends ErrorManager {
        Exception lastException;

        /* synthetic */ InitializationErrorManager(InitializationErrorManager -this0) {
            this();
        }

        private InitializationErrorManager() {
        }

        public void error(String msg, Exception ex, int code) {
            this.lastException = ex;
        }
    }

    private class MeteredStream extends OutputStream {
        final OutputStream out;
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

    private boolean isParentWritable(Path path) {
        Path parent = path.getParent();
        if (parent == null) {
            parent = path.toAbsolutePath().getParent();
        }
        return parent != null ? Files.isWritable(parent) : false;
    }

    /* JADX WARNING: Missing block: B:40:?, code:
            locks.add(r23.lockFileName);
     */
    /* JADX WARNING: Missing block: B:42:0x012a, code:
            r23.files = new java.io.File[r23.count];
            r8 = 0;
     */
    /* JADX WARNING: Missing block: B:44:0x0145, code:
            if (r8 >= r23.count) goto L_0x01b8;
     */
    /* JADX WARNING: Missing block: B:45:0x0147, code:
            r23.files[r8] = generate(r23.pattern, r8, r15);
            r8 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:69:0x01be, code:
            if (r23.append == false) goto L_0x01e2;
     */
    /* JADX WARNING: Missing block: B:70:0x01c0, code:
            open(r23.files[0], true);
     */
    /* JADX WARNING: Missing block: B:71:0x01d5, code:
            r6 = r5.lastException;
     */
    /* JADX WARNING: Missing block: B:72:0x01d7, code:
            if (r6 == null) goto L_0x020b;
     */
    /* JADX WARNING: Missing block: B:74:0x01dd, code:
            if ((r6 instanceof java.io.IOException) == false) goto L_0x01e6;
     */
    /* JADX WARNING: Missing block: B:76:0x01e1, code:
            throw ((java.io.IOException) r6);
     */
    /* JADX WARNING: Missing block: B:77:0x01e2, code:
            rotate();
     */
    /* JADX WARNING: Missing block: B:79:0x01ea, code:
            if ((r6 instanceof java.lang.SecurityException) == false) goto L_0x01ef;
     */
    /* JADX WARNING: Missing block: B:81:0x01ee, code:
            throw ((java.lang.SecurityException) r6);
     */
    /* JADX WARNING: Missing block: B:83:0x020a, code:
            throw new java.io.IOException("Exception: " + r6);
     */
    /* JADX WARNING: Missing block: B:84:0x020b, code:
            setErrorManager(new java.util.logging.ErrorManager());
     */
    /* JADX WARNING: Missing block: B:85:0x0217, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void openFiles() throws IOException {
        int retries;
        LogManager.getLogManager().checkPermission();
        if (this.count < 1) {
            throw new IllegalArgumentException("file count = " + this.count);
        }
        if (this.limit < 0) {
            this.limit = 0;
        }
        InitializationErrorManager em = new InitializationErrorManager(null);
        setErrorManager(em);
        int unique = -1;
        while (true) {
            unique++;
            if (unique > MAX_LOCKS) {
                throw new IOException("Couldn't get lock for " + this.pattern);
            }
            this.lockFileName = generate(this.pattern, 0, unique).toString() + ".lck";
            synchronized (locks) {
                if (!locks.contains(this.lockFileName)) {
                    Path lockFilePath = Paths.get(this.lockFileName, new String[0]);
                    FileChannel channel = null;
                    boolean fileCreated = false;
                    int retries2 = -1;
                    while (channel == null) {
                        retries = retries2 + 1;
                        if (retries2 >= 1) {
                            break;
                        }
                        try {
                            channel = FileChannel.open(lockFilePath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                            fileCreated = true;
                        } catch (FileAlreadyExistsException e) {
                            if (!Files.isRegularFile(lockFilePath, LinkOption.NOFOLLOW_LINKS) || !isParentWritable(lockFilePath)) {
                                break;
                            }
                            try {
                                channel = FileChannel.open(lockFilePath, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
                            } catch (NoSuchFileException e2) {
                                retries2 = retries;
                            } catch (IOException e3) {
                            }
                        }
                        retries2 = retries;
                    }
                    retries = retries2;
                    if (channel != null) {
                        boolean available;
                        this.lockFileChannel = channel;
                        try {
                            if (this.lockFileChannel.tryLock() != null) {
                                available = true;
                            } else {
                                available = false;
                            }
                        } catch (IOException e4) {
                            available = fileCreated;
                        } catch (OverlappingFileLockException e5) {
                            available = false;
                        }
                        if (available) {
                            break;
                        }
                        this.lockFileChannel.close();
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
            char ch2 = 0;
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
                    } else if (ch2 == 'u') {
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
        if (this.count > 1 && (sawg ^ 1) != 0) {
            word = word + "." + generation;
        }
        if (unique > 0 && (sawu ^ 1) != 0) {
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
        return;
    }

    /* JADX WARNING: Missing block: B:13:0x0024, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                this.lockFileChannel.close();
            } catch (Exception e) {
            }
            synchronized (locks) {
                locks.remove(this.lockFileName);
            }
            new File(this.lockFileName).delete();
            this.lockFileName = null;
            this.lockFileChannel = null;
        }
    }
}
