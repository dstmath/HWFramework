package android.app.backup;

import android.app.backup.IBackupManagerMonitor.Stub;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Pair;

public class BackupManager {
    public static final int ERROR_AGENT_FAILURE = -1003;
    public static final int ERROR_BACKUP_CANCELLED = -2003;
    public static final int ERROR_BACKUP_NOT_ALLOWED = -2001;
    public static final int ERROR_PACKAGE_NOT_FOUND = -2002;
    public static final int ERROR_TRANSPORT_ABORTED = -1000;
    public static final int ERROR_TRANSPORT_INVALID = -2;
    public static final int ERROR_TRANSPORT_PACKAGE_REJECTED = -1002;
    public static final int ERROR_TRANSPORT_QUOTA_EXCEEDED = -1005;
    public static final int ERROR_TRANSPORT_UNAVAILABLE = -1;
    public static final String EXTRA_BACKUP_SERVICES_AVAILABLE = "backup_services_available";
    public static final int FLAG_NON_INCREMENTAL_BACKUP = 1;
    public static final String PACKAGE_MANAGER_SENTINEL = "@pm@";
    public static final int SUCCESS = 0;
    private static final String TAG = "BackupManager";
    private static IBackupManager sService;
    private Context mContext;

    private class BackupManagerMonitorWrapper extends Stub {
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
            this.mHandler = new Handler(context.getMainLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            Pair<String, BackupProgress> obj = msg.obj;
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
        private final SelectBackupTransportCallback mListener;

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

    public int requestRestore(RestoreObserver observer) {
        return requestRestore(observer, null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0043  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int requestRestore(RestoreObserver observer, BackupManagerMonitor monitor) {
        Throwable th;
        int result = -1;
        checkServiceBinder();
        if (sService != null) {
            RestoreSession session = null;
            try {
                IRestoreSession binder = sService.beginRestoreSession(this.mContext.getPackageName(), null);
                if (binder != null) {
                    RestoreSession session2 = new RestoreSession(this.mContext, binder);
                    try {
                        result = session2.restorePackage(this.mContext.getPackageName(), observer, monitor);
                        session = session2;
                    } catch (RemoteException e) {
                        session = session2;
                        try {
                            Log.e(TAG, "restoreSelf() unable to contact service");
                            if (session != null) {
                            }
                            return result;
                        } catch (Throwable th2) {
                            th = th2;
                            if (session != null) {
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        session = session2;
                        if (session != null) {
                            session.endRestoreSession();
                        }
                        throw th;
                    }
                }
                if (session != null) {
                    session.endRestoreSession();
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "restoreSelf() unable to contact service");
                if (session != null) {
                    session.endRestoreSession();
                }
                return result;
            }
        }
        return result;
    }

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

    public void selectBackupTransport(ComponentName transport, SelectBackupTransportCallback listener) {
        checkServiceBinder();
        if (sService != null) {
            try {
                sService.selectBackupTransportAsync(transport, listener == null ? null : new SelectTransportListenerWrapper(this.mContext, listener));
            } catch (RemoteException e) {
                Log.e(TAG, "selectBackupTransportAsync() couldn't connect");
            }
        }
    }

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

    public int requestBackup(String[] packages, BackupObserver observer) {
        return requestBackup(packages, observer, null, 0);
    }

    public int requestBackup(String[] packages, BackupObserver observer, BackupManagerMonitor monitor, int flags) {
        checkServiceBinder();
        if (sService != null) {
            IBackupObserver observerWrapper;
            IBackupManagerMonitor monitorWrapper;
            if (observer == null) {
                observerWrapper = null;
            } else {
                observerWrapper = new BackupObserverWrapper(this.mContext, observer);
            }
            if (monitor == null) {
                monitorWrapper = null;
            } else {
                monitorWrapper = new BackupManagerMonitorWrapper(monitor);
            }
            try {
                return sService.requestBackup(packages, observerWrapper, monitorWrapper, flags);
            } catch (RemoteException e) {
                Log.e(TAG, "requestBackup() couldn't connect");
            }
        }
        return -1;
    }

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
}
