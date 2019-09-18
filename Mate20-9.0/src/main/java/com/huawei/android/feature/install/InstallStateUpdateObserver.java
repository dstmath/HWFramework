package com.huawei.android.feature.install;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.android.feature.install.config.RemoteConfig;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InstallStateUpdateObserver {
    /* access modifiers changed from: private */
    public static final String TAG = InstallStateUpdateObserver.class.getSimpleName();
    protected Context mContext;
    private IntentFilter mIntentFilter;
    private Set<InstallStateUpdatedListener> mListeners = Collections.newSetFromMap(new ConcurrentHashMap());
    protected RemoteConfig mRemoteConfig;
    private l mStateUpdateReceiver = new l(this, (byte) 0);

    protected InstallStateUpdateObserver(Context context, RemoteConfig remoteConfig) {
        this.mContext = context;
        this.mRemoteConfig = remoteConfig;
        this.mIntentFilter = new IntentFilter(getIntentFilterAction());
    }

    /* access modifiers changed from: protected */
    public abstract String getIntentFilterAction();

    public abstract void handleStateUpdate(Intent intent);

    public final synchronized void notifyState(InstallSessionState installSessionState) {
        for (InstallStateUpdatedListener onStateUpdate : this.mListeners) {
            onStateUpdate.onStateUpdate(installSessionState);
        }
    }

    public synchronized void registerListener(InstallStateUpdatedListener installStateUpdatedListener) {
        this.mListeners.add(installStateUpdatedListener);
        if (this.mListeners.size() == 1) {
            this.mContext.registerReceiver(this.mStateUpdateReceiver, this.mIntentFilter);
        }
    }

    public synchronized void unregisterListener(InstallStateUpdatedListener installStateUpdatedListener) {
        this.mListeners.remove(installStateUpdatedListener);
        if (this.mListeners.isEmpty()) {
            this.mContext.unregisterReceiver(this.mStateUpdateReceiver);
        }
    }
}
