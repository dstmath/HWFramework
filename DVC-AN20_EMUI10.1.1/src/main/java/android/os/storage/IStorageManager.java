package android.os.storage;

import android.content.pm.IPackageMoveObserver;
import android.content.res.ObbInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IVoldTaskListener;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.storage.IObbActionListener;
import android.os.storage.IStorageEventListener;
import android.os.storage.IStorageShutdownObserver;
import com.android.internal.os.AppFuseMount;

public interface IStorageManager extends IInterface {
    void abortChanges(String str, boolean z) throws RemoteException;

    void abortIdleMaintenance() throws RemoteException;

    void addUserKeyAuth(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    void allocateBytes(String str, long j, int i, String str2) throws RemoteException;

    void benchmark(String str, IVoldTaskListener iVoldTaskListener) throws RemoteException;

    int changeEncryptionPassword(int i, String str) throws RemoteException;

    void clearPassword() throws RemoteException;

    void commitChanges() throws RemoteException;

    void createUserKey(int i, int i2, boolean z) throws RemoteException;

    void createUserKeyISec(int i, int i2, boolean z) throws RemoteException;

    int decryptStorage(String str) throws RemoteException;

    void destroyUserKey(int i) throws RemoteException;

    void destroyUserKeyISec(int i) throws RemoteException;

    void destroyUserStorage(String str, int i, int i2) throws RemoteException;

    int encryptStorage(int i, String str) throws RemoteException;

    void fixateNewestUserKeyAuth(int i) throws RemoteException;

    void forgetAllVolumes() throws RemoteException;

    void forgetVolume(String str) throws RemoteException;

    void format(String str) throws RemoteException;

    void fstrim(int i, IVoldTaskListener iVoldTaskListener) throws RemoteException;

    long getAllocatableBytes(String str, int i, String str2) throws RemoteException;

    long getCacheQuotaBytes(String str, int i) throws RemoteException;

    long getCacheSizeBytes(String str, int i) throws RemoteException;

    DiskInfo[] getDisks() throws RemoteException;

    int getEncryptionState() throws RemoteException;

    String getField(String str) throws RemoteException;

    IBinder getHwInnerService() throws RemoteException;

    String getKeyDesc(int i, int i2, int i3) throws RemoteException;

    int getMaxTimeCost() throws RemoteException;

    int getMinTimeCost() throws RemoteException;

    String getMountedObbPath(String str) throws RemoteException;

    int getNotificationLevel() throws RemoteException;

    String getPassword() throws RemoteException;

    int getPasswordType() throws RemoteException;

    int getPercentComplete() throws RemoteException;

    int getPreLoadPolicyFlag(int i, int i2) throws RemoteException;

    String getPrimaryStorageUuid() throws RemoteException;

    int getUndiscardInfo() throws RemoteException;

    StorageVolume[] getVolumeList(int i, String str, int i2) throws RemoteException;

    VolumeRecord[] getVolumeRecords(int i) throws RemoteException;

    VolumeInfo[] getVolumes(int i) throws RemoteException;

    boolean isConvertibleToFBE() throws RemoteException;

    boolean isObbMounted(String str) throws RemoteException;

    boolean isSecure() throws RemoteException;

    boolean isUserKeyUnlocked(int i) throws RemoteException;

    long lastMaintenance() throws RemoteException;

    void lockUserKey(int i) throws RemoteException;

    void lockUserKeyISec(int i) throws RemoteException;

    void lockUserScreenISec(int i, int i2) throws RemoteException;

    void mkdirs(String str, String str2) throws RemoteException;

    void mount(String str) throws RemoteException;

    void mountObb(String str, String str2, String str3, IObbActionListener iObbActionListener, int i, ObbInfo obbInfo) throws RemoteException;

    AppFuseMount mountProxyFileDescriptorBridge() throws RemoteException;

    boolean needsCheckpoint() throws RemoteException;

    ParcelFileDescriptor openProxyFileDescriptor(int i, int i2, int i3) throws RemoteException;

    void partitionMixed(String str, int i) throws RemoteException;

    void partitionPrivate(String str) throws RemoteException;

    void partitionPublic(String str) throws RemoteException;

    void prepareUserStorage(String str, int i, int i2, int i3) throws RemoteException;

    void registerListener(IStorageEventListener iStorageEventListener) throws RemoteException;

    void runIdleMaintenance() throws RemoteException;

    void runMaintenance() throws RemoteException;

    void setDebugFlags(int i, int i2) throws RemoteException;

    void setField(String str, String str2) throws RemoteException;

    void setPrimaryStorageUuid(String str, IPackageMoveObserver iPackageMoveObserver) throws RemoteException;

    boolean setScreenStateFlag(int i, int i2, int i3) throws RemoteException;

    void setVolumeNickname(String str, String str2) throws RemoteException;

    void setVolumeUserFlags(String str, int i, int i2) throws RemoteException;

    void shutdown(IStorageShutdownObserver iStorageShutdownObserver) throws RemoteException;

    void startCheckpoint(int i) throws RemoteException;

    int startClean() throws RemoteException;

    int stopClean() throws RemoteException;

    boolean supportsCheckpoint() throws RemoteException;

    void unlockUserKey(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    void unlockUserKeyISec(int i, int i2, byte[] bArr, byte[] bArr2) throws RemoteException;

    void unlockUserScreenISec(int i, int i2, byte[] bArr, byte[] bArr2, int i3) throws RemoteException;

    void unmount(String str) throws RemoteException;

    void unmountObb(String str, boolean z, IObbActionListener iObbActionListener, int i) throws RemoteException;

    void unregisterListener(IStorageEventListener iStorageEventListener) throws RemoteException;

    int verifyEncryptionPassword(String str) throws RemoteException;

    public static class Default implements IStorageManager {
        @Override // android.os.storage.IStorageManager
        public void registerListener(IStorageEventListener listener) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void unregisterListener(IStorageEventListener listener) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void shutdown(IStorageShutdownObserver observer) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void mountObb(String rawPath, String canonicalPath, String key, IObbActionListener token, int nonce, ObbInfo obbInfo) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void unmountObb(String rawPath, boolean force, IObbActionListener token, int nonce) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public boolean isObbMounted(String rawPath) throws RemoteException {
            return false;
        }

        @Override // android.os.storage.IStorageManager
        public String getMountedObbPath(String rawPath) throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public int decryptStorage(String password) throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public int encryptStorage(int type, String password) throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public int changeEncryptionPassword(int type, String password) throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public StorageVolume[] getVolumeList(int uid, String packageName, int flags) throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public int getEncryptionState() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public int verifyEncryptionPassword(String password) throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public void mkdirs(String callingPkg, String path) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public int getPasswordType() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public String getPassword() throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public void clearPassword() throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void setField(String field, String contents) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public String getField(String field) throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public long lastMaintenance() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public void runMaintenance() throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public DiskInfo[] getDisks() throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public VolumeInfo[] getVolumes(int flags) throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public VolumeRecord[] getVolumeRecords(int flags) throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public void mount(String volId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void unmount(String volId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void format(String volId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void partitionPublic(String diskId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void partitionPrivate(String diskId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void partitionMixed(String diskId, int ratio) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void setVolumeNickname(String fsUuid, String nickname) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void setVolumeUserFlags(String fsUuid, int flags, int mask) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void forgetVolume(String fsUuid) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void forgetAllVolumes() throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public String getPrimaryStorageUuid() throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void benchmark(String volId, IVoldTaskListener listener) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void setDebugFlags(int flags, int mask) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void createUserKey(int userId, int serialNumber, boolean ephemeral) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void destroyUserKey(int userId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void lockUserKey(int userId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public boolean isUserKeyUnlocked(int userId) throws RemoteException {
            return false;
        }

        @Override // android.os.storage.IStorageManager
        public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void destroyUserStorage(String volumeUuid, int userId, int flags) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public boolean isConvertibleToFBE() throws RemoteException {
            return false;
        }

        @Override // android.os.storage.IStorageManager
        public void addUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void fixateNewestUserKeyAuth(int userId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void fstrim(int flags, IVoldTaskListener listener) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public AppFuseMount mountProxyFileDescriptorBridge() throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public ParcelFileDescriptor openProxyFileDescriptor(int mountPointId, int fileId, int mode) throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public long getCacheQuotaBytes(String volumeUuid, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public long getCacheSizeBytes(String volumeUuid, int uid) throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public long getAllocatableBytes(String volumeUuid, int flags, String callingPackage) throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public void allocateBytes(String volumeUuid, long bytes, int flags, String callingPackage) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void runIdleMaintenance() throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void abortIdleMaintenance() throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public boolean isSecure() throws RemoteException {
            return false;
        }

        @Override // android.os.storage.IStorageManager
        public int startClean() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public int stopClean() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public int getNotificationLevel() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public int getUndiscardInfo() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public int getMaxTimeCost() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public int getMinTimeCost() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public int getPercentComplete() throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public void unlockUserKeyISec(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void lockUserKeyISec(int userId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void unlockUserScreenISec(int userId, int serialNumber, byte[] token, byte[] secret, int type) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void lockUserScreenISec(int userId, int serialNumber) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public int getPreLoadPolicyFlag(int userId, int serialNumber) throws RemoteException {
            return 0;
        }

        @Override // android.os.storage.IStorageManager
        public boolean setScreenStateFlag(int userId, int serialNumber, int flag) throws RemoteException {
            return false;
        }

        @Override // android.os.storage.IStorageManager
        public String getKeyDesc(int userId, int serialNumber, int sdpClass) throws RemoteException {
            return null;
        }

        @Override // android.os.storage.IStorageManager
        public void createUserKeyISec(int userId, int serialNumber, boolean ephemeral) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void destroyUserKeyISec(int userId) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public void commitChanges() throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public boolean supportsCheckpoint() throws RemoteException {
            return false;
        }

        @Override // android.os.storage.IStorageManager
        public void startCheckpoint(int numTries) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public boolean needsCheckpoint() throws RemoteException {
            return false;
        }

        @Override // android.os.storage.IStorageManager
        public void abortChanges(String message, boolean retry) throws RemoteException {
        }

        @Override // android.os.storage.IStorageManager
        public IBinder getHwInnerService() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IStorageManager {
        private static final String DESCRIPTOR = "android.os.storage.IStorageManager";
        static final int TRANSACTION_abortChanges = 88;
        static final int TRANSACTION_abortIdleMaintenance = 81;
        static final int TRANSACTION_addUserKeyAuth = 71;
        static final int TRANSACTION_allocateBytes = 79;
        static final int TRANSACTION_benchmark = 60;
        static final int TRANSACTION_changeEncryptionPassword = 29;
        static final int TRANSACTION_clearPassword = 38;
        static final int TRANSACTION_commitChanges = 84;
        static final int TRANSACTION_createUserKey = 62;
        static final int TRANSACTION_createUserKeyISec = 601;
        static final int TRANSACTION_decryptStorage = 27;
        static final int TRANSACTION_destroyUserKey = 63;
        static final int TRANSACTION_destroyUserKeyISec = 602;
        static final int TRANSACTION_destroyUserStorage = 68;
        static final int TRANSACTION_encryptStorage = 28;
        static final int TRANSACTION_fixateNewestUserKeyAuth = 72;
        static final int TRANSACTION_forgetAllVolumes = 57;
        static final int TRANSACTION_forgetVolume = 56;
        static final int TRANSACTION_format = 50;
        static final int TRANSACTION_fstrim = 73;
        static final int TRANSACTION_getAllocatableBytes = 78;
        static final int TRANSACTION_getCacheQuotaBytes = 76;
        static final int TRANSACTION_getCacheSizeBytes = 77;
        static final int TRANSACTION_getDisks = 45;
        static final int TRANSACTION_getEncryptionState = 32;
        static final int TRANSACTION_getField = 40;
        static final int TRANSACTION_getHwInnerService = 701;
        static final int TRANSACTION_getKeyDesc = 507;
        static final int TRANSACTION_getMaxTimeCost = 1006;
        static final int TRANSACTION_getMinTimeCost = 1007;
        static final int TRANSACTION_getMountedObbPath = 25;
        static final int TRANSACTION_getNotificationLevel = 1004;
        static final int TRANSACTION_getPassword = 37;
        static final int TRANSACTION_getPasswordType = 36;
        static final int TRANSACTION_getPercentComplete = 1008;
        static final int TRANSACTION_getPreLoadPolicyFlag = 505;
        static final int TRANSACTION_getPrimaryStorageUuid = 58;
        static final int TRANSACTION_getUndiscardInfo = 1005;
        static final int TRANSACTION_getVolumeList = 30;
        static final int TRANSACTION_getVolumeRecords = 47;
        static final int TRANSACTION_getVolumes = 46;
        static final int TRANSACTION_isConvertibleToFBE = 69;
        static final int TRANSACTION_isObbMounted = 24;
        static final int TRANSACTION_isSecure = 1001;
        static final int TRANSACTION_isUserKeyUnlocked = 66;
        static final int TRANSACTION_lastMaintenance = 42;
        static final int TRANSACTION_lockUserKey = 65;
        static final int TRANSACTION_lockUserKeyISec = 502;
        static final int TRANSACTION_lockUserScreenISec = 504;
        static final int TRANSACTION_mkdirs = 35;
        static final int TRANSACTION_mount = 48;
        static final int TRANSACTION_mountObb = 22;
        static final int TRANSACTION_mountProxyFileDescriptorBridge = 74;
        static final int TRANSACTION_needsCheckpoint = 87;
        static final int TRANSACTION_openProxyFileDescriptor = 75;
        static final int TRANSACTION_partitionMixed = 53;
        static final int TRANSACTION_partitionPrivate = 52;
        static final int TRANSACTION_partitionPublic = 51;
        static final int TRANSACTION_prepareUserStorage = 67;
        static final int TRANSACTION_registerListener = 1;
        static final int TRANSACTION_runIdleMaintenance = 80;
        static final int TRANSACTION_runMaintenance = 43;
        static final int TRANSACTION_setDebugFlags = 61;
        static final int TRANSACTION_setField = 39;
        static final int TRANSACTION_setPrimaryStorageUuid = 59;
        static final int TRANSACTION_setScreenStateFlag = 506;
        static final int TRANSACTION_setVolumeNickname = 54;
        static final int TRANSACTION_setVolumeUserFlags = 55;
        static final int TRANSACTION_shutdown = 20;
        static final int TRANSACTION_startCheckpoint = 86;
        static final int TRANSACTION_startClean = 1002;
        static final int TRANSACTION_stopClean = 1003;
        static final int TRANSACTION_supportsCheckpoint = 85;
        static final int TRANSACTION_unlockUserKey = 64;
        static final int TRANSACTION_unlockUserKeyISec = 501;
        static final int TRANSACTION_unlockUserScreenISec = 503;
        static final int TRANSACTION_unmount = 49;
        static final int TRANSACTION_unmountObb = 23;
        static final int TRANSACTION_unregisterListener = 2;
        static final int TRANSACTION_verifyEncryptionPassword = 33;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IStorageManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IStorageManager)) {
                return new Proxy(obj);
            }
            return (IStorageManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "registerListener";
            }
            if (transactionCode == 2) {
                return "unregisterListener";
            }
            if (transactionCode == 32) {
                return "getEncryptionState";
            }
            if (transactionCode == 33) {
                return "verifyEncryptionPassword";
            }
            if (transactionCode == 42) {
                return "lastMaintenance";
            }
            if (transactionCode == 43) {
                return "runMaintenance";
            }
            switch (transactionCode) {
                case 20:
                    return "shutdown";
                case 45:
                    return "getDisks";
                case 46:
                    return "getVolumes";
                case 47:
                    return "getVolumeRecords";
                case 48:
                    return "mount";
                case 49:
                    return "unmount";
                case 50:
                    return "format";
                case 51:
                    return "partitionPublic";
                case 52:
                    return "partitionPrivate";
                case 53:
                    return "partitionMixed";
                case 54:
                    return "setVolumeNickname";
                case 55:
                    return "setVolumeUserFlags";
                case 56:
                    return "forgetVolume";
                case 57:
                    return "forgetAllVolumes";
                case 58:
                    return "getPrimaryStorageUuid";
                case 59:
                    return "setPrimaryStorageUuid";
                case 60:
                    return "benchmark";
                case 61:
                    return "setDebugFlags";
                case 62:
                    return "createUserKey";
                case 63:
                    return "destroyUserKey";
                case 64:
                    return "unlockUserKey";
                case 65:
                    return "lockUserKey";
                case 66:
                    return "isUserKeyUnlocked";
                case 67:
                    return "prepareUserStorage";
                case 68:
                    return "destroyUserStorage";
                case 69:
                    return "isConvertibleToFBE";
                case 601:
                    return "createUserKeyISec";
                case 602:
                    return "destroyUserKeyISec";
                case 701:
                    return "getHwInnerService";
                case 1001:
                    return "isSecure";
                case 1002:
                    return "startClean";
                case 1003:
                    return "stopClean";
                case 1004:
                    return "getNotificationLevel";
                case 1005:
                    return "getUndiscardInfo";
                case 1006:
                    return "getMaxTimeCost";
                case 1007:
                    return "getMinTimeCost";
                case 1008:
                    return "getPercentComplete";
                default:
                    switch (transactionCode) {
                        case 22:
                            return "mountObb";
                        case 23:
                            return "unmountObb";
                        case 24:
                            return "isObbMounted";
                        case 25:
                            return "getMountedObbPath";
                        default:
                            switch (transactionCode) {
                                case 27:
                                    return "decryptStorage";
                                case 28:
                                    return "encryptStorage";
                                case 29:
                                    return "changeEncryptionPassword";
                                case 30:
                                    return "getVolumeList";
                                default:
                                    switch (transactionCode) {
                                        case 35:
                                            return "mkdirs";
                                        case 36:
                                            return "getPasswordType";
                                        case 37:
                                            return "getPassword";
                                        case 38:
                                            return "clearPassword";
                                        case 39:
                                            return "setField";
                                        case 40:
                                            return "getField";
                                        default:
                                            switch (transactionCode) {
                                                case 71:
                                                    return "addUserKeyAuth";
                                                case 72:
                                                    return "fixateNewestUserKeyAuth";
                                                case 73:
                                                    return "fstrim";
                                                case 74:
                                                    return "mountProxyFileDescriptorBridge";
                                                case 75:
                                                    return "openProxyFileDescriptor";
                                                case 76:
                                                    return "getCacheQuotaBytes";
                                                case 77:
                                                    return "getCacheSizeBytes";
                                                case 78:
                                                    return "getAllocatableBytes";
                                                case 79:
                                                    return "allocateBytes";
                                                case 80:
                                                    return "runIdleMaintenance";
                                                case 81:
                                                    return "abortIdleMaintenance";
                                                default:
                                                    switch (transactionCode) {
                                                        case 84:
                                                            return "commitChanges";
                                                        case 85:
                                                            return "supportsCheckpoint";
                                                        case 86:
                                                            return "startCheckpoint";
                                                        case 87:
                                                            return "needsCheckpoint";
                                                        case 88:
                                                            return "abortChanges";
                                                        default:
                                                            switch (transactionCode) {
                                                                case 501:
                                                                    return "unlockUserKeyISec";
                                                                case 502:
                                                                    return "lockUserKeyISec";
                                                                case 503:
                                                                    return "unlockUserScreenISec";
                                                                case 504:
                                                                    return "lockUserScreenISec";
                                                                case 505:
                                                                    return "getPreLoadPolicyFlag";
                                                                case 506:
                                                                    return "setScreenStateFlag";
                                                                case 507:
                                                                    return "getKeyDesc";
                                                                default:
                                                                    return null;
                                                            }
                                                    }
                                            }
                                    }
                            }
                    }
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ObbInfo _arg5;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                registerListener(IStorageEventListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                unregisterListener(IStorageEventListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 32) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getEncryptionState();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 33) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = verifyEncryptionPassword(data.readString());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 42) {
                data.enforceInterface(DESCRIPTOR);
                long _result3 = lastMaintenance();
                reply.writeNoException();
                reply.writeLong(_result3);
                return true;
            } else if (code != 43) {
                boolean _arg2 = false;
                boolean _arg1 = false;
                boolean _arg12 = false;
                boolean _arg22 = false;
                switch (code) {
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        shutdown(IStorageShutdownObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        DiskInfo[] _result4 = getDisks();
                        reply.writeNoException();
                        reply.writeTypedArray(_result4, 1);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        VolumeInfo[] _result5 = getVolumes(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedArray(_result5, 1);
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        VolumeRecord[] _result6 = getVolumeRecords(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedArray(_result6, 1);
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        mount(data.readString());
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        unmount(data.readString());
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        format(data.readString());
                        reply.writeNoException();
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        partitionPublic(data.readString());
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        partitionPrivate(data.readString());
                        reply.writeNoException();
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        partitionMixed(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 54:
                        data.enforceInterface(DESCRIPTOR);
                        setVolumeNickname(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 55:
                        data.enforceInterface(DESCRIPTOR);
                        setVolumeUserFlags(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 56:
                        data.enforceInterface(DESCRIPTOR);
                        forgetVolume(data.readString());
                        reply.writeNoException();
                        return true;
                    case 57:
                        data.enforceInterface(DESCRIPTOR);
                        forgetAllVolumes();
                        reply.writeNoException();
                        return true;
                    case 58:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = getPrimaryStorageUuid();
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case 59:
                        data.enforceInterface(DESCRIPTOR);
                        setPrimaryStorageUuid(data.readString(), IPackageMoveObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 60:
                        data.enforceInterface(DESCRIPTOR);
                        benchmark(data.readString(), IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 61:
                        data.enforceInterface(DESCRIPTOR);
                        setDebugFlags(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 62:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        int _arg13 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        createUserKey(_arg0, _arg13, _arg2);
                        reply.writeNoException();
                        return true;
                    case 63:
                        data.enforceInterface(DESCRIPTOR);
                        destroyUserKey(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 64:
                        data.enforceInterface(DESCRIPTOR);
                        unlockUserKey(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 65:
                        data.enforceInterface(DESCRIPTOR);
                        lockUserKey(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 66:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isUserKeyUnlocked = isUserKeyUnlocked(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isUserKeyUnlocked ? 1 : 0);
                        return true;
                    case 67:
                        data.enforceInterface(DESCRIPTOR);
                        prepareUserStorage(data.readString(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 68:
                        data.enforceInterface(DESCRIPTOR);
                        destroyUserStorage(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 69:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isConvertibleToFBE = isConvertibleToFBE();
                        reply.writeNoException();
                        reply.writeInt(isConvertibleToFBE ? 1 : 0);
                        return true;
                    case 601:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        int _arg14 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg22 = true;
                        }
                        createUserKeyISec(_arg02, _arg14, _arg22);
                        reply.writeNoException();
                        return true;
                    case 602:
                        data.enforceInterface(DESCRIPTOR);
                        destroyUserKeyISec(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 701:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result8 = getHwInnerService();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result8);
                        return true;
                    case 1001:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSecure = isSecure();
                        reply.writeNoException();
                        reply.writeInt(isSecure ? 1 : 0);
                        return true;
                    case 1002:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = startClean();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 1003:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = stopClean();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 1004:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getNotificationLevel();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 1005:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getUndiscardInfo();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 1006:
                        data.enforceInterface(DESCRIPTOR);
                        int _result13 = getMaxTimeCost();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 1007:
                        data.enforceInterface(DESCRIPTOR);
                        int _result14 = getMinTimeCost();
                        reply.writeNoException();
                        reply.writeInt(_result14);
                        return true;
                    case 1008:
                        data.enforceInterface(DESCRIPTOR);
                        int _result15 = getPercentComplete();
                        reply.writeNoException();
                        reply.writeInt(_result15);
                        return true;
                    case IBinder.INTERFACE_TRANSACTION:
                        reply.writeString(DESCRIPTOR);
                        return true;
                    default:
                        switch (code) {
                            case 22:
                                data.enforceInterface(DESCRIPTOR);
                                String _arg03 = data.readString();
                                String _arg15 = data.readString();
                                String _arg23 = data.readString();
                                IObbActionListener _arg3 = IObbActionListener.Stub.asInterface(data.readStrongBinder());
                                int _arg4 = data.readInt();
                                if (data.readInt() != 0) {
                                    _arg5 = ObbInfo.CREATOR.createFromParcel(data);
                                } else {
                                    _arg5 = null;
                                }
                                mountObb(_arg03, _arg15, _arg23, _arg3, _arg4, _arg5);
                                reply.writeNoException();
                                return true;
                            case 23:
                                data.enforceInterface(DESCRIPTOR);
                                String _arg04 = data.readString();
                                if (data.readInt() != 0) {
                                    _arg12 = true;
                                }
                                unmountObb(_arg04, _arg12, IObbActionListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                                reply.writeNoException();
                                return true;
                            case 24:
                                data.enforceInterface(DESCRIPTOR);
                                boolean isObbMounted = isObbMounted(data.readString());
                                reply.writeNoException();
                                reply.writeInt(isObbMounted ? 1 : 0);
                                return true;
                            case 25:
                                data.enforceInterface(DESCRIPTOR);
                                String _result16 = getMountedObbPath(data.readString());
                                reply.writeNoException();
                                reply.writeString(_result16);
                                return true;
                            default:
                                switch (code) {
                                    case 27:
                                        data.enforceInterface(DESCRIPTOR);
                                        int _result17 = decryptStorage(data.readString());
                                        reply.writeNoException();
                                        reply.writeInt(_result17);
                                        return true;
                                    case 28:
                                        data.enforceInterface(DESCRIPTOR);
                                        int _result18 = encryptStorage(data.readInt(), data.readString());
                                        reply.writeNoException();
                                        reply.writeInt(_result18);
                                        return true;
                                    case 29:
                                        data.enforceInterface(DESCRIPTOR);
                                        int _result19 = changeEncryptionPassword(data.readInt(), data.readString());
                                        reply.writeNoException();
                                        reply.writeInt(_result19);
                                        return true;
                                    case 30:
                                        data.enforceInterface(DESCRIPTOR);
                                        StorageVolume[] _result20 = getVolumeList(data.readInt(), data.readString(), data.readInt());
                                        reply.writeNoException();
                                        reply.writeTypedArray(_result20, 1);
                                        return true;
                                    default:
                                        switch (code) {
                                            case 35:
                                                data.enforceInterface(DESCRIPTOR);
                                                mkdirs(data.readString(), data.readString());
                                                reply.writeNoException();
                                                return true;
                                            case 36:
                                                data.enforceInterface(DESCRIPTOR);
                                                int _result21 = getPasswordType();
                                                reply.writeNoException();
                                                reply.writeInt(_result21);
                                                return true;
                                            case 37:
                                                data.enforceInterface(DESCRIPTOR);
                                                String _result22 = getPassword();
                                                reply.writeNoException();
                                                reply.writeString(_result22);
                                                return true;
                                            case 38:
                                                data.enforceInterface(DESCRIPTOR);
                                                clearPassword();
                                                return true;
                                            case 39:
                                                data.enforceInterface(DESCRIPTOR);
                                                setField(data.readString(), data.readString());
                                                return true;
                                            case 40:
                                                data.enforceInterface(DESCRIPTOR);
                                                String _result23 = getField(data.readString());
                                                reply.writeNoException();
                                                reply.writeString(_result23);
                                                return true;
                                            default:
                                                switch (code) {
                                                    case 71:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        addUserKeyAuth(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                                                        reply.writeNoException();
                                                        return true;
                                                    case 72:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        fixateNewestUserKeyAuth(data.readInt());
                                                        reply.writeNoException();
                                                        return true;
                                                    case 73:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        fstrim(data.readInt(), IVoldTaskListener.Stub.asInterface(data.readStrongBinder()));
                                                        reply.writeNoException();
                                                        return true;
                                                    case 74:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        AppFuseMount _result24 = mountProxyFileDescriptorBridge();
                                                        reply.writeNoException();
                                                        if (_result24 != null) {
                                                            reply.writeInt(1);
                                                            _result24.writeToParcel(reply, 1);
                                                        } else {
                                                            reply.writeInt(0);
                                                        }
                                                        return true;
                                                    case 75:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        ParcelFileDescriptor _result25 = openProxyFileDescriptor(data.readInt(), data.readInt(), data.readInt());
                                                        reply.writeNoException();
                                                        if (_result25 != null) {
                                                            reply.writeInt(1);
                                                            _result25.writeToParcel(reply, 1);
                                                        } else {
                                                            reply.writeInt(0);
                                                        }
                                                        return true;
                                                    case 76:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        long _result26 = getCacheQuotaBytes(data.readString(), data.readInt());
                                                        reply.writeNoException();
                                                        reply.writeLong(_result26);
                                                        return true;
                                                    case 77:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        long _result27 = getCacheSizeBytes(data.readString(), data.readInt());
                                                        reply.writeNoException();
                                                        reply.writeLong(_result27);
                                                        return true;
                                                    case 78:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        long _result28 = getAllocatableBytes(data.readString(), data.readInt(), data.readString());
                                                        reply.writeNoException();
                                                        reply.writeLong(_result28);
                                                        return true;
                                                    case 79:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        allocateBytes(data.readString(), data.readLong(), data.readInt(), data.readString());
                                                        reply.writeNoException();
                                                        return true;
                                                    case 80:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        runIdleMaintenance();
                                                        reply.writeNoException();
                                                        return true;
                                                    case 81:
                                                        data.enforceInterface(DESCRIPTOR);
                                                        abortIdleMaintenance();
                                                        reply.writeNoException();
                                                        return true;
                                                    default:
                                                        switch (code) {
                                                            case 84:
                                                                data.enforceInterface(DESCRIPTOR);
                                                                commitChanges();
                                                                reply.writeNoException();
                                                                return true;
                                                            case 85:
                                                                data.enforceInterface(DESCRIPTOR);
                                                                boolean supportsCheckpoint = supportsCheckpoint();
                                                                reply.writeNoException();
                                                                reply.writeInt(supportsCheckpoint ? 1 : 0);
                                                                return true;
                                                            case 86:
                                                                data.enforceInterface(DESCRIPTOR);
                                                                startCheckpoint(data.readInt());
                                                                reply.writeNoException();
                                                                return true;
                                                            case 87:
                                                                data.enforceInterface(DESCRIPTOR);
                                                                boolean needsCheckpoint = needsCheckpoint();
                                                                reply.writeNoException();
                                                                reply.writeInt(needsCheckpoint ? 1 : 0);
                                                                return true;
                                                            case 88:
                                                                data.enforceInterface(DESCRIPTOR);
                                                                String _arg05 = data.readString();
                                                                if (data.readInt() != 0) {
                                                                    _arg1 = true;
                                                                }
                                                                abortChanges(_arg05, _arg1);
                                                                reply.writeNoException();
                                                                return true;
                                                            default:
                                                                switch (code) {
                                                                    case 501:
                                                                        data.enforceInterface(DESCRIPTOR);
                                                                        unlockUserKeyISec(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray());
                                                                        reply.writeNoException();
                                                                        return true;
                                                                    case 502:
                                                                        data.enforceInterface(DESCRIPTOR);
                                                                        lockUserKeyISec(data.readInt());
                                                                        reply.writeNoException();
                                                                        return true;
                                                                    case 503:
                                                                        data.enforceInterface(DESCRIPTOR);
                                                                        unlockUserScreenISec(data.readInt(), data.readInt(), data.createByteArray(), data.createByteArray(), data.readInt());
                                                                        reply.writeNoException();
                                                                        return true;
                                                                    case 504:
                                                                        data.enforceInterface(DESCRIPTOR);
                                                                        lockUserScreenISec(data.readInt(), data.readInt());
                                                                        reply.writeNoException();
                                                                        return true;
                                                                    case 505:
                                                                        data.enforceInterface(DESCRIPTOR);
                                                                        int _result29 = getPreLoadPolicyFlag(data.readInt(), data.readInt());
                                                                        reply.writeNoException();
                                                                        reply.writeInt(_result29);
                                                                        return true;
                                                                    case 506:
                                                                        data.enforceInterface(DESCRIPTOR);
                                                                        boolean screenStateFlag = setScreenStateFlag(data.readInt(), data.readInt(), data.readInt());
                                                                        reply.writeNoException();
                                                                        reply.writeInt(screenStateFlag ? 1 : 0);
                                                                        return true;
                                                                    case 507:
                                                                        data.enforceInterface(DESCRIPTOR);
                                                                        String _result30 = getKeyDesc(data.readInt(), data.readInt(), data.readInt());
                                                                        reply.writeNoException();
                                                                        reply.writeString(_result30);
                                                                        return true;
                                                                    default:
                                                                        return super.onTransact(code, data, reply, flags);
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
            } else {
                data.enforceInterface(DESCRIPTOR);
                runMaintenance();
                reply.writeNoException();
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IStorageManager {
            public static IStorageManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.os.storage.IStorageManager
            public void registerListener(IStorageEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void unregisterListener(IStorageEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void shutdown(IStorageShutdownObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().shutdown(observer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void mountObb(String rawPath, String canonicalPath, String key, IObbActionListener token, int nonce, ObbInfo obbInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(rawPath);
                        try {
                            _data.writeString(canonicalPath);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(key);
                            _data.writeStrongBinder(token != null ? token.asBinder() : null);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(nonce);
                            if (obbInfo != null) {
                                _data.writeInt(1);
                                obbInfo.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            try {
                                if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                    _reply.readException();
                                    _reply.recycle();
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().mountObb(rawPath, canonicalPath, key, token, nonce, obbInfo);
                                _reply.recycle();
                                _data.recycle();
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.os.storage.IStorageManager
            public void unmountObb(String rawPath, boolean force, IObbActionListener token, int nonce) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rawPath);
                    _data.writeInt(force ? 1 : 0);
                    _data.writeStrongBinder(token != null ? token.asBinder() : null);
                    _data.writeInt(nonce);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unmountObb(rawPath, force, token, nonce);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public boolean isObbMounted(String rawPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rawPath);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isObbMounted(rawPath);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public String getMountedObbPath(String rawPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(rawPath);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMountedObbPath(rawPath);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int decryptStorage(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().decryptStorage(password);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int encryptStorage(int type, String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(password);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().encryptStorage(type, password);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int changeEncryptionPassword(int type, String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(password);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().changeEncryptionPassword(type, password);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public StorageVolume[] getVolumeList(int uid, String packageName, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVolumeList(uid, packageName, flags);
                    }
                    _reply.readException();
                    StorageVolume[] _result = (StorageVolume[]) _reply.createTypedArray(StorageVolume.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int getEncryptionState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEncryptionState();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int verifyEncryptionPassword(String password) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(password);
                    if (!this.mRemote.transact(33, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().verifyEncryptionPassword(password);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void mkdirs(String callingPkg, String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    _data.writeString(path);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().mkdirs(callingPkg, path);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int getPasswordType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(36, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPasswordType();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public String getPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPassword();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void clearPassword() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(38, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().clearPassword();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void setField(String field, String contents) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(field);
                    _data.writeString(contents);
                    if (this.mRemote.transact(39, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setField(field, contents);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public String getField(String field) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(field);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getField(field);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public long lastMaintenance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().lastMaintenance();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void runMaintenance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().runMaintenance();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public DiskInfo[] getDisks() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisks();
                    }
                    _reply.readException();
                    DiskInfo[] _result = (DiskInfo[]) _reply.createTypedArray(DiskInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public VolumeInfo[] getVolumes(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVolumes(flags);
                    }
                    _reply.readException();
                    VolumeInfo[] _result = (VolumeInfo[]) _reply.createTypedArray(VolumeInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public VolumeRecord[] getVolumeRecords(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (!this.mRemote.transact(47, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVolumeRecords(flags);
                    }
                    _reply.readException();
                    VolumeRecord[] _result = (VolumeRecord[]) _reply.createTypedArray(VolumeRecord.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void mount(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().mount(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void unmount(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unmount(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void format(String volId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().format(volId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void partitionPublic(String diskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().partitionPublic(diskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void partitionPrivate(String diskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    if (this.mRemote.transact(52, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().partitionPrivate(diskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void partitionMixed(String diskId, int ratio) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(diskId);
                    _data.writeInt(ratio);
                    if (this.mRemote.transact(53, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().partitionMixed(diskId, ratio);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void setVolumeNickname(String fsUuid, String nickname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fsUuid);
                    _data.writeString(nickname);
                    if (this.mRemote.transact(54, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVolumeNickname(fsUuid, nickname);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void setVolumeUserFlags(String fsUuid, int flags, int mask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fsUuid);
                    _data.writeInt(flags);
                    _data.writeInt(mask);
                    if (this.mRemote.transact(55, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVolumeUserFlags(fsUuid, flags, mask);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void forgetVolume(String fsUuid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fsUuid);
                    if (this.mRemote.transact(56, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forgetVolume(fsUuid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void forgetAllVolumes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(57, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forgetAllVolumes();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public String getPrimaryStorageUuid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(58, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrimaryStorageUuid();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void setPrimaryStorageUuid(String volumeUuid, IPackageMoveObserver callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(59, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPrimaryStorageUuid(volumeUuid, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void benchmark(String volId, IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(60, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().benchmark(volId, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void setDebugFlags(int flags, int mask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeInt(mask);
                    if (this.mRemote.transact(61, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDebugFlags(flags, mask);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void createUserKey(int userId, int serialNumber, boolean ephemeral) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeInt(ephemeral ? 1 : 0);
                    if (this.mRemote.transact(62, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createUserKey(userId, serialNumber, ephemeral);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void destroyUserKey(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(63, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyUserKey(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void unlockUserKey(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    if (this.mRemote.transact(64, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unlockUserKey(userId, serialNumber, token, secret);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void lockUserKey(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(65, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().lockUserKey(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public boolean isUserKeyUnlocked(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(66, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isUserKeyUnlocked(userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void prepareUserStorage(String volumeUuid, int userId, int serialNumber, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(67, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().prepareUserStorage(volumeUuid, userId, serialNumber, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void destroyUserStorage(String volumeUuid, int userId, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(userId);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(68, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyUserStorage(volumeUuid, userId, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public boolean isConvertibleToFBE() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(69, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isConvertibleToFBE();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void addUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    if (this.mRemote.transact(71, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addUserKeyAuth(userId, serialNumber, token, secret);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void fixateNewestUserKeyAuth(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(72, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fixateNewestUserKeyAuth(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void fstrim(int flags, IVoldTaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(73, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fstrim(flags, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public AppFuseMount mountProxyFileDescriptorBridge() throws RemoteException {
                AppFuseMount _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(74, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().mountProxyFileDescriptorBridge();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AppFuseMount.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public ParcelFileDescriptor openProxyFileDescriptor(int mountPointId, int fileId, int mode) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mountPointId);
                    _data.writeInt(fileId);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(75, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openProxyFileDescriptor(mountPointId, fileId, mode);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public long getCacheQuotaBytes(String volumeUuid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(76, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCacheQuotaBytes(volumeUuid, uid);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public long getCacheSizeBytes(String volumeUuid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(77, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCacheSizeBytes(volumeUuid, uid);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public long getAllocatableBytes(String volumeUuid, int flags, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeInt(flags);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(78, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllocatableBytes(volumeUuid, flags, callingPackage);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void allocateBytes(String volumeUuid, long bytes, int flags, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(volumeUuid);
                    _data.writeLong(bytes);
                    _data.writeInt(flags);
                    _data.writeString(callingPackage);
                    if (this.mRemote.transact(79, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().allocateBytes(volumeUuid, bytes, flags, callingPackage);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void runIdleMaintenance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(80, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().runIdleMaintenance();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void abortIdleMaintenance() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(81, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().abortIdleMaintenance();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public boolean isSecure() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1001, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSecure();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int startClean() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1002, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startClean();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int stopClean() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1003, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopClean();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int getNotificationLevel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1004, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNotificationLevel();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int getUndiscardInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1005, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUndiscardInfo();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int getMaxTimeCost() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1006, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxTimeCost();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int getMinTimeCost() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1007, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMinTimeCost();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int getPercentComplete() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1008, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPercentComplete();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void unlockUserKeyISec(int userId, int serialNumber, byte[] token, byte[] secret) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    if (this.mRemote.transact(501, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unlockUserKeyISec(userId, serialNumber, token, secret);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void lockUserKeyISec(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(502, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().lockUserKeyISec(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void unlockUserScreenISec(int userId, int serialNumber, byte[] token, byte[] secret, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeByteArray(token);
                    _data.writeByteArray(secret);
                    _data.writeInt(type);
                    if (this.mRemote.transact(503, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unlockUserScreenISec(userId, serialNumber, token, secret, type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void lockUserScreenISec(int userId, int serialNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    if (this.mRemote.transact(504, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().lockUserScreenISec(userId, serialNumber);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public int getPreLoadPolicyFlag(int userId, int serialNumber) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    if (!this.mRemote.transact(505, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPreLoadPolicyFlag(userId, serialNumber);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public boolean setScreenStateFlag(int userId, int serialNumber, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeInt(flag);
                    boolean _result = false;
                    if (!this.mRemote.transact(506, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setScreenStateFlag(userId, serialNumber, flag);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public String getKeyDesc(int userId, int serialNumber, int sdpClass) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeInt(sdpClass);
                    if (!this.mRemote.transact(507, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getKeyDesc(userId, serialNumber, sdpClass);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void createUserKeyISec(int userId, int serialNumber, boolean ephemeral) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(serialNumber);
                    _data.writeInt(ephemeral ? 1 : 0);
                    if (this.mRemote.transact(601, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createUserKeyISec(userId, serialNumber, ephemeral);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void destroyUserKeyISec(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(602, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().destroyUserKeyISec(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void commitChanges() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(84, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().commitChanges();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public boolean supportsCheckpoint() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(85, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportsCheckpoint();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void startCheckpoint(int numTries) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(numTries);
                    if (this.mRemote.transact(86, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startCheckpoint(numTries);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public boolean needsCheckpoint() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(87, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().needsCheckpoint();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public void abortChanges(String message, boolean retry) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    _data.writeInt(retry ? 1 : 0);
                    if (this.mRemote.transact(88, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().abortChanges(message, retry);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.storage.IStorageManager
            public IBinder getHwInnerService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(701, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwInnerService();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IStorageManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IStorageManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
