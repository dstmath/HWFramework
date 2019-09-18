package android.app.backup;

import android.Manifest;
import android.annotation.SystemApi;
import android.app.backup.IBackupManager;
import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import android.app.backup.ISelectBackupTransportCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.util.Pair;

public class BackupManager {
    @SystemApi
    public static final int ERROR_AGENT_FAILURE = -1003;
    @SystemApi
    public static final int ERROR_BACKUP_CANCELLED = -2003;
    @SystemApi
    public static final int ERROR_BACKUP_NOT_ALLOWED = -2001;
    @SystemApi
    public static final int ERROR_PACKAGE_NOT_FOUND = -2002;
    @SystemApi
    public static final int ERROR_TRANSPORT_ABORTED = -1000;
    @SystemApi
    public static final int ERROR_TRANSPORT_INVALID = -2;
    @SystemApi
    public static final int ERROR_TRANSPORT_PACKAGE_REJECTED = -1002;
    @SystemApi
    public static final int ERROR_TRANSPORT_QUOTA_EXCEEDED = -1005;
    @SystemApi
    public static final int ERROR_TRANSPORT_UNAVAILABLE = -1;
    public static final String EXTRA_BACKUP_SERVICES_AVAILABLE = "backup_services_available";
    @SystemApi
    public static final int FLAG_NON_INCREMENTAL_BACKUP = 1;
    @SystemApi
    public static final String PACKAGE_MANAGER_SENTINEL = "@pm@";
    @SystemApi
    public static final int SUCCESS = 0;
    private static final String TAG = "BackupManager";
    private static IBackupManager sService;
    private Context mContext;

    private class BackupManagerMonitorWrapper extends IBackupManagerMonitor.Stub {
        final BackupManagerMonitor mMonitor;

        BackupManagerMonitorWrapper(BackupManagerMonitor monitor) {
            this.mMonitor = monitor;
        }

        public void onEvent(Bundle event) throws RemoteException {
            this.mMonitor.onEvent(event);
        }
    }

    private class BackupObserverWrapper extends IBackupObserver.Stub {
        static final int MSG_FINISHED = 3;
        static final int MSG_RESULT = 2;
        static final int MSG_UPDATE = 1;
        final Handler mHandler;
        final BackupObserver mObserver;

