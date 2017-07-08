package android.content;

import android.accounts.Account;
import android.content.ISyncAdapter.Stub;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.Trace;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractThreadedSyncAdapter {
    @Deprecated
    public static final int LOG_SYNC_DETAILS = 2743;
    private boolean mAllowParallelSyncs;
    private final boolean mAutoInitialize;
    private final Context mContext;
    private final ISyncAdapterImpl mISyncAdapterImpl;
    private final AtomicInteger mNumSyncStarts;
    private final Object mSyncThreadLock;
    private final HashMap<Account, SyncThread> mSyncThreads;

    private class ISyncAdapterImpl extends Stub {
        private ISyncAdapterImpl() {
        }

        public void startSync(ISyncContext syncContext, String authority, Account account, Bundle extras) {
            SyncContext syncContextClient = new SyncContext(syncContext);
            Account threadsKey = AbstractThreadedSyncAdapter.this.toSyncKey(account);
            synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                boolean alreadyInProgress;
                if (AbstractThreadedSyncAdapter.this.mSyncThreads.containsKey(threadsKey)) {
                    alreadyInProgress = true;
                } else if (AbstractThreadedSyncAdapter.this.mAutoInitialize && extras != null && extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false)) {
                    try {
                        if (ContentResolver.getIsSyncable(account, authority) < 0) {
                            ContentResolver.setIsSyncable(account, authority, 1);
                        }
                        syncContextClient.onFinished(new SyncResult());
                        return;
                    } catch (Throwable th) {
                        syncContextClient.onFinished(new SyncResult());
                    }
                } else {
                    SyncThread syncThread = new SyncThread("SyncAdapterThread-" + AbstractThreadedSyncAdapter.this.mNumSyncStarts.incrementAndGet(), syncContextClient, authority, account, extras, null);
                    AbstractThreadedSyncAdapter.this.mSyncThreads.put(threadsKey, syncThread);
                    syncThread.start();
                    alreadyInProgress = false;
                }
                if (alreadyInProgress) {
                    syncContextClient.onFinished(SyncResult.ALREADY_IN_PROGRESS);
                }
            }
        }

        public void cancelSync(ISyncContext syncContext) {
            Thread info = null;
            synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                for (Thread current : AbstractThreadedSyncAdapter.this.mSyncThreads.values()) {
                    if (current.mSyncContext.getSyncContextBinder() == syncContext.asBinder()) {
                        info = current;
                        break;
                    }
                }
            }
            if (info == null) {
                return;
            }
            if (AbstractThreadedSyncAdapter.this.mAllowParallelSyncs) {
                AbstractThreadedSyncAdapter.this.onSyncCanceled(info);
            } else {
                AbstractThreadedSyncAdapter.this.onSyncCanceled();
            }
        }

        public void initialize(Account account, String authority) throws RemoteException {
            Bundle extras = new Bundle();
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, true);
            startSync(null, authority, account, extras);
        }
    }

    private class SyncThread extends Thread {
        private final Account mAccount;
        private final String mAuthority;
        private final Bundle mExtras;
        private final SyncContext mSyncContext;
        private final Account mThreadsKey;

        private SyncThread(String name, SyncContext syncContext, String authority, Account account, Bundle extras) {
            super(name);
            this.mSyncContext = syncContext;
            this.mAuthority = authority;
            this.mAccount = account;
            this.mExtras = extras;
            this.mThreadsKey = AbstractThreadedSyncAdapter.this.toSyncKey(account);
        }

        public void run() {
            Object -get4;
            Process.setThreadPriority(10);
            Trace.traceBegin(128, this.mAuthority);
            SyncResult syncResult = new SyncResult();
            ContentProviderClient contentProviderClient = null;
            try {
                if (isCanceled()) {
                    Trace.traceEnd(128);
                    if (!isCanceled()) {
                        this.mSyncContext.onFinished(syncResult);
                    }
                    synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                        AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
                    }
                    return;
                }
                contentProviderClient = AbstractThreadedSyncAdapter.this.mContext.getContentResolver().acquireContentProviderClient(this.mAuthority);
                if (contentProviderClient != null) {
                    AbstractThreadedSyncAdapter.this.onPerformSync(this.mAccount, this.mExtras, this.mAuthority, contentProviderClient, syncResult);
                } else {
                    syncResult.databaseError = true;
                }
                Trace.traceEnd(128);
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                if (!isCanceled()) {
                    this.mSyncContext.onFinished(syncResult);
                }
                -get4 = AbstractThreadedSyncAdapter.this.mSyncThreadLock;
                synchronized (-get4) {
                }
                AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
            } catch (SecurityException e) {
                AbstractThreadedSyncAdapter.this.onSecurityException(this.mAccount, this.mExtras, this.mAuthority, syncResult);
                syncResult.databaseError = true;
                Trace.traceEnd(128);
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                if (!isCanceled()) {
                    this.mSyncContext.onFinished(syncResult);
                }
                -get4 = AbstractThreadedSyncAdapter.this.mSyncThreadLock;
                synchronized (-get4) {
                }
                AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
            } catch (Throwable th) {
                Trace.traceEnd(128);
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                if (!isCanceled()) {
                    this.mSyncContext.onFinished(syncResult);
                }
                synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                }
                AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
            }
        }

        private boolean isCanceled() {
            return Thread.currentThread().isInterrupted();
        }
    }

    public abstract void onPerformSync(Account account, Bundle bundle, String str, ContentProviderClient contentProviderClient, SyncResult syncResult);

    public AbstractThreadedSyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public AbstractThreadedSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        this.mSyncThreads = new HashMap();
        this.mSyncThreadLock = new Object();
        this.mContext = context;
        this.mISyncAdapterImpl = new ISyncAdapterImpl();
        this.mNumSyncStarts = new AtomicInteger(0);
        this.mAutoInitialize = autoInitialize;
        this.mAllowParallelSyncs = allowParallelSyncs;
    }

    public Context getContext() {
        return this.mContext;
    }

    private Account toSyncKey(Account account) {
        if (this.mAllowParallelSyncs) {
            return account;
        }
        return null;
    }

    public final IBinder getSyncAdapterBinder() {
        return this.mISyncAdapterImpl.asBinder();
    }

    public void onSecurityException(Account account, Bundle extras, String authority, SyncResult syncResult) {
    }

    public void onSyncCanceled() {
        synchronized (this.mSyncThreadLock) {
            SyncThread syncThread = (SyncThread) this.mSyncThreads.get(null);
        }
        if (syncThread != null) {
            syncThread.interrupt();
        }
    }

    public void onSyncCanceled(Thread thread) {
        thread.interrupt();
    }
}
