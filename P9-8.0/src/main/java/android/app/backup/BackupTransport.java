package android.app.backup;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import com.android.internal.backup.IBackupTransport;
import com.android.internal.backup.IBackupTransport.Stub;

public class BackupTransport {
    public static final int AGENT_ERROR = -1003;
    public static final int AGENT_UNKNOWN = -1004;
    public static final int FLAG_USER_INITIATED = 1;
    public static final int NO_MORE_DATA = -1;
    public static final int TRANSPORT_ERROR = -1000;
    public static final int TRANSPORT_NOT_INITIALIZED = -1001;
    public static final int TRANSPORT_OK = 0;
    public static final int TRANSPORT_PACKAGE_REJECTED = -1002;
    public static final int TRANSPORT_QUOTA_EXCEEDED = -1005;
    IBackupTransport mBinderImpl = new TransportImpl();

    class TransportImpl extends Stub {
        TransportImpl() {
        }

        public String name() throws RemoteException {
            return BackupTransport.this.name();
        }

        public Intent configurationIntent() throws RemoteException {
            return BackupTransport.this.configurationIntent();
        }

        public String currentDestinationString() throws RemoteException {
            return BackupTransport.this.currentDestinationString();
        }

        public Intent dataManagementIntent() {
            return BackupTransport.this.dataManagementIntent();
        }

        public String dataManagementLabel() {
            return BackupTransport.this.dataManagementLabel();
        }

        public String transportDirName() throws RemoteException {
            return BackupTransport.this.transportDirName();
        }

        public long requestBackupTime() throws RemoteException {
            return BackupTransport.this.requestBackupTime();
        }

        public int initializeDevice() throws RemoteException {
            return BackupTransport.this.initializeDevice();
        }

        public int performBackup(PackageInfo packageInfo, ParcelFileDescriptor inFd, int flags) throws RemoteException {
            return BackupTransport.this.performBackup(packageInfo, inFd, flags);
        }

        public int clearBackupData(PackageInfo packageInfo) throws RemoteException {
            return BackupTransport.this.clearBackupData(packageInfo);
        }

        public int finishBackup() throws RemoteException {
            return BackupTransport.this.finishBackup();
        }

        public RestoreSet[] getAvailableRestoreSets() throws RemoteException {
            return BackupTransport.this.getAvailableRestoreSets();
        }

        public long getCurrentRestoreSet() throws RemoteException {
            return BackupTransport.this.getCurrentRestoreSet();
        }

        public int startRestore(long token, PackageInfo[] packages) throws RemoteException {
            return BackupTransport.this.startRestore(token, packages);
        }

        public RestoreDescription nextRestorePackage() throws RemoteException {
            return BackupTransport.this.nextRestorePackage();
        }

        public int getRestoreData(ParcelFileDescriptor outFd) throws RemoteException {
            return BackupTransport.this.getRestoreData(outFd);
        }

        public void finishRestore() throws RemoteException {
            BackupTransport.this.finishRestore();
        }

        public long requestFullBackupTime() throws RemoteException {
            return BackupTransport.this.requestFullBackupTime();
        }

        public int performFullBackup(PackageInfo targetPackage, ParcelFileDescriptor socket, int flags) throws RemoteException {
            return BackupTransport.this.performFullBackup(targetPackage, socket, flags);
        }

        public int checkFullBackupSize(long size) {
            return BackupTransport.this.checkFullBackupSize(size);
        }

        public int sendBackupData(int numBytes) throws RemoteException {
            return BackupTransport.this.sendBackupData(numBytes);
        }

        public void cancelFullBackup() throws RemoteException {
            BackupTransport.this.cancelFullBackup();
        }

        public boolean isAppEligibleForBackup(PackageInfo targetPackage, boolean isFullBackup) throws RemoteException {
            return BackupTransport.this.isAppEligibleForBackup(targetPackage, isFullBackup);
        }

        public long getBackupQuota(String packageName, boolean isFullBackup) {
            return BackupTransport.this.getBackupQuota(packageName, isFullBackup);
        }

        public int getNextFullRestoreDataChunk(ParcelFileDescriptor socket) {
            return BackupTransport.this.getNextFullRestoreDataChunk(socket);
        }

        public int abortFullRestore() {
            return BackupTransport.this.abortFullRestore();
        }
    }

    public IBinder getBinder() {
        return this.mBinderImpl.asBinder();
    }

    public String name() {
        throw new UnsupportedOperationException("Transport name() not implemented");
    }

    public Intent configurationIntent() {
        return null;
    }

    public String currentDestinationString() {
        throw new UnsupportedOperationException("Transport currentDestinationString() not implemented");
    }

    public Intent dataManagementIntent() {
        return null;
    }

    public String dataManagementLabel() {
        throw new UnsupportedOperationException("Transport dataManagementLabel() not implemented");
    }

    public String transportDirName() {
        throw new UnsupportedOperationException("Transport transportDirName() not implemented");
    }

    public int initializeDevice() {
        return -1000;
    }

    public int clearBackupData(PackageInfo packageInfo) {
        return -1000;
    }

    public int finishBackup() {
        return -1000;
    }

    public long requestBackupTime() {
        return 0;
    }

    public int performBackup(PackageInfo packageInfo, ParcelFileDescriptor inFd, int flags) {
        return performBackup(packageInfo, inFd);
    }

    public int performBackup(PackageInfo packageInfo, ParcelFileDescriptor inFd) {
        return -1000;
    }

    public RestoreSet[] getAvailableRestoreSets() {
        return null;
    }

    public long getCurrentRestoreSet() {
        return 0;
    }

    public int startRestore(long token, PackageInfo[] packages) {
        return -1000;
    }

    public RestoreDescription nextRestorePackage() {
        return null;
    }

    public int getRestoreData(ParcelFileDescriptor outFd) {
        return -1000;
    }

    public void finishRestore() {
        throw new UnsupportedOperationException("Transport finishRestore() not implemented");
    }

    public long requestFullBackupTime() {
        return 0;
    }

    public int performFullBackup(PackageInfo targetPackage, ParcelFileDescriptor socket, int flags) {
        return performFullBackup(targetPackage, socket);
    }

    public int performFullBackup(PackageInfo targetPackage, ParcelFileDescriptor socket) {
        return -1002;
    }

    public int checkFullBackupSize(long size) {
        return 0;
    }

    public int sendBackupData(int numBytes) {
        return -1000;
    }

    public void cancelFullBackup() {
        throw new UnsupportedOperationException("Transport cancelFullBackup() not implemented");
    }

    public boolean isAppEligibleForBackup(PackageInfo targetPackage, boolean isFullBackup) {
        return true;
    }

    public long getBackupQuota(String packageName, boolean isFullBackup) {
        return Long.MAX_VALUE;
    }

    public int getNextFullRestoreDataChunk(ParcelFileDescriptor socket) {
        return 0;
    }

    public int abortFullRestore() {
        return 0;
    }
}
