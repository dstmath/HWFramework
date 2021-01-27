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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.DumpUtils;
import com.android.server.backup.utils.RandomAccessFileUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;

public class Trampoline extends IBackupManager.Stub {
    private static final String BACKUP_ACTIVATED_FILENAME = "backup-activated";
    private static final String BACKUP_DISABLE_PROPERTY = "ro.backup.disable";
    private static final String BACKUP_SUPPRESS_FILENAME = "backup-suppress";
    private static final String BACKUP_THREAD = "backup";
    private static final String REMEMBER_ACTIVATED_FILENAME = "backup-remember-activated";
    private final Context mContext;
    private final boolean mGlobalDisable;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private volatile BackupManagerService mService;
    private final Object mStateLock = new Object();
    private final UserManager mUserManager;

    public Trampoline(Context context) {
        this.mContext = context;
        this.mGlobalDisable = isBackupDisabled();
        this.mHandlerThread = new HandlerThread("backup", 10);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.mUserManager = UserManager.get(context);
    }

    /* access modifiers changed from: protected */
    public boolean isBackupDisabled() {
        return SystemProperties.getBoolean(BACKUP_DISABLE_PROPERTY, false);
    }

    /* access modifiers changed from: protected */
    public int binderGetCallingUserId() {
        return Binder.getCallingUserHandle().getIdentifier();
    }

    /* access modifiers changed from: protected */
    public int binderGetCallingUid() {
        return Binder.getCallingUid();
    }

    /* access modifiers changed from: protected */
    public File getSuppressFileForSystemUser() {
        return new File(UserBackupManagerFiles.getBaseStateDir(0), BACKUP_SUPPRESS_FILENAME);
    }

    /* access modifiers changed from: protected */
    public File getRememberActivatedFileForNonSystemUser(int userId) {
        return UserBackupManagerFiles.getStateFileInSystemDir(REMEMBER_ACTIVATED_FILENAME, userId);
    }

    /* access modifiers changed from: protected */
    public File getActivatedFileForNonSystemUser(int userId) {
        return UserBackupManagerFiles.getStateFileInSystemDir(BACKUP_ACTIVATED_FILENAME, userId);
    }

