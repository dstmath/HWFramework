package android.app;

import android.annotation.UnsupportedAppUsage;
import android.app.SharedPreferencesImpl;
import android.content.SharedPreferences;
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

/* access modifiers changed from: package-private */
public final class SharedPreferencesImpl implements SharedPreferences {
    private static final Object CONTENT = new Object();
    private static final boolean DEBUG = false;
    private static final long MAX_FSYNC_DURATION_MILLIS = 256;
    private static final String TAG = "SharedPreferencesImpl";
    private final File mBackupFile;
    @GuardedBy({"this"})
    private long mCurrentMemoryStateGeneration;
    @GuardedBy({"mWritingToDiskLock"})
    private long mDiskStateGeneration;
    @GuardedBy({"mLock"})
    private int mDiskWritesInFlight = 0;
    @UnsupportedAppUsage
    private final File mFile;
    @GuardedBy({"mLock"})
    private final WeakHashMap<SharedPreferences.OnSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<>();
    @GuardedBy({"mLock"})
    private boolean mLoaded = false;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private Map<String, Object> mMap;
    private final int mMode;
    private int mNumSync = 0;
    @GuardedBy({"mLock"})
    private long mStatSize;
    @GuardedBy({"mLock"})
    private StructTimespec mStatTimestamp;
    @GuardedBy({"mWritingToDiskLock"})
    private final ExponentiallyBucketedHistogram mSyncTimes = new ExponentiallyBucketedHistogram(16);
    @GuardedBy({"mLock"})
    private Throwable mThrowable;
    private final Object mWritingToDiskLock = new Object();

    static /* synthetic */ int access$308(SharedPreferencesImpl x0) {
        int i = x0.mDiskWritesInFlight;
        x0.mDiskWritesInFlight = i + 1;
        return i;
    }

    static /* synthetic */ int access$310(SharedPreferencesImpl x0) {
        int i = x0.mDiskWritesInFlight;
        x0.mDiskWritesInFlight = i - 1;
        return i;
    }

    static /* synthetic */ long access$608(SharedPreferencesImpl x0) {
        long j = x0.mCurrentMemoryStateGeneration;
        x0.mCurrentMemoryStateGeneration = 1 + j;
        return j;
    }

    @UnsupportedAppUsage
    SharedPreferencesImpl(File file, int mode) {
        this.mFile = file;
        this.mBackupFile = makeBackupFile(file);
        this.mMode = mode;
        this.mLoaded = false;
        this.mMap = null;
        this.mThrowable = null;
        startLoadFromDisk();
    }

