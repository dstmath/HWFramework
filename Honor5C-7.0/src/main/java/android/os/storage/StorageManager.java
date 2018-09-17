package android.os.storage;

import android.app.ActivityThread;
import android.bluetooth.BluetoothClass.Device.Major;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.IPackageMoveObserver;
import android.net.ProxyInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.IObbActionListener.Stub;
import android.provider.Settings.Global;
import android.security.keymaster.KeymasterDefs;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.Preconditions;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class StorageManager {
    public static final int CRYPT_TYPE_DEFAULT = 1;
    public static final int CRYPT_TYPE_PASSWORD = 0;
    public static final int CRYPT_TYPE_PATTERN = 2;
    public static final int CRYPT_TYPE_PIN = 3;
    public static final int DEBUG_EMULATE_FBE = 2;
    public static final int DEBUG_FORCE_ADOPTABLE = 1;
    public static final int DEBUG_SDCARDFS_FORCE_OFF = 8;
    public static final int DEBUG_SDCARDFS_FORCE_ON = 4;
    private static final long DEFAULT_FULL_THRESHOLD_BYTES = 1048576;
    private static final long DEFAULT_THRESHOLD_MAX_BYTES = 524288000;
    private static final int DEFAULT_THRESHOLD_PERCENTAGE = 10;
    public static final int FLAG_FOR_WRITE = 256;
    public static final int FLAG_INCLUDE_INVISIBLE = 1024;
    public static final int FLAG_REAL_STATE = 512;
    public static final int FLAG_STORAGE_CE = 2;
    public static final int FLAG_STORAGE_DE = 1;
    public static final String OWNER_INFO_KEY = "OwnerInfo";
    public static final String PASSWORD_VISIBLE_KEY = "PasswordVisible";
    public static final String PATTERN_VISIBLE_KEY = "PatternVisible";
    public static final String PROP_EMULATE_FBE = "persist.sys.emulate_fbe";
    public static final String PROP_FORCE_ADOPTABLE = "persist.fw.force_adoptable";
    public static final String PROP_HAS_ADOPTABLE = "vold.has_adoptable";
    public static final String PROP_PRIMARY_PHYSICAL = "ro.vold.primary_physical";
    public static final String PROP_SDCARDFS = "persist.sys.sdcardfs";
    public static final String SYSTEM_LOCALE_KEY = "SystemLocale";
    private static final String TAG = "StorageManager";
    public static final String UUID_PRIMARY_PHYSICAL = "primary_physical";
    public static final String UUID_PRIVATE_INTERNAL = null;
    private static volatile IMountService sMountService;
    private final Context mContext;
    private final ArrayList<StorageEventListenerDelegate> mDelegates;
    private final Looper mLooper;
    private final IMountService mMountService;
    private final AtomicInteger mNextNonce;
    private final ObbActionListener mObbActionListener;
    private final ContentResolver mResolver;

    private class ObbActionListener extends Stub {
        private SparseArray<ObbListenerDelegate> mListeners;

        private ObbActionListener() {
            this.mListeners = new SparseArray();
        }

        public void onObbResult(String filename, int nonce, int status) {
            synchronized (this.mListeners) {
                ObbListenerDelegate delegate = (ObbListenerDelegate) this.mListeners.get(nonce);
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
        private final int nonce;

        /* renamed from: android.os.storage.StorageManager.ObbListenerDelegate.1 */
        class AnonymousClass1 extends Handler {
            AnonymousClass1(Looper $anonymous0) {
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                OnObbStateChangeListener changeListener = ObbListenerDelegate.this.getListener();
                if (changeListener != null) {
                    changeListener.onObbStateChange((String) msg.obj, msg.arg1);
                }
            }
        }

        ObbListenerDelegate(OnObbStateChangeListener listener) {
            this.nonce = StorageManager.this.getNextNonce();
            this.mObbEventListenerRef = new WeakReference(listener);
            this.mHandler = new AnonymousClass1(StorageManager.this.mLooper);
        }

        OnObbStateChangeListener getListener() {
            if (this.mObbEventListenerRef == null) {
                return null;
            }
            return (OnObbStateChangeListener) this.mObbEventListenerRef.get();
        }

        void sendObbStateChanged(String path, int state) {
            this.mHandler.obtainMessage(StorageManager.CRYPT_TYPE_PASSWORD, state, StorageManager.CRYPT_TYPE_PASSWORD, path).sendToTarget();
        }
    }

    private static class StorageEventListenerDelegate extends IMountServiceListener.Stub implements Callback {
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
            this.mHandler = new Handler(looper, (Callback) this);
        }

        public boolean handleMessage(Message msg) {
            SomeArgs args = msg.obj;
            switch (msg.what) {
                case MSG_STORAGE_STATE_CHANGED /*1*/:
                    this.mCallback.onStorageStateChanged((String) args.arg1, (String) args.arg2, (String) args.arg3);
                    args.recycle();
                    return true;
                case MSG_VOLUME_STATE_CHANGED /*2*/:
                    this.mCallback.onVolumeStateChanged((VolumeInfo) args.arg1, args.argi2, args.argi3);
                    args.recycle();
                    return true;
                case MSG_VOLUME_RECORD_CHANGED /*3*/:
                    this.mCallback.onVolumeRecordChanged((VolumeRecord) args.arg1);
                    args.recycle();
                    return true;
                case MSG_VOLUME_FORGOTTEN /*4*/:
                    this.mCallback.onVolumeForgotten((String) args.arg1);
                    args.recycle();
                    return true;
                case MSG_DISK_SCANNED /*5*/:
                    this.mCallback.onDiskScanned((DiskInfo) args.arg1, args.argi2);
                    args.recycle();
                    return true;
                case MSG_DISK_DESTROYED /*6*/:
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
            this.mHandler.obtainMessage(MSG_STORAGE_STATE_CHANGED, args).sendToTarget();
        }

        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = vol;
            args.argi2 = oldState;
            args.argi3 = newState;
            this.mHandler.obtainMessage(MSG_VOLUME_STATE_CHANGED, args).sendToTarget();
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = rec;
            this.mHandler.obtainMessage(MSG_VOLUME_RECORD_CHANGED, args).sendToTarget();
        }

        public void onVolumeForgotten(String fsUuid) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fsUuid;
            this.mHandler.obtainMessage(MSG_VOLUME_FORGOTTEN, args).sendToTarget();
        }

        public void onDiskScanned(DiskInfo disk, int volumeCount) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk;
            args.argi2 = volumeCount;
            this.mHandler.obtainMessage(MSG_DISK_SCANNED, args).sendToTarget();
        }

        public void onDiskDestroyed(DiskInfo disk) throws RemoteException {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk;
            this.mHandler.obtainMessage(MSG_DISK_DESTROYED, args).sendToTarget();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.storage.StorageManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.storage.StorageManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.os.storage.StorageManager.<clinit>():void");
    }

    private int getNextNonce() {
        return this.mNextNonce.getAndIncrement();
    }

    @Deprecated
    public static StorageManager from(Context context) {
        return (StorageManager) context.getSystemService(StorageManager.class);
    }

    public StorageManager(Context context, Looper looper) {
        this.mNextNonce = new AtomicInteger(CRYPT_TYPE_PASSWORD);
        this.mDelegates = new ArrayList();
        this.mObbActionListener = new ObbActionListener();
        this.mContext = context;
        this.mResolver = context.getContentResolver();
        this.mLooper = looper;
        this.mMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        if (this.mMountService == null) {
            throw new IllegalStateException("Failed to find running mount service");
        }
    }

    public void registerListener(StorageEventListener listener) {
        synchronized (this.mDelegates) {
            StorageEventListenerDelegate delegate = new StorageEventListenerDelegate(listener, this.mLooper);
            try {
                this.mMountService.registerListener(delegate);
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
                StorageEventListenerDelegate delegate = (StorageEventListenerDelegate) i.next();
                if (delegate.mCallback == listener) {
                    try {
                        this.mMountService.unregisterListener(delegate);
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
            this.mMountService.mountObb(str, new File(rawPath).getCanonicalPath(), str2, this.mObbActionListener, this.mObbActionListener.addListener(listener));
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
            this.mMountService.unmountObb(rawPath, force, this.mObbActionListener, this.mObbActionListener.addListener(listener));
            return true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isObbMounted(String rawPath) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        try {
            return this.mMountService.isObbMounted(rawPath);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getMountedObbPath(String rawPath) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        try {
            return this.mMountService.getMountedObbPath(rawPath);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<DiskInfo> getDisks() {
        try {
            return Arrays.asList(this.mMountService.getDisks());
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

    public List<VolumeInfo> getVolumes() {
        try {
            return Arrays.asList(this.mMountService.getVolumes(CRYPT_TYPE_PASSWORD));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<VolumeInfo> getWritablePrivateVolumes() {
        try {
            ArrayList<VolumeInfo> res = new ArrayList();
            VolumeInfo[] volumes = this.mMountService.getVolumes(CRYPT_TYPE_PASSWORD);
            int length = volumes.length;
            for (int i = CRYPT_TYPE_PASSWORD; i < length; i += FLAG_STORAGE_DE) {
                VolumeInfo vol = volumes[i];
                if (vol.getType() == FLAG_STORAGE_DE && vol.isMountedWritable()) {
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
            return Arrays.asList(this.mMountService.getVolumeRecords(CRYPT_TYPE_PASSWORD));
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
            if (!(rec == null || TextUtils.isEmpty(rec.nickname))) {
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
            this.mMountService.mount(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unmount(String volId) {
        try {
            this.mMountService.unmount(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void format(String volId) {
        try {
            this.mMountService.format(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long benchmark(String volId) {
        try {
            return this.mMountService.benchmark(volId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void partitionPublic(String diskId) {
        try {
            this.mMountService.partitionPublic(diskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void partitionPrivate(String diskId) {
        try {
            this.mMountService.partitionPrivate(diskId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void partitionMixed(String diskId, int ratio) {
        try {
            this.mMountService.partitionMixed(diskId, ratio);
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
                    this.mMountService.partitionPublic(diskId);
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
            this.mMountService.setVolumeNickname(fsUuid, nickname);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setVolumeInited(String fsUuid, boolean inited) {
        int i = FLAG_STORAGE_DE;
        try {
            IMountService iMountService = this.mMountService;
            if (!inited) {
                i = CRYPT_TYPE_PASSWORD;
            }
            iMountService.setVolumeUserFlags(fsUuid, i, FLAG_STORAGE_DE);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setVolumeSnoozed(String fsUuid, boolean snoozed) {
        int i = FLAG_STORAGE_CE;
        try {
            IMountService iMountService = this.mMountService;
            if (!snoozed) {
                i = CRYPT_TYPE_PASSWORD;
            }
            iMountService.setVolumeUserFlags(fsUuid, i, FLAG_STORAGE_CE);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void forgetVolume(String fsUuid) {
        try {
            this.mMountService.forgetVolume(fsUuid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public String getPrimaryStorageUuid() {
        try {
            return this.mMountService.getPrimaryStorageUuid();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) {
        try {
            this.mMountService.setPrimaryStorageUuid(volumeUuid, callback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public StorageVolume getStorageVolume(File file) {
        return getStorageVolume(getVolumeList(), file);
    }

    public static StorageVolume getStorageVolume(File file, int userId) {
        return getStorageVolume(getVolumeList(userId, CRYPT_TYPE_PASSWORD), file);
    }

    private static StorageVolume getStorageVolume(StorageVolume[] volumes, File file) {
        if (file == null) {
            return null;
        }
        try {
            file = file.getCanonicalFile();
            int i = CRYPT_TYPE_PASSWORD;
            int length = volumes.length;
            while (i < length) {
                StorageVolume volume = volumes[i];
                try {
                    if (FileUtils.contains(volume.getPathFile().getCanonicalFile(), file)) {
                        return volume;
                    }
                    i += FLAG_STORAGE_DE;
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
        return Environment.MEDIA_UNKNOWN;
    }

    public List<StorageVolume> getStorageVolumes() {
        ArrayList<StorageVolume> res = new ArrayList();
        Collections.addAll(res, getVolumeList(UserHandle.myUserId(), Major.IMAGING));
        return res;
    }

    public StorageVolume getPrimaryStorageVolume() {
        return getVolumeList(UserHandle.myUserId(), Major.IMAGING)[CRYPT_TYPE_PASSWORD];
    }

    public StorageVolume[] getVolumeList() {
        return getVolumeList(this.mContext.getUserId(), CRYPT_TYPE_PASSWORD);
    }

    public static StorageVolume[] getVolumeList(int userId, int flags) {
        IMountService mountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        try {
            String packageName = ActivityThread.currentOpPackageName();
            if (packageName == null) {
                String[] packageNames = ActivityThread.getPackageManager().getPackagesForUid(Process.myUid());
                if (packageNames == null || packageNames.length <= 0) {
                    return new StorageVolume[CRYPT_TYPE_PASSWORD];
                }
                packageName = packageNames[CRYPT_TYPE_PASSWORD];
            }
            int uid = ActivityThread.getPackageManager().getPackageUid(packageName, KeymasterDefs.KM_ENUM, userId);
            if (uid <= 0) {
                return new StorageVolume[CRYPT_TYPE_PASSWORD];
            }
            return mountService.getVolumeList(uid, packageName, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public String[] getVolumePaths() {
        StorageVolume[] volumes = getVolumeList();
        int count = volumes.length;
        String[] paths = new String[count];
        for (int i = CRYPT_TYPE_PASSWORD; i < count; i += FLAG_STORAGE_DE) {
            paths[i] = volumes[i].getPath();
        }
        return paths;
    }

    public StorageVolume getPrimaryVolume() {
        return getPrimaryVolume(getVolumeList());
    }

    public static StorageVolume getPrimaryVolume(StorageVolume[] volumes) {
        int length = volumes.length;
        for (int i = CRYPT_TYPE_PASSWORD; i < length; i += FLAG_STORAGE_DE) {
            StorageVolume volume = volumes[i];
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
        return Math.min((path.getTotalSpace() * ((long) Global.getInt(this.mResolver, Global.SYS_STORAGE_THRESHOLD_PERCENTAGE, DEFAULT_THRESHOLD_PERCENTAGE))) / 100, Global.getLong(this.mResolver, Global.SYS_STORAGE_THRESHOLD_MAX_BYTES, DEFAULT_THRESHOLD_MAX_BYTES));
    }

    public long getStorageFullBytes(File path) {
        return Global.getLong(this.mResolver, Global.SYS_STORAGE_FULL_THRESHOLD_BYTES, DEFAULT_FULL_THRESHOLD_BYTES);
    }

    public void createUserKey(int userId, int serialNumber, boolean ephemeral) {
        try {
            this.mMountService.createUserKey(userId, serialNumber, ephemeral);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void destroyUserKey(int userId) {
        try {
            this.mMountService.destroyUserKey(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            this.mMountService.unlockUserKey(userId, serialNumber, token, secret);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void lockUserKey(int userId) {
        try {
            this.mMountService.lockUserKey(userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) {
        try {
            this.mMountService.prepareUserStorage(volumeUuid, userId, serialNumber, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void destroyUserStorage(String volumeUuid, int userId, int flags) {
        try {
            this.mMountService.destroyUserStorage(volumeUuid, userId, flags);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static boolean isUserKeyUnlocked(int userId) {
        if (sMountService == null) {
            sMountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
        }
        if (sMountService == null) {
            Slog.w(TAG, "Early during boot, assuming locked");
            return false;
        }
        long token = Binder.clearCallingIdentity();
        try {
            boolean isUserKeyUnlocked = sMountService.isUserKeyUnlocked(userId);
            Binder.restoreCallingIdentity(token);
            return isUserKeyUnlocked;
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
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
        return !"unsupported".equalsIgnoreCase(SystemProperties.get("ro.crypto.state", "unsupported"));
    }

    public static boolean isEncrypted() {
        return "encrypted".equalsIgnoreCase(SystemProperties.get("ro.crypto.state", ProxyInfo.LOCAL_EXCL_LIST));
    }

    public static boolean isFileEncryptedNativeOnly() {
        if (!isEncrypted()) {
            return false;
        }
        return WifiManager.EXTRA_PASSPOINT_ICON_FILE.equalsIgnoreCase(SystemProperties.get("ro.crypto.type", ProxyInfo.LOCAL_EXCL_LIST));
    }

    public static boolean isBlockEncrypted() {
        if (!isEncrypted()) {
            return false;
        }
        return "block".equalsIgnoreCase(SystemProperties.get("ro.crypto.type", ProxyInfo.LOCAL_EXCL_LIST));
    }

    public static boolean isNonDefaultBlockEncrypted() {
        boolean z = true;
        if (!isBlockEncrypted()) {
            return false;
        }
        try {
            if (IMountService.Stub.asInterface(ServiceManager.getService("mount")).getPasswordType() == FLAG_STORAGE_DE) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting encryption type");
            return false;
        }
    }

    public static boolean isBlockEncrypting() {
        return !ProxyInfo.LOCAL_EXCL_LIST.equalsIgnoreCase(SystemProperties.get("vold.encrypt_progress", ProxyInfo.LOCAL_EXCL_LIST));
    }

    public static boolean inCryptKeeperBounce() {
        return "trigger_restart_min_framework".equals(SystemProperties.get("vold.decrypt"));
    }

    public static boolean isFileEncryptedEmulatedOnly() {
        return SystemProperties.getBoolean(PROP_EMULATE_FBE, false);
    }

    public static boolean isFileEncryptedNativeOrEmulated() {
        if (isFileEncryptedNativeOnly()) {
            return true;
        }
        return isFileEncryptedEmulatedOnly();
    }

    public static File maybeTranslateEmulatedPathToInternal(File path) {
        try {
            VolumeInfo[] vols = IMountService.Stub.asInterface(ServiceManager.getService("mount")).getVolumes(CRYPT_TYPE_PASSWORD);
            int length = vols.length;
            for (int i = CRYPT_TYPE_PASSWORD; i < length; i += FLAG_STORAGE_DE) {
                VolumeInfo vol = vols[i];
                if ((vol.getType() == FLAG_STORAGE_CE || vol.getType() == 0) && vol.isMountedReadable()) {
                    File internalPath = FileUtils.rewriteAfterRename(vol.getPath(), vol.getInternalPath(), path);
                    if (internalPath != null && internalPath.exists()) {
                        return internalPath;
                    }
                }
            }
            return path;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public ParcelFileDescriptor mountAppFuse(String name) {
        try {
            return this.mMountService.mountAppFuse(name);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
