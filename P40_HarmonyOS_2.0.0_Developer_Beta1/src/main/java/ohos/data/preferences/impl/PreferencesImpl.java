package ohos.data.preferences.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import ohos.data.preferences.Preferences;
import ohos.data.preferences.PreferencesFileBrokenException;
import ohos.data.preferences.PreferencesFileReadException;
import ohos.data.preferences.PreferencesFileWriteException;
import ohos.data.utils.ExecutorUtils;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class PreferencesImpl implements Preferences {
    private static final int BLOCK_QUEUE_SIZE = 1000;
    private static final int CORE_POOL_SIZE = 0;
    private static final int KEEP_ALIVE_SECONDS = 3;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "PreferencesImpl");
    private static final int MAXIMUM_POOL_SIZE = 1;
    private boolean isFileBroken = false;
    private final File mBackupFile;
    private final File mBrokenFile;
    private long mCurrentMemoryStateGeneration;
    private final ExecutorService mDiskReadExecutor;
    private long mDiskStateGeneration;
    private final ExecutorService mDiskWriteExecutor;
    private final File mFile;
    private boolean mLoaded = false;
    private final Object mLock = new Object();
    private HashMap<String, Object> mMap = new HashMap<>();
    private final Set<Preferences.PreferencesObserver> mObservers;
    private final Object mWritingToDiskLock = new Object();
    private List<String> modifiedKeys = new ArrayList();

    public PreferencesImpl(File file) {
        this.mFile = file;
        this.mBackupFile = makeBackupFile(this.mFile);
        this.mBrokenFile = makeBrokenFile(this.mFile);
        this.mObservers = Collections.newSetFromMap(new WeakHashMap());
        this.mDiskWriteExecutor = ExecutorUtils.getExecutorService("PreferencesImplWrite#", 0, 1, 3, null);
        this.mDiskReadExecutor = ExecutorUtils.getExecutorService("PreferencesImplRead#", 0, 1, 3, 1000);
        startLoadFromDisk();
    }

    static File makeBackupFile(File file) {
        return new File(file.getPath() + ".bak");
    }

    static File makeBrokenFile(File file) {
        return new File(file.getPath() + ".broken");
    }

    @Override // ohos.data.preferences.Preferences
    public int getInt(String str, int i) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (!(obj instanceof Integer)) {
                return i;
            }
            return ((Integer) obj).intValue();
        }
    }

    @Override // ohos.data.preferences.Preferences
    public String getString(String str, String str2) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (!(obj instanceof String)) {
                return str2;
            }
            return (String) obj;
        }
    }

    @Override // ohos.data.preferences.Preferences
    public boolean getBoolean(String str, boolean z) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (!(obj instanceof Boolean)) {
                return z;
            }
            return ((Boolean) obj).booleanValue();
        }
    }

    @Override // ohos.data.preferences.Preferences
    public float getFloat(String str, float f) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (!(obj instanceof Float)) {
                return f;
            }
            return ((Float) obj).floatValue();
        }
    }

    @Override // ohos.data.preferences.Preferences
    public long getLong(String str, long j) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (!(obj instanceof Long)) {
                return j;
            }
            return ((Long) obj).longValue();
        }
    }

    @Override // ohos.data.preferences.Preferences
    public Set<String> getStringSet(String str, Set<String> set) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (!(obj instanceof Set)) {
                return set;
            }
            return (Set) obj;
        }
    }

    @Override // ohos.data.preferences.Preferences
    public Map<String, ?> getAll() {
        HashMap hashMap;
        synchronized (this.mLock) {
            awaitLoadFile();
            hashMap = new HashMap(this.mMap);
        }
        return hashMap;
    }

    @Override // ohos.data.preferences.Preferences
    public boolean hasKey(String str) {
        boolean containsKey;
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            containsKey = this.mMap.containsKey(str);
        }
        return containsKey;
    }

    private void checkKey(String str) {
        if (str == null) {
            throw new IllegalArgumentException("key is null.");
        } else if (str.isEmpty()) {
            throw new IllegalArgumentException("key is empty.");
        } else if (str.length() > 80) {
            throw new IllegalArgumentException("the length of key should be less than 80characters.");
        }
    }

    private void startLoadFromDisk() {
        synchronized (this.mLock) {
            this.mLoaded = false;
            this.isFileBroken = false;
        }
        this.mDiskReadExecutor.execute(new Runnable() {
            /* class ohos.data.preferences.impl.$$Lambda$PreferencesImpl$y2yLcPI1LWeUIcETKKygyj67jes */

            @Override // java.lang.Runnable
            public final void run() {
                PreferencesImpl.lambda$y2yLcPI1LWeUIcETKKygyj67jes(PreferencesImpl.this);
            }
        });
    }

    /* access modifiers changed from: private */
    public void loadFromDisk() {
        synchronized (this.mLock) {
            if (!this.mLoaded) {
                checkBackupFile();
            } else {
                return;
            }
        }
        HashMap<String, Object> hashMap = null;
        if (this.mFile.canRead()) {
            try {
                hashMap = PreferencesXmlUtils.readSettingXml(this.mFile.getCanonicalPath());
            } catch (IOException unused) {
                HiLog.error(LABEL, "loadFromDisk IOException when get canonical path:%{public}s", new Object[]{this.mFile.getName()});
            } catch (PreferencesFileReadException e) {
                HiLog.error(LABEL, "loadFromDisk exception:%{public}s", new Object[]{e.getMessage()});
                generateBrokenFile();
            }
        }
        synchronized (this.mLock) {
            if (hashMap != null) {
                this.mMap = hashMap;
            }
            this.mLoaded = true;
            this.mLock.notifyAll();
        }
    }

    private void checkBackupFile() {
        if (this.mBackupFile.exists()) {
            if (!this.mFile.delete()) {
                HiLog.error(LABEL, "Couldn't delete file %{public}s when loadFromDisk.", new Object[]{this.mFile.getName()});
            }
            if (this.mBackupFile.renameTo(this.mFile)) {
                PreferencesXmlUtils.limitFilePermission(this.mFile);
            } else {
                HiLog.error(LABEL, "Couldn't rename backup file %{public}s to file %{public}s when loadFromDisk and backup exist.", new Object[]{this.mBackupFile.getName(), this.mFile.getName()});
            }
        }
    }

    private void generateBrokenFile() {
        this.isFileBroken = true;
        if (this.mBrokenFile.exists() && !this.mBrokenFile.delete()) {
            HiLog.error(LABEL, "Couldn't delete mBrokenFile %{public}s when loadFromDisk.", new Object[]{this.mBrokenFile.getName()});
        } else if (this.mFile.renameTo(this.mBrokenFile)) {
            PreferencesXmlUtils.limitFilePermission(this.mBrokenFile);
        } else {
            HiLog.error(LABEL, "Couldn't rename file %{public}s to broken file %{public}s when loadFromDisk.", new Object[]{this.mFile.getName(), this.mBrokenFile.getName()});
        }
    }

    private void awaitLoadFile() {
        while (!this.mLoaded) {
            try {
                this.mLock.wait();
            } catch (InterruptedException e) {
                HiLog.error(LABEL, "awaitLoadFile exception:%{public}s", new Object[]{e.getMessage()});
            }
        }
        if (this.isFileBroken) {
            throw new PreferencesFileBrokenException("Preferences file is broken.");
        }
    }

    @Override // ohos.data.preferences.Preferences
    public Preferences putInt(String str, int i) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (obj != null && obj.equals(Integer.valueOf(i))) {
                return this;
            }
            this.mMap.put(str, Integer.valueOf(i));
            this.modifiedKeys.add(str);
            return this;
        }
    }

    @Override // ohos.data.preferences.Preferences
    public Preferences putBoolean(String str, boolean z) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (obj != null && obj.equals(Boolean.valueOf(z))) {
                return this;
            }
            this.mMap.put(str, Boolean.valueOf(z));
            this.modifiedKeys.add(str);
            return this;
        }
    }

    @Override // ohos.data.preferences.Preferences
    public Preferences putLong(String str, long j) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (obj != null && obj.equals(Long.valueOf(j))) {
                return this;
            }
            this.mMap.put(str, Long.valueOf(j));
            this.modifiedKeys.add(str);
            return this;
        }
    }

    @Override // ohos.data.preferences.Preferences
    public Preferences putString(String str, String str2) {
        checkKey(str);
        if (str2 == null || str2.length() <= 8192) {
            synchronized (this.mLock) {
                awaitLoadFile();
                if (equals(this.mMap.get(str), str2)) {
                    return this;
                }
                this.mMap.put(str, str2);
                this.modifiedKeys.add(str);
                return this;
            }
        }
        throw new IllegalArgumentException("The length of string value should be less than 8192characters.");
    }

    @Override // ohos.data.preferences.Preferences
    public Preferences putStringSet(String str, Set<String> set) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            if (equals(this.mMap.get(str), set)) {
                return this;
            }
            this.mMap.put(str, set);
            this.modifiedKeys.add(str);
            return this;
        }
    }

    private boolean equals(Object obj, Object obj2) {
        if (obj == null) {
            return obj2 == null;
        }
        return obj.equals(obj2);
    }

    @Override // ohos.data.preferences.Preferences
    public Preferences putFloat(String str, float f) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            Object obj = this.mMap.get(str);
            if (obj != null && obj.equals(Float.valueOf(f))) {
                return this;
            }
            this.mMap.put(str, Float.valueOf(f));
            this.modifiedKeys.add(str);
            return this;
        }
    }

    @Override // ohos.data.preferences.Preferences
    public Preferences clear() {
        synchronized (this.mLock) {
            awaitLoadFile();
            if (!this.mMap.isEmpty()) {
                this.modifiedKeys.addAll(this.mMap.keySet());
                this.mMap.clear();
            }
        }
        return this;
    }

    @Override // ohos.data.preferences.Preferences
    public Preferences delete(String str) {
        checkKey(str);
        synchronized (this.mLock) {
            awaitLoadFile();
            if (this.mMap.remove(str) != null) {
                this.modifiedKeys.add(str);
            }
        }
        return this;
    }

    @Override // ohos.data.preferences.Preferences
    public void flush() {
        MemoryToDiskRequest commitToMemory = commitToMemory();
        commitToMemory.isSyncRequest = false;
        this.mDiskWriteExecutor.execute(commitToMemory);
        notifyObservers(commitToMemory);
    }

    @Override // ohos.data.preferences.Preferences
    public boolean flushSync() {
        MemoryToDiskRequest commitToMemory = commitToMemory();
        commitToMemory.isSyncRequest = true;
        try {
            this.mDiskWriteExecutor.execute(commitToMemory);
            commitToMemory.writtenToDiskLatch.await();
            if (commitToMemory.wasWritten) {
                HiLog.debug(LABEL, "%{public}s:%{public}s written", new Object[]{this.mFile.getName(), Long.valueOf(commitToMemory.memoryStateGeneration)});
            }
        } catch (InterruptedException e) {
            HiLog.error(LABEL, "flushSync exception:%{public}s", new Object[]{e.getMessage()});
        }
        notifyObservers(commitToMemory);
        return commitToMemory.writeToDiskResult;
    }

    private MemoryToDiskRequest commitToMemory() {
        MemoryToDiskRequest memoryToDiskRequest;
        synchronized (this.mLock) {
            awaitLoadFile();
            HashMap hashMap = new HashMap(this.mMap);
            HashSet hashSet = new HashSet(this.mObservers);
            ArrayList arrayList = new ArrayList();
            if (!this.modifiedKeys.isEmpty()) {
                this.mCurrentMemoryStateGeneration++;
                arrayList.addAll(this.modifiedKeys);
                this.modifiedKeys.clear();
            }
            memoryToDiskRequest = new MemoryToDiskRequest(this.mCurrentMemoryStateGeneration, hashMap, arrayList, hashSet);
        }
        return memoryToDiskRequest;
    }

    private void notifyObservers(MemoryToDiskRequest memoryToDiskRequest) {
        if (!(memoryToDiskRequest.observers == null || memoryToDiskRequest.keysModified == null || memoryToDiskRequest.keysModified.size() == 0)) {
            for (int size = memoryToDiskRequest.keysModified.size() - 1; size >= 0; size--) {
                String str = memoryToDiskRequest.keysModified.get(size);
                for (Preferences.PreferencesObserver preferencesObserver : memoryToDiskRequest.observers) {
                    if (preferencesObserver != null) {
                        preferencesObserver.onChange(this, str);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class MemoryToDiskRequest implements Runnable {
        boolean isSyncRequest;
        List<String> keysModified;
        final HashMap<String, Object> mapToWriteToDisk;
        final long memoryStateGeneration;
        Set<Preferences.PreferencesObserver> observers;
        boolean wasWritten = false;
        volatile boolean writeToDiskResult = false;
        final CountDownLatch writtenToDiskLatch = new CountDownLatch(1);

        public MemoryToDiskRequest(long j, HashMap<String, Object> hashMap, List<String> list, Set<Preferences.PreferencesObserver> set) {
            this.memoryStateGeneration = j;
            this.mapToWriteToDisk = hashMap;
            this.keysModified = list;
            this.observers = set;
        }

        /* access modifiers changed from: package-private */
        public void setDiskWriteResult(boolean z, boolean z2) {
            this.wasWritten = z;
            this.writeToDiskResult = z2;
            this.writtenToDiskLatch.countDown();
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (PreferencesImpl.this.mWritingToDiskLock) {
                PreferencesImpl.this.writeToFileLocked(this);
            }
        }
    }

    private boolean checkRequestValidForStateGeneration(MemoryToDiskRequest memoryToDiskRequest) {
        boolean z;
        synchronized (this.mWritingToDiskLock) {
            z = true;
            if (this.mDiskStateGeneration >= memoryToDiskRequest.memoryStateGeneration) {
                z = false;
            } else if (!memoryToDiskRequest.isSyncRequest) {
                synchronized (this.mLock) {
                    if (this.mCurrentMemoryStateGeneration != memoryToDiskRequest.memoryStateGeneration) {
                        z = false;
                    }
                }
            }
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeToFileLocked(MemoryToDiskRequest memoryToDiskRequest) {
        if (this.mFile.exists()) {
            if (!checkRequestValidForStateGeneration(memoryToDiskRequest)) {
                memoryToDiskRequest.setDiskWriteResult(false, true);
                return;
            } else if (this.mBackupFile.exists()) {
                if (!this.mFile.delete()) {
                    HiLog.error(LABEL, "Couldn't delete file %{public}s when writeToFile and backup exist.", new Object[]{this.mFile.getName()});
                }
            } else if (this.mFile.renameTo(this.mBackupFile)) {
                PreferencesXmlUtils.limitFilePermission(this.mBackupFile);
            } else {
                HiLog.error(LABEL, "Couldn't rename file %{public}s to backup file %{public}s", new Object[]{this.mFile.getName(), this.mBackupFile.getName()});
                memoryToDiskRequest.setDiskWriteResult(false, false);
                return;
            }
        }
        try {
            PreferencesXmlUtils.writeSettingXml(memoryToDiskRequest.mapToWriteToDisk, this.mFile.getCanonicalPath());
            if (this.mBackupFile.exists() && !this.mBackupFile.delete()) {
                HiLog.error(LABEL, "Couldn't delete backup file %{public}s when writeToFile finish.", new Object[]{this.mBackupFile.getName()});
            }
            this.mDiskStateGeneration = memoryToDiskRequest.memoryStateGeneration;
            memoryToDiskRequest.setDiskWriteResult(true, true);
        } catch (IOException unused) {
            HiLog.error(LABEL, "writeToFileLocked IOException when get canonical pathname of file:%{public}s", new Object[]{this.mFile.getName()});
            if (this.mFile.exists() && !this.mFile.delete()) {
                HiLog.error(LABEL, "Couldn't clean up partially-written file %{public}s", new Object[]{this.mFile.getName()});
            }
            memoryToDiskRequest.setDiskWriteResult(false, false);
        } catch (PreferencesFileWriteException e) {
            HiLog.error(LABEL, "writeToFileLocked exception:%{public}s", new Object[]{e.getMessage()});
            HiLog.error(LABEL, "Couldn't clean up partially-written file %{public}s", new Object[]{this.mFile.getName()});
            memoryToDiskRequest.setDiskWriteResult(false, false);
        }
    }

    @Override // ohos.data.preferences.Preferences
    public void registerObserver(Preferences.PreferencesObserver preferencesObserver) {
        synchronized (this.mLock) {
            this.mObservers.add(preferencesObserver);
        }
    }

    @Override // ohos.data.preferences.Preferences
    public void unregisterObserver(Preferences.PreferencesObserver preferencesObserver) {
        synchronized (this.mLock) {
            this.mObservers.remove(preferencesObserver);
        }
    }
}
