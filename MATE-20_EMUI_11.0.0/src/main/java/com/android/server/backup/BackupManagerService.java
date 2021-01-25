package com.android.server.backup;

import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.app.backup.IFullBackupRestoreObserver;
import android.app.backup.IRestoreSession;
import android.app.backup.ISelectBackupTransportCallback;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.FileUtils;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.server.BatteryService;
import com.android.server.SystemConfig;
import com.android.server.SystemService;
import com.android.server.voiceinteraction.DatabaseHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Set;

public class BackupManagerService {
    public static final boolean DEBUG = true;
    public static final boolean DEBUG_SCHEDULING = true;
    @VisibleForTesting
    static final String DUMP_RUNNING_USERS_MESSAGE = "Backup Manager is running for users:";
    public static final boolean MORE_DEBUG = false;
    public static final String TAG = "BackupManagerService";
    private static Trampoline sInstance;
    private final HandlerThread mBackupThread;
    private final Context mContext;
    private final SparseArray<UserBackupManagerService> mServiceUsers = new SparseArray<>();
    private final Trampoline mTrampoline;
    private Set<ComponentName> mTransportWhitelist;
    private final BroadcastReceiver mUserRemovedReceiver = new BroadcastReceiver() {
        /* class com.android.server.backup.BackupManagerService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int userId;
            if ("android.intent.action.USER_REMOVED".equals(intent.getAction()) && (userId = intent.getIntExtra("android.intent.extra.user_handle", -10000)) > 0) {
                BackupManagerService.this.onRemovedNonSystemUser(userId);
            }
        }
    };

    static Trampoline getInstance() {
        return sInstance;
    }

    public BackupManagerService(Context context, Trampoline trampoline, HandlerThread backupThread) {
        this.mContext = (Context) Preconditions.checkNotNull(context);
        this.mTrampoline = (Trampoline) Preconditions.checkNotNull(trampoline);
        this.mBackupThread = (HandlerThread) Preconditions.checkNotNull(backupThread);
        this.mTransportWhitelist = SystemConfig.getInstance().getBackupTransportWhitelist();
        if (this.mTransportWhitelist == null) {
            this.mTransportWhitelist = Collections.emptySet();
        }
        this.mContext.registerReceiver(this.mUserRemovedReceiver, new IntentFilter("android.intent.action.USER_REMOVED"));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onRemovedNonSystemUser(int userId) {
        Slog.i(TAG, "Removing state for non system user " + userId);
        if (!FileUtils.deleteContentsAndDir(UserBackupManagerFiles.getStateDirInSystemDir(userId))) {
            Slog.w(TAG, "Failed to delete state dir for removed user: " + userId);
        }
    }

    private void enforceCallingPermissionOnUserId(int userId, String message) {
        if (Binder.getCallingUserHandle().getIdentifier() != userId) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL", message);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void startServiceForUser(int userId) {
        if (this.mServiceUsers.get(userId) != null) {
            Slog.i(TAG, "userId " + userId + " already started, so not starting again");
            return;
        }
        startServiceForUser(userId, UserBackupManagerService.createAndInitializeService(userId, this.mContext, this.mTrampoline, this.mTransportWhitelist));
    }

    /* access modifiers changed from: package-private */
    public void startServiceForUser(int userId, UserBackupManagerService userBackupManagerService) {
        this.mServiceUsers.put(userId, userBackupManagerService);
        Trace.traceBegin(64, "backup enable");
        userBackupManagerService.initializeBackupEnableState();
        Trace.traceEnd(64);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void stopServiceForUser(int userId) {
        UserBackupManagerService userBackupManagerService = (UserBackupManagerService) this.mServiceUsers.removeReturnOld(userId);
        if (userBackupManagerService != null) {
            userBackupManagerService.tearDownService();
            KeyValueBackupJob.cancel(userId, this.mContext);
            FullBackupJob.cancel(userId, this.mContext);
        }
    }

    @VisibleForTesting
    public SparseArray<UserBackupManagerService> getServiceUsers() {
        return this.mServiceUsers;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public UserBackupManagerService getServiceForUserIfCallerHasPermission(int userId, String caller) {
        enforceCallingPermissionOnUserId(userId, caller);
        UserBackupManagerService userBackupManagerService = this.mServiceUsers.get(userId);
        if (userBackupManagerService == null) {
            Slog.w(TAG, "Called " + caller + " for unknown user: " + userId);
        }
        return userBackupManagerService;
    }

    public void dataChanged(int userId, String packageName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "dataChanged()");
        if (userBackupManagerService != null) {
            userBackupManagerService.dataChanged(packageName);
        }
    }

    public void agentConnected(int userId, String packageName, IBinder agentBinder) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "agentConnected()");
        if (userBackupManagerService != null) {
            userBackupManagerService.agentConnected(packageName, agentBinder);
        }
    }

