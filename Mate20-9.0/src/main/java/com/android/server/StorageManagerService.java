package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.admin.SecurityLog;
import android.app.usage.StorageStatsManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.ObbInfo;
import android.database.ContentObserver;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.os.Binder;
import android.os.DropBoxManager;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IStoraged;
import android.os.IVold;
import android.os.IVoldListener;
import android.os.IVoldTaskListener;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelableException;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.IObbActionListener;
import android.os.storage.IStorageEventListener;
import android.os.storage.IStorageShutdownObserver;
import android.os.storage.StorageManager;
import android.os.storage.StorageManagerInternal;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.DataUnit;
import android.util.Flog;
import android.util.Pair;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IMediaContainerService;
import com.android.internal.os.AppFuseMount;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.FuseUnavailableMountException;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.HexDump;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.BatteryService;
import com.android.server.HwServiceFactory;
import com.android.server.UiModeManagerService;
import com.android.server.Watchdog;
import com.android.server.backup.BackupPasswordManager;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.storage.AppFuseBridge;
import com.android.server.usage.UnixCalendar;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import libcore.io.IoUtils;
import libcore.util.EmptyArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

class StorageManagerService extends AbsStorageManagerService implements Watchdog.Monitor, ActivityManagerInternal.ScreenObserver {
    private static final String ATTR_CREATED_MILLIS = "createdMillis";
    private static final String ATTR_FS_UUID = "fsUuid";
    private static final String ATTR_LAST_BENCH_MILLIS = "lastBenchMillis";
    private static final String ATTR_LAST_TRIM_MILLIS = "lastTrimMillis";
    private static final String ATTR_NICKNAME = "nickname";
    private static final String ATTR_PART_GUID = "partGuid";
    private static final String ATTR_PRIMARY_STORAGE_UUID = "primaryStorageUuid";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_USER_FLAGS = "userFlags";
    private static final String ATTR_VERSION = "version";
    private static final int CHECK_VOLUME_COMPLETED = 0;
    private static final int CRYPTO_ALGORITHM_KEY_SIZE = 128;
    public static final String[] CRYPTO_TYPES = {"password", BatteryService.HealthServiceWrapper.INSTANCE_VENDOR, "pattern", "pin"};
    private static final boolean DEBUG_EVENTS = false;
    private static final boolean DEBUG_OBB = false;
    static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName(PackageManagerService.DEFAULT_CONTAINER_PACKAGE, "com.android.defcontainer.DefaultContainerService");
    private static final boolean DEFAULT_VALUE = true;
    private static final boolean EMULATE_FBE_SUPPORTED = true;
    private static final int H_ABNORMAL_SD_BROADCAST = 13;
    private static final int H_ABORT_IDLE_MAINT = 12;
    private static final int H_BACKUP_DEV_MOUNT = 14;
    private static final int H_DAEMON_CONNECTED = 2;
    private static final int H_FSTRIM = 4;
    private static final int H_INTERNAL_BROADCAST = 7;
    private static final int H_PARTITION_FORGET = 9;
    private static final int H_RESET = 10;
    private static final int H_RUN_IDLE_MAINT = 11;
    private static final int H_SHUTDOWN = 3;
    private static final int H_SYSTEM_READY = 1;
    protected static final int H_VOLUME_BROADCAST = 6;
    private static final int H_VOLUME_CHECK = 15;
    private static final int H_VOLUME_MOUNT = 5;
    private static final int H_VOLUME_UNMOUNT = 8;
    private static final int INTERNAL = 0;
    private static final String LAST_FSTRIM_FILE = "last-fstrim";
    private static final int MOVE_STATUS_COPY_FINISHED = 82;
    private static final int OBB_FLUSH_MOUNT_STATE = 5;
    private static final int OBB_MCS_BOUND = 2;
    private static final int OBB_MCS_RECONNECT = 4;
    private static final int OBB_MCS_UNBIND = 3;
    private static final int OBB_RUN_ACTION = 1;
    private static final int PBKDF2_HASH_ROUNDS = 1024;
    private static final String PRIMARYSD = "persist.sys.primarysd";
    private static final String PROPERTIES_PRIVACY_SPACE_HIDSD = "ro.config.hw_privacySpace_hidSd";
    private static final int SDCARD = 1;
    private static final String TAG = "StorageManagerService";
    private static final String TAG_STORAGE_BENCHMARK = "storage_benchmark";
    private static final String TAG_STORAGE_TRIM = "storage_trim";
    private static final String TAG_VOLUME = "volume";
    private static final String TAG_VOLUMES = "volumes";
    private static final int VERSION_ADD_PRIMARY = 2;
    private static final int VERSION_FIX_PRIMARY = 3;
    private static final int VERSION_INIT = 1;
    private static final boolean WATCHDOG_ENABLE = false;
    private static final String ZRAM_ENABLED_PROPERTY = "persist.sys.zram_enabled";
    /* access modifiers changed from: private */
    public static final boolean mIsHidSd = SystemProperties.getBoolean(PROPERTIES_PRIVACY_SPACE_HIDSD, true);
    static StorageManagerService sSelf = null;
    @GuardedBy("mAppFuseLock")
    private AppFuseBridge mAppFuseBridge = null;
    private final Object mAppFuseLock = new Object();
    private volatile boolean mBootCompleted = false;
    /* access modifiers changed from: private */
    public final Callbacks mCallbacks;
    /* access modifiers changed from: private */
    public IMediaContainerService mContainerService = null;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public volatile int mCurrentUserId = 0;
    private volatile boolean mDaemonConnected = false;
    /* access modifiers changed from: private */
    public final DefaultContainerConnection mDefContainerConn = new DefaultContainerConnection();
    @GuardedBy("mLock")
    private ArrayMap<String, CountDownLatch> mDiskScanLatches = new ArrayMap<>();
    @GuardedBy("mLock")
    protected ArrayMap<String, DiskInfo> mDisks = new ArrayMap<>();
    protected final Handler mHandler;
    /* access modifiers changed from: private */
    public long mLastMaintenance;
    /* access modifiers changed from: private */
    public final File mLastMaintenanceFile;
    private final IVoldListener mListener = new IVoldListener.Stub() {
        public void onLockedDiskAdd() {
            Slog.d(StorageManagerService.TAG, "sdlock IVoldListener onLockedDiskAdd");
            StorageManagerService.sSelf.onLockedDiskAdd();
        }

        public void onLockedDiskRemove() {
            Slog.d(StorageManagerService.TAG, "sdlock IVoldListener onLockedDiskRemove");
            StorageManagerService.sSelf.onLockedDiskRemove();
        }

        public void onDiskCreated(String diskId, int flags) {
            int flags2;
            Slog.i(StorageManagerService.TAG, "onDiskCreated : diskId = " + diskId + ",flags =  " + flags);
            int deviceCode = StorageManagerService.this.getUsbDeviceExInfo();
            synchronized (StorageManagerService.this.mLock) {
                String value = SystemProperties.get("persist.sys.adoptable");
                char c = 65535;
                int hashCode = value.hashCode();
                if (hashCode != 464944051) {
                    if (hashCode == 1528363547) {
                        if (value.equals("force_off")) {
                            c = 1;
                        }
                    }
                } else if (value.equals("force_on")) {
                    c = 0;
                }
                switch (c) {
                    case 0:
                        flags |= 1;
                        break;
                    case 1:
                        flags &= -2;
                        break;
                }
                flags2 = flags | 1;
                if ((flags2 & 8) != 0) {
                    if (deviceCode == 0) {
                        flags2 |= 64;
                    } else if (deviceCode == 1) {
                        flags2 |= 128;
                    }
                }
                StorageManagerService.this.mDisks.put(diskId, new DiskInfo(diskId, flags2));
            }
            if ((flags2 & 4) != 0) {
                StorageManagerService.sSelf.onLockedDiskChange();
            } else if ((flags2 & 8) != 0) {
                StorageManagerService.this.notifyDeviceStateToTelephony("SD", "in", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            }
        }

        public void onDiskScanned(String diskId) {
            synchronized (StorageManagerService.this.mLock) {
                DiskInfo disk = StorageManagerService.this.mDisks.get(diskId);
                if (disk != null) {
                    StorageManagerService.this.onDiskScannedLocked(disk);
                }
            }
        }

        public void onDiskMetadataChanged(String diskId, long sizeBytes, String label, String sysPath) {
            synchronized (StorageManagerService.this.mLock) {
                DiskInfo disk = StorageManagerService.this.mDisks.get(diskId);
                if (disk != null) {
                    disk.size = sizeBytes;
                    disk.label = label;
                    disk.sysPath = sysPath;
                }
            }
        }

        public void onDiskDestroyed(String diskId) {
            synchronized (StorageManagerService.this.mLock) {
                DiskInfo disk = StorageManagerService.this.mDisks.remove(diskId);
                if (disk != null) {
                    StorageManagerService.this.mCallbacks.notifyDiskDestroyed(disk);
                }
                if (disk.isUsb()) {
                    StorageManagerService.this.notifyDeviceStateToTelephony("SD", "out", BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                }
            }
        }

        public void onVolumeCreated(String volId, int type, String diskId, String partGuid) {
            Slog.i(StorageManagerService.TAG, "onVolumeCreated : volId = " + volId + ",type = " + type + " ,diskId = " + diskId + ", partGuid = " + partGuid);
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = new VolumeInfo(volId, type, StorageManagerService.this.mDisks.get(diskId), partGuid);
                StorageManagerService.this.mVolumes.put(volId, vol);
                StorageManagerService.this.onVolumeCreatedLocked(vol);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x005a, code lost:
            return;
         */
        public void onCheckVolumeCompleted(String volId, String diskId, String partGuid, int isSucc) {
            Slog.i(StorageManagerService.TAG, "onCheckVolumeCompleted :  volId = " + volId + ",diskId = " + diskId + ",partGuid = " + partGuid + ",isSucc = " + isSucc);
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    if (isSucc == 0) {
                        StorageManagerService.this.onCheckVolumeCompletedLocked(vol);
                    } else {
                        int oldState = vol.state;
                        vol.state = 6;
                        StorageManagerService.this.mCallbacks.notifyVolumeStateChanged(vol, oldState, 6);
                    }
                }
            }
        }

        public void onVolumeStateChanged(String volId, int state) {
            Slog.i(StorageManagerService.TAG, "onVolumeStateChanged :  volId = " + volId + ",state = " + state);
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    int oldState = vol.state;
                    int newState = state;
                    vol.state = newState;
                    StorageManagerService.this.onVolumeStateChangedLocked(vol, oldState, newState);
                }
            }
        }

