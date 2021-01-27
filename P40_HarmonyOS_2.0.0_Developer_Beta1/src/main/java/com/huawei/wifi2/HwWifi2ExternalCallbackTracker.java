package com.huawei.wifi2;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.wifi.HwHiLog;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwWifi2ExternalCallbackTracker<T> {
    private static final int NUM_CALLBACKS_WARN_LIMIT = 10;
    private static final int NUM_CALLBACKS_WTF_LIMIT = 20;
    private static final String TAG = "HwWifi2ExternalCallbackTracker";
    private final Map<Integer, ExternalCallbackHolder<T>> mCallbacks = new HashMap();
    private final Handler mHandler;

    public HwWifi2ExternalCallbackTracker(Handler handler) {
        this.mHandler = handler;
    }

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
                HwHiLog.e(HwWifi2ExternalCallbackTracker.TAG, false, "Error on linkToDeath ", new Object[0]);
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
            HwHiLog.i(HwWifi2ExternalCallbackTracker.TAG, false, "Binder died %{public}s", new Object[]{this.mBinder});
        }
    }

    public boolean add(IBinder binder, T callbackObject, int callbackIdentifier) {
        ExternalCallbackHolder<T> externalCallback = ExternalCallbackHolder.createAndLinkToDeath(binder, callbackObject, new ExternalCallbackHolder.DeathCallback(callbackIdentifier) {
            /* class com.huawei.wifi2.$$Lambda$HwWifi2ExternalCallbackTracker$Ka2WEW231umpcGO90SYOboK5s5w */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.huawei.wifi2.HwWifi2ExternalCallbackTracker.ExternalCallbackHolder.DeathCallback
            public final void onDeath() {
                HwWifi2ExternalCallbackTracker.this.lambda$add$1$HwWifi2ExternalCallbackTracker(this.f$1);
            }
        });
        if (externalCallback == null) {
            return false;
        }
        if (this.mCallbacks.containsKey(Integer.valueOf(callbackIdentifier)) && remove(callbackIdentifier)) {
            HwHiLog.i(TAG, false, "Replacing callback %{public}d", new Object[]{Integer.valueOf(callbackIdentifier)});
        }
        this.mCallbacks.put(Integer.valueOf(callbackIdentifier), externalCallback);
        HwHiLog.i(TAG, false, "call back size is %{public}d", new Object[]{Integer.valueOf(this.mCallbacks.size())});
        return true;
    }

    public /* synthetic */ void lambda$add$1$HwWifi2ExternalCallbackTracker(int callbackIdentifier) {
        this.mHandler.post(new Runnable(callbackIdentifier) {
            /* class com.huawei.wifi2.$$Lambda$HwWifi2ExternalCallbackTracker$JVlet4yqP9UvmYmHxVBH34S64IQ */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                HwWifi2ExternalCallbackTracker.this.lambda$add$0$HwWifi2ExternalCallbackTracker(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$add$0$HwWifi2ExternalCallbackTracker(int callbackIdentifier) {
        HwHiLog.i(TAG, false, "Remove external callback on death %{public}d", new Object[]{Integer.valueOf(callbackIdentifier)});
        remove(callbackIdentifier);
    }

    public boolean remove(int callbackIdentifier) {
        ExternalCallbackHolder<T> externalCallback = this.mCallbacks.remove(Integer.valueOf(callbackIdentifier));
        if (externalCallback == null) {
            HwHiLog.e(TAG, false, "Unknown external callback %{public}d", new Object[]{Integer.valueOf(callbackIdentifier)});
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