    @UnsupportedAppUsage
    private void startLoadFromDisk() {
        synchronized (this.mLock) {
            this.mLoaded = false;
        }
        new Thread("SharedPreferencesImpl-load") {
            /* class android.app.SharedPreferencesImpl.AnonymousClass1 */

            public void run() {
                SharedPreferencesImpl.this.loadFromDisk();
            }
        }.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0024, code lost:
        if (r8.mFile.exists() == false) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002c, code lost:
        if (r8.mFile.canRead() != false) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
        android.util.Log.w(android.app.SharedPreferencesImpl.TAG, "Attempt to read preferences file " + r8.mFile + " without permission");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004b, code lost:
        r0 = null;
        r1 = null;
        r2 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r1 = android.system.Os.stat(r8.mFile.getPath());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005f, code lost:
        if (r8.mFile.canRead() == false) goto L_0x00a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0061, code lost:
        r3 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:?, code lost:
        r3 = new java.io.BufferedInputStream(new java.io.FileInputStream(r8.mFile), 16384);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0075, code lost:
        r0 = com.android.internal.util.XmlUtils.readMapXml(r3);
        libcore.io.IoUtils.closeQuietly(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x007a, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x007c, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
        android.util.Log.w(android.app.SharedPreferencesImpl.TAG, "Cannot read " + r8.mFile.getAbsolutePath(), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x009e, code lost:
        libcore.io.IoUtils.closeQuietly(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a1, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00a2, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a3, code lost:
        r2 = r3;
        r3 = r1;
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        r8.mLoaded = true;
        r8.mThrowable = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b2, code lost:
        if (r2 == null) goto L_0x00b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b4, code lost:
        if (r1 != null) goto L_0x00b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:?, code lost:
        r8.mMap = r1;
        r8.mStatTimestamp = r3.st_mtim;
        r8.mStatSize = r3.st_size;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00c1, code lost:
        r8.mMap = new java.util.HashMap();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00c9, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:?, code lost:
        r8.mThrowable = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00cc, code lost:
        r0 = r8.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00cf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00d0, code lost:
        r8.mLock.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00d5, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00d6, code lost:
        r0 = r8.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00d8, code lost:
        r0.notifyAll();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00dd, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00ad  */
    private void loadFromDisk() {
        Map<String, Object> map;
        StructStat stat;
        synchronized (this.mLock) {
            if (!this.mLoaded) {
                if (this.mBackupFile.exists()) {
                    this.mFile.delete();
                    this.mBackupFile.renameTo(this.mFile);
                }
            } else {
                return;
            }
        }
        StructStat stat2 = stat;
        Map<String, Object> map2 = map;
        synchronized (this.mLock) {
        }
    }

    static File makeBackupFile(File prefsFile) {
        return new File(prefsFile.getPath() + ".bak");
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0020, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0029, code lost:
        if (r1.st_mtim.equals(r8.mStatTimestamp) == false) goto L_0x0035;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0031, code lost:
        if (r8.mStatSize == r1.st_size) goto L_0x0034;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0034, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0035, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0036, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003b, code lost:
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

    @Override // android.content.SharedPreferences
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        synchronized (this.mLock) {
            this.mListeners.put(listener, CONTENT);
        }
    }

    @Override // android.content.SharedPreferences
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        synchronized (this.mLock) {
            this.mListeners.remove(listener);
        }
    }

    @GuardedBy({"mLock"})
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
        Throwable th = this.mThrowable;
        if (th != null) {
            throw new IllegalStateException(th);
        }
    }

    @Override // android.content.SharedPreferences
    public Map<String, ?> getAll() {
        HashMap hashMap;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            hashMap = new HashMap(this.mMap);
        }
        return hashMap;
    }

    @Override // android.content.SharedPreferences
    public String getString(String key, String defValue) {
        String str;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            String v = (String) this.mMap.get(key);
            str = v != null ? v : defValue;
        }
        return str;
    }

