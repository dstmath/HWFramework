package com.android.server.backup;

import android.app.IBackupAgent;
import android.app.backup.IBackupManager;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.ISelectBackupTransportCallback;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface BackupManagerServiceInterface {
    void acknowledgeAdbBackupOrRestore(int i, boolean z, String str, String str2, IFullBackupRestoreObserver iFullBackupRestoreObserver);

    void adbBackup(ParcelFileDescriptor parcelFileDescriptor, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8, String[] strArr);

    void adbRestore(ParcelFileDescriptor parcelFileDescriptor);

    void agentConnected(String str, IBinder iBinder);

    void agentDisconnected(String str);

    void backupNow();

    boolean beginFullBackup(FullBackupJob fullBackupJob);

    IRestoreSession beginRestoreSession(String str, String str2);

    IBackupAgent bindToAgentSynchronous(ApplicationInfo applicationInfo, int i);

    void cancelBackups();

    void clearBackupData(String str, String str2);

    void dataChanged(String str);

    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    void endFullBackup();

    String[] filterAppsEligibleForBackup(String[] strArr);

    void fullTransportBackup(String[] strArr);

    int generateRandomIntegerToken();

    BackupAgentTimeoutParameters getAgentTimeoutParameters();

    long getAvailableRestoreToken(String str);

    IBackupManager getBackupManagerBinder();

    Intent getConfigurationIntent(String str);

    String getCurrentTransport();

    Intent getDataManagementIntent(String str);

    String getDataManagementLabel(String str);

    String getDestinationString(String str);

    String[] getTransportWhitelist();

    boolean hasBackupPassword();

    void initializeTransports(String[] strArr, IBackupObserver iBackupObserver);

    boolean isAppEligibleForBackup(String str);

    boolean isBackupEnabled();

    ComponentName[] listAllTransportComponents();

    String[] listAllTransports();

    void opComplete(int i, long j);

    void prepareOperationTimeout(int i, long j, BackupRestoreTask backupRestoreTask, int i2);

    int requestBackup(String[] strArr, IBackupObserver iBackupObserver, int i);

    int requestBackup(String[] strArr, IBackupObserver iBackupObserver, IBackupManagerMonitor iBackupManagerMonitor, int i);

    void restoreAtInstall(String str, int i);

    String selectBackupTransport(String str);

    void selectBackupTransportAsync(ComponentName componentName, ISelectBackupTransportCallback iSelectBackupTransportCallback);

    void setAutoRestore(boolean z);

    void setBackupEnabled(boolean z);

    boolean setBackupPassword(String str, String str2);

    void setBackupProvisioned(boolean z);

    void tearDownAgentAndKill(ApplicationInfo applicationInfo);

    void unlockSystemUser();

    void updateTransportAttributes(ComponentName componentName, String str, Intent intent, String str2, Intent intent2, String str3);

    boolean waitUntilOperationComplete(int i);
}
