package android.os.storage;

import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.ActivityThread;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.IPackageMoveObserver;
import android.os.Binder;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IVoldTaskListener;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelableException;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.ProxyFileDescriptorCallback;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.IObbActionListener;
import android.os.storage.IStorageEventListener;
import android.os.storage.IStorageManager;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.telephony.SubscriptionPlan;
import android.text.TextUtils;
import android.util.DataUnit;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import android.view.View;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.AppFuseMount;
import com.android.internal.os.FuseAppLoop;
import com.android.internal.os.FuseUnavailableMountException;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StorageManager {
    public static final String ACTION_MANAGE_STORAGE = "android.os.storage.action.MANAGE_STORAGE";
    public static final int CRYPT_TYPE_DEFAULT = 1;
    public static final int CRYPT_TYPE_PASSWORD = 0;
    public static final int CRYPT_TYPE_PATTERN = 3;
    public static final int CRYPT_TYPE_PIN = 2;
    public static final int DEBUG_ADOPTABLE_FORCE_OFF = 2;
    public static final int DEBUG_ADOPTABLE_FORCE_ON = 1;
    public static final int DEBUG_EMULATE_FBE = 4;
    public static final int DEBUG_SDCARDFS_FORCE_OFF = 16;
    public static final int DEBUG_SDCARDFS_FORCE_ON = 8;
    public static final int DEBUG_VIRTUAL_DISK = 32;
    private static final long DEFAULT_CACHE_MAX_BYTES = DataUnit.GIBIBYTES.toBytes(5);
    private static final int DEFAULT_CACHE_PERCENTAGE = 10;
    private static final long DEFAULT_FULL_THRESHOLD_BYTES = DataUnit.MEBIBYTES.toBytes(1);
    private static final long DEFAULT_THRESHOLD_MAX_BYTES = DataUnit.MEBIBYTES.toBytes(500);
    private static final int DEFAULT_THRESHOLD_PERCENTAGE = 5;
    public static final int ENCRYPTION_STATE_ERROR_CORRUPT = -4;
    public static final int ENCRYPTION_STATE_ERROR_INCOMPLETE = -2;
    public static final int ENCRYPTION_STATE_ERROR_INCONSISTENT = -3;
    public static final int ENCRYPTION_STATE_ERROR_UNKNOWN = -1;
    public static final int ENCRYPTION_STATE_NONE = 1;
    public static final int ENCRYPTION_STATE_OK = 0;
    public static final String EXTRA_REQUESTED_BYTES = "android.os.storage.extra.REQUESTED_BYTES";
    public static final String EXTRA_UUID = "android.os.storage.extra.UUID";
    @SystemApi
    public static final int FLAG_ALLOCATE_AGGRESSIVE = 1;
    public static final int FLAG_ALLOCATE_DEFY_ALL_RESERVED = 2;
    public static final int FLAG_ALLOCATE_DEFY_HALF_RESERVED = 4;
    public static final int FLAG_FOR_WRITE = 256;
    public static final int FLAG_INCLUDE_INVISIBLE = 1024;
    public static final int FLAG_REAL_STATE = 512;
    public static final int FLAG_STORAGE_CE = 2;
    public static final int FLAG_STORAGE_DE = 1;
    public static final int FSTRIM_FLAG_DEEP = 1;
    public static final String OWNER_INFO_KEY = "OwnerInfo";
    public static final String PASSWORD_VISIBLE_KEY = "PasswordVisible";
    public static final String PATTERN_VISIBLE_KEY = "PatternVisible";
    public static final String PROP_ADOPTABLE = "persist.sys.adoptable";
    public static final String PROP_EMULATE_FBE = "persist.sys.emulate_fbe";
    public static final String PROP_HAS_ADOPTABLE = "vold.has_adoptable";
    public static final String PROP_HAS_RESERVED = "vold.has_reserved";
    public static final String PROP_PRIMARY_PHYSICAL = "ro.vold.primary_physical";
    public static final String PROP_SDCARDFS = "persist.sys.sdcardfs";
    public static final String PROP_VIRTUAL_DISK = "persist.sys.virtual_disk";
    public static final String SYSTEM_LOCALE_KEY = "SystemLocale";
    private static final String TAG = "StorageManager";
    public static final UUID UUID_DEFAULT = UUID.fromString("41217664-9172-527a-b3d5-edabb50a7d69");
    public static final String UUID_PRIMARY_PHYSICAL = "primary_physical";
    public static final UUID UUID_PRIMARY_PHYSICAL_ = UUID.fromString("0f95a519-dae7-5abf-9519-fbd6209e05fd");
    public static final String UUID_PRIVATE_INTERNAL = null;
    public static final String UUID_SYSTEM = "system";
    public static final UUID UUID_SYSTEM_ = UUID.fromString("5d258386-e60d-59e3-826d-0089cdd42cc0");
    private static final String XATTR_CACHE_GROUP = "user.cache_group";
    private static final String XATTR_CACHE_TOMBSTONE = "user.cache_tombstone";
    private static volatile IStorageManager sStorageManager = null;
    private final Context mContext;
    private final ArrayList<StorageEventListenerDelegate> mDelegates = new ArrayList<>();
    @GuardedBy("mFuseAppLoopLock")
    private FuseAppLoop mFuseAppLoop = null;
    private final Object mFuseAppLoopLock = new Object();
    /* access modifiers changed from: private */
    public final Looper mLooper;
    private final AtomicInteger mNextNonce = new AtomicInteger(0);
    private final ObbActionListener mObbActionListener = new ObbActionListener();
    private final ContentResolver mResolver;
    private final IStorageManager mStorageManager;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AllocateFlags {
    }

    private class ObbActionListener extends IObbActionListener.Stub {
        private SparseArray<ObbListenerDelegate> mListeners;

        private ObbActionListener() {
            this.mListeners = new SparseArray<>();
        }

        public void onObbResult(String filename, int nonce, int status) {
            ObbListenerDelegate delegate;
            synchronized (this.mListeners) {
                delegate = this.mListeners.get(nonce);
                if (delegate != null) {
                    this.mListeners.remove(nonce);
                }
            }
            if (delegate != null) {
                delegate.sendObbStateChanged(filename, status);
            }
        }

        public int addListener(OnObbStateChangeListener listener) {
            ObbListenerDelegate delegate = new ObbListenerDelegate(listener);
            synchronized (this.mListeners) {
                this.mListeners.put(delegate.nonce, delegate);
            }
            return delegate.nonce;
        }
    }

    private class ObbListenerDelegate {
        private final Handler mHandler;
        private final WeakReference<OnObbStateChangeListener> mObbEventListenerRef;
        /* access modifiers changed from: private */
        public final int nonce;

        ObbListenerDelegate(OnObbStateChangeListener listener) {
            this.nonce = StorageManager.this.getNextNonce();
            this.mObbEventListenerRef = new WeakReference<>(listener);
            this.mHandler = new Handler(StorageManager.this.mLooper, StorageManager.this) {
                public void handleMessage(Message msg) {
                    OnObbStateChangeListener changeListener = ObbListenerDelegate.this.getListener();
                    if (changeListener != null) {
                        changeListener.onObbStateChange((String) msg.obj, msg.arg1);
                    }
                }
            };
        }

        /* access modifiers changed from: package-private */
        public OnObbStateChangeListener getListener() {
            if (this.mObbEventListenerRef == null) {
                return null;
            }
            return (OnObbStateChangeListener) this.mObbEventListenerRef.get();
        }

        /* access modifiers changed from: package-private */
        public void sendObbStateChanged(String path, int state) {
            this.mHandler.obtainMessage(0, state, 0, path).sendToTarget();
        }
    }

    private static class StorageEventListenerDelegate extends IStorageEventListener.Stub implements Handler.Callback {
        private static final int MSG_DISK_DESTROYED = 6;
        private static final int MSG_DISK_SCANNED = 5;
        private static final int MSG_STORAGE_STATE_CHANGED = 1;
        private static final int MSG_VOLUME_FORGOTTEN = 4;
        private static final int MSG_VOLUME_RECORD_CHANGED = 3;
        private static final int MSG_VOLUME_STATE_CHANGED = 2;
        final StorageEventListener mCallback;
        final Handler mHandler;

        public StorageEventListenerDelegate(StorageEventListener callback, Looper looper) {
            this.mCallback = callback;
            this.mHandler = new Handler(looper, (Handler.Callback) this);
        }

        public boolean handleMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            switch (msg.what) {
                case 1:
                    this.mCallback.onStorageStateChanged((String) args.arg1, (String) args.arg2, (String) args.arg3);
                    args.recycle();
                    return true;
                case 2:
                    this.mCallback.onVolumeStateChanged((VolumeInfo) args.arg1, args.argi2, args.argi3);
                    args.recycle();
                    return true;
                case 3:
                    this.mCallback.onVolumeRecordChanged((VolumeRecord) args.arg1);
                    args.recycle();
                    return true;
                case 4:
                    this.mCallback.onVolumeForgotten((String) args.arg1);
                    args.recycle();
                    return true;
                case 5:
                    this.mCallback.onDiskScanned((DiskInfo) args.arg1, args.argi2);
                    args.recycle();
                    return true;
                case 6:
                    this.mCallback.onDiskDestroyed((DiskInfo) args.arg1);
                    args.recycle();
                    return true;
                default:
                    args.recycle();
                    return false;
            }
        }

        public void onUsbMassStorageConnectionChanged(boolean connected) throws RemoteException {
        }

        public void onStorageStateChanged(String path, String oldState, String newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = path;
            args.arg2 = oldState;
            args.arg3 = newState;
            this.mHandler.obtainMessage(1, args).sendToTarget();
        }

        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = vol;
            args.argi2 = oldState;
            args.argi3 = newState;
            this.mHandler.obtainMessage(2, args).sendToTarget();
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = rec;
            this.mHandler.obtainMessage(3, args).sendToTarget();
        }

        public void onVolumeForgotten(String fsUuid) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fsUuid;
            this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        public void onDiskScanned(DiskInfo disk, int volumeCount) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk;
            args.argi2 = volumeCount;
            this.mHandler.obtainMessage(5, args).sendToTarget();
        }

        public void onDiskDestroyed(DiskInfo disk) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk;
            this.mHandler.obtainMessage(6, args).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public int getNextNonce() {
        return this.mNextNonce.getAndIncrement();
    }

    @Deprecated
    public static StorageManager from(Context context) {
        return (StorageManager) context.getSystemService(StorageManager.class);
    }

    public StorageManager(Context context, Looper looper) throws ServiceManager.ServiceNotFoundException {
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mLooper = looper;
        this.mStorageManager = IStorageManager.Stub.asInterface(ServiceManager.getServiceOrThrow("mount"));
    }

    public void registerListener(StorageEventListener listener) {
        synchronized (this.mDelegates) {
            StorageEventListenerDelegate delegate = new StorageEventListenerDelegate(listener, this.mLooper);
            try {
                this.mStorageManager.registerListener(delegate);
                this.mDelegates.add(delegate);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public void unregisterListener(StorageEventListener listener) {
        synchronized (this.mDelegates) {
            Iterator<StorageEventListenerDelegate> i = this.mDelegates.iterator();
            while (i.hasNext()) {
                StorageEventListenerDelegate delegate = i.next();
                if (delegate.mCallback == listener) {
                    try {
                        this.mStorageManager.unregisterListener(delegate);
                        i.remove();
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    @Deprecated
    public void enableUsbMassStorage() {
    }

    @Deprecated
    public void disableUsbMassStorage() {
    }

    @Deprecated
    public boolean isUsbMassStorageConnected() {
        return false;
    }

    @Deprecated
    public boolean isUsbMassStorageEnabled() {
        return false;
    }

    public boolean mountObb(String rawPath, String key, OnObbStateChangeListener listener) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        try {
            String str = rawPath;
            String str2 = key;
            this.mStorageManager.mountObb(str, new File(rawPath).getCanonicalPath(), str2, this.mObbActionListener, this.mObbActionListener.addListener(listener));
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to resolve path: " + rawPath, e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public boolean unmountObb(String rawPath, boolean force, OnObbStateChangeListener listener) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        try {
            this.mStorageManager.unmountObb(rawPath, force, this.mObbActionListener, this.mObbActionListener.addListener(listener));
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isObbMounted(String rawPath) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        try {
            return this.mStorageManager.isObbMounted(rawPath);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getMountedObbPath(String rawPath) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        try {
            return this.mStorageManager.getMountedObbPath(rawPath);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<DiskInfo> getDisks() {
        try {
            return Arrays.asList(this.mStorageManager.getDisks());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public DiskInfo findDiskById(String id) {
        Preconditions.checkNotNull(id);
        for (DiskInfo disk : getDisks()) {
            if (Objects.equals(disk.id, id)) {
                return disk;
            }
        }
        return null;
    }

    public VolumeInfo findVolumeById(String id) {
        Preconditions.checkNotNull(id);
        for (VolumeInfo vol : getVolumes()) {
            if (Objects.equals(vol.id, id)) {
                return vol;
            }
        }
        return null;
    }

    public VolumeInfo findVolumeByUuid(String fsUuid) {
        Preconditions.checkNotNull(fsUuid);
        for (VolumeInfo vol : getVolumes()) {
            if (Objects.equals(vol.fsUuid, fsUuid)) {
                return vol;
            }
        }
        return null;
    }

    public VolumeRecord findRecordByUuid(String fsUuid) {
        Preconditions.checkNotNull(fsUuid);
        for (VolumeRecord rec : getVolumeRecords()) {
            if (Objects.equals(rec.fsUuid, fsUuid)) {
                return rec;
            }
        }
        return null;
    }

    public VolumeInfo findPrivateForEmulated(VolumeInfo emulatedVol) {
        if (emulatedVol != null) {
            return findVolumeById(emulatedVol.getId().replace(VolumeInfo.ID_EMULATED_INTERNAL, VolumeInfo.ID_PRIVATE_INTERNAL));
        }
        return null;
    }

    public VolumeInfo findEmulatedForPrivate(VolumeInfo privateVol) {
        if (privateVol != null) {
            return findVolumeById(privateVol.getId().replace(VolumeInfo.ID_PRIVATE_INTERNAL, VolumeInfo.ID_EMULATED_INTERNAL));
        }
        return null;
    }

    public VolumeInfo findVolumeByQualifiedUuid(String volumeUuid) {
        if (Objects.equals(UUID_PRIVATE_INTERNAL, volumeUuid)) {
            return findVolumeById(VolumeInfo.ID_PRIVATE_INTERNAL);
        }
        if (Objects.equals(UUID_PRIMARY_PHYSICAL, volumeUuid)) {
            return getPrimaryPhysicalVolume();
        }
        return findVolumeByUuid(volumeUuid);
    }

    public UUID getUuidForPath(File path) throws IOException {
        Preconditions.checkNotNull(path);
        String pathString = path.getCanonicalPath();
        if (FileUtils.contains(Environment.getDataDirectory().getAbsolutePath(), pathString)) {
            return UUID_DEFAULT;
        }
        try {
            for (VolumeInfo vol : this.mStorageManager.getVolumes(0)) {
                if (!(vol.path == null || !FileUtils.contains(vol.path, pathString) || vol.type == 0)) {
                    try {
                        return convert(vol.fsUuid);
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
            throw new FileNotFoundException("Failed to find a storage device for " + path);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public File findPathForUuid(String volumeUuid) throws FileNotFoundException {
        VolumeInfo vol = findVolumeByQualifiedUuid(volumeUuid);
        if (vol != null) {
            return vol.getPath();
        }
        throw new FileNotFoundException("Failed to find a storage device for " + volumeUuid);
    }

    public boolean isAllocationSupported(FileDescriptor fd) {
        try {
            getUuidForPath(ParcelFileDescriptor.getFile(fd));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public List<VolumeInfo> getVolumes() {
        try {
            return Arrays.asList(this.mStorageManager.getVolumes(0));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<VolumeInfo> getWritablePrivateVolumes() {
        try {
            ArrayList<VolumeInfo> res = new ArrayList<>();
            for (VolumeInfo vol : this.mStorageManager.getVolumes(0)) {
                if (vol.getType() == 1 && vol.isMountedWritable()) {
                    res.add(vol);
                }
            }
            return res;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<VolumeRecord> getVolumeRecords() {
        try {
            return Arrays.asList(this.mStorageManager.getVolumeRecords(0));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getBestVolumeDescription(VolumeInfo vol) {
        if (vol == null) {
            return null;
        }
        if (!TextUtils.isEmpty(vol.fsUuid)) {
            VolumeRecord rec = findRecordByUuid(vol.fsUuid);
            if (rec != null && !TextUtils.isEmpty(rec.nickname)) {
                return rec.nickname;
            }
        }
        if (!TextUtils.isEmpty(vol.getDescription())) {
            return vol.getDescription();
        }
        if (vol.disk != null) {
            return vol.disk.getDescription();
        }
        return null;
    }

    public VolumeInfo getPrimaryPhysicalVolume() {
        for (VolumeInfo vol : getVolumes()) {
            if (vol.isPrimaryPhysical()) {
                return vol;
            }
        }
        return null;
    }

    public void mount(String volId) {
        try {
            this.mStorageManager.mount(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unmount(String volId) {
        try {
            this.mStorageManager.unmount(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void format(String volId) {
        try {
            this.mStorageManager.format(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public long benchmark(String volId) {
        final CompletableFuture<PersistableBundle> result = new CompletableFuture<>();
        benchmark(volId, new IVoldTaskListener.Stub() {
            public void onStatus(int status, PersistableBundle extras) {
            }

            public void onFinished(int status, PersistableBundle extras) {
                result.complete(extras);
            }
        });
        try {
            return result.get(3, TimeUnit.MINUTES).getLong("run", SubscriptionPlan.BYTES_UNLIMITED) * TimeUtils.NANOS_PER_MS;
        } catch (Exception e) {
            return SubscriptionPlan.BYTES_UNLIMITED;
        }
    }

    public void benchmark(String volId, IVoldTaskListener listener) {
        try {
            this.mStorageManager.benchmark(volId, listener);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void partitionPublic(String diskId) {
        try {
            this.mStorageManager.partitionPublic(diskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void partitionPrivate(String diskId) {
        try {
            this.mStorageManager.partitionPrivate(diskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void partitionMixed(String diskId, int ratio) {
        try {
            this.mStorageManager.partitionMixed(diskId, ratio);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void wipeAdoptableDisks() {
        for (DiskInfo disk : getDisks()) {
            String diskId = disk.getId();
            if (disk.isAdoptable()) {
                Slog.d(TAG, "Found adoptable " + diskId + "; wiping");
                try {
                    this.mStorageManager.partitionPublic(diskId);
                } catch (Exception e) {
                    Slog.w(TAG, "Failed to wipe " + diskId + ", but soldiering onward", e);
                }
            } else {
                Slog.d(TAG, "Ignorning non-adoptable disk " + disk.getId());
            }
        }
    }

    public void setVolumeNickname(String fsUuid, String nickname) {
        try {
            this.mStorageManager.setVolumeNickname(fsUuid, nickname);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setVolumeInited(String fsUuid, boolean inited) {
        try {
            this.mStorageManager.setVolumeUserFlags(fsUuid, inited, 1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setVolumeSnoozed(String fsUuid, boolean snoozed) {
        try {
            this.mStorageManager.setVolumeUserFlags(fsUuid, snoozed ? 2 : 0, 2);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void forgetVolume(String fsUuid) {
        try {
            this.mStorageManager.forgetVolume(fsUuid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getPrimaryStorageUuid() {
        try {
            return this.mStorageManager.getPrimaryStorageUuid();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) {
        try {
            this.mStorageManager.setPrimaryStorageUuid(volumeUuid, callback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public StorageVolume getStorageVolume(File file) {
        return getStorageVolume(getVolumeList(), file);
    }

    public static StorageVolume getStorageVolume(File file, int userId) {
        return getStorageVolume(getVolumeList(userId, 0), file);
    }

    private static StorageVolume getStorageVolume(StorageVolume[] volumes, File file) {
        if (file == null) {
            return null;
        }
        try {
            File file2 = file.getCanonicalFile();
            int length = volumes.length;
            int i = 0;
            while (i < length) {
                StorageVolume volume = volumes[i];
                try {
                    if (FileUtils.contains(volume.getPathFile().getCanonicalFile(), file2)) {
                        return volume;
                    }
                    i++;
                } catch (IOException e) {
                }
            }
            return null;
        } catch (IOException e2) {
            Slog.d(TAG, "Could not get canonical path for " + file);
            return null;
        }
    }

    @Deprecated
    public String getVolumeState(String mountPoint) {
        StorageVolume vol = getStorageVolume(new File(mountPoint));
        if (vol != null) {
            return vol.getState();
        }
        return "unknown";
    }

    public List<StorageVolume> getStorageVolumes() {
        ArrayList<StorageVolume> res = new ArrayList<>();
        Collections.addAll(res, getVolumeList(this.mContext.getUserId(), View.SYSTEM_UI_LAYOUT_FLAGS));
        return res;
    }

    public StorageVolume getPrimaryStorageVolume() {
        return getVolumeList(this.mContext.getUserId(), View.SYSTEM_UI_LAYOUT_FLAGS)[0];
    }

    public static Pair<String, Long> getPrimaryStoragePathAndSize() {
        return Pair.create(null, Long.valueOf(FileUtils.roundStorageSize(Environment.getDataDirectory().getTotalSpace() + Environment.getRootDirectory().getTotalSpace())));
    }

    public long getPrimaryStorageSize() {
        return FileUtils.roundStorageSize(Environment.getDataDirectory().getTotalSpace() + Environment.getRootDirectory().getTotalSpace());
    }

    public void mkdirs(File file) {
        try {
            this.mStorageManager.mkdirs(this.mContext.getOpPackageName(), file.getAbsolutePath());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public StorageVolume[] getVolumeList() {
        return getVolumeList(this.mContext.getUserId(), 0);
    }

    public static StorageVolume[] getVolumeList(int userId, int flags) {
        if (sStorageManager == null) {
            sStorageManager = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        }
        try {
            String packageName = ActivityThread.currentOpPackageName();
            if (packageName == null) {
                String[] packageNames = ActivityThread.getPackageManager().getPackagesForUid(Process.myUid());
                if (packageNames != null) {
                    if (packageNames.length > 0) {
                        packageName = packageNames[0];
                    }
                }
                return new StorageVolume[0];
            }
            return HwFrameworkFactory.getHwApiCacheManagerEx().getVolumeList(sStorageManager, packageName, userId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public String[] getVolumePaths() {
        StorageVolume[] volumes = getVolumeList();
        int count = volumes.length;
        String[] paths = new String[count];
        for (int i = 0; i < count; i++) {
            paths[i] = volumes[i].getPath();
        }
        return paths;
    }

    public StorageVolume getPrimaryVolume() {
        return getPrimaryVolume(getVolumeList());
    }

    public static StorageVolume getPrimaryVolume(StorageVolume[] volumes) {
        for (StorageVolume volume : volumes) {
            if (volume.isPrimary()) {
                return volume;
            }
        }
        throw new IllegalStateException("Missing primary storage");
    }

    public long getStorageBytesUntilLow(File path) {
        return path.getUsableSpace() - getStorageFullBytes(path);
    }

    public long getStorageLowBytes(File path) {
        return Math.min((path.getTotalSpace() * ((long) Settings.Global.getInt(this.mResolver, Settings.Global.SYS_STORAGE_THRESHOLD_PERCENTAGE, 5))) / 100, Settings.Global.getLong(this.mResolver, Settings.Global.SYS_STORAGE_THRESHOLD_MAX_BYTES, DEFAULT_THRESHOLD_MAX_BYTES));
    }

    public long getStorageCacheBytes(File path, int flags) {
        long result = Math.min((path.getTotalSpace() * ((long) Settings.Global.getInt(this.mResolver, Settings.Global.SYS_STORAGE_CACHE_PERCENTAGE, 10))) / 100, Settings.Global.getLong(this.mResolver, Settings.Global.SYS_STORAGE_CACHE_MAX_BYTES, DEFAULT_CACHE_MAX_BYTES));
        if ((flags & 1) != 0 || (flags & 2) != 0) {
            return 0;
        }
        if ((flags & 4) != 0) {
            return result / 2;
        }
        return result;
    }

    public long getStorageFullBytes(File path) {
        return Settings.Global.getLong(this.mResolver, Settings.Global.SYS_STORAGE_FULL_THRESHOLD_BYTES, DEFAULT_FULL_THRESHOLD_BYTES);
    }

    public void createUserKey(int userId, int serialNumber, boolean ephemeral) {
        try {
            this.mStorageManager.createUserKey(userId, serialNumber, ephemeral);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void destroyUserKey(int userId) {
        try {
            this.mStorageManager.destroyUserKey(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            this.mStorageManager.unlockUserKey(userId, serialNumber, token, secret);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void lockUserKey(int userId) {
        try {
            this.mStorageManager.lockUserKey(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) {
        try {
            this.mStorageManager.prepareUserStorage(volumeUuid, userId, serialNumber, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void destroyUserStorage(String volumeUuid, int userId, int flags) {
        try {
            this.mStorageManager.destroyUserStorage(volumeUuid, userId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean isUserKeyUnlocked(int userId) {
        if (sStorageManager == null) {
            sStorageManager = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        }
        if (sStorageManager == null) {
            Slog.w(TAG, "Early during boot, assuming locked");
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            boolean isUserKeyUnlocked = sStorageManager.isUserKeyUnlocked(userId);
            Binder.restoreCallingIdentity(token);
            return isUserKeyUnlocked;
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public boolean isEncrypted(File file) {
        if (FileUtils.contains(Environment.getDataDirectory(), file)) {
            return isEncrypted();
        }
        if (FileUtils.contains(Environment.getExpandDirectory(), file)) {
            return true;
        }
        return false;
    }

    public static boolean isEncryptable() {
        return RoSystemProperties.CRYPTO_ENCRYPTABLE;
    }

    public static boolean isEncrypted() {
        return RoSystemProperties.CRYPTO_ENCRYPTED;
    }

    public static boolean isFileEncryptedNativeOnly() {
        if (!isEncrypted()) {
            return false;
        }
        return RoSystemProperties.CRYPTO_FILE_ENCRYPTED;
    }

    public static boolean isBlockEncrypted() {
        if (!isEncrypted()) {
            return false;
        }
        return RoSystemProperties.CRYPTO_BLOCK_ENCRYPTED;
    }

    public static boolean isNonDefaultBlockEncrypted() {
        boolean z = false;
        if (!isBlockEncrypted()) {
            return false;
        }
        try {
            if (IStorageManager.Stub.asInterface(ServiceManager.getService("mount")).getPasswordType() != 1) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting encryption type");
            return false;
        }
    }

    public static boolean isBlockEncrypting() {
        return !"".equalsIgnoreCase(SystemProperties.get("vold.encrypt_progress", ""));
    }

    public static boolean inCryptKeeperBounce() {
        return "trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"));
    }

    public static boolean isFileEncryptedEmulatedOnly() {
        return SystemProperties.getBoolean(PROP_EMULATE_FBE, false);
    }

    public static boolean isFileEncryptedNativeOrEmulated() {
        return isFileEncryptedNativeOnly() || isFileEncryptedEmulatedOnly();
    }

    public static boolean hasAdoptable() {
        return SystemProperties.getBoolean(PROP_HAS_ADOPTABLE, false);
    }

    public static File maybeTranslateEmulatedPathToInternal(File path) {
        return path;
    }

    @VisibleForTesting
    public ParcelFileDescriptor openProxyFileDescriptor(int mode, ProxyFileDescriptorCallback callback, Handler handler, ThreadFactory factory) throws IOException {
        ParcelFileDescriptor pfd;
        Preconditions.checkNotNull(callback);
        MetricsLogger.count(this.mContext, "storage_open_proxy_file_descriptor", 1);
        while (true) {
            try {
                synchronized (this.mFuseAppLoopLock) {
                    boolean newlyCreated = false;
                    if (this.mFuseAppLoop == null) {
                        AppFuseMount mount = this.mStorageManager.mountProxyFileDescriptorBridge();
                        if (mount != null) {
                            this.mFuseAppLoop = new FuseAppLoop(mount.mountPointId, mount.fd, factory);
                            newlyCreated = true;
                        } else {
                            throw new IOException("Failed to mount proxy bridge");
                        }
                    }
                    if (handler == null) {
                        handler = new Handler(Looper.getMainLooper());
                    }
                    try {
                        int fileId = this.mFuseAppLoop.registerCallback(callback, handler);
                        pfd = this.mStorageManager.openProxyFileDescriptor(this.mFuseAppLoop.getMountPointId(), fileId, mode);
                        if (pfd == null) {
                            this.mFuseAppLoop.unregisterCallback(fileId);
                            throw new FuseUnavailableMountException(this.mFuseAppLoop.getMountPointId());
                        }
                    } catch (FuseUnavailableMountException exception) {
                        if (!newlyCreated) {
                            this.mFuseAppLoop = null;
                        } else {
                            throw new IOException(exception);
                        }
                    }
                }
                return pfd;
            } catch (RemoteException e) {
                throw new IOException(e);
            }
        }
    }

    public ParcelFileDescriptor openProxyFileDescriptor(int mode, ProxyFileDescriptorCallback callback) throws IOException {
        return openProxyFileDescriptor(mode, callback, null, null);
    }

    public ParcelFileDescriptor openProxyFileDescriptor(int mode, ProxyFileDescriptorCallback callback, Handler handler) throws IOException {
        Preconditions.checkNotNull(handler);
        return openProxyFileDescriptor(mode, callback, handler, null);
    }

    @VisibleForTesting
    public int getProxyFileDescriptorMountPointId() {
        int mountPointId;
        synchronized (this.mFuseAppLoopLock) {
            mountPointId = this.mFuseAppLoop != null ? this.mFuseAppLoop.getMountPointId() : -1;
        }
        return mountPointId;
    }

    public long getCacheQuotaBytes(UUID storageUuid) throws IOException {
        try {
            return this.mStorageManager.getCacheQuotaBytes(convert(storageUuid), this.mContext.getApplicationInfo().uid);
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public long getCacheSizeBytes(UUID storageUuid) throws IOException {
        try {
            return this.mStorageManager.getCacheSizeBytes(convert(storageUuid), this.mContext.getApplicationInfo().uid);
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public long getAllocatableBytes(UUID storageUuid) throws IOException {
        return getAllocatableBytes(storageUuid, 0);
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public long getAllocatableBytes(UUID storageUuid, int flags) throws IOException {
        try {
            return this.mStorageManager.getAllocatableBytes(convert(storageUuid), flags, this.mContext.getOpPackageName());
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
            throw new RuntimeException(e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public void allocateBytes(UUID storageUuid, long bytes) throws IOException {
        allocateBytes(storageUuid, bytes, 0);
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public void allocateBytes(UUID storageUuid, long bytes, int flags) throws IOException {
        try {
            this.mStorageManager.allocateBytes(convert(storageUuid), bytes, flags, this.mContext.getOpPackageName());
        } catch (ParcelableException e) {
            e.maybeRethrow(IOException.class);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public void allocateBytes(FileDescriptor fd, long bytes) throws IOException {
        allocateBytes(fd, bytes, 0);
    }

    @SuppressLint({"Doclava125"})
    @SystemApi
    public void allocateBytes(FileDescriptor fd, long bytes, int flags) throws IOException {
        File file = ParcelFileDescriptor.getFile(fd);
        UUID uuid = getUuidForPath(file);
        int i = 0;
        while (i < 3) {
            try {
                long needBytes = bytes - (Os.fstat(fd).st_blocks * 512);
                if (needBytes > 0) {
                    allocateBytes(uuid, needBytes, flags);
                }
                Os.posix_fallocate(fd, 0, bytes);
                return;
            } catch (ErrnoException e) {
                if (e.errno != OsConstants.ENOSYS) {
                    if (e.errno != OsConstants.ENOTSUP) {
                        throw e;
                    }
                }
                Log.w(TAG, "fallocate() not supported; falling back to ftruncate()");
                Os.ftruncate(fd, bytes);
                return;
            } catch (ErrnoException e2) {
                if (e2.errno == OsConstants.ENOSPC) {
                    Log.w(TAG, "Odd, not enough space; let's try again?");
                    i++;
                } else {
                    throw e2.rethrowAsIOException();
                }
            }
        }
        throw new IOException("Well this is embarassing; we can't allocate " + bytes + " for " + file);
    }

    private static void setCacheBehavior(File path, String name, boolean enabled) throws IOException {
        if (!path.isDirectory()) {
            throw new IOException("Cache behavior can only be set on directories");
        } else if (enabled) {
            try {
                Os.setxattr(path.getAbsolutePath(), name, "1".getBytes(StandardCharsets.UTF_8), 0);
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        } else {
            try {
                Os.removexattr(path.getAbsolutePath(), name);
            } catch (ErrnoException e2) {
                if (e2.errno != OsConstants.ENODATA) {
                    throw e2.rethrowAsIOException();
                }
            }
        }
    }

    private static boolean isCacheBehavior(File path, String name) throws IOException {
        try {
            Os.getxattr(path.getAbsolutePath(), name);
            return true;
        } catch (ErrnoException e) {
            if (e.errno == OsConstants.ENODATA) {
                return false;
            }
            throw e.rethrowAsIOException();
        }
    }

    public void setCacheBehaviorGroup(File path, boolean group) throws IOException {
        setCacheBehavior(path, XATTR_CACHE_GROUP, group);
    }

    public boolean isCacheBehaviorGroup(File path) throws IOException {
        return isCacheBehavior(path, XATTR_CACHE_GROUP);
    }

    public void setCacheBehaviorTombstone(File path, boolean tombstone) throws IOException {
        setCacheBehavior(path, XATTR_CACHE_TOMBSTONE, tombstone);
    }

    public boolean isCacheBehaviorTombstone(File path) throws IOException {
        return isCacheBehavior(path, XATTR_CACHE_TOMBSTONE);
    }

    public static UUID convert(String uuid) {
        if (Objects.equals(uuid, UUID_PRIVATE_INTERNAL)) {
            return UUID_DEFAULT;
        }
        if (Objects.equals(uuid, UUID_PRIMARY_PHYSICAL)) {
            return UUID_PRIMARY_PHYSICAL_;
        }
        if (Objects.equals(uuid, UUID_SYSTEM)) {
            return UUID_SYSTEM_;
        }
        return UUID.fromString(uuid);
    }

    public static String convert(UUID storageUuid) {
        if (UUID_DEFAULT.equals(storageUuid)) {
            return UUID_PRIVATE_INTERNAL;
        }
        if (UUID_PRIMARY_PHYSICAL_.equals(storageUuid)) {
            return UUID_PRIMARY_PHYSICAL;
        }
        if (UUID_SYSTEM_.equals(storageUuid)) {
            return UUID_SYSTEM;
        }
        return storageUuid.toString();
    }

    public int startClean() {
        try {
            return this.mStorageManager.startClean();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int stopClean() {
        try {
            return this.mStorageManager.stopClean();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getNotificationLevel() {
        try {
            return this.mStorageManager.getNotificationLevel();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getUndiscardInfo() {
        try {
            return this.mStorageManager.getUndiscardInfo();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMaxTimeCost() {
        try {
            return this.mStorageManager.getMaxTimeCost();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMinTimeCost() {
        try {
            return this.mStorageManager.getMinTimeCost();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPercentComplete() {
        try {
            return this.mStorageManager.getPercentComplete();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unlockUserKeyISec(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            this.mStorageManager.unlockUserKeyISec(userId, serialNumber, token, secret);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void lockUserKeyISec(int userId) {
        try {
            this.mStorageManager.lockUserKeyISec(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unlockUserScreenISec(int userId, int serialNumber, byte[] token, byte[] secret, int type) {
        try {
            this.mStorageManager.unlockUserScreenISec(userId, serialNumber, token, secret, type);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void lockUserScreenISec(int userId, int serialNumber) {
        try {
            this.mStorageManager.lockUserScreenISec(userId, serialNumber);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getPreLoadPolicyFlag(int userId, int serialNumber) {
        try {
            return this.mStorageManager.getPreLoadPolicyFlag(userId, serialNumber);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean setScreenStateFlag(int userId, int serialNumber, int flag) {
        try {
            return this.mStorageManager.setScreenStateFlag(userId, serialNumber, flag);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getKeyDesc(int userId, int serialNumber, int sdpClass) {
        try {
            return this.mStorageManager.getKeyDesc(userId, serialNumber, sdpClass);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void createUserKeyISec(int userId, int serialNumber, boolean ephemeral) {
        try {
            this.mStorageManager.createUserKeyISec(userId, serialNumber, ephemeral);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void destroyUserKeyISec(int userId) {
        try {
            this.mStorageManager.destroyUserKeyISec(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