    @Override // android.content.SharedPreferences
    public Set<String> getStringSet(String key, Set<String> defValues) {
        Set<String> set;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Set<String> v = (Set) this.mMap.get(key);
            set = v != null ? v : defValues;
        }
        return set;
    }

    @Override // android.content.SharedPreferences
    public int getInt(String key, int defValue) {
        int intValue;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Integer v = (Integer) this.mMap.get(key);
            intValue = v != null ? v.intValue() : defValue;
        }
        return intValue;
    }

    @Override // android.content.SharedPreferences
    public long getLong(String key, long defValue) {
        long longValue;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Long v = (Long) this.mMap.get(key);
            longValue = v != null ? v.longValue() : defValue;
        }
        return longValue;
    }

    @Override // android.content.SharedPreferences
    public float getFloat(String key, float defValue) {
        float floatValue;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Float v = (Float) this.mMap.get(key);
            floatValue = v != null ? v.floatValue() : defValue;
        }
        return floatValue;
    }

    @Override // android.content.SharedPreferences
    public boolean getBoolean(String key, boolean defValue) {
        boolean booleanValue;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            Boolean v = (Boolean) this.mMap.get(key);
            booleanValue = v != null ? v.booleanValue() : defValue;
        }
        return booleanValue;
    }

    @Override // android.content.SharedPreferences
    public boolean contains(String key) {
        boolean containsKey;
        synchronized (this.mLock) {
            awaitLoadedLocked();
            containsKey = this.mMap.containsKey(key);
        }
        return containsKey;
    }

    @Override // android.content.SharedPreferences
    public SharedPreferences.Editor edit() {
        synchronized (this.mLock) {
            awaitLoadedLocked();
        }
        return new EditorImpl();
    }

    /* access modifiers changed from: private */
    public static class MemoryCommitResult {
        final List<String> keysModified;
        final Set<SharedPreferences.OnSharedPreferenceChangeListener> listeners;
        final Map<String, Object> mapToWriteToDisk;
        final long memoryStateGeneration;
        boolean wasWritten;
        @GuardedBy({"mWritingToDiskLock"})
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

    public final class EditorImpl implements SharedPreferences.Editor {
        @GuardedBy({"mEditorLock"})
        private boolean mClear = false;
        private final Object mEditorLock = new Object();
        @GuardedBy({"mEditorLock"})
        private final Map<String, Object> mModified = new HashMap();

        public EditorImpl() {
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putString(String key, String value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, value);
            }
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putStringSet(String key, Set<String> values) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, values == null ? null : new HashSet(values));
            }
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putInt(String key, int value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, Integer.valueOf(value));
            }
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putLong(String key, long value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, Long.valueOf(value));
            }
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putFloat(String key, float value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, Float.valueOf(value));
            }
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, Boolean.valueOf(value));
            }
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor remove(String key) {
            synchronized (this.mEditorLock) {
                this.mModified.put(key, this);
            }
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public SharedPreferences.Editor clear() {
            synchronized (this.mEditorLock) {
                this.mClear = true;
            }
            return this;
        }

        @Override // android.content.SharedPreferences.Editor
        public void apply() {
            final long startTime = System.currentTimeMillis();
            final MemoryCommitResult mcr = commitToMemory();
            final Runnable awaitCommit = new Runnable() {
                /* class android.app.SharedPreferencesImpl.EditorImpl.AnonymousClass1 */

                public void run() {
                    try {
                        mcr.writtenToDiskLatch.await();
                    } catch (InterruptedException e) {
                    }
                }
            };
            QueuedWork.addFinisher(awaitCommit);
            SharedPreferencesImpl.this.enqueueDiskWrite(mcr, new Runnable() {
                /* class android.app.SharedPreferencesImpl.EditorImpl.AnonymousClass2 */

                public void run() {
                    awaitCommit.run();
                    QueuedWork.removeFinisher(awaitCommit);
                }
            });
            lambda$notifyListeners$0$SharedPreferencesImpl$EditorImpl(mcr);
        }

        /* JADX WARNING: Removed duplicated region for block: B:43:0x00b1  */
        private MemoryCommitResult commitToMemory() {
            Map<String, Object> mapToWriteToDisk;
            long memoryStateGeneration;
            Object existingValue;
            List<String> keysModified = null;
            Set<SharedPreferences.OnSharedPreferenceChangeListener> listeners = null;
            synchronized (SharedPreferencesImpl.this.mLock) {
                if (SharedPreferencesImpl.this.mDiskWritesInFlight > 0) {
                    SharedPreferencesImpl.this.mMap = new HashMap(SharedPreferencesImpl.this.mMap);
                }
                mapToWriteToDisk = SharedPreferencesImpl.this.mMap;
                SharedPreferencesImpl.access$308(SharedPreferencesImpl.this);
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
                                if (!mapToWriteToDisk.containsKey(k) || (existingValue = mapToWriteToDisk.get(k)) == null || !existingValue.equals(v)) {
                                    mapToWriteToDisk.put(k, v);
                                    changesMade = true;
                                    if (hasListeners) {
                                        keysModified.add(k);
                                    }
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
                        SharedPreferencesImpl.access$608(SharedPreferencesImpl.this);
                    }
                    memoryStateGeneration = SharedPreferencesImpl.this.mCurrentMemoryStateGeneration;
                }
            }
            return new MemoryCommitResult(memoryStateGeneration, keysModified, listeners, mapToWriteToDisk);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:8:0x001b, code lost:
            return false;
         */
        /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
        @Override // android.content.SharedPreferences.Editor
        public boolean commit() {
            MemoryCommitResult mcr = commitToMemory();
            SharedPreferencesImpl.this.enqueueDiskWrite(mcr, null);
            mcr.writtenToDiskLatch.await();
            lambda$notifyListeners$0$SharedPreferencesImpl$EditorImpl(mcr);
            return mcr.writeToDiskResult;
        }

        /* access modifiers changed from: private */
        /* renamed from: notifyListeners */
        public void lambda$notifyListeners$0$SharedPreferencesImpl$EditorImpl(MemoryCommitResult mcr) {
            if (!(mcr.listeners == null || mcr.keysModified == null || mcr.keysModified.size() == 0)) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    for (int i = mcr.keysModified.size() - 1; i >= 0; i--) {
                        String key = mcr.keysModified.get(i);
                        for (SharedPreferences.OnSharedPreferenceChangeListener listener : mcr.listeners) {
                            if (listener != null) {
                                listener.onSharedPreferenceChanged(SharedPreferencesImpl.this, key);
                            }
                        }
                    }
                    return;
                }
                ActivityThread.sMainThreadHandler.post(new Runnable(mcr) {
                    /* class android.app.$$Lambda$SharedPreferencesImpl$EditorImpl$3CAjkhzA131V3VsLfP2uy0FWZ0 */
                    private final /* synthetic */ SharedPreferencesImpl.MemoryCommitResult f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        SharedPreferencesImpl.EditorImpl.this.lambda$notifyListeners$0$SharedPreferencesImpl$EditorImpl(this.f$1);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void enqueueDiskWrite(final MemoryCommitResult mcr, final Runnable postWriteRunnable) {
        boolean wasEmpty;
        boolean z = false;
        final boolean isFromSyncCommit = postWriteRunnable == null;
        Runnable writeToDiskRunnable = new Runnable() {
            /* class android.app.SharedPreferencesImpl.AnonymousClass2 */

            public void run() {
                synchronized (SharedPreferencesImpl.this.mWritingToDiskLock) {
                    SharedPreferencesImpl.this.writeToFile(mcr, isFromSyncCommit);
                }
                synchronized (SharedPreferencesImpl.this.mLock) {
                    SharedPreferencesImpl.access$310(SharedPreferencesImpl.this);
                }
                Runnable runnable = postWriteRunnable;
                if (runnable != null) {
                    runnable.run();
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
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            File parent = file.getParentFile();
            if (!parent.mkdir()) {
                Log.e(TAG, "Couldn't create directory for SharedPreferences file " + file);
                return null;
            }
            FileUtils.setPermissions(parent.getPath(), 505, -1, -1);
            try {
                return new FileOutputStream(file);
            } catch (FileNotFoundException e2) {
                Log.e(TAG, "Couldn't create SharedPreferences file " + file, e2);
                return null;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    @GuardedBy({"mWritingToDiskLock"})
    private void writeToFile(MemoryCommitResult mcr, boolean isFromSyncCommit) {
        if (this.mFile.exists()) {
            boolean needsWrite = false;
            if (this.mDiskStateGeneration < mcr.memoryStateGeneration) {
                if (isFromSyncCommit) {
                    needsWrite = true;
                } else {
                    synchronized (this.mLock) {
                        try {
                            if (this.mCurrentMemoryStateGeneration == mcr.memoryStateGeneration) {
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
            FileOutputStream str = createFileOutputStream(this.mFile);
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
                    this.mStatTimestamp = stat.st_mtim;
                    this.mStatSize = stat.st_size;
                }
            } catch (ErrnoException e) {
            }
            this.mBackupFile.delete();
            this.mDiskStateGeneration = mcr.memoryStateGeneration;
            mcr.setDiskWriteResult(true, true);
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
            mcr.setDiskWriteResult(false, false);
        } catch (IOException e3) {
            Log.w(TAG, "writeToFile: Got exception:", e3);
            Log.e(TAG, "Couldn't clean up partially-written file " + this.mFile);
            mcr.setDiskWriteResult(false, false);
        }
    }
}
