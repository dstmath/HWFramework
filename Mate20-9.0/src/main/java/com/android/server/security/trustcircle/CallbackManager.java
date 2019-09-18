package com.android.server.security.trustcircle;

import android.os.IBinder;
import android.os.RemoteException;
import com.android.server.security.trustcircle.utils.LogHelper;
import huawei.android.security.ILifeCycleCallback;

public class CallbackManager {
    private static final String TAG = CallbackManager.class.getSimpleName();
    private static volatile CallbackManager sInstance;
    private IBinder.DeathRecipient mILifeCycleBinderDieListener = new IBinder.DeathRecipient() {
        public void binderDied() {
            ILifeCycleCallback unused = CallbackManager.this.mILifeCycleCallback = null;
        }
    };
    /* access modifiers changed from: private */
    public volatile ILifeCycleCallback mILifeCycleCallback = null;

    private CallbackManager() {
    }

    public static CallbackManager getInstance() {
        if (sInstance == null) {
            synchronized (CallbackManager.class) {
                if (sInstance == null) {
                    sInstance = new CallbackManager();
                }
            }
        }
        return sInstance;
    }

    public boolean registerILifeCycleCallback(ILifeCycleCallback callback) {
        if (callback == null || this.mILifeCycleCallback != null) {
            String str = TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("error, register ILifeCycleCallback failed: ");
            sb.append(callback == null ? "registered callback is null" : this.mILifeCycleCallback != null ? "previous callback is not null" : "");
            LogHelper.e(str, sb.toString());
            return false;
        }
        this.mILifeCycleCallback = callback;
        try {
            this.mILifeCycleCallback.asBinder().linkToDeath(this.mILifeCycleBinderDieListener, 0);
            return true;
        } catch (RemoteException e) {
            String str2 = TAG;
            LogHelper.e(str2, "RemoteException in registerILifeCycleCallback: " + e.getMessage());
            this.mILifeCycleCallback = null;
            return false;
        }
    }

    public boolean isILifeCycleCallbackValid() {
        return this.mILifeCycleCallback != null;
    }

    public ILifeCycleCallback getILifeCycleCallback() {
        return this.mILifeCycleCallback;
    }

    public void unregisterILifeCycleCallback() {
        this.mILifeCycleCallback = null;
    }
}
