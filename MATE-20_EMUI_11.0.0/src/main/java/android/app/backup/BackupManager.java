package android.app.backup;

import android.Manifest;
import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
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
    @UnsupportedAppUsage
    private static IBackupManager sService;
    private Context mContext;

    @UnsupportedAppUsage
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
        IBackupManager iBackupManager = sService;
        if (iBackupManager != null) {
            try {
                iBackupManager.dataChanged(this.mContext.getPackageName());
            } catch (RemoteException e) {
                Log.d(TAG, "dataChanged() couldn't connect");
            }
        }
    }

    public static void dataChanged(String packageName) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager != null) {
            try {
                iBackupManager.dataChanged(packageName);
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
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            IRestoreSession binder = iBackupManager.beginRestoreSessionForUser(this.mContext.getUserId(), null, null);
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
        IBackupManager iBackupManager = sService;
        if (iBackupManager != null) {
            try {
                iBackupManager.setBackupEnabled(isEnabled);
            } catch (RemoteException e) {
                Log.e(TAG, "setBackupEnabled() couldn't connect");
            }
        }
    }

    @SystemApi
    public boolean isBackupEnabled() {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return false;
        }
        try {
            return iBackupManager.isBackupEnabled();
        } catch (RemoteException e) {
            Log.e(TAG, "isBackupEnabled() couldn't connect");
            return false;
        }
    }

    @SystemApi
    public boolean isBackupServiceActive(UserHandle user) {
        this.mContext.enforceCallingOrSelfPermission(Manifest.permission.BACKUP, "isBackupServiceActive");
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return false;
        }
        try {
            return iBackupManager.isBackupServiceActive(user.getIdentifier());
        } catch (RemoteException e) {
            Log.e(TAG, "isBackupEnabled() couldn't connect");
            return false;
        }
    }

    @SystemApi
    public void setAutoRestore(boolean isEnabled) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager != null) {
            try {
                iBackupManager.setAutoRestore(isEnabled);
            } catch (RemoteException e) {
                Log.e(TAG, "setAutoRestore() couldn't connect");
            }
        }
    }

    @SystemApi
    public String getCurrentTransport() {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            return iBackupManager.getCurrentTransport();
        } catch (RemoteException e) {
            Log.e(TAG, "getCurrentTransport() couldn't connect");
            return null;
        }
    }

    @SystemApi
    public ComponentName getCurrentTransportComponent() {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            return iBackupManager.getCurrentTransportComponentForUser(this.mContext.getUserId());
        } catch (RemoteException e) {
            Log.e(TAG, "getCurrentTransportComponent() couldn't connect");
            return null;
        }
    }

    @SystemApi
    public String[] listAllTransports() {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            return iBackupManager.listAllTransports();
        } catch (RemoteException e) {
            Log.e(TAG, "listAllTransports() couldn't connect");
            return null;
        }
    }

    @SystemApi
    @Deprecated
    public void updateTransportAttributes(ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, String dataManagementLabel) {
        updateTransportAttributes(transportComponent, name, configurationIntent, currentDestinationString, dataManagementIntent, (CharSequence) dataManagementLabel);
    }

    @SystemApi
    public void updateTransportAttributes(ComponentName transportComponent, String name, Intent configurationIntent, String currentDestinationString, Intent dataManagementIntent, CharSequence dataManagementLabel) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager != null) {
            try {
                iBackupManager.updateTransportAttributesForUser(this.mContext.getUserId(), transportComponent, name, configurationIntent, currentDestinationString, dataManagementIntent, dataManagementLabel);
            } catch (RemoteException e) {
                Log.e(TAG, "describeTransport() couldn't connect");
            }
        }
    }

    @SystemApi
    @Deprecated
    public String selectBackupTransport(String transport) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            return iBackupManager.selectBackupTransport(transport);
        } catch (RemoteException e) {
            Log.e(TAG, "selectBackupTransport() couldn't connect");
            return null;
        }
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
            sService.selectBackupTransportAsyncForUser(this.mContext.getUserId(), transport, wrapper);
        }
    }

    @SystemApi
    public void backupNow() {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager != null) {
            try {
                iBackupManager.backupNow();
            } catch (RemoteException e) {
                Log.e(TAG, "backupNow() couldn't connect");
            }
        }
    }

    @SystemApi
    public long getAvailableRestoreToken(String packageName) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return 0;
        }
        try {
            return iBackupManager.getAvailableRestoreTokenForUser(this.mContext.getUserId(), packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "getAvailableRestoreToken() couldn't connect");
            return 0;
        }
    }

    @SystemApi
    public boolean isAppEligibleForBackup(String packageName) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return false;
        }
        try {
            return iBackupManager.isAppEligibleForBackupForUser(this.mContext.getUserId(), packageName);
        } catch (RemoteException e) {
            Log.e(TAG, "isAppEligibleForBackup(pkg) couldn't connect");
            return false;
        }
    }

    @SystemApi
    public int requestBackup(String[] packages, BackupObserver observer) {
        return requestBackup(packages, observer, null, 0);
    }

    @SystemApi
    public int requestBackup(String[] packages, BackupObserver observer, BackupManagerMonitor monitor, int flags) {
        BackupObserverWrapper observerWrapper;
        checkServiceBinder();
        if (sService == null) {
            return -1;
        }
        BackupManagerMonitorWrapper monitorWrapper = null;
        if (observer == null) {
            observerWrapper = null;
        } else {
            try {
                observerWrapper = new BackupObserverWrapper(this.mContext, observer);
            } catch (RemoteException e) {
                Log.e(TAG, "requestBackup() couldn't connect");
                return -1;
            }
        }
        if (monitor != null) {
            monitorWrapper = new BackupManagerMonitorWrapper(monitor);
        }
        return sService.requestBackup(packages, observerWrapper, monitorWrapper, flags);
    }

    @SystemApi
    public void cancelBackups() {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager != null) {
            try {
                iBackupManager.cancelBackups();
            } catch (RemoteException e) {
                Log.e(TAG, "cancelBackups() couldn't connect.");
            }
        }
    }

    public UserHandle getUserForAncestralSerialNumber(long ancestralSerialNumber) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            return iBackupManager.getUserForAncestralSerialNumber(ancestralSerialNumber);
        } catch (RemoteException e) {
            Log.e(TAG, "getUserForAncestralSerialNumber() couldn't connect");
            return null;
        }
    }

    @SystemApi
    public void setAncestralSerialNumber(long ancestralSerialNumber) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager != null) {
            try {
                iBackupManager.setAncestralSerialNumber(ancestralSerialNumber);
            } catch (RemoteException e) {
                Log.e(TAG, "setAncestralSerialNumber() couldn't connect");
            }
        }
    }

    @SystemApi
    public Intent getConfigurationIntent(String transportName) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            return iBackupManager.getConfigurationIntentForUser(this.mContext.getUserId(), transportName);
        } catch (RemoteException e) {
            Log.e(TAG, "getConfigurationIntent() couldn't connect");
            return null;
        }
    }

    @SystemApi
    public String getDestinationString(String transportName) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            return iBackupManager.getDestinationStringForUser(this.mContext.getUserId(), transportName);
        } catch (RemoteException e) {
            Log.e(TAG, "getDestinationString() couldn't connect");
            return null;
        }
    }

    @SystemApi
    public Intent getDataManagementIntent(String transportName) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            return iBackupManager.getDataManagementIntentForUser(this.mContext.getUserId(), transportName);
        } catch (RemoteException e) {
            Log.e(TAG, "getDataManagementIntent() couldn't connect");
            return null;
        }
    }

    @SystemApi
    @Deprecated
    public String getDataManagementLabel(String transportName) {
        CharSequence label = getDataManagementIntentLabel(transportName);
        if (label == null) {
            return null;
        }
        return label.toString();
    }

    @SystemApi
    public CharSequence getDataManagementIntentLabel(String transportName) {
        checkServiceBinder();
        IBackupManager iBackupManager = sService;
        if (iBackupManager == null) {
            return null;
        }
        try {
            return iBackupManager.getDataManagementLabelForUser(this.mContext.getUserId(), transportName);
        } catch (RemoteException e) {
            Log.e(TAG, "getDataManagementIntentLabel() couldn't connect");
            return null;
        }
    }

    /* access modifiers changed from: private */
    public class BackupObserverWrapper extends IBackupObserver.Stub {
        static final int MSG_FINISHED = 3;
        static final int MSG_RESULT = 2;
        static final int MSG_UPDATE = 1;
        final Handler mHandler;
        final BackupObserver mObserver;

        BackupObserverWrapper(Context context, BackupObserver observer) {
            this.mHandler = new Handler(context.getMainLooper(), BackupManager.this) {
                /* class android.app.backup.BackupManager.BackupObserverWrapper.AnonymousClass1 */

                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    int i = msg.what;
                    if (i == 1) {
                        Pair<String, BackupProgress> obj = (Pair) msg.obj;
                        BackupObserverWrapper.this.mObserver.onUpdate(obj.first, obj.second);
                    } else if (i == 2) {
                        BackupObserverWrapper.this.mObserver.onResult((String) msg.obj, msg.arg1);
                    } else if (i != 3) {
                        Log.w(BackupManager.TAG, "Unknown message: " + msg);
                    } else {
                        BackupObserverWrapper.this.mObserver.backupFinished(msg.arg1);
                    }
                }
            };
            this.mObserver = observer;
        }

        @Override // android.app.backup.IBackupObserver
        public void onUpdate(String currentPackage, BackupProgress backupProgress) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(1, Pair.create(currentPackage, backupProgress)));
        }

        @Override // android.app.backup.IBackupObserver
        public void onResult(String currentPackage, int status) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(2, status, 0, currentPackage));
        }

        @Override // android.app.backup.IBackupObserver
        public void backupFinished(int status) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(3, status, 0));
        }
    }

    private class SelectTransportListenerWrapper extends ISelectBackupTransportCallback.Stub {
        private final Handler mHandler;
        private final SelectBackupTransportCallback mListener;

        SelectTransportListenerWrapper(Context context, SelectBackupTransportCallback listener) {
            this.mHandler = new Handler(context.getMainLooper());
            this.mListener = listener;
        }

        @Override // android.app.backup.ISelectBackupTransportCallback
        public void onSuccess(final String transportName) {
            this.mHandler.post(new Runnable() {
                /* class android.app.backup.BackupManager.SelectTransportListenerWrapper.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    SelectTransportListenerWrapper.this.mListener.onSuccess(transportName);
                }
            });
        }

        @Override // android.app.backup.ISelectBackupTransportCallback
        public void onFailure(final int reason) {
            this.mHandler.post(new Runnable() {
                /* class android.app.backup.BackupManager.SelectTransportListenerWrapper.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    SelectTransportListenerWrapper.this.mListener.onFailure(reason);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public class BackupManagerMonitorWrapper extends IBackupManagerMonitor.Stub {
        final BackupManagerMonitor mMonitor;

        BackupManagerMonitorWrapper(BackupManagerMonitor monitor) {
            this.mMonitor = monitor;
        }

        @Override // android.app.backup.IBackupManagerMonitor
        public void onEvent(Bundle event) throws RemoteException {
            this.mMonitor.onEvent(event);
        }
    }
}
