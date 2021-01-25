package android.mtp;

import android.media.MediaFile;
import android.mtp.MtpObjectEx;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.FileObserver;
import android.os.storage.StorageVolume;
import android.provider.Telephony;
import android.util.Log;
import com.android.internal.util.Preconditions;
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
import java.util.List;
import java.util.Set;

public class MtpStorageManager {
    private static final int IN_IGNORED = 32768;
    private static final int IN_ISDIR = 1073741824;
    private static final int IN_ONLYDIR = 16777216;
    private static final int IN_Q_OVERFLOW = 16384;
    private static final String TAG = MtpStorageManager.class.getSimpleName();
    private static final boolean sDebug = Log.isLoggable(TAG, 3);
    private volatile boolean mCheckConsistency = false;
    private Thread mConsistencyThread = new Thread(new Runnable() {
        /* class android.mtp.$$Lambda$MtpStorageManager$HocvlaKIXOtuA3p8uOP9PA7UJtw */

        @Override // java.lang.Runnable
        public final void run() {
            MtpStorageManager.this.lambda$new$0$MtpStorageManager();
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

        public abstract void sendObjectInfoChanged(int i);

        public abstract void sendObjectRemoved(int i);
    }

    /* access modifiers changed from: private */
    public enum MtpObjectState {
        NORMAL,
        FROZEN,
        FROZEN_ADDED,
        FROZEN_REMOVED,
        FROZEN_ONESHOT_ADD,
        FROZEN_ONESHOT_DEL
    }

    /* access modifiers changed from: private */
    public enum MtpOperation {
        NONE,
        ADD,
        RENAME,
        COPY,
        DELETE
    }

    /* access modifiers changed from: private */
    public class MtpObjectObserver extends FileObserver {
        MtpObject mObject;

        MtpObjectObserver(MtpObject object) {
            super(object.getPath().toString(), 16778184);
            this.mObject = object;
        }

        @Override // android.os.FileObserver
        public void onEvent(int event, String path) {
            synchronized (MtpStorageManager.this) {
                if ((event & 16384) != 0) {
                    Log.e(MtpStorageManager.TAG, "Received Inotify overflow event!");
                }
                MtpObject obj = this.mObject.getChild(path);
                if ((event & 128) == 0) {
                    if ((event & 256) == 0) {
                        if ((event & 64) == 0) {
                            if ((event & 512) == 0) {
                                if ((32768 & event) != 0) {
                                    if (MtpStorageManager.sDebug) {
                                        String str = MtpStorageManager.TAG;
                                        Log.i(str, "inotify for " + this.mObject.getPath() + " deleted");
                                    }
                                    if (this.mObject.mObserver != null) {
                                        this.mObject.mObserver.stopWatching();
                                    }
                                    this.mObject.mObserver = null;
                                } else if ((event & 8) != 0) {
                                    if (MtpStorageManager.sDebug) {
                                        String str2 = MtpStorageManager.TAG;
                                        Log.i(str2, "inotify for " + this.mObject.getPath() + " CLOSE_WRITE: " + path);
                                    }
                                    MtpStorageManager.this.handleChangedObject(this.mObject, path);
                                } else if (MtpStorageManager.sDebug) {
                                    String str3 = MtpStorageManager.TAG;
                                    Log.w(str3, "Got unrecognized event " + path + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + event);
                                }
                            }
                        }
                        if (obj == null) {
                            if (MtpStorageManager.sDebug) {
                                String str4 = MtpStorageManager.TAG;
                                Log.w(str4, "Object was null in event " + path);
                            }
                            return;
                        }
                        if (MtpStorageManager.sDebug) {
                            String str5 = MtpStorageManager.TAG;
                            Log.i(str5, "Got inotify removed event for " + path + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + event);
                        }
                        MtpStorageManager.this.handleRemovedObject(obj);
                    }
                }
                if (MtpStorageManager.sDebug) {
                    String str6 = MtpStorageManager.TAG;
                    Log.i(str6, "Got inotify added event for " + path + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + event);
                }
                MtpStorageManager.this.handleAddedObject(this.mObject, path, (1073741824 & event) != 0);
            }
        }

        @Override // android.os.FileObserver
        public void finalize() {
        }
    }

    public static class MtpObject {
        private HashMap<String, MtpObject> mChildren;
        private int mId;
        private boolean mIsDir;
        private String mName;
        private FileObserver mObserver = null;
        private MtpOperation mOp;
        private MtpObject mParent;
        private MtpObjectState mState = MtpObjectState.NORMAL;
        private MtpStorage mStorage;
        private int mStorageId;
        private boolean mVisited = false;
        private MtpObjectEx mtpObjectEx;

        MtpObject(String name, int id, MtpStorage storage, MtpObject parent, boolean isDir) {
            this.mId = id;
            this.mName = name;
            this.mStorage = (MtpStorage) Preconditions.checkNotNull(storage);
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
            if (this.mIsDir) {
                return 12289;
            }
            return MediaFile.getFormatCode(this.mName, null);
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

        public String getVolumeName() {
            return this.mStorage.getVolumeName();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setName(String name) {
            this.mName = name;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setId(int id) {
            this.mId = id;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isVisited() {
            return this.mVisited;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setParent(MtpObject parent) {
            this.mParent = parent;
            if (parent != null) {
                this.mStorageId = parent.getStorageId();
            } else {
                this.mStorageId = this.mId;
            }
        }

        public MtpObjectEx getMtpObjectEx() {
            if (this.mtpObjectEx == null) {
                MtpObjectEx.Params p = new MtpObjectEx.Params();
                p.id = getId();
                p.storageId = getStorageId();
                p.parentGetId = -1;
                if (getParent() != null) {
                    p.parentGetId = getParent().isRoot() ? 0 : getParent().getId();
                }
                p.size = getSize();
                p.time = getModifiedTime();
                p.name = getName();
                p.format = getFormat();
                p.path = "";
                if (getPath() != null) {
                    p.path = getPath().toString();
                }
                p.isDir = isDir();
                this.mtpObjectEx = new MtpObjectEx(p);
            }
            return this.mtpObjectEx;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setDir(boolean dir) {
            if (dir != this.mIsDir) {
                this.mIsDir = dir;
                this.mChildren = this.mIsDir ? new HashMap<>() : null;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setVisited(boolean visited) {
            this.mVisited = visited;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private MtpObjectState getState() {
            return this.mState;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setState(MtpObjectState state) {
            this.mState = state;
            if (this.mState == MtpObjectState.NORMAL) {
                this.mOp = MtpOperation.NONE;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private MtpOperation getOperation() {
            return this.mOp;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setOperation(MtpOperation op) {
            this.mOp = op;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private FileObserver getObserver() {
            return this.mObserver;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setObserver(FileObserver observer) {
            this.mObserver = observer;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addChild(MtpObject child) {
            this.mChildren.put(child.getName(), child);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private MtpObject getChild(String name) {
            return this.mChildren.get(name);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Collection<MtpObject> getChildren() {
            return this.mChildren.values();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean exists() {
            return getPath().toFile().exists();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private MtpObject copy(boolean recursive) {
            MtpObject copy = new MtpObject(this.mName, this.mId, this.mStorage, this.mParent, this.mIsDir);
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

    public MtpStorageManager(MtpNotifier notifier, Set<String> subdirectories) {
        this.mMtpNotifier = notifier;
        this.mSubdirectories = subdirectories;
        if (this.mCheckConsistency) {
            this.mConsistencyThread.start();
        }
    }

    public /* synthetic */ void lambda$new$0$MtpStorageManager() {
        while (this.mCheckConsistency) {
            try {
                Thread.sleep(15000);
                if (checkConsistency()) {
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
        for (MtpObject obj : this.mObjects.values()) {
            if (obj.getObserver() != null) {
                obj.getObserver().stopWatching();
                obj.setObserver(null);
            }
        }
        for (MtpObject obj2 : this.mRoots.values()) {
            if (obj2.getObserver() != null) {
                obj2.getObserver().stopWatching();
                obj2.setObserver(null);
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
        this.mRoots.put(Integer.valueOf(storageId), new MtpObject(storage.getPath(), storageId, storage, null, true));
        return storage;
    }

    public synchronized MtpStorage addMtpStorage(StorageVolume volume, int storageId) {
        MtpStorage storage;
        storage = new MtpStorage(volume, storageId);
        this.mRoots.put(Integer.valueOf(storageId), new MtpObject(volume.getPath(), storageId, storage, null, true));
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
        for (String name : split) {
            if (obj != null) {
                if (obj.isDir()) {
                    if (!"".equals(name)) {
                        if (!obj.isVisited()) {
                            getChildren(obj);
                        }
                        obj = obj.getChild(name);
                    }
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

    public synchronized List<MtpObject> getObjects(int parent, int format, int storageId) {
        boolean recursive = parent == 0;
        ArrayList<MtpObject> objs = new ArrayList<>();
        boolean ret = true;
        if (parent == -1) {
            parent = 0;
        }
        ArrayList<MtpObject> arrayList = null;
        if (storageId == -1 && parent == 0) {
            for (MtpObject root : this.mRoots.values()) {
                ret &= getObjects(objs, root, format, recursive);
            }
            if (ret) {
                arrayList = objs;
            }
            return arrayList;
        }
        MtpObject obj = parent == 0 ? getStorageRoot(storageId) : getObject(parent);
        if (obj == null) {
            return null;
        }
        if (getObjects(objs, obj, format, recursive)) {
            arrayList = objs;
        }
        return arrayList;
    }

    private synchronized boolean getObjects(List<MtpObject> toAdd, MtpObject parent, int format, boolean rec) {
        Collection<MtpObject> children = getChildren(parent);
        if (children == null) {
            return false;
        }
        for (MtpObject o : children) {
            if (format == 0 || o.getFormat() == format) {
                toAdd.add(o);
            }
        }
        boolean ret = true;
        if (rec) {
            for (MtpObject o2 : children) {
                if (o2.isDir()) {
                    ret &= getObjects(toAdd, o2, format, true);
                }
            }
        }
        return ret;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0066, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0067, code lost:
        if (r2 != null) goto L_0x0069;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0069, code lost:
        $closeResource(r3, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x006c, code lost:
        throw r4;
     */
    private synchronized Collection<MtpObject> getChildren(MtpObject object) {
        if (object != null) {
            if (object.isDir()) {
                if (!object.isVisited()) {
                    Path dir = object.getPath();
                    if (object.getObserver() != null) {
                        Log.e(TAG, "Observer is not null!");
                    }
                    object.setObserver(new MtpObjectObserver(object));
                    object.getObserver().startWatching();
                    DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
                    for (Path file : stream) {
                        addObjectToCache(object, file.getFileName().toString(), file.toFile().isDirectory());
                    }
                    try {
                        $closeResource(null, stream);
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
        MtpObject obj = new MtpObject(newName, getNextObjectId(), parent.mStorage, parent, isDir);
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
                if (!ret && sDebug) {
                    Log.w(TAG, "Failed to remove from parent " + removed.getPath());
                }
                if (!removed.isRoot()) {
                    ret = this.mRoots.remove(Integer.valueOf(removed.getId()), removed) && ret;
                } else if (removeGlobal) {
                    ret = this.mObjects.remove(Integer.valueOf(removed.getId()), removed) && ret;
                }
                if (!ret && sDebug) {
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
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x012e, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x012f, code lost:
        if (r3 != null) goto L_0x0131;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x0131, code lost:
        $closeResource(r4, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0134, code lost:
        throw r5;
     */
    private synchronized void handleAddedObject(MtpObject parent, String path, boolean isDir) {
        MtpOperation op = MtpOperation.NONE;
        MtpObject obj = parent.getChild(path);
        if (obj != null) {
            MtpObjectState state = obj.getState();
            op = obj.getOperation();
            if (!(obj.isDir() == isDir || state == MtpObjectState.FROZEN_REMOVED)) {
                String str = TAG;
                Log.d(str, "Inconsistent directory info! " + obj.getPath());
            }
            obj.setDir(isDir);
            int i = AnonymousClass1.$SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[state.ordinal()];
            if (i == 1 || i == 2) {
                obj.setState(MtpObjectState.FROZEN_ADDED);
            } else if (i == 3) {
                obj.setState(MtpObjectState.NORMAL);
            } else if (i != 4 && i != 5) {
                if (sDebug) {
                    String str2 = TAG;
                    Log.w(str2, "Unexpected state in add " + path + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + state);
                }
            } else {
                return;
            }
            if (sDebug) {
                String str3 = TAG;
                Log.i(str3, state + " transitioned to " + obj.getState() + " in op " + op);
            }
        } else {
            obj = addObjectToCache(parent, path, isDir);
            if (obj != null) {
                this.mMtpNotifier.sendObjectAdded(obj.getId());
            } else {
                if (sDebug) {
                    String str4 = TAG;
                    Log.w(str4, "object " + path + " already exists");
                }
                return;
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
                DirectoryStream<Path> stream = Files.newDirectoryStream(obj.getPath());
                for (Path file : stream) {
                    if (sDebug) {
                        Log.i(TAG, "Manually handling event");
                    }
                    handleAddedObject(obj, file.getFileName().toString(), file.toFile().isDirectory());
                }
                try {
                    $closeResource(null, stream);
                } catch (IOException | DirectoryIteratorException e) {
                    Log.e(TAG, e.toString());
                    obj.getObserver().stopWatching();
                    obj.setObserver(null);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: android.mtp.MtpStorageManager$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$android$mtp$MtpStorageManager$MtpObjectState = new int[MtpObjectState.values().length];

        static {
            try {
                $SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[MtpObjectState.FROZEN.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[MtpObjectState.FROZEN_REMOVED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[MtpObjectState.FROZEN_ONESHOT_ADD.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[MtpObjectState.NORMAL.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[MtpObjectState.FROZEN_ADDED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[MtpObjectState.FROZEN_ONESHOT_DEL.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleRemovedObject(MtpObject obj) {
        MtpObjectState state = obj.getState();
        MtpOperation op = obj.getOperation();
        int i = AnonymousClass1.$SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[state.ordinal()];
        boolean z = true;
        if (i == 1) {
            obj.setState(MtpObjectState.FROZEN_REMOVED);
        } else if (i != 4) {
            if (i == 5) {
                obj.setState(MtpObjectState.FROZEN_REMOVED);
            } else if (i != 6) {
                Log.e(TAG, "Got unexpected object remove for " + obj.getName());
            } else {
                if (op == MtpOperation.RENAME) {
                    z = false;
                }
                removeObjectFromCache(obj, z, false);
            }
        } else if (removeObjectFromCache(obj, true, true)) {
            this.mMtpNotifier.sendObjectRemoved(obj.getId());
        }
        if (sDebug) {
            Log.i(TAG, state + " transitioned to " + obj.getState() + " in op " + op);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void handleChangedObject(MtpObject parent, String path) {
        MtpOperation mtpOperation = MtpOperation.NONE;
        MtpObject obj = parent.getChild(path);
        if (obj != null) {
            if (!obj.isDir() && obj.getSize() > 0) {
                obj.mtpObjectEx = null;
                obj.getState();
                obj.getOperation();
                this.mMtpNotifier.sendObjectInfoChanged(obj.getId());
                if (sDebug) {
                    String str = TAG;
                    Log.d(str, "sendObjectInfoChanged: id=" + obj.getId() + ",size=" + obj.getSize());
                }
            }
        } else if (sDebug) {
            String str2 = TAG;
            Log.w(str2, "object " + path + " null");
        }
    }

    public void flushEvents() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }

    public synchronized void dump() {
        for (Integer num : this.mObjects.keySet()) {
            int key = num.intValue();
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
            sb.append(obj.isVisited() ? Telephony.BaseMmsColumns.MMS_VERSION : "nv");
            sb.append(" | ");
            sb.append(obj.getState());
            Log.i(str, sb.toString());
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:115:0x0310, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x0311, code lost:
        if (r4 != null) goto L_0x0313;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0313, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x0316, code lost:
        throw r6;
     */
    public synchronized boolean checkConsistency() {
        boolean ret;
        List<MtpObject> objs = new ArrayList<>();
        objs.addAll(this.mRoots.values());
        objs.addAll(this.mObjects.values());
        ret = true;
        for (MtpObject obj : objs) {
            if (!obj.exists()) {
                if (sDebug) {
                    String str = TAG;
                    Log.w(str, "Object doesn't exist " + obj.getPath() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + obj.getId());
                }
                ret = false;
            }
            if (obj.getState() != MtpObjectState.NORMAL) {
                if (sDebug) {
                    String str2 = TAG;
                    Log.w(str2, "Object " + obj.getPath() + " in state " + obj.getState());
                }
                ret = false;
            }
            if (obj.getOperation() != MtpOperation.NONE) {
                if (sDebug) {
                    String str3 = TAG;
                    Log.w(str3, "Object " + obj.getPath() + " in operation " + obj.getOperation());
                }
                ret = false;
            }
            if (!obj.isRoot() && this.mObjects.get(Integer.valueOf(obj.getId())) != obj) {
                if (sDebug) {
                    String str4 = TAG;
                    Log.w(str4, "Object " + obj.getPath() + " is not in map correctly");
                }
                ret = false;
            }
            if (obj.getParent() != null) {
                if (obj.getParent().isRoot() && obj.getParent() != this.mRoots.get(Integer.valueOf(obj.getParent().getId()))) {
                    if (sDebug) {
                        String str5 = TAG;
                        Log.w(str5, "Root parent is not in root mapping " + obj.getPath());
                    }
                    ret = false;
                }
                if (!obj.getParent().isRoot() && obj.getParent() != this.mObjects.get(Integer.valueOf(obj.getParent().getId()))) {
                    if (sDebug) {
                        String str6 = TAG;
                        Log.w(str6, "Parent is not in object mapping " + obj.getPath());
                    }
                    ret = false;
                }
                if (obj.getParent().getChild(obj.getName()) != obj) {
                    if (sDebug) {
                        String str7 = TAG;
                        Log.w(str7, "Child does not exist in parent " + obj.getPath());
                    }
                    ret = false;
                }
            }
            if (obj.isDir()) {
                if (obj.isVisited() == (obj.getObserver() == null)) {
                    if (sDebug) {
                        String str8 = TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append(obj.getPath());
                        sb.append(" is ");
                        sb.append(obj.isVisited() ? "" : "not ");
                        sb.append(" visited but observer is ");
                        sb.append(obj.getObserver());
                        Log.w(str8, sb.toString());
                    }
                    ret = false;
                }
                if (!obj.isVisited() && obj.getChildren().size() > 0) {
                    if (sDebug) {
                        String str9 = TAG;
                        Log.w(str9, obj.getPath() + " is not visited but has children");
                    }
                    ret = false;
                }
                DirectoryStream<Path> stream = Files.newDirectoryStream(obj.getPath());
                Set<String> files = new HashSet<>();
                for (Path file : stream) {
                    if (obj.isVisited() && obj.getChild(file.getFileName().toString()) == null && (this.mSubdirectories == null || !obj.isRoot() || this.mSubdirectories.contains(file.getFileName().toString()))) {
                        String str10 = TAG;
                        Log.w(str10, "File exists in fs but not in children " + file);
                        ret = false;
                    }
                    files.add(file.toString());
                }
                for (MtpObject child : obj.getChildren()) {
                    if (!files.contains(child.getPath().toString())) {
                        if (sDebug) {
                            String str11 = TAG;
                            Log.w(str11, "File in children doesn't exist in fs " + child.getPath());
                        }
                        ret = false;
                    }
                    if (child != this.mObjects.get(Integer.valueOf(child.getId()))) {
                        if (sDebug) {
                            String str12 = TAG;
                            Log.w(str12, "Child is not in object map " + child.getPath());
                        }
                        ret = false;
                    }
                }
                try {
                    $closeResource(null, stream);
                } catch (IOException | DirectoryIteratorException e) {
                    Log.w(TAG, e.toString());
                    ret = false;
                }
            }
        }
        return ret;
    }

    public synchronized int beginSendObject(MtpObject parent, String name, int format) {
        if (sDebug) {
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
        if (sDebug) {
            String str = TAG;
            Log.v(str, "endSendObject " + succeeded);
        }
        return generalEndAddObject(obj, succeeded, true);
    }

    public synchronized boolean beginRenameObject(MtpObject obj, String newName) {
        if (sDebug) {
            String str = TAG;
            Log.v(str, "beginRenameObject " + obj.getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + newName);
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
        if (sDebug) {
            String str = TAG;
            Log.v(str, "endRenameObject " + success);
        }
        MtpObject parent = obj.getParent();
        oldObj = parent.getChild(oldName);
        if (!success) {
            MtpObjectState oldState = oldObj.getState();
            oldObj.setName(obj.getName());
            oldObj.setState(obj.getState());
            oldObj = obj;
            oldObj.setName(oldName);
            oldObj.setState(oldState);
            obj = oldObj;
            parent.addChild(obj);
            parent.addChild(oldObj);
        }
        return generalEndRenameObject(oldObj, obj, success);
    }

    public synchronized boolean beginRemoveObject(MtpObject obj) {
        if (sDebug) {
            String str = TAG;
            Log.v(str, "beginRemoveObject " + obj.getName());
        }
        return !obj.isRoot() && !isSpecialSubDir(obj) && generalBeginRemoveObject(obj, MtpOperation.DELETE);
    }

    public synchronized boolean endRemoveObject(MtpObject obj, boolean success) {
        boolean z;
        if (sDebug) {
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

    public synchronized boolean beginMoveObject(MtpObject obj, MtpObject newParent) {
        if (sDebug) {
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
            return z;
        }
        MtpObject oldObj = obj.copy(false);
        obj.setParent(newParent);
        oldObj.getParent().addChild(oldObj);
        obj.getParent().addChild(obj);
        return generalBeginRenameObject(oldObj, obj);
    }

    public synchronized boolean endMoveObject(MtpObject oldParent, MtpObject newParent, String name, boolean success) {
        if (sDebug) {
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
                    return z;
                }
                if (!success) {
                    MtpObjectState oldState = oldObj.getState();
                    oldObj.setParent(newObj.getParent());
                    oldObj.setState(newObj.getState());
                    oldObj = newObj;
                    oldObj.setParent(oldParent);
                    oldObj.setState(oldState);
                    newObj = oldObj;
                    newObj.getParent().addChild(newObj);
                    oldParent.addChild(oldObj);
                }
                return generalEndRenameObject(oldObj, newObj, success);
            }
        }
        return false;
    }

    public synchronized int beginCopyObject(MtpObject object, MtpObject newParent) {
        if (sDebug) {
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
        if (sDebug) {
            String str = TAG;
            Log.v(str, "endCopyObject " + object.getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + success);
        }
        return generalEndCopyObject(object, success, false);
    }

    private synchronized boolean generalEndAddObject(MtpObject obj, boolean succeeded, boolean removeGlobal) {
        int i = AnonymousClass1.$SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[obj.getState().ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 5) {
                    return false;
                }
                obj.setState(MtpObjectState.NORMAL);
                if (!succeeded) {
                    MtpObject parent = obj.getParent();
                    if (!removeObjectFromCache(obj, removeGlobal, false)) {
                        return false;
                    }
                    handleAddedObject(parent, obj.getName(), obj.isDir());
                }
            } else if (!removeObjectFromCache(obj, removeGlobal, false)) {
                return false;
            } else {
                if (succeeded) {
                    this.mMtpNotifier.sendObjectRemoved(obj.getId());
                }
            }
        } else if (succeeded) {
            obj.setState(MtpObjectState.FROZEN_ONESHOT_ADD);
        } else if (!removeObjectFromCache(obj, removeGlobal, false)) {
            return false;
        }
        return true;
    }

    private synchronized boolean generalEndRemoveObject(MtpObject obj, boolean success, boolean removeGlobal) {
        int i = AnonymousClass1.$SwitchMap$android$mtp$MtpStorageManager$MtpObjectState[obj.getState().ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i != 5) {
                    return false;
                }
                obj.setState(MtpObjectState.NORMAL);
                if (success) {
                    MtpObject parent = obj.getParent();
                    if (!removeObjectFromCache(obj, removeGlobal, false)) {
                        return false;
                    }
                    handleAddedObject(parent, obj.getName(), obj.isDir());
                }
            } else if (!removeObjectFromCache(obj, removeGlobal, false)) {
                return false;
            } else {
                if (!success) {
                    this.mMtpNotifier.sendObjectRemoved(obj.getId());
                }
            }
        } else if (success) {
            obj.setState(MtpObjectState.FROZEN_ONESHOT_DEL);
        } else {
            obj.setState(MtpObjectState.NORMAL);
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
        boolean z;
        z = true;
        boolean ret = generalEndRemoveObject(fromObj, success, !success);
        if (!generalEndAddObject(toObj, success, success) || !ret) {
            z = false;
        }
        return z;
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
