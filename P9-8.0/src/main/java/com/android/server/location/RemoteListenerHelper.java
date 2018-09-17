package com.android.server.location;

import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
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
    protected static final int RESULT_NOT_AVAILABLE = 1;
    protected static final int RESULT_NOT_SUPPORTED = 2;
    protected static final int RESULT_SUCCESS = 0;
    protected static final int RESULT_UNKNOWN = 5;
    private final Handler mHandler;
    private boolean mHasIsSupported;
    private boolean mIsRegistered;
    private boolean mIsSupported;
    private int mLastReportedResult = 5;
    private final Map<IBinder, LinkedListener> mListenerMap = new HashMap();
    private final String mTag;

    protected interface ListenerOperation<TListener extends IInterface> {
        void execute(TListener tListener) throws RemoteException;
    }

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
                Log.v(RemoteListenerHelper.this.mTag, "mName = " + this.mName + " Error in monitored listener.", e);
            }
        }
    }

    private class LinkedListener implements DeathRecipient {
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
            Log.d(RemoteListenerHelper.this.mTag, "Remote Listener died: " + this.mListener + " of " + this.mPkgName);
            RemoteListenerHelper.this.removeListener(this.mListener);
        }

        public void setPkgName(String pkgName) {
            this.mPkgName = pkgName;
        }

        public String getPkgName() {
            return this.mPkgName;
        }
    }

    protected abstract ListenerOperation<TListener> getHandlerOperation(int i);

    protected abstract boolean isAvailableInPlatform();

    protected abstract boolean isGpsEnabled();

    protected abstract boolean registerWithService();

    protected abstract void unregisterFromService();

    protected RemoteListenerHelper(Handler handler, String name) {
        Preconditions.checkNotNull(name);
        this.mHandler = handler;
        this.mTag = name;
    }

    public boolean addListener(TListener listener, String pkgName) {
        if (!addListener(listener)) {
            return false;
        }
        synchronized (this.mListenerMap) {
            LinkedListener deathListener = (LinkedListener) this.mListenerMap.get(listener.asBinder());
            if (deathListener != null) {
                deathListener.setPkgName(pkgName);
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:40:0x006c, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean addListener(TListener listener) {
        Preconditions.checkNotNull(listener, "Attempted to register a 'null' listener.");
        IBinder binder = listener.asBinder();
        LinkedListener deathListener = new LinkedListener(listener);
        synchronized (this.mListenerMap) {
            if (this.mListenerMap.containsKey(binder)) {
                return true;
            }
            try {
                int result;
                binder.linkToDeath(deathListener, 0);
                this.mListenerMap.put(binder, deathListener);
                if (!isAvailableInPlatform()) {
                    result = 1;
                } else if (this.mHasIsSupported && (this.mIsSupported ^ 1) != 0) {
                    result = 2;
                } else if (!isGpsEnabled()) {
                    result = 3;
                } else if (!tryRegister()) {
                    result = 4;
                } else if (this.mHasIsSupported && this.mIsSupported) {
                    result = 0;
                }
                post(listener, getHandlerOperation(result), "");
                return true;
            } catch (RemoteException e) {
                Log.v(this.mTag, "Remote listener already died.", e);
                return false;
            }
        }
    }

    public void removeListener(TListener listener) {
        LinkedListener linkedListener;
        Preconditions.checkNotNull(listener, "Attempted to remove a 'null' listener.");
        IBinder binder = listener.asBinder();
        synchronized (this.mListenerMap) {
            linkedListener = (LinkedListener) this.mListenerMap.remove(binder);
            if (this.mListenerMap.isEmpty()) {
                tryUnregister();
            }
        }
        if (linkedListener != null) {
            binder.unlinkToDeath(linkedListener, 0);
        }
    }

    protected void foreach(ListenerOperation<TListener> operation) {
        synchronized (this.mListenerMap) {
            foreachUnsafe(operation);
        }
    }

    protected void foreachDirect(ListenerOperation<TListener> operation) {
        synchronized (this.mListenerMap) {
            for (LinkedListener linkedListener : this.mListenerMap.values()) {
                if (!GpsFreezeProc.getInstance().isFreeze(linkedListener.getPkgName())) {
                    if (linkedListener.mSkipCount.get() > 0) {
                        linkedListener.mSkipCount.getAndDecrement();
                    } else {
                        try {
                            operation.execute(linkedListener.getUnderlyingListener());
                        } catch (RemoteException e) {
                            Log.e(this.mTag, "Error in monitored listener to " + linkedListener.getPkgName());
                            linkedListener.mSkipCount.set(1000);
                        }
                    }
                }
            }
        }
    }

    protected void setSupported(boolean value) {
        synchronized (this.mListenerMap) {
            this.mHasIsSupported = true;
            this.mIsSupported = value;
        }
    }

    protected boolean tryUpdateRegistrationWithService() {
        synchronized (this.mListenerMap) {
            if (!isGpsEnabled()) {
                tryUnregister();
                return true;
            } else if (this.mListenerMap.isEmpty()) {
                return true;
            } else if (tryRegister()) {
                return true;
            } else {
                foreachUnsafe(getHandlerOperation(4));
                return false;
            }
        }
    }

    protected void updateResult() {
        synchronized (this.mListenerMap) {
            int newResult = calculateCurrentResultUnsafe();
            if (this.mLastReportedResult == newResult) {
                return;
            }
            foreachUnsafe(getHandlerOperation(newResult));
            this.mLastReportedResult = newResult;
        }
    }

    private void foreachUnsafe(ListenerOperation<TListener> operation) {
        for (LinkedListener linkedListener : this.mListenerMap.values()) {
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

    private boolean tryRegister() {
        if (!this.mIsRegistered) {
            this.mIsRegistered = registerWithService();
        }
        return this.mIsRegistered;
    }

    private void tryUnregister() {
        if (this.mIsRegistered) {
            unregisterFromService();
            this.mIsRegistered = false;
        }
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
        if (isGpsEnabled()) {
            return 0;
        }
        return 3;
    }
}
