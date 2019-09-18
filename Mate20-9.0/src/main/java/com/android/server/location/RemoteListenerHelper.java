package com.android.server.location;

import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

abstract class RemoteListenerHelper<TListener extends IInterface> {
    protected static final int RESULT_GPS_LOCATION_DISABLED = 3;
    protected static final int RESULT_INTERNAL_ERROR = 4;
    protected static final int RESULT_NOT_ALLOWED = 6;
    protected static final int RESULT_NOT_AVAILABLE = 1;
    protected static final int RESULT_NOT_SUPPORTED = 2;
    protected static final int RESULT_SUCCESS = 0;
    protected static final int RESULT_UNKNOWN = 5;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private boolean mHasIsSupported;
    /* access modifiers changed from: private */
    public volatile boolean mIsRegistered;
    private boolean mIsSupported;
    private int mLastReportedResult = 5;
    /* access modifiers changed from: private */
    public final Map<IBinder, RemoteListenerHelper<TListener>.LinkedListener> mListenerMap = new HashMap();
    /* access modifiers changed from: private */
    public final String mTag;

    private class HandlerRunnable implements Runnable {
        private final TListener mListener;
        private final String mName;
        private final ListenerOperation<TListener> mOperation;

        public HandlerRunnable(TListener listener, ListenerOperation<TListener> operation, String name) {
            this.mListener = listener;
            this.mOperation = operation;
            this.mName = name;
        }

        public void run() {
            try {
                this.mOperation.execute(this.mListener);
            } catch (RemoteException e) {
                String access$400 = RemoteListenerHelper.this.mTag;
                Log.v(access$400, "mName = " + this.mName + " Error in monitored listener.", e);
            }
        }
    }

    private class LinkedListener implements IBinder.DeathRecipient {
        private final TListener mListener;
        private String mPkgName;
        public AtomicInteger mSkipCount = new AtomicInteger(0);

        public LinkedListener(TListener listener) {
            this.mListener = listener;
        }

        public TListener getUnderlyingListener() {
            return this.mListener;
        }

        public void binderDied() {
            String access$400 = RemoteListenerHelper.this.mTag;
            Log.d(access$400, "Remote Listener died: " + this.mListener + " of " + this.mPkgName);
            RemoteListenerHelper.this.removeListener(this.mListener);
        }

        public void setPkgName(String pkgName) {
            this.mPkgName = pkgName;
        }

        public String getPkgName() {
            return this.mPkgName;
        }
    }

    protected interface ListenerOperation<TListener extends IInterface> {
        void execute(TListener tlistener) throws RemoteException;
    }

    /* access modifiers changed from: protected */
    public abstract ListenerOperation<TListener> getHandlerOperation(int i);

    /* access modifiers changed from: protected */
    public abstract boolean isAvailableInPlatform();

    /* access modifiers changed from: protected */
    public abstract boolean isGpsEnabled();

    /* access modifiers changed from: protected */
    public abstract int registerWithService();

    /* access modifiers changed from: protected */
    public abstract void unregisterFromService();

    protected RemoteListenerHelper(Handler handler, String name) {
        Preconditions.checkNotNull(name);
        this.mHandler = handler;
        this.mTag = name;
    }

    public boolean isRegistered() {
        return this.mIsRegistered;
    }

