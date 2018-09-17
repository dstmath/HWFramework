package android.app.backup;

import android.app.backup.IBackupObserver.Stub;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.DataContract.Apps.LaunchMode;
import android.util.Log;
import android.util.Pair;

public class BackupManager {
    public static final int ERROR_AGENT_FAILURE = -1003;
    public static final int ERROR_BACKUP_NOT_ALLOWED = -2001;
    public static final int ERROR_PACKAGE_NOT_FOUND = -2002;
    public static final int ERROR_TRANSPORT_ABORTED = -1000;
    public static final int ERROR_TRANSPORT_PACKAGE_REJECTED = -1002;
    public static final int ERROR_TRANSPORT_QUOTA_EXCEEDED = -1005;
    public static final int SUCCESS = 0;
    private static final String TAG = "BackupManager";
    private static IBackupManager sService;
    private Context mContext;

    private class BackupObserverWrapper extends Stub {
        static final int MSG_FINISHED = 3;
        static final int MSG_RESULT = 2;
        static final int MSG_UPDATE = 1;
        final Handler mHandler;
        final BackupObserver mObserver;

        /* renamed from: android.app.backup.BackupManager.BackupObserverWrapper.1 */
        class AnonymousClass1 extends Handler {
            AnonymousClass1(Looper $anonymous0) {
                super($anonymous0);
            }

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BackupObserverWrapper.MSG_UPDATE /*1*/:
                        Pair<String, BackupProgress> obj = msg.obj;
                        BackupObserverWrapper.this.mObserver.onUpdate((String) obj.first, (BackupProgress) obj.second);
                    case BackupObserverWrapper.MSG_RESULT /*2*/:
                        BackupObserverWrapper.this.mObserver.onResult((String) msg.obj, msg.arg1);
                    case BackupObserverWrapper.MSG_FINISHED /*3*/:
                        BackupObserverWrapper.this.mObserver.backupFinished(msg.arg1);
                    default:
                        Log.w(BackupManager.TAG, "Unknown message: " + msg);
                }
            }
        }

        BackupObserverWrapper(Context context, BackupObserver observer) {
            this.mHandler = new AnonymousClass1(context.getMainLooper());
            this.mObserver = observer;
        }

        public void onUpdate(String currentPackage, BackupProgress backupProgress) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_UPDATE, Pair.create(currentPackage, backupProgress)));
        }

        public void onResult(String currentPackage, int status) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_RESULT, status, BackupManager.SUCCESS, currentPackage));
        }

        public void backupFinished(int status) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(MSG_FINISHED, status, BackupManager.SUCCESS));
        }
    }

    private static void checkServiceBinder() {
        if (sService == null) {
            sService = IBackupManager.Stub.asInterface(ServiceManager.getService(LaunchMode.BACKUP));
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
        Throwable th;
        int result = -1;
        checkServiceBinder();
        if (sService != null) {
            RestoreSession restoreSession = null;
            try {
                IRestoreSession binder = sService.beginRestoreSession(this.mContext.getPackageName(), null);
                if (binder != null) {
                    RestoreSession session = new RestoreSession(this.mContext, binder);
                    try {
                        result = session.restorePackage(this.mContext.getPackageName(), observer);
                        restoreSession = session;
                    } catch (RemoteException e) {
                        restoreSession = session;
                        try {
                            Log.e(TAG, "restoreSelf() unable to contact service");
                            if (restoreSession != null) {
                                restoreSession.endRestoreSession();
                            }
                            return result;
                        } catch (Throwable th2) {
                            th = th2;
                            if (restoreSession != null) {
                                restoreSession.endRestoreSession();
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        restoreSession = session;
                        if (restoreSession != null) {
                            restoreSession.endRestoreSession();
                        }
                        throw th;
                    }
                }
                if (restoreSession != null) {
                    restoreSession.endRestoreSession();
                }
            } catch (RemoteException e2) {
                Log.e(TAG, "restoreSelf() unable to contact service");
                if (restoreSession != null) {
                    restoreSession.endRestoreSession();
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
        checkServiceBinder();
        if (sService != null) {
            IBackupObserver iBackupObserver;
            if (observer == null) {
                iBackupObserver = null;
            } else {
                iBackupObserver = new BackupObserverWrapper(this.mContext, observer);
            }
            try {
                return sService.requestBackup(packages, iBackupObserver);
            } catch (RemoteException e) {
                Log.e(TAG, "requestBackup() couldn't connect");
            }
        }
        return -1;
    }
}