    public void agentDisconnected(int userId, String packageName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "agentDisconnected()");
        if (userBackupManagerService != null) {
            userBackupManagerService.agentDisconnected(packageName);
        }
    }

    public void opComplete(int userId, int token, long result) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "opComplete()");
        if (userBackupManagerService != null) {
            userBackupManagerService.opComplete(token, result);
        }
    }

    public void initializeTransports(int userId, String[] transportNames, IBackupObserver observer) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "initializeTransports()");
        if (userBackupManagerService != null) {
            userBackupManagerService.initializeTransports(transportNames, observer);
        }
    }

    public void clearBackupData(int userId, String transportName, String packageName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "clearBackupData()");
        if (userBackupManagerService != null) {
            userBackupManagerService.clearBackupData(transportName, packageName);
        }
    }

    public String getCurrentTransport(int userId) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "getCurrentTransport()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.getCurrentTransport();
    }

    public ComponentName getCurrentTransportComponent(int userId) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "getCurrentTransportComponent()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.getCurrentTransportComponent();
    }

    public String[] listAllTransports(int userId) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "listAllTransports()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.listAllTransports();
    }

    public ComponentName[] listAllTransportComponents(int userId) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "listAllTransportComponents()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.listAllTransportComponents();
    }

    public String[] getTransportWhitelist() {
        String[] whitelistedTransports = new String[this.mTransportWhitelist.size()];
        int i = 0;
        for (ComponentName component : this.mTransportWhitelist) {
            whitelistedTransports[i] = component.flattenToShortString();
            i++;
        }
        return whitelistedTransports;
    }

    public void updateTransportAttributes(int userId, ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, CharSequence dataManagementLabel) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "updateTransportAttributes()");
        if (userBackupManagerService != null) {
            userBackupManagerService.updateTransportAttributes(transportComponent, name, configurationIntent, currentDestinationString, dataManagementIntent, dataManagementLabel);
        }
    }

    @Deprecated
    public String selectBackupTransport(int userId, String transportName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "selectBackupTransport()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.selectBackupTransport(transportName);
    }

    public void selectBackupTransportAsync(int userId, ComponentName transportComponent, ISelectBackupTransportCallback listener) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "selectBackupTransportAsync()");
        if (userBackupManagerService != null) {
            userBackupManagerService.selectBackupTransportAsync(transportComponent, listener);
        }
    }

    public Intent getConfigurationIntent(int userId, String transportName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "getConfigurationIntent()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.getConfigurationIntent(transportName);
    }

    public void setAncestralSerialNumber(long ancestralSerialNumber) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(Binder.getCallingUserHandle().getIdentifier(), "setAncestralSerialNumber()");
        if (userBackupManagerService != null) {
            userBackupManagerService.setAncestralSerialNumber(ancestralSerialNumber);
        }
    }

    /* JADX INFO: finally extract failed */
    public UserHandle getUserForAncestralSerialNumber(long ancestralSerialNumber) {
        int callingUserId = Binder.getCallingUserHandle().getIdentifier();
        long oldId = Binder.clearCallingIdentity();
        try {
            int[] userIds = ((UserManager) this.mContext.getSystemService(UserManager.class)).getProfileIds(callingUserId, false);
            Binder.restoreCallingIdentity(oldId);
            for (int userId : userIds) {
                UserBackupManagerService userBackupManagerService = getServiceUsers().get(userId);
                if (userBackupManagerService != null && userBackupManagerService.getAncestralSerialNumber() == ancestralSerialNumber) {
                    return UserHandle.of(userId);
                }
            }
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldId);
            throw th;
        }
    }

    public String getDestinationString(int userId, String transportName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "getDestinationString()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.getDestinationString(transportName);
    }

    public Intent getDataManagementIntent(int userId, String transportName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "getDataManagementIntent()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.getDataManagementIntent(transportName);
    }

    public CharSequence getDataManagementLabel(int userId, String transportName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "getDataManagementLabel()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.getDataManagementLabel(transportName);
    }

    public void setBackupEnabled(int userId, boolean enable) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "setBackupEnabled()");
        if (userBackupManagerService != null) {
            userBackupManagerService.setBackupEnabled(enable);
        }
    }

    public void setAutoRestore(int userId, boolean autoRestore) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "setAutoRestore()");
        if (userBackupManagerService != null) {
            userBackupManagerService.setAutoRestore(autoRestore);
        }
    }

    public boolean isBackupEnabled(int userId) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "isBackupEnabled()");
        return userBackupManagerService != null && userBackupManagerService.isBackupEnabled();
    }

    public boolean isAppEligibleForBackup(int userId, String packageName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "isAppEligibleForBackup()");
        return userBackupManagerService != null && userBackupManagerService.isAppEligibleForBackup(packageName);
    }

    public String[] filterAppsEligibleForBackup(int userId, String[] packages) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "filterAppsEligibleForBackup()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.filterAppsEligibleForBackup(packages);
    }

    public void backupNow(int userId) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "backupNow()");
        if (userBackupManagerService != null) {
            userBackupManagerService.backupNow();
        }
    }

    public int requestBackup(int userId, String[] packages, IBackupObserver observer, IBackupManagerMonitor monitor, int flags) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "requestBackup()");
        if (userBackupManagerService == null) {
            return -2001;
        }
        return userBackupManagerService.requestBackup(packages, observer, monitor, flags);
    }

    public void cancelBackups(int userId) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "cancelBackups()");
        if (userBackupManagerService != null) {
            userBackupManagerService.cancelBackups();
        }
    }

    public boolean beginFullBackup(int userId, FullBackupJob scheduledJob) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "beginFullBackup()");
        return userBackupManagerService != null && userBackupManagerService.beginFullBackup(scheduledJob);
    }

    public void endFullBackup(int userId) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "endFullBackup()");
        if (userBackupManagerService != null) {
            userBackupManagerService.endFullBackup();
        }
    }

    public void fullTransportBackup(int userId, String[] packageNames) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "fullTransportBackup()");
        if (userBackupManagerService != null) {
            userBackupManagerService.fullTransportBackup(packageNames);
        }
    }

    public void restoreAtInstall(int userId, String packageName, int token) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "restoreAtInstall()");
        if (userBackupManagerService != null) {
            userBackupManagerService.restoreAtInstall(packageName, token);
        }
    }

    public IRestoreSession beginRestoreSession(int userId, String packageName, String transportName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "beginRestoreSession()");
        if (userBackupManagerService == null) {
            return null;
        }
        return userBackupManagerService.beginRestoreSession(packageName, transportName);
    }

    public long getAvailableRestoreToken(int userId, String packageName) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "getAvailableRestoreToken()");
        if (userBackupManagerService == null) {
            return 0;
        }
        return userBackupManagerService.getAvailableRestoreToken(packageName);
    }

    public boolean setBackupPassword(String currentPassword, String newPassword) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(0, "setBackupPassword()");
        if (userBackupManagerService == null || !userBackupManagerService.setBackupPassword(currentPassword, newPassword)) {
            return false;
        }
        return true;
    }

    public boolean hasBackupPassword() {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(0, "hasBackupPassword()");
        if (userBackupManagerService == null || !userBackupManagerService.hasBackupPassword()) {
            return false;
        }
        return true;
    }

    public void adbBackup(int userId, ParcelFileDescriptor fd, boolean includeApks, boolean includeObbs, boolean includeShared, boolean doWidgets, boolean doAllApps, boolean includeSystem, boolean doCompress, boolean doKeyValue, String[] packageNames) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "adbBackup()");
        if (userBackupManagerService != null) {
            userBackupManagerService.adbBackup(fd, includeApks, includeObbs, includeShared, doWidgets, doAllApps, includeSystem, doCompress, doKeyValue, packageNames);
        }
    }

    public void adbRestore(int userId, ParcelFileDescriptor fd) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "adbRestore()");
        if (userBackupManagerService != null) {
            userBackupManagerService.adbRestore(fd);
        }
    }

    public void acknowledgeAdbBackupOrRestore(int userId, int token, boolean allow, String currentPassword, String encryptionPassword, IFullBackupRestoreObserver observer) {
        UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(userId, "acknowledgeAdbBackupOrRestore()");
        if (userBackupManagerService != null) {
            userBackupManagerService.acknowledgeAdbBackupOrRestore(token, allow, currentPassword, encryptionPassword, observer);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpAndUsageStatsPermission(this.mContext, TAG, pw)) {
            if (args != null) {
                for (String arg : args) {
                    if (DatabaseHelper.SoundModelContract.KEY_USERS.equals(arg.toLowerCase())) {
                        pw.print(DUMP_RUNNING_USERS_MESSAGE);
                        for (int i = 0; i < this.mServiceUsers.size(); i++) {
                            pw.print(" " + this.mServiceUsers.keyAt(i));
                        }
                        pw.println();
                        return;
                    }
                }
            }
            UserBackupManagerService userBackupManagerService = getServiceForUserIfCallerHasPermission(0, "dump()");
            if (userBackupManagerService != null) {
                userBackupManagerService.dump(fd, pw, args);
            }
        }
    }

    public static final class Lifecycle extends SystemService {
        public Lifecycle(Context context) {
            super(context);
            Trampoline unused = BackupManagerService.sInstance = new Trampoline(context);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.backup.BackupManagerService$Lifecycle */
        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.backup.Trampoline, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        @Override // com.android.server.SystemService
        public void onStart() {
            publishBinderService(BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD, BackupManagerService.sInstance);
        }

        @Override // com.android.server.SystemService
        public void onUnlockUser(int userId) {
            if (userId == 0) {
                BackupManagerService.sInstance.initializeService();
            }
            BackupManagerService.sInstance.unlockUser(userId);
        }

        @Override // com.android.server.SystemService
        public void onStopUser(int userId) {
            BackupManagerService.sInstance.stopUser(userId);
        }
    }
}
