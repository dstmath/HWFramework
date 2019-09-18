package java.util.prefs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import sun.util.locale.BaseLocale;
import sun.util.logging.PlatformLogger;

public class FileSystemPreferences extends AbstractPreferences {
    private static final int EACCES = 13;
    private static final int EAGAIN = 11;
    /* access modifiers changed from: private */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final int ERROR_CODE = 1;
    private static int INIT_SLEEP_TIME = 50;
    private static final int LOCK_HANDLE = 0;
    private static int MAX_ATTEMPTS = 5;
    private static final int USER_READ_WRITE = 384;
    private static final int USER_RWX = 448;
    private static final int USER_RWX_ALL_RX = 493;
    private static final int USER_RW_ALL_READ = 420;
    /* access modifiers changed from: private */
    public static boolean isSystemRootModified = false;
    /* access modifiers changed from: private */
    public static boolean isSystemRootWritable;
    /* access modifiers changed from: private */
    public static boolean isUserRootModified = false;
    /* access modifiers changed from: private */
    public static boolean isUserRootWritable;
    static File systemLockFile;
    static Preferences systemRoot;
    /* access modifiers changed from: private */
    public static File systemRootDir;
    private static int systemRootLockHandle = 0;
    /* access modifiers changed from: private */
    public static File systemRootModFile;
    /* access modifiers changed from: private */
    public static long systemRootModTime;
    static File userLockFile;
    static Preferences userRoot = null;
    /* access modifiers changed from: private */
    public static File userRootDir;
    private static int userRootLockHandle = 0;
    /* access modifiers changed from: private */
    public static File userRootModFile;
    /* access modifiers changed from: private */
    public static long userRootModTime;
    final List<Change> changeLog = new ArrayList();
    /* access modifiers changed from: private */
    public final File dir;
    private final boolean isUserNode;
    private long lastSyncTime = 0;
    NodeCreate nodeCreate = null;
    /* access modifiers changed from: private */
    public Map<String, String> prefsCache = null;
    /* access modifiers changed from: private */
    public final File prefsFile;
    /* access modifiers changed from: private */
    public final File tmpFile;

    private abstract class Change {
        /* access modifiers changed from: package-private */
        public abstract void replay();

        private Change() {
        }
    }

    private class NodeCreate extends Change {
        private NodeCreate() {
            super();
        }

        /* access modifiers changed from: package-private */
        public void replay() {
        }
    }

    private class Put extends Change {
        String key;
        String value;

        Put(String key2, String value2) {
            super();
            this.key = key2;
            this.value = value2;
        }

        /* access modifiers changed from: package-private */
        public void replay() {
            FileSystemPreferences.this.prefsCache.put(this.key, this.value);
        }
    }

    private class Remove extends Change {
        String key;

        Remove(String key2) {
            super();
            this.key = key2;
        }

        /* access modifiers changed from: package-private */
        public void replay() {
            FileSystemPreferences.this.prefsCache.remove(this.key);
        }
    }

    /* access modifiers changed from: private */
    public static native int chmod(String str, int i);

    private static native int[] lockFile0(String str, int i, boolean z);

    private static native int unlockFile0(int i);

