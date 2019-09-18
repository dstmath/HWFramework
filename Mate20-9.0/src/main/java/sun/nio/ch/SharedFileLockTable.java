package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.channels.Channel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/* compiled from: FileLockTable */
class SharedFileLockTable extends FileLockTable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static ConcurrentHashMap<FileKey, List<FileLockReference>> lockMap = new ConcurrentHashMap<>();
    private static ReferenceQueue<FileLock> queue = new ReferenceQueue<>();
    private final Channel channel;
    private final FileKey fileKey;

    /* compiled from: FileLockTable */
    private static class FileLockReference extends WeakReference<FileLock> {
        private FileKey fileKey;

        FileLockReference(FileLock referent, ReferenceQueue<FileLock> queue, FileKey key) {
            super(referent, queue);
            this.fileKey = key;
        }

        /* access modifiers changed from: package-private */
        public FileKey fileKey() {
            return this.fileKey;
        }
    }

    SharedFileLockTable(Channel channel2, FileDescriptor fd) throws IOException {
        this.channel = channel2;
        this.fileKey = FileKey.create(fd);
    }

    public void add(FileLock fl) throws OverlappingFileLockException {
        List<FileLockReference> list;
        List<FileLockReference> prev = lockMap.get(this.fileKey);
        while (true) {
            if (prev == null) {
                List<FileLockReference> arrayList = new ArrayList<>(2);
                synchronized (arrayList) {
                    prev = lockMap.putIfAbsent(this.fileKey, arrayList);
                    if (prev == null) {
                        arrayList.add(new FileLockReference(fl, queue, this.fileKey));
                        List<FileLockReference> prev2 = arrayList;
                        break;
                    }
                }
            }
            synchronized (prev) {
                try {
                    list = lockMap.get(this.fileKey);
                    if (prev == list) {
                        checkList(prev, fl.position(), fl.size());
                        prev.add(new FileLockReference(fl, queue, this.fileKey));
                    } else {
                        try {
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            List<FileLockReference> list2 = list;
                            th = th2;
                            while (true) {
                                try {
                                    break;
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                            throw th;
                        }
                    }
                } catch (Throwable th4) {
                    th = th4;
                    List<FileLockReference> list3 = prev;
                    while (true) {
                        break;
                    }
                    throw th;
                }
            }
            prev = list;
        }
        removeStaleEntries();
    }

    private void removeKeyIfEmpty(FileKey fk, List<FileLockReference> list) {
        if (list.isEmpty()) {
            lockMap.remove(fk);
        }
    }

    public void remove(FileLock fl) {
        List<FileLockReference> list = lockMap.get(this.fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (true) {
                    if (index >= list.size()) {
                        break;
                    }
                    FileLockReference ref = list.get(index);
                    if (((FileLock) ref.get()) == fl) {
                        ref.clear();
                        list.remove(index);
                        break;
                    }
                    index++;
                }
            }
        }
    }

    public List<FileLock> removeAll() {
        List<FileLock> result = new ArrayList<>();
        List<FileLockReference> list = lockMap.get(this.fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (index < list.size()) {
                    FileLockReference ref = list.get(index);
                    FileLock lock = (FileLock) ref.get();
                    if (lock == null || lock.acquiredBy() != this.channel) {
                        index++;
                    } else {
                        ref.clear();
                        list.remove(index);
                        result.add(lock);
                    }
                }
                removeKeyIfEmpty(this.fileKey, list);
            }
        }
        return result;
    }

    public void replace(FileLock fromLock, FileLock toLock) {
        List<FileLockReference> list = lockMap.get(this.fileKey);
        synchronized (list) {
            int index = 0;
            while (true) {
                if (index >= list.size()) {
                    break;
                }
                FileLockReference ref = list.get(index);
                if (((FileLock) ref.get()) == fromLock) {
                    ref.clear();
                    list.set(index, new FileLockReference(toLock, queue, this.fileKey));
                    break;
                }
                index++;
            }
        }
    }

    private void checkList(List<FileLockReference> list, long position, long size) throws OverlappingFileLockException {
        for (FileLockReference ref : list) {
            FileLock fl = (FileLock) ref.get();
            if (fl != null && fl.overlaps(position, size)) {
                throw new OverlappingFileLockException();
            }
        }
    }

    private void removeStaleEntries() {
        while (true) {
            FileLockReference fileLockReference = (FileLockReference) queue.poll();
            FileLockReference ref = fileLockReference;
            if (fileLockReference != null) {
                FileKey fk = ref.fileKey();
                List<FileLockReference> list = lockMap.get(fk);
                if (list != null) {
                    synchronized (list) {
                        list.remove((Object) ref);
                        removeKeyIfEmpty(fk, list);
                    }
                }
            } else {
                return;
            }
        }
    }
}
