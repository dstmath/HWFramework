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
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static ConcurrentHashMap<FileKey, List<FileLockReference>> lockMap;
    private static ReferenceQueue<FileLock> queue;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.ch.SharedFileLockTable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.ch.SharedFileLockTable.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.ch.SharedFileLockTable.<clinit>():void");
    }

    SharedFileLockTable(Channel channel, FileDescriptor fd) throws IOException {
        this.channel = channel;
        this.fileKey = FileKey.create(fd);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                        break;
                    }
                }
                list = prev;
            }
            synchronized (list) {
                List<FileLockReference> current = (List) lockMap.get(this.fileKey);
                if (list == current) {
                    break;
                }
                list2 = current;
            }
            list = list2;
        }
        list.add(new FileLockReference(fl, queue, this.fileKey));
        removeStaleEntries();
    }

    private void removeKeyIfEmpty(FileKey fk, List<FileLockReference> list) {
        if (-assertionsDisabled || Thread.holdsLock(list)) {
            if (!-assertionsDisabled) {
                if ((lockMap.get(fk) == list ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            if (list.isEmpty()) {
                lockMap.remove(fk);
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void remove(FileLock fl) {
        Object obj = 1;
        if (!-assertionsDisabled) {
            if ((fl != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (true) {
                    if (index >= list.size()) {
                        break;
                    }
                    FileLockReference ref = (FileLockReference) list.get(index);
                    FileLock lock = (FileLock) ref.get();
                    if (lock == fl) {
                        break;
                    }
                    index++;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<FileLock> removeAll() {
        List<FileLock> result = new ArrayList();
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (true) {
                    if (index < list.size()) {
                        FileLockReference ref = (FileLockReference) list.get(index);
                        FileLock lock = (FileLock) ref.get();
                        if (lock == null || lock.acquiredBy() != this.channel) {
                            index++;
                        } else {
                            ref.clear();
                            list.remove(index);
                            result.add(lock);
                        }
                    } else {
                        removeKeyIfEmpty(this.fileKey, list);
                    }
                }
            }
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void replace(FileLock fromLock, FileLock toLock) {
        List<FileLockReference> list = (List) lockMap.get(this.fileKey);
        if (!-assertionsDisabled) {
            if ((list != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        synchronized (list) {
            FileLockReference ref;
            int index = 0;
            while (true) {
                if (index >= list.size()) {
                    break;
                }
                ref = (FileLockReference) list.get(index);
                if (((FileLock) ref.get()) == fromLock) {
                    break;
                }
                index++;
            }
            ref.clear();
            list.set(index, new FileLockReference(toLock, queue, this.fileKey));
        }
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