        public void onVolumeMetadataChanged(String volId, String fsType, String fsUuid, String fsLabel) {
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    vol.fsType = fsType;
                    vol.fsUuid = fsUuid;
                    vol.fsLabel = fsLabel;
                }
            }
        }

        public void onVolumePathChanged(String volId, String path) {
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    vol.path = path;
                }
            }
        }

        public void onVolumeInternalPathChanged(String volId, String internalPath) {
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    vol.internalPath = internalPath;
                }
            }
        }

        public void onVolumeDestroyed(String volId) {
            Slog.i(StorageManagerService.TAG, "onVolumeDestroyed : volId = " + volId);
            synchronized (StorageManagerService.this.mLock) {
                StorageManagerService.this.mVolumes.remove(volId);
            }
        }

        public void onSdHealthReport(String volId, int newState) {
            Slog.i(StorageManagerService.TAG, "onSdHealthReport :  volId = " + volId + ", newState = " + newState);
            synchronized (StorageManagerService.this.mLock) {
                VolumeInfo vol = StorageManagerService.this.mVolumes.get(volId);
                if (vol != null) {
                    int oldState = vol.state;
                    Intent intent = new Intent("com.huawei.storage.MEDIA_ABNORMAL_SD");
                    intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", vol);
                    intent.putExtra("android.os.storage.extra.VOLUME_OLD_STATE", oldState);
                    intent.putExtra("android.os.storage.extra.VOLUME_NEW_STATE", newState);
                    Flog.i(1002, "AbNormal SD card volumeInfo = " + vol.toString() + ",errorCode = " + newState);
                    StorageManagerService.this.mHandler.obtainMessage(13, intent).sendToTarget();
                }
            }
        }

        public void onCryptsdMessage(String message) {
            Slog.d(StorageManagerService.TAG, "sdcrypt IVoldListener onCryptsdMessage" + message);
            StorageManagerService.sSelf.onCryptsdMessage(message);
        }
    };
    @GuardedBy("mLock")
    protected int[] mLocalUnlockedUsers = EmptyArray.INT;
    protected final Object mLock = LockGuard.installNewLock(4);
    private final LockPatternUtils mLockPatternUtils;
    @GuardedBy("mLock")
    private IPackageMoveObserver mMoveCallback;
    @GuardedBy("mLock")
    private String mMoveTargetUuid;
    @GuardedBy("mAppFuseLock")
    private int mNextAppFuseName = 0;
    /* access modifiers changed from: private */
    public final ObbActionHandler mObbActionHandler;
    /* access modifiers changed from: private */
    public final Map<IBinder, List<ObbState>> mObbMounts = new HashMap();
    /* access modifiers changed from: private */
    public final Map<String, ObbState> mObbPathToStateMap = new HashMap();
    private PackageManagerService mPms;
    @GuardedBy("mLock")
    private String mPrimaryStorageUuid;
    @GuardedBy("mLock")
    private ArrayMap<String, VolumeRecord> mRecords = new ArrayMap<>();
    private BroadcastReceiver mScreenAndDreamingReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_OFF".equals(action) || "android.intent.action.DREAMING_STARTED".equals(action)) {
                FstrimServiceIdler.setScreenOn(false);
                FstrimServiceIdler.scheduleFstrim(StorageManagerService.this.mContext);
            } else if ("android.intent.action.SCREEN_ON".equals(action) || "android.intent.action.DREAMING_STOPPED".equals(action)) {
                FstrimServiceIdler.setScreenOn(true);
                FstrimServiceIdler.cancelFstrim(StorageManagerService.this.mContext);
                StorageManagerService.this.abortIdleMaint(null);
            }
        }
    };
    private volatile boolean mSecureKeyguardShowing = true;
    private final AtomicFile mSettingsFile;
    private final StorageManagerInternalImpl mStorageManagerInternal = new StorageManagerInternalImpl();
    /* access modifiers changed from: private */
    public volatile IStoraged mStoraged;
    private volatile boolean mSystemReady = false;
    @GuardedBy("mLock")
    protected int[] mSystemUnlockedUsers = EmptyArray.INT;
    private final BroadcastReceiver mToVoldBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("android.intent.action.BOOT_COMPLETED")) {
                StorageManagerService.this.bootCompleteToVold();
            }
        }
    };
    private BroadcastReceiver mUserReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int userId = intent.getIntExtra("android.intent.extra.user_handle", -1);
            int i = 0;
            Preconditions.checkArgument(userId >= 0);
            try {
                if ("android.intent.action.USER_ADDED".equals(action)) {
                    UserManager um = (UserManager) StorageManagerService.this.mContext.getSystemService(UserManager.class);
                    StorageManagerService.this.mVold.onUserAdded(userId, um.getUserSerialNumber(userId));
                    Slog.i(StorageManagerService.TAG, "add user before getuserinfo");
                    UserInfo userInfo = um.getUserInfo(userId);
                    if (StorageManagerService.mIsHidSd && userInfo.isHwHiddenSpace()) {
                        synchronized (StorageManagerService.this.mVolumes) {
                            int size = StorageManagerService.this.mVolumes.size();
                            while (i < size) {
                                VolumeInfo vol = StorageManagerService.this.mVolumes.valueAt(i);
                                if (StorageManagerService.this.isExternalSDcard(vol) && vol.isMountedReadable()) {
                                    Slog.i(StorageManagerService.TAG, "begin to send block_id");
                                    vol.blockedUserId = userId;
                                    StorageManagerService.this.mVold.sendBlockUserId(vol.id, vol.blockedUserId);
                                }
                                i++;
                            }
                        }
                    }
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    synchronized (StorageManagerService.this.mVolumes) {
                        Slog.i(StorageManagerService.TAG, "remove user before getuserinfo");
                        UserInfo userInfo2 = ((UserManager) StorageManagerService.this.mContext.getSystemService(UserManager.class)).getUserInfo(userId);
                        int size2 = StorageManagerService.this.mVolumes.size();
                        while (i < size2) {
                            VolumeInfo vol2 = StorageManagerService.this.mVolumes.valueAt(i);
                            if (vol2.mountUserId == userId) {
                                vol2.mountUserId = -10000;
                                StorageManagerService.this.mHandler.obtainMessage(8, vol2).sendToTarget();
                            } else if (StorageManagerService.mIsHidSd && userInfo2.isHwHiddenSpace() && StorageManagerService.this.isExternalSDcard(vol2) && vol2.isMountedReadable()) {
                                Slog.i(StorageManagerService.TAG, "begin to reset block_id");
                                vol2.blockedUserId = -1;
                                StorageManagerService.this.mVold.sendBlockUserId(vol2.id, vol2.blockedUserId);
                            }
                            i++;
                        }
                    }
                    StorageManagerService.this.mVold.onUserRemoved(userId);
                }
            } catch (Exception e) {
                Slog.wtf(StorageManagerService.TAG, e);
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mUserStartedFinish = false;
    protected volatile IVold mVold;
    @GuardedBy("mLock")
    protected final ArrayMap<String, VolumeInfo> mVolumes = new ArrayMap<>();

    class AppFuseMountScope extends AppFuseBridge.MountScope {
        boolean opened = false;

        public AppFuseMountScope(int uid, int pid, int mountId) {
            super(uid, pid, mountId);
        }

        public ParcelFileDescriptor open() throws NativeDaemonConnectorException {
            try {
                return new ParcelFileDescriptor(StorageManagerService.this.mVold.mountAppFuse(this.uid, Process.myPid(), this.mountId));
            } catch (Exception e) {
                throw new NativeDaemonConnectorException("Failed to mount", (Throwable) e);
            }
        }

        public void close() throws Exception {
            if (this.opened) {
                StorageManagerService.this.mVold.unmountAppFuse(this.uid, Process.myPid(), this.mountId);
                this.opened = false;
            }
        }
    }

    private static class Callbacks extends Handler {
        private static final int MSG_DISK_DESTROYED = 6;
        private static final int MSG_DISK_SCANNED = 5;
        private static final int MSG_STORAGE_STATE_CHANGED = 1;
        private static final int MSG_VOLUME_FORGOTTEN = 4;
        private static final int MSG_VOLUME_RECORD_CHANGED = 3;
        private static final int MSG_VOLUME_STATE_CHANGED = 2;
        private final RemoteCallbackList<IStorageEventListener> mCallbacks = new RemoteCallbackList<>();

        public Callbacks(Looper looper) {
            super(looper);
        }

        public void register(IStorageEventListener callback) {
            this.mCallbacks.register(callback);
        }

        public void unregister(IStorageEventListener callback) {
            this.mCallbacks.unregister(callback);
        }

        public void handleMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            int n = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    invokeCallback(this.mCallbacks.getBroadcastItem(i), msg.what, args);
                } catch (RemoteException e) {
                }
            }
            this.mCallbacks.finishBroadcast();
            args.recycle();
        }

        private void invokeCallback(IStorageEventListener callback, int what, SomeArgs args) throws RemoteException {
            switch (what) {
                case 1:
                    callback.onStorageStateChanged((String) args.arg1, (String) args.arg2, (String) args.arg3);
                    return;
                case 2:
                    callback.onVolumeStateChanged((VolumeInfo) args.arg1, args.argi2, args.argi3);
                    return;
                case 3:
                    callback.onVolumeRecordChanged((VolumeRecord) args.arg1);
                    return;
                case 4:
                    callback.onVolumeForgotten((String) args.arg1);
                    return;
                case 5:
                    callback.onDiskScanned((DiskInfo) args.arg1, args.argi2);
                    return;
                case 6:
                    callback.onDiskDestroyed((DiskInfo) args.arg1);
                    return;
                default:
                    return;
            }
        }

        /* access modifiers changed from: private */
        public void notifyStorageStateChanged(String path, String oldState, String newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = path;
            args.arg2 = oldState;
            args.arg3 = newState;
            obtainMessage(1, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = vol.clone();
            args.argi2 = oldState;
            args.argi3 = newState;
            obtainMessage(2, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyVolumeRecordChanged(VolumeRecord rec) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = rec.clone();
            obtainMessage(3, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyVolumeForgotten(String fsUuid) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = fsUuid;
            obtainMessage(4, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyDiskScanned(DiskInfo disk, int volumeCount) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk.clone();
            args.argi2 = volumeCount;
            obtainMessage(5, args).sendToTarget();
        }

        /* access modifiers changed from: private */
        public void notifyDiskDestroyed(DiskInfo disk) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = disk.clone();
            obtainMessage(6, args).sendToTarget();
        }
    }

    class DefaultContainerConnection implements ServiceConnection {
        DefaultContainerConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            StorageManagerService.this.mObbActionHandler.sendMessage(StorageManagerService.this.mObbActionHandler.obtainMessage(2, IMediaContainerService.Stub.asInterface(service)));
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    public static class Lifecycle extends SystemService {
        private StorageManagerService mStorageManagerService;

        public Lifecycle(Context context) {
            super(context);
            HwServiceFactory.IHwStorageManagerService iSMS = HwServiceFactory.getHwStorageManagerService();
            if (iSMS != null) {
                this.mStorageManagerService = iSMS.getInstance(getContext());
            } else {
                this.mStorageManagerService = new StorageManagerService(getContext());
            }
        }

        /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.StorageManagerService, android.os.IBinder] */
        public void onStart() {
            publishBinderService("mount", this.mStorageManagerService);
            this.mStorageManagerService.start();
        }

        public void onBootPhase(int phase) {
            if (phase == 550) {
                this.mStorageManagerService.systemReady();
            } else if (phase == 1000) {
                this.mStorageManagerService.bootCompleted();
            }
        }

        public void onSwitchUser(int userHandle) {
            int unused = this.mStorageManagerService.mCurrentUserId = userHandle;
        }

        public void onUnlockUser(int userHandle) {
            this.mStorageManagerService.onUnlockUser(userHandle);
        }

        public void onCleanupUser(int userHandle) {
            this.mStorageManagerService.onCleanupUser(userHandle);
        }
    }

    class MountObbAction extends ObbAction {
        private final int mCallingUid;
        private final String mKey;

        MountObbAction(ObbState obbState, String key, int callingUid) {
            super(obbState);
            this.mKey = key;
            this.mCallingUid = callingUid;
        }

        public void handleExecute() throws ObbException {
            boolean isMounted;
            String binderKey;
            StorageManagerService.this.warnOnNotMounted();
            ObbInfo obbInfo = getObbInfo();
            if (StorageManagerService.this.isUidOwnerOfPackageOrSystem(obbInfo.packageName, this.mCallingUid)) {
                synchronized (StorageManagerService.this.mObbMounts) {
                    isMounted = StorageManagerService.this.mObbPathToStateMap.containsKey(this.mObbState.rawPath);
                }
                if (!isMounted) {
                    if (this.mKey == null) {
                        binderKey = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                    } else {
                        try {
                            String hashedKey = new BigInteger(SecretKeyFactory.getInstance(BackupPasswordManager.PBKDF_CURRENT).generateSecret(new PBEKeySpec(this.mKey.toCharArray(), obbInfo.salt, 1024, 128)).getEncoded()).toString(16);
                            binderKey = hashedKey;
                            String str = hashedKey;
                        } catch (GeneralSecurityException e) {
                            throw new ObbException(20, (Throwable) e);
                        }
                    }
                    try {
                        this.mObbState.volId = StorageManagerService.this.mVold.createObb(this.mObbState.canonicalPath, binderKey, this.mObbState.ownerGid);
                        StorageManagerService.this.mVold.mount(this.mObbState.volId, 0, -1);
                        synchronized (StorageManagerService.this.mObbMounts) {
                            StorageManagerService.this.addObbStateLocked(this.mObbState);
                        }
                        notifyObbStateChange(1);
                    } catch (Exception e2) {
                        throw new ObbException(21, (Throwable) e2);
                    }
                } else {
                    throw new ObbException(24, "Attempt to mount OBB which is already mounted: " + obbInfo.filename);
                }
            } else {
                throw new ObbException(25, "Denied attempt to mount OBB " + obbInfo.filename + " which is owned by " + obbInfo.packageName);
            }
        }

        public String toString() {
            return "MountObbAction{" + this.mObbState + '}';
        }
    }

    abstract class ObbAction {
        private static final int MAX_RETRIES = 3;
        ObbState mObbState;
        private int mRetries;

        /* access modifiers changed from: package-private */
        public abstract void handleExecute() throws ObbException;

        ObbAction(ObbState obbState) {
            this.mObbState = obbState;
        }

        public void execute(ObbActionHandler handler) {
            try {
                this.mRetries++;
                if (this.mRetries > 3) {
                    StorageManagerService.this.mObbActionHandler.sendEmptyMessage(3);
                    notifyObbStateChange(new ObbException(20, "Failed to bind to media container service"));
                    return;
                }
                handleExecute();
                StorageManagerService.this.mObbActionHandler.sendEmptyMessage(3);
            } catch (ObbException e) {
                notifyObbStateChange(e);
                StorageManagerService.this.mObbActionHandler.sendEmptyMessage(3);
            }
        }

        /* access modifiers changed from: protected */
        public ObbInfo getObbInfo() throws ObbException {
            try {
                ObbInfo obbInfo = StorageManagerService.this.mContainerService.getObbInfo(this.mObbState.canonicalPath);
                if (obbInfo != null) {
                    return obbInfo;
                }
                throw new ObbException(20, "Missing OBB info for: " + this.mObbState.canonicalPath);
            } catch (Exception e) {
                throw new ObbException(25, (Throwable) e);
            }
        }

        /* access modifiers changed from: protected */
        public void notifyObbStateChange(ObbException e) {
            Slog.w(StorageManagerService.TAG, e);
            notifyObbStateChange(e.status);
        }

        /* access modifiers changed from: protected */
        public void notifyObbStateChange(int status) {
            if (this.mObbState != null && this.mObbState.token != null) {
                try {
                    this.mObbState.token.onObbResult(this.mObbState.rawPath, this.mObbState.nonce, status);
                } catch (RemoteException e) {
                    Slog.w(StorageManagerService.TAG, "StorageEventListener went away while calling onObbStateChanged");
                }
            }
        }
    }

    private class ObbActionHandler extends Handler {
        private final List<ObbAction> mActions = new LinkedList();
        private boolean mBound = false;

        ObbActionHandler(Looper l) {
            super(l);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ObbAction action = (ObbAction) msg.obj;
                    if (this.mBound || connectToService()) {
                        this.mActions.add(action);
                        break;
                    } else {
                        action.notifyObbStateChange(new ObbException(20, "Failed to bind to media container service"));
                        return;
                    }
                case 2:
                    if (msg.obj != null) {
                        IMediaContainerService unused = StorageManagerService.this.mContainerService = (IMediaContainerService) msg.obj;
                    }
                    if (StorageManagerService.this.mContainerService != null) {
                        if (this.mActions.size() <= 0) {
                            Slog.w(StorageManagerService.TAG, "Empty queue");
                            break;
                        } else {
                            ObbAction action2 = this.mActions.get(0);
                            if (action2 != null) {
                                action2.execute(this);
                                break;
                            }
                        }
                    } else {
                        for (ObbAction action3 : this.mActions) {
                            action3.notifyObbStateChange(new ObbException(20, "Failed to bind to media container service"));
                        }
                        this.mActions.clear();
                        break;
                    }
                    break;
                case 3:
                    if (this.mActions.size() > 0) {
                        this.mActions.remove(0);
                    }
                    if (this.mActions.size() == 0) {
                        if (this.mBound) {
                            disconnectService();
                            break;
                        }
                    } else {
                        StorageManagerService.this.mObbActionHandler.sendEmptyMessage(2);
                        break;
                    }
                    break;
                case 4:
                    if (this.mActions.size() > 0) {
                        if (this.mBound) {
                            disconnectService();
                        }
                        if (!connectToService()) {
                            for (ObbAction action4 : this.mActions) {
                                action4.notifyObbStateChange(new ObbException(20, "Failed to bind to media container service"));
                            }
                            this.mActions.clear();
                            break;
                        }
                    }
                    break;
                case 5:
                    String path = (String) msg.obj;
                    synchronized (StorageManagerService.this.mObbMounts) {
                        List<ObbState> obbStatesToRemove = new LinkedList<>();
                        for (ObbState state : StorageManagerService.this.mObbPathToStateMap.values()) {
                            if (state.canonicalPath.startsWith(path)) {
                                obbStatesToRemove.add(state);
                            }
                        }
                        for (ObbState obbState : obbStatesToRemove) {
                            StorageManagerService.this.removeObbStateLocked(obbState);
                            try {
                                obbState.token.onObbResult(obbState.rawPath, obbState.nonce, 2);
                            } catch (RemoteException e) {
                                Slog.i(StorageManagerService.TAG, "Couldn't send unmount notification for  OBB: " + obbState.rawPath);
                            }
                        }
                    }
                    break;
            }
        }

        private boolean connectToService() {
            if (!StorageManagerService.this.mContext.bindServiceAsUser(new Intent().setComponent(StorageManagerService.DEFAULT_CONTAINER_COMPONENT), StorageManagerService.this.mDefContainerConn, 1, UserHandle.SYSTEM)) {
                return false;
            }
            this.mBound = true;
            return true;
        }

        private void disconnectService() {
            IMediaContainerService unused = StorageManagerService.this.mContainerService = null;
            this.mBound = false;
            StorageManagerService.this.mContext.unbindService(StorageManagerService.this.mDefContainerConn);
        }
    }

    private static class ObbException extends Exception {
        public final int status;

        public ObbException(int status2, String message) {
            super(message);
            this.status = status2;
        }

        public ObbException(int status2, Throwable cause) {
            super(cause.getMessage(), cause);
            this.status = status2;
        }
    }

    class ObbState implements IBinder.DeathRecipient {
        final String canonicalPath;
        final int nonce;
        final int ownerGid;
        final String rawPath;
        final IObbActionListener token;
        String volId;

        public ObbState(String rawPath2, String canonicalPath2, int callingUid, IObbActionListener token2, int nonce2, String volId2) {
            this.rawPath = rawPath2;
            this.canonicalPath = canonicalPath2;
            this.ownerGid = UserHandle.getSharedAppGid(callingUid);
            this.token = token2;
            this.nonce = nonce2;
            this.volId = volId2;
        }

        public IBinder getBinder() {
            return this.token.asBinder();
        }

        public void binderDied() {
            StorageManagerService.this.mObbActionHandler.sendMessage(StorageManagerService.this.mObbActionHandler.obtainMessage(1, new UnmountObbAction(this, true)));
        }

        public void link() throws RemoteException {
            getBinder().linkToDeath(this, 0);
        }

        public void unlink() {
            getBinder().unlinkToDeath(this, 0);
        }

        public String toString() {
            return "ObbState{" + "rawPath=" + this.rawPath + ",canonicalPath=" + this.canonicalPath + ",ownerGid=" + this.ownerGid + ",token=" + this.token + ",binder=" + getBinder() + ",volId=" + this.volId + '}';
        }
    }

    private final class StorageManagerInternalImpl extends StorageManagerInternal {
        private final CopyOnWriteArrayList<StorageManagerInternal.ExternalStorageMountPolicy> mPolicies;

        private StorageManagerInternalImpl() {
            this.mPolicies = new CopyOnWriteArrayList<>();
        }

        public void addExternalStoragePolicy(StorageManagerInternal.ExternalStorageMountPolicy policy) {
            this.mPolicies.add(policy);
        }

        public void onExternalStoragePolicyChanged(int uid, String packageName) {
            StorageManagerService.this.remountUidExternalStorage(uid, getExternalStorageMountMode(uid, packageName));
        }

        public int getExternalStorageMountMode(int uid, String packageName) {
            int mountMode = HwBootFail.STAGE_BOOT_SUCCESS;
            Iterator<StorageManagerInternal.ExternalStorageMountPolicy> it = this.mPolicies.iterator();
            while (it.hasNext()) {
                int policyMode = it.next().getMountMode(uid, packageName);
                if (policyMode == 0) {
                    return 0;
                }
                mountMode = Math.min(mountMode, policyMode);
            }
            if (mountMode == Integer.MAX_VALUE) {
                return 0;
            }
            return mountMode;
        }

        public boolean hasExternalStorage(int uid, String packageName) {
            if (uid == 1000) {
                return true;
            }
            Iterator<StorageManagerInternal.ExternalStorageMountPolicy> it = this.mPolicies.iterator();
            while (it.hasNext()) {
                if (!it.next().hasExternalStorage(uid, packageName)) {
                    return false;
                }
            }
            return true;
        }
    }

    class StorageManagerServiceHandler extends Handler {
        public StorageManagerServiceHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            String envState;
            int i = 0;
            switch (msg.what) {
                case 1:
                    StorageManagerService.this.handleSystemReady();
                    return;
                case 2:
                    StorageManagerService.this.handleDaemonConnected();
                    return;
                case 3:
                    IStorageShutdownObserver obs = (IStorageShutdownObserver) msg.obj;
                    boolean success = false;
                    try {
                        StorageManagerService.this.mVold.shutdown();
                        success = true;
                    } catch (Exception e) {
                        Slog.wtf(StorageManagerService.TAG, e);
                    }
                    if (obs != null) {
                        if (!success) {
                            i = -1;
                        }
                        try {
                            obs.onShutDownComplete(i);
                            return;
                        } catch (Exception e2) {
                            return;
                        }
                    } else {
                        return;
                    }
                case 4:
                    Slog.i(StorageManagerService.TAG, "Running fstrim idle maintenance");
                    try {
                        long unused = StorageManagerService.this.mLastMaintenance = System.currentTimeMillis();
                        StorageManagerService.this.mLastMaintenanceFile.setLastModified(StorageManagerService.this.mLastMaintenance);
                    } catch (Exception e3) {
                        Slog.e(StorageManagerService.TAG, "Unable to record last fstrim!");
                    }
                    StorageManagerService.this.fstrim(0, null);
                    Runnable callback = (Runnable) msg.obj;
                    if (callback != null) {
                        callback.run();
                        return;
                    }
                    return;
                case 5:
                    VolumeInfo vol = (VolumeInfo) msg.obj;
                    if (StorageManagerService.this.isMountDisallowed(vol)) {
                        Slog.i(StorageManagerService.TAG, "Ignoring mount " + vol.getId() + " due to policy");
                        return;
                    } else if (HwDeviceManager.disallowOp(12) && !vol.id.contains("public:179") && vol.id.contains("public:")) {
                        Slog.i(StorageManagerService.TAG, "Usb mass device is disabled by dpm");
                        return;
                    } else if (StorageManagerService.this.isSDCardDisable(vol)) {
                        Slog.i(StorageManagerService.TAG, "Ignoring mount " + vol.getId() + " due to cryptsd");
                        return;
                    } else if (StorageManagerService.this.mUserStartedFinish || vol.getType() != 0 || vol.getDiskId() == null) {
                        int mountFlags = vol.mountFlags;
                        if (HwDeviceManager.disallowOp(100) && vol.type == 0 && vol.getDisk() != null && vol.getDisk().isSd()) {
                            mountFlags |= 64;
                            if (StorageManagerService.this.sendPrimarySDCardROBroadcastIfNeeded(vol)) {
                                return;
                            }
                        }
                        if (vol.type == 0 && vol.getDisk() != null && vol.getDisk().isSd() && 1 == SystemProperties.getInt(StorageManagerService.PRIMARYSD, 0)) {
                            mountFlags |= 128;
                        }
                        try {
                            StorageManagerService.this.mVold.mount(vol.id, mountFlags, vol.mountUserId);
                            if (StorageManagerService.this.isExternalSDcard(vol)) {
                                StorageManagerService.this.mVold.sendBlockUserId(vol.id, vol.blockedUserId);
                                return;
                            }
                            return;
                        } catch (Exception e4) {
                            Slog.wtf(StorageManagerService.TAG, e4);
                            return;
                        }
                    } else {
                        Slog.i(StorageManagerService.TAG, "waiting user start finish; trying again , diskId = " + vol.getDiskId());
                        sendMessageDelayed(obtainMessage(5, msg.obj), 500);
                        return;
                    }
                case 6:
                    StorageVolume userVol = (StorageVolume) msg.obj;
                    Slog.d(StorageManagerService.TAG, "Volume " + userVol.getId() + " broadcasting " + envState + " to " + userVol.getOwner());
                    String action = VolumeInfo.getBroadcastForEnvironment(envState);
                    if (action != null) {
                        Intent intent = new Intent(action, Uri.fromFile(userVol.getPathFile()));
                        intent.putExtra("android.os.storage.extra.STORAGE_VOLUME", userVol);
                        intent.addFlags(83886080);
                        StorageManagerService.this.mContext.sendBroadcastAsUser(intent, userVol.getOwner());
                        return;
                    }
                    return;
                case 7:
                    StorageManagerService.this.mContext.sendBroadcastAsUser((Intent) msg.obj, UserHandle.ALL, "android.permission.WRITE_MEDIA_STORAGE");
                    return;
                case 8:
                    try {
                        StorageManagerService.this.unmount(((VolumeInfo) msg.obj).getId());
                        return;
                    } catch (IllegalStateException e5) {
                        return;
                    }
                case 9:
                    VolumeRecord rec = (VolumeRecord) msg.obj;
                    StorageManagerService.this.forgetPartition(rec.partGuid, rec.fsUuid);
                    return;
                case 10:
                    StorageManagerService.this.resetIfReadyAndConnected();
                    return;
                case 11:
                    Slog.i(StorageManagerService.TAG, "Running idle maintenance");
                    StorageManagerService.this.runIdleMaint((Runnable) msg.obj);
                    return;
                case 12:
                    Slog.i(StorageManagerService.TAG, "Aborting idle maintenance");
                    StorageManagerService.this.abortIdleMaint((Runnable) msg.obj);
                    return;
                case 13:
                    StorageManagerService.this.mContext.sendBroadcastAsUser((Intent) msg.obj, UserHandle.ALL);
                    return;
                case 14:
                    StorageManagerService.this.checkIfBackUpDeviceMount(((Integer) msg.obj).intValue());
                    return;
                case 15:
                    try {
                        StorageManagerService.this.mVold.check(((VolumeInfo) msg.obj).id);
                        return;
                    } catch (Exception e6) {
                        Slog.wtf(StorageManagerService.TAG, e6);
                        return;
                    }
                default:
                    return;
            }
        }
    }

    class UnmountObbAction extends ObbAction {
        private final boolean mForceUnmount;

        UnmountObbAction(ObbState obbState, boolean force) {
            super(obbState);
            this.mForceUnmount = force;
        }

        public void handleExecute() throws ObbException {
            ObbState existingState;
            StorageManagerService.this.warnOnNotMounted();
            synchronized (StorageManagerService.this.mObbMounts) {
                existingState = (ObbState) StorageManagerService.this.mObbPathToStateMap.get(this.mObbState.rawPath);
            }
            if (existingState == null) {
                throw new ObbException(23, "Missing existingState");
            } else if (existingState.ownerGid != this.mObbState.ownerGid) {
                notifyObbStateChange(new ObbException(25, "Permission denied to unmount OBB " + existingState.rawPath + " (owned by GID " + existingState.ownerGid + ")"));
            } else {
                try {
                    StorageManagerService.this.mVold.unmount(this.mObbState.volId);
                    StorageManagerService.this.mVold.destroyObb(this.mObbState.volId);
                    this.mObbState.volId = null;
                    synchronized (StorageManagerService.this.mObbMounts) {
                        StorageManagerService.this.removeObbStateLocked(existingState);
                    }
                    notifyObbStateChange(2);
                } catch (Exception e) {
                    throw new ObbException(22, (Throwable) e);
                }
            }
        }

        public String toString() {
            return "UnmountObbAction{" + this.mObbState + ",force=" + this.mForceUnmount + '}';
        }
    }

    class VoldResponseCode {
        public static final int VOLUMED_SD_MOUNTED_RO = 870;

        VoldResponseCode() {
        }
    }

    private VolumeInfo findVolumeByIdOrThrow(String id) {
        synchronized (this.mLock) {
            VolumeInfo vol = this.mVolumes.get(id);
            if (vol != null) {
                return vol;
            }
            throw new IllegalArgumentException("No volume found for ID " + id);
        }
    }

    private String findVolumeIdForPathOrThrow(String path) {
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                if (vol.path == null || !path.startsWith(vol.path)) {
                    i++;
                } else {
                    String str = vol.id;
                    return str;
                }
            }
            throw new IllegalArgumentException("No volume found for path " + path);
        }
    }

    /* access modifiers changed from: private */
    public VolumeRecord findRecordForPath(String path) {
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                if (vol.path == null || !path.startsWith(vol.path)) {
                    i++;
                } else {
                    VolumeRecord volumeRecord = this.mRecords.get(vol.fsUuid);
                    return volumeRecord;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public String scrubPath(String path) {
        if (path.startsWith(Environment.getDataDirectory().getAbsolutePath())) {
            return "internal";
        }
        VolumeRecord rec = findRecordForPath(path);
        if (rec == null || rec.createdMillis == 0) {
            return UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
        }
        return "ext:" + ((int) ((System.currentTimeMillis() - rec.createdMillis) / UnixCalendar.WEEK_IN_MILLIS)) + "w";
    }

    private VolumeInfo findStorageForUuid(String volumeUuid) {
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, volumeUuid)) {
            return storage.findVolumeById("emulated");
        }
        if (Objects.equals("primary_physical", volumeUuid)) {
            return storage.getPrimaryPhysicalVolume();
        }
        return storage.findEmulatedForPrivate(storage.findVolumeByUuid(volumeUuid));
    }

    private boolean shouldBenchmark() {
        long benchInterval = Settings.Global.getLong(this.mContext.getContentResolver(), "storage_benchmark_interval", UnixCalendar.WEEK_IN_MILLIS);
        if (benchInterval == -1) {
            return false;
        }
        if (benchInterval == 0) {
            return true;
        }
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                VolumeRecord rec = this.mRecords.get(vol.fsUuid);
                if (vol.isMountedWritable() && rec != null && System.currentTimeMillis() - rec.lastBenchMillis >= benchInterval) {
                    return true;
                }
            }
            return false;
        }
    }

    private CountDownLatch findOrCreateDiskScanLatch(String diskId) {
        CountDownLatch latch;
        synchronized (this.mLock) {
            latch = this.mDiskScanLatches.get(diskId);
            if (latch == null) {
                latch = new CountDownLatch(1);
                this.mDiskScanLatches.put(diskId, latch);
            }
        }
        return latch;
    }

    private void waitForLatch(CountDownLatch latch, String condition, long timeoutMillis) throws TimeoutException {
        long startMillis = SystemClock.elapsedRealtime();
        while (!latch.await(5000, TimeUnit.MILLISECONDS)) {
            try {
                Slog.w(TAG, "Thread " + Thread.currentThread().getName() + " still waiting for " + condition + "...");
            } catch (InterruptedException e) {
                Slog.w(TAG, "Interrupt while waiting for " + condition);
            }
            if (timeoutMillis > 0 && SystemClock.elapsedRealtime() > startMillis + timeoutMillis) {
                throw new TimeoutException("Thread " + Thread.currentThread().getName() + " gave up waiting for " + condition + " after " + timeoutMillis + "ms");
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleSystemReady() {
        initIfReadyAndConnected();
        resetIfReadyAndConnected();
        MountServiceIdler.scheduleIdlePass(this.mContext);
        FstrimServiceIdler.schedulePreFstrim(this.mContext);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("zram_enabled"), false, new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                StorageManagerService.this.refreshZramSettings();
            }
        });
        refreshZramSettings();
    }

    /* access modifiers changed from: private */
    public void refreshZramSettings() {
        String propertyValue = SystemProperties.get(ZRAM_ENABLED_PROPERTY);
        if (!BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(propertyValue)) {
            String desiredPropertyValue = Settings.Global.getInt(this.mContext.getContentResolver(), "zram_enabled", 1) != 0 ? "1" : "0";
            if (!desiredPropertyValue.equals(propertyValue)) {
                SystemProperties.set(ZRAM_ENABLED_PROPERTY, desiredPropertyValue);
            }
        }
    }

    @Deprecated
    private void killMediaProvider(List<UserInfo> users) {
        if (users != null) {
            long token = Binder.clearCallingIdentity();
            try {
                for (UserInfo user : users) {
                    if (!user.isSystemOnly()) {
                        ProviderInfo provider = this.mPms.resolveContentProvider("media", 786432, user.id);
                        if (provider != null) {
                            try {
                                ActivityManager.getService().killApplication(provider.applicationInfo.packageName, UserHandle.getAppId(provider.applicationInfo.uid), -1, "vold reset");
                                break;
                            } catch (RemoteException e) {
                            }
                        } else {
                            continue;
                        }
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    @GuardedBy("mLock")
    private void addInternalVolumeLocked() {
        VolumeInfo internal = new VolumeInfo("private", 1, null, null);
        internal.state = 2;
        internal.path = Environment.getDataDirectory().getAbsolutePath();
        this.mVolumes.put(internal.id, internal);
    }

    private void initIfReadyAndConnected() {
        Slog.d(TAG, "Thinking about init, mSystemReady=" + this.mSystemReady + ", mDaemonConnected=" + this.mDaemonConnected);
        if (this.mSystemReady && this.mDaemonConnected && !StorageManager.isFileEncryptedNativeOnly()) {
            boolean initLocked = StorageManager.isFileEncryptedEmulatedOnly();
            Slog.d(TAG, "Setting up emulation state, initlocked=" + initLocked);
            for (UserInfo user : ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers()) {
                if (initLocked) {
                    try {
                        this.mVold.lockUserKey(user.id);
                    } catch (Exception e) {
                        Slog.wtf(TAG, e);
                    }
                } else {
                    this.mVold.unlockUserKey(user.id, user.serialNumber, encodeBytes(null), encodeBytes(null));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void resetIfReadyAndConnected() {
        int[] systemUnlockedUsers;
        Slog.d(TAG, "Thinking about reset, mSystemReady=" + this.mSystemReady + ", mDaemonConnected=" + this.mDaemonConnected);
        if (this.mSystemReady && this.mDaemonConnected) {
            List<UserInfo> users = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers();
            killMediaProvider(users);
            synchronized (this.mLock) {
                systemUnlockedUsers = this.mSystemUnlockedUsers;
                this.mDisks.clear();
                this.mVolumes.clear();
                addInternalVolumeLocked();
            }
            try {
                this.mVold.reset();
                for (UserInfo user : users) {
                    this.mVold.onUserAdded(user.id, user.serialNumber);
                }
                for (int userId : systemUnlockedUsers) {
                    this.mVold.onUserStarted(userId);
                    this.mStoraged.onUserStarted(userId);
                }
                this.mVold.onSecureKeyguardStateChanged(this.mSecureKeyguardShowing);
            } catch (Exception e) {
                Slog.wtf(TAG, e);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onUnlockUser(int userId) {
        Slog.d(TAG, "onUnlockUser " + userId);
        try {
            this.mVold.onUserStarted(userId);
            this.mStoraged.onUserStarted(userId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
        Slog.d(TAG, "hava send user_started to vold , userId = " + userId);
        this.mUserStartedFinish = true;
        synchronized (this.mLock) {
            for (int i = 0; i < this.mVolumes.size(); i++) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                if (vol.isVisibleForRead(userId) && vol.isMountedReadable()) {
                    StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, false);
                    this.mHandler.obtainMessage(6, userVol).sendToTarget();
                    String envState = VolumeInfo.getEnvironmentForState(vol.getState());
                    this.mCallbacks.notifyStorageStateChanged(userVol.getPath(), envState, envState);
                }
            }
            this.mSystemUnlockedUsers = ArrayUtils.appendInt(this.mSystemUnlockedUsers, userId);
        }
    }

    /* access modifiers changed from: private */
    public void onCleanupUser(int userId) {
        Slog.d(TAG, "onCleanupUser " + userId);
        try {
            this.mVold.onUserStopped(userId);
            this.mStoraged.onUserStopped(userId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
        synchronized (this.mLock) {
            this.mSystemUnlockedUsers = ArrayUtils.removeInt(this.mSystemUnlockedUsers, userId);
        }
    }

    public void onAwakeStateChanged(boolean isAwake) {
    }

    public void onKeyguardStateChanged(boolean isShowing) {
        KeyguardManager keyguardManager = (KeyguardManager) this.mContext.getSystemService(KeyguardManager.class);
        if (keyguardManager == null) {
            Slog.i(TAG, "KeyguradManager service is null");
            return;
        }
        this.mSecureKeyguardShowing = isShowing && keyguardManager.isDeviceSecure();
        Slog.i(TAG, "onKeyguardStateChanged " + isShowing);
        try {
            this.mVold.onSecureKeyguardStateChanged(this.mSecureKeyguardShowing);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    /* access modifiers changed from: package-private */
    public void runIdleMaintenance(Runnable callback) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4, callback));
    }

    public void runMaintenance() {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        runIdleMaintenance(null);
    }

    public long lastMaintenance() {
        return this.mLastMaintenance;
    }

    public void onDaemonConnected() {
        this.mDaemonConnected = true;
        this.mHandler.obtainMessage(2).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void handleDaemonConnected() {
        initIfReadyAndConnected();
        resetIfReadyAndConnected();
        if (BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS.equals(SystemProperties.get("vold.encrypt_progress"))) {
            copyLocaleFromMountService();
        }
    }

    private void copyLocaleFromMountService() {
        try {
            String systemLocale = getField("SystemLocale");
            if (!TextUtils.isEmpty(systemLocale)) {
                Slog.d(TAG, "Got locale " + systemLocale + " from mount service");
                Locale locale = Locale.forLanguageTag(systemLocale);
                Configuration config = new Configuration();
                config.setLocale(locale);
                try {
                    ActivityManager.getService().updatePersistentConfiguration(config);
                } catch (RemoteException e) {
                    Slog.e(TAG, "Error setting system locale from mount service", e);
                }
                Slog.d(TAG, "Setting system properties to " + systemLocale + " from mount service");
                SystemProperties.set("persist.sys.locale", locale.toLanguageTag());
            }
        } catch (RemoteException e2) {
        } catch (IllegalStateException e3) {
            Slog.e(TAG, "copyLocaleFromMountService, getField threw IllegalStateException");
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void onDiskScannedLocked(DiskInfo disk) {
        int volumeCount = 0;
        for (int i = 0; i < this.mVolumes.size(); i++) {
            if (Objects.equals(disk.id, this.mVolumes.valueAt(i).getDiskId())) {
                volumeCount++;
            }
        }
        Intent intent = new Intent("android.os.storage.action.DISK_SCANNED");
        intent.addFlags(83886080);
        intent.putExtra("android.os.storage.extra.DISK_ID", disk.id);
        intent.putExtra("android.os.storage.extra.VOLUME_COUNT", volumeCount);
        this.mHandler.obtainMessage(7, intent).sendToTarget();
        CountDownLatch latch = this.mDiskScanLatches.remove(disk.id);
        if (latch != null) {
            latch.countDown();
        }
        disk.volumeCount = volumeCount;
        this.mCallbacks.notifyDiskScanned(disk, volumeCount);
    }

    public int getPrivacySpaceUserId() {
        Context context = this.mContext;
        Context context2 = this.mContext;
        for (UserInfo info : ((UserManager) context.getSystemService("user")).getUsers()) {
            if (info.isHwHiddenSpace()) {
                return info.id;
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mLock")
    public void onVolumeCreatedLocked(VolumeInfo vol) {
        if (this.mPms.isOnlyCoreApps()) {
            Slog.d(TAG, "System booted in core-only mode; ignoring volume " + vol.getId());
            return;
        }
        if (vol.type == 2) {
            VolumeInfo privateVol = ((StorageManager) this.mContext.getSystemService(StorageManager.class)).findPrivateForEmulated(vol);
            if (Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, this.mPrimaryStorageUuid) && "private".equals(privateVol.id)) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags = 1 | vol.mountFlags;
                vol.mountFlags = vol.mountFlags | 2;
                this.mHandler.obtainMessage(5, vol).sendToTarget();
            } else if (Objects.equals(privateVol.fsUuid, this.mPrimaryStorageUuid)) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags = 1 | vol.mountFlags;
                vol.mountFlags = vol.mountFlags | 2;
                this.mHandler.obtainMessage(5, vol).sendToTarget();
            }
        } else if (vol.type == 0) {
            if (Objects.equals("primary_physical", this.mPrimaryStorageUuid) && vol.disk.isDefaultPrimary()) {
                Slog.v(TAG, "Found primary storage at " + vol);
                vol.mountFlags = vol.mountFlags | 1;
                vol.mountFlags = vol.mountFlags | 2;
            }
            if (vol.disk.isAdoptable()) {
                vol.mountFlags |= 2;
            }
            vol.mountUserId = this.mCurrentUserId;
            vol.blockedUserId = -1;
            if (mIsHidSd) {
                Slog.i(TAG, "onVolumeCreatedLocked before getPrivacySpaceUserId");
                vol.blockedUserId = getPrivacySpaceUserId();
            }
            this.mHandler.obtainMessage(15, vol).sendToTarget();
        } else if (vol.type == 1) {
            this.mHandler.obtainMessage(5, vol).sendToTarget();
        } else {
            Slog.d(TAG, "Skipping automatic mounting of " + vol);
        }
    }

    /* access modifiers changed from: protected */
    @GuardedBy("mLock")
    public void onCheckVolumeCompletedLocked(VolumeInfo vol) {
        if (this.mPms.isOnlyCoreApps()) {
            Slog.d(TAG, "onCheckVolumeCompletedLocked : System booted in core-only mode; ignoring volume " + vol.getId());
            return;
        }
        if (vol.type == 0) {
            this.mHandler.obtainMessage(5, vol).sendToTarget();
        } else {
            Slog.d(TAG, "onCheckVolumeCompletedLocked : Skipping automatic mounting of " + vol);
        }
    }

    private boolean isBroadcastWorthy(VolumeInfo vol) {
        switch (vol.getType()) {
            case 0:
            case 1:
            case 2:
                switch (vol.getState()) {
                    case 0:
                    case 2:
                    case 3:
                    case 5:
                    case 6:
                    case 8:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void onVolumeStateChangedLocked(VolumeInfo vol, int oldState, int newState) {
        if (HwFrameworkFactory.getHwApiCacheManagerEx() != null) {
            HwFrameworkFactory.getHwApiCacheManagerEx().notifyVolumeStateChanged(oldState, newState);
        }
        if (vol.isMountedReadable() && !TextUtils.isEmpty(vol.fsUuid)) {
            VolumeRecord rec = this.mRecords.get(vol.fsUuid);
            if (rec == null) {
                VolumeRecord rec2 = new VolumeRecord(vol.type, vol.fsUuid);
                rec2.partGuid = vol.partGuid;
                rec2.createdMillis = System.currentTimeMillis();
                if (vol.type == 1) {
                    rec2.nickname = vol.disk.getDescription();
                }
                this.mRecords.put(rec2.fsUuid, rec2);
                writeSettingsLocked();
            } else if (TextUtils.isEmpty(rec.partGuid)) {
                rec.partGuid = vol.partGuid;
                writeSettingsLocked();
            }
        }
        this.mCallbacks.notifyVolumeStateChanged(vol, oldState, newState);
        if (this.mBootCompleted && isBroadcastWorthy(vol)) {
            Flog.i(1002, "onVolumeStateChangedLocked volumeInfo = " + vol.toString());
            Intent intent = new Intent("android.os.storage.action.VOLUME_STATE_CHANGED");
            intent.putExtra("android.os.storage.extra.VOLUME_ID", vol.id);
            intent.putExtra("android.os.storage.extra.VOLUME_STATE", newState);
            intent.putExtra("android.os.storage.extra.FS_UUID", vol.fsUuid);
            intent.addFlags(83886080);
            this.mHandler.obtainMessage(7, intent).sendToTarget();
        }
        String oldStateEnv = VolumeInfo.getEnvironmentForState(oldState);
        String newStateEnv = VolumeInfo.getEnvironmentForState(newState);
        if (!Objects.equals(oldStateEnv, newStateEnv)) {
            for (int userId : this.mSystemUnlockedUsers) {
                if (vol.isVisibleForRead(userId)) {
                    StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, false);
                    this.mHandler.obtainMessage(6, userVol).sendToTarget();
                    this.mCallbacks.notifyStorageStateChanged(userVol.getPath(), oldStateEnv, newStateEnv);
                }
            }
            if (3 == newState) {
                Intent intent2 = new Intent("com.huawei.android.MEDIA_MOUNTED_RO");
                intent2.putExtra("android.os.storage.extra.STORAGE_VOLUME", vol);
                intent2.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
                Flog.i(1002, "SD card romounted volumeInfo = " + vol.toString());
                this.mHandler.obtainMessage(7, intent2).sendToTarget();
            }
        }
        if (vol.type == 0 && vol.state == 5) {
            this.mObbActionHandler.sendMessage(this.mObbActionHandler.obtainMessage(5, vol.path));
        }
        maybeLogMediaMount(vol, newState);
    }

    private void maybeLogMediaMount(VolumeInfo vol, int newState) {
        if (SecurityLog.isLoggingEnabled()) {
            DiskInfo disk = vol.getDisk();
            if (disk != null && (disk.flags & 12) != 0) {
                String label = disk.label != null ? disk.label.trim() : BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
                if (newState == 2 || newState == 3) {
                    SecurityLog.writeEvent(210013, new Object[]{vol.path, label});
                } else if (newState == 0 || newState == 8) {
                    SecurityLog.writeEvent(210014, new Object[]{vol.path, label});
                }
            }
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void onMoveStatusLocked(int status) {
        if (this.mMoveCallback == null) {
            Slog.w(TAG, "Odd, status but no move requested");
            return;
        }
        try {
            this.mMoveCallback.onStatusChanged(-1, status, -1);
        } catch (RemoteException e) {
        }
        if (status == 82) {
            Slog.d(TAG, "Move to " + this.mMoveTargetUuid + " copy phase finshed; persisting");
            this.mPrimaryStorageUuid = this.mMoveTargetUuid;
            writeSettingsLocked();
        }
        if (PackageManager.isMoveStatusFinished(status)) {
            Slog.d(TAG, "Move to " + this.mMoveTargetUuid + " finished with status " + status);
            this.mMoveCallback = null;
            this.mMoveTargetUuid = null;
        }
    }

    /* access modifiers changed from: protected */
    public void enforcePermission(String perm) {
        this.mContext.enforceCallingOrSelfPermission(perm, perm);
    }

    /* access modifiers changed from: private */
    public boolean isMountDisallowed(VolumeInfo vol) {
        UserManager userManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        boolean isUsbRestricted = false;
        if (vol.disk != null && vol.disk.isUsb()) {
            isUsbRestricted = userManager.hasUserRestriction("no_usb_file_transfer", Binder.getCallingUserHandle());
        }
        boolean isTypeRestricted = false;
        if (vol.type == 0 || vol.type == 1) {
            isTypeRestricted = userManager.hasUserRestriction("no_physical_media", Binder.getCallingUserHandle());
        }
        if (isUsbRestricted || isTypeRestricted) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean isSDCardDisable(VolumeInfo vol) {
        if (!"wait_unlock".equals(getCryptsdState()) || vol.type != 0 || vol.getDisk() == null || !vol.getDisk().isSd()) {
            return false;
        }
        Slog.i(TAG, "wait_unlock, disallow domount");
        return true;
    }

    /* access modifiers changed from: private */
    public boolean sendPrimarySDCardROBroadcastIfNeeded(VolumeInfo vol) {
        long ident = Binder.clearCallingIdentity();
        try {
            if (1 == SystemProperties.getInt(PRIMARYSD, 0)) {
                Intent intentReboot = new Intent("com.huawei.android.PRIMARYSD_MOUNTED_RO");
                intentReboot.addFlags(DumpState.DUMP_SERVICE_PERMISSIONS);
                Flog.i(1002, "Primary SD card ro volumeInfo = " + vol.toString());
                this.mHandler.obtainMessage(7, intentReboot).sendToTarget();
                return true;
            }
            Binder.restoreCallingIdentity(ident);
            return false;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private String getCryptsdState() {
        long ident = Binder.clearCallingIdentity();
        try {
            if (!SystemProperties.getBoolean("ro.config.support_sdcard_crypt", true)) {
                return "none";
            }
            String state = SystemProperties.get("vold.cryptsd.state", "none");
            Slog.i(TAG, "CryptsdState: " + state);
            Binder.restoreCallingIdentity(ident);
            return state;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private void enforceAdminUser() {
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        int callingUserId = UserHandle.getCallingUserId();
        long token = Binder.clearCallingIdentity();
        try {
            if (!um.getUserInfo(callingUserId).isAdmin()) {
                throw new SecurityException("Only admin users can adopt sd cards");
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public StorageManagerService(Context context) {
        sSelf = this;
        this.mContext = context;
        this.mCallbacks = new Callbacks(FgThread.get().getLooper());
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mPms = (PackageManagerService) ServiceManager.getService("package");
        HandlerThread hthread = new HandlerThread(TAG);
        hthread.start();
        this.mHandler = new StorageManagerServiceHandler(hthread.getLooper());
        this.mObbActionHandler = new ObbActionHandler(IoThread.get().getLooper());
        this.mLastMaintenanceFile = new File(new File(Environment.getDataDirectory(), "system"), LAST_FSTRIM_FILE);
        if (!this.mLastMaintenanceFile.exists()) {
            try {
                new FileOutputStream(this.mLastMaintenanceFile).close();
            } catch (IOException e) {
                Slog.e(TAG, "Unable to create fstrim record " + this.mLastMaintenanceFile.getPath());
            }
        } else {
            this.mLastMaintenance = this.mLastMaintenanceFile.lastModified();
        }
        this.mSettingsFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), "storage.xml"), "storage-settings");
        synchronized (this.mLock) {
            readSettingsLocked();
        }
        LocalServices.addService(StorageManagerInternal.class, this.mStorageManagerInternal);
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_ADDED");
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(this.mUserReceiver, userFilter, null, this.mHandler);
        IntentFilter bootFilter = new IntentFilter();
        bootFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mToVoldBroadcastReceiver, bootFilter, null, this.mHandler);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.DREAMING_STOPPED");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.DREAMING_STARTED");
        this.mContext.registerReceiver(this.mScreenAndDreamingReceiver, filter, null, this.mHandler);
        synchronized (this.mLock) {
            addInternalVolumeLocked();
        }
    }

    /* access modifiers changed from: private */
    public void start() {
        connect();
    }

    /* access modifiers changed from: private */
    public void connect() {
        IBinder binder = ServiceManager.getService("storaged");
        if (binder != null) {
            try {
                binder.linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        Slog.w(StorageManagerService.TAG, "storaged died; reconnecting");
                        IStoraged unused = StorageManagerService.this.mStoraged = null;
                        StorageManagerService.this.connect();
                    }
                }, 0);
            } catch (RemoteException e) {
                binder = null;
            }
        }
        if (binder != null) {
            this.mStoraged = IStoraged.Stub.asInterface(binder);
        } else {
            Slog.w(TAG, "storaged not found; trying again");
        }
        IBinder binder2 = ServiceManager.getService("vold");
        if (binder2 != null) {
            try {
                binder2.linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        Slog.w(StorageManagerService.TAG, "vold died; reconnecting");
                        StorageManagerService.this.mVold = null;
                        StorageManagerService.this.connect();
                    }
                }, 0);
            } catch (RemoteException e2) {
                binder2 = null;
            }
        }
        if (binder2 != null) {
            this.mVold = IVold.Stub.asInterface(binder2);
            try {
                this.mVold.setListener(this.mListener);
            } catch (RemoteException e3) {
                this.mVold = null;
                Slog.w(TAG, "vold listener rejected; trying again", e3);
            }
        } else {
            Slog.w(TAG, "vold not found; trying again");
        }
        if (this.mStoraged == null || this.mVold == null) {
            BackgroundThread.getHandler().postDelayed(new Runnable() {
                public final void run() {
                    StorageManagerService.this.connect();
                }
            }, 1000);
        } else {
            onDaemonConnected();
        }
    }

    /* access modifiers changed from: private */
    public void systemReady() {
        ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class)).registerScreenObserver(this);
        this.mSystemReady = true;
        this.mHandler.obtainMessage(1).sendToTarget();
    }

    /* access modifiers changed from: private */
    public void bootCompleted() {
        this.mBootCompleted = true;
        int deviceCode = getUsbDeviceExInfo();
        if (deviceCode < 0) {
            Slog.i(TAG, "getUsbDeviceExInfo deviceCode < 0 ,return .");
        } else {
            this.mHandler.obtainMessage(14, Integer.valueOf(deviceCode)).sendToTarget();
        }
    }

    private String getDefaultPrimaryStorageUuid() {
        if (SystemProperties.getBoolean("ro.vold.primary_physical", false)) {
            return "primary_physical";
        }
        return StorageManager.UUID_PRIVATE_INTERNAL;
    }

    @GuardedBy("mLock")
    private void readSettingsLocked() {
        this.mRecords.clear();
        this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
        FileInputStream fis = null;
        try {
            fis = this.mSettingsFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(fis, StandardCharsets.UTF_8.name());
            while (true) {
                int next = in.next();
                int type = next;
                boolean validAttr = true;
                if (next == 1) {
                    break;
                } else if (type == 2) {
                    String tag = in.getName();
                    if (TAG_VOLUMES.equals(tag)) {
                        int version = XmlUtils.readIntAttribute(in, ATTR_VERSION, 1);
                        boolean primaryPhysical = SystemProperties.getBoolean("ro.vold.primary_physical", false);
                        if (version < 3) {
                            if (version < 2 || primaryPhysical) {
                                validAttr = false;
                            }
                        }
                        if (validAttr) {
                            this.mPrimaryStorageUuid = XmlUtils.readStringAttribute(in, ATTR_PRIMARY_STORAGE_UUID);
                        }
                    } else if (TAG_VOLUME.equals(tag)) {
                        VolumeRecord rec = readVolumeRecord(in);
                        this.mRecords.put(rec.fsUuid, rec);
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
            Slog.wtf(TAG, "Failed reading metadata", e2);
        } catch (XmlPullParserException e3) {
            Slog.wtf(TAG, "Failed reading metadata", e3);
        } catch (Throwable th) {
            IoUtils.closeQuietly(null);
            throw th;
        }
        IoUtils.closeQuietly(fis);
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void writeSettingsLocked() {
        try {
            FileOutputStream fos = this.mSettingsFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, true);
            out.startTag(null, TAG_VOLUMES);
            XmlUtils.writeIntAttribute(out, ATTR_VERSION, 3);
            XmlUtils.writeStringAttribute(out, ATTR_PRIMARY_STORAGE_UUID, this.mPrimaryStorageUuid);
            int size = this.mRecords.size();
            for (int i = 0; i < size; i++) {
                writeVolumeRecord(out, this.mRecords.valueAt(i));
            }
            out.endTag(null, TAG_VOLUMES);
            out.endDocument();
            this.mSettingsFile.finishWrite(fos);
        } catch (IOException e) {
            if (0 != 0) {
                this.mSettingsFile.failWrite(null);
            }
        }
    }

    public static VolumeRecord readVolumeRecord(XmlPullParser in) throws IOException {
        VolumeRecord meta = new VolumeRecord(XmlUtils.readIntAttribute(in, "type"), XmlUtils.readStringAttribute(in, ATTR_FS_UUID));
        meta.partGuid = XmlUtils.readStringAttribute(in, ATTR_PART_GUID);
        meta.nickname = XmlUtils.readStringAttribute(in, ATTR_NICKNAME);
        meta.userFlags = XmlUtils.readIntAttribute(in, ATTR_USER_FLAGS);
        meta.createdMillis = XmlUtils.readLongAttribute(in, ATTR_CREATED_MILLIS);
        meta.lastTrimMillis = XmlUtils.readLongAttribute(in, ATTR_LAST_TRIM_MILLIS);
        meta.lastBenchMillis = XmlUtils.readLongAttribute(in, ATTR_LAST_BENCH_MILLIS);
        return meta;
    }

    public static void writeVolumeRecord(XmlSerializer out, VolumeRecord rec) throws IOException {
        out.startTag(null, TAG_VOLUME);
        XmlUtils.writeIntAttribute(out, "type", rec.type);
        XmlUtils.writeStringAttribute(out, ATTR_FS_UUID, rec.fsUuid);
        XmlUtils.writeStringAttribute(out, ATTR_PART_GUID, rec.partGuid);
        XmlUtils.writeStringAttribute(out, ATTR_NICKNAME, rec.nickname);
        XmlUtils.writeIntAttribute(out, ATTR_USER_FLAGS, rec.userFlags);
        XmlUtils.writeLongAttribute(out, ATTR_CREATED_MILLIS, rec.createdMillis);
        XmlUtils.writeLongAttribute(out, ATTR_LAST_TRIM_MILLIS, rec.lastTrimMillis);
        XmlUtils.writeLongAttribute(out, ATTR_LAST_BENCH_MILLIS, rec.lastBenchMillis);
        out.endTag(null, TAG_VOLUME);
    }

    public void registerListener(IStorageEventListener listener) {
        this.mCallbacks.register(listener);
    }

    public void unregisterListener(IStorageEventListener listener) {
        this.mCallbacks.unregister(listener);
    }

    public void shutdown(IStorageShutdownObserver observer) {
        enforcePermission("android.permission.SHUTDOWN");
        Slog.i(TAG, "Shutting down");
        this.mHandler.obtainMessage(3, observer).sendToTarget();
    }

    public boolean isExternalSDcard(VolumeInfo vol) {
        if (vol != null && vol.type == 0 && vol.getDisk() != null && vol.getDisk().isSd()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void bootCompleteToVold() {
        try {
            Flog.i(1002, "begin send BootCompleteToVold");
        } catch (Exception e) {
            Flog.e(1002, "other Exception ");
        }
    }

    public void mount(String volId) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        Slog.i(TAG, "mount : volId = " + volId + " pid = " + Binder.getCallingPid() + ",uid = " + Binder.getCallingUid());
        if (isMountDisallowed(vol)) {
            throw new SecurityException("Mounting " + volId + " restricted by policy");
        } else if (HwDeviceManager.disallowOp(12) && !vol.id.contains("public:179") && vol.id.contains("public:")) {
            Slog.i(TAG, "Usb mass device is disabled by dpm");
            throw new SecurityException("Mounting " + volId + " failed because of USB otg is disabled by dpm");
        } else if (!isSDCardDisable(vol)) {
            int mountFlags = vol.mountFlags;
            if (HwDeviceManager.disallowOp(100) && vol.type == 0 && vol.getDisk() != null && vol.getDisk().isSd()) {
                mountFlags |= 64;
                if (sendPrimarySDCardROBroadcastIfNeeded(vol)) {
                    return;
                }
            }
            vol.blockedUserId = -1;
            if (mIsHidSd) {
                Slog.i(TAG, "mount before getPrivacySpaceUserId");
                vol.blockedUserId = getPrivacySpaceUserId();
            }
            if (vol.type == 0 && vol.getDisk() != null && vol.getDisk().isSd() && 1 == SystemProperties.getInt(PRIMARYSD, 0)) {
                mountFlags |= 128;
            }
            try {
                this.mVold.mount(vol.id, mountFlags, vol.mountUserId);
                if (isExternalSDcard(vol)) {
                    this.mVold.sendBlockUserId(vol.id, vol.blockedUserId);
                }
            } catch (Exception e) {
                Slog.wtf(TAG, e);
            }
        } else {
            throw new SecurityException("Mounting " + volId + " restricted by cryptsd");
        }
    }

    public void unmount(String volId) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        VolumeInfo vol = findVolumeByIdOrThrow(volId);
        try {
            Slog.i(TAG, "unmount : volId = " + volId + " pid = " + Binder.getCallingPid() + ",uid = " + Binder.getCallingUid());
            this.mVold.unmount(vol.id);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void format(String volId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        try {
            this.mVold.format(findVolumeByIdOrThrow(volId).id, UiModeManagerService.Shell.NIGHT_MODE_STR_AUTO);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void benchmark(String volId, final IVoldTaskListener listener) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        try {
            this.mVold.benchmark(volId, new IVoldTaskListener.Stub() {
                public void onStatus(int status, PersistableBundle extras) {
                    StorageManagerService.this.dispatchOnStatus(listener, status, extras);
                }

                public void onFinished(int status, PersistableBundle extras) {
                    StorageManagerService.this.dispatchOnFinished(listener, status, extras);
                    String path = extras.getString("path");
                    String ident = extras.getString("ident");
                    long create = extras.getLong("create");
                    long run = extras.getLong("run");
                    long destroy = extras.getLong("destroy");
                    DropBoxManager dropBox = (DropBoxManager) StorageManagerService.this.mContext.getSystemService(DropBoxManager.class);
                    if (dropBox != null) {
                        dropBox.addText(StorageManagerService.TAG_STORAGE_BENCHMARK, StorageManagerService.this.scrubPath(path) + " " + ident + " " + create + " " + run + " " + destroy);
                    }
                    synchronized (StorageManagerService.this.mLock) {
                        VolumeRecord rec = StorageManagerService.this.findRecordForPath(path);
                        if (rec != null) {
                            rec.lastBenchMillis = System.currentTimeMillis();
                            StorageManagerService.this.writeSettingsLocked();
                        }
                    }
                }
            });
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public void partitionPublic(String diskId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            this.mVold.partition(diskId, 0, -1);
            waitForLatch(latch, "partitionPublic", 180000);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void partitionPrivate(String diskId) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        enforceAdminUser();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            this.mVold.partition(diskId, 1, -1);
            waitForLatch(latch, "partitionPrivate", 180000);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void partitionMixed(String diskId, int ratio) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        enforceAdminUser();
        CountDownLatch latch = findOrCreateDiskScanLatch(diskId);
        try {
            this.mVold.partition(diskId, 2, ratio);
            waitForLatch(latch, "partitionMixed", 180000);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void setVolumeNickname(String fsUuid, String nickname) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = this.mRecords.get(fsUuid);
            rec.nickname = nickname;
            this.mCallbacks.notifyVolumeRecordChanged(rec);
            writeSettingsLocked();
        }
    }

    public void setVolumeUserFlags(String fsUuid, int flags, int mask) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = this.mRecords.get(fsUuid);
            rec.userFlags = (rec.userFlags & (~mask)) | (flags & mask);
            this.mCallbacks.notifyVolumeRecordChanged(rec);
            writeSettingsLocked();
        }
    }

    public void forgetVolume(String fsUuid) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        Preconditions.checkNotNull(fsUuid);
        synchronized (this.mLock) {
            VolumeRecord rec = this.mRecords.remove(fsUuid);
            if (rec != null && !TextUtils.isEmpty(rec.partGuid)) {
                this.mHandler.obtainMessage(9, rec).sendToTarget();
            }
            this.mCallbacks.notifyVolumeForgotten(fsUuid);
            if (Objects.equals(this.mPrimaryStorageUuid, fsUuid)) {
                this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
                this.mHandler.obtainMessage(10).sendToTarget();
            }
            writeSettingsLocked();
        }
    }

    public void forgetAllVolumes() {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        synchronized (this.mLock) {
            for (int i = 0; i < this.mRecords.size(); i++) {
                String fsUuid = this.mRecords.keyAt(i);
                VolumeRecord rec = this.mRecords.valueAt(i);
                if (!TextUtils.isEmpty(rec.partGuid)) {
                    this.mHandler.obtainMessage(9, rec).sendToTarget();
                }
                this.mCallbacks.notifyVolumeForgotten(fsUuid);
            }
            this.mRecords.clear();
            if (!Objects.equals(StorageManager.UUID_PRIVATE_INTERNAL, this.mPrimaryStorageUuid)) {
                this.mPrimaryStorageUuid = getDefaultPrimaryStorageUuid();
            }
            writeSettingsLocked();
            this.mHandler.obtainMessage(10).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void forgetPartition(String partGuid, String fsUuid) {
        try {
            this.mVold.forgetPartition(partGuid, fsUuid);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void fstrim(int flags, final IVoldTaskListener listener) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        try {
            this.mVold.fstrim(flags, new IVoldTaskListener.Stub() {
                public void onStatus(int status, PersistableBundle extras) {
                    StorageManagerService.this.dispatchOnStatus(listener, status, extras);
                    if (status == 0) {
                        String path = extras.getString("path");
                        long bytes = extras.getLong("bytes");
                        long time = extras.getLong("time");
                        DropBoxManager dropBox = (DropBoxManager) StorageManagerService.this.mContext.getSystemService(DropBoxManager.class);
                        if (dropBox != null) {
                            dropBox.addText(StorageManagerService.TAG_STORAGE_TRIM, StorageManagerService.this.scrubPath(path) + " " + bytes + " " + time);
                        }
                        synchronized (StorageManagerService.this.mLock) {
                            VolumeRecord rec = StorageManagerService.this.findRecordForPath(path);
                            if (rec != null) {
                                rec.lastTrimMillis = System.currentTimeMillis();
                                StorageManagerService.this.writeSettingsLocked();
                            }
                        }
                    }
                }

                public void onFinished(int status, PersistableBundle extras) {
                    StorageManagerService.this.dispatchOnFinished(listener, status, extras);
                }
            });
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    /* access modifiers changed from: package-private */
    public void runIdleMaint(final Runnable callback) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        try {
            this.mLastMaintenance = System.currentTimeMillis();
            this.mLastMaintenanceFile.setLastModified(this.mLastMaintenance);
        } catch (Exception e) {
            Slog.e(TAG, "Unable to record last fstrim!");
        }
        try {
            this.mVold.runIdleMaint(new IVoldTaskListener.Stub() {
                public void onStatus(int status, PersistableBundle extras) {
                }

                public void onFinished(int status, PersistableBundle extras) {
                    if (callback != null) {
                        BackgroundThread.getHandler().post(callback);
                    }
                }
            });
        } catch (Exception e2) {
            Slog.wtf(TAG, e2);
        }
    }

    public void runIdleMaintenance() {
        runIdleMaint(null);
    }

    /* access modifiers changed from: package-private */
    public void abortIdleMaint(final Runnable callback) {
        enforcePermission("android.permission.MOUNT_FORMAT_FILESYSTEMS");
        try {
            this.mVold.abortIdleMaint(new IVoldTaskListener.Stub() {
                public void onStatus(int status, PersistableBundle extras) {
                }

                public void onFinished(int status, PersistableBundle extras) {
                    if (callback != null) {
                        BackgroundThread.getHandler().post(callback);
                    }
                }
            });
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void abortIdleMaintenance() {
        abortIdleMaint(null);
    }

    /* access modifiers changed from: private */
    public void remountUidExternalStorage(int uid, int mode) {
        try {
            this.mVold.remountUid(uid, mode);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void setDebugFlags(int flags, int mask) {
        String value;
        String value2;
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        boolean z = true;
        if ((mask & 4) != 0) {
            if (StorageManager.isFileEncryptedNativeOnly()) {
                throw new IllegalStateException("Emulation not supported on device with native FBE");
            } else if (!this.mLockPatternUtils.isCredentialRequiredToDecrypt(false)) {
                long token = Binder.clearCallingIdentity();
                try {
                    SystemProperties.set("persist.sys.emulate_fbe", Boolean.toString((flags & 4) != 0));
                    ((PowerManager) this.mContext.getSystemService(PowerManager.class)).reboot(null);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalStateException("Emulation requires disabling 'Secure start-up' in Settings > Security");
            }
        }
        if ((mask & 3) != 0) {
            if ((flags & 1) != 0) {
                value2 = "force_on";
            } else if ((flags & 2) != 0) {
                value2 = "force_off";
            } else {
                value2 = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            long token2 = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.adoptable", value2);
                this.mHandler.obtainMessage(10).sendToTarget();
            } finally {
                Binder.restoreCallingIdentity(token2);
            }
        }
        if ((mask & 24) != 0) {
            if ((flags & 8) != 0) {
                value = "force_on";
            } else if ((flags & 16) != 0) {
                value = "force_off";
            } else {
                value = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
            }
            long token3 = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.sdcardfs", value);
                this.mHandler.obtainMessage(10).sendToTarget();
            } finally {
                Binder.restoreCallingIdentity(token3);
            }
        }
        if ((mask & 32) != 0) {
            if ((flags & 32) == 0) {
                z = false;
            }
            boolean enabled = z;
            long token4 = Binder.clearCallingIdentity();
            try {
                SystemProperties.set("persist.sys.virtual_disk", Boolean.toString(enabled));
                this.mHandler.obtainMessage(10).sendToTarget();
            } finally {
                Binder.restoreCallingIdentity(token4);
            }
        }
    }

    public String getPrimaryStorageUuid() {
        String str;
        synchronized (this.mLock) {
            str = this.mPrimaryStorageUuid;
        }
        return str;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        r8.mVold.moveStorage(r2.id, r3.id, new com.android.server.StorageManagerService.AnonymousClass12(r8));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00d1, code lost:
        android.util.Slog.wtf(TAG, r0);
     */
    public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) {
        enforcePermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS");
        synchronized (this.mLock) {
            if (Objects.equals(this.mPrimaryStorageUuid, volumeUuid)) {
                throw new IllegalArgumentException("Primary storage already at " + volumeUuid);
            } else if (this.mMoveCallback == null) {
                this.mMoveCallback = callback;
                this.mMoveTargetUuid = volumeUuid;
                for (UserInfo user : ((UserManager) this.mContext.getSystemService(UserManager.class)).getUsers()) {
                    if (StorageManager.isFileEncryptedNativeOrEmulated() && !isUserKeyUnlocked(user.id)) {
                        Slog.w(TAG, "Failing move due to locked user " + user.id);
                        onMoveStatusLocked(-10);
                        return;
                    }
                }
                if (!Objects.equals("primary_physical", this.mPrimaryStorageUuid)) {
                    if (!Objects.equals("primary_physical", volumeUuid)) {
                        VolumeInfo from = findStorageForUuid(this.mPrimaryStorageUuid);
                        VolumeInfo to = findStorageForUuid(volumeUuid);
                        if (from == null) {
                            Slog.w(TAG, "Failing move due to missing from volume " + this.mPrimaryStorageUuid);
                            onMoveStatusLocked(-6);
                            return;
                        } else if (to == null) {
                            Slog.w(TAG, "Failing move due to missing to volume " + volumeUuid);
                            onMoveStatusLocked(-6);
                            return;
                        }
                    }
                }
                Slog.d(TAG, "Skipping move to/from primary physical");
                onMoveStatusLocked(82);
                onMoveStatusLocked(-100);
                this.mHandler.obtainMessage(10).sendToTarget();
            } else {
                throw new IllegalStateException("Move already in progress");
            }
        }
    }

    /* access modifiers changed from: private */
    public void warnOnNotMounted() {
        synchronized (this.mLock) {
            int i = 0;
            while (i < this.mVolumes.size()) {
                VolumeInfo vol = this.mVolumes.valueAt(i);
                if (!vol.isPrimary() || !vol.isMountedWritable()) {
                    i++;
                } else {
                    return;
                }
            }
            Slog.w(TAG, "No primary storage mounted!");
        }
    }

    /* access modifiers changed from: private */
    public boolean isUidOwnerOfPackageOrSystem(String packageName, int callerUid) {
        boolean z = true;
        if (callerUid == 1000) {
            return true;
        }
        if (packageName == null) {
            return false;
        }
        if (callerUid != this.mPms.getPackageUid(packageName, 268435456, UserHandle.getUserId(callerUid))) {
            z = false;
        }
        return z;
    }

    public String getMountedObbPath(String rawPath) {
        ObbState state;
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        warnOnNotMounted();
        synchronized (this.mObbMounts) {
            state = this.mObbPathToStateMap.get(rawPath);
        }
        if (state != null) {
            return findVolumeByIdOrThrow(state.volId).getPath().getAbsolutePath();
        }
        Slog.w(TAG, "Failed to find OBB mounted at " + rawPath);
        return null;
    }

    public boolean isObbMounted(String rawPath) {
        boolean containsKey;
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        synchronized (this.mObbMounts) {
            containsKey = this.mObbPathToStateMap.containsKey(rawPath);
        }
        return containsKey;
    }

    public void mountObb(String rawPath, String canonicalPath, String key, IObbActionListener token, int nonce) {
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        Preconditions.checkNotNull(canonicalPath, "canonicalPath cannot be null");
        Preconditions.checkNotNull(token, "token cannot be null");
        int callingUid = Binder.getCallingUid();
        ObbState obbState = new ObbState(rawPath, canonicalPath, callingUid, token, nonce, null);
        this.mObbActionHandler.sendMessage(this.mObbActionHandler.obtainMessage(1, new MountObbAction(obbState, key, callingUid)));
    }

    public void unmountObb(String rawPath, boolean force, IObbActionListener token, int nonce) {
        ObbState existingState;
        Preconditions.checkNotNull(rawPath, "rawPath cannot be null");
        synchronized (this.mObbMounts) {
            existingState = this.mObbPathToStateMap.get(rawPath);
        }
        if (existingState != null) {
            ObbState newState = new ObbState(rawPath, existingState.canonicalPath, Binder.getCallingUid(), token, nonce, existingState.volId);
            this.mObbActionHandler.sendMessage(this.mObbActionHandler.obtainMessage(1, new UnmountObbAction(newState, force)));
            return;
        }
        Slog.w(TAG, "Unknown OBB mount at " + rawPath);
    }

    public int getEncryptionState() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        try {
            return this.mVold.fdeComplete();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return -1;
        }
    }

    public int decryptStorage(String password) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (!TextUtils.isEmpty(password)) {
            try {
                this.mVold.fdeCheckPassword(password);
                this.mHandler.postDelayed(new Runnable() {
                    public final void run() {
                        StorageManagerService.lambda$decryptStorage$1(StorageManagerService.this);
                    }
                }, 1000);
                return 0;
            } catch (Exception e) {
                Slog.wtf(TAG, e);
                return -1;
            }
        } else {
            throw new IllegalArgumentException("password cannot be empty");
        }
    }

    public static /* synthetic */ void lambda$decryptStorage$1(StorageManagerService storageManagerService) {
        try {
            storageManagerService.mVold.fdeRestart();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public int encryptStorage(int type, String password) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (type == 1) {
            password = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        } else if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password cannot be empty");
        }
        try {
            this.mVold.fdeEnable(type, password, 0);
            return 0;
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return -1;
        }
    }

    public int changeEncryptionPassword(int type, String password) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (StorageManager.isFileEncryptedNativeOnly()) {
            return -1;
        }
        if (type == 1) {
            password = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        } else if (TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("password cannot be empty");
        }
        try {
            this.mVold.fdeChangePassword(type, password);
            return 0;
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return -1;
        }
    }

    public int verifyEncryptionPassword(String password) throws RemoteException {
        if (Binder.getCallingUid() == 1000) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
            if (!TextUtils.isEmpty(password)) {
                try {
                    this.mVold.fdeVerifyPassword(password);
                    return 0;
                } catch (Exception e) {
                    Slog.wtf(TAG, e);
                    return -1;
                }
            } else {
                throw new IllegalArgumentException("password cannot be empty");
            }
        } else {
            throw new SecurityException("no permission to access the crypt keeper");
        }
    }

    public int getPasswordType() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        try {
            return this.mVold.fdeGetPasswordType();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return -1;
        }
    }

    public void setField(String field, String contents) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (!StorageManager.isFileEncryptedNativeOnly()) {
            try {
                this.mVold.fdeSetField(field, contents);
            } catch (Exception e) {
                Slog.wtf(TAG, e);
            }
        }
    }

    public String getField(String field) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        if (StorageManager.isFileEncryptedNativeOnly()) {
            return null;
        }
        try {
            return this.mVold.fdeGetField(field);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return null;
        }
    }

    public boolean isConvertibleToFBE() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "no permission to access the crypt keeper");
        try {
            return this.mVold.isConvertibleToFbe();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return false;
        }
    }

    public String getPassword() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "only keyguard can retrieve password");
        try {
            return this.mVold.fdeGetPassword();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
            return null;
        }
    }

    public void clearPassword() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CRYPT_KEEPER", "only keyguard can clear password");
        try {
            this.mVold.fdeClearPassword();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void createUserKey(int userId, int serialNumber, boolean ephemeral) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.createUserKey(userId, serialNumber, ephemeral);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void destroyUserKey(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.destroyUserKey(userId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    /* access modifiers changed from: protected */
    public String encodeBytes(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return "!";
        }
        return HexDump.toHexString(bytes);
    }

    public void addUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.addUserKeyAuth(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void fixateNewestUserKeyAuth(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.fixateNewestUserKeyAuth(userId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            if (!this.mLockPatternUtils.isSecure(userId) || !ArrayUtils.isEmpty(secret)) {
                try {
                    this.mVold.unlockUserKey(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
                } catch (Exception e) {
                    Slog.wtf(TAG, e);
                    return;
                }
            } else {
                throw new IllegalStateException("Secret required to unlock secure user " + userId);
            }
        }
        synchronized (this.mLock) {
            this.mLocalUnlockedUsers = ArrayUtils.appendInt(this.mLocalUnlockedUsers, userId);
        }
    }

    public void lockUserKey(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.lockUserKey(userId);
            synchronized (this.mLock) {
                this.mLocalUnlockedUsers = ArrayUtils.removeInt(this.mLocalUnlockedUsers, userId);
            }
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public boolean isUserKeyUnlocked(int userId) {
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mLocalUnlockedUsers, userId);
        }
        return contains;
    }

    public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.prepareUserStorage(volumeUuid, userId, serialNumber, flags);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void destroyUserStorage(String volumeUuid, int userId, int flags) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            this.mVold.destroyUserStorage(volumeUuid, userId, flags);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public AppFuseMount mountProxyFileDescriptorBridge() {
        AppFuseMount appFuseMount;
        Slog.v(TAG, "mountProxyFileDescriptorBridge");
        int uid = Binder.getCallingUid();
        int pid = Binder.getCallingPid();
        while (true) {
            synchronized (this.mAppFuseLock) {
                boolean newlyCreated = false;
                if (this.mAppFuseBridge == null) {
                    this.mAppFuseBridge = new AppFuseBridge();
                    new Thread(this.mAppFuseBridge, AppFuseBridge.TAG).start();
                    newlyCreated = true;
                }
                try {
                    int name = this.mNextAppFuseName;
                    this.mNextAppFuseName = name + 1;
                    appFuseMount = new AppFuseMount(name, this.mAppFuseBridge.addBridge(new AppFuseMountScope(uid, pid, name)));
                    break;
                } catch (NativeDaemonConnectorException e) {
                    throw e.rethrowAsParcelableException();
                } catch (FuseUnavailableMountException e2) {
                    if (newlyCreated) {
                        Slog.e(TAG, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS, e2);
                        return null;
                    }
                    this.mAppFuseBridge = null;
                }
            }
            return appFuseMount;
        }
    }

    public ParcelFileDescriptor openProxyFileDescriptor(int mountId, int fileId, int mode) {
        Slog.v(TAG, "mountProxyFileDescriptor");
        int pid = Binder.getCallingPid();
        try {
            synchronized (this.mAppFuseLock) {
                if (this.mAppFuseBridge == null) {
                    Slog.e(TAG, "FuseBridge has not been created");
                    return null;
                }
                ParcelFileDescriptor openFile = this.mAppFuseBridge.openFile(pid, mountId, fileId, mode);
                return openFile;
            }
        } catch (FuseUnavailableMountException | InterruptedException error) {
            Slog.v(TAG, "The mount point has already been invalid", error);
            return null;
        }
    }

    public void mkdirs(String callingPkg, String appPath) {
        int userId = UserHandle.getUserId(Binder.getCallingUid());
        Environment.UserEnvironment userEnv = new Environment.UserEnvironment(userId);
        String propertyName = "sys.user." + userId + ".ce_available";
        if (!isUserKeyUnlocked(userId)) {
            throw new IllegalStateException("Failed to prepare " + appPath);
        } else if (userId != 0 || SystemProperties.getBoolean(propertyName, false)) {
            ((AppOpsManager) this.mContext.getSystemService("appops")).checkPackage(Binder.getCallingUid(), callingPkg);
            try {
                File appFile = new File(appPath).getCanonicalFile();
                if (FileUtils.contains(userEnv.buildExternalStorageAppDataDirs(callingPkg), appFile) || FileUtils.contains(userEnv.buildExternalStorageAppObbDirs(callingPkg), appFile) || FileUtils.contains(userEnv.buildExternalStorageAppMediaDirs(callingPkg), appFile)) {
                    String appPath2 = appFile.getAbsolutePath();
                    if (!appPath2.endsWith(SliceClientPermissions.SliceAuthority.DELIMITER)) {
                        appPath2 = appPath2 + SliceClientPermissions.SliceAuthority.DELIMITER;
                    }
                    try {
                        this.mVold.mkdirs(appPath2);
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to prepare " + appPath2 + ": " + e);
                    }
                } else {
                    throw new SecurityException("Invalid mkdirs path: " + appFile);
                }
            } catch (IOException e2) {
                throw new IllegalStateException("Failed to resolve " + appPath + ": " + e2);
            }
        } else {
            throw new IllegalStateException("Failed to prepare " + appPath);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00b4, code lost:
        if (r2.getPath() != null) goto L_0x00b9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0101, code lost:
        if (r17 != false) goto L_0x0156;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0103, code lost:
        android.util.Log.w(TAG, "No primary storage defined yet; hacking together a stub");
        r0 = android.os.SystemProperties.getBoolean("ro.vold.primary_physical", false);
        r4 = android.os.Environment.getLegacyExternalStorageDirectory();
        r36 = r0;
        r22 = new android.os.storage.StorageVolume("stub_primary", r4, r4, r1.mContext.getString(17039374), true, r0, !r0, false, 0, new android.os.UserHandle(r3), null, "removed");
        r11.add(0, r22);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0162, code lost:
        return (android.os.storage.StorageVolume[]) r11.toArray(new android.os.storage.StorageVolume[r11.size()]);
     */
    public StorageVolume[] getVolumeList(int uid, String packageName, int flags) {
        boolean forWrite;
        UserInfo userInfo;
        boolean match;
        boolean z;
        int i;
        int i2 = flags;
        int userId = UserHandle.getUserId(uid);
        boolean forWrite2 = (i2 & 256) != 0;
        boolean realState = (i2 & 512) != 0;
        boolean includeInvisible = (i2 & 1024) != 0;
        long token = Binder.clearCallingIdentity();
        UserInfo userInfo2 = null;
        try {
            boolean userKeyUnlocked = isUserKeyUnlocked(userId);
            boolean storagePermission = this.mStorageManagerInternal.hasExternalStorage(uid, packageName);
            if (this.mCurrentUserId != userId) {
                try {
                    userInfo2 = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUserInfo(userId);
                } catch (Throwable th) {
                    th = th;
                    boolean z2 = forWrite2;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
            UserInfo userInfo3 = userInfo2;
            Binder.restoreCallingIdentity(token);
            boolean userKeyUnlocked2 = userKeyUnlocked;
            ArrayList<StorageVolume> res = new ArrayList<>();
            synchronized (this.mLock) {
                boolean foundPrimary = false;
                int i3 = 0;
                while (i3 < this.mVolumes.size()) {
                    VolumeInfo vol = this.mVolumes.valueAt(i3);
                    int type = vol.getType();
                    if (type == 0 || type == 2) {
                        if (forWrite2) {
                            if (userInfo3 != null) {
                                try {
                                    if (userInfo3.isClonedProfile()) {
                                        i = userInfo3.profileGroupId;
                                        match = vol.isVisibleForWrite(i);
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    UserInfo userInfo4 = userInfo3;
                                    boolean z3 = forWrite2;
                                    throw th;
                                }
                            }
                            i = userId;
                            match = vol.isVisibleForWrite(i);
                        } else {
                            try {
                                if (!vol.isVisibleForRead((userInfo3 == null || !userInfo3.isClonedProfile()) ? userId : userInfo3.profileGroupId)) {
                                    if (includeInvisible) {
                                    }
                                    z = false;
                                    match = z;
                                }
                                z = true;
                                match = z;
                            } catch (Throwable th3) {
                                th = th3;
                                UserInfo userInfo5 = userInfo3;
                                boolean z4 = forWrite2;
                                throw th;
                            }
                        }
                        if (match) {
                            boolean reportUnmounted = false;
                            userInfo = userInfo3;
                            try {
                                forWrite = forWrite2;
                                if (vol.getType() == 2 && !userKeyUnlocked2) {
                                    reportUnmounted = true;
                                } else if (!storagePermission && !realState) {
                                    reportUnmounted = true;
                                }
                                StorageVolume userVol = vol.buildStorageVolume(this.mContext, userId, reportUnmounted);
                                if (vol.isPrimary()) {
                                    res.add(0, userVol);
                                    foundPrimary = true;
                                } else {
                                    res.add(userVol);
                                }
                                i3++;
                                userInfo3 = userInfo;
                                forWrite2 = forWrite;
                                int i4 = flags;
                                int i5 = uid;
                                String str = packageName;
                            } catch (Throwable th4) {
                                th = th4;
                                throw th;
                            }
                        }
                    }
                    userInfo = userInfo3;
                    forWrite = forWrite2;
                    i3++;
                    userInfo3 = userInfo;
                    forWrite2 = forWrite;
                    int i42 = flags;
                    int i52 = uid;
                    String str2 = packageName;
                }
                boolean z5 = forWrite2;
            }
        } catch (Throwable th5) {
            th = th5;
            boolean z6 = forWrite2;
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    public DiskInfo[] getDisks() {
        DiskInfo[] res;
        synchronized (this.mLock) {
            res = new DiskInfo[this.mDisks.size()];
            for (int i = 0; i < this.mDisks.size(); i++) {
                res[i] = this.mDisks.valueAt(i);
            }
        }
        return res;
    }

    /* JADX INFO: finally extract failed */
    public VolumeInfo[] getVolumes(int flags) {
        VolumeInfo[] volumeInfoArr;
        synchronized (this.mLock) {
            ArrayList<VolumeInfo> list = new ArrayList<>();
            int privacy_id = -1;
            if (mIsHidSd) {
                long token = Binder.clearCallingIdentity();
                try {
                    privacy_id = getPrivacySpaceUserId();
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            }
            for (int i = 0; i < this.mVolumes.size(); i++) {
                if (UserHandle.getUserId(Binder.getCallingUid()) != privacy_id || !isExternalSDcard(this.mVolumes.valueAt(i)) || UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
                    list.add(this.mVolumes.valueAt(i));
                }
            }
            volumeInfoArr = (VolumeInfo[]) list.toArray(new VolumeInfo[list.size()]);
        }
        return volumeInfoArr;
    }

    public VolumeRecord[] getVolumeRecords(int flags) {
        VolumeRecord[] res;
        synchronized (this.mLock) {
            res = new VolumeRecord[this.mRecords.size()];
            for (int i = 0; i < this.mRecords.size(); i++) {
                res[i] = this.mRecords.valueAt(i);
            }
        }
        return res;
    }

    public long getCacheQuotaBytes(String volumeUuid, int uid) {
        if (uid != Binder.getCallingUid()) {
            this.mContext.enforceCallingPermission("android.permission.STORAGE_INTERNAL", TAG);
        }
        long token = Binder.clearCallingIdentity();
        try {
            return ((StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class)).getCacheQuotaBytes(volumeUuid, uid);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public long getCacheSizeBytes(String volumeUuid, int uid) {
        if (uid != Binder.getCallingUid()) {
            this.mContext.enforceCallingPermission("android.permission.STORAGE_INTERNAL", TAG);
        }
        long token = Binder.clearCallingIdentity();
        try {
            long cacheBytes = ((StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class)).queryStatsForUid(volumeUuid, uid).getCacheBytes();
            Binder.restoreCallingIdentity(token);
            return cacheBytes;
        } catch (IOException e) {
            throw new ParcelableException(e);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }

    private int adjustAllocateFlags(int flags, int callingUid, String callingPackage) {
        if ((flags & 1) != 0) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.ALLOCATE_AGGRESSIVE", TAG);
        }
        int flags2 = flags & -3 & -5;
        AppOpsManager appOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        long token = Binder.clearCallingIdentity();
        try {
            if (appOps.isOperationActive(26, callingUid, callingPackage)) {
                Slog.d(TAG, "UID " + callingUid + " is actively using camera; letting them defy reserved cached data");
                flags2 |= 4;
            }
            return flags2;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public long getAllocatableBytes(String volumeUuid, int flags, String callingPackage) {
        String str = volumeUuid;
        int flags2 = adjustAllocateFlags(flags, Binder.getCallingUid(), callingPackage);
        StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        StorageStatsManager stats = (StorageStatsManager) this.mContext.getSystemService(StorageStatsManager.class);
        long token = Binder.clearCallingIdentity();
        try {
            File path = storage.findPathForUuid(str);
            long usable = path.getUsableSpace();
            long lowReserved = storage.getStorageLowBytes(path);
            long fullReserved = storage.getStorageFullBytes(path);
            long lowReserved2 = lowReserved;
            if (!stats.isQuotaSupported(str) || SystemProperties.getInt("persist.sys.install_no_quota", 1) != 1) {
                StorageManager storageManager = storage;
                if ((flags2 & 1) != 0) {
                    long max = Math.max(0, usable - fullReserved);
                    Binder.restoreCallingIdentity(token);
                    return max;
                }
                long max2 = Math.max(0, usable - lowReserved2);
                Binder.restoreCallingIdentity(token);
                return max2;
            }
            long cacheTotal = stats.getCacheBytes(str);
            File file = path;
            StorageManager storageManager2 = storage;
            try {
                long cacheClearable = Math.max(0, cacheTotal - storage.getStorageCacheBytes(path, flags2));
                if ((flags2 & 1) != 0) {
                    long j = cacheTotal;
                    long max3 = Math.max(0, (usable + cacheClearable) - fullReserved);
                    Binder.restoreCallingIdentity(token);
                    return max3;
                }
                long max4 = Math.max(0, (usable + cacheClearable) - lowReserved2);
                Binder.restoreCallingIdentity(token);
                return max4;
            } catch (IOException e) {
                e = e;
                try {
                    throw new ParcelableException(e);
                } catch (Throwable th) {
                    e = th;
                    Binder.restoreCallingIdentity(token);
                    throw e;
                }
            }
        } catch (IOException e2) {
            e = e2;
            StorageManager storageManager3 = storage;
            throw new ParcelableException(e);
        } catch (Throwable th2) {
            e = th2;
            StorageManager storageManager4 = storage;
            Binder.restoreCallingIdentity(token);
            throw e;
        }
    }

    public void allocateBytes(String volumeUuid, long bytes, int flags, String callingPackage) {
        long bytes2;
        int flags2 = adjustAllocateFlags(flags, Binder.getCallingUid(), callingPackage);
        long allocatableBytes = getAllocatableBytes(volumeUuid, flags2, callingPackage);
        if (bytes <= allocatableBytes) {
            StorageManager storage = (StorageManager) this.mContext.getSystemService(StorageManager.class);
            long token = Binder.clearCallingIdentity();
            try {
                File path = storage.findPathForUuid(volumeUuid);
                if ((flags2 & 1) != 0) {
                    bytes2 = bytes + storage.getStorageFullBytes(path);
                } else {
                    bytes2 = bytes + storage.getStorageLowBytes(path);
                }
                this.mPms.freeStorage(volumeUuid, bytes2, flags2);
                Binder.restoreCallingIdentity(token);
            } catch (IOException e) {
                throw new ParcelableException(e);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        } else {
            throw new ParcelableException(new IOException("Failed to allocate " + bytes + " because only " + allocatableBytes + " allocatable"));
        }
    }

    /* access modifiers changed from: private */
    public void addObbStateLocked(ObbState obbState) throws RemoteException {
        IBinder binder = obbState.getBinder();
        List<ObbState> obbStates = this.mObbMounts.get(binder);
        if (obbStates == null) {
            obbStates = new ArrayList<>();
            this.mObbMounts.put(binder, obbStates);
        } else {
            for (ObbState o : obbStates) {
                if (o.rawPath.equals(obbState.rawPath)) {
                    throw new IllegalStateException("Attempt to add ObbState twice. This indicates an error in the StorageManagerService logic.");
                }
            }
        }
        obbStates.add(obbState);
        try {
            obbState.link();
            this.mObbPathToStateMap.put(obbState.rawPath, obbState);
        } catch (RemoteException e) {
            obbStates.remove(obbState);
            if (obbStates.isEmpty()) {
                this.mObbMounts.remove(binder);
            }
            throw e;
        }
    }

    /* access modifiers changed from: private */
    public void removeObbStateLocked(ObbState obbState) {
        IBinder binder = obbState.getBinder();
        List<ObbState> obbStates = this.mObbMounts.get(binder);
        if (obbStates != null) {
            if (obbStates.remove(obbState)) {
                obbState.unlink();
            }
            if (obbStates.isEmpty()) {
                this.mObbMounts.remove(binder);
            }
        }
        this.mObbPathToStateMap.remove(obbState.rawPath);
    }

    /* access modifiers changed from: private */
    public void dispatchOnStatus(IVoldTaskListener listener, int status, PersistableBundle extras) {
        if (listener != null) {
            try {
                listener.onStatus(status, extras);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void dispatchOnFinished(IVoldTaskListener listener, int status, PersistableBundle extras) {
        if (listener != null) {
            try {
                listener.onFinished(status, extras);
            } catch (RemoteException e) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, writer)) {
            IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ", 160);
            synchronized (this.mLock) {
                pw.println("Disks:");
                pw.increaseIndent();
                for (int i = 0; i < this.mDisks.size(); i++) {
                    this.mDisks.valueAt(i).dump(pw);
                }
                pw.decreaseIndent();
                pw.println();
                pw.println("Volumes:");
                pw.increaseIndent();
                for (int i2 = 0; i2 < this.mVolumes.size(); i2++) {
                    VolumeInfo vol = this.mVolumes.valueAt(i2);
                    if (!"private".equals(vol.id)) {
                        vol.dump(pw);
                    }
                }
                pw.decreaseIndent();
                pw.println();
                pw.println("Records:");
                pw.increaseIndent();
                for (int i3 = 0; i3 < this.mRecords.size(); i3++) {
                    this.mRecords.valueAt(i3).dump(pw);
                }
                pw.decreaseIndent();
                pw.println();
                pw.println("Primary storage UUID: " + this.mPrimaryStorageUuid);
                Pair<String, Long> pair = StorageManager.getPrimaryStoragePathAndSize();
                if (pair == null) {
                    pw.println("Internal storage total size: N/A");
                } else {
                    pw.print("Internal storage (");
                    pw.print((String) pair.first);
                    pw.print(") total size: ");
                    pw.print(pair.second);
                    pw.print(" (");
                    pw.print(DataUnit.MEBIBYTES.toBytes(((Long) pair.second).longValue()));
                    pw.println(" MiB)");
                }
                pw.println("Local unlocked users: " + Arrays.toString(this.mLocalUnlockedUsers));
                pw.println("System unlocked users: " + Arrays.toString(this.mSystemUnlockedUsers));
            }
            synchronized (this.mObbMounts) {
                pw.println();
                pw.println("mObbMounts:");
                pw.increaseIndent();
                for (Map.Entry<IBinder, List<ObbState>> e : this.mObbMounts.entrySet()) {
                    pw.println(e.getKey() + ":");
                    pw.increaseIndent();
                    for (ObbState obbState : e.getValue()) {
                        pw.println(obbState);
                    }
                    pw.decreaseIndent();
                }
                pw.decreaseIndent();
                pw.println();
                pw.println("mObbPathToStateMap:");
                pw.increaseIndent();
                for (Map.Entry<String, ObbState> e2 : this.mObbPathToStateMap.entrySet()) {
                    pw.print(e2.getKey());
                    pw.print(" -> ");
                    pw.println(e2.getValue());
                }
                pw.decreaseIndent();
            }
            pw.println();
            pw.print("Last maintenance: ");
            pw.println(TimeUtils.formatForLogging(this.mLastMaintenance));
        }
    }

    public void monitor() {
        try {
            this.mVold.monitor();
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public boolean isSecure() {
        return new LockPatternUtils(this.mContext).isSecure(ActivityManager.getCurrentUser());
    }

    public boolean isSecureEx(int userId) {
        return new LockPatternUtils(this.mContext).isSecure(userId);
    }

    public void onCryptsdMessage(String message) {
    }

    public int getUsbDeviceExInfo() {
        return -1;
    }

    /* access modifiers changed from: protected */
    public void checkIfBackUpDeviceMount(int deviceCode) {
    }
}
