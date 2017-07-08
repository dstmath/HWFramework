package android.app;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.FileUtils;
import android.os.Looper;
import android.os.Process;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;
import com.android.internal.util.XmlUtils;
import com.google.android.collect.Maps;
import dalvik.system.BlockGuard;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParserException;

final class SharedPreferencesImpl implements SharedPreferences {
    private static final boolean DEBUG = false;
    private static final String TAG = "SharedPreferencesImpl";
    private static final Object mContent = null;
    private final File mBackupFile;
    private int mDiskWritesInFlight;
    private final File mFile;
    private final WeakHashMap<OnSharedPreferenceChangeListener, Object> mListeners;
    private boolean mLoaded;
    private Map<String, Object> mMap;
    private int mMode;
    private long mStatSize;
    private long mStatTimestamp;
    private final Object mWritingToDiskLock;

    /* renamed from: android.app.SharedPreferencesImpl.1 */
    class AnonymousClass1 extends Thread {
        AnonymousClass1(String $anonymous0) {
            super($anonymous0);
        }

        public void run() {
            SharedPreferencesImpl.this.loadFromDisk();
        }
    }

    /* renamed from: android.app.SharedPreferencesImpl.2 */
    class AnonymousClass2 implements Runnable {
        final /* synthetic */ MemoryCommitResult val$mcr;
        final /* synthetic */ Runnable val$postWriteRunnable;

        AnonymousClass2(MemoryCommitResult val$mcr, Runnable val$postWriteRunnable) {
            this.val$mcr = val$mcr;
            this.val$postWriteRunnable = val$postWriteRunnable;
        }

        public void run() {
            synchronized (SharedPreferencesImpl.this.mWritingToDiskLock) {
                SharedPreferencesImpl.this.writeToFile(this.val$mcr);
            }
            synchronized (SharedPreferencesImpl.this) {
                SharedPreferencesImpl sharedPreferencesImpl = SharedPreferencesImpl.this;
                sharedPreferencesImpl.mDiskWritesInFlight = sharedPreferencesImpl.mDiskWritesInFlight - 1;
            }
            if (this.val$postWriteRunnable != null) {
                this.val$postWriteRunnable.run();
            }
        }
    }

    public final class EditorImpl implements Editor {
        private boolean mClear;
        private final Map<String, Object> mModified;

