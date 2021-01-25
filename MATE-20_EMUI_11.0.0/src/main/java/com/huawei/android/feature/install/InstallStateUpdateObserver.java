package com.huawei.android.feature.install;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InstallStateUpdateObserver {
    private static final String TAG = InstallStateUpdateObserver.class.getSimpleName();
    protected Context mContext;
    private IntentFilter mIntentFilter;
    private Set<InstallStateUpdatedListener> mListeners = Collections.newSetFromMap(new ConcurrentHashMap());

    protected InstallStateUpdateObserver(Context context) {
        this.mContext = context;
        this.mIntentFilter = new IntentFilter(getIntentFilterAction());
    }

    /* access modifiers changed from: protected */
    public abstract String getIntentFilterAction();

    /* access modifiers changed from: protected */
    public abstract void handleStateUpdate(Intent intent);

    public final synchronized void notifyState(InstallSessionState installSessionState) {
        for (InstallStateUpdatedListener installStateUpdatedListener : this.mListeners) {
            installStateUpdatedListener.onStateUpdate(installSessionState);
        }
    }
}
