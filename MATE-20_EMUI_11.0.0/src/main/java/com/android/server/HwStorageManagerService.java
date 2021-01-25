package com.android.server;

import android.content.Context;
import android.content.pm.IPackageMoveObserver;
import android.content.res.ObbInfo;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.IVoldTaskListener;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.IObbActionListener;
import android.os.storage.IStorageEventListener;
import android.os.storage.IStorageShutdownObserver;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.telephony.HwTelephonyManagerInner;
import android.util.Slog;
import com.android.internal.os.AppFuseMount;
import com.android.internal.util.ArrayUtils;
import com.android.server.location.HwLocalLocationProvider;
import com.huawei.server.security.HwServiceSecurityPartsFactoryEx;
import com.huawei.server.security.fileprotect.DefaultHwSfpService;

public class HwStorageManagerService extends StorageManagerService {
    private static final String BASE_PARTITION_NAME = "base";
    private static final String CREATE_USER_KEY_ISEC = "create_user_key_isec";
    private static final int DEFAULT_PRELOAD_POLICY_FLAG = -1;
    private static final String DESTROY_USER_KEY_ISEC = "destroy_user_key_isec";
    private static final int FILE_INVALID_NULL = -201;
    private static final int FILE_INVALID_PATH = -202;
    private static final String GET_KEY_DESC = "get_key_desc";
    private static final String GET_PRE_LOAD_POLICY_FLAG = "get_pre_load_policy_flag";
    private static final int HONOR_STORAGE_SERIAL = 1;
    private static final int HUAWEI_EARL_PRODUCT_ID = 15168;
    private static final int HUAWEI_EARL_STORAGE_SERIAL = 0;
    private static final int HUAWEI_EARL_VENDOR_ID = 4817;
    private static final long HWFS_GET_DEV_NAME_ERROR = -1;
    private static final int INVALID_DEVICE_SERIAL = -1;
    private static final String LOCK_USER_KEY_ISEC = "lock_user_key_isec";
    private static final String LOCK_USER_SCREEN_ISEC = "lock_user_screen_isec";
    private static final String MANAGE_USE_SECURITY = "com.huawei.permission.MANAGE_USE_SECURITY";
    private static final int NATIVE_METHOD_NOT_FOUND = -201;
    private static final int NATIVE_METHOD_NOT_VALID = -200;
    private static final int PARTITION_SIZE = 2;
    private static final int PARTITION_START_POS = 1;
    private static final int PERMISSION_NOT_ALLOW = -200;
    private static final String PRELOAD_PARTITION_NAME = "preload";
    public static final int REQUEST_SET_DESCRIPTOR = 7;
    private static final int SCREEN_FLAG_FACIAL_RECOGNITION = 4;
    private static final int SCREEN_FLAG_FINGERPRINT_UNLOCK = 3;
    private static final int SCREEN_FLAG_LOCK_SCREEN = 1;
    private static final int SCREEN_FLAG_PASSWORD_UNLOCK = 2;
    private static final String SECONDARY_USERDATA_PARTITION_NAME = "userdata2";
    private static final String SET_SCREEN_STATE_FLAG = "set_screen_state_flag";
    private static final int SIZE_STRINGBUFFER = 256;
    private static final String TAG = "HwStorageManagerService";
    private static final int UNLOCK_TYPE_FACIAL_RECOGNITION = 4;
    private static final int UNLOCK_TYPE_FINGERPRINT = 2;
    private static final int UNLOCK_TYPE_PASSWORD_GRAPHICS = 1;
    private static final String UNLOCK_USER_KEY_ISEC = "unlock_user_key_isec";
    private static final String UNLOCK_USER_SCREEN_ISEC = "unlock_user_screen_isec";
    public static final int USB_CLASS_INTERFACE = 1;
    private static final String USERDATA_PARTITION_NAME = "userdata";
    private static final String VERSION_PARTITION_NAME = "version";
    private static boolean mLoadLibraryFailed;
    private Context mContext;
    private UsbManager mUsbManager;

    private static native void finalize_native();