    /* access modifiers changed from: private */
    public static PlatformLogger getLogger() {
        return PlatformLogger.getLogger("java.util.prefs");
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                FileSystemPreferences.syncWorld();
            }
        });
    }

    static synchronized Preferences getUserRoot() {
        Preferences preferences;
        synchronized (FileSystemPreferences.class) {
            if (userRoot == null) {
                setupUserRoot();
                userRoot = new FileSystemPreferences(true);
            }
            preferences = userRoot;
        }
        return preferences;
    }

    private static void setupUserRoot() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                File unused = FileSystemPreferences.userRootDir = new File(System.getProperty("java.util.prefs.userRoot", System.getProperty("user.home")), ".java/.userPrefs");
                if (!FileSystemPreferences.userRootDir.exists()) {
                    if (FileSystemPreferences.userRootDir.mkdirs()) {
                        try {
                            int unused2 = FileSystemPreferences.chmod(FileSystemPreferences.userRootDir.getCanonicalPath(), FileSystemPreferences.USER_RWX);
                        } catch (IOException e) {
                            FileSystemPreferences.getLogger().warning("Could not change permissions on userRoot directory. ");
                        }
                        FileSystemPreferences.getLogger().info("Created user preferences directory.");
                    } else {
                        FileSystemPreferences.getLogger().warning("Couldn't create user preferences directory. User preferences are unusable.");
                    }
                }
                boolean unused3 = FileSystemPreferences.isUserRootWritable = FileSystemPreferences.userRootDir.canWrite();
                String USER_NAME = System.getProperty("user.name");
                File access$000 = FileSystemPreferences.userRootDir;
                FileSystemPreferences.userLockFile = new File(access$000, ".user.lock." + USER_NAME);
                File access$0002 = FileSystemPreferences.userRootDir;
                File unused4 = FileSystemPreferences.userRootModFile = new File(access$0002, ".userRootModFile." + USER_NAME);
                if (!FileSystemPreferences.userRootModFile.exists()) {
                    try {
                        FileSystemPreferences.userRootModFile.createNewFile();
                        int result = FileSystemPreferences.chmod(FileSystemPreferences.userRootModFile.getCanonicalPath(), FileSystemPreferences.USER_READ_WRITE);
                        if (result != 0) {
                            PlatformLogger access$200 = FileSystemPreferences.getLogger();
                            access$200.warning("Problem creating userRoot mod file. Chmod failed on " + FileSystemPreferences.userRootModFile.getCanonicalPath() + " Unix error code " + result);
                        }
                    } catch (IOException e2) {
                        FileSystemPreferences.getLogger().warning(e2.toString());
                    }
                }
                long unused5 = FileSystemPreferences.userRootModTime = FileSystemPreferences.userRootModFile.lastModified();
                return null;
            }
        });
    }

    static synchronized Preferences getSystemRoot() {
        Preferences preferences;
        synchronized (FileSystemPreferences.class) {
            if (systemRoot == null) {
                setupSystemRoot();
                systemRoot = new FileSystemPreferences(false);
            }
            preferences = systemRoot;
        }
        return preferences;
    }

    private static void setupSystemRoot() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                File unused = FileSystemPreferences.systemRootDir = new File(System.getProperty("java.util.prefs.systemRoot", "/etc/.java"), ".systemPrefs");
                if (!FileSystemPreferences.systemRootDir.exists()) {
                    File unused2 = FileSystemPreferences.systemRootDir = new File(System.getProperty("java.home"), ".systemPrefs");
                    if (!FileSystemPreferences.systemRootDir.exists()) {
                        if (FileSystemPreferences.systemRootDir.mkdirs()) {
                            FileSystemPreferences.getLogger().info("Created system preferences directory in java.home.");
                            try {
                                int unused3 = FileSystemPreferences.chmod(FileSystemPreferences.systemRootDir.getCanonicalPath(), FileSystemPreferences.USER_RWX_ALL_RX);
                            } catch (IOException e) {
                            }
                        } else {
                            FileSystemPreferences.getLogger().warning("Could not create system preferences directory. System preferences are unusable.");
                        }
                    }
                }
                boolean unused4 = FileSystemPreferences.isSystemRootWritable = FileSystemPreferences.systemRootDir.canWrite();
                FileSystemPreferences.systemLockFile = new File(FileSystemPreferences.systemRootDir, ".system.lock");
                File unused5 = FileSystemPreferences.systemRootModFile = new File(FileSystemPreferences.systemRootDir, ".systemRootModFile");
                if (!FileSystemPreferences.systemRootModFile.exists() && FileSystemPreferences.isSystemRootWritable) {
                    try {
                        FileSystemPreferences.systemRootModFile.createNewFile();
                        int result = FileSystemPreferences.chmod(FileSystemPreferences.systemRootModFile.getCanonicalPath(), FileSystemPreferences.USER_RW_ALL_READ);
                        if (result != 0) {
                            PlatformLogger access$200 = FileSystemPreferences.getLogger();
                            access$200.warning("Chmod failed on " + FileSystemPreferences.systemRootModFile.getCanonicalPath() + " Unix error code " + result);
                        }
                    } catch (IOException e2) {
                        FileSystemPreferences.getLogger().warning(e2.toString());
                    }
                }
                long unused6 = FileSystemPreferences.systemRootModTime = FileSystemPreferences.systemRootModFile.lastModified();
                return null;
            }
        });
    }

    private void replayChanges() {
        int n = this.changeLog.size();
        for (int i = 0; i < n; i++) {
            this.changeLog.get(i).replay();
        }
    }

    /* access modifiers changed from: private */
    public static void syncWorld() {
        Preferences userRt;
        Preferences systemRt;
        synchronized (FileSystemPreferences.class) {
            userRt = userRoot;
            systemRt = systemRoot;
        }
        if (userRt != null) {
            try {
                userRt.flush();
            } catch (BackingStoreException e) {
                PlatformLogger logger = getLogger();
                logger.warning("Couldn't flush user prefs: " + e);
            }
        }
        if (systemRt != null) {
            try {
                systemRt.flush();
            } catch (BackingStoreException e2) {
                PlatformLogger logger2 = getLogger();
                logger2.warning("Couldn't flush system prefs: " + e2);
            }
        }
    }

    private FileSystemPreferences(boolean user) {
        super(null, "");
        this.isUserNode = user;
        this.dir = user ? userRootDir : systemRootDir;
        this.prefsFile = new File(this.dir, "prefs.xml");
        this.tmpFile = new File(this.dir, "prefs.tmp");
    }

    public FileSystemPreferences(String path, File lockFile, boolean isUserNode2) {
        super(null, "");
        this.isUserNode = isUserNode2;
        this.dir = new File(path);
        this.prefsFile = new File(this.dir, "prefs.xml");
        this.tmpFile = new File(this.dir, "prefs.tmp");
        this.newNode = !this.dir.exists();
        if (this.newNode) {
            this.prefsCache = new TreeMap();
            this.nodeCreate = new NodeCreate();
            this.changeLog.add(this.nodeCreate);
        }
        if (isUserNode2) {
            userLockFile = lockFile;
            File parentFile = lockFile.getParentFile();
            userRootModFile = new File(parentFile, lockFile.getName() + ".rootmod");
            return;
        }
        systemLockFile = lockFile;
        File parentFile2 = lockFile.getParentFile();
        systemRootModFile = new File(parentFile2, lockFile.getName() + ".rootmod");
    }

    private FileSystemPreferences(FileSystemPreferences parent, String name) {
        super(parent, name);
        this.isUserNode = parent.isUserNode;
        this.dir = new File(parent.dir, dirName(name));
        this.prefsFile = new File(this.dir, "prefs.xml");
        this.tmpFile = new File(this.dir, "prefs.tmp");
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                FileSystemPreferences.this.newNode = !FileSystemPreferences.this.dir.exists();
                return null;
            }
        });
        if (this.newNode) {
            this.prefsCache = new TreeMap();
            this.nodeCreate = new NodeCreate();
            this.changeLog.add(this.nodeCreate);
        }
    }

    public boolean isUserNode() {
        return this.isUserNode;
    }

    /* access modifiers changed from: protected */
    public void putSpi(String key, String value) {
        initCacheIfNecessary();
        this.changeLog.add(new Put(key, value));
        this.prefsCache.put(key, value);
    }

    /* access modifiers changed from: protected */
    public String getSpi(String key) {
        initCacheIfNecessary();
        return this.prefsCache.get(key);
    }

    /* access modifiers changed from: protected */
    public void removeSpi(String key) {
        initCacheIfNecessary();
        this.changeLog.add(new Remove(key));
        this.prefsCache.remove(key);
    }

    private void initCacheIfNecessary() {
        if (this.prefsCache == null) {
            try {
                loadCache();
            } catch (Exception e) {
                this.prefsCache = new TreeMap();
            }
        }
    }

    private void loadCache() throws BackingStoreException {
        FileInputStream fis;
        Map<String, String> m = new TreeMap<>();
        long newLastSyncTime = 0;
        try {
            newLastSyncTime = this.prefsFile.lastModified();
            fis = new FileInputStream(this.prefsFile);
            XmlSupport.importMap(fis, m);
            $closeResource(null, fis);
        } catch (Exception e) {
            if (e instanceof InvalidPreferencesFormatException) {
                PlatformLogger logger = getLogger();
                logger.warning("Invalid preferences format in " + this.prefsFile.getPath());
                this.prefsFile.renameTo(new File(this.prefsFile.getParentFile(), "IncorrectFormatPrefs.xml"));
                m = new TreeMap<>();
            } else if (e instanceof FileNotFoundException) {
                PlatformLogger logger2 = getLogger();
                logger2.warning("Prefs file removed in background " + this.prefsFile.getPath());
            } else {
                PlatformLogger logger3 = getLogger();
                logger3.warning("Exception while reading cache: " + e.getMessage());
                throw new BackingStoreException((Throwable) e);
            }
        } catch (Throwable th) {
            $closeResource(r4, fis);
            throw th;
        }
        this.prefsCache = m;
        this.lastSyncTime = newLastSyncTime;
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private void writeBackCache() throws BackingStoreException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws BackingStoreException {
                    FileOutputStream fos;
                    try {
                        if (!FileSystemPreferences.this.dir.exists()) {
                            if (!FileSystemPreferences.this.dir.mkdirs()) {
                                throw new BackingStoreException(FileSystemPreferences.this.dir + " create failed.");
                            }
                        }
                        fos = new FileOutputStream(FileSystemPreferences.this.tmpFile);
                        XmlSupport.exportMap(fos, FileSystemPreferences.this.prefsCache);
                        fos.close();
                        if (FileSystemPreferences.this.tmpFile.renameTo(FileSystemPreferences.this.prefsFile)) {
                            return null;
                        }
                        throw new BackingStoreException("Can't rename " + FileSystemPreferences.this.tmpFile + " to " + FileSystemPreferences.this.prefsFile);
                    } catch (Exception e) {
                        if (e instanceof BackingStoreException) {
                            throw ((BackingStoreException) e);
                        }
                        throw new BackingStoreException((Throwable) e);
                    } catch (Throwable th) {
                        r1.addSuppressed(th);
                    }
                    throw th;
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((BackingStoreException) e.getException());
        }
    }

    /* access modifiers changed from: protected */
    public String[] keysSpi() {
        initCacheIfNecessary();
        return (String[]) this.prefsCache.keySet().toArray(new String[this.prefsCache.size()]);
    }

    /* access modifiers changed from: protected */
    public String[] childrenNamesSpi() {
        return (String[]) AccessController.doPrivileged(new PrivilegedAction<String[]>() {
            public String[] run() {
                List<String> result = new ArrayList<>();
                File[] dirContents = FileSystemPreferences.this.dir.listFiles();
                if (dirContents != null) {
                    for (int i = 0; i < dirContents.length; i++) {
                        if (dirContents[i].isDirectory()) {
                            result.add(FileSystemPreferences.nodeName(dirContents[i].getName()));
                        }
                    }
                }
                return (String[]) result.toArray(FileSystemPreferences.EMPTY_STRING_ARRAY);
            }
        });
    }

    /* access modifiers changed from: protected */
    public AbstractPreferences childSpi(String name) {
        return new FileSystemPreferences(this, name);
    }

    public void removeNode() throws BackingStoreException {
        synchronized ((isUserNode() ? userLockFile : systemLockFile)) {
            if (lockFile(false)) {
                try {
                    super.removeNode();
                } finally {
                    unlockFile();
                }
            } else {
                throw new BackingStoreException("Couldn't get file lock.");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void removeNodeSpi() throws BackingStoreException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws BackingStoreException {
                    if (FileSystemPreferences.this.changeLog.contains(FileSystemPreferences.this.nodeCreate)) {
                        FileSystemPreferences.this.changeLog.remove((Object) FileSystemPreferences.this.nodeCreate);
                        FileSystemPreferences.this.nodeCreate = null;
                        return null;
                    } else if (!FileSystemPreferences.this.dir.exists()) {
                        return null;
                    } else {
                        FileSystemPreferences.this.prefsFile.delete();
                        FileSystemPreferences.this.tmpFile.delete();
                        File[] junk = FileSystemPreferences.this.dir.listFiles();
                        if (junk.length != 0) {
                            FileSystemPreferences.getLogger().warning("Found extraneous files when removing node: " + Arrays.asList(junk));
                            for (File delete : junk) {
                                delete.delete();
                            }
                        }
                        if (FileSystemPreferences.this.dir.delete()) {
                            return null;
                        }
                        throw new BackingStoreException("Couldn't delete dir: " + FileSystemPreferences.this.dir);
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((BackingStoreException) e.getException());
        }
    }

    public synchronized void sync() throws BackingStoreException {
        boolean shared;
        if (isUserNode()) {
            shared = false;
        } else {
            shared = !isSystemRootWritable;
        }
        synchronized ((isUserNode() ? userLockFile : systemLockFile)) {
            if (lockFile(shared)) {
                final Long newModTime = (Long) AccessController.doPrivileged(new PrivilegedAction<Long>() {
                    public Long run() {
                        long nmt;
                        boolean z = false;
                        if (FileSystemPreferences.this.isUserNode()) {
                            nmt = FileSystemPreferences.userRootModFile.lastModified();
                            if (FileSystemPreferences.userRootModTime == nmt) {
                                z = true;
                            }
                            boolean unused = FileSystemPreferences.isUserRootModified = z;
                        } else {
                            nmt = FileSystemPreferences.systemRootModFile.lastModified();
                            if (FileSystemPreferences.systemRootModTime == nmt) {
                                z = true;
                            }
                            boolean unused2 = FileSystemPreferences.isSystemRootModified = z;
                        }
                        return new Long(nmt);
                    }
                });
                try {
                    super.sync();
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            if (FileSystemPreferences.this.isUserNode()) {
                                long unused = FileSystemPreferences.userRootModTime = newModTime.longValue() + 1000;
                                FileSystemPreferences.userRootModFile.setLastModified(FileSystemPreferences.userRootModTime);
                            } else {
                                long unused2 = FileSystemPreferences.systemRootModTime = newModTime.longValue() + 1000;
                                FileSystemPreferences.systemRootModFile.setLastModified(FileSystemPreferences.systemRootModTime);
                            }
                            return null;
                        }
                    });
                } finally {
                    unlockFile();
                }
            } else {
                throw new BackingStoreException("Couldn't get file lock.");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void syncSpi() throws BackingStoreException {
        syncSpiPrivileged();
    }

    private void syncSpiPrivileged() throws BackingStoreException {
        if (isRemoved()) {
            throw new IllegalStateException("Node has been removed");
        } else if (this.prefsCache != null) {
            if (!isUserNode() ? isSystemRootModified : isUserRootModified) {
                long lastModifiedTime = this.prefsFile.lastModified();
                if (lastModifiedTime != this.lastSyncTime) {
                    loadCache();
                    replayChanges();
                    this.lastSyncTime = lastModifiedTime;
                }
            } else if (this.lastSyncTime != 0 && !this.dir.exists()) {
                this.prefsCache = new TreeMap();
                replayChanges();
            }
            if (!this.changeLog.isEmpty()) {
                writeBackCache();
                long lastModifiedTime2 = this.prefsFile.lastModified();
                if (this.lastSyncTime <= lastModifiedTime2) {
                    this.lastSyncTime = 1000 + lastModifiedTime2;
                    this.prefsFile.setLastModified(this.lastSyncTime);
                }
                this.changeLog.clear();
            }
        }
    }

    public void flush() throws BackingStoreException {
        if (!isRemoved()) {
            sync();
        }
    }

    /* access modifiers changed from: protected */
    public void flushSpi() throws BackingStoreException {
    }

    private static boolean isDirChar(char ch) {
        return (ch <= 31 || ch >= 127 || ch == '/' || ch == '.' || ch == '_') ? false : true;
    }

    private static String dirName(String nodeName) {
        int n = nodeName.length();
        for (int i = 0; i < n; i++) {
            if (!isDirChar(nodeName.charAt(i))) {
                return BaseLocale.SEP + Base64.byteArrayToAltBase64(byteArray(nodeName));
            }
        }
        return nodeName;
    }

    private static byte[] byteArray(String s) {
        int len = s.length();
        byte[] result = new byte[(2 * len)];
        int j = 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            int j2 = j + 1;
            result[j] = (byte) (c >> 8);
            j = j2 + 1;
            result[j2] = (byte) c;
        }
        return result;
    }

    /* access modifiers changed from: private */
    public static String nodeName(String dirName) {
        int i = 0;
        if (dirName.charAt(0) != '_') {
            return dirName;
        }
        byte[] a = Base64.altBase64ToByteArray(dirName.substring(1));
        StringBuffer result = new StringBuffer(a.length / 2);
        while (i < a.length) {
            int i2 = i + 1;
            result.append((char) (((a[i] & 255) << 8) | (a[i2] & 255)));
            i = i2 + 1;
        }
        return result.toString();
    }

    private boolean lockFile(boolean shared) throws SecurityException {
        boolean usernode = isUserNode();
        File lockFile = usernode ? userLockFile : systemLockFile;
        long sleepTime = (long) INIT_SLEEP_TIME;
        int errorCode = 0;
        int i = 0;
        while (i < MAX_ATTEMPTS) {
            try {
                int[] result = lockFile0(lockFile.getCanonicalPath(), usernode ? USER_READ_WRITE : USER_RW_ALL_READ, shared);
                errorCode = result[1];
                if (result[0] != 0) {
                    if (usernode) {
                        userRootLockHandle = result[0];
                    } else {
                        systemRootLockHandle = result[0];
                    }
                    return true;
                }
            } catch (IOException e) {
            }
            try {
                Thread.sleep(sleepTime);
                sleepTime *= 2;
                i++;
            } catch (InterruptedException e2) {
                checkLockFile0ErrorCode(errorCode);
                return false;
            }
        }
        checkLockFile0ErrorCode(errorCode);
        return false;
    }

    private void checkLockFile0ErrorCode(int errorCode) throws SecurityException {
        if (errorCode == 13) {
            StringBuilder sb = new StringBuilder();
            sb.append("Could not lock ");
            sb.append(isUserNode() ? "User prefs." : "System prefs.");
            sb.append(" Lock file access denied.");
            throw new SecurityException(sb.toString());
        } else if (errorCode != 11) {
            PlatformLogger logger = getLogger();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Could not lock ");
            sb2.append(isUserNode() ? "User prefs. " : "System prefs.");
            sb2.append(" Unix error code ");
            sb2.append(errorCode);
            sb2.append(".");
            logger.warning(sb2.toString());
        }
    }

    private void unlockFile() {
        boolean usernode = isUserNode();
        if (usernode) {
            File file = userLockFile;
        } else {
            File file2 = systemLockFile;
        }
        int lockHandle = usernode ? userRootLockHandle : systemRootLockHandle;
        if (lockHandle == 0) {
            PlatformLogger logger = getLogger();
            StringBuilder sb = new StringBuilder();
            sb.append("Unlock: zero lockHandle for ");
            sb.append(usernode ? "user" : "system");
            sb.append(" preferences.)");
            logger.warning(sb.toString());
            return;
        }
        int result = unlockFile0(lockHandle);
        if (result != 0) {
            PlatformLogger logger2 = getLogger();
            StringBuilder sb2 = new StringBuilder();
            sb2.append("Could not drop file-lock on ");
            sb2.append(isUserNode() ? "user" : "system");
            sb2.append(" preferences. Unix error code ");
            sb2.append(result);
            sb2.append(".");
            logger2.warning(sb2.toString());
            if (result == 13) {
                StringBuilder sb3 = new StringBuilder();
                sb3.append("Could not unlock");
                sb3.append(isUserNode() ? "User prefs." : "System prefs.");
                sb3.append(" Lock file access denied.");
                throw new SecurityException(sb3.toString());
            }
        }
        if (isUserNode()) {
            userRootLockHandle = 0;
        } else {
            systemRootLockHandle = 0;
        }
    }
}
