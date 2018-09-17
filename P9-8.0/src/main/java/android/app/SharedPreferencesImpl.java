package android.app;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.FileUtils;
import android.os.Looper;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ExponentiallyBucketedHistogram;
import com.android.internal.util.XmlUtils;
import com.google.android.collect.Maps;
import dalvik.system.BlockGuard;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import org.xmlpull.v1.XmlPullParserException;

final class SharedPreferencesImpl implements SharedPreferences {
    private static final Object CONTENT = new Object();
    private static final boolean DEBUG = false;
    private static final long MAX_FSYNC_DURATION_MILLIS = 256;
    private static final String TAG = "SharedPreferencesImpl";
    private final File mBackupFile;
    @GuardedBy("this")
    private long mCurrentMemoryStateGeneration;
    @GuardedBy("mWritingToDiskLock")
    private long mDiskStateGeneration;
    @GuardedBy("mLock")
    private int mDiskWritesInFlight = 0;
    private final File mFile;
    @GuardedBy("mLock")
    private final WeakHashMap<OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap();
    @GuardedBy("mLock")
    private boolean mLoaded = false;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private Map<String, Object> mMap;
    private int mMode;
    private int mNumSync = 0;
    @GuardedBy("mLock")
    private long mStatSize;
    @GuardedBy("mLock")
    private long mStatTimestamp;
    @GuardedBy("mWritingToDiskLock")
    private final ExponentiallyBucketedHistogram mSyncTimes = new ExponentiallyBucketedHistogram(16);
    private final Object mWritingToDiskLock = new Object();

    public final class EditorImpl implements Editor {
        @GuardedBy("mLock")
        private boolean mClear = false;
        private final Object mLock = new Object();
        @GuardedBy("mLock")
        private final Map<String, Object> mModified = Maps.newHashMap();

        public Editor putString(String key, String value) {
            synchronized (this.mLock) {
                this.mModified.put(key, value);
            }
            return this;
        }

        public Editor putStringSet(String key, Set<String> values) {
            Object obj = null;
            synchronized (this.mLock) {
                Map map = this.mModified;
                if (values != null) {
                    obj = new HashSet(values);
                }
                map.put(key, obj);
            }
            return this;
        }

        public Editor putInt(String key, int value) {
            synchronized (this.mLock) {
                this.mModified.put(key, Integer.valueOf(value));
            }
            return this;
        }

        public Editor putLong(String key, long value) {
            synchronized (this.mLock) {
                this.mModified.put(key, Long.valueOf(value));
            }
            return this;
        }

        public Editor putFloat(String key, float value) {
            synchronized (this.mLock) {
                this.mModified.put(key, Float.valueOf(value));
            }
            return this;
        }

        public Editor putBoolean(String key, boolean value) {
            synchronized (this.mLock) {
                this.mModified.put(key, Boolean.valueOf(value));
            }
            return this;
        }

        public Editor remove(String key) {
            synchronized (this.mLock) {
                this.mModified.put(key, this);
            }
            return this;
        }

        public Editor clear() {
            synchronized (this.mLock) {
                this.mClear = true;
            }
            return this;
        }

        public void apply() {
            final long startTime = System.currentTimeMillis();
            final MemoryCommitResult mcr = commitToMemory();
            final Runnable awaitCommit = new Runnable() {
                public void run() {
                    try {
                        mcr.writtenToDiskLatch.await();
                    } catch (InterruptedException e) {
                    }
                }
            };
            QueuedWork.addFinisher(awaitCommit);
            SharedPreferencesImpl.this.enqueueDiskWrite(mcr, new Runnable() {
                public void run() {
                    awaitCommit.run();
                    QueuedWork.removeFinisher(awaitCommit);
                }
            });
            notifyListeners(mcr);
        }

