package android.mtp;

import android.media.MediaFile;
import android.mtp.MtpStorageManager;
import android.os.FileObserver;
import android.os.storage.StorageVolume;
import android.util.Log;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MtpStorageManager {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int IN_IGNORED = 32768;
    private static final int IN_ISDIR = 1073741824;
    private static final int IN_ONLYDIR = 16777216;
    private static final int IN_Q_OVERFLOW = 16384;
    /* access modifiers changed from: private */
    public static final String TAG = MtpStorageManager.class.getSimpleName();
    private volatile boolean mCheckConsistency = false;
    private Thread mConsistencyThread = new Thread(new Runnable() {
        public final void run() {
            MtpStorageManager.lambda$new$0(MtpStorageManager.this);
        }
    });
    private MtpNotifier mMtpNotifier;
    private int mNextObjectId = 1;
    private int mNextStorageId = 1;
    private HashMap<Integer, MtpObject> mObjects = new HashMap<>();
    private HashMap<Integer, MtpObject> mRoots = new HashMap<>();
    private Set<String> mSubdirectories;

    public static abstract class MtpNotifier {
        public abstract void sendObjectAdded(int i);

        public abstract void sendObjectRemoved(int i);
    }

    public static class MtpObject {
        /* access modifiers changed from: private */
        public HashMap<String, MtpObject> mChildren;
        private int mId;
        private boolean mIsDir;
        private String mName;
        /* access modifiers changed from: private */
        public FileObserver mObserver = null;
        private MtpOperation mOp;
        private MtpObject mParent;
        private MtpObjectState mState = MtpObjectState.NORMAL;
        private int mStorageId;
        private boolean mVisited = false;

        MtpObject(String name, int id, MtpObject parent, boolean isDir) {
            this.mId = id;
            this.mName = name;
            this.mParent = parent;
            HashMap<String, MtpObject> hashMap = null;
            this.mIsDir = isDir;
            this.mOp = MtpOperation.NONE;
            this.mChildren = this.mIsDir ? new HashMap<>() : hashMap;
            if (parent != null) {
                this.mStorageId = parent.getStorageId();
            } else {
                this.mStorageId = id;
            }
        }

        public void setStorageId(int id) {
            this.mStorageId = id;
        }

        public String getName() {
            return this.mName;
        }

        public int getId() {
            return this.mId;
        }

        public boolean isDir() {
            return this.mIsDir;
        }

        public int getFormat() {
            return this.mIsDir ? MtpConstants.FORMAT_ASSOCIATION : MediaFile.getFormatCode(this.mName, null);
        }

        public int getStorageId() {
            return this.mStorageId;
        }

        public long getModifiedTime() {
            return getPath().toFile().lastModified() / 1000;
        }

        public MtpObject getParent() {
            return this.mParent;
        }

        public MtpObject getRoot() {
            return isRoot() ? this : this.mParent.getRoot();
        }

        public long getSize() {
            if (this.mIsDir) {
                return 0;
            }
            return getPath().toFile().length();
        }

        public Path getPath() {
            return isRoot() ? Paths.get(this.mName, new String[0]) : this.mParent.getPath().resolve(this.mName);
        }

        public boolean isRoot() {
            return this.mParent == null;
        }

        /* access modifiers changed from: private */
        public void setName(String name) {
            this.mName = name;
        }

        /* access modifiers changed from: private */
        public void setId(int id) {
            this.mId = id;
        }

        /* access modifiers changed from: private */
        public boolean isVisited() {
            return this.mVisited;
        }

        /* access modifiers changed from: private */
        public void setParent(MtpObject parent) {
            this.mParent = parent;
            if (parent != null) {
                this.mStorageId = parent.getStorageId();
            } else {
                this.mStorageId = this.mId;
            }
        }

        /* access modifiers changed from: private */
        public void setDir(boolean dir) {
            if (dir != this.mIsDir) {
                this.mIsDir = dir;
                this.mChildren = this.mIsDir ? new HashMap<>() : null;
            }
        }

        /* access modifiers changed from: private */
        public void setVisited(boolean visited) {
            this.mVisited = visited;
        }

        /* access modifiers changed from: private */
        public MtpObjectState getState() {
            return this.mState;
        }

        /* access modifiers changed from: private */
        public void setState(MtpObjectState state) {
            this.mState = state;
            if (this.mState == MtpObjectState.NORMAL) {
                this.mOp = MtpOperation.NONE;
            }
        }

        /* access modifiers changed from: private */
        public MtpOperation getOperation() {
            return this.mOp;
        }

        /* access modifiers changed from: private */
        public void setOperation(MtpOperation op) {
            this.mOp = op;
        }

        /* access modifiers changed from: private */
        public FileObserver getObserver() {
            return this.mObserver;
        }

        /* access modifiers changed from: private */
        public void setObserver(FileObserver observer) {
            this.mObserver = observer;
        }

        /* access modifiers changed from: private */
        public void addChild(MtpObject child) {
            this.mChildren.put(child.getName(), child);
        }

        /* access modifiers changed from: private */
        public MtpObject getChild(String name) {
            return this.mChildren.get(name);
        }

        /* access modifiers changed from: private */
        public Collection<MtpObject> getChildren() {
            return this.mChildren.values();
        }

        /* access modifiers changed from: private */
        public boolean exists() {
            return getPath().toFile().exists();
        }

        /* access modifiers changed from: private */
        public MtpObject copy(boolean recursive) {
            MtpObject copy = new MtpObject(this.mName, this.mId, this.mParent, this.mIsDir);
            copy.mIsDir = this.mIsDir;
            copy.mVisited = this.mVisited;
            copy.mState = this.mState;
            copy.mChildren = this.mIsDir ? new HashMap<>() : null;
            if (recursive && this.mIsDir) {
                for (MtpObject child : this.mChildren.values()) {
                    MtpObject childCopy = child.copy(true);
                    childCopy.setParent(copy);
                    copy.addChild(childCopy);
                }
            }
            return copy;
        }
    }

    private class MtpObjectObserver extends FileObserver {
        MtpObject mObject;

        MtpObjectObserver(MtpObject object) {
            super(object.getPath().toString(), 16778184);
            this.mObject = object;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c2, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:51:0x0125, code lost:
            return;
         */
        public void onEvent(int event, String path) {
            synchronized (MtpStorageManager.this) {
                if ((event & 16384) != 0) {
                    try {
                        Log.e(MtpStorageManager.TAG, "Received Inotify overflow event!");
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                MtpObject obj = this.mObject.getChild(path);
                if ((event & 128) == 0 && (event & 256) == 0) {
                    if ((event & 8) == 0) {
                        if ((event & 64) == 0) {
                            if ((event & 512) == 0) {
                                if ((32768 & event) != 0) {
                                    if (MtpStorageManager.DEBUG) {
                                        String access$000 = MtpStorageManager.TAG;
                                        Log.i(access$000, "inotify for " + this.mObject.getPath() + " deleted");
                                    }
                                    if (this.mObject.mObserver != null) {
                                        this.mObject.mObserver.stopWatching();
                                    }
                                    FileObserver unused = this.mObject.mObserver = null;
                                } else if (MtpStorageManager.DEBUG) {
                                    String access$0002 = MtpStorageManager.TAG;
                                    Log.w(access$0002, "Got unrecognized event " + path + " " + event);
                                }
                            }
                        }
                        if (obj != null) {
                            if (MtpStorageManager.DEBUG) {
                                String access$0003 = MtpStorageManager.TAG;
                                Log.i(access$0003, "Got inotify removed event for " + path + " " + event);
                            }
                            MtpStorageManager.this.handleRemovedObject(obj);
                        } else if (MtpStorageManager.DEBUG) {
                            String access$0004 = MtpStorageManager.TAG;
                            Log.w(access$0004, "Object was null in event " + path);
                        }
                    }
                }
                if (MtpStorageManager.DEBUG) {
                    String access$0005 = MtpStorageManager.TAG;
                    Log.i(access$0005, "Got inotify added event for " + path + " " + event);
                }
                MtpStorageManager.this.handleAddedObject(this.mObject, path, (1073741824 & event) != 0);
            }
        }

        public void finalize() {
        }
    }

    private enum MtpObjectState {
        NORMAL,
        FROZEN,
        FROZEN_ADDED,
        FROZEN_REMOVED,
        FROZEN_ONESHOT_ADD,
        FROZEN_ONESHOT_DEL
    }

    private enum MtpOperation {
        NONE,
        ADD,
        RENAME,
        COPY,
        DELETE
    }

    public MtpStorageManager(MtpNotifier notifier, Set<String> subdirectories) {
        this.mMtpNotifier = notifier;
        this.mSubdirectories = subdirectories;
        if (this.mCheckConsistency) {
            this.mConsistencyThread.start();
        }
    }

    public static /* synthetic */ void lambda$new$0(MtpStorageManager mtpStorageManager) {
        while (mtpStorageManager.mCheckConsistency) {
            try {
                Thread.sleep(15000);
                if (mtpStorageManager.checkConsistency()) {
                    Log.v(TAG, "Cache is consistent");
                } else {
                    Log.w(TAG, "Cache is not consistent");
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public synchronized void close() {
        for (MtpObject obj : Stream.concat(this.mRoots.values().stream(), this.mObjects.values().stream())) {
            if (obj.getObserver() != null) {
                obj.getObserver().stopWatching();
                obj.setObserver(null);
            }
        }
        if (this.mCheckConsistency) {
            this.mCheckConsistency = false;
            this.mConsistencyThread.interrupt();
            try {
                this.mConsistencyThread.join();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void setSubdirectories(Set<String> subDirs) {
        this.mSubdirectories = subDirs;
    }

    public synchronized MtpStorage addMtpStorage(StorageVolume volume) {
        MtpStorage storage;
        int storageId = ((getNextStorageId() & 65535) << 16) + 1;
        storage = new MtpStorage(volume, storageId);
        this.mRoots.put(Integer.valueOf(storageId), new MtpObject(storage.getPath(), storageId, null, true));
        return storage;
    }

    public synchronized MtpStorage addMtpStorage(StorageVolume volume, int storageId) {
        MtpStorage storage;
        MtpObject root = new MtpObject(volume.getPath(), storageId, null, true);
        storage = new MtpStorage(volume, storageId);
        this.mRoots.put(Integer.valueOf(storageId), root);
        return storage;
    }

    public synchronized void removeMtpStorage(MtpStorage storage) {
        removeObjectFromCache(getStorageRoot(storage.getStorageId()), true, true);
    }

    private synchronized boolean isSpecialSubDir(MtpObject obj) {
        return obj.getParent().isRoot() && this.mSubdirectories != null && !this.mSubdirectories.contains(obj.getName());
    }

    public synchronized MtpObject getByPath(String path) {
        MtpObject obj = null;
        for (MtpObject root : this.mRoots.values()) {
            if (path.startsWith(root.getName())) {
                obj = root;
                path = path.substring(root.getName().length());
            }
        }
        String[] split = path.split("/");
        int length = split.length;
        int i = 0;
        while (i < length) {
            String name = split[i];
            if (obj != null) {
                if (obj.isDir()) {
                    if (!"".equals(name)) {
                        if (!obj.isVisited()) {
                            getChildren(obj);
                        }
                        obj = obj.getChild(name);
                    }
                    i++;
                }
            }
            return null;
        }
        return obj;
    }

    public synchronized MtpObject getObject(int id) {
        if (id == 0 || id == -1) {
            Log.w(TAG, "Can't get root storages with getObject()");
            return null;
        } else if (!this.mObjects.containsKey(Integer.valueOf(id))) {
            String str = TAG;
            Log.w(str, "Id " + id + " doesn't exist");
            return null;
        } else {
            return this.mObjects.get(Integer.valueOf(id));
        }
    }

    public MtpObject getStorageRoot(int id) {
        if (this.mRoots.containsKey(Integer.valueOf(id))) {
            return this.mRoots.get(Integer.valueOf(id));
        }
        String str = TAG;
        Log.w(str, "StorageId " + id + " doesn't exist");
        return null;
    }

    private int getNextObjectId() {
        int ret = this.mNextObjectId;
        this.mNextObjectId = (int) (((long) this.mNextObjectId) + 1);
        return ret;
    }

    private int getNextStorageId() {
        int i = this.mNextStorageId;
        this.mNextStorageId = i + 1;
        return i;
    }

    public synchronized Stream<MtpObject> getObjects(int parent, int format, int storageId) {
        boolean recursive = parent == 0;
        if (parent == -1) {
            parent = 0;
        }
        if (storageId == -1 && parent == 0) {
            ArrayList<Stream<MtpObject>> streamList = new ArrayList<>();
            for (MtpObject root : this.mRoots.values()) {
                streamList.add(getObjects(root, format, recursive));
            }
            return (Stream) Stream.of(streamList).flatMap($$Lambda$JdUL9ZP9AzcttUlxZCHq6pfTzU.INSTANCE).reduce($$Lambda$MtpStorageManager$QdR1YPNkK9RX4bISfNvQAOnGxGE.INSTANCE).orElseGet($$Lambda$MtpStorageManager$TsWypJRYDhxg01Bfs_tm2d_H9zU.INSTANCE);
        }
        MtpObject obj = parent == 0 ? getStorageRoot(storageId) : getObject(parent);
        if (obj == null) {
            return null;
        }
        return getObjects(obj, format, recursive);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0069, code lost:
        return r1;
     */
    private synchronized Stream<MtpObject> getObjects(MtpObject parent, int format, boolean rec) {
        Collection<MtpObject> children = getChildren(parent);
        if (children == null) {
            return null;
        }
        Stream<R> flatMap = Stream.of(children).flatMap($$Lambda$72U6ffwsZ0Sm2BXYilXSg7hTsO8.INSTANCE);
        if (format != 0) {
            flatMap = flatMap.filter(new Predicate(format) {
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                public final boolean test(Object obj) {
                    return MtpStorageManager.lambda$getObjects$1(this.f$0, (MtpStorageManager.MtpObject) obj);
                }
            });
        }
        if (rec) {
            ArrayList<Stream<MtpObject>> streamList = new ArrayList<>();
            streamList.add(flatMap);
            for (MtpObject o : children) {
                if (o.isDir()) {
                    streamList.add(getObjects(o, format, true));
                }
            }
            flatMap = (Stream) Stream.of(streamList).filter($$Lambda$MtpStorageManager$ZX5EBcSdO0MZYnMFDwTJpRFAOd0.INSTANCE).flatMap($$Lambda$JdUL9ZP9AzcttUlxZCHq6pfTzU.INSTANCE).reduce($$Lambda$MtpStorageManager$QdR1YPNkK9RX4bISfNvQAOnGxGE.INSTANCE).orElseGet($$Lambda$MtpStorageManager$TsWypJRYDhxg01Bfs_tm2d_H9zU.INSTANCE);
        }
    }

    static /* synthetic */ boolean lambda$getObjects$1(int format, MtpObject o) {
        return o.getFormat() == format;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0065, code lost:
        r3 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0066, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006a, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006b, code lost:
        r7 = r4;
        r4 = r3;
        r3 = r7;
     */
    /* JADX WARNING: Unknown top exception splitter block from list: {B:47:0x0092=Splitter:B:47:0x0092, B:42:0x008a=Splitter:B:42:0x008a} */
    private synchronized Collection<MtpObject> getChildren(MtpObject object) {
        DirectoryStream<Path> stream;
        Throwable th;
        Throwable th2;
        if (object != null) {
            if (object.isDir()) {
                if (!object.isVisited()) {
                    Path dir = object.getPath();
                    if (object.getObserver() != null) {
                        Log.e(TAG, "Observer is not null!");
                    }
                    object.setObserver(new MtpObjectObserver(object));
                    object.getObserver().startWatching();
                    try {
                        stream = Files.newDirectoryStream(dir);
                        for (Path file : stream) {
                            addObjectToCache(object, file.getFileName().toString(), file.toFile().isDirectory());
                        }
                        if (stream != null) {
                            $closeResource(null, stream);
                        }
                        object.setVisited(true);
                    } catch (IOException | DirectoryIteratorException e) {
                        Log.e(TAG, e.toString());
                        object.getObserver().stopWatching();
                        object.setObserver(null);
                        return null;
                    }
                }
                return object.getChildren();
            }
        }
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("Can't find children of ");
        sb.append(object == null ? "null" : Integer.valueOf(object.getId()));
        Log.w(str, sb.toString());
        return null;
        if (stream != null) {
            $closeResource(th, stream);
        }
        throw th2;
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

    private synchronized MtpObject addObjectToCache(MtpObject parent, String newName, boolean isDir) {
        if (!parent.isRoot() && getObject(parent.getId()) != parent) {
            return null;
        }
        if (parent.getChild(newName) != null) {
            return null;
        }
        if (this.mSubdirectories != null && parent.isRoot() && !this.mSubdirectories.contains(newName)) {
            return null;
        }
        MtpObject obj = new MtpObject(newName, getNextObjectId(), parent, isDir);
        this.mObjects.put(Integer.valueOf(obj.getId()), obj);
        parent.addChild(obj);
        return obj;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0045  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x009a  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00c0  */
    private synchronized boolean removeObjectFromCache(MtpObject removed, boolean removeGlobal, boolean recursive) {
        boolean ret;
        if (!removed.isRoot()) {
            if (!removed.getParent().mChildren.remove(removed.getName(), removed)) {
                ret = false;
                if (!ret && DEBUG) {
                    Log.w(TAG, "Failed to remove from parent " + removed.getPath());
                }
                if (!removed.isRoot()) {
                    ret = this.mRoots.remove(Integer.valueOf(removed.getId()), removed) && ret;
                } else if (removeGlobal) {
                    ret = this.mObjects.remove(Integer.valueOf(removed.getId()), removed) && ret;
                }
                if (!ret && DEBUG) {
                    Log.w(TAG, "Failed to remove from global cache " + removed.getPath());
                }
                if (removed.getObserver() != null) {
                    removed.getObserver().stopWatching();
                    removed.setObserver(null);
                }
                if (removed.isDir() && recursive) {
                    for (MtpObject child : new ArrayList<>(removed.getChildren())) {
                        ret = removeObjectFromCache(child, removeGlobal, true) && ret;
                    }
                }
            }
        }
        ret = true;
        Log.w(TAG, "Failed to remove from parent " + removed.getPath());
        if (!removed.isRoot()) {
        }
        Log.w(TAG, "Failed to remove from global cache " + removed.getPath());
        if (removed.getObserver() != null) {
        }
        while (r4.hasNext()) {
        }
        return ret;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0156, code lost:
        r4 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0157, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x015b, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x015c, code lost:
        r9 = r5;
        r5 = r4;
        r4 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x019c, code lost:
        return;
     */
    public synchronized void handleAddedObject(MtpObject parent, String path, boolean isDir) {
        DirectoryStream<Path> stream;
        Throwable th;
        Throwable th2;
        MtpOperation op = MtpOperation.NONE;
        MtpObject obj = parent.getChild(path);
        if (obj != null) {
            MtpObjectState state = obj.getState();
            op = obj.getOperation();
            if (!(obj.isDir() == isDir || state == MtpObjectState.FROZEN_REMOVED)) {
                Log.d(TAG, "Inconsistent directory info! " + obj.getPath());
            }
            obj.setDir(isDir);
            switch (state) {
                case FROZEN:
                case FROZEN_REMOVED:
                    obj.setState(MtpObjectState.FROZEN_ADDED);
                    break;
                case FROZEN_ONESHOT_ADD:
                    obj.setState(MtpObjectState.NORMAL);
                    this.mMtpNotifier.sendObjectAdded(obj.getId());
                    break;
                case NORMAL:
                case FROZEN_ADDED:
                    return;
                default:
                    if (DEBUG) {
                        Log.w(TAG, "Unexpected state in add " + path + " " + state);
                        break;
                    }
                    break;
            }
            if (DEBUG) {
                Log.i(TAG, state + " transitioned to " + obj.getState() + " in op " + op);
            }
        } else {
            obj = addObjectToCache(parent, path, isDir);
            if (obj != null) {
                if (!isDir) {
                    if (obj.getSize() == 0) {
                        obj.setState(MtpObjectState.FROZEN_ONESHOT_ADD);
                    }
                }
                this.mMtpNotifier.sendObjectAdded(obj.getId());
            } else if (DEBUG) {
                Log.w(TAG, "object " + path + " already exists");
            }
        }
        if (isDir) {
            if (op != MtpOperation.RENAME) {
                if (op == MtpOperation.COPY && !obj.isVisited()) {
                    return;
                }
                if (obj.getObserver() != null) {
                    Log.e(TAG, "Observer is not null!");
                    return;
                }
                obj.setObserver(new MtpObjectObserver(obj));
                obj.getObserver().startWatching();
                obj.setVisited(true);
                try {
                    stream = Files.newDirectoryStream(obj.getPath());
                    for (Path file : stream) {
                        if (DEBUG) {
                            Log.i(TAG, "Manually handling event for " + file.getFileName().toString());
                        }
                        handleAddedObject(obj, file.getFileName().toString(), file.toFile().isDirectory());
                    }
                    if (stream != null) {
                        $closeResource(null, stream);
                    }
                } catch (IOException | DirectoryIteratorException e) {
                    Log.e(TAG, e.toString());
                    obj.getObserver().stopWatching();
                    obj.setObserver(null);
                }
            } else {
                return;
            }
        }
        return;
        if (stream != null) {
            $closeResource(th, stream);
        }
        throw th2;
    }

    /* access modifiers changed from: private */
    public synchronized void handleRemovedObject(MtpObject obj) {
        MtpObjectState state = obj.getState();
        MtpOperation op = obj.getOperation();
        int i = AnonymousClass1.$SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[state.ordinal()];
        boolean z = true;
        if (i != 1) {
            switch (i) {
                case 4:
                    if (removeObjectFromCache(obj, true, true)) {
                        this.mMtpNotifier.sendObjectRemoved(obj.getId());
                        break;
                    }
                    break;
                case 5:
                    obj.setState(MtpObjectState.FROZEN_REMOVED);
                    break;
                case 6:
                    if (op == MtpOperation.RENAME) {
                        z = false;
                    }
                    removeObjectFromCache(obj, z, false);
                    break;
                default:
                    Log.e(TAG, "Got unexpected object remove for " + obj.getName());
                    break;
            }
        } else {
            obj.setState(MtpObjectState.FROZEN_REMOVED);
        }
        if (DEBUG) {
            Log.i(TAG, state + " transitioned to " + obj.getState() + " in op " + op);
        }
    }

    public void flushEvents() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }

    public synchronized void dump() {
        for (Integer intValue : this.mObjects.keySet()) {
            int key = intValue.intValue();
            MtpObject obj = this.mObjects.get(Integer.valueOf(key));
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append(key);
            sb.append(" | ");
            sb.append(obj.getParent() == null ? Integer.valueOf(obj.getParent().getId()) : "null");
            sb.append(" | ");
            sb.append(obj.getName());
            sb.append(" | ");
            sb.append(obj.isDir() ? "dir" : "obj");
            sb.append(" | ");
            sb.append(obj.isVisited() ? "v" : "nv");
            sb.append(" | ");
            sb.append(obj.getState());
            Log.i(str, sb.toString());
        }
    }

    public synchronized boolean checkConsistency() {
        boolean ret;
        DirectoryStream<Path> stream;
        ret = true;
        for (MtpObject obj : Stream.concat(this.mRoots.values().stream(), this.mObjects.values().stream())) {
            if (!obj.exists()) {
                if (DEBUG) {
                    Log.w(TAG, "Object doesn't exist " + obj.getPath() + " " + obj.getId());
                }
                ret = false;
            }
            if (obj.getState() != MtpObjectState.NORMAL) {
                if (DEBUG) {
                    Log.w(TAG, "Object " + obj.getPath() + " in state " + obj.getState());
                }
                ret = false;
            }
            if (obj.getOperation() != MtpOperation.NONE) {
                if (DEBUG) {
                    Log.w(TAG, "Object " + obj.getPath() + " in operation " + obj.getOperation());
                }
                ret = false;
            }
            if (!obj.isRoot() && this.mObjects.get(Integer.valueOf(obj.getId())) != obj) {
                if (DEBUG) {
                    Log.w(TAG, "Object " + obj.getPath() + " is not in map correctly");
                }
                ret = false;
            }
            if (obj.getParent() != null) {
                if (obj.getParent().isRoot() && obj.getParent() != this.mRoots.get(Integer.valueOf(obj.getParent().getId()))) {
                    if (DEBUG) {
                        Log.w(TAG, "Root parent is not in root mapping " + obj.getPath());
                    }
                    ret = false;
                }
                if (!obj.getParent().isRoot() && obj.getParent() != this.mObjects.get(Integer.valueOf(obj.getParent().getId()))) {
                    if (DEBUG) {
                        Log.w(TAG, "Parent is not in object mapping " + obj.getPath());
                    }
                    ret = false;
                }
                if (obj.getParent().getChild(obj.getName()) != obj) {
                    if (DEBUG) {
                        Log.w(TAG, "Child does not exist in parent " + obj.getPath());
                    }
                    ret = false;
                }
            }
            if (obj.isDir()) {
                if (obj.isVisited() == (obj.getObserver() == null)) {
                    if (DEBUG) {
                        String str = TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append(obj.getPath());
                        sb.append(" is ");
                        sb.append(obj.isVisited() ? "" : "not ");
                        sb.append(" visited but observer is ");
                        sb.append(obj.getObserver());
                        Log.w(str, sb.toString());
                    }
                    ret = false;
                }
                if (!obj.isVisited() && obj.getChildren().size() > 0) {
                    if (DEBUG) {
                        Log.w(TAG, obj.getPath() + " is not visited but has children");
                    }
                    ret = false;
                }
                try {
                    stream = Files.newDirectoryStream(obj.getPath());
                    Set<String> files = new HashSet<>();
                    for (Path file : stream) {
                        if (obj.isVisited() && obj.getChild(file.getFileName().toString()) == null && (this.mSubdirectories == null || !obj.isRoot() || this.mSubdirectories.contains(file.getFileName().toString()))) {
                            Log.w(TAG, "File exists in fs but not in children " + file);
                            ret = false;
                        }
                        files.add(file.toString());
                    }
                    for (MtpObject child : obj.getChildren()) {
                        if (!files.contains(child.getPath().toString())) {
                            if (DEBUG) {
                                Log.w(TAG, "File in children doesn't exist in fs " + child.getPath());
                            }
                            ret = false;
                        }
                        if (child != this.mObjects.get(Integer.valueOf(child.getId()))) {
                            if (DEBUG) {
                                Log.w(TAG, "Child is not in object map " + child.getPath());
                            }
                            ret = false;
                        }
                    }
                    if (stream != null) {
                        $closeResource(null, stream);
                    }
                } catch (IOException | DirectoryIteratorException e) {
                    Log.w(TAG, e.toString());
                    ret = false;
                } catch (Throwable th) {
                    if (stream != null) {
                        $closeResource(r6, stream);
                    }
                    throw th;
                }
            }
        }
        return ret;
    }

    public synchronized int beginSendObject(MtpObject parent, String name, int format) {
        if (DEBUG) {
            String str = TAG;
            Log.v(str, "beginSendObject " + name);
        }
        if (!parent.isDir()) {
            return -1;
        }
        if (parent.isRoot() && this.mSubdirectories != null && !this.mSubdirectories.contains(name)) {
            return -1;
        }
        getChildren(parent);
        MtpObject obj = addObjectToCache(parent, name, format == 12289);
        if (obj == null) {
            return -1;
        }
        obj.setState(MtpObjectState.FROZEN);
        obj.setOperation(MtpOperation.ADD);
        return obj.getId();
    }

    public synchronized boolean endSendObject(MtpObject obj, boolean succeeded) {
        if (DEBUG) {
            String str = TAG;
            Log.v(str, "endSendObject " + succeeded);
        }
        return generalEndAddObject(obj, succeeded, true);
    }

    public synchronized boolean beginRenameObject(MtpObject obj, String newName) {
        if (DEBUG) {
            String str = TAG;
            Log.v(str, "beginRenameObject " + obj.getName() + " " + newName);
        }
        if (obj.isRoot()) {
            return false;
        }
        if (isSpecialSubDir(obj)) {
            return false;
        }
        if (obj.getParent().getChild(newName) != null) {
            return false;
        }
        MtpObject oldObj = obj.copy(false);
        obj.setName(newName);
        obj.getParent().addChild(obj);
        oldObj.getParent().addChild(oldObj);
        return generalBeginRenameObject(oldObj, obj);
    }

    public synchronized boolean endRenameObject(MtpObject obj, String oldName, boolean success) {
        MtpObject oldObj;
        if (DEBUG) {
            String str = TAG;
            Log.v(str, "endRenameObject " + success);
        }
        MtpObject parent = obj.getParent();
        oldObj = parent.getChild(oldName);
        if (!success) {
            MtpObject temp = oldObj;
            MtpObjectState oldState = oldObj.getState();
            temp.setName(obj.getName());
            temp.setState(obj.getState());
            oldObj = obj;
            oldObj.setName(oldName);
            oldObj.setState(oldState);
            obj = temp;
            parent.addChild(obj);
            parent.addChild(oldObj);
        }
        return generalEndRenameObject(oldObj, obj, success);
    }

    public synchronized boolean beginRemoveObject(MtpObject obj) {
        if (DEBUG) {
            String str = TAG;
            Log.v(str, "beginRemoveObject " + obj.getName());
        }
        return !obj.isRoot() && !isSpecialSubDir(obj) && generalBeginRemoveObject(obj, MtpOperation.DELETE);
    }

    public synchronized boolean endRemoveObject(MtpObject obj, boolean success) {
        boolean z;
        if (DEBUG) {
            Log.v(TAG, "endRemoveObject " + success);
        }
        boolean ret = true;
        z = false;
        if (obj.isDir()) {
            Iterator it = new ArrayList(obj.getChildren()).iterator();
            while (it.hasNext()) {
                MtpObject child = (MtpObject) it.next();
                if (child.getOperation() == MtpOperation.DELETE) {
                    ret = endRemoveObject(child, success) && ret;
                }
            }
        }
        if (generalEndRemoveObject(obj, success, true) && ret) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0065, code lost:
        return r0;
     */
    public synchronized boolean beginMoveObject(MtpObject obj, MtpObject newParent) {
        if (DEBUG) {
            Log.v(TAG, "beginMoveObject " + newParent.getPath());
        }
        if (obj.isRoot()) {
            return false;
        }
        if (isSpecialSubDir(obj)) {
            return false;
        }
        getChildren(newParent);
        if (newParent.getChild(obj.getName()) != null) {
            return false;
        }
        if (obj.getStorageId() != newParent.getStorageId()) {
            boolean z = true;
            MtpObject newObj = obj.copy(true);
            newObj.setParent(newParent);
            newParent.addChild(newObj);
            if (!generalBeginRemoveObject(obj, MtpOperation.RENAME) || !generalBeginCopyObject(newObj, false)) {
                z = false;
            }
        } else {
            MtpObject oldObj = obj.copy(false);
            obj.setParent(newParent);
            oldObj.getParent().addChild(oldObj);
            obj.getParent().addChild(obj);
            return generalBeginRenameObject(oldObj, obj);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0043, code lost:
        return r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0072, code lost:
        return false;
     */
    public synchronized boolean endMoveObject(MtpObject oldParent, MtpObject newParent, String name, boolean success) {
        if (DEBUG) {
            Log.v(TAG, "endMoveObject " + success);
        }
        MtpObject oldObj = oldParent.getChild(name);
        MtpObject newObj = newParent.getChild(name);
        boolean z = false;
        if (oldObj != null) {
            if (newObj != null) {
                if (oldParent.getStorageId() != newObj.getStorageId()) {
                    boolean ret = endRemoveObject(oldObj, success);
                    if (generalEndCopyObject(newObj, success, true) && ret) {
                        z = true;
                    }
                } else {
                    if (!success) {
                        MtpObject temp = oldObj;
                        MtpObjectState oldState = oldObj.getState();
                        temp.setParent(newObj.getParent());
                        temp.setState(newObj.getState());
                        oldObj = newObj;
                        oldObj.setParent(oldParent);
                        oldObj.setState(oldState);
                        newObj = temp;
                        newObj.getParent().addChild(newObj);
                        oldParent.addChild(oldObj);
                    }
                    return generalEndRenameObject(oldObj, newObj, success);
                }
            }
        }
    }

    public synchronized int beginCopyObject(MtpObject object, MtpObject newParent) {
        if (DEBUG) {
            String str = TAG;
            Log.v(str, "beginCopyObject " + object.getName() + " to " + newParent.getPath());
        }
        String name = object.getName();
        if (!newParent.isDir()) {
            return -1;
        }
        if (newParent.isRoot() && this.mSubdirectories != null && !this.mSubdirectories.contains(name)) {
            return -1;
        }
        getChildren(newParent);
        if (newParent.getChild(name) != null) {
            return -1;
        }
        MtpObject newObj = object.copy(object.isDir());
        newParent.addChild(newObj);
        newObj.setParent(newParent);
        if (!generalBeginCopyObject(newObj, true)) {
            return -1;
        }
        return newObj.getId();
    }

    public synchronized boolean endCopyObject(MtpObject object, boolean success) {
        if (DEBUG) {
            String str = TAG;
            Log.v(str, "endCopyObject " + object.getName() + " " + success);
        }
        return generalEndCopyObject(object, success, false);
    }

    private synchronized boolean generalEndAddObject(MtpObject obj, boolean succeeded, boolean removeGlobal) {
        int i = AnonymousClass1.$SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[obj.getState().ordinal()];
        if (i != 5) {
            switch (i) {
                case 1:
                    if (succeeded) {
                        obj.setState(MtpObjectState.FROZEN_ONESHOT_ADD);
                        break;
                    } else if (!removeObjectFromCache(obj, removeGlobal, false)) {
                        return false;
                    }
                    break;
                case 2:
                    if (removeObjectFromCache(obj, removeGlobal, false)) {
                        if (succeeded) {
                            this.mMtpNotifier.sendObjectRemoved(obj.getId());
                            break;
                        }
                    } else {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        } else {
            obj.setState(MtpObjectState.NORMAL);
            if (!succeeded) {
                MtpObject parent = obj.getParent();
                if (!removeObjectFromCache(obj, removeGlobal, false)) {
                    return false;
                }
                handleAddedObject(parent, obj.getName(), obj.isDir());
            }
        }
        return true;
    }

    private synchronized boolean generalEndRemoveObject(MtpObject obj, boolean success, boolean removeGlobal) {
        int i = AnonymousClass1.$SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[obj.getState().ordinal()];
        if (i != 5) {
            switch (i) {
                case 1:
                    if (!success) {
                        obj.setState(MtpObjectState.NORMAL);
                        break;
                    } else {
                        obj.setState(MtpObjectState.FROZEN_ONESHOT_DEL);
                        break;
                    }
                case 2:
                    if (removeObjectFromCache(obj, removeGlobal, false)) {
                        if (!success) {
                            this.mMtpNotifier.sendObjectRemoved(obj.getId());
                            break;
                        }
                    } else {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
        } else {
            obj.setState(MtpObjectState.NORMAL);
            if (success) {
                MtpObject parent = obj.getParent();
                if (!removeObjectFromCache(obj, removeGlobal, false)) {
                    return false;
                }
                handleAddedObject(parent, obj.getName(), obj.isDir());
            }
        }
        return true;
    }

    private synchronized boolean generalBeginRenameObject(MtpObject fromObj, MtpObject toObj) {
        fromObj.setState(MtpObjectState.FROZEN);
        toObj.setState(MtpObjectState.FROZEN);
        fromObj.setOperation(MtpOperation.RENAME);
        toObj.setOperation(MtpOperation.RENAME);
        return true;
    }

    private synchronized boolean generalEndRenameObject(MtpObject fromObj, MtpObject toObj, boolean success) {
        return generalEndAddObject(toObj, success, success) && generalEndRemoveObject(fromObj, success, success ^ true);
    }

    private synchronized boolean generalBeginRemoveObject(MtpObject obj, MtpOperation op) {
        obj.setState(MtpObjectState.FROZEN);
        obj.setOperation(op);
        if (obj.isDir()) {
            for (MtpObject child : obj.getChildren()) {
                generalBeginRemoveObject(child, op);
            }
        }
        return true;
    }

    private synchronized boolean generalBeginCopyObject(MtpObject obj, boolean newId) {
        obj.setState(MtpObjectState.FROZEN);
        obj.setOperation(MtpOperation.COPY);
        if (newId) {
            obj.setId(getNextObjectId());
            this.mObjects.put(Integer.valueOf(obj.getId()), obj);
        }
        if (obj.isDir()) {
            for (MtpObject child : obj.getChildren()) {
                if (!generalBeginCopyObject(child, newId)) {
                    return false;
                }
            }
        }
        return true;
    }

    private synchronized boolean generalEndCopyObject(MtpObject obj, boolean success, boolean addGlobal) {
        boolean ret;
        boolean z;
        if (success && addGlobal) {
            this.mObjects.put(Integer.valueOf(obj.getId()), obj);
        }
        boolean ret2 = true;
        ret = false;
        if (obj.isDir()) {
            Iterator it = new ArrayList(obj.getChildren()).iterator();
            while (it.hasNext()) {
                MtpObject child = (MtpObject) it.next();
                if (child.getOperation() == MtpOperation.COPY) {
                    ret2 = generalEndCopyObject(child, success, addGlobal) && ret2;
                }
            }
        }
        if (!success) {
            if (addGlobal) {
                z = false;
                if (generalEndAddObject(obj, success, z) && ret2) {
                    ret = true;
                }
            }
        }
        z = true;
        ret = true;
        return ret;
    }
}