    private void createFile(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            if (!file.createNewFile()) {
                Slog.w(BackupManagerService.TAG, "Failed to create file " + file.getPath());
            }
        }
    }

    private void deleteFile(File file) {
        if (file.exists() && !file.delete()) {
            Slog.w(BackupManagerService.TAG, "Failed to delete file " + file.getPath());
        }
    }

    @GuardedBy({"mStateLock"})
    private void deactivateBackupForUserLocked(int userId) throws IOException {
        if (userId == 0) {
            createFile(getSuppressFileForSystemUser());
        } else {
            deleteFile(getActivatedFileForNonSystemUser(userId));
        }
    }

    @GuardedBy({"mStateLock"})
    private void activateBackupForUserLocked(int userId) throws IOException {
        if (userId == 0) {
            deleteFile(getSuppressFileForSystemUser());
        } else {
            createFile(getActivatedFileForNonSystemUser(userId));
        }
    }

    private boolean isUserReadyForBackup(int userId) {
        return (this.mService == null || this.mService.getServiceUsers().get(userId) == null || !isBackupActivatedForUser(userId)) ? false : true;
    }

    private boolean isBackupActivatedForUser(int userId) {
        if (getSuppressFileForSystemUser().exists()) {
            return false;
        }
        if (userId == 0 || getActivatedFileForNonSystemUser(userId).exists()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mContext;
    }

    /* access modifiers changed from: protected */
    public UserManager getUserManager() {
        return this.mUserManager;
    }

    /* access modifiers changed from: protected */
    public BackupManagerService createBackupManagerService() {
        return new BackupManagerService(this.mContext, this, this.mHandlerThread);
    }

    /* access modifiers changed from: protected */
    public void postToHandler(Runnable runnable) {
        this.mHandler.post(runnable);
    }

    /* access modifiers changed from: package-private */
    public void initializeService() {
        postToHandler(new Runnable() {
            /* class com.android.server.backup.$$Lambda$Trampoline$RB5ywBeynPqhAQ6VHW4qi9U4c84 */

            @Override // java.lang.Runnable
            public final void run() {
                Trampoline.this.lambda$initializeService$0$Trampoline();
            }
        });
    }

    public /* synthetic */ void lambda$initializeService$0$Trampoline() {
        Trace.traceBegin(64, "backup init");
        if (this.mGlobalDisable) {
            Slog.i(BackupManagerService.TAG, "Backup service not supported");
            return;
        }
        synchronized (this.mStateLock) {
            if (this.mService == null) {
                this.mService = createBackupManagerService();
            }
        }
        Trace.traceEnd(64);
    }

    /* access modifiers changed from: package-private */
    public void unlockUser(int userId) {
        postToHandler(new Runnable(userId) {
            /* class com.android.server.backup.$$Lambda$Trampoline$_vkvTSe7bkLYa64kIjWjqjTHe7E */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Trampoline.this.lambda$unlockUser$1$Trampoline(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: startServiceForUser */
    public void lambda$unlockUser$1$Trampoline(int userId) {
        if (this.mService != null && isBackupActivatedForUser(userId)) {
            Slog.i(BackupManagerService.TAG, "Starting service for user: " + userId);
            this.mService.startServiceForUser(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopUser(int userId) {
        postToHandler(new Runnable(userId) {
            /* class com.android.server.backup.$$Lambda$Trampoline$a6uexAeN8zHcMQQ9h_KZ71UQV_A */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                Trampoline.this.lambda$stopUser$2$Trampoline(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$stopUser$2$Trampoline(int userId) {
        if (this.mService != null) {
            Slog.i(BackupManagerService.TAG, "Stopping service for user: " + userId);
            this.mService.stopServiceForUser(userId);
        }
    }

    private void enforcePermissionsOnUser(int userId) throws SecurityException {
        if (userId == 0 || getUserManager().getUserInfo(userId).isManagedProfile()) {
            int caller = binderGetCallingUid();
            if (caller != 1000 && caller != 0) {
                throw new SecurityException("No permission to configure backup activity");
            }
            return;
        }
        this.mContext.enforceCallingOrSelfPermission("android.permission.BACKUP", "No permission to configure backup activity");
        this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", "No permission to configure backup activity");
    }

    public void setBackupServiceActive(int userId, boolean makeActive) {
        enforcePermissionsOnUser(userId);
        if (userId != 0) {
            try {
                File rememberFile = getRememberActivatedFileForNonSystemUser(userId);
                createFile(rememberFile);
                RandomAccessFileUtils.writeBoolean(rememberFile, makeActive);
            } catch (IOException e) {
                Slog.e(BackupManagerService.TAG, "Unable to persist backup service activity", e);
            }
        }
        if (this.mGlobalDisable) {
            Slog.i(BackupManagerService.TAG, "Backup service not supported");
            return;
        }
        synchronized (this.mStateLock) {
            StringBuilder sb = new StringBuilder();
            sb.append("Making backup ");
            sb.append(makeActive ? "" : "in");
            sb.append("active");
            Slog.i(BackupManagerService.TAG, sb.toString());
            if (makeActive) {
                if (this.mService == null) {
                    this.mService = createBackupManagerService();
                }
                try {
                    activateBackupForUserLocked(userId);
                } catch (IOException e2) {
                    Slog.e(BackupManagerService.TAG, "Unable to persist backup service activity");
                }
                if (getUserManager().isUserUnlocked(userId)) {
                    long oldId = Binder.clearCallingIdentity();
                    try {
                        lambda$unlockUser$1$Trampoline(userId);
                    } finally {
                        Binder.restoreCallingIdentity(oldId);
                    }
                }
            } else {
                try {
                    deactivateBackupForUserLocked(userId);
                } catch (IOException e3) {
                    Slog.e(BackupManagerService.TAG, "Unable to persist backup service inactivity");
                }
                stopUser(userId);
            }
        }
    }

    public boolean isBackupServiceActive(int userId) {
        boolean z;
        synchronized (this.mStateLock) {
            z = this.mService != null && isBackupActivatedForUser(userId);
        }
        return z;
    }

    public void dataChangedForUser(int userId, String packageName) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.dataChanged(userId, packageName);
        }
    }

    public void dataChanged(String packageName) throws RemoteException {
        dataChangedForUser(binderGetCallingUserId(), packageName);
    }

    public void initializeTransportsForUser(int userId, String[] transportNames, IBackupObserver observer) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.initializeTransports(userId, transportNames, observer);
        }
    }

    public void clearBackupDataForUser(int userId, String transportName, String packageName) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.clearBackupData(userId, transportName, packageName);
        }
    }

    public void clearBackupData(String transportName, String packageName) throws RemoteException {
        clearBackupDataForUser(binderGetCallingUserId(), transportName, packageName);
    }

    public void agentConnectedForUser(int userId, String packageName, IBinder agent) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.agentConnected(userId, packageName, agent);
        }
    }

    public void agentConnected(String packageName, IBinder agent) throws RemoteException {
        agentConnectedForUser(binderGetCallingUserId(), packageName, agent);
    }

    public void agentDisconnectedForUser(int userId, String packageName) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.agentDisconnected(userId, packageName);
        }
    }

    public void agentDisconnected(String packageName) throws RemoteException {
        agentDisconnectedForUser(binderGetCallingUserId(), packageName);
    }

    public void restoreAtInstallForUser(int userId, String packageName, int token) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.restoreAtInstall(userId, packageName, token);
        }
    }

    public void restoreAtInstall(String packageName, int token) throws RemoteException {
        restoreAtInstallForUser(binderGetCallingUserId(), packageName, token);
    }

    public void setBackupEnabledForUser(int userId, boolean isEnabled) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.setBackupEnabled(userId, isEnabled);
        }
    }

    public void setBackupEnabled(boolean isEnabled) throws RemoteException {
        setBackupEnabledForUser(binderGetCallingUserId(), isEnabled);
    }

    public void setAutoRestoreForUser(int userId, boolean doAutoRestore) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.setAutoRestore(userId, doAutoRestore);
        }
    }

    public void setAutoRestore(boolean doAutoRestore) throws RemoteException {
        setAutoRestoreForUser(binderGetCallingUserId(), doAutoRestore);
    }

    public boolean isBackupEnabledForUser(int userId) throws RemoteException {
        return isUserReadyForBackup(userId) && this.mService.isBackupEnabled(userId);
    }

    public boolean isBackupEnabled() throws RemoteException {
        return isBackupEnabledForUser(binderGetCallingUserId());
    }

    public boolean setBackupPassword(String currentPw, String newPw) throws RemoteException {
        return isUserReadyForBackup(binderGetCallingUserId()) && this.mService.setBackupPassword(currentPw, newPw);
    }

    public boolean hasBackupPassword() throws RemoteException {
        return isUserReadyForBackup(binderGetCallingUserId()) && this.mService.hasBackupPassword();
    }

    public void backupNowForUser(int userId) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.backupNow(userId);
        }
    }

    public void backupNow() throws RemoteException {
        backupNowForUser(binderGetCallingUserId());
    }

    public void adbBackup(int userId, ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean allApps, boolean allIncludesSystem, boolean doCompress, boolean doKeyValue, String[] packageNames) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.adbBackup(userId, fd, includeApks, includeObbs, includeShared, doWidgets, allApps, allIncludesSystem, doCompress, doKeyValue, packageNames);
        }
    }

    public void fullTransportBackupForUser(int userId, String[] packageNames) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.fullTransportBackup(userId, packageNames);
        }
    }

    public void adbRestore(int userId, ParcelFileDescriptor fd) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.adbRestore(userId, fd);
        }
    }

    public void acknowledgeFullBackupOrRestoreForUser(int userId, int token, boolean allow, String curPassword, String encryptionPassword, IFullBackupRestoreObserver observer) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.acknowledgeAdbBackupOrRestore(userId, token, allow, curPassword, encryptionPassword, observer);
        }
    }

    public void acknowledgeFullBackupOrRestore(int token, boolean allow, String curPassword, String encryptionPassword, IFullBackupRestoreObserver observer) throws RemoteException {
        acknowledgeFullBackupOrRestoreForUser(binderGetCallingUserId(), token, allow, curPassword, encryptionPassword, observer);
    }

    public String getCurrentTransportForUser(int userId) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            return this.mService.getCurrentTransport(userId);
        }
        return null;
    }

    public String getCurrentTransport() throws RemoteException {
        return getCurrentTransportForUser(binderGetCallingUserId());
    }

    public ComponentName getCurrentTransportComponentForUser(int userId) {
        if (isUserReadyForBackup(userId)) {
            return this.mService.getCurrentTransportComponent(userId);
        }
        return null;
    }

    public String[] listAllTransportsForUser(int userId) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            return this.mService.listAllTransports(userId);
        }
        return null;
    }

    public String[] listAllTransports() throws RemoteException {
        return listAllTransportsForUser(binderGetCallingUserId());
    }

    public ComponentName[] listAllTransportComponentsForUser(int userId) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            return this.mService.listAllTransportComponents(userId);
        }
        return null;
    }

    public String[] getTransportWhitelist() {
        if (isUserReadyForBackup(binderGetCallingUserId())) {
            return this.mService.getTransportWhitelist();
        }
        return null;
    }

    public void updateTransportAttributesForUser(int userId, ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, CharSequence dataManagementLabel) {
        if (isUserReadyForBackup(userId)) {
            this.mService.updateTransportAttributes(userId, transportComponent, name, configurationIntent, currentDestinationString, dataManagementIntent, dataManagementLabel);
        }
    }

    public String selectBackupTransportForUser(int userId, String transport) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            return this.mService.selectBackupTransport(userId, transport);
        }
        return null;
    }

    public String selectBackupTransport(String transport) throws RemoteException {
        return selectBackupTransportForUser(binderGetCallingUserId(), transport);
    }

    public void selectBackupTransportAsyncForUser(int userId, ComponentName transport, ISelectBackupTransportCallback listener) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.selectBackupTransportAsync(userId, transport, listener);
        } else if (listener != null) {
            try {
                listener.onFailure(-2001);
            } catch (RemoteException e) {
            }
        }
    }

    public Intent getConfigurationIntentForUser(int userId, String transport) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            return this.mService.getConfigurationIntent(userId, transport);
        }
        return null;
    }

    public Intent getConfigurationIntent(String transport) throws RemoteException {
        return getConfigurationIntentForUser(binderGetCallingUserId(), transport);
    }

    public String getDestinationStringForUser(int userId, String transport) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            return this.mService.getDestinationString(userId, transport);
        }
        return null;
    }

    public String getDestinationString(String transport) throws RemoteException {
        return getDestinationStringForUser(binderGetCallingUserId(), transport);
    }

    public Intent getDataManagementIntentForUser(int userId, String transport) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            return this.mService.getDataManagementIntent(userId, transport);
        }
        return null;
    }

    public Intent getDataManagementIntent(String transport) throws RemoteException {
        return getDataManagementIntentForUser(binderGetCallingUserId(), transport);
    }

    public CharSequence getDataManagementLabelForUser(int userId, String transport) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            return this.mService.getDataManagementLabel(userId, transport);
        }
        return null;
    }

    public IRestoreSession beginRestoreSessionForUser(int userId, String packageName, String transportID) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            return this.mService.beginRestoreSession(userId, packageName, transportID);
        }
        return null;
    }

    public void opCompleteForUser(int userId, int token, long result) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.opComplete(userId, token, result);
        }
    }

    public void opComplete(int token, long result) throws RemoteException {
        opCompleteForUser(binderGetCallingUserId(), token, result);
    }

    public long getAvailableRestoreTokenForUser(int userId, String packageName) {
        if (isUserReadyForBackup(userId)) {
            return this.mService.getAvailableRestoreToken(userId, packageName);
        }
        return 0;
    }

    public boolean isAppEligibleForBackupForUser(int userId, String packageName) {
        return isUserReadyForBackup(userId) && this.mService.isAppEligibleForBackup(userId, packageName);
    }

    public String[] filterAppsEligibleForBackupForUser(int userId, String[] packages) {
        if (isUserReadyForBackup(userId)) {
            return this.mService.filterAppsEligibleForBackup(userId, packages);
        }
        return null;
    }

    public int requestBackupForUser(int userId, String[] packages, IBackupObserver observer, IBackupManagerMonitor monitor, int flags) throws RemoteException {
        if (!isUserReadyForBackup(userId)) {
            return -2001;
        }
        return this.mService.requestBackup(userId, packages, observer, monitor, flags);
    }

    public int requestBackup(String[] packages, IBackupObserver observer, IBackupManagerMonitor monitor, int flags) throws RemoteException {
        return requestBackupForUser(binderGetCallingUserId(), packages, observer, monitor, flags);
    }

    public void cancelBackupsForUser(int userId) throws RemoteException {
        if (isUserReadyForBackup(userId)) {
            this.mService.cancelBackups(userId);
        }
    }

    public void cancelBackups() throws RemoteException {
        cancelBackupsForUser(binderGetCallingUserId());
    }

    public UserHandle getUserForAncestralSerialNumber(long ancestralSerialNumber) {
        if (this.mService != null) {
            return this.mService.getUserForAncestralSerialNumber(ancestralSerialNumber);
        }
        return null;
    }

    public void setAncestralSerialNumber(long ancestralSerialNumber) {
        if (this.mService != null) {
            this.mService.setAncestralSerialNumber(ancestralSerialNumber);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, BackupManagerService.TAG, pw)) {
            if (isUserReadyForBackup(binderGetCallingUserId())) {
                this.mService.dump(fd, pw, args);
            } else {
                pw.println("Inactive");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean beginFullBackup(int userId, FullBackupJob scheduledJob) {
        return isUserReadyForBackup(userId) && this.mService.beginFullBackup(userId, scheduledJob);
    }

    /* access modifiers changed from: package-private */
    public void endFullBackup(int userId) {
        if (isUserReadyForBackup(userId)) {
            this.mService.endFullBackup(userId);
        }
    }
}