        private MemoryCommitResult commitToMemory() {
            Throwable th;
            List list = null;
            Set listeners = null;
            synchronized (SharedPreferencesImpl.this.mLock) {
                try {
                    long memoryStateGeneration;
                    if (SharedPreferencesImpl.this.mDiskWritesInFlight > 0) {
                        SharedPreferencesImpl.this.mMap = new HashMap(SharedPreferencesImpl.this.mMap);
                    }
                    Map<String, Object> mapToWriteToDisk = SharedPreferencesImpl.this.mMap;
                    SharedPreferencesImpl sharedPreferencesImpl = SharedPreferencesImpl.this;
                    sharedPreferencesImpl.mDiskWritesInFlight = sharedPreferencesImpl.mDiskWritesInFlight + 1;
                    boolean hasListeners = SharedPreferencesImpl.this.mListeners.size() > 0;
                    if (hasListeners) {
                        List<String> keysModified = new ArrayList();
                        try {
                            listeners = new HashSet(SharedPreferencesImpl.this.mListeners.keySet());
                            list = keysModified;
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    synchronized (this.mLock) {
                        boolean changesMade = false;
                        if (this.mClear) {
                            if (!SharedPreferencesImpl.this.mMap.isEmpty()) {
                                changesMade = true;
                                SharedPreferencesImpl.this.mMap.clear();
                            }
                            this.mClear = false;
                        }
                        for (Entry<String, Object> e : this.mModified.entrySet()) {
                            String k = (String) e.getKey();
                            EditorImpl v = e.getValue();
                            if (v != this && v != null) {
                                if (SharedPreferencesImpl.this.mMap.containsKey(k)) {
                                    Object existingValue = SharedPreferencesImpl.this.mMap.get(k);
                                    if (existingValue != null && existingValue.equals(v)) {
                                    }
                                }
                                SharedPreferencesImpl.this.mMap.put(k, v);
                            } else if (SharedPreferencesImpl.this.mMap.containsKey(k)) {
                                SharedPreferencesImpl.this.mMap.remove(k);
                            }
                            changesMade = true;
                            if (hasListeners) {
                                list.add(k);
                            }
                        }
                        this.mModified.clear();
                        if (changesMade) {
                            sharedPreferencesImpl = SharedPreferencesImpl.this;
                            sharedPreferencesImpl.mCurrentMemoryStateGeneration = sharedPreferencesImpl.mCurrentMemoryStateGeneration + 1;
                        }
                        memoryStateGeneration = SharedPreferencesImpl.this.mCurrentMemoryStateGeneration;
                    }
                    return new MemoryCommitResult(memoryStateGeneration, list, listeners, mapToWriteToDisk, null);
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }

        public boolean commit() {
            MemoryCommitResult mcr = commitToMemory();
            SharedPreferencesImpl.this.enqueueDiskWrite(mcr, null);
            try {
                mcr.writtenToDiskLatch.await();
                notifyListeners(mcr);
                return mcr.writeToDiskResult;
            } catch (InterruptedException e) {
                return false;
            }
        }

        private void notifyListeners(final MemoryCommitResult mcr) {
            if (mcr.listeners != null && mcr.keysModified != null && mcr.keysModified.size() != 0) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    for (int i = mcr.keysModified.size() - 1; i >= 0; i--) {
                        String key = (String) mcr.keysModified.get(i);
                        for (OnSharedPreferenceChangeListener listener : mcr.listeners) {
                            if (listener != null) {
                                listener.onSharedPreferenceChanged(SharedPreferencesImpl.this, key);
                            }
                        }
                    }
                } else {
                    ActivityThread.sMainThreadHandler.post(new Runnable() {
                        public void run() {
                            EditorImpl.this.notifyListeners(mcr);
                        }
                    });
                }
            }
        }
    }

    private static class MemoryCommitResult {
        final List<String> keysModified;
        final Set<OnSharedPreferenceChangeListener> listeners;
        final Map<String, Object> mapToWriteToDisk;
        final long memoryStateGeneration;
        boolean wasWritten;
        @GuardedBy("mWritingToDiskLock")
        volatile boolean writeToDiskResult;
        final CountDownLatch writtenToDiskLatch;

        /* synthetic */ MemoryCommitResult(long memoryStateGeneration, List keysModified, Set listeners, Map mapToWriteToDisk, MemoryCommitResult -this4) {
            this(memoryStateGeneration, keysModified, listeners, mapToWriteToDisk);
        }

        private MemoryCommitResult(long memoryStateGeneration, List<String> keysModified, Set<OnSharedPreferenceChangeListener> listeners, Map<String, Object> mapToWriteToDisk) {
            this.writtenToDiskLatch = new CountDownLatch(1);
            this.writeToDiskResult = false;
            this.wasWritten = false;
            this.memoryStateGeneration = memoryStateGeneration;
            this.keysModified = keysModified;
            this.listeners = listeners;
            this.mapToWriteToDisk = mapToWriteToDisk;
        }

        void setDiskWriteResult(boolean wasWritten, boolean result) {
            this.wasWritten = wasWritten;
            this.writeToDiskResult = result;
            this.writtenToDiskLatch.countDown();
        }
    }

    SharedPreferencesImpl(File file, int mode) {
        this.mFile = file;
        this.mBackupFile = makeBackupFile(file);
        this.mMode = mode;
        this.mLoaded = false;
        this.mMap = null;
        startLoadFromDisk();
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    public void awaitLoaded() {
        synchronized (this) {
            while (!this.mLoaded) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void startLoadFromDisk() {
        synchronized (this.mLock) {
            this.mLoaded = false;
        }
        new Thread("SharedPreferencesImpl-load") {
            public void run() {
                SharedPreferencesImpl.this.loadFromDisk();
            }
        }.start();
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:0x00ca A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:24:0x0076} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00a9 A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:22:0x0068} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0080  */
    /* JADX WARNING: Missing block: B:13:0x0024, code:
            if (r10.mFile.exists() == false) goto L_0x0053;
     */
    /* JADX WARNING: Missing block: B:15:0x002e, code:
            if ((r10.mFile.canRead() ^ 1) == 0) goto L_0x0053;
     */
    /* JADX WARNING: Missing block: B:16:0x0030, code:
            android.util.Log.w(TAG, "Attempt to read preferences file " + r10.mFile + " without permission");
     */
    /* JADX WARNING: Missing block: B:17:0x0053, code:
            r2 = null;
            r3 = null;
     */
    /* JADX WARNING: Missing block: B:19:?, code:
            r3 = android.system.Os.stat(r10.mFile.getPath());
     */
    /* JADX WARNING: Missing block: B:20:0x0065, code:
            if (r10.mFile.canRead() == false) goto L_0x007d;
     */
    /* JADX WARNING: Missing block: B:21:0x0067, code:
            r4 = null;
     */
    /* JADX WARNING: Missing block: B:23:?, code:
            r5 = new java.io.BufferedInputStream(new java.io.FileInputStream(r10.mFile), 16384);
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            r2 = com.android.internal.util.XmlUtils.readMapXml(r5);
     */
    /* JADX WARNING: Missing block: B:27:?, code:
            libcore.io.IoUtils.closeQuietly(r5);
     */
    /* JADX WARNING: Missing block: B:29:0x007f, code:
            monitor-enter(r10.mLock);
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            r10.mLoaded = true;
     */
    /* JADX WARNING: Missing block: B:33:0x0083, code:
            if (r2 != null) goto L_0x0085;
     */
    /* JADX WARNING: Missing block: B:34:0x0085, code:
            r10.mMap = r2;
            r10.mStatTimestamp = r3.st_mtime;
            r10.mStatSize = r3.st_size;
     */
    /* JADX WARNING: Missing block: B:35:0x008f, code:
            r10.mLock.notifyAll();
     */
    /* JADX WARNING: Missing block: B:37:0x0095, code:
            return;
     */
    /* JADX WARNING: Missing block: B:41:0x0099, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            android.util.Log.w(TAG, "getSharedPreferences", r1);
     */
    /* JADX WARNING: Missing block: B:45:?, code:
            libcore.io.IoUtils.closeQuietly(r4);
     */
    /* JADX WARNING: Missing block: B:47:0x00a9, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:49:?, code:
            android.util.Log.w(TAG, "getSharedPreferences", r1);
     */
    /* JADX WARNING: Missing block: B:51:?, code:
            libcore.io.IoUtils.closeQuietly(r4);
     */
    /* JADX WARNING: Missing block: B:52:0x00b7, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:53:0x00b8, code:
            libcore.io.IoUtils.closeQuietly(r4);
     */
    /* JADX WARNING: Missing block: B:54:0x00bb, code:
            throw r6;
     */
    /* JADX WARNING: Missing block: B:56:?, code:
            r10.mMap = new java.util.HashMap();
     */
    /* JADX WARNING: Missing block: B:60:0x00c7, code:
            r6 = th;
     */
    /* JADX WARNING: Missing block: B:61:0x00c8, code:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:62:0x00ca, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:63:0x00cb, code:
            r4 = r5;
     */
    /* JADX WARNING: Missing block: B:64:0x00cd, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:65:0x00ce, code:
            r4 = r5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadFromDisk() {
        synchronized (this.mLock) {
            if (this.mLoaded) {
            } else if (this.mBackupFile.exists()) {
                this.mFile.delete();
                this.mBackupFile.renameTo(this.mFile);
            }
        }
    }

    static File makeBackupFile(File prefsFile) {
        return new File(prefsFile.getPath() + ".bak");
    }

    void startReloadIfChangedUnexpectedly() {
        synchronized (this.mLock) {
            if (hasFileChangedUnexpectedly()) {
                startLoadFromDisk();
                return;
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:?, code:
            dalvik.system.BlockGuard.getThreadPolicy().onReadFromDisk();
            r1 = android.system.Os.stat(r10.mFile.getPath());
     */
    /* JADX WARNING: Missing block: B:10:0x001c, code:
            r4 = r10.mLock;
     */
    /* JADX WARNING: Missing block: B:11:0x001f, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:14:0x0026, code:
            if (r10.mStatTimestamp != r1.st_mtime) goto L_0x0030;
     */
    /* JADX WARNING: Missing block: B:17:0x002e, code:
            if (r10.mStatSize == r1.st_size) goto L_0x0037;
     */
    /* JADX WARNING: Missing block: B:18:0x0030, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:19:0x0031, code:
            return r2;
     */
    /* JADX WARNING: Missing block: B:24:0x0036, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:25:0x0037, code:
            r2 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean hasFileChangedUnexpectedly() {
        boolean z = true;
        synchronized (this.mLock) {
            if (this.mDiskWritesInFlight > 0) {
                return false;
            }
        }
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this.mLock) {
            this.mListeners.put(listener, CONTENT);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this.mLock) {
            this.mListeners.remove(listener);
        }
    }

    private void awaitLoadedLocked() {
        if (!this.mLoaded) {
            BlockGuard.getThreadPolicy().onReadFromDisk();
        }
        while (!this.mLoaded) {
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public Map<String, ?> getAll() {
        Map hashMap;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            hashMap = new HashMap(this.mMap);
        }
        return hashMap;
    }

    public String getString(String key, String defValue) {
        String v;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            v = (String) this.mMap.get(key);
            if (v == null) {
                v = defValue;
            }
        }
        return v;
    }

    public Set<String> getStringSet(String key, Set<String> defValues) {
        Set<String> v;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            v = (Set) this.mMap.get(key);
            if (v == null) {
                v = defValues;
            }
        }
        return v;
    }

    public int getInt(String key, int defValue) {
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Integer v = (Integer) this.mMap.get(key);
            if (v != null) {
                defValue = v.intValue();
            }
        }
        return defValue;
    }

    public long getLong(String key, long defValue) {
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Long v = (Long) this.mMap.get(key);
            if (v != null) {
                defValue = v.longValue();
            }
        }
        return defValue;
    }

    public float getFloat(String key, float defValue) {
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Float v = (Float) this.mMap.get(key);
            if (v != null) {
                defValue = v.floatValue();
            }
        }
        return defValue;
    }

    public boolean getBoolean(String key, boolean defValue) {
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Boolean v = (Boolean) this.mMap.get(key);
            if (v != null) {
                defValue = v.booleanValue();
            }
        }
        return defValue;
    }

    public boolean contains(String key) {
        boolean containsKey;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            containsKey = this.mMap.containsKey(key);
        }
        return containsKey;
    }

    public Editor edit() {
        synchronized (this.mLock) {
            awaitLoadedLocked();
        }
        return new EditorImpl();
    }

    private void enqueueDiskWrite(final MemoryCommitResult mcr, final Runnable postWriteRunnable) {
        final boolean isFromSyncCommit = postWriteRunnable == null;
        Runnable writeToDiskRunnable = new Runnable() {
            public void run() {
                synchronized (SharedPreferencesImpl.this.mWritingToDiskLock) {
                    SharedPreferencesImpl.this.writeToFile(mcr, isFromSyncCommit);
                }
                synchronized (SharedPreferencesImpl.this.mLock) {
                    SharedPreferencesImpl sharedPreferencesImpl = SharedPreferencesImpl.this;
                    sharedPreferencesImpl.mDiskWritesInFlight = sharedPreferencesImpl.mDiskWritesInFlight - 1;
                }
                if (postWriteRunnable != null) {
                    postWriteRunnable.run();
                }
            }
        };
        if (isFromSyncCommit) {
            boolean wasEmpty;
            synchronized (this.mLock) {
                wasEmpty = this.mDiskWritesInFlight == 1;
            }
            if (wasEmpty) {
                writeToDiskRunnable.run();
                return;
            }
        }
        QueuedWork.queue(writeToDiskRunnable, isFromSyncCommit ^ 1);
    }

    private static FileOutputStream createFileOutputStream(File file) {
        FileOutputStream str = null;
        try {
            str = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            File parent = file.getParentFile();
            if (parent.mkdir()) {
                FileUtils.setPermissions(parent.getPath(), 505, -1, -1);
                try {
                    str = new FileOutputStream(file);
                } catch (FileNotFoundException e2) {
                    Log.e(TAG, "Couldn't create SharedPreferences file " + file, e2);
                }
            } else {
                Log.e(TAG, "Couldn't create directory for SharedPreferences file " + file);
                return null;
            }
        }
        return str;
    }

    private void writeToFile(MemoryCommitResult mcr, boolean isFromSyncCommit) {
        if (this.mFile.exists()) {
            boolean needsWrite = false;
            if (this.mDiskStateGeneration < mcr.memoryStateGeneration) {
                if (isFromSyncCommit) {
                    needsWrite = true;
                } else {
                    synchronized (this.mLock) {
                        if (this.mCurrentMemoryStateGeneration == mcr.memoryStateGeneration) {
                            needsWrite = true;
                        }
                    }
                }
            }
            if (!needsWrite) {
                mcr.setDiskWriteResult(false, true);
                return;
            } else if (this.mBackupFile.exists()) {
                this.mFile.delete();
            } else if (!this.mFile.renameTo(this.mBackupFile)) {
                Log.e(TAG, "Couldn't rename file " + this.mFile + " to backup file " + this.mBackupFile);
                mcr.setDiskWriteResult(false, false);
                return;
            }
        }
        try {
            OutputStream str = createFileOutputStream(this.mFile);
            if (str == null) {
                mcr.setDiskWriteResult(false, false);
                return;
            }
            XmlUtils.writeMapXml(mcr.mapToWriteToDisk, str);
            long writeTime = System.currentTimeMillis();
            FileUtils.sync(str);
            long fsyncTime = System.currentTimeMillis();
            str.close();
            ContextImpl.setFilePermissionsFromMode(this.mFile.getPath(), this.mMode, 0);
            try {
                StructStat stat = Os.stat(this.mFile.getPath());
                synchronized (this.mLock) {
                    this.mStatTimestamp = stat.st_mtime;
                    this.mStatSize = stat.st_size;
                }
            } catch (ErrnoException e) {
                Log.e(TAG, "writeToFile");
            }
            this.mBackupFile.delete();
            this.mDiskStateGeneration = mcr.memoryStateGeneration;
            mcr.setDiskWriteResult(true, true);
            long fsyncDuration = fsyncTime - writeTime;
            this.mSyncTimes.add(Long.valueOf(fsyncDuration).intValue());
            this.mNumSync++;
            if (this.mNumSync % 1024 == 0 || fsyncDuration > 256) {
                this.mSyncTimes.log(TAG, "Time required to fsync " + this.mFile + ": ");
            }
            return;
        } catch (XmlPullParserException e2) {
            Log.w(TAG, "writeToFile: Got exception:", e2);
        } catch (IOException e3) {
            Log.w(TAG, "writeToFile: Got exception:", e3);
        }
        if (this.mFile.exists() && !this.mFile.delete()) {
            Log.e(TAG, "Couldn't clean up partially-written file " + this.mFile);
        }
        mcr.setDiskWriteResult(false, false);
    }
}
