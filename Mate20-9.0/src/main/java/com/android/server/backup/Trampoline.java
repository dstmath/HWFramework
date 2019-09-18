package com.android.server.backup;

import android.app.backup.IBackupManager;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Slog;
import com.android.internal.util.DumpUtils;
import com.android.server.BatteryService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;

public class Trampoline extends IBackupManager.Stub {
    static final String BACKUP_DISABLE_PROPERTY = "ro.backup.disable";
    static final String BACKUP_SUPPRESS_FILENAME = "backup-suppress";
    static final boolean DEBUG_TRAMPOLINE = false;
    static final String TAG = "BackupManagerService";
    final Context mContext;
    final boolean mGlobalDisable = isBackupDisabled();
    private HandlerThread mHandlerThread;
    volatile BackupManagerServiceInterface mService;
    final File mSuppressFile = getSuppressFile();

    public Trampoline(Context context) {
        this.mContext = context;
        this.mSuppressFile.getParentFile().mkdirs();
    }

    /* access modifiers changed from: protected */
    public boolean isBackupDisabled() {
        return SystemProperties.getBoolean(BACKUP_DISABLE_PROPERTY, false);
    }

    /* access modifiers changed from: protected */
    public int binderGetCallingUid() {
        return Binder.getCallingUid();
    }

    /* access modifiers changed from: protected */
    public File getSuppressFile() {
        return new File(new File(Environment.getDataDirectory(), BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD), BACKUP_SUPPRESS_FILENAME);
    }

    /* access modifiers changed from: protected */
    public BackupManagerServiceInterface createBackupManagerService() {
        return BackupManagerService.create(this.mContext, this, this.mHandlerThread);
    }

    public void initialize(int whichUser) {
        if (whichUser == 0) {
            if (this.mGlobalDisable) {
                Slog.i("BackupManagerService", "Backup/restore not supported");
                return;
            }
            synchronized (this) {
                if (!this.mSuppressFile.exists()) {
                    this.mService = createBackupManagerService();
                } else {
                    Slog.i("BackupManagerService", "Backup inactive in user " + whichUser);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unlockSystemUser() {
        this.mHandlerThread = new HandlerThread(BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD, 10);
        this.mHandlerThread.start();
        new Handler(this.mHandlerThread.getLooper()).post(new Runnable() {
            public final void run() {
                Trampoline.lambda$unlockSystemUser$0(Trampoline.this);
            }
        });
    }

    public static /* synthetic */ void lambda$unlockSystemUser$0(Trampoline trampoline) {
        Trace.traceBegin(64, "backup init");
        trampoline.initialize(0);
        Trace.traceEnd(64);
        BackupManagerServiceInterface svc = trampoline.mService;
        Slog.i("BackupManagerService", "Unlocking system user; mService=" + trampoline.mService);
        if (svc != null) {
            svc.unlockSystemUser();
        }
    }

    public void setBackupServiceActive(int userHandle, boolean makeActive) {
        int caller = binderGetCallingUid();
        if (caller != 1000 && caller != 0) {
            throw new SecurityException("No permission to configure backup activity");
        } else if (this.mGlobalDisable) {
            Slog.i("BackupManagerService", "Backup/restore not supported");
        } else {
            if (userHandle == 0) {
                synchronized (this) {
                    if (makeActive != isBackupServiceActive(userHandle)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Making backup ");
                        sb.append(makeActive ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : "in");
                        sb.append("active in user ");
                        sb.append(userHandle);
                        Slog.i("BackupManagerService", sb.toString());
                        if (makeActive) {
                            this.mService = createBackupManagerService();
                            this.mSuppressFile.delete();
                        } else {
                            this.mService = null;
                            try {
                                this.mSuppressFile.createNewFile();
                            } catch (IOException e) {
                                Slog.e("BackupManagerService", "Unable to persist backup service inactivity");
                            }
                        }
                    }
                }
            }
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
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.dataChanged(packageName);
        }
    }

    public void initializeTransports(String[] transportNames, IBackupObserver observer) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.initializeTransports(transportNames, observer);
        }
    }

    public void clearBackupData(String transportName, String packageName) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.clearBackupData(transportName, packageName);
        }
    }

