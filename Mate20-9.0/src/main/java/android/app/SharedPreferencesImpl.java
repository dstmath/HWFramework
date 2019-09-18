package android.app;

import android.app.SharedPreferencesImpl;
import android.content.SharedPreferences;
import android.hardware.radio.V1_0.RadioError;
import android.os.FileUtils;
import android.os.Looper;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.system.StructTimespec;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.ExponentiallyBucketedHistogram;
import com.android.internal.util.XmlUtils;
import dalvik.system.BlockGuard;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    /* access modifiers changed from: private */
    @GuardedBy("this")
    public long mCurrentMemoryStateGeneration;
    @GuardedBy("mWritingToDiskLock")
    private long mDiskStateGeneration;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public int mDiskWritesInFlight = 0;
    private final File mFile;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public final WeakHashMap<SharedPreferences.OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<>();
    @GuardedBy("mLock")
    private boolean mLoaded = false;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public Map<String, Object> mMap;
    private final int mMode;
    private int mNumSync = 0;
    @GuardedBy("mLock")
    private long mStatSize;
    @GuardedBy("mLock")
    private StructTimespec mStatTimestamp;
    @GuardedBy("mWritingToDiskLock")
    private final ExponentiallyBucketedHistogram mSyncTimes = new ExponentiallyBucketedHistogram(16);
    @GuardedBy("mLock")
    private Throwable mThrowable;
    /* access modifiers changed from: private */
    public final Object mWritingToDiskLock = new Object();

    public final class EditorImpl implements SharedPreferences.Editor {
        @GuardedBy("mEditorLock")
        private boolean mClear = false;
        private final Object mEditorLock = new Object();
        @GuardedBy("mEditorLock")
        private final Map<String, Object> mModified = new HashMap();

        public EditorImpl() {
        }

        public SharedPreferences.Editor putString(String key, String value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, value);
            }
            return this;
        }

        public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, values == null ? null : new HashSet(values));
            }
            return this;
        }

        public SharedPreferences.Editor putInt(String key, int value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, Integer.valueOf(value));
            }
            return this;
        }

        public SharedPreferences.Editor putLong(String key, long value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, Long.valueOf(value));
            }
            return this;
        }

        public SharedPreferences.Editor putFloat(String key, float value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, Float.valueOf(value));
            }
            return this;
        }

        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, Boolean.valueOf(value));
            }
            return this;
        }

        public SharedPreferences.Editor remove(String key) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, this);
            }
            return this;
        }

        public SharedPreferences.Editor clear() {
            synchronized (this.mEditorLock) {
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

        /* JADX WARNING: Removed duplicated region for block: B:43:0x00b2  */
        private MemoryCommitResult commitToMemory() {
            Map<String, Object> mapToWriteToDisk;
            long memoryStateGeneration;
            List<String> keysModified = null;
            Set<SharedPreferences.OnSharedPreferenceChangeListener> listeners = null;
            synchronized (SharedPreferencesImpl.this.mLock) {
                if (SharedPreferencesImpl.this.mDiskWritesInFlight > 0) {
                    Map unused = SharedPreferencesImpl.this.mMap = new HashMap(SharedPreferencesImpl.this.mMap);
                }
                mapToWriteToDisk = SharedPreferencesImpl.this.mMap;
                int unused2 = SharedPreferencesImpl.this.mDiskWritesInFlight = SharedPreferencesImpl.this.mDiskWritesInFlight + 1;
                boolean hasListeners = SharedPreferencesImpl.this.mListeners.size() > 0;
                if (hasListeners) {
                    keysModified = new ArrayList<>();
                    listeners = new HashSet<>(SharedPreferencesImpl.this.mListeners.keySet());
                }
                synchronized (this.mEditorLock) {
                    boolean changesMade = false;
                    if (this.mClear) {
                        if (!mapToWriteToDisk.isEmpty()) {
                            changesMade = true;
                            mapToWriteToDisk.clear();
                        }
                        this.mClear = false;
                    }
                    for (Map.Entry<String, Object> e : this.mModified.entrySet()) {
                        String k = e.getKey();
                        Object v = e.getValue();
                        if (v != this) {
                            if (v != null) {
                                if (mapToWriteToDisk.containsKey(k)) {
                                    Object existingValue = mapToWriteToDisk.get(k);
                                    if (existingValue != null && existingValue.equals(v)) {
                                    }
                                }
                                mapToWriteToDisk.put(k, v);
                                changesMade = true;
                                if (hasListeners) {
                                    keysModified.add(k);
                                }
                            }
                        }
                        if (mapToWriteToDisk.containsKey(k)) {
                            mapToWriteToDisk.remove(k);
                            changesMade = true;
                            if (hasListeners) {
                            }
                        }
                    }
                    this.mModified.clear();
                    if (changesMade) {
                        long unused3 = SharedPreferencesImpl.this.mCurrentMemoryStateGeneration = 1 + SharedPreferencesImpl.this.mCurrentMemoryStateGeneration;
                    }
                    memoryStateGeneration = SharedPreferencesImpl.this.mCurrentMemoryStateGeneration;
                }
            }
            MemoryCommitResult memoryCommitResult = new MemoryCommitResult(memoryStateGeneration, keysModified, listeners, mapToWriteToDisk);
            return memoryCommitResult;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x001c, code lost:
            return false;
         */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
        public boolean commit() {
            MemoryCommitResult mcr = commitToMemory();
            SharedPreferencesImpl.this.enqueueDiskWrite(mcr, null);
            mcr.writtenToDiskLatch.await();
            notifyListeners(mcr);
            return mcr.writeToDiskResult;
        }

        /* access modifiers changed from: private */
        public void notifyListeners(MemoryCommitResult mcr) {
            if (mcr.listeners != null && mcr.keysModified != null && mcr.keysModified.size() != 0) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    for (int i = mcr.keysModified.size() - 1; i >= 0; i--) {
                        String key = mcr.keysModified.get(i);
                        for (SharedPreferences.OnSharedPreferenceChangeListener listener : mcr.listeners) {
                            if (listener != null) {
                                listener.onSharedPreferenceChanged(SharedPreferencesImpl.this, key);
                            }
                        }
                    }
                } else {
                    ActivityThread.sMainThreadHandler.post(new Runnable(mcr) {
                        private final /* synthetic */ SharedPreferencesImpl.MemoryCommitResult f$1;

                        {
                            this.f$1 = r2;
                        }

                        public final void run() {
                            SharedPreferencesImpl.EditorImpl.this.notifyListeners(this.f$1);
                        }
                    });
                }
            }
        }
    }

    private static class MemoryCommitResult {
        final List<String> keysModified;
        final Set<SharedPreferences.OnSharedPreferenceChangeListener> listeners;
        final Map<String, Object> mapToWriteToDisk;
        final long memoryStateGeneration;
        boolean wasWritten;
        @GuardedBy("mWritingToDiskLock")
        volatile boolean writeToDiskResult;
        final CountDownLatch writtenToDiskLatch;

        private MemoryCommitResult(long memoryStateGeneration2, List<String> keysModified2, Set<SharedPreferences.OnSharedPreferenceChangeListener> listeners2, Map<String, Object> mapToWriteToDisk2) {
            this.writtenToDiskLatch = new CountDownLatch(1);
            this.writeToDiskResult = false;
            this.wasWritten = false;
            this.memoryStateGeneration = memoryStateGeneration2;
            this.keysModified = keysModified2;
            this.listeners = listeners2;
            this.mapToWriteToDisk = mapToWriteToDisk2;
        }

        /* access modifiers changed from: package-private */
        public void setDiskWriteResult(boolean wasWritten2, boolean result) {
            this.wasWritten = wasWritten2;
            this.writeToDiskResult = result;
            this.writtenToDiskLatch.countDown();
        }
    }

    static /* synthetic */ int access$310(SharedPreferencesImpl x0) {
        int i = x0.mDiskWritesInFlight;
        x0.mDiskWritesInFlight = i - 1;
        return i;
    }

    SharedPreferencesImpl(File file, int mode) {
        this.mFile = file;
        this.mBackupFile = makeBackupFile(file);
        this.mMode = mode;
        this.mLoaded = false;
        this.mMap = null;
        this.mThrowable = null;
        startLoadFromDisk();
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

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        if (r8.mFile.exists() == false) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        if (r8.mFile.canRead() != false) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
        android.util.Log.w(TAG, "Attempt to read preferences file " + r8.mFile + " without permission");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004b, code lost:
        r0 = null;
        r1 = null;
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r1 = android.system.Os.stat(r8.mFile.getPath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0060, code lost:
        if (r8.mFile.canRead() == false) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r2 = new java.io.BufferedInputStream(new java.io.FileInputStream(r8.mFile), 16384);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0076, code lost:
        r0 = com.android.internal.util.XmlUtils.readMapXml(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        libcore.io.IoUtils.closeQuietly(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007b, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x007d, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        android.util.Log.w(TAG, "Cannot read " + r8.mFile.getAbsolutePath(), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        libcore.io.IoUtils.closeQuietly(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x009e, code lost:
        libcore.io.IoUtils.closeQuietly(null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a1, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a2, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a3, code lost:
        r3 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00a7, code lost:
        r2 = r1;
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00ab, code lost:
        monitor-enter(r8.mLock);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:?, code lost:
        r8.mLoaded = true;
        r8.mThrowable = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b1, code lost:
        if (r3 == null) goto L_0x00b3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00b3, code lost:
        if (r1 != null) goto L_0x00b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
        r8.mMap = r1;
        r8.mStatTimestamp = r2.st_mtim;
        r8.mStatSize = r2.st_size;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00c0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00c2, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00c4, code lost:
        r8.mMap = new java.util.HashMap();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:?, code lost:
        r8.mThrowable = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:?, code lost:
        r0 = r8.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00d2, code lost:
        r8.mLock.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00d7, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00d8, code lost:
        r0 = r8.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00da, code lost:
        r0.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00df, code lost:
        return;
     */
    public void loadFromDisk() {
        synchronized (this.mLock) {
            if (!this.mLoaded) {
                if (this.mBackupFile.exists()) {
                    this.mFile.delete();
                    this.mBackupFile.renameTo(this.mFile);
                }
            }
        }
    }

    static File makeBackupFile(File prefsFile) {
        return new File(prefsFile.getPath() + ".bak");
    }

    /* access modifiers changed from: package-private */
    public void startReloadIfChangedUnexpectedly() {
        synchronized (this.mLock) {
            if (hasFileChangedUnexpectedly()) {
                startLoadFromDisk();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        dalvik.system.BlockGuard.getThreadPolicy().onReadFromDisk();
        r1 = android.system.Os.stat(r8.mFile.getPath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001d, code lost:
        r3 = r8.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0021, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002a, code lost:
        if (r1.st_mtim.equals(r8.mStatTimestamp) == false) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0032, code lost:
        if (r8.mStatSize == r1.st_size) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0035, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0037, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0038, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003d, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x000b, code lost:
        r0 = true;
     */
    private boolean hasFileChangedUnexpectedly() {
        synchronized (this.mLock) {
            if (this.mDiskWritesInFlight > 0) {
                return false;
            }
        }
    }

    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        synchronized (this.mLock) {
            this.mListeners.put(listener, CONTENT);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        synchronized (this.mLock) {
            this.mListeners.remove(listener);
        }
    }

    @GuardedBy("mLock")
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
        if (this.mThrowable != null) {
            throw new IllegalStateException(this.mThrowable);
        }
    }

    public Map<String, ?> getAll() {
        HashMap hashMap;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            hashMap = new HashMap(this.mMap);
        }
        return hashMap;
    }

    public String getString(String key, String defValue) {
        String str;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            String v = (String) this.mMap.get(key);
            str = v != null ? v : defValue;
        }
        return str;
    }

    public Set<String> getStringSet(String key, Set<String> defValues) {
        Set<String> set;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Set<String> v = (Set) this.mMap.get(key);
            set = v != null ? v : defValues;
        }
        return set;
    }

    public int getInt(String key, int defValue) {
        int intValue;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Integer v = (Integer) this.mMap.get(key);
            intValue = v != null ? v.intValue() : defValue;
        }
        return intValue;
    }

    public long getLong(String key, long defValue) {
        long longValue;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Long v = (Long) this.mMap.get(key);
            longValue = v != null ? v.longValue() : defValue;
        }
        return longValue;
    }

    public float getFloat(String key, float defValue) {
        float floatValue;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Float v = (Float) this.mMap.get(key);
            floatValue = v != null ? v.floatValue() : defValue;
        }
        return floatValue;
    }

    public boolean getBoolean(String key, boolean defValue) {
        boolean booleanValue;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Boolean v = (Boolean) this.mMap.get(key);
            booleanValue = v != null ? v.booleanValue() : defValue;
        }
        return booleanValue;
    }

    public boolean contains(String key) {
        boolean containsKey;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            containsKey = this.mMap.containsKey(key);
        }
        return containsKey;
    }

    public SharedPreferences.Editor edit() {
        synchronized (this.mLock) {
            awaitLoadedLocked();
        }
        return new EditorImpl();
    }

    /* access modifiers changed from: private */
    public void enqueueDiskWrite(final MemoryCommitResult mcr, final Runnable postWriteRunnable) {
        boolean wasEmpty;
        boolean z = false;
        final boolean isFromSyncCommit = postWriteRunnable == null;
        Runnable writeToDiskRunnable = new Runnable() {
            public void run() {
                synchronized (SharedPreferencesImpl.this.mWritingToDiskLock) {
                    SharedPreferencesImpl.this.writeToFile(mcr, isFromSyncCommit);
                }
                synchronized (SharedPreferencesImpl.this.mLock) {
                    SharedPreferencesImpl.access$310(SharedPreferencesImpl.this);
                }
                if (postWriteRunnable != null) {
                    postWriteRunnable.run();
                }
            }
        };
        if (isFromSyncCommit) {
            synchronized (this.mLock) {
                wasEmpty = this.mDiskWritesInFlight == 1;
            }
            if (wasEmpty) {
                writeToDiskRunnable.run();
                return;
            }
        }
        if (!isFromSyncCommit) {
            z = true;
        }
        QueuedWork.queue(writeToDiskRunnable, z);
    }

    private static FileOutputStream createFileOutputStream(File file) {
        FileOutputStream str = null;
        try {
            str = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            File parent = file.getParentFile();
            if (!parent.mkdir()) {
                Log.e(TAG, "Couldn't create directory for SharedPreferences file " + file);
                return null;
            }
            FileUtils.setPermissions(parent.getPath(), RadioError.OEM_ERROR_5, -1, -1);
            try {
                str = new FileOutputStream(file);
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "Couldn't create SharedPreferences file " + file, e2);
            }
        }
        return str;
    }

    /* access modifiers changed from: private */
    @GuardedBy("mWritingToDiskLock")
    public void writeToFile(MemoryCommitResult mcr, boolean isFromSyncCommit) {
        MemoryCommitResult memoryCommitResult = mcr;
        if (this.mFile.exists()) {
            boolean needsWrite = false;
            if (this.mDiskStateGeneration < memoryCommitResult.memoryStateGeneration) {
                if (isFromSyncCommit) {
                    needsWrite = true;
                } else {
                    synchronized (this.mLock) {
                        try {
                            if (this.mCurrentMemoryStateGeneration == memoryCommitResult.memoryStateGeneration) {
                                needsWrite = true;
                            }
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                }
            }
            if (!needsWrite) {
                memoryCommitResult.setDiskWriteResult(false, true);
                return;
            } else if (this.mBackupFile.exists()) {
                this.mFile.delete();
            } else if (!this.mFile.renameTo(this.mBackupFile)) {
                Log.e(TAG, "Couldn't rename file " + this.mFile + " to backup file " + this.mBackupFile);
                memoryCommitResult.setDiskWriteResult(false, false);
                return;
            }
        }
        try {
            FileOutputStream str = createFileOutputStream(this.mFile);
            if (str == null) {
                memoryCommitResult.setDiskWriteResult(false, false);
                return;
            }
            XmlUtils.writeMapXml(memoryCommitResult.mapToWriteToDisk, str);
            long writeTime = System.currentTimeMillis();
            FileUtils.sync(str);
            long fsyncTime = System.currentTimeMillis();
            str.close();
            ContextImpl.setFilePermissionsFromMode(this.mFile.getPath(), this.mMode, 0);
            try {
                StructStat stat = Os.stat(this.mFile.getPath());
                synchronized (this.mLock) {
                    this.mStatTimestamp = stat.st_mtim;
                    this.mStatSize = stat.st_size;
                }
            } catch (ErrnoException e) {
            }
            this.mBackupFile.delete();
            this.mDiskStateGeneration = memoryCommitResult.memoryStateGeneration;
            memoryCommitResult.setDiskWriteResult(true, true);
            long fsyncDuration = fsyncTime - writeTime;
            this.mSyncTimes.add((int) fsyncDuration);
            this.mNumSync++;
            if (this.mNumSync % 1024 == 0 || fsyncDuration > 256) {
                this.mSyncTimes.log(TAG, "Time required to fsync " + this.mFile + ": ");
            }
        } catch (XmlPullParserException e2) {
            Log.w(TAG, "writeToFile: Got exception:", e2);
            if (this.mFile.exists() && !this.mFile.delete()) {
                Log.e(TAG, "Couldn't clean up partially-written file " + this.mFile);
            }
            memoryCommitResult.setDiskWriteResult(false, false);
        } catch (IOException e3) {
            Log.w(TAG, "writeToFile: Got exception:", e3);
            Log.e(TAG, "Couldn't clean up partially-written file " + this.mFile);
            memoryCommitResult.setDiskWriteResult(false, false);
        }
    }
}
