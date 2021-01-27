package android.os.storage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.common.HwFrameworkFactory;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.IPackageMoveObserver;
import android.content.res.ObbInfo;
import android.content.res.ObbScanner;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
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
import android.provider.MediaStore;
import android.provider.Settings;
import android.sysprop.VoldProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.DataUnit;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.AppFuseMount;
import com.android.internal.os.FuseAppLoop;
import com.android.internal.os.FuseUnavailableMountException;
import com.android.internal.os.RoSystemProperties;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Preconditions;
import dalvik.system.BlockGuard;
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
    @UnsupportedAppUsage
    public static final int CRYPT_TYPE_DEFAULT = 1;
    @UnsupportedAppUsage
    public static final int CRYPT_TYPE_PASSWORD = 0;
    public static final int CRYPT_TYPE_PATTERN = 2;
    public static final int CRYPT_TYPE_PIN = 3;
    public static final int DEBUG_ADOPTABLE_FORCE_OFF = 2;
    public static final int DEBUG_ADOPTABLE_FORCE_ON = 1;
    public static final int DEBUG_EMULATE_FBE = 4;
    public static final int DEBUG_ISOLATED_STORAGE_FORCE_OFF = 128;
    public static final int DEBUG_ISOLATED_STORAGE_FORCE_ON = 64;
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
    @UnsupportedAppUsage
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
    public static final int FLAG_STORAGE_EXTERNAL = 4;
    public static final int FSTRIM_FLAG_DEEP = 1;
    private static final boolean LOCAL_LOGV = Log.isLoggable(TAG, 2);
    public static final String OWNER_INFO_KEY = "OwnerInfo";
    public static final String PASSWORD_VISIBLE_KEY = "PasswordVisible";
    public static final String PATTERN_VISIBLE_KEY = "PatternVisible";
    public static final String PROP_ADOPTABLE = "persist.sys.adoptable";
    public static final String PROP_EMULATE_FBE = "persist.sys.emulate_fbe";
    public static final String PROP_HAS_ADOPTABLE = "vold.has_adoptable";
    public static final String PROP_HAS_RESERVED = "vold.has_reserved";
    public static final String PROP_ISOLATED_STORAGE = "persist.sys.isolated_storage";
    public static final String PROP_ISOLATED_STORAGE_SNAPSHOT = "sys.isolated_storage_snapshot";
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
    private final AppOpsManager mAppOps;
    private final Context mContext;
    private final ArrayList<StorageEventListenerDelegate> mDelegates = new ArrayList<>();
    @GuardedBy({"mFuseAppLoopLock"})
    private FuseAppLoop mFuseAppLoop = null;
    private final Object mFuseAppLoopLock = new Object();
    private final Looper mLooper;
    private final AtomicInteger mNextNonce = new AtomicInteger(0);
    private final ObbActionListener mObbActionListener = new ObbActionListener();
    private final ContentResolver mResolver;
    private final IStorageManager mStorageManager;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AllocateFlags {
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
            this.mHandler = new Handler(looper, this);
        }

        @Override // android.os.Handler.Callback
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

        @Override // android.os.storage.IStorageEventListener
        public void onUsbMassStorageConnectionChanged(boolean connected) throws RemoteException {
        }

        @Override // android.os.storage.IStorageEventListener
        public void onStorageStateChanged(String path, String oldState, String newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = path;
            args.arg2 = oldState;
            args.arg3 = newState;
            this.mHandler.obtainMessage(1, args).sendToTarget();
        }

        @Override // android.os.storage.IStorageEventListener
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = vol;
            args.argi2 = oldState;
            args.argi3 = newState;
            this.mHandler.obtainMessage(2, args).sendToTarget();
        }

        @Override // android.os.storage.IStorageEventListener
        public void onVolumeRecordChanged(VolumeRecord rec) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = rec;
            this.mHandler.obtainMessage(3, args).sendToTarget();
        }

        @Override // android.os.storage.IStorageEventListener
        public void onVolumeForgotten(String fsUuid) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fsUuid;
            this.mHandler.obtainMessage(4, args).sendToTarget();
        }

        @Override // android.os.storage.IStorageEventListener
        public void onDiskScanned(DiskInfo disk, int volumeCount) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk;
            args.argi2 = volumeCount;
            this.mHandler.obtainMessage(5, args).sendToTarget();
        }

        @Override // android.os.storage.IStorageEventListener
        public void onDiskDestroyed(DiskInfo disk) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk;
            this.mHandler.obtainMessage(6, args).sendToTarget();
        }
    }

    private class ObbActionListener extends IObbActionListener.Stub {
        private SparseArray<ObbListenerDelegate> mListeners;

        private ObbActionListener() {
            this.mListeners = new SparseArray<>();
        }

        @Override // android.os.storage.IObbActionListener
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getNextNonce() {
        return this.mNextNonce.getAndIncrement();
    }

    /* access modifiers changed from: private */
    public class ObbListenerDelegate {
        private final Handler mHandler;
        private final WeakReference<OnObbStateChangeListener> mObbEventListenerRef;
        private final int nonce;

        ObbListenerDelegate(OnObbStateChangeListener listener) {
            this.nonce = StorageManager.this.getNextNonce();
            this.mObbEventListenerRef = new WeakReference<>(listener);
            this.mHandler = new Handler(StorageManager.this.mLooper, StorageManager.this) {
                /* class android.os.storage.StorageManager.ObbListenerDelegate.AnonymousClass1 */

                @Override // android.os.Handler
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
            WeakReference<OnObbStateChangeListener> weakReference = this.mObbEventListenerRef;
            if (weakReference == null) {
                return null;
            }
            return weakReference.get();
        }

        /* access modifiers changed from: package-private */
        public void sendObbStateChanged(String path, int state) {
            this.mHandler.obtainMessage(0, state, 0, path).sendToTarget();
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public static StorageManager from(Context context) {
        return (StorageManager) context.getSystemService(StorageManager.class);
    }

    @UnsupportedAppUsage
    public StorageManager(Context context, Looper looper) throws ServiceManager.ServiceNotFoundException {
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mLooper = looper;
        this.mStorageManager = IStorageManager.Stub.asInterface(ServiceManager.getServiceOrThrow("mount"));
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    @Deprecated
    public void enableUsbMassStorage() {
    }

    @UnsupportedAppUsage
    @Deprecated
    public void disableUsbMassStorage() {
    }

    @UnsupportedAppUsage
    @Deprecated
    public boolean isUsbMassStorageConnected() {
        return false;
    }

    @UnsupportedAppUsage
    @Deprecated
    public boolean isUsbMassStorageEnabled() {
        return false;
    }

    public boolean mountObb(String rawPath, String key, OnObbStateChangeListener listener) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(listener, "listener cannot be null");
        try {
            String canonicalPath = new File(rawPath).getCanonicalPath();
            this.mStorageManager.mountObb(rawPath, canonicalPath, key, this.mObbActionListener, this.mObbActionListener.addListener(listener), getObbInfo(canonicalPath));
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to resolve path: " + rawPath, e);
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    private ObbInfo getObbInfo(String canonicalPath) {
        try {
            return ObbScanner.getObbInfo(canonicalPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't get OBB info for " + canonicalPath, e);
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

    @UnsupportedAppUsage
    public List<DiskInfo> getDisks() {
        try {
            return Arrays.asList(this.mStorageManager.getDisks());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public DiskInfo findDiskById(String id) {
        Preconditions.checkNotNull(id);
        for (DiskInfo disk : getDisks()) {
            if (Objects.equals(disk.id, id)) {
                return disk;
            }
        }
        return null;
    }

    @UnsupportedAppUsage
    public VolumeInfo findVolumeById(String id) {
        Preconditions.checkNotNull(id);
        for (VolumeInfo vol : getVolumes()) {
            if (Objects.equals(vol.id, id)) {
                return vol;
            }
        }
        return null;
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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
            VolumeInfo[] volumes = this.mStorageManager.getVolumes(0);
            int length = volumes.length;
            for (int i = 0; i < length; i++) {
                VolumeInfo vol = volumes[i];
                if (!(vol.path == null || !FileUtils.contains(vol.path, pathString) || vol.type == 0 || vol.type == 5)) {
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

    @UnsupportedAppUsage
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
            VolumeInfo[] volumes = this.mStorageManager.getVolumes(0);
            for (VolumeInfo vol : volumes) {
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

    @UnsupportedAppUsage
    public String getBestVolumeDescription(VolumeInfo vol) {
        VolumeRecord rec;
        if (vol == null) {
            return null;
        }
        if (!TextUtils.isEmpty(vol.fsUuid) && (rec = findRecordByUuid(vol.fsUuid)) != null && !TextUtils.isEmpty(rec.nickname)) {
            return rec.nickname;
        }
        if (!TextUtils.isEmpty(vol.getDescription())) {
            return vol.getDescription();
        }
        if (vol.disk != null) {
            return vol.disk.getDescription();
        }
        return null;
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void unmount(String volId) {
        try {
            this.mStorageManager.unmount(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
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
            /* class android.os.storage.StorageManager.AnonymousClass1 */

            @Override // android.os.IVoldTaskListener
            public void onStatus(int status, PersistableBundle extras) {
            }

            @Override // android.os.IVoldTaskListener
            public void onFinished(int status, PersistableBundle extras) {
                result.complete(extras);
            }
        });
        try {
            return result.get(3, TimeUnit.MINUTES).getLong("run", Long.MAX_VALUE) * TimeUtils.NANOS_PER_MS;
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    public void benchmark(String volId, IVoldTaskListener listener) {
        try {
            this.mStorageManager.benchmark(volId, listener);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
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
            this.mStorageManager.setVolumeUserFlags(fsUuid, inited ? 1 : 0, 1);
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

    public StorageVolume getStorageVolume(Uri uri) {
        String volumeName = MediaStore.getVolumeName(uri);
        if (((volumeName.hashCode() == -1921573490 && volumeName.equals(MediaStore.VOLUME_EXTERNAL_PRIMARY)) ? (char) 0 : 65535) == 0) {
            return getPrimaryStorageVolume();
        }
        for (StorageVolume vol : getStorageVolumes()) {
            if (Objects.equals(vol.getNormalizedUuid(), volumeName)) {
                return vol;
            }
        }
        throw new IllegalStateException("Unknown volume for " + uri);
    }

    public static StorageVolume getStorageVolume(File file, int userId) {
        return getStorageVolume(getVolumeList(userId, 0), file);
    }

    @UnsupportedAppUsage
    private static StorageVolume getStorageVolume(StorageVolume[] volumes, File file) {
        if (file == null) {
            return null;
        }
        String path = file.getAbsolutePath();
        if (path.startsWith(ContentResolver.DEPRECATE_DATA_PREFIX)) {
            return ((StorageManager) AppGlobals.getInitialApplication().getSystemService(StorageManager.class)).getStorageVolume(ContentResolver.translateDeprecatedDataPath(path));
        }
        try {
            File file2 = file.getCanonicalFile();
            for (StorageVolume volume : volumes) {
                try {
                    if (FileUtils.contains(volume.getPathFile().getCanonicalFile(), file2)) {
                        return volume;
                    }
                } catch (IOException e) {
                }
            }
            return null;
        } catch (IOException e2) {
            Slog.d(TAG, "Could not get canonical path for " + file);
            return null;
        }
    }

    @UnsupportedAppUsage
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
        Collections.addAll(res, getVolumeList(this.mContext.getUserId(), 1536));
        return res;
    }

    public StorageVolume getPrimaryStorageVolume() {
        return getVolumeList(this.mContext.getUserId(), 1536)[0];
    }

    public static Pair<String, Long> getPrimaryStoragePathAndSize() {
        return Pair.create(null, Long.valueOf(FileUtils.roundStorageSize(Environment.getDataDirectory().getTotalSpace() + Environment.getRootDirectory().getTotalSpace())));
    }

    public long getPrimaryStorageSize() {
        return FileUtils.roundStorageSize(Environment.getDataDirectory().getTotalSpace() + Environment.getRootDirectory().getTotalSpace());
    }

    public void mkdirs(File file) {
        BlockGuard.getVmPolicy().onPathAccess(file.getAbsolutePath());
        try {
            this.mStorageManager.mkdirs(this.mContext.getOpPackageName(), file.getAbsolutePath());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public StorageVolume[] getVolumeList() {
        return getVolumeList(this.mContext.getUserId(), 0);
    }

    @UnsupportedAppUsage
    public static StorageVolume[] getVolumeList(int userId, int flags) {
        if (sStorageManager == null) {
            sStorageManager = IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
        }
        try {
            String packageName = ActivityThread.currentOpPackageName();
            if (packageName == null) {
                String[] packageNames = ActivityThread.getPackageManager().getPackagesForUid(Process.myUid());
                if (packageNames == null || packageNames.length <= 0) {
                    Log.i(TAG, "Process.myUid = " + Process.myUid() + ",packageNames is null or packageNames.length <= 0 ,return.");
                    return new StorageVolume[0];
                }
                packageName = packageNames[0];
            }
            StorageVolume[] storageVolumes = HwFrameworkFactory.getHwApiCacheManagerEx().getVolumeList(sStorageManager, packageName, userId, flags);
            if ((storageVolumes == null || storageVolumes.length <= 0) && Log.HWINFO) {
                Log.i(TAG, "packageName = " + packageName + ",userId = " + userId + ",flags = " + flags + ",storageVolumes is null or storageVolumes.length <= 0 ,return.");
            }
            return storageVolumes;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public long getStorageBytesUntilLow(File path) {
        return path.getUsableSpace() - getStorageFullBytes(path);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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
        if (!isBlockEncrypted()) {
            return false;
        }
        try {
            if (IStorageManager.Stub.asInterface(ServiceManager.getService("mount")).getPasswordType() != 1) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting encryption type");
            return false;
        }
    }

    public static boolean isBlockEncrypting() {
        return !"".equalsIgnoreCase(VoldProperties.encrypt_progress().orElse(""));
    }

    public static boolean inCryptKeeperBounce() {
        return "trigger_restart_min_framework".equals(VoldProperties.decrypt().orElse(""));
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

    @SystemApi
    public static boolean hasIsolatedStorage() {
        return SystemProperties.getBoolean(PROP_ISOLATED_STORAGE_SNAPSHOT, SystemProperties.getBoolean(PROP_ISOLATED_STORAGE, true));
    }

    @Deprecated
    public static File maybeTranslateEmulatedPathToInternal(File path) {
        return path;
    }

    public File translateAppToSystem(File file, int pid, int uid) {
        return file;
    }

    public File translateSystemToApp(File file, int pid, int uid) {
        return file;
    }

    public static boolean checkPermissionAndAppOp(Context context, boolean enforce, int pid, int uid, String packageName, String permission, int op) {
        return checkPermissionAndAppOp(context, enforce, pid, uid, packageName, permission, op, true);
    }

    public static boolean checkPermissionAndCheckOp(Context context, boolean enforce, int pid, int uid, String packageName, String permission, int op) {
        return checkPermissionAndAppOp(context, enforce, pid, uid, packageName, permission, op, false);
    }

    private static boolean checkPermissionAndAppOp(Context context, boolean enforce, int pid, int uid, String packageName, String permission, int op, boolean note) {
        int mode;
        if (context.checkPermission(permission, pid, uid) == 0) {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(AppOpsManager.class);
            if (note) {
                mode = appOps.noteOpNoThrow(op, uid, packageName);
            } else {
                try {
                    appOps.checkPackage(uid, packageName);
                    mode = appOps.checkOpNoThrow(op, uid, packageName);
                } catch (SecurityException e) {
                    if (!enforce) {
                        return false;
                    }
                    throw e;
                }
            }
            if (mode == 0) {
                return true;
            }
            if (mode != 1 && mode != 2 && mode != 3) {
                throw new IllegalStateException(AppOpsManager.opToName(op) + " has unknown mode " + AppOpsManager.modeToName(mode));
            } else if (!enforce) {
                return false;
            } else {
                throw new SecurityException("Op " + AppOpsManager.opToName(op) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + AppOpsManager.modeToName(mode) + " for package " + packageName);
            }
        } else if (!enforce) {
            return false;
        } else {
            throw new SecurityException("Permission " + permission + " denied for package " + packageName);
        }
    }

    private boolean checkPermissionAndAppOp(boolean enforce, int pid, int uid, String packageName, String permission, int op) {
        return checkPermissionAndAppOp(this.mContext, enforce, pid, uid, packageName, permission, op);
    }

    private boolean noteAppOpAllowingLegacy(boolean enforce, int pid, int uid, String packageName, int op) {
        int mode = this.mAppOps.noteOpNoThrow(op, uid, packageName);
        if (mode == 0) {
            return true;
        }
        if (mode != 1 && mode != 2 && mode != 3) {
            throw new IllegalStateException(AppOpsManager.opToName(op) + " has unknown mode " + AppOpsManager.modeToName(mode));
        } else if (this.mAppOps.checkOpNoThrow(87, uid, packageName) == 0) {
            return true;
        } else {
            if (!enforce) {
                return false;
            }
            throw new SecurityException("Op " + AppOpsManager.opToName(op) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + AppOpsManager.modeToName(mode) + " for package " + packageName);
        }
    }

    public boolean checkPermissionReadAudio(boolean enforce, int pid, int uid, String packageName) {
        if (!checkPermissionAndAppOp(enforce, pid, uid, packageName, Manifest.permission.READ_EXTERNAL_STORAGE, 59)) {
            return false;
        }
        return noteAppOpAllowingLegacy(enforce, pid, uid, packageName, 81);
    }

    public boolean checkPermissionWriteAudio(boolean enforce, int pid, int uid, String packageName) {
        if (!checkPermissionAndAppOp(enforce, pid, uid, packageName, Manifest.permission.WRITE_EXTERNAL_STORAGE, 60)) {
            return false;
        }
        return noteAppOpAllowingLegacy(enforce, pid, uid, packageName, 82);
    }

    public boolean checkPermissionReadVideo(boolean enforce, int pid, int uid, String packageName) {
        if (!checkPermissionAndAppOp(enforce, pid, uid, packageName, Manifest.permission.READ_EXTERNAL_STORAGE, 59)) {
            return false;
        }
        return noteAppOpAllowingLegacy(enforce, pid, uid, packageName, 83);
    }

    public boolean checkPermissionWriteVideo(boolean enforce, int pid, int uid, String packageName) {
        if (!checkPermissionAndAppOp(enforce, pid, uid, packageName, Manifest.permission.WRITE_EXTERNAL_STORAGE, 60)) {
            return false;
        }
        return noteAppOpAllowingLegacy(enforce, pid, uid, packageName, 84);
    }

    public boolean checkPermissionReadImages(boolean enforce, int pid, int uid, String packageName) {
        if (!checkPermissionAndAppOp(enforce, pid, uid, packageName, Manifest.permission.READ_EXTERNAL_STORAGE, 59)) {
            return false;
        }
        return noteAppOpAllowingLegacy(enforce, pid, uid, packageName, 85);
    }

    public boolean checkPermissionWriteImages(boolean enforce, int pid, int uid, String packageName) {
        if (!checkPermissionAndAppOp(enforce, pid, uid, packageName, Manifest.permission.WRITE_EXTERNAL_STORAGE, 60)) {
            return false;
        }
        return noteAppOpAllowingLegacy(enforce, pid, uid, packageName, 86);
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

    @SystemApi
    @SuppressLint({"Doclava125"})
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

    @SystemApi
    @SuppressLint({"Doclava125"})
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

    @SystemApi
    @SuppressLint({"Doclava125"})
    public void allocateBytes(FileDescriptor fd, long bytes, int flags) throws IOException {
        File file = ParcelFileDescriptor.getFile(fd);
        UUID uuid = getUuidForPath(file);
        for (int i = 0; i < 3; i++) {
            try {
                long needBytes = bytes - (Os.fstat(fd).st_blocks * 512);
                if (needBytes > 0) {
                    allocateBytes(uuid, needBytes, flags);
                }
                try {
                    Os.posix_fallocate(fd, 0, bytes);
                    return;
                } catch (ErrnoException e) {
                    if (e.errno == OsConstants.ENOSYS || e.errno == OsConstants.ENOTSUP) {
                        Log.w(TAG, "fallocate() not supported; falling back to ftruncate()");
                        Os.ftruncate(fd, bytes);
                        return;
                    }
                    throw e;
                }
            } catch (ErrnoException e2) {
                if (e2.errno == OsConstants.ENOSPC) {
                    Log.w(TAG, "Odd, not enough space; let's try again?");
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
        if (Objects.equals(uuid, "system")) {
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
            return "system";
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
