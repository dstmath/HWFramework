package com.android.server.backup;

import android.app.backup.IBackupManager.Stub;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.ISelectBackupTransportCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.internal.util.DumpUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;

public class Trampoline extends Stub {
    static final String BACKUP_DISABLE_PROPERTY = "ro.backup.disable";
    static final String BACKUP_SUPPRESS_FILENAME = "backup-suppress";
    static final boolean DEBUG_TRAMPOLINE = false;
    static final String TAG = "BackupManagerService";
    final Context mContext;
    final boolean mGlobalDisable = SystemProperties.getBoolean(BACKUP_DISABLE_PROPERTY, false);
    volatile BackupManagerService mService;
    final File mSuppressFile;

    public Trampoline(Context context) {
        this.mContext = context;
        File dir = new File(Environment.getDataDirectory(), "backup");
        dir.mkdirs();
        this.mSuppressFile = new File(dir, BACKUP_SUPPRESS_FILENAME);
    }

    public void initialize(int whichUser) {
        if (whichUser == 0) {
            if (this.mGlobalDisable) {
                Slog.i(TAG, "Backup/restore not supported");
                return;
            }
            synchronized (this) {
                if (this.mSuppressFile.exists()) {
                    Slog.i(TAG, "Backup inactive in user " + whichUser);
                } else {
                    this.mService = new BackupManagerService(this.mContext, this);
                }
            }
        }
    }

    public void setBackupServiceActive(int userHandle, boolean makeActive) {
        int caller = Binder.getCallingUid();
        if (caller != 1000 && caller != 0) {
            throw new SecurityException("No permission to configure backup activity");
        } else if (this.mGlobalDisable) {
            Slog.i(TAG, "Backup/restore not supported");
            return;
        } else {
            if (userHandle == 0) {
                synchronized (this) {
                    if (makeActive != isBackupServiceActive(userHandle)) {
                        Slog.i(TAG, "Making backup " + (makeActive ? "" : "in") + "active in user " + userHandle);
                        if (makeActive) {
                            this.mService = new BackupManagerService(this.mContext, this);
                            this.mSuppressFile.delete();
                        } else {
                            this.mService = null;
                            try {
                                this.mSuppressFile.createNewFile();
                            } catch (IOException e) {
                                Slog.e(TAG, "Unable to persist backup service inactivity");
                            }
                        }
                    }
                }
            }
            return;
        }
    }

    public boolean isBackupServiceActive(int userHandle) {
        boolean z = false;
        if (userHandle != 0) {
            return false;
        }
        synchronized (this) {
            if (this.mService != null) {
                z = true;
            }
        }
        return z;
    }

    public void dataChanged(String packageName) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.dataChanged(packageName);
        }
    }

    public void clearBackupData(String transportName, String packageName) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.clearBackupData(transportName, packageName);
        }
    }

    public void agentConnected(String packageName, IBinder agent) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.agentConnected(packageName, agent);
        }
    }

    public void agentDisconnected(String packageName) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.agentDisconnected(packageName);
        }
    }

    public void restoreAtInstall(String packageName, int token) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.restoreAtInstall(packageName, token);
        }
    }

    public void setBackupEnabled(boolean isEnabled) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.setBackupEnabled(isEnabled);
        }
    }

    public void setAutoRestore(boolean doAutoRestore) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.setAutoRestore(doAutoRestore);
        }
    }

    public void setBackupProvisioned(boolean isProvisioned) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.setBackupProvisioned(isProvisioned);
        }
    }

    public boolean isBackupEnabled() throws RemoteException {
        BackupManagerService svc = this.mService;
        return svc != null ? svc.isBackupEnabled() : false;
    }

    public boolean setBackupPassword(String currentPw, String newPw) throws RemoteException {
        BackupManagerService svc = this.mService;
        return svc != null ? svc.setBackupPassword(currentPw, newPw) : false;
    }

    public boolean hasBackupPassword() throws RemoteException {
        BackupManagerService svc = this.mService;
        return svc != null ? svc.hasBackupPassword() : false;
    }

    public void backupNow() throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.backupNow();
        }
    }

    public void adbBackup(ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean allApps, boolean allIncludesSystem, boolean doCompress, boolean doKeyValue, String[] packageNames) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.adbBackup(fd, includeApks, includeObbs, includeShared, doWidgets, allApps, allIncludesSystem, doCompress, doKeyValue, packageNames);
        }
    }

    public void fullTransportBackup(String[] packageNames) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.fullTransportBackup(packageNames);
        }
    }

    public void adbRestore(ParcelFileDescriptor fd) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.adbRestore(fd);
        }
    }

    public void acknowledgeFullBackupOrRestore(int token, boolean allow, String curPassword, String encryptionPassword, IFullBackupRestoreObserver observer) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.acknowledgeAdbBackupOrRestore(token, allow, curPassword, encryptionPassword, observer);
        }
    }

    public String getCurrentTransport() throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.getCurrentTransport();
        }
        return null;
    }

    public String[] listAllTransports() throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.listAllTransports();
        }
        return null;
    }

    public ComponentName[] listAllTransportComponents() throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.listAllTransportComponents();
        }
        return null;
    }

    public String[] getTransportWhitelist() {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.getTransportWhitelist();
        }
        return null;
    }

    public String selectBackupTransport(String transport) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.selectBackupTransport(transport);
        }
        return null;
    }

    public void selectBackupTransportAsync(ComponentName transport, ISelectBackupTransportCallback listener) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.selectBackupTransportAsync(transport, listener);
        } else if (listener != null) {
            try {
                listener.onFailure(-2001);
            } catch (RemoteException e) {
            }
        }
    }

    public Intent getConfigurationIntent(String transport) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.getConfigurationIntent(transport);
        }
        return null;
    }

    public String getDestinationString(String transport) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.getDestinationString(transport);
        }
        return null;
    }

    public Intent getDataManagementIntent(String transport) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.getDataManagementIntent(transport);
        }
        return null;
    }

    public String getDataManagementLabel(String transport) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.getDataManagementLabel(transport);
        }
        return null;
    }

    public IRestoreSession beginRestoreSession(String packageName, String transportID) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            return svc.beginRestoreSession(packageName, transportID);
        }
        return null;
    }

    public void opComplete(int token, long result) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.opComplete(token, result);
        }
    }

    public long getAvailableRestoreToken(String packageName) {
        BackupManagerService svc = this.mService;
        return svc != null ? svc.getAvailableRestoreToken(packageName) : 0;
    }

    public boolean isAppEligibleForBackup(String packageName) {
        BackupManagerService svc = this.mService;
        return svc != null ? svc.isAppEligibleForBackup(packageName) : false;
    }

    public int requestBackup(String[] packages, IBackupObserver observer, IBackupManagerMonitor monitor, int flags) throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc == null) {
            return -2001;
        }
        return svc.requestBackup(packages, observer, monitor, flags);
    }

    public void cancelBackups() throws RemoteException {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.cancelBackups();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            BackupManagerService svc = this.mService;
            if (svc != null) {
                svc.dump(fd, pw, args);
            } else {
                pw.println("Inactive");
            }
        }
    }

    boolean beginFullBackup(FullBackupJob scheduledJob) {
        BackupManagerService svc = this.mService;
        return svc != null ? svc.beginFullBackup(scheduledJob) : false;
    }

    void endFullBackup() {
        BackupManagerService svc = this.mService;
        if (svc != null) {
            svc.endFullBackup();
        }
    }
}