    public boolean addListener(TListener listener, String pkgName) {
        if (!addListener(listener)) {
            return false;
        }
        synchronized (this.mListenerMap) {
            RemoteListenerHelper<TListener>.LinkedListener deathListener = this.mListenerMap.get(listener.asBinder());
            if (deathListener != null) {
                deathListener.setPkgName(pkgName);
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:32:0x005a, code lost:
        return true;
     */
    public boolean addListener(TListener listener) {
        int result;
        Preconditions.checkNotNull(listener, "Attempted to register a 'null' listener.");
        IBinder binder = listener.asBinder();
        RemoteListenerHelper<TListener>.LinkedListener deathListener = new LinkedListener(listener);
        synchronized (this.mListenerMap) {
            if (this.mListenerMap.containsKey(binder)) {
                return true;
            }
            try {
                binder.linkToDeath(deathListener, 0);
                this.mListenerMap.put(binder, deathListener);
                if (!isAvailableInPlatform()) {
                    result = 1;
                } else if (this.mHasIsSupported != 0 && !this.mIsSupported) {
                    result = 2;
                } else if (!isGpsEnabled()) {
                    result = 3;
                } else if (this.mHasIsSupported && this.mIsSupported) {
                    tryRegister();
                    result = 0;
                }
                post(listener, getHandlerOperation(result), BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
                return true;
            } catch (RemoteException e) {
                Log.v(this.mTag, "Remote listener already died.", e);
                return false;
            }
        }
    }

    public void removeListener(TListener listener) {
        RemoteListenerHelper<TListener>.LinkedListener linkedListener;
        Preconditions.checkNotNull(listener, "Attempted to remove a 'null' listener.");
        IBinder binder = listener.asBinder();
        synchronized (this.mListenerMap) {
            linkedListener = this.mListenerMap.remove(binder);
            if (this.mListenerMap.isEmpty()) {
                tryUnregister();
            }
        }
        if (linkedListener != null) {
            binder.unlinkToDeath(linkedListener, 0);
        }
    }

    /* access modifiers changed from: protected */
    public void foreach(ListenerOperation<TListener> operation) {
        synchronized (this.mListenerMap) {
            foreachUnsafe(operation);
        }
    }

    /* access modifiers changed from: protected */
    public void foreachDirect(ListenerOperation<TListener> operation) {
        synchronized (this.mListenerMap) {
            for (RemoteListenerHelper<TListener>.LinkedListener linkedListener : this.mListenerMap.values()) {
                if (!GpsFreezeProc.getInstance().isFreeze(linkedListener.getPkgName())) {
                    if (linkedListener.mSkipCount.get() > 0) {
                        linkedListener.mSkipCount.getAndDecrement();
                    } else {
                        try {
                            operation.execute(linkedListener.getUnderlyingListener());
                        } catch (RemoteException e) {
                            String str = this.mTag;
                            Log.e(str, "Error in monitored listener to " + linkedListener.getPkgName());
                            linkedListener.mSkipCount.set(1000);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setSupported(boolean value) {
        synchronized (this.mListenerMap) {
            this.mHasIsSupported = true;
            this.mIsSupported = value;
        }
    }

    /* access modifiers changed from: protected */
    public void tryUpdateRegistrationWithService() {
        synchronized (this.mListenerMap) {
            if (!isGpsEnabled()) {
                tryUnregister();
            } else if (!this.mListenerMap.isEmpty()) {
                tryRegister();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateResult() {
        synchronized (this.mListenerMap) {
            int newResult = calculateCurrentResultUnsafe();
            if (this.mLastReportedResult != newResult) {
                foreachUnsafe(getHandlerOperation(newResult));
                this.mLastReportedResult = newResult;
            }
        }
    }

    /* access modifiers changed from: private */
    public void foreachUnsafe(ListenerOperation<TListener> operation) {
        for (RemoteListenerHelper<TListener>.LinkedListener linkedListener : this.mListenerMap.values()) {
            if (!GpsFreezeProc.getInstance().isFreeze(linkedListener.getPkgName())) {
                post(linkedListener.getUnderlyingListener(), operation, linkedListener.getPkgName());
            }
        }
    }

    private void post(TListener listener, ListenerOperation<TListener> operation, String name) {
        if (operation != null) {
            this.mHandler.post(new HandlerRunnable(listener, operation, name));
        }
    }

    private void tryRegister() {
        this.mHandler.post(new Runnable() {
            int registrationState = 4;

            public void run() {
                if (!RemoteListenerHelper.this.mIsRegistered) {
                    this.registrationState = RemoteListenerHelper.this.registerWithService();
                    boolean unused = RemoteListenerHelper.this.mIsRegistered = this.registrationState == 0;
                }
                if (!RemoteListenerHelper.this.mIsRegistered) {
                    RemoteListenerHelper.this.mHandler.post(new Runnable() {
                        public void run() {
                            synchronized (RemoteListenerHelper.this.mListenerMap) {
                                RemoteListenerHelper.this.foreachUnsafe(RemoteListenerHelper.this.getHandlerOperation(AnonymousClass1.this.registrationState));
                            }
                        }
                    });
                }
            }
        });
    }

    private void tryUnregister() {
        this.mHandler.post(new Runnable() {
            public void run() {
                if (RemoteListenerHelper.this.mIsRegistered) {
                    RemoteListenerHelper.this.unregisterFromService();
                    boolean unused = RemoteListenerHelper.this.mIsRegistered = false;
                }
            }
        });
    }

    private int calculateCurrentResultUnsafe() {
        if (!isAvailableInPlatform()) {
            return 1;
        }
        if (!this.mHasIsSupported || this.mListenerMap.isEmpty()) {
            return 5;
        }
        if (!this.mIsSupported) {
            return 2;
        }
        if (!isGpsEnabled()) {
            return 3;
        }
        return 0;
    }
}