    private static native void init_native();

    private static native int nativeGetMaxTimeCost();

    private static native int nativeGetMinTimeCost();

    private static native int nativeGetNotificationLevel();

    private static native long nativeGetPartitionInfo(String str, int i);

    private static native int nativeGetPercentComplete();

    private static native int nativeGetUndiscardInfo();

    private static native int nativeStartClean();

    private static native int nativeStopClean();

    public /* bridge */ /* synthetic */ void abortChanges(String x0, boolean x1) throws RemoteException {
        HwStorageManagerService.super.abortChanges(x0, x1);
    }

    public /* bridge */ /* synthetic */ void abortIdleMaintenance() {
        HwStorageManagerService.super.abortIdleMaintenance();
    }

    public /* bridge */ /* synthetic */ void allocateBytes(String x0, long x1, int x2, String x3) {
        HwStorageManagerService.super.allocateBytes(x0, x1, x2, x3);
    }

    public /* bridge */ /* synthetic */ void benchmark(String x0, IVoldTaskListener x1) {
        HwStorageManagerService.super.benchmark(x0, x1);
    }

    public /* bridge */ /* synthetic */ int changeEncryptionPassword(int x0, String x1) {
        return HwStorageManagerService.super.changeEncryptionPassword(x0, x1);
    }

    public /* bridge */ /* synthetic */ void clearPassword() throws RemoteException {
        HwStorageManagerService.super.clearPassword();
    }

    public /* bridge */ /* synthetic */ void commitChanges() throws RemoteException {
        HwStorageManagerService.super.commitChanges();
    }

