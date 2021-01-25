package android.content;

import android.accounts.Account;
import android.content.ISyncAdapter;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Log;
import com.android.internal.util.function.pooled.PooledLambda;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractThreadedSyncAdapter {
    private static final boolean ENABLE_LOG = (Build.IS_DEBUGGABLE && Log.isLoggable(TAG, 3));
    @Deprecated
    public static final int LOG_SYNC_DETAILS = 2743;
    private static final String TAG = "SyncAdapter";
    private boolean mAllowParallelSyncs;
    private final boolean mAutoInitialize;
    private final Context mContext;
    private final ISyncAdapterImpl mISyncAdapterImpl;
    private final AtomicInteger mNumSyncStarts;
    private final Object mSyncThreadLock;
    private final HashMap<Account, SyncThread> mSyncThreads;

    public abstract void onPerformSync(Account account, Bundle bundle, String str, ContentProviderClient contentProviderClient, SyncResult syncResult);

    public AbstractThreadedSyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public AbstractThreadedSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        this.mSyncThreads = new HashMap<>();
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Account toSyncKey(Account account) {
        if (this.mAllowParallelSyncs) {
            return account;
        }
        return null;
    }

    /* access modifiers changed from: private */
    public class ISyncAdapterImpl extends ISyncAdapter.Stub {
        private ISyncAdapterImpl() {
        }

        @Override // android.content.ISyncAdapter
        public void onUnsyncableAccount(ISyncAdapterUnsyncableAccountCallback cb) {
            Handler.getMain().sendMessage(PooledLambda.obtainMessage($$Lambda$AbstractThreadedSyncAdapter$ISyncAdapterImpl$L6ZtOCe8gjKwJj0908ytPlrD8Rc.INSTANCE, AbstractThreadedSyncAdapter.this, cb));
        }

        /* JADX WARNING: Code restructure failed: missing block: B:24:0x0087, code lost:
            if (android.content.AbstractThreadedSyncAdapter.ENABLE_LOG == false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0089, code lost:
            android.util.Log.d(android.content.AbstractThreadedSyncAdapter.TAG, "startSync() finishing");
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:?, code lost:
            return;
         */
        /* JADX WARNING: Removed duplicated region for block: B:53:0x010e A[Catch:{ all -> 0x00fd }] */
        /* JADX WARNING: Removed duplicated region for block: B:57:0x011e  */
        @Override // android.content.ISyncAdapter
        public void startSync(ISyncContext syncContext, String authority, Account account, Bundle extras) {
            Throwable th;
            Throwable th2;
            SyncThread syncThread;
            if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                if (extras != null) {
                    extras.size();
                }
                Log.d(AbstractThreadedSyncAdapter.TAG, "startSync() start " + authority + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + account + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + extras);
            }
            try {
                SyncContext syncContextClient = new SyncContext(syncContext);
                Account threadsKey = AbstractThreadedSyncAdapter.this.toSyncKey(account);
                synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                    if (AbstractThreadedSyncAdapter.this.mSyncThreads.containsKey(threadsKey)) {
                        if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                            Log.d(AbstractThreadedSyncAdapter.TAG, "  alreadyInProgress");
                        }
                        syncThread = 1;
                    } else if (!AbstractThreadedSyncAdapter.this.mAutoInitialize || extras == null || !extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false)) {
                        SyncThread syncThread2 = new SyncThread("SyncAdapterThread-" + AbstractThreadedSyncAdapter.this.mNumSyncStarts.incrementAndGet(), syncContextClient, authority, account, extras);
                        AbstractThreadedSyncAdapter.this.mSyncThreads.put(threadsKey, syncThread2);
                        syncThread2.start();
                        syncThread = null;
                    } else {
                        try {
                            if (ContentResolver.getIsSyncable(account, authority) < 0) {
                                ContentResolver.setIsSyncable(account, authority, 1);
                            }
                        } finally {
                            syncContextClient.onFinished(new SyncResult());
                        }
                    }
                }
                if (syncThread != null) {
                    try {
                        syncContextClient.onFinished(SyncResult.ALREADY_IN_PROGRESS);
                    } catch (Error | RuntimeException e) {
                        th2 = e;
                        try {
                            if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                                Log.d(AbstractThreadedSyncAdapter.TAG, "startSync() caught exception", th2);
                            }
                            throw th2;
                        } catch (Throwable th3) {
                            th = th3;
                            if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                            }
                            throw th;
                        }
                    }
                }
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "startSync() finishing");
                }
            } catch (Error | RuntimeException e2) {
                th2 = e2;
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                }
                throw th2;
            } catch (Throwable th4) {
                th = th4;
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "startSync() finishing");
                }
                throw th;
            }
        }

        @Override // android.content.ISyncAdapter
        public void cancelSync(ISyncContext syncContext) {
            SyncThread info = null;
            synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                Iterator it = AbstractThreadedSyncAdapter.this.mSyncThreads.values().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    SyncThread current = (SyncThread) it.next();
                    if (current.mSyncContext.getSyncContextBinder() == syncContext.asBinder()) {
                        info = current;
                        break;
                    }
                }
            }
            if (info != null) {
                try {
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "cancelSync() " + info.mAuthority + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + info.mAccount);
                    }
                    if (AbstractThreadedSyncAdapter.this.mAllowParallelSyncs) {
                        AbstractThreadedSyncAdapter.this.onSyncCanceled(info);
                    } else {
                        AbstractThreadedSyncAdapter.this.onSyncCanceled();
                    }
                } catch (Error | RuntimeException th) {
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "cancelSync() caught exception", th);
                    }
                    throw th;
                } catch (Throwable th2) {
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "cancelSync() finishing");
                    }
                    throw th2;
                }
            } else if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                Log.w(AbstractThreadedSyncAdapter.TAG, "cancelSync() unknown context");
            }
            if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                Log.d(AbstractThreadedSyncAdapter.TAG, "cancelSync() finishing");
            }
        }
    }

    /* access modifiers changed from: private */
    public class SyncThread extends Thread {
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

        /* JADX WARNING: Removed duplicated region for block: B:101:0x0168 A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:69:0x00f3 A[Catch:{ SecurityException -> 0x00fc, all -> 0x00ea }] */
        /* JADX WARNING: Removed duplicated region for block: B:74:0x0103 A[Catch:{ SecurityException -> 0x00fc, all -> 0x00ea }] */
        /* JADX WARNING: Removed duplicated region for block: B:78:0x011c  */
        /* JADX WARNING: Removed duplicated region for block: B:81:0x0125  */
        /* JADX WARNING: Removed duplicated region for block: B:84:0x0131 A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:95:0x0153  */
        /* JADX WARNING: Removed duplicated region for block: B:98:0x015c  */
        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            Throwable th;
            SecurityException e;
            Throwable th2;
            Process.setThreadPriority(10);
            if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                Log.d(AbstractThreadedSyncAdapter.TAG, "Thread started");
            }
            Trace.traceBegin(128, this.mAuthority);
            SyncResult syncResult = new SyncResult();
            ContentProviderClient provider = null;
            try {
                if (isCanceled()) {
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "Already canceled");
                    }
                    Trace.traceEnd(128);
                    if (0 != 0) {
                        provider.release();
                    }
                    if (!isCanceled()) {
                        this.mSyncContext.onFinished(syncResult);
                    }
                    synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                        AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
                    }
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "Thread finished");
                        return;
                    }
                    return;
                }
                try {
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "Calling onPerformSync...");
                    }
                    ContentProviderClient provider2 = AbstractThreadedSyncAdapter.this.mContext.getContentResolver().acquireContentProviderClient(this.mAuthority);
                    if (provider2 != null) {
                        try {
                            AbstractThreadedSyncAdapter.this.onPerformSync(this.mAccount, this.mExtras, this.mAuthority, provider2, syncResult);
                        } catch (SecurityException e2) {
                            e = e2;
                            provider = provider2;
                            if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                            }
                            AbstractThreadedSyncAdapter.this.onSecurityException(this.mAccount, this.mExtras, this.mAuthority, syncResult);
                            syncResult.databaseError = true;
                            Trace.traceEnd(128);
                            if (provider != null) {
                            }
                            if (!isCanceled()) {
                            }
                            synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                            }
                        } catch (Error | RuntimeException e3) {
                            th2 = e3;
                            provider = provider2;
                            if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                            }
                            throw th2;
                        } catch (Throwable th3) {
                            th = th3;
                            provider = provider2;
                            Trace.traceEnd(128);
                            if (provider != null) {
                            }
                            if (!isCanceled()) {
                            }
                            synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                            }
                        }
                    } else {
                        syncResult.databaseError = true;
                    }
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "onPerformSync done");
                    }
                    Trace.traceEnd(128);
                    if (provider2 != null) {
                        provider2.release();
                    }
                    if (!isCanceled()) {
                        this.mSyncContext.onFinished(syncResult);
                    }
                    synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                        AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
                    }
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "Thread finished");
                    }
                } catch (Error | RuntimeException e4) {
                    th2 = e4;
                    if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                        Log.d(AbstractThreadedSyncAdapter.TAG, "caught exception", th2);
                    }
                    throw th2;
                }
            } catch (SecurityException e5) {
                e = e5;
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "SecurityException", e);
                }
                AbstractThreadedSyncAdapter.this.onSecurityException(this.mAccount, this.mExtras, this.mAuthority, syncResult);
                syncResult.databaseError = true;
                Trace.traceEnd(128);
                if (provider != null) {
                    provider.release();
                }
                if (!isCanceled()) {
                    this.mSyncContext.onFinished(syncResult);
                }
                synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                    AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
                }
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "Thread finished");
                }
            } catch (Throwable th4) {
                th = th4;
                Trace.traceEnd(128);
                if (provider != null) {
                    provider.release();
                }
                if (!isCanceled()) {
                    this.mSyncContext.onFinished(syncResult);
                }
                synchronized (AbstractThreadedSyncAdapter.this.mSyncThreadLock) {
                    AbstractThreadedSyncAdapter.this.mSyncThreads.remove(this.mThreadsKey);
                }
                if (AbstractThreadedSyncAdapter.ENABLE_LOG) {
                    Log.d(AbstractThreadedSyncAdapter.TAG, "Thread finished");
                }
                throw th;
            }
        }

        private boolean isCanceled() {
            return Thread.currentThread().isInterrupted();
        }
    }

    public final IBinder getSyncAdapterBinder() {
        return this.mISyncAdapterImpl.asBinder();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleOnUnsyncableAccount(ISyncAdapterUnsyncableAccountCallback cb) {
        boolean doSync;
        try {
            doSync = onUnsyncableAccount();
        } catch (RuntimeException e) {
            Log.e(TAG, "Exception while calling onUnsyncableAccount, assuming 'true'", e);
            doSync = true;
        }
        try {
            cb.onUnsyncableAccountDone(doSync);
        } catch (RemoteException e2) {
            Log.e(TAG, "Could not report result of onUnsyncableAccount", e2);
        }
    }

    public boolean onUnsyncableAccount() {
        return true;
    }

    public void onSecurityException(Account account, Bundle extras, String authority, SyncResult syncResult) {
    }

    public void onSyncCanceled() {
        SyncThread syncThread;
        synchronized (this.mSyncThreadLock) {
            syncThread = this.mSyncThreads.get(null);
        }
        if (syncThread != null) {
            syncThread.interrupt();
        }
    }

    public void onSyncCanceled(Thread thread) {
        thread.interrupt();
    }
}