        /* renamed from: android.app.SharedPreferencesImpl.EditorImpl.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ MemoryCommitResult val$mcr;

            AnonymousClass1(MemoryCommitResult val$mcr) {
                this.val$mcr = val$mcr;
            }

            public void run() {
                try {
                    this.val$mcr.writtenToDiskLatch.await();
                } catch (InterruptedException e) {
                }
            }
        }

        /* renamed from: android.app.SharedPreferencesImpl.EditorImpl.2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ Runnable val$awaitCommit;

            AnonymousClass2(Runnable val$awaitCommit) {
                this.val$awaitCommit = val$awaitCommit;
            }

            public void run() {
                this.val$awaitCommit.run();
                QueuedWork.remove(this.val$awaitCommit);
            }
        }

        /* renamed from: android.app.SharedPreferencesImpl.EditorImpl.3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ MemoryCommitResult val$mcr;

            AnonymousClass3(MemoryCommitResult val$mcr) {
                this.val$mcr = val$mcr;
            }

            public void run() {
                EditorImpl.this.notifyListeners(this.val$mcr);
            }
        }

        public EditorImpl() {
            this.mModified = Maps.newHashMap();
            this.mClear = SharedPreferencesImpl.DEBUG;
        }

        public Editor putString(String key, String value) {
            synchronized (this) {
                this.mModified.put(key, value);
            }
            return this;
        }

        public Editor putStringSet(String key, Set<String> values) {
            Object obj = null;
            synchronized (this) {
                Map map = this.mModified;
                if (values != null) {
                    obj = new HashSet(values);
                }
                map.put(key, obj);
            }
            return this;
        }

        public Editor putInt(String key, int value) {
            synchronized (this) {
                this.mModified.put(key, Integer.valueOf(value));
            }
            return this;
        }

        public Editor putLong(String key, long value) {
            synchronized (this) {
                this.mModified.put(key, Long.valueOf(value));
            }
            return this;
        }

        public Editor putFloat(String key, float value) {
            synchronized (this) {
                this.mModified.put(key, Float.valueOf(value));
            }
            return this;
        }

        public Editor putBoolean(String key, boolean value) {
            synchronized (this) {
                this.mModified.put(key, Boolean.valueOf(value));
            }
            return this;
        }

        public Editor remove(String key) {
            synchronized (this) {
                this.mModified.put(key, this);
            }
            return this;
        }

        public Editor clear() {
            synchronized (this) {
                this.mClear = true;
            }
            return this;
        }

        public void apply() {
            MemoryCommitResult mcr = commitToMemory();
            Runnable awaitCommit = new AnonymousClass1(mcr);
            QueuedWork.add(awaitCommit);
            SharedPreferencesImpl.this.enqueueDiskWrite(mcr, new AnonymousClass2(awaitCommit));
            notifyListeners(mcr);
        }

        private MemoryCommitResult commitToMemory() {
            boolean hasListeners = true;
            MemoryCommitResult mcr = new MemoryCommitResult();
            synchronized (SharedPreferencesImpl.this) {
                if (SharedPreferencesImpl.this.mDiskWritesInFlight > 0) {
                    SharedPreferencesImpl.this.mMap = new HashMap(SharedPreferencesImpl.this.mMap);
                }
                mcr.mapToWriteToDisk = SharedPreferencesImpl.this.mMap;
                SharedPreferencesImpl sharedPreferencesImpl = SharedPreferencesImpl.this;
                sharedPreferencesImpl.mDiskWritesInFlight = sharedPreferencesImpl.mDiskWritesInFlight + 1;
                if (SharedPreferencesImpl.this.mListeners.size() <= 0) {
                    hasListeners = SharedPreferencesImpl.DEBUG;
                }
                if (hasListeners) {
                    mcr.keysModified = new ArrayList();
                    mcr.listeners = new HashSet(SharedPreferencesImpl.this.mListeners.keySet());
                }
                synchronized (this) {
                    if (this.mClear) {
                        if (!SharedPreferencesImpl.this.mMap.isEmpty()) {
                            mcr.changesMade = true;
                            SharedPreferencesImpl.this.mMap.clear();
                        }
                        this.mClear = SharedPreferencesImpl.DEBUG;
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
                        mcr.changesMade = true;
                        if (hasListeners) {
                            mcr.keysModified.add(k);
                        }
                    }
                    this.mModified.clear();
                }
            }
            return mcr;
        }

        public boolean commit() {
            MemoryCommitResult mcr = commitToMemory();
            SharedPreferencesImpl.this.enqueueDiskWrite(mcr, null);
            try {
                mcr.writtenToDiskLatch.await();
                notifyListeners(mcr);
                return mcr.writeToDiskResult;
            } catch (InterruptedException e) {
                return SharedPreferencesImpl.DEBUG;
            }
        }

        private void notifyListeners(MemoryCommitResult mcr) {
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
                    ActivityThread.sMainThreadHandler.post(new AnonymousClass3(mcr));
                }
            }
        }
    }

    private static class MemoryCommitResult {
        public boolean changesMade;
        public List<String> keysModified;
        public Set<OnSharedPreferenceChangeListener> listeners;
        public Map<?, ?> mapToWriteToDisk;
        public volatile boolean writeToDiskResult;
        public final CountDownLatch writtenToDiskLatch;

        private MemoryCommitResult() {
            this.writtenToDiskLatch = new CountDownLatch(1);
            this.writeToDiskResult = SharedPreferencesImpl.DEBUG;
        }

        public void setDiskWriteResult(boolean result) {
            this.writeToDiskResult = result;
            this.writtenToDiskLatch.countDown();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.SharedPreferencesImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.SharedPreferencesImpl.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.SharedPreferencesImpl.<clinit>():void");
    }

    SharedPreferencesImpl(File file, int mode) {
        this.mDiskWritesInFlight = 0;
        this.mLoaded = DEBUG;
        this.mWritingToDiskLock = new Object();
        this.mListeners = new WeakHashMap();
        this.mFile = file;
        this.mBackupFile = makeBackupFile(file);
        this.mMode = mode;
        this.mLoaded = DEBUG;
        this.mMap = null;
        startLoadFromDisk();
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void awaitLoaded() {
        synchronized (this) {
            while (true) {
                if (this.mLoaded) {
                } else {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    private void startLoadFromDisk() {
        synchronized (this) {
            this.mLoaded = DEBUG;
        }
        new AnonymousClass1("SharedPreferencesImpl-load").start();
    }

    private void loadFromDisk() {
        Exception e;
        Object obj;
        Throwable th;
        synchronized (this) {
            if (this.mLoaded) {
                return;
            }
            if (this.mBackupFile.exists()) {
                this.mFile.delete();
                this.mBackupFile.renameTo(this.mFile);
            }
            if (this.mFile.exists() && !this.mFile.canRead()) {
                Log.w(TAG, "Attempt to read preferences file " + this.mFile + " without permission");
            }
            Map map = null;
            StructStat structStat = null;
            try {
                structStat = Os.stat(this.mFile.getPath());
                if (this.mFile.canRead()) {
                    AutoCloseable autoCloseable = null;
                    try {
                        BufferedInputStream str = new BufferedInputStream(new FileInputStream(this.mFile), Process.PROC_OUT_FLOAT);
                        try {
                            map = XmlUtils.readMapXml(str);
                            IoUtils.closeQuietly(str);
                        } catch (XmlPullParserException e2) {
                            e = e2;
                            obj = str;
                            Log.w(TAG, "getSharedPreferences", e);
                            IoUtils.closeQuietly(autoCloseable);
                            synchronized (this) {
                                this.mLoaded = true;
                                if (map != null) {
                                    this.mMap = map;
                                    this.mStatTimestamp = structStat.st_mtime;
                                    this.mStatSize = structStat.st_size;
                                } else {
                                    this.mMap = new HashMap();
                                }
                                notifyAll();
                            }
                        } catch (Exception e3) {
                            e = e3;
                            obj = str;
                            try {
                                Log.w(TAG, "getSharedPreferences", e);
                                IoUtils.closeQuietly(autoCloseable);
                                synchronized (this) {
                                    this.mLoaded = true;
                                    if (map != null) {
                                        this.mMap = new HashMap();
                                    } else {
                                        this.mMap = map;
                                        this.mStatTimestamp = structStat.st_mtime;
                                        this.mStatSize = structStat.st_size;
                                    }
                                    notifyAll();
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                IoUtils.closeQuietly(autoCloseable);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            obj = str;
                            IoUtils.closeQuietly(autoCloseable);
                            throw th;
                        }
                    } catch (XmlPullParserException e4) {
                        e = e4;
                        Log.w(TAG, "getSharedPreferences", e);
                        IoUtils.closeQuietly(autoCloseable);
                        synchronized (this) {
                            this.mLoaded = true;
                            if (map != null) {
                                this.mMap = new HashMap();
                            } else {
                                this.mMap = map;
                                this.mStatTimestamp = structStat.st_mtime;
                                this.mStatSize = structStat.st_size;
                            }
                            notifyAll();
                        }
                    } catch (Exception e5) {
                        e = e5;
                        Log.w(TAG, "getSharedPreferences", e);
                        IoUtils.closeQuietly(autoCloseable);
                        synchronized (this) {
                            this.mLoaded = true;
                            if (map != null) {
                                this.mMap = map;
                                this.mStatTimestamp = structStat.st_mtime;
                                this.mStatSize = structStat.st_size;
                            } else {
                                this.mMap = new HashMap();
                            }
                            notifyAll();
                        }
                    }
                }
            } catch (ErrnoException e6) {
            }
            synchronized (this) {
                this.mLoaded = true;
                if (map != null) {
                    this.mMap = map;
                    this.mStatTimestamp = structStat.st_mtime;
                    this.mStatSize = structStat.st_size;
                } else {
                    this.mMap = new HashMap();
                }
                notifyAll();
            }
        }
    }

    static File makeBackupFile(File prefsFile) {
        return new File(prefsFile.getPath() + ".bak");
    }

    void startReloadIfChangedUnexpectedly() {
        synchronized (this) {
            if (hasFileChangedUnexpectedly()) {
                startLoadFromDisk();
                return;
            }
        }
    }

    private boolean hasFileChangedUnexpectedly() {
        boolean z = true;
        synchronized (this) {
            if (this.mDiskWritesInFlight > 0) {
                return DEBUG;
            }
            try {
                BlockGuard.getThreadPolicy().onReadFromDisk();
                StructStat stat = Os.stat(this.mFile.getPath());
                synchronized (this) {
                    if (this.mStatTimestamp == stat.st_mtime && this.mStatSize == stat.st_size) {
                        z = DEBUG;
                    }
                }
                return z;
            } catch (ErrnoException e) {
                return true;
            }
        }
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            this.mListeners.put(listener, mContent);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        synchronized (this) {
            this.mListeners.remove(listener);
        }
    }

    private void awaitLoadedLocked() {
        if (!this.mLoaded) {
            BlockGuard.getThreadPolicy().onReadFromDisk();
        }
        while (!this.mLoaded) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public Map<String, ?> getAll() {
        Map hashMap;
        synchronized (this) {
            awaitLoadedLocked();
            hashMap = new HashMap(this.mMap);
        }
        return hashMap;
    }

    public String getString(String key, String defValue) {
        String v;
        synchronized (this) {
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
        synchronized (this) {
            awaitLoadedLocked();
            v = (Set) this.mMap.get(key);
            if (v == null) {
                v = defValues;
            }
        }
        return v;
    }

    public int getInt(String key, int defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Integer v = (Integer) this.mMap.get(key);
            if (v != null) {
                defValue = v.intValue();
            }
        }
        return defValue;
    }

    public long getLong(String key, long defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Long v = (Long) this.mMap.get(key);
            if (v != null) {
                defValue = v.longValue();
            }
        }
        return defValue;
    }

    public float getFloat(String key, float defValue) {
        synchronized (this) {
            awaitLoadedLocked();
            Float v = (Float) this.mMap.get(key);
            if (v != null) {
                defValue = v.floatValue();
            }
        }
        return defValue;
    }

    public boolean getBoolean(String key, boolean defValue) {
        synchronized (this) {
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
        synchronized (this) {
            awaitLoadedLocked();
            containsKey = this.mMap.containsKey(key);
        }
        return containsKey;
    }

    public Editor edit() {
        synchronized (this) {
            awaitLoadedLocked();
        }
        return new EditorImpl();
    }

    private void enqueueDiskWrite(MemoryCommitResult mcr, Runnable postWriteRunnable) {
        Runnable writeToDiskRunnable = new AnonymousClass2(mcr, postWriteRunnable);
        if (postWriteRunnable == null ? true : DEBUG) {
            boolean wasEmpty;
            synchronized (this) {
                wasEmpty = this.mDiskWritesInFlight == 1 ? true : DEBUG;
            }
            if (wasEmpty) {
                writeToDiskRunnable.run();
                return;
            }
        }
        QueuedWork.singleThreadExecutor().execute(writeToDiskRunnable);
    }

    private static FileOutputStream createFileOutputStream(File file) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            File parent = file.getParentFile();
            if (parent.mkdir()) {
                FileUtils.setPermissions(parent.getPath(), (int) IActivityManager.IS_PACKAGE_CLONED_TRANSACTION, -1, -1);
                try {
                    fileOutputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e2) {
                    Log.e(TAG, "Couldn't create SharedPreferences file " + file, e2);
                }
            } else {
                Log.e(TAG, "Couldn't create directory for SharedPreferences file " + file);
                return null;
            }
        }
        return fileOutputStream;
    }

    private void writeToFile(MemoryCommitResult mcr) {
        if (this.mFile.exists()) {
            if (!mcr.changesMade) {
                mcr.setDiskWriteResult(true);
                return;
            } else if (this.mBackupFile.exists()) {
                this.mFile.delete();
            } else if (!this.mFile.renameTo(this.mBackupFile)) {
                Log.e(TAG, "Couldn't rename file " + this.mFile + " to backup file " + this.mBackupFile);
                mcr.setDiskWriteResult(DEBUG);
                return;
            }
        }
        try {
            FileOutputStream str = createFileOutputStream(this.mFile);
            if (str == null) {
                mcr.setDiskWriteResult(DEBUG);
                return;
            }
            XmlUtils.writeMapXml(mcr.mapToWriteToDisk, str);
            FileUtils.sync(str);
            str.close();
            ContextImpl.setFilePermissionsFromMode(this.mFile.getPath(), this.mMode, 0);
            try {
                StructStat stat = Os.stat(this.mFile.getPath());
                synchronized (this) {
                    this.mStatTimestamp = stat.st_mtime;
                    this.mStatSize = stat.st_size;
                }
            } catch (ErrnoException e) {
            }
            this.mBackupFile.delete();
            mcr.setDiskWriteResult(true);
        } catch (XmlPullParserException e2) {
            Log.w(TAG, "writeToFile: Got exception:", e2);
            if (this.mFile.exists() && !this.mFile.delete()) {
                Log.e(TAG, "Couldn't clean up partially-written file " + this.mFile);
            }
            mcr.setDiskWriteResult(DEBUG);
        } catch (IOException e3) {
            Log.w(TAG, "writeToFile: Got exception:", e3);
            Log.e(TAG, "Couldn't clean up partially-written file " + this.mFile);
            mcr.setDiskWriteResult(DEBUG);
        }
    }
}