    public /* bridge */ /* synthetic */ void createUserKey(int x0, int x1, boolean x2) {
        HwStorageManagerService.super.createUserKey(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ int decryptStorage(String x0) {
        return HwStorageManagerService.super.decryptStorage(x0);
    }

    public /* bridge */ /* synthetic */ void destroyUserKey(int x0) {
        HwStorageManagerService.super.destroyUserKey(x0);
    }

    public /* bridge */ /* synthetic */ void destroyUserStorage(String x0, int x1, int x2) {
        HwStorageManagerService.super.destroyUserStorage(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ int encryptStorage(int x0, String x1) {
        return HwStorageManagerService.super.encryptStorage(x0, x1);
    }

    public /* bridge */ /* synthetic */ void forgetAllVolumes() {
        HwStorageManagerService.super.forgetAllVolumes();
    }

    public /* bridge */ /* synthetic */ void forgetVolume(String x0) {
        HwStorageManagerService.super.forgetVolume(x0);
    }

    public /* bridge */ /* synthetic */ void format(String x0) {
        HwStorageManagerService.super.format(x0);
    }

    public /* bridge */ /* synthetic */ void fstrim(int x0, IVoldTaskListener x1) {
        HwStorageManagerService.super.fstrim(x0, x1);
    }

    public /* bridge */ /* synthetic */ long getAllocatableBytes(String x0, int x1, String x2) {
        return HwStorageManagerService.super.getAllocatableBytes(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ long getCacheQuotaBytes(String x0, int x1) {
        return HwStorageManagerService.super.getCacheQuotaBytes(x0, x1);
    }

    public /* bridge */ /* synthetic */ long getCacheSizeBytes(String x0, int x1) {
        return HwStorageManagerService.super.getCacheSizeBytes(x0, x1);
    }

    public /* bridge */ /* synthetic */ DiskInfo[] getDisks() {
        return HwStorageManagerService.super.getDisks();
    }

    public /* bridge */ /* synthetic */ int getEncryptionState() {
        return HwStorageManagerService.super.getEncryptionState();
    }

    public /* bridge */ /* synthetic */ String getField(String x0) throws RemoteException {
        return HwStorageManagerService.super.getField(x0);
    }

    public /* bridge */ /* synthetic */ IBinder getHwInnerService() {
        return HwStorageManagerService.super.getHwInnerService();
    }

    public /* bridge */ /* synthetic */ String getMountedObbPath(String x0) {
        return HwStorageManagerService.super.getMountedObbPath(x0);
    }

    public /* bridge */ /* synthetic */ String getPassword() throws RemoteException {
        return HwStorageManagerService.super.getPassword();
    }

    public /* bridge */ /* synthetic */ int getPasswordType() {
        return HwStorageManagerService.super.getPasswordType();
    }

    public /* bridge */ /* synthetic */ String getPrimaryStorageUuid() {
        return HwStorageManagerService.super.getPrimaryStorageUuid();
    }

    public /* bridge */ /* synthetic */ int getPrivacySpaceUserId() {
        return HwStorageManagerService.super.getPrivacySpaceUserId();
    }

    public /* bridge */ /* synthetic */ VolumeInfo getVolumeInfo(String x0) {
        return HwStorageManagerService.super.getVolumeInfo(x0);
    }

    public /* bridge */ /* synthetic */ StorageVolume[] getVolumeList(int x0, String x1, int x2) {
        return HwStorageManagerService.super.getVolumeList(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ VolumeRecord[] getVolumeRecords(int x0) {
        return HwStorageManagerService.super.getVolumeRecords(x0);
    }

    public /* bridge */ /* synthetic */ VolumeInfo[] getVolumes(int x0) {
        return HwStorageManagerService.super.getVolumes(x0);
    }

    public /* bridge */ /* synthetic */ boolean isConvertibleToFBE() throws RemoteException {
        return HwStorageManagerService.super.isConvertibleToFBE();
    }

    public /* bridge */ /* synthetic */ boolean isExternalSDcard(VolumeInfo x0) {
        return HwStorageManagerService.super.isExternalSDcard(x0);
    }

    public /* bridge */ /* synthetic */ boolean isObbMounted(String x0) {
        return HwStorageManagerService.super.isObbMounted(x0);
    }

    public /* bridge */ /* synthetic */ boolean isSecure() {
        return HwStorageManagerService.super.isSecure();
    }

    public /* bridge */ /* synthetic */ boolean isSecureEx(int x0) {
        return HwStorageManagerService.super.isSecureEx(x0);
    }

    public /* bridge */ /* synthetic */ boolean isUserKeyUnlocked(int x0) {
        return HwStorageManagerService.super.isUserKeyUnlocked(x0);
    }

    public /* bridge */ /* synthetic */ long lastMaintenance() {
        return HwStorageManagerService.super.lastMaintenance();
    }

    public /* bridge */ /* synthetic */ void lockUserKey(int x0) {
        HwStorageManagerService.super.lockUserKey(x0);
    }

    public /* bridge */ /* synthetic */ void mkdirs(String x0, String x1) {
        HwStorageManagerService.super.mkdirs(x0, x1);
    }

    public /* bridge */ /* synthetic */ void monitor() {
        HwStorageManagerService.super.monitor();
    }

    public /* bridge */ /* synthetic */ void mount(String x0) {
        HwStorageManagerService.super.mount(x0);
    }

    public /* bridge */ /* synthetic */ void mountAfterCheckCompleted(VolumeInfo x0) {
        HwStorageManagerService.super.mountAfterCheckCompleted(x0);
    }

    public /* bridge */ /* synthetic */ void mountObb(String x0, String x1, String x2, IObbActionListener x3, int x4, ObbInfo x5) {
        HwStorageManagerService.super.mountObb(x0, x1, x2, x3, x4, x5);
    }

    public /* bridge */ /* synthetic */ AppFuseMount mountProxyFileDescriptorBridge() {
        return HwStorageManagerService.super.mountProxyFileDescriptorBridge();
    }

    public /* bridge */ /* synthetic */ boolean needsCheckpoint() throws RemoteException {
        return HwStorageManagerService.super.needsCheckpoint();
    }

    public /* bridge */ /* synthetic */ void notifyVolumeStateChanged(VolumeInfo x0, int x1, int x2) {
        HwStorageManagerService.super.notifyVolumeStateChanged(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void onAwakeStateChanged(boolean x0) {
        HwStorageManagerService.super.onAwakeStateChanged(x0);
    }

    public /* bridge */ /* synthetic */ void onCheckStart(String x0) {
        HwStorageManagerService.super.onCheckStart(x0);
    }

    public /* bridge */ /* synthetic */ void onDaemonConnected() {
        HwStorageManagerService.super.onDaemonConnected();
    }

    public /* bridge */ /* synthetic */ void onKeyguardStateChanged(boolean x0) {
        HwStorageManagerService.super.onKeyguardStateChanged(x0);
    }

    public /* bridge */ /* synthetic */ ParcelFileDescriptor openProxyFileDescriptor(int x0, int x1, int x2) {
        return HwStorageManagerService.super.openProxyFileDescriptor(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void partitionMixed(String x0, int x1) {
        HwStorageManagerService.super.partitionMixed(x0, x1);
    }

    public /* bridge */ /* synthetic */ void partitionPrivate(String x0) {
        HwStorageManagerService.super.partitionPrivate(x0);
    }

    public /* bridge */ /* synthetic */ void partitionPublic(String x0) {
        HwStorageManagerService.super.partitionPublic(x0);
    }

    public /* bridge */ /* synthetic */ void prepareUserStorage(String x0, int x1, int x2, int x3) {
        HwStorageManagerService.super.prepareUserStorage(x0, x1, x2, x3);
    }

    public /* bridge */ /* synthetic */ void registerListener(IStorageEventListener x0) {
        HwStorageManagerService.super.registerListener(x0);
    }

    public /* bridge */ /* synthetic */ void runIdleMaintenance() {
        HwStorageManagerService.super.runIdleMaintenance();
    }

    public /* bridge */ /* synthetic */ void runMaintenance() {
        HwStorageManagerService.super.runMaintenance();
    }

    public /* bridge */ /* synthetic */ void setDebugFlags(int x0, int x1) {
        HwStorageManagerService.super.setDebugFlags(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setField(String x0, String x1) throws RemoteException {
        HwStorageManagerService.super.setField(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setPrimaryStorageUuid(String x0, IPackageMoveObserver x1) {
        HwStorageManagerService.super.setPrimaryStorageUuid(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setVolumeNickname(String x0, String x1) {
        HwStorageManagerService.super.setVolumeNickname(x0, x1);
    }

    public /* bridge */ /* synthetic */ void setVolumeUserFlags(String x0, int x1, int x2) {
        HwStorageManagerService.super.setVolumeUserFlags(x0, x1, x2);
    }

    public /* bridge */ /* synthetic */ void shutdown(IStorageShutdownObserver x0) {
        HwStorageManagerService.super.shutdown(x0);
    }

    public /* bridge */ /* synthetic */ void startCheckpoint(int x0) throws RemoteException {
        HwStorageManagerService.super.startCheckpoint(x0);
    }

    public /* bridge */ /* synthetic */ boolean supportsCheckpoint() throws RemoteException {
        return HwStorageManagerService.super.supportsCheckpoint();
    }

    public /* bridge */ /* synthetic */ String translateInternal(String x0, int x1, int x2, boolean x3, String x4) {
        return HwStorageManagerService.super.translateInternal(x0, x1, x2, x3, x4);
    }

    public /* bridge */ /* synthetic */ void unlockUserKey(int x0, int x1, byte[] x2, byte[] x3) {
        HwStorageManagerService.super.unlockUserKey(x0, x1, x2, x3);
    }

    public /* bridge */ /* synthetic */ void unmount(String x0) {
        HwStorageManagerService.super.unmount(x0);
    }

    public /* bridge */ /* synthetic */ void unmountObb(String x0, boolean x1, IObbActionListener x2, int x3) {
        HwStorageManagerService.super.unmountObb(x0, x1, x2, x3);
    }

    public /* bridge */ /* synthetic */ void unregisterListener(IStorageEventListener x0) {
        HwStorageManagerService.super.unregisterListener(x0);
    }

    public /* bridge */ /* synthetic */ int verifyEncryptionPassword(String x0) throws RemoteException {
        return HwStorageManagerService.super.verifyEncryptionPassword(x0);
    }

    static {
        mLoadLibraryFailed = false;
        try {
            System.loadLibrary("hwstoragemanager_jni");
        } catch (UnsatisfiedLinkError e) {
            mLoadLibraryFailed = true;
            Slog.d(TAG, "hwstoragemanager_jni library not found!");
        }
    }

    public HwStorageManagerService(Context context) {
        super(context);
        this.mContext = context;
        if (!mLoadLibraryFailed) {
            init_native();
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        if (!mLoadLibraryFailed) {
            finalize_native();
        }
        try {
            HwStorageManagerService.super.finalize();
        } catch (Throwable th) {
        }
    }

    public int startClean() {
        Slog.d(TAG, "startClean:");
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingPermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS") == 0) {
            try {
                if (!mLoadLibraryFailed) {
                    return nativeStartClean();
                }
                Slog.d(TAG, "nativeStartClean not valid!");
                return -200;
            } catch (UnsatisfiedLinkError e) {
                Slog.d(TAG, "nativeStartClean not found!");
                return -201;
            }
        } else {
            throw new SecurityException("you have no permission to call startClean from uid:" + Binder.getCallingUid());
        }
    }

    public int stopClean() {
        Slog.d(TAG, "stopClean:");
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingPermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS") == 0) {
            try {
                if (!mLoadLibraryFailed) {
                    return nativeStopClean();
                }
                Slog.d(TAG, "nativeStopClean not valid!");
                return -200;
            } catch (UnsatisfiedLinkError e) {
                Slog.d(TAG, "nativeStopClean not found!");
                return -201;
            }
        } else {
            throw new SecurityException("you have no permission to call stopClean from uid:" + Binder.getCallingUid());
        }
    }

    public int getNotificationLevel() {
        Slog.d(TAG, "getNotificationLevel:");
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            try {
                if (!mLoadLibraryFailed) {
                    return nativeGetNotificationLevel();
                }
                Slog.d(TAG, "nativeGetNotificationLevel not valid!");
                return -200;
            } catch (UnsatisfiedLinkError e) {
                Slog.d(TAG, "nativeGetNotificationLevel not found!");
                return -201;
            }
        } else {
            throw new SecurityException("you have no permission to call getNotificationLevel from uid:" + Binder.getCallingUid());
        }
    }

    public int getUndiscardInfo() {
        Slog.d(TAG, "getUndiscardInfo:");
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingPermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS") == 0) {
            try {
                if (!mLoadLibraryFailed) {
                    return nativeGetUndiscardInfo();
                }
                Slog.d(TAG, "nativeGetUndiscardInfo not valid!");
                return -200;
            } catch (UnsatisfiedLinkError e) {
                Slog.d(TAG, "nativeGetUndiscardInfo not found!");
                return -201;
            }
        } else {
            throw new SecurityException("you have no permission to call getUndiscardInfo from uid:" + Binder.getCallingUid());
        }
    }

    public int getMaxTimeCost() {
        Slog.d(TAG, "getMaxTimeCost:");
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            try {
                if (!mLoadLibraryFailed) {
                    return nativeGetMaxTimeCost();
                }
                Slog.d(TAG, "nativeGetMaxTimeCost not valid!");
                return -200;
            } catch (UnsatisfiedLinkError e) {
                Slog.d(TAG, "nativeGetMaxTimeCost not found!");
                return -201;
            }
        } else {
            throw new SecurityException("you have no permission to call getMaxTimeCost from uid:" + Binder.getCallingUid());
        }
    }

    public int getMinTimeCost() {
        Slog.d(TAG, "getMinTimeCost:");
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000) {
            try {
                if (!mLoadLibraryFailed) {
                    return nativeGetMinTimeCost();
                }
                Slog.d(TAG, "nativeGetMinTimeCost not valid!");
                return -200;
            } catch (UnsatisfiedLinkError e) {
                Slog.d(TAG, "nativeGetMinTimeCost not found!");
                return -201;
            }
        } else {
            throw new SecurityException("you have no permission to call getMinTimeCost from uid:" + Binder.getCallingUid());
        }
    }

    public int getPercentComplete() {
        Slog.d(TAG, "getPercentComplete:");
        if (UserHandle.getAppId(Binder.getCallingUid()) == 1000 || this.mContext.checkCallingPermission("android.permission.MOUNT_UNMOUNT_FILESYSTEMS") == 0) {
            try {
                if (!mLoadLibraryFailed) {
                    return nativeGetPercentComplete();
                }
                Slog.d(TAG, "nativeGetPercentComplete not valid!");
                return -200;
            } catch (UnsatisfiedLinkError e) {
                Slog.d(TAG, "nativeGetPercentComplete not found!");
                return -201;
            }
        } else {
            throw new SecurityException("you have no permission to call getPercentComplete from uid:" + Binder.getCallingUid());
        }
    }

    public void unlockUserKeyISec(int userId, int serialNumber, byte[] token, byte[] secret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        if (StorageManager.isFileEncryptedNativeOrEmulated()) {
            if (!StorageManagerService.sSelf.isSecureEx(userId) || !ArrayUtils.isEmpty(secret)) {
                try {
                    Slog.d(TAG, "unlockUserKeyIsec");
                    this.mVold.unlockUserKeyIsec(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
                } catch (Exception e) {
                    Slog.wtf(TAG, e);
                    return;
                }
            } else {
                throw new IllegalStateException("Token required to unlock secure user " + userId);
            }
        }
        synchronized (this.mLock) {
            this.mLocalUnlockedUsers = ArrayUtils.appendInt(this.mLocalUnlockedUsers, userId);
        }
    }

    public void lockUserKeyISec(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            Slog.d(TAG, "lockUserKeyIsec");
            this.mVold.lockUserKeyIsec(userId);
            synchronized (this.mLock) {
                this.mLocalUnlockedUsers = ArrayUtils.removeInt(this.mLocalUnlockedUsers, userId);
            }
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void unlockUserScreenISec(int userId, int serialNumber, byte[] token, byte[] secret, int type) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        if (!StorageManager.isFileEncryptedNativeOrEmulated()) {
            return;
        }
        if (type == 1 || type == 2 || type == 4) {
            DefaultHwSfpService sfpService = HwServiceSecurityPartsFactoryEx.getInstance().getHwSfpService();
            if (sfpService != null) {
                sfpService.notifyUnlockScreen(userId);
            }
            try {
                Slog.d(TAG, "unlockUserScreenISec");
                this.mVold.unlockUserScreenIsec(userId, serialNumber, encodeBytes(token), encodeBytes(secret), type);
            } catch (Exception e) {
                Slog.e(TAG, "unlockUserScreenISec has exception");
            }
        } else {
            Slog.e(TAG, "unlockUserScreenISec error, wrong type");
        }
    }

    public void lockUserScreenISec(int userId, int serialNumber) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            Slog.d(TAG, "lockUserScreenIsec");
            this.mVold.lockUserScreenIsec(userId, serialNumber);
        } catch (Exception e) {
            Slog.e(TAG, "lockUserScreenISec has exception");
        }
    }

    public int getPreLoadPolicyFlag(int userId, int serialNumber) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        if (!StorageManager.isFileEncryptedNativeOrEmulated()) {
            return -1;
        }
        try {
            Slog.d(TAG, "getPreLoadPolicyFlag");
            return this.mVold.getPrepareFlag(userId, serialNumber);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "getPreLoadPolicyFlag failed NumberFormatException");
            return -1;
        } catch (Exception e2) {
            Slog.e(TAG, "getPreLoadPolicyFlag failed Exception");
            return -1;
        }
    }

    public boolean setScreenStateFlag(int userId, int serialNumber, int flag) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        if (!StorageManager.isFileEncryptedNativeOrEmulated()) {
            return false;
        }
        if (flag == 1 || flag == 2 || flag == 3 || flag == 4) {
            try {
                Slog.d(TAG, "setScreenStateFlag");
                this.mVold.lockScreenPrepare(userId, serialNumber, flag);
                return true;
            } catch (Exception e) {
                Slog.e(TAG, "setScreenStateFlag has exception");
                return false;
            }
        } else {
            Slog.e(TAG, "setScreenStateFlag failed wrong flag input");
            return false;
        }
    }

    public String getKeyDesc(int userId, int serialNumber, int sdpClass) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        if (!StorageManager.isFileEncryptedNativeOrEmulated()) {
            return null;
        }
        try {
            Slog.d(TAG, "getKeyDesc");
            return this.mVold.getKeyDesc(userId, serialNumber, sdpClass);
        } catch (Exception e) {
            Slog.e(TAG, "destroyUserKeyISec has exception");
            return null;
        }
    }

    public void createUserKeyISec(int userId, int serialNumber, boolean ephemeral) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            Slog.d(TAG, "createUserKeyIsec");
            this.mVold.createUserKeyIsec(userId, serialNumber, ephemeral);
        } catch (Exception e) {
            Slog.e(TAG, "createUserKeyISec has exception");
        }
    }

    public void destroyUserKeyISec(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        if (userId == 0) {
            Slog.e(TAG, "System user cannot be removed.");
            return;
        }
        try {
            Slog.d(TAG, "destroyUserKeyISec");
            this.mVold.destroyUserKeyIsec(userId);
        } catch (Exception e) {
            Slog.e(TAG, "destroyUserKeyISec has exception");
        }
    }

    public void addUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            if (SystemProperties.getBoolean("ro.config.support_iudf", false)) {
                Slog.d(TAG, "addUserKeyAuthIsec");
                this.mVold.addUserKeyAuthIsec(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
                return;
            }
            this.mVold.addUserKeyAuth(userId, serialNumber, encodeBytes(token), encodeBytes(secret));
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void fixateNewestUserKeyAuth(int userId) {
        enforcePermission("android.permission.STORAGE_INTERNAL");
        try {
            if (SystemProperties.getBoolean("ro.config.support_iudf", false)) {
                Slog.d(TAG, "fixateNewestUserKeyAuthIsec");
                this.mVold.fixateNewestUserKeyAuthIsec(userId);
                return;
            }
            this.mVold.fixateNewestUserKeyAuth(userId);
        } catch (Exception e) {
            Slog.wtf(TAG, e);
        }
    }

    public void onLockedDiskAdd() {
        HwSdLockService.getInstance(this.mContext).onLockedDiskAdd();
    }

    public void onLockedDiskRemove() {
        HwSdLockService.getInstance(this.mContext).onLockedDiskRemove();
    }

    public void onLockedDiskChange() {
        HwSdLockService.getInstance(this.mContext).onLockedDiskChange();
    }

    public void onCryptsdMessage(String message) {
        HwSdCryptdService.getInstance(this.mContext).onCryptsdMessage(message);
    }

    public long getPartitionInfo(String partitionName, int infoType) {
        if (this.mContext.checkCallingPermission(MANAGE_USE_SECURITY) != 0) {
            Slog.i(TAG, "getPartitionInfo(): permissin deny");
            return -200;
        } else if (partitionName == null) {
            Slog.i(TAG, "getPartitionInfo error partitionName null");
            return -201;
        } else if (-1 != partitionName.indexOf(46) || -1 != partitionName.indexOf(47)) {
            Slog.i(TAG, "getPartitionInfo error partitionName valid " + partitionName);
            return -202;
        } else if (!BASE_PARTITION_NAME.equals(partitionName) || infoType != 2) {
            return nativeGetPartitionInfo(partitionName, infoType);
        } else {
            long userdataAddress = nativeGetPartitionInfo(SECONDARY_USERDATA_PARTITION_NAME, 1);
            if (userdataAddress == HWFS_GET_DEV_NAME_ERROR) {
                Slog.i(TAG, "userdata2 does not exist");
                userdataAddress = nativeGetPartitionInfo(USERDATA_PARTITION_NAME, 1);
            }
            long versionRomSize = nativeGetPartitionInfo("version", 2);
            long preloadRomSize = nativeGetPartitionInfo(PRELOAD_PARTITION_NAME, 2);
            long baseRomSize = (userdataAddress - versionRomSize) - preloadRomSize;
            Slog.i(TAG, "getBasePartitionInfo: userdataAddress = " + userdataAddress + ", versionRomSize = " + versionRomSize + ", preloadRomSize = " + preloadRomSize + ", baseRomSize = " + baseRomSize);
            return baseRomSize;
        }
    }

    public void notifyDeviceStateToTelephony(String device, String state, String extras) {
        HwTelephonyManagerInner.getDefault().notifyDeviceState(device, state, extras);
    }

    public int inquireStorageSerialNumber(UsbDeviceConnection connection) {
        byte[] sStringBuffer = new byte[256];
        try {
            if (connection.controlTransfer(193, 7, 0, 0, sStringBuffer, 1, 0) >= 0) {
                return sStringBuffer[0] & HwLocalLocationProvider.LOCATION_TYPE_ACCURACY_PRIORITY;
            }
            return -1;
        } catch (Exception e) {
            Slog.e(TAG, "Can not communicate with USB device");
            return -1;
        }
    }

    public int getUsbDeviceExInfo() {
        if (this.mUsbManager == null) {
            this.mUsbManager = (UsbManager) this.mContext.getSystemService("usb");
            if (this.mUsbManager == null) {
                return -1;
            }
        }
        for (UsbDevice device : this.mUsbManager.getDeviceList().values()) {
            if (device != null && HUAWEI_EARL_PRODUCT_ID == device.getProductId() && HUAWEI_EARL_VENDOR_ID == device.getVendorId()) {
                UsbDeviceConnection connection = null;
                long ident = Binder.clearCallingIdentity();
                try {
                    if (this.mUsbManager.hasPermission(device)) {
                        connection = this.mUsbManager.openDevice(device);
                        if (connection == null) {
                            Slog.e(TAG, " openDevice error !");
                            if (connection != null) {
                                connection.close();
                            }
                            Binder.restoreCallingIdentity(ident);
                            return -1;
                        }
                        int deviceCode = inquireStorageSerialNumber(connection);
                        if (deviceCode == 0 || deviceCode == 1) {
                            if (connection != null) {
                                connection.close();
                            }
                            Binder.restoreCallingIdentity(ident);
                            return deviceCode;
                        }
                    } else {
                        Slog.e(TAG, " openDevice(): permissin deny ");
                    }
                } finally {
                    if (connection != null) {
                        connection.close();
                    }
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void checkIfBackUpDeviceMount(int deviceCode) {
        synchronized (this.mLock) {
            DiskInfo diskInfo = null;
            for (String s : this.mDisks.keySet()) {
                diskInfo = (DiskInfo) this.mDisks.get(s);
                if (diskInfo.isUsb()) {
                    int flags = diskInfo.flags;
                    if (deviceCode == 0) {
                        flags |= 64;
                    } else if (deviceCode == 1) {
                        flags |= 128;
                    }
                    diskInfo = new DiskInfo(s, flags);
                    this.mDisks.replace(s, diskInfo);
                }
            }
            for (String s2 : this.mVolumes.keySet()) {
                VolumeInfo vol = (VolumeInfo) this.mVolumes.get(s2);
                if (diskInfo != null && !vol.id.contains("public:179") && vol.id.contains("public:")) {
                    vol = new VolumeInfo(vol.getId(), vol.type, diskInfo, vol.partGuid);
                    this.mVolumes.replace(s2, vol);
                    onVolumeCreatedLocked(vol);
                }
                int[] iArr = this.mSystemUnlockedUsers;
                int length = iArr.length;
                boolean z = false;
                int i = 0;
                while (i < length) {
                    int userId = iArr[i];
                    if (vol.isVisibleForRead(userId)) {
                        this.mHandler.obtainMessage(6, vol.buildStorageVolume(this.mContext, userId, z)).sendToTarget();
                    }
                    i++;
                    z = false;
                }
            }
        }
    }
}
