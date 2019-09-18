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

        private InitializationErrorManager() {
        }

        public void error(String msg, Exception ex, int code) {
            this.lastException = ex;
        }
    }

    private class MeteredStream extends OutputStream {
        final OutputStream out;
        int written;

        MeteredStream(OutputStream out2, int written2) {
            this.out = out2;
            this.written = written2;
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

    private void open(File fname, boolean append2) throws IOException {
        int len = 0;
        if (append2) {
            len = (int) fname.length();
        }
        this.meter = new MeteredStream(new BufferedOutputStream(new FileOutputStream(fname.toString(), append2)), len);
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

    public FileHandler(String pattern2) throws IOException, SecurityException {
        if (pattern2.length() >= 1) {
            checkPermission();
            configure();
            this.pattern = pattern2;
            this.limit = 0;
            this.count = 1;
            openFiles();
            return;
        }
        throw new IllegalArgumentException();
    }

    public FileHandler(String pattern2, boolean append2) throws IOException, SecurityException {
        if (pattern2.length() >= 1) {
            checkPermission();
            configure();
            this.pattern = pattern2;
            this.limit = 0;
            this.count = 1;
            this.append = append2;
            openFiles();
            return;
        }
        throw new IllegalArgumentException();
    }

    public FileHandler(String pattern2, int limit2, int count2) throws IOException, SecurityException {
        if (limit2 < 0 || count2 < 1 || pattern2.length() < 1) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern2;
        this.limit = limit2;
        this.count = count2;
        openFiles();
    }

    public FileHandler(String pattern2, int limit2, int count2, boolean append2) throws IOException, SecurityException {
        if (limit2 < 0 || count2 < 1 || pattern2.length() < 1) {
            throw new IllegalArgumentException();
        }
        checkPermission();
        configure();
        this.pattern = pattern2;
        this.limit = limit2;
        this.count = count2;
        this.append = append2;
        openFiles();
    }

    private boolean isParentWritable(Path path) {
        Path parent = path.getParent();
        if (parent == null) {
            parent = path.toAbsolutePath().getParent();
        }
        return parent != null && Files.isWritable(parent);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:53:?, code lost:
        locks.add(r14.lockFileName);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00c4, code lost:
        r14.files = new java.io.File[r14.count];
        r5 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00cd, code lost:
        if (r5 >= r14.count) goto L_0x00dc;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00cf, code lost:
        r14.files[r5] = generate(r14.pattern, r5, r4);
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00de, code lost:
        if (r14.append == 0) goto L_0x00e8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00e0, code lost:
        open(r14.files[0], true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00e8, code lost:
        rotate();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00eb, code lost:
        r2 = r1.lastException;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00ed, code lost:
        if (r2 == null) goto L_0x0116;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00f1, code lost:
        if ((r2 instanceof java.io.IOException) != false) goto L_0x0112;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00f5, code lost:
        if ((r2 instanceof java.lang.SecurityException) == false) goto L_0x00fb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00fa, code lost:
        throw ((java.lang.SecurityException) r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0111, code lost:
        throw new java.io.IOException("Exception: " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0115, code lost:
        throw ((java.io.IOException) r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0116, code lost:
        setErrorManager(new java.util.logging.ErrorManager());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x011e, code lost:
        return;
     */
    private void openFiles() throws IOException {
        boolean available;
        int retries;
        LogManager.getLogManager().checkPermission();
        if (this.count >= 1) {
            if (this.limit < 0) {
                this.limit = 0;
            }
            InitializationErrorManager em = new InitializationErrorManager();
            setErrorManager(em);
            int unique = -1;
            while (true) {
                unique++;
                if (unique <= MAX_LOCKS) {
                    this.lockFileName = generate(this.pattern, 0, unique).toString() + ".lck";
                    synchronized (locks) {
                        if (!locks.contains(this.lockFileName)) {
                            Path lockFilePath = Paths.get(this.lockFileName, new String[0]);
                            int retries2 = -1;
                            FileChannel channel = null;
                            boolean fileCreated = false;
                            while (true) {
                                if (channel != null) {
                                    break;
                                }
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
                                    } catch (IOException e3) {
                                    }
                                }
                                retries2 = retries;
                            }
                            int i = retries;
                            if (channel != null) {
                                this.lockFileChannel = channel;
                                try {
                                    available = this.lockFileChannel.tryLock() != null;
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
                } else {
                    throw new IOException("Couldn't get lock for " + this.pattern);
                }
            }
        } else {
            throw new IllegalArgumentException("file count = " + this.count);
        }
    }

    private File generate(String pattern2, int generation, int unique) throws IOException {
        File file;
        File file2 = null;
        String word = "";
        int ix = 0;
        boolean sawg = false;
        boolean sawu = false;
        while (ix < pattern2.length()) {
            char ch = pattern2.charAt(ix);
            ix++;
            char ch2 = 0;
            if (ix < pattern2.length()) {
                ch2 = Character.toLowerCase(pattern2.charAt(ix));
            }
            if (ch == '/') {
                if (file2 == null) {
                    file = new File(word);
                } else {
                    file = new File(file2, word);
                }
                file2 = file;
                word = "";
            } else {
                if (ch == '%') {
                    if (ch2 == 't') {
                        String tmpDir = System.getProperty("java.io.tmpdir");
                        if (tmpDir == null) {
                            tmpDir = System.getProperty("user.home");
                        }
                        file2 = new File(tmpDir);
                        ix++;
                        word = "";
                    } else if (ch2 == 'h') {
                        file2 = new File(System.getProperty("user.home"));
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
        if (this.count > 1 && !sawg) {
            word = word + "." + generation;
        }
        if (unique > 0 && !sawu) {
            word = word + "." + unique;
        }
        if (word.length() <= 0) {
            return file2;
        }
        if (file2 == null) {
            return new File(word);
        }
        return new File(file2, word);
    }

    /* access modifiers changed from: private */
    public synchronized void rotate() {
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

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0024, code lost:
        return;
     */
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
