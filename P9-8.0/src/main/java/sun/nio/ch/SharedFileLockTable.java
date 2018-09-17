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
    static final /* synthetic */ boolean -assertionsDisabled = (SharedFileLockTable.class.desiredAssertionStatus() ^ 1);
    private static ConcurrentHashMap<FileKey, List<FileLockReference>> lockMap = new ConcurrentHashMap();
    private static ReferenceQueue<FileLock> queue = new ReferenceQueue();
    private final Channel channel;
    private final FileKey fileKey;

    /* compiled from: FileLockTable */
    private static class FileLockReference extends WeakReference<FileLock> {
        private FileKey fileKey;

        FileLockReference(FileLock referent, ReferenceQueue<FileLock> queue, FileKey key) {
            super(referent, queue);
            this.fileKey = key;
        }

        FileKey fileKey() {
            return this.fileKey;
        }
    }

    SharedFileLockTable(Channel channel, FileDescriptor fd) throws IOException {
        this.channel = channel;
        this.fileKey = FileKey.create(fd);
    }

    public void add(FileLock fl) throws OverlappingFileLockException {
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        while (true) {
            List<FileLockReference> list2;
            if (list == null) {
                List<FileLockReference> prev;
                list = new ArrayList(2);
                synchronized (list) {
                    prev = (List) lockMap.putIfAbsent(this.fileKey, list);
                    if (prev == null) {
                        list.-java_util_stream_Collectors-mthref-2(new FileLockReference(fl, queue, this.fileKey));
                        break;
                    }
                }
                list = prev;
            }
            synchronized (list) {
                List<FileLockReference> current = (List) lockMap.get(this.fileKey);
                if (list == current) {
                    checkList(list, fl.position(), fl.size());
                    list.-java_util_stream_Collectors-mthref-2(new FileLockReference(fl, queue, this.fileKey));
                    break;
                }
                list2 = current;
            }
            list = list2;
        }
        removeStaleEntries();
    }

    private void removeKeyIfEmpty(FileKey fk, List<FileLockReference> list) {
        if (!-assertionsDisabled && !Thread.holdsLock(list)) {
            throw new AssertionError();
        } else if (!-assertionsDisabled && lockMap.get(fk) != list) {
            throw new AssertionError();
        } else if (list.isEmpty()) {
            lockMap.remove(fk);
        }
    }

    public void remove(FileLock fl) {
        if (-assertionsDisabled || fl != null) {
            List<FileLockReference> list = (List) lockMap.get(this.fileKey);
            if (list != null) {
                synchronized (list) {
                    int index = 0;
                    while (index < list.size()) {
                        FileLockReference ref = (FileLockReference) list.get(index);
                        FileLock lock = (FileLock) ref.get();
                        if (lock != fl) {
                            index++;
                        } else if (-assertionsDisabled || (lock != null && lock.acquiredBy() == this.channel)) {
                            ref.clear();
                            list.remove(index);
                        } else {
                            throw new AssertionError();
                        }
                    }
                }
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    public List<FileLock> removeAll() {
        List<FileLock> result = new ArrayList();
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (index < list.size()) {
                    FileLockReference ref = (FileLockReference) list.get(index);
                    FileLock lock = (FileLock) ref.get();
                    if (lock == null || lock.acquiredBy() != this.channel) {
                        index++;
                    } else {
                        ref.clear();
                        list.remove(index);
                        result.-java_util_stream_Collectors-mthref-2(lock);
                    }
                }
                removeKeyIfEmpty(this.fileKey, list);
            }
        }
        return result;
    }

    public void replace(FileLock fromLock, FileLock toLock) {
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (-assertionsDisabled || list != null) {
            synchronized (list) {
                for (int index = 0; index < list.size(); index++) {
                    FileLockReference ref = (FileLockReference) list.get(index);
                    if (((FileLock) ref.get()) == fromLock) {
                        ref.clear();
                        list.set(index, new FileLockReference(toLock, queue, this.fileKey));
                        break;
                    }
                }
            }
            return;
        }
        throw new AssertionError();
    }

    private void checkList(List<FileLockReference> list, long position, long size) throws OverlappingFileLockException {
        if (-assertionsDisabled || Thread.holdsLock(list)) {
            for (FileLockReference ref : list) {
                FileLock fl = (FileLock) ref.get();
                if (fl != null && fl.overlaps(position, size)) {
                    throw new OverlappingFileLockException();
                }
            }
            return;
        }
        throw new AssertionError();
    }

    private void removeStaleEntries() {
        while (true) {
            Object ref = (FileLockReference) queue.poll();
            if (ref != null) {
                FileKey fk = ref.fileKey();
                List<FileLockReference> list = (List) lockMap.get(fk);
                if (list != null) {
                    synchronized (list) {
                        list.remove(ref);
                        removeKeyIfEmpty(fk, list);
                    }
                }
            } else {
                return;
            }
        }
    }
}
