package com.android.server.wifi.util;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExternalCallbackTracker<T> {
    private static final int NUM_CALLBACKS_WARN_LIMIT = 10;
    private static final int NUM_CALLBACKS_WTF_LIMIT = 20;
    private static final String TAG = "WifiExternalCallbackTracker";
    private final Map<Integer, ExternalCallbackHolder<T>> mCallbacks = new HashMap();
    private final Handler mHandler;

    /* access modifiers changed from: private */
    public static class ExternalCallbackHolder<T> implements IBinder.DeathRecipient {
        private final IBinder mBinder;
        private final T mCallbackObject;
        private final DeathCallback mDeathCallback;

        public interface DeathCallback {
            void onDeath();
        }

        private ExternalCallbackHolder(IBinder binder, T callbackObject, DeathCallback deathCallback) {
            this.mBinder = (IBinder) Preconditions.checkNotNull(binder);
            this.mCallbackObject = (T) Preconditions.checkNotNull(callbackObject);
            this.mDeathCallback = (DeathCallback) Preconditions.checkNotNull(deathCallback);
        }

        public static <T> ExternalCallbackHolder createAndLinkToDeath(IBinder binder, T callbackObject, DeathCallback deathCallback) {
            ExternalCallbackHolder<T> externalCallback = new ExternalCallbackHolder<>(binder, callbackObject, deathCallback);
            try {
                binder.linkToDeath(externalCallback, 0);
                return externalCallback;
            } catch (RemoteException e) {
                Log.e(ExternalCallbackTracker.TAG, "Error on linkToDeath - " + e);
                return null;
            }
        }

        public void reset() {
            this.mBinder.unlinkToDeath(this, 0);
        }

        public T getCallback() {
            return this.mCallbackObject;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            this.mDeathCallback.onDeath();
            Log.d(ExternalCallbackTracker.TAG, "Binder died " + this.mBinder);
        }
    }

    public ExternalCallbackTracker(Handler handler) {
        this.mHandler = handler;
    }

    public boolean add(IBinder binder, T callbackObject, int callbackIdentifier) {
        ExternalCallbackHolder<T> externalCallback = ExternalCallbackHolder.createAndLinkToDeath(binder, callbackObject, new ExternalCallbackHolder.DeathCallback(callbackIdentifier) {
            /* class com.android.server.wifi.util.$$Lambda$ExternalCallbackTracker$8QoY6PIJITD7KnJG1izwMVWuxyA */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.server.wifi.util.ExternalCallbackTracker.ExternalCallbackHolder.DeathCallback
            public final void onDeath() {
                ExternalCallbackTracker.this.lambda$add$1$ExternalCallbackTracker(this.f$1);
            }
        });
        if (externalCallback == null) {
            return false;
        }
        if (this.mCallbacks.containsKey(Integer.valueOf(callbackIdentifier)) && remove(callbackIdentifier)) {
            Log.d(TAG, "Replacing callback " + callbackIdentifier);
        }
        this.mCallbacks.put(Integer.valueOf(callbackIdentifier), externalCallback);
        if (this.mCallbacks.size() > 20) {
            Log.wtf(TAG, "Too many callbacks: " + this.mCallbacks.size());
            return true;
        } else if (this.mCallbacks.size() <= 10) {
            return true;
        } else {
            Log.w(TAG, "Too many callbacks: " + this.mCallbacks.size());
            return true;
        }
    }

    public /* synthetic */ void lambda$add$1$ExternalCallbackTracker(int callbackIdentifier) {
        this.mHandler.post(new Runnable(callbackIdentifier) {
            /* class com.android.server.wifi.util.$$Lambda$ExternalCallbackTracker$KYTcA3u6_AoU2fIQJVi9Ivbg58 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ExternalCallbackTracker.this.lambda$add$0$ExternalCallbackTracker(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$add$0$ExternalCallbackTracker(int callbackIdentifier) {
        Log.d(TAG, "Remove external callback on death " + callbackIdentifier);
        remove(callbackIdentifier);
    }

    public boolean remove(int callbackIdentifier) {
        ExternalCallbackHolder<T> externalCallback = this.mCallbacks.remove(Integer.valueOf(callbackIdentifier));
        if (externalCallback == null) {
            Log.w(TAG, "Unknown external callback " + callbackIdentifier);
            return false;
        }
        externalCallback.reset();
        return true;
    }

    public List<T> getCallbacks() {
        List<T> callbacks = new ArrayList<>();
        for (ExternalCallbackHolder<T> externalCallback : this.mCallbacks.values()) {
            callbacks.add(externalCallback.getCallback());
        }
        return callbacks;
    }

    public int getNumCallbacks() {
        return this.mCallbacks.size();
    }

    public void clear() {
        for (ExternalCallbackHolder<T> externalCallback : this.mCallbacks.values()) {
            externalCallback.reset();
        }
        this.mCallbacks.clear();
    }
}
