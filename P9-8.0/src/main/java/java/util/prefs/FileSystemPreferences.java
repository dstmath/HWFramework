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
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final int ERROR_CODE = 1;
    private static int INIT_SLEEP_TIME = 50;
    private static final int LOCK_HANDLE = 0;
    private static int MAX_ATTEMPTS = 5;
    private static final int USER_READ_WRITE = 384;
    private static final int USER_RWX = 448;
    private static final int USER_RWX_ALL_RX = 493;
    private static final int USER_RW_ALL_READ = 420;
    private static boolean isSystemRootModified = false;
    private static boolean isSystemRootWritable;
    private static boolean isUserRootModified = false;
    private static boolean isUserRootWritable;
    static File systemLockFile;
    static Preferences systemRoot;
    private static File systemRootDir;
    private static int systemRootLockHandle = 0;
    private static File systemRootModFile;
    private static long systemRootModTime;
    static File userLockFile;
    static Preferences userRoot = null;
    private static File userRootDir;
    private static int userRootLockHandle = 0;
    private static File userRootModFile;
    private static long userRootModTime;
    final List<Change> changeLog = new ArrayList();
    private final File dir;
    private final boolean isUserNode;
    private long lastSyncTime = 0;
    NodeCreate nodeCreate = null;
    private Map<String, String> prefsCache = null;
    private final File prefsFile;
    private final File tmpFile;

    private abstract class Change {
        /* synthetic */ Change(FileSystemPreferences this$0, Change -this1) {
            this();
        }

        abstract void replay();

        private Change() {
        }
    }

    private class NodeCreate extends Change {
        /* synthetic */ NodeCreate(FileSystemPreferences this$0, NodeCreate -this1) {
            this();
        }

        private NodeCreate() {
            super(FileSystemPreferences.this, null);
        }

        void replay() {
        }
    }

    private class Put extends Change {
        String key;
        String value;

        Put(String key, String value) {
            super(FileSystemPreferences.this, null);
            this.key = key;
            this.value = value;
        }

        void replay() {
            FileSystemPreferences.this.prefsCache.put(this.key, this.value);
        }
    }

    private class Remove extends Change {
        String key;

        Remove(String key) {
            super(FileSystemPreferences.this, null);
            this.key = key;
        }

        void replay() {
            FileSystemPreferences.this.prefsCache.remove(this.key);
        }
    }

    private static native int chmod(String str, int i);

    private static native int[] lockFile0(String str, int i, boolean z);

    private static native int unlockFile0(int i);

    private static PlatformLogger getLogger() {
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
                FileSystemPreferences.userRootDir = new File(System.getProperty("java.util.prefs.userRoot", System.getProperty("user.home")), ".java/.userPrefs");
                if (!FileSystemPreferences.userRootDir.exists()) {
                    if (FileSystemPreferences.userRootDir.mkdirs()) {
                        try {
                            FileSystemPreferences.chmod(FileSystemPreferences.userRootDir.getCanonicalPath(), FileSystemPreferences.USER_RWX);
                        } catch (IOException e) {
                            FileSystemPreferences.getLogger().warning("Could not change permissions on userRoot directory. ");
                        }
                        FileSystemPreferences.getLogger().info("Created user preferences directory.");
                    } else {
                        FileSystemPreferences.getLogger().warning("Couldn't create user preferences directory. User preferences are unusable.");
                    }
                }
                FileSystemPreferences.isUserRootWritable = FileSystemPreferences.userRootDir.canWrite();
                String USER_NAME = System.getProperty("user.name");
                FileSystemPreferences.userLockFile = new File(FileSystemPreferences.userRootDir, ".user.lock." + USER_NAME);
                FileSystemPreferences.userRootModFile = new File(FileSystemPreferences.userRootDir, ".userRootModFile." + USER_NAME);
                if (!FileSystemPreferences.userRootModFile.exists()) {
                    try {
                        FileSystemPreferences.userRootModFile.createNewFile();
                        int result = FileSystemPreferences.chmod(FileSystemPreferences.userRootModFile.getCanonicalPath(), FileSystemPreferences.USER_READ_WRITE);
                        if (result != 0) {
                            FileSystemPreferences.getLogger().warning("Problem creating userRoot mod file. Chmod failed on " + FileSystemPreferences.userRootModFile.getCanonicalPath() + " Unix error code " + result);
                        }
                    } catch (IOException e2) {
                        FileSystemPreferences.getLogger().warning(e2.toString());
                    }
                }
                FileSystemPreferences.userRootModTime = FileSystemPreferences.userRootModFile.lastModified();
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
                FileSystemPreferences.systemRootDir = new File(System.getProperty("java.util.prefs.systemRoot", "/etc/.java"), ".systemPrefs");
                if (!FileSystemPreferences.systemRootDir.exists()) {
                    FileSystemPreferences.systemRootDir = new File(System.getProperty("java.home"), ".systemPrefs");
                    if (!FileSystemPreferences.systemRootDir.exists()) {
                        if (FileSystemPreferences.systemRootDir.mkdirs()) {
                            FileSystemPreferences.getLogger().info("Created system preferences directory in java.home.");
                            try {
                                FileSystemPreferences.chmod(FileSystemPreferences.systemRootDir.getCanonicalPath(), FileSystemPreferences.USER_RWX_ALL_RX);
                            } catch (IOException e) {
                            }
                        } else {
                            FileSystemPreferences.getLogger().warning("Could not create system preferences directory. System preferences are unusable.");
                        }
                    }
                }
                FileSystemPreferences.isSystemRootWritable = FileSystemPreferences.systemRootDir.canWrite();
                FileSystemPreferences.systemLockFile = new File(FileSystemPreferences.systemRootDir, ".system.lock");
                FileSystemPreferences.systemRootModFile = new File(FileSystemPreferences.systemRootDir, ".systemRootModFile");
                if (!FileSystemPreferences.systemRootModFile.exists() && FileSystemPreferences.isSystemRootWritable) {
                    try {
                        FileSystemPreferences.systemRootModFile.createNewFile();
                        int result = FileSystemPreferences.chmod(FileSystemPreferences.systemRootModFile.getCanonicalPath(), FileSystemPreferences.USER_RW_ALL_READ);
                        if (result != 0) {
                            FileSystemPreferences.getLogger().warning("Chmod failed on " + FileSystemPreferences.systemRootModFile.getCanonicalPath() + " Unix error code " + result);
                        }
                    } catch (IOException e2) {
                        FileSystemPreferences.getLogger().warning(e2.toString());
                    }
                }
                FileSystemPreferences.systemRootModTime = FileSystemPreferences.systemRootModFile.lastModified();
                return null;
            }
        });
    }

    private void replayChanges() {
        int n = this.changeLog.size();
        for (int i = 0; i < n; i++) {
            ((Change) this.changeLog.get(i)).replay();
        }
    }

    private static void syncWorld() {
        Preferences userRt;
        Preferences systemRt;
        synchronized (FileSystemPreferences.class) {
            userRt = userRoot;
            systemRt = systemRoot;
        }
        if (userRt != null) {
            try {
                userRt.flush();
            } catch (Object e) {
                getLogger().warning("Couldn't flush user prefs: " + e);
            }
        }
        if (systemRt != null) {
            try {
                systemRt.flush();
            } catch (Object e2) {
                getLogger().warning("Couldn't flush system prefs: " + e2);
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

    public FileSystemPreferences(String path, File lockFile, boolean isUserNode) {
        super(null, "");
        this.isUserNode = isUserNode;
        this.dir = new File(path);
        this.prefsFile = new File(this.dir, "prefs.xml");
        this.tmpFile = new File(this.dir, "prefs.tmp");
        this.newNode = this.dir.exists() ^ 1;
        if (this.newNode) {
            this.prefsCache = new TreeMap();
            this.nodeCreate = new NodeCreate(this, null);
            this.changeLog.add(this.nodeCreate);
        }
        if (isUserNode) {
            userLockFile = lockFile;
            userRootModFile = new File(lockFile.getParentFile(), lockFile.getName() + ".rootmod");
            return;
        }
        systemLockFile = lockFile;
        systemRootModFile = new File(lockFile.getParentFile(), lockFile.getName() + ".rootmod");
    }

    private FileSystemPreferences(FileSystemPreferences parent, String name) {
        super(parent, name);
        this.isUserNode = parent.isUserNode;
        this.dir = new File(parent.dir, dirName(name));
        this.prefsFile = new File(this.dir, "prefs.xml");
        this.tmpFile = new File(this.dir, "prefs.tmp");
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                FileSystemPreferences.this.newNode = FileSystemPreferences.this.dir.exists() ^ 1;
                return null;
            }
        });
        if (this.newNode) {
            this.prefsCache = new TreeMap();
            this.nodeCreate = new NodeCreate(this, null);
            this.changeLog.add(this.nodeCreate);
        }
    }

    public boolean isUserNode() {
        return this.isUserNode;
    }

    protected void putSpi(String key, String value) {
        initCacheIfNecessary();
        this.changeLog.add(new Put(key, value));
        this.prefsCache.put(key, value);
    }

    protected String getSpi(String key) {
        initCacheIfNecessary();
        return (String) this.prefsCache.get(key);
    }

    protected void removeSpi(String key) {
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

    /* JADX WARNING: Removed duplicated region for block: B:27:0x006e A:{SYNTHETIC, Splitter: B:27:0x006e} */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x007f A:{Catch:{ Exception -> 0x0021 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0073 A:{SYNTHETIC, Splitter: B:30:0x0073} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadCache() throws BackingStoreException {
        Throwable th;
        Throwable th2 = null;
        Map<String, String> m = new TreeMap();
        long newLastSyncTime = 0;
        try {
            newLastSyncTime = this.prefsFile.lastModified();
            FileInputStream fis = null;
            try {
                FileInputStream fis2 = new FileInputStream(this.prefsFile);
                try {
                    XmlSupport.importMap(fis2, m);
                    if (fis2 != null) {
                        try {
                            fis2.close();
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                    }
                    if (th2 != null) {
                        throw th2;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    fis = fis2;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Throwable th5) {
                            if (th2 == null) {
                                th2 = th5;
                            } else if (th2 != th5) {
                                th2.addSuppressed(th5);
                            }
                        }
                    }
                    if (th2 == null) {
                        throw th2;
                    }
                    throw th;
                }
            } catch (Throwable th6) {
                th = th6;
                if (fis != null) {
                }
                if (th2 == null) {
                }
            }
        } catch (Throwable e) {
            if (e instanceof InvalidPreferencesFormatException) {
                getLogger().warning("Invalid preferences format in " + this.prefsFile.getPath());
                this.prefsFile.renameTo(new File(this.prefsFile.getParentFile(), "IncorrectFormatPrefs.xml"));
                m = new TreeMap();
            } else if (e instanceof FileNotFoundException) {
                getLogger().warning("Prefs file removed in background " + this.prefsFile.getPath());
            } else {
                getLogger().warning("Exception while reading cache: " + e.getMessage());
                throw new BackingStoreException(e);
            }
        }
        this.prefsCache = m;
        this.lastSyncTime = newLastSyncTime;
    }

    private void writeBackCache() throws BackingStoreException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                /* JADX WARNING: Removed duplicated region for block: B:32:0x006b A:{SYNTHETIC, Splitter: B:32:0x006b} */
                /* JADX WARNING: Removed duplicated region for block: B:42:0x007c A:{Catch:{ Exception -> 0x003b }} */
                /* JADX WARNING: Removed duplicated region for block: B:35:0x0070 A:{SYNTHETIC, Splitter: B:35:0x0070} */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public Void run() throws BackingStoreException {
                    Throwable th = null;
                    try {
                        if (FileSystemPreferences.this.dir.exists() || (FileSystemPreferences.this.dir.mkdirs() ^ 1) == 0) {
                            FileOutputStream fos = null;
                            Throwable th2;
                            try {
                                FileOutputStream fos2 = new FileOutputStream(FileSystemPreferences.this.tmpFile);
                                try {
                                    XmlSupport.exportMap(fos2, FileSystemPreferences.this.prefsCache);
                                    if (fos2 != null) {
                                        try {
                                            fos2.close();
                                        } catch (Throwable th3) {
                                            th2 = th3;
                                        }
                                    }
                                    th2 = null;
                                    if (th2 != null) {
                                        throw th2;
                                    } else if (FileSystemPreferences.this.tmpFile.renameTo(FileSystemPreferences.this.prefsFile)) {
                                        return null;
                                    } else {
                                        throw new BackingStoreException("Can't rename " + FileSystemPreferences.this.tmpFile + " to " + FileSystemPreferences.this.prefsFile);
                                    }
                                } catch (Throwable th4) {
                                    th2 = th4;
                                    fos = fos2;
                                    if (fos != null) {
                                        try {
                                            fos.close();
                                        } catch (Throwable th5) {
                                            if (th == null) {
                                                th = th5;
                                            } else if (th != th5) {
                                                th.addSuppressed(th5);
                                            }
                                        }
                                    }
                                    if (th == null) {
                                        throw th;
                                    }
                                    throw th2;
                                }
                            } catch (Throwable th6) {
                                th2 = th6;
                                if (fos != null) {
                                }
                                if (th == null) {
                                }
                            }
                        } else {
                            throw new BackingStoreException(FileSystemPreferences.this.dir + " create failed.");
                        }
                    } catch (Throwable e) {
                        if (e instanceof BackingStoreException) {
                            throw ((BackingStoreException) e);
                        }
                        throw new BackingStoreException(e);
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            throw ((BackingStoreException) e.getException());
        }
    }

    protected String[] keysSpi() {
        initCacheIfNecessary();
        return (String[]) this.prefsCache.keySet().toArray(new String[this.prefsCache.size()]);
    }

    protected String[] childrenNamesSpi() {
        return (String[]) AccessController.doPrivileged(new PrivilegedAction<String[]>() {
            public String[] run() {
                List<String> result = new ArrayList();
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

    protected AbstractPreferences childSpi(String name) {
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

    protected void removeNodeSpi() throws BackingStoreException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws BackingStoreException {
                    if (FileSystemPreferences.this.changeLog.contains(FileSystemPreferences.this.nodeCreate)) {
                        FileSystemPreferences.this.changeLog.remove(FileSystemPreferences.this.nodeCreate);
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
            shared = isSystemRootWritable ^ 1;
        }
        synchronized ((isUserNode() ? userLockFile : systemLockFile)) {
            if (lockFile(shared)) {
                final Long newModTime = (Long) AccessController.doPrivileged(new PrivilegedAction<Long>() {
                    public Long run() {
                        long nmt;
                        boolean z = true;
                        if (FileSystemPreferences.this.isUserNode()) {
                            nmt = FileSystemPreferences.userRootModFile.lastModified();
                            if (FileSystemPreferences.userRootModTime != nmt) {
                                z = false;
                            }
                            FileSystemPreferences.isUserRootModified = z;
                        } else {
                            nmt = FileSystemPreferences.systemRootModFile.lastModified();
                            if (FileSystemPreferences.systemRootModTime != nmt) {
                                z = false;
                            }
                            FileSystemPreferences.isSystemRootModified = z;
                        }
                        return new Long(nmt);
                    }
                });
                try {
                    super.sync();
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            if (FileSystemPreferences.this.isUserNode()) {
                                FileSystemPreferences.userRootModTime = newModTime.longValue() + 1000;
                                FileSystemPreferences.userRootModFile.setLastModified(FileSystemPreferences.userRootModTime);
                            } else {
                                FileSystemPreferences.systemRootModTime = newModTime.longValue() + 1000;
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

    protected void syncSpi() throws BackingStoreException {
        syncSpiPrivileged();
    }

    private void syncSpiPrivileged() throws BackingStoreException {
        if (isRemoved()) {
            throw new IllegalStateException("Node has been removed");
        } else if (this.prefsCache != null) {
            long lastModifiedTime;
            if (isUserNode() ? isUserRootModified : isSystemRootModified) {
                lastModifiedTime = this.prefsFile.lastModified();
                if (lastModifiedTime != this.lastSyncTime) {
                    loadCache();
                    replayChanges();
                    this.lastSyncTime = lastModifiedTime;
                }
            } else if (!(this.lastSyncTime == 0 || (this.dir.exists() ^ 1) == 0)) {
                this.prefsCache = new TreeMap();
                replayChanges();
            }
            if (!this.changeLog.isEmpty()) {
                writeBackCache();
                lastModifiedTime = this.prefsFile.lastModified();
                if (this.lastSyncTime <= lastModifiedTime) {
                    this.lastSyncTime = 1000 + lastModifiedTime;
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

    protected void flushSpi() throws BackingStoreException {
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
        byte[] result = new byte[(len * 2)];
        int j = 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            int i2 = j + 1;
            result[j] = (byte) (c >> 8);
            j = i2 + 1;
            result[i2] = (byte) c;
        }
        return result;
    }

    private static String nodeName(String dirName) {
        if (dirName.charAt(0) != '_') {
            return dirName;
        }
        byte[] a = Base64.altBase64ToByteArray(dirName.substring(1));
        StringBuffer result = new StringBuffer(a.length / 2);
        int i = 0;
        while (i < a.length) {
            int i2 = i + 1;
            int highByte = a[i] & 255;
            i = i2 + 1;
            result.append((char) ((highByte << 8) | (a[i2] & 255)));
        }
        return result.toString();
    }

    private boolean lockFile(boolean shared) throws SecurityException {
        boolean usernode = isUserNode();
        int errorCode = 0;
        File lockFile = usernode ? userLockFile : systemLockFile;
        long sleepTime = (long) INIT_SLEEP_TIME;
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
            throw new SecurityException("Could not lock " + (isUserNode() ? "User prefs." : "System prefs.") + " Lock file access denied.");
        } else if (errorCode != 11) {
            getLogger().warning("Could not lock " + (isUserNode() ? "User prefs. " : "System prefs.") + " Unix error code " + errorCode + ".");
        }
    }

    private void unlockFile() {
        boolean usernode = isUserNode();
        File lockFile;
        if (usernode) {
            lockFile = userLockFile;
        } else {
            lockFile = systemLockFile;
        }
        int lockHandle = usernode ? userRootLockHandle : systemRootLockHandle;
        if (lockHandle == 0) {
            getLogger().warning("Unlock: zero lockHandle for " + (usernode ? "user" : "system") + " preferences.)");
            return;
        }
        int result = unlockFile0(lockHandle);
        if (result != 0) {
            getLogger().warning("Could not drop file-lock on " + (isUserNode() ? "user" : "system") + " preferences." + " Unix error code " + result + ".");
            if (result == 13) {
                throw new SecurityException("Could not unlock" + (isUserNode() ? "User prefs." : "System prefs.") + " Lock file access denied.");
            }
        }
        if (isUserNode()) {
            userRootLockHandle = 0;
        } else {
            systemRootLockHandle = 0;
        }
    }
}