        BackupObserverWrapper(Context context, BackupObserver observer) {
            this.mHandler = new Handler(context.getMainLooper(), BackupManager.this) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            Pair<String, BackupProgress> obj = (Pair) msg.obj;
                            BackupObserverWrapper.this.mObserver.onUpdate((String) obj.first, (BackupProgress) obj.second);
                            return;
                        case 2:
                            BackupObserverWrapper.this.mObserver.onResult((String) msg.obj, msg.arg1);
                            return;
                        case 3:
                            BackupObserverWrapper.this.mObserver.backupFinished(msg.arg1);
                            return;
                        default:
                            Log.w(BackupManager.TAG, "Unknown message: " + msg);
                            return;
                    }
                }
            };
            this.mObserver = observer;
        }

        public void onUpdate(String currentPackage, BackupProgress backupProgress) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, Pair.create(currentPackage, backupProgress)));
        }

        public void onResult(String currentPackage, int status) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, status, 0, currentPackage));
        }

        public void backupFinished(int status) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3, status, 0));
        }
    }

    private class SelectTransportListenerWrapper extends ISelectBackupTransportCallback.Stub {
        private final Handler mHandler;
        /* access modifiers changed from: private */
        public final SelectBackupTransportCallback mListener;

        SelectTransportListenerWrapper(Context context, SelectBackupTransportCallback listener) {
            this.mHandler = new Handler(context.getMainLooper());
            this.mListener = listener;
        }

        public void onSuccess(final String transportName) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SelectTransportListenerWrapper.this.mListener.onSuccess(transportName);
                }
            });
        }

        public void onFailure(final int reason) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    SelectTransportListenerWrapper.this.mListener.onFailure(reason);
                }
            });
        }
    }

    private static void checkServiceBinder() {
        if (sService == null) {
            sService = IBackupManager.Stub.asInterface(ServiceManager.getService(Context.BACKUP_SERVICE));
        }
    }

    public BackupManager(Context context) {
        this.mContext = context;
    }

    public void dataChanged() {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.dataChanged(this.mContext.getPackageName());
            } catch (RemoteException e) {
                Log.d(TAG, "dataChanged() couldn't connect");
            }
        }
    }

    public static void dataChanged(String packageName) {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.dataChanged(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "dataChanged(pkg) couldn't connect");
            }
        }
    }

    @Deprecated
    public int requestRestore(RestoreObserver observer) {
        return requestRestore(observer, null);
    }

    @SystemApi
    @Deprecated
    public int requestRestore(RestoreObserver observer, BackupManagerMonitor monitor) {
        Log.w(TAG, "requestRestore(): Since Android P app can no longer request restoring of its backup.");
        return -1;
    }

    @SystemApi
    public RestoreSession beginRestoreSession() {
        checkServiceBinder();
        if (sService == null) {
            return null;
        }
        try {
            IRestoreSession binder = sService.beginRestoreSession(null, null);
            if (binder != null) {
                return new RestoreSession(this.mContext, binder);
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "beginRestoreSession() couldn't connect");
            return null;
        }
    }

    @SystemApi
    public void setBackupEnabled(boolean isEnabled) {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.setBackupEnabled(isEnabled);
            } catch (RemoteException e) {
                Log.e(TAG, "setBackupEnabled() couldn't connect");
            }
        }
    }

    @SystemApi
    public boolean isBackupEnabled() {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.isBackupEnabled();
            } catch (RemoteException e) {
                Log.e(TAG, "isBackupEnabled() couldn't connect");
            }
        }
        return false;
    }

    @SystemApi
    public boolean isBackupServiceActive(UserHandle user) {
        this.mContext.enforceCallingOrSelfPermission(Manifest.permission.BACKUP, "isBackupServiceActive");
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.isBackupServiceActive(user.getIdentifier());
            } catch (RemoteException e) {
                Log.e(TAG, "isBackupEnabled() couldn't connect");
            }
        }
        return false;
    }

    @SystemApi
    public void setAutoRestore(boolean isEnabled) {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.setAutoRestore(isEnabled);
            } catch (RemoteException e) {
                Log.e(TAG, "setAutoRestore() couldn't connect");
            }
        }
    }

    @SystemApi
    public String getCurrentTransport() {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.getCurrentTransport();
            } catch (RemoteException e) {
                Log.e(TAG, "getCurrentTransport() couldn't connect");
            }
        }
        return null;
    }

    @SystemApi
    public String[] listAllTransports() {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.listAllTransports();
            } catch (RemoteException e) {
                Log.e(TAG, "listAllTransports() couldn't connect");
            }
        }
        return null;
    }

    @SystemApi
    public void updateTransportAttributes(ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, String dataManagementLabel) {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.updateTransportAttributes(transportComponent, name, configurationIntent, currentDestinationString, dataManagementIntent, dataManagementLabel);
            } catch (RemoteException e) {
                Log.e(TAG, "describeTransport() couldn't connect");
            }
        }
    }

    @SystemApi
    @Deprecated
    public String selectBackupTransport(String transport) {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.selectBackupTransport(transport);
            } catch (RemoteException e) {
                Log.e(TAG, "selectBackupTransport() couldn't connect");
            }
        }
        return null;
    }

    @SystemApi
    public void selectBackupTransport(ComponentName transport, SelectBackupTransportCallback listener) {
        SelectTransportListenerWrapper wrapper;
        checkServiceBinder();
        if (sService != null) {
            if (listener == null) {
                wrapper = null;
            } else {
                try {
                    wrapper = new SelectTransportListenerWrapper(this.mContext, listener);
                } catch (RemoteException e) {
                    Log.e(TAG, "selectBackupTransportAsync() couldn't connect");
                    return;
                }
            }
            sService.selectBackupTransportAsync(transport, wrapper);
        }
    }

    @SystemApi
    public void backupNow() {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.backupNow();
            } catch (RemoteException e) {
                Log.e(TAG, "backupNow() couldn't connect");
            }
        }
    }

    @SystemApi
    public long getAvailableRestoreToken(String packageName) {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.getAvailableRestoreToken(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "getAvailableRestoreToken() couldn't connect");
            }
        }
        return 0;
    }

    @SystemApi
    public boolean isAppEligibleForBackup(String packageName) {
        checkServiceBinder();
        if (sService != null) {
            try {
                return sService.isAppEligibleForBackup(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "isAppEligibleForBackup(pkg) couldn't connect");
            }
        }
        return false;
    }

    @SystemApi
    public int requestBackup(String[] packages, BackupObserver observer) {
        return requestBackup(packages, observer, null, 0);
    }

    @SystemApi
    public int requestBackup(String[] packages, BackupObserver observer, BackupManagerMonitor monitor, int flags) {
        BackupObserverWrapper observerWrapper;
        checkServiceBinder();
        if (sService != null) {
            BackupManagerMonitorWrapper monitorWrapper = null;
            if (observer == null) {
                observerWrapper = null;
            } else {
                try {
                    observerWrapper = new BackupObserverWrapper(this.mContext, observer);
                } catch (RemoteException e) {
                    Log.e(TAG, "requestBackup() couldn't connect");
                }
            }
            if (monitor != null) {
                monitorWrapper = new BackupManagerMonitorWrapper(monitor);
            }
            return sService.requestBackup(packages, observerWrapper, monitorWrapper, flags);
        }
        return -1;
    }

    @SystemApi
    public void cancelBackups() {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.cancelBackups();
            } catch (RemoteException e) {
                Log.e(TAG, "cancelBackups() couldn't connect.");
            }
        }
    }

    @SystemApi
    public Intent getConfigurationIntent(String transportName) {
        if (sService != null) {
            try {
                return sService.getConfigurationIntent(transportName);
            } catch (RemoteException e) {
                Log.e(TAG, "getConfigurationIntent() couldn't connect");
            }
        }
        return null;
    }

    @SystemApi
    public String getDestinationString(String transportName) {
        if (sService != null) {
            try {
                return sService.getDestinationString(transportName);
            } catch (RemoteException e) {
                Log.e(TAG, "getDestinationString() couldn't connect");
            }
        }
        return null;
    }

    @SystemApi
    public Intent getDataManagementIntent(String transportName) {
        if (sService != null) {
            try {
                return sService.getDataManagementIntent(transportName);
            } catch (RemoteException e) {
                Log.e(TAG, "getDataManagementIntent() couldn't connect");
            }
        }
        return null;
    }

    @SystemApi
    public String getDataManagementLabel(String transportName) {
        if (sService != null) {
            try {
                return sService.getDataManagementLabel(transportName);
            } catch (RemoteException e) {
                Log.e(TAG, "getDataManagementLabel() couldn't connect");
            }
        }
        return null;
    }
}