    public void agentConnected(String packageName, IBinder agent) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.agentConnected(packageName, agent);
        }
    }

    public void agentDisconnected(String packageName) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.agentDisconnected(packageName);
        }
    }

    public void restoreAtInstall(String packageName, int token) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.restoreAtInstall(packageName, token);
        }
    }

    public void setBackupEnabled(boolean isEnabled) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.setBackupEnabled(isEnabled);
        }
    }

    public void setAutoRestore(boolean doAutoRestore) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.setAutoRestore(doAutoRestore);
        }
    }

    public void setBackupProvisioned(boolean isProvisioned) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.setBackupProvisioned(isProvisioned);
        }
    }

    public boolean isBackupEnabled() throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.isBackupEnabled();
        }
        return false;
    }

    public boolean setBackupPassword(String currentPw, String newPw) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.setBackupPassword(currentPw, newPw);
        }
        return false;
    }

    public boolean hasBackupPassword() throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.hasBackupPassword();
        }
        return false;
    }

    public void backupNow() throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.backupNow();
        }
    }

    public void adbBackup(ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean allApps, boolean allIncludesSystem, boolean doCompress, boolean doKeyValue, String[] packageNames) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.adbBackup(fd, includeApks, includeObbs, includeShared, doWidgets, allApps, allIncludesSystem, doCompress, doKeyValue, packageNames);
        }
    }

    public void fullTransportBackup(String[] packageNames) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.fullTransportBackup(packageNames);
        }
    }

    public void adbRestore(ParcelFileDescriptor fd) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.adbRestore(fd);
        }
    }

    public void acknowledgeFullBackupOrRestore(int token, boolean allow, String curPassword, String encryptionPassword, IFullBackupRestoreObserver observer) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.acknowledgeAdbBackupOrRestore(token, allow, curPassword, encryptionPassword, observer);
        }
    }

    public String getCurrentTransport() throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.getCurrentTransport();
        }
        return null;
    }

    public String[] listAllTransports() throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.listAllTransports();
        }
        return null;
    }

    public ComponentName[] listAllTransportComponents() throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.listAllTransportComponents();
        }
        return null;
    }

    public String[] getTransportWhitelist() {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.getTransportWhitelist();
        }
        return null;
    }

    public void updateTransportAttributes(ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, String dataManagementLabel) {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.updateTransportAttributes(transportComponent, name, configurationIntent, currentDestinationString, dataManagementIntent, dataManagementLabel);
        }
    }

    public String selectBackupTransport(String transport) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.selectBackupTransport(transport);
        }
        return null;
    }

    public void selectBackupTransportAsync(ComponentName transport, ISelectBackupTransportCallback listener) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
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
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.getConfigurationIntent(transport);
        }
        return null;
    }

    public String getDestinationString(String transport) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.getDestinationString(transport);
        }
        return null;
    }

    public Intent getDataManagementIntent(String transport) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.getDataManagementIntent(transport);
        }
        return null;
    }

    public String getDataManagementLabel(String transport) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.getDataManagementLabel(transport);
        }
        return null;
    }

    public IRestoreSession beginRestoreSession(String packageName, String transportID) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.beginRestoreSession(packageName, transportID);
        }
        return null;
    }

    public void opComplete(int token, long result) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.opComplete(token, result);
        }
    }

    public long getAvailableRestoreToken(String packageName) {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.getAvailableRestoreToken(packageName);
        }
        return 0;
    }

    public boolean isAppEligibleForBackup(String packageName) {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.isAppEligibleForBackup(packageName);
        }
        return false;
    }

    public String[] filterAppsEligibleForBackup(String[] packages) {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.filterAppsEligibleForBackup(packages);
        }
        return null;
    }

    public int requestBackup(String[] packages, IBackupObserver observer, IBackupManagerMonitor monitor, int flags) throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc == null) {
            return -2001;
        }
        return svc.requestBackup(packages, observer, monitor, flags);
    }

    public void cancelBackups() throws RemoteException {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.cancelBackups();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, "BackupManagerService", pw)) {
            BackupManagerServiceInterface svc = this.mService;
            if (svc != null) {
                svc.dump(fd, pw, args);
            } else {
                pw.println("Inactive");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean beginFullBackup(FullBackupJob scheduledJob) {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            return svc.beginFullBackup(scheduledJob);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void endFullBackup() {
        BackupManagerServiceInterface svc = this.mService;
        if (svc != null) {
            svc.endFullBackup();
        }
    }
}
